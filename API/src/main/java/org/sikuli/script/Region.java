/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Observer;
import org.sikuli.script.support.*;
import org.sikuli.util.Highlight;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * A Region is a rectangular area on a screen.
 * <p>completely contained in that screen (no screen overlapping)</p>
 * NOTES:
 * <br>- when needed (find ops), the pixel content is captured from the screen
 * <br>- if nothing else is said, the center pixel is the target for mouse actions
 */
public class Region extends Element {

  //<editor-fold desc="000 Fields and global features">
  protected void copyElementAttributes(Element element) {
    super.copyElementAttributes(element);
    setWaitScanRate(element.getWaitScanRate());
    setObserveScanRate(element.getObserveScanRate());
    setRepeatWaitTime(element.getRepeatWaitTime());
    setAutoWaitTimeout(element.getAutoWaitTimeout());
    setFindFailedResponse(element.getFindFailedResponse());
    setFindFailedHandler(element.getFindFailedHandler());
    setThrowException(element.getThrowException());
  }

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
      nameText = "#" + getName() + "# ";
    }
    return String.format("%sR[%d,%d %dx%d]@S(%s)", nameText, x, y, w, h, scrText);
  }

  /**
   * @return a compact description
   */
  public String toStringShort() {
    return toString();
  }

  /**
   * Check whether this Region is contained by any of the available screens
   *
   * @return true if yes, false otherwise
   */
  public boolean isValid() {
    if (this instanceof IScreen) {
      return true;
    }
    return getScreen() != null && w > 0 && h > 0;
  }
  //</editor-fold>

  //<editor-fold desc="001 for Python private">

  /**
   * INTERNAL: Get a default Region for Python
   *
   * @return the default screen
   */
  public static Region getDefaultInstance4py() {
    return new Screen();
  }

  /**
   * Create a new Region
   * <br>- Region(x, y, w, h)
   * <br>- Region(someRegion)
   *
   * @param args
   * @return new Region
   */
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

  //<editor-fold defaultstate="collapsed" desc="002 Constructors">

  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region() {
    rows = 0;
  }

  public Region(Dimension size) {
    this(0, 0, size.width, size.height);
  }

  /**
   * internal use only, used for new Screen objects to get the Region behavior
   */
  protected Region(boolean isScreenUnion) {
    this.isScreenUnion = isScreenUnion;
    this.rows = 0;
  }

  private boolean isScreenUnion = false;

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
    setScreen(r.getScreen());
    setOtherScreenOf(r);
    rows = 0;
    copyElementAttributes(r);
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
   * create a region with a corner at the given point.
   * <br>as specified with x y
   * <br> 0 0 top left
   * <br> 0 1 bottom left
   * <br> 1 0 top right
   * <br> 1 1 bottom right
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
   * create a region with a corner at the given point.
   * <br>as specified with x y
   * <br> 0 0 top left
   * <br> 0 1 bottom left
   * <br>1 0 top right
   * <br> 1 1 bottom right
   * <br>same as the corresponding create method, here to be naming compatible with class Location
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
   * Create a region from another region including the region's settings
   *
   * @param r the region
   * @return then new region
   */
  public static Region create(Region r) {
    Region reg = Region.create(r.x, r.y, r.w, r.h, r.getScreen());
    reg.copyElementAttributes(r);
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

  //<editor-fold defaultstate="collapsed" desc="003 getters setters modificators">

  /**
   * @return the screen, that contains the top left corner of the region. Returns primary screen if outside of any
   * screen.
   * @deprecated Only for compatibility, to get the screen containing this region, use {@link #getScreen()}
   */
  @Deprecated
  public IScreen getScreenContaining() {
    return getScreen();
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
   * resets this region to the given location, and size.
   * <br> this might move the region even to another screen
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
    initScreen(null);
  }

  /**
   * resets this region to the given rectangle.
   * <br> this might move the region even to another screen
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
   * resets this region to the given region.
   * <br> this might move the region even to another screen
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
   * set the regions position.
   * <br>this might move the region even to another screen
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
   * set the regions position.
   * <br>this might move the region even to another screen
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
   * set the regions position/size.
   * <br>this might move the region even to another screen
   *
   * @param r Region
   * @return the region itself
   * @deprecated to be like AWT Rectangle API use setRect() instead
   */
  @Deprecated
  public Region morphTo(Region r) {
    return (Region) setRect(r);
  }

  /**
   * resize the region using the given padding values.
   * <br>might be negative
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
   * extend the region, so it contains the given region.
   * <br>but only the part inside the current screen
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
   * extend the region, so it contains the given point.
   * <br>but only the part inside the current screen
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

  //<editor-fold desc="004 housekeeping private">
  protected static Region getFakeRegion() {
    if (fakeRegion == null) {
      fakeRegion = new Region(0, 0, 5, 5);
    }
    return fakeRegion;
  }

  private static Region fakeRegion;

  public Image getImage() {
    ScreenImage image = getScreen().capture(x, y, w, h);
    return image;
  }

  public Mat getContent() {
    return getImage().getContent();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="006 parts of a Region">

  /**
   * select the specified part of the region.
   * <br>
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
   * example: getTile(522) will use a raster of 5 rows and 5 columns and return the cell in the middle<br>
   * special cases:<br>
   * if either y or z are == or &gt; x: returns the respective row or column<br>
   * example: getTile(525) will use a raster of 5 rows and 5 columns and return the row in the middle<br>
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
   * the area constants for use with getTile()
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
  private int rows = 0;
  private int cols = 0;
  private int rowH = 0;
  private int colW = 0;
  private int rowHd = 0;
  private int colWd = 0;

  public Region getTile(int part) {
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
   * store info: this region is divided vertically into n even rows.
   * <br>a preparation for using getRow()
   *
   * @param n number of rows
   * @return the top row
   */
  public Region setRows(int n) {
    return setRaster(n, 0);
  }

  /**
   * store info: this region is divided horizontally into n even columns.
   * <br> a preparation for using getCol()
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
   * store info: this region is divided into a raster of even cells.
   * <br>a preparation for using getCell()
   * <br>adjusted to a minimum cell size of 5 x 5 pixels
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
   * get the specified row counting from 0.
   * <br>
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
   * get the specified column counting from 0.
   * <br>
   * negative counts reverse from the end (last is -1)
   * <br>
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
   * get the specified cell counting from (0, 0), if a raster is setup.
   * <br>
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

  //<editor-fold defaultstate="collapsed" desc="007 new regions relative to this region">
  private int newValue(int base, Number val) {
    if (val.intValue() >= base) {
      return base;
    }
    if (val.doubleValue() < 0.1) {
      return base / 2;
    }
    if (val.doubleValue() < 1) {
      return (int) Math.round(base * val.doubleValue());
    }
    return val.intValue();
  }

  /**
   * In the top left corner a new Region is created.
   * <ul>
   *   <li>no parameter: width/2 and height/2</li>
   *   <li>one number: new width and height/2</li>
   *   <li>two numbers: new width and new height</li>
   * </ul>
   * <p>if the number is 0, the respective value is value/2</p>
   * <p>if the number is a decimal between 0 and one, the respective value is value*number</p>
   * <p>if the number is greater 1, it is the new value</p>
   * <p>it is an error, if the value is greater than the actual width or height respectively</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region topLeft(Number... args) {
    switch (args.length) {
      case 0:
        return new Region(x, y, w / 2, h / 2);
      case 1:
        int newW = newValue(w, args[0]);
        return new Region(x, y, newW, h / 2);
      case 2:
        newW = newValue(w, args[0]);
        int newH = newValue(h, args[1]);
        return new Region(x, y, newW, newH);
      default:
        return this;
    }
  }

  /**
   * In the top right corner a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region topRight(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x + w - newW, y, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x + w - newW, y, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x + w - newW, y, newW, newH);
      default:
        return this;
    }
  }

  /**
   * At the top a new Region with same width is created.
   * <p>about the parameter see: {@link #topLeft}</p>
   * @param val number for the new height (omitted means 0)
   * @return a new Region or the given Region in case of error
   */
  public Region top(Number... val) {
    switch (val.length) {
      case 0:
        return topLeft(0, 0).union(topRight(0, 0));
      case 1:
        return topLeft(0, val[0]).union(topRight(0, val[0]));
      default:
        return this;
    }
  }

  /**
   * In the top middle a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region topMiddle(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x + (w - newW) / 2, y, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x + (w - newW) / 2, y, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x + (w - newW) / 2, y, newW, newH);
      default:
        return this;
    }
  }

  /**
   * In the left middle a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region leftMiddle(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x, y + (h - newH) / 2, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x, y + (h - newH) / 2, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x, y + (h - newH) / 2, newW, newH);
      default:
        return this;
    }
  }

  /**
   * At the left side a new Region with same height is created.
   * <p>about the parameter see: {@link #topLeft}</p>
   * @param val number for the new width (omitted means 0)
   * @return a new Region or the given Region in case of error
   */
  public Region leftSide(Number... val) {
    switch (val.length) {
      case 0:
        return topLeft().union(bottomLeft());
      case 1:
        return topLeft(val[0]).union(bottomLeft(val[0]));
      default:
        return this;
    }
  }

  /**
   * In the right middle a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region rightMiddle(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x + w - newW, y + (h - newH) / 2, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x + w - newW, y + (h - newH) / 2, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x  + w - newW, y + (h - newH) / 2, newW, newH);
      default:
        return this;
    }
  }

  /**
   * At the right side a new Region with same height is created.
   * <p>about the parameter see: {@link #topLeft}</p>
   * @param val number for the new width (omitted means 0)
   * @return a new Region or the given Region in case of error
   */
  public Region rightSide(Number... val) {
    switch (val.length) {
      case 0:
        return topRight().union(bottomRight());
      case 1:
        return topRight(val[0]).union(bottomRight(val[0]));
      default:
        return this;
    }
  }

  /**
   * In the bottom left corner a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region bottomLeft(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x, y + h - newH, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x, y + h - newH, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x, y + h - newH, newW, newH);
      default:
        return this;
    }
  }

  /**
   * In the bottom right corner a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region bottomRight(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x + w - newW, y + h - newH, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x + w - newW, y + h - newH, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x + w - newW, y + h - newH, newW, newH);
      default:
        return this;
    }
  }

  /**
   * At the bottom a new Region with same width is created.
   * <p>about the parameter see: {@link #topLeft}</p>
   * @param val number for the new height (omitted means 0)
   * @return a new Region or the given Region in case of error
   */
  public Region bottom(Number... val) {
    switch (val.length) {
      case 0:
        return bottomLeft(0, 0).union(bottomRight(0, 0));
      case 1:
        return bottomLeft(0, val[0]).union(bottomRight(0, val[0]));
      default:
        return this;
    }
  }

  /**
   * In the bottom middle a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region bottomMiddle(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x + (w - newW) / 2, y + h - newH, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x + (w - newW) / 2, y + h - newH, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x + (w - newW) / 2, y + h - newH, newW, newH);
      default:
        return this;
    }
  }

  /**
   * Around the center a new Region is created.
   * <p>about the parameters see: {@link #topLeft}</p>
   * @param args 0 .. 2 numbers (more than 2 values is an error)
   * @return a new Region or the given Region in case of error
   */
  public Region middle(Number... args) {
    switch (args.length) {
      case 0:
        int newW = w / 2;
        int newH = h / 2;
        return new Region(x + (w - newW) / 2, y + (h - newH) / 2, newW, newH);
      case 1:
        newW = newValue(w, args[0]);
        newH = h / 2;
        return new Region(x + (w - newW) / 2, y + (h - newH) / 2, newW, newH);
      case 2:
        newW = newValue(w, args[0]);
        newH = newValue(h, args[1]);
        return new Region(x + (w - newW) / 2, y + (h - newH) / 2, newW, newH);
      default:
        return this;
    }
  }

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
   * create a region right of the right side with same height. the new region extends to the right screen border.
   * <br> use grow() to include the current region
   *
   * @return the new region
   */
  public Region right() {
    int distToRightScreenBorder = getScreen().getX() + getScreen().getW() - (getX() + getW());
    return right(distToRightScreenBorder);
  }

  /**
   * create a region right of the right side with same height and given width.
   * <br>negative width creates the right part
   * with width inside the region
   * <br>use grow() to include the current region
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
   * create a region left of the left side with same height.
   * <br> the new region extends to the left screen border
   * <br> use grow() to include the current region
   *
   * @return the new region
   */
  public Region left() {
    int distToLeftScreenBorder = getX() - getScreen().getX();
    return left(distToLeftScreenBorder);
  }

  /**
   * create a region left of the left side with same height and given width.
   * <br> negative width creates the left part with width inside the region use grow() to include the current region
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
   * create a region above the top side with same width.
   * <br> the new region extends to the top screen border
   * <br> use grow() to include the current region
   *
   * @return the new region
   */
  public Region above() {
    int distToAboveScreenBorder = getY() - getScreen().getY();
    return above(distToAboveScreenBorder);
  }

  /**
   * create a region above the top side with same width and given height
   * <br> negative height creates the top part with height inside the region use grow() to include the current region
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
   * create a region below the bottom side with same width.
   * <br> the new region extends to the bottom screen border
   * <br> use grow() to include the current region
   *
   * @return the new region
   */
  public Region below() {
    int distToBelowScreenBorder = getScreen().getY() + getScreen().getH() - (getY() + getH());
    return below(distToBelowScreenBorder);
  }

  /**
   * create a region below the bottom side with same width and given height.
   * <br>negative height creates the bottom part with height inside the region use grow() to include the current region
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

  public Region getInset(Region inset) {
    return new Region(x + inset.x, y + inset.y, inset.w, inset.h);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="008 handle coordinates">

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

  /**
   * @return current location of mouse pointer
   * @deprecated use {@link Mouse#at()} instead
   */
  @Deprecated
  public static Location atMouse() {
    return Mouse.at();
  }

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
   * create a Location object, that can be used as an offset taking the width and hight of this Region
   *
   * @return a new Location object with width and height as x and y
   */
  public Location asOffset() {
    return new Location(w, h);
  }

//TODO  obsolete??
/*
  protected Match toGlobalCoord(Match m) {
    m.x += x;
    m.y += y;
    return m;
  }
*/
  //</editor-fold>

  //<editor-fold desc="009 points relative to the region">

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
   * @return point middle on left edge
   */
  public Location leftAt() {
    return leftAt(0);
  }

  /**
   * negative offset goes to the left.
   * <br>might be off current screen
   *
   * @param offset pixels
   * @return point with given offset horizontally to middle point on left edge
   */
  public Location leftAt(int offset) {
    return checkAndSetRemote(new Location(x - offset, y + h / 2));
  }

  /**
   * @return point middle on top edge
   */
  public Location aboveAt() {
    return aboveAt(0);
  }

  /**
   * negative offset goes towards top of screen.
   * <br>might be off current screen
   *
   * @param offset pixels
   * @return point with given offset vertically to middle point on top edge
   */
  public Location aboveAt(int offset) {
    return checkAndSetRemote(new Location(x + w / 2, y - offset));
  }

  /**
   * @return point middle on bottom edge
   */
  public Location belowAt() {
    return belowAt(0);
  }

  /**
   * positive offset goes towards bottom of screen.
   * <br>might be off current screen
   *
   * @param offset pixels
   * @return point with given offset vertically to middle point on bottom edge
   */
  public Location belowAt(int offset) {
    return checkAndSetRemote(new Location(x + w / 2, y + h + offset));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="020 find image 1 one">

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
    if (Settings.NewAPI) { // TODO wait()
      return super.wait(target, timeout);
    }
    lastMatch = null;
    String shouldAbort = "";
    RepeatableFind rf = new RepeatableFind(target, null);
    Image img = rf._image;
    String targetStr = img.getName();
    Boolean response = true;
    if (!img.isText() && !img.isValid()) {
      response = handleImageMissing(img, false); //wait
      if (response == null) {
        if (Settings.SwitchToText) {
          log(logLevel, "wait: image missing: switching to text search (deprecated - use text methods)");
          response = true;
          img.asText(true);
          rf.setTarget("\t" + target + "\t");
        } else {
          throw new RuntimeException(String.format("SikuliX: wait: ImageMissing: %s", target));
        }
      }
    }
    while (null != response && response) {
      log(logLevel, "wait: waiting %.1f secs for %s to appear in %s", timeout, targetStr, this.toStringShort());
      if (rf.repeat(timeout)) {
        lastMatch = rf.getMatch();
        //lastMatch.setImage(img);
        if (isOtherScreen()) {
          lastMatch.setOtherScreenOf(this);
        } else if (img != null) {
          img.setLastSeen(lastMatch.getRect(), lastMatch.score());
        }
        log(logLevel, "wait: %s appeared (%s)", img.getName(), lastMatch);
        return lastMatch;
      } else {
        response = handleFindFailed(target, img);
        if (null == response) {
          shouldAbort = FindFailed.createErrorMessage(this, img);
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
    log(logLevel, "wait: %s did not appear [%d msec]", targetStr, new Date().getTime() - lastFindTime);
    if (!shouldAbort.isEmpty()) {
      throw new FindFailed(shouldAbort);
    }
    return lastMatch;
  }

  /**
   * finds the given Pattern, String or Image in the region and returns the best match.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target what (PSI) to find
   * @return If found, the element. null otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Match find(PSI target) throws FindFailed {
    if (Settings.NewAPI) { //TODO find()
      return super.find(target);
    }
    Image img = Element.getImage(target);
    lastMatch = null;
    Boolean response = true;
    if (!img.isText() && !img.isValid()) {
      response = handleImageMissing(img, false); //find()
      if (response == null) {
        if (Settings.SwitchToText) {
          log(logLevel, "find: image missing: switching to text search (deprecated - use text methods)");
          response = true;
          img.asText(true);
          target = (PSI) ("\t" + target + "\t");
        } else {
          throw new RuntimeException(String.format("SikuliX: find: ImageMissing: %s", target));
        }
      }
    }
    String targetStr = img.getName();
    while (null != response && response) {
      log(logLevel, "find: waiting 0 secs for %s to appear in %s", targetStr, this.toStringShort());
      lastMatch = doFind(target, img, null);
      if (lastMatch != null) {
        if (isOtherScreen()) {
          lastMatch.setOtherScreenOf(this);
        } else if (img != null) {
          img.setLastSeen(lastMatch.getRect(), lastMatch.score());
        }
        log(logLevel, "find: %s appeared (%s)", targetStr, lastMatch);
        break;
      }
      log(logLevel, "find: %s did not appear [%d msec]", targetStr, new Date().getTime() - lastFindTime);
      if (null == lastMatch) {
        response = handleFindFailed(target, img);
      }
    }
    if (null == response) {
      throw new FindFailed(FindFailed.createErrorMessage(this, img));
    }
    return lastMatch;
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
    if (Settings.NewAPI) { //TODO exists()
      return super.exists(target, timeout);
    }
    lastMatch = null;
    RepeatableFind rf = new RepeatableFind(target, null);
    Image img = rf._image;
    Boolean response = true;
    if (!img.isText() && !img.isValid()) {
      response = handleImageMissing(img, false);//exists
      if (response == null) {
        if (Settings.SwitchToText) {
          log(logLevel, "Exists: image missing: switching to text search (deprecated - use text methods)");
          response = true;
          img.asText(true);
          rf.setTarget("\t" + target + "\t");
        } else {
          throw new RuntimeException(String.format("SikuliX: exists: ImageMissing: %s", target));
        }
      }
    }
    String targetStr = img.getName();
    log(logLevel, "exists: waiting %.1f secs for %s to appear in %s", timeout, targetStr, this.toStringShort());
    if (rf.repeat(timeout)) {
      lastMatch = rf.getMatch();
      //lastMatch.setImage(img);
      if (isOtherScreen()) {
        lastMatch.setOtherScreenOf(this);
      } else if (img != null) {
        img.setLastSeen(lastMatch.getRect(), lastMatch.score());
      }
      log(logLevel, "exists: %s has appeared (%s)", targetStr, lastMatch);
      return lastMatch;
    }
    log(logLevel, "exists: %s did not appear [%d msec]", targetStr, new Date().getTime() - lastFindTime);
    return null;
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
    if (Settings.NewAPI) { //TODO waitVanish()
      return super.waitVanish(target, timeout);
    }
    RepeatableVanish rv = new RepeatableVanish(target);
    Image img = rv._image;
    String targetStr = img.getName();
    Boolean response = true;
    if (!img.isValid()) {
      response = handleImageMissing(img, false);//waitVanish
    }
    if (null != response && response) {
      log(logLevel, "waiting for " + targetStr + " to vanish within %.1f secs", timeout);
      if (rv.repeat(timeout)) {
        log(logLevel, "%s vanished", targetStr);
        return true;
      }
      log(logLevel, "%s did not vanish before timeout", targetStr);
      return false;
    }
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="020 find image 2 many">

  /**
   * finds all occurences of the given Pattern, String or Image in the region and returns an Iterator of Matches.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target A search criteria
   * @return All elements matching
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Matches findAll(PSI target) throws FindFailed {
//  public <PSI> Iterator<Match> findAll(PSI target) throws FindFailed {
    if (Settings.NewAPI) { //TODO findAll()
      return super.findAll(target);
    }
    lastMatches = null;
    RepeatableFindAll rf = new RepeatableFindAll(target, null);
    Image img = rf._image;
    String targetStr = img.getName();
    Boolean response = true;
    if (!img.isValid()) {
      response = handleImageMissing(img, false);//findAll
      if (response == null) {
        throw new RuntimeException(String.format("SikuliX: findAll: ImageMissing: %s", target));
      }
    }
    while (null != response && response) {
      log(logLevel, "findAll: waiting %.1f secs for (multiple) %s to appear in %s",
              getAutoWaitTimeout(), targetStr, this.toStringShort());
      if (getAutoWaitTimeout() > 0) {
        rf.repeat(getAutoWaitTimeout());
        lastMatches = rf.getMatches();
      } else {
        lastMatches = doFindAll(target, null);
      }
      if (lastMatches != null) {
        log(logLevel, "findAll: %s has appeared", targetStr);
        break;
      } else {
        log(logLevel, "findAll: %s did not appear", targetStr);
        response = handleFindFailed(target, img);
      }
    }
    if (null == response) {
      throw new FindFailed(FindFailed.createErrorMessage(this, img));
    }
    return lastMatches;
  }

  public <PSI> List<Match> findAllList(PSI target) {
    return getAll(target);
  }

  public List<Match> findAny(Object... args) {
    if (Settings.NewAPI) { //TODO findAny(Object...)
      return super.findAny(args);
    }
    if (args.length == 0) {
      return new ArrayList<Match>();
    }
    List<Object> pList = new ArrayList<>();
    pList.addAll(Arrays.asList(args));
    return findAnyList(pList);
  }

  public List<Match> findAnyList(List<Object> pList) {
    if (Settings.NewAPI) { //TODO findAny(List)
      return super.findAnyList(pList);
    }
    Debug.log(logLevel, "findAny: enter");
    if (pList == null || pList.size() == 0) {
      return new ArrayList<Match>();
    }
    List<Match> mList = findAnyCollect(pList);
    return mList;
  }
  //</editor-fold>

  //<editor-fold desc="021 find text">
  public Match waitText(String text, double timeout) throws FindFailed {
    return relocate(wait("\t" + text + "\t", timeout));
  }

  public Match waitText(String text) throws FindFailed {
    return waitText(text, getAutoWaitTimeout());
  }

  public Match waitT(String text, double timeout) throws FindFailed {
    return waitText(text, timeout);
  }

  public Match waitT(String text) throws FindFailed {
    return waitT(text, getAutoWaitTimeout());
  }

  public Match findText(String text) throws FindFailed {
    return waitText(text, 0);
  }


  public Match existsText(String text, double timeout) {
    Match match = null;
    try {
      match = wait("\t" + text + "\t", timeout);
    } catch (FindFailed findFailed) {
    }
    return match;
  }

  public Match existsText(String text) {
    return existsText(text, getAutoWaitTimeout());
  }

  public boolean hasText(String text) {
    return null != existsText(text, 0);
  }

  public List<Match> findAllText(String text) {
    List<Match> matches = new ArrayList<>();
    try {
      matches = ((Finder) findAll("\t" + text + "\t")).getList();
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

  //<editor-fold defaultstate="collapsed" desc="022 find internal private">

  /**
   * Match doFind( Pattern/String/Image ) finds the given pattern on the screen and returns the best match without
   * waiting.
   */
  private <PSI> Match doFind(PSI ptn, Image img, RepeatableFind repeating) {
    Finder finder = null;
    Match match = null;
    //IScreen screen = null;
    boolean findingText = false;
    ScreenImage simg;
    double findTimeout = getAutoWaitTimeout();
    String someText = "";
    if (repeating != null) {
      findTimeout = repeating.getFindTimeOut();
    }
    if (repeating != null && repeating._finder != null) {
      finder = repeating._finder;
      simg = getScreen().capture(this);
      finder.setScreenImage(simg);
      //TODO finder.setRepeating();
      if (Settings.FindProfiling) {
        Debug.logp("[FindProfiling] Region.doFind repeat: %d msec",
                new Date().getTime() - lastSearchTimeRepeat);
      }
      lastSearchTime = (new Date()).getTime();
      finder.findRepeat();
    } else {
      //screen = getScreen();
      lastFindTime = (new Date()).getTime();
      if (ptn instanceof String) {
        if (((String) ptn).startsWith("\t") && ((String) ptn).endsWith("\t")) {
          findingText = true;
          someText = ((String) ptn).replaceAll("\\t", "");
        } else {
          if (img.isValid()) {
            lastSearchTime = (new Date()).getTime();
            finder = checkLastSeenAndCreateFinder(img, findTimeout, null);
            if (!finder.hasNext()) {
              runFinder(finder, img);
            }
          } else if (img.isText()) {
            findingText = true;
            someText = img.getNameAsText();
          }
        }
        if (findingText) {
          log(logLevel, "doFind: Switching to TextSearch");
          finder = new Finder(this);
          lastSearchTime = (new Date()).getTime();
          finder.findText(someText);
        }
      } else if (ptn instanceof Pattern) {
        if (img.isValid()) {
          lastSearchTime = (new Date()).getTime();
          finder = checkLastSeenAndCreateFinder(img, findTimeout, (Pattern) ptn);
          if (!finder.hasNext()) {
            runFinder(finder, ptn);
          }
        }
      } else if (ptn instanceof Image || ptn instanceof ScreenImage) {
        if (img.isValid()) {
          lastSearchTime = (new Date()).getTime();
          finder = checkLastSeenAndCreateFinder(img, findTimeout, null);
          if (!finder.hasNext()) {
            runFinder(finder, img);
          }
        }
      } else {
        throw new RuntimeException(String.format("SikuliX: find, wait, exists: invalid parameter: %s", ptn));
      }
      if (repeating != null) {
        repeating._finder = finder;
        repeating._image = img;
      }
    }
    if (finder != null) {
      lastSearchTimeRepeat = lastSearchTime;
      lastSearchTime = (new Date()).getTime() - lastSearchTime;
      if (finder.hasNext()) {
        lastFindTime = (new Date()).getTime() - lastFindTime;
        match = finder.next();
        match.setTimes(lastFindTime, lastSearchTime);
        if (Settings.Highlight) {
          match.highlight(Settings.DefaultHighlightTime);
        }
        if (Settings.FindProfiling) {
          Debug.logp("[FindProfiling] Region.doFind final: %d msec", lastSearchTime);
        }
      }
    }
    return match;
  }

  private void runFinder(Finder f, Object target) {
    if (Debug.shouldHighlight()) {
      if (getScreen().getW() > w + 20 && getScreen().getH() > h + 20) {
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
    double score = 0;
    if (Settings.CheckLastSeen && null != img.getLastSeen()) {
      score = img.getLastSeenScore() - 0.01;
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
          if (getScreen().getW() > w + 10 && getScreen().getH() > h + 10) {
            highlight(2, "#000255000");
          }
        }

        if (ptn == null) {
          f.find(new Pattern(img).similar(score));
        } else {
          f.find(new Pattern(ptn).similar(score));
        }
        if (f.hasNext()) {
          log(logLevel, "checkLastSeen: still there");
          return f;
        }
        log(logLevel, "checkLastSeen: not there");
      }
    }
    return new Finder(base, this);
  }

  /**
   * Match findAll( Pattern/String/Image ) finds all the given pattern on the screen and returns the best matches
   * without waiting.
   */
  private <PSI> Matches doFindAll(PSI ptn, RepeatableFindAll repeating) {
    boolean findingText = false;
    Finder finder = null;
    String someText = "";
    if (repeating != null && repeating._finder != null) {
      finder = repeating._finder;
      finder.setScreenImage(getScreen().capture(x, y, w, h));
      //TODO finder.setRepeating();
      finder.findAllRepeat();
    } else {
      Image img = null;
      if (ptn instanceof String) {
        if (((String) ptn).startsWith("\t") && ((String) ptn).endsWith("\t")) {
          findingText = true;
          someText = ((String) ptn).replaceAll("\\t", "");
        } else {
          img = Image.create((String) ptn);
          if (img.isValid()) {
            finder = new Finder(getScreen().capture(x, y, w, h), this);
            finder.findAll(img);
          } else if (img.isText()) {
            findingText = true;
            someText = img.getNameAsText();
          }
        }
        if (findingText) {
          finder = new Finder(this);
          finder.findAllText(someText);
        }
      } else if (ptn instanceof Pattern) {
        if (((Pattern) ptn).isValid()) {
          img = ((Pattern) ptn).getImage();
          finder = new Finder(getScreen().capture(x, y, w, h), this);
          finder.findAll((Pattern) ptn);
        }
      } else if (ptn instanceof Image) {
        if (((Image) ptn).isValid()) {
          img = ((Image) ptn);
          finder = new Finder(getScreen().capture(x, y, w, h), this);
          finder.findAll((Image) ptn);
        }
      } else {
        throw new RuntimeException(String.format("SikuliX: Region: doFind: invalid parameter: %s", ptn));
      }
      if (repeating != null) {
        repeating._finder = finder;
        repeating._image = img;
      }
    }
    if (finder.hasNext()) {
      return finder;
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
      int MaxTimePerScan = (int) (1000.0 / getWaitScanRate());
      int timeoutMilli = (int) (timeout * 1000);
      long begin_t = (new Date()).getTime();
      do {
        long before_find = (new Date()).getTime();
        run();
        if (ifSuccessful()) {
          return true;
        } else if (timeoutMilli < MaxTimePerScan) {
          return false;
        }
        long after_find = (new Date()).getTime();
        if (after_find - before_find < MaxTimePerScan) {
          getRobotForElement().delay((int) (MaxTimePerScan - (after_find - before_find)));
        } else {
          getRobotForElement().delay(10);
        }
      } while (begin_t + timeout * 1000 > (new Date()).getTime());
      return false;
    }
  }

  private class RepeatableFind extends Repeatable {

    Object _target;

    public void setTarget(String target) {
      _target = target;
    }

    Match _match = null;
    Finder _finder = null;
    Image _image = null;

    public <PSI> RepeatableFind(PSI target, Image img) {
      _target = target;
      if (img == null) {
        _image = new Image(target);
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
    Matches _matches = null;
    Finder _finder = null;
    Image _image = null;

    public <PSI> RepeatableFindAll(PSI target, Image img) {
      _target = target;
      if (img == null) {
        _image = Element.getImage(target);
      } else {
        _image = img;
      }
    }

    public Matches getMatches() {
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
    Debug.log(logLevel, "findAnyCollect: waiting for SubFindRuns");
    nobj = 0;
    boolean all = false;
    while (!all) {
      all = true;
      for (SubFindRun sub : theSubs) {
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
   * @param args values as above
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

  /**
   * The Highlight for this Region
   */
  private Highlight regionHighlight = null;

  private void highlightClose() {
    regionHighlight.close();
    internalUseOnlyHighlightReset();
  }

  /**
   * INTERNAL: ONLY
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
    return highlight(0, color);
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
    return highlight(null);
  }

  /**
   * Toggle the regions Highlight border (given color).
   * <br>
   * allowed color specifications for frame color: <br>
   * - a color name out of: black, blue, cyan, gray, green, magenta, orange, pink, red, white, yellow (lowercase and
   * uppercase can be mixed, internally transformed to all uppercase) <br>
   * - these colornames exactly written: lightGray, LIGHT_GRAY, darkGray and DARK_GRAY <br>
   * - a hex value like in HTML: #XXXXXX (max 6 hex digits)<br>
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
      Debug.error("highlight: not possible for %s", getScreen());
      return this;
    }
    if (secs < 0) {
      secs = -secs;
      if (lastMatch != null) {
        return new Region(lastMatch).highlight(secs, color);
      }
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
   * event.
   * <br>
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
   * obtained using {@link Observing}.
   * <br>
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
      Image img = Element.getImage(targetThreshhold);
      Boolean response = true;
      if (!img.isValid()) {
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
   * about this event.
   * <br>
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
   * event can be obtained using {@link Observing}.
   * <br>
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
   * event can be obtained using {@link Observing}.
   * <br>
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
   * about this event.
   * <br>
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
   * event can be obtained using {@link Observing}.
   * <br>
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
//	 *INTERNAL: ONLY: for use with scripting API bridges
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
//	 *INTERNAL: ONLY: for use with scripting API bridges
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
//	 *INTERNAL: ONLY: for use with scripting API bridges
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
    log(logLevel, "%s: onChange%s: %s minSize: %d", toStringShort(),
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
   * INTERNAL: ONLY: for use with scripting API bridges
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
    int MaxTimePerScan = (int) (1000.0 / getObserveScanRate());
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

  //<editor-fold defaultstate="collapsed" desc="040 Mouse - click">

  /**
   * double click at the region's last successful match
   * <br>use center if no lastMatch
   * <br>if region is a match: click targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int doubleClick() {
    try { // needed to cut throw chain for FindFailed
      return doubleClick(match(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * double click at the given target location.
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
  public <PFRML> int doubleClick(PFRML target) throws FindFailed {
    return doubleClick(target, 0);
  }


  /**
   * double click at the given target location.
   * <br> holding down the given modifier keys
   * <br> Pattern or Filename - do a find before and use the match
   * <br> Region - position at center
   * <br> Match - position at match's targetOffset
   * <br> Location - position at that point
   * <br>
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
   * right click at the region's last successful match.
   * <br>use center if no lastMatch
   * <br>if region is a match: click targetOffset
   *
   * @return 1 if possible, 0 otherwise
   */
  public int rightClick() {
    try { // needed to cut throw chain for FindFailed
      return rightClick(match(), 0);
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * right click at the given target location
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
  public <PFRML> int rightClick(PFRML target) throws FindFailed {
    return rightClick(target, 0);
  }


  /**
   * right click at the given target location
   * <br> holding down the given modifier keys
   * <br> Pattern or Filename - do a find before and use the match
   * <br> Region - position at center
   * <br> Match - position at match's targetOffset
   * <br> Location - position at that point
   * <br>
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
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="041 Mouse - drag & drop">

  /**
   * Drag from region's last match and drop at given target.
   * <br>applying Settings.DelayAfterDrag and DelayBeforeDrop
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
   * Drag from a position and drop to another using left mouse button.
   * <br>applying Settings.DelayAfterDrag and DelayBeforeDrop
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
   * Prepare a drag action: move mouse to given target.
   * <br>press and hold left mouse button
   * <br>wait Settings.DelayAfterDrag
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
   * finalize a drag action with a drop: move mouse to given target.
   * <br> wait Settings.DelayBeforeDrop before releasing the left mouse button
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

  //<editor-fold desc="043 Mouse wheel">
  /*
   * Used for LEGACY handling in
   * int wheel(PFRML target, int direction, int steps, int modifiers)
   */
  private final Set<Integer> WHEEL_MODIFIERS = new HashSet<>(Arrays.asList(new Integer[]{
          0,
          KeyModifier.CTRL,
          KeyModifier.ALT,
          KeyModifier.SHIFT,
          KeyModifier.CMD
  }));

  /**
   * Move the wheel at the current mouse position.
   * <br> the given steps in the given direction:
   * <br>Button.WHEEL_DOWN,
   * <br>Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @return 1 in any case
   */
  public int wheel(int direction, int steps) {
    return wheel(direction, steps, 0);
  }

  /**
   * Move the wheel at the current mouse position.
   * <br> the given steps in the given direction:
   * <br>Button.WHEEL_DOWN,
   * <br>Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers constants according to class Key - combine using +
   * @return 1 in any case
   */
  public int wheel(int direction, int steps, String modifiers) {
    int modifiersMask = Key.convertModifiers(modifiers);
    return wheel(direction, steps, modifiersMask);
  }


  /**
   * Move the wheel at the current mouse position
   * <br> the given steps in the given direction:
   * <br>Button.WHEEL_DOWN,
   * <br>Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 in any case
   */
  public int wheel(int direction, int steps, int modifiers) {
    return wheel(direction, steps, modifiers, Mouse.WHEEL_STEP_DELAY);
  }

  /**
   * Move the wheel at the current mouse position.
   * <br> the given steps in the given direction:
   * <br>Button.WHEEL_DOWN,
   * <br>Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers constants according to class Key - combine using +
   * @param stepDelay number of milliseconds to wait when incrementing the step value
   * @return 1 in any case
   */
  public int wheel(int direction, int steps, String modifiers, int stepDelay) {
    int modifiersMask = Key.convertModifiers(modifiers);
    return wheel(direction, steps, modifiersMask, stepDelay);
  }

  /**
   * Move the wheel at the current mouse position.
   * <br> the given steps in the given direction:
   * <br>Button.WHEEL_DOWN,
   * <br>Button.WHEEL_UP
   *
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @param stepDelay number of milliseconds to wait when incrementing the step value
   * @return 1 in any case
   */
  public int wheel(int direction, int steps, int modifiers, int stepDelay) {
    try { // needed to cut throw chain for FindFailed
      wheel(match(), direction, steps, modifiers, stepDelay);
      return 1;
    } catch (FindFailed ex) {
      return 0;
    }
  }

  /**
   * move the mouse pointer to the given target location
   * <br> and move the wheel the given steps in the given direction:
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
    return wheel(target, direction, steps, 0);
  }

  /**
   * move the mouse pointer to the given target location
   * <br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location target
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps, String modifiers) throws FindFailed {
    int modifiersMask = Key.convertModifiers(modifiers);
    return wheel(target, direction, steps, modifiersMask, Mouse.WHEEL_STEP_DELAY);
  }

  /**
   * move the mouse pointer to the given target location
   * <br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location target
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers the value of the resulting bitmask (see KeyModifier)
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps, int modifiers) throws FindFailed {
    int stepDelay = Mouse.WHEEL_STEP_DELAY;

    /*
     * LEGACY
     * Previous method signature was
     *
     * public <PFRML> int wheel(PFRML target, int direction, int steps, int stepDelay)
     *
     * That's why we interpret the modifiers parameter as stepDelay if it is not a
     * reasonable modifier for wheel actions.
     */
    if (!WHEEL_MODIFIERS.contains(modifiers)) {
      modifiers = 0;
      stepDelay = modifiers;
    }

    return wheel(target, direction, steps, modifiers, stepDelay);
  }

  /**
   * move the mouse pointer to the given target location.
   * <br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location target
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers constants according to class Key - combine using +
   * @param stepDelay number of milliseconds to wait when incrementing the step value
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps, String modifiers, int stepDelay) throws FindFailed {
    int modifiersMask = Key.convertModifiers(modifiers);
    return wheel(target, direction, steps, modifiersMask, stepDelay);
  }

  /**
   * move the mouse pointer to the given target location.
   * <br> and move the wheel the given steps in the given direction:
   * <br>Button.WHEEL_DOWN, Button.WHEEL_UP
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location target
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param direction to move the wheel
   * @param steps     the number of steps
   * @param modifiers constants according to class Key - combine using +
   * @param stepDelay number of milliseconds to wait when incrementing the step value
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PFRML> int wheel(PFRML target, int direction, int steps, int modifiers, int stepDelay) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    if (loc != null) {
      Mouse.use(this);
      Mouse.keep(this);
      Mouse.move(loc, this);
      Mouse.wheel(this, direction, steps, modifiers, stepDelay);
      Mouse.let(this);
      return 1;
    }
    return 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="045 Keyboard">

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
    IRobot robot = getRobotForElement();
    int pause = 20 + (Settings.TypeDelay > 1 ? 1000 : (int) (Settings.TypeDelay * 1000));
    Settings.TypeDelay = 0.0;
    robot.typeStarts();
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
              robot.typeKey(key.intValue());
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
        robot.typeKey(key.intValue());
      } else {
        robot.typeChar(c, IRobot.KeyMode.PRESS_RELEASE);
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

    robot.typeEnds();
    robot.waitForIdle();
    return 0;
  }

  /**
   * enters the given text one character/key after another using keyDown/keyUp.
   * <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   * <br>the text is entered at the current position of the focus/carret
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
   * enters the given text one character/key after another using keyDown/keyUp.
   * <br>while holding down the given modifier keys
   * <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   * <br>the text is entered at the current position of the focus/carret
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
   * enters the given text one character/key after another using keyDown/keyUp.
   * <br>while holding down the given modifier keys
   * <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   * <br>the text is entered at the current position of the focus/carret
   *
   * @param text      containing characters and/or Key constants
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   */
  public int type(String text, String modifiers) {
    String target = null;
    int modifiersMask = Key.convertModifiers(modifiers);

    /*
     * If no modifiers are active it might be that the
     * method has been called with the intention to call
     * in fact type(String patternFilename, String text).
     * e.g. Region(...).type("pattern.png", "Hello world")
     */
    if (modifiersMask == 0) {
      target = text;
      text = modifiers;
    }
    try {
      return keyin(target, text, modifiersMask);
    } catch (FindFailed findFailed) {
      return 0;
    }
  }

  /**
   * first does a click(target) at the given target position to gain focus/carret.
   * <br>then enters the given text one character/key after another using keyDown/keyUp
   * <br>about the usable Key constants see keyDown(keys)
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
   * first does a click(target) at the given target position to gain focus/carret
   * <br>enters the given text one character/key after another using keyDown/keyUp
   * <br>while holding down the given modifier keys
   * <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
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
   * first does a click(target) at the given target position to gain focus/carret
   * <br>enters the given text one character/key after another using keyDown/keyUp
   * <br>while holding down the given modifier keys
   * <br>about the usable Key constants see keyDown(keys)
   * <br>Class Key only provides a subset of a US-QWERTY PC keyboard layout
   *
   * @param <PFRML>   Pattern, Filename, Text, Region, Match or Location
   * @param target    Pattern, Filename, Text, Region, Match or Location
   * @param text      containing characters and/or Key constants
   * @param modifiers constants according to class Key - combine using +
   * @return 1 if possible, 0 otherwise
   * @throws FindFailed if not found
   */
  public <PFRML> int type(PFRML target, String text, String modifiers) throws FindFailed {
    int modifiersMask = Key.convertModifiers(modifiers);
    return keyin(target, text, modifiersMask);
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
        log(logLevel, "Key.WIN as modifier");
        modWindows = "Windows";
      }
      if (modifiers != 0) {
        modText = String.format("( %s ) ", KeyEvent.getKeyModifiersText(modifiers));
        if (modWindows != null) {
          modText = modText.replace("Meta", modWindows);
        }
      }
      Debug.action("%s TYPE \"%s\"", modText, showText);
      log(logLevel, "%s TYPE \"%s\"", modText, showText);
      profiler.lap("before getting Robot");
      IRobot r = getRobotForElement();
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
  //</editor-fold>

  //<editor-fold desc="046 Keyboard - low level">

  /**
   * press and hold the given key use a constant from java.awt.event.KeyEvent which might be special in the current
   * machine/system environment
   *
   * @param keycode Java KeyCode
   */
  public void keyDown(int keycode) {
    getRobotForElement().keyDown(keycode);
  }

  /**
   * press and hold the given keys including modifier keys.
   * <br>use the key constants defined in class Key,
   * <br>which only provides a subset of a US-QWERTY PC keyboard layout
   * <br>might be mixed with simple characters
   * <br>use + to concatenate Key constants
   *
   * @param keys valid keys
   */
  public void keyDown(String keys) {
    getRobotForElement().keyDown(keys);
  }

  /**
   * release all currently pressed keys
   */
  public void keyUp() {
    getRobotForElement().keyUp();
  }

  /**
   * release the given keys (see keyDown(keycode) )
   *
   * @param keycode Java KeyCode
   */
  public void keyUp(int keycode) {
    getRobotForElement().keyUp(keycode);
  }

  /**
   * release the given keys (see keyDown(keys) )
   *
   * @param keys valid keys
   */
  public void keyUp(String keys) {
    getRobotForElement().keyUp(keys);
  }
  //</editor-fold>

  //<editor-fold desc="047 paste unicode text via clipboard">

  /**
   * pastes the text at the current position of the focus/carret.
   * <br>using the clipboard and strg/ctrl/cmd-v (paste keyboard shortcut)
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
   * first does a click(target) at the given target position to gain focus/carret.
   * <br> and then pastes the text
   * <br> using the clipboard and strg/ctrl/cmd-v (paste keyboard shortcut)
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
      IRobot r = getRobotForElement();
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
  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param <PFRML> Pattern, String, Image, Match, Region or Location
   * @param target  PFRML
   * @throws FindFailed image not found
   */
  public <PFRML> void aTap(PFRML target) throws FindFailed {
    Location loc = getLocationFromTarget(target);
    if (loc != null) {
      ExtensionManager.invokeAndWait("ADBDevice.tap", this, loc.x, loc.y);
    }
  }

  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param text text
   */
  public void aInput(String text) {
    ExtensionManager.invokeAndWait("ADBDevice.input", this, text);
  }

  /*
   *
   * EXPERIMENTAL: for Android over ADB
   *
   * @param key key
   */
  public void aKey(int key) {
    ExtensionManager.invoke("ADBDevice.inputKeyEvent", this, key);
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
    Location locFrom = getLocationFromTarget(from);
    Location locTo = getLocationFromTarget(to);
    if (locFrom != null && locTo != null) {
      ExtensionManager.invokeAndWait("ADBDevice.swipe", this, locFrom.x, locFrom.y, locTo.x, locTo.y);
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

  //<editor-fold desc="050 save capture to file">
  public String saveCapture(Object... args) {
    return ((Screen) getScreen()).cmdCapture(args).getStoredAt();
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
      simg.saveLastScreenImage(RunTime.get().fSikulixStore);
    }
  }
  //</editor-fold>
}
