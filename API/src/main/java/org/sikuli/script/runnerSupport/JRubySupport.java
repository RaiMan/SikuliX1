/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runnerSupport;

import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.jruby.Ruby;
//import org.jruby.runtime.ThreadContext;
import org.sikuli.basics.Debug;
import org.sikuli.script.support.RunTime;

/**
 * This class implements JRuby specific parts
 */
public class JRubySupport implements IRunnerSupport {

  //<editor-fold defaultstate="collapsed" desc="00 logging">
  private static final String me = "JRuby: ";
  private static int lvl = 3;

  public void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }

  public void terminate(int retVal, String msg, Object... args) {
    RunTime.get().terminate(retVal, me + msg, args);
  }
  //</editor-fold>

  private static ArrayList<String> sysargv = null;

  /**
   * Mandatory method which returns an instance of the helper
   *
   * @return
   */
  public static JRubySupport get() {
    if (null == instance) {
      instance = new JRubySupport();
      RunTime.get().exportLib();
      instance.createScriptingContainer();
    }
    return instance;
  }

  private static JRubySupport instance = null;

  private JRubySupport() {}

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

  private static Object interpreter = null;
  private static Class cInterpreter = null;

  public List<String> interpreterGetLoadPaths() {
    //return interpreter.getLoadPaths();
    try {
      return (List<String>) cInterpreter.getMethod("getLoadPaths", new Class[0])
              .invoke(cInterpreter, new Object[0]);
    } catch (Exception e) {
      log(-1, "interpreter.getLoadPaths(): %s", e.getMessage());
      RunTime.get().terminate(-1, "JRuby: reflection problem");
    }
    return null;
  }

  public Object interpreterRunScriptletString(String script) {
    //return interpreter.runScriptlet(script);
    try {
      return cInterpreter.getMethod("runScriptlet", new Class[]{String.class})
              .invoke(cInterpreter, new Object[]{script});
    } catch (Exception e) {
      log(-1, "interpreter.runScriptletString(): %s", e.getMessage());
      RunTime.get().terminate(-1, "JRuby: reflection problem");
    }
    return null;
  }

  public Object interpreterRunScriptletFile(Reader reader, String filename) {
    //return interpreter.runScriptlet(reader, filename);
    try {
      return cInterpreter.getMethod("runScriptlet", new Class[]{Reader.class, String.class})
              .invoke(cInterpreter, new Object[]{reader, filename});
    } catch (Exception e) {
      log(-1, "interpreter.runScriptletFile(): %s", e.getMessage());
      RunTime.get().terminate(-1, "JRuby: reflection problem");
    }
    return null;
  }

  public void interpreterSetScriptFilename(String filename) {
    //interpreter.setScriptFilename(filename);
    try {
      cInterpreter.getMethod("setScriptFilename", new Class[]{String.class})
              .invoke(cInterpreter, new Object[]{filename});
    } catch (Exception e) {
      log(-1, "interpreter.setScriptFilename(): %s", e.getMessage());
      RunTime.get().terminate(-1, "JRuby: reflection problem");
    }
  }

  /**
   * Initializes the ScriptingContainer and creates an instance.
   */
  public void createScriptingContainer() {
    //TODO create a specific RubyPath (sys.path)
    if (interpreter == null) {
      RunTime.get().fSikulixLib.getAbsolutePath();
      // ScriptingContainer.initialize(System.getProperties(), null,
      // sysargv.toArray(new String[0]));
      try {
        cInterpreter = Class.forName("org.jruby.embed.ScriptingContainer");
      } catch (ClassNotFoundException e) {
        log(-1, "not found on classpath");
      }
      try {
        //interpreter = new ScriptingContainer(LocalContextScope.THREADSAFE);
        Class cLocalContextScope = Class.forName("org.jruby.embed.LocalContextScope");
        Constructor RI_new = cInterpreter.getConstructor(new Class[]{cLocalContextScope});
        interpreter = RI_new.newInstance(new Object[]{cLocalContextScope.getField("THREADSAFE")});
        //interpreter.setCompileMode(CompileMode.JIT);
        Class cCompileMode = Class.forName("org.jruby.RubyInstanceConfig.CompileMode");
        Field jit = cCompileMode.getField("JIT");
        Method mSetCompileMode = cInterpreter.getMethod("setCompileMode", new Class[]{cCompileMode});
        mSetCompileMode.invoke(cInterpreter, new Object[]{jit});
      } catch (Exception e) {
        log(-1, "init problem: %s", e.getMessage());
      }
    }
  }

  public void interpreterTerminate() {
    // TODO this is currently a bad idea because it terminates the static interpreter instance.
    if (interpreter != null) {
      //interpreter.terminate();
      try {
        cInterpreter.getMethod("terminate", new Class[0]).invoke(cInterpreter, new Object[0]);
      } catch (Exception e) {
        log(-1, "interpreter.terminate(): %s", e.getMessage());
        RunTime.get().terminate(-1, "JRuby: reflection problem");
      }
      interpreter = null;
    }
  }

  public boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    try {
      interpreterSetOutput(stdout);
    } catch (Exception e) {
      e.printStackTrace();
      log(-1, "JRuby: redirect STDOUT: %s", e.getMessage());
      return false;
    }
    try {
      interpreterSetError(stderr);
    } catch (Exception e) {
      log(-1, "JRuby: redirect STDERR: %s", e.getMessage());
      return false;
    }
    return true;
  }

  void interpreterSetOutput(PrintStream stdout) {
    //interpreter.setOutput(stdout);
    try {
      cInterpreter.getMethod("setOutput", new Class[]{PrintStream.class})
              .invoke(cInterpreter, new Object[]{stdout});
    } catch (Exception e) {
      log(-1, "interpreter.setOutput(): %s", e.getMessage());
      RunTime.get().terminate(-1, "JRuby: reflection problem");
    }
  }

  void interpreterSetError(PrintStream stderr) {
    //interpreter.setOutput(stderr);
    try {
      cInterpreter.getMethod("setError", new Class[]{PrintStream.class})
              .invoke(cInterpreter, new Object[]{stderr});
    } catch (Exception e) {
      log(-1, "interpreter.setError(): %s", e.getMessage());
      RunTime.get().terminate(-1, "JRuby: reflection problem");
    }
  }

  public void fillSysArgv(File filename, String[] argv) {
    JRubySupport.sysargv = new ArrayList<String>();
    if (filename != null) {
      JRubySupport.sysargv.add(filename.getAbsolutePath());
    }
    if (argv != null) {
      JRubySupport.sysargv.addAll(Arrays.asList(argv));
    }
  }

  public int findErrorSource(Throwable thr, String filename) {
    String err = thr.getMessage();

    errorLine = -1;
    errorColumn = -1;
    errorClass = RB_UNKNOWN;
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
      msg += " stopped with error in line " + errorLine;
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
    return errorLine;
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


}
