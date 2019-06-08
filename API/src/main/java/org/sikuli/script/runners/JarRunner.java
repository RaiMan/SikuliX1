/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

/**
 * Runs a sikulix script packed in a jar file
 * 
 * NOT SUPPORTED YET!
 * 
 * @author mbalmer
 *
 */

public class JarRunner extends AbstractScriptRunner {
  
  public static final String NAME = "RunnableJar";
  public static final String TYPE = "text/jar";
  public static final String[] EXTENSIONS = new String[] {"jar"};

  @Override
  public boolean isSupported() {    
    return false;
  }

  @Override
  public boolean isWrapper() {
    return true;
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
