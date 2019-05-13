/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.guide.Guide;
import org.sikuli.script.Image;
import org.sikuli.script.support.RunTime;
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

  public static boolean FindProfiling = false;

  public static boolean InputFontMono = false;
  public static int InputFontSize = 14;

  //TODO handlesMacBundles still needed?
  public static boolean handlesMacBundles = true;

  public static final float FOREVER = Float.POSITIVE_INFINITY;

  //TODO Proxy as command line options
  public static String proxyName = "";
  public static String proxyIP = "";
  public static InetAddress proxyAddress = null;
  public static String proxyPort = "";
  public static boolean proxyChecked = false;
  public static Proxy proxy = null;

  //TODO Mac Application
  public static boolean isMacApp = false;

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
  public static boolean OverwriteImages = false;

  public static String OcrDataPath = null;
  public static boolean OcrTextSearch = true;
  public static boolean OcrTextRead = true;
  public static String OcrLanguage = "eng";
  public static boolean SwitchToText = false;

  public static boolean TRUE = true;
  public static boolean FALSE = false;

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
  public static float WaitAfterHighlight = 0.3f;

  public static boolean ActionLogs = true;
  public static boolean InfoLogs = true;
  public static boolean DebugLogs = false;
  public static boolean ProfileLogs = false;
  public static boolean TraceLogs = false;
  public static boolean LogTime = false;

  public static boolean UserLogs = true;
  public static String UserLogPrefix = "user";
  public static boolean UserLogTime = true;

  public static boolean isJava7() {
    return Guide.JavaVersion > 6;
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

  public static String getDataPath() {
    return RunTime.get().fSikulixAppPath.getAbsolutePath();
  }

  public static final int ISWINDOWS = 0;
  public static final int ISMAC = 1;
  public static final int ISLINUX = 2;
  public static int getOS() {
    if (isMac()) {
      return ISMAC;
    } else if (isWindows()) {
      return ISWINDOWS;
    }
    return ISLINUX;
  }

  public static boolean isWindows() {
    return RunTime.get().runningWindows;
  }

  public static boolean isLinux() {
    return RunTime.get().runningLinux;
  }

  public static boolean isMac() {
    return RunTime.get().runningMac;
  }

  public static String getOSVersion() {
    return System.getProperty("os.version");
  }

  public static String getTimestamp() {
    return (new Date()).getTime() + "";
  }

  public static String getVersion() {
    return RunTime.get().getVersion();
  }

  public static String getVersionBuild() {
    return RunTime.get().SXVersionLong;
  }
}
