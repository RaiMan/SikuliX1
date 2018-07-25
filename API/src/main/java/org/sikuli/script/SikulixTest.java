/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SikulixTest {

  private static Screen scr = new Screen();

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  private static String showBase = "/Users/raimundhocke/IdeaProjects/SikuliX114/API/src/main/resources/ImagesAPI.sikuli";
  private static String showLink;
  private static int showWait;
  private static int showBefore;
  private static boolean isShown = false;

  private static void show(String image) {
    show(image, 3, 0);
  }

  private static void show(String image, int wait) {
    show(image, wait, 0);
  }

  private static void showStop() {
    scr.type("w", Key.CMD);
    isShown = false;
  }

  private static void show(String image, int wait, int before) {
    if (!image.endsWith(".png")) {
      image += ".png";
    }
    showLink = "file://" + showBase + "/" + image;
    showWait = wait;
    showBefore = before;
    Thread runnable = new Thread() {
      @Override
      public void run() {
        if (before > 0) {
          RunTime.pause(showBefore);
        }
        App.openLink(showLink);
        if (wait > 0) {
          RunTime.pause(showWait);
          //p("%s", App.focusedWindow());
          scr.type("w", Key.CMD);
        } else {
          isShown = true;
        }
      }
    };
    runnable.start();
  }

  public static void main(String[] args) {
//    String property = System.getProperty("java.awt.graphicsenv");
//    p("java.awt.graphicsenv: %s", property);
    RunTime runTime = RunTime.get();
    ImagePath.setBundlePath(showBase);
    Match match = null;
    String testImage = "findBase";

    List<Integer> runTest = new ArrayList<>();
    //runTest.add(1);
    //runTest.add(2);
    //runTest.add(3);
    runTest.add(4);

    if (runTest.contains(1)) {
      p("***** starting test1 scr.exists(testImage)");
      show(testImage, 0);
      //scr.hover();
      match = scr.exists(testImage);
      match.highlight(2);
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
      showStop();
      p("***** endOf test2");
    }
    if (runTest.contains(3)) {
      p("***** start test3 text OCR");
      App.focus("safari");
      scr.wait(1.0);
      TextRecognizer tr = TextRecognizer.start().setLanguage("deu");
      Region reg = scr.selectRegion();
      String text = "";
      if (Do.SX.isNotNull(reg)) {
        text = reg.text().trim();
      }
      p("read:\n%s", text);
      p("***** endOf test3");
    }
    if (runTest.contains(4)) {
      p("***** start test4 tessAPI");
      App.focus("safari");
      scr.wait(1.0);
      TextRecognizer tr = TextRecognizer.start();
      Tesseract1 tapi = tr.getAPI();
      Region reg = scr.selectRegion();
      BufferedImage bimg = scr.capture(reg).getImage();
      String text = "";
      List<Word> words = tapi.getWords(tr.resize(bimg), 3);
      //      p("read:\n%s", text);
      p("***** endOf test4");
    }
    if (isShown) {
      showStop();
    }
  }

  private static Region getInset(Region base, Region inset) {
    return new Region(base.x + inset.x, base.y + inset.y, inset.w, inset.h);
  }
}
