package org.sikuli.basics;

import org.sikuli.hotkey.HotkeyController;

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
  public void cleanUp() {
    if (controller != null) {
      controller.stop();
    }
  }
}
