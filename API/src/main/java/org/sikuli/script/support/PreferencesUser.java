/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.Options;
import org.sikuli.script.Sikulix;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

public class PreferencesUser {

  public final static int yes = 1;
  public final static int no = 0;
  public final static int AUTO_NAMING_TIMESTAMP = 0;
  public final static int AUTO_NAMING_OCR = 1;
  public final static int AUTO_NAMING_OFF = 2;
  public final static int HORIZONTAL = 0;
  public final static int VERTICAL = 1;
  public final static int UNKNOWN = -1;
  public final static int NEWBEE = 0;
  public final static int SCRIPTER = 1;
  public final static int SIKULI_USER = 2;
  public final static int THUMB_HEIGHT = 50;

  // Needed to detect if the user prefs contain the old default value
  public final static String OLD_DEFAULT_CONSOLE_CSS =
      "body   { font-family:serif; font-size: 12px; }"
              + ".normal{ color: black; }"
              + ".debug { color:#505000; }"
              + ".info  { color: blue; }"
              + ".log   { color: #09806A; }"
              + ".error { color: red; }";

  public final static String DEFAULT_CONSOLE_CSS =
          "body   { font-family:monospace; font-size: 11px; }"
                  + ".normal{ color: black; }"
                  + ".debug { color:#505000; }"
                  + ".info  { color: blue; }"
                  + ".log   { color: #09806A; }"
                  + ".error { color: red; }";
  static PreferencesUser _instance = null;

  public static PreferencesUser get() {
    if (_instance == null) {
      _instance = new PreferencesUser();
    }
    return _instance;
  }

  private final boolean isUserNode;

  private PreferencesUser() {
    boolean isUserNodeTemp  = true;
    if (Commons.isSandBox()) {
      pref = new SXPreferences();
      isUserNodeTemp = false;
    } else {
      Class<Sikulix> sikulixClass = Sikulix.class;
      pref = Preferences.userNodeForPackage(sikulixClass);
    }
    isUserNode = isUserNodeTemp;
    if (getUserType() < 0) {
      setDefaults();
    }
  }

  Preferences pref;

  public void kill() {
    if (!isUserNode) {
      return;
    }
    Debug.info("%s::Kill()", this.getClass().getSimpleName());
    try {
      pref.removeNode();
    } catch (BackingStoreException e) {
      throw new RuntimeException(e);
    }
  }

  public Preferences getStore() {
    return pref;
  }

  private class SXPreferences extends Preferences {

    final String prfx = Commons.SXPREFS_OPT;
    Options opts = Commons.getGlobalOptions();

    @Override
    public void put(String key, String value) {
      opts.set(prfx + key, value);
    }

    @Override
    public String get(String key, String def) {
      return opts.get(prfx + key, def);
    }

    @Override
    public void remove(String key) {
      Options.delOpt(opts, prfx + key);
    }

    @Override
    public void clear() {
      for (String key : keys()) {
        Options.delOpt(opts, key);
      }
    }

    @Override
    public void putInt(String key, int value) {
      opts.setInteger(prfx + key, value);
    }

    @Override
    public int getInt(String key, int def) {
      return opts.getInteger(prfx + key, def);
    }

    @Override
    public void putLong(String key, long value) {
      opts.setLong(prfx + key, value);
    }

    @Override
    public long getLong(String key, long def) {
      return opts.getLong(prfx + key, def);
    }

    @Override
    public void putBoolean(String key, boolean value) {
      opts.setBool(prfx + key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
      return opts.is(prfx + key, def);
    }

    @Override
    public void putFloat(String key, float value) {
      opts.setFloat(prfx + key, value);
    }

    @Override
    public float getFloat(String key, float def) {
      return opts.getFloat(prfx + key, def);
    }

    @Override
    public void putDouble(String key, double value) {
      opts.setDouble(prfx + key, value);
    }

    @Override
    public double getDouble(String key, double def) {
      return opts.getDouble(prfx + key, def);
    }

    @Override
    public String[] keys() {
      return opts.getAll(prfx).keySet().toArray(new String[0]);
    }

    @Override
    public String toString() {
      String optionsToString = "";
      Map<String, String> options = opts.getAll(prfx);
      if (!options.isEmpty()) {
        for (String key : options.keySet()) {
          optionsToString += key + "=" + options.get(key) +"\n";
        }
        optionsToString = optionsToString.substring(0, optionsToString.length() - 1);
      }
      return optionsToString;
    }

    //region NOT IMPLEMENTED
    @Override
    public void putByteArray(String key, byte[] value) {

    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
      return new byte[0];
    }

    @Override
    public String[] childrenNames() {
      return new String[0];
    }

    @Override
    public Preferences parent() {
      return null;
    }

    @Override
    public Preferences node(String pathName) {
      return null;
    }

    @Override
    public boolean nodeExists(String pathName) {
      return false;
    }

    @Override
    public void removeNode() {

    }

    @Override
    public String name() {
      return null;
    }

    @Override
    public String absolutePath() {
      return null;
    }

    @Override
    public boolean isUserNode() {
      return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public void sync() {

    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {

    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {

    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {

    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {

    }

    @Override
    public void exportNode(OutputStream os) {

    }

    @Override
    public void exportSubtree(OutputStream os) {

    }
    //endregion
  }

  public boolean save(String path) {
    try {
      FileOutputStream pout = new FileOutputStream(path);
      pref.exportSubtree(pout); //TODO
      pout.close();
    } catch (Exception ex) {
      Debug.error("UserPrefs: export: did not work: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  public boolean load(String path) {
    try {
      Preferences.importPreferences(new FileInputStream(path));
    } catch (Exception ex) {
      Debug.error("UserPrefs: import: did not work: ", ex.getMessage());
      return false;
    }
    return true;
  }

  public void remove(String key) {
    pref.remove(key);
  }

  public Map<String, String> getAll(String prefix) {
    Map<String, String> allPrefs = new HashMap<>();
    String[] keys = new String[0];
    try {
      keys = pref.keys();
    } catch (BackingStoreException e) {
    }
    try {
      for (String item : keys) {
        if (prefix.isEmpty() || item.startsWith(prefix)) {
          allPrefs.put(item, pref.get(item, ""));
        }
      }
    } catch (Exception ex) {
      Debug.error("Prefs.getAll: prefix (%s) did not work", prefix);
    }
    return allPrefs;
  }

  public void removeAll(String prefix) {
    try {
      for (String item : pref.keys()) {
        if (prefix.isEmpty() || item.startsWith(prefix)) {
          pref.remove(item);
        }
      }
    } catch (Exception ex) {
      Debug.error("Prefs.removeAll: prefix (%s) did not work", prefix);
    }
  }

  public void reset() {
    removeAll("");
    setDefaults();
    store();
  }

  public void store() {
    try {
      pref.flush(); //TODO
    } catch (BackingStoreException e) {
      Debug.error("UserPrefs: store: did not work: ", e.getMessage());
    }
  }

  public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
    pref.addPreferenceChangeListener(pcl); //TODO Change Listener
  }

  // ***** user type
  public void setUserType(int typ) {
    pref.putInt("USER_TYPE", typ); //TODO putInt
  }

  public int getUserType() {
    return pref.getInt("USER_TYPE", UNKNOWN); //TODO getInt
  }

  // ***** capture hot key
  private int defaultCaptureHotkey = KeyEvent.VK_2; //"2".charAt(0);

  public void setCaptureHotkey(int hkey) {
    pref.putInt("CAPTURE_HOTKEY", hkey);
  }

  public int getCaptureHotkey() {
    return pref.getInt("CAPTURE_HOTKEY", defaultCaptureHotkey);
  }

  public void setCaptureHotkeyModifiers(int mod) {
    pref.putInt("CAPTURE_HOTKEY_MODIFIERS", mod);
  }

  public int getCaptureHotkeyModifiers() {
    return pref.getInt("CAPTURE_HOTKEY_MODIFIERS", defaultCaptureHotkeyModifiers());
  }

  private int defaultCaptureHotkeyModifiers() {
    int mod = InputEvent.SHIFT_DOWN_MASK + InputEvent.META_DOWN_MASK;
    if (!Settings.isMac()) {
      mod = InputEvent.SHIFT_DOWN_MASK + InputEvent.CTRL_DOWN_MASK;
    }
    return mod;
  }

  public void setCaptureDelay(double v) {
    pref.putDouble("CAPTURE_DELAY", v); //TODO putDouble
  }

  public double getCaptureDelay() {
    return pref.getDouble("CAPTURE_DELAY", 1.0); //TODO getDouble
  }

  // ***** abort key
  private int defaultStopHotkey = KeyEvent.VK_C; //"c".charAt(0);

  public void setStopHotkey(int hkey) {
    pref.putInt("STOP_HOTKEY", hkey);
  }

  public int getStopHotkey() {
    return pref.getInt("STOP_HOTKEY",defaultStopHotkey);
  }

  public void setStopHotkeyModifiers(int mod) {
    pref.putInt("STOP_HOTKEY_MODIFIERS", mod);
  }

  public int getStopHotkeyModifiers() {
    return pref.getInt("GET_HOTKEY_MODIFIERS", defaultStopHotkeyModifiers());
  }

  private int defaultStopHotkeyModifiers() {
    int mod = InputEvent.SHIFT_DOWN_MASK + InputEvent.META_DOWN_MASK;
    if (!Settings.isMac()) {
      mod = InputEvent.SHIFT_DOWN_MASK + InputEvent.ALT_DOWN_MASK;
    }
    return mod;
  }

  // ***** indentation support
  public void setExpandTab(boolean flag) {
    pref.putBoolean("EXPAND_TAB", flag); //TODO putBoolean
  }

  public boolean getExpandTab() {
    return pref.getBoolean("EXPAND_TAB", true); //TODO getBoolean
  }

  public void setTabWidth(int width) {
    pref.putInt("TAB_WIDTH", width);
  }

  public int getTabWidth() {
    return pref.getInt("TAB_WIDTH", 4);
  }

  public String getTabWhitespace() {
    if (getExpandTab()) {
      char[] blanks = new char[getTabWidth()];
      Arrays.fill(blanks, ' ');
      return new String(blanks);
    } else {
      return "\t";
    }
  }

  // ***** font settings
  public void setFontSize(int size) {
    pref.putInt("FONT_SIZE", size);
  }

  public int getFontSize() {
    return pref.getInt("FONT_SIZE", 18);
  }

  public void setFontName(String font) {
    pref.put("FONT_NAME", font); //TODO put
  }

  public String getFontName() {
    return pref.get("FONT_NAME", "Monospaced"); //TODO get
  }

  // ***** locale support
  public void setLocale(Locale l) {
    pref.put("LOCALE", l.toString());
  }

  public Locale getLocale() {
    String locale = pref.get("LOCALE", Locale.getDefault().toString());
    String[] code = locale.split("_");
    if (code.length == 1) {
      return new Locale.Builder().setLanguage(code[0]).build();
    } else if (code.length == 2) {
      return new Locale.Builder().setLanguage(code[0]).setRegion(code[1]).build();
    } else {
      return new Locale.Builder().setLanguage(code[0]).setRegion(code[1]).setVariant(code[2]).build();
    }
  }

  // ***** image capture and naming
  public void setAutoNamingMethod(int m) {
    pref.putInt("AUTO_NAMING", m);
  }

  public int getAutoNamingMethod() {
    return pref.getInt("AUTO_NAMING", AUTO_NAMING_OCR);
  }

  public void setDefaultThumbHeight(int h) {
    pref.putInt("DEFAULT_THUMB_HEIGHT", h);
  }

  public void resetDefaultThumbHeight() {
    pref.putInt("DEFAULT_THUMB_HEIGHT", THUMB_HEIGHT);
  }

  public int getDefaultThumbHeight() {
    return pref.getInt("DEFAULT_THUMB_HEIGHT", THUMB_HEIGHT);
  }

  public void setAutoCaptureForCmdButtons(boolean flag) {
    pref.putInt("AUTO_CAPTURE_FOR_CMD_BUTTONS", flag ? 1 : 0);
  }

  public boolean getAutoCaptureForCmdButtons() {
    return pref.getInt("AUTO_CAPTURE_FOR_CMD_BUTTONS", 1) != 0;
  }

  // ***** save options
  public void setAtSaveMakeHTML(boolean flag) {
    pref.putBoolean("AT_SAVE_MAKE_HTML", flag);
  }

  public boolean getAtSaveMakeHTML() {
    return pref.getBoolean("AT_SAVE_MAKE_HTML", false);
  }

  public void setAtSaveCleanBundle(boolean flag) {
    pref.putBoolean("AT_SAVE_CLEAN_BUNDLE", flag);
  }

  public boolean getAtSaveCleanBundle() {
    return pref.getBoolean("AT_SAVE_CLEAN_BUNDLE", true);
  }

  // ***** script run options
  public void setPrefMoreRunSave(boolean flag) {
    pref.putBoolean("PREF_MORE_RUN_SAVE", flag);
  }

  public boolean getPrefMoreRunSave() {
    return pref.getBoolean("PREF_MORE_RUN_SAVE", false);
  }

  public void setPrefMoreHighlight(boolean flag) {
    pref.putBoolean("PREF_MORE_HIGHLIGHT", flag);
  }

  //TODO: implement prefMoreHighlight
  public boolean getPrefMoreHighlight() {
    return pref.getBoolean("PREF_MORE_HIGHLIGHT", false);
  }

  // ***** auto update support
  //TODO implement update
  public void setCheckUpdate(boolean flag) {
    pref.putBoolean("CHECK_UPDATE", false);
  }

  public boolean getCheckUpdate() {
    return pref.getBoolean("CHECK_UPDATE", false);
  }

  public void setLastSeenUpdate(String ver) {
    pref.put("LAST_SEEN_UPDATE", ver);
  }

  public String getLastSeenUpdate() {
    return pref.get("LAST_SEEN_UPDATE", "0.0");
  }

  public void setCheckUpdateTime() {
    pref.putLong("LAST_CHECK_UPDATE", (new Date()).getTime()); //TODO putLong
  }

  public long getCheckUpdateTime() {
    return pref.getLong("LAST_CHECK_UPDATE", (new Date()).getTime()); //TODO getLong
  }

  // ***** IDE general support
  public void setIdeSize(Dimension size) {
    String str = (int) size.getWidth() + "x" + (int) size.getHeight();
    pref.put("IDE_SIZE", str);
  }

  public Dimension getIdeSize() {
    String str = pref.get("IDE_SIZE", "1024x700");
    String[] w_h = str.split("x");
    return new Dimension(Integer.parseInt(w_h[0]), Integer.parseInt(w_h[1]));
  }

  public void setIdeLocation(Point p) {
    String str = p.x + "," + p.y;
    pref.put("IDE_LOCATION", str);
  }

  public Point getIdeLocation() {
    String str = pref.get("IDE_LOCATION", "0,0");
    String[] x_y = str.split(",");
    return new Point(Integer.parseInt(x_y[0]), Integer.parseInt(x_y[1]));
  }

  // ***** IDE Editor options
  public void setPrefMoreImageThumbs(boolean flag) {
    pref.putBoolean("PREF_MORE_IMAGE_THUMBS", flag);
  }

  public boolean getPrefMoreImageThumbs() {
    return pref.getBoolean("PREF_MORE_IMAGE_THUMBS", true);
  }

  public void setPrefMorePlainText(boolean flag) {
    pref.putBoolean("PREF_MORE_PLAIN_TEXT", flag);
  }

  public boolean getPrefMorePlainText() {
    return pref.getBoolean("PREF_MORE_PLAIN_TEXT", false);
  }

  // currently: last open filenames
  public void setIdeSession(String session_str) {
    pref.put("IDE_SESSION", session_str);
  }

  public String getIdeSession() {
    return pref.get("IDE_SESSION", "");
  }

  // support for IDE image path
  public void setPrefMoreImages(boolean flag) {
    pref.putBoolean("PREF_MORE_IMAGES", flag);
  }

  public boolean getPrefMoreImages() {
    return pref.getBoolean("PREF_MORE_IMAGES", false);
  }

  public void setPrefMoreImagesPath(String path) {
    pref.put("PREF_MORE_IMAGES_PATH", path);
  }

  public String getPrefMoreImagesPath() {
    return pref.get("PREF_MORE_IMAGES_PATH", "");
  }

  // ***** message area settings
  public void setPrefMoreMessage(int typ) {
    pref.putInt("PREF_MORE_MESSAGE", typ);
  }

  public int getPrefMoreMessage() {
    return pref.getInt("PREF_MORE_MESSAGE", HORIZONTAL);
  }

  public void setPrefMoreLogActions(boolean flag) {
    pref.putBoolean("PREF_MORE_LOG_ACTIONS", flag);
  }

  public boolean getPrefMoreLogActions() {
    return pref.getBoolean("PREF_MORE_LOG_ACTIONS", true);
  }

  public void setPrefMoreLogInfo(boolean flag) {
    pref.putBoolean("PREF_MORE_LOG_INFO", flag);
  }

  public boolean getPrefMoreLogInfo() {
    return pref.getBoolean("PREF_MORE_LOG_INFO", true);
  }

  public void setPrefMoreLogDebug(boolean flag) {
    pref.putBoolean("PREF_MORE_LOG_DEBUG", flag);
  }

  public boolean getPrefMoreLogDebug() {
    return pref.getBoolean("PREF_MORE_LOG_DEBUG", true);
  }

  public void setConsoleCSS(String css) {
    pref.put("CONSOLE_CSS", css);
  }

  public String getConsoleCSS() {
    String css = pref.get("CONSOLE_CSS", DEFAULT_CONSOLE_CSS);

    /*
     *  Hack to detect if the user prefs contain the old default
     *  value.
     *  In such a case, the new style is forced.
     *
     *  TODO: Ensure that the user prefs do not contain default
     *  values. Having them in prefs makes it really cumbersome
     *  to change them afterwards.
     */
    if (OLD_DEFAULT_CONSOLE_CSS.equals(css)) {
      css = DEFAULT_CONSOLE_CSS;
    }

    return css;
  }

  // ***** general setter getter
  public void put(String key, String val) {
    pref.put(key, val);
  }

  public String get(String key, String default_) {
    return pref.get(key, default_);
  }

  public void setDefaults() {
    setUserType(SIKULI_USER);
    setIdeSession("");

// ***** capture hot key
    setCaptureHotkey(defaultCaptureHotkey);
    setCaptureHotkeyModifiers(defaultCaptureHotkeyModifiers());
    setCaptureDelay(1.0);

// ***** abort key
    setStopHotkey(defaultStopHotkey);
    setStopHotkeyModifiers(defaultStopHotkeyModifiers());

// ***** indentation support
    setExpandTab(true);
    setTabWidth(4);

// ***** font settings
    setFontSize(14);
    setFontName("Monospaced");

// ***** locale support
    setLocale(Locale.getDefault());

// ***** image capture and naming
    setAutoNamingMethod(AUTO_NAMING_TIMESTAMP);
    setPrefMoreImageThumbs(true);
    setDefaultThumbHeight(THUMB_HEIGHT);

// ***** save options
    setAtSaveMakeHTML(false);
    setAtSaveCleanBundle(true);

// ***** script run options
    setPrefMoreRunSave(true);
    setPrefMoreHighlight(false);

// ***** auto update support
    setCheckUpdate(false);
    setLastSeenUpdate("0.0.0");
    setCheckUpdateTime();

// ***** IDE general support
    setIdeSize(new Dimension(1024, 700));
    setIdeLocation(new Point(0, 0));

    setPrefMoreImages(false);
    setPrefMoreImagesPath("");

    setPrefMoreMessage(PreferencesUser.VERTICAL);

// ***** message area settings
    setPrefMoreLogActions(true);
    setPrefMoreLogInfo(true);
    setPrefMoreLogDebug(true);

    setConsoleCSS(DEFAULT_CONSOLE_CSS);
  }
}
