/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.natives;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.App;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LinuxUtil implements OSUtil {

  private static boolean wmctrlAvail = true;
  private static boolean xdoToolAvail = true;

  private String[] wmctrlLine = new String[0];

  @Override
  public void checkFeatureAvailability() {
    List<CommandLine> commands = Arrays.asList(
            CommandLine.parse("wmctrl -m"),
            CommandLine.parse("xdotool -v")
    );
    for (CommandLine cmd : commands) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      String commandstring = StringUtils.toString(cmd.toStrings(), " ");

      try {
        DefaultExecutor executor = new DefaultExecutor();
        // other return values throw exception
        executor.setExitValue(0);
        //save system output
        executor.setStreamHandler(new PumpStreamHandler(outputStream));
        executor.execute(cmd);
      } catch (ExecuteException e) {
        // it ran, but exited with non-zero status -- accept
        Debug.info("App: command %s ran, but failed: `%s'. Hoping for the best",
                commandstring, e.toString());
      } catch (IOException e) {
        String executable = cmd.toStrings()[0];
        if (executable.equals("wmctrl")) {
          wmctrlAvail = false;
        }
        if (executable.equals("xdotool")) {
          xdoToolAvail = false;
        }
        Debug.error("App: command %s is not executable, the App features will not work", executable);
      } finally {
        logCommandSysout(commandstring, outputStream);
      }
    }
  }

  private static void logCommandSysout(String commandstring, ByteArrayOutputStream outputStream) {
    //try to create some useful error output
    if (outputStream.size() > 0) {
      Debug.log(4, "command '" + commandstring + "' output:\n" + outputStream.toString());
    }
  }

  private boolean isAvailable(boolean module, String cmd, String feature) {
    if (module) {
      return true;
    }
    Debug.error("%s: feature %s: not available or not working", cmd, feature);
    return false;
  }

  @Override
  public App.AppEntry getApp(int appPID, String appName) {
    return new App.AppEntry(appName, "" + appPID, "", "", "");
  }

  @Override
  public int isRunning(App.AppEntry app) {
    int pid;
    if (app == null) {
      return -1;
    } else if (app.pid >= 0) {
      pid = app.pid;
    } else if (app.name == null || app.name.isEmpty()) {
      return -1;
    } else {
      pid = this.findWindowPID(app.name, 0);
    }
    //save system output
    final DefaultExecutor executor = new DefaultExecutor();
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    executor.setStreamHandler(new PumpStreamHandler(outputStream));
    final CommandLine command = CommandLine.parse("ps -p " + pid);
    try {
      //check if PID exists, if not throw exception
      executor.execute(command);
      return 1;
    } catch (IOException e) {
      System.out.println("[error] App.isRunning: '" + command + "'" + e.getMessage());
      logCommandSysout(command.toString(), outputStream);
      return -1;
    }
  }

  @Override
  public int open(String appName) {
    try {
      String cmd[] = {"sh", "-c", "(" + appName + ") &\necho -n $!"};
      Process p = Runtime.getRuntime().exec(cmd);

      InputStream in = p.getInputStream();
      byte pidBytes[] = new byte[64];
      int len = in.read(pidBytes);
      String pidStr = new String(pidBytes, 0, len);
      int pid = Integer.parseInt(pidStr);
      p.waitFor();
      return pid;
      //return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] openApp:\n" + e.getMessage());
      return 0;
    }
  }

  @Override
  public int open(App.AppEntry app) {
    return open(app.execName);
  }

  @Override
  public int switchto(String appName, int winNum) {
    int windowPID = findWindowPID(appName, winNum);
//    if (windowPID > 1) {
//      return switchto(windowPID, winNum);
//    }
    if (windowPID > -1 && wmctrlLine.length > 0) {
      return bringWindowToFront(wmctrlLine[0], windowPID);
    }
    System.err.println("[error] switchApp: could not identify process with search name '" + appName + "'");
    return -1;
  }

  @Override
  public int switchto(String appName) {
    return switchto(appName, 0);
  }

  @Override
  public int switchto(App.AppEntry app, int num) {
    if (app.pid > 0) {
      return switchto(app.pid, num);
    }
    return switchto(app.execName, num);
  }

  @Override
  public int close(String appName) {
    try {
      //on the success exit value = 0 -> so no exception will be thrown
      CommandExecutorResult result1 = CommandExecutorHelper.execute("pidof " + appName, 0);
      String pid = result1.getStandardOutput();
      if (pid == null || pid.isEmpty()) {
        throw new CommandExecutorException("No app could be found with Name '" + appName + "'");
      }
      //use kill incase that killall could maybe not work in all environments
      return CommandExecutorHelper.execute("kill " + pid, 0).getExitValue();
    } catch (Exception e) {
      //try to search for the appName
      Integer windowPID = findWindowPID(appName, 1);
      if (windowPID > 1) {
        try {
          return CommandExecutorHelper.execute("kill " + windowPID.toString(), 0).getExitValue();
        } catch (Exception e1) {
          e.addSuppressed(e1);
        }
      }
      System.out.println("[error] closeApp:\n" + e.getMessage());
      return -1;
    }
  }

  @Override
  public int close(App.AppEntry app) {
    if (app.pid > 0) {
      return close(app.pid);
    }
    return close(app.execName);
  }

  @Override
  public Map<Integer, String[]> getApps(String name) {
    return null;
  }

  @Override
  public Rectangle getFocusedWindow() {
    if (!isAvailable(xdoToolAvail, "getFocusedWindow", "xdoTool")) {
      return null;
    }
    String cmd[] = {"xdotool", "getactivewindow"};
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      InputStream in = p.getInputStream();
      BufferedReader bufin = new BufferedReader(new InputStreamReader(in));
      String str = bufin.readLine();
      long id = Integer.parseInt(str);
      String hexid = String.format("0x%08x", id);
      return findRegion(hexid, 0, SearchType.WINDOW_ID);
    } catch (IOException e) {
      System.out.println("[error] getFocusedWindow:\n" + e.getMessage());
      return null;
    }
  }

  @Override
  public Rectangle getWindow(String appName) {
    return getWindow(appName, 0);
  }

  private Rectangle findRegion(String appName, int winNum, SearchType type) {
    String[] winLine = findWindow(appName, winNum, type);
    if (winLine != null && winLine.length >= 7) {
      int x = new Integer(winLine[3]);
      int y = Integer.parseInt(winLine[4]);
      int w = Integer.parseInt(winLine[5]);
      int h = Integer.parseInt(winLine[6]);
      return new Rectangle(x, y, w, h);
    }
    return null;
  }

  private String[] findWindow(String appName, int winNum, SearchType type) {
    String[] found = {};
    int numFound = 0;
    try {
      CommandExecutorResult result = CommandExecutorHelper.execute("wmctrl -lpGx", 0);

      int slash = appName.lastIndexOf("/");
      if (slash >= 0) {
        // remove path: /usr/bin/....
        appName = appName.substring(slash + 1);
      }

      if (type == SearchType.APP_NAME) {
        appName = appName.toLowerCase();
      }
      String[] lines = result.getStandardOutput().split("\\n");
      for (String str : lines) {
        //Debug.log("read: " + str);
        String winLine[] = str.split("\\s+", 10);
        boolean ok = false;

        if (type == SearchType.WINDOW_ID) {
          if (appName.equals(winLine[0])) {
            ok = true;
          }
        } else if (type == SearchType.PID) {
          if (appName.equals(winLine[2])) {
            ok = true;
          }
        } else if (type == SearchType.APP_NAME) {
          String winLineName = winLine[7].toLowerCase();
          if (appName.equals(winLineName)) {
            ok = true;
          }

          if (!ok && winLine[9].toLowerCase().contains(appName)) {
            ok = true;
          }
        }

        if (ok) {
          if (numFound >= winNum) {
            //Debug.log("Found window" + winLine);
            found = winLine;
            break;
          }
          numFound++;
        }
      }
    } catch (Exception e) {
      System.out.println("[error] findWindow:\n" + e.getMessage());
      return null;
    }
    return found;
  }

  /**
   * Returns a PID of the givenAppname and the winNumber
   *
   * @param appName
   * @param winNum
   * @return the PID or -1 on errors
   */
  protected int findWindowPID(String appName, int winNum) {
    wmctrlLine = new String[0];
    String[] windowLine = findWindow(appName, winNum, SearchType.APP_NAME);
    if (windowLine != null && windowLine.length > 1) {
      wmctrlLine = windowLine;
      return Integer.parseInt(windowLine[2]);
    }
    return -1;
  }

  @Override
  public Rectangle getWindow(String appName, int winNum) {
    return findRegion(appName, winNum, SearchType.APP_NAME);
  }

  @Override
  public Rectangle getWindow(int pid) {
    return getWindow(pid, 0);
  }

  @Override
  public Rectangle getWindow(int pid, int winNum) {
    return findRegion("" + pid, winNum, SearchType.PID);
  }

  @Override
  public int close(int pid) {
    if (!isAvailable(wmctrlAvail, "closeApp", "wmctrl")) {
      return -1;
    }
    String winLine[] = findWindow("" + pid, 0, SearchType.PID);
    if (winLine == null) {
      return -1;
    }
    String cmd[] = {"wmctrl", "-ic", winLine[0]};
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      return p.exitValue();
    } catch (Exception e) {
      System.out.println("[error] closeApp:\n" + e.getMessage());
      return -1;
    }
  }

  @Override
  public int switchto(int pid, int num) {
    if (!isAvailable(wmctrlAvail, "switchApp", "wmctrl")) {
      return -1;
    }
    String winLine[] = findWindow("" + pid, num, SearchType.PID);
    if (winLine == null || winLine.length < 1) {
      System.err.println("[error] switchApp: window of PID '" + pid + "' couldn't be found!");
      return -1;
    }
/*
    try {
      // execute wmctrl with hex, e.g. 'wmctrl -ia 0x00000'
      CommandExecutorHelper.execute("wmctrl -ia " + winLine[0], 0);
      //on the success exit value = 0 -> so no exception will be thrown
      return pid;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("[error] switchApp:\n" + e.getMessage());
      return -1;
    }
*/
    return bringWindowToFront(winLine[0], pid);
  }

  private int bringWindowToFront(String windowID, int pid) {
    try {
      // execute wmctrl with hex, e.g. 'wmctrl -ia 0x00000'
      CommandExecutorHelper.execute("wmctrl -ia " + windowID, 0);
      //on the success exit value = 0 -> so no exception will be thrown
      return pid;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("[error] switchApp:\n" + e.getMessage());
      return -1;
    }
  }

  @Override
  public void bringWindowToFront(Window win, boolean ignoreMouse) {
  }

  private enum SearchType {

    APP_NAME,
    WINDOW_ID,
    PID
  }
}
