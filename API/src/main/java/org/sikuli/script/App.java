/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.sikuli.basics.Debug;
import org.sikuli.natives.OSUtil;
import org.sikuli.natives.OSUtil.OsProcess;
import org.sikuli.natives.OSUtil.OsWindow;
import org.sikuli.natives.SysUtil;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
 * App implements features to manage (open, switch to, close) applications. on
 * the system we are running on and to access their assets like windows <br>
 * TAKE CARE: function behavior differs depending on the running system (cosult
 * the docs for more info)
 *
 * @author rhocke
 * @author mbalmer
 */
public class App {

  // <editor-fold defaultstate="collapsed" desc="00 housekeeping">
  private static OSUtil osUtil = SysUtil.getOSUtil();
//  private static final Map<Type, String> appsWindows;
//  private static final Map<Type, String> appsMac;
//  private static final Region aRegion = new Region();

  static {
//    appsWindows = new HashMap<Type, String>();
//    appsWindows.put(Type.EDITOR, "Notepad");
//    appsWindows.put(Type.BROWSER, "Google Chrome");
//    appsWindows.put(Type.VIEWER, "");
//    appsMac = new HashMap<Type, String>();
//    appsMac.put(Type.EDITOR, "TextEdit");
//    appsMac.put(Type.BROWSER, "Safari");
//    appsMac.put(Type.VIEWER, "Preview");
  }

  private static boolean shouldLog = false;

  public static void log(String msg, Object... args) {
    if (shouldLog) {
      Debug.print("[AppLog] " + msg, args);
    }
  }

  public static void error(String msg, Object... args) {
    Debug.print("[AppError] " + msg, args);
  }

  public static void logOn() {
    shouldLog = true;
  }

  public static void logOff() {
    shouldLog = false;
  }
  // </editor-fold>

  //<editor-fold desc="01 instance">
  private static class NullProcess extends OsProcess {

    @Override
    public long getPid() {
      return -1;
    }

    @Override
    public String getExecutable() {
      return "NotRunning";
    }

    @Override
    public boolean isRunning() {
      return false;
    }

    @Override
    public boolean close(boolean force) {
      return false;
    }
  }

  private OsProcess process = null;
  private CommandLine cmd = null;

  public void reset() {
    process = new NullProcess();
  }

  private static int globalMaxWait = 10;
  private int maxWait;

  public App() {
    process = new NullProcess();
    maxWait = globalMaxWait;
  }

  public App(String name) {
    this();
    setName(name);
  }

  protected App(OsProcess process) {
    this.process = process;
  }

  public static App withPID(long pid) {
    OsProcess process = osUtil.findProcesses(pid).stream().findFirst().orElse(new NullProcess());
    if (null == process) {
      return null;
    }
    App app = new App(process);
    return app;
  }

  private String workDir = "";

  public boolean setWorkDir() {
    if (cmd.getExecutable().isEmpty()) {
      return false;
    }
    this.workDir = new File(cmd.getExecutable()).getParentFile().getAbsolutePath();
    return true;
  }

  public boolean setWorkDir(String workDirPath) {
    if (workDirPath == null || workDirPath.isEmpty()) {
      return false;
    }
    File fWorkDir = new File(workDirPath);
    if (fWorkDir.isAbsolute() && fWorkDir.isDirectory()) {
      this.workDir = fWorkDir.getAbsolutePath();
    }
    return false;
  }

  public String getWorkDir() {
    return workDir;
  }

  @Override
  public String toString() {
    final String windowTitle = getTitle().isEmpty() ? "" : " (" + getTitle() + ")";
    String givenExecutable = getGivenExecutable().isEmpty() ? "" : getGivenExecutable();
    String arguments = getArguments().isEmpty() ? "" : getArguments();
    String executable = getExecutable();
    return String.format("[%d:%s%s] %s %s", getPID(), executable, windowTitle, givenExecutable, arguments);
  }

  public void print() {
    System.out.println(this);
    for (Region win : windows()) {
      System.out.println("     " + win);
    };
  }
  //</editor-fold>

  // <editor-fold desc="02 running/valid">
  public boolean isValid() {
    return process.getPid() > 0;
  }

  public boolean isRunning() {
    return isRunning(0);
  }

  public boolean isRunning(int maxTime) {
    if (isClosing()) {
      return false;
    }
    do {
      if (process.isRunning()) {
        return true;
      }
      pause(1);
    } while (--maxTime > 0);
    return false;
  }

  public static List<App> allWithWindow() {
    List<App> apps = new ArrayList<>();
    List<OsProcess> processes = osUtil.getProcesses().stream()
        .filter((p) -> osUtil.isUserApp(p))
        .collect(Collectors.toList());
    for (OsProcess proc : processes) {
      App app = new App(proc);
      if (app.hasWindows()) {
        apps.add(app);
      }
    }
    return apps;
  }

  public static List<OSUtil.OsWindow> allWindows() {
    return osUtil.getWindows();
  }

  public static List<OSUtil.OsWindow> allAppWindows() {
    return osUtil.getAppWindows();
  }

  public boolean hasWindows() {
    return windows().size() > 0;
  }
  // </editor-fold>

  //<editor-fold desc="05 arguments, name, executable, pid">
  public String getGivenExecutable() {
    if (cmd != null) {
      return cmd.getExecutable();
    } else {
      return "";
    }
  }

  /**
   * Use setArguments() instead
   */
  @Deprecated
  public App setUsing(String options) {
    return setArguments(options);
  }

  public App setArguments(String arguments) {
    this.cmd = CommandLine.parse("\"" + cmd.getExecutable() + "\" " + arguments);
    return this;
  }

  /**
   * Use getArguments() insead
   */
  @Deprecated
  public String getOptions() {
    return getArguments();
  }

  public String getArguments() {
    if (cmd != null) {
      return String.join(" ", cmd.getArguments());
    } else {
      return "";
    }
  }

  public void setName(String name) {
    if (StringUtils.isBlank(name)) {
      return;
    }

    // Use apache.commons.exec.CommandLine to correctly tokenize given command.
    cmd = CommandLine.parse(name);
    String executable = cmd.getExecutable();
    process = osUtil.findProcesses(executable).stream().findFirst().orElse(new NullProcess());
  }

  public String getExecutable() {
    return process.getExecutable();
  }

  public long getPID() {
    return process.getPid();
  }
  //</editor-fold>

  //<editor-fold desc="20 open">

  /**
   * tries to open an app using the given name and waits waitTime for being ready
   * If the app is already open, it is brought to foreground
   *
   * @param name     - something that could be used on commandline to start the
   *                 app
   * @param waitTime to wait for app to open (secs)
   * @return the App instance
   */
  public static App open(String name, int waitTime) {
    App app = new App(name);
    app.openAndWait(waitTime);
    return app;
  }

  /**
   * tries to open an app using the given name and waits 1 sec for being ready If
   * the app is already open, it is brought to foreground
   *
   * @param name - something that could be used on commandline to start the app
   * @return the App instance
   */
  public static App open(String name) {
    return open(name, globalMaxWait);
  }

  /**
   * tries to open the app defined by this App instance<br>
   * do not wait for the app to get running
   *
   * @return this or null on failure
   */
  public boolean open() {
    return openAndWait(maxWait);
  }

  /**
   * tries to open the app defined by this App instance<br>
   * and waits until app is running
   *
   * @param waitTime max waittime until running
   * @return this or null on failure
   */
  public boolean open(int waitTime) {
    return openAndWait(waitTime);
  }

  private AtomicBoolean opening = new AtomicBoolean(false);

  private boolean openAndWait(int waitTime) {
    if (!waitUntilClosed(waitTime)) {
      log("App.open: app is still closing (after %d seconds): %s", waitTime, this);
      return false;
    }
    if (!isRunning()) {
      if (opening.compareAndExchange(false, true)) {
        log("App.open: already opening: %s", this);
        return false;
      }
      process = osUtil.open(cmd.toStrings(), workDir, waitTime);
      opening.set(false);
      if (process != null) {
        return true;
      } else {
        process = new NullProcess();
        return false;
      }
    } else {
      log("App.open: already running: %s", this);
      return focus();
    }
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="23 close">
  private AtomicBoolean closing = new AtomicBoolean(false);

  public boolean isClosing() {
    return closing.get();
  }

  public boolean waitUntilClosed(int waitTime) {
    if (!isClosing()) {
      return true;
    }
    do {
      pause(1);
      if (!isClosing()) {
        return true;
      }
    } while (--waitTime > 0);
    return false;
  }

  /**
   * tries to identify a running app with the given name and then tries to close
   * it
   *
   * @param appName name
   * @return 0 for success -1 otherwise
   */
  public static boolean close(String appName) {
    return new App(appName).close();
  }

  /**
   * tries to close the app defined by this App instance, waits max 10 seconds for
   * the app to no longer be running
   *
   * @return this or null on failure
   */
  public boolean close() {
    return close(maxWait);
  }

  /**
   * tries to close the app defined by this App instance, waits max given seconds
   * for the app to no longer be running
   *
   * @param waitTime to wait for app to close (secs)
   * @return this or null on failure
   */
  public boolean close(int waitTime) {
    if (!isRunning()) {
      log("App.close: not running: %s", this);
      return false;
    }

    if (closing.compareAndExchange(false, true)) {
      log("App.close: already closing: %s", this);
      return false;
    }

    try {
      // try to close gracefully
      boolean success = process.close(false);

      // Close request was successful.
      // Wait for the app to die
      if (success) {
        do {
          pause(1);

          if (!isRunning()) {
            log("App.close: Closed gracefully: %s", this);
            return true;
          }
          waitTime--;
        } while (waitTime > 0);
      }

      log("App.close: Closing app gracefully failed. Trying to close forcefully: %s", this);
      process.close(true);

      if (!isRunning()) {
        log("App.close: Closed forcefully: %s", this);
        return true;
      }

      log("App.close: did not work: %s", this);
      return false;
    } finally {
      closing.set(false);
    }

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
    if (Commons.runningWindows()) {
      window().type(Key.F4, Key.ALT);
    } else if (Commons.runningMac()) {
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
  // </editor-fold>

  //<editor-fold desc="26 focus">
  public static App focusedApp() {
    final OsProcess process = osUtil.getFocusedProcess();
    if (process == null) {
      return new App(new NullProcess());
    } else {
      return new App(process);
    }
  }

  /**
   * tries to identify a running app with name or (part of) window title bringing
   * its topmost window to front
   *
   * @param title name
   * @return an App instance - is invalid and not useable if not found as running
   */
  public static App focus(String title) {
    return focus(title, 0);
  }

  /**
   * tries to identify a running app with name or (part of) window title bringing
   * its window with given index to front
   *
   * @param index of the window among the found windows
   * @param title name
   * @return an App instance - is invalid and not useable if not found as running
   */
  public static App focus(String title, int index) {
    List<OsWindow> windows = osUtil.findWindows(title);

    int actual = -1;
    if (windows.size() > index) {
      actual = index;
    } else if (windows.size() > 0) {
      actual = windows.size() - 1;
    }
    if (actual > -1) {
      App app = new App(windows.get(actual).getProcess());
      app.toFront(title);
      return app;
    }

    App app = new App(title);
    if (app.isRunning()) {
      app.focus();
      return app;
    }

    error("App.focus: no window nor app: %s", title);
    return new App();
  }

  /**
   * tries to focus this applications top window
   *
   * @return true on succes, false otherwise
   */
  public boolean focus() {
    Region region = toFront();
    return !region.isEmpty();
  }

  public Region toFront(Object... args) {
    OsWindow window = null;
    int winNum = 0;
    String title = "";
    List<OsWindow> windows = null;
    Object arg;
    if (args.length > 0) {
      arg = args[0];
    } else {
      arg = 0;
    }
    if (arg instanceof Integer) {
      winNum = Math.max(0, (Integer) arg);
    } else if (arg instanceof String) {
      title = (String) arg;
      windows = osUtil.getWindows(process);
      for (OsWindow win : windows) {
        if (win.getTitle().toUpperCase().contains(title.toUpperCase())) {
          break;
        }
        winNum++;
      }
      if (winNum == windows.size()) {
        winNum = 0;
      }
    }
    if (null == windows) {
      windows = osUtil.getWindows(process);
    }
    if (windows.size() > 0) {
      winNum = Math.min(winNum, windows.size() - 1);
      window = windows.get(winNum);
      window.focus(winNum); //TODO macOS trick: window not directly focusable
    }
    return asRegion(window);
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="30 window">
  public List<Region> windows() {
    return osUtil.getWindows(process).stream().map((w) -> asRegion(w.getBounds(), w.getTitle())).collect(Collectors.toList());
  }

  /**
   * evaluates the region currently occupied by the topmost window of this App
   * instance. The region might not be fully visible, not visible at all or
   * invalid with respect to the current monitor configuration (outside any
   * screen)
   *
   * @return the region
   */
  public Region window() {
    return window(0);
  }

  /**
   * evaluates the region currently occupied by the window with the given number
   * of this App instance. The region might not be fully visible, not visible at
   * all or invalid with respect to the current monitor configuration (outside any
   * screen)
   *
   * @param winNum window
   * @return the region
   */

  public Region window(int winNum) {
    List<OsWindow> windows = osUtil.getWindows(process);
    if (windows.size() > 0) {
      return asRegion(windows.get(Math.max(0, Math.min(windows.size() - 1, winNum))));
    }
    return asNullRegion();
  }

  public String getTitle() {
    return getWindowTitle();
  }

  public String getTitle(int windowNumber) {
    return getWindowTitle(windowNumber);
  }

  public String getWindowTitle() {
    return getWindowTitle(0);
  }

  public String getWindowTitle(int windowNumber) {
    Region window = window(windowNumber);
    if (window != null) {
      return window.getName();
    }
    return "";
  }

  /**
   * evaluates the region currently occupied by the systemwide frontmost window
   * (usually the one that has focus for mouse and keyboard actions)
   *
   * @return the region
   */
  public static Region focusedWindow() {
    OsWindow window = osUtil.getFocusedWindow();
    if (window != null) {
      return asRegion(window.getBounds(), window.getTitle());
    } else {
      return asNullRegion();
    }
  }

  private static Region asNullRegion() {
    return asRegion(new Rectangle(0, 0, 0, 0), "NullWindow");
  }

  private static boolean isNullRegion(Region region) {
    return region.isEmpty();
  }

  private static Region asRegion(OsWindow window) {
    if (window == null) {
      return asNullRegion();
    }
    return asRegion(window.getBounds(), window.getTitle());
  }

  private static Region asRegion(Rectangle r) {
    return asRegion(r, "");
  }

  private static Region asRegion(Rectangle r, String title) {
    if (r != null) {
      final Region reg = new Region(r, true);
      if (title != null && !title.isEmpty()) {
        reg.setName(title);
      }
      return reg;
    } else {
      return null;
    }
  }
  //</editor-fold>

  //<editor-fold desc="35 minimize/maximize/restore">

  /**
   * tries to minimize this applications top window
   *
   * @return true on succes, false otherwise
   */
  public boolean minimize() {
    if (!isRunning(0)) {
      error("App.minimize: not running: %s", toString());
      return false;
    }

    List<OsWindow> windows = osUtil.getWindows(process);

    if (!windows.isEmpty()) {
      return windows.get(0).minimize();
    }

    error("App.focus: no window for %s", toString());
    return false;
  }

  /**
   * tries to minimize this applications top window
   *
   * @return true on succes, false otherwise
   */
  public boolean maximize() {
    if (!isRunning(0)) {
      error("App.minimize: not running: %s", toString());
      return false;
    }

    List<OsWindow> windows = osUtil.getWindows(process);

    if (!windows.isEmpty()) {
      return windows.get(0).maximize();
    }

    error("App.focus: no window for %s", toString());
    return false;
  }

  /**
   * tries to restore this applications top window.
   *
   * @return true on succes, false otherwise
   */
  public boolean restore() {
    if (!isRunning(0)) {
      error("App.minimize: not running: %s", toString());
      return false;
    }

    List<OsWindow> windows = osUtil.getWindows(process);

    if (!windows.isEmpty()) {
      return windows.get(0).restore();
    }

    error("App.focus: no window for %s", toString());
    return false;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="70 run">
  public static int lastRunReturnCode = -1;
  public static String lastRunStdout = "";
  public static String lastRunStderr = "";
  public static String lastRunResult = "";

  /**
   * the given text is parsed into a String[] suitable for issuing a
   * Runtime.getRuntime().exec(args). quoting is preserved/obeyed. the first item
   * must be an executable valid for the running system.<br>
   * After completion, the following information is available: <br>
   * App.lastRunResult: a string containing the complete result according to the
   * docs of the run() command<br>
   * App.lastRunStdout: a string containing only the output lines that went to
   * stdout<br>
   * App.lastRunStderr: a string containing only the output lines that went to
   * stderr<br>
   * App.lastRunReturnCode: the value, that is returnd as returncode
   *
   * @param cmd the command to run starting with an executable item
   * @return the final returncode of the command execution
   */
  public static int run(String cmd) {
    lastRunResult = RunTime.runcmd(cmd);
    String NL = Commons.runningWindows() ? "\r\n" : "\n";
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
      if (RunTime.runCmdError.equals(res[n])) {
        isError = true;
        continue;
      }
      lastRunStdout += res[n] + NL;
    }
    return lastRunReturnCode;
  }
//</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="80 clipboard">

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
     * Dumps a given text (either String or StringBuffer) into the Clipboard, with a
     * default MIME type
     */
    public static void putText(CharSequence data) throws Exception {
      StringSelection copy = new StringSelection(data.toString());
      getSystemClipboard().setContents(copy, copy);
    }

    /**
     * Dumps a given text (either String or StringBuffer) into the Clipboard with a
     * specified MIME type
     */
    public static void putText(TextType type, Charset charset, TransferType transferType, CharSequence data)
        throws Exception {
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
     * Enumeration for the transferScriptt type property in MIME types (InputStream,
     * CharBuffer, etc.)
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

  //<editor-fold defaultstate="collapsed" desc="90 features based on httpclient">
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
//</editor-fold>">

  public static void pause(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException ex) {
    }
  }

  public static void pause(float seconds) {
    try {
      Thread.sleep((int) (seconds * 1000));
    } catch (InterruptedException ex) {
    }
  }

  // <editor-fold defaultstate="collapsed" desc="95 special app features">
/*
  public static enum Type {

    EDITOR, BROWSER, VIEWER
  }

  public static Region start(Type appType) {
    App app = null;
    Region win;
    try {
      if (Type.EDITOR.equals(appType)) {
        if (Commons.runningMac()) {
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
        if (Commons.runningWindows()) {
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
        if (Commons.runningWindows()) {
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
 */
//</editor-fold>
}
