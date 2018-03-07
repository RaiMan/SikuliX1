/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.vnc;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.util.OverlayCapturePrompt;
import org.sikuli.util.ScreenHighlighter;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sikulix.vnc.*;

public class VNCScreen extends Region implements IScreen, Closeable {
  private final VNCClient client;
  private volatile boolean closed;
  private final IRobot robot;
  private ScreenImage lastScreenImage;

  private static Map<VNCScreen, VNCClient> screens = new HashMap<>();

//TODO Java9 on Mac
//public class SocketDescriptor implements FileDescriptor {
//  private static SelectorProvider DefaultSelectorProvider() {
//      protected SelectorProvider() {
//        this(checkPermission());
//    }
//
//    private static boolean loadProviderFromProperty() {

  public static VNCScreen start(String theIP, int thePort, String password, int cTimeout, int timeout) throws IOException {
    VNCScreen scr = null;
    if (RunTime.get().runningMac && !RunTime.get().isJava9("VNCScreen not yet working on Mac")) {
      scr = new VNCScreen(VNCClient.connect(theIP, thePort, password, true));
      screens.put(scr, scr.client);
    }
    return scr;
  }

  public static VNCScreen start(String theIP, int thePort, int cTimeout, int timeout) throws IOException {
    VNCScreen scr = new VNCScreen(VNCClient.connect(theIP, thePort, null, true));
    screens.put(scr, scr.client);
    return scr;
  }

  public static VNCScreen start(String theIP, int thePort) throws IOException {
    return start(theIP, thePort, 0, 0);
  }

  public void stop() {
    try {
      close();
    } catch (IOException e) {
      Debug.error("VNCScreen: stop: %s", e.getMessage());
    }
    screens.put(this, null);
  }

  public static void stopAll() {
    for (VNCScreen scr : screens.keySet()) {
      if (screens.get(scr) == null) {
        continue;
      }
      try {
        scr.close();
      } catch (IOException e) {
        Debug.error("VNCScreen: stopAll: %s", e.getMessage());
      }
    }
    screens.clear();
  }

  private VNCScreen(final VNCClient client) {
    this.client = client;
    this.robot = new VNCRobot(this);
    setOtherScreen(this);
    setRect(getBounds());
    initScreen(this);
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.processMessages();
        } catch (RuntimeException e) {
          if (!closed) {
            throw e;
          }
        }
      }
    }).start();
    client.refreshFramebuffer();
    //RunTime.get().pause(5);
  }

  @Override
  public void close() throws IOException {
    closed = true;
    client.close();
    screens.put(this, null);
  }

  @Override
  public IRobot getRobot() {
    return robot;
  }

  @Override
  public Rectangle getBounds() {
    return client.getBounds();
  }

  @Override
  public ScreenImage capture() {
    return capture(getBounds());
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
  public ScreenImage capture(int x, int y, int w, int h) {
    BufferedImage image = client.getFrameBuffer(x, y, w, h);
    ScreenImage img = new ScreenImage(
            new Rectangle(x, y, w, h),
            image
    );
    lastScreenImage = img;
    return img;
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

  @Override
  public int getIdFromPoint(int srcx, int srcy) {
    return 0;
  }

  @Override
  protected <PSIMRL> Location getLocationFromTarget(PSIMRL target) throws FindFailed {
    Location location = super.getLocationFromTarget(target);
    if (location != null) {
      location.setOtherScreen(this);
    }
    return location;
  }

  @Override
  public ScreenImage getLastScreenImageFromScreen() {
    return lastScreenImage;
  }

  @Override
  public ScreenImage userCapture(final String msg) {
    if (robot == null) {
      return null;
    }

    final OverlayCapturePrompt prompt = new OverlayCapturePrompt(this);

    Thread th = new Thread() {
      @Override
      public void run() {
        prompt.prompt(msg);
      }
    };

    th.start();

    boolean hasShot = false;
    ScreenImage simg = null;
    int count = 0;
    while (!hasShot) {
      this.wait(0.1f);
      if (count++ > 300) {
        break;
      }
      if (prompt.isComplete()) {
        simg = prompt.getSelection();
        if (simg != null) {
          lastScreenImage = simg;
        }
        hasShot = true;
        prompt.close();
      }
    }
    if (!hasShot) {
      prompt.close();
    }

    return simg;
  }

  public VNCClient getClient() {
    return client;
  }

  public Region set(Region element) {
    return setOther(element);
  }

  public Location set(Location element) {
    return setOther(element);
  }

  public Region setOther(Region element) {
    element.setOtherScreen(this);
    return element;
  }

  public Location setOther(Location element) {
    element.setOtherScreen(this);
    return element;
  }

  public Location newLocation(int x, int y) {
    Location loc = new Location(x, y);
    loc.setOtherScreen(this);
    return loc;
  }

  public Location newLocation(Location loc) {
    return newLocation(loc.x, loc.y);
  }

  public Region newRegion(int x, int y, int w, int h) {
    Region reg = Region.create(x, y, w, h);
    reg.setOtherScreen(this);
    return reg;
  }

  public Region newRegion(Location loc, int w, int h) {
    return newRegion(loc.x, loc.y, w, h);
  }

  public Region newRegion(Region reg) {
    return newRegion(reg.x, reg.y, reg.w, reg.h);
  }
}
