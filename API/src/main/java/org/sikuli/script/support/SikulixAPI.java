package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.*;
import org.sikuli.script.runners.JavaScriptRunner;
import org.sikuli.script.runners.ServerRunner;
import org.sikuli.util.SikulixTest;
import py4Java.GatewayServer;

import java.io.File;
import java.security.CodeSource;
import java.util.List;

public class SikulixAPI {

  public static void main(String[] args) {

    RunTime.afterStart(RunTime.Type.API, args);

    if (args.length == 1 && "test".equals(args[0])) {
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
    }
    Sikulix.terminate();
  }
}
