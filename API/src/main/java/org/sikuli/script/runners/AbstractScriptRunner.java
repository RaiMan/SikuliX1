/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.IntSupplier;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.SikuliXception;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.Runner;

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

  PrintStream redirectedStdout;
  PrintStream redirectedStderr;

  protected void log(int level, String message, Object... args) {
    Debug.logx(level, getName() + "Runner: " + message, args);
  }

  private void logNotSupported(String method) {
    Debug.log(-1, "%s does not (yet) support %s", getName(), method);
  }

  @Override
  public final void init(String[] args) throws SikuliXception {
    synchronized (this) {
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
    synchronized (this) {
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

  public boolean canHandle(String identifier) {
    if (identifier != null) {
      if (getType().equals(identifier)) {
        return true;
      }
      if (hasExtension(identifier)) {
        return true;
      }
      if (hasExtension(FilenameUtils.getExtension(identifier))) {
        return true;
      }
      if (identifier.toLowerCase().equals(getName().toLowerCase())) {
        return true;
      }
      if (identifier.toLowerCase().startsWith(getName().toLowerCase() + "*")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public final void redirect(PrintStream stdout, PrintStream stderr) {
    synchronized (this) {
      Debug.log(3, "%s: Initiate IO redirect", getName());

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
      Debug.log(3, "%s: IO redirect established", getName());
    }
  }

  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    logNotSupported("IO redirection");
    return false;
  }

  @Override
  public final int runScript(String script, String[] scriptArgs, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();

    return synchronizedRunning(() -> {
      init(null);
      int savedLevel = Debug.getDebugLevel();
      if (!Debug.isGlobalDebug()) {
        Debug.off();
      }

      int exitValue = doRunScript(script, scriptArgs, options);
      Debug.setDebugLevel(savedLevel);
      return exitValue;
    });
  }

  protected int doRunScript(String scriptfile, String[] scriptArgs, IScriptRunner.Options options) {
    logNotSupported("runScript");
    return Runner.NOT_SUPPORTED;
  }

  @Override
  public final int evalScript(String script, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();
    return synchronizedRunning(() -> {
      init(null);
      return doEvalScript(script, options);
    });
  }

  protected int doEvalScript(String script, IScriptRunner.Options options) {
    logNotSupported("evalScript");
    return -1;
  }

  @Override
  public final void runLines(String lines, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();
    synchronizedRunning(() -> {
      init(null);
      doRunLines(lines, options);
      return 0;
    });
  }

  protected void doRunLines(String lines, IScriptRunner.Options options) {
    logNotSupported("runLines");
  }

  @Override
  public final int runTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, IScriptRunner.Options maybeOptions) {
    IScriptRunner.Options options = null != maybeOptions ? maybeOptions : new IScriptRunner.Options();
    return synchronizedRunning(() -> {
      init(null);
      return doRunTest(scriptfile, imagedirectory, scriptArgs, options);
    });
  }

  protected int doRunTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, IScriptRunner.Options options) {
    logNotSupported("runTest");
    return -1;
  }

  @Override
  public final int runInteractive(String[] scriptArgs) {
    return synchronizedRunning(() -> {
      init(null);
      return doRunInteractive(scriptArgs);
    });
  }

  protected int doRunInteractive(String[] scriptArgs) {
    logNotSupported("runInteractive");
    return -1;
  }

  @Override
  public String getCommandLineHelp() {
    logNotSupported("getCommandLineHelp");
    return null;
  }

  @Override
  public String getInteractiveHelp() {
    logNotSupported("getInteractiveHelp");
    return null;
  }

  @Override
  public final void close() {
    synchronized (this) {
      ready = false;
      doClose();
    }
  }

  protected void doClose() {
    // NOOP if not implemented
  }

  @Override
  public final void reset() {
    synchronized (this) {
      try {
        close();
        init(null);
        log(3, "reset requested (experimental: please report oddities)");
      } catch (Exception e) {
        log(-1, "reset requested but did not work. Please report this case." +
                "Do not run scripts anymore and restart the IDE after having saved your work");
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

  public final boolean isRunning() {
    return running;
  }

  public boolean isAbortSupported() {
    return false;
  }

  public final void abort() {
    if(running && isAbortSupported()) {
      doAbort();
    }
  }

  protected void doAbort() {
    // NOOP if not implemented
  }

  private int synchronizedRunning(IntSupplier block) {
    synchronized (this) {
      try {
        running = true;
        return block.getAsInt();
      } finally {
        running = false;
      }
    }
  }
}
