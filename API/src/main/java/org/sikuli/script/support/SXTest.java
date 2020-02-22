/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.opencv.highgui.HighGui;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.script.Image;

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

  public static long setUpTime = 0;
  public static JFrame defaultFrame = null;
  public static String defaultFrameImage = "";
  public static long waitBefore = 0;

  public static String testBase = "house_shot";
  public static String testBaseX2 = "house_shot_mac";
  public static String testName = "house1";
  public static String testNameTrans = "houseTx";
  public static String testNameMask = "houseTm";
  public static String httpURI = "https://sikulix-2014.readthedocs.io/en/latest/_images/popup.png";

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

  public JFrame createFrame(String imageName) {
    JFrame frame = new JFrame();
    frame.setAlwaysOnTop(true);
    frame.setUndecorated(true);
    ImageIcon image = new ImageIcon(new File(bundlePath, imageName + ".png").getAbsolutePath());
    Dimension size = new Dimension(image.getIconWidth(), image.getIconHeight());
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
      public void missing (ObserveEvent event) {
        event.setResponse(FindFailedResponse.ABORT);
      }
    };
  }

  public void setUpBase() {
    Settings.NewAPI = false;
    Settings.AlwaysResize = 1;
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
    showImage = false;
    Settings.ProfileLogs = false;
  }

  public void testIntro(Object... args) {
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    if (args.length > 0 && args[0] instanceof String) {
      int pause = 1;
      String imageName = (String) args[0];
      if (null == defaultFrame || !imageName.equals(defaultFrameImage)) {
        defaultFrame = createFrame((String) args[0]);
        pause = 2;
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          RunTime.pause(waitBefore);
          if (null != defaultFrame) {
            defaultFrame.setVisible(true);
          }
        }
      }).start();
      RunTime.pause(pause);
    }
    start = new Date().getTime();
  }

  public void testOutro(String message, Object... args) {
    String duration = String.format("%5d", new Date().getTime() - start);
    if (defaultFrame != null) {
      defaultFrame.setVisible(false);
      RunTime.pause(1);
    }
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    if (args.length > 0 && args[0] instanceof Image) {
      showCV((Image) args[0]);
    }
    Debug.logp(duration + " " + methodName + ": " + message, args);
  }

  public void test900_Template() {
    Settings.NewAPI = true;
    Image image = new Image("some image");
    //image is shown if first parm and single test
    testOutro("%s", image);
    //assertTrue("NotValid: " + image.toString(), image.isValid());
  }
}
