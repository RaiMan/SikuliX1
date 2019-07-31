/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;

import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;
import org.sikuli.basics.Debug;
import org.sikuli.script.Finder;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.recorder.RecordedEventsFlow;
import org.sikuli.script.support.recorder.actions.IRecordedAction;

/**
 * Records native input events and transforms them into executable actions.
 *
 * @author balmma
 */
public class Recorder implements NativeKeyListener, NativeMouseListener, NativeMouseMotionListener {

  private static final long MOUSE_SCREENSHOT_DELAY = 500;
  private static final long MOUSE_MOVE_SCREENSHOT_DELAY = 100;
  private static final long KEY_SCREENSHOT_DELAY = 500;

  private static final int MOUSE_MOVE_THRESHOLD = 10;

  private RecordedEventsFlow eventsFlow = new RecordedEventsFlow();
  private File screenshotDir;

  private boolean running = false;

  ScreenImage currentImage = null;
  String currentImageFilePath = null;

  private long currentMouseX = 0;
  private long currentMouseY = 0;

  static {
    try {
      // Make Global Screen logger quiet.
      // Floods the console otherwise
      Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);

      GlobalScreen.registerNativeHook();
    } catch (NativeHookException e) {
      Debug.error("Error registering native hook: %s", e.getMessage());
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
          Debug.error("Error unregistering native hook: %s", e.getMessage());
        }
      }
    });

  }

  private boolean capturing = false;
  private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

  /*
   * Captures the screen after a given delay.
   * During the delay, other calls to this method are ignored.
   */
  private void screenshot(long delayMillis) {
    synchronized (SCHEDULER) {
      if (!capturing) {
        capturing = true;
        SCHEDULER.schedule((() -> {
          try {
            synchronized (screenshotDir) {
              if (screenshotDir.exists()) {
                ScreenImage img = Screen.getPrimaryScreen().capture();
                // Dedupe screenshots
                if (new Finder(img).findDiffPercentage(currentImage) > 0.0001) {
                  currentImage = img;
                  currentImageFilePath = currentImage.save(screenshotDir.getAbsolutePath());
                  eventsFlow.addScreenshot(currentImageFilePath);
                } else {
                  eventsFlow.addScreenshot(currentImageFilePath);
                }
              }
            }
          } finally {
            synchronized (SCHEDULER) {
              capturing = false;
            }
          }
        }), delayMillis, TimeUnit.MILLISECONDS);
      }
    }
  }

  private void add(NativeInputEvent e, long screenshotDelayMillis) {
    eventsFlow.addEvent(e);
    screenshot(screenshotDelayMillis);
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    add(e, KEY_SCREENSHOT_DELAY);
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
    add(e, KEY_SCREENSHOT_DELAY);
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent e) {
    // do not handle
  }

  /**
   * starts recording
   */
  public void start() {
    if (!running) {
      running = true;

      eventsFlow.clear();
      currentImage = null;
      currentImageFilePath = null;

      try {
        screenshotDir = Files.createTempDirectory("sikulix").toFile();
        screenshotDir.deleteOnExit();
      } catch (IOException e) {
        e.printStackTrace();
      }

      screenshot(0);

      GlobalScreen.addNativeKeyListener(this);
      GlobalScreen.addNativeMouseListener(this);
      GlobalScreen.addNativeMouseMotionListener(this);
    }

  }

  /**
   * Stops recording and transforms the recorded events into actions.
   *
   * @param progress optional ProgressMonitor
   * @return actions resulted from the recorded events
   */
  public List<IRecordedAction> stop(ProgressMonitor progress) {
    if (running) {
      running = false;
      GlobalScreen.removeNativeMouseMotionListener(this);
      GlobalScreen.removeNativeMouseListener(this);
      GlobalScreen.removeNativeKeyListener(this);

      synchronized (screenshotDir) {
        List<IRecordedAction> actions = eventsFlow.compile(progress);

        // remove screenshots after compile to free up disk space
        try {
          FileUtils.deleteDirectory(screenshotDir);
        } catch (IOException e) {
          e.printStackTrace();
        }
        return actions;
      }
    }
    return new ArrayList<>();
  }

  public boolean isRunning() {
    return running;
  }

  @Override
  public void nativeMouseClicked(NativeMouseEvent e) {
    // do not handle
  }

  @Override
  public void nativeMousePressed(NativeMouseEvent e) {
    saveMousePosition(e);
    add(e, MOUSE_SCREENSHOT_DELAY);
  }

  @Override
  public void nativeMouseReleased(NativeMouseEvent e) {
    saveMousePosition(e);
    add(e, MOUSE_SCREENSHOT_DELAY);
  }

  @Override
  public void nativeMouseMoved(NativeMouseEvent e) {
    addMouseIfRelevantMove(e);
  }

  @Override
  public void nativeMouseDragged(NativeMouseEvent e) {
    addMouseIfRelevantMove(e);
  }

  private void saveMousePosition(NativeMouseEvent e) {
    currentMouseX = e.getX();
    currentMouseY = e.getY();
  }

  private void addMouseIfRelevantMove(NativeMouseEvent e) {
    // only add relevant mouse moves > MOUSE_MOVE_THRESHOLD px
    if (Math.abs(e.getX() - currentMouseX) > MOUSE_MOVE_THRESHOLD
        || Math.abs(e.getY() - currentMouseY) > MOUSE_MOVE_THRESHOLD) {
      saveMousePosition(e);
      add(e, MOUSE_MOVE_SCREENSHOT_DELAY);
    }
  }
}