package org.sikuli.script.support.devices;

import org.sikuli.basics.Settings;
import org.sikuli.script.Location;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MouseDevice extends Devices {

  private static TYPE deviceType = TYPE.MOUSE;

  private static AtomicBoolean useable = new AtomicBoolean(false);

  public static boolean isUseable() {
    return useable.get();
  }

  public static void start() {
    Point lnow = at();
    Point lc, lcn;
    for (ScreenDevice scrd : ScreenDevice.get()) {
      lc = scrd.getCenter();
      getMouseRobot().mouseMove(lc.x, lc.y);
      getMouseRobot().waitForIdle();
      lcn = at();
      //lcn = move(lc);
      if (MouseDevice.nearby(lc, lcn)) {
        move(lnow);
        useable.set(true);
      } else {
        break;
      }
    }
  }

  public static boolean nearby(Object target, Object actual) {
    int x1, x2, y1, y2;
    int delta = Settings.MousePositionDelta;
    if (target instanceof Point) {
      x1 = ((Point) target).x;
      y1 = ((Point) target).y;
    } else if (target instanceof Location) {
      x1 = ((Location) target).x;
      y1 = ((Location) target).y;
    } else {
      return false;
    }
    if (actual instanceof Point) {
      x2 = ((Point) actual).x;
      y2 = ((Point) actual).y;
    } else if (actual instanceof Location) {
      x2 = ((Location) actual).x;
      y2 = ((Location) actual).y;
    } else {
      return false;
    }
    if (x2 < (x1 - delta) || x2 > (x1 + delta) || y2 < (y1 - delta) || y2 > (y1 + delta)) {
      return false;
    }
    return true;
  }

  public static Point at() {
    PointerInfo mp = MouseInfo.getPointerInfo();
    if (mp != null) {
      return new Point(MouseInfo.getPointerInfo().getLocation());
    } else {
      log(deviceType, -1, "not possible to get mouse position (PointerInfo == null)");
      return null;
    }
  }

  public static Point move(Point where) {
    getMouseRobot().mouseMove(where.x, where.y);
    getMouseRobot().waitForIdle();
    return at();
  }
}
