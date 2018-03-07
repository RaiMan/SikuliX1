/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;

/**
 * runtain the path list of locations, where images will be searched.
 * <br>the first entry always is the bundlepath used on the scripting level<br>
 * Python import automatically adds a sikuli bundle here<br>
 * supported locations:<br>
 * - absolute filesystem paths<br>
 * - inside jars relative to root level given by a class found on classpath<br>
 * - a location in the web given as string starting with http[s]://<br>
 * - any location as a valid URL, from where image files can be loaded<br>
 */
public class ImagePath {

  static RunTime runTime = RunTime.get();

  private static final String me = "ImagePath: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  /**
   * represents an imagepath entry
   */
  public static class PathEntry {

    public URL pathURL;
    public String path;

    /**
     * create a new image path entry
     *
     * @param givenName the given path relative or absolute
     * @param eqivalentURL the evaluated URL
     */
    public PathEntry(String givenName, URL eqivalentURL) {
      path = FileManager.normalize(givenName);
			if (eqivalentURL != null) {
				pathURL = eqivalentURL;
			} else {
				pathURL = makePathURL(path, null).pathURL;
			}
			log(lvl+1, "PathEntry: %s \nas %s", path, pathURL);
    }

		public String getPath() {
			if (pathURL == null) {
				return "-- empty --";
			}
      String uPath = pathURL.toExternalForm();
			if (isFile() && uPath.startsWith("file:")) {
        uPath = uPath.substring(5);
      }
			return uPath;
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

		public boolean exists() {
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
			if (! (other instanceof PathEntry)) {
				if (other instanceof URL) {
					if (pathURL.equals((URL) other)) {
						return true;
					}
				} else if (other instanceof String) {
          if (isFile()) {
            return FileManager.pathEquals(pathURL.getPath(), (String) other);
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

  private static final List<PathEntry> imagePaths = Collections.synchronizedList(new ArrayList<PathEntry>());
  private static PathEntry bundlePath = null;

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

  public static String getPath(int ix) {
    PathEntry pe = imagePaths.get(0);
    String path = null;
    if (pe != null) {
      path = pe.getPath();
    }
    return path;
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

  private static boolean bundleEquals(Object path) {
    if (bundlePath != null) {
      return bundlePath.equals(path);
    }
    return false;
  }

  public static boolean isImageBundled(URL fURL) {
		if ("file".equals(fURL.getProtocol())) {
  		return bundleEquals(new File(fURL.getPath()).getParent());
		}
		return false;
	}

  /**
   * try to find the given relative image file name on the image path<br>
   * starting from entry 0, the first found existence is taken<br>
   * absolute file names are checked for existence
   *
   * @param fname relative or absolute filename
   * @return a valid URL or null if not found/exists
   */
  public static URL find(String fname) {
    URL fURL = null;
		String proto = "";
    fname = FileManager.normalize(fname);
    if (new File(fname).isAbsolute()) {
      if (new File(fname).exists()) {
        fURL = FileManager.makeURL(fname);
      } else {
        log(-1, "find: File does not exist: " + fname);
      }
			return fURL;
    } else {
      if (bundlePath == null) {
        setBundlePath(null);
      }
      for (PathEntry path : getPaths()) {
        if (path == null) {
          continue;
        }
				proto = path.pathURL.getProtocol();
				if ("file".equals(proto)) {
          fURL = FileManager.makeURL(path.pathURL, fname);
          if (new File(fURL.getPath()).exists()) {
            break;
          }
        } else if ("jar".equals(proto) || proto.startsWith("http")) {
          fURL = FileManager.getURLForContentFromURL(path.pathURL, fname);
          if (fURL != null) {
            break;
          }
        } else {
          log(-1, "find: URL not supported: " + path.pathURL);
					return fURL;
        }
      }
      if (fURL == null) {
        log(-1, "find: not on image path: " + fname);
        dump(lvl);
      }
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
			log (-1, "addHTTP: not possible: %s\n%s", pathHTTP, ex);
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
      log (-1, "removeHTTP: not possible: %s\n%s", pathHTTP, ex);
      return false;
		}
  }

  /**
   * create a new PathEntry from the given absolute path name and add it to the
   * end of the current image path<br>
   * for images stored in jars:<br>
   * Set the primary image path to the top folder level of a jar based on the
   * given class name (must be found on class path). When not running from a jar
 (e.g. running in some IDE) the path will be the path to the compiled
 classes (for Maven based projects this is target/classes that contains all
 stuff copied from src/run/resources automatically)<br>
   * For situations, where the images cannot be found automatically in the non-jar situation, you
   * might give an alternative path either absolute or relative to the working folder.
   * @param mainPath absolute path name or a valid classname optionally followed by /subfolder...
   * @param altPath alternative image folder, when not running from jar
   * @return true if successful otherwise false
   */
  public static boolean add(String mainPath, String altPath) {
    PathEntry path = null;
    File fPath = new File(mainPath);
    if (!fPath.isAbsolute() && mainPath.contains(":")) {
      return addHTTP(mainPath);
    }
    path = makePathURL(mainPath, altPath);
    if (path != null) {
      if (hasPath(path) < 0) {
        log(lvl, "add: %s", path);
        imagePaths.add(path);
      } else {
        log(lvl, "duplicate not added: %s", path);
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
      fpJar = runTime.fSxBaseJar.getAbsolutePath();
      if (!fpJar.endsWith(".jar")) {
        return false;
      }
    }
    if (new File(fpJar).exists()) {
      if (fpImage == null) {
        fpImage = "";
      }
      pathURL = FileManager.makeURL(fpJar + "!/" + fpImage, "jar");
      add(pathURL);
    }
    return true;
  }

  private static int hasPath(PathEntry path) {
    PathEntry pe = imagePaths.get(0);
    if (imagePaths.size() == 1 && pe == null) {
      return -1;
    }
    if (pe != null && pe.equals(path)) {
      return 0;
    }
    for (PathEntry p : imagePaths.subList(1, imagePaths.size())) {
      if (p != null && p.equals(path)) {
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
    if (!fPath.isAbsolute() && path.contains(":")) {
      return removeHTTP(path);
    }
    return remove(makePathURL(FileManager.normalize(path), null).pathURL);
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
      Image.purge(pURL);
      bundlePath = null;
      Settings.BundlePath = null;
      imagePaths.set(0, null);
    }
    Iterator<PathEntry> it = imagePaths.subList(1, imagePaths.size()).iterator();
    PathEntry p, p0;
    p0 = imagePaths.get(0);
    while (it.hasNext()) {
      p = it.next();
      if (!p.equals(pURL)) {
        continue;
      }
      it.remove();
      Image.purge(p.pathURL);
    }
    return true;
  }

  /**
   * empty path list and add given path as first entry
	 * Image cache is cleared completely
   *
   * @param path absolute path
   * @return true on success, false otherwise
   */
  public static boolean reset(String path) {
    if (bundleEquals(path)) {
      return true;
    }
    reset();
    return setBundlePath(path);
  }

  /**
   * empty path list and keep bundlePath (entry 0)<br>
	 * Image cache is cleared completely
   * convenience for the scripting level
   * @return true
   */
  public static boolean reset() {
    log(lvl, "reset");
    if (imagePaths.isEmpty()) {
			return false;
		}
    for (PathEntry p : imagePaths) {
      if (p == null) {
        continue;
      }
      Image.purge(p.pathURL);
    }
    PathEntry bp = imagePaths.get(0);
    imagePaths.clear();
		imagePaths.add(bp);
    return true;
  }

  /**
   * the given path replaces bundlepath (entry 0)
	 * and Settings.bundlePath is set to given path
   *
   * @param bPath an absolute file path
   * @return true on success, false otherwise
   */
  public static boolean setBundlePath(String bPath) {
		PathEntry path = null;
		if (bPath == null) {
			// called on first find, if bundlepath still null
			path = makePathURL(FileManager.normalizeAbsolute(Settings.BundlePath, false), null);
		} else {
			path = makePathURL(FileManager.normalizeAbsolute(bPath, false), null);
		}
		if (path != null && path.isFile()) {
      if (bundleEquals(path)) {
				return true;
			}
      Image.purge(bundlePath);
			if (path.exists()) {
				imagePaths.set(0, path);
				Settings.BundlePath = path.getPath();
				bundlePath = path;
				log(lvl, "new BundlePath:\n%s", path);
				return true;
			}
		}
    if (getCount() ==0) {
      String wf = System.getProperty("user.dir");
      log(-1, "setBundlePath: invalid BundlePath: %s \nusing working folder: %s",
              bPath, wf);
      if (!new File(wf).exists()) {
        log(-1, "setBundlePath: Fatal error: working folder does not exist --- terminating");
        System.exit(1);
      }
      return setBundlePath(wf);
    }
    return true;
  }

  /**
   * no trailing path separator
   * @return the current bundle path (might be the fallback working folder)
   */
  public static String getBundlePath() {
    if (bundlePath == null) {
      setBundlePath(null);
    }
    return new File(FileManager.slashify(bundlePath.getPath(), false)).getAbsolutePath();
  }

  /**
   * no trailing path separator
   * @return the current bundle path (might be the fallback working folder)
   */
  public static String getBundlePathSet() {
    if (bundlePath == null) {
      return null;
    }
    return new File(FileManager.slashify(bundlePath.getPath(), false)).getAbsolutePath();
  }

  /**
   * no trailing path separator
   * @return the current bundle path (might be the fallback working folder)
   */
  public static String getBundleFolder() {
    if (bundlePath == null) {
      setBundlePath(null);
    }
    return new File(FileManager.slashify(bundlePath.getPath(), true)).getAbsolutePath();
  }

  private static PathEntry makePathURL(String fpMainPath, String fpAltPath) {
		if (fpMainPath == null || fpMainPath.isEmpty()) {
			return null;
		}
		URL pathURL = null;
    File fPath = new File(FileManager.normalizeAbsolute(fpMainPath, false));
		if (fPath.exists()) {
			pathURL = FileManager.makeURL(fPath.getAbsolutePath());
		} else {
      if (fpMainPath.contains("\\")) {
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
        log(-1,"add: class %s not found on classpath.", klassName);
			}
			if (cls != null) {
				CodeSource codeSrc = cls.getProtectionDomain().getCodeSource();
				if (codeSrc != null && codeSrc.getLocation() != null) {
					URL jarURL = codeSrc.getLocation();
					if (runTime.runningWinApp || jarURL.getPath().endsWith(".jar")) {
						pathURL = FileManager.makeURL(jarURL.toString() + "!/" + fpSubPath, "jar");
					} else {
						if (fpAltPath == null || fpAltPath.isEmpty()) {
							fpAltPath = jarURL.getPath();
						}
						if (new File(FileManager.normalizeAbsolute(fpAltPath, false), fpSubPath).exists()) {
              File fAltPath = new File(FileManager.normalizeAbsolute(fpAltPath, false), fpSubPath);
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
}
