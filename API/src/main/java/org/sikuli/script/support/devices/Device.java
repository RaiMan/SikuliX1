package org.sikuli.script.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Device {

  public enum TYPE {VNC, ANDROID, SCREEN, MOUSE, KEYBOARD}

  private static TYPE deviceType = null;

  static void log(TYPE type, int level, String message, Object... args) {
    if (Debug.is(level)) {
      Debug.logx(level, type + "-DEVICE: " + message, args);
    }
  }

  private static Robot globalRobot = null;

  protected static Robot getMouseRobot() {
    if (globalRobot == null) {
      try {
        globalRobot = new Robot();
      } catch (AWTException e) {
        Commons.terminate(999, String.format("SikuliX: Devices: AWT.Robot: %s", e.getMessage()));
      }
    }
    return globalRobot;
  }

  public static void start(TYPE type, Map<String, Object> options) {
    if (type.equals(TYPE.SCREEN)) {
      ScreenDevice.start();
    } else if (type.equals(TYPE.MOUSE)) {
      MouseDevice.start();
    }
  }

  public static void start(TYPE type) {
    start(type, null);
  }

  public Device init(Map<String, Object> options) {
    //TODO must be overwritten
    Debug.error("stop: not implemented for %s", deviceType);
    return null;
  }

  public void stop() {
    //TODO must be overwritten
    Debug.error("stop: not implemented for %s", deviceType);
  }

  public static void checkAccessibility() {
    start(TYPE.SCREEN);
    //check Mouse
    while(MouseDevice.isMoving()) {
      Commons.pause(1.0);
    }
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          start(TYPE.MOUSE);
        } catch (Exception e) {
          Debug.error("Start MouseDevice: %s", e.getMessage());
        }
      }
    });
    long mouseCheckWait = 500;
    long duration = 0;
    if (Commons.runningMac()) {
      //macOS: check Screen capture
      long start = new Date().getTime();
      start(TYPE.SCREEN);
      Rectangle srect = ScreenDevice.getPrimary().asRectangle();
      BufferedImage screenImage = ScreenDevice.getRobot(0).captureScreen(srect).getImage(); // checkAccessibility
      DataBuffer data = screenImage.getData(new Rectangle(200, 10, srect.width - 200, 1)).getDataBuffer();
      int min = data.getElem(0);
      int max = data.getElem(0);
      for (int n = 0; n < data.getSize(); n++) {
        min = Math.min(min, data.getElem(n));
        max = Math.max(max, data.getElem(n));
      }
      Color cmin = new Color(min);
      Color cmax = new Color(max);
      boolean singleColor =
          (Math.abs(cmax.getRed() - cmin.getRed()) < 2) &&
              (Math.abs(cmax.getGreen() - cmin.getGreen()) < 2) &&
              (Math.abs(cmax.getBlue() - cmin.getBlue()) < 2);
      if (singleColor) {
        ScreenDevice.isUseable(false);
      }
      duration = new Date().getTime() - start;
    }
    Commons.pause(Math.max(1, (mouseCheckWait - duration)) / 1000.0); //Windows: wait for threaded Mouse check
    executorService.shutdown();
  }

  public static boolean isCaptureBlocked() { //TODO
    return !ScreenDevice.isUseable();
  }
}
