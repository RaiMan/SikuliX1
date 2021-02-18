/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.script.runnerSupport.JythonSupport;

import java.util.List;

public class SikulixEvaluate {
  public static void main(String[] args) {
    RunTime.get().show();
    if (args.length == 0) {
      Commons.printLog("SikulixEvaluate: Nothing to do!");
      return;
    }
    if ("test".equals(args[0])) {
      test();
    }
  }

  public static void test() {

//BREAKPOINT before test
    Commons.printLog("***** start of testing *****");

//    Screen scr = new Screen();

//    Debug.on(3);
//    Image img1 = new Image(scr.userCapture());
//    Image img2 = new Image(scr.userCapture());
//    scr.wait(5.0);
//    Match match = scr.waitBest(5, img1, img2);
//    if (null != match) {
//      match.highlight(2);
//    } else {
//      scr.highlight(-2);
//    }
//    List<Match> matches = scr.waitAny(5, img1, img2);
//    if (matches.size() > 0) {
//      for (Match m : matches) {
//        m.highlight(1);
//      }
//    } else {
//      scr.highlight(-2);
//    }

//TEST: ImagePath revision
//    Debug.on(3);
//    File workDir = Commons.getWorkDir();
//    String classes = new File(workDir, "target/classes/images").getAbsolutePath();
//    ImagePath.setBundlePath(classes);
//    ImagePath.add(classes);
//    String jar = "target/sikulixapi-2.0.5.jar";
//    File fJar = new File(jar);
//    ImagePath.addJar(jar, "images");
//    ImagePath.add(classes);
//    ImagePath.addJar(jar, "images");
//    ImagePath.dump(0);

//TEST: SX.pop... feature should return null, if timed out
//    Object feedback = SX.popup("test timeout", 5);
//    Commons.printLog("popup returned %s", feedback);

//TEST: find(Image.getSub()) did not work always (BufferedImage problem)
//solution: make sub same type as original
//    Image image = new Image(scr.userCapture());
//    try {
//      scr.find(image).highlight(2);
//      Image subImage = image.getSub(Region.WEST); //
//      scr.find(subImage).highlight(2);
//    } catch (FindFailed findFailed) {
//      Commons.printLog("not found: %s", image);
//    }

//TEST: macOS S & P behavior
//    new Screen();

//TEST: sim value can be float
//    Settings.MinSimilarity = 0.6f;
//    Pattern pat = new Pattern("someImage");
//    Commons.printLog("%s", pat);
//    pat = new Pattern("someImage").similar(0.6f);
//    Commons.printLog("%s", pat);

//BREAKPOINT after test
    Commons.printLog("***** end of testing *****");
  }

  private static void testFolderList() {
    try {
      Class<?> aClass = Class.forName("net.sourceforge.tess4j.Tesseract");
      String folder = "/tessdata";
      aClass = JythonSupport.class;
//      folder = "LibJython";
//      folder = ".";
//      List<String> fileList = Commons.getFileList(folder, aClass);
      List<String> fileList = Commons.getFileList("<appdata>/SikulixLibs");
      fileList = null;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void testReadContent() {
    List<String> contentList = Commons.getContentList("LibJython", JythonSupport.class);
    String log = Commons.getLog();
  }
}
