/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.scriptrunner;

import org.sikuli.basics.Debug;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;
import org.sikuli.script.Screen;

import java.io.File;

public class JSScriptRunner implements IScriptRunner {

  private static final String me = "JSScriptRunner: ";
  private int lvl = 3;

  private void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private boolean isReady = false;
  private String[] fileEndings = new String[]{"js"};


  @Override
  public void init(String[] args) {
    if (isReady) {
      return;
    }
    new Screen();
    isReady = true;
  }

  @Override
  public void runLines(String lines){
    log(-1, "runLines: not yet implemented");
  }

  @Override
  public int runScript(File scriptfile, File imagedirectory, String[] scriptArgs, String[] forIDE) {
    log(lvl, "runJavaScript: running statements");
    File scriptFolder = scriptfile.getParentFile();
    Runner.run(scriptFolder.getAbsolutePath());
    return 0;
  }

  @Override
  public int runTest(File scriptfile, File imagedirectory, String[] scriptArgs, String[] forIDE) {
    return 0;
  }

  @Override
  public int runInteractive(String[] scriptArgs) {
    return 0;
  }

  @Override
  public String getCommandLineHelp() {
    return null;
  }

  @Override
  public String getInteractiveHelp() {
    return null;
  }

  @Override
  public String getName() {
    return Runner.RJSCRIPT;
  }

  @Override
  public String[] getFileEndings() {
    return fileEndings;
  }

  @Override
  public String hasFileEnding(String ending) {
    for (String suf : fileEndings) {
      if (suf.equals(ending.toLowerCase())) {
        return suf;
      }
    }
    return null;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean doSomethingSpecial(String action, Object[] args) {
    return false;
  }

  @Override
  public void execBefore(String[] stmts) {

  }

  @Override
  public void execAfter(String[] stmts) {

  }
}
