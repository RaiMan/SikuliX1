/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.basics.Animator;
import org.sikuli.basics.AnimatorOutQuarticEase;
import org.sikuli.basics.AnimatorTimeBased;
import org.sikuli.basics.Settings;
import org.sikuli.natives.SXUser32;
import org.sikuli.basics.Debug;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.sikuli.script.*;
import org.sikuli.util.Highlight;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;

/**
 * INTERNAL USE Implementation of IRobot making a DesktopRobot using java.awt.Robot
 */
public class RobotDesktop extends Robot implements IRobot {

  final static int MAX_DELAY = 60000;
  public static final int ALL_MODIFIERS = KeyModifier.SHIFT | KeyModifier.CTRL | KeyModifier.ALT |  KeyModifier.META | KeyModifier.ALTGR;

  private static int heldButtons = 0;
  private static String heldKeys = "";
  private static final ArrayList<Integer> heldKeyCodes = new ArrayList<Integer>();
  public static int stdAutoDelay = 0;
  public static int stdDelay = 10;
  public static int stdMaxElapsed = 1000;
  private Screen scr = null;
  private long start;
  private static boolean isMouseInitialized = false;

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

  public RobotDesktop() throws AWTException {
    super();
    setAutoDelay(stdAutoDelay);
  }

  public RobotDesktop(GraphicsDevice gdev) throws AWTException {
    super(gdev);
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
    Rectangle cRect = new Rectangle(rect);
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
    if (ms == 0) {
      doMouseMove(dest.x, dest.y);
      waitForIdle();
      checkMousePosition(dest);
      return;
    }
    Animator aniX = new AnimatorTimeBased(
            new AnimatorOutQuarticEase(src.x, dest.x, ms));
    Animator aniY = new AnimatorTimeBased(
            new AnimatorOutQuarticEase(src.y, dest.y, ms));
    float x = 0, y = 0;
    while (aniX.running()) {
      x = aniX.step();
      y = aniY.step();
      doMouseMove((int) x, (int) y);
    }
    checkMousePosition(new Location((int) x, (int) y));
  }

  private void doMouseMove(int x, int y) {
    mouseMove(x, y);
  }

  private void checkMousePosition(Location p) {
    PointerInfo mp = MouseInfo.getPointerInfo();
    Point pc;
    if (mp == null) {
      Debug.error("RobotDesktop: checkMousePosition: MouseInfo.getPointerInfo invalid\nafter move to %s", p);
    } else {
      pc = mp.getLocation();
      if (pc.x != p.x || pc.y != p.y) {
        if (isMouseInitialized) {
          if (Settings.checkMousePosition) {
            Debug.error("RobotDesktop: checkMousePosition: should be %s\nbut after move is %s"
                            + "\nPossible cause in case you did not touch the mouse while script was running:\n"
                            + " Mouse actions are blocked generally or by the frontmost application."
                            + (Settings.isWindows() ? "\nYou might try to run the SikuliX stuff as admin." : ""),
                    p, new Location(pc));
          }
        }
      }
    }
    if (!isMouseInitialized) {
      isMouseInitialized = true;
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

  private void doMouseDown(int buttons) {
    Highlight fakeHighlight = null;
    if (RunTime.get().needsRobotFake()) {
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
    if (modifiers > ALL_MODIFIERS) { // TODO: Do we  really have to handle this?
      return;
    }
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      typeChar(Key.C_SHIFT, KeyMode.PRESS_ONLY);
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      typeChar(Key.C_CTRL, KeyMode.PRESS_ONLY);
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      typeChar(Key.C_ALT, KeyMode.PRESS_ONLY);
    }
    if ((modifiers & KeyModifier.META) != 0) {
      typeChar(Key.C_META, KeyMode.PRESS_ONLY);
    }
    if ((modifiers & KeyModifier.ALTGR) != 0) {
      typeChar(Key.C_ALTGR, KeyMode.PRESS_ONLY);
    }
  }

  @Override
  public void releaseModifiers(int modifiers) {
    if (modifiers > ALL_MODIFIERS) { // TODO: Do we  really have to handle this?
      return;
    }
    if ((modifiers & KeyModifier.SHIFT) != 0) {
      typeChar(Key.C_SHIFT, KeyMode.RELEASE_ONLY);
    }
    if ((modifiers & KeyModifier.CTRL) != 0) {
      typeChar(Key.C_CTRL, KeyMode.RELEASE_ONLY);
    }
    if ((modifiers & KeyModifier.ALT) != 0) {
      typeChar(Key.C_ALT, KeyMode.RELEASE_ONLY);
    }
    if ((modifiers & KeyModifier.META) != 0) {
      typeChar(Key.C_META, KeyMode.RELEASE_ONLY);
    }
    if ((modifiers & KeyModifier.ALTGR) != 0) {
      typeChar(Key.C_ALTGR, KeyMode.RELEASE_ONLY);
    }
  }

  @Override
  public void keyDown(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        if (heldKeys.indexOf(keys.charAt(i)) == -1) {
          Debug.log(4, "press: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.PRESS_ONLY);
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

  private void doKeyPress(int keyCode) {
    Highlight fakeHighlight = null;
    if (RunTime.get().needsRobotFake()) {
      fakeHighlight = Highlight.fakeHighlight();
    }
    logRobot(stdAutoDelay, "KeyPress: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    if (null != fakeHighlight) {
      delay(20);
      fakeHighlight.close();
      delay(20);
    }

    // on Windows we detect the current layout in KeyboardLayout.
    // Since this layout is not compatible to AWT Robot, we have to use
    // the User32 API to simulate the key press
    if (Settings.AutoDetectKeyboardLayout && Settings.isWindows()) {
        int scanCode =  SXUser32.INSTANCE.MapVirtualKeyW(keyCode, 0);
        SXUser32.INSTANCE.keybd_event((byte)keyCode, (byte)scanCode, new WinDef.DWORD(0), new BaseTSD.ULONG_PTR(0));
    }else{
      keyPress(keyCode);
    }


    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("KeyPress: extended delay: %d", stdMaxElapsed);
  }

  @Override
  public void keyUp(String keys) {
    if (keys != null && !"".equals(keys)) {
      for (int i = 0; i < keys.length(); i++) {
        int pos;
        if ((pos = heldKeys.indexOf(keys.charAt(i))) != -1) {
          Debug.log(4, "release: " + keys.charAt(i));
          typeChar(keys.charAt(i), IRobot.KeyMode.RELEASE_ONLY);
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

  private void doKeyRelease(int keyCode) {
    logRobot(stdAutoDelay, "KeyRelease: WaitForIdle: %s - Delay: %d");
    setAutoDelay(stdAutoDelay);
    // on Windows we detect the current layout in KeyboardLayout.
    // Since this layout is not compatible to AWT Robot, we have to use
    // the User32 API to simulate the key release
    if (Settings.AutoDetectKeyboardLayout && Settings.isWindows()) {
      int scanCode =  SXUser32.INSTANCE.MapVirtualKeyW(keyCode, 0);
      SXUser32.INSTANCE.keybd_event((byte)keyCode, (byte)scanCode, new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP), new BaseTSD.ULONG_PTR(0));
    }else{
      keyRelease(keyCode);
    }

    if (stdAutoDelay == 0) {
      delay(stdDelay);
    }
    logRobot("KeyRelease: extended delay: %d", stdMaxElapsed);
  }

  @Override
  public void typeChar(char character, KeyMode mode) {
    Debug.log(4, "Robot: doType: %s ( %d )",
            KeyEvent.getKeyText(Key.toJavaKeyCode(character)[0]).toString(),
            Key.toJavaKeyCode(character)[0]);
    doType(mode, Key.toJavaKeyCode(character));
  }

  @Override
  public void typeKey(int key) {
    Debug.log(4, "Robot: doType: %s ( %d )", KeyEvent.getKeyText(key), key);
    if (Settings.isMac()) {
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

  @Override
  public void typeStarts() {
  }

  @Override
  public void typeEnds() {
  }

  @Override
  public void cleanup() {
  }
}
