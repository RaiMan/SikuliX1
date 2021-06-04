/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.runner;

import java.io.File;

import org.sikuli.support.Commons;

public class AppleScriptRunner extends ProcessRunner {

  public static final String NAME = "AppleScript";
  public static final String TYPE = "text/applescript";
  public static final String[] EXTENSIONS = new String[] {"scpt", "scptd", "applescript"};

  private static final int LVL = 3;

  @Override
  protected void doInit(String[] args) throws Exception {
    doRedirect(null, null);
  }

  @Override
  protected int doEvalScript(String script, IRunner.Options options) {
    return super.doRunScript("osascript", new String[]{"-e", script}, options);
  }

  @Override
  protected int doRunScript(String scriptFile, String[] scriptArgs, IRunner.Options options) {
    String path = new File(scriptFile).getAbsolutePath();
    return super.doRunScript("osascript", new String[] {path}, options);
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
