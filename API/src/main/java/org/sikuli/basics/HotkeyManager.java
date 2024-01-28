/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.SikuliXception;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.KeyboardLayout;
import org.sikuli.script.support.PreferencesUser;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to bind hotkeys to hotkey listeners
 */
public abstract class HotkeyManager {

  private static HotkeyManager _instance = null;
  private static Map<String, Integer[]> hotkeys;
  private static Map<String, Integer[]> hotkeysGlobal = new HashMap<String, Integer[]>();
  private static final String HotkeyTypeCapture = "Capture";
  private static int HotkeyTypeCaptureKey;
  private static int HotkeyTypeCaptureMod;
  private static final String HotkeyTypeAbort = "Abort";
  private static int HotkeyTypeAbortKey;
  private static int HotkeyTypeAbortMod;


  public static HotkeyManager getInstance() {
    if (_instance == null) {
      if (Commons.runningWindows() || Commons.runningMac()) {
        _instance = new GenericHotkeyManager();
      } else {
        String cls = getOSHotkeyManagerClass();
        if (cls != null) {
          try {
            Class<?> c = Class.forName(cls);
            Constructor<?> constr = c.getConstructor();
            _instance = (HotkeyManager) constr.newInstance();
          } catch (Exception e) {
            Debug.error("HotkeyManager: Can't create " + cls + ": " + e.getMessage());
          }
        }
      }
      hotkeys = new HashMap<String, Integer[]>();
    }
    return _instance;
  }

  public static void reset(boolean isTerminating) {
    if (_instance == null) {
      return;
    }
    int result = reset(hotkeys, isTerminating);
    if (result > 0) {
      Debug.log(4, "HotkeyManager: reset: removed all user hotkeys.");
    }
    if (result < 0 && !isTerminating) {
      //RunTime.get().terminate(999, "HotkeyManager: reset: did not work");
      throw new SikuliXception(String.format("fatal: " + "HotkeyManager: reset: did not work"));
    }
    if (!isTerminating) {
      return;
    }
    result = reset(hotkeysGlobal, isTerminating);
    if (result > 0) {
      Debug.log(4, "HotkeyManager: reset: removed all SikuliX hotkeys.");
    }
    if (isTerminating) {
      _instance.cleanUp();
    }
  }

  private static int reset(Map<String, Integer[]> hotkeys, boolean isTerminating) {
    int removed = _instance._removeAll(hotkeys, isTerminating);
    hotkeys.clear();
    return removed;
  }

  abstract public void cleanUp();

  private static String getOSHotkeyManagerClass() {
    String pkg = "org.sikuli.basics.";
    if (Commons.runningMac()) {
      return pkg + "MacHotkeyManager";
    } else if (Commons.runningWindows()) {
      return pkg + "WindowsHotkeyManager";
    } else if (Commons.runningLinux()) {
      return pkg + "LinuxHotkeyManager";
    } else {
      Debug.error("HotkeyManager: not supported on your OS.");
      return null;
    }
}

  private static String getKeyCodeText(int key) {
    return KeyEvent.getKeyText(key).toUpperCase();
  }

  private static String getKeyModifierText(int modifiers) {
    String txtMod = InputEvent.getModifiersExText(modifiers).toUpperCase();
    if (Settings.isMac()) {
      txtMod = txtMod.replace("META", "CMD");
      txtMod = txtMod.replace("WINDOWS", "CMD");
    } else {
      txtMod = txtMod.replace("META", "WIN");
      txtMod = txtMod.replace("WINDOWS", "WIN");
    }
    return txtMod;
  }

  /**
   * install a hotkey listener for a global hotkey (capture, abort, ...)
   *
   * @param hotkeyType a type string
   * @param callback   HotkeyListener
   * @return success
   */
  public boolean addHotkey(String hotkeyType, HotkeyListener callback) {
    PreferencesUser pref = PreferencesUser.get();
    if (hotkeyType == HotkeyTypeCapture) {
      HotkeyTypeCaptureKey = pref.getCaptureHotkey();
      HotkeyTypeCaptureMod = pref.getCaptureHotkeyModifiers();
      return installHotkey(HotkeyTypeCaptureKey, HotkeyTypeCaptureMod, callback, hotkeyType);
    } else if (hotkeyType == HotkeyTypeAbort) {
      HotkeyTypeAbortKey = pref.getStopHotkey();
      HotkeyTypeAbortMod = pref.getStopHotkeyModifiers();
      return installHotkey(HotkeyTypeAbortKey, HotkeyTypeAbortMod, callback, hotkeyType);
    } else {
      Debug.error("HotkeyManager: addHotkey: HotkeyType %s not supported", hotkeyType);
      return false;
    }
  }

  public String getHotKeyText(String hotkeyType) {
    PreferencesUser pref = PreferencesUser.get();
    String key = "";
    String mod = "";
    if (hotkeyType == HotkeyTypeCapture) {
      key = getKeyCodeText(pref.getCaptureHotkey());
      mod = getKeyModifierText(pref.getCaptureHotkeyModifiers());
    } else if (hotkeyType == HotkeyTypeAbort) {
      key = getKeyCodeText(pref.getStopHotkey());
      mod = getKeyModifierText(pref.getStopHotkeyModifiers());
    } else {
      Debug.error("HotkeyManager: getHotKeyText: HotkeyType %s not supported", hotkeyType);
    }
    return mod + " " + key;
  }

  /**
   * install a hotkey listener.
   *
   * @param key       key character (class Key)
   * @param modifiers modifiers flag
   * @param callback  HotkeyListener
   * @return true if success. false otherwise.
   */
  public boolean addHotkey(char key, int modifiers, HotkeyListener callback) {
    return addHotkey("" + key, modifiers, callback);
  }

  /**
   * install a hotkey listener.
   *
   * @param key       key character (class Key)
   * @param modifiers modifiers flag
   * @param callback  HotkeyListener
   * @return true if success. false otherwise.
   */
  public boolean addHotkey(String key, int modifiers, HotkeyListener callback) {
    int[] keyCodes = KeyboardLayout.toJavaKeyCode(key.toLowerCase().charAt(0));
    int keyCode = keyCodes[0];
    return installHotkey(keyCode, modifiers, callback, "");
  }

  private boolean installHotkey(int key, int mod, HotkeyListener callback, String hotkeyType) {
    boolean res;
    String txtMod = getKeyModifierText(mod);
    String txtCode = getKeyCodeText(key);
    String token = "" + key + mod;
    Debug.log(3, "HotkeyManager: add %s Hotkey: %s %s (%d, %d)", hotkeyType, txtMod, txtCode, key, mod);
    boolean checkGlobal = true;
    if (hotkeys.containsKey(token)) {
      res = _instance._removeHotkey(hotkeys.get(token)[0], hotkeys.get(token)[1]);
      if (!res) {
        Debug.error("HotkeyManager: addHotkey: failed to remove already defined hotkey");
        return false;
      } else {
        checkGlobal = false;
      }
    }
    if (checkGlobal) {
      for (String kg : hotkeysGlobal.keySet()) {
        int gkey = hotkeysGlobal.get(kg)[0];
        int gmod = hotkeysGlobal.get(kg)[1];
        if (gkey == key && gmod == mod) {
          Debug.error("HotkeyManager: addHotkey: ignored: trying to redefine global hotkey %s", kg);
          return false;
        }
      }
    }
    res = _instance._addHotkey(key, mod, callback);
    if (res) {
      if (hotkeyType.isEmpty()) {
        hotkeys.put(token, new Integer[]{key, mod});
      } else {
        hotkeysGlobal.put(hotkeyType, new Integer[]{key, mod});
      }
    } else {
      Debug.error("HotkeyManager: addHotkey: failed");
    }
    return res;
  }

  /**
   * remove a hotkey by type (not supported yet)
   *
   * @param hotkeyType capture, abort, ...
   * @return success
   */
  public boolean removeHotkey(String hotkeyType) {
    if (hotkeysGlobal.containsKey(hotkeyType)) {
      return uninstallHotkey(hotkeyType);
    } else {
      Debug.error("HotkeyManager: removeHotkey: HotkeyType %s not defined", hotkeyType);
      return false;
    }
  }

  /**
   * remove a hotkey and uninstall a hotkey listener.
   *
   * @param key       key character (class Key)
   * @param modifiers modifiers flag
   * @return true if success. false otherwise.
   */
  public boolean removeHotkey(char key, int modifiers) {
    return removeHotkey("" + key, modifiers);
  }

  /**
   * uninstall a hotkey listener.
   *
   * @param key       key string (class Key)
   * @param modifiers modifiers flag
   * @return true if success. false otherwise.
   */
  public boolean removeHotkey(String key, int modifiers) {
    int[] keyCodes = KeyboardLayout.toJavaKeyCode(key.toLowerCase().charAt(0));
    int keyCode = keyCodes[0];
    return uninstallHotkey(keyCode, modifiers);
  }

  private boolean uninstallHotkey(String type) {
    int gkey = hotkeysGlobal.get(type)[0];
    int gmod = hotkeysGlobal.get(type)[1];
    boolean success = uninstallHotkey(gkey, gmod);
    if (success) {
      hotkeysGlobal.remove(type);
    }
    return success;
  }

  private boolean uninstallHotkey(int key, int mod) {
    String txtMod = getKeyModifierText(mod);
    String txtCode = getKeyCodeText(key);
    Debug.log(4,"HotkeyManager: remove Hotkey: %s %s (%d, %d)", txtMod, txtCode, key, mod);
    boolean res = _instance._removeHotkey(key, mod);
    if (res) {
      hotkeys.remove(key);
    } else {
      Debug.error("HotkeyManager: removeHotkey: failed");
    }
    return res;
  }

  abstract public boolean _addHotkey(int keyCode, int modifiers, HotkeyListener callback);

  abstract public boolean _removeHotkey(int keyCode, int modifiers);

  abstract public int _removeAll(Map<String, Integer[]> hotkeys, boolean isTerminating);
}
