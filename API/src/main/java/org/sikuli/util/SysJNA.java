/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.bridj.BridJ;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.sikuli.basics.Debug;

/**
 * Direct access to system functions via JNI, JNA, BridJ, ...
 */
public class SysJNA {
  /**
   * Direct access to Windows API kernel32.dll via BridJ
   */
  @Library("kernel32")
  public static class WinKernel32 {

    static {
      BridJ.register();
    }

    // http://msdn.microsoft.com/en-us/library/windows/desktop/ms683188(v=vs.85).aspx
    //DWORD WINAPI GetEnvironmentVariable(
    //  _In_opt_   LPCTSTR lpName,
    //  _Out_opt_  LPTSTR lpBuffer,
    //  _In_       DWORD nSize
    //);
    private static native int GetEnvironmentVariableW(
            Pointer<Character> lpName,
            Pointer<Character> lpBuffer,
            int nSize
    );

    // http://msdn.microsoft.com/en-us/library/windows/desktop/ms686206(v=vs.85).aspx
    //BOOL WINAPI SetEnvironmentVariable(
    //  _In_      LPCTSTR lpName,
    //  _In_opt_  LPCTSTR lpValue
    //);
    private static native boolean SetEnvironmentVariableW(
            Pointer<Character> lpName,
            Pointer<Character> lpValue
    );

    /**
     * get the current value of a variable from real Windows environment
     *
     * @param name of the environment variable
     * @return current content
     */
    public static String getEnvironmentVariable(String name) {
      final int BUFFER_SIZE =  32767;
      Pointer<Character> buffer = Pointer.allocateArray(Character.class, BUFFER_SIZE);
      int result = GetEnvironmentVariableW(Pointer.pointerToWideCString(name), buffer, BUFFER_SIZE);
      if (result == 0) {
        Debug.error("WinKernel32: getEnvironmentVariable: does not work for: %s", name);
        return null;
      }
      return buffer.getWideCString();
    }

    /**
     * set the value of a variable in real Windows environment
     *
     * @param name of the environment variable
     * @param value of the environment variable
     * @return success
     */
    public static boolean setEnvironmentVariable(String name, String value) {
      if (!SetEnvironmentVariableW(Pointer.pointerToWideCString(name), Pointer.pointerToWideCString(value))) {
        Debug.error("WinKernel32: setEnvironmentVariable: does not work for: %s = %s", name, value);
        return false;
      }
      return true;
    }
  }

  /**
   * Direct access to Windows API user32.dll via BridJ
   */
  @Library("user32")
  public static class WinUser32 {

    /*
    https://msdn.microsoft.com/pt-br/library/windows/desktop/dd375731
    VK_NUM_LOCK 0x90
    VK_SCROLL 0x91
    VK_CAPITAL 0x14
    */
    private static int WinNumLock = 0x90;
    private static int WinScrollLock = 0x91;
    private static int WinCapsLock = 0x14;

    static {
      BridJ.register();
    }

    public static boolean isNumLockOn() {
      int state = GetKeyState(WinNumLock);
      return state > 0;
    }

    public static boolean isScrollLockOn() {
      int state = GetKeyState(WinScrollLock);
      return state > 0;
    }

    public static boolean isCapsLockOn() {
      int state = GetKeyState(WinCapsLock);
      return state > 0;
    }

    /*
    https://msdn.microsoft.com/en-us/library/ms646301(VS.85).aspx
    SHORT WINAPI GetKeyState(
      _In_ int nVirtKey
    );
     */
    private static native int GetKeyState(int aVK);
  }
}
