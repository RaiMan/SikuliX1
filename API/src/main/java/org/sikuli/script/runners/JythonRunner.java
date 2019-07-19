/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.python.core.BytecodeLoader;
import org.python.core.PyCode;
import org.python.core.PyList;
import org.python.util.PythonInterpreter;
import org.sikuli.basics.Debug;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runnerHelpers.JythonHelper;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

/**
 * Executes Sikuliscripts written in Python/Jython.
 */

public class JythonRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "Jython";
  public static final String TYPE = "text/jython";
  public static final String[] EXTENSIONS = new String[] { "py" };

  private static RunTime runTime = RunTime.get();

  private int lvl = 3;

  // TODO Refactoring to make JythonHelper non global or get rid of it entirely.
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
      final String normalizedLines = normalizePartialScript(lines);

      executeScriptHeader();

      try {
        interpreter.exec(normalizedLines);
      } catch (Exception ex) {
        log(-1, "runPython: (%s) raised: %s", "\n" + normalizedLines, ex);
      }
    }
  }

  private static final Pattern[] IF_START_PATTERNS = new Pattern[] { Pattern.compile("\\s*if.*:\\s*") };

  private static final Pattern[] IF_END_PATTERNS = new Pattern[] { Pattern.compile("\\s*elif.*:\\s*"),
      Pattern.compile("\\s*else\\s*:\\s*") };

  private static final Pattern[] TRY_START_PATTERNS = new Pattern[] { Pattern.compile("\\s*try\\s*:\\s*") };

  private static final Pattern[] TRY_END_PATTERNS = new Pattern[] { Pattern.compile("\\s*except.*:\\s*"),
      Pattern.compile("\\s*finally\\s*:\\s*") };

  private static final String GENERATED_MARKER = " # line generated";

  /*
   * Normalizes a partial script passed to runLines.
   */
  private String normalizePartialScript(String script) {
    List<String> lines = getLines(script);

    lines = stripComments(lines);
    lines = normalizeIndentation(lines);

    String indentation = detectIndentation(script);
    lines = fixLastLine(lines, indentation);
    lines = fixUnclosedTryBlock(lines, indentation);
    lines = fixUnopenedBlock(lines, TRY_START_PATTERNS, TRY_END_PATTERNS, "try:", indentation);
    lines = fixUnopenedBlock(lines, IF_START_PATTERNS, IF_END_PATTERNS, "if True:", indentation);
    lines = fixFirstLine(lines);

    return String.join("\n", lines) + "\n";
  }

  private static final Pattern COMMENT_PATTERN = Pattern.compile("\\s*#.*");

  private List<String> stripComments(List<String> lines) {
    return lines.stream().filter((line) -> !COMMENT_PATTERN.matcher(line).matches()).collect(Collectors.toList());
  }

  /*
   * Remove unnecessary indentation.
   *
   * In the example it removed the first 2 spaces on each line.
   *
   * ---
   *   if True:
   *     hello("world") ---
   */
  private List<String> normalizeIndentation(List<String> lines) {
    while (true) {
      for (String line : lines) {
        if (detectIndentation(line).isEmpty()) {
          return lines;
        }
      }

      lines = lines.stream().map((line) -> line.substring(1)).collect(Collectors.toList());
    }
  }

  /*
   * Fixes for example the following:
   *
   * ---
   * try:
   *   print("hello")
   * ---
   *
   * Also handle nested tries and adds the required number of excepts with the
   * corresponding indentation.
   *
   */
  private List<String> fixUnclosedTryBlock(List<String> lines, String indentation) {
    lines = new ArrayList<>(lines);

    List<String> lineIndentations = new LinkedList<>();

    for (String line : lines) {
      if (lineMatches(line, TRY_START_PATTERNS)) {
        lineIndentations.add(detectIndentation(line));
      } else if (lineMatches(line, TRY_END_PATTERNS)) {
        if (!lineIndentations.isEmpty()) {
          String lineIndentation = detectIndentation(line);
          if (lineIndentation.equals(lineIndentations.get(lineIndentations.size() - 1))) {
            lineIndentations.remove(lineIndentations.size() - 1);
          }
        }
      }
    }

    Collections.reverse(lineIndentations);

    for (String lineIndentation : lineIndentations) {
      lines.add(lineIndentation + "except:" + GENERATED_MARKER);
      lines.add(lineIndentation + indentation + "raise" + GENERATED_MARKER);
    }

    return lines;
  }

  /*
   * Fixes for example the following:
   *
   * ---
   *     print("foo")
   *   else print("bar")
   * except:
   *   print("error") ---
   *
   * Creates the required try or if to get a valid block. Also works with nested
   * blocks.
   */
  private List<String> fixUnopenedBlock(List<String> lines, Pattern[] startPatterns, Pattern[] endPatterns,
      String startExpression, String indentation) {
    lines = new ArrayList<>(lines);

    List<Integer> lineNumbers = new ArrayList<>();
    List<String> lineIndentations = new ArrayList<>();

    for (int lineNumber = lines.size() - 1; lineNumber >= 0; lineNumber--) {
      String line = lines.get(lineNumber);
      if (lineMatches(line, endPatterns)) {
        String lineIndentation = detectIndentation(line);
        if (lineIndentations.isEmpty() || !lineIndentation.equals(lineIndentations.get(0))) {
          lineNumbers.add(0, lineNumber);
          lineIndentations.add(0, lineIndentation);
        } else {
          lineNumbers.set(0, lineNumber);
          lineIndentations.set(0, lineIndentation);
        }
      } else if (lineMatches(line, startPatterns)) {
        if (!lineNumbers.isEmpty()) {
          lineNumbers.remove(0);
          lineIndentations.remove(0);
        }
      }
    }

    int insertCount = 0;

    for (int i = lineNumbers.size() - 1; i >= 0; i--) {
      int lineNumber = lineNumbers.get(i) + (insertCount++);
      String lineIndentation = lineIndentations.get(i);

      int index = 0;

      for (int n = lineNumber - 1; n >= 0; n--) {
        String line = lines.get(n);

        if (!line.trim().isEmpty() && detectIndentation(line).length() < lineIndentation.length()) {
          index = n + 1;
          break;
        }
      }
      String newLine = lineIndentation + startExpression + GENERATED_MARKER;

      if (index == lineNumber) {
        newLine += "\n" + lineIndentation + indentation + "pass" + GENERATED_MARKER;
      }

      lines.add(index, newLine);
    }

    return lines;
  }

  /*
   * Checks if last line is one of the start/end patterns and makes it a valid
   * statement.
   */
  private List<String> fixLastLine(List<String> lines, String indentation) {
    lines = new ArrayList<>(lines);

    String lastLine = lines.get(lines.size() - 1);

    if (lineMatches(lastLine, TRY_END_PATTERNS)) {
      lines.add(detectIndentation(lastLine) + indentation + "raise" + GENERATED_MARKER);
    } else if (lineMatches(lastLine, IF_START_PATTERNS) || lineMatches(lastLine, IF_END_PATTERNS)
        || lineMatches(lastLine, TRY_START_PATTERNS)) {
      lines.add(detectIndentation(lastLine) + indentation + "pass" + GENERATED_MARKER);
    }

    return lines;
  }

  private List<String> fixFirstLine(List<String> lines) {
    lines = new ArrayList<>(lines);

    if (!detectIndentation(lines.get(0)).isEmpty()) {
      lines.add(0, "if True:" + GENERATED_MARKER);
    }

    return lines;
  }

  /*
   * Checks if the given line matches at least one of the given patterns.
   */
  private boolean lineMatches(String line, Pattern[] patterns) {
    return Arrays.asList(patterns).stream().anyMatch((pattern) -> pattern.matcher(line).matches());
  }

  private static final Pattern INDENTATION_PATTERN = Pattern.compile("(\\s+).+?");

  /*
   * Detects the shortest sequence of whitespaces with length > 0). If there is no
   * such sequence, it returns an empty String.
   */
  private String detectIndentation(String script) {
    String indentation = "";

    for (String line : getLines(script)) {
      Matcher m = INDENTATION_PATTERN.matcher(line);
      if (m.matches()) {
        if (indentation.isEmpty() || m.group(1).length() < indentation.length()) {
          indentation = m.group(1);
        }
      }
    }
    return indentation;
  }

  private List<String> getLines(String script) {
    return Arrays.asList(script.split("\n"));
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
   * @param argv       arguments to be populated into sys.argv
   * @param options
   * @return The exitcode
   */
  @Override
  protected int doRunScript(String scriptFile, String[] argv, IScriptRunner.Options options) {

    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JythonRunner.class) {
      File pyFile = new File(scriptFile);
      sysargv = new ArrayList<String>();
      sysargv.add(pyFile.getAbsolutePath());
      if (argv != null) {
        sysargv.addAll(Arrays.asList(argv));
      }

      executeScriptHeader();

      prepareFileLocation(pyFile, options);

      int exitCode = 0;

      try {
        if (scriptFile.endsWith("$py.class")) {
          byte[] data = FileUtils.readFileToByteArray(new File(scriptFile));

          PyCode code = BytecodeLoader.makeCode(FilenameUtils.getBaseName(scriptFile), data, scriptFile);

          interpreter.exec(code);
        } else {
          interpreter.execfile(pyFile.getAbsolutePath());
        }

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
      } finally {
        interpreter.cleanup();
      }

      if (System.out.checkError()) {
        Sikulix.popError("System.out is broken (console output)!" + "\nYou will not see any messages anymore!"
            + "\nSave your work and restart the IDE!", "Fatal Error");
      }

      return exitCode;
    }
  }

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
      "use() #resetROI()"
  };

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

}
