/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

/**
 * maintain the path list of locations, where images will be searched.
 * <br>the first entry always is the bundlepath used on the scripting level<br>
 * Python import automatically adds a sikuli bundle here<br>
 * supported locations:<br>
 * - absolute filesystem paths<br>
 * - inside jars relative to root level given by a class found on classpath<br>
 * - a location in the web given as string starting with http[s]://<br>
 * - any location as a valid URL, from where image files can be loaded<br>
 */
public class ImagePath {

  private static final String me = "ImagePath: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  //<editor-fold desc="01 path list">
  private static final List<PathEntry> imagePaths = Collections.synchronizedList(new ArrayList<PathEntry>());

  static {
    imagePaths.add(null);
  }

  /**
   * get the list of path entries (as PathEntry)
   *
   * @return pathentries
   */
  public static List<PathEntry> getPaths() {
    return imagePaths;
  }

  private static int getCount() {
    int count = imagePaths.size();
    for (PathEntry path : imagePaths) {
      if (path == null) {
        count--;
      }
    }
    return count;
  }

  private static URL remove(int entry) {
    if (entry > 0 && entry < imagePaths.size()) {
      PathEntry pathEntry = imagePaths.remove(entry);
      Image.purge(pathEntry);
      return pathEntry.pathURL;
    }
    return null;
  }

  /**
   * the path list as string array
   *
   * @return an array of the file path's currently in the path list
   */
  public static String[] get() {
    int i = 0;
    String[] paths = new String[getPaths().size()];
    i = 0;
    for (PathEntry p : imagePaths) {
      paths[i++] = p.getPath();
    }
    return paths;
  }

  /**
   * print the list of path entries
   *
   * @param lvl debug level to use
   */
  public static void dump(int lvl) {
    log(lvl, "ImagePath has %d entries (valid %d)", imagePaths.size(), getCount());
    int i = 0;
    for (PathEntry p : imagePaths) {
      if (i == 0) {
        log(lvl, "BundlePath: %s", (p == null ? "--- not set ---" : p.getPath()));
      } else {
        log(lvl, "Path %d: %s", i, p.getPath());
      }
      i++;
    }
  }

  /**
   * empty path list and keep bundlePath (entry 0)<br>
   * Image cache is cleared completely
   * convenience for the scripting level
   */
  public static void reset() {
    log(lvl, "reset");
    if (imagePaths.isEmpty()) {
      return;
    }
    for (PathEntry pathEntry : imagePaths) {
      if (pathEntry == null) {
        continue;
      }
      Image.purge(pathEntry);
    }
    PathEntry bundlePath = getBundle();
    imagePaths.clear();
    imagePaths.add(bundlePath);
  }

  /**
   * empty path list and add path as bundle path (entry 0)
   * Image cache is cleared completely
   *
   * @param path absolute path
   * @return true on success, false otherwise
   */
  public static boolean reset(String path) {
    reset();
    if (bundleEquals(path)) {
      return true;
    }
    return setBundlePath(path);
  }

  public static void clear() {
    reset();
    log(lvl, "clear");
    Image.purge();
    imagePaths.set(0, null);
  }
  //</editor-fold>

  //<editor-fold desc="02 path entry">

  /**
   * represents an imagepath entry
   */
  public static class PathEntry {

    private URL pathURL = null;
    private String path = null;

    private PathEntry(String main, String sub, URL eqivalentURL) {
      if (main == null) {
        main = "";
      }
      if (sub == null) {
        sub = "";
      }
      path = main + (sub.isEmpty() ? sub : "+" + sub);
      pathURL = eqivalentURL;
      log(lvl + 1, "ImagePathEntry: %s (%s)", pathURL, path);
    }

    @Override
    public boolean equals(Object other) {
      if (pathURL == null) {
        return false;
      }
      if (other instanceof PathEntry) {
        return pathURL.toExternalForm().equals(((PathEntry) other).pathURL.toExternalForm());
      } else {
        if (other instanceof URL) {
          return pathURL.toExternalForm().equals(((URL) other).toExternalForm());
        } else if (other instanceof String || other instanceof File) {
          if (isFile() || isJar()) {
            return pathURL.toExternalForm().equals(getPathEntry(other, null).pathURL.toExternalForm());
          }
          return false;
        }
        return false;
      }
    }

    @Override
    public String toString() {
      return getPath();
    }

    public boolean isValid() {
      return pathURL != null;
    }

    public boolean isFile() {
      if (pathURL == null) {
        return false;
      }
      return "file".equals(pathURL.getProtocol());
    }

    public boolean isJar() {
      if (pathURL == null) {
        return false;
      }
      return "jar".equals(pathURL.getProtocol());
    }

    public boolean isHTTP() {
      if (pathURL == null) {
        return false;
      }
      return pathURL.getProtocol().startsWith("http");
    }

    public String getPath() {
      String path = "--invalid--";
      if (isValid()) {
        if (isHTTP()) {
          return pathURL.toExternalForm();
        } else {
          File file = getFile();
          if (file != null) {
            return file.getPath();
          }
        }
      }
      return path;
    }

    public File getFile() {
      if (isValid()) {
        return Commons.urlToFile(pathURL);
      }
      return null;
    }

    public URL getURL() {
      return pathURL;
    }
  }

  private static boolean hasPathEntry(PathEntry pathEntry) {
    PathEntry bundle = getBundle();
    if (imagePaths.size() == 1 && bundle == null) {
      return false;
    }
    if (isBundle(pathEntry)) {
      return true;
    }
    return isPathEntry(pathEntry) == 0 ? false : true;
  }

  private static int isPathEntry(PathEntry pathEntry) {
    if (null != pathEntry) {
      int i = 1;
      for (PathEntry entry : imagePaths.subList(1, imagePaths.size())) {
        if (entry != null && entry.equals(pathEntry)) {
          return i;
        }
        i++;
      }
    }
    return 0;
  }

  private static PathEntry getPathEntry(Object path, String folder) {
    PathEntry pathEntry = null;
    if (null != path) {
      if (path instanceof String) {
        pathEntry = createPathEntry((String) path, folder);
      } else if (path instanceof File) {
        pathEntry = createPathEntry(((File) path).getAbsolutePath(), folder);
      } else if (path instanceof URL) {
        if (folder == null) {
          pathEntry = new PathEntry("__PATH_URL__", null, (URL) path);
        } else {
          //TODO getPathEntry: url + folder not implmented
          log(-1, "getPathEntry: url + folder not implmented");
          return null;
        }
      } else {
        log(-1, "getPathEntry: invalid path: %s (String, File or URL");
        return null;
      }
    }
    return pathEntry;
  }

  private static int getPathEntryIndex(URL url) {
    PathEntry whereEntry = getPathEntry(url, null);
    return isPathEntry(whereEntry);
  }

  private static PathEntry createPathEntry(String mainPath, String altPathOrFolder) {
    if (mainPath == null || mainPath.isEmpty()) {
      return null;
    }
    if (mainPath.toLowerCase().endsWith(".jar")) {
      return createPathEntryJar(mainPath, altPathOrFolder);
    }
    if (mainPath.toLowerCase().startsWith("http://") || mainPath.toLowerCase().startsWith("https://")) {
      return createPathEntryHttp(mainPath, altPathOrFolder);
    }
    if (mainPath.indexOf(":") > 4) {
      String[] parts = mainPath.split(":");
      mainPath = "http://" + parts[0];
      if (parts.length > 1) {
        return createPathEntryHttp(mainPath + "/" + parts[1], altPathOrFolder);
      } else {
        return createPathEntryHttp(mainPath, altPathOrFolder);
      }
    }
    URL pathURL = Commons.makeURL(mainPath, altPathOrFolder);
    if (pathURL == null) {
      return createPathEntryClass(mainPath, altPathOrFolder);
    }
    return new PathEntry(mainPath, altPathOrFolder, pathURL);
  }

  private static PathEntry createPathEntryJar(String jar, String folder) {
    PathEntry pathEntry;
    if (".".equals(jar)) {
      jar = RunTime.get().fSxBaseJar.getAbsolutePath();
    }
    URL url = Commons.makeURL(jar, folder);
    pathEntry = new PathEntry(jar, folder, url);
    return pathEntry;
  }

  private static PathEntry createPathEntryClass(String possibleClass, String altPath) {
    URL pathURL = null;
    Class cls = null;
    String klassName;
    String fpSubPath = "";
    String subPath = null;
    int n = possibleClass.indexOf("/");
    if (n > 0) {
      klassName = possibleClass.substring(0, n);
      if ((n + 1) < possibleClass.length()) {
        fpSubPath = possibleClass.substring(n + 1);
      }
    } else {
      klassName = possibleClass;
    }
    try {
      cls = Class.forName(klassName);
    } catch (ClassNotFoundException ex) {
    }
    if (cls != null) {
      CodeSource codeSrc = cls.getProtectionDomain().getCodeSource();
      if (codeSrc != null && codeSrc.getLocation() != null) {
        URL classURL = codeSrc.getLocation();
        pathURL = Commons.makeURL(classURL, fpSubPath);
      } else {
        cls = null;
      }
      if (cls == null) {
        log(lvl, "createPathEntryClass: class not found (%s) from path (%s)", klassName, possibleClass);
        pathURL = Commons.makeURL(altPath, fpSubPath);
        possibleClass = altPath;
        subPath = fpSubPath;
      }
    }
    return new PathEntry(possibleClass, subPath, pathURL);
  }

  private static PathEntry createPathEntryHttp(String netURL, String folder) {
    URL url = Commons.makeURL(netURL, folder);
    return new PathEntry(netURL, folder, url);
  }
  //</editor-fold>

  //<editor-fold desc="03 add/remove path entry --- old">

  /**
   * create a new PathEntry from the given path and add it to the
   * end of the current image path<br>
   *
   * @param mainPath relative or absolute path
   * @return true if successful otherwise false
   */
  public static boolean add(String mainPath) {
    return add(mainPath, null);
  }

  /**
   * create a new PathEntry from the path and add it to the
   * end of the current image path<br>
   * for images stored in jars:<br>
   * Set the primary image path to the top folder level of a jar based on the
   * given class name (must be found on class path). When not running from a jar
   * (e.g. running in some IDE) the path will be the path to the compiled classes <br>
   * For situations, where the images cannot be found automatically in the non-jar situation, you
   * might give an alternative path either absolute or relative to the working folder.
   *
   * @param mainPath        absolute path name or a valid classname optionally followed by /subfolder...
   * @param altPathOrFolder alternative image folder, when not running from jar
   * @return true if successful otherwise false
   */
  public static boolean add(String mainPath, String altPathOrFolder) {
    return null != append(mainPath, altPathOrFolder);
  }

  /**
   * remove entry with given path (same as given with add)
   *
   * @param path relative or absolute path
   * @return true on success, false otherwise
   */
  public static boolean remove(String path) {
    return remove(path, null);
  }

  public static boolean remove(String mainPath, String altPathOrFolder) {
    return null != remove((Object) mainPath, altPathOrFolder);
  }

  /**
   * create a new PathEntry from the given net resource folder accessible via HTTP at
   * end of the current image path<br>
   * BE AWARE:<br>
   * Files stored in the given remote folder must allow HTTP HEAD-requests (checked)<br>
   * redirections are not followed (suppressed)
   *
   * @param pathHTTP folder address like siteaddress or siteaddress/folder/subfolder (e.g. download.sikuli.de/images)
   * @return true if successful otherwise false
   */
  public static boolean addHTTP(String pathHTTP) {
    return null != append(makeNetURL(pathHTTP));
  }

  private static URL makeNetURL(String pathHTTP) {
    String proto = "http://";
    String protos = "https://";
    if (!pathHTTP.startsWith(proto) && !pathHTTP.startsWith(protos)) {
      pathHTTP = proto + pathHTTP;
    }
    return Commons.makeURL(pathHTTP);
  }

  public static boolean removeHTTP(String pathHTTP) {
    return null != remove(makeNetURL(pathHTTP));
  }

  public static boolean addJar(String fpJar) {
    return addJar(fpJar, null);
  }

  public static boolean addJar(String fpJar, String fpImage) {
    if (!fpJar.endsWith(".jar") && !fpJar.contains(".jar!")) {
      fpJar += ".jar";
    }
    return null != append(fpJar, fpImage);
  }

  public static boolean removeJar(String fpJar) {
    return removeJar(fpJar, null);
  }

  public static boolean removeJar(String fpJar, String fpImage) {
    if (!fpJar.endsWith(".jar") && !fpJar.contains(".jar!")) {
      fpJar += ".jar";
    }
    return remove(fpJar, null);
  }

  /**
   * add entry to end of list (the given URL is not checked)
   *
   * @param pURL a valid URL (not checked)
   */
  public static boolean add(URL pURL) {
    return null != append(pURL);
  }
  //</editor-fold>

  //<editor-fold desc="04 add/remove path entry --- new">
  public static URL get(Object what) {
    return get(what, null);
  }

  public static URL get(Object what, String folder) {
    PathEntry whatEntry = getPathEntry(what, folder);
    return null != whatEntry ? whatEntry.pathURL : null;
  }

  public static boolean has(Object what) {
    return has(what, null);
  }

  public static boolean has(Object what, String folder) {
    return hasPathEntry(getPathEntry(what, folder));
  }

  public static URL insert(Object what) {
    return insert(what, null, null);
  }

  public static URL insert(Object what, String folder) {
    return insert(what, folder, null);
  }

  public static URL insert(Object what, URL where) {
    return insert(what, null, where);
  }

  public static URL insert(Object what, String folder, URL where) {
    URL url = null;
    PathEntry whatEntry = getPathEntry(what, folder);
    if (null != whatEntry && whatEntry.isValid()) {
      remove(isPathEntry(whatEntry));
      int pathEntryIndex = getPathEntryIndex(where);
      if (0 == pathEntryIndex) {
        pathEntryIndex = 1;
      }
      imagePaths.add(pathEntryIndex, whatEntry);
      url = whatEntry.pathURL;
    }
    return url;
  }

  public static URL append(Object what) {
    return append(what, null, null);
  }

  public static URL append(Object what, String folder) {
    return append(what, folder, null);
  }

  public static URL append(Object what, URL where) {
    return append(what, null, where);
  }

  public static URL append(Object what, String folder, URL where) {
    URL url = null;
    PathEntry whatEntry = getPathEntry(what, folder);
    if (null != whatEntry && whatEntry.isValid()) {
      remove(isPathEntry(whatEntry));
      int pathEntryIndex = getPathEntryIndex(where) + 1;
      if (1 == pathEntryIndex) {
        pathEntryIndex = imagePaths.size();
      }
      getPaths().add(pathEntryIndex, whatEntry);
      url = whatEntry.pathURL;
    }
    return url;
  }

  public static URL replace(Object what, URL where) {
    return replace(what, null, where);
  }

  public static URL replace(Object what, String folder, URL where) {
    URL url = null;
    PathEntry whatEntry = getPathEntry(what, folder);
    if (null != whatEntry && whatEntry.isValid()) {
      int pathEntryIndex = getPathEntryIndex(where);
      if (0 < pathEntryIndex) {
        Image.purge(getPaths().get(pathEntryIndex));
        getPaths().set(pathEntryIndex, whatEntry);
        url = whatEntry.pathURL;
      }
    }
    return url;
  }

  public static URL remove(Object what) {
    return remove(isPathEntry(getPathEntry(what, null)));
  }

  public static URL remove(Object what, String folder) {
    return remove(isPathEntry(getPathEntry(what, folder)));
  }
  //</editor-fold>

  //<editor-fold desc="05 bundle path">
  public static boolean hasBundlePath() {
    return getBundle() != null;
  }

  /**
   * the given path replaces bundlepath (entry 0)
   *
   * @param newBundlePath an absolute file path
   * @return true on success, false otherwise
   */
  public static boolean setBundlePath(String newBundlePath) {
    if (newBundlePath == null) {
      newBundlePath = Settings.BundlePath;
      if (newBundlePath == null) {
        return false;
      }
    }
    PathEntry entry = getPathEntry(newBundlePath, null);
    if (entry != null && entry.isValid()) {
      remove(isPathEntry(entry));
      setBundle(entry);
      Commons.bundlePathValid(entry);
      return true;
    }
    return false;
  }

  /**
   * no trailing path separator
   *
   * @return the current bundle path
   */
  public static String getBundlePath() {
    if (!hasBundlePath()) {
      if (!setBundlePath()) {
        return null;
      }
    }
    return getBundle().getPath();
  }

  public static File setBundleFolder(File folder) {
    PathEntry entry = getPathEntry(folder, null);
    if (entry != null && entry.isValid()) {
      PathEntry oldBundle = getBundle();
      if (entry.equals(oldBundle)) {
        return folder;
      }
      Image.purge(oldBundle);
      setBundle(entry);
      log(lvl, "new BundlePath: %s", entry);
    }
    return null;
  }

  protected static PathEntry getBundle() {
    return imagePaths.get(0);
  }

  private static boolean isBundle(PathEntry pathEntry) {
    return getBundle() != null && getBundle().equals(pathEntry);
  }

  private static boolean bundleEquals(Object path) {
    if (hasBundlePath()) {
      return getBundle().equals(path);
    }
    return false;
  }

  private static boolean setBundlePath() {
    return setBundlePath(null);
  }

  private static void setBundle(PathEntry pathEntry) {
    imagePaths.set(0, pathEntry);
  }
  //</editor-fold>

  //<editor-fold desc="10 find image">
  public static URL check(String name) {
    return find(Image.getValidImageFilename(name));
  }

  /**
   * try to find the given relative image file name on the image path<br>
   * starting from entry 0, the first found existence is taken<br>
   * absolute file names are checked for existence
   *
   * @param imageFileName relative or absolute filename with extension
   * @return a valid URL or null if not found/exists
   */
  public static URL find(String imageFileName) {
    URL fURL = null;
    String proto = "";
    File imageFile = new File(imageFileName);
    if (imageFile.isAbsolute()) {
      if (imageFile.exists()) {
        fURL = Commons.makeURL(imageFile);
      } else {
        log(-1, "find: File does not exist: %s", imageFileName);
      }
      return fURL;
    } else {
      for (PathEntry entry : getPaths()) {
        if (entry == null || !entry.isValid()) {
          continue;
        }
        proto = entry.pathURL.getProtocol();
        if ("file".equals(proto)) {
          if (new File(entry.getPath(), imageFileName).exists()) {
            return Commons.makeURL(entry.getPath(), imageFileName);
          }
        } else if ("jar".equals(proto) || proto.startsWith("http")) {
          URL url = Commons.makeURL(entry.getPath(), imageFileName);
          if (url != null) {
            int check = -1;
            if (proto.startsWith("http")) {
              check = FileManager.isUrlUseabel(url);
            } else {
              try {
                InputStream inputStream = url.openStream();
                check = inputStream.available();
              } catch (IOException e) {
              }
            }
            if (check > 0) {
              return url;
            }
          }
        }
      }
      log(-1, "find: not there: %s", imageFileName);
      dump(lvl);
      return fURL;
    }
  }

  /**
   * given absolute or relative (searched on image path) file name<br>
   * is tried to open as a BufferedReader<br>
   * BE AWARE: use br.close() when finished
   *
   * @param fname relative or absolute filename
   * @return the BufferedReader to be used or null if not possible
   */
  public static BufferedReader open(String fname) {
    log(lvl, "open: " + fname);
    URL furl = find(fname);
    if (furl != null) {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new InputStreamReader(furl.openStream()));
      } catch (IOException ex) {
        log(-1, "open: %s", ex.getMessage());
        return null;
      }
      try {
        br.mark(10);
        if (br.read() < 0) {
          br.close();
          return null;
        }
        br.reset();
        return br;
      } catch (IOException ex) {
        log(-1, "open: %s", ex.getMessage());
        try {
          br.close();
        } catch (IOException ex1) {
          log(-1, "open: %s", ex1.getMessage());
          return null;
        }
        return null;
      }
    }
    return null;
  }

  public static boolean isImageBundled(URL fURL) {
    if ("file".equals(fURL.getProtocol())) {
      return bundleEquals(new File(fURL.getPath()).getParent());
    }
    return false;
  }
  //</editor-fold>
}
