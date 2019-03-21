/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import org.sikuli.basics.Debug;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;
import org.sikuli.script.Screen;
import org.sikuli.script.SikuliXception;
import org.sikuli.script.Sikulix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptRunner extends AbstractScriptRunner {

  public static final String NAME = "JavaScript";
  public static final String TYPE = "text/javascript";
  public static final String[] EXTENSIONS = new String[] {"js"};

  private static final RunTime RUN_TIME = RunTime.get();

  private static String BEFORE_JS_JAVA_8 = "load(\"nashorn:mozilla_compat.js\");";
  private static String BEFORE_JS
          = "importPackage(Packages.org.sikuli.script); "
          + "importClass(Packages.org.sikuli.basics.Debug); "
          + "importClass(Packages.org.sikuli.basics.Settings);";

  private static final String me = "JSScriptRunner: ";
  private int lvl = 3;

//  Class cIDE;
//  Method mShow;
//  Method mHide;

  ScriptEngine engine;

  @Override
  protected void doInit(String[] args) throws Exception {
    Debug.info("JavaScript init");
    ScriptEngineManager jsFactory = new ScriptEngineManager();
    ScriptEngine engine = jsFactory.getEngineByName("JavaScript");

    log(lvl, "ScriptingEngine started: JavaScript (ending .js)");

    String prolog = "";
    if(RUN_TIME.isJava8()) {
      prolog += BEFORE_JS_JAVA_8;
    }
    prolog += BEFORE_JS;
    prolog += RUN_TIME.extractResourceToString("JavaScript", "commands.js", "");
    engine.eval(prolog);

    // TODO move this to proper place
//    if (RunTime.Type.IDE.equals(RunTime.get().runType)) {
//      try {
//        cIDE = Class.forName("org.sikuli.ide.SikuliIDE");
//        mHide = cIDE.getMethod("hideIDE", new Class[0]);
//        mShow = cIDE.getMethod("showIDE", new Class[0]);
//      } catch (Exception ex) {
//        log(-1, "initjs: getIDE");
//      }
//    }
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, Map<String,Object> options) {
    log(lvl, "runJavaScript: running statements");
    try {
      engine.eval(new FileReader(new File(scriptFile)));
    } catch (FileNotFoundException | ScriptException e) {
      log(-1, "runTest failed", e);
      return -1;
    }
    return 0;
  }

  @Override
  protected int doEvalScript(String script, Map<String,Object> options) {
    try {
      boolean silent = false;
      if (script.startsWith("#")) {
        script = script.substring(1);
        silent = true;
      }


      engine.eval(script);
    } catch (ScriptException e) {
      log(-1, "evalScript failed", e);
      return -1;
    }
    return 0;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isSupported() {
    ScriptEngineManager jsFactory = new ScriptEngineManager();
    return jsFactory.getEngineByName("JavaScript") != null;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS.clone();
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
