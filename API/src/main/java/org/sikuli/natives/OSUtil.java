/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import org.sikuli.script.App;
import org.sikuli.script.Region;

import java.awt.Rectangle;
import java.awt.Window;
import java.util.List;
import java.util.Map;

public interface OSUtil {
  // Windows: returns PID, 0 if fails
  // Others: return 0 if succeeds, -1 if fails

  /**
   * check if needed command libraries or packages are installed and working<br>
   * if not ok, respective features will do nothing but issue error messages
   */
  public void checkFeatureAvailability();

  public App get(App app);

  public Map<Integer, String[]> getApps(String name);

	public App open(App app);

  public App switchto(App app, int num);

  public App switchto(String title);

  public App close(App app);

  public Rectangle getWindow(String titel);

  public Rectangle getWindow(App app);

  public Rectangle getWindow(App app, int winNum);

  public Rectangle getFocusedWindow();

  public List<Region> getWindows(App app);

  public void bringWindowToFront(Window win, boolean ignoreMouse);
}
