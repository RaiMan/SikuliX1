/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.script.support.RunTime;

public class Sikulix {

  public static void main(String[] args) {
    System.setProperty("sikuli.IDE_should_run", "develop");
    RunTime.start(RunTime.Type.IDE, args);
    SikulixIDE.main(args);
  }
}
