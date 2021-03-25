package org.sikuli.script.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Location;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.IScreen;
import org.sikuli.script.support.RunTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelpDevice extends AbstractDevice {

  public static void stop(Device type, AbstractDevice device) {
    if (type != null) {
      if (type.equals(Device.VNC)) {
        stopVNC();
        return;
      }
      if (type.equals(Device.ANDROID)) {
        stopAndroid();
        return;
      }
    }
  }

  private static void stopVNC() {
    Class cVNC = null;
    Method cVNCstop = null;
    try {
      cVNC = Class.forName("org.sikuli.vnc.VNCScreen");
    } catch (ClassNotFoundException e) {
    }
    if (null != cVNC) {
      try {
        cVNCstop = cVNC.getMethod("stopAll", null);
      } catch (NoSuchMethodException e) {
      }
    }
    if (null != cVNCstop) {
      String error = "";
      try {
        cVNCstop.invoke(null, null);
      } catch (IllegalAccessException e) {
        error = e.getMessage();
      } catch (InvocationTargetException e) {
        error = e.getMessage();
      }
      if (!error.isEmpty()) {
        Debug.info("Error while stopping VNCScreen: %s", error);
      }
    }
  }

  public static AbstractDevice startVNC(String theIP, int thePort, String password, int cTimeout, int timeout) {
    return null;
  }

  public static AbstractDevice startVNC(String theIP, int thePort, int cTimeout, int timeout) {
    return null;
  }

  private static void stopAndroid() {
    Class cADB = null;
    Method cADBstop = null;
    try {
      cADB = Class.forName("org.sikuli.vnc.VNCScreen");
    } catch (ClassNotFoundException e) {
    }
    if (null != cADB) {
      try {
        cADBstop = cADB.getMethod("stopAll", null);
      } catch (NoSuchMethodException e) {
      }
    }
    if (null != cADBstop) {
      String error = "";
      try {
        cADBstop.invoke(null, null);
      } catch (IllegalAccessException e) {
        error = e.getMessage();
      } catch (InvocationTargetException e) {
        error = e.getMessage();
      }
      if (!error.isEmpty()) {
        Debug.info("Error while stopping ADBScreen: %s", error);
      }
    }
  }

  //<editor-fold desc="048 Mobile actions (Android)">

//              ADBScreen aScr = (ADBScreen) defaultScreen;
//              aScr.wakeUp(2);
//              aScr.wait(1.0);
//              aScr.userCapture("");
//  sImgNonLocal = (ScreenImage) defaultScreen.action("userCapture");

  public static boolean isAndroid(IScreen screen) {
    try {
      screen.getClass().equals(Class.forName("org.sikuli.android.ADBScreen"));
      return true;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      RunTime.terminate(999, "Android support not available");
    }
    return false;
  }

  public <PFRML> void aTap(PFRML target) throws FindFailed {
//    adbScreen.action("tap", loc.x, loc.y);
//    adbScreen.waitAfterAction();

//    if (isAndroid() && adbDevice != null) {
//      Location loc = getLocationFromTarget(target);
//      if (loc != null) {
//        adbDevice.tap(loc.x, loc.y);
//      }
//    }
  }

  public void aInput(String text) {
//    getScreen().action("input", text);

//    if (isAndroid() && adbDevice != null) {
//      adbDevice.input(text);
//    }
  }

  public void aKey(int key) {
//    getScreen().action("inputKeyEvent", key);

//    if (isAndroid() && adbDevice != null) {
//      adbDevice.inputKeyEvent(key);
//    }
  }

  public <PFRML> void aSwipe(PFRML from, PFRML to) throws FindFailed {
//    adbScreen.action("swipe", locFrom.x, locFrom.y, locTo.x, locTo.y);
//    adbScreen.waitAfterAction();

//    if (isAndroid() && adbDevice != null) {
//      Location locFrom = getLocationFromTarget(from);
//      Location locTo = getLocationFromTarget(to);
//      if (locFrom != null && locTo != null) {
//        adbDevice.swipe(locFrom.x, locFrom.y, locTo.x, locTo.y);
//      }
//    }
  }
  //</editor-fold>
}

