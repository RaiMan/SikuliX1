package org.sikuli.idesupport;

import org.sikuli.ide.SikuliIDE;

import java.awt.Desktop;
import java.awt.desktop.*;

/**
 * This class only compiles with Java 9+
 * If you want to compile with Java 8, you have to hide this classfile<br>
 * (is possible, because it is accessed in Java 9 only using reflection)
 */
public class IDEMacSupport implements AboutHandler, PreferencesHandler, QuitHandler, OpenFilesHandler {

  static SikuliIDE ide = null;

  public static void support(SikuliIDE theIDE) {
    ide = theIDE;
    IDEMacSupport macSupport = new IDEMacSupport();
    Desktop desktop = Desktop.getDesktop();
    desktop.setAboutHandler(macSupport);
    desktop.setPreferencesHandler(macSupport);
    desktop.setQuitHandler(macSupport);
    desktop.setOpenFileHandler(macSupport);
  }

  @Override
  public void openFiles(OpenFilesEvent e) {
//    log(lvl, "nativeSupport: should open files");
//    macOpenFiles = e.getFiles();
//    for (File f : macOpenFiles) {
//      log(lvl, "nativeSupport: openFiles: %s", macOpenFiles);
//    }
  }

  @Override
  public void handleAbout(AboutEvent e) {
    ide.doAbout();
  }

  @Override
  public void handlePreferences(PreferencesEvent e) {
    ide.showPreferencesWindow();
  }

  @Override
  public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
    if (!ide.quit()) {
      response.cancelQuit();
    } else {
      response.performQuit();
    }
  }
}
