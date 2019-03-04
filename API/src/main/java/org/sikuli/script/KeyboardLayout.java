package org.sikuli.script;

import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class KeyboardLayout {
  private static final Locale defaultLocale = new Locale("en", "US");
  
  private static final Map<Locale, Map<Character, int[]>> LAYOUTS = buildLayouts();
  
  private static final int VK_OEM_1 = 0xBA; //OEM_1 (: ;)
  private static final int VK_OEM_102 = 0xE2; //OEM_102 (> <)
  private static final int VK_OEM_2 = 0xBF; //OEM_2 (? /)
  private static final int VK_OEM_3 = 0xC0; //OEM_3 (~ `)
  private static final int VK_OEM_4 = 0xDB; //OEM_4 ({ [)
  private static final int VK_OEM_5 = 0xDC; //OEM_5 (| \)
  private static final int VK_OEM_6 = 0xDD; //OEM_6 (} ])
  private static final int VK_OEM_7 = 0xDE; //OEM_7 (" ')
  private static final int VK_OEM_8 = 0xDF; //OEM_8 (§ !)
  
  private static final int VK_OEM_ATTN = 0xF0; //Oem Attn
  private static final int VK_OEM_AUTO = 0xF3; //Auto
  private static final int VK_OEM_AX = 0xE1; //Ax
  private static final int VK_OEM_BACKTAB = 0xF5; //Back Tab
  private static final int VK_OEM_CLEAR = 0xFE; //OemClr
  private static final int VK_OEM_COMMA = 0xBC; //OEM_COMMA (< ,)
  private static final int VK_OEM_COPY = 0xF2; //Copy
  private static final int VK_OEM_CUSEL = 0xEF; //Cu Sel
  private static final int VK_OEM_ENLW = 0xF4; //Enlw
  private static final int VK_OEM_FINISH = 0xF1; //Finish
  private static final int VK_OEM_FJ_LOYA = 0x95; //Loya
  private static final int VK_OEM_FJ_MASSHOU = 0x93; //Mashu
  private static final int VK_OEM_FJ_ROYA = 0x96; //Roya
  private static final int VK_OEM_FJ_TOUROKU = 0x94; //Touroku
  private static final int  VK_OEM_JUMP = 0xEA; //Jump
  private static final int VK_OEM_MINUS = 0xBD; //OEM_MINUS (_ -)
  private static final int VK_OEM_PA1 = 0xEB; //OemPa1
  private static final int VK_OEM_PA2 = 0xEC; //OemPa2
  private static final int VK_OEM_PA3 = 0xED; //OemPa3
  private static final int VK_OEM_PERIOD = 0xBE; //OEM_PERIOD (> .)
  private static final int VK_OEM_PLUS = 0xBB; //OEM_PLUS (+ =)
  private static final int VK_OEM_RESET = 0xE9; //Reset
  private static final int VK_OEM_WSCTR = 0xEE; //WsCtrl
  
  private static final int VK_RMENU = 0xA5; //Right ALT
  private static final int VK_RETURN =  0x0D; //Ente

  private static Map<Locale, Map<Character, int[]>> buildLayouts() {
        
    Map<Locale, Map<Character, int[]>> layouts = new HashMap<>();

    layouts.put(defaultLocale, buildEnUs());
    layouts.put(new Locale("de", "CH"), buildDeCh());

    return layouts;
  }

  private static Map<Character, int[]> buildEnUs() {
    Map<Character, int[]> layout = new HashMap<>();

    layout.putAll(buildLatinLowercase());
    layout.putAll(buildLatinUppercase());
    layout.putAll(buildArabicNumbers());
    layout.putAll(buildCommonFunctional());

    // Row 3 (below function keys)
    // layout.put('§', new int[]{192}); //not producable
    layout.put('!', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_1 });
    layout.put('@', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_2 });
    layout.put('#', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_3 });
    layout.put('$', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_4 });
    layout.put('%', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 });
    layout.put('^', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_6 });
    layout.put('&', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_7 });
    layout.put('*', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_8 });
    layout.put('(', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_9 });
    layout.put(')', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_0 });
    layout.put('-', new int[] { VK_OEM_MINUS });
    layout.put('_', new int[] { KeyEvent.VK_SHIFT, VK_OEM_MINUS });
    layout.put('=', new int[] { VK_OEM_PLUS });
    layout.put('+', new int[] { KeyEvent.VK_SHIFT, VK_OEM_PLUS });
    // Row 2
    // q w e r t y u i o p
    layout.put('[', new int[] { VK_OEM_4});
    layout.put('{', new int[] { KeyEvent.VK_SHIFT, VK_OEM_4 });
    layout.put(']', new int[] { VK_OEM_6 });
    layout.put('}', new int[] { KeyEvent.VK_SHIFT, VK_OEM_6 });
    // Row 1
    // a s d f g h j k l
    layout.put(';', new int[] { VK_OEM_1 });
    layout.put(':', new int[] { KeyEvent.VK_SHIFT, VK_OEM_1 });
    layout.put('\'', new int[] { VK_OEM_7 });
    layout.put('"', new int[] { KeyEvent.VK_SHIFT, VK_OEM_7 });
    layout.put('\\', new int[] { VK_OEM_5 });
    layout.put('|', new int[] { KeyEvent.VK_SHIFT, VK_OEM_5 });
    // RETURN, BACKSPACE, TAB
    layout.put('\b', new int[] { KeyEvent.VK_BACK_SPACE });
    layout.put('\t', new int[] { KeyEvent.VK_TAB });
    layout.put('\r', new int[] { VK_RETURN });
    layout.put('\n', new int[] { VK_RETURN });
    // SPACE
    layout.put(' ', new int[] { KeyEvent.VK_SPACE });
    // Row 0 (first above SPACE)
    layout.put('`', new int[] { VK_OEM_3 });
    layout.put('~', new int[] { KeyEvent.VK_SHIFT, VK_OEM_3 });
    // z x c v b n m
    layout.put(',', new int[] { VK_OEM_COMMA });
    layout.put('<', new int[] { KeyEvent.VK_SHIFT, VK_OEM_COMMA });
    layout.put('.', new int[] { VK_OEM_PERIOD });
    layout.put('>', new int[] { KeyEvent.VK_SHIFT, VK_OEM_PERIOD });
    layout.put('/', new int[] { VK_OEM_2 });
    layout.put('?', new int[] { KeyEvent.VK_SHIFT, VK_OEM_2 });

    return layout;
  }

  private static Map<Character, int[]> buildDeCh() {
        
    Map<Character, int[]> layout = new HashMap<>();

    layout.putAll(buildLatinLowercase());
    layout.putAll(buildLatinUppercase());
    layout.putAll(buildArabicNumbers());
    layout.putAll(buildCommonFunctional());

    // Row 3 (below function keys)    
    layout.put('¨', new int[] { VK_OEM_3 });
    layout.put('!', new int[] { KeyEvent.VK_SHIFT, VK_OEM_3 });
    layout.put('@', new int[] { VK_RMENU, KeyEvent.VK_2 });
    layout.put('#', new int[] { VK_RMENU, KeyEvent.VK_3 });
    layout.put('$', new int[] { VK_OEM_8 });
    layout.put('£', new int[] { KeyEvent.VK_SHIFT, VK_OEM_8 });
    layout.put('%', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 });
    layout.put('^', new int[] { VK_OEM_6 });
    layout.put('&', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_6 });
    layout.put('*', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_3 });
    layout.put('(', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_8 });
    layout.put(')', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_9 });
    layout.put('-', new int[] { VK_OEM_MINUS });
    layout.put('_', new int[] { KeyEvent.VK_SHIFT, VK_OEM_MINUS });
    layout.put('=', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_0 });
    layout.put('+', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_1 });
    layout.put('§', new int[] { VK_OEM_2 });
    layout.put('°', new int[] { KeyEvent.VK_SHIFT, VK_OEM_2 });
    layout.put('€', new int[] { VK_RMENU, KeyEvent.VK_E });
    // Row 2
    // q w e r t y u i o p
                   
    layout.put('[', new int[] { VK_RMENU, VK_OEM_1 }); 
    layout.put('{', new int[] { VK_RMENU, VK_OEM_5 });
    layout.put(']', new int[] { VK_RMENU, VK_OEM_3 });
    layout.put('}', new int[] { VK_RMENU, VK_OEM_8 });
    // Row 1
    // a s d f g h j k l
    layout.put(';', new int[] { KeyEvent.VK_SHIFT, VK_OEM_COMMA });
    layout.put(':', new int[] { KeyEvent.VK_SHIFT, VK_OEM_PERIOD });
    layout.put('\'', new int[] { VK_OEM_4 });
    layout.put('"', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_2 });
    layout.put('\\', new int[] { VK_RMENU, VK_OEM_102});
    layout.put('¦', new int[] { VK_RMENU, KeyEvent.VK_1 });
    layout.put('|', new int[] { VK_RMENU, KeyEvent.VK_7 });
    // RETURN, BACKSPACE, TAB
    layout.put('\b', new int[] { KeyEvent.VK_BACK_SPACE });
    layout.put('\t', new int[] { KeyEvent.VK_TAB });
    layout.put('\r', new int[] { VK_RETURN });
    layout.put('\n', new int[] { VK_RETURN });
    // SPACE
    layout.put(' ', new int[] { KeyEvent.VK_SPACE });
    // Row 0 (first above SPACE)
    layout.put('`', new int[] { KeyEvent.VK_SHIFT, VK_OEM_6 });
    layout.put('~', new int[] { VK_RMENU, VK_OEM_6 });
    // z x c v b n m
    layout.put(',', new int[] { VK_OEM_COMMA });
    layout.put('<', new int[] { VK_OEM_102 });
    layout.put('.', new int[] { VK_OEM_PERIOD });
    layout.put('>', new int[] { KeyEvent.VK_SHIFT, VK_OEM_102 });
    layout.put('/', new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_7 });
    layout.put('?', new int[] { KeyEvent.VK_SHIFT, VK_OEM_4 });
    
    // Umlauts
    layout.put('ü', new int[] { VK_OEM_1 });
    layout.put('è', new int[] { KeyEvent.VK_SHIFT, VK_OEM_1 });
    layout.put('ö', new int[] { VK_OEM_7 });
    layout.put('é', new int[] { KeyEvent.VK_SHIFT, VK_OEM_7 });
    layout.put('ä', new int[] { VK_OEM_5 });
    layout.put('à', new int[] { KeyEvent.VK_SHIFT, VK_OEM_5 });
            
    return layout;
  }

  private static Map<Character, int[]> buildLatinLowercase() {
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
    return layout;
  }

  private static Map<Character, int[]> buildLatinUppercase() {
    Map<Character, int[]> layout = new HashMap<>();
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
    return layout;
  }

  private static Map<Character, int[]> buildArabicNumbers() {
    Map<Character, int[]> layout = new HashMap<>();
    layout.put('1', new int[] { KeyEvent.VK_1 });
    layout.put('2', new int[] { KeyEvent.VK_2 });
    layout.put('3', new int[] { KeyEvent.VK_3 });
    layout.put('4', new int[] { KeyEvent.VK_4 });
    layout.put('5', new int[] { KeyEvent.VK_5 });
    layout.put('6', new int[] { KeyEvent.VK_6 });
    layout.put('7', new int[] { KeyEvent.VK_7 });
    layout.put('8', new int[] { KeyEvent.VK_8 });
    layout.put('9', new int[] { KeyEvent.VK_9 });
    layout.put('0', new int[] { KeyEvent.VK_0 });
    return layout;
  }

  private static Map<Character, int[]> buildCommonFunctional() {
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
    return layout;
  }
  
  public static int[] toJavaKeyCode(char c) {              
    Map<Character, int[]> layout = LAYOUTS.get(InputContext.getInstance().getLocale());   
    
    if(layout == null){
      layout = LAYOUTS.get(defaultLocale);
    }
    
    int[] codes = layout.get(c);

    if (codes == null) {
      throw new IllegalArgumentException("Key: Not supported character: " + c);
    }

    return codes;
  }

}