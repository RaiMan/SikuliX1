/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.runner;

public class AbortableScriptRunnerWrapper {
  private Object runnerLock = new Object();
  private IRunner runner;

  public IRunner getRunner() {
    synchronized(runnerLock) {
      return runner;
    }
  }

  public void setRunner(IRunner runner) {
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
