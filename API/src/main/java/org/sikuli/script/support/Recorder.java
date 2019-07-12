package org.sikuli.script.support;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.MSER;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;
import org.sikuli.script.Finder;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.generators.ICodeGenerator;
import org.sikuli.script.support.recorder.RecordedEventsFlow;
import org.sikuli.script.support.recorder.actions.IRecordedAction;
import org.sikuli.script.Finder.Finder2;
import org.sikuli.script.Key;
import org.sikuli.script.Match;

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
    if (debounce) {
      debouncer.debounce(new Runnable() {
        @Override
        public void run() {
          ScreenImage img = Screen.getPrimaryScreen().capture();
          String file = img.save(screenshotDir.getAbsolutePath());
          eventsFlow.addScreenshot(file);
        }
      }, SCREENSHOT_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS);
    }else {
      executor.execute(() -> {
        ScreenImage img = Screen.getPrimaryScreen().capture();
        String file = img.save(screenshotDir.getAbsolutePath());
        eventsFlow.addScreenshot(file);
      });
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