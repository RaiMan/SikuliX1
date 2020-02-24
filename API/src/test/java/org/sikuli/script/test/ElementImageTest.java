/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.FindFailedResponse;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.support.SXTest;
import org.sikuli.util.Highlight;

import java.net.URL;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementImageTest extends SXTest {

  @Before
  public void setUp() {
    setUpBase();
  }

  @Test
  public void test000_StartUp() {
    startUpBase();
  }

  @Test
  public void test100_ImageFind() {
    testIntro();
    Image shot = new Image(testBase);
    Match match = null;
    try {
      match = shot.find(testName);
    } catch (FindFailed findFailed) {
    }
    testOutro("%s in %s is %s", testName, shot, match);
  }

  @Test
  public void test105_ImageFindResize() {
    testIntro();
    Image shot = new Image(testBaseX2);
    Assert.assertTrue("", shot.isValid());
    Match match = null;
    Settings.AlwaysResize = 2;
    try {
      match = shot.find(testName);
    } catch (FindFailed findFailed) {
    }
    testOutro("%s in %s is %s", testName, shot, match);
  }

  @Test
  public void test110_ImageFindTrans() {
    testIntro();
    Image shot = new Image(testBase);
    Match match = null;
    try {
      match = shot.find(testNameTrans);
    } catch (FindFailed findFailed) {
    }
    testOutro("%s in %s is %s", testName, shot, match);
  }

  @Test
  public void test120_ImageFindChanges() {
    if (showImage) {
      testIntro(testBase);
    } else {
      testIntro();
    }
    Image original = new Image(testBase);
    Image changed = new Image(testChanged);
    List<Match> changes = original.findChanges(changed);
    for (Match change : changes) {
      if (showImage) {
        change.highlight();
      }
    }
    if (showImage) {
      Highlight.closeAll(3);
    }
    testOutro("%s%s == %s changes %d", "", original, changed, changes.size());
    Assert.assertTrue("Not all changes!", changes.size() == 6);
  }

  @Test
  public void test800_ImageMissingPrompt() {
    testIntro();
    if (showImage) {
      String testMissing = makeImageFileName(testMissingName, true);
      Image.setMissingPrompt();
      Image missing = null;
      try {
        missing = new Image(testMissing);
        testOutro("%screated: %s missing: %s", "", missing, testMissingName);
        Assert.assertTrue("create missing not valid", missing.isValid());
      } catch (Exception e) {
        testOutro("not created missing: %s (%s)", testMissingName, e.getMessage());
      }
    } else {
      testOutro("skipped: missing: %s", testMissingName);
    }
  }

  @Test
  public void test800_FindFailedPrompt() {
    testIntro();
    if (showImage) {
      Image where = new Image(testBase);
      where.setFindFailedResponse(FindFailedResponse.PROMPT);
      String testFailed = copyImageFileName(testNameMask, "testFailed");
      Image what = new Image(testFailed);
      try {
        Match match = where.find(what);
//        testOutro("%screated: %s missing: %s", "", missing, testMissingName);
//        Assert.assertTrue("create missing not valid", missing.isValid());
      } catch (Exception e) {
        testOutro("not created failing: %s (%s)", testFailed, e.getMessage());
      }
    } else {
      testOutro("skipped: FindFailed prompting");
    }
  }

  @Test
  public void test999() {
    testIntro();
    Map<URL, List<Object>> cache = Image.getCache();
    testOutro("%s", Image.cacheStats());
  }
}
