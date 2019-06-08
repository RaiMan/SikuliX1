/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.python.apache.commons.compress.compressors.FileNameUtil;
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
    String extension = FilenameUtils.getExtension(scriptFileOrFolder);
    File scriptFile = new File(scriptFileOrFolder);
    if (extension.equals("sikuli")) {
      File innerScriptFile = Runner.getScriptFile(scriptFile);
      if (null != innerScriptFile) {
        return Runner.run(innerScriptFile.getAbsolutePath(), scriptArgs, null);
      } else {
        log(-1, "runScript: not runnable: %s", scriptFile);
        return Runner.FILE_NOT_FOUND;
      }
    } else if (extension.equals("skl")) {
      //TODO SKLRunner
      return new SKLRunner().runScript(scriptFileOrFolder, scriptArgs, options);
    } else if (extension.equals("jar")) {
      //TODO JarRunner
      return new JarRunner().runScript(scriptFileOrFolder, scriptArgs, options);
    } else {
      return new InvalidRunner().runScript(scriptFileOrFolder, scriptArgs, options);
    }
  }
}
