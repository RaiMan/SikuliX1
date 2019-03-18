package org.sikuli.script.runners;

import java.net.URI;
import java.util.Map;

import org.sikuli.basics.Debug;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.SikuliXception;

public class InvalidRunner extends AbstractScriptRunner {
  
  String identifier;
    
  public InvalidRunner(String identifier) {
    super();
    this.identifier = identifier;
  }

  public InvalidRunner(Class<? extends IScriptRunner> cl) {
    this(cl.getName());
  }
  
  private void logError() {
    Debug.log(-1, "Invalid runner: %s", identifier);
  }

  @Override
  public int runScript(URI scriptfile, String[] scriptArgs, Map<String, Object> options) {  
    logError();
    return -1;
  }

  @Override
  public int evalScript(String script, Map<String, Object> options) {
    logError();
    return -1;
  }

  @Override
  public void runLines(String lines, Map<String, Object> options) {
    logError();
  }

  @Override
  public int runTest(URI scriptfile, URI imagedirectory, String[] scriptArgs, Map<String, Object> options) {
    logError();
    return -1;
  }

  @Override
  public int runInteractive(String[] scriptArgs) {
    logError();
    return -1;
  }

  @Override
  public String getCommandLineHelp() {
    logError();
    return null;
  }

  @Override
  public String getInteractiveHelp() {
    logError();
    return null;
  }

  @Override
  public boolean isSupported() {  
    return false;
  }

  @Override
  public String getName() {    
    return "InvalidRunner";
  }

  @Override
  public String[] getExtensions() {
    // TODO Auto-generated method stub
    return new String[]{};
  }

  @Override
  public String getType() {    
    return "invalid";
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean doSomethingSpecial(String action, Object[] args) {
    logError();
    return false;
  }

  @Override
  public void execBefore(String[] stmts) {
    logError();

  }

  @Override
  public void execAfter(String[] stmts) {
    logError();

  }

  @Override
  protected void doInit(String[] args) throws Exception {
    logError();
    throw new SikuliXception("Invalid");
  }

}
