/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    if (isShown) {
      scr.type("w", Key.CMD);
      isShown = false;
    }
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

  private static RunTime runTime = RunTime.get();
  private static Region reg = null;

  private static boolean openTestPage() {
    return openTestPage("");
  }

  private static boolean openTestPage(String page) {
    String browser = "edge";
    if (runTime.runningMac) {
      browser = "safari";
    }
    String testPageBase = "https://github.com/RaiMan/SikuliX1/wiki/";
    String testPage = "Empty-Wiki--SikuliX-1.1.3-plus";
    String corner = "apple";
    if (!page.isEmpty()) {
      testPage = page;
    }
    App appBrowser = new App(browser);
    boolean success = false;
    if (appBrowser.isRunning()) {
      appBrowser.focus();
      scr.wait(1.0);
      App.setClipboard("");
      scr.type("l", Key.CMD);
      scr.type("c", Key.CMD);
      scr.type(Key.ESC);
      String actualPage = App.getClipboard();
      if (!actualPage.isEmpty()) {
        success = actualPage.contains(testPage);
      } else {
        scr.type("w", Key.CMD);
      }
    }
    if (!success && App.openLink("https://github.com/RaiMan/SikuliX1/wiki/Empty-Wiki--SikuliX-1.1.3-plus")) {
      scr.wait(1.0);
      reg = App.focusedWindow();
      if (Do.SX.isNotNull(reg.exists("apple", 10))) {
        success = true;
      }
    }
    if (success) {
      reg = App.focusedWindow();
      List<Match> matches = reg.getAll(corner);
      if (matches.size() == 2) {
        reg = matches.get(0).union(matches.get(1));
        reg.h += 5;
      } else {
        success = false;
      }
    }
    if (!success) {
      p("***** Error: web page did not open (10 secs)");
    }
    return success;
  }

  public static void main(String[] args) {
    ImagePath.setBundlePath(showBase);
    Match match = null;
    String testImage = "findBase";

    List<Integer> runTest = new ArrayList<>();
    //runTest.add(1);
    //runTest.add(2);
    //runTest.add(3);
    //runTest.add(4);
    //runTest.add(5);
    //runTest.add(6);
    //runTest.add(7);

    int newest = 8;

    if (runTest.size() == 0) {
      runTest.add(newest);
    }

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
      p("***** endOf test2");
    }

    showStop();

    if (runTest.contains(3)) {
      p("***** start test3 text OCR");
      if (openTestPage()) {
        TextRecognizer tr = TextRecognizer.start();
        String text = "";
        if (Do.SX.isNotNull(reg)) {
          text = reg.text().trim();
        }
        p("***** read:\n%s", text);
      }
      p("***** endOf test3");
    }

    if (runTest.contains(4)) {
      p("***** start test4 findWord");
      String aWord = "brown";
      if (openTestPage()) {
        TextRecognizer tr = TextRecognizer.start();
        Match mText = reg.findWord(aWord);
        if (Do.SX.isNotNull(mText)) {
          mText.highlight(2);
          reg.findWords(aWord).show(2);
        }
      }
      p("***** endOf test4");
    }

    if (runTest.contains(5)) {
      p("***** start test5 findLines with RegEx");
      String aRegex = "jumps.*?lazy";
      if (openTestPage()) {
        TextRecognizer tr = TextRecognizer.start();
        List<Match> matches = reg.findLines(Finder.asRegEx(aRegex)).show(3);
        for (Match found : matches) {
          p("**** line: %s", found.getText());
        }
        aRegex = "jumps.*?very.*?lazy";
        matches = reg.findLines(Finder.asRegEx(aRegex)).show(3);
        for (Match found : matches) {
          p("**** line: %s", found.getText());
        }
      }
      p("***** endOf test5");
    }

    if (runTest.contains(6)) {
      p("***** start test6 Region.find(someText)");
      String[] aTexts = new String[]{"another", "very, very lazy dog", "very + dog"};
      if (openTestPage()) {
        TextRecognizer tr = TextRecognizer.start();
        for (String aText : aTexts) {
          match = reg.existsText(aText);
          if (Do.SX.isNotNull(match)) {
            match.highlight(2);
          }
        }
      }
      p("***** endOf test6");
    }

    if (runTest.contains(7)) {
      p("***** start test7 Region.find(allText)");
      String aText = "very lazy dog";
      if (openTestPage()) {
        TextRecognizer tr = TextRecognizer.start();
        Match found = null;
        found = reg.hasText(Finder.asRegEx(aText));
        if (Do.SX.isNotNull(found)) {
          found.highlight(2);
        }
        //aText = "very.*?dog";
        reg.findAllText(aText).show(2);
      }
      p("***** endOf test7");
    }

    if (runTest.contains(8)) {
      p("***** start test8 Region.getWordList/getLineList");
      if (openTestPage()) {
        TextRecognizer tr = TextRecognizer.start();
        List<Match> lines = reg.getLineList();
        if (lines.size() > 0) {
          for (Match line : lines) {
            line.highlight(1);
            p("***** line: %s", line.getText());
          }
        }
        List<Match> words = reg.getWordList();
        if (words.size() > 0) {
          int jump = words.size()/10;
          int current = 0;
          for (Match word : words) {
            if (current % 10 == 0) {
              word.highlight(1);
            }
            p("%s", word.getText());
            current++;
          }
        }
      }
      p("***** endOf test8");
    }
  }

  private static Region getInset(Region base, Region inset) {
    return new Region(base.x + inset.x, base.y + inset.y, inset.w, inset.h);
  }
}
