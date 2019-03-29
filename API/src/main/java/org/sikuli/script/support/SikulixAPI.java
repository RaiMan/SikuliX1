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

    Debug.log(3,"Sikulix: starting API");

    if (args.length == 1 && "runtest".equals(args[0])) {
      SikulixTest.main(new String[]{});
      Sikulix.terminate();
    }

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

    if (args.length == 1 && "testlibs".equals(args[0])) {
      TextRecognizer.start();
    }

    if (args.length == 1 && "createlibs".equals(args[0])) {
      Debug.off();
      CodeSource codeSource = Sikulix.class.getProtectionDomain().getCodeSource();
      if (codeSource != null && codeSource.getLocation().toString().endsWith("classes/")) {
        File libsSource = new File(new File(codeSource.getLocation().getFile()).getParentFile().getParentFile(), "src/main/resources");
        for (String sys : new String[]{"mac", "windows", "linux"}) {
          Sikulix.print("******* %s", sys);
          String sxcontentFolder = String.format("sikulixlibs/%s/libs64", sys);
          List<String> sikulixlibs = RunTime.get().getResourceList(sxcontentFolder);
          String sxcontent = "";
          for (String lib : sikulixlibs) {
            if (lib.equals("sikulixcontent")) {
              continue;
            }
            sxcontent += lib + "\n";
          }
          Sikulix.print("%s", sxcontent);
          FileManager.writeStringToFile(sxcontent, new File(libsSource, sxcontentFolder + "/sikulixcontent"));
        }
      }
    }

    Sikulix.terminate();
  }
}
