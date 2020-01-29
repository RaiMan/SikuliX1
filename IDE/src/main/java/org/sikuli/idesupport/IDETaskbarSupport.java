/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sikuli.basics.Settings;
import org.sikuli.script.support.RunTime;

public class IDETaskbarSupport {

  /**
   * Sets the task icon in the OS task bar.
   *
   * This class contains some reflective code that can be removed
   * as soon as we ditch Java 8 support.
   *
   * @param img the task image to set.
   */

  public static void setTaksIcon(Image img) {
    /*
     * Java 9 provides java.awt.Taskbar which is a nice abstraction to do this on multiple platforms.
     * But because we have to be Java 8 backwards compatible we have to use some reflection here to
     * get the job done properly.
     *
     * TODO Replace reflective code with code snippet below as soon as we ditch Java 8 support.
     */

//    if(Taskbar.isTaskbarSupported()) {
//      Taskbar taskbar = Taskbar.getTaskbar();
//
//      if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
//        taskbar.setIconImage(img);
//      }
//    }

    try {
      if (RunTime.get().isJava9()) {
        Class<?> clTaskbar = Class.forName("java.awt.Taskbar");
        Method isTaskbarSupported = clTaskbar.getMethod("isTaskbarSupported");
        Method getTaskbar = clTaskbar.getMethod("getTaskbar");

        if(Boolean.TRUE.equals(isTaskbarSupported.invoke(clTaskbar))) {
          Object taskbar = getTaskbar.invoke(clTaskbar);

          @SuppressWarnings({ "rawtypes", "unchecked" })
          Class<Enum> clDesktopFeature = (Class<Enum>) Class.forName("java.awt.Taskbar$Feature");

          @SuppressWarnings("unchecked")
          Object iconImageEnum = Enum.valueOf(clDesktopFeature, "ICON_IMAGE");
          if(Boolean.TRUE.equals(clTaskbar.getMethod("isSupported", clDesktopFeature).invoke(taskbar, iconImageEnum))) {
            Method setIconImage = clTaskbar.getMethod("setIconImage", new Class[]{java.awt.Image.class});
            setIconImage.invoke(taskbar, img);
          }
        }
      } else if (Settings.isMac()) { // special handling for MacOS if we are on Java 8
        Class<?> appClass = Class.forName("com.apple.eawt.Application");
        Method getApplication = appClass.getMethod("getApplication");
        Object application = getApplication.invoke(appClass);
        Method setDockIconImage = appClass.getMethod("setDockIconImage", new Class[]{java.awt.Image.class});
        setDockIconImage.invoke(application, img);
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
      // Just ignore this if not supported
    }
  }
}
