/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.Image;
import org.sikuli.script.support.Commons;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.*;

//import org.sikuli.script.RunTime;

/**
 * This is the container for all
 */
public class Settings {

  private Settings(){}

  public static void init() {}

  public final static Field[] _FIELDS = Settings.class.getFields();
  public final static Map<String, Field> _FIELDS_LIST = new HashMap<>();

  static {
    for (Field field : _FIELDS) {
      if (field.getName().startsWith("_")) {
        continue;
      }
      int fldMods = field.getModifiers();
      if (Modifier.isFinal(fldMods)) {
        continue;
      }
      String valType = field.getType().getName();
      if (valType.contains(".") && !valType.endsWith(".String")) {
        continue;
      }
      _FIELDS_LIST.put(field.getName(), field);
    }
  }

  public static boolean set(String fName, Object fValue) {
    if (!_FIELDS_LIST.containsKey(fName)) {
      Debug.error("Settings.%s: does not exist", fName);
      return false;
    }
    Field field = _FIELDS_LIST.get(fName);
    String valType = field.getType().getSimpleName().toUpperCase().substring(0, 1);
    Object value = fValue;
    if (fValue instanceof String) {
      String val = ((String) fValue);
      if ("null".equals(val)) {
        value = null;
      } else {
        switch (valType) {
          case "B":
            value = Commons.asBool(val);
            break;
          case "I":
            value = Commons.asInt(val);
            break;
          case "L":
            value = Commons.asLong(val);
            break;
          case "F":
            value = Commons.asFloat(val);
            break;
          case "D":
            value = Commons.asDouble(val);
            break;
          default:
        }
      }
    }
    try {
      field.set(null, value);
    } catch (IllegalAccessException e) {
      Debug.error("Settings.%s = %s --- access not possible", fName, value);
      return false;
    } catch (IllegalArgumentException e) {
      Debug.error("Settings.%s = %s --- value not possible", fName, value);
      return false;
    }
    return true;
  }

  public static Object get(String fName) {
    Object value = null;
    try {
      Field field = Settings.class.getField(fName);
      value = field.get(null);
    } catch (NoSuchFieldException e) {
    } catch (IllegalAccessException e) {
    }
    return value;
  }

  public static boolean experimental = false;

  public static boolean FindProfiling = false;

  public static boolean InputFontMono = false;
  public static int InputFontSize = 14;

  //TODO Proxy as command line options
  public static String proxyName = "";
  public static String proxyIP = "";
  public static InetAddress proxyAddress = null;
  public static String proxyPort = "";
  public static boolean proxyChecked = false;
  public static Proxy proxy = null;

  public static boolean ThrowException = true; // throw FindFailed exception
  public static float AutoWaitTimeout = 3f; // in seconds
  public static float WaitScanRate = 3f; // frames per second
  public static float ObserveScanRate = 3f; // frames per second
  public static int ObserveMinChangedPixels = 50; // in pixels
  public static int RepeatWaitTime = 1; // wait 1 second for visual to vanish after action
  public static double MinSimilarity = 0.7;
  public static float AlwaysResize = 0;
  public static int DefaultPadding = 50;
  public static boolean AutoDetectKeyboardLayout = true;

  public static boolean CheckLastSeen = true;
  public static float CheckLastSeenSimilar = 0.95f;

  public static org.sikuli.script.ImageCallback ImageCallback = null;

  private static int ImageCache = 64;

  public static void setImageCache(int max) {
    if (ImageCache > max) {
      Image.clearCache(max);
    }
    ImageCache = max;
  }

  public static int getImageCache() {
    return ImageCache;
  }

  public static double DelayValue = 0.3;
  public static double DelayBeforeMouseDown = DelayValue;
  public static double DelayAfterDrag = DelayValue;
  public static double DelayBeforeDrag = -DelayValue;
  public static double DelayBeforeDrop = DelayValue;

  // setting to false reverses the wheel direction
  public static boolean WheelNatural = true;

  //setting to false supresses error message in RobotDesktop
  public static boolean checkMousePosition = true;
  public static int MousePositionDelta = 2;

  /**
   * Specify a delay between the key presses in seconds as 0.nnn. This only
   * applies to the next type and is then reset to 0 again. A value &gt; 1 is cut
   * to 1.0 (max delay of 1 second)
   */
  public static double TypeDelay = 0.0;

  /**
   * Specify a delay between the mouse down and up in seconds as 0.nnn. This
   * only applies to the next click action and is then reset to 0 again. A value
   * &gt; 1 is cut to 1.0 (max delay of 1 second)
   */
  public static double ClickDelay = 0.0;

  public static boolean ClickTypeHack = false;

  public static String BundlePath = null;
  public static String WorkdirBundlePath = "SikulixBundlePath";

  public static boolean OverwriteImages = false;

  public static final String OcrLanguageDefault = "eng";
  public static String OcrLanguage = OcrLanguageDefault;
  public static String OcrDataPath = null;
  public static boolean OcrTextSearch = true;
  public static boolean OcrTextRead = true;
  public static boolean SwitchToText = false;

  public static float SlowMotionDelay = 2.0f; // in seconds
  public static float MoveMouseDelay = 0.5f; // in seconds
  private static float MoveMouseDelaySaved = MoveMouseDelay;
  private static boolean ShowActions = false;

  public static boolean isShowActions() {
    return ShowActions;
  }

  public static void setShowActions(boolean ShowActions) {
    if (ShowActions) {
      MoveMouseDelaySaved = MoveMouseDelay;
    } else {
      MoveMouseDelay = MoveMouseDelaySaved;
    }
    Settings.ShowActions = ShowActions;
  }

  /**
   * true = highlight every match (default: false) (show red rectangle around)
   * for DefaultHighlightTime seconds (default: 2)
   */
  public static boolean Highlight = false;
  public static float DefaultHighlightTime = 2f;
  public static String DefaultHighlightColor = "RED";
  public static boolean HighlightTransparent = false;
  public static double WaitAfterHighlight = 0.3;

  public static boolean ActionLogs = true;
  public static boolean InfoLogs = true;
  public static boolean DebugLogs = false;
  public static boolean ProfileLogs = false;
  public static boolean TraceLogs = false;
  public static boolean LogTime = false;

  public static boolean UserLogs = true;
  public static String UserLogPrefix = "user";
  public static boolean UserLogTime = true;

  public static String getFilePathSeperator() {
    return File.separator;
  }

  public static String getPathSeparator() {
    return File.pathSeparator;
  }

  public static String getDataPath() {
    return Commons.getAppDataPath().getAbsolutePath();
  }

  public static OS getOS() {
    if (isWindows()) {
      return OS.WINDOWS;
    } else if (isMac()) {
      return OS.MAC;
    } else if (isLinux()) {
      return OS.LINUX;
    } else {
      return OS.NOT_SUPPORTED;
    }
  }

  public static boolean isWindows() {
    return Commons.runningWindows();
  }

  public static boolean isLinux() {
    return Commons.runningLinux();
  }

  public static boolean isMac() {
    return Commons.runningMac();
  }

  public static String getOSVersion() {
    return System.getProperty("os.version");
  }

  public static String getTimestamp() {
    return (new Date()).getTime() + "";
  }

  public static String getVersion() {
    return Commons.getSXVersion();
  }

  public static String getVersionBuild() {
    return Commons.getSXVersionLong();
  }
}
