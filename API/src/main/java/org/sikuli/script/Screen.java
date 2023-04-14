/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.*;
import org.sikuli.script.support.devices.Device;
import org.sikuli.script.support.devices.ScreenDevice;
import org.sikuli.script.support.gui.SXDialog;
import org.sikuli.util.EventObserver;
import org.sikuli.util.OverlayCapturePrompt;

/**
 * A screen represents a physical monitor with its coordinates and size according to the global
 * point system: the screen areas are grouped around a point (0,0) like in a cartesian system (the
 * top left corner and the points contained in the screen area might have negative x and/or y values)
 * <br >The screens are arranged in an array (index = id) and each screen is always the same object
 * (not possible to create new objects).
 * <br>A screen inherits from class Region, so it can be used as such in all aspects. If you need
 * the region of the screen more than once, you have to create new ones based on the screen.
 * <br>The so called primary screen is the one with top left (0,0) and has id 0.
 */
public class Screen extends Region implements IScreen {

  protected static final String logName = "Screen: ";

  private static Screen[] screens = null;
  private static int primaryScreen = -1;
  private static int waitForScreenshot = 300;
  private static boolean isActiveCapturePrompt = false;
  private static EventObserver captureObserver = null;

  protected int curID = -1;
  protected int oldID = 0;

  private final static String promptMsg = "Select a region on the screen";
  public static boolean ignorePrimaryAtCapture = false;
  protected boolean waitPrompt;
  protected OverlayCapturePrompt prompt;
  public ScreenImage lastScreenImage = null;
  private long lastCaptureTime = -1;

  public static Screen getDefaultInstance4py() {
    return new Screen();
  }

  //<editor-fold defaultstate="collapsed" desc="monitors">
  static int nMonitors = 0;

  protected static void initScreens() {
    if (screens != null) {
      return;
    }
    log(logLevel + 1, "initScreens: starting");
    if (!ScreenDevice.isHeadless()) {
      nMonitors = ScreenDevice.numDevices();
      primaryScreen = 0;
      screens = new Screen[nMonitors];
      for (int i = 0; i < screens.length; i++) {
        screens[i] = ScreenDevice.makeScreen(i);
        screens[i].getRobot();
      }
    }   else {
      throw new SikuliXception(String.format("SikuliX: Init: running in headless environment"));
    }
    log(logLevel + 1, "initScreens: ending");
  }

  public static void resetScreens() {
    screens = null;
    initScreens();
  }

  public void setup(int num) {
    final Rectangle rect = ScreenDevice.get(num).asRectangle();
    x = rect.x;
    y = rect.y;
    w = rect.width;
    h = rect.height;
    curID = num;
  }
  //</editor-fold>

  /**
   * Is the screen object having the top left corner as (0,0). If such a screen does not exist it is
   * the screen with id 0.
   */
  public Screen() {
    super();
    if (primaryScreen < 0) {
      initScreens();
    }
    curID = primaryScreen;
    initScreen();
  }

  /**
   * The screen object with the given id
   *
   * @param id valid screen number
   */
  public Screen(int id) {
    super();
    if (primaryScreen < 0) {
      initScreens();
    }
    if (id < 0 || id >= nMonitors) {
      Debug.error("Screen(%d) not in valid range 0 to %d - using primary %d",
          id, nMonitors - 1, primaryScreen);
      curID = primaryScreen;
    } else {
      curID = id;
    }
    initScreen();
  }

  public static Screen make4py(ArrayList args) {
    Screen theScreen = new Screen();
    if (args.size() == 1 && args.get(0) instanceof Integer) {
      theScreen = new Screen((Integer) args.get(0));
    }
    return theScreen;
  }

  public void reset() {
    setup(curID);
  }

  private void initScreen() {
    Rectangle bounds = getBounds();
    x = (int) bounds.getX();
    y = (int) bounds.getY();
    w = (int) bounds.getWidth();
    h = (int) bounds.getHeight();
  }

  public static Screen as(int id) {
    if (primaryScreen < 0) {
      initScreens();
    }
    if (id < 0 || id >= nMonitors) {
      Debug.error("Screen(%d) not in valid range 0 to %d - using primary %d",
          id, nMonitors - 1, primaryScreen);
      return screens[0];
    } else {
      return screens[id];
    }
  }

  /**
   * create a Screen (ScreenUnion) object as a united region of all available monitors
   *
   * @return ScreenUnion
   */
  public static ScreenUnion all() {
    return new ScreenUnion();
  }

  /**
   * INTERNAL USE
   * collect all physical screens to one big region<br>
   * TODO: under evaluation, wether it really makes sense
   *
   * @param isScreenUnion true/false
   */
  public Screen(boolean isScreenUnion) {
    super(isScreenUnion);
  }

  /**
   * INTERNAL USE
   * collect all physical screens to one big region<br>
   * This is under evaluation, wether it really makes sense
   */
  public void setAsScreenUnion() {
    oldID = curID;
    curID = -1;
  }

  /**
   * INTERNAL USE
   * reset from being a screen union to the screen used before
   */
  public void setAsScreen() {
    curID = oldID;
  }

  /**
   * {@inheritDoc}
   *
   * @return Screen
   */
  @Override
  public Screen getScreen() {
    return this;
  }

  /**
   * Should not be used - makes no sense for Screen object
   *
   * @param s Screen
   * @return returns a new Region with the screen's location/dimension
   */
  @Override
  protected Region setScreen(IScreen s) {
    return new Region(getBounds());
  }

  /**
   * show the current monitor setup
   */
  public static void showMonitors() {
    if (primaryScreen < 0) {
      initScreens();
    }
    Debug.print("*** monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.print("*** Primary is Screen %d", primaryScreen);
    for (int i = 0; i < nMonitors; i++) {
      Debug.print("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.print("*** end monitor configuration ***");
  }

  /**
   * re-initialize the monitor setup (e.g. when it was changed while running)
   */
  public static void resetMonitors() {
    Debug.error("*** BE AWARE: experimental - might not work ***");
    Debug.error("Re-evaluation of the monitor setup has been requested");
    Debug.error("... Current Region/Screen objects might not be valid any longer");
    Debug.error("... Use existing Region/Screen objects only if you know what you are doing!");
    resetScreens();
    Debug.print("*** new monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.print("*** Primary is Screen %d", primaryScreen);
    for (int i = 0; i < nMonitors; i++) {
      Debug.print("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.error("*** end new monitor configuration ***");
  }

  public static void resetMonitorsQuiet() {
    resetScreens();
  }

  /**
   * @return number of available screens
   */
  public static int getNumberScreens() {
    if (primaryScreen < 0) {
      initScreens();
    }
    return nMonitors;
  }

  /**
   * @return the id of the screen at (0,0), if not exists 0
   */
  public static int getPrimaryId() {
    if (primaryScreen < 0) {
      initScreens();
    }
    return primaryScreen;
  }

  /**
   * @return the screen at (0,0), if not exists the one with id 0
   */
  public static Screen getPrimaryScreen() {
    if (primaryScreen < 0) {
      initScreens();
    }
    return screens[primaryScreen];
  }

  /**
   * @param id of the screen
   * @return the screen with given id, the primary screen if id is invalid
   */
  public static Screen getScreen(int id) {
    if (primaryScreen < 0) {
      initScreens();
    }
    if (id < 0 || id >= nMonitors) {
      Debug.error("Screen: invalid screen id %d - using primary screen", id);
      id = primaryScreen;
    }
    return screens[id];
  }

  /**
   * @return the screen's rectangle
   */
  @Override
  public Rectangle getBounds() {
    return ScreenDevice.get(curID).asRectangle();
  }

  /**
   * @param id of the screen
   * @return the physical coordinate/size <br>as AWT.Rectangle to avoid mix up with getROI
   */
  public static Rectangle getBounds(int id) {
    return ScreenDevice.get(id).asRectangle();
  }

  /**
   * @return the id
   */
  @Override
  public int getID() {
    return curID;
  }

  public String getIDString() {
    return "" + getID();
  }


  /**
   * INTERNAL USE: to be compatible with ScreenUnion
   *
   * @param x value
   * @param y value
   * @return id of the screen
   */
  @Override
  public int getIdFromPoint(int x, int y) {
    return curID;
  }

  /**
   * Gets the Robot of this Screen.
   *
   * @return The Robot for this Screen
   */
  @Override
  public IRobot getRobot() {
    return ScreenDevice.getRobot(getID());
  }

  protected static IRobot getRobot(Region reg) {
    if (reg == null || null == reg.getScreen()) {
      return getPrimaryScreen().getRobot();
    } else {
      return reg.getScreen().getRobot();
    }
  }

  /**
   * creates a region on the current screen with the given coordinate/size. The coordinate is
   * translated to the current screen from its relative position on the screen it would have been
   * created normally.
   *
   * @param loc    Location
   * @param width  value
   * @param height value
   * @return the new region
   */
  public Region newRegion(Location loc, int width, int height) {
    return Region.create(loc.copyTo(this), width, height);
  }

  @Override
  public Region newRegion(Region reg) {
    return copyTo(this);
  }

  @Override
  public Region newRegion(int x, int y, int w, int h) {
    return newRegion(new Location(x, y), w, h);
  }

  /**
   * creates a location on the current screen with the given point. The coordinate is translated to
   * the current screen from its relative position on the screen it would have been created
   * normally.
   *
   * @param loc Location
   * @return the new location
   */
  public Location newLocation(Location loc) {
    return (new Location(loc)).copyTo(this);
  }

  @Override
  public ScreenImage getLastScreenImageFromScreen() {
    return lastScreenImage;
  }

  //<editor-fold defaultstate="collapsed" desc="Capture - SelectRegion">

  public ScreenImage cmdCapture(Object... args) {
    ScreenImage shot = doCapture(args);
    if (shot != null) {
      shot.getFile();
    }
    return shot;
  }

  public ScreenImage doCapture(Object... args) {
    ScreenImage shot = null;
    if (args.length == 0) {
      shot = userCapture("capture an image");
    } else {
      Object arg0 = args[0];
      if (args.length == 1) {
        if (arg0 instanceof String) {
          if (((String) arg0).isEmpty()) {
            shot = capture();
          } else {
            shot = userCapture((String) arg0);
          }
        } else if (arg0 instanceof Region) {
          shot = capture((Region) arg0);
        } else if (arg0 instanceof Rectangle) {
          shot = capture((Rectangle) arg0);
        } else {
          shot = capture();
        }
      } else if (args.length > 1 && args.length < 4) {
        Object arg1 = args[1];
        String path = "";
        String name = "";
        if ((arg0 instanceof Region || arg0 instanceof String || arg0 instanceof Rectangle) && arg1 instanceof String) {
          if (args.length == 3) {
            Object arg2 = args[2];
            if (arg2 instanceof String) {
              name = (String) arg2;
              path = (String) arg1;
            }
          } else {
            name = (String) arg1;
          }
          if (!name.isEmpty()) {
            if (arg0 instanceof Region) {
              shot = capture((Region) arg0);
            } else if (arg0 instanceof Rectangle) {
              shot = capture((Rectangle) arg0);
            } else {
              shot = userCapture((String) arg0);
            }
            if (shot != null) {
              if (!path.isEmpty()) {
                shot.getFile(path, name);
              } else {
                shot.saveInBundle(name);
              }
            }
            return shot;
          }
        }
        Debug.error("Screen: capture: Invalid parameters");
      } else if (args.length == 4) {
        Integer argInt = null;
        for (Object arg : args) {
          argInt = null;
          try {
            argInt = (Integer) arg;
          } catch (Exception ex) {
            break;
          }
        }
        if (argInt != null) {
          shot = capture((int) args[0], (int) args[1], (int) args[2], (int) args[3]);
        }
      } else {
        Debug.error("Screen: capture: Invalid parameters");
      }
    }
    return shot;
  }

  /**
   * create a ScreenImage with the physical bounds of this screen
   *
   * @return the image
   */
  @Override
  public ScreenImage capture() {
    return capture(getRect());
  }

  /**
   * create a ScreenImage with given coordinates on this screen.
   *
   * @param x x-coordinate of the region to be captured
   * @param y y-coordinate of the region to be captured
   * @param w width of the region to be captured
   * @param h height of the region to be captured
   * @return the image of the region
   */
  @Override
  public ScreenImage capture(int x, int y, int w, int h) {
    Rectangle rect = newRegion(new Location(x, y), w, h).getRect();
    return capture(rect);
  }

  /**
   * create a ScreenImage with given rectangle on this screen.
   *
   * @param rect The Rectangle to be captured
   * @return the image of the region
   */
  @Override
  public ScreenImage capture(Rectangle rect) {
    lastCaptureTime = new Date().getTime();
    ScreenImage simg = getRobot().captureScreen(rect);
    if (Settings.FindProfiling) {
      Debug.print("[FindProfiling] Screen.capture [%d x %d]: %d msec",
          rect.width, rect.height, new Date().getTime() - lastCaptureTime);
    }
    lastScreenImage = simg;
    if (Debug.getDebugLevel() > 2) {
      simg.saveLastScreenImage(Commons.getAppDataStore(), curID);
    }
    return simg;
  }

  /**
   * create a ScreenImage with given region on this screen
   *
   * @param reg The Region to be captured
   * @return the image of the region
   */
  @Override
  public ScreenImage capture(Region reg) {
    return capture(reg.getRect());
  }

  public static boolean doPrompt(String message, EventObserver obs) {
    if (Device.isCaptureBlocked()) { //TODO interactive capture
      Commons.terminate(999, "Capture is blocked");
      return false;
    }
    captureObserver = obs;
    Screen.getPrimaryScreen().userCapture(message);
    return true;
  }

  public static void closePrompt() {
    for (int is = 0; is < Screen.getNumberScreens(); is++) {
      if (!Screen.getScreen(is).hasPrompt()) {
        continue;
      }
      Screen.getScreen(is).prompt.close();
    }
  }

  public static void closePrompt(Screen scr) {
    for (int is = 0; is < Screen.getNumberScreens(); is++) {
      if (Screen.getScreen(is).getID() == scr.getID() ||
          !Screen.getScreen(is).hasPrompt()) {
        continue;
      }
      Screen.getScreen(is).prompt.close();
      Screen.getScreen(is).prompt = null;
    }
  }

  private static synchronized boolean setActiveCapturePrompt() {
    if (isActiveCapturePrompt) {
      return false;
    }
    Debug.log(3, "TRACE: Screen: setActiveCapturePrompt");
    isActiveCapturePrompt = true;
    return true;
  }

  private static synchronized void resetActiveCapturePrompt() {
    Debug.log(3, "TRACE: Screen: resetActiveCapturePrompt");
    isActiveCapturePrompt = false;
    captureObserver = null;
  }

  public static void resetPrompt(OverlayCapturePrompt ocp) {
    int scrID = ocp.getScrID();
    if (scrID > -1) {
      Screen.getScreen(scrID).prompt = null;
    }
    resetActiveCapturePrompt();
  }

  public boolean hasPrompt() {
    return prompt != null;
  }

  /**
   * interactive capture with predefined message: lets the user capture a screen image using the
   * mouse to draw the rectangle
   *
   * @return the image
   */
  public ScreenImage userCapture() {
    return userCapture("");
  }

  /**
   * interactive capture with given message: lets the user capture a screen image using the mouse to
   * draw the rectangle
   *
   * @param message text
   * @return the image
   */
  @Override
  public ScreenImage userCapture(final String message) {
    if (!setActiveCapturePrompt()) {
      return null;
    }
    if (Device.isCaptureBlocked()) { //TODO userCapture
      Commons.terminate(999, "Capture is blocked");
    }
    Debug.log(3, "TRACE: Screen: userCapture");
    waitPrompt = true;
    final int numScr = Screen.getNumberScreens();
    Screen[] screens = new Screen[numScr];
    for (int is = 0; is < numScr; is++) {
      screens[is] = Screen.getScreen(is);
    }
    Thread th = new Thread() {
      @Override
      public void run() {
        String msg = message.isEmpty() ? promptMsg : message;
        OverlayCapturePrompt[] prompts = new OverlayCapturePrompt[Screen.getNumberScreens()];
        for (int is = 0; is < numScr; is++) {
          Screen scr = screens[is];
          prompts[is] = scr.prompt = new OverlayCapturePrompt(scr);
          prompts[is].addObserver(captureObserver);
        }
        for (int is = 0; is < numScr; is++) {
          prompts[is].prompt(msg);
        }
      }
    };
    th.start();
    if (captureObserver != null) {
      return null;
    }
    boolean isComplete = false;
    ScreenImage simg = null;
    int count = 0;
    while (!isComplete) {
      this.wait(0.1f);
      if (count++ > waitForScreenshot) {
        break;
      }
      for (int is = 0; is < numScr; is++) {
        OverlayCapturePrompt ocp = screens[is].prompt;
        if (ocp == null) {
          continue;
        }
        if (ocp.isComplete()) {
          closePrompt(screens[is]);
          simg = ocp.getSelection();
          if (simg != null) {
            screens[is].lastScreenImage = simg;
          }
          ocp.close();
          screens[is].prompt = null;
          isComplete = true;
        }
      }
    }
    resetActiveCapturePrompt();
    return simg;
  }

  /**
   * interactive region create with predefined message: lets the user draw the rectangle using the
   * mouse
   *
   * @return the region
   */
  public Region selectRegion() {
    return selectRegion("Select a region on the screen");
  }

  /**
   * interactive region create with given message: lets the user draw the rectangle using the mouse
   *
   * @param message text
   * @return the region
   */
  public Region selectRegion(final String message) {
    Debug.log(3, "TRACE: Screen: selectRegion");
    ScreenImage sim = userCapture(message);
    if (sim == null) {
      return null;
    }
    Rectangle r = sim.getROI();
    return Region.create((int) r.getX(), (int) r.getY(),
        (int) r.getWidth(), (int) r.getHeight());
  }
  //</editor-fold>

  @Override
  public Region setOther(Region element) {
    return element.setOtherScreen(this);
  }

  @Override
  public Location setOther(Location element) {
    return element.setOtherScreen(this);
  }

  @Override
  public Location newLocation(int x, int y) {
    return new Location(x, y).setOtherScreen(this);
  }

  @Override
  public void waitAfterAction() {

  }
  @Override
  public Object action(String action, Object... args) {
    return null;
  }

  /**
   * see {@link #showImage(String, long, long)}
   * @param imgName
   * @return dialog object
   */
  public SXDialog showImage(String imgName) {
    return showImage(imgName, 0, (long) getAutoWaitTimeout());
  }

  /**
   * see {@link #showImage(String, long, long)}
   * @param imgName
   * @param when
   * @return dialog object
   */
  public SXDialog showImage(String imgName, final long when) {
    return showImage(imgName, when, (long) getAutoWaitTimeout());
  }

  /**
   * show an image on screen<br>
   * useable to test find actions and related features
   * @param imgName absolute or relative filename of image
   * @param when seconds to wait until popup
   * @param time seconds for how long to show
   * @return the shown object, which might be used to remove it from screen
   * @see #unshowImage(SXDialog)
   */
  public SXDialog showImage(String imgName, final long when, final long time) {
    SXDialog sxDialogImage = null;
    URL url = ImagePath.find(imgName);
    if (null != url && "file".equals(url.getProtocol())) {
      Image img = Image.from(url);
      if (img == null) {
        Debug.error("Image not possible: %s", url);
        return null;
      }
      Location where = getCenter().above(img.h / 2).left(img.w / 2);
      String imgPath = Commons.getValidImageFilename(url.getPath());
      sxDialogImage = new SXDialog("#image; file:" + imgPath,
          new Point(where.x, where.y), SXDialog.POSITION.TOPLEFT);
    } else {
      Debug.error("Image not found in FileSystem: %s", imgName);
      return null;
    }
    SXDialog.onScreen(sxDialogImage, when, time);
    return sxDialogImage;
  }

  /**
   * remove an image from screen shown before using {@link #showImage(String)}
   * @param sxDialogImage the image to remove
   */
  public void unshowImage(SXDialog sxDialogImage) {
    if (null == sxDialogImage) {
      return;
    }
    sxDialogImage.dispose();
  }
}
