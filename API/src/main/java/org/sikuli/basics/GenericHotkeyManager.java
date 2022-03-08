/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

import org.sikuli.hotkey.HotkeyController;

import java.util.Map;

public class GenericHotkeyManager extends HotkeyManager {

  HotkeyController controller = null;

  @Override
  public boolean _addHotkey(int keyCode, int modifiers, HotkeyListener callback) {
    if (controller == null) {
      controller = HotkeyController.get();
    }
    return !controller.addHotkey(callback, keyCode, modifiers).isEmpty();
  }

  @Override
  public boolean _removeHotkey(int keyCode, int modifiers) {
    if (controller == null) {
      return false;
    }
    return controller.removeHotkey(keyCode, modifiers);
  }

  @Override
  public int _removeAll(Map<String, Integer[]> hotkeys, boolean isTerminating) {
    if (controller == null) {
      return 0;
    }
    controller.setTerminating(isTerminating);
    for (Integer[] keyMods : hotkeys.values()) {
      if (!controller.removeHotkey(keyMods[0], keyMods[1])) {
        return -1;
      }
    }
    return hotkeys.size();
  }

  @Override
  public void cleanUp() {
    if (controller != null) {
      controller.stop();
    }
  }
}
