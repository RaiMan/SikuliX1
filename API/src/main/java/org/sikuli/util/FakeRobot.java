/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;

import java.awt.*;

/**
 * for testing non local IScreen implementations (e.g. VNCScreen)
 */
public class FakeRobot implements IRobot{

  public static IRobot getDesktopRobot() {
    try {
      return new RobotDesktop();
    } catch (AWTException e) {
      Debug.error("FakeRobot: getDesktopRobot: not possible: returning null");
      return null;
    }
  }

  @Override
  public void keyDown(String keys) {

  }

  @Override
  public void keyUp(String keys) {

  }

  @Override
  public void keyDown(int code) {

  }

  @Override
  public void keyUp(int code) {

  }

  @Override
  public void keyUp() {

  }

  @Override
  public void pressModifiers(int modifiers) {

  }

  @Override
  public void releaseModifiers(int modifiers) {

  }

  @Override
  public void typeChar(char character, KeyMode mode) {

  }

  @Override
  public void typeKey(int key) {

  }

  @Override
  public void typeStarts() {

  }

  @Override
  public void typeEnds() {

  }

  @Override
  public void mouseMove(int x, int y) {
    Debug.error("FakeRobot: mouseMove: (%d, %d)", x, y);
  }

  @Override
  public void mouseDown(int buttons) {

  }

  @Override
  public int mouseUp(int buttons) {
    Debug.error("FakeRobot: mouseUp: most probably a click");
    return 0;
  }

  @Override
  public void mouseReset() {

  }

  @Override
  public void clickStarts() {

  }

  @Override
  public void clickEnds() {

  }

  @Override
  public void smoothMove(Location dest) {
    Debug.error("FakeRobot: mouseMove: (%d, %d)", dest.x, dest.y);
  }

  @Override
  public void smoothMove(Location src, Location dest, long ms) {
    Debug.error("FakeRobot: mouseMove: (%d, %d)", dest.x, dest.y);
  }

  @Override
  public void mouseWheel(int wheelAmt) {

  }

  @Override
  public ScreenImage captureScreen(Rectangle screenRect) {
    Debug.log(3, "FakeRobot: captureScreen: should not be used: returning null");
    return null;
  }

  @Override
  public void waitForIdle() {

  }

  @Override
  public void delay(int ms) {

  }

  @Override
  public void setAutoDelay(int ms) {

  }

  @Override
  public Color getColorAt(int x, int y) {
    return null;
  }

  @Override
  public void cleanup() {

  }

  @Override
  public boolean isRemote() {
    return true;
  }

  /**
   * Return the underlying device object (if any).
   */
  @Override
  public IScreen getScreen() {
    return null;
  }
}
