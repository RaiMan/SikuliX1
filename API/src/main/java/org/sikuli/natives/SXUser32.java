package org.sikuli.natives;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface SXUser32 extends User32 {

  SXUser32 INSTANCE = (SXUser32) Native.loadLibrary("user32", SXUser32.class, W32APIOptions.DEFAULT_OPTIONS);

  short GetKeyState(int vKey);
}

