/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import org.sikuli.basics.Debug;
import org.sikuli.script.App;
import org.sikuli.script.Key;
import org.sikuli.script.RunTime;
import org.sikuli.script.Screen;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WinUtil implements OSUtil {

  @Override
  public void checkFeatureAvailability() {
    RunTime.loadLibrary("WinUtil");
  }

  @Override
  public App.AppEntry getApp(int appPID, String appName) {
    if (appPID == 0) {
      return null;
    }
    App.AppEntry app = null;
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
            name = "\"" + parts[0] +  "\"";
          }
        } else {
          parts = name.split(" ");
          if (parts.length > 1) {
            options = name.substring(parts[0].length() + 1);
            name = parts[0];
          }
        }
        if (name.startsWith("\"")) {
          execName = new File(name.substring(1, name.length()-1)).getName().toUpperCase();
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
      cmd = String.format("!tasklist /V /FO CSV /NH /FI \"IMAGENAME eq %s\"", execName);
      result = RunTime.get().runcmd(cmd);
      lines = result.split("\r\n");
      if ("0".equals(lines[0].trim())) {
        for (int nl = 1; nl < lines.length; nl++) {
          parts = lines[nl].split("\"");
          if (parts.length < 2) {
            continue;
          }
          String theWindow = parts[parts.length - 1];
          String theName = parts[1];
          String thePID = parts[3];
          if (theWindow.contains("N/A")) continue;
          app = new App.AppEntry(theName, thePID, theWindow, "", "");
          break;
        }
      }
    }
    return app;
  }

  @Override
  public Map<Integer, String[]> getApps(String name) {
    Map<Integer, String[]> apps = new HashMap<Integer, String[]>();
    String cmd;
    if (name == null || name.isEmpty()) {
      cmd = cmd = "!tasklist /V /FO CSV /NH /FI \"SESSIONNAME eq Console\"";
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
        String theWindow = parts[parts.length - 1];
        String thePID = parts[3];
        String theName = parts[1];
        Integer pid = -1;
        try {
          pid = Integer.parseInt(thePID);
        } catch (Exception ex) {
        }
        if (pid != -1) {
          if (theWindow.contains("N/A")) {
            pid = -pid;
          }
          apps.put(pid, new String[] {theName, theWindow});
        }
      }
    } else {
      Debug.logp(result);
    }
    return apps;
  }

  @Override
  public int isRunning(App.AppEntry app) {
    if (app.pid > 0) {
      return 1;
    }
    if (app.name.isEmpty()) {
      return -1;
    }
    if (getWindow(app.name, 0) != null) {
      return 1;
    }
    App.AppEntry ae = getApp(app.pid, app.name);
    if (ae != null && ae.pid > 0) {
      return 1;
    }
    return 0;
  }

  @Override
  public int open(String appName) {
    int pid = openApp(appName);
    return pid < 1 ? -1 : pid;
  }

  @Override
  public int open(App.AppEntry app) {
    if (app.pid > -1) {
      return switchApp(app.pid, 0);
    }
    String cmd = app.execName;
    if (!app.options.isEmpty()) {
      cmd += " " + app.options;
    }
    int pid = openApp(cmd);
    return pid < 1 ? -1 : pid;
  }

  @Override
  public int switchto(String appName) {
    return switchApp(appName, 0);
  }

  @Override
  public int switchto(String appName, int winNum) {
    return switchApp(appName, winNum);
  }

  @Override
  public int switchto(int pid, int num) {
    return switchApp(pid, num);
  }

  @Override
  public int switchto(App.AppEntry app, int num) {
    if (app.pid > -1) {
      String wname = app.window;
      if (wname.startsWith("!")) {
        wname = wname.substring(1);
      }
      return switchto(wname, 0);
    }
    if (app.window.startsWith("!")) {
      String token = app.window.substring(1);
      if(!token.isEmpty()) {
        return switchto(token, 0);
      } else {
        App.AppEntry newApp = getApp(app.pid, app.name);
        if (newApp == null) {
          return switchto(app.execName, 0);
        } else {
          return switchto(newApp.window, 0);
        }
      }
    }
    return switchto(app.execName, num);
  }

  @Override
  public int close(String appName) {
    return closeApp(appName);
  }

  @Override
  public int close(int pid) {
    return closeApp(pid);
  }

  @Override
  public int close(App.AppEntry app) {
    if (app.pid > -1) {
      return closeApp(app.pid);
    }
    if (app.window.startsWith("!")) {
      String token = app.window.substring(1);
      if(!token.isEmpty()) {
        switchto(app.window.substring(1), 0);
        RunTime.pause(1);
        new Screen().type(Key.F4, Key.ALT);
        return 0;
      } else {
        app = getApp(app.pid, app.name);
      }
    }
    if (app != null) {
      if (app.pid > -1) {
        return closeApp(app.pid);
      } else {
        return closeApp(app.execName.replaceAll("\"", ""));
      }
    } else {
      return -1;
    }
  }

  public native int switchApp(String appName, int num);

  public native int switchApp(int pid, int num);

  public native int openApp(String appName);

  public native int closeApp(String appName);

  public native int closeApp(int pid);

  @Override
  public Rectangle getWindow(String appName) {
    return getWindow(appName, 0);
  }

  @Override
  public Rectangle getWindow(int pid) {
    return getWindow(pid, 0);
  }

  @Override
  public Rectangle getWindow(String appName, int winNum) {
    long hwnd = getHwnd(appName, winNum);
    return _getWindow(hwnd, winNum);
  }

  @Override
  public Rectangle getWindow(int pid, int winNum) {
    long hwnd = getHwnd(pid, winNum);
    return _getWindow(hwnd, winNum);
  }

  @Override
  public Rectangle getFocusedWindow() {
    Rectangle rect = getFocusedRegion();
    return rect;
  }

  @Override
  public native void bringWindowToFront(Window win, boolean ignoreMouse);

  private static native long getHwnd(String appName, int winNum);

  private static native long getHwnd(int pid, int winNum);

  private static native Rectangle getRegion(long hwnd, int winNum);

  private static native Rectangle getFocusedRegion();

  private Rectangle _getWindow(long hwnd, int winNum) {
    Rectangle rect = getRegion(hwnd, winNum);
    return rect;
  }
}
