/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.sikuli.basics.Debug;
import org.sikuli.script.App;
import org.sikuli.script.Region;
import org.sikuli.script.RunTime;
import org.sikuli.util.ProcessRunner;

import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.platform.win32.Kernel32;

public class WinUtil implements OSUtil {

  static final int BUFFERSIZE = 32 * 1024 - 1;
  static final Kernel32 kernel32 = Kernel32.INSTANCE;
  static final SXUser32 sxuser32 = SXUser32.INSTANCE;

  public static String getEnv(String envKey) {
    char[] retChar = new char[BUFFERSIZE];
    String envVal = null;
    int retInt = kernel32.GetEnvironmentVariable(envKey, retChar, BUFFERSIZE);
    if (retInt > 0) {
      envVal = new String(Arrays.copyOfRange(retChar, 0, retInt));
    }
    sxuser32.EnumWindows(new WinUser.WNDENUMPROC() {
      @Override
      public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
        return false;
      }
    }, null);
    return envVal;
  }

  public static void allWindows() {
    sxuser32.EnumWindows(new WinUser.WNDENUMPROC() {
      @Override
      public boolean callback(WinDef.HWND hwnd, Pointer pointer) {
        return false;
      }
    }, null);
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
    if (app.getPID() > 0) {
      app = getTaskByPID(app);
    } else {
      app = getTaskByName(app);
    }
    return app;
  }

  //<editor-fold desc="old getApp">
/*
  @Override
  public App getApp(App app) {
    if (app.getPID() == 0) {
      return app;
    }
    Object filter;
    if (appPID < 0) {
      filter = appName;
    } else {
      filter = appPID;
    }
    String name = "";
    String execName = "";
    String options = "";
    Integer pid = -1;
    String[] parts;
    if (filter instanceof String) {
      name = (String) filter;
      if (name.startsWith("!")) {
        name = name.substring(1);
        execName = name;
      } else {
        if (name.startsWith("\"")) {
          parts = name.substring(1).split("\"");
          if (parts.length > 1) {
            options = name.substring(parts[0].length() + 3);
            name = "\"" + parts[0] + "\"";
          }
        } else {
          parts = name.split(" -- ");
          if (parts.length > 1) {
            options = parts[1];
            name = parts[0];
          }
        }
        if (name.startsWith("\"")) {
          execName = new File(name.substring(1, name.length() - 1)).getName().toUpperCase();
        } else {
          execName = new File(name).getName().toUpperCase();
        }
      }
    } else if (filter instanceof Integer) {
      pid = (Integer) filter;
    } else {
      return app;
    }
    Debug.log(3, "WinUtil.getApp: %s", filter);
    String cmd;
    if (pid < 0) {
      cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\"";
    } else {
      cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"PID eq " + pid.toString() + "\"";
    }
    String result = RunTime.get().runcmd(cmd);
    String[] lines = result.split("\r\n");
    if ("0".equals(lines[0].trim())) {
      for (int nl = 1; nl < lines.length; nl++) {
        parts = lines[nl].split("\"");
        if (parts.length < 2) {
          continue;
        }
        String theWindow = parts[parts.length - 1];
        String theName = parts[1];
        String thePID = parts[3];
        //Debug.log(3, "WinUtil.getApp: %s:%s(%s)", thePID, theName, theWindow);
        if (!name.isEmpty()) {
          if ((theName.toUpperCase().contains(execName) && !theWindow.contains("N/A"))
                  || theWindow.contains(name)) {
            return new App.AppEntry(theName, thePID, theWindow, "", "");
          }
        } else {
          try {
            if (Integer.parseInt(thePID) == pid) {
              return new App.AppEntry(theName, thePID, theWindow, "", "");
            }
          } catch (Exception ex) {
          }
        }
      }
    } else {
      Debug.logp(result);
    }
    if (!options.isEmpty()) {
      return new App.AppEntry(name, "", "", "", options);
    }
    if (app == null) {
      List<String> theApp = getTaskByName(execName);
      if (theApp.size() > 0) {
        app = new App.AppEntry(theApp.get(0), theApp.get(1), theApp.get(2), "", "");
      }
    }
    return app;
  }
*/
  //</editor-fold>

  private static App getTaskByName(App app) {
    String cmd = String.format("!tasklist /V /FO CSV /NH /FI \"IMAGENAME eq %s\"",
            (app.getToken().isEmpty() ? app.getName() + ".exe" : app.getToken()));
    String sysout = RunTime.get().runcmd(cmd);
    String[] lines = sysout.split("\r\n");
    String[] parts = null;
    app.reset();
    if ("0".equals(lines[0].trim())) {
      for (int n = 1; n < lines.length; n++) {
        parts = lines[n].split("\"");
        if (parts.length < 2) {
          continue;
        }
        if (parts[parts.length - 1].contains("N/A")) continue;
        app.setPID(parts[3]);
        app.setWindow(parts[parts.length - 1]);
        break;
      }
    }
    return app;
  }

  private static App getTaskByPID(App app) {
    if (!app.isValid()) {
      return app;
    }
    String[] name_pid_window = evalTaskByPID(app.getPID());
    if (name_pid_window[1].isEmpty()) {
      app.reset();
    } else {
      app.setWindow(name_pid_window[2]);
    }
    return app;
  }

  private static String[] evalTaskByPID(int pid) {
    String cmd = String.format("!tasklist /V /FO CSV /NH /FI \"PID eq %d\"", pid);
    String sysout = RunTime.get().runcmd(cmd);
    String[] lines = sysout.split("\r\n");
    String[] parts = null;
    if ("0".equals(lines[0].trim())) {
      for (int n = 1; n < lines.length; n++) {
        parts = lines[n].split("\"");
        if (parts.length < 2) {
          continue;
        }
        return new String[]{parts[1], "pid", parts[parts.length - 1]}; //name, window
      }
    }
    return new String[]{"", "", ""};
  }

  private static App getTaskByWindow(String title) {
    App app = new App();
    return app;
  }

  @Override
  public List<App> getApps(String name) {
    List<App> apps = new ArrayList<>();
    String cmd;
    if (name == null || name.isEmpty()) {
      cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\" /FI \"status eq running\" /FI \"username ne N/A\"";
    } else {
      cmd = String.format("!tasklist /V /FO CSV /NH /FI \"IMAGENAME eq %s\"", name);
    }
    String result = RunTime.get().runcmd(cmd);
    String[] lines = result.split("\r\n");
    if ("0".equals(lines[0].trim())) {
      for (int nl = 1; nl < lines.length; nl++) {
        String[] parts = lines[nl].split("\"");
        if (parts.length < 3) {
          continue;
        }
        String thePID = parts[3].trim();
        Integer pid = -1;
        try {
          pid = Integer.parseInt(thePID);
        } catch (Exception ex) {
        }
        String theWindow = parts[parts.length - 1].trim();
        if (pid != -1) {
          if (theWindow.contains("N/A")) {
            pid = -pid;
          }
          App theApp = new App();
          theApp.setName(parts[1].trim());
          theApp.setWindow(theWindow);
          theApp.setPID(pid);
          apps.add(theApp);
        }
      }
    } else {
      Debug.logp(result);
    }
    return apps;
  }

  @Override
  public boolean open(App app) {
    if (app.isValid()) {
      return 0 == switchApp(app.getPID(), 0);
    } else {
      String cmd = app.getExec();
      if (!app.getOptions().isEmpty()) {
        start(cmd, app.getOptions());
      } else {
        start(cmd);
      }
    }
    return true;
  }

  private int start(String... cmd) {
    return ProcessRunner.startApp(cmd);
  }

  @Override
  public boolean switchto(App app) {
    if (!app.isValid()) {
      return false;
    }
    int loopCount = 0;
    while (loopCount < 100) {
      int pid = switchApp(app.getWindow(), 0);
      if (pid > 0) {
        if (pid == app.getPID()) {
          app.setFocused(true);
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
      String[] name_pid_window = evalTaskByPID(pid);
      app.setName(name_pid_window[0]);
      app.setWindow(name_pid_window[2]);
    }
    return app;
  }

  @Override
  public boolean close(App app) {
    if (closeApp(app.getPID()) == 0) {
      app.reset();
      return true;
    }
    return false;
  }

  @Override
  public Rectangle getWindow(App app) {
    return getWindow(app, 0);
  }

  @Override
  public Rectangle getWindow(App app, int winNum) {
    get(app);
    if (!app.isValid()) {
      return new Rectangle();
    }
    return getWindow(app.getPID(), winNum);
  }

  @Override
  public Rectangle getWindow(String title) {
    return getWindow(title, 0);
  }

  private Rectangle getWindow(String title, int winNum) {
    long hwnd = getHwnd(title, winNum);
    return _getWindow(hwnd, winNum);
  }

  private Rectangle getWindow(int pid, int winNum) {
    long hwnd = getHwnd(pid, winNum);
    return _getWindow(hwnd, winNum);
  }

  private Rectangle _getWindow(long hwnd, int winNum) {
    Rectangle rect = getRegion(hwnd, winNum);
    return rect;
  }

  @Override
  public Rectangle getFocusedWindow() {
    Rectangle rect = getFocusedRegion();
    return rect;
  }

  @Override
  public List<Region> getWindows(App app) {
    return new ArrayList<>();
  }

  public native int switchApp(String appName, int num);

  public native int switchApp(int pid, int num);

  public native int openApp(String appName);

  public native int closeApp(String appName);

  public native int closeApp(int pid);

  @Override
  public native void bringWindowToFront(Window win, boolean ignoreMouse);

  private static native long getHwnd(String appName, int winNum);

  private static native long getHwnd(int pid, int winNum);

  private static native Rectangle getRegion(long hwnd, int winNum);

  private static native Rectangle getFocusedRegion();
}
