/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;

import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

public class AppleScriptRunner extends ProcessRunner {

  public static final String NAME = "AppleScript";
  public static final String TYPE = "text/applescript";
  public static final String[] EXTENSIONS = new String[] {"scpt", "scptd", "applescript"};

  @Override
  protected int doEvalScript(String script, IScriptRunner.Options options) {
    return super.doRunScript("osascript", new String[]{"-e", script}, options);
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IScriptRunner.Options options) {
    String path = new File(scriptFile).getAbsolutePath();
    return super.doRunScript("osascript", new String[] {path}, options);
  }

  @Override
  public boolean isSupported() {
    return RunTime.get().runningMac;
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
