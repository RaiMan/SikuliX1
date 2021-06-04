/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.runner;

import org.sikuli.basics.Debug;
import org.sikuli.support.ide.Runner;

import java.io.File;

public class InvalidRunner extends AbstractRunner {

  String identifier;

  public InvalidRunner() {}

  public InvalidRunner(String identifier) {
    super();
    this.identifier = identifier;
  }

  public InvalidRunner(Class<? extends IRunner> cl) {
    this(cl.getName());
  }

  @Override
  public String getName() {
    if (null == identifier) {
      return "InvalidRunner";
    }
    return "InvalidRunner: " + identifier;
  }

  @Override
  public String[] getExtensions() {
    // TODO Auto-generated method stub
    return new String[]{};
  }

  public EffectiveRunner getEffectiveRunner(String script) {
    return new EffectiveRunner(this, null, false);
  }

  @Override
  public String getType() {
    return "invalid/" + identifier;
  }

  protected int doRunScript(String scriptfile, String[] scriptArgs, IRunner.Options options) {
    Debug.error("no runner available for: %s", new File(scriptfile).getName());
    return Runner.NOT_SUPPORTED;
  }
}
