package org.sikuli.script.runners;

import org.sikuli.script.RunTime;

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
    if (RunTime.Start.hasPython()) {
      return true;
    }
    return false;
  }
}
