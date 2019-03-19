/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.net.URI;
import java.util.Map;

/**
 * Runs a packed sikulix script
 * 
 * NOT SUPPORTED YET!
 * 
 * @author mbalmer
 *
 */

public class SKLRunner extends AbstractScriptRunner {
  
  public static final String NAME = "PackedSikulix";
  public static final String TYPE = "text/skl";
  public static final String[] EXTENSIONS = new String[] {"skl"};

  @Override
  public boolean isSupported() {    
    return false;
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
