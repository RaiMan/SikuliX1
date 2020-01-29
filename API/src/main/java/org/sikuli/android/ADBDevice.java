/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.android;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.ScreenImage;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADBDevice {

/*
  static {
    RunTime.loadLibrary(RunTime.libOpenCV);
  }
*/

  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "ADBDevice: " + message, args);
  }

  private JadbDevice device = null;
  private int devW = -1;
  private int devH = -1;
  private ADBRobot robot = null;
  private ADBScreen screen = null;
  private boolean isMulti = false;

  private List<String> deviceProps = new ArrayList<>();
  private int deviceVersion = -1;
  private String sDeviceVersion = "???";


  private static ADBDevice adbDevice = null;
  private String adbExec = "";


  public static int KEY_HOME = 3;
  public static int KEY_BACK = 4;
  public static int KEY_MENU = 82;
  public static int KEY_POWER = 26;

  private ADBDevice() {
  }

  public static ADBDevice init() {
    return init("");
  }

  public static ADBDevice init(String adbExec) {
    if (adbDevice == null) {
      adbDevice = new ADBDevice();
      adbDevice.device = ADBClient.getDevice(adbExec);
      if (adbDevice.device == null) {
        adbDevice = null;
      } else {
        adbDevice.initDevice(adbDevice);
        adbDevice.adbExec = ADBClient.getADB();
        RunTime.loadLibrary(RunTime.libOpenCV);
      }
    }
    return adbDevice;
  }

  public static ADBDevice init(int id) {
      ADBDevice adbDevice = new ADBDevice();
      adbDevice.device = ADBClient.getDevice(id);
      if (adbDevice.device == null) {
        return null;
      } else {
        adbDevice.initDevice(adbDevice);
        adbDevice.adbExec = ADBClient.getADB();
        RunTime.loadLibrary(RunTime.libOpenCV);
      }
    return adbDevice;
  }

  private void initDevice(ADBDevice device) {
    device.deviceProps = Arrays.asList(device.exec("getprop").split("\n"));
    //[ro.build.version.release]: [6.0.1]
    //[ro.product.brand]: [google]
    //[ro.product.manufacturer]: [asus]
    //[ro.product.model]: [Nexus 7]
    //[ro.product.name]: [razor]
    //[ro.serialno]: [094da986]
    Pattern pProp = Pattern.compile("\\[(.*?)\\]:.*?\\[(.*)\\]");
    Matcher mProp = null;
    String val = "";
    String key = "";
    for (String prop : device.deviceProps) {
      if (!prop.startsWith("[ro.")) continue;
      mProp = pProp.matcher(prop);
      if (mProp.find()) {
        key = mProp.group(1);
        if (key.contains("build.version.release")) {
          val = mProp.group(2);
          try {
            device.deviceVersion = Integer.parseInt(val.split("\\.")[0]);
            device.sDeviceVersion = val;
          } catch (Exception e) {
          }
        }
      }
    }
    log(lvl, "init: %s", device.toString());
  }

  public static void reset() {
    adbDevice = null;
    ADBClient.reset();
  }

  public String toString() {
    return String.format("attached device: serial(%s) display(%dx%d) version(%s)",
            getDeviceSerial(), getBounds().width, getBounds().height, sDeviceVersion);
  }

  public ADBRobot getRobot(ADBScreen screen) {
    if (robot == null) {
      this.screen = screen;
      robot = new ADBRobot(screen, this);
    }
    return robot;
  }

  public String getDeviceSerial() {
    return device.getSerial();
  }

  public Rectangle getBounds() {
    if (devW < 0) {
      Dimension dim = getDisplayDimension();
      devW = (int) dim.getWidth();
      devH = (int) dim.getHeight();
    }
    return new Rectangle(0, 0, devW, devH);
  }

  public ScreenImage captureScreen() {
    BufferedImage bimg = captureDeviceScreen();
    return new ScreenImage(getBounds(), bimg);
  }

  public ScreenImage captureScreen(Rectangle rect) {
    BufferedImage bimg = captureDeviceScreen(rect.x, rect.y, rect.width, rect.height);
    return new ScreenImage(rect, bimg);
  }

  public BufferedImage captureDeviceScreen() {
    return captureDeviceScreen(0, 0, -1, -1);
  }

  public BufferedImage captureDeviceScreen(int x, int y, int w, int h) {
    Mat matImage = captureDeviceScreenMat(x, y, w, h);
    BufferedImage bImage = null;
    if (matImage != null) {
      bImage = new BufferedImage(matImage.width(), matImage.height(), BufferedImage.TYPE_3BYTE_BGR);
      byte[] bImageData = ((DataBufferByte) bImage.getRaster().getDataBuffer()).getData();
      matImage.get(0, 0, bImageData);
    }
    return bImage;
  }

  public Mat captureDeviceScreenMat(int x, int y, int actW, int actH) {
    log(lvl, "captureDeviceScreenMat: enter: [%d,%d %dx%d]", x, y, actW, actH);
    byte[] imagePrefix = new byte[12];
    byte[] image = new byte[0];
    boolean isfullScreen = false;
    if (x == 0 && y == 0 && actW < 0 && actH < 0) {
      isfullScreen = true;
    }
    int currentW;
    int currentH;
    int channels = 4;
    Mat matImage = new Mat();
    try (InputStream deviceOut = device.execute("screencap")) {
      Debug timer = Debug.startTimer();
      while (deviceOut.available() < 12) ;
      deviceOut.read(imagePrefix);
      if (imagePrefix[8] != 0x01) {
        log(-1, "captureDeviceScreenMat: image type not RGBA");
        return null;
      }
      currentW = byte2int(imagePrefix, 0, 4);
      currentH = byte2int(imagePrefix, 4, 4);
      if (!((currentW == devW && currentH == devH) || (currentH == devW && currentW == devH))) {
        log(-1, "captureDeviceScreenMat: width or height differ from device values");
        return null;
      }
      if (isfullScreen) {
        actW = currentW;
        actH = currentH;
      } else {
        if (x + actW > currentW) {
          actW = currentW - x;
        }
        if (y + actH > currentH) {
          actH = currentH - y;
        }
      }
      long duration = timer.lap("");
      int nPixels = actW * actH;
      image = new byte[nPixels * channels];
      int atImage = 0;
      boolean endOfStream = false;
      int maxR = y + actH;
      int maxC = x + actW;
      byte[] pixel = new byte[channels];
      int pixelByte = -1;
      while (true) {
        for (int npr = 0; npr < maxR; npr++) {
          for (int npc = 0; npc < currentW; npc++) {
            //byte[] pixel = deviceOut.readNBytes(4);
            for (int np = 0; np < channels; np++) {
              pixelByte = deviceOut.read();
              if (pixelByte > -1) {
                pixel[np] = (byte) pixelByte;
              } else {
                endOfStream = true;
              }
            }
            if (!endOfStream) {
              if (pixel[3] == -1) {
                if (npr >= y && npc >= x && npc < maxC) {
                  image[atImage++] = pixel[0];
                  image[atImage++] = pixel[1];
                  image[atImage++] = pixel[2];
                  image[atImage++] = pixel[3];
                }
              } else {
                log(-1, "buffer problem: %d", nPixels);
                return null;
              }
            } else {
              break;
            }
          }
          if (endOfStream) {
            break;
          }
        }
        break;
      }
      Mat matOrg = new Mat(actH, actW, CvType.CV_8UC4);
      matOrg.put(0, 0, image);
      List<Mat> matsOrg = new ArrayList<Mat>();
      Core.split(matOrg, matsOrg);
      matsOrg.remove(3);
      List<Mat> matsImage = new ArrayList<Mat>();
      matsImage.add(matsOrg.get(2));
      matsImage.add(matsOrg.get(1));
      matsImage.add(matsOrg.get(0));
      Core.merge(matsImage, matImage);
      log(lvl, "captureDeviceScreenMat: exit: [%d,%d %dx%d] %d (%d)",
              x, y, actW, actH, duration, timer.end());
    } catch (Exception e) {
      log(-1, "captureDeviceScreenMat: [%d,%d %dx%d] %s", x, y, actW, actH, e);
    }
    return matImage;
  }

  private int byte2int(byte[] bytes, int start, int len) {
    int val = 0;
    int fact = 1;
    for (int i = start; i < start + len; i++) {
      int b = bytes[i] & 0xff;
      val += b * fact;
      fact *= 256;
    }
    return val;
  }

  private InputStream execADB(String... args) {
    if (args.length < 1) {
      return null;
    }
    ProcessBuilder app = new ProcessBuilder();
    List<String> cmd = new ArrayList<String>();
    Map<String, String> processEnv = app.environment();
    cmd.add(adbExec);
    for (String arg : args) {
      cmd.add(arg);
    }
    app.directory(null);
    app.redirectErrorStream(false);
    app.command(cmd);
    Process process = null;
    try {
      process = app.start();
      return process.getInputStream();
    } catch (IOException e) {
      log(-1, "execADB: %s (%s)", cmd, e);
    }
    return null;
  }

  private Dimension getDisplayDimension() {
    String dump = dumpsys("display");
    String token = "mDefaultViewport= ... deviceWidth=1200, deviceHeight=1920}";
    Dimension dim = null;
    Pattern displayDimension = Pattern.compile(
            "mDefaultViewport.*?=.*?deviceWidth=(\\d*).*?deviceHeight=(\\d*)");
    Matcher match = displayDimension.matcher(dump);
    if (match.find()) {
      int w = Integer.parseInt(match.group(1));
      int h = Integer.parseInt(match.group(2));
      dim = new Dimension(w, h);
    } else {
      log(-1, "getDisplayDimension: dumpsys display: token not found: %s", token);
    }
    return dim;
  }

  public String exec(String command, String... args) {
    String out = "";
    try (InputStream stdout = device.executeShell(command, args)) {
      out = inputStreamToString(stdout, "UTF-8");
    } catch (IOException | JadbException e) {
      log(-1, "exec: %s: %s", command, e);
      return null;
    }
    return out;
  }

  public String dumpsys(String component) {
    String out = "";
    if (component == null || component.isEmpty()) {
      component = "power";
    }
    try (InputStream stdout = component.toLowerCase().contains("all") ?
            device.executeShell("dumpsys") :
            device.executeShell("dumpsys", component)) {
      out = inputStreamToString(stdout, "UTF-8");
    } catch (IOException | JadbException e) {
      log(-1, "dumpsys: %s: %s", component, e);
    }
    return out;
  }

  public String printDump(String component) {
    String dump = dumpsys(component);
    if (!dump.isEmpty()) {
      System.out.println("***** Android device dump: " + component);
      System.out.println(dump);
    }
    return dump;
  }

  public String printDump() {
    String dump = dumpsys("all");
    if (!dump.isEmpty()) {
      File out = new File(RunTime.get().fSikulixStore, "android_dump_" + getDeviceSerial() + ".txt");
      System.out.println("***** Android device dump all services");
      System.out.println("written to file: " + out.getAbsolutePath());
      FileManager.writeStringToFile(dump, out);
    }
    return dump;
  }

  private static final int BUFFER_SIZE = 4 * 1024;

  private static String inputStreamToString(InputStream inputStream, String charsetName) {
    StringBuilder builder = new StringBuilder();
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(inputStream, charsetName);
      char[] buffer = new char[BUFFER_SIZE];
      int length;
      while ((length = reader.read(buffer)) != -1) {
        builder.append(buffer, 0, length);
      }
      return builder.toString();
    } catch (Exception e) {
      return "";
    }
  }

  public void wakeUp(int seconds) {
    int times = seconds * 4;
    try {
      if (null == isDisplayOn()) {
        log(-1, "wakeUp: not possible - see log");
        return;
      }
      //device.executeShell("input", "keyevent", "224");
      inputKeyEvent(224);
      while (0 < times--) {
        if (isDisplayOn()) {
          return;
        } else {
          RunTime.pause(0.25f);
        }
      }
    } catch (Exception e) {
      log(-1, "wakeUp: did not work: %s", e);
    }
    log(-1, "wakeUp: timeout: %d seconds", seconds);
  }

  public Boolean isDisplayOn() {
    // deviceidle | grep mScreenOn=true|false
    // v < 5: power | grep mScreenOn=true|false
    // v > 4: power | grep Display Power: state=ON|OFF
    String dump = dumpsys("power");
    Pattern displayOn = Pattern.compile("mScreenOn=(..)");
    String isOn = "tr";
    if (deviceVersion > 4) {
      displayOn = Pattern.compile("Display Power: state=(..)");
      isOn = "ON";
    }
    Matcher match = displayOn.matcher(dump);
    if (match.find()) {
      if (match.group(1).contains(isOn)) {
        return true;
      }
      return false;
    } else {
      log(-1, "isDisplayOn: (Android version %d) dumpsys power: pattern not found: %s", deviceVersion, displayOn);
    }
    return null;
  }

  public void inputKeyEvent(int key) {
    try {
      device.executeShell("input", "keyevent", Integer.toString(key));
    } catch (Exception e) {
      log(-1, "inputKeyEvent: %d did not work: %s", e.getMessage());
    }
  }

  public void tap(int x, int y) {
    try {
      device.executeShell("input tap", Integer.toString(x), Integer.toString(y));
    } catch (IOException | JadbException e) {
      log(-1, "tap: %s", e);
    }
  }

  public void swipe(int x1, int y1, int x2, int y2) {
    try {
      device.executeShell("input swipe", Integer.toString(x1), Integer.toString(y1),
              Integer.toString(x2), Integer.toString(y2));
    } catch (IOException | JadbException e) {
      log(-1, "swipe: %s", e);
    }
  }

  private String textBuffer = "";
  private boolean typing = false;

  public synchronized boolean typeStarts() {
    if (!typing) {
      textBuffer = "";
      typing = true;
      return true;
    }
    return false;
  }

  public synchronized void typeEnds() {
    if (typing) {
      input(textBuffer);
      typing = false;
    }
  }

  public void typeChar(char character) {
    if (typing) {
      textBuffer += character;
    }
  }

  public static float inputDelay = 0.05f;

  public void input(String text) {
    try {
      device.executeShell("input text ", text);
      RunTime.pause(text.length() * inputDelay);
    } catch (Exception e) {
      log(-1, "input: %s", e);
    }
  }
}
