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
   * @param img the task image to set.
   */
  public static void setTaksIcon(Image img) {
    /*
     * We also want to set the dock image here.
     *
     * Java 9 provides java.awt.Taskbar which is a nice abstraction to do this on multiple platforms.
     * But because we have to be Java 8 backwards compatible we have to use some reflection here to
     * get the job done properly.
     */
    try {
      if (RunTime.get().isJava9()) {
        Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
        Method isTaskbarSupported = taskbarClass.getMethod("isTaskbarSupported");
        Method getTaskbar = taskbarClass.getMethod("getTaskbar");

        if(Boolean.TRUE.equals(isTaskbarSupported.invoke(taskbarClass))) {
          Object taskbar = getTaskbar.invoke(taskbarClass);
          Method setIconImage = taskbarClass.getMethod("setIconImage", new Class[]{java.awt.Image.class});
          setIconImage.invoke(taskbar, img);
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
