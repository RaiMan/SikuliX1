/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support.ide;

import org.sikuli.basics.Debug;
import org.sikuli.support.FileManager;
import org.sikuli.script.Sikulix;
import org.sikuli.support.Commons;
import org.sikuli.support.runner.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IDESupport {

  private static final String me = "IDESupport: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static Map<String, IIDESupport> ideSupporter = new HashMap<String, IIDESupport>();

  public static IIDESupport get(String type) {
    return ideSupporter.get(type);
  }

  public static void initIDESupport() {
    ServiceLoader<IIDESupport> sloader = ServiceLoader.load(IIDESupport.class);
    Iterator<IIDESupport> supIterator = sloader.iterator();
    while (supIterator.hasNext()) {
      IIDESupport current = supIterator.next();
      try {
        for (String ending : current.getTypes()) {
          ideSupporter.put(ending, current);
        }
      } catch (Exception ex) {
      }
    }
  }

  public static boolean transferScript(String src, String dest, IRunner runner) {
    FileManager.FileFilter filter = new FileManager.FileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().endsWith(".html")) {
          return false;
        } else if (entry.getName().endsWith(".$py.class")) {
          return false;
        } else {
          for (String ending : runner.getExtensions()) {
            if (entry.getName().endsWith("." + ending)) {
              return false;
            }
          }
        }
        return true;
      }
    };
    try {
      FileManager.xcopy(src, dest, filter);
    } catch (IOException ex) {
      log(-1, "transferScript: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  private static final Class<?>[] IDE_RUNNER_CLASSES = new Class<?>[]{
      JythonRunner.class,
      PythonRunner.class,
      JRubyRunner.class,
      TextRunner.class};

  private static final List<IRunner> IDE_RUNNERS = new ArrayList<>();

  public static void initRunners() {
    synchronized (IDE_RUNNERS) {
      if (IDE_RUNNERS.isEmpty()) {
        List<IRunner> runners = Runner.getRunners();
        for (Class<?> runnerClass : IDE_RUNNER_CLASSES) {
          for (IRunner runner : runners) {
            if (runnerClass.equals(runner.getClass())) {
              log(lvl, "added: %s", runner.getName());
              IDE_RUNNERS.add(runner);
              break;
            }
          }
        }
      }
    }

    if (IDE_RUNNERS.isEmpty()) {
      String em = "Terminating: No scripting support available. Rerun Setup!";
      log(-1, em);
      Sikulix.popError(em, "IDE has problems ...");
      System.exit(1);
    }

    defaultRunner = IDE_RUNNERS.get(0);
    // initialize runner to speed up first script run
    (new Thread() {
      @Override
      public void run() {
        defaultRunner.init(null);
      }
    }).start();

    Commons.startLog(1,"IDESupport exit: defaultrunner: %s (%s)", defaultRunner.getName(), defaultRunner.getExtensions()[0]);
  }

  public static IRunner getDefaultRunner() {
    return defaultRunner;
  }

  private static IRunner defaultRunner = null;
}
