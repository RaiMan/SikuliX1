/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.RunTime;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ExtensionManager {
	private static ExtensionManager _instance = null;
	private ArrayList<Extension> extensions;

  private ExtensionManager() {
    extensions = new ArrayList<Extension>();
    Extension e;
    String p, n, v;
    File dir = new File(Settings.getUserExtPath());
    for (File d : dir.listFiles()) {
      if (d.getAbsolutePath().endsWith(".jar")) {
        p = d.getAbsolutePath();
        n = d.getName();
        n = n.substring(0, n.length()-4);
        if (n.contains("-")) {
          v = n.substring(n.lastIndexOf("-")+1);
          n = n.substring(0, n.lastIndexOf("-"));
        } else {
          v = "0.0";
        }
        e = new Extension(n, p, v);
        extensions.add(e);
      }
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
		String extPath = Settings.getUserExtPath();
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
    }
    else {
      return false;
    }
  }

	public String getLoadPath(String name) {
    Extension e = find(name);
		if (e != null) {
      Debug.log(2, "ExtensionManager: found: "+ name + " ( " + e.version + " )");
      return e.path;
    }
    else {
      if (!name.endsWith(".jar")) {
        Debug.error("ExtensionManager: not found: "+ name );
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
      name = name.substring(0, name.length()-4);
    }
    String v;
    if (name.contains("-")) {
      v = name.substring(name.lastIndexOf("-")+1);
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

}

class Extension implements Serializable {
	public String name, path, version;
	public Extension(String name_, String path_, String version_) {
		name = name_;
		path = path_;
		version = version_;
	}
}
