/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.basics.FileManager;
import org.sikuli.script.Sikulix;
import org.sikuli.script.TextRecognizer;
import org.sikuli.script.runners.JavaScriptRunner;

import java.io.File;

public class SikulixAPI {

  public static void main(String[] args) {

    RunTime.afterStart(RunTime.Type.API, args);

    if (args.length == 2 && "test".equals(args[1])) {
      String version = RunTime.get().getVersion();
      File lastSession = new File(RunTime.get().fSikulixStore, "LastAPIJavaScript.js");
      String runSomeJS = "";
      if (lastSession.exists()) {
        runSomeJS = FileManager.readFileToString(lastSession);
      }
      runSomeJS = Sikulix.inputText("enter some JavaScript (know what you do - may silently die ;-)"
              + "\nexample: run(\"git*\") will run the JavaScript showcase from GitHub"
              + "\nWhat you enter now will be shown the next time.",
          "API::JavaScriptRunner " + version, 10, 60, runSomeJS);
      if (runSomeJS == null || runSomeJS.isEmpty()) {
        Sikulix.popup("Nothing to do!", version);
      } else {
        while (null != runSomeJS && !runSomeJS.isEmpty()) {
          FileManager.writeStringToFile(runSomeJS, lastSession);
          Runner.getRunner(JavaScriptRunner.class).runScript(runSomeJS, null, null);
          runSomeJS = Sikulix.inputText("Edit the JavaScript and/or press OK to run it (again)\n"
                  + "Press Cancel to terminate",
              "API::JavaScriptRunner " + version, 10, 60, runSomeJS);
        }
      }
      RunTime.terminate();
    }

    RunTime runtime = RunTime.get();

    TextRecognizer.start();

    System.out.println("SikuliX API: nothing to do");
    RunTime.terminate();
  }
}
