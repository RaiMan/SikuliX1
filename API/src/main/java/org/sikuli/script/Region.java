/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Observer;
import org.sikuli.script.support.*;
import org.sikuli.script.support.devices.HelpDevice;
import org.sikuli.util.Highlight;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A Region is a rectengular area on a screen.
 * <br>
 * <p>completely contained in that screen (no screen overlapping)</p>
 * NOTES:
 * <br>- when needed (find ops), the pixel content is captured from the screen
 * <br>- if nothing else is said, the center pixel is the target for mouse actions
 */
public class Region extends Element {

  public static final String logName = "Region: ";

  //<editor-fold desc="000 for Python">
  public static Region getDefaultInstance4py() {
    return new Screen();
  }

  public static Region make4py(ArrayList args) {
    log(3, "make: args: %s", args);
    Region reg = new Screen();
    if (null != args) {
      int argn = 1;
      for (Object arg : args) {
        log(3, "%d: %s (%s)", argn++, arg.getClass().getSimpleName(), arg);
      }
      if (args.size() == 4) {
        //case1: Region(x,y,w,h)
        int num = 4;
        for (Object arg : args) {
          if (arg instanceof Integer) {
            num--;
          }
        }
        if (num == 0) {
          reg = create((Integer) args.get(0), (Integer) args.get(1), (Integer) args.get(2), (Integer) args.get(3));
        }
      } else if (args.size() == 1 && args.get(0) instanceof Region) {
        //case2: Region(Region)
        reg = create((Region) args.get(0));
      }
    }
    return reg;
  }
  //</editor-fold>

  //<editor-fold desc="001 Fields x, y, w, h">

  /**
   * set the horizontal position of the top-left corner
   *
   * @param X new x-value of top-left corner
   * @return this Region
   */
  public Region setX(int X) {
    x = X;
    initScreen(null); //setX
    return this;
  }

  /**
   * @param Y new y position of top left corner
   * @return this Region
   */
  public Region setY(int Y) {
    y = Y;
    initScreen(null); //setY
    return this;
  }

  /**
   * @param W new width
   * @return this Region
   */
  public Region setW(int W) {
    w = W > 1 ? W : 1;
    initScreen(null); //setW
    return this;
  }

  /**
   * @param H new height
   * @return this Region
   */
  public Region setH(int H) {
    h = H > 1 ? H : 1;
    initScreen(null); //setH
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="004 housekeeping">
  private boolean isScreenUnion = false;
  private boolean isVirtual = false;

  /**
   * The Screen containing the Region
   */
  private IScreen scr;
  protected boolean otherScreen = false;

  protected static Region getFakeRegion() {
    if (fakeRegion == null) {
      fakeRegion = new Region(0, 0, 5, 5);
    }
    return fakeRegion;
  }

  private static Region fakeRegion;

  /**
   * {@inheritDoc}
   *
   * @return the description
   */
  @Override
  public String toString() {
    String scrText = getScreen() == null ? "?" :
        "" + (-1 == getScreen().getID() ? "Union" : "" + getScreen().getID());
    if (isOtherScreen()) {
      scrText = getScreen().getIDString();
    }
    String nameText = "";
    if (!getName().isEmpty()) {
      nameText = " (" + getName() + ")";
    }
    if (scrText.equals("?")) {
      scrText = "@OUTSIDE";
    } else {
      scrText = "@S(" + scrText + ")";
    }
    return String.format("R[%d,%d %dx%d%s]%s", x, y, w, h, nameText, scrText);
  }

  /**
   * @return a compact description
   */
  public String toStringShort() {
    return toString();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="005 Init & special use">

  public Image getImage() {
    long before = new Date().getTime();
    ScreenImage capture = getScreen().capture(x, y, w, h);
    long after = new Date().getTime();
    long when = before + (after - before) / 2;
    capture.setTimeCreated(when);
    return new Image(capture);
  }

  /**
   * INTERNAL USE
   *
   * @param iscr screen
   */
  public void initScreen(IScreen iscr) {
    doInitScreen(iscr, false);
  }

  void doInitScreen(IScreen iscr, boolean silent) {
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
      if (!silent) {
        Debug.error("Region(%d,%d,%d,%d) outside any screen - actions might not work", x, y, w, h);
      }
    }
  }

  public void resetScreen() {
    Screen scr = (Screen) getScreen();
    scr.reset();
  }

  private Location checkAndSetRemote(Location loc) {
    if (!isOtherScreen()) {
      return loc;
    }
    return loc.setOtherScreen(getScreen());
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
   * INTERNAL: flags this region as belonging to a non-Desktop screen
   *
   * @param aScreen screen
   * @return this Region
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
    return getScreen() != null && w != 0 && h != 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="002 Constructors">

  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region() {
    rows = 0;
  }

  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region(boolean isScreenUnion) {
    this.isScreenUnion = isScreenUnion;
    this.rows = 0;
  }

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
    initScreen(parentScreen); //Region(int X, int Y, int W, int H, IScreen parentScreen)
  }

  public Region(int X, int Y, int W, int H, IScreen parentScreen, boolean silent) {
    this.rows = 0;
    this.x = X;
    this.y = Y;
    this.w = W > 1 ? W : 1;
    this.h = H > 1 ? H : 1;
    doInitScreen(parentScreen, silent); //Region(int X, int Y, int W, int H, IScreen parentScreen)
  }

  /**
   * Convenience: a minimal Region to be used as a Point
   *
   * @param X top left x
   * @param Y top left y
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

  public Region(Rectangle r, boolean silent) {
    this(r.x, r.y, r.width, r.height, null,  silent);
    this.rows = 0;
  }

  /**
   * Create a new region from another region.
   * including the region's settings
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
    findFailedHandler = r.findFailedHandler;
    throwException = r.throwException;
    waitScanRate = r.waitScanRate;
    observeScanRate = r.observeScanRate;
    repeatWaitTime = r.repeatWaitTime;
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
  public static Region create(int X, int Y, int W, int H, IScreen scr) {
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
  public final static int LEFT = 0;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the right corner of
   * the new Region.
   */
  public final static int RIGHT = 1;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the top corner of the
   * new Region.
   */
  public final static int TOP = 0;
  /**
   * Flag for the {@link #create(Location, int, int, int, int)} method. Sets the Location to be on the bottom corner of
   * the new Region.
   */
  public final static int BOTTOM = 1;

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
    if (create_x_direction == LEFT) {
      if (create_y_direction == TOP) {
        X = _x;
        Y = _y;
      } else {
        X = _x;
        Y = _y - h;
      }
    } else {
      if (create_y_direction == TOP) {
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

  //<editor-fold defaultstate="collapsed" desc="008 handle coordinates">

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

  //<editor-fold defaultstate="collapsed" desc="003 getters setters modificators">

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
    initScreen(scr); //setScreen
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
    initScreen(null); //setCenter
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
    initScreen(null); //setTopRight
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
    initScreen(null); //setBottomLeft
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
    initScreen(null); //setBottomRight
    return this;
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
    initScreen(null); //setSize
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
    initScreen(null); //setRect
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
   *
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
    initScreen(null); //setROI
  }

  /**
   * resets this region to the given rectangle <br> this might move the region even to another screen
   *
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
   *
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
    initScreen(null); //setLocation
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
    initScreen(null); //add
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
    initScreen(null); //add
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
    initScreen(null); //add
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="050 save capture to file">
  public String saveCapture(Object... args) {
    return ((Screen) getScreen()).cmdCapture(args).getStoredAt();
  }

  public Image getCapture(String... path) {
    final Image image = new Image(((Screen) getScreen()).doCapture(this));
    if (path.length > 0) {
      File file;
      if (path.length == 1) {
        file = Commons.asFile(path[0]);
      } else {
        file = Commons.asFile(path[0], path[1]);
      }
      image.save(file);
    }
    return image;
  }

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
    if (null == getScreen().getLastScreenImageFromScreen() || path == null) {
      return null;
    }
    return getScreen().getLastScreenImageFromScreen().getFile(path, name);
  }

  public void saveLastScreenImage() {
    ScreenImage simg = getScreen().getLastScreenImageFromScreen();
    if (simg != null) {
      simg.saveLastScreenImage(Commons.getAppDataStore());
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="007 spatial operators - new regions">

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
   * @param whatever offset taken from Region, Match, Image, Location or Offset
   * @return the new region
   */
  public Region offset(Object whatever) {
    Offset offset = new Offset(whatever);
    return Region.create(x + offset.x, y + offset.y, w, h, getScreen());
  }

  /**
   * create region with same size at top left corner offset
   *
   * @param x horizontal offset
   * @param y vertical offset
   * @return the new region
   */
  public Region offset(int x, int y) {
    return Region.create(this.x + x, this.y + y, w, h, getScreen());
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
    return Region.create(r.x, r.y, r.width, r.height, getScreen());
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
    return Region.create(x - l, y - t, w + l + r, h + t + b, getScreen());
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
    return Region.create(_x, y, Math.abs(width), h, getScreen());
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
    return checkAndSetRemote(new Location(x - offset, y + h / 2));
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
    return Region.create(getScreen().getBounds().intersection(new Rectangle(_x, y, Math.abs(width), h)), getScreen());
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
    return checkAndSetRemote(new Location(x + w / 2, y - offset));
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
    return Region.create(getScreen().getBounds().intersection(new Rectangle(x, _y, w, Math.abs(height))), getScreen());
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
    return checkAndSetRemote(new Location(x + w / 2, y + h + offset));
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
    return Region.create(x, _y, w, Math.abs(height), getScreen());
  }

  /**
   * create a new region containing both regions
   *
   * @param ur region to unite with
   * @return the new region
   */
  public Region union(Region ur) {
    Rectangle r = getRect().union(ur.getRect());
    return Region.create(r.x, r.y, r.width, r.height, getScreen());
  }

  /**
   * create a new region containing all regions
   *
   * @param regs regions to unite
   * @return the new region
   */
  public static Region union(List<Region> regs) {
    Region reg = new Region();
    for (Region r : regs) {
      reg = reg.union(r);
    }
    return reg;
  }

  /**
   * create a region that is the intersection of the given regions
   *
   * @param ir the region to intersect with like AWT Rectangle API
   * @return the new region
   */
  public Region intersection(Region ir) {
    Rectangle r = getRect().intersection(ir.getRect());
    return Region.create(r.x, r.y, r.width, r.height, getScreen());
  }

  public Region getInset(Region inset) {
    return new Region(x + inset.x, y + inset.y, inset.w, inset.h);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="006 parts of a Region">

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
  public static final int NH = 202, NORTH = NH, TH = NH;
  public static final int NE = 302, NORTH_EAST = NE, TR = NE;
  public static final int NW = 300, NORTH_WEST = NW, TL = NW;
  public static final int NM = 301, NORTH_MID = NM, TM = NM;

  public static final int SH = 212, SOUTH = SH, BH = SH;
  public static final int SE = 322, SOUTH_EAST = SE, BR = SE;
  public static final int SW = 320, SOUTH_WEST = SW, BL = SW;
  public static final int SM = 321, SOUTH_MID = SM, BM = SM;

  public static final int EH = 221, EAST = EH, RH = EH;
  public static final int EN = NE, EAST_NORTH = NE, RT = TR;
  public static final int ES = SE, EAST_SOUTH = SE, RB = BR;
  public static final int EM = 312, EAST_MID = EM, RM = EM;

  public static final int WH = 220, WEST = WH, LH = WH;
  public static final int WN = NW, WEST_NORTH = NW, LT = TL;
  public static final int WS = SW, WEST_SOUTH = SW, LB = BL;
  public static final int WM = 310, WEST_MID = WM, LM = WM;

  public static final int MM = 311, MIDDLE = MM, M3 = MM, MID_THIRD = MM, CC = MM;
  public static final int TT = 200;
  public static final int RR = 201;
  public static final int BB = 211;
  public static final int LL = 210;

  public static final int MV = 441, MID_VERTICAL = MV, CV = MV;
  public static final int MH = 414, MID_HORIZONTAL = MH, CH = MH;
  public static final int M2 = 444, MIDDLE_BIG = M2, C2 = M2, MID_BIG = M2;

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
    Region reg = new Region(this);
    reg.setRows(n);
    return reg.getRow(r);
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

  //<editor-fold defaultstate="collapsed" desc="028 highlight">

  /**
   * INTERNAL: highlight (for Python support):
   * <pre>
   * all act on the related Region
   * () - on/off,
   * (int) - int seconds
   * (String) - on/off with given color
   * (int,String) - int seconds with given color
   * (float), (float,String) - same as int
   * </pre>
   *
   * @param args valuaes as above
   * @return this
   */
  public Region highlight4py(ArrayList args) {
    if (args.size() > 0) {
      log(3, "highlight: %s", args);
      if (args.get(0) instanceof String) {
        highlight((String) args.get(0));
      } else if (args.get(0) instanceof Number) {
        int highlightTime = ((Number) args.get(0)).intValue();
        if (args.size() == 1) {
          highlight(highlightTime);
        } else {
          highlight(highlightTime, (String) args.get(1));
        }
      }
    }
    return this;
  }

  public static <E> void highlight(List<E> regs) {
    highlight(regs, 3);
  }

  public static <E> void highlight(List<E> regs, int timeout) { //TODO possible out-of-memory with too many regions
    int lights = 0;
    for (Object reg : regs) {
      if (reg != null && reg instanceof Region) {
        try {
          ((Region) reg).highlight();
        } catch (Exception e) {
          break;
        }
        lights++;
      }
    }
    if (timeout > 0 && lights > 0) {
      Commons.pause(timeout);
      highlightAllOff();
    }
  }

  /**
   * The Highlight for this Region
   */
  private Highlight regionHighlight = null;

  private void highlightClose() {
    regionHighlight.close();
    internalUseOnlyHighlightReset();
  }

  /**
   * INTERNAL USE ONLY
   */
  public void internalUseOnlyHighlightReset() {
    regionHighlight = null;
  }

  /**
   * Switch off all actual highlights
   */
  public static void highlightAllOff() {
    Highlight.closeAll();
  }

  /**
   * Switch on the regions highlight with default color
   *
   * @return this Region
   */
  public Region highlightOn() {
    return highlightOn(null);
  }

  /**
   * Switch on the regions highlight with given color
   *
   * @param color Color of frame (see method highlight(color))
   * @return this Region
   */
  public Region highlightOn(String color) {
    return doHighlight(-1, color);
  }

  /**
   * Switch off the regions highlight
   *
   * @return this Region
   */
  public Region highlightOff() {
    if (regionHighlight != null) {
      highlightClose();
    }
    return this;
  }

  /**
   * show a colored frame around the region for a given time or switch on/off
   * <p>
   * () or (color) switch on/off with color (default red)
   * <p>
   * (number) or (number, color) show in color (default red) for number seconds (cut to int)
   *
   * @return this region
   **/
  public Region highlight() {
    return highlight("");
  }

  /**
   * Toggle the regions Highlight border (given color)<br>
   * allowed color specifications for frame color: <br>
   * - a color name out of: black, blue, cyan, gray, green, magenta, orange, pink, red, white, yellow (lowercase and
   * uppercase can be mixed, internally transformed to all uppercase) <br>
   * - these colornames exactly written: lightGray, LIGHT_GRAY, darkGray and DARK_GRAY <br>
   * - a hex value like in HTML: #XXXXXX (max 6 hex digits)
   * - an RGB specification as: #rrrgggbbb where rrr, ggg, bbb are integer values in range 0 - 255
   * padded with leading zeros if needed (hence exactly 9 digits)
   *
   * @param color Color of frame
   * @return the region itself
   */
  public Region highlight(String color) {
    if (regionHighlight != null) {
      highlightClose();
      return this;
    }
    return highlightOn(color);
  }

  /**
   * show the regions Highlight for the given time in seconds (red frame) if 0 - use the global Settings.SlowMotionDelay
   *
   * @param secs time in seconds
   * @return the region itself
   */
  public Region highlight(double secs) {
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
  public Region highlight(double secs, String color) {
    if (getScreen() == null || isOtherScreen() || isScreenUnion) {
      Debug.error("highlight: not possible for %s", getScreen() == null ? "Image" : getScreen());
      return this;
    }
    if (secs < 0) {
      secs = -secs;
      if (lastMatch != null) {
        return lastMatch.doHighlight(secs, color);
      }
    }
    return doHighlight(secs, color);
  }

  protected Region doHighlight(double secs, String color) {
    if (Math.abs(secs) < 0.1) {
      return this;
    }
    Debug.action("highlight " + toStringShort() + " for " + secs + " secs"
        + (color != null ? " color: " + color : ""));
    if (regionHighlight != null) {
      highlightClose();
    }
    regionHighlight = new Highlight(this, color).doShow(secs);
    return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="020 wait/waitVanish">

  /**
   * Waits for the Pattern, String or Image to appear or timeout (in second) is passed
   *
   * @param <PSI>   Pattern, String or Image
   * @param target  The target to search for
   * @param timeout Timeout in seconds (will be 0 when Image)
   * @return The found Match
   * @throws FindFailed if the Find operation finally failed
   */
  public <PSI> Match wait(PSI target, double timeout) throws FindFailed {
    Match match = executeFind(target, timeout, 0, null, FINDTYPE.SINGLE).getMatch();// wait
    lastMatch = match;
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

  public <PSI> List<Match> waitAll(PSI target, double timeout) throws FindFailed {
    List<Match> matches = getAll(timeout, target);
    if (matches.size() == 0) {
      throw new FindFailed(String.format("in %s with %s", this, getImageFromTarget(target)));
    }
    return matches;
  }

  public <PSI> List<Match> waitAll(PSI target) throws FindFailed {
    return waitAll(target, autoWaitTimeout);
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
    Finder finder;
    try {
      finder = executeFind(target, timeout, 0, null, FINDTYPE.VANISH);// waitVanish
    } catch (FindFailed e) {
      return false;
    }
    return !finder.hasNext();
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

  public Match waitBest(double time, Object... args) {
    if (args.length == 0) {
      return null;
    }
    return getBest(time, varargsToList(args)); // waitBest
  }

  /**
   * @deprecated use getBest
   */
  @Deprecated
  public Match waitBestList(double time, List<Object> arg) {
    return getBest(time, arg); // waitBestList
  }

  public List<Match> waitAny(double time, Object... args) {
    if (args.length == 0) {
      return new ArrayList<>();
    }
    return getAny(time, varargsToList(args)); // waitAny
  }

  /**
   * @deprecated use getAny
   */
  @Deprecated
  public List<Match> waitAnyList(double time, List<Object> arg) {
    return getAny(time, arg); // waitAnyList
  }

  /**
   * @deprecated use getBest
   */
  @Deprecated
  public Match findBestList(List<Object> arg) {
    return waitBestList(0, arg);
  }

  /**
   * @deprecated use getAny
   */
  @Deprecated
  public List<Match> findAnyList(List<Object> arg) {
    return waitAnyList(0, arg);
  }
  //</editor-fold>

  //<editor-fold desc="021 find text public methods">
  public Match waitText(String text, double timeout) throws FindFailed {
    return relocate(wait("\t" + text + "\t", timeout));
  }

  public Match waitText(String text) throws FindFailed {
    return waitText(text, autoWaitTimeout);
  }

  public Match waitT(String text, double timeout) throws FindFailed {
    return waitText(text, timeout);
  }

  public Match waitT(String text) throws FindFailed {
    return waitT(text, autoWaitTimeout);
  }

  public Match findText(String text) throws FindFailed {
    return waitText(text, 0);
  }

  public Match existsText(String text, double timeout) {
    Match match = null;
    try {
      match = relocate(wait("\t" + text + "\t", timeout));
    } catch (FindFailed findFailed) {
    }
    return match;
  }

  public Match existsText(String text) {
    return existsText(text, autoWaitTimeout);
  }

  public boolean hasText(String text) {
    return null != existsText(text, 0);
  }

  public List<Match> findAllText(String text) {
    List<Match> matches = new ArrayList<>();
    try {
      matches = relocate(((Finder) findAll("\t" + text + "\t")).getList());
    } catch (FindFailed ff) {
    }
    return matches;
  }

  protected List<Match> relocate(List<Match> matches) {
    for (Match match : matches) {
      match.x += this.x;
      match.y += this.y;
      match.setScreen(this.getScreen());
    }
    return matches;
  }

  protected Match relocate(Match match) {
    match.x += this.x;
    match.y += this.y;
    match.setScreen(this.getScreen());
    return match;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="023 Find internal support">
/*
  private class SubFindAll extends SubFindRun {

    Finder[] finders;

    public SubFindAll(Finder[] pFinders, int pSubN, Object pTarget, Region pReg, ScreenImage pBase, double pWaitTime) {
      subN = pSubN;
      target = pTarget;
      reg = pReg;
      waitTime = pWaitTime;
      finders = pFinders;
      base = pBase;
    }

    @Override
    public void run() {
      try {
        finders[subN] = (Finder) reg.runFindAll(target, waitTime, base);
      } catch (FindFailed e) {
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

    public void shouldStop() {
      if (!hasFinished()) {
        repeatables[subN].setShouldStop();
      }
    }
  }
*/
  @SuppressWarnings("unchecked")
  protected <PSIMRL> Location getLocationFromTarget(PSIMRL target) throws FindFailed {
    if (target instanceof ArrayList) {
      ArrayList parms = (ArrayList) target;
      if (parms.size() == 1) {
        target = (PSIMRL) parms.get(0);
      } else if (parms.size() == 2) {
        if (parms.get(0) instanceof Integer && parms.get(0) instanceof Integer) {
          return new Location((Integer) parms.get(0), (Integer) parms.get(1));
        }
      } else {
        return null;
      }
    }
    if (target instanceof Pattern || target instanceof String || target instanceof Image) {
      Match m = wait(target);
      if (m != null) {
        if (isOtherScreen()) {
          return m.getTarget().setOtherScreen(getScreen());
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

  //<editor-fold defaultstate="collapsed" desc="030 Observing">
  /**
   * Flag, if an observer is running on this region {@link Settings}
   */
  private boolean observing = false;
  private boolean observingInBackground = false;

  /**
   * The {@link org.sikuli.script.support.Observer} Singleton instance
   */
  private org.sikuli.script.support.Observer regionObserver = null;

  public org.sikuli.script.support.Observer getObserver() {
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
      Image img = Element.getImageFromTarget(targetThreshhold);
      Boolean response = true;
      if (!img.isValid() && img.hasIOException()) {
        response = handleImageMissing(img, false);//onAppear, ...
        if (response == null) {
          throw new RuntimeException(
              String.format("SikuliX: Region: onEvent: %s ImageMissing: %s", obsType, targetThreshhold));
        }
      }
    }
    String name = Observing.add(this, (ObserverCallBack) observer, obsType, targetThreshhold);
    log(logLevel, "%s: observer %s %s: %s with: %s", toStringShort(), obsType,
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
//
//  public String onChangeDo(Integer threshold, Object observer) {
//    String name = Observing.add(this, (ObserverCallBack) observer, ObserveEvent.Type.CHANGE, threshold);
//    log(logLevel, "%s: onChange%s: %s minSize: %d", toStringShort(),
//        (observer == null ? "" : " with callback"), name, threshold);
//    return name;
//  }
//</editor-fold>

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
    log(logLevel, "observe: starting in " + this.toStringShort() + " for " + secs + " seconds");
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
      log(logLevel, "observe: stopped due to timeout in "
          + this.toStringShort() + " for " + secs + " seconds");
    } else {
      log(logLevel, "observe: ended successfully: " + this.toStringShort());
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
    log(logLevel, "observeInBackground now running");
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
    log(logLevel, "observe: request to stop observer for " + this.toStringShort());
    observing = false;
    observingInBackground = false;
    Observing.removeRunningObserver(this);
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

  //<editor-fold defaultstate="collapsed" desc="040 Mouse actions - clicking">
  public Location checkMatch() {
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
    return mouseMove();
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
    log(logLevel, "hover: " + target);
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
  @SuppressWarnings("unchecked")
  public <PFRML> int click(PFRML target, Integer modifiers) throws FindFailed {
    int ret = 0;
    if (target instanceof ArrayList) {
      ArrayList parms = (ArrayList) target;
      if (parms.size() > 0) {
        target = (PFRML) parms.get(0);
      } else {
        return ret;
      }
    }
    Location loc = getLocationFromTarget(target);
    if (null != loc) {
      ret = Mouse.click(loc, InputEvent.BUTTON1_DOWN_MASK, modifiers, false, this);
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
      ret = Mouse.click(loc, InputEvent.BUTTON1_DOWN_MASK, modifiers, true, this);
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
      ret = Mouse.click(loc, InputEvent.BUTTON3_DOWN_MASK, modifiers, false, this);
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

  //<editor-fold defaultstate="collapsed" desc="041 Mouse actions - drag & drop">

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
        r1.mouseDown(InputEvent.BUTTON1_DOWN_MASK);
        double DelayBeforeDrag = Settings.DelayBeforeDrag;
        if (DelayBeforeDrag < 0.0) {
          DelayBeforeDrag = Settings.DelayAfterDrag;
        }
        r1.delay((int) (DelayBeforeDrag * 1000));
        r2.smoothMove(loc2);
        r2.delay((int) (Settings.DelayBeforeDrop * 1000));
        r2.mouseUp(InputEvent.BUTTON1_DOWN_MASK);
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
        r.mouseDown(InputEvent.BUTTON1_DOWN_MASK);
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
        r.mouseUp(InputEvent.BUTTON1_DOWN_MASK);
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

  //<editor-fold defaultstate="collapsed" desc="042 Mouse actions - low level + Wheel">

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
    try { // needed to cut throw chain for FindFailed
      return mouseMove(checkMatch());
    } catch (FindFailed ex) {
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

  //<editor-fold defaultstate="collapsed" desc="045 Keyboard actions + paste">

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
    for (int i = 0; i < text.length(); i++) {
      log(logLevel + 1, "write: (%d) %s", i, text.substring(i));
      c = text.charAt(i);
      token = null;
      boolean isModifier = false;
      if (c == '#') {
        if (text.charAt(i + 1) == '#') {
          log(logLevel, "write at: %d: %s", i, c);
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
        log(logLevel + 1, "write: %d: %s", i, c);
      } else {
        log(logLevel + 1, "write: token at %d: %s", i, token);
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
              log(logLevel + 1, "write: type delay: " + t);
            } else {
              log(logLevel + 1, "write: wait: " + t);
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
            log(logLevel + 1, "write: %s Repeating: %d", token, repeat);
          } else {
            log(logLevel + 1, "write: %s", tokenSave);
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
              robot.typeKey(key.intValue()); //write
            }
            continue;
          }
        }
      }
      if (!modifier.isEmpty()) {
        log(logLevel + 1, "write: modifier + " + modifier);
        for (int n = 0; n < modifier.length(); n++) {
          robot.keyDown(Key.toJavaKeyCodeFromText(String.format("#%s.", modifier.substring(n, n + 1))));
        }
      }
      if (key > -1) {
        robot.typeKey(key.intValue()); // write
      } else {
        robot.typeChar(c, IRobot.KeyMode.PRESS_RELEASE); // write
      }
      if (!modifier.isEmpty()) {
        log(logLevel + 1, "write: modifier - " + modifier);
        for (int n = 0; n < modifier.length(); n++) {
          robot.keyUp(Key.toJavaKeyCodeFromText(String.format("#%s.", modifier.substring(n, n + 1))));
        }
      }
      robot.delay(pause);
      modifier = "";
    }

    robot.waitForIdle();
    return 0;
  }

  public void typex(int uniCode) {
    if (uniCode < 0 && uniCode > -1000) {
      uniCode = -uniCode;
      typex(" " + String.format("%d", (uniCode + 1000)).substring(1));
    } else {
      typex(String.format("%d", (uniCode + 10000)).substring(1));
    }
  }

  public void typex(char uniChar) {
    typex(String.format("%d", (((int) uniChar) + 100000)).substring(1));
  }

  public void typex(String uniCode) {
    IRobot r = getRobotForRegion();
    int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
    Settings.TypeDelay = 0.0;
    r.typex(uniCode);
    r.delay(pause);
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
      return doType(null, text, 0);
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
      return doType(null, text, modifiers);
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
      return doType(target, text, modifiersNew);
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
    return doType(target, text, 0);
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
    return doType(target, text, modifiers);
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
    return doType(target, text, modifiersNew);
  }

  private <PFRML> int doType(PFRML target, String text, int modifiers) throws FindFailed {
    if (target != null && 0 == click(target, 0)) {
      return 0;
    }
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
        log(logLevel, "Key.WIN as modifier");
        modWindows = "Windows";
      }
      if (modifiers != 0) {
        modText = String.format("( %s ) ", InputEvent.getModifiersExText(modifiers)); //KeyEvent.getKeyModifiersText(modifiers));
        if (modWindows != null) {
          modText = modText.replace("Meta", modWindows);
        }
      }
      Debug.action("%s TYPE \"%s\"", modText, showText);
      IRobot r = getRobotForRegion();
      int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
      Settings.TypeDelay = 0.0;
      for (int i = 0; i < text.length(); i++) {
        r.pressModifiers(modifiers);
        if (r.typeChar(text.charAt(i), IRobot.KeyMode.PRESS_RELEASE)) { // type/keyin
          r.releaseModifiers(modifiers);
        }
        r.delay(pause);
      }
      r.waitForIdle();
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

  //<editor-fold desc="048 Mobile actions (Android)">
  private boolean isAndroid() {
    if (isOtherScreen() && HelpDevice.isAndroid(getScreen())) {
      return true;
    }
    return false;
  }

  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param <PFRML> Pattern, String, Image, Match, Region or Location
   * @param target  PFRML
   * @throws FindFailed image not found
   */
  public <PFRML> void aTap(PFRML target) throws FindFailed {
    if (isAndroid()) {
      Location loc = getLocationFromTarget(target);
      if (loc != null) {
        IScreen adbScreen = getScreen();
        adbScreen.action("tap", loc.x, loc.y);
        adbScreen.waitAfterAction();
      }
    }
  }

  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param text text
   */
  public void aInput(String text) {
    if (isAndroid()) {
      getScreen().action("input", text);
    }
  }

  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param key key
   */
  public void aKey(int key) {
    if (isAndroid()) {
      getScreen().action("inputKeyEvent", key);
    }
  }

  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param <PFRML> Pattern, String, Image, Match, Region or Location
   * @param from    PFRML
   * @param to      PFRML
   * @throws FindFailed image not found
   */
  public <PFRML> void aSwipe(PFRML from, PFRML to) throws FindFailed {
    if (isAndroid()) {
      Location locFrom = getLocationFromTarget(from);
      Location locTo = getLocationFromTarget(to);
      if (locFrom != null && locTo != null) {
        IScreen adbScreen = getScreen();
        adbScreen.action("swipe", locFrom.x, locFrom.y, locTo.x, locTo.y);
        adbScreen.waitAfterAction();
      }
    }
  }

  /*
   *
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

  /*
   *
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

  /*
   *
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

  /*
   *
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
}
