/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.basics.Debug;
import org.sikuli.util.ProcessRunner;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Sikulix {

  static boolean verbose = false;
  static String jarName = "";
  static File sxFolder = null;
  static File fAppData;
  static File fDirExtensions = null;
  static File[] fExtensions = null;
  static List<String> extensions = new ArrayList<>();
  static String ClassPath = "";
  static String start = String.format("%d", new Date().getTime());
  static String osName = System.getProperty("os.name").substring(0, 1).toLowerCase();
  static String jythonVersion = "2.7.1";
  static String jrubyVersion = "9.2.0.0";
  static boolean moveJython = false;
  static boolean moveJRuby = false;
  static boolean jythonLatest = false;
  static boolean jrubyLatest = false;

  public static void main(String[] args) {
    CodeSource codeSrc = SikuliIDE.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      try {
        jarName = codeSrc.getLocation().getPath();
        jarName = URLDecoder.decode(jarName, "utf8");
      } catch (UnsupportedEncodingException e) {
        log(-1, "URLDecoder: not possible: %s", jarName);
        System.exit(1);
      }
      sxFolder = new File(jarName).getParentFile();
    }

    if (args.length > 0 && args[0].equals("-v")) {
      verbose = true;
      args[0] += start;
    }

    fAppData = makeAppData();
    log(1, "Running: %s", jarName);
    log(1, "AppData: %s", fAppData);

    if (jarName.endsWith(".jar")) {
      log(1, "starting");
      ClassPath = jarName;
    } else {
      SikulixRunIDE.main(args);
      return;
    }
    File[] sxFolderList = sxFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.endsWith(".jar")) {
          if (name.contains("jython") && name.contains("standalone")) {
            moveJython = true;
            return true;
          }
          if (name.contains("jruby") && name.contains("complete")) {
            moveJRuby = true;
            return true;
          }
        }
        return false;
      }
    });

    fDirExtensions = new File(fAppData, "Extensions");
    boolean extensionsOK = true;

    if (!fDirExtensions.exists()) {
      fDirExtensions.mkdir();
    }

    if (!fDirExtensions.exists()) {
      log(1, "folder extension not available: %s", fDirExtensions);
      extensionsOK = false;
    }

    if (extensionsOK) {
      fExtensions = fDirExtensions.listFiles();
      if (moveJython || moveJRuby) {
        for (File fExtension : fExtensions) {
          String name = fExtension.getName();
          if ((name.contains("jython") && name.contains("standalone")) ||
                  (name.contains("jruby") && name.contains("complete"))) {
            fExtension.delete();
          }
        }
      }
      if (sxFolderList.length > 0) {
        for (File fJar : sxFolderList) {
          try {
            Files.move(fJar.toPath(), fDirExtensions.toPath().resolve(fJar.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
            log(1, "moving to extensions: %s", fJar);
          } catch (IOException e) {
            log(-1, "moving to extensions: %s (%s)", fJar, e.getMessage());
          }
        }
      }

      log(1, "looking for extension jars in: %s", fDirExtensions);
      String separator = File.pathSeparator;
      fExtensions = fDirExtensions.listFiles();
      for (File fExtension : fExtensions) {
        String pExtension = fExtension.getAbsolutePath();
        if (pExtension.endsWith(".jar")) {
          if (!ClassPath.isEmpty()) {
            ClassPath += separator;
          }
          if (pExtension.contains("jython") && pExtension.contains("standalone")) {
            if (pExtension.contains(jythonVersion)) {
              jythonLatest = true;
            }
          } else if (pExtension.contains("jruby") && pExtension.contains("complete")) {
            if (pExtension.contains(jrubyVersion)) {
              jrubyLatest = true;
            }
          }
          ClassPath += pExtension;
          extensions.add(pExtension);
          log(1, "adding extension: %s", fExtension);
        }
      }
    }
    if (!jythonLatest && !jrubyLatest) {
      String message = "Neither Jython nor JRuby available" +
              "\nIDE not yet useable with JavaScript only" +
              "\nPlease consult the docs for a solution";
      if (!verbose) {
        JOptionPane.showMessageDialog(null, message, "IDE not useable", JOptionPane.ERROR_MESSAGE);
      } else {
        log(-1, message);
      }
      System.exit(-1);
    }

    List<String> cmd = new ArrayList<>();
    cmd.add("java");
//    if ("m".equals(osName)) {
//      cmd.add("-Xdock:name=SikuliX");
//      cmd.add("-Xdock:icon=\"" + new File(fAppData, "SikulixLibs/sikulix.icns").getAbsolutePath() + "\"");
//    }
    cmd.add("-cp");
    cmd.add(ClassPath);
    cmd.add("org.sikuli.ide.SikulixRunIDE");
    cmd.addAll(Arrays.asList(args));
    int exitValue = ProcessRunner.detach(cmd);
    log(1, "terminating: returned: %d", exitValue);
    System.exit(exitValue);
  }

  private static void log(int level, String msg, Object... args) {
    String msgShow = "[DEBUG] RunIDE: " + msg;
    if (level < 0) {
      msgShow = "[ERROR] RunIDE: " + msg;
    } else if (!verbose) {
      return;
    }
    System.out.println(String.format(msgShow, args));
  }

  private static File makeAppData() {
    File fSikulixAppPath = new File("");
    File fAppPath = new File("");
    File fUserDir = null;
    String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isEmpty() || !(fUserDir = new File(userHome)).exists()) {
      log(-1, "JavaSystemProperty::user.home not valid: %s", userHome);
      System.exit(-1);
    } else {
      if ("w".equals(osName)) {
        String appPath = System.getenv("APPDATA");
        if (appPath != null && !appPath.isEmpty()) {
          fAppPath = new File(appPath);
          fSikulixAppPath = new File(fAppPath, "Sikulix");
        }
      } else if ("m".equals(osName)) {
        fAppPath = new File(fUserDir, "Library/Application Support");
        fSikulixAppPath = new File(fAppPath, "Sikulix");
      } else {
        fAppPath = fUserDir;
        fSikulixAppPath = new File(fAppPath, ".Sikulix");
      }
      if (!fSikulixAppPath.exists()) {
        fSikulixAppPath.mkdirs();
      }
      if (!fSikulixAppPath.exists()) {
        log(-1, "JavaSystemProperty::user.home not valid: %s", userHome);
        System.exit(-1);
      }
    }
    return fSikulixAppPath;
  }

  protected static void prepareMac() {
    try {
      // set the brushed metal look and feel, if desired
      System.setProperty("apple.awt.brushMetalLook", "true");

      // use the mac system menu bar
      System.setProperty("apple.laf.useScreenMenuBar", "true");

      // set the "About" menu item name
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiStar");

      // use smoother fonts
      System.setProperty("apple.awt.textantialiasing", "true");

      // ref: http://developer.apple.com/releasenotes/Java/Java142RNTiger/1_NewFeatures/chapter_2_section_3.html
      System.setProperty("apple.awt.graphics.EnableQ2DX", "true");

      // use the system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // put your debug code here ...
    }
  }
}
