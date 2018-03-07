/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.HotkeyManager;
import org.sikuli.basics.HotkeyListener;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.util.SysJNA;

/**
 * this class implements an interface to the Java key system
 * as represented by java.awt.event.KeyEvent.
 * for the functions Region.type() and Region.write()
 * by translating key constants for special keys and plain text per character.<br>
 * for details consult the docs
 */
public class Key {

  /**
   * add a hotkey and listener
   * @param key respective key specifier according class Key
   * @param modifiers respective key specifier according class KeyModifiers
   * @param listener a HotKeyListener instance
   * @return true if ok, false otherwise
   */
  public static boolean addHotkey(String key, int modifiers, HotkeyListener listener) {
    return HotkeyManager.getInstance().addHotkey(key, modifiers, listener);
  }

  /**
   * add a hotkey and listener
   * @param key respective key specifier according class Key
   * @param modifiers respective key specifier according class KeyModifiers
   * @param listener a HotKeyListener instance
   * @return true if ok, false otherwise
   */
  public static boolean addHotkey(char key, int modifiers, HotkeyListener listener) {
    return HotkeyManager.getInstance().addHotkey(key, modifiers, listener);
  }

  /**
   * remove a hotkey and listener
   * @param key respective key specifier according class Key
   * @param modifiers respective key specifier according class KeyModifiers
   * @return true if ok, false otherwise
   */
  public static boolean removeHotkey(String key, int modifiers) {
    return HotkeyManager.getInstance().removeHotkey(key, modifiers);
  }

  /**
   * remove a hotkey and listener
   * @param key respective key specifier according class Key
   * @param modifiers respective key specifier according class KeyModifiers
   * @return true if ok, false otherwise
   */
  public static boolean removeHotkey(char key, int modifiers) {
    return HotkeyManager.getInstance().removeHotkey(key, modifiers);
  }

  static String[] keyVK = new String[] {
  //<editor-fold defaultstate="collapsed" desc="VK_xxx constant names and values Java 7">
    "VK_0","48",
    "VK_1","49",
    "VK_2","50",
    "VK_3","51",
    "VK_4","52",
    "VK_5","53",
    "VK_6","54",
    "VK_7","55",
    "VK_8","56",
    "VK_9","57",
    "VK_A","65",
    "VK_ACCEPT","30",
    "VK_ADD","107",
    "VK_AGAIN","65481",
    "VK_ALL_CANDIDATES","256",
    "VK_ALPHANUMERIC","240",
    "VK_ALT","18",
    "VK_ALT_GRAPH","65406",
    "VK_AMPERSAND","150",
    "VK_ASTERISK","151",
    "VK_AT","512",
    "VK_B","66",
    "VK_BACK_QUOTE","192",
    "VK_BACK_SLASH","92",
    "VK_BACK_SPACE","8",
    "VK_BEGIN","65368",
    "VK_BRACELEFT","161",
    "VK_BRACERIGHT","162",
    "VK_C","67",
    "VK_CANCEL","3",
    "VK_CAPS_LOCK","20",
    "VK_CIRCUMFLEX","514",
    "VK_CLEAR","12",
    "VK_CLOSE_BRACKET","93",
    "VK_CODE_INPUT","258",
    "VK_COLON","513",
    "VK_COMMA","44",
    "VK_COMPOSE","65312",
    "VK_CONTEXT_MENU","525",
    "VK_CONTROL","17",
    "VK_CONVERT","28",
    "VK_COPY","65485",
    "VK_CUT","65489",
    "VK_D","68",
    "VK_DEAD_ABOVEDOT","134",
    "VK_DEAD_ABOVERING","136",
    "VK_DEAD_ACUTE","129",
    "VK_DEAD_BREVE","133",
    "VK_DEAD_CARON","138",
    "VK_DEAD_CEDILLA","139",
    "VK_DEAD_CIRCUMFLEX","130",
    "VK_DEAD_DIAERESIS","135",
    "VK_DEAD_DOUBLEACUTE","137",
    "VK_DEAD_GRAVE","128",
    "VK_DEAD_IOTA","141",
    "VK_DEAD_MACRON","132",
    "VK_DEAD_OGONEK","140",
    "VK_DEAD_SEMIVOICED_SOUND","143",
    "VK_DEAD_TILDE","131",
    "VK_DEAD_VOICED_SOUND","142",
    "VK_DECIMAL","110",
    "VK_DELETE","127",
    "VK_DIVIDE","111",
    "VK_DOLLAR","515",
    "VK_DOWN","40",
    "VK_E","69",
    "VK_END","35",
    "VK_ENTER","10",
    "VK_EQUALS","61",
    "VK_ESCAPE","27",
    "VK_EURO_SIGN","516",
    "VK_EXCLAMATION_MARK","517",
    "VK_F","70",
    "VK_F1","112",
    "VK_F10","121",
    "VK_F11","122",
    "VK_F12","123",
    "VK_F13","61440",
    "VK_F14","61441",
    "VK_F15","61442",
    "VK_F16","61443",
    "VK_F17","61444",
    "VK_F18","61445",
    "VK_F19","61446",
    "VK_F2","113",
    "VK_F20","61447",
    "VK_F21","61448",
    "VK_F22","61449",
    "VK_F23","61450",
    "VK_F24","61451",
    "VK_F3","114",
    "VK_F4","115",
    "VK_F5","116",
    "VK_F6","117",
    "VK_F7","118",
    "VK_F8","119",
    "VK_F9","120",
    "VK_FINAL","24",
    "VK_FIND","65488",
    "VK_FULL_WIDTH","243",
    "VK_G","71",
    "VK_GREATER","160",
    "VK_H","72",
    "VK_HALF_WIDTH","244",
    "VK_HELP","156",
    "VK_HIRAGANA","242",
    "VK_HOME","36",
    "VK_I","73",
    "VK_INPUT_METHOD_ON_OFF","263",
    "VK_INSERT","155",
    "VK_INVERTED_EXCLAMATION_MARK","518",
    "VK_J","74",
    "VK_JAPANESE_HIRAGANA","260",
    "VK_JAPANESE_KATAKANA","259",
    "VK_JAPANESE_ROMAN","261",
    "VK_K","75",
    "VK_KANA","21",
    "VK_KANA_LOCK","262",
    "VK_KANJI","25",
    "VK_KATAKANA","241",
    "VK_KP_DOWN","225",
    "VK_KP_LEFT","226",
    "VK_KP_RIGHT","227",
    "VK_KP_UP","224",
    "VK_L","76",
    "VK_LEFT","37",
    "VK_LEFT_PARENTHESIS","519",
    "VK_LESS","153",
    "VK_M","77",
    "VK_META","157",
    "VK_MINUS","45",
    "VK_MODECHANGE","31",
    "VK_MULTIPLY","106",
    "VK_N","78",
    "VK_NONCONVERT","29",
    "VK_NUM_LOCK","144",
    "VK_NUMBER_SIGN","520",
    "VK_NUMPAD0","96",
    "VK_NUMPAD1","97",
    "VK_NUMPAD2","98",
    "VK_NUMPAD3","99",
    "VK_NUMPAD4","100",
    "VK_NUMPAD5","101",
    "VK_NUMPAD6","102",
    "VK_NUMPAD7","103",
    "VK_NUMPAD8","104",
    "VK_NUMPAD9","105",
    "VK_O","79",
    "VK_OPEN_BRACKET","91",
    "VK_P","80",
    "VK_PAGE_DOWN","34",
    "VK_PAGE_UP","33",
    "VK_PASTE","65487",
    "VK_PAUSE","19",
    "VK_PERIOD","46",
    "VK_PLUS","521",
    "VK_PREVIOUS_CANDIDATE","257",
    "VK_PRINTSCREEN","154",
    "VK_PROPS","65482",
    "VK_Q","81",
    "VK_QUOTE","222",
    "VK_QUOTEDBL","152",
    "VK_R","82",
    "VK_RIGHT","39",
    "VK_RIGHT_PARENTHESIS","522",
    "VK_ROMAN_CHARACTERS","245",
    "VK_S","83",
    "VK_SCROLL_LOCK","145",
    "VK_SEMICOLON","59",
    "VK_SEPARATER","108",
    "VK_SEPARATOR","108",
    "VK_SHIFT","16",
    "VK_SLASH","47",
    "VK_SPACE","32",
    "VK_STOP","65480",
    "VK_SUBTRACT","109",
    "VK_T","84",
    "VK_TAB","9",
    "VK_U","85",
    "VK_UNDEFINED","0",
    "VK_UNDERSCORE","523",
    "VK_UNDO","65483",
    "VK_UP","38",
    "VK_V","86",
    "VK_W","87",
    "VK_WINDOWS","524",
    "VK_X","88",
    "VK_Y","89",
    "VK_Z","90"
  //</editor-fold>
  };

  // non function keys on US-Keyboard per line top down without modifier keys
  public static String keyboardUS =
          "1234567890-=" + "qwertyuiop[]" + "asdfghjkl;'\\" + "`zxcvbnm,./";

  //<editor-fold defaultstate="collapsed" desc="KeyNames to UniCode (used in type() with Key.XXX)">
  public static final String SPACE = " ";
  public static final String ENTER = "\r";
  public static final String BACKSPACE = "\b";
  public static final String TAB = "\t";
  public static final String ESC = "\u001b";
  public static final char C_ESC = '\u001b';
  public static final String UP = "\ue000";
  public static final char C_UP = '\ue000';
  public static final String RIGHT = "\ue001";
  public static final char C_RIGHT = '\ue001';
  public static final String DOWN = "\ue002";
  public static final char C_DOWN = '\ue002';
  public static final String LEFT = "\ue003";
  public static final char C_LEFT = '\ue003';
  public static final String PAGE_UP = "\ue004";
  public static final char C_PAGE_UP = '\ue004';
  public static final String PAGE_DOWN = "\ue005";
  public static final char C_PAGE_DOWN = '\ue005';
  public static final String DELETE = "\ue006";
  public static final char C_DELETE = '\ue006';
  public static final String END = "\ue007";
  public static final char C_END = '\ue007';
  public static final String HOME = "\ue008";
  public static final char C_HOME = '\ue008';
  public static final String INSERT = "\ue009";
  public static final char C_INSERT = '\ue009';
  public static final String F1 = "\ue011";
  public static final char C_F1 = '\ue011';
  public static final String F2 = "\ue012";
  public static final char C_F2 = '\ue012';
  public static final String F3 = "\ue013";
  public static final char C_F3 = '\ue013';
  public static final String F4 = "\ue014";
  public static final char C_F4 = '\ue014';
  public static final String F5 = "\ue015";
  public static final char C_F5 = '\ue015';
  public static final String F6 = "\ue016";
  public static final char C_F6 = '\ue016';
  public static final String F7 = "\ue017";
  public static final char C_F7 = '\ue017';
  public static final String F8 = "\ue018";
  public static final char C_F8 = '\ue018';
  public static final String F9 = "\ue019";
  public static final char C_F9 = '\ue019';
  public static final String F10 = "\ue01A";
  public static final char C_F10 = '\ue01A';
  public static final String F11 = "\ue01B";
  public static final char C_F11 = '\ue01B';
  public static final String F12 = "\ue01C";
  public static final char C_F12 = '\ue01C';
  public static final String F13 = "\ue01D";
  public static final char C_F13 = '\ue01D';
  public static final String F14 = "\ue01E";
  public static final char C_F14 = '\ue01E';
  public static final String F15 = "\ue01F";
  public static final char C_F15 = '\ue01F';
  public static final String SHIFT = "\ue020";
  public static final char C_SHIFT = '\ue020';
  public static final String CTRL = "\ue021";
  public static final char C_CTRL = '\ue021';
  public static final String ALT = "\ue022";
  public static final char C_ALT = '\ue022';
  public static final String ALTGR = "\ue043";
  public static final char C_ALTGR = '\ue043';
  public static final String META = "\ue023";
  public static final char C_META = '\ue023';
  public static final String CMD = "\ue023";
  public static final char C_CMD = '\ue023';
  public static final String WIN = "\ue042";
  public static final char C_WIN = '\ue042';
  public static final String PRINTSCREEN = "\ue024";
  public static final char C_PRINTSCREEN = '\ue024';
  public static final String SCROLL_LOCK = "\ue025";
  public static final char C_SCROLL_LOCK = '\ue025';
  public static final String PAUSE = "\ue026";
  public static final char C_PAUSE = '\ue026';
  public static final String CAPS_LOCK = "\ue027";
  public static final char C_CAPS_LOCK = '\ue027';
  public static final String NUM0 = "\ue030";
  public static final char C_NUM0 = '\ue030';
  public static final String NUM1 = "\ue031";
  public static final char C_NUM1 = '\ue031';
  public static final String NUM2 = "\ue032";
  public static final char C_NUM2 = '\ue032';
  public static final String NUM3 = "\ue033";
  public static final char C_NUM3 = '\ue033';
  public static final String NUM4 = "\ue034";
  public static final char C_NUM4 = '\ue034';
  public static final String NUM5 = "\ue035";
  public static final char C_NUM5 = '\ue035';
  public static final String NUM6 = "\ue036";
  public static final char C_NUM6 = '\ue036';
  public static final String NUM7 = "\ue037";
  public static final char C_NUM7 = '\ue037';
  public static final String NUM8 = "\ue038";
  public static final char C_NUM8 = '\ue038';
  public static final String NUM9 = "\ue039";
  public static final char C_NUM9 = '\ue039';
  public static final String SEPARATOR = "\ue03A";
  public static final char C_SEPARATOR = '\ue03A';
  public static final String NUM_LOCK = "\ue03B";
  public static final char C_NUM_LOCK = '\ue03B';
  public static final String ADD = "\ue03C";
  public static final char C_ADD = '\ue03C';
  public static final String MINUS = "\ue03D";
  public static final char C_MINUS = '\ue03D';
  public static final String MULTIPLY = "\ue03E";
  public static final char C_MULTIPLY = '\ue03E';
  public static final String DIVIDE = "\ue03F";
  public static final char C_DIVIDE = '\ue03F';
  public static final String DECIMAL = "\ue040";
  public static final char C_DECIMAL = '\ue040'; // VK_DECIMAL
  public static final String CONTEXT = "\ue041";
  public static final char C_CONTEXT = '\ue041'; // VK_CONTEXT_MENU
  public static final String NEXT = "\ue044";
  public static final char C_NEXT = '\ue044'; // VK_CONTEXT_MENU

  public static final char cMax = '\ue050';
  public static final char cMin = '\ue000';
  public static int keyMaxLength;
//</editor-fold>

  private static Map<String, Integer> keyTexts = new HashMap<String, Integer>();
  private static Map<Integer, String> keys = new HashMap<Integer, String>();

  static {
    //<editor-fold defaultstate="collapsed" desc="create the keyname map used with write()">
    String sKey;
    keyMaxLength = 0;
    for (char c = cMin; c < cMax; c++) {
      sKey = toJavaKeyCodeText(c);
      if (!sKey.equals(""+c)) {
        keyTexts.put(sKey, toJavaKeyCode(c)[0]);
        keyMaxLength = sKey.length() > keyMaxLength ? sKey.length() : keyMaxLength;
      }
    }
    keyTexts.put("#ENTER.", toJavaKeyCode('\n')[0]);
    keyTexts.put("#N.", toJavaKeyCode('\n')[0]);
    keyTexts.put("#BACK.", toJavaKeyCode('\b')[0]);
    keyTexts.put("#B.", toJavaKeyCode('\b')[0]);
    keyTexts.put("#TAB.", toJavaKeyCode('\t')[0]);
    keyTexts.put("#T.", toJavaKeyCode('\t')[0]);
    keyTexts.put("#X.", toJavaKeyCode(C_NEXT)[0]);
    keyTexts.put("#ESC.", toJavaKeyCode(C_ESC)[0]);
    keyTexts.put("#U.", toJavaKeyCode(C_UP)[0]);
    keyTexts.put("#D.", toJavaKeyCode(C_DOWN)[0]);
    keyTexts.put("#L.", toJavaKeyCode(C_LEFT)[0]);
    keyTexts.put("#R.", toJavaKeyCode(C_RIGHT)[0]);
    keyTexts.put("#S.", toJavaKeyCode(C_SHIFT)[0]);
    keyTexts.put("#S+", toJavaKeyCode(C_SHIFT)[0]);
    keyTexts.put("#S-", toJavaKeyCode(C_SHIFT)[0]);
    keyTexts.put("#A.", toJavaKeyCode(C_ALT)[0]);
    keyTexts.put("#A+", toJavaKeyCode(C_ALT)[0]);
    keyTexts.put("#A-", toJavaKeyCode(C_ALT)[0]);
    keyTexts.put("#C.", toJavaKeyCode(C_CTRL)[0]);
    keyTexts.put("#C+", toJavaKeyCode(C_CTRL)[0]);
    keyTexts.put("#C-", toJavaKeyCode(C_CTRL)[0]);
    keyTexts.put("#M.", toJavaKeyCode(C_META)[0]);
    keyTexts.put("#M+", toJavaKeyCode(C_META)[0]);
    keyTexts.put("#M-", toJavaKeyCode(C_META)[0]);
    for (String k : keyTexts.keySet()) {
      if (Debug.getDebugLevel() > 3) {
        Debug.log(4, "Key: %s is: %s", k, KeyEvent.getKeyText(keyTexts.get(k)));
      }
      if (k.length() < 4) {
        continue;
      }
      keys.put(keyTexts.get(k), k);
    }
    //</editor-fold>
  }

  //<editor-fold defaultstate="collapsed" desc="support functions for write()">
  public static String getTextFromKeycode(int key) {
    return keys.get(key);
  }

  public static boolean isRepeatable(String token) {
    int key = toJavaKeyCodeFromText(token);
    switch (key) {
      case KeyEvent.VK_UP: return true;
      case KeyEvent.VK_DOWN: return true;
      case KeyEvent.VK_RIGHT: return true;
      case KeyEvent.VK_LEFT: return true;
      case -KeyEvent.VK_TAB: return true;
      case KeyEvent.VK_TAB: return true;
      case KeyEvent.VK_ENTER: return true;
      case KeyEvent.VK_BACK_SPACE: return true;
    }
    return false;
  }

  public static boolean isModifier(String token) {
    if (toJavaKeyCodeFromText(token) == toJavaKeyCodeFromText("#S.") ||
        toJavaKeyCodeFromText(token) == toJavaKeyCodeFromText("#C.") ||
        toJavaKeyCodeFromText(token) == toJavaKeyCodeFromText("#A.") ||
        toJavaKeyCodeFromText(token) == toJavaKeyCodeFromText("#M.")) {
      return true;
    }
    return false;
  }

  public static int toJavaKeyCodeFromText(String key) {
    if (null == keyTexts.get(key)) {
      return -1;
    } else {
      return keyTexts.get(key).intValue();
    }
  }

  public static void dump() {
    Map<Integer, String> namesVK = new HashMap<Integer, String>();
    for (int i = 0; i < keyVK.length; i += 2) {
      namesVK.put(Integer.decode(keyVK[i+1]), keyVK[i].substring(3));
    }
    Map<String, Integer> sortedNames = new TreeMap<String, Integer>(keyTexts);
    System.out.println("[info] Key: dump keynames (tokens) used with Region write");
    System.out.println("[info] Token to use --- KeyEvent::keycode --- KeyEvent::keyname");
    int keyN;
    for (String key : sortedNames.keySet()) {
      keyN = sortedNames.get(key);
      if (keyN < 1) {
        continue;
      }
      System.out.println(String.format("%s = %d (%s)", key, keyN, namesVK.get(keyN)));
    }
  }
  //</editor-fold>

  /**
   * Convert Sikuli Key to Java virtual key code
	 * @param key as String
	 * @return the Java KeyCodes
   */
  public static int[] toJavaKeyCode(String key) {
    if (key.length() > 0) {
      return toJavaKeyCode(key.charAt(0));
    }
    return null;
  }

  /**
   * Convert Sikuli Key to Java virtual key code
	 * @param key as Character
	 * @return the Java KeyCodes
   */
  public static int[] toJavaKeyCode(char key) {
    switch (key) {
//Lowercase
      case 'a': return new int[]{KeyEvent.VK_A};
      case 'b': return new int[]{KeyEvent.VK_B};
      case 'c': return new int[]{KeyEvent.VK_C};
      case 'd': return new int[]{KeyEvent.VK_D};
      case 'e': return new int[]{KeyEvent.VK_E};
      case 'f': return new int[]{KeyEvent.VK_F};
      case 'g': return new int[]{KeyEvent.VK_G};
      case 'h': return new int[]{KeyEvent.VK_H};
      case 'i': return new int[]{KeyEvent.VK_I};
      case 'j': return new int[]{KeyEvent.VK_J};
      case 'k': return new int[]{KeyEvent.VK_K};
      case 'l': return new int[]{KeyEvent.VK_L};
      case 'm': return new int[]{KeyEvent.VK_M};
      case 'n': return new int[]{KeyEvent.VK_N};
      case 'o': return new int[]{KeyEvent.VK_O};
      case 'p': return new int[]{KeyEvent.VK_P};
      case 'q': return new int[]{KeyEvent.VK_Q};
      case 'r': return new int[]{KeyEvent.VK_R};
      case 's': return new int[]{KeyEvent.VK_S};
      case 't': return new int[]{KeyEvent.VK_T};
      case 'u': return new int[]{KeyEvent.VK_U};
      case 'v': return new int[]{KeyEvent.VK_V};
      case 'w': return new int[]{KeyEvent.VK_W};
      case 'x': return new int[]{KeyEvent.VK_X};
      case 'y': return new int[]{KeyEvent.VK_Y};
      case 'z': return new int[]{KeyEvent.VK_Z};
//Uppercase
      case 'A': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_A};
      case 'B': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_B};
      case 'C': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_C};
      case 'D': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_D};
      case 'E': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_E};
      case 'F': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_F};
      case 'G': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_G};
      case 'H': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_H};
      case 'I': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_I};
      case 'J': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_J};
      case 'K': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_K};
      case 'L': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_L};
      case 'M': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_M};
      case 'N': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_N};
      case 'O': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_O};
      case 'P': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_P};
      case 'Q': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Q};
      case 'R': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_R};
      case 'S': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_S};
      case 'T': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_T};
      case 'U': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_U};
      case 'V': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_V};
      case 'W': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_W};
      case 'X': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_X};
      case 'Y': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Y};
      case 'Z': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Z};
//Row 3 (below function keys)
//      case '§': return new int[]{192}; //not producable
      case '1': return new int[]{KeyEvent.VK_1};
      case '!': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_1};
      case '2': return new int[]{KeyEvent.VK_2};
      case '@': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_2};
      case '3': return new int[]{KeyEvent.VK_3};
      case '#': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_3};
      case '4': return new int[]{KeyEvent.VK_4};
      case '$': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_4};
      case '5': return new int[]{KeyEvent.VK_5};
      case '%': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_5};
      case '6': return new int[]{KeyEvent.VK_6};
      case '^': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_6};
      case '7': return new int[]{KeyEvent.VK_7};
      case '&': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_7};
      case '8': return new int[]{KeyEvent.VK_8};
      case '*': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_8};
      case '9': return new int[]{KeyEvent.VK_9};
      case '(': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_9};
      case '0': return new int[]{KeyEvent.VK_0};
      case ')': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_0};
      case '-': return new int[]{KeyEvent.VK_MINUS};
      case '_': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS};
      case '=': return new int[]{KeyEvent.VK_EQUALS};
      case '+': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS};
//Row 2
// q w e r t y u i o p
      case '[': return new int[]{KeyEvent.VK_OPEN_BRACKET};
      case '{': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET};
      case ']': return new int[]{KeyEvent.VK_CLOSE_BRACKET};
      case '}': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET};
//Row 1
// a s d f g h j k l
      case ';': return new int[]{KeyEvent.VK_SEMICOLON};
      case ':': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON};
      case '\'': return new int[]{KeyEvent.VK_QUOTE};
      case '"': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE};
      case '\\': return new int[]{KeyEvent.VK_BACK_SLASH};
      case '|': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH};
//RETURN, BACKSPACE, TAB
      case '\b': return new int[]{KeyEvent.VK_BACK_SPACE};
      case '\t': return new int[]{KeyEvent.VK_TAB};
      case '\r': return new int[]{KeyEvent.VK_ENTER};
      case '\n': return new int[]{KeyEvent.VK_ENTER};
//SPACE
      case ' ': return new int[]{KeyEvent.VK_SPACE};
//Row 0 (first above SPACE)
      case '`': return new int[]{KeyEvent.VK_BACK_QUOTE};
      case '~': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE};
// z x c v b n m
      case ',': return new int[]{KeyEvent.VK_COMMA};
      case '<': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA};
      case '.': return new int[]{KeyEvent.VK_PERIOD};
      case '>': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD};
      case '/': return new int[]{KeyEvent.VK_SLASH};
      case '?': return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH};
//Modifier
      case Key.C_SHIFT: return new int[]{KeyEvent.VK_SHIFT};
      case Key.C_CTRL:  return new int[]{KeyEvent.VK_CONTROL};
      case Key.C_ALT:   return new int[]{KeyEvent.VK_ALT};
      case Key.C_META:  return new int[]{KeyEvent.VK_META};
//Cursor movement
      case Key.C_UP:     return new int[]{KeyEvent.VK_UP};
      case Key.C_RIGHT:     return new int[]{KeyEvent.VK_RIGHT};
      case Key.C_DOWN:      return new int[]{KeyEvent.VK_DOWN};
      case Key.C_LEFT:      return new int[]{KeyEvent.VK_LEFT};
      case Key.C_PAGE_UP:   return new int[]{KeyEvent.VK_PAGE_UP};
      case Key.C_PAGE_DOWN: return new int[]{KeyEvent.VK_PAGE_DOWN};
      case Key.C_END:       return new int[]{KeyEvent.VK_END};
      case Key.C_HOME:      return new int[]{KeyEvent.VK_HOME};
      case Key.C_DELETE:    return new int[]{KeyEvent.VK_DELETE};
//Function keys
      case Key.C_ESC: return new int[]{KeyEvent.VK_ESCAPE};
      case Key.C_F1:  return new int[]{KeyEvent.VK_F1};
      case Key.C_F2:  return new int[]{KeyEvent.VK_F2};
      case Key.C_F3:  return new int[]{KeyEvent.VK_F3};
      case Key.C_F4:  return new int[]{KeyEvent.VK_F4};
      case Key.C_F5:  return new int[]{KeyEvent.VK_F5};
      case Key.C_F6:  return new int[]{KeyEvent.VK_F6};
      case Key.C_F7:  return new int[]{KeyEvent.VK_F7};
      case Key.C_F8:  return new int[]{KeyEvent.VK_F8};
      case Key.C_F9:  return new int[]{KeyEvent.VK_F9};
      case Key.C_F10: return new int[]{KeyEvent.VK_F10};
      case Key.C_F11: return new int[]{KeyEvent.VK_F11};
      case Key.C_F12: return new int[]{KeyEvent.VK_F12};
      case Key.C_F13: return new int[]{KeyEvent.VK_F13};
      case Key.C_F14: return new int[]{KeyEvent.VK_F14};
      case Key.C_F15: return new int[]{KeyEvent.VK_F15};
//Toggling kezs
      case Key.C_SCROLL_LOCK: return new int[]{KeyEvent.VK_SCROLL_LOCK};
      case Key.C_NUM_LOCK:    return new int[]{KeyEvent.VK_NUM_LOCK};
      case Key.C_CAPS_LOCK:   return new int[]{KeyEvent.VK_CAPS_LOCK};
      case Key.C_INSERT:      return new int[]{KeyEvent.VK_INSERT};
//Windows special
      case Key.C_PAUSE:       return new int[]{KeyEvent.VK_PAUSE};
      case Key.C_PRINTSCREEN: return new int[]{KeyEvent.VK_PRINTSCREEN};
//Num pad
      case Key.C_NUM0: return new int[]{KeyEvent.VK_NUMPAD0};
      case Key.C_NUM1: return new int[]{KeyEvent.VK_NUMPAD1};
      case Key.C_NUM2: return new int[]{KeyEvent.VK_NUMPAD2};
      case Key.C_NUM3: return new int[]{KeyEvent.VK_NUMPAD3};
      case Key.C_NUM4: return new int[]{KeyEvent.VK_NUMPAD4};
      case Key.C_NUM5: return new int[]{KeyEvent.VK_NUMPAD5};
      case Key.C_NUM6: return new int[]{KeyEvent.VK_NUMPAD6};
      case Key.C_NUM7: return new int[]{KeyEvent.VK_NUMPAD7};
      case Key.C_NUM8: return new int[]{KeyEvent.VK_NUMPAD8};
      case Key.C_NUM9: return new int[]{KeyEvent.VK_NUMPAD9};
//Num pad special
      case Key.C_SEPARATOR: return new int[]{KeyEvent.VK_SEPARATOR};
      case Key.C_ADD:       return new int[]{KeyEvent.VK_ADD};
      case Key.C_MINUS:     return new int[]{KeyEvent.VK_SUBTRACT};
      case Key.C_MULTIPLY:  return new int[]{KeyEvent.VK_MULTIPLY};
      case Key.C_DIVIDE:    return new int[]{KeyEvent.VK_DIVIDE};
      case Key.C_DECIMAL:   return new int[]{KeyEvent.VK_DECIMAL};
      case Key.C_CONTEXT:   return new int[]{KeyEvent.VK_CONTEXT_MENU};
      case Key.C_WIN:   return new int[]{KeyEvent.VK_WINDOWS};
//hack: alternative tab in GUI
      case Key.C_NEXT:   return new int[]{-KeyEvent.VK_TAB};

      default:
        throw new IllegalArgumentException("Key: Not supported character: " + key);
    }
  }

  /**
   *
   * @param key as Character
   * @return a printable version of a special key
   */
  public static String toJavaKeyCodeText(char key) {
    switch (key) {
//RETURN, BACKSPACE, TAB
      case '\b': return "#BACK.";
      case '\t': return "#TAB.";
      case C_NEXT: return "#TAB.";
      case '\r': return "#ENTER.";
      case '\n': return "#ENTER.";
//Cursor movement
      case Key.C_UP: return "#UP.";
      case Key.C_RIGHT: return "#RIGHT.";
      case Key.C_DOWN: return "#DOWN.";
      case Key.C_LEFT: return "#LEFT.";
      case Key.C_PAGE_UP: return "#PUP.";
      case Key.C_PAGE_DOWN: return "#PDOWN.";
      case Key.C_END: return "#END.";
      case Key.C_HOME: return "#HOME.";
      case Key.C_DELETE: return "#DEL.";
//Function keys
      case Key.C_ESC: return "#ESC.";
      case Key.C_F1: return "#F1.";
      case Key.C_F2: return "#F2.";
      case Key.C_F3: return "#F3.";
      case Key.C_F4: return "#F4.";
      case Key.C_F5: return "#F5.";
      case Key.C_F6: return "#F6.";
      case Key.C_F7: return "#F7.";
      case Key.C_F8: return "#F8.";
      case Key.C_F9: return "#F9.";
      case Key.C_F10: return "#F10.";
      case Key.C_F11: return "#F11.";
      case Key.C_F12: return "#F12.";
      case Key.C_F13: return "#F13.";
      case Key.C_F14: return "#F14.";
      case Key.C_F15: return "#F15.";
//Toggling kezs
      case Key.C_SCROLL_LOCK: return "#SCROLL_LOCK.";
      case Key.C_NUM_LOCK: return "#NUM_LOCK.";
      case Key.C_CAPS_LOCK: return "#CAPS_LOCK.";
      case Key.C_INSERT: return "#INS.";
//Windows special
      case Key.C_PAUSE: return "#PAUSE.";
      case Key.C_PRINTSCREEN: return "#PRINTSCREEN.";
      case Key.C_WIN:   return "#WIN.";
//Num pad
      case Key.C_NUM0: return "#NUM0.";
      case Key.C_NUM1: return "#NUM1.";
      case Key.C_NUM2: return "#NUM2.";
      case Key.C_NUM3: return "#NUM3.";
      case Key.C_NUM4: return "#NUM4.";
      case Key.C_NUM5: return "#NUM5.";
      case Key.C_NUM6: return "#NUM6.";
      case Key.C_NUM7: return "#NUM7.";
      case Key.C_NUM8: return "#NUM8.";
      case Key.C_NUM9: return "#NUM9.";
//Num pad special
      case Key.C_SEPARATOR: return "#NSEP.";
      case Key.C_ADD: return "#NADD.";
      case Key.C_MINUS: return "#NSUB.";
      case Key.C_MULTIPLY: return "#NMUL";
      case Key.C_DIVIDE: return "#NDIV.";
      case Key.C_DECIMAL: return "#NDEC.";
      case Key.C_CONTEXT: return "#NCON.";
//KeyModifiers
      case Key.C_SHIFT: return "#SHIFT.";
      case Key.C_CTRL:  return "#CTRL.";
      case Key.C_ALT:   return "#ALT.";
      case Key.C_META:  return "#META.";

      default:
        return "" + key;
    }
  }

  protected static int convertModifiers(String mod) {
    int modNew = 0;
    char key;
    for (int i = 0; i < mod.length(); i++) {
      key = mod.charAt(i);
      if (Key.C_CTRL == key) {
        modNew |= KeyModifier.CTRL;
      } else if (Key.C_ALT == key) {
        modNew |= KeyModifier.ALT;
      } else if (Key.C_SHIFT == key) {
        modNew |= KeyModifier.SHIFT;
      } else if (Key.C_CMD == key) {
        modNew |= KeyModifier.CMD;
      } else if (Key.C_META == key) {
        modNew |= KeyModifier.META;
      } else if (Key.C_ALTGR == key) {
        modNew |= KeyModifier.ALTGR;
      } else if (Key.C_WIN == key) {
        modNew |= KeyModifier.WIN;
      }
    }
    return modNew;
  }

  /**
   * get the lock state of the given key
   *
   * @param key as Character (scroll, caps, num)
   * @return true/false
   */
  public static boolean isLockOn(char key) {
//    Toolkit tk = Toolkit.getDefaultToolkit();
//        return tk.getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);
//        return tk.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
//        return tk.getLockingKeyState(KeyEvent.VK_NUM_LOCK);
    if (!RunTime.get().runningWindows) {
      return false;
    }
    switch (key) {
      case '\ue025':
        return SysJNA.WinUser32.isScrollLockOn();
      case '\ue027':
        return SysJNA.WinUser32.isCapsLockOn();
      case '\ue03B':
        return SysJNA.WinUser32.isNumLockOn();
      default:
        return false;
    }
  }

	/**
	 * HotKey modifier to be used with Sikuli's HotKey feature
	 * @return META(CMD) on Mac, CTRL otherwise
	 */
	public static int getHotkeyModifier() {
    if (Settings.isMac()) {
      return KeyEvent.VK_META;
    } else {
      return KeyEvent.VK_CONTROL;
    }
  }

  //<editor-fold defaultstate="collapsed" desc="keyboard setup - INTERNAL USE ONLY">
  static String[] result = new String[] {""};

  /**
   * INTERNAL USE ONLY
   * create an alternate keycode table for a non-US keyboard
   */
//	public static void keyBoardSetup() {
//		Map<Character, Integer[]> keyLocal = new HashMap<Character, Integer[]>();
//		String[] keyXX = new String[]{"", "", "", "",};
//		String keysx = Key.keyboardUS;
//
//		try {
//			Screen s = new Screen(0);
//			ImagePath.add("org.sikuli.basics.SikuliX/Images");
//
//			ShowKeyBoardSetupWindow win = new ShowKeyBoardSetupWindow();
//			win.start();
//			s.wait(3.0F);
//
//			Location btnOK = s.find("SikuliLogo").getCenter();
//			Location txtArea = btnOK.offset(0, 400);
//			s.click(txtArea);
//			s.wait(1.0F);
//			String[] mods = new String[]{"", "S", "A", "SA"};
//			Debug.setDebugLevel(0);
//			for (String modx : mods) {
//				s.paste((modx.isEmpty() ? "NO" : modx) + " modifier" + "\n");
//				if (!modx.isEmpty()) {
//					if (modx.length() == 1) {
//						modx = "#" + modx + ".";
//					} else if (modx.length() == 2) {
//						modx = "#" + modx.substring(0, 1) + ".#" + modx.substring(1, 2) + ".";
//					} else {
//						break;
//					}
//				}
//				String c;
//				for (int n = 0; n < keysx.length(); n++) {
//					c = "" + keysx.charAt(n);
//					s.paste(c);
//					s.type(" ");
//					if (Mouse.hasMoved()) {
//						s.click(txtArea);
//						s.wait(0.3F);
//					}
//					s.write(String.format("%s%s ", modx, c));
//					if ("=".equals(c) || "]".equals(c) || "\\".equals(c)) {
//						s.paste("\n");
//					}
//				}
//				s.paste("\n");
//				s.type(Key.ENTER);
//			}
//			Debug.setDebugLevel(3);
//			s.click(btnOK);
//		} catch (FindFailed e) {
//			Debug.error("KeyBoardSetup: Does not work - getting FindFailed");
//			return;
//		}
//		while (true) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException ex) {
//			}
//			Debug.log(3, "waiting for result\n" + result[0]);
//			if (!result[0].isEmpty()) {
//				break;
//			}
//		}
//		String[] keyNewx = result[0].split("\n");
//		String keysNew;
//		String mod;
//		int nKDE = 0;
//		for (int n = 0; n < keyNewx.length; n += 6) {
//			mod = keyNewx[n].substring(0, 2);
//			keysNew = keyNewx[n + 1] + keyNewx[n + 2] + keyNewx[n + 3] + keyNewx[n + 4];
//			keyXX[nKDE] = mod + ": " + keysNew;
//			Debug.log(3, "%s", keyXX[nKDE]);
//			nKDE++;
//		}
//		String kSet;
//		int offset = 4;
//		char keyOld, keyNew;
//		int[] codeA;
//		String codeS;
//		String modText;
//		int nOld;
//		for (int n = 0; n < 4; n++) {
//			kSet = keyXX[n].substring(4);
//			mod = keyXX[n].substring(0, 2);
//			modText = "";
//			if ("S ".equals(mod)) {
//				modText = "SHIFT ";
//			} else if ("A ".equals(mod)) {
//				modText = "ALT ";
//			} else if ("SA".equals(mod)) {
//				modText = "SHIFT ALT ";
//			}
//			Debug.log(3, mod + "\n" + kSet);
//			nOld = 0;
//			for (int i = 0; i < kSet.length(); i++) {
//				if (i + 3 > kSet.length()) {
//					break;
//				}
//				if (!" ".equals("" + kSet.charAt(i + 3))) {
//					offset = 3;
//				}
//				keyOld = kSet.charAt(i);
//				keyNew = kSet.charAt(i + 2);
//				Integer[] codeI = new Integer[]{-1, -1, -1};
//				try {
//					codeA = Key.toJavaKeyCode(keyNew);
//					codeS = "";
//					for (int iK : codeA) {
//						codeS += KeyEvent.getKeyText(iK) + " ";
//					}
//					if (codeA.length == 1) {
//						codeI[2] = codeA[0];
//					} else if (codeA.length == 2) {
//						codeI[2] = codeA[1];
//						codeI[0] = codeA[0];
//					} else if (codeA.length == 3) {
//						codeI[2] = codeA[2];
//						codeI[0] = codeA[0];
//						codeI[1] = codeA[1];
//					}
//				} catch (Exception e) {
//					codeS = "UNKNOWN ";
//					codeI[2] = Key.toJavaKeyCode(keysx.charAt(nOld))[0];
//					codeS += modText + KeyEvent.getKeyText(codeI[2]);
//					for (int ik = 0; ik < 2; ik++) {
//						if (mod.charAt(ik) == 'S') {
//							codeI[ik] = KeyEvent.VK_SHIFT;
//						} else if (mod.charAt(ik) == 'A') {
//							codeI[ik] = KeyEvent.VK_ALT;
//						}
//					}
//				}
//				if (keyLocal.get(keyNew) == null) {
//					keyLocal.put(keyNew, codeI);
//				}
//				Debug.log(3, "%s %c %c %s", mod, keyOld, keyNew, codeS);
//				i += offset - 1;
//				offset = 4;
//				nOld += 1;
//			}
//		}
//
//		Debug.log(3, "--------------- keyLocal");
//		Integer[] codeI;
//		for (Character kc : keyLocal.keySet()) {
//			codeI = keyLocal.get(kc);
//			Debug.log(3, "%c %s %s %s", kc.charValue(),
//							(codeI[0] == -1 ? "" : KeyEvent.getKeyText(codeI[0])),
//							(codeI[1] == -1 ? "" : KeyEvent.getKeyText(codeI[1])),
//							(codeI[2] == -1 ? "" : KeyEvent.getKeyText(codeI[2])));
//		}
//	}

  /**
   * INTERNAL USE ONLY
   *
   * @param code keycode
   * @param mod modifier
   * @return readable key text
   */
  public static String convertKeyToText(int code, int mod) {
    String txtMod = KeyEvent.getKeyModifiersText(mod);
    String txtCode = KeyEvent.getKeyText(code);
    String ret;
    if (code == KeyEvent.VK_ALT || code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_SHIFT) {
      ret = txtMod;
    } else {
      ret = txtMod + " " + txtCode;
    }
    return ret;
  }

//  static class ShowKeyBoardSetupWindow extends Thread {
//
//    JFrame kbSetup;
//
//    @Override
//    public void run() {
//      BufferedImage img = Image.create("SikuliLogo").get();
//      Debug.log(3, "KBSetup: %s", img);
//      Image.dump();
//      kbSetup = new JFrame("Localized Keyboard Setup");
//      Container mpwinCP = kbSetup.getContentPane();
//      mpwinCP.setLayout(new BorderLayout());
//      KeyBoardSetupWindow win = new KeyBoardSetupWindow(result);
//      mpwinCP.add(win, BorderLayout.CENTER);
//      kbSetup.pack();
//      kbSetup.setAlwaysOnTop(true);
//      kbSetup.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//      win.setLogo(new ImageIcon(img));
//      kbSetup.setVisible(true);
//    }
//  }

  /**
   * INTERNAL USE ONLY
   * create a table containing all relevant key, keycode and keytext settings for VK_xxx
   */
  public static void createKeyTable() {
    Map<String, String> namesVK = new HashMap<String, String>();
    for (int i = 0; i < keyVK.length; i += 2) {
      namesVK.put(keyVK[i+1], keyVK[i].substring(3));
    }

    String keyText;
    int upper = 66000;
    String form = "%05d: %-25s %-20s %-15s %15s %s";
    for (int key = 0; key < upper; key++) {
      char ckey = (char) key;
      String k1 = "...";
      String k2 = "...";
      try {
        if (toJavaKeyCode((ckey)).length > 1) {
                  k1 = namesVK.get(new Integer(toJavaKeyCode(ckey)[0]).toString());
                  k2 = namesVK.get(new Integer(toJavaKeyCode(ckey)[1]).toString());
        } else if (key > 32) {
          k2 = namesVK.get(new Integer(toJavaKeyCode(ckey)[0]).toString());
        }
      } catch (Exception e) {      }
      keyText = KeyEvent.getKeyText(key);
      if (keyText.startsWith("Unknown") && ("...".equals(k2) || key > 255)) {
        continue;
      }
      if (keyText.startsWith("Unknown")) {
        keyText = "???";
      }
      if (keyText.length() == 1) {
        if (key < 256 ) {
          if (key < 32) {
            key = ' ';
          }
          Debug.log(3, form, key, keyText, "...", "...", k2, k1);
        }
        continue;
      }
      keyText = keyText.replaceAll(" ", "_");
      String vkey = namesVK.get(new Integer(key).toString());
      if (vkey == null || vkey.equals(keyText.toUpperCase())) {
        vkey = "...";
      }
      if (null == getTextFromKeycode(key)) {
        Debug.log(3, form, key, keyText.toUpperCase(),
                vkey, "...", k2, k1);
      } else {
        Debug.log(3, form, key, keyText.toUpperCase(),
                vkey, getTextFromKeycode(key), k2, k1);
      }
    }
  }
  //</editor-fold>
}
