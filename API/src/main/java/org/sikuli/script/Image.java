/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.Mat;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;

/**
 * This class hides the complexity behind image names given as string.
 * <br>It's companion is {@link ImagePath} that maintains a list of places, where image files are
 * loaded from.<br>
 * An Image object:<br>
 * - has a name, either given or taken from the basename<br>
 * - keeps it's pixel content in a configurable cache avoiding reload from source<br>
 * - remembers, where it was found when searched the last time<br>
 * - can be sourced from the filesystem, from jars, from the web and from other in memory images <br>
 * - has features for basic image manipulation and presentation<br>
 * - contains the stuff to communicate with the underlying OpenCV based search engine <br>
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
  public <SUFEBMP> Image(SUFEBMP what, String name) {
    sourceClass = what.getClass().getSimpleName();
    onScreen(false);
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
    onScreen(false);
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
    element.asText(isText());
    ((Image) element).setIsBundled(imageIsBundled);
    ((Image) element).setLastSeen(getLastSeen(), getLastSeenScore());
  }

  private void init(Pattern pattern) {

  }

  private void init(String filename) {
    if (null == filename || filename.isEmpty()) {
      initTerminate("%s", "filename null or empty");
    }
    filename = getValidImageFilename(filename);
    try {
      URI uri = new URI(filename);
      if (uri.getScheme() != null && !"file".equals(uri.getScheme())) {
        init(uri);
        return;
      }
    } catch (URISyntaxException e) {
    }
    init(new File(filename));
  }

  private void init(File file) {
    String error = OK;
    if (null == file) {
      initTerminate("%s", "file is null");
    }
    URL imageURL = evalURL(file);
    if (imageURL != null) {
      error = createContent(imageURL);
    } else {
      error = String.format("init: %s file not found or path not valid", file);
    }
    if (!error.isEmpty()) {
      //TODO image missing
    }
  }

  private void init(URL url) {
    if (!createContent(url).isEmpty()) {
      //TODO image missing
    };
  }

  private void init(URI uri) {
    String error = OK;
    URL resource = null;
    try {
      resource = uri.toURL();
    } catch (MalformedURLException e) {
    }
    if (uri.getScheme().equals("class")) {
      Class clazz = null;
      try {
        clazz = Class.forName(uri.getAuthority());
      } catch (ClassNotFoundException e) {
        error = String.format("class not found: %s", uri);
      }
      resource = clazz.getResource(uri.getPath());
      if (resource == null) {
        error = String.format("not found: %s", uri);
      }
    }
    if (resource == null) {
      error = String.format("uri not valid: %s", uri);
    } else if (!createContent(resource).isEmpty()) {
      //TODO image missing
    };
  }

  private void init(Element element) {
    if (element.isOnScreen()) {
      element = element.getImage();
    }
    copyElementRectangle(element);
    copyElementContent(element);
    copyPatternAttributes(element);
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

  //<editor-fold desc="001 caching new">
  static boolean isCaching() {
    return Settings.getImageCache() > 0;
  }

  public static void resetCache() {
    ImageCache.reset();
  }

  public static Map<URL, List<Object>> getCache() {
    return ImageCache.cache;
  }

  public static String cacheStats() {
    return ImageCache.stats();
  }

  static class ImageCache {
    private static double currentSize = 0;

    static Map<URL, List<Object>> cache = Collections.synchronizedMap(new HashMap<>());

    static Mat put(URL url, Mat mat) {
      ArrayList<Object> items = new ArrayList<>();
      items.add(mat);
      items.add(0.0);
      items.add(isFile(url) ? new File(url.getPath()).lastModified() : -1);
      cache.put(url, items);
      return mat;
    }

    static Mat get(URL key) {
      List<Object> items = cache.get(key);
      if (items == null) {
        return new Mat();
      }
      Object content = items.get(0);
      if (null == content) {
        return new Mat();
      }
      Double count = (Double) items.get(1) + 1;
      if (count < Double.MAX_VALUE)
      items.set(1, count);
      return (Mat) content;
    }

    static void reset() {
      cache = Collections.synchronizedMap(new HashMap<>());
    }

    public static boolean isValid(URL url) {
      List<Object> items = cache.get(url);
      if (items == null) {
        return false;
      }
      return null != items.get(0) && !((Mat) items.get(0)).empty();
    }

    public static String stats() {
      int count = cache.size();
      double size = 0;
      double used = 0;
      for ( List<Object> items : cache.values()) {
        size += ((Mat) items.get(0)).width() * ((Mat) items.get(0)).height();
        used += (Double) items.get(1);
      }
      return String.format("ImageCache: urls(%d) size(%.0f KB) used(%.0f times)", count, size / 1000, used);
    }
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

  //<editor-fold desc="004 Fields Pattern aspects">
  private <PE> void copyPatternAttributes(PE source) {
    if (source instanceof Pattern) {
      similarity = ((Pattern) source).getSimilar();
      offset(((Pattern) source).getTargetOffset());
      waitAfter = ((Pattern) source).waitAfter();
      maskImage = ((Pattern) source).getMask();
    } else if (source instanceof Image) {
      similarity = ((Image) source).similarity();
      offset(((Image) source).offset());
      waitAfter = ((Image) source).waitAfter();
      maskImage = ((Image) source).getMask();
    }
  }

  Image maskImage = null;

  public <SUFEBMP> Image mask(SUFEBMP what) {
    maskImage = new Image(what, Element.asMaskImage());
    return this;
  }

  boolean hasMask() {
    return null != maskImage;
  }

  Image getMask() {
    return maskImage;
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
   */
  public Image similarity(double similarity) {
    this.similarity = similarity;
    return this;
  }

  private int offsetX = 0;
  private int offsetY = 0;

  /**
   * Get the value of offset
   *
   * @return the value of offset
   */
  public Location offset() {
    return new Location(offsetX, offsetY);
  }

  /**
   * Set the value of offset
   *
   * @param offset new value of offset
   */
  public Image offset(Location offset) {
    this.offsetX = offset.x;
    this.offsetY = offset.y;
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
   */
  public Image waitAfter(int waitAfter) {
    this.waitAfter = waitAfter;
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="005 bufferedImage --- to be checked">

  //TODO bufferedImage --- to be checked

  public static BufferedImage createSubimage(BufferedImage bimg, Rectangle rect) {
    Rectangle crop;
    crop = new Rectangle(0, 0, bimg.getWidth(), bimg.getHeight()).intersection(rect);
    BufferedImage newBimg = new BufferedImage(crop.width, crop.height, bimg.getType());
    Graphics2D g2d = newBimg.createGraphics();
    BufferedImage subimage = bimg.getSubimage(rect.x, rect.y, (int) rect.getWidth(), (int) rect.getHeight());
    g2d.drawImage(subimage, 0, 0, null);
    g2d.dispose();
    return newBimg;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="710 isText --- to be checked">
  public String getNameAsText() {
    return imageNameGiven;
  }

  private String imageNameGiven = null;
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="720 isBundled --- to be checked">
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

  //<editor-fold defaultstate="collapsed" desc="810 URL --- to be checked">

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

  //<editor-fold desc="830 load/save --- to be checked">
  public static void setIDEshouldReload(Element img) { //TODO
    ideShouldReload = true;
    ((Image)img).wasRecaptured = true;
    ((Image)img).lastSeen = null;
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
}
