package org.sikuli.script.support;

import java.io.*;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public class Commons {
  private static String logText;

  private static void resetLog() {
    logText = "";
  }

  private static void startLog(String msg, Object... args) {
    logText = String.format(msg, args) + "\n";
  }

  private static void log(String msg, Object... args) {
    logText += String.format(msg, args) + "\n";
  }

  public static String getLog() {
    return logText;
  }

  public static boolean parmsValid(Object... parms) {
    boolean success = true;
    for (Object parm : parms) {
      if (null == parm) {
        success = false;
      } else if (parm instanceof String && ((String) parm).isEmpty()) {
        success = false;
      }
    }
    if (!success) {
      log("Parameters not valid!");
      return false;
    }
    return true;
  }

  private static final String osName = System.getProperty("os.name").toLowerCase();
  private static final String osVersionSysProp = System.getProperty("os.version").toLowerCase();

  public static boolean runningWindows() {
    return osName.startsWith("windows");
  }

  public static boolean runningMac() {
    return osName.startsWith("mac");
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
    for (int i = 0; i < anArray.length / 2; i++) {
      int temp = anArray[i];
      anArray[i] = anArray[anArray.length - i - 1];
      anArray[anArray.length - i - 1] = temp;
    }
    return anArray;
  }

  public static boolean isOdd(int number) {
    return number % 2 == 0;
  }

  public static List<String> getContentList(String res) {
    return getContentList(res, RunTime.class);
  }

  public static List<String> getContentList(String res, Class classReference) {
    startLog("Commons.getContentList(res: %s, ref: %s", res, classReference);
    List<String> resList = new ArrayList<>();
    if (!parmsValid(res, classReference)) {
      return resList;
    }
    InputStream aIS = null;
    String content = null;
    res = new File(res, "sikulixcontent.txt").getPath();
    if (runningWindows()) {
      res = res.replace("\\", "/");
    }
    aIS = classReference.getResourceAsStream(res);
    String pn = classReference.getPackage().getName().replace(".", "/");
    pn = "/" + pn + "/" + res;
    if (aIS != null) {
      content = new String(copy(aIS));
    }
    log("getResourceList: %s (%s)", res, content);
    aIS = null;
    try {
      if (aIS != null) {
        aIS.close();
      }
    } catch (Exception ex) {
    }
    if (null != content) {
      String[] names = content.split("\n");
      for (String name : names) {
        if (name.equals("sikulixcontent")) continue;
        resList.add(name.trim());
      }
    }
    return resList;
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] tmp = new byte[8192];
    int len;
    while (true) {
      len = in.read(tmp);
      if (len <= 0) {
        break;
      }
      out.write(tmp, 0, len);
    }
    out.flush();
  }

  private static byte[] copy(InputStream inputStream) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length = 0;
    try {
      while ((length = inputStream.read(buffer)) != -1) {
        baos.write(buffer, 0, length);
      }
      inputStream.close();
    } catch (IOException e) {
    }
    return baos.toByteArray();
  }

}
