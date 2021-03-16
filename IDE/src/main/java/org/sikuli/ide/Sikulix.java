/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.apache.commons.cli.CommandLine;
import org.sikuli.basics.*;
import org.sikuli.idesupport.ExtensionManager;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.ProcessRunner;
import org.sikuli.script.support.RunTime;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Sikulix {

  public static void main(String[] args) {
    System.setProperty("sikuli.IDE_should_run", "develop");
    List<String> finalArgs = RunTime.evalArgsStart(args);

    CommandArgs cmdArgs = new CommandArgs();
    CommandLine cmdLine = cmdArgs.getCommandLine(args);

    if (cmdLine != null && cmdLine.hasOption("a")) {
      String optionValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      File path = Commons.setAppDataPath(optionValue);
      RunTime.fTempPath = new File(path, "Temp");
    }

    File runningJar = Commons.getRunningJar(Commons.Type.IDE);
    RunTime.startLog(1, "Running: %s", runningJar);

    File fAppData = Commons.getAppDataPath();
    RunTime.startLog(1, "AppData: %s", fAppData);

    String classPath = ExtensionManager.makeClassPath(runningJar);
    if (!runningJar.getName().endsWith(".jar") || classPath.split(File.pathSeparator).length < 2) {
      SikulixIDE.main(args);
    } else {
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
  }
}
