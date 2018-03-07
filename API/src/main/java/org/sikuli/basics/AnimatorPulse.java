/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.basics;

import java.util.Date;

public class AnimatorPulse implements Animator {

  protected float _v1, _v2;
  protected long _interval, _totalMS;
  protected boolean _running;
  protected long _begin_time = -1;

  public AnimatorPulse(float v1, float v2, long interval, long totalMS) {
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
