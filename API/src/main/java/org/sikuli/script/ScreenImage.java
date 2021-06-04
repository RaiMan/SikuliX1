/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Settings;
import org.sikuli.support.FileManager;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.support.Commons;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Date;
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
	int x, y, w, h;
	Rectangle rect;
	BufferedImage bimg;
	String filename = null;

	/**
	 * create ScreenImage with given
	 *
	 * @param rect the rectangle it was taken from
	 * @param img the BufferedImage
	 */
	public ScreenImage(Rectangle rect, BufferedImage img) {
		bimg = img;
		x = rect.x;
		y = rect.y;
		w = bimg.getWidth();
		if (w != rect.width) {
			rect.width = w;
		}
		h = bimg.getHeight();
		if (h != rect.height) {
			rect.height = h;
		}
		this.rect = rect;
	}

	/**
	 * create ScreenImage from given
	 *
	 * @param shotFile previously saved screenshot
	 */
	public ScreenImage(File shotFile) {
		bimg = Image.getBufferedImage(shotFile);
		x = 0;
		y = 0;
		w = bimg.getWidth();
		h = bimg.getHeight();
		rect = new Rectangle(x, y, w, h);
	}

  public ScreenImage getSub(Rectangle sub) {
    if (!rect.contains(sub)) {
      return this;
    }
    BufferedImage img = bimg.getSubimage(sub.x - x, sub.y - y, sub.width, sub.height);
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
    if (filename == null) {
      filename = save();
    }
    return filename;
  }

  public String getStoredAt() {
	  return filename;
  }

	/**
	 * stores the image as PNG file in the standard temp folder
	 * with a created filename (sikuliximage-timestamp.png)
	 * if not yet stored before
	 *
	 * @return absolute path to stored file
	 */
  public String save() {
    return FileManager.saveTimedImage(bimg, Commons.getTempFolder().getAbsolutePath(), "#sikuliximage");
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
    return FileManager.saveTimedImage(bimg, path, "#sikuliximage");
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
    return FileManager.saveTimedImage(bimg, path, name);
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
		return filename;
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
		if (!filename.equals(this.filename) || image.getName().startsWith("_")) {
			ImageIO.write(bimg, "png", image);
			this.filename = filename;
		}
	}

	/**
	 *
	 * @return the stored image in memory
	 */
	public BufferedImage getImage() {
		return bimg;
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
	public Rectangle getRect() {
		return rect;
	}

  public void saveLastScreenImage(File fPath) {
    try {
  		ImageIO.write(bimg, "png", new File(fPath, "LastScreenImage.png"));
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

    Imgproc.cvtColor(Commons.makeMat(this.getImage()), thisGray, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(Commons.makeMat(((ScreenImage) other).getImage()), otherGray, Imgproc.COLOR_BGR2GRAY);
    Core.absdiff(thisGray, otherGray, mDiffAbs);
    return Core.countNonZero(mDiffAbs) == 0;
  }

  public double diffPercentage(ScreenImage scrImg) {
		if (scrImg == null || this.w != scrImg.w || this.h != scrImg.h) {
			return 1;
		}
		return (double) diffPixel(scrImg) / (w * h);
	}

	private int diffPixel(ScreenImage scrImg) {
		Mat thisGray = new Mat();
		Mat otherGray = new Mat();
		Mat mDiffAbs = new Mat();

		Imgproc.cvtColor(Commons.makeMat(this.getImage()), thisGray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(Commons.makeMat(scrImg.getImage()), otherGray, Imgproc.COLOR_BGR2GRAY);
		Core.absdiff(thisGray, otherGray, mDiffAbs);
		final int countNonZero = Core.countNonZero(mDiffAbs);
		return countNonZero;
	}

	/**
	 * stores the image as PNG file in the given path
	 * with a created filename (sikuliximage-timestamp.png)
	 *
	 * @param path valid path string
	 * @return absolute path to stored file
	 */
	public String saveInto(String path) {
		return saveInto(new File(path));
	}

	public String saveInto(File path) {
		File fImage = new File(path, String.format("%s-%d.png", "sikuliximage", new Date().getTime()));
		try {
			ImageIO.write(bimg, FilenameUtils.getExtension(fImage.getName()), fImage);
			Debug.log(3, "ScreenImage::saveImage: %s", fImage);
		} catch (Exception ex) {
			Debug.error("ScreenImage::saveInto: did not work: %s (%s)", fImage, ex.getMessage());
			return null;
		}
		return fImage.getAbsolutePath();
	}

}
