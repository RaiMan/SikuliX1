/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;

import java.util.ArrayList;

public class Offset {

  //<editor-fold desc="000 for Python">
  public static Offset getDefaultInstance4py() {
    return new Offset(0,0);
  }

  public static Offset make4py(ArrayList args) {
    Debug.log(3, "make: args: %s", args);
    Offset off = getDefaultInstance4py();
    if (null != args) {
      int argn = 1;
      for (Object arg : args) {
        Debug.log(3, "%d: %s (%s)", argn++, arg.getClass().getSimpleName(), arg);
      }
      if (args.size() == 2) {
        //case1: Offset(x,y)
        int num = 2;
        for (Object arg : args) {
          if (arg instanceof Integer || arg instanceof Double) {
            num--;
          }
        }
        if (num == 0) {
          off = new Offset((Integer) args.get(0), (Integer) args.get(1));
        }
      } else if (args.size() == 1) {
        //case2: Offset(whatever)
        off = new Offset(args.get(0));
      }
    }
    return off;
  }
  //</editor-fold>

  //<editor-fold desc="001 setters/getters">
  /**
   * sets x to the given value
   * @param x new x
   * @return this
   */
  public Offset setX(int x) {
    this.x = x;
    return this;
  }

  /**
   * sets x to the given value
   * @param x new x
   * @return this
   */
  public Offset setX(double x) {
    this.x = (int) x;
    return this;
  }

  /**
   *
   * @return x value
   */
  public int getX() {
    return x;
  }

  /**
   * x value
   */
  public int x = 0;

  /**
   * sets y to the given value
   * @param y new y
   * @return this
   */
  public Offset setY(int y) {
    this.y = y;
    return this;
  }

  /**
   * sets y to the given value
   * @param y new y
   * @return this
   */
  public Offset setY(double y) {
    this.y = (int) y;
    return this;
  }

  /**
   *
   * @return y value
   */
  public int getY() {
    return y;
  }
  /**
   * y value
   */
  public int y = 0;

  /**
   * sets to the given values
   * @param x new x might be non-int
   * @param y new y might be non-int
   * @return this
   */
  public Offset set(int x, int y) {
    this.x = (int) x;
    this.y = (int) y;
    return this;
  }

  /**
   * sets the given values
   * @param x new x double
   * @param y new y double
   * @return this
   */
  public Offset set(double x, double y) {
    set((int) x, (int) y);
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="003 constructors">
  public Offset() {
  }

  /**
   * a new offset with given x and y distances
   *
   * @param x column
   * @param y row
   */
  public Offset(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public <RMILO> Offset(RMILO whatEver) {
    if (whatEver instanceof Region || whatEver instanceof Match) {
      Region what = (Region) whatEver;
      x = what.w;
      y = what.h;
    } else if (whatEver instanceof Image) {
      Image what = (Image) whatEver;
      if (null != what.get()) {
        x = what.get().getWidth();
        y = what.get().getHeight();
      }
    } else if (whatEver instanceof Location) {
      Location what = (Location) whatEver;
      x = what.x;
      y = what.y;
    } else if (whatEver instanceof Offset) {
      Offset what = (Offset) whatEver;
      x = what.x;
      y = what.y;
    }
  }

  public <RMILO> Offset modify(RMILO whatever) {
    Offset offset = new Offset(whatever);
    offset.x = this.x + offset.x;
    offset.y = this.y + offset.y;
    return offset;
  }
  //</editor-fold>
}
