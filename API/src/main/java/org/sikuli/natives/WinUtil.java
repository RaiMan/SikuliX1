/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

public class WinUtil extends GenericOsUtil {

  static final SXUser32 user32 = SXUser32.INSTANCE;

  private static final class WinWindow extends OsWindow {
    private HWND hWnd;

    public WinWindow(HWND hWnd) {
      this.hWnd = hWnd;
    }

    @Override
    public OsProcess getProcess() {
      IntByReference pid = new IntByReference();
      user32.GetWindowThreadProcessId(hWnd, pid);

      Optional<ProcessHandle> handle = ProcessHandle.of(pid.getValue());

      if (handle.isPresent()) {
        return new GenericOsProcess(ProcessHandle.of(pid.getValue()).get());
      }

      return null;
    }

    @Override
    public String getTitle() {
      char[] text = new char[1024];
      int length = user32.GetWindowText(hWnd, text, 1024);
      return length > 0 ? new String(text, 0, length) : "";
    }

    @Override
    public Rectangle getBounds() {
      RECT rect = new User32.RECT();
      boolean success = user32.GetWindowRect(hWnd, rect);
      return success ? rect.toRectangle() : null;
    }

    @Override
    public boolean focus() {
      WinUser.WINDOWPLACEMENT lpwndpl = new WinUser.WINDOWPLACEMENT();

      user32.GetWindowPlacement(hWnd, lpwndpl);

      if (lpwndpl.showCmd == WinUser.SW_SHOWMINIMIZED || lpwndpl.showCmd == WinUser.SW_MINIMIZE) {
        user32.ShowWindow(hWnd, WinUser.SW_RESTORE);
      }

      boolean success = user32.SetForegroundWindow(hWnd);

      if (success) {
        return (user32.SetFocus(hWnd) != null);
      }

      return false;
    }

    @Override
    public boolean focus(int winNum) {
      return focus();
    }

    @Override
    public boolean minimize() {
      return user32.ShowWindow(hWnd, WinUser.SW_MINIMIZE);
    }

    @Override
    public boolean maximize() {
      return user32.ShowWindow(hWnd, WinUser.SW_MAXIMIZE);
    }

    @Override
    public boolean restore() {
      return user32.ShowWindow(hWnd, WinUser.SW_RESTORE);
    }

    @Override
    public boolean equals(Object other) {
      return other != null && other instanceof WinWindow && this.hWnd.equals(((WinWindow) other).hWnd);
    }
  }

  @Override
  public List<OsWindow> getWindows() {
    return allWindows();
  }

  @Override
  public List<OsWindow> getAppWindows() { //TODO
    return allWindows();
  }

  @Override
  public List<OsWindow> findWindows(String title) {
    if (StringUtils.isNotBlank(title)) {
      return allWindows().stream().filter((w) -> w.getTitle().contains(title)).collect(Collectors.toList());
    }
    return new ArrayList<>(0);
  }

  @Override
  public List<OsWindow> getWindows(OsProcess process) {
    if (process != null) {
      return allWindows().stream().filter((w) -> process.equals(w.getProcess())).collect(Collectors.toList());
    }
    return new ArrayList<>(0);
  }

  @Override
  public OsWindow getFocusedWindow() {
    HWND hWnd = user32.GetForegroundWindow();
    return new WinWindow(hWnd);
  }

  private List<OsWindow> allWindows() {
    /* Initialize the empty window list. */
    final List<OsWindow> windows = new ArrayList<>();

    boolean result = user32.EnumWindows(new WinUser.WNDENUMPROC() {
      public boolean callback(final HWND hWnd, final Pointer data) {
        // Only visible and top level. Ensures that top level window is at index 0
        if (user32.IsWindowVisible(hWnd) && user32.GetWindow(hWnd, new DWORD(WinUser.GW_OWNER)) == null) {
          final WinWindow win = new WinWindow(hWnd);
          if (!win.getTitle().isEmpty() && !win.getBounds().isEmpty()) {
            windows.add(win);
          }

          // get child windows as well
/*
          user32.EnumChildWindows(hWnd, new WinUser.WNDENUMPROC() {
            public boolean callback(final HWND hWnd, final Pointer data) {
              if (user32.IsWindowVisible(hWnd)) {
                final WinWindow win = new WinWindow(hWnd);
                if (!win.getTitle().isEmpty() && !win.getBounds().isEmpty()) {
                  windows.add(win);
                }
              }

              return true;
            }
          }, null);
*/
        }

        return true;
      }
    }, null);

    /* Handle errors. */
    if (!result && Kernel32.INSTANCE.GetLastError() != 0) {
      throw new RuntimeException("Couldn't enumerate windows.");
    }

    return windows;
  }

  @Override
  public OsProcess getFocusedProcess() {
    final OsWindow focusedWindow = getFocusedWindow();
    return focusedWindow.getProcess();
  }

  @Override
  public boolean isUserApp(OsProcess process) { //TODO
    return true;
  }

  // https://msdn.microsoft.com/pt-br/library/windows/desktop/dd375731
  // VK_NUM_LOCK 0x90
  // VK_SCROLL 0x91
  // VK_CAPITAL 0x14

  public static int isNumLockOn() {
    int winNumLock = 0x90;
    return user32.GetKeyState(winNumLock);
  }

  public static int isScrollLockOn() {
    int winScrollLock = 0x91;
    return user32.GetKeyState(winScrollLock);
  }

  public static int isCapsLockOn() {
    int winCapsLock = 0x14;
    return user32.GetKeyState(winCapsLock);
  }

  static final int BUFFERSIZE = 32 * 1024 - 1;
  static final Kernel32 kernel32 = Kernel32.INSTANCE;

  public static String getEnv(String envKey) {
    char[] retChar = new char[BUFFERSIZE];
    String envVal = null;
    int retInt = kernel32.GetEnvironmentVariable(envKey, retChar, BUFFERSIZE);
    if (retInt > 0) {
      envVal = new String(Arrays.copyOfRange(retChar, 0, retInt));
    }
    return envVal;
  }

  public static String setEnv(String envKey, String envVal) {
    boolean retOK = kernel32.SetEnvironmentVariable(envKey, envVal);
    if (retOK) {
      return getEnv(envKey);
    }
    return null;
  }
}