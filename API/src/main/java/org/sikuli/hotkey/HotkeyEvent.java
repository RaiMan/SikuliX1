/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */

package org.sikuli.hotkey;

public class HotkeyEvent {
  private int keyCode = 0;
  private int modifiers = 0;
  private String key = "";
  private String modifier = "";

  public HotkeyEvent(String key, String modifier) {
    this.key = key;
    this.modifier = modifier;
  }

  public HotkeyEvent(int keyCode, int modifiers) {
    this.keyCode = keyCode;
    this.modifiers = modifiers;
  }

  public String get() {
    return toString();
  }

  public String toString() {
    if (key.isEmpty()) {
      return String.format("%d(%d)", keyCode, modifiers);
    } else {
      return modifier + key;
    }
  }
}

