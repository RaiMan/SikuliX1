/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
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
  void checkFeatureAvailability();

  App get(App app);

  List<App> getApps(String name);

	boolean open(App app);

  boolean switchto(App app);

  App switchto(String title, int index);

  boolean close(App app);

  Rectangle getWindow(String titel);

  Rectangle getWindow(App app);

  Rectangle getWindow(App app, int winNum);

  Rectangle getFocusedWindow();

  List<Region> getWindows(App app);
}
