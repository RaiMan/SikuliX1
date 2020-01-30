/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.sikuli.script.support.IScriptRunner;

public class AbortableScriptRunnerWrapper {
  private Object runnerLock = new Object();
  private IScriptRunner runner;

  public IScriptRunner getRunner() {
    synchronized(runnerLock) {
      return runner;
    }
  }

  public void setRunner(IScriptRunner runner) {
    synchronized(runnerLock) {
      this.runner = runner;
    }
  }

  public void clearRunner() {
    synchronized(runnerLock) {
      this.runner = null;
    }
  }

  public boolean isAbortSupported() {
    synchronized(runnerLock) {
      return null != runner && runner.isAbortSupported();
    }
  }

  public void doAbort() {
    synchronized(runnerLock) {
      if (isAbortSupported()) {
        runner.abort();
      }
    }
  }
}
