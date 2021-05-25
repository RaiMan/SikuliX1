package org.sikuli.script.support.devices;

import org.sikuli.script.Image;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.support.IRobot;
import org.sikuli.script.support.RobotDesktop;
import org.sikuli.script.support.RunTime;
import org.sikuli.util.OverlayCapturePrompt;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenDevice extends Devices {

  private static TYPE deviceType = TYPE.SCREEN;

  private Rectangle bounds = null;

  private int id = -1;

  public int getId() {
    return id;
  }

  static void start() {
    initDevices();
  }

  private ScreenDevice() {
  }

  private ScreenDevice(Rectangle bounds) {
    this.bounds = bounds;
  }

  public int x() {
    return bounds.x;
  }

  public int y() {
    return bounds.y;
  }

  public int h() {
    return bounds.height;
  }

  public int height() {
    return bounds.height;
  }

  public int w() {
    return bounds.width;
  }

  public int width() {
    return bounds.width;
  }

  public Point getCenter() {
    return new Point((int) bounds.getCenterX(), (int) bounds.getCenterY());
  }

  public static Screen makeScreen(int num) {
    final Screen screen = new Screen();
    screen.setup(num);
    return screen;
  }

  public static boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }

  private static int mainMonitor = -1;
  private static int nDevices;
  private static ScreenDevice[] devices = null;

  private static void initDevices() {
    if (mainMonitor > -1) {
      return;
    }
    if (!isHeadless()) {
      log(deviceType, 4, "Accessing: GraphicsEnvironment.getLocalGraphicsEnvironment()");
      GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      log(deviceType, 4, "Accessing: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()");
      GraphicsDevice[] gdevs = genv.getScreenDevices();
      nDevices = gdevs.length;
      if (nDevices == 0) {
        RunTime.terminate(999, "GraphicsEnvironment: running not possibel: no ScreenDevices");
      }
      devices = new ScreenDevice[nDevices];
      Rectangle currentBounds;
      for (int i = 0; i < nDevices; i++) {
        String addOn = "";
        currentBounds = gdevs[i].getDefaultConfiguration().getBounds();
        if (currentBounds.contains(new Point(0, 0))) {
          if (mainMonitor < 0) {
            mainMonitor = i;
            addOn = " (is primary screen)";
          } else {
            addOn = " (has (0,0) too!";
          }
        }
        final ScreenDevice device = new ScreenDevice(currentBounds);
        device.id = i;
        devices[i] = device;
        log(deviceType, 3,"%s" + addOn, device);
      }
      if (mainMonitor < 0) {
        log(deviceType, 3, "No ScreenDevice has (0,0) --- using 0 as primary: %s", devices[0]);
        mainMonitor = 0;
      }
    } else {
      RunTime.terminate(999, "GraphicsEnvironment: running not possible: is headless");
    }
  }

  public static int numDevices() {
    return nDevices;
  }

  public static ScreenDevice primary() {
    return get(mainMonitor);
  }

  public static Screen getPrimaryScreen() {
    return makeScreen(mainMonitor);
  }

  public static ScreenDevice[] get() {
    if (devices == null) {
      initDevices();
    }
    return devices;
  }

  public static ScreenDevice get(int n) {
    if (devices == null) {
      initDevices();
    }
    ScreenDevice device = devices[0];
    if (n > 0 && n < nDevices) {
      device = devices[n];
    }
    return device;
  }

  public static ScreenDevice getScreenForPoint(Point point) {
    for (ScreenDevice scr : get()) {
      if (scr.bounds.contains(point)) {
        return scr;
      }
    }
    return primary();
  }

  public Rectangle asRectangle() {
    return bounds;
  }

  public Region asRegion() {
    return new Region(bounds);
  }

  public Image asImage() {
    //TODO ScreenDevice.asImage: capture
    BufferedImage bImg = null;
    return new Image(bImg);
  }

  private static Robot robot;

  public static Robot getRobot() {
    if (robot == null) {
      try {
        robot = new Robot();
      } catch (AWTException e) {
        throw new RuntimeException(String.format("ScreenDevice: getRobot: %s", e.getMessage()));
      }
    }
    return robot;
  }


  public BufferedImage capture() {
    return getRobot().createScreenCapture(asRectangle());
  }

  private static AtomicBoolean capturePromptActive = new AtomicBoolean(false);

  public static boolean capturePromptActive() {
    synchronized (capturePromptActive) {
      boolean state = capturePromptActive.get();
      if (state == false) {
        capturePromptActive.set(true);
      }
      return state;
    }
  }

  public static void capturePromptClosed() {
    capturePromptActive.set(false);
  }

  private OverlayCapturePrompt prompt = null;

  public OverlayCapturePrompt getPrompt() {
    return prompt;
  }

  public void setPrompt(OverlayCapturePrompt prompt) {
    this.prompt = prompt;
  }

  public void showPrompt() {
    if (prompt != null) {
      prompt.prompt();
    }
  }

  public void closePrompt() {
    if (prompt != null) {
      prompt.close();
      prompt = null;
    }
  }

  public boolean hasPrompt() {
    return prompt != null;
  }

  public static void closeCapturePrompts() {
    for (ScreenDevice screen : ScreenDevice.get()) {
      screen.closePrompt();
    }
    capturePromptClosed();
  }

  public static void closeOtherCapturePrompts(ScreenDevice screen) {
    for (ScreenDevice otherScreen : ScreenDevice.get()) {
      if (otherScreen.equals(screen)) {
        continue;
      }
      otherScreen.closePrompt();
    }
  }

  public String toString() {
    return String.format("S[%d,(%d,%d),(%d,%d)]", id, x(), y(), w(), h());
  }
}
