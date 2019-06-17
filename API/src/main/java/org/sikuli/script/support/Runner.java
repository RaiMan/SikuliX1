/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.sikuli.basics.Debug;
import org.sikuli.script.runners.AbstractScriptRunner;
import org.sikuli.script.runners.InvalidRunner;
//import org.sikuli.script.runners.SikulixRunner;

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
      for (IScriptRunner runner : supportedRunners) {
        if (runner.canHandle(identifier)) {          
          return runner;
        }
      }
//    File possibleScriptFileOrFolder = new File(identifier);
//    if (possibleScriptFileOrFolder.isDirectory()) {
//      return true;
//    }
//    String extension = FilenameUtils.getExtension(identifier);
//    return extension.isEmpty() || "sikuli".equals(extension);
      log(-1, "getRunner: none found for: %s", identifier);
      return new InvalidRunner();
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
  public static final int FILE_NOT_FOUND = 256;
  public static final int FILE_NOT_FOUND_SILENT = 257;
  public static final int NOT_SUPPORTED = 258;

  public static int runScript(String script) {
    if (script.contains("\n")) {
      String[] header = script.substring(0, Math.min(100, script.length())).trim().split("\n");
      IScriptRunner runner = null;
      if (header.length > 0) {
        String selector = header[0];
        runner = getRunner(selector);
        if (runner.isSupported()) {
          script = script.replaceFirst(selector, "").trim();
          return runner.evalScript(script, null);
        }
      }
      return 0;
    } else
      return runScripts(new String[]{script});
  }

  public static int runScripts(String[] runScripts) {
    int exitCode = 0;
    File scriptFile;
    if (runScripts != null && runScripts.length > 0) {
      IScriptRunner.Options runOptions = new IScriptRunner.Options();
      for (String scriptGiven : runScripts) {                          
        IScriptRunner runner = getRunner(scriptGiven);        
        exitCode = runner.runScript(scriptGiven, null, runOptions);
        
        if (exitCode != 0) {
          return exitCode;
        }
        
        
//        if (!scriptFile.isAbsolute()) {
//          log(lvl, "runScript: requested: %s / %s",
//              null == lastWorkFolder ? "unknown" : lastWorkFolder, scriptGiven);
//          scriptFile = Runner.checkScriptFolderOrFile(lastWorkFolder, scriptFile);
//        } else {
//          log(lvl, "runScript: requested: %s", scriptGiven);
//        }
//        if (!scriptFile.exists()) {
//          if (FilenameUtils.getExtension(scriptFile.getName()).isEmpty()) {
//            exitCode = FILE_NOT_FOUND;
//            for (String extension : SikulixRunner.EXTENSIONS) {
//              File withExtension = new File(scriptFile.getPath() + "." + extension);
//              if (withExtension.exists()) {
//                scriptFile = withExtension;
//                exitCode = 0;
//                break;
//              }
//            }
//          }
//        } else if (scriptFile.isDirectory() && !FilenameUtils.getExtension(scriptFile.getPath()).equals("sikuli")) {
//          File innerScriptFile = Runner.getScriptFile(scriptFile);
//          if (null == innerScriptFile) {
//            if (scriptFile.isFile()) {
//              log(3, "runscript: not supported: (.%s) %s",
//                  FilenameUtils.getExtension(scriptFile.getPath()), scriptFile);
//              lastWorkFolder = scriptFile.getParent();
//            } else {
//              lastWorkFolder = scriptFile.getPath();
//            }
//            log(lvl, "runScript: new workfolder: %s", lastWorkFolder);
//            continue;
//          } else {
//            lastWorkFolder = scriptFile.getParent();
//            scriptFile = innerScriptFile;
//          }
//        }
//        if (exitCode == 0) {
//          runOptions.setScriptName(scriptGiven);
//          log(lvl, "runScript: executing: %s", scriptFile);
//          exitCode = run(scriptFile.getPath(), RunTime.getUserArgs(), runOptions);
//        }
//        if (exitCode == FILE_NOT_FOUND_SILENT) {
//          continue;
//        } else if (exitCode == FILE_NOT_FOUND) {
//          log(-1, "runScript: not found: %s / %s", lastWorkFolder, scriptGiven);
//          exitCode = -1;
//        } else if (exitCode < 0) {
//          log(lvl, "runscript: Exit code < 0: Terminating multi-script-run");
//          break;
//        }
//        lastReturnCode = exitCode;
//      }
//    }
        
      }}
    return exitCode;
  }

  public static synchronized int run(String script, String[] args, IScriptRunner.Options options) {
    String extension = FilenameUtils.getExtension(script);
    IScriptRunner runner = getRunner(script);
    int retVal;
    retVal = runner.runScript(script, args, options);
    return retVal;
  }

//  public static File checkScriptFolderOrFile(String baseFolder, File folderOrFile) {
//    if (null == folderOrFile) {
//      return null;
//    }
//    RunTime runTime = RunTime.get();
//    File fBaseFolder = null;
//    if (!folderOrFile.isAbsolute()) {
//      if (null == baseFolder) {
//        if (!folderOrFile.isAbsolute() && new File(runTime.fWorkDir, folderOrFile.getPath()).exists()) {
//          fBaseFolder = runTime.fWorkDir;
//        } else if (!folderOrFile.isAbsolute() && new File(runTime.fUserDir, folderOrFile.getPath()).exists()) {
//          fBaseFolder = runTime.fUserDir;
//        }
//      } else {
//        fBaseFolder = new File(baseFolder);
//      }
//      folderOrFile = new File(fBaseFolder, folderOrFile.getPath());
//    }
//    try {
//      folderOrFile = folderOrFile.getCanonicalFile();
//    } catch (IOException e) {
//    }
//    return folderOrFile;
//  }

  public static File getScriptFile(File fScriptFileOrFolder) {
    if (fScriptFileOrFolder == null) {
      return null;
    }

    // check if fScriptFileOrFolder is a supported script file
    if (fScriptFileOrFolder.isFile()) {
      for (IScriptRunner runner : getRunners()) {
        if (runner.canHandle(fScriptFileOrFolder.getName())) {
          return fScriptFileOrFolder;
        }       
      }
    }

    // check if fScriptFileOrFolder contains a supported script file with same name
    if (fScriptFileOrFolder.isDirectory()) {
      for (File aFile : fScriptFileOrFolder.listFiles()) {
        if (FilenameUtils.removeExtension(aFile.getName()).equals(FilenameUtils.removeExtension(fScriptFileOrFolder.getName()))) {
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
