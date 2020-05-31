/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.natives.OSUtil;
import org.sikuli.natives.SysUtil;
import org.sikuli.script.support.RunTime;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.StatusLine;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpResponseException;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;

/**
 * App implements features to manage (open, switch to, close) applications. on the system we are running on and to
 * access their assets like windows
 * <br>
 * TAKE CARE: function behavior differs depending on the running system (cosult the docs for more info)
 */
public class App {

  private static OSUtil _osUtil = null;
  private static final Map<Type, String> appsWindows;
  private static final Map<Type, String> appsMac;
  private static final Region aRegion = new Region();

  static {
    appsWindows = new HashMap<Type, String>();
    appsWindows.put(Type.EDITOR, "Notepad");
    appsWindows.put(Type.BROWSER, "Google Chrome");
    appsWindows.put(Type.VIEWER, "");
    appsMac = new HashMap<Type, String>();
    appsMac.put(Type.EDITOR, "TextEdit");
    appsMac.put(Type.BROWSER, "Safari");
    appsMac.put(Type.VIEWER, "Preview");
  }

  private boolean isGivenAsWindowTitle = false;
  public boolean isWindow() {
    return isGivenAsWindowTitle;
  }

  private void setGivenAsWindowTitle() {
    isGivenAsWindowTitle = true;
  }

  private void resetGivenAsWindowTitle() {
    isGivenAsWindowTitle = false;
  }

  //  //<editor-fold defaultstate="collapsed" desc="9 features based on org.apache.httpcomponents.httpclient">
//  private static CloseableHttpClient httpclient = null;
//
//  /**
//   * create a HTTP Client (for use of wwGet, ... multiple times as session)
//   * @return true on success, false otherwise
//   */
//  public static boolean wwwStart() {
//    if (httpclient != null) {
//      return true;
//    }
//    httpclient = HttpClients.createDefault();
//    if (httpclient != null) {
//      return true;
//    }
//    return false;
//  }
//
//  /**
//   * stop a started HTTP Client
//   */
//  public static void wwwStop() {
//    if (httpclient != null) {
//      try {
//        httpclient.close();
//      } catch (IOException ex) {
//      }
//      httpclient = null;
//    }
//  }
//
//  /**
//   * issue a http(s) request
//   * @param url a valid url as used in a browser
//   * @return textual content of the response or empty (UTF-8)
//   * @throws IOException
//   */
//  public static String wwwGet(String url) throws IOException {
//    HttpGet httpget = new HttpGet(url);
//    CloseableHttpResponse response = null;
//    ResponseHandler rh = new ResponseHandler() {
//      @Override
//      public String handleResponse(final HttpResponse response) throws IOException {
//        StatusLine statusLine = response.getStatusLine();
//        HttpEntity entity = response.getEntity();
//        if (statusLine.getStatusCode() >= 300) {
//          throw new HttpResponseException(
//                  statusLine.getStatusCode(),
//                  statusLine.getReasonPhrase());
//        }
//        if (entity == null) {
//          throw new ClientProtocolException("Response has no content");
//        }
//        InputStream is = entity.getContent();
//        ByteArrayOutputStream result = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = is.read(buffer)) != -1) {
//          result.write(buffer, 0, length);
//        }
//        return result.toString("UTF-8");
//      }
//    };
//    boolean oneTime = false;
//    if (httpclient == null) {
//      wwwStart();
//      oneTime = true;
//    }
//    Object content = httpclient.execute(httpget, rh);
//    if (oneTime) {
//      wwwStop();
//    }
//    return (String) content;
//  }
//
//  /**
//   * same as wwwGet(), but the content is also saved to a file
//   * @param url a valid url as used in a browser
//   * @param pOut absolute path to output file (overwritten) (if null: bundlePath/wwwSave.txt is taken)
//   * @return textual content of the response or empty (UTF-8)
//   * @throws IOException
//   */
//  public static String wwwSave(String url, String pOut) throws IOException {
//    String content = wwwGet(url);
//    File out = null;
//    if (pOut == null) {
//      out = new File(ImagePath.getBundleFolder(), "wwwSave.txt");
//    } else {
//      out = new File(pOut);
//    }
//    FileManager.writeStringToFile(content, out);
//    return content;
//  }
//  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="8 special app features">
  public static enum Type {

    EDITOR, BROWSER, VIEWER
  }

  public static Region start(Type appType) {
    App app = null;
    Region win;
    try {
      if (Type.EDITOR.equals(appType)) {
        if (RunTime.get().runningMac) {
          app = new App(appsMac.get(appType));
          if (app.window() != null) {
            app.focus();
            aRegion.wait(0.5);
            win = app.window();
            aRegion.click(win);
            aRegion.write("#M.a#B.");
            return win;
          } else {
            app.open();
            win = app.waitForWindow();
            app.focus();
            aRegion.wait(0.5);
            aRegion.click(win);
            return win;
          }
        }
        if (RunTime.get().runningWindows) {
          app = new App(appsWindows.get(appType));
          if (app.window() != null) {
            app.focus();
            aRegion.wait(0.5);
            win = app.window();
            aRegion.click(win);
            aRegion.write("#C.a#B.");
            return win;
          } else {
            app.open();
            win = app.waitForWindow();
            app.focus();
            aRegion.wait(0.5);
            aRegion.click(win);
            return win;
          }
        }
      } else if (Type.BROWSER.equals(appType)) {
        if (RunTime.get().runningWindows) {
          app = new App(appsWindows.get(appType));
          if (app.window() != null) {
            app.focus();
            aRegion.wait(0.5);
            win = app.window();
            aRegion.click(win);
//            aRegion.write("#C.a#B.");
            return win;
          } else {
            app.open();
            win = app.waitForWindow();
            app.focus();
            aRegion.wait(0.5);
            aRegion.click(win);
            return win;
          }
        }
        return null;
      } else if (Type.VIEWER.equals(appType)) {
        return null;
      }
    } catch (Exception ex) {
    }
    return null;
  }

  public Region waitForWindow() {
    return waitForWindow(5);
  }

  public Region waitForWindow(int seconds) {
    Region win = null;
    while ((win = window()) == null && seconds > 0) {
      aRegion.wait(0.5);
      seconds -= 0.5;
    }
    return win;
  }

  public static boolean openLink(String url) {
    if (!Desktop.isDesktopSupported()) {
      return false;
    }
    try {
      Desktop.getDesktop().browse(new URL(url).toURI());
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  private static Region asRegion(Rectangle r) {
    if (r != null) {
      return Region.create(r);
    } else {
      return null;
    }
  }

  public static void pause(int time) {
    try {
      Thread.sleep(time * 1000);
    } catch (InterruptedException ex) {
    }
  }

  public static void pause(float time) {
    try {
      Thread.sleep((int) (time * 1000));
    } catch (InterruptedException ex) {
    }
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="0 housekeeping">
  private static boolean shouldLog = false;
  private String appNameGiven = "";
  private String appName = "";
  private String appToken = "";
  private String appExec = "";
  private String appExecPath = "";
  private String appWorkDir = "";
  private String appOptions = "";
  private String appWindow = "";
  private int appPID = -1;
  private int maxWait = 10;

  public static void log(String msg, Object... args) {
    if (shouldLog) {
      Debug.logp("[AppLog] " + msg, args);
    }
  }

  public static void logOn() {
    shouldLog = true;
  }

  public static void logOff() {
    shouldLog = false;
  }

  public void reset() {
    appPID = -1;
    appWindow = "";
  }

  public App() {
    if (_osUtil == null) {
      _osUtil = SysUtil.getOSUtil();
      _osUtil.checkFeatureAvailability();
    }
  }

  public App(String name) {
    this();
    init(name);
  }

  private void init(String name) {
    if (name.isEmpty()) {
      return;
    }
    appNameGiven = name.trim();
    String[] parts;
    //C:\Program Files\Mozilla Firefox\firefox.exe -- options
    parts = appNameGiven.split(" -- ");
    String possibleAppExec = "";
    if (parts.length > 1) {
      appOptions = parts[1].trim();
      possibleAppExec = parts[0].replace("\"", "").trim();
    } else {
      if (appNameGiven.startsWith("\"")) {
        parts = appNameGiven.substring(1).split("\"");
        if (parts.length > 1) {
          appOptions = appNameGiven.substring(parts[0].length() + 2).trim();
          possibleAppExec = parts[0];
        } else {
          possibleAppExec = appNameGiven.replace("\"", "");
        }
      } else {
        possibleAppExec = appNameGiven;
      }
    }
    File fExec = new File(possibleAppExec);
    if (fExec.isAbsolute()) {
      if (!fExec.exists()) {
        log("App: init: does not exist or not valid: %s", fExec);
      } else {
        appExec = fExec.getAbsolutePath();
        appExecPath = fExec.getParent();
      }
    }
    if (!appExec.isEmpty()) {
      appName = fExec.getName();
    } else {
      if (RunTime.onWindows() || possibleAppExec.startsWith("?")) {
        appName = appNameGiven;
        setGivenAsWindowTitle();
        if (appName.startsWith("?")) {
          appName = appName.substring(1);
        }
      } else if (!RunTime.onWindows()) {
        appExec = possibleAppExec;
        appName = possibleAppExec;
      }
    }
    log("App.create: %s", toString());
  }

  @Override
  public String toString() {
    if (isWindow()) {
      return String.format("[%d:?%s (%s)] %s", appPID, appName, appWindow, appNameGiven);
    } else {
      return String.format("[%d:%s (%s)] %s %s", appPID, appName, appWindow, appExec, appOptions);
    }
  }

  public String toStringShort() {
    if (isWindow()) {
      return String.format("[%d:?%s]", appPID, appName);
    } else {
      return String.format("[%d:%s]", appPID, appName);
    }
  }
  //</editor-fold>

  //<editor-fold desc="7 app list">

  public static List<App> getApps() {
    new App();
    return _osUtil.getApps("");
  }

  public static List<App> getApps(String name) {
    new App();
    return _osUtil.getApps(name);
  }

  public static void listApps() {
    new App();
    List<App> appList = _osUtil.getApps("");
    logOn();
    log("***** all running apps");
    for (App app : appList) {
      if (app.getPID() > 0) {
        log("%s", app);
      }
    }
    log("***** end of list (%d)", appList.size());
    logOff();
  }

  public static void listApps(String name) {
    new App();
    List<App> appList = _osUtil.getApps(name);
    logOn();
    log("***** running apps matching: %s", name);
    for (App app : appList) {
      log("%s", app);
    }
    log("***** end of list (%d)", appList.size());
    logOff();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="6 getter/setter">
  public String getToken() {
    return appToken;
  }

  public void setToken(String appToken) {
    this.appToken = appToken;
  }

  public App setUsing(String options) {
    if (options != null) {
      appOptions = options;
    } else {
      appOptions = "";
    }
    return this;
  }

  public void setNameGiven(String nameGiven) {
    appNameGiven = nameGiven;
  }

  public String getNameGiven() {
    return appNameGiven;
  }

  public String getOptions() {
    return appOptions;
  }

  public String getName() {
    return appName;
  }

  public void setName(String name) {
    appName = name;
  }

  public String getExec() {
    return appExec;
  }

  public void setExec(String exec) {
    appExec = exec;
  }

  public String getTitle() {
    return getWindowTitle();
  }

  public String getTitle(int windowNumber) {
    return getWindowTitle();
  }

  public String getWindowTitle() {
    return appWindow == null ? "" : appWindow;
  }

  public void setWindow(String appWindow) {
    this.appWindow = appWindow;
  }

  public void setPID(int appPID) {
    this.appPID = appPID;
  }

  public void setPID(String appPID) {
    try {
      this.appPID = Integer.parseInt(appPID);
    } catch (NumberFormatException e) {
      reset();
    }
  }

  public int getPID() {
    return appPID;
  }

  public boolean setWorkDir() {
    if (appExecPath.isEmpty()) {
      return false;
    }
    appWorkDir = appExecPath;
    return true;
  }

  public boolean setWorkDir(String workDirPath) {
    if (workDirPath == null || workDirPath.isEmpty()) {
      return false;
    }
    File fWorkDir = new File(workDirPath);
    if (!fWorkDir.isAbsolute() || !fWorkDir.isDirectory() || !fWorkDir.exists()) {
      return false;
    }
    appWorkDir = fWorkDir.getAbsolutePath();
    return true;
  }

  public String getWorkDir() {
    return appWorkDir;
  }
  //</editor-fold>

  //<editor-fold desc="2 running/valid">
  public boolean isValid() {
    return appPID > 0;
  }

  public boolean isRunning() {
    return isRunning(0);
  }

  public boolean isRunning(int maxTime) {
    _osUtil.get(this);
    if (maxTime == 0 && !isValid()) {
      return false;
    }
    while (!isValid() && maxTime > 0) {
      maxTime -= 1;
      pause(1);
      _osUtil.get(this);
    }
    return isValid();
  }

  public boolean hasWindow() {
    if (!isValid()) {
      return false;
    }
    return !getWindowTitle().isEmpty();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="1 open">
  boolean isOpen = false;

  /**
   * tries to open an app using the given name and waits waitTime for being ready
   * If the app is already open, it is brought to foreground
   *
   * @param appName  name - something that could be used on commandline to start the app
   * @param waitTime to wait for app to open (secs)
   * @return the App instance
   */
  public static App open(String appName, int waitTime) {
    App app = new App(appName);
    app.openAndWait(waitTime);
    return app;
  }

  /**
   * tries to open an app using the given name and waits 1 sec for being ready
   * If the app is already open, it is brought to foreground
   *
   * @param appName name - something that could be used on commandline to start the app
   * @return the App instance
   */
  public static App open(String appName) {
    return open(appName, 1);
  }

  /**
   * tries to open the app defined by this App instance<br>
   * do not wait for the app to get running
   *
   * @return this or null on failure
   */
  public boolean open() {
    openAndWait(5);
    return isValid();
  }

  /**
   * tries to open the app defined by this App instance<br>
   * and waits until app is running
   *
   * @param waitTime max waittime until running
   * @return this or null on failure
   */
  public boolean open(int waitTime) {
    openAndWait(waitTime);
    return isValid() && hasWindow();
  }

  private void openAndWait(int waitTime) {
    isOpen = false;
    if (!isRunning(0)) {
      boolean isOpen = _osUtil.open(this);
      if (isOpen) {
        if (!isRunning(waitTime)) {
          log("App.open: not running after %d secs (%s)", waitTime, appNameGiven);
        } else {
          log("App.open: %s", this);
          if (!focus()) {
            while (!hasWindow() && waitTime > 0) {
              waitTime -= 1;
              pause(1);
              focus();
            }
          }
        }
      } else {
        log("App.open: %s: did not work - app not valid", appNameGiven);
      }
    } else {
      log("App.open: already running: %s", this);
      isOpen = true;
      focus();
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="4 close">

  /**
   * tries to identify a running app with the given name and then tries to close it
   *
   * @param appName name
   * @return 0 for success -1 otherwise
   */
  public static boolean close(String appName) {
    return new App(appName).close();
  }

  public boolean isClosing() {
    return isClosing;
  }

  private boolean isClosing = false;

  /**
   * tries to close the app defined by this App instance, waits max 10 seconds for the app to no longer be running
   *
   * @return this or null on failure
   */
  public boolean close() {
    return close(5);
  }

  /**
   * tries to close the app defined by this App instance, waits max given seconds for the app to no longer be running
   *
   * @param waitTime to wait for app to close (secs)
   * @return this or null on failure
   */
  public boolean close(int waitTime) {
    if (!isRunning()) {
      log("App.close: not running: %s", this);
      return false;
    }
    boolean success = _osUtil.close(this);
    if (success) {
      isClosing = true;
      int timeTowait = maxWait;
      if (waitTime > 0) {
        timeTowait = waitTime;
      }
      while (isRunning(0) && timeTowait > 0) {
        pause(1);
        timeTowait--;
      }
    }
    isClosing = false;
    if (isValid() || !success) {
      log("App.close: did not work: %s", this);
      return false;
    }
    log("App.close: %s", this);
    reset();
    return true;
  }

  public int closeByKey() {
    return closeByKey(0);
  }

  public int closeByKey(int waitTime) {
    if (!isRunning()) {
      log("App.closeByKey: not running: %s", this);
      return 1;
    }
    focus();
    RunTime.pause(1);
    if (RunTime.get().runningWindows) {
      window().type(Key.F4, Key.ALT);
    } else if (RunTime.get().runningMac) {
      window().type("q", Key.CMD);
    } else {
      window().type("q", Key.CTRL);
    }
    int timeTowait = maxWait;
    if (waitTime > 0) {
      timeTowait = waitTime;
    }
    while (isRunning(0) && timeTowait > 0) {
      timeTowait--;
    }
    if (!isValid()) {
      log("App.closeByKey: %s", this);
    } else {
      log("App.closeByKey: did not work: %s", this);
      return 1;
    }
    return 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="3 focus">
  private boolean isFocused = false;

  public void setFocused(boolean state) {
    isFocused = state;
  }

  public boolean hasFocus() {
    return isFocused;
  }

  /**
   * tries to identify a running app with name or (part of) window title
   * bringing its topmost window to front
   *
   * @param title name
   * @return an App instance - is invalid and not useable if not found as running
   */
  public static App focus(String title) {
    return focus(title, 0);
  }

  /**
   * tries to identify a running app with name or (part of) window title
   * bringing its window with given index to front
   *
   * @param index of the window among the found windows
   * @param title name
   * @return an App instance - is invalid and not useable if not found as running
   */
  public static App focus(String title, int index) {
    App app = new App(title);
    app.focus();
    return app;
  }

  /**
   * tries to make it the foreground application bringing its frontmost window to front
   *
   * @return the App instance
   */
  public boolean focus() {
    isFocused = false;
    if (!isOpen && !isRunning(0)) {
      log("App.focus: not running: %s", toString());
      return false;
    }
    isOpen = false;
    if (!_osUtil.switchto(this)) {
      log("App.focus: no window for %s", toString());
      return false;
    } else {
      isFocused = true;
      if (isWindow()) {
        resetGivenAsWindowTitle();
      }
      log("App.focus: %s", this);
    }
    return true;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="5 window">

  /**
   * evaluates the region currently occupied by the topmost window of this App instance. The region might not be fully
   * visible, not visible at all or invalid with respect to the current monitor configuration (outside any screen)
   *
   * @return the region
   */
  public Region window() {
    return window(0);
  }

  /**
   * evaluates the region currently occupied by the window with the given number of this App instance. The region might
   * not be fully visible, not visible at all or invalid with respect to the current monitor configuration (outside any
   * screen)
   *
   * @param winNum window
   * @return the region
   */
  public Region window(int winNum) {
    Region windowRegion = null;
    Rectangle windowRect = _osUtil.getWindow(this, winNum);
    if (null != windowRect) {
      windowRegion = asRegion(windowRect);
      if (winNum == 0) {
        windowRegion.setName(getTitle());
      } else {

      }
    }
    return windowRegion;
  }

  /**
   * evaluates the region currently occupied by the systemwide frontmost window (usually the one that has focus for
   * mouse and keyboard actions)
   *
   * @return the region
   */
  public static Region focusedWindow() {
    new App();
    return asRegion(_osUtil.getFocusedWindow());
  }

  public List<Region> getWindows() {
    new App();
    List<Region> regWindows = new ArrayList<>();
    return _osUtil.getWindows(this);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="run">
  public static int lastRunReturnCode = -1;
  public static String lastRunStdout = "";
  public static String lastRunStderr = "";
  public static String lastRunResult = "";

  /**
   * the given text is parsed into a String[] suitable for issuing a Runtime.getRuntime().exec(args). quoting is
   * preserved/obeyed. the first item must be an executable valid for the running system.<br>
   * After completion, the following information is available: <br>
   * App.lastRunResult: a string containing the complete result according to the docs of the run() command<br>
   * App.lastRunStdout: a string containing only the output lines that went to stdout<br>
   * App.lastRunStderr: a string containing only the output lines that went to stderr<br>
   * App.lastRunReturnCode: the value, that is returnd as returncode
   *
   * @param cmd the command to run starting with an executable item
   * @return the final returncode of the command execution
   */
  public static int run(String cmd) {
    lastRunResult = RunTime.get().runcmd(cmd);
    String NL = RunTime.get().runningWindows ? "\r\n" : "\n";
    String[] res = lastRunResult.split(NL);
    try {
      lastRunReturnCode = Integer.parseInt(res[0].trim());
    } catch (Exception ex) {
    }
    lastRunStdout = "";
    lastRunStderr = "";
    boolean isError = false;
    for (int n = 1; n < res.length; n++) {
      if (isError) {
        lastRunStderr += res[n] + NL;
        continue;
      }
      if (RunTime.get().runCmdError.equals(res[n])) {
        isError = true;
        continue;
      }
      lastRunStdout += res[n] + NL;
    }
    return lastRunReturnCode;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="clipboard">

  /**
   * evaluates the current textual content of the system clipboard
   *
   * @return the textual content or empty string if not possible
   */
  public static String getClipboard() {
    Transferable content = null;
    try {
      content = Clipboard.getSystemClipboard().getContents(null);
    } catch (Exception ex) {
      Debug.error("Env.getClipboard: %s", ex.getMessage());
    }
    if (content != null) {
      try {
        if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          return (String) content.getTransferData(DataFlavor.stringFlavor);
        }
      } catch (UnsupportedFlavorException ex) {
        Debug.error("Env.getClipboard: UnsupportedFlavorException: " + content);
      } catch (IOException ex) {
        Debug.error("Env.getClipboard: %s", ex.getMessage());
      }
    }
    return "";
  }

  /**
   * sets the current textual content of the system clipboard to the given text
   *
   * @param text text
   */
  public static void setClipboard(String text) {
    try {
      Clipboard.putText(Clipboard.PLAIN, Clipboard.UTF8, Clipboard.CHAR_BUFFER, text);
    } catch (Exception ex) {
      Debug.error("Env.setClipboard: %s", ex.getMessage());
    }
  }

  private static class Clipboard {

    public static final TextType HTML = new TextType("text/html");
    public static final TextType PLAIN = new TextType("text/plain");

    public static final Charset UTF8 = new Charset("UTF-8");
    public static final Charset UTF16 = new Charset("UTF-16");
    public static final Charset UNICODE = new Charset("unicode");
    public static final Charset US_ASCII = new Charset("US-ASCII");

    public static final TransferType READER = new TransferType(Reader.class);
    public static final TransferType INPUT_STREAM = new TransferType(InputStream.class);
    public static final TransferType CHAR_BUFFER = new TransferType(CharBuffer.class);
    public static final TransferType BYTE_BUFFER = new TransferType(ByteBuffer.class);

    private static java.awt.datatransfer.Clipboard systemClipboard = null;

    private Clipboard() {
    }

    /**
     * Dumps a given text (either String or StringBuffer) into the Clipboard, with a default MIME type
     */
    public static void putText(CharSequence data) throws Exception {
      StringSelection copy = new StringSelection(data.toString());
      getSystemClipboard().setContents(copy, copy);
    }

    /**
     * Dumps a given text (either String or StringBuffer) into the Clipboard with a specified MIME type
     */
    public static void putText(TextType type, Charset charset, TransferType transferType, CharSequence data) throws Exception {
      String mimeType = type + "; charset=" + charset + "; class=" + transferType;
      TextTransferable transferable = new TextTransferable(mimeType, data.toString());
      getSystemClipboard().setContents(transferable, transferable);
    }

    public static java.awt.datatransfer.Clipboard getSystemClipboard() throws Exception {
      if (systemClipboard == null) {
        systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (systemClipboard == null) {
          throw new Exception("Clipboard: Toolkit.getDefaultToolkit().getSystemClipboard() returns null");
        }
      }
      return systemClipboard;
    }

    private static class TextTransferable implements Transferable, ClipboardOwner {

      private String data;
      private DataFlavor flavor;

      public TextTransferable(String mimeType, String data) {
        flavor = new DataFlavor(mimeType, "Text");
        this.data = data;
      }

      @Override
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavor, DataFlavor.stringFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
        boolean b = this.flavor.getPrimaryType().equals(flavor.getPrimaryType());
        return b || flavor.equals(DataFlavor.stringFlavor);
      }

      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.isRepresentationClassInputStream()) {
          return new StringReader(data);
        } else if (flavor.isRepresentationClassReader()) {
          return new StringReader(data);
        } else if (flavor.isRepresentationClassCharBuffer()) {
          return CharBuffer.wrap(data);
        } else if (flavor.isRepresentationClassByteBuffer()) {
          return ByteBuffer.wrap(data.getBytes());
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
          return data;
        }
        throw new UnsupportedFlavorException(flavor);
      }

      @Override
      public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
      }
    }

    /**
     * Enumeration for the text type property in MIME types
     */
    public static class TextType {

      private String type;

      private TextType(String type) {
        this.type = type;
      }

      @Override
      public String toString() {
        return type;
      }
    }

    /**
     * Enumeration for the charset property in MIME types (UTF-8, UTF-16, etc.)
     */
    public static class Charset {

      private String name;

      private Charset(String name) {
        this.name = name;
      }

      @Override
      public String toString() {
        return name;
      }
    }

    /**
     * Enumeration for the transferScriptt type property in MIME types (InputStream, CharBuffer, etc.)
     */
    public static class TransferType {

      private Class dataClass;

      private TransferType(Class streamClass) {
        this.dataClass = streamClass;
      }

      public Class getDataClass() {
        return dataClass;
      }

      @Override
      public String toString() {
        return dataClass.getName();
      }
    }

  }
//</editor-fold>
}
