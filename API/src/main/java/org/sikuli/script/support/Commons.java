package org.sikuli.script.support;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Commons {
  //<editor-fold desc="00 basics">
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
  //</editor-fold>

  //<editor-fold desc="10 folder handling">
  public static List<String> getFileList(String resFolder, Class classReference) {
    List<String> fileList = new ArrayList<>();
    if (!parmsValid(resFolder, classReference)) {
      return fileList;
    }
    if (resFolder.equals(".")) {
      resFolder = "/" + classReference.getPackage().getName().replace(".", "/");
    }
    URL dirURL = classReference.getResource(resFolder);
    if (dirURL != null) {
      if (dirURL.getProtocol().equals("file")) {
        Map<String, List<String>> folderList = new HashMap<>();
        fileList = createFinalFolderList(getFolderFileList(dirURL, "", folderList));
      } else if (dirURL.getProtocol().equals("jar")) {
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
        String jarFolder = dirURL.getPath().substring(dirURL.getPath().indexOf("!") + 2);
        JarFile jarFile;
        try {
          jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        } catch (IOException e) {
          jarFile = null;
        }
        if (jarFile != null) {
          Enumeration<JarEntry> entries = jarFile.entries();
          if (entries.hasMoreElements()) {
            Map<String, List<String>> jarList = new HashMap<>();
            while (entries.hasMoreElements()) {
              String name = entries.nextElement().getName();
              if (name.startsWith(jarFolder)) {
                name = name.substring(jarFolder.length() + 1);
                if (name.endsWith("/")) {
                  jarList.put(name, new ArrayList<>());
                } else if (!name.isEmpty()) {
                  String dName = name.substring(0, name.lastIndexOf("/") + 1);
                  String fName = name.substring(dName.length());
                  if (dName.isEmpty()) {
                    jarList.put("/" + name, new ArrayList<>());
                  } else {
                    jarList.get(dName).add(fName);
                  }
                }
              }
            }
            fileList = createFinalFolderList(jarList);
          }
        }
      }
    }
    return fileList;
  }

  private static Map<String, List<String>> getFolderFileList(URL dirURL, String folder, Map<String, List<String>> folderList) {
    try {
      File fileFolder = new File(new File(dirURL.toURI()), folder);
      String[] list = fileFolder.list();
      for (String entry : list) {
        if (new File(fileFolder, entry).isFile()) {
          if (folder.isEmpty()) {
            folderList.put("/" + entry, new ArrayList<>());
          } else {
            folderList.get(folder).add(entry);
          }
          entry = null;
        } else {
          folderList.put(folder + entry + "/", new ArrayList<>());
          getFolderFileList(dirURL, folder + entry + "/", folderList);
        }
        entry = null;
      }
    } catch (URISyntaxException e) {
    }
    return folderList;
  }

  private static List<String> createFinalFolderList(Map<String, List<String>> folderList) {
    List<String> fileList = new ArrayList<>();
    for (String dName : folderList.keySet()) {
      if (dName.startsWith("/")) {
        fileList.add(dName);
      } else {
        fileList.add(dName);
        for (String fName : folderList.get(dName)) {
          fileList.add(fName);
        }
      }
    }
    return fileList;
  }
  //</editor-fold>

  //<editor-fold desc="15 file handling">
  public static List<String> getContentList(String res) {
    return getContentList(res, RunTime.class);
  }

  public static List<String> getContentList(String resFolder, Class classReference) {
    startLog("Commons.getContentList(res: %s, ref: %s)", resFolder, classReference);
    List<String> resList = new ArrayList<>();
    if (!parmsValid(resFolder, classReference)) {
      return resList;
    }
    String resFile = new File(resFolder, "sikulixcontent.txt").getPath();
    if (runningWindows()) {
      resFile = resFile.replace("\\", "/");
    }
    String filePath = "/" + classReference.getPackage().getName().replace(".", "/") + "/" + resFile;
    String content = copyResourceToString(resFile, classReference);
    log("getResourceList: %s\n(%s)", resFile, content);
    if (null != content) {
      String[] names = content.split("\n");
      for (String name : names) {
        if (name.equals("sikulixcontent")) continue;
        resList.add(name.trim());
      }
    }
    return resList;
  }

  public static String copyResourceToString(String res, Class classReference) {
    InputStream stream = classReference.getResourceAsStream(res);
    try {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    } catch (Exception ex) {
      stream = null;
    }
    return new String(copy(stream));
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
  //</editor-fold>

  //<editor-fold desc="20 library handling">
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
  //</editor-fold>
}
