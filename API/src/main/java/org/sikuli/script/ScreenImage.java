/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Debug;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * CANDIDATE FOR DEPRECATION
 *
 * stores a BufferedImage usually ceated by screen capture,
 * the screen rectangle it was taken from and
 * the filename, where it is stored as PNG (only if requested)
 *
 * This will be replaced by Image in the long run
 */
public class ScreenImage {

	/**
	 * x, y, w, h of the stored ROI
	 *
	 */
	public int x, y, w, h;
	protected Rectangle _roi;
	protected BufferedImage _img;
	protected String _filename = null;

	/**
	 * create ScreenImage with given
	 *
	 * @param roi the rectangle it was taken from
	 * @param img the BufferedImage
	 */
	public ScreenImage(Rectangle roi, BufferedImage img) {
		_img = img;
		_roi = roi;
		x = (int) roi.getX();
		y = (int) roi.getY();
		w = _img.getWidth();
		h = _img.getHeight();
	}

  public ScreenImage getSub(Rectangle sub) {
    if (!_roi.contains(sub)) {
      return this;
    }
    BufferedImage img = _img.getSubimage(sub.x - x, sub.y - y, sub.width, sub.height);
    return new ScreenImage(sub, img);
  }

	/**
	 * creates the PNG tempfile only when needed.
	 *
	 * @return absolute path to stored tempfile
	 * @throws IOException if not found
	 * @deprecated use getFile() instead
	 */
	@Deprecated
	public String getFilename() throws IOException {
		return getFile();
	}

  /**
   * INTERNAL USE: use getTimedFile() instead
   * @return absolute path to stored file
   */
	public String getFile() {
    if (_filename == null) {
      _filename = save();
    }
    return _filename;
  }

  public String getStoredAt() {
	  return _filename;
  }

	/**
	 * stores the image as PNG file in the standard temp folder
	 * with a created filename (sikuliximage-timestamp.png)
	 * if not yet stored before
	 *
	 * @return absolute path to stored file
	 */
  public String save() {
    return FileManager.saveTimedImage(_img, RunTime.get().fpBaseTempPath, "sikuliximage");
  }

	/**
	 * stores the image as PNG file in the given path
	 * with a created filename (sikuliximage-timestamp.png)
	 *
	 * @param path valid path string
	 * @return absolute path to stored file
	 */
  public String getFile(String path) {
    return save(path);
  }

	/**
	 * stores the image as PNG file in the given path
	 * with a created filename (sikuliximage-timestamp.png)
	 *
	 * @param path valid path string
	 * @return absolute path to stored file
	 */
  public String save(String path) {
    return FileManager.saveTimedImage(_img, path, "sikuliximage");
  }

	/**
	 * stores the image as PNG file in the given path
	 * with a created filename (givenName-timestamp.png)
	 *
	 * @param path valid path string
	 * @param name file name
	 * @return absolute path to stored file
	 */
  public String save(String path, String name) {
    return FileManager.saveTimedImage(_img, path, name);
  }

	/**
	 * stores the image as PNG file in the given path
	 * with the given filename
	 *
	 * @param path valid path string
	 * @param name filename (.png is added if not present)
	 * @return absolute path to stored file
	 */
	public String getFile(String path, String name) {
    if (name == null) {
      name = Settings.getTimestamp() + ".png";
    } else if (!name.endsWith(".png")) {
			name += ".png";
		}
    try {
      File tmp = new File(path, name);
      createFile(tmp);
      Debug.log(3, "ScreenImage.getFile:\n%s", tmp);
    } catch (IOException iOException) {
      Debug.error("ScreenImage.getFile: IOException", iOException);
      return null;
    }
		return _filename;
	}

	public String saveInBundle(String name) {
    if (!name.endsWith(".png")) {
			name += ".png";
		}
    if (!name.startsWith("_")) {
			name = "_" + name;
		}
    File fImage = new File(name);
    try {
      fImage = new File(ImagePath.getBundlePath(), name);
      createFile(fImage);
      Debug.log(3, "ScreenImage.saveInBundle:\n%s", fImage);
    } catch (IOException iOException) {
      Debug.error("ScreenImage.saveInBundle: IOException", iOException);
      return null;
    }
    Image.reload(fImage.getAbsolutePath());
    return fImage.getAbsolutePath();
  }

	// store image to given path if not yet stored
	private void createFile(File tmp) throws IOException {
		String filename = tmp.getAbsolutePath();
		if (_filename == null || !filename.equals(_filename) || tmp.getName().startsWith("_")) {
			ImageIO.write(_img, "png", tmp);
			_filename = filename;
		}
	}

	/**
	 *
	 * @return the stored image in memory
	 */
	public BufferedImage getImage() {
		return _img;
	}

	/**
	 *
	 * @return the Region, the iamge was created from
	 */
	public Region getRegion() {
		return new Region(_roi);
	}

	/**
	 *
	 * @return the screen rectangle, the iamge was created from
	 */
	public Rectangle getROI() {
		return _roi;
	}

  public void saveLastScreenImage(File fPath) {
    try {
  		ImageIO.write(_img, "png", new File(fPath, "LastScreenImage.png"));
    } catch (Exception ex) {}
  }

}
