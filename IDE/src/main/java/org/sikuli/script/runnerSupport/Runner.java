/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runnerSupport;

import org.apache.commons.io.FilenameUtils;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.util.ClasspathHelper;
import org.sikuli.basics.Debug;
import org.sikuli.script.ImagePath;
import org.sikuli.script.runnerSupport.IScriptRunner.EffectiveRunner;
import org.sikuli.script.runners.AbstractScriptRunner;
import org.sikuli.script.runners.InvalidRunner;
import org.sikuli.script.support.Commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;

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

        log(3, "::initRunners: starting");

        Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.sikuli.script.runners"),
            new SubTypesScanner());

        Set<Class<? extends AbstractScriptRunner>> classes = reflections.getSubTypesOf(AbstractScriptRunner.class);

        for (Class<? extends AbstractScriptRunner> cl : classes) {
          IScriptRunner current = null;

          try {
            current = cl.getConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
              | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            continue;
          }

          String name = current.getName();
          if (name != null && !name.startsWith("Not")) {
            runners.add(current);
            if (current.isSupported()) {
              supportedRunners.add(current);
            }
          }
        }
        log(3, "::initRunners: complete");
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

  public static String[] resolveRelativeFiles(String[] givenScripts) {
    String[] runScripts = new String[givenScripts.length];
    String baseDir = Commons.getWorkDir().getPath();
    for (int i = 0; i < runScripts.length; i++) {
      String givenScript = givenScripts[i];
      String file = resolveRelativeFile(givenScript, baseDir);
      if (file == null) {
        file = resolveRelativeFile(givenScript + ".sikuli", baseDir);
      }
      if (file == null) {
        file = resolveRelativeFile(givenScript + ".py", baseDir);
      }
      if (file == null) {
        file = resolveRelativeFile(givenScript + ".rb", baseDir);
      }
      if (file == null) {
        runScripts[i] = "?" + givenScript;
        continue;
      }
      try {
        file = new File(file).getCanonicalPath();
      } catch (IOException e) {
      }
      EffectiveRunner runnerAndFile = Runner.getEffectiveRunner(file);
      IScriptRunner runner = runnerAndFile.getRunner();
      String fileToRun = runnerAndFile.getScript();
      File possibleDir = null;
      if (null == fileToRun) {
        for (String ending : new String[]{"", ".sikuli"}) {
          possibleDir = new File(file + ending);
          if (possibleDir.exists()) {
            break;
          } else {
            possibleDir = null;
          }
        }
        if (null == possibleDir) {
          runScripts[i] = "?" + givenScript;
          continue;
        }
        baseDir = possibleDir.getAbsolutePath();
        runnerAndFile =  Runner.getEffectiveRunner(baseDir);
        fileToRun = runnerAndFile.getScript();
        if (fileToRun == null) {
          fileToRun = "!" + baseDir;
        } else {
          fileToRun = baseDir;
        }
      }
      runScripts[i] = fileToRun;
      if (i == 0) {
        if (!fileToRun.startsWith("!")) {
          baseDir = new File(fileToRun).getParent();
        }
      }
    }
    return runScripts;
  }

  /**
   * a relative path is checked for existence in the current base folder,
   * working folder and user home folder in this sequence.
   *
   * @param scriptName
   * @return absolute file or null if not found
   */
  public static String resolveRelativeFile(String scriptName, String baseDir) {
    if (Commons.runningWindows() && (scriptName.startsWith("\\") || scriptName.startsWith("/"))) {
      scriptName = new File(scriptName).getAbsolutePath();
      return scriptName;
    }
    File file = new File(scriptName);
    if (!file.isAbsolute()) {
      File inBaseDir = new File(baseDir, scriptName);
      if (inBaseDir.exists()) {
        file = inBaseDir;
      } else {
        File inWorkDir = new File(Commons.getWorkDir(), scriptName);
        if (inWorkDir.exists()) {
          file = inWorkDir;
        } else {
          File inUserHome = new File(Commons.getUserHome(), scriptName);
          if (inUserHome.exists()) {
            file = inUserHome;
          } else {
            return null;
          }
        }
      }
    }
    return file.getAbsolutePath();
  }

  public static int runScript(String script, String[] args, IScriptRunner.Options options) {
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
    }
    File scriptFile = new File(script);
    if (!scriptFile.isAbsolute()) {
      File currentScript = null;
      File currentFolder = new File(ImagePath.getBundlePath()).getParentFile();
      try {
        currentScript = new File(currentFolder, script).getCanonicalFile();
      } catch (IOException e) {
        log(-1, "canonical file problem: %s / %s", currentFolder, script);
      }
      if (null == currentScript || !currentScript.exists()) {
        if (null != currentScript) {
          if (FilenameUtils.getExtension(script).isEmpty()) {
            try {
              currentScript = new File(currentFolder, script + ".sikuli").getCanonicalFile();
            } catch (IOException e) {
              log(-1, "canonical file problem: %s / %s", currentFolder, script + ".sikuli");
              currentScript = null;
            }
          }
          if (null == currentScript || !currentScript.exists()) {
            currentScript = null;
          }
        }
        if (null == currentScript) {
          log(-1, "script not found: %s", script);
          return FILE_NOT_FOUND;
        }
      }
      script = currentScript.getPath();
    }
    return runScripts(new String[]{script}, args, options);
  }

  public static int runScripts(String[] runScripts, String[] args, IScriptRunner.Options options) {
    int exitCode = 0;
    if (runScripts != null && runScripts.length > 0) {
      if (runScripts.length == 1) {
        if (runScripts[0].startsWith("!") && runScripts[0].endsWith(".jar")) {
          //TODO should be detected earlier
          log(-1, "runscript: No runner for given jar: %s", runScripts[0]);
          return FILE_NOT_FOUND;
        }
      }
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
          setLastScriptRunReturnCode(0);
          exitCode = runner.runScript(scriptGiven, args, options);
          setLastScriptRunReturnCode(exitCode);
        }
        if (exitCode != 0) {
          if (exitCode == FILE_NOT_FOUND) {
            log(-1, "runscript: (%d) not found: %s", exitCode, scriptGiven);
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
   * Aborts all runners by calling
   * their abort() method.
   */
  public static void abortAll() {
    for(IScriptRunner runner : supportedRunners) {
      runner.abort();
    }
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
        if (FilenameUtils.getBaseName(aFile.getName()).toLowerCase()
            .equals(FilenameUtils.getBaseName(fScriptFolder.getName()).toLowerCase())) {
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

  public static int getLastScriptRunReturnCode() {
    return lastScriptRunReturnCode;
  }

  public static void setLastScriptRunReturnCode(int lastScriptRunReturnCode) {
    lastScriptRunReturnCode = lastScriptRunReturnCode;
  }

  private static int lastScriptRunReturnCode = 0;

}
