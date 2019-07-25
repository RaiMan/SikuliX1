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
    synchronized (JRubyRunner.class) {
      if (null == scriptFile) {
        // run the Ruby statements from argv (special for setup functional test)
        jrubySupport.fillSysArgv(null, null);
        executeScriptHeader(new String[0]);
        return runRuby(null, scriptArgs, null, options);
      }
      File rbFile = new File(new File(scriptFile).getAbsolutePath());
      jrubySupport.fillSysArgv(rbFile, scriptArgs);

      executeScriptHeader(new String[]{rbFile.getParentFile().getAbsolutePath(),
              rbFile.getParentFile().getParentFile().getAbsolutePath()});

      prepareFileLocation(rbFile, options);

      int exitCode = runRuby(rbFile, null, new String[]{rbFile.getParentFile().getAbsolutePath()}, options);

      log(lvl + 1, "runScript: at exit: path:");
      for (Object p : jrubySupport.interpreterGetLoadPaths()) {
        log(lvl + 1, "runScript: " + p.toString());
      }
      log(lvl + 1, "runScript: at exit: --- end ---");
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
        log(-1, "runLines: (%s) raised: %s", lines, ex);
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
    jrubySupport.interpreterTerminate();
    redirected = false;
  }

  private static int savedpathlen = 0;
  private static boolean redirected = false;

  private int runRuby(File ruFile, String[] stmts, String[] scriptPaths, IScriptRunner.Options options) {
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
        jrubySupport.interpreterSetScriptFilename(filename);
        jrubySupport.interpreterRunScriptletString(buffer.toString());
      } else {
        filename = ruFile.getAbsolutePath();
        if (scriptPaths != null) {
          BufferedReader scriptReader = new BufferedReader(
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
          jrubySupport.interpreterRunScriptletFile(scriptReader, filename);

        } else {
          log(-1, "runRuby: invalid arguments");
          exitCode = -1;
        }
      }
    } catch (Exception e) {
      exitCode = 1;

      java.util.regex.Pattern p = java.util.regex.Pattern.compile("SystemExit: ([0-9]+)");
      Matcher matcher = p.matcher(e.toString());
//TODO error stop I18N
      if (matcher.find()) {
        exitCode = Integer.parseInt(matcher.group(1));
        Debug.info("Exit code: " + exitCode);
      } else {
        if (null != ruFile) {
          if (null != options) {
            int errorExit = jrubySupport.findErrorSource(e, filename);
            options.setErrorLine(errorExit);
          }
        } else {
          Debug.error("runRuby: Ruby exception: %s with %s", e.getMessage(), stmt);
        }
      }
    }
    return exitCode;
  }

  /**
   * Executes the defined header for the jruby script.
   *
   * @param syspaths List of all syspath entries
   */
  private void executeScriptHeader(String[] syspaths) {
    List<String> path = jrubySupport.interpreterGetLoadPaths();
    String sikuliLibPath = RunTime.get().fSikulixLib.getAbsolutePath();
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
      savedpathlen = jrubySupport.interpreterGetLoadPaths().size();
      log(lvl + 1, "executeScriptHeader: saved sys.path: %d", savedpathlen);
    }
    while (jrubySupport.interpreterGetLoadPaths().size() > savedpathlen) {
      jrubySupport.interpreterGetLoadPaths().remove(savedpathlen);
    }
    log(lvl + 1, "executeScriptHeader: at entry: path:");
    for (String p : jrubySupport.interpreterGetLoadPaths()) {
      log(lvl + 1, p);
    }
    log(lvl + 1, "executeScriptHeader: at entry: --- end ---");
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

  @Override
  public boolean isAbortSupported() {
    return true;
  }

}
