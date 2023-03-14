package org.sikuli.script.support.devices;

import org.sikuli.basics.Settings;
import org.sikuli.script.Location;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MouseDevice extends Device {

  private static TYPE deviceType = TYPE.MOUSE;

  private static AtomicBoolean useable = null;

  public static boolean isUseable() {
    if (useable == null) {
      useable = new AtomicBoolean(false);
      Device.checkAccessibility();
    }
    return useable.get();
  }



  public static void start() {
    Point lnow = at();
    for (ScreenDevice scrd : ScreenDevice.getAll()) {
      getMouseRobot().mouseMove(scrd.getCenter().x, scrd.getCenter().y);
      getMouseRobot().delay(100);
      if (!MouseDevice.nearby(scrd.getCenter(), at())) {
        break;
      }
      if (useable == null) {
        useable = new AtomicBoolean(true);
      } else {
        useable.set(true);
      }
      getMouseRobot().mouseMove(lnow.x, lnow.y);
      break;
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

  public static boolean isMoving() {
    PointerInfo mp = MouseInfo.getPointerInfo();
    if (mp != null) {
      Commons.pause(0.1);
      Point before = mp.getLocation();
      Point after = MouseInfo.getPointerInfo().getLocation();
      return (before.x != after.x || before.y != after.y);
    } else {
      return false;
    }
  }
}
