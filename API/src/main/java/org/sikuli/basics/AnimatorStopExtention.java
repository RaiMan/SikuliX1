/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

public class AnimatorStopExtention extends AnimatorTimeValueFunction {

  AnimatorTimeValueFunction _func;

  public AnimatorStopExtention(AnimatorTimeValueFunction func, long totalTime) {
    super(func._beginVal, func._endVal, totalTime);
    _func = func;
    _totalTime = totalTime;
  }

  @Override
  public float getValue(long t) {
    return _func.getValue(t);
  }

  @Override
  public boolean isEnd(long t) {
    return t >= _totalTime;
  }
}
