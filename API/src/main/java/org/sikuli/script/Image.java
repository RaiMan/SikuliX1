/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.gui.SXDialog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.*;

/**
 * This class hides the complexity behind image names given as string.
 * <br>Image does not have public nor public constructors: use create()
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

  static {
    Commons.loadOpenCV();
  }

  private static String logName = "Image: ";

  private long whenCaptured = -1;

  //<editor-fold desc="00 0  instance">
  public static Image getDefaultInstance4py() {
    return new Image(new Screen().capture());
  }

  public Location getTopLeft() {
    return Optional.ofNullable(topLeft).map(Location::new).orElseGet(() -> new Location(0, 0));
  }

  public void setTopLeft(int x, int y) {
    topLeft = new Point(x, y);
  }

  private Point topLeft = null;

  private Image() {
  }

  public static <SFIRBS> Image from(SFIRBS whatEver) {
    if (whatEver instanceof String) {
      return Image.create((String) whatEver);
    } else if (whatEver instanceof File) {
      return Image.create((File) whatEver);
    } else if (whatEver instanceof URL) {
      return Image.create((URL) whatEver);
    } else if (whatEver instanceof Match) {
      Region theRegion = new Region((Match) whatEver);
      return theRegion.getImage();
    } else if (whatEver instanceof Region) {
      return ((Region) whatEver).getImage();
    } else if (whatEver instanceof Image) {
      return Image.create((Image) whatEver);
    } else if (whatEver instanceof ScreenImage) {
      return new Image((ScreenImage) whatEver);
    } else if (whatEver instanceof BufferedImage) {
      return new Image((BufferedImage) whatEver);
    }
    throw new IllegalArgumentException(String.format("Illegal Image source: %s", whatEver != null ? whatEver.getClass() : "null"));
  }


  private Image(String fname, URL fURL) {
    init(fname, fURL);
  }

  private Image(URL fURL) {
    if (fURL != null) {
      if ("file".equals(fURL.getProtocol())) {
        init(fURL.getPath(), fURL);
      } else {
        init(getNameFromURL(fURL), fURL);
      }
    } else {
      setName("");
    }
  }

  private void init(String fileName, URL fURL) {
    setName(fileName);
    if (getName().isEmpty() || fURL == null) {
      return;
    }
    fileURL = fURL;
    if (ImagePath.isImageBundled(fURL)) {
      imageIsBundled = true;
      setName(new File(getName()).getName());
    }
    load();
  }

  public static void reinit(Image img) {
    URL fURL = null;
    File imgFile = new File(img.getName());
    if (imgFile.isAbsolute()) {
      if (imgFile.exists()) {
        fURL = Commons.makeURL(imgFile); // reinit
      }
    } else {
      fURL = imageNames.get(img.getName());
      if (fURL == null) {
        fURL = ImagePath.find(img.getName());
      }
    }
    if (fURL != null) {
      img.init(img.getName(), fURL);
    }
  }

  private Image copy() {
    Image imgTarget = new Image();
    imgTarget.setName(getName());
    imgTarget.setFileURL(fileURL);
    imgTarget.setBimg(bimg);
    imgTarget.setIsAbsolute(imageIsAbsolute);
    imgTarget.setIsText(imageIsText);
    imgTarget.setIsBundled(imageIsBundled);
    imgTarget.setLastSeen(getLastSeen(), getLastSeenScore()); // copy
    imgTarget.setHasIOException(hasIOException());
    if (isPattern()) {
      imgTarget.setSimilarity(similarity);
      imgTarget.setOffset(offset);
      imgTarget.setWaitAfter(waitAfter);
      imgTarget.setIsPattern(true);
    }
    return imgTarget;
  }

  /**
   * create a new image from a buffered image<br>
   * can only be reused with the object reference
   *
   * @param img BufferedImage
   */
  public Image(BufferedImage img) {
    this(img, null);
  }

  /**
   * create a new image from a buffered image<br>
   * giving it a descriptive name for printout and logging <br>
   * can only be reused with the object reference
   *
   * @param img  BufferedImage
   * @param name descriptive name
   */
  public Image(BufferedImage img, String name) {
    setName(isBImg);
    if (name != null) {
      setName(getName() + name);
    }
    bimg = img;
    w = bimg.getWidth();
    h = bimg.getHeight();
    log(logLevel + 1, "BufferedImage: (%d, %d)%s", w, h,
        (name == null ? "" : " with name: " + name));
  }

  /**
   * create a new image from a Sikuli ScreenImage (captured)<br>
   * can only be reused with the object reference
   *
   * @param img ScreenImage
   */
  public Image(ScreenImage img) {
    this(img.getImage(), null);
    this.setTopLeft(img.x, img.y);
  }

  /**
   * create a new image from a Sikuli ScreenImage (captured)<br>
   * giving it a descriptive name for printout and logging <br>
   * can only be reused with the object reference
   *
   * @param img  ScreenImage
   * @param name descriptive name
   */
  public Image(ScreenImage img, String name) {
    this(img.getImage(), name);
    this.setTopLeft(img.x, img.y);
  }

  /**
   * check whether image is available for Finder.find()<br>
   * This is for backward compatibility<br>
   * The new ImageFinder uses isUsable()
   *
   * @return true if lodable from file or is an in memory image
   */
  public boolean isValid() {
    return fileURL != null || getName().contains(isBImg);
  }

  /**
   * checks, wether the Image can be used with the new ImageFinder
   *
   * @return true/false
   */
  public boolean isUseable() {
    return isValid() || imageIsPattern;
  }

  @Override
  public String toString() {
    if (isText()) {
      return String.format("I[isText: %s]", getNameGiven());
    }
    return String.format("I[" +
        (getName() != null ? getName() : "__UNKNOWN__") + "(%dx%d)]", w, h)
        + (lastSeen == null ? ""
        : String.format(" at(%d,%d)%%%.2f", lastSeen.x, lastSeen.y, lastScore * 100.0))
        + (topLeft == null ? ""
        : String.format(" from(%d,%d)", topLeft.x, topLeft.y, lastScore * 100.0));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="00 0 imageName">

  /**
   * @return the image's absolute filename or null if jar, http or in memory
   * image
   */
  public String getFilename() {
    if (fileURL != null && "file".equals(fileURL.getProtocol())) {
      return Commons.urlToFile(fileURL).getAbsolutePath();
    } else {
      return getName();
    }
  }

  private boolean bHasIOException = false;

  public boolean hasIOException() {
    return bHasIOException;
  }

  public void setHasIOException(boolean state) {
    bHasIOException = state;
  }

  //  /**
//   * Get the image's descriptive name
//   *
//   * @return the name
//   */
//  public String getName() {
//    if (isText()) {
//      return imageNameGiven;
//    }
//    return getName();
//  }
//
//  public Image setName(String imageName) {
//    this.imageName = imageName;
//    return this;
//  }
//
//  private String imageName = null;
  private String imageNameGiven = null;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="00 1 URL">

  /**
   * @return the evaluated url for this image (might be null)
   */
  public URL getURL() {
    return fileURL;
  }

  public Image setFileURL(URL fileURL) {
    this.fileURL = fileURL;
    return this;
  }

  private URL fileURL = null;

  public boolean inMemory() {
    return fileURL == null && bimg != null;
  }

  private static Image get(URL imgURL) {
    return imageFiles.get(imgURL);
  }

  private static String getNameFromURL(URL fURL) {
    //TODO add handling for http
    if ("jar".equals(fURL.getProtocol())) {
      int n = fURL.getPath().lastIndexOf(".jar!/");
      int k = fURL.getPath().substring(0, n).lastIndexOf("/");
      if (n > -1) {
        return "JAR:" + fURL.getPath().substring(k + 1, n) + fURL.getPath().substring(n + 5);
      }
    }
    return "???:" + fURL.getPath();
  }

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

  //<editor-fold defaultstate="collapsed" desc="00 2 bufferedImage">
  public Image setBimg(BufferedImage bimg) {
    this.bimg = bimg;
    if (bimg != null) {
      w = bimg.getWidth();
      h = bimg.getHeight();
      bsize = bimg.getData().getDataBuffer().getSize();
    } else {
      bsize = 0;
      w = -1;
      h = -1;
    }
    return this;
  }

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
   * return the image's BufferedImage (load it if not in cache)
   *
   * @return BufferedImage (might be null)
   */
  public BufferedImage get() {
    if (bimg != null) {
      if (fileURL == null) {
        log(logLevel + 1, "getImage inMemory: %s", getName());
      } else {
        log(logLevel + 1, "getImage from cache: %s", getName());
      }
      return bimg;
    } else {
      return load();
    }
  }

  /**
   * @return size of image
   */
  public Dimension getSize() {
    return new Dimension(w, h);
  }

  private int getKB() {
    if (bimg == null) {
      return 0;
    }
    return (int) bsize / KB;
  }

  /**
   * resize the loaded image with factor using OpenCV ImgProc.resize()
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param factor resize factor
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public BufferedImage resize(float factor) {
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
    return resize(get(), factor, interpolation);
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

  private boolean textSearch = false;

  public boolean isTextSearch() {
    return textSearch;
  }

  public String getNameGiven() {
    return imageNameGiven;
  }
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

  //<editor-fold defaultstate="collapsed" desc="00 6 isPattern">
  private boolean imageIsPattern = false;

  /**
   * true if this image contains pattern aspects<br>
   * only useable with the new ImageFinder
   *
   * @return true if yes, false otherwise
   */
  public boolean isPattern() {
    return imageIsPattern;
  }

  public Image setIsPattern(boolean imageIsPattern) {
    this.imageIsPattern = imageIsPattern;
    return this;
  }

  private Location offset = new Location(0, 0);

  /**
   * Get the value of offset
   *
   * @return the value of offset
   */
  public Location getOffset() {
    return offset;
  }

  /**
   * Set the value of offset
   *
   * @param offset new value of offset
   * @return the image
   */
  public Image setOffset(Location offset) {
    this.offset = offset;
    return this;
  }

  private double similarity = Settings.MinSimilarity;

  /**
   * Get the value of similarity
   *
   * @return the value of similarity
   */
  public double getSimilarity() {
    return similarity;
  }

  /**
   * Set the value of similarity
   *
   * @param similarity new value of similarity
   * @return the image
   */
  public Image setSimilarity(double similarity) {
    this.similarity = similarity;
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="01 create">
  /**
   * create a new Image as copy of the given Image
   *
   * @param imgSrc given Image
   * @return new Image
   */
  private static Image create(Image imgSrc) {
    return imgSrc.copy();
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
  private static Image create(String fName) {
    Image img = get(fName);
    return createImageValidate(img); // create(String fName)
  }

  public static Image createSilent(String fName) {
    silent = true;
    Image image = create(fName);
    silent = false;
    return image;
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
  private static Image create(File imageFile) {
    Image img = get(imageFile.getAbsolutePath());
    return createImageValidate(img); // create(File imageFile)
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
  private static Image create(URL url) {
    Image img = null;
    if (url != null) {
      img = get(url);
    }
    if (img == null) {
      img = new Image(url);
    }
    return createImageValidate(img); // create(URL url)
  }

  /**
   * FOR INTERNAL USE: from IDE - suppresses load error message
   *
   * @param fName image filename
   * @return this
   */
  public static Image createThumbNail(String fName) {
    Image img = get(fName);
    return createImageValidate(img); // createThumbNail(String fName)
  }

  private static Image createImageValidate(Image img) {
    if (img == null) {
      return new Image("", null);
    }
    if (!img.isValid()) {
      if (Commons.isValidImageFilename(img.getNameGiven())) {
        img.setIsText(false);
      } else {
        img.setIsText(true);
      }
    }
    return img;
  }
//</editor-fold>

//<editor-fold desc="02 getSub">
  public Image getSub(Rectangle r) {
    return getSub(r.x, r.y, r.width, r.height);
  }

  /**
   * create a sub image from this image
   *
   * @param part (the constants Region.XXX as used with {@link Region#get(int)})
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
    BufferedImage bi = new BufferedImage(w, h, get().getType());
    Graphics2D g = bi.createGraphics();
    g.drawImage(get().getSubimage(x, y, w, h), 0, 0, null);
    g.dispose();
    return new Image(bi);
  }
  //</editor-fold>

  //<editor-fold desc="04 caching">
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
    if (isCaching()) {
      purge(path.getURL());
    }
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
    img.setBimg(null);
    images.remove(img);
  }

  //TODO make obsolete
  public static void unCache(String fileName) {
    unCache(Commons.makeURL(new File(fileName).getAbsolutePath()));
  }

  /**
   * Print the current state of the cache
   */
  public static void dump() {
    dump(0);
  }

  /**
   * Print the current state of the cache, verbosity depends on debug level
   *
   * @param lvl debug level used here
   */
  public static void dump(int lvl) {
    if (!isCaching()) {
      return;
    }
    log(logLevel, "--- start of Image dump ---");
    ImagePath.dump(lvl);
    log(logLevel, "ImageFiles entries: %d", imageFiles.size());
    Iterator<Map.Entry<URL, Image>> it = imageFiles.entrySet().iterator();
    Map.Entry<URL, Image> entry;
    while (it.hasNext()) {
      entry = it.next();
      log(logLevel, entry.getKey().toString());
    }
    log(logLevel, "ImageNames entries: %d", imageNames.size());
    Iterator<Map.Entry<String, URL>> nit = imageNames.entrySet().iterator();
    Map.Entry<String, URL> name;
    while (nit.hasNext()) {
      name = nit.next();
      log(logLevel, "%s %d KB (%s)", new File(name.getKey()).getName(),
          imageFiles.get(name.getValue()).getKB(), name.getValue());
    }
    if (Settings.getImageCache() == 0) {
      log(logLevel, "Cache state: switched off!");
    } else {
      log(logLevel, "Cache state: Max %d MB (entries: %d  used: %d %% %d KB)",
          Settings.getImageCache(), images.size(),
          (int) (100 * currentMemory / (Settings.getImageCache() * MB)), (int) (currentMemory / KB));
    }
    log(logLevel, "--- end of Image dump ---");
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
      File fOrg = Commons.urlToFile(fileURL);
      File fBack = new File(fOrg.getParentFile(), "_BACKUP_" + fOrg.getName());
      if (FileManager.xcopy(fOrg, fBack)) {
        hasBackup = fBack.getPath();
        log(logLevel + 1, "backup: %s created", fBack.getName());
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
        log(logLevel + 1, "restore: %s restored", fOrg.getName());
        FileManager.deleteFileOrFolder(fBack);
        hasBackup = "";
        return true;
      }
      log(-1, "restore: %s did not work", fBack.getName());
    }
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="06 load/save">

  /**
   * FOR INTERNAL USE: tries to get the image from the cache, if not cached yet:
   * create and load a new image
   *
   * @param fName image filename
   * @return this
   */
  private static Image get(String fName) {
    if (fName == null || fName.isEmpty()) {
      return null;
    }
    Image image = null;
    if (fName.startsWith("\t") && fName.endsWith("\t")) {
      fName = fName.substring(1, fName.length() - 1);
      image = new Image();
      image.setIsText(true);
      image.textSearch = true;
      image.imageNameGiven = fName;
    } else {
      URL imageURL;
      String imageFileName = Commons.getValidImageFilename(fName);
      File imageFile = new File(imageFileName);
      if (imageFile.isAbsolute()) {
        if (imageFile.exists()) {
          imageURL = Commons.makeURL(imageFile); // get
        } else {
          return null;
        }
      } else {
        imageURL = imageNames.get(imageFileName);
        if (imageURL == null) {
          imageURL = ImagePath.find(imageFileName);
        }
      }
      if (imageURL != null && Image.isCaching()) {
        image = imageFiles.get(imageURL);
        if (image != null && null == imageNames.get(image.getName())) {
          imageNames.put(image.getName(), imageURL);
        }
      }
      if (image == null) {
        image = new Image(imageFileName, imageURL);
        image.setIsAbsolute(imageFile.isAbsolute());
      } else {
        if (image.bimg != null) {
          log(logLevel + 1, "reused: %s (%s)", image.getName(), image.fileURL);
        } else {
          if (Settings.getImageCache() > 0) {
            image.load();
          }
        }
      }
      image.imageNameGiven = fName;
    }
    return image;
  }

  private BufferedImage load() {
    BufferedImage bImage = null;
    if (fileURL != null) {
      bimg = null;
      try {
        bImage = ImageIO.read(fileURL);
      } catch (Exception e) {
        log(-1, "load: failed: %s", fileURL);
        bHasIOException = true;
        fileURL = null;
        return null;
      }
      if (getName() != null) {
        if (isCaching()) {
          imageFiles.put(fileURL, this);
          imageNames.put(getName(), fileURL);
        }
        w = bImage.getWidth();
        h = bImage.getHeight();
        bsize = bImage.getData().getDataBuffer().getSize();
        log(logLevel + 1, "loaded: %s (%s)", getName(), fileURL);
        if (isCaching()) {
          int maxMemory = Settings.getImageCache() * MB;
          currentMemoryUp(bsize);
          bimg = bImage;
          images.add(this);
          log(logLevel + 1, "cached: %s (%d KB) (# %d KB %d -- %d %% of %d MB)",
              getName(), getKB(),
              images.size(), (int) (currentMemory / KB),
              (int) (100 * currentMemory / maxMemory), (int) (maxMemory / MB));
        }
      } else {
        log(-1, "invalid! not loaded! %s", fileURL);
      }
    }
    return bImage;
  }

  private BufferedImage loadAgain() {
    BufferedImage bImage = null;
    if (fileURL != null) {
      bimg = null;
      try {
        bImage = ImageIO.read(fileURL);
      } catch (Exception e) {
        log(-1, "loadAgain: failed: %s", fileURL);
        bHasIOException = true;
        if (Image.isCaching()) {
          imageFiles.remove(fileURL);
        }
        return null;
      }
      if (isCaching()) {
        imageFiles.put(fileURL, this);
        imageNames.put(getName(), fileURL);
      }
      w = bImage.getWidth();
      h = bImage.getHeight();
      bsize = bImage.getData().getDataBuffer().getSize();
      log(logLevel + 1, "loaded again: %s (%s)", getName(), fileURL);
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
        image.setLastSeen(null, 0); // reload
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

  public String save(String name) {
    return save(name, ImagePath.getBundlePath());
  }

  public String save(String name, String path) {
    return save(new File(path, name));
  }

  public String save(File file) {
    String fileName = FilenameUtils.removeExtension(file.getAbsolutePath()) + ".png";
    File fImg = new File(fileName);
    try {
      ImageIO.write(get(), "png", fImg);
      Debug.log(3, "Image::save: %s", fImg);
    } catch (IOException e) {
      Debug.error("Image::save: %s did not work (%s)", fImg, e.getMessage());
    }
    fileURL = Commons.makeURL(fImg); // save
    return fileName;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="00 8 waitAfter">
  private int waitAfter;

  /**
   * Get the value of waitAfter
   *
   * @return the value of waitAfter
   */
  public int getWaitAfter() {
    return waitAfter;
  }

  /**
   * Set the value of waitAfter
   *
   * @param waitAfter new value of waitAfter
   * @return the image
   */
  public Image setWaitAfter(int waitAfter) {
    this.waitAfter = waitAfter;
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="00 7 lastSeen">
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

  public Image setLastSeen(Match match) {
    if (match == null) {
      lastSeen = null;
      lastScore = 0.0;
      return this;
    }
    lastSeen = match.getRect();
    lastScore = match.getScore();
    return this;
  }
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

  //<editor-fold desc="80 image handling resize Mat">
  public final static String PNG = "png";
  public final static String dotPNG = "." + PNG;

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

    private int value;

    Interpolation(int value) {
      this.value = value;
    }

  }
  public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
    final AffineTransform af = new AffineTransform();
    af.scale((double) width / originalImage.getWidth(),
        (double) height / originalImage.getHeight());
    final AffineTransformOp operation = new AffineTransformOp(
        af, AffineTransformOp.TYPE_BILINEAR);
    BufferedImage rescaledImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);
    rescaledImage = operation.filter(originalImage, rescaledImage);
    return rescaledImage;
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

  /**
   * resize the given image with factor using OpenCV ImgProc.resize()
   *
   * @param bimg          given image
   * @param factor        resize factor
   * @param interpolation algorithm used for pixel interpolation
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public static BufferedImage resize(BufferedImage bimg, float factor, Interpolation interpolation) {
    return getBufferedImage(cvResize(bimg, factor, interpolation));
  }

  /**
   * resize the given image (as cvMat in place) with factor using OpenCV ImgProc.resize()<br>
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param mat    given image as cvMat
   * @param factor resize factor
   */
  public static void resize(Mat mat, float factor) {
    resize(mat, factor, Interpolation.CUBIC);
  }

  /**
   * resize the given image (as cvMat in place) with factor using OpenCV ImgProc.resize()<br>
   *
   * @param mat           given image as cvMat
   * @param factor        resize factor
   * @param interpolation algorithm used for pixel interpolation.
   */
  public static void resize(Mat mat, float factor, Interpolation interpolation) {
    cvResize(mat, factor, interpolation);
  }

  private static Mat cvResize(BufferedImage bimg, double rFactor, Interpolation interpolation) {
    Mat mat = makeMat(bimg);
    cvResize(mat, rFactor, interpolation);
    return mat;
  }

  private static void cvResize(Mat mat, double rFactor, Interpolation interpolation) {
    int newW = (int) (rFactor * mat.width());
    int newH = (int) (rFactor * mat.height());
    Imgproc.resize(mat, mat, new Size(newW, newH), 0, 0, interpolation.value);
  }

  public static Mat getNewMat() {
    return new Mat();
  }

  public static Mat getNewMat(Size size, int type, int fill) {
    switch (type) {
      case 1:
        type = CvType.CV_8UC1;
        break;
      case 3:
        type = CvType.CV_8UC3;
        break;
      case 4:
        type = CvType.CV_8UC4;
        break;
      default:
        type = -1;
    }
    if (type < 0) {
      return new Mat();
    }
    Mat result;
    if (fill < 0) {
      result = new Mat(size, type);
    } else {
      result = new Mat(size, type, new Scalar(fill));
    }
    return result;
  }

  public static List<Mat> getMatList(BufferedImage bImg) {
    byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
    Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
    aMat.put(0, 0, data);
    List<Mat> mats = new ArrayList<Mat>();
    Core.split(aMat, mats);
    return mats;
  }

  public static Mat makeMat(BufferedImage bImg) {
    return makeMat(bImg, true);
  }

  public static Mat makeMat(BufferedImage bImg, boolean asBGR) {
    if (bImg.getType() == BufferedImage.TYPE_INT_RGB) {
      int[] data = ((DataBufferInt) bImg.getRaster().getDataBuffer()).getData();
      ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      intBuffer.put(data);
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
      aMat.put(0, 0, byteBuffer.array());
      Mat oMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      Mat oMatA = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      List<Mat> mixIn = new ArrayList<Mat>(Arrays.asList(new Mat[]{aMat}));
      List<Mat> mixOut = new ArrayList<Mat>(Arrays.asList(new Mat[]{oMatA, oMatBGR}));
      //A 0 - R 1 - G 2 - B 3 -> A 0 - B 1 - G 2 - R 3
      Core.mixChannels(mixIn, mixOut, new MatOfInt(0, 0, 1, 3, 2, 2, 3, 1));
      return oMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_3BYTE_BGR) {
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED
        || bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      String bImgType = "BYTE_BINARY";
      if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
        bImgType = "BYTE_INDEXED";
      }
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg, 0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) { //TODO || bImg.getType() == BufferedImage.TYPE_CUSTOM) {
      List<Mat> mats = getMatList(bImg);
      Size size = mats.get(0).size();
      if (!asBGR) {
        Mat mBGRA = getNewMat(size, 4, -1);
        mats.add(mats.remove(0));
        Core.merge(mats, mBGRA);
        return mBGRA;
      } else {
        Mat mBGR = getNewMat(size, 3, -1);
        mats.remove(0);
        Core.merge(mats, mBGR);
        return mBGR;
      }
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      aMat.put(0, 0, data);
      return aMat;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg, 0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else {
      Debug.error("makeMat: BufferedImage: type not supported: %d --- please report this problem", bImg.getType());
    }
    return getNewMat();
  }

  public static BufferedImage getBufferedImage(Mat mat) {
    return getBufferedImage(mat, dotPNG);
  }

  public static BufferedImage getBufferedImage(Mat mat, String type) {
    BufferedImage bImg = null;
    MatOfByte bytemat = new MatOfByte();
    if (SX.isNull(mat)) {
      mat = getNewMat();
    }
    Imgcodecs.imencode(type, mat, bytemat);
    byte[] bytes = bytemat.toArray();
    InputStream in = new ByteArrayInputStream(bytes);
    try {
      bImg = ImageIO.read(in);
    } catch (IOException ex) {
      Debug.error("getBufferedImage: %s error(%s)", mat, ex.getMessage());
    }
    return bImg;
  }

  //</editor-fold>

  public void show() {
    String url = "";
    if (inMemory()) {
      ;
    } else {
      url = fileURL.toString();
    }
    String text =
        "#globals; \n" +
            "#image; " + url + "; \n" +
//            "---\n" +
//            "#close; Press ESC or Click;" +
            "";
    final SXDialog show = new SXDialog(text);
    show.run();
    while (show.isRunning()) {
      RunTime.pause(1);
    }
  }
}
