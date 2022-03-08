/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import java.io.*;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public class SikulixRun {
  public static void main(String[] args) {

    File fUserHome = null;
    String aFolder = System.getProperty("user.home");
    if (aFolder == null || aFolder.isEmpty() || !(fUserHome = new File(aFolder)).exists()) {
      System.out.println("[Error] JavaSystemProperty::user.home not valid");
      System.exit(1);
    }

    File fWorkDir = null;
    aFolder = System.getProperty("user.dir");
    if (aFolder == null || aFolder.isEmpty() || !(fWorkDir = new File(aFolder)).exists()) {
      System.out.println("[Error] JavaSystemProperty::user.dir not valid");
      System.exit(1);
    }

    String jarName = "";
    CodeSource codeSrc = SikulixRun.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      try {
        jarName = codeSrc.getLocation().getPath();
        jarName = URLDecoder.decode(jarName, "utf8");
      } catch (UnsupportedEncodingException e) {
        System.out.println("[Error] URLDecoder: not possible: " + jarName);
        System.exit(1);
      }
    }

    int indexJar = 0;
    boolean verbose = false;
    if (args.length > 0) {
      if (args[0].startsWith("-v")) {
        verbose = true;
        indexJar = 1;
      }
    }

    File jarFile = new File(jarName);
    File jarFolder = jarFile.getParentFile();
    File sikulixJython = new File(jarFolder, "sikulixjython.jar");
    File fSikulixjar = null;
    if (!sikulixJython.exists()) {
      String sikulixjar = "";
      if (args.length > indexJar) {
        sikulixjar = args[indexJar]; //absolute path to a sikulix....jar
        fSikulixjar = new File(sikulixjar);
        if (!fSikulixjar.isAbsolute()) {
          if (!(fSikulixjar = new File(fWorkDir, sikulixjar)).exists()) {
            fSikulixjar = new File(fUserHome, sikulixjar);
          }
        }
        if (!fSikulixjar.exists()) {
          System.out.println("[Error] given sikulix....jar not found: " + fSikulixjar);
          System.exit(1);
        }
      } else {
        File appPath = getAppPath();
        if (null != appPath) {
          String lastUsedJar = readFileToString(new File(appPath, "SikulixStore/lastUsedJar.txt"));
          if (lastUsedJar == null) {
            appPath = null;
          } else {
            fSikulixjar = new File(lastUsedJar);
          }
        }
        if (appPath == null || !fSikulixjar.exists()) {
          System.out.println("[Error] sikulix....jar not specified and not found elsewhere");
          System.exit(1);
        }
      }
    } else {
      fSikulixjar = sikulixJython;
    }

    String separator = File.pathSeparator;
    List<String> finalArgs = new ArrayList<>();
    if (verbose) {
      System.out.println("Using as sikulix....jar: " + fSikulixjar);
      finalArgs.add("-v");
    }
    finalArgs.add("-r");
    finalArgs.add(jarName.replace(".jar", ".executablejar"));

    List<String> cmd = new ArrayList<>();
    System.getProperty("java.home");
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("windows")) {
      cmd.add(System.getProperty("java.home") + "\\bin\\java.exe");
    } else {
      cmd.add(System.getProperty("java.home") + "/bin/java");
    }
    cmd.add("-Dfile.encoding=UTF-8");
    cmd.add("-cp");
    cmd.add(jarName + separator + fSikulixjar.getAbsolutePath());
    cmd.add("org.sikuli.script.Sikulix");
    cmd.addAll(finalArgs);

    runBlocking(cmd);
  }


  private static int runBlocking(List<String> cmd) {
    int exitValue = 0;
    if (cmd.size() > 0) {
      ProcessBuilder app = new ProcessBuilder();
      app.command(cmd);
      app.redirectInput(ProcessBuilder.Redirect.INHERIT);
      app.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      Process process = null;
      try {
        process = app.start();
      } catch (Exception e) {
        System.out.println("[Error] ProcessRunner: start: " + e.getMessage());
      }

      try {
        if (process != null) {
          process.waitFor();
          exitValue = process.exitValue();
        }
      } catch (InterruptedException e) {
        System.out.println("[Error] ProcessRunner: waitFor: " + e.getMessage());
      }
    }
    return exitValue;
  }

  private static String readFileToString(File fPath) {
    StringBuilder result = new StringBuilder();
    String sResult;
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(fPath));
      char[] buf = new char[1024];
      int r = 0;
      while ((r = reader.read(buf)) != -1) {
        result.append(buf, 0, r);
      }
      sResult = result.toString().trim();
    } catch (Exception e) {
      sResult = null;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
        }
      }
    }
    return sResult;
  }

  public static File getAppPath() {
    File sxAppPath = null;
    File fUserDir;
    String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isEmpty() || !(fUserDir = new File(userHome)).exists()) {
      return null;
    } else {
      String osNameShort = System.getProperty("os.name").substring(0, 1).toLowerCase();
      if ("w".equals(osNameShort)) {
        String appPath = System.getenv("APPDATA");
        if (appPath != null && !appPath.isEmpty()) {
          sxAppPath = new File(new File(appPath), "Sikulix");
        }
      } else if ("m".equals(osNameShort)) {
        sxAppPath = new File(new File(fUserDir, "Library/Application Support"), "Sikulix");
      } else {
        sxAppPath = new File(fUserDir, ".Sikulix");
      }
      if (sxAppPath != null && !sxAppPath.exists()) {
        sxAppPath.mkdirs();
      }
      if (sxAppPath == null || !sxAppPath.exists()) {
        sxAppPath = null;
      }
    }
    return sxAppPath;
  }
}
