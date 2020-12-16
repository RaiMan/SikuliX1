package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.script.SikuliXception;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Commons {

  private static String sxVersion;

  private static String sxVersionLong;
  private static String sxVersionShort;
  private static String sxBuild;
  private static String sxBuildStamp;
  private static String sxBuildNumber;

  private static final String osName = System.getProperty("os.name").toLowerCase();
  private static final String osVersion = System.getProperty("os.version").toLowerCase();

  private static String javaInfo;
  private static String javaVersion;
  private static String javaArch;
  private static String javaJreVersion;
  private static int javaVersionNumber;

  static {
    Properties prop = new Properties();
    String svf = "/Settings/sikulixversion.txt";
    try {
      InputStream is;
      is = Commons.class.getResourceAsStream(svf);
      if (is == null) {
        String msg = String.format("fatal: not found on classpath: %s", svf);
        throw new SikuliXception(msg);
      }
      prop.load(is);
      is.close();
    } catch (IOException e) {
      String msg = String.format("fatal: load did not work: %s (%s)", svf, e.getMessage());
      throw new SikuliXception(msg);
    }
    //    sikulixvproject=2.0.0  or 2.1.0-SNAPSHOT
    sxVersion = prop.getProperty("sikulixvproject");
    //    sikulixbuild=2019-10-17_09:58
    sxBuild = prop.getProperty("sikulixbuild");
    sxBuildStamp = sxBuild
        .replace("_", "").replace("-", "").replace(":", "")
        .substring(0, 12);
    //    sikulixbuildnumber= BE-AWARE: only real in deployed artefacts (TravisCI)
    //    in development context undefined:
    sxBuildNumber = prop.getProperty("sikulixbuildnumber");
    if (sxBuildNumber.contains("TRAVIS_BUILD_NUMBER")) {
      sxBuildNumber = "";
    }

    if (sxBuildNumber.isEmpty()) {
      sxVersionLong = sxVersion + String.format("-%s", sxBuildStamp);
    } else {
      sxVersionLong = sxVersion + String.format("-#%s-%s", sxBuildNumber, sxBuildStamp);
    }
    sxVersionShort = sxVersion.replace("-SNAPSHOT", "");

    javaInfo = "Java " + javaVersion + "(" + javaArch + ") " + javaJreVersion;
  }

  public static void init() {};

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
        //TODO
      }
    }
    return appDataPath;
  }

  public static File resetAppDataPath() {
    appDataPath = null;
    return getAppDataPath();
  }

  private static File appDataPath = null;

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
  //</editor-fold>

  //<editor-fold desc="10 folder handling">
  public static List<String> getFileList(String resFolder) {
    return getFileList(resFolder, Object.class);
  }

  public static List<String> getFileList(String resFolder, Class classReference) {
    List<String> fileList = new ArrayList<>();
    if (!parmsValid(resFolder, classReference)) {
      return fileList;
    }
    if (resFolder.equals(".")) {
      resFolder = "/" + classReference.getPackage().getName().replace(".", "/");
    }
    URL dirURL;
    if (classReference == Object.class) {
      dirURL = makeURL(resFolder);
    } else {
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
