package org.sikuli.script.runners;

import java.net.URI;
import java.util.Map;

/**
 * Runs a text file.
 * 
 * NOT SUPPORTED YET.
 * 
 * @author mbalmer
 *
 */

public class TextRunner extends AbstractScriptRunner {

  @Override
  public int runScript(URI scriptfile, String[] scriptArgs, Map<String,Object> options) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int evalScript(String script, Map<String,Object> options) {
    // TODO Auto-generated method stub
    return 0;
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
    return false;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getExtensions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return null;
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
