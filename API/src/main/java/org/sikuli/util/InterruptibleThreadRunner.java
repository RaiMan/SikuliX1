/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import java.util.function.Supplier;
import com.sun.jna.ptr.IntByReference;

/**
 * This class implements an interruptible thread runner.
 *
 * The runner can be started using run() and executes the given block in a new
 * thread.
 *
 * A runner can be interrupt by calling the interrupt() method.
 *
 * @author mbalmer
 *
 */

public class InterruptibleThreadRunner {
  private Thread execThread;

  /**
   * Runs the given block in a new thread. The block is interrupted automatically
   * after the given timeout or can be interrupted by calling interrupt().
   *
   * @param timeout Timeout in milliseconds, 0 means no timeout.
   * @param block Supplier is expected to return the execution error code.
   * @return
   */
  public int run(long timeout, Supplier<Integer> block) {
    synchronized (this) {
      final IntByReference exitCode = new IntByReference(0);

      execThread = new Thread() {
        @Override
        public void run() {
          try {
            exitCode.setValue(block.get());
          } finally {
            synchronized(InterruptibleThreadRunner.this) {
              InterruptibleThreadRunner.this.notifyAll();
            }
          }
        }
      };
      execThread.start();

      if (timeout > 0) {
        Thread timeoutThread = new Thread() {
          @Override
          public void run() {
            long start = System.currentTimeMillis();

            while (null != execThread) {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              if (System.currentTimeMillis() - start > timeout) {
                InterruptibleThreadRunner.this.interrupt();
                break;
              }
            }
          }
        };
        timeoutThread.start();
      }

      try {
        this.wait();
      } catch (InterruptedException e) {
        interrupt();
      } finally {
        execThread = null;
      }

      return exitCode.getValue();
    }
  }

  public void interrupt() {
    synchronized (this) {
      if (null != execThread && execThread.isAlive()) {
        execThread.interrupt();
        execThread.stop();
      }
    }
  }
}
