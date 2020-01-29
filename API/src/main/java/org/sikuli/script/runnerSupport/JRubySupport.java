/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runnerSupport;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jruby.RubyInstanceConfig;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.RunTime;

public class JRubySupport implements IRunnerSupport {

  //<editor-fold defaultstate="collapsed" desc="00 initialization">
  private static final String me = "JRuby: ";
  private static int lvl = 3;

  public void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private JRubySupport() {}

  public static JRubySupport get() {
    if (null == instance) {
      instance = new JRubySupport();
      RunTime.get().exportLib();
      instance.interpreterInitialization();
    }
    return instance;
  }

  private static JRubySupport instance = null;

  public static boolean isSupported() {
    try {
      Class.forName("org.jruby.embed.ScriptingContainer");
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  private static ScriptingContainer interpreter = null;

  public void interpreterInitialization() {
    //TODO create a specific RubyPath (sys.path)
    if (interpreter == null) {
      RunTime.get().fSikulixLib.getAbsolutePath();
      //TODO needed?
      //ScriptingContainer.initialize(System.getProperties(), null, sysargv.toArray(new String[0]));
      try {
        Class.forName("org.jruby.embed.ScriptingContainer");
      } catch (ClassNotFoundException e) {
        log(-1, "not found on classpath");
        return;
      }
      try {
        interpreter = new ScriptingContainer(LocalContextScope.THREADSAFE);
        interpreter.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
      } catch (Exception e) {
        log(-1, "init problem: %s", e.getMessage());
        interpreter = null;
      }
    }
  }

  public boolean interpreterRedirect(PrintStream stdout, PrintStream stderr) {
    if (interpreter == null) {
      return false;
    }
    try {
      interpreter.setOutput(stdout);
    } catch (Exception e) {
      e.printStackTrace();
      log(-1, "JRuby: redirect STDOUT: %s", e.getMessage());
      return false;
    }
    try {
      interpreter.setError(stderr);
    } catch (Exception e) {
      log(-1, "JRuby: redirect STDERR: %s", e.getMessage());
      return false;
    }
    return true;
  }
  //</editor-fold>

  //<editor-fold desc="10 before script run">
  public List<String> interpreterGetLoadPaths() {
    if (null == interpreter) {
      return null;
    }
    return interpreter.getLoadPaths();
  }

  public void executeScriptHeader(List<String> codeBefore, String ... paths) {
    List<String> path = interpreterGetLoadPaths();
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
      savedpathlen = interpreterGetLoadPaths().size();
    }
    while (interpreterGetLoadPaths().size() > savedpathlen) {
      interpreterGetLoadPaths().remove(savedpathlen);
    }
    for (String syspath : paths) {
      path.add(new File(syspath).getAbsolutePath());
    }

    interpreterRunScriptletString(SCRIPT_HEADER);

    if (codeBefore != null) {
      StringBuilder buffer = new StringBuilder();
      for (String line : codeBefore) {
        buffer.append(line);
      }
      interpreterRunScriptletString(buffer.toString());
    }
  }

  private static int savedpathlen = 0;
  private final static String SCRIPT_HEADER =
          "# coding: utf-8\n"
                  + "require 'Lib/sikulix'\n"
                  + "include Sikulix\n";

  private static ArrayList<String> sysargv = null;

  public void fillSysArgv(File filename, String[] argv) {
    sysargv = new ArrayList<>();
    if (filename != null) {
      sysargv.add(filename.getAbsolutePath());
    }
    if (argv != null) {
      sysargv.addAll(Arrays.asList(argv));
    }
    if (interpreter != null) {
      interpreter.setArgv(sysargv.toArray(new String[0]));
    }
  }
  //</editor-fold>

  //<editor-fold desc="20 script run">
  public Object interpreterRunScriptletString(String script) {
    if (null == interpreter) {
      return null;
    }
    return interpreter.runScriptlet(script);
  }

  public Object interpreterRunScriptletFile(Reader reader, String filename) throws Throwable {
    if (null == interpreter) {
      return null;
    }
    return interpreter.runScriptlet(reader, filename);
  }
  //</editor-fold>

  //<editor-fold desc="30 after script run">
  public void interpreterTerminate() {
    // TODO this is currently a bad idea because it terminates the static interpreter instance.
    if (interpreter != null) {
      interpreter.terminate();
//      try {
//        cInterpreter.getMethod("terminate", new Class[0]).invoke(interpreter, new Object[0]);
//      } catch (Exception e) {
//        log(-1, "interpreter.terminate(): %s", e.getMessage());
//      }
      interpreter = null;
    }
  }

  public int findErrorSource(Throwable thr, String filename, int headerOffset) {
    String err = thr.getMessage();

    errorLine = -1;
    errorColumn = -1;
    errorClass = RB_UNKNOWN;
    errorType = "--UnKnown--";
    errorText = "--UnKnown--";

    String msg;

    if (err.startsWith("(SyntaxError)")) {
      // org.jruby.parser.ParserSyntaxException
      // (SyntaxError) /tmp/sikuli-3213678404470696048.rb:2: syntax error, unexpected tRCURLY

      Pattern pLineS = Pattern.compile("(?<=:)(\\d+):(.*)");
      Matcher mLine = pLineS.matcher(err);
      if (mLine.find()) {
        errorText = mLine.group(2) == null ? errorText : mLine.group(2);
        errorLine = Integer.parseInt(mLine.group(1));
        errorColumn = -1;
        errorClass = RB_SYNTAX;
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
          errorClass = RB_RUNTIME;
          this.errorText = thr.getMessage();

          Pattern sikType = Pattern.compile("(?<=org.sikuli.script.)(.*)(?=:)");
          Matcher mSikType = sikType.matcher(this.errorText);

          if (mSikType.find()) {
            errorType = mSikType.group(1);
          } else if (errorType.equals("RuntimeError")) {
            errorClass = RB_JAVA;
          }
          break;
        }
      }
    }

    msg = "script";
    if (errorLine != -1) {
      // log(-1,_I("msgErrorLine", srcLine));
      msg += " stopped with error in line " + (errorLine - headerOffset);
      if (errorColumn != -1) {
        msg += " at column " + errorColumn;
      }
    } else {
      msg += "] stopped with error at line --unknown--";
    }

    if (errorClass == RB_RUNTIME || errorClass == RB_SYNTAX) {
      Debug.error(msg);
      Debug.error(errorType + " ( " + errorText + " )");
      if (errorClass == RB_RUNTIME) {
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
    } else if (errorClass == RB_JAVA) {
    } else {
      Debug.error(msg);
      Debug.error("Could not evaluate error source nor reason. Analyze StackTrace!");
      Debug.error(err);
    }
    return errorLine - headerOffset;
  }

  private int errorLine;
  private int errorColumn;
  private String errorType;
  private String errorText;
  private int errorClass;
  private String errorTrace;

  private static final int RB_SYNTAX = 0;
  private static final int RB_RUNTIME = 1;
  private static final int RB_JAVA = 2;
  private static final int RB_UNKNOWN = -1;
  //</editor-fold>

  @Override
  public boolean runObserveCallback(Object[] args) {
    boolean result = false;
    Object callback = args[0];
    Object e = args[1];
    try {
      Class<?> rubyProcClass = callback.getClass();
      Method getRuntime = rubyProcClass.getMethod("getRuntime", new Class<?>[0]);
      Object runtime = getRuntime.invoke(callback, new Object[0]);
      Class<?> runtimeClass = getRuntime.getReturnType();

      Method getCurrentContext = runtimeClass.getMethod("getCurrentContext", new Class<?>[0]);
      Object context = getCurrentContext.invoke(runtime, new Object[0]);

      Class<?> jrubyUtil = Class.forName("org.jruby.javasupport.JavaUtil");
      Method convertJavaToRuby = jrubyUtil.getMethod("convertJavaToRuby",
              new Class<?>[]{runtimeClass, Object.class});

      Object paramForRuby = convertJavaToRuby.invoke(null, new Object[]{runtime, e});

      Object iRubyObject = Array.newInstance(Class.forName("org.jruby.runtime.builtin.IRubyObject"), 1);
      Array.set(iRubyObject, 0, paramForRuby);

      Method call = rubyProcClass.getMethod("call",
              new Class<?>[]{context.getClass(), iRubyObject.getClass()});
      call.invoke(callback, new Object[]{context, iRubyObject});
      result = true;
    } catch (Exception ex) {
      String msg = ex.getMessage();
      Debug.error("ObserverCallBack: problem with scripting handler: %s\n%s\n%s", me, callback, msg);
    }
    return result;
  }
}
