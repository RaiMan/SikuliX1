/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.Image;
import org.sikuli.script.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;

public class SXTest {

  public String methodName = "NotValid";
  public RunTime runTime = RunTime.get();
  public String bundlePath = null;
  public static boolean showImage = true;
  public static long start = 0;
  public static String duration;

  public static long setUpTime = 0;
  public static JFrame defaultFrame = null;
  public static String defaultFrameImage = "";
  public static long waitBefore = 0;
  public static long waitAfter = 0;
  public static Dimension defaultFrameSize = null;

  public static String testBase = "house_shot";
  public static String testChanged = "house_shot_changed";
  public static String testBaseX2 = "house_shot_mac";
  public static String testName = "house1";
  public static String testNameX2 = "house1_mac";
  public static String testNameTrans = "houseTx";
  public static String testNameMask = "houseTm";
  public static String testMissingName = "missing";
  public static String httpURI = "https://sikulix-2014.readthedocs.io/en/latest/_images/popup.png";

  static {
    Settings.ActionLogs = false;
  }

  public Region defaultRegion = new Screen();
  public static boolean useScreen = true;

  public Region getDefaultRegion() {
    if (useScreen) {
      defaultRegion = new Screen();
    } else {
      defaultRegion = new Region(defaultFrameSize);
    }
    return defaultRegion;
  }

  public void showCV(Image image) {
    showCV(methodName, image);
  }

  public void showCV(String title, Image image) {
    if (!showImage || !image.isValid()) {
      return;
    }
    HighGui.namedWindow(title);
    Screen screen = Screen.getPrimaryScreen();
    int x = (screen.w - image.w) / 2;
    int y = (screen.h - image.h) / 2;
    HighGui.moveWindow(title, x, y);
    HighGui.imshow(title, image.getContent());
    HighGui.waitKey(3000);
  }

  public static void showCV(Mat image) {
    String title = "show Mat";
    HighGui.namedWindow(title);
    Screen screen = Screen.getPrimaryScreen();
    int x = (screen.w - image.width()) / 2;
    int y = (screen.h - image.height()) / 2;
    HighGui.moveWindow(title, x, y);
    HighGui.imshow(title, image);
    HighGui.waitKey(3000);
  }

  public JFrame createFrame(JFrame givenFrame, String imageName) {
    if (null != givenFrame) {
      givenFrame.dispose();
    }
    JFrame frame = new JFrame();
    frame.setAlwaysOnTop(true);
    frame.setUndecorated(true);
    ImageIcon image = new ImageIcon(new File(bundlePath, imageName + ".png").getAbsolutePath());
    Dimension size = new Dimension(image.getIconWidth(), image.getIconHeight());
    defaultFrameSize = size;
    frame.setPreferredSize(size);
    frame.add(new JLabel(image));
    frame.pack();
    return frame;
  }

  public ObserverCallBack createObserverCallBack() {
    return new ObserverCallBack() {
      @Override
      public void findfailed(ObserveEvent event) {
        event.setResponse(FindFailedResponse.ABORT);
      }

      @Override
      public void missing(ObserveEvent event) {
        event.setResponse(FindFailedResponse.ABORT);
      }
    };
  }

  public void setUpBase() {
    Settings.NewAPI = true;
    Settings.AlwaysResize = 1;
    Settings.CheckLastSeen = false;
    if (null == bundlePath) {
      setUpTime = new Date().getTime();
      File bundleFile = new File(runTime.fWorkDir, "src/main/resources/images");
      ImagePath.setBundleFolder(bundleFile);
      bundlePath = ImagePath.getBundlePath();
      setUpTime = new Date().getTime() - setUpTime;
    }
  }

  public void startUpBase() {
    if (!RunTime.isHeadless()) {
      Region region = new Region(100, 200, 300, 400);
      Image image = new Image(region);
    }
    useScreen = false;
    showImage = false;
    Settings.ProfileLogs = false;
  }

  public JFrame testIntro(Object... args) {
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    if (args.length > 0 && args[0] instanceof String) {
      int pause = 1;
      String imageName = (String) args[0];
      if (null == defaultFrame || !imageName.equals(defaultFrameImage)) {
        if (null != defaultFrame) {
          defaultFrame.dispose();
        }
        defaultFrameImage = (String) args[0];
        defaultFrame = createFrame(defaultFrame, defaultFrameImage);
        defaultRegion = new Region(defaultFrameSize);
        pause = 2;
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          RunTime.pause(waitBefore);
          if (null != defaultFrame) {
            defaultFrame.setVisible(true);
          }
          if (waitAfter > 0) {
            RunTime.pause(waitAfter);
            defaultFrame.setVisible(false);
          }
        }
      }).start();
      RunTime.pause(pause);
    }
    start = new Date().getTime();
    return defaultFrame;
  }

  public void testOutro(String message, Object... args) {
    duration = String.format("%5d", new Date().getTime() - start);
    if (defaultFrame != null) {
      defaultFrame.setVisible(false);
      RunTime.pause(1);
    }
    waitBefore = 0;
    waitAfter = 0;
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    if (args.length > 0 && args[0] instanceof Image) {
      showCV((Image) args[0]);
    }
    Debug.logp(duration + " " + methodName + ": " + message, args);
  }

  public boolean checkMatch(Match match, double score) {
    return match != null && match.score() > score;
  }

  public String makeImageFileName(String name, boolean delete) {
    File file = new File(bundlePath, Element.getValidImageFilename(name));
    if (delete) FileManager.deleteFileOrFolder(file);
    return file.getAbsolutePath();
  }

  public String copyImageFileName(String from, String to) {
    File fileFrom = new File(bundlePath, Element.getValidImageFilename(from));
    File fileto = new File(bundlePath, Element.getValidImageFilename(to));
    FileManager.xcopy(fileFrom, fileto);
    return Element.getValidImageFilename(to);
  }
}
