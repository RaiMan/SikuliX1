/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.support.Commons;

public class AppleScriptRunner extends ProcessRunner {

  public static final String NAME = "AppleScript";
  public static final String TYPE = "text/applescript";
  public static final String[] EXTENSIONS = new String[] {"scpt", "scptd", "applescript"};

  private static final int LVL = 3;

  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    int retVal = -1;
    if (isSupported()) {
      retVal = super.doRunScript("osascript", new String[]{"-e", script}, options);
    }
    return retVal;
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    String path = new File(scriptFile).getAbsolutePath();
    return isSupported() ? super.doRunScript("osascript", new String[] {path}, options) : -1;
  }

  @Override
  public boolean isSupported() {
    return Commons.runningMac();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS.clone();
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
