/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.ide;

/*
 * TODO Uncomment as soon as we ditch Java 8 support
 */
import org.sikuli.ide.SikulixIDE;

import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.io.File;
import java.util.List;

public class IDEDesktopSupport implements AboutHandler, PreferencesHandler, QuitHandler, OpenFilesHandler {

  public static List<File> filesToOpen = null;
  public static boolean showAbout = true;
  public static boolean showPrefs = true;
  public static boolean showQuit = true;

  public static void init() {

    if(Taskbar.isTaskbarSupported()) {
      Taskbar taskbar = Taskbar.getTaskbar();

      if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
        taskbar.setIconImage(Toolkit.getDefaultToolkit().createImage(
            SikulixIDE.class.getResource("/icons/sikulix-icon.png")));
      }
    }

    if (Desktop.isDesktopSupported()) {
      IDEDesktopSupport support = new IDEDesktopSupport();
      Desktop desktop = Desktop.getDesktop();

      if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
        desktop.setAboutHandler(support);
        showAbout = false;
      }

      if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
        desktop.setPreferencesHandler(support);
        showPrefs = false;
      }

      if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
        desktop.setQuitHandler(support);
        showQuit = false;
      }

      if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
        desktop.setOpenFileHandler(support);
      }
    }
  }

  @Override
  public void handleAbout(AboutEvent e) {
    //TODO about dialog
//    new SXDialog("sxideabout", SikulixIDE.getWindowTop(), SXDialog.POSITION.TOP).run();
  }

  @Override
  public void handlePreferences(PreferencesEvent e) {
    SikulixIDE.get().showPreferencesWindow();
  }

  @Override
  public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
    if (!SikulixIDE.get().terminate()) {
      response.cancelQuit();
    } else {
      response.performQuit();
    }
  }

  @Override
  public void openFiles(OpenFilesEvent e) {
    filesToOpen = e.getFiles();
    for (File f : filesToOpen) {
      //TODO DesktopAction :: openFiles
    }
  }
}

