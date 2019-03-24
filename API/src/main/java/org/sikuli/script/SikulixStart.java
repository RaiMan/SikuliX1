/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;

public class SikulixStart {

  public static boolean isVerbose() {
    return verbose;
  }

  public static void setVerbose(boolean verbose) {
    SikulixStart.verbose = verbose;
  }

  static boolean verbose = false;

  public static void log(int level, String msg, Object... args) {
    String msgShow = "[DEBUG] RunIDE: " + msg;
    if (level < 0) {
      msgShow = "[ERROR] RunIDE: " + msg;
    } else if (!verbose) {
      return;
    }
    System.out.println(String.format(msgShow, args));
  }

  private static String osName = System.getProperty("os.name").substring(0, 1).toLowerCase();

  public static File getAppPath() {
    File sxAppPath = new File("");
    File fUserDir = null;
    String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isEmpty() || !(fUserDir = new File(userHome)).exists()) {
      log(-1, "JavaSystemProperty::user.home not valid: %s", userHome);
      System.exit(-1);
    } else {
      if ("w".equals(osName)) {
        String appPath = System.getenv("APPDATA");
        if (appPath != null && !appPath.isEmpty()) {
          sxAppPath = new File(new File(appPath), "Sikulix");
        }
      } else if ("m".equals(osName)) {
        sxAppPath = new File(new File(fUserDir, "Library/Application Support"), "Sikulix");
      } else {
        sxAppPath = new File(fUserDir, ".Sikulix");
      }
      if (!sxAppPath.exists()) {
        sxAppPath.mkdirs();
      }
      if (!sxAppPath.exists()) {
        log(-1, "JavaSystemProperty::user.home not valid: %s", userHome);
        System.exit(-1);
      }
    }
    return sxAppPath;
  }

  public static File getRunningJar() {
    File jarFile = null;
    String jarName = "notKnown";
    CodeSource codeSrc = SikulixStart.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      try {
        jarName = codeSrc.getLocation().getPath();
        jarName = URLDecoder.decode(jarName, "utf8");
      } catch (UnsupportedEncodingException e) {
        log(-1, "URLDecoder: not possible: %s", jarName);
        System.exit(1);
      }
      jarFile = new File(jarName);
    }
    return jarFile;
  }

  static String jythonVersion = "2.7.1";
  static String jrubyVersion = "9.2.0.0";
  static boolean moveJython = false;
  static boolean moveJRuby = false;

  public static String makeClassPath(File jarFile) {
    log(1, "starting");
    String classPath = jarFile.getAbsolutePath();

    File[] sxFolderList = jarFile.getParentFile().listFiles(new FilenameFilter() {
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

    File sxExtensions = new File(getAppPath(), "Extensions");
    boolean extensionsOK = true;

    if (!sxExtensions.exists()) {
      sxExtensions.mkdir();
    }
    if (!sxExtensions.exists()) {
      log(1, "folder extension not available: %s", sxExtensions);
      extensionsOK = false;
    }

    boolean jythonLatest = false;
    boolean jrubyLatest = false;

    if (extensionsOK) {
      File[] fExtensions = sxExtensions.listFiles();
      if (moveJython || moveJRuby) {
        for (File fExtension : fExtensions) {
          String name = fExtension.getName();
          if ((name.contains("jython") && name.contains("standalone")) ||
                  (name.contains("jruby") && name.contains("complete"))) {
            fExtension.delete();
          }
        }
      }
      if (null != sxFolderList && sxFolderList.length > 0) {
        for (File fJar : sxFolderList) {
          try {
            Files.move(fJar.toPath(), sxExtensions.toPath().resolve(fJar.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
            log(1, "moving to extensions: %s", fJar);
          } catch (IOException e) {
            log(-1, "moving to extensions: %s (%s)", fJar, e.getMessage());
          }
        }
      }

      log(1, "looking for extension jars in: %s", sxExtensions);
      String separator = File.pathSeparator;
      for (File fExtension : fExtensions) {
        String pExtension = fExtension.getAbsolutePath();
        if (pExtension.endsWith(".jar")) {
          if (!classPath.isEmpty()) {
            classPath += separator;
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
          classPath += pExtension;
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
    return classPath;
  }
}
