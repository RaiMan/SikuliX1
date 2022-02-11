package org.sikuli.script.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.Location;
import org.sikuli.script.support.Commons;

import java.awt.*;

public class MouseDevice extends Devices {

  private static TYPE deviceType = TYPE.MOUSE;

  private static boolean usable = true;

  public static boolean isUsable() {
    return usable;
  }

  public static boolean isNotUseable(String function) {
    String fText = function.isEmpty() ? "" : "." + function + "()";
    if (notUseable) {
      Debug.error("Mouse%s: not usable (blocked)", fText);
    }
    return notUseable;
  }

  public static boolean isNotUseable() {
    return isNotUseable("");
  }

  public static void setNotUseable() {
    notUseable = true;
    if (Commons.runningMac()) {
      Debug.error("Mouse: not useable (blocked)\n" +
          "See: https://github.com/RaiMan/SikuliX1/wiki/Allow-SikuliX-actions-on-macOS");
    } else {
      Debug.error("Mouse: not useable (blocked)");
    }
  }

  private static boolean notUseable = false;

  public static void start() {
    log(deviceType, 3, "checking usability");
    Point lnow = at();
    float mmd = Settings.MoveMouseDelay;
    Settings.MoveMouseDelay = 0f;
    Point lc, lcn;
    for (ScreenDevice scrd : ScreenDevice.get()) {
      lc = scrd.getCenter();
      lcn = move(lc);
      if (MouseDevice.nearby(lc, lcn)) {
        log(deviceType, 3, "ok: %s at: (%d, %d)", scrd, lc.x, lc.y);
        move(lnow);
      } else {
        log(deviceType, 3, "not ok: %s at: (%d, %d) but is: (%d, %d)", scrd, lc.x, lc.y, lcn.x, lcn.y);
        usable = false;
      }
    }
    Settings.MoveMouseDelay = mmd;
    if (!isUsable()) {
      if (Commons.runningMac()) {
        Commons.terminate(999, "Mouse.init: Mouse not useable (blocked) - Screenshots might not work either!");
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
