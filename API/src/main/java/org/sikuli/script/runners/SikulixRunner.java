/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.ImagePath;
import org.sikuli.script.support.Runner;

/**
 * Runs Sikulix scripts.
 * 
 * A sikulix script is a directory (optionally with a .sikuli extension)
 *  
 * @author mbalmer
 *
 */

public class SikulixRunner extends AbstractScriptRunner {
  
  public static final String NAME = "Sikulix";
  public static final String TYPE = "directory/sikulix";
  public static final String[] EXTENSIONS = new String[] {"sikuli"};

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    if (!ImagePath.hasBundlePath())
      ImagePath.setBundlePath(new File(scriptFile).getAbsolutePath());
    else {
      ImagePath.add(new File(scriptFile).getAbsolutePath());
    }
    
    File innerScriptFile = Runner.getScriptFile(new File(scriptFile));
            
    return Runner.run(innerScriptFile.getAbsolutePath(), scriptArgs, null);
  }
   
  @Override
  public boolean isSupported() {    
    return false;
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
}
