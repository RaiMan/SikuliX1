/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

/**
 * INTERNAL USE
 */
public class SikuliXception extends RuntimeException {

  public SikuliXception(String message) {
    super(message);
  }
  
  public SikuliXception(String message, Exception e) {
    super(message, e);
  }
}
