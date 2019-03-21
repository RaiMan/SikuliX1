/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils.EvalUnit;
import org.jruby.runtime.ThreadContext;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.RunTime;

public class JRubyRunner extends AbstractScriptRunner {

  public static final String NAME = "jruby";
  public static final String TYPE = "text/ruby";
  public static final String[] EXTENSIONS = new String[] { "rb" };

  static RunTime sxRunTime = RunTime.get();

  private int lvl = 3;

  /**
   * The ScriptingContainer instance
   */
  private static ScriptingContainer interpreter = null;
  private static int savedpathlen = 0;
  /**
   * sys.argv for the jruby script
   */
  private static ArrayList<String> sysargv = null;
  /**
   * The header commands, that are executed before every script
   */
  private final static String SCRIPT_HEADER = "# coding: utf-8\n" + "require 'Lib/sikulix'\n" + "include Sikulix\n";

  private static ArrayList<String> codeBefore = null;
  private static ArrayList<String> codeAfter = null;
  /**
   * CommandLine args
   */
  private int errorLine;
  private int errorColumn;
  private String errorType;
  private String errorText;
  private int errorClass;
  private String errorTrace;

  private static final int PY_SYNTAX = 0;
  private static final int PY_RUNTIME = 1;
  private static final int PY_JAVA = 2;
  private static final int PY_UNKNOWN = -1;

  private static String sikuliLibPath;

  private static Ruby runtime;
  private static ThreadContext context;
  private static boolean redirected = false;

  @Override
  protected void doInit(String[] args) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      Debug.info("JRuby init");
      // TODO classpath and other path handlings
      sikuliLibPath = sxRunTime.fSikulixLib.getAbsolutePath();
      createScriptingContainer();
    }
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, Map<String, Object> options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      if (null == scriptFile) {
        // run the Ruby statements from argv (special for setup functional test)
        fillSysArgv(null, null);
        executeScriptHeader(new String[0]);
        return runRuby(null, scriptArgs, null);
      }
      File file = new File(new File(scriptFile).getAbsolutePath());
      fillSysArgv(file, scriptArgs);

      executeScriptHeader(new String[] { file.getParentFile().getAbsolutePath(),
          file.getParentFile().getParentFile().getAbsolutePath() });
      int exitCode = runRuby(file, null, new String[] { file.getParentFile().getAbsolutePath() });
      log(lvl + 1, "runScript: at exit: path:");
      for (Object p : interpreter.getLoadPaths()) {
        log(lvl + 1, "runScript: " + p.toString());
      }
      log(lvl + 1, "runScript: at exit: --- end ---");
      return exitCode;
    }
  }

  @Override
  protected int doEvalScript(String script, Map<String, Object> options) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      executeScriptHeader(new String[0]);
      interpreter.runScriptlet(script);
      return 0;
    }
  }

  @Override
  protected int doRunInteractive(String[] scriptArgs) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      fillSysArgv(null, scriptArgs);

      String[] args = null;
      String[] iargs = { /* "-i", "-c", */
          "require 'irb'\n" + "ScriptRunner.runningInteractive();\n"
              + "print \"Hello, this is your interactive Sikuli (rules for interactive Ruby apply)\\n"
              + "use the UP/DOWN arrow keys to walk through the input history\\n"
              + "help()<enter> will output some basic Ruby information\\n" + "... use ctrl-d to end the session\"\n"
              + "IRB.start(__FILE__)\n" };
      if (scriptArgs != null && scriptArgs.length > 0) {
        args = new String[scriptArgs.length + iargs.length];
        System.arraycopy(iargs, 0, args, 0, iargs.length);
        System.arraycopy(scriptArgs, 0, args, iargs.length, scriptArgs.length);
      } else {
        args = iargs;
      }
      StringBuilder buffer = new StringBuilder();
      for (String e : args) {
        buffer.append(e);
      }
      createScriptingContainer();
      executeScriptHeader(new String[0]);
      interpreter.runScriptlet(buffer.toString());
      return 0;
    }
  }

  @Override
  public String getCommandLineHelp() {
    return "You are using the JRuby ScriptRunner";
  }

  @Override
  public String getInteractiveHelp() {
    return "**** this might be helpful ****\n" + "-- execute a line of code by pressing <enter>\n"
        + "-- separate more than one statement on a line using ;\n"
        + "-- Unlike the iDE, this command window will not vanish, when using a Sikuli feature\n"
        + "   so take care, that all you need is visible on the screen\n" + "-- to create an image interactively:\n"
        + "img = capture()\n" + "-- use a captured image later:\n" + "click(img)";
  }

  @Override
  public boolean isSupported() {
    try {
      Class.forName("org.jruby.embed.ScriptingContainer");
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }

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
    // TODO this is currently a bad idea because it terminates the static
    // interpreter instance.
    if (interpreter != null) {
      interpreter.terminate();
      interpreter = null;
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

  private int runRuby(File ruFile, String[] stmts, String[] scriptPaths) {
    int exitCode = 0;
    String stmt = "";
    boolean fromIDE = false;
    String filename = "<script>";
    try {
      if (null == ruFile) {
        log(lvl, "runRuby: running statements");
        StringBuilder buffer = new StringBuilder();
        for (String e : stmts) {
          buffer.append(e);
        }
        interpreter.setScriptFilename(filename);
        interpreter.runScriptlet(buffer.toString());
      } else {
        filename = ruFile.getAbsolutePath();
        if (scriptPaths != null) {
          BufferedReader script = new BufferedReader(
              new InputStreamReader(new FileInputStream(ruFile.getAbsolutePath()), "UTF-8"));

          if (scriptPaths.length > 1) {
            filename = FileManager.slashify(scriptPaths[0], true) + scriptPaths[1] + ".sikuli";
            log(lvl, "runRuby: running script from IDE: \n" + filename);
            if (scriptPaths[0] == null) {
              filename = "";
            }
            fromIDE = true;
          } else {
            filename = scriptPaths[0];
            log(lvl, "runRuby: running script: \n" + filename);
          }
          interpreter.runScriptlet(script, filename);

        } else {
          log(-1, "runRuby: invalid arguments");
          exitCode = -1;
        }
      }
    } catch (Exception e) {
      java.util.regex.Pattern p = java.util.regex.Pattern.compile("SystemExit: ([0-9]+)");
      Matcher matcher = p.matcher(e.toString());
//TODO error stop I18N
      if (matcher.find()) {
        exitCode = Integer.parseInt(matcher.group(1));
        Debug.info("Exit code: " + exitCode);
      } else {
        if (null != ruFile) {
          exitCode = findErrorSource(e, filename, scriptPaths);
        } else {
          Debug.error("runRuby: Ruby exception: %s with %s", e.getMessage(), stmt);
        }
        if (fromIDE) {
          exitCode *= -1;
        } else {
          exitCode = 1;
        }
      }
    }
    return exitCode;
  }

  private int findErrorSource(Throwable thr, String filename, String[] forIDE) {
    String err = thr.getMessage();

    errorLine = -1;
    errorColumn = -1;
    errorClass = PY_UNKNOWN;
    errorType = "--UnKnown--";
    errorText = "--UnKnown--";

    String msg;

    if (err.startsWith("(SyntaxError)")) {
      // org.jruby.parser.ParserSyntaxException
      // (SyntaxError) /tmp/sikuli-3213678404470696048.rb:2: syntax error, unexpected
      // tRCURLY

      Pattern pLineS = Pattern.compile("(?<=:)(\\d+):(.*)");
      Matcher mLine = pLineS.matcher(err);
      if (mLine.find()) {
        log(lvl + 2, "SyntaxError error line: " + mLine.group(1));
        errorText = mLine.group(2) == null ? errorText : mLine.group(2);
        log(lvl + 2, "SyntaxError: " + errorText);
        errorLine = Integer.parseInt(mLine.group(1));
        errorColumn = -1;
        errorClass = PY_SYNTAX;
        errorType = "SyntaxError";
      }
    } else {
      // if (err.startsWith("(NameError)")) {
      // org.jruby.embed.EvalFailedException
      // (NameError) undefined local variable or method `asdf' for main:Object

      Pattern type = Pattern.compile("(?<=\\()(\\w*)");
      Matcher mLine = type.matcher(err);
      if (mLine.find()) {
        errorType = mLine.group(1);
      }
      Throwable cause = thr.getCause();
      // cause.printStackTrace();
      for (StackTraceElement line : cause.getStackTrace()) {
        if (line.getFileName().equals(filename)) {
          errorText = cause.getMessage();
          errorColumn = -1;
          errorLine = line.getLineNumber();
          errorClass = PY_RUNTIME;
          this.errorText = thr.getMessage();

          Pattern sikType = Pattern.compile("(?<=org.sikuli.script.)(.*)(?=:)");
          Matcher mSikType = sikType.matcher(this.errorText);

          if (mSikType.find()) {
            errorType = mSikType.group(1);
          } else if (errorType.equals("RuntimeError")) {
            errorClass = PY_JAVA;
          }
          break;
        }
      }
    }

    msg = "script";
    if (forIDE != null) {
      msg += " [ " + forIDE[1] + " ]";
    }
    if (errorLine != -1) {
      // log(-1,_I("msgErrorLine", srcLine));
      msg += " stopped with error in line " + errorLine;
      if (errorColumn != -1) {
        msg += " at column " + errorColumn;
      }
    } else {
      msg += "] stopped with error at line --unknown--";
    }

    if (errorClass == PY_RUNTIME || errorClass == PY_SYNTAX) {
      Debug.error(msg);
      Debug.error(errorType + " ( " + errorText + " )");
      if (errorClass == PY_RUNTIME) {
        Throwable cause = thr.getCause();
        // cause.printStackTrace();
        StackTraceElement[] stack = cause.getStackTrace();
        /*
         * StringWriter writer = new StringWriter(); PrintWriter out = new
         * PrintWriter(writer); cause.printStackTrace(out); errorTrace =
         * writer.toString();
         */
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement line : stack) {
          builder.append(line.getLineNumber());
          builder.append(":\t");
          builder.append(line.getClassName());
          builder.append(" ( ");
          builder.append(line.getMethodName());
          builder.append(" )\t");
          builder.append(line.getFileName());
          builder.append('\n');
        }
        errorTrace = builder.toString();
        if (errorTrace.length() > 0) {
          Debug.error("--- Traceback --- error source first\n" + "line: class ( method ) file \n" + errorTrace
              + "[error] --- Traceback --- end --------------");
          log(lvl + 2, "--- Traceback --- error source first\n" + "line: class ( method ) file \n" + errorTrace
              + "[error] --- Traceback --- end --------------");
        }
      }
    } else if (errorClass == PY_JAVA) {
    } else {
      Debug.error(msg);
      Debug.error("Could not evaluate error source nor reason. Analyze StackTrace!");
      Debug.error(err);
    }
    return errorLine;
  }

  /**
   * Initializes the ScriptingContainer and creates an instance.
   */
  private void createScriptingContainer() {
//TODO create a specific RubyPath (sys.path)
    if (interpreter == null) {
      // ScriptingContainer.initialize(System.getProperties(), null,
      // sysargv.toArray(new String[0]));

      interpreter = new ScriptingContainer(LocalContextScope.THREADSAFE);
      interpreter.setCompatVersion(CompatVersion.RUBY2_0);
      interpreter.setCompileMode(CompileMode.JIT);
    }
  }

  public ScriptingContainer getScriptingContainer() {
    if (interpreter == null) {
      sysargv = new ArrayList<String>();
      sysargv.add("--???--");
      sysargv.addAll(Arrays.asList(sxRunTime.getArgs()));
      createScriptingContainer();
    }
    return interpreter;
  }

  /**
   * Executes the defined header for the jruby script.
   *
   * @param syspaths List of all syspath entries
   */
  private void executeScriptHeader(String[] syspaths) {
    List<String> path = interpreter.getLoadPaths();
    if (path.size() == 0 || !FileManager.pathEquals((String) path.get(0), sikuliLibPath)) {
      log(lvl, "executeScriptHeader: adding SikuliX Lib path to sys.path\n" + sikuliLibPath);
      int pathLength = path.size();
      String[] pathNew = new String[pathLength + 1];
      pathNew[0] = sikuliLibPath;
      for (int i = 0; i < pathLength; i++) {
        log(lvl + 1, "executeScriptHeader: before: %d: %s", i, path.get(i));
        pathNew[i + 1] = (String) path.get(i);
      }
      for (int i = 0; i < pathLength; i++) {
        path.set(i, pathNew[i]);
      }
      path.add(pathNew[pathNew.length - 1]);
      for (int i = 0; i < pathNew.length; i++) {
        log(lvl + 1, "executeScriptHeader: after: %d: %s", i, path.get(i));
      }
    }
    if (savedpathlen == 0) {
      savedpathlen = interpreter.getLoadPaths().size();
      log(lvl + 1, "executeScriptHeader: saved sys.path: %d", savedpathlen);
    }
    while (interpreter.getLoadPaths().size() > savedpathlen) {
      interpreter.getLoadPaths().remove(savedpathlen);
    }
    log(lvl + 1, "executeScriptHeader: at entry: path:");
    for (String p : interpreter.getLoadPaths()) {
      log(lvl + 1, p);
    }
    log(lvl + 1, "executeScriptHeader: at entry: --- end ---");
    for (String syspath : syspaths) {
      path.add(FileManager.slashify(syspath, false));
    }

    interpreter.runScriptlet(SCRIPT_HEADER);

    if (codeBefore != null) {
      StringBuilder buffer = new StringBuilder();
      for (String line : codeBefore) {
        buffer.append(line);
      }
      interpreter.runScriptlet(buffer.toString());
    }
  }

  @Override
  protected boolean doRedirect(PipedInputStream stdout, PipedInputStream stderr) {
    // Since we have a static interpreter, we have to synchronize class wide
    synchronized (JRubyRunner.class) {
      if (!redirected) {
        Debug.info("JRuby redirect");
        redirected = true;

        ScriptingContainer interpreter = getScriptingContainer();
        try {
          PipedOutputStream pout = new PipedOutputStream(stdout);
          PrintStream ps = new PrintStream(pout, true);
          interpreter.setOutput(ps);
        } catch (Exception e) {
          e.printStackTrace();
          log(-1, "%s: redirect STDOUT: %s", getName(), e.getMessage());
          return false;
        }
        try {
          PipedOutputStream pout = new PipedOutputStream(stderr);
          PrintStream ps = new PrintStream(pout, true);
          interpreter.setError(ps);
        } catch (Exception e) {
          log(-1, "%s: redirect STDERR: %s", getName(), e.getMessage());
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Fills the sysargv list for the Ruby script
   *
   * @param filename The file containing the script: Has to be passed as first
   *                 parameter in Ruby
   * @param argv     The parameters passed to Sikuli with --args
   */
  private void fillSysArgv(File filename, String[] argv) {
    sysargv = new ArrayList<String>();
    if (filename != null) {
      sysargv.add(filename.getAbsolutePath());
    }
    if (argv != null) {
      sysargv.addAll(Arrays.asList(argv));
    }
  }
}
