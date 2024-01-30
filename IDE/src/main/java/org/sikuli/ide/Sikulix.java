/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.basics.*;
import org.sikuli.idesupport.IDEDesktopSupport;
import org.sikuli.recorder.Recorder;
import org.sikuli.script.SX;
import org.sikuli.script.SikuliXception;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runnerSupport.Runner;
import org.sikuli.script.runners.ProcessRunner;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.PreferencesUser;
import org.sikuli.script.support.devices.Device;
import org.sikuli.script.support.devices.MouseDevice;
import org.sikuli.script.support.devices.ScreenDevice;
import org.sikuli.script.support.gui.SXDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.sikuli.util.CommandArgsEnum.*;

public class Sikulix {

  public static void main(String[] args) {
    //region startup
    Debug.isIDEstarting(true);
    Commons.setStartClass(Sikulix.class);

    if (Commons.isRunningFromJar()) {
      Debug.print("<!--- META-INF/MANIFEST.MF from running jar");
      String manifest = Commons.getManifest();
      Debug.print(manifest.isEmpty() ? "... could not be read!" : manifest);
      Debug.print("-->");

      File parent = Commons.getMainClassLocation().getParentFile();
      if (parent != null) {
        if (Commons.runningMac()) {
          if (new File(parent, ".jpackage.xml").exists()) {
            Commons.isRunningApp(true);
            Debug.print("IDE : running as app");
          }
        } else if (Commons.runningWindows()) {
          if (new File(parent, "SikulixIDE.cfg").exists()) {
            Commons.isRunningApp(true);
            Debug.print("IDE : running as exe");
          }
        }
      }
      if (Commons.isRunningApp()) {
      }
    }

    IDEDesktopSupport.initStart();

    Commons.setStartArgs(args);

    if (Commons.hasStartArg(CONSOLE) || System.getProperty("sikuli.console") != null) {
      Debug.setConsole();
    }

    if (Commons.hasStartArg(QUIET)) {
      Debug.setQuiet();
    } else {
      if (Commons.hasStartArg(VERBOSE) || Commons.hasStartArg(DEBUG) || System.getProperty("sikuli.Debug") != null) {
        Debug.setVerbose();
      }
      if (Commons.getStartArgInt(DEBUG) > Debug.getDebugLevel()) {
        Debug.setDebugLevel(Commons.getStartArgInt(DEBUG));
      }
    }

    if (Commons.hasStartArg(APPDATA)) {
      //TODO when no path is given
      File path = Commons.setAppDataPath(Commons.getStartArg(APPDATA));
      Commons.setTempFolder(new File(path, "Temp"));
    } else {
      Commons.setTempFolder();
    }

    if (Commons.hasExtendedArg("jruby")) {
      boolean hasJRuby = true;
      try {
        Thread.currentThread().getContextClassLoader().loadClass("org.jruby.Main");
      } catch (ClassNotFoundException e) {
        if (!Commons.isRunningFromJar()) {
          Commons.terminate(1, "JRuby not available (running from classes)");
        }
        hasJRuby = false;
      }
      if (!hasJRuby) {
        File runningJar = Commons.getMainClassLocation();
        File tempFolder = Commons.getTempFolder();
        List<String> startArgs = Commons.getStartArgs();
        String jrubyJar = startArgs.get(startArgs.size() - 1);
        File fJrubyJar;
        if (jrubyJar.toLowerCase().equals("--jruby")) {
          if (Commons.isSandBox()) {
            fJrubyJar = new File(Commons.getAppDataPath(), "Lib/site-packages/jruby.jar");
          } else {
            fJrubyJar = new File(runningJar.getParentFile(), "jruby.jar");
          }
        } else {
          if (jrubyJar.startsWith("./")) {
            jrubyJar = jrubyJar.substring(2);
            System.out.println(jrubyJar);
            if (Commons.isSandBox()) {
              fJrubyJar = new File(Commons.getAppDataPath(), "Lib/site-packages/" + jrubyJar);
            } else {
              fJrubyJar = new File(runningJar.getParentFile(), jrubyJar);
            }
            System.out.println(fJrubyJar);
          } else {
            fJrubyJar = new File(jrubyJar);
          }
        }
        if (fJrubyJar.exists()) {
          jrubyJar = fJrubyJar.getAbsolutePath();
          String lines = "Class-Path: " + jrubyJar + "\n";
          File fClasspath = new File(tempFolder, "classpath.txt");
          FileManager.writeStringToFile(lines, fClasspath);
          ProcessRunner.run("jar", "-f", runningJar.getAbsolutePath(),
              "-u", "-m", fClasspath.getAbsolutePath());
          fClasspath.delete();
          System.out.println(String.format("--- JRuby added to manifest classpath\n" + jrubyJar +
              "\n--- Restart the IDE using:\n" +
              runningJar.getAbsolutePath()));
          Commons.terminate(1, "");
        } else {

        }
      }
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
      PreferencesUser.get().remove("CAPTURE_HOTKEY");
      PreferencesUser.get().remove("CAPTURE_HOTKEY_MODIFIERS");
      PreferencesUser.get().remove("STOP_HOTKEY");
      PreferencesUser.get().remove("STOP_HOTKEY_MODIFIERS");
      PreferencesUser.get().remove("IDE_LOCATION");
      PreferencesUser.get().remove("IDE_SIZE");
      PreferencesUser.get().remove("IDE_SESSION");

      PreferencesUser.get().kill(); //TODO remove prefs store
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

    if (Commons.hasStartArg(LOAD)) { //TODO
      String sx_arg_load = Commons.getOption("SX_ARG_LOAD");
      String[] scripts = Runner.resolveRelativeFiles(Commons.asArray(sx_arg_load));
      for (String script : scripts) {

      }
    }
    //endregion

    //region checkAccessibility
    Device.checkAccessibility();
    boolean muse = !MouseDevice.isUseable();
    boolean suse = !ScreenDevice.isUseable();
    Boolean popAnswer = true;
    if (muse || suse) {
      String text = (muse ? "Mouse usage seems to be blocked!\n" : "") +
          (suse ? "Make screenshots seems to be blocked!\n" : "") +
          "\nDo you want to continue?\n(Be sure you know what you are doing!)";
      popAnswer = SX.popAsk(text);
    }
    if (!popAnswer) {
      Commons.terminate(254, "User terminated IDE startup (Mouse/Screenshot blocked)");
    }
    //endregion

    if (!Commons.hasStartArg(RUN) && !Commons.hasStartArg(RUNSERVER) && !Commons.hasStartArg(RECORD)) {
      //region start IDE
      Debug.log(3, "IDE starting");
      ideSplash = null;
      if (Commons.isRunningFromJar()) {
        if (!Debug.isQuiet()) {
          ideSplash = new SXDialog("sxidestartup", SikulixIDE.getWindowTop(), SXDialog.POSITION.TOP);
          ideSplash.run();
        }
      }

      //region IDE block second start
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
            stopSplash();
/*
            Class<?> classIDE = Class.forName("org.sikuli.ide.SikulixIDE");
            Method stopSplash = classIDE.getMethod("stopSplash", new Class[0]);
            stopSplash.invoke(null, new Object[0]);
*/
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
        File tempFolder = Commons.getTempFolder();
        if (tempFolder != null) {
          Arrays.stream(Objects.requireNonNull(tempFolder.list())).filter(aFile -> (aFile.startsWith("Sikulix"))
              || (aFile.startsWith("jffi") && aFile.endsWith(".tmp")))
              .map(aFile -> new File(Commons.getTempFolder(), aFile)).forEach(FileManager::deleteFileOrFolder);
        }
      }
      //endregion

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

      SikulixIDE.start(args);
      //endregion

    } else if (Commons.hasStartArg(RUN)) {
      //region run scripts
      Debug.isIDEstarting(false);
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
      //endregion

    } else if (Commons.hasStartArg(RUNSERVER)) {
      //region run server
      Debug.isIDEstarting(false);
      Debug.getIdeStartLog();
      Class<?> cServer = null;
      try {
        cServer = Class.forName("org.sikuli.script.runners.ServerRunner");
        cServer.getMethod("run").invoke(null);
        Commons.terminate();
      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
      }
      try {
        cServer = Class.forName("org.sikuli.script.support.SikulixServer");
        if (!(Boolean) cServer.getMethod("run").invoke(null)) {
          Commons.terminate(1, "SikulixServer: terminated with errors");
        }
      } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
      }
      Commons.terminate();
      //endregion

    } else if (Commons.hasStartArg(RECORD)) {
      //region recording
      SX.popup("ok to start recording");
      Recorder.INSTANCE.startRecording();
      SX.popup("ok to stop recording");
      Recorder.INSTANCE.finishRecording();
      Commons.terminate();
      //endregion
    }
  }

  static SXDialog ideSplash;

  public static void stopSplash() {
    if (ideSplash != null) {
      ideSplash.setVisible(false);
      ideSplash.dispose();
      ideSplash = null;
    }
  }
}
