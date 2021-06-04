/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.Settings;
import org.sikuli.support.gui.SXDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * INTERNAL USE: Support for accessing files and other ressources
 */
public class FileManager {

  private static String me = "FileManager";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + ": " + message, args);
  }

  static final int DOWNLOAD_BUFFER_SIZE = 153600;
  private static SXDialog _progress = null;
  private static final String EXECUTABLE = "#executable";

  private static int tryGetFileSize(URL aUrl) {
    HttpURLConnection conn = null;
    try {
      if (getProxy() != null) {
        conn = (HttpURLConnection) aUrl.openConnection(getProxy());
      } else {
        conn = (HttpURLConnection) aUrl.openConnection();
      }
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(30000);
      conn.setRequestMethod("HEAD");
      conn.getInputStream();
      return conn.getContentLength();
    } catch (Exception ex) {
      return 0;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  public static int isUrlUseabel(String sURL) {
    try {
      return isUrlUseabel(new URL(sURL));
    } catch (Exception ex) {
      return -1;
    }
  }

  public static int isUrlUseabel(URL aURL) {
    HttpURLConnection conn = null;
    try {
//			HttpURLConnection.setFollowRedirects(false);
      if (getProxy() != null) {
        conn = (HttpURLConnection) aURL.openConnection(getProxy());
      } else {
        conn = (HttpURLConnection) aURL.openConnection();
      }
//			con.setInstanceFollowRedirects(false);
      conn.setRequestMethod("HEAD");
      int retval = conn.getResponseCode();
//				HttpURLConnection.HTTP_BAD_METHOD 405
//				HttpURLConnection.HTTP_NOT_FOUND 404
      if (retval == HttpURLConnection.HTTP_OK) {
        return 1;
      } else if (retval == HttpURLConnection.HTTP_NOT_FOUND) {
        return 0;
      } else if (retval == HttpURLConnection.HTTP_FORBIDDEN) {
        return 0;
      } else {
        return -1;
      }
    } catch (Exception ex) {
      return -1;
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  public static Proxy getProxy() {
    Proxy proxy = Settings.proxy;
    if (!Settings.proxyChecked) {
      String phost = Settings.proxyName;
      String padr = Settings.proxyIP;
      String pport = Settings.proxyPort;
      InetAddress a = null;
      int p = -1;
      if (phost != null) {
        a = getProxyAddress(phost);
      }
      if (a == null && padr != null) {
        a = getProxyAddress(padr);
      }
      if (a != null && pport != null) {
        p = getProxyPort(pport);
      }
      if (a != null && p > 1024) {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(a, p));
        log(lvl, "Proxy defined: %s : %d", a.getHostAddress(), p);
      }
      Settings.proxyChecked = true;
      Settings.proxy = proxy;
    }
    return proxy;
  }

  public static InetAddress getProxyAddress(String arg) {
    try {
      return InetAddress.getByName(arg);
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  public static int getProxyPort(String p) {
    int port;
    int pDefault = 8080;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return -1;
      }
    } else {
      return pDefault;
    }
    return port;
  }

  public static boolean setProxy(String pName, String pPort) {
    InetAddress a = null;
    String host = null;
    String adr = null;
    int p = -1;
    if (pName != null) {
      a = getProxyAddress(pName);
      if (a == null) {
        a = getProxyAddress(pName);
        if (a != null) {
          adr = pName;
        }
      } else {
        host = pName;
      }
    }
    if (a != null && pPort != null) {
      p = getProxyPort(pPort);
    }
    if (a != null && p > 1024) {
      log(lvl, "Proxy stored: %s : %d", a.getHostAddress(), p);
      Settings.proxyChecked = true;
      Settings.proxyName = host;
      Settings.proxyIP = adr;
      Settings.proxyPort = pPort;
      Settings.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(a, p));
      PreferencesUser prefs = PreferencesUser.get();
      prefs.put("ProxyName", (host == null ? "" : host));
      prefs.put("ProxyIP", (adr == null ? "" : adr));
      prefs.put("ProxyPort", "" + p);
      return true;
    }
    return false;
  }

  /**
   * download a file at the given url to a local folder
   *
   * @param url       a valid url
   * @param localPath the folder where the file should go (will be created if necessary)
   * @return the absolute path to the downloaded file or null on any error
   */
  public static String downloadURL(URL url, String localPath) {
    String[] path = url.getPath().split("/");
    String filename = path[path.length - 1];
    String targetPath = null;
    int srcLength = 1;
    int srcLengthKB = 0;
    int done;
    int totalBytesRead = 0;
    File fullpath = new File(localPath);
    if (fullpath.exists()) {
      if (fullpath.isFile()) {
        log(-1, "download: target path must be a folder:\n%s", localPath);
        fullpath = null;
      }
    } else {
      if (!fullpath.mkdirs()) {
        log(-1, "download: could not create target folder:\n%s", localPath);
        fullpath = null;
      }
    }
    if (fullpath != null) {
      srcLength = tryGetFileSize(url);
      srcLengthKB = (int) (srcLength / 1024);
      if (srcLength > 0) {
        log(lvl, "Downloading %s having %d KB", filename, srcLengthKB);
      } else {
        log(lvl, "Downloading %s with unknown size", filename);
      }
      fullpath = new File(localPath, filename);
      targetPath = fullpath.getAbsolutePath();
      done = 0;
      if (_progress != null) {
        //TODO progress popup
//        _progress.setProFile(filename);
//        _progress.setProSize(srcLengthKB);
//        _progress.setProDone(0);
        _progress.setVisible(true);
      }
      InputStream reader = null;
      FileOutputStream writer = null;
      try {
        writer = new FileOutputStream(fullpath);
        if (getProxy() != null) {
          reader = url.openConnection(getProxy()).getInputStream();
        } else {
          reader = url.openConnection().getInputStream();
        }
        byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
        int bytesRead = 0;
        long begin_t = (new Date()).getTime();
        long chunk = (new Date()).getTime();
        while ((bytesRead = reader.read(buffer)) > 0) {
          writer.write(buffer, 0, bytesRead);
          totalBytesRead += bytesRead;
          if (srcLength > 0) {
            done = (int) ((totalBytesRead / (double) srcLength) * 100);
          } else {
            done = (int) (totalBytesRead / 1024);
          }
          if (((new Date()).getTime() - chunk) > 1000) {
            if (_progress != null) {
              //TODO progress popup: _progress.setProDone(done);
            }
            chunk = (new Date()).getTime();
          }
        }
        writer.close();
        log(lvl, "downloaded %d KB to:\n%s", (int) (totalBytesRead / 1024), targetPath);
        log(lvl, "download time: %d", (int) (((new Date()).getTime() - begin_t) / 1000));
      } catch (Exception ex) {
        log(-1, "problems while downloading\n%s", ex);
        targetPath = null;
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException ex) {
          }
        }
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException ex) {
          }
        }
      }
      if (_progress != null) {
        //TODO progress popup
//        if (targetPath == null) {
//          _progress.setProDone(-1);
//        } else {
//          if (srcLength <= 0) {
//            _progress.setProSize((int) (totalBytesRead / 1024));
//          }
//          _progress.setProDone(100);
//        }
//        _progress.closeAfter(3);
        _progress = null;
      }
    }
    if (targetPath == null) {
      fullpath.delete();
    }
    return targetPath;
  }

  /**
   * download a file at the given url to a local folder
   *
   * @param url       a string representing a valid url
   * @param localPath the folder where the file should go (will be created if necessary)
   * @return the absolute path to the downloaded file or null on any error
   */
  public static String downloadURL(String url, String localPath) {
    URL urlSrc = null;
    try {
      urlSrc = new URL(url);
    } catch (MalformedURLException ex) {
      log(-1, "download: bad URL: " + url);
      return null;
    }
    return downloadURL(urlSrc, localPath);
  }

  public static String downloadURL(String url, String localPath, JFrame progress) {
    //TODO progress popup: _progress = (SplashFrame) progress;
    return downloadURL(url, localPath);
  }

  public static String downloadURLtoString(String src) {
    boolean silent = false;
    if (src.startsWith("#")) {
      silent = true;
      src = src.substring(1);
    }
    URL url = null;
    try {
      url = new URL(src);
    } catch (MalformedURLException ex) {
      log(-1, "download to string: bad URL:\n%s", src);
      return null;
    }
    return downloadURLtoString(url, silent);
  }

  public static String downloadURLtoString(URL uSrc) {
    return downloadURLtoString(uSrc, false);
  }

  public static String downloadURLtoString(URL uSrc, boolean silent) {
    String content = "";
    InputStream reader = null;
    if (!silent) {
      log(lvl, "download to string from:\n%s,", uSrc);
    }
    try {
      if (getProxy() != null) {
        reader = uSrc.openConnection(getProxy()).getInputStream();
      } else {
        reader = uSrc.openConnection().getInputStream();
      }
      byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
      int bytesRead = 0;
      while ((bytesRead = reader.read(buffer)) > 0) {
        content += (new String(Arrays.copyOfRange(buffer, 0, bytesRead), Charset.forName("utf-8")));
      }
    } catch (Exception ex) {
      if (!silent) {
        log(-1, "problems while downloading\n" + ex.getMessage());
      }
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
        }
      }
    }
    return content;
  }

  /**
   * open the given url in the standard browser
   *
   * @param url string representing a valid url
   * @return false on error, true otherwise
   */
  public static boolean openURL(String url) {
    try {
      URL u = new URL(url);
      Desktop.getDesktop().browse(u.toURI());
    } catch (Exception ex) {
      log(-1, "show in browser: bad URL: " + url);
      return false;
    }
    return true;
  }

  public static int getRandomInt() {
    int rand = 1 + new Random().nextInt();
    return (rand < 0 ? rand * -1 : rand);
  }

  public static boolean deleteFileOrFolder(File fPath, FileFilter filter) {
    return doDeleteFileOrFolder(fPath, filter);
  }

  public static boolean deleteFileOrFolder(File fPath) {
    return doDeleteFileOrFolder(fPath, null);
  }

  public static boolean deleteFileOrFolder(String fpPath, FileFilter filter) {
    if (fpPath.startsWith("#")) {
      fpPath = fpPath.substring(1);
    } else {
      log(lvl, "deleteFileOrFolder: %s\n%s", (filter == null ? "" : "filtered: "), fpPath);
    }
    return doDeleteFileOrFolder(new File(fpPath), filter);
  }

  public static boolean deleteFileOrFolder(String fpPath) {
    if (fpPath.startsWith("#")) {
      fpPath = fpPath.substring(1);
    } else {
      log(lvl, "deleteFileOrFolder:\n%s", fpPath);
    }
    return doDeleteFileOrFolder(new File(fpPath), null);
  }

  public static void resetFolder(File fPath) {
    log(lvl, "resetFolder:\n%s", fPath);
    doDeleteFileOrFolder(fPath, null);
    fPath.mkdirs();
  }

  private static boolean doDeleteFileOrFolder(File fPath, FileFilter filter) {
    if (fPath == null) {
      return false;
    }
    File aFile;
    String[] entries;
    boolean somethingLeft = false;
    if (fPath.exists() && fPath.isDirectory()) {
      entries = fPath.list();
      for (int i = 0; i < entries.length; i++) {
        aFile = new File(fPath, entries[i]);
        if (filter != null && !filter.accept(aFile)) {
          somethingLeft = true;
          continue;
        }
        if (aFile.isDirectory()) {
          if (!doDeleteFileOrFolder(aFile, filter)) {
            return false;
          }
        } else {
          try {
            aFile.delete();
          } catch (Exception ex) {
            log(-1, "deleteFile: not deleted:\n%s\n%s", aFile, ex);
            return false;
          }
        }
      }
    }
    // deletes intermediate empty directories and finally the top now empty dir
    if (!somethingLeft && fPath.exists()) {
      try {
        fPath.delete();
      } catch (Exception ex) {
        log(-1, "deleteFolder: not deleted:\n" + fPath.getAbsolutePath() + "\n" + ex.getMessage());
        return false;
      }
    }
    return true;
  }

  public static void traverseFolder(File fPath, FileFilter filter) {
    if (fPath == null) {
      return;
    }
    File aFile;
    String[] entries;
    if (fPath.isDirectory()) {
      entries = fPath.list();
      for (int i = 0; i < entries.length; i++) {
        aFile = new File(fPath, entries[i]);
        if (filter != null) {
          filter.accept(aFile);
        }
        if (aFile.isDirectory()) {
          traverseFolder(aFile, filter);
        }
      }
    }
  }

  public static File createTempFile(String suffix) {
    return createTempFile(suffix, null);
  }

  public static File createTempFile(String suffix, String path) {
    String fPrefix = "sikulitemp-";
    String fSuffix = "." + suffix;
    File fpath = Commons.getIDETemp();
    if (path != null) {
      fpath = new File(path);
    }
    try {
      fpath.mkdirs();
      File temp = File.createTempFile(fPrefix, fSuffix, fpath);
      temp.deleteOnExit();
      return temp;
    } catch (IOException ex) {
      log(-1, "createTempFile: IOException: %s\n%s", ex.getMessage(),
          fpath + File.separator + fPrefix + "12....56" + fSuffix);
      return null;
    }
  }

  public static String saveTimedImage(BufferedImage img, String path, String name) {
    RunTime.pause(0.01f);
    if (null == path) {
      return null;
    }
    File fImage = new File(path, name);
    String formatName = "png";
    if (name == null) {
      fImage = new File(path, String.format("noname-%d.png", name, new Date().getTime()));
    } else if (name.startsWith("#")) {
      fImage = new File(path, String.format("%s-%d.png", name.substring(1), new Date().getTime()));
    } else if (!name.isEmpty()) {
      if (!name.contains(".")) {
        fImage = new File(path, name + ".png");
      } else {
        formatName = name.substring(name.lastIndexOf(".") + 1);
      }
    }
    try {
      ImageIO.write(img, formatName, fImage);
      log(3, "saveImage: %s", fImage);
    } catch (Exception ex) {
      log(-1, "saveTimedImage: did not work: %s (%s)", fImage, ex.getMessage());
      return null;
    }
    return fImage.getAbsolutePath();
  }

  public static boolean xcopy(File fSrc, File fDest) {
    if (fSrc == null || fDest == null) {
      return false;
    }
    try {
      doXcopy(fSrc, fDest, null);
    } catch (Exception ex) {
      log(lvl, "xcopy from: %s\nto: %s\n%s", fSrc, fDest, ex);
      return false;
    }
    return true;
  }

  public static boolean xcopy(File fSrc, File fDest, FileFilter filter) {
    if (fSrc == null || fDest == null) {
      return false;
    }
    try {
      doXcopy(fSrc, fDest, filter);
    } catch (Exception ex) {
      log(lvl, "xcopy from: %s\nto: %s\n%s", fSrc, fDest, ex);
      return false;
    }
    return true;
  }

  public static void xcopy(String src, String dest) throws IOException {
    doXcopy(new File(src), new File(dest), null);
  }

  public static void xcopy(String src, String dest, FileFilter filter) throws IOException {
    doXcopy(new File(src), new File(dest), filter);
  }

  private static void doXcopy(File fSrc, File fDest, FileFilter filter) throws IOException {
    if (fSrc.getAbsolutePath().equals(fDest.getAbsolutePath())) {
      return;
    }
    if (fSrc.isDirectory()) {
      if (filter == null || filter.accept(fSrc)) {
        if (!fDest.exists()) {
          fDest.mkdirs();
        }
        String[] children = fSrc.list();
        for (String child : children) {
          if (child.equals(fDest.getName())) {
            continue;
          }
          doXcopy(new File(fSrc, child), new File(fDest, child), filter);
        }
      }
    } else {
      if (filter == null || filter.accept(fSrc)) {
        try {
          if (fDest.isDirectory()) {
            fDest = new File(fDest, fSrc.getName());
          }
          InputStream in = new FileInputStream(fSrc);
          OutputStream out = new FileOutputStream(fDest);
          // Copy the bits from instream to outstream
          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }
          in.close();
          out.close();
        } catch (IOException ex) {
          log(-1, "xcopy: %s to: %s (%s)", fSrc, fDest, ex.getMessage());
          throw new IOException(ex.getMessage(), ex.getCause());
        }
      }
    }
  }

  public static String slashify(String path, Boolean isDirectory) {
    if (path != null) {
      if (path.contains("%")) {
        try {
          path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception ex) {
          log(-1, "slashify: filename might not be useable: %s", path);
        }
      }
      if (File.separatorChar != '/') {
        path = path.replace(File.separatorChar, '/');
      }
      if (isDirectory != null) {
        if (isDirectory) {
          if (!path.endsWith("/")) {
            path = path + "/";
          }
        } else if (path.endsWith("/")) {
          path = path.substring(0, path.length() - 1);
        }
      }
      if (path.startsWith("./")) {
        path = path.substring(2);
      }
      log(lvl, "slashify: file: %s", path);
      return path;
    } else {
      log(lvl, "slashify: file: null");
      return "";
    }
  }

  public static String normalize(String path) {
    String pathNormalized = path;
    if (path != null) {
      if (path.contains("%")) {
        try {
          pathNormalized = URLDecoder.decode(path, "UTF-8");
        } catch (Exception ex) {
        }
      }
      if (!new File(pathNormalized).isAbsolute() && pathNormalized.startsWith("\\")) {
        pathNormalized = new File(pathNormalized).getAbsoluteFile().getPath();
      }
    }
    return pathNormalized;
  }

  public static String normalizeAbsolute(String filename) {
    filename = normalize(filename);
    String jarSuffix = "";
    int nJarSuffix;
    if (0 < (nJarSuffix = filename.indexOf(".jar!"))) {
      jarSuffix = filename.substring(nJarSuffix + 4);
      filename = filename.substring(0, nJarSuffix + 4);
    }
    File aFile = new File(filename);
    try {
      aFile = aFile.getCanonicalFile();
    } catch (Exception ex) {
    }
    log(lvl + 1, "normalizeAbsolute: file: %s", aFile);
    return aFile.getPath() + jarSuffix;
  }

  public static boolean isFilenameDotted(String name) {
    String nameParent = new File(name).getParent();
    if (nameParent != null && nameParent.contains(".")) {
      return true;
    }
    return false;
  }

  public static URL makeURL(String fName) {
    return makeURL(fName, "file");
  }

  public static URL makeURL(String fName, String type) {
    try {
      if ("file".equals(type)) {
        fName = normalizeAbsolute(fName);
        if (!fName.startsWith("/")) {
          fName = "/" + fName;
        }
      }
      if ("jar".equals(type)) {
        if (!fName.contains("!/")) {
          fName += "!/";
        }
        fName = fName.startsWith("file:") ? "jar:" + fName : "jar:file:" + fName;
        URL url = new URL(fName);
        return url;
      } else if ("file".equals(type)) {
        File aFile = new File(fName);
        if (aFile.exists() && aFile.isDirectory()) {
          if (!fName.endsWith("/")) {
            fName += "/";
          }
        }
      }
      return new URL(type, null, fName);
    } catch (MalformedURLException ex) {
      return null;
    }
  }

  public static int getPort(String p) {
    int port;
    int pDefault = 50000;
    if (p != null) {
      try {
        port = Integer.parseInt(p);
      } catch (NumberFormatException ex) {
        return -1;
      }
    } else {
      return pDefault;
    }
    if (port < 1024) {
      port += pDefault;
    }
    return port;
  }

  public static String getAddress(String arg) {
    try {
      if (arg == null) {
        return InetAddress.getLocalHost().getHostAddress();
      }
      return InetAddress.getByName(arg).getHostAddress();
    } catch (UnknownHostException ex) {
      return null;
    }
  }

  public static boolean writeStringToFile(String text, File fPath) {
    PrintStream out = null;
    try {
      out = new PrintStream(new FileOutputStream(fPath), false, "UTF-8");
      out.print(text);
    } catch (Exception e) {
      log(-1, "writeStringToFile: did not work: " + fPath + "\n" + e.getMessage());
    }
    if (out != null) {
      out.close();
      return true;
    }
    return false;
  }

  public static String readFileToString(File fPath) {
    try {
      return doReadFileToString(fPath);
    } catch (Exception ex) {
      return "";
    }
  }

  public static String[] readFileToStringArray(File fPath) {
    String[] array = new String[0];
    final String content = readFileToString(fPath);
    if (!content.strip().isEmpty()) {
      array = content.strip().split("\n");
      if (array.length > 0) {
        for (int n = 0; n < array.length; n++) {
          array[n] = array[n].strip();
        }
      }
    }
    return array;
  }

  private static String doReadFileToString(File fPath) throws IOException {
    StringBuilder result = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(fPath));
      char[] buf = new char[1024];
      int r = 0;
      while ((r = reader.read(buf)) != -1) {
        result.append(buf, 0, r);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return result.toString();
  }

  public static String makeScriptjar(List<String> options) {
    File fSikulixTemp = new File(Commons.getAppDataStore(), "SikulixTemp");
    FileManager.resetFolder(fSikulixTemp);
    String target = doMakeScriptjar(options, fSikulixTemp);
    deleteFileOrFolder(fSikulixTemp);
    return target;
  }

  private static String doMakeScriptjar(List<String> options, File fSikulixTemp) {
    boolean makingScriptjarPlain = false;
    if (options.size() > 0 && "plain".equals(options.get(0))) {
      makingScriptjarPlain = true;
      options.remove(0);
    }
    File scriptFile = null;
    File scriptFolder = null;
    File scriptFolderSikuli = null;
    String scriptName = null;
    String[] fileList = new String[]{null, null, null};
    String[] preList = new String[]{null, null, null};
    if (options.size() > 0) {
      scriptFolder = new File(options.get(0));
      if (!scriptFolder.exists()) {
        scriptFolderSikuli = new File(scriptFolder.getAbsolutePath() + ".sikuli");
        if (!scriptFolderSikuli.exists()) {
          log(-1, "makingScriptJar: script folder invalid: " + scriptFolder.getAbsolutePath());
          return null;
        }
      } else {
        if (scriptFolder.getAbsolutePath().endsWith(".sikuli")) {
          scriptFolderSikuli = scriptFolder;
        } else {
          scriptFile = new File(scriptFolder, "__run__.py");
          if (!scriptFile.exists()) {
            log(-1, "makingScriptJar: script file missing: " + scriptFile.getAbsolutePath());
            return null;
          }
        }
      }
    } else {
      log(-1, "makingScriptJar: no script file given");
      return null;
    }

    String fpScriptJar = "";
    File fScriptSource = new File(fSikulixTemp, "scriptSource");
    File fScriptCompiled = new File(fSikulixTemp, "scriptCompiled");
    File fWorkdir = scriptFolder.getParentFile();
    FileFilter skipCompiled = new FileManager.FileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().contains("$py.class")) {
          return false;
        }
        return true;
      }
    };
    if (null != scriptFolderSikuli) {
      log(lvl, "makingScriptJar: compiling sikuli script: %s", scriptFolderSikuli);
      fWorkdir = scriptFolderSikuli.getParentFile();
      scriptName = scriptFolder.getName().replace(".sikuli", "");
      fpScriptJar = scriptName + "_sikuli.jar";
      scriptFile = new File(scriptFolderSikuli, scriptName + ".py");
      if (!scriptFile.exists()) {
        log(-1, "makingScriptJar: script folder invalid: " + scriptFolderSikuli.getAbsolutePath());
        return null;
      }
      xcopy(scriptFolderSikuli, fScriptSource, skipCompiled);
      String script = "";
      String prolog = "import org.sikuli.script.SikulixForJython\n" +
          "from sikuli import *\n" +
          "Debug.on(3)\n" +
          "for e in sys.path:\n" +
          "    print e\n" +
          "    if e.endswith(\".jar\"):\n" +
          "        jar = e\n" +
          "        break\n" +
          "ImagePath.addJar(jar, \"\")\n" +
          "import " + scriptName + "\n";
      FileManager.writeStringToFile(prolog + script, new File(fScriptSource, "__run__.py"));
      FileManager.writeStringToFile(prolog + script, new File(fScriptSource, "__main__.py"));
      script = FileManager.readFileToString(new File(fScriptSource, scriptName + ".py"));
      prolog = "from sikuli import *\naddImportPath(getBundlePath())\n";
      FileManager.writeStringToFile(prolog + script, new File(fScriptSource, scriptName + ".py"));
    } else {
      log(lvl, "makingScriptJar: compiling plain script: %s", scriptFolder);
      xcopy(scriptFolder, fScriptSource, skipCompiled);
    }

    Commons.runFunctionScriptingSupport("compileJythonFolder",
        new Object[]{fScriptSource.getAbsolutePath(), fScriptCompiled.getAbsolutePath()});
    FileManager.xcopy(fScriptCompiled, fSikulixTemp);
    FileManager.deleteFileOrFolder(fScriptSource);
    FileManager.deleteFileOrFolder(fScriptCompiled);
    fileList[0] = fSikulixTemp.getAbsolutePath();

    String[] jarsList = new String[]{null, null};
    if (!makingScriptjarPlain) {
//      File fJarRunner = new File(runTime.fSikulixExtensions, "archiv");
//      fJarRunner = new File(fJarRunner, "JarRunner.class");
//      File fJarRunnerDir = new File(fSikulixTemp, "org/python/util");
//      fJarRunnerDir.mkdirs();
//      FileManager.xcopy(fJarRunner, fJarRunnerDir);

      String manifest = "Manifest-Version: 1.0\nMain-Class: org.sikuli.support.SikulixRun\n";
      File fMetaInf = new File(fSikulixTemp, "META-INF");
      fMetaInf.mkdir();
      FileManager.writeStringToFile(manifest, new File(fMetaInf, "MANIFEST.MF"));
      Class<?> cSikulixRun = null;
      try {
        cSikulixRun = Class.forName("org.sikuli.support.support.SikulixRun");
      } catch (ClassNotFoundException e) {
      }
      if (null != cSikulixRun) {
        InputStream resourceAsStream = cSikulixRun.getResourceAsStream("SikulixRun.class");
        File targetFile = new File(fSikulixTemp, "org/sikuli/support/support");
        targetFile.mkdirs();
        targetFile = new File(targetFile, "SikulixRun.class");
        try {
          byte[] buffer = new byte[resourceAsStream.available()];
          resourceAsStream.read(buffer);
          OutputStream outStream = new FileOutputStream(targetFile);
          outStream.write(buffer);
          outStream.close();
        } catch (IOException e) {
          cSikulixRun = null;
        }
      }
      if (null == cSikulixRun) {
        log(-1, "makingRunnableScriptJar: problems creating main class org.sikuli.support.SikulixRun");
        return null;
      }
    }

    String targetJar = (new File(fWorkdir, fpScriptJar)).getAbsolutePath();
    if (!buildJar(targetJar, jarsList, fileList, preList, new FileManager.JarFileFilter() {
      @Override
      public boolean accept(ZipEntry entry, String jarname) {
        if (entry.getName().startsWith("META-INF")) {
          return false;
        }
        return true;
      }
    })) {
      log(-1, "makingScriptJar: problems building jar - for details see logfile");
      return null;
    }
    log(lvl, "makingScriptJar: ended successfully: %s", targetJar);
    return targetJar;
  }

  public static boolean packJar(String folderName, String jarName, String prefix) {
    jarName = FileManager.slashify(jarName, false);
    if (!jarName.endsWith(".jar")) {
      jarName += ".jar";
    }
    folderName = FileManager.slashify(folderName, true);
    if (!(new File(folderName)).isDirectory()) {
      log(-1, "packJar: not a directory or does not exist: " + folderName);
      return false;
    }
    try {
      File dir = new File((new File(jarName)).getAbsolutePath()).getParentFile();
      if (dir != null) {
        if (!dir.exists()) {
          dir.mkdirs();
        }
      } else {
        throw new Exception("workdir is null");
      }
      log(lvl, "packJar: %s from %s in workDir %s", jarName, folderName, dir.getAbsolutePath());
      if (!folderName.startsWith("http://") && !folderName.startsWith("https://")) {
        folderName = "file://" + (new File(folderName)).getAbsolutePath();
      }
      URL src = new URL(folderName);
      JarOutputStream jout = new JarOutputStream(new FileOutputStream(jarName));
      addToJar(jout, new File(src.getFile()), prefix);
      jout.close();
    } catch (Exception ex) {
      log(-1, "packJar: " + ex.getMessage());
      return false;
    }
    log(lvl, "packJar: completed");
    return true;
  }

  public static boolean buildJar(String targetJar, String[] jars,
                                 String[] files, String[] prefixs, FileManager.JarFileFilter filter) {
    boolean logShort = false;
    if (targetJar.startsWith("#")) {
      logShort = true;
      targetJar = targetJar.substring(1);
      log(lvl, "buildJar: %s", new File(targetJar).getName());
    } else {
      log(lvl, "buildJar:\n%s", targetJar);
    }
    try {
      JarOutputStream jout = new JarOutputStream(new FileOutputStream(targetJar));
      ArrayList done = new ArrayList();
      for (int i = 0; i < jars.length; i++) {
        if (jars[i] == null) {
          continue;
        }
        if (logShort) {
          log(lvl, "buildJar: adding: %s", new File(jars[i]).getName());
        } else {
          log(lvl, "buildJar: adding:\n%s", jars[i]);
        }
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(jars[i]));
        ZipInputStream zin = new ZipInputStream(bin);
        for (ZipEntry zipentry = zin.getNextEntry(); zipentry != null; zipentry = zin.getNextEntry()) {
          if (filter == null || filter.accept(zipentry, jars[i])) {
            if (!done.contains(zipentry.getName())) {
              jout.putNextEntry(zipentry);
              if (!zipentry.isDirectory()) {
                bufferedWrite(zin, jout);
              }
              done.add(zipentry.getName());
              log(lvl + 1, "adding: %s", zipentry.getName());
            }
          }
        }
        zin.close();
        bin.close();
      }
      if (files != null) {
        for (int i = 0; i < files.length; i++) {
          if (files[i] == null) {
            continue;
          }
          if (logShort) {
            log(lvl, "buildJar: adding %s at %s", new File(files[i]).getName(), prefixs[i]);
          } else {
            log(lvl, "buildJar: adding %s at %s", files[i], prefixs[i]);
          }
          addToJar(jout, new File(files[i]), prefixs[i]);
        }
      }
      jout.close();
    } catch (Exception ex) {
      log(-1, "buildJar: %s", ex);
      return false;
    }
    log(lvl, "buildJar: completed");
    return true;
  }

  /**
   * unpack a jar file to a folder
   *
   * @param jarName    absolute path to jar file
   * @param folderName absolute path to the target folder
   * @param del        true if the folder should be deleted before unpack
   * @param strip      true if the path should be stripped
   * @param filter     to select specific content
   * @return true if success,  false otherwise
   */
  public static boolean unpackJar(String jarName, String folderName, boolean del, boolean strip,
                                  FileManager.JarFileFilter filter) {
    jarName = FileManager.slashify(jarName, false);
    if (!jarName.endsWith(".jar")) {
      jarName += ".jar";
    }
    if (!new File(jarName).isAbsolute()) {
      log(-1, "unpackJar: jar path not absolute");
      return false;
    }
    if (folderName == null) {
      folderName = jarName.substring(0, jarName.length() - 4);
    } else if (!new File(folderName).isAbsolute()) {
      log(-1, "unpackJar: folder path not absolute");
      return false;
    }
    folderName = FileManager.slashify(folderName, true);
    ZipInputStream in;
    BufferedOutputStream out;
    try {
      if (del) {
        FileManager.deleteFileOrFolder(folderName);
      }
      in = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarName)));
      log(lvl, "unpackJar: %s to %s", jarName, folderName);
      boolean isExecutable;
      int n;
      File f;
      for (ZipEntry z = in.getNextEntry(); z != null; z = in.getNextEntry()) {
        if (filter == null || filter.accept(z, null)) {
          if (z.isDirectory()) {
            (new File(folderName, z.getName())).mkdirs();
          } else {
            n = z.getName().lastIndexOf(EXECUTABLE);
            if (n >= 0) {
              f = new File(folderName, z.getName().substring(0, n));
              isExecutable = true;
            } else {
              f = new File(folderName, z.getName());
              isExecutable = false;
            }
            if (strip) {
              f = new File(folderName, f.getName());
            } else {
              f.getParentFile().mkdirs();
            }
            out = new BufferedOutputStream(new FileOutputStream(f));
            bufferedWrite(in, out);
            out.close();
            if (isExecutable) {
              f.setExecutable(true, false);
            }
          }
        }
      }
      in.close();
    } catch (Exception ex) {
      log(-1, "unpackJar: " + ex.getMessage());
      return false;
    }
    log(lvl, "unpackJar: completed");
    return true;
  }

  private static void addToJar(JarOutputStream jar, File dir, String prefix) throws IOException {
    File[] content;
    prefix = prefix == null ? "" : prefix;
    if (dir.isDirectory()) {
      content = dir.listFiles();
      for (int i = 0, l = content.length; i < l; ++i) {
        if (content[i].isDirectory()) {
          jar.putNextEntry(new ZipEntry(prefix + (prefix.equals("") ? "" : "/") + content[i].getName() + "/"));
          addToJar(jar, content[i], prefix + (prefix.equals("") ? "" : "/") + content[i].getName());
        } else {
          addToJarWriteFile(jar, content[i], prefix);
        }
      }
    } else {
      addToJarWriteFile(jar, dir, prefix);
    }
  }

  private static void addToJarWriteFile(JarOutputStream jar, File file, String prefix) throws IOException {
    if (file.getName().startsWith(".")) {
      return;
    }
    String suffix = "";
//TODO buildjar: suffix EXECUTABL
//    if (file.canExecute()) {
//      suffix = EXECUTABLE;
//    }
    jar.putNextEntry(new ZipEntry(prefix + (prefix.equals("") ? "" : "/") + file.getName() + suffix));
    FileInputStream in = new FileInputStream(file);
    bufferedWrite(in, jar);
    in.close();
  }

/*
  public static String unzipSKL(String fpSkl) {
    File fSkl = new File(fpSkl);
    if (!fSkl.exists()) {
      log(-1, "unzipSKL: file not found: %s", fpSkl);
    }
    String name = fSkl.getName();
    name = name.substring(0, name.lastIndexOf('.'));
    File fSikuliDir = FileManager.createTempDir(name + ".sikuli");
    if (null != fSikuliDir) {
      fSikuliDir.deleteOnExit();
      FileManager.unzip(fSkl, fSikuliDir);
    }
    if (null == fSikuliDir) {
      log(-1, "unzipSKL: not possible for:\n%s", fpSkl);
      return null;
    }
    return fSikuliDir.getAbsolutePath();
  }
*/

  static class JarFileFilter {
    boolean accept(ZipEntry entry, String jarname) {
      return true;
    }
  }

  public static class FileFilter {
    public boolean accept(File entry) {
      return true;
    }
  }

  private static synchronized void bufferedWrite(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024 * 512];
    int read;
    while (true) {
      read = in.read(buffer);
      if (read == -1) {
        break;
      }
      out.write(buffer, 0, read);
    }
    out.flush();
  }

  public static boolean pathEquals(String path1, String path2) {
    return new File(path1).equals(new File(path2));
  }

}
