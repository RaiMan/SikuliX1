package org.sikuli.script.support.devices;

import org.sikuli.script.Image;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.support.RobotDesktop;
import org.sikuli.script.support.RunTime;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenDevice extends Devices {

  private static TYPE deviceType = TYPE.SCREEN;

  private GraphicsDevice gdev = null;

  public GraphicsDevice getDevice() {
    return gdev;
  }

  private RobotDesktop robot = null;

  public RobotDesktop getRobot() {
    return robot;
  }

  private int id = -1;

  static void start() {
    initDevices();
  }

  private ScreenDevice() {
  }

  private ScreenDevice(GraphicsDevice gdev) {
    this.gdev = gdev;
    try {
      robot = new RobotDesktop(gdev);
    } catch (AWTException e) {
      RunTime.terminate(999, "ScreenDevice: robot: %s", e.getMessage());
    }
  }

  private Rectangle getBounds() {
    return gdev.getDefaultConfiguration().getBounds();
  }

  public int x() {
    return getBounds().x;
  }

  public int y() {
    return getBounds().y;
  }

  public int h() {
    return getBounds().height;
  }

  public int height() {
    return getBounds().height;
  }

  public int w() {
    return getBounds().width;
  }

  public int width() {
    return getBounds().width;
  }

  public Point getCenter() {
    return new Point((int) getBounds().getCenterX(), (int) getBounds().getCenterY());
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
  private static ScreenDevice[] devices;

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
        final ScreenDevice device = new ScreenDevice(gdevs[i]);
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

  public static RobotDesktop getRobot(int id) {
    return get(id).getRobot();
  }

  public Rectangle asRectangle() {
    return getBounds();
  }

  public Region asRegion() {
    return new Region(getBounds());
  }

  public Image asImage() {
    BufferedImage bImg = robot.captureScreen(getBounds()).getImage();
    return new Image(bImg);
  }

  public String toString() {
    return String.format("S[%d,(%d,%d),(%d,%d)]", id, x(), y(), w(), h());
  }
}
