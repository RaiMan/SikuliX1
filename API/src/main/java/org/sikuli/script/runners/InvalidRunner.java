/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import org.sikuli.script.support.IScriptRunner;

public class InvalidRunner extends AbstractScriptRunner {

  String identifier;

  public InvalidRunner() {}

  public InvalidRunner(String identifier) {
    super();
    this.identifier = identifier;
  }

  public InvalidRunner(Class<? extends IScriptRunner> cl) {
    this(cl.getName());
  }

  @Override
  public String getName() {
    return "InvalidRunner: " + identifier;
  }

  @Override
  public String[] getExtensions() {
    // TODO Auto-generated method stub
    return new String[]{};
  }

  public Object[] getEffectiveRunner(String script) {
    Object[] returnValue = new Object[]{null, null, null};
    returnValue[0] = this;
    returnValue[2] = false;
    return returnValue;
  }

  @Override
  public String getType() {
    return "invalid/" + identifier;
  }
}
