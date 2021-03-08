/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.HotkeyEvent;
import org.sikuli.basics.HotkeyListener;
import org.sikuli.basics.HotkeyManager;
import py4j.GatewayServer;

public class SikulixAPI {

  public static void main(String[] args) {

    RunTime.afterStart(Commons.Type.API, args);

    System.out.println("SikuliX API: nothing to do");
    RunTime.terminate();
  }

  public static void runPy4jServer() {
    if (!isRunningPythonServer()) {
      try {
        Class.forName("py4j.GatewayServer");
        pythonServer = new py4j.GatewayServer();
      } catch (ClassNotFoundException e) {
        System.out.println("[ERROR] Python server: py4j not on classpath");
        RunTime.terminate();
      }
      Debug.reset();
      HotkeyManager.getInstance().addHotkey("Abort", new HotkeyListener() {
        @Override
        public void hotkeyPressed(HotkeyEvent e) {
          Debug.log(3, "Stop HotKey was pressed");
          if (isRunningPythonServer()) {
            Debug.logp("Python server: trying to stop");
            pythonServer.shutdown();
            pythonServer = null;
            RunTime.terminate();
          }
        }
      });
      int port = pythonServer.getPort();
      info("Running py4j server on port %s", port);
      try {
        pythonServer.start(false);
      } catch (Exception e) {
        error("py4j server already running on port %s --- TERMINATING", port);
        RunTime.terminate();
      }
    }
  }

  private static boolean isRunningPythonServer() {
    return pythonServer != null;
  }

  private static GatewayServer pythonServer = null;


  private static void info(String message, Object... args) {
    System.out.println(String.format("[info] SikulixAPI: " + message, args));
  }

  private static void error(String message, Object... args) {
    System.out.println(String.format("[error] SikulixAPI: " + message, args));
  }
}
