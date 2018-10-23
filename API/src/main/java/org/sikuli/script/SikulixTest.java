/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.util.ScreenHighlighter;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SikulixTest {

  //<editor-fold desc="housekeeping">
  private static Screen scr = new Screen();

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

  private static String showBase = "API/src/main/resources/ImagesAPI";
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
    showLink = "file://" + Image.create(image).getFileURL().getPath();
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
      if (Do.SX.isNotNull(allScreen.exists(pCorner, 30))) {
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
    ScreenHighlighter.closeAll();
    showStop();
    browserStop();
  }

  private static List<Match> highlight(List<Match> regs, int time) {
    for (Match reg : regs) {
      reg.highlight();
    }
    scr.wait(time * 1.0);
    ScreenHighlighter.closeAll();
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
    Debug.reset();
    String browser = "edge";
    if (runTime.runningMac) {
      browser = "safari";
      keyMeta = Key.CMD;
    }
    ImagePath.setBundlePath(new File(runTime.fWorkDir, showBase).getAbsolutePath());
    Match match = null;
    String testImage = "findBase";

//    runTest.add(0);
//    runTest.add(1); // exists
//    runTest.add(2); // findChange
//    runTest.add(3); // text OCR
//    runTest.add(4); // text find word
//    runTest.add(5); // text find lines RegEx
//    runTest.add(6); // text Region.find(someText)
//    runTest.add(7); // text Region.findAll(someText)
//    runTest.add(8); // text Region.getWordList/getLineList
//    runTest.add(9); // basic transparency
//    runTest.add(10); // transparency with pattern
//    runTest.add(11); // find SwitchToText
//    runTest.add(12); // class App

    if (runTest.size() > 1) {
      if (-1 < runTest.indexOf(0)) {
        runTest.remove(runTest.indexOf(0));
      }
    } else if (runTest.size() == 0) {
      before("test99", "play");
//      Debug.on(3);
//      Debug.globalTraceOn();
      after();
    }

    //<editor-fold desc="test1 exists">
    if (shouldRunTest(1)) {
      before("test1", "scr.exists(testImage)");
      show(testImage, 0);
      scr.wait(2.0);
      match = scr.exists(testImage, 10);
      match.highlight(2);

      after();
    }
    //</editor-fold>

    //<editor-fold desc="test2 findChange">
    if (shouldRunTest(2)) {
      before("test2", "findChanges");
      show(testImage, 0);
      scr.wait(2.0);
      Finder finder = new Finder(testImage);
      String imgChange = "findChange3";
      List<Region> changes = finder.findChanges(imgChange);
      match = scr.exists(testImage, 10);
      for (Region change : changes) {
        match.getInset(change).highlight(1);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test3 text OCR">
    if (shouldRunTest(3)) {
      before("test3", "text OCR");
      if (openTestPage()) {
        String text = "";
        if (Do.SX.isNotNull(reg)) {
          text = reg.text().trim();
        }
        p("***** read:\n%s", text);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test4 find word">
    if (shouldRunTest(4)) {
      before("test4", "findWord");
      String aWord = "brown";
      if (openTestPage()) {
        Match mText = reg.findWord(aWord);
        if (Do.SX.isNotNull(mText)) {
          mText.highlight(2);
          highlight(reg.findWords(aWord), 2);
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test5 findLines with RegEx">
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

    //<editor-fold desc="test6 Region.find(someText)">
    if (shouldRunTest(6)) {
      before("test6", "Region.find(someText)");
      String[] aTexts = new String[]{"another", "very, very lazy dog", "very + dog"};
      if (openTestPage()) {
        for (String aText : aTexts) {
          match = reg.existsText(aText);
          if (Do.SX.isNotNull(match)) {
            match.highlight(2);
          }
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test7 Region.findAll(someText)">
    if (shouldRunTest(7)) {
      before("test7", "Region.findAll(someText)");
      String aText = "very lazy dog";
      if (openTestPage()) {
        Match found = null;
        found = reg.hasText(Finder.asRegEx(aText));
        if (Do.SX.isNotNull(found)) {
          found.highlight(2);
        }
        highlight(reg.findAllText(aText), 2);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test8 Region.getWordList/getLineList">
    if (shouldRunTest(8)) {
      before("test8", "Region.getWords/getLines");
      if (openTestPage()) {
        List<Match> lines = reg.collectLines();
        if (lines.size() > 0) {
          for (Match line : lines) {
            line.highlight(1);
            p("***** line: %s", line.getText());
          }
        }
        List<Match> words = reg.collectWords();
        if (words.size() > 0) {
          int jump = words.size() / 10;
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
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test9 basic transparency">
    if (shouldRunTest(9)) {
      before("test9", "basic transparency");
      Pattern imgBG = new Pattern(Image.create("buttonTextOpa"));
      Pattern img = new Pattern(Image.create("buttonText"));
      Pattern imgTrans = new Pattern(Image.create("buttonTextTrans"));
      Pattern maskBlack = new Pattern("buttonTextBlackMask").mask();
      Pattern imgBlack = new Pattern("buttonTextBlackMask");
      Pattern maskTrans = new Pattern("buttonTextTransMask");
      Pattern maskedBlack = new Pattern(maskBlack).mask(maskBlack);
      Pattern maskedTrans = new Pattern(maskBlack).mask(maskTrans);
      Pattern[] patterns = new Pattern[]{imgBG, img, imgTrans, imgBlack, maskBlack, maskTrans, maskedBlack, maskedTrans};
//      Pattern[] patterns = new Pattern[]{maskedBlack};
      if (openTestPage("Test-page-1")) {
        //reg.highlight(1);
        reg.setAutoWaitTimeout(0);
        String out = "";
        for (Pattern image : patterns) {
          highlight(reg.has(image));
          try {
            Finder fmatches = (Finder) reg.findAll(image);
            List<Match> matches = reg.findAllList(image);
            out += String.format("*** findAll: %d of %s\n", matches.size(), image);
            for (Match next : matches) {
              next.highlight();
            }
          } catch (FindFailed findFailed) {
            out += String.format("findAll failed: %s\n", image);
          }
          scr.wait(1.0);
          ScreenHighlighter.closeAll();
        }
        p("%s", out);
      }
      scr.wait(1.0);
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test10 transparency with pattern">
    if (shouldRunTest(10)) {
      before("test10", "transparency with pattern");
      String wb = "whiteBlack";
      String wt = "whiteTrans";
      String wwt = "whiteWithText";
      //App.focus("preview"); scr.wait(1.0);
      show(wwt, 0);
      scr.wait(2.0);
      reg = scr;
      reg = App.focusedWindow();
      Pattern wbMask = new Pattern(wb).asMask();
      Pattern pWwtWb = new Pattern(wwt).withMask(wbMask);
      p("***** real image");
      reg.has(wwt);
      reg.highlight(-1);
      p("***** pattern asMask()");
      reg.has(wbMask);
      reg.highlight(-1);
      p("***** pattern withMask()");
      reg.has(pWwtWb);
      reg.highlight(-1);
      p("***** transparent masked image");
      reg.has(wt);
      reg.highlight(-1);
      after();
    }
    //</editor-fold>

    //<editor-fold desc="find SwitchToText">
    if (shouldRunTest(11)) {
      before("test11", "find SwitchToText");
      Settings.SwitchToText = true;
      String[] aTexts = new String[]{"another"};
      reg = scr;
      if (openTestPage()) {
        for (String aText : aTexts) {
          match = reg.has(aText);
          if (Do.SX.isNotNull(match)) {
            match.highlight(1);
          }
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="class App">
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
      //App app = App.open(notepad);
//      App app = new App(notepad);
//      App app = new App("/Applications/Brackets.app");
//      App app = new App("preview");
      App app = new App("safari");
//      app = new App(firefox);
//      app = new App(chrome);
      app.open(10);
      RunTime.pause(3);
      app.focus(1);
      if (app.isRunning(5)) {
        List<Region> windows = app.getWindows();
        for (Region window : windows) {
          p("%s", window);
        }
        RunTime.pause(10);
        //p("app: %s (%s)", app, app.focusedWindow());
        app.focusedWindow().highlight(2);
        //RunTime.pause(2);
        //app.close();
        app.closeByKey();
        //p("app: %s (%s)", app, app.window());
        app.open(5);
        //app = App.open(chrome);
        p("app: %s (%s)", app, app.focusedWindow());
        app.focusedWindow().highlight(2);
      }

      //App.focus("preview");
      //scr.wait(1.0);
      //reg = App.focusedWindow();
    }
  }
}
