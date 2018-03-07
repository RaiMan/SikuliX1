/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.Key;

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to bind hotkeys to hotkey listeners
 */
public abstract class HotkeyManager {

  private static HotkeyManager _instance = null;
  private static Map<Integer, Integer> hotkeys;
  private static Map<Integer, Integer> hotkeysGlobal = new HashMap<Integer, Integer>();
  private static final String HotkeyTypeCapture = "Capture";
  private static int HotkeyTypeCaptureKey;
  private static int HotkeyTypeCaptureMod;
  private static final String HotkeyTypeAbort = "Abort";
  private static int HotkeyTypeAbortKey;
  private static int HotkeyTypeAbortMod;

  public static HotkeyManager getInstance() {
    if (_instance == null) {
      if (Settings.isWindows() || Settings.isMac()) {
//      if (Settings.isMac()) {
        _instance = new GenericHotkeyManager();
      } else {
        String cls = getOSHotkeyManagerClass();
        if (cls != null) {
          try {
            Class c = Class.forName(cls);
            Constructor constr = c.getConstructor();
            _instance = (HotkeyManager) constr.newInstance();
          } catch (Exception e) {
            Debug.error("HotkeyManager: Can't create " + cls + ": " + e.getMessage());
          }
        }
      }
      hotkeys = new HashMap<Integer, Integer>();
    }
    return _instance;
  }

  /**
   * remove all hotkeys
   */
  public static void reset() {
    if (_instance == null || hotkeys.isEmpty()) {
      return;
    }
    Debug.log(3, "HotkeyManager: reset - removing all defined hotkeys.");
    boolean res;
    int[] hk = new int[hotkeys.size()];
    int i = 0;
    for (Integer k : hotkeys.keySet()) {
      res = _instance._removeHotkey(k, hotkeys.get(k));
      if (!res) {
        Debug.error("HotkeyManager: reset: failed to remove hotkey: %s %s",
                getKeyModifierText(hotkeys.get(k)), getKeyCodeText(k));
        hk[i++] = -1;
      } else {
        hk[i++] = k;
        Debug.log(3, "removed (%d, %d)", k, hotkeys.get(k));
      }
    }
    for (int k : hk) {
      if (k == -1) {
        continue;
      }
      hotkeys.remove(k);
    }
  }

  private static String getOSHotkeyManagerClass() {
    String pkg = "org.sikuli.basics.";
    int theOS = Settings.getOS();
    switch (theOS) {
      case Settings.ISMAC:
        return pkg + "MacHotkeyManager";
      case Settings.ISWINDOWS:
        return pkg + "WindowsHotkeyManager";
      case Settings.ISLINUX:
        return pkg + "LinuxHotkeyManager";
      default:
        Debug.error("HotkeyManager: Hotkey registration is not supported on your OS.");
    }
    return null;
  }

  private static String getKeyCodeText(int key) {
    return KeyEvent.getKeyText(key).toUpperCase();
  }

  private static String getKeyModifierText(int modifiers) {
    String txtMod = KeyEvent.getKeyModifiersText(modifiers).toUpperCase();
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
    PreferencesUser pref = PreferencesUser.getInstance();
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
    PreferencesUser pref = PreferencesUser.getInstance();
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
    int[] keyCodes = Key.toJavaKeyCode(key.toLowerCase());
    int keyCode = keyCodes[0];
    return installHotkey(keyCode, modifiers, callback, "");
  }

  private boolean installHotkey(int key, int mod, HotkeyListener callback, String hotkeyType) {
    boolean res;
    String txtMod = getKeyModifierText(mod);
    String txtCode = getKeyCodeText(key);
    Debug.info("HotkeyManager: add %s Hotkey: %s %s (%d, %d)", hotkeyType, txtMod, txtCode, key, mod);
    boolean checkGlobal = true;
    for (Integer k : hotkeys.keySet()) {
      if (k == key && mod == hotkeys.get(key)) {
        res = _instance._removeHotkey(key, hotkeys.get(key));
        if (!res) {
          Debug.error("HotkeyManager: addHotkey: failed to remove already defined hotkey");
          return false;
        } else {
          checkGlobal = false;
        }
      }
    }
    if (checkGlobal) {
      for (Integer kg : hotkeysGlobal.keySet()) {
        if (kg == key && mod == hotkeysGlobal.get(key)) {
          Debug.error("HotkeyManager: addHotkey: ignored: trying to redefine a global hotkey");
          return false;
        }
      }
    }
    res = _instance._addHotkey(key, mod, callback);
    if (res) {
      if (hotkeyType.isEmpty()) {
        hotkeys.put(key, mod);
      } else {
        hotkeysGlobal.put(key, mod);
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
    if (hotkeyType == HotkeyTypeCapture) {
      return uninstallHotkey(HotkeyTypeCaptureKey, HotkeyTypeCaptureMod, hotkeyType);
    } else if (hotkeyType == HotkeyTypeAbort) {
      return uninstallHotkey(HotkeyTypeAbortKey, HotkeyTypeAbortMod, hotkeyType);
    } else {
      Debug.error("HotkeyManager: removeHotkey: using HotkeyType as %s not supported yet", hotkeyType);
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
    int[] keyCodes = Key.toJavaKeyCode(key.toLowerCase());
    int keyCode = keyCodes[0];
    return uninstallHotkey(keyCode, modifiers, "");
  }

  private boolean uninstallHotkey(int key, int mod, String hotkeyType) {
    boolean res;
    String txtMod = getKeyModifierText(mod);
    String txtCode = getKeyCodeText(key);
    Debug.info("HotkeyManager: remove %s Hotkey: %s %s (%d, %d)", hotkeyType, txtMod, txtCode, key, mod);
    res = _instance._removeHotkey(key, mod);
    if (res) {
      hotkeys.remove(key);
    } else {
      Debug.error("HotkeyManager: removeHotkey: failed");
    }
    return res;
  }

  abstract public boolean _addHotkey(int keyCode, int modifiers, HotkeyListener callback);

  abstract public boolean _removeHotkey(int keyCode, int modifiers);

  abstract public void cleanUp();
}
