/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXOpenCV;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * stores a BufferedImage usually ceated by screen capture,
 * the screen rectangle it was taken from and
 * the filename, where it is stored as PNG (only if requested)
 */
public class ScreenImage extends Image {

  //<editor-fold desc="00 instance">
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
    onScreen(false);
  }

  /**
   * create ScreenImage from given
   *
   * @param shotFile previously saved screenshot
   */
  public ScreenImage(File shotFile) {
    Image image = new Image(shotFile);
    _img = image.getBufferedImage();
    x = 0;
    y = 0;
    w = _img.getWidth();
    h = _img.getHeight();
    _roi = new Rectangle(x, y, w, h);
  }

  public Mat getContent() {
    if (super.getContent().empty()) {
      setContent(SXOpenCV.makeMat(_img));
    }
    return super.getContent();
  }

  public Location start;
  public Location end;

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
   * @return the stored image in memory
   */
  public BufferedImage getBufferedImage() {
    return _img;
  }

  public Mat makeMat() {
    return SXOpenCV.makeMat(_img);
  }

  /**
   * @return the Region, the iamge was created from
   */
  public Region getRegion() {
    return new Region(_roi);
  }

  /**
   * @return the screen rectangle, the iamge was created from
   */
  public Rectangle getROI() {
    return _roi;
  }
  //</editor-fold>

  //TODO getSub
  public ScreenImage getSub(Rectangle sub) {
    if (!_roi.contains(sub)) {
      return this;
    }
    BufferedImage img = _img.getSubimage(sub.x - x, sub.y - y, sub.width, sub.height);
    return new ScreenImage(sub, img);
  }

  //<editor-fold desc="10 save content --- to be revised">

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
      ImageIO.write(_img, FilenameUtils.getExtension(fImage.getName()), fImage);
      log(3, "saveImage: %s", fImage);
    } catch (Exception ex) {
      log(-1, "saveTimedImage: did not work: %s (%s)", fImage, ex.getMessage());
      return null;
    }
    return fImage.getAbsolutePath();
  }

  public String save(String name) {
    if (!name.endsWith(".png")) {
      name += ".png";
    }
    if (!name.startsWith("_")) {
      name = "_" + name;
    }
    String fileName = super.save(name);
    //TODO check success (image missing?)
    Image.reload(fileName);
    return fileName;
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
      File imageFile = new File(path, name);
      storeImage(imageFile);
      Debug.log(3, "ScreenImage.store: %s", imageFile);
    } catch (IOException iOException) {
      Debug.error("ScreenImage.store: IOException", iOException);
      return null;
    }
    return _filename;
  }

  // store image to given path if not yet stored
  private void storeImage(File imageFile) throws IOException {
    String filename = imageFile.getAbsolutePath();
    if (!filename.equals(_filename) || imageFile.getName().startsWith("_")) {
      ImageIO.write(_img, FilenameUtils.getExtension(filename), imageFile);
      _filename = filename;
    }
  }

//TODO save as timestamped image

//	/**
//	 * stores the image as PNG file in the given path
//	 * with a created filename (givenName-timestamp.png)
//	 *
//	 * @param path valid path string
//	 * @param name file name
//	 * @return absolute path to stored file
//	 */
//  public String save(String path, String name) {
//    return FileManager.saveTimedImage(_img, path, name);
//  }

  /**
   * creates the PNG tempfile only when needed.
   *
   * @return absolute path to stored tempfile
   * @deprecated use save() instead
   */
  @Deprecated
  public String getFilename() {
    return getFile();
  }

  /**
   * INTERNAL USE: use getTimedFile() instead
   *
   * @return absolute path to stored file
   */
  private String getFile() {
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
    return FileManager.saveTimedImage(_img, RunTime.get().fpBaseTempPath, "#sikuliximage");
  }

  /**
   * stores the image as PNG file in the given path
   * with a created filename (sikuliximage-timestamp.png)
   *
   * @param path valid path string
   * @return absolute path to stored file
   */
  public String getFile(String path) {
    return saveInto(path);
  }

  public void saveLastScreenImage(File fPath) {
    try {
      ImageIO.write(_img, "png", new File(fPath, "LastScreenImage.png"));
    } catch (Exception ex) {
    }
  }
  //</editor-fold>

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || !(other instanceof Image)) {
      return false;
    }
    return diffPercentage((Image) other) == 0;
  }

}
