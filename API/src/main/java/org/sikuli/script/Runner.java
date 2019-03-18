/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.runners.AbstractScriptRunner;
import org.sikuli.script.runners.AppleScriptRunner;
import org.sikuli.script.runners.InvalidRunner;
import org.sikuli.script.runners.JavaScriptRunner;
import org.sikuli.script.runners.PowershellRunner;
import org.sikuli.script.runners.RobotRunner;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;
import org.sikuli.util.JythonHelper;

import com.kenai.jffi.Array;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * INTERNAL USE --- NOT official API<br>
 *   not in version 2
 */
public class Runner {

  static final String me = "Runner: ";
  static final int lvl = 3;
  static final RunTime runTime = RunTime.get();

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
     
  private static List<IScriptRunner> runners = new LinkedList<>();
  private static Map<String, IScriptRunner> resolvedRunners = new HashMap<>();
    
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
    if (lvl > 2) {
      runTime.printArgs();
    }
 
    String[] runScripts = null;
    runTime.runningTests = false;
    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
      runScripts = cmdLine.getOptionValues(CommandArgsEnum.RUN.longname());
    }

    return runScripts;
  }

  public static int run(String givenName) {
    return run(givenName, new String[0]);
  }
  
  private static boolean isReady = false;
 
  public static void initRunners(boolean async) {
    synchronized(runners) {
      if (isReady) {
        return;
      }
           
      log(lvl, "initScriptingSupport: enter");
      if (runners.isEmpty()) {
                           
        Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.sikuli.script.runners"), new SubTypesScanner());
        
        Set<Class<? extends AbstractScriptRunner>> classes = reflections.getSubTypesOf(AbstractScriptRunner.class);                 
        
        for (Class<? extends AbstractScriptRunner> cl : classes) {
          IScriptRunner current = null;
                 
          try {
            current = cl.getConstructor().newInstance();
          } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
              | InvocationTargetException | NoSuchMethodException | SecurityException e) {

            log(lvl, "initScriptingSupport: warning: %s", e.getMessage());
            continue;
          }             
         
          String name = current.getName();
          if (name != null && !name.startsWith("Not") && current.isSupported()) {
            runners.add(current);               
            log(lvl, "initScriptingSupport: added: %s", name);
          }
        }
      }
      if (runners.isEmpty()) {
        String em = "Terminating: No scripting support available. Rerun Setup!";
        log(-1, em);
        if (runTime.isRunningIDE) {
          Sikulix.popError(em, "IDE has problems ...");
        }
        System.exit(1);
      } else {
        //TODO JavaScript only script support
        IScriptRunner defaultRunner = runners.get(0);
        
        Runner.RDEFAULT = defaultRunner.getName();
        Runner.EDEFAULT = defaultRunner.getExtensions()[0];
        if (Runner.EDEFAULT == "js" && runners.size() > 1) {
          defaultRunner = runners.get(0);
          Runner.RDEFAULT = defaultRunner.getName();
          Runner.EDEFAULT = defaultRunner.getExtensions()[0];
        }                    
      }
      log(lvl, "initScriptingSupport: exit with defaultrunner: %s (%s)", Runner.RDEFAULT, Runner.EDEFAULT);
      isReady = true;
    }
  }
  
  public static IScriptRunner getRunner(String identifier) {
    if (identifier == null) {
      return null;
    }
    
    synchronized(runners) {            
      initRunners(false);          
                        
      IScriptRunner runner = resolvedRunners.get(identifier);      
      
      if(runner == null) {
        for(IScriptRunner r : runners) {
          if(r.canHandle(identifier)) {
            runner = r;
            resolvedRunners.put(identifier, r);
            break;
          }
        }
        
      }
                  
      if (runner != null) {
        runner.init(null);
      } else {
        log(-1, "getRunner: no runner found for:\n%s", identifier);
        runner = new InvalidRunner(identifier);
      }      
      
      return runner; 
    }
  }
  
  public static List<IScriptRunner> getRunners(){
    synchronized(runners) {          
      initRunners(false);
    
      return new LinkedList<IScriptRunner>(runners);
    }
  }
      
  public static IScriptRunner getRunner(Class<? extends IScriptRunner> runnerClass) {
    synchronized(runners) {            
      initRunners(false);
      
      for(IScriptRunner r : runners) {
        if(r.getClass().equals(runnerClass)) {
          r.init(null);
          return r;
        }
      }
    }
    return new InvalidRunner(runnerClass);
  }
  
  public static Set<String> getExtensions() {
    synchronized(runners) {            
      initRunners(false);
      
      Set<String> extensions = new HashSet<>();
      
      for (IScriptRunner runner : runners) {
        for(String ex : runner.getExtensions()) {
           extensions.add(ex);   
        }     
      }
      
      return extensions;
    }
  }
  
  public static String getExtension(String identifier) {
    synchronized(runners) {            
      initRunners(false);
      
      String[] extensions = getRunner(identifier).getExtensions();
      
      if (extensions.length > 0) {
        return extensions[0];
      }
      
      return null;
    }
  } 
  

  public static synchronized int run(String givenName, String[] args) {
    String savePath = ImagePath.getBundlePathSet();
    
    int retVal = Runner.getRunner(givenName).runScript(URI.create(givenName), args, null);      
           
    if (savePath != null) {
      ImagePath.setBundlePath(savePath);
    }
    lastReturnCode = retVal;
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
      for (String givenScriptName : runScripts) {
        if (lastReturnCode == -1) {
          log(lvl, "Exit code -1: Terminating multi-script-run");
          break;
        }
        someJS = runTime.getOption("runsetup");
        if (!someJS.isEmpty()) {
          log(lvl, "Options.runsetup: %s", someJS);          
          getRunner(JavaScriptRunner.class).evalScript(someJS, null);                   
        }       
        exitCode = getRunner(givenScriptName).runScript(URI.create(givenScriptName), runTime.getArgs(), null);
        someJS = runTime.getOption("runteardown");
        if (!someJS.isEmpty()) {
          log(lvl, "Options.runteardown: %s", someJS);
          getRunner(JavaScriptRunner.class).evalScript(someJS, null); 
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
      for (IScriptRunner runner : getRunners()) {        
        for (String suffix : runner.getExtensions()) {
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
  
  /**
   * @Deprecated Use Runner.getRunner(JavaScriptRunner.class).evalScript(script, null)
   * 
   * @param script
   */
  @Deprecated
  public static void runjsEval(String script) {
    getRunner(JavaScriptRunner.class).evalScript(script, null);    
  }
      
  @Deprecated
  public static int runjs(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
     return Runner.run(fScript != null ? fScript.getAbsolutePath() : uGivenScript.toString());    
  }
  
  @Deprecated
  public static int runas(String givenScriptScript) {
    return Runner.getRunner(AppleScriptRunner.class).evalScript(givenScriptScript, null);    
  }
  
  @Deprecated
  public static int runrobot(String code) {
    return Runner.getRunner(RobotRunner.class).evalScript(code, null);
  }  
  
  @Deprecated
  public static int runas(String givenScriptScript, boolean silent) {
    Map<String,Object> options = new HashMap<>();
    options.put(AppleScriptRunner.SILENT_OPTION, silent);    
    return Runner.getRunner(AppleScriptRunner.class).evalScript(givenScriptScript, options);    
  }
  
  @Deprecated
  public static int runps(String givenScriptScript) {
    return Runner.getRunner(PowershellRunner.class).evalScript(givenScriptScript, null);
  }
  
  @Deprecated
  public static int runpy(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return Runner.run(fScript != null ? fScript.getAbsolutePath() : uGivenScript.toString(), args);       
  }
  
  @Deprecated
  public static int runrb(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return Runner.run(fScript != null ? fScript.getAbsolutePath() : uGivenScript.toString(), args); 
  }
  
  @Deprecated
  public static int runtxt(File fScript, URL uGivenScript, String givenScriptScript, String[] args) {
    return Runner.run(fScript != null ? fScript.getAbsolutePath() : uGivenScript.toString(), args); 
  }  
}
