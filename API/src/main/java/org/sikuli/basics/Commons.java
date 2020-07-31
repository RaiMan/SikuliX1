package org.sikuli.basics;

public class Commons {
  private static final String osVersionSysProp = System.getProperty("os.version");

  public static boolean runningWindows() {
    return osVersionSysProp.startsWith("windows");
  }

  public static boolean runningMac() {
    return osVersionSysProp.startsWith("mac");
  }

  public static boolean runningLinux() {
    return !runningMac() && !runningWindows();
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
