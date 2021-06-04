/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support.runner;

/**
 * Runs a packed sikulix script
 *
 * @author mbalmer
 */

public class SKLRunner extends ZipRunner {

  public static final String NAME = "PackedSikulix";
  public static final String TYPE = "text/skl";
  public static final String[] EXTENSIONS = new String[] { "skl" };
  
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
