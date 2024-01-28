/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.Commons;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
    dumpDo(lvl, null);
  }

  public static void dump(String msg) {
    dumpDo(0, msg);
  }

  private static void dumpDo(int lvl, String msg) {
    if (null != msg) {
      log(lvl, "****** %s", msg);
    }
    int i = 0;
    for (PathEntry p : imagePaths) {
      if (i == 0) {
        log(lvl, "BundlePath: %s", (p == null ? "--- not set ---" : p.getPath()));
      } else {
        log(lvl, "Path %d: %s", i, p.getPath());
      }
      i++;
    }
    if (null != msg) {
      log(lvl, "****** ------------ ******");
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
    List<PathEntry> toSave = new ArrayList<>();
    for (PathEntry pathEntry : imagePaths) {
      if (pathEntry == null) {
        continue;
      }
      Image.purge(pathEntry);
      if (pathEntry.isSpecial()) {
        toSave.add(pathEntry);
      }
    }
    PathEntry bundlePath = getBundle();
    imagePaths.clear();
    imagePaths.add(bundlePath);
    if (toSave.size() > 0) {
      imagePaths.addAll(toSave);
    }
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

  private static boolean isBundlePathSupported() {
    return false;
  }

  private static void bundlePathValid(PathEntry entry) {
    if (!isBundlePathSupported() && !entry.isFile()) {
      Debug.error("Not supported as BundlePath: %s", entry.getURL());
    }
  }
  //</editor-fold>

  //<editor-fold desc="02 path entry">

  /**
   * represents an imagepath entry
   */
  public static class PathEntry {

    private URL pathURL = null;
    private String pathURLpath = null;
    private String path = null;
    private Class clazz = null;
    private String clazzSub = null;

    private PathEntry(String main, String sub, URL eqivalentURL) {
      if (main == null) {
        main = "";
      }
      if (sub == null) {
        sub = "";
      }
      path = main + (sub.isEmpty() ? sub : "+" + sub);
      pathURL = eqivalentURL;
      if (pathURL != null) {
        pathURLpath = Commons.urlToFile(pathURL).getAbsolutePath();//pathURL.toExternalForm();
        if (pathURL.getProtocol().equals("jar")) {
          if (!main.contains(".jar")) {
            String[] parts = main.replace("\\", "/").split("/");
            try {
              clazz = Class.forName(parts[0]);
              if (parts.length > 0) {
                clazzSub = main.substring(parts[0].length());
              }
            } catch (ClassNotFoundException e) {
              Commons.terminate(999, "ImagePath::PathEntry(%s): as class not possible", main);
            }
          }
          pathURLpath = Commons.urlToFile(pathURL).getAbsolutePath();
        } else if (pathURL.getProtocol().equals("file")) {
          pathURLpath = Commons.urlToFile(pathURL).getAbsolutePath();
        }
      }
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
            return pathURL.toExternalForm().equals(getPathEntry(other, null).pathURL.toExternalForm()); // equals
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

    public boolean isSpecial() {
      if (path.equals("__FROM-IMPORT__")) {
        return true;
      }
      return false;
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
        return pathURLpath;
      }
      return path;
    }

    public File getFile() {
      if (isValid()) {
        if (pathURL.getProtocol().equals("file")) {
          return new File(getPath());
        }
      }
      return null;
    }

    public File getJar() {
      if (isValid()) {
        if (pathURL.getProtocol().equals("jar")) {
          String path = getPath();
          path = path.split("!")[0];
          return new File(path);
        }
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
    if (path == null) {
      return null;
    }
    // Debug.log(3, "*** ImagePath::getPathEntry: %s", path); //TODO
    PathEntry pathEntry = null;
    String special = null;
    if (null != path) {
      if (folder != null && folder.startsWith("__") && folder.endsWith("__")) {
        special = folder;
        folder = null;
      }
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
      }
    }
    if (pathEntry == null) {
      log(-1, "getPathEntry: invalid path: %s (String, File or URL)", path);
      return null;
    }
    if (special != null) {
      pathEntry.path = special;
    }
    //Debug.log(3, "ImagePath::getPathEntry returns: %s", pathEntry); //TODO
    return pathEntry;
  }

  private static int getPathEntryIndex(URL url) {
    if (url == null) {
      return 0;
    }
    PathEntry whereEntry = getPathEntry(url, null); // getPathEntryIndex
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
    URL pathURL = Commons.makeURL(mainPath, altPathOrFolder); // createPathEntry
    //Debug.log(3, "createPathEntry: mainPath: %s", mainPath); //TODO
    PathEntry pathEntry = new PathEntry(mainPath, altPathOrFolder, pathURL);
    //Debug.log(3, "createPathEntry returns: pathEntry: %s", pathEntry); //TODO
    return pathEntry;
  }

  private static PathEntry createPathEntryJar(String jar, String folder) {
    PathEntry pathEntry;
    if (".".equals(jar)) {
      jar = Commons.getMainClassLocation().getAbsolutePath();
    }
    URL url = Commons.makeURL(jar, folder); // createPathEntryJar
    pathEntry = new PathEntry(jar, folder, url);
    return pathEntry;
  }

  private static PathEntry createPathEntryHttp(String netURL, String folder) {
    URL url = Commons.makeURL(netURL, folder); // createPathEntryHttp
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
    return Commons.makeURL(pathHTTP, ""); // makeNetURL
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
    PathEntry whatEntry = getPathEntry(what, folder); // get
    return null != whatEntry ? whatEntry.pathURL : null;
  }

  public static boolean has(Object what) {
    return has(what, null);
  }

  public static boolean has(Object what, String folder) {
    return hasPathEntry(getPathEntry(what, folder)); // has
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
    PathEntry whatEntry = getPathEntry(what, folder); // insert
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
    PathEntry whatEntry = getPathEntry(what, folder); // append
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

  public static void appendFromImport(Object what) {
    append(what, "__FROM-IMPORT__");
  }

  public static URL replace(Object what, URL where) {
    return replace(what, null, where);
  }

  public static URL replace(Object what, String folder, URL where) {
    URL url = null;
    PathEntry whatEntry = getPathEntry(what, folder); // replace
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
    return remove(isPathEntry(getPathEntry(what, null))); // remove
  }

  public static URL remove(Object what, String folder) {
    return remove(isPathEntry(getPathEntry(what, folder))); // remove
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
        newBundlePath = new File(Commons.getWorkDir(), Settings.WorkdirBundlePath).getAbsolutePath();
      }
    }
    if (new File(newBundlePath).exists()) {
      PathEntry entry = getPathEntry(newBundlePath, null); // setBundlePath
      if (entry != null && entry.isValid()) {
        remove(isPathEntry(entry));
        setBundle(entry);
        bundlePathValid(entry);
        return true;
      }
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
    PathEntry entry = getPathEntry(folder, null); // setBundleFolder
    if (entry != null && entry.isValid()) {
      PathEntry oldBundle = getBundle();
      if (entry.equals(oldBundle)) {
        return folder;
      }
      if (null != oldBundle) {
        Image.purge(oldBundle);
      }
      setBundle(entry);
      log(lvl, "new BundlePath: %s", entry);
      return folder;
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
  public static String check(Object toCheck) {
    if (toCheck instanceof String) {
      String validImageFilename = Commons.getValidImageFilename((String) toCheck);
      URL url = find(validImageFilename, true);
      if (url != null) {
        validImageFilename = url.toExternalForm();
      }
      return validImageFilename;
    }
    return Image.from(toCheck).getFilename();
  }

  /**
   * try to find the given relative image file name on the image path<br>
   * starting from entry 0, the first found existence is taken<br>
   * absolute file names are checked for existence
   *
   * @param imageName relative or absolute filename with extension
   * @return a valid URL or null if not found/exists
   */
  public static URL find(String imageName) {
    return find(imageName, false);
  }

  private static URL find(String imageName, boolean silent) {
    String proto = "";
    String imageFileName = Commons.getValidImageFilename(imageName);
    if (imageName.endsWith("#")) {
      imageFileName = imageName.substring(0, imageName.length() - 1);
    }
    File imageFile = new File(imageFileName);
    if (imageFile.isAbsolute()) {
      if (imageFile.exists()) {
        return Commons.makeURL(imageFile, ""); // find absolute
      } else {
        if (!silent) {
          log(-1, "find: File does not exist: %s", imageName);
        }
        return null;
      }
    }
    URL url = null;
    for (PathEntry entry : getPaths()) {
      if (url != null) {
        break;
      }
      if (entry == null || !entry.isValid()) {
        continue;
      }
      proto = entry.pathURL.getProtocol();
      if (entry.clazz != null) {
        url = Commons.makeURL(entry.clazz.getResource(entry.clazzSub), imageFileName); // find with class
        proto = "jar";
      }
      if ("jar".equals(proto) || proto.startsWith("http")) {
        if (url == null) {
          url = Commons.makeURL(entry.getPath(), imageFileName); // find
        }
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
          if (check < 1) {
            url = null;
          }
        }
      } else if ("file".equals(proto)) {
        if (new File(entry.getPath(), imageFileName).exists()) {
          url = Commons.makeURL(entry.getPath(), imageFileName); // find
        }
      } else {
        imageFile = new File(Commons.getWorkDir(), imageFileName);
        if (imageFile.exists()) {
          url = Commons.makeURL(imageFile, ""); // find workdir
        } else {
          imageFile = new File(new File(Commons.getWorkDir(), Settings.WorkdirBundlePath), imageName);
          if (imageFile.exists()) {
            url = Commons.makeURL(imageFile, ""); // find workdir Settings.WorkdirBundlePath
          }
        }
      }
    }
    if (url == null) {
      if (!silent) {
        log(lvl, "find: not in ImagePath: %s", imageName);
        dump(lvl);
      }
    }
    return url;
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
