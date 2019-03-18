package org.sikuli.script.runners;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.sikuli.script.ImagePath;
import org.sikuli.script.Runner;

/**
 * Runs a text file.
 * 
 * NOT SUPPORTED YET.
 * 
 * @author mbalmer
 *
 */

public class SikulixRunner extends AbstractScriptRunner {
  
  public static final String NAME = "Sikulix";
  public static final String TYPE = "directory/sikulix";
  public static final String[] EXTENSIONS = new String[] {"sikuli"};

  @Override
  public int runScript(URI scriptfile, String[] scriptArgs, Map<String,Object> options) {
    if (null == ImagePath.getBundlePathSet())
      ImagePath.setBundlePath(new File(scriptfile).getAbsolutePath());
    else {
      ImagePath.add(new File(scriptfile).getAbsolutePath());
    }
    
    File innerScriptFile = Runner.getScriptFile(new File(scriptfile));
    
    return Runner.run(innerScriptFile.getAbsolutePath());     
  }
   
  @Override
  public int evalScript(String script, Map<String,Object> options) {
    // TODO Auto-generated method stub
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
    return true;
  }
  
  @Override 
  public boolean canHandle(String identifier) {       
     return new File(identifier).isDirectory();    
  }

  @Override
  public String getName() {   
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    // TODO Auto-generated method stub
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
