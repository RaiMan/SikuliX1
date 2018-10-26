/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;

public class SikulixForJythonDebug extends SikulixForJython {

  static {
    Debug.setWithTimeElapsed(0);
    Debug.globalTraceOn();
    Debug.on(3);
    staticInit();
  }

  private SikulixForJythonDebug() {
    new SikulixForJython();
  }
}
