/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

/**
 * to activate Jython support use org.sikuli.script.SikulixForJython instead
 */
@Deprecated
public class SikulixForJython {

  static {
    org.sikuli.script.SikulixForJython.get();
  }
}
