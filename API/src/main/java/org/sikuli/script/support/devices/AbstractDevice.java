package org.sikuli.script.support.devices;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.RunTime;

import java.util.Map;

public abstract class AbstractDevice {

  public enum Device {VNC, ANDROID}

  public Device deviceType = null;

  public static AbstractDevice start(Device type, Map<String, Object> options) {
    //TODO must be overwritten
    terminate("stop: %s not implemented", type);
    return null;
  }

  public static void stop(Device type) {
    //TODO must be overwritten
    Debug.error("stop: %s not implemented", type);
  }

  public void stop() {
    //TODO must be overwritten
    Debug.error("stop: not implemented for %s", deviceType);
  }

  protected static void terminate(String message, Object... args) {
    RunTime.terminate(999, "AbstractDevice: " + message, args);
  }
}
