package org.sikuli.script.runners;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;

public class PythonRunner extends AbstractScriptRunner {

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
    if (RunTime.hasPython()) {
      return true;
    }
    return false;
  }

  @Override
  protected int doRunScript(String scriptfile, String[] scriptArgs, IScriptRunner.Options options) {
    if (!isSupported()) {
      return -1;
    }
    Debug.info("Python: running script: %s", scriptfile);
    return 0;
  }

  @Override
  public void execBefore(String[] stmts) {

  }

  @Override
  public void execAfter(String[] stmts) {

  }
}
