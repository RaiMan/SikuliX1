package com.sikulix.basetests;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.sikuli.script.*;
import org.sikuli.script.support.RunTime;

import java.io.File;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestFind {

  private String currentTest = "";
  private String result = "";
  private String info = "";
  private String baseImage = "winButtonsEdge";
  private static String imagesPath = "target/test-classes/testimages";

  private static String message(String message, Object... args) {
    return(String.format(message, args));
  }
  private void log(String message, Object... args) {
    System.out.println(message( "[" + this.getClass().getSimpleName() + "] " + message, args));
  }
  private void logError(String message, Object... args) {
    log(" [ERROR] " + message, args);
  }
  private void logr(String message, Object... args) {
    result += "\n  " + message(message, args);
  }
  private String getErrorMessage() {
    return "[FAILED] " + info;
  }

  private String getBaseImage(String image) {
    return image.isEmpty() ? baseImage : image;
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
    info = "";
  }

  @After
  public void tearDown() {
    if (!result.isEmpty()) {
      log("%s:%s", currentTest, result);
    }
  }

  @Test
  public void test_000_play() {
    currentTest = "test_000_play";
    info = message("setup: bundlepath is null");
    assert null != ImagePath.getBundlePath() : getErrorMessage();
    logr("Images: %s", ImagePath.getBundlePath());
    logr("should not fail ;-)");
  }

  private String imageFind = "";
  private String testBaseImage = "";
//  private String testBaseImage = "macButtonsSafari";
//  private String testBaseImage = "macButtonsChrome";

  @Test
  public void test_001_FinderFind() {
    currentTest = "test_001_FinderFind";
    String imageBase = getBaseImage(testBaseImage);
    Finder finder = new Finder(Image.create(imageBase));
    find(finder, "apple", imageBase);
    find(finder, "button", imageBase);
  }

  private void find(Finder finder, String image, String imageBase) {
    findResized(finder, image, imageBase, 1);
  }

  @Test
  public void test_002_FinderFindResized() {
    currentTest = "test_002_FinderFindResized";
    String imageBase = getBaseImage(testBaseImage);
    Finder finder = new Finder(Image.create(imageBase));
    findResized(finder, "macApple", imageBase, 0.5);
    findResized(finder, "macButton", imageBase, 0.5);
  }

  private void findResized(Finder finder, String image, String imageBase, double factor) {
    if (factor > 0 && factor != 1) {
      finder.find(new Pattern(image).resize((float) factor));
      info = message("find: %s (x %.1f) in: %s", image, factor, imageBase);
    } else {
      finder.find(image);
      info = message("find: %s in: %s", image, imageBase);
    }
    assert finder.hasNext() : getErrorMessage();
    Match match = finder.next();
    logr(info + " (%%%.2f) (%d,%d)",100 * match.getScore(), match.x, match.y);
  }

  @Test
  public void test_003_FinderFindAll() {
    currentTest = "test_003_FinderFindAll";
    String imageBase = getBaseImage(testBaseImage);
    Finder finder = new Finder(Image.create(imageBase));
    findAll(finder, "apple", imageBase);
    findAll(finder, "button", imageBase);
  }

  private void findAll(Finder finder, String img, String imageBase) {
    String imageFind = img;
    finder.findAll(imageFind);
    info = message("findAll: %s in: %s", imageFind, imageBase);
    assert finder.hasNext() : getErrorMessage();
    Match match = finder.next();
    while (null != match) {
      logr(info + " (%%%.2f) (%d,%d)",100 * match.getScore(), match.x, match.y);
      match = finder.next();
    }
  }
}

