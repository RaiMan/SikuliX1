/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.RunTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

  public static final String SCREENSHOT_DIRECTORY = ".screenshots";

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

  /**
   * the path list as string array
   *
   * @return an array of the file path's currently in the path list
   */
  public static String[] get() {
    int i = 0;
    for (PathEntry p : imagePaths) {
      if (p == null) {
        continue;
      }
      i++;
    }
    String[] paths = new String[i];
    i = 0;
    for (PathEntry p : imagePaths) {
      if (p == null) {
        continue;
      }
      paths[i++] = p.getPath();
      if (p.isFile()) {
        paths[i - 1] = new File(p.getPath()).getAbsolutePath();
      }
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
    String bundle = "(taken as bundle path)";
    for (PathEntry p : imagePaths) {
      if (p == null) {
        log(lvl, "Path: NULL %s", bundle);
      } else {
        log(lvl, "Path: given: %s\nis: %s", p.path, p.getPath());
      }
      bundle = "";
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
      imageCachePurge();
    }
    PathEntry bundlePath = getBundle();
    imagePaths.clear();
    imagePaths.add(bundlePath);
  }

  //TODO image cache purge ???
  private static void imageCachePurge() {
  }

  //</editor-fold>

  //<editor-fold desc="02 init path entry">

  /**
   * represents an imagepath entry
   */
  public static class PathEntry {

    public URL pathURL = null;
    public String path = null;

    /**
     * create a new image path entry
     *
     * @param givenName    the given path relative or absolute
     * @param eqivalentURL the evaluated URL
     */
    public PathEntry(String givenName, URL eqivalentURL) {
      path = FileManager.normalize(givenName);
      if (eqivalentURL != null) {
        pathURL = eqivalentURL;
      } else {
        pathURL = makePathURL(path, null).pathURL;
      }
      log(lvl + 1, "ImagePathEntry: %s (%s)", path, pathURL);
    }

    public PathEntry(File folder) {
      try {
        path = folder.getPath();
        pathURL = new URL("file", null, path);
        log(lvl + 1, "ImagePathEntry: %s (%s)", path, pathURL);
      } catch (IOException e) {
        log(-1, "ImagePathEntry: %s", folder);
      }
    }

    public boolean isValid() {
      return path != null && pathURL != null;
    }

    public String getPath() {
      if (pathURL == null) {
        return "-- empty --";
      }
      if (isFile()) {
        return pathURL.getPath();
      }
      return null;
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

    public File getFile() {
      if (isFile()) {
        return new File(getPath());
      }
      return null;
    }

    public boolean existsFile() {
      if (pathURL == null) {
        return false;
      }
      return new File(getPath()).exists();
    }

    @Override
    public boolean equals(Object other) {
      if (pathURL == null) {
        return false;
      }
      if (!(other instanceof PathEntry)) {
        if (other instanceof URL) {
          if (pathURL.equals(other)) {
            return true;
          }
        } else if (other instanceof String) {
          if (isFile()) {
            try {
              return new File(pathURL.getFile()).equals(new File((String) other).getCanonicalFile());
            } catch (IOException e) {
              return false;
            }
          }
          return false;
        } else if (other instanceof File) {
          if (isFile()) {
            try {
              return new File(pathURL.getFile()).equals(((File) other).getCanonicalFile());
            } catch (IOException e) {
              return false;
            }
          }
          return false;
        }
        return false;
      }
      if (pathURL.equals(((PathEntry) other).pathURL)) {
        return true;
      }
      return false;
    }

    @Override
    public String toString() {
      return getPath();
    }
  }

  private static PathEntry makePathURL(String fpMainPath, String fpAltPath) {
    if (fpMainPath == null || fpMainPath.isEmpty()) {
      return null;
    }
    URL pathURL = null;
    File fPath = new File(FileManager.normalizeAbsolute(fpMainPath));
    if (fPath.exists()) {
      pathURL = FileManager.makeURL(fPath.getAbsolutePath());
    } else {
      if (fpMainPath.contains("\\")) {
        log(-1, "add: folder does not exist (%s)", fPath);
        return null;
      }
      Class cls = null;
      String klassName;
      String fpSubPath = "";
      int n = fpMainPath.indexOf("/");
      if (n > 0) {
        klassName = fpMainPath.substring(0, n);
        if (n < fpMainPath.length() - 2) {
          fpSubPath = fpMainPath.substring(n + 1);
        }
      } else {
        klassName = fpMainPath;
      }
      try {
        cls = Class.forName(klassName);
      } catch (ClassNotFoundException ex) {
        log(-1, "add: class not found (%s) or folder does not exist (%s)", klassName, fPath);
      }
      if (cls != null) {
        CodeSource codeSrc = cls.getProtectionDomain().getCodeSource();
        if (codeSrc != null && codeSrc.getLocation() != null) {
          URL jarURL = codeSrc.getLocation();
          if (jarURL.getPath().endsWith(".jar")) {
            pathURL = FileManager.makeURL(jarURL.toString() + "!/" + fpSubPath, "jar");
          } else {
            if (fpAltPath == null || fpAltPath.isEmpty()) {
              fpAltPath = jarURL.getPath();
            }
            if (new File(FileManager.normalizeAbsolute(fpAltPath), fpSubPath).exists()) {
              File fAltPath = new File(FileManager.normalizeAbsolute(fpAltPath), fpSubPath);
              pathURL = FileManager.makeURL(fAltPath.getPath());
            }
          }
        }
      }
    }
    if (pathURL != null) {
      return new PathEntry(fpMainPath, pathURL);
    }
    return null;
  }
  //</editor-fold>

  //<editor-fold desc="03 handle path entry">

  /**
   * create a new PathEntry from the given absolute path name and add it to the
   * end of the current image path<br>
   * for usage with jars see; {@link #add(String, String)}
   *
   * @param mainPath relative or absolute path
   * @return true if successful otherwise false
   */
  public static boolean add(String mainPath) {
    return add(mainPath, null);
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
    try {
      String proto = "http://";
      String protos = "https://";
      if (pathHTTP.startsWith(proto) || pathHTTP.startsWith(protos)) {
        proto = "";
      }
      pathHTTP = FileManager.slashify(pathHTTP, false);
      URL aURL = new URL(proto + pathHTTP);
      if (0 != FileManager.isUrlUseabel(new URL(aURL.toString() +
          "/THIS_FILE_SHOULD_RETURN_404"))) {
        return false;
      }
      PathEntry path = new PathEntry(pathHTTP, aURL);
      if (hasPath(path) < 0) {
        log(lvl, "add: %s", path);
        imagePaths.add(path);
      } else {
        log(lvl, "duplicate not added: %s", path);
      }
    } catch (Exception ex) {
      log(-1, "addHTTP: not possible: %s\n%s", pathHTTP, ex);
      return false;
    }
    return true;
  }

  public static boolean removeHTTP(String pathHTTP) {
    try {
      String proto = "http://";
      String protos = "https://";
      if (pathHTTP.startsWith(proto) || pathHTTP.startsWith(protos)) {
        proto = "";
      }
      pathHTTP = FileManager.slashify(pathHTTP, false);
      return remove(new URL(proto + pathHTTP));
    } catch (Exception ex) {
      log(-1, "removeHTTP: not possible: %s\n%s", pathHTTP, ex);
      return false;
    }
  }

  /**
   * create a new PathEntry from the given absolute path name and add it to the
   * end of the current image path<br>
   * for images stored in jars:<br>
   * Set the primary image path to the top folder level of a jar based on the
   * given class name (must be found on class path). When not running from a jar
   * (e.g. running in some IDE) the path will be the path to the compiled
   * classes (for Maven based projects this is target/classes that contains all
   * stuff copied from src/run/resources automatically)<br>
   * For situations, where the images cannot be found automatically in the non-jar situation, you
   * might give an alternative path either absolute or relative to the working folder.
   *
   * @param mainPath absolute path name or a valid classname optionally followed by /subfolder...
   * @param altPath  alternative image folder, when not running from jar
   * @return true if successful otherwise false
   */
  public static boolean add(String mainPath, String altPath) {
    PathEntry pathEntry = null;
    File fPath = new File(mainPath);
    if (!fPath.isAbsolute() && mainPath.contains(":")) {
      if (fPath.getAbsolutePath().charAt(2) != ":".charAt(0)) {
        return addHTTP(mainPath);
      }
    }
    pathEntry = makePathURL(mainPath, altPath);
    if (pathEntry != null) {
      if (hasPath(pathEntry) < 0) {
        log(lvl, "add: %s", pathEntry);
        imagePaths.add(pathEntry);
      } else {
        log(lvl, "duplicate not added: %s", pathEntry);
      }
      return true;
    } else {
      log(-1, "add: not valid: %s %s", mainPath,
          (altPath == null ? "" : " / " + altPath));
    }
    return false;
  }

  public static boolean addJar(String fpJar, String fpImage) {
    URL pathURL = null;
    if (".".equals(fpJar)) {
      fpJar = RunTime.get().fSxBaseJar.getAbsolutePath();
      if (!fpJar.endsWith(".jar")) {
        return false;
      }
    }
    if (new File(fpJar).exists()) {
      if (fpImage == null) {
        fpImage = "";
      }
      log(3, "addJar: %s", fpJar);
      pathURL = FileManager.makeURL(fpJar + "!/" + fpImage, "jar");
      add(pathURL);
    }
    return true;
  }

  private static int hasPath(PathEntry path) {
    PathEntry bundle = getBundle();
    if (imagePaths.size() == 1 && bundle == null) {
      return -1;
    }
    if (bundle != null && bundle.equals(path)) {
      return 0;
    }
    for (PathEntry pathEntry : imagePaths.subList(1, imagePaths.size())) {
      if (pathEntry != null && pathEntry.equals(path)) {
        return 1;
      }
    }
    return -1;
  }

  /**
   * add entry to end of list (the given URL is not checked)
   *
   * @param pURL a valid URL (not checked)
   */
  public static void add(URL pURL) {
    imagePaths.add(new PathEntry("__PATH_URL__", pURL));
  }

  /**
   * remove entry with given path (same as given with add)
   *
   * @param path relative or absolute path
   * @return true on success, false otherwise
   */
  public static boolean remove(String path) {
    File fPath = new File(path);
    if (!fPath.isAbsolute() && (path.contains("http://") || path.contains("https://"))) {
      return removeHTTP(path);
    }
    return remove(makePathURL(FileManager.normalize(path), null).pathURL);
  }

  /**
   * remove entry with given path (same as given with add)
   *
   * @param directory relative or absolute path as File
   * @return true on success, false otherwise
   */
  public static boolean remove(File directory) {
    return remove(makePathURL(directory.getAbsolutePath(), null).pathURL);
  }

  /**
   * remove entry with given URL<br>
   * bundlepath (entry 0) cannot be removed
   * loaded images are removed from cache
   *
   * @param pURL a valid URL (not checked)
   * @return true on success, false ozherwise
   */
  private static boolean remove(URL pURL) {
    if (bundleEquals(pURL)) {
      imageCachePurge();
      return true;
    }
    Iterator<PathEntry> it = imagePaths.subList(1, imagePaths.size()).iterator();
    PathEntry pathEntry;
    while (it.hasNext()) {
      pathEntry = it.next();
      if (!pathEntry.equals(pURL)) {
        continue;
      }
      it.remove();
      imageCachePurge();
    }
    return true;
  }
  //</editor-fold>

  //<editor-fold desc="05 bundle path">
  public static boolean hasBundlePath() {
    return getBundle() != null;
  }

  private static boolean bundleEquals(Object path) {
    if (hasBundlePath()) {
      return getBundle().equals(path);
    }
    return false;
  }

  /**
   * empty path list and add given path as first entry
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

  private static boolean setBundlePath() {
    return setBundlePath(null);
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
    File newBundleFile = new File(newBundlePath);
    if (!newBundleFile.isAbsolute()) {
      if (hasBundlePath()) {
        return false;
      }
      newBundleFile = new File(getBundlePath(), newBundlePath);
    }
    return null != setBundleFolder(newBundleFile);
  }

  public static File setBundleFolder(File folder) {
    try {
      folder = folder.getCanonicalFile();
    } catch (IOException e) {
      log(-1, "canonical file problem: %s", folder);
      return null;
    }
    if (null == folder || bundleEquals(folder)) {
      return folder;
    }
    if (folder.exists()) {
      PathEntry oldBundle = getBundle();
      imageCachePurge();
      PathEntry pathEntry = new PathEntry(folder);
      if (pathEntry.isValid()) {
        setBundle(pathEntry);
        log(lvl, "new BundlePath: %s", pathEntry);
        return pathEntry.getFile();
      }
    }
    return null;
  }

  static PathEntry getBundle() {
    return imagePaths.get(0);
  }

  private static void setBundle(PathEntry pathEntry) {
    imagePaths.set(0, pathEntry);
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

  //</editor-fold>

  //<editor-fold desc="10 find image">

  /**
   * try to find the given relative image file name on the image path<br>
   * starting from entry 0, the first found existence is taken<br>
   * absolute file names are checked for existence
   *
   * @param imageFile relative or absolute file
   * @return a valid URL or null if not found/exists
   */
  public static URL find(File imageFile) {
    URL fURL = null;
    String proto = "";
    String imageFileName = imageFile.getPath();
    if (imageFile.isAbsolute() || imageFileName.startsWith("\\")) {
      fURL = Element.createURL(imageFile);
    } else {
      for (PathEntry path : getPaths()) {
        if (path == null) {
          continue;
        }
        proto = path.pathURL.getProtocol();
        if ("file".equals(proto)) {
          imageFile = new File(path.pathURL.getPath(), imageFileName);
          if (imageFile.exists()) {
            fURL = Element.createURL(imageFile);
            break;
          }
        } else if ("jar".equals(proto) || proto.startsWith("http")) { //TODO imagepath jar and net
          fURL = FileManager.getURLForContentFromURL(path.pathURL, imageFileName);
        }
        if (fURL != null) {
          break;
        }
      }
      if (fURL == null) {
        log(-1, "find: %s file not found", imageFileName);
        dump(lvl);
      }
    }
    return fURL;
  }

  public static URL find(String imageFileName) {
    return find(new File(imageFileName));
  }

  private static URL normalize(URL url) {
    String path = url.getPath();
    if ("file".equals(url.getProtocol())) {
      if (path.contains("%")) {
        path = path.replace("%20", " ");
        if (path.contains("%")) {
        }
      }
      try {
        new File(path).getCanonicalFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
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
