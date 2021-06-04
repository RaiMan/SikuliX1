package org.sikuli.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.support.Commons;
import org.sikuli.support.RunTime;

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
      if (lc.equals(lcn)) {
        log(deviceType,3,"ok: %s at: (%d, %d)", scrd, lc.x, lc.y);
      } else {
        log(deviceType,3,"not ok: %s at: (%d, %d) but: (%d, %d)", scrd, lc.x, lc.y, lcn.x, lcn.y);
        usable = false;
      }
      move(lnow);
    }
    Settings.MoveMouseDelay = mmd;
    if (!isUsable()) {
      if (Commons.runningMac()) {
        RunTime.terminate(999, "Mouse.init: Mouse not useable (blocked) - Screenshots might not work either!");
      }
    }
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
    getGlobalRobot().mouseMove(where.x, where.y);
    getGlobalRobot().waitForIdle();
    return at();
  }
}
