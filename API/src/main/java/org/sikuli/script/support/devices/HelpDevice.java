package org.sikuli.script.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Mouse;
import org.sikuli.script.OCR;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.IScreen;
import org.sikuli.script.support.Observing;
import org.sikuli.util.Highlight;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelpDevice extends Device {

  public static void stop(TYPE type) {
    if (type != null) {
      if (type.equals(TYPE.VNC)) {
        stopVNC();
        return;
      }
      if (type.equals(TYPE.ANDROID)) {
        stopAndroid();
        return;
      }
      if (type.equals(TYPE.MOUSE)) {
        Mouse.reset();
        return;
      }
      if (type.equals(TYPE.KEYBOARD)) {
        return;
      }
      if (type.equals(TYPE.SCREEN)) {
        Highlight.closeAll();
        Settings.DefaultHighlightColor = "RED";
        Settings.DefaultHighlightTime = 2.0f;
        Settings.Highlight = false;
        Settings.setShowActions(false);
        FindFailed.reset();
        OCR.reset();
        Settings.OcrLanguage = Settings.OcrLanguageDefault;
        Settings.OcrDataPath = null;
        Observing.cleanUp();
        return;
      }
    }
  }

  public static void stopAll() {
    for (TYPE device : TYPE.values()) {
      stop(device);
    }
  }

  //<editor-fold desc="10 VNC support">
  private static Class getClazz(String className) {
    Class clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
    }
    return clazz;
  }

  private static Object invokeMethod(Class<?> clazz, String methodName) {
    Method method = null;
    Object result = null;
    if (null != clazz) {
      try {
        method = clazz.getMethod(methodName, (Class<?>[]) null);
      } catch (NoSuchMethodException e) {
      }
    }
    if (null != method) {
      String error = "";
      try {
        result = method.invoke(null, (Object[]) null);
      } catch (IllegalAccessException | InvocationTargetException e) {
        error = e.getMessage();
      }
      if (!error.isEmpty()) {
        Debug.info("Error while stopping VNCScreen: %s", error);
      }
    }
    return result;
  }

  static final String classNameVNC = "org.sikuli.vnc.VNCScreen";

  private static void stopVNC() {
    invokeMethod(getClazz(classNameVNC), "stopAll");
  }

  public static Device startVNC(String theIP, int thePort, String password, int cTimeout, int timeout) {
    return null;
  }

  public static Device startVNC(String theIP, int thePort, int cTimeout, int timeout) {
    return null;
  }
  //</editor-fold>

  //<editor-fold desc="20 Mobile actions (Android)">
  private static void stopAndroid() {
    Class<?> cADB = null;
    Method cADBstop = null;
    try {
      cADB = Class.forName("org.sikuli.vnc.VNCScreen");
    } catch (ClassNotFoundException e) {
    }
    if (null != cADB) {
      try {
        cADBstop = cADB.getMethod("stopAll", (Class<?>[]) null);
      } catch (NoSuchMethodException e) {
      }
    }
    if (null != cADBstop) {
      String error = "";
      try {
        cADBstop.invoke(null, (Object[]) null);
      } catch (IllegalAccessException | InvocationTargetException e) {
        error = e.getMessage();
      }
      if (!error.isEmpty()) {
        Debug.info("Error while stopping ADBScreen: %s", error);
      }
    }
  }

  public static boolean isAndroid(IScreen screen) {
    try {
      screen.getClass().equals(Class.forName("org.sikuli.android.ADBScreen"));
      return true;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      Commons.terminate(999, "Android support not available");
    }
    return false;
  }

//              ADBScreen aScr = (ADBScreen) defaultScreen;
//              aScr.wakeUp(2);
//              aScr.wait(1.0);
//              aScr.userCapture("");
//  sImgNonLocal = (ScreenImage) defaultScreen.action("userCapture");

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

