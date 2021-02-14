/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.script.support.IRobot;
import org.sikuli.script.support.IScreen;
import org.sikuli.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Törcsi on 2016. 06. 26.
 * Revised by RaiMan
 */

//TODO possible to use https://github.com/Genymobile/scrcpy?

public class ADBScreen extends Region implements EventObserver, IScreen {

  private static boolean isFake = false;
  protected IRobot robot = null;
  private static int logLvl = 3;
  private ScreenImage lastScreenImage = null;
  private Rectangle bounds;

  private boolean waitPrompt = false;
  protected OverlayCapturePrompt prompt;
  private String promptMsg = "Select a region on the screen";
  private static int waitForScreenshot = 300;

  public boolean needsUnLock = false;
  public int waitAfterAction = 1;

  //---------------------------Inits
  private ADBDevice device = null;
  private static ADBScreen screen = null;

  public static ADBScreen start() {
    return start("");
  }

  public static ADBScreen start(String adbExec) {
    if (screen == null) {
      try {
        screen = new ADBScreen(adbExec);
      } catch (Exception e) {
        Debug.log(-1, "ADBScreen: start: No devices attached");
        screen = null;
      }
    }
    return screen;
  }

  public static void stop() {
    if (null != screen) {
      Debug.log(3, "ADBScreen: stopping android support");
      ADBDevice.reset();
      screen = null;
    }
  }

  public ADBScreen() {
    this("");
  }

  public ADBScreen(String adbExec) {
    super();
    setOtherScreen(this);
    device = ADBDevice.init(adbExec);
    init();
  }

  public ADBScreen(int id) {
    super();
    setOtherScreen(this);
    device = ADBDevice.init(id);
    init();
  }

  private void init() {
    if (device != null) {
      robot = device.getRobot(this);
      robot.setAutoDelay(10);
      bounds = device.getBounds();
      w = bounds.width;
      h = bounds.height;
    }
  }

  public boolean isValid() {
    return null != device;
  }

  public ADBDevice getDevice() {
    return device;
  }

//  private ADBScreen getScreenWithDevice(int id) {
//    if (screen == null) {
//      log(-1, "getScreenWithDevice: Android support not started");
//      return null;
//    }
//    ADBScreen multiScreen = new ADBScreen(id);
//    if (!multiScreen.isValid()) {
//      log(-1, "getScreenWithDevice: no device with id = %d", id);
//      return null;
//    }
//    return multiScreen;
//  }

  public String toString() {
    if (null == device) {
      return "Android:INVALID";
    } else {
      return String.format("Android %s", getDeviceDescription());
    }
  }

  public String getDeviceDescription() {
    return String.format("%s (%d x %d)", device.getDeviceSerial(), bounds.width, bounds.height);
  }

  public void wakeUp(int seconds) {
    if (null == device) {
      return;
    }
    if (null == device.isDisplayOn()) {
      Debug.log(-1, "wakeUp: not possible - see log");
      return;
    }
    if (!device.isDisplayOn()) {
      device.wakeUp(seconds);
      if (needsUnLock) {
        aSwipeUp();
      }
    }
  }

  public String exec(String command, String... args) {
    if (device == null) {
      return null;
    }
    return device.exec(command, args);
  }

  //-----------------------------Overrides
  @Override
  public IScreen getScreen() {
    return this;
  }

  @Override
  public void update(EventSubject s) {
    waitPrompt = false;
  }

  @Override
  public IRobot getRobot() {
    return robot;
  }

  @Override
  public Rectangle getBounds() {
    return bounds;
  }

  @Override
  public ScreenImage capture() {
    return capture(x, y, -1, -1);
  }

  @Override
  public ScreenImage capture(int x, int y, int w, int h) {
    ScreenImage simg = null;
    if (device != null) {
      Debug.log(3, "ADBScreen.capture: (%d,%d) %dx%d", x, y, w, h);
      simg = device.captureScreen(new Rectangle(x, y, w, h));
    } else {
      Debug.log(-1, "capture: no ADBRobot available");
    }
    lastScreenImage = simg;
    return simg;
  }

  @Override
  public ScreenImage capture(Region reg) {
    return capture(reg.x, reg.y, reg.w, reg.h);
  }

  @Override
  public ScreenImage capture(Rectangle rect) {
    return capture(rect.x, rect.y, rect.width, rect.height);
  }

  @Override
  public int getID() {
    return 0;
  }

  public String getIDString() {
    return "Android " + getDeviceDescription();
  }

  @Override
  public ScreenImage getLastScreenImageFromScreen() {
    return lastScreenImage;
  }

  private EventObserver captureObserver = null;

  @Override
  public ScreenImage userCapture(final String msg) {
    if (robot == null) {
      return null;
    }
    waitPrompt = true;
    Thread th = new Thread() {
      @Override
      public void run() {
        prompt = new OverlayCapturePrompt(ADBScreen.this);
        prompt.prompt(msg);
      }
    };

    th.start();

    ScreenImage simg = null;
    int count = 0;
    while (true) {
      this.wait(0.1f);
      if (count++ > waitForScreenshot) {
        break;
      }
      if (prompt == null) {
        continue;
      }
      if (prompt.isComplete()) {
        simg = prompt.getSelection();
        if (simg != null) {
          lastScreenImage = simg;
        }
        break;
      }
    }
    if (null != prompt) {
      prompt.close();
      prompt = null;
    }
    return simg;
  }

  @Override
  public int getIdFromPoint(int srcx, int srcy) {
    return 0;
  }

  public Region set(Region element) {
    return setOther(element);
  }

  public Location set(Location element) {
    return setOther(element);
  }

  @Override
  public Region setOther(Region element) {
    return element.setOtherScreen(this);
  }

  @Override
  public Location setOther(Location element) {
    return element.setOtherScreen(this);
  }

  @Override
  public Region newRegion(Location loc, int width, int height) {
    return new Region(loc.x, loc.y, width, height, this);
  }

  @Override
  public Region newRegion(Region reg) {
    return new Region(reg).setOtherScreen(this);
  }

  @Override
  public Region newRegion(int _x, int _y, int width, int height) {
    return new Region(_x, _y, width, height, this);
  }

  @Override
  public Location newLocation(int _x, int _y) {
    return new Location(_x, _y).setOtherScreen(this);
  }

  @Override
  public Location newLocation(Location loc) {
    return new Location(loc).setOtherScreen(this);
  }
}
