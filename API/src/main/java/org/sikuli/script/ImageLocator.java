/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;

/**
 * This class was used to locate image files in the filesystem <br>
 * and in the internet (the files are cached locally) <br>
 *
 * @deprecated will be completely replaced by the classes Image and ImagePath
 * relevant functions are already redirected as needed
 */
@Deprecated
public class ImageLocator {

  static RunTime runTime = RunTime.get();

  static ArrayList<String> pathList = new ArrayList<String>();
  static int firstEntries = 1;
  static File _cache_dir_global = new File(RunTime.get().fpBaseTempPath, "sikuli_cache/SIKULI_GLOBAL/");
  static Map<URI, String> _cache = new HashMap<URI, String>();

  static {
    pathList.add("");
    resetImagePath("");
    if (pathList.size() >= 1 && "".equals(pathList.get(0))) {
      pathList.set(0, System.getProperty("user.dir"));
    }
    if (!_cache_dir_global.exists()) {
      try {
        _cache_dir_global.mkdir();
      } catch (Exception e) {
        Debug.error("ImageLocator: Local cache dir not possible: " + _cache_dir_global);
        _cache_dir_global = null;
      }
    }
  }

  /**
   * forwarded to ImagePath.getImagePath()
   *
   * @return an array of the imagepaths as added
   * @deprecated
   */
  @Deprecated
  public static String[] getImagePath() {
    return ImagePath.get();
  }

  /**
   * forwarded to ImagePath.add()
   *
   * @return null if not successful
   * @deprecated
   */
  @Deprecated
  public static String addImagePath(String path) {
    if (!ImagePath.add(path)) {
      return null;
    } else {
      return path;
    }
  }

  /**
   * forwarded to ImagePath.remove()
   *
   * @deprecated
   */
  @Deprecated
  public static void removeImagePath(String path) {
    ImagePath.remove(path);
  }

  /**
   * forwarded to ImagePath.setBundlePath()
   *
   * @deprecated
   */
  @Deprecated
  public static void setBundlePath(String bundlePath) {
    ImagePath.setBundlePath(bundlePath);
  }

  /**
   * forwarded to ImagePath.getBundlePath()
   *
   * @return the current bundlepath
   * @deprecated
   */
  @Deprecated
  public static String getBundlePath() {
    return ImagePath.getBundlePath();
  }

  public static String locate(String filename) throws IOException {
    if (filename != null) {
      String ret;
      URL url = getURL(filename);
      if (url != null) {
        ret = getFileFromURL(url);
        if (ret != null) {
          return ret;
        }
      }
      File f = new File(filename);
      if (f.isAbsolute()) {
        if (f.exists()) {
          return f.getAbsolutePath();
        }
      } else {
        ret = searchFile(filename);
        if (ret != null) {
          return ret;
        }
      }
    } else {
      filename = "*** not known ***";
    }
    throw new FileNotFoundException("ImageLocator.locate: " + filename + " does not exist or cannot be found on ImagePath");
  }

  /**
   * forwarded to Image.create(filename).get()
   *
   * @return a BufferedImage from the given filename or null
   * @deprecated
   */
  @Deprecated
  public static BufferedImage getImage(String filename) {
    return Image.create(filename).get();
  }

  /***************************
  * methods below are obsolete
  ****************************/
  private static String[] splitImagePath(String path) {
    if (path == null || "".equals(path)) {
      return new String[0];
    }
    path = path.replaceAll("[Hh][Tt][Tt][Pp]://", "__http__//");
    path = path.replaceAll("[Hh][Tt][Tt][Pp][Ss]://", "__https__//");
    String[] pl = path.split(Settings.getPathSeparator());
    File pathName;
    for (int i = 0; i < pl.length; i++) {
      boolean isURL = false;
      path = pl[i];
      if (path.indexOf("__http__") >= 0) {
        path = path.replaceAll("__http__//", "http://");
        isURL = true;
      } else if (path.indexOf("__https__") >= 0) {
        path = path.replaceAll("__https__//", "https://");
        isURL = true;
      }
      if (isURL) {
        if ((path = getURL(path).getPath()) != null) {
          if (!path.endsWith("/")) {
            pl[i] = path + "/";
          }
        } else {
          pl[i] = null;
        }
      } else {
        pathName = new File(path);
        if (pathName.exists()) {
          pl[i] = FileManager.slashify(pathName.getAbsolutePath(), true);
        } else {
          pathList.remove(pl[i]);
          pl[i] = null;
        }
      }
    }
    return pl;
  }

  private static URL getURL(String s) {
    try {
      URL url = new URL(s);
      return url;
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private static String addImagePath(String[] pl, boolean first) {
    int addedAt = firstEntries;
    if (addedAt == pathList.size()) {
      first = false;
    }
    String epl;
    File fepl;
    for (int i = 0; i < pl.length; i++) {
      if (pl[i] == null) {
        continue;
      }
      epl = pl[i];
      //fepl = new File(epl);
//TODO handle relative paths
      if (!pathList.contains(epl)) {
        if (!first) {
          pathList.add(epl);
        } else {
          pathList.add(addedAt, epl);
          addedAt++;
        }
      }
    }
    if (pl.length > 0) {
      return pl[0];
    } else {
      return null;
    }
  }

  private static String addImagePath(String path, boolean first) {
    String pl[] = splitImagePath(path);
    removeImagePath(pl);
    return addImagePath(pl, first);
  }

  private static String addImagePathFirst(String path) {
    return addImagePath(path, true);
  }

  private static String addImagePath(String[] pl) {
    return addImagePath(pl, false);
  }

  private static String addImagePathFirst(String[] pl) {
    return addImagePath(pl, true);
  }

  private static void removeImagePath(String[] pl) {
    for (int i = 0; i < pl.length; i++) {
      if (pl[i] != null) {
        pathList.remove(pl[i]);
      }
    }
  }

  private static void clearImagePath() {
    Iterator<String> ip = pathList.listIterator(1);
    String p;
    while (ip.hasNext()) {
      p = ip.next();
      if (!p.substring(0, p.length() - 1).endsWith(".sikuli")) {
        ip.remove();
      }
    }
    if (firstEntries == pathList.size()) {
      addImagePath(System.getenv("SIKULI_IMAGE_PATH"));
      addImagePath(System.getProperty("SIKULI_IMAGE_PATH"));
    } else {
      addImagePathFirst(System.getProperty("SIKULI_IMAGE_PATH"));
      addImagePathFirst(System.getenv("SIKULI_IMAGE_PATH"));
    }
  }

  private static void resetImagePath(String path) {
    clearImagePath();
    String pl[] = splitImagePath(path);
    if (pl.length > 0) {
      pathList.set(0, pl[0]);
      Settings.BundlePath = pl[0].substring(0, pl[0].length() - 1);
      pl[0] = null;
      addImagePath(pl);
    }
  }

  private static void resetImagePath(String[] pl) {
    clearImagePath();
    addImagePath(pl);
  }

  private static String searchFile(String filename) {
    File f;
    String ret;
    for (Iterator<String> it = pathList.iterator(); it.hasNext();) {
      String path = it.next();
      URL url = getURL(path);
      if (url != null) {
        try {
          ret = getFileFromURL(new URL(url, filename));
          if (ret != null) {
            return ret;
          }
        } catch (MalformedURLException ex) {
        }
      }
      f = new File(path, filename);
      if (f.exists()) {
        Debug.log(3, "ImageLocator: found " + filename + " in " + path);
        return f.getAbsolutePath();
      }
    }
    return null;
  }

  private static String getFileFromURL(URL url) {
    if (_cache_dir_global == null) {
      Debug.error("ImageLocator.getFileFromURL: Local cache dir not available - cannot download from url" + url);
      return null;
    }
    try {
      URI uri = url.toURI();
      if (_cache.containsKey(uri)) {
        Debug.log(2, "ImageLocator.getFileFromURL: " + uri + " taken from cache");
        return _cache.get(uri);
      }
      String localFile = FileManager.downloadURL(url, _cache_dir_global.getPath());
      if (localFile != null) {
        Debug.log(2, "ImageLocator.getFileFromURL: download " + uri + " to local: " + localFile);
        _cache.put(uri, localFile);
      }
      return localFile;
    } catch (java.net.URISyntaxException e) {
      Debug.log(2, "ImageLocator.getFileFromURL: URI syntax error: " + url + ", " + e.getMessage());
      return null;
    }
  }
}
