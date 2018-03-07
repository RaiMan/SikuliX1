/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */

package org.sikuli.hotkey;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import org.sikuli.basics.Debug;
import org.sikuli.basics.HotkeyListener;
import org.sikuli.script.KeyModifier;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to bind hotkeys to hotkey listeners
 */
public class HotkeyController {

  private static HotkeyController instance = null;
  private Provider hotkeyProvider = null;
  private Map<String, HotKeyListenerWrapper> hotkeys = new HashMap<>();

  public static HotkeyController get() {
    if (Keys.isNull(instance)) {
      instance = new HotkeyController();
      instance.initProvider();
    }
    return instance;
  }

  private void initProvider() {
    if (Keys.isNull(hotkeyProvider)) {
      hotkeyProvider = Provider.getCurrentProvider(false);
    }
  }

  /**
   * remove all hotkeys
   */
  public void stop() {
    if (Keys.isNull(hotkeyProvider)) {
      return;
    }
    Debug.log(3,"stopping hotkey provider");
    hotkeyProvider.reset();
    hotkeyProvider.stop();
    hotkeyProvider = null;
    hotkeys.clear();
  }

  /**
   * install a hotkey listener for a global hotkey
   *
   * @param hotkeys  one or more strings each with a valid key name (1+ modifiers, 1 key)<br>
   *                 or one string with valid key names separated by whitespace
   * @param callback HotkeyListener
   * @return success
   */
  public String addHotkey(HotkeyCallback callback, String... hotkeys) {
    initProvider();
    String finalKey = installHotkey(callback, hotkeys);
    if (Keys.isNotSet(finalKey)) {
      String hotkey = "";
      for (String key : hotkeys) {
        hotkey += key + " ";
      }
      Debug.log(-1,"HotkeyController: addHotkey: invalid arguments: %s %s", hotkey,
              (Keys.isNull(callback) ? "(no callback)" : ""));
    }
    return finalKey;
  }

  private static Map<String, Integer> oldFashionedKeys = new HashMap<>();

  public String addHotkey(HotkeyListener callback, int key, int modifier) {
    String sKey = Keys.getKeyName(key);
    oldFashionedKeys.put(sKey, key);
    String sMod = KeyModifier.getModifierNames(modifier);
    oldFashionedKeys.put(sMod, modifier);
    Debug.log(3, "HotkeyController: addHotkey: %d mod:%d (%s %s)", key, modifier, sKey, sMod);
    return installHotkey(callback, sKey + " " + sMod );
  }

  private String installHotkey(Object listener, String... keys) {
    if (Keys.isNull(listener)) {
      return "";
    }
    if (keys.length > 0) {
      String hkey = "";
      String hmods = "";
      if (keys.length == 1) {
        keys = keys[0].split("\\s");
      }
      if (keys.length > 0 && Keys.isNotNull(listener)) {
        for (String key : keys) {
          String modifier = Keys.getModifierName(key.trim());
          if (Keys.isSet(modifier)) {
            hmods += modifier + " ";
            continue;
          }
          if (hkey.isEmpty()) {
            hkey = Keys.getKeyName(key.trim());
          }
        }
      }
      if (Keys.isSet(hkey)) {
        String finalKey = (hmods + hkey).trim();
        Debug.log(3,"installHotkey: %s", finalKey);
        HotKeyListenerWrapper hotKeyListenerWrapper = new HotKeyListenerWrapper(hkey, hmods, listener);
        hotkeyProvider.register(KeyStroke.getKeyStroke(finalKey), hotKeyListenerWrapper);
        hotkeys.put(finalKey, hotKeyListenerWrapper);
        return finalKey;
      }
    }
    return "";
  }

  public boolean removeHotkey(String givenKey) {
    if (Keys.isNotNull(hotkeyProvider) && !hotkeys.isEmpty()) {
      hotkeyProvider.reset();
      hotkeys.remove(givenKey.trim());
      Debug.log(3,"removeHotkey: %s", givenKey);
      if (!hotkeys.isEmpty()) {
        for (String keys : hotkeys.keySet()) {
          Debug.log(3,"installHotkey again: %s", keys);
          HotKeyListenerWrapper callback = hotkeys.get(keys);
          hotkeyProvider.register(KeyStroke.getKeyStroke(keys), callback);
        }
      }
    }
    return true;
  }

  public boolean removeHotkey(int key, int modifier) {
    String sKey = Keys.getKeyName(key);
    String sMod = KeyModifier.getModifierNames(modifier);
    Debug.log(3, "HotkeyController: removeHotkey: %d mod:%d (%s %s)", key, modifier, sKey, sMod);
    return removeHotkey(sKey + " " + sMod);
  }

  private class HotKeyListenerWrapper implements HotKeyListener {
    private String key = "";
    private String modifier = "";
    private Object callback = null;

    private boolean isOldFashion() {
      return callback instanceof HotkeyListener;
    }

    public HotKeyListenerWrapper(String key, String modifier, Object callback) {
      this.key = key;
      this.modifier = modifier.trim();
      this.callback = callback;
    }

    public HotKeyListenerWrapper(String keys, Object callback) {
      String[] keyParts = keys.split("\\s");
      this.key = keyParts[keyParts.length - 1];
      this.modifier = keys.substring(0, keys.length() - key.length()).trim();
      this.callback = callback;
    }

    @Override
    public void onHotKey(HotKey hotKey) {
      if (isOldFashion()) {
        int nkey = oldFashionedKeys.get(key);
        int nmod = oldFashionedKeys.get(modifier);
        org.sikuli.basics.HotkeyEvent hotkeyEvent = new org.sikuli.basics.HotkeyEvent(nkey, nmod);
        ((HotkeyListener) callback).hotkeyPressed(hotkeyEvent);
      } else {
        ((HotkeyCallback) callback).hotkeyPressed(new HotkeyEvent(key, modifier));
      }
    }
  }
}
