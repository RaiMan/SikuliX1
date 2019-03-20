package org.sikuli.script;

import org.sikuli.basics.Debug;

public class SX {

  public void reset() {
    Debug.log(3, "SX.reset()");
    Screen.resetMonitorsQuiet();
    Mouse.reset();
  }
}
