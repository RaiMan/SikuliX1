/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXTest;
import org.sikuli.util.Highlight;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementRegionOldTest extends SXTest {

  @Before
  public void setUp() {
    setUpBase();
  }

  @Test
  public void test000_StartUp() {
    startUpBase();
  }

  @Test
  public void test200_RegionFind() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewAPI = false;
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
  public void test210_RegionFindTrans() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewAPI = false;
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
    testOutro("%s in %s is %s", testName, reg, match);
    Assert.assertTrue("Not found!", checkMatch(match, 0.95));
  }

  @Test
  public void test211_RegionFindMask() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewAPI = false;
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
  public void test220_RegionFindAll() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewAPI = false;
    Region reg = getDefaultRegion();
    Iterator<Match> matches = null;
    try {
      matches = reg.findAll(testName);
    } catch (FindFailed findFailed) {
    }
    Match match = null;
    int matchCount = 0;
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
  public void test999() {
    testIntro();
    Map<URL, List<Object>> cache = Image.getCache();
    testOutro("%s", Image.cacheStats());
  }
}
