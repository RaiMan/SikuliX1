package org.sikuli.script.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.recorder.RecordedEventsFlow;
import org.sikuli.script.support.recorder.actions.IRecordedAction;

public class Recorder implements NativeKeyListener, NativeMouseListener, NativeMouseMotionListener {

  private static final int SCREENSHOT_DEBOUNCE_MILLIS = 100;

  private RecordedEventsFlow eventsFlow = new RecordedEventsFlow();
  private File screenshotDir;

  private boolean running = false;

  static {
    try {
      Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);

      GlobalScreen.registerNativeHook();
    } catch (NativeHookException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        try {
          GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });

  }

  private class Debouncer {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Future<?> previous = null;

    public void debounce(Runnable runnable, long delay, TimeUnit unit) {
      synchronized (this) {
        if (previous != null) {
          previous.cancel(false);
        }

        previous = scheduler.schedule(new Runnable() {
          @Override
          public void run() {
            try {
              runnable.run();
            } finally {
              synchronized (Debouncer.this) {
                previous = null;
              }
            }
          }
        }, delay, unit);
      }
    }
  }

  private Debouncer debouncer = new Debouncer();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private void screenshot(boolean debounce) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        ScreenImage img = Screen.getPrimaryScreen().capture();
        String file = img.save(screenshotDir.getAbsolutePath());
        eventsFlow.addScreenshot(file);
      }
    };    
    
    if (debounce) {
      debouncer.debounce(runnable, SCREENSHOT_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS);
    }else {
      executor.execute(runnable);
    }
  }

  public void add(NativeInputEvent e) {
    eventsFlow.addEvent(e);
    screenshot(true);
  }

  public void nativeKeyPressed(NativeKeyEvent e) {
    add(e);
  }

  public void nativeKeyReleased(NativeKeyEvent e) {
    add(e);
  }

  public void nativeKeyTyped(NativeKeyEvent e) {

  }

  public void start() {
    if (!running) {
      running = true;

      eventsFlow.clear();

      try {
        screenshotDir = Files.createTempDirectory("sikulix").toFile();
        screenshotDir.deleteOnExit();
      } catch (IOException e) {
        e.printStackTrace();
      }

      screenshot(false);

      GlobalScreen.addNativeKeyListener(this);
      GlobalScreen.addNativeMouseListener(this);
      GlobalScreen.addNativeMouseMotionListener(this);
    }

  }

  public List<IRecordedAction> stop() {
    if (running) {
      running = false;
      GlobalScreen.removeNativeMouseMotionListener(this);
      GlobalScreen.removeNativeMouseListener(this);
      GlobalScreen.removeNativeKeyListener(this);

      return eventsFlow.compile();
    }
    return new ArrayList<>();
  }

  public boolean isRunning() {
    return running;
  }

  @Override
  public void nativeMouseClicked(NativeMouseEvent e) {
    // add(e);
  }

  @Override
  public void nativeMousePressed(NativeMouseEvent e) {
    add(e);
  }

  @Override
  public void nativeMouseReleased(NativeMouseEvent e) {
    add(e);
  }

  @Override
  public void nativeMouseMoved(NativeMouseEvent e) {
    add(e);
  }

  @Override
  public void nativeMouseDragged(NativeMouseEvent e) {
    add(e);
  }
}