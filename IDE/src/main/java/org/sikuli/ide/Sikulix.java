/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.basics.FileManager;
import org.sikuli.idesupport.ExtensionManager;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.ProcessRunner;
import org.sikuli.script.support.RunTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Sikulix {

  public static void main(String[] args) {
    System.setProperty("sikuli.IDE_should_run", "develop");

    Commons.setStartArgs(args);

    for (String arg : args) {
      if ("-v".equals(arg)) {
        RunTime.setVerbose();
      } else if ("-q".equals(arg)) {
        RunTime.setQuiet();
      } else if ("-r".equals(arg)) {
        RunTime.setShouldRunScript();
      } else if ("-s".equals(arg)) {
        RunTime.setAsServer();
      }
    }

    if (Commons.hasArg("a")) {
      String argValue = Commons.getArg("a");
      File path = Commons.setAppDataPath(argValue);
      Commons.setTempFolder(new File(path, "Temp"));
    } else {
      Commons.setTempFolder();
    }

    File runningJar = Commons.getRunningJar();
    RunTime.startLog(1, "Running: %s", runningJar);

    RunTime.startLog(1, "AppData: %s", Commons.getAppDataPath());

    //TODO Extensions??
    String classPath = ExtensionManager.makeClassPath(runningJar);

    if (!runningJar.getName().endsWith(".jar") || classPath.split(File.pathSeparator).length < 2) {
      SikulixIDE.main(args);
    } else {
      SikulixIDE.main(args);
      //TODO start IDE in subprocess?
      //region hidden
      if (false) {
        RunTime.terminate(999, "//TODO start IDE in subprocess?");
        FileManager.writeStringToFile(runningJar.getAbsolutePath(),
            new File(Commons.getAppDataStore(), "lastUsedJar.txt"));
        List<String> cmd = new ArrayList<>();
        System.getProperty("java.home");
        if (Commons.runningWindows()) {
          cmd.add(System.getProperty("java.home") + "\\bin\\java.exe");
        } else {
          cmd.add(System.getProperty("java.home") + "/bin/java");
        }
        if (!Commons.isJava8()) {
      /*
      Suppress Java 9+ warnings
      --add-opens
      java.desktop/javax.swing.plaf.basic=ALL-UNNAMED
      --add-opens
      java.base/sun.nio.ch=ALL-UNNAMED
      --add-opens
      java.base/java.io=ALL-UNNAMED
      */
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
      //endregion
    }
  }
}
