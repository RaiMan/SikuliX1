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

import java.net.URL;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElementRegionTest extends SXTest {

  @Before
  public void setUp() {
    setUpBase();
    Settings.setImageCache(0);
  }

  @Test
  public void test000_StartUp() {
    startUpBase();
  }

  @Test
  public void test200_RegionFindOld() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Region reg = new Screen();
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
  }

  @Test
  public void test210_RegionFindTransOld() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Region reg = new Screen();
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
  }

  @Ignore
  public void test211_RegionFindMaskOld() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Region reg = new Screen();
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
  }

  @Test
  public void test250_RegionFind() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewFind = true;
    Region reg = new Screen();
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
  }

  @Test
  public void test252_RegionWait() {
    waitBefore = 2;
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewFind = true;
    Region reg = new Screen();
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
  }

  @Test
  public void test260_RegionFindTrans() {
    testIntro(testBase);
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewFind = true;
    Region reg = new Screen();
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
  }

  @Ignore
  public void test261_RegionFindMask() {
    testIntro();
    Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    Settings.NewFind = true;
    Region reg = new Screen();
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
  }

  @Test
  public void test999() {
    testIntro();
    Map<URL, List<Object>> cache = Image.getCache();
    testOutro("%s", Image.cacheStats());
  }
}
