package com.sikulix.basetests;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;
import org.sikuli.script.support.RunTime;

import java.io.File;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestFind {

  private String currentTest = "";
  private String result = "";
  private static String imagesPath = "target/test-classes/images";

  private static String message(String message, Object... args) {
    return(String.format(message, args));
  }
  private static void log(String message, Object... args) {
    System.out.println(message("[TestAll] " + message, args));
  }
  private static void logError(String message, Object... args) {
    log("[ERROR] " + message, args);
  }

  @BeforeClass
  public static void setUpClass() {
    ImagePath.setBundleFolder(new File(RunTime.sysPropUserDir, imagesPath));
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    currentTest = "currentTest";
    result = "";
  }

  @After
  public void tearDown() {
    if (!result.isEmpty()) {
      log("%s: %s", currentTest, result);
    }
  }

  @Test
  public void test_000_play() {
    currentTest = "test_000_play";
    log("%s; Images: %s", currentTest, ImagePath.getBundlePath());
    result = "should not fail ;-)";
  }

  @Test
  public void test_001_FinderFind() {
    currentTest = "test_001_FinderFind";
    String imageBase = "buttons";
    Finder finder = new Finder(Image.create(imageBase));
    String imageFind = "apple";
    finder.find(imageFind);
    result = "failed";
    assert finder.hasNext() : result;
    result = message("found: %s in: %s", imageFind, imageBase);
  }
}

