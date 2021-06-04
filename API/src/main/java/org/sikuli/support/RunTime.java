/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.HotkeyManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.SikuliXception;
import org.sikuli.support.devices.HelpDevice;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RunTime {

  static {
    Commons.init();
  }

  //<editor-fold defaultstate="collapsed" desc="04 instance">
  private RunTime() {
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="02 logging">
  private static int lvl = 3;
  private int minLvl = lvl;

  public static String arrayToQuotedString(String[] args) {
    String ret = "";
    for (String s : args) {
      if (s.contains(" ")) {
        s = "\"" + s + "\"";
      }
      ret += s + " ";
    }
    return ret;
  }

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "RunTime: " + message, args);
  }

  private static void logp(String message, Object... args) {
    Debug.logx(-3, message, args);
  }

  private static void logp(int level, String message, Object... args) {
    if (level <= Debug.getDebugLevel()) {
      logp(message, args);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="03 variables">
  public enum RunType {
    JAR, CLASSES, OTHER
  }

  public static boolean testing = false;

  private static Class clsRef = RunTime.class;

  private List<URL> classPathActual = new ArrayList<>();
  private List<String> classPathList = new ArrayList<>();

  private static boolean areLibsExported = false;
  private static Map<String, Boolean> libsLoaded = new HashMap<String, Boolean>();

  public File fSxBaseJar = null;
  public static String fpContent = "sikulixcontent";

  public boolean runningJar = true;
  public boolean runningWindows = false;
  public boolean runningMac = false;
  public boolean runningLinux = false;
  //</editor-fold>

  //<editor-fold desc="99 cleanUp">
  public static void terminate() {
    terminate(0, "");
  }

  public static void terminate(int retval, String message, Object... args) {
    String outMsg = String.format(message, args);
    if (retval < 999) {
      if (!outMsg.isEmpty()) {
        System.out.println("TERMINATING: " + outMsg);
      }
      cleanUp();
      System.exit(retval);
    }
    System.out.println("FATAL ERROR: " + outMsg);
    throw new SikuliXception(String.format("FATAL: " + outMsg));
  }

  public static void cleanUp() {
    HotkeyManager.reset(true);
    HelpDevice.stopAll();
  }

  public static void cleanUpAfterScript() {
    HotkeyManager.reset(false);
    HelpDevice.stopAll();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="20 helpers">
  public void crash() {
    int x = 1 / 0;
  }

  public static void pause(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException ex) {
    }
  }

  public static void pause(float time) {
    try {
      Thread.sleep((int) (time * 1000));
    } catch (InterruptedException ex) {
    }
  }

//TODO abortScripting obsolete
/*
  protected void abortScripting(String msg1, String msg2) {
    Thread current = Thread.currentThread();
    String where = "";
    if (isJythonReady) {
      where = JythonHelper.get().getCurrentLine();
      log(-1, msg2);
      log(-1, msg1 + " %s", where);
    }
    if (where.isEmpty()) {
      throw new RuntimeException(msg1 + msg2);
    }
    current.interrupt();
    current.stop();
  }
*/

  /**
   * INTERNAL USE: to check whether we are running in compiled classes context
   *
   * @return true if the code source location is a folder ending with classes (Maven convention)
   */
  public boolean isRunningFromJar() {
    return runningJar;
  }

  public boolean isJava9(String... args) {
    if (Commons.getJavaVersion() > 8) {
      if (args.length > 0) {
        log(-1, "*** Java 9+: %s", args[0]);
      }
      return true;
    } else {
      return false;
    }
  }
  //</editor-fold>

  //<editor-fold desc="16 get resources NEW">
  public static List<String> getResourceList(String res) {
    return getResourceList(res, clsRef);
  }

  public static List<String> getResourceList(String res, Class classReference) {
    List<String> resList = new ArrayList<>();
    CodeSource codeSource = classReference.getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      return resList;
    }
    InputStream aIS = null;
    String content = null;
    res = new File(res, "sikulixcontent").getPath();
    if (Commons.runningWindows()) {
      res = res.replace("\\", "/");
    }
    if (!res.startsWith("/")) {
      res = "/" + res;
    }
    try {
      aIS = (InputStream) classReference.getResourceAsStream(res);
      if (aIS != null) {
        content = new String(copy(aIS));
        aIS.close();
      }
      log(lvl + 1, "getResourceList: %s (%s)", res, content);
      aIS = null;
    } catch (Exception ex) {
      log(-1, "getResourceList: %s (%s)", res, ex);
    }
    try {
      if (aIS != null) {
        aIS.close();
      }
    } catch (Exception ex) {
    }
    if (null != content) {
      String[] names = content.split("\n");
      for (String name : names) {
        if (name.equals("sikulixcontent")) continue;
        resList.add(name.trim());
      }
    }
    return resList;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="15 handling resources from classpath">

  /**
   * export all resource files from the given subtree on classpath to the given folder retaining the subtree<br>
   * to export a specific file from classpath use extractResourceToFile or extractResourceToString
   *
   * @param fpRessources path of the subtree relative to root
   * @param fFolder      folder where to export (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return the filtered list of files (compact sikulixcontent format)
   */

  public static List<String> extractResourcesToFolder(String fpRessources, File fFolder, FilenameFilter filter) {
    List<String> content;
    content = resourceList(fpRessources, filter);
    if (content == null) {
      return null;
    }
    if (fFolder == null) {
      return content;
    }
    return doExtractToFolderWithList(fpRessources, fFolder, content);
  }

  private static List<String> doExtractToFolderWithList(String fpRessources, File fFolder, List<String> content) {
    int count = 0;
    int ecount = 0;
    String subFolder = "";
    if (content != null && content.size() > 0) {
      for (String eFile : content) {
        if (eFile == null) {
          continue;
        }
        if (eFile.endsWith("/")) {
          subFolder = eFile.substring(0, eFile.length() - 1);
          continue;
        }
        if (!subFolder.isEmpty()) {
          eFile = new File(subFolder, eFile).getPath();
        }
        if (extractResourceToFile(fpRessources, eFile, fFolder)) {
          log(lvl + 1, "extractResourceToFile done: %s", eFile);
          count++;
        } else {
          ecount++;
        }
      }
    }
    if (ecount > 0) {
      log(lvl, "files exported: %d - skipped: %d from %s to:\n %s", count, ecount, fpRessources, fFolder);
    } else {
      log(lvl, "files exported: %d from: %s to:\n %s", count, fpRessources, fFolder);
    }
    return content;
  }

  /**
   * export all resource files from the given subtree in given jar to the given folder retaining the subtree
   *
   * @param aJar         absolute path to an existing jar or a string identifying the jar on classpath (no leading /)
   * @param fpRessources path of the subtree or file relative to root
   * @param fFolder      folder where to export (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return the filtered list of files (compact sikulixcontent format)
   */
  public List<String> extractResourcesToFolderFromJar(String aJar, String fpRessources, File fFolder, FilenameFilter
      filter) {
    List<String> content = new ArrayList<String>();
    File faJar = new File(aJar);
    URL uaJar = null;
    fpRessources = FileManager.slashify(fpRessources, false);
    if (faJar.isAbsolute()) {
      if (!faJar.exists()) {
        log(-1, "extractResourcesToFolderFromJar: does not exist:\n%s", faJar);
        return null;
      }
      try {
        uaJar = new URL("jar", null, "file:" + aJar);
      } catch (MalformedURLException ex) {
        log(-1, "extractResourcesToFolderFromJar: bad URL for:\n%s", faJar);
        return null;
      }
    } else {
      uaJar = fromClasspath(aJar);
      if (uaJar == null) {
        log(-1, "extractResourcesToFolderFromJar: not on classpath: %s", aJar);
        return null;
      }
      try {
        String sJar = "file:" + uaJar.getPath() + "!/";
        uaJar = new URL("jar", null, sJar);
      } catch (MalformedURLException ex) {
        log(-1, "extractResourcesToFolderFromJar: bad URL for:\n%s", uaJar);
        return null;
      }
    }
    content = doResourceListJar(uaJar, fpRessources, content, filter);
    if (fFolder == null) {
      return content;
    }
    copyFromJarToFolderWithList(uaJar, fpRessources, content, fFolder);
    return content;
  }

  /**
   * store a resource found on classpath to a file in the given folder with same filename
   *
   * @param inPrefix a subtree found in classpath
   * @param inFile   the filename combined with the prefix on classpath
   * @param outDir   a folder where to export
   * @return success
   */
  public static boolean extractResourceToFile(String inPrefix, String inFile, File outDir) {
    return extractResourceToFile(inPrefix, inFile, outDir, "");
  }

  /**
   * store a resource found on classpath to a file in the given folder
   *
   * @param inPrefix a subtree found in classpath
   * @param inFile   the filename combined with the prefix on classpath
   * @param outDir   a folder where to export
   * @param outFile  the filename for export
   * @return success
   */
  public static boolean extractResourceToFile(String inPrefix, String inFile, File outDir, String outFile) {
    InputStream aIS;
    FileOutputStream aFileOS;
    String content = inPrefix + "/" + inFile;
    try {
      content = Commons.runningWindows() ? content.replace("\\", "/") : content;
      if (!content.startsWith("/")) {
        content = "/" + content;
      }
      aIS = (InputStream) clsRef.getResourceAsStream(content);
      if (aIS == null) {
        File fInFile = new File(content);
        if (!fInFile.exists()) {
          throw new IOException(String.format("resource not accessible: %s", content));
        }
        aIS = new FileInputStream(fInFile);
      }
      File out = outFile.isEmpty() ? new File(outDir, inFile) : new File(outDir, outFile);
      if (!out.getParentFile().exists()) {
        out.getParentFile().mkdirs();
      }
      aFileOS = new FileOutputStream(out);
      copy(aIS, aFileOS);
      aIS.close();
      aFileOS.close();
    } catch (Exception ex) {
      log(-1, "extractResourceToFile: %s\n%s", content, ex);
      return false;
    }
    return true;
  }

  /**
   * store the content of a resource found on classpath in the returned string
   *
   * @param inPrefix a subtree from root found in classpath (leading /)
   * @param inFile   the filename combined with the prefix on classpath
   * @param encoding
   * @return file content
   */
  public static String extractResourceToString(String inPrefix, String inFile, String encoding) {
    InputStream aIS = null;
    String out = null;
    String content = inPrefix + "/" + inFile;
    if (!content.startsWith("/")) {
      content = "/" + content;
    }
    try {
      content = Commons.runningWindows() ? content.replace("\\", "/") : content;
      aIS = (InputStream) clsRef.getResourceAsStream(content);
      if (aIS == null) {
        throw new IOException("resource not accessible");
      }
      if (encoding == null) {
        encoding = "UTF-8";
        out = new String(copy(aIS));
      } else if (encoding.isEmpty()) {
        out = new String(copy(aIS), "UTF-8");
      } else {
        out = new String(copy(aIS), encoding);
      }
      aIS.close();
      aIS = null;
    } catch (Exception ex) {
      log(-1, "extractResourceToString as %s from:\n%s\n%s", encoding, content, ex);
    }
    try {
      if (aIS != null) {
        aIS.close();
      }
    } catch (Exception ex) {
    }
    return out;
  }

  public static URL resourceLocation(String folderOrFile) {
    log(lvl, "resourceLocation: (%s) %s", clsRef, folderOrFile);
    if (!folderOrFile.startsWith("/")) {
      folderOrFile = "/" + folderOrFile;
    }
    return clsRef.getResource(folderOrFile);
  }

  private static List<String> resourceList(String folder, FilenameFilter filter) {
    List<String> files = new ArrayList<String>();
    if (!folder.startsWith("/")) {
      folder = "/" + folder;
    }
    URL uFolder = resourceLocation(folder);
    File fFolder = null;
    if (uFolder == null) {
      fFolder = new File(folder);
      if (fFolder.exists()) {
        files = doResourceListFolder(fFolder, files, filter);
      } else {
        log(lvl, "resourceList: not found: %s", folder);
      }
      return files;
    }
    try {
      uFolder = new URL(uFolder.toExternalForm().replaceAll(" ", "%20"));
    } catch (Exception ex) {
    }
    URL uContentList = clsRef.getResource(folder + "/" + fpContent);
    if (uContentList != null) {
      return doResourceListWithList(folder, files, filter);
    }
    try {
      fFolder = new File(uFolder.toURI());
      log(lvl, "resourceList: having folder: %s", fFolder);
      files.add(fFolder.getPath());
      files = doResourceListFolder(fFolder, files, filter);
      files.remove(0);
      return files;
    } catch (Exception ex) {
      if (!"jar".equals(uFolder.getProtocol())) {
        log(lvl, "resourceList:\n%s", folder);
        log(-1, "resourceList: URL neither folder nor jar:\n%s", ex);
        return null;
      }
    }
    String[] parts = uFolder.getPath().split("!");
    if (parts.length < 2 || !parts[0].startsWith("file:")) {
      log(lvl, "resourceList:\n%s", folder);
      log(-1, "resourceList: not a valid jar URL: " + uFolder.getPath());
      return null;
    }
    String fpFolder = parts[1];
    log(lvl, "resourceList: having jar: %s", uFolder);
    return doResourceListJar(uFolder, fpFolder, files, filter);
  }

  /**
   * write the list as it is produced by calling extractResourcesToFolder to the given file with system line
   * separator<br>
   * non-compact format: every file with full path
   *
   * @param folder path of the subtree relative to root with leading /
   * @param target the file to write the list (if null, only list - no file)
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public String[] resourceListAsFile(String folder, File target, FilenameFilter filter) {
    String content = resourceListAsString(folder, filter);
    if (content == null) {
      log(-1, "resourceListAsFile: did not work: %s", folder);
      return null;
    }
    if (target != null) {
      try {
        FileManager.deleteFileOrFolder(target.getAbsolutePath());
        target.getParentFile().mkdirs();
        PrintWriter aPW = new PrintWriter(target);
        aPW.write(content);
        aPW.close();
      } catch (Exception ex) {
        log(-1, "resourceListAsFile: %s:\n%s", target, ex);
      }
    }
    return content.split(System.getProperty("line.separator"));
  }

  /**
   * write the list as it is produced by calling extractResourcesToFolder to the given file with system line
   * separator<br>
   * compact sikulixcontent format
   *
   * @param folder       path of the subtree relative to root with leading /
   * @param targetFolder the folder where to store the file sikulixcontent (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public String[] resourceListAsSikulixContent(String folder, File targetFolder, FilenameFilter filter) {
    List<String> contentList = resourceList(folder, filter);
    if (contentList == null) {
      log(-1, "resourceListAsSikulixContent: did not work: %s", folder);
      return null;
    }
    File target = null;
    String arrString[] = new String[contentList.size()];
    try {
      PrintWriter aPW = null;
      if (targetFolder != null) {
        target = new File(targetFolder, fpContent);
        FileManager.deleteFileOrFolder(target);
        target.getParentFile().mkdirs();
        aPW = new PrintWriter(target);
      }
      int n = 0;
      for (String line : contentList) {
        arrString[n++] = line;
        if (targetFolder != null) {
          aPW.println(line);
        }
      }
      if (targetFolder != null) {
        aPW.close();
      }
    } catch (Exception ex) {
      log(-1, "resourceListAsFile: %s:\n%s", target, ex);
    }
    return arrString;
  }

  /**
   * write the list as it is produced by calling extractResourcesToFolder to the given file with system line
   * separator<br>
   * compact sikulixcontent format
   *
   * @param aJar         absolute path to an existing jar or a string identifying the jar on classpath (no leading /)
   * @param folder       path of the subtree relative to root with leading /
   * @param targetFolder the folder where to store the file sikulixcontent (if null, only list - no export)
   * @param filter       implementation of interface FilenameFilter or null for no filtering
   * @return success
   */
  public String[] resourceListAsSikulixContentFromJar(String aJar, String folder, File targetFolder, FilenameFilter
      filter) {
    List<String> contentList = extractResourcesToFolderFromJar(aJar, folder, null, filter);
    if (contentList == null || contentList.size() == 0) {
      log(-1, "resourceListAsSikulixContentFromJar: did not work: %s", folder);
      return null;
    }
    File target = null;
    String arrString[] = new String[contentList.size()];
    try {
      PrintWriter aPW = null;
      if (targetFolder != null) {
        target = new File(targetFolder, fpContent);
        FileManager.deleteFileOrFolder(target);
        target.getParentFile().mkdirs();
        aPW = new PrintWriter(target);
      }
      int n = 0;
      for (String line : contentList) {
        arrString[n++] = line;
        if (targetFolder != null) {
          aPW.println(line);
        }
      }
      if (targetFolder != null) {
        aPW.close();
      }
    } catch (Exception ex) {
      log(-1, "resourceListAsFile: %s:\n%s", target, ex);
    }
    return arrString;
  }

  /**
   * write the list produced by calling extractResourcesToFolder to the returned string with system line separator<br>
   * non-compact format: every file with full path
   *
   * @param folder path of the subtree relative to root with leading /
   * @param filter implementation of interface FilenameFilter or null for no filtering
   * @return the resulting string
   */
  public String resourceListAsString(String folder, FilenameFilter filter) {
    return resourceListAsString(folder, filter, null);
  }

  /**
   * write the list produced by calling extractResourcesToFolder to the returned string with given separator<br>
   * non-compact format: every file with full path
   *
   * @param folder    path of the subtree relative to root with leading /
   * @param filter    implementation of interface FilenameFilter or null for no filtering
   * @param separator to be used to separate the entries
   * @return the resulting string
   */
  public String resourceListAsString(String folder, FilenameFilter filter, String separator) {
    List<String> aList = resourceList(folder, filter);
    if (aList == null) {
      return null;
    }
    if (separator == null) {
      separator = System.getProperty("line.separator");
    }
    String out = "";
    String subFolder = "";
    if (aList != null && aList.size() > 0) {
      for (String eFile : aList) {
        if (eFile == null) {
          continue;
        }
        if (eFile.endsWith("/")) {
          subFolder = eFile.substring(0, eFile.length() - 1);
          continue;
        }
        if (!subFolder.isEmpty()) {
          eFile = new File(subFolder, eFile).getPath();
        }
        out += eFile.replace("\\", "/") + separator;
      }
    }
    return out;
  }

  private static List<String> doResourceListFolder(File fFolder, List<String> files, FilenameFilter filter) {
    int localLevel = testing ? lvl : lvl + 1;
    String subFolder = "";
    if (fFolder.isDirectory()) {
      if (files.size() > 0 && !FileManager.pathEquals(fFolder.getPath(), files.get(0))) {
        subFolder = fFolder.getPath().substring(files.get(0).length() + 1).replace("\\", "/") + "/";
        if (filter != null && !filter.accept(new File(files.get(0), subFolder), "")) {
          return files;
        }
      } else {
        logp(localLevel, "scanning folder:\n%s", fFolder);
        subFolder = "/";
        files.add(subFolder);
      }
      String[] subList = fFolder.list();
      for (String entry : subList) {
        File fEntry = new File(fFolder, entry);
        if (fEntry.isDirectory()) {
          files.add(fEntry.getAbsolutePath().substring(1 + files.get(0).length()).replace("\\", "/") + "/");
          doResourceListFolder(fEntry, files, filter);
          files.add(subFolder);
        } else {
          if (filter != null && !filter.accept(fFolder, entry)) {
            continue;
          }
          logp(localLevel, "from %s adding: %s", (subFolder.isEmpty() ? "." : subFolder), entry);
          files.add(fEntry.getAbsolutePath().substring(1 + fFolder.getPath().length()));
        }
      }
    }
    return files;
  }

  private static List<String> doResourceListWithList(String folder, List<String> files, FilenameFilter filter) {
    String content = extractResourceToString(folder, fpContent, "");
    String[] contentList = content.split(content.indexOf("\r") != -1 ? "\r\n" : "\n");
    if (filter == null) {
      files.addAll(Arrays.asList(contentList));
    } else {
      for (String fpFile : contentList) {
        if (filter.accept(new File(fpFile), "")) {
          files.add(fpFile);
        }
      }
    }
    return files;
  }

  private static List<String> doResourceListJar(URL uJar, String fpResource, List<String> files, FilenameFilter filter) {
    ZipInputStream zJar;
    String fpJar = uJar.getPath().split("!")[0];
    int localLevel = testing ? lvl : lvl + 1;
    String fileSep = "/";
    if (!fpJar.endsWith(".jar")) {
      return files;
    }
    logp(localLevel, "scanning jar:\n%s", uJar);
    fpResource = (fpResource.startsWith("/") ? fpResource.substring(1) : fpResource) + "/";
    File fFolder = new File(fpResource);
    File fSubFolder = null;
    ZipEntry zEntry;
    String subFolder = "";
    boolean skip = false;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        if (zEntry.getName().endsWith("/")) {
          continue;
        }
        String zePath = zEntry.getName();
        if (zePath.startsWith(fpResource)) {
//          if (fpResource.length()  == zePath.length()) {
//            files.add(zePath);
//            return files;
//          }
          String zeName = zePath.substring(fpResource.length());
          int nSep = zeName.lastIndexOf(fileSep);
          String zefName = zeName.substring(nSep + 1, zeName.length());
          String zeSub = "";
          if (nSep > -1) {
            zeSub = zeName.substring(0, nSep + 1);
            if (!subFolder.equals(zeSub)) {
              subFolder = zeSub;
              fSubFolder = new File(fFolder, subFolder);
              skip = false;
              if (filter != null && !filter.accept(fSubFolder, "")) {
                skip = true;
                continue;
              }
              files.add(zeSub);
            }
            if (skip) {
              continue;
            }
          } else {
            if (!subFolder.isEmpty()) {
              subFolder = "";
              fSubFolder = fFolder;
              files.add("/");
            }
          }
          //TODO exportLib: NPE????
          if (fSubFolder == null) {
            RunTime.terminate(999, "exportLib: NPE???? %s (%s)", zefName, fpResource);
          }
          if (filter != null && !filter.accept(fSubFolder, zefName)) {
            continue;
          }
          files.add(zefName);
          logp(localLevel, "from %s adding: %s", (zeSub.isEmpty() ? "." : zeSub), zefName);
        }
      }
    } catch (Exception ex) {
      log(-1, "doResourceListJar: %s", ex);
      ex.printStackTrace();
      return files;
    }
    return files;
  }

  public List<String> listFilesInJar(URL uJar) {
    ZipInputStream zJar;
    String fpJar = uJar.getPath().split("!")[0];
    int localLevel = testing ? lvl : lvl + 1;
    String fileSep = "/";
    if (!fpJar.endsWith(".jar")) {
      return null;
    }
    logp(localLevel, "listFilesInJar: scanning jar:\n%s", uJar);
    List<String> files = new ArrayList<>();
    ZipEntry zEntry;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        if (zEntry.getName().endsWith("/")) {
          continue;
        }
        String zePath = zEntry.getName();
        files.add(zePath);
        logp(localLevel, "listFilesInJar: adding: %s", zePath);
      }
    } catch (Exception ex) {
      log(-1, "listFilesInJar: %s", ex);
      return files;
    }
    return files;
  }

  private boolean copyFromJarToFolderWithList(URL uJar, String fpRessource, List<String> files, File fFolder) {
    if (files == null || files.isEmpty()) {
      log(lvl, "copyFromJarToFolderWithList: list of files is empty");
      return false;
    }
    String fpJar = uJar.getPath().split("!")[0];
    if (!fpJar.endsWith(".jar")) {
      return false;
    }
    int localLevel = testing ? lvl : lvl + 1;
    logp(localLevel, "scanning jar:\n%s", uJar);
    fpRessource = fpRessource.startsWith("/") ? fpRessource.substring(1) : fpRessource;

    String subFolder = "";

    int maxFiles = files.size() - 1;
    int nFiles = 0;

    ZipEntry zEntry;
    ZipInputStream zJar;
    String zPath;
    int prefix = fpRessource.length();
    fpRessource += !fpRessource.isEmpty() ? "/" : "";
    String current = "/";
    boolean shouldStop = false;
    try {
      zJar = new ZipInputStream(new URL(fpJar).openStream());
      while ((zEntry = zJar.getNextEntry()) != null) {
        zPath = zEntry.getName();
        if (zPath.endsWith("/")) {
          continue;
        }
        while (current.endsWith("/")) {
          if (nFiles > maxFiles) {
            shouldStop = true;
            break;
          }
          subFolder = current.length() == 1 ? "" : current;
          current = files.get(nFiles++);
          if (!current.endsWith("/")) {
            current = fpRessource + subFolder + current;
            break;
          }
        }
        if (shouldStop) {
          break;
        }
        if (zPath.startsWith(current)) {
          if (zPath.length() == fpRessource.length() - 1) {
            log(-1, "extractResourcesToFolderFromJar: only ressource folders allowed - use filter");
            return false;
          }
          logp(localLevel, "copying: %s", zPath);
          File out = new File(fFolder, zPath.substring(prefix));
          if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
          }
          FileOutputStream aFileOS = new FileOutputStream(out);
          copy(zJar, aFileOS);
          aFileOS.close();
          if (nFiles > maxFiles) {
            break;
          }
          current = files.get(nFiles++);
          if (!current.endsWith("/")) {
            current = fpRessource + subFolder + current;
          }
        }
      }
      zJar.close();
    } catch (Exception ex) {
      log(-1, "doResourceListJar: %s", ex);
      return false;
    }
    return true;
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] tmp = new byte[8192];
    int len;
    while (true) {
      len = in.read(tmp);
      if (len <= 0) {
        break;
      }
      out.write(tmp, 0, len);
    }
    out.flush();
  }

  private static byte[] copy(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length = 0;
    while ((length = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, length);
    }
    return baos.toByteArray();
  }

  public class oneFileFilter implements FilenameFilter {

    String aFile;

    public oneFileFilter(String aFileGiven) {
      aFile = aFileGiven;
    }

    @Override
    public boolean accept(File dir, String name) {
      if (name.contains(aFile)) {
        return true;
      }
      return false;
    }

  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="12 classpath handling">
  private void storeClassPath() {
    if (isJava9()) {
      String separator = File.pathSeparator;
      String cp = System.getProperty("java.class.path");
      classPathList = Arrays.asList(cp.split(separator));
    } else {
      classPathActual.clear();
      URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      classPathActual = Arrays.asList(sysLoader.getURLs());
      classPathList.clear();
      for (URL urlPath : classPathActual) {
        classPathList.add(urlPath.toExternalForm());
      }
    }
  }

  /**
   * print the current classpath entries to sysout
   */
  public void dumpClassPath() {
    dumpClassPath(null);
  }

  /**
   * print the current classpath entries to sysout whose path name contain the given string
   *
   * @param filter the fileter string
   */
  public void dumpClassPath(String filter) {
    filter = filter == null ? "" : filter;
    logp("*** classpath dump %s", filter);
    storeClassPath();
    filter = filter.toUpperCase();
    int n = 0;
    for (String sEntry : classPathList) {
      if (!filter.isEmpty()) {
        if (!sEntry.toUpperCase().contains(filter)) {
          n++;
          continue;
        }
      }
      logp("%3d: %s", n, sEntry);
      n++;
    }
    logp("*** classpath dump end");
  }

  /**
   * check whether a classpath entry contains the given identifying string, stops on first match
   *
   * @param artefact the identifying string
   * @return the absolute path of the entry found - null if not found
   */
  private String isOnClasspath(String artefact, boolean isJar) {
    artefact = FileManager.slashify(artefact, false);
    String cpe = null;
    if (classPathList.isEmpty()) {
      storeClassPath();
    }
    for (String entry : classPathList) {
      String sEntry = FileManager.slashify(new File(entry).getPath(), false);
      if (sEntry.contains(artefact)) {
        if (isJar) {
          if (!sEntry.endsWith(".jar")) {
            continue;
          }
          if (!new File(sEntry).getName().contains(artefact)) {
            continue;
          }
          if (new File(sEntry).getName().contains("4" + artefact)) {
            continue;
          }
        }
        cpe = new File(entry).getPath();
        break;
      }
    }
    return cpe;
  }

  public String isJarOnClasspath(String artefact) {
    return isOnClasspath(artefact, true);
  }

  public String isOnClasspath(String artefact) {
    return isOnClasspath(artefact, false);
  }

  public URL fromClasspath(String artefact) {
    artefact = FileManager.slashify(artefact, false).toUpperCase();
    URL cpe = null;
    String scpe = null;
    if (classPathActual.isEmpty()) {
      storeClassPath();
    }
    for (String entry : classPathList) {
      String sEntry = FileManager.slashify(new File(entry).getPath(), false);
      if (sEntry.toUpperCase().contains(artefact)) {
        scpe = entry;
        break;
      }
    }
    if (null != scpe) {
      try {
        cpe = new URL(scpe);
      } catch (MalformedURLException e) {
      }
    }
    return cpe;
  }

  /**
   * check wether a the given URL is on classpath
   *
   * @param path URL to look for
   * @return true if found else otherwise
   */
  public boolean isOnClasspath(URL path) {
    if (classPathActual.isEmpty()) {
      storeClassPath();
    }
    for (String entry : classPathList) {
    }
    return false;
  }

  List<String> sxClasspath = new ArrayList<>();

  public boolean addToClasspath(String jarOrFolder) {
    return addToClasspath(jarOrFolder, "");
  }

  public boolean addToClasspath(String jarOrFolder, String caller) {
    if (null != isOnClasspath(jarOrFolder)) {
      return true;
    }
    if (isJava9("skipped: addToClasspath() - caller: " + caller)) {
      sxClasspath.add(jarOrFolder);
      return false;
    }
    if (!new File(jarOrFolder).exists()) {
      log(-1, "addToClasspath: does not exist - not added:\n%s", jarOrFolder);
      return false;
    }
    //TODO addToClasspath
//    log(lvl, "addToClasspath:\n%s", uJarOrFolder);
//    Method method;
//    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//    Class sysclass = URLClassLoader.class;
//    try {
//      method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
//      method.setAccessible(true);
//      method.invoke(sysLoader, new Object[]{uJarOrFolder});
//    } catch (Exception ex) {
//      log(-1, "Did not work: %s", ex.getMessage());
//      return false;
//    }
//    storeClassPath();
    return false;
  }

/*
  public File asExtension(String fpJar) {
    File fJarFound = new File(FileManager.normalizeAbsolute(fpJar));
    if (!fJarFound.exists()) {
      String fpCPEntry = runTime.isOnClasspath(fJarFound.getName());
      if (fpCPEntry == null) {
        fJarFound = new File(Commons.getExtensionsFolder(), fpJar);
        if (!fJarFound.exists()) {
          fJarFound = new File(Commons.getLibFolder(), fpJar);
          if (!fJarFound.exists()) {
            fJarFound = null;
          }
        }
      } else {
        fJarFound = new File(fpCPEntry, fJarFound.getName());
      }
    } else {
      return null;
    }
    return fJarFound;
  }
*/
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="21 runcmd">
  public final static String runCmdError = "*****error*****";

  /**
   * run a system command finally using Java::Runtime.getRuntime().exec(args) and waiting for completion
   *
   * @param cmd the command as it would be given on command line, quoting is preserved
   * @return the output produced by the command (sysout [+ "*** error ***" + syserr] if the syserr part is present, the
   * command might have failed
   */
  public static String runcmd(String cmd) {
    return runcmd(new String[]{cmd});
  }

  /**
   * run a system command finally using Java::Runtime.getRuntime().exec(args) and waiting for completion
   *
   * @param args the command as it would be given on command line splitted into the space devided parts, first part is
   *             the command, the rest are parameters and their values
   * @return the output produced by the command (sysout [+ "*** error ***" + syserr] if the syserr part is present, the
   * command might have failed
   */
  public static String runcmd(String args[]) {
    if (args.length == 0) {
      return "";
    }
    String NL = System.lineSeparator();
    boolean silent = false;
    if (args.length == 1) {
      String separator = "\"";
      ArrayList<String> argsx = new ArrayList<String>();
      StringTokenizer toks;
      String tok;
      String cmd = args[0];
      if (Settings.isWindows()) {
        cmd = cmd.replaceAll("\\\\ ", "%20;");
      }
      toks = new StringTokenizer(cmd);
      while (toks.hasMoreTokens()) {
        tok = toks.nextToken(" ");
        if (tok.length() == 0) {
          continue;
        }
        if (separator.equals(tok)) {
          continue;
        }
        if (tok.startsWith(separator)) {
          if (tok.endsWith(separator)) {
            tok = tok.substring(1, tok.length() - 1);
          } else {
            tok = tok.substring(1);
            tok += toks.nextToken(separator);
          }
        }
        argsx.add(tok.replaceAll("%20;", " "));
      }
      args = argsx.toArray(new String[0]);
    }
    if (args[0].startsWith("!")) {
      silent = true;
      args[0] = args[0].substring(1);
    }
    if (args[0].startsWith("#")) {
      String pgm = args[0].substring(1);
      args[0] = (new File(pgm)).getAbsolutePath();
      runcmd(new String[]{"chmod", "ugo+x", args[0]});
    }
    String result = "";
    String error = runCmdError + NL;
    String errorOut = "";
    boolean hasError = false;
    int retVal;
    try {
      if (!silent) {
        if (lvl <= Debug.getDebugLevel()) {
          log(lvl, arrayToQuotedString(args));
        } else {
          Debug.info("runcmd: " + arrayToQuotedString(args));
        }
      }
      //TODO use ProcessRunner
      Process process = Runtime.getRuntime().exec(args);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String s;
      while ((s = stdInput.readLine()) != null) {
        if (!s.isEmpty()) {
          result += s + NL;
        }
      }
      while ((s = stdError.readLine()) != null) {
        if (!s.isEmpty()) {
          errorOut += s + NL;
        }
      }
      if (!errorOut.isEmpty()) {
        error = error + errorOut;
        hasError = true;
      }
      process.waitFor();
      retVal = process.exitValue();
      process.destroy();
    } catch (Exception e) {
      log(-1, "fatal error: " + e);
      result = String.format(error + "%s", e);
      retVal = 9999;
      hasError = true;
    }
    if (hasError) {
      result += error;
    }
    lastResult = result;
    return String.format("%d%s%s", retVal, NL, result);
  }

  private static String lastResult = "";

  public static String getLastCommandResult() {
    return lastResult;
  }
//</editor-fold>
}
