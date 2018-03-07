/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import com.explodingpixels.macwidgets.MacUtils;
import org.apache.commons.cli.CommandLine;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.sikuli.android.ADBClient;
import org.sikuli.android.ADBScreen;
import org.sikuli.android.ADBTest;
import org.sikuli.basics.*;
import org.sikuli.idesupport.IDESplash;
import org.sikuli.idesupport.IDESupport;
import org.sikuli.idesupport.IIDESupport;
import org.sikuli.script.*;
import org.sikuli.script.Image;
import org.sikuli.script.Sikulix;
import org.sikuli.scriptrunner.IScriptRunner;
import org.sikuli.scriptrunner.ScriptingSupport;
import org.sikuli.util.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;

public class SikuliIDE extends JFrame implements InvocationHandler {

  private static String me = "IDE: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  final static boolean ENABLE_UNIFIED_TOOLBAR = true;
  final static Color COLOR_SEARCH_FAILED = Color.red;
  final static Color COLOR_SEARCH_NORMAL = Color.black;
  final static int WARNING_CANCEL = 2;
  final static int WARNING_ACCEPTED = 1;
  final static int WARNING_DO_NOTHING = 0;
  final static int IS_SAVE_ALL = 3;
  private Dimension _windowSize = null;
  private Point _windowLocation = null;
  private CloseableTabbedPane tabPane;
  private EditorLineNumberView lineNumberColumn;
  private JSplitPane _mainSplitPane;
  private JTabbedPane msgPane;
  private boolean msgPaneCollapsed = false;
  private EditorConsolePane _console;
  private JXCollapsiblePane _cmdList;
  private SikuliIDEStatusBar _status = null;
  private ButtonCapture _btnCapture;
  private ButtonRun _btnRun = null, _btnRunViz = null;
  private boolean ideIsRunningScript = false;
  private JXSearchField _searchField;
  private JMenuBar _menuBar = new JMenuBar();
  private JMenu _fileMenu = new JMenu(_I("menuFile"));
  private JMenu _editMenu = new JMenu(_I("menuEdit"));
  private UndoAction _undoAction = new UndoAction();
  private RedoAction _redoAction = new RedoAction();
  private FindAction _findHelper;
  private JMenu _runMenu = new JMenu(_I("menuRun"));
  private JMenu _viewMenu = new JMenu(_I("menuView"));
  private JMenu _toolMenu = new JMenu(_I("menuTool"));
  private JMenu _helpMenu = new JMenu(_I("menuHelp"));
  private JXCollapsiblePane _sidePane;
  private JCheckBoxMenuItem _chkShowUnitTest;
  private JMenuItem chkShowCmdList = null;
  private JCheckBoxMenuItem chkShowThumbs;
  //private UnitTestRunner _testRunner;
  private static CommandLine cmdLine;
  private static String cmdValue;
  private static String[] loadScripts = null;
  private static SikuliIDE sikulixIDE = null;
  private boolean _inited = false;
  private int restoredScripts = 0;
  private int alreadyOpenedTab = -1;
  private PreferencesUser prefs;
  private boolean ACCESSING_AS_FOLDER = false;
  private static long start;
  private boolean showAbout = true;
  private boolean showPrefs = true;
  private boolean showQuit = true;
  IDESplash ideSplash = null;
  boolean idePause = false;
  int waitBeforeVisible = 0;

  private synchronized boolean setPause(Boolean state) {
    if (state != null) {
      idePause = state;
    }
    return idePause;
  }

  private boolean getPause() {
    return setPause(null);
  }

  private void waitPause() {
    if (getPause()) {
      ideSplash.setVisible(false);
      Sikulix.popup("No options yet!\nClick OK to continue!",
              String.format("%s-%s", runTime.getVersionShort(), runTime.sxBuildStamp));
      ideSplash.showAction(" ");
      ideSplash.setVisible(true);
      waitBeforeVisible = 2;
    }
  }

  public static void showIDE() {
    Debug.log(3, "showIDE");
    sikulixIDE.setVisible(true);
  }

  public static void hideIDE() {
    Debug.log(3, "hideIDE");
    sikulixIDE.setVisible(false);
    RunTime.pause(0.5f);
  }

  public static void showAgain() {
    sikulixIDE.setVisible(true);
    EditorPane codePane = sikulixIDE.getCurrentCodePane();
    codePane.requestFocus();
  }

  private SikuliIDE() {
    super("SikuliX-IDE");
  }

  public static synchronized SikuliIDE getInstance() {
    if (sikulixIDE == null) {
      sikulixIDE = new SikuliIDE();
    }
    return sikulixIDE;
  }

  public static String _I(String key, Object... args) {
    try {
      return SikuliIDEI18N._I(key, args);
    } catch (Exception e) {
      Debug.log(3, "[I18N] " + key);
      return key;
    }
  }

  public static RunTime runTime;

  public static void run(String[] args) {

    start = (new Date()).getTime();

    runTime = RunTime.get(RunTime.Type.IDE, args);

    CommandArgs cmdArgs = new CommandArgs("IDE");
    cmdLine = cmdArgs.getCommandLine(CommandArgs.scanArgs(args));

    if (cmdLine == null) {
      Debug.error("Did not find any valid option on command line!");
      System.exit(1);
    }

    runTime.setArgs(cmdArgs.getUserArgs(), cmdArgs.getSikuliArgs());

    if (RunTime.shouldRunServer) {
      RunServer.run(null);
      System.exit(0);
    }

    if (cmdLine.hasOption("h")) {
      cmdArgs.printHelp();
      System.exit(0);
    }

    if (cmdLine.hasOption(CommandArgsEnum.RUN.shortname())
            || cmdLine.hasOption(CommandArgsEnum.TEST.shortname())
            || cmdLine.hasOption(CommandArgsEnum.INTERACTIVE.shortname())) {
      log(lvl, "Switching to ScriptRunner with option -r, -t or -i");
      ScriptingSupport.runscript(args);
    }

    getInstance();
    log(3, "running with Locale: %s", SikuliIDEI18N.getLocaleShow());

    sikulixIDE.initNativeSupport();
    sikulixIDE.ideSplash = new IDESplash(runTime);

    if (cmdLine.hasOption(CommandArgsEnum.DEBUG.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.DEBUG.longname());
      if (cmdValue != null) {
        Debug.on(cmdValue);
      }
    }

    if (cmdLine.hasOption("c")) {
      System.setProperty("sikuli.console", "false");
    }

    if (cmdLine.hasOption(CommandArgsEnum.LOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.LOGFILE.longname());
      if (!Debug.setLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.USERLOGFILE.shortname())) {
      cmdValue = cmdLine.getOptionValue(CommandArgsEnum.USERLOGFILE.longname());
      if (!Debug.setUserLogFile(cmdValue == null ? "" : cmdValue)) {
        System.exit(1);
      }
    }

    if (cmdLine.hasOption(CommandArgsEnum.LOAD.shortname())) {
      loadScripts = cmdLine.getOptionValues(CommandArgsEnum.LOAD.longname());
      log(lvl, "requested to load: %s", loadScripts);
    }

//TODO how to differentiate open and run for doubleclick/drop scripts
    if (macOpenFiles != null) {
      for (File f : macOpenFiles) {
        if (f.getName().endsWith(".sikuli") || f.getName().endsWith(".skl")) {
          ScriptingSupport.runscript(new String[]{"-r", f.getAbsolutePath()});
        }
      }
    }

    runTime.printArgs();

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //TODO UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {
      log(-1, "Problem loading UIManager!\nError: %s", e.getMessage());
    }

    sikulixIDE.initHotkeys();
    sikulixIDE.ideSplash.showAction("Interrupt with " + HotkeyManager.getInstance().getHotKeyText("Abort"));
    sikulixIDE.ideSplash.showStep("Init ScriptingSupport");

    ScriptingSupport.init();
    IDESupport.initIDESupport();
    sikulixIDE.initSikuliIDE(args);
  }

  private void initSikuliIDE(String[] args) {
    sikulixIDE.ideSplash.showStep("Reading Preferences");
    prefs = PreferencesUser.getInstance();
    //prefs.exportPrefs(new File(runTime.fUserDir, "SikulixIDEprefs.txt").getAbsolutePath());
    if (prefs.getUserType() < 0) {
      prefs.setUserType(PreferencesUser.NEWBEE);
      prefs.setIdeSession("");
      prefs.setDefaults(prefs.getUserType());
    }

    _windowSize = prefs.getIdeSize();
    _windowLocation = prefs.getIdeLocation();

    Rectangle monitor = runTime.hasPoint(_windowLocation);
    if (monitor == null) {
      log(-1, "Remembered window not valid. Going to primary screen");
      monitor = runTime.getMonitor(-1);
      _windowSize.width = 0;
    }
    if (_windowSize.width == 0) {
      _windowSize = new Dimension(1024, 700);
      _windowLocation = new Point(100, 50);
    }
    Rectangle win = monitor.intersection(new Rectangle(_windowLocation, _windowSize));
    setSize(win.getSize());
    setLocation(_windowLocation);

    sikulixIDE.ideSplash.showStep("Init Window");
    Debug.log(3, "IDE: Adding components to window");
    initMenuBars(this);
    final Container c = getContentPane();
    c.setLayout(new BorderLayout());
    Debug.log(3, "IDE: creating tabbed editor");
    initTabPane();
    Debug.log(3, "IDE: creating message area");
    initMsgPane(prefs.getPrefMoreMessage() == PreferencesUser.HORIZONTAL);
// RaiMan not used		initSidePane(); // IDE UnitTest

    Debug.log(3, "IDE: creating combined work window");
    JPanel codeAndUnitPane = new JPanel(new BorderLayout(10, 10));
    codeAndUnitPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    codeAndUnitPane.add(tabPane, BorderLayout.CENTER);
// RaiMan not used		codeAndUnitPane.add(_sidePane, BorderLayout.EAST);
    if (prefs.getPrefMoreMessage() == PreferencesUser.VERTICAL) {
      _mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codeAndUnitPane, msgPane);
    } else {
      _mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeAndUnitPane, msgPane);
    }
    _mainSplitPane.setResizeWeight(0.6);
    _mainSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    Debug.log(3, "IDE: Putting all together");
    JPanel editPane = new JPanel(new BorderLayout(0, 0));

    JComponent cp = createCommandPane();

    if (PreferencesUser.getInstance().getPrefMoreCommandBar()) {
      editPane.add(cp, BorderLayout.WEST);
    }

    editPane.add(_mainSplitPane, BorderLayout.CENTER);
    c.add(editPane, BorderLayout.CENTER);

    JToolBar tb = initToolbar();
    c.add(tb, BorderLayout.NORTH); // the buttons

    c.add(initStatusbar(), BorderLayout.SOUTH);
    c.doLayout();

    initShortcutKeys();
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initWindowListener();
    initTooltip();

    sikulixIDE.ideSplash.showStep("Check for Updates");
    autoCheckUpdate();

    try {
      getCurrentCodePane().requestFocus();
    } catch (Exception e) {
    }

    waitPause();
    sikulixIDE.ideSplash.showStep("Restore last Session");
    restoreSession(0);
    if (tabPane.getTabCount() == 0) {
      (new FileAction()).doNew(null);
    }
    tabPane.setSelectedIndex(0);

    Debug.info("IDE startup: %4.1f seconds", (new Date().getTime() - start) / 1000.0);

    if (runTime.isJava9()) {
      Debug.info("***** BE AWARE: Running on Java 9");
      Debug.info("... Please report problems *****");
    }
    if (waitBeforeVisible > 0) {
      try {
        Thread.sleep(1000 * waitBeforeVisible);
      } catch (InterruptedException ex) {
      }
    }
    sikulixIDE.ideSplash.setVisible(false);
    sikulixIDE.ideSplash.dispose();
    sikulixIDE.ideSplash = null;
    setVisible(true);
    _mainSplitPane.setDividerLocation(0.6);
    _inited = true;
  }

  private void initNativeSupport() {
    if (!Settings.isMac()) {
      return;
    }
    log(lvl, "initNativeSupport: starting");
    if (System.getProperty("sikulix.asapp") != null) {
      Settings.isMacApp = true;
    }
    try {
      if (runTime.isJava9()) {
        log(lvl, "initNativeSupport: Java 9: trying with java.awt.Desktop");
        Class sysclass = URLClassLoader.class;
        Class clazzMacSupport = sysclass.forName("org.sikuli.idesupport.IDEMacSupport");
        Method macSupport = clazzMacSupport.getDeclaredMethod("support", SikuliIDE.class);
        macSupport.invoke(null, sikulixIDE);
        showAbout = showPrefs = showQuit = false;
      } else {
        Class sysclass = URLClassLoader.class;
        Class comAppleEawtApplication = sysclass.forName("com.apple.eawt.Application");
        Method mGetApplication = comAppleEawtApplication.getDeclaredMethod("getApplication", null);
        Object instApplication = mGetApplication.invoke(null, null);

        Class clAboutHandler = sysclass.forName("com.apple.eawt.AboutHandler");
        Class clPreferencesHandler = sysclass.forName("com.apple.eawt.PreferencesHandler");
        Class clQuitHandler = sysclass.forName("com.apple.eawt.QuitHandler");
        Class clOpenHandler = sysclass.forName("com.apple.eawt.OpenFilesHandler");

        Object appHandler = Proxy.newProxyInstance(
                comAppleEawtApplication.getClassLoader(),
                new Class[]{clAboutHandler, clPreferencesHandler, clQuitHandler, clOpenHandler},
                this);
        Method m = comAppleEawtApplication.getMethod("setAboutHandler", new Class[]{clAboutHandler});
        m.invoke(instApplication, new Object[]{appHandler});
        showAbout = false;
        m = comAppleEawtApplication.getMethod("setPreferencesHandler", new Class[]{clPreferencesHandler});
        m.invoke(instApplication, new Object[]{appHandler});
        showPrefs = false;
        m = comAppleEawtApplication.getMethod("setQuitHandler", new Class[]{clQuitHandler});
        m.invoke(instApplication, new Object[]{appHandler});
        showQuit = false;
        m = comAppleEawtApplication.getMethod("setOpenFileHandler", new Class[]{clOpenHandler});
        m.invoke(instApplication, new Object[]{appHandler});
      }
    } catch (Exception ex) {
      String em = String.format("initNativeSupport: Mac: error:\n%s", ex.getMessage());
      log(-1, em);
      Sikulix.popError(em, "IDE has problems ...");
      System.exit(1);
    }
    log(lvl, "initNativeSupport: success");
  }

  private static List<File> macOpenFiles = null;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mName = method.getName();
    if ("handleAbout".equals(mName)) {
      sikulixIDE.doAbout();
    } else if ("handlePreferences".equals(mName)) {
      sikulixIDE.showPreferencesWindow();
    } else if ("openFiles".equals(mName)) {
      log(lvl, "nativeSupport: should open files");
      try {
        Method mOpenFiles = args[0].getClass().getMethod("getFiles", new Class[]{});
        macOpenFiles = (List<File>) mOpenFiles.invoke(args[0], new Class[]{});
        for (File f : macOpenFiles) {
          log(lvl, "nativeSupport: openFiles: %s", macOpenFiles);
        }
      } catch (Exception ex) {
        log(lvl, "NativeSupport: Quit: error: %s", ex.getMessage());
        System.exit(1);
      }
    } else if ("handleQuitRequestWith".equals(mName)) {
      try {
        Class sysclass = URLClassLoader.class;
        Class comAppleEawtQuitResponse = sysclass.forName("com.apple.eawt.QuitResponse");
        Method mCancelQuit = comAppleEawtQuitResponse.getMethod("cancelQuit", null);
        Method mPerformQuit = comAppleEawtQuitResponse.getMethod("performQuit", null);
        Object resp = args[1];
        if (!sikulixIDE.quit()) {
          mCancelQuit.invoke(resp, null);
        } else {
          mPerformQuit.invoke(resp, null);
        }
      } catch (Exception ex) {
        log(lvl, "NativeSupport: Quit: error: %s", ex.getMessage());
        System.exit(1);
      }
    }
    return new Object();
  }

  @Override
  public void setTitle(String title) {
    super.setTitle(runTime.SikuliVersionIDE + " - " + title);
  }

  public static ImageIcon getIconResource(String name) {
    URL url = SikuliIDE.class.getResource(name);
    if (url == null) {
      Debug.error("Warning: could not load \"" + name + "\" icon");
      return null;
    }
    return new ImageIcon(url);
  }

  //<editor-fold defaultstate="collapsed" desc="save / restore session">
  private boolean saveSession(int action, boolean quitting) {
    int nTab = tabPane.getTabCount();
    StringBuilder sbuf = new StringBuilder();
    for (int tabIndex = 0; tabIndex < nTab; tabIndex++) {
      try {
        EditorPane codePane = getPaneAtIndex(tabIndex);
        if (action == WARNING_DO_NOTHING) {
          if (quitting) {
            codePane.setDirty(false);
          }
          if (codePane.getCurrentFilename() == null) {
            continue;
          }
        } else if (codePane.isDirty()) {
          if (!(new FileAction()).doSaveIntern(tabIndex)) {
            if (quitting) {
              codePane.setDirty(false);
            }
            continue;
          }
        }
        if (action == IS_SAVE_ALL) {
          continue;
        }
        File f = codePane.getCurrentFile();
        if (f != null) {
          String bundlePath = codePane.getSrcBundle();
          Debug.log(5, "save session: " + bundlePath);
          if (tabIndex != 0) {
            sbuf.append(";");
          }
          sbuf.append(bundlePath);
        }
      } catch (Exception e) {
        log(-1, "Problem while trying to save all changed-not-saved scripts!\nError: %s", e.getMessage());
        return false;
      }
    }
    PreferencesUser.getInstance().setIdeSession(sbuf.toString());
    return true;
  }

  private void restoreSession(int tabIndex) {
    String session_str = prefs.getIdeSession();
    if (session_str == null && loadScripts == null && macOpenFiles == null) {
      return;
    }
    List<File> filesToLoad = new ArrayList<File>();
    if (macOpenFiles != null) {
      for (File f : macOpenFiles) {
        filesToLoad.add(f);
        restoreScriptFromSession(f);
      }
    }
    if (session_str != null) {
      String[] filenames = session_str.split(";");
      for (int i = 0; i < filenames.length; i++) {
        if (filenames[i].isEmpty()) {
          continue;
        }
        File f = new File(filenames[i]);
        if (f.exists() && !filesToLoad.contains(f)) {
          Debug.log(3, "restore session: %s", f);
          filesToLoad.add(f);
          restoreScriptFromSession(f);
        }
      }
    }
    if (loadScripts != null) {
      for (int i = 0; i < loadScripts.length; i++) {
        if (loadScripts[i].isEmpty()) {
          continue;
        }
        File f = new File(loadScripts[i]);
        if (f.exists() && !filesToLoad.contains(f)) {
          Debug.log(3, "preload script: %s", f);
          filesToLoad.add(f);
          restoreScriptFromSession(f);
        }
      }
    }
  }

  private boolean restoreScriptFromSession(File file) {
    EditorPane ep = (new FileAction()).doNew(null, -1);
    ep.loadFile(file.getAbsolutePath());
    if (ep.hasEditingFile()) {
      setCurrentFileTabTitle(file.getAbsolutePath());
      return true;
    }
    log(-1, "restoreScriptFromSession: Can't load: %s", file);
//    (new FileAction()).doCloseTab(null);
    return false;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Support SikuliIDE">
  public static IIDESupport getIDESupport(String ending) {
    return IDESupport.ideSupporter.get(ending);
  }

  public JMenu getFileMenu() {
    return _fileMenu;
  }

  public JMenu getRunMenu() {
    return _runMenu;
  }

  public CloseableTabbedPane getTabPane() {
    return tabPane;
  }

  public EditorPane getCurrentCodePane() {
    if (tabPane.getSelectedIndex() == -1) {
      return null;
    }
    JScrollPane scrPane = (JScrollPane) tabPane.getSelectedComponent();
    EditorPane pane = (EditorPane) scrPane.getViewport().getView();
    return pane;
  }

  public EditorPane getPaneAtIndex(int index) {
    JScrollPane scrPane = (JScrollPane) tabPane.getComponentAt(index);
    EditorPane codePane = (EditorPane) scrPane.getViewport().getView();
    return codePane;
  }

  public void setCurrentFileTabTitle(String fname) {
    int tabIndex = tabPane.getSelectedIndex();
    setFileTabTitle(fname, tabIndex);
  }

  public String getCurrentFileTabTitle() {
    String fname = tabPane.getTitleAt(tabPane.getSelectedIndex());
    if (fname.startsWith("*")) {
      return fname.substring(1);
    } else {
      return fname;
    }
  }

  public void setCurrentFileTabTitleDirty(boolean isDirty) {
    int i = tabPane.getSelectedIndex();
    String title = tabPane.getTitleAt(i);
    if (!isDirty && title.startsWith("*")) {
      title = title.substring(1);
      tabPane.setTitleAt(i, title);
    } else if (isDirty && !title.startsWith("*")) {
      title = "*" + title;
      tabPane.setTitleAt(i, title);
    }
  }

  public void setFileTabTitle(String fName, int tabIndex) {
    String sName = new File(fName).getName();
    int i = sName.lastIndexOf(".");
    if (i > 0) {
      tabPane.setTitleAt(tabIndex, sName.substring(0, i));
    } else {
      tabPane.setTitleAt(tabIndex, sName);
    }
    this.setTitle(new File(fName).getAbsolutePath());
  }

  public ArrayList<String> getOpenedFilenames() {
    int nTab = tabPane.getTabCount();
    File file = null;
    String filePath;
    ArrayList<String> filenames = new ArrayList<String>(0);
    if (nTab > 0) {
      for (int i = 0; i < nTab; i++) {
        EditorPane codePane = getPaneAtIndex(i);
        file = codePane.getCurrentFile(false);
        if (file != null) {
          filePath = FileManager.slashify(file.getAbsolutePath(), false);
          filePath = filePath.substring(0, filePath.lastIndexOf("/"));
          filenames.add(filePath);
        } else {
          filenames.add("");
        }
      }
    }
    return filenames;
  }

  public int isAlreadyOpen(String filename) {
    int aot = getOpenedFilenames().indexOf(filename);
    if (aot > -1 && aot < (tabPane.getTabCount() - 1)) {
      alreadyOpenedTab = aot;
      return aot;
    }
    return -1;
  }

  private void autoCheckUpdate() {
    PreferencesUser pref = PreferencesUser.getInstance();
    if (!pref.getCheckUpdate()) {
      return;
    }
    long last_check = pref.getCheckUpdateTime();
    long now = (new Date()).getTime();
    if (now - last_check > 1000 * 604800) {
      Debug.log(3, "autocheck update");
      (new HelpAction()).checkUpdate(true);
    }
    pref.setCheckUpdateTime();
  }

  public synchronized boolean isRunningScript() {
    return ideIsRunningScript;
  }

  public synchronized void setIsRunningScript(boolean state) {
    ideIsRunningScript = state;
  }

  protected boolean doBeforeRun() {
    int action;
    if (checkDirtyPanes()) {
      if (prefs.getPrefMoreRunSave()) {
        action = WARNING_ACCEPTED;
      } else {
        action = askForSaveAll("Run");
        if (action < 0) {
          return false;
        }
      }
      saveSession(action, false);
    }
    Settings.ActionLogs = prefs.getPrefMoreLogActions();
    Settings.DebugLogs = prefs.getPrefMoreLogDebug();
    Settings.InfoLogs = prefs.getPrefMoreLogInfo();
    Settings.Highlight = prefs.getPrefMoreHighlight();
    Settings.OcrTextSearch = prefs.getPrefMoreTextSearch();
    Settings.OcrTextRead = prefs.getPrefMoreTextOCR();
    runTime.resetProject();
    return true;
  }

  protected boolean doBeforeQuit() {
    if (checkDirtyPanes()) {
      int action = askForSaveAll("Quit");
      if (action < 0) {
        return false;
      }
      return saveSession(action, true);
    }
    return saveSession(WARNING_DO_NOTHING, true);
  }

  private int askForSaveAll(String typ) {
//TODO I18N
    String warn = "Some scripts are not saved yet!";
    String title = SikuliIDEI18N._I("dlgAskCloseTab");
    String[] options = new String[3];
    options[WARNING_DO_NOTHING] = typ + " immediately";
    options[WARNING_ACCEPTED] = "Save all and " + typ;
    options[WARNING_CANCEL] = SikuliIDEI18N._I("cancel");
    int ret = JOptionPane.showOptionDialog(this, warn, title, 0, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
    if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
      return -1;
    }
    return ret;
  }

  public void doAbout() {
    //TODO full featured About
    String info = "You are running " + runTime.SikuliVersionIDE
            + "\n\nNeed help? -> start with Help Menu\n\n"
            + "*** Have fun ;-)\n\n"
            + "Tsung-Hsiang Chang aka vgod\n"
            + "Tom Yeh\n"
            + "Raimund Hocke aka RaiMan\n\n"
            + "\n\nBuild: " + runTime.SikuliVersionBuild;
    JOptionPane.showMessageDialog(this, info,
            "Sikuli About", JOptionPane.PLAIN_MESSAGE);
  }

  private static String[] collectOptions(String type, String[] args) {
    List<String> resArgs = new ArrayList<String>();
    if (args != null) {
      resArgs.addAll(Arrays.asList(args));
    }
    String msg = "-----------------------   You might set some options    -----------------------";
    msg += "\n\n";
    msg += "-r name       ---   Run script name: foo[.sikuli] or foo.skl (no IDE window)";
    msg += "\n";
    msg += "-u [file]        ---   Write user log messages to file (default: <WorkingFolder>/UserLog.txt )";
    msg += "\n";
    msg += "-f [file]         ---   Write Sikuli log messages to file (default: <WorkingFolder>/SikuliLog.txt)";
    msg += "\n";
    msg += "-d n             ---   Set a higher level n for Sikuli's debug messages (default: 0)";
    msg += "\n";
    msg += "-- …more…         All space delimited entries after -- go to sys.argv";
    msg += "\n                           \"<some text>\" makes one parameter (may contain intermediate blanks)";
    msg += "\n\n";
    msg += "-------------------------------------------------------------------------";
    msg += "\n";
    msg += "-d                Special debugging option in case of mysterious errors:";
    msg += "\n";
    msg += "                    Debug level is set to 3 and debug output goes to <WorkingFolder>/SikuliLog.txt";
    msg += "\n";
    msg += "                    Content might be used to ask questions or report bugs";
    msg += "\n";
    msg += "-------------------------------------------------------------------------";
    msg += "\n";
    msg += "                    Just click OK to start IDE with no options - defaults will be used";

    String ret = JOptionPane.showInputDialog(null, msg, "SikuliX: collect runtime options",
            JOptionPane.QUESTION_MESSAGE);

    if (ret == null) {
      return null;
    }
    log(3, "collectOptions: returned [" + ret + "]");
    if (!ret.isEmpty()) {
      System.setProperty("sikuli.SIKULI_COMMAND", ret);
      resArgs.addAll(Arrays.asList(ret.split(" +")));
    }
    return resArgs.toArray(new String[0]);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isInited --- RaiMan not used">
  public boolean isInited() {
    return _inited;
  }
  //</editor-fold>

  private JMenuItem createMenuItem(JMenuItem item, KeyStroke shortcut, ActionListener listener) {
    if (shortcut != null) {
      item.setAccelerator(shortcut);
    }
    item.addActionListener(listener);
    return item;
  }

  private JMenuItem createMenuItem(String name, KeyStroke shortcut, ActionListener listener) {
    JMenuItem item = new JMenuItem(name);
    return createMenuItem(item, shortcut, listener);
  }

  class MenuAction implements ActionListener {

    protected Method actMethod = null;
    protected String action;

    public MenuAction() {
    }

    public MenuAction(String item) throws NoSuchMethodException {
      Class[] paramsWithEvent = new Class[1];
      try {
        paramsWithEvent[0] = Class.forName("java.awt.event.ActionEvent");
        actMethod = this.getClass().getMethod(item, paramsWithEvent);
        action = item;
      } catch (ClassNotFoundException cnfe) {
        log(-1, "Can't find menu action: " + cnfe);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (actMethod != null) {
        try {
          Debug.log(3, "MenuAction." + action);
          Object[] params = new Object[1];
          params[0] = e;
          actMethod.invoke(this, params);
        } catch (Exception ex) {
          log(-1, "Problem when trying to invoke menu action %s\nError: %s",
                  action, ex.getMessage());
        }
      }
    }
  }

  private static JMenu recentMenu = null;
  private static Map<String, String> recentProjects = new HashMap<String, String>();
  private static java.util.List<String> recentProjectsMenu = new ArrayList<String>();
  private static int recentMax = 10;
  private static int recentMaxMax = recentMax + 10;

  //<editor-fold defaultstate="collapsed" desc="Init FileMenu">
  private void initFileMenu() throws NoSuchMethodException {
    JMenuItem jmi;
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);

    if (showAbout) {
      _fileMenu.add(createMenuItem("About SikuliX",
              KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, scMask),
              new FileAction(FileAction.ABOUT)));
      _fileMenu.addSeparator();
    }

    _fileMenu.add(createMenuItem(_I("menuFileNew"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, scMask),
            new FileAction(FileAction.NEW)));

    jmi = _fileMenu.add(createMenuItem(_I("menuFileOpen"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, scMask),
            new FileAction(FileAction.OPEN)));
    jmi.setName("OPEN");

    recentMenu = new JMenu(_I("menuRecent"));

    if (Settings.experimental) {
      _fileMenu.add(recentMenu);
    }

    if (Settings.isMac() && !Settings.handlesMacBundles) {
      _fileMenu.add(createMenuItem("Open folder.sikuli ...",
              null,
              //            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, scMask),
              new FileAction(FileAction.OPEN_FOLDER)));
    }

    jmi = _fileMenu.add(createMenuItem(_I("menuFileSave"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, scMask),
            new FileAction(FileAction.SAVE)));
    jmi.setName("SAVE");

    jmi = _fileMenu.add(createMenuItem(_I("menuFileSaveAs"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                    InputEvent.SHIFT_MASK | scMask),
            new FileAction(FileAction.SAVE_AS)));
    jmi.setName("SAVE_AS");

    if (Settings.isMac() && !Settings.handlesMacBundles) {
      _fileMenu.add(createMenuItem(_I("Save as folder.sikuli ..."),
              //            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
              //            InputEvent.SHIFT_MASK | scMask),
              null,
              new FileAction(FileAction.SAVE_AS_FOLDER)));
    }

//TODO    _fileMenu.add(createMenuItem(_I("menuFileSaveAll"),
    _fileMenu.add(createMenuItem("Save all",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                    InputEvent.CTRL_MASK | scMask),
            new FileAction(FileAction.SAVE_ALL)));

    _fileMenu.add(createMenuItem(_I("menuFileExport"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E,
                    InputEvent.SHIFT_MASK | scMask),
            new FileAction(FileAction.EXPORT)));

    _fileMenu.add(createMenuItem("Export as jar",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, scMask),
            new FileAction(FileAction.ASJAR)));

    _fileMenu.add(createMenuItem("Export as runnable jar",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J,
                    InputEvent.SHIFT_MASK | scMask),
            new FileAction(FileAction.ASRUNJAR)));

    jmi = _fileMenu.add(createMenuItem(_I("menuFileCloseTab"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, scMask),
            new FileAction(FileAction.CLOSE_TAB)));
    jmi.setName("CLOSE_TAB");

    if (showPrefs) {
      _fileMenu.addSeparator();
      _fileMenu.add(createMenuItem(_I("menuFilePreferences"),
              KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, scMask),
              new FileAction(FileAction.PREFERENCES)));
    }

    if (showQuit) {
      _fileMenu.addSeparator();
      _fileMenu.add(createMenuItem(_I("menuFileQuit"),
              null, new FileAction(FileAction.QUIT)));
    }
  }

  public FileAction getFileAction(int tabIndex) {
    return new FileAction(tabIndex);
  }

  class FileAction extends MenuAction {

    static final String ABOUT = "doAbout";
    static final String NEW = "doNew";
    static final String INSERT = "doInsert";
    static final String OPEN = "doLoad";
    static final String RECENT = "doRecent";
    static final String OPEN_FOLDER = "doLoadFolder";
    static final String SAVE = "doSave";
    static final String SAVE_AS = "doSaveAs";
    static final String SAVE_AS_FOLDER = "doSaveAsFolder";
    static final String SAVE_ALL = "doSaveAll";
    static final String EXPORT = "doExport";
    static final String ASJAR = "doAsJar";
    static final String ASRUNJAR = "doAsRunJar";
    static final String CLOSE_TAB = "doCloseTab";
    static final String PREFERENCES = "doPreferences";
    static final String QUIT = "doQuit";
    static final String ENTRY = "doRecent";
    private int targetTab = -1;

    public FileAction() {
      super();
    }

    public FileAction(int tabIndex) {
      super();
      targetTab = tabIndex;
    }

    public FileAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doAbout(ActionEvent ae) {
      sikulixIDE.doAbout();
    }

    public void doQuit(ActionEvent ae) {
      log(lvl, "doQuit requested");
      if (!doBeforeQuit()) {
        return;
      }
      while (true) {
        EditorPane codePane = sikulixIDE.getCurrentCodePane();
        if (codePane == null) {
          break;
        }
        if (!sikulixIDE.closeCurrentTab()) {
          return;
        }
      }
      Sikulix.cleanUp(0);
      System.exit(0);
    }

    public void doPreferences(ActionEvent ae) {
      sikulixIDE.showPreferencesWindow();
    }

    public void doNew(ActionEvent ae) {
      EditorPane ep = doNew(ae, -1);
      ep.getSrcBundle();
      ep.initBeforeLoad(null);
    }

    public EditorPane doNew(ActionEvent ae, int tabIndex) {
      log(lvl, "doNew: create new tab at: %d", tabIndex);
      EditorPane codePane = new EditorPane(sikulixIDE);
      JScrollPane scrPane = new JScrollPane(codePane);
      lineNumberColumn = new EditorLineNumberView(codePane);
      scrPane.setRowHeaderView(lineNumberColumn);
      if (ae == null) {
        if (tabIndex < 0 || tabIndex >= tabPane.getTabCount()) {
          tabPane.addTab(_I("tabUntitled"), scrPane);
        } else {
          tabPane.addTab(_I("tabUntitled"), scrPane, tabIndex);
        }
        tabPane.setSelectedIndex(tabIndex < 0 ? tabPane.getTabCount() - 1 : tabIndex);
      } else {
        tabPane.addTab(_I("tabUntitled"), scrPane, 0);
        tabPane.setSelectedIndex(0);
      }
//      codePane.getSrcBundle();
      codePane.requestFocus();
      return codePane;
    }

    public void doInsert(ActionEvent ae) {
      doLoad(null);
    }

    public void doLoad(ActionEvent ae) {
      boolean accessingAsFile = false;
      if (Settings.isMac()) {
        accessingAsFile = !ACCESSING_AS_FOLDER;
        ACCESSING_AS_FOLDER = false;
      }
      alreadyOpenedTab = tabPane.getSelectedIndex();
      String fname = tabPane.getLastClosed();
      try {
        EditorPane codePane = doNew(null, targetTab);
        if (ae != null || fname == null) {
          codePane.isSourceBundleTemp();
          fname = codePane.loadFile(accessingAsFile);
        } else {
          codePane.loadFile(fname);
          if (codePane.hasEditingFile()) {
            setCurrentFileTabTitle(fname);
          } else {
            fname = null;
          }
        }
        if (fname != null) {
          sikulixIDE.setCurrentFileTabTitle(fname);
        } else {
          if (ae != null) {
            doCloseTab(null);
          }
          tabPane.setSelectedIndex(alreadyOpenedTab);
        }
        doRecentAdd(getCurrentCodePane());
      } catch (IOException eio) {
        log(-1, "Problem when trying to load %s\nError: %s",
                fname, eio.getMessage());
      }
    }

    private void doRecentAdd(EditorPane codePane) {
      String fPath = new File(codePane.getSrcBundle()).getAbsolutePath();
      if (Settings.experimental) {
        log(3, "doRecentAdd: %s", fPath);
        String fName = new File(fPath).getName();
        if (recentProjectsMenu.contains(fName)) {
          recentProjectsMenu.remove(fName);
        } else {
          recentProjects.put(fName, fPath);
          if (recentProjectsMenu.size() == recentMaxMax) {
            String fObsolete = recentProjectsMenu.remove(recentMax - 1);
            recentProjects.remove(fObsolete);
          }
        }
        recentProjectsMenu.add(0, fName);
        recentMenu.removeAll();
        for (String entry : recentProjectsMenu.subList(1, recentProjectsMenu.size())) {
          if (isAlreadyOpen(recentProjects.get(entry)) > -1) {
            continue;
          }
          try {
            recentMenu.add(createMenuItem(entry,
                    null,
                    new FileAction(FileAction.ENTRY)));
          } catch (NoSuchMethodException ex) {
          }
        }
      }
    }

    public void doRecent(ActionEvent ae) {
      log(3, "doRecent: menuOpenRecent: %s", ae.getActionCommand());
    }

    public void doLoadFolder(ActionEvent ae) {
      Debug.log(3, "IDE: doLoadFolder requested");
      ACCESSING_AS_FOLDER = true;
      doLoad(ae);
    }

    public void doSave(ActionEvent ae) {
      String fname = null;
      try {
        EditorPane codePane = getCurrentCodePane();
        fname = codePane.saveFile();
        if (fname != null) {
          fname = codePane.getSrcBundle();
          setCurrentFileTabTitle(fname);
          tabPane.setLastClosed(fname);
        }
      } catch (Exception ex) {
        if (ex instanceof IOException) {
          log(-1, "Problem when trying to save %s\nError: %s",
                  fname, ex.getMessage());
        } else {
          log(-1, "A non-IOException-problem when trying to save %s\nError: %s",
                  fname, ex.getMessage());
        }
      }
    }

    public boolean doSaveIntern(int tabIndex) {
      int currentTab = tabPane.getSelectedIndex();
      tabPane.setSelectedIndex(tabIndex);
      boolean retval = true;
      EditorPane codePane = getPaneAtIndex(tabIndex);
      String fname = null;
      try {
        fname = codePane.saveFile();
        if (fname != null) {
          setFileTabTitle(fname, tabIndex);
        } else {
          retval = false;
        }
      } catch (Exception ex) {
        log(-1, "Problem when trying to save %s\nError: %s",
                fname, ex.getMessage());
        retval = false;
      }
      tabPane.setSelectedIndex(currentTab);
      return retval;
    }

    public void doSaveAs(ActionEvent ae) {
      boolean accessingAsFile = false;
      if (Settings.isMac()) {
        accessingAsFile = !ACCESSING_AS_FOLDER;
        ACCESSING_AS_FOLDER = false;
      }
      String fname = null;
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log(lvl, "doSaveAs requested: %s", orgName);
      try {
        fname = codePane.saveAsFile(accessingAsFile);
        if (fname != null) {
          setCurrentFileTabTitle(fname);
        } else {
          log(-1, "doSaveAs: %s not completed", orgName);
        }
      } catch (Exception ex) {
        log(-1, "doSaveAs: %s Error: %s", orgName, ex.getMessage());
      }
    }

    public void doSaveAsFolder(ActionEvent ae) {
      log(lvl, "doSaveAsFolder requested");
      ACCESSING_AS_FOLDER = true;
      doSaveAs(ae);
    }

    public void doSaveAll(ActionEvent ae) {
      log(lvl, "doSaveAll requested");
      if (!checkDirtyPanes()) {
        return;
      }
      saveSession(IS_SAVE_ALL, false);
    }

    public void doExport(ActionEvent ae) {
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log(lvl, "doExport requested: %s", orgName);
      String fname = null;
      try {
        fname = codePane.exportAsZip();
      } catch (Exception ex) {
        log(-1, "Problem when trying to save %s\nError: %s",
                fname, ex.getMessage());
      }
    }

    public void doAsJar(ActionEvent ae) {
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log(lvl, "doAsJar requested: %s", orgName);
      if (codePane.isDirty()) {
        Sikulix.popError("Please save script before!", "Export as jar");
      } else {
        File fScript = codePane.getCurrentFile();
        List<String> options = new ArrayList<>();
        options.add("plain");
        options.add(fScript.getParentFile().getAbsolutePath());
        String fpJar = FileManager.makeScriptjar(options);
        if (null != fpJar) {
          Sikulix.popup(fpJar, "Export as jar ...");
        } else {
          Sikulix.popError("did not work for: " + orgName, "Export as jar");
        }
      }
    }

    public void doAsRunJar(ActionEvent ae) {
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log(lvl, "doAsRunJar requested: %s", orgName);
      if (codePane.isDirty()) {
        Sikulix.popError("Please save script before!", "Export as runnable jar");
      } else {
        File fScript = codePane.getCurrentFile();
        List<String> options = new ArrayList<>();
        options.add(fScript.getParentFile().getAbsolutePath());
        Sikulix.popup("... this may take some 10 seconds\nclick ok and wait for result popup" +
                        "\nthere is no progressindication",
                "Export as runnable jar");
        String fpJar = FileManager.makeScriptjar(options);
        if (null != fpJar) {
          Sikulix.popup(fpJar, "Export as runnable jar ...");
        } else {
          Sikulix.popError("did not work for: " + orgName, "Export as runnable jar");
        }
      }
    }

    public void doCloseTab(ActionEvent ae) {
      if (ae == null) {
        tabPane.remove(tabPane.getSelectedIndex());
        return;
      }
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log(lvl, "doCloseTab requested: %s", orgName);
      try {
        if (codePane.close()) {
          tabPane.remove(tabPane.getSelectedIndex());
        }
      } catch (Exception ex) {
        Debug.info("Can't close this tab: %s", ex.getMessage());
      }
      codePane = getCurrentCodePane();
      if (codePane != null) {
        codePane.requestFocus();
      } else if (ae != null) {
        (new FileAction()).doNew(null);
      }
    }
  }

  public void showPreferencesWindow() {
    PreferencesWin pwin = new PreferencesWin();
    pwin.setAlwaysOnTop(true);
    pwin.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    if (!Settings.isJava7()) {
      pwin.setLocation(getLocation());
    }
    pwin.setVisible(true);
  }

  public boolean closeCurrentTab() {
    EditorPane pane = getCurrentCodePane();
    (new FileAction()).doCloseTab(null);
    if (pane == getCurrentCodePane()) {
      return false;
    }
    return true;
  }

  public boolean quit() {
    (new FileAction()).doQuit(null);
    if (getCurrentCodePane() == null) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean checkDirtyPanes() {
    for (int i = 0; i < tabPane.getTabCount(); i++) {
      try {
        EditorPane codePane = getPaneAtIndex(i);
        if (codePane.isDirty()) {
          //RaiMan not used: getRootPane().putClientProperty("Window.documentModified", true);
          return true;
        }
      } catch (Exception e) {
        Debug.error("checkDirtyPanes: " + e.getMessage());
      }
    }
    //RaiMan not used: getRootPane().putClientProperty("Window.documentModified", false);
    return false;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Init EditMenu">
  private void initEditMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _editMenu.setMnemonic(java.awt.event.KeyEvent.VK_E);
    JMenuItem undoItem = _editMenu.add(_undoAction);
    undoItem.setAccelerator(
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, scMask));
    JMenuItem redoItem = _editMenu.add(_redoAction);
    redoItem.setAccelerator(
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, scMask | InputEvent.SHIFT_MASK));
    _editMenu.addSeparator();

    _editMenu.add(createMenuItem(_I("menuEditCut"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, scMask),
            new EditAction(EditAction.CUT)));
    _editMenu.add(createMenuItem(_I("menuEditCopy"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, scMask),
            new EditAction(EditAction.COPY)));
    _editMenu.add(createMenuItem(_I("menuEditPaste"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, scMask),
            new EditAction(EditAction.PASTE)));
    _editMenu.add(createMenuItem(_I("menuEditSelectAll"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, scMask),
            new EditAction(EditAction.SELECT_ALL)));

    _editMenu.addSeparator();
    JMenu findMenu = new JMenu(_I("menuFind"));
    _findHelper = new FindAction();
    findMenu.setMnemonic(KeyEvent.VK_F);
    findMenu.add(createMenuItem(_I("menuFindFind"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, scMask),
            new FindAction(FindAction.FIND)));
    findMenu.add(createMenuItem(_I("menuFindFindNext"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, scMask),
            new FindAction(FindAction.FIND_NEXT)));
    findMenu.add(createMenuItem(_I("menuFindFindPrev"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, scMask | InputEvent.SHIFT_MASK),
            new FindAction(FindAction.FIND_PREV)));
    _editMenu.add(findMenu);

    _editMenu.addSeparator();
    _editMenu.add(createMenuItem(_I("menuEditIndent"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0),
            new EditAction(EditAction.INDENT)));
    _editMenu.add(createMenuItem(_I("menuEditUnIndent"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, InputEvent.SHIFT_MASK),
            new EditAction(EditAction.UNINDENT)));
  }

  class EditAction extends MenuAction {

    static final String CUT = "doCut";
    static final String COPY = "doCopy";
    static final String PASTE = "doPaste";
    static final String SELECT_ALL = "doSelectAll";
    static final String INDENT = "doIndent";
    static final String UNINDENT = "doUnindent";

    public EditAction() {
      super();
    }

    public EditAction(String item) throws NoSuchMethodException {
      super(item);
    }

    private void performEditorAction(String action, ActionEvent ae) {
      EditorPane pane = getCurrentCodePane();
      pane.getActionMap().get(action).actionPerformed(ae);
    }

    public void doCut(ActionEvent ae) {
//TODO delete current line if no selection
      performEditorAction(DefaultEditorKit.cutAction, ae);
    }

    public void doCopy(ActionEvent ae) {
//TODO copy current line if no selection
      performEditorAction(DefaultEditorKit.copyAction, ae);
    }

    public void doPaste(ActionEvent ae) {
      performEditorAction(DefaultEditorKit.pasteAction, ae);
    }

    public void doSelectAll(ActionEvent ae) {
      performEditorAction(DefaultEditorKit.selectAllAction, ae);
    }

    public void doIndent(ActionEvent ae) {
      EditorPane pane = getCurrentCodePane();
      (new SikuliEditorKit.InsertTabAction()).actionPerformed(pane);
    }

    public void doUnindent(ActionEvent ae) {
      EditorPane pane = getCurrentCodePane();
      (new SikuliEditorKit.DeindentAction()).actionPerformed(pane);
    }
  }

  class OpenRecent extends MenuAction {

    public OpenRecent() {
      super();
    }

    public void openRecent(ActionEvent ae) {
      log(lvl, "openRecent: %s", ae.getActionCommand());
    }
  }

  class FindAction extends MenuAction {

    static final String FIND = "doFind";
    static final String FIND_NEXT = "doFindNext";
    static final String FIND_PREV = "doFindPrev";

    public FindAction() {
      super();
    }

    public FindAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doFind(ActionEvent ae) {
      _searchField.selectAll();
      _searchField.requestFocus();
    }

    public void doFindNext(ActionEvent ae) {
      findNext(_searchField.getText());
    }

    public void doFindPrev(ActionEvent ae) {
      findPrev(_searchField.getText());
    }

    private boolean _find(String str, int begin, boolean forward) {
      if (str == "!") {
        return false;
      }
      EditorPane codePane = getCurrentCodePane();
      int pos = codePane.search(str, begin, forward);
      Debug.log(7, "find \"" + str + "\" at " + begin + ", found: " + pos);
      if (pos < 0) {
        return false;
      }
      return true;
    }

    public boolean findStr(String str) {
      if (getCurrentCodePane() != null) {
        return _find(str, getCurrentCodePane().getCaretPosition(), true);
      }
      return false;
    }

    public boolean findPrev(String str) {
      if (getCurrentCodePane() != null) {
        return _find(str, getCurrentCodePane().getCaretPosition(), false);
      }
      return false;
    }

    public boolean findNext(String str) {
      if (getCurrentCodePane() != null) {
        return _find(str,
                getCurrentCodePane().getCaretPosition() + str.length(),
                true);
      }
      return false;
    }

    public void setFailed(boolean failed) {
      Debug.log(7, "search failed: " + failed);
      _searchField.setBackground(Color.white);
      if (failed) {
        _searchField.setForeground(COLOR_SEARCH_FAILED);
      } else {
        _searchField.setForeground(COLOR_SEARCH_NORMAL);
      }
    }
  }

  class UndoAction extends AbstractAction {

    public UndoAction() {
      super(_I("menuEditUndo"));
      setEnabled(false);
    }

    public void updateUndoState() {
      if (getCurrentCodePane() != null
              && getCurrentCodePane().getUndoManager().canUndo()) {
        setEnabled(true);
      } else {
        setEnabled(false);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      EditorUndoManager undo = getCurrentCodePane().getUndoManager();
      try {
        undo.undo();
      } catch (CannotUndoException ex) {
      }
      updateUndoState();
      _redoAction.updateRedoState();
    }
  }

  class RedoAction extends AbstractAction {

    public RedoAction() {
      super(_I("menuEditRedo"));
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      EditorUndoManager undo = getCurrentCodePane().getUndoManager();
      try {
        undo.redo();
      } catch (CannotRedoException ex) {
      }
      updateRedoState();
      _undoAction.updateUndoState();
    }

    protected void updateRedoState() {
      if (getCurrentCodePane() != null
              && getCurrentCodePane().getUndoManager().canRedo()) {
        setEnabled(true);
      } else {
        setEnabled(false);
      }
    }
  }

  public void updateUndoRedoStates() {
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Init Run Menu">
  private void initRunMenu() throws NoSuchMethodException {
    JMenuItem item;
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _runMenu.setMnemonic(java.awt.event.KeyEvent.VK_R);
    item = _runMenu.add(createMenuItem(_I("menuRunRun"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, scMask),
            new RunAction(RunAction.RUN)));
    item.setName("RUN");
    item = _runMenu.add(createMenuItem(_I("menuRunRunAndShowActions"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
                    InputEvent.ALT_MASK | scMask),
            new RunAction(RunAction.RUN_SHOW_ACTIONS)));
    item.setName("RUN_SLOWLY");

    PreferencesUser pref = PreferencesUser.getInstance();
    item = createMenuItem(_I("menuRunStop"),
            KeyStroke.getKeyStroke(
                    pref.getStopHotkey(), pref.getStopHotkeyModifiers()),
            new RunAction(RunAction.RUN_SHOW_ACTIONS));
    item.setEnabled(false);
    _runMenu.add(item);
  }

  class RunAction extends MenuAction {

    static final String RUN = "run";
    static final String RUN_SHOW_ACTIONS = "runShowActions";

    public RunAction() {
      super();
    }

    public RunAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void run(ActionEvent ae) {
      doRun(_btnRun);
    }

    public void runShowActions(ActionEvent ae) {
      doRun(_btnRunViz);
    }

    private void doRun(ButtonRun btn) {
      btn.runCurrentScript();
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Init View Menu">
  private void initViewMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _viewMenu.setMnemonic(java.awt.event.KeyEvent.VK_V);

    if (prefs.getPrefMoreCommandBar()) {
      chkShowCmdList = new JCheckBoxMenuItem(_I("menuViewCommandList"), true);
      _viewMenu.add(createMenuItem(chkShowCmdList,
              KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, scMask),
              new ViewAction(ViewAction.CMD_LIST)));
    }

    chkShowThumbs = new JCheckBoxMenuItem(_I("menuViewShowThumbs"), false);
    _viewMenu.add(createMenuItem(chkShowThumbs,
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, scMask),
            new ViewAction(ViewAction.SHOW_THUMBS)));

//TODO Message Area clear
//TODO Message Area LineBreak
  }

  class ViewAction extends MenuAction {

    static final String UNIT_TEST = "toggleUnitTest";
    static final String CMD_LIST = "toggleCmdList";
    static final String SHOW_THUMBS = "toggleShowThumbs";

    public ViewAction() {
      super();
    }

    public ViewAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void toggleCmdList(ActionEvent ae) {
      _cmdList.setCollapsed(!_cmdList.isCollapsed());
    }

    public void toggleShowThumbs(ActionEvent ae) {
      getCurrentCodePane().showThumbs = chkShowThumbs.getState();
      getCurrentCodePane().saveCaretPosition();
      if (!getCurrentCodePane().reparse()) {
        chkShowThumbs.setState(!chkShowThumbs.getState());
        getCurrentCodePane().showThumbs = chkShowThumbs.getState();
      }
    }

    public void toggleUnitTest(ActionEvent ae) {
      if (_chkShowUnitTest.getState()) {
        _sidePane.setCollapsed(false);
      } else {
        _sidePane.setCollapsed(true);
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Init ToolMenu">
  private void initToolMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _toolMenu.setMnemonic(java.awt.event.KeyEvent.VK_T);

    _toolMenu.add(createMenuItem(_I("menuToolExtensions"),
            null,
            new ToolAction(ToolAction.EXTENSIONS)));

    _toolMenu.add(createMenuItem(_I("menuToolAndroid"),
            null,
            new ToolAction(ToolAction.ANDROID)));
  }

  class ToolAction extends MenuAction {

    static final String EXTENSIONS = "extensions";
    static final String ANDROID = "android";

    ToolAction() {
      super();
    }

    public ToolAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void extensions(ActionEvent ae) {
      showExtensionsFrame();
    }

    public void android(ActionEvent ae) {
      androidSupport();
    }

  }

  private void showExtensionsFrame() {
//    String warn = "You might proceed, if you\n"
//            + "- have some programming skills\n"
//            + "- read the docs about extensions\n"
//            + "- know what you are doing\n\n"
//            + "Otherwise you should press Cancel!";
    String warn = "Not available yet - click what you like ;-)";
    String title = "Need your attention!";
    String[] options = new String[3];
    options[WARNING_DO_NOTHING] = "OK";
    options[WARNING_ACCEPTED] = "Be quiet!";
    options[WARNING_CANCEL] = "Cancel";
    int ret = JOptionPane.showOptionDialog(this, warn, title, 0, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
    if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
      return;
    }
    if (ret == WARNING_ACCEPTED) {
      //TODO set prefs to be quiet on extensions warning
    }
    ;
    ExtensionManagerFrame extmg = ExtensionManagerFrame.getInstance();
    if (extmg != null) {
      extmg.setVisible(true);
    }
  }

  private static IScreen defaultScreen = null;

  public static IScreen getDefaultScreen() {
    return defaultScreen;
  }

  private void androidSupport() {
//    if (runTime.isJava9("Android/adbc not working yet")) {
//      return;
//    }
    final ADBScreen aScr = new ADBScreen();
    String title = "Android Support - !!EXPERIMENTAL!!";
    if (aScr.isValid()) {
      String warn = "Device found: " + aScr.getDeviceDescription() + "\n\n" +
              "click Check: a short test is run with the device\n" +
              "click Default: set device as default screen for capture\n" +
              "click Cancel: nothing is done (default screen is reset)\n" +
              "\nBE PREPARED: Feature is experimental - no guarantee ;-)";
      String[] options = new String[3];
      options[WARNING_DO_NOTHING] = "Check";
      options[WARNING_ACCEPTED] = "Default";
      options[WARNING_CANCEL] = "Cancel";
      int ret = JOptionPane.showOptionDialog(this, warn, title, 0, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
      if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
        defaultScreen = null;
        return;
      }
      if (ret == WARNING_DO_NOTHING) {
        SikuliIDE.hideIDE();
        Thread test = new Thread() {
          @Override
          public void run() {
            androidSupportTest(aScr);
          }
        };
        test.start();
      } else if (ret == WARNING_ACCEPTED) {
        defaultScreen = aScr;
        return;
      }
    } else if (!ADBClient.isAdbAvailable) {
      Sikulix.popError("Package adb seems not to be available.\nIt must be installed for Android support.", title);
    } else {
      Sikulix.popError("No android device attached", title);
    }
  }

  private void androidSupportTest(ADBScreen aScr) {
    ADBTest.ideTest(aScr);
    SikuliIDE.showIDE();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Init Help Menu">
  private void initHelpMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);

    _helpMenu.add(createMenuItem(_I("menuHelpQuickStart"),
            null, new HelpAction(HelpAction.QUICK_START)));
    _helpMenu.addSeparator();

    _helpMenu.add(createMenuItem(_I("menuHelpGuide"),
            null, new HelpAction(HelpAction.OPEN_DOC)));
    _helpMenu.add(createMenuItem(_I("menuHelpDocumentations"),
            null, new HelpAction(HelpAction.OPEN_GUIDE)));
    _helpMenu.add(createMenuItem(_I("menuHelpFAQ"),
            null, new HelpAction(HelpAction.OPEN_FAQ)));
    _helpMenu.add(createMenuItem(_I("menuHelpAsk"),
            null, new HelpAction(HelpAction.OPEN_ASK)));
    _helpMenu.add(createMenuItem(_I("menuHelpBugReport"),
            null, new HelpAction(HelpAction.OPEN_BUG_REPORT)));

//    _helpMenu.add(createMenuItem(_I("menuHelpTranslation"),
//            null, new HelpAction(HelpAction.OPEN_TRANSLATION)));
    _helpMenu.addSeparator();
    _helpMenu.add(createMenuItem(_I("menuHelpHomepage"),
            null, new HelpAction(HelpAction.OPEN_HOMEPAGE)));

    _helpMenu.addSeparator();
    _helpMenu.add(createMenuItem(_I("menuHelpCheckUpdate"),
            null, new HelpAction(HelpAction.CHECK_UPDATE)));
  }

  class HelpAction extends MenuAction {

    static final String CHECK_UPDATE = "doCheckUpdate";
    static final String QUICK_START = "openQuickStart";
    static final String OPEN_DOC = "openDoc";
    static final String OPEN_GUIDE = "openTutor";
    static final String OPEN_FAQ = "openFAQ";
    static final String OPEN_ASK = "openAsk";
    static final String OPEN_BUG_REPORT = "openBugReport";
    static final String OPEN_TRANSLATION = "openTranslation";
    static final String OPEN_HOMEPAGE = "openHomepage";

    public HelpAction() {
      super();
    }

    public HelpAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void openQuickStart(ActionEvent ae) {
      FileManager.openURL("http://sikulix.com/quickstart/");
    }

    public void openDoc(ActionEvent ae) {
      FileManager.openURL("http://sikulix-2014.readthedocs.org/en/latest/index.html");
    }

    public void openTutor(ActionEvent ae) {
      FileManager.openURL("http://www.sikuli.org/videos.html");
    }

    public void openFAQ(ActionEvent ae) {
      FileManager.openURL("https://answers.launchpad.net/sikuli/+faqs");
    }

    public void openAsk(ActionEvent ae) {
      String title = "SikuliX - Ask a question";
      String msg = "If you want to ask a question about SikuliX\n%s\n"
              + "\nplease do the following:"
              + "\n- after having clicked yes"
              + "\n   the page on Launchpad should open in your browser."
              + "\n- You should first check using Launchpad's search funktion,"
              + "\n   wether similar questions have already been asked."
              + "\n- If you decide to ask a new question,"
              + "\n   try to enter a short but speaking title"
              + "\n- In a new questions's text field first paste using ctrl/cmd-v"
              + "\n   which should enter the SikuliX version/system/java info"
              + "\n   that was internally stored in the clipboard before"
              + "\n\nIf you do not want to ask a question now: click No";
      askBugOrAnswer(msg, title, "https://answers.launchpad.net/sikuli");
    }

    public void openBugReport(ActionEvent ae) {
      String title = "SikuliX - Report a bug";
      String msg = "If you want to report a bug for SikuliX\n%s\n"
              + "\nplease do the following:"
              + "\n- after having clicked yes"
              + "\n   the page on Launchpad should open in your browser"
              + "\n- fill in a short but speaking bug title and create the bug"
              + "\n- in the bug's text field first paste using ctrl/cmd-v"
              + "\n   which should enter the SikuliX version/system/java info"
              + "\n   that was internally stored in the clipboard before"
              + "\n\nIf you do not want to report a bug now: click No";
      askBugOrAnswer(msg, title, "https://bugs.launchpad.net/sikuli/+filebug");
    }

    private void askBugOrAnswer(String msg, String title, String url) {
      String si = runTime.getSystemInfo();
      System.out.println(si);
      msg = String.format(msg, si);
      if (Sikulix.popAsk(msg, title)) {
        Clipboard clb = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection sic = new StringSelection(si.toString());
        clb.setContents(sic, sic);
        FileManager.openURL(url);
      }
    }

    public void openTranslation(ActionEvent ae) {
      FileManager.openURL("https://translations.launchpad.net/sikuli/sikuli-x/+translations");
    }

    public void openHomepage(ActionEvent ae) {
      FileManager.openURL("http://sikulix.com");
    }

    public void doCheckUpdate(ActionEvent ae) {
      if (!checkUpdate(false)) {
        JOptionPane.showMessageDialog(null,
                _I("msgNoUpdate"), runTime.SikuliVersionIDE,
                JOptionPane.INFORMATION_MESSAGE);
      }
    }

    public boolean checkUpdate(boolean isAutoCheck) {
      JFrame f = null;
      String ver = "";
      String details;
      AutoUpdater au = new AutoUpdater();
      if (!isAutoCheck) {
//TODO replace this hack: wait update check
        f = au.showUpdateFrame("Checking for new version ... please wait!",
                "Checking for new version ... please wait! Checking for new version ... please wait!", -1);
      }
      PreferencesUser pref = PreferencesUser.getInstance();
      Debug.log(3, "being asked to check update");
      int whatUpdate = au.checkUpdate();
      if (f != null) {
        f.dispose();
      }
      if (whatUpdate >= AutoUpdater.SOMEBETA) {
//TODO add Prefs wantBeta check
        whatUpdate -= AutoUpdater.SOMEBETA;
      }
      if (whatUpdate > 0) {
        if (whatUpdate == AutoUpdater.BETA) {
          ver = au.getBeta();
          details = au.getBetaDetails();
        } else {
          ver = au.getVersion();
          details = au.getDetails();
        }
        if (isAutoCheck && pref.getLastSeenUpdate().equals(ver)) {
          return false;
        }
        au.showUpdateFrame(_I("dlgUpdateAvailable", ver), details, whatUpdate);
        PreferencesUser.getInstance().setLastSeenUpdate(ver);
        return true;
      }
      return false;
    }
  }

  private void initMenuBars(JFrame frame) {
    try {
      initFileMenu();
      initEditMenu();
      initRunMenu();
      initViewMenu();
      initToolMenu();
      initHelpMenu();
    } catch (NoSuchMethodException e) {
      log(-1, "Problem when initializing menues\nError: %s", e.getMessage());
    }

    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_runMenu);
    _menuBar.add(_viewMenu);
    _menuBar.add(_toolMenu);
    _menuBar.add(_helpMenu);
    frame.setJMenuBar(_menuBar);
  }

  //<editor-fold defaultstate="collapsed" desc="Init LeftBar Commands">
  private String[] getCommandCategories() {
    String[] CommandCategories = {
            _I("cmdListFind"),
            _I("cmdListMouse"),
            _I("cmdListKeyboard"),
            _I("cmdListObserver")
    };
    return CommandCategories;
  }

  private String[][] getCommandsOnToolbar() {
    String[][] CommandsOnToolbar = {
            {"find"}, {"PATTERN"},
            {_I("cmdFind")},
            {"findAll"}, {"PATTERN"},
            {_I("cmdFindAll")},
            {"wait"}, {"PATTERN", "[timeout]"},
            {_I("cmdWait")},
            {"waitVanish"}, {"PATTERN", "[timeout]"},
            {_I("cmdWaitVanish")},
            {"exists"}, {"PATTERN", "[timeout]"},
            {_I("cmdExists")},
            {"----"}, {}, {},
            {"click"}, {"PATTERN", "[modifiers]"},
            {_I("cmdClick")},
            {"doubleClick"}, {"PATTERN", "[modifiers]"},
            {_I("cmdDoubleClick")},
            {"rightClick"}, {"PATTERN", "[modifiers]"},
            {_I("cmdRightClick")},
            {"hover"}, {"PATTERN"},
            {_I("cmdHover")},
            {"dragDrop"}, {"PATTERN", "PATTERN", "[modifiers]"},
            {_I("cmdDragDrop")},
            /* RaiMan not used
             * {"drag"}, {"PATTERN"},
             * {"dropAt"}, {"PATTERN", "[delay]"},
             * RaiMan not used */
            {"----"}, {}, {},
            {"type"}, {"_text", "[modifiers]"},
            {_I("cmdType")},
            {"type"}, {"PATTERN", "_text", "[modifiers]"},
            {_I("cmdType2")},
            {"paste"}, {"_text", "[modifiers]"},
            {_I("cmdPaste")},
            {"paste"}, {"PATTERN", "_text", "[modifiers]"},
            {_I("cmdPaste2")},
            {"----"}, {}, {},
            {"onAppear"}, {"PATTERN", "_hnd"},
            {_I("cmdOnAppear")},
            {"onVanish"}, {"PATTERN", "_hnd"},
            {_I("cmdOnVanish")},
            {"onChange"}, {"_hnd"},
            {_I("cmdOnChange")},
            {"observe"}, {"[time]", "[background]"},
            {_I("cmdObserve")},};
    return CommandsOnToolbar;
  }

  private JComponent createCommandPane() {
    JXTaskPaneContainer con = new JXTaskPaneContainer();

    PreferencesUser pref = PreferencesUser.getInstance();
    JCheckBox chkAutoCapture
            = new JCheckBox(_I("cmdListAutoCapture"),
            pref.getAutoCaptureForCmdButtons());
    chkAutoCapture.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(javax.swing.event.ChangeEvent e) {
        boolean flag = ((JCheckBox) e.getSource()).isSelected();
        PreferencesUser pref = PreferencesUser.getInstance();
        pref.setAutoCaptureForCmdButtons(flag);
      }
    });
    JXTaskPane setPane = new JXTaskPane();
    setPane.setTitle(_I("cmdListSettings"));
    setPane.add(chkAutoCapture);
    setPane.setCollapsed(true);
    con.add(setPane);
    int cat = 0;
    JXTaskPane taskPane = new JXTaskPane();
    taskPane.setTitle(getCommandCategories()[cat++]);
    con.add(taskPane);
    String[][] CommandsOnToolbar = getCommandsOnToolbar();
    boolean collapsed;
    for (int i = 0; i < CommandsOnToolbar.length; i++) {
      String cmd = CommandsOnToolbar[i++][0];
      String[] params = CommandsOnToolbar[i++];
      String[] desc = CommandsOnToolbar[i];
//TODO: more elegeant way, to handle special cases
      if (cmd.equals("----")) {
        if (cat == 2) {
          collapsed = true;
        } else {
          collapsed = false;
        }
        if (cat == 3) {
          if (prefs.getUserType() == PreferencesUser.NEWBEE) {
            break;
          } else {
            collapsed = true;
          }
        }
        taskPane = new JXTaskPane();
        taskPane.setTitle(getCommandCategories()[cat++]);
        con.add(taskPane);
        taskPane.setCollapsed(collapsed);
      } else {
        taskPane.add(new ButtonGenCommand(cmd, desc[0], params));
      }
    }
    Dimension conDim = con.getSize();
    con.setPreferredSize(new Dimension(250, 1000));
    _cmdList = new JXCollapsiblePane(JXCollapsiblePane.Direction.LEFT);
    _cmdList.setMinimumSize(new Dimension(0, 0));
    _cmdList.add(new JScrollPane(con));
    _cmdList.setCollapsed(false);
    return _cmdList;
  }

  //<editor-fold defaultstate="collapsed" desc="RaiMan obsolete">
  private JToolBar initCmdToolbar() {
    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    toolbar.add(createCommandPane());
    return toolbar;
  }
  //</editor-fold>
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Init ToolBar Buttons">
  private JToolBar initToolbar() {
    if (ENABLE_UNIFIED_TOOLBAR) {
      MacUtils.makeWindowLeopardStyle(this.getRootPane());
    }

    JToolBar toolbar = new JToolBar();
    JButton btnInsertImage = new ButtonInsertImage();
    _btnCapture = new ButtonCapture();
    JButton btnSubregion = new ButtonSubregion().init();
    JButton btnLocation = new ButtonLocation().init();
    JButton btnOffset = new ButtonOffset().init();
    JButton btnShow = new ButtonShow().init();
    JButton btnShowIn = new ButtonShowIn().init();
    toolbar.add(_btnCapture);
    toolbar.add(btnInsertImage);
    toolbar.add(btnSubregion);
    toolbar.add(btnLocation);
    toolbar.add(btnOffset);
    toolbar.add(btnShow);
    toolbar.add(btnShowIn);
    toolbar.add(Box.createHorizontalGlue());
    _btnRun = new ButtonRun();
    toolbar.add(_btnRun);
    _btnRunViz = new ButtonRunViz();
    toolbar.add(_btnRunViz);
    toolbar.add(Box.createHorizontalGlue());

//TODO get it working for OSX 10.10
//    if (!Settings.isMac10()) {
    JComponent jcSearchField = createSearchField();
    toolbar.add(jcSearchField);
//    }

    toolbar.add(Box.createRigidArea(new Dimension(7, 0)));
    toolbar.setFloatable(false);
    //toolbar.setMargin(new Insets(0, 0, 0, 5));
    return toolbar;
  }

  class ButtonInsertImage extends ButtonOnToolbar implements ActionListener {

    public ButtonInsertImage() {
      super();
      URL imageURL = SikuliIDE.class.getResource("/icons/insert-image-icon.png");
      setIcon(new ImageIcon(imageURL));
      setText(SikuliIDE._I("btnInsertImageLabel"));
      //setMaximumSize(new Dimension(26,26));
      setToolTipText(SikuliIDE._I("btnInsertImageHint"));
      addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      EditorPane codePane = getCurrentCodePane();
      File file = new SikulixFileChooser(sikulixIDE).loadImage();
      if (file == null) {
        return;
      }
      String path = FileManager.slashify(file.getAbsolutePath(), false);
      Debug.info("load image: " + path);
      EditorPatternButton icon;
      String img = codePane.copyFileToBundle(path).getAbsolutePath();
      if (prefs.getDefaultThumbHeight() > 0) {
        icon = new EditorPatternButton(codePane, img);
        codePane.insertComponent(icon);
      } else {
        codePane.insertString("\"" + (new File(img)).getName() + "\"");
      }
    }
  }

  class ButtonSubregion extends ButtonOnToolbar implements ActionListener, EventObserver {

    String promptText;
    String buttonText;
    String iconFile;
    String buttonHint;

    public ButtonSubregion() {
      super();
      promptText = SikuliIDE._I("msgCapturePrompt");
      buttonText = "Region"; // SikuliIDE._I("btnRegionLabel");
      iconFile = "/icons/region-icon.png";
      buttonHint = SikuliIDE._I("btnRegionHint");
    }

    public ButtonSubregion init() {
      URL imageURL = SikuliIDE.class.getResource(iconFile);
      setIcon(new ImageIcon(imageURL));
      setText(buttonText);
      //setMaximumSize(new Dimension(26,26));
      setToolTipText(buttonHint);
      addActionListener(this);
      return this;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      if (shouldRun()) {
        sikulixIDE.setVisible(false);
        RunTime.pause(0.5f);
        Screen.doPrompt(promptText, this);
      } else {
        nothingTodo();
      }
    }

    public void nothingTodo() {
    }

    public boolean shouldRun() {
      Debug.log(3, "TRACE: ButtonSubRegion triggered");
      return true;
    }

    @Override
    public void update(EventSubject es) {
      OverlayCapturePrompt ocp = (OverlayCapturePrompt) es;
      ScreenImage simg = ocp.getSelection();
      Screen.closePrompt();
      Screen.resetPrompt(ocp);
      captureComplete(simg);
      updateAfter();
    }

    public void updateAfter() {
      SikuliIDE.showAgain();
    }

    public void captureComplete(ScreenImage simg) {
      int x, y, w, h;
      EditorPane codePane = getCurrentCodePane();
      if (simg != null) {
        Rectangle roi = simg.getROI();
        x = (int) roi.getX();
        y = (int) roi.getY();
        w = (int) roi.getWidth();
        h = (int) roi.getHeight();
        sikulixIDE.setVisible(false);
        if (codePane.showThumbs) {
          if (prefs.getPrefMoreImageThumbs()) {
            codePane.insertComponent(new EditorRegionButton(codePane, x, y, w, h));
          } else {
            codePane.insertComponent(new EditorRegionLabel(codePane,
                    new EditorRegionButton(codePane, x, y, w, h).toString()));
          }
        } else {
          codePane.insertString(codePane.getRegionString(x, y, w, h));
        }
      }
    }
  }

  class ButtonLocation extends ButtonSubregion {

    public ButtonLocation() {
      super();
      promptText = "Select a Location";
      buttonText = "Location";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Select location as center of selection";
    }

    @Override
    public void captureComplete(ScreenImage simg) {
      int x, y, w, h;
      if (simg != null) {
        Rectangle roi = simg.getROI();
        x = (int) (roi.getX() + roi.getWidth() / 2);
        y = (int) (roi.getY() + roi.getHeight() / 2);
        getCurrentCodePane().insertString(String.format("Location(%d, %d)", x, y));
      }
    }
  }

  class ButtonOffset extends ButtonSubregion {

    public ButtonOffset() {
      super();
      promptText = "Select an Offset";
      buttonText = "Offset";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Select offset as topLeft to buttomRight of selection";
    }

    @Override
    public void captureComplete(ScreenImage simg) {
      int x, y, ox, oy;
      if (simg != null) {
        Rectangle roi = simg.getROI();
        x = (int) roi.getX();
        y = (int) roi.getY();
        ox = (int) roi.getWidth();
        oy = (int) roi.getHeight();
        getCurrentCodePane().insertString(String.format("Region(%d, %d, %d, %d).asOffset()", x, y, ox, oy));
      }
    }
  }

  class ButtonShow extends ButtonOnToolbar implements ActionListener {

    String buttonText;
    String iconFile;
    String buttonHint;

    public ButtonShow() {
      super();
      buttonText = "Show";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Show the item at the cursor";
    }

    public ButtonShow init() {
      URL imageURL = SikuliIDE.class.getResource(iconFile);
      setIcon(new ImageIcon(imageURL));
      setText(buttonText);
      //setMaximumSize(new Dimension(26,26));
      setToolTipText(buttonHint);
      addActionListener(this);
      return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String line = "";
      EditorPane codePane = getCurrentCodePane();
      line = codePane.getLineTextAtCaret();
      String item = codePane.parseLineText(line);
      if (!item.isEmpty()) {
        String eval = "";
        item = item.replaceAll("\"", "\\\"");
        if (item.startsWith("Region")) {
          if (item.contains(".asOffset()")) {
            item = item.replace(".asOffset()", "");
          }
          eval = "Region.create" + item.substring(6) + ".highlight(2);";
        } else if (item.startsWith("Location")) {
          eval = "new " + item + ".grow(10).highlight(2);";
        } else if (item.startsWith("Pattern")) {
          eval = "m = Screen.all().exists(new " + item
                  + ", 0); if (m != null) m.highlight(2); else print(m);";
        } else if (item.startsWith("\"")) {
          eval = "m = Screen.all().exists(" + item
                  + ", 0); if (m != null) m.highlight(2); else print(m);";
        }
        if (!eval.isEmpty()) {
          Runner.runjsEval(eval);
          return;
        }
      }
      Sikulix.popup("Nothing to show");
    }
  }

  class ButtonShowIn extends ButtonSubregion {

    String item = "";

    public ButtonShowIn() {
      super();
      buttonText = "Show in";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Show the item at the cursor in the selected region";
    }

    @Override
    public boolean shouldRun() {
      Debug.log(3, "TRACE: ButtonShowIn triggered");
      EditorPane codePane = getCurrentCodePane();
      String line = codePane.getLineTextAtCaret();
      item = codePane.parseLineText(line);
      item = item.replaceAll("\"", "\\\"");
      if (item.startsWith("Pattern")) {
        item = "m = null; r = #region#; "
                + "if (r != null) m = r.exists(new " + item + ", 0); "
                + "if (m != null) m.highlight(2); else print(m);";
      } else if (item.startsWith("\"")) {
        item = "m = null; r = #region#; "
                + "if (r != null) m = r.exists(" + item + ", 0); "
                + "if (m != null) m.highlight(2); else print(m);";
      }
      return !item.isEmpty();
    }

    @Override
    public void nothingTodo() {
      Sikulix.popup("Nothing to show");
    }

    @Override
    public void captureComplete(ScreenImage simg) {
      if (simg != null) {
        Region reg = new Region(simg.getROI());
        String itemReg = String.format("new Region(%d, %d, %d, %d)", reg.x, reg.y, reg.w, reg.h);
        item = item.replace("#region#", itemReg);
        Runner.runjsEval(item);
      } else {
        SikuliIDE.showAgain();
        nothingTodo();
      }
    }

    @Override
    public void updateAfter() {
    }
  }

  class ButtonRun extends ButtonOnToolbar implements ActionListener {

    private Thread _runningThread = null;

    public ButtonRun() {
      super();

      URL imageURL = SikuliIDE.class.getResource("/icons/run_big_green.png");
      setIcon(new ImageIcon(imageURL));
      initTooltip();
      addActionListener(this);
      setText(_I("btnRunLabel"));
      //setMaximumSize(new Dimension(45,45));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      runCurrentScript();
    }

    public void runCurrentScript() {
      if (System.out.checkError()) {
        Sikulix.popError("System.out is broken (console output)!"
                + "\nYou will not see any messages anymore!"
                + "\nSave your work and restart the IDE!"
                + "\nYou may ignore this on your own risk!", "Fatal Error");
      }
      SikuliIDE.getStatusbar().setMessage("... PLEASE WAIT ... checking IDE state before running script");
      if (ideIsRunningScript
              || sikulixIDE.getCurrentCodePane().getDocument().getLength() == 0
              || !sikulixIDE.doBeforeRun()) {
        return;
      }
      SikuliIDE.getStatusbar().resetMessage();
      sikulixIDE.setVisible(false);
      RunTime.pause(0.1f);
      sikulixIDE.setIsRunningScript(true);
      final IScriptRunner[] srunners = new IScriptRunner[]{null};
      EditorPane codePane = getCurrentCodePane();
      String cType = codePane.getContentType();
      File scriptFile = null;
      if (codePane.isDirty()) {
        scriptFile = FileManager.createTempFile(Runner.typeEndings.get(cType));
        if (scriptFile != null) {
          try {
            codePane.write(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(scriptFile), "UTF8")));
          } catch (Exception ex) {
            scriptFile = null;
          }
        }
        if (scriptFile == null) {
          log(-1, "runCurrentScript: temp file for running not available");
          return;
        }
      } else {
        scriptFile = codePane.getCurrentFile();
      }
      _console.clear();
      resetErrorMark();
      String parent = null;
      File path = new File(getCurrentBundlePath());
      if (path != null && !codePane.isSourceBundleTemp()) {
        parent = path.getParent();
      }
      IScriptRunner srunner = ScriptingSupport.getRunner(null, cType);
      if (srunner == null) {
        log(-1, "runCurrentScript: Could not load a script runner for: %s", cType);
        return;
      }
      addScriptCode(srunner);
      srunners[0] = srunner;
      ImagePath.reset(path.getAbsolutePath());
      String tabtitle = tabPane.getTitleAt(tabPane.getSelectedIndex());
      if (tabtitle.startsWith("*")) {
        tabtitle = tabtitle.substring(1);
      }
      final SubRun doRun = new SubRun(srunners, scriptFile, path, parent, tabtitle);
      _runningThread = new Thread(doRun);
      _runningThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          if (System.out.checkError()) {
            Sikulix.popError("System.out is broken (console output)!"
                    + "\nYou will not see any messages anymore!"
                    + "\nSave your work and restart the IDE!", "Fatal Error");
          }
          log(lvl, "Scriptrun: cleanup in handler for uncaughtException: %s", e.toString());
          doRun.hasFinished(true);
          doRun.afterRun();
        }
      });
      _runningThread.start();
    }

    private class SubRun implements Runnable {
      private boolean finished = false;
      private int ret = 0;
      private IScriptRunner[] srunners = null;
      private File scriptFile = null;
      private File path = null;
      private String tabtitle = "";
      private String parent;

      public SubRun(IScriptRunner[] srunners, File scriptFile, File path,
                    String parent, String tabtitle) {
        this.srunners = srunners;
        this.scriptFile = scriptFile;
        this.path = path;
        this.tabtitle = tabtitle;
        this.parent = parent;
      }

      @Override
      public void run() {
        try {
          ret = srunners[0].runScript(scriptFile, path, runTime.getArgs(),
                  new String[]{parent, tabtitle});
        } catch (Exception ex) {
          log(-1, "(%s).runScript: Exception: %s", srunners[0], ex);
        }
        hasFinished(true);
        afterRun();
      }

      public int getRet() {
        return ret;
      }

      public boolean hasFinished() {
        return hasFinished(false);
      }

      public synchronized boolean hasFinished(boolean state) {
        if (state) {
          finished = true;
        }
        return finished;
      }

      public void afterRun() {
        addErrorMark(ret);
        srunners[0].close();
        srunners[0] = null;
        if (Image.getIDEshouldReload()) {
          EditorPane pane = sikulixIDE.getCurrentCodePane();
          int line = pane.getLineNumberAtCaret(pane.getCaretPosition());
          sikulixIDE.getCurrentCodePane().reparse();
          sikulixIDE.getCurrentCodePane().jumpTo(line);
        }
        sikulixIDE.setIsRunningScript(false);
        sikulixIDE.setVisible(true);
        Sikulix.cleanUp(0);
        _runningThread = null;
      }
    }

    protected void addScriptCode(IScriptRunner srunner) {
      srunner.execBefore(null);
      srunner.execBefore(new String[]{"Settings.setShowActions(Settings.FALSE)"});
    }

    public boolean isRunning() {
      return _runningThread != null;
    }

    public boolean stopRunScript() {
      if (_runningThread != null) {
        _runningThread.interrupt();
        _runningThread.stop();
        return true;
      }
      return false;
    }

    private void initTooltip() {
      PreferencesUser pref = PreferencesUser.getInstance();
      String strHotkey = Key.convertKeyToText(
              pref.getStopHotkey(), pref.getStopHotkeyModifiers());
      String stopHint = _I("btnRunStopHint", strHotkey);
      setToolTipText(_I("btnRun", stopHint));
    }

    public void addErrorMark(int line) {
      if (line < 0) {
        line *= -1;
      } else {
        return;
      }
      JScrollPane scrPane = (JScrollPane) tabPane.getSelectedComponent();
      EditorLineNumberView lnview = (EditorLineNumberView) (scrPane.getRowHeader().getView());
      lnview.addErrorMark(line);
      EditorPane codePane = SikuliIDE.this.getCurrentCodePane();
      codePane.jumpTo(line);
      codePane.requestFocus();
    }

    public void resetErrorMark() {
      JScrollPane scrPane = (JScrollPane) tabPane.getSelectedComponent();
      EditorLineNumberView lnview = (EditorLineNumberView) (scrPane.getRowHeader().getView());
      lnview.resetErrorMark();
    }
  }

  class ButtonRunViz extends ButtonRun {

    public ButtonRunViz() {
      super();
      URL imageURL = SikuliIDE.class.getResource("/icons/run_big_yl.png");
      setIcon(new ImageIcon(imageURL));
      setToolTipText(_I("menuRunRunAndShowActions"));
      setText(_I("btnRunSlowMotionLabel"));
    }

    @Override
    protected void addScriptCode(IScriptRunner srunner) {
      srunner.execBefore(null);
      srunner.execBefore(new String[]{"Settings.setShowActions(Settings.TRUE)"});
    }
  }

  protected String getCurrentBundlePath() {
    EditorPane pane = getCurrentCodePane();
    return pane.getBundlePath();
  }

  private JComponent createSearchField() {
    _searchField = new JXSearchField("Find");
    _searchField.setUseNativeSearchFieldIfPossible(true);
    //_searchField.setLayoutStyle(JXSearchField.LayoutStyle.MAC);
    _searchField.setMinimumSize(new Dimension(220, 30));
    _searchField.setPreferredSize(new Dimension(220, 30));
    _searchField.setMaximumSize(new Dimension(380, 30));
    _searchField.setMargin(new Insets(0, 3, 0, 3));
    _searchField.setToolTipText("Search is case sensitive - "
            + "start with ! to make search not case sensitive");

    _searchField.setCancelAction(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        getCurrentCodePane().requestFocus();
        _findHelper.setFailed(false);
      }
    });
    _searchField.setFindAction(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        //FIXME: On Linux the found selection disappears somehow
        if (!Settings.isLinux()) //HACK
        {
          _searchField.selectAll();
        }
        boolean ret = _findHelper.findNext(_searchField.getText());
        _findHelper.setFailed(!ret);
      }
    });
    _searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(java.awt.event.KeyEvent ke) {
        boolean ret;
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
          //FIXME: On Linux the found selection disappears somehow
          if (!Settings.isLinux()) //HACK
          {
            _searchField.selectAll();
          }
          ret = _findHelper.findNext(_searchField.getText());
        } else {
          ret = _findHelper.findStr(_searchField.getText());
        }
        _findHelper.setFailed(!ret);
      }
    });
    return _searchField;
  }
  //</editor-fold>

  private void initTabPane() {
    tabPane = new CloseableTabbedPane();
    tabPane.setUI(new AquaCloseableTabbedPaneUI());
    tabPane.addCloseableTabbedPaneListener(
            new CloseableTabbedPaneListener() {
              @Override
              public boolean closeTab(int i) {
                EditorPane codePane;
                try {
                  codePane = getPaneAtIndex(i);
                  tabPane.setLastClosed(codePane.getSrcBundle());
                  Debug.log(4, "close tab " + i + " n:" + tabPane.getComponentCount());
                  boolean ret = codePane.close();
                  Debug.log(4, "after close tab n:" + tabPane.getComponentCount());
                  if (ret && tabPane.getTabCount() < 2) {
                    (new FileAction()).doNew(null);
                  }
                  return ret;
                } catch (Exception e) {
                  log(-1, "Problem closing tab %d\nError: %s", i, e.getMessage());
                  return false;
                }
              }
            });

    tabPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(javax.swing.event.ChangeEvent e) {
        EditorPane codePane = null;
        JTabbedPane tab = (JTabbedPane) e.getSource();
        int i = tab.getSelectedIndex();
        if (i >= 0) {
          codePane = getPaneAtIndex(i);
          String fname = codePane.getCurrentSrcDir();
          if (fname == null) {
            SikuliIDE.this.setTitle(tab.getTitleAt(i));
          } else {
            ImagePath.setBundlePath(fname);
            SikuliIDE.this.setTitle(fname);
          }
          SikuliIDE.this.chkShowThumbs.setState(SikuliIDE.this.getCurrentCodePane().showThumbs);
        }
        updateUndoRedoStates();
        if (codePane != null) {
          SikuliIDE.getStatusbar().setCurrentContentType(
                  SikuliIDE.this.getCurrentCodePane().getSikuliContentType());
        }
      }
    });

  }

  private void initMsgPane(boolean atBottom) {
    msgPane = new JTabbedPane();
    _console = new EditorConsolePane();
    msgPane.addTab(_I("paneMessage"), null, _console, "DoubleClick to hide/unhide");
    if (Settings.isWindows() || Settings.isLinux()) {
      msgPane.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }
    msgPane.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() < 2) {
          return;
        }
        if (msgPaneCollapsed) {
          _mainSplitPane.setDividerLocation(_mainSplitPane.getLastDividerLocation());
          msgPaneCollapsed = false;
        } else {
          int pos = _mainSplitPane.getWidth() - 35;
          if (prefs.getPrefMoreMessage() == PreferencesUser.HORIZONTAL) {
            pos = _mainSplitPane.getHeight() - 35;
          }
          _mainSplitPane.setDividerLocation(pos);
          msgPaneCollapsed = true;
        }
      }
      //<editor-fold defaultstate="collapsed" desc="mouse events not used">

      @Override
      public void mousePressed(MouseEvent me) {
      }

      @Override
      public void mouseReleased(MouseEvent me) {
      }

      @Override
      public void mouseEntered(MouseEvent me) {
      }

      @Override
      public void mouseExited(MouseEvent me) {
      }
      //</editor-fold>
    });
  }

  public Container getMsgPane() {
    return msgPane;
  }

  private SikuliIDEStatusBar initStatusbar() {
    _status = new SikuliIDEStatusBar();
    return _status;
  }

  public static SikuliIDEStatusBar getStatusbar() {
    if (sikulixIDE == null) {
      return null;
    } else {
      return sikulixIDE._status;
    }
  }

  private void initWindowListener() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        SikuliIDE.this.quit();
      }
    });
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        PreferencesUser.getInstance().setIdeSize(SikuliIDE.this.getSize());
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        PreferencesUser.getInstance().setIdeLocation(SikuliIDE.this.getLocation());
      }
    });
  }

  private void initTooltip() {
    ToolTipManager tm = ToolTipManager.sharedInstance();
    tm.setDismissDelay(30000);
  }

  //<editor-fold defaultstate="collapsed" desc="Init ShortCuts HotKeys">
  private void nextTab() {
    int i = tabPane.getSelectedIndex();
    int next = (i + 1) % tabPane.getTabCount();
    tabPane.setSelectedIndex(next);
  }

  private void prevTab() {
    int i = tabPane.getSelectedIndex();
    int prev = (i - 1 + tabPane.getTabCount()) % tabPane.getTabCount();
    tabPane.setSelectedIndex(prev);
  }

  private void initShortcutKeys() {
    final int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      private boolean isKeyNextTab(java.awt.event.KeyEvent ke) {
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_TAB
                && ke.getModifiers() == InputEvent.CTRL_MASK) {
          return true;
        }
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_CLOSE_BRACKET
                && ke.getModifiers() == (InputEvent.META_MASK | InputEvent.SHIFT_MASK)) {
          return true;
        }
        return false;
      }

      private boolean isKeyPrevTab(java.awt.event.KeyEvent ke) {
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_TAB
                && ke.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)) {
          return true;
        }
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_OPEN_BRACKET
                && ke.getModifiers() == (InputEvent.META_MASK | InputEvent.SHIFT_MASK)) {
          return true;
        }
        return false;
      }

      public void eventDispatched(AWTEvent e) {
        java.awt.event.KeyEvent ke = (java.awt.event.KeyEvent) e;
        //Debug.log(ke.toString());
        if (ke.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
          if (isKeyNextTab(ke)) {
            nextTab();
          } else if (isKeyPrevTab(ke)) {
            prevTab();
          }
        }
      }
    }, AWTEvent.KEY_EVENT_MASK);

  }

  public void removeCaptureHotkey() {
    HotkeyManager.getInstance().removeHotkey("Capture");
  }

  public void installCaptureHotkey() {
    HotkeyManager.getInstance().addHotkey("Capture", new HotkeyListener() {
      @Override
      public void hotkeyPressed(HotkeyEvent e) {
        if (!isRunningScript()) {
          onQuickCapture();
        }
      }
    });
  }

  public void onQuickCapture() {
    onQuickCapture(null);
  }

  public void onQuickCapture(String arg) {
    if (isInited()) {
      Debug.log(3, "QuickCapture");
      _btnCapture.capture(0);
    }
  }

  public void removeStopHotkey() {
    HotkeyManager.getInstance().removeHotkey("Abort");
  }

  public void installStopHotkey() {
    HotkeyManager.getInstance().addHotkey("Abort", new HotkeyListener() {
      @Override
      public void hotkeyPressed(HotkeyEvent e) {
        onStopRunning();
      }
    });
  }

  public void onStopRunning() {
    Debug.log(3, "AbortKey was pressed");
    boolean shouldCleanUp = true;
    if (_btnRun != null && _btnRun.isRunning()) {
      shouldCleanUp &= _btnRun.stopRunScript();
    }
    if (_btnRunViz != null && _btnRunViz.isRunning()) {
      shouldCleanUp &= _btnRunViz.stopRunScript();
    }
    if (_btnRun == null && _btnRunViz == null) {
      ideSplash.showAction("... accepted - please wait ...");
      setPause(true);
      return;
    }
    if (shouldCleanUp) {
      org.sikuli.script.Sikulix.cleanUp(-1);
      this.setVisible(true);
    } else {
      Debug.log(3, "AbortKey was pressed, but nothing to stop here ;-)");
    }
  }

  private void initHotkeys() {
    installCaptureHotkey();
    installStopHotkey();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="IDE Unit Testing --- RaiMan not used">
  /*
   private void initSidePane() {
   initUnitPane();
   _sidePane = new JXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT);
   _sidePane.setMinimumSize(new Dimension(0, 0));
   CloseableTabbedPane tabPane = new CloseableTabbedPane();
   _sidePane.getContentPane().add(tabPane);
   tabPane.setMinimumSize(new Dimension(0, 0));
   tabPane.addTab(_I("tabUnitTest"), _unitPane);
   tabPane.addCloseableTabbedPaneListener(new CloseableTabbedPaneListener() {
   @Override
   public boolean closeTab(int tabIndexToClose) {
   _sidePane.setCollapsed(true);
   _chkShowUnitTest.setState(false);
   return false;
   }
   });
   _sidePane.setCollapsed(true);
   }

   private void initUnitPane() {
   _testRunner = new UnitTestRunner();
   _unitPane = _testRunner.getPanel();
   _chkShowUnitTest.setState(false);
   addAuxTab(_I("paneTestTrace"), _testRunner.getTracePane());
   }
   */
  public void addAuxTab(String tabName, JComponent com) {
    msgPane.addTab(tabName, com);
  }

  public void jumpTo(String funcName) throws BadLocationException {
    EditorPane pane = getCurrentCodePane();
    pane.jumpTo(funcName);
    pane.grabFocus();
  }

  public void jumpTo(int lineNo) throws BadLocationException {
    EditorPane pane = getCurrentCodePane();
    pane.jumpTo(lineNo);
    pane.grabFocus();
  }
  //</editor-fold>
}
