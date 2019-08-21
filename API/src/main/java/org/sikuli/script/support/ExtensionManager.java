/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.runners.ProcessRunner;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ExtensionManager {

  private static File sxExtensions = new File(RunTime.getAppPath(), "Extensions");

  public static String makeClassPath(File jarFile) {
    RunTime.startLog(1, "starting");
    String jarPath = jarFile.getAbsolutePath();
    if (!classPath.isEmpty()) {
      classPath += File.pathSeparator;
    }
    classPath += jarPath;

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
    boolean extensionsOK = true;

    if (!sxExtensions.exists()) {
      sxExtensions.mkdir();
    }
    if (!sxExtensions.exists()) {
      RunTime.startLog(1, "folder extension not available: %s", sxExtensions);
      extensionsOK = false;
    }

    if (extensionsOK) {
      readExtensions(false);
      File[] fExtensions = sxExtensions.listFiles();
      if (moveJython || moveJRuby) {
        for (File fExtension : fExtensions) {
          String name = fExtension.getName();
          if ((moveJython && name.contains("jython") && name.contains("standalone")) ||
                  (moveJRuby && name.contains("jruby") && name.contains("complete"))) {
            fExtension.delete();
          }
        }
      }
      if (null != sxFolderList && sxFolderList.length > 0) {
        for (File fJar : sxFolderList) {
          try {
            Files.move(fJar.toPath(), sxExtensions.toPath().resolve(fJar.toPath().getFileName()), StandardCopyOption.REPLACE_EXISTING);
            RunTime.startLog(1, "moving to extensions: %s", fJar);
          } catch (IOException e) {
            RunTime.startLog(-1, "moving to extensions: %s (%s)", fJar, e.getMessage());
          }
        }
      }

      //log(1, "looking for extension jars in: %s", sxExtensions);
      fExtensions = sxExtensions.listFiles();
      for (File fExtension : fExtensions) {
        String pExtension = fExtension.getAbsolutePath();
        if (pExtension.endsWith(".jar")) {
          if (!classPath.isEmpty()) {
            classPath += File.pathSeparator;
          }
          if (pExtension.contains("jython") && pExtension.contains("standalone")) {
            if (jythonReady) {
              continue;
            }
            if (pExtension.contains(jythonVersion)) {
              jythonReady = true;
            }
          } else if (pExtension.contains("jruby") && pExtension.contains("complete")) {
            if (jrubyReady) {
              continue;
            }
            if (pExtension.contains(jrubyVersion)) {
              jrubyReady = true;
            }
          }
          classPath += pExtension;
          RunTime.startLog(1, "adding extension: %s", fExtension);
        }
      }
      if (!jythonReady && !jrubyReady) {
        String message = "Neither Jython nor JRuby available" +
                "\nPlease consult the docs for a solution.\n" +
                "\nIDE might not be useable with JavaScript only";
        if (RunTime.isIDE()) {
          if (!RunTime.isVerbose()) {
            JOptionPane.showMessageDialog(null, message, "IDE startup problem",
                    JOptionPane.ERROR_MESSAGE);
          } else {
            RunTime.startLog(-1, message);
          }
        }
      }
    }
    return classPath;
  }

  private static String classPath = "";

  public static void readExtensions(boolean afterStart) {
    sxExtensionsFileContent = new ArrayList<>();
    sxExtensionsFile = new File(sxExtensions, "extensions.txt");
    String txtExtensions = FileManager.readFileToString(sxExtensionsFile);
    if (!txtExtensions.isEmpty()) {
      String[] lines = txtExtensions.split("\\n");
      String extlines = "";
      for (String line : lines) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
          continue;
        }
        extlines += line + "\n";
        sxExtensionsFileContent.add(line);
      }
      RunTime.startLog(1, "extensions.txt\n%s", extlines);
    }
    if (sxExtensionsFileContent.size() == 0) {
      RunTime.startLog(1, "no extensions.txt nor valid content");
      return;
    }
    for (String line : sxExtensionsFileContent) {
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
          if (afterStart) {
            continue;
          }
          jythonReady = true;
          setJythonExtern(true);
        }
        if ("python".equals(token)) {
          if (!afterStart) {
            continue;
          }
          if (extFile.isAbsolute()) {
            if (extFile.exists()) {
              RunTime.startLog(1, "Python available at: %s", extPath);
              python = extPath;
            }
          } else {
            String runOut = ProcessRunner.run(extPath, "-V");
            if (runOut.startsWith("0\n")) {
              python = extPath;
              RunTime.startLog(1, "Python available as command: %s (%s)", extPath, runOut.substring(2));
            }
          }
          continue;
        }
        if (!afterStart) {
          if (!classPath.isEmpty()) {
            classPath += File.pathSeparator;
          }
          classPath += extPath;
          RunTime.startLog(1, "adding extension: %s", extPath);
        }
      }
    }
  }

  private static List<String> sxExtensionsFileContent = new ArrayList<>();

  public static boolean hasExtensionsFile() {
    return sxExtensionsFile != null;
  }

  public static File getExtensionsFile() {
    return sxExtensionsFile;
  }

  private static File sxExtensionsFile = null;

  public static String getExtensionsFileDefault() {
    return "# add absolute paths one per line, that point to other jars,\n" +
            "# that need to be available on Java's classpath at runtime\n" +
            "# They will be added automatically at startup in the given sequence\n" +
            "\n" +
            "# empty lines and lines beginning with # or // are ignored\n" +
            "# delete the leading # to activate a prepared keyword line\n" +
            "\n" +
            "# pointer to a Jython install outside SikuliX\n" +
            "# jython = c:/jython2.7.1/jython.jar\n" +
            "\n" +
            "# the Python executable as used on a commandline\n" +
            "# activating will enable the support for real Python\n" +
            "# python = python\n";
  }

  final static int WARNING_CANCEL = 2;

  static String jythonVersion = "2.7.1";
  private static boolean moveJython = false;
  private static boolean jythonReady = false;

  public static boolean isJythonExtern() {
    return jythonExtern;
  }

  public static void setJythonExtern(boolean jythonExtern) {
    ExtensionManager.jythonExtern = jythonExtern;
  }

  private static boolean jythonExtern = false;

  public static boolean hasPython() {
    return !python.isEmpty();
  }

  public static String getPython() {
    return python;
  }

  private static String python = "";

  private static String jrubyVersion = "9.2.0.0";
  private static boolean moveJRuby = false;
  private static boolean jrubyReady = false;

  public static boolean isJrubyExtern() {
    return jrubyExtern;
  }

  public static void setJrubyExtern(boolean jrubyExtern) {
    ExtensionManager.jrubyExtern = jrubyExtern;
  }

  private static boolean jrubyExtern = false;

  public static boolean shouldCheckContent(String type, String identifier) {
    boolean usePython = false;
    if (type.contains("ython") && hasPython()) {
      if (type.equals(identifier)) {
        return true;
      }
      if (RunTime.shouldRunPythonServer()) {
        usePython = true;
      } else if (hasShebang(shebangPython, identifier)) {
        usePython = true;
      }
      if (usePython) {
        return "text/python".equals(type);
      }
      return "text/jython".equals(type);
    }
    return true;
  }

  public static boolean hasShebang(String type, String scriptFile) {
    try (Reader reader = new InputStreamReader(new FileInputStream(scriptFile), "UTF-8")) {
      char[] chars = new char[type.length()];
      int read = reader.read(chars);
      if (read == type.length()) {
        if (type.equals(new String(chars).toUpperCase())) {
          return true;
        }
      }
    } catch (Exception ex) {
      if (scriptFile.length() >= type.length()
              && type.equals(scriptFile.substring(0, type.length()).toUpperCase())) {
        return true;
      }
    }
    return false;
  }

  public static final String shebangPython = "#!PYTHON";
  public static final String shebangJython = "#!JYTHON";

  public static File getSitesTxt() {
    return sxSitesTxt;
  }

  private static File sxSitesTxt = new File(RunTime.getAppPath(), "Lib/site-packages/sites.txt");

  public static String getSitesTxtDefault() {
    return "# add absolute paths one per line, that point to other directories/jars,\n" +
            "# where importable modules (Jython, plain Python, SikuliX scripts, ...) can be found.\n" +
            "# They will be added automatically at startup to the end of sys.path in the given sequence\n" +
            "\n" +
            "# lines beginning with # and blank lines are ignored and can be used as comments\n";
  }
  final static int WARNING_ACCEPTED = 1;
  final static int WARNING_DO_NOTHING = 0;
  //<editor-fold desc="10 old handling">
  private static ExtensionManager _instance = null;
  private ArrayList<Extension> extensions;
  private File fExtensions = null;
  private ExtensionManager() {
    extensions = new ArrayList<Extension>();
    fExtensions = RunTime.get().fSikulixExtensions;
    Extension e;
    String path, name, version;
    for (File d : Objects.requireNonNull(fExtensions.listFiles())) {
      if (d.getAbsolutePath().endsWith(".jar")) {
        path = d.getAbsolutePath();
        name = d.getName();
        name = name.substring(0, name.length() - 4);
        if (name.contains("-")) {
          version = name.substring(name.lastIndexOf("-") + 1);
          name = name.substring(0, name.lastIndexOf("-"));
        } else {
          version = "0.0";
        }
        e = new Extension(name, path, version);
        extensions.add(e);
      }
    }
  }

  public static List<String> getExtensionNames() {
    List<String> extensions = new ArrayList<>();
    for (File extension : Objects.requireNonNull(sxExtensions.listFiles())) {
      String name = extension.getName();
      if (name.startsWith(".") || !name.endsWith(".jar")) {
        if (name.startsWith("extension_classpath.txt")) {
          extensions.add(0, name);
          List<String> classpath = new ArrayList<>();
          String[] content = FileManager.readFileToString(extension).split("\\n");
          for (String line : content) {
            line = line.trim();
            if (line.startsWith("#") || line.startsWith("//")) {
              continue;
            }
            classpath.add(line);
          }
        }
        continue;
      }
      extensions.add(name);
    }
    return extensions;
  }

  public static void show() {
    StringBuilder warn = new StringBuilder("Nothing to do here currently - click what you like ;-)\n" +
            "\nExtensions folder: \n" + sxExtensions +
            "\n\nCurrent content:");
    List<String> extensionNames = getExtensionNames();
    for (String extension : extensionNames) {
      warn.append("\n").append(extension);
    }
    if (hasExtensionsFile()) {
      warn.append("\n\nextensions.txt content:");
      for (String extension : sxExtensionsFileContent) {
        warn.append("\n").append(extension);
      }
    }
    warn.append("\n\n" + "see menu File -> Open Special Files");
    String title = "SikuliX1 Extensions";
    String[] options = new String[3];
    options[WARNING_DO_NOTHING] = "OK";
    options[WARNING_ACCEPTED] = "More ...";
    options[WARNING_CANCEL] = "Cancel";
    int ret = JOptionPane.showOptionDialog(null, warn.toString(), title,
            0, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
    if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
      return;
    }
  }

  public static ExtensionManager getInstance() {
    if (_instance == null) {
      _instance = new ExtensionManager();
    }
    return _instance;
  }

  public boolean install(String name, String url, String version) {
    if (url.startsWith("---extensions---")) {
      url = RunTime.get().SikuliRepo + name + "-" + version + ".jar";
    }
    String extPath = fExtensions.getAbsolutePath();
    String tmpdir = RunTime.get().fpBaseTempPath;
    try {
      File localFile = new File(FileManager.downloadURL(new URL(url), tmpdir));
      String extName = localFile.getName();
      File targetFile = new File(extPath, extName);
      if (targetFile.exists()) {
        targetFile.delete();
      }
      if (!localFile.renameTo(targetFile)) {
        Debug.error("ExtensionManager: Failed to install " + localFile.getName() + " to " + targetFile.getAbsolutePath());
        return false;
      }
      addExtension(name, localFile.getAbsolutePath(), version);
    } catch (IOException e) {
      Debug.error("ExtensionManager: Failed to download " + url);
      return false;
    }
    return true;
  }

  private void addExtension(String name, String path, String version) {
    Extension e = find(name, version);
    if (e == null) {
      extensions.add(new Extension(name, path, version));
    } else {
      e.path = path;
    }
  }

  public boolean isInstalled(String name) {
    if (find(name) != null) {
      return true;
    } else {
      return false;
    }
  }

  public String getLoadPath(String name) {
    Extension e = find(name);
    if (e != null) {
      Debug.log(2, "ExtensionManager: found: " + name + " ( " + e.version + " )");
      return e.path;
    } else {
      if (!name.endsWith(".jar")) {
        Debug.error("ExtensionManager: not found: " + name);
      }
      return null;
    }
  }

  public boolean isOutOfDate(String name, String version) {
    Extension e = find(name);
    if (e == null) {
      return false;
    } else {
      String s1 = normalisedVersion(e.version); // installed version
      String s2 = normalisedVersion(version);  // version number to check
      int cmp = s1.compareTo(s2);
      return cmp < 0;
    }
  }

  public String getVersion(String name) {
    Extension e = find(name);
    if (e != null) {
      return e.version;
    } else {
      return null;
    }
  }

  private Extension find(String name) {
    if (name.endsWith(".jar")) {
      name = name.substring(0, name.length() - 4);
    }
    String v;
    if (name.contains("-")) {
      v = name.substring(name.lastIndexOf("-") + 1);
      return find(name.substring(0, name.lastIndexOf("-")), v);
    } else {
      v = normalisedVersion("0.0");
    }
    Extension ext = null;
    for (Extension e : extensions) {
      if (e.name.equals(name)) {
        if (v.compareTo(normalisedVersion(e.version)) <= 0) {
          ext = e;
          v = normalisedVersion(e.version);
        }
      }
    }
    return ext;
  }

  private Extension find(String name, String version) {
    String v = normalisedVersion(version);
    for (Extension e : extensions) {
      if (e.name.equals(name) && normalisedVersion(e.version).equals(v)) {
        return e;
      }
    }
    return null;
  }

  private static String normalisedVersion(String version) {
    return normalisedVersion(version, ".", 4);
  }

  private static String normalisedVersion(String version, String sep, int maxWidth) {
    String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
    StringBuilder sb = new StringBuilder();
    for (String s : split) {
      sb.append(String.format("%" + maxWidth + 's', s));
    }
    return sb.toString();
  }

  static class Extension implements Serializable {
    public String name, path, version;

    public Extension(String name_, String path_, String version_) {
      name = name_;
      path = path_;
      version = version_;
    }
  }
  //</editor-fold>
}

