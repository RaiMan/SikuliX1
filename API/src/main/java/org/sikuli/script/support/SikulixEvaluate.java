package org.sikuli.script.support;

import org.sikuli.script.runnerSupport.JythonSupport;

import java.util.List;

public class SikulixEvaluate {
  public static void testReadContent() {
    List<String> contentList = Commons.getContentList("LibJython", JythonSupport.class);
    String log = Commons.getLog();
  }
}
