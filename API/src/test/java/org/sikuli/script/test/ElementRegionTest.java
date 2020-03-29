/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXTest;
import org.sikuli.util.Highlight;

import java.net.URL;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementRegionTest extends SXTest {

  @Before
  public void setUp() {
    setUpBase();
  }

  @Test
  public void test000_StartUp() {
    startUpBase();
  }

  @Test
  public void test250_RegionFind() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    Match match = null;
    try {
      match = reg.find(testName);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testName, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test251_RegionFindLastSeen() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    //useScreen = false;
    Settings.CheckLastSeen = true;
    testIntro(testBase);
    Region reg = getDefaultRegion();
    //reg = reg.topLeft();
    //reg.highlight(2);
    Match match = null;
    try {
      match = reg.find(testName);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testName, reg, match);
    testIntro(testBase);
    match = null;
    //reg.ignoreLastSeen();
    try {
      match = reg.find(testName);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testName, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test252_RegionWait() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    waitBefore = 2;
    testIntro(testBase);
    Region reg = getDefaultRegion();
    Match match = null;
    try {
      match = reg.wait(testName, 5);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testName, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test253_RegionWaitVanish() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    waitAfter = 3;
    testIntro(testBase);
    boolean vanished = false;
    Region reg = getDefaultRegion();
    if (reg.has(testName)) {
      vanished = reg.waitVanish(testName);
    }
    testOutro("%s in %s vanished %s", testName, reg, vanished);
    Assert.assertTrue("Not vanished!", vanished);
  }

  @Test
  public void test255_RegionFindAllIterator() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    Iterator<Match> matches = null;
    int matchCount = 0;
    try {
      matches = reg.findAll(testName);
    } catch (FindFailed findFailed) {
    }
    Match match = null;
    while (matches.hasNext()) {
      matchCount++;
      match = matches.next();
      if (showImage) {
        match.highlight();
      }
    }
    if (showImage) {
      Highlight.closeAll(3);
    }
    testOutro("%s in %s is %s (%d)", testName, reg, match, matchCount);
    Assert.assertNotNull("Not Found!", match);
  }

  @Test
  public void test258_RegionFindAllList() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    List<Match> matches = null;
    int matchCount = 0;
    try {
      matches = reg.findAll(testName).asList();
    } catch (FindFailed findFailed) {
    }
    if (matches != null) {
      matchCount = matches.size();
    }
    if (showImage) {
      for (Match match : matches) {
        match.highlight();
      }
      Highlight.closeAll(3);
    }
    testOutro("%s in %s found %d times", testName, reg, matchCount);
    Assert.assertTrue("Not Found!", matchCount == 3);
  }

  @Test
  public void test260_RegionFindTrans() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    Match match = null;
    try {
      match = reg.find(testNameTrans);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testNameTrans, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test261_RegionFindMask() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    Match match = null;
    try {
      Image imageMasked = new Image(testName).mask(testNameMask);
      match = reg.find(imageMasked);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testName, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test262_RegionFindIsMasked() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    Match match = null;
    try {
      Image imageMasked = new Image(testNameMask).masked();
      match = reg.find(imageMasked);
    } catch (FindFailed findFailed) {
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
    }
    testOutro("%s in %s is %s", testName, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test270_RegionFindAny() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro(testBase);
    Region reg = getDefaultRegion();
    List<Match> matches = new ArrayList<>();
    int matchCount = 0;
    int targetCount = 6;
    List<Object> images = new ArrayList<>();
    for (int i = 1; i < targetCount + 1; i++) {
      images.add("any" + i);
    }
    long start = new Date().getTime();
    for (Object image : images) {
      Match match;
      try {
        match = reg.find(image);
      } catch (FindFailed findFailed) {
        match = null;
      }
      matches.add(match);
    }
    long elapsed = new Date().getTime() - start;
    start = new Date().getTime();
    matches = reg.findAnyList(images);
    long elapsed1 = new Date().getTime() - start;
    if (matches != null) {
      matchCount = matches.size();
    }
    if (showImage) {
      for (Match match : matches) {
        match.highlight();
        Debug.logp("%s", match);
      }
      Highlight.closeAll(3);
    }
    testOutro("%s in %s found %d times (%d, %d)", testName, reg, matchCount, elapsed, elapsed1);
    Assert.assertTrue("Not Found!", matchCount == targetCount);
  }

  @Test
  public void test300_FindFailedPrompt() {
    if (showImage) {
      testIntro(testBase);
      Region reg = defaultRegion;
      reg.setFindFailedResponse(FindFailedResponse.PROMPT);
      String testFailed = copyImageFileName(testNameMask, "testFailed");
      Image what = new Image(testFailed);
      try {
        Match match = reg.find(what);
        if (null == match) {
          testOutro("skipping after prompt: %s in %s", testFailed, reg);
        } else {
          match.highlight(2);
          testOutro("success after prompt: %s in %s is %s", testFailed, reg, match);
        }
      } catch (Exception e) {
        testOutro("failing after prompt: %s (%s)", testFailed, e.getMessage());
      }
    } else {
      testOutro("skipped: FindFailed prompting");
    }
  }

  @Ignore
  public void test700_BasicText() {
    Region reg = defaultRegion;
    reg = new Region(0, 0, 300, 300);

    Image txtImg;
    Mat content;
    String text;

    testIntro();
    txtImg = new Image("txtImg");
    content = txtImg.getContent();
    text = OCR.readText(content);
    testOutro("mat text: %s", text);
    testIntro();
    text = OCR.readText(txtImg.getContent());
    testOutro("mat text: %s", text);

    Image.resetCache();

    testIntro();
    txtImg = new Image("txtImg");
    text = OCR.readText(txtImg);
    testOutro("img text: %s", text);
    testIntro();
    text = OCR.readText(txtImg);
    testOutro("img text: %s", text);
  }


  @Ignore
  public void test800_RegionFindFailed() {
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    testIntro();
    Region reg = getDefaultRegion();
    Match match = null;
    String error = "";
    try {
      match = reg.find(testName);
    } catch (FindFailed findFailed) {
      error = findFailed.getMessage();
    }
    if (null != match) {
      if (showImage) {
        match.highlight(2);
      }
      testOutro("%s in %s is %s", testName, reg, match);
    } else {
      testOutro("Not Found: %s in %s", testName, reg);
    }
    Assert.assertFalse("Should not be fond!", error.isEmpty());
  }

  @Test
  public void test999() {
    testIntro();
    Map<URL, List<Object>> cache = Image.getCache();
    testOutro("%s", Image.cacheStats());
  }
}
