/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.SikuliXception;
import org.sikuli.script.runnerSupport.IScriptRunner;

import com.sun.jna.ptr.IntByReference;
import org.sikuli.script.support.Commons;

public abstract class AbstractScriptRunner implements IScriptRunner {

//  a copy template
//  public static final String NAME = "SomeName";
//  public static final String TYPE = "text/something";
//  public static final String[] EXTENSIONS = new String[0];
//
//  @Override
//  public String getName() {
//    return NAME;
//  }
//
//  @Override
//  public String[] getExtensions() {
//    return EXTENSIONS;
//  }
//
//  @Override
//  public String getType() {
//    return TYPE;
//  }

  private boolean ready = false;

  private boolean running = false;

  public final boolean isRunning() {
    return running;
  }

  private PrintStream redirectedStdout;
  private PrintStream redirectedStderr;

  public boolean isStdoutRedirected() {
    return null != redirectedStdout;
  }

  private static volatile Thread worker = null;
  private static final ScheduledExecutorService TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
  private static final Object WORKER_LOCK = new Object();
  private boolean aborted = false;

  protected void log(int level, String message, Object... args) {
    Debug.logx(level, getName() + "Runner: " + message, args);
  }

  private void logNotSupported(String method) {
    log(-1, "does not (yet) support %s", method);
  }

  @Override
  public final void init(String[] args) throws SikuliXception {
    synchronized (AbstractScriptRunner.class) {
      if (!ready) {
        try {
          doInit(args);

          if (redirectedStdout != null && redirectedStderr != null) {
            redirectNow(redirectedStdout, redirectedStderr);
          }

          ready = true;
        } catch (Exception e) {
          throw new SikuliXception("Cannot initialize Script runner " + this.getName(), e);
        }
      }
    }
  }

  protected void doInit(String[] args) throws Exception {
    // NOOP if not implemented
  }

  public final boolean isReady() {
    synchronized (AbstractScriptRunner.class) {
      return ready;
    }
  }

  @Override
  public boolean isSupported() {
    return false;
  }

  @Override
  public final boolean hasExtension(String ending) {
    for (String suf : getExtensions()) {
      if (suf.equals(ending.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  public String getDefaultExtension() {
    return getExtensions()[0];
  }

  public boolean canHandle(String identifier) {
    if (identifier != null) {
      if (getType().equals(identifier)) {
        return true;
      }
      if (identifier.toLowerCase().equals(getName().toLowerCase())) {
        return true;
      }
      if (identifier.toLowerCase().startsWith(getName().toLowerCase() + "*")) {
        return true;
      }
      if (hasExtension(identifier)) {
        return true;
      }
      if (canHandleFileEnding(identifier)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public final void redirect(PrintStream stdout, PrintStream stderr) {
    synchronized (AbstractScriptRunner.class) {
      log(4, "Initiate IO redirect");

      this.redirectedStdout = stdout;
      this.redirectedStderr = stderr;

      if (ready) {
        if (stdout != null && stderr != null) {
          redirectNow(stdout, stderr);
        } else {
          doRedirect(System.out, System.err);
        }
      }
    }
  }

  private final void redirectNow(PrintStream stdout, PrintStream stderr) {
    boolean ret = doRedirect(stdout, stderr);
    if (ret) {
      log(3, "IO redirect established");
    }
  }

  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    return false;
  }

  protected int savedLevel;
  @Override
  public final int runScript(String script, String[] scriptArgs, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();

    return runSynchronized(options, () -> {
      savedLevel = Debug.getDebugLevel();
      if (!Debug.isVerbose()) {
        Debug.off();
      }

      int exitCode = -1;

      if (script != null) {
        adjustBundlePath(script, options);
        beforeScriptRun(script, options);

        try {
          exitCode = doRunScript(script, scriptArgs, options);
        } finally {
          if (script != null) {
            resetBundlePath(script, options);
            //TODO reset options per scriptrun
            Settings.SwitchToText = false;
          }
        }

        afterScriptRun(script, options);
        Debug.setDebugLevel(savedLevel);
      }

      return exitCode;
    });
  }

  public EffectiveRunner getEffectiveRunner(String script) {
    return new EffectiveRunner(this, script, false);
  }

  public static final int NOT_SUPPORTED = 257;

  protected int doRunScript(String scriptfile, String[] scriptArgs, IScriptRunner.Options options) {
    logNotSupported("runScript");
    return NOT_SUPPORTED;
  }

  @Override
  public final int evalScript(String script, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();

    return runSynchronized(options, () -> doEvalScript(script, options));
  }

  protected int doEvalScript(String script, IScriptRunner.Options options) {
    logNotSupported("evalScript");
    return -1;
  }

  @Override
  public final void runLines(String lines, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();

    runSynchronized(options, () -> {
      doRunLines(lines, options);
      return 0;
    });
  }

  protected void doRunLines(String lines, IScriptRunner.Options options) {
    logNotSupported("runLines");
  }

  @Override
  public final void close() {
    synchronized (AbstractScriptRunner.class) {
      ready = false;
      doClose();
    }
  }

  protected void doClose() {
    // NOOP if not implemented
  }

  @Override
  public final void reset() {
    synchronized (AbstractScriptRunner.class) {
      try {
        close();
        init(null);
        log(3, "reset requested (experimental: please report oddities)");
      } catch (Exception e) {
        log(-1, "reset requested but did not work. Please report this case."
            + "Do not run scripts anymore and restart the IDE after having saved your work");
      }
    }
  }

  @Override
  public void execBefore(String[] stmts) {
    if (stmts == null) {
      codeBefore = null;
      return;
    }
    if (codeBefore == null) {
      codeBefore = new ArrayList<String>();
    }
    codeBefore.addAll(Arrays.asList(stmts));
  }

  @Override
  public void execAfter(String[] stmts) {
    if (stmts == null) {
      codeAfter = null;
      return;
    }
    if (codeAfter == null) {
      codeAfter = new ArrayList<String>();
    }
    codeAfter.addAll(Arrays.asList(stmts));
  }

  static ArrayList<String> codeBefore = null;
  static ArrayList<String> codeAfter = null;

  public boolean isAbortSupported() {
    return false;
  }

  public final void abort() {
    synchronized (WORKER_LOCK) {
      if (worker != null && running && isAbortSupported()) {
        aborted = true;
        doAbort();
      }
    }
  }

  /**
   * Interrupts the worker thread.
   * <p>
   * Can be overridden by Runner implementations if an interrupt
   * is not needed.
   */
  protected void doAbort() {
    worker.interrupt();
  }

  @Override
  public final boolean isAborted() {
    synchronized (WORKER_LOCK) {
      return aborted;
    }
  }

  private int runAbortable(IScriptRunner.Options options, IntSupplier block) {
    boolean newWorker;
    IntByReference exitCode = new IntByReference(1);

    synchronized (WORKER_LOCK) {
      aborted = false;
      newWorker = worker == null;

      if (newWorker) {
        worker = new Thread() {
          @Override
          public void run() {
            synchronized (AbstractScriptRunner.class) {
              try {
                exitCode.setValue(block.getAsInt());
              } finally {
                AbstractScriptRunner.class.notify();
              }
            }
          }
        };
        worker.start();
      }
    }

    if (newWorker) {
      ScheduledFuture<?> timeoutFuture = null;
      try {
        if (options.getTimeout() > 0) {
          final long timeout = options.getTimeout();

          timeoutFuture = TIMEOUT_EXECUTOR.schedule(() -> {
            Debug.info("%s script timed out after %d ms", getName(), timeout);
            abort();
          }, timeout, TimeUnit.MILLISECONDS);
        }

        AbstractScriptRunner.class.wait();

      } catch (InterruptedException e) {
        log(-1, "Script interrupted unexpectedly: %s", e.getMessage());
      } finally {
        if (timeoutFuture != null) {
          timeoutFuture.cancel(false);
          timeoutFuture = null;
        }
        synchronized (WORKER_LOCK) {
          worker = null;
        }
      }
    } else {
      exitCode.setValue(block.getAsInt());
    }
    return exitCode.getValue();
  }

  private int runSynchronized(IScriptRunner.Options options, IntSupplier block) {
    synchronized (AbstractScriptRunner.class) {
      running = true;

      init(null);

      try {
        return runAbortable(options, block);
      } finally {
        running = false;
      }
    }
  }

  public final boolean canHandleFileEnding(String identifier) {
    for (String suf : getFileEndings()) {
      if (identifier.toLowerCase().endsWith(suf.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  public String[] getFileEndings() {
    String[] extensions = getExtensions();
    String[] endings = new String[extensions.length];

    for (int i = 0; i < extensions.length; i++) {
      endings[i] = "." + extensions[i];
    }
    return endings;
  }

  protected void adjustBundlePath(String script, IScriptRunner.Options options) {
    // NOOP
  }

  protected void resetBundlePath(String script, IScriptRunner.Options options) {
    // NOOP
  }

  public void beforeScriptRun(String script, Options options) {
    // NOOP
  }

  public void afterScriptRun(String script, Options options) {
    // NOOP
  }

  public void adjustImportPath(Map<String, String> files, IScriptRunner.Options options) {
    // NOOP
  }

  public List<String> getImportPlaces() {
    return new ArrayList<>(); //NOOP
  }
}
