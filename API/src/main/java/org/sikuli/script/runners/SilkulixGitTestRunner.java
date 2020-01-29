/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import org.sikuli.script.support.IScriptRunner;

/**
 * Runs the Sikulix test scripts from
 * 
 * https://github.com/RaiMan/SikuliX-2014/tree/master/TestScripts
 * 
 * TODO Check if this is still needed
 * 
 * @author mbalmer
 *
 */

public class SilkulixGitTestRunner extends NetworkRunner {
  
  public static final String NAME = "Git";
  public static final String TYPE = "git";
  public static final String[] EXTENSIONS = new String[0];
  
  String GIT_SCRIPTS = "https://github.com/RaiMan/SikuliX-2014/tree/master/TestScripts/";
    
  protected int doEvalScript(String scriptFile, IScriptRunner.Options options) {
    if (scriptFile.endsWith(GIT_SCRIPTS)) {
      scriptFile = scriptFile + "showcase";            
    }    
    return super.runScript(scriptFile, null, options);    
  }

  @Override
  public boolean isSupported() {
    return false;
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
}
