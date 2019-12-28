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
  private static String imagesPath = "target/test-classes/images";

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
    result += "\n" + message(message, args);
  }
  private String getErrorMessage() {
    return "[FAILED] " + info;
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
    logr("Images: %s", ImagePath.getBundlePath());
    logr("should not fail ;-)");
  }

  @Test
  public void test_001_FinderFind() {
    currentTest = "test_001_FinderFind";
    String imageBase = "buttons";
    Finder finder = new Finder(Image.create(imageBase));
    String imageFind = "apple";
    finder.find(imageFind);
    info = message("find: %s in: %s", imageFind, imageBase);
    assert finder.hasNext() : getErrorMessage();
    logr(info + " (%%%.4f)", 100 * finder.next().getScore());
  }

  @Test
  public void test_002_FinderFindResized() {
    currentTest = "test_002_FinderFindResized";
    String imageBase = "buttons";
    Finder finder = new Finder(Image.create(imageBase));
    String imageFind = "macButton";
    Pattern imageFindResized = new Pattern(imageFind).resize(0.5f);
    finder.find(imageFindResized);
    info = message("find: %s (resized 0.5) in: %s", imageFind, imageBase);
    assert finder.hasNext() : getErrorMessage();
    logr(info + " (%%%.2f)", 100 * finder.next().getScore());
  }

  @Test
  public void test_003_FinderFindAll1() {
    currentTest = "test_003_FinderFindAll1";
    String imageBase = "buttons1";
    Finder finder = new Finder(Image.create(imageBase));
    findAll(finder, "apple", imageBase);
  }

  private void findAll(Finder finder, String img, String imageBase) {
    String imageFind = img;
    finder.findAll(imageFind);
    info = message("findAll: %s in: %s", imageFind, imageBase);
    assert finder.hasNext() : getErrorMessage();
    Match match = finder.next();
    while (null != match) {
      logr(info + " (%%%.4f) (%d,%d)",100 * match.getScore(), match.x, match.y);
      match = finder.next();
    }
  }

  @Test
  public void test_004_FinderFindAll2() {
    //Debug.on(3);
    currentTest = "test_004_FinderFindAll1";
    String imageBase = "buttons";
    Finder finder = new Finder(Image.create(imageBase));
    findAll(finder, "button", imageBase);
  }
}

