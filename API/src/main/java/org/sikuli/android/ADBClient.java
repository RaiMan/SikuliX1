/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.android;


import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;
import se.vidstige.jadb.AdbServerLauncher;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.Subprocess;

import java.io.File;
import java.util.List;

/**
 * Created by tg44 on 2016. 06. 26..
 * Modified by RaiMan
 */
public class ADBClient {

  private static JadbConnection jadb = null;
  private static boolean shouldStopServer = false;
  private static JadbDevice device = null;
  public static boolean isAdbAvailable = false;
  public static String adbExecBase = "platform-tools/adb";
  public static String adbExec;
  private static String adbFilePath = "adb";

  private static void init(String adbWhereIs) {
    //getConnection(true);
    String adbPath;
    if (jadb == null) {
      if (adbWhereIs == null || adbWhereIs.isEmpty()) {
        adbExec = adbExecBase;
        if (RunTime.get().runningWindows) {
          adbExec += ".exe";
        }
        File fAdbPath = new File(RunTime.get().fSikulixExtensions, "android/" + adbExec);
        adbFilePath = fAdbPath.getAbsolutePath();
        if (!fAdbPath.exists()) {
          adbPath = System.getenv("sikulixadb");
          if (adbPath == null) {
            adbPath = System.getProperty("sikulixadb");
          }
          if (adbPath == null) {
            adbPath = Commons.getWorkDir().getAbsolutePath();
          }
          File adbFile = new File(adbPath, adbExec);
          if (!adbFile.exists()) {
            adbFile = new File(adbPath);
          }
          adbFilePath = adbFile.getAbsolutePath();
        }
      } else {
        adbFilePath = adbWhereIs;
      }
      try {
        new AdbServerLauncher(new Subprocess(), adbFilePath).launch();
        getConnection(false);
        if (jadb != null) {
          isAdbAvailable = true;
          shouldStopServer = true;
          Debug.log(3, "ADBClient: ADBServer started (%s)", adbFilePath);
        } else {
          reset();
        }
      } catch (Exception e) {
        //Cannot run program "adb": error=2, No such file or directory
        if (e.getMessage().startsWith("Cannot run program")) {
          Debug.error("ADBClient: package adb not available: %s", adbFilePath);
        } else {
          Debug.error("ADBClient: ADBServer problem: %s", e.getMessage());
        }
      }
    }
    if (jadb != null) {
      device = getDevice(0);
    }
    if (device != null) {
      Debug.log(3, "ADBClient: init: attached device: serial(%s)", device.getSerial());
    }
  }

  public static JadbDevice getDevice(int id) {
    if (id < 0) {
      return null;
    }
    List<JadbDevice> devices;
    JadbDevice device = null;
    try {
      devices = jadb.getDevices();
      if (devices != null && devices.size() > id) {
        device = devices.get(id);
      } else {
        if (id < 1) {
          Debug.error("ADBClient: init: no devices attached");
        }
      }
    } catch (Exception e) {
    }
    return device;
  }

  public static void reset() {
    device = null;
    jadb = null;
    if (!shouldStopServer) {
      return;
    }
    try {
      Process p = Runtime.getRuntime().exec(new String[]{adbFilePath, "kill-server"});
      p.waitFor();
      Debug.log(3, "ADBClient: ADBServer should be stopped now");
    } catch (Exception e) {
      Debug.error("ADBClient: reset: kill-server did not work");
    }
  }

  public static String getADB() {
    if (isAdbAvailable) {
      return adbFilePath;
    }
    return "";
  }

  private static void getConnection(boolean quiet) {
    if (jadb == null) {
      try {
        jadb = new JadbConnection();
        String jadbHostVersion = jadb.getHostVersion();
        Debug.log(3, "ADBClient: ADBServer connection established (%s)", jadbHostVersion);
      } catch (Exception e) {
        if (!quiet) {
          Debug.error("ADBClient: ADBServer connection not possible: %s", e.getMessage());
        }
        jadb = null;
      }
    }
  }

  public static JadbDevice getDevice(String adbExec) {
    init(adbExec);
    return device;
  }

  //TODO: get device by id

  public boolean isValid() {
    return jadb != null;
  }

  public boolean hasDevices() {
    return device != null;
  }
}
