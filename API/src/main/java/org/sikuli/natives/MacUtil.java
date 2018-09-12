/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

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
  static String cmd = "tell application \"System Events\"\n"
          + "set found to \"NotFound\"\n"
          + "try\n"
          + "#LINE#\n"
          + "end try\n"
          + "end tell\n"
          + "if not found is equal to \"NotFound\" then\n"
          + "set windowName to \"\"\n"
          + "try\n"
          + "set windowName to name of first window of application (name of found)\n"
          + "end try\n"
          + "set found to {name of found, «class idux» of found, windowName}\n"
          + "end if\n" +
            "found\n";
  static String cmdLineApp = "set found to first item of (processes whose name is \"#APP#\")";
  static String cmdLinePID = "set found to first item of (processes whose unix id is equal to #PID#)";

  @Override
  public App getApp(App app) {
    String name = "";
    String theCmd = "";
    int pid = -1;
    Object filter;
    if (app.getPID() < 0) {
      filter = app.getName();
    } else {
      filter = app.getPID();
    }
    if (filter instanceof String) {
      name = (String) filter;
      theCmd = cmd.replace("#LINE#", cmdLineApp);
      theCmd = theCmd.replaceAll("#APP#", name);
    } else if (filter instanceof Integer) {
      pid = (Integer) filter;
      theCmd = cmd.replace("#LINE#", cmdLinePID);
      theCmd = theCmd.replaceAll("#PID#", "" + pid);
    } else {
      return app;
    }
    int retVal = Runner.runas(theCmd, true);
    String result = RunTime.get().getLastCommandResult();
    String title = "???";
    String sPid = "-1";
    String sName = "NotKnown";
    if (retVal > -1) {
      if (!result.contains("NotFound")) {
        String[] parts = result.split(",");
        if (parts.length > 1) {
          sName = parts[0];
          sPid = parts[1];
        }
        if (parts.length > 2) {
          title = parts[2];
        }
        if (parts.length > 3) {
          for (int i = 3; i < parts.length; i++) {
            title += "," + parts[i];
          }
        }
        //app = new App.AppEntry(sName.trim(), sPid.trim(), title.trim(), "", "");
        app.setName(sName.trim());
        app.setPID(sPid.trim());
        app.setWindow(title.trim());
      }
    }
    return app;
  }

  @Override
  public App open(App app) {
    String appName = app.getExec().startsWith(app.getName()) ? app.getName() : app.getExec();
    int retval = 0;
    if (runTime.osVersion.startsWith("10.10.")) {
      if (Runner.runas(String.format("tell app \"%s\" to activate", appName), true) != 0) {
        retval = -1;
      }
    } else {
      retval = _openApp(appName) ? 0 : -1;
    }
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

  private int close(String appName) {
    try {
      String cmd[] = {"sh", "-c",
              "ps aux |  grep \"" + appName + "\" | awk '{print $2}' | xargs kill"};
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      return -1;
    }
  }

  private int close(int pid) {
    try {
      String cmd[] = {"sh", "-c", "kill " + pid};
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      return -1;
    }
  }

  private void checkAxEnabled(String name) {
    if (!System.getProperty("os.name").toLowerCase().startsWith("mac")) {
      return;
    }
    if (Integer.parseInt(System.getProperty("os.version").replace(".", "")) > 108 && !isAxEnabled()) {
      if (name == null) {
        JOptionPane.showMessageDialog(null,
                "This app uses Sikuli feature " + usedFeature + ", which needs\n"
                + "access to the Mac's assistive device support.\n"
                + "You have to explicitly allow this in the System Preferences.\n"
                + "(System Preferences -> Security & Privacy -> Privacy)\n"
                + "Currently we cannot do this for you.\n\n"
                + "Be prepared to get some crash after clicking ok.\n"
                + "Please check the System Preferences and come back.",
                "SikuliX on Mac Mavericks Special", JOptionPane.PLAIN_MESSAGE);
        System.out.println("[error] MacUtil: on Mavericks: no access to assistive device support");
      }
      usedFeature = name;
      return;
    }
    if (!isAxEnabled()) {
      if (_askedToEnableAX) {
        return;
      }
      int ret = JOptionPane.showConfirmDialog(null,
              "You need to enable Accessibility API to use the function \""
              + name + "\".\n"
              + "Should I open te System Preferences for you?",
              "Accessibility API not enabled",
              JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
      if (ret == JOptionPane.YES_OPTION) {
        openAxSetting();
        JOptionPane.showMessageDialog(null,
                "Check \"Enable access for assistant devices\""
                + "in the System Preferences\n and then close this dialog.",
                "Enable Accessibility API", JOptionPane.INFORMATION_MESSAGE);
      }
      _askedToEnableAX = true;
    }
  }

//Mac Mavericks: delete app entry from list - in terminal on one line
//sudo sqlite3 /Library/Application\ Support/com.apple.TCC/Tcc.db
//'delete from access where client like "%part of app name%"'

  @Override
  public Rectangle getWindow(App app) {
    return getWindow(app, 0);
  }

  @Override
  public Rectangle getWindow(App app, int winNum) {
    checkAxEnabled("getWindow");
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
    checkAxEnabled(null);
    return rect;
  }

  @Override
  public Rectangle getFocusedWindow() {
    checkAxEnabled("getFocusedWindow");
    Rectangle rect = getFocusedRegion();
    checkAxEnabled(null);
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
