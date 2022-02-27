/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.apache.commons.cli.CommandLine;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.HotkeyManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.script.runners.ProcessRunner;
import org.sikuli.script.support.devices.Devices;
import org.sikuli.script.support.devices.HelpDevice;
import org.sikuli.script.support.devices.MouseDevice;
import org.sikuli.script.support.devices.ScreenDevice;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.sikuli.util.CommandArgsEnum.*;

public class Commons {

  //TODO force early Commons static initializer (RunTime)
  public static void init() {
  }

  //<editor-fold desc="00 static">
  private static Class COMMONS_CLASS = Commons.class;
  private static final long START_MOMENT;
  protected static boolean RUNNINGIDE = false;
  static PrintStream SX_PRINTOUT;
  private static Options GLOBAL_OPTIONS = null;
  static String GLOBAL_LOG = "";

  private static final String SX_VERSION;
  private static final String SX_VERSION_LONG;
  private static final String SX_VERSION_SHORT;
  private static final String SX_BUILD;
  private static final String SX_BUILD_STAMP;

  private static final String SYSWIN = "windows";
  private static final String SYSMAC = "mac";
  private static final String SYSMACM1 = "macm1";
  private static final String SYSLUX = "linux";

  private static final String OS_NAME;
  private static final String OS_VERSION;
  private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
  private static final String ARCHM1 = "aarch64";

  public static String getSysName() {
    return runningWindows() ? SYSWIN : (runningMac() ? (runningMacM1() ? SYSMACM1 : SYSMAC) : SYSLUX);
  }

  public static boolean runningWindows() {
    return OS_NAME.startsWith(SYSWIN);
  }

  public static boolean runningMac() {
    return OS_NAME.startsWith(SYSMAC);
  }

  public static boolean runningMacM1() {
    return runningMac() && ARCHM1.equals(OS_ARCH);
  }

  public static boolean runningLinux() {
    return !runningMac() && !runningWindows();
  }

  private static final String SX_TEMP_DIR = System.getProperty("java.io.tmpdir");
  private static File SX_TEMP_FOLDER = null;

  private static Locale SX_LOCALE = new Locale("en", "US");

  public static boolean isRunningIDE() {
    if (RUNNINGIDE &&
        !hasOption("SX_ARG_RUN") &&
        !hasOption("SX_ARG_RUNSERVER") &&
        !hasOption("SX_ARG_RUNPYSERVER")) {
      return true;
    }
    return false;
  }

  public static boolean isRunningPackage() {
    if (!RUNNINGIDE) return false;
    File packFolder = getMainClassLocation().getParentFile().getParentFile();
    if (new File(packFolder, "app").exists() && new File(packFolder, "runtime").exists()
        && new File(packFolder, "runtime/release").exists()) {
      return true;
    }
    return false;
  }

  private static JFrame SXIDE = null;

  public static JFrame getSXIDE() {
    return SXIDE;
  }

  public static void setSXIDE(JFrame SXIDE) {
    Commons.SXIDE = SXIDE;
  }

  public static Region getSXIDERegion() {
    Region regIDE = new Region(0, 0, 1, 1);
    regIDE.setName("***SXIDE***");
    return regIDE;
  }

  private static boolean SNAPSHOT = false;
  public static boolean isSnapshot() {
    return SNAPSHOT;
  }

  private static File IS_RUNNING_FILE = null;
  private static FileOutputStream IS_RUNNING_STREAM;

  public static void setIsRunning(File file, FileOutputStream stream) {
    IS_RUNNING_FILE = file;
    IS_RUNNING_STREAM = stream;
  }

  private static void runShutdownHook() {
    debug("***** final cleanup at System.exit() *****");
    //TODO runShutdownHook cleanUp();
    if (IS_RUNNING_FILE != null) {
      try {
        IS_RUNNING_STREAM.close();
      } catch (IOException ex) {
      }
      IS_RUNNING_FILE.delete();
    }
    if (GLOBAL_OPTIONS != null) {
      saveGlobalOptions();
    }
    if (!GLOBAL_LOG.isEmpty()) {
      File logFile = asFile(getUserHome(), "sikulixide_startlog.txt");
      FileManager.writeStringToFile(GLOBAL_LOG, logFile);
    }
    if (SX_PRINTOUT != null) {
      SX_PRINTOUT.close();
    }
  }

  static {
    START_MOMENT = new Date().getTime();

    String caller = Thread.currentThread().getStackTrace()[2].getClassName();
    if (caller.contains(".ide.")) {
      RUNNINGIDE = true;
    }

    if (!System.getProperty("os.arch").contains("64")) {
      String msg = "SikuliX fatal Error: System must be 64-Bit";
      if (RUNNINGIDE) {
        terminate(254, msg);
      } else {
        throw new SikuliXception(msg);
      }
    }

    if (!"64".equals(System.getProperty("sun.arch.data.model"))) {
      String msg = "SikuliX fatal Error: Java must be 64-Bit";
      if (RUNNINGIDE) {
        terminate(254, msg);
      } else {
        throw new SikuliXception(msg);
      }
    }

    //TODO check with Java 18 for correct Windows 11 version + macOS version
    if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
      OS_NAME = "windows";
      if (System.getProperty("os.version").toLowerCase().startsWith("1")) {
        String[] sysInfo = ProcessRunner.run("cmd", "/C", "systeminfo").split("\n");
        String v1 = sysInfo[2].trim().split(":")[1].trim().split(" ")[2];
        String v2 = sysInfo[3].trim().split(":")[1].trim().split(" ")[0];
        if (v1.equals("11")) {
          OS_VERSION = "11";
        } else {
          OS_VERSION = System.getProperty("os.version").toLowerCase();
        }
      } else {
        OS_VERSION = System.getProperty("os.version").toLowerCase();
      }
    } else {
/*TODO macOS version is 11.5 instead of 11.6
% sw_vers
ProductName:	macOS
ProductVersion:	11.6
BuildVersion:	20G165
... or ...
%system_profiler SPSoftwareDataType
Software:
    System Software Overview:
      System Version: macOS 11.6 (20G165)
*/
      OS_NAME = System.getProperty("os.name").toLowerCase();
      OS_VERSION = System.getProperty("os.version").toLowerCase();
    }

    Properties sxProps = new Properties();
    String svf = "/Settings/sikulixversion.txt";
    if (RUNNINGIDE) {
      svf = "/Settings/sikulixversionide.txt";
    }
    try {
      InputStream is;
      is = Commons.class.getResourceAsStream(svf);
      if (is == null) {
        String msg = String.format("SikuliX fatal Error: not found on classpath: %s", svf);
        if (RUNNINGIDE) {
          terminate(254, msg);
        } else {
          throw new SikuliXception(msg);
        }
      }
      sxProps.load(is);
      is.close();
    } catch (IOException e) {
      String msg = String.format("SikuliX fatal Error: load did not work: %s (%s)", svf, e.getMessage());
      if (RUNNINGIDE) {
        terminate(254, msg);
      } else {
        throw new SikuliXception(msg);
      }
    }
    //    sikulixvproject=2.0.0  or 2.1.0-SNAPSHOT
    SX_VERSION = sxProps.getProperty("sikulixvproject");
    //    sikulixbuild=2019-10-17_09:58
    SX_BUILD = sxProps.getProperty("sikulixbuild");
    SX_BUILD_STAMP = SX_BUILD
        .replace("_", "").replace("-", "").replace(":", "")
        .substring(0, 12);
    SX_VERSION_LONG = SX_VERSION + String.format("-%s", SX_BUILD_STAMP);
    SX_VERSION_SHORT = SX_VERSION.replace("-SNAPSHOT", "");
    if (SX_VERSION.contains("-SNAPSHOT")) {
      SNAPSHOT = true;
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> runShutdownHook()));
  }
  //</editor-fold>

  //<editor-fold desc="01 logging">
  public static long timeNow() {
    return new Date().getTime();
  }

  public static long timeSince(long start) {
    return new Date().getTime() - start;
  }

  static File SX_LOGFILE = null;

  public static void setLogFile(File file) {
    try {
      PrintStream printoutNew = new PrintStream(file);
      SX_LOGFILE = file;
      if (SX_PRINTOUT != null) {
        SX_PRINTOUT.close();
      }
      SX_PRINTOUT = printoutNew;
    } catch (Exception ex) {
      terminate(999, "Commons::setLogFile: not possible: %s", ex.getMessage());
    }
  }

  public static void resetLogFile() {
    try {
      PrintStream printoutNew = System.out;
      if (SX_PRINTOUT != null) {
        SX_PRINTOUT.close();
      }
      SX_PRINTOUT = printoutNew;
    } catch (Exception ex) {
      terminate(999, "Commons::resetLogFile: not possible: %s", ex.getMessage());
    }
  }

  public static File getLogFile() {
    return SX_LOGFILE;
  }

  public static PrintStream getLogStream() {
    if (SX_PRINTOUT == null) {
      SX_PRINTOUT = System.out;
    }
    return SX_PRINTOUT;
  }

  private static void printlnOut(String msg, Object... args) {
    printOut(msg + "\n", args);
  }

  private static void printOut(String msg, Object... args) {
    getLogStream().printf(msg, args);
  }

  public static synchronized void addlog(String msg) {
    if (SNAPSHOT) {
      GLOBAL_LOG += String.format("[SXGLOBAL %4.3f] ", getSinceStart()) + msg + System.lineSeparator();
    }
  }

  public static void info(String msg, Object... args) {
    if (!isQuiet()) {
      printOut("[SXINFO] " + msg + "%n", args);
    }
  }

  public static void error(String msg, Object... args) {
    if (!isQuiet()) {
      printOut("[SXERROR] " + msg + "%n", args);
    }
  }

  public static void debug(String msg, Object... args) {
    if (isDebug()) {
      printOut("[SXDEBUG] " + msg + "%n", args);
    }
  }

  private static final int DEBUG_LEVEL_QUIET = -9999;
  private static int debugLevel = 0;
  private static boolean verbose = false;

  public static void setDebug() {
    debugLevel = 3;
  }

  public static boolean isDebug() {
    return debugLevel > 2;
  }

  public static void setQuiet() {
    debugLevel = DEBUG_LEVEL_QUIET;
  }

  public static boolean isQuiet() {
    return debugLevel < 0;
  }

  public static void setVerbose() {
    setDebug();
    verbose = true;
  }

  public static boolean isVerbose() {
    return verbose;
  }

  private static boolean traceEnterExit = false;

  public static boolean isTraceEnterExit() {
    return traceEnterExit;
  }

  public static void startTraceEnterExit() {
    traceEnterExit = true;
  }

  public static void stopTraceEnterExit() {
    traceEnterExit = false;
  }

  private static boolean trace = false;

  public static boolean isTrace() {
    return trace;
  }

  public static void startTrace() {
    trace = true;
  }

  public static void stopTrace() {
    trace = false;
  }

  public static String trace() {
    return trace(null);
  }

  public static String trace(String msg, Object... args) {
    if (isTrace() || msg == null) {
      int functionIndex = msg == null ? 3 : 2;
      StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[functionIndex];
      String className = stackTrace.getFileName().replace(".java", "");
      String methodName = stackTrace.getMethodName();
      int lineNumber = stackTrace.getLineNumber();
      printOut(String.format("[%d_%s::%s] ", lineNumber, className, methodName));
      if (msg != null && !msg.isEmpty()) {
        String out = String.format(msg, args);
        out = out.replace("\n\n", "\n");
        out = out.replace("\n\n", "\n");
        printOut(out);
      }
      printOut("\n");
      return methodName;
    }
    return "";
  }

  public static String enter(String method, String parameter, Object... args) {
    String parms = String.format(parameter, args);
    if (isTraceEnterExit()) {
      printOut("[TRACE enter] " + method + "(" + parms + ")%n");
    }
    return "parameter(" + parms.replace("%", "%%") + ")";
  }

  public static void exit(String method, String returns, Object... args) {
    if (isTraceEnterExit()) {
      printOut("[TRACE exit] " + method + ": " + returns + "%n", args);
    }
  }
  //</editor-fold>

  //<editor-fold desc="02 startup / terminate">
  static final String[] xmlDate = {""};

  public static synchronized String setCurrentSnapshotDate(String date) {
    if (!date.isEmpty()) {
      String thisDate = SX_BUILD_STAMP.substring(0, 8);
      if (Integer.parseInt(date) > Integer.parseInt(thisDate)) {
        xmlDate[0] = date;
      }
    }
    return xmlDate[0];
  }

  public static String getCurrentSnapshotDate() {
    return setCurrentSnapshotDate("");
  }

  public static double getSinceStart() {
    return (new Date().getTime() - START_MOMENT) / 1000.0;
  }

  public static Locale getLocale() {
    return SX_LOCALE;
  }

  public static void setLocale(Locale locale) {
    SX_LOCALE = locale;
    setOption("SX_LOCALE", locale); //TODO
  }

  public static boolean isSandBox() {
    return hasStartArg(APPDATA) && APP_DATA_SANDBOX != null;
  }

  public static void checkAccessibility() {
    Devices.start(Devices.TYPE.SCREEN);
    //check Mouse
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Devices.start(Devices.TYPE.MOUSE);
        } catch (Exception e) {
          System.out.println("");
        }
      }
    });
    if (Commons.runningMac()) {
      //macOS: check Screen capture
      Rectangle srect = ScreenDevice.primary().asRectangle();
      BufferedImage screenImage = ScreenDevice.getRobot(0).captureScreen(srect).getImage(); // checkAccessibility
      DataBuffer data = screenImage.getData(new Rectangle(200, 10, srect.width - 200, 1)).getDataBuffer();
      int min = data.getElem(0);
      int max = data.getElem(0);
      for (int n = 0; n < data.getSize(); n++) {
        min = Math.min(min, data.getElem(n));
        max = Math.max(max, data.getElem(n));
      }
      Color cmin = new Color(min);
      Color cmax = new Color(max);
      boolean singleColor =
          (Math.abs(cmax.getRed() - cmin.getRed()) < 2) &&
              (Math.abs(cmax.getGreen() - cmin.getGreen()) < 2) &&
              (Math.abs(cmax.getBlue() - cmin.getBlue()) < 2);
      if (singleColor) {
        ScreenDevice.isUseable(false);
      }
    } else {
      pause(0.5); //Windows: wait for threaded Mouse check
    }
    if (!MouseDevice.isUseable()) {
      System.out.println( //TODO mouse blocked message
          "*****************************************************\n" +
              "    Mouse/Key features are blocked - not useable\n" +
              "*****************************************************");
    }
    if (!ScreenDevice.isUseable()) {
      System.out.println( //TODO screenshots blocked message
          "*****************************************************\n" +
              "  Screenshots blocked - find on screen not useable\n" +
              "*****************************************************");
    }
  }

  public static boolean isCaptureBlocked() {
    return !ScreenDevice.isUseable();
  }

  private static Class startClass = null;

  public static Class getStartClass() {
    return startClass;
  }

  public static void setStartClass(Class startClass) {
    String caller = Thread.currentThread().getStackTrace()[2].getClassName();
    if (caller.startsWith("org.sikuli.ide.Sikulix") || caller.startsWith("org.sikuli.script.Sikulix")) {
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

  private static CommandLine cmdLine = null;
  private static CommandArgs cmdArgs = null;
  private static String[] userArgs = new String[0];
  private static String STARTUPFILE = null;
  private static String STARTUPINFO = null;
  private static List<String> STARTUPLINES = new ArrayList<>();
  private static List<String> STARTUPARGS = new ArrayList<>();
  private static List<File> FILESTOLOAD = new ArrayList<>();

  public static void setStartupFile(String fileName) {
    STARTUPFILE = fileName;
  }

  public static List<File> getFilesToLoad() {
    return FILESTOLOAD;
  }

  public static void addFilesToLoad(List<File> files) {
    FILESTOLOAD.addAll(files);
  }

  public static void setStartArgs(String[] args) {
    int iArgs = 0;
    if (args.length > 0) {
      if (!args[0].isEmpty() && args[0].endsWith("sikulixide")) {
        if (STARTUPFILE == null) {
          STARTUPFILE = args[0];
        }
        iArgs = 1;
      }
      if (args.length > iArgs) {
        for (int i = iArgs; i < args.length; i++) {
          STARTUPARGS.add(args[i]);
        }
      }
    }

    if (runningMac()) {
      pause(getSinceStart() / 4);
    }

    if (STARTUPFILE != null) {
      //Commons.addlog("Commons::setStartArgs(args): STARTUPFILE: " + STARTUPFILE);
      File startupFile = asFile(STARTUPFILE);
      if (!startupFile.exists()) {
        STARTUPFILE = null;
      } else {
        File startupLogFile = new File(startupFile.getParentFile(),
            startupFile.getName().replaceAll("\\.", "-") + ".log");
        FileManager.writeStringToFile("***** startupLogFile *****", startupLogFile);
        Commons.setLogFile(startupLogFile);
        STARTUPINFO = FileManager.readFileToString(startupFile);
        String[] info = STARTUPINFO.split(System.lineSeparator());
        if (info.length > 0) {
          for (String line : info) {
            line = line.strip();
            if (!line.isEmpty()) {
              if (line.startsWith("/") || line.startsWith("#")) {
                continue;
              }
              STARTUPLINES.add(line);
            }
          }
        }
        if (STARTUPLINES.size() > 0) {
          List<String> lines = new ArrayList<>();

          for (String line : STARTUPLINES) {
            if (line.startsWith("-")) {
              String[] parms = parmStringToArray(line);
              STARTUPARGS.addAll(0, List.of(parms));
              continue;
            }
            lines.add(line);
          }
          STARTUPLINES = lines;
        }
      }
    } else {
      resetLogFile();
    }
    if (STARTUPARGS.size() > 0) {
      cmdArgs = new CommandArgs(Commons.RUNNINGIDE);
      cmdLine = cmdArgs.getCommandLine(STARTUPARGS.toArray(new String[0]));
      if (cmdLine != null) {
        userArgs = cmdArgs.getUserArgs();
      }
    }
  }

  public static String[] getUserArgs() {
    return userArgs;
  }

  public static void printHelp() {
    cmdArgs.printHelp();
  }

  public static boolean hasStartArg(CommandArgsEnum option) {
    if (option.equals(DEBUG)) {
      String prop = System.getProperty("sikuli.Debug");
      if (prop != null) {
        return true;
      }
    } else if (option.equals(CONSOLE)) {
      if (isRunningPackage() && STARTUPFILE != null) {
        return false;
      }
      String prop = System.getProperty("sikuli.console");
      if (prop != null) {
        return true;
      }
    }
    if (cmdLine != null && cmdLine.hasOption(option.shortname())) {
      return true;
    }
    return false;
  }

  public static String getStartArg(CommandArgsEnum option) {
    String val = "";
    if (hasStartArg(option) && !option.hasArgs()) {
      val = cmdLine.getOptionValue(option.shortname());
      val = val == null ? "" : val;
    }
    return val;
  }

  public static boolean hasExtendedArg(String option) {
    if (cmdArgs != null && cmdArgs.getExtendedArgs().contains(option)) {
      return true;
    }
    return false;
  }

  public static void terminate() {
    terminate(0, "");
  }

  public static void terminate(int retval, String message, Object... args) {
    String outMsg = String.format(message, args);
    if (retval < 999) {
      if (!outMsg.isEmpty()) {
        Commons.printlnOut("TERMINATING: " + outMsg);
      }
      cleanUp();
      System.exit(retval);
    }
    throw new SikuliXception(String.format("FATAL: " + outMsg));
  }

  public static void cleanUp() {
    HotkeyManager.reset(true);
    HelpDevice.stopAll();
  }

  public static void cleanUpAfterScript() {
    HotkeyManager.reset(false);
    HelpDevice.stopAll();
  }
  //</editor-fold>

  //<editor-fold desc="05 standard directories">
  public static File setTempFolder() {
    if (null == SX_TEMP_FOLDER) {
      SX_TEMP_FOLDER = new File(SX_TEMP_DIR);
      SX_TEMP_FOLDER.mkdirs();
    }
    return SX_TEMP_FOLDER;
  }

  public static File setTempFolder(File folder) {
    SX_TEMP_FOLDER = folder;
    SX_TEMP_FOLDER.mkdirs();
    return SX_TEMP_FOLDER;
  }

  public static File getTempFolder() {
    return setTempFolder();
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
        setAppDataPath("");
        error("Commons.getAppDataPath: standard place not possible - using: %s", appDataPath);
      }
    }
    return appDataPath;
  }

  public static File setAppDataPath(String givenAppPath) {
    if (givenAppPath.isEmpty()) {
      givenAppPath = "SikulixAppData";
    }
    appDataPath = new File(givenAppPath);
    if (givenAppPath.startsWith("~/")) {
      appDataPath = new File(getUserHome(), givenAppPath);
    } else if (givenAppPath.startsWith("./")) {
      appDataPath = new File(getWorkDir(), givenAppPath);
    }
    if (!appDataPath.isAbsolute()) {
      appDataPath = new File(getWorkDir(), givenAppPath);
    }
    appDataPath.mkdirs();
    if (!appDataPath.exists()) {
      terminate(999, "Commons: setAppDataPath: %s (%s)", givenAppPath, "not created/not exists");
    }
    APP_DATA_SANDBOX = new File(appDataPath.getAbsolutePath());
    return appDataPath;
  }

  private static File appDataPath = null;
  private static File APP_DATA_SANDBOX = null;

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
      error("Commons.getUserHome: env: user.home not valid: %s (trying work-dir)", userHomePath);
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
      terminate(999, "Commons.getWorkDir: env: user.dir not valid: %s (exiting)", workDirPath);
    }
    return workDir;
  }

  private static File workDir = null;

  public static String getJarLibsPath() {
    return "/sikulixlibs/" + Commons.getSysName() + "/libs";
  }

  public static final String userLibsPath = "SIKULIX_LIBS";

  public static String getUserLibsPath() {
    return System.getenv(userLibsPath);
  }

  public static File getFromExternalLibsFolder(String libName) {
    String libsPath = getUserLibsPath();
    if (libsPath == null) {
      return null;
    }
    String[] paths = libsPath.split(File.pathSeparator);
    File libFile = null;
    for (String path : paths) {
      if (new File(path, libName).exists()) {
        libFile = new File(path, libName);
        break;
      }
    }
    return libFile;
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
  public static boolean runningIDE() {
    return RUNNINGIDE;
  }

  public static String getSXVersion() {
    return SX_VERSION;
  }

  public static String getSXVersionIDE() {
    return "SikulixIDE-" + SX_VERSION;
  }

  public static String getSXVersionAPI() {
    return "SikulixAPI-" + SX_VERSION;
  }

  public static String getSXVersionLong() {
    return SX_VERSION_LONG;
  }

  public static String getSXVersionShort() {
    return SX_VERSION_SHORT;
  }

  public static String getSXBuild() {
    return SX_BUILD;
  }

  public static String getSxBuildStamp() {
    return SX_BUILD_STAMP;
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
    String libToken = String.format("%s_%s_MadeForSikuliX%s%s.txt",
        Commons.getSXVersionShort(), Commons.getSxBuildStamp(),
        Commons.runningMac() ? "M" : (Commons.runningWindows() ? "W" : "L"),
        Commons.getOSArch());
    FileManager.writeStringToFile("*** Do not delete this file ***\n", new File(folder, libToken));
  }

  public static String getOSName() {
    return OS_NAME;
  }

  public static String getOSVersion() {
    return OS_VERSION;
  }

  public static String getOSArch() {
    return OS_ARCH;
  }

  public static String getOSInfo() {
    String info = OS_VERSION + " (" + OS_ARCH + ")";
    if (runningWindows()) return "Windows " + info;
    if (runningMac()) return "macOS " + info;
    return System.getProperty("os.name") + " " + info;
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

  public static void getStatus() {
    printlnOut("***** System Information Dump *****");
    printlnOut(String.format("*** SystemInfo\n%s", getSystemInfo()));
    System.getProperties().list(getLogStream());
    printlnOut("*** System Environment");
    for (String key : System.getenv().keySet()) {
      printlnOut(String.format("%s = %s", key, System.getenv(key)));
    }
    printlnOut("*** Java Class Path");
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URL[] urls = sysLoader.getURLs();
    for (int i = 0; i < urls.length; i++) {
      printlnOut(String.format("%d: %s", i, urls[i]));
    }
    printlnOut("***** System Information Dump ***** end *****");
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
  public static URL makeURL(Object main) {
    return makeURL(main, null);
  }

  public static URL makeURL(Object main, String sub) {
    String enter = enter("makeURL", "main: %s, sub: %s", main, sub);
    //debug("makeURL: main: %s", main); //TODO
    if (main == null) {
      error("makeURL: 1st parameter main is null");
      return null;
    }
    URL url = null;
    File mainFile = null;
    String mainPath = "";
    boolean isJar = false;
    if (main instanceof File) {
      mainFile = (File) main;
    } else if (main instanceof String) {
      mainPath = (String) main;
      mainFile = new File(mainPath);
    } else if (main instanceof URL) {
      URL mainURL = (URL) main;
      if (sub != null && !sub.isEmpty()) {
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
    } else if (main instanceof Class) {

    }
    if (!mainFile.isAbsolute() && (mainPath.startsWith("http:") || mainPath.startsWith("https:"))) {
      if (sub != null && !sub.isEmpty()) {
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
    } else {
      if (mainFile.getPath().endsWith(".jar") || mainFile.getPath().contains(".jar!")) {
        isJar = true;
      }
      if (!isJar && sub != null && !sub.isEmpty()) {
        mainFile = new File(mainFile, sub);
      }
      if (mainFile.getPath().contains("%")) {
        try {
          mainFile = new File(URLDecoder.decode(mainFile.getPath(), "UTF-8"));
        } catch (Exception e) {
          error("makeURL: mainFile with %%: not decodable(UTF-8): %s (%s)", mainFile, e.getMessage());
        }
      }
      try {
        if (!isJar) {
          if (!mainFile.isAbsolute()) {
            mainFile = new File(getWorkDir(), mainFile.getPath());
          }
          mainFile = mainFile.getCanonicalFile();
          if (!mainFile.exists()) {
            if (main instanceof String) {
              url = makeClassURL((String) main);
            }
            if (url == null) {
              error(enter);
              error("makeURL(%s): file does not exist and is not a class resource", main);
            }
          } else {
            url = mainFile.toURI().toURL();
          }
        } else {
          String[] parts = mainFile.getPath().split("\\.jar");
          String jarPart = parts[0] + ".jar";
          if (!new File(jarPart).exists()) {
            throw new IOException();
          }
          String subPart = sub != null ? sub : "";
          if (parts.length > 1 && parts[1].length() > 1) {
            subPart = (parts[1].startsWith("!") ? parts[1].substring(2) : parts[1].substring(1));
            if (sub != null && !sub.isEmpty()) {
              subPart += "/" + sub;
            }
          }
          subPart = "!/" + subPart.replace("\\", "/");
          subPart = subPart.replace("//", "/");
          url = new URL("jar:file:" + jarPart + subPart);
        }
      } catch (MalformedURLException e) {
        error(enter);
        error("makeURL: malformed: %s", mainFile);
      } catch (IOException e) {
        error(enter);
        error("makeURL: not found: %s", mainFile);
      }
    }

    //debug("makeURL returns: url: %s", url); //TODO
    exit("makeURL", "url: %s", url);
    return url;
  }

  public static URL makeClassURL(String sClass) {
    //debug("makeClassURL: sClass: %s", sClass); //TODO
    if (sClass == null || sClass.isEmpty()) {
      return null;
    }
    URL url = null;
    Class<?> aClass = null;
    sClass = sClass.replace("\\", "/");
    String[] parts = sClass.split("/");
    String possibleClass = parts[0];
    if (possibleClass.endsWith(":")) {
      return null;
    }
    try {
      aClass = Class.forName(possibleClass);
      url = aClass.getResource("/" + possibleClass.replace(".", "/") + ".class");
    } catch (ClassNotFoundException e) {
      error("makeURL(%s): class does not exist: %s", sClass, possibleClass);
      return null;
    }
    if (parts.length > 1) {
      url = aClass.getResource(sClass.substring(possibleClass.length()));
    }
    //debug("makeClassURL returns: url: %s", url); //TODO
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
    if (null == option || option.isBlank()) {
      terminate(999, "Commons: asFolder(): not possible for %s", option);
    }
    File folder = new File(option);
    if (!folder.isAbsolute()) {
      folder = new File(Commons.getWorkDir(), option);
    }
    if (!folder.isDirectory()) {
      if (folder.exists()) {
        return folder.getParentFile();
      }
      folder.mkdirs();
      if (!folder.exists()) {
        terminate(999, "Commons: asFolder(): not possible for %s", folder);
      }
    }
    return folder;
  }
  //</editor-fold>

  //<editor-fold desc="15 file handling">
  public static List<String> getFileList(Object givenMain, Object... options) {
    return doGetFileList(givenMain, null, null, options);
  }

  public static List<String> getFileList(Object givenMain, String givenSubFolder, Object... options) {
    return doGetFileList(givenMain, null, null, options);
  }

  public static List<String> getFileList(String givenFolder, Class classReference, Object... options) {
    return doGetFileList(givenFolder, null, classReference, options);
  }

  private static List<String> doGetFileList(Object givenMainOrFolder, String givenSubfolder, Class classReference, Object... options) {
    Commons.trace("ENTER: Main(%s) Sub(%s) Class(%s)", givenMainOrFolder, givenSubfolder, classReference);
    List<String> fileList = new ArrayList<>();
    String givenFolder = "/";
    int MAX_LEVEL = 5;
    boolean listFolders = false;
    for (Object option : options) {
      if (option instanceof Integer) {
        MAX_LEVEL = (Integer) option;
        if (MAX_LEVEL < 1) {
          MAX_LEVEL = Integer.MAX_VALUE;
        }
      } else if (option instanceof Boolean) {
        listFolders = (Boolean) option;
      }
    }
    if (givenMainOrFolder instanceof Class) {
      classReference = (Class) givenMainOrFolder;
    } else if (givenMainOrFolder instanceof File) {
      givenFolder = ((File) givenMainOrFolder).getPath();
    } else {
      givenFolder = givenMainOrFolder.toString();
    }
    //check for sikulixcontent file
    if (classReference != null) {
      String resFile = new File(givenFolder, "sikulixcontent").getPath();
      String content = copyResourceToString(resFile, classReference);
      if (content != null && !content.isEmpty()) {
        String[] names = content.split("\n");
        for (String name : names) {
          if (name.equals("sikulixcontent")) {
            continue;
          }
          fileList.add(name.trim());
        }
        return fileList;
      }
    }

    // get file list
    String path = null;
    boolean isJar = false;
    boolean isFile = false;
    if (classReference != null) {
      while (true) {
        if (classReference.getResource(givenFolder) == null) {
          if (!givenFolder.startsWith("/")) {
            givenFolder = "/" + givenFolder;
            continue;
          }
          Commons.trace("ERROR(not found) Folder(%s) Class(%s)", givenFolder, classReference);
          return fileList;
        }
        break;
      }
      try {
        path = classReference.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        if (path.endsWith(".jar")) {
          isJar = true;
        } else {
          isFile = true;
        }
      } catch (URISyntaxException ignored) {
      }
    } else if (givenFolder != null && !givenFolder.isEmpty()) {
      if (givenFolder.endsWith(".jar") || givenFolder.contains(".jar!/")) {
        String[] parts = givenFolder.split("!/");
        givenFolder = "/";
        path = parts[0];
        if (parts.length > 1) {
          givenFolder += parts[1];
        }
        isJar = true;
      } else {
        path = givenFolder;
        givenFolder = "";
        isFile = true; //TODO zip, http
      }
    }
    Commons.trace("BEFORE: Path(%s) Folder(%s)", path, givenFolder);
    if (path != null) {
      FileSystem fs = null;
      String fspath = null;
      try {
        if (isJar) {
          URI uri = URI.create("jar:file:" + path);
          fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
          fspath = givenFolder;
        } else if (isFile) {
          fs = FileSystems.getFileSystem(URI.create("file:/"));
          fspath = new File(path, givenFolder).getPath();
        }
        Stream<Path> pathStream;
        if (listFolders) {
          pathStream = Files.walk(fs.getPath(fspath), MAX_LEVEL).filter(Files::isDirectory);
        } else {
          pathStream = Files.walk(fs.getPath(fspath), MAX_LEVEL).filter(Files::isRegularFile);
        }
        fileList = pathStream.map(Path::toString).collect(Collectors.toList());
        if (isJar) {
          fs.close();
        }
      } catch (IOException e) {
      }
    }
    return fileList;
  }

  public static String copyResourceToString(String res, Class classReference) {
    InputStream stream = classReference.getResourceAsStream(res.replace("\\", "/"));
    String content = "";
    try {
      if (stream != null) {
        content = new String(copy(stream));
        stream.close();
      } else {
        trace("ERROR(not found) File(%s) Class(%s)", res, classReference);
        return null;
      }
    } catch (Exception ex) {
      trace("ERROR File(%s) Class(%s): %s", res, classReference, ex.getMessage());
    }
    return content;
  }

  public static boolean copyResourceToFile(String res, Class classReference, File file) {
    InputStream stream = classReference.getResourceAsStream(res.replace("\\", "/"));
    if (stream == null) {
      return false;
    }
    OutputStream out;
    try {
      try {
        file.getParentFile().mkdirs();
        out = new FileOutputStream(file);
      } catch (FileNotFoundException e) {
        return false;
      }
      copy(stream, out);
      stream.close();
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  public static void copyResourceFiles(String from, String toWhere, Class clazz) {
    List<String> libFiles = Commons.getFileList(from, clazz);
    if (libFiles.size() > 0) {
      String libFolderName = "/" + from + "/";
      String file0 = libFiles.get(0);
      int prefix = file0.indexOf(libFolderName);
      for (String file : libFiles) {
        String fromFile = file.substring(prefix);
        String toFile = new File(toWhere, fromFile.substring(1)).getPath();
        Commons.copyResourceToFile(fromFile, clazz, new File(toFile));
      }
    }

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
    if (null == option || option.isBlank()) {
      terminate(999, "Commons: asFile(): not possible for %s", option);
    }
    File file = new File(option);
    if (!file.isAbsolute()) {
      file = new File(Commons.getWorkDir(), option);
    }
    if (file.isDirectory()) {
      terminate(999, "Commons: asFile(): is directory %s", file);
    }
    if (!file.getParentFile().isDirectory()) {
      file.getParentFile().mkdirs();
      if (!file.getParentFile().isDirectory()) {
        terminate(999, "Commons: asFile(): not possible for %s", option);
      }
    }
    return file;
  }

  public static File asFile(Object path, String option) {
    File folder = null;
    if (path instanceof String) {
      folder = asFolder((String) path);
    } else if (path instanceof File) {
      folder = asFolder(((File) path).getPath());
    } else if (path instanceof URL) {
      URL url = (URL) path;
      String urlPath = url.getPath();
      if (url.getProtocol().equals("jar") || url.getProtocol().equals("file")) {
        urlPath = urlPath.substring(5);
      }
      File urlFile = new File(urlPath);
      if (url.getProtocol().equals("jar")) {
        String[] split = urlPath.split("!");
        urlFile = new File(split[0], split[1].substring(1));
      }
      return urlFile;
    } else {
      terminate(999, "Commons: asFile(): path invalid %s", path);
    }
    return asFile(new File(folder, option).getPath());
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

  private static final String libOpenCV = Core.NATIVE_LIBRARY_NAME;

  public static boolean loadOpenCV() {
    return loadLibrary(libOpenCV);
  }

  public static String getLibFilename(String aFile) {
    if (runningWindows()) {
      aFile += ".dll";
    } else if (runningMac()) {
      aFile = "lib" + aFile + ".dylib";
    } else {
      aFile = "lib" + aFile + ".so";
    }
    return aFile;
  }

  private static List<String> libsLoaded = new ArrayList<>();
  private static boolean areLibsExported = false;

  public static boolean loadLibrary(String libName) {
    debug("loadLibrary: trying: %s", libName);
    if (libsLoaded.contains(libName)) {
      return true;
    }
    String libFileName = getLibFilename(libName);
    String userLib = "";
    //try from env::SIKULIX_LIBS
    File fLib = loadLib(getFromExternalLibsFolder(libFileName));
    if (fLib != null) {
      userLib = userLibsPath + ": ";
    } else {
      //try exported libs
      if (!areLibsExported) {
        libsExport();
        if (!areLibsExported) {
          throw new SikuliXception("loadLib: deferred exporting of libs did not work");
        }
      }
      fLib = loadLib(new File(getLibsFolder(), libFileName));
    }
    if (fLib == null) {
      //try from system library folders
      fLib = loadLib(new File(libFileName));
    }
    if (null == fLib) {
      terminate(999, "FATAL: loadLibrary: %s not in any libs folder or not useable", libFileName);
    }
    libsLoaded.add(libName);
    debug("loadLibrary: success: %s%s", userLib, fLib);
    return true;
  }

  private static File loadLib(File fLib) {
    if (fLib == null) {
      return null;
    }
    try {
      if (fLib.isAbsolute()) {
        if (fLib.exists()) {
          System.load(fLib.getAbsolutePath());
        } else {
          return null;
        }
      } else {
        System.loadLibrary("" + fLib);
      }
    } catch (Exception e) {
      error("loadLibrary: not useable: %s (%s)", fLib.getName(), e.getMessage());
      return null;
    }
    return fLib;
  }

  private static void libsExport() {
    String OPENCV_JAVA = "opencv_java";
    String fpJarLibs = getJarLibsPath();
    File fLibsFolder = getLibsFolder();
    if (fLibsFolder.exists()) {
      if (!hasVersionFile(fLibsFolder)) {
        FileManager.deleteFileOrFolder(fLibsFolder);
        debug("libsFolder: has wrong content: %s", fLibsFolder);
      }
    }
    if (!fLibsFolder.exists()) {
      fLibsFolder.mkdirs();
      if (!fLibsFolder.exists()) {
        throw new SikuliXception("libsFolder: folder not available: " + fLibsFolder);
      }
      makeVersionFile(fLibsFolder); //TODO
      debug("libsFolder: created %s (%s)", fLibsFolder, getSXVersionLong());
      didExport = true;
    }
    if (!shouldExport()) { //TODO for what needed?
      areLibsExported = true;
      return;
    }
    List<String> nativesList = Commons.getFileList(fpJarLibs, COMMONS_CLASS);
    for (String aFile : nativesList) {
      String copyMsg = "";

      String inFile;
      if (aFile.startsWith("//") || aFile.startsWith("#")) {
        continue;
      } else if (aFile.startsWith("/")) {
        String[] parts = aFile.split("@");
        if (parts.length > 1) {
          inFile = parts[0];
          try {
            COMMONS_CLASS = Class.forName(parts[1]);
          } catch (ClassNotFoundException e) {
            copyMsg = String.format(": failed: %s", e.getMessage());
            inFile = null;
          }
        } else {
          inFile = aFile;
        }
        aFile = new File(inFile).getName();
      } else {
        inFile = new File(fpJarLibs, aFile).getPath();
      }
      if (inFile != null) {
        if (OPENCV_JAVA.equals(aFile)) {
          inFile = inFile.replace(OPENCV_JAVA, getLibFilename(libOpenCV));
          aFile = new File(inFile).getName();
        }
        try (FileOutputStream outFile = new FileOutputStream(new File(fLibsFolder, aFile));
             InputStream inStream = COMMONS_CLASS.getResourceAsStream(inFile.replace("\\", "/"))) {
          RunTime.copy(inStream, outFile);
        } catch (Exception ex) {
          copyMsg = String.format(": failed: %s", ex.getMessage());
        }
        copyMsg = String.format("libsExport: %s%s", aFile, copyMsg);
      }
      if (copyMsg.contains("failed")) {
        FileManager.deleteFileOrFolder(fLibsFolder);
        error(copyMsg);
        break;
      } else {
        debug(copyMsg);
      }
    }
    areLibsExported = true;
  }

  private static boolean didExport = false;

  public static boolean shouldExport() {
    return didExport;
  }

  //</editor-fold>

  //<editor-fold desc="30 Options handling SX_ARG_">
  public final static String SXPREFS_OPT = "SX_PREFS_";
  public final static String SETTINGS_OPT = "Settings.";
  public final static String SXARGS_OPT = "SX_ARG_";

  public static void initGlobalOptions() {
    if (GLOBAL_OPTIONS == null) {
      GLOBAL_OPTIONS = new Options();
      GLOBAL_OPTIONS.set("SX_ARG_JAR", getMainClassLocation().getAbsolutePath());
      if (STARTUPFILE != null) {
        GLOBAL_OPTIONS.set("SX_ARG_STARTUP", new File(STARTUPFILE));
      }
      // *************** add commandline args
      String val = "";
      if (RUNNINGIDE && cmdLine != null) {
        for (CommandArgsEnum arg : CommandArgsEnum.values()) {
          if (cmdLine.hasOption(arg.shortname())) {
            if (null != arg.hasArgs()) {
              if (arg.hasArgs()) {
                String[] args = cmdLine.getOptionValues(arg.shortname());
                if (args.length > 1) {
                  for (int n = 0; n < args.length; n++) {
                    val += args[n] + File.pathSeparator;
                  }
                  val = val.substring(0, val.length() - 1);
                } else {
                  val = args[0];
                }
              } else {
                val = cmdLine.getOptionValue(arg.shortname());
                val = val == null ? "" : val;
              }
            }
            GLOBAL_OPTIONS.set("SX_ARG_" + arg, val);
            val = "";
          }
        }
        val = "";
        for (String arg : cmdArgs.getUserArgs()) {
          val += arg + " ";
        }
        if (!val.isEmpty()) {
          GLOBAL_OPTIONS.set("SX_ARG_USER", val.trim());
        }
      }

      // ************* add Settings defaults
      for (String name : Settings._FIELDS_LIST.keySet()) {
        Object value = Settings.get(name);
        if (value == null) {
          value = "null";
        }
        GLOBAL_OPTIONS.add("Settings." + name, value);
      }

      // add IDE Preferences defaults
      if (RUNNINGIDE && isSandBox()) {
        PreferencesUser prefsIDE = PreferencesUser.get();
        prefsIDE.setDefaults();
      }

      // *************** check for existing optionsfile and load it
      File globalOptionsFile = null;
      if (isSandBox()) {
        globalOptionsFile = getOptionFile();
      } else {
        globalOptionsFile = getOptionFileDefault();
      }
      if (globalOptionsFile != null && globalOptionsFile.exists()) {
        GLOBAL_OPTIONS.load(globalOptionsFile, new Options.Filter() {
          @Override
          public boolean accept(String key) {
            if (key.startsWith(SXARGS_OPT)) {
              return false;
            }
            return true;
          }
        });
      }

      // ***************** add options from a given startup config file
      if (STARTUPLINES != null) {
        for (String line : STARTUPLINES) {
          if (line.contains("=")) {
            if (line.startsWith("=")) {
              continue;
            }
            String[] parts = line.split("=");
            if (parts.length > 0) {
              String key = parts[0].strip();
              val = "";
              if (parts.length > 1) {
                val = parts[1].strip();
              }
              GLOBAL_OPTIONS.set(key, val);
            }
          } else {
            GLOBAL_OPTIONS.set(line.strip(), "");
          }
        }
      }
    }
  }

  public static Map<String, String> getOptions() {
    if (GLOBAL_OPTIONS == null) {
      initGlobalOptions();
    }
    return GLOBAL_OPTIONS.getAll();
  }

  public static Options getGlobalOptions() {
    if (GLOBAL_OPTIONS == null) {
      terminate(999, "Commons::globalOptions: early access - not initialized");
    }
    return GLOBAL_OPTIONS;
  }

  static void saveGlobalOptions() {
    File optionFile;
    if (isSandBox()) {
      optionFile = getOptionFile();
      if (null == optionFile) {
        optionFile = new File(APP_DATA_SANDBOX, getOptionFileName());
      }
    } else {
      optionFile = getOptionFileDefault();
      if (null == optionFile || !optionFile.exists()) {
        optionFile = new File(getAppDataStore(), getOptionFileNameBackup());
      }
    }
    String optionsAsLines = getOptionsAsLines();
    FileManager.writeStringToFile(optionsAsLines, optionFile);
  }

  public static File getOptionFile() {
    return getOptionFile(getOptionFileName());
  }

  static File getOptionFileDefault() {
    return getOptionFile(new File(getAppDataStore(), getOptionFileName()).getAbsolutePath());
  }

  static File getOptionFile(String fpOptions) {
    File fOptions = new File(fpOptions);
    if (!fOptions.isAbsolute()) {
      for (File aFile : new File[]{getAppDataPath(), getWorkDir(), getUserHome()}) {
        fOptions = new File(aFile, fpOptions);
        if (fOptions.exists()) {
          break;
        } else {
          fOptions = null;
        }
      }
    }
    return fOptions;
  }

  static String getOptionFileName() {
    String fileName = "SikulixOptions.txt";
    return fileName;
  }

  static String getOptionFileNameBackup() {
    String fileName = "SikulixOptionsBackup.txt";
    return fileName;
  }

  public static void show() {
    String runningAs = "running as jar";
    if (Commons.isRunningPackage()) {
      runningAs = "running from package";
    }
    info("***** show environment for %s (%s)", Commons.getSXVersion(), Commons.getSxBuildStamp());
    if (STARTUPFILE != null) {
      info("startupfile: %s", STARTUPFILE);
    }
    info("%s: %s (%s)", runningAs, getMainClassLocation(), getStartClass().getCanonicalName());
    info("running on: %s", Commons.getOSInfo());
    info("running Java: %s", Commons.getJavaInfo());
    info("java.io.tmpdir: %s", Commons.getTempFolder());
    info("app data folder: %s", Commons.getAppDataPath());
    info("work dir: %s", Commons.getWorkDir());
    info("user.home: %s", Commons.getUserHome());
    Commons.showOptions("SX_", "_PREFS_");
    info("***** show environment end");
  }

  public static void showOptions() {
    showOptions("");
  }

  public static void showOptions(String prefix) {
    doShowOptions(prefix, "");
  }

  public static void showOptions(String prefix, String... except) {
    doShowOptions(prefix, except);
  }

  static void doShowOptions(String prefix, String... except) {
    info("%s", getOptionsAsLines(prefix, except));
  }

  static String getOptionsAsLines() {
    return getOptionsAsLines("");
  }

  static String getOptionsAsLines(String prefix, String... except) {
    if (except.length == 1 && except[0].isEmpty()) {
      except = null;
    }
    TreeMap<String, String> sortedOptions = new TreeMap<>();
    sortedOptions.putAll(getOptions());
    int len = 0;
    List<String> keys = new ArrayList<>();
    for (String key : sortedOptions.keySet()) {
      if (!key.startsWith(prefix)) {
        continue;
      }
      if (except != null && except.length > 0) {
        for (String exKey : except) {
          if (key.contains(exKey)) {
            key = null;
            break;
          }
        }
      }
      if (key == null) {
        continue;
      }
      keys.add(key);
      if (key.length() < len) {
        continue;
      }
      len = key.length();
    }
    String out = "";
    for (String key : keys) {
      String val = sortedOptions.get(key);
      if (val.isEmpty()) {
        out += key + System.lineSeparator();
      } else {
        out += String.format("%-" + len + "s" + " = %s", key, val) + System.lineSeparator();
      }
    }
    return out;
  }

  public static boolean hasOption(String option) {
    if (GLOBAL_OPTIONS == null) {
      initGlobalOptions();
    }
    return GLOBAL_OPTIONS.has(option);
  }

  public static String getOption(String option) {
    if (GLOBAL_OPTIONS == null) {
      initGlobalOptions();
    }
    return GLOBAL_OPTIONS.get(option, "");
  }

  public static String getOption(String option, Object deflt) {
    if (GLOBAL_OPTIONS == null) {
      initGlobalOptions();
    }
    if (deflt instanceof String) {
      return GLOBAL_OPTIONS.get(option, (String) deflt);
    } else {
      return GLOBAL_OPTIONS.get(option, deflt.toString());

    }
  }

  public static void setOption(String option, Object val) {
    if (GLOBAL_OPTIONS == null) {
      initGlobalOptions();
    }
    GLOBAL_OPTIONS.set(option, val);
  }

  public static int asInt(String val) {
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public static Long asLong(String val) {
    try {
      return Long.parseLong(val);
    } catch (NumberFormatException e) {
      return -1L;
    }
  }

  public static double asDouble(String val) {
    try {
      return Double.parseDouble(val);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public static float asFloat(String val) {
    try {
      return Float.parseFloat(val);
    } catch (NumberFormatException e) {
      return -1F;
    }
  }

  public static boolean asBool(String val) {
    if (!val.isEmpty()) {
      val = val.toUpperCase().substring(0, 1);
      if (val.equals("0") || val.equals("F") || val.equals("N")) return false;
      if (val.equals("1") || val.equals("T") || val.equals("Y")) return true;
    }
    return false;
  }

  public static String[] asArray(String val) {
    return val.split(File.pathSeparator);
  }
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
    } else if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) { //TODO || bImg.getType() == BufferedImage.TYPE_CUSTOM) {
      List<Mat> mats = getMatList(bImg);
      Size size = mats.get(0).size();
      if (!asBGR) {
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
        classSup = Class.forName("org.sikuli.script.runnerSupport.JythonSupport");
      } catch (ClassNotFoundException e) {
        terminate(999, "Commons: JythonSupport: %s", e.getMessage());
      }
    } else if (reference instanceof String && ((String) reference).contains("org.jruby")) {
      try {
        classSup = Class.forName("org.sikuli.script.runnerSupport.JRubySupport");
      } catch (ClassNotFoundException e) {
        terminate(999, "Commons: JRubySupport: %s", e.getMessage());
      }
    } else {
      terminate(999, "Commons: ScriptingSupport: not supported: %s", reference);
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
        method = classSup.getMethod(function, Object[].class);
        returnSup = method.invoke(instanceSup, new Object[]{args});
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
      terminate(999, "Commons: runScriptingSupportFunction(%s, %s, %s): %s",
          instanceSup, method, args, error);
    }
    return returnSup;
  }
  //</editor-fold>

  //<editor-fold desc="99 stuff">
  public static void pause(double time) {
    try {
      Thread.sleep((int) (time * 1000));
    } catch (InterruptedException ex) {
    }
  }

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

  public static String[] parmStringToArray(String line) {
    String separator = "\"";
    ArrayList<String> argsx = new ArrayList<String>();
    StringTokenizer toks;
    String tok;
//    if (Settings.isWindows()) {
//      line = line.replaceAll("\\\\ ", "%20;");
//    }
    toks = new StringTokenizer(line);
    while (toks.hasMoreTokens()) {
      tok = toks.nextToken(" ");
      if (tok.length() == 0) {
        continue;
      }
      if (separator.equals(tok)) {
        continue;
      }
      if (tok.startsWith(separator)) {
        if (tok.endsWith(separator)) {
          tok = tok.substring(1, tok.length() - 1);
        } else {
          tok = tok.substring(1);
          tok += toks.nextToken(separator);
        }
      }
      argsx.add(tok); //.replaceAll("%20;", " "));
    }
    return argsx.toArray(new String[0]);
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
