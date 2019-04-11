/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.SikuliXception;

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

  boolean ready = false;

  PrintStream redirectedStdout;
  PrintStream redirectedStderr;

  protected void log(int level, String message, Object... args) {
    Debug.logx(level, getName() + ": " + message, args);
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
    // noop if not implemented
  }

  ;

  public final boolean isReady() {
    synchronized (this) {
      return ready;
    }
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
    return identifier != null && (
            identifier.toLowerCase().equals(getName().toLowerCase()) ||
                    identifier.toLowerCase().startsWith(getName().toLowerCase() + "*") ||
                    getType().equals(identifier) ||
                    hasExtension(identifier) ||
                    (new File(identifier).exists() && hasExtension(FilenameUtils.getExtension(identifier))));
  }

  ;

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
  public final int runScript(String scriptfile, String[] scriptArgs, IScriptRunner.Options options) {
    synchronized (this) {
      init(null);
      int savedLevel = Debug.getDebugLevel();
      if (!Debug.isGlobalDebug()) {
        Debug.off();
      }
      int exitValue = doRunScript(scriptfile, scriptArgs, options);
      Debug.setDebugLevel(savedLevel);
      return exitValue;
    }
  }

  protected int doRunScript(String scriptfile, String[] scriptArgs, IScriptRunner.Options options) {
    logNotSupported("runScript");
    return -1;
  }

  @Override
  public final int evalScript(String script, IScriptRunner.Options options) {
    synchronized (this) {
      init(null);
      return doEvalScript(script, options);
    }
  }

  protected int doEvalScript(String script, IScriptRunner.Options options) {
    logNotSupported("evalScript");
    return -1;
  }

  @Override
  public final void runLines(String lines, IScriptRunner.Options options) {
    synchronized (this) {
      init(null);
      doRunLines(lines, options);
    }
  }

  protected void doRunLines(String lines, IScriptRunner.Options options) {
    logNotSupported("runLines");
  }

  @Override
  public final int runTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, IScriptRunner.Options options) {
    synchronized (this) {
      init(null);
      return doRunTest(scriptfile, imagedirectory, scriptArgs, options);
    }
  }

  protected int doRunTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, IScriptRunner.Options options) {
    logNotSupported("runTest");
    return -1;
  }

  @Override
  public final int runInteractive(String[] scriptArgs) {
    synchronized (this) {
      init(null);
      return doRunInteractive(scriptArgs);
    }
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
  public boolean isSupported() {
    return false;
  }

  @Override
  public final void close() {
    synchronized (this) {
      ready = false;
      doClose();
    }
  }

  protected void doClose() {
    // noop if not implemented
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
    // noop if not implemented
  }

  @Override
  public void execAfter(String[] stmts) {
    // noop if not implemented
  }
}
