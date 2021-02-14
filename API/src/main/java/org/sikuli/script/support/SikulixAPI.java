/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.HotkeyEvent;
import org.sikuli.basics.HotkeyListener;
import org.sikuli.basics.HotkeyManager;
import py4j.GatewayServer;

public class SikulixAPI {

  public static void main(String[] args) {

    RunTime.afterStart(RunTime.Type.API, args);

/*
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
*/

    //RunTime runtime = RunTime.get();

    if (RunTime.shouldRunPythonServer()) {
      GatewayServer pythonServer = null;
      if (!RunTime.isRunningPythonServer()) {
        try {
          Class.forName("py4j.GatewayServer");
          pythonServer = new py4j.GatewayServer();
        } catch (ClassNotFoundException e) {
          System.out.println("[ERROR] Python server: py4j not on classpath");
          RunTime.terminate();
        }
        Debug.reset();
        Debug.on(3);
        RunTime.installStopHotkeyPythonServer();
        RunTime.setPythonServer(pythonServer);
        int port = pythonServer.getPort();
        System.out.println("Running py4j server at port " + port);
        RunTime.pythonServer.start(false);
      }
    } else {
      System.out.println("SikuliX API: nothing to do");
    }
    RunTime.terminate();
  }
}
