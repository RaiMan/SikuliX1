/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.basics.*;
import org.sikuli.idesupport.IDEDesktopSupport;
import org.sikuli.script.SikuliXception;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runnerSupport.Runner;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.PreferencesUser;
import org.sikuli.script.support.devices.MouseDevice;
import org.sikuli.script.support.devices.ScreenDevice;
import org.sikuli.script.support.gui.SXDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.sikuli.util.CommandArgsEnum.*;

public class Sikulix {

  static SXDialog ideSplash;
  static int waitStart = 0;

  public static void stopSplash() {
    if (waitStart > 0) {
      try {
        Thread.sleep(waitStart * 1000);
      } catch (InterruptedException e) {
      }
    }

    if (ideSplash != null) {
      ideSplash.setVisible(false);
      ideSplash.dispose();
      ideSplash = null;
    }
  }

  public static void main(String[] args) {
    //region startup
    Debug.isIDEstarting(true);
    Commons.setStartClass(Sikulix.class);

    //Commons.addlog("Sikulix::IDEDesktopSupport.initStart()");
    IDEDesktopSupport.initStart();

    //Commons.addlog("Sikulix::Commons.setStartArgs(args)");
    Commons.setStartArgs(args);

    if (Commons.hasStartArg(QUIET)) {
      Debug.setQuiet();
    } else if (Commons.hasStartArg(VERBOSE)) {
      Debug.setVerbose();
    } else if (Commons.hasStartArg(DEBUG)) {
      Debug.setDebugLevel(3);
    }

    if (Commons.hasStartArg(APPDATA)) {
      File path = Commons.setAppDataPath(Commons.getStartArg(APPDATA));
      Commons.setTempFolder(new File(path, "Temp"));
    } else {
      Commons.setTempFolder();
    }

    Commons.initGlobalOptions();

    if (Commons.isSnapshot()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          //https://oss.sonatype.org/content/repositories/snapshots/com/sikulix/sikulixidemac/2.0.6-SNAPSHOT/maven-metadata.xml";
          String ossrhURL = "https://oss.sonatype.org/content/repositories/snapshots/com/sikulix/sikulixide";
          ossrhURL += (Commons.runningWindows() ? "win" : (Commons.runningMac() ? (Commons.runningMacM1() ? "macm1" : "mac") : "lux"));
          ossrhURL += "/" + Commons.getSXVersion() + "/maven-metadata.xml";
          String xml = FileManager.downloadURLtoString("#" + ossrhURL);
          if (!xml.isEmpty()) {
            String xmlParm = "<timestamp>";
            int xmlParmPos = xml.indexOf(xmlParm);
            if (xmlParmPos > -1) {
              int pos = xmlParmPos + xmlParm.length();
              String date = xml.substring(pos, pos + 8);
              Commons.setCurrentSnapshotDate(date);
            }
          }
        }
      }).start();
    }

    if (Commons.hasExtendedArg("reset")) {
      System.out.println("[INFO] IDE: resetting global options and terminating --- see docs");

      PreferencesUser.get().remove("IDE_LOCATION");
      PreferencesUser.get().remove("IDE_SIZE");
      PreferencesUser.get().remove("IDE_SESSION");
      PreferencesUser.get().remove("IDE_SESSION");

      System.exit(0);
    }

    if (Commons.hasStartArg(HELP)) {
      Commons.printHelp();
      Debug.setDebugLevel(3);
      Commons.show();
      Commons.showOptions(Commons.SXPREFS_OPT);
      System.exit(0);
    }

    if (Commons.hasStartArg(LOGFILE)) {
      String logfileName = Commons.getStartArg(LOGFILE);
      Debug.setDebugLogFile(logfileName);
    }

    if (Commons.hasStartArg(USERLOGFILE)) {
      String logfileName = Commons.getStartArg(USERLOGFILE);
      Debug.setUserLogFile(logfileName);
    }

    Commons.checkAccessibility(); //TODO

    //Commons.show(); //TODO

    if (Commons.hasStartArg(RUN)) {
      if (!MouseDevice.isUseable() || !ScreenDevice.isUseable()) {
        //TODO mouse not useable ??? System.exit(1);
      }
      HotkeyManager.getInstance().addHotkey("Abort", new HotkeyListener() {
        @Override
        public void hotkeyPressed(HotkeyEvent e) {
          if (Commons.hasStartArg(RUN)) {
            Runner.abortAll();
            Commons.terminate(254, "AbortKey was pressed: aborting all running scripts");
          }
        }
      });
      String[] scripts = Runner.resolveRelativeFiles(Commons.asArray(Commons.getOption("SX_ARG_RUN")));
      int exitCode = Runner.runScripts(scripts, Commons.getUserArgs(), new IScriptRunner.Options());
      if (exitCode > 255) {
        exitCode = 254;
      }
      Commons.terminate(exitCode, "");
    }

    if (Commons.hasStartArg(RUNSERVER)) {
      //TODO mouse not useable
      if (!MouseDevice.isUseable() || !ScreenDevice.isUseable()) {
        System.exit(1);
      }
      Class cServer = null;
      try {
        cServer = Class.forName("org.sikuli.script.runners.ServerRunner");
        cServer.getMethod("run").invoke(null);
        Commons.terminate();
      } catch (ClassNotFoundException e) {
      } catch (NoSuchMethodException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      }
      try {
        cServer = Class.forName("org.sikuli.script.support.SikulixServer");
        if (!(Boolean) cServer.getMethod("run").invoke(null)) {
          Commons.terminate(1, "SikulixServer: terminated with errors");
        }
      } catch (ClassNotFoundException e) {
      } catch (IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      } catch (NoSuchMethodException e) {
      }
      Commons.terminate();
    }

    if (Commons.hasStartArg(LOAD)) { //TODO
      String sx_arg_load = Commons.getOption("SX_ARG_LOAD");
      String[] scripts = Runner.resolveRelativeFiles(Commons.asArray(sx_arg_load));
      for (String script : scripts) {

      }
    }

    Debug.addlog("IDE starting");

    //TODO mouse not useable
    if (!MouseDevice.isUseable() || !ScreenDevice.isUseable()) {
      //System.exit(1);
    }
    //endregion


    ideSplash = null;
    if (Commons.isRunningFromJar()) {
      if (!Debug.isQuiet()) {
        ideSplash = new SXDialog("sxidestartup", SikulixIDE.getWindowTop(), SXDialog.POSITION.TOP);
        ideSplash.run();
      }
    }

    if (!Commons.hasStartArg(APPDATA)) {
      File isRunning;
      FileOutputStream isRunningFile;
      String isRunningFilename = "s_i_k_u_l_i-ide-isrunning";
      isRunning = new File(Commons.getTempFolder(), isRunningFilename);
      boolean shouldTerminate = false;
      try {
        isRunning.createNewFile();
        isRunningFile = new FileOutputStream(isRunning);
        if (null == isRunningFile.getChannel().tryLock()) {
          Class<?> classIDE = Class.forName("org.sikuli.ide.SikulixIDE");
          Method stopSplash = classIDE.getMethod("stopSplash", new Class[0]);
          stopSplash.invoke(null, new Object[0]);
          org.sikuli.script.Sikulix.popError("Terminating: IDE already running");
          shouldTerminate = true;
        } else {
          Commons.setIsRunning(isRunning, isRunningFile);
        }
      } catch (Exception ex) {
        org.sikuli.script.Sikulix.popError("Terminating on FatalError: cannot access IDE lock for/n" + isRunning);
        shouldTerminate = true;
      }
      if (shouldTerminate) {
        System.exit(1);
      }
      for (String aFile : Commons.getTempFolder().list()) {
        if ((aFile.startsWith("Sikulix"))
            || (aFile.startsWith("jffi") && aFile.endsWith(".tmp"))) {
          FileManager.deleteFileOrFolder(new File(Commons.getTempFolder(), aFile));
        }
      }
    }

    //region IDE temp folder
    File ideTemp = new File(Commons.getTempFolder(), String.format("Sikulix_%d", FileManager.getRandomInt()));
    ideTemp.mkdirs();
    try {
      File tempTest = new File(ideTemp, "tempTest.txt");
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
        throw new SikuliXception(String.format("init: temp folder not useable: %s", Commons.getTempFolder()));
      }
    } catch (Exception e) {
      throw new SikuliXception(String.format("init: temp folder not useable: %s", Commons.getTempFolder()));
    }
    Commons.setIDETemp(ideTemp);
    //endregion

    if (Commons.runningMac()) {
      try {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
      } catch (Exception e) {
      }
    }

//    System.setProperty("python.home", "");
//    System.setProperty("python.import.site", "false");

    //Commons.addlog("Sikulix::SikulixIDE.start(args)");
    SikulixIDE.start(args);

    //region IDE subprocess
    if (false) {
      /*
      if (false) {
        RunTime.terminate(999, "start IDE in subprocess?");
        List<String> cmd = new ArrayList<>();
        System.getProperty("java.home");
        if (Commons.runningWindows()) {
          cmd.add(System.getProperty("java.home") + "\\bin\\java.exe");
        } else {
          cmd.add(System.getProperty("java.home") + "/bin/java");
        }
        if (!Commons.isJava8()) {
      */
//      Suppress Java 9+ warnings
//      --add-opens
//      java.desktop/javax.swing.plaf.basic=ALL-UNNAMED
//      --add-opens
//      java.base/sun.nio.ch=ALL-UNNAMED
//      --add-opens
//      java.base/java.io=ALL-UNNAMED
/*

        IDE start: --add-opens supress warnings
          cmd.add("--add-opens");
          cmd.add("java.desktop/javax.swing.plaf.basic=ALL-UNNAMED");
          cmd.add("--add-opens");
          cmd.add("java.base/sun.nio.ch=ALL-UNNAMED");
          cmd.add("--add-opens");
          cmd.add("java.base/java.io=ALL-UNNAMED");
        }

        cmd.add("-Dfile.encoding=UTF-8");
        cmd.add("-Dsikuli.IDE_should_run");

        if (!classPath.isEmpty()) {
          cmd.add("-cp");
          cmd.add(classPath);
        }

        cmd.add("org.sikuli.ide.SikulixIDE");
//      cmd.addAll(finalArgs);

        RunTime.startLog(3, "*********************** leaving start");

    if (shouldDetach()) {
      ProcessRunner.detach(cmd);
      System.exit(0);
    } else {
      int exitCode = ProcessRunner.runBlocking(cmd);
      System.exit(exitCode);
    }



        int exitCode = ProcessRunner.runBlocking(cmd);
        System.exit(exitCode);
      }
      //endregion
*/
    }
    //endregion
  }
}
