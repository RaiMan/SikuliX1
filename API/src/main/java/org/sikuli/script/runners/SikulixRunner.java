/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.ImagePath;
import org.sikuli.script.support.RunTime;
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
  public boolean isSupported() {
    return true;
  }

  @Override
  public boolean canHandle(String identifier) {
    File possibleScriptFileOrFolder = new File(identifier);
    if (possibleScriptFileOrFolder.isDirectory()) {
      return true;
    }
    String extension = FilenameUtils.getExtension(identifier);
    return extension.isEmpty() || "sikuli".equals(extension);
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
  protected int doRunScript(String scriptFileOrFolder, String[] scriptArgs, IScriptRunner.Options options) {
    File scriptFile = new File(scriptFileOrFolder);
    File scriptFolder = null;
    String workFolder = options.getWorkFolder();
    String scriptGiven = options.getScriptName();
    if (FilenameUtils.getExtension(scriptFile.getName()).isEmpty()) {
      scriptFolder = new File(scriptFile.getPath());
      scriptFile = new File(scriptFile.getPath() + ".sikuli");
    }
    scriptFile = Runner.checkScriptFolderOrFile(workFolder, scriptFile);
    if (!scriptFile.exists()) {
      if (null != scriptFolder) {
        log(3, "runScripts: %s as .sikuli not found - trying as folder", scriptGiven);
        scriptFile = Runner.checkScriptFolderOrFile(workFolder, scriptFolder);
      }
      if (!scriptFile.exists()) {
        return Runner.FILE_NOT_FOUND;
      }
    }

//TODO BundlePath

//    if (!ImagePath.hasBundlePath())
//      ImagePath.setBundlePath(new File(scriptFile).getAbsolutePath());
//    else {
//      ImagePath.add(new File(scriptFile).getAbsolutePath());
//    }
    
    options.setWorkFolder(scriptFile.getParent());
    File innerScriptFile = Runner.getScriptFile(scriptFile);
            
    return Runner.run(innerScriptFile.getAbsolutePath(), scriptArgs, null);
  }
}
