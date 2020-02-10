/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXOpenCV;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

/**
 * to define a more complex search target<br>
 * - non-standard minimum similarity <br>
 * - click target other than center <br>
 * - masked image (ignore transparent pixels)
 * - resize in case of different environment (scaled views)
 * - wait after used in actions like click
 */

//TODO all stuff besides attribute handling should be in Image

public class Pattern extends Image {

  //private double similarity = Settings.MinSimilarity;
  //private Location offset = new Location(0, 0);

  //<editor-fold desc="00 instance">
  //TODO not needed anymore
  private Image image = null;

  /**
   * creates empty Pattern object at least setFilename() or setBImage() must be used before the
   * Pattern object is ready for anything
   */
  public Pattern() {
  }

  protected void copyAllAttributes(Element element) {
    super.copyAllAttributes(element);
    if (element instanceof Pattern) {
      similarity = ((Pattern) element).getSimilarity();
      offset.x = ((Pattern) element).offset.x;
      offset.y = ((Pattern) element).offset.y;
      copyMaskAttributes((Pattern) element);
      resizeFactor = ((Pattern) element).resizeFactor;
      waitAfter = ((Pattern) element).waitAfter;
    }
  }

  /**
   * create a new Pattern from another (attribs are copied)
   *
   * @param p other Pattern
   */
  public Pattern(Pattern p) {
    image = p.getImage();
  }

  /**
   * create a Pattern with given image<br>
   *
   * @param img Image
   */
  public Pattern(Image img) {
    image = img.create(img);
  }

  /**
   * create a Pattern based on an image file name<br>
   *
   * @param imgpath image filename
   */
  public Pattern(String imgpath) {
    image = Image.create(imgpath);
  }

  /**
   * Pattern from a Java resource (Object.class.getResource)
   *
   * @param url image file URL
   */
  public Pattern(URL url) {
    if (null == url) {
      RunTime.terminate(999, "Pattern(URL): given url is null - a resource might not be available");
    }
    image = Image.create(url);
  }

  /**
   * A Pattern from a BufferedImage
   *
   * @param bimg BufferedImage
   */
  public Pattern(BufferedImage bimg) {
    image = new Image(bimg);
  }

  /**
   * A Pattern from a ScreenImage
   *
   * @param simg ScreenImage
   */
  public Pattern(ScreenImage simg) {
    image = new Image(simg.getBufferedImage());
  }

  @Override
  public String toString() {
    String ret = "P(" + (image == null ? "null" : image.getName())
        + (isValid() ? "" : " -- not valid!")
        + ")";
    ret += " S: " + similarity;
    if (offset.x != 0 || offset.y != 0) {
      ret += " T: " + offset.x + "," + offset.y;
    }
    if (withMask || isMask) {
      ret += " masked";
    }
    return ret;
  }
  //</editor-fold>

  //<editor-fold desc="resize">
  //TODO revise implementation of auto-resize
  public Pattern resize(float factor) {
    resizeFactor = factor;
    return this;
  }

  public float getResize() {
    return resizeFactor;
  }

  private float resizeFactor = 0;
  //</editor-fold>

  //<editor-fold desc="mask">
  private boolean withMask = false;
  private Mat patternMask = SXOpenCV.newMat();
  private boolean isMask = false;

  private void copyMaskAttributes(Pattern pattern) {
    patternMask = pattern.patternMask.clone();
    isMask = pattern.isMask;
    withMask = pattern.withMask;
  }

  public Pattern mask(String sMask) {
    return withMask(new Pattern(Image.create(sMask)));
  }

  public Pattern mask(Image iMask) {
    return withMask(new Pattern(iMask));
  }

  public Pattern mask(Pattern pMask) {
    return withMask(pMask);
  }

  public Pattern withMask(Pattern pMask) {
    if (isValid()) {
      Mat mask = SXOpenCV.newMat();
      if (pMask.isValid()) {
        if (pMask.hasMask()) {
          mask = pMask.getMask();
        } else {
          mask = pMask.extractMask();
        }
      }
      if (mask.empty()
          || image.getSize().getWidth() != mask.width()
          || image.getSize().getHeight() != mask.height()) {
        Debug.log(-1, "Pattern (%s): withMask: not valid", image, pMask.image);
        mask = SXOpenCV.newMat();
      } else {
        Debug.log(3, "Pattern: %s withMask: %s", image, pMask.image);
      }
      if (!mask.empty()) {
        patternMask = mask;
        withMask = true;
      }
    }
    return this;
  }

  public Pattern withMask() {
    return mask();
  }

  public Mat getMask() {
    return patternMask;
  }

  public boolean hasMask() {
    return !patternMask.empty();
  }

  public Pattern mask() {
    return asMask();
  }

  public Pattern asMask() {
    if (isValid()) {
      Debug.log(3, "Pattern: asMask: %s", image);
      Mat mask = extractMask();
      if (!mask.empty()) {
        patternMask = mask;
        isMask = true;
      } else {
        Debug.log(-1, "Pattern: asMask: not valid", image);
      }
    }
    return this;
  }

  private Mat extractMask() {
    List<Mat> mats = SXOpenCV.extractMask(SXOpenCV.makeMat(image.get(), false), false);
    return mats.get(1);
  }
  //</editor-fold>

  //<editor-fold desc="filename, URL">
  /**
   * set a new image for this pattern
   *
   * @param fileName image filename
   * @return the Pattern itself
   */
  public Pattern setFilename(String fileName) {
    image = Image.create(fileName);
    return this;
  }

  /**
   * set a new image for this pattern
   *
   * @param fileURL image file URL
   * @return the Pattern itself
   */
  public Pattern setFilename(URL fileURL) {
    image = Image.create(fileURL);
    return this;
  }

  /**
   * set a new image for this pattern
   *
   * @param img Image
   * @return the Pattern itself
   */
  public Pattern setFilename(Image img) {
    image = img;
    return this;
  }

  /**
   * the current image's absolute filepath
   * <br>will return null, if image is in jar or in web
   * <br>use getFileURL in this case
   *
   * @return might be null
   */
  public String getFilename() {
    return image.getFilename();
  }

  /**
   * the current image's URL
   *
   * @return might be null
   */
  public URL getFileURL() {
    return image.getURL();
  }
  //</editor-fold>

  //<editor-fold desc="similarity">
  /**
   * sets the minimum Similarity to use with findX
   *
   * @param sim value 0 to 1
   * @return the Pattern object itself
   */
  public Pattern similar(double sim) {
    similarity = sim;
    return this;
  }

  /**
   * sets the minimum Similarity to 0.99 which means exact match
   *
   * @return the Pattern object itself
   */
  public Pattern exact() {
    similarity = 0.99;
    return this;
  }

  /**
   * @return the current minimum similarity
   */
  public double getSimilar() {
    return this.similarity;
  }
  //</editor-fold>

  //<editor-fold desc="target offset">
  /**
   * set the offset from the match's center to be used with mouse actions
   *
   * @param dx x offset
   * @param dy y offset
   * @return the Pattern object itself
   */
  public Pattern targetOffset(int dx, int dy) {
    offset.x = dx;
    offset.y = dy;
    return this;
  }

  /**
   * set the offset from the match's center to be used with mouse actions
   *
   * @param loc Location
   * @return the Pattern object itself
   */
  public Pattern targetOffset(Location loc) {
    offset.x = loc.x;
    offset.y = loc.y;
    return this;
  }

  /**
   * @return the current offset
   */
  public Location getTargetOffset() {
    return offset;
  }
  //</editor-fold>

  //<editor-fold desc="target image">
  /**
   * INTERNAL: USE! Might vanish without notice!
   *
   * @return might be null
   */
  public BufferedImage getBImage() {
    return getBufferedImage();
  }
  //</editor-fold>
}
