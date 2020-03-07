/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.support.RunTime;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

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
  Preferences pref = null;

  public static PreferencesUser get() {
    if (_instance == null) {
      _instance = new PreferencesUser();
    }
    return _instance;
  }

  private PreferencesUser() {
    Debug.log(3, "init user preferences");
    if (RunTime.isSandbox()) {
      pref = Preferences.userNodeForPackage(org.sikuli.script.Sikulix.class);
    } else {
      pref = Preferences.userNodeForPackage(org.sikuli.script.Sikulix.class);
    }
  }

  public boolean save(String path) {
    try {
      FileOutputStream pout = new FileOutputStream(new File(path));
      pref.exportSubtree(pout);
      pout.close();
    } catch (Exception ex) {
      Debug.error("UserPrefs: export: did not work: %s", ex.getMessage());
      return false;
    }
    return true;
  }

  public boolean load(String path) {
    try {
      Preferences.importPreferences(new FileInputStream(new File(path)));
    } catch (Exception ex) {
      Debug.error("UserPrefs: import: did not work: ", ex.getMessage());
      return false;
    }
    return true;
  }

  public void remove(String key) {
    pref.remove(key);
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
      pref.flush();
    } catch (BackingStoreException e) {
      Debug.error("UserPrefs: reset: did not work: ", e.getMessage());
    }
  }

  public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
    pref.addPreferenceChangeListener(pcl);
  }

  // ***** user type
  public void setUserType(int typ) {
    pref.putInt("USER_TYPE", typ);
  }

  public int getUserType() {
    return pref.getInt("USER_TYPE", UNKNOWN);
  }

  // ***** capture hot key
  public void setCaptureHotkey(int hkey) {
    pref.putInt("CAPTURE_HOTKEY", hkey);
  }

  public int getCaptureHotkey() {
    return pref.getInt("CAPTURE_HOTKEY", 50); // default: '2'
  }

  public void setCaptureHotkeyModifiers(int mod) {
    if (mod < 0) {
    }
    pref.putInt("CAPTURE_HOTKEY_MODIFIERS", mod);
  }

  public int getCaptureHotkeyModifiers() {
    return pref.getInt("CAPTURE_HOTKEY_MODIFIERS", defaultCaptureHotkeyModifiers());
  }

  private int defaultCaptureHotkeyModifiers() {
    int mod = Event.SHIFT_MASK + Event.META_MASK;
    if (!Settings.isMac()) {
      mod = Event.SHIFT_MASK + Event.CTRL_MASK;
    }
    return mod;
  }

  public void setCaptureDelay(double v) {
    pref.putDouble("CAPTURE_DELAY", v);
  }

  public double getCaptureDelay() {
    return pref.getDouble("CAPTURE_DELAY", 1.0);
  }

  // ***** abort key
  public void setStopHotkey(int hkey) {
    pref.putInt("STOP_HOTKEY", hkey);
  }

  public int getStopHotkey() {
    return pref.getInt("STOP_HOTKEY", 67); // default: 'c'
  }

  public void setStopHotkeyModifiers(int mod) {
    pref.putInt("STOP_HOTKEY_MODIFIERS", mod);
  }

  public int getStopHotkeyModifiers() {
    return pref.getInt("GET_HOTKEY_MODIFIERS", defaultStopHotkeyModifiers());
  }

  private int defaultStopHotkeyModifiers() {
    int mod = Event.SHIFT_MASK + Event.META_MASK;
    if (!Settings.isMac()) {
      mod = Event.SHIFT_MASK + Event.ALT_MASK;
    }
    return mod;
  }

  // ***** indentation support
  public void setExpandTab(boolean flag) {
    pref.putBoolean("EXPAND_TAB", flag);
  }

  public boolean getExpandTab() {
    return pref.getBoolean("EXPAND_TAB", true);
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
    pref.put("FONT_NAME", font);
  }

  public String getFontName() {
    return pref.get("FONT_NAME", "Monospaced");
  }

  // ***** locale support
  public void setLocale(Locale l) {
    pref.put("LOCALE", l.toString());
  }

  public Locale getLocale() {
    String locale = pref.get("LOCALE", Locale.getDefault().toString());
    String[] code = locale.split("_");
    if (code.length == 1) {
      return new Locale(code[0]);
    } else if (code.length == 2) {
      return new Locale(code[0], code[1]);
    } else {
      return new Locale(code[0], code[1], code[2]);
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
    pref.putLong("LAST_CHECK_UPDATE", (new Date()).getTime());
  }

  public long getCheckUpdateTime() {
    return pref.getLong("LAST_CHECK_UPDATE", (new Date()).getTime());
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
    return pref.get("IDE_SESSION", null);
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
    return pref.get("PREF_MORE_IMAGES_PATH", null);
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
    pref.putBoolean("PREF_MORE_LOG_INFO", flag);
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

// ***** capture hot key
    setCaptureHotkey(50);
    setCaptureHotkeyModifiers(defaultCaptureHotkeyModifiers());
    setCaptureDelay(1.0);

// ***** abort key
    setStopHotkey(67);
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
    setIdeSize(new Dimension(0, 0));
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
