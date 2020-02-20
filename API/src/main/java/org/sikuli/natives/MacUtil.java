/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.script.runners.AppleScriptRunner;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.Runner;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MacUtil implements OSUtil {

  private static boolean _askedToEnableAX = false;

  @Override
  public void checkFeatureAvailability() {
    RunTime.get().loadLibrary("MacUtil");
  }

  /*
  tell application "System Events"
    set found to "NotFound"
    try
      set found to first item of (processes whose name is "#APP#")
      set found to first item of (processes whose unix id is equal to #PID#)
    end try
    found
  end tell
  if not found is equal to "NotFound" then
    set windowName to ""
    try
    set windowName to name of first window of application "#APP#"
    end try
    set found to {name of found, «class idux» of found, windowName}
  end if
  found
  */
  static String cmd = "set found to \"NotFound\"\n"
          + "try\n"
          + "tell application \"System Events\"\n"
          + "#LINE#\n"
          + "end tell\n"
          + "if not (found is equal to \"NotFound\") then\n"
          + "set windowName to \"\"\n"
          + "try\n"
          + "set windowName to name of first window of found\n"
          + "end try\n"
          + "set found to {name of found, «class idux» of found, windowName, file of found}\n"
          + "end if\n"
          + "end try\n"
          + "found\n";
  static String cmdLineApp = "set found to first item of (processes whose displayed name is \"#APP#\")";
  static String cmdLinePID = "set found to first item of (processes whose unix id is equal to #PID#)";

  /*
  set theWindows to {}
  repeat with win in (windows of application "#APP#" whose visible is true)
	  copy {name of win, bounds of win} to end of theWindows
  end repeat
  theWindows
  */

  private static final IScriptRunner.Options SILENT_OPTIONS = new IScriptRunner.Options().setSilent(true);

  String cmdGetWindows = "set theWindows to {}\n" +
          "repeat with win in (windows of application \"#APP#\" whose visible is true)\n" +
          "copy {name of win, bounds of win} to end of theWindows\n" +
          "end repeat\n" +
          "theWindows\n";

  @Override
  public App get(App app) {
    String name = app.getName();
    int pid = app.getPID();
    String theCmd = "";
    if (pid < 0) {
      if (!name.isEmpty()) {
        List<App> apps = getApps(name);
        if (apps.size() > 0) {
          App theApp = apps.get(0);
          app.setPID(theApp.getPID());
          app.setName(theApp.getName());
          app.setToken(theApp.getToken());
          app.setExec(theApp.getExec());
          app.setWindow(theApp.getWindowTitle());
        }
      }
      return app;
    } else {
      theCmd = cmd.replace("#LINE#", cmdLinePID);
      theCmd = theCmd.replaceAll("#PID#", "" + pid);
      int retVal = Runner.getRunner(AppleScriptRunner.class).evalScript(theCmd, SILENT_OPTIONS);
      String result = RunTime.get().getLastCommandResult().trim();
      if (retVal > -1) {
        if (!result.contains("NotFound")) {
          String[] parts = result.split(",");
          if (parts.length > 1) {
            app.setName(parts[0].trim());
            app.setPID(parts[1].trim());
          }
          if (parts.length > 2) {
            app.setWindow(parts[2].trim());
          }
          if (parts.length > 3) {
            for (int i = 3; i < parts.length; i++) {
              String part = parts[i].trim();
              if (part.startsWith("alias ")) {
                String[] folders = part.split(":");
                part = "";
                for (int nf = 1; nf < folders.length; nf++) {
                  part += "/" + folders[nf];
                }
                app.setExec(part);
                continue;
              }
              app.setWindow(app.getWindowTitle() + "," + parts);
            }
          }
        }
      } else {
        app.reset();
      }
    }
    return app;
  }

  @Override
  public boolean open(App app) {
    String appName = app.getExec().isEmpty() ? app.getName() : app.getExec();
    String cmd = "open -a \"" + appName + "\"";
    if (!app.getOptions().isEmpty()) {
      cmd += " --args " + app.getOptions();
    }
    int ret = shRun(cmd);
    return ret == 0;
  }

  @Override
  public boolean switchto(App app) {
    if (app.isValid()) {
      //osascript -e "tell app \"safari\" to activate"
      String cmd = "tell application \""
              + app.getName()
              + "\" to activate";
      return 0 == Runner.getRunner(AppleScriptRunner.class).evalScript(cmd, SILENT_OPTIONS);
    }
    return false;
  }

  @Override
  public App switchto(String title, int index) {
    //TODO switchTo window title
    return new App();
  }

  @Override
  public boolean close(App app) {
    int ret;
    if (app.getPID() > -1) {
      ret = close(app.getPID());
    } else {
      ret = close(app.getExec().startsWith(app.getName()) ? app.getName() : app.getExec());
    }
    if (ret == 0) {
      app.reset();
    }
    return ret == 0;
  }

  private static int shRun(String sCmd) {
    String cmd[] = {"sh", "-c", sCmd};
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      return -1;
    }
  }

  private int close(String appName) {
    String cmd = "ps aux |  grep \"" + appName + "\" | grep -v \"grep\" | awk '{print $2}' | xargs kill";
    return shRun(cmd);
  }

  private int close(int pid) {
    String cmd = "kill " + pid;
    return shRun(cmd);
  }

  @Override
  public Rectangle getWindow(App app) {
    return getWindow(app, 0);
  }

  @Override
  public Rectangle getWindow(App app, int winNum) {
    int pid = getPID(app.getName());
    return getWindow(pid, winNum);
  }

  @Override
  public Rectangle getWindow(String appName) {
    return getWindow(new App(appName), 0);
  }

  private Rectangle getWindow(int pid) {
    return getWindow(pid, 0);
  }

  private Rectangle getWindow(int pid, int winNum) {
    Rectangle rect = getRegion(pid, winNum);
    return rect;
  }

  @Override
  public Rectangle getFocusedWindow() {
    Rectangle rect = getFocusedRegion();
    return rect;
  }

  @Override
  public List<Region> getWindows(App app) {
    List<Region> windows = new ArrayList<>();
    String theCmd = cmdGetWindows.replace("#APP#", app.getName());
    int retVal = Runner.getRunner(AppleScriptRunner.class).evalScript(theCmd, SILENT_OPTIONS);
    String result = RunTime.get().getLastCommandResult().trim();
    if (retVal > -1 && !result.isEmpty()) {
      Debug.trace("getWindows: %s", result);
      String[] parts = result.split(",");
      int lenResult = parts.length;
      if (lenResult % 5 != 0) {
        Debug.error("getWindow: at least one window title has a comma - giving up: %s", result);
        return windows;
      }
      for (int nWin = 0; nWin < lenResult; nWin += 5) {
        try {
          int x = Integer.parseInt(parts[nWin + 1].trim());
          int y = Integer.parseInt(parts[nWin + 2].trim());
          int w = Integer.parseInt(parts[nWin + 3].trim()) - x;
          int h = Integer.parseInt(parts[nWin + 4].trim()) - y;
          Region reg = new Region(x, y, w, h);
          reg.setName(parts[nWin]);
          windows.add(reg);
        } catch (NumberFormatException e) {
          Debug.error("getWindow: invalid coordinates: %s", result);
        }
      }
    }
    return windows;
  }

  @Override
  public List<App> getApps(String name) {
    new App();
    String cmd = "tell application \"System Events\"\n" +
            "set plist to (processes whose background only is false)\n" +
            "set resultlist to {}\n" +
            "repeat with n from 1 to the length of plist\n" +
            "set proc to item n of plist\n" +
            "set pwin to \"\"\ntry\nset pwin to name of first window of proc\nend try\n" +
            "set entry to {pwin as text, \"|||\", «class idux» of proc as text," +
            "displayed name of proc as text, name of proc as text, get file of proc, \"###\"}\n" +
            "set end of resultlist to entry\n" +
            "end repeat\n" +
            "end tell\n" +
            "resultlist";
    int retVal = Runner.getRunner(AppleScriptRunner.class).evalScript(cmd, SILENT_OPTIONS);
    String result = RunTime.get().getLastCommandResult().trim();
    String[] processes = (", " + result).split(", ###");
    List<App> appList = new ArrayList<>();
    int pid = 0;
    for (String process : processes) {
      if (process.startsWith(", ")) {
        process = process.substring(2);
      }
      App theApp = new App();
      String[] parts = process.split(", \\|\\|\\|,");
      String pWin = parts[0].trim();
      parts = parts[1].split(",");
      try {
        pid = Integer.parseInt(parts[0].trim());
      } catch (NumberFormatException e) {
        pid--;
      }
      String dispName = parts[1].trim();
      String procName = parts[2].trim();
      String[] pAlias = parts[3].split(":");
      String pExec = pAlias[pAlias.length - 1];
      String pToken = String.format("%s|%s|%s", dispName, procName, pExec);
      if (name.isEmpty() || pToken.toUpperCase().contains(name.toUpperCase())) {
        theApp.setName(dispName);
        theApp.setPID(pid);
        theApp.setToken(pToken);
        theApp.setExec(pExec);
        theApp.setWindow(pWin);
        appList.add(theApp);
      }
    }
    return appList;
  }

  public static native int getPID(String appName);

  public static native Rectangle getRegion(int pid, int winNum);

  public static native Rectangle getFocusedRegion();
}
