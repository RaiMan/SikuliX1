/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

public class SikulixBoot {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }
  public static void main(String[] args) {
//    String property = System.getProperty("java.awt.graphicsenv");
//    p("java.awt.graphicsenv: %s", property);
    RunTime runTime = RunTime.get();
    Screen scr = new Screen();
    ImagePath.setBundlePath(runTime.fSikulixStore.getAbsolutePath());
    p("***** starting test");
    scr.hover();
    String testImageName = "testImage";
    String testImage = "_" + testImageName;
//    ScreenImage image = scr.userCapture();
//    String testImage = image.saveInBundle(testImageName);
    Match match = scr.exists(testImage);
    p("***** ending test");
  }
}
