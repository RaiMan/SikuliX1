/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.SplashFrame;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.script.App;
import org.sikuli.script.RunTime;
import org.sikuli.basics.Settings;
//import org.sikuli.script.Sikulix;
import org.sikuli.script.Sikulix;
import org.sikuli.util.LinuxSupport;
import org.sikuli.util.ProcessRunner;

public class RunSetup {

  private static File fDownloadsGeneric = null;

  private static String downloadedFiles;
  private static boolean noSetup = false;
  private static String workDir;
  private static File fWorkDir;
  private static String logfile;
  private static String version;
  //TODO wrong if version number parts have more than one digit
  private static String minorversion;
  private static String majorversion;
  private static String updateVersion;
  private static String downloadIDE;
  private static String downloadAPI;
  private static String downloadLibsMac;
  private static String downloadLibsWin;
  private static String downloadLibsLux;
  private static String downloadRServer;
  private static String downloadJython;
  private static String downloadJython25;
  private static String downloadJRuby;
  private static String downloadJRubyAddOns;
  private static String localAPI = "sikulixapi.jar";
  private static String localIDE = "sikulix.jar";
  private static String localSetup;
  private static String localTess = "sikulixtessdata.jar";
  private static String localJython = "sikulixjython.jar";
  private static String localJRuby = "sikulixjruby.jar";
  private static String localJRubyAddOns = "sikulixjrubyaddons.jar";
  private static String runsikulix = "runsikulix";
  private static String localLogfile;
  private static SetUpSelect winSU;
  private static JFrame winSetup;
  private static boolean getIDE, getJython, getAPI;
  private static boolean getRServer = false;
  private static boolean forAllSystems = false;
  private static boolean getTess = false;
  private static boolean getJRuby = false;
  private static boolean getJRubyAddOns = false;
  private static String localJar;
  private static boolean hasOptions = false;
  private static List<String> options = new ArrayList<String>();
  private static JFrame splash = null;
  private static String me = "RunSetup";
  private static int lvl = 2;
  private static String msg;
  private static boolean shouldPackBundledLibs = true;
  private static long start;
  private static boolean logToFile = true;
  private static boolean forSystemWin = false;
  private static boolean forSystemMac = false;
  private static boolean forSystemLux = false;
  private static String libsMac = "sikulixlibsmac";
  private static String libsWin = "sikulixlibswin";
  private static String libsLux = "sikulixlibslux";
  private static File folderLib;
  private static File folderLibsWin;
  private static File folderLibsLux;
  private static String linuxDistro = "*** testing Linux ***";
  private static String osarch;

  //TODO set true to test on Mac
  private static boolean isLinux = false;

  private static boolean libsProvided = false;
  private static String[] addonFileList = new String[]{null, null, null, null, null};
  private static String[] addonFilePrefix = new String[]{null, null, null, null, null};
  private static int addonVision = 0;
  private static int addonGrabKey = 1;
  private static int addonLibswindows = 2;
  private static int addonFolderLib = 3;
  private static boolean notests = false;
  private static boolean clean = false;
  private static RunTime runTime;
  private static File fDownloadsGenericApp;
  private static boolean useLibsProvided = false;
  private static File fDownloadsObsolete;
  private static boolean runningWithProject = false;
  private static boolean shouldBuildVision = false;
  private static boolean bequiet = false;
  private static String sikulixMavenGroup = "com/sikulix/";
  private static boolean withExtensions = false;

  static Map<String, String> downloadsLookfor = new HashMap<String, String>();
  static Map<String, File> downloadsFound = new HashMap<String, File>();

  private static File fSetupStuff = null;
  private static boolean hasAPI = true;
  private static String[] jarsList = new String[]{
          null, // ide
          null, // api
          null, // tess
          null, // jython
          null, // jruby
          null, // jruby+
          null, // libwin
          null, // libmac
          null // liblux
  };

  private static boolean testingMaven = false;
  private static boolean useLocalMavenRepo = false;

  //<editor-fold defaultstate="collapsed" desc="new logging concept">
  private static void logp(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + message, args);
  }

  private static void logPlus(int level, String message, Object... args) {
    String sout = Debug.logx(level, me + ": " + message, args);
    if (logToFile) {
      System.out.println(sout);
    }
  }
//</editor-fold>

  public static void main(String[] args) throws IOException {

    runTime = RunTime.get(RunTime.Type.SETUP, args);

    version = runTime.getVersionShort();
    minorversion = runTime.getVersionShort().substring(0, 5);
    majorversion = runTime.getVersionShort().substring(0, 3);

    if (args.length > 0 && "test".equals(args[0])) testingMaven = true;

    localSetup = String.format("sikulixsetup-%s-%s-project.jar", version, runTime.sxBuildStamp);
    if (runTime.fSxBaseJar.getPath().contains(localSetup)) {
      runningWithProject = true;
    }

    if (!testingMaven && (runTime.runningInProject || runningWithProject)) {
      runningWithProject = true;
      runTime.shouldCleanDownloads = true;
      downloadIDE = String.format("sikulixsetupIDE-%s-%s.jar", version, runTime.sxBuildStamp);
      downloadAPI = String.format("sikulixsetupAPI-%s-%s.jar", version, runTime.sxBuildStamp);
      downloadLibsMac = String.format("sikulixlibsmac-%s-%s.jar", version, runTime.sxBuildStamp);
      downloadLibsWin = String.format("sikulixlibswin-%s-%s.jar", version, runTime.sxBuildStamp);
      downloadLibsLux = String.format("sikulixlibslux-%s-%s.jar", version, runTime.sxBuildStamp);
    } else {
      localSetup = "sikulixsetup-" + version + ".jar";
      downloadIDE = getMavenJarName("sikulixsetupIDE#forsetup");
      downloadAPI = getMavenJarName("sikulixsetupAPI#forsetup");
      downloadLibsMac = getMavenJarName("sikulixlibsmac");
      downloadLibsWin = getMavenJarName("sikulixlibswin");
      downloadLibsLux = getMavenJarName("sikulixlibslux");
    }

    downloadJython = new File(runTime.SikuliJythonMaven).getName();
    downloadJython25 = new File(runTime.SikuliJythonMaven25).getName();
    downloadJRuby = new File(runTime.SikuliJRubyMaven).getName();

//    CodeSource codeSrc = RunSetup.class.getProtectionDomain().getCodeSource();
//    if (codeSrc != null && codeSrc.getLocation() != null) {
//      codeSrc.getLocation();
//    } else {
//      log(-1, "Fatal Error 201: Not possible to accessjar file for RunSetup.class");
//      Sikulix.terminate(201);
//    }

    if (runTime.SikuliVersionBetaN > 0 && runTime.SikuliVersionBetaN < 99) {
      updateVersion = String.format("%d.%d.%d-Beta%d",
              runTime.SikuliVersionMajor, runTime.SikuliVersionMinor, runTime.SikuliVersionSub,
              1 + runTime.SikuliVersionBetaN);
    } else if (runTime.SikuliVersionBetaN < 1) {
      updateVersion = String.format("%d.%d.%d",
              runTime.SikuliVersionMajor, runTime.SikuliVersionMinor,
              1 + runTime.SikuliVersionSub);
    } else {
      updateVersion = String.format("%d.%d.%d",
              runTime.SikuliVersionMajor, 1 + runTime.SikuliVersionMinor, 0);
    }

    options.addAll(Arrays.asList(args));

    //<editor-fold defaultstate="collapsed" desc="options return version">
    if (args.length > 0 && "stamp".equals(args[0])) {
      System.out.println(runTime.SikuliProjectVersion + "-" + runTime.sxBuildStamp);
      System.exit(0);
    }

    if (args.length > 0 && "frommavenforsed".equals(args[0])) {
      bequiet = true;
      String name = getMavenJarPath(args[2]);
      if (name == null) {
        name = runTime.dlMavenSnapshot + sikulixMavenGroup;
      }
      name = name.replaceAll("/", "\\\\/");
      System.out.println(name);
      System.exit(0);
    }

    if (args.length > 0 && "build".equals(args[0])) {
      System.out.println(runTime.SikuliVersionBuild);
      System.exit(0);
    }

    if (args.length > 0 && "pversion".equals(args[0])) {
      System.out.println(runTime.SikuliProjectVersion);
      System.exit(0);
    }

    if (args.length > 0 && "uversion".equals(args[0])) {
      System.out.println(runTime.SikuliProjectVersionUsed);
      System.exit(0);
    }

    if (args.length > 0 && "version".equals(args[0])) {
      System.out.println(runTime.getVersionShort());
      System.exit(0);
    }

    if (args.length > 0 && "minorversion".equals(args[0])) {
      System.out.println(minorversion);
      System.exit(0);
    }

    if (args.length > 0 && "majorversion".equals(args[0])) {
      System.out.println(majorversion);
      System.exit(0);
    }

    if (args.length > 0 && "updateversion".equals(args[0])) {
      System.out.println(updateVersion);
      System.exit(0);
    }
    //</editor-fold>

    if (options.size() > 0 && "noSetup".equals(options.get(0))) {
      noSetup = true;
      options.remove(0);
    }

    if (testingMaven) {
      if (runTime.isVersionRelease()) {
        useLocalMavenRepo = true;
      }
      options.remove(0);
    }

    boolean makingScriptjar = false;
    if (options.size() > 0 && "scriptjar".equals(options.get(0))) {
      makingScriptjar = true;
      hasOptions = true;
      options.remove(0);
    }

    //TODO add parameter for proxy settings, linux options
    if (options.size() > 0 && "options".equals(options.get(0))) {
      options.remove(0);
      if (!options.isEmpty()) {
        for (String val : options) {
          if (val.contains("1.1")) {
            hasOptions = true;
            getIDE = true;
            getJython = true;
          } else if (val.contains("1.2")) {
            hasOptions = true;
            getIDE = true;
            getJRuby = true;
          } else if (val.contains("1.3")) {
            hasOptions = true;
            getIDE = true;
            getJRuby = true;
            getJRubyAddOns = true;
          } else if ("2".equals(val)) {
            hasOptions = true;
            getAPI = true;
          } else if ("3".equals(val)) {
            hasOptions = true;
            getTess = true;
          } else if ("4".equals(val)) {
            hasOptions = true;
            forAllSystems = true;
          } else if (val.contains("4.1")) {
            hasOptions = true;
            forSystemWin = true;
          } else if (val.contains("4.2")) {
            hasOptions = true;
            forSystemMac = true;
          } else if (val.contains("4.3")) {
            hasOptions = true;
            forSystemLux = true;
          } else if ("5".equals(val)) {
            hasOptions = true;
            getRServer = true;
          } else if (val.toLowerCase().startsWith("lib")) {
            hasOptions = true;
            libsProvided = true;
          } else if (val.toLowerCase().startsWith("buildv")) {
            hasOptions = true;
            shouldBuildVision = true;
          } else if (val.toLowerCase().startsWith("not")) {
            notests = true;
          } else if (val.toLowerCase().startsWith("clean")) {
            clean = true;
          } else if (val.toLowerCase().startsWith("ext")) {
            hasOptions = true;
            withExtensions = true;
          }
        }
        options.clear();
      }
    }

    if (makingScriptjar) {
      String target = FileManager.makeScriptjar(options);
      if (null != target) {
        log(0, "makingScriptJar: ended successfully: %s", target);
        System.exit(0);
      } else {
        log(0, "makingScriptJar: did not work");
        System.exit(1);
      }
    }

    String splashJava9 = "";
    if (runTime.isJava9()) {
      splashJava9 = "*** on Java9 *** ";
    }

    if (!hasOptions && !testingMaven) {
      String msg = String.format("You are about to run a setup for %s (%s)", version, runTime.sxBuildStamp);
      msg += "\n\nYou should have a suitable backup, ";
      msg += "\nto go back in case to what you have now.";
      msg += "\n\n" + splashJava9 + "Click NO to stop here ";
      if (!popAsk(msg)) {
        userTerminated("");
      }
    }

    localLogfile = "SikuliX-" + version + "-SetupLog.txt";

    if (options.size() > 0) {
      if (!"-jar".equals(options.get(0))) {
        System.out.println("invalid command line options - terminating");
        for (String opt : options) {
          System.out.println("option: " + opt);
        }
        System.exit(-1);
      }
      log(lvl, "Seems to be run using mvn exec:exec");
    } else {
      log(lvl, "command line options:");
      String strArgs = "";
      for (String arg : args) {
        strArgs += arg + " ";
      }
      log(lvl, "%s", strArgs);
    }

    //<editor-fold defaultstate="collapsed" desc="general preps">
    Settings.runningSetup = true;
    Settings.LogTime = true;

    runTime.makeFolders();

    fWorkDir = runTime.fSxBase;
    fDownloadsGeneric = runTime.fSikulixDownloadsGeneric;
    fDownloadsGeneric.mkdirs();
    fDownloadsGenericApp = runTime.fSikulixDownloadsBuild;
    fDownloadsGenericApp.mkdirs();
    if (testingMaven || runTime.runningInProject) {
      fWorkDir = runTime.fSikulixSetup;
      fWorkDir.mkdir();
    }
    fDownloadsObsolete = new File(fWorkDir, "Downloads");
    workDir = fWorkDir.getAbsolutePath();

    fSetupStuff = new File(fWorkDir, "SetupStuff");
    FileManager.resetFolder(fSetupStuff);

    osarch = "" + runTime.javaArch;
    if (runTime.runningLinux) {
      linuxDistro = runTime.linuxDistro;
      isLinux = true;
    }

    if (!testingMaven && runTime.runningInProject) {
      if (noSetup) {
        log(lvl, "creating Setup folder - not running setup");
      } else {
        log(lvl, "have to create Setup folder before running setup");
      }

      if (!createSetupFolder(fWorkDir)) {
        log(-1, "createSetupFolder: did not work- terminating");
        System.exit(1);
      }
      if (noSetup) {
        System.exit(0);
      }
      logToFile = false;
    }

    checkDownloads();

//    if (!testingMaven) {
//      logToFile = true;
//    }

    if (logToFile) {
      logfile = (new File(fWorkDir, localLogfile)).getAbsolutePath();
      if (!Debug.setLogFile(logfile)) {
        popError(workDir + "\n... folder we are running in must be user writeable! \n"
                + "please correct the problem and start again.");
        System.exit(0);
      }
    }

    if (args.length > 0) {
      logPlus(lvl, "... starting with: " + arrayToString(args));
    } else {
      logPlus(lvl, "... starting with no args given");
    }

    if (isLinux) {
      logPlus(lvl, "LinuxDistro: %s (%s-Bit)", linuxDistro, osarch);
    }

    logPlus(lvl, "Setup: %s %s in folder:\n%s", runTime.getVersionShort(), runTime.SikuliVersionBuild, fWorkDir);

    File localJarIDE = new File(fWorkDir, localIDE);
    File localJarAPI = new File(fWorkDir, localAPI);

    folderLibsWin = new File(fSetupStuff, "sikulixlibs/windows");
    folderLib = new File(fSetupStuff, "Lib");
    folderLibsLux = runTime.fLibsProvided;

    //TODO Windows 8 HKLM/SOFTWARE/JavaSoft add Prefs ????
    boolean success;
    if (!libsProvided && LinuxSupport.existsLibs()) {
      if (popAsk(String.format("Found a libs folder at\n%s\n"
              + "Click YES to use the contained libs "
              + "for setup (be sure they are useable).\n"
              + "Click NO to make a clean setup (libs are deleted).", folderLibsLux))) {
        useLibsProvided = true;
      } else {
        FileManager.resetFolder(folderLibsLux);
      }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="display setup options">
    String proxyMsg = "";

    if (!hasOptions) {
      getIDE = false;
      getJython = false;
      getAPI = false;
      winSetup = new JFrame("SikuliX-Setup");
      Border rpb = new LineBorder(Color.YELLOW, 8);
      winSetup.getRootPane().setBorder(rpb);
      Container winCP = winSetup.getContentPane();
      winCP.setLayout(new BorderLayout());
      winSU = new SetUpSelect();
      winCP.add(winSU, BorderLayout.CENTER);
      winSU.option2.setSelected(true);
      winSetup.pack();
      winSetup.setLocationRelativeTo(null);
      winSetup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      winSetup.setVisible(true);

      //setup version basic
      winSU.suVersion.setText(runTime.getVersionShort() + "   (" + runTime.SikuliVersionBuild + ")");

      // running system
      Settings.getOS();
      msg = runTime.osName + " " + Settings.getOSVersion();
      if (isLinux) {
        msg += " (" + linuxDistro + ")";
      }
      winSU.suSystem.setText(msg);
      logPlus(lvl, "RunningSystem: " + msg);

      // folder running in
      winSU.suFolder.setText(workDir);
      logPlus(lvl, "parent of jar/classes: %s", workDir);

      // running Java
      String osarch = System.getProperty("os.arch");
      msg = "Java " + Settings.JavaVersion + " (" + Settings.JavaArch + ") " + Settings.JREVersion;
      winSU.suJava.setText(msg);
      logPlus(lvl, "RunningJava: " + msg);

      PreferencesUser prefs = PreferencesUser.getInstance();
      boolean prefsHaveProxy = false;
      String pName = prefs.get("ProxyName", "");
      String pPort = prefs.get("ProxyPort", "");
      if (!pName.isEmpty() && !pPort.isEmpty()) {
        prefsHaveProxy = true;
        winSU.pName.setText(pName);
        winSU.pPort.setText(pPort);
      }

      winSU.addPropertyChangeListener("background", new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
          winSetup.setVisible(false);
        }
      });

      while (true) {
        if (winSU.getBackground() == Color.YELLOW) {
          break;
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
      }

      pName = winSU.pName.getText();
      pPort = winSU.pPort.getText();
      if (!pName.isEmpty() && !pPort.isEmpty()) {
        if (FileManager.setProxy(pName, pPort)) {
          logPlus(lvl, "Requested to run with proxy: %s ", Settings.proxy);
          proxyMsg = "... using proxy: " + Settings.proxy;
        }
      } else if (prefsHaveProxy) {
        prefs.put("ProxyName", "");
        prefs.put("ProxyPort", "");
      }
      Settings.proxyChecked = true;
      //</editor-fold>

      //<editor-fold defaultstate="collapsed" desc="evaluate setup options">
      if (winSU.option1.isSelected()) {
        getIDE = true;
        if (winSU.option2.isSelected()) {
          getJython = true;
        }
        if (winSU.option3.isSelected()) {
          getJRuby = true;
        }
        if (!getJython && !getJRuby) {
          getIDE = false;
        }
      }
      if (winSU.option4.isSelected()) {
        getAPI = true;
      }
      if (winSU.option5.isSelected()) {
        getTess = true;
      }
//        if (winSU.option6.isSelected()) {
//          forAllSystems = true;
//        }
//        if (winSU.option7.isSelected()) {
//          getRServer = true;
//        }

      if (((getTess || forAllSystems) && !(getIDE || getAPI))) {
        popError("You only selected Option 3 !\n"
                + "This is currently not supported.\n"
                + "Please start allover again with valid options.\n");
        terminate("");
      }
      msg = "The following file(s) will be downloaded to\n"
              + workDir + "\n";
    }

    getTess = false; // tessdata completely contained in API-forsetup and IDE-forsetup
    downloadedFiles = "";
    if (getIDE || getAPI || getRServer) {

      if (!proxyMsg.isEmpty()) {
        msg += proxyMsg + "\n";
      }
      if (forAllSystems) {
        msg += "\n--- Native support libraries for all systems (sikulixlibs...)\n";
        downloadedFiles += downloadLibsWin + " ";
        downloadedFiles += downloadLibsMac + " ";
        downloadedFiles += downloadLibsLux + " ";
      } else {
        msg += "\n--- Native support libraries for " + runTime.osName + " (sikulixlibs...)\n";
        if (runTime.runningWindows) {
          downloadedFiles += downloadLibsWin + " ";
        } else if (runTime.runningMac) {
          downloadedFiles += downloadLibsMac + " ";
        } else if (runTime.runningLinux) {
          downloadedFiles += downloadLibsLux + " ";
        }
      }
      if (getIDE) {
        downloadedFiles += downloadIDE + " ";
        downloadedFiles += downloadAPI + " ";
        msg += "\n--- Package 1 ---\n"
                + downloadIDE + " (IDE/Scripting)\n"
                + downloadAPI + " (Java API)";
        if (getJython) {
          downloadedFiles += downloadJython + " ";
          msg += "\n - with Jython";
        }
        if (getJRuby) {
          downloadedFiles += downloadJRuby + " ";
          msg += "\n - with JRuby";
          if (downloadJRubyAddOns != null) {
            if (getJRubyAddOns) {
              downloadedFiles += downloadJRubyAddOns + " ";
              msg += " incl. AddOns";
            }
          } else {
            getJRubyAddOns = false;
          }
        }
        if (Settings.isMac()) {
          msg += "\n - creating Mac application";
        }
        msg += "\n";
      }
      if (getAPI) {
        msg += "\n--- Package 2 ---\n" + downloadAPI;
        if (!getIDE) {
          downloadedFiles += downloadAPI + " ";
          msg += " (Java API)";
        } else {
          msg += " (done in package 1)";
        }
      }
      if (getTess || getRServer) {
        if (getIDE || getAPI) {
          msg += "\n";
        }
        msg += "\n--- Additions ---";
        if (getTess) {
          downloadedFiles += "tessdata-eng" + " ";
          msg += "\n" + "tessdata-eng" + " (Tesseract)";
        }
        if (downloadRServer != null) {
          if (getRServer) {
            downloadedFiles += downloadRServer + " ";
            msg += "\n" + downloadRServer + " (RemoteServer)";
          }
        } else {
          getRServer = false;
        }
      }
    }

    if (getIDE || getAPI || getRServer || withExtensions) {
      msg += "\n\nOnly click NO, if you want to terminate setup now!\n"
              + "Click YES even if you want to use local copies in Downloads!";
      if (!popAsk(msg)) {
        terminate("");
      }
    } else {
      popError("Nothing selected! You might try again ;-)");
      terminate("");
    }
//</editor-fold>

    String localTemp = "sikulixtemp.jar";
    localJar = null;
    File fTargetJar;
    String targetJar;
    boolean downloadOK = true;
    boolean dlOK = true;
    downloadOK = true;
//    String dlDirBuild = fDownloadsBuild.getAbsolutePath();
    String dlDirGenericApp = fDownloadsGenericApp.getAbsolutePath();
    String dlDirGeneric = fDownloadsGeneric.getAbsolutePath();
    String dlDownloads = fDownloadsObsolete.getAbsolutePath();
    boolean shouldUseDownloads = hasOptions && fDownloadsObsolete.exists();
    String dlDir = shouldUseDownloads ? dlDownloads : dlDirGenericApp;

    if (!forSystemWin && !forSystemMac && !forSystemLux) {
      forSystemLux = isLinux;
      if (!isLinux) {
        forSystemWin = Settings.isWindows();
        forSystemMac = Settings.isMac();
      }
    }
    File fDownloaded = null;
    String sDownloaded;
    String sDownloadedName;

    //<editor-fold defaultstate="collapsed" desc="download lib jars">
    if (testingMaven) {
      fDownloaded = downloadJarFromMavenSx("sikulixsetup#forsetup", fWorkDir.getAbsolutePath(), "sikulixsetup");
      if (fDownloaded == null) {
        logPlus(-1, "%s not possible to copy from local MavenRepo");
        downloadOK = false;
      }
      String fpDownloaded = fDownloaded.getAbsolutePath();
      fpDownloaded = fpDownloaded.replace("-forsetup", "");
      fDownloaded.renameTo(new File(fpDownloaded));
    }

    if (getIDE || getAPI) {
      if (forSystemLux || forAllSystems) {
        jarsList[8] = new File(workDir, libsLux + ".jar").getAbsolutePath();
        fDownloaded = downloadedAlready("lux", "Linux native libs", false);
        if (fDownloaded == null) {
          fDownloaded = downloadJarFromMavenSx(libsLux, dlDirGeneric, libsLux);
        }
        downloadOK &= copyFromDownloads(fDownloaded, libsLux, jarsList[8]);
        if (downloadOK && isLinux) {
          runTime.addToClasspath(jarsList[8], "RunSetup: libs for Linux");
          runTime.dumpClassPath("sikulix");
          if (shouldBuildVision) {
            logPlus(lvl, "Requested to build libVisionProxy.so on the fly");
            if (!LinuxSupport.haveToBuild()) {
              terminate("Building libVisionproxy.so not possible - check the log");
            }
          }
          logPlus(lvl, "checking usability of bundled, provided or built libs");
          if (!RunTime.loadLibrary(LinuxSupport.slibVision, useLibsProvided)) {
            logPlus(-1, "libVisionproxy.so finally not useable");
            terminate("Giving up!");
          } else logPlus(lvl, "Bundled, provided or built libVisionproxy.so is useable");
          useLibsProvided = runTime.useLibsProvided || LinuxSupport.shouldUseProvided;
        }
      }

      if (forSystemWin || forAllSystems) {
        jarsList[6] = new File(workDir, libsWin + ".jar").getAbsolutePath();
        fDownloaded = downloadedAlready("win", "Windows native libs", false);
        if (fDownloaded == null) {
          fDownloaded = downloadJarFromMavenSx(libsWin, dlDirGeneric, libsWin);
        }
        boolean dlLibsWinOK = copyFromDownloads(fDownloaded, libsWin, jarsList[6]);
        if (dlLibsWinOK) {
          FileManager.resetFolder(folderLibsWin);
          String aJar = FileManager.normalizeAbsolute(jarsList[6], false);
          if (null == runTime.resourceListAsSikulixContentFromJar(aJar, "sikulixlibs/windows", folderLibsWin, null)) {
            terminate("libswin content list could not be created", 999);
          }
          addonFileList[addonLibswindows] = new File(folderLibsWin, runTime.fpContent).getAbsolutePath();
          addonFilePrefix[addonLibswindows] = libsWin;
        }
        downloadOK &= dlLibsWinOK;
      }

      if (forSystemMac || forAllSystems) {
        jarsList[7] = new File(workDir, libsMac + ".jar").getAbsolutePath();
        fDownloaded = downloadedAlready("mac", "Mac native libs", false);
        if (fDownloaded == null) {
          fDownloaded = downloadJarFromMavenSx(libsMac, dlDirGeneric, libsMac);
        }
        downloadOK &= copyFromDownloads(fDownloaded, libsMac, jarsList[7]);
      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="download IDE/API Jython/JRuby ...">
    if (getAPI) {
      sDownloaded = "sikulixapi";
      localJar = new File(workDir, localAPI).getAbsolutePath();
      fDownloaded = downloadedAlready("api", "Java API package", true);
      if (fDownloaded == null) {
        fDownloaded = downloadJarFromMavenSx("sikulixsetupAPI#forsetup", dlDir, sDownloaded);
      }
      boolean dlApiOK = copyFromDownloads(fDownloaded, sDownloaded, localJar);
/*
      if (dlApiOK && (forSystemWin || forAllSystems)) {
        FileManager.resetFolder(folderLib);
        String aJar = FileManager.normalizeAbsolute(localJar, false);
        if (null == runTime.resourceListAsSikulixContentFromJar(aJar, "Lib", folderLib, null)) {
          terminate("Lib content list could not be created", 999);
        }
        addonFileList[addonFolderLib] = new File(folderLib, runTime.fpContent).getAbsolutePath();
        addonFilePrefix[addonFolderLib] = "Lib";
      }
*/
      downloadOK &= dlApiOK;
    }

    if (getIDE) {
      sDownloaded = "sikulix";
      localJar = new File(workDir, localIDE).getAbsolutePath();
      fDownloaded = downloadedAlready("ide", "SikuliX IDE package", true);
      if (fDownloaded == null) {
        fDownloaded = downloadJarFromMavenSx("sikulixsetupIDE#forsetup", dlDir, sDownloaded);
      }
      downloadOK &= copyFromDownloads(fDownloaded, sDownloaded, localJar);
    }

    if (getJython) {
      sDownloaded = "Jython";
      targetJar = new File(workDir, localJython).getAbsolutePath();
      if (Settings.isJava6()) {
        logPlus(lvl, "running on Java 6: need to use Jython 2.5 - which is downloaded");
        fDownloaded = downloadedAlready("python25", "Jython 2.5", false);
        if (fDownloaded == null) {
          fDownloaded = downloadJarFromMaven(runTime.SikuliJythonMaven25, dlDirGeneric, sDownloaded);
        }
        downloadedFiles.replace(downloadJython, downloadJython25);
      } else {
        if (popAsk("If you click YES, you will get Jython version 2.7.0 (recommended)\n"
                + "... but in rare cases there might be issues with UTF-8/Unicode\n"
                + "that usually appear on startup when UTF-8 characters\n"
                + "are present somewhere in the system environment\n"
                + "If you encounter such problems with Jython 2.7.0\n"
                + "run setup again and\n"
                + "click NO to get Jython a 2.5.4 version")) {
          // TODO: use runtime.SikuliJythonVersion25 in the message above
          sDownloadedName = new File(runTime.SikuliJythonMaven).getName();
          fDownloaded = downloadedAlready("python", "Jython 2.7", false);
          if (fDownloaded == null) {
            fDownloaded = downloadJarFromMaven(runTime.SikuliJythonMaven, dlDirGeneric, sDownloaded);
          }
        } else {
          fDownloaded = downloadedAlready("python25", "Jython 2.5", false);
          if (fDownloaded == null) {
            fDownloaded = downloadJarFromMaven(runTime.SikuliJythonMaven25, dlDirGeneric, sDownloaded);
          }
          downloadedFiles = downloadedFiles.replace(downloadJython, downloadJython25);
        }
      }
      downloadOK &= copyFromDownloads(fDownloaded, sDownloaded, targetJar);
    }

    if (getJRuby) {
      sDownloaded = "JRuby";
      sDownloadedName = new File(runTime.SikuliJRubyMaven).getName();
      targetJar = new File(workDir, localJRuby).getAbsolutePath();
      fDownloaded = downloadsFound.get("ruby");
      if (fDownloaded == null) {
        fDownloaded = downloadJarFromMaven(runTime.SikuliJRubyMaven, dlDirGeneric, sDownloaded);
      }
      downloadOK &= copyFromDownloads(fDownloaded, sDownloaded, targetJar);
      if (downloadOK && getJRubyAddOns) {
        sDownloaded = "JRuby AddOns";
        targetJar = new File(workDir, localJRubyAddOns).getAbsolutePath();
        fDownloaded = downloadsFound.get("rubyaddons");
        fDownloaded = download(runTime.downloadBaseDir, dlDirGeneric, downloadJRubyAddOns, sDownloaded);
        downloadOK &= copyFromDownloads(fDownloaded, sDownloaded, targetJar);
      }
    }

    if (downloadOK && withExtensions) {
      for (String item : runTime.standardExtensions) {
        sDownloaded = item;
        sDownloadedName = item + ".jar";
        fTargetJar = new File(runTime.fSikulixExtensions, sDownloadedName);
        if (!fTargetJar.exists()) {
          fDownloaded = download(runTime.downloadBaseDir, runTime.fSikulixExtensions.getAbsolutePath(), sDownloadedName, item);
          downloadOK &= fDownloaded != null;
        } else {
          logPlus(lvl, "request to download: %s ignored - already there", sDownloadedName);
        }
      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="download for tesseract">
    if (getTess) {
/*
      String langTess = "eng";
      targetJar = new File(workDir, localTess).getAbsolutePath();
      String xTess = runTime.tessData.get(langTess);
      String[] xTessNames = xTess.split("/");
      String xTessName = xTessNames[xTessNames.length - 1];
      String tessFolder = "tessdata-" + langTess;
      File fArchiv = downloadedAlready("tess", "Tesseract tessdata-eng", false);
      if (fArchiv == null) {
        fArchiv = download(xTess, dlDirGeneric, null, tessFolder);
        logPlus(lvl, "downloaded: %s", tessFolder);
      } else {
        logPlus(lvl, "using already downloaded: %s", tessFolder);
      }
      File fTessWork = fArchiv.getParentFile();
      log(lvl, "trying to extract from: %s", xTessName);
      Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
      archiver.extract(fArchiv, fTessWork);
      File fTess = new File(fTessWork, "tesseract-ocr/tessdata");
      if (!fTess.exists()) {
        logPlus(-1, "Download: tessdata: version: eng - did not work");
        downloadOK = false;
      } else {
        File fTessData = new File(fTessWork, tessFolder);
        log(lvl, "preparing the tessdata stuff in:\n%s", fTessData.getAbsolutePath());
        FileManager.resetFolder(fTessData);
        FileManager.xcopy(fTess.getAbsolutePath(), fTessData.getAbsolutePath());
        FileManager.deleteFileOrFolder(fTess.getParent());
        runTime.extractResourcesToFolder("sikulixtessdata", fTessData, null);
        log(lvl, "finally preparing %s", localTess);
        fTargetJar = (new File(workDir, localTemp));
        targetJar = fTargetJar.getAbsolutePath();
        String tessJar = new File(workDir, localTess).getAbsolutePath();

        success = runTime.addToClasspath(fTessData.getParent());
        runTime.resourceListAsSikulixContent(tessFolder, fTessData, null);

        downloadOK &= FileManager.buildJar("#" + targetJar, new String[]{},
                new String[]{fTessData.getAbsolutePath()},
                new String[]{"sikulixtessdata"}, null);
        downloadOK &= handleTempAfter(targetJar, tessJar);

        FileManager.deleteFileOrFolder(fTessData.getAbsolutePath());
      }
*/
    }
    //</editor-fold>

    if (!downloadedFiles.isEmpty()) {
      logPlus(lvl, "Download ended");
      logPlus(lvl, "Downloads for selected options:\n" + downloadedFiles);
    }
    if (!downloadOK) {
      msg = "Some of the downloads did not complete successfully.\n"
              + "Check the logfile for possible error causes.\n\n"
              + "If you think, setup's inline download is blocked somehow on\n"
              + "your system, you might download the appropriate raw packages manually\n"
              + "into the folder Downloads in the setup folder and run setup again.\n\n"
              + "files to download (information is in the setup log file too)\n\n";
      for (String fnToDownload : downloadedFiles.split(" ")) {
        msg += fnToDownload + "\n";
      }
      msg += "\nBe aware: The raw packages are not useable without being processed by setup!\n\n"
              + "For other reasons, you might simply try to run setup again.";
      popError(msg);
      terminate("download not completed successfully", 1);
    }

    //<editor-fold defaultstate="collapsed" desc="create jars and add needed stuff">
    if (!getIDE && !getAPI) {
      logPlus(lvl, "Nothing else to do");
      System.exit(0);
    }

    if (isLinux) {
      if (libsProvided || useLibsProvided) {
        shouldPackBundledLibs = false;
      }
      if (!shouldPackBundledLibs) {
        addonFileList[addonVision] = new File(folderLibsLux, LinuxSupport.libVision).getAbsolutePath();
        addonFileList[addonGrabKey] = new File(folderLibsLux, LinuxSupport.libGrabKey).getAbsolutePath();
        for (int i = 0; i < 2; i++) {
          if (!new File(addonFileList[i]).exists()) {
            addonFileList[i] = null;
          } else {
            logPlus(lvl, "user provided lib: %s", addonFileList[i]);
          }
        }
        String libPrefix = "sikulixlibs/linux/libs" + osarch;
        logPlus(lvl, "libs will be stored in jar at %s", libPrefix);
        addonFilePrefix[addonVision] = libPrefix;
        addonFilePrefix[addonGrabKey] = libPrefix;
      }
    }

    success = true;
    FileManager.JarFileFilter libsFilter = new FileManager.JarFileFilter() {
      @Override
      public boolean accept(ZipEntry entry, String jarname) {
        if (!forAllSystems) {
          if (forSystemWin) {
            if (entry.getName().startsWith("sikulixlibs/mac")
                    || entry.getName().startsWith("sikulixlibs/linux")
                    || entry.getName().endsWith("sikulixfoldercontent")
                    || entry.getName().startsWith("jxgrabkey")) {
              return false;
            }
          } else if (forSystemMac) {
            if (entry.getName().startsWith("sikulixlibs/windows")
                    || entry.getName().startsWith("sikulixlibs/linux")
                    || entry.getName().startsWith("com.melloware.jintellitype")
                    || entry.getName().startsWith("jxgrabkey")) {
              return false;
            }
          } else if (forSystemLux) {
            if (entry.getName().startsWith("sikulixlibs/windows")
                    || entry.getName().startsWith("sikulixlibs/mac")
                    || entry.getName().startsWith("com.melloware.jintellitype")) {
              return false;
            }
          }
        }
        if (forSystemLux || forAllSystems) {
          if (!shouldPackBundledLibs && entry.getName().contains(LinuxSupport.libVision)
                  && entry.getName().contains("libs" + osarch)) {
            if (new File(folderLibsLux, LinuxSupport.libVision).exists()) {
              logPlus(lvl, "Adding provided lib: %s (libs%s)", LinuxSupport.libVision, osarch);
              return false;
            } else {
              return true;
            }
          }
          if (!shouldPackBundledLibs && entry.getName().contains(LinuxSupport.libGrabKey)
                  && entry.getName().contains("libs" + osarch)) {
            if (new File(folderLibsLux, LinuxSupport.libGrabKey).exists()) {
              logPlus(lvl, "Adding provided lib: %s (libs%s)", LinuxSupport.libGrabKey, osarch);
              return false;
            } else {
              return true;
            }
          }
        }
        return true;
      }
    };

    splash = showSplash("Now creating jars, application and commandfiles", "please wait - may take some seconds ...");

    if (getTess) {
      //jarsList[2] = (new File(workDir, localTess)).getAbsolutePath();
    }

    if (success && getAPI) {
      File fAPI = new File(workDir, localAPI);
      jarsList[1] = fAPI.getAbsolutePath();
      hasAPI = fAPI.exists();
      logPlus(lvl, "adding needed stuff to sikulixapi.jar");
      localJar = (new File(workDir, localAPI)).getAbsolutePath();
      targetJar = (new File(workDir, localTemp)).getAbsolutePath();
      success &= FileManager.buildJar("#" + targetJar, jarsList,
              addonFileList, addonFilePrefix, libsFilter);
      success &= handleTempAfter(targetJar, localJar);
      FileManager.deleteFileOrFolder(new File(fDownloadsGeneric, localAPI));
      if (success) {
        FileManager.xcopy(new File(localJar), new File(fDownloadsGeneric, localAPI));
      }
    }

    if (getAPI && getTess) {
//      new File(workDir, localTess).delete();
//      jarsList[2] = null;
    }

    if (success && getIDE) {
      logPlus(lvl, "adding needed stuff to sikulix.jar");
      localJar = (new File(workDir, localIDE)).getAbsolutePath();
      jarsList[0] = localJar;
      jarsList[1] = null;
      if (getJython) {
        jarsList[3] = (new File(workDir, localJython)).getAbsolutePath();
      }
      if (getJRuby) {
        jarsList[4] = (new File(workDir, localJRuby)).getAbsolutePath();
        if (getJRubyAddOns) {
          jarsList[5] = (new File(workDir, localJRubyAddOns)).getAbsolutePath();
        }
      }
      targetJar = (new File(workDir, localTemp)).getAbsolutePath();
      success &= FileManager.buildJar("#" + targetJar, jarsList,
              null, null, libsFilter);
      success &= handleTempAfter(targetJar, localJar);
      FileManager.deleteFileOrFolder(new File(fDownloadsGeneric, localIDE));
      if (success) {
        FileManager.xcopy(new File(localJar), new File(fDownloadsGeneric, localIDE));
      }

      if (Settings.isMac()) {
        logPlus(lvl, "making the Mac application Sikulix.app");
        String macAppContentOrg = "macapp";
        File fMacApp = new File(workDir, "SikuliX.app");
        if (null == runTime.extractResourcesToFolder(macAppContentOrg, fMacApp, null)) {
          log(-1, "did not work");
        } else {
          File fMacAppjar = new File(fMacApp, "Contents/Java/" + localIDE);
          new File(fMacApp, "run").setExecutable(true);
          new File(fMacApp, "Contents/MacOS/JavaAppLauncher").setExecutable(true);
          fMacAppjar.getParentFile().mkdirs();
          FileManager.xcopy(new File(localJar), fMacAppjar);
          FileManager.deleteFileOrFolder(new File(localJar));
          localJarIDE = fMacAppjar;
        }
      }
    }

    if (success && getIDE) {
      logPlus(lvl, "processing commandfiles");
      if (runTime.runningWindows) {
        runTime.extractResourceToFile("Commands/windows", runsikulix + ".cmd", fWorkDir);
      } else if (runTime.runningMac) {
        runTime.extractResourceToFile("Commands/mac", runsikulix, fWorkDir);
        new File(fWorkDir, runsikulix).setExecutable(true);
      } else if (isLinux) {
        runTime.extractResourceToFile("Commands/linux", runsikulix, fWorkDir);
        new File(fWorkDir, runsikulix).setExecutable(true);
        new File(fWorkDir, localIDE).setExecutable(true);
      }
    }

    closeSplash(splash);

    if (!success) {
      popError("Bad things happened trying to add native stuff to selected jars --- terminating!");
      terminate("Adding stuff to jars did not work");
    }
    //</editor-fold>

    if (!notests && runTime.isHeadless()) {
      log(lvl, "Running headless --- skipping tests");
    }

    //<editor-fold defaultstate="collapsed" desc="api test">
    boolean runAPITest = false;
    if (getAPI && !notests && !runTime.isHeadless()) {
      String apiTest = hasOptions ? "testSetupSilent" : "testSetup";
      logPlus(lvl, "Trying to run functional test: JAVA-API %s", splashJava9);
      splash = showSplash("Trying to run functional test(s) - wait for the result popup",
              splashJava9 + " Java-API: org.sikuli.script.Sikulix.testSetup()");
      start += 2000;
      closeSplash(splash);
      if (runTime.isJava9("setup API test - with ProcessRunner")) {
        String result = null;
        try {
          result = ProcessRunner.run("work=" + workDir, "java", "-jar", "?sikulixapi", apiTest);
          if (!result.startsWith("success")) {
            log(-1, "setup API test: did not work\n%s", result);
            throw new Exception("testSetup returned false");
          }
          log(lvl, "setup API test: success");
        } catch (Exception e) {
          terminate("ProcessRunner: " + e.getMessage());
        }
      } else {
        if (!runTime.addToClasspath(localJarAPI.getAbsolutePath())) {
          closeSplash(splash);
          log(-1, "Java-API test: ");
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test JAVA-API did not work", 1);
        }
        try {
          log(lvl, "trying to run org.sikuli.script.Sikulix.testSetup()");
          Class sysclass = URLClassLoader.class;
          Class SikuliCL = sysclass.forName("org.sikuli.script.Sikulix");
          log(lvl, "class found: " + SikuliCL.toString());
          Method method = SikuliCL.getDeclaredMethod(apiTest, new Class[0]);
          log(lvl, "getMethod: " + method.toString());
          method.setAccessible(true);
          closeSplash(splash);
          log(lvl, "invoke: " + method.toString());
          Object ret = method.invoke(null, new Object[0]);
          if (!(Boolean) ret) {
            throw new Exception("testSetup returned false");
          }
        } catch (Exception ex) {
          closeSplash(splash);
          log(-1, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test Java-API did not work", 1);
        }
        runAPITest = true;
      }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ide test">
    if (getIDE && !notests && !runTime.isHeadless()) {
      success = true;
      if (!runAPITest) {
        runTime.makeFolders();
      }
      if (!runTime.isJava9("setup IDE test - addToClasspath() skipped")) {
        if (!runTime.addToClasspath(localJarIDE.getAbsolutePath())) {
          closeSplash(splash);
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test IDE did not work", 1);
        }
      }
      String testMethod;
      if (getJython) {
        if (hasOptions) {
          testMethod = "print('testSetup: Jython: success')";
        } else {
          testMethod = "Sikulix.testSetup('Jython Scripting')";
        }
        logPlus(lvl, "Jython: Trying to run functional test: running script statements via SikuliScript");
        splash = showSplash("Jython Scripting: Trying to run functional test - wait for the result popup",
                splashJava9 + "   Running script statements via SikuliScript");
        start += 2000;
        try {
          String testargs[] = new String[]{"-testSetup", "jython", testMethod};
          closeSplash(splash);
          runScriptTest(testargs);
          if (null == testargs[0]) {
            throw new Exception("testSetup ran with problems");
          }
        } catch (Exception ex) {
          closeSplash(splash);
          success &= false;
          log(-1, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test Jython did not work", 1);
        }
      }
      if (getJRuby && !runTime.isJava9("JRuby test skipped")) {
        if (hasOptions) {
          if (runTime.isJava9()) {
            testMethod = "print \\\"testSetup: JRuby: success\\\"";
          } else {
            testMethod = "print \"testSetup: JRuby: success\"";
          }
        } else {
          if (runTime.isJava9()) {
            testMethod = "Sikulix.testSetup(\\\"JRuby Scripting\\\")";
          } else {
            testMethod = "Sikulix.testSetup(\"JRuby Scripting\")";
          }
        }
        logPlus(lvl, "JRuby: Trying to run functional test: running script statements via SikuliScript");
        splash = showSplash("JRuby Scripting: Trying to run functional test - wait for the result popup",
                splashJava9 + "Running script statements via SikuliScript");
        start += 2000;
        try {
          String testargs[] = new String[]{"-testSetup", "jruby", testMethod};
          closeSplash(splash);
          runScriptTest(testargs);
          if (null == testargs[0]) {
            throw new Exception("testSetup ran with problems");
          }
        } catch (Exception ex) {
          closeSplash(splash);
          success &= false;
          log(-1, "content of returned error's (%s) message:\n%s", ex, ex.getMessage());
          popError("Something serious happened! Sikuli not useable!\n"
                  + "Check the error log at " + (logfile == null ? "printout" : logfile));
          terminate("Functional test JRuby did not work", 1);
        }
      }
      if (success && Settings.isMac()) {
        logPlus(lvl, "MacApp created - should be moved to /Applications");
        splash = showSplash(splashJava9 + "MacApp created",
                "Should be moved to /Applications");
        start += 3000;
        closeSplash(splash);
      }
    }
    //</editor-fold>

    if (!notests) {
      splash = showSplash(splashJava9 + "Setup seems to have ended successfully!",
              "Detailed information see: " + (logfile == null ? "printout" : logfile));
      start += 2000;

      closeSplash(splash);
    }

    logPlus(lvl, "... SikuliX Setup seems to have ended successfully ;-)");

    finalCleanup();

    System.exit(RunTime.testing ? 1 : 0);
  }

  private static String arrayToString(String[] args) {
    String ret = "";
    for (String s : args) {
      if (s.contains(" ")) {
        s = "\"" + s + "\"";
      }
      ret += s + " ";
    }
    return ret;
  }

  private static void finalCleanup() {
    if (hasAPI) jarsList[1] = null;
    for (int i = (getAPI ? 2 : 1); i < jarsList.length; i++) {
      if (jarsList[i] != null) {
        new File(jarsList[i]).delete();
      }
    }
    FileManager.deleteFileOrFolder(fSetupStuff);
  }

  private static void runScriptTest(String[] testargs) {
    if (runTime.isJava9("setup run script test " + testargs[1] + " - with ProcessRunner")) {
      String result = null;
      try {
        String jarSikulix = "sikulix.jar";
        if (runTime.runningMac) {
          jarSikulix = "SikuliX.app/Contents/Java/sikulix.jar";
        }
        result = ProcessRunner.run("work=" + workDir, "java", "-jar", jarSikulix,
                testargs[0], testargs[1], testargs[2]);
        if (!result.startsWith("success")) {
          log(-1, "setup run script test " + testargs[1] + ": did not work\n%s", result);
          testargs[0] = null;
        }
        log(lvl, "setup run script test " + testargs[1] + ": success");
      } catch (Exception e) {
        terminate("ProcessRunner: " + e.getMessage());
      }
    } else {
      try {
        Class scriptRunner = Class.forName("org.sikuli.scriptrunner.ScriptingSupport");
        Method mGetApplication = scriptRunner.getDeclaredMethod("runscript",
                new Class[]{String[].class});
        mGetApplication.invoke(null, new Object[]{testargs});
      } catch (Exception ex) {
        log(lvl, "runScriptTest: error: %s", ex.getMessage());
      }
    }
  }

  private static String addSeps(String item) {
    if (Settings.isWindows()) {
      return item.replace("/", "\\");
    }
    return item;
  }

  private static void checkDownloads() {
    log(lvl, "checkDownloads: workDir:\n%s", fWorkDir);
    log(lvl, "checkDownloads: workDirDownloads:\n%s", fDownloadsObsolete);
    log(lvl, "checkDownloads: downloadsGeneric:\n%s", fDownloadsGeneric);
    log(lvl, "checkDownloads: downloadsGenericApp:\n%s", fDownloadsGenericApp);
    downloadsLookfor.put("api", "sikulixsetupAPI-");
    downloadsFound.put("api", null);
    downloadsLookfor.put("ide", "sikulixsetupIDE-");
    downloadsFound.put("ide", null);
    downloadsLookfor.put("win", "sikulixlibswin-");
    downloadsFound.put("win", null);
    downloadsLookfor.put("mac", "sikulixlibsmac-");
    downloadsFound.put("mac", null);
    downloadsLookfor.put("lux", "sikulixlibslux-");
    downloadsFound.put("lux", null);
    downloadsLookfor.put("python", new File(runTime.SikuliJythonMaven).getName());
    downloadsFound.put("python", null);
    downloadsLookfor.put("python25", new File(runTime.SikuliJythonMaven25).getName());
    downloadsFound.put("python25", null);
    downloadsLookfor.put("ruby", "jruby");
    downloadsFound.put("ruby", null);
    downloadsLookfor.put("rubyaddons", "NotYetDefined");
    downloadsFound.put("rubyaddons", null);
    downloadsLookfor.put("tess", "tesseract");
    downloadsFound.put("tess", null);

    String doubleFiles = "";
    for (File aFolder : new File[]{
            fWorkDir, fDownloadsObsolete, fDownloadsGenericApp, fDownloadsGeneric}) {
      File[] filesContained = aFolder.listFiles(new FilenameFilter() {
        List<String> valid = new ArrayList<>(downloadsLookfor.values());

        @Override
        public boolean accept(File dir, String name) {
          for (String sFile : valid) {
            if (name.startsWith(sFile)) {
              return true;
            }
          }
          return false;
        }
      });
      if (filesContained != null) {
        for (File aFile : filesContained) {
          for (String prefix : downloadsLookfor.keySet()) {
            if (prefix.startsWith("python")) {
              if (downloadsLookfor.get(prefix).equals(aFile.getName())) {
                downloadsFound.put(prefix, aFile);
              }
            } else if (aFile.getName().startsWith(downloadsLookfor.get(prefix))) {
              if (null == downloadsFound.get(prefix)) {
                downloadsFound.put(prefix, aFile);
              } else {
                if (aFile.getParentFile().equals(downloadsFound.get(prefix).getParentFile())) {
                  doubleFiles += aFile + "\n";
                }
              }
            }
          }
        }
      }
    }
    for (String prefix : downloadsFound.keySet()) {
      File fpDownloaded = downloadsFound.get(prefix);
      if (fpDownloaded != null) {
        log(lvl, "checkDownloads: found: %s:\n%s", prefix, fpDownloaded);
      } else {
        log(lvl, "checkDownloads: not found: %s", prefix);
      }
    }
    if (!doubleFiles.isEmpty()) {
      popError("The following files are double or even more often found in the\n"
              + "respective folders setup checks before downloading new artefacts:\n" + doubleFiles +
              "Please check and take care, that only one version of these files is found.\n"
              + "Correct the problem and try again");
      terminate("double downloaded files");
    }
  }

  private static boolean createSetupFolder(File fTargetDir) {
    String projectDir = runTime.fSxProject.getAbsolutePath();
    boolean success = true;

    File fSetup = getProjectJarFile(projectDir, "Setup", "sikulixsetup", "-forsetup.jar");
    success &= fSetup != null;
    File fIDEPlus = getProjectJarFile(projectDir, "SetupIDE", "sikulixsetupIDE", "-forsetup.jar");
    success &= fIDEPlus != null;
    File fAPIPlus = getProjectJarFile(projectDir, "SetupAPI", "sikulixsetupAPI", "-forsetup.jar");
    success &= fAPIPlus != null;
/*
    File fLibsmac = getProjectJarFile(projectDir, "Libsmac", libsMac, ".jar");
    success &= fLibsmac != null;
    File fLibswin = getProjectJarFile(projectDir, "Libswin", libsWin, ".jar");
    success &= fLibswin != null;
    File fLibslux = getProjectJarFile(projectDir, "Libslux", libsLux, ".jar");
    success &= fLibslux != null;
*/

    File fJythonJar = new File(runTime.SikuliJython);
    if (!noSetup && !fJythonJar.exists()) {
      log(lvl, "createSetupFolder: missing: " + fJythonJar.getAbsolutePath());
      success = false;
    }
    File fJythonJar25 = new File(runTime.SikuliJython25);
    if (!noSetup && !fJythonJar25.exists()) {
      log(lvl, "createSetupFolder: missing: " + fJythonJar.getAbsolutePath());
      fJythonJar25 = null;
    }
    File fJrubyJar = new File(runTime.SikuliJRuby);
    if (!noSetup && !fJrubyJar.exists()) {
      log(lvl, "createSetupFolder: missing " + fJrubyJar.getAbsolutePath());
      success = false;
    }

    if (success) {
      FileManager.resetFolder(fDownloadsGenericApp);
      success &= FileManager.xcopy(fSetup, new File(fTargetDir, localSetup));
      if (success) {
        for (String sFile : fTargetDir.list()) {
          if (sFile.contains("sikulixsetup") &&
                  sFile.contains("-project") &&
                  !sFile.contains(localSetup)) {
            FileManager.deleteFileOrFolder(new File(fTargetDir, sFile));
          }
        }
      }
      success &= FileManager.xcopy(fIDEPlus, new File(fDownloadsGenericApp, downloadIDE));
      success &= FileManager.xcopy(fAPIPlus, new File(fDownloadsGenericApp, downloadAPI));

      //for (File fEntry : new File[]{fLibsmac, fLibswin, fLibslux}) {
      //DONT copy libs... jars
      //success &= FileManager.xcopy(fEntry, new File(fDownloadsGenericApp, fEntry.getName()));
      //}

      if (!noSetup) {
        success &= FileManager.xcopy(fJythonJar, new File(fDownloadsGeneric, downloadJython));
        if (fJythonJar25 != null) {
          FileManager.xcopy(fJythonJar25, new File(fDownloadsGeneric, downloadJython25));
        }
        success &= FileManager.xcopy(fJrubyJar, new File(fDownloadsGeneric, downloadJRuby));
      }

//TODO JRubyAddOns
      String jrubyAddons = "sikulixjrubyaddons-" + runTime.SikuliProjectVersion + "-plain.jar";
      File fJRubyAddOns = new File(projectDir, "JRubyAddOns/target/" + jrubyAddons);
//        success &= FileManager.xcopy(fJRubyAddOns, new File(fDownloadsGeneric, downloadJRubyAddOns));

    }
    return success;
  }

  private static File getProjectJarFile(String project, String jarFileDir, String jarFilePre, String jarFileSuf) {
    String jarFileName = getProjectJarFileName(jarFilePre, jarFileSuf);
    File fJarFile = new File(project, jarFileDir + "/target/" + jarFileName);
    if (!fJarFile.exists()) {
      log(-1, "createSetupFolder: missing: " + fJarFile.getAbsolutePath());
      return null;
    } else {
      return fJarFile;
    }
  }

  private static String getProjectJarFileName(String jarFilePre, String jarFileSuf) {
    return String.format("%s-%s%s", jarFilePre, runTime.SikuliProjectVersion, jarFileSuf);
  }

  private static boolean handleTempAfter(String temp, String target) {
    boolean success = true;
    logPlus(lvl, "renaming sikulixtemp.jar to target jar: %s", new File(target).getName());
    FileManager.deleteFileOrFolder("#" + target);
    success &= !new File(target).exists();
    if (success) {
      success &= (new File(temp)).renameTo(new File(target));
      if (!success) {
        logPlus(lvl, "rename did not work --- trying copy");
        try {
          FileManager.xcopy(new File(temp).getAbsolutePath(), target);
          success = new File(target).exists();
          if (success) {
            FileManager.deleteFileOrFolder(new File(temp).getAbsolutePath());
            success = !new File(temp).exists();
          }
        } catch (IOException ex) {
          success &= false;
        }
        if (!success) {
          logPlus(-1, "did not work");
          terminate("");
        }
      }
    }
    return success;
  }

  private static boolean getProxy(String pn, String pp) {
    if (!pn.isEmpty()) {
      Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
      if (p.matcher(pn).matches()) {
        Settings.proxyIP = pn;
      } else {
        Settings.proxyName = pn;
      }
      String msgp = String.format("Requested to use this Proxy: %s (%s)", pn, pp);
      logPlus(lvl, msgp);
      if (pp.isEmpty()) {
        popError(String.format("Proxy specification invalid: %s (%s)", pn, pp));
        logPlus(-1, "Terminating --- Proxy invalid");
        return false;
      } else {
        if (!popAsk(msgp)) {
          logPlus(-1, "Terminating --- User did not accept Proxy: %s %s", pn, pp);
          return false;
        }
      }
      Settings.proxyPort = pp;
      return true;
    }
    return false;
  }

  protected static void helpOption(int option) {
    String m;
    String om = "";
    m = "\n-------------------- Some Information on this option, that might "
            + "help to decide, wether to select it ------------------";
    switch (option) {
      case (1):
        om = "Package 1: You get SikuliX (sikulix.jar) which supports all usages of Sikuli";
//              -------------------------------------------------------------
        m += "\nIt is recommended for people new to Sikuli to get a feeling about the features";
        m += "\n - and those who want to develop Sikuli scripts with the Sikuli IDE";
        m += "\n - and those who want to run Sikuli scripts from commandline.";
        m += "\nDirectly supported scripting languages are Jython and JRuby (you might choose one of them or even both)";
        m += "\n\nFor those who know ;-) additionally you can ...";
        m += "\n- develop Java programs with Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n- develop in any Java aware scripting language adding Sikuli features in IDE's like Eclipse, NetBeans, ...";
        m += "\n\nSpecial INFO for Jython, JRuby and Java developement";
        m += "\nIf you want to use standalone Jython/JRuby or want to develop in Java in parallel,";
        m += "\nyou should select Package 2 additionally (Option 2)";
        m += "\nIn these cases, Package 1 (SikuliX) can be used for image management and for small tests/trials.";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        if (Settings.isMac()) {
          m += "\n\nSpecial info for Mac systems:";
          m += "\nYou will have a SikuliX.app in the setup working folder, that runs the IDE.";
          m += "\nRecommendation: move it to the Applications folder.";
          m += "\nIf you need to run stuff from commandline: use runsikulix";
        }
        break;
      case (2):
        om = "Package 2: To support developement in Java or any Java aware scripting language. you get sikulixapi.jar."
                + "\nYou might want Package 1 (SikuliX) additionally to use the IDE for managing the images or some trials.";
//              -------------------------------------------------------------
        m += "\nThe content of this package is stripped down to what is needed to develop in Java"
                + " or any Java aware scripting language \n(no IDE, no bundled script run support for Jython/JRuby)";
        m += "\n\nHence this package is not runnable and must be in the class path to use it"
                + " for developement or at runtime";
        m += "\n\nSpecial info for usage with Jython/JRuby: It contains the Sikuli Jython/JRuby API ..."
                + "\n... and adds itself to Jython/JRuby path at runtime"
                + "\n... and exports the Sikuli Jython/JRuby modules to the folder Libs at runtime"
                + "\nthat helps to setup the auto-complete in IDE's like NetBeans, Eclipse ...";
        if (Settings.isWindows()) {
          m += "\n\nSpecial info for Windows systems:";
          m += "\nThe generated jars can be used out of the box with Java 32-Bit and Java 64-Bit as well.";
          m += "\nThe Java version is detected at runtime and the native support is switched accordingly.";
        }
        break;
      case (3):
        om = "Tesseract support for language english is bundled";
        m = "";
        break;
      default:
        m = "";
        om = "Not available";
    }
    popInfo("asking for option " + option + ": " + om + "\n" + m);
  }

  private static String packMessage(String msg) {
    msg = msg.replace("\n\n", "\n");
    msg = msg.replace("\n\n", "\n");
    if (msg.startsWith("\n")) {
      msg = msg.substring(1);
    }
    if (msg.endsWith("\n")) {
      msg = msg.substring(0, msg.length() - 1);
    }
    return "--------------------\n" + msg + "\n--------------------";
  }

  private static void popError(String msg) {
    logPlus(3, "\npopError: " + packMessage(msg));
    if (!hasOptions) {
      JOptionPane.showMessageDialog(null, msg, "SikuliX-Setup: having problems ...", JOptionPane.ERROR_MESSAGE);
    }
  }

  private static void popInfo(String msg) {
    logPlus(3, "\npopInfo: " + packMessage(msg));
    if (!hasOptions) {
      JOptionPane.showMessageDialog(null, msg, "SikuliX-Setup: info ...", JOptionPane.PLAIN_MESSAGE);
    }
  }

  private static boolean popAsk(String msg) {
    logPlus(3, "\npopAsk: " + packMessage(msg));
    if (hasOptions) {
      return true;
    }
    int ret = JOptionPane.showConfirmDialog(null, msg, "SikuliX-Setup: question ...", JOptionPane.YES_NO_OPTION);
    if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
      return false;
    }
    return true;
  }

  private static JFrame showSplash(String title, String msg) {
    if (hasOptions | notests) {
      return null;
    }
    start = (new Date()).getTime();
    return new SplashFrame(new String[]{"splash", "# " + title, "#... " + msg});
  }

  private static void closeSplash(JFrame splash) {
    if (splash == null) {
      return;
    }
    long elapsed = (new Date()).getTime() - start;
    if (elapsed < 3000) {
      try {
        Thread.sleep(3000 - elapsed);
      } catch (InterruptedException ex) {
      }
    }
    splash.dispose();
  }

  private static File download(String sDir, String tDir, String item, String itemName) {
    String dlSource;
    if (item == null) {
      String[] items = sDir.split("/");
      item = items[items.length - 1];
      dlSource = sDir;
    } else {
      if (!sDir.endsWith("/")) {
        sDir += "/";
      }
      dlSource = sDir + item;
    }
    if (itemName == null) {
      itemName = item;
    }
    String fname = null;
    if (hasOptions) {
      logPlus(lvl, "SilentSetup: Downloading: %s", itemName);
      fname = FileManager.downloadURL(dlSource, tDir, null);
    } else {
      JFrame progress = new SplashFrame("download");
      fname = FileManager.downloadURL(dlSource, tDir, progress);
      progress.dispose();
    }
    if (null == fname) {
      return null;
    }
    return new File(fname);
  }

  //libDownloaded = takeAlreadyDownloaded(RunFolder, RunFolder/Downloads/ dlDirBuild, dlDirGeneric, libsLux);
  private static File downloadedAlready(String item, String itemName, boolean isVersioned) {
    File targetFolder = isVersioned ? fDownloadsGenericApp : fDownloadsGeneric;
    File artefact = downloadsFound.get(item);
    File target;
    if (artefact != null) {
      target = new File(targetFolder, artefact.getName());
      artefact = downloadedAlreadyAsk(artefact, itemName);
      if (artefact != null && !hasOptions) {
        if (artefact.getParentFile().equals(isVersioned ? fDownloadsGenericApp : fDownloadsGeneric)) {
          return artefact;
        }
        if (FileManager.xcopy(artefact, target)) {
          artefact.delete();
          artefact = target;
        }
      }
    }
    return artefact;
  }

  private static File downloadedAlreadyAsk(File artefact, String itemName) {
    if (artefact.exists()) {
//      if (runningWithProject) {
//        return artefact;
//      }
      if (popAsk("You have for " + itemName + "\n"
              + artefact.getAbsolutePath()
              + "\nClick YES, if you want to use this for setup processing\n\n"
              + "... or click NO, to ignore it and download a fresh copy")) {
        return artefact;
      }
    }
    return null;
  }

  private static boolean copyFromDownloads(File artefact, String item, String jar) {
    if (artefact == null) {
      return false;
    }
    try {
      FileManager.xcopy(artefact.getAbsolutePath(), jar);
    } catch (IOException ex) {
      log(-1, "Unable to copy from Downloads: %s\n%s", artefact, ex.getMessage());
      return false;
    }
    logPlus(lvl, "Copied from Downloads: " + item);
    return true;
  }

  private static String getMavenJarPath(String givenItem) {
    String mPath;
    String mJar = "";
    String itemSuffix = "";
    String item = givenItem;
    if (item.contains("#")) {
      String[] parts = item.split("#");
      item = parts[0];
      itemSuffix = "-" + parts[1];
    }
    String usedVersion = version;
    if (runTime.isVersionRelease() || useReleaseVersion.contains(item)) {
      if (useReleaseVersion.contains(item)) usedVersion = releaseVersion;
      mPath = String.format("%s%s/%s/", sikulixMavenGroup, item, usedVersion);
      mJar = String.format("%s-%s%s.jar", item, usedVersion, itemSuffix);
    } else {
      String dlMavenSnapshotPath = version + "-SNAPSHOT";
      String dlMavenSnapshotXML = "maven-metadata.xml";
      String dlMavenSnapshotPrefix = String.format("%s%s/%s/", sikulixMavenGroup, item, dlMavenSnapshotPath);
      String timeStamp = "";
      String buildNumber = "";
      mPath = runTime.dlMavenSnapshot + dlMavenSnapshotPrefix;
      String xml = mPath + dlMavenSnapshotXML;
      String xmlContent = FileManager.downloadURLtoString(xml);
      if (xmlContent != null && !xmlContent.isEmpty()) {
        Matcher m = Pattern.compile("<timestamp>(.*?)</timestamp>").matcher(xmlContent);
        if (m.find()) {
          timeStamp = m.group(1);
          m = Pattern.compile("<buildNumber>(.*?)</buildNumber>").matcher(xmlContent);
          if (m.find()) {
            buildNumber = m.group(1);
          }
        }
      }
      if (!timeStamp.isEmpty() && !buildNumber.isEmpty()) {
        mJar = String.format("%s-%s-%s-%s%s.jar", item, version, timeStamp, buildNumber, itemSuffix);
        log(lvl, "getMavenJar: %s", mJar);
      } else {
        if (!bequiet) {
          log(-1, "Maven download: could not get timestamp nor buildnumber for %s from:"
                  + "\n%s\nwith content:\n%s", givenItem, xml, xmlContent);
        }
        return null;
      }
    }
    return mPath + mJar;
  }

  private static String getMavenJarName(String item) {
    String fpJar = getMavenJarPath(item);
    if (fpJar == null) {
      return null;
    }
    return new File(fpJar).getName();
  }

  static String useReleaseVersion = "sikulixlibsmac sikulixlibswin sikulixlibslux";
  static String releaseVersion = "1.1.1";

  private static File downloadJarFromMavenSx(String item, String targetDir, String itemName) {
    String fpJar = getMavenJarPath(item);
    if (fpJar == null) {
      return null;
    }
    if (runTime.isVersionRelease() || useReleaseVersion.contains(item)) {
      return downloadJarFromMaven(fpJar, targetDir, itemName);
    } else {
      return download(fpJar, targetDir, null, itemName);
    }
  }

  private static File downloadJarFromMaven(String item, String target, String itemName) {
    if (item.startsWith("http")) {
      return download(item, target, null, itemName);
    } else {
      if (useLocalMavenRepo) {
        try {
          URL theFile = new URL("file", null, runTime.SikuliLocalRepo + item);
          return download(theFile.toExternalForm(), target, null, itemName);
        } catch (MalformedURLException e) {
          return null;
        }
      } else {
        return download(runTime.dlMavenRelease + item, target, null, itemName);
      }
    }
  }

  private static void userTerminated(String msg) {
    if (!msg.isEmpty()) {
      logPlus(lvl, msg);
    }
    logPlus(lvl, "User requested termination.");
    System.exit(0);
  }

  private static void prepTerminate(String msg) {
    logPlus(-1, msg);
    logPlus(-1, "... terminated abnormally :-(");
    popError("Something serious happened! Sikuli not useable!\n"
            + "Check the error log at " + (logfile == null ? "printout" : logfile));
    finalCleanup();
  }

  private static void terminate(String msg) {
    prepTerminate(msg);
    System.exit(0);
  }

  private static void terminate(String msg, int ret) {
    prepTerminate(msg);
    System.exit(ret);
  }
}
