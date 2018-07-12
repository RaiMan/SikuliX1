/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class SikulixTest {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }
  public static void main(String[] args) {
//    String property = System.getProperty("java.awt.graphicsenv");
//    p("java.awt.graphicsenv: %s", property);
    RunTime runTime = RunTime.get();
    Screen scr = new Screen();
    ImagePath.setBundlePath(runTime.fSikulixStore.getAbsolutePath());
    Match match = null;
    String testImage = "findBase";

    List<Integer> runTest = new ArrayList<>();
    runTest.add(1);
    runTest.add(2);

    if (runTest.contains(1)) {
      p("***** starting test1 scr.exists(testImage)");
      //scr.hover();
      match = scr.exists(testImage);
      //match.highlight(2);
      p("***** ending test");
    }
    if (runTest.contains(2)) {
      p("***** start test2 findChange");
      Finder finder = new Finder(testImage);
      String imgChange = "findChange3";
      List<Region> changes = finder.findChanges(imgChange); //, 100);
      for (Region change : changes) {
        getInset(match, change).highlight(1);
      }
      p("***** endOf test2");
    }
  }

  private static Region getInset(Region base, Region inset) {
    return new Region(base.x + inset.x, base.y + inset.y, inset.w, inset.h);
  }
}
