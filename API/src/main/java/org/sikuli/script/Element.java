/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * INTERNAL: An abstract super-class for {@link Region}, {@link Location}, {@link Image} ... .
 * <br>
 * <p>BE AWARE: This class cannot be used as such (cannot be instantiated)
 * <br>... instead use the sub-classes as needed</p>
 * NOTES:
 * <br>- the intention is, to have only one implementation for features,
 * that are the same for all or some pixel/screen related classes
 */
public abstract class Element {

  static final int logLevel = 3;

  static void log(int level, String message, Object... args) {
    String className = Thread.currentThread().getStackTrace()[2].getClassName();
    String caller = className.substring(className.lastIndexOf(".") + 1);
    Debug.logx(level, caller + ": " + message, args);
  }

  static void terminate(String message, Object... args) {
    String className = Thread.currentThread().getStackTrace()[2].getClassName();
    String caller = className.substring(className.lastIndexOf(".") + 1);
    throw new SikuliXception(caller + ": " + String.format(message, args));
  }

  private static boolean trace = false;

  public static void traceOn() {
    trace = true;
  }

  public static void traceOff() {
    trace = false;
  }

  static void trace(String message, Object... args) {
    if (trace) {
      String className = Thread.currentThread().getStackTrace()[2].getClassName();
      String caller = className.substring(className.lastIndexOf(".") + 1);
      Debug.logp("TRACE: " + caller + ": " + message, args);
    }
  }

  static void initTerminate(String message, Object item) {
    terminate("init: " + message, item);
  }

  public String toString() {
    String clazz = this.getClass().getSimpleName();
    String fileName = "";
    if (fileName() != null) {
      fileName = "(" + new File(fileName()).getName() + ")";
    }
    return String.format("[Element: %s(%s) (%d,%d %dx%d) %s]", clazz, sourceClass, x, y, w, h, fileName);
  }

  //<editor-fold desc="000 Fields x, y - top left corner or point (0 for Images)">

  /**
   * @return x of top left corner or point
   */
  public Element setX(int x) {
    this.x = x;
    return this;
  }

  /**
   * sets x to the given value.
   * convenience: allow calculated decimals
   *
   * @param x new x
   * @return this
   */
  public Element setX(double x) {
    this.x = (int) x;
    return this;
  }

  /**
   * @return x of top left corner or point
   */
  public int getX() {
    return x;
  }

  /**
   * x (horizontal) of top left corner or point.
   */
  public int x = 0;

  /**
   * sets y to the given value
   *
   * @param y new y
   * @return this
   */
  public Element setY(int y) {
    this.y = y;
    return this;
  }

  /**
   * sets y to the given value
   * convenience: allow calculated decimals
   *
   * @param y new y
   * @return this
   */
  public Element setY(double y) {
    this.y = (int) y;
    return this;
  }

  /**
   * @return y of top left corner or point
   */
  public int getY() {
    return y;
  }

  /**
   * y (vertical) of top left corner or point
   */
  public int y = 0;

  /**
   * sets the coordinates/top left corner to the given values
   *
   * @param x new x
   * @param y new y
   * @return this
   */
  public Element set(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * sets the coordinates/top left corner to the given values.
   * convenience: allow calculated decimals
   *
   * @param x new x double
   * @param y new y double
   * @return this
   */
  public Element set(double x, double y) {
    set((int) x, (int) y);
    return this;
  }

  public Element setLocation(Object topLeft) {
    if (topLeft instanceof Point) {
      set(((Point) topLeft).x, ((Point) topLeft).y);
    } else if (topLeft instanceof Element) {
      set(((Element) topLeft).x, ((Element) topLeft).y);
    }
    return this;
  }

  public Element reLocate(Object topLeft) {
    if (topLeft instanceof Point) {
      set(((Point) topLeft).x + x, ((Point) topLeft).y + y);
    } else if (topLeft instanceof Element) {
      set(((Element) topLeft).x + x, ((Element) topLeft).y + y);
    }
    return this;
  }

  /**
   * get as AWT point
   *
   * @return Point
   */
  public Point getPoint() {
    return new Point(x, y);
  }
  //</editor-fold>

  //<editor-fold desc="001 Fields w, h - dimension (0 for points)">

  /**
   * @return width of the Element (0 for Points)
   */
  public int getW() {
    return w;
  }

  public void setW(int w) {
    this.w = w;
  }

  /**
   * Width of the Element (0 for Points)
   */
  public int w = 0;

  /**
   * @return height of Element (0 for Points)
   */
  public int getH() {
    return h;
  }

  public void setH(int h) {
    this.h = h;
  }

  /**
   * Height of the Element (0 for Points)
   */
  public int h = 0;
  //</editor-fold>

  //<editor-fold desc="002 Fields rectangle">
  protected void copyElementRectangle(Element element) {
    x = element.x;
    y = element.y;
    w = element.w;
    h = element.h;
  }

  protected void setSize(Mat mat) {
    w = mat.cols();
    h = mat.rows();
  }

  /**
   * @return size of image
   */
  public Dimension getSize() {
    return new Dimension(w, h);
  }

  public boolean sameSize(Element element) {
    return getSize().equals(element.getSize());
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
  public Element size(float factor) {
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
  public Element size(float factor, Interpolation interpolation) {
    SXOpenCV.resize(getContent(), factor, interpolation);
    return this;
  }

  /**
   * @return the AWT Rectangle of the region
   */
  public Rectangle getRect() {
    return new Rectangle(x, y, w, h);
  }

  /**
   * set the regions position/size.
   * <br>this might move the region even to another screen
   *
   * @param r the AWT Rectangle to use for position/size
   * @return the region itself
   */
  public Element setRect(Rectangle r) {
    return setRect(r.x, r.y, r.width, r.height);
  }

  /**
   * set the regions position/size.
   * <br>this might move the region even to another screen
   *
   * @param X new x of top left corner
   * @param Y new y of top left corner
   * @param W new width
   * @param H new height
   * @return the region itself
   */
  public Element setRect(int X, int Y, int W, int H) {
    x = X;
    y = Y;
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(null);
    return this;
  }

  /**
   * set the regions position/size.
   * <br>this might move the region even to another screen
   *
   * @param r the region to use for position/size
   * @return the region itself
   */
  public Element setRect(Region r) {
    return setRect(r.x, r.y, r.w, r.h);
  }

  private float resizeFactor = 1;

  public float resize() {
    return resizeFactor;
  }

  public Element resize(float factor) {
    resizeFactor = Math.max(factor, 0.1f);
    return this;
  }

  /**
   * create a new region containing both regions
   *
   * @param element region to unite with
   * @return the new region
   */
  public Match union(Element element) {
    Rectangle rect = getRect().union(element.getRect());
    return new Match(rect);
  }

  /**
   * create a region that is the intersection of the given regions
   *
   * @param element the region to intersect with like AWT Rectangle API
   * @return the new region
   */
  public Match intersection(Element element) {
    Rectangle rect = getRect().intersection(element.getRect());
    return new Match(rect);
  }

  public boolean isInside(Element element) {
    Match intersection = intersection(element);
    return intersection.w == w && intersection.h == h;
  }
  //</editor-fold>

  //<editor-fold desc="003 Fields pixel content">
  public static <SUFEBM> Image getImage(SUFEBM target) {
    if (target instanceof Image) {
      return (Image) target;
    } else if (target instanceof Pattern) {
      return ((Pattern) target).getImage();
    }
    return new Image(target);
  }

  public static <SUFEBM> BufferedImage getBufferedImage(SUFEBM whatEver) {
    return getImage(whatEver).getBufferedImage();
  }

  public Image getImage() {
    return (Image) this;
  }

  public BufferedImage getBufferedImage() {
    return SXOpenCV.makeBufferedImage(getImage().getContent());
  }

  /**
   * check whether image has pixel content<br>
   *
   * @return true if has pixel content
   */
  public boolean isValid() {
    if (isOnScreen() || null == imageURL) {
      if (imageContent.empty() && this instanceof ScreenImage) {
        imageContent = ((ScreenImage) this).makeMat();
      }
      return !imageContent.empty();
    }
    if (Settings.ImageCaching) {
      return isCached(url());
    }
    return !getImageContent().empty();
  }

  public Mat getContent() {
    if (isOnScreen() || null == imageURL) {
      if (imageContent.empty() && this instanceof ScreenImage) {
        imageContent = ((ScreenImage) this).makeMat();
      }
      return imageContent;
    }
    return getMat(this);
  }

  Mat getImageContent() {
    return imageContent;
  }

  void setImageContent(Mat content) {
    imageContent = content;
  }

  public Mat cloneContent() {
    return getContent().clone();
  }

  public void setContent(Mat mat) {
    imageContent = mat;
  }

  public void updateContent(Mat mat) {
    if (null == imageURL) {
      imageContent = mat;
    } else {
      putIntoCache(this, mat); // update content
    }
  }

  protected void copyElementContent(Element element) {
    if (element.url() != null) {
      url(element.url());
      if (!Settings.ImageCaching) {
        setImageContent(element.getImageContent());
      }
    } else {
      setContent(element.cloneContent());
    }
  }

  public boolean isFakeImage() {
    return getName().equals(FAKE_IMAGE);
  }

  public void asFakeImage() {
    setName(FAKE_IMAGE);
  }

  private static final String FAKE_IMAGE = "Internal-Fake-Image";

  public boolean isMaskImage() {
    return getName().equals(MASK_IMAGE);
  }

  public static String asMaskImage() {
    return MASK_IMAGE;
  }

  private static final String MASK_IMAGE = "Internal-Mask-Image";

  protected void createContent(URL url) {
    createContent(url, false);
  }

  protected void reCreateContent() {
    createContent(url(), true);
  }

  private void createContent(URL url, boolean isReLoad) {
    boolean success = true;
    if (null == url) {
      if (getName().isEmpty()) {
        return;
      }
      success = false;
    }
    if (success && "file".equals(url.getProtocol())) {
      try {
        url = new URL("file", null, -1, new File(url.getPath()).getCanonicalFile().getAbsolutePath());
      } catch (IOException e) {
        success = false;
      }
    }
    if (success) {
      if (!isFakeImage() && isCached(url)) {
        if (!isReLoad) {
          imageURL = url;
          setSize(getMat(this));  //TODO revise FakeImage hack
          return;
        }
      }
      Mat content = getMatFromURL(url, isMaskImage());
      if (!content.empty()) {
        imageURL = url;
        if (!isFakeImage()) {
          setSize(content);
        }
        putIntoCache(this, content); // create content
      } else {
        success = false;
      }
    }
    if (!success) {
      Object target = url == null ? getName() : url;
      if (!handleImageMissing(target)) {
        initTerminate("Image finally not loaded: %s", url);
      }
    }
  }

  private static Mat getMatFromURL(URL url, boolean isMaskImage) {
    byte[] bytes = null;
    Mat content = new Mat();
    try {
      InputStream inputStream = url.openStream();
      bytes = inputStream.readAllBytes();
    } catch (IOException e) {
    }
    if (bytes != null) {
      MatOfByte matOfByte = new MatOfByte();
      matOfByte.fromArray(bytes);
      content = Imgcodecs.imdecode(matOfByte, -1);
      if (isMaskImage) {
        List<Mat> mats = SXOpenCV.extractMask(content, false);
        content = mats.get(1);
      }
    }
    return content;
  }

  public static Mat reload(URL url) {
    Mat content = new Mat();
    if (url != null) {
      content = getMatFromURL(url, false);
    }
    return content;
  }

  protected static void reload(String fpImage) {
    URL url = evalURL(fpImage);
    if (url != null) {
      Image image = new Image();
      image.asFakeImage(); //TODO revise FakeImage hack
      image.createContent(url); //TODO reload
    }
  }

  public void reload() {
    if (url() != null) {
      reCreateContent();
    }
  }

  private Mat imageContent = SXOpenCV.newMat();
  //</editor-fold>

  //<editor-fold desc="004 Fields CV-attributes">
  private double stdDev = -1;

  public void stdDev(double value) {
    stdDev = value;
  }

  private double mean = -1;

  public void mean(double value) {
    mean = value;
  }

  private Color meanColor = null;

  public void meanColor(Color color) {
    meanColor = color;
  }

  private boolean plain = false;

  public boolean plain() {
    return plain;
  }

  public void plain(boolean state) {
    plain = state;
  }

  private boolean black = false;

  public void black(boolean state) {
    black = state;
  }

  public boolean black() {
    return black;
  }

  private boolean white = false;

  public void white(boolean state) {
    white = state;
  }

  public double diffPercentage(Image otherImage) {
    if (SX.isNull(otherImage)) {
      return 1.0;
    }
    return SXOpenCV.diffPercentage(getContent(), otherImage.getContent());
  }
  //</editor-fold>

  //<editor-fold desc="005 Fields URL, filename">
  private final static String PNG = "png";
  private final static String dotPNG = "." + PNG;

  protected static URL evalURL(String imagefileName) {
    return ImagePath.find(imagefileName);
  }

  protected static URL evalURL(File imagefile) {
    return ImagePath.find(imagefile);
  }

  public static URL createURL(String fileName) {
    return createURL(new File(fileName));
  }

  public static URL createURL(File file) {
    String urlPath = file.getPath();
    if (file.isAbsolute() || file.getPath().startsWith("\\")) {
      try {
        urlPath = file.getCanonicalFile().getAbsolutePath();
      } catch (IOException e) {
        urlPath = file.getAbsolutePath();
      }
    }
    try {
      return new URL("file", null, -1, urlPath);
    } catch (IOException e) {
    }
    return null;
  }

  public void url(URL url) {
    if (url.getProtocol().equals("file")) {
      imageURL = createURL(url.getPath());
    }
    imageURL = url;
  }

  public void url(String fileName) {
    imageURL = createURL(fileName);
  }

  public boolean hasURL() {
    return null != imageURL;
  }

  public URL url() {
    return imageURL;
  }

  private URL imageURL = null;

  public String fileName() {
    File file = file();
    return (file == null ? null : file.getAbsolutePath());
  }

  public File file() {
    if (imageURL != null) {
      return new File(imageURL.getPath());
    }
    return null;
  }

  static boolean isFile(URL url) {
    return url != null && url.getProtocol().equals("file");
  }

  boolean reloaded = false;

  protected boolean isReloaded() {
    return reloaded;
  }

  protected void wasReloaded() {
    reloaded = true;
  }
  //</editor-fold>

  //<editor-fold desc="006 Fields name, lastMatch, text...">
  protected boolean shouldCheckLastSeen() {
    return Settings.CheckLastSeen;
  }

  private boolean isText = false;

  public boolean isText() {
    return isText;
  }

  public Element asText(boolean state) {
    isText = state;
    return this;
  }

  /**
   * INTERNAL: to identify an Element
   *
   * @return the name
   */
  public String getName() {
    if (name.isEmpty() && fileName() != null) {
      return new File(fileName()).getName();
    }
    return name;
  }

  /**
   * INTERNAL: to identify an Element
   *
   * @param name to be used
   */
  public void setName(String name) {
    this.name = name;
  }

  private String name = "";
  protected String sourceClass = "";

  protected Match lastMatch = null;
  protected Matches lastMatches = null;

  private void resetLastMatch(boolean findAll) {
    if (findAll) {
      lastMatches = null;
    } else {
      lastMatch = null;
    }
  }

  //TODO need to be cloned?
  protected void copyMatchAttributes(Element element) {
    lastMatch = element.lastMatch;
    lastMatches = element.lastMatches;
  }

  protected long lastSearchTime = -1;
  protected long lastFindTime = -1;
  protected long lastSearchTimeRepeat = -1;

  /**
   * a find operation saves its match on success in this region/image.
   * <br>... unchanged if not successful
   *
   * @return the Match object from last successful find
   */
  public Match getLastMatch() {
    return lastMatch;
  }

  public Match match() {
    if (null != lastMatch) {
      return lastMatch;
    }
    return new Match(this);
  }

  public Match match(Match match) {
    lastMatch = match;
    return match;
  }

  /**
   * a searchAll operation saves its matches on success in this region/image
   * <br>... unchanged if not successful
   *
   * @return a Match-Iterator of matches from last successful searchAll
   */
  public Iterator<Match> getLastMatches() {
    return lastMatches;
  }

  public Iterator<Match> matches() {
    if (null != lastMatches) {
      return lastMatches;
    }
    return new Match(this);
  }

  public Match matches(Match match) {
    lastMatch = match;
    return match;
  }

  //</editor-fold>

  //<editor-fold desc="007 Fields wait observe timing">
  protected void copyTimingAttributes(Element element) {
    autoWaitTimeout = element.autoWaitTimeout;
    waitScanRate = element.waitScanRate;
    observeScanRate = element.observeScanRate;
    repeatWaitTime = element.repeatWaitTime;
  }

  /**
   * the time in seconds a find operation should wait.
   * <br>for the appearence of the target in this element
   * <br>initial value is {@link Settings#AutoWaitTimeout} (default 3) - ignored for non-screen
   *
   * @param sec seconds
   */
  public void setAutoWaitTimeout(double sec) {
    autoWaitTimeout = sec;
  }

  /**
   * current autoWaitTimeout
   *
   * @return value in seconds
   * @see #setAutoWaitTimeout(double)
   */
  public double getAutoWaitTimeout() {
    return autoWaitTimeout;
  }

  private double autoWaitTimeoutDefault = Settings.AutoWaitTimeout;
  private double autoWaitTimeout = autoWaitTimeoutDefault;

  /**
   * @return current WaitScanRate as rate per second
   * @see #setWaitScanRate(float)
   */
  public float getWaitScanRate() {
    return waitScanRate;
  }

  /**
   * the rate how often an image search should be repeated in this element.
   * <br>pause between subsequent searches is 1/waitScanrate in seconds
   * <br>initial value is {@link Settings#WaitScanRate} (default 3) - ignored for non-screen
   *
   * @param waitScanRate decimal number as rate per second
   */
  public void setWaitScanRate(float waitScanRate) {
    this.waitScanRate = waitScanRate;
    scanWait = 1000 / waitScanRate;
  }

  private void waitAfterScan(long before, long until) {
    long now = new Date().getTime();
    long time = (long) (scanWait - now + before);
    if (time < 10) {
      return;
    }
    if ((now + time) > until) {
      time = until - now;
    }
    if (time > 9) {
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
      }
    }
  }

  private float waitScanRateDefault = Settings.WaitScanRate;
  private double scanWait = 1000 / waitScanRateDefault;
  private float waitScanRate = waitScanRateDefault;


  /**
   * @return current observeScanRate as rate per second
   * @see #setObserveScanRate(float)
   */
  public float getObserveScanRate() {
    return observeScanRate;
  }

  /**
   * set the individual observeScanRate
   *
   * @param observeScanRate decimal number as rate per second
   */
  public void setObserveScanRate(float observeScanRate) {
    this.observeScanRate = observeScanRate;
  }

  private float observeScanRateDefault = Settings.ObserveScanRate;
  private float observeScanRate = observeScanRateDefault;

  /**
   * INTERNAL: Observe
   *
   * @return the regions current RepeatWaitTime time in seconds
   */
  public int getRepeatWaitTime() {
    return repeatWaitTime;
  }

  /**
   * INTERNAL: Observe set the regions individual WaitForVanish
   *
   * @param time in seconds
   */
  public void setRepeatWaitTime(int time) {
    repeatWaitTime = time;
  }

  private int repeatWaitTimeDefault = Settings.RepeatWaitTime;
  private int repeatWaitTime = repeatWaitTimeDefault;
  //</editor-fold>

  //<editor-fold desc="008 Fields FindFailed Settings">
  protected void copyFindFailedSettings(Element element) {
    throwException = element.throwException;
    findFailedResponse = element.findFailedResponse;
    findFailedHandler = element.findFailedHandler;
  }

  /**
   * true - should throw {@link FindFailed} if not found in this region<br>
   * false - do not abort script on FindFailed (might lead to NPE's later)<br>
   * default: {@link Settings#ThrowException}<br>
   * sideEffects: {@link #setFindFailedResponse(FindFailedResponse)} true:ABORT, false:SKIP<br>
   * see also: {@link #setFindFailedResponse(FindFailedResponse)}<br>
   * and: {@link #setFindFailedHandler(Object)}
   *
   * @param flag true/false
   */
  public void setThrowException(boolean flag) {
    throwException = flag;
    if (throwException) {
      findFailedResponse = FindFailedResponse.ABORT;
    } else {
      findFailedResponse = FindFailedResponse.SKIP;
    }
  }

  /**
   * reset to default {@link #setThrowException(boolean)}
   */
  public void resetThrowException() {
    setThrowException(throwExceptionDefault);
  }

  /**
   * current setting {@link #setThrowException(boolean)}
   *
   * @return true/false
   */
  public boolean getThrowException() {
    return throwException;
  }

  private boolean throwExceptionDefault = Settings.ThrowException;
  private boolean throwException = throwExceptionDefault;

  /**
   * FindFailedResponse.<br>
   * ABORT - abort script on FindFailed <br>
   * SKIP - ignore FindFailed<br>
   * PROMPT - display prompt on FindFailed to let user decide how to proceed<br>
   * RETRY - continue to wait for appearence after FindFailed<br>
   * HANDLE - call a handler on exception {@link #setFindFailedHandler(Object)}<br>
   * default: ABORT<br>
   * see also: {@link #setThrowException(boolean)}
   *
   * @param response {@link FindFailed}
   */
  public void setFindFailedResponse(FindFailedResponse response) {
    findFailedResponse = response;
  }

  /**
   * reset to default {@link #setFindFailedResponse(FindFailedResponse)}
   */
  public void resetFindFailedResponse() {
    setFindFailedResponse(findFailedResponseDefault);
  }

  /**
   * @return the current setting {@link #setFindFailedResponse(FindFailedResponse)}
   */
  public FindFailedResponse getFindFailedResponse() {
    return findFailedResponse;
  }

  private FindFailedResponse findFailedResponseDefault = FindFailed.getResponse();
  private FindFailedResponse findFailedResponse = findFailedResponseDefault;

  public void setFindFailedHandler(Object handler) {
    if (handler != null) {
      findFailedResponse = FindFailedResponse.HANDLE;
      findFailedHandler = FindFailed.setHandler(handler, ObserveEvent.Type.FINDFAILED);
      log(logLevel, "Setting FindFailedHandler");
    }
  }

  public Object getFindFailedHandler() {
    return findFailedHandler;
  }

  private Object findFailedHandler = FindFailed.getFindFailedHandler();
  //</editor-fold>

  //<editor-fold desc="009 image cache">
  public static void resetCache() {
    if (Settings.ImageCaching) {
      ImageCache.reset();
    }
  }

  public static Map<URL, List<Object>> getCache() {
    return ImageCache.cache;
  }

  public static String cacheStats() {
    if (Settings.ImageCaching) {
      return ImageCache.stats();
    }
    return "";
  }

  static boolean isCached(URL url) {
    if (Settings.ImageCaching) {
      return ImageCache.isCached(url);
    }
    return false;
  }

  static void putIntoCache(Element element, Mat content) {
    if (Settings.ImageCaching) {
      ImageCache.put(element.url(), content);
    } else {
      ImageCache.put(element.url());
      element.setImageContent(content);
    }
  }

  static Mat getMat(Element element) {
    if (Settings.ImageCaching) {
      return ImageCache.getMat(element);
    }
    return element.getImageContent();
  }

  public static Match getMatchLastSeen(Element element) {
    return ImageCache.getLastMatched(element);
  }

  static void setMatchLastSeen(Element element, Match match) {
    ImageCache.setLastMatched(element, match);
  }

  private static class ImageCache {

    static int ITEM_MAT = 0;
    static int ITEM_COUNT = 1;
    static int ITEM_LASTMOD = 2;
    static int ITEM_LASTSEEN = 3;

    static Map<URL, List<Object>> cache = Collections.synchronizedMap(new HashMap<>());

    public static boolean isCached(URL url) {
      List<Object> items = cache.get(url);
      if (items == null) {
        return false;
      }
      return null != items.get(0) && !((Mat) items.get(0)).empty();
    }

    static void put(URL url) {
      put(url, null);
    }

    static Mat put(URL url, Mat mat) {
      ArrayList<Object> items = new ArrayList<>();
      items.add(mat);
      items.add(0.0); //reuse count
      items.add(isFile(url) ? new File(url.getPath()).lastModified() : -1); //to detect external change
      items.add(null); //last seen match
      cache.put(url, items);
      return mat;
    }

    static Match getLastMatched(Element element) {
      URL url = element.url();
      if (url == null) {
        return null;
      }
      List<Object> items = cache.get(url);
      if (items == null) {
        return null;
      }
      return (Match) items.get(ITEM_LASTSEEN);
    }

    static void setLastMatched(Element element, Match match) {
      URL url = element.url();
      if (url == null) {
        return;
      }
      List<Object> items = cache.get(url);
      if (items == null) {
        return;
      }
      match.setImage(null);
      items.set(ITEM_LASTSEEN, match);
      cache.put(url, items);
    }

    static Mat getMat(Element element) {
      URL url = element.url();
      List<Object> items = cache.get(url);
      if (items == null) {
        return new Mat();
      }
      Object content = items.get(ITEM_MAT);
      if (null == content) {
        return new Mat();
      }
      Mat newMat = possibleReload(element, url, items);
      if (element.isReloaded()) {
        content = newMat;
        items.set(ITEM_MAT, content);
      }
      useCountUp(items);
      return (Mat) content;
    }

    private static Mat possibleReload(Element element, URL url, List<Object> items) {
      Mat newContent = new Mat();
      if (isFile(url)) {
        long modified = new File(url.getPath()).lastModified();
        long lastMod = (long) items.get(ITEM_LASTMOD);
        if (modified > lastMod) {
          newContent = reload(url);
          if (!newContent.empty()) {
            items.set(ITEM_COUNT, -1);
            items.set(ITEM_LASTMOD, modified);
            element.wasReloaded();
          }
        }
      }
      return newContent;
    }

    private static void useCountUp(List<Object> items) {
      Double count = (Double) items.get(ITEM_COUNT) + 1;
      if (count < Double.MAX_VALUE) {
        items.set(ITEM_COUNT, count);
      }
    }

    static void reset() {
      cache = Collections.synchronizedMap(new HashMap<>());
    }

    public static String stats() {
      int count = cache.size();
      double size = 0;
      double used = 0;
      for (List<Object> items : cache.values()) {
        size += ((Mat) items.get(0)).width() * ((Mat) items.get(0)).height();
        used += (Double) items.get(1);
      }
      return String.format("ImageCache: urls(%d) size(%.0f KB) used(%.0f times)", count, size / 1000, used);
    }
  }
  //</editor-fold>

  //<editor-fold desc="010 global features">
  protected void copyElementAttributes(Element element) {
    name = element.name;
    copyElementRectangle(element);
    copyElementContent(element);
    copyMatchAttributes(element);
    copyTimingAttributes(element);
    copyFindFailedSettings(element);
  }

  protected static boolean isValidImageFilename(String fname) {
    String validEndings = ".png.jpg.jpeg";
    String ending = FilenameUtils.getExtension(fname);
    return !ending.isEmpty() && validEndings.contains(ending.toLowerCase());
  }

  public static String getValidImageFilename(String fname) {
    if (isValidImageFilename(fname)) {
      return fname;
    }
    return fname + ".png";
  }

  protected Location getLocationFromTarget(Object target) throws FindFailed { //TODO allow AWT elements: Rectangle, Point, ...
    if (!(target instanceof ArrayList)) {
      if (target instanceof Element && ((Element) target).isOnScreen()) {
        return ((Element) target).getTarget();
      }
      Match match = wait(target);
      if (match != null) {
        if (isOtherScreen()) {
          return (Location) match.getTarget().setOtherScreen(getScreen());
        } else {
          return match.getTarget();
        }
      }
    } else {
      ArrayList parms = (ArrayList) target;
      if (parms.size() == 1) {
        target = parms.get(0);
      } else if (parms.size() == 2) {
        if (parms.get(0) instanceof Integer && parms.get(1) instanceof Integer) {
          return new Location((Integer) parms.get(0), (Integer) parms.get(1));
        }
      } else {
        return null;
      }
    }
    return null;
  }

  // to avoid NPE for Regions being outside any screen
  protected IRobot getRobotForElement() {
    if (getScreen() == null) {
      return Screen.getGlobalRobot();
    }
    return getScreen().getRobot();
  }

  /**
   * convenience method
   *
   * @return the region's center
   */
  public Location getTarget() {
    return getCenter();
  }

  /**
   * @return the center pixel location of the Element or the Point
   */
  public Location getCenter() {
    return checkAndSetRemote(new Location(getX() + getW() / 2, getY() + getH() / 2));
  }

  /**
   * Moves the region to the area, whose center is the given location
   *
   * @param loc the location which is the new center of the region
   * @return the region itself
   */
  public Element setCenter(Location loc) {
    Location c = getCenter();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(null);
    return this;
  }

  /**
   * WARNING: wait(long timeout) is taken by Java Object as final. This method catches any interruptedExceptions
   *
   * @param timeout The time to wait
   */
  public void wait(double timeout) {
    try {
      Thread.sleep((long) (timeout * 1000L));
    } catch (InterruptedException e) {
    }
  }

  protected boolean isPoint() {
    return false;
  }

  protected boolean isEmpty() {
    return w <= 1 && h <= 1;
  }
  //</editor-fold>

  //<editor-fold desc="011 save content to file">
  public String save(String name) {
    return save(name, ImagePath.getBundlePath());
  }

  public String save(String name, String path) {
    if (!isValid() || path == null) {
      return null;
    }
    File fImg = new File(path, name);
    return save(fImg);
  }

  public String save(File imageFile) {
    boolean imwrite = false;
    File parent = imageFile.getParentFile();
    String name = getValidImageFilename(imageFile.getName());
    imageFile = new File(parent, name);
    try {
      FileUtils.forceMkdir(parent);
      imwrite = Imgcodecs.imwrite(imageFile.getAbsolutePath(), getContent());
    } catch (IOException e) {
    }
    if (imwrite) {
      Debug.log(3, "Image::save: %s", imageFile);
    } else {
      Debug.error("Image::save: %s did not work", imageFile);
      return null;
    }
    return imageFile.getAbsolutePath();
  }
  //</editor-fold>

  //<editor-fold desc="015 Screen related">
  protected boolean isOnScreen() {
    return isScreenElement;
  }

  protected void onScreen(boolean state) {
    isScreenElement = state;
  }

  private boolean isScreenElement = true;

  //TODO revise local/remote screen handling
  protected Location checkAndSetRemote(Location loc) {
    if (!isOtherScreen()) {
      return loc;
    }
    return (Location) loc.setOtherScreen(getScreen());
  }

  //TODO revise initScreen/initRegion

  /**
   * INTERNAL: USE
   *
   * @param iscr screen
   */
  public void initScreen(IScreen iscr) {
    // check given screen first
    Rectangle rect, screenRect;
    IScreen screen, screenOn;
    if (iscr != null) {
      if (iscr.isOtherScreen()) {
        if (x < 0) {
          w = w + x;
          x = 0;
        }
        if (y < 0) {
          h = h + y;
          y = 0;
        }
        setScreen(iscr);
        setOtherScreen(iscr);
        return;
      }
      if (iscr.getID() > -1) {
        rect = regionOnScreen(iscr);
        if (rect != null) {
          x = rect.x;
          y = rect.y;
          w = rect.width;
          h = rect.height;
          setScreen(iscr);
          return;
        }
      } else {
        // is ScreenUnion
        return;
      }
    }
    // check all possible screens if no screen was given or the region is not on given screen
    // crop to the screen with the largest intersection
    screenRect = new Rectangle(0, 0, 0, 0);
    screenOn = null;

    if (getScreen() == null || !isOtherScreen()) {
      for (int i = 0; i < Screen.getNumberScreens(); i++) {
        screen = Screen.getScreen(i);
        rect = regionOnScreen(screen);
        if (rect != null) {
          if (rect.width * rect.height > screenRect.width * screenRect.height) {
            screenRect = rect;
            screenOn = screen;
          }
        }
      }
    } else {
      rect = regionOnScreen(getScreen());
      if (rect != null) {
        if (rect.width * rect.height > screenRect.width * screenRect.height) {
          screenRect = rect;
          screenOn = getScreen();
        }
      }
    }

    if (screenOn != null) {
      x = screenRect.x;
      y = screenRect.y;
      w = screenRect.width;
      h = screenRect.height;
      setScreen(screenOn);
    } else {
      // no screen found
      setScreen(null);
      Debug.error("Region(%d,%d,%d,%d) outside any screen - subsequent actions might not work as expected", x, y, w, h);
    }
  }

  /**
   * Checks if the Screen contains the Region.
   *
   * @param screen The Screen in which the Region might be
   * @return True, if the Region is on the Screen. False if the Region is not inside the Screen
   */
  protected Rectangle regionOnScreen(IScreen screen) {
    if (screen == null) {
      return null;
    }
    // get intersection of Region and Screen
    Rectangle rect = screen.getRect().intersection(getRect());
    // no Intersection, Region is not on the Screen
    if (rect.isEmpty()) {
      return null;
    }
    return rect;
  }

  /**
   * INTERNAL: USE - EXPERIMENTAL if true: this region is not bound to any screen
   *
   * @param rect rectangle
   * @return the current state
   */
  public static Region virtual(Rectangle rect) {
    Region reg = new Region();
    reg.x = rect.x;
    reg.y = rect.y;
    reg.w = rect.width;
    reg.h = rect.height;
    reg.setVirtual(true);
    reg.setScreen(Screen.getPrimaryScreen());
    return reg;
  }

  /**
   * INTERNAL: USE - EXPERIMENTAL if true: this region is not bound to any screen
   *
   * @return the current state
   */
  public boolean isVirtual() {
    return isVirtual;
  }

  /**
   * INTERNAL: USE - EXPERIMENTAL
   *
   * @param state if true: this region is not bound to any screen
   */
  public void setVirtual(boolean state) {
    isVirtual = state;
  }

  private boolean isVirtual = false;

  public IScreen getScreen() {
    if (this instanceof IScreen) {
      return (IScreen) this;
    }
    if (isOtherScreen()) {
      return getOtherScreen();
    }
    if (isOnScreen()) {
      if (scr == null) {
        if (Screen.isHeadless())
          throw new SikuliXception("Element::getScreen: not possible - running headless");
        for (int i = 0; i < Screen.getNumberScreens(); i++) {
          if (Screen.getScreen(i).getBounds().contains(this.x, this.y)) {
            setScreen(Screen.getScreen(i));
            break;
          }
        }
        return Screen.getPrimaryScreen();
      }
    }
    return scr;
  }

  public void setScreen(IScreen scr) {
    if (this instanceof IScreen) {
      return;
    }
    if (isOnScreen()) {
      this.scr = scr;
    }
  }

  /**
   * Sets a new Screen for this region.
   *
   * @param id the containing screen object's id
   * @return the region itself
   */
  protected Element setScreen(int id) {
    setScreen(Screen.getScreen(id));
    return this;
  }

  /**
   * The Screen containing the Region
   */
  private IScreen scr = null;

  //TODO feature otherScreen has to be revised

  /**
   * INTERNAL USE
   * reveals wether the containing screen is a DeskTopScreen or not
   *
   * @return null if DeskTopScreen
   */
  public boolean isOtherScreen() {
    return (otherScreen != null);
  }

  /**
   * INTERNAL USE
   * identifies the point as being on a non-desktop-screen
   *
   * @param scr Screen
   * @return this
   */
  public Element setOtherScreen(IScreen scr) {
    otherScreen = scr;
    return this;
  }

  /**
   * INTERNAL USE
   * identifies the point as being on a non-desktop-screen
   * if this is true for the given Element
   *
   * @return this
   */
  protected Element setOtherScreenOf(Element element) {
    if (element.isOtherScreen()) {
      setOtherScreen(element.getOtherScreen());
    }
    return this;
  }

  /**
   * @return the non-desktop-screen
   */
  public IScreen getOtherScreen() {
    return otherScreen;
  }

  private IScreen otherScreen = null;
  //</editor-fold>

  //<editor-fold desc="016 handle image missing">
  static FindFailedResponse missing = FindFailedResponse.ABORT;

  public static void setMissingPrompt() {
    missing = FindFailedResponse.PROMPT;
  }

  public static void setMissingAbort() {
    missing = FindFailedResponse.ABORT;
  }

  protected void setImageMissingHandler(Object handler) {
    imageMissingHandler = FindFailed.setHandler(handler, ObserveEvent.Type.MISSING);
  }

  protected Object imageMissingHandler = FindFailed.getImageMissingHandler();

  protected Boolean handleImageMissing(Object what) {
    if (!(this instanceof Image)) {
      terminate("handleImageMissing: not valid for this %s and that %s", this, what);
    }
    File imageFile = null;
    if (what instanceof File) {
      imageFile = (File) what;
    } else if (what instanceof URL) { //TODO image missing URL
      terminate("handleImageMissing: not implemented for URL: %s", what);
    } else if (what instanceof String) {
      imageFile = new File((String) what);
    } else {
      terminate("handleImageMissing: not implemented for %s (%s)", what, what.getClass());
    }
    FindFailedResponse whatToDo = missing;
    if (imageMissingHandler != null) {
      ObserveEvent evt = null;
      evt = new ObserveEvent("", ObserveEvent.Type.MISSING, null, this, this, 0);
      ((ObserverCallBack) imageMissingHandler).missing(evt);
      whatToDo = evt.getResponse();
    }
    if (FindFailedResponse.PROMPT.equals(whatToDo)) {
      String folder = imageFile.getParent();
      String imageName = getValidImageFilename(imageFile.getName());
      String message = "Folder: " + (folder == null ? "not yet selected" : folder);
      String title = "Image missing: " + imageName;
      Integer response = SX.popGeneric(message, title, "Abort", new String[]{"Capture", "Abort"});
      if (response == 0) {
        response = SX.popGeneric("Decide where to save the shot.", "Capture: " + title, "Save in Bundle",
            new String[]{"Save in Bundle", "Select folder", "Abort"});
        if (response == 0) {
          imageFile = new File(ImagePath.getBundlePath(), imageName);
        } else if (response == 1) {
          String imageFolder = SX.popFile("Decide where to save the shot.", "Capture: " + title);
          imageFile = new File(imageFolder);
          if (!imageFile.isDirectory()) {
            imageFile = imageFile.getParentFile();
          }
          imageFile = new File(imageFile, imageName);
        } else {
          return false;
        }
        if (captureImage(imageFile)) {
          return true;
        }
      }
    }
    if (Settings.SwitchToText) {
      asText(true);
      return true;
    }
    return false;
  }

  private boolean captureImage() {
    return captureImage(null);
  }

  private boolean captureImage(File file) { //TODO: allow OK with mouse in corner (click OK steels focus)
    URL url;
    if (null == file) {
      url = url();
      file = file();
    } else {
      url = evalURL(file);
    }
    SX.popup("Make the screen ready for capture." +
        "\n\nClick OK when ready.", "Capture missing image");
    RunTime.pause(1);
    ScreenImage simg = new Screen(0).userCapture("Capture missing image" + file.getName());
    if (simg.isValid()) {
      simg.getFile(file.getParent(), Element.getValidImageFilename(file.getName()));
      Mat content = simg.getContent();
      setSize(content);
      url(url);
      putIntoCache(this, content); // capture
      return true;
    }
    return false;
  }

  @Deprecated
  protected Boolean handleImageMissing(Element element, boolean recap) { //TODO deprecated (see handleFindFailed)
    if (!(this instanceof Image)) {
      terminate("handleImageMissing: not valid for this");
    }
    Image image = (Image) element;
    log(logLevel, "handleImageMissing: %s", image.getName());
    ObserveEvent evt = null;
    FindFailedResponse response = findFailedResponse;
    if (!recap && imageMissingHandler != null) {
      evt = new ObserveEvent("", ObserveEvent.Type.MISSING, null, image, this, 0);
      ((ObserverCallBack) imageMissingHandler).missing(evt);
      response = evt.getResponse();
    }
    if (recap || FindFailedResponse.PROMPT.equals(response)) {
      response = handleFindFailedShowDialog(image, true);
    }
    if (FindFailedResponse.RETRY.equals(response)) {
      getRobotForElement().delay(500);
      ScreenImage simg = getScreen().userCapture((recap ? "recapture " : "capture missing ") + image.getName());
      if (simg != null) {
        String path = ImagePath.getBundlePath();
        if (path == null) {
          return null;
        }
        simg.getFile(path, image.getName());
        image.reload();
        if (image.isValid()) {
          Image.setIDEshouldReload(image);
          return true;
        }
      }
      return null;
    } else if (FindFailedResponse.ABORT.equals(response)) {
      log(-1, "handleImageMissing: Response.ABORT: aborting");
      log(-1, "Did you want to find text? If yes, use text methods (see docs).");
      return null;
    }
    log(logLevel, "handleImageMissing: skip requested on %s", (recap ? "recapture " : "capture missing "));
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="017 handle FindFailed">
  protected FindFailedResponse handleFindFailed(Element what) {
    FindFailedResponse whatToDo = findFailedResponse;
    if (FindFailedResponse.HANDLE.equals(whatToDo)) {
      ObserveEvent evt = null;
      ObserveEvent.Type type = ObserveEvent.Type.FINDFAILED;
      if (findFailedHandler != null && ((ObserverCallBack) findFailedHandler).getType().equals(type)) {
        log(logLevel, "handleFindFailed: Response.HANDLE: calling handler");
        evt = new ObserveEvent("", type, what, this); //TODO handler parameter
        ((ObserverCallBack) findFailedHandler).findfailed(evt);
        whatToDo = evt.getResponse();
      }
    }
    if (FindFailedResponse.PROMPT.equals(whatToDo)) {
      int capture = 0;
      int retry = 1;
      int abort = 3;
      String message = "Folder: " + what.file().getParent() + "" +
          "\nWhere: " + this;
      String title = "Find failed for: " + what.file().getName();
      Integer response = SX.popGeneric(message, title, "Abort", new String[]{"Capture", "Retry", "Skip", "Abort"});
      if (response < 0) {
        response = abort;
      } else if (response == capture) { //TODO get IDE informed about recapture
        response = abort;
        if (what.captureImage()) {
          response = retry;
        }
      }
      whatToDo = (new FindFailedResponse[]
          {null, FindFailedResponse.RETRY, FindFailedResponse.SKIP, FindFailedResponse.ABORT})[response];
    }
    return whatToDo;
  }

  @Deprecated
  protected <PSI> Boolean handleFindFailed(PSI target, Image img) { //TODO make obsolete
    log(logLevel, "handleFindFailed: %s", target);
    Boolean state = null;
    ObserveEvent evt = null;
    FindFailedResponse response = findFailedResponse;
    if (FindFailedResponse.HANDLE.equals(response)) {
      ObserveEvent.Type type = ObserveEvent.Type.FINDFAILED;
      if (findFailedHandler != null && ((ObserverCallBack) findFailedHandler).getType().equals(type)) {
        log(logLevel, "handleFindFailed: Response.HANDLE: calling handler");
        evt = new ObserveEvent("", type, target, img, this, 0);
        ((ObserverCallBack) findFailedHandler).findfailed(evt);
        response = evt.getResponse();
      }
    }
    if (FindFailedResponse.ABORT.equals(response)) {
      state = null;
    } else if (FindFailedResponse.SKIP.equals(response)) {
      state = false;
    } else if (FindFailedResponse.RETRY.equals(response)) {
      state = true;
    }
    if (FindFailedResponse.PROMPT.equals(response)) {
      response = handleFindFailedShowDialog(img, false);
    } else {
      return state;
    }
    if (FindFailedResponse.ABORT.equals(response)) {
      state = null;
    } else if (FindFailedResponse.SKIP.equals(response)) {
      if (img.backup()) {
        img.delete();
        state = handleImageMissing(img, true); //hack: FindFailed-ReCapture
        if (state == null || !state) {
          if (!img.restore()) {
            state = null;
          } else {
            img.getBufferedImage();
          }
        }
      }
    } else if (FindFailedResponse.RETRY.equals(response)) {
      state = true;
    }
    return state;
  }

  @Deprecated
  protected FindFailedResponse handleFindFailedShowDialog(Element image, boolean shouldCapture) {
    FindFailedResponse response;
    FindFailedDialog fd = new FindFailedDialog(image, shouldCapture);
    fd.setVisible(true);
    response = fd.getResponse();
    fd.dispose();
    wait(0.5);
    return response;
  }

  private String hasBackup = "";

  boolean backup() {
    if (isValid()) {
      File fOrg = new File(url().getPath());
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

  boolean restore() {
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

  //TODO delete an Image??
  void delete() {
    //TODO File fImg = remove();
    //if (null != fImg) FileManager.deleteFileOrFolder(fImg);
  }
  //</editor-fold>

  //<editor-fold desc="020 find image one">

  /**
   * finds the given Pattern, String or Image in the Element and returns the best match.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target what (PSI) to find in this Region
   * @return If found, the element. null otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Match find(PSI target) throws FindFailed {
    return doFind(target);
  }

  /**
   * Check if target exists with a specified timeout.
   * <br>
   * timout = 0: returns immediately after first search,
   * does not raise FindFailed
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target, double timeout) {
    try {
      if (!isOnScreen() || timeout < 0.01) {
        return doFind(target);
      }
      return doFind(target, 0);
    } catch (FindFailed findFailed) {
      return null;
    }
  }

  /**
   * Check if target exists (with the default autoWaitTimeout),
   * does not raise FindFailed
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target) {
    return exists(target, getAutoWaitTimeout());
  }

  /**
   * Check if target exists.
   * <pre>
   * - does not raise FindFailed
   * - like exists(target, 0) but returns true/false
   * - which means only one search
   * - no wait for target to appear
   * - intended to be used in logical expressions
   * - use getLastMatch() to get the match if found
   * </pre>
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return true if found, false otherwise
   */
  public <PSI> boolean has(PSI target) {
    return null != exists(target, 0);
  }

  /**
   * Check if target appears within the specified time
   * <pre>
   * - does not raise FindFailed
   * - like exists(target, timeout) but returns true/false
   * - intended to be used in logical expressions
   * - use getLastMatch() to get the match if found
   * </pre>
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds
   * @return true if found, false otherwise
   */
  public <PSI> boolean has(PSI target, double timeout) {
    return null != exists(target, timeout);
  }

  /**
   * Waits for the Pattern, String or Image to appear until the AutoWaitTimeout value is exceeded.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target The target to search for
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target) throws FindFailed {
    if (target instanceof Float || target instanceof Double) {
      wait(0.0 + ((Double) target));
      return null;
    }
    return wait(target, getAutoWaitTimeout());
  }

  public <PSI> Match wait(PSI target, double timeout) throws FindFailed {
    if (!isOnScreen() || timeout < 0.01) {
      return doFind(target);
    }
    return doFind(target, timeout);
  }

  /**
   * waits until target vanishes or timeout (in seconds) is passed (AutoWaitTimeout)
   *
   * @param <PSI>  Pattern, String or Image
   * @param target The target to wait for it to vanish
   * @return true if the target vanishes, otherwise returns false.
   */
  public <PSI> boolean waitVanish(PSI target) {
    return waitVanish(target, getAutoWaitTimeout());
  }

  /**
   * waits until target vanishes or timeout (in seconds) is passed
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  Pattern, String or Image
   * @param timeout time in seconds
   * @return true if target vanishes, false otherwise and if imagefile is missing.
   */
  public <PSI> boolean waitVanish(PSI target, double timeout) {
    long before = new Date().getTime();
    try {
      doFind(target, 0);
    } catch (FindFailed findFailed) {
      return true;
    }
    waitAfterScan(before, before + (int) (timeout * 1000));
    long elapsed = new Date().getTime() - before;
    timeout = timeout - elapsed / 1000.0;
    if (timeout < 0) {
      return false;
    }
    Match match = null;
    try {
      match = doFind(target, -timeout);
    } catch (FindFailed findFailed) {
    }
    return match == null;
  }
  //</editor-fold>

  //<editor-fold desc="022 find image many">

  /**
   * finds all occurences of the given Pattern, String or Image in the region and returns an Iterator of Matches.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Matches findAll(PSI target) throws FindFailed {
    return doFindAll(target);
  }

  /**
   * like {@link #findAll(Object)} - but does not throw FindFailed and returns a list of matches
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching as list
   */
  public <PSI> List<Match> getAll(PSI target) {
    List<Match> mList = new ArrayList<>();
    try {
      mList = findAll(target).asList();
    } catch (FindFailed findFailed) {
    }
    return mList;
  }

  /**
   * like {@link #getAll(Object)} - but returns the list of matches sorted from left to right and top down (along rows).
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching row-wise as list, empty if nothing found
   */
  public <PSI> List<Match> findAllByRow(PSI target) {
    List<Match> mList = getAll(target);
    if (!mList.isEmpty()) {
      Collections.sort(mList, (m1, m2) -> {
        int xMid1 = m1.getCenter().x;
        int yMid1 = m1.getCenter().y;
        int yTop = yMid1 - m1.h / 2;
        int yBottom = yMid1 + m1.h / 2;
        int xMid2 = m2.getCenter().x;
        int yMid2 = m2.getCenter().y;
        if (yMid2 > yTop && yMid2 < yBottom) {
          if (xMid1 > xMid2) {
            return 1;
          }
        } else if (yMid2 < yTop) {
          return 1;
        }
        return -1;
      });
    }
    return mList;
  }

  /**
   * like {@link #getAll(Object)} - but returns a list of matches sorted top down and left to right (along columns).
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching column-wise as list, empty if nothing found
   */
  public <PSI> List<Match> findAllByColumn(PSI target) {
    List<Match> mList = getAll(target);
    if (!mList.isEmpty()) {
      Collections.sort(mList, (m1, m2) -> {
        int xMid1 = m1.getCenter().x;
        int yMid1 = m1.getCenter().y;
        int xLeft = xMid1 - m1.w / 2;
        int xRight = xMid1 + m1.w / 2;
        int xMid2 = m2.getCenter().x;
        int yMid2 = m2.getCenter().y;
        if (xMid2 > xLeft && xMid2 < xRight) {
          if (yMid1 > yMid2) {
            return 1;
          }
        } else if (xMid2 < xLeft) {
          return 1;
        }
        return -1;
      });
    }
    return mList;
  }

  /**
   * like {@link #getAll(Object)} - but returns the smallest rectangle containing all found matches
   *
   * @param target Pattern, String or Image
   * @param <PSI>  A search criteria
   * @return the resulting area or the search area if nothing is found
   */
  public <PSI> Match unionAll(PSI target) {
    List<Match> matches = getAll(target);
    if (matches.size() < 2) {
      return new Match(this);
    }
    Match theUnion = null;
    for (Match match : matches) {
      if (null == theUnion) {
        theUnion = match;
      } else {
        theUnion = theUnion.union(match);
      }
    }
    return theUnion;
  }

  public List<Match> findAny(Object... args) {
    Object[] targets = new Object[args.length];
    int nTarget = 0;
    for (Object arg : args) {
      targets[nTarget++] = arg;
    }
    List<Match> mList = dofindAny(targets);
    return mList;
  }

  public List<Match> findAnyList(List<Object> args) {
    Object[] targets = new Object[args.size()];
    int nTarget = 0;
    for (Object arg : args) {
      targets[nTarget++] = arg;
    }
    List<Match> mList = dofindAny(targets);
    return mList;
  }

  public Match unionAny(Object... targets) {
    if (targets.length < 2) {
      return new Match(this);
    }
    List<Object> pList = new ArrayList<>();
    pList.addAll(Arrays.asList(targets));
    return unionAnyList(pList);
  }

  public Match unionAnyList(List<Object> targets) {
    if (targets.size() < 2) {
      return new Match(this);
    }
    List<Match> matches = new ArrayList<>();
    matches = findAnyList(targets);
    if (matches.size() < 2) {
      return new Match(this);
    }
    Match theUnion = null;
    for (Match match : matches) {
      if (null == theUnion) {
        theUnion = match;
      } else {
        theUnion = theUnion.union(match);
      }
    }
    return theUnion;
  }

  public Match findBest(Object... args) {
    if (args.length == 0) {
      return null;
    }
    List<Object> pList = new ArrayList<>();
    pList.addAll(Arrays.asList(args));
    return findBestList(pList);
  }

  public Match findBestList(List<Object> pList) {
    Debug.log(logLevel, "findBest: enter");
    if (pList == null || pList.size() == 0) {
      return null;
    }
    Match mResult = null;
    List<Match> mList = findAnyList(pList);
    if (mList.size() > 0) {
      Collections.sort(mList, (m1, m2) -> {
        double ms = m2.score() - m1.score();
        if (ms < 0) {
          return -1;
        } else if (ms > 0) {
          return 1;
        }
        return 0;
      });
      mResult = mList.get(0);
    }
    return mResult;
  }

  public <SUFEBMP> List<Match> findChanges(SUFEBMP image) {
    List<Match> changes = new ArrayList<>();
    if (SX.isNotNull(image)) {
      Image changedImage = new Image(image);
      long start = new Date().getTime();
      changes = SXOpenCV.doFindChanges((Image) this, changedImage);
    }
    return changes;
  }
  //</editor-fold>

  //<editor-fold desc="029 find image private">
  private Match doFind(Object target) throws FindFailed {
    return doFind(target, 0, false);
  }

  private Match doFind(Object target, double timeout) throws FindFailed {
    return doFind(target, timeout, false);
  }

  private Match doFindAll(Object target) throws FindFailed {
    return doFind(target, 0, true);
  }

  private boolean ignoreLastSeen = false;

  public void ignoreLastSeen() {
    ignoreLastSeen = true;
  }

  private boolean isIgnoreLastSeen() {
    return ignoreLastSeen;
  }

  private Match doFind(Object target, double timeout, boolean findAll) throws FindFailed {
    if (!isValid()) {
      return null;
    }
    boolean isVanish = false;
    if (timeout < 0) {
      isVanish = true;
      timeout = -timeout;
    } else {
      resetLastMatch(findAll);
    }
    long startFind = new Date().getTime();
    Mat where = new Mat();
    if (!isOnScreen()) {
      //TODO handle masked base image
      if (getContent().channels() == 4) {
        List<Mat> mats = SXOpenCV.extractMask(getContent(), true);
        where = mats.get(0);
      } else {
        where = getImage().getContent();
      }
    }
    long whereTime = new Date().getTime() - startFind;
    long whatTime = 0;
    Match match;
    FindAttributes findAttributes;
    while (true) {
      long startSearch, startWhat;
      long searchTime = 0;
      Match matchResult;
      startWhat = new Date().getTime();
      findAttributes = new FindAttributes(target);
      whatTime = new Date().getTime() - startWhat;
      long before = new Date().getTime();
      long waitUntil = before + (int) (timeout * 1000);
      long startWhere;
      boolean firstSearch = true;
      long whereTimeLS = 0;
      long searchTimeLS = 0;
      while (true) {
        if (firstSearch && isOnScreen() && shouldCheckLastSeen() && !findAll && !isVanish) {
          trace("checkLastSeen: enter");
          Match lastSeenMatch = findAttributes.getMatchLastSeen();
          if (lastSeenMatch != null && lastSeenMatch.isInside(this)) {
            trace("checkLastSeen: start");
            Mat whereLastSeen;
            if ((w + h) < 1000) {
              long startWhereLS = new Date().getTime();
              where = getImage().getContent();
              Element regionLastSeen = lastSeenMatch.getRegion();
              whereLastSeen = SXOpenCV.getSubMat(this, where, regionLastSeen);
              whereTimeLS = new Date().getTime() - startWhereLS;
            } else {
              long startWhereLS = new Date().getTime();
              whereLastSeen = lastSeenMatch.getRegion().getImage().getContent();
              whereTimeLS = new Date().getTime() - startWhereLS;
            }
            long startSearchLS = new Date().getTime();
            matchResult = SXOpenCV.checkLastSeen(whereLastSeen, findAttributes);
            searchTimeLS = new Date().getTime() - startSearchLS;
            if (!isIgnoreLastSeen()) {
              if (matchResult != null) {
                matchResult.x = lastSeenMatch.x - this.x;
                matchResult.y = lastSeenMatch.y - this.y;
                trace("checkLastSeen: found: %s", matchResult);
                break;
              }
            } else {
              where = new Mat();
            }
          }
          trace("checkLastSeen: exit");
        }
        if (isOnScreen() && (!firstSearch || where.empty())) {
          startWhere = new Date().getTime();
          where = getImage().getContent();
          whereTime = new Date().getTime() - startWhere;
        }
        startSearch = new Date().getTime();
        matchResult = SXOpenCV.findMatch(where, findAttributes, findAll);
        searchTime = new Date().getTime() - startSearch;
        if (timeout < 0.01) {
          break;
        }
        if ((isVanish && matchResult == null) || (!isVanish && matchResult != null) || before > waitUntil) {
          break;
        }
        waitAfterScan(before, waitUntil);
        before = new Date().getTime();
        firstSearch = false;
      }
      long findTime = new Date().getTime() - startFind;
      long[] times = new long[]{findTime, searchTime + searchTimeLS, whereTime + whereTimeLS, whatTime};
      match = Match.createFromResult(this, findAttributes, matchResult, times);
      if (isVanish) {
        return match;
      }
      if (match == null) {
        FindFailedResponse response = handleFindFailed(findAttributes.originalTarget());
        if (FindFailedResponse.RETRY.equals(response)) {
          SX.popAsk("Make the screen ready for find retry." +
              "\n\nClick Yes when ready.\nClick No to abort.", "Retry after FindFailed");
          startFind = new Date().getTime();
          continue;
        }
        if (FindFailedResponse.ABORT.equals(response)) {
          throw new FindFailed(FindFailed.createErrorMessage(this, findAttributes.target()));
        }
      }
      break;
    }

    if (isOnScreen()) {
      if (!findAll && Settings.CheckLastSeen) {
        setMatchLastSeen(findAttributes.originalTarget(), match);
      }
      if (!findAll) {
        match(match);
      } else {
        matches(match);
      }
    }
    return match;
  }

  private List<Match> dofindAny(Object[] targets) {
    class FindSub implements Runnable {
      Element element;
      Object target;
      int nTarget;
      Match[] matches;
      Boolean finished = false;

      FindSub(Element element, Object target, int nTarget, Match[] matches) {
        this.element = element.getImage();
        this.target = target;
        this.nTarget = nTarget;
        this.matches = matches;
      }

      public boolean hasFinished() {
        return hasFinished(false);
      }

      public synchronized boolean hasFinished(boolean state) {
        if (state) {
          finished = true;
        }
        return finished;
      }

      @Override
      public void run() {
        try {
          Match match = element.find(target);
          matches[nTarget] = match;
          matches[nTarget].setIndex(nTarget);
        } catch (Exception ex) {
        }
        hasFinished(true);
      }
    }

    Match[] matches = new Match[targets.length];
    FindSub[] subs = new FindSub[targets.length];
    int nTarget = 0;
    Image screenImage = this.getImage();
    for (Object target : targets) {
      subs[nTarget] = new FindSub(screenImage, target, nTarget, matches);
      new Thread(subs[nTarget++]).start();
    }
    boolean all = false;
    while (!all) {
      all = true;
      for (FindSub sub : subs) {
        all &= sub.hasFinished();
      }
    }
    return Arrays.asList(matches);
  }

//TODO   private Match findInImage(ScreenImage base, Object target) throws IOException {
  /*
  private Match findInImage(ScreenImage base, Object target) throws IOException {
    Finder finder = null;
    Match match = null;
    boolean findingText = false;
    Image img = null;
    if (target instanceof String) {
      if (((String) target).startsWith("\t") && ((String) target).endsWith("\t")) {
        findingText = true;
      } else {
        img = Image.create((String) target);
        if (img.isValid()) {
          finder = doCheckLastSeenAndCreateFinder(base, img, 0.0, null);
          if (!finder.hasNext()) {
            runFinder(finder, img);
          }
        } else if (img.isText()) {
          findingText = true;
        } else {
          throw new IOException("Region: findInImage: Image not loadable: " + target.toString());
        }
      }
      if (findingText) {
        log(logLevel, "findInImage: Switching to TextSearch");
        finder = new Finder(getScreen().capture(x, y, w, h), this);
        finder.findText((String) target);
      }
    } else if (target instanceof Pattern) {
      if (((Pattern) target).isValid()) {
        img = ((Pattern) target).getImage();
        finder = doCheckLastSeenAndCreateFinder(base, img, 0.0, (Pattern) target);
        if (!finder.hasNext()) {
          runFinder(finder, target);
        }
      } else {
        throw new IOException("Region: findInImage: Image not loadable: " + target.toString());
      }
    } else if (target instanceof Image) {
      if (((Image) target).isValid()) {
        img = ((Image) target);
        finder = doCheckLastSeenAndCreateFinder(base, img, 0.0, null);
        if (!finder.hasNext()) {
          runFinder(finder, img);
        }
      } else {
        throw new IOException("Region: findInImage: Image not loadable: " + target.toString());
      }
    } else {
      log(-1, "findInImage: invalid parameter: %s", target);
      return null;
    }
    if (finder.hasNext()) {
      match = finder.next();
      //match.setImage(img);
      img.setLastSeen(match.getRect(), match.score());
    }
    return match;
  }
*/

//TODO   private List<Match> findAnyCollect(List<Object> pList) {
/*
  private List<Match> findAnyCollect(List<Object> pList) {
    List<Match> mList = new ArrayList<Match>();
    if (pList == null) {
      return mList;
    }
    Match[] mArray = new Match[pList.size()];
    Region.SubFindRun[] theSubs = new Region.SubFindRun[pList.size()];
    int nobj = 0;
    ScreenImage base = getScreen().capture(this);
    for (Object obj : pList) {
      mArray[nobj] = null;
      if (obj instanceof Pattern || obj instanceof String || obj instanceof Image) {
        theSubs[nobj] = new Region.SubFindRun(mArray, nobj, base, obj, this);
        new Thread(theSubs[nobj]).start();
      }
      nobj++;
    }
    Debug.log(logLevel, "findAnyCollect: waiting for SubFindRuns");
    nobj = 0;
    boolean all = false;
    while (!all) {
      all = true;
      for (Region.SubFindRun sub : theSubs) {
        all &= sub.hasFinished();
      }
    }
    Debug.log(logLevel, "findAnyCollect: SubFindRuns finished");
    nobj = 0;
    for (Match match : mArray) {
      if (match != null) {
        match.setIndex(nobj);
        mList.add(match);
      } else {
      }
      nobj++;
    }
    return mList;
  }
*/

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="030 OCR - read text, line, word, char">

  /**
   * tries to read the text in this region/image<br>
   * might contain misread characters, NL characters and
   * other stuff, when interpreting contained grafics as text<br>
   * Best results: one or more lines of text with no contained grafics
   *
   * @return the text read (utf8 encoded)
   */
  public String text() {
    return OCR.readText(getContent());
  }

  /**
   * get text from this region/image
   * supposing it is one line of text
   *
   * @return the text or empty string
   */
  public String textLine() {
    return OCR.readLine(this);
  }

  /**
   * get text from this region/image
   * supposing it is one word
   *
   * @return the text or empty string
   */
  public String textWord() {
    return OCR.readWord(this);
  }

  /**
   * get text from this region/image
   * supposing it is one character
   *
   * @return the text or empty string
   */
  public String textChar() {
    return OCR.readChar(this);
  }

  /**
   * find text lines in this region/image
   *
   * @return list of strings each representing one line of text
   */
  public List<String> textLines() {
    List<String> lines = new ArrayList<>();
    List<Match> matches = findLines();
    for (Match match : matches) {
      lines.add(match.getText());
    }
    return lines;
  }

  /**
   * find the words as text in this region/image (top left to bottom right)<br>
   * a word is a sequence of detected utf8-characters surrounded by significant background space
   * might contain characters misinterpreted from contained grafics
   *
   * @return list of strings each representing one word
   */
  public List<String> textWords() {
    List<String> words = new ArrayList<>();
    List<Match> matches = findWords();
    for (Match match : matches) {
      words.add(match.getText());
    }
    return words;
  }

  /**
   * Find all lines as text (top left to bottom right) in this {@link Region} or {@link Image}
   *
   * @return a list of text {@link Match}es or empty list if not found
   */
  public List<Match> findLines() {
    return relocate(OCR.readLines(this));
  }

  /**
   * Find all words as text (top left to bottom right)
   *
   * @return a list of text matches
   */
  public List<Match> findWords() {
    return relocate(OCR.readWords(this));
  }
  //</editor-fold>

  //<editor-fold desc="032 find text as word or line">

  /**
   * Find the first word as text (top left to bottom right) containing the given text
   *
   * @param word to be searched
   * @return a text match or null if not found
   */
  public Match findWord(String word) {
    Match match = null;
    if (!word.isEmpty()) {
      Object result = doFindText(word, levelWord, false);
      if (result != null) {
        match = relocate((Match) result);
      }
    }
    return match;
  }

  /**
   * Find all words as text (top left to bottom right) containing the given text
   *
   * @param word to be searched
   * @return a list of text matches
   */
  public List<Match> findWords(String word) {
    Finder finder = ((Finder) doFindText(word, levelWord, true));
    if (null != finder) {
      return finder.getListFor(this);
    }
    return new ArrayList<>();
  }

  /**
   * Find the first line as text (top left to bottom right) containing the given text
   *
   * @param text the line should contain
   * @return a text match or null if not found
   */
  public Match findLine(String text) {
    Match match = null;
    if (!text.isEmpty()) {
      Object result = doFindText(text, levelLine, false);
      if (result != null) {
        match = relocate((Match) result);
      }
    }
    return match;
  }

  /**
   * Find all lines as text (top left to bottom right) containing the given text
   *
   * @param text the lines should contain
   * @return a list of text matches or empty list if not found
   */
  public List<Match> findLines(String text) {
    Finder finder = (Finder) doFindText(text, levelLine, true);
    if (null != finder) {
      return finder.getListFor(this);
    }
    return new ArrayList<>();
  }

  private int levelWord = 3;
  private int levelLine = 2;

  private Object doFindText(String text, int level, boolean multi) {
    Object returnValue = null;
    Finder finder = new Finder(this);
    lastSearchTime = (new Date()).getTime();
    if (level == levelWord) {
      if (multi) {
        if (finder.findWords(text)) {
          returnValue = finder;
        }
      } else {
        if (finder.findWord(text)) {
          returnValue = finder.next();
        }
      }
    } else if (level == levelLine) {
      if (multi) {
        if (finder.findLines(text)) {
          returnValue = finder;
        }
      } else {
        if (finder.findLine(text)) {
          returnValue = finder.next();
        }
      }
    }
    return returnValue;
  }
  //</editor-fold>

  //<editor-fold desc="034 find text like find image">
  public Match findText(String text) throws FindFailed {
    //TODO implement findText
    throw new SikuliXception(String.format("Pixels: findText: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }

  public Match findT(String text) throws FindFailed {
    return findText(text);
  }

  public Match existsText(String text) {
    //TODO existsText: try: findText:true catch: false
    throw new SikuliXception(String.format("Pixels: existsText: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }

  public Match existsT(String text) {
    return existsText(text);
  }

  public boolean hasText(String text) {
    return null != existsText(text);
  }

  public boolean hasT(String text) {
    return hasText(text);
  }

  public List<Match> findAllText(String text) {
    List<Match> matches = new ArrayList<>();
    throw new SikuliXception(String.format("Pixels: findAllText: not implemented for", this.getClass().getCanonicalName()));
    //return matches;
  }

  public List<Match> findAllT(String text) {
    return findAllText(text);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="040 Mouse - low level">

  /**
   * press and hold the specified buttons - use + to combine Button.LEFT left mouse button Button.MIDDLE middle mouse
   * button Button.RIGHT right mouse button
   *
   * @param buttons spec
   */
  public void mouseDown(int buttons) {
    Mouse.down(buttons, this);
  }

  /**
   * release all currently held buttons
   */
  public void mouseUp() {
    Mouse.up(0, this);
  }

  /**
   * release the specified mouse buttons (see mouseDown) if buttons==0, all currently held buttons are released
   *
   * @param buttons spec
   */
  public void mouseUp(int buttons) {
    Mouse.up(buttons, this);
  }

  /**
   * move the mouse pointer to the region's last successful match.
   * <br>same as hover
   * <br>
   *
   * @return 1 if possible, 0 otherwise
   */
  public int mouseMove() {
    try { // needed to cut throw chain for FindFailed
      return mouseMove(match());
    } catch (FindFailed ex) {
    }
    return 0;
  }

  /**
   * move the mouse pointer to the given target location
   * <br> same as hover
   * <br> Pattern or Filename - do a find before and use the match
   * <br> Region - position at center
   * <br> Match - position at match's targetOffset
   * <br> Location - position at that point
   * <br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int mouseMove(PFRML target) throws FindFailed {
    int ret = 0;
    Location loc = getLocationFromTarget(target);
    if (null != loc) {
      ret = Mouse.move(loc, this);
    }
    return ret;
  }

  /**
   * move the mouse from the current position to the offset position given by the parameters
   *
   * @param xoff horizontal offset (&lt; 0 left, &gt; 0 right)
   * @param yoff vertical offset (&lt; 0 up, &gt; 0 down)
   * @return 1 if possible, 0 otherwise
   */
  public int mouseMove(int xoff, int yoff) {
    try {
      return mouseMove(Mouse.at().offset(xoff, yoff));
    } catch (Exception ex) {
      return 0;
    }
  }
  //</editor-fold>

  //<editor-fold desc="042 Mouse - click">

  /**
   * time in milliseconds to delay between button down/up at next click only (max 1000)
   *
   * @param millisecs value
   */
  public void delayClick(int millisecs) {
    Settings.ClickDelay = millisecs;
  }

  /**
   * move the mouse pointer to region's last successful match.
   * <br>use center if no lastMatch
   * <br>if region is a match: move to targetOffset
   * <br>same as mouseMove
   *
   * @return 1 if possible, 0 otherwise
   */
  public int hover() {
    return mouseMove();
  }

  /**
   * move the mouse pointer to the given target location.
   * <br>same as mouseMove
   * <br> Pattern or Filename - do a find before and use the match
   * <br> Region - position at center
   * <br> Match - position at match's targetOffset
   * <br> Location - position at that point
   * <br>
   *
   * @param <PFRML> to search: Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int hover(PFRML target) throws FindFailed {
    log(logLevel, "hover: " + target);
    return mouseMove(target);
  }

  /**
   * left click at the region's last successful match.
   * <br>use center if no lastMatch
   * <br>if region is a match: click targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int click() {
    try { // needed to cut throw chain for FindFailed
      return click(match(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * left click at the given target location.
   * <br> Pattern or Filename - do a find before and use the match
   * <br> Region - position at center
   * <br> Match - position at match's targetOffset
   * <br> Location - position at that point
   * <br>
   *
   * @param <PFRML> to search: Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int click(PFRML target) throws FindFailed {
    return click(target, 0);
  }

  /**
   * left click at the given target location.
   * <br> holding down the given modifier keys
   * <br> Pattern or Filename - do a find before and use the match
   * <br> Region - position at center
   * <br> Match - position at match's targetOffset
   * <br> Location - position at that point
   * <br>
   *
   * @param <PFRML>   to search: Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int click(PFRML target, Integer modifiers) throws FindFailed {
    int ret = 0;
    Location loc = getLocationFromTarget(target);
    if (null != loc) {
      ret = Mouse.click(loc, InputEvent.BUTTON1_DOWN_MASK, modifiers, false, this);
    }
    return ret;
  }
  //</editor-fold>

  //<editor-fold desc="090 helper private">
  protected List<Match> relocate(List<Match> matches) {
    return matches;
  }

  protected Match relocate(Match match) {
    return match;
  }
  //</editor-fold>

  //<editor-fold desc="099 deprecated features">

  /**
   * @return a list of matches
   * @see #findLines()
   * @deprecated use findLines() instead
   */
  public List<Match> collectLines() {
    return findLines();
  }

  /**
   * @return a list of lines as strings
   * @see #textLines()
   * @deprecated use textLines() instead
   */
  @Deprecated
  public List<String> collectLinesText() {
    return textLines();
  }

  /**
   * @return a list of matches
   * @see #findWords()
   * @deprecated use findWords() instead
   */
  @Deprecated
  public List<Match> collectWords() {
    return findWords();
  }

  /**
   * @return a list of words sa strings
   * @see #textWords()
   * @deprecated use textWords() instead
   */
  @Deprecated
  public List<String> collectWordsText() {
    return textWords();
  }
  //</editor-fold>
}
