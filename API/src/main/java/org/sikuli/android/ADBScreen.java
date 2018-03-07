/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.android;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.util.*;

import java.awt.*;

/**
 * Created by Törcsi on 2016. 06. 26.
 * Revised by RaiMan
 */
public class ADBScreen extends Region implements EventObserver, IScreen {

  static {
    RunTime.loadLibrary("VisionProxy");
  }

  private static String me = "ADBScreen: ";
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

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
    if (screen == null) {
      try {
        screen = new ADBScreen();
      } catch (Exception e) {
        log(-1, "start: No devices attached");
        screen = null;
      }
    }
    return screen;
  }

  public static void stop() {
      ADBDevice.reset();
      screen = null;
  }

  public ADBScreen() {
    super();
    setOtherScreen(this);

    device = ADBDevice.init();
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

  public String toString() {
    if (null == device) {
      return "ADBScreen: No Android device attached";
    } else {
      return String.format("ADBScreen: Android device: %s", getDeviceDescription());
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
      log(-1, "wakeUp: not possible - see log");
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
      log(logLvl, "ADBScreen.capture: (%d,%d) %dx%d", x, y, w, h);
      simg = device.captureScreen(new Rectangle(x, y, w, h));
    } else {
      log(-1, "capture: no ADBRobot available");
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

  public void showTarget(Location loc) {
    showTarget(loc, Settings.SlowMotionDelay);
  }

  protected void showTarget(Location loc, double secs) {
    if (Settings.isShowActions()) {
      ScreenHighlighter overlay = new ScreenHighlighter(this, null);
      overlay.showTarget(loc, (float) secs);
    }
  }

  @Override
  public int getID() {
    return 0;
  }

  public String getIDString() {
    return "Android";
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

    boolean hasShot = false;
    ScreenImage simg = null;
    int count = 0;
    while (!hasShot) {
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
          hasShot = true;
        }
        prompt.close();
      }
    }
    prompt.close();
    prompt = null;
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
