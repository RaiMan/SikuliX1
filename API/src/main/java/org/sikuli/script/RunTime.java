/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.util.JythonHelper;
import org.sikuli.util.LinuxSupport;
import org.sikuli.util.SysJNA;

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

  public static File scriptProject = null;
  public static URL uScriptProject = null;
  public static boolean shouldRunServer = false;
  private static boolean isTerminating = false;

  public static void resetProject() {
    scriptProject = null;
    uScriptProject = null;
  }

  public static String appDataMsg = "";

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
    String where = "unknown";
    if (Region.runTime.isJythonReady) {
      where = JythonHelper.get().getCurrentLine();
    }
    log(-1, msg1 + " %s", where);
    log(-1, msg2);
    current.interrupt();
    current.stop();
  }

  //<editor-fold defaultstate="collapsed" desc="logging">
  private final String me = "RunTime%s: ";
  private int lvl = 3;
  private int minLvl = lvl;
  private static String preLogMessages = "";

  public final static String runCmdError = "*****error*****";
  public static String NL = "\n";
  public boolean runningInteractive = false;
  public boolean runningTests = false;
  public String interactiveRunner;
  public File fLibsProvided;
  public File fLibsLocal;
  public boolean useLibsProvided = false;
  private String lastResult = "";
  public boolean shouldCleanDownloads = false;
  public boolean isJythonReady = false;
  private boolean shouldExport = false;

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
    log(-1, " *** terminating: " + message, args);
    System.exit(retval);
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="instance">

  /**
   * INTERNAL USE
   */
  private RunTime() {
  }

  public static synchronized RunTime get(Type typ) {
    return get(typ, null);
  }

  /**
   * INTERNAL USE to initialize the runtime environment for SikuliX<br>
   * for public use: use RunTime.get() to get the existing instance
   *
   * @param typ IDE or API
   * @return the RunTime singleton instance
   */
  public static synchronized RunTime get(Type typ, String[] clArgs) {
    if (runTime == null) {
      runTime = new RunTime();
      int debugLevel = 0;

      if (null != clArgs) {
        debugLevel = checkArgs(clArgs, typ);
        if (Type.IDE.equals(typ)) {
          if (debugLevel == -1) {
            Debug.on(3);
            Debug.log(3, "RunTime: option -d detected --- log goes to SikulixLog.txt");
            Debug.setLogFile("");
            Settings.LogTime = true;
            System.setProperty("sikuli.console", "false");
          } else if (debugLevel == 998) {
            runTime.allowMultipleInstances = true;
          } else if (debugLevel == 999) {
            runTime.runningScripts = true;
          } else if (debugLevel == -3) {
            //if (Type.IDE.equals(typ) && "runserver".equals(opt)) {
            shouldRunServer = true;
          }
        }
      }
      if (Type.API.equals(typ)) {
        Debug.init();
      }

//<editor-fold defaultstate="collapsed" desc="versions">
      String vJava = System.getProperty("java.runtime.version");
      String vVM = System.getProperty("java.vm.version");
      String vClass = System.getProperty("java.class.version");
      String vSysArch = System.getProperty("sikuli.arch");
      Object vSikuliJavaok = System.getProperty("sikuli.javaok");
      if (null == vSysArch) {
        vSysArch = System.getProperty("os.arch");
      } else {
        runTime.log(runTime.lvl, "SystemProperty given: sikuli.arch=%s", vSysArch);
      }
      if (vSysArch != null) {
        if (vSysArch.contains("64")) {
          runTime.javaArch = 64;
        }
      } else {
        runTime.log(runTime.lvl, "Java arch (32 or 64 Bit) not detected nor given - using %d Bit", runTime.javaArch);
      }
      try {
        if (!vJava.startsWith("1")) {
          runTime.javaVersion = 9;
        } else {
          runTime.javaVersion = Integer.parseInt(vJava.substring(2, 3));
        }
        runTime.javaShow = String.format("java %d-%d version %s vm %s class %s arch %s",
                runTime.javaVersion, runTime.javaArch, vJava, vVM, vClass, vSysArch);
      } catch (Exception ex) {
        runTime.log(-1, "Java version not detected (using 7): %s", vJava);
        runTime.javaVersion = 7;
        runTime.javaShow = String.format("java ?7?-%d version %s vm %s class %s arch %s",
                runTime.javaArch, vJava, vVM, vClass, vSysArch);
        runTime.logp(runTime.javaShow);
        runTime.dumpSysProps();
      }

      if (Debug.getDebugLevel() > runTime.minLvl) {
        runTime.dumpSysProps();
      }

      if (!runTime.isJavaOK()) {
        if (null != vSikuliJavaok) {
          runTime.log(-1, "Java version unusual, but should be used (sikuli.javaok given)!");
        } else {
          if (Type.SETUP.equals(typ)) {
            //runTime.log(-1, "***** EXPERIMENTAL: Setup running on Java 9 *****");
          } else {
            //runTime.terminate(-1, "Java version must be 1.7 or 1.8!");
            runTime.log(-1, "***** BE AWARE: Running on Java 9 - Please report problems *****");
          }
        }
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

      String aFolder = System.getProperty("user.home");
      if (aFolder == null || aFolder.isEmpty() || !(runTime.fUserDir = new File(aFolder)).exists()) {
        runTime.terminate(-1, "JavaSystemProperty::user.home not valid");
      }

      aFolder = System.getProperty("user.dir");
      if (aFolder == null || aFolder.isEmpty() || !(runTime.fWorkDir = new File(aFolder)).exists()) {
        runTime.terminate(-1, "JavaSystemProperty::user.dir not valid");
      }

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

      File fDebug = new File(runTime.fSikulixStore, "SikulixDebug.txt");
      if (fDebug.exists()) {
        if (Debug.getDebugLevel() == 0) {
          Debug.setDebugLevel(3);
        }
        Debug.setLogFile(fDebug.getAbsolutePath());
        if (Type.IDE.equals(typ)) {
          System.setProperty("sikuli.console", "false");
        }
        runTime.logp("auto-debugging with level %d into:\n%s", Debug.getDebugLevel(), fDebug);
      }

      runTime.fTestFolder = new File(runTime.fUserDir, "SikulixTest");
      runTime.fTestFile = new File(runTime.fTestFolder, "SikulixTest.txt");

      runTime.loadOptions(typ);
      int dl = runTime.getOptionNumber("Debug.level");
      if (dl > 0 && Debug.getDebugLevel() < 2) {
        Debug.setDebugLevel(dl);
      }
      if (Debug.getDebugLevel() == 2) {
        runTime.dumpOptions();
      }

      if (Type.SETUP.equals(typ) && debugLevel != -2) {
        Debug.setDebugLevel(3);
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
        if (Type.SETUP.equals(typ)) {
          runTime.initSetup();
        }
      }
    }
    if (testingWinApp && !runTime.runningWindows) {
      runTime.terminate(1, "***** for testing winapp: not running on Windows");
    }
    return runTime;
  }

  /**
   * get the initialized RunTime singleton instance
   *
   * @return
   */
  public static synchronized RunTime get() {
    if (runTime == null) {
      return get(Type.API);
    }
    return runTime;
  }

  /**
   * INTERNAL USE get a new initialized RunTime singleton instance
   *
   * @return
   */
  public static synchronized RunTime reset(Type typ) {
    if (runTime != null) {
      preLogMessages += "RunTime: resetting RunTime instance;";
      if (Sikulix.testNumber == 1) {
        Debug.setDebugLevel(debugLevelSaved);
      }
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

  //<editor-fold defaultstate="collapsed" desc="variables">
  public enum Type {

    IDE, API, SETUP, INIT
  }

  private enum theSystem {

    WIN, MAC, LUX, FOO
  }

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
  public String baseJar = "";
  public String userName = "";
  public String fpBaseTempPath = "";

  private Class clsRef = RunTime.class;
  private Class clsRefBase = clsRef;

  private List<URL> classPath = new ArrayList<URL>();
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

  private File fOptions = null;
  private Properties options = null;
  private String fnOptions = "SikulixOptions.txt";
  private String fnPrefs = "SikulixPreferences.txt";

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
  public int javaVersion = 0;
  public String javahome = FileManager.slashify(System.getProperty("java.home"), false);
  public String osName = "NotKnown";
  public String sysName = "NotKnown";
  public String osVersion = "";
  private String appType = null;
  public int debuglevelAPI = -1;
  private boolean runningScripts = false;
  public String linuxDistro = "???LINUX???";
  public String linuxNeededLibs = "";
  public boolean allowMultipleInstances = false;

  //</editor-fold>
  GraphicsEnvironment genv = null;
  GraphicsDevice[] gdevs;
  public Rectangle[] monitorBounds = null;
  Rectangle rAllMonitors;
  int mainMonitor = -1;
  int nMonitors = 0;
  Point pNull = new Point(0, 0);

  //<editor-fold defaultstate="collapsed" desc="global init">
  private void init(Type typ) {

//<editor-fold defaultstate="collapsed" desc="general">
    if ("winapp".equals(getOption("testing"))) {
      log(lvl, "***** for testing: simulating WinApp");
      testingWinApp = true;
    }
    for (String line : preLogMessages.split(";")) {
      if (!line.isEmpty()) {
        log(lvl, line);
      }
    }
    log(lvl, "global init: entering as: %s", typ);

    sxBuild = SikuliVersionBuild;
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
      terminate(1, "init: java.io.tmpdir not valid (null or empty");
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
        terminate(1, appDataMsg, fSikulixAppPath);
      }
      fSikulixExtensions = new File(fSikulixAppPath, "Extensions");
      fSikulixLib = new File(fSikulixAppPath, "Lib");
      fSikulixDownloadsGeneric = new File(fSikulixAppPath, "SikulixDownloads");
      fSikulixSetup = new File(fSikulixAppPath, "SikulixSetup");
      fLibsProvided = new File(fSikulixAppPath, fpSysLibs);
      fLibsLocal = fLibsProvided.getParentFile().getParentFile();
      fSikulixExtensions.mkdir();
      fSikulixDownloadsGeneric.mkdir();
    } catch (Exception ex) {
      terminate(1, appDataMsg + "\n" + ex.toString(), fSikulixAppPath);
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
        terminate(1, "GraphicsEnvironment has no ScreenDevices");
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
    try {
      if (Type.IDE.equals(typ)) {
        clsRef = Class.forName("org.sikuli.ide.SikuliIDE");
      } else if (Type.SETUP.equals(typ)) {
        clsRef = Class.forName("org.sikuli.setup.RunSetup");
      }
    } catch (Exception ex) {
    }
    CodeSource codeSrc = clsRef.getProtectionDomain().getCodeSource();
    String base = null;
    if (codeSrc != null && codeSrc.getLocation() != null) {
      base = FileManager.slashify(codeSrc.getLocation().getPath(), false);
    }
    appType = "from a jar";
    if (base != null) {
      fSxBaseJar = new File(base);
      String jn = fSxBaseJar.getName();
      fSxBase = fSxBaseJar.getParentFile();
      log(lvl, "runs as %s in: %s", jn, fSxBase.getAbsolutePath());
      if (jn.contains("classes")) {
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
          if (jn.endsWith(".exe")) {
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
      terminate(1, "no valid Java context for SikuliX available "
              + "(java.security.CodeSource.getLocation() is null)");
    }
    if (runningInProject) {
      fSxProjectTestScriptsJS = new File(fSxProject, "StuffContainer/testScripts/testJavaScript");
      fSxProjectTestScripts = new File(fSxProject, "StuffContainer/testScripts");
    }

    List<String> items = new ArrayList<String>();
    if (Type.API.equals(typ)) {
      String optJython = getOption("jython");
      if (!optJython.isEmpty()) {
        items.add(optJython);
      }
    }
    if (!Type.SETUP.equals(typ)) {
      String optClasspath = getOption("classpath");
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
//</editor-fold>

    if (runningWinApp || testingWinApp) {
      runTime.fpJarLibs += "windows";
      runTime.fpSysLibs = runTime.fpJarLibs.substring(1) + "/libs" + runTime.javaArch;
    }
    if (!Type.SETUP.equals(typ)) {
      libsExport(typ);
    } else {
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

  //<editor-fold defaultstate="collapsed" desc="libs export">
  public void makeFolders() {
    fLibsFolder = new File(fSikulixAppPath, "SikulixLibs_" + sxBuildStamp);
    if (testing) {
      logp("***** for testing: delete folders SikulixLibs/ and Lib/");
      FileManager.deleteFileOrFolder(fLibsFolder);
      FileManager.deleteFileOrFolder(fSikulixLib);
    }
    if (!fLibsFolder.exists()) {
      fLibsFolder.mkdirs();
      if (!fLibsFolder.exists()) {
        terminate(1, "libs folder not available: " + fLibsFolder.toString());
      }
      log(lvl, "new libs folder at: %s", fLibsFolder);
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
        if (name.contains("SikulixLibs")) {
          return true;
        }
        return false;
      }
    });
    if (fpList.length > 1) {
      log(lvl, "deleting obsolete libs folders in AppPath");
      for (String entry : fpList) {
        if (entry.endsWith(sxBuildStamp)) {
          continue;
        }
        FileManager.deleteFileOrFolder(new File(fSikulixAppPath, entry));
      }
    }
  }

  private boolean libsLoad(String libName) {
    if (!areLibsExported) {
      libsExport(runType);
    }
    if (!areLibsExported) {
      terminate(1, "loadLib: deferred exporting of libs did not work");
    }
    if (runningWindows) {
      libName += ".dll";
    } else if (runningMac) {
      libName = "lib" + libName + ".dylib";
    } else if (runningLinux) {
      libName = "lib" + libName + ".so";
    }
    File fLib = new File(fLibsFolder, libName);
    Boolean vLib = libsLoaded.get(libName);
    if (vLib == null || !fLib.exists()) {
      terminate(1, String.format("loadlib: %s not available in %s", libName, fLibsFolder));
    }
    String msg = "loadLib: %s";
    int level = lvl;
    if (vLib) {
      level++;
      msg += " already loaded";
    }
    if (vLib) {
      log(level, msg, libName);
      return true;
    }
    boolean shouldTerminate = false;
    Error loadError = null;
    while (!shouldTerminate) {
      shouldTerminate = true;
      loadError = null;
      try {
        System.load(new File(fLibsFolder, libName).getAbsolutePath());
      } catch (Error e) {
        loadError = e;
        if (runningLinux) {
          log(-1, msg + " not usable: \n%s", libName, loadError);
          shouldTerminate = !LinuxSupport.checkAllLibs();
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
        terminate(1, "problem with native library: " + libName);
      }
    }
    libsLoaded.put(libName, true);
    log(level, msg, libName);
    return true;
  }

  private boolean libsCheck(File flibsFolder) {
    // 1.1-MadeForSikuliX64M.txt
    String name = String.format("1.1-MadeForSikuliX%d%s.txt", javaArch, runningOn.toString().substring(0, 1));
    if (!new File(flibsFolder, name).exists()) {
      log(lvl, "libs folder empty or has wrong content");
      return false;
    }
    return true;
  }

  private void libsExport(Type typ) {
    shouldExport = false;
    makeFolders();
    URL uLibsFrom = null;
    if (!libsCheck(fLibsFolder)) {
      FileManager.deleteFileOrFolder(fLibsFolder);
      fLibsFolder.mkdirs();
      shouldExport = true;
      if (!fLibsFolder.exists()) {
        terminate(1, "libs folder not available: " + fLibsFolder.toString());
      }
    }
    if (shouldExport) {
      String sysShort = "win";
      boolean shouldAddLibsJar = false;
      if (!runningWinApp && !testingWinApp) {
        sysShort = runningOn.toString().toLowerCase();
      }
      String fpLibsFrom = "";
      if (runningJar) {
        fpLibsFrom = fSxBaseJar.getAbsolutePath();
        if (fpLibsFrom.contains("forsetup")) {
          shouldAddLibsJar = true;
        }
      } else {
        String fSrcFolder = typ.toString();
        if (Type.SETUP.toString().equals(fSrcFolder)) {
          fSrcFolder = "Setup";
        }
        fpLibsFrom = fSxBaseJar.getPath().replace(fSrcFolder, "Libs" + sysShort) + "/";
      }
      if (testing && !runningJar) {
        if (testingWinApp || testSwitch()) {
          logp("***** for testing: exporting from classes");
        } else {
          logp("***** for testing: exporting from jar");
          shouldAddLibsJar = true;
        }
      }
      if (!shouldAddLibsJar &&
              (null != isJarOnClasspath("sikulix.jar") || null != isJarOnClasspath("sikulixapi.jar"))) {
        shouldAddLibsJar = false;
        fpLibsFrom = "";
      }
      if (shouldAddLibsJar) {
        fpLibsFrom = new File(fSxProject,
                String.format("Libs%s/target/sikulixlibs%s-1.1.2.jar", sysShort, sysShort)).getAbsolutePath();
      }
      log(lvl, "now exporting libs");
      if (!fpLibsFrom.isEmpty()) {
        addToClasspath(fpLibsFrom, "RunTime.libsExport " + typ);
      }
      uLibsFrom = clsRef.getResource(fpJarLibs);
      if (testing || uLibsFrom == null) {
        dumpClassPath();
      }
      if (uLibsFrom == null) {
        terminate(1, "libs to export not found on above classpath: " + fpJarLibs);
      }
      log(lvl, "libs to export are at:\n%s", uLibsFrom);
      if (runningWinApp || testingWinApp) {
        String libsAccepted = "libs" + javaArch;
        extractResourcesToFolder(fpJarLibs, fLibsFolder, new LibsFilter(libsAccepted));
        File fCurrentLibs = new File(fLibsFolder, libsAccepted);
        if (FileManager.xcopy(fCurrentLibs, fLibsFolder)) {
          FileManager.deleteFileOrFolder(fCurrentLibs);
        } else {
          terminate(1, "could not create libs folder for Windows --- see log");
        }
      } else {
        extractResourcesToFolder(fpJarLibs, fLibsFolder, null);
      }
    }
    for (String aFile : fLibsFolder.list()) {
      libsLoaded.put(aFile, false);
    }
    if (useLibsProvided) {
      log(lvl, "Linux: requested to use provided libs - copying");
      LinuxSupport.copyProvidedLibs(fLibsFolder);
    }
    if (runningWindows) {
      addToWindowsSystemPath(fLibsFolder);
      if (!checkJavaUsrPath(fLibsFolder)) {
        log(-1, "Problems setting up on Windows - see errors - might not work and crash later");
      }
      String lib = "jawt.dll";
      File fJawtDll = new File(fLibsFolder, lib);
      FileManager.deleteFileOrFolder(fJawtDll);
      FileManager.xcopy(new File(javahome + "/bin/" + lib), fJawtDll);
      if (!fJawtDll.exists()) {
        terminate(1, "problem copying %s", fJawtDll);
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
  public static void loadLibrary(String libname) {
    if (isTerminating) {
      return;
    }
    RunTime.get().libsLoad(libname);
  }

  /**
   * INTERNAL USE: load a native library from the libs folder
   *
   * @param libname name of library without prefix/suffix/ending
   */
  public static boolean loadLibrary(String libname, boolean useLibsProvided) {
    RunTime rt = RunTime.get();
    rt.useLibsProvided = useLibsProvided;
    return rt.libsLoad(libname);
  }

  private void addToWindowsSystemPath(File fLibsFolder) {
    String syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
    if (syspath == null) {
      terminate(1, "addToWindowsSystemPath: cannot access system path");
    } else {
      String libsPath = (fLibsFolder.getAbsolutePath()).replaceAll("/", "\\");
      if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
        if (SysJNA.WinKernel32.setEnvironmentVariable("PATH", libsPath + ";" + syspath)) {
          syspath = SysJNA.WinKernel32.getEnvironmentVariable("PATH");
          if (!syspath.toUpperCase().contains(libsPath.toUpperCase())) {
            log(-1, "addToWindowsSystemPath: adding to system path did not work:\n%s", syspath);
            terminate(1, "addToWindowsSystemPath: did not work - see error");
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
    optionsIDE = Preferences.userNodeForPackage(Sikulix.class);
    if (jreVersion.startsWith("1.6")) {
      String jyversion = "";
      Properties prop = new Properties();
      String fp = "org/python/version.properties";
      InputStream ifp = null;
      try {
        ifp = classLoader.getResourceAsStream(fp);
        if (ifp != null) {
          prop.load(ifp);
          ifp.close();
          jyversion = prop.getProperty("jython.version");
        }
      } catch (IOException ex) {
      }
      if (!jyversion.isEmpty() && !jyversion.startsWith("2.5")) {
        Sikulix.popError(String.format("The bundled Jython %s\n"
                + "cannot be used on Java 6!\n"
                + "Run setup again in this environment.\n"
                + "Click OK to terminate now", jyversion));
        System.exit(1);
      }
    }

    Settings.isRunningIDE = true;

    if (runningMac) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      if (!runningMacApp && !runningInProject) {
        if (!Sikulix.popAsk("This use of SikuliX is not supported\n"
                + "and might lead to misbehavior!\n"
                + "Click YES to continue (you should be sure)\n"
                + "Click NO to terminate and check the situation.")) {
          System.exit(1);
        }
      }
    }

    log(lvl, "initIDEbefore: leaving");
  }

  private void initIDEafter() {
//    log(lvl, "initIDEafter: entering");
//    log(lvl, "initIDEafter: leaving");
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

  //<editor-fold defaultstate="collapsed" desc="init for Setup">
  private void initSetup() {
//    log(lvl, "initSetup: entering");
//    log(lvl, "initSetup: leaving");
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="helpers">

  /**
   * INTERNAL USE: to check whether we are running in compiled classes context
   *
   * @return true if the code source location is a folder ending with classes (Maven convention)
   */
  public boolean isRunningFromJar() {
    return runningJar;
  }

  /**
   * @return return true if Java version gt 7
   */
  public boolean isJava9(String... args) {
    if (javaVersion > 8) {
      if (args.length > 0) {
        log(-1, "*** Java 9: %s", args[0]);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return return true if Java version gt 7
   */
  public boolean isJava8() {
    return javaVersion > 7;
  }

  /**
   * @return return true if Java version gt 6
   */
  public boolean isJavaOK() {
    if (javaVersion < 7 || javaVersion > 8) {
      return false;
    }
    return true;
  }

  public boolean isOSX10() {
    return osVersion.startsWith("10.10.") || osVersion.startsWith("10.11.") || osVersion.startsWith("10.12.");
  }

  public boolean needsRobotFake() {
    return !Settings.ClickFast && runningMac && isOSX10();
  }

  /**
   * print out some basic information about the current runtime environment
   */
  public void show() {
    if (hasOptions()) {
      dumpOptions();
    }
    logp("***** show environment for %s (build %s)", runType, sxBuildStamp);
    logp("user.home: %s", fUserDir);
    logp("user.dir (work dir): %s", fWorkDir);
    logp("user.name: %s", userName);
    logp("java.io.tmpdir: %s", fTempPath);
    logp("running %dBit on %s (%s) %s", javaArch, osName,
            (linuxDistro.contains("???") ? osVersion : linuxDistro), appType);
    logp(javaShow);
    logp("app data folder: %s", fSikulixAppPath);
    logp("libs folder: %s", fLibsFolder);
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

  public String getVersionShortBasic() {
    return sversion.substring(0, 3);
  }

  public String getVersionShort() {
    if (SikuliVersionBetaN > 0 && SikuliVersionBetaN < 99) {
      return bversion;
    } else {
      return sversion;
    }
  }

  public String getSystemInfo() {
    return String.format("%s/%s/%s", SikuliVersionLong, SikuliSystemVersion, SikuliJavaVersion);
  }

  public boolean isVersionRelease() {
    return SikuliVersionType.isEmpty();
  }

  public String getVersion() {
    return SikuliVersion;
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

  //<editor-fold defaultstate="collapsed" desc="internal options handling">
  private void loadOptions(Type typ) {
    for (File aFile : new File[]{fWorkDir, fUserDir, fSikulixStore}) {
      log(lvl, "loadOptions: check: %s", aFile);
      fOptions = new File(aFile, fnOptions);
      if (fOptions.exists()) {
        break;
      } else {
        fOptions = null;
      }
    }
    if (fOptions != null) {
      options = new Properties();
      try {
        InputStream is;
        is = new FileInputStream(fOptions);
        options.load(is);
        is.close();
      } catch (Exception ex) {
        log(-1, "while checking Options file:\n%s", fOptions);
        fOptions = null;
        options = null;
      }
      testing = isOption("testing", false);
      if (testing) {
        Debug.setDebugLevel(3);
      }
      log(lvl, "found Options file at: %s", fOptions);
    }
    if (hasOptions()) {
      for (Object oKey : options.keySet()) {
        String sKey = (String) oKey;
        String[] parts = sKey.split("\\.");
        if (parts.length == 1) {
          continue;
        }
        String sClass = parts[0];
        String sAttr = parts[1];
        Class cClass = null;
        Field cField = null;
        Class ccField = null;
        if (sClass.contains("Settings")) {
          try {
            cClass = Class.forName("org.sikuli.basics.Settings");
            cField = cClass.getField(sAttr);
            ccField = cField.getType();
            if (ccField.getName() == "boolean") {
              cField.setBoolean(null, isOption(sKey));
            } else if (ccField.getName() == "int") {
              cField.setInt(null, getOptionNumber(sKey));
            } else if (ccField.getName() == "float") {
              cField.setInt(null, getOptionNumber(sKey));
            } else if (ccField.getName() == "double") {
              cField.setInt(null, getOptionNumber(sKey));
            } else if (ccField.getName() == "String") {
              cField.set(null, getOption(sKey));
            }
          } catch (Exception ex) {
            log(-1, "loadOptions: not possible: %s = %s", sKey, options.getProperty(sKey));
          }
        }
      }
    }
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)
   *
   * @param pName the option key (case-sensitive)
   * @return true only if option exists and has yes or true (not case-sensitive), in all other cases false
   */
  public boolean isOption(String pName) {
    return isOption(pName, false);
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)
   *
   * @param pName    the option key (case-sensitive)
   * @param bDefault the default to be returned if option absent or empty
   * @return true if option has yes or no, false for no or false (not case-sensitive)
   */
  public boolean isOption(String pName, Boolean bDefault) {
    if (options == null) {
      return bDefault;
    }
    String pVal = options.getProperty(pName, bDefault.toString()).toLowerCase();
    if (pVal.isEmpty()) {
      return bDefault;
    } else if (pVal.contains("yes") || pVal.contains("true") || pVal.contains("on")) {
      return true;
    } else if (pVal.contains("no") || pVal.contains("false") || pVal.contains("off")) {
      return false;
    }
    return true;
  }

  /**
   * look into the option file if any (if no option file is found, the option is taken as not existing)
   *
   * @param pName the option key (case-sensitive)
   * @return the associated value, empty string if absent
   */
  public String getOption(String pName) {
    if (options == null) {
      return "";
    }
    String pVal = options.getProperty(pName, "");
    return pVal;
  }

  /**
   * look into the option file if any (if no option file is found, the option is taken as not existing)<br>
   * side-effect: if no options file is there, an options store will be created in memory<br>
   * in this case and when the option is absent or empty, the given default will be stored<br>
   * you might later save the options store to a file with storeOptions()
   *
   * @param pName    the option key (case-sensitive)
   * @param sDefault the default to be returned if option absent or empty
   * @return the associated value, the default value if absent or empty
   */
  public String getOption(String pName, String sDefault) {
    if (options == null) {
      options = new Properties();
      options.setProperty(pName, sDefault);
      return sDefault;
    }
    String pVal = options.getProperty(pName, sDefault);
    if (pVal.isEmpty()) {
      options.setProperty(pName, sDefault);
      return sDefault;
    }
    return pVal;
  }

  /**
   * store an option key-value pair, overwrites existing value<br>
   * new option store is created if necessary and can later be saved to a file
   *
   * @param pName
   * @param sValue
   */
  public void setOption(String pName, String sValue) {
    if (options == null) {
      options = new Properties();
    }
    options.setProperty(pName, sValue);
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)<br>
   * tries to convert the stored string value into an integer number (gives 0 if not possible)<br>
   *
   * @param pName the option key (case-sensitive)
   * @return the converted integer number, 0 if absent or not possible
   */
  public int getOptionNumber(String pName) {
    if (options == null) {
      return 0;
    }
    String pVal = options.getProperty(pName, "0");
    int nVal = 0;
    try {
      nVal = Integer.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * CONVENIENCE: look into the option file if any (if no option file is found, the option is taken as not existing)<br>
   * tries to convert the stored string value into an integer number (gives 0 if not possible)<br>
   *
   * @param pName    the option key (case-sensitive)
   * @param nDefault the default to be returned if option absent, empty or not convertable
   * @return the converted integer number, default if absent, empty or not possible
   */
  public int getOptionNumber(String pName, Integer nDefault) {
    if (options == null) {
      return nDefault;
    }
    String pVal = options.getProperty(pName, nDefault.toString());
    int nVal = nDefault;
    try {
      nVal = Integer.decode(pVal);
    } catch (Exception ex) {
    }
    return nVal;
  }

  /**
   * all options and their values
   *
   * @return a map of key-value pairs containing the found options, empty if no options file found
   */
  public Map<String, String> getOptions() {
    Map<String, String> mapOptions = new HashMap<String, String>();
    if (options != null) {
      Enumeration<?> optionNames = options.propertyNames();
      String optionName;
      while (optionNames.hasMoreElements()) {
        optionName = (String) optionNames.nextElement();
        mapOptions.put(optionName, getOption(optionName));
      }
    }
    return mapOptions;
  }

  /**
   * check whether options are defined
   *
   * @return true if at lest one option defined else false
   */
  public boolean hasOptions() {
    return options != null && options.size() > 0;
  }

  /**
   * all options and their values written to sysout as key = value
   */
  public void dumpOptions() {
    if (hasOptions()) {
      logp("*** options dump:\n%s", (fOptions == null ? "" : fOptions));
      for (String sOpt : getOptions().keySet()) {
        logp("%s = %s", sOpt, getOption(sOpt));
      }
      logp("*** options dump end");
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Sikulix options handling">
  public String sSikulixapi = "sikulixapi.jar";
  public int SikuliVersionMajor;
  public int SikuliVersionMinor;
  public int SikuliVersionSub;
  public int SikuliVersionBetaN;
  public String SikuliProjectVersionUsed = "";
  public String SikuliProjectVersion = "";
  public String SikuliVersionBuild;
  public String SikuliVersionType;
  public String SikuliVersionTypeText;
  public String downloadBaseDirBase;
  public String downloadBaseDirWeb;
  public String downloadBaseDir;
  // used for download of production versions
  private final String dlProdLink = "https://launchpad.net/raiman/sikulix2013+/";
  private final String dlProdLink1 = ".0";
  private final String dlProdLink2 = "/+download/";
  // used for download of development versions (nightly builds)
  private final String dlDevLink = "http://nightly.sikuli.de/";
  public String SikuliRepo;
  public String SikuliLocalRepo = "";
  public String[] ServerList = {};
  private String sversion;
  private String bversion;
  public String SikuliVersionDefault;
  public String SikuliVersionBeta;
  public String SikuliVersionDefaultIDE;
  public String SikuliVersionBetaIDE;
  public String SikuliVersionDefaultScript;
  public String SikuliVersionBetaScript;
  public String SikuliVersion;
  public String SikuliVersionIDE;
  public String SikuliVersionScript;
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

  public Map<String, String> tessData = new HashMap<String, String>();

  //TODO needed ???
  public final String libOpenCV = "libopencv_java248";

  public String SikuliVersionLong;
  public String SikuliSystemVersion;
  public String SikuliJavaVersion;

  private void initSikulixOptions() {
    SikuliRepo = null;
    Properties prop = new Properties();
    String svf = "sikulixversion.txt";
    try {
      InputStream is;
      is = clsRef.getClassLoader().getResourceAsStream("Settings/" + svf);
      if (is == null) {
        terminate(1, "initSikulixOptions: not found on classpath: %s", "Settings/" + svf);
      }
      prop.load(is);
      is.close();
      String svt = prop.getProperty("sikulixdev");
      SikuliVersionMajor = Integer.decode(prop.getProperty("sikulixvmaj"));
      SikuliVersionMinor = Integer.decode(prop.getProperty("sikulixvmin"));
      SikuliVersionSub = Integer.decode(prop.getProperty("sikulixvsub"));
      SikuliVersionBetaN = Integer.decode(prop.getProperty("sikulixbeta"));
      String ssxbeta = "";
      if (SikuliVersionBetaN > 0) {
        ssxbeta = String.format("-Beta%d", SikuliVersionBetaN);
      }
      SikuliVersionBuild = prop.getProperty("sikulixbuild");
      log(lvl + 1, "%s version from %s: %d.%d.%d%s build: %s", svf,
              SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub, ssxbeta,
              SikuliVersionBuild, svt);
      sversion = String.format("%d.%d.%d",
              SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub);
      bversion = String.format("%d.%d.%d-Beta%d",
              SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub, SikuliVersionBetaN);
      SikuliVersionDefault = "SikuliX " + sversion;
      SikuliVersionBeta = "Sikuli " + bversion;
      SikuliVersionDefaultIDE = "SikulixIDE " + sversion;
      SikuliVersionBetaIDE = "SikulixIDE " + bversion;
      SikuliVersionDefaultScript = "SikulixScript " + sversion;
      SikuliVersionBetaScript = "SikulixScript " + bversion;

      SikuliVersionTypeText = "";
      if ("release".equals(svt)) {
        downloadBaseDirBase = dlProdLink;
        downloadBaseDirWeb = downloadBaseDirBase + getVersionShortBasic() + dlProdLink1;
        downloadBaseDir = downloadBaseDirWeb + dlProdLink2;
        SikuliVersionType = "";
      } else {
        downloadBaseDirBase = dlDevLink;
        downloadBaseDirWeb = dlDevLink;
        downloadBaseDir = dlDevLink;
        //TODO switch on for 1.1.2
        //SikuliVersionTypeText = "nightly";
        SikuliVersionBuild += SikuliVersionTypeText;
        SikuliVersionType = svt;
      }
      if (SikuliVersionBetaN > 0) {
        SikuliVersion = SikuliVersionBeta;
        SikuliVersionIDE = SikuliVersionBetaIDE;
        SikuliVersionScript = SikuliVersionBetaScript;
        SikuliVersionLong = bversion + "(" + SikuliVersionBuild + ")";
      } else {
        SikuliVersion = SikuliVersionDefault;
        SikuliVersionIDE = SikuliVersionDefaultIDE;
        SikuliVersionScript = SikuliVersionDefaultScript;
        SikuliVersionLong = sversion + "(" + SikuliVersionBuild + ")";
      }
      SikuliProjectVersionUsed = prop.getProperty("sikulixvused");
      SikuliProjectVersion = prop.getProperty("sikulixvproject");
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

      SikuliSystemVersion = osn + System.getProperty("os.version");
      SikuliJavaVersion = "Java" + javaVersion + "(" + javaArch + ")" + jreVersion;
//TODO this should be in RunSetup only
//TODO debug version: where to do in sikulixapi.jar
//TODO need a function: reveal all environment and system information
//      log(lvl, "%s version: downloading from %s", svt, downloadBaseDir);
    } catch (Exception e) {
      Debug.error("Settings: load version file %s did not work", svf);
      Sikulix.endError(999);
    }
//    tessData.put("eng", "http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz");
    tessData.put("eng", "http://download.sikulix.com/tesseract-ocr-3.02.eng.tar.gz");
    Env.setSikuliVersion(SikuliVersion);
  }

  //</editor-fold>

  //<editor-fold desc="user public options support">

  private static String optThisComingFromFile = "thisOptions.comingFromWhatFile";
  private static String optThisWhatIsANumber = "thisOptions.whatIsAnumber";
  private static String whatIsANumber = "#";

  private static boolean optIsNumber(Properties props, String pName) {
    String prefix = getOpt(props, pName, whatIsANumber);
    if (pName.contains(prefix)) {
      return true;
    }
    return false;
  }

  /**
   * load a properties file
   *
   * @param fpOptions path to a file containing options
   * @return the Properties store or null
   */
  public Properties loadOpts(String fpOptions) {
    if (fpOptions == null) {
      log(-1, "loadOptions: (error: no file)");
      return null;
    }
    File fOptions = new File(fpOptions);
    if (!fOptions.isFile()) {
      log(-1, "loadOptions: (error: not found) %s", fOptions);
      return null;
    }
    Properties pOptions = new Properties();
    try {
      fpOptions = fOptions.getCanonicalPath();
      InputStream is;
      is = new FileInputStream(fOptions);
      pOptions.load(is);
      is.close();
    } catch (Exception ex) {
      log(-1, "loadOptions: %s (error %s)", fOptions, ex.getMessage());
      return null;
    }
    log(lvl, "loadOptions: ok (%d): %s", pOptions.size(), fOptions.getName());
    pOptions.setProperty(optThisComingFromFile, fpOptions);
    return pOptions;
  }

  public static Properties makeOpts() {
    return new Properties();
  }

  /**
   * save a properties store to a file (prop: this.comingfrom = abs. filepath)
   *
   * @param pOptions the prop store
   * @return success
   */
  public boolean saveOpts(Properties pOptions) {
    String fpOptions = pOptions.getProperty(optThisComingFromFile);
    if (null == fpOptions) {
      log(-1, "saveOptions: no prop %s", optThisComingFromFile);
      return false;
    }
    return saveOpts(pOptions, fpOptions);
  }

  /**
   * save a properties store to the given file
   *
   * @param pOptions  the prop store
   * @param fpOptions path to a file
   * @return success
   */
  public boolean saveOpts(Properties pOptions, String fpOptions) {
    pOptions.remove(optThisComingFromFile);
    File fOptions = new File(fpOptions);
    try {
      fpOptions = fOptions.getCanonicalPath();
      OutputStream os;
      os = new FileOutputStream(fOptions);
      pOptions.store(os, "");
      os.close();
    } catch (Exception ex) {
      log(-1, "saveOptions: %s (error %s)", fOptions, ex.getMessage());
      return false;
    }
    log(lvl, "saveOptions: saved: %s", fpOptions);
    return true;
  }

  public static boolean hasOpt(Properties props, String pName) {
    return null != props && null != props.getProperty(pName);
  }

  public static String getOpt(Properties props, String pName) {
    return getOpt(props, pName, "");
  }

  public static String getOpt(Properties props, String pName, String deflt) {
    String retVal = deflt;
    if (hasOpt(props, pName)) {
      retVal = props.getProperty(pName);
    }
    return retVal;
  }

  public static String setOpt(Properties props, String pName, String pVal) {
    String retVal = "";
    if (hasOpt(props, pName)) {
      retVal = props.getProperty(pName);
    }
    props.setProperty(pName, pVal);
    return retVal;
  }

  public static double getOptNum(Properties props, String pName) {
    return getOptNum(props, pName, 0d);
  }

  public static double getOptNum(Properties props, String pName, double deflt) {
    double retVal = deflt;
    if (hasOpt(props, pName)) {
      try {
        retVal = Double.parseDouble(props.getProperty(pName));
      } catch (Exception ex) {
      }
    }
    return retVal;
  }

  public static double setOptNum(Properties props, String pName, double pVal) {
    double retVal = 0d;
    if (hasOpt(props, pName)) {
      try {
        retVal = Double.parseDouble(props.getProperty(pName));
      } catch (Exception ex) {
      }
    }
    props.setProperty(pName, ((Double) pVal).toString());
    return retVal;
  }

  public static String delOpt(Properties props, String pName) {
    String retVal = "";
    if (hasOpt(props, pName)) {
      retVal = props.getProperty(pName);
    }
    props.remove(pName);
    return retVal;
  }

  public static Map<String, String> getOpts(Properties props) {
    Map<String, String> mapOptions = new HashMap<String, String>();
    if (props != null) {
      Enumeration<?> optionNames = props.propertyNames();
      String optionName;
      while (optionNames.hasMoreElements()) {
        optionName = (String) optionNames.nextElement();
        mapOptions.put(optionName, props.getProperty(optionName));
      }
    }
    return mapOptions;
  }

  public static int setOpts(Properties props, Map<String, String> aMap) {
    int n = 0;
    for (String key : aMap.keySet()) {
      props.setProperty(key, aMap.get(key));
      n++;
    }
    return n;
  }

  public static boolean delOpts(Properties props) {
    if (null != props) {
      props.clear();
      return true;
    }
    return false;
  }

  public static int hasOpts(Properties props) {
    if (null != props) {
      return props.size();
    }
    return 0;
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
        logp("%s", uaJar);
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
        throw new IOException("resource not accessible");
      }
      File out = outFile.isEmpty() ? new File(outDir, inFile) : new File(outDir, inFile);
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
    log(lvl, "resourceList: enter");
    List<String> files = new ArrayList<String>();
    if (!folder.startsWith("/")) {
      folder = "/" + folder;
    }
    URL uFolder = resourceLocation(folder);
    if (uFolder == null) {
      log(lvl, "resourceList: not found: %s", folder);
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
    File fFolder = null;
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
      if (!FileManager.pathEquals(fFolder.getPath(), files.get(0))) {
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
    if (isJava9("skipped: storeClassPath()")) {
      return;
    }
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    classPath = Arrays.asList(sysLoader.getURLs());
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
    if (isJava9("skipped: dumpClassPath()")) {
      return;
    }
    logp("*** classpath dump %s", filter);
    storeClassPath();
    String sEntry;
    filter = filter.toUpperCase();
    int n = 0;
    for (URL uEntry : classPath) {
      sEntry = uEntry.getPath();
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
    if (classPath.isEmpty()) {
      storeClassPath();
    }
    for (URL entry : classPath) {
      String sEntry = FileManager.slashify(new File(entry.getPath()).getPath(), false);
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
        cpe = new File(entry.getPath()).getPath();
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
    if (classPath.isEmpty()) {
      storeClassPath();
    }
    for (URL entry : classPath) {
      String sEntry = FileManager.slashify(new File(entry.getPath()).getPath(), false);
      if (sEntry.toUpperCase().contains(artefact)) {
        return entry;
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
    for (URL entry : classPath) {
      if (new File(path.getPath()).equals(new File(entry.getPath()))) {
        return true;
      }
    }
    return false;
  }

  List<String> sxClasspath = new ArrayList<>();

  public boolean addToClasspath(String jarOrFolder) {
    return addToClasspath(jarOrFolder, "");
  }

  public boolean addToClasspath(String jarOrFolder, String caller) {
    if (isJava9("skipped: addToClasspath() - caller: " + caller)) {
      sxClasspath.add(jarOrFolder);
      return false;
    }
    URL uJarOrFolder = FileManager.makeURL(jarOrFolder);
    if (!new File(jarOrFolder).exists()) {
      log(-1, "addToClasspath: does not exist - not added:\n%s", jarOrFolder);
      return false;
    }
    if (isOnClasspath(uJarOrFolder)) {
      return true;
    }
    log(lvl, "addToClasspath:\n%s", uJarOrFolder);
    Method method;
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
    try {
      method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(sysLoader, new Object[]{uJarOrFolder});
    } catch (Exception ex) {
      log(-1, "Did not work: %s", ex.getMessage());
      return false;
    }
    storeClassPath();
    return true;
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
    if (Debug.getDebugLevel() < lvl) {
      return;
    }
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

  public static int checkArgs(String[] args, Type typ) {
    int debugLevel = -99;
    boolean runningScripts = false;
    boolean allowMultipleInstances = false;
    List<String> options = new ArrayList<String>();
    options.addAll(Arrays.asList(args));
    for (int n = 0; n < options.size(); n++) {
      String opt = options.get(n);
      if ("nodebug".equals(opt)) {
        return -2;
      }
      if (Type.IDE.equals(typ) && "-s".equals(opt.toLowerCase())) {
        return -3;
      }
      if (!opt.startsWith("-")) {
        continue;
      }
      if (opt.startsWith("-d")) {
        try {
          debugLevel = n + 1 == options.size() ? -1 : Integer.decode(options.get(n + 1));
        } catch (Exception ex) {
          debugLevel = -1;
        }
        if (debugLevel > -1) {
          Debug.on(debugLevel);
        }
      } else if (opt.startsWith("-r") || opt.startsWith("-t")
              || opt.startsWith("-s") || opt.startsWith("-i")) {
        runningScripts = true;
      } else if (opt.startsWith("-m")) {
        allowMultipleInstances = true;
      }
    }
    if (runningScripts) {
      return 999;
    }
    if (allowMultipleInstances) {
      return 998;
    }
    return debugLevel;
  }
//</editor-fold>

}
