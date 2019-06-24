/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.sikuli.basics.Debug;
import org.sikuli.basics.HotkeyEvent;
import org.sikuli.basics.HotkeyListener;
import org.sikuli.basics.HotkeyManager;
import org.sikuli.script.runners.AbstractScriptRunner;
import org.sikuli.script.runners.InvalidRunner;
import org.sikuli.script.support.IScriptRunner.EffectiveRunner;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;
  static final RunTime runTime = RunTime.get();

  // <editor-fold desc="00 runner handling">
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

        Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.sikuli.script.runners"),
            new SubTypesScanner());

        Set<Class<? extends AbstractScriptRunner>> classes = reflections.getSubTypesOf(AbstractScriptRunner.class);

        for (Class<? extends AbstractScriptRunner> cl : classes) {
          IScriptRunner current = null;

          try {
            current = cl.getConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
              | InvocationTargetException | NoSuchMethodException | SecurityException e) {

            log(lvl, "init: %s: warning: not possible", cl);
            continue;
          }

          String name = current.getName();
          if (name != null && !name.startsWith("Not")) {
            runners.add(current);
            if (current.isSupported()) {
              log(lvl, "added: %s %s %s", current.getName(), Arrays.toString(current.getExtensions()),
                  current.getType());
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
      for (IScriptRunner runner : supportedRunners) {
        if (runner.canHandle(identifier)) {
          return runner;
        }
      }
      return new InvalidRunner();
    }
  }

  public static EffectiveRunner getEffectiveRunner(String identifier) {
    IScriptRunner runner = getRunner(identifier);
    return runner.getEffectiveRunner(identifier);
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
  // </editor-fold>

  public static final int FILE_NOT_FOUND = 256;
  public static final int NOT_SUPPORTED = 257;

  public static int runScript(String script,  String[] args, IScriptRunner.Options options) {
    if (script.contains("\n")) {
      String[] header = script.substring(0, Math.min(100, script.length())).trim().split("\n");
      IScriptRunner runner = null;
      if (header.length > 0) {
        String selector = header[0];
        runner = getRunner(selector);
        if (runner.isSupported()) {
          script = script.replaceFirst(selector, "").trim();
          return runner.evalScript(script, options);
        }
      }
      return 0;
    } else
      return run(script, args, options);
  }

  private static IScriptRunner currentRunner = new InvalidRunner();

  public static int runScripts(String[] runScripts, String[] args, IScriptRunner.Options options) {
    int exitCode = 0;
    if (runScripts != null && runScripts.length > 0) {

      // making stop hotkey available
      HotkeyManager.getInstance().addHotkey("Abort", new HotkeyListener() {
        @Override
        public void hotkeyPressed(HotkeyEvent e) {
          Debug.log(3, "Stop HotKey was pressed");
          currentRunner.abort();
        }
      });

      for (String scriptGiven : runScripts) {
        if (scriptGiven.startsWith("!")) {
          // special meaning from -r option evaluation to get a synchronous log and noop action
          log(3, "runscript: new base directory: %s", scriptGiven.substring(1));
          continue;
        } else if (scriptGiven.startsWith("?")) {
          // special meaning from -r option evaluation to get a synchronous log and action
          scriptGiven = scriptGiven.substring(1);
          exitCode = FILE_NOT_FOUND;
        } else {
          log(3, "runscript: running script: %s", scriptGiven);
          IScriptRunner runner = getRunner(scriptGiven);
          RunTime.get().setLastScriptRunReturnCode(0);
          currentRunner = runner;
          exitCode = runner.runScript(scriptGiven, args, options);
          RunTime.get().setLastScriptRunReturnCode(exitCode);
          currentRunner = new InvalidRunner();
        }
        if (exitCode != 0) {
          if (exitCode == FILE_NOT_FOUND) {
            log(-1, "runscript: not found: %s", scriptGiven);
          }
          break;
        }
      }
    }
    return exitCode;
  }

  public static synchronized int run(String script, String[] args, IScriptRunner.Options options) {
    IScriptRunner runner = getRunner(script);
    int retVal;
    retVal = runner.runScript(script, args, options);
    return retVal;
  }

  /**
   * Checks, whether the given directory contains a file with extension, that<br>
   *  - has the same name (excluding extension)<br>
   *  - can be run by one of the supported runners
   *
   * @param fScriptFolder directory that might have a script file
   * @return the script file's absolute path
   */
  public static File getScriptFile(File fScriptFolder) {
    if (fScriptFolder == null) {
      return null;
    }
    if (fScriptFolder.isDirectory()) {
      for (File aFile : fScriptFolder.listFiles()) {
        if (aFile.isDirectory()) {
          // contained directories need not be checked
          continue;
        }
        if (FilenameUtils.getBaseName(aFile.getName())
            .equals(FilenameUtils.getBaseName(fScriptFolder.getName()))) {
          for (IScriptRunner runner : getRunners()) {
            if (runner.canHandle(aFile.getName())) {
              return aFile;
            }
          }
        }
      }
    }
    return null;
  }
}
