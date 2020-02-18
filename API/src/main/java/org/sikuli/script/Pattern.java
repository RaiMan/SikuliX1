/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Settings;

import java.awt.image.BufferedImage;

/**
 * to define a more complex search target<br>
 * - non-standard minimum similarity <br>
 * - click target other than center <br>
 * - masked image (ignore transparent pixels)
 * - resize in case of different environment (scaled views)
 * - wait after used in actions like click
 * <p>
 * NOTE: Pattern is a <b>candidate for deprecation</b> in the long run and will be substituted by Image
 */

public class Pattern {

  public static Pattern make(Image img, double sim, Location off, float rFactor, String mask) {
    org.sikuli.script.Pattern pattern = new org.sikuli.script.Pattern(img);
    pattern.similar(sim);
    pattern.targetOffset(off);
    pattern.resize(rFactor);
    pattern.setMaskFromString(mask);
    return pattern;
  }

  //<editor-fold desc="00 instance">
  private Pattern() {
  }

  protected void copyAllAttributes(Pattern pattern) {
    image = new Image(pattern.image);
    similarity = pattern.similarity;
    offset.x = pattern.offset.x;
    offset.y = pattern.offset.y;
    maskImage = pattern.getMask();
    resizeFactor = pattern.resizeFactor;
    waitAfter = pattern.waitAfter;
  }

  public boolean isValid() {
    return image != null && image.isValid();
  }

  /**
   * Create a Pattern from various sources.
   * <pre>
   * - from another pattern with all attributes
   * - from a file (String, URL, File)
   * - from an image {@link Element}
   * - from a BufferedImage or OpenCV Mat
   * </pre>
   *
   * @param what      the source
   * @param <PSUFEBM> see source variants
   */
  public <PSUFEBM> Pattern(PSUFEBM what) {
    if (what instanceof Pattern) {
      copyAllAttributes((Pattern) what);
    } else {
      image = new Image(what);
    }
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
    if (hasMask()) {
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
  public <SUFEBMP> Pattern mask(SUFEBMP what) {
    Image image = new Image(what, Element.asMaskImage());
    if (this.image.sameSize(image)) {
      maskImage = image;
    }
    return this;
  }

  public boolean hasMask() {
    return null != maskImage;
  }

  public Image getMask() {
    return maskImage;
  }

  private Image maskImage = null;

  public void setMaskFromString(String mask) {
    //TODO mask from string
  }
  //</editor-fold>

  //<editor-fold desc="image">
  private Image image = null;

  public Image getImage() {
    return image;
  }

  //TODO revise: used only in Guide
  public BufferedImage getBImage() {
    BufferedImage bimg = null;
    if (isValid()) {
      bimg = image.getBufferedImage();
    }
    return bimg;
  }

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
   * the current image's absolute filepath
   * <br>will return null, if image is in jar or in web
   *
   * @return might be null
   */
  //TODO should work for all images (jar, web, ...)
  public String getFilename() {
    return image.getFilename();
  }
  //</editor-fold>

  //<editor-fold desc="similarity">
  private double similarity = Settings.MinSimilarity;

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

  public Location getTargetOffset() {
    return offset;
  }

  private Location offset = new Location(0, 0);
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="waitAfter">
  private int waitAfter;

  /**
   * Get the value of waitAfter
   *
   * @return the value of waitAfter
   */
  public int waitAfter() {
    return waitAfter;
  }

  /**
   * Set the value of waitAfter
   *
   * @param waitAfter new value of waitAfter
   * @return the image
   */
  public Pattern waitAfter(int waitAfter) {
    this.waitAfter = waitAfter;
    return this;
  }
  //</editor-fold>
}
