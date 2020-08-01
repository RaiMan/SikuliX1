package org.sikuli.basics;

import java.io.File;

public class Commons {
  private static final String osVersionSysProp = System.getProperty("os.version").toLowerCase();

  public static boolean runningWindows() {
    return osVersionSysProp.startsWith("windows");
  }

  public static boolean runningMac() {
    return osVersionSysProp.startsWith("mac");
  }

  public static boolean runningLinux() {
    return !runningMac() && !runningWindows();
  }

  public static String jnaPathAdd(String sFolder) {
    String jnaPath = System.getProperty("jna.library.path");
    if (null == jnaPath) {
      jnaPath = "";
    }
    File folder = new File(sFolder);
    if (!folder.exists()) {
      return jnaPath;
    }
    if (!jnaPath.isEmpty()) {
      jnaPath = File.pathSeparator + jnaPath;
    }
    jnaPath = folder.getAbsolutePath() + jnaPath;
    System.setProperty("jna.library.path", jnaPath);
    return jnaPath;
  }

  public static boolean loadLib(String lib) {

    return true;
  }

  public static int[] reverseIntArray(int[] anArray) {
    for(int i=0; i<anArray.length/2; i++){
      int temp = anArray[i];
      anArray[i] = anArray[anArray.length -i -1];
      anArray[anArray.length -i -1] = temp;
    }
    return anArray;
  }

  public static boolean isOdd(int number) {
    return number % 2 == 0;
  }
}
