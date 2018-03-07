/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

public class AnimatorTimeBased implements Animator {

  private long _begin_time;
  private boolean _running;
  private AnimatorTimeValueFunction _func;

  public AnimatorTimeBased(AnimatorTimeValueFunction func) {
    _begin_time = -1;
    _running = true;
    _func = func;
  }

  @Override
  public float step() {
    if (_begin_time == -1) {
      _begin_time = System.currentTimeMillis();
      return _func.getValue(0);
    }

    long now = System.currentTimeMillis();
    long delta = now - _begin_time;
    float ret = _func.getValue(delta);
    _running = !_func.isEnd(delta);
    return ret;
  }

  @Override
  public boolean running() {
    return _running;
  }
}
