/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXTest;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementBasicsTest extends SXTest {

  @Before
  public void setUp() {
    setUpBase();
  }

  @Test
  public void test000_StartUp() {
    startUpBase();
  }

  @Ignore
  public void test001_Playground() {
    testIntro();
    RunTime.pause(3);
    testOutro("");
  }

  @Test
  public void test010_ImageRegion() {
    //defaultFrame.setVisible(true);
    testIntro();
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Region region = new Region(100, 200, 300, 400);
    Image image = new Image(region);
    testOutro("%s", image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test011_ImageLocation() {
    testIntro();
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Location location = new Location(100, 200);
    Image image = new Image(location);
    testOutro("%s", image);
    assertFalse("Valid???: " + image.toString(), image.isValid());
  }

  @Test
  public void test012_ImageScreenImage() {
    testIntro();
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Region region = new Region(100, 200, 300, 400);
    Image image = region.getImage();
    testOutro("%s", image);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test020_ImageFilename() {
    testIntro();
    String imageName = "../images/" + testName;
    Image image = new Image(imageName);
    testOutro("%s (%s)", image, imageName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test021_ImageFilenameCached() {
    testIntro();
    String imageName = testName;
    Image image = new Image(imageName);
    testOutro("%s (%s)", image, imageName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test025_PatternImageFilename() {
    testIntro();
    String imageName = testName;
    Pattern pattern = new Pattern(imageName);
    testOutro("%s (%s)", pattern, imageName);
    assertTrue("NotValid: " + pattern.toString(), pattern.isValid());
  }

  @Test
  public void test030_ImageFileResource() {
    testIntro();
    String resName = "class://Image::/images/" + testName;
    Image image = new Image(org.sikuli.script.Image.class, "images/" + testName);
    testOutro("%s (%s)", image, resName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Ignore
  public void test031_ImageFileJarResource() {
    testIntro();
    String resName = "class://SikulixImages::/provided/images/" + testName;
    Image image = new Image(null); //com.sikulix.SikulixImages.class, "provided/images/" + testName);
    testOutro("%s (%s)", image, resName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Ignore
  public void test032_ImageFileJarGetResource() {
    testIntro();
    String resName = "getResource::SikulixImages::/provided/images/" + testName;
    Image image = new Image(null); //SikulixImages.class.getResource("/provided/images/" + testName + ".png"));
    testOutro("%s (%s)", image, resName);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test040_ImageFileHTTP() {
    testIntro();
    Image image = new Image(httpURI);
    testOutro("%s (%s)", image, httpURI);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test041_ImageFileHTTPCached() {
    testIntro();
    Image image = new Image(httpURI);
    testOutro("%s (%s)", image, httpURI);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void test999() {
    testIntro();
    Map<URL, List<Object>> cache = Image.getCache();
    testOutro("%s", Image.cacheStats());
  }
}
