/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.runners.AbstractScriptRunner;
import org.sikuli.script.runners.InvalidRunner;
import org.sikuli.script.runners.JavaScriptRunner;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;

/**
 * INTERNAL USE --- NOT official API<br>
 * not in version 2
 */
public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;
  static final RunTime runTime = RunTime.get();

  private static String[] runScripts = null;
  private static int lastReturnCode = 0;

  private static List<IScriptRunner> runners = new LinkedList<>();
  private static List<IScriptRunner> supportedRunners = new LinkedList<>();

  static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  public static String[] evalArgs(String[] args) {
    CommandArgs cmdArgs = new CommandArgs("SCRIPT");
    CommandLine cmdLine = cmdArgs.getCommandLine(CommandArgs.scanArgs(args));
    String cmdValue;

    if (cmdLine == null || cmdLine.getOptions().length == 0) {
      log(-1, "Did not find any valid option on command line!");
      cmdArgs.printHelp();
      System.exit(1);
    }

    if (cmdLine.hasOption(CommandArgsEnum.HELP.shortname())) {
      cmdArgs.printHelp();
      System.exit(1);
    }

    if (cmdLine.hasOption(CommandArgsEnum.LOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.LOGFILE.longname());
      if (!Debug.setLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.USERLOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.USERLOGFILE.longname());
      if (!Debug.setUserLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.DEBUG.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      if (cmdValue == null) {
        Debug.setDebugLevel(3);
        Settings.LogTime = true;
        if (!Debug.isLogToFile()) {
          Debug.setLogFile("");
        }
      } else {
        Debug.setDebugLevel(cmdValue);
      }
    }

    runTime.setArgs(cmdArgs.getUserArgs(), cmdArgs.getSikuliArgs());
    log(lvl, "commandline: %s", cmdArgs.getArgsOrg());
    if (lvl > 2) {
      runTime.printArgs();
    }

    String[] runScripts = null;
    runTime.runningTests = false;
    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.RUN.longname());
    }

    return runScripts;
  }

  public static int run(String givenName) {
    return run(givenName, new String[0]);
  }

  private static boolean isReady = false;

  public static void initRunners() {
    synchronized (runners) {
      if (isReady) {
        return;
      }

      if (runners.isEmpty()) {

        Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.sikuli.script.runners"), new SubTypesScanner());

        Set<Class<? extends AbstractScriptRunner>> classes = reflections.getSubTypesOf(AbstractScriptRunner.class);

        for (Class<? extends AbstractScriptRunner> cl : classes) {
          IScriptRunner current = null;

          try {
            current = cl.getConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                  | InvocationTargetException | NoSuchMethodException | SecurityException e) {

            log(lvl, "initRunners: warning: %s", e.getMessage());
            continue;
          }

          String name = current.getName();
          if (name != null && !name.startsWith("Not")) {
            runners.add(current);
            if (current.isSupported()) {
              log(lvl, "initRunners: added: %s", current.getName());
              supportedRunners.add(current);
            }
          }
        }
      }
      isReady = true;
    }
  }

  public static IScriptRunner getRunner(String identifier) {
    if (identifier == null) {
      return null;
    }

    synchronized (runners) {
      initRunners();

      for (IScriptRunner r : supportedRunners) {
        if (r.canHandle(identifier)) {
          return r;
        }
      }

      log(-1, "getRunner: no runner found for:\n%s", identifier);
      return new InvalidRunner(identifier);
    }
  }

  public static List<IScriptRunner> getRunners() {
    synchronized (runners) {
      initRunners();

      return new LinkedList<IScriptRunner>(supportedRunners);
    }
  }

  public static IScriptRunner getRunner(Class<? extends IScriptRunner> runnerClass) {
    synchronized (runners) {
      initRunners();

      for (IScriptRunner r : supportedRunners) {
        if (r.getClass().equals(runnerClass)) {
          return r;
        }
      }
    }
    return new InvalidRunner(runnerClass);
  }

  public static Set<String> getExtensions() {
    synchronized (runners) {
      initRunners();

      Set<String> extensions = new HashSet<>();

      for (IScriptRunner runner : runners) {
        for (String ex : runner.getExtensions()) {
          extensions.add(ex);
        }
      }

      return extensions;
    }
  }

  public static Set<String> getNames() {
    synchronized (runners) {
      initRunners();

      Set<String> names = new HashSet<>();

      for (IScriptRunner runner : runners) {
        names.add(runner.getName());
      }

      return names;
    }
  }

  public static Set<String> getTypes() {
    synchronized (runners) {
      initRunners();

      Set<String> types = new HashSet<>();

      for (IScriptRunner runner : runners) {
        types.add(runner.getType());
      }

      return types;
    }
  }

  public static String getExtension(String identifier) {
    synchronized (runners) {
      initRunners();

      for (IScriptRunner r : runners) {
        if (r.canHandle(identifier)) {
          String[] extensions = r.getExtensions();

          if (extensions.length > 0) {
            return extensions[0];
          }
        }
      }
      return null;
    }
  }

  public static synchronized int run(String script, String[] args) {
   return run(script, args, null);
  }

  public static synchronized int run(String script, String[] args, IScriptRunner.Options options) {
    String savePath = ImagePath.getBundlePathSet();

    IScriptRunner runner = Runner.getRunner(script);

    int retVal;

    retVal = runner.runScript(script, args, options);

    if (savePath != null) {
      ImagePath.setBundlePath(savePath);
    }
    lastReturnCode = retVal;
    return retVal;
  }

  public static int getLastReturnCode() {
    return lastReturnCode;
  }

  public static int runScripts(String[] args) {
    runScripts = Runner.evalArgs(args);
    String someJS = "";
    int exitCode = 0;
    if (runScripts != null && runScripts.length > 0) {
      for (String givenScriptName : runScripts) {
        if (lastReturnCode == -1) {
          log(lvl, "Exit code -1: Terminating multi-script-run");
          break;
        }
        someJS = runTime.getOption("runsetup");
        if (!someJS.isEmpty()) {
          log(lvl, "Options.runsetup: %s", someJS);
          getRunner(JavaScriptRunner.class).evalScript(someJS, null);
        }
        exitCode = run(givenScriptName, runTime.getArgs());
        someJS = runTime.getOption("runteardown");
        if (!someJS.isEmpty()) {
          log(lvl, "Options.runteardown: %s", someJS);
          getRunner(JavaScriptRunner.class).evalScript(someJS, null);
        }
        if (exitCode == -999) {
          exitCode = lastReturnCode;
        }
        lastReturnCode = exitCode;
      }
    }
    return exitCode;
  }

  public static File getScriptFile(File fScriptFolder) {
    if (fScriptFolder == null) {
      return null;
    }

    // check if fScriptFolder is a supported script file
    if (fScriptFolder.isFile()) {
      for (IScriptRunner runner : getRunners()) {
        for (String extension : runner.getExtensions()) {
          if (FilenameUtils.getExtension(fScriptFolder.getName()).equals(extension)) {
            return fScriptFolder;
          }
        }
      }
    }

    File[] content = FileManager.getScriptFile(fScriptFolder);
    if (null == content) {
      return null;
    }
    File fScript = null;
    for (File aFile : content) {
      for (IScriptRunner runner : getRunners()) {
        for (String extension : runner.getExtensions()) {
          if (!FilenameUtils.getExtension(aFile.getName()).equals(extension)) {
            continue;
          }
          fScript = aFile;
          break;
        }
        if (fScript != null) {
          break;
        }
      }
      if (fScript != null) {
        break;
      }
    }
    // try with compiled script
    if (content.length == 1 && content[0].getName().endsWith("$py.class")) {
      fScript = content[0];
    }
    return fScript;
  }
}
