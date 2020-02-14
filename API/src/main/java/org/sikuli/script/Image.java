/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.SXOpenCV;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * This class hides the complexity behind image names given as string.
 * <br>It's companion is {@link ImagePath} that maintains a list of places, where image files are
 * loaded from.<br>
 * An Image object:<br>
 * - has a name, either given or taken from the basename<br>
 * - keeps it's in memory buffered image in a configurable cache avoiding reload
 * from source<br>
 * - remembers, where it was found when searched the last time<br>
 * - can be sourced from the filesystem, from jars, from the web and from other
 * in memory images <br>
 * - will have features for basic image manipulation and presentation<br>
 * - contains the stuff to communicate with the underlying OpenCV based search
 * engine <br>
 * <p>
 * This class maintains<br>
 * - a list of all images ever loaded in this session with their source
 * reference and a ref to the image object<br>
 * - a list of all images currently having their content in memory (buffered
 * image) (managed as a configurable cache)<br>
 * The caching can be configured using {@link Settings#setImageCache(int)}
 */
public class Image extends Element {

  void log() {
    //TODO ****************** debug stop
    log(-1, "DebugStop");
  }

  //<editor-fold desc="000  instance">
  public static Image getDefaultInstance4py() {
    return new Image(new Screen().capture());
  }

  protected boolean isOnScreen() {
    return false;
  }

  protected Image() {
  }

  /**
   * Create an Image from various sources.
   * <pre>
   * - from a file (String, URL, File)
   * - from an {@link Element} (Region, Image, ...)
   * - from a BufferedImage or OpenCV Mat
   * - from a Pattern including the Pattern specific attributes
   * </pre>
   *
   * @param what      the source
   * @param <SUFEBMP> see source variants
   */
  public <SUFEBMP> Image(SUFEBMP what) {
    this(what, "");
  }

  /**
   * Create an Image from various sources.
   * <pre>
   * - from a file (String, URL, File)
   * - from an {@link Element} (Region, Image, ...)
   * - from a BufferedImage or OpenCV Mat
   * </pre>
   *
   * @param what      the source
   * @param <SUFEBMP> see source variants
   * @param name      to identify non-file images
   */
  //TODO is an image name really needed?
  public <SUFEBMP> Image(SUFEBMP what, String name) {
    sourceClass = what.getClass().getSimpleName();
    if (what instanceof Pattern) {
      init((Pattern) what);
    } else if (what instanceof String) {
      init((String) what);
    } else if (what instanceof File) {
      init((File) what);
    } else if (what instanceof URL) {
      init((URL) what);
    } else if (what instanceof Element) {
      init((Element) what);
    } else if (what instanceof Mat) {
      init((Mat) what);
    }
  }

  /**
   * Creates an Image from a resource file on classpath.
   *
   * @param clazz    a class found on classpath as reference
   * @param resource the resource identifier (.png is assumed)
   */
  public Image(Class clazz, String resource) {
    if (!new File(resource).isAbsolute()) {
      resource = "/" + resource;
    }
    init("class://" + clazz.getCanonicalName() + resource);
  }

  protected void copyElementAttributes(Element element) {
    super.copyElementAttributes(element);
    setName(element.getName());
    ((Image) element).setFileURL(fileURL);
    ((Image) element).setIsAbsolute(imageIsAbsolute);
    ((Image) element).setIsText(imageIsText);
    ((Image) element).setIsBundled(imageIsBundled);
    ((Image) element).setLastSeen(getLastSeen(), getLastSeenScore());
  }

  private void init(Pattern pattern) {

  }

  private void init(String filename) {
    if (filename.isEmpty()) {
      initTerminate("%s", "init: filename is empty");
    }
    filename = getValidImageFilename(filename);
    try {
      URI uri = new URI(filename);
      if (uri.getScheme() != null && !"file".equals(uri.getScheme())) {
        init(uri);
      }
    } catch (URISyntaxException e) {
    }
    init(new File(filename));
  }

  private void init(File file) {
    if (null == file) {
      initTerminate("%s", "init: file is null");
    }
    URL imageURL = null;
    if (file.isAbsolute() || file.getPath().startsWith("\\")) {
      try {
        imageURL = file.toURI().toURL();
      } catch (MalformedURLException e) {
      }
    } else {
      imageURL = ImagePath.find(file.getPath());
    }
    if (imageURL == null || !new File(imageURL.getPath()).exists()) {
      initTerminate("init: %s file not found or path not valid", file);
    }
    url(imageURL);
    if (isCaching()) {
      setContent(ImageCache.get(imageURL));
    }
    if (!isValid()) {
      setContent(Imgcodecs.imread(fileName(), -1));
      if (isValid() && isCaching()) {
        ImageCache.put(imageURL, getContent());
      }
    }
    if (isValid()) {
      setSize(getContent());
    }
  }

  private void init(URL url) {
    if (isFile(url)) {
      File file = asFile(url);
      if (null == file) {
        initTerminate( "%s url not valid", url);
      }
      init(file);
    }
  }

  private void init(URI uri) {
    URL resource = null;
    if (uri.getScheme().equals("class")) {
      Class clazz = null;
      try {
        clazz = Class.forName(uri.getAuthority());
      } catch (ClassNotFoundException e) {
        initTerminate("%s class not found", uri);
      }
      resource = clazz.getResource(uri.getPath());
      if (resource == null) {
        initTerminate("%s (not found)", uri);
      }
    } else if (uri.getScheme().startsWith("http")) {
      try {
        resource = uri.toURL();
      } catch (MalformedURLException e) {
        initTerminate("%s uri not valid", uri);
      }
    }
    if (null != resource) {
      try {
        InputStream inputStream = resource.openStream();
        byte[] bytes = inputStream.readAllBytes();
        MatOfByte matOfByte = new MatOfByte();
        matOfByte.fromArray(bytes);
        Mat content = Imgcodecs.imdecode(matOfByte, -1);
        setContentAndSize(content);
      } catch (IOException e) {
        initTerminate("%s io error", uri);
      }
    }
  }

  private void init(Element element) {
    copyElementRectangle(element);
    if (element.isOnScreen()) {
      setContent(element.getImage().getContent());
    } else {
      copyElementRectangle(element);
      copyElementContent(element);
      url(element.url());
    }
  }

  private void init(Mat mat) {
    setContent(mat);
    w = mat.cols();
    h = mat.rows();
  }

  private void initTerminate(String message, Object item) {
    terminate("init: " + message, item);
  }

  public void reloadContent() {
    if (url() != null) {
      init(url());
    }
  }

  private static boolean isFile(URL url) {
    return url.getProtocol().equals("file");
  }

  private static File asFile(URL url) {
    File file = null;
    if (url.getProtocol().equals("file")) {
      try {
        file = new File(url.getPath()).getCanonicalFile();
      } catch (IOException e) {
      }
    }
    return file;
  }

  /**
   * @return the image's absolute filename or null if jar, http or in memory
   * image
   */
  public String getFilename() {
    if (url() != null && "file".equals(url().getProtocol())) {
      return new File(url().getPath()).getAbsolutePath();
    } else {
      return getName();
    }
  }
  //</editor-fold>

  //<editor-fold desc="002 Pattern aspects">
  private <PE> void copyPatternAttributes(PE source) {
    if (source instanceof Pattern) {
      similarity = ((Pattern) source).getSimilar();
      offset = ((Pattern) source).getTargetOffset();
      waitAfter = ((Pattern) source).waitAfter();
    } else if (source instanceof Image) {
      similarity = ((Image) source).similarity;
      offset = ((Image) source).offset;
      waitAfter = ((Image) source).waitAfter;
    }
  }

  private double similarity = Settings.MinSimilarity;

  /**
   * Get the value of similarity
   *
   * @return the value of similarity
   */
  public double similarity() {
    return similarity;
  }

  /**
   * Set the value of similarity
   *
   * @param similarity new value of similarity
   * @return the image
   */
  public Image similarity(double similarity) {
    this.similarity = similarity;
    return this;
  }

  private Location offset = new Location(0, 0);

  /**
   * Get the value of offset
   *
   * @return the value of offset
   */
  public Location offset() {
    return offset;
  }

  /**
   * Set the value of offset
   *
   * @param offset new value of offset
   * @return the image
   */
  public Image offset(Location offset) {
    this.offset = offset;
    return this;
  }

  private int waitAfter;

  /**
   * Get the value of waitAfter
   *
   * @return the value of waitAfter
   */
  public int waitAfter() {
    return waitAfter;
  }

  /**
   * Set the value of waitAfter
   *
   * @param waitAfter new value of waitAfter
   * @return the image
   */
  public Image waitAfter(int waitAfter) {
    this.waitAfter = waitAfter;
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="003 lastSeen">
  private Rectangle lastSeen = null;
  private double lastScore = 0.0;

  /**
   * if the image was already found before
   *
   * @return the rectangle where it was found
   */
  public Rectangle getLastSeen() {
    return lastSeen;
  }

  /**
   * if the image was already found before
   *
   * @return the similarity score
   */
  public double getLastSeenScore() {
    return lastScore;
  }

  /**
   * Internal Use: set the last seen info after a find
   *
   * @param lastSeen Match
   * @param sim      SimilarityScore
   * @return the image
   */
  public Image setLastSeen(Rectangle lastSeen, double sim) {
    this.lastSeen = lastSeen;
    this.lastScore = sim;
    return this;
  }
  //</editor-fold>

  public double diffPercentage(Image otherImage) {
    if (SX.isNull(otherImage)) {
      return 1.0;
    }
    return SXOpenCV.diffPercentage(getContent(), otherImage.getContent());
  }

  //<editor-fold defaultstate="collapsed" desc="00 1 URL">

  /**
   * @return the evaluated url for this image (might be null)
   */
  public URL getURL() {
    return url();
  }

  public Image setFileURL(URL fileURL) {
    this.fileURL = fileURL;
    return this;
  }

  private URL fileURL = null;

  public boolean isFile() {
    if (isValid()) {
      URL furl = getURL();
      if ("file".equals(furl.getProtocol())) {
        return true;
      }
    }
    return false;
  }

  private boolean imageIsAbsolute = false;

  /**
   * @return true if image was given with absolute filepath
   */
  public boolean isAbsolute() {
    return imageIsAbsolute;
  }

  public Image setIsAbsolute(boolean val) {
    imageIsAbsolute = val;
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="00 3 isText">
  private boolean imageIsText = false;

  /**
   * @return true if the given image name did not give a valid image so it might
   * be text to search
   */
  public boolean isText() {
    return imageIsText;
  }

  public Image setIsText(boolean val) {
    imageIsText = val;
    return this;
  }

  public String getNameAsText() {
    return imageNameGiven;
  }

  private String imageNameGiven = null;
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="00 5 isBundled">
  private boolean imageIsBundled = false;

  private Image setIsBundled(boolean imageIsBundled) {
    this.imageIsBundled = imageIsBundled;
    return this;
  }

  /**
   * INTERNAL USE: image is contained in a bundle (.sikuli)
   *
   * @return true/false
   */
  public boolean isBundled() {
    return imageIsBundled;
  }

//</editor-fold>

  //<editor-fold desc="800 create obsolete">

  /**
   * create a new Image as copy of the given Image
   *
   * @param image given Image
   * @return new Image
   */
  public static Image create(Image image) {
    return new Image(image);
  }

  /**
   * create a new image from a filename <br>
   * file ending .png is added if missing (currently valid: png, jpg, jpeg)<br>
   * relative filename: [...path.../]name[.png] is searched on current image path<br>
   * absolute filename is taken as is
   * if image exists, it is loaded to cache <br>
   * already loaded image with same name (given path) is reused (taken from cache) <br>
   * <p>
   * if image not found, it might be a text to be searched (imageIsText = true)
   *
   * @param fName image filename
   * @return an Image object (might not be valid - check with isValid())
   */
  public static Image create(String fName) {
    return new Image(fName);
  }

  /**
   * create a new image from the given file <br>
   * file ending .png is added if missing (currently valid: png, jpg, jpeg)<br>
   * relative filename: [...path.../]name[.png] is searched on current image path<br>
   * absolute filename is taken as is
   * if image exists, it is loaded to cache <br>
   * already loaded image with same name (given path) is reused (taken from cache) <br>
   * <p>
   * if image not found, it might be a text to be searched (imageIsText = true)
   *
   * @param imageFile a Java File object
   * @return an Image object (might not be valid - check with isValid())
   */
  public static Image create(File imageFile) {
    return new Image(imageFile);
  }

  /**
   * create a new image from the given url <br>
   * file ending .png is added if missing <br>
   * filename: ...url-path.../name[.png] is loaded from the url and and cached
   * <br>
   * already loaded image with same url is reused (reference) and taken from
   * cache
   *
   * @param url image file URL
   * @return the image
   */
  public static Image create(URL url) {
    return new Image(url);
  }

  /**
   * create a sub image from this image
   *
   * @param part (the constants Region.XXX as used with {@link Region#getTile(int)})
   * @return the sub image
   */
  public Image getSub(int part) {
    Rectangle r = Region.getRectangle(new Rectangle(0, 0, getSize().width, getSize().height), part);
    return getSub(r.x, r.y, r.width, r.height);
  }

  public Image getSub(Region reg) {
    return getSub(reg.x, reg.y, reg.w, reg.h);
  }

  /**
   * create a sub image from this image
   *
   * @param x pixel column
   * @param y pixel row
   * @param w width
   * @param h height
   * @return the new image
   */
  public Image getSub(int x, int y, int w, int h) {
    BufferedImage bi = createBufferedImage(w, h);
    Graphics2D g = bi.createGraphics();
    g.drawImage(getBufferedImage().getSubimage(x, y, w, h), 0, 0, null);
    g.dispose();
    return new Image(bi);
  }

  private static BufferedImage createBufferedImage(int w, int h) {
    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int[] nBits = {8, 8, 8, 8};
    ColorModel cm = new ComponentColorModel(cs, nBits, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    SampleModel sm = cm.createCompatibleSampleModel(w, h);
    DataBufferByte db = new DataBufferByte(w * h * 4);
    WritableRaster r = WritableRaster.createWritableRaster(sm, db, new Point(0, 0));
    BufferedImage bm = new BufferedImage(cm, r, false, null);
    return bm;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="810 bufferedImage obsolete">
  private BufferedImage bimg = null;
  private int bsize = 0;

  public static BufferedImage getSubimage(BufferedImage bimg, Rectangle rect) {
    return bimg.getSubimage(rect.x, rect.y, (int) rect.getWidth(), (int) rect.getHeight());
  }

  public static BufferedImage createSubimage(BufferedImage bimg, Rectangle rect) {
    Rectangle crop;
    crop = new Rectangle(0, 0, bimg.getWidth(), bimg.getHeight()).intersection(rect);
    BufferedImage newBimg = new BufferedImage(crop.width, crop.height, bimg.getType());
    Graphics2D g2d = newBimg.createGraphics();
    g2d.drawImage(getSubimage(bimg, crop), 0, 0, null);
    g2d.dispose();
    return newBimg;
  }

  /**
   * Available resize interpolation algorithms
   */
  public enum Interpolation {
    NEAREST(Imgproc.INTER_NEAREST),
    LINEAR(Imgproc.INTER_LINEAR),
    CUBIC(Imgproc.INTER_CUBIC),
    AREA(Imgproc.INTER_AREA),
    LANCZOS4(Imgproc.INTER_LANCZOS4),
    LINEAR_EXACT(Imgproc.INTER_LINEAR_EXACT),
    MAX(Imgproc.INTER_MAX);

    public int value;

    Interpolation(int value) {
      this.value = value;
    }
  }

  /**
   * resize the Image in place with factor
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param factor resize factor
   * @return this Image resized
   * @see Interpolation
   */
  public Image size(float factor) {
    return size(factor, Interpolation.CUBIC);
  }

  /**
   * resize the Image in place with factor
   * <p>
   * Uses the given interpolation algorithm.
   *
   * @param factor        resize factor
   * @param interpolation algorithm {@link Interpolation}
   * @return this Image resized
   * @see Interpolation
   */
  public Image size(float factor, Interpolation interpolation) {
    SXOpenCV.resize(getContent(), factor, interpolation);
    return this;
  }

  /**
   * resize the Image with factor
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param factor resize factor
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public Object resize(float factor) {
    return resize(factor, Interpolation.CUBIC);
  }

  /**
   * resize the loaded image with factor using OpenCV ImgProc.resize()
   *
   * @param factor        resize factor
   * @param interpolation algorithm used for pixel interpolation
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public BufferedImage resize(float factor, Interpolation interpolation) {
    return getBufferedImage(SXOpenCV.cvResize(cloneContent(), factor, interpolation));
  }

  /**
   * resize the given image with factor using OpenCV ImgProc.resize()
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param bimg   given image
   * @param factor resize factor
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public static BufferedImage resize(BufferedImage bimg, float factor) {
    return resize(bimg, factor, Interpolation.CUBIC);
  }

  private static BufferedImage resize(BufferedImage bimg, float factor, Interpolation interpolation) {
    return Element.getBufferedImage(SXOpenCV.cvResize(bimg, factor, interpolation));
  }

//</editor-fold>

  //<editor-fold desc="820 caching obsolete">
  private static class ImageCache {
    static Map<URL, Mat> cache = Collections.synchronizedMap(new HashMap<URL, Mat>());

    static Mat put(URL key, Mat value) {
      return cache.put(key, value);
    }

    static Mat get(URL key) {
      Mat content = cache.get(key);
      if (null == content) {
        return new Mat();
      }
      return content;
    }

    static void reset() {
      cache = Collections.synchronizedMap(new HashMap<URL, Mat>());
    }
  }

  private static List<Image> images = Collections.synchronizedList(new ArrayList<Image>());
  private static Map<URL, Image> imageFiles = Collections.synchronizedMap(new HashMap<URL, Image>());
  private static Map<String, URL> imageNames = Collections.synchronizedMap(new HashMap<String, URL>());
  private static final int KB = 1024;
  private static final int MB = KB * KB;
  private final static String isBImg = "__BufferedImage__";

  private static long currentMemory = 0;

  private static synchronized long currentMemoryChange(long size, long max) {
    long maxMemory = max;
    if (max < 0) {
      maxMemory = Settings.getImageCache() * MB;
      currentMemory += size;
    }
    if (currentMemory > maxMemory) {
      Image first;
      while (images.size() > 0 && currentMemory > maxMemory) {
        first = images.remove(0);
        first.bimg = null;
        currentMemory -= first.bsize;
      }
      if (maxMemory == 0) {
        currentMemory = 0;
      } else {
        currentMemory = Math.max(0, currentMemory);
      }
    }
    if (size < 0) {
      currentMemory = Math.max(0, currentMemory);
    }
    return currentMemory;
  }

  private static long currentMemoryUp(long size) {
    return currentMemoryChange(size, -1);
  }

  private static long currentMemoryDown(long size) {
    currentMemory -= size;
    currentMemory = Math.max(0, currentMemory);
    return currentMemoryChange(-size, -1);
  }

  private static long currentMemoryDownUp(int sizeOld, int sizeNew) {
    currentMemoryDown(sizeOld);
    return currentMemoryUp(sizeNew);
  }

  private static boolean isCaching() {
    return Settings.getImageCache() > 0;
  }

  public static void clearCache(int maxSize) {
    currentMemoryChange(0, maxSize);
  }

  public static void purge() {
    purge(ImagePath.getBundle());
  }

  public static void purge(ImagePath.PathEntry path) {
    if (path == null) {
      return;
    }
    purge(path.pathURL);
  }

  private static synchronized void purge(URL pathURL) {
    List<Image> imagePurgeList = new ArrayList<>();
    List<String> imageNamePurgeList = new ArrayList<>();
    URL imgURL;
    Image img;
    log(logLevel + 1, "purge: ImagePath: %s", pathURL.getPath());
    Iterator<Map.Entry<URL, Image>> it = imageFiles.entrySet().iterator();
    Map.Entry<URL, Image> entry;
    while (it.hasNext()) {
      entry = it.next();
      imgURL = entry.getKey();
      if (imgURL.toString().startsWith(pathURL.toString())) {
        log(logLevel + 1, "purge: URL: %s", imgURL.toString());
        img = entry.getValue();
        imagePurgeList.add(img);
        imageNamePurgeList.add(img.getName());
        it.remove();
      }
    }
    if (!imagePurgeList.isEmpty()) {
      Iterator<Image> bit = images.iterator();
      while (bit.hasNext()) {
        img = bit.next();
        if (imagePurgeList.contains(img)) {
          bit.remove();
          log(logLevel + 1, "purge: bimg: %s", img);
          currentMemoryDown(img.bsize);
        }
      }
    }
    for (String name : imageNamePurgeList) {
      imageNames.remove(name);
    }
  }

  private static void unCache(URL imgURL) {
    Image img = imageFiles.get(imgURL);
    if (img == null) {
      return;
    }
    currentMemoryDown(img.bsize);
    images.remove(img);
  }

  //TODO make obsolete
  public static void unCache(String fileName) {
    unCache(FileManager.makeURL(new File(fileName).getAbsolutePath()));
  }

  /**
   * clears all caches (should only be needed for debugging)
   */
  public static void reset() {
    clearCache(0);
    imageNames.clear();
    imageFiles.clear();
  }

  public File remove() {
    URL furl = null;
    if (isFile()) {
      furl = getURL();
      unCache(furl);
      return new File(furl.getPath());
    }
    return null;
  }

  public void delete() {
    File fImg = remove();
    if (null != fImg) FileManager.deleteFileOrFolder(fImg);
  }

  private String hasBackup = "";

  public boolean backup() {
    if (isValid()) {
      File fOrg = new File(fileURL.getPath());
      File fBack = new File(fOrg.getParentFile(), "_BACKUP_" + fOrg.getName());
      if (FileManager.xcopy(fOrg, fBack)) {
        hasBackup = fBack.getPath();
        log(logLevel, "backup: %s created", fBack.getName());
        return true;
      }
      log(-1, "backup: %s did not work", fBack.getName());
    }
    return false;
  }

  public boolean restore() {
    if (!hasBackup.isEmpty()) {
      File fBack = new File(hasBackup);
      File fOrg = new File(hasBackup.replace("_BACKUP_", ""));
      if (FileManager.xcopy(fBack, fOrg)) {
        log(logLevel, "restore: %s restored", fOrg.getName());
        FileManager.deleteFileOrFolder(fBack);
        hasBackup = "";
        return true;
      }
      log(-1, "restore: %s did not work", fBack.getName());
    }
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="03 load/save">
//  private static Image get(String fName) {
//    if (fName == null || fName.isEmpty()) {
//      return null;
//    }
//    Image image = null;
//    if (fName.startsWith("\t") && fName.endsWith("\t")) {
//      fName = fName.substring(1, fName.length() - 1);
//      image = new Image();
//      image.setIsText(true);
//    } else {
//      URL imageURL = null;
//      String imageFileName = getValidImageFilename(fName);
//      if (imageFileName.isEmpty()) {
//        log(-1, "not a valid image type: " + fName);
//        imageFileName = fName;
//      }
//      File imageFile = new File(imageFileName);
//      if (imageFile.isAbsolute() && imageFile.exists()) {
//        try {
//          imageURL = new URL("file", null, imageFile.getPath());
//        } catch (MalformedURLException e) {
//        }
//      } else {
//        imageURL = imageNames.get(imageFileName);
//        if (imageURL == null) {
//          imageURL = ImagePath.find(imageFileName);
//        }
//      }
//      if (imageURL != null) {
//        image = imageFiles.get(imageURL);
//        if (image != null && null == imageNames.get(image.getName())) {
//          imageNames.put(image.getName(), imageURL);
//        }
//      }
//      if (image == null) {
//        image = new Image(imageURL);
//        image.setIsAbsolute(imageFile.isAbsolute());
//      } else {
//        if (image.bimg != null) {
//          log(3, "reused: %s (%s)", image.getName(), image.fileURL);
//        } else {
//          if (Settings.getImageCache() > 0) {
//            image.load();
//          }
//        }
//      }
//    }
//    image.imageNameGiven = fName;
//    return image;
//  }

//  private BufferedImage load() {
//    BufferedImage bImage = null;
//    if (fileURL != null) {
//      bimg = null;
//      try {
//        bImage = ImageIO.read(fileURL);
//      } catch (Exception e) {
//        log(-1, "load: failed: %s", fileURL);
//        fileURL = null;
//        return null;
//      }
//      if (getName() != null) {
//        imageFiles.put(fileURL, this);
//        imageNames.put(getName(), fileURL);
//        w = bImage.getWidth();
//        h = bImage.getHeight();
//        bsize = bImage.getData().getDataBuffer().getSize();
//        log(logLevel, "loaded: %s (%s)", getName(), fileURL);
//        if (isCaching()) {
//          currentMemoryUp(bsize);
//          bimg = bImage;
//          images.add(this);
//        }
//      } else {
//        log(-1, "invalid! not loaded! %s", fileURL);
//      }
//    }
//    return bImage;
//  }

  private BufferedImage loadAgain() {
    BufferedImage bImage = null;
    if (fileURL != null) {
      bimg = null;
      try {
        bImage = ImageIO.read(fileURL);
      } catch (Exception e) {
        log(-1, "loadAgain: failed: %s", fileURL);
        imageFiles.remove(fileURL);
        return null;
      }
      imageFiles.put(fileURL, this);
      imageNames.put(getName(), fileURL);
      w = bImage.getWidth();
      h = bImage.getHeight();
      bsize = bImage.getData().getDataBuffer().getSize();
      log(logLevel, "loaded again: %s (%s)", getName(), fileURL);
    }
    return bImage;
  }

  public static void reload(String fpImage) {
//    URL uImage = FileManager.makeURL(fpImage);
    URL uImage = imageNames.get(fpImage);
    if (imageFiles.containsKey(uImage)) {
      Image image = imageFiles.get(uImage);
      int sizeOld = image.bsize;
      if (null != image.loadAgain()) {
        currentMemoryDownUp(sizeOld, image.bsize);
        image.setLastSeen(null, 0);
      }
    }
  }

  public static void setIDEshouldReload(Image img) {
    ideShouldReload = true;
    img.wasRecaptured = true;
    img.lastSeen = null;
  }

  public static boolean getIDEshouldReload() {
    boolean state = ideShouldReload;
    ideShouldReload = false;
    return state;
  }

  private static boolean ideShouldReload = false;

  public boolean isRecaptured() {
    boolean state = wasRecaptured;
    wasRecaptured = false;
    return state;
  }

  public boolean wasRecaptured = false;
  //</editor-fold>

  //<editor-fold desc="10 raster">
  /**
   * to support a raster over the image
   */
  private int rows = 0;
  private int cols = 0;
  private int rowH = 0;
  private int colW = 0;
  private int rowHd = 0;
  private int colWd = 0;

  /**
   * store info: this image is divided vertically into n even rows <br>
   * a preparation for using getRow()
   *
   * @param n number of rows
   * @return the top row
   */
  public Image setRows(int n) {
    return setRaster(n, 0);
  }

  /**
   * store info: this image is divided horizontally into n even columns <br>
   * a preparation for using getCol()
   *
   * @param n number of Columns
   * @return the leftmost column
   */
  public Image setCols(int n) {
    return setRaster(0, n);
  }

  /**
   * @return number of eventually defined rows in this image or 0
   */
  public int getRows() {
    return rows;
  }

  /**
   * @return height of eventually defined rows in this image or 0
   */
  public int getRowH() {
    return rowH;
  }

  /**
   * @return number of eventually defined columns in this image or 0
   */
  public int getCols() {
    return cols;
  }

  /**
   * @return width of eventually defined columns in this image or 0
   */
  public int getColW() {
    return colW;
  }

  /**
   * store info: this image is divided into a raster of even cells <br>
   * a preparation for using getCell()
   *
   * @param r number of rows
   * @param c number of columns
   * @return the top left cell
   */
  public Image setRaster(int r, int c) {
    rows = r;
    cols = c;
    if (r > 0) {
      rowH = (int) (getSize().height / r);
      rowHd = getSize().height - r * rowH;
    }
    if (c > 0) {
      colW = (int) (getSize().width / c);
      colWd = getSize().width - c * colW;
    }
    return getCell(0, 0);
  }

  /**
   * get the specified row counting from 0, if rows or raster are setup <br>negative
   * counts reverse from the end (last = -1) <br>values outside range are 0 or last
   * respectively
   *
   * @param r row number
   * @return the row as new image or the image itself, if no rows are setup
   */
  public Image getRow(int r) {
    if (rows == 0) {
      return this;
    }
    if (r < 0) {
      r = rows + r;
    }
    r = Math.max(0, r);
    r = Math.min(r, rows - 1);
    return getSub(0, r * rowH, getSize().width, rowH);
  }

  /**
   * get the specified column counting from 0, if columns or raster are setup<br>
   * negative counts reverse from the end (last = -1) <br>values outside range are 0
   * or last respectively
   *
   * @param c column number
   * @return the column as new image or the image itself, if no columns are
   * setup
   */
  public Image getCol(int c) {
    if (cols == 0) {
      return this;
    }
    if (c < 0) {
      c = cols + c;
    }
    c = Math.max(0, c);
    c = Math.min(c, cols - 1);
    return getSub(c * colW, 0, colW, getSize().height);
  }

  /**
   * get the specified cell counting from (0, 0), if a raster is setup <br>
   * negative counts reverse from the end (last = -1) <br>values outside range are 0
   * or last respectively
   *
   * @param r row number
   * @param c column number
   * @return the cell as new image or the image itself, if no raster is setup
   */
  public Image getCell(int r, int c) {
    if (rows == 0) {
      return getCol(c);
    }
    if (cols == 0) {
      return getRow(r);
    }
    if (rows == 0 && cols == 0) {
      return this;
    }
    if (r < 0) {
      r = rows - r;
    }
    if (c < 0) {
      c = cols - c;
    }
    r = Math.max(0, r);
    r = Math.min(r, rows - 1);
    c = Math.max(0, c);
    c = Math.min(c, cols - 1);
    return getSub(c * colW, r * rowH, colW, rowH);
  }
  //</editor-fold>

  //<editor-fold desc="20 text/OCR from imagefile">

  /**
   * convenience method: get text from given image file
   *
   * @param imgFile image filename
   * @return the text or null
   */
  public static String text(String imgFile) {
    return OCR.readText(imgFile);
  }

  /**
   * convenience method: get text from given image file
   * supposing it is one line of text
   *
   * @param imgFile image filename
   * @return the text or empty string
   */
  public static String textLine(String imgFile) {
    return OCR.readLine(imgFile);
  }

  /**
   * convenience method: get text from given image file
   * supposing it is one word
   *
   * @param imgFile image filename
   * @return the text or empty string
   */
  public static String textWord(String imgFile) {
    return OCR.readWord(imgFile);
  }

  /**
   * convenience method: get text from given image file
   * supposing it is one character
   *
   * @param imgFile image filename
   * @return the text or empty string
   */
  public static String textChar(String imgFile) {
    return OCR.readChar(imgFile);
  }
  //</editor-fold>
}
