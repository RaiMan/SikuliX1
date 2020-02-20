/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.script.*;
import org.sikuli.util.Highlight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SXTestOld {

  //<editor-fold desc="00 housekeeping">
  private static Screen scr;

  private static long start = -1;

  private static void timer() {
    start = new Date().getTime();
  }

  private static void timer(String msg) {
    p("%d (%s)", new Date().getTime() - start, msg.isEmpty() ? "msec" : msg);
  }

  private static void p(String msg, Object... args) {
    if (msg.isEmpty()) {
      return;
    }
    System.out.println(String.format(msg, args));
  }

  private static void error(String msg, Object... args) {
    p("[ERROR]" + msg, args);
  }

  private static String showBase = "API/src/main/resources/Images";
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

  public static void showStop() {
    if (isShown) {
      scr.type("w", keyMeta);
      isShown = false;
    }
  }

  public static void show(String image, int wait, int before) {
    if (!image.endsWith(".png")) {
      image += ".png";
    }
    showLink = "file://" + Image.create(image).getURL().getPath();
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

  private static RunTime runTime;
  private static Region reg = null;
  private static Region regWin = null;

  public static boolean openTestPage() {
    return openTestPage("");
  }

  private static String keyMeta = Key.CTRL;
  private static boolean isBrowserRunning = false;

  public static boolean openTestPage(String page) {
    String testPageBase = "https://github.com/RaiMan/SikuliX1/wiki/";
    String testPage = "Test-page-text";
    if (!page.isEmpty()) {
      testPage = page;
    }
    String actualPage = testPageBase + testPage;
    boolean success = false;
    String corner = "apple";
    Pattern pCorner = new Pattern(corner).similar(0.9);
    Match cornerSeen = null;
    if (App.openLink(actualPage)) {
      scr.wait(1.0);
      Screen allScreen = Screen.all();
      if (SX.isNotNull(allScreen.exists(pCorner, 30))) {
        success = true;
        cornerSeen = allScreen.getLastMatch();
        cornerSeen.hover();
        reg = App.focusedWindow();
        regWin = new Region(reg);
      }
    }
    if (success) {
      int wheelDirection = 0;
      success = false;
      while (!success) {
        List<Match> matches = reg.getAll(corner);
        if (matches.size() == 2) {
          reg = matches.get(0).union(matches.get(1));
          reg.h += 5;
          success = true;
          break;
        }
        if (wheelDirection == 0) {
          wheelDirection = Button.WHEEL_DOWN;
          reg.wheel(wheelDirection, 1);
          scr.wait(0.5);
          Match cornerMatch = regWin.exists(pCorner);
          if (cornerMatch.y >= cornerSeen.y) {
            wheelDirection *= -1;
          }
        }
        reg.wheel(wheelDirection, 1);
        scr.wait(0.5);
      }
    }
    if (!success) {
      p("***** Error: web page did not open (30 secs)");
    } else {
      //reg.highlight(1);
      isBrowserRunning = true;
    }
    return success;
  }

  private static void browserStop() {
    if (isBrowserRunning) {
      scr.type("w", keyMeta);
    }
    isBrowserRunning = false;
  }

  private static String currentTest = "";

  private static void before(String test, String text) {
    currentTest = test;
    p("***** starting %s %s", test, text);
  }

  private static void after() {
    p("***** ending %s", currentTest);
    Highlight.closeAll();
    showStop();
    browserStop();
  }

  private static List<Match> highlight(List<Match> regs, int time) {
    for (Match reg : regs) {
      reg.highlight();
    }
    scr.wait(time * 1.0);
    Highlight.closeAll();
    return regs;
  }

  private static void highlight(List<Match> regs) {
    highlight(regs, 1);
  }

  private static Match highlight(Match match) {
    if (null != match) match.highlight(1);
    return match;
  }

  private static List<Integer> runTest = new ArrayList<>();

  private static boolean shouldRunTest(int nTest) {
    if (runTest.contains(0) || runTest.contains(nTest)) {
      return true;
    }
    return false;
  }
  //</editor-fold>

  public static void main(String[] args) {
    //Debug.on(3);

    try {
      ImagePath.setBundlePath(new File("..", "_Support/_test").getCanonicalPath());
      p("ImagePath: %s", ImagePath.getBundlePath());
    } catch (IOException e) {
      p("ImagePath???");
      System.exit(1);
    }

    ScreenImage scrImg;

    scr = new Screen();
    Screen scr0 = Screen.get(0); //new Screen();
    p("scr(%d): %s", scr.getID(), scr);

    int scaleFactor = scr.getScale();
    p("Scale(%d) %d", scr.getID(), scaleFactor);

    scrImg = scr.capture();
    scrImg.save("scr");
    p("shot: scr (%dx%d)", scrImg.w, scrImg.h);

    Screen scr1 = new Screen(1);
    p("scr1: %s", scr1);

//    scaleFactor = scr1.getScale();
//    p("Scale(%d) %d", scr1.getID(), scaleFactor);

    scrImg = scr1.capture();
    scrImg.save("scr1");
    p("shot: scr1 (%dx%d)", scrImg.w, scrImg.h);

    Mouse.init();

    scr1.hover();
    p("scr1 hover: %s", Mouse.at());

    scr.hover();
    p("scr hover: %s", Mouse.at());

    System.exit(0);

    Debug.reset();
    String browser = "edge";
    if (runTime.runningMac) {
      browser = "safari";
      keyMeta = Key.CMD;
    }
    ImagePath.setBundlePath(new File(runTime.fWorkDir, showBase).getAbsolutePath());
    Match match = null;
    String testImage = "findBase";

    //<editor-fold desc="test02 findChange">
    if (shouldRunTest(2)) {
      before("test2", "findChanges");
      show(testImage, 0);
      scr.wait(2.0);
      Finder finder = new Finder(Image.create(testImage));
      String imgChange = "findChange3";
      List<Region> changes = finder.findChanges(imgChange);
      match = scr.exists(testImage, 10);
      for (Region change : changes) {
        match.getInset(change).highlight(1);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test03 text OCR">
    if (shouldRunTest(3)) {
      before("test3", "text OCR");
      if (openTestPage()) {
        String text = "";
        if (SX.isNotNull(reg)) {
          text = reg.text().trim();
        }
        p("***** read:\n%s", text);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test04 find word">
    if (shouldRunTest(4)) {
      before("test4", "findWord");
      String aWord = "brown";
      if (openTestPage()) {
        Match mText = reg.findWord(aWord);
        if (SX.isNotNull(mText)) {
          mText.highlight(2);
          highlight(reg.findWords(aWord), 2);
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test05 findLines with RegEx">
    if (shouldRunTest(5)) {
      before("test5", "findLines with RegEx");
      String aRegex = "jumps.*?lazy";
      if (openTestPage()) {
        List<Match> matches = highlight(reg.findLines(Finder.asRegEx(aRegex)), 3);
        for (Match found : matches) {
          p("**** line: %s", found.getText());
        }
        aRegex = "jumps.*?very.*?lazy";
        matches = highlight(reg.findLines(Finder.asRegEx(aRegex)), 3);
        for (Match found : matches) {
          p("**** line: %s", found.getText());
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test12 class App">
    if (shouldRunTest(12)) {
      before("test12", "class App");
      String chrome = "google chrome";
      String firefox = "firefox";
      String notepad = "brackets";
      if (runTime.runningWindows) {
        chrome = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
        firefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
        notepad = "C:\\Program Files\\Notepad++\\notepad++.exe";
      }
      App app = new App(notepad);
//      App app = new App("/Applications/Brackets.app");
//      App app = new App("preview");
//      App apps = new App("safari");
//      app = new App(firefox);
//      app = new App(chrome);
      app.open(10);
      //RunTime.pause(3);
      app.focus();
      if (app.isRunning(5)) {
        List<Region> windows = app.getWindows();
        for (Region window : windows) {
          p("window: %s", window);
        }
        app.focusedWindow().highlight(2);
        RunTime.pause(3);
        //app.close();
        app.closeByKey();
        app.open(5);
        p("app: %s (%s)", app, app.focusedWindow());
        app.focusedWindow().highlight(2);
      }

      //App.focus("preview");
      //scr.wait(1.0);
      //reg = App.focusedWindow();
    }
    //</editor-fold>
    //</editor-fold>
  }
}
