package org.sikuli.script.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.sikuli.script.Finder;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.recorder.RecordedEventsFlow;
import org.sikuli.script.support.recorder.actions.IRecordedAction;

public class Recorder implements NativeKeyListener, NativeMouseListener, NativeMouseMotionListener {

  private static final int SCREENSHOT_THROTTLE_MILLIS = 200;

  private RecordedEventsFlow eventsFlow = new RecordedEventsFlow();
  private File screenshotDir;

  private boolean running = false;

  ScreenImage currentImage = null;

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

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });

  }

  private static final class Throttler {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long lastExecution = 0;

    public void execute(Runnable runnable, long threshold, TimeUnit unit) {
      synchronized (this) {
        long now = System.currentTimeMillis();

        if (now - lastExecution > threshold) {
          lastExecution = now;
          executor.execute(runnable);
        }
      }
    }
  }

  private final Throttler THROTTLER = new Throttler();

  private final Runnable SCREENSHOT_RUNNABLE = new Runnable() {
    @Override
    public void run() {
      synchronized (screenshotDir) {
        if (screenshotDir.exists()) {
          ScreenImage img = Screen.getPrimaryScreen().capture();
          if(new Finder(img).findDiffPercentage(currentImage) > 0.0001) {
            currentImage = img;
            String imageFilePath = currentImage.save(screenshotDir.getAbsolutePath());
            eventsFlow.addScreenshot(imageFilePath);
          }
        }
      }
    }
  };

  private void screenshot() {
    THROTTLER.execute(SCREENSHOT_RUNNABLE, SCREENSHOT_THROTTLE_MILLIS, TimeUnit.MILLISECONDS);
  }

  public void add(NativeInputEvent e) {
    eventsFlow.addEvent(e);
    screenshot();
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
      currentImage = null;

      try {
        screenshotDir = Files.createTempDirectory("sikulix").toFile();
        screenshotDir.deleteOnExit();
      } catch (IOException e) {
        e.printStackTrace();
      }

      screenshot();

      GlobalScreen.addNativeKeyListener(this);
      GlobalScreen.addNativeMouseListener(this);
      GlobalScreen.addNativeMouseMotionListener(this);
    }

  }

  public List<IRecordedAction> stop(ProgressMonitor progress) {
    if (running) {
      running = false;
      GlobalScreen.removeNativeMouseMotionListener(this);
      GlobalScreen.removeNativeMouseListener(this);
      GlobalScreen.removeNativeKeyListener(this);

      synchronized (screenshotDir) {
        List<IRecordedAction> actions = eventsFlow.compile(progress);

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