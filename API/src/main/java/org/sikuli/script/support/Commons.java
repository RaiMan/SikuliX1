/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.SikuliXception;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commons {

  //<editor-fold desc="00 basics">
  private static String sxVersion;

  private static String sxVersionLong;
  private static String sxVersionShort;
  private static String sxBuild;
  private static String sxBuildStamp;
  private static String sxBuildNumber;

  private static final String osName = System.getProperty("os.name").toLowerCase();
  private static final String osVersion = System.getProperty("os.version").toLowerCase();

  static {
    if (!System.getProperty("os.arch").contains("64")) {
      throw new SikuliXception("SikuliX fatal Error: System must be 64-Bit");
    }

    if (!"64".equals(System.getProperty("sun.arch.data.model"))) {
      throw new SikuliXception("SikuliX fatal Error: Java must be 64-Bit");
    }

    Properties sxProps = new Properties();
    String svf = "/Settings/sikulixversion.txt";
    try {
      InputStream is;
      is = Commons.class.getResourceAsStream(svf);
      if (is == null) {
        String msg = String.format("SikuliX fatal Error: not found on classpath: %s", svf);
        throw new SikuliXception(msg);
      }
      sxProps.load(is);
      is.close();
    } catch (IOException e) {
      String msg = String.format("SikuliX fatal Error: load did not work: %s (%s)", svf, e.getMessage());
      throw new SikuliXception(msg);
    }
    //    sikulixvproject=2.0.0  or 2.1.0-SNAPSHOT
    sxVersion = sxProps.getProperty("sikulixvproject");
    //    sikulixbuild=2019-10-17_09:58
    sxBuild = sxProps.getProperty("sikulixbuild");
    sxBuildStamp = sxBuild
        .replace("_", "").replace("-", "").replace(":", "")
        .substring(0, 12);
    //    sikulixbuildnumber= BE-AWARE: only real in deployed artefacts (TravisCI)
    //    in development context undefined:
    sxBuildNumber = sxProps.getProperty("sikulixbuildnumber");
    if (sxBuildNumber.contains("TRAVIS_BUILD_NUMBER")) {
      sxBuildNumber = "";
    }

    if (sxBuildNumber.isEmpty()) {
      sxVersionLong = sxVersion + String.format("-%s", sxBuildStamp);
    } else {
      sxVersionLong = sxVersion + String.format("-#%s-%s", sxBuildNumber, sxBuildStamp);
    }
    sxVersionShort = sxVersion.replace("-SNAPSHOT", "");
  }

  public static void init() {
  }

  private static String logText;

  private static String resetLog() {
    String text = logText;
    logText = "";
    return text;
  }

  private static void startLog(String msg, Object... args) {
    logText = String.format(msg, args) + "\n";
  }

  private static void log(String msg, Object... args) {
    logText += String.format(msg, args) + "\n";
  }

  public static String getLog() {
    return resetLog();
  }

  public static void printLog() {
    System.out.println(resetLog());
  }

  public static void printLog(String msg, Object... args) {
    startLog(msg, args);
    System.out.println(resetLog());
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

  private static URL makeURL(String path) {
    URL dirURL;
    if (path.startsWith("<appdata>")) {
      String path1 = getAppDataPath().getAbsolutePath();
      path = path.replace("<appdata>", path1);
    }
    try {
      File resFolderFile = new File(path);
      if (!resFolderFile.isAbsolute()) {
        resFolderFile = new File(resFolderFile.getAbsolutePath());
      }
      dirURL = new URL("file:" + (runningWindows() ? "/" : "") + resFolderFile);
    } catch (MalformedURLException e) {
      dirURL = null;
    }
    return dirURL;
  }
  //</editor-fold>

  //<editor-fold desc="01 standard directories">
  public static File getAppDataPath() {
    if (null == appDataPath) {
      if (runningWindows()) {
        String appPath = System.getenv("APPDATA");
        if (appPath != null && !appPath.isEmpty()) {
          appDataPath = new File(new File(appPath), "Sikulix");
        }
      } else if (runningMac()) {
        appDataPath = new File(new File(getUserHome(), "Library/Application Support"), "Sikulix");
      } else {
        appDataPath = new File(getUserHome(), ".Sikulix");
      }
      if (appDataPath != null && !appDataPath.exists()) {
        appDataPath.mkdirs();
      }
      if (appDataPath == null || !appDataPath.exists()) {
        appDataPath = new File(getUserHome(), "SikulixAppData");
        Debug.error("Commons.getAppDataPath: standard place not possible - using: %s", appDataPath);
      }
    }
    return appDataPath;
  }

  public static File setAppDataPath(String givenAppPath) {
    appDataPath = new File(givenAppPath);
    if (!appDataPath.isAbsolute()) {
      appDataPath = new File(userHome, givenAppPath);
      appDataPath.mkdirs();
      if (!appDataPath.exists()) {
        //TODO setAppDataPath
      }
    }
    return appDataPath;
  }

  public static File resetAppDataPath() {
    appDataPath = null;
    return getAppDataPath();
  }

  private static File appDataPath = null;

  public static File getAppDataStore() {
    File sikulixStore = new File(getAppDataPath(), "SikulixStore");
    if (!sikulixStore.exists()) {
      sikulixStore.mkdirs();
    }
    return sikulixStore;
  }

  public static File getUserHome() {
    String userHomePath = "Env not valid";
    if (userHome == null) {
      userHomePath = System.getProperty("user.home");
      if (userHomePath != null && !userHomePath.isEmpty()) {
        userHome = new File(userHomePath);
        if (!userHome.isAbsolute() && !userHome.isDirectory()) {
          userHome = null;
        }
      }
    }
    if (userHome == null) {
      Debug.error("Commons.getUserHome: env: user.home not valid: %s (trying work-dir)", userHomePath);
      userHome = getWorkDir();
    }
    return userHome;
  }

  private static File userHome = null;

  public static File getWorkDir() {
    String workDirPath = "Env not valid";
    if (workDir == null) {
      workDirPath = System.getProperty("user.dir");
      if (workDirPath != null && !workDirPath.isEmpty()) {
        workDir = new File(workDirPath);
        if (!workDir.isAbsolute() && !workDir.isDirectory()) {
          workDir = null;
        }
      }
    }
    if (workDir == null) {
      Debug.error("Commons.getWorkDir: env: user.dir not valid: %s (exiting)", workDirPath);
      //TODO System.exit
      System.exit(-1);
    }
    return workDir;
  }

  private static File workDir = null;
  //</editor-fold>

  //<editor-fold desc="02 version infos">
  public static boolean runningWindows() {
    return osName.startsWith("windows");
  }

  public static boolean runningMac() {
    return osName.startsWith("mac");
  }

  public static boolean runningLinux() {
    return !runningMac() && !runningWindows();
  }

  public static String getSXVersion() {
    return sxVersion;
  }

  public static String getSXVersionIDE() {
    return "SikulixIDE-" + sxVersion;
  }

  public static String getSXVersionAPI() {
    return "SikulixAPI-" + sxVersion;
  }

  public static String getSXVersionLong() {
    return sxVersionLong;
  }

  public static String getSXVersionShort() {
    return sxVersionShort;
  }

  public static String getSXBuild() {
    return sxBuild;
  }

  public static String getSxBuildStamp() {
    return sxBuildStamp;
  }

  public static String getSXBuildNumber() {
    if (sxBuildNumber.isEmpty()) {
      return "dev";
    }
    return sxBuildNumber;
  }

  public static boolean hasVersionFile(File folder) {
    String[] resourceList = folder.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.contains("_MadeForSikuliX")) return true;
        return false;
      }
    });
    String libVersion = "";
    String libStamp = "";
    if (resourceList.length > 0) {
      Matcher matcher = Pattern.compile("(.*?)_(.*?)_MadeForSikuliX.*?txt").matcher(resourceList[0]);
      if (matcher.find()) {
        libVersion = matcher.group(1);
        libStamp = matcher.group(2);
      }
    }
    if (libVersion.isEmpty() || !libVersion.equals(getSXVersionShort()) ||
        libStamp.length() != Commons.getSxBuildStamp().length()
        || 0 != libStamp.compareTo(Commons.getSxBuildStamp())) {
      return false;
    }
    return true;
  }

  public static void makeVersionFile(File folder) {
    String libToken = String.format("%s_%s_MadeForSikuliX64%s.txt",
        Commons.getSXVersionShort(), Commons.getSxBuildStamp(),
        Commons.runningMac() ? "M" : (Commons.runningWindows() ? "W" : "L"));
    FileManager.writeStringToFile("*** Do not delete this file ***\n", new File(folder, libToken));
  }

  public static String getOSName() {
    return osName;
  }

  public static String getOSVersion() {
    return osVersion;
  }

  public static String getOSInfo() {
    if (runningWindows()) return "Windows " + osVersion;
    if (runningMac()) return "macOS " + osVersion;
    return System.getProperty("os.name") + " " + osVersion;
  }

  public static int getJavaVersion() {
    String vJava = System.getProperty("java.specification.version");
    int nJava;
    if (vJava.startsWith("1.")) {
      nJava = Integer.parseInt(vJava.substring(2));
    } else {
      nJava = Integer.parseInt(vJava);
    }
    return nJava;
  }

  public static String getJavaInfo() {
    return System.getProperty("java.vendor") + " " + System.getProperty("java.runtime.version");
  }

  public static boolean isJava8() {
    return 8 == getJavaVersion();
  }
  //</editor-fold>

  //<editor-fold desc="03 Java System Properties">
  public static boolean hasSysProp(String prop) {
    return null != System.getProperty(prop);
  }

  public static String getSysProp(String prop) {
    if (hasSysProp(prop)) {
      return System.getProperty(prop);
    }
    return "";
  }

  /**
   * print the current java system properties key-value pairs sorted by key
   */
  public static void dumpSysProps() {
    dumpSysProps(null);
  }

  /**
   * print the current java system properties key-value pairs sorted by key but only keys containing filter
   *
   * @param filter the filter string
   */
  public static void dumpSysProps(String filter) {
    ArrayList<String> propsKeys = getSysProps(filter);
    Integer eLen = 0;
    for (String prop : propsKeys) {
      if (prop.length() > eLen) {
        eLen = prop.length();
      }
    }
    String form = "%-" + eLen.toString() + "s = %s";
    startLog("***** system properties (%s)", filter.isEmpty() ? "all" : filter);
    for (String prop : propsKeys) {
      log(form, prop, System.getProperty(prop));
    }
    printLog();
  }

  public static ArrayList<String> getSysProps(String filter) {
    filter = filter == null ? "" : filter;
    Properties sysProps = System.getProperties();
    ArrayList<String> keysProps = new ArrayList<String>();
    for (Object entry : sysProps.keySet()) {
      String sEntry = (String) entry;
      if (filter.isEmpty() || !filter.isEmpty() && sEntry.startsWith(filter)) {
        keysProps.add(sEntry);
      }
    }
    Collections.sort(keysProps);
    return keysProps;
  }


  //</editor-fold>

  //<editor-fold desc="10 folder handling">
  public static List<String> getFileList(String resFolder) {
    return getFileList(resFolder, null);
  }

  public static List<String> getFileList(String resFolder, Class classReference) {
    List<String> fileList = new ArrayList<>();
    URL dirURL;
    if (classReference == null) {
      dirURL = makeURL(resFolder);
    } else {
      if (resFolder.equals(".")) {
        resFolder = "/" + classReference.getPackage().getName().replace(".", "/");
      }
      dirURL = classReference.getResource(resFolder);
    }
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
      //TODO getFolderFileList does not work with url-names containing illegal chars (space, ...)
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
    } catch (Exception ex) {
      Debug.error("");
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
      return null;
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