/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.util.JythonHelper;
import org.sikuli.util.LinuxSupport;
import org.sikuli.util.SysJNA;
import org.opencv.core.Core;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.*;
import java.util.List;
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

  //<editor-fold defaultstate="collapsed" desc="logging">
  private final String me = "RunTime%s: ";
  private int lvl = 3;
  private int minLvl = lvl;
  private static String preLogMessages = "";

  private void log(int level, String message, Object... args) {
    Debug.logx(level, String.format(me, runType) + message, args);
  }

  private void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }

  public void terminate(int retval, String message, Object... args) {
    Sikulix.terminate(retval, message, args);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="variables">
  public enum Type {
    IDE, API, SETUP, INIT
//    IDE, API, INIT
  }

  private enum theSystem {
    WIN, MAC, LUX, FOO
  }

  private static Options sxOptions = null;

  public static File scriptProject = null;
  public static URL uScriptProject = null;
  private static boolean isTerminating = false;

  public static String appDataMsg = "";

  private static RunTime runTime = null;
  private static int debugLevelSaved;
  private static String debugLogfileSaved;
  public static boolean testing = false;
  public static boolean testingWinApp = false;

  public Type runType = Type.INIT;

  public String sxBuild = "";
  public String sxBuildStamp = "";
  public String jreVersion = java.lang.System.getProperty("java.runtime.version");
  public Preferences optionsIDE = null;
  public ClassLoader classLoader = RunTime.class.getClassLoader();
  public String userName = "";
  public String fpBaseTempPath = "";

  private Class clsRef = RunTime.class;
  private Class clsRefAPI = Sikulix.class;

  private List<URL> classPath = new ArrayList<>();
  private List<String> classPathList = new ArrayList<>();
  public File fTempPath = null;
  public File fBaseTempPath = null;
  public File fLibsFolder = null;
  public String fpJarLibs = "/sikulixlibs/";
  public String fpSysLibs = null;
  boolean areLibsExported = false;
  private Map<String, Boolean> libsLoaded = new HashMap<String, Boolean>();
  public File fUserDir = null;
  public File fWorkDir = null;
  public File fTestFolder = null;
  public File fTestFile = null;
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
  public boolean runningWinApp = false;
  public boolean runningMacApp = false;
  private theSystem runningOn = theSystem.FOO;
  private final String osNameSysProp = System.getProperty("os.name");
  private final String osVersionSysProp = System.getProperty("os.version");
  public String javaShow = "not-set";
  public int javaArch = 32;
  public String osArch = "??";
  public int javaVersion = 0;
  public String javahome = FileManager.slashify(System.getProperty("java.home"), false);
  public String osName = "NotKnown";
  public String sysName = "NotKnown";
  public String osVersion = "";
  private String appType = null;
  public String linuxDistro = "???LINUX???";

  public boolean allowMultipleInstances = false;
  public boolean shouldRunServer = false;
  public boolean runningScripts = false;
  public boolean runningInteractive = false;
  public boolean runningTests = false;
  public String interactiveRunner;

  public final static String runCmdError = "*****error*****";
  public static String NL = "\n";
  public File fLibsProvided;
  public File fLibsLocal;
  public boolean useLibsProvided = false;
  private String lastResult = "";
  public boolean shouldCleanDownloads = false;
  public boolean isJythonReady = false;
  private boolean shouldExport = false;

  GraphicsEnvironment genv = null;
  GraphicsDevice[] gdevs;
  public Rectangle[] monitorBounds = null;
  Rectangle rAllMonitors;
  int mainMonitor = -1;
  int nMonitors = 0;
  Point pNull = new Point(0, 0);

  static String sikulixGlobalDebug = null;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="instance">
  private RunTime() {
  }

  public static synchronized RunTime get(Type typ) {
    return get(typ, null);
  }

  public static void checkArgs(RunTime runTime, String[] args, Type typ) {
    if (args == null) {
      return;
    }
    int debugLevel = -99;
    boolean runningScripts = false;
    boolean allowMultipleInstances = false;
    List<String> options = new ArrayList<String>();
    options.addAll(Arrays.asList(args));
    for (int n = 0; n < options.size(); n++) {
      String opt = options.get(n);
      if ("-s".equals(opt.toLowerCase())) {
        runTime.shouldRunServer = true;
      }
      if (!opt.startsWith("-")) {
        continue;
      }
      if (opt.startsWith("-d")) {
        try {
          debugLevel = n + 1 == options.size() ? -1 : Integer.decode(options.get(n + 1));
          Debug.on(debugLevel);
        } catch (Exception ex) {
        }
      } else if (opt.startsWith("-r")) {
        runTime.runningScripts = true;
      } else if (opt.startsWith("-i")) {
        runTime.runningInteractive = true;
      } else if (opt.startsWith("-m")) {
        runTime.allowMultipleInstances = true;
      }
    }
  }

  public static synchronized RunTime get(Type typ, String[] clArgs) {
    if (runTime != null) {
      return runTime;
    }
    sikulixGlobalDebug = System.getenv("SIKULIXDEBUG");
    if (sikulixGlobalDebug != null) {
      Debug.setDebugLevel(3);
      Debug.setWithTimeElapsed(0);
      Debug.globalTraceOn();
    }
    Debug.log(3, "RunTimeINIT: starting %s", typ);
    runTime = new RunTime();
    int debugLevel = 0;

    checkArgs(runTime, clArgs, typ);

//<editor-fold defaultstate="collapsed" desc="versions">
    Debug.log(3, "RunTimeINIT: java version");
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
      runTime.terminate(999, "Java version must at least be 8 (%s)", runTime.javaShow);
    }

    if (null == vSysArch) {
      runTime.terminate(999, "Java arch not 64 Bit or not detected (%s)", runTime.javaShow);
    }

    runTime.osVersion = runTime.osVersionSysProp;
    String os = runTime.osNameSysProp.toLowerCase();
    if (os.startsWith("windows")) {
      runTime.runningOn = theSystem.WIN;
      runTime.sysName = "windows";
      runTime.osName = "Windows";
      runTime.runningWindows = true;
      runTime.NL = "\r\n";
    } else if (os.startsWith("mac")) {
      runTime.runningOn = theSystem.MAC;
      runTime.sysName = "mac";
      runTime.osName = "Mac OSX";
      runTime.runningMac = true;
    } else if (os.startsWith("linux")) {
      runTime.runningOn = theSystem.LUX;
      runTime.sysName = "linux";
      runTime.osName = "Linux";
      runTime.runningLinux = true;
//        String result = runTime.runcmd("lsb_release -i -r -s");
//        if (result.contains("*** error ***")) {
//          runTime.log(-1, "command returns error: lsb_release -i -r -s\n%s", result);
//        } else {
//          runTime.linuxDistro = result.replaceAll("\n", " ").trim();
//        }
    } else {
      // Presume Unix -- pretend to be Linux
      runTime.runningOn = theSystem.LUX;
      runTime.sysName = os;
      runTime.osName = runTime.osNameSysProp;
      runTime.runningLinux = true;
      runTime.linuxDistro = runTime.osNameSysProp;
    }
    runTime.fpJarLibs += runTime.sysName + "/libs" + runTime.javaArch;
    runTime.fpSysLibs = runTime.fpJarLibs.substring(1);

    Debug.log(3, "RunTimeINIT: user.home");
    String aFolder = System.getProperty("user.home");
    if (aFolder == null || aFolder.isEmpty() || !(runTime.fUserDir = new File(aFolder)).exists()) {
      runTime.terminate(999, "JavaSystemProperty::user.home not valid");
    }

    Debug.log(3, "RunTimeINIT: user.dir");
    aFolder = System.getProperty("user.dir");
    if (aFolder == null || aFolder.isEmpty() || !(runTime.fWorkDir = new File(aFolder)).exists()) {
      runTime.terminate(999, "JavaSystemProperty::user.dir not valid");
    }

    Debug.log(3, "RunTimeINIT: app data path");
    runTime.fSikulixAppPath = new File("SikulixAppDataNotAvailable");
    if (runTime.runningWindows) {
      appDataMsg = "init: Windows: %APPDATA% not valid (null or empty) or is not accessible:\n%s";
      String tmpdir = System.getenv("APPDATA");
      if (tmpdir != null && !tmpdir.isEmpty()) {
        runTime.fAppPath = new File(tmpdir);
        runTime.fSikulixAppPath = new File(runTime.fAppPath, "Sikulix");
      }
    } else if (runTime.runningMac) {
      appDataMsg = "init: Mac: SikulxAppData does not exist or is not accessible:\n%s";
      runTime.fAppPath = new File(runTime.fUserDir, "Library/Application Support");
      runTime.fSikulixAppPath = new File(runTime.fAppPath, "Sikulix");
    } else if (runTime.runningLinux) {
      runTime.fAppPath = runTime.fUserDir;
      runTime.fSikulixAppPath = new File(runTime.fAppPath, ".Sikulix");
      appDataMsg = "init: Linux: SikulxAppData does not exist or is not accessible:\n%s";
    }
    runTime.fSikulixStore = new File(runTime.fSikulixAppPath, "SikulixStore");
    runTime.fSikulixStore.mkdirs();
//</editor-fold>

    debugLevelSaved = Debug.getDebugLevel();
    debugLogfileSaved = Debug.logfile;

    Debug.log(3, "RunTimeINIT: store debug.txt");
    File fDebug = new File(runTime.fSikulixStore, "SikulixDebug.txt");
    if (fDebug.exists()) {
      if (Debug.getDebugLevel() == 0) {
        Debug.setDebugLevel(3);
      }
      Debug.setLogFile(fDebug.getAbsolutePath());
      if (Type.IDE.equals(typ)) {
        System.setProperty("sikuli.console", "false");
      }
      Debug.log(3, "auto-debugging with level %d into:\n%s", Debug.getDebugLevel(), fDebug);
    }

    runTime.fTestFolder = new File(runTime.fUserDir, "SikulixTest");
    runTime.fTestFile = new File(runTime.fTestFolder, "SikulixTest.txt");

    sxOptions = Options.init(typ);
    int dl = sxOptions.getOptionInteger("Debug.level");
    if (dl > 0 && Debug.getDebugLevel() < 2) {
      Debug.setDebugLevel(dl);
    }
    if (Debug.getDebugLevel() == 2) {
      sxOptions.dumpOptions();
    }

    Settings.init(); // force Settings initialization
    runTime.initSikulixOptions();

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

/*
  public static synchronized RunTime get(String[] args) {
    if (runTime == null) {
      return get(Type.API, args);
    }
    return runTime;
  }
*/

  public static synchronized RunTime get() {
    if (runTime == null) {
      return get(Type.API);
    }
    return runTime;
  }

  public static synchronized RunTime reset(Type typ) {
    if (runTime != null) {
      preLogMessages += "RunTime: resetting RunTime instance;";
      Debug.setLogFile(debugLogfileSaved);
      runTime = null;
    }
    return get(typ);
  }

  /**
   * INTERNAL USE get a new initialized RunTime singleton instance
   *
   * @return
   */
  public static synchronized RunTime reset() {
    return reset(Type.API);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="global init">
  private void init(Type typ) {

//<editor-fold defaultstate="collapsed" desc="general">
    if ("winapp".equals(sxOptions.getOption("testing"))) {
      log(lvl, "***** for testing: simulating WinApp");
      testingWinApp = true;
    }
    for (String line : preLogMessages.split(";")) {
      if (!line.isEmpty()) {
        log(lvl, line);
      }
    }
    log(lvl, "global init: entering as: %s", typ);

    sxBuild = SXBuild;
    sxBuildStamp = sxBuild.replace("_", "").replace("-", "").replace(":", "").substring(0, 12);

    if (System.getProperty("user.name") != null) {
      userName = System.getProperty("user.name");
    }
    if (userName.isEmpty()) {
      userName = "unknown";
    }

    String tmpdir = System.getProperty("java.io.tmpdir");
    if (tmpdir != null && !tmpdir.isEmpty()) {
      fTempPath = new File(tmpdir);
    } else {
      terminate(999, "init: java.io.tmpdir not valid (null or empty");
    }
    fBaseTempPath = new File(fTempPath,
            String.format("Sikulix_%d", FileManager.getRandomInt()));
    fpBaseTempPath = fBaseTempPath.getAbsolutePath();
    fBaseTempPath.mkdirs();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        isTerminating = true;
        log(lvl, "final cleanup");
        if (isRunning != null) {
          try {
            isRunningFile.close();
          } catch (IOException ex) {
          }
          isRunning.delete();
        }

        if (shouldCleanDownloads) {
          FileManager.deleteFileOrFolder(fSikulixDownloadsBuild);
        }
        for (File f : fTempPath.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            File aFile = new File(dir, name);
            boolean isObsolete = false;
            long lastTime = aFile.lastModified();
            if (lastTime == 0) {
              return false;
            }
            if (lastTime < ((new Date().getTime()) - 7 * 24 * 60 * 60 * 1000)) {
              isObsolete = true;
            }
            if (name.contains("BridJExtractedLibraries") && isObsolete) {
              return true;
            }
            if (name.toLowerCase().contains("sikuli")) {
              if (name.contains("Sikulix_")) {
                if (isObsolete || aFile.equals(fBaseTempPath)) {
                  return true;
                }
              } else {
                return true;
              }
            }
            return false;
          }
        })) {
          Debug.log(4, "cleanTemp: " + f.getName());
          FileManager.deleteFileOrFolder(f.getAbsolutePath());
        }
      }
    });

    if (Type.IDE.equals(typ) && !runningScripts && !allowMultipleInstances) {
      isRunning = new File(fTempPath, isRunningFilename);
      boolean shouldTerminate = false;
      try {
        isRunning.createNewFile();
        isRunningFile = new FileOutputStream(isRunning);
        if (null == isRunningFile.getChannel().tryLock()) {
          Class<?> classIDE = Class.forName("org.sikuli.ide.SikuliIDE");
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
        terminate(999, appDataMsg, fSikulixAppPath);
      }
      fSikulixExtensions = new File(fSikulixAppPath, "Extensions");
      fSikulixExtensions.mkdir();
      fSikulixDownloadsGeneric = new File(fSikulixAppPath, "SikulixDownloads");
      fSikulixDownloadsGeneric.mkdir();
      fSikulixLib = new File(fSikulixAppPath, "Lib");
      fSikulixSetup = new File(fSikulixAppPath, "SikulixSetup");
      fLibsProvided = new File(fSikulixAppPath, fpSysLibs);
      fLibsLocal = fLibsProvided.getParentFile().getParentFile();
    } catch (Exception ex) {
      terminate(999, appDataMsg + "\n" + ex.toString(), fSikulixAppPath);
    }

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="monitors">
    if (!isHeadless()) {
      log(lvl, "Accessing: GraphicsEnvironment.getLocalGraphicsEnvironment()");
      genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      log(lvl, "Accessing: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()");
      gdevs = genv.getScreenDevices();
      nMonitors = gdevs.length;
      if (nMonitors == 0) {
        terminate(999, "GraphicsEnvironment has no ScreenDevices");
      }
      monitorBounds = new Rectangle[nMonitors];
      rAllMonitors = null;
      Rectangle currentBounds;
      for (int i = 0; i < nMonitors; i++) {
        currentBounds = gdevs[i].getDefaultConfiguration().getBounds();
        if (null != rAllMonitors) {
          rAllMonitors = rAllMonitors.union(currentBounds);
        } else {
          rAllMonitors = currentBounds;
        }
        if (currentBounds.contains(pNull)) {
          if (mainMonitor < 0) {
            mainMonitor = i;
            log(lvl, "ScreenDevice %d has (0,0) --- will be primary Screen(0)", i);
          } else {
            log(lvl, "ScreenDevice %d too contains (0,0)!", i);
          }
        }
        log(lvl, "Monitor %d: (%d, %d) %d x %d", i,
                currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height);
        monitorBounds[i] = currentBounds;
      }
      if (mainMonitor < 0) {
        log(lvl, "No ScreenDevice has (0,0) --- using 0 as primary: %s", monitorBounds[0]);
        mainMonitor = 0;
      }
    } else {
      log(lvl, "running in headless environment");
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="classpath">
/*
    try {
      if (Type.IDE.equals(typ)) {
        clsRef = Class.forName("org.sikuli.ide.SikuliIDE");
      } else if (Type.SETUP.equals(typ)) {
        clsRef = Class.forName("org.sikuli.setup.RunSetup");
      }
    } catch (Exception ex) {
    }
*/
    clsRef = RunTime.class;
    CodeSource codeSrc = clsRef.getProtectionDomain().getCodeSource();
    String base = null;
    if (codeSrc != null && codeSrc.getLocation() != null) {
      base = FileManager.slashify(codeSrc.getLocation().getPath(), false);
    }
    appType = "from a jar";
    if (base != null) {
      fSxBaseJar = new File(base);
      String baseJarName = fSxBaseJar.getName();
      fSxBase = fSxBaseJar.getParentFile();
      log(lvl, "runs as %s in: %s", baseJarName, fSxBase.getAbsolutePath());
      Debug.setWithTimeElapsed();
      if (baseJarName.contains("classes")) {
        runningJar = false;
        fSxProject = fSxBase.getParentFile().getParentFile();
        log(lvl, "not jar - supposing Maven project: %s", fSxProject);
        appType = "in Maven project from classes";
        runningInProject = true;
      } else if ("target".equals(fSxBase.getName())) {
        fSxProject = fSxBase.getParentFile().getParentFile();
        log(lvl, "folder target detected - supposing Maven project: %s", fSxProject);
        appType = "in Maven project from some jar";
        runningInProject = true;
      } else {
        if (runningWindows) {
          if (baseJarName.endsWith(".exe")) {
            runningWinApp = true;
            runningJar = false;
            appType = "as application .exe";
          }
        } else if (runningMac) {
          if (fSxBase.getAbsolutePath().contains("SikuliX.app/Content")) {
            runningMacApp = true;
            appType = "as application .app";
            if (!fSxBase.getAbsolutePath().startsWith("/Applications")) {
              appType += " (not from /Applications folder)";
            }
          }
        }
      }
    } else {
      dumpClassPath();
      terminate(999, String.format("no valid Java context (%s)", clsRef));
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
//</editor-fold>

    if (runningWinApp || testingWinApp) {
      runTime.fpJarLibs += "windows";
      runTime.fpSysLibs = runTime.fpJarLibs.substring(1) + "/libs" + runTime.javaArch;
    }

//TODO RunTime: remove SETUP
/*
    if (Type.SETUP.equals(typ)) {
      fSikulixDownloadsBuild = new File(fSikulixAppPath, "SikulixDownloads_" + sxBuildStamp);
      String[] fpList = fSikulixAppPath.list(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          if (name.contains("SikulixDownloads_")) {
            if (name.contains(sxBuildStamp)) {
              return false;
            }
            return true;
          }
          return false;
        }
      });
      if (fpList.length > 0) {
        log(lvl, "deleting versioned downloads folder in AppPath (%s)", fSikulixDownloadsBuild.getName());
        for (String entry : fpList) {
          //new File(fSikulixAppPath, entry).renameTo(fSikulixDownloadsBuild);
          FileManager.deleteFileOrFolder(new File(fSikulixAppPath, entry));
        }
      }
    }
*/

    runType = typ;
    if (Debug.getDebugLevel() == minLvl) {
      show();
    }
    log(lvl, "global init: leaving");
  }

  class LibsFilter implements FilenameFilter {

    String sAccept = "";

    public LibsFilter(String toAccept) {
      sAccept = toAccept;
    }

    @Override
    public boolean accept(File dir, String name) {
      if (dir.getPath().contains(sAccept)) {
        return true;
      }
      return false;
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Sikulix options handling">
  public int SikuliVersionMajor;
  public int SikuliVersionMinor;
  public int SikuliVersionSub;
  public String SXVersion = "";
  public String SXVersionLong;
  public String SXVersionShort;
  public String SXBuild = "";
  public String SXVersionIDE;
  public String SXVersionAPI;

  public String SXSystemVersion;
  public String SXJavaVersion;

  public String SikuliJythonVersion;
  public String SikuliJythonVersion25 = "2.5.4-rc1";
  public String SikuliJythonMaven;
  public String SikuliJythonMaven25;
  public String SikuliJython;
  public String SikuliJython25;
  public String SikuliJRubyVersion;
  public String SikuliJRuby;
  public String SikuliJRubyMaven;

  public String dlMavenRelease = "https://repo1.maven.org/maven2/";
  public String dlMavenSnapshot = "https://oss.sonatype.org/content/groups/public/";
  public String SikuliRepo;
  public String SikuliLocalRepo = "";
  public String[] ServerList = {};

  public Map<String, String> tessData = new HashMap<String, String>();

  public static final String libOpenCV = Core.NATIVE_LIBRARY_NAME;

  void initSikulixOptions() {
    SikuliRepo = null;
    Properties prop = new Properties();
    String svf = "sikulixversion.txt";
    try {
      InputStream is;
      is = RunTime.class.getClassLoader().getResourceAsStream("Settings/" + svf);
      if (is == null) {
        Debug.error("initSikulixOptions: not found on classpath: %s", "Settings/" + svf);
        Sikulix.endError(999);
      }
      prop.load(is);
      is.close();
      SikuliVersionMajor = Integer.decode(prop.getProperty("sikulixvmaj"));
      SikuliVersionMinor = Integer.decode(prop.getProperty("sikulixvmin"));
      SikuliVersionSub = Integer.decode(prop.getProperty("sikulixvsub"));
      SXBuild = prop.getProperty("sikulixbuild");
      SXVersion = prop.getProperty("sikulixvproject");
    } catch (Exception e) {
      Debug.error("Settings: load version file %s did not work", svf);
      Sikulix.endError(999);
    }

    log(lvl, "version: %s build: %s", SXVersion, SXBuild);

    SXVersionIDE = "SikulixIDE-" + SXVersion;
    SXVersionAPI = "SikulixAPI " + SXVersion;
    SXVersionLong = SXVersion + "-" + SXBuild;
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

    SikuliLocalRepo = FileManager.slashify(prop.getProperty("sikulixlocalrepo"), true);
    SikuliJythonVersion = prop.getProperty("sikulixvjython");
    SikuliJythonMaven = "org/python/jython-standalone/"
            + SikuliJythonVersion + "/jython-standalone-" + SikuliJythonVersion + ".jar";
    SikuliJythonMaven25 = "org/python/jython-standalone/"
            + SikuliJythonVersion25 + "/jython-standalone-" + SikuliJythonVersion25 + ".jar";
    SikuliJython = SikuliLocalRepo + SikuliJythonMaven;
    SikuliJython25 = SikuliLocalRepo + SikuliJythonMaven25;
    SikuliJRubyVersion = prop.getProperty("sikulixvjruby");
    SikuliJRubyMaven = "org/jruby/jruby-complete/"
            + SikuliJRubyVersion + "/jruby-complete-" + SikuliJRubyVersion + ".jar";
    SikuliJRuby = SikuliLocalRepo + SikuliJRubyMaven;

    SXSystemVersion = osn + System.getProperty("os.version");
    SXJavaVersion = "Java" + javaVersion + "(" + javaArch + ")" + jreVersion;
//    tessData.put("eng", "http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz");
    tessData.put("eng", "http://download.sikulix.com/tesseract-ocr-3.02.eng.tar.gz");
    Env.setSikuliVersion(SXVersion);
  }

  String getOption(String oName) {
    return sxOptions.getOption(oName);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="libs export">
  public File fLibsFolderStatic = null;

  public boolean makeFolders() {
    boolean newLibsFolder = false;
    fLibsFolderStatic = new File(fSikulixAppPath, "SikulixLibs");
//    fLibsFolder = new File(fSikulixAppPath, "SikulixLibs_" + sxBuildStamp);
    fLibsFolder = fLibsFolderStatic;
    if (!fLibsFolder.exists()) {
      fLibsFolder.mkdirs();
      if (!fLibsFolder.exists()) {
        terminate(999, "libs folder not available: " + fLibsFolder.toString());
      }
      log(lvl, "new libs folder at: %s", fLibsFolder);
      newLibsFolder = true;
    } else {
      log(lvl, "exists libs folder at: %s", fLibsFolder);
    }
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
      log(lvl, "deleting obsolete libs folders in Temp");
      for (String entry : fpList) {
        if (entry.endsWith(sxBuildStamp)) {
          continue;
        }
        FileManager.deleteFileOrFolder(new File(fTempPath, entry));
      }
    }
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
      log(lvl, "deleting obsolete libs folders in AppPath");
      for (String entry : fpList) {
        FileManager.deleteFileOrFolder(new File(fSikulixAppPath, entry));
      }
    }
    return newLibsFolder;
  }

  private boolean libsLoad(String libName) {
    if (!areLibsExported) {
      libsExport();
    }
    if (!areLibsExported) {
      terminate(999, "loadLib: deferred exporting of libs did not work");
    }
    File fLibsFolderUsed = fLibsFolder;
    if (runningWindows) {
      libName += ".dll";
    } else if (runningMac) {
      libName = "lib" + libName + ".dylib";
    } else if (runningLinux) {
      libName = "lib" + libName + ".so";
    }
    String msg = "loadLib: %s";
    File fLib = new File(fLibsFolder, libName);
    int level = lvl;
    if (!runningLinux) {
      Boolean vLib = libsLoaded.get(libName);
      if (vLib == null || !fLib.exists()) {
        fLib = new File(fLibsFolderStatic, libName);
        if (!fLib.exists()) {
          terminate(999, String.format("loadlib: %s not in any libs folder", libName));
        } else {
          fLibsFolderUsed = fLibsFolderStatic;
          libsLoaded.put(libName, true);
          vLib = false;
        }
      }
      if (vLib) {
        level++;
        msg += " already loaded";
      }
      if (vLib) {
        log(level, msg, libName);
        return true;
      }
    }
    boolean shouldTerminate = false;
    Error loadError = null;
    while (!shouldTerminate) {
      shouldTerminate = true;
      loadError = null;
      try {
        if (runningLinux && libName.startsWith("libopen")) {
          libName = "opencv_java";
          System.loadLibrary(libName);
        } else {
          System.load(new File(fLibsFolderUsed, libName).getAbsolutePath());
        }
      } catch (Error e) {
        loadError = e;
        if (runningLinux) {
          log(-1, msg + " not usable: \n%s", libName, loadError);
          terminate(999, "problem with native library: " + libName);
        }
      }
    }
    if (loadError != null) {
      log(-1, "Problematic lib: %s (...TEMP...)", fLib);
      log(-1, "%s loaded, but it might be a problem with needed dependent libraries\nERROR: %s",
              libName, loadError.getMessage().replace(fLib.getAbsolutePath(), "...TEMP..."));
      if (Settings.runningSetup) {
        return false;
      } else {
        terminate(999, "problem with native library: " + libName);
      }
    }
    libsLoaded.put(libName, true);
    log(level, msg, libName);
    return true;
  }

  private void libsExport() {
    boolean newLibsFolder = makeFolders();
    String sysChar = runningOn.toString().substring(0, 1);
    URL urlLibsLocation = clsRefAPI.getResource(fpJarLibs);
    String protocol = urlLibsLocation.getProtocol();
    String jarPath = "";
    String jarFolder = "";
    if ("jar".equals(protocol)) {
      jarPath = urlLibsLocation.getPath()
              .replace("file:", "")
              .replaceAll("%20", " ");
      String[] parts = jarPath.split("!");
      jarFolder = "/";
      if (parts.length > 1) {
        jarPath = parts[0];
        jarFolder = parts[1];
      }
    } else if ("file" != protocol){
      terminate(999, "export libs invalid: %s", urlLibsLocation);
    }
    String resourceList = runTime.resourceListAsString(fpJarLibs, null);
    Matcher matcher = Pattern.compile("1\\..*?MadeForSikuliX.*?txt").matcher(resourceList);
    String libVersion = "";
    if (matcher.find()) {
      libVersion = matcher.group();
    }
    if (libVersion.isEmpty()) {
      shouldExport = true;
    } else if (!new File(fLibsFolder, libVersion).exists()) {
      log(lvl, "libs folder empty or has wrong content");
      shouldExport = true;
    }
    if (shouldExport) {
      if (!newLibsFolder) {
        FileManager.deleteFileOrFolder(fLibsFolder);
        fLibsFolder.mkdir();
        if (!fLibsFolder.exists()) {
          terminate(999, "libs folder not available: " + fLibsFolder.toString());
        }
      }
      log(lvl, "export libs from: %s", urlLibsLocation);
      if ("file".equals(protocol)) {
        //log(lvl, "file: %s", urlLibsLocation.getPath());
        extractResourcesToFolder(urlLibsLocation.getPath(), fLibsFolder, null);
      } else if ("jar".equals(protocol)) {
        log(lvl, "jar: %s at %s", jarPath, jarFolder);
        extractResourcesToFolderFromJar(jarPath, jarFolder, fLibsFolder, null);
      }
    }
    shouldExport = false;
    for (String aFile : fLibsFolder.list()) {
      libsLoaded.put(aFile, false);
    }
    if (useLibsProvided) {
      log(lvl, "Linux: requested to use provided libs - copying");
      LinuxSupport.copyProvidedLibs(fLibsFolder);
    }
    if (runningWindows) {
      addToWindowsSystemPath(fLibsFolder);
      //TODO: Windows: Java Classloader::usr_paths needed for libs access?
      if (!checkJavaUsrPath(fLibsFolder)) {
        log(-1, "Problems setting up on Windows - see errors - might not work and crash later");
      }
      String lib = "jawt.dll";
      File fJawtDll = new File(fLibsFolder, lib);
      FileManager.deleteFileOrFolder(fJawtDll);
      FileManager.xcopy(new File(javahome + "/bin/" + lib), fJawtDll);
      if (!fJawtDll.exists()) {
        terminate(999, "problem copying %s", fJawtDll);
      }
    }
    areLibsExported = true;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="native libs handling">

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
    String syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
    if (syspath == null) {
      terminate(999, "addToWindowsSystemPath: cannot access system path");
    } else {
      String libsPath = (fLibsFolder.getAbsolutePath()).replaceAll("/", "\\");
      if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
        if (SysJNA.WinKernel32.setEnvironmentVariable("PATH", libsPath + ";" + syspath)) {
          syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
          if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
            log(-1, "addToWindowsSystemPath: adding to system path did not work:\n%s", syspath);
            terminate(999, "addToWindowsSystemPath: did not work - see error");
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

  //<editor-fold defaultstate="collapsed" desc="init for IDE">
  File isRunning = null;
  FileOutputStream isRunningFile = null;
  String isRunningFilename = "s_i_k_u_l_i-ide-isrunning";

  private void initIDEbefore() {
    log(lvl, "initIDEbefore: entering");

    Settings.isRunningIDE = true;

    log(lvl, "initIDEbefore: leaving");
  }

  private void initIDEafter() {
    log(lvl, "initIDEafter: entering");
    log(lvl, "initIDEafter: leaving");
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="init for API">
  private void initAPI() {
    log(lvl, "initAPI: entering");
    if (shouldExport
            || !fSikulixLib.exists()
            || !new File(fSikulixLib, "robot").exists()
            || !new File(fSikulixLib, "sikuli").exists()) {
      fSikulixLib.mkdir();
      extractResourcesToFolder("Lib", fSikulixLib, null);
    } else {
      extractResourcesToFolder("Lib/sikuli", new File(fSikulixLib, "sikuli"), null);
    }
    log(lvl, "initAPI: leaving");
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="helpers">
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

  public static void resetProject() {
    scriptProject = null;
    uScriptProject = null;
  }

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

  public boolean isOSX10() {
    return osVersion.startsWith("10.1");
  }

  public boolean needsRobotFake() {
    return !Settings.ClickFast && runningMac && isOSX10();
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
    logp("running %dBit(%s) on %s (%s) %s", javaArch, osArch, osName,
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
        JythonHelper.get().showSysPath();
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

  //<editor-fold defaultstate="collapsed" desc="handling resources from classpath">
  protected List<String> extractTessData(File folder) {
    List<String> files = new ArrayList<String>();
    String tessdata = "/sikulixtessdata";
    URL uContentList = clsRef.getResource(tessdata + "/" + fpContent);
    if (uContentList != null) {
      files = doResourceListWithList(tessdata, files, null);
      if (files.size() > 0) {
        files = doExtractToFolderWithList(tessdata, folder, files);
      }
    } else {
      files = extractResourcesToFolder("/sikulixtessdata", folder, null);
    }
    return (files.size() == 0 ? null : files);
  }

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
    List<String> content = null;
    content = resourceList(fpRessources, filter);
    if (content == null) {
      return null;
    }
    if (fFolder == null) {
      return content;
    }
    return doExtractToFolderWithList(fpRessources, fFolder, content);
  }

  private List<String> doExtractToFolderWithList(String fpRessources, File fFolder, List<String> content) {
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
  public List<String> extractResourcesToFolderFromJar(String aJar, String fpRessources, File fFolder, FilenameFilter filter) {
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
      String sFolder = FileManager.normalizeAbsolute(fFolder.getPath(), false);
      if (":".equals(sFolder.substring(2, 3))) {
        sFolder = sFolder.substring(1);
      }
      files.add(sFolder);
      files = doResourceListFolder(new File(sFolder), files, filter);
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
  public String[] resourceListAsSikulixContentFromJar(String aJar, String folder, File targetFolder, FilenameFilter filter) {
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

  private List<String> doResourceListWithList(String folder, List<String> files, FilenameFilter filter) {
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

  private void copy(InputStream in, OutputStream out) throws IOException {
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

  //<editor-fold defaultstate="collapsed" desc="classpath handling">
  private void storeClassPath() {
    if (isJava9()) {
      String separator = File.pathSeparator;
      String cp = System.getProperty("java.class.path");
      classPathList = Arrays.asList(cp.split(separator));
    } else {
      classPath.clear();
      URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      classPath = Arrays.asList(sysLoader.getURLs());
      classPathList.clear();
      for (URL urlPath : classPath) {
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
    if (classPath.isEmpty()) {
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
    if (classPath.isEmpty()) {
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
    File fJarFound = new File(FileManager.normalizeAbsolute(fpJar, false));
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

  //<editor-fold defaultstate="collapsed" desc="system enviroment">

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

  /**
   * checks, whether Java runs with a valid GraphicsEnvironment (usually means real screens connected)
   *
   * @return false if Java thinks it has access to screen(s), true otherwise
   */
  public boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }

  public boolean isMultiMonitor() {
    return nMonitors > 1;
  }

  public Rectangle getMonitor(int n) {
    if (isHeadless()) {
      return new Rectangle(0, 0, 1, 1);
    }
    if (null == monitorBounds) {
      return null;
    }
    n = (n < 0 || n >= nMonitors) ? mainMonitor : n;
    return monitorBounds[n];
  }

  public Rectangle getAllMonitors() {
    if (isHeadless()) {
      return new Rectangle(0, 0, 1, 1);
    }
    return rAllMonitors;
  }

  public Rectangle hasPoint(Point aPoint) {
    if (isHeadless()) {
      return new Rectangle(0, 0, 1, 1);
    }
    for (Rectangle rMon : monitorBounds) {
      if (rMon.contains(aPoint)) {
        return rMon;
      }
    }
    return null;
  }

  public Rectangle hasRectangle(Rectangle aRect) {
    if (isHeadless()) {
      return new Rectangle(0, 0, 1, 1);
    }
    return hasPoint(aRect.getLocation());
  }

  public GraphicsDevice getGraphicsDevice(int id) {
    return gdevs[id];
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="runcmd">

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
          log(lvl, Sikulix.arrayToString(args));
        } else {
          Debug.info("runcmd: " + Sikulix.arrayToString(args));
        }
      }
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

  //<editor-fold defaultstate="collapsed" desc="args handling for scriptrunner">
  private String[] args = new String[0];
  private String[] sargs = new String[0];

  public void setArgs(String[] args, String[] sargs) {
    this.args = args;
    this.sargs = sargs;
  }

  public String[] getSikuliArgs() {
    return sargs;
  }

  public String[] getArgs() {
    return args;
  }

  public void printArgs() {
    String[] xargs = getSikuliArgs();
    if (xargs.length > 0) {
      Debug.log(lvl, "--- Sikuli parameters ---");
      for (int i = 0; i < xargs.length; i++) {
        Debug.log(lvl, "%d: %s", i + 1, xargs[i]);
      }
    }
    xargs = getArgs();
    if (xargs.length > 0) {
      Debug.log(lvl, "--- User parameters ---");
      for (int i = 0; i < xargs.length; i++) {
        Debug.log(lvl, "%d: %s", i + 1, xargs[i]);
      }
    }
  }

//</editor-fold>

}
