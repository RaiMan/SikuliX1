package org.sikuli.script.support.devices;

import org.sikuli.basics.Settings;
import org.sikuli.script.SikuliXception;
import org.sikuli.script.support.Commons;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;

public class MouseDevice extends Devices {

  private static TYPE deviceType = TYPE.MOUSE;

  private static boolean usable = true;

  public static boolean isUsable() {
    return usable;
  }

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
        throw new SikuliXception("Mouse.init: Mouse not useable (blocked) - Screenshots might not work either!");
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
    getRobot().mouseMove(where.x, where.y);
    getRobot().waitForIdle();
    return at();
  }
}
