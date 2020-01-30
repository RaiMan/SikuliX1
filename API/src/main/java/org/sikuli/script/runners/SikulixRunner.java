/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;

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
    File file = new File(identifier);

    if (file.isDirectory()) {
      File innerScriptFile = Runner.getScriptFile(file);
      return null != innerScriptFile;
    }

    return false;
  }

  @Override
  protected int doRunScript(String scriptFolder, String[] scriptArgs, IScriptRunner.Options options) {
    EffectiveRunner runnerAndFile = getEffectiveRunner(scriptFolder);
    IScriptRunner runner = runnerAndFile.getRunner();
    String innerScriptFile = runnerAndFile.getScript();
    if (null != runner) {
      try {
        wrapper.setRunner(runner);
        return runner.runScript(innerScriptFile, scriptArgs, options);
      } finally {
        wrapper.clearRunner();
      }
    }
    return Runner.FILE_NOT_FOUND;
  }

  public EffectiveRunner getEffectiveRunner(String scriptFileOrFolder) {
    File scriptFile = new File(scriptFileOrFolder);
    File innerScriptFile = Runner.getScriptFile(scriptFile);
    if (null != innerScriptFile) {
      String innerScriptFilePath = innerScriptFile.getAbsolutePath();
      return new EffectiveRunner(Runner.getRunner(innerScriptFilePath), innerScriptFilePath, true);
    }
    return new EffectiveRunner(null, null, false);
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
