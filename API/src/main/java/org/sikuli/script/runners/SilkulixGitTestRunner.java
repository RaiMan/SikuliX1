/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.sikuli.script.Runner;

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
  
  public static final String NAME = "git*";
  public static final String TYPE = "git*";
  public static final String[] EXTENSIONS = new String[] {"git*"};
  
  String GIT_SCRIPTS = "https://github.com/RaiMan/SikuliX-2014/tree/master/TestScripts/";
  
  @Override
  public int runScript(URI scriptfile, String[] scriptArgs, Map<String,Object> options) {
    if (scriptfile.toString().endsWith(GIT_SCRIPTS)) {
      scriptfile = URI.create(scriptfile.toString() + "showcase");            
    }    
    return super.runScript(scriptfile, scriptArgs, options);    
  }

  @Override
  public int evalScript(String script, Map<String,Object> options) {
    return -1;
  }

  @Override
  public void runLines(String lines, Map<String,Object> options) {
    // TODO Auto-generated method stub

  }

  @Override
  public int runTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, Map<String,Object> options) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int runInteractive(String[] scriptArgs) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getCommandLineHelp() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getInteractiveHelp() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isSupported() {
    // TODO Auto-generated method stub
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

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean doSomethingSpecial(String action, Object[] args) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void execBefore(String[] stmts) {
    // TODO Auto-generated method stub

  }

  @Override
  public void execAfter(String[] stmts) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doInit(String[] args) {
    // TODO Auto-generated method stub

  }

}
