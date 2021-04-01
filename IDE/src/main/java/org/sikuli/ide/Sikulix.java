/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.basics.FileManager;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import java.io.File;

public class Sikulix {

  public static void main(String[] args) {
    Commons.setStartClass(Sikulix.class);
    Commons.setStartArgs(args);

    if (Commons.hasArg("v")) {
      Commons.setVerbose();
    } else if (Commons.hasArg("r")) {
      RunTime.setShouldRunScript();
    }

    if (Commons.hasArg("a")) {
      String argValue = Commons.getArg("a");
      File path = Commons.setAppDataPath(argValue);
      Commons.setTempFolder(new File(path, "Temp"));
    } else {
      Commons.setTempFolder();
    }


    File runningFrom = Commons.getMainClassLocation();
    RunTime.startLog(1, "Running: %s :: %s", runningFrom, Sikulix.class.getCanonicalName());

    RunTime.startLog(1, "AppData: %s", Commons.getAppDataPath());

    //TODO Extensions??
    //String classPath = ExtensionManager.makeClassPath(runningFrom);

    //TODO prep for export to jar
    if (Commons.isRunningFromJar()) {
      FileManager.writeStringToFile(Commons.getMainClassLocation().getAbsolutePath(),
          new File(Commons.getAppDataStore(), "lastUsedJar.txt"));
    }

    //TODO start IDE in subprocess?
    if (true) {
      SikulixIDE.main(args);
    } else {
      /*
      if (false) {
        RunTime.terminate(999, "//TODO start IDE in subprocess?");
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

//TODO IDE start: --add-opens supress warnings
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
        //TODO detach IDE: for what does it make sense?
*/
/*
    if (shouldDetach()) {
      ProcessRunner.detach(cmd);
      System.exit(0);
    } else {
      int exitCode = ProcessRunner.runBlocking(cmd);
      System.exit(exitCode);
    }
*/
/*

        int exitCode = ProcessRunner.runBlocking(cmd);
        System.exit(exitCode);
      }
      //endregion
*/
    }
  }
}
