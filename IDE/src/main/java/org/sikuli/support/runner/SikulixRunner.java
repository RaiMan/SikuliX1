/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support.runner;

import org.sikuli.support.ide.Runner;

import java.io.File;

/**
 * Runs Sikulix scripts.
 * <p>
 * A sikulix script is a directory (optionally with a .sikuli extension)
 *
 * @author mbalmer
 */

public class SikulixRunner extends AbstractRunner {

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
  protected int doRunScript(String scriptFolder, String[] scriptArgs, IRunner.Options options) {
    EffectiveRunner runnerAndFile = getEffectiveRunner(scriptFolder);
    IRunner runner = runnerAndFile.getRunner();
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
