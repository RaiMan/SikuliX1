/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.event.InputEvent;

/**
 * complementing class Key with the constants for the modifier keys<br>
 * only still there for backward compatibility (is already duplicated in Key)
 */
public class KeyModifier {
  public static final int CTRL = InputEvent.CTRL_MASK; //ctrl
  public static final int SHIFT = InputEvent.SHIFT_MASK; //shift
  public static final int ALT = InputEvent.ALT_MASK; //alt
  public static final int ALTGR = InputEvent.ALT_GRAPH_MASK; //altGraph
  public static final int META = InputEvent.META_MASK; //meta
  public static final int CMD = InputEvent.META_MASK;
  public static final int WIN = InputEvent.META_MASK;

  @Deprecated
  public static final int KEY_CTRL = InputEvent.CTRL_MASK;
  @Deprecated
  public static final int KEY_SHIFT = InputEvent.SHIFT_MASK;
  @Deprecated
  public static final int KEY_ALT = InputEvent.ALT_MASK;
  @Deprecated
  public static final int KEY_META = InputEvent.META_MASK;
  @Deprecated
  public static final int KEY_CMD = InputEvent.META_MASK;
  @Deprecated
  public static final int KEY_WIN = InputEvent.META_MASK;

  public static String getModifierNames(int modifier) {
    String names = "";
    if (0 != (modifier & CTRL)) {
      names += "ctrl ";
    }
    if (0 != (modifier & SHIFT)) {
      names += "shift ";
    }
    if (0 != (modifier & ALT)) {
      names += "alt ";
    }
    if (0 != (modifier & ALTGR)) {
      names += "altGraph ";
    }
    if (0 != (modifier & META)) {
      names += "meta ";
    }
    return names.trim();
  }
}
