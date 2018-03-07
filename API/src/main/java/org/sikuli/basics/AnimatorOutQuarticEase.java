/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

public class AnimatorOutQuarticEase extends AnimatorTimeValueFunction {

  public AnimatorOutQuarticEase(float beginVal, float endVal, long totalTime) {
    super(beginVal, endVal, totalTime);
  }

  @Override
  public float getValue(long t) {
    if (t > _totalTime) {
      return _endVal;
    }
    double t1 = (double) t / _totalTime;
    double t2 = t1 * t1;
    return (float) (_beginVal
            + (_endVal - _beginVal) * (-1 * t2 * t2 + 4 * t1 * t2 - 6 * t2 + 4 * t1));
  }
}
