/*
 * Copyright (c) 2017 - sikulix.com - MIT license
 */
package org.sikuli.hotkey;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * this class implements an interface to the Java key system
 * as represented by java.awt.event.KeyEvent.
 * for the functions type() and write()
 * by translating key constants for special keys and plain text per character.<br>
 */
public class Keys {

  static Map<Integer, String> namesVK = new HashMap<>();

  static String[] keyVK = new String[]{
    //<editor-fold defaultstate="collapsed" desc="VK_xxx constant names and values Java 7">
          "VK_0", "48",
          "VK_1", "49",
          "VK_2", "50",
          "VK_3", "51",
          "VK_4", "52",
          "VK_5", "53",
          "VK_6", "54",
          "VK_7", "55",
          "VK_8", "56",
          "VK_9", "57",
          "VK_A", "65",
          "VK_ACCEPT", "30",
          "VK_ADD", "107",
          "VK_AGAIN", "65481",
          "VK_ALL_CANDIDATES", "256",
          "VK_ALPHANUMERIC", "240",
          "VK_ALT", "18",
          "VK_ALT_GRAPH", "65406",
          "VK_AMPERSAND", "150",
          "VK_ASTERISK", "151",
          "VK_AT", "512",
          "VK_B", "66",
          "VK_BACK_QUOTE", "192",
          "VK_BACK_SLASH", "92",
          "VK_BACK_SPACE", "8",
          "VK_BEGIN", "65368",
          "VK_BRACELEFT", "161",
          "VK_BRACERIGHT", "162",
          "VK_C", "67",
          "VK_CANCEL", "3",
          "VK_CAPS_LOCK", "20",
          "VK_CIRCUMFLEX", "514",
          "VK_CLEAR", "12",
          "VK_CLOSE_BRACKET", "93",
          "VK_CODE_INPUT", "258",
          "VK_COLON", "513",
          "VK_COMMA", "44",
          "VK_COMPOSE", "65312",
          "VK_CONTEXT_MENU", "525",
          "VK_CONTROL", "17",
          "VK_CONVERT", "28",
          "VK_COPY", "65485",
          "VK_CUT", "65489",
          "VK_D", "68",
          "VK_DEAD_ABOVEDOT", "134",
          "VK_DEAD_ABOVERING", "136",
          "VK_DEAD_ACUTE", "129",
          "VK_DEAD_BREVE", "133",
          "VK_DEAD_CARON", "138",
          "VK_DEAD_CEDILLA", "139",
          "VK_DEAD_CIRCUMFLEX", "130",
          "VK_DEAD_DIAERESIS", "135",
          "VK_DEAD_DOUBLEACUTE", "137",
          "VK_DEAD_GRAVE", "128",
          "VK_DEAD_IOTA", "141",
          "VK_DEAD_MACRON", "132",
          "VK_DEAD_OGONEK", "140",
          "VK_DEAD_SEMIVOICED_SOUND", "143",
          "VK_DEAD_TILDE", "131",
          "VK_DEAD_VOICED_SOUND", "142",
          "VK_DECIMAL", "110",
          "VK_DELETE", "127",
          "VK_DIVIDE", "111",
          "VK_DOLLAR", "515",
          "VK_DOWN", "40",
          "VK_E", "69",
          "VK_END", "35",
          "VK_ENTER", "10",
          "VK_EQUALS", "61",
          "VK_ESCAPE", "27",
          "VK_EURO_SIGN", "516",
          "VK_EXCLAMATION_MARK", "517",
          "VK_F", "70",
          "VK_F1", "112",
          "VK_F10", "121",
          "VK_F11", "122",
          "VK_F12", "123",
          "VK_F13", "61440",
          "VK_F14", "61441",
          "VK_F15", "61442",
          "VK_F16", "61443",
          "VK_F17", "61444",
          "VK_F18", "61445",
          "VK_F19", "61446",
          "VK_F2", "113",
          "VK_F20", "61447",
          "VK_F21", "61448",
          "VK_F22", "61449",
          "VK_F23", "61450",
          "VK_F24", "61451",
          "VK_F3", "114",
          "VK_F4", "115",
          "VK_F5", "116",
          "VK_F6", "117",
          "VK_F7", "118",
          "VK_F8", "119",
          "VK_F9", "120",
          "VK_FINAL", "24",
          "VK_FIND", "65488",
          "VK_FULL_WIDTH", "243",
          "VK_G", "71",
          "VK_GREATER", "160",
          "VK_H", "72",
          "VK_HALF_WIDTH", "244",
          "VK_HELP", "156",
          "VK_HIRAGANA", "242",
          "VK_HOME", "36",
          "VK_I", "73",
          "VK_INPUT_METHOD_ON_OFF", "263",
          "VK_INSERT", "155",
          "VK_INVERTED_EXCLAMATION_MARK", "518",
          "VK_J", "74",
          "VK_JAPANESE_HIRAGANA", "260",
          "VK_JAPANESE_KATAKANA", "259",
          "VK_JAPANESE_ROMAN", "261",
          "VK_K", "75",
          "VK_KANA", "21",
          "VK_KANA_LOCK", "262",
          "VK_KANJI", "25",
          "VK_KATAKANA", "241",
          "VK_KP_DOWN", "225",
          "VK_KP_LEFT", "226",
          "VK_KP_RIGHT", "227",
          "VK_KP_UP", "224",
          "VK_L", "76",
          "VK_LEFT", "37",
          "VK_LEFT_PARENTHESIS", "519",
          "VK_LESS", "153",
          "VK_M", "77",
          "VK_META", "157",
          "VK_MINUS", "45",
          "VK_MODECHANGE", "31",
          "VK_MULTIPLY", "106",
          "VK_N", "78",
          "VK_NONCONVERT", "29",
          "VK_NUM_LOCK", "144",
          "VK_NUMBER_SIGN", "520",
          "VK_NUMPAD0", "96",
          "VK_NUMPAD1", "97",
          "VK_NUMPAD2", "98",
          "VK_NUMPAD3", "99",
          "VK_NUMPAD4", "100",
          "VK_NUMPAD5", "101",
          "VK_NUMPAD6", "102",
          "VK_NUMPAD7", "103",
          "VK_NUMPAD8", "104",
          "VK_NUMPAD9", "105",
          "VK_O", "79",
          "VK_OPEN_BRACKET", "91",
          "VK_P", "80",
          "VK_PAGE_DOWN", "34",
          "VK_PAGE_UP", "33",
          "VK_PASTE", "65487",
          "VK_PAUSE", "19",
          "VK_PERIOD", "46",
          "VK_PLUS", "521",
          "VK_PREVIOUS_CANDIDATE", "257",
          "VK_PRINTSCREEN", "154",
          "VK_PROPS", "65482",
          "VK_Q", "81",
          "VK_QUOTE", "222",
          "VK_QUOTEDBL", "152",
          "VK_R", "82",
          "VK_RIGHT", "39",
          "VK_RIGHT_PARENTHESIS", "522",
          "VK_ROMAN_CHARACTERS", "245",
          "VK_S", "83",
          "VK_SCROLL_LOCK", "145",
          "VK_SEMICOLON", "59",
          "VK_SEPARATER", "108",
          "VK_SEPARATOR", "108",
          "VK_SHIFT", "16",
          "VK_SLASH", "47",
          "VK_SPACE", "32",
          "VK_STOP", "65480",
          "VK_SUBTRACT", "109",
          "VK_T", "84",
          "VK_TAB", "9",
          "VK_U", "85",
          "VK_UNDEFINED", "0",
          "VK_UNDERSCORE", "523",
          "VK_UNDO", "65483",
          "VK_UP", "38",
          "VK_V", "86",
          "VK_W", "87",
          "VK_WINDOWS", "524",
          "VK_X", "88",
          "VK_Y", "89",
          "VK_Z", "90"
          //</editor-fold>
  };

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

  private static Map<String, Integer> keyNames = new HashMap<String, Integer>();
  private static Map<Integer, String> keyCodes = new HashMap<Integer, String>();
  private static Map<String, String> modifierNames = new HashMap<String, String>();
  private static Map<Integer, String> keys = new HashMap<Integer, String>();
  private static Map<String, Integer> keyTexts = new HashMap<String, Integer>();

  static {
    //<editor-fold defaultstate="collapsed" desc="create the keyname map used with write()">
    String sKey;
    keyMaxLength = 0;
    for (char c = cMin; c < cMax; c++) {
      sKey = toJavaKeyCodeText(c);
      if (!sKey.equals("" + c)) {
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
      //log.trace("Key: %s is: %s", k, KeyEvent.getKeyText(keyTexts.getAll(k)));
      if (k.length() < 4) {
        continue;
      }
      keys.put(keyTexts.get(k), k);
    }
    //</editor-fold>

    //<editor-fold desc="key/modifier names according to AWT-KeyStroke">
    keyNames = getKeyNames();
    for (String key : keyNames.keySet()) {
      keyCodes.put(keyNames.get(key), key);
    }
    modifierNames.put("CTRL", "ctrl");
    modifierNames.put("CONTROL", "ctrl");
    modifierNames.put(CTRL, "ctrl");
    modifierNames.put("#C", "ctrl");
    modifierNames.put("ALT", "alt");
    modifierNames.put("#A", "alt");
    modifierNames.put(ALT, "alt");
    modifierNames.put("SHIFT", "shift");
    modifierNames.put("#S", "shift");
    modifierNames.put(SHIFT, "shift");
    modifierNames.put(META, "meta");
    modifierNames.put("#M", "meta");
    modifierNames.put("META", "meta");
    modifierNames.put("CMD", "meta");
    modifierNames.put("COMMAND", "meta");
    modifierNames.put(CMD, "meta");
    modifierNames.put("WIN", "meta");
    modifierNames.put("WINDOWS", "meta");
    modifierNames.put(WIN, "meta");
    modifierNames.put("ALTGR", "altGraph");
    modifierNames.put("ALTGRAPH", "altGraph");
    modifierNames.put(ALTGR, "altGraph");
    //</editor-fold>
    for (int i = 0; i < keyVK.length; i += 2) {
      namesVK.put(Integer.decode(keyVK[i + 1]), keyVK[i].substring(3));
    }
  }

  //<editor-fold defaultstate="collapsed" desc="support functions for write()">
  public static String getTextFromKeycode(int key) {
    return keys.get(key);
  }

  public static boolean isRepeatable(String token) {
    int key = toJavaKeyCodeFromText(token);
    switch (key) {
      case KeyEvent.VK_UP:
        return true;
      case KeyEvent.VK_DOWN:
        return true;
      case KeyEvent.VK_RIGHT:
        return true;
      case KeyEvent.VK_LEFT:
        return true;
      case -KeyEvent.VK_TAB:
        return true;
      case KeyEvent.VK_TAB:
        return true;
      case KeyEvent.VK_ENTER:
        return true;
      case KeyEvent.VK_BACK_SPACE:
        return true;
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

  public static Map<String, Integer> getKeyNames() {
    Map<String, Integer> keyNames = new HashMap<>();
    for (int i = 0; i < keyVK.length; i += 2) {
      keyNames.put(keyVK[i].substring(3), Integer.decode(keyVK[i + 1]));
    }
    Map<String, Integer> sortedKeyNames = new TreeMap<>(keyNames);
    return sortedKeyNames;
  }

  public static String getModifierName(String key) {
    String modifier = modifierNames.get(key);
    if (isSet(modifier)) {
      return modifier;
    }
    modifier = modifierNames.get(key.toUpperCase());
    if (isSet(modifier)) {
      return modifier;
    }
    return "";
  }

  public static String getModifierNames(int mod) {
    return "";
  }

  public static String getKeyName(String key) {
    Integer keyCode = keyNames.get(key.toUpperCase());
    if (isNotNull(keyCode)) {
      return key.toUpperCase();
    }
    if (isKeyConstant(key)) {
      int code = toJavaKeyCode(key)[0];
      key = keyCodes.get(code);
      if (isSet(key)) {
        return key;
      }
    }
    return "";
  }

  public static String getKeyName(int key) {
    String keyName = namesVK.get(key);
    if (null == keyName) {
      return "";
    }
    return keyName;
  }

  private static boolean isKeyConstant(String key) {
    Character character = key.charAt(0);
    return character >= '\ue000' && character < '\ue050';
  }
  //</editor-fold>

  public static int[] toJavaKeyCode(String key) {
    if (key.length() > 0) {
      return toJavaKeyCode(key.charAt(0));
    }
    return null;
  }

  public static int[] toJavaKeyCode(char key) {
    switch (key) {
//Lowercase
      case 'a':
        return new int[]{KeyEvent.VK_A};
      case 'b':
        return new int[]{KeyEvent.VK_B};
      case 'c':
        return new int[]{KeyEvent.VK_C};
      case 'd':
        return new int[]{KeyEvent.VK_D};
      case 'e':
        return new int[]{KeyEvent.VK_E};
      case 'f':
        return new int[]{KeyEvent.VK_F};
      case 'g':
        return new int[]{KeyEvent.VK_G};
      case 'h':
        return new int[]{KeyEvent.VK_H};
      case 'i':
        return new int[]{KeyEvent.VK_I};
      case 'j':
        return new int[]{KeyEvent.VK_J};
      case 'k':
        return new int[]{KeyEvent.VK_K};
      case 'l':
        return new int[]{KeyEvent.VK_L};
      case 'm':
        return new int[]{KeyEvent.VK_M};
      case 'n':
        return new int[]{KeyEvent.VK_N};
      case 'o':
        return new int[]{KeyEvent.VK_O};
      case 'p':
        return new int[]{KeyEvent.VK_P};
      case 'q':
        return new int[]{KeyEvent.VK_Q};
      case 'r':
        return new int[]{KeyEvent.VK_R};
      case 's':
        return new int[]{KeyEvent.VK_S};
      case 't':
        return new int[]{KeyEvent.VK_T};
      case 'u':
        return new int[]{KeyEvent.VK_U};
      case 'v':
        return new int[]{KeyEvent.VK_V};
      case 'w':
        return new int[]{KeyEvent.VK_W};
      case 'x':
        return new int[]{KeyEvent.VK_X};
      case 'y':
        return new int[]{KeyEvent.VK_Y};
      case 'z':
        return new int[]{KeyEvent.VK_Z};
//Uppercase
      case 'A':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_A};
      case 'B':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_B};
      case 'C':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_C};
      case 'D':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_D};
      case 'E':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_E};
      case 'F':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_F};
      case 'G':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_G};
      case 'H':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_H};
      case 'I':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_I};
      case 'J':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_J};
      case 'K':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_K};
      case 'L':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_L};
      case 'M':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_M};
      case 'N':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_N};
      case 'O':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_O};
      case 'P':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_P};
      case 'Q':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Q};
      case 'R':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_R};
      case 'S':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_S};
      case 'T':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_T};
      case 'U':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_U};
      case 'V':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_V};
      case 'W':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_W};
      case 'X':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_X};
      case 'Y':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Y};
      case 'Z':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_Z};
//Row 3 (below function keys)
//      case '§': return new int[]{192}; //not producable
      case '1':
        return new int[]{KeyEvent.VK_1};
      case '!':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_1};
      case '2':
        return new int[]{KeyEvent.VK_2};
      case '@':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_2};
      case '3':
        return new int[]{KeyEvent.VK_3};
      case '#':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_3};
      case '4':
        return new int[]{KeyEvent.VK_4};
      case '$':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_4};
      case '5':
        return new int[]{KeyEvent.VK_5};
      case '%':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_5};
      case '6':
        return new int[]{KeyEvent.VK_6};
      case '^':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_6};
      case '7':
        return new int[]{KeyEvent.VK_7};
      case '&':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_7};
      case '8':
        return new int[]{KeyEvent.VK_8};
      case '*':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_8};
      case '9':
        return new int[]{KeyEvent.VK_9};
      case '(':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_9};
      case '0':
        return new int[]{KeyEvent.VK_0};
      case ')':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_0};
      case '-':
        return new int[]{KeyEvent.VK_MINUS};
      case '_':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS};
      case '=':
        return new int[]{KeyEvent.VK_EQUALS};
      case '+':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS};
//Row 2
// q w e r t y u i o p
      case '[':
        return new int[]{KeyEvent.VK_OPEN_BRACKET};
      case '{':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET};
      case ']':
        return new int[]{KeyEvent.VK_CLOSE_BRACKET};
      case '}':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET};
//Row 1
// a s d f g h j k l
      case ';':
        return new int[]{KeyEvent.VK_SEMICOLON};
      case ':':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON};
      case '\'':
        return new int[]{KeyEvent.VK_QUOTE};
      case '"':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE};
      case '\\':
        return new int[]{KeyEvent.VK_BACK_SLASH};
      case '|':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH};
//RETURN, BACKSPACE, TAB
      case '\b':
        return new int[]{KeyEvent.VK_BACK_SPACE};
      case '\t':
        return new int[]{KeyEvent.VK_TAB};
      case '\r':
        return new int[]{KeyEvent.VK_ENTER};
      case '\n':
        return new int[]{KeyEvent.VK_ENTER};
//SPACE
      case ' ':
        return new int[]{KeyEvent.VK_SPACE};
//Row 0 (first above SPACE)
      case '`':
        return new int[]{KeyEvent.VK_BACK_QUOTE};
      case '~':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE};
// z x c v b n m
      case ',':
        return new int[]{KeyEvent.VK_COMMA};
      case '<':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA};
      case '.':
        return new int[]{KeyEvent.VK_PERIOD};
      case '>':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD};
      case '/':
        return new int[]{KeyEvent.VK_SLASH};
      case '?':
        return new int[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH};
//Modifier
      case Keys.C_SHIFT:
        return new int[]{KeyEvent.VK_SHIFT};
      case Keys.C_CTRL:
        return new int[]{KeyEvent.VK_CONTROL};
      case Keys.C_ALT:
        return new int[]{KeyEvent.VK_ALT};
      case Keys.C_META:
        return new int[]{KeyEvent.VK_META};
//Cursor movement
      case Keys.C_UP:
        return new int[]{KeyEvent.VK_UP};
      case Keys.C_RIGHT:
        return new int[]{KeyEvent.VK_RIGHT};
      case Keys.C_DOWN:
        return new int[]{KeyEvent.VK_DOWN};
      case Keys.C_LEFT:
        return new int[]{KeyEvent.VK_LEFT};
      case Keys.C_PAGE_UP:
        return new int[]{KeyEvent.VK_PAGE_UP};
      case Keys.C_PAGE_DOWN:
        return new int[]{KeyEvent.VK_PAGE_DOWN};
      case Keys.C_END:
        return new int[]{KeyEvent.VK_END};
      case Keys.C_HOME:
        return new int[]{KeyEvent.VK_HOME};
      case Keys.C_DELETE:
        return new int[]{KeyEvent.VK_DELETE};
//Function keys
      case Keys.C_ESC:
        return new int[]{KeyEvent.VK_ESCAPE};
      case Keys.C_F1:
        return new int[]{KeyEvent.VK_F1};
      case Keys.C_F2:
        return new int[]{KeyEvent.VK_F2};
      case Keys.C_F3:
        return new int[]{KeyEvent.VK_F3};
      case Keys.C_F4:
        return new int[]{KeyEvent.VK_F4};
      case Keys.C_F5:
        return new int[]{KeyEvent.VK_F5};
      case Keys.C_F6:
        return new int[]{KeyEvent.VK_F6};
      case Keys.C_F7:
        return new int[]{KeyEvent.VK_F7};
      case Keys.C_F8:
        return new int[]{KeyEvent.VK_F8};
      case Keys.C_F9:
        return new int[]{KeyEvent.VK_F9};
      case Keys.C_F10:
        return new int[]{KeyEvent.VK_F10};
      case Keys.C_F11:
        return new int[]{KeyEvent.VK_F11};
      case Keys.C_F12:
        return new int[]{KeyEvent.VK_F12};
      case Keys.C_F13:
        return new int[]{KeyEvent.VK_F13};
      case Keys.C_F14:
        return new int[]{KeyEvent.VK_F14};
      case Keys.C_F15:
        return new int[]{KeyEvent.VK_F15};
//Toggling kezs
      case Keys.C_SCROLL_LOCK:
        return new int[]{KeyEvent.VK_SCROLL_LOCK};
      case Keys.C_NUM_LOCK:
        return new int[]{KeyEvent.VK_NUM_LOCK};
      case Keys.C_CAPS_LOCK:
        return new int[]{KeyEvent.VK_CAPS_LOCK};
      case Keys.C_INSERT:
        return new int[]{KeyEvent.VK_INSERT};
//Windows special
      case Keys.C_PAUSE:
        return new int[]{KeyEvent.VK_PAUSE};
      case Keys.C_PRINTSCREEN:
        return new int[]{KeyEvent.VK_PRINTSCREEN};
//Num pad
      case Keys.C_NUM0:
        return new int[]{KeyEvent.VK_NUMPAD0};
      case Keys.C_NUM1:
        return new int[]{KeyEvent.VK_NUMPAD1};
      case Keys.C_NUM2:
        return new int[]{KeyEvent.VK_NUMPAD2};
      case Keys.C_NUM3:
        return new int[]{KeyEvent.VK_NUMPAD3};
      case Keys.C_NUM4:
        return new int[]{KeyEvent.VK_NUMPAD4};
      case Keys.C_NUM5:
        return new int[]{KeyEvent.VK_NUMPAD5};
      case Keys.C_NUM6:
        return new int[]{KeyEvent.VK_NUMPAD6};
      case Keys.C_NUM7:
        return new int[]{KeyEvent.VK_NUMPAD7};
      case Keys.C_NUM8:
        return new int[]{KeyEvent.VK_NUMPAD8};
      case Keys.C_NUM9:
        return new int[]{KeyEvent.VK_NUMPAD9};
//Num pad special
      case Keys.C_SEPARATOR:
        return new int[]{KeyEvent.VK_SEPARATOR};
      case Keys.C_ADD:
        return new int[]{KeyEvent.VK_ADD};
      case Keys.C_MINUS:
        return new int[]{KeyEvent.VK_SUBTRACT};
      case Keys.C_MULTIPLY:
        return new int[]{KeyEvent.VK_MULTIPLY};
      case Keys.C_DIVIDE:
        return new int[]{KeyEvent.VK_DIVIDE};
      case Keys.C_DECIMAL:
        return new int[]{KeyEvent.VK_DECIMAL};
      case Keys.C_CONTEXT:
        return new int[]{KeyEvent.VK_CONTEXT_MENU};
      case Keys.C_WIN:
        return new int[]{KeyEvent.VK_WINDOWS};
//hack: alternative tab in GUI
      case Keys.C_NEXT:
        return new int[]{-KeyEvent.VK_TAB};

      default:
        throw new IllegalArgumentException("Key: Not supported character: " + key);
    }
  }

  public static String toJavaKeyCodeText(char key) {
    switch (key) {
//RETURN, BACKSPACE, TAB
      case '\b':
        return "#BACK.";
      case '\t':
        return "#TAB.";
      case C_NEXT:
        return "#TAB.";
      case '\r':
        return "#ENTER.";
      case '\n':
        return "#ENTER.";
//Cursor movement
      case Keys.C_UP:
        return "#UP.";
      case Keys.C_RIGHT:
        return "#RIGHT.";
      case Keys.C_DOWN:
        return "#DOWN.";
      case Keys.C_LEFT:
        return "#LEFT.";
      case Keys.C_PAGE_UP:
        return "#PUP.";
      case Keys.C_PAGE_DOWN:
        return "#PDOWN.";
      case Keys.C_END:
        return "#END.";
      case Keys.C_HOME:
        return "#HOME.";
      case Keys.C_DELETE:
        return "#DEL.";
//Function keys
      case Keys.C_ESC:
        return "#ESC.";
      case Keys.C_F1:
        return "#F1.";
      case Keys.C_F2:
        return "#F2.";
      case Keys.C_F3:
        return "#F3.";
      case Keys.C_F4:
        return "#F4.";
      case Keys.C_F5:
        return "#F5.";
      case Keys.C_F6:
        return "#F6.";
      case Keys.C_F7:
        return "#F7.";
      case Keys.C_F8:
        return "#F8.";
      case Keys.C_F9:
        return "#F9.";
      case Keys.C_F10:
        return "#F10.";
      case Keys.C_F11:
        return "#F11.";
      case Keys.C_F12:
        return "#F12.";
      case Keys.C_F13:
        return "#F13.";
      case Keys.C_F14:
        return "#F14.";
      case Keys.C_F15:
        return "#F15.";
//Toggling kezs
      case Keys.C_SCROLL_LOCK:
        return "#SCROLL_LOCK.";
      case Keys.C_NUM_LOCK:
        return "#NUM_LOCK.";
      case Keys.C_CAPS_LOCK:
        return "#CAPS_LOCK.";
      case Keys.C_INSERT:
        return "#INS.";
//Windows special
      case Keys.C_PAUSE:
        return "#PAUSE.";
      case Keys.C_PRINTSCREEN:
        return "#PRINTSCREEN.";
      case Keys.C_WIN:
        return "#WIN.";
//Num pad
      case Keys.C_NUM0:
        return "#NUM0.";
      case Keys.C_NUM1:
        return "#NUM1.";
      case Keys.C_NUM2:
        return "#NUM2.";
      case Keys.C_NUM3:
        return "#NUM3.";
      case Keys.C_NUM4:
        return "#NUM4.";
      case Keys.C_NUM5:
        return "#NUM5.";
      case Keys.C_NUM6:
        return "#NUM6.";
      case Keys.C_NUM7:
        return "#NUM7.";
      case Keys.C_NUM8:
        return "#NUM8.";
      case Keys.C_NUM9:
        return "#NUM9.";
//Num pad special
      case Keys.C_SEPARATOR:
        return "#NSEP.";
      case Keys.C_ADD:
        return "#NADD.";
      case Keys.C_MINUS:
        return "#NSUB.";
      case Keys.C_MULTIPLY:
        return "#NMUL";
      case Keys.C_DIVIDE:
        return "#NDIV.";
      case Keys.C_DECIMAL:
        return "#NDEC.";
      case Keys.C_CONTEXT:
        return "#NCON.";
//KeyModifiers
      case Keys.C_SHIFT:
        return "#SHIFT.";
      case Keys.C_CTRL:
        return "#CTRL.";
      case Keys.C_ALT:
        return "#ALT.";
      case Keys.C_META:
        return "#META.";

      default:
        return "" + key;
    }
  }

  protected static int convertModifiers(String mod) {
    int modNew = 0;
    char key;
    for (int i = 0; i < mod.length(); i++) {
      key = mod.charAt(i);
      if (Keys.C_CTRL == key) {
        modNew |= Modifier.CTRL;
      } else if (Keys.C_ALT == key) {
        modNew |= Modifier.ALT;
      } else if (Keys.C_SHIFT == key) {
        modNew |= Modifier.SHIFT;
      } else if (Keys.C_CMD == key) {
        modNew |= Modifier.CMD;
      } else if (Keys.C_META == key) {
        modNew |= Modifier.META;
      } else if (Keys.C_ALTGR == key) {
        modNew |= Modifier.ALTGR;
      } else if (Keys.C_WIN == key) {
        modNew |= Modifier.WIN;
      }
    }
    return modNew;
  }

  static boolean isNull(Object object) {
    return object == null;
  }

  static boolean isNotNull(Object object) {
    return object != null;
  }

  static boolean isSet(Object object) {
    if (object != null) {
      if (object instanceof String && !((String) object).isEmpty()) {
        return true;
      }
    }
    return false;
  }

  static boolean isNotSet(Object object) {
    return !isSet(object);
  }

  public class Modifier {
    public static final int CTRL = InputEvent.CTRL_DOWN_MASK;
    public static final int SHIFT = InputEvent.SHIFT_DOWN_MASK;
    public static final int ALT = InputEvent.ALT_DOWN_MASK;
    public static final int ALTGR = InputEvent.ALT_GRAPH_DOWN_MASK;
    public static final int META = InputEvent.META_DOWN_MASK;
    public static final int CMD = InputEvent.META_DOWN_MASK;
    public static final int WIN = InputEvent.META_DOWN_MASK;

    @Deprecated
    public static final int KEY_CTRL = InputEvent.CTRL_MASK;
    @Deprecated
    public static final int KEY_SHIFT = SHIFT;
    @Deprecated
    public static final int KEY_ALT = ALT;
    @Deprecated
    public static final int KEY_META = META;
    @Deprecated
    public static final int KEY_CMD = META;
    @Deprecated
    public static final int KEY_WIN = META;
  }
}
