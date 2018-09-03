/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FindInput2 {

  static {
    Finder2.init();
  }

  private Mat mask = new Mat();

  protected boolean hasMask() {
    return !mask.empty();
  }

  protected Mat getMask() {
    return mask;
  }

  protected void setMask(Mat mask) {
    this.mask = mask;
  }

  private Mat targetBGR = new Mat();

  public String getTargetText() {
    return targetText;
  }

  private String targetText = "";

  public void setWhere(Region where) {
    this.where = where;
  }

  public Region getWhere() {
    return where;
  }

  private Region where = null;

  private double similarity = 0.7;

  private boolean findAll = false;

  private Mat target = null;
  private boolean targetTypeText = false;

  public boolean isValid() {
    if (Do.SX.isNull(source) && Do.SX.isNull(where)) {
      return false;
    }
    if (Do.SX.isNotNull(target)) {
      return true;
    }
    if (targetTypeText) {
      return true;
    }
    return false;
  }

  public int getTextLevel() {
    return textLevel;
  }

  public void setTextLevel(int textLevel) {
    this.textLevel = textLevel;
  }

  private int textLevel = -1;

  public boolean isText() {
    return targetTypeText;
  }

  public boolean isFindAll() {
    return findAll;
  }

  public void setTargetText(String text) {
    targetText = text;
    targetTypeText = true;
  }

  public void setTarget(Mat target) {
    this.target = target;
  }

  public Mat getTarget() {
    if (targetBGR.empty()) {
      return target;
    }
    return targetBGR;
  }

  private Mat source = null;

  public void setSource(Mat source) {
    this.source = source;
  }

  public Mat getBase() {
    return source;
  }

  boolean isPattern = false;

  public void setIsPattern() {
    isPattern = true;
  }

  public boolean isPattern() {
    return isPattern;
  }

  public void setPattern(boolean pattern) {
    isPattern = pattern;
  }

  public void setSimilarity(double similarity) {
    this.similarity = similarity;
  }

  public boolean isExact() {
    return similarity >= 0.99;
  }

  public boolean shouldSearchDownsized(float resizeMinFactor) {
    return !hasMask() && !isExact() && !isFindAll() && getResizeFactor() > resizeMinFactor;
  }

  private double scoreMaxDiff = 0.05;

  protected double getScoreMaxDiff() {
    return scoreMaxDiff;
  }

  protected double getScore() {
    return similarity;
  }

  public void setFindAll() {
    findAll = true;
  }

  protected boolean plainColor = false;
  protected boolean blackColor = false;
  protected boolean whiteColor = false;

  public boolean isPlainColor() {
    return isValid() && plainColor;
  }

  public boolean isBlack() {
    return isValid() && blackColor;
  }

  public boolean isWhite() {
    return isValid() && blackColor;
  }

  public double getResizeFactor() {
    return isValid() ? resizeFactor : 1;
  }

  protected double resizeFactor;

  private final int resizeMinDownSample = 12;
  private int[] meanColor = null;
  private double minThreshhold = 1.0E-5;

  public Color getMeanColor() {
    return new Color(meanColor[2], meanColor[1], meanColor[0]);
  }

  public boolean isMeanColorEqual(Color otherMeanColor) {
    Color col = getMeanColor();
    int r = (col.getRed() - otherMeanColor.getRed()) * (col.getRed() - otherMeanColor.getRed());
    int g = (col.getGreen() - otherMeanColor.getGreen()) * (col.getGreen() - otherMeanColor.getGreen());
    int b = (col.getBlue() - otherMeanColor.getBlue()) * (col.getBlue() - otherMeanColor.getBlue());
    return Math.sqrt(r + g + b) < minThreshhold;
  }

  double targetStdDev = -1;
  double targetMean = -1;

  public void setAttributes() {
    if (targetTypeText) {
      return;
    }
    List<Mat> mats = Finder2.extractMask(target, true);
    targetBGR = mats.get(0);
    if (mask.empty()) {
      mask = mats.get(1);
    }

    //TODO plaincolor/black with masking
    plainColor = false;
    blackColor = false;
    resizeFactor = Math.min(((double) targetBGR.width()) / resizeMinDownSample,
            ((double) targetBGR.height()) / resizeMinDownSample);
    resizeFactor = Math.max(1.0, resizeFactor);
    MatOfDouble pMean = new MatOfDouble();
    MatOfDouble pStdDev = new MatOfDouble();
    Mat check = new Mat();

    if (mask.empty()) {
      check = targetBGR;
    } else {
      Core.multiply(targetBGR, mask, check);
    }
    Core.meanStdDev(check, pMean, pStdDev);
    double sum = 0.0;
    double[] arr = pStdDev.toArray();
    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }
    targetStdDev = sum;
    if (sum < minThreshhold) {
      plainColor = true;
    }
    sum = 0.0;
    arr = pMean.toArray();
    meanColor = new int[arr.length];
    for (int i = 0; i < arr.length; i++) {
      meanColor[i] = (int) arr[i];
      sum += arr[i];
    }
    targetMean = sum;
    if (sum < minThreshhold && plainColor) {
      blackColor = true;
    }
    if (meanColor.length > 1) {
      whiteColor = isMeanColorEqual(Color.WHITE);
    }
  }

  public String toString() {
    return String.format("(stdDev: %.4f mean: %4f)", targetStdDev, targetMean);
  }

//TODO for compilation - remove when native is obsolete
  public static long getCPtr(FindInput2 p) {
    return 0;
  }
}
