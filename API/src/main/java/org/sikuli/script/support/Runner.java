/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

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
import org.sikuli.script.ImagePath;
import org.sikuli.script.runners.AbstractScriptRunner;
import org.sikuli.script.runners.InvalidRunner;
import org.sikuli.script.runners.JavaScriptRunner;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;

public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;
  static final RunTime runTime = RunTime.get();

  //<editor-fold desc="00 runner handling">
  private static List<IScriptRunner> runners = new LinkedList<>();
  private static List<IScriptRunner> supportedRunners = new LinkedList<>();

  static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
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

            log(lvl, "warning: %s", e.getMessage());
            continue;
          }

          String name = current.getName();
          if (name != null && !name.startsWith("Not")) {
            runners.add(current);
            if (current.isSupported()) {
              log(lvl, "added: %s %s %s", current.getName(), Arrays.toString(current.getExtensions()), current.getType());
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
          if (!ExtensionManager.shouldCheckContent(r.getType(), identifier)) {
            continue;
          }
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
  //</editor-fold>

  public static int getLastReturnCode() {
    return lastReturnCode;
  }

  private static int lastReturnCode = 0;
  private static String lastWorkFolder = null;

  public static int runScripts(String[] runScripts) {
    String someJS = "";
    int exitCode = 0;
    if (runScripts != null && runScripts.length > 0) {
      for (String scriptGiven : runScripts) {
        String actualFolder = lastWorkFolder;
        if (!scriptGiven.endsWith(".sikuli")) {
          scriptGiven += ".sikuli";
        }
        if (null == actualFolder && !new File(scriptGiven).isAbsolute()) {
          if (new File(runTime.fWorkDir, scriptGiven).exists()) {
            actualFolder = runTime.fWorkDir.getAbsolutePath();
          }
        }
        String  scriptFileName = FileManager.normalize(scriptGiven, actualFolder);
        if (!new File(scriptFileName).exists()) {
          log(-1, "Script file not found: %s", scriptFileName);
          exitCode = 1;
          break;
        }
        lastWorkFolder = new File(scriptFileName).getParent();
        if (lastReturnCode < 0) {
          log(lvl, "Exit code -1: Terminating multi-script-run");
          break;
        }
//        someJS = runTime.getOption("runsetup");
//        if (!someJS.isEmpty()) {
//          log(lvl, "Options.runsetup: %s", someJS);
//          getRunner(JavaScriptRunner.class).evalScript(someJS, null);
//        }
        exitCode = run(scriptFileName, RunTime.getUserArgs());
//        someJS = runTime.getOption("runteardown");
//        if (!someJS.isEmpty()) {
//          log(lvl, "Options.runteardown: %s", someJS);
//          getRunner(JavaScriptRunner.class).evalScript(someJS, null);
//        }
        if (exitCode == -999) {
          exitCode = lastReturnCode;
        }
        lastReturnCode = exitCode;
      }
    }
    return exitCode;
  }

  public static synchronized int run(String givenName) {
    return run(givenName, new String[0]);
  }

  public static synchronized int run(String script, String[] args) {
    return run(script, args, null);
  }

  public static synchronized int run(String script, String[] args, IScriptRunner.Options options) {
    IScriptRunner runner = getRunner(script);
    int retVal;
    retVal = runner.runScript(script, args, options);
    return retVal;
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
