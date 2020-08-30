package org.sikuli.script.support;

public class Commons {
  private static final String osName = System.getProperty("os.name").toLowerCase();

  public static boolean runningWindows() {
    return osName.startsWith("windows");
  }

  public static boolean runningMac() {
    return osName.startsWith("mac");
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
