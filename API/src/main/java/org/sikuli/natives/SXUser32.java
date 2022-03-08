/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface SXUser32 extends User32 {

  SXUser32 INSTANCE = (SXUser32) Native.load("user32", SXUser32.class, W32APIOptions.DEFAULT_OPTIONS);

  short GetKeyState(int vKey);

  int MapVirtualKeyExW (int uCode, int nMapType, int dwhkl);
  
  int MapVirtualKeyW(int uCode, int uMapType);

  boolean GetKeyboardState(byte[] lpKeyState);

  int ToUnicodeEx(int wVirtKey, int wScanCode, byte[] lpKeyState, char[] pwszBuff, int cchBuff, int wFlags, int dwhkl);
  
  void keybd_event(byte bVk, byte bScan, DWORD dwFlags, ULONG_PTR dwExtraInfo);
  
}

