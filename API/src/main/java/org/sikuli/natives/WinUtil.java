/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.script.App;
import org.sikuli.script.Region;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.runners.ProcessRunner;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.jna.platform.win32.Kernel32;

public class WinUtil implements OSUtil {

  static final int BUFFERSIZE = 32 * 1024 - 1;
  static final Kernel32 kernel32 = Kernel32.INSTANCE;
  static final SXUser32 sxuser32 = SXUser32.INSTANCE;
  static final User32 user32 = User32.INSTANCE;
  static final Psapi psapi = Psapi.INSTANCE;

  public static final class ProcessInfo {
    private int pid;
    private String imageName;

    public ProcessInfo(
        final int pid,
        final String imageName) {
      this.pid = pid;
      this.imageName = imageName;
    }

    public int getPid() {
      return pid;
    }

    public String getImageName() {
      return imageName;
    }
  }

  public static final class WindowInfo {
    public HWND hwnd;
    public int pid;
    public String title;

    public WindowInfo(HWND hwnd, int pid, String title) {
      super();
      this.hwnd = hwnd;
      this.pid = pid;
      this.title = title;
    }

    public HWND getHwnd() {
      return hwnd;
    }

    public int getPid() {
      return pid;
    }

    public String getTitle() {
      return title;
    }
  }

  public static List<WindowInfo> allWindows() {
    /* Initialize the empty window list. */
    final List<WindowInfo> windows = new ArrayList<>();

    /* Enumerate all of the windows and add all of the one for the
     * given process id to our list. */
    boolean result = user32.EnumWindows(
        new WinUser.WNDENUMPROC() {
          public boolean callback(
              final HWND hwnd, final Pointer data) {

            if (user32.IsWindowVisible(hwnd)) {
              IntByReference windowPid = new IntByReference();
              user32.GetWindowThreadProcessId(hwnd, windowPid);

              String windowTitle = getWindowTitle(hwnd);

              windows.add(new WindowInfo(hwnd, windowPid.getValue(), windowTitle));
            }

            return true;
          }
        },
        null);

    /* Handle errors. */
    if (!result && Kernel32.INSTANCE.GetLastError() != 0) {
      throw new RuntimeException("Couldn't enumerate windows.");
    }

    /* Return the window list. */
    return windows;
  }

  public static String getWindowTitle(HWND hWnd) {
    char[] text = new char[1024];
    int length = user32.GetWindowText(hWnd, text, 1024);
    return length > 0 ? new String(text, 0, length) : null;
  }

  public static String getTopWindowTitle(int pid) {
    List<WindowInfo> windows = getWindowsForPid(pid);
    if (!windows.isEmpty()) {
      return getWindowsForPid(pid).get(0).getTitle();
    }
    return null;
  }

  public static List<ProcessInfo> allProcesses() {
    List<ProcessInfo> processList = new ArrayList<ProcessInfo>();

    HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
        Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0));

    try {
      Tlhelp32.PROCESSENTRY32.ByReference pe
          = new Tlhelp32.PROCESSENTRY32.ByReference();
      for (boolean more = Kernel32.INSTANCE.Process32First(snapshot, pe);
           more;
           more = Kernel32.INSTANCE.Process32Next(snapshot, pe)) {
        int pid = pe.th32ProcessID.intValue();
        String name = getProcessImageName(pe.th32ProcessID.intValue());
        if (null == name) {
          continue;
        }
        processList.add(new ProcessInfo(pid, name));
      }
      return processList;
    } finally {
      Kernel32.INSTANCE.CloseHandle(snapshot);
    }
  }

  private static String getProcessImageName(int pid) {
    HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(
        0x1000,
        false,
        pid);

    if (hProcess != null) {
      try {
        char[] imageNameChars = new char[1024];
        IntByReference imageNameLen
            = new IntByReference(imageNameChars.length);
        if (Kernel32.INSTANCE.QueryFullProcessImageName(hProcess, 0, imageNameChars, imageNameLen)) {
          String name = FilenameUtils.getName(new String(imageNameChars, 0, imageNameLen.getValue()));
          return name;
        }
        return null;
      } finally {
        Kernel32.INSTANCE.CloseHandle(hProcess);
      }
    }
    return null;
  }

  private static List<WindowInfo> getWindowsForPid(int pid) {
    return allWindows().stream().filter((w) -> w.getPid() == pid).collect(Collectors.toList());
  }

  private static List<WindowInfo> getWindowsForName(String name) {
    return allWindows().stream().filter((w) -> {
      String imageName = getProcessImageName(w.getPid());

      if (imageName != null && imageName.equals(name + ".exe")) {
        return true;
      }

      String windowTitle = w.getTitle();

      if (windowTitle != null && windowTitle.contains(name)) {
        return true;
      }

      return false;
    }).collect(Collectors.toList());
  }

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

  /*
  https://msdn.microsoft.com/pt-br/library/windows/desktop/dd375731
  VK_NUM_LOCK 0x90
  VK_SCROLL 0x91
  VK_CAPITAL 0x14
  */
  private static int WinNumLock = 0x90;
  private static int WinScrollLock = 0x91;
  private static int WinCapsLock = 0x14;

  public static int isNumLockOn() {
    int state = sxuser32.GetKeyState(WinNumLock);
    return state;
  }

  public static int isScrollLockOn() {
    int state = sxuser32.GetKeyState(WinScrollLock);
    return state;
  }

  public static int isCapsLockOn() {
    int state = sxuser32.GetKeyState(WinCapsLock);
    return state;
  }

  @Override
  public void checkFeatureAvailability() {
    RunTime.loadLibrary("WinUtil");
  }

  @Override
  public App get(App app) {
    app.log("Win:get(App): %s", app);
    if (app.getPID() > 0) {
      app = getTaskByPID(app);
    } else {
      app = getTaskByName(app);
    }
    return app;
  }

  private static App getTaskByName(App app) {

    if (app.isWindow()) {
      return getTaskByWindow(app);
    } else {
      new File(app.getExec()).getName();
      String appName = app.getToken().isEmpty() ? new File(app.getExec()).getName() : app.getToken();

      app.log("Win:getTaskByName: %s", appName);
      List<ProcessInfo> processes = allProcesses();

      for (ProcessInfo p : processes) {
        if (p.getImageName() != null && p.getImageName().equals(appName)) {
          app.setPID(p.getPid());
          app.setWindow(getTopWindowTitle(p.getPid()));
          return app;
        }
      }
      return app;
    }
  }

  private static App getTaskByPID(App app) {
    if (!app.isValid()) {
      return app;
    }

    app.log("Win:getTaskByPID: %s", app.getPID());
    List<ProcessInfo> processes = allProcesses();

    for (ProcessInfo p : processes) {
      if (p.getPid() == app.getPID()) {
        app.setWindow(getTopWindowTitle(p.getPid()));
        return app;
      }
    }

    app.reset();
    return app;
  }

  private static App getTaskByWindow(App app) {
    app.log("Win:getTaskByWindow: %s", app.getName());
    String title = app.getName();

    List<WindowInfo> windows = allWindows();

    for (WindowInfo window : windows) {
      String windowTitle = window.getTitle();

      if (windowTitle != null && windowTitle.contains(title)) {
        int pid = window.getPid();
        app.setPID(pid);
        app.setWindow(windowTitle);
        List<ProcessInfo> processes = allProcesses();
        for (ProcessInfo pInfo : processes) {
          if (pid == pInfo.getPid()) {
            app.setNameGiven(app.getName());
            app.setName(pInfo.getImageName());
          }
        }
        return app;
      }
    }

    return app;
  }

  @Override
  public List<App> getApps(String name) {
    List<App> apps = new ArrayList<>();

    List<ProcessInfo> processes = allProcesses();

    for (ProcessInfo p : processes) {
      if (p.getImageName().contains(name)) {
        String winTitle = getTopWindowTitle(p.getPid());
        if (winTitle == null) continue;
        App theApp = new App();
        theApp.setName(p.getImageName());
        theApp.setWindow(winTitle);
        theApp.setPID(p.getPid());
        apps.add(theApp);
      }
    }
    return apps;
  }

  @Override
  public boolean open(App app) {
    if (app.isValid()) {
      return 0 == switchApp(app.getPID(), 0);
    } else {
      String cmd = app.getExec();
      String workDir = app.getWorkDir();
      if (!app.getOptions().isEmpty()) {
        return start(cmd, workDir, app.getOptions());
      } else {
        return start(cmd, workDir);
      }
    }
  }

  private boolean start(String... cmd) {
    return ProcessRunner.startApp(cmd);
  }

  @Override
  public boolean switchto(App app) {
    if (!app.isValid()) {
      return false;
    }
    int loopCount = 0;
    while (loopCount < 100) {
      int pid = switchApp(app.getPID(), 0);
      if (pid > 0) {
        if (pid == app.getPID()) {
          app.setFocused(true);
          getTaskByPID(app);
          return true;
        }
      } else {
        break;
      }
      loopCount++;
    }
    return false;
  }

  @Override
  public App switchto(String title, int index) {
    App app = new App();
    int pid = switchApp(title, index);
    if (pid > 0) {
      app.setPID(pid);
      return getTaskByPID(app);
    }
    return app;
  }

  @Override
  public boolean close(App app) {
    return ProcessRunner.closeApp("" + app.getPID());
//    if (closeApp(app.getPID()) == 0) {
//      app.reset();
//      return true;
//    }
  }

  //TODO implement using JNA
  public native int closeApp(int pid);

  @Override
  public Rectangle getWindow(App app) {
    return getWindow(app, 0);
  }

  @Override
  public Rectangle getWindow(App app, int winNum) {
    get(app);
    if (!app.isValid()) {
      return null;
    }
    return getWindow(app.getPID(), winNum);
  }

  @Override
  public Rectangle getWindow(String title) {
    return getWindow(title, 0);
  }

  private Rectangle getWindow(String title, int winNum) {
    HWND hwnd = getHwnd(title, winNum);
    return hwnd != null ? _getWindow(hwnd, winNum) : null;
  }

  private Rectangle getWindow(int pid, int winNum) {
    HWND hwnd = getHwnd(pid, winNum);
    return hwnd != null ? _getWindow(hwnd, winNum) : null;
  }

  private Rectangle _getWindow(HWND hwnd, int winNum) {
    Rectangle rect = getRegion(hwnd, winNum);
    return rect;
  }

  @Override
  public Rectangle getFocusedWindow() {
    return getFocusedRegion();
  }

  @Override
  public List<Region> getWindows(App app) {
    app = get(app);
    List<WindowInfo> windows = getWindowsForPid(app.getPID());

    List<Region> regions = new ArrayList<>();

    for (WindowInfo w : windows) {
      regions.add(Region.create(_getWindow(w.getHwnd(), 0)));
    }

    return regions;
  }

  private int switchAppWindow(WindowInfo window) {
    HWND hwnd = window.getHwnd();

    WinUser.WINDOWPLACEMENT lpwndpl = new WinUser.WINDOWPLACEMENT();

    user32.GetWindowPlacement(hwnd, lpwndpl);

    if (lpwndpl.showCmd == WinUser.SW_SHOWMINIMIZED || lpwndpl.showCmd == WinUser.SW_MINIMIZE) {
      user32.ShowWindow(hwnd, WinUser.SW_RESTORE);
    }

    boolean success = user32.SetForegroundWindow(hwnd);

    if (success) {
      user32.SetFocus(hwnd);
      IntByReference windowPid = new IntByReference();
      user32.GetWindowThreadProcessId(hwnd, windowPid);

      return windowPid.getValue();
    } else {
      return 0;
    }
  }

  public int switchApp(String appName, int num) {
    List<WindowInfo> windows = getWindowsForName(appName);

    if (windows.size() > num) {
      return switchAppWindow(windows.get(num));
    }

    return 0;
  }

  public int switchApp(int pid, int num) {
    List<WindowInfo> windows = getWindowsForPid(pid);

    if (windows.size() > num) {
      return switchAppWindow(windows.get(num));
    }

    return 0;
  }

  private static HWND getHwnd(String appName, int winNum) {
    List<WindowInfo> windows = getWindowsForName(appName);

    if (windows.size() > winNum) {
      return windows.get(winNum).getHwnd();
    }
    return null;
  }

  private static HWND getHwnd(int pid, int winNum) {
    List<WindowInfo> windows = getWindowsForPid(pid);

    if (windows.size() > winNum) {
      return windows.get(winNum).getHwnd();
    }
    return null;
  }

  private static Rectangle getRegion(HWND hwnd, int winNum) {
    RECT rect = new User32.RECT();
    boolean success = user32.GetWindowRect(hwnd, rect);
    return success ? rect.toRectangle() : null;
  }

  private static Rectangle getFocusedRegion() {
    HWND hwnd = user32.GetForegroundWindow();
    RECT rect = new User32.RECT();
    boolean success = user32.GetWindowRect(hwnd, rect);
    return success ? rect.toRectangle() : null;
  }
}