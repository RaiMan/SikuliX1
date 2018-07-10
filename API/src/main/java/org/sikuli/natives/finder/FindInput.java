/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.natives.finder;

import org.opencv.core.Mat;

public class FindInput {

  private Mat target = null;
  private int targetType = 0;
  private String targetString = "";

  private Mat source = null;

  private double similarity = 0.7;

  private boolean findAll = false;

  public void setTarget(int target_type, String target_string) {
    targetType = target_type;
  }

  public void setTarget(Mat target) {
    this.target = target;
  }

  public void setSource(Mat source) {
    this.source = source;
  }

  public void setSimilarity(double similarity) {
    this.similarity = similarity;
  }

  public void setFindAll() {
    findAll = true;
  }
}
