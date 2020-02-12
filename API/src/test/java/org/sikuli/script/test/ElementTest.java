/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.support.RunTime;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementTest {

  String methodName = "NotValid";
  RunTime runTime = RunTime.get();
  String bundlePath = null;

  void testIntro() {
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
  }

  void testOutro(String message, Object... args) {
    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    Debug.logp(methodName + ": " + message, args);
  }

  @Before
  public void setUp() {
    if (null == bundlePath) {
      File bundleFile = new File(runTime.fWorkDir, "src/main/resources/images");
      ImagePath.setBundleFolder(bundleFile);
      bundlePath = ImagePath.getBundlePath();
    }
  }

  @Test
  public void test000_StartUp() {
    Region region = new Region(100, 200, 300, 400);
    Image image = new Image(region);
  }

  @Test
  public void test001_ImageRegion() {
    Region region = new Region(100, 200, 300, 400);
    Image image = new Image(region);
    testOutro("%s", image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test002_ImageLocation() {
    Location location = new Location(100, 200);
    Image image = new Image(location);
    testOutro("%s", image);
    assertFalse("Valid???: " + image.toString(), image.isValid());
  }

  @Test
  public void test003_ImageScreenImage() {
    Region region = new Region(100, 200, 300, 400);
    Image image = region.getImage();
    testOutro("%s", image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  String testName = "test";

  @Test
  public void test004_ImageFilename() {
    String imageName = "../images/" + testName;
    Image image = new Image(imageName);
    testOutro("%s (%s)", image, imageName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test005_ImageFilenameCached() {
    String imageName = testName;
    Image image = new Image(imageName);
    testOutro("%s (%s)", image, imageName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test006_ImageFileResource() {
    String resName = "class:///images/" + testName;
    Image image = new Image(org.sikuli.script.Image.class, "images/" + testName);
    testOutro("%s (%s)", image, resName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }
}
