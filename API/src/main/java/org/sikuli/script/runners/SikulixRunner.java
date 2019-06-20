/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.Runner;
import org.sikuli.util.AbortableScriptRunnerWrapper;

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

  private AbortableScriptRunnerWrapper wrapper = new AbortableScriptRunnerWrapper();

  @Override
  public boolean isSupported() {
    return true;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return new String[0];
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public boolean canHandle(String identifier) {
    for (String ending : new String[]{"", "sikuli"}) {
      if (FilenameUtils.getExtension(identifier).equals(ending)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String resolveRelativeFile(String script) {
    for (String ending : new String[]{"", ".sikuli"}) {
      String scriptFile = super.resolveRelativeFile(script + ending);
      if (null != scriptFile) {
        return scriptFile;
      }
    }
    return null;
  }

  @Override
  protected int doRunScript(String scriptFolder, String[] scriptArgs, IScriptRunner.Options options) {
    Object[] runnerAndFile = getEffectiveRunner(scriptFolder);
    IScriptRunner runner = (IScriptRunner) runnerAndFile[0];
    String innerScriptFile = (String) runnerAndFile[1];
    if (null != runner) {
      try {
        wrapper.setRunner(runner);
        return runner.runScript(innerScriptFile, scriptArgs, options);
      } finally {
        wrapper.clearRunner();
      }
    } else {
      if (FilenameUtils.getExtension(scriptFolder).equals("sikuli")) {
        log(-1, "runScript: not runnable: %s", scriptFolder);
        return Runner.FILE_NOT_FOUND;
      } else {
        options.setBaseFolder(new File(scriptFolder));
        return Runner.FILE_NOT_FOUND_TAKE_AS_FOLDER;
      }
    }
  }

  public Object[] getEffectiveRunner(String scriptFileOrFolder) {
    Object[] returnValue = new Object[] {null, null, null};
    File scriptFile = new File(scriptFileOrFolder);
    File innerScriptFile = Runner.getScriptFile(scriptFile);
    if (null != innerScriptFile) {
        returnValue[0] = Runner.getRunner(innerScriptFile.getAbsolutePath());
        returnValue[1] = innerScriptFile.getAbsolutePath();
        returnValue[2] = true;
    }
    return returnValue;
  }

  @Override
  public boolean isAbortSupported() {
    return wrapper.isAbortSupported();
  }

  @Override
  protected void doAbort() {
    wrapper.doAbort();
  }
}
