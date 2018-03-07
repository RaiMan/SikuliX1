/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import java.awt.Rectangle;

/**
 * CANDIDATE FOR DEPRECATION
 * INTERNAL USE
 * An extension of DesktopScreen, that uses all active monitors as one big screen
 *
 * TO BE EVALUATED: is this really needed?
 */
public class ScreenUnion extends Screen {

  private Rectangle _bounds;

  public ScreenUnion() {
    super(true);
    _bounds = new Rectangle();
    for (int i = 0; i < Screen.getNumberScreens(); i++) {
      _bounds = _bounds.union(Screen.getBounds(i));
    }
    x = _bounds.x;
    y = _bounds.y;
    w = _bounds.width;
    h = _bounds.height;
  }

  public Region getRegion() {
    return Region.virtual(_bounds);
  }

  @Override
  public int getIdFromPoint(int x, int y) {
    Rectangle sr = getBounds();
    int _x = x + getBounds().x;
    int _y = y + getBounds().y;
    for (int i = 0; i < getNumberScreens(); i++) {
      if (Screen.getScreen(i).contains(new Location(_x, _y))) {
        Debug.log(3, "ScreenUnion: getIdFromPoint: " +
                     "(%d, %d) as (%d, %d) in (%d, %d, %d, %d) on %d",
                       x, y, _x, _y, sr.x, sr.y, sr.width, sr.height, i);
        return i;
      }
    }
    Debug.log(3, "ScreenUnion: getIdFromPoint: " +
                 "(%d, %d) as (%d, %d) in (%d, %d, %d, %d) on ???",
                   x, y, _x, _y, sr.x, sr.y, sr.width, sr.height);
    return 0;
  }

  @Override
  public Rectangle getBounds() {
    return _bounds;
  }

  @Override
  public ScreenImage capture(Rectangle rect) {
    Debug.log(4, "ScreenUnion: capture: (%d,%d) %dx%d", rect.x, rect.y, rect.width, rect.height);
    Screen s = Screen.getPrimaryScreen();
//    Location tl = new Location(rect.getLocation());
//    for (Screen sx : Screen.screens) {
//      if (sx.contains(tl)) {
//        s = sx;
//        break;
//      }
//    }
    ScreenImage si = s.capture(rect);
    return si;
  }

  @Override
  public boolean useFullscreen() {
    return false;
  }
}
