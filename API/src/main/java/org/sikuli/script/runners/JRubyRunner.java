/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import org.sikuli.basics.Debug;
import org.sikuli.script.runnerSupport.JRubySupport;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.Runner;

import java.io.*;
import java.util.regex.Matcher;

public class JRubyRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "JRuby";
  public static final String TYPE = "text/ruby";
  public static final String[] EXTENSIONS = new String[]{"rb"};

  //<editor-fold desc="00 initialization">
  @Override
  public boolean isAbortSupported() {
    return true;
  }

  @Override
  public boolean isSupported() {
    return jrubySupport.isSupported();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS.clone();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void doClose() {
    jrubySupport.interpreterTerminate();
    redirected = false;
  }

  static JRubySupport jrubySupport = null;

  private int lvl = 3;

  @Override
  protected void doInit(String[] args) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      if (null == jrubySupport) {
        jrubySupport = JRubySupport.get();
        // execute script headers to already do the warmup during init
        jrubySupport.executeScriptHeader(codeBefore);
      }
    }
  }
  //</editor-fold>

  //<editor-fold desc="10 run scripts">
  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    Integer exitCode = 0;
    Object exitValue = null;
    synchronized (JRubyRunner.class) {
      File rubyFile = new File(new File(scriptFile).getAbsolutePath());
      jrubySupport.fillSysArgv(rubyFile, scriptArgs);

      jrubySupport.executeScriptHeader(codeBefore,
              rubyFile.getParentFile().getAbsolutePath(),
              rubyFile.getParentFile().getParentFile().getAbsolutePath());

      prepareFileLocation(rubyFile, options);

      BufferedReader scriptReader = null;
      try {
        scriptReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(rubyFile.getAbsolutePath()), "UTF-8"));
      } catch (IOException ex) {
        log(-1, "reading script: %s", ex.getMessage());
        return Runner.FILE_NOT_FOUND;
      }
      try {
        //TODO handle exitValue (the result of the last line in the script or the value returned by statement return something)
        exitValue = jrubySupport.interpreterRunScriptletFile(scriptReader, rubyFile.getAbsolutePath());
      } catch (Throwable scriptException) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("SystemExit: ([0-9]+)");
        Matcher matcher = p.matcher(scriptException.toString());
        if (matcher.find()) {
          exitCode = Integer.parseInt(matcher.group(1));
          Debug.info("Exit code: " + exitCode);
        } else {
          //TODO to be optimized (avoid double message)
          int errorExit = jrubySupport.findErrorSource(scriptException, rubyFile.getAbsolutePath());
          options.setErrorLine(errorExit);
        }
      }
      return exitCode;
    }

  }

  @Override
  protected void doRunLines(String lines, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      try {
        jrubySupport.interpreterRunScriptletString(lines);
      } catch (Exception ex) {
      }
    }
  }

  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      jrubySupport.executeScriptHeader(codeBefore);
      jrubySupport.interpreterRunScriptletString(script);
      return 0;
    }
  }
  //</editor-fold>

  //<editor-fold desc="20 redirect">
  @Override
  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      if (!redirected) {
        redirected = true;
        return jrubySupport.interpreterRedirect(stdout, stderr);
      }
      return true;
    }
  }

  private static boolean redirected = false;
  //</editor-fold>
}
