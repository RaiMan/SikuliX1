/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.recorder.Recorder;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runners.AppleScriptRunner;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SikulixAPI;
import org.sikuli.script.support.devices.Device;
import org.sikuli.script.support.devices.HelpDevice;
import org.sikuli.script.support.devices.MouseDevice;
import org.sikuli.script.support.devices.ScreenDevice;
import org.sikuli.script.support.gui.SXDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.sikuli.util.CommandArgsEnum.APPDATA;

public class Sikulix {

  public static void main(String[] args) {
    Commons.init();
    System.setProperty("sikuli.API_should_run", "develop"); //TODO needed?
    if (args.length == 1 && "buildDate".equals(args[0])) {
      System.out.println(Commons.getSxBuildStamp()); //OK
      System.exit(0);
    }

//TODO place to test something in the API context
    if (args.length == 1) {
      Debug.print("BuildStamp: %s", Commons.getSXBuild());
      Debug.print("osVersion: %s", Commons.getOSInfo());

      String arg = args[0];
      if (arg.toLowerCase().startsWith("test")) {
        arg = arg.toLowerCase().replace("test", "");
        try {
          testRun(arg);
        } catch (Exception e) {
          Debug.error("%s", e);
          System.exit(1);
        }
      }
      System.exit(0);
    }

    SikulixAPI.main(args);
  }

  public static void testRun(String arg) throws Exception {
    Screen scr = new Screen();

    if (arg.startsWith("rec")) {
      //region recording
      Device.checkAccessibility();
      if(!MouseDevice.isUseable() || !ScreenDevice.isUseable()) {
        Commons.terminate(999, "Mouse and/or Screen not useable");
      }

      // we are running in API context
      Commons.setStartClass(Sikulix.class);

      // AppDataPath as SikulixAppData in Project/API
      File path = Commons.setAppDataPath(Commons.getStartArg(APPDATA));
      Commons.setTempFolder(new File(path, "Temp"));

      // delete previous recordings
      Recorder.INSTANCE.resetRecordingDirectory();

      SX.popup("ok to start recording");
      Recorder.INSTANCE.startRecording();

      SX.popup("ok to stop recording");
      Recorder.INSTANCE.finishRecording();

      Commons.terminate();
      //endregion
    }

    if ("app".equals(arg)) {
      final IScriptRunner.Options options = new IScriptRunner.Options().setOutput();
      new AppleScriptRunner().evalScript("display dialog \"hello\"", options);
      if (!options.getOutput().strip().contains("OK")) System.exit(-1);

      List<App> apps = App.allWithWindow();
      App app = new App("finder");
      if (!app.isRunning()) {
        app.open();
      } else {
        app.focus();
      }
      App.pause(2);
      Region window = app.toFront("smile");
      //window.highlight(2);


      List<Region> windows = app.windows();
      //app.window(0).highlight(2);
      Debug.print("app.getTitle(): %s", app.getTitle());
      for (Region w : windows) {
        Debug.print(w.getName());
      }
    }

    if ("click".equals(arg)) {
      Debug.on(3);
      File userHome = Commons.getUserHome();
      File testBundle = new File(userHome, "IdeaProjects/SikuliX1/API/src/main/resources/images");
      ImagePath.setBundleFolder(testBundle);
      ImagePath.dump(0);

      Image img = Image.from("img");
      Debug.log("%s", img);
      Debug.log("%s", img.getFilename());

      Match mimg = scr.exists(img, 0);
      try {
        Debug.log(3, "Match::%s", mimg);
        Settings.MoveMouseDelay = 0;
        int clicked = scr.click(mimg);
        Debug.log(3, "Click::%s", clicked);
      } catch (FindFailed e) {
        Debug.error("click::FindFailed");
      }
    }

    if ("zzz".equals(arg)) {
      Debug.on(3);
      try {
        scr.hover();
        scr.text();
      } catch (Exception e) {
        Debug.print("%s", e.getMessage());
      }
      //arg = "xxx";
      arg = "";
    }

    if ("xxx".equals(arg)) {
      Region reg = new Region(35, 0, 100, 25);
      Image image = Image.from(reg);
      String text = image.text();
      Debug.info("testxxx: %s", text);
    }

    if ("find".equals(arg)) {
      //scr.selectRegion().text();
      File userHome = Commons.getUserHome();
      File testBundle = new File(userHome, "IdeaProjects/SikuliX1/API/src/main/resources/images");
      ImagePath.setBundleFolder(testBundle);

      Class<?> aClass = Class.forName("com.sikulix.JarImages");
      URL jarimages = aClass.getResource("/jarimages/jarimg.png");
      //ImagePath.append(jarimages);

      URL jarimage = aClass.getResource("/jarimages/jarimg.png");

      Image urlimg = Image.from(jarimage);

      List<Object> obs = new ArrayList<>(Arrays.asList("img", "img100"));
      Match match;
      List<Match> matches;
      Debug.on(3);
      //TextRecognizer.get(null);
      //print("%s", scr.selectRegion().text());
      //String img = ImagePath.check("pylon");
      //scr.userCapture();
      //scr.hover();
      //Pattern pImg = Pattern.get("img", 0.9, 20, 30);
      //matches = scr.getAll(pImg);
      //scr.highlight(matches, 2);
      //scr.getAny(obs);

      match = scr.find(urlimg);
      match.hover();

      System.exit(1);

      String images = ImagePath.getBundlePath();
      SXDialog sxabout = new SXDialog("sxabout", new Point(500, 500), SXDialog.POSITION.TOP);
      sxabout.run();
      while (sxabout.isRunning()) {
        Commons.pause(1);
      }
      System.exit(0);

      //scr = new Screen(1);
      SXDialog sxDialog = new SXDialog("#image; file:" + images + "/SikulixTest001.png;",
          scr.getTopLeft().getPoint(), SXDialog.POSITION.TOPLEFT);
      Region reg = new Region(0, 0, 500, 600);
      ScreenImage screenImage = scr.capture(reg);
      Image image = new Image(screenImage);
      image = Image.from("SikulixTest001");
      Commons.pause(1);

      reg = scr;
      //reg.setFindFailedResponse(FindFailedResponse.PROMPT);
      SXDialog.onScreen(sxDialog);
      App.focus("idea");
    }
  }

  //<editor-fold desc="00 log">
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "Sikulix: " + message, args);
  }
  //</editor-fold>

  //<editor-fold desc="01 popat">
  public static Location popat(JFrame atFrame) {
    locPopAt = null;
    locPopAtFrame = atFrame;
    return new Location(atFrame.getLocation());
  }

  public static Location popat(Point at) {
    locPopAt = new Point(at);
    return new Location(locPopAt);
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
    Rectangle rect = ScreenDevice.get(0).asRectangle();
    //TODO should be IDE window
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
    JFrame anchor = null;
    if (null == locPopAt) {
      if (null != locPopAtFrame) {
        anchor = locPopAtFrame;
      } else {
        locPopAt = getLocPopAt();
        if (null == locPopAt) {
          return null;
        }
      }
    }
    if (anchor == null) {
      anchor = popLocation(locPopAt.x, locPopAt.y);
    }
    locPopAt = null;
    locPopAtFrame = null;
    return anchor;
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
  private static JFrame locPopAtFrame = null;
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
    if (anchor != null && anchor.getSize().width < 2) {
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
    return RunTime.runcmd(cmd);
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
  public static Device vncStart(String theIP, int thePort, String password, int cTimeout, int timeout) {
    //TODO finally implement VNCScreen as VNCDevice
    return HelpDevice.startVNC(theIP, thePort, password, cTimeout, timeout);
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
  public static Device vncStart(String theIP, int thePort, int cTimeout, int timeout) {
    //TODO finally implement VNCScreen as VNCDevice
    return HelpDevice.startVNC(theIP, thePort, cTimeout, timeout);
  }
  //</editor-fold>

  //<editor-fold desc="20 Sikulix preferences store - deprecated">
  @Deprecated
  public static void prefStore(String key, Object value) {
    Options.prefStore(key, value);
  }

  @Deprecated
  public static String prefLoad(String key, Object deflt) {
    return Options.prefLoad(key, deflt);
  }

  @Deprecated
  public static String prefRemove(String key) {
    return Options.prefRemove(key);
  }

  @Deprecated
  public static void prefRemove() {
    Options.prefRemoveAll();
  }
  //</editor-fold> (deprecated

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
  //</editor-fold>
}
