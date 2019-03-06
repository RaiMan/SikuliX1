package org.sikuli.script;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.sikuli.basics.Settings;
import org.sikuli.natives.SXUser32;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinDef.HWND;

public class KeyboardLayout {
  private static final int DEFAULT_KEYBOARD_LAYOUT_ID = 0x0409; // en-US;
  private static final Map<Character, int[]> DEFAULT_KEYBOARD_LAYOUT = buildAwtEnUs(); // en-US;

  private static final Map<Integer, Map<Character, int[]>> LAYOUTS = new HashMap<>();
  
  private static final SXUser32 sxuser32 = SXUser32.INSTANCE;

  private enum VkCode {
    // Numbers
    VK_1(KeyEvent.VK_1), VK_2(KeyEvent.VK_2), VK_3(KeyEvent.VK_3), VK_4(KeyEvent.VK_4), VK_5(KeyEvent.VK_5),
    VK_6(KeyEvent.VK_6), VK_7(KeyEvent.VK_7), VK_8(KeyEvent.VK_8), VK_9(KeyEvent.VK_9), VK_0(KeyEvent.VK_0),

    // ASCII
    VK_A(KeyEvent.VK_A), VK_B(KeyEvent.VK_B), VK_C(KeyEvent.VK_C), VK_D(KeyEvent.VK_D), VK_E(KeyEvent.VK_E),
    VK_F(KeyEvent.VK_F), VK_G(KeyEvent.VK_G), VK_H(KeyEvent.VK_H), VK_I(KeyEvent.VK_I), VK_J(KeyEvent.VK_J),
    VK_K(KeyEvent.VK_K), VK_L(KeyEvent.VK_L), VK_M(KeyEvent.VK_M), VK_N(KeyEvent.VK_N), VK_O(KeyEvent.VK_O),
    VK_P(KeyEvent.VK_P), VK_Q(KeyEvent.VK_Q), VK_R(KeyEvent.VK_R), VK_S(KeyEvent.VK_S), VK_T(KeyEvent.VK_T),
    VK_U(KeyEvent.VK_U), VK_V(KeyEvent.VK_V), VK_W(KeyEvent.VK_W), VK_X(KeyEvent.VK_X), VK_Y(KeyEvent.VK_Y),
    VK_Z(KeyEvent.VK_Z),

    // OEM Codes
    VK_OEM_1(0xBA), // OEM_1 (: ),)
    VK_OEM_102(0xE2), // OEM_102 (> <)
    VK_OEM_2(0xBF), // OEM_2 (? /)
    VK_OEM_3(0xC0), // OEM_3 (~ `)
    VK_OEM_4(0xDB), // OEM_4 ({ [)
    VK_OEM_5(0xDC), // OEM_5 (| \)
    VK_OEM_6(0xDD), // OEM_6 (} ])
    VK_OEM_7(0xDE), // OEM_7 (" ')
    VK_OEM_8(0xDF), // OEM_8 (§ !)

    VK_OEM_ATTN(0xF0), // Oem Attn
    VK_OEM_AUTO(0xF3), // Auto
    VK_OEM_AX(0xE1), // Ax
    VK_OEM_BACKTAB(0xF5), // Back Tab
    VK_OEM_CLEAR(0xFE), // OemClr
    VK_OEM_COMMA(0xBC), // OEM_COMMA (< ,)
    VK_OEM_COPY(0xF2), // Copy
    VK_OEM_CUSEL(0xEF), // Cu Sel
    VK_OEM_ENLW(0xF4), // Enlw
    VK_OEM_FINISH(0xF1), // Finish
    VK_OEM_FJ_LOYA(0x95), // Loya
    VK_OEM_FJ_MASSHOU(0x93), // Mashu
    VK_OEM_FJ_ROYA(0x96), // Roya
    VK_OEM_FJ_TOUROKU(0x94), // Touroku
    VK_OEM_JUMP(0xEA), // Jump
    VK_OEM_MINUS(0xBD), // OEM_MINUS (_ -)
    VK_OEM_PA1(0xEB), // OemPa1
    VK_OEM_PA2(0xEC), // OemPa2
    VK_OEM_PA3(0xED), // OemPa3
    VK_OEM_PERIOD(0xBE), // OEM_PERIOD (> .)
    VK_OEM_PLUS(0xBB), // OEM_PLUS (+ =)
    VK_OEM_RESET(0xE9), // Reset
    VK_OEM_WSCTR(0xEE); // WsCtrl

    private int code;

    private VkCode(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

  private static final int VK_RETURN = 0x0D; // Enter

  private static final int MAPVK_VK_TO_VSC = 0;

  private static Map<Character, int[]> mapKeyCodes(int[] modifiers, int keyboarLayoutId) {
    Map<Character, int[]> mappings = new HashMap<>();

    int spaceScanCode = sxuser32.MapVirtualKeyExW(KeyEvent.VK_SPACE, MAPVK_VK_TO_VSC, keyboarLayoutId);

    for (VkCode vk : VkCode.values()) {
      byte[] keyStates = new byte[256];

      for (int modifier : modifiers) {
        keyStates[modifier] |= 0x80;
      }

      keyStates[vk.getCode()] |= 0x80;

      int scanCode = sxuser32.MapVirtualKeyExW(vk.getCode(), MAPVK_VK_TO_VSC, keyboarLayoutId);

      char[] buff = new char[1];

      int ret = sxuser32.ToUnicodeEx(vk.getCode(), scanCode, keyStates, buff, 1, 0, keyboarLayoutId);

      int[] codes = Arrays.copyOf(modifiers, modifiers.length + 1);
      codes[modifiers.length] = vk.getCode();

      if (ret > 0) {
        mappings.put(buff[0], codes);
      } else if (ret < 0) {
        // Get DEAD key
        keyStates = new byte[256];
        keyStates[KeyEvent.VK_SPACE] |= 0x80;
        ret = sxuser32.ToUnicodeEx(KeyEvent.VK_SPACE, spaceScanCode, keyStates, buff, 1, 0, keyboarLayoutId);
        if (ret > 0) {
          mappings.put(buff[0], codes);
        }
      }
    }

    return mappings;
  }

  private static Map<Character, int[]> buildWindowsLayout(int keyboarLayoutId) {
    Map<Character, int[]> layout = new HashMap<>();

    layout.putAll(mapKeyCodes(new int[] { KeyEvent.VK_CONTROL, KeyEvent.VK_ALT }, keyboarLayoutId));
    layout.putAll(mapKeyCodes(new int[] { KeyEvent.VK_SHIFT }, keyboarLayoutId));
    layout.putAll(mapKeyCodes(new int[0], keyboarLayoutId));
    layout.put('\r', new int[] { VK_RETURN });
    layout.put('\n', new int[] { VK_RETURN });
    layout.putAll(buildCommon());

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
//	      layout.put('§', new int[]{192}); //not producable
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
    layout.put('\r', new int[] { KeyEvent.VK_ENTER });
    layout.put('\n', new int[] { KeyEvent.VK_ENTER });

    layout.putAll(buildCommon());

    return layout;
  }

  private static Map<Character, int[]> buildCommon() {
    Map<Character, int[]> layout = new HashMap<>();
    // Modifier
    layout.put(Key.C_SHIFT, new int[] { KeyEvent.VK_SHIFT });
    layout.put(Key.C_CTRL, new int[] { KeyEvent.VK_CONTROL });
    layout.put(Key.C_ALT, new int[] { KeyEvent.VK_ALT });
    layout.put(Key.C_META, new int[] { KeyEvent.VK_META });
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
    layout.put('\b', new int[] { KeyEvent.VK_BACK_SPACE });
    layout.put('\t', new int[] { KeyEvent.VK_TAB });
    // SPACE
    layout.put(' ', new int[] { KeyEvent.VK_SPACE });

    return layout;
  }
  
  private static Map<Character, int[]> getCurrentLayout(){
    Map<Character, int[]> layout = DEFAULT_KEYBOARD_LAYOUT;

    if (Settings.AutoDetectKeyboardLayout && Settings.isWindows()) {
          int keyboarLayoutId = DEFAULT_KEYBOARD_LAYOUT_ID;
      HWND hwnd = sxuser32.GetForegroundWindow();
      if (hwnd != null) {
          int threadID = sxuser32.GetWindowThreadProcessId(hwnd, null);               
          keyboarLayoutId = sxuser32.GetKeyboardLayout(threadID);       
      }
                  
      layout = LAYOUTS.get(keyboarLayoutId);
      
      if (layout == null) {
        layout = buildWindowsLayout(keyboarLayoutId);
        LAYOUTS.put(keyboarLayoutId, layout);
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
}