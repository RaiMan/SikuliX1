/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import org.sikuli.script.App;

import java.awt.*;
import java.util.Map;

public interface OSUtil {
  // Windows: returns PID, 0 if fails
  // Others: return 0 if succeeds, -1 if fails

  /**
   * check if needed command libraries or packages are installed and working<br>
   * if not ok, respective features will do nothing but issue error messages
   */
  public void checkFeatureAvailability();

  public App.AppEntry getApp(int pid, String name);

  public Map<Integer, String[]> getApps(String name);

  public int isRunning(App.AppEntry app);

	public int open(String appName);

  public int open(App.AppEntry app);

  // Windows: returns PID, 0 if fails
  // Others: return 0 if succeeds, -1 if fails
  public int switchto(String appName);

  public int switchto(String appName, int winNum);

  //internal use
  public int switchto(int pid, int num);

  public int switchto(App.AppEntry app, int num);

  // returns 0 if succeeds, -1 if fails
  public int close(String appName);

  //internal use
  public int close(int pid);

  public int close(App.AppEntry app);

  public Rectangle getWindow(String appName);

  public Rectangle getWindow(String appName, int winNum);

  Rectangle getWindow(int pid);

  Rectangle getWindow(int pid, int winNum);

  public Rectangle getFocusedWindow();

  public void bringWindowToFront(Window win, boolean ignoreMouse);
}
