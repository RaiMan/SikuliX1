/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.basics.Animator;
import org.sikuli.basics.AnimatorOutQuarticEase;
import org.sikuli.basics.AnimatorTimeBased;
import org.sikuli.basics.Settings;
import org.sikuli.natives.SXUser32;
import org.sikuli.basics.Debug;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.sikuli.script.*;
import org.sikuli.script.support.devices.Device;
import org.sikuli.script.support.devices.MouseDevice;
import org.sikuli.util.Highlight;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * INTERNAL USE Implementation of IRobot making a DesktopRobot using java.awt.Robot
 */
public class RobotDesktop extends Robot implements IRobot {

  final static int MAX_DELAY = 60000;

  private static int heldButtons = 0;
  private static String heldKeys = "";
  private static final ArrayList<Integer> heldKeyCodes = new ArrayList<Integer>();
  public static int stdAutoDelay = 0;
  public static int stdDelay = 10;
  public static int stdMaxElapsed = 1000;
  private Screen scr = null;
  private long start;

  private void logRobot(int delay, String msg) {
    start = new Date().getTime();
    int theDelay = getAutoDelay();
    if (theDelay > 0 && theDelay > delay) {
      Debug.log(0, msg, isAutoWaitForIdle(), theDelay);
    }
  }

  private void logRobot(String msg, int maxElapsed) {
    long elapsed = new Date().getTime() - start;
    if (elapsed > maxElapsed) {
      Debug.log(0, msg, elapsed);
      setAutoDelay(stdAutoDelay);
    }
  }

  private GraphicsDevice gdev = null;

  public RobotDesktop(GraphicsDevice gdev) throws AWTException {
    super(gdev);
    this.gdev = gdev;
    setAutoDelay(stdAutoDelay);
  }

  @Override
  public void delay(int ms) {
    if (ms < 0) {
      return;
    }
    while (ms > MAX_DELAY) {
      super.delay(MAX_DELAY);
      ms -= MAX_DELAY;
    }
    super.delay(ms);
  }

  @Override
  public ScreenImage captureScreen(Rectangle rect) {
    if (Device.isCaptureBlocked()) { //TODO robot captureScreen
      Commons.terminate(999, "Capture is blocked");
    }
//    Rectangle s = scr.getBounds();
    Rectangle cRect = new Rectangle(rect);
//    cRect.translate(-s.x, -s.y);
    BufferedImage img = createScreenCapture(rect);
    return new ScreenImage(rect, img);
  }

  @Override
  public Color getColorAt(int x, int y) {
    return getPixelColor(x, y);
  }

  @Override
  public boolean isRemote() {
    return false;
  }

  @Override
  public Screen getScreen() {
    return scr;
  }

  @Override
  public void smoothMove(Location dest) {
    smoothMove(Mouse.at(), dest, (long) (Settings.MoveMouseDelay * 1000L));
  }

  public void smoothMoveSlow(Location dest) {
    smoothMove(Mouse.at(), dest, (long) (Settings.SlowMotionDelay * 1000L));
  }

  @Override
  public void smoothMove(Location src, Location dest, long ms) {
    Debug.log(4, "RobotDesktop: smoothMove (%.1f): %s ---> %s", ms / 1000f, src, dest);
    if (src.equals(dest)) {
      return;
    }
    float x = 0, y = 0;
    if (ms == 0) {
      doMouseMove(dest.x, dest.y);
      x = dest.x;
      y = dest.y;
    } else {
      Animator aniX = new AnimatorTimeBased(
          new AnimatorOutQuarticEase(src.x, dest.x, ms));
      Animator aniY = new AnimatorTimeBased(
          new AnimatorOutQuarticEase(src.y, dest.y, ms));
      while (aniX.running()) {
        x = aniX.step();
        y = aniY.step();
        doMouseMove((int) x, (int) y);
      }
    }
    checkMousePosition(new Location(x, y));
  }

  private void doMouseMove(int x, int y) {
    mouseMove(x, y);
  }

  private void checkMousePosition(Location targetPos) {
    if (!MouseDevice.isUseable()) { // checkMousePosition
      return;
    }
    PointerInfo mp = MouseInfo.getPointerInfo();
    Point actualPos;
    if (mp == null) {
      Debug.error("RobotDesktop: checkMousePosition: MouseInfo.getPointerInfo invalid after move to %s", targetPos);
    } else {
      actualPos = mp.getLocation();
      if (Settings.checkMousePosition && !MouseDevice.nearby(targetPos, actualPos)) {
        Debug.error("RobotDesktop: checkMousePosition: should be %s - but is (%d, %d)!"
                + "\nIf you did not move the mouse while script was running:\n"
                + " Mouse actions might be blocked generally or by the target application."
                + (Settings.isWindows() ? "\nYou might try to run the SikuliX stuff from commandline in admin-mode." : ""),
            targetPos, actualPos.x, actualPos.y);
      }
    }
  }

  public void moveMouse(int x, int y) {
    doMouseMove(x, y);
  }

  @Override
  public void mouseDown(int buttons) {
    if (heldButtons != 0) {
      heldButtons |= buttons;
    } else {
      heldButtons = buttons;
    }
    doMouseDown(heldButtons);
  }

  private boolean needsRobotFake() {
    return Commons.runningMac() && Settings.ClickTypeHack;
  }

  private void doMouseDown(int buttons) {
    Highlight fakeHighlight = null;
    if (needsRobotFake()) {
      fakeHighlight = Highlight.fakeHighlight();
    }
    logRobot(stdAutoDelay, "MouseDown: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    if (null != fakeHighlight) {
      delay(20);
      fakeHighlight.close();
      delay(20);
    }
    mousePress(buttons);
    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("MouseDown: extended delay: %d", stdMaxElapsed);
  }

  @Override
  public int mouseUp(int buttons) {
    if (buttons == 0) {
      doMouseUp(heldButtons);
      heldButtons = 0;
    } else {
      doMouseUp(buttons);
      heldButtons &= ~buttons;
    }
    return heldButtons;
  }

  private void doMouseUp(int buttons) {
    logRobot(stdAutoDelay, "MouseUp: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    mouseRelease(buttons);
    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("MouseUp: extended delay: %d", stdMaxElapsed);
  }

  @Override
  public void mouseReset() {
    if (heldButtons != 0) {
      mouseRelease(heldButtons);
      if (stdAutoDelay == 0) {
        delay(stdDelay);
      }
      heldButtons = 0;
    }
  }

  @Override
  public void clickStarts() {
  }

  @Override
  public void clickEnds() {
  }

  @Override
  public void pressModifiers(int modifiers) {
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      typeChar(Key.C_SHIFT, KeyMode.PRESS_ONLY); // pressModifiers
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      typeChar(Key.C_CTRL, KeyMode.PRESS_ONLY); // pressModifiers
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      typeChar(Key.C_ALT, KeyMode.PRESS_ONLY); // pressModifiers
    }
    if ((modifiers & KeyModifier.META) != 0) {
      typeChar(Key.C_META, KeyMode.PRESS_ONLY); // pressModifiers
    }
    if ((modifiers & KeyModifier.ALTGR) != 0) {
      typeChar(Key.C_ALTGR, KeyMode.PRESS_ONLY); // pressModifiers
    }
  }

  @Override
  public void releaseModifiers(int modifiers) {
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      typeChar(Key.C_SHIFT, KeyMode.RELEASE_ONLY); // releaseModifiers
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      typeChar(Key.C_CTRL, KeyMode.RELEASE_ONLY); // releaseModifiers
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      typeChar(Key.C_ALT, KeyMode.RELEASE_ONLY); // releaseModifiers
    }
    if ((modifiers & KeyModifier.META) != 0) {
      typeChar(Key.C_META, KeyMode.RELEASE_ONLY); // releaseModifiers
    }
    if ((modifiers & KeyModifier.ALTGR) != 0) {
      typeChar(Key.C_ALTGR, KeyMode.RELEASE_ONLY); // releaseModifiers
    }
  }

  @Override
  public void keyDown(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        if (heldKeys.indexOf(keys.charAt(i)) == -1) {
          Debug.log(4, "press: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.PRESS_ONLY); // KeyDown
          heldKeys += keys.charAt(i);
        }
      }
    }
  }

  @Override
  public void keyDown(int code) {
    if (!heldKeyCodes.contains(code)) {
      doKeyPress(code);
      heldKeyCodes.add(code);
    }
  }

  @Override
  public void keyUp(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        int pos;
        if ((pos = heldKeys.indexOf(keys.charAt(i))) != -1) {
          Debug.log(4, "release: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.RELEASE_ONLY); // keyUp
          heldKeys = heldKeys.substring(0, pos)
              + heldKeys.substring(pos + 1);
        }
      }
    }
  }

  @Override
  public void keyUp(int code) {
    if (heldKeyCodes.contains(code)) {
      doKeyRelease(code);
      heldKeyCodes.remove((Object) code);
    }
  }

  @Override
  public void keyUp() {
    keyUp(heldKeys);
    for (int code : new ArrayList<>(heldKeyCodes)) {
      keyUp(code);
    }
  }

  @Override
  public boolean typeChar(char character, KeyMode mode) {
    int[] keyCode;
    try {
      keyCode = KeyboardLayout.toJavaKeyCode(character);
      doType(mode, keyCode);
    } catch (IllegalArgumentException e) {
      if (Commons.runningWindows() || Commons.runningMac()) {
        if (mode == KeyMode.PRESS_RELEASE) {
          if (Debug.getDebugLevel() == 3) {
            Debug.log(3, "Robot::type(%s): not possible --- trying with typex()", character);
          }
          keyUp();
          typex("" + character);
          return false;
        } else {
          Debug.error("Robot::typex(): not supported for PRESS_ONLY nor RELEASE_ONLY");
        }
      } else {
        Debug.error("Robot::type(%s): not possible and typex() not implemented", character);
      }
    }
    return true;
  }

  private static int[] numPad =
      new int[]{KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3, KeyEvent.VK_NUMPAD4,
          KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9};

  public void typex(String text) {
    String retVal = "";
    String[] cNums = new String[text.length()];
    boolean isCode = true;
    boolean isHex = false;
    if (text.length() > 5) {
      isCode = false;
      if (text.length() == 6 && text.startsWith("0x")) {
        isHex = true;
      }
    } else {
      for (int ci = 0; ci < text.length(); ci++) {
        if (ci == 0 && text.startsWith(" ")) {
          continue;
        }
        if (!("0123456789").contains(text.substring(ci, ci + 1))) {
          isCode = false;
          break;
        }
      }
    }
    String cNum;
    if (isCode) {
      cNum = text;
      if (text.startsWith(" ")) {
        cNum = text.substring(1);
        cNum = "00".substring(0, 3 - cNum.length()) + cNum;
      } else if (text.length() < 4) {
        cNum = "000".substring(0, 4 - text.length()) + text;
      }
      cNums = new String[]{cNum};
    } else if (isHex) {
      int hexVal = Integer.parseInt(text.substring(2), 16);
      cNums = new String[]{String.format("%d", (hexVal + 100000)).substring(1)};
    } else {
      for (int ci = 0; ci < text.length(); ci++) {
        cNums[ci] = String.format("%d", (((int) text.charAt(ci)) + 100000)).substring(1);
      }
    }
    KeyboardLayout.changeKeyboard(KeyboardLayout.UNI_HEX_INP);
    for (String code : cNums) {
      if (Commons.runningWindows()) {
        pressModifiers(KeyModifier.ALT);
        for (int i = 0; i < code.length(); i++) {
          int iNum = 0;
          try {
            iNum = Integer.parseInt(code.substring(i, i + 1));
          } catch (NumberFormatException e) {
            return;
          }
          keyPress(numPad[iNum]);
          keyRelease(numPad[iNum]);
        }
        releaseModifiers(KeyModifier.ALT);
      } else if (Commons.runningMac()) {
        if (!KeyboardLayout.hasUnicodeKeyboard) {
          Debug.error("Robot::typex: not possible: Keyboardlayout(Unicode Hex Input) not available.");
          break;
        }
        int nCode = -1;
        String hCode = "";
        try {
          nCode = Integer.parseInt(code);
          hCode = String.format("%h", nCode).strip();
        } catch (NumberFormatException e) {
        }
        if (hCode.length() < 4) {
          hCode = "000".substring(0, 4 - hCode.length()) + hCode;
        }
        retVal = hCode;
        if (false) { //TODO needed on Mac?? (hCode.charAt(0) == '0' && hCode.charAt(3) == '0') {
          Debug.error("Robot::typex: cannot be typed using ALT+ABCD (macOS problem: 0x0..0): %s (0x%s)", (char) nCode, hCode);
        } else {
          Map<Character, int[]> keys = KeyboardLayout.getAwtEnUS();
          pressModifiers(KeyModifier.ALT);
          for (int i = 0; i < hCode.length(); i++) {
            Character c = hCode.charAt(i);
            int kcode = keys.get(c)[0];
            keyPress(kcode);
            keyRelease(kcode);
          }
          releaseModifiers(KeyModifier.ALT);
          Debug.log(3, "typex: %s", hCode);
        }
      } else {
        Debug.error("Robot::typex(): not supported (currently Windows/Mac only)");
        break;
      }
    }
    if (KeyboardLayout.hasUnicodeKeyboard) {
      KeyboardLayout.changeKeyboard();
    }
  }

  @Override
  public void typeKey(int key) {
    if (Settings.isMac()) {
/*
      if (key == Key.toJavaKeyCodeFromText("#N.")) {
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#C."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#C."));
        return;
      } else if (key == Key.toJavaKeyCodeFromText("#T.")) {
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#C."));
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#C."));
        return;
      } else if (key == Key.toJavaKeyCodeFromText("#X.")) {
        key = Key.toJavaKeyCodeFromText("#T.");
        doType(KeyMode.PRESS_ONLY, Key.toJavaKeyCodeFromText("#A."));
        doType(KeyMode.PRESS_RELEASE, key);
        doType(KeyMode.RELEASE_ONLY, Key.toJavaKeyCodeFromText("#A."));
        return;
      }
*/
    }
    doType(KeyMode.PRESS_RELEASE, key);
  }

  private void doType(KeyMode mode, int... keyCodes) {
    waitForIdle();
    if (mode == KeyMode.PRESS_ONLY) {
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyPress(keyCodes[i]);
      }
    } else if (mode == KeyMode.RELEASE_ONLY) {
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyRelease(keyCodes[i]);
      }
    } else {
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyPress(keyCodes[i]);
      }
      for (int i = 0; i < keyCodes.length; i++) {
        doKeyRelease(keyCodes[i]);
      }
    }
    waitForIdle();
  }

  private void doKeyPress(int keyCode) {
    Highlight fakeHighlight = null;
    if (needsRobotFake()) {
      fakeHighlight = Highlight.fakeHighlight();
    }
    logRobot(stdAutoDelay, "KeyPress: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    if (null != fakeHighlight) {
      delay(20);
      fakeHighlight.close();
      delay(20);
    }

    if (Settings.AutoDetectKeyboardLayout && Settings.isWindows()) {
      int scanCode = SXUser32.INSTANCE.MapVirtualKeyW(keyCode, 0);
      SXUser32.INSTANCE.keybd_event((byte) keyCode, (byte) scanCode,
          new WinDef.DWORD(0), new BaseTSD.ULONG_PTR(0));
    } else {
      keyPress(keyCode);
    }

    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("KeyPress: extended delay: %d", stdMaxElapsed);
  }

  private void doKeyRelease(int keyCode) {
    logRobot(stdAutoDelay, "KeyRelease: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    if (Settings.AutoDetectKeyboardLayout && Settings.isWindows()) {
/*
      WinUser.INPUT input = new WinUser.INPUT();
      input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
      input.input.setType("ki");
      input.input.ki.wScan = new WinDef.WORD(0);
      input.input.ki.time = new WinDef.DWORD(0);
      input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
      input.input.ki.wVk = new WinDef.WORD(keyCode);
      input.input.ki.dwFlags = new WinDef.DWORD(
          WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);

      User32.INSTANCE.SendInput(new WinDef.DWORD(1),
          (WinUser.INPUT[]) input.toArray(1), input.size());
*/
      int scanCode = SXUser32.INSTANCE.MapVirtualKeyW(keyCode, 0);
      SXUser32.INSTANCE.keybd_event((byte) keyCode, (byte) scanCode,
          new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP), new BaseTSD.ULONG_PTR(0));
    } else {
      keyRelease(keyCode);
    }

    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("KeyRelease: extended delay: %d", stdMaxElapsed);
  }

  @Override
  public void cleanup() {
  }
}
