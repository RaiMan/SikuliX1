/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.vnc;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.awt.event.KeyEvent.*;

class VNCRobot implements IRobot {
  public static final int VNC_POINTER_EVENT_BUTTON_1 = 1 << 0;
  public static final int VNC_POINTER_EVENT_BUTTON_2 = 1 << 1;
  public static final int VNC_POINTER_EVENT_BUTTON_3 = 1 << 2;
  public static final int VNC_POINTER_EVENT_BUTTON_4 = 1 << 3;
  public static final int VNC_POINTER_EVENT_BUTTON_5 = 1 << 4;
  public static final int VNC_POINTER_EVENT_BUTTON_6 = 1 << 5;
  public static final int VNC_POINTER_EVENT_BUTTON_7 = 1 << 6;
  public static final int VNC_POINTER_EVENT_BUTTON_8 = 1 << 7;

  private final VNCScreen screen;
  private int mouseX;
  private int mouseY;
  private int mouseButtons;
  private int autoDelay;
  private Set<Integer> pressedKeys;
  private boolean shiftPressed;

  public VNCRobot(VNCScreen screen) {
    this.screen = screen;
    this.autoDelay = 100;
    this.pressedKeys = new TreeSet<>();
  }

  @Override
  public ScreenImage captureScreen(Rectangle screenRect) {
    return screen.capture(screenRect);
  }

  @Override
  public boolean isRemote() {
    return true;
  }

  @Override
  public IScreen getScreen() {
    return screen;
  }

  @Override
  public void keyDown(String keys) {
    for (int i = 0; i < keys.length(); i++) {
      typeChar(keys.charAt(i), KeyMode.PRESS_ONLY);
    }
  }

  @Override
  public void keyUp(String keys) {
    for (int i = 0; i < keys.length(); i++) {
      typeChar(keys.charAt(i), KeyMode.RELEASE_ONLY);
    }
  }

  @Override
  public void keyDown(int code) {
    typeKey(code, KeyMode.PRESS_ONLY);
  }

  @Override
  public void keyUp(int code) {
    typeCode(keyToXlib(code), KeyMode.RELEASE_ONLY);
  }

  @Override
  public void keyUp() {
    for (Integer key : new ArrayList<>(pressedKeys)) {
      typeCode(key, KeyMode.RELEASE_ONLY);
    }
  }

  @Override
  public void pressModifiers(int modifiers) {
    typeModifiers(modifiers, KeyMode.PRESS_ONLY);
  }

  @Override
  public void releaseModifiers(int modifiers) {
    typeModifiers(modifiers, KeyMode.RELEASE_ONLY);
  }

  private void typeModifiers(int modifiers, KeyMode keyMode) {
    if ((modifiers & KeyModifier.CTRL) != 0) typeKey(KeyEvent.VK_CONTROL, keyMode);
    if ((modifiers & KeyModifier.SHIFT) != 0) typeKey(KeyEvent.VK_SHIFT, keyMode);
    if ((modifiers & KeyModifier.ALT) != 0) typeKey(KeyEvent.VK_ALT, keyMode);
    if ((modifiers & KeyModifier.ALTGR) != 0) typeKey(KeyEvent.VK_ALT_GRAPH, keyMode);
    if ((modifiers & KeyModifier.META) != 0) typeKey(KeyEvent.VK_META, keyMode);
  }

  @Override
  public void typeStarts() {
    // Nothing to do
  }

  @Override
  public void typeEnds() {
    // Nothing to do
  }

  @Override
  public void typeKey(int key) {
    typeKey(key, KeyMode.PRESS_RELEASE);
  }

  @Override
  public void typeChar(char character, KeyMode mode) {
    if (character >= '\ue000' && character < '\ue050') {
      typeKey(Key.toJavaKeyCode(character)[0], mode);
    } else {
      typeCode(charToXlib(character), mode);
    }
  }

  public void typeKey(int key, KeyMode mode) {
    typeCode(keyToXlib(key), mode);
  }

  private void typeCode(int xlibCode, KeyMode mode) {
    boolean addShift = requiresShift(xlibCode) && !shiftPressed;
    try {
      if (mode == KeyMode.PRESS_RELEASE || mode == KeyMode.PRESS_ONLY) {
        if (addShift) {
          pressKey(XKeySym.XK_Shift_L);
        }
        pressKey(xlibCode);
        if (xlibCode == XKeySym.XK_Shift_L || xlibCode == XKeySym.XK_Shift_R || xlibCode == XKeySym.XK_Shift_Lock) {
          shiftPressed = true;
        }
      }

      if (mode == KeyMode.PRESS_RELEASE || mode == KeyMode.RELEASE_ONLY) {
        releaseKey(xlibCode);
        if (addShift) {
          releaseKey(XKeySym.XK_Shift_L);
        }

        if (xlibCode == XKeySym.XK_Shift_L || xlibCode == XKeySym.XK_Shift_R || xlibCode == XKeySym.XK_Shift_Lock) {
          shiftPressed = false;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void pressKey(int key) throws IOException {
    screen.getClient().keyDown(key);
    pressedKeys.add(key);
  }

  private void releaseKey(int key) throws IOException {
    screen.getClient().keyUp(key);
    pressedKeys.remove(key);
  }

  private int charToXlib(char c) {
    if (c == 0x007F) {
      return XKeySym.XK_Delete;
    }
    if (c >= 0x0020 && c <= 0x00FF) {
      return c;
    }
    switch (c) {
      case '\u0008':
        return XKeySym.XK_BackSpace;
      case '\u0009':
        return XKeySym.XK_Tab;
      case '\n':
        return XKeySym.XK_Linefeed;
      case '\u000b':
        return XKeySym.XK_Clear;
      case '\r':
        return XKeySym.XK_Return;
      case '\u0013':
        return XKeySym.XK_Pause;
      case '\u0014':
        return XKeySym.XK_Scroll_Lock;
      case '\u0015':
        return XKeySym.XK_Sys_Req;
      case '\u001b':
        return XKeySym.XK_Escape;
      default:
        throw new IllegalArgumentException("Cannot type character " + c);
    }
  }

  private int keyToXlib(int code) {
    switch (code) {
      case VK_ENTER:
        return XKeySym.XK_Return;
      case VK_BACK_SPACE:
        return XKeySym.XK_BackSpace;
      case VK_TAB:
        return XKeySym.XK_Tab;
      case VK_CANCEL:
        return XKeySym.XK_Cancel;
      case VK_CLEAR:
        return XKeySym.XK_Clear;
      case VK_SHIFT:
        return XKeySym.XK_Shift_L;
      case VK_CONTROL:
        return XKeySym.XK_Control_L;
      case VK_ALT:
        return XKeySym.XK_Alt_L;
      case VK_PAUSE:
        return XKeySym.XK_Pause;
      case VK_CAPS_LOCK:
        return XKeySym.XK_Caps_Lock;
      case VK_ESCAPE:
        return XKeySym.XK_Escape;
      case VK_SPACE:
        return XKeySym.XK_space;
      case VK_PAGE_UP:
        return XKeySym.XK_Page_Up;
      case VK_PAGE_DOWN:
        return XKeySym.XK_Page_Down;
      case VK_END:
        return XKeySym.XK_End;
      case VK_HOME:
        return XKeySym.XK_Home;
      case VK_LEFT:
        return XKeySym.XK_Left;
      case VK_UP:
        return XKeySym.XK_Up;
      case VK_RIGHT:
        return XKeySym.XK_Right;
      case VK_DOWN:
        return XKeySym.XK_Down;
      case VK_COMMA:
        return XKeySym.XK_comma;
      case VK_MINUS:
        return XKeySym.XK_minus;
      case VK_PERIOD:
        return XKeySym.XK_period;
      case VK_SLASH:
        return XKeySym.XK_slash;
      case VK_0:
        return XKeySym.XK_0;
      case VK_1:
        return XKeySym.XK_1;
      case VK_2:
        return XKeySym.XK_2;
      case VK_3:
        return XKeySym.XK_3;
      case VK_4:
        return XKeySym.XK_4;
      case VK_5:
        return XKeySym.XK_5;
      case VK_6:
        return XKeySym.XK_6;
      case VK_7:
        return XKeySym.XK_7;
      case VK_8:
        return XKeySym.XK_8;
      case VK_9:
        return XKeySym.XK_9;
      case VK_SEMICOLON:
        return XKeySym.XK_semicolon;
      case VK_EQUALS:
        return XKeySym.XK_equal;
      case VK_A:
        return shiftPressed ? XKeySym.XK_A : XKeySym.XK_a;
      case VK_B:
        return shiftPressed ? XKeySym.XK_B : XKeySym.XK_b;
      case VK_C:
        return shiftPressed ? XKeySym.XK_C : XKeySym.XK_c;
      case VK_D:
        return shiftPressed ? XKeySym.XK_D : XKeySym.XK_d;
      case VK_E:
        return shiftPressed ? XKeySym.XK_E : XKeySym.XK_e;
      case VK_F:
        return shiftPressed ? XKeySym.XK_F : XKeySym.XK_f;
      case VK_G:
        return shiftPressed ? XKeySym.XK_G : XKeySym.XK_g;
      case VK_H:
        return shiftPressed ? XKeySym.XK_H : XKeySym.XK_h;
      case VK_I:
        return shiftPressed ? XKeySym.XK_I : XKeySym.XK_i;
      case VK_J:
        return shiftPressed ? XKeySym.XK_J : XKeySym.XK_j;
      case VK_K:
        return shiftPressed ? XKeySym.XK_K : XKeySym.XK_k;
      case VK_L:
        return shiftPressed ? XKeySym.XK_L : XKeySym.XK_l;
      case VK_M:
        return shiftPressed ? XKeySym.XK_M : XKeySym.XK_m;
      case VK_N:
        return shiftPressed ? XKeySym.XK_N : XKeySym.XK_n;
      case VK_O:
        return shiftPressed ? XKeySym.XK_O : XKeySym.XK_o;
      case VK_P:
        return shiftPressed ? XKeySym.XK_P : XKeySym.XK_p;
      case VK_Q:
        return shiftPressed ? XKeySym.XK_Q : XKeySym.XK_q;
      case VK_R:
        return shiftPressed ? XKeySym.XK_R : XKeySym.XK_r;
      case VK_S:
        return shiftPressed ? XKeySym.XK_S : XKeySym.XK_s;
      case VK_T:
        return shiftPressed ? XKeySym.XK_T : XKeySym.XK_t;
      case VK_U:
        return shiftPressed ? XKeySym.XK_U : XKeySym.XK_u;
      case VK_V:
        return shiftPressed ? XKeySym.XK_V : XKeySym.XK_v;
      case VK_W:
        return shiftPressed ? XKeySym.XK_W : XKeySym.XK_w;
      case VK_X:
        return shiftPressed ? XKeySym.XK_X : XKeySym.XK_x;
      case VK_Y:
        return shiftPressed ? XKeySym.XK_Y : XKeySym.XK_y;
      case VK_Z:
        return shiftPressed ? XKeySym.XK_Z : XKeySym.XK_z;
      case VK_OPEN_BRACKET:
        return XKeySym.XK_bracketleft;
      case VK_BACK_SLASH:
        return XKeySym.XK_backslash;
      case VK_CLOSE_BRACKET:
        return XKeySym.XK_bracketright;
      case VK_NUMPAD0:
        return XKeySym.XK_KP_0;
      case VK_NUMPAD1:
        return XKeySym.XK_KP_1;
      case VK_NUMPAD2:
        return XKeySym.XK_KP_2;
      case VK_NUMPAD3:
        return XKeySym.XK_KP_3;
      case VK_NUMPAD4:
        return XKeySym.XK_KP_4;
      case VK_NUMPAD5:
        return XKeySym.XK_KP_5;
      case VK_NUMPAD6:
        return XKeySym.XK_KP_6;
      case VK_NUMPAD7:
        return XKeySym.XK_KP_7;
      case VK_NUMPAD8:
        return XKeySym.XK_KP_8;
      case VK_NUMPAD9:
        return XKeySym.XK_KP_9;
      case VK_MULTIPLY:
        return XKeySym.XK_KP_Multiply;
      case VK_ADD:
        return XKeySym.XK_KP_Add;
      case VK_SEPARATOR:
        return XKeySym.XK_KP_Separator;
      case VK_SUBTRACT:
        return XKeySym.XK_KP_Subtract;
      case VK_DECIMAL:
        return XKeySym.XK_KP_Decimal;
      case VK_DIVIDE:
        return XKeySym.XK_KP_Divide;
      case VK_DELETE:
        return XKeySym.XK_KP_Delete;
      case VK_NUM_LOCK:
        return XKeySym.XK_Num_Lock;
      case VK_SCROLL_LOCK:
        return XKeySym.XK_Scroll_Lock;
      case VK_F1:
        return XKeySym.XK_F1;
      case VK_F2:
        return XKeySym.XK_F2;
      case VK_F3:
        return XKeySym.XK_F3;
      case VK_F4:
        return XKeySym.XK_F4;
      case VK_F5:
        return XKeySym.XK_F5;
      case VK_F6:
        return XKeySym.XK_F6;
      case VK_F7:
        return XKeySym.XK_F7;
      case VK_F8:
        return XKeySym.XK_F8;
      case VK_F9:
        return XKeySym.XK_F9;
      case VK_F10:
        return XKeySym.XK_F10;
      case VK_F11:
        return XKeySym.XK_F11;
      case VK_F12:
        return XKeySym.XK_F12;
      case VK_F13:
        return XKeySym.XK_F13;
      case VK_F14:
        return XKeySym.XK_F14;
      case VK_F15:
        return XKeySym.XK_F15;
      case VK_F16:
        return XKeySym.XK_F16;
      case VK_F17:
        return XKeySym.XK_F17;
      case VK_F18:
        return XKeySym.XK_F18;
      case VK_F19:
        return XKeySym.XK_F19;
      case VK_F20:
        return XKeySym.XK_F20;
      case VK_F21:
        return XKeySym.XK_F21;
      case VK_F22:
        return XKeySym.XK_F22;
      case VK_F23:
        return XKeySym.XK_F23;
      case VK_F24:
        return XKeySym.XK_F24;
      case VK_PRINTSCREEN:
        return XKeySym.XK_Print;
      case VK_INSERT:
        return XKeySym.XK_Insert;
      case VK_HELP:
        return XKeySym.XK_Help;
      case VK_META:
        return XKeySym.XK_Meta_L;
      case VK_KP_UP:
        return XKeySym.XK_KP_Up;
      case VK_KP_DOWN:
        return XKeySym.XK_KP_Down;
      case VK_KP_LEFT:
        return XKeySym.XK_KP_Left;
      case VK_KP_RIGHT:
        return XKeySym.XK_KP_Right;
      case VK_DEAD_GRAVE:
        return XKeySym.XK_dead_grave;
      case VK_DEAD_ACUTE:
        return XKeySym.XK_dead_acute;
      case VK_DEAD_CIRCUMFLEX:
        return XKeySym.XK_dead_circumflex;
      case VK_DEAD_TILDE:
        return XKeySym.XK_dead_tilde;
      case VK_DEAD_MACRON:
        return XKeySym.XK_dead_macron;
      case VK_DEAD_BREVE:
        return XKeySym.XK_dead_breve;
      case VK_DEAD_ABOVEDOT:
        return XKeySym.XK_dead_abovedot;
      case VK_DEAD_DIAERESIS:
        return XKeySym.XK_dead_diaeresis;
      case VK_DEAD_ABOVERING:
        return XKeySym.XK_dead_abovering;
      case VK_DEAD_DOUBLEACUTE:
        return XKeySym.XK_dead_doubleacute;
      case VK_DEAD_CARON:
        return XKeySym.XK_dead_caron;
      case VK_DEAD_CEDILLA:
        return XKeySym.XK_dead_cedilla;
      case VK_DEAD_OGONEK:
        return XKeySym.XK_dead_ogonek;
      case VK_DEAD_IOTA:
        return XKeySym.XK_dead_iota;
      case VK_DEAD_VOICED_SOUND:
        return XKeySym.XK_dead_voiced_sound;
      case VK_DEAD_SEMIVOICED_SOUND:
        return XKeySym.XK_dead_semivoiced_sound;
      case VK_AMPERSAND:
        return XKeySym.XK_ampersand;
      case VK_ASTERISK:
        return XKeySym.XK_asterisk;
      case VK_QUOTEDBL:
        return XKeySym.XK_quotedbl;
      case VK_LESS:
        return XKeySym.XK_less;
      case VK_GREATER:
        return XKeySym.XK_greater;
      case VK_BRACELEFT:
        return XKeySym.XK_bracketleft;
      case VK_BRACERIGHT:
        return XKeySym.XK_bracketright;
      case VK_AT:
        return XKeySym.XK_at;
      case VK_COLON:
        return XKeySym.XK_colon;
      case VK_CIRCUMFLEX:
        return XKeySym.XK_acircumflex;
      case VK_DOLLAR:
        return XKeySym.XK_dollar;
      case VK_EURO_SIGN:
        return XKeySym.XK_EuroSign;
      case VK_EXCLAMATION_MARK:
        return XKeySym.XK_exclam;
      case VK_INVERTED_EXCLAMATION_MARK:
        return XKeySym.XK_exclamdown;
      case VK_LEFT_PARENTHESIS:
        return XKeySym.XK_parenleft;
      case VK_NUMBER_SIGN:
        return XKeySym.XK_numbersign;
      case VK_PLUS:
        return XKeySym.XK_plus;
      case VK_RIGHT_PARENTHESIS:
        return XKeySym.XK_parenright;
      case VK_UNDERSCORE:
        return XKeySym.XK_underscore;
      case VK_WINDOWS:
        return XKeySym.XK_Super_L;
      case VK_COMPOSE:
        return XKeySym.XK_Multi_key;
      case VK_ALT_GRAPH:
        return XKeySym.XK_ISO_Level3_Shift;
      case VK_BEGIN:
        return XKeySym.XK_Begin;
    }
    throw new IllegalArgumentException("Cannot type keycode " + code);
  }

  private boolean requiresShift(int xlibKeySym) {
    // This is keyboard layout dependent.
    // What's encoded here is for a basic US layout
    switch (xlibKeySym) {
      case XKeySym.XK_A:
      case XKeySym.XK_B:
      case XKeySym.XK_C:
      case XKeySym.XK_D:
      case XKeySym.XK_E:
      case XKeySym.XK_F:
      case XKeySym.XK_G:
      case XKeySym.XK_H:
      case XKeySym.XK_I:
      case XKeySym.XK_J:
      case XKeySym.XK_K:
      case XKeySym.XK_L:
      case XKeySym.XK_M:
      case XKeySym.XK_N:
      case XKeySym.XK_O:
      case XKeySym.XK_P:
      case XKeySym.XK_Q:
      case XKeySym.XK_R:
      case XKeySym.XK_S:
      case XKeySym.XK_T:
      case XKeySym.XK_U:
      case XKeySym.XK_V:
      case XKeySym.XK_W:
      case XKeySym.XK_X:
      case XKeySym.XK_Y:
      case XKeySym.XK_Z:
      case XKeySym.XK_exclam:
      case XKeySym.XK_at:
      case XKeySym.XK_numbersign:
      case XKeySym.XK_dollar:
      case XKeySym.XK_percent:
      case XKeySym.XK_asciicircum:
      case XKeySym.XK_ampersand:
      case XKeySym.XK_asterisk:
      case XKeySym.XK_parenleft:
      case XKeySym.XK_parenright:
      case XKeySym.XK_underscore:
      case XKeySym.XK_plus:
      case XKeySym.XK_braceleft:
      case XKeySym.XK_braceright:
      case XKeySym.XK_colon:
      case XKeySym.XK_quotedbl:
      case XKeySym.XK_bar:
      case XKeySym.XK_less:
      case XKeySym.XK_greater:
      case XKeySym.XK_question:
      case XKeySym.XK_asciitilde:
      case XKeySym.XK_plusminus:
        return true;
      default:
        return false;
    }

  }

  @Override
  public void mouseMove(int x, int y) {
    try {
      screen.getClient().mouseEvent(mouseButtons, x, y);
      mouseX = x;
      mouseY = y;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void mouseDown(int buttons) {
    if ((buttons & Mouse.LEFT) != 0) mouseButtons |= VNC_POINTER_EVENT_BUTTON_1;
    if ((buttons & Mouse.MIDDLE) != 0) mouseButtons |= VNC_POINTER_EVENT_BUTTON_2;
    if ((buttons & Mouse.RIGHT) != 0) mouseButtons |= VNC_POINTER_EVENT_BUTTON_3;
    mouseMove(mouseX, mouseY);
  }

  @Override
  public int mouseUp(int buttons) {
    if ((buttons & Mouse.LEFT) != 0) mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_1;
    if ((buttons & Mouse.MIDDLE) != 0) mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_2;
    if ((buttons & Mouse.RIGHT) != 0) mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_3;
    mouseMove(mouseX, mouseY);

    int remainingButtons = 0;
    if ((mouseButtons & VNC_POINTER_EVENT_BUTTON_1) != 0) remainingButtons |= Mouse.LEFT;
    if ((mouseButtons & VNC_POINTER_EVENT_BUTTON_2) != 0) remainingButtons |= Mouse.MIDDLE;
    if ((mouseButtons & VNC_POINTER_EVENT_BUTTON_3) != 0) remainingButtons |= Mouse.RIGHT;
    return remainingButtons;
  }

  @Override
  public void mouseReset() {
    mouseButtons = 0;
    mouseMove(mouseX, mouseY);
  }

  @Override
  public void clickStarts() {
    // Nothing to do
  }

  @Override
  public void clickEnds() {
    // Nothing to do
  }

  @Override
  public void smoothMove(Location dest) {
    smoothMove(new Location(mouseX, mouseY), dest, (long) (Settings.MoveMouseDelay * 1000L));
  }

  @Override
  public void smoothMove(Location src, Location dest, long duration) {
    if (duration <= 0) {
      mouseMove(dest.getX(), dest.getY());
      return;
    }

    float x = src.getX();
    float y = src.getY();
    float dx = dest.getX() - src.getX();
    float dy = dest.getY() - src.getY();

    long start = System.currentTimeMillis();
    long elapsed = 0;
    do {
      float fraction = (float) elapsed / (float) duration;
      mouseMove((int) (x + fraction * dx), (int) (y + fraction * dy));
      delay(autoDelay);
      elapsed = System.currentTimeMillis() - start;
    } while (elapsed < duration);
    mouseMove(dest.x, dest.y);
  }

  @Override
  public void mouseWheel(int wheelAmt) {
    if (wheelAmt == Mouse.WHEEL_DOWN) {
      mouseButtons |= VNC_POINTER_EVENT_BUTTON_5;
      mouseMove(mouseX, mouseY);
      mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_5;
      mouseMove(mouseX, mouseY);
    } else if (wheelAmt == Mouse.WHEEL_UP) {
      mouseButtons |= VNC_POINTER_EVENT_BUTTON_4;
      mouseMove(mouseX, mouseY);
      mouseButtons &= ~VNC_POINTER_EVENT_BUTTON_4;
      mouseMove(mouseX, mouseY);
    }
  }

  @Override
  public void waitForIdle() {
    // Nothing to do
  }

  @Override
  public void delay(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      // ignored
    }
  }

  @Override
  public void setAutoDelay(int ms) {
    autoDelay = ms;
  }

  @Override
  public Color getColorAt(int x, int y) {
    ScreenImage image = captureScreen(new Rectangle(x, y, 1, 1));
    return new Color(image.getImage().getRGB(0, 0));
  }

  @Override
  public void cleanup() {
    // Nothing to do
  }
}
