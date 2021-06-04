/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.runner;

import org.sikuli.basics.Debug;
import org.sikuli.support.FileManager;
import org.sikuli.support.ide.ExtensionManager;

import java.io.File;
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

  @Override
  protected int doRunScript(String scriptfile, String[] scriptArgs, IRunner.Options options) {
    if (!isSupported()) {
      return -1;
    }
//TODO    RunTime.startPythonServer();
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
