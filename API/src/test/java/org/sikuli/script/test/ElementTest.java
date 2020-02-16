/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.opencv.highgui.HighGui;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.script.support.RunTime;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementTest {

  String methodName = "NotValid";
  RunTime runTime = RunTime.get();
  String bundlePath = null;
  static boolean showImage = true;

  void testIntro() {
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
  }

  void testOutro(String message, Object... args) {
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    if (args[0] instanceof Image) show((Image) args[0]);
    Debug.logp(methodName + ": " + message, args);
  }

  void show(Image image) {
    show(methodName, image);
  }

  void show(String title, Image image) {
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

  @Before
  public void setUp() {
    Settings.NewFind = false;
    if (null == bundlePath) {
      File bundleFile = new File(runTime.fWorkDir, "src/main/resources/images");
      ImagePath.setBundleFolder(bundleFile);
      bundlePath = ImagePath.getBundlePath();
    }
  }

  @Test
  public void test000_StartUp() {
    if (!RunTime.isHeadless()) {
      Region region = new Region(100, 200, 300, 400);
      Image image = new Image(region);
    }
    showImage = false;
  }

  @Test
  public void test010_ImageRegion() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Region region = new Region(100, 200, 300, 400);
    Image image = new Image(region);
    testOutro("%s", image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test011_ImageLocation() {
    Location location = new Location(100, 200);
    Image image = new Image(location);
    testOutro("%s", image);
    assertFalse("Valid???: " + image.toString(), image.isValid());
  }

  @Test
  public void test012_ImageScreenImage() {
    Region region = new Region(100, 200, 300, 400);
    Image image = region.getImage();
    testOutro("%s", image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  String testName = "test";

  @Test
  public void test020_ImageFilename() {
    String imageName = "../images/" + testName;
    Image image = new Image(imageName);
    testOutro("%s (%s)", image, imageName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test021_ImageFilenameCached() {
    String imageName = testName;
    Image image = new Image(imageName);
    testOutro("%s (%s)", image, imageName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test030_ImageFileResource() {
    String resName = "class:///images/" + testName;
    Image image = new Image(org.sikuli.script.Image.class, "images/" + testName);
    testOutro("%s (%s)", image, resName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  String httpURI = "https://sikulix-2014.readthedocs.io/en/latest/_images/popup.png";

  @Test
  public void test040_ImageFileHTTP() {
    Image image = new Image(httpURI);
    testOutro("%s (%s)", image, httpURI);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test041_ImageFileHTTPCached() {
    Image image = new Image(httpURI);
    testOutro("%s (%s)", image, httpURI);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test100_ImageFind() {
    Image shot = new Image("shot");
    Match match = null;
    try {
      match = shot.find(testName);
    } catch (FindFailed findFailed) {
    }
    testOutro("%s in %s is %s", testName, shot, match);
  }

  @Test
  public void test200_RegionFindOld() {
    Region reg = new Screen();
    Match match = null;
    try {
      match = reg.find(testName);
    } catch (FindFailed findFailed) {
    }
///TODO check highlight
    if (null != match) {
//      match.highlight(2);
    }
    testOutro("%s in %s is %s", testName, reg, match);
  }

  @Test
  public void test201_RegionFind() {
    Settings.NewFind = true;
    Region reg = new Screen();
    Match match = null;
    try {
      match = reg.find(testName);
    } catch (FindFailed findFailed) {
    }
///TODO check highlight
    if (null != match) {
//      match.highlight(2);
    }
    testOutro("%s in %s is %s", testName, reg, match);
  }

  @Test
  public void test202_RegionWait() {
    Settings.NewFind = true;
    Region reg = new Screen();
    Match match = null;
    try {
      match = reg.wait(testName, 5);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
//      match.highlight(2);
    }
    testOutro("%s in %s is %s", testName, reg, match);
  }

  static Image savedImage = null;

  @Ignore
  public void test900_Template() {
    Settings.NewFind = true;
    Image image = savedImage = new Image(testName);
    testOutro("%s", "testName");
    show(image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test999() {
    Map<URL, List<Object>> cache = Image.getCache();
    testOutro("%s", Image.cacheStats());
  }
}
