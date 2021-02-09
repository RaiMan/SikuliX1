package org.sikuli.script.support;

import org.sikuli.basics.Settings;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
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


//TEST: macOS S & P behavior
//    new Screen();

//TEST: sim value can be float
//    Settings.MinSimilarity = 0.6f;
//    Pattern pat = new Pattern("someImage");
//    Commons.printLog("%s", pat);
//    pat = new Pattern("someImage").similar(0.6f);
//    Commons.printLog("%s", pat);
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
