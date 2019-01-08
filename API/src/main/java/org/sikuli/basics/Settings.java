/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.Image;
import org.sikuli.script.RunTime;
//import org.sikuli.script.RunTime;

import java.io.File;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Date;

/**
 * This is the container for all
 */
public class Settings {

  public static synchronized void init(RunTime givenRunTime) {
    runTime = givenRunTime;
  }

  private static RunTime runTime = null;

  public static boolean experimental = false;

  public static boolean noPupUps = false;
  public static boolean FindProfiling = false;

  public static boolean InputFontMono = false;
  public static int InputFontSize = 14;

  public static boolean isRunningIDE = false;

  public static boolean handlesMacBundles = true;
  public static boolean runningSetup = false;

  public static String OcrDataPath = null;

  public static final float FOREVER = Float.POSITIVE_INFINITY;
  public static final int JavaVersion = makeJavaVersion();

  private static int makeJavaVersion() {
    int major = 0;
    try {
      String vJava = java.lang.System.getProperty("java.specification.version");
      if (vJava.startsWith("1.")) {
        major = Integer.parseInt(vJava.substring(2, 3));
      } else {
        String[] parts = vJava.split("\\.");
        major = Integer.parseInt(parts[0]);
      }
    } catch (Exception ex) {
    }
    return major;
  }

  public static String proxyName = "";
  public static String proxyIP = "";
  public static InetAddress proxyAddress = null;
  public static String proxyPort = "";
  public static boolean proxyChecked = false;
  public static Proxy proxy = null;

  public static final int ISWINDOWS = 0;
  public static final int ISMAC = 1;
  public static final int ISLINUX = 2;
  public static boolean isMacApp = false;

  public static boolean ThrowException = true; // throw FindFailed exception
  public static float AutoWaitTimeout = 3f; // in seconds
  public static float WaitScanRate = 3f; // frames per second
  public static float ObserveScanRate = 3f; // frames per second
  public static int ObserveMinChangedPixels = 50; // in pixels
  public static int RepeatWaitTime = 1; // wait 1 second for visual to vanish after action
  public static double MinSimilarity = 0.7;
  public static boolean CheckLastSeen = true;
  public static float CheckLastSeenSimilar = 0.95f;
  //public static boolean UseImageFinder = false;
  public static float AlwaysResize = 0;
  public static org.sikuli.script.ImageCallback ImageCallback = null;

  private static int ImageCache = 64;

  /**
   * set the maximum to be used for the {@link Image} cache
   * <br>the start up value is 64 (meaning MB)
   * <br>using 0 switches off caching and clears the cache in that moment
   *
   * @param max cache size in MB
   */
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
  @Deprecated
  // use DelayBeforeDrag instead
  public static double DelayAfterDrag = DelayValue;
  public static double DelayBeforeDrag = -DelayValue;
  public static double DelayBeforeDrop = DelayValue;

  // setting to false reverses the wheel direction
  public static boolean WheelNatural = true;

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
  public static boolean ClickFast = false;
  public static boolean RobotFake = true;

  public static String BundlePath = null;
  public static boolean OcrTextSearch = true;
  public static boolean OcrTextRead = true;
  public static String OcrLanguage = "eng";
  public static boolean SwitchToText = false;

  /**
   * true = start slow motion mode, false: stop it (default: false) show a
   * visual for SlowMotionDelay seconds (default: 2)
   */
  public static boolean TRUE = true;
  public static boolean FALSE = false;

  private static boolean ShowActions = false;
  public static boolean OverwriteImages = false;

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

  public static float SlowMotionDelay = 2.0f; // in seconds
  public static float MoveMouseDelay = 0.5f; // in seconds
  private static float MoveMouseDelaySaved = MoveMouseDelay;

  /**
   * true = highlight every match (default: false) (show red rectangle around)
   * for DefaultHighlightTime seconds (default: 2)
   */
  public static boolean Highlight = false;
  public static float DefaultHighlightTime = 2f;
  public static float WaitAfterHighlight = 0.3f;
  public static boolean ActionLogs = true;
  public static boolean InfoLogs = true;
  public static boolean DebugLogs = false;
  public static boolean ProfileLogs = false;
  public static boolean LogTime = false;
  public static boolean UserLogs = true;
  public static String UserLogPrefix = "user";
  public static boolean UserLogTime = true;
  public static boolean TraceLogs = false;
  /**
   * default pixels to add around with nearby() and grow()
   */
  public static final int DefaultPadding = 50;

  public static boolean isJava7() {
    return JavaVersion > 6;
  }

  public static boolean isJava6() {
    return JavaVersion < 7;
  }

  public static String getFilePathSeperator() {
    return File.separator;
  }

  public static String getPathSeparator() {
    if (isWindows()) {
      return ";";
    }
    return ":";
  }

  public static String getSikuliDataPath() {
    return runTime.fSikulixAppPath.getAbsolutePath();
  }

  public static int getOS() {
    int osRet;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("mac")) {
      osRet = ISMAC;
    } else if (os.startsWith("windows")) {
      osRet = ISWINDOWS;
    } else {
      osRet = ISLINUX;
    }
    return osRet;
  }

  public static boolean isWindows() {
    return runTime.runningWindows;
  }

  public static boolean isLinux() {
    return runTime.runningLinux;
  }

  public static boolean isMac() {
    return runTime.runningMac;
  }

  public static String getOSVersion() {
    return System.getProperty("os.version");
  }

  public static String getTimestamp() {
    return (new Date()).getTime() + "";
  }
}
