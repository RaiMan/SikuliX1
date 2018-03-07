/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.natives.Vision;

/**
 * This class hides the complexity behind image names given as string.
 * <br>Image does not have public nor protected constructors: use create()
 * <br>It's companion is {@link ImagePath} that maintains a list of places, where image files are
 * loaded from.<br>
 * Another companion {@link ImageGroup} will allow to look at images in a folder as a
 * group.<br>
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
 *
 * This class maintains<br>
 * - a list of all images ever loaded in this session with their source
 * reference and a ref to the image object<br>
 * - a list of all images currently having their content in memory (buffered
 * image) (managed as a configurable cache)<br>
 * The caching can be configured using {@link Settings#setImageCache(int)}
 */
public class Image {

  static RunTime runTime = RunTime.get();

  private static String me = "Image: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
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

  private static boolean ideShouldReload = false;
  protected boolean wasRecaptured = false;
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

  public boolean isRecaptured() {
    boolean state = wasRecaptured;
    wasRecaptured = false;
    return state;
  }

//<editor-fold defaultstate="collapsed" desc="imageName">
  private String imageName = null;
  private String imageNameGiven = null;
  private boolean bHasIOException = false;

  public boolean hasIOException() {
    return bHasIOException;
  }

  public void setHasIOException(boolean state) {
    bHasIOException = state;
  }

  public String getImageName() {
    return imageName;
  }

  public Image setImageName(String imageName) {
    this.imageName = imageName;
    return this;
  }

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="fileURL">
  private URL fileURL = null;
  private String imageAsFile = null;

  public URL getFileURL() {
    return fileURL;
  }

  public Image setFileURL(URL fileURL) {
    this.fileURL = fileURL;
    return this;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="bimg">
  private BufferedImage bimg = null;

  protected Image setBimg(BufferedImage bimg) {
    this.bimg = bimg;
    if (bimg != null) {
      bwidth = bimg.getWidth();
      bheight = bimg.getHeight();
      bsize = bimg.getData().getDataBuffer().getSize();
    } else {
      bsize = 0;
      bwidth = -1;
      bheight = -1;
    }
    return this;
  }

  private int bsize = 0;
  private int bwidth = -1;
  private int bheight = -1;
//</editor-fold>

  private ImageGroup group = null;

//<editor-fold defaultstate="collapsed" desc="isText">
  private boolean imageIsText = false;

  /**
   *
   * @return true if the given image name did not give a valid image so it might
   * be text to search
   */
  public boolean isText() {
    return imageIsText;
  }

  private Image setIsText(boolean val) {
    imageIsText = val;
    return this;
  }

  public String getText() {
    return imageNameGiven;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="isAbsolute">
  private boolean imageIsAbsolute = false;
  /**
   *
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

//<editor-fold defaultstate="collapsed" desc="isBundled">
  private boolean imageIsBundled = false;

  private Image setIsBundled(boolean imageIsBundled) {
    this.imageIsBundled = imageIsBundled;
    return this;
  }

  /**
   * INTERNAL USE: image is contained in a bundle (.sikuli)
   * @return true/false
   */
  public boolean isBundled() {
    return imageIsBundled;
  }

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="isPattern">
  private boolean imageIsPattern = false;

  /**
   * true if this image contains pattern aspects<br>
   * only useable with the new ImageFinder
   * @return true if yes, false otherwise
   */
  public boolean isPattern() {
    return imageIsPattern;
  }

  public Image setIsPattern(boolean imageIsPattern) {
    this.imageIsPattern = imageIsPattern;
    return this;
  }

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="waitAfter">
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

//<editor-fold defaultstate="collapsed" desc="offset">
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
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="similarity">
  private float similarity = (float) Settings.MinSimilarity;

  /**
   * Get the value of similarity
   *
   * @return the value of similarity
   */
  public float getSimilarity() {
    return similarity;
  }

  /**
   * Set the value of similarity
   *
   * @param similarity new value of similarity
   * @return the image
   */
  public Image setSimilarity(float similarity) {
    this.similarity = similarity;
    return this;
  }

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="lastSeen">
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
   * @param sim SimilarityScore
   * @return the image
   */
  protected Image setLastSeen(Rectangle lastSeen, double sim) {
    this.lastSeen = lastSeen;
    this.lastScore = sim;
    if (group != null) {
      group.addImageFacts(this, lastSeen, sim);
    }
    return this;
  }
//</editor-fold>

  private boolean beSilent = false;

  /**
   * to support a raster over the image
   */
  private int rows = 0;
  private int cols = 0;
  private int rowH = 0;
  private int colW = 0;
  private int rowHd = 0;
  private int colWd = 0;

  @Override
  public String toString() {
    return String.format(
            (imageName != null ? imageName : "__UNKNOWN__") + ": (%dx%d)", bwidth, bheight)
            + (lastSeen == null ? ""
            : String.format(" seen at (%d, %d) with %.2f", lastSeen.x, lastSeen.y, lastScore));
  }

  private Image() {
  }

	private Image(String fname, URL fURL) {
    init(fname, fURL, true);
  }

  private Image(String fname, URL fURL, boolean silent) {
    init(fname, fURL, silent);
  }

	private void init(String fileName, URL fURL, boolean silent) {
    imageName = fileName;
    if (imageName.isEmpty() || fURL == null) {
      return;
    }
    fileURL = fURL;
		if (ImagePath.isImageBundled(fURL)) {
			imageIsBundled = true;
			imageName = new File(imageName).getName();
		}
		beSilent = silent;
    load();
  }

  private BufferedImage load() {
    BufferedImage bImage = null;
    if (fileURL != null) {
      bimg = null;
      try {
        bImage = ImageIO.read(fileURL);
      } catch (Exception e) {
        if (!beSilent) {
          log(-1, "could not be loaded: %s", fileURL);
        }
        bHasIOException = true;
				fileURL = null;
        return null;
      }
      if (imageName != null) {
        imageFiles.put(fileURL, this);
        imageNames.put(imageName, fileURL);
        bwidth = bImage.getWidth();
        bheight = bImage.getHeight();
        bsize = bImage.getData().getDataBuffer().getSize();
        log(lvl, "loaded: %s (%s)", imageName, fileURL);
        if (isCaching()) {
          int maxMemory = Settings.getImageCache() * MB;
          currentMemoryUp(bsize);
          bimg = bImage;
          images.add(this);
          log(lvl, "cached: %s (%d KB) (# %d KB %d -- %d %% of %d MB)",
                  imageName, getKB(),
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
        if (!beSilent) {
          log(-1, "could not be loaded again: %s", fileURL);
        }
        bHasIOException = true;
				imageFiles.remove(fileURL);
        return null;
      }
      imageFiles.put(fileURL, this);
      imageNames.put(imageName, fileURL);
      bwidth = bImage.getWidth();
      bheight = bImage.getHeight();
      bsize = bImage.getData().getDataBuffer().getSize();
      log(lvl, "loaded again: %s (%s)", imageName, fileURL);
    }
    return bImage;
  }

  private Image copy() {
    Image imgTarget = new Image();
    imgTarget.setImageName(imageName);
    imgTarget.setFileURL(fileURL);
    imgTarget.setBimg(bimg);
    imgTarget.setGroup(group);
    imgTarget.setIsAbsolute(imageIsAbsolute);
    imgTarget.setIsText(imageIsText);
    imgTarget.setIsBundled(imageIsBundled);
    imgTarget.setLastSeen(getLastSeen(), getLastSeenScore());
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
   * create a new Image as copy of the given Image
   * @param imgSrc given Image
   * @return new Image
   */
  public static Image create(Image imgSrc) {
    return imgSrc.copy();
  }

  /**
   * create a new image from a filename <br>
   * file ending .png is added if missing (currently valid: png, jpg, jpeg)<br>
   * relative filename: [...path.../]name[.png] is searched on current image path<br>
   * absolute filename is taken as is
   * if image exists, it is loaded to cache <br>
   * already loaded image with same name (given path) is reused (taken from cache) <br>
   *
   * if image not found, it might be a text to be searched (imageIsText = true)
   *
   * @param fName image filename
   * @return an Image object (might not be valid - check with isValid())
   */
  public static Image create(String fName) {
    Image img = get(fName, false);
    return createImageValidate(img, true);
  }

  /**
   * create a new Image with Pattern aspects from an existing Pattern
   * @param p a Pattern
   * @return the new Image
   */
  public static Image create(Pattern p) {
    Image img = p.getImage().copy();
    img.setIsPattern(true);
    img.setSimilarity(p.getSimilar());
    img.setOffset(p.getTargetOffset());
    img.setWaitAfter(p.getTimeAfter());
		return img;
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
    Image img = get(url);
    if (img == null) {
      img = new Image(url);
    }
    return createImageValidate(img, true);
  }

  protected static <PSI> Image getImageFromTarget(PSI target) {
    if (target instanceof Pattern) {
      return ((Pattern) target).getImage();
    } else if (target instanceof String) {
      Image img = get((String) target, true);
      img = createImageValidate(img, true);
      return img;
    } else if (target instanceof Image) {
      return (Image) target;
    } else {
        runTime.abortScripting("aborting script at:",
                String.format("find, wait, exists: invalid parameter: %s", target));
    }
    return null;
  }

  /**
   * FOR INTERNAL USE: from IDE - suppresses load error message
   *
   * @param fName image filename
   * @return this
   */
  public static Image createThumbNail(String fName) {
    Image img = get(fName, true);
    return createImageValidate(img, false);
  }

  private static Image createImageValidate(Image img, boolean verbose) {
    if (img == null) {
      log(-1, "Image not valid, creating empty Image");
      return new Image("", null);
    }
    if (!img.isValid()) {
      if (Settings.OcrTextSearch) {
        img.setIsText(true);
        if (Settings.isValidImageFilename(img.getName())) {
          img.setIsText(false);
        }
      } else {
        if (verbose) {
					log(-1, "Image not valid, but TextSearch is switched off!");
				}
      }
    }
    return img;
  }

	/**
	 * stores the image as PNG file in the standard temp folder
	 * with a created filename (sikuli-image-#unique-random#.png)
	 * if not yet stored before
	 *
	 * @return absolute path to stored file
	 */
	public String asFile() {
    if (imageAsFile == null) {
      if (bimg != null) {
       imageAsFile = FileManager.saveTmpImage(bimg);
      }
    }
    return imageAsFile;
  }

  /**
   * FOR INTERNAL USE: see get(String, boolean)
   *
   * @param fName image filename
   * @return this
   */
  protected static Image get(String fName) {
    return get(fName, false);
  }

  /**
   * FOR INTERNAL USE: tries to get the image from the cache, if not cached yet:
   * create and load a new image
   *
   * @param fName image filename
   * @param silent true: suppress some error messages
   * @return this
   */
	private static Image get(String fName, boolean silent) {
    if (fName == null || fName.isEmpty()) {
      return null;
    }
    Image img = null;
    if (fName.startsWith("\t") && fName.endsWith("\t")) {
      fName = fName.substring(1, fName.length() - 1);
      img = new Image();
      img.setIsText(true);
    } else {
      fName = FileManager.slashify(fName, false);
      URL fURL = null;
      String fileName = Settings.getValidImageFilename(fName);
      if (fileName.isEmpty()) {
        log(-1, "not a valid image type: " + fName);
        fileName = fName;
      }
      File imgFile = new File(fileName);
      if (imgFile.isAbsolute()) {
        if (imgFile.exists()) {
          fURL = FileManager.makeURL(fileName);
        }
      } else {
        fURL = imageNames.get(fileName);
        if (fURL == null) {
          fURL = ImagePath.find(fileName);
        }
      }
      if (fURL != null) {
        img = imageFiles.get(fURL);
        if (img != null && null == imageNames.get(img.imageName)) {
          imageNames.put(img.imageName, fURL);
        }
      }
      if (img == null) {
        img = new Image(fileName, fURL, silent);
        img.setIsAbsolute(imgFile.isAbsolute());
      } else {
        if (img.bimg != null) {
          log(3, "reused: %s (%s)", img.imageName, img.fileURL);
        } else {
          if (Settings.getImageCache() > 0) {
            img.load();
          }
        }
      }
    }
		img.imageNameGiven = fName;
    return img;
  }

	protected static void set(Image img) {
    URL fURL = null;
    File imgFile = new File(img.getName());
    if (imgFile.isAbsolute()) {
      if (imgFile.exists()) {
        fURL = FileManager.makeURL(img.getName());
      }
    } else {
      fURL = imageNames.get(img.getName());
      if (fURL == null) {
        fURL = ImagePath.find(img.getName());
      }
    }
    if (fURL != null) {
      img.init(img.getName(), fURL, true);
    }
  }

  protected static Image get(URL imgURL) {
    return imageFiles.get(imgURL);
  }

  private Image(URL fURL) {
    if ("file".equals(fURL.getProtocol())) {
      init(fURL.getPath(), fURL, true);
    } else {
      init(getNameFromURL(fURL), fURL, true);
    }
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
   * @param img BufferedImage
   * @param name descriptive name
   */
  public Image(BufferedImage img, String name) {
    imageName = isBImg;
    if (name != null) {
      imageName += name;
    }
    bimg = img;
    bwidth = bimg.getWidth();
    bheight = bimg.getHeight();
		log(lvl, "BufferedImage: (%d, %d)%s", bwidth, bheight,
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
  }

  /**
   * create a new image from a Sikuli ScreenImage (captured)<br>
   * giving it a descriptive name for printout and logging <br>
   * can only be reused with the object reference
   *
   * @param img ScreenImage
   * @param name descriptive name
   */
  public Image(ScreenImage img, String name) {
    this(img.getImage(), name);
  }

  /**
   * INTERNAL USE: IDE: to get rid of cache entries at script save, close or
   * save as
   *
   * @param bundlePath absolute path for an image set in this folder
   */
  public static void purge(String bundlePath) {
    if (imageFiles.isEmpty() || ImagePath.getPaths().get(0) == null) {
      return;
    }
    URL pathURL = FileManager.makeURL(bundlePath);
    if (!ImagePath.getPaths().get(0).pathURL.equals(pathURL)) {
      log(-1, "purge: not current bundlepath: " + pathURL);
      return;
    }
    purge(pathURL);
  }

  protected static void purge(ImagePath.PathEntry path) {
    if (path == null) {
      return;
    }
    purge(path.pathURL);
  }

  protected static synchronized void purge(URL pathURL) {
    List<Image> imagePurgeList = new ArrayList<>();
    List<String> imageNamePurgeList = new ArrayList<>();
    URL imgURL;
    Image img;
    log(lvl, "purge: ImagePath: %s", pathURL.getPath());
    Iterator<Map.Entry<URL, Image>> it = imageFiles.entrySet().iterator();
    Map.Entry<URL, Image> entry;
    while (it.hasNext()) {
      entry = it.next();
      imgURL = entry.getKey();
      if (imgURL.toString().startsWith(pathURL.toString())) {
        log(lvl + 1, "purge: URL: %s", imgURL.toString());
        img = entry.getValue();
        imagePurgeList.add(img);
        imageNamePurgeList.add(img.imageName);
        it.remove();
      }
    }
    if (!imagePurgeList.isEmpty()) {
      Iterator<Image> bit = images.iterator();
      while (bit.hasNext()) {
        img = bit.next();
        if (imagePurgeList.contains(img)) {
          bit.remove();
          log(lvl + 1, "purge: bimg: %s", img);
          currentMemoryDown(img.bsize);
        }
      }
    }
    for (String name : imageNamePurgeList) {
      imageNames.remove(name);
    }
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

  public File remove() {
    URL furl = null;
    if (isFile()) {
      furl = getURL();
      unCacheImage(furl);
      return new File(furl.getPath());
    }
    return null;
  }

  public void delete() {
    File fImg = remove();
    if (null != fImg) FileManager.deleteFileOrFolder(fImg);
  }

  private String hasBackup = "";

  protected boolean backup() {
    if (isValid()) {
      File fOrg = new File(fileURL.getPath());
      File fBack = new File(fOrg.getParentFile(), "_BACKUP_" + fOrg.getName());
      if (FileManager.xcopy(fOrg, fBack)) {
        hasBackup = fBack.getPath();
        log(lvl, "backup: %s created", fBack.getName());
        return true;
      }
      log(-1, "backup: %s did not work", fBack.getName());
    }
    return false;
  }

  protected boolean restore() {
    if (!hasBackup.isEmpty()) {
      File fBack = new File(hasBackup);
      File fOrg = new File(hasBackup.replace("_BACKUP_", ""));
      if (FileManager.xcopy(fBack, fOrg)) {
        log(lvl, "restore: %s restored", fOrg.getName());
        FileManager.deleteFileOrFolder(fBack);
        hasBackup = "";
        return true;
      }
      log(-1, "restore: %s did not work", fBack.getName());
    }
    return false;
  }

  /**
   * purge the given image file's in memory image data and remove it from cache.
   * @param imgFileName an absolute filename
   */
  public static void unCacheBundledImage(String imgFileName) {
    URL imgURL = FileManager.makeURL(new File(imgFileName).getAbsolutePath());
    unCacheImage(imgURL);
  }

  /**
   * purge the given image's in memory image data and remove it from cache.
   * @param imgURL URL of an image file
   */
  public static void unCacheImage(URL imgURL) {
    Image img = imageFiles.get(imgURL);
    if (img == null) {
      return;
    }
    currentMemoryDown(img.bsize);
    img.setBimg(null);
    images.remove(img);
  }


  /**
   * Print the current state of the cache
   */
  public static void dump() {
    dump(0);
  }

  /**
   * Print the current state of the cache, verbosity depends on debug level
   * @param lvl debug level used here
   */
  public static void dump(int lvl) {
    log(lvl, "--- start of Image dump ---");
    ImagePath.dump(lvl);
    log(lvl, "ImageFiles entries: %d", imageFiles.size());
    Iterator<Map.Entry<URL, Image>> it = imageFiles.entrySet().iterator();
    Map.Entry<URL, Image> entry;
    while (it.hasNext()) {
      entry = it.next();
      log(lvl, entry.getKey().toString());
    }
    log(lvl, "ImageNames entries: %d", imageNames.size());
    Iterator<Map.Entry<String, URL>> nit = imageNames.entrySet().iterator();
    Map.Entry<String, URL> name;
    while (nit.hasNext()) {
      name = nit.next();
      log(lvl, "%s %d KB (%s)", new File(name.getKey()).getName(),
							imageFiles.get(name.getValue()).getKB(), name.getValue());
    }
    if (Settings.getImageCache() == 0) {
      log(lvl, "Cache state: switched off!");
    } else {
      log(lvl, "Cache state: Max %d MB (entries: %d  used: %d %% %d KB)",
              Settings.getImageCache(), images.size(),
              (int) (100 * currentMemory / (Settings.getImageCache() * MB)), (int) (currentMemory / KB));
    }
    log(lvl, "--- end of Image dump ---");
  }

  /**
   * clears all caches (should only be needed for debugging)
   */
  public static void reset() {
    clearCache(0);
    imageNames.clear();
    imageFiles.clear();
  }

  /**
   * Get the image's descriptive name
   *
   * @return the name
   */
  public String getName() {
    if (isText()) {
      return imageNameGiven;
    }
    return imageName;
  }

  /**
   *
   * @return the current ImageGroup
   */
  public ImageGroup getGroup() {
    return group;
  }

  /**
   * set the ImageGroup this image should belong to
   *
   * @param group ImageGroup
   */
  public void setGroup(ImageGroup group) {
    this.group = group;
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
	 * @return true/false
	 */
	public boolean isUseable() {
    return isValid() || imageIsPattern;
  }

  /**
   *
   * @return the evaluated url for this image (might be null)
   */
  public URL getURL() {
    return fileURL;
  }

  /**
   * @return the image's absolute filename or null if jar, http or in memory
   * image
   */
  public String getFilename() {
    if (fileURL != null && "file".equals(fileURL.getProtocol())) {
      return new File(fileURL.getPath()).getAbsolutePath();
    } else {
			return imageName;
		}
  }

  /**
   * return the image's BufferedImage (load it if not in cache)
   *
	 * @return BufferedImage (might be null)
   */
  public BufferedImage get() {
    return get(true);
  }

  protected BufferedImage get(boolean shouldLoad) {
    if (bimg != null) {
      if (fileURL == null) {
        log(lvl + 1, "getImage inMemory: %s", imageName);
      } else {
        log(lvl + 1, "getImage from cache: %s", imageName);
      }
      return bimg;
    } else {
      if (shouldLoad) {
        return load();
      } else {
        return null;
      }
    }
  }

  /**
   *
   * @return size of image
   */
  public Dimension getSize() {
    return new Dimension(bwidth, bheight);
  }

	private int getKB() {
    if (bimg == null) {
      return 0;
    }
		return (int) bsize / KB;
	}

  /**
   * resize the loaded image with factor using Graphics2D.drawImage
   * @param factor resize factor
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public BufferedImage resize(float factor) {
    int type;
    BufferedImage bufimg = get();
    type = bufimg.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : bufimg.getType();
    int width = (int) (getSize().getWidth() * factor);
    int height = (int) (getSize().getHeight() * factor);
    BufferedImage resizedImage = new BufferedImage(width, height, type);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(bufimg, 0, 0, width, height, null);
    g.dispose();
    return resizedImage;
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
    g.drawImage(get().getSubimage(x, y, w, h), 0, 0, null);
    g.dispose();
    return new Image(bi);
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
   *
   * @return number of eventually defined rows in this image or 0
   */
  public int getRows() {
    return rows;
  }

  /**
   *
   * @return height of eventually defined rows in this image or 0
   */
  public int getRowH() {
    return rowH;
  }

  /**
   *
   * @return number of eventually defined columns in this image or 0
   */
  public int getCols() {
    return cols;
  }

  /**
   *
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

  /**
   * get the OpenCV Mat version of the image's BufferedImage
   *
   * @return OpenCV Mat
   */
  public Mat getMat() {
    return createMat(get());
  }

  protected static Mat createMat(BufferedImage img) {
    if (img != null) {
      Debug timer = Debug.startTimer("Mat create\t (%d x %d) from \n%s", img.getWidth(), img.getHeight(), img);
      Mat mat_ref = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC4);
      timer.lap("init");
      byte[] data;
      BufferedImage cvImg;
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      int[] nBits = {8, 8, 8, 8};
      ColorModel cm = new ComponentColorModel(cs, nBits, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
      SampleModel sm = cm.createCompatibleSampleModel(img.getWidth(), img.getHeight());
      DataBufferByte db = new DataBufferByte(img.getWidth() * img.getHeight() * 4);
      WritableRaster r = WritableRaster.createWritableRaster(sm, db, new Point(0, 0));
      cvImg = new BufferedImage(cm, r, false, null);
      timer.lap("empty");
      Graphics2D g = cvImg.createGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
      timer.lap("created");
      data = ((DataBufferByte) cvImg.getRaster().getDataBuffer()).getData();
      mat_ref.put(0, 0, data);
      Mat mat = new Mat();
      timer.lap("filled");
      Imgproc.cvtColor(mat_ref, mat, Imgproc.COLOR_RGBA2BGR, 3);
      timer.end();
      return mat;
    } else {
      return null;
    }
  }

  /**
   * to get old style OpenCV Mat for FindInput
   *
   * @return SWIG interfaced OpenCV Mat
   * @deprecated
   */
  @Deprecated
  protected org.sikuli.natives.Mat getMatNative() {
    return convertBufferedImageToMat(get());
  }

  protected static org.sikuli.natives.Mat convertBufferedImageToMat(BufferedImage img) {
    if (img != null) {
      long theMatTime = new Date().getTime();
      byte[] data = convertBufferedImageToByteArray(img);
      org.sikuli.natives.Mat theMat = Vision.createMat(img.getHeight(), img.getWidth(), data);
      if (Settings.FindProfiling) {
        Debug.logp("[FindProfiling] createCVMat [%d x %d]: %d msec",
                img.getWidth(), img.getHeight(), new Date().getTime() - theMatTime);
      }
      return theMat;
    } else {
      return null;
    }
  }

  protected static byte[] convertBufferedImageToByteArray(BufferedImage img) {
    if (img != null) {
      BufferedImage cvImg = createBufferedImage(img.getWidth(), img.getHeight());
      Graphics2D g = cvImg.createGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
      return ((DataBufferByte) cvImg.getRaster().getDataBuffer()).getData();
    } else {
      return null;
    }
  }

  protected static BufferedImage createBufferedImage(int w, int h) {
    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int[] nBits = {8, 8, 8, 8};
    ColorModel cm = new ComponentColorModel(cs, nBits, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    SampleModel sm = cm.createCompatibleSampleModel(w, h);
    DataBufferByte db = new DataBufferByte(w * h * 4);
    WritableRaster r = WritableRaster.createWritableRaster(sm, db, new Point(0, 0));
    BufferedImage bm = new BufferedImage(cm, r, false, null);
    return bm;
  }

	// **************** for Tesseract4Java ********************
    /**
     * Converts <code>BufferedImage</code> to <code>ByteBuffer</code>.
     *
     * @param bi Input image
     * @return pixel data
     */
    public static ByteBuffer convertImageData(BufferedImage bi) {
        DataBuffer buff = bi.getRaster().getDataBuffer();
        // ClassCastException thrown if buff not instanceof DataBufferByte because raster data is not necessarily bytes.
        // Convert the original buffered image to grayscale.
        if (!(buff instanceof DataBufferByte)) {
            bi = convertImageToGrayscale(bi);
            buff = bi.getRaster().getDataBuffer();
        }
        byte[] pixelData = ((DataBufferByte) buff).getData();
        //        return ByteBuffer.wrap(pixelData);
        ByteBuffer buf = ByteBuffer.allocateDirect(pixelData.length);
        buf.order(ByteOrder.nativeOrder());
        buf.put(pixelData);
        buf.flip();
        return buf;
    }

		/**
     * A simple method to convert an image to gray scale.
     *
     * @param image input image
     * @return a monochrome image
     */
    public static BufferedImage convertImageToGrayscale(BufferedImage image) {
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = tmp.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return tmp;
    }

	/**
	 * find an image in another image
	 * @param img image
	 * @return a Match or null
	 */
	public Match find(Image img) {
		log(-1, "find: not implemented yet");
		return null;
	}

	/**
	 * find all images in another image
	 * @param img image
	 * @return Match or null
	 */
	public Iterator<Match> findAll(Image img) {
		log(-1, "findAll: not implemented yet");
		return null;
	}

	/**
	 * OCR-read the text from the image
	 * @return the text or null
	 */
	public String text() {
//TODO: use Tess4J here already??
    if (Settings.OcrTextRead) {
      TextRecognizer tr = TextRecognizer.getInstance();
      if (tr == null) {
        Debug.error("text: text recognition is now switched off");
        return null;
      }
      String textRead = tr.recognize(this.get());
      log(lvl, "text: #(" + textRead + ")#");
      return textRead;
    }
    Debug.error("text: text recognition is currently switched off");
		return null;
	}

	/**
	 * convenience method: get text from given image file
	 * @param imgFile image filename
	 * @return the text or null
	 */
	public static String text(String imgFile) {
		return create(imgFile).text();
	}
}
