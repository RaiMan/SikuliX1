/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.basics.FileManager;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * INTERNAL USE: This will be replaced by Image in the long run<br>
 * Hence do not use it in your application.<br>
 * <br>
 * If you want to handle screen captures:<br>
 * Image = Region.getImage()<br>
 * ... and then use the Image features<br>
 *<br>
 * stores a BufferedImage usually ceated by screen capture,
 * the screen rectangle it was taken from and
 * the filename, where it is stored as PNG (only if requested)
 */
public class ScreenImage {

	/**
	 * x, y, w, h of the stored ROI
	 *
	 */
	public int x, y, w, h;
	protected Rectangle rect;
	protected BufferedImage _img;
	protected String _filename = null;

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}

	private long timeCreated = -1;



	private Location start;
	private Location end;

	public void setStartEnd(Location start, Location end) {
		this.start = start;
		this.end = end;
	}

	public Location getStart() {
		return start;
	}

	public Location getEnd() {
		return end;
	}

	/**
	 * create ScreenImage with given
	 *
	 * @param rect the rectangle it was taken from
	 * @param img the BufferedImage
	 */
	public ScreenImage(Rectangle rect, BufferedImage img) {
		_img = img;
		this.rect = rect;
		x = (int) rect.getX();
		y = (int) rect.getY();
		w = _img.getWidth();
		h = _img.getHeight();
	}

	public Rectangle getRect() {
		return rect;
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
	  return getFile();
  }

	/**
	 * stores the image as PNG file in the standard temp folder
	 * with a created filename (sikuliximage-timestamp.png)
	 * if not yet stored before
	 *
	 * @return absolute path to stored file
	 */
  public String save() {
    return FileManager.saveTimedImage(_img, Commons.getTempFolder().getAbsolutePath(), "#sikuliximage");
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
    return FileManager.saveTimedImage(_img, path, "#sikuliximage");
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
      File image = new File(path, name);
      storeImage(image);
      Debug.log(3, "ScreenImage.store: %s", image);
    } catch (IOException iOException) {
      Debug.error("ScreenImage.store: IOException", iOException);
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
    File fImage = null;
    if (null != ImagePath.getBundlePath()) {
			try {
				fImage = new File(ImagePath.getBundlePath(), name);
				storeImage(fImage);
				Debug.log(3, "ScreenImage: saveInBundle: %s", fImage);
			} catch (IOException iOException) {
				Debug.error("ScreenImage: saveInBundle: did not work: %s (%s)",fImage, iOException);
				return null;
			}
			Image.reload(fImage.getAbsolutePath());
			return fImage.getAbsolutePath();
		}
    return null;
  }

	// store image to given path if not yet stored
	private void storeImage(File image) throws IOException {
		String filename = image.getAbsolutePath();
		if (!filename.equals(_filename) || image.getName().startsWith("_")) {
			ImageIO.write(_img, "png", image);
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
		return new Region(rect);
	}

	/**
	 *
	 * @return the screen rectangle, the iamge was created from
	 */
	public Rectangle getROI() {
		return rect;
	}

	public void saveLastScreenImage(File fPath) {
		saveLastScreenImage(fPath, -1);
	}

	public void saveLastScreenImage(File fPath, int id) {
    try {
  		ImageIO.write(_img, "png", new File(fPath, "LastScreenImage" +
					(id < 0 ? "" : "" + id) + ".png"));
    } catch (Exception ex) {}
  }

  @Override
  public boolean equals(Object other) {
    if(this == other) {
      return true;
    }

    if(other == null || !(other instanceof ScreenImage)) {
      return false;
    }

    Mat thisGray = new Mat();
    Mat otherGray = new Mat();
    Mat mDiffAbs = new Mat();

    Imgproc.cvtColor(Image.makeMat(this.getImage()), thisGray, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(Image.makeMat(((ScreenImage) other).getImage()), otherGray, Imgproc.COLOR_BGR2GRAY);
    Core.absdiff(thisGray, otherGray, mDiffAbs);
    return Core.countNonZero(mDiffAbs) == 0;
  }

}
