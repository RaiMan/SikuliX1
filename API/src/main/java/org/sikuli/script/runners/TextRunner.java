/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

/**
 * Runs a text file.
 *
 * NOT SUPPORTED YET (and I don't even know what to support here).
 *
 * @author mbalmer
 *
 */

public class TextRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "Text";
  public static final String TYPE = "text/text";
  public static final String[] EXTENSIONS = new String[] {"txt"};

  @Override
  public boolean isSupported() {
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
