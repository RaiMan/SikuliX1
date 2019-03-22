/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.python.core.PyList;
import org.python.util.PythonInterpreter;
import org.python.util.jython;
import org.sikuli.basics.Debug;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.RunTime;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runnerHelpers.JythonHelper;

/**
 * Executes Sikuliscripts written in Python/Jython.
 */

public class JythonRunner extends AbstractScriptRunner {

  public static final String NAME = "Jython";
  public static final String TYPE = "text/python";
  public static final String[] EXTENSIONS = new String[] { "py" };

  private static RunTime runTime = RunTime.get();

  private int lvl = 3;

  //TODO Refactoring to make JythonHelper non global or get rid of it entirely.
  /*
   * The PythonInterpreter instance
   *
   * Currently this has to be static because JythonHelper is a global object and
   * takes the interpreter to work with. Having multiple interpreters in the same
   * VM doesn't work.
   */
  protected PythonInterpreter getInterpreter() {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (interpreter == null) {
        sysargv.add("");
        PythonInterpreter.initialize(System.getProperties(), null, sysargv.toArray(new String[0]));
        interpreter = new PythonInterpreter();
      }
      return interpreter;
    }
  }

  private static PythonInterpreter interpreter = null;


  protected JythonHelper getHelper() {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (helper == null) {
        helper = JythonHelper.set(getInterpreter());
      }
      return helper;
    }
  }

  private static JythonHelper helper = null;

  /**
   * sys.argv for the jython script
   */
  private ArrayList<String> sysargv = new ArrayList<String>();

  @Override
  protected void doInit(String[] param) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      log(lvl, "starting initialization");
      getInterpreter();
      getHelper();

      helper.getSysPath();
      String fpAPILib = runTime.fSikulixLib.getAbsolutePath();
      helper.putSysPath(fpAPILib, 0);
      helper.setSysPath();
      helper.addSitePackages();
      helper.showSysPath();
      interpreter.exec("import sys");
      Debug.setWithTimeElapsed();
      log(lvl, "ready: version %s", interpreter.eval("sys.version.split(\"(\")[0]\n").toString());
      Debug.unsetWithTimeElapsed();
    }
  }

  @Override
  protected void doRunLines(String lines, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (lines.contains("\n")) {
        if (lines.startsWith(" ") || lines.startsWith("\t")) {
          lines = "if True:\n" + lines;
        }
      }
      try {
        interpreter.exec(lines);
      } catch (Exception ex) {
        log(-1, "runPython: (%s) raised: %s", lines, ex);
      }
    }
  }

  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      interpreter.exec(script);
      return 0;
    }
  }

  /**
   * Executes the jythonscript
   *
   * @param scriptFile
   * @param argv    arguments to be populated into sys.argv
   * @param options
   * @return The exitcode
   */
  @Override
  protected int doRunScript(String scriptFile, String[] argv, IScriptRunner.Options options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      File pyFile = new File(scriptFile);
      sysargv = new ArrayList<String>();
      if (pyFile != null) {
        sysargv.add(pyFile.getAbsolutePath());
      }
      if (argv != null) {
        sysargv.addAll(Arrays.asList(argv));
      }
      executeScriptHeader();
      int exitCode = 0;
      try {
        interpreter.execfile(pyFile.getAbsolutePath());
      } catch (Exception scriptException) {
        exitCode = 1;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("SystemExit: (-?[0-9]+)");
        Matcher matcher = p.matcher(scriptException.toString());
        if (matcher.find()) {
          exitCode = Integer.parseInt(matcher.group(1));
          Debug.info("Exit code: " + exitCode);
        } else {
          int errorExit = helper.findErrorSource(scriptException, pyFile.getAbsolutePath());
          if (null != options) {
            options.setErrorLine(errorExit);
          }
        }
      }
      if (System.out.checkError()) {
        Sikulix.popError("System.out is broken (console output)!" + "\nYou will not see any messages anymore!"
                + "\nSave your work and restart the IDE!", "Fatal Error");
      }
      return exitCode;
    }
  }

//TODO revise the before/after concept (to support IDE reruns)

  /**
   * {@inheritDoc}
   */
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

  private ArrayList<String> codeBefore = null;

  /**
   * {@inheritDoc}
   */
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

  private ArrayList<String> codeAfter = null;

  private void executeScriptHeader() {
    for (String line : SCRIPT_HEADER) {
      log(lvl + 1, "executeScriptHeader: %s", line);
      interpreter.exec(line);
    }
    if (codeBefore != null) {
      for (String line : codeBefore) {
        interpreter.exec(line);
      }
    }

    PyList jyargv = interpreter.getSystemState().argv;
    jyargv.clear();
    for (String item : sysargv) {
      jyargv.add(item);
    }
  }

  /**
   * The header commands, that are executed before every script
   */
  private static String[] SCRIPT_HEADER = new String[] {
          "# -*- coding: utf-8 -*- ",
          "import org.sikuli.script.SikulixForJython",
          "from sikuli import *",
          "use() #resetROI()",
          "setShowActions(False)" };

  /**
   * {@inheritDoc}
   */
  @Override
  protected int doRunInteractive(String[] argv) {
    String[] jy_args = null;
    String[] iargs = { "-i", "-c",
        "from sikuli import *; ScriptingSupport.runningInteractive(); use(); "
            + "print \"Hello, this is your interactive Sikuli (rules for interactive Python apply)\\n"
            + "use the UP/DOWN arrow keys to walk through the input history\\n"
            + "help()<enter> will output some basic Python information\\n" + "... use ctrl-d to end the session\"" };
    if (argv != null && argv.length > 0) {
      jy_args = new String[argv.length + iargs.length];
      System.arraycopy(iargs, 0, jy_args, 0, iargs.length);
      System.arraycopy(argv, 0, jy_args, iargs.length, argv.length);
    } else {
      jy_args = iargs;
    }
    jython.main(jy_args);
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getInteractiveHelp() {
    return "**** this might be helpful ****\n" + "-- execute a line of code by pressing <enter>\n"
        + "-- separate more than one statement on a line using ;\n"
        + "-- Unlike the iDE, this command window will not vanish, when using a Sikuli feature\n"
        + "   so take care, that all you need is visible on the screen\n" + "-- to create an image interactively:\n"
        + "img = capture()\n" + "-- use a captured image later:\n" + "click(img)";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCommandLineHelp() {
    return "You are using the Jython ScriptRunner";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSupported() {
    try {
      Class.forName("org.python.util.PythonInterpreter");
      return true;
    } catch (ClassNotFoundException ex) {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void doClose() {
    if (interpreter != null) {
      try {
        interpreter.close();
      } catch (Exception e) {
      }
      interpreter = null;
      redirected = false;
    }
  }

  @Override
  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      if (!redirected) {
        redirected = true;
        PythonInterpreter py = getInterpreter();
        try {
          py.setOut(stdout);
        } catch (Exception e) {
          log(-1, "%s: redirect STDOUT: %s", getName(), e.getMessage());
          return false;
        }
        try {
          py.setErr(stderr);
        } catch (Exception e) {
          log(-1, "%s: redirect STDERR: %s", getName(), e.getMessage());
          return false;
        }
      }
      return true;
    }
  }

  private static boolean redirected = false;

// TODO SikuliToHtmlConverter implement in Java
/*
  final static InputStream SikuliToHtmlConverter
          = JythonScriptRunner.class.getResourceAsStream("/scripts/sikuli2html.py");
  static String pyConverter
          = FileManager.convertStreamToString(SikuliToHtmlConverter);
  private void convertSrcToHtml(String bundle) {
    PythonInterpreter py = new PythonInterpreter();
    log(lvl, "Convert Sikuli source code " + bundle + " to HTML");
    py.set("local_convert", true);
    py.set("sikuli_src", bundle);
    py.exec(pyConverter);
  }
*/

}
