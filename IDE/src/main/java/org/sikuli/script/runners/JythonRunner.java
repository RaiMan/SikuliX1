/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import org.sikuli.basics.Debug;
import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runnerSupport.JythonSupport;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.support.Commons;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Executes Sikuliscripts written in Python/Jython.
 */

public class JythonRunner extends AbstractLocalFileScriptRunner {

  //<editor-fold desc="00 standard runner stuff">
  public static final String NAME = "Jython";
  public static final String TYPE = "text/jython";
  public static final String[] EXTENSIONS = new String[]{"py"};
  static String interpreterVersion = null;

  private final int lvl = 3;

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
      if (interpreterVersion == null) {
        log(3, "starting initialization");
        interpreterVersion = "";
        jythonSupport = JythonSupport.get();
        if (jythonSupport.isReady()) {
          jythonSupport.getSysPath();
          if (!Commons.isRunningFromJar()) {
            jythonSupport.putSysPath(new File(Commons.getMainClassLocation(), "Lib").getAbsolutePath(), 0);
          }
          jythonSupport.addSitePackages();
          jythonSupport.showSysPath();
          try {
            jythonSupport.interpreterExecString("import sys");
            jythonSupport.interpreterExecString("import org.sikuli.script.runnerSupport.Runner as Runner");
            interpreterVersion = ("" + jythonSupport.interpreterEval("sys.version")).split(" ")[0];
          } catch (Exception e) {
          }
          if (interpreterVersion.isEmpty()) {
            log(-1, "not ready --- scripts cannot be run");
          } else {
            log(3, "ready: %s", interpreterVersion);
          }
        } else {
          log(-1, "JythonSupport not ready --- scripts cannot be run");
        }
      }
    }
  }

  private void initAbort() {
    String script = "sx_runner_sx = Runner.getRunner(\"" + NAME + "\")\n"
        + "def trace_calls_for_abort(frame, evt, arg):\n"
        + "  if sx_runner_sx.isAborted():\n"
        + "    raise RuntimeError(\"Aborted\")\n"
        + "  return trace_calls_for_abort\n"
        + "sys.settrace(trace_calls_for_abort)";
    jythonSupport.interpreterExecString(script);
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

  public List<String> getImportPlaces() {
    if (jythonSupport == null) {
      return new ArrayList<>();
    }
    List<String> places = jythonSupport.getSysPathAsList();
    return places;
  }
  //</editor-fold>

  //<editor-fold desc="10 run / eval">
  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (!jythonSupport.isReady()) {
        jythonSupport.log(-1, "No PythonInterpreter --- scripts cannot be run");
        return 0;
      }
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
  protected int doRunScript(String scriptFile, String[] argv, IScriptRunner.Options options) {

    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (!jythonSupport.isReady()) {
        jythonSupport.log(-1, "No PythonInterpreter --- scripts cannot be run");
        return 0;
      }
      initAbort();

      File pyFile = new File(scriptFile);
      log(3, "before: %s", pyFile);

      jythonSupport.interpreterFillSysArgv(pyFile, argv);
      jythonSupport.executeScriptHeader(codeBefore);

      int exitCode = 0;

      try {
        if (scriptFile.endsWith("$py.class")) {
          jythonSupport.interpreterExecCode(pyFile);
        } else {
          jythonSupport.interpreterExecFile(pyFile.getAbsolutePath());
        }
      } catch (Throwable scriptException) {
        if (!isAborted()) {
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
//TODO exit exception not reported to outside???
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

      Debug.setDebugLevel(savedLevel);
      log(3, "after: %s (returns: %d)", pyFile, exitCode);
      return exitCode;
    }
  }

  private static List<String> importPlacesBeforeRun;

  public void beforeScriptRun(String script, Options options) {
    importPlacesBeforeRun = getImportPlaces();
  }

  public void afterScriptRun(String script, Options options) {
    jythonSupport.resetSysPath(importPlacesBeforeRun);
  }

  @Override
  protected void doRunLines(String lines, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (!jythonSupport.isReady()) {
        jythonSupport.log(-1, "No PythonInterpreter --- scripts cannot be run");
        return;
      }
      initAbort();
      jythonSupport.executeScriptHeader(codeBefore);

      try {
        jythonSupport.interpreterExecString(lines);
      } catch (Exception ex) {
        if (!isAborted()) {
          log(-1, "runPython: (%s) raised: %s", "\n" + lines, ex);
        }
      }
    }
  }
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

  public void adjustImportPath(Map<String, String> files, IScriptRunner.Options options) {
    if (files.get("isBundle").equals("true")) {
      List<String> importPlaces = getImportPlaces();
      if (importPlaces.size() == 0) {
        return;
      }
      String syspath0 = importPlaces.get(0);
      String importFrom = files.get("folder");
      if (!importFrom.equals(syspath0)) {
/*
      if (importFrom.endsWith(".sikuli")) {
        importFrom = new File(importFrom).getParent();
      }
*/
        jythonSupport.getSysPath();
        jythonSupport.replaceSysPath(importFrom, 0);
        List<String> syspath = getImportPlaces();
        syspath0 = syspath.get(0);
      }
      log(lvl, "syspath[0]: %s", syspath0);
    }
  }
}
