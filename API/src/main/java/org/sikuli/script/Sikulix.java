/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.vnc.VNCScreen;
import org.sikuli.android.ADBScreen;
import org.sikuli.basics.*;
import org.sikuli.util.JythonHelper;
import org.sikuli.util.ScreenHighlighter;
import org.sikuli.util.SikulixFileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;

/**
 * INTERNAL USE ONLY --- NOT part of official API
 */
public class Sikulix {

  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "Sikulix: " + message, args);
  }

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  private static void terminate(int retVal, String msg, Object... args) {
    p(msg, args);
    System.exit(retVal);
  }

  private static boolean runningFromJar;
  private static String jarPath;
  private static String jarParentPath;
  private static final String prefNonSikuli = "nonSikuli_";
  private static RunTime rt = null;
  public static int testNumber = -1;
  private static boolean shouldRunServer = false;
  private static Point locPopAt = null;

  static {
    String jarName = "";

    CodeSource codeSrc = Sikulix.class.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      jarName = codeSrc.getLocation().getPath();
    }

    if (jarName.contains("sikulixsetupAPI")) {
      JOptionPane.showMessageDialog(null, "Not useable!\nRun setup first!",
              "sikulixsetupAPI", JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }

    rt = RunTime.get();
    if (Debug.getDebugLevel() == 0) {
      Debug.setDebugLevel(1);
    }

    if (codeSrc != null && codeSrc.getLocation() != null) {
      URL jarURL = codeSrc.getLocation();
      jarPath = FileManager.slashify(new File(jarURL.getPath()).getAbsolutePath(), false);
      jarParentPath = (new File(jarPath)).getParent();
      if (jarPath.endsWith(".jar")) {
        runningFromJar = true;
      } else {
        jarPath += "/";
      }
    }
  }

  /**
   * checking parameter -d on commandline<br>
   * 0 - list all available tests<br>
   * 1 - run all available tests<br>
   * n - run the test with that number if available
   *
   * @param args currently only -d is evaluated
   */
  public static void main(String[] args) throws FindFailed {

    if (args.length > 0 && args[0].toLowerCase().startsWith("-s")) {
      shouldRunServer = true;
    } else {
      int dl = RunTime.checkArgs(args, RunTime.Type.API);
      if (dl > -1 && dl < 999) {
        testNumber = dl;
        Debug.on(3);
      } else {
        testNumber = -1;
      }

      testNumber = rt.getOptionNumber("testing.test", testNumber);

      if (dl == 999) {
        int exitCode = Runner.runScripts(args);
        cleanUp(exitCode);
        System.exit(exitCode);
      } else if (testNumber > -1) {
        if (!rt.testing) {
          rt.show();
          rt.testing = true;
        }
        //Tests.runTest(testNumber);
        System.exit(1);
      }
    }
    rt = RunTime.get();
    if (rt.fSxBaseJar.getName().contains("setup")) {
      Sikulix.popError("Not useable!\nRun setup first!");
      System.exit(0);
    }

    if (shouldRunServer) {
      if (RunServer.run(null)) {
        System.exit(1);
      }
    }

    boolean playing = false;
    if (args.length > 0 && "play".equals(args[0])) {
      playing = true;
//-------- playground Observe
//      Region reg = new Region(0,0,80,80);
//      reg.highlight(2);
//      reg.onChange(new ObserverCallBack(){
//        @Override
//        public void changed(ObserveEvent evt) {
//          if (evt.getCount() > 3) {
//            p("in handler: %d - stopping", evt.getCount());
//            evt.stopObserver();
//          } else {
//            p("in handler: %d", evt.getCount());
//          }
//        }
//      });
//      reg.observeInBackground();
//      int n = 0;
//      while (reg.isObserving()) {
//        p("%d - observing", n);
//        reg.wait(1.0);
//        n++;
//      }
//-------- playground VNC
//      VNCScreen vs = vncStart("192.168.2.63", 5900, "vnc", 0, 0);
//      p("%s", vs);
//      vs.wait(3.0);
//      vs.type(Key.UP);
//      vs.click();
//      vs.type("test");
//      vs.type("a", Key.CMD);
//      vs.type("c", Key.CMD);
//      vs.click();
//      vs.type("v", Key.CMD);
//      vs.wait(1.0);
//      vs.userCapture("");
//      vs.stop();
//      vs.wait(3.0);

//      while (true) {
//        String file = popFile("");
//        p("selected: %s", file);
//        if (file.isEmpty()) {
//          break;
//        }
//      }
// -------- playground
    }
    if (playing) {
      System.exit(1);
    }

    if (args.length > 0 && args[0].startsWith("test")) {
      Debug.on(3);
      String test = args[0];
      try {
        Method method = Sikulix.class.getDeclaredMethod(test, new Class[0]);
        log(lvl, "running test %s", test);
        Object ret = method.invoke(null, new Object[0]);
      } catch (Exception e) {
        log(-1, "Problem: %s(%s)", e.getCause(), e.getMessage());
        System.exit(1);
      }
      System.exit(0);
    }

    String version = String.format("(%s-%s)", rt.getVersionShort(), rt.sxBuildStamp);
    File lastSession = new File(rt.fSikulixStore, "LastAPIJavaScript.js");
    String runSomeJS = "";
    if (lastSession.exists()) {
      runSomeJS = FileManager.readFileToString(lastSession);
    }
    runSomeJS = inputText("enter some JavaScript (know what you do - may silently die ;-)"
                    + "\nexample: run(\"git*\") will run the JavaScript showcase from GitHub"
                    + "\nWhat you enter now will be shown the next time.",
            "API::JavaScriptRunner " + version, 10, 60, runSomeJS);
    if (runSomeJS == null || runSomeJS.isEmpty()) {
      popup("Nothing to do!", version);
    } else {
      while (null != runSomeJS && !runSomeJS.isEmpty()) {
        FileManager.writeStringToFile(runSomeJS, lastSession);
        Runner.runjs(null, null, runSomeJS, null);
        runSomeJS = inputText("Edit the JavaScript and/or press OK to run it (again)\n"
                        + "Press Cancel to terminate",
                "API::JavaScriptRunner " + version, 10, 60, runSomeJS);
      }
    }
    System.exit(0);
  }

  /**
   * build a jar on the fly at runtime from a folder.<br>
   * special for Jython: if the folder contains a __init__.py on first level,
   * the folder will be copied to the jar root (hence preserving module folders)
   *
   * @param targetJar    absolute path to the created jar (parent folder must exist, jar is overwritten)
   * @param sourceFolder absolute path to a folder, the contained folder structure
   *                     will be copied to the jar root level
   * @return
   */
  public static boolean buildJarFromFolder(String targetJar, String sourceFolder) {
    log(lvl, "buildJarFromFolder: \nfrom Folder: %s\nto Jar: %s", sourceFolder, targetJar);
    File fJar = new File(targetJar);
    if (!fJar.getParentFile().exists()) {
      log(-1, "buildJarFromFolder: parent folder of Jar not available");
      return false;
    }
    File fSrc = new File(sourceFolder);
    if (!fSrc.exists() || !fSrc.isDirectory()) {
      log(-1, "buildJarFromFolder: source folder not available");
      return false;
    }
    String prefix = null;
    if (new File(fSrc, "__init__.py").exists() || new File(fSrc, "__init__$py.class").exists()) {
      prefix = fSrc.getName();
      if (prefix.endsWith("_")) {
        prefix = prefix.substring(0, prefix.length() - 1);
      }
    }
    return FileManager.buildJar(targetJar, new String[]{null},
            new String[]{sourceFolder}, new String[]{prefix}, null);
  }

  /**
   * the foo.py files in the given source folder are compiled to JVM-ByteCode-classfiles foo$py.class
   * and stored in the target folder (thus securing your code against changes).<br>
   * A folder structure is preserved. All files not ending as .py will be copied also.
   * The target folder might then be packed to a jar using buildJarFromFolder.<br>
   * Be aware: you will get no feedback about any compile problems,
   * so make sure your code compiles error free. Currently there is no support for running such a jar,
   * it can only be used with load()/import, but you might provide a simple script that does load()/import
   * and then runs something based on available functions in the jar code.
   *
   * @param fpSource absolute path to a folder/folder-tree containing the stuff to be copied/compiled
   * @param fpTarget the folder that will contain the copied/compiled stuff (folder is first deleted)
   * @return false if anything goes wrong, true means should have worked
   */
  public static boolean compileJythonFolder(String fpSource, String fpTarget) {
    JythonHelper jython = JythonHelper.get();
    if (jython != null) {
      File fTarget = new File(fpTarget);
      FileManager.deleteFileOrFolder(fTarget);
      fTarget.mkdirs();
      if (!fTarget.exists()) {
        log(-1, "compileJythonFolder: target folder not available\n%", fTarget);
        return false;
      }
      File fSource = new File(fpSource);
      if (!fSource.exists()) {
        log(-1, "compileJythonFolder: source folder not available\n", fSource);
        return false;
      }
      if (fTarget.equals(fSource)) {
        log(-1, "compileJythonFolder: target folder cannot be the same as the source folder");
        return false;
      }
      FileManager.xcopy(fSource, fTarget);
      if (!jython.exec("import compileall")) {
        return false;
      }
      jython = doCompileJythonFolder(jython, fTarget);
      FileManager.traverseFolder(fTarget, new CompileJythonFilter(jython));
    }
    return false;
  }

  private static class CompileJythonFilter implements FileManager.FileFilter {

    JythonHelper jython = null;

    public CompileJythonFilter(JythonHelper jython) {
      this.jython = jython;
    }

    @Override
    public boolean accept(File entry) {
      if (jython != null && entry.isDirectory()) {
        jython = doCompileJythonFolder(jython, entry);
      }
      return false;
    }
  }

  private static JythonHelper doCompileJythonFolder(JythonHelper jython, File fSource) {
    String fpSource = FileManager.slashify(fSource.getAbsolutePath(), false);
    if (!jython.exec(String.format("compileall.compile_dir(\"%s\","
            + "maxlevels = 0, quiet = 1)", fpSource))) {
      return null;
    }
    for (File aFile : fSource.listFiles()) {
      if (aFile.getName().endsWith(".py")) {
        aFile.delete();
      }
    }
    return jython;
  }

  public static boolean isRunningFromJar() {
    return runningFromJar;
  }

  public static String getJarPath() {
    return jarPath;
  }

  public static String getJarParentPath() {
    return jarParentPath;
  }

  private static boolean runningSikulixapi = false;

  /**
   * Get the value of runningSikulixUtilapi
   *
   * @return the value of runningSikulixUtilapi
   */
  public static boolean isRunningSikulixapi() {
    return runningSikulixapi;
  }

  /**
   * Set the value of runningSikulixUtilapi
   *
   * @param runningAPI new value of runningSikulixUtilapi
   */
  public static void setRunningSikulixapi(boolean runningAPI) {
    runningSikulixapi = runningAPI;
  }

  /**
   * call this, to initialize Sikuli up to useability
   *
   * @return the primary screen object or null if headless
   */
  public static Screen init() {
    if (!canRun()) {
      return null;
    }
//TODO collect initializations here
    Mouse.init();
    return new Screen();
  }

  /**
   * Can SikuliX be run on this machine?
   *
   * @return true if not running headless false otherwise
   */
  public static boolean canRun() {
    return !RunTime.get().isHeadless();
  }

  /**
   * INTERNAL USE: convenience function: runs {@link #cleanUp(int)}, prints a message endNormal and terminates with
   * returncode
   *
   * @param n
   */
  public static void endNormal(int n) {
    log(lvl, "endNormal: %d", n);
    cleanUp(n);
    System.exit(n);
  }

  /**
   * INTERNAL USE: convenience function: runs {@link #cleanUp(int)}, prints a message endWarning and terminates with
   * returncode
   *
   * @param n returncode
   */
  public static void endWarning(int n) {
    log(lvl, "endWarning: %d", n);
    cleanUp(n);
    System.exit(n);
  }

  /**
   * INTERNAL USE: convenience function: runs {@link #cleanUp(int)}, prints a message endError and terminates with
   * returncode
   *
   * @param n
   */
  public static void endError(int n) {
    log(lvl, "endError: %d", n);
    cleanUp(n);
    System.exit(n);
  }

  public static void terminate(int n) {
    String msg = "***** Terminating SikuliX Setup after a fatal error"
            + (n == 0 ? "*****\n" : " %d *****\n")
            + "SikuliX is not useable!\n"
            + "Check the error log at " + Debug.logfile;
    if (Settings.runningSetup) {
      if (Settings.noPupUps) {
        log(-1, msg, n);
      } else {
        popError(String.format(msg, n));
      }
    } else {
      msg = "***** Terminating SikuliX after a fatal error"
              + (n == 0 ? "*****\n" : " %d *****\n")
              + "It makes no sense to continue!\n"
              + "If you do not have any idea about the error cause or solution, run again\n"
              + "with a Debug level of 3. You might paste the output to the Q&A board.";
      log(-1, msg, n);
      if (Settings.isRunningIDE) {
        popError(String.format(msg, n));
      }
      cleanUp(n);
    }
    if (!Settings.isRunningIDE) {
      System.exit(1);
    } else {
      throw new IllegalStateException("Aborting script due to an internal error - see log");
    }
  }

  /**
   * INTERNAL USE: resets stateful Sikuli X features: <br>
   * ScreenHighlighter, Observing, Mouse, Key, Hotkeys <br>
   * When in IDE: resets selected options to defaults (TODO)
   *
   * @param n returncode
   */
  public static void cleanUp(int n) {
    log(lvl, "cleanUp: %d", n);
    VNCScreen.stopAll();
    ADBScreen.stop();
    ScreenHighlighter.closeAll();
    Observing.cleanUp();
    HotkeyManager.reset();
    try {
      new RobotDesktop().keyUp();
    } catch (AWTException e) {
    }
    Mouse.reset();
  }

  /**
   * INTERNAL USE: used in setup: tests basic SikulixUtil features
   *
   * @return success
   */
  public static boolean testSetup() {
    return doTestSetup("Java API", false);
  }

  /**
   * INTERNAL USE: used in setup: tests basic SikulixUtil features
   *
   * @return success
   */
  public static boolean testSetup(String src) {
    return doTestSetup(src, false);
  }

  /**
   * INTERNAL USE: used in setup: tests basic SikulixUtil features
   *
   * @return success
   */
  public static boolean testSetupSilent() {
    Settings.noPupUps = true;
    return doTestSetup("Java API", true);
  }

  private static boolean doTestSetup(String testSetupSource, boolean silent) {
    Region r = Region.create(0, 0, 100, 100);
    Image img = new Image(r.getScreen().capture(r).getImage());
    Pattern p = new Pattern(img);
    Finder f = new Finder(img);
    boolean success = (null != f.find(p));
    log(lvl, "testSetup: Finder setup with image %s", (!success ? "did not work" : "worked"));
    if (success &= f.hasNext()) {
      success = (null != f.find(img.asFile()));
      log(lvl, "testSetup: Finder setup with image file %s", (!success ? "did not work" : "worked"));
      success &= f.hasNext();
      String screenFind = "Screen.find(imagefile)";
      try {
        ((Screen) r.getScreen()).find(img.asFile());
        log(lvl, "testSetup: %s worked", screenFind);
        screenFind = "repeated Screen.find(imagefile)";
        ((Screen) r.getScreen()).find(img.asFile());
        log(lvl, "testSetup: %s worked", screenFind);
      } catch (Exception ex) {
        log(lvl, "testSetup: %s did not work", screenFind);
        success = false;
      }
    }
    if (success) {
      if (!silent) {
        popup("Hallo from Sikulix.testSetup: " + testSetupSource + "\n"
                + "SikuliX seems to be working!\n\nHave fun!");
        log(lvl, "testSetup: Finder.find: worked");
      } else {
        System.out.println("[info] RunSetup: Sikulix.testSetup: Java Sikuli seems to be working!");
      }
      return true;
    }
    log(lvl, "testSetup: last Screen/Finder.find: did not work");
    return false;
  }

//  @Deprecated
//  public static boolean addToClasspath(String jar) {
//    return RunTime.get().addToClasspath(jar);
//  }
//
//  @Deprecated
//  public static boolean isOnClasspath(String artefact) {
//    return null != RunTime.get().isOnClasspath(artefact);
//  }

  public static String run(String cmdline) {
    return run(new String[]{cmdline});
  }

  public static String run(String[] cmd) {
    return rt.runcmd(cmd);
  }

  public static void popError(String message) {
    popError(message, "Sikuli");
  }

  public static void popError(String message, String title) {
    JFrame anchor = popLocation();
    JOptionPane.showMessageDialog(anchor, message, title, JOptionPane.ERROR_MESSAGE);
    if (anchor != null) {
      anchor.dispose();
    }
  }

  /**
   * request user's input as one line of text <br>
   * with hidden = true: <br>
   * the dialog works as password input (input text hidden as bullets) <br>
   * take care to destroy the return value as soon as possible (internally the password is deleted on return)
   *
   * @param msg
   * @param preset
   * @param title
   * @param hidden
   * @return the text entered
   */
  public static String input(String msg, String preset, String title, boolean hidden) {
    JFrame anchor = popLocation();
    String ret = "";
    if (!hidden) {
      if ("".equals(title)) {
        title = "Sikuli input request";
      }
      ret = (String) JOptionPane.showInputDialog(anchor, msg, title,
              JOptionPane.PLAIN_MESSAGE, null, null, preset);
    } else {
      preset = "";
      JTextArea tm = new JTextArea(msg);
      tm.setColumns(20);
      tm.setLineWrap(true);
      tm.setWrapStyleWord(true);
      tm.setEditable(false);
      tm.setBackground(new JLabel().getBackground());
      JPasswordField pw = new JPasswordField(preset);
      JPanel pnl = new JPanel();
      pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
      pnl.add(pw);
      pnl.add(Box.createVerticalStrut(10));
      pnl.add(tm);
      int retval = JOptionPane.showConfirmDialog(anchor, pnl, title, JOptionPane.OK_CANCEL_OPTION);
      if (0 == retval) {
        char[] pwc = pw.getPassword();
        for (int i = 0; i < pwc.length; i++) {
          ret = ret + pwc[i];
          pwc[i] = 0;
        }
      }
    }
    if (anchor != null) {
      anchor.dispose();
    }
    return ret;
  }

  public static String input(String msg, String title, boolean hidden) {
    return input(msg, "", title, hidden);
  }

  public static String input(String msg, boolean hidden) {
    return input(msg, "", "", hidden);
  }

  public static String input(String msg, String preset, String title) {
    return input(msg, preset, title, false);
  }

  public static String input(String msg, String preset) {
    return input(msg, preset, "", false);
  }

  public static String input(String msg) {
    return input(msg, "", "", false);
  }

  public static boolean popAsk(String msg) {
    return popAsk(msg, null);
  }

  public static boolean popAsk(String msg, String title) {
    if (title == null) {
      title = "... something to decide!";
    }
    JFrame anchor = popLocation();
    int ret = JOptionPane.showConfirmDialog(anchor, msg, title, JOptionPane.YES_NO_OPTION);
    if (anchor != null) {
      anchor.dispose();
    }
    if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
      return false;
    }
    return true;
  }

  public static void popup(String message) {
    popup(message, "Sikuli");
  }

  public static void popup(String message, String title) {
    JFrame anchor = popLocation();
    JOptionPane.showMessageDialog(anchor, message, title, JOptionPane.PLAIN_MESSAGE);
    if (anchor != null) {
      anchor.dispose();
    }
  }

  public static Location popat(Location at) {
    locPopAt = new Point(at.x, at.y);
    return new Location(locPopAt);
  }

  public static Location popat(Region at) {
    locPopAt = new Point(at.getCenter().x, at.getCenter().y);
    return new Location(locPopAt);
  }

  public static Location popat(int atx, int aty) {
    locPopAt = new Point(atx, aty);
    return new Location(locPopAt);
  }

  public static Location popat() {
    locPopAt = getLocPopAt();
    return new Location(locPopAt);
  }

  private static Point getLocPopAt() {
    Rectangle screen0 = rt.getMonitor(0);
    if (null == screen0) {
      return null;
    }
    return new Point((int) screen0.getCenterX(), (int) screen0.getCenterY());
  }

  private static JFrame popLocation() {
    if (null == locPopAt) {
      locPopAt = getLocPopAt();
      if (null == locPopAt) {
        return null;
      }
    }
    return popLocation(locPopAt.x, locPopAt.y);
  }

  private static JFrame popLocation(int x, int y) {
    JFrame anchor = new JFrame();
    anchor.setAlwaysOnTop(true);
    anchor.setUndecorated(true);
    anchor.setSize(1, 1);
    anchor.setLocation(x, y);
    anchor.setVisible(true);
    return anchor;
  }

  public static String popSelect(String msg, String[] options, String preset) {
    return popSelect(msg, null, options, preset);
  }

  public static String popSelect(String msg, String[] options) {
    if (options.length == 0) {
      return "";
    }
    return popSelect(msg, null, options, options[0]);
  }

  public static String popSelect(String msg, String title, String[] options) {
    if (options.length == 0) {
      return "";
    }
    return popSelect(msg, title, options, options[0]);
  }

  public static String popSelect(String msg, String title, String[] options, String preset) {
    if (title == null || "".equals(title)) {
      title = "... something to select!";
    }
    if (options.length == 0) {
      return "";
    }
    if (preset == null) {
      preset = options[0];
    }
    JFrame anchor = popLocation();
    String ret = (String) JOptionPane.showInputDialog(anchor, msg, title,
            JOptionPane.PLAIN_MESSAGE, null, options, preset);
    if (anchor != null) {
      anchor.dispose();
    }
    return ret;
  }

  public static String popFile(String title) {
    popat(new Screen(0).getCenter());
    JFrame anchor = popLocation();
    SikulixFileChooser fc = new SikulixFileChooser(anchor);
    File ret = fc.show(title);
    fc = null;
    if (anchor != null) {
      anchor.dispose();
    }
    if (ret == null) {
      return "";
    }
    return ret.getAbsolutePath();
  }

  public static String inputText(String msg) {
    return inputText(msg, "", 0, 0, "");
  }

  public static String inputText(String msg, int lines, int width) {
    return inputText(msg, "", lines, width, "");
  }

  public static String inputText(String msg, int lines, int width, String text) {
    return inputText(msg, "", lines, width, text);
  }

  public static String inputText(String msg, String text) {
    return inputText(msg, "", 0, 0, text);
  }

  /**
   * Shows a dialog request to enter text in a multiline text field <br>
   * it has line wrapping on word bounds and a vertical scrollbar if needed
   *
   * @param msg   the message to display below the textfield
   * @param title the title for the dialog (default: SikuliX input request)
   * @param lines the maximum number of lines visible in the text field (default 9)
   * @param width the maximum number of characters visible in one line (default 20 letters m)
   * @param text  a preset text to show
   * @return The user's input including the line breaks.
   */
  public static String inputText(String msg, String title, int lines, int width, String text) {
    width = Math.max(20, width);
    lines = Math.max(9, lines);
    if ("".equals(title)) {
      title = "SikuliX input request";
    }
    JTextArea ta = new JTextArea("");
    String fontname = "Dialog";
    int pluswidth = 1;
    if (Settings.InputFontMono) {
      fontname = "Monospaced";
      pluswidth = 3;
    }
    ta.setFont(new Font(fontname, Font.PLAIN, Math.max(14, Settings.InputFontSize)));
    int w = (width + pluswidth) * ta.getFontMetrics(ta.getFont()).charWidth('m');
    int h = (lines + 1) * ta.getFontMetrics(ta.getFont()).getHeight();
    ta.setText(text);
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);

    JScrollPane sp = new JScrollPane();
    sp.setViewportView(ta);
    sp.setPreferredSize(new Dimension(w, h));

    JTextArea tm = new JTextArea("");
    tm.setFont(new Font(fontname, Font.PLAIN, Math.max(14, Settings.InputFontSize)));
    tm.setColumns(width);
    tm.setText(msg);
    tm.setLineWrap(true);
    tm.setWrapStyleWord(true);
    tm.setEditable(false);
    tm.setBackground(new JLabel().getBackground());

    JPanel pnl = new JPanel();
    pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
    pnl.add(sp);
    pnl.add(Box.createVerticalStrut(10));
    pnl.add(tm);
    pnl.add(Box.createVerticalStrut(10));
    JFrame anchor = popLocation();
    int ret = JOptionPane.showConfirmDialog(anchor, pnl, title, JOptionPane.OK_CANCEL_OPTION);
    if (anchor != null) {
      anchor.dispose();
    }
    if (0 == ret) {
      return ta.getText();
    } else {
      return null;
    }
  }

  public static boolean importPrefs(String path) {
    return true;
  }

  public static String arrayToString(String[] args) {
    String ret = "";
    for (String s : args) {
      if (s.contains(" ")) {
        s = "\"" + s + "\"";
      }
      ret += s + " ";
    }
    return ret;
  }

  public static boolean exportPrefs(String path) {
    return true;
  }

  /**
   * store a key-value-pair in Javas persistent preferences storage that is used by SikuliX to save settings and
   * information between IDE sessions<br>
   * this allows, to easily make some valuable information persistent
   *
   * @param key   name of the item
   * @param value item content
   */
  public static void prefStore(String key, String value) {
    PreferencesUser.getInstance().put(prefNonSikuli + key, value);
  }

  /**
   * retrieve the value of a previously stored a key-value-pair from Javas persistent preferences storage that is used
   * by SikuliX to save settings and information between IDE sessions<br>
   *
   * @param key name of the item
   * @return the item content or empty string if not stored yet
   */
  public static String prefLoad(String key) {
    return PreferencesUser.getInstance().get(prefNonSikuli + key, "");
  }

  /**
   * retrieve the value of a previously stored a key-value-pair from Javas persistent preferences storage that is used
   * by SikuliX to save settings and information between IDE sessions<br>
   *
   * @param key   name of the item
   * @param value the item content or the given value if not stored yet (default)
   * @return the item content or the given default
   */
  public static String prefLoad(String key, String value) {
    return PreferencesUser.getInstance().get(prefNonSikuli + key, value);
  }

  /**
   * permanently remove the previously stored key-value-pair having the given key from Javas persistent preferences
   * storage that is used by SikuliX to save settings and information between IDE sessions<br>
   *
   * @param key name of the item to permanently remove
   * @return the item content that would be returned by prefLoad(key)
   */
  public static String prefRemove(String key) {
    String val = prefLoad(key);
    PreferencesUser.getInstance().remove(prefNonSikuli + key);
    return val;
  }

  /**
   * permanently remove all previously stored key-value-pairs (by prefsStore()) from Javas persistent preferences
   * storage that is used by SikuliX to save settings and information between IDE sessions<br>
   */
  public static void prefRemove() {
    PreferencesUser.getInstance().removeAll(prefNonSikuli);
  }

  /**
   * convenience for a password protected VNCScreen connection
   * (use theVNCScreen.stop() to stop the connection)
   * active screens are auto-stopped at cleanup
   *
   * @param theIP    the server IP
   * @param thePort  the port number
   * @param password a needed password for the server in plain text
   * @param cTimeout seconds to wait for a valid connection
   * @param timeout  value in milli-seconds during normal operation
   * @return a VNCScreen object
   */
  public static VNCScreen vncStart(String theIP, int thePort, String password, int cTimeout, int timeout) {
    try {
      return VNCScreen.start(theIP, thePort, password, cTimeout, timeout);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * convenience for a VNCScreen connection (use theVNCScreen.stop() to stop the connection)
   * active screens are auto-stopped at cleanup
   *
   * @param theIP    the server IP
   * @param thePort  the port number
   * @param cTimeout seconds to wait for a valid connection
   * @param timeout  value in milli-seconds during normal operation
   * @return a VNCScreen object
   */
  public static VNCScreen vncStart(String theIP, int thePort, int cTimeout, int timeout) {
    try {
      return VNCScreen.start(theIP, thePort, cTimeout, timeout);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
