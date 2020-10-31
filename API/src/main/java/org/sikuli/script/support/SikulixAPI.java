/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

public class SikulixAPI {

  public static void main(String[] args) {

    RunTime.afterStart(RunTime.Type.API, args);
    System.out.println("SikuliX API: nothing to do");
    RunTime.terminate();
  }
}
