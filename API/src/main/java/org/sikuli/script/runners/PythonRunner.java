/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import org.apache.commons.io.FileUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.ExtensionManager;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PythonRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "Python";
  public static final String TYPE = "text/python";
  public static final String[] EXTENSIONS = new String[]{"py"};

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public boolean isSupported() {
//    if (ExtensionManager.hasPython()) {
//      return true;
//    }
    return false;
  }

  protected boolean doRedirect(PrintStream stdout, PrintStream stderr) {
    return false;
  }

  @Override
  protected int doRunScript(String scriptfile, String[] scriptArgs, IScriptRunner.Options options) {
    if (!isSupported()) {
      return -1;
    }
    if (Debug.isGlobalTrace()) {
      Debug.setDebugLevel(3);
    }
//    RunTime.startPythonServer();
    String scriptContent = FileManager.readFileToString(new File(scriptfile));
    Debug.log(3,"Python: running script: %s\n%s\n********** end", scriptfile, scriptContent);
    List<String> runArgs = new ArrayList<>();
    runArgs.add(ExtensionManager.getPython());
    runArgs.add(scriptfile);
    runArgs.addAll(Arrays.asList(scriptArgs));
    String runOut = ProcessRunner.run(runArgs);
    int runExitValue = 0;
    if (!runOut.startsWith("0\n")) {
      Debug.error("%s", runOut);
    } else {
      Debug.logp("%s", runOut.substring(2));
    }
    return 0;
  }
}
