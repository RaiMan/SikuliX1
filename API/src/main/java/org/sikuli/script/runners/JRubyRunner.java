/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.runnerSupport.JRubySupport;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;

public class JRubyRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "JRuby";
  public static final String TYPE = "text/ruby";
  public static final String[] EXTENSIONS = new String[]{"rb"};

  static JRubySupport jrubySupport = null;

  private int lvl = 3;

  @Override
  protected void doInit(String[] args) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      if (null == jrubySupport) {
        jrubySupport = JRubySupport.get();
        // execute script headers to already do the warmup during init
        executeScriptHeader(new String[]{});
      }
    }
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    Integer exitCode = 0;
    Object exitValue = null;
    synchronized (JRubyRunner.class) {
      File rubyFile = new File(new File(scriptFile).getAbsolutePath());
      jrubySupport.fillSysArgv(rubyFile, scriptArgs);

      executeScriptHeader(new String[]{rubyFile.getParentFile().getAbsolutePath(),
              rubyFile.getParentFile().getParentFile().getAbsolutePath()});

      prepareFileLocation(rubyFile, options);

      BufferedReader scriptReader = null;
      try {
        scriptReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(rubyFile.getAbsolutePath()), "UTF-8"));
      } catch (IOException ex) {
      }
      try {
        //TODO what is exitValue?
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
      executeScriptHeader(new String[0]);
      jrubySupport.interpreterRunScriptletString(script);
      return 0;
    }
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

  /**
   * Executes the defined header for the jruby script.
   *
   * @param syspaths List of all syspath entries
   */
  private void executeScriptHeader(String[] syspaths) {
    List<String> path = jrubySupport.interpreterGetLoadPaths();
    if (null == path) {
      return;
    }
    String sikuliLibPath = RunTime.get().fSikulixLib.getAbsolutePath();
    if (path.size() == 0 || !FileManager.pathEquals(path.get(0), sikuliLibPath)) {
      log(lvl, "executeScriptHeader: adding SikuliX Lib path to sys.path\n" + sikuliLibPath);
      int pathLength = path.size();
      String[] pathNew = new String[pathLength + 1];
      pathNew[0] = sikuliLibPath;
      for (int i = 0; i < pathLength; i++) {
        pathNew[i + 1] = path.get(i);
      }
      for (int i = 0; i < pathLength; i++) {
        path.set(i, pathNew[i]);
      }
      path.add(pathNew[pathNew.length - 1]);
    }
    if (savedpathlen == 0) {
      savedpathlen = jrubySupport.interpreterGetLoadPaths().size();
    }
    while (jrubySupport.interpreterGetLoadPaths().size() > savedpathlen) {
      jrubySupport.interpreterGetLoadPaths().remove(savedpathlen);
    }
    for (String syspath : syspaths) {
      path.add(FileManager.slashify(syspath, false));
    }

    jrubySupport.interpreterRunScriptletString(SCRIPT_HEADER);

    if (codeBefore != null) {
      StringBuilder buffer = new StringBuilder();
      for (String line : codeBefore) {
        buffer.append(line);
      }
      jrubySupport.interpreterRunScriptletString(buffer.toString());
    }
  }

  private static int savedpathlen = 0;
  private final static String SCRIPT_HEADER =
          "# coding: utf-8\n"
                  + "require 'Lib/sikulix'\n"
                  + "include Sikulix\n";

  @Override
  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      if (!redirected) {
        redirected = true;
        return jrubySupport.doRedirect(stdout, stderr);
      }
      return true;
    }
  }

  private static boolean redirected = false;

  @Override
  public boolean isAbortSupported() {
    return true;
  }

}
