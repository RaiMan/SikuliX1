/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.CodeSource;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.sikuli.util.CommandArgsEnum.*;

public class Commons {

  //<editor-fold desc="00 static">
  private static String sxVersion;

  private static String sxVersionLong;
  private static String sxVersionShort;
  private static String sxBuild;
  private static String sxBuildStamp;
  private static String sxBuildNumber;

  private static final String osName = System.getProperty("os.name").toLowerCase();
  private static final String osVersion = System.getProperty("os.version").toLowerCase();

  private static final String sxTempDir = System.getProperty("java.io.tmpdir");
  private static File sxTempFolder = null;

  private static long startMoment;

  private static File isRunning = null;
  private static FileOutputStream isRunningFile;

  private static void runShutdownHook() {
    debug("***** final cleanup at System.exit() *****");
    //TODO runShutdownHook cleanUp();

    if (isRunning != null) {
      try {
        isRunningFile.close();
      } catch (IOException ex) {
      }
      isRunning.delete();
    }
  }

  public static void setIsRunning(File token, FileOutputStream tokenStream) {
    isRunning = token;
    isRunningFile = tokenStream;
  }

  static {
    startMoment = new Date().getTime();

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

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        runShutdownHook();
      }
    });
  }

  public static void init() {
  }

  public static double getSinceStart() {
    return (new Date().getTime() - startMoment) / 1000.0;
  }
  //</editor-fold>

  //<editor-fold desc="01 logging">
  public static void info(String msg, Object... args) {
    if (hasOption(VERBOSE)) {
      System.out.printf("[SXINFO] " + msg + "%n", args);
    }
  }

  public static void error(String msg, Object... args) {
    System.out.printf("[SXERROR] " + msg + "%n", args);
  }

  public static boolean isDebug() {
    return debug;
  }

  public static void startDebug() {
    debug = true;
  }

  public static void stopDebug() {
    debug = true;
  }

  private static boolean debug = false;

  public static void debug(String msg, Object... args) {
    if (isDebug() || Debug.isGlobalDebug()) {
      System.out.printf("[SXDEBUG] " + msg + "%n", args);
    }
  }

  public static boolean isTrace() {
    return trace;
  }

  public static void startTrace() {
    trace = true;
  }

  public static void stopTrace() {
    trace = false;
  }

  private static boolean trace = false;

  public static void trace(String msg, Object... args) {
    if (isTrace()) {
      StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
      String className = stackTrace.getFileName().replace(".java", "");
      String methodName = stackTrace.getMethodName();
      int lineNumber = stackTrace.getLineNumber();
      System.out.print(String.format("[%d->%s::%s] ", lineNumber, className, methodName));
      String out = String.format(msg, args);
      out = out.replace("\n\n", "\n");
      out = out.replace("\n\n", "\n");
      System.out.println(out);
    }
  }

  public static String enter(String method, String parameter, Object... args) {
    String parms = String.format(parameter, args);
    if (isTrace()) {
      System.out.println("[TRACE Commons] enter: " + method + "(" + parms + ")");
    }
    return "parameter(" + parms.replace("%", "%%") + ")";
  }

  public static void exit(String method, String returns, Object... args) {
    if (isTrace()) {
      System.out.printf("[TRACE Commons] exit: " + method + ": " + returns + "%n", args);
    }
  }

  public static void startLog(int level, String msg, Object... args) {
    if (level < 3) {
      if (!hasOption(VERBOSE)) {
        return;
      }
      if (hasOption(QUIET)) {
        return;
      }
    }
    System.out.println(String.format("[DEBUG STARTUP] " + msg, args));
  }
  //</editor-fold>

  //<editor-fold desc="02 startup / terminate">
  private static Class startClass = null;

  public static Class getStartClass() {
    return startClass;
  }

  public static void setStartClass(Class startClass) {
    String caller = Thread.currentThread().getStackTrace()[2].getClassName();
    if (caller.startsWith("org.sikuli.ide.Sikulix")) {
      Commons.startClass = startClass;
    } else {
      error("FATAL: setStartClass: not allowed from: %s", caller);
      System.exit(-1);
    }
  }

  public static boolean isRunningFromJar() {
    return getMainClassLocation().getAbsolutePath().endsWith(".jar");
  }

  public static File getMainClassLocation() {
    return getClassLocation(getStartClass());
  }

  public static File getClassLocation(Class theClass) {
    File jarFile = null;
    String jarName = "notKnown";
    CodeSource codeSrc = theClass.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      try {
        jarName = codeSrc.getLocation().getPath();
        jarName = URLDecoder.decode(jarName, "utf8");
      } catch (UnsupportedEncodingException e) {
        error("URLDecoder: not possible: %s", jarName);
        System.exit(1);
      }
      jarFile = new File(jarName);
    }
    return jarFile;
  }

  private static String[] startArgs = null;
  private static CommandLine cmdLine = null;
  private static CommandArgs cmdArgs = null;
  private static String[] userArgs = new String[0];

  public static void setStartArgs(String[] args) {
    startArgs = args;
    cmdArgs = new CommandArgs();
    cmdLine = cmdArgs.getCommandLine(args);
    userArgs = cmdArgs.getUserArgs();
  }

  public static String[] getUserArgs() {
    return userArgs;
  }

  public static void setUserArgs(String[] args) {
    userArgs = args;
  }

  public static void printHelp() {
    cmdArgs.printHelp();
  }

  public static boolean hasArg(String arg) {
    return cmdLine != null && cmdLine.hasOption(arg);
  }

  public static String getArg(String arg) {
    return cmdLine.getOptionValue(arg);
  }

  public static String[] getArgs(String arg) {
    String[] args = cmdLine.getOptionValues(arg);
    return args;
  }

  static boolean jythonReady = false;

  public static boolean isJythonReady() {
    return jythonReady;
  }

  public static void setJythonReady() {
    Commons.jythonReady = true;
  }
  //</editor-fold>

  //<editor-fold desc="05 standard directories">
  public static File setTempFolder() {
    if (null == sxTempFolder) {
      sxTempFolder = new File(sxTempDir);
    }
    return sxTempFolder;
  }

  public static File setTempFolder(File folder) {
    sxTempFolder = folder;
    sxTempFolder.mkdirs();
    return sxTempFolder;
  }

  public static File getTempFolder() {
    return sxTempFolder;
  }

  public static File getIDETemp() {
    return fIDETemp;
  }

  public static void setIDETemp(File ideTemp) {
    Commons.fIDETemp = ideTemp;
  }

  private static File fIDETemp = null;

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
      appDataPath = new File(getUserHome(), givenAppPath);
      appDataPath.mkdirs();
      if (!appDataPath.exists()) {
        RunTime.terminate(999, "Commons: setAppDataPath: %s (%s)", givenAppPath, "not created");
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

  public static File setWorkDir(Object path) {
    File file = getWorkDir();
    if (path instanceof String) {
      file = new File((String) path);
      if (!file.exists()) {
        file = new File(getWorkDir(), (String) path);
      }
    } else if (path instanceof File) {
      file = (File) path;
    }
    if (!file.exists()) {
      file = getWorkDir();
    }
    workDir = file;
    return workDir;
  }

  private static File workDir = null;

  public static String getJarLibsPath() {
    return "/sikulixlibs";
  }

  public static File getLibsFolder() {
    return new File(getAppDataPath(), "SikulixLibs");
  }

  public static File getLibFolder() {
    return new File(getAppDataPath(), "Lib");
  }

  public static File getExtensionsFolder() {
    return new File(getAppDataPath(), "Extensions");
  }
  //</editor-fold>

  //<editor-fold desc="06 version infos">
  private static String SYSWIN = "windows";
  private static String SYSMAC = "mac";
  private static String SYSLUX = "linux";

  public static String getSysName() {
    return runningWindows() ? SYSWIN : (runningMac() ? SYSMAC : SYSLUX);
  }

  public static boolean runningWindows() {
    return osName.startsWith(SYSWIN);
  }

  public static boolean runningMac() {
    return osName.startsWith(SYSMAC);
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
    String[] resourceList = folder.list((dir, name) -> name.contains("_MadeForSikuliX"));
    String libVersion = "";
    String libStamp = "";
    if (resourceList.length > 0) {
      Matcher matcher = Pattern.compile("(.*?)_(.*?)_MadeForSikuliX.*?txt").matcher(resourceList[0]);
      if (matcher.find()) {
        libVersion = matcher.group(1);
        libStamp = matcher.group(2);
      }
    }
    return !libVersion.isEmpty() && libVersion.equals(getSXVersionShort()) &&
        libStamp.length() == Commons.getSxBuildStamp().length()
        && 0 == libStamp.compareTo(Commons.getSxBuildStamp());
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

  public static String getSystemInfo() {
    return String.format("%s/%s/Java %s", getSXVersionLong(), getOSInfo(), Commons.getJavaVersion());
  }

  public void getStatus() {
    System.out.println("***** System Information Dump *****");
    System.out.println(String.format("*** SystemInfo\n%s", getSystemInfo()));
    System.getProperties().list(System.out);
    System.out.println("*** System Environment");
    for (String key : System.getenv().keySet()) {
      System.out.println(String.format("%s = %s", key, System.getenv(key)));
    }
    System.out.println("*** Java Class Path");
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    for (int i = 0; i < urls.length; i++) {
      System.out.println(String.format("%d: %s", i, urls[i]));
    }
    System.out.println("***** System Information Dump ***** end *****");
  }
  //</editor-fold>

  //<editor-fold desc="07 Java System Properties">
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
    info("***** system properties (%s)", filter.isEmpty() ? "all" : filter);
    for (String prop : propsKeys) {
      info(form, prop, System.getProperty(prop));
    }
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
      dirURL = makeURLFromPath(resFolder);
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
        fileList.addAll(folderList.get(dName));
      }
    }
    return fileList;
  }

  public static URL makeURL() {
    return makeURL("", null);
  }

  public static URL makeURL(Object main) {
    return makeURL(main, null);
  }

  public static URL makeURL(Object main, String sub) {
    String enter = enter("makeURL", "main: %s, sub: %s", main, sub);
    if (main == null) {
      error("makeURL: 1st parameter main is null");
      return null;
    }
    URL url = null;
    File mainFile = null;
    String mainPath = "";
    boolean isJar = false;
    boolean isHTTP = false;
    if (main instanceof File) {
      mainFile = (File) main;
    } else if (main instanceof String) {
      mainPath = (String) main;
      mainFile = new File(mainPath);
    } else if (main instanceof URL) {
      URL mainURL = (URL) main;
      if (sub != null) {
        if (mainURL.getProtocol().equals("jar")) {
          mainPath = mainURL.getPath();
          sub = sub.replace("\\", "/");
          if (sub.endsWith("/")) {
            sub = sub.substring(0, sub.length() - 1);
          }
          if (sub.startsWith("/")) {
            String[] parts = mainPath.split(".jar!");
            mainPath = parts[0] + ".jar!" + sub;
          } else {
            if (mainPath.endsWith("/")) {
              mainPath = mainPath + sub;
            } else {
              mainPath = mainPath + "/" + sub;
            }
          }
          try {
            mainURL = new URL("jar:" + mainPath);
          } catch (IOException e) {
            e.printStackTrace();
          }
          return mainURL;
        } else {
          error(enter);
          error("makeURL: URL protocol not implemented");
        }
      }
      return mainURL;
    }
    if (!mainFile.isAbsolute()) {
      if (mainPath.startsWith("http:") || mainPath.startsWith("https:")) {
        isHTTP = true;
      } else {
        mainFile = new File(getWorkDir(), mainFile.getPath());
        debug("makeURL: mainFile relative: in current workdir: %s", getWorkDir());
      }
    }
    if (isHTTP) {
      if (sub != null) {
        if (!mainPath.endsWith("/")) {
          mainPath += "/";
        }
        if (sub.startsWith("/")) {
          sub = sub.substring(1);
        }
        mainPath += sub;
      }
      try {
        url = new URL(mainPath);
      } catch (MalformedURLException e) {
        error(enter);
        error("makeURL: net url malformed: %s (%s)", mainPath, e.getMessage());
      }
    }
    if (!isHTTP) {
      if (mainFile.getPath().endsWith(".jar") || mainFile.getPath().contains(".jar!")) {
        isJar = true;
      }
      if (!isJar && sub != null) {
        mainFile = new File(mainFile, sub);
      }
      try {
        if (mainFile.getPath().contains("%")) {
          try {
            mainFile = new File(URLDecoder.decode(mainFile.getPath(), "UTF-8"));
          } catch (Exception e) {
            error("makeURL: mainFile with %%: not decodable(UTF-8): %s (%s)", mainFile, e.getMessage());
          }
        }
        mainFile = mainFile.getCanonicalFile();
        if (isJar) {
          String[] parts = mainFile.getPath().split("\\.jar");
          String jarPart = parts[0] + ".jar";
          String subPart = sub != null ? sub : "";
          if (parts.length > 1 && parts[1].length() > 1) {
            subPart = (parts[1].startsWith("!") ? parts[1].substring(2) : parts[1].substring(1));
            if (sub != null) subPart += "/" + sub;
          }
          subPart = "!/" + subPart.replace("\\", "/");
          subPart = subPart.replace("//", "/");
          url = new URL("jar:file:" + jarPart + subPart);
          mainFile = new File(jarPart);
        } else {
          url = mainFile.toURI().toURL();
        }
      } catch (MalformedURLException e) {
        error(enter);
        error("makeURL: url malformed: %s (%s)", mainFile, e.getMessage());
      } catch (IOException e) {
        error(enter);
        error("makeURL: mainFile.getCanonicalFile().toURI().toURL(): %s (%s)", mainFile, e.getMessage());
      }
      if (!mainFile.exists()) {
        error(enter);
        error("makeURL: file does not exist: %s", mainFile);
        return null;
      }
    }
    exit("makeURL", "url: %s", url);
    return url;
  }

  private static URL makeURLFromPath(String path) {
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

  public static File urlToFile(URL url) {
    File file = null;
    String path = url.getPath();
    if (url.getProtocol().equals("jar") || url.getProtocol().equals("file")) {
      path = path.replace("jar:", "");
      path = path.replace("file:", "");
      if (url.getProtocol().equals("jar")) {
        path = path.split("!/")[0];
      }
      file = new File(path);
      if (path.contains("%")) {
        try {
          file = new File(URLDecoder.decode(path, "UTF-8"));
        } catch (Exception e) {
          error("urlToFile: not decodable(UTF-8): %s (%s)", url, e.getMessage());
        }
      }
    }
    return file;
  }

  public static File asFolder(String option) {
    if (null == option) {
      return null;
    }
    File folder = new File(option);
    if (!folder.isAbsolute()) {
      folder = new File(Commons.getWorkDir(), option);
    }
    if (folder.isDirectory() && folder.exists()) {
      return folder;
    }
    return null;
  }
  //</editor-fold>

  //<editor-fold desc="15 file handling">
  public static List<String> getContentList(String res) {
    return getContentList(res, RunTime.class);
  }

  public static List<String> getContentList(String resFolder, Class classReference) {
    debug("getContentList(res: %s, ref: %s)", resFolder, classReference);
    List<String> resList = new ArrayList<>();
    if (!parmsValid(resFolder, classReference)) {
      return resList;
    }
    String resFile = new File(resFolder, "sikulixcontent.txt").getPath();
    if (runningWindows()) {
      resFile = resFile.replace("\\", "/");
    }
    String content = copyResourceToString(resFile, classReference);
    debug("getResourceList: %s\n(%s)", resFile, content);
    if (!content.isEmpty()) {
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
    String content = "";
    try {
      if (stream != null) {
        content = new String(copy(stream));
        stream.close();
      } else {
        Commons.trace("not found: %s", res);
      }
    } catch (Exception ex) {
      Commons.trace("not found: %s", res);
    }
    return content;
  }

  public static boolean copyResourceToFile(String res, Class classReference, File file) {
    InputStream stream = classReference.getResourceAsStream(res);
    OutputStream out;
    try {
      out = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      return false;
    }
    try {
      if (stream != null) {
        copy(stream, out);
        stream.close();
      }
    } catch (Exception ex) {
      return false;
    }
    return true;
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

  public static File asFile(String option) {
    if (null == option) {
      return null;
    }
    if (null == asFolder(option)) {
      File file = new File(option);
      if (!file.isAbsolute()) {
        file = new File(Commons.getWorkDir(), option);
      }
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  /**
   * Copy a file *src* to the folder *dest*. If a file with the
   * same name exists in that path, add a unique suffix -N.
   *
   * @param fSrc  source file
   * @param folder destination folder
   * @return the destination file if ok, null otherwise
   */
  public static File smartCopy(File fSrc, File folder) {
    String newName = fSrc.getName();
    File fDest = new File(folder, newName);
    if (fSrc.equals(fDest)) {
      return fDest;
    }
    fDest = getUniqueFilename(fDest);
    try {
      FileUtils.copyFile(fSrc, fDest);
      return fDest;
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * check if file exists and in case create a unique filename by adding a suffix -N,
   * where N starts with 1 and is incremented until unique
   * @param file source file
   * @return file made unique
   */
  public static File getUniqueFilename(File file) {
    if (!file.exists()) {
      return file;
    }
    final String ext = FilenameUtils.getExtension(file.getName());
    final String name = FilenameUtils.getBaseName(file.getName());
    File folder = file.getParentFile();
    final Matcher matcher = Pattern.compile("^(.*)\\-([0-9]+)$").matcher(name);
    String newName = name;
    File newFile;
    if (matcher.find()) {
      newName = matcher.group(1);
      int ix = Integer.parseInt(matcher.group(2)) + 1;
      newFile = new File(folder, newName + "-" + ix +"." + ext);
      while (newFile.exists()) {
        newFile = new File(folder, newName + "-" + ++ix +"." + ext);
      }
    } else {
      newFile = new File(folder, newName + "-1." + ext);
    }
    return newFile;
  }
  //</editor-fold>

  //<editor-fold desc="20 library handling">
  private static final String libOpenCV = Core.NATIVE_LIBRARY_NAME;
  private static final String libOpenCVclassref = "nu.pattern.OpenCV";
  private static String libOpenCVresname = "opencv/%s/x86_64/";
  private static boolean libOpenCVloaded = false;

  public static void loadOpenCV() {
    if (!libOpenCVloaded) {
      String libFileName = libOpenCV;
      String resName = libOpenCVresname;
      Class classRef = null;
      try {
        classRef = Class.forName(libOpenCVclassref);
      } catch (ClassNotFoundException e) {
        RunTime.terminate(999, "Commons.loadLib: %s: load failed",
                resName + "::" + libFileName);
      }
      if (Commons.runningWindows()) {
        libFileName += ".dll";
        resName = String.format(resName, "windows");
      } else if (Commons.runningMac()) {
        libFileName = "lib" + libOpenCV + ".dylib";
        resName = String.format(resName, "osx");
      } else if (Commons.runningLinux()) {
        libFileName = "lib" + libOpenCV + ".so";
        resName = String.format(resName, "linux");
      }
      if (doLoadLib(classRef, resName, libFileName)) {
        libOpenCVloaded = true;
      }
    }
  }

  private static final String jarLibsPath = "/sikulixlibs/";
  private static List<String> libsLoaded = new ArrayList<>();
  public static final String LIB_JXGRABKEY = "JXGrabKey";

  public static boolean loadLib(String libName) {
    if (!libsLoaded.contains(libName)) {
      String libFileName = libName;
      if (Commons.runningWindows()) {
        libFileName += ".dll";
      } else if (Commons.runningMac()) {
        libFileName = "lib" + libName + ".dylib";
      } else if (Commons.runningLinux()) {
        libFileName = "lib" + libName + ".so";
      }
      if (libName.equals(LIB_JXGRABKEY)) {
        if (doLoadLib(Commons.class, jarLibsPath, libFileName)) {
          libsLoaded.add(libName);
          return true;
        }
      }
      RunTime.terminate(999, "Commons.loadLib: %s");
    }
    return true;
  }

  private static boolean doLoadLib(Class classRef, String resPath, String fileName) {
    File fLibsFolder = Commons.getLibsFolder();
    if (fLibsFolder.exists()) {
      if (!Commons.hasVersionFile(fLibsFolder)) {
        FileManager.deleteFileOrFolder(fLibsFolder);
      }
    }
    if (!fLibsFolder.exists()) {
      fLibsFolder.mkdirs();
      if (!fLibsFolder.exists()) {
        RunTime.terminate(999, "Commons.loadLib: %s: create failed", fLibsFolder);
      }
      makeVersionFile(fLibsFolder);
    }
    File libFile = new File(fLibsFolder, fileName);
    try (FileOutputStream outFile = new FileOutputStream(libFile);
         InputStream inStream = classRef.getResourceAsStream(resPath + fileName)) {
      copy(inStream, outFile);
    } catch (Exception ex) {
      RunTime.terminate(999, "Commons.loadLib: %s: export failed", fileName);
    }
    try {
      System.load(libFile.getAbsolutePath());
    } catch (Exception e) {
      RunTime.terminate(999, "Commons.loadLib: %s: load failed" + libFile);
    }
    return true;
  }

  private static boolean didExport = false;

  public static boolean shouldExport() {
    return didExport;
  }

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
  //</editor-fold>

  //<editor-fold desc="30 Options handling">
  public static void show() {
    info("***** show environment for %s (%s)", Commons.getSXVersion(), Commons.getSxBuildStamp());
    info("running as: %s (%s)", getMainClassLocation(), getStartClass().getCanonicalName());
    info("running on: %s", Commons.getOSInfo());
    info("running Java: %s", Commons.getJavaInfo());
    info("java.io.tmpdir: %s", Commons.getTempFolder());
    info("app data folder: %s", Commons.getAppDataPath());
    info("work dir: %s", Commons.getWorkDir());
    info("user.home: %s", Commons.getUserHome());
    info("active locale: %s", globalOptions.getOption("SX_LOCALE"));
    if (hasOption(CommandArgsEnum.VERBOSE) || isJythonReady()) {
//      dumpClassPath("sikulix");
      if (isJythonReady()) {
        int saveLvl = Debug.getDebugLevel();
        Debug.setDebugLevel(3);
        Commons.runFunctionScriptingSupport("showSysPath", null);
        Screen.showMonitors();
        Debug.setDebugLevel(saveLvl);
      }
    }
    info("***** show environment end");
  }

  private static Options globalOptions = null;

  public static Options globals() {
    return globalOptions;
  }

  public static void showOptions() {
    showOptions("");
  }

  public static void showOptions(String prefix) {
    Map<String, String> options = globals().getOptions();
    TreeMap<String, String> sortedOptions = new TreeMap<>();
    sortedOptions.putAll(options);
    int len = 0;
    for (String key : sortedOptions.keySet()) {
      if (!key.startsWith(prefix)) {
        continue;
      }
      if (key.length() < len) {
        continue;
      }
      len = key.length();
    }
    String formKey = "%-" + len + "s";
    String formVal = " = %s";
    for (String key : sortedOptions.keySet()) {
      if (!key.startsWith(prefix)) {
        continue;
      }
      String val = sortedOptions.get(key);
      if (val.isEmpty()) {
        info(formKey, key);
      } else {
        info(formKey + formVal, key, val);
      }
    }
  }

  public static void initOptions() {
    if (globalOptions == null) {
      Options options = Options.create();
      // *************** add commandline args
      for (CommandArgsEnum arg : CommandArgsEnum.values()) {
        String val = "";
        if (hasArg(arg.shortname())) {
          if (arg.hasArgs()) {
            String[] args = getArgs(arg.shortname());
            if (args.length > 1) {
              for (int n = 0; n < args.length; n++) {
                val += "|" + args[n];
              }
            } else {
              val = args[0];
            }
          }
          options.setOption("ARG_" + arg.toString(), (val == null ? "" : val));
        }
      }
      options.setOption("SX_JAR", getMainClassLocation().getAbsolutePath());
      String prop = System.getProperty("sikuli.Debug");
      if (prop != null) {
        options.setOption("SX_DEBUG_LEVEL", prop);
      }
      prop = System.getProperty("sikuli.console");
      if (prop != null) {
        if (prop.equals("false")) {
          if (!hasOption(CONSOLE)) {
            options.setOption("ARG_" + CONSOLE.name(), "");
          }
        }
      }
      globalOptions = options;
    }
  }

  public static boolean hasOption(CommandArgsEnum option) {
    return hasOption("ARG_" + option.name());
  }

  public static boolean hasOption(String option) {
    if (globalOptions == null) {
      return false;
    }
    return globalOptions.hasOption(option);
  }

  public static Options getOptions() {
    return sxOptions;
  }

  public static void setOptions(Options options) {
    sxOptions = options;
  }

  private static Options sxOptions = null;
  //</editor-fold>

  //<editor-fold desc="80 image handling">

  public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
    final AffineTransform af = new AffineTransform();
    af.scale((double) width / originalImage.getWidth(),
        (double) height / originalImage.getHeight());
    final AffineTransformOp operation = new AffineTransformOp(
        af, AffineTransformOp.TYPE_BILINEAR);
    BufferedImage rescaledImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);
    rescaledImage = operation.filter(originalImage, rescaledImage);
    return rescaledImage;
  }

  /**
   * Available resize interpolation algorithms
   */
  public enum Interpolation {
    NEAREST(Imgproc.INTER_NEAREST),
    LINEAR(Imgproc.INTER_LINEAR),
    CUBIC(Imgproc.INTER_CUBIC),
    AREA(Imgproc.INTER_AREA),
    LANCZOS4(Imgproc.INTER_LANCZOS4),
    LINEAR_EXACT(Imgproc.INTER_LINEAR_EXACT),
    MAX(Imgproc.INTER_MAX);

    private int value;

    Interpolation(int value) {
      this.value = value;
    }
  }

  /**
   * resize the given image with factor using OpenCV ImgProc.resize()
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param bimg   given image
   * @param factor resize factor
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public static BufferedImage resize(BufferedImage bimg, float factor) {
    return resize(bimg, factor, Interpolation.CUBIC);
  }

  /**
   * resize the given image with factor using OpenCV ImgProc.resize()
   *
   * @param bimg          given image
   * @param factor        resize factor
   * @param interpolation algorithm used for pixel interpolation
   * @return a new BufferedImage resized (width*factor, height*factor)
   */
  public static BufferedImage resize(BufferedImage bimg, float factor, Interpolation interpolation) {
    return getBufferedImage(cvResize(bimg, factor, interpolation));
  }

  /**
   * resize the given image (as cvMat in place) with factor using OpenCV ImgProc.resize()<br>
   * <p>
   * Uses CUBIC as the interpolation algorithm.
   *
   * @param mat    given image as cvMat
   * @param factor resize factor
   */
  public static void resize(Mat mat, float factor) {
    resize(mat, factor, Interpolation.CUBIC);
  }

  /**
   * resize the given image (as cvMat in place) with factor using OpenCV ImgProc.resize()<br>
   *
   * @param mat           given image as cvMat
   * @param factor        resize factor
   * @param interpolation algorithm used for pixel interpolation.
   */
  public static void resize(Mat mat, float factor, Interpolation interpolation) {
    cvResize(mat, factor, interpolation);
  }

  private static Mat cvResize(BufferedImage bimg, double rFactor, Interpolation interpolation) {
    Mat mat = makeMat(bimg);
    cvResize(mat, rFactor, interpolation);
    return mat;
  }

  private static void cvResize(Mat mat, double rFactor, Interpolation interpolation) {
    int newW = (int) (rFactor * mat.width());
    int newH = (int) (rFactor * mat.height());
    Imgproc.resize(mat, mat, new Size(newW, newH), 0, 0, interpolation.value);
  }

  public static Mat getNewMat() {
    return new Mat();
  }

  public static Mat getNewMat(Size size, int type, int fill) {
    switch (type) {
      case 1:
        type = CvType.CV_8UC1;
        break;
      case 3:
        type = CvType.CV_8UC3;
        break;
      case 4:
        type = CvType.CV_8UC4;
        break;
      default:
        type = -1;
    }
    if (type < 0) {
      return new Mat();
    }
    Mat result;
    if (fill < 0) {
      result = new Mat(size, type);
    } else {
      result = new Mat(size, type, new Scalar(fill));
    }
    return result;
  }

  public static List<Mat> getMatList(BufferedImage bImg) {
    byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
    Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
    aMat.put(0, 0, data);
    List<Mat> mats = new ArrayList<Mat>();
    Core.split(aMat, mats);
    return mats;
  }

  public static Mat makeMat(BufferedImage bImg) {
    return makeMat(bImg, true);
  }

  public static Mat makeMat(BufferedImage bImg, boolean asBGR) {
    if (bImg.getType() == BufferedImage.TYPE_INT_RGB) {
      int[] data = ((DataBufferInt) bImg.getRaster().getDataBuffer()).getData();
      ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      intBuffer.put(data);
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
      aMat.put(0, 0, byteBuffer.array());
      Mat oMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      Mat oMatA = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      List<Mat> mixIn = new ArrayList<Mat>(Arrays.asList(new Mat[]{aMat}));
      List<Mat> mixOut = new ArrayList<Mat>(Arrays.asList(new Mat[]{oMatA, oMatBGR}));
      //A 0 - R 1 - G 2 - B 3 -> A 0 - B 1 - G 2 - R 3
      Core.mixChannels(mixIn, mixOut, new MatOfInt(0, 0, 1, 3, 2, 2, 3, 1));
      return oMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_3BYTE_BGR) {
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED
        || bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      String bImgType = "BYTE_BINARY";
      if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
        bImgType = "BYTE_INDEXED";
      }
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg, 0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR
        || bImg.getType() == BufferedImage.TYPE_CUSTOM) {
      List<Mat> mats = getMatList(bImg);
      Size size = mats.get(0).size();
      if (!asBGR || bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
        Mat mBGRA = getNewMat(size, 4, -1);
        mats.add(mats.remove(0));
        Core.merge(mats, mBGRA);
        return mBGRA;
      } else {
        Mat mBGR = getNewMat(size, 3, -1);
        mats.remove(0);
        Core.merge(mats, mBGR);
        return mBGR;
      }
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
      aMat.put(0, 0, data);
      return aMat;
    } else if (bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
      BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics graphics = bimg3b.getGraphics();
      graphics.drawImage(bImg, 0, 0, null);
      byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
      Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
      aMatBGR.put(0, 0, data);
      return aMatBGR;
    } else {
      error("makeMat: BufferedImage: type not supported: %d --- please report this problem", bImg.getType());
    }
    return getNewMat();
  }

  public final static String PNG = "png";
  public final static String dotPNG = "." + PNG;

  public static BufferedImage getBufferedImage(Mat mat) {
    return getBufferedImage(mat, dotPNG);
  }

  public static BufferedImage getBufferedImage(Mat mat, String type) {
    BufferedImage bImg = null;
    MatOfByte bytemat = new MatOfByte();
    if (SX.isNull(mat)) {
      mat = getNewMat();
    }
    Imgcodecs.imencode(type, mat, bytemat);
    byte[] bytes = bytemat.toArray();
    InputStream in = new ByteArrayInputStream(bytes);
    try {
      bImg = ImageIO.read(in);
    } catch (IOException ex) {
      error("getBufferedImage: %s error(%s)", mat, ex.getMessage());
    }
    return bImg;
  }
  //</editor-fold>

  //<editor-fold desc="90 reflections">
  public static Object runFunctionScriptingSupport(String function, Object[] args) {
    return runFunctionScriptingSupport(null, function, args);
  }

  public static Object runFunctionScriptingSupport(Object reference, String function, Object[] args) {
    Class<?> classSup = null;
    if (reference == null || (reference instanceof String && ((String) reference).contains("org.python"))) {
      try {
        classSup = Class.forName("org.sikuli.support.ide.JythonSupport");
      } catch (ClassNotFoundException e) {
        RunTime.terminate(999, "Commons: JythonSupport: %s", e.getMessage());
      }
    } else if (reference instanceof String && ((String) reference).contains("org.jruby")) {
      try {
        classSup = Class.forName("org.sikuli.support.ide.JRubySupport");
      } catch (ClassNotFoundException e) {
        RunTime.terminate(999, "Commons: JRubySupport: %s", e.getMessage());
      }
    } else {
      RunTime.terminate(999, "Commons: ScriptingSupport: not supported: %s", reference);
    }
    Object returnSup = null;
    String error = "";
    Object instanceSup = null;
    Method method = null;
    try {
      instanceSup = classSup.getMethod("get", null).invoke(null, null);
      if (args == null) {
        method = classSup.getMethod(function, null);
        returnSup = method.invoke(instanceSup);
      } else {
        method = classSup.getMethod(function, new Class[]{Object[].class});
        returnSup = method.invoke(instanceSup, args);
      }
    } catch (NoSuchMethodException e) {
      error = e.toString();
    } catch (IllegalAccessException e) {
      error = e.toString();
    } catch (InvocationTargetException e) {
      error = e.toString();
    } catch (IllegalArgumentException e) {
      error = e.toString();
    }
    if (!error.isEmpty()) {
      RunTime.terminate(999, "Commons: runScriptingSupportFunction(%s, %s, %s): %s",
          instanceSup, method, args, error);
    }
    return returnSup;
  }
  //</editor-fold>

  //<editor-fold desc="99 stuff">
  public static void browse(String url) {
    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(new URI(url));
      } catch (IOException e) {
      } catch (URISyntaxException e) {
      }
    }
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
      error("Parameters not valid!");
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

  public static Object[] reverseArray(Object[] anArray) {
    for (int i = 0; i < anArray.length / 2; i++) {
      Object temp = (Object) anArray[i];
      anArray[i] = anArray[anArray.length - i - 1];
      anArray[anArray.length - i - 1] = temp;
    }
    return anArray;
  }

  public static boolean isOdd(int number) {
    return number % 2 == 0;
  }
  //</editor-fold>
}
