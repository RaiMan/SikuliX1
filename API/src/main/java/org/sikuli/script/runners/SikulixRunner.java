/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.sikuli.script.IScriptRunner;
import org.sikuli.script.ImagePath;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;

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
    if (null == ImagePath.getBundlePathSet())
      ImagePath.setBundlePath(new File(scriptFile).getAbsolutePath());
    else {
      ImagePath.add(new File(scriptFile).getAbsolutePath());
    }
    
    File innerScriptFile = Runner.getScriptFile(new File(scriptFile));
            
    return Runner.run(innerScriptFile.getAbsolutePath());     
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
}
