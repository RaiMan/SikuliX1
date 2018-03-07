/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.apache.commons.cli.CommandLine;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;
import org.sikuli.util.JythonHelper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * INTERNAL USE --- NOT official API<br>
 *   not in version 2
 */
public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;
  static final RunTime runTime = RunTime.get();

  public static Map<String, String> endingTypes = new HashMap<String, String>();
  public static Map<String, String> typeEndings = new HashMap<String, String>();
  public static Map<String, String> runnerTypes = new HashMap<String, String>();
  public static String ERUBY = "rb";
  public static String EPYTHON = "py";
  public static String EJSCRIPT = "js";
  public static String EASCRIPT = "script";
  public static String ESSCRIPT = "ps1";
  public static String EPLAIN = "txt";
  public static String EDEFAULT = EPYTHON;
  public static String CPYTHON = "text/python";
  public static String CRUBY = "text/ruby";
  public static String CJSCRIPT = "text/javascript";
  public static String CASCRIPT = "text/applescript";
  public static String CSSCRIPT = "text/powershell";
  public static String CPLAIN = "text/plain";
  public static String RPYTHON = "jython";
  public static String RRUBY = "jruby";
  public static String RJSCRIPT = "JavaScript";
  public static String RASCRIPT = "AppleScript";
  public static String RSSCRIPT = "PowerShell";
  public static String RRSCRIPT = "Robot";
  public static String RDEFAULT = RPYTHON;

  private static String[] runScripts = null;
  private static String[] testScripts = null;
  private static int lastReturnCode = 0;

  private static String beforeJSjava8 = "load(\"nashorn:mozilla_compat.js\");";
  private static String beforeJS
          = "importPackage(Packages.org.sikuli.script); "
          + "importClass(Packages.org.sikuli.basics.Debug); "
          + "importClass(Packages.org.sikuli.basics.Settings);";

  static {
    endingTypes.put(EPYTHON, CPYTHON);
    endingTypes.put(ERUBY, CRUBY);
    endingTypes.put(EJSCRIPT, CJSCRIPT);
    endingTypes.put(EPLAIN, CPLAIN);
    for (String k : endingTypes.keySet()) {
      typeEndings.put(endingTypes.get(k), k);
    }
    runnerTypes.put(EPYTHON, RPYTHON);
    runnerTypes.put(ERUBY, RRUBY);
    runnerTypes.put(EJSCRIPT, RJSCRIPT);
  }

  static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  public static String[] evalArgs(String[] args) {
    CommandArgs cmdArgs = new CommandArgs("SCRIPT");
    CommandLine cmdLine = cmdArgs.getCommandLine(CommandArgs.scanArgs(args));
    String cmdValue;

    if (cmdLine == null || cmdLine.getOptions().length == 0) {
      log(-1, "Did not find any valid option on command line!");
      cmdArgs.printHelp();
      System.exit(1);
    }

    if (cmdLine.hasOption(CommandArgsEnum.HELP.shortname())) {
      cmdArgs.printHelp();
      System.exit(1);
    }

    if (cmdLine.hasOption(CommandArgsEnum.LOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.LOGFILE.longname());
      if (!Debug.setLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.USERLOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.USERLOGFILE.longname());
      if (!Debug.setUserLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.DEBUG.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      if (cmdValue == null) {
        Debug.setDebugLevel(3);
        Settings.LogTime = true;
        if (!Debug.isLogToFile()) {
          Debug.setLogFile("");
        }
      } else {
        Debug.setDebugLevel(cmdValue);
      }
    }

    runTime.setArgs(cmdArgs.getUserArgs(), cmdArgs.getSikuliArgs());
    log(lvl, "commandline: %s", cmdArgs.getArgsOrg());
    runTime.printArgs();

    // select script runner and/or start interactive session
    // option is overloaded - might specify runner for -r/-t
    if (cmdLine.hasOption(CommandArgsEnum.INTERACTIVE.shortname())) {
      if (!cmdLine.hasOption(CommandArgsEnum.RUN.shortname())
              && !cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
        runTime.interactiveRunner = cmdLine.getOptionValue(CommandArgsEnum.INTERACTIVE.longname());
        runTime.runningInteractive = true;
        return null;
      }
    }

    String[] runScripts = null;
    runTime.runningTests = false;
    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.RUN.longname());
    } else if (cmdLine.hasOption(CommandArgsEnum.TEST.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.TEST.longname());
      log(-1, "Command line option -t: not yet supported! %s", Arrays.asList(args).toString());
      runTime.runningTests = true;
//TODO run a script as unittest with HTMLTestRunner
      System.exit(1);
    }
    return runScripts;
  }

  public static int run(String givenName) {
    return run(givenName, new String[0]);
  }

  public static int run(String givenName, String[] args) {
    String savePath = ImagePath.getBundlePathSet();
    int retVal = new RunBox(givenName, args, false).run();
    if (savePath != null) {
      ImagePath.setBundlePath(savePath);
    }
    lastReturnCode = retVal;
    return retVal;
  }

  public static int runTest(String givenName) {
    return runTest(givenName, new String[0]);
  }

  public static int runTest(String givenName, String[] args) {
    String savePath = ImagePath.getBundlePath();
    int retVal = new RunBox(givenName, args, true).run();
    ImagePath.setBundlePath(savePath);
    return retVal;
  }

  public static int getLastReturnCode() {
    return lastReturnCode;
  }

  public static int runScripts(String[] args) {
    runScripts = Runner.evalArgs(args);
    String someJS = "";
    int exitCode = 0;
    if (runScripts != null && runScripts.length > 0) {
      boolean runAsTest = runTime.runningTests;
      for (String givenScriptName : runScripts) {
        if (lastReturnCode == -1) {
          log(lvl, "Exit code -1: Terminating multi-script-run");
          break;
        }
        someJS = runTime.getOption("runsetup", "");
        if (!someJS.isEmpty()) {
          log(lvl, "Options.runsetup: %s", someJS);
          new RunBox().runjs(null, null, someJS, null);
        }
        RunBox rb = new RunBox(givenScriptName, runTime.getArgs(), runAsTest);
        exitCode = rb.run();
        someJS = runTime.getOption("runteardown", "");
        if (!someJS.isEmpty()) {
          log(lvl, "Options.runteardown: %s", someJS);
          new RunBox().runjs(null, null, someJS, null);
        }
        if (exitCode == -999) {
          exitCode = lastReturnCode;
        }
        lastReturnCode = exitCode;
      }
    }
    return exitCode;
  }

  public static File getScriptFile(File fScriptFolder) {
    if (fScriptFolder == null) {
      return null;
    }
    File[] content = FileManager.getScriptFile(fScriptFolder);
    if (null == content) {
      return null;
    }
    File fScript = null;
    for (File aFile : content) {
      for (String suffix : Runner.endingTypes.keySet()) {
        if (!aFile.getName().endsWith("." + suffix)) {
          continue;
        }
        fScript = aFile;
        break;
      }
      if (fScript != null) {
        break;
      }
    }
    // try with compiled script
    if (content.length == 1 && content[0].getName().endsWith("$py.class")) {
      fScript = content[0];
    }
    return fScript;
  }

  static JythonHelper pyRunner = null;
  static Class cIDE;
  static Method mShow;
  static Method mHide;

  protected static boolean initpy() {
    if (pyRunner == null) {
      pyRunner = JythonHelper.get();
      if (pyRunner == null) {
        return false;
      }
      pyRunner.exec("# -*- coding: utf-8 -*- ");
      pyRunner.exec("import org.sikuli.script.SikulixForJython");
      pyRunner.exec("from sikuli import *");
    }
    return true;
  }

  static Object rbRunner = null;
  static Object txtRunner = null;
  static ScriptEngine jsRunner = null;

  public static ScriptEngine initjs() {
    ScriptEngineManager jsFactory = new ScriptEngineManager();
    ScriptEngine jsr = jsFactory.getEngineByName("JavaScript");
    if (jsr != null) {
      log(lvl, "ScriptingEngine started: JavaScript (ending .js)");
    } else {
      runTime.terminate(1, "ScriptingEngine for JavaScript not available");
    }
    if (RunTime.Type.IDE.equals(runTime.runType)) {
      try {
        cIDE = Class.forName("org.sikuli.ide.SikuliIDE");
        mHide = cIDE.getMethod("hideIDE", new Class[0]);
        mShow = cIDE.getMethod("showIDE", new Class[0]);
      } catch (Exception ex) {
        log(-1, "initjs: getIDE");
      }
    }
    return jsr;
  }

  public static String prologjs(String before) {
    String after = before;
    if (after.isEmpty()) {
      if (runTime.isJava8()) {
        after += beforeJSjava8;
      }
      after += beforeJS;
    } else {
      String commands = runTime.extractResourceToString("JavaScript", "commands.js", "");
      if (commands != null) {
        after += commands;
      }
    }
    return after;
  }

  public static Object[] runBoxInit(String givenName, File scriptProject, URL uScriptProject) {
    String gitScripts = "https://github.com/RaiMan/SikuliX-2014/tree/master/TestScripts/";
    String givenScriptHost = "";
    String givenScriptFolder = "";
    String givenScriptName;
    String givenScriptScript = "";
    String givenScriptType = "sikuli";
    String givenScriptScriptType = RDEFAULT;
    Boolean givenScriptExists = true;
    URL uGivenScript = null;
    URL uGivenScriptFile = null;
    givenScriptName = givenName;
    String[] parts = null;
    int isNet;
    boolean isInline = false;
    givenName = givenName.trim();
    if (givenName.toLowerCase().startsWith(RASCRIPT.toLowerCase())) {
      givenScriptScriptType = RASCRIPT;
      givenScriptName = null;
      givenScriptScript = givenName.substring(RASCRIPT.length() + 1);
      isInline = true;
    } else if (givenName.toLowerCase().startsWith(RSSCRIPT.toLowerCase())) {
      givenScriptScriptType = RSSCRIPT;
      givenScriptName = null;
      givenScriptScript = givenName.substring(RSSCRIPT.length() + 1);
      isInline = true;
    } else if (givenName.toLowerCase().startsWith(RRSCRIPT.toLowerCase())) {
      givenScriptScriptType = RRSCRIPT;
      givenScriptName = null;
      givenScriptScript = givenName.substring(RRSCRIPT.length() + 1);
      isInline = true;
    } else if (givenName.toLowerCase().startsWith("git*")) {
      if (givenName.length() == 4) {
        givenName = gitScripts + "showcase";
      } else {
        givenName = gitScripts + givenName.substring(4);
      }
    }
    if (!isInline) {
      boolean fromNet = false;
      String scriptLocation = givenName;
      String content = "";
      if (-1 < (isNet = givenName.indexOf("://"))) {
        givenName = givenName.substring(isNet + 3);
        if (givenName.indexOf(":") == -1) {
          givenName = givenName.replaceFirst("/", ":");
        }
      }
      if (givenName.indexOf(":") > 5) {
        parts = givenName.split(":");
        if (parts.length > 1 && !parts[1].isEmpty()) {
          fromNet = true;
          givenScriptHost = parts[0];
          givenScriptName = new File(parts[1]).getName();
          String fpFolder = new File(parts[1]).getParent();
          if (null != fpFolder && !fpFolder.isEmpty()) {
            givenScriptFolder = FileManager.slashify(fpFolder, true);
          }
        }
        scriptLocation = givenName;
        givenScriptExists = false;
      }
      if (fromNet) {
        if (givenScriptHost.contains("github.com")) {
          givenScriptHost = "https://raw.githubusercontent.com";
          givenScriptFolder = givenScriptFolder.replace("tree/", "");
        } else {
          givenScriptHost = "http://" + givenScriptHost;
        }
        if (givenScriptName.endsWith(".zip")) {
          scriptLocation = givenScriptHost + givenScriptFolder + givenScriptName;
          if (0 < FileManager.isUrlUseabel(scriptLocation)) {
            runTime.terminate(1, ".zip from net not yet supported\n%s", scriptLocation);
          }
        } else {
          for (String suffix : endingTypes.keySet()) {
            String dlsuffix = "";
            if (suffix != "js") {
              dlsuffix = ".txt";
            }
            givenScriptScript = givenScriptName + "/" + givenScriptName + "." + suffix;
            scriptLocation = givenScriptHost + "/" + givenScriptFolder + givenScriptScript;
            givenScriptScriptType = runnerTypes.get(suffix);
            if (0 < FileManager.isUrlUseabel(scriptLocation)) {
              content = FileManager.downloadURLtoString(scriptLocation);
              break;
            } else if (!dlsuffix.isEmpty()) {
              givenScriptScript = givenScriptName + "/" + givenScriptName + "." + suffix + dlsuffix;
              scriptLocation = givenScriptHost + "/" + givenScriptFolder + givenScriptScript;
              if (0 < FileManager.isUrlUseabel(scriptLocation)) {
                content = FileManager.downloadURLtoString(scriptLocation);
                break;
              }
            }
            scriptLocation = givenScriptHost + "/" + givenScriptFolder + givenScriptName;
          }
          if (content != null && !content.isEmpty()) {
            givenScriptType = "NET";
            givenScriptScript = content;
            givenScriptExists = true;
            try {
              uGivenScript = new URL(givenScriptHost + "/" + givenScriptFolder + givenScriptName);
            } catch (Exception ex) {
              givenScriptExists = false;
            }
          } else {
            givenScriptExists = false;
          }
        }
        if (!givenScriptExists) {
          log(-1, "given script location not supported or not valid:\n%s", scriptLocation);
        } else {
          String header = "# ";
          String trailer = "\n";
          if (RJSCRIPT.equals(givenScriptScriptType)) {
            header = "/*\n";
            trailer = "*/\n";
          }
          header += scriptLocation + "\n";
          if (Debug.is() > 2) {
            FileManager.writeStringToFile(header + trailer + content,
                    new File(runTime.fSikulixStore, "LastScriptFromNet.txt"));
          }
        }
      } else {
        boolean sameFolder = givenScriptName.startsWith("./");
        if (sameFolder) {
          givenScriptName = givenScriptName.substring(2);
        }
        if (givenScriptName.startsWith("JS*")) {
          givenScriptName = new File(runTime.fSxProjectTestScriptsJS, givenScriptName.substring(3)).getPath();
        }
        if (givenScriptName.startsWith("TEST*")) {
          givenScriptName = new File(runTime.fSxProjectTestScripts, givenScriptName.substring(5)).getPath();
        }
        String scriptName = new File(givenScriptName).getName();
        if (scriptName.contains(".")) {
          parts = scriptName.split("\\.");
          givenScriptScript = parts[0];
          givenScriptType = parts[1];
        } else {
          givenScriptScript = scriptName;
        }
        if (sameFolder && scriptProject != null) {
          givenScriptName = new File(scriptProject, givenScriptName).getPath();
        } else if (sameFolder && uScriptProject != null) {
          givenScriptHost = uScriptProject.getHost();
          givenScriptFolder = uScriptProject.getPath().substring(1);
        } else if (scriptProject == null && givenScriptHost.isEmpty()) {
          String fpParent = new File(givenScriptName).getParent();
          if (fpParent == null || fpParent.isEmpty()) {
            scriptProject = null;
          } else {
            scriptProject = new File(givenScriptName).getParentFile();
          }
        }
      }
    }
    Object[] vars = new Object[]{givenScriptHost, givenScriptFolder, givenScriptName,
            givenScriptScript, givenScriptType, givenScriptScriptType,
            uGivenScript, uGivenScriptFile, givenScriptExists, scriptProject, uScriptProject};
    return vars;
  }

  public static void runjsEval(String script) {
    new RunBox().runjsEval(script);
  }

  public static int runjs(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return new RunBox().runjs(fScript, uGivenScript, givenScriptScript, args);
  }

  public static int runas(String givenScriptScript) {
    return runas(givenScriptScript, false);
  }

  public static int runrobot(String code) {
    if (!JythonHelper.get().prepareRobot()) {
      return -1;
    }
    File script = new File(ImagePath.getBundlePath());
    File fRobotWork = new File(script.getAbsolutePath() + ".robot");
    FileManager.deleteFileOrFolder(fRobotWork);
    fRobotWork.mkdir();
    String sName = script.getName().replace(".sikuli", "");
    File fPyCode = new File(script, sName + ".py");
    String pyCode = FileManager.readFileToString(fPyCode);
    int prefix = pyCode.indexOf("\"\"\")");
    if (prefix > 0) {
      pyCode = pyCode.substring(prefix + 4).trim();
      int refLib = code.indexOf("./inline/");
      String inlineLib = "";
      File fInline = null;
      String fpInline = "";
      // Keyword implementations are inline
      if (!pyCode.isEmpty()) {
        if (refLib < 0) {
          log(-1, "runRobot: inline code ignored - no ./inline/");
        }
        inlineLib = code.substring(refLib + 9);
        inlineLib = inlineLib.substring(0, inlineLib.indexOf("\n")).trim();
        fInline = new File(fRobotWork, inlineLib + ".py");
        pyCode = "from sikuli import *\n" + pyCode;
        FileManager.writeStringToFile(pyCode, fInline);
        fpInline = FileManager.slashify(fInline.getAbsolutePath(), false);
        code = code.replace("./inline/" + inlineLib, fpInline);
      } else {
        if (refLib > -1) {
          log(-1, "runRobot: having ./inline/, but no inline code found");
          return -1;
        }
      }
    }
    File fRobot = new File(fRobotWork, sName + ".robot");
    FileManager.writeStringToFile(code, fRobot);
    if (!initpy()) {
      log(-1, "Running Python scripts:init failed");
      return -999;
    }
    pyRunner.exec("from sikuli import *; from threading import currentThread; currentThread().name = \"MainThread\"");
    //pyRunner.exec("import robot.run;");
    String robotCmd = String.format(
            "ret = robot.run(\"%s\", "
                    + "outputdir=\"%s\")", fRobot, fRobotWork);
    File fReport = new File(fRobotWork, "report.html");
    String urlReport = fReport.getAbsolutePath();
    if (RunTime.get().runningWindows) {
      robotCmd = robotCmd.replaceAll("\\\\", "\\\\\\\\");
      urlReport = "/" + urlReport.replaceAll("\\\\", "/");
    }
    pyRunner.exec(robotCmd + "; print \"robot.run returned:\", ret; " +
            String.format("print \"robot.run output is here:\\n%s\";",
            fRobotWork.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\")));
    if (new File(fRobotWork, "report.html").exists()) {
      App.openLink("file:" + urlReport);
    }
    return 0;
  }

  //<editor-fold defaultstate="collapsed" desc="robot run options">
//  -N --name name           Set the name of the top level test suite. Underscores
//                          in the name are converted to spaces. Default name is
//                          created from the name of the executed data source.
// -D --doc documentation   Set the documentation of the top level test suite.
//                          Underscores in the documentation are converted to
//                          spaces and it may also contain simple HTML formatting
//                          (e.g. *bold* and http://url/).
// -M --metadata name:value *  Set metadata of the top level suite. Underscores
//                          in the name and value are converted to spaces. Value
//                          can contain same HTML formatting as --doc.
//                          Example: --metadata version:1.2
// -G --settag tag *        Sets given tag(s) to all executed test cases.
// -t --test name *         Select test cases to run by name or long name. Name
//                          is case and space insensitive and it can also be a
//                          simple pattern where `*` matches anything and `?`
//                          matches any char. If using `*` and `?` in the console
//                          is problematic see --escape and --argumentfile.
// -s --suite name *        Select test suites to run by name. When this option
//                          is used with --test, --include or --exclude, only
//                          test cases in matching suites and also matching other
//                          filtering criteria are selected. Name can be a simple
//                          pattern similarly as with --test and it can contain
//                          parent name separated with a dot. For example
//                          `-s X.Y` selects suite `Y` only if its parent is `X`.
// -i --include tag *       Select test cases to run by tag. Similarly as name
//                          with --test, tag is case and space insensitive and it
//                          is possible to use patterns with `*` and `?` as
//                          wildcards. Tags and patterns can also be combined
//                          together with `AND`, `OR`, and `NOT` operators.
//                          Examples: --include foo --include bar*
//                                    --include fooANDbar*
// -e --exclude tag *       Select test cases not to run by tag. These tests are
//                          not run even if included with --include. Tags are
//                          matched using the rules explained with --include.
// -R --rerunfailed output  Select failed tests from an earlier output file to be
//                          re-executed. Equivalent to selecting same tests
//                          individually using --test option.
//    --runfailed output    Deprecated since RF 2.8.4. Use --rerunfailed instead.
// -c --critical tag *      Tests having given tag are considered critical. If no
//                          critical tags are set, all tags are critical. Tags
//                          can be given as a pattern like with --include.
// -n --noncritical tag *   Tests with given tag are not critical even if they
//                          have a tag set with --critical. Tag can be a pattern.
// -v --variable name:value *  Set variables in the test data. Only scalar
//                          variables are supported and name is given without
//                          `${}`. See --escape for how to use special characters
//                          and --variablefile for a more powerful variable
//                          setting mechanism that allows also list variables.
//                          Examples:
//                          --variable str:Hello  =>  ${str} = `Hello`
//                          -v str:Hi_World -E space:_  =>  ${str} = `Hi World`
//                          -v x: -v y:42  =>  ${x} = ``, ${y} = `42`
// -V --variablefile path *  File to read variables from (e.g. `path/vars.py`).
//                          Example file:
//                          |  import random
//                          |  __all__ = [`scalar`, `LIST__var`, `integer`]
//                          |  scalar = `Hello world!`
//                          |  LIST__var = [`Hello`, `list`, `world`]
//                          |  integer = random.randint(1,10)
//                          =>
//                          ${scalar} = `Hello world!`
//                          @{var} = [`Hello`,`list`,`world`]
//                          ${integer} = <random integer from 1 to 10>
// -d --outputdir dir       Where to create output files. The default is the
//                          directory where tests are run from and the given path
//                          is considered relative to that unless it is absolute.
// -o --output file         XML output file. Given path, similarly as paths given
//                          to --log, --report, --xunit, and --debugfile, is
//                          relative to --outputdir unless given as an absolute
//                          path. Other output files are created based on XML
//                          output files after the test execution and XML outputs
//                          can also be further processed with Rebot tool. Can be
//                          disabled by giving a special value `NONE`. In this
//                          case, also log and report are automatically disabled.
//                          Default: output.xml
// -l --log file            HTML log file. Can be disabled by giving a special
//                          value `NONE`. Default: log.html
//                          Examples: `--log mylog.html`, `-l NONE`
// -r --report file         HTML report file. Can be disabled with `NONE`
//                          similarly as --log. Default: report.html
// -x --xunit file          xUnit compatible result file. Not created unless this
//                          option is specified.
//    --xunitfile file      Deprecated. Use --xunit instead.
//    --xunitskipnoncritical  Mark non-critical tests on xUnit output as skipped.
// -b --debugfile file      Debug file written during execution. Not created
//                          unless this option is specified.
// -T --timestampoutputs    When this option is used, timestamp in a format
//                          `YYYYMMDD-hhmmss` is added to all generated output
//                          files between their basename and extension. For
//                          example `-T -o output.xml -r report.html -l none`
//                          creates files like `output-20070503-154410.xml` and
//                          `report-20070503-154410.html`.
//    --splitlog            Split log file into smaller pieces that open in
//                          browser transparently.
//    --logtitle title      Title for the generated test log. The default title
//                          is `<Name Of The Suite> Test Log`. Underscores in
//                          the title are converted into spaces in all titles.
//    --reporttitle title   Title for the generated test report. The default
//                          title is `<Name Of The Suite> Test Report`.
//    --reportbackground colors  Background colors to use in the report file.
//                          Either `all_passed:critical_passed:failed` or
//                          `passed:failed`. Both color names and codes work.
//                          Examples: --reportbackground green:yellow:red
//                                    --reportbackground #00E:#E00
// -L --loglevel level      Threshold level for logging. Available levels: TRACE,
//                          DEBUG, INFO (default), WARN, NONE (no logging). Use
//                          syntax `LOGLEVEL:DEFAULT` to define the default
//                          visible log level in log files.
//                          Examples: --loglevel DEBUG
//                                    --loglevel DEBUG:INFO
//    --suitestatlevel level  How many levels to show in `Statistics by Suite`
//                          in log and report. By default all suite levels are
//                          shown. Example:  --suitestatlevel 3
//    --tagstatinclude tag *  Include only matching tags in `Statistics by Tag`
//                          and `Test Details` in log and report. By default all
//                          tags set in test cases are shown. Given `tag` can
//                          also be a simple pattern (see e.g. --test).
//    --tagstatexclude tag *  Exclude matching tags from `Statistics by Tag` and
//                          `Test Details`. This option can be used with
//                          --tagstatinclude similarly as --exclude is used with
//                          --include.
//    --tagstatcombine tags:name *  Create combined statistics based on tags.
//                          These statistics are added into `Statistics by Tag`
//                          and matching tests into `Test Details`. If optional
//                          `name` is not given, name of the combined tag is got
//                          from the specified tags. Tags are combined using the
//                          rules explained in --include.
//                          Examples: --tagstatcombine requirement-*
//                                    --tagstatcombine tag1ANDtag2:My_name
//    --tagdoc pattern:doc *  Add documentation to tags matching given pattern.
//                          Documentation is shown in `Test Details` and also as
//                          a tooltip in `Statistics by Tag`. Pattern can contain
//                          characters `*` (matches anything) and `?` (matches
//                          any char). Documentation can contain formatting
//                          similarly as with --doc option.
//                          Examples: --tagdoc mytag:My_documentation
//                                    --tagdoc regression:*See*_http://info.html
//                                    --tagdoc owner-*:Original_author
//    --tagstatlink pattern:link:title *  Add external links into `Statistics by
//                          Tag`. Pattern can contain characters `*` (matches
//                          anything) and `?` (matches any char). Characters
//                          matching to wildcard expressions can be used in link
//                          and title with syntax %N, where N is index of the
//                          match (starting from 1). In title underscores are
//                          automatically converted to spaces.
//                          Examples: --tagstatlink mytag:http://my.domain:Link
//                          --tagstatlink bug-*:http://tracker/id=%1:Bug_Tracker
//    --removekeywords all|passed|for|wuks|name:<pattern> *  Remove keyword data
//                          from the generated log file. Keywords containing
//                          warnings are not removed except in `all` mode.
//                          all:     remove data from all keywords
//                          passed:  remove data only from keywords in passed
//                                   test cases and suites
//                          for:     remove passed iterations from for loops
//                          wuks:    remove all but the last failing keyword
//                                   inside `BuiltIn.Wait Until Keyword Succeeds`
//                          name:<pattern>:  remove data from keywords that match
//                                   the given pattern. The pattern is matched
//                                   against the full name of the keyword (e.g.
//                                   'MyLib.Keyword', 'resource.Second Keyword'),
//                                   is case, space, and underscore insensitive,
//                                   and may contain `*` and `?` as wildcards.
//                                   Examples: --removekeywords name:Lib.HugeKw
//                                             --removekeywords name:myresource.*
//    --flattenkeywords for|foritem|name:<pattern> *  Flattens matching keywords
//                          in the generated log file. Matching keywords get all
//                          log messages from their child keywords and children
//                          are discarded otherwise.
//                          for:     flatten for loops fully
//                          foritem: flatten individual for loop iterations
//                          name:<pattern>:  flatten matched keywords using same
//                                   matching rules as with
//                                   `--removekeywords name:<pattern>`
//    --listener class *    A class for monitoring test execution. Gets
//                          notifications e.g. when a test case starts and ends.
//                          Arguments to listener class can be given after class
//                          name, using colon as separator. For example:
//                          --listener MyListenerClass:arg1:arg2
//    --warnonskippedfiles  If this option is used, skipped test data files will
//                          cause a warning that is visible in the console output
//                          and the log file. By default skipped files only cause
//                          an info level syslog message.
//    --nostatusrc          Sets the return code to zero regardless of failures
//                          in test cases. Error codes are returned normally.
//    --runemptysuite       Executes tests also if the top level test suite is
//                          empty. Useful e.g. with --include/--exclude when it
//                          is not an error that no test matches the condition.
//    --dryrun              Verifies test data and runs tests so that library
//                          keywords are not executed.
//    --exitonfailure       Stops test execution if any critical test fails.
//    --exitonerror         Stops test execution if any error occurs when parsing
//                          test data, importing libraries, and so on.
//    --skipteardownonexit  Causes teardowns to be skipped if test execution is
//                          stopped prematurely.
//    --randomize all|suites|tests|none  Randomizes the test execution order.
//                          all:    randomizes both suites and tests
//                          suites: randomizes suites
//                          tests:  randomizes tests
//                          none:   no randomization (default)
//                          Use syntax `VALUE:SEED` to give a custom random seed.
//                          The seed must be an integer.
//                          Examples: --randomize all
//                                    --randomize tests:1234
//    --runmode mode *      Deprecated in version 2.8. Use individual options
//                          --dryrun, --exitonfailure, --skipteardownonexit, or
//                          --randomize instead.
// -W --monitorwidth chars  Width of the monitor output. Default is 78.
// -C --monitorcolors auto|on|ansi|off  Use colors on console output or not.
//                          auto: use colors when output not redirected (default)
//                          on:   always use colors
//                          ansi: like `on` but use ANSI colors also on Windows
//                          off:  disable colors altogether
//                          Note that colors do not work with Jython on Windows.
// -K --monitormarkers auto|on|off  Show `.` (success) or `F` (failure) on
//                          console when top level keywords in test cases end.
//                          Values have same semantics as with --monitorcolors.
// -P --pythonpath path *   Additional locations (directories, ZIPs, JARs) where
//                          to search test libraries from when they are imported.
//                          Multiple paths can be given by separating them with a
//                          colon (`:`) or using this option several times. Given
//                          path can also be a glob pattern matching multiple
//                          paths but then it normally must be escaped or quoted.
//                          Examples:
//                          --pythonpath libs/
//                          --pythonpath /opt/testlibs:mylibs.zip:yourlibs
//                          -E star:STAR -P lib/STAR.jar -P mylib.jar
// -E --escape what:with *  Escape characters which are problematic in console.
//                          `what` is the name of the character to escape and
//                          `with` is the string to escape it with. Note that
//                          all given arguments, incl. data sources, are escaped
//                          so escape characters ought to be selected carefully.
//                          <--------------------ESCAPES------------------------>
//                          Examples:
//                          --escape space:_ --metadata X:Value_with_spaces
//                          -E space:SP -E quot:Q -v var:QhelloSPworldQ
// -A --argumentfile path *  Text file to read more arguments from. Use special
//                          path `STDIN` to read contents from the standard input
//                          stream. File can have both options and data sources
//                          one per line. Contents do not need to be escaped but
//                          spaces in the beginning and end of lines are removed.
//                          Empty lines and lines starting with a hash character
//                          (#) are ignored.
//                          Example file:
//                          |  --include regression
//                          |  --name Regression Tests
//                          |  # This is a comment line
//                          |  my_tests.html
//                          |  path/to/test/directory/
//                          Examples:
//                          --argumentfile argfile.txt --argumentfile STDIN
// -h -? --help             Print usage instructions.
// --version                Print version information.
//
//Options that are marked with an asterisk (*) can be specified multiple times.
//For example, `--test first --test third` selects test cases with name `first`
//and `third`. If other options are given multiple times, the last value is used.
//
//Long option format is case-insensitive. For example, --SuiteStatLevel is
//equivalent to but easier to read than --suitestatlevel. Long options can
//also be shortened as long as they are unique. For example, `--logti Title`
//works while `--lo log.html` does not because the former matches only --logtitle
//but the latter matches --log, --loglevel and --logtitle.
//
//Environment Variables
//=====================
//
//ROBOT_OPTIONS             Space separated list of default options to be placed
//                          in front of any explicit options on the command line.
//ROBOT_SYSLOG_FILE         Path to a file where Robot Framework writes internal
//                          information about parsing test case files and running
//                          tests. Can be useful when debugging problems. If not
//                          set, or set to special value `NONE`, writing to the
//                          syslog file is disabled.
//ROBOT_SYSLOG_LEVEL        Log level to use when writing to the syslog file.
//                          Available levels are the same as for --loglevel
//                          command line option and the default is INFO.
//</editor-fold>

  public static int runas(String givenScriptScript, boolean silent) {
    if (!runTime.runningMac) {
      return -1;
    }
    String prefix = silent ? "!" : "";
    String osascriptShebang = "#!/usr/bin/osascript\n";
    givenScriptScript = osascriptShebang + givenScriptScript;
    File aFile = FileManager.createTempFile("script");
    aFile.setExecutable(true);
    FileManager.writeStringToFile(givenScriptScript, aFile);
    String retVal = runTime.runcmd(new String[]{prefix + aFile.getAbsolutePath()});
    String[] parts = retVal.split("\n");
    int retcode = -1;
    try {
      retcode = Integer.parseInt(parts[0]);
    } catch (Exception ex) {
    }
    if (retcode != 0) {
      if (silent) {
        log(lvl, "AppleScript:\n%s\nreturned:\n%s", givenScriptScript, runTime.getLastCommandResult());
      } else {
        log(-1, "AppleScript:\n%s\nreturned:\n%s", givenScriptScript, runTime.getLastCommandResult());
      }
    }
    return retcode;
  }

  public static int runps(String givenScriptScript) {
    if (!runTime.runningWindows) {
      return -1;
    }
    File aFile = FileManager.createTempFile("ps1");
    FileManager.writeStringToFile(givenScriptScript, aFile);
    String[] psDirect = new String[]{
            "powershell.exe", "-ExecutionPolicy", "UnRestricted",
            "-NonInteractive", "-NoLogo", "-NoProfile", "-WindowStyle", "Hidden",
            "-File", aFile.getAbsolutePath()
    };
    String[] psCmdType = new String[]{
            "cmd.exe", "/S", "/C",
            "type " + aFile.getAbsolutePath() + " | powershell -noprofile -"
    };
    String retVal = runTime.runcmd(psCmdType);
    String[] parts = retVal.split("\\s");
    int retcode = -1;
    try {
      retcode = Integer.parseInt(parts[0]);
    } catch (Exception ex) {
    }
    if (retcode != 0) {
      log(-1, "PowerShell:\n%s\nreturned:\n%s", givenScriptScript, runTime.getLastCommandResult());
    }
    return retcode;
  }

  public static int runpy(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return new RunBox().runpy(fScript, uGivenScript, givenScriptScript, args);
  }

  public static int runrb(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return new RunBox().runrb(fScript, uGivenScript, givenScriptScript, args);
  }

  public static int runtxt(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return new RunBox().runtxt(fScript, uGivenScript, givenScriptScript, args);
  }

  static class RunBox {

    String jsScript;

    public RunBox() {
    }

    public static int runtxt(File fScript, URL script, String scriptName, String[] args) {
      Runner.log(-1, "Running plain text scripts not yet supported!");
      return -999;
    }

    public static int runrb(File fScript, URL script, String scriptName, String[] args) {
      Runner.log(-1, "Running Ruby scripts not yet supported!");
      return -999;
    }

    public static int runpy(File fScript, URL script, String scriptName, String[] args) {
      String fpScript;
      if (fScript == null) {
        fpScript = script.toExternalForm();
      } else {
        fpScript = fScript.getAbsolutePath();
      }
      if (!Runner.initpy()) {
        Runner.log(-1, "Running Python scripts:init failed");
        return -999;
      }
      if (args == null || args.length == 0) {
        args = RunTime.get().getArgs();
      }
      String[] newArgs = new String[args.length + 1];
      for (int i = 0; i < args.length; i++) {
        newArgs[i + 1] = args[i];
      }
      Runner.pyRunner.setSysArgv(newArgs);
      newArgs[0] = fpScript;
      int retval;
      if (fScript == null) {
        ImagePath.addHTTP(fpScript);
        retval = (Runner.pyRunner.exec(scriptName) ? 0 : -1);
        ImagePath.removeHTTP(fpScript);
      } else {
        if (null == ImagePath.getBundlePathSet())
          ImagePath.setBundlePath(fScript.getParent());
        else {
          ImagePath.add(fScript.getParent());
        }
        retval = Runner.pyRunner.execfile(fpScript);
      }
      return retval;
    }

    public void runjsEval(String script) {
      if (script.isEmpty()) {
        return;
      }
      String initSikulix = "";
      if (Runner.jsRunner == null) {
        Runner.jsRunner = Runner.initjs();
        initSikulix = Runner.prologjs(initSikulix);
      }
      try {
        if (!initSikulix.isEmpty()) {
          initSikulix = Runner.prologjs(initSikulix);
          Runner.jsRunner.eval(initSikulix);
        }
        Runner.log(lvl, "JavaScript: eval: %s", script);
        jsScript = script;
        Thread evalThread = new Thread() {
          @Override
          public void run() {
            try {
              Runner.mHide.invoke(null, new Class[0]);
              Runner.jsRunner.eval(jsScript);
              Runner.mShow.invoke(null, new Class[0]);
            } catch (Exception ex) {
              Runner.log(-1, "not possible:\n%s", ex);
              try {
                Runner.mShow.invoke(null, new Class[0]);
              } catch (Exception e) {
                Sikulix.terminate(901);
              }
            }
          }
        };
        evalThread.start();
      } catch (Exception ex) {
        Runner.log(-1, "init not possible:\n%s", ex);
      }
    }

    public int runjs(File fScript, URL script, String scriptName, String[] args) {
      String initSikulix = "";
      if (Runner.jsRunner == null) {
        Runner.jsRunner = Runner.initjs();
        initSikulix = Runner.prologjs(initSikulix);
      }
      try {
        if (null != fScript) {
          File innerBundle = new File(fScript.getParentFile(), scriptName + ".sikuli");
          if (innerBundle.exists()) {
            ImagePath.setBundlePath(innerBundle.getCanonicalPath());
          } else {
            ImagePath.setBundlePath(fScript.getParent());
          }
        } else if (script != null) {
          ImagePath.addHTTP(script.toExternalForm());
          String sname = new File(script.toExternalForm()).getName();
          ImagePath.addHTTP(script.toExternalForm() + "/" + sname + ".sikuli");
        }
        if (!initSikulix.isEmpty()) {
          initSikulix = Runner.prologjs(initSikulix);
          Runner.jsRunner.eval(initSikulix);
          initSikulix = "";
        }
        if (null != fScript) {
          Runner.jsRunner.eval(new FileReader(fScript));
        } else {
          Runner.jsRunner.eval(scriptName);
        }
      } catch (Exception ex) {
        Runner.log(-1, "not possible:\n%s", ex);
      }
      return 0;
    }

    //    static File scriptProject = null;
//    static URL uScriptProject = null;
    RunTime runTime = RunTime.get();
    boolean asTest = false;
    String[] args = new String[0];

    String givenScriptHost = "";
    String givenScriptFolder = "";
    String givenScriptName = "";
    String givenScriptScript = "";
    String givenScriptType = "sikuli";
    String givenScriptScriptType = RDEFAULT;
    URL uGivenScript = null;
    URL uGivenScriptFile = null;
    boolean givenScriptExists = true;

    RunBox(String givenName, String[] givenArgs, boolean isTest) {
      Object[] vars = Runner.runBoxInit(givenName, RunTime.scriptProject, RunTime.uScriptProject);
      givenScriptHost = (String) vars[0];
      givenScriptFolder = (String) vars[1];
      givenScriptName = (String) vars[2];
      givenScriptScript = (String) vars[3];
      givenScriptType = (String) vars[4];
      givenScriptScriptType = (String) vars[5];
      uGivenScript = (URL) vars[6];
      uGivenScriptFile = (URL) vars[7];
      givenScriptExists = (Boolean) vars[8];
      RunTime.scriptProject = (File) vars[9];
      RunTime.uScriptProject = (URL) vars[10];
      args = givenArgs;
      asTest = isTest;
    }

    int run() {
      if (Runner.RASCRIPT.equals(givenScriptScriptType)) {
        return Runner.runas(givenScriptScript);
      } else if (Runner.RSSCRIPT.equals(givenScriptScriptType)) {
        return Runner.runps(givenScriptScript);
      } else if (Runner.RRSCRIPT.equals(givenScriptScriptType)) {
        return Runner.runrobot(givenScriptScript);
      }
      int exitCode = 0;
      log(lvl, "givenScriptName:\n%s", givenScriptName);
      if (-1 == FileManager.slashify(givenScriptName, false).indexOf("/") && RunTime.scriptProject != null) {
        givenScriptName = new File(RunTime.scriptProject, givenScriptName).getPath();
      }
      if (givenScriptName.endsWith(".skl")) {
        log(-1, "RunBox.run: .skl scripts not yet supported.");
        return -9999;
//        givenScriptName = FileManager.unzipSKL(givenScriptName);
//        if (givenScriptName == null) {
//          log(-1, "not possible to make .skl runnable");
//          return -9999;
//        }
      }
      if ("NET".equals(givenScriptType)) {
        if (Runner.RJSCRIPT.equals(givenScriptScriptType)) {
          exitCode = runjs(null, uGivenScript, givenScriptScript, args);
        } else if (Runner.RPYTHON.equals(givenScriptScriptType)) {
          exitCode = runpy(null, uGivenScript, givenScriptScript, args);
        } else {
          log(-1, "running from net not supported for %s\n%s", givenScriptScriptType, uGivenScript);
        }
      } else {
        File fScript = Runner.getScriptFile(new File(givenScriptName));
        if (fScript == null) {
          return -9999;
        }
        fScript = new File(FileManager.normalizeAbsolute(fScript.getPath(), true));
        if (null == RunTime.scriptProject) {
          RunTime.scriptProject = fScript.getParentFile().getParentFile();
        }
        log(lvl, "Trying to run script:\n%s", fScript);
        if (fScript.getName().endsWith(EJSCRIPT)) {
          exitCode = runjs(fScript, null, givenScriptScript, args);
        } else if (fScript.getName().endsWith(EPYTHON)) {
          exitCode = runpy(fScript, null, givenScriptScript, args);
        } else if (fScript.getName().endsWith(ERUBY)) {
          exitCode = runrb(fScript, null, givenScriptScript, args);
        } else if (fScript.getName().endsWith(EPLAIN)) {
          exitCode = runtxt(fScript, null, givenScriptScript, args);
        } else {
          log(-1, "Running not supported currently for:\n%s", fScript);
          return -9999;
        }
      }
      return exitCode;
    }
  }
}
