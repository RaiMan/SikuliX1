package org.sikuli.script.support.devices;

import org.sikuli.script.Image;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.support.RunTime;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class ScreenDevice extends Devices {

  private static TYPE deviceType = TYPE.SCREEN;

  private Rectangle bounds = null;
  private int id = -1;

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

  private static Screen primaryScreen = null;

  public static Screen getPrimaryScreen() {
    if (primaryScreen == null) {
      start();
      primaryScreen = makeScreen(mainMonitor);
    }
    return primaryScreen;
  }

  public static ScreenDevice[] get() {
    return devices;
  }

  public static ScreenDevice get(int n) {
    ScreenDevice device = devices[0];
    if (n > 0 && n < nDevices) {
      device = devices[n];
    }
    return device;
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

  public String toString() {
    return String.format("S[%d,(%d,%d),(%d,%d)]", id, x(), y(), w(), h());
  }
}
