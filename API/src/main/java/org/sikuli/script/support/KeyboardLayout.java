/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sikuli.basics.Settings;
import org.sikuli.natives.SXUser32;

import com.sun.jna.platform.win32.WinDef.HKL;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.sikuli.script.Key;
import org.sikuli.script.runners.ProcessRunner;

public class KeyboardLayout {
  private static final int DEFAULT_KEYBOARD_LAYOUT_ID = 0x0409; // en-US;
  private static final Map<Character, int[]> DEFAULT_KEYBOARD_LAYOUT = buildAwtEnUs(); // en-US;

  public static Map<Character, int[]> getAwtEnUS() {
    return DEFAULT_KEYBOARD_LAYOUT;
  }

  private static final Map<Integer, Map<Character, int[]>> LAYOUTS = new HashMap<>();

  class WindowsVkCodes {
    public static final int VK_SHIFT = 0x10; // SHIFT key
    public static final int VK_CONTROL = 0x11; // CTRL key
    public static final int VK_MENU = 0x12; // ALT key
    public static final int VK_ABNT_C1 = 0xC1; // Abnt C1
    public static final int VK_ABNT_C2 = 0xC2; // Abnt C2
    public static final int VK_ADD = 0x6B; // Numpad +
    public static final int VK_ATTN = 0xF6; // Attn
    public static final int VK_BACK = 0x08; // Backspace
    public static final int VK_CANCEL = 0x03; // Break
    public static final int VK_CLEAR = 0x0C; // Clear
    public static final int VK_CRSEL = 0xF7; // Cr Sel
    public static final int VK_DECIMAL = 0x6E; // Numpad .
    public static final int VK_DIVIDE = 0x6F; // Numpad /
    public static final int VK_EREOF = 0xF9; // Er Eof
    public static final int VK_ESCAPE = 0x1B; // Esc
    public static final int VK_EXECUTE = 0x2B; // Execute
    public static final int VK_EXSEL = 0xF8; // Ex Sel
    public static final int VK_ICO_CLEAR = 0xE6; // IcoClr
    public static final int VK_ICO_HELP = 0xE3; // IcoHlp
    public static final int VK_KEY_0 = 0x30; // = '0') 0
    public static final int VK_KEY_1 = 0x31; // = '1') 1
    public static final int VK_KEY_2 = 0x32; // = '2') 2
    public static final int VK_KEY_3 = 0x33; // = '3') 3
    public static final int VK_KEY_4 = 0x34; // = '4') 4
    public static final int VK_KEY_5 = 0x35; // = '5') 5
    public static final int VK_KEY_6 = 0x36; // = '6') 6
    public static final int VK_KEY_7 = 0x37; // = '7') 7
    public static final int VK_KEY_8 = 0x38; // = '8') 8
    public static final int VK_KEY_9 = 0x39; // = '9') 9
    public static final int VK_KEY_A = 0x41; // = 'A') A
    public static final int VK_KEY_B = 0x42; // = 'B') B
    public static final int VK_KEY_C = 0x43; // = 'C') C
    public static final int VK_KEY_D = 0x44; // = 'D') D
    public static final int VK_KEY_E = 0x45; // = 'E') E
    public static final int VK_KEY_F = 0x46; // = 'F') F
    public static final int VK_KEY_G = 0x47; // = 'G') G
    public static final int VK_KEY_H = 0x48; // = 'H') H
    public static final int VK_KEY_I = 0x49; // = 'I') I
    public static final int VK_KEY_J = 0x4A; // = 'J') J
    public static final int VK_KEY_K = 0x4B; // = 'K') K
    public static final int VK_KEY_L = 0x4C; // = 'L') L
    public static final int VK_KEY_M = 0x4D; // = 'M') M
    public static final int VK_KEY_N = 0x4E; // = 'N') N
    public static final int VK_KEY_O = 0x4F; // = 'O') O
    public static final int VK_KEY_P = 0x50; // = 'P') P
    public static final int VK_KEY_Q = 0x51; // = 'Q') Q
    public static final int VK_KEY_R = 0x52; // = 'R') R
    public static final int VK_KEY_S = 0x53; // = 'S') S
    public static final int VK_KEY_T = 0x54; // = 'T') T
    public static final int VK_KEY_U = 0x55; // = 'U') U
    public static final int VK_KEY_V = 0x56; // = 'V') V
    public static final int VK_KEY_W = 0x57; // = 'W') W
    public static final int VK_KEY_X = 0x58; // = 'X') X
    public static final int VK_KEY_Y = 0x59; // = 'Y') Y
    public static final int VK_KEY_Z = 0x5A; // = 'Z') Z
    public static final int VK_MULTIPLY = 0x6A; // Numpad *
    public static final int VK_NONAME = 0xFC; // NoName
    public static final int VK_NUMPAD0 = 0x60; // Numpad 0
    public static final int VK_NUMPAD1 = 0x61; // Numpad 1
    public static final int VK_NUMPAD2 = 0x62; // Numpad 2
    public static final int VK_NUMPAD3 = 0x63; // Numpad 3
    public static final int VK_NUMPAD4 = 0x64; // Numpad 4
    public static final int VK_NUMPAD5 = 0x65; // Numpad 5
    public static final int VK_NUMPAD6 = 0x66; // Numpad 6
    public static final int VK_NUMPAD7 = 0x67; // Numpad 7
    public static final int VK_NUMPAD8 = 0x68; // Numpad 8
    public static final int VK_NUMPAD9 = 0x69; // Numpad 9
    public static final int VK_OEM_1 = 0xBA; // OEM_1 = : ;)
    public static final int VK_OEM_102 = 0xE2; // OEM_102 = > <)
    public static final int VK_OEM_2 = 0xBF; // OEM_2 = ? /)
    public static final int VK_OEM_3 = 0xC0; // OEM_3 = ~ `)
    public static final int VK_OEM_4 = 0xDB; // OEM_4 = { [)
    public static final int VK_OEM_5 = 0xDC; // OEM_5 = | \)
    public static final int VK_OEM_6 = 0xDD; // OEM_6 = } ])
    public static final int VK_OEM_7 = 0xDE; // OEM_7 = " ')
    public static final int VK_OEM_8 = 0xDF; // OEM_8 = § !)
    public static final int VK_OEM_ATTN = 0xF0; // Oem Attn
    public static final int VK_OEM_AUTO = 0xF3; // Auto
    public static final int VK_OEM_AX = 0xE1; // Ax
    public static final int VK_OEM_BACKTAB = 0xF5; // Back Tab
    public static final int VK_OEM_CLEAR = 0xFE; // OemClr
    public static final int VK_OEM_COMMA = 0xBC; // OEM_COMMA = < ,)
    public static final int VK_OEM_COPY = 0xF2; // Copy
    public static final int VK_OEM_CUSEL = 0xEF; // Cu Sel
    public static final int VK_OEM_ENLW = 0xF4; // Enlw
    public static final int VK_OEM_FINISH = 0xF1; // Finish
    public static final int VK_OEM_FJ_LOYA = 0x95; // Loya
    public static final int VK_OEM_FJ_MASSHOU = 0x93; // Mashu
    public static final int VK_OEM_FJ_ROYA = 0x96; // Roya
    public static final int VK_OEM_FJ_TOUROKU = 0x94; // Touroku
    public static final int VK_OEM_JUMP = 0xEA; // Jump
    public static final int VK_OEM_MINUS = 0xBD; // OEM_MINUS = _ -)
    public static final int VK_OEM_PA1 = 0xEB; // OemPa1
    public static final int VK_OEM_PA2 = 0xEC; // OemPa2
    public static final int VK_OEM_PA3 = 0xED; // OemPa3
    public static final int VK_OEM_PERIOD = 0xBE; // OEM_PERIOD = > .)
    public static final int VK_OEM_PLUS = 0xBB; // OEM_PLUS = + =)
    public static final int VK_OEM_RESET = 0xE9; // Reset
    public static final int VK_OEM_WSCTRL = 0xEE; // WsCtrl
    public static final int VK_PA1 = 0xFD; // Pa1
    public static final int VK_PACKET = 0xE7; // Packet
    public static final int VK_PLAY = 0xFA; // Play
    public static final int VK_PROCESSKEY = 0xE5; // Process
    public static final int VK_RETURN = 0x0D; // Enter
    public static final int VK_SELECT = 0x29; // Select
    public static final int VK_SEPARATOR = 0x6C; // Separator
    public static final int VK_SPACE = 0x20; // Space
    public static final int VK_SUBTRACT = 0x6D; // Num -
    public static final int VK_TAB = 0x09; // Tab
    public static final int VK_ZOOM = 0xFB; // Zoom
    public static final int VK__none_ = 0xFF; // no VK mapping
    public static final int VK_ACCEPT = 0x1E; // Accept
    public static final int VK_APPS = 0x5D; // Context Menu
    public static final int VK_BROWSER_BACK = 0xA6; // Browser Back
    public static final int VK_BROWSER_FAVORITES = 0xAB; // Browser Favorites
    public static final int VK_BROWSER_FORWARD = 0xA7; // Browser Forward
    public static final int VK_BROWSER_HOME = 0xAC; // Browser Home
    public static final int VK_BROWSER_REFRESH = 0xA8; // Browser Refresh
    public static final int VK_BROWSER_SEARCH = 0xAA; // Browser Search
    public static final int VK_BROWSER_STOP = 0xA9; // Browser Stop
    public static final int VK_CAPITAL = 0x14; // Caps Lock
    public static final int VK_CONVERT = 0x1C; // Convert
    public static final int VK_DELETE = 0x2E; // Delete
    public static final int VK_DOWN = 0x28; // Arrow Down
    public static final int VK_END = 0x23; // End
    public static final int VK_F1 = 0x70; // F1
    public static final int VK_F10 = 0x79; // F10
    public static final int VK_F11 = 0x7A; // F11
    public static final int VK_F12 = 0x7B; // F12
    public static final int VK_F13 = 0x7C; // F13
    public static final int VK_F14 = 0x7D; // F14
    public static final int VK_F15 = 0x7E; // F15
    public static final int VK_F16 = 0x7F; // F16
    public static final int VK_F17 = 0x80; // F17
    public static final int VK_F18 = 0x81; // F18
    public static final int VK_F19 = 0x82; // F19
    public static final int VK_F2 = 0x71; // F2
    public static final int VK_F20 = 0x83; // F20
    public static final int VK_F21 = 0x84; // F21
    public static final int VK_F22 = 0x85; // F22
    public static final int VK_F23 = 0x86; // F23
    public static final int VK_F24 = 0x87; // F24
    public static final int VK_F3 = 0x72; // F3
    public static final int VK_F4 = 0x73; // F4
    public static final int VK_F5 = 0x74; // F5
    public static final int VK_F6 = 0x75; // F6
    public static final int VK_F7 = 0x76; // F7
    public static final int VK_F8 = 0x77; // F8
    public static final int VK_F9 = 0x78; // F9
    public static final int VK_FINAL = 0x18; // Final
    public static final int VK_HELP = 0x2F; // Help
    public static final int VK_HOME = 0x24; // Home
    public static final int VK_ICO_00 = 0xE4; // Ico00 *
    public static final int VK_INSERT = 0x2D; // Insert
    public static final int VK_JUNJA = 0x17; // Junja
    public static final int VK_KANA = 0x15; // Kana
    public static final int VK_KANJI = 0x19; // Kanji
    public static final int VK_LAUNCH_APP1 = 0xB6; // App1
    public static final int VK_LAUNCH_APP2 = 0xB7; // App2
    public static final int VK_LAUNCH_MAIL = 0xB4; // Mail
    public static final int VK_LAUNCH_MEDIA_SELECT = 0xB5; // Media
    public static final int VK_LBUTTON = 0x01; // Left Button **
    public static final int VK_LCONTROL = 0xA2; // Left Ctrl
    public static final int VK_LEFT = 0x25; // Arrow Left
    public static final int VK_LMENU = 0xA4; // Left Alt
    public static final int VK_LSHIFT = 0xA0; // Left Shift
    public static final int VK_LWIN = 0x5B; // Left Win
    public static final int VK_MBUTTON = 0x04; // Middle Button **
    public static final int VK_MEDIA_NEXT_TRACK = 0xB0; // Next Track
    public static final int VK_MEDIA_PLAY_PAUSE = 0xB3; // Play / Pause
    public static final int VK_MEDIA_PREV_TRACK = 0xB1; // Previous Track
    public static final int VK_MEDIA_STOP = 0xB2; // Stop
    public static final int VK_MODECHANGE = 0x1F; // Mode Change
    public static final int VK_NEXT = 0x22; // Page Down
    public static final int VK_NONCONVERT = 0x1D; // Non Convert
    public static final int VK_NUMLOCK = 0x90; // Num Lock
    public static final int VK_OEM_FJ_JISHO = 0x92; // Jisho
    public static final int VK_PAUSE = 0x13; // Pause
    public static final int VK_PRINT = 0x2A; // Print
    public static final int VK_PRIOR = 0x21; // Page Up
    public static final int VK_RBUTTON = 0x02; // Right Button **
    public static final int VK_RCONTROL = 0xA3; // Right Ctrl
    public static final int VK_RIGHT = 0x27; // Arrow Right
    public static final int VK_RMENU = 0xA5; // Right Alt
    public static final int VK_RSHIFT = 0xA1; // Right Shift
    public static final int VK_RWIN = 0x5C; // Right Win
    public static final int VK_SCROLL = 0x91; // Scrol Lock
    public static final int VK_SLEEP = 0x5F; // Sleep
    public static final int VK_SNAPSHOT = 0x2C; // Print Screen
    public static final int VK_UP = 0x26; // Arrow Up
    public static final int VK_VOLUME_DOWN = 0xAE; // Volume Down
    public static final int VK_VOLUME_MUTE = 0xAD; // Volume Mute
    public static final int VK_VOLUME_UP = 0xAF; // Volume Up
    public static final int VK_XBUTTON1 = 0x05; // X Button 1 **
    public static final int VK_XBUTTON2 = 0x06; // X Button 2 **
  }

  private static final int[] AUTO_DETECT_VK_CODES = new int[] {
//region AUTO_DETECT_VK_CODES
      // Numbers
      WindowsVkCodes.VK_KEY_1, WindowsVkCodes.VK_KEY_2, WindowsVkCodes.VK_KEY_3, WindowsVkCodes.VK_KEY_4,
      WindowsVkCodes.VK_KEY_5, WindowsVkCodes.VK_KEY_6, WindowsVkCodes.VK_KEY_7, WindowsVkCodes.VK_KEY_8,
      WindowsVkCodes.VK_KEY_9, WindowsVkCodes.VK_KEY_0,

      // ASCII
      WindowsVkCodes.VK_KEY_A, WindowsVkCodes.VK_KEY_B, WindowsVkCodes.VK_KEY_C, WindowsVkCodes.VK_KEY_D,
      WindowsVkCodes.VK_KEY_E, WindowsVkCodes.VK_KEY_F, WindowsVkCodes.VK_KEY_G, WindowsVkCodes.VK_KEY_H,
      WindowsVkCodes.VK_KEY_I, WindowsVkCodes.VK_KEY_J, WindowsVkCodes.VK_KEY_K, WindowsVkCodes.VK_KEY_L,
      WindowsVkCodes.VK_KEY_M, WindowsVkCodes.VK_KEY_N, WindowsVkCodes.VK_KEY_O, WindowsVkCodes.VK_KEY_P,
      WindowsVkCodes.VK_KEY_Q, WindowsVkCodes.VK_KEY_R, WindowsVkCodes.VK_KEY_S, WindowsVkCodes.VK_KEY_T,
      WindowsVkCodes.VK_KEY_U, WindowsVkCodes.VK_KEY_V, WindowsVkCodes.VK_KEY_W, WindowsVkCodes.VK_KEY_X,
      WindowsVkCodes.VK_KEY_Y, WindowsVkCodes.VK_KEY_Z,

      // OEM Codes
      WindowsVkCodes.VK_OEM_1, // OEM_1 ,)
      WindowsVkCodes.VK_OEM_102, // OEM_102
      WindowsVkCodes.VK_OEM_2, // OEM_2
      WindowsVkCodes.VK_OEM_3, // OEM_3
      WindowsVkCodes.VK_OEM_4, // OEM_4
      WindowsVkCodes.VK_OEM_5, // OEM_5
      WindowsVkCodes.VK_OEM_6, // OEM_6
      WindowsVkCodes.VK_OEM_7, // OEM_7
      WindowsVkCodes.VK_OEM_8, // OEM_8

      WindowsVkCodes.VK_OEM_ATTN, // Oem Attn
      WindowsVkCodes.VK_OEM_AUTO, // Auto
      WindowsVkCodes.VK_OEM_AX, // Ax
      WindowsVkCodes.VK_OEM_BACKTAB, // Back Tab
      WindowsVkCodes.VK_OEM_CLEAR, // OemClr
      WindowsVkCodes.VK_OEM_COMMA, // OEM_COMMA
      WindowsVkCodes.VK_OEM_COPY, // Copy
      WindowsVkCodes.VK_OEM_CUSEL, // Cu Sel
      WindowsVkCodes.VK_OEM_ENLW, // Enlw
      WindowsVkCodes.VK_OEM_FINISH, // Finish
      WindowsVkCodes.VK_OEM_FJ_LOYA, // Loya
      WindowsVkCodes.VK_OEM_FJ_MASSHOU, // Mashu
      WindowsVkCodes.VK_OEM_FJ_ROYA, // Roya
      WindowsVkCodes.VK_OEM_FJ_TOUROKU, // Touroku
      WindowsVkCodes.VK_OEM_JUMP, // Jump
      WindowsVkCodes.VK_OEM_MINUS, // OEM_MINUS
      WindowsVkCodes.VK_OEM_PA1, // OemPa1
      WindowsVkCodes.VK_OEM_PA2, // OemPa2
      WindowsVkCodes.VK_OEM_PA3, // OemPa3
      WindowsVkCodes.VK_OEM_PERIOD, // OEM_PERIOD
      WindowsVkCodes.VK_OEM_PLUS, // OEM_PLUS
      WindowsVkCodes.VK_OEM_RESET, // Reset
      WindowsVkCodes.VK_OEM_WSCTRL // WsCtrl
  };
  //endregion

  private static Map<Character, int[]> mapKeyCodes(int[] modifiers, int keyboarLayoutId) {
    final int MAPVK_VK_TO_VSC = 0;
    Map<Character, int[]> mappings = new HashMap<>();

    int spaceScanCode = SXUser32.INSTANCE.MapVirtualKeyExW(WindowsVkCodes.VK_SPACE, MAPVK_VK_TO_VSC, keyboarLayoutId);

    for (int vk : AUTO_DETECT_VK_CODES) {
      byte[] keyStates = new byte[256];

      for (int modifier : modifiers) {
        keyStates[modifier] |= 0x80;
      }

      keyStates[vk] |= 0x80;

      int scanCode = SXUser32.INSTANCE.MapVirtualKeyExW(vk, MAPVK_VK_TO_VSC, keyboarLayoutId);

      char[] buff = new char[1];

      int ret = SXUser32.INSTANCE.ToUnicodeEx(vk, scanCode, keyStates, buff, 1, 0, keyboarLayoutId);

      int[] codes = Arrays.copyOf(modifiers, modifiers.length + 1);
      codes[modifiers.length] = vk;

      if (ret > 0) {
        mappings.put(buff[0], codes);
      } else if (ret < 0) {
        // Get DEAD key
        keyStates = new byte[256];
        keyStates[WindowsVkCodes.VK_SPACE] |= 0x80;
        ret = SXUser32.INSTANCE.ToUnicodeEx(WindowsVkCodes.VK_SPACE, spaceScanCode, keyStates, buff, 1, 0, keyboarLayoutId);
        if (ret > 0) {
          mappings.put(buff[0], codes);
        }
      }
    }

    return mappings;
  }

  private static Map<Character, int[]> buildWindowsLayout(int keyboarLayoutId) {
    Map<Character, int[]> layout = new HashMap<>();

    layout.putAll(mapKeyCodes(new int[] { WindowsVkCodes.VK_CONTROL, WindowsVkCodes.VK_MENU }, keyboarLayoutId));
    layout.putAll(mapKeyCodes(new int[] { WindowsVkCodes.VK_SHIFT }, keyboarLayoutId));
    layout.putAll(mapKeyCodes(new int[0], keyboarLayoutId));

    // Modifier
    layout.put(Key.C_SHIFT, new int[] { WindowsVkCodes.VK_SHIFT });
    layout.put(Key.C_CTRL, new int[] { WindowsVkCodes.VK_CONTROL });
    layout.put(Key.C_ALT, new int[] { WindowsVkCodes.VK_MENU });
    layout.put(Key.C_ALTGR, new int[] { WindowsVkCodes.VK_CONTROL, WindowsVkCodes.VK_MENU });
    layout.put(Key.C_META, new int[] { WindowsVkCodes.VK_LWIN });
    // Cursor movement
    layout.put(Key.C_UP, new int[] { WindowsVkCodes.VK_UP});
    layout.put(Key.C_RIGHT, new int[] { WindowsVkCodes.VK_RIGHT });
    layout.put(Key.C_DOWN, new int[] { WindowsVkCodes.VK_DOWN });
    layout.put(Key.C_LEFT, new int[] { WindowsVkCodes.VK_LEFT });
    layout.put(Key.C_PAGE_UP, new int[] { WindowsVkCodes.VK_PRIOR });
    layout.put(Key.C_PAGE_DOWN, new int[] { WindowsVkCodes.VK_NEXT});
    layout.put(Key.C_END, new int[] { WindowsVkCodes.VK_END });
    layout.put(Key.C_HOME, new int[] { WindowsVkCodes.VK_HOME });
    layout.put(Key.C_DELETE, new int[] { WindowsVkCodes.VK_DELETE });
    // Function keys
    layout.put(Key.C_ESC, new int[] { WindowsVkCodes.VK_ESCAPE });
    layout.put(Key.C_F1, new int[] { WindowsVkCodes.VK_F1 });
    layout.put(Key.C_F2, new int[] { WindowsVkCodes.VK_F2 });
    layout.put(Key.C_F3, new int[] { WindowsVkCodes.VK_F3 });
    layout.put(Key.C_F4, new int[] { WindowsVkCodes.VK_F4 });
    layout.put(Key.C_F5, new int[] { WindowsVkCodes.VK_F5 });
    layout.put(Key.C_F6, new int[] { WindowsVkCodes.VK_F6 });
    layout.put(Key.C_F7, new int[] { WindowsVkCodes.VK_F7 });
    layout.put(Key.C_F8, new int[] { WindowsVkCodes.VK_F8 });
    layout.put(Key.C_F9, new int[] { WindowsVkCodes.VK_F9 });
    layout.put(Key.C_F10, new int[] { WindowsVkCodes.VK_F10 });
    layout.put(Key.C_F11, new int[] { WindowsVkCodes.VK_F11 });
    layout.put(Key.C_F12, new int[] { WindowsVkCodes.VK_F12 });
    layout.put(Key.C_F13, new int[] { WindowsVkCodes.VK_F13 });
    layout.put(Key.C_F14, new int[] { WindowsVkCodes.VK_F14 });
    layout.put(Key.C_F15, new int[] { WindowsVkCodes.VK_F15 });
    // Toggling keys
    layout.put(Key.C_SCROLL_LOCK, new int[] { WindowsVkCodes.VK_SCROLL });
    layout.put(Key.C_NUM_LOCK, new int[] { WindowsVkCodes.VK_NUMLOCK });
    layout.put(Key.C_CAPS_LOCK, new int[] { WindowsVkCodes.VK_CAPITAL });
    layout.put(Key.C_INSERT, new int[] { WindowsVkCodes.VK_INSERT });
    // Windows special
    layout.put(Key.C_PAUSE, new int[] { WindowsVkCodes.VK_PAUSE });
    layout.put(Key.C_PRINTSCREEN, new int[] { WindowsVkCodes.VK_SNAPSHOT });
    // Num pad
    layout.put(Key.C_NUM0, new int[] { WindowsVkCodes.VK_NUMPAD0 });
    layout.put(Key.C_NUM1, new int[] { WindowsVkCodes.VK_NUMPAD1 });
    layout.put(Key.C_NUM2, new int[] { WindowsVkCodes.VK_NUMPAD2 });
    layout.put(Key.C_NUM3, new int[] { WindowsVkCodes.VK_NUMPAD3 });
    layout.put(Key.C_NUM4, new int[] { WindowsVkCodes.VK_NUMPAD4 });
    layout.put(Key.C_NUM5, new int[] { WindowsVkCodes.VK_NUMPAD5 });
    layout.put(Key.C_NUM6, new int[] { WindowsVkCodes.VK_NUMPAD6 });
    layout.put(Key.C_NUM7, new int[] { WindowsVkCodes.VK_NUMPAD7 });
    layout.put(Key.C_NUM8, new int[] { WindowsVkCodes.VK_NUMPAD8 });
    layout.put(Key.C_NUM9, new int[] { WindowsVkCodes.VK_NUMPAD9 });
    // Num pad special
    layout.put(Key.C_SEPARATOR, new int[] { WindowsVkCodes.VK_SEPARATOR });
    layout.put(Key.C_ADD, new int[] { WindowsVkCodes.VK_ADD });
    layout.put(Key.C_MINUS, new int[] { WindowsVkCodes.VK_SUBTRACT });
    layout.put(Key.C_MULTIPLY, new int[] { WindowsVkCodes.VK_MULTIPLY });
    layout.put(Key.C_DIVIDE, new int[] { WindowsVkCodes.VK_DIVIDE });
    layout.put(Key.C_DECIMAL, new int[] { WindowsVkCodes.VK_DECIMAL });
    layout.put(Key.C_CONTEXT, new int[] { WindowsVkCodes.VK_APPS });
    layout.put(Key.C_WIN, new int[] { WindowsVkCodes.VK_LWIN });
    // hack: alternative tab in GUI
    layout.put(Key.C_NEXT, new int[] { -WindowsVkCodes.VK_TAB });
    // RETURN, BACKSPACE, TAB
    layout.put('\r', new int[] { WindowsVkCodes.VK_RETURN });
    layout.put('\n', new int[] { WindowsVkCodes.VK_RETURN });
    layout.put('\b', new int[] { WindowsVkCodes.VK_BACK });
    layout.put('\t', new int[] { WindowsVkCodes.VK_TAB });
    // SPACE
    layout.put(' ', new int[] { WindowsVkCodes.VK_SPACE });

    return layout;
  }

  // build default en-US layout for platforms where we can't detect layout
  // automatically and use AWT Robot to type keys
  private static Map<Character, int[]> buildAwtEnUs() {
    Map<Character, int[]> layout = new HashMap<>();

    layout.put('a', new int[] { KeyEvent.VK_A });
    layout.put('b', new int[] { KeyEvent.VK_B });
    layout.put('c', new int[] { KeyEvent.VK_C });
    layout.put('d', new int[] { KeyEvent.VK_D });
    layout.put('e', new int[] { KeyEvent.VK_E });
    layout.put('f', new int[] { KeyEvent.VK_F });
    layout.put('g', new int[] { KeyEvent.VK_G });
    layout.put('h', new int[] { KeyEvent.VK_H });
    layout.put('i', new int[] { KeyEvent.VK_I });
    layout.put('j', new int[] { KeyEvent.VK_J });
    layout.put('k', new int[] { KeyEvent.VK_K });
    layout.put('l', new int[] { KeyEvent.VK_L });
    layout.put('m', new int[] { KeyEvent.VK_M });
    layout.put('n', new int[] { KeyEvent.VK_N });
    layout.put('o', new int[] { KeyEvent.VK_O });
    layout.put('p', new int[] { KeyEvent.VK_P });
    layout.put('q', new int[] { KeyEvent.VK_Q });
    layout.put('r', new int[] { KeyEvent.VK_R });
    layout.put('s', new int[] { KeyEvent.VK_S });
    layout.put('t', new int[] { KeyEvent.VK_T });
    layout.put('u', new int[] { KeyEvent.VK_U });
    layout.put('v', new int[] { KeyEvent.VK_V });
    layout.put('w', new int[] { KeyEvent.VK_W });
    layout.put('x', new int[] { KeyEvent.VK_X });
    layout.put('y', new int[] { KeyEvent.VK_Y });
    layout.put('z', new int[] { KeyEvent.VK_Z });
    // Uppercase
    layout.put('A', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_A });
    layout.put('B', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_B });
    layout.put('C', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_C });
    layout.put('D', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_D });
    layout.put('E', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_E });
    layout.put('F', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_F });
    layout.put('G', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_G });
    layout.put('H', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_H });
    layout.put('I', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_I });
    layout.put('J', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_J });
    layout.put('K', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_K });
    layout.put('L', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_L });
    layout.put('M', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_M });
    layout.put('N', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_N });
    layout.put('O', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_O });
    layout.put('P', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_P });
    layout.put('Q', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Q });
    layout.put('R', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_R });
    layout.put('S', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_S });
    layout.put('T', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_T });
    layout.put('U', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_U });
    layout.put('V', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_V });
    layout.put('W', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_W });
    layout.put('X', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_X });
    layout.put('Y', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Y });
    layout.put('Z', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Z });
    // Row 3 (below function keys)
    layout.put('1', new int[] { KeyEvent.VK_1 });
    layout.put('!', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_1 });
    layout.put('2', new int[] { KeyEvent.VK_2 });
    layout.put('@', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_2 });
    layout.put('3', new int[] { KeyEvent.VK_3 });
    layout.put('#', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_3 });
    layout.put('4', new int[] { KeyEvent.VK_4 });
    layout.put('$', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_4 });
    layout.put('5', new int[] { KeyEvent.VK_5 });
    layout.put('%', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 });
    layout.put('6', new int[] { KeyEvent.VK_6 });
    layout.put('^', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_6 });
    layout.put('7', new int[] { KeyEvent.VK_7 });
    layout.put('&', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_7 });
    layout.put('8', new int[] { KeyEvent.VK_8 });
    layout.put('*', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_8 });
    layout.put('9', new int[] { KeyEvent.VK_9 });
    layout.put('(', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_9 });
    layout.put('0', new int[] { KeyEvent.VK_0 });
    layout.put(')', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_0 });
    layout.put('-', new int[] { KeyEvent.VK_MINUS });
    layout.put('_', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS });
    layout.put('=', new int[] { KeyEvent.VK_EQUALS });
    layout.put('+', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS });
    // Row 2
    // q w e r t y u i o p
    layout.put('[', new int[] { KeyEvent.VK_OPEN_BRACKET });
    layout.put('{', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET });
    layout.put(']', new int[] { KeyEvent.VK_CLOSE_BRACKET });
    layout.put('}', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET });
    // Row 1
    // a s d f g h j k l
    layout.put(';', new int[] { KeyEvent.VK_SEMICOLON });
    layout.put(':', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON });
    layout.put('\'', new int[] { KeyEvent.VK_QUOTE });
    layout.put('"', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE });
    layout.put('\\', new int[] { KeyEvent.VK_BACK_SLASH });
    layout.put('|', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH });
    // RETURN, BACKSPACE, TAB
    layout.put('\b', new int[] { KeyEvent.VK_BACK_SPACE });
    layout.put('\t', new int[] { KeyEvent.VK_TAB });
    layout.put('\r', new int[] { KeyEvent.VK_ENTER });
    layout.put('\n', new int[] { KeyEvent.VK_ENTER });
    // SPACE
    layout.put(' ', new int[] { KeyEvent.VK_SPACE });
    // Row 0 (first above SPACE)
    layout.put('`', new int[] { KeyEvent.VK_BACK_QUOTE });
    layout.put('~', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE });
    // z x c v b n m
    layout.put(',', new int[] { KeyEvent.VK_COMMA });
    layout.put('<', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA });
    layout.put('.', new int[] { KeyEvent.VK_PERIOD });
    layout.put('>', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD });
    layout.put('/', new int[] { KeyEvent.VK_SLASH });
    layout.put('?', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH });

    // Modifier
    layout.put(Key.C_SHIFT, new int[] { KeyEvent.VK_SHIFT });
    layout.put(Key.C_CTRL, new int[] { KeyEvent.VK_CONTROL });
    layout.put(Key.C_ALT, new int[] { KeyEvent.VK_ALT });
    layout.put(Key.C_META, new int[] { KeyEvent.VK_META });
    layout.put(Key.C_ALTGR, new int[] { KeyEvent.VK_ALT_GRAPH });
    // Cursor movement
    layout.put(Key.C_UP, new int[] { KeyEvent.VK_UP });
    layout.put(Key.C_RIGHT, new int[] { KeyEvent.VK_RIGHT });
    layout.put(Key.C_DOWN, new int[] { KeyEvent.VK_DOWN });
    layout.put(Key.C_LEFT, new int[] { KeyEvent.VK_LEFT });
    layout.put(Key.C_PAGE_UP, new int[] { KeyEvent.VK_PAGE_UP });
    layout.put(Key.C_PAGE_DOWN, new int[] { KeyEvent.VK_PAGE_DOWN });
    layout.put(Key.C_END, new int[] { KeyEvent.VK_END });
    layout.put(Key.C_HOME, new int[] { KeyEvent.VK_HOME });
    layout.put(Key.C_DELETE, new int[] { KeyEvent.VK_DELETE });
    // Function keys
    layout.put(Key.C_ESC, new int[] { KeyEvent.VK_ESCAPE });
    layout.put(Key.C_F1, new int[] { KeyEvent.VK_F1 });
    layout.put(Key.C_F2, new int[] { KeyEvent.VK_F2 });
    layout.put(Key.C_F3, new int[] { KeyEvent.VK_F3 });
    layout.put(Key.C_F4, new int[] { KeyEvent.VK_F4 });
    layout.put(Key.C_F5, new int[] { KeyEvent.VK_F5 });
    layout.put(Key.C_F6, new int[] { KeyEvent.VK_F6 });
    layout.put(Key.C_F7, new int[] { KeyEvent.VK_F7 });
    layout.put(Key.C_F8, new int[] { KeyEvent.VK_F8 });
    layout.put(Key.C_F9, new int[] { KeyEvent.VK_F9 });
    layout.put(Key.C_F10, new int[] { KeyEvent.VK_F10 });
    layout.put(Key.C_F11, new int[] { KeyEvent.VK_F11 });
    layout.put(Key.C_F12, new int[] { KeyEvent.VK_F12 });
    layout.put(Key.C_F13, new int[] { KeyEvent.VK_F13 });
    layout.put(Key.C_F14, new int[] { KeyEvent.VK_F14 });
    layout.put(Key.C_F15, new int[] { KeyEvent.VK_F15 });
    // Toggling kezs
    layout.put(Key.C_SCROLL_LOCK, new int[] { KeyEvent.VK_SCROLL_LOCK });
    layout.put(Key.C_NUM_LOCK, new int[] { KeyEvent.VK_NUM_LOCK });
    layout.put(Key.C_CAPS_LOCK, new int[] { KeyEvent.VK_CAPS_LOCK });
    layout.put(Key.C_INSERT, new int[] { KeyEvent.VK_INSERT });
    // Windows special
    layout.put(Key.C_PAUSE, new int[] { KeyEvent.VK_PAUSE });
    layout.put(Key.C_PRINTSCREEN, new int[] { KeyEvent.VK_PRINTSCREEN });
    // Num pad
    layout.put(Key.C_NUM0, new int[] { KeyEvent.VK_NUMPAD0 });
    layout.put(Key.C_NUM1, new int[] { KeyEvent.VK_NUMPAD1 });
    layout.put(Key.C_NUM2, new int[] { KeyEvent.VK_NUMPAD2 });
    layout.put(Key.C_NUM3, new int[] { KeyEvent.VK_NUMPAD3 });
    layout.put(Key.C_NUM4, new int[] { KeyEvent.VK_NUMPAD4 });
    layout.put(Key.C_NUM5, new int[] { KeyEvent.VK_NUMPAD5 });
    layout.put(Key.C_NUM6, new int[] { KeyEvent.VK_NUMPAD6 });
    layout.put(Key.C_NUM7, new int[] { KeyEvent.VK_NUMPAD7 });
    layout.put(Key.C_NUM8, new int[] { KeyEvent.VK_NUMPAD8 });
    layout.put(Key.C_NUM9, new int[] { KeyEvent.VK_NUMPAD9 });
    // Num pad special
    layout.put(Key.C_SEPARATOR, new int[] { KeyEvent.VK_SEPARATOR });
    layout.put(Key.C_ADD, new int[] { KeyEvent.VK_ADD });
    layout.put(Key.C_MINUS, new int[] { KeyEvent.VK_SUBTRACT });
    layout.put(Key.C_MULTIPLY, new int[] { KeyEvent.VK_MULTIPLY });
    layout.put(Key.C_DIVIDE, new int[] { KeyEvent.VK_DIVIDE });
    layout.put(Key.C_DECIMAL, new int[] { KeyEvent.VK_DECIMAL });
    layout.put(Key.C_CONTEXT, new int[] { KeyEvent.VK_CONTEXT_MENU });
    layout.put(Key.C_WIN, new int[] { KeyEvent.VK_WINDOWS });
    // hack: alternative tab in GUI
    layout.put(Key.C_NEXT, new int[] { -KeyEvent.VK_TAB });
    // RETURN, BACKSPACE, TAB
    layout.put('\r', new int[] { KeyEvent.VK_ENTER });
    layout.put('\n', new int[] { KeyEvent.VK_ENTER });
    layout.put('\b', new int[] { KeyEvent.VK_BACK_SPACE });
    layout.put('\t', new int[] { KeyEvent.VK_TAB });
    // SPACE
    layout.put(' ', new int[] { KeyEvent.VK_SPACE });

    return layout;
  }

  private static Map<Character, int[]> getCurrentLayout() {
    Map<Character, int[]> layout = DEFAULT_KEYBOARD_LAYOUT;

    if (Settings.AutoDetectKeyboardLayout && Commons.runningWindows()) {
      int keyboarLayoutId = DEFAULT_KEYBOARD_LAYOUT_ID;
      HWND hwnd = SXUser32.INSTANCE.GetForegroundWindow();
      if (hwnd != null) {
        int threadID = SXUser32.INSTANCE.GetWindowThreadProcessId(hwnd, null);
        HKL keyboardLayoutHKL = SXUser32.INSTANCE.GetKeyboardLayout(threadID);
        if (keyboardLayoutHKL != null) {
          keyboarLayoutId = keyboardLayoutHKL.getLanguageIdentifier();
        }
      }

      synchronized(LAYOUTS) {
        layout = LAYOUTS.get(keyboarLayoutId);

        if (layout == null) {
          layout = buildWindowsLayout(keyboarLayoutId);
          LAYOUTS.put(keyboarLayoutId, layout);
        }
      }
    }
    return layout;
  }

  public static int[] toJavaKeyCode(char c) {
    int[] keyCodes = getCurrentLayout().get(c);
    if (keyCodes == null) {
      throw new IllegalArgumentException("Key: Not supported character: " + c);
    }
    return keyCodes;
  }

  static File keyboardChanger = null;
  static boolean hasUnicodeKeyboard = false;
  static String kbCurrent = null;
  static Map<String, String> kbs = new HashMap<>();
  static String KEYLAYOUT = "com.apple.keylayout.";
  static String UNI_HEX_INP = "UnicodeHexInput";

  //region Mac-only: eval and change keyboard
  public static void changeKeyboard(String... kbNew) {
    if (!Commons.runningMac()) {
      return;
    }
    if (kbs.size() == 0) {
      evalKeyboards();
    }
    if (kbNew.length == 0) {
      if (kbCurrent == null) {
        return;
      }
      kbNew = new String[]{kbCurrent};
    }
    String kbName = kbs.get(kbNew[0]);
    if (kbName == null) {
      return;
    }
    try {
      ProcessRunner.runCommand(keyboardChanger.getAbsolutePath(), kbName);
    } catch (Exception e) {
    }
  }

  static void evalKeyboards() {
    if (!Commons.runningMac()) {
      return;
    }
    if (keyboardChanger == null) {
      keyboardChanger = new File(Commons.getLibsFolder(), "keyboard");
      if (!keyboardChanger.exists()) {
        Commons.loadOpenCV();
      }
      if (!keyboardChanger.exists()) {
        Commons.terminate(999, "RobotDesktop::evalKeyboards: keyboardChanger not available");
      }
    }
    String kblines = "";
    try {
      kblines = ProcessRunner.runCommand(keyboardChanger.getAbsolutePath(), "list");
    } catch (Exception e) {
    }
    if (!kblines.isEmpty() && kblines.startsWith("success")) {
      List<String> kbList = Arrays.stream(kblines.substring("success ".length()).split("\n")).collect(Collectors.toList());
      kbCurrent = kbList.remove(kbList.size() - 1).split("\\(")[0].strip();
      kbCurrent = kbCurrent.replace(" ", "").replace(".", "");
      kbCurrent = kbCurrent.split("[^A-Za-z]")[0];
      for (String kb : kbList) {
        kb = kb.strip();
        if (kb.startsWith(KEYLAYOUT)) {
          if (kb.endsWith(UNI_HEX_INP)) {
            hasUnicodeKeyboard = true;
          }
          kbs.put(kb.substring(KEYLAYOUT.length()).split("[^A-Za-z]")[0], kb);
        }
      }
      if (!kbs.keySet().contains(kbCurrent)) {
        hasUnicodeKeyboard = false;
      }
    }
  }
  //endregion
}