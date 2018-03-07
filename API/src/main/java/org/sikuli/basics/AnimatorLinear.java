/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

public class AnimatorLinear extends AnimatorTimeBased {

  public AnimatorLinear(float beginVal, float endVal, long totalMS) {
    super(new AnimatorLinearInterpolation(beginVal, endVal, totalMS));
  }
}
