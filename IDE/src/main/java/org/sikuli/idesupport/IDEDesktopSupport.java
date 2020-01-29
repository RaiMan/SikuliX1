/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

/*
 * TODO Uncomment as soon as we ditch Java 8 support
 */
//import java.awt.Desktop;
//import java.awt.desktop.AboutEvent;
//import java.awt.desktop.AboutHandler;
//import java.awt.desktop.OpenFilesEvent;
//import java.awt.desktop.OpenFilesHandler;
//import java.awt.desktop.PreferencesEvent;
//import java.awt.desktop.PreferencesHandler;
//import java.awt.desktop.QuitEvent;
//import java.awt.desktop.QuitHandler;
//import java.awt.desktop.QuitResponse;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.Sikulix;
import org.sikuli.script.support.RunTime;

/**
 * Native desktop support.
 * <p>
 * Currently this class has a lot of reflective code to make it compiling
 * and working on Java 8.
 * The sections that can be removed after we ditch Java 8 support
 * are marked with TODOs.
 */
public class IDEDesktopSupport implements InvocationHandler { //, AboutHandler, PreferencesHandler, QuitHandler, OpenFilesHandler { // TODO Activate as soon as we ditch Java 8 support.

  static SikulixIDE ide = null;

  private static String me = "IDE: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  public static List<File> macOpenFiles = null;
  public static boolean showAbout = true;
  public static boolean showPrefs = true;
  public static boolean showQuit = true;

  public static void init(SikulixIDE theIDE) {
    ide = theIDE;

    IDEDesktopSupport support = new IDEDesktopSupport();

    /*
     * Same code as below in plain Java 9 for reference.
     * Because we have to be Java 8 backwards compatible,
     * we have to use reflection to use Java 9 features.
     * TODO As soon as we ditch Java 8 support, we can remove
     * all the reflection and use this code snippet instead.
     */
//    if(Desktop.isDesktopSupported()) {
//      Desktop desktop = Desktop.getDesktop();
//
//      if(desktop.isSupported(Desktop.Action.APP_ABOUT)){
//        desktop.setAboutHandler(support);
//        showAbout = false;
//      }
//
//      if(desktop.isSupported(Desktop.Action.APP_PREFERENCES)){
//        desktop.setPreferencesHandler(support);
//        showPrefs = false;
//      }
//
//      if(desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)){
//        desktop.setQuitHandler(support);;
//        showQuit = false;
//      }
//
//      if(desktop.isSupported(Desktop.Action.APP_OPEN_FILE)){
//        desktop.setOpenFileHandler(support);
//      }
//    }

    try {
      if (RunTime.get().isJava9()) {
        Class<?> clDesktop = Class.forName("java.awt.Desktop");

        if (Boolean.TRUE.equals(clDesktop.getMethod("isDesktopSupported").invoke(clDesktop))) {
          Method getDesktop = clDesktop.getMethod("getDesktop");
          Object desktop = getDesktop.invoke(clDesktop);

          Class<?> clAboutHandler = Class.forName("java.awt.desktop.AboutHandler");
          Class<?> clPreferencesHandler = Class.forName("java.awt.desktop.PreferencesHandler");
          Class<?> clQuitHandler = Class.forName("java.awt.desktop.QuitHandler");
          Class<?> clOpenHandler = Class.forName("java.awt.desktop.OpenFilesHandler");

          Object appHandler = Proxy.newProxyInstance(
                  clDesktop.getClassLoader(),
                  new Class[]{clAboutHandler, clPreferencesHandler, clQuitHandler, clOpenHandler},
                  support);

          @SuppressWarnings({"rawtypes", "unchecked"})
          Class<Enum> clDesktopAction = (Class<Enum>) Class.forName("java.awt.Desktop$Action");

          @SuppressWarnings("unchecked")
          Object appAboutEnum = Enum.valueOf(clDesktopAction, "APP_ABOUT");
          if (Boolean.TRUE.equals(clDesktop.getMethod("isSupported", clDesktopAction).invoke(desktop, appAboutEnum))) {
            Method m = clDesktop.getMethod("setAboutHandler", clAboutHandler);
            m.invoke(desktop, appHandler);
            showAbout = false;
          }

          @SuppressWarnings("unchecked")
          Object appPreferencesEnum = Enum.valueOf(clDesktopAction, "APP_PREFERENCES");
          if (Boolean.TRUE.equals(clDesktop.getMethod("isSupported", clDesktopAction).invoke(desktop, appPreferencesEnum))) {
            Method m = clDesktop.getMethod("setPreferencesHandler", clPreferencesHandler);
            m.invoke(desktop, appHandler);
            showPrefs = false;
          }

          @SuppressWarnings("unchecked")
          Object appQuitHandlerEnum = Enum.valueOf(clDesktopAction, "APP_QUIT_HANDLER");
          if (Boolean.TRUE.equals(clDesktop.getMethod("isSupported", clDesktopAction).invoke(desktop, appQuitHandlerEnum))) {
            Method m = clDesktop.getMethod("setQuitHandler", clQuitHandler);
            m.invoke(desktop, new Object[]{appHandler});
            showQuit = false;
          }

          @SuppressWarnings("unchecked")
          Object appOpenFileHandlerEnum = Enum.valueOf(clDesktopAction, "APP_OPEN_FILE");
          if (Boolean.TRUE.equals(clDesktop.getMethod("isSupported", clDesktopAction).invoke(desktop, appOpenFileHandlerEnum))) {
            Method m = clDesktop.getMethod("setOpenFileHandler", clOpenHandler);
            m.invoke(desktop, appHandler);
          }
        }
      } else if (Settings.isMac()) {
        /*
         * Special Java 8 handling for MacOS.
         * TODO Remove this as soon we ditch Java 8 support.
         */
        Class<?> comAppleEawtApplication = Class.forName("com.apple.eawt.Application");
        Method mGetApplication = comAppleEawtApplication.getMethod("getApplication");
        Object instApplication = mGetApplication.invoke(comAppleEawtApplication);

        Class<?> clAboutHandler = Class.forName("com.apple.eawt.AboutHandler");
        Class<?> clPreferencesHandler = Class.forName("com.apple.eawt.PreferencesHandler");
        Class<?> clQuitHandler = Class.forName("com.apple.eawt.QuitHandler");
        Class<?> clOpenHandler = Class.forName("com.apple.eawt.OpenFilesHandler");

        Object appHandler = Proxy.newProxyInstance(
                comAppleEawtApplication.getClassLoader(),
                new Class[]{clAboutHandler, clPreferencesHandler, clQuitHandler, clOpenHandler},
                support);
        Method m = comAppleEawtApplication.getMethod("setAboutHandler", clAboutHandler);
        m.invoke(instApplication, appHandler);
        showAbout = false;
        m = comAppleEawtApplication.getMethod("setPreferencesHandler", clPreferencesHandler);
        m.invoke(instApplication, appHandler);
        showPrefs = false;
        m = comAppleEawtApplication.getMethod("setQuitHandler", clQuitHandler);
        m.invoke(instApplication, appHandler);
        showQuit = false;
        m = comAppleEawtApplication.getMethod("setOpenFileHandler", clOpenHandler);
        m.invoke(instApplication, appHandler);
      }

    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
      String em = String.format("initNativeSupport: Mac: error:\n%s", ex.getMessage());
      log(-1, em);
      Sikulix.popError(em, "IDE has problems ...");
      System.exit(1);
    }
  }

  /*
   * Keep this for reference.
   * TODO As soon as we ditch Java 8 support we can use this
   * Java 9 implementation.
   */
//  @Override
//  public void openFiles(OpenFilesEvent e) {
//    log(lvl, "nativeSupport: should open files");
//    macOpenFiles = e.getFiles();
//    for (File f : macOpenFiles) {
//      log(lvl, "nativeSupport: openFiles: %s", macOpenFiles);
//    }
//  }
//
//  @Override
//  public void handleAbout(AboutEvent e) {
//    ide.showAbout();
//  }
//
//  @Override
//  public void handlePreferences(PreferencesEvent e) {
//    ide.showPreferencesWindow();
//  }
//
//  @Override
//  public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
//    if (!ide.quit()) {
//      response.cancelQuit();
//    } else {
//      response.performQuit();
//    }
//  }


  /*
   * Reflective invocation handler.
   * TODO Can be replaced by the above methods as soon as we ditch Java 8 support.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mName = method.getName();
    if ("handleAbout".equals(mName)) {
      ide.showAbout();
    } else if ("handlePreferences".equals(mName)) {
      ide.showPreferencesWindow();
    } else if ("openFiles".equals(mName)) {
      log(lvl, "nativeSupport: openfiles: not implemented");
//      try {
//        Method mOpenFiles = args[0].getClass().getMethod("getFiles", new Class[]{});
//        macOpenFiles = (List<File>) mOpenFiles.invoke(args[0]);
//        log(lvl, "nativeSupport: openFiles: %s", macOpenFiles);
//      } catch (Exception ex) {
//        log(lvl, "NativeSupport: Quit: error: %s", ex.getMessage());
//        System.exit(1);
//      }
    } else if ("handleQuitRequestWith".equals(mName)) {
      try {
        Class<?> comMacQuitResponse;
        if (RunTime.get().isJava9()) {
          comMacQuitResponse = Class.forName("java.awt.desktop.QuitResponse");
        } else {
          comMacQuitResponse = Class.forName("com.apple.eawt.QuitResponse");
        }
        Method mCancelQuit = comMacQuitResponse.getMethod("cancelQuit");
        Method mPerformQuit = comMacQuitResponse.getMethod("performQuit");
        Object resp = args[1];
        if (!ide.quit()) {
          mCancelQuit.invoke(resp);
        } else {
          mPerformQuit.invoke(resp);
        }
      } catch (Exception ex) {
        log(lvl, "NativeSupport: Quit: error: %s", ex.getMessage());
        System.exit(1);
      }
    }
    return new Object();
  }
}
