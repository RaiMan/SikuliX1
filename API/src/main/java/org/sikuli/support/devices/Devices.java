package org.sikuli.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.support.RunTime;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Map;

public abstract class Devices {

  public enum TYPE {VNC, ANDROID, SCREEN, MOUSE, KEYBOARD}

  private static TYPE deviceType = null;

  static void log(TYPE type, int level, String message, Object... args) {
    if (Debug.is(level)) {
      Debug.logx(level, type + "Device: " + message, args);
    }
  }

  void log(int level, String message, Object... args) {
    if (Debug.is(level)) {
      Debug.logx(level, deviceType + "Device: " + message, args);
    }
  }

  private static Robot globalRobot = null;

  protected static Robot getGlobalRobot() {
    if (globalRobot == null) {
      try {
        globalRobot = new Robot();
      } catch (AWTException e) {
        RunTime.terminate(999, String.format("SikuliX: Devices: AWT.Robot: %s", e.getMessage()));
      }
    }
    return globalRobot;
  }

  public static void start(TYPE type, Map<String, Object> options) {
    if (type.equals(TYPE.SCREEN)) {
      ScreenDevice.start();
    }
  }

  public static void start(TYPE type) {
    start(type, null);
  }

  public Devices init(Map<String, Object> options) {
    //TODO must be overwritten
    Debug.error("init: not implemented for %s", deviceType);
    return null;
  }

  public void stop() {
    //TODO must be overwritten
    Debug.error("stop: not implemented for %s", deviceType);
  }


}
