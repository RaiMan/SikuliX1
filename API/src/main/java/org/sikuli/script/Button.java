/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.event.InputEvent;

/**
 * Defines the constants for use with the mouse actions
 * for the button to use and the wheel direction
 */
public class Button {
  public static int LEFT = InputEvent.BUTTON1_MASK;
  public static int MIDDLE = InputEvent.BUTTON2_MASK;
  public static int RIGHT = InputEvent.BUTTON3_MASK;
  public static int WHEEL_UP = -1;
  public static int WHEEL_DOWN = 1;
}
