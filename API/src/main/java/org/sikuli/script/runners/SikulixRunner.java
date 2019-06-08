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
 * <p>
 * A sikulix script is a directory (optionally with a .sikuli extension)
 *
 * @author mbalmer
 */

public class SikulixRunner extends AbstractScriptRunner {

  public static final String NAME = "Sikulix";
  public static final String TYPE = "directory/sikulix";
  public static final String[] EXTENSIONS = new String[]{"sikuli", "skl", "jar"};

  @Override
  protected File checkWithExtensions(File scriptFile) {
    String scriptFilePath = scriptFile.getPath();
    for (String extension : getExtensions()) {
      File alternateFile = new File(scriptFilePath + "." + extension);
      if (alternateFile.exists()) {
        scriptFile = alternateFile;
        break;
      }
    }
    return scriptFile;
  }

  @Override
  public boolean isSupported() {
    return true;
  }

  @Override
  public boolean isWrapper() {
    return true;
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
      if (!scriptGiven.endsWith("/")) {
        scriptFile = new File(scriptFile.getPath() + ".sikuli");
      }
    }
    scriptFile = Runner.checkScriptFolderOrFile(workFolder, scriptFile);
    if (null == scriptFile || !scriptFile.exists()) {
      if (null != scriptFolder) {
        log(3, "runScripts: %s as .sikuli not found - trying as folder", scriptGiven);
        scriptFile = Runner.checkScriptFolderOrFile(workFolder, scriptFolder);
      }
      if (null == scriptFile || !scriptFile.exists()) {
        return Runner.FILE_NOT_FOUND;
      }
    }

//TODO BundlePath

//    if (!ImagePath.hasBundlePath())
//      ImagePath.setBundlePath(new File(scriptFile).getAbsolutePath());
//    else {
//      ImagePath.add(new File(scriptFile).getAbsolutePath());
//    }

    File innerScriptFile = Runner.getScriptFile(scriptFile);
    if (null != innerScriptFile) {
      options.setWorkFolder(scriptFile.getParent());
      return Runner.run(innerScriptFile.getAbsolutePath(), scriptArgs, null);
    } else {
      if (scriptFile.isFile()) {
        log(3, "not supported: (.%s) %s",
                FilenameUtils.getExtension(scriptFile.getPath()), scriptFile);
        options.setWorkFolder(scriptFile.getParent());
      } else {
        options.setWorkFolder(scriptFile.getPath());
      }
      return Runner.FILE_NOT_FOUND_SILENT;
    }
  }
}
