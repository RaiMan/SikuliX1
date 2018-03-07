/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Törcsi on 2016. 06. 26.
 * Revised by RaiMan
 */
public class ADBRobot implements IRobot {

  private int mouse_X1 = -1;
  private int mouse_Y1 = -1;

  private int mouse_X2 = -1;
  private int mouse_Y2 = -1;


  private boolean mouseDown = false;

  private int autodelay = 0;
  private boolean waitForIdle = false;
  final static int MAX_DELAY = 60000;

  private ADBScreen screen;
  private ADBDevice device;

  public ADBRobot(ADBScreen screen, ADBDevice device) {
    this.screen = screen;
    this.device = device;
  }

  private void notSupported(String feature) {
    Debug.error("ADBRobot: %s: not supported yet", feature);

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
  public void cleanup() {
    notSupported("feature");
  }

  //<editor-fold desc="key actions not supported yet">
  @Override
  public void keyDown(String keys) {
    notSupported("keyDown");
  }

  @Override
  public void keyUp(String keys) {
    notSupported("keyUp");
  }

  @Override
  public void keyDown(int code) {
    notSupported("keyDown");
  }

  @Override
  public void keyUp(int code) {
    notSupported("keyUp");
  }

  @Override
  public void keyUp() {
    notSupported("keyUp");
  }

  @Override
  public void pressModifiers(int modifiers) {
    if (modifiers != 0) {
      notSupported("pressModifiers");
    }
  }

  @Override
  public void releaseModifiers(int modifiers) {
    if (modifiers != 0) {
      notSupported("releaseModifiers");
    }
  }

  @Override
  public void typeChar(char character, KeyMode mode) {
    if (device == null) {
      return;
    }
    device.typeChar(character);
  }

  @Override
  public void typeKey(int key) {
    notSupported("typeKey");
  }

  @Override
  public void typeStarts() {
    if (device == null) {
      return;
    }
    while (!device.typeStarts()) {
      RunTime.pause(1);
    }
  }

  @Override
  public void typeEnds() {
    if (device == null) {
      return;
    }
    device.typeEnds();
  }

  //</editor-fold>

  @Override
  public void mouseMove(int x, int y) {
    if (!mouseDown) {
      mouse_X1 = x;
      mouse_Y1 = y;
    } else {
      mouse_X2 = x;
      mouse_Y2 = y;
    }
  }

  @Override
  public void mouseDown(int buttons) {
    clickStarts();
  }

  @Override
  public int mouseUp(int buttons) {
    clickEnds();
    return 0;
  }

  @Override
  public void mouseReset() {
    mouseDown = false;
  }

  @Override
  public void clickStarts() {
    mouseDown = true;
    mouse_X2 = mouse_X1;
    mouse_Y2 = mouse_Y1;
  }

  @Override
  public void clickEnds() {
    if (device == null) {
      return;
    }
    if (mouseDown) {
      mouseDown = false;
      if (mouse_X1 == mouse_X2 && mouse_Y1 == mouse_Y2) {
        device.tap(mouse_X1, mouse_Y1);
      } else {
        device.swipe(mouse_X1, mouse_Y1, mouse_X2, mouse_Y2);
      }
    }
  }

  //<editor-fold desc="mouse actions not supported yet">
  @Override
  public void smoothMove(Location dest) {
    mouseMove(dest.x, dest.y);
  }

  @Override
  public void smoothMove(Location src, Location dest, long ms) {
    notSupported("smoothMove");
  }

  @Override
  public void mouseWheel(int wheelAmt) {
    notSupported("mouseWheel");
  }
  //</editor-fold>

  @Override
  public ScreenImage captureScreen(Rectangle screenRect) {
    if (device == null) {
      return null;
    }
    return device.captureScreen(screenRect);
  }

  @Override
  public Color getColorAt(int x, int y) {
    notSupported("getColorAt");
    return null;
  }

  @Override
  public void waitForIdle() {
    try {
      new java.awt.Robot().waitForIdle();
    } catch (AWTException e) {
      Debug.log(-1, "Error-could non instantiate robot: " + e);
    }
  }

  @Override
  public void delay(int ms) {
    if (ms < 0) {
      ms = 0;
    }
    if (ms > MAX_DELAY) {
      ms = MAX_DELAY;
    }
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Debug.log(-1, "Thread Interrupted: " + e);
    }
  }

  @Override
  public void setAutoDelay(int ms) {
    if (ms < 0) {
      ms = 0;
    }
    if (ms > MAX_DELAY) {
      ms = MAX_DELAY;
    }
    autodelay = ms;
  }
}

