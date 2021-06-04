/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.support.recorder;

import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.SikuliXception;
import org.sikuli.support.Commons;
import org.sikuli.support.RunTime;
import org.sikuli.support.recorder.actions.IRecordedAction;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records native input events and transforms them into executable actions.
 *
 * @author balmma
 */
public class Recorder implements NativeKeyListener, NativeMouseListener, NativeMouseMotionListener, NativeMouseWheelListener {

  private static final long MOUSE_SCREENSHOT_DELAY = 500;
  private static final long MOUSE_MOVE_SCREENSHOT_DELAY = 100;
  private static final long KEY_SCREENSHOT_DELAY = 500;

  private static final int MOUSE_MOVE_THRESHOLD = 20;

  private RecordedEventsFlow eventsFlow = new RecordedEventsFlow();
  private File screenshotDir;

  private volatile boolean running = false;

  ScreenImage currentImage = null;
  String currentImageFilePath = null;

  private long currentMouseX = 0;
  private long currentMouseY = 0;
  private Location currentMousePos = null;

  private boolean capturing = false;
  private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

  private static void registerNativeHook() {
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
  }

  private static void unregisterNativeHook() {
    try {
      /*
       * We unregister the native hook on Windows because it blocks some special keys
       * in AWT while registered (e.g. AltGr).
       *
       * We do not unregister on Linux because this terminates the whole JVM.
       * Interestingly, the special keys are not blocked on Linux at all.
       *
       * TODO: Has to be checked on Mac OS, but I guess that not unregistering is
       * the better option here.
       *
       * Re-registering doesn't hurt anyway, because JNativeHook checks the register
       * state before registering again. So unregister is only really needed on Windows.
       */
      if (Settings.isWindows()) {
        GlobalScreen.unregisterNativeHook();
      }
    } catch (NativeHookException e) {
      Debug.error("Error unregistering native hook: %s", e.getMessage());
    }
  }

  /*
   * Captures the screen after a given delay. During the delay, other calls to
   * this method are ignored.
   */
  private void screenshot(long delayMillis) {
    synchronized (SCHEDULER) {
      if (!capturing) {
        capturing = true;
        SCHEDULER.schedule((() -> {
          try {
            synchronized (screenshotDir) {
              if (screenshotDir.exists()) {
                final Screen screen = getRelevantScreen();
                ScreenImage img = screen.capture();
                // Dedupe screenshots
                if (img.diffPercentage(currentImage) > 0.0001) {
                  currentImage = img;
                  currentImageFilePath = currentImage.saveInto(screenshotDir);
                }
                final int screenID = screen.getID();
                if (screenID > 9) {
                  RunTime.terminate(999, "Recorder: screen id > 9 --- not implemented");
                }
                String pathToSave = screenID + currentImageFilePath;
                eventsFlow.addScreenshot(pathToSave);
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

  private Screen getRelevantScreen() {
    return currentMousePos.getMonitor();
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
      Commons.loadOpenCV();
      running = true;

      eventsFlow.clear();
      currentImage = null;
      currentImageFilePath = null;

      try {
        screenshotDir = Files.createTempDirectory("sikulix").toFile();
        screenshotDir.deleteOnExit();
      } catch (IOException e) {
        throw new SikuliXception("Recorder: createTempDirectory: not possible");
      }

      screenshot(0);

      Recorder.registerNativeHook();
      //GlobalScreen.addNativeKeyListener(this);
      GlobalScreen.addNativeMouseListener(this);
      GlobalScreen.addNativeMouseMotionListener(this);
      GlobalScreen.addNativeMouseWheelListener(this);
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

      GlobalScreen.removeNativeMouseWheelListener(this);
      GlobalScreen.removeNativeMouseMotionListener(this);
      GlobalScreen.removeNativeMouseListener(this);
//      GlobalScreen.removeNativeKeyListener(this);
      Recorder.unregisterNativeHook();

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
    currentMousePos = new Location(currentMouseX, currentMouseY);
  }

  private void addMouseIfRelevantMove(NativeMouseEvent e) {
    // only add relevant mouse moves > MOUSE_MOVE_THRESHOLD px
    if (Math.abs(e.getX() - currentMouseX) > MOUSE_MOVE_THRESHOLD
        || Math.abs(e.getY() - currentMouseY) > MOUSE_MOVE_THRESHOLD) {
      saveMousePosition(e);
      add(e, MOUSE_MOVE_SCREENSHOT_DELAY);
    }
  }

  @Override
  public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
    saveMousePosition(e);
    add(e, MOUSE_SCREENSHOT_DELAY);
  }
}