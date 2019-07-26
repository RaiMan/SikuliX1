/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runnerSupport.JythonSupport;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * Executes Sikuliscripts written in Python/Jython.
 */

public class JythonRunner extends AbstractLocalFileScriptRunner {

  //<editor-fold desc="00 standard runner stuff">
  public static final String NAME = "Jython";
  public static final String TYPE = "text/jython";
  public static final String[] EXTENSIONS = new String[]{"py"};

  private static RunTime runTime = RunTime.get();

  private int lvl = 3;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSupported() {
    return jythonSupport.isSupported();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getExtensions() {
    return EXTENSIONS.clone();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public boolean isAbortSupported() {
    return true;
  }

  @Override
  public String[] getFileEndings() {
    String[] endings = super.getFileEndings();
    endings = Arrays.copyOf(endings, endings.length + 1);
    endings[endings.length - 1] = "$py.class";
    return endings;
  }

  @Override
  protected void doInit(String[] param) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      log(lvl, "starting initialization");

      jythonSupport = JythonSupport.get();
      jythonSupport.getSysPath();
      String fpAPILib = runTime.fSikulixLib.getAbsolutePath();
      jythonSupport.putSysPath(fpAPILib, 0);
      jythonSupport.setSysPath();
      jythonSupport.addSitePackages();
      jythonSupport.showSysPath();
      jythonSupport.interpreterExecString("import sys");
      String interpreterVersion = jythonSupport.interpreterEval("sys.version.split(\"(\")[0]\n").toString();

      Debug.setWithTimeElapsed();
      log(lvl, "ready: version %s", interpreterVersion);
      Debug.unsetWithTimeElapsed();
    }
  }

  static JythonSupport jythonSupport = null;

  /**
   * {@inheritDoc}
   */
  @Override
  public void doClose() {
    jythonSupport.interpreterClose();
    redirected = false;
  }
  //</editor-fold>

  //<editor-fold desc="10 run / eval">
  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      jythonSupport.interpreterExecString(script);
      return 0;
    }
  }

  /**
   * Executes the jythonscript
   *
   * @param scriptFile
   * @param argv       arguments to be populated into sys.argv
   * @param options
   * @return The exitcode
   */
  @Override
  protected int doRunScript(String scriptFile, String[] argv, IScriptRunner.Options options) {

    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      File pyFile = new File(scriptFile);

      jythonSupport.interpreterFillSysArgv(pyFile, argv);
      executeScriptHeader();

      prepareFileLocation(pyFile, options);

      int exitCode = 0;

      try {
        if (scriptFile.endsWith("$py.class")) {
          jythonSupport.interpreterExecCode(new File(scriptFile));
        } else {
          jythonSupport.interpreterExecFile(pyFile.getAbsolutePath());
        }
      } catch (Exception scriptException) {
        exitCode = 1;
        if (scriptException instanceof InvocationTargetException) {
          scriptException = (Exception) ((InvocationTargetException) scriptException).getTargetException();
          java.util.regex.Pattern p = java.util.regex.Pattern.compile("SystemExit: (-?[0-9]+)");
          Matcher matcher = p.matcher(scriptException.toString());
          if (matcher.find()) {
            exitCode = Integer.parseInt(matcher.group(1));
            Debug.info("Exit code: " + exitCode);
          } else {
            int errorExit = jythonSupport.findErrorSource(scriptException, pyFile.getAbsolutePath());
            if (null != options) {
              options.setErrorLine(errorExit);
            }
          }
        } else {
          log(-1, "exec script: %s", scriptException.getMessage());
        }
      } finally {
        jythonSupport.interpreterCleanup();
      }

      if (System.out.checkError()) {
        Sikulix.popError("System.out is broken (console output)!" + "\nYou will not see any messages anymore!"
                + "\nSave your work and restart the IDE!", "Fatal Error");
      }

      return exitCode;
    }
  }

  @Override
  protected void doRunLines(String lines, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {

      executeScriptHeader();

      try {
        jythonSupport.interpreterExecString(lines);
      } catch (Exception ex) {
        log(-1, "runPython: (%s) raised: %s", "\n" + lines, ex);
      }
    }
  }

  private void executeScriptHeader() {
    for (String line : SCRIPT_HEADER) {
      log(lvl + 1, "executeScriptHeader: %s", line);
      jythonSupport.interpreterExecString(line);
    }
    if (codeBefore != null) {
      for (String line : codeBefore) {
        jythonSupport.interpreterExecString(line);
      }
    }
  }

  /**
   * The header commands, that are executed before every script
   */
  private static String[] SCRIPT_HEADER = new String[]{
          "# -*- coding: utf-8 -*- ",
          "import org.sikuli.script.SikulixForJython",
          "from sikuli import *",
          "use() #resetROI()"
  };
  //</editor-fold>

  //<editor-fold desc="20 redirect">
  @Override
  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (!redirected) {
        redirected = true;
        return jythonSupport.interpreterRedirect(stdout, stderr);
      }
      return true;
    }
  }

  private static boolean redirected = false;
  //</editor-fold>

  //<editor-fold desc="99 SikuliToHtmlConverter">
  // TODO SikuliToHtmlConverter implement in Java
  /*
   * final static InputStream SikuliToHtmlConverter =
   * JythonScriptRunner.class.getResourceAsStream("/scripts/sikuli2html.py");
   * static String pyConverter =
   * FileManager.convertStreamToString(SikuliToHtmlConverter); private void
   * convertSrcToHtml(String bundle) { PythonInterpreter py = new
   * PythonInterpreter(); log(lvl, "Convert Sikuli source code " + bundle +
   * " to HTML"); py.set("local_convert", true); py.set("sikuli_src", bundle);
   * py.exec(pyConverter); }
   */
  //</editor-fold>

}
