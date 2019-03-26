/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.apache.commons.cli.CommandLine;
import org.sikuli.basics.FileManager;
import org.sikuli.util.CommandArgs;
import org.sikuli.util.CommandArgsEnum;
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
import java.util.Date;
import java.util.List;

public class SikulixStart {

  public static long getElapsedStart() {
    return elapsedStart;
  }

  static long elapsedStart = new Date().getTime();


  public static String getStart() {
    return start;
  }

  static String start = String.format("%d", elapsedStart);

  private static String osName = System.getProperty("os.name").substring(0, 1).toLowerCase();

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

  static String jythonVersion = "2.7.1";

  public static boolean isJythonExtern() {
    return jythonExtern;
  }

  public static void setJythonExtern(boolean jythonExtern) {
    SikulixStart.jythonExtern = jythonExtern;
  }

  static boolean jythonExtern = false;

  public static boolean hasPython() {
    return !python.isEmpty();
  }

  public static String getPython() {
    return python;
  }

  static String python = "";

  static String jrubyVersion = "9.2.0.0";

  static boolean jrubyExtern = false;

  public static String getDebugLevel() {
    return debugLevel;
  }

  private static String debugLevel = "0";

  public static String getLogFile() {
    return logFile;
  }

  private static String logFile = "";

  public static String getUserLogFile() {
    return userLogFile;
  }

  private static String userLogFile = "";

  public static String[] getLoadScripts() {
    return loadScripts;
  }

  private static String[] loadScripts = new String[0];

  public static boolean isQuiet() {
    return quiet;
  }

  private static boolean quiet = false;

  public static boolean shouldRunServer() {
    return asServer;
  }

  private static boolean asServer = false;

  public static void getArgs(String[] args) {
    CommandLine cmdLine;
    String cmdValue;

    CommandArgs cmdArgs = new CommandArgs();
    cmdLine = cmdArgs.getCommandLine(CommandArgs.scanArgs(args));

    boolean cmdLineValid = true;
    if (cmdLine == null) {
      log(-1, "Did not find any valid option on command line!");
      cmdLineValid = false;
    }

    if (cmdLineValid && cmdLine.hasOption("h")) {
      cmdArgs.printHelp();
      System.exit(0);
    }

    if (cmdLineValid && cmdLine.hasOption("v")) {
      setVerbose(true);
    }

    if (cmdLineValid && cmdLine.hasOption("q")) {
      quiet = true;
    }

    if (cmdLineValid && cmdLine.hasOption("s")) {
      asServer = true;
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.DEBUG.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      if (cmdValue != null) {
        debugLevel = cmdValue;
      }
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.LOGFILE.shortname())) {
      logFile = cmdLine.getOptionValue(CommandArgsEnum.LOGFILE.longname());
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.USERLOGFILE.shortname())) {
      userLogFile = cmdLine.getOptionValue(CommandArgsEnum.USERLOGFILE.longname());
    }

    if (cmdLineValid && cmdLine.hasOption("c")) {
      System.setProperty("sikuli.console", "false");
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.LOAD.shortname())) {
      loadScripts = cmdLine.getOptionValues(CommandArgsEnum.LOAD.longname());
    }

    if (cmdLineValid && cmdLine.hasOption(CommandArgsEnum.RUN.shortname())) {
    }
  }

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

  static boolean moveJython = false;
  static boolean moveJRuby = false;

  public static String makeClassPath(File jarFile) {
    log(1, "starting");
    boolean isDev = false;
    String jarPath = jarFile.getAbsolutePath();
    if (!jarPath.endsWith(".jar")) {
      isDev = true;
    }
    String classPath = jarPath;

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

    boolean jythonReady = false;
    boolean jrubyReady = false;

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

      //log(1, "looking for extension jars in: %s", sxExtensions);
      String separator = File.pathSeparator;
      for (File fExtension : fExtensions) {
        String pExtension = fExtension.getAbsolutePath();
        if (pExtension.endsWith(".jar")) {
          if (!classPath.isEmpty()) {
            classPath += separator;
          }
          if (pExtension.contains("jython") && pExtension.contains("standalone")) {
            if (pExtension.contains(jythonVersion)) {
              jythonReady = true;
            }
          } else if (pExtension.contains("jruby") && pExtension.contains("complete")) {
            if (pExtension.contains(jrubyVersion)) {
              jrubyReady = true;
            }
          }
          classPath += pExtension;
          log(1, "adding extension: %s", fExtension);
        }
      }
      String txtExtensions = FileManager.readFileToString(new File(sxExtensions, "extensions.txt"));
      List<String> extGiven = new ArrayList<>();
      if (!txtExtensions.isEmpty()) {
        String[] lines = txtExtensions.split("\\n");
        String extlines = "";
        for (String line : lines) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
            continue;
          }
          extlines += line + "\n";
          extGiven.add(line);
        }
        log(1, "extensions.txt\n%s", extlines);
      }
      if (extGiven.size() == 0) {
        log(1, "no extensions.txt nor valid content");
      }
      for (String line : extGiven) {
        String token = "";
        String extPath = line;
        String[] lineParts = line.split("=");
        if (lineParts.length > 1) {
          token = lineParts[0].trim();
          extPath = lineParts[1].trim();
        }
        File extFile = new File(extPath);
        if (extFile.isAbsolute() && !extFile.exists()) {
          continue;
        }
        if (!token.isEmpty()) {
          if ("jython".equals(token)) {
            jythonReady = true;
            setJythonExtern(true);
          }
          if ("python".equals(token)) {
            if (extFile.isAbsolute()) {
              if (extFile.exists()) {
                log(1, "Python available at: %s", extPath);
                SikulixStart.python = extPath;
              }
            } else {
              String runOut = ProcessRunner.run(extPath, "-V");
              if (runOut.startsWith("0\n")) {
                python = "#" + extPath;
                log(1, "Python available as command: %s (%s)", extPath, runOut.substring(2));
              }
            }
            continue;
          }
          if (!classPath.isEmpty()) {
            classPath += separator;
          }
          classPath += extPath;
          log(1, "adding extension: %s", extPath);
        }
      }
      if (!jythonReady && !jrubyReady && !isDev) {
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
    }
    return classPath;
  }
}
