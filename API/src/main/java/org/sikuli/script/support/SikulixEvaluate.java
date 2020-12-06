package org.sikuli.script.support;

import org.sikuli.script.runnerSupport.JythonSupport;

public class SikulixEvaluate {
  public static void testReadContent() {
    Commons.getContentList("Lib", JythonSupport.class);
    String log = Commons.getLog();
  }
}
