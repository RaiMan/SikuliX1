/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

public class AnimatorQuarticEase extends AnimatorTimeValueFunction {

  public AnimatorQuarticEase(float beginVal, float endVal, long totalTime) {
    super(beginVal, endVal, totalTime);
  }

  @Override
  public float getValue(long t) {
    if (t > _totalTime) {
      return _endVal;
    }
    double t1 = (double) t / _totalTime;
    return (float) (_beginVal + (_endVal - _beginVal) * t1 * t1 * t1 * t1);
  }
}
