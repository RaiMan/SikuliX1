/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;

import org.sikuli.android.ADBDevice;
import org.sikuli.android.ADBScreen;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.util.ScreenHighlighter;

/**
 * A Region is a rectengular area and lies always completely inside its parent screen
 */
public class Region {

  static RunTime runTime = RunTime.get();

  private static String me = "Region: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  //<editor-fold desc="housekeeping">
  /**
   * The Screen containing the Region
   */
  private IScreen scr;
  protected boolean otherScreen = false;

  /**
   * The ScreenHighlighter for this Region
   */
  private ScreenHighlighter overlay = null;
  /**
   * X-coordinate of the Region
   */
  public int x;
  /**
   * Y-coordinate of the Region
   */
  public int y;
  /**
   * Width of the Region
   */
  public int w;
  /**
   * Height of the Region
   */
  public int h;
  /**
   * Setting, how to react if an image is not found {@link FindFailed}
   */
  private FindFailedResponse findFailedResponse = FindFailed.defaultFindFailedResponse;
  private Object findFailedHandler = FindFailed.getFindFailedHandler();
  private Object imageMissingHandler = FindFailed.getImageMissingHandler();
  /**
   * Setting {@link Settings}, if exception is thrown if an image is not found
   */
  private boolean throwException = Settings.ThrowException;
  /**
   * Default time to wait for an image {@link Settings}
   */
  private double autoWaitTimeout = Settings.AutoWaitTimeout;
  private float waitScanRate = Settings.WaitScanRate;
  /**
   * Flag, if an observer is running on this region {@link Settings}
   */
  private boolean observing = false;
  private boolean observingInBackground = false;
  private float observeScanRate = Settings.ObserveScanRate;
  private int repeatWaitTime = Settings.RepeatWaitTime;
  /**
   * The {@link Observer} Singleton instance
   */
  private Observer regionObserver = null;

  /**
   * The last found {@link Match} in the Region
   */
  private Match lastMatch = null;
  /**
   * The last found {@link Match}es in the Region
   */
  private Iterator<Match> lastMatches = null;
  private long lastSearchTime = -1;
  private long lastFindTime = -1;
  private boolean isScreenUnion = false;
  private boolean isVirtual = false;
  private long lastSearchTimeRepeat = -1;

  /**
   * {@inheritDoc}
   *
   * @return the description
   */
  @Override
  public String toString() {
    String scrText = getScreen() == null ? "?" :
            "" + (-1 == getScreen().getID() ? "Union" : "" + getScreen().getID());
    return String.format("R[%d,%d %dx%d]@S(%s) E:%s, T:%.1f",
            x, y, w, h, scrText,
            throwException ? "Y" : "N", autoWaitTimeout);
  }

  /**
   * INTERNAL USE ONLY
   *
   * @return text
   */
  public String getIDString() {
    return "NonLocal";
  }

  /**
   * @return a compact description
   */
  public String toStringShort() {
    if (isOtherScreen()) {
      return String.format("%s, %dx%d", getScreen().getIDString(), w, h);
    } else {
      String scrText = getScreen() == null ? "?" :
              "" + (-1 == getScreen().getID() ? "Union" : getScreen().getID());
      return String.format("R[%d,%d %dx%d]@S(%s)", x, y, w, h, scrText);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="OFF: Specials for scripting environment">
  /*
   public Object __enter__() {
   Debug.error("Region: with(__enter__): Trying to make it a Jython Region for with: usage");
   IScriptRunner runner = Settings.getScriptRunner("jython", null, null);
   if (runner != null) {
   Object[] jyreg = new Object[]{this};
   if (runner.doSomethingSpecial("createRegionForWith", jyreg)) {
   if (jyreg[0] != null) {
   return jyreg[0];
   }
   }
   }
   Debug.error("Region: with(__enter__): Sorry, not possible");
   return null;
   }

   public void __exit__(Object type, Object value, Object traceback) {
   Debug.error("Region: with(__exit__): Sorry, not a Jython Region and not posssible!");
   }
   */
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Initialization">

  /**
   * INTERNAL USE
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
        this.scr = iscr;
        this.otherScreen = true;
        return;
      }
      if (iscr.getID() > -1) {
        rect = regionOnScreen(iscr);
        if (rect != null) {
          x = rect.x;
          y = rect.y;
          w = rect.width;
          h = rect.height;
          this.scr = iscr;
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

    if (scr == null || !scr.isOtherScreen()) {
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
      rect = regionOnScreen(scr);
      if (rect != null) {
        if (rect.width * rect.height > screenRect.width * screenRect.height) {
          screenRect = rect;
          screenOn = scr;
        }
      }
    }

    if (screenOn != null) {
      x = screenRect.x;
      y = screenRect.y;
      w = screenRect.width;
      h = screenRect.height;
      this.scr = screenOn;
    } else {
      // no screen found
      this.scr = null;
      Debug.error("Region(%d,%d,%d,%d) outside any screen - subsequent actions might not work as expected", x, y, w, h);
    }
  }

  private Location checkAndSetRemote(Location loc) {
    if (!isOtherScreen()) {
      return loc;
    }
    return loc.setOtherScreen(scr);
  }

  /**
   * INTERNAL USE - EXPERIMENTAL if true: this region is not bound to any screen
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
    reg.scr = Screen.getPrimaryScreen();
    return reg;
  }

  /**
   * INTERNAL USE - EXPERIMENTAL if true: this region is not bound to any screen
   *
   * @return the current state
   */
  public boolean isVirtual() {
    return isVirtual;
  }

  /**
   * INTERNAL USE - EXPERIMENTAL
   *
   * @param state if true: this region is not bound to any screen
   */
  public void setVirtual(boolean state) {
    isVirtual = state;
  }

  /**
   * INTERNAL USE: checks wether this region belongs to a non-Desktop screen
   *
   * @return true/false
   */
  public boolean isOtherScreen() {
    return otherScreen;
  }

  /**
   * INTERNAL USE: flags this region as belonging to a non-Desktop screen
   */
  public void setOtherScreen() {
    otherScreen = true;
  }

  /**
   * INTERNAL USE: flags this region as belonging to a non-Desktop screen
   *
   * @param aScreen screen
   */
  public Region setOtherScreen(IScreen aScreen) {
    scr = aScreen;
    setOtherScreen();
    return this;
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
   * Check wether thie Region is contained by any of the available screens
   *
   * @return true if yes, false otherwise
   */
  public boolean isValid() {
    if (this instanceof Screen) {
      return true;
    }
    return scr != null && w != 0 && h != 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructors to be used with Jython">

  /**
   * Create a region with the provided coordinate / size and screen
   *
   * @param X            X position
   * @param Y            Y position
   * @param W            width
   * @param H            heigth
   * @param screenNumber The number of the screen containing the Region
   */
  public Region(int X, int Y, int W, int H, int screenNumber) {
    this(X, Y, W, H, Screen.getScreen(screenNumber));
    this.rows = 0;
  }

  /**
   * Create a region with the provided coordinate / size and screen
   *
   * @param X            X position
   * @param Y            Y position
   * @param W            width
   * @param H            heigth
   * @param parentScreen the screen containing the Region
   */
  public Region(int X, int Y, int W, int H, IScreen parentScreen) {
    this.rows = 0;
    this.x = X;
    this.y = Y;
    this.w = W > 1 ? W : 1;
    this.h = H > 1 ? H : 1;
    initScreen(parentScreen);
  }

  /**
   * Convenience: a minimal Region to be used as a Point (backport from Version 2)<br>
   * is always on primary screen
   *
   * @param X
   * @param Y
   */
  public Region(int X, int Y) {
    this(X, Y, 1, 1, null);
  }

  /**
   * Create a region with the provided coordinate / size
   *
   * @param X X position
   * @param Y Y position
   * @param W width
   * @param H heigth
   */
  public Region(int X, int Y, int W, int H) {
    this(X, Y, W, H, null);
    this.rows = 0;
    log(lvl, "init: (%d, %d, %d, %d)", X, Y, W, H);
  }

  /**
   * Create a region from a Rectangle
   *
   * @param r the Rectangle
   */
  public Region(Rectangle r) {
    this(r.x, r.y, r.width, r.height, null);
    this.rows = 0;
  }

  /**
   * Create a new region from another region<br>including the region's settings
   *
   * @param r the region
   */
  public Region(Region r) {
    init(r);
  }

  private void init(Region r) {
    if (!r.isValid()) {
      return;
    }
    x = r.x;
    y = r.y;
    w = r.w;
    h = r.h;
    scr = r.getScreen();
    otherScreen = r.isOtherScreen();
    rows = 0;
    autoWaitTimeout = r.autoWaitTimeout;
    findFailedResponse = r.findFailedResponse;
    throwException = r.throwException;
    waitScanRate = r.waitScanRate;
    observeScanRate = r.observeScanRate;
    repeatWaitTime = r.repeatWaitTime;
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Quasi-Constructors to be used in Java">

  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region() {
    this.rows = 0;
  }

  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region(boolean isScreenUnion) {
    this.isScreenUnion = isScreenUnion;
    this.rows = 0;
  }

  /**
   * Create a region with the provided top left corner and size
   *
   * @param X top left X position
   * @param Y top left Y position
   * @param W width
   * @param H heigth
   * @return then new region
   */
  public static Region create(int X, int Y, int W, int H) {
    return Region.create(X, Y, W, H, null);
  }

  /**
   * Create a region with the provided top left corner and size
   *
   * @param X   top left X position
   * @param Y   top left Y position
   * @param W   width
   * @param H   heigth
   * @param scr the source screen
   * @return the new region
   */
  private static Region create(int X, int Y, int W, int H, IScreen scr) {
    return new Region(X, Y, W, H, scr);
  }

  /**
   * Create a region with the provided top left corner and size
   *
   * @param loc top left corner
   * @param w   width
   * @param h   height
   * @return then new region
   */
  public static Region create(Location loc, int w, int h) {
    int _x = loc.x;
    int _y = loc.y;
    IScreen s = loc.getScreen();
    if (s == null) {
      _x = _y = 0;
      s = Screen.getPrimaryScreen();
    }
    return Region.create(_x, _y, w, h, s);
  }

  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the left corner of
   * the new Region.
   */
  public final static int CREATE_X_DIRECTION_LEFT = 0;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the right corner of
   * the new Region.
   */
  public final static int CREATE_X_DIRECTION_RIGHT = 1;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the top corner of the
   * new Region.
   */
  public final static int CREATE_Y_DIRECTION_TOP = 0;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the bottom corner of
   * the new Region.
   */
  public final static int CREATE_Y_DIRECTION_BOTTOM = 1;

  /**
   * create a region with a corner at the given point<br>as specified with x y<br> 0 0 top left<br> 0 1 bottom left<br>
   * 1 0 top right<br> 1 1 bottom right<br>
   *
   * @param loc                the refence point
   * @param create_x_direction == 0 is left side !=0 is right side
   * @param create_y_direction == 0 is top side !=0 is bottom side
   * @param w                  the width
   * @param h                  the height
   * @return the new region
   */
  public static Region create(Location loc, int create_x_direction, int create_y_direction, int w, int h) {
    int _x = loc.x;
    int _y = loc.y;
    IScreen s = loc.getScreen();
    if (s == null) {
      _x = _y = 0;
      s = Screen.getPrimaryScreen();
    }
    int X;
    int Y;
    int W = w;
    int H = h;
    if (create_x_direction == CREATE_X_DIRECTION_LEFT) {
      if (create_y_direction == CREATE_Y_DIRECTION_TOP) {
        X = _x;
        Y = _y;
      } else {
        X = _x;
        Y = _y - h;
      }
    } else {
      if (create_y_direction == CREATE_Y_DIRECTION_TOP) {
        X = _x - w;
        Y = _y;
      } else {
        X = _x - w;
        Y = _y - h;
      }
    }
    return Region.create(X, Y, W, H, s);
  }

  /**
   * create a region with a corner at the given point<br>as specified with x y<br> 0 0 top left<br> 0 1 bottom left<br>
   * 1 0 top right<br> 1 1 bottom right<br>same as the corresponding create method, here to be naming compatible with
   * class Location
   *
   * @param loc the refence point
   * @param x   ==0 is left side !=0 is right side
   * @param y   ==0 is top side !=0 is bottom side
   * @param w   the width
   * @param h   the height
   * @return the new region
   */
  public static Region grow(Location loc, int x, int y, int w, int h) {
    return Region.create(loc, x, y, w, h);
  }

  /**
   * Create a region from a Rectangle
   *
   * @param r the Rectangle
   * @return the new region
   */
  public static Region create(Rectangle r) {
    return Region.create(r.x, r.y, r.width, r.height, null);
  }

  /**
   * Create a region from a Rectangle on a given Screen
   *
   * @param r            the Rectangle
   * @param parentScreen the new parent screen
   * @return the new region
   */
  protected static Region create(Rectangle r, IScreen parentScreen) {
    return Region.create(r.x, r.y, r.width, r.height, parentScreen);
  }

  /**
   * Create a region from another region<br>including the region's settings
   *
   * @param r the region
   * @return then new region
   */
  public static Region create(Region r) {
    Region reg = Region.create(r.x, r.y, r.w, r.h, r.getScreen());
    reg.autoWaitTimeout = r.autoWaitTimeout;
    reg.findFailedResponse = r.findFailedResponse;
    reg.throwException = r.throwException;
    return reg;
  }

  /**
   * create a region with the given point as center and the given size
   *
   * @param loc the center point
   * @param w   the width
   * @param h   the height
   * @return the new region
   */
  public static Region grow(Location loc, int w, int h) {
    int _x = loc.x;
    int _y = loc.y;
    IScreen s = loc.getScreen();
    if (s == null) {
      _x = _y = 0;
      s = Screen.getPrimaryScreen();
    }
    int X = _x - (int) w / 2;
    int Y = _y - (int) h / 2;
    return Region.create(X, Y, w, h, s);
  }

  /**
   * create a minimal region at given point with size 1 x 1
   *
   * @param loc the point
   * @return the new region
   */
  public static Region grow(Location loc) {
    int _x = loc.x;
    int _y = loc.y;
    IScreen s = loc.getScreen();
    if (s == null) {
      _x = _y = 0;
      s = Screen.getPrimaryScreen();
    }
    return Region.create(_x, _y, 1, 1, s);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="handle coordinates">

  /**
   * check if current region contains given point
   *
   * @param point Point
   * @return true/false
   */
  public boolean contains(Location point) {
    return getRect().contains(point.x, point.y);
  }

  /**
   * check if mouse pointer is inside current region
   *
   * @return true/false
   */
  public boolean containsMouse() {
    return contains(Mouse.at());
  }

  /**
   * new region with same offset to current screen's top left on given screen
   *
   * @param scrID number of screen
   * @return new region
   */
  public Region copyTo(int scrID) {
    return copyTo(Screen.getScreen(scrID));
  }

  /**
   * new region with same offset to current screen's top left on given screen
   *
   * @param screen new parent screen
   * @return new region
   */
  public Region copyTo(IScreen screen) {
    Location o = new Location(getScreen().getBounds().getLocation());
    Location n = new Location(screen.getBounds().getLocation());
    return Region.create(n.x + x - o.x, n.y + y - o.y, w, h, screen);
  }

  /**
   * used in Observer.callChangeObserving, Finder.next to adjust region relative coordinates of matches to screen
   * coordinates
   *
   * @param m
   * @return the modified match
   */
  protected Match toGlobalCoord(Match m) {
    m.x += x;
    m.y += y;
    return m;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="handle Settings">
  //TODO should be possible to reset to current global value resetXXX()

  /**
   * true - (initial setting) should throw exception FindFailed if findX unsuccessful in this region<br> false - do not
   * abort script on FindFailed (might leed to null pointer exceptions later)
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
   * current setting for this region (see setThrowException)
   *
   * @return true/false
   */
  public boolean getThrowException() {
    return throwException;
  }

  /**
   * the time in seconds a find operation should wait for the appearence of the target in this region<br> initial value
   * is the global AutoWaitTimeout setting at time of Region creation
   *
   * @param sec seconds
   */
  public void setAutoWaitTimeout(double sec) {
    autoWaitTimeout = sec;
  }

  /**
   * current setting for this region (see setAutoWaitTimeout)
   *
   * @return value of seconds
   */
  public double getAutoWaitTimeout() {
    return autoWaitTimeout;
  }

  /**
   * FindFailedResponse.<br>
   * ABORT - (initial value) abort script on FindFailed (= setThrowException(true) )<br>
   * SKIP - ignore FindFailed (same as setThrowException(false) )<br>
   * PROMPT - display prompt on FindFailed to let user decide how to proceed<br>
   * RETRY - continue to wait for appearence after FindFailed (caution: endless loop)
   *
   * @param response the FindFailedResponse
   */
  public void setFindFailedResponse(FindFailedResponse response) {
    findFailedResponse = response;
  }

  public void setFindFailedHandler(Object handler) {
    findFailedHandler = setHandler(handler, ObserveEvent.Type.FINDFAILED);
    log(lvl, "Setting FindFailedHandler");
  }

  public void setImageMissingHandler(Object handler) {
    imageMissingHandler = setHandler(handler, ObserveEvent.Type.MISSING);
    log(lvl, "Setting ImageMissingHandler");
  }

  private Object setHandler(Object handler, ObserveEvent.Type type) {
    findFailedResponse = FindFailedResponse.HANDLE;
    if (handler != null && (handler.getClass().getName().contains("org.python")
            || handler.getClass().getName().contains("org.jruby"))) {
      handler = new ObserverCallBack(handler, type);
    } else {
      ((ObserverCallBack) handler).setType(type);
    }
    return handler;
  }

  /**
   * @return the current setting (see setFindFailedResponse)
   */
  public FindFailedResponse getFindFailedResponse() {
    return findFailedResponse;
  }

  /**
   * @return the regions current WaitScanRate
   */
  public float getWaitScanRate() {
    return waitScanRate;
  }

  /**
   * set the regions individual WaitScanRate
   *
   * @param waitScanRate decimal number
   */
  public void setWaitScanRate(float waitScanRate) {
    this.waitScanRate = waitScanRate;
  }

  /**
   * @return the regions current ObserveScanRate
   */
  public float getObserveScanRate() {
    return observeScanRate;
  }

  /**
   * set the regions individual ObserveScanRate
   *
   * @param observeScanRate decimal number
   */
  public void setObserveScanRate(float observeScanRate) {
    this.observeScanRate = observeScanRate;
  }

  /**
   * INTERNAL USE: Observe
   *
   * @return the regions current RepeatWaitTime time in seconds
   */
  public int getRepeatWaitTime() {
    return repeatWaitTime;
  }

  /**
   * INTERNAL USE: Observe set the regions individual WaitForVanish
   *
   * @param time in seconds
   */
  public void setRepeatWaitTime(int time) {
    repeatWaitTime = time;
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getters / setters / modificators">

  /**
   * @return the Screen object containing the region
   */
  public IScreen getScreen() {
    return scr;
  }

  // to avoid NPE for Regions being outside any screen
  private IRobot getRobotForRegion() {
    if (getScreen() == null || isScreenUnion) {
      return Screen.getPrimaryScreen().getRobot();
    }
    return getScreen().getRobot();
  }

  /**
   * @return the screen, that contains the top left corner of the region. Returns primary screen if outside of any
   * screen.
   * @deprecated Only for compatibility, to get the screen containing this region, use {@link #getScreen()}
   */
  @Deprecated
  public IScreen getScreenContaining() {
    return getScreen();
  }

  /**
   * Sets a new Screen for this region.
   *
   * @param scr the containing screen object
   * @return the region itself
   */
  protected Region setScreen(IScreen scr) {
    initScreen(scr);
    return this;
  }

  /**
   * Sets a new Screen for this region.
   *
   * @param id the containing screen object's id
   * @return the region itself
   */
  protected Region setScreen(int id) {
    return setScreen(Screen.getScreen(id));
  }

  /**
   * synonym for showMonitors
   */
  public void showScreens() {
    Screen.showMonitors();
  }

  /**
   * synonym for resetMonitors
   */
  public void resetScreens() {
    Screen.resetMonitors();
  }

  // ************************************************

  /**
   * @return the center pixel location of the region
   */
  public Location getCenter() {
    return checkAndSetRemote(new Location(getX() + getW() / 2, getY() + getH() / 2));
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
   * Moves the region to the area, whose center is the given location
   *
   * @param loc the location which is the new center of the region
   * @return the region itself
   */
  public Region setCenter(Location loc) {
    Location c = getCenter();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(null);
    return this;
  }

  /**
   * @return top left corner Location
   */
  public Location getTopLeft() {
    return checkAndSetRemote(new Location(x, y));
  }

  /**
   * Moves the region to the area, whose top left corner is the given location
   *
   * @param loc the location which is the new top left point of the region
   * @return the region itself
   */
  public Region setTopLeft(Location loc) {
    return setLocation(loc);
  }

  /**
   * @return top right corner Location
   */
  public Location getTopRight() {
    return checkAndSetRemote(new Location(x + w - 1, y));
  }

  /**
   * Moves the region to the area, whose top right corner is the given location
   *
   * @param loc the location which is the new top right point of the region
   * @return the region itself
   */
  public Region setTopRight(Location loc) {
    Location c = getTopRight();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(null);
    return this;
  }

  /**
   * @return bottom left corner Location
   */
  public Location getBottomLeft() {
    return checkAndSetRemote(new Location(x, y + h - 1));
  }

  /**
   * Moves the region to the area, whose bottom left corner is the given location
   *
   * @param loc the location which is the new bottom left point of the region
   * @return the region itself
   */
  public Region setBottomLeft(Location loc) {
    Location c = getBottomLeft();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(null);
    return this;
  }

  /**
   * @return bottom right corner Location
   */
  public Location getBottomRight() {
    return checkAndSetRemote(new Location(x + w - 1, y + h - 1));
  }

  /**
   * Moves the region to the area, whose bottom right corner is the given location
   *
   * @param loc the location which is the new bottom right point of the region
   * @return the region itself
   */
  public Region setBottomRight(Location loc) {
    Location c = getBottomRight();
    x = x - c.x + loc.x;
    y = y - c.y + loc.y;
    initScreen(null);
    return this;
  }

  // ************************************************

  /**
   * @return x of top left corner
   */
  public int getX() {
    return x;
  }

  /**
   * @return y of top left corner
   */
  public int getY() {
    return y;
  }

  /**
   * @return width of region
   */
  public int getW() {
    return w;
  }

  /**
   * @return height of region
   */
  public int getH() {
    return h;
  }

  /**
   * @param X new x position of top left corner
   */
  public void setX(int X) {
    x = X;
    initScreen(null);
  }

  /**
   * @param Y new y position of top left corner
   */
  public void setY(int Y) {
    y = Y;
    initScreen(null);
  }

  /**
   * @param W new width
   */
  public void setW(int W) {
    w = W > 1 ? W : 1;
    initScreen(null);
  }

  /**
   * @param H new height
   */
  public void setH(int H) {
    h = H > 1 ? H : 1;
    initScreen(null);
  }

  // ************************************************

  /**
   * @param W new width
   * @param H new height
   * @return the region itself
   */
  public Region setSize(int W, int H) {
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(null);
    return this;
  }

  /**
   * @return the AWT Rectangle of the region
   */
  public Rectangle getRect() {
    return new Rectangle(x, y, w, h);
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param r the AWT Rectangle to use for position/size
   * @return the region itself
   */
  public Region setRect(Rectangle r) {
    return setRect(r.x, r.y, r.width, r.height);
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param X new x of top left corner
   * @param Y new y of top left corner
   * @param W new width
   * @param H new height
   * @return the region itself
   */
  public Region setRect(int X, int Y, int W, int H) {
    x = X;
    y = Y;
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(null);
    return this;
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param r the region to use for position/size
   * @return the region itself
   */
  public Region setRect(Region r) {
    return setRect(r.x, r.y, r.w, r.h);
  }

  // ****************************************************

  /**
   * resets this region (usually a Screen object) to the coordinates of the containing screen
   * <p>
   * Because of the wanted side effect for the containing screen, this should only be used with screen objects. For
   * Region objects use setRect() instead.
   */
  public void setROI() {
    setROI(getScreen().getBounds());
  }

  /**
   * resets this region to the given location, and size <br> this might move the region even to another screen
   * <p>
   * <br>Because of the wanted side effect for the containing screen, this should only be used with screen objects.
   * <br>For Region objects use setRect() instead.
   *
   * @param X new x
   * @param Y new y
   * @param W new width
   * @param H new height
   */
  public void setROI(int X, int Y, int W, int H) {
    x = X;
    y = Y;
    w = W > 1 ? W : 1;
    h = H > 1 ? H : 1;
    initScreen(null);
  }

  /**
   * resets this region to the given rectangle <br> this might move the region even to another screen
   * <p>
   * <br>Because of the wanted side effect for the containing screen, this should only be used with screen objects.
   * <br>For Region objects use setRect() instead.
   *
   * @param r AWT Rectangle
   */
  public void setROI(Rectangle r) {
    setROI(r.x, r.y, r.width, r.height);
  }

  /**
   * resets this region to the given region <br> this might move the region even to another screen
   * <p>
   * <br>Because of the wanted side effect for the containing screen, this should only be used with screen objects.
   * <br>For Region objects use setRect() instead.
   *
   * @param reg Region
   */
  public void setROI(Region reg) {
    setROI(reg.getX(), reg.getY(), reg.getW(), reg.getH());
  }

  /**
   * A function only for backward compatibility - Only makes sense with Screen objects
   *
   * @return the Region being the current ROI of the containing Screen
   */
  public Region getROI() {
    IScreen screen = getScreen();
    Rectangle screenRect = screen.getRect();
    return new Region(screenRect.x, screenRect.y, screenRect.width, screenRect.height, screen);
  }

  // ****************************************************

  /**
   * @return the region itself
   * @deprecated only for backward compatibility
   */
  @Deprecated
  public Region inside() {
    return this;
  }

  /**
   * set the regions position<br>this might move the region even to another screen
   *
   * @param loc new top left corner
   * @return the region itself
   * @deprecated to be like AWT Rectangle API use setLocation()
   */
  @Deprecated
  public Region moveTo(Location loc) {
    return setLocation(loc);
  }

  /**
   * set the regions position<br>this might move the region even to another screen
   *
   * @param loc new top left corner
   * @return the region itself
   */
  public Region setLocation(Location loc) {
    x = loc.x;
    y = loc.y;
    initScreen(null);
    return this;
  }

  /**
   * set the regions position/size<br>this might move the region even to another screen
   *
   * @param r Region
   * @return the region itself
   * @deprecated to be like AWT Rectangle API use setRect() instead
   */
  @Deprecated
  public Region morphTo(Region r) {
    return setRect(r);
  }

  /**
   * resize the region using the given padding values<br>might be negative
   *
   * @param l padding on left side
   * @param r padding on right side
   * @param t padding at top side
   * @param b padding at bottom side
   * @return the region itself
   */
  public Region add(int l, int r, int t, int b) {
    x = x - l;
    y = y - t;
    w = w + l + r;
    if (w < 1) {
      w = 1;
    }
    h = h + t + b;
    if (h < 1) {
      h = 1;
    }
    initScreen(null);
    return this;
  }

  /**
   * extend the region, so it contains the given region<br>but only the part inside the current screen
   *
   * @param r the region to include
   * @return the region itself
   */
  public Region add(Region r) {
    Rectangle rect = getRect();
    rect.add(r.getRect());
    setRect(rect);
    initScreen(null);
    return this;
  }

  /**
   * extend the region, so it contains the given point<br>but only the part inside the current screen
   *
   * @param loc the point to include
   * @return the region itself
   */
  public Region add(Location loc) {
    Rectangle rect = getRect();
    rect.add(loc.x, loc.y);
    setRect(rect);
    initScreen(null);
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="lastMatch">
  // ************************************************

  /**
   * a find operation saves its match on success in the used region object<br>unchanged if not successful
   *
   * @return the Match object from last successful find in this region
   */
  public Match getLastMatch() {
    return lastMatch;
  }

  // ************************************************

  /**
   * a searchAll operation saves its matches on success in the used region object<br>unchanged if not successful
   *
   * @return a Match-Iterator of matches from last successful searchAll in this region
   */
  public Iterator<Match> getLastMatches() {
    return lastMatches;
  }
  //</editor-fold>

  //<editor-fold desc="save capture to file">
  public String saveScreenCapture() {
    return getScreen().capture(this).save();
  }

  public String saveScreenCapture(String path) {
    return getScreen().capture(this).save(path);
  }

  public String saveScreenCapture(String path, String name) {
    return getScreen().capture(this).save(path, name);
  }

  // ************************************************

  /**
   * get the last image taken on this regions screen
   *
   * @return the stored ScreenImage
   */
  public ScreenImage getLastScreenImage() {
    return getScreen().getLastScreenImageFromScreen();
  }

  /**
   * stores the lastScreenImage in the current bundle path with a created unique name
   *
   * @return the absolute file name
   * @throws java.io.IOException if not possible
   */
  public String getLastScreenImageFile() throws IOException {
    return getScreen().getLastScreenImageFile(ImagePath.getBundlePath(), null);
  }

  /**
   * stores the lastScreenImage in the current bundle path with the given name
   *
   * @param name file name (.png is added if not there)
   * @return the absolute file name
   * @throws java.io.IOException if not possible
   */
  public String getLastScreenImageFile(String name) throws IOException {
    return getScreen().getLastScreenImageFromScreen().getFile(ImagePath.getBundlePath(), name);
  }

  /**
   * stores the lastScreenImage in the given path with the given name
   *
   * @param path path to use
   * @param name file name (.png is added if not there)
   * @return the absolute file name
   * @throws java.io.IOException if not possible
   */
  public String getLastScreenImageFile(String path, String name) throws IOException {
    return getScreen().getLastScreenImageFromScreen().getFile(path, name);
  }

  public void saveLastScreenImage() {
    ScreenImage simg = getScreen().getLastScreenImageFromScreen();
    if (simg != null) {
      simg.saveLastScreenImage(runTime.fSikulixStore);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="spatial operators - new regions">

  /**
   * check if current region contains given region
   *
   * @param region the other Region
   * @return true/false
   */
  public boolean contains(Region region) {
    return getRect().contains(region.getRect());
  }

  /**
   * create a Location object, that can be used as an offset taking the width and hight of this Region
   *
   * @return a new Location object with width and height as x and y
   */
  public Location asOffset() {
    return new Location(w, h);
  }

  /**
   * create region with same size at top left corner offset
   *
   * @param loc use its x and y to set the offset
   * @return the new region
   */
  public Region offset(Location loc) {
    return Region.create(x + loc.x, y + loc.y, w, h, scr);
  }

  /**
   * create region with same size at top left corner offset
   *
   * @param x horizontal offset
   * @param y vertical offset
   * @return the new region
   */
  public Region offset(int x, int y) {
    return Region.create(this.x + x, this.y + y, w, h, scr);
  }

  /**
   * create a region enlarged Settings.DefaultPadding pixels on each side
   *
   * @return the new region
   * @deprecated to be like AWT Rectangle API use grow() instead
   */
  @Deprecated
  public Region nearby() {
    return grow(Settings.DefaultPadding, Settings.DefaultPadding);
  }

  /**
   * create a region enlarged range pixels on each side
   *
   * @param range the margin to be added around
   * @return the new region
   * @deprecated to be like AWT Rectangle API use grow() instaed
   */
  @Deprecated
  public Region nearby(int range) {
    return grow(range, range);
  }

  /**
   * create a region enlarged n pixels on each side (n = Settings.DefaultPadding = 50 default)
   *
   * @return the new region
   */
  public Region grow() {
    return grow(Settings.DefaultPadding, Settings.DefaultPadding);
  }

  /**
   * create a region enlarged range pixels on each side
   *
   * @param range the margin to be added around
   * @return the new region
   */
  public Region grow(int range) {
    return grow(range, range);
  }

  /**
   * create a region enlarged w pixels on left and right side and h pixels at top and bottom
   *
   * @param w pixels horizontally
   * @param h pixels vertically
   * @return the new region
   */
  public Region grow(int w, int h) {
    Rectangle r = getRect();
    r.grow(w, h);
    return Region.create(r.x, r.y, r.width, r.height, scr);
  }

  /**
   * create a region enlarged l pixels on left and r pixels right side and t pixels at top side and b pixels a bottom
   * side. negative values go inside (shrink)
   *
   * @param l add to the left
   * @param r add to right
   * @param t add above
   * @param b add beneath
   * @return the new region
   */
  public Region grow(int l, int r, int t, int b) {
    return Region.create(x - l, y - t, w + l + r, h + t + b, scr);
  }

  /**
   * point middle on right edge
   *
   * @return point middle on right edge
   */
  public Location rightAt() {
    return rightAt(0);
  }

  /**
   * positive offset goes to the right. might be off current screen
   *
   * @param offset pixels
   * @return point with given offset horizontally to middle point on right edge
   */
  public Location rightAt(int offset) {
    return checkAndSetRemote(new Location(x + w + offset, y + h / 2));
  }

  /**
   * create a region right of the right side with same height. the new region extends to the right screen border<br>
   * use grow() to include the current region
   *
   * @return the new region
   */
  public Region right() {
    int distToRightScreenBorder = getScreen().getX() + getScreen().getW() - (getX() + getW());
    return right(distToRightScreenBorder);
  }

  /**
   * create a region right of the right side with same height and given width. negative width creates the right part
   * with width inside the region<br>
   * use grow() to include the current region
   *
   * @param width pixels
   * @return the new region
   */
  public Region right(int width) {
    int _x;
    if (width < 0) {
      _x = x + w + width;
    } else {
      _x = x + w;
    }
    return Region.create(_x, y, Math.abs(width), h, scr);
  }

  /**
   * @return point middle on left edge
   */
  public Location leftAt() {
    return leftAt(0);
  }

  /**
   * negative offset goes to the left <br>might be off current screen
   *
   * @param offset pixels
   * @return point with given offset horizontally to middle point on left edge
   */
  public Location leftAt(int offset) {
    return checkAndSetRemote(new Location(x + offset, y + h / 2));
  }

  /**
   * create a region left of the left side with same height<br> the new region extends to the left screen border<br> use
   * grow() to include the current region
   *
   * @return the new region
   */
  public Region left() {
    int distToLeftScreenBorder = getX() - getScreen().getX();
    return left(distToLeftScreenBorder);
  }

  /**
   * create a region left of the left side with same height and given width<br>
   * negative width creates the left part with width inside the region use grow() to include the current region <br>
   *
   * @param width pixels
   * @return the new region
   */
  public Region left(int width) {
    int _x;
    if (width < 0) {
      _x = x;
    } else {
      _x = x - width;
    }
    return Region.create(getScreen().getBounds().intersection(new Rectangle(_x, y, Math.abs(width), h)), scr);
  }

  /**
   * @return point middle on top edge
   */
  public Location aboveAt() {
    return aboveAt(0);
  }

  /**
   * negative offset goes towards top of screen <br>might be off current screen
   *
   * @param offset pixels
   * @return point with given offset vertically to middle point on top edge
   */
  public Location aboveAt(int offset) {
    return checkAndSetRemote(new Location(x + w / 2, y + offset));
  }

  /**
   * create a region above the top side with same width<br> the new region extends to the top screen border<br> use
   * grow() to include the current region
   *
   * @return the new region
   */
  public Region above() {
    int distToAboveScreenBorder = getY() - getScreen().getY();
    return above(distToAboveScreenBorder);
  }

  /**
   * create a region above the top side with same width and given height<br>
   * negative height creates the top part with height inside the region use grow() to include the current region
   *
   * @param height pixels
   * @return the new region
   */
  public Region above(int height) {
    int _y;
    if (height < 0) {
      _y = y;
    } else {
      _y = y - height;
    }
    return Region.create(getScreen().getBounds().intersection(new Rectangle(x, _y, w, Math.abs(height))), scr);
  }

  /**
   * @return point middle on bottom edge
   */
  public Location belowAt() {
    return belowAt(0);
  }

  /**
   * positive offset goes towards bottom of screen <br>might be off current screen
   *
   * @param offset pixels
   * @return point with given offset vertically to middle point on bottom edge
   */
  public Location belowAt(int offset) {
    return checkAndSetRemote(new Location(x + w / 2, y + h - offset));
  }

  /**
   * create a region below the bottom side with same width<br> the new region extends to the bottom screen border<br>
   * use grow() to include the current region
   *
   * @return the new region
   */
  public Region below() {
    int distToBelowScreenBorder = getScreen().getY() + getScreen().getH() - (getY() + getH());
    return below(distToBelowScreenBorder);
  }

  /**
   * create a region below the bottom side with same width and given height<br>
   * negative height creates the bottom part with height inside the region use grow() to include the current region
   *
   * @param height pixels
   * @return the new region
   */
  public Region below(int height) {
    int _y;
    if (height < 0) {
      _y = y + h + height;
    } else {
      _y = y + h;
    }
    return Region.create(x, _y, w, Math.abs(height), scr);
  }

  /**
   * create a new region containing both regions
   *
   * @param ur region to unite with
   * @return the new region
   */
  public Region union(Region ur) {
    Rectangle r = getRect().union(ur.getRect());
    return Region.create(r.x, r.y, r.width, r.height, scr);
  }

  /**
   * create a region that is the intersection of the given regions
   *
   * @param ir the region to intersect with like AWT Rectangle API
   * @return the new region
   */
  public Region intersection(Region ir) {
    Rectangle r = getRect().intersection(ir.getRect());
    return Region.create(r.x, r.y, r.width, r.height, scr);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="parts of a Region">

  /**
   * select the specified part of the region.
   * <p>
   * <br>Constants for the top parts of a region (Usage: Region.CONSTANT)<br>
   * shown in brackets: possible shortcuts for the part constant<br>
   * NORTH (NH, TH) - upper half <br>
   * NORTH_WEST (NW, TL) - left third in upper third <br>
   * NORTH_MID (NM, TM) - middle third in upper third <br>
   * NORTH_EAST (NE, TR) - right third in upper third <br>
   * ... similar for the other directions: <br>
   * right side: EAST (Ex, Rx)<br>
   * bottom part: SOUTH (Sx, Bx) <br>
   * left side: WEST (Wx, Lx)<br>
   * <br>
   * specials for quartered:<br>
   * TT top left quarter<br>
   * RR top right quarter<br>
   * BB bottom right quarter<br>
   * LL bottom left quarter<br>
   * <br>
   * specials for the center parts:<br>
   * MID_VERTICAL (MV, CV) half of width vertically centered <br>
   * MID_HORIZONTAL (MH, CH) half of height horizontally centered <br>
   * MID_BIG (M2, C2) half of width / half of height centered <br>
   * MID_THIRD (MM, CC) third of width / third of height centered <br>
   * <br>
   * Based on the scheme behind these constants there is another possible usage:<br>
   * specify part as e 3 digit integer where the digits xyz have the following meaning<br>
   * 1st x: use a raster of x rows and x columns<br>
   * 2nd y: the row number of the wanted cell<br>
   * 3rd z: the column number of the wanted cell<br>
   * y and z are counting from 0<br>
   * valid numbers: 200 up to 999 (&lt; 200 are invalid and return the region itself) <br>
   * example: get(522) will use a raster of 5 rows and 5 columns and return the cell in the middle<br>
   * special cases:<br>
   * if either y or z are == or &gt; x: returns the respective row or column<br>
   * example: get(525) will use a raster of 5 rows and 5 columns and return the row in the middle<br>
   * <br>
   * internally this is based on {@link #setRaster(int, int) setRaster} and {@link #getCell(int, int) getCell} <br>
   * <br>
   * If you need only one row in one column with x rows or only one column in one row with x columns you can use
   * {@link #getRow(int, int) getRow} or {@link #getCol(int, int) getCol}
   *
   * @param part the part to get (Region.PART long or short)
   * @return new region
   */

  /**
   * the area constants for use with get()
   */
  public static final int NW = 300, NORTH_WEST = NW, TL = NW;
  public static final int NM = 301, NORTH_MID = NM, TM = NM;
  public static final int NE = 302, NORTH_EAST = NE, TR = NE;
  public static final int EM = 312, EAST_MID = EM, RM = EM;
  public static final int SE = 322, SOUTH_EAST = SE, BR = SE;
  public static final int SM = 321, SOUTH_MID = SM, BM = SM;
  public static final int SW = 320, SOUTH_WEST = SW, BL = SW;
  public static final int WM = 310, WEST_MID = WM, LM = WM;
  public static final int MM = 311, MIDDLE = MM, M3 = MM;
  public static final int TT = 200;
  public static final int RR = 201;
  public static final int BB = 211;
  public static final int LL = 210;
  public static final int NH = 202, NORTH = NH, TH = NH;
  public static final int EH = 221, EAST = EH, RH = EH;
  public static final int SH = 212, SOUTH = SH, BH = SH;
  public static final int WH = 220, WEST = WH, LH = WH;
  public static final int MV = 441, MID_VERTICAL = MV, CV = MV;
  public static final int MH = 414, MID_HORIZONTAL = MH, CH = MH;
  public static final int M2 = 444, MIDDLE_BIG = M2, C2 = M2;
  public static final int EN = NE, EAST_NORTH = NE, RT = TR;
  public static final int ES = SE, EAST_SOUTH = SE, RB = BR;
  public static final int WN = NW, WEST_NORTH = NW, LT = TL;
  public static final int WS = SW, WEST_SOUTH = SW, LB = BL;

  /**
   * to support a raster over the region
   */
  private int rows;
  private int cols = 0;
  private int rowH = 0;
  private int colW = 0;
  private int rowHd = 0;
  private int colWd = 0;

  public Region get(int part) {
    return Region.create(getRectangle(getRect(), part));
  }

  protected static Rectangle getRectangle(Rectangle rect, int part) {
    if (part < 200 || part > 999) {
      return rect;
    }
    Region r = Region.create(rect);
    int pTyp = (int) (part / 100);
    int pPos = part - pTyp * 100;
    int pRow = (int) (pPos / 10);
    int pCol = pPos - pRow * 10;
    r.setRaster(pTyp, pTyp);
    if (pTyp == 3) {
      // NW = 300, NORTH_WEST = NW;
      // NM = 301, NORTH_MID = NM;
      // NE = 302, NORTH_EAST = NE;
      // EM = 312, EAST_MID = EM;
      // SE = 322, SOUTH_EAST = SE;
      // SM = 321, SOUTH_MID = SM;
      // SW = 320, SOUTH_WEST = SW;
      // WM = 310, WEST_MID = WM;
      // MM = 311, MIDDLE = MM, M3 = MM;
      return r.getCell(pRow, pCol).getRect();
    }
    if (pTyp == 2) {
      // NH = 202, NORTH = NH;
      // EH = 221, EAST = EH;
      // SH = 212, SOUTH = SH;
      // WH = 220, WEST = WH;
      if (pRow > 1) {
        return r.getCol(pCol).getRect();
      } else if (pCol > 1) {
        return r.getRow(pRow).getRect();
      }
      return r.getCell(pRow, pCol).getRect();
    }
    if (pTyp == 4) {
      // MV = 441, MID_VERTICAL = MV;
      // MH = 414, MID_HORIZONTAL = MH;
      // M2 = 444, MIDDLE_BIG = M2;
      if (pRow > 3) {
        if (pCol > 3) {
          return r.getCell(1, 1).union(r.getCell(2, 2)).getRect();
        }
        return r.getCell(0, 1).union(r.getCell(3, 2)).getRect();
      } else if (pCol > 3) {
        return r.getCell(1, 0).union(r.getCell(2, 3)).getRect();
      }
      return r.getCell(pRow, pCol).getRect();
    }
    return rect;
  }

  /**
   * store info: this region is divided vertically into n even rows <br>
   * a preparation for using getRow()
   *
   * @param n number of rows
   * @return the top row
   */
  public Region setRows(int n) {
    return setRaster(n, 0);
  }

  /**
   * store info: this region is divided horizontally into n even columns <br>
   * a preparation for using getCol()
   *
   * @param n number of columns
   * @return the leftmost column
   */
  public Region setCols(int n) {
    return setRaster(0, n);
  }

  /**
   * @return the number of rows or null
   */
  public int getRows() {
    return rows;
  }

  /**
   * @return the row height or 0
   */
  public int getRowH() {
    return rowH;
  }

  /**
   * @return the number of columns or 0
   */
  public int getCols() {
    return cols;
  }

  /**
   * @return the columnwidth or 0
   */
  public int getColW() {
    return colW;
  }

  /**
   * Can be used to check, wether the Region currently has a valid raster
   *
   * @return true if it has a valid raster (either getCols or getRows or both would return &gt; 0) false otherwise
   */
  public boolean isRasterValid() {
    return (rows > 0 || cols > 0);
  }

  private int spanMin = 5;

  /**
   * store info: this region is divided into a raster of even cells <br>
   * a preparation for using getCell()<br>
   * adjusted to a minimum cell size of 5 x 5 pixels
   *
   * @param r number of rows
   * @param c number of columns
   * @return the topleft cell
   */
  public Region setRaster(int r, int c) {
    rows = Math.max(1, r);
    cols = Math.max(1, c);
    rowH = h / rows;
    if (rowH < spanMin) {
      rowH = spanMin;
      rows = h / rowH;
    }
    rowHd = h - rows * rowH;
    colW = w / cols;
    if (colW < spanMin) {
      colW = spanMin;
      cols = w / colW;
    }
    colWd = w - cols * colW;
    return getCell(0, 0);
  }

  /**
   * get the specified row counting from 0<br>
   * negative counts reverse from the end (last is -1)<br>
   * values outside range are 0 or last respectively
   *
   * @param r row number
   * @return the row as new region or the region itself, if no rows are setup
   */
  public Region getRow(int r) {
    if (rows == 0) {
      return this;
    }
    if (r < 0) {
      r = rows + r;
    }
    r = Math.max(0, r);
    r = Math.min(r, rows - 1);
    return Region.create(x, y + r * rowH, w, rowH);
  }

  public Region getRow(int r, int n) {
    return this;
  }

  /**
   * get the specified column counting from 0<br>
   * negative counts reverse from the end (last is -1)<br>
   * values outside range are 0 or last respectively
   *
   * @param c column number
   * @return the column as new region or the region itself, if no columns are setup
   */
  public Region getCol(int c) {
    if (cols == 0) {
      return this;
    }
    if (c < 0) {
      c = cols + c;
    }
    c = Math.max(0, c);
    c = Math.min(c, cols - 1);
    return Region.create(x + c * colW, y, colW, h);
  }

  /**
   * divide the region in n columns and select column c as new Region
   *
   * @param c the column to select counting from 0 or negative to count from the end
   * @param n how many columns to devide in
   * @return the selected part or the region itself, if parameters are invalid
   */
  public Region getCol(int c, int n) {
    Region r = new Region(this);
    r.setCols(n);
    return r.getCol(c);
  }

  /**
   * get the specified cell counting from (0, 0), if a raster is setup <br>
   * negative counts reverse from the end (last = -1) values outside range are 0 or last respectively
   *
   * @param r row number
   * @param c column number
   * @return the cell as new region or the region itself, if no raster is setup
   */
  public Region getCell(int r, int c) {
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
    return Region.create(x + c * colW, y + r * rowH, colW, rowH);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="highlight">
  protected void updateSelf() {
    if (overlay != null) {
      highlight(false, null);
      highlight(true, null);
    }
  }

  protected Region silentHighlight(boolean onOff) {
    if (onOff && overlay == null) {
      return doHighlight(true, null, true);
    }
    if (!onOff && overlay != null) {
      return doHighlight(false, null, true);
    }
    return this;
  }

  /**
   * Toggle the regions Highlight visibility (red frame)
   *
   * @return the region itself
   */
  public Region highlight() {
    // Pass true if overlay is null, false otherwise
    highlight(overlay == null, null);
    return this;
  }

  /**
   * Toggle the regions Highlight visibility (frame of specified color)<br>
   * allowed color specifications for frame color: <br>
   * - a color name out of: black, blue, cyan, gray, green, magenta, orange, pink, red, white, yellow (lowercase and
   * uppercase can be mixed, internally transformed to all uppercase) <br>
   * - these colornames exactly written: lightGray, LIGHT_GRAY, darkGray and DARK_GRAY <br>
   * - a hex value like in HTML: #XXXXXX (max 6 hex digits) - an RGB specification as: #rrrgggbbb where rrr, ggg, bbb
   * are integer values in range 0 - 255 padded with leading zeros if needed (hence exactly 9 digits)
   *
   * @param color Color of frame
   * @return the region itself
   */
  public Region highlight(String color) {
    // Pass true if overlay is null, false otherwise
    highlight(overlay == null, color);
    return this;
  }

  /**
   * Sets the regions Highlighting border
   *
   * @param toEnable set overlay enabled or disabled
   * @param color    Color of frame (see method highlight(color))
   */
  private Region highlight(boolean toEnable, String color) {
    return doHighlight(toEnable, color, false);
  }

  private Region doHighlight(boolean toEnable, String color, boolean silent) {
    if (isOtherScreen()) {
      return this;
    }
    if (!silent) {
      Debug.action("toggle highlight " + toString() + ": " + toEnable
              + (color != null ? " color: " + color : ""));
    }
    if (toEnable) {
      overlay = new ScreenHighlighter(getScreen(), color);
      overlay.setWaitAfter(silent);
      overlay.highlight(this);
    } else {
      if (overlay != null) {
        overlay.close();
        overlay = null;
      }
    }
    return this;
  }

  /**
   * show the regions Highlight for the given time in seconds (red frame) if 0 - use the global Settings.SlowMotionDelay
   *
   * @param secs time in seconds
   * @return the region itself
   */
  public Region highlight(float secs) {
    return highlight(secs, null);
  }

  /**
   * show the regions Highlight for the given time in seconds (frame of specified color) if 0 - use the global
   * Settings.SlowMotionDelay
   *
   * @param secs  time in seconds
   * @param color Color of frame (see method highlight(color))
   * @return the region itself
   */
  public Region highlight(float secs, String color) {
    if (getScreen() == null || isOtherScreen() || isScreenUnion) {
      Debug.error("highlight: not possible for %s", getScreen());
      return this;
    }
    if (secs < 0.1) {
      return highlight((int) secs, color);
    }
    Debug.action("highlight " + toStringShort() + " for " + secs + " secs"
            + (color != null ? " color: " + color : ""));
    ScreenHighlighter _overlay = new ScreenHighlighter(getScreen(), color);
    _overlay.highlight(this, secs);
    return this;
  }

  /**
   * hack to implement the getLastMatch() convenience 0 means same as highlight() &lt; 0 same as highlight(secs) if
   * available the last match is highlighted
   *
   * @param secs seconds
   * @return this region
   */
  public Region highlight(int secs) {
    return highlight(secs, null);
  }

  /**
   * Show highlight in selected color
   *
   * @param secs  time in seconds
   * @param color Color of frame (see method highlight(color))
   * @return this region
   */
  public Region highlight(int secs, String color) {
    if (getScreen() == null || isOtherScreen() || isScreenUnion) {
      Debug.error("highlight: not possible for %s", getScreen());
      return this;
    }
    if (secs > 0) {
      return highlight((float) secs, color);
    }
    if (lastMatch != null) {
      if (secs < 0) {
        return lastMatch.highlight((float) -secs, color);
      }
      return lastMatch.highlight(Settings.DefaultHighlightTime, color);
    }
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="find public methods">

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

  /**
   * return false to skip <br>
   * return true to try again <br>
   * throw FindFailed to abort
   *
   * @param img Handles a failed find action
   */
  private <PSI> Boolean handleFindFailed(PSI target, Image img, boolean isExists) {
    log(lvl, "handleFindFailed: %s", target);
    Boolean state = null;
    ObserveEvent evt = null;
    FindFailedResponse response = findFailedResponse;
    if (FindFailedResponse.HANDLE.equals(response)) {
      ObserveEvent.Type type = ObserveEvent.Type.FINDFAILED;
      if (findFailedHandler != null && ((ObserverCallBack) findFailedHandler).getType().equals(type)) {
        log(lvl, "handleFindFailed: Response.HANDLE: calling handler");
        evt = new ObserveEvent("", type, target, img, this, 0);
        ((ObserverCallBack) findFailedHandler).findfailed(evt);
        response = evt.getResponse();
      }
    }
    if (FindFailedResponse.ABORT.equals(response)) {
      state = null;
      if (isExists) {
        state = false;
      }
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
      // TODO HACK to allow recapture on FindFailed PROMPT
      if (img.backup()) {
        img.delete();
        state = handleImageMissing(img, true);
        if (state == null || !state) {
          if (!img.restore()) {
            state = null;
          } else {
            img.get();
          }
        }
      }
    } else if (FindFailedResponse.RETRY.equals(response)) {
      state = true;
    }
    return state;
  }

  private Boolean handleImageMissing(Image img, boolean recap) {
    log(lvl, "handleImageMissing: %s", img.getName());
    ObserveEvent evt = null;
    FindFailedResponse response = findFailedResponse;
    if (FindFailedResponse.HANDLE.equals(response)) {
      ObserveEvent.Type type = ObserveEvent.Type.MISSING;
      if (imageMissingHandler != null && ((ObserverCallBack) imageMissingHandler).getType().equals(type)) {
        log(lvl, "handleImageMissing: Response.HANDLE: calling handler");
        evt = new ObserveEvent("", type, null, img, this, 0);
        ((ObserverCallBack) imageMissingHandler).missing(evt);
        response = evt.getResponse();
      } else {
        response = FindFailedResponse.PROMPT;
      }
    }
    if (FindFailedResponse.PROMPT.equals(response)) {
      log(lvl, "handleImageMissing: Response.PROMPT");
      response = handleFindFailedShowDialog(img, true);
    }
    if (findFailedResponse.RETRY.equals(response)) {
      log(lvl, "handleImageMissing: Response.RETRY: %s", (recap ? "recapture " : "capture missing "));
      getRobotForRegion().delay(500);
      ScreenImage simg = getScreen().userCapture(
              (recap ? "recapture " : "capture missing ") + img.getName());
      if (simg != null) {
        String path = ImagePath.getBundlePath();
        if (path == null) {
          log(-1, "handleImageMissing: no bundle path - aborting");
          return null;
        }
        simg.getFile(path, img.getImageName());
        Image.set(img);
        if (img.isValid()) {
          log(lvl, "handleImageMissing: %scaptured: %s", (recap ? "re" : ""), img);
          Image.setIDEshouldReload(img);
          return true;
        }
      }
      return null;
    } else if (findFailedResponse.ABORT.equals(response)) {
      log(lvl, "handleImageMissing: Response.ABORT: aborting");
      return null;
    }
    log(lvl, "handleImageMissing: skip requested on %s", (recap ? "recapture " : "capture missing "));
    return false;
  }

  private FindFailedResponse handleFindFailedShowDialog(Image img, boolean shouldCapture) {
    log(lvl, "handleFindFailedShowDialog: requested %s", (shouldCapture ? "(with capture)" : ""));
    FindFailedResponse response;
    FindFailedDialog fd = new FindFailedDialog(img, shouldCapture);
    fd.setVisible(true);
    response = fd.getResponse();
    fd.dispose();
    wait(0.5);
    log(lvl, "handleFindFailedShowDialog: answer is %s", response);
    return response;
  }

  /**
   * finds the given Pattern, String or Image in the region and returns the best match. If AutoWaitTimeout is set, this
   * is equivalent to wait(). Otherwise only one search attempt will be done.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return If found, the element. null otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Match find(PSI target) throws FindFailed {
    if (autoWaitTimeout > 0) {
      return wait(target, autoWaitTimeout);
    }
    lastMatch = null;
    Image img = Image.getImageFromTarget(target);
    Boolean response = true;
    if (!img.isText() && !img.isValid() && img.hasIOException()) {
      response = handleImageMissing(img, false);
      if (response == null) {
        runTime.abortScripting("Find: Abort:", "ImageMissing: " + target.toString());
      }
    }
    String targetStr = img.getName();
    while (null != response && response) {
      log(lvl, "find: waiting 0 secs for %s to appear in %s", targetStr, this.toStringShort());
      lastMatch = doFind(target, img, null);
      if (lastMatch != null) {
        lastMatch.setImage(img);
        if (isOtherScreen()) {
          lastMatch.setOtherScreen();
        } else if (img != null) {
          img.setLastSeen(lastMatch.getRect(), lastMatch.getScore());
        }
        log(lvl, "find: %s appeared (%s)", targetStr, lastMatch);
        break;
      }
      log(lvl, "find: %s did not appear [%d msec]", targetStr, new Date().getTime() - lastFindTime);
      if (null == lastMatch) {
        response = handleFindFailed(target, img, false);
      }
    }
    if (null == response) {
      throw new FindFailed(FindFailed.createdefault(this, img));
    }
    return lastMatch;
  }

  /**
   * Check if target exists (with the default autoWaitTimeout)
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target) {
    return exists(target, autoWaitTimeout);
  }

  /**
   * Check if target exists with a specified timeout<br>
   * timout = 0: returns immediately after first search
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds
   * @return the match (null if not found or image file missing)
   */
  public <PSI> Match exists(PSI target, double timeout) {
    lastMatch = null;
    String shouldAbort = "";
    RepeatableFind rf = new RepeatableFind(target, null);
    Image img = rf._image;
    Boolean response = true;
    if (!img.isText() && !img.isValid() && img.hasIOException()) {
      response = handleImageMissing(img, false);
      if (response == null) {
        runTime.abortScripting("Exists: Abort:", "ImageMissing: " + target.toString());
      }
    }
    String targetStr = img.getName();
    while (null != response && response) {
      log(lvl, "exists: waiting %.1f secs for %s to appear in %s", timeout, targetStr, this.toStringShort());
      if (rf.repeat(timeout)) {
        lastMatch = rf.getMatch();
        lastMatch.setImage(img);
        if (isOtherScreen()) {
          lastMatch.setOtherScreen();
        } else if (img != null) {
          img.setLastSeen(lastMatch.getRect(), lastMatch.getScore());
        }
        log(lvl, "exists: %s has appeared (%s)", targetStr, lastMatch);
        return lastMatch;
      } else {
        response = handleFindFailed(target, img, true);
        if (null == response) {
          shouldAbort = FindFailed.createdefault(this, img);
        } else if (response) {
          if (img.isRecaptured()) {
            rf = new RepeatableFind(target, img);
          }
          continue;
        }
        break;
      }
    }
    if (!shouldAbort.isEmpty()) {
      runTime.abortScripting("Exists: Abort:", "FindFailed: " + shouldAbort);
    } else {
      log(lvl, "exists: %s did not appear [%d msec]", targetStr, new Date().getTime() - lastFindTime);
    }
    return null;
  }

  /**
   * finds all occurences of the given Pattern, String or Image in the region and returns an Iterator of Matches.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Iterator<Match> findAll(PSI target) throws FindFailed {
    lastMatches = null;
    RepeatableFindAll rf = new RepeatableFindAll(target, null);
    Image img = rf._image;
    String targetStr = img.getName();
    Boolean response = true;
    if (!img.isValid() && img.hasIOException()) {
      response = handleImageMissing(img, false);
      if (response == null) {
        runTime.abortScripting("FindAll: Abort:", "ImageMissing: " + target.toString());
      }
    }
    while (null != response && response) {
      log(lvl, "findAll: waiting %.1f secs for (multiple) %s to appear in %s",
              autoWaitTimeout, targetStr, this.toStringShort());
      if (autoWaitTimeout > 0) {
        rf.repeat(autoWaitTimeout);
        lastMatches = rf.getMatches();
      } else {
        lastMatches = doFindAll(target, null);
      }
      if (lastMatches != null) {
        log(lvl, "findAll: %s has appeared", targetStr);
        break;
      } else {
        log(lvl, "findAll: %s did not appear", targetStr);
        response = handleFindFailed(target, img, false);
      }
    }
    if (null == response) {
      throw new FindFailed(FindFailed.createdefault(this, img));
    }
    return lastMatches;
  }

  public <PSI> Match[] findAllByRow(PSI target) {
    Match[] matches = new Match[0];
    List<Match> mList = findAllCollect(target);
    if (mList.isEmpty()) {
      return null;
    }
    Collections.sort(mList, new Comparator<Match>() {
      @Override
      public int compare(Match m1, Match m2) {
        if (m1.y == m2.y) {
          return m1.x - m2.x;
        }
        return m1.y - m2.y;
      }
    });
    return mList.toArray(matches);
  }

  public <PSI> Match[] findAllByColumn(PSI target) {
    Match[] matches = new Match[0];
    List<Match> mList = findAllCollect(target);
    if (mList.isEmpty()) {
      return null;
    }
    Collections.sort(mList, new Comparator<Match>() {
      @Override
      public int compare(Match m1, Match m2) {
        if (m1.x == m2.x) {
          return m1.y - m2.y;
        }
        return m1.x - m2.x;
      }
    });
    return mList.toArray(matches);
  }

  private <PSI> List<Match> findAllCollect(PSI target) {
    Iterator<Match> mIter = null;
    try {
      mIter = findAll(target);
    } catch (Exception ex) {
      Debug.error("findAllByRow: %s", ex.getMessage());
      return null;
    }
    List<Match> mList = new ArrayList<Match>();
    while (mIter.hasNext()) {
      mList.add(mIter.next());
    }
    return mList;
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
    Debug.log(lvl, "findBest: enter");
    if (pList == null || pList.size() == 0) {
      return null;
    }
    Match mResult = null;
    List<Match> mList = findAnyCollect(pList);
    if (mList.size() > 0) {
      Collections.sort(mList, new Comparator<Match>() {
        @Override
        public int compare(Match m1, Match m2) {
          double ms = m2.getScore() - m1.getScore();
          if (ms < 0) {
            return -1;
          } else if (ms > 0) {
            return 1;
          }
          return 0;
        }
      });
      mResult = mList.get(0);
    }
    return mResult;
  }

  public List<Match> findAny(Object... args) {
    if (args.length == 0) {
      return new ArrayList<Match>();
    }
    List<Object> pList = new ArrayList<>();
    pList.addAll(Arrays.asList(args));
    return findAnyList(pList);
  }

  public List<Match> findAnyList(List<Object> pList) {
    Debug.log(lvl, "findAny: enter");
    if (pList == null || pList.size() == 0) {
      return new ArrayList<Match>();
    }
    List<Match> mList = findAnyCollect(pList);
    return mList;
  }

  private List<Match> findAnyCollect(List<Object> pList) {
    List<Match> mList = new ArrayList<Match>();
    if (pList == null) {
      return mList;
    }
    Match[] mArray = new Match[pList.size()];
    SubFindRun[] theSubs = new SubFindRun[pList.size()];
    int nobj = 0;
    ScreenImage base = getScreen().capture(this);
    for (Object obj : pList) {
      mArray[nobj] = null;
      if (obj instanceof Pattern || obj instanceof String || obj instanceof Image) {
        theSubs[nobj] = new SubFindRun(mArray, nobj, base, obj, this);
        new Thread(theSubs[nobj]).start();
      }
      nobj++;
    }
    Debug.log(lvl, "findAnyCollect: waiting for SubFindRuns");
    nobj = 0;
    boolean all = false;
    while (!all) {
      all = true;
      for (SubFindRun sub : theSubs) {
        all &= sub.hasFinished();
      }
    }
    Debug.log(lvl, "findAnyCollect: SubFindRuns finished");
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

  private class SubFindRun implements Runnable {

    Match[] mArray;
    ScreenImage base;
    Object target;
    Region reg;
    boolean finished = false;
    int subN;

    public SubFindRun(Match[] pMArray, int pSubN,
                      ScreenImage pBase, Object pTarget, Region pReg) {
      subN = pSubN;
      base = pBase;
      target = pTarget;
      reg = pReg;
      mArray = pMArray;
    }

    @Override
    public void run() {
      try {
        mArray[subN] = reg.findInImage(base, target);
      } catch (Exception ex) {
        log(-1, "findAnyCollect: image file not found:\n", target);
      }
      hasFinished(true);
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
  }

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
        if (TextRecognizer.getInstance() != null) {
          log(lvl, "findInImage: Switching to TextSearch");
          finder = new Finder(getScreen().capture(x, y, w, h), this);
          finder.findText((String) target);
        }
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
      match.setImage(img);
      img.setLastSeen(match.getRect(), match.getScore());
    }
    return match;
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
    return wait(target, autoWaitTimeout);
  }

  /**
   * Waits for the Pattern, String or Image to appear or timeout (in second) is passed
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target, double timeout) throws FindFailed {
    lastMatch = null;
    String shouldAbort = "";
    RepeatableFind rf = new RepeatableFind(target, null);
    Image img = rf._image;
    String targetStr = img.getName();
    Boolean response = true;
    if (!img.isText() && !img.isValid() && img.hasIOException()) {
      response = handleImageMissing(img, false);
      if (response == null) {
        runTime.abortScripting("Wait: Abort:", "ImageMissing: " + target.toString());
      }
    }
    while (null != response && response) {
      log(lvl, "wait: waiting %.1f secs for %s to appear in %s", timeout, targetStr, this.toStringShort());
      if (rf.repeat(timeout)) {
        lastMatch = rf.getMatch();
        lastMatch.setImage(img);
        if (isOtherScreen()) {
          lastMatch.setOtherScreen();
        } else if (img != null) {
          img.setLastSeen(lastMatch.getRect(), lastMatch.getScore());
        }
        log(lvl, "wait: %s appeared (%s)", targetStr, lastMatch);
        return lastMatch;
      } else {
        response = handleFindFailed(target, img, false);
        if (null == response) {
          shouldAbort = FindFailed.createdefault(this, img);
          break;
        } else if (response) {
          if (img.isRecaptured()) {
            rf = new RepeatableFind(target, img);
          }
          continue;
        }
        break;
      }
    }
    log(lvl, "wait: %s did not appear [%d msec]", targetStr, new Date().getTime() - lastFindTime);
    if (!shouldAbort.isEmpty()) {
      throw new FindFailed(shouldAbort);
    }
    return lastMatch;
  }

  /**
   * waits until target vanishes or timeout (in seconds) is passed (AutoWaitTimeout)
   *
   * @param <PSI>  Pattern, String or Image
   * @param target The target to wait for it to vanish
   * @return true if the target vanishes, otherwise returns false.
   */
  public <PSI> boolean waitVanish(PSI target) {
    return waitVanish(target, autoWaitTimeout);
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
    RepeatableVanish rv = new RepeatableVanish(target);
    Image img = rv._image;
    String targetStr = img.getName();
    Boolean response = true;
    if (!img.isValid() && img.hasIOException()) {
      response = handleImageMissing(img, false);
    }
    if (null != response && response) {
      log(lvl, "waiting for " + targetStr + " to vanish within %.1f secs", timeout);
      if (rv.repeat(timeout)) {
        log(lvl, "%s vanished", targetStr);
        return true;
      }
      log(lvl, "%s did not vanish before timeout", targetStr);
      return false;
    }
    return false;
  }

//TODO 1.2.0 Region.compare as time optimized Region.exists

  /**
   * time optimized Region.exists, when image-size == region-size<br>
   * 1.1.x: just using exists(img, 0), sizes not checked
   *
   * @param img image file name
   * @return the match or null if not equal
   */
  public Match compare(String img) {
    return compare(Image.create(img));
  }

  /**
   * time optimized Region.exists, when image-size == region-size<br>
   * 1.1.x: just using exists(img, 0), sizes not checked
   *
   * @param img Image object
   * @return the match or null if not equal
   */
  public Match compare(Image img) {
    return exists(img, 0);
  }

  /**
   * Use findText() instead of find() in cases where the given string could be misinterpreted as an image filename
   *
   * @param text    text
   * @param timeout time
   * @return the matched region containing the text
   * @throws org.sikuli.script.FindFailed if not found
   */
  public Match findText(String text, double timeout) throws FindFailed {
    // the leading/trailing tab is used to internally switch to text search directly
    return wait("\t" + text + "\t", timeout);
  }

  /**
   * Use findText() instead of find() in cases where the given string could be misinterpreted as an image filename
   *
   * @param text text
   * @return the matched region containing the text
   * @throws org.sikuli.script.FindFailed if not found
   */
  public Match findText(String text) throws FindFailed {
    return findText(text, autoWaitTimeout);
  }

  /**
   * Use findAllText() instead of findAll() in cases where the given string could be misinterpreted as an image filename
   *
   * @param text text
   * @return the matched region containing the text
   * @throws org.sikuli.script.FindFailed if not found
   */
  public Iterator<Match> findAllText(String text) throws FindFailed {
    // the leading/trailing tab is used to internally switch to text search directly
    return findAll("\t" + text + "\t");
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="find internal methods">

  /**
   * Match doFind( Pattern/String/Image ) finds the given pattern on the screen and returns the best match without
   * waiting.
   */
  private <PSI> Match doFind(PSI ptn, Image img, RepeatableFind repeating) {
    Finder f = null;
    Match m = null;
    IScreen s = null;
    boolean findingText = false;
    ScreenImage simg;
    double findTimeout = autoWaitTimeout;
    String someText = "";
    if (repeating != null) {
      findTimeout = repeating.getFindTimeOut();
    }
    if (repeating != null && repeating._finder != null) {
      simg = getScreen().capture(this);
      f = repeating._finder;
      f.setScreenImage(simg);
      f.setRepeating();
      if (Settings.FindProfiling) {
        Debug.logp("[FindProfiling] Region.doFind repeat: %d msec",
                new Date().getTime() - lastSearchTimeRepeat);
      }
      lastSearchTime = (new Date()).getTime();
      f.findRepeat();
    } else {
      s = getScreen();
      lastFindTime = (new Date()).getTime();
      if (ptn instanceof String) {
        if (((String) ptn).startsWith("\t") && ((String) ptn).endsWith("\t")) {
          findingText = true;
          someText = ((String) ptn).replaceAll("\\t", "");
        } else {
          if (img.isValid()) {
            lastSearchTime = (new Date()).getTime();
            f = checkLastSeenAndCreateFinder(img, findTimeout, null);
            if (!f.hasNext()) {
              runFinder(f, img);
            }
          } else if (img.isText()) {
            findingText = true;
            someText = img.getText();
          }
        }
        if (findingText) {
          log(lvl, "doFind: Switching to TextSearch");
          if (TextRecognizer.getInstance() != null) {
            f = new Finder(getScreen().capture(x, y, w, h), this);
            lastSearchTime = (new Date()).getTime();
            f.findText(someText);
          }
        }
      } else if (ptn instanceof Pattern) {
        if (img.isValid()) {
          lastSearchTime = (new Date()).getTime();
          f = checkLastSeenAndCreateFinder(img, findTimeout, (Pattern) ptn);
          if (!f.hasNext()) {
            runFinder(f, ptn);
          }
        }
      } else if (ptn instanceof Image) {
        if (img.isValid()) {
          lastSearchTime = (new Date()).getTime();
          f = checkLastSeenAndCreateFinder(img, findTimeout, null);
          if (!f.hasNext()) {
            runFinder(f, img);
          }
        }
      } else {
        runTime.abortScripting("aborting script at:",
                String.format("find, wait, exists: invalid parameter: %s", ptn));
      }
      if (repeating != null) {
        repeating._finder = f;
        repeating._image = img;
      }
    }
    if (f != null) {
      lastSearchTimeRepeat = lastSearchTime;
      lastSearchTime = (new Date()).getTime() - lastSearchTime;
      if (f.hasNext()) {
        lastFindTime = (new Date()).getTime() - lastFindTime;
        m = f.next();
        m.setTimes(lastFindTime, lastSearchTime);
        if (Settings.FindProfiling) {
          Debug.logp("[FindProfiling] Region.doFind final: %d msec", lastSearchTime);
        }
      }
    }
    return m;
  }

  private void runFinder(Finder f, Object target) {
    if (Debug.shouldHighlight()) {
      if (this.scr.getW() > w + 20 && this.scr.getH() > h + 20) {
        highlight(2, "#000255000");
      }
    }
    if (target instanceof Image) {
      f.find((Image) target);
    } else if (target instanceof Pattern) {
      f.find((Pattern) target);
    }
  }

  private Finder checkLastSeenAndCreateFinder(Image img, double findTimeout, Pattern ptn) {
    return doCheckLastSeenAndCreateFinder(null, img, findTimeout, ptn);
  }

  private Finder doCheckLastSeenAndCreateFinder(ScreenImage base, Image img, double findTimeout, Pattern ptn) {
    if (base == null) {
      base = getScreen().capture(this);
    }
    boolean shouldCheckLastSeen = false;
    float score = 0;
    if (!Settings.UseImageFinder && Settings.CheckLastSeen && null != img.getLastSeen()) {
      score = (float) (img.getLastSeenScore() - 0.01);
      if (ptn != null) {
        if (!(ptn.getSimilar() > score)) {
          shouldCheckLastSeen = true;
        }
      }
    }
    if (shouldCheckLastSeen) {
      Region r = Region.create(img.getLastSeen());
      if (this.contains(r)) {
        Finder f = new Finder(base.getSub(r.getRect()), r);
        if (Debug.shouldHighlight()) {
          if (this.scr.getW() > w + 10 && this.scr.getH() > h + 10) {
            highlight(2, "#000255000");
          }
        }

        if (ptn == null) {
          f.find(new Pattern(img).similar(score));
        } else {
          f.find(new Pattern(ptn).similar(score));
        }
        if (f.hasNext()) {
          log(lvl, "checkLastSeen: still there");
          return f;
        }
        log(lvl, "checkLastSeen: not there");
      }
    }
    if (Settings.UseImageFinder) {
      ImageFinder f = new ImageFinder(this);
      f.setFindTimeout(findTimeout);
      return f;
    } else {
      return new Finder(base, this);
    }
  }

  /**
   * Match findAllNow( Pattern/String/Image ) finds all the given pattern on the screen and returns the best matches
   * without waiting.
   */
  private <PSI> Iterator<Match> doFindAll(PSI ptn, RepeatableFindAll repeating) {
    boolean findingText = false;
    Finder f;
    ScreenImage simg = getScreen().capture(x, y, w, h);
    if (repeating != null && repeating._finder != null) {
      f = repeating._finder;
      f.setScreenImage(simg);
      f.setRepeating();
      f.findAllRepeat();
    } else {
      f = new Finder(simg, this);
      Image img = null;
      if (ptn instanceof String) {
        if (((String) ptn).startsWith("\t") && ((String) ptn).endsWith("\t")) {
          findingText = true;
        } else {
          img = Image.create((String) ptn);
          if (img.isValid()) {
            f.findAll(img);
          } else if (img.isText()) {
            findingText = true;
          }
        }
        if (findingText) {
          if (TextRecognizer.getInstance() != null) {
            log(lvl, "doFindAll: Switching to TextSearch");
            f.findAllText((String) ptn);
          }
        }
      } else if (ptn instanceof Pattern) {
        if (((Pattern) ptn).isValid()) {
          img = ((Pattern) ptn).getImage();
          f.findAll((Pattern) ptn);
        }
      } else if (ptn instanceof Image) {
        if (((Image) ptn).isValid()) {
          img = ((Image) ptn);
          f.findAll((Image) ptn);
        }
      } else {
        log(-1, "doFind: invalid parameter: %s", ptn);
        Sikulix.terminate(999);
      }
      if (repeating != null) {
        repeating._finder = f;
        repeating._image = img;
      }
    }
    if (f.hasNext()) {
      return f;
    }
    return null;
  }

  // Repeatable Find ////////////////////////////////
  private abstract class Repeatable {

    private double findTimeout;

    abstract void run();

    abstract boolean ifSuccessful();

    double getFindTimeOut() {
      return findTimeout;
    }

    // return TRUE if successful before timeout
    // return FALSE if otherwise
    // throws Exception if any unexpected error occurs
    boolean repeat(double timeout) {
      findTimeout = timeout;
      int MaxTimePerScan = (int) (1000.0 / waitScanRate);
      int timeoutMilli = (int) (timeout * 1000);
      long begin_t = (new Date()).getTime();
      do {
        long before_find = (new Date()).getTime();
        run();
        if (ifSuccessful()) {
          return true;
        } else if (timeoutMilli < MaxTimePerScan || Settings.UseImageFinder) {
          // instant return on first search failed if timeout very small or 0
          // or when using new ImageFinder
          return false;
        }
        long after_find = (new Date()).getTime();
        if (after_find - before_find < MaxTimePerScan) {
          getRobotForRegion().delay((int) (MaxTimePerScan - (after_find - before_find)));
        } else {
          getRobotForRegion().delay(10);
        }
      } while (begin_t + timeout * 1000 > (new Date()).getTime());
      return false;
    }
  }

  private class RepeatableFind extends Repeatable {

    Object _target;
    Match _match = null;
    Finder _finder = null;
    Image _image = null;

    public <PSI> RepeatableFind(PSI target, Image img) {
      _target = target;
      if (img == null) {
        _image = Image.getImageFromTarget(target);
      } else {
        _image = img;
      }
    }

    public Match getMatch() {
      if (_finder != null) {
        _finder.destroy();
      }
      return (_match == null) ? _match : new Match(_match);
    }

    @Override
    public void run() {
      _match = doFind(_target, _image, this);
    }

    @Override
    boolean ifSuccessful() {
      return _match != null;
    }
  }

  private class RepeatableVanish extends RepeatableFind {

    public <PSI> RepeatableVanish(PSI target) {
      super(target, null);
    }

    @Override
    boolean ifSuccessful() {
      return _match == null;
    }
  }

  private class RepeatableFindAll extends Repeatable {

    Object _target;
    Iterator<Match> _matches = null;
    Finder _finder = null;
    Image _image = null;

    public <PSI> RepeatableFindAll(PSI target, Image img) {
      _target = target;
      if (img == null) {
        _image = Image.getImageFromTarget(target);
      } else {
        _image = img;
      }
    }

    public Iterator<Match> getMatches() {
      return _matches;
    }

    @Override
    public void run() {
      _matches = doFindAll(_target, this);
    }

    @Override
    boolean ifSuccessful() {
      return _matches != null;
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Find internal support">
//  private <PatternStringRegionMatch> Region getRegionFromTarget(PatternStringRegionMatch target) throws FindFailed {
//    if (target instanceof Pattern || target instanceof String || target instanceof Image) {
//      Match m = find(target);
//      if (m != null) {
//        return m.setScreen(scr);
//      }
//      return null;
//    }
//    if (target instanceof Region) {
//      return ((Region) target).setScreen(scr);
//    }
//    return null;
//  }
  protected <PSIMRL> Location getLocationFromTarget(PSIMRL target) throws FindFailed {
    if (target instanceof Pattern || target instanceof String || target instanceof Image) {
      Match m = find(target);
      if (m != null) {
        if (isOtherScreen()) {
          return m.getTarget().setOtherScreen(scr);
        } else {
          return m.getTarget();
        }
      }
      return null;
    }
    if (target instanceof Match) {
      return ((Match) target).getTarget();
    }
    if (target instanceof Region) {
      return ((Region) target).getCenter();
    }
    if (target instanceof Location) {
      return new Location((Location) target);
    }
    return null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Observing">
  protected Observer getObserver() {
    if (regionObserver == null) {
      regionObserver = new Observer(this);
    }
    return regionObserver;
  }

  /**
   * evaluate if at least one event observer is defined for this region (the observer need not be running)
   *
   * @return true, if the region has an observer with event observers
   */
  public boolean hasObserver() {
    if (regionObserver != null) {
      return regionObserver.hasObservers();
    }
    return false;
  }

  /**
   * @return true if an observer is running for this region
   */
  public boolean isObserving() {
    return observing;
  }

  /**
   * @return true if any events have happened for this region, false otherwise
   */
  public boolean hasEvents() {
    return Observing.hasEvents(this);
  }

  /**
   * the region's events are removed from the list
   *
   * @return the region's happened events as array if any (size might be 0)
   */
  public ObserveEvent[] getEvents() {
    return Observing.getEvents(this);
  }

  /**
   * the event is removed from the list
   *
   * @param name event's name
   * @return the named event if happened otherwise null
   */
  public ObserveEvent getEvent(String name) {
    return Observing.getEvent(name);
  }

  /**
   * set the observer with the given name inactive (not checked while observing)
   *
   * @param name observers name
   */
  public void setInactive(String name) {
    if (!hasObserver()) {
      return;
    }
    Observing.setActive(name, false);
  }

  /**
   * set the observer with the given name active (checked while observing)
   *
   * @param name observers name
   */
  public void setActive(String name) {
    if (!hasObserver()) {
      return;
    }
    Observing.setActive(name, true);
  }

  /**
   * a subsequently started observer in this region should wait for target and notify the given observer about this
   * event<br>
   * for details about the observe event handler: {@link ObserverCallBack}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   *
   * @param <PSI>    Pattern, String or Image
   * @param target   Pattern, String or Image
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public <PSI> String onAppear(PSI target, Object observer) {
    return onEvent(target, observer, ObserveEvent.Type.APPEAR);
  }

  /**
   * a subsequently started observer in this region should wait for target success and details about the event can be
   * obtained using @{link Observing}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the event's name
   */
  public <PSI> String onAppear(PSI target) {
    return onEvent(target, null, ObserveEvent.Type.APPEAR);
  }

  private <PSIC> String onEvent(PSIC targetThreshhold, Object observer, ObserveEvent.Type obsType) {
    if (observer != null && (observer.getClass().getName().contains("org.python")
            || observer.getClass().getName().contains("org.jruby"))) {
      observer = new ObserverCallBack(observer, obsType);
    }
    if (!(targetThreshhold instanceof Integer)) {
      Image img = Image.getImageFromTarget(targetThreshhold);
      Boolean response = true;
      if (!img.isValid() && img.hasIOException()) {
        response = handleImageMissing(img, false);
        if (response == null) {
          runTime.abortScripting("onEvent(" + obsType.name() + "): Abort:",
                  "ImageMissing: " + targetThreshhold.toString());
        }
      }
    }
    String name = Observing.add(this, (ObserverCallBack) observer, obsType, targetThreshhold);
    log(lvl, "%s: observer %s %s: %s with: %s", toStringShort(), obsType,
            (observer == null ? "" : " with callback"), name, targetThreshhold);
    return name;
  }

  /**
   * a subsequently started observer in this region should wait for the target to vanish and notify the given observer
   * about this event<br>
   * for details about the observe event handler: {@link ObserverCallBack}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   *
   * @param <PSI>    Pattern, String or Image
   * @param target   Pattern, String or Image
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public <PSI> String onVanish(PSI target, Object observer) {
    return onEvent(target, observer, ObserveEvent.Type.VANISH);
  }

  /**
   * a subsequently started observer in this region should wait for the target to vanish success and details about the
   * event can be obtained using @{link Observing}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}<br>
   *
   * @param <PSI>  Pattern, String or Image
   * @param target Pattern, String or Image
   * @return the event's name
   */
  public <PSI> String onVanish(PSI target) {
    return onEvent(target, null, ObserveEvent.Type.VANISH);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region and notify the given observer
   * about this event for details about the observe event handler: {@link ObserverCallBack} for details about
   * APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @param threshold minimum size of changes (rectangle threshhold x threshold)
   * @param observer  ObserverCallBack
   * @return the event's name
   */
  public String onChange(Integer threshold, Object observer) {
    return onEvent((threshold > 0 ? threshold : Settings.ObserveMinChangedPixels),
            observer, ObserveEvent.Type.CHANGE);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region success and details about the
   * event can be obtained using @{link Observing}<br>
   * for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @param threshold minimum size of changes (rectangle threshhold x threshold)
   * @return the event's name
   */
  public String onChange(Integer threshold) {
    return onEvent((threshold > 0 ? threshold : Settings.ObserveMinChangedPixels),
            null, ObserveEvent.Type.CHANGE);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region and notify the given observer
   * about this event <br>
   * minimum size of changes used: Settings.ObserveMinChangedPixels for details about the observe event handler:
   * {@link ObserverCallBack} for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @param observer ObserverCallBack
   * @return the event's name
   */
  public String onChange(Object observer) {
    return onEvent(Settings.ObserveMinChangedPixels, observer, ObserveEvent.Type.CHANGE);
  }

  /**
   * a subsequently started observer in this region should wait for changes in the region success and details about the
   * event can be obtained using @{link Observing}<br>
   * minimum size of changes used: Settings.ObserveMinChangedPixels for details about APPEAR/VANISH/CHANGE events:
   * {@link ObserveEvent}
   *
   * @return the event's name
   */
  public String onChange() {
    return onEvent(Settings.ObserveMinChangedPixels, null, ObserveEvent.Type.CHANGE);
  }

  //<editor-fold defaultstate="collapsed" desc="obsolete">
//	/**
//	 *INTERNAL USE ONLY: for use with scripting API bridges
//	 * @param <PSI> Pattern, String or Image
//	 * @param target Pattern, String or Image
//	 * @param observer ObserverCallBack
//	 * @return the event's name
//	 */
//	public <PSI> String onAppearJ(PSI target, Object observer) {
//		return onEvent(target, observer, ObserveEvent.Type.APPEAR);
//	}
//
//	/**
//	 *INTERNAL USE ONLY: for use with scripting API bridges
//	 * @param <PSI> Pattern, String or Image
//	 * @param target Pattern, String or Image
//	 * @param observer ObserverCallBack
//	 * @return the event's name
//	 */
//	public <PSI> String onVanishJ(PSI target, Object observer) {
//		return onEvent(target, observer, ObserveEvent.Type.VANISH);
//	}
//
//	/**
//	 *INTERNAL USE ONLY: for use with scripting API bridges
//	 * @param threshold min pixel size - 0 = ObserveMinChangedPixels
//	 * @param observer ObserverCallBack
//	 * @return the event's name
//	 */
//	public String onChangeJ(int threshold, Object observer) {
//		return onEvent( (threshold > 0 ? threshold : Settings.ObserveMinChangedPixels),
//						observer, ObserveEvent.Type.CHANGE);
//	}
//
//</editor-fold>

  public String onChangeDo(Integer threshold, Object observer) {
    String name = Observing.add(this, (ObserverCallBack) observer, ObserveEvent.Type.CHANGE, threshold);
    log(lvl, "%s: onChange%s: %s minSize: %d", toStringShort(),
            (observer == null ? "" : " with callback"), name, threshold);
    return name;
  }

  /**
   * start an observer in this region that runs forever (use stopObserving() in handler) for details about the observe
   * event handler: {@link ObserverCallBack} for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @return false if not possible, true if events have happened
   */
  public boolean observe() {
    return observe(Float.POSITIVE_INFINITY);
  }

  /**
   * start an observer in this region for the given time for details about the observe event handler:
   * {@link ObserverCallBack} for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @param secs time in seconds the observer should run
   * @return false if not possible, true if events have happened
   */
  public boolean observe(double secs) {
    return observeDo(secs);
  }

  /**
   * INTERNAL USE ONLY: for use with scripting API bridges
   *
   * @param secs time in seconds the observer should run
   * @return false if not possible, true if events have happened
   */
  public boolean observeInLine(double secs) {
    return observeDo(secs);
  }

  private boolean observeDo(double secs) {
    if (regionObserver == null) {
      Debug.error("Region: observe: Nothing to observe (Region might be invalid): " + this.toStringShort());
      return false;
    }
    if (observing) {
      if (!observingInBackground) {
        Debug.error("Region: observe: already running for this region. Only one allowed!");
        return false;
      }
    }
    log(lvl, "observe: starting in " + this.toStringShort() + " for " + secs + " seconds");
    int MaxTimePerScan = (int) (1000.0 / observeScanRate);
    long begin_t = (new Date()).getTime();
    long stop_t;
    if (secs > Long.MAX_VALUE) {
      stop_t = Long.MAX_VALUE;
    } else {
      stop_t = begin_t + (long) (secs * 1000);
    }
    regionObserver.initialize();
    observing = true;
    Observing.addRunningObserver(this);
    while (observing && stop_t > (new Date()).getTime()) {
      long before_find = (new Date()).getTime();
      ScreenImage simg = getScreen().capture(x, y, w, h);
      if (!regionObserver.update(simg)) {
        observing = false;
        break;
      }
      if (!observing) {
        break;
      }
      long after_find = (new Date()).getTime();
      try {
        if (after_find - before_find < MaxTimePerScan) {
          Thread.sleep((int) (MaxTimePerScan - (after_find - before_find)));
        }
      } catch (Exception e) {
      }
    }
    boolean observeSuccess = false;
    if (observing) {
      observing = false;
      log(lvl, "observe: stopped due to timeout in "
              + this.toStringShort() + " for " + secs + " seconds");
    } else {
      log(lvl, "observe: ended successfully: " + this.toStringShort());
      observeSuccess = Observing.hasEvents(this);
    }
    return observeSuccess;
  }

  /**
   * start an observer in this region for the given time that runs in background - for details about the observe event
   * handler: {@link ObserverCallBack} for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @param secs time in seconds the observer should run
   * @return false if not possible, true otherwise
   */
  public boolean observeInBackground(double secs) {
    if (observing) {
      Debug.error("Region: observeInBackground: already running for this region. Only one allowed!");
      return false;
    }
    observing = true;
    observingInBackground = true;
    Thread observeThread = new Thread(new ObserverThread(secs));
    observeThread.start();
    log(lvl, "observeInBackground now running");
    return true;
  }

  /**
   * start an observer in this region that runs in background forever - for details about the observe event
   * handler: {@link ObserverCallBack} for details about APPEAR/VANISH/CHANGE events: {@link ObserveEvent}
   *
   * @return false if not possible, true otherwise
   */
  public boolean observeInBackground() {
    return observeInBackground(Double.MAX_VALUE);
  }

  private class ObserverThread implements Runnable {

    private double time;

    ObserverThread(double time) {
      this.time = time;
    }

    @Override
    public void run() {
      observeDo(time);
    }
  }

  /**
   * stops a running observer
   */
  public void stopObserver() {
    log(lvl, "observe: request to stop observer for " + this.toStringShort());
    observing = false;
    observingInBackground = false;
  }

  /**
   * stops a running observer printing an info message
   *
   * @param message text
   */
  public void stopObserver(String message) {
    Debug.info(message);
    stopObserver();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Mouse actions - clicking">
  protected Location checkMatch() {
    if (lastMatch != null) {
      return lastMatch.getTarget();
    }
    return getTarget();
  }

  /**
   * move the mouse pointer to region's last successful match <br>use center if no lastMatch <br>
   * if region is a match: move to targetOffset <br>same as mouseMove
   *
   * @return 1 if possible, 0 otherwise
   */
  public int hover() {
    try { // needed to cut throw chain for FindFailed
      return hover(checkMatch());
    } catch (FindFailed ex) {
    }
    return 0;
  }

  /**
   * move the mouse pointer to the given target location<br> same as mouseMove<br> Pattern or Filename - do a find
   * before and use the match<br> Region - position at center<br> Match - position at match's targetOffset<br> Location
   * - position at that point<br>
   *
   * @param <PFRML> to search: Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int hover(PFRML target) throws FindFailed {
    log(lvl, "hover: " + target);
    return mouseMove(target);
  }

  /**
   * left click at the region's last successful match <br>use center if no lastMatch <br>if region is a match: click
   * targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int click() {
    try { // needed to cut throw chain for FindFailed
      return click(checkMatch(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * left click at the given target location<br> Pattern or Filename - do a find before and use the match<br> Region -
   * position at center<br> Match - position at match's targetOffset<br>
   * Location - position at that point<br>
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
   * left click at the given target location<br> holding down the given modifier keys<br>
   * Pattern or Filename - do a find before and use the match<br> Region - position at center<br>
   * Match - position at match's targetOffset<br> Location - position at that point<br>
   *
   * @param <PFRML>   to search: Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int click(PFRML target, Integer modifiers) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = 0;
    if (null != loc) {
      ret = Mouse.click(loc, InputEvent.BUTTON1_MASK, modifiers, false, this);
    }
    //TODO      SikuliActionManager.getInstance().clickTarget(this, target, _lastScreenImage, _lastMatch);
    return ret;
  }

  /**
   * double click at the region's last successful match <br>use center if no lastMatch <br>if region is a match: click
   * targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int doubleClick() {
    try { // needed to cut throw chain for FindFailed
      return doubleClick(checkMatch(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * double click at the given target location<br> Pattern or Filename - do a find before and use the match<br> Region -
   * position at center<br> Match - position at match's targetOffset<br>
   * Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int doubleClick(PFRML target) throws FindFailed {
    return doubleClick(target, 0);
  }

  /**
   * double click at the given target location<br> holding down the given modifier keys<br>
   * Pattern or Filename - do a find before and use the match<br> Region - position at center<br > Match - position at
   * match's targetOffset<br> Location - position at that point<br>
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int doubleClick(PFRML target, Integer modifiers) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = 0;
    if (null != loc) {
      ret = Mouse.click(loc, InputEvent.BUTTON1_MASK, modifiers, true, this);
    }
    //TODO      SikuliActionManager.getInstance().doubleClickTarget(this, target, _lastScreenImage, _lastMatch);
    return ret;
  }

  /**
   * right click at the region's last successful match <br>use center if no lastMatch <br>if region is a match: click
   * targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int rightClick() {
    try { // needed to cut throw chain for FindFailed
      return rightClick(checkMatch(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * right click at the given target location<br> Pattern or Filename - do a find before and use the match<br> Region -
   * position at center<br> Match - position at match's targetOffset<br > Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int rightClick(PFRML target) throws FindFailed {
    return rightClick(target, 0);
  }

  /**
   * right click at the given target location<br> holding down the given modifier keys<br>
   * Pattern or Filename - do a find before and use the match<br> Region - position at center<br > Match - position at
   * match's targetOffset<br> Location - position at that point<br>
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int rightClick(PFRML target, Integer modifiers) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = 0;
    if (null != loc) {
      ret = Mouse.click(loc, InputEvent.BUTTON3_MASK, modifiers, false, this);
    }
    //TODO      SikuliActionManager.getInstance().rightClickTarget(this, target, _lastScreenImage, _lastMatch);
    return ret;
  }

  /**
   * time in milliseconds to delay between button down/up at next click only (max 1000)
   *
   * @param millisecs value
   */
  public void delayClick(int millisecs) {
    Settings.ClickDelay = millisecs;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Mouse actions - drag & drop">

  /**
   * Drag from region's last match and drop at given target <br>applying Settings.DelayAfterDrag and DelayBeforeDrop
   * <br> using left mouse button
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int dragDrop(PFRML target) throws FindFailed {
    return dragDrop(lastMatch, target);
  }

  /**
   * Drag from a position and drop to another using left mouse button<br>applying Settings.DelayAfterDrag and
   * DelayBeforeDrop
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param t1      source position
   * @param t2      destination position
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int dragDrop(PFRML t1, PFRML t2) throws FindFailed {
    Location loc1 = getLocationFromTarget(t1);
    Location loc2 = getLocationFromTarget(t2);
    int retVal = 0;
    if (loc1 != null && loc2 != null) {
      IRobot r1 = loc1.getRobotForPoint("drag");
      IRobot r2 = loc2.getRobotForPoint("drop");
      if (r1 != null && r2 != null) {
        Mouse.use(this);
        r1.smoothMove(loc1);
        r1.delay((int) (Settings.DelayBeforeMouseDown * 1000));
        r1.mouseDown(InputEvent.BUTTON1_MASK);
        double DelayBeforeDrag = Settings.DelayBeforeDrag;
        if (DelayBeforeDrag < 0.0) {
          DelayBeforeDrag = Settings.DelayAfterDrag;
        }
        r1.delay((int) (DelayBeforeDrag * 1000));
        r2.smoothMove(loc2);
        r2.delay((int) (Settings.DelayBeforeDrop * 1000));
        r2.mouseUp(InputEvent.BUTTON1_MASK);
        Mouse.let(this);
        retVal = 1;
      }
    }
    Settings.DelayBeforeMouseDown = Settings.DelayValue;
    Settings.DelayAfterDrag = Settings.DelayValue;
    Settings.DelayBeforeDrag = -Settings.DelayValue;
    Settings.DelayBeforeDrop = Settings.DelayValue;
    return retVal;
  }

  /**
   * Prepare a drag action: move mouse to given target <br>press and hold left mouse button <br >wait
   * Settings.DelayAfterDrag
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int drag(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int retVal = 0;
    if (loc != null) {
      IRobot r = loc.getRobotForPoint("drag");
      if (r != null) {
        Mouse.use(this);
        r.smoothMove(loc);
        r.delay((int) (Settings.DelayBeforeMouseDown * 1000));
        r.mouseDown(InputEvent.BUTTON1_MASK);
        double DelayBeforeDrag = Settings.DelayBeforeDrag;
        if (DelayBeforeDrag < 0.0) {
          DelayBeforeDrag = Settings.DelayAfterDrag;
        }
        r.delay((int) (DelayBeforeDrag * 1000));
        r.waitForIdle();
        Mouse.let(this);
        retVal = 1;
      }
    }
    Settings.DelayBeforeMouseDown = Settings.DelayValue;
    Settings.DelayAfterDrag = Settings.DelayValue;
    Settings.DelayBeforeDrag = -Settings.DelayValue;
    Settings.DelayBeforeDrop = Settings.DelayValue;
    return retVal;
  }

  /**
   * finalize a drag action with a drop: move mouse to given target <br>
   * wait Settings.DelayBeforeDrop <br>
   * before releasing the left mouse button
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int dropAt(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int retVal = 0;
    if (loc != null) {
      IRobot r = loc.getRobotForPoint("drag");
      if (r != null) {
        Mouse.use(this);
        r.smoothMove(loc);
        r.delay((int) (Settings.DelayBeforeDrop * 1000));
        r.mouseUp(InputEvent.BUTTON1_MASK);
        r.waitForIdle();
        Mouse.let(this);
        retVal = 1;
      }
    }
    Settings.DelayBeforeMouseDown = Settings.DelayValue;
    Settings.DelayAfterDrag = Settings.DelayValue;
    Settings.DelayBeforeDrag = -Settings.DelayValue;
    Settings.DelayBeforeDrop = Settings.DelayValue;
    return retVal;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Mouse actions - low level + Wheel">

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
   * move the mouse pointer to the region's last successful match<br>same as hover<br>
   *
   * @return 1 if possible, 0 otherwise
   */
  public int mouseMove() {
    if (lastMatch != null) {
      try {
        return mouseMove(lastMatch);
      } catch (FindFailed ex) {
        return 0;
      }
    }
    return 0;
  }

  /**
   * move the mouse pointer to the given target location<br> same as hover<br> Pattern or Filename - do a find before
   * and use the match<br> Region - position at center<br> Match - position at match's targetOffset<br>
   * Location - position at that point<br>
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed for Pattern or Filename
   */
  public <PFRML> int mouseMove(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    int ret = 0;
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

  /**
   * Move the wheel at the current mouse position<br> the given steps in the given direction: <br >Button.WHEEL_DOWN,
   * Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @return 1 in any case
   */
  public int wheel(int direction, int steps) {
    Mouse.wheel(direction, steps, this);
    return 1;
  }

  /**
   * move the mouse pointer to the given target location<br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location target
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps) throws FindFailed {
    return wheel(target, direction, steps, Mouse.WHEEL_STEP_DELAY);
  }

  /**
   * move the mouse pointer to the given target location<br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location target
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param stepDelay number of miliseconds to wait when incrementing the step value
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps, int stepDelay) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    if (loc != null) {
      Mouse.use(this);
      Mouse.keep(this);
      Mouse.move(loc, this);
      Mouse.wheel(direction, steps, this, stepDelay);
      Mouse.let(this);
      return 1;
    }
    return 0;
  }

  /**
   * @return current location of mouse pointer
   * @deprecated use {@link Mouse#at()} instead
   */
  @Deprecated
  public static Location atMouse() {
    return Mouse.at();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Keyboard actions + paste">

  /**
   * press and hold the given key use a constant from java.awt.event.KeyEvent which might be special in the current
   * machine/system environment
   *
   * @param keycode Java KeyCode
   */
  public void keyDown(int keycode) {
    getRobotForRegion().keyDown(keycode);
  }

  /**
   * press and hold the given keys including modifier keys <br>use the key constants defined in class Key, <br>which
   * only provides a subset of a US-QWERTY PC keyboard layout <br>might be mixed with simple characters
   * <br>use + to concatenate Key constants
   *
   * @param keys valid keys
   */
  public void keyDown(String keys) {
    getRobotForRegion().keyDown(keys);
  }

  /**
   * release all currently pressed keys
   */
  public void keyUp() {
    getRobotForRegion().keyUp();
  }

  /**
   * release the given keys (see keyDown(keycode) )
   *
   * @param keycode Java KeyCode
   */
  public void keyUp(int keycode) {
    getRobotForRegion().keyUp(keycode);
  }

  /**
   * release the given keys (see keyDown(keys) )
   *
   * @param keys valid keys
   */
  public void keyUp(String keys) {
    getRobotForRegion().keyUp(keys);
  }

  /**
   * Compact alternative for type() with more options <br>
   * - special keys and options are coded as #XN. or #X+ or #X- <br>
   * where X is a refrence for a special key and N is an optional repeat factor <br>
   * A modifier key as #X. modifies the next following key<br>
   * the trailing . ends the special key, the + (press and hold) or - (release) does the same, <br>
   * but signals press-and-hold or release additionally.<br>
   * except #W / #w all special keys are not case-sensitive<br>
   * a #wn. inserts a wait of n millisecs or n secs if n less than 60 <br>
   * a #Wn. sets the type delay for the following keys (must be &gt; 60 and denotes millisecs) - otherwise taken as
   * normal wait<br>
   * Example: wait 2 secs then type CMD/CTRL - N then wait 1 sec then type DOWN 3 times<br>
   * Windows/Linux: write("#w2.#C.n#W1.#d3.")<br>
   * Mac: write("#w2.#M.n#W1.#D3.")<br>
   * for more details about the special key codes and examples consult the docs <br>
   *
   * @param text a coded text interpreted as a series of key actions (press/hold/release)
   * @return 0 for success 1 otherwise
   */
  public int write(String text) {
    Debug.info("Write: " + text);
    char c;
    String token, tokenSave;
    String modifier = "";
    int k;
    IRobot robot = getRobotForRegion();
    int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
    Settings.TypeDelay = 0.0;
    robot.typeStarts();
    for (int i = 0; i < text.length(); i++) {
      log(lvl + 1, "write: (%d) %s", i, text.substring(i));
      c = text.charAt(i);
      token = null;
      boolean isModifier = false;
      if (c == '#') {
        if (text.charAt(i + 1) == '#') {
          log(lvl, "write at: %d: %s", i, c);
          i += 1;
          continue;
        }
        if (text.charAt(i + 2) == '+' || text.charAt(i + 2) == '-') {
          token = text.substring(i, i + 3);
          isModifier = true;
        } else if (-1 < (k = text.indexOf('.', i))) {
          if (k > -1) {
            token = text.substring(i, k + 1);
            if (token.length() > Key.keyMaxLength || token.substring(1).contains("#")) {
              token = null;
            }
          }
        }
      }
      Integer key = -1;
      if (token == null) {
        log(lvl + 1, "write: %d: %s", i, c);
      } else {
        log(lvl + 1, "write: token at %d: %s", i, token);
        int repeat = 0;
        if (token.toUpperCase().startsWith("#W")) {
          if (token.length() > 3) {
            i += token.length() - 1;
            int t = 0;
            try {
              t = Integer.parseInt(token.substring(2, token.length() - 1));
            } catch (NumberFormatException ex) {
            }
            if ((token.startsWith("#w") && t > 60)) {
              pause = 20 + (t > 1000 ? 1000 : t);
              log(lvl + 1, "write: type delay: " + t);
            } else {
              log(lvl + 1, "write: wait: " + t);
              robot.delay((t < 60 ? t * 1000 : t));
            }
            continue;
          }
        }
        tokenSave = token;
        token = token.substring(0, 2).toUpperCase() + ".";
        if (Key.isRepeatable(token)) {
          try {
            repeat = Integer.parseInt(tokenSave.substring(2, tokenSave.length() - 1));
          } catch (NumberFormatException ex) {
            token = tokenSave;
          }
        } else if (tokenSave.length() == 3 && Key.isModifier(tokenSave.toUpperCase())) {
          i += tokenSave.length() - 1;
          modifier += tokenSave.substring(1, 2).toUpperCase();
          continue;
        } else {
          token = tokenSave;
        }
        if (-1 < (key = Key.toJavaKeyCodeFromText(token))) {
          if (repeat > 0) {
            log(lvl + 1, "write: %s Repeating: %d", token, repeat);
          } else {
            log(lvl + 1, "write: %s", tokenSave);
            repeat = 1;
          }
          i += tokenSave.length() - 1;
          if (isModifier) {
            if (tokenSave.endsWith("+")) {
              robot.keyDown(key);
            } else {
              robot.keyUp(key);
            }
            continue;
          }
          if (repeat > 1) {
            for (int n = 0; n < repeat; n++) {
              robot.typeKey(key.intValue());
            }
            continue;
          }
        }
      }
      if (!modifier.isEmpty()) {
        log(lvl + 1, "write: modifier + " + modifier);
        for (int n = 0; n < modifier.length(); n++) {
          robot.keyDown(Key.toJavaKeyCodeFromText(String.format("#%s.", modifier.substring(n, n + 1))));
        }
      }
      if (key > -1) {
        robot.typeKey(key.intValue());
      } else {
        robot.typeChar(c, IRobot.KeyMode.PRESS_RELEASE);
      }
      if (!modifier.isEmpty()) {
        log(lvl + 1, "write: modifier - " + modifier);
        for (int n = 0; n < modifier.length(); n++) {
          robot.keyUp(Key.toJavaKeyCodeFromText(String.format("#%s.", modifier.substring(n, n + 1))));
        }
      }
      robot.delay(pause);
      modifier = "";
    }

    robot.typeEnds();
    robot.waitForIdle();
    return 0;
  }

  /**
   * enters the given text one character/key after another using keyDown/keyUp
   * <br>about the usable Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC
   * keyboard layout<br>the text is entered at the current position of the focus/carret
   *
   * @param text containing characters and/or Key constants
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text) {
    try {
      return keyin(null, text, 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * enters the given text one character/key after another using keyDown/keyUp<br>while holding down the given modifier
   * keys <br>about the usable Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC
   * keyboard layout<br>the text is entered at the current position of the focus/carret
   *
   * @param text      containing characters and/or Key constants
   * @param modifiers constants according to class KeyModifiers
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text, int modifiers) {
    try {
      return keyin(null, text, modifiers);
    } catch (FindFailed findFailed) {
      return 0;
    }
  }

  /**
   * enters the given text one character/key after another using
   * <p>
   * keyDown/keyUp<br>while holding down the given modifier keys <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout<br>the text is entered at the current
   * position of the focus/carret
   *
   * @param text      containing characters and/or Key constants
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text, String modifiers) {
    String target = null;
    int modifiersNew = Key.convertModifiers(modifiers);
    if (modifiersNew == 0) {
      target = text;
      text = modifiers;
    }
    try {
      return keyin(target, text, modifiersNew);
    } catch (FindFailed findFailed) {
      return 0;
    }
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br>enters the given text one
   * character/key after another using keyDown/keyUp <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @param text    containing characters and/or Key constants
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text) throws FindFailed {
    return keyin(target, text, 0);
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br>enters the given text one
   * character/key after another using keyDown/keyUp <br>while holding down the given modifier keys<br>about the usable
   * Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param text      containing characters and/or Key constants
   * @param modifiers constants according to class KeyModifiers
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text, int modifiers) throws FindFailed {
    return keyin(target, text, modifiers);
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br>enters the given text one
   * character/key after another using keyDown/keyUp <br>while holding down the given modifier keys<br>about the usable
   * Key constants see keyDown(keys) <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param text      containing characters and/or Key constants
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text, String modifiers) throws FindFailed {
    int modifiersNew = Key.convertModifiers(modifiers);
    return keyin(target, text, modifiersNew);
  }

  private <PFRML> int keyin(PFRML target, String text, int modifiers) throws FindFailed {
    if (target != null && 0 == click(target, 0)) {
      return 0;
    }
    Debug profiler = Debug.startTimer("Region.type");
    if (text != null && !"".equals(text)) {
      String showText = "";
      for (int i = 0; i < text.length(); i++) {
        showText += Key.toJavaKeyCodeText(text.charAt(i));
      }
      String modText = "";
      String modWindows = null;
      if ((modifiers & KeyModifier.WIN) != 0) {
        modifiers -= KeyModifier.WIN;
        modifiers |= KeyModifier.META;
        log(lvl, "Key.WIN as modifier");
        modWindows = "Windows";
      }
      if (modifiers != 0) {
        modText = String.format("( %s ) ", KeyEvent.getKeyModifiersText(modifiers));
        if (modWindows != null) {
          modText = modText.replace("Meta", modWindows);
        }
      }
      Debug.action("%s TYPE \"%s\"", modText, showText);
      log(lvl, "%s TYPE \"%s\"", modText, showText);
      profiler.lap("before getting Robot");
      IRobot r = getRobotForRegion();
      int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
      Settings.TypeDelay = 0.0;
      profiler.lap("before typing");
      r.typeStarts();
      for (int i = 0; i < text.length(); i++) {
        r.pressModifiers(modifiers);
        r.typeChar(text.charAt(i), IRobot.KeyMode.PRESS_RELEASE);
        r.releaseModifiers(modifiers);
        r.delay(pause);
      }
      r.typeEnds();
      profiler.lap("after typing, before waitForIdle");
      r.waitForIdle();
      profiler.end();
      return 1;
    }

    return 0;
  }

  /**
   * time in milliseconds to delay between each character at next type only (max 1000)
   *
   * @param millisecs value
   */
  public void delayType(int millisecs) {
    Settings.TypeDelay = millisecs;
  }

  /**
   * pastes the text at the current position of the focus/carret <br>using the clipboard and strg/ctrl/cmd-v (paste
   * keyboard shortcut)
   *
   * @param text a string, which might contain unicode characters
   * @return 0 if possible, 1 otherwise
   */
  public int paste(String text) {
    try {
      return paste(null, text);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret <br> and then pastes the text <br>
   * using the clipboard and strg/ctrl/cmd-v (paste keyboard shortcut)
   *
   * @param <PFRML> Pattern, Filename, Text, Region, Match or Location target
   * @param target  Pattern, Filename, Text, Region, Match or Location
   * @param text    a string, which might contain unicode characters
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int paste(PFRML target, String text) throws FindFailed {
    if (target != null && 0 == click(target, 0)) {
      return 0;
    }
    if (text != null) {
      App.setClipboard(text);
      int mod = Key.getHotkeyModifier();
      IRobot r = getRobotForRegion();
      r.keyDown(mod);
      r.keyDown(KeyEvent.VK_V);
      r.keyUp(KeyEvent.VK_V);
      r.keyUp(mod);
      return 1;
    }
    return 0;
  }
  //</editor-fold>

  //<editor-fold desc="Mobile actions (Android)">
  private ADBDevice adbDevice = null;
  private ADBScreen adbScreen = null;

  private boolean isAndroid() {
    if (isOtherScreen()) {
      IScreen scr = getScreen();
      if (scr instanceof ADBScreen) {
        adbScreen = (ADBScreen) scr;
        adbDevice = adbScreen.getDevice();
        return true;
      }
    }
    return false;
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   *
   * @param <PFRML> Pattern, String, Image, Match, Region or Location
   * @param target  PFRML
   * @throws FindFailed image not found
   */
  public <PFRML> void aTap(PFRML target) throws FindFailed {
    if (isAndroid() && adbDevice != null) {
      Location loc = getLocationFromTarget(target);
      if (loc != null) {
        adbDevice.tap(loc.x, loc.y);
        RunTime.pause(adbScreen.waitAfterAction);
      }
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   *
   * @param text text
   */
  public void aInput(String text) {
    if (isAndroid() && adbDevice != null) {
      adbDevice.input(text);
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   *
   * @param key key
   */
  public void aKey(int key) {
    if (isAndroid() && adbDevice != null) {
      adbDevice.inputKeyEvent(key);
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   *
   * @param <PFRML> Pattern, String, Image, Match, Region or Location
   * @param from    PFRML
   * @param to      PFRML
   * @throws FindFailed image not found
   */
  public <PFRML> void aSwipe(PFRML from, PFRML to) throws FindFailed {
    if (isAndroid() && adbDevice != null) {
      Location locFrom = getLocationFromTarget(from);
      Location locTo = getLocationFromTarget(to);
      if (locFrom != null && locTo != null) {
        adbDevice.swipe(locFrom.x, locFrom.y, locTo.x, locTo.y);
        RunTime.pause(adbScreen.waitAfterAction);
      }
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   */
  public void aSwipeUp() {
    int midX = (int) (w / 2);
    int swipeStep = (int) (h / 5);
    try {
      aSwipe(new Location(midX, h - swipeStep), new Location(midX, swipeStep));
    } catch (FindFailed findFailed) {
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   */
  public void aSwipeDown() {
    int midX = (int) (w / 2);
    int swipeStep = (int) (h / 5);
    try {
      aSwipe(new Location(midX, swipeStep), new Location(midX, h - swipeStep));
    } catch (FindFailed findFailed) {
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   */
  public void aSwipeLeft() {
    int midY = (int) (h / 2);
    int swipeStep = (int) (w / 5);
    try {
      aSwipe(new Location(w - swipeStep, midY), new Location(swipeStep, midY));
    } catch (FindFailed findFailed) {
    }
  }

  /**
   * EXPERIMENTAL: for Android over ADB
   */
  public void aSwipeRight() {
    int midY = (int) (h / 2);
    int swipeStep = (int) (w / 5);
    try {
      aSwipe(new Location(swipeStep, midY), new Location(w - swipeStep, midY));
    } catch (FindFailed findFailed) {
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="OCR - read text from Screen">

  /**
   * STILL EXPERIMENTAL: tries to read the text in this region<br> might contain misread characters, NL characters and
   * other stuff, when interpreting contained grafics as text<br>
   * Best results: one line of text with no grafics in the line
   *
   * @return the text read (utf8 encoded)
   */
  public String text() {
    if (Settings.OcrTextRead) {
      ScreenImage simg = getScreen().capture(x, y, w, h);
      TextRecognizer tr = TextRecognizer.getInstance();
      if (tr == null) {
        Debug.error("text: text recognition is now switched off");
        return "--- no text ---";
      }
      String textRead = tr.recognize(simg);
      log(lvl, "text: #(" + textRead + ")#");
      return textRead;
    }
    Debug.error("text: text recognition is currently switched off");
    return "--- no text ---";
  }

  /**
   * VERY EXPERIMENTAL: returns a list of matches, that represent single words, that have been found in this region<br>
   * the match's x,y,w,h the region of the word<br> Match.getText() returns the word (utf8) at this match<br>
   * Match.getScore() returns a value between 0 ... 1, that represents some OCR-confidence value<br > (the higher, the
   * better the OCR engine thinks the result is)
   *
   * @return a list of matches
   */
  public List<Match> listText() {
    if (Settings.OcrTextRead) {
      ScreenImage simg = getScreen().capture(x, y, w, h);
      TextRecognizer tr = TextRecognizer.getInstance();
      if (tr == null) {
        Debug.error("text: text recognition is now switched off");
        return null;
      }
      log(lvl, "listText: scanning %s", this);
      return tr.listText(simg, this);
    }
    Debug.error("text: text recognition is currently switched off");
    return null;
  }
  //</editor-fold>
}
