/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support.runner;

import org.sikuli.basics.Debug;
import org.sikuli.ide.SikulixIDE;
import org.sikuli.support.ide.JythonSupport;
import org.sikuli.script.Sikulix;
import org.sikuli.support.Commons;

import java.io.File;
import java.io.PrintStream;
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

  private int lvl = 3;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSupported() {
    //TODO JythonSupport to be revised (no side effects, if Jython not available): return JythonSupport.isSupported();
    try {
      Class.forName("org.python.util.PythonInterpreter");
      return true;
    } catch (ClassNotFoundException ex) {
      Debug.log(-1, "no Jython on classpath --- consult the docs for a solution, if needed");
      return false;
    }
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
      Commons.startLog(1, "JythonRunner: starting initialization (%4.1f sec)", Commons.getSinceStart());

      jythonSupport = JythonSupport.get();
      jythonSupport.getSysPath();
      if (!Commons.isRunningFromJar()) {
        jythonSupport.putSysPath(new File(Commons.getMainClassLocation(), "LIB").getAbsolutePath(), 0);
      }
      jythonSupport.addSitePackages();
      jythonSupport.showSysPath();
      jythonSupport.interpreterExecString("import sys");
      jythonSupport.interpreterExecString("import org.sikuli.support.ide.Runner as Runner");
      String interpreterVersion = jythonSupport.interpreterEval("sys.version.split(\"(\")[0]\n").toString();
      if (interpreterVersion.isEmpty()) {
        interpreterVersion = "could not be evaluated";
      }
      Commons.startLog(3, "Jython ready: version %s (%4.1f sec)", interpreterVersion, Commons.getSinceStart());
      SikulixIDE.showAfterStart();
    }
  }

  private void initAbort() {
    jythonSupport.interpreterExecString("sx_runner_sx = Runner.getRunner(\"" + NAME + "\")\n"
                                      + "def trace_calls_for_abort(frame, evt, arg):\n"
                                      + "  if sx_runner_sx.isAborted():\n"
                                      + "    raise RuntimeError(\"Aborted\")\n"
                                      + "  return trace_calls_for_abort\n"
                                      + "sys.settrace(trace_calls_for_abort)");
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
  protected int doEvalScript(String script, IRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      initAbort();
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
  protected int doRunScript(String scriptFile, String[] argv, IRunner.Options options) {

    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      initAbort();

      File pyFile = new File(scriptFile);

      jythonSupport.interpreterFillSysArgv(pyFile, argv);
      jythonSupport.executeScriptHeader(codeBefore);

      int exitCode = 0;

      try {
        if (scriptFile.endsWith("$py.class")) {
          jythonSupport.interpreterExecCode(new File(scriptFile));
        } else {
          jythonSupport.interpreterExecFile(pyFile.getAbsolutePath());
        }
      } catch (Throwable scriptException) {
        if(!isAborted()) {
          exitCode = 1;
          java.util.regex.Pattern p = java.util.regex.Pattern.compile("SystemExit: (-?[0-9]+)");
          String exception = scriptException.toString();

          Matcher matcher = p.matcher(exception);
          if (matcher.find()) {
            exitCode = Integer.parseInt(matcher.group(1));
            Debug.info("Exit code: " + exitCode);
          } else {
            int errorExit = jythonSupport.findErrorSource(scriptException, pyFile.getAbsolutePath());
            if (null != options) {
              options.setErrorLine(errorExit);
            }
          }
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
  protected void doRunLines(String lines, IRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      initAbort();
      jythonSupport.executeScriptHeader(codeBefore);

      try {
        jythonSupport.interpreterExecString(lines);
      } catch (Exception ex) {
        if(!isAborted()) {
          log(-1, "runPython: (%s) raised: %s", "\n" + lines, ex);
        }
      }
    }
  }
  //</editor-fold>

  //<editor-fold desc="20 redirect">
  private static boolean redirected = false;

  @Override
  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    //TODO extra redirect for interpreter needed? for what?
    // Since we have a static interpreter, we have to synchronize class wide
//    synchronized (JythonRunner.class) {
//      if (!redirected) {
//        redirected = true;
//        return jythonSupport.interpreterRedirect(stdout, stderr);
//      }
//      return true;
//    }
    return true;
  }
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
