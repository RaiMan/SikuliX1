/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.runnerSupport.JythonSupport;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SikulixAPI;
import org.sikuli.vnc.VNCScreen;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;

public class Sikulix {

  public static void main(String[] args) throws FindFailed {
    System.setProperty("sikuli.API_should_run", "develop");
    RunTime.start(RunTime.Type.API, args);
    SikulixAPI.main(args);
  }

  //<editor-fold desc="00 log">
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "Sikulix: " + message, args);
  }

  public static String print(String msg, Object... args) {
    String outMsg = String.format(msg, args);
    System.out.println(outMsg);
    return outMsg;
  }
  //</editor-fold>

  //<editor-fold desc="01 popat">
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
    if (Screen.isHeadless()) {
      return null;
    }
    Rectangle rect = Screen.getMonitorBounds();
    if (RunTime.isRunningIDE) {
      //TODO should be IDE window
    }
    return new Point((int) rect.getCenterX(), (int) rect.getCenterY());
  }

  public static Location getCurrentPopLocation() {
    if (null == locPopAt) {
      locPopAt = getLocPopAt();
      if (null == locPopAt) {
        return null;
      }
    }
    return new Location(locPopAt);
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

  private static Point locPopAt = null;
  //</editor-fold>

  //<editor-fold desc="02 popup">
  public static void popup(String message) {
    popup(message, "Sikuli");
  }

  public static void popup(String message, String title) {
//    JFrame anchor = popLocation();
//    JOptionPane.showMessageDialog(anchor, message, title, JOptionPane.PLAIN_MESSAGE);
//    if (anchor != null) {
//      anchor.dispose();
//    }
    SX.popup(message, title);
  }
  //</editor-fold>

  //<editor-fold desc="03 popError">
  public static void popError(String message) {
    popError(message, "Sikuli");
  }

  public static void popError(String message, String title) {
//    JFrame anchor = popLocation();
//    JOptionPane.showMessageDialog(anchor, message, title, JOptionPane.ERROR_MESSAGE);
//    if (anchor != null) {
//      anchor.dispose();
//    }
    SX.popError(message, title);
  }
  //</editor-fold>

  //<editor-fold desc="04 popAsk">
  public static boolean popAsk(String msg) {
    return popAsk(msg, null);
  }

  public static boolean popAsk(String msg, String title) {
    if (title == null) {
      title = "... something to decide!";
    }
//    JFrame anchor = popLocation();
//    int ret = JOptionPane.showConfirmDialog(anchor, msg, title, JOptionPane.YES_NO_OPTION);
//    if (anchor != null) {
//      anchor.dispose();
//    }
//    if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
//      return false;
//    }
//    return true;
    return SX.popAsk(msg, title);
  }
  //</editor-fold>

  //<editor-fold desc="05 popSelect">
  public static String popSelect(String msg, String[] options, String preset) {
    return popSelect(msg, "", preset, options);
  }

  public static String popSelect(String msg, String[] options) {
    if (options.length == 0) {
      return "";
    }
    return popSelect(msg, "", options[0], options);
  }

  public static String popSelect(String msg, String title, String[] options) {
    if (options.length == 0) {
      return "";
    }
    return popSelect(msg, title, options[0], options);
  }

  public static String popSelect(String msg, String title, String preset, String[] options) {
    if (title == null || title.isEmpty()) {
      title = "... something to select!";
    }
    if (options.length == 0) {
      return "";
    }
    if (preset == null || preset.isEmpty()) {
      preset = options[0];
    }
//    JFrame anchor = popLocation();
//    String ret = (String) JOptionPane.showInputDialog(anchor, msg, title,
//            JOptionPane.PLAIN_MESSAGE, null, options, preset);
//    if (anchor != null) {
//      anchor.dispose();
//    }
    return SX.popSelect(msg, title, preset, null, Integer.MAX_VALUE, null, options);
  }
  //</editor-fold>

  //<editor-fold desc="06 popFile">
  public static String popFile(String title) {
//    popat(new Screen(0).getCenter());
//    JFrame anchor = popLocation();
//    File fileChoosen = new SikulixFileChooser(anchor).open(title);
//    if (anchor != null) {
//      anchor.dispose();
//    }
//    if (fileChoosen == null) {
//      return "";
//    }
//    return fileChoosen.getAbsolutePath();
    return SX.popFile(title);
  }
  //</editor-fold>

  //<editor-fold desc="07 input">

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
  //</editor-fold>

  //<editor-fold desc="08 inputText">
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
  //</editor-fold>

  //<editor-fold desc="10 run">
  public static String run(String cmdline) {
    return run(new String[]{cmdline});
  }

  public static String run(String[] cmd) {
    return RunTime.get().runcmd(cmd);
  }
  //</editor-fold>

  //<editor-fold desc="11 vnc">

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
    return VNCScreen.start(theIP, thePort, password, cTimeout, timeout);
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
    return VNCScreen.start(theIP, thePort, cTimeout, timeout);
  }
  //</editor-fold>

  //<editor-fold desc="99 buildjar, compile">

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
    JythonSupport jython = JythonSupport.get();
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
      if (!jython.interpreterExecString("import compileall")) {
        return false;
      }
      jython = doCompileJythonFolder(jython, fTarget);
      FileManager.traverseFolder(fTarget, new CompileJythonFilter(jython));
    }
    return false;
  }

  private static class CompileJythonFilter extends FileManager.FileFilter{

    JythonSupport jython = null;

    CompileJythonFilter(JythonSupport jython) {
      this.jython = jython;
    }

    public boolean accept(File entry) {
      if (jython != null && entry.isDirectory()) {
        jython = doCompileJythonFolder(jython, entry);
      }
      return false;
    }
  }

  private static JythonSupport doCompileJythonFolder(JythonSupport jython, File fSource) {
    String fpSource = FileManager.slashify(fSource.getAbsolutePath(), false);
    if (!jython.interpreterExecString(String.format("compileall.compile_dir(\"%s\","
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
  //</editor-fold>
}
