/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import org.sikuli.basics.Debug;
import org.sikuli.script.RunTime;
import org.sikuli.script.SikulixStart;

import javax.swing.*;

public class SikulixIDE {

  private static final String osName = System.getProperty("os.name").substring(0, 1).toLowerCase();

  public static void main(String[] args) {

    if (SikulixStart.isVerbose()) {
      Debug.setWithTimeElapsed(SikulixStart.getElapsedStart());
      Debug.setGlobalDebug(3);
      Debug.globalTraceOn();
      Debug.setStartWithTrace();
      Debug.log(3,"Sikulix: starting IDE");
    }

    if (SikulixStart.isQuiet()) {
      Debug.quietOn();
    }

    if ("m".equals(osName)) {
      prepareMac();
    }

    RunTime runTime = RunTime.get(RunTime.Type.IDE, args);

    SikuliIDE.run(runTime, args);
  }

  private static void prepareMac() {
    try {
      // set the brushed metal look and feel, if desired
      System.setProperty("apple.awt.brushMetalLook", "true");

      // use the mac system menu bar
      System.setProperty("apple.laf.useScreenMenuBar", "true");

      // set the "About" menu item name
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiStar");

      // use smoother fonts
      System.setProperty("apple.awt.textantialiasing", "true");

      // ref: http://developer.apple.com/releasenotes/Java/Java142RNTiger/1_NewFeatures/chapter_2_section_3.html
      System.setProperty("apple.awt.graphics.EnableQ2DX", "true");

      // use the system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // put your debug code here ...
    }
  }
}
