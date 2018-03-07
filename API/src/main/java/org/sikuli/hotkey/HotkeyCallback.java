/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */

package org.sikuli.hotkey;

public abstract class HotkeyCallback {

  /**
   * Override this to implement your own hotkey handler.
   *
   * @param e HotkeyEvent
   */
  abstract public void hotkeyPressed(HotkeyEvent e);

  /**
   * INTERNAL USE: system specific handler implementation
   *
   * @param e HotkeyEvent
   */
  public void invokeHotkeyPressed(final HotkeyEvent e) {
    Thread hotkeyThread = new Thread() {
      @Override
      public void run() {
        hotkeyPressed(e);
      }
    };
    hotkeyThread.start();
  }
}
