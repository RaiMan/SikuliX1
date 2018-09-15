/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import org.sikuli.basics.Debug;
import org.sikuli.script.App;
import org.sikuli.script.RunTime;
import org.sikuli.script.Runner;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MacUtil implements OSUtil {

  private static boolean _askedToEnableAX = false;
  private String usedFeature;
  private static RunTime runTime = null;

  @Override
  public void checkFeatureAvailability() {
    runTime = RunTime.get();
    RunTime.loadLibrary("MacUtil");
    checkAxEnabled();
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
          + "if not found is equal to \"NotFound\" then\n"
          + "set windowName to \"\"\n"
          + "set windowName to name of first window of application (name of found)\n"
          + "set found to {name of found, «class idux» of found, windowName}\n"
          + "end if\n"
          + "end try\n"
          + "found\n";
  static String cmdLineApp = "set found to first item of (processes whose name is \"#APP#\")";
  static String cmdLinePID = "set found to first item of (processes whose unix id is equal to #PID#)";

  @Override
  public App getApp(App app) {
    String name = app.getName();
    int pid = app.getPID();
    String theCmd = "";
    if (pid < 0) {
      if (!name.isEmpty()) {
        theCmd = cmd.replace("#LINE#", cmdLineApp);
        theCmd = theCmd.replaceAll("#APP#", name);
      } else {
        return app;
      }
    } else {
      theCmd = cmd.replace("#LINE#", cmdLinePID);
      theCmd = theCmd.replaceAll("#PID#", "" + pid);
    }
    int retVal = Runner.runas(theCmd, true);
    String result = RunTime.get().getLastCommandResult().trim();
    if (retVal > -1) {
      if (!result.contains("NotFound")) {
        String[] parts = result.split(",");
        if (parts.length > 1) {
          app.setName(parts[0].trim());
          app.setPID(parts[1].trim());
        }
        if (parts.length > 2) {
          app.setWindow(parts[2]);
        }
        if (parts.length > 3) {
          for (int i = 3; i < parts.length; i++) {
            app.setWindow(app.getWindow() + "," + parts[i]);
          }
        }
      } else {
        app.reset();
      }
    }
    return app;
  }

  @Override
  public App open(App app) {
    String appName = app.getExec().startsWith(app.getName()) ? app.getName() : app.getExec();
    String cmd = "open -a " + appName;
    if (!app.getOptions().isEmpty()) {
      cmd += " --args " + app.getOptions();
    }
    int retval = shRun(cmd);
    if (retval == 0) {
      retval = getPID(appName);
    }
    return app;
  }

  @Override
  public App switchto(App app, int num) {
    return open(app);
  }

  @Override
  public App switchto(String appName) {
    return open(new App(appName));
  }

  @Override
  public App close(App app) {
    int ret = 0;
    if (app.getPID() > -1) {
      ret = close(app.getPID());
    } else {
      ret = close(app.getExec().startsWith(app.getName()) ? app.getName() : app.getExec());
    }
    if (ret == 0) {
      app.reset();
    }
    return app;
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

  private void checkAxEnabled() {
    if (runTime.isOSX10() && !isAxEnabled()) {
      JOptionPane.showMessageDialog(null,
              "SikuliX needs access to the Mac's assistive device support.\n"
                      + "You have to explicitly allow this in the System Preferences.\n"
                      + "(... -> Security & Privacy -> Privacy -> Accessibility)\n"
                      + "Please check the System Preferences and come back.",
              "macOS Accessibility", JOptionPane.ERROR_MESSAGE);
      runTime.terminate(-1, "App: MacUtil: no access to assistive device support");
    }
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
  public native void bringWindowToFront(Window win, boolean ignoreMouse);

  public static native boolean _openApp(String appName);

  public static native int getPID(String appName);

  public static native Rectangle getRegion(int pid, int winNum);

  public static native Rectangle getFocusedRegion();

  public static native boolean isAxEnabled();

  public static native void openAxSetting();

  @Override
  public Map<Integer, String[]> getApps(String name) {
    return null;
  }
}
