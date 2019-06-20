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
  protected int doRunScript(String scriptFileOrFolder, String[] scriptArgs, IScriptRunner.Options options) {
    File scriptFile = new File(scriptFileOrFolder);

    File innerScriptFile = Runner.getScriptFile(scriptFile);

    if (null != innerScriptFile) {
      try {
        IScriptRunner runner = Runner.getRunner(innerScriptFile.getAbsolutePath());
        wrapper.setRunner(runner);
        return runner.runScript(innerScriptFile.getAbsolutePath(), scriptArgs, options);
      } finally {
        wrapper.clearRunner();
      }
    } else {
      log(-1, "runScript: not runnable: %s", scriptFile);
      return Runner.FILE_NOT_FOUND;
    }
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
