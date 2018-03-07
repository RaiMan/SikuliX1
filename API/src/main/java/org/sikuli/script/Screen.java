/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.*;
import java.util.Date;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.util.EventObserver;
import org.sikuli.util.OverlayCapturePrompt;
import org.sikuli.util.ScreenHighlighter;

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

  static RunTime runTime = RunTime.get();

  private static String me = "Screen: ";
  private static int lvl = 3;
  private static Region fakeRegion;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static IRobot globalRobot = null;
  protected static Screen[] screens = null;
  protected static int primaryScreen = -1;
  private static int waitForScreenshot = 300;
  protected IRobot robot = null;
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

  //<editor-fold defaultstate="collapsed" desc="Initialization">

  static {
    RunTime.loadLibrary("VisionProxy");
    initScreens(false);
  }

  private long lastCaptureTime = -1;

//  private static void initScreens() {
//    initScreens(false);
//  }

  public int getcurrentID() {
    return curID;
  }

  private static void initScreens(boolean reset) {
    if (screens != null && !reset) {
      return;
    }
    log(lvl + 1, "initScreens: entry");
    primaryScreen = 0;
    setMouseRobot();
    if (null == globalRobot) {
      screens = new Screen[1];
      screens[0] = null;
    } else {
      screens = new Screen[runTime.nMonitors];
      screens[0] = new Screen(0, runTime.mainMonitor);
      screens[0].initScreen();
      int nMonitor = 0;
      for (int i = 1; i < screens.length; i++) {
        if (nMonitor == runTime.mainMonitor) {
          nMonitor++;
        }
        screens[i] = new Screen(i, nMonitor);
        screens[i].initScreen();
        nMonitor++;
      }
      Mouse.init();
      if (getNumberScreens() > 1) {
        log(lvl, "initScreens: multi monitor mouse check");
        Location lnow = Mouse.at();
        float mmd = Settings.MoveMouseDelay;
        Settings.MoveMouseDelay = 0f;
        Location lc = null, lcn = null;
        for (Screen s : screens) {
          lc = s.getCenter();
          Mouse.move(lc);
          lcn = Mouse.at();
          if (!lc.equals(lcn)) {
            log(lvl, "*** multimonitor click check: %s center: (%d, %d) --- NOT OK:  (%d, %d)",
                    s.toStringShort(), lc.x, lc.y, lcn.x, lcn.y);
          } else {
            log(lvl, "*** checking: %s center: (%d, %d) --- OK", s.toStringShort(), lc.x, lc.y);
          }
        }
        Mouse.move(lnow);
        Settings.MoveMouseDelay = mmd;
      }
    }
  }

  public static IRobot getGlobalRobot() {
    return globalRobot;
  }

  private static void setMouseRobot() {
    try {
      if (globalRobot == null && !GraphicsEnvironment.isHeadless()) {
        globalRobot = new RobotDesktop();
      }
    } catch (AWTException e) {
      Debug.error("Can't initialize global Robot for Mouse: " + e.getMessage());
    }
  }

  private IRobot getMouseRobot() {
    setMouseRobot();
    if (null == globalRobot && !GraphicsEnvironment.isHeadless()) {
      log(-1, "problem getting a java.awt.Robot");
      Sikulix.endError(999);
    }
    return globalRobot;
  }

  protected static Region getFakeRegion() {
    if (fakeRegion == null) {
      fakeRegion = new Region(0, 0, 5, 5);
    }
    return fakeRegion;
  }

  /**
   * create a Screen (ScreenUnion) object as a united region of all available monitors
   *
   * @return ScreenUnion
   */
  public static ScreenUnion all() {
    return new ScreenUnion();
  }

  // hack to get an additional internal constructor for the initialization
  private Screen(int id, boolean init) {
    super();
    curID = id;
  }

  // hack to get an additional internal constructor for the initialization
  private Screen(int id, int monitor) {
    super();
    curID = id;
    this.monitor = monitor;
  }

  public static Screen as(int id) {
    if (id < 0 || id >= runTime.nMonitors) {
      Debug.error("Screen(%d) not in valid range 0 to %d - using primary %d",
              id, runTime.nMonitors - 1, primaryScreen);
      return screens[0];
    } else {
      return screens[id];
    }
  }

  /**
   * The screen object with the given id
   *
   * @param id valid screen number
   */
  public Screen(int id) {
    super();
    if (id < 0 || id >= runTime.nMonitors) {
      Debug.error("Screen(%d) not in valid range 0 to %d - using primary %d",
              id, runTime.nMonitors - 1, primaryScreen);
      curID = primaryScreen;
    } else {
      curID = id;
    }
    monitor = screens[curID].monitor;
    initScreen();
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
   * Is the screen object having the top left corner as (0,0). If such a screen does not exist it is
   * the screen with id 0.
   */
  public Screen() {
    super();
    curID = primaryScreen;
    initScreen();
  }

  //TODO: remove this method if it is not needed
  public void initScreen(Screen scr) {
    updateSelf();
  }

  private void initScreen() {
    Rectangle bounds = getBounds();
    x = (int) bounds.getX();
    y = (int) bounds.getY();
    w = (int) bounds.getWidth();
    h = (int) bounds.getHeight();
//    try {
//      robot = new RobotDesktop(this);
//      robot.setAutoDelay(10);
//    } catch (AWTException e) {
//      Debug.error("Can't initialize Java Robot on Screen " + curID + ": " + e.getMessage());
//      robot = null;
//    }
    robot = globalRobot;
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
   * Should not be used - throws UnsupportedOperationException
   *
   * @param s Screen
   * @return should not return
   */
  @Override
  protected Region setScreen(IScreen s) {
    throw new UnsupportedOperationException("The setScreen() method cannot be called from a Screen object.");
  }

  /**
   * show the current monitor setup
   */
  public static void showMonitors() {
//    initScreens();
    Debug.logp("*** monitor configuration [ %s Screen(s)] ***", Screen.getNumberScreens());
    Debug.logp("*** Primary is Screen %d", primaryScreen);
    for (int i = 0; i < runTime.nMonitors; i++) {
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
    for (int i = 0; i < runTime.nMonitors; i++) {
      Debug.logp("Screen %d: %s", i, Screen.getScreen(i).toStringShort());
    }
    Debug.error("*** end new monitor configuration ***");
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getters setters">
  protected boolean useFullscreen() {
    return false;
  }

  private static int getValidID(int id) {
    if (id < 0 || id >= runTime.nMonitors) {
      Debug.error("Screen: invalid screen id %d - using primary screen", id);
      return primaryScreen;
    }
    return id;
  }

  private static int getValidMonitor(int id) {
    if (id < 0 || id >= runTime.nMonitors) {
      Debug.error("Screen: invalid screen id %d - using primary screen", id);
      return runTime.mainMonitor;
    }
    return screens[id].monitor;
  }

  /**
   * @return number of available screens
   */
  public static int getNumberScreens() {
    return runTime.nMonitors;
  }

  /**
   * @return the id of the screen at (0,0), if not exists 0
   */
  public static int getPrimaryId() {
    return primaryScreen;
  }

  /**
   * @return the screen at (0,0), if not exists the one with id 0
   */
  public static Screen getPrimaryScreen() {
    return screens[primaryScreen];
  }

  /**
   * @param id of the screen
   * @return the screen with given id, the primary screen if id is invalid
   */
  public static Screen getScreen(int id) {
    return screens[getValidID(id)];
  }

  /**
   * @return the screen's rectangle
   */
  @Override
  public Rectangle getBounds() {
    return new Rectangle(runTime.getMonitor(monitor));
  }

  /**
   * @param id of the screen
   * @return the physical coordinate/size <br>as AWT.Rectangle to avoid mix up with getROI
   */
  public static Rectangle getBounds(int id) {
    return new Rectangle(runTime.getMonitor(getValidMonitor(id)));
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
    return getMouseRobot();
  }

  protected static IRobot getRobot(Region reg) {
    if (reg == null || null == reg.getScreen()) {
      return getPrimaryScreen().getMouseRobot();
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
    return null;
  }

  @Override
  public ScreenImage getLastScreenImageFromScreen() {
    return lastScreenImage;
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
  public Region newRegion(int x, int y, int w, int h) {
    return null;
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Capture - SelectRegion">

  public ScreenImage cmdCapture(Object... args) {
    if (args.length == 0) {
      return userCapture("capture an image");
    }
    Object arg0 = args[0];
    if (args.length == 1) {
      if (arg0 instanceof String) {
        return userCapture((String) arg0);
      } else if (arg0 instanceof Region) {
        return capture((Region) arg0);
      } else if (arg0 instanceof Rectangle) {
        return capture((Rectangle) arg0);
      }
    } else if (args.length > 1 && args.length < 4) {
      Object arg1 = args[1];
      String path = "";
      String name = "";
      if ((arg0 instanceof Region || arg0 instanceof String) && arg1 instanceof String) {
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
          ScreenImage shot = null;
          if (arg0 instanceof Region) {
            shot = capture((Region) arg0);
          } else {
            shot = userCapture((String) arg0);
          }
          if (shot != null) {
            if (!path.isEmpty()) {
              shot.getFile(path, name);
            } else {
              shot.saveInBundle(name);
            }
            return shot;
          } else {
            return null;
          }
        }
      }
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
        return capture((int) args[0], (int) args[1], (int) args[2], (int) args[3]);
      }
    }
    return userCapture("Invalid parameter for capture");
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

  public ScreenImage captureforHighlight(int x, int y, int w, int h) {
    return robot.captureScreen(new Rectangle(x, y, w, h));
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
    ScreenImage simg = robot.captureScreen(rect);
    if (Settings.FindProfiling) {
      Debug.logp("[FindProfiling] Screen.capture [%d x %d]: %d msec",
              rect.width, rect.height, new Date().getTime() - lastCaptureTime);
    }
    lastScreenImage = simg;
    if (Debug.getDebugLevel() > lvl) {
      simg.saveLastScreenImage(runTime.fSikulixStore);
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

  public String saveCapture(String name) {
    return saveCapture(name, null);
  }

  public String saveCapture(String name, Region reg) {
    ScreenImage simg;
    if (reg == null) {
      simg = userCapture("Capture for image " + name);
    } else {
      simg = capture(reg);
    }
    if (simg == null) {
      return null;
    } else {
      return simg.saveInBundle(name);
    }
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

  //<editor-fold defaultstate="collapsed" desc="Visual effects">
  @Override
  public void showTarget(Location loc) {
    showTarget(loc, Settings.SlowMotionDelay);
  }

  protected void showTarget(Location loc, double secs) {
    if (Settings.isShowActions()) {
      ScreenHighlighter overlay = new ScreenHighlighter(this, null);
      overlay.showTarget(loc, (float) secs);
    }
  }
  //</editor-fold>

  @Override
  public String toString() {
    Rectangle r = getBounds();
    String scrText = curID == -1 ? "Union" : "" + curID;
    return String.format("S(%s)[%d,%d %dx%d] E:%s, T:%.1f",
            scrText, (int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight(),
            getThrowException() ? "Y" : "N", getAutoWaitTimeout());
  }

  /**
   * only a short version of toString()
   *
   * @return like S(0) [0,0, 1440x900]
   */
  @Override
  public String toStringShort() {
    Rectangle r = getBounds();
    String scrText = curID == -1 ? "Union" : "" + curID;
    return String.format("S(%s)[%d,%d %dx%d]",
            scrText, (int) r.getX(), (int) r.getY(),
            (int) r.getWidth(), (int) r.getHeight());
  }

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
