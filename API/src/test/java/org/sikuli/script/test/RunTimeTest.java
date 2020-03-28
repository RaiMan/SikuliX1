/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.sikuli.script.support.ExtensionManager;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RunTimeTest extends SXTest {

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
    //Assume.assumeFalse("Running headless - ignoring test", RunTime.isHeadless());
    RunTime.pause(3);
    testOutro("");
  }

  @Test
  public void test010_ExtensionsFile() {
    RunTime.setVerbose();
    testIntro();
    String classPath = ExtensionManager.makeClassPath(null);
    testOutro("classpath: %s", classPath);
    //assertTrue("NotValid: " + image.toString(), image.isValid());
  }
}
