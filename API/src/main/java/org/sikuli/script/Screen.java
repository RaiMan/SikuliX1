/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Date;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.*;
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

  private static IRobot globalRobot = null;
  protected static Screen[] screens = null;
  protected static int primaryScreen = -1;
  private static int waitForScreenshot = 300;
  protected int curID = -1;
  protected int oldID = 0;
  protected int monitor = -1;
  protected boolean waitPrompt;
  protected OverlayCapturePrompt prompt;
  private final static String promptMsg = "Select a region on the screen";
  public static boolean ignorePrimaryAtCapture = false;
  public ScreenImage lastScreenImage = null;
  private static boolean isActiveCapturePrompt = false;
  private static EventObserver captureObserver = null;
  private long lastCaptureTime = -1;

  static int nMonitors = 0;
  static Rectangle[] monitorBounds = null;
  static int mainMonitor = -1;

  public static Screen getDefaultInstance4py() {
    return new Screen();
  }

  //<editor-fold defaultstate="collapsed" desc="monitors">
  static GraphicsEnvironment genv = null;
  static GraphicsDevice[] gdevs;

  public static boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }

  private static boolean initMonitors() {
    if (!isHeadless()) {
      log(logLevel, "Accessing: GraphicsEnvironment.getLocalGraphicsEnvironment()");
      genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      log(logLevel, "Accessing: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()");
      gdevs = genv.getScreenDevices();
      nMonitors = gdevs.length;
      mainMonitor = -1;
      if (nMonitors == 0) {
        throw new SikuliXception(String.format("SikuliX: Init: GraphicsEnvironment has no ScreenDevices"));
      }
      monitorBounds = new Rectangle[nMonitors];
      Rectangle currentBounds;
      for (int i = 0; i < nMonitors; i++) {
        currentBounds = gdevs[i].getDefaultConfiguration().getBounds();
        if (currentBounds.contains(new Point(0, 0))) {
          if (mainMonitor < 0) {
            mainMonitor = i;
            log(logLevel, "ScreenDevice %d has (0,0) --- will be primary Screen(0)", i);
          } else {
            log(logLevel, "ScreenDevice %d too contains (0,0)!", i);
          }
        }
        log(logLevel, "Monitor %d: (%d, %d) %d x %d", i,
            currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height);
        monitorBounds[i] = currentBounds;
      }
      if (mainMonitor < 0) {
        log(logLevel, "No ScreenDevice has (0,0) --- using 0 as primary: %s", monitorBounds[0]);
        mainMonitor = 0;
      }
      return true;
    } else {
      throw new SikuliXception(String.format("SikuliX: Init: running in headless environment"));
    }
  }

  public static Rectangle getMonitor(int n) {
    if (primaryScreen < 0) {
      initScreens(false);
    }
    n = (n < 0 || n >= nMonitors) ? mainMonitor : n;
    return monitorBounds[n];
  }

  public static Rectangle hasPoint(Point aPoint) {
    if (primaryScreen < 0) {
      initScreens(false);
    }
    for (Rectangle rMon : monitorBounds) {
      if (rMon.contains(aPoint)) {
        return rMon;
      }
    }
    return null;
  }

  private static Boolean initScreensFirstTime = null;

  protected static void initScreens(boolean reset) {
    if (screens != null && !reset) {
      return;
    }
    if (null == initScreensFirstTime) {
      initScreensFirstTime = true;
    } else {
      if (initScreensFirstTime) {
        initScreensFirstTime = false;
        return;
      }
    }
    log(logLevel, "initScreens: starting");
    if (initMonitors()) {
      primaryScreen = 0;
      getGlobalRobot();
      screens = new Screen[nMonitors];
      screens[0] = new Screen(0, mainMonitor);
      screens[0].initScreen();
      int nMonitor = 0;
      for (int i = 1; i < screens.length; i++) {
        if (nMonitor == mainMonitor) {
          nMonitor++;
        }
        screens[i] = new Screen(i, nMonitor);
        screens[i].initScreen();
        nMonitor++;
      }

//TODO macOS: allow mouse/keyboard usage
      Mouse.init();

      log(logLevel, "initScreens: monitor mouse check");
      Location lnow = Mouse.at();
      float mmd = Settings.MoveMouseDelay;
      Settings.MoveMouseDelay = 0f;
      Location lc = null, lcn = null;
      for (Screen s : screens) {
        lc = s.getCenter();
        Mouse.move(lc);
        if (Mouse.isNotUseable()) {
          break;
        }
        lcn = Mouse.at();
        if (!lc.equals(lcn)) {
          log(logLevel, "*** multimonitor click check: %s center: (%d, %d) --- NOT OK:  (%d, %d)",
              s.toStringShort(), lc.x, lc.y, lcn.x, lcn.y);
        } else {
          log(logLevel, "*** checking: %s center: (%d, %d) --- OK", s.toStringShort(), lc.x, lc.y);
        }
      }
      if (!Mouse.isNotUseable()) {
        Mouse.move(lnow);
      }
      Settings.MoveMouseDelay = mmd;
    }
    if (Mouse.isNotUseable()) {
      RunTime runTime = RunTime.get();
      if (runTime.runType.equals(RunTime.Type.API) && Commons.runningMac()) {
        throw new SikuliXception("Mouse.init: Mouse not useable (blocked) - Screenshots might not work either!");
      }
    }
    log(logLevel, "initScreens: ending");
  }
  //</editor-fold>

  /**
   * Is the screen object having the top left corner as (0,0). If such a screen does not exist it is
   * the screen with id 0.
   */
  public Screen() {
    super();
    if (primaryScreen < 0) {
      initScreens(false);
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
      initScreens(false);
    }
    if (id < 0 || id >= nMonitors) {
      Debug.error("Screen(%d) not in valid range 0 to %d - using primary %d",
          id, nMonitors - 1, primaryScreen);
      curID = primaryScreen;
    } else {
      curID = id;
    }
    monitor = screens[curID].monitor;
    initScreen();
  }

  public static Screen make4py(ArrayList args) {
    Screen theScreen = new Screen();
    if (args.size() == 1 && args.get(0) instanceof Integer) {
      theScreen = new Screen((Integer) args.get(0));
    }
    return theScreen;
  }

  private void initScreen() {
    Rectangle bounds = getBounds();
    x = (int) bounds.getX();
    y = (int) bounds.getY();
    w = (int) bounds.getWidth();
    h = (int) bounds.getHeight();
  }

  // hack to get an additional internal constructor for the initialization
  private Screen(int id, int monitor) {
    super();
    curID = id;
    this.monitor = monitor;
  }

  public static Screen as(int id) {
    if (primaryScreen < 0) {
      initScreens(false);
    }
    if (id < 0 || id >= nMonitors) {
      Debug.error("Screen(%d) not in valid range 0 to %d - using primary %d",
          id, nMonitors - 1, primaryScreen);
      return screens[0];
    } else {
      return screens[id];
    }
  }

  protected static IRobot getGlobalRobot() {
    if (globalRobot == null) {
      try {
        globalRobot = new RobotDesktop();
      } catch (AWTException e) {
        throw new RuntimeException(String.format("SikuliX: Screen: getGlobalRobot: %s", e.getMessage()));
      }
    }
    return globalRobot;
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
      initScreens(false);
    }
    Debug.logp("*** monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.logp("*** Primary is Screen %d", primaryScreen);
    for (int i = 0; i < nMonitors; i++) {
      Debug.logp("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.logp("*** end monitor configuration ***");
  }

  /**
   * re-initialize the monitor setup (e.g. when it was changed while running)
   */
  public static void resetMonitors() {
    Debug.error("*** BE AWARE: experimental - might not work ***");
    Debug.error("Re-evaluation of the monitor setup has been requested");
    Debug.error("... Current Region/Screen objects might not be valid any longer");
    Debug.error("... Use existing Region/Screen objects only if you know what you are doing!");
    initScreens(true);
    Debug.logp("*** new monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.logp("*** Primary is Screen %d", primaryScreen);
    for (int i = 0; i < nMonitors; i++) {
      Debug.logp("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.error("*** end new monitor configuration ***");
  }

  public static void resetMonitorsQuiet() {
    initScreens(true);
  }

  private static int getValidMonitor(int id) {
    if (id < 0 || id >= nMonitors) {
      Debug.error("Screen: invalid screen id %d - using primary screen", id);
      return mainMonitor;
    }
    return screens[id].monitor;
  }

  /**
   * @return number of available screens
   */
  public static int getNumberScreens() {
    if (primaryScreen < 0) {
      initScreens(false);
    }
    return nMonitors;
  }

  /**
   * @return the id of the screen at (0,0), if not exists 0
   */
  public static int getPrimaryId() {
    if (primaryScreen < 0) {
      initScreens(false);
    }
    return primaryScreen;
  }

  /**
   * @return the screen at (0,0), if not exists the one with id 0
   */
  public static Screen getPrimaryScreen() {
    if (primaryScreen < 0) {
      initScreens(false);
    }
    return screens[primaryScreen];
  }

  /**
   * @param id of the screen
   * @return the screen with given id, the primary screen if id is invalid
   */
  public static Screen getScreen(int id) {
    if (primaryScreen < 0) {
      initScreens(false);
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
    if (isHeadless()) {
      return new Rectangle();
    }
    return new Rectangle(getMonitor(getValidMonitor(curID)));
  }

  /**
   * @param id of the screen
   * @return the physical coordinate/size <br>as AWT.Rectangle to avoid mix up with getROI
   */
  public static Rectangle getBounds(int id) {
    if (isHeadless()) {
      return new Rectangle();
    }
    return new Rectangle(getMonitor(getValidMonitor(id)));
  }

  /**
   * each screen has exactly one robot (internally used for screen capturing)
   * <br>available as a convenience for those who know what they are doing. Should not be needed
   * normally.
   *
   * @param id of the screen
   * @return the AWT.Robot of the given screen, if id invalid the primary screen
   */
  public static IRobot getRobot(int id) {
    return getScreen(id).getRobot();
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
    return getGlobalRobot();
  }

  protected static IRobot getRobot(Region reg) {
    if (reg == null || null == reg.getScreen()) {
      return getPrimaryScreen().getGlobalRobot();
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
    if (shot != null) {
      shot.getFile();
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
    ScreenImage simg = globalRobot.captureScreen(rect);
    if (Settings.FindProfiling) {
      Debug.logp("[FindProfiling] Screen.capture [%d x %d]: %d msec",
          rect.width, rect.height, new Date().getTime() - lastCaptureTime);
    }
    lastScreenImage = simg;
    if (Debug.getDebugLevel() > logLevel) {
      simg.saveLastScreenImage(RunTime.get().fSikulixStore);
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

  public static void doPrompt(String message, EventObserver obs) {
    captureObserver = obs;
    Screen.getPrimaryScreen().userCapture(message);
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
    Debug.log(3, "TRACE: Screen: userCapture");
    waitPrompt = true;
    Thread th = new Thread() {
      @Override
      public void run() {
        String msg = message.isEmpty() ? promptMsg : message;
        for (int is = 0; is < Screen.getNumberScreens(); is++) {
          if (ignorePrimaryAtCapture && is == 0) {
            continue;
          }
          Screen.getScreen(is).prompt = new OverlayCapturePrompt(Screen.getScreen(is));
          Screen.getScreen(is).prompt.addObserver(captureObserver);
          Screen.getScreen(is).prompt.prompt(msg);
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
      for (int is = 0; is < Screen.getNumberScreens(); is++) {
        OverlayCapturePrompt ocp = Screen.getScreen(is).prompt;
        if (ocp == null) {
          continue;
        }
        if (ocp.isComplete()) {
          closePrompt(Screen.getScreen(is));
          simg = ocp.getSelection();
          if (simg != null) {
            Screen.getScreen(is).lastScreenImage = simg;
          }
          ocp.close();
          Screen.getScreen(is).prompt = null;
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
}
