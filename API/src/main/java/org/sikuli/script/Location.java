/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * A point like AWT.Point using global coordinates (x, y).
 * hence modifications might move location out of
 * any screen (not checked as is done with region)
 *
 */
public class Location implements Comparable<Location>{

  static RunTime runTime = RunTime.get();

  public int x;
  public int y;
  private IScreen otherScreen = null;

  /**
   * to allow calculated x and y that might not be integers
   * @param x column
   * @param y row
   * truncated to the integer part
   */
  public Location(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  /**
   * a new point at the given coordinates
   * @param x column
   * @param y row
   */
  public Location(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * duplicates the point
   * @param loc other Location
   */
  public Location(Location loc) {
    x = loc.x;
    y = loc.y;
    if (loc.isOtherScreen()) {
      otherScreen = loc.getScreen();
    }
  }

  /**
   * create from AWT point
   * @param point a Point
   */
  public Location(Point point) {
    x = (int) point.x;
    y = (int) point.y;
  }

	/**
	 *
	 * @return x value
	 */
	public int getX() {
    return x;
  }

	/**
	 *
	 * @return y value
	 */
	public int getY() {
    return y;
  }

  /**
   * get as AWT point
   * @return Point
   */
  public Point getPoint() {
    return new Point(x,y);
  }

  /**
   * sets the coordinates to the given values (moves it)
   * @param x new x
   * @param y new y
   * @return this
   */
  public Location setLocation(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * sets the coordinates to the given values (moves it)
   * @param x new x might be non-int
   * @param y new y might be non-int
   * @return this
   */
  public Location setLocation(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
    return this;
  }

  /**
    * Returns null, if outside of any screen and not contained in a non-Desktop Screen instance (e.g. remote screen)<br>
    * subsequent actions WILL crash if not tested for null return
    *
    * @return the screen, that contains the given point
    */
  public IScreen getScreen() {
    Rectangle r;
    if (otherScreen != null) {
      return otherScreen;
    }
    for (int i = 0; i < Screen.getNumberScreens(); i++) {
      r = Screen.getScreen(i).getBounds();
      if (r.contains(this.x, this.y)) {
        return Screen.getScreen(i);
      }
    }
    Debug.error("Location: outside any screen (%s, %s) - subsequent actions might not work as expected", x, y);
    return null;
  }

  /**
    * Returns primary screen, if outside of any screen or not contained in a non-Desktop Screen instance (e.g. remote screen)<br>
    *
    * @return the real screen, that contains the given point
    */
  public Screen getMonitor() {
    Rectangle r;
    Screen scr = null;
    if (otherScreen == null) {
      for (int i = 0; i < Screen.getNumberScreens(); i++) {
        r = Screen.getScreen(i).getBounds();
        if (r.contains(this.x, this.y)) {
          scr = Screen.getScreen(i);
          break;
        }
      }
    } else {
      Debug.error("Location: getMonitor: (%s, %s) not on real screen - using primary", x, y);
      scr = Screen.getPrimaryScreen();
    }
    if (scr == null) {
      Debug.error("Location: getMonitor: (%s, %s) outside any screen - using primary", x, y);
      scr = Screen.getPrimaryScreen();
    }
    return scr;
  }

	/**
	 * INTERNAL USE
	 * reveals wether the containing screen is a DeskTopScreen or not
	 * @return null if DeskTopScreen
	 */
	public boolean isOtherScreen() {
    return (otherScreen != null);
  }

	/**
	 * INTERNAL USE
	 * identifies the point as being on a non-desktop-screen
	 * @param scr Screen
	 * @return this
	 */
  public Location setOtherScreen(IScreen scr) {
    otherScreen = scr;
    return this;
  }

	/**
	 * INTERNAL USE
	 * identifies the point as being on a non-desktop-screen
	 * if this is true for the given location
	 * @return this
	 */
  private Location setOtherScreen(Location loc) {
    if (loc.isOtherScreen()) {
      setOtherScreen(loc.getScreen());
    }
    return this;
  }

// TODO Location.getColor() implement more support and make it useable
  /**
   * Get the color at the given Point for details: see java.awt.Robot and ...Color
   *
   * @return The Color of the Point
   */
  public Color getColor() {
    if (getScreen() == null) {
      return null;
    }
    return getScreen().getRobot().getColorAt(x, y);
  }

  /**
   * the offset of given point to this Location
   *
   * @param loc the other Location
   * @return relative offset
   */
  public Location getOffset(Location loc) {
    return new Location(loc.x - x, loc.y - y);
  }

  /**
   * create a region with this point as center and the given size
   *
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public Region grow(int w, int h) {
    return Region.grow(this, w, h);
  }

  /**
   * create a region with this point as center and the given size
   *
   * @param wh the width and height
   * @return the new region
   */
  public Region grow(int wh) {
    return grow(wh, wh);
  }

  /**
   * create a region with a corner at this point<br>as specified with x y<br> 0 0 top left<br>
   * 0 1 bottom left<br> 1 0 top right<br> 1 1 bottom right<br>
   *
   * @param CREATE_X_DIRECTION == 0 is left side !=0 is right side, see {@link Region#CREATE_X_DIRECTION_LEFT}, {@link Region#CREATE_X_DIRECTION_RIGHT}
   * @param CREATE_Y_DIRECTION == 0 is top side !=0 is bottom side, see {@link Region#CREATE_Y_DIRECTION_TOP}, {@link Region#CREATE_Y_DIRECTION_BOTTOM}
   * @param w the width
   * @param h the height
   * @return the new region
   */
  public Region grow(int CREATE_X_DIRECTION, int CREATE_Y_DIRECTION, int w, int h) {
    return Region.create(this, CREATE_X_DIRECTION, CREATE_Y_DIRECTION, w, h);
  }

  /**
   * moves the point the given amounts in the x and y direction, might be negative <br>might move
   * point outside of any screen, not checked
   *
   * @param dx x offset
   * @param dy y offset
   * @return the location itself modified
	 * @deprecated use {@link #translate(int, int)}
   */
	@Deprecated
  public Location moveFor(int dx, int dy) {
    x += dx;
    y += dy;
    return this;
  }

  /**
   * convenience: like awt point
   * @param dx x offset
   * @param dy y offset
   * @return the location itself modified
   */
  public Location translate(int dx, int dy) {
    return moveFor(dx, dy);
  }

  /**
   * changes the locations x and y value to the given values (moves it) <br>might move point
   * outside of any screen, not checked
   *
   * @param X new x
   * @param Y new y
   * @return the location itself modified
	 * @deprecated use {@link #move(int, int)}
   */
	@Deprecated
  public Location moveTo(int X, int Y) {
    x = X;
    y = Y;
    return this;
  }

  /**
   * convenience: like awt point
   * @param X new x
   * @param Y new y
   * @return the location itself modified
   */
  public Location move(int X, int Y) {
    return moveTo(X, Y);
  }

  /**
   * creates a point at the given offset, might be negative <br>might create a point outside of
   * any screen, not checked
   *
   * @param dx x offset
   * @param dy y offset
   * @return new location
   */
  public Location offset(int dx, int dy) {
    return new Location(x + dx, y + dy);
  }

  /**
   * creates a point at the given offset, might be negative <br>might create a point outside of
   * any screen, not checked
   *
   * @param loc offset given as Location
   * @return new location
   */
  public Location offset(Location loc) {
    return new Location(x + loc.x, y + loc.y);
  }

/**
   * creates a point at the given offset to the left, might be negative <br>might create a point
   * outside of any screen, not checked
   *
   * @param dx x offset
   * @return new location
   */
  public Location left(int dx) {
    return new Location(x - dx, y).setOtherScreen(this);
  }

  /**
   * creates a point at the given offset to the right, might be negative <br>might create a point
   * outside of any screen, not checked
   *
   * @param dx x offset
   * @return new location
   */
  public Location right(int dx) {
    return new Location(x + dx, y).setOtherScreen(this);
  }

  /**
   * creates a point at the given offset above, might be negative <br>might create a point outside
   * of any screen, not checked
   *
   * @param dy y offset
   * @return new location
   */
  public Location above(int dy) {
    return new Location(x, y - dy).setOtherScreen(this);
  }

  /**
   * creates a point at the given offset below, might be negative <br>might create a point outside
   * of any screen, not checked
   *
   * @param dy y offset
   * @return new location
   */
  public Location below(int dy) {
    return new Location(x, y + dy).setOtherScreen(this);
  }

  /**
   * new point with same offset to current screen's top left on given screen
   *
   * @param scrID number of screen
   * @return new location
   */
  public Location copyTo(int scrID) {
    return copyTo(Screen.getScreen(scrID));
  }

  /**
   * New point with same offset to current screen's top left on given screen
   *
   * @param screen new parent screen
   * @return new location
   */
  public Location copyTo(IScreen screen) {
    IScreen s = getScreen();
    s = (s == null ? Screen.getPrimaryScreen() : s);
    Location o = new Location(s.getBounds().getLocation());
    Location n = new Location(screen.getBounds().getLocation());
    return new Location(n.x + x - o.x, n.y + y - o.y);
  }

  /**
   * Move the mouse to this location point
   *
   * @return this
   */
  public Location hover() {
    Mouse.move(this);
    return this;
  }

  /**
   * Move the mouse to this location point and click left
   *
   * @return this
   */
  public Location click() {
    Mouse.click(this, "L");
    return this;
  }

  /**
   * Move the mouse to this location point and double click left
   *
   * @return this
   */
  public Location doubleClick() {
    Mouse.click(this, "LD");
    return this;
  }

  /**
   * Move the mouse to this location point and click right
   *
   * @return this
   */
  public Location rightClick() {
    Mouse.click(this, "R");
    return this;
  }

  @Override
  public boolean equals(Object oThat) {
    if (this == oThat) {
      return true;
    }
    if (!(oThat instanceof Location)) {
      return false;
    }
    Location that = (Location) oThat;
    return x == that.x && y == that.y;
  }

  /**
   * {@inheritDoc}
	 * @param loc other Location
   * @return -1 if given point is more above and/or left, 1 otherwise (0 is equal)
   */
  @Override
  public int compareTo(Location loc) {
    if (equals(loc)) {
      return 0;
    }
    if (loc.x > x) {
      return 1;
    } else if (loc.x == x) {
      if (loc.y > y) {
        return 1;
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   * @return the description
   */
  @Override
  public String toString() {
    IScreen s = getScreen();

    if(s instanceof Screen){
    	return "L(" + x + "," + y + ")" + "@" + ((Screen) s).toStringShort();
    }
    else{
    	return "L(" + x + "," + y + ")" +
                ((s == null) ? "" : "@" + s.toString());
    }
  }

  /**
   *
   * @return a shorter description
   */
  public String toStringShort() {
    return "L(" + x + "," + y + ")";
  }

	public String toJSON() {
		return String.format("[\"L\", %d, %d]", x, y);
	}

  protected IRobot getRobotForPoint(String action) {
    if (getScreen() == null) {
      Debug.error("Point %s outside any screen for %s - might not work", this, action);
      return Screen.getGlobalRobot();
    }
    if (!getScreen().isOtherScreen()) {
      getScreen().showTarget(this);
    }
    return getScreen().getRobot();
  }
}
