/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.apache.commons.cli.CommandLine;
import org.opencv.core.Core;
import org.sikuli.android.ADBScreen;
import org.sikuli.basics.*;
import org.sikuli.natives.WinUtil;
import org.sikuli.script.*;
import org.sikuli.script.runnerSupport.JythonSupport;
import org.sikuli.script.runners.ProcessRunner;
import org.sikuli.script.support.IScriptRunner.EffectiveRunner;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;
import org.sikuli.util.Highlight;
import org.sikuli.vnc.VNCScreen;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.security.CodeSource;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * INTERNAL USE --- NOT official API<br>
 * not as is in version 2
 * <p>
 * Intended to concentrate all, that is needed at startup of sikulix or sikulixapi and may be at runtime by SikuliX or
 * any caller
 */
public class RunTime {

  private static RunTime runTime = null;

  private static final String osNameShort = System.getProperty("os.name").substring(0, 1).toLowerCase();

  public static boolean isIDE() {
    return startAsIDE;
  }

  private static boolean startAsIDE = true;

  //<editor-fold desc="01 startup">
  public static void startAPI(String appPath) {
    File path = getAppPath(appPath);
    fTempPath = new File(path, "Temp");
    startAPI();
  }

  public static void startAPI() {
    RunTime.get(Type.API);
  }

  public static void start(RunTime.Type type, String[] args)  {
    if (Type.API.equals(type)) {
      startAsIDE = false;
      if (args.length == 1 && "buildDate".equals(args[0])) {
        RunTime runTime = RunTime.get();
        System.out.println(runTime.SXBuild);
        System.exit(0);
      }

//TODO place to test something in the API context
      if (args.length == 1 && "test".equals(args[0])) {
/*
        startAPI("XXX");
        URL resource = SikulixImages.class.getResource("/provided/images/house1.png");
        URL resource = org.sikuli.script.Image.class.getResource("/Settings/test.png");
        Image image = Image.create(resource);
        Screen scr = new Screen();
        boolean ok = scr.has(image);
        RunTime.startLog(1, "test: %s", image);
*/
        System.exit(0);
      }
    }

    List<String> finalArgs = evalArgsStart(args);

    File runningJar = getRunningJar(type);
    RunTime.startLog(1, "Running: %s", runningJar);

    File fAppData = getAppPath();
    RunTime.startLog(1, "AppData: %s", fAppData);

    String classPath = ExtensionManager.makeClassPath(runningJar);
    if (runningJar.getName().endsWith(".jar")) {
      FileManager.writeStringToFile(runningJar.getAbsolutePath(),
          new File(RunTime.getAppPath(),"SikulixStore/lastUsedJar.txt"));
    } else {
      return;
    }

    List<String> cmd = new ArrayList<>();
    System.getProperty("java.home");
    if (get().runningWindows) {
      cmd.add(System.getProperty("java.home") + "\\bin\\java.exe");
    } else {
      cmd.add(System.getProperty("java.home") + "/bin/java");
    }
    if (get().isJava9()) {
      /*
      Suppress Java 9+ warnings
      --add-opens
      java.desktop/javax.swing.plaf.basic=ALL-UNNAMED
      --add-opens
      java.base/sun.nio.ch=ALL-UNNAMED
      --add-opens
      java.base/java.io=ALL-UNNAMED
      -Dnashorn.args=--no-deprecation-warning
      */
      cmd.add("--add-opens");
      cmd.add("java.desktop/javax.swing.plaf.basic=ALL-UNNAMED");
      cmd.add("--add-opens");
      cmd.add("java.base/sun.nio.ch=ALL-UNNAMED");
      cmd.add("--add-opens");
      cmd.add("java.base/java.io=ALL-UNNAMED");
      cmd.add("-Dnashorn.args=--no-deprecation-warning");
    }

    cmd.add("-Dfile.encoding=UTF-8");
    if (startAsIDE) {
      cmd.add("-Dsikuli.IDE_should_run");
    } else {
      cmd.add("-Dsikuli.API_should_run");
    }
    if (!classPath.isEmpty()) {
      cmd.add("-cp");
      cmd.add(classPath);
    }
    if (startAsIDE) {
      cmd.add("org.sikuli.ide.SikulixIDE");
    } else {
      cmd.add("org.sikuli.script.support.SikulixAPI");
    }
    cmd.addAll(finalArgs);

    RunTime.startLog(3, "*********************** leaving start");
    //TODO detach IDE: for what does it make sense?
/*
    if (shouldDetach()) {
      ProcessRunner.detach(cmd);
      System.exit(0);
    } else {
      int exitCode = ProcessRunner.runBlocking(cmd);
      System.exit(exitCode);
    }
*/
    int exitCode = ProcessRunner.runBlocking(cmd);
    System.exit(exitCode);
  }

  private static File getRunningJar(Type type) {
    File jarFile = null;
    String jarName = "notKnown";
    CodeSource codeSrc = RunTime.class.getProtectionDomain().getCodeSource();
    if (Type.IDE.equals(type)) {
      try {
        Class cIDE = Class.forName("org.sikuli.ide.SikulixIDE");
        codeSrc = cIDE.getProtectionDomain().getCodeSource();
      } catch (ClassNotFoundException e) {
        startLog(-1, "IDE startup: not possible for: %s", e.getMessage());
        System.exit(1);
      }
    }
    if (codeSrc != null && codeSrc.getLocation() != null) {
      try {
        jarName = codeSrc.getLocation().getPath();
        jarName = URLDecoder.decode(jarName, "utf8");
      } catch (UnsupportedEncodingException e) {
        startLog(-1, "URLDecoder: not possible: %s", jarName);
        System.exit(1);
      }
      jarFile = new File(jarName);
    }
    return jarFile;
  }

  public static void afterStart(RunTime.Type type, String[] args) {
    String startType = "IDE";
    if (Type.IDE.equals(type)) {
      if (null == System.getProperty("sikuli.IDE_should_run")) {
        System.out.println("[ERROR] org.sikuli.ide.SikulixIDE: unauthorized use. Use: org.sikuli.ide.Sikulix");
        System.exit(1);
      }
    } else {
      if (null == System.getProperty("sikuli.API_should_run")) {
        System.out.println("[ERROR] org.sikuli.script.SikulixAPI: unauthorized use. Use: org.sikuli.script.Sikulix");
        System.exit(1);
      }
      startType = "API";
    }

    evalArgsStart(args);
    Debug.log(3, "Sikulix: starting " + startType);
    evalArgs(args);
    ExtensionManager.readExtensions(true);

    if (isQuiet()) {
      Debug.quietOn();
    } else if (isVerbose()) {
      Debug.setWithTimeElapsed(RunTime.getElapsedStart());
      Debug.setGlobalDebug(3);
      Debug.globalTraceOn();
      Debug.setStartWithTrace();
    }

    if (!getLogFile().isEmpty()) {
      Debug.setLogFile(getLogFile());
    }

    if (!getUserLogFile().isEmpty()) {
      Debug.setUserLogFile(getUserLogFile());
    }

    if (runningScripts()) {
      HotkeyManager.getInstance().addHotkey("Abort", new HotkeyListener() {
        @Override
        public void hotkeyPressed(HotkeyEvent e) {
          if (RunTime.get().runningScripts()) {
            Runner.abortAll();
            terminate(254, "AbortKey was pressed: aborting all running scripts");
          }
        }
      });
      int exitCode = Runner.runScripts(RunTime.getRunScripts(), userArgs, new IScriptRunner.Options());
      if (exitCode > 255) {
        exitCode = 254;
      }
      terminate(exitCode, "");
    }

//TODO simplify, when SikulixServer is implemented
    if (shouldRunServer()) {
      Class cServer = null;
      try {
        cServer = Class.forName("org.sikuli.script.runners.ServerRunner");
        cServer.getMethod("run").invoke(null);
        terminate();
      } catch (ClassNotFoundException e) {
      } catch (NoSuchMethodException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      }
      try {
        cServer = Class.forName("org.sikuli.script.support.SikulixServer");
        if (!(Boolean) cServer.getMethod("run").invoke(null)) {
          terminate(1, "SikulixServer: terminated with errors");
        }
      } catch (ClassNotFoundException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      } catch (NoSuchMethodException e) {
      }
      terminate();
    }
  }

  public static File asFolder(String option) {
    if (null == option) {
      return null;
    }
    File folder = new File(option);
    if (!folder.isAbsolute()) {
      folder = new File(get().fWorkDir, option);
    }
    if (folder.isDirectory() && folder.exists()) {
      return folder;
    }
    return null;
  }

  public static File asFile(String option) {
    if (null == option) {
      return null;
    }
    if (null == asFolder(option)) {
      File file = new File(option);
      if (!file.isAbsolute()) {
        file = new File(get().fWorkDir, option);
      }
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  public static void evalArgs(String[] args) {

    CommandLine cmdLine;
    String cmdValue;

    CommandArgs cmdArgs = new CommandArgs();
    cmdLine = cmdArgs.getCommandLine(args);

    boolean cmdLineValid = true;
    if (cmdLine == null) {
      startLog(-1, "Did not find any valid option on command line!");
      cmdLineValid = false;
    } else {
      setArgs(cmdArgs.getUserArgs(), cmdArgs.getSXArgs());
    }

    if (cmdLineValid && cmdLine.hasOption("h")) {
      cmdArgs.printHelp();
      System.exit(0);
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.DEBUG.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      if (cmdValue != null) {
        Debug.setDebugLevel(cmdValue);
      }
    }

    if (cmdLineValid && cmdLine.hasOption("g")) {
      if (cmdLine.hasOption("s")) {
        serverGroups = cmdLine.getOptionValue("g");
        startLog(3, "groups (-g): %s", serverGroups);
      } else {
        startLog(-1, "groups (-g): currently only accepted with -s");
      }
    }

    if (cmdLineValid && cmdLine.hasOption("x")) {
      if (cmdLine.hasOption("s")) {
        serverExtra = cmdLine.getOptionValue("x");
        startLog(3, "extra (-x): %s", serverExtra);
      } else {
        startLog(-1, "extra (-x): currently only accepted with -s");
      }
    }

    if (cmdLineValid && cmdLine.hasOption("s")) {
      serverOptions = cmdLine.getOptionValues("s");
    }

    if (cmdLineValid && cmdLine.hasOption("m")) {
      setAllowMultiple();
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.LOGFILE.shortname())) {
      logFile = cmdLine.getOptionValue(CommandArgsEnum.LOGFILE.longname());
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.USERLOGFILE.shortname())) {
      userLogFile = cmdLine.getOptionValue(CommandArgsEnum.USERLOGFILE.longname());
    }

    if (cmdLineValid && cmdLine.hasOption("c")) {
      System.setProperty("sikuli.console", "false");
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.LOAD.shortname())) {
      loadScripts = cmdLine.getOptionValues(CommandArgsEnum.LOAD.longname());
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      runScripts = resolveRelativeFiles(cmdLine.getOptionValues(CommandArgsEnum.RUN.longname()));
    }
  }

  public static String[] resolveRelativeFiles(String[] givenScripts) {
    String[] runScripts = new String[givenScripts.length];
    String baseDir = get().fWorkDir.getPath();
    for (int i = 0; i < runScripts.length; i++) {
      String givenScript = givenScripts[i];
      String file = resolveRelativeFile(givenScript, baseDir);
      if (file == null) {
        file = resolveRelativeFile(givenScript + ".sikuli", baseDir);
        if (file == null) {
          runScripts[i] = "?" + givenScript;
          continue;
        }
      } else {
        if (i == 0 && file.endsWith(".sikuli")) {
          baseDir = new File(file).getParent();
        }
      }
      EffectiveRunner runnerAndFile = Runner.getEffectiveRunner(file);
      IScriptRunner runner = runnerAndFile.getRunner();
      String fileToRun = runnerAndFile.getScript();
      File possibleDir = null;
      if (null == fileToRun) {
        for (String ending : new String[]{"", ".sikuli"}) {
          possibleDir = new File(file + ending);
          if (possibleDir.exists()) {
            break;
          } else {
            possibleDir = null;
          }
        }
        if (null == possibleDir) {
          runScripts[i] = "?" + givenScript;
          continue;
        }
        baseDir = possibleDir.getAbsolutePath();
        runnerAndFile = Runner.getEffectiveRunner(baseDir);
        fileToRun = runnerAndFile.getScript();
        if (fileToRun == null) {
          fileToRun = "!" + baseDir;
        } else {
          fileToRun = baseDir;
        }
      }
      runScripts[i] = fileToRun;
    }
    return runScripts;
  }

  /**
   * a relative path is checked for existence in the current base folder,
   * working folder and user home folder in this sequence.
   *
   * @param scriptName
   * @return absolute file or null if not found
   */
  public static String resolveRelativeFile(String scriptName, String baseDir) {
    if (get().runningWindows && (scriptName.startsWith("\\") || scriptName.startsWith("/"))) {
      scriptName = new File(scriptName).getAbsolutePath();
      return scriptName;
    }
    File file = new File(scriptName);
    if (!file.isAbsolute()) {
      File inBaseDir = new File(baseDir, scriptName);
      if (inBaseDir.exists()) {
        file = inBaseDir;
      } else {
        File inWorkDir = new File(get().fWorkDir, scriptName);
        if (inWorkDir.exists()) {
          file = inWorkDir;
        } else {
          File inUserDir = new File(get().fUserDir, scriptName);
          if (inUserDir.exists()) {
            file = inUserDir;
          } else {
            return null;
          }
        }
      }
    }
    return file.getAbsolutePath();
  }

  private static List<String> evalArgsStart(String[] args) {
    List<String> finalArgs = new ArrayList<>();
    for (String arg : args) {
      if ("-v".equals(arg)) {
        setVerbose();
      } else if ("-q".equals(arg)) {
        setQuiet();
      } else if ("-r".equals(arg)) {
        shouldRunScript = true;
      } else if ("-s".equals(arg)) {
        asServer = true;
      }
      finalArgs.add(arg);
    }
    return finalArgs;
  }

  private static String[] userArgs = new String[0];
  private static String[] sxArgs = new String[0];

  private static void setArgs(String[] args, String[] sargs) {
    userArgs = args;
    sxArgs = sargs;
  }

  public static String[] getSXArgs() {
    return sxArgs;
  }

  public static void setUserArgs(String[] args) {
    userArgs = new String[args.length];
    int n = 0;
    for (String arg : args) {
      userArgs[n] = arg;
      n++;
    }
  }

  public static String[] getUserArgs() {
    return userArgs;
  }

  public static void printArgs() {
    String[] xargs = getSXArgs();
    if (xargs.length > 0) {
      startLog(1, "--- Sikuli parameters ---");
      for (int i = 0; i < xargs.length; i++) {
        startLog(1, "%d: %s", i + 1, xargs[i]);
      }
    }
    xargs = getUserArgs();
    if (xargs.length > 0) {
      startLog(1, "--- User parameters ---");
      for (int i = 0; i < xargs.length; i++) {
        startLog(1, "%d: %s", i + 1, xargs[i]);
      }
    }
  }

  public static long getElapsedStart() {
    return elapsedStart;
  }

  private static long elapsedStart = new Date().getTime();

  public static String getLogFile() {
    return logFile;
  }

  private static String logFile = "";

  public static String getUserLogFile() {
    return userLogFile;
  }

  private static String userLogFile = "";

  public static String[] getLoadScripts() {
    return loadScripts;
  }

  private static String[] loadScripts = new String[0];

  public static String[] getRunScripts() {
    return runScripts;
  }

  private static boolean shouldRunScript = false;
  private static String[] runScripts = new String[0];

  public static boolean runningScripts() {
    return shouldRunScript;
  }

  public static boolean shouldRunServer() {
    return asServer;
  }

  private static boolean asServer = false;

  public static String[] getServerOptions() {
    return serverOptions;
  }

  private static String[] serverOptions = null;

  public static String getServerGroups() {
    return serverGroups;
  }

  private static String serverGroups = null;

  public static String getServerExtra() {
    return serverExtra;
  }

  private static String serverExtra = null;

  public static void setAllowMultiple() {
    allowMultiple = true;
  }

  public static boolean isAllowMultiple() {
    return allowMultiple;
  }

  private static boolean allowMultiple = false;

  public static boolean shouldDetach() {
    return !runningScripts() && !shouldRunServer();
  }

  public static File getAppPath() {
    return getAppPath(null);
  }

  public static File getAppPath(String givenAppPath) {
    if (null != sxAppPath) {
      return sxAppPath;
    }
    File fUserDir;
    String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isEmpty() || !(fUserDir = new File(userHome)).exists()) {
      startLog(-1, "JavaSystemProperty::user.home not valid: %s", userHome);
      System.exit(-1);
    } else {
      if (null == givenAppPath) {
        if ("w".equals(osNameShort)) {
          String appPath = System.getenv("APPDATA");
          if (appPath != null && !appPath.isEmpty()) {
            sxAppPath = new File(new File(appPath), "Sikulix");
          }
        } else if ("m".equals(osNameShort)) {
          sxAppPath = new File(new File(fUserDir, "Library/Application Support"), "Sikulix");
        } else {
          sxAppPath = new File(fUserDir, ".Sikulix");
        }
        if (sxAppPath != null && !sxAppPath.exists()) {
          sxAppPath.mkdirs();
        }
      } else {
        sxAppPath = new File(givenAppPath);
        if (!sxAppPath.isAbsolute()) {
          sxAppPath = new File(fUserDir, givenAppPath);
          sxAppPath.mkdirs();
        }
      }
      if (sxAppPath == null || !sxAppPath.exists()) {
        startLog(-1, "Default APPDATA path not valid: %s (using user.home/SikulixAppData)", sxAppPath);
        sxAppPath = new File(fUserDir, "SikulixAppData");
      }
    }
    return sxAppPath;
  }

  private static File sxAppPath = null;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="02 logging">
  private int lvl = 3;
  private int minLvl = lvl;
  private static String preLogMessages = "";

  public static boolean isVerbose() {
    return verbose || Debug.getDebugLevel() > 2;
  }

  public static void setVerbose() {
    RunTime.verbose = true;
    Debug.setDebugLevel(3);
    Debug.setWithTimeElapsed(RunTime.getElapsedStart());
    Debug.setGlobalDebug(3);
    Debug.globalTraceOn();
    Debug.setStartWithTrace();
  }

  private static boolean verbose = false;

  public static boolean isQuiet() {
    return quiet;
  }

  public static void setQuiet() {
    RunTime.quiet = true;
  }

  private static boolean quiet = false;

  public static void startLog(int level, String msg, Object... args) {
    String typ = startAsIDE ? "IDE" : "API";
    String msgShow = String.format("startUp: %s: ", typ);
    if (!isVerbose()) {
      return;
    }
    if (level < 0) {
      msgShow = "[ERROR]" + msgShow + msg;
      System.out.println(String.format(msgShow, args));
      return;
    }
    if (isQuiet()) {
      return;
    }
    if (isVerbose()) {
      if (level > 0) {
        msgShow = "[DEBUG]" + msgShow + msg;
      } else {
        msgShow = "[INFO]" + msgShow + msg;
      }
      System.out.println(String.format(msgShow, args));
    }
  }

  public static String arrayToQuotedString(String[] args) {
    String ret = "";
    for (String s : args) {
      if (s.contains(" ")) {
        s = "\"" + s + "\"";
      }
      ret += s + " ";
    }
    return ret;
  }

  private void log(int level, String message, Object... args) {
    Debug.logx(level, "RunTime:" + message, args);
  }

  private void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="03 variables">
  public enum Type {
    IDE, API, INIT
  }

  private enum theSystem {
    WIN, MAC, LUX, FOO
  }

  public enum RunType {
    JAR, CLASSES, OTHER
  }

  public RunType runningAs = RunType.OTHER;

  public boolean runningIDE() {
    return Type.IDE.equals(runType);
  }

  private static Options sxOptions = null;

  private static boolean isTerminating = false;
  private static boolean hasDoneCleanUpTerminating = false;

  public static String appDataMsg = "";

  public static boolean testing = false;
  public static boolean testingWinApp = false;

  public Type runType = Type.INIT;

  public String sxBuild = "";
  public String sxBuildStamp = "";
  public String jreVersion = java.lang.System.getProperty("java.runtime.version");
  public Preferences optionsIDE = null;
  public ClassLoader classLoader = RunTime.class.getClassLoader();
  public String userName = "";

  private Class clsRef = RunTime.class;

  private List<URL> classPathActual = new ArrayList<>();
  private List<String> classPathList = new ArrayList<>();
  public static File fTempPath = null;
  public File fBaseTempPath = null;
  public String fpBaseTempPath = "";
  public File fLibsFolder = null;
  public String fpJarLibs = "/sikulixlibs/";
  public String fpSysLibs = null;
  boolean areLibsExported = false;
  private Map<String, Boolean> libsLoaded = new HashMap<String, Boolean>();
  public File fUserDir = null;
  public File fWorkDir = null;

  public int getLastScriptRunReturnCode() {
    return lastScriptRunReturnCode;
  }

  public void setLastScriptRunReturnCode(int lastScriptRunReturnCode) {
    this.lastScriptRunReturnCode = lastScriptRunReturnCode;
  }

  private int lastScriptRunReturnCode = 0;

  public File fAppPath = null;
  public File fSikulixAppPath = null;
  public File fSikulixExtensions = null;
  public String[] standardExtensions = new String[]{"selenium4sikulix"};
  public File fSikulixLib = null;
  public File fSikulixStore;
  public File fSikulixDownloadsGeneric = null;
  public File fSikulixDownloadsBuild = null;
  public File fSikulixSetup;

  public File fSxBase = null;
  public File fSxBaseJar = null;
  public File fSxProject = null;
  public File fSxProjectTestScriptsJS = null;
  public File fSxProjectTestScripts = null;
  public String fpContent = "sikulixcontent";

  public boolean runningJar = true;
  public boolean runningInProject = false;
  public boolean runningWindows = false;
  public boolean runningMac = false;
  public boolean runningLinux = false;
  //private theSystem runningOn = theSystem.FOO;
  private final String osNameSysProp = System.getProperty("os.name");
  private final String osVersionSysProp = System.getProperty("os.version");
  public String javaShow = "not-set";
  public int javaArch = 32;
  public String osArch = "??";
  public int javaVersion = 0;
  public File javahome = new File(System.getProperty("java.home"));
  public String osName = "NotKnown";
  public String sysName = "NotKnown";
  public String osVersion = "";
  private String appType = null;
  public String linuxDistro = "???LINUX???";

  public int SikuliVersionMajor;
  public int SikuliVersionMinor;
  public int SikuliVersionSub;
  public String SXVersion = "";
  public String SXVersionLong;
  public String SXVersionShort;
  public String SXBuild = "";
  public String SXBuildNumber = "";
  public String SXVersionIDE;
  public String SXVersionAPI;

  public String SXSystemVersion;
  public String SXJavaVersion;

  public String[] ServerList = {};

  public static final String libOpenCV = Core.NATIVE_LIBRARY_NAME;
  public final static String runCmdError = "*****error*****";
  public static String NL = "\n";
  public File fLibsProvided;
  public File fLibsLocal;
  public boolean useLibsProvided = false;
  private String lastResult = "";
  public boolean isJythonReady = false;

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="04 instance">
  private RunTime() {
  }

  public static synchronized RunTime get() {
    if (runTime == null) {
      return get(Type.API);
    }
    return runTime;
  }

  static final long started = new Date().getTime();
  static final long obsolete = started - 2 * 24 * 60 * 60 * 1000;

  static boolean isObsolete(long refTime) {
    if (refTime == 0) {
      return false;
    }
    return refTime < obsolete;
  }

  static boolean optTesting = false;

  public boolean isTesting() {
    return optTesting;
  }

  public static synchronized RunTime get(Type typ) {
    if (runTime != null) {
      return runTime;
    }
    runTime = new RunTime();

    //<editor-fold defaultstate="collapsed" desc="versions">
    if (Debug.getDebugLevel() > 3) {
      runTime.dumpSysProps();
    }
    String vJava = System.getProperty("java.specification.version");
    String vVM = System.getProperty("java.vm.version");
    String vClass = System.getProperty("java.class.version");

    runTime.osArch = System.getProperty("os.arch");
    String vSysArch = System.getProperty("sun.arch.data.model");
    if (vSysArch != null) {
      if (vSysArch.contains("64")) {
        runTime.javaArch = 64;
      } else {
        vSysArch = null;
      }
    }

    try {
      if (vJava.startsWith("1.")) {
        runTime.javaVersion = Integer.parseInt(vJava.substring(2, 3));
      } else {
        String[] parts = vJava.split("\\.");
        runTime.javaVersion = Integer.parseInt(parts[0]);
      }
      runTime.javaShow = String.format("java %d version %s vm %s class %s arch %s",
          runTime.javaVersion, vJava, vVM, vClass, vSysArch);
    } catch (Exception ex) {
    }

    if (runTime.javaVersion < 8) {
      throw new SikuliXception(String.format("fatal: " + "Java version must at least be 8 (%s)", runTime.javaShow));
    }

    if (null == vSysArch) {
      throw new SikuliXception(String.format("fatal: " + "Java arch must be 64 Bit (%s)", runTime.javaShow));
    }

    runTime.osVersion = runTime.osVersionSysProp;
    String os = runTime.osNameSysProp.toLowerCase();
    if (os.startsWith("windows")) {
      runTime.sysName = "windows";
      runTime.osName = "Windows";
      runTime.runningWindows = true;
      runTime.NL = "\r\n";
    } else if (os.startsWith("mac")) {
      runTime.sysName = "mac";
      runTime.osName = "Mac OSX";
      runTime.runningMac = true;
    } else if (os.startsWith("linux")) {
      runTime.sysName = "linux";
      runTime.osName = "Linux";
      runTime.runningLinux = true;
    } else {
      // Presume Unix -- pretend to be Linux
      runTime.sysName = os;
      runTime.osName = runTime.osNameSysProp;
      runTime.runningLinux = true;
      runTime.linuxDistro = runTime.osNameSysProp;
    }
    runTime.fpJarLibs += runTime.sysName + "/libs" + runTime.javaArch;
    runTime.fpSysLibs = runTime.fpJarLibs.substring(1);

    String aFolder = System.getProperty("user.home");
    if (aFolder == null || aFolder.isEmpty() || !(runTime.fUserDir = new File(aFolder)).exists()) {
      throw new SikuliXception(String.format("fatal: " + "JavaSystemProperty::user.home not valid"));
    }

    aFolder = System.getProperty("user.dir");
    if (aFolder == null || aFolder.isEmpty() || !(runTime.fWorkDir = new File(aFolder)).exists()) {
      throw new SikuliXception(String.format("fatal: " + "JavaSystemProperty::user.dir not valid"));
    }

    //TODO APPDATA
/*
    runTime.fSikulixAppPath = new File("SikulixAppDataNotAvailable");
    if (runTime.runningWindows) {
      appDataMsg = "init: Windows: %APPDATA% not valid (null or empty) or is not accessible: %s";
      String tmpdir = System.getenv("APPDATA");
      if (tmpdir != null && !tmpdir.isEmpty()) {
        runTime.fAppPath = new File(tmpdir);
        runTime.fSikulixAppPath = new File(runTime.fAppPath, "Sikulix");
      }
    } else if (runTime.runningMac) {
      appDataMsg = "init: Mac: SikulxAppData does not exist or is not accessible: %s";
      runTime.fAppPath = new File(runTime.fUserDir, "Library/Application Support");
      runTime.fSikulixAppPath = new File(runTime.fAppPath, "Sikulix");
    } else if (runTime.runningLinux) {
      runTime.fAppPath = runTime.fUserDir;
      runTime.fSikulixAppPath = new File(runTime.fAppPath, ".Sikulix");
      appDataMsg = "init: Linux: SikulxAppData does not exist or is not accessible: %s";
    }
*/
    runTime.fSikulixAppPath = getAppPath();
    runTime.fSikulixStore = new File(runTime.fSikulixAppPath, "SikulixStore");
    runTime.fSikulixStore.mkdirs();
    //</editor-fold>

    sxOptions = Options.init(runTime);
    optTesting = sxOptions.isOption("testing", false);
    if (optTesting) {
      Debug.info("Options: testing = on");
    }

    int optDebugLevel = optTesting ? Debug.getDebugLevel() : sxOptions.getOptionInteger("Debug.level", -1);
    if (optDebugLevel > Debug.getDebugLevel()) {
      Debug.info("Options: Debug.level = %d", optDebugLevel);
      Debug.on(optDebugLevel);
    }

    Settings.init(runTime); // force Settings initialization
    runTime.initSikulixOptions();

    //TODO addShutdownHook
    hasDoneCleanUpTerminating = false;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        runShutdownHook();
      }
    });

    runTime.init(typ);
    if (Type.IDE.equals(typ)) {
      runTime.initIDEbefore();
      runTime.initAPI();
      runTime.initIDEafter();
    } else {
      runTime.initAPI();
    }
    return runTime;
  }

//TODO reset() needed?
/*
  public static synchronized RunTime reset(Type typ) {
    if (runTime != null) {
      preLogMessages += "RunTime: resetting RunTime instance;";
      runTime = null;
    }
    return get(typ);
  }

  public static synchronized RunTime reset() {
    return reset(Type.API);
  }
*/
  //</editor-fold>

  public Rectangle getMonitor(int n) {
    if (Screen.isHeadless()) {
      return new Rectangle();
    }
    return Screen.getMonitor(n);
  }

  public Rectangle hasPoint(Point aPoint) {
    return Screen.hasPoint(aPoint);
  }


  //<editor-fold defaultstate="collapsed" desc="05 global init">
  File isRunning = null;
  FileOutputStream isRunningFile = null;
  String isRunningFilename = "s_i_k_u_l_i-ide-isrunning";

  private void init(Type typ) {
    if ("winapp".equals(sxOptions.getOption("testing"))) {
      log(lvl, "***** for testing: simulating WinApp");
      testingWinApp = true;
    }
    for (String line : preLogMessages.split(";")) {
      if (!line.isEmpty()) {
        log(lvl, line);
      }
    }
    log(4, "global init: entering as: %s", typ);

    sxBuild = SXBuild;
    sxBuildStamp = sxBuild.replace("_", "").replace("-", "").replace(":", "").substring(0, 12);

    if (System.getProperty("user.name") != null) {
      userName = System.getProperty("user.name");
    }
    if (userName.isEmpty()) {
      userName = "unknown";
    }

    if (fTempPath == null) {
      String tmpdir = System.getProperty("java.io.tmpdir");
      if (tmpdir != null && !tmpdir.isEmpty()) {
        fTempPath = new File(tmpdir);
      } else {
        throw new SikuliXception("init: java.io.tmpdir not valid (null or empty");
      }
    }
    fBaseTempPath = new File(fTempPath, String.format("Sikulix_%d", FileManager.getRandomInt()));
    fpBaseTempPath = fBaseTempPath.getAbsolutePath();
    fBaseTempPath.mkdirs();
    try {
      File tempTest = new File(fBaseTempPath, "tempTest.txt");
      FileManager.writeStringToFile("temp test", tempTest);
      boolean success = true;
      if (tempTest.exists()) {
        tempTest.delete();
        if (tempTest.exists()) {
          success = false;
        }
      } else {
        success = false;
      }
      if (!success) {
        throw new SikuliXception(String.format("init: temp folder not useable: %s", fTempPath));
      }
    } catch (Exception e) {
      throw new SikuliXception(String.format("init: temp folder not useable: %s", fTempPath));
    }
    log(3, "temp folder ok: %s", fpBaseTempPath);
    if (Type.IDE.equals(typ) && !runningScripts() && !isAllowMultiple()) {
      isRunning = new File(fTempPath, isRunningFilename);
      boolean shouldTerminate = false;
      try {
        isRunning.createNewFile();
        isRunningFile = new FileOutputStream(isRunning);
        if (null == isRunningFile.getChannel().tryLock()) {
          Class<?> classIDE = Class.forName("org.sikuli.ide.SikulixIDE");
          Method stopSplash = classIDE.getMethod("stopSplash", new Class[0]);
          stopSplash.invoke(null, new Object[0]);
          Sikulix.popError("Terminating: IDE already running");
          shouldTerminate = true;
        }
      } catch (Exception ex) {
        Sikulix.popError("Terminating on FatalError: cannot access IDE lock for/n" + isRunning);
        shouldTerminate = true;
      }
      if (shouldTerminate) {
        System.exit(1);
      }
    }

    for (String aFile : fTempPath.list()) {
      if ((aFile.startsWith("Sikulix") && (new File(aFile).isFile()))
          || (aFile.startsWith("jffi") && aFile.endsWith(".tmp"))) {
        FileManager.deleteFileOrFolder(new File(fTempPath, aFile));
      }
    }

    try {
      if (!fSikulixAppPath.exists()) {
        fSikulixAppPath.mkdirs();
      }
      if (!fSikulixAppPath.exists()) {
        throw new SikuliXception(String.format(appDataMsg, fSikulixAppPath));
      }
      fSikulixExtensions = new File(fSikulixAppPath, "Extensions");
      if (!fSikulixExtensions.exists()) {
        fSikulixExtensions.mkdir();
      }
      fSikulixDownloadsGeneric = new File(fSikulixAppPath, "SikulixDownloads");
      if (!fSikulixDownloadsGeneric.exists()) {
        fSikulixDownloadsGeneric.mkdir();
      }
      fSikulixLib = new File(fSikulixAppPath, "Lib");
      fSikulixSetup = new File(fSikulixAppPath, "SikulixSetup");
      fLibsProvided = new File(fSikulixAppPath, fpSysLibs);
      fLibsLocal = fLibsProvided.getParentFile().getParentFile();
    } catch (Exception ex) {
      throw new SikuliXception(String.format(appDataMsg + ex.toString(), fSikulixAppPath));
    }

    clsRef = RunTime.class;
    CodeSource codeSrc = clsRef.getProtectionDomain().getCodeSource();
    fSxBaseJar = null;
    URL urlCodeSrc = null;
    String urlCodeSrcProto = "not-set";
    if (codeSrc != null) {
      urlCodeSrc = codeSrc.getLocation();
      urlCodeSrcProto = urlCodeSrc.getProtocol();
      if (null != codeSrc) {
        fSxBaseJar = new File(codeSrc.getLocation().getPath());
        if (urlCodeSrcProto == "file") {
          runningAs = RunType.CLASSES;
          if (urlCodeSrc.getPath().endsWith(".jar")) {
            runningAs = RunType.JAR;
          }
        } else {
          runningAs = RunType.OTHER;
        }
      }
    }
    appType = "from a jar";
    if (fSxBaseJar != null) {
      String baseJarName = fSxBaseJar.getName();
      fSxBase = fSxBaseJar.getParentFile();
      log(4, "runningAs: %s (%s) in: %s", runningAs, baseJarName, fSxBase.getAbsolutePath());
      Debug.setWithTimeElapsed();
      if (baseJarName.contains("classes")) {
        runningJar = false;
        fSxProject = fSxBase.getParentFile().getParentFile();
        log(4, "not jar - supposing Maven project: %s", fSxProject);
        appType = "in Maven project from classes";
        runningInProject = true;
      } else if ("target".equals(fSxBase.getName())) {
        fSxProject = fSxBase.getParentFile().getParentFile();
        log(4, "folder target detected - supposing Maven project: %s", fSxProject);
        appType = "in Maven project from some jar";
        runningInProject = true;
      } else {
        //TODO what???
      }
    } else {
      dumpClassPath();
      throw new SikuliXception(String.format("no valid Java context (%s)", clsRef));
    }
    if (runningInProject) {
      fSxProjectTestScriptsJS = new File(fSxProject, "StuffContainer/testScripts/testJavaScript");
      fSxProjectTestScripts = new File(fSxProject, "StuffContainer/testScripts");
    }

//TODO RunTime: extensions / Jython
/*
    List<String> items = new ArrayList<String>();
    if (Type.API.equals(typ)) {
      String optJython = sxOptions.getOption("jython");
      if (!optJython.isEmpty()) {
        items.add(optJython);
      }
    }

    if (!Type.SETUP.equals(typ)) {
      String optClasspath = sxOptions.getOption("classpath");
      if (!optClasspath.isEmpty()) {
        items.addAll(Arrays.asList(optClasspath.split(System.getProperty("path.separator"))));
      }
      items.addAll(Arrays.asList(standardExtensions));
      if (items.size() > 0) {
        String[] fList = fSikulixExtensions.list();
        for (String item : items) {
          item = item.trim();
          if (new File(item).isAbsolute()) {
            addToClasspath(item, "RunTime: init from options " + typ);
          } else {
            for (String fpFile : fList) {
              File fFile = new File(fSikulixExtensions, fpFile);
              if (fFile.length() > 0) {
                if (fpFile.startsWith(item)) {
                  addToClasspath(fFile.getAbsolutePath(), "RunTime: init from options " + typ);
                  break;
                }
              } else {
                fFile.delete();
              }
            }
          }
        }
      }
    }
*/
    runType = typ;
    if (Debug.getDebugLevel() == minLvl) {
      show();
    }
    log(4, "global init: leaving");
  }
  //</editor-fold>

  //<editor-fold desc="99 cleanUp">
  public static void terminate() {
    terminate(0, "");
  }

  public static void terminate(int retval, String message, Object... args) {
    String outMsg = String.format(message, args);
    if (!outMsg.isEmpty()) {
      System.out.println("TERMINATING: " + outMsg);
    }
    if (retval < 999) {
      isTerminating = true;
      cleanUp();
      System.exit(retval);
    }
    throw new SikuliXception(String.format("fatal: " + outMsg));
  }

  public static void cleanUp() {
    if (hasDoneCleanUpTerminating) {
      return;
    }
    if (!isTerminating) {
      runTime.log(3, "***** running cleanUp *****");
      Highlight.closeAll();
      Settings.DefaultHighlightColor = "RED";
      Settings.DefaultHighlightTime = 2.0f;
      Settings.Highlight = false;
      Settings.setShowActions(false);
      FindFailed.reset();
      OCR.reset();
      Settings.OcrLanguage = Settings.OcrLanguageDefault;
      Settings.OcrDataPath = null;
    }

    try {
      VNCScreen.stopAll();
      ADBScreen.stop();
    } catch (Exception e) {
      Debug.info("Error while stopping VNCScreen: %s", e.getMessage());
    }

    Observing.cleanUp();
    HotkeyManager.reset(isTerminating);
    if (null != cleanupRobot) {
      cleanupRobot.keyUp();
    }
    Mouse.reset();
    //TODO cannot be done in shutdownhook: PreferencesUser.get().store();
    if (isTerminating) {
      hasDoneCleanUpTerminating = true;
    }
  }

  private static void runShutdownHook() {
    isTerminating = true;
    if (Debug.isStartWithTrace()) {
      Debug.on(3);
      Debug.globalTraceOn();
    }
    runTime.log(runTime.lvl, "***** final cleanup at System.exit() *****");
    cleanUp();

    if (runTime.isRunning != null) {
      try {
        runTime.isRunningFile.close();
      } catch (IOException ex) {
      }
      runTime.isRunning.delete();
    }

    for (File f : runTime.fTempPath.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.toLowerCase().contains("sikuli")) {
          if (name.contains("Sikulix_")) {
            if (isObsolete(new File(dir, name).lastModified()) || name.equals(runTime.fBaseTempPath.getName())) {
              return true;
            }
          } else {
            return true;
          }
        }
        return false;
      }
    })) {
      runTime.log(4, "cleanTemp: " + f.getName());
      FileManager.deleteFileOrFolder(f.getAbsolutePath());
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="13 Sikulix options handling">
  void initSikulixOptions() {
    Properties prop = new Properties();
    String svf = "sikulixversion.txt";
    try {
      InputStream is;
      is = RunTime.class.getClassLoader().getResourceAsStream("Settings/" + svf);
      if (is == null) {
        String msg = String.format("fatal: " + "initSikulixVersion: not found on classpath: %s", "Settings/" + svf);
        Debug.error(msg);
        throw new SikuliXception(msg);
      }
      prop.load(is);
      is.close();

      //    sikulixvproject=2.0.0  or 2.1.0-SNAPSHOT
      SXVersion = prop.getProperty("sikulixvproject");
      String[] version = SXVersion.replace("-SNAPSHOT", "").split("\\.");
      if (version.length != 3) {
        throw new SikuliXception(String.format("Settings: wrong version format: %s", SXVersion));
      }
      SikuliVersionMajor = Integer.decode(version[0]);
      SikuliVersionMinor = Integer.decode(version[1]);
      SikuliVersionSub = Integer.decode(version[2]);

      //    sikulixbuild=2019-10-17_09:58
      SXBuild = prop.getProperty("sikulixbuild");

      //    sikulixbuildnumber= BE-AWARE: only real in deployed artefacts (TravisCI)
      //    in development context undefined:
      SXBuildNumber = prop.getProperty("sikulixbuildnumber");
      if (SXBuildNumber.contains("TRAVIS_BUILD_NUMBER")) {
        SXBuildNumber = "";
      }

      // currently not used (see ExtensionManager)
      //    sikulixvjython=2.7.1
      //    sikulixvjruby=9.2.0.0
    } catch (Exception e) {
      String msg = String.format("Settings: load version file %s did not work: %s", svf, e.getMessage());
      Debug.error(msg);
      throw new SikuliXception(msg);
    }

    SXVersionIDE = "SikulixIDE-" + SXVersion;
    SXVersionAPI = "SikulixAPI " + SXVersion;
    if (SXBuildNumber.isEmpty()) {
      SXVersionLong = SXVersion + String.format("-%s", SXBuild);
    } else {
      SXVersionLong = SXVersion + String.format("-#%s-%s", SXBuildNumber, SXBuild);
    }
    SXVersionShort = SXVersion.replace("-SNAPSHOT", "");

    String osn = "UnKnown";
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("mac")) {
      osn = "Mac";
    } else if (os.startsWith("windows")) {
      osn = "Windows";
    } else if (os.startsWith("linux")) {
      osn = "Linux";
    }
    SXSystemVersion = osn + System.getProperty("os.version");
    SXJavaVersion = "Java" + javaVersion + "(" + javaArch + ")" + jreVersion;
  }

  public String getOption(String oName) {
    return sxOptions.getOption(oName);
  }

  public Options options() {
    return sxOptions;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="11 libs export">
  private boolean libsLoad(String libName) {
    log(lvl, "loadlib: trying %s", libName);
    String msg = "loadLib: %s";
    if (!areLibsExported) {
      libsExport();
    }
    if (!areLibsExported) {
      throw new SikuliXception("loadLib: deferred exporting of libs did not work");
    }
    File fLibsFolderUsed = fLibsFolder;
    if (runningWindows) {
      libName += ".dll";
    } else if (runningMac) {
      libName = "lib" + libName + ".dylib";
    } else if (runningLinux) {
      libName = "lib" + libName + ".so";
    }
    File fLib = new File(fLibsFolder, libName);
    int level = lvl;
    if (!runningLinux) {
      Boolean vLib = libsLoaded.get(libName);
      if (vLib == null || !fLib.exists()) {
        if (!fLib.exists()) {
          throw new SikuliXception(String.format("loadlib: %s not in any libs folder", libName));
        } else {
          vLib = false;
        }
      }
      if (vLib) {
        level++;
        msg += " already loaded";
        log(level, msg, libName);
        return true;
      }
    }
    try {
      if (runningLinux && libName.startsWith("libopen")) {
        libName = "opencv_java";
        System.loadLibrary(libName);
      } else {
        System.load(fLib.getAbsolutePath());
      }
    } catch (Exception e) {
      log(-1, "not usable: %s", e.getMessage());
      terminate(999, "problem with native library: " + libName);
    } catch (UnsatisfiedLinkError e) {
      log(-1, msg + " (failed) probably dependent libs missing:\n%s", libName, e.getMessage());
      String helpURL = "https://github.com/RaiMan/SikuliX1/wiki/macOS-Linux:-Support-Libraries-for-OpenCV-4";
      if (RunTime.isIDE()) {
        Debug.error("Save your work, correct the problem and restart the IDE!");
        try {
          Desktop.getDesktop().browse(new URI(helpURL));
        } catch (Exception ex) {
        }
      }
      Debug.error("see: " + helpURL);
      terminate(999, "problem with native library: " + libName);
    }
    libsLoaded.put(libName, true);
    log(level, msg + " (success)", libName);
    return true;
  }

  private boolean didExport = false;

  public boolean shouldExport() {
    return didExport;
  }

  private void libsExport() {
/*
    remove obsolete libs folders in Temp
*/
    String[] fpList = fTempPath.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.contains("SikulixLibs")) {
          return true;
        }
        return false;
      }
    });
    if (fpList.length > 0) {
      log(lvl, "libsExport: deleting obsolete libs folders in Temp");
      for (String entry : fpList) {
        if (entry.endsWith(sxBuildStamp)) {
          continue;
        }
        FileManager.deleteFileOrFolder(new File(fTempPath, entry));
      }
    }

/*
    remove libsfolder < 1.1.4
*/
    fpList = fSikulixAppPath.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.contains("SikulixLibs_")) {
          return true;
        }
        return false;
      }
    });
    if (fpList.length > 0) {
      log(lvl, "libsExport: deleting obsolete libs folders in AppPath");
      for (String entry : fpList) {
        FileManager.deleteFileOrFolder(new File(fSikulixAppPath, entry));
      }
    }

/*
    export
*/
    fLibsFolder = new File(fSikulixAppPath, "SikulixLibs");
    String libMsg = "folder exists:";
    if (fLibsFolder.exists()) {
      String[] resourceList = fLibsFolder.list(new FilenameFilter() {
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
      if (libVersion.isEmpty() || !libVersion.equals(getVersionShort()) ||
          libStamp.length() != sxBuildStamp.length() || 0 != libStamp.compareTo(sxBuildStamp)) {
        FileManager.deleteFileOrFolder(fLibsFolder);
        log(lvl, "libsExport: folder has wrong content: %s (%s - %s)", fLibsFolder, libVersion, libStamp);
      }
    }

    if (!fLibsFolder.exists()) {
      fLibsFolder.mkdirs();
      if (!fLibsFolder.exists()) {
        throw new SikuliXception("libsExport: folder not available: " + fLibsFolder.toString());
      }
      String libToken = String.format("%s_%s_MadeForSikuliX64%s.txt",
          getVersionShort(), sxBuildStamp, runningMac ? "M" : (runningWindows ? "W" : "L"));
      FileManager.writeStringToFile("*** Do not delete this file ***\n", new File(fLibsFolder, libToken));
      libMsg = "folder created:";
      List<String> nativesList = getResourceList(fpJarLibs);
      for (String aFile : nativesList) {
        String copyMsg = "";
        String inFile;
        Class classRef = clsRef;
        if (aFile.startsWith("/")) {
          String[] parts = aFile.split("@");
          if (parts.length > 1) {
            inFile = parts[0];
            try {
              classRef = Class.forName(parts[1]);
            } catch (ClassNotFoundException e) {
              copyMsg = String.format("libExport: %s: ", aFile);
            }
          } else {
            inFile = aFile;
          }
          aFile = new File(inFile).getName();
        } else {
          inFile = new File(fpJarLibs, aFile).getPath();
        }
        if (runningWindows) {
          inFile = inFile.replace("\\", "/");
        }
        try (FileOutputStream outFile = new FileOutputStream(new File(fLibsFolder, aFile));
             InputStream inStream = classRef.getResourceAsStream(inFile)) {
          copy(inStream, outFile);
          libsLoaded.put(aFile, false);
        } catch (Exception ex) {
          copyMsg += String.format("failed: %s", ex.getMessage());
        }
        copyMsg = String.format("libsExport: %s: %s", aFile, copyMsg);
        if (copyMsg.contains("failed")) {
          FileManager.deleteFileOrFolder(fLibsFolder);
          log(-1, copyMsg);
          break;
        } else {
          log(lvl + 1, copyMsg);
          didExport = true;
        }
      }
    }

    //TODO useLibsProvided
/*
      if (useLibsProvided) {
        log(lvl, "Linux: requested to use provided libs - copying");
        LinuxSupport.copyProvidedLibs(fLibsFolder);
      }
*/

    if (runningWindows) {
      addToWindowsSystemPath(fLibsFolder);
//TODO: Windows: Java Classloader::usr_paths needed for libs access?
//      if (!checkJavaUsrPath(fLibsFolder)) {
//        log(-1, "Problems setting up on Windows - see errors - might not work and crash later");
//      }

//TODO: jawt.dll no longer needed
//      String lib = "jawt.dll";
//      File fJawtDll = new File(fLibsFolder, lib);
//      FileManager.deleteFileOrFolder(fJawtDll);
//      FileManager.xcopy(new File(javahome, "bin/" + lib), fJawtDll);
//      if (!fJawtDll.exists()) {
//        throw new SikuliXception("problem copying " + fJawtDll);
//      }
    }
    log(lvl, "libsExport: " + libMsg + " %s (%s - %s)", fLibsFolder, getVersionShort(), sxBuildStamp);
    areLibsExported = true;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="10 native libs handling">

  /**
   * INTERNAL USE: load a native library from the libs folder
   *
   * @param libname name of library without prefix/suffix/ending
   */
  public static boolean loadLibrary(String libname) {
    if (isTerminating) {
      return false;
    }
    return RunTime.get().libsLoad(libname);
  }

  /**
   * INTERNAL USE: load a native library from the libs folder
   *
   * @param libname name of library without prefix/suffix/ending
   */
  public static boolean loadLibrary(String libname, boolean useLibsProvided) {
    RunTime runTime = RunTime.get();
    runTime.useLibsProvided = useLibsProvided;
    return loadLibrary(libname);
  }

  private void addToWindowsSystemPath(File fLibsFolder) {
    for (File bridjFile : runTime.fTempPath.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.contains("BridJExtractedLibraries")) {
          return true;
        }
        return false;
      }
    })) {
      runTime.log(4, "cleanTemp: " + bridjFile.getName());
      FileManager.deleteFileOrFolder(bridjFile);
    }
    //TODO String syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
    String syspath = WinUtil.getEnv("PATH");
    if (syspath == null) {
      throw new SikuliXception("addToWindowsSystemPath: cannot access system path");
    } else {
      String libsPath = fLibsFolder.getAbsolutePath();
      if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
        // TODO if (SysJNA.WinKernel32.setEnvironmentVariable("PATH", libsPath + ";" + syspath)) {
        if (null != (syspath = WinUtil.setEnv("PATH", libsPath + ";" + syspath))) {
          if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
            log(-1, "addToWindowsSystemPath: adding to system path did not work:\n%s", syspath);
            throw new SikuliXception("addToWindowsSystemPath: did not work - see error");
          }
        }
        log(lvl, "addToWindowsSystemPath: added to systempath:\n%s", libsPath);
      }
    }
  }

  private boolean checkJavaUsrPath(File fLibsFolder) {
    //TODO Java 9: Windows: Java Classloader::usr_paths needed for libs access?
    if (isJava9()) {
      return true;
    }
    String fpLibsFolder = fLibsFolder.getAbsolutePath();
    Field usrPathsField = null;
    boolean contained = false;
    try {
      usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    } catch (NoSuchFieldException ex) {
      log(-1, "checkJavaUsrPath: get\n%s", ex);
    } catch (SecurityException ex) {
      log(-1, "checkJavaUsrPath: get\n%s", ex);
    }
    if (usrPathsField != null) {
      usrPathsField.setAccessible(true);
      try {
        //get array of paths
        String[] javapaths = (String[]) usrPathsField.get(null);
        //check if the path to add is already present
        for (String p : javapaths) {
          if (new File(p).equals(fLibsFolder)) {
            contained = true;
            break;
          }
        }
        //add the new path
        if (!contained) {
          final String[] newPaths = Arrays.copyOf(javapaths, javapaths.length + 1);
          newPaths[newPaths.length - 1] = fpLibsFolder;
          usrPathsField.set(null, newPaths);
          log(lvl, "checkJavaUsrPath: added to ClassLoader.usrPaths");
          contained = true;
        }
      } catch (IllegalAccessException ex) {
        log(-1, "checkJavaUsrPath: set\n%s", ex);
      } catch (IllegalArgumentException ex) {
        log(-1, "checkJavaUsrPath: set\n%s", ex);
      }
      return contained;
    }
    return false;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="07 init for IDE">
  public static boolean isRunningIDE = false;

  private void initIDEbefore() {
    log(4, "initIDEbefore: entering");
    isRunningIDE = true;
    log(4, "initIDEbefore: leaving");
  }

  private void initIDEafter() {
    log(4, "initIDEafter: entering");
    try {
      cIDE = Class.forName("org.sikuli.ide.SikulixIDE");
      mHide = cIDE.getMethod("hideIDE", new Class[0]);
      mShow = cIDE.getMethod("showIDE", new Class[0]);
    } catch (Exception ex) {
      log(-1, "SikulixIDE: reflection: %s", ex.getMessage());
    }
    log(4, "initIDEafter: leaving");
  }

  Class<?> cIDE = null;
  Method mHide = null;
  Method mShow = null;

  public void hideIDE() {
    if (null != cIDE) {
      try {
        mHide.invoke(null, new Object[0]);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  public void showIDE() {
    if (null != cIDE) {
      try {
        mShow.invoke(null, new Object[0]);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="06 init for API">
  private static RobotDesktop cleanupRobot = null;

  private void initAPI() {
    log(4, "initAPI: entering");
    try {
      cleanupRobot = new RobotDesktop();
    } catch (AWTException e) {
    }
    log(4, "initAPI: leaving");
  }

  private static boolean isLibExported = false;

  public void exportLib() {
    if (isLibExported) {
      return;
    }
    if (!fSikulixLib.exists()
        || !new File(fSikulixLib, "sikuli").exists()) {
      fSikulixLib.mkdir();
      extractResourcesToFolder("Lib", fSikulixLib, null);
    } else {
      extractResourcesToFolder("Lib/sikuli", new File(fSikulixLib, "sikuli"), null);
    }
    // RFW support: module robot should no longer be here (2.1.0)
    File fLibRobot = new File(RunTime.get().fSikulixLib, "robot");
    if (fLibRobot.exists()) {
      FileManager.deleteFileOrFolder(fLibRobot);
    }
    isLibExported = true;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="20 helpers">
  public void crash() {
    int x = 1 / 0;
  }

  public static void pause(int time) {
    try {
      Thread.sleep(time * 1000);
    } catch (InterruptedException ex) {
    }
  }

  public static void pause(float time) {
    try {
      Thread.sleep((int) (time * 1000));
    } catch (InterruptedException ex) {
    }
  }

//TODO abortScripting obsolete
/*
  protected void abortScripting(String msg1, String msg2) {
    Thread current = Thread.currentThread();
    String where = "";
    if (isJythonReady) {
      where = JythonHelper.get().getCurrentLine();
      log(-1, msg2);
      log(-1, msg1 + " %s", where);
    }
    if (where.isEmpty()) {
      throw new RuntimeException(msg1 + msg2);
    }
    current.interrupt();
    current.stop();
  }
*/

  /**
   * INTERNAL USE: to check whether we are running in compiled classes context
   *
   * @return true if the code source location is a folder ending with classes (Maven convention)
   */
  public boolean isRunningFromJar() {
    return runningJar;
  }

  public boolean isJava9(String... args) {
    if (javaVersion > 8) {
      if (args.length > 0) {
        log(-1, "*** Java 9+: %s", args[0]);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean isJava8() {
    return javaVersion > 7;
  }

  public boolean needsRobotFake() {
    return runningMac && Settings.ClickTypeHack;
  }

  /**
   * print out some basic information about the current runtime environment
   */
  public void show() {
    if (sxOptions.hasOptions()) {
      sxOptions.dumpOptions();
    }
    logp("***** show environment for %s %s", SXVersionLong, runType);
    logp("user.home: %s", fUserDir);
    logp("user.dir (work dir): %s", fWorkDir);
    logp("user.name: %s", userName);
    logp("java.io.tmpdir: %s", fTempPath);
    logp("running %dBit(%s) on %s (%s) %s", javaArch, osArch, osNameShort,
        (linuxDistro.contains("???") ? osVersion : linuxDistro), appType);
    logp(javaShow);
    logp("app data folder: %s", fSikulixAppPath);
    //logp("libs folder: %s", fLibsFolder);
    if (runningJar) {
      logp("executing jar: %s", fSxBaseJar);
    }
    if (Debug.getDebugLevel() > minLvl - 1 || isJythonReady) {
      dumpClassPath("sikulix");
      if (isJythonReady) {
        int saveLvl = Debug.getDebugLevel();
        Debug.setDebugLevel(lvl);
        JythonSupport.get().showSysPath();
        Screen.showMonitors();
        Debug.setDebugLevel(saveLvl);
      }
    }
    logp("***** show environment end");
  }

  public boolean testSwitch() {
    if (0 == (new Date().getTime() / 10000) % 2) {
      return true;
    }
    return false;
  }

  public String getVersionShort() {
    return SXVersionShort;
  }

  public String getSystemInfo() {
    return String.format("%s/%s/%s", SXVersionLong, SXSystemVersion, SXJavaVersion);
  }

  public boolean isVersionRelease() {
    return !SXVersion.endsWith("-SNAPSHOT");
  }

  public String getVersion() {
    return SXVersion;
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

  //<editor-fold desc="16 get resources NEW">
  public List<String> getResourceList(String res) {
    return getResourceList(res, clsRef);
  }

  public List<String> getResourceList(String res, Class classReference) {
    List<String> resList = new ArrayList<>();
    CodeSource codeSource = classReference.getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      return resList;
    }
    InputStream aIS = null;
    String content = null;
    res = new File(res, "sikulixcontent").getPath();
    if (runningWindows) {
      res = res.replace("\\", "/");
    }
    if (!res.startsWith("/")) {
      res = "/" + res;
    }
    try {
      aIS = (InputStream) classReference.getResourceAsStream(res);
      if (aIS != null) {
        content = new String(copy(aIS));
        aIS.close();
      }
      log(lvl + 1, "getResourceList: %s (%s)", res, content);
      aIS = null;
    } catch (Exception ex) {
      log(-1, "getResourceList: %s (%s)", res, ex);
    }
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
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="15 handling resources from classpath">

  /**
   * export all resource files from the given subtree on classpath to the given folder retaining the subtree<br>
   * to export a specific file from classpath use extractResourceToFile or extractResourceToString
   *
   * @param fpRessources path of the subtree relative to root
   * @param fFolder      folder where to export (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return the filtered list of files (compact sikulixcontent format)
   */

  public List<String> extractResourcesToFolder(String fpRessources, File fFolder, FilenameFilter filter) {
    List<String> content;
    content = resourceList(fpRessources, filter);
    if (content == null) {
      return null;
    }
    if (fFolder == null) {
      return content;
    }
    return doExtractToFolderWithList(fpRessources, fFolder, content);
  }

  public List<String> doExtractToFolderWithList(String fpRessources, File fFolder, List<String> content) {
    int count = 0;
    int ecount = 0;
    String subFolder = "";
    if (content != null && content.size() > 0) {
      for (String eFile : content) {
        if (eFile == null) {
          continue;
        }
        if (eFile.endsWith("/")) {
          subFolder = eFile.substring(0, eFile.length() - 1);
          continue;
        }
        if (!subFolder.isEmpty()) {
          eFile = new File(subFolder, eFile).getPath();
        }
        if (extractResourceToFile(fpRessources, eFile, fFolder)) {
          log(lvl + 1, "extractResourceToFile done: %s", eFile);
          count++;
        } else {
          ecount++;
        }
      }
    }
    if (ecount > 0) {
      log(lvl, "files exported: %d - skipped: %d from %s to:\n %s", count, ecount, fpRessources, fFolder);
    } else {
      log(lvl, "files exported: %d from: %s to:\n %s", count, fpRessources, fFolder);
    }
    return content;
  }

  /**
   * export all resource files from the given subtree in given jar to the given folder retaining the subtree
   *
   * @param aJar         absolute path to an existing jar or a string identifying the jar on classpath (no leading /)
   * @param fpRessources path of the subtree or file relative to root
   * @param fFolder      folder where to export (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return the filtered list of files (compact sikulixcontent format)
   */
  public List<String> extractResourcesToFolderFromJar(String aJar, String fpRessources, File fFolder, FilenameFilter
      filter) {
    List<String> content = new ArrayList<String>();
    File faJar = new File(aJar);
    URL uaJar = null;
    fpRessources = FileManager.slashify(fpRessources, false);
    if (faJar.isAbsolute()) {
      if (!faJar.exists()) {
        log(-1, "extractResourcesToFolderFromJar: does not exist:\n%s", faJar);
        return null;
      }
      try {
        uaJar = new URL("jar", null, "file:" + aJar);
      } catch (MalformedURLException ex) {
        log(-1, "extractResourcesToFolderFromJar: bad URL for:\n%s", faJar);
        return null;
      }
    } else {
      uaJar = fromClasspath(aJar);
      if (uaJar == null) {
        log(-1, "extractResourcesToFolderFromJar: not on classpath: %s", aJar);
        return null;
      }
      try {
        String sJar = "file:" + uaJar.getPath() + "!/";
        uaJar = new URL("jar", null, sJar);
      } catch (MalformedURLException ex) {
        log(-1, "extractResourcesToFolderFromJar: bad URL for:\n%s", uaJar);
        return null;
      }
    }
    content = doResourceListJar(uaJar, fpRessources, content, filter);
    if (fFolder == null) {
      return content;
    }
    copyFromJarToFolderWithList(uaJar, fpRessources, content, fFolder);
    return content;
  }

  /**
   * store a resource found on classpath to a file in the given folder with same filename
   *
   * @param inPrefix a subtree found in classpath
   * @param inFile   the filename combined with the prefix on classpath
   * @param outDir   a folder where to export
   * @return success
   */
  public boolean extractResourceToFile(String inPrefix, String inFile, File outDir) {
    return extractResourceToFile(inPrefix, inFile, outDir, "");
  }

  /**
   * store a resource found on classpath to a file in the given folder
   *
   * @param inPrefix a subtree found in classpath
   * @param inFile   the filename combined with the prefix on classpath
   * @param outDir   a folder where to export
   * @param outFile  the filename for export
   * @return success
   */
  public boolean extractResourceToFile(String inPrefix, String inFile, File outDir, String outFile) {
    InputStream aIS;
    FileOutputStream aFileOS;
    String content = inPrefix + "/" + inFile;
    try {
      content = runningWindows ? content.replace("\\", "/") : content;
      if (!content.startsWith("/")) {
        content = "/" + content;
      }
      aIS = (InputStream) clsRef.getResourceAsStream(content);
      if (aIS == null) {
        File fInFile = new File(content);
        if (!fInFile.exists()) {
          throw new IOException(String.format("resource not accessible: %s", content));
        }
        aIS = new FileInputStream(fInFile);
      }
      File out = outFile.isEmpty() ? new File(outDir, inFile) : new File(outDir, outFile);
      if (!out.getParentFile().exists()) {
        out.getParentFile().mkdirs();
      }
      aFileOS = new FileOutputStream(out);
      copy(aIS, aFileOS);
      aIS.close();
      aFileOS.close();
    } catch (Exception ex) {
      log(-1, "extractResourceToFile: %s\n%s", content, ex);
      return false;
    }
    return true;
  }

  /**
   * store the content of a resource found on classpath in the returned string
   *
   * @param inPrefix a subtree from root found in classpath (leading /)
   * @param inFile   the filename combined with the prefix on classpath
   * @param encoding
   * @return file content
   */
  public String extractResourceToString(String inPrefix, String inFile, String encoding) {
    InputStream aIS = null;
    String out = null;
    String content = inPrefix + "/" + inFile;
    if (!content.startsWith("/")) {
      content = "/" + content;
    }
    try {
      content = runningWindows ? content.replace("\\", "/") : content;
      aIS = (InputStream) clsRef.getResourceAsStream(content);
      if (aIS == null) {
        throw new IOException("resource not accessible");
      }
      if (encoding == null) {
        encoding = "UTF-8";
        out = new String(copy(aIS));
      } else if (encoding.isEmpty()) {
        out = new String(copy(aIS), "UTF-8");
      } else {
        out = new String(copy(aIS), encoding);
      }
      aIS.close();
      aIS = null;
    } catch (Exception ex) {
      log(-1, "extractResourceToString as %s from:\n%s\n%s", encoding, content, ex);
    }
    try {
      if (aIS != null) {
        aIS.close();
      }
    } catch (Exception ex) {
    }
    return out;
  }

  public URL resourceLocation(String folderOrFile) {
    log(lvl, "resourceLocation: (%s) %s", clsRef, folderOrFile);
    if (!folderOrFile.startsWith("/")) {
      folderOrFile = "/" + folderOrFile;
    }
    return clsRef.getResource(folderOrFile);
  }

  private List<String> resourceList(String folder, FilenameFilter filter) {
    List<String> files = new ArrayList<String>();
    if (!folder.startsWith("/")) {
      folder = "/" + folder;
    }
    URL uFolder = resourceLocation(folder);
    File fFolder = null;
    if (uFolder == null) {
      fFolder = new File(folder);
      if (fFolder.exists()) {
        files = doResourceListFolder(fFolder, files, filter);
      } else {
        log(lvl, "resourceList: not found: %s", folder);
      }
      return files;
    }
    try {
      uFolder = new URL(uFolder.toExternalForm().replaceAll(" ", "%20"));
    } catch (Exception ex) {
    }
    URL uContentList = clsRef.getResource(folder + "/" + fpContent);
    if (uContentList != null) {
      return doResourceListWithList(folder, files, filter);
    }
    try {
      fFolder = new File(uFolder.toURI());
      log(lvl, "resourceList: having folder: %s", fFolder);
      files.add(fFolder.getPath());
      files = doResourceListFolder(fFolder, files, filter);
      files.remove(0);
      return files;
    } catch (Exception ex) {
      if (!"jar".equals(uFolder.getProtocol())) {
        log(lvl, "resourceList:\n%s", folder);
        log(-1, "resourceList: URL neither folder nor jar:\n%s", ex);
        return null;
      }
    }
    String[] parts = uFolder.getPath().split("!");
    if (parts.length < 2 || !parts[0].startsWith("file:")) {
      log(lvl, "resourceList:\n%s", folder);
      log(-1, "resourceList: not a valid jar URL: " + uFolder.getPath());
      return null;
    }
    String fpFolder = parts[1];
    log(lvl, "resourceList: having jar: %s", uFolder);
    return doResourceListJar(uFolder, fpFolder, files, filter);
  }

  /**
   * write the list as it is produced by calling extractResourcesToFolder to the given file with system line
   * separator<br>
   * non-compact format: every file with full path
   *
   * @param folder path of the subtree relative to root with leading /
   * @param target the file to write the list (if null, only list - no file)
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public String[] resourceListAsFile(String folder, File target, FilenameFilter filter) {
    String content = resourceListAsString(folder, filter);
    if (content == null) {
      log(-1, "resourceListAsFile: did not work: %s", folder);
      return null;
    }
    if (target != null) {
      try {
        FileManager.deleteFileOrFolder(target.getAbsolutePath());
        target.getParentFile().mkdirs();
        PrintWriter aPW = new PrintWriter(target);
        aPW.write(content);
        aPW.close();
      } catch (Exception ex) {
        log(-1, "resourceListAsFile: %s:\n%s", target, ex);
      }
    }
    return content.split(System.getProperty("line.separator"));
  }

  /**
   * write the list as it is produced by calling extractResourcesToFolder to the given file with system line
   * separator<br>
   * compact sikulixcontent format
   *
   * @param folder       path of the subtree relative to root with leading /
   * @param targetFolder the folder where to store the file sikulixcontent (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public String[] resourceListAsSikulixContent(String folder, File targetFolder, FilenameFilter filter) {
    List<String> contentList = resourceList(folder, filter);
    if (contentList == null) {
      log(-1, "resourceListAsSikulixContent: did not work: %s", folder);
      return null;
    }
    File target = null;
    String arrString[] = new String[contentList.size()];
    try {
      PrintWriter aPW = null;
      if (targetFolder != null) {
        target = new File(targetFolder, fpContent);
        FileManager.deleteFileOrFolder(target);
        target.getParentFile().mkdirs();
        aPW = new PrintWriter(target);
      }
      int n = 0;
      for (String line : contentList) {
        arrString[n++] = line;
        if (targetFolder != null) {
          aPW.println(line);
        }
      }
      if (targetFolder != null) {
        aPW.close();
      }
    } catch (Exception ex) {
      log(-1, "resourceListAsFile: %s:\n%s", target, ex);
    }
    return arrString;
  }

  /**
   * write the list as it is produced by calling extractResourcesToFolder to the given file with system line
   * separator<br>
   * compact sikulixcontent format
   *
   * @param aJar         absolute path to an existing jar or a string identifying the jar on classpath (no leading /)
   * @param folder       path of the subtree relative to root with leading /
   * @param targetFolder the folder where to store the file sikulixcontent (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public String[] resourceListAsSikulixContentFromJar(String aJar, String folder, File targetFolder, FilenameFilter
      filter) {
    List<String> contentList = extractResourcesToFolderFromJar(aJar, folder, null, filter);
    if (contentList == null || contentList.size() == 0) {
      log(-1, "resourceListAsSikulixContentFromJar: did not work: %s", folder);
      return null;
    }
    File target = null;
    String arrString[] = new String[contentList.size()];
    try {
      PrintWriter aPW = null;
      if (targetFolder != null) {
        target = new File(targetFolder, fpContent);
        FileManager.deleteFileOrFolder(target);
        target.getParentFile().mkdirs();
        aPW = new PrintWriter(target);
      }
      int n = 0;
      for (String line : contentList) {
        arrString[n++] = line;
        if (targetFolder != null) {
          aPW.println(line);
        }
      }
      if (targetFolder != null) {
        aPW.close();
      }
    } catch (Exception ex) {
      log(-1, "resourceListAsFile: %s:\n%s", target, ex);
    }
    return arrString;
  }

  /**
   * write the list produced by calling extractResourcesToFolder to the returned string with system line separator<br>
   * non-compact format: every file with full path
   *
   * @param folder path of the subtree relative to root with leading /
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return the resulting string
   */
  public String resourceListAsString(String folder, FilenameFilter filter) {
    return resourceListAsString(folder, filter, null);
  }

  /**
   * write the list produced by calling extractResourcesToFolder to the returned string with given separator<br>
   * non-compact format: every file with full path
   *
   * @param folder    path of the subtree relative to root with leading /
   * @param filter    implementation of interface FilenameFilter or null for no filtering
   * @param separator to be used to separate the entries
   * @return the resulting string
   */
  public String resourceListAsString(String folder, FilenameFilter filter, String separator) {
    List<String> aList = resourceList(folder, filter);
    if (aList == null) {
      return null;
    }
    if (separator == null) {
      separator = System.getProperty("line.separator");
    }
    String out = "";
    String subFolder = "";
    if (aList != null && aList.size() > 0) {
      for (String eFile : aList) {
        if (eFile == null) {
          continue;
        }
        if (eFile.endsWith("/")) {
          subFolder = eFile.substring(0, eFile.length() - 1);
          continue;
        }
        if (!subFolder.isEmpty()) {
          eFile = new File(subFolder, eFile).getPath();
        }
        out += eFile.replace("\\", "/") + separator;
      }
    }
    return out;
  }

  private List<String> doResourceListFolder(File fFolder, List<String> files, FilenameFilter filter) {
    int localLevel = testing ? lvl : lvl + 1;
    String subFolder = "";
    if (fFolder.isDirectory()) {
      if (files.size() > 0 && !FileManager.pathEquals(fFolder.getPath(), files.get(0))) {
        subFolder = fFolder.getPath().substring(files.get(0).length() + 1).replace("\\", "/") + "/";
        if (filter != null && !filter.accept(new File(files.get(0), subFolder), "")) {
          return files;
        }
      } else {
        logp(localLevel, "scanning folder:\n%s", fFolder);
        subFolder = "/";
        files.add(subFolder);
      }
      String[] subList = fFolder.list();
      for (String entry : subList) {
        File fEntry = new File(fFolder, entry);
        if (fEntry.isDirectory()) {
          files.add(fEntry.getAbsolutePath().substring(1 + files.get(0).length()).replace("\\", "/") + "/");
          doResourceListFolder(fEntry, files, filter);
          files.add(subFolder);
        } else {
          if (filter != null && !filter.accept(fFolder, entry)) {
            continue;
          }
          logp(localLevel, "from %s adding: %s", (subFolder.isEmpty() ? "." : subFolder), entry);
          files.add(fEntry.getAbsolutePath().substring(1 + fFolder.getPath().length()));
        }
      }
    }
    return files;
  }

  public List<String> doResourceListWithList(String folder, List<String> files, FilenameFilter filter) {
    String content = extractResourceToString(folder, fpContent, "");
    String[] contentList = content.split(content.indexOf("\r") != -1 ? "\r\n" : "\n");
    if (filter == null) {
      files.addAll(Arrays.asList(contentList));
    } else {
      for (String fpFile : contentList) {
        if (filter.accept(new File(fpFile), "")) {
          files.add(fpFile);
        }
      }
    }
    return files;
  }

  private List<String> doResourceListJar(URL uJar, String fpResource, List<String> files, FilenameFilter filter) {
    ZipInputStream zJar;
    String fpJar = uJar.getPath().split("!")[0];
    int localLevel = testing ? lvl : lvl + 1;
    String fileSep = "/";
    if (!fpJar.endsWith(".jar")) {
      return files;
    }
    logp(localLevel, "scanning jar:\n%s", uJar);
    fpResource = (fpResource.startsWith("/") ? fpResource.substring(1) : fpResource) + "/";
    File fFolder = new File(fpResource);
    File fSubFolder = null;
    ZipEntry zEntry;
    String subFolder = "";
    boolean skip = false;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        if (zEntry.getName().endsWith("/")) {
          continue;
        }
        String zePath = zEntry.getName();
        if (zePath.startsWith(fpResource)) {
//          if (fpResource.length()  == zePath.length()) {
//            files.add(zePath);
//            return files;
//          }
          String zeName = zePath.substring(fpResource.length());
          int nSep = zeName.lastIndexOf(fileSep);
          String zefName = zeName.substring(nSep + 1, zeName.length());
          String zeSub = "";
          if (nSep > -1) {
            zeSub = zeName.substring(0, nSep + 1);
            if (!subFolder.equals(zeSub)) {
              subFolder = zeSub;
              fSubFolder = new File(fFolder, subFolder);
              skip = false;
              if (filter != null && !filter.accept(fSubFolder, "")) {
                skip = true;
                continue;
              }
              files.add(zeSub);
            }
            if (skip) {
              continue;
            }
          } else {
            if (!subFolder.isEmpty()) {
              subFolder = "";
              fSubFolder = fFolder;
              files.add("/");
            }
          }
          if (filter != null && !filter.accept(fSubFolder, zefName)) {
            continue;
          }
          files.add(zefName);
          logp(localLevel, "from %s adding: %s", (zeSub.isEmpty() ? "." : zeSub), zefName);
        }
      }
    } catch (Exception ex) {
      log(-1, "doResourceListJar: %s", ex);
      return files;
    }
    return files;
  }

  public List<String> listFilesInJar(URL uJar) {
    ZipInputStream zJar;
    String fpJar = uJar.getPath().split("!")[0];
    int localLevel = testing ? lvl : lvl + 1;
    String fileSep = "/";
    if (!fpJar.endsWith(".jar")) {
      return null;
    }
    logp(localLevel, "listFilesInJar: scanning jar:\n%s", uJar);
    List<String> files = new ArrayList<>();
    ZipEntry zEntry;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        if (zEntry.getName().endsWith("/")) {
          continue;
        }
        String zePath = zEntry.getName();
        files.add(zePath);
        logp(localLevel, "listFilesInJar: adding: %s", zePath);
      }
    } catch (Exception ex) {
      log(-1, "listFilesInJar: %s", ex);
      return files;
    }
    return files;
  }

  private boolean copyFromJarToFolderWithList(URL uJar, String fpRessource, List<String> files, File fFolder) {
    if (files == null || files.isEmpty()) {
      log(lvl, "copyFromJarToFolderWithList: list of files is empty");
      return false;
    }
    String fpJar = uJar.getPath().split("!")[0];
    if (!fpJar.endsWith(".jar")) {
      return false;
    }
    int localLevel = testing ? lvl : lvl + 1;
    logp(localLevel, "scanning jar:\n%s", uJar);
    fpRessource = fpRessource.startsWith("/") ? fpRessource.substring(1) : fpRessource;

    String subFolder = "";

    int maxFiles = files.size() - 1;
    int nFiles = 0;

    ZipEntry zEntry;
    ZipInputStream zJar;
    String zPath;
    int prefix = fpRessource.length();
    fpRessource += !fpRessource.isEmpty() ? "/" : "";
    String current = "/";
    boolean shouldStop = false;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        zPath = zEntry.getName();
        if (zPath.endsWith("/")) {
          continue;
        }
        while (current.endsWith("/")) {
          if (nFiles > maxFiles) {
            shouldStop = true;
            break;
          }
          subFolder = current.length() == 1 ? "" : current;
          current = files.get(nFiles++);
          if (!current.endsWith("/")) {
            current = fpRessource + subFolder + current;
            break;
          }
        }
        if (shouldStop) {
          break;
        }
        if (zPath.startsWith(current)) {
          if (zPath.length() == fpRessource.length() - 1) {
            log(-1, "extractResourcesToFolderFromJar: only ressource folders allowed - use filter");
            return false;
          }
          logp(localLevel, "copying: %s", zPath);
          File out = new File(fFolder, zPath.substring(prefix));
          if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
          }
          FileOutputStream aFileOS = new FileOutputStream(out);
          copy(zJar, aFileOS);
          aFileOS.close();
          if (nFiles > maxFiles) {
            break;
          }
          current = files.get(nFiles++);
          if (!current.endsWith("/")) {
            current = fpRessource + subFolder + current;
          }
        }
      }
      zJar.close();
    } catch (Exception ex) {
      log(-1, "doResourceListJar: %s", ex);
      return false;
    }
    return true;
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
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

  private byte[] copy(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length = 0;
    while ((length = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, length);
    }
    return baos.toByteArray();
  }

  public class oneFileFilter implements FilenameFilter {

    String aFile;

    public oneFileFilter(String aFileGiven) {
      aFile = aFileGiven;
    }

    @Override
    public boolean accept(File dir, String name) {
      if (name.contains(aFile)) {
        return true;
      }
      return false;
    }

  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="12 classpath handling">
  private void storeClassPath() {
    if (isJava9()) {
      String separator = File.pathSeparator;
      String cp = System.getProperty("java.class.path");
      classPathList = Arrays.asList(cp.split(separator));
    } else {
      classPathActual.clear();
      URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      classPathActual = Arrays.asList(sysLoader.getURLs());
      classPathList.clear();
      for (URL urlPath : classPathActual) {
        classPathList.add(urlPath.toExternalForm());
      }
    }
  }

  /**
   * print the current classpath entries to sysout
   */
  public void dumpClassPath() {
    dumpClassPath(null);
  }

  /**
   * print the current classpath entries to sysout whose path name contain the given string
   *
   * @param filter the fileter string
   */
  public void dumpClassPath(String filter) {
    filter = filter == null ? "" : filter;
    logp("*** classpath dump %s", filter);
    storeClassPath();
    filter = filter.toUpperCase();
    int n = 0;
    for (String sEntry : classPathList) {
      if (!filter.isEmpty()) {
        if (!sEntry.toUpperCase().contains(filter)) {
          n++;
          continue;
        }
      }
      logp("%3d: %s", n, sEntry);
      n++;
    }
    logp("*** classpath dump end");
  }

  /**
   * check whether a classpath entry contains the given identifying string, stops on first match
   *
   * @param artefact the identifying string
   * @return the absolute path of the entry found - null if not found
   */
  private String isOnClasspath(String artefact, boolean isJar) {
    artefact = FileManager.slashify(artefact, false);
    String cpe = null;
    if (classPathList.isEmpty()) {
      storeClassPath();
    }
    for (String entry : classPathList) {
      String sEntry = FileManager.slashify(new File(entry).getPath(), false);
      if (sEntry.contains(artefact)) {
        if (isJar) {
          if (!sEntry.endsWith(".jar")) {
            continue;
          }
          if (!new File(sEntry).getName().contains(artefact)) {
            continue;
          }
          if (new File(sEntry).getName().contains("4" + artefact)) {
            continue;
          }
        }
        cpe = new File(entry).getPath();
        break;
      }
    }
    return cpe;
  }

  public String isJarOnClasspath(String artefact) {
    return isOnClasspath(artefact, true);
  }

  public String isOnClasspath(String artefact) {
    return isOnClasspath(artefact, false);
  }

  public URL fromClasspath(String artefact) {
    artefact = FileManager.slashify(artefact, false).toUpperCase();
    URL cpe = null;
    String scpe = null;
    if (classPathActual.isEmpty()) {
      storeClassPath();
    }
    for (String entry : classPathList) {
      String sEntry = FileManager.slashify(new File(entry).getPath(), false);
      if (sEntry.toUpperCase().contains(artefact)) {
        scpe = entry;
        break;
      }
    }
    if (null != scpe) {
      try {
        cpe = new URL(scpe);
      } catch (MalformedURLException e) {
      }
    }
    return cpe;
  }

  /**
   * check wether a the given URL is on classpath
   *
   * @param path URL to look for
   * @return true if found else otherwise
   */
  public boolean isOnClasspath(URL path) {
    if (classPathActual.isEmpty()) {
      storeClassPath();
    }
    for (String entry : classPathList) {
    }
    return false;
  }

  List<String> sxClasspath = new ArrayList<>();

  public boolean addToClasspath(String jarOrFolder) {
    return addToClasspath(jarOrFolder, "");
  }

  public boolean addToClasspath(String jarOrFolder, String caller) {
    if (null != isOnClasspath(jarOrFolder)) {
      return true;
    }
    if (isJava9("skipped: addToClasspath() - caller: " + caller)) {
      sxClasspath.add(jarOrFolder);
      return false;
    }
    if (!new File(jarOrFolder).exists()) {
      log(-1, "addToClasspath: does not exist - not added:\n%s", jarOrFolder);
      return false;
    }
    //TODO addToClasspath
//    log(lvl, "addToClasspath:\n%s", uJarOrFolder);
//    Method method;
//    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//    Class sysclass = URLClassLoader.class;
//    try {
//      method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
//      method.setAccessible(true);
//      method.invoke(sysLoader, new Object[]{uJarOrFolder});
//    } catch (Exception ex) {
//      log(-1, "Did not work: %s", ex.getMessage());
//      return false;
//    }
//    storeClassPath();
    return false;
  }

  public File asExtension(String fpJar) {
    File fJarFound = new File(FileManager.normalizeAbsolute(fpJar));
    if (!fJarFound.exists()) {
      String fpCPEntry = runTime.isOnClasspath(fJarFound.getName());
      if (fpCPEntry == null) {
        fJarFound = new File(runTime.fSikulixExtensions, fpJar);
        if (!fJarFound.exists()) {
          fJarFound = new File(runTime.fSikulixLib, fpJar);
          if (!fJarFound.exists()) {
            fJarFound = null;
          }
        }
      } else {
        fJarFound = new File(fpCPEntry, fJarFound.getName());
      }
    } else {
      return null;
    }
    return fJarFound;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="22 system enviroment">

  /**
   * print the current java system properties key-value pairs sorted by key
   */
  public void dumpSysProps() {
    dumpSysProps(null);
  }

  /**
   * print the current java system properties key-value pairs sorted by key but only keys containing filter
   *
   * @param filter the filter string
   */
  public void dumpSysProps(String filter) {
    filter = filter == null ? "" : filter;
    logp("*** system properties dump " + filter);
    Properties sysProps = System.getProperties();
    ArrayList<String> keysProp = new ArrayList<String>();
    Integer nL = 0;
    String entry;
    for (Object e : sysProps.keySet()) {
      entry = (String) e;
      if (entry.length() > nL) {
        nL = entry.length();
      }
      if (filter.isEmpty() || !filter.isEmpty() && entry.contains(filter)) {
        keysProp.add(entry);
      }
    }
    Collections.sort(keysProp);
    String form = "%-" + nL.toString() + "s = %s";
    for (Object e : keysProp) {
      logp(form, e, sysProps.get(e));
    }
    logp("*** system properties dump end" + filter);
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="21 runcmd">

  /**
   * run a system command finally using Java::Runtime.getRuntime().exec(args) and waiting for completion
   *
   * @param cmd the command as it would be given on command line, quoting is preserved
   * @return the output produced by the command (sysout [+ "*** error ***" + syserr] if the syserr part is present, the
   * command might have failed
   */
  public String runcmd(String cmd) {
    return runcmd(new String[]{cmd});
  }

  /**
   * run a system command finally using Java::Runtime.getRuntime().exec(args) and waiting for completion
   *
   * @param args the command as it would be given on command line splitted into the space devided parts, first part is
   *             the command, the rest are parameters and their values
   * @return the output produced by the command (sysout [+ "*** error ***" + syserr] if the syserr part is present, the
   * command might have failed
   */
  public String runcmd(String args[]) {
    if (args.length == 0) {
      return "";
    }
    boolean silent = false;
    if (args.length == 1) {
      String separator = "\"";
      ArrayList<String> argsx = new ArrayList<String>();
      StringTokenizer toks;
      String tok;
      String cmd = args[0];
      if (Settings.isWindows()) {
        cmd = cmd.replaceAll("\\\\ ", "%20;");
      }
      toks = new StringTokenizer(cmd);
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
        argsx.add(tok.replaceAll("%20;", " "));
      }
      args = argsx.toArray(new String[0]);
    }
    if (args[0].startsWith("!")) {
      silent = true;
      args[0] = args[0].substring(1);
    }
    if (args[0].startsWith("#")) {
      String pgm = args[0].substring(1);
      args[0] = (new File(pgm)).getAbsolutePath();
      runcmd(new String[]{"chmod", "ugo+x", args[0]});
    }
    String result = "";
    String error = runCmdError + NL;
    String errorOut = "";
    boolean hasError = false;
    int retVal;
    try {
      if (!silent) {
        if (lvl <= Debug.getDebugLevel()) {
          log(lvl, arrayToQuotedString(args));
        } else {
          Debug.info("runcmd: " + arrayToQuotedString(args));
        }
      }
      //TODO use ProcessRunner
      Process process = Runtime.getRuntime().exec(args);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String s;
      while ((s = stdInput.readLine()) != null) {
        if (!s.isEmpty()) {
          result += s + NL;
        }
      }
      while ((s = stdError.readLine()) != null) {
        if (!s.isEmpty()) {
          errorOut += s + NL;
        }
      }
      if (!errorOut.isEmpty()) {
        error = error + errorOut;
        hasError = true;
      }
      process.waitFor();
      retVal = process.exitValue();
      process.destroy();
    } catch (Exception e) {
      log(-1, "fatal error: " + e);
      result = String.format(error + "%s", e);
      retVal = 9999;
      hasError = true;
    }
    if (hasError) {
      result += error;
    }
    lastResult = result;
    return String.format("%d%s%s", retVal, NL, result);
  }

  public String getLastCommandResult() {
    return lastResult;
  }
//</editor-fold>
}
