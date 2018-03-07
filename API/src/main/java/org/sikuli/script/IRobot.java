/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * INTERNAL USE <br>
 * function template for (alternative) Robot implementations
 */
public interface IRobot {
   enum KeyMode {
      PRESS_ONLY, RELEASE_ONLY, PRESS_RELEASE
   };
   void keyDown(String keys);
   void keyUp(String keys);
   void keyDown(int code);
   void keyUp(int code);
   void keyUp();
   void pressModifiers(int modifiers);
   void releaseModifiers(int modifiers);
   void typeChar(char character, KeyMode mode);
   void typeKey(int key);
   void typeStarts();
   void typeEnds();
   void mouseMove(int x, int y);
   void mouseDown(int buttons);
   int mouseUp(int buttons);
   void mouseReset();
   void clickStarts();
   void clickEnds();
   void smoothMove(Location dest);
   void smoothMove(Location src, Location dest, long ms);
   void mouseWheel(int wheelAmt);
   ScreenImage captureScreen(Rectangle screenRect);
   void waitForIdle();
   void delay(int ms);
   void setAutoDelay(int ms);
   Color getColorAt(int x, int y);
   void cleanup();
   boolean isRemote();

   /**
    *  Return the underlying device object (if any).
    */
   IScreen getScreen();
}
