/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

import java.util.Date;

public interface IAnimator {

  public float step();

  public boolean running();
}

abstract class TimeValueFunction {

  protected float _beginVal, _endVal;
  protected long _totalTime;

  public TimeValueFunction(float beginVal, float endVal, long totalTime) {
    _beginVal = beginVal;
    _endVal = endVal;
    _totalTime = totalTime;
  }

  public boolean isEnd(long t) {
    return t >= _totalTime;
  }

  abstract public float getValue(long t);
}

class LinearInterpolation extends TimeValueFunction {

  float _stepUnit;

  public LinearInterpolation(float beginVal, float endVal, long totalTime) {
    super(beginVal, endVal, totalTime);
    _stepUnit = (endVal - beginVal) / (float) totalTime;
  }

  @Override
  public float getValue(long t) {
    if (t > _totalTime) {
      return _endVal;
    }
    return _beginVal + _stepUnit * t;
  }
}

class QuarticEase extends TimeValueFunction {

  public QuarticEase(float beginVal, float endVal, long totalTime) {
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

class OutQuarticEase extends TimeValueFunction {

  public OutQuarticEase(float beginVal, float endVal, long totalTime) {
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

class StopExtention extends TimeValueFunction {

  TimeValueFunction _func;

  public StopExtention(TimeValueFunction func, long totalTime) {
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

class TimeBasedAnimator implements IAnimator {

  protected long _begin_time;
  protected float _beginVal, _endVal, _stepUnit;
  protected long _totalMS;
  protected boolean _running;
  protected TimeValueFunction _func;

  public TimeBasedAnimator(TimeValueFunction func) {
    _begin_time = -1;
    _running = true;
    _func = func;
  }

  @Override
  public float step() {
    if (_begin_time == -1) {
      _begin_time = (new Date()).getTime();
      return _func.getValue(0);
    }

    long now = (new Date()).getTime();
    long delta = now - _begin_time;
    float ret = _func.getValue(delta);
    _running = !_func.isEnd(delta);
    return ret;
  }

  @Override
  public boolean running() {
    return _running;
  }

  public void stop() {
    _running = false;
  }
}

class PulseAnimator implements IAnimator {

  protected float _v1, _v2;
  protected long _interval, _totalMS;
  protected boolean _running;
  protected long _begin_time = -1;

  public PulseAnimator(float v1, float v2, long interval, long totalMS) {
    _v1 = v1;
    _v2 = v2;
    _interval = interval;
    _totalMS = totalMS;
    _running = true;

  }

  @Override
  public float step() {
    if (_begin_time == -1) {
      _begin_time = (new Date()).getTime();
      return _v1;
    }

    long now = (new Date()).getTime();
    long delta = now - _begin_time;
    if (delta >= _totalMS) {
      _running = false;
    }
    if ((delta / _interval) % 2 == 0) {
      return _v1;
    } else {
      return _v2;
    }
  }

  @Override
  public boolean running() {
    return _running;
  }
}

class LinearAnimator extends TimeBasedAnimator {

  public LinearAnimator(float beginVal, float endVal, long totalMS) {
    super(new LinearInterpolation(beginVal, endVal, totalMS));
  }
}
