/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.net.URI;
import java.util.Map;

import org.sikuli.basics.Debug;
import org.sikuli.script.IScriptRunner;
import org.sikuli.script.SikuliXception;

public class InvalidRunner extends AbstractScriptRunner {
  
  String identifier;
    
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

  @Override
  public String getType() {    
    return "invalid/" + identifier;
  } 
}
