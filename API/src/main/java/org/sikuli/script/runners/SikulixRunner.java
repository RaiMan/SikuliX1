/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.script.support.IScriptRunner;
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

  private IScriptRunner currentRunner;

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
  protected int doRunScript(String scriptFileOrFolder, String[] scriptArgs, IScriptRunner.Options options) {
    File scriptFile = new File(scriptFileOrFolder);

    File innerScriptFile = Runner.getScriptFile(scriptFile);

    if (null != innerScriptFile) {
      try {
        currentRunner = Runner.getRunner(innerScriptFile.getAbsolutePath());
        return currentRunner.runScript(innerScriptFile.getAbsolutePath(), scriptArgs, options);
      } finally {
        currentRunner = null;
      }
    } else {
      log(-1, "runScript: not runnable: %s", scriptFile);
      return Runner.FILE_NOT_FOUND;
    }
  }

  @Override
  public boolean isAbortSupported() {
    return null != currentRunner && currentRunner.isAbortSupported();
  }

  @Override
  protected void doAbort() {
    if (isAbortSupported()) {
      currentRunner.abort();
    }
  }

}
