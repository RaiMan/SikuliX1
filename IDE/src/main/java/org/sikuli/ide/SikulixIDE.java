/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.android.ADBClient;
import org.sikuli.android.ADBScreen;
import org.sikuli.android.ADBTest;
import org.sikuli.basics.*;
import org.sikuli.idesupport.IDESplash;
import org.sikuli.idesupport.IDESupport;
import org.sikuli.idesupport.IIDESupport;
import org.sikuli.script.Image;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runners.JavaScriptRunner;
import org.sikuli.script.*;
import org.sikuli.script.support.*;
import org.sikuli.util.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
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
import java.util.List;
import java.util.*;

public class SikulixIDE extends JFrame implements InvocationHandler {

  //<editor-fold desc="00 startup / quit">
  private static String me = "IDE: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  static RunTime runTime;

  public static void main(String[] args) {

    RunTime.afterStart(RunTime.Type.IDE, args);

    if ("m".equals(System.getProperty("os.name").substring(0, 1).toLowerCase())) {
      prepareMacUI();
    }
    runTime = RunTime.get(RunTime.Type.IDE);

    get();

    if (Debug.getDebugLevel() < 3) {
      ideSplash = new IDESplash();
    }

    log(3, "running with Locale: %s", SikuliIDEI18N.getLocaleShow());

    sikulixIDE.prepareMacMenus();

    if (Debug.getDebugLevel() > 2) {
      RunTime.printArgs();
    }

    sikulixIDE.initHotkeys();
    Debug.log(3, "IDE: Init ScriptingSupport");

    IDESupport.init();
    IDESupport.initIDESupport();
    sikulixIDE.initSikuliIDE();
  }

  public boolean quit() {
    terminate();
    if (getCurrentCodePane() == null) {
      return true;
    } else {
      return false;
    }
  }

  private boolean closeIDE() {
    if (!doBeforeQuit()) {
      return false;
    }
    while (true) {
      EditorPane codePane = sikulixIDE.getCurrentCodePane();
      if (codePane == null) {
        break;
      }
      if (!removeCurrentTab()) {
        return false;
      }
    }
    return true;
  }

  private boolean doBeforeQuit() {
    if (checkDirtyPanes()) {
      int action = askForSaveAll("Quit");
      if (action < 0) {
        return false;
      }
      return saveSession(action, true);
    }
    return saveSession(DO_NOT_SAVE, true);
  }

  private int askForSaveAll(String typ) {
//TODO I18N
    String warn = "Some scripts are not saved yet!";
    String title = SikuliIDEI18N._I("dlgAskCloseTab");
    String[] options;
    int ret = -1;
    SikulixIDE parent = this;
    options = new String[3];
    options[WARNING_DO_NOTHING] = typ + " immediately";
    options[WARNING_ACCEPTED] = "Save all and " + typ;
    options[WARNING_CANCEL] = SikuliIDEI18N._I("cancel");
    ret = JOptionPane.showOptionDialog(parent, warn, title, 0, JOptionPane.WARNING_MESSAGE,
            null, options, options[options.length - 1]);
    if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
      return -1;
    }
    return ret;
  }
  //</editor-fold>

  //<editor-fold desc="01 IDE instance">
  private SikulixIDE() {
    super("SikulixIDE");
  }

  static synchronized SikulixIDE get() {
    if (sikulixIDE == null) {
      sikulixIDE = new SikulixIDE();
    }
    return sikulixIDE;
  }

  private static SikulixIDE sikulixIDE = null;

  static IIDESupport getIDESupport(String identifier) {
    return IDESupport.ideSupporter.get(identifier);
  }

  @Override
  public void setTitle(String title) {
    super.setTitle(runTime.SXVersionIDE + " - " + title);
  }

  static ImageIcon getIconResource(String name) {
    URL url = SikulixIDE.class.getResource(name);
    if (url == null) {
      Debug.error("Warning: could not load \"" + name + "\" icon");
      return null;
    }
    return new ImageIcon(url);
  }

  public static void showIDE() {
    showAgain();
  }

  public static void hideIDE() {
    get().setVisible(false);
    RunTime.pause(0.5f);
  }

  static void showAgain() {
    EditorPane codePane = get().getCurrentCodePane();
    if (codePane == null) {
      get().newTabEmpty();
      codePane = get().getCurrentCodePane();
    }
    get().setVisible(true);
    codePane.requestFocusInWindow();
  }

  static String _I(String key, Object... args) {
    try {
      return SikuliIDEI18N._I(key, args);
    } catch (Exception e) {
      log(3, "[I18N] " + key);
      return key;
    }
  }
  //</editor-fold>

  //<editor-fold desc="02 init IDE">
  private void initSikuliIDE() {
    Debug.log(3, "IDE: Reading Preferences");
    prefs = PreferencesUser.get();
    //prefs.exportPrefs(new File(runTime.fUserDir, "SikulixIDEprefs.txt").getAbsolutePath());
    if (prefs.getUserType() < 0) {
      prefs.setIdeSession("");
      prefs.setDefaults();
    }

    Dimension windowSize = prefs.getIdeSize();
    Point windowLocation = prefs.getIdeLocation();
    Rectangle monitor = runTime.hasPoint(windowLocation);
    if (monitor == null) {
      log(-1, "Remembered window not valid. Going to primary screen");
      monitor = runTime.getMonitor(-1);
      windowSize.width = 0;
    }
    if (windowSize.width == 0) {
      windowSize = new Dimension(1024, 700);
      windowLocation = new Point(100, 50);
    }
    Rectangle win = monitor.intersection(new Rectangle(windowLocation, windowSize));
    setSize(win.getSize());
    setLocation(windowLocation);

    Debug.log(3, "IDE: Adding components to window");
    initMenuBars(this);
    final Container ideContainer = getContentPane();
    ideContainer.setLayout(new BorderLayout());
    Debug.log(3, "IDE: creating tabbed editor");
    initTabs();
    Debug.log(3, "IDE: creating message area");
    if (runTime.isTesting()) {
      System.setProperty("sikuli.console", "false");
    }
    initMessageArea();
    Debug.log(3, "IDE: creating combined work window");
    JPanel codePane = new JPanel(new BorderLayout(10, 10));
    codePane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    codePane.add(tabs, BorderLayout.CENTER);
    if (prefs.getPrefMoreMessage() == PreferencesUser.VERTICAL) {
      mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codePane, messageArea);
    } else {
      mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codePane, messageArea);
    }
    mainPane.setResizeWeight(0.6);
    mainPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    Debug.log(3, "IDE: Putting all together");
    JPanel editPane = new JPanel(new BorderLayout(0, 0));

    editPane.add(mainPane, BorderLayout.CENTER);
    ideContainer.add(editPane, BorderLayout.CENTER);
    Debug.log(3, "IDE: Putting all together - after main pane");

    JToolBar tb = initToolbar();
    ideContainer.add(tb, BorderLayout.NORTH);
    Debug.log(3, "IDE: Putting all together - after toolbar");

    ideContainer.add(initStatusbar(), BorderLayout.SOUTH);
    Debug.log(3, "IDE: Putting all together - before layout");
    ideContainer.doLayout();
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Debug.log(3, "IDE: Putting all together - after layout");
    initShortcutKeys();
    initWindowListener();
    initTooltip();


    //Debug.log(3, "IDE: Putting all together - Check for Updates");
    //TODO autoCheckUpdate();

    //waitPause();
    Debug.log(3, "IDE: Putting all together - Restore last Session");
    restoreSession(0);
    if (tabs.getTabCount() == 0) {
      newTabEmpty();
    }
    tabs.setSelectedIndex(0);

    String j9Message = "";
    if (runTime.isJava9()) {
      j9Message = "*** Running on Java 9+";
    }
    Debug.log(lvl, "IDE startup: %4.1f seconds %s", (new Date().getTime() - RunTime.getElapsedStart()) / 1000.0, j9Message);
    Debug.unsetWithTimeElapsed();
    if (Debug.getDebugLevel() < 3) {
      Debug.reset();
    }

    stopSplash();
    setVisible(true);
    mainPane.setDividerLocation(0.6);
    _inited = true;
    try {
      EditorPane editorPane = getCurrentCodePane();
      if (editorPane.isText) {
        collapseMessageArea();
      }
      editorPane.requestFocusInWindow();
    } catch (Exception e) {
    }
  }

  private JSplitPane mainPane;
  static IDESplash ideSplash = null;
  private boolean _inited = false;

  static void stopSplash() {
    if (ideSplash != null) {
      ideSplash.setVisible(false);
      ideSplash.dispose();
      ideSplash = null;
    }
  }

  private void initTabs() {
    tabs = new CloseableTabbedPane();
    tabs.setUI(new AquaCloseableTabbedPaneUI());
    tabs.addCloseableTabbedPaneListener(new CloseableTabbedPaneListener() {
      @Override
      public boolean closeTab(int tabIndexToClose) {
        EditorPane editorPane;
        try {
          editorPane = getPaneAtIndex(tabIndexToClose);
          tabs.setLastClosed(editorPane.getSourceReference());
          boolean ret = editorPane.close();
          return ret;
        } catch (Exception e) {
          log(-1, "Problem closing tab %d\nError: %s", tabIndexToClose, e.getMessage());
          return false;
        }
      }
    });
    tabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(javax.swing.event.ChangeEvent e) {
        log(4, "********** Tab switched");
        EditorPane editorPane;
        JTabbedPane tab = (JTabbedPane) e.getSource();
        int i = tab.getSelectedIndex();
        if (i >= 0) {
          editorPane = getPaneAtIndex(i);
          if (!editorPane.hasEditingFile()) {
            return;
          }
          if (editorPane.isTemp()) {
            setTitle(tab.getTitleAt(i));
          } else {
            if (editorPane.isBundle()) {
              setTitle(editorPane.getFolderPath());
            } else {
              setTitle(editorPane.getFilePath());
            }
          }
          editorPane.setBundleFolder();
          if (editorPane.isDirty()) {
            editorPane.checkSource(); // tab switch
          }
          int dot = editorPane.getCaret().getDot();
          editorPane.setCaretPosition(dot);
          if (editorPane.isText) {
            collapseMessageArea();
          } else {
            uncollapseMessageArea();
          }
          chkShowThumbs.setState(getCurrentCodePane().showThumbs);
          getStatusbar().setType(getCurrentCodePane().getType());
        }
        updateUndoRedoStates();
      }
    });
  }

  CloseableTabbedPane getTabs() {
    return tabs;
  }

  private CloseableTabbedPane tabs;
  //</editor-fold>

  //<editor-fold desc="03 init for Mac">
  private static void prepareMacUI() {
    try {
      // set the brushed metal look and feel, if desired
      System.setProperty("apple.awt.brushMetalLook", "true");

      // use the mac system menu bar
      System.setProperty("apple.laf.useScreenMenuBar", "true");

      // set the "About" menu item name
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiStar");

      // use smoother fonts
      System.setProperty("apple.awt.textantialiasing", "true");

      // ref: http://developer.apple.com/releasenotes/Java/Java142RNTiger/1_NewFeatures/chapter_2_section_3.html
      System.setProperty("apple.awt.graphics.EnableQ2DX", "true");

      // use the system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // put your debug code here ...
    }
  }

  private void prepareMacMenus() {
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
        Method macSupport = clazzMacSupport.getDeclaredMethod("support", SikulixIDE.class);
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
  private boolean showAbout = true;
  private boolean showPrefs = true;
  private boolean showQuit = true;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mName = method.getName();
    if ("handleAbout".equals(mName)) {
      sikulixIDE.showAbout();
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
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="04 save / restore session">
  final static int WARNING_CANCEL = 2;
  final static int WARNING_ACCEPTED = 1;
  final static int WARNING_DO_NOTHING = 0;
  final static int DO_SAVE_ALL = 3;
  final static int DO_NOT_SAVE = 0;
  private PreferencesUser prefs;
  private static String[] loadScripts = null;

  private boolean saveSession(int action, boolean quitting) {
    int nTab = tabs.getTabCount();
    int selectedTab = tabs.getSelectedIndex();
    StringBuilder sbuf = new StringBuilder();
    EditorPane editorPane = null;
    boolean success = true;
    for (int tabIndex = 0; tabIndex < nTab; tabIndex++) {
      try {
        editorPane = getPaneAtIndex(tabIndex);
        String fileName = editorPane.editorPaneFileSelected;
        if (action == DO_NOT_SAVE) {
          if (quitting) {
            editorPane.setDirty(false);
          }
          if (fileName == null) {
            continue;
          }
        } else if (editorPane.isDirty()) {
          if (!saveQietly(tabIndex)) {
            if (quitting) {
              editorPane.setDirty(false);
            }
            continue;
          }
        }
        if (action == DO_SAVE_ALL) {
          continue;
        }
        if (editorPane.isText) {
          fileName += "###isText";
        }
        if (sbuf.length() > 0) {
          sbuf.append(";");
        }
        sbuf.append(fileName);
      } catch (Exception e) {
        log(-1, "Save all: %s: %s", editorPane.getCurrentFile(), e.getMessage());
        success = false;
        break;
      }

    }
    if (success) {
      PreferencesUser.get().setIdeSession(sbuf.toString());
    }
    tabs.setSelectedIndex(selectedTab);
    return success;
  }

  boolean saveQietly(int tabIndex) {
    int currentTab = tabs.getSelectedIndex();
    tabs.setSelectedIndex(tabIndex);
    boolean retval = true;
    EditorPane codePane = getPaneAtIndex(tabIndex);
    String fname = null;
    try {
      fname = codePane.saveTabContent();
      if (fname != null) {
        if (!codePane.isBundle()) {
          fname = codePane.getFilePath();
          codePane.showType();
        }
        setFileTabTitle(fname, tabIndex);
      } else {
        retval = false;
      }
    } catch (Exception ex) {
      log(-1, "Problem when trying to save %s\nError: %s",
              fname, ex.getMessage());
      retval = false;
    }
    tabs.setSelectedIndex(currentTab);
    return retval;
  }

  private int restoreSession(int tabIndex) {
    String session_str = prefs.getIdeSession();
    int filesLoaded = 0;
    List<File> filesToLoad = new ArrayList<File>();
    if (macOpenFiles != null && macOpenFiles.size() > 0) {
      for (File f : macOpenFiles) {
        filesToLoad.add(f);
        if (restoreScriptFromSession(f)) filesLoaded++;
      }
    }
    if (session_str != null && !session_str.isEmpty()) {
      String[] filenames = session_str.split(";");
      if (filenames.length > 0) {
        log(3, "Restore scripts from last session");
        for (int i = 0; i < filenames.length; i++) {
          if (filenames[i].isEmpty()) {
            continue;
          }
          File fileToLoad = new File(filenames[i]);
          File fileToLoadClean = new File(filenames[i].replace("###isText", ""));
          String shortName = fileToLoad.getName();
          if (fileToLoadClean.exists() && !filesToLoad.contains(fileToLoad)) {
            if (shortName.endsWith(".py")) {
              log(3, "Restore Python script: %s", fileToLoad.getName());
            } else if (shortName.endsWith("###isText")) {
              log(3, "Restore Text file: %s", fileToLoad.getName());
            } else {
              log(3, "Restore Sikuli script: %s", fileToLoad);
            }
            filesToLoad.add(fileToLoad);
            if (restoreScriptFromSession(fileToLoad)) {
              filesLoaded++;
            }
          }
        }
      }
    }
    if (loadScripts != null && loadScripts.length > 0) {
      log(3, "Preload given scripts");
      for (int i = 0; i < loadScripts.length; i++) {
        if (loadScripts[i].isEmpty()) {
          continue;
        }
        File f = new File(loadScripts[i]);
        if (f.exists() && !filesToLoad.contains(f)) {
          if (f.getName().endsWith(".py")) {
            Debug.info("Python script: %s", f.getName());
          } else {
            log(3, "Sikuli script: %s", f);
          }
          if (restoreScriptFromSession(f)) filesLoaded++;
        }
      }
    }
    return filesLoaded;
  }

  private boolean restoreScriptFromSession(File file) {
    EditorPane editorPane = makeTab(-1);
    if (file.getPath().endsWith("###isText")) {
      file = new File(file.getPath().replace("###isText", ""));
      editorPane.isText = true;
    }
    editorPane.loadFile(file);
    if (editorPane.hasEditingFile()) {
      setCurrentFileTabTitle(file.getAbsolutePath());
      editorPane.setCaretPosition(0);
      return true;
    }
    log(-1, "restoreScriptFromSession: Can't load: %s", file);
    return false;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="06 tabs handling">
  void terminate() {
    log(lvl, "Quit requested");
    if (closeIDE()) {
      runTime.terminate(0, "");
    }
    log(-1, "Quit: cancelled or did not work");
  }

  EditorPane makeTab() {
    EditorPane editorPane = new EditorPane();
    lineNumberColumn = new EditorLineNumberView(editorPane);
    editorPane.getScrollPane().setRowHeaderView(lineNumberColumn);
    return editorPane;
  }

  EditorPane makeTab(int tabIndex) {
    log(lvl + 1, "makeTab: %d", tabIndex);
    EditorPane editorPane = makeTab();
    JScrollPane scrPane = editorPane.getScrollPane();
    if (tabIndex < 0 || tabIndex >= tabs.getTabCount()) {
      tabs.addTab(_I("tabUntitled"), scrPane);
    } else {
      tabs.addTab(_I("tabUntitled"), scrPane, tabIndex);
    }
    tabs.setSelectedIndex(tabIndex < 0 ? tabs.getTabCount() - 1 : tabIndex);
    editorPane.requestFocus();
    return editorPane;
  }

  void newTabEmpty() {
    EditorPane editorPane = makeTab();
    editorPane.setTemp(true);
    tabs.addTab(_I("tabUntitled"), editorPane.getScrollPane(), 0);
    tabs.setSelectedIndex(0);
    editorPane.init(null);
    editorPane.setTempFile();
  }

  String newTabWithContent(String fname) {
    return newTabWithContent(fname, -1);
  }

  String newTabWithContent(String fname, int tabIndex) {
    boolean accessingAsFile = false;
    if (Settings.isMac()) {
      accessingAsFile = !ACCESSING_AS_FOLDER;
      ACCESSING_AS_FOLDER = false;
    }
    int selectedTab = tabs.getSelectedIndex();
    EditorPane editorPane = makeTab(tabIndex);
    File tabFile;
    if (null == fname) {
      tabFile = editorPane.selectFile(accessingAsFile);
    } else {
      tabFile = new File(fname);
    }
    if (tabFile != null) {
      editorPane.loadFile(tabFile);
      if (editorPane.hasEditingFile()) {
        setCurrentFileTabTitle(tabFile.getAbsolutePath());
        recentAdd(tabFile.getAbsolutePath());
        if (editorPane.isText) {
          collapseMessageArea();
        }
      }
    } else {
      log(3, "selectFile: cancelled");
      removeCurrentTab();
      int alreadyOpen = tabs.getAlreadyOpen();
      if (alreadyOpen < 0) {
        tabs.setSelectedIndex(selectedTab);
      } else {
        tabs.setSelectedIndex(alreadyOpen);
      }
    }
    if (null == tabFile) {
      return null;
    } else {
      return tabFile.getAbsolutePath();
    }
  }

  boolean checkDirtyPanes() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
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

  EditorPane getCurrentCodePane() {
    if (tabs.getTabCount() == 0) {
      return null;
    }
    JScrollPane scrPane = (JScrollPane) tabs.getSelectedComponent();
    EditorPane pane = (EditorPane) scrPane.getViewport().getView();
    return pane;
  }

  EditorPane getPaneAtIndex(int index) {
    JScrollPane scrPane = (JScrollPane) tabs.getComponentAt(index);
    EditorPane codePane = (EditorPane) scrPane.getViewport().getView();
    return codePane;
  }

  void setCurrentFileTabTitle(String fname) {
    int tabIndex = tabs.getSelectedIndex();
    setFileTabTitle(fname, tabIndex);
  }

  String getCurrentFileTabTitle() {
    String fname = tabs.getTitleAt(tabs.getSelectedIndex());
    if (fname.startsWith("*")) {
      return fname.substring(1);
    } else {
      return fname;
    }
  }

  void setCurrentFileTabTitleDirty(boolean isDirty) {
    int i = tabs.getSelectedIndex();
    String title = tabs.getTitleAt(i);
    if (!isDirty && title.startsWith("*")) {
      title = title.substring(1);
      tabs.setTitleAt(i, title);
    } else if (isDirty && !title.startsWith("*")) {
      title = "*" + title;
      tabs.setTitleAt(i, title);
    }
  }

  void setFileTabTitle(String fName, int tabIndex) {
    String ideTitle;
    EditorPane codePane = getCurrentCodePane();
    if (codePane.isBundle()) {
      String shortName = new File(fName).getName();
      ideTitle = new File(fName).getAbsolutePath();
      int i = shortName.lastIndexOf(".");
      if (i > 0) {
        tabs.setTitleAt(tabIndex, shortName.substring(0, i));
      } else {
        tabs.setTitleAt(tabIndex, shortName);
      }
    } else {
      tabs.setTitleAt(tabIndex, codePane.getCurrentFile().getName());
      ideTitle = codePane.getFilePath();
    }
    this.setTitle(ideTitle);
  }

  ArrayList<String> getOpenedFilenames() {
    int nTab = tabs.getTabCount();
    File file = null;
    String filePath;
    ArrayList<String> filenames = new ArrayList<String>(0);
    if (nTab > 0) {
      for (int i = 0; i < nTab; i++) {
        EditorPane codePane = getPaneAtIndex(i);
        file = codePane.getCurrentFile(false);
        if (file != null) {
          filePath = FileManager.slashify(file.getAbsolutePath(), false);
          if (!codePane.isPython && !codePane.isText) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
          }
          filenames.add(filePath);
        } else {
          filenames.add("");
        }
      }
    }
    return filenames;
  }

  int isAlreadyOpen(String filename) {
    int aot = getOpenedFilenames().indexOf(filename);
    if (aot > -1 && aot < (tabs.getTabCount() - 1)) {
      alreadyOpenedTab = aot;
      return aot;
    }
    return -1;
  }

  private int alreadyOpenedTab = -1;

  boolean removeCurrentTab() {
    EditorPane pane = getCurrentCodePane();
    tabs.remove(tabs.getSelectedIndex());
    if (pane == getCurrentCodePane()) {
      return false;
    }
    return true;
  }

  void closeCurrentTab() {
    EditorPane codePane = getCurrentCodePane();
    String orgName = codePane.getCurrentShortFilename();
    log(lvl, "doCloseTab requested: %s", orgName);
    try {
      codePane.close();
      tabs.remove(tabs.getSelectedIndex());
    } catch (Exception ex) {
      Debug.info("Can't close this tab: %s", ex.getMessage());
    }
    codePane = getCurrentCodePane();
    if (codePane != null) {
      codePane.requestFocus();
    } else {
      newTabEmpty();
    }
  }
  //</editor-fold>

  //<editor-fold desc="07 menu helpers">
  void recentAdd(String fPath) {
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

  public void showAbout() {
    //TODO full featured About
    String info = "You are running " + runTime.SXVersionIDE
            + "\n\nNeed help? -> start with Help Menu\n\n"
            + "*** Have fun ;-)\n\n"
            + "Tsung-Hsiang Chang aka vgod\n"
            + "Tom Yeh\n"
            + "Raimund Hocke aka RaiMan\n\n"
            + "\n\n" + String.format("Build#: %s (%s)", runTime.SXBuildNumber, runTime.SXBuild);
    JOptionPane.showMessageDialog(this, info,
            "Sikuli About", JOptionPane.PLAIN_MESSAGE);
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

  void openSpecial() {
    log(lvl, "Open Special requested");
    Map<String, String> specialFiles = new Hashtable<>();
    specialFiles.put("1 SikuliX Global Options", runTime.options().getOptionsFile());
    File extensionsFile = ExtensionManager.getExtensionsFile();
    specialFiles.put("2 SikuliX Extensions Options", extensionsFile.getAbsolutePath());
    File sitesTxt = ExtensionManager.getSitesTxt();
    specialFiles.put("3 SikuliX Additional Sites", sitesTxt.getAbsolutePath());
    String[] defaults = new String[specialFiles.size()];
    defaults[0] = Options.getOptionsFileDefault();
    defaults[1] = ExtensionManager.getExtensionsFileDefault();
    defaults[2] = ExtensionManager.getSitesTxtDefault();
    String msg = "";
    int num = 1;
    String[] files = new String[specialFiles.size()];
    for (String specialFile : specialFiles.keySet()) {
      files[num - 1] = specialFiles.get(specialFile).trim();
      msg += specialFile + "\n";
      num++;
    }
    msg += "\n" + "Enter a number to select a file";
    String answer = SX.input(msg, "Edit a special SikuliX file", false, 10);
    if (null != answer && !answer.isEmpty()) {
      try {
        num = Integer.parseInt(answer.substring(0, 1));
        if (num > 0 && num <= specialFiles.size()) {
          String file = files[num - 1];
          if (!new File(file).exists()) {
            FileManager.writeStringToFile(defaults[num - 1], file);
          }
          String selectedFile = file + "###isText";
          log(lvl, "Open Special: should load: %s", file);
          newTabWithContent(selectedFile);
        }
      } catch (NumberFormatException e) {
      }
    }
  }
  //</editor-fold>

  //<editor-fold desc="10 Init Menus">
  private JMenuBar _menuBar = new JMenuBar();
  private JMenu _fileMenu = null; //new JMenu(_I("menuFile"));
  private JMenu _editMenu = null; //new JMenu(_I("menuEdit"));
  private UndoAction _undoAction = null; //new UndoAction();
  private RedoAction _redoAction = null; //new RedoAction();
  private JMenu _runMenu = null; //new JMenu(_I("menuRun"));
  private JMenu _viewMenu = null; //new JMenu(_I("menuView"));
  private JMenu _toolMenu = null; //new JMenu(_I("menuTool"));
  private JMenu _helpMenu = null; //new JMenu(_I("menuHelp"));

  void initMenuBars(JFrame frame) {
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

  JMenuItem createMenuItem(JMenuItem item, KeyStroke shortcut, ActionListener listener) {
    if (shortcut != null) {
      item.setAccelerator(shortcut);
    }
    item.addActionListener(listener);
    return item;
  }

  JMenuItem createMenuItem(String name, KeyStroke shortcut, ActionListener listener) {
    JMenuItem item = new JMenuItem(name);
    return createMenuItem(item, shortcut, listener);
  }

  class MenuAction implements ActionListener {

    Method actMethod = null;
    String action;

    MenuAction() {
    }

    MenuAction(String item) throws NoSuchMethodException {
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
          log(3, "MenuAction." + action);
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
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="11 Init FileMenu">
  JMenu getFileMenu() {
    return _fileMenu;
  }

  private boolean ACCESSING_AS_FOLDER = false;
  private EditorLineNumberView lineNumberColumn;

  //<editor-fold desc="menu">
  private void initFileMenu() throws NoSuchMethodException {
    _fileMenu = new JMenu(_I("menuFile"));
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
                    InputEvent.SHIFT_DOWN_MASK | scMask),
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
                    InputEvent.CTRL_DOWN_MASK | scMask),
            new FileAction(FileAction.SAVE_ALL)));

    _fileMenu.add(createMenuItem(_I("menuFileExport"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E,
                    InputEvent.SHIFT_DOWN_MASK | scMask),
            new FileAction(FileAction.EXPORT)));

    _fileMenu.add(createMenuItem("Export as jar",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, scMask),
            new FileAction(FileAction.ASJAR)));

//TODO export as runnable jar
//    _fileMenu.add(createMenuItem("Export as runnable jar",
//            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J,
//                    InputEvent.SHIFT_MASK | scMask),
//            new FileAction(FileAction.ASRUNJAR)));

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

    _fileMenu.addSeparator();
    _fileMenu.add(createMenuItem("Open Special Files",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK | scMask),
            new FileAction(FileAction.OPEN_SPECIAL)));

    _fileMenu.add(createMenuItem("Restart IDE",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
                    InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
            new FileAction(FileAction.RESTART)));

    if (showQuit) {
      _fileMenu.add(createMenuItem(_I("menuFileQuit"),
              null, new FileAction(FileAction.QUIT)));
    }
  }

  FileAction getFileAction(int tabIndex) {
    return new FileAction(tabIndex);
  }
  //</editor-fold>

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
    static final String RESTART = "doRestart";
    static final String OPEN_SPECIAL = "doOpenSpecial";

    FileAction() {
      super();
    }

    FileAction(String item) throws NoSuchMethodException {
      super(item);
    }

    FileAction(int tabIndex) {
      super();
      targetTab = tabIndex;
    }

    private int targetTab = -1;

    public void doNew(ActionEvent ae) {
      newTabEmpty();
    }

    //TODO not used
    public void doInsert(ActionEvent ae) {
      newTabWithContent(tabs.getLastClosed(), targetTab);
    }

    public void doLoad(ActionEvent ae) {
      newTabWithContent(null);
    }

    //TODO not used
    public void doLoadFolder(ActionEvent ae) {
      Debug.log(3, "IDE: doLoadFolder requested");
      ACCESSING_AS_FOLDER = true;
      doLoad(ae);
    }

    //TODO not used
    public void doRecent(ActionEvent ae) {
      log(3, "doRecent: menuOpenRecent: %s", ae.getActionCommand());
    }

    class OpenRecent extends MenuAction {

      OpenRecent() {
        super();
      }

      void openRecent(ActionEvent ae) {
        log(lvl, "openRecent: %s", ae.getActionCommand());
      }
    }

    public void doSave(ActionEvent ae) {
      String fname = null;
      try {
        EditorPane codePane = getCurrentCodePane();
        fname = codePane.saveTabContent();
        if (fname != null) {
          if (codePane.isPython || codePane.isText) {
            fname = codePane.getFilePath();
            codePane.showType();
          } else {
            fname = codePane.getSrcBundle();
          }
          setCurrentFileTabTitle(fname);
          tabs.setLastClosed(fname);
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
        fname = codePane.saveAsSelect(accessingAsFile);
        if (fname != null) {
          setCurrentFileTabTitle(fname);
        } else {
          log(-1, "doSaveAs: %s not completed", orgName);
        }
      } catch (Exception ex) {
        log(-1, "doSaveAs: %s Error: %s", orgName, ex.getMessage());
      }
    }

    //TODO not used
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
      saveSession(DO_SAVE_ALL, false);
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

    //TODO not used
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
      closeCurrentTab();
    }

    public void doAbout(ActionEvent ae) {
      showAbout();
    }

    public void doPreferences(ActionEvent ae) {
      showPreferencesWindow();
    }

    public void doOpenSpecial(ActionEvent ae) {
      (new Thread() {
        @Override
        public void run() {
          openSpecial();
        }
      }).start();
    }

    public void doRestart(ActionEvent ae) {
      log(lvl, "Restart IDE requested");
      if (closeIDE()) {
        log(lvl, "Restarting IDE");
        runTime.terminate(255, "Restarting IDE");
      }
      log(-1, "Restart IDE: did not work");
    }

    public void doQuit(ActionEvent ae) {
      terminate();
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="12 Init EditMenu">
  private String findText = "";

  private void initEditMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    int scMaskMETA = InputEvent.META_DOWN_MASK;
    int scMaskCTRL = InputEvent.CTRL_DOWN_MASK;

    _editMenu = new JMenu(_I("menuEdit"));
    _editMenu.setMnemonic(java.awt.event.KeyEvent.VK_E);

    _undoAction = new UndoAction();
    JMenuItem undoItem = _editMenu.add(_undoAction);
    undoItem.setAccelerator(
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, scMask));
    _redoAction = new RedoAction();
    JMenuItem redoItem = _editMenu.add(_redoAction);
    redoItem.setAccelerator(
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, scMask | InputEvent.SHIFT_DOWN_MASK));

    _editMenu.addSeparator();
    _editMenu.add(createMenuItem(_I("menuEditCopy"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, scMask),
            new EditAction(EditAction.COPY)));
    _editMenu.add(createMenuItem("Copy line",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, scMaskCTRL),
            new EditAction(EditAction.COPY)));
    _editMenu.add(createMenuItem(_I("menuEditCut"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, scMask),
            new EditAction(EditAction.CUT)));
    _editMenu.add(createMenuItem("Cut line",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, scMaskCTRL),
            new EditAction(EditAction.CUT)));
    _editMenu.add(createMenuItem(_I("menuEditPaste"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, scMask),
            new EditAction(EditAction.PASTE)));
    _editMenu.add(createMenuItem(_I("menuEditSelectAll"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, scMask),
            new EditAction(EditAction.SELECT_ALL)));

    _editMenu.addSeparator();
    JMenu findMenu = new JMenu(_I("menuFind"));
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

    EditAction() {
      super();
    }

    EditAction(String item) throws NoSuchMethodException {
      super(item);
    }

    //mac: defaults write -g ApplePressAndHoldEnabled -bool false

    private void performEditorAction(String action, ActionEvent ae) {
      EditorPane pane = getCurrentCodePane();
      pane.getActionMap().get(action).actionPerformed(ae);
    }

    private void selectLine() {
      EditorPane pane = getCurrentCodePane();
      Element lineAtCaret = pane.getLineAtCaret(-1);
      pane.select(lineAtCaret.getStartOffset(), lineAtCaret.getEndOffset());
    }

    public void doCut(ActionEvent ae) {
      if (getCurrentCodePane().getSelectedText() == null) {
        selectLine();
      }
      performEditorAction(DefaultEditorKit.cutAction, ae);
    }

    public void doCopy(ActionEvent ae) {
      if (getCurrentCodePane().getSelectedText() == null) {
        selectLine();
      }
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

  class FindAction extends MenuAction {

    static final String FIND = "doFind";
    static final String FIND_NEXT = "doFindNext";
    static final String FIND_PREV = "doFindPrev";

    FindAction() {
      super();
    }

    FindAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doFind(ActionEvent ae) {
//      _searchField.selectAll();
//      _searchField.requestFocus();
      findText = Sikulix.input(
              "Enter text to be searched (case sensitive)\n" +
                      "Start with ! to search case insensitive\n",
              findText, "SikuliX IDE -- Find");
      if (null == findText) {
        return;
      }
      if (findText.isEmpty()) {
        Debug.error("Find(%s): search text is empty", findText);
        return;
      }
      if (!findStr(findText)) {
        Debug.error("Find(%s): not found", findText);
      }
    }

    public void doFindNext(ActionEvent ae) {
      if (!findText.isEmpty()) {
        findNext(findText);
      }
    }

    public void doFindPrev(ActionEvent ae) {
      if (!findText.isEmpty()) {
        findPrev(findText);
      }
    }

    private boolean _find(String str, int begin, boolean forward) {
      if (str == "!") {
        return false;
      }
      EditorPane codePane = getCurrentCodePane();
      int pos = codePane.search(str, begin, forward);
      log(4, "find \"" + str + "\" at " + begin + ", found: " + pos);
      if (pos < 0) {
        return false;
      }
      return true;
    }

    boolean findStr(String str) {
      if (getCurrentCodePane() != null) {
        return _find(str, getCurrentCodePane().getCaretPosition(), true);
      }
      return false;
    }

    boolean findPrev(String str) {
      if (getCurrentCodePane() != null) {
        return _find(str, getCurrentCodePane().getCaretPosition(), false);
      }
      return false;
    }

    boolean findNext(String str) {
      if (getCurrentCodePane() != null) {
        return _find(str,
                getCurrentCodePane().getCaretPosition() + str.length(),
                true);
      }
      return false;
    }

//    public void setFailed(boolean failed) {
//      Debug.log(7, "search failed: " + failed);
//      _searchField.setBackground(Color.white);
//      if (failed) {
//        _searchField.setForeground(COLOR_SEARCH_FAILED);
//      } else {
//        _searchField.setForeground(COLOR_SEARCH_NORMAL);
//      }
//    }
  }

  class UndoAction extends AbstractAction {

    UndoAction() {
      super(_I("menuEditUndo"));
      setEnabled(false);
    }

    void updateUndoState() {
      EditorPane pane = getCurrentCodePane();
      if (pane != null) {
        if (pane.getUndoRedo(pane).getUndoManager().canUndo()) {
          setEnabled(true);
        }
      } else {
        setEnabled(false);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      EditorPane pane = getCurrentCodePane();
      if (pane != null) {
        try {
          pane.getUndoRedo(pane).getUndoManager().undo();
        } catch (CannotUndoException ex) {
        }
        updateUndoState();
        _redoAction.updateRedoState();
      }
    }
  }

  class RedoAction extends AbstractAction {

    RedoAction() {
      super(_I("menuEditRedo"));
      setEnabled(false);
    }

    protected void updateRedoState() {
      EditorPane pane = getCurrentCodePane();
      if (pane != null) {
        if (pane.getUndoRedo(pane).getUndoManager().canRedo()) {
          setEnabled(true);
        }
      } else {
        setEnabled(false);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      EditorPane pane = getCurrentCodePane();
      if (pane != null) {
        try {
          pane.getUndoRedo(pane).getUndoManager().redo();
        } catch (CannotRedoException ex) {
        }
        updateRedoState();
        _undoAction.updateUndoState();
      }
    }
  }

  void updateUndoRedoStates() {
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="13 Init Run Menu">
  JMenu getRunMenu() {
    return _runMenu;
  }

  private void initRunMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _runMenu = new JMenu(_I("menuRun"));
    _runMenu.setMnemonic(java.awt.event.KeyEvent.VK_R);
    _runMenu.add(createMenuItem(_I("menuRunRun"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, scMask),
            new RunAction(RunAction.RUN)));
    _runMenu.add(createMenuItem(_I("menuRunRunAndShowActions"),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
                    InputEvent.ALT_DOWN_MASK | scMask),
            new RunAction(RunAction.RUN_SHOW_ACTIONS)));
    _runMenu.add(createMenuItem("Run selection",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
                    InputEvent.SHIFT_DOWN_MASK | scMask),
            new RunAction(RunAction.RUN_SELECTION)));
  }

  class RunAction extends MenuAction {

    static final String RUN = "runNormal";
    static final String RUN_SHOW_ACTIONS = "runShowActions";
    static final String RUN_SELECTION = "runSelection";

    RunAction() {
      super();
    }

    RunAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void runNormal(ActionEvent ae) {
      _btnRun.runCurrentScript();
    }

    public void runShowActions(ActionEvent ae) {
      _btnRunViz.runCurrentScript();
    }

    public void runSelection(ActionEvent ae) {
      getCurrentCodePane().runSelection();
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="14 Init View Menu">
  private JCheckBoxMenuItem chkShowThumbs;

  private void initViewMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _viewMenu = new JMenu(_I("menuView"));
    _viewMenu.setMnemonic(java.awt.event.KeyEvent.VK_V);

    boolean prefMorePlainText = PreferencesUser.get().getPrefMorePlainText();

    chkShowThumbs = new JCheckBoxMenuItem(_I("menuViewShowThumbs"), !prefMorePlainText);
    _viewMenu.add(createMenuItem(chkShowThumbs,
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, scMask),
            new ViewAction(ViewAction.SHOW_THUMBS)));

//TODO Message Area clear
//TODO Message Area LineBreak
  }

  class ViewAction extends MenuAction {

    static final String SHOW_THUMBS = "toggleShowThumbs";

    ViewAction() {
      super();
    }

    ViewAction(String item) throws NoSuchMethodException {
      super(item);
    }

    private long lastWhen = -1;

    public void toggleShowThumbs(ActionEvent ae) {
      if (runTime.runningMac) {
        if (lastWhen < 0) {
          lastWhen = new Date().getTime();
        } else {
          long delay = new Date().getTime() - lastWhen;
          lastWhen = -1;
          if (delay < 500) {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
            source.setState(!source.getState());
            return;
          }
        }
      }
      boolean showThumbsState = chkShowThumbs.getState();
      getCurrentCodePane().showThumbs = showThumbsState;
      getCurrentCodePane().doReparse();
      return;
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="15 Init ToolMenu">
  private void initToolMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _toolMenu = new JMenu(_I("menuTool"));
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

    ToolAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void extensions(ActionEvent ae) {
      showExtensions();
    }

    public void android(ActionEvent ae) {
      androidSupport();
    }
  }

  private void showExtensions() {
    ExtensionManager.show();
  }

  private static IScreen defaultScreen = null;

  static IScreen getDefaultScreen() {
    return defaultScreen;
  }


  private void androidSupport() {
    final ADBScreen aScr = new ADBScreen();
    String title = "Android Support - !!EXPERIMENTAL!!";
    if (aScr.isValid()) {
      String warn = "Device found: " + aScr.getDeviceDescription() + "\n\n" +
              "click Check: a short test is run with the device\n" +
              "click Default...: set device as default screen for capture\n" +
              "click Cancel: capture is reset to local screen\n" +
              "\nBE PREPARED: Feature is experimental - no guarantee ;-)";
      String[] options = new String[3];
      options[WARNING_DO_NOTHING] = "Check";
      options[WARNING_ACCEPTED] = "Default Android";
      options[WARNING_CANCEL] = "Cancel";
      int ret = JOptionPane.showOptionDialog(this, warn, title, 0, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
      if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
        defaultScreen = null;
        return;
      }
      if (ret == WARNING_DO_NOTHING) {
        SikulixIDE.hideIDE();
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
    ADBScreen.stop();
    SikulixIDE.showIDE();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="16 Init Help Menu">
  private static Boolean newBuildAvailable = null;
  private static String newBuildStamp = "";

  private void initHelpMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    _helpMenu = new JMenu(_I("menuHelp"));
    _helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);

    _helpMenu.add(createMenuItem(_I("menuHelpQuickStart"),
            null, new HelpAction(HelpAction.QUICK_START)));
    _helpMenu.addSeparator();

    _helpMenu.add(createMenuItem(_I("menuHelpGuide"),
            null, new HelpAction(HelpAction.OPEN_DOC)));
//    _helpMenu.add(createMenuItem(_I("menuHelpDocumentations"),
//            null, new HelpAction(HelpAction.OPEN_GUIDE)));
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
    _helpMenu.add(createMenuItem("SikuliX1 Downloads",
            null, new HelpAction(HelpAction.OPEN_DOWNLOADS)));
    _helpMenu.add(createMenuItem(_I("menuHelpCheckUpdate"),
            null, new HelpAction(HelpAction.CHECK_UPDATE)));
  }

  private void lookUpdate() {
    newBuildAvailable = null;
    String token = "This version was built at ";
    String httpDownload = "#https://raiman.github.io/SikuliX1/downloads.html";
    String pageDownload = FileManager.downloadURLtoString(httpDownload);
    if (!pageDownload.isEmpty()) {
      newBuildAvailable = false;
    }
    int locStamp = pageDownload.indexOf(token);
    if (locStamp > 0) {
      locStamp += token.length();
      String latestBuildFull = pageDownload.substring(locStamp, locStamp + 16);
      String latestBuild = latestBuildFull.replaceAll("-", "").replace("_", "").replace(":", "");
      String actualBuild = runTime.sxBuildStamp;
      try {
        long lb = Long.parseLong(latestBuild);
        long ab = Long.parseLong(actualBuild);
        if (lb > ab) {
          newBuildAvailable = true;
          newBuildStamp = latestBuildFull;
        }
        log(lvl, "latest build: %s this build: %s (newer: %s)", latestBuild, actualBuild, newBuildAvailable);
      } catch (NumberFormatException e) {
        log(-1, "check for new build: stamps not readable");
      }
    }
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
    static final String OPEN_DOWNLOADS = "openDownloads";

    HelpAction() {
      super();
    }

    HelpAction(String item) throws NoSuchMethodException {
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

    public void openDownloads(ActionEvent ae) {
      FileManager.openURL("https://raiman.github.io/SikuliX1/downloads.html");
    }

    public void doCheckUpdate(ActionEvent ae) {
      if (!checkUpdate(false)) {
        lookUpdate();
        int msgType = newBuildAvailable != null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        String updMsg = newBuildAvailable != null ? (newBuildAvailable ?
                _I("msgUpdate") + ": " + newBuildStamp :
                _I("msgNoUpdate")) : _I("msgUpdateError");
        JOptionPane.showMessageDialog(null, updMsg,
                runTime.SXVersionIDE, msgType);
      }
    }

    boolean checkUpdate(boolean isAutoCheck) {
      String ver = "";
      String details;
      AutoUpdater au = new AutoUpdater();
      PreferencesUser pref = PreferencesUser.get();
      log(3, "being asked to check update");
      int whatUpdate = au.checkUpdate();
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
        PreferencesUser.get().setLastSeenUpdate(ver);
        return true;
      }
      return false;
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="20 Init ToolBar Buttons">
  private ButtonCapture _btnCapture;
  private ButtonRun _btnRun = null, _btnRunViz = null;

  private JToolBar initToolbar() {
//    if (ENABLE_UNIFIED_TOOLBAR) {
//      MacUtils.makeWindowLeopardStyle(this.getRootPane());
//    }

    JToolBar toolbar = new JToolBar();
    JButton btnInsertImage = new ButtonInsertImage();
    JButton btnSubregion = new ButtonSubregion().init();
    JButton btnLocation = new ButtonLocation().init();
    JButton btnOffset = new ButtonOffset().init();
    JButton btnShow = new ButtonShow().init();
    JButton btnShowIn = new ButtonShowIn().init();
    _btnCapture = new ButtonCapture();
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

//    JComponent jcSearchField = createSearchField();
//    toolbar.add(jcSearchField);

    toolbar.add(Box.createRigidArea(new Dimension(7, 0)));
    toolbar.setFloatable(false);
    //toolbar.setMargin(new Insets(0, 0, 0, 5));
    return toolbar;
  }

  class ButtonInsertImage extends ButtonOnToolbar implements ActionListener {

    ButtonInsertImage() {
      super();
      URL imageURL = SikulixIDE.class.getResource("/icons/insert-image-icon.png");
      setIcon(new ImageIcon(imageURL));
      setText(SikulixIDE._I("btnInsertImageLabel"));
      //setMaximumSize(new Dimension(26,26));
      setToolTipText(SikulixIDE._I("btnInsertImageHint"));
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

    ButtonSubregion() {
      super();
      promptText = SikulixIDE._I("msgCapturePrompt");
      buttonText = "Region"; // SikuliIDE._I("btnRegionLabel");
      iconFile = "/icons/region-icon.png";
      buttonHint = SikulixIDE._I("btnRegionHint");
    }

    ButtonSubregion init() {
      URL imageURL = SikulixIDE.class.getResource(iconFile);
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

    void nothingTodo() {
    }

    boolean shouldRun() {
      log(3, "TRACE: ButtonSubRegion triggered");
      return true;
    }

    @Override
    public void update(EventSubject es) {
      OverlayCapturePrompt ocp = (OverlayCapturePrompt) es;
      ScreenImage simg = ocp.getSelection();
      Screen.closePrompt();
      Screen.resetPrompt(ocp);
      captureComplete(simg);
      SikulixIDE.showAgain();
    }

    void captureComplete(ScreenImage simg) {
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

    ButtonLocation() {
      super();
      promptText = "Select a Location";
      buttonText = "Location";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Define a Location as center of your selection";
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

    ButtonOffset() {
      super();
      promptText = "Select an Offset";
      buttonText = "Offset";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Define an Offset as width and height of your selection";
    }

    @Override
    public void captureComplete(ScreenImage simg) {
      int x, y, ox, oy;
      if (simg != null) {
        Rectangle roi = simg.getROI();
        ox = (int) roi.getWidth();
        oy = (int) roi.getHeight();
        Location start = simg.getStart();
        Location end = simg.getEnd();
        if (end.x < start.x) {
          ox *= -1;
        }
        if (end.y < start.y) {
          oy *= -1;
        }
        getCurrentCodePane().insertString(String.format("Offset(%d, %d)", ox, oy));
      }
    }
  }

  class ButtonShow extends ButtonOnToolbar implements ActionListener {

    String buttonText;
    String iconFile;
    String buttonHint;

    ButtonShow() {
      super();
      buttonText = "Show";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Find and highlight the image in the line having the cursor";
    }

    ButtonShow init() {
      URL imageURL = SikulixIDE.class.getResource(iconFile);
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
                  + ", 0); if (m != null) m.highlight(2);";
        } else if (item.startsWith("\"")) {
          eval = "m = Screen.all().exists(" + item
                  + ", 0); if (m != null) m.highlight(2);";
        }
        if (!eval.isEmpty()) {
          IScriptRunner runner = Runner.getRunner(JavaScriptRunner.class);
          runner.evalScript("#" + eval, null);
          return;
        }
      }
      Sikulix.popup("Nothing to show!" +
              "\nThe line with the cursor should contain:" +
              "\n- an absolute Region or Location" +
              "\n- an image file name or" +
              "\n- a Pattern with an image file name");
    }
  }

  class ButtonShowIn extends ButtonSubregion {

    String item = "";

    ButtonShowIn() {
      super();
      buttonText = "Show in";
      iconFile = "/icons/region-icon.png";
      buttonHint = "Same as Show, but in the selected region";
    }

    @Override
    public boolean shouldRun() {
      log(3, "TRACE: ButtonShowIn triggered");
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
      } else {
        item = "";
      }
      return !item.isEmpty();
    }

    @Override
    void nothingTodo() {
      Sikulix.popup("Nothing to show");
    }

    @Override
    public void captureComplete(ScreenImage simg) {
      if (simg != null) {
        Region reg = new Region(simg.getROI());
        String itemReg = String.format("new Region(%d, %d, %d, %d)", reg.x, reg.y, reg.w, reg.h);
        item = item.replace("#region#", itemReg);
        Runner.getRunner(JavaScriptRunner.class).evalScript(item, null);
      } else {
        SikulixIDE.showAgain();
        nothingTodo();
      }
    }
  }

  //</editor-fold>

  //<editor-fold desc="21 Init Run Buttons">
  boolean doBeforeRun() {
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
    return true;
  }

  synchronized boolean isRunningScript() {
    return ideIsRunningScript;
  }

  synchronized void setIsRunningScript(boolean state) {
    ideIsRunningScript = state;
  }

  private boolean ideIsRunningScript = false;

  void addErrorMark(int line) {
    if (line < 1) {
      return;
    }
    JScrollPane scrPane = (JScrollPane) tabs.getSelectedComponent();
    EditorLineNumberView lnview = (EditorLineNumberView) (scrPane.getRowHeader().getView());
    lnview.addErrorMark(line);
    EditorPane codePane = SikulixIDE.this.getCurrentCodePane();
    codePane.jumpTo(line);
    codePane.requestFocus();
  }

  void resetErrorMark() {
    JScrollPane scrPane = (JScrollPane) tabs.getSelectedComponent();
    EditorLineNumberView lnview = (EditorLineNumberView) (scrPane.getRowHeader().getView());
    lnview.resetErrorMark();
  }

  class ButtonRun extends ButtonOnToolbar implements ActionListener {

    private Thread thread = null;

    ButtonRun() {
      super();

      URL imageURL = SikulixIDE.class.getResource("/icons/run_big_green.png");
      setIcon(new ImageIcon(imageURL));
      initTooltip();
      addActionListener(this);
      setText(_I("btnRunLabel"));
      //setMaximumSize(new Dimension(45,45));
    }

    private void initTooltip() {
      PreferencesUser pref = PreferencesUser.get();
      String strHotkey = Key.convertKeyToText(
              pref.getStopHotkey(), pref.getStopHotkeyModifiers());
      String stopHint = _I("btnRunStopHint", strHotkey);
      setToolTipText(_I("btnRun", stopHint));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
//      if (getCurrentCodePane().isText) {
//        return;
//      }
      runCurrentScript();
    }

    void runCurrentScript() {
      if (System.out.checkError()) {
        Sikulix.popError("System.out is broken (console output)!"
                + "\nYou will not see any messages anymore!"
                + "\nSave your work and restart the IDE!"
                + "\nYou may ignore this on your own risk!", "Fatal Error");
      }
      SikulixIDE.getStatusbar().setMessage("... PLEASE WAIT ... checking IDE state before running script");
      if (isRunningScript()
              || getCurrentCodePane().getDocument().getLength() == 0
              || !doBeforeRun()) {
        return;
      }
      SikulixIDE.getStatusbar().resetMessage();
      SikulixIDE.hideIDE();
      RunTime.pause(0.1f);
      sikulixIDE.setIsRunningScript(true);
      EditorPane editorPane = getCurrentCodePane();
      File scriptFile;
      if (editorPane.isDirty()) {
        editorPane.checkSource(); // runCurrentScript
        if (editorPane.isTemp()) {
          scriptFile = editorPane.getCurrentFile(false);
        } else {
          scriptFile = FileManager.createTempFile(Runner.getExtension(editorPane.getType()));
        }
        if (scriptFile != null) {
          try {
            editorPane.write(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scriptFile), "UTF8")));
          } catch (Exception ex) {
            scriptFile = null;
          }
        }
        if (scriptFile == null) {
          log(-1, "runCurrentScript: temp file for running not available");
          return;
        }
      } else {
        scriptFile = editorPane.getCurrentFile();
      }
      messages.clear();
      resetErrorMark();
      IScriptRunner scriptRunner = Runner.getRunner(editorPane.getType());
      if (scriptRunner == null) {
        log(-1, "runCurrentScript: Could not load a script runner for: %s", editorPane.getType());
        return;
      }
      beforeRun();
      //      ImagePath.reset();
      final SubRun doRun = new SubRun(scriptRunner, scriptFile, this);
      thread = new Thread(doRun);
      thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
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
      thread.start();
    }

    void beforeRun() {
      Settings.ActionLogs = prefs.getPrefMoreLogActions();
      Settings.DebugLogs = prefs.getPrefMoreLogDebug();
      Settings.InfoLogs = prefs.getPrefMoreLogInfo();
      Settings.Highlight = prefs.getPrefMoreHighlight();
    }

    boolean isRunning() {
      return thread != null;
    }

    void notRunning() {
      thread = null;
    }

    boolean stopRunScript() {
      if (thread != null) {
        thread.interrupt();
        thread.stop();
        thread = null;
        return true;
      }
      return false;
    }
  }

  class ButtonRunViz extends ButtonRun {

    ButtonRunViz() {
      super();
      URL imageURL = SikulixIDE.class.getResource("/icons/run_big_yl.png");
      setIcon(new ImageIcon(imageURL));
      setToolTipText(_I("menuRunRunAndShowActions"));
      setText(_I("btnRunSlowMotionLabel"));
    }

    @Override
    protected void beforeRun() {
      super.beforeRun();
      Settings.setShowActions(true);
    }
  }

  private class SubRun implements Runnable {
    private boolean finished = false;
    private int ret = 0;
    private File scriptFile = null;
    private IScriptRunner scriptRunner = null;
    private IScriptRunner.Options options = new IScriptRunner.Options();
    ButtonRun buttonRun;

    SubRun(IScriptRunner scriptRunner, File scriptFile, ButtonRun buttonRun) {
      this.scriptFile = scriptFile;
      this.scriptRunner = scriptRunner;
      this.buttonRun = buttonRun;
    }

    @Override
    public void run() {
      try {
        options.setRunningInIDE();
        ret = scriptRunner.runScript(scriptFile.getAbsolutePath(), RunTime.getUserArgs(), options);
      } catch (Exception ex) {
        log(-1, "(%s) runScript: Exception: %s", scriptRunner.getName(), ex);
      }
      hasFinished(true);
      afterRun();
    }

    int getRet() {
      return ret;
    }

    boolean hasFinished() {
      return hasFinished(false);
    }

    synchronized boolean hasFinished(boolean state) {
      if (state) {
        finished = true;
      }
      return finished;
    }

    void afterRun() {
      addErrorMark(options.getErrorLine());
      if (Image.getIDEshouldReload()) {
        EditorPane pane = getCurrentCodePane();
        int line = pane.getLineNumberAtCaret(pane.getCaretPosition());
        getCurrentCodePane().doReparse();
        getCurrentCodePane().jumpTo(line);
      }
      setIsRunningScript(false);
      RunTime.cleanUp();
      buttonRun.notRunning();
      SikulixIDE.showAgain();
    }
  }
  //</editor-fold>

  //<editor-fold desc="30 MsgArea, Statusbar">
  private void initMessageArea() {
    messageArea = new JTabbedPane();
    messages = new EditorConsolePane();
    messageArea.addTab(_I("paneMessage"), null, messages, "DoubleClick to hide/unhide");
    if (Settings.isWindows() || Settings.isLinux()) {
      messageArea.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }
    messageArea.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() < 2) {
          return;
        }
        toggleCollapsed();
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

  private JTabbedPane messageArea;
  private EditorConsolePane messages;

  void clearMessageArea() {
    messages.clear();
  }

  void collapseMessageArea() {
    if (messageAreaCollapsed) {
      return;
    }
    toggleCollapsed();
  }

  void uncollapseMessageArea() {
    if (messageAreaCollapsed) {
      toggleCollapsed();
    }
  }

  private void toggleCollapsed() {
    if (messageAreaCollapsed) {
      mainPane.setDividerLocation(mainPane.getLastDividerLocation());
      messageAreaCollapsed = false;
    } else {
      int pos = mainPane.getWidth() - 35;
      mainPane.setDividerLocation(pos);
      messageAreaCollapsed = true;
    }
  }

  private boolean messageAreaCollapsed = false;

  private SikuliIDEStatusBar initStatusbar() {
    _status = new SikuliIDEStatusBar();
    return _status;
  }

  static SikuliIDEStatusBar getStatusbar() {
    if (sikulixIDE == null) {
      return null;
    } else {
      return sikulixIDE._status;
    }
  }

  private SikuliIDEStatusBar _status = null;

  private void initWindowListener() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        SikulixIDE.this.quit();
      }
    });
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        PreferencesUser.get().setIdeSize(SikulixIDE.this.getSize());
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        PreferencesUser.get().setIdeLocation(SikulixIDE.this.getLocation());
      }
    });
  }

  private void initTooltip() {
    ToolTipManager tm = ToolTipManager.sharedInstance();
    tm.setDismissDelay(30000);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="25 Init ShortCuts HotKeys">
  private void nextTab() {
    int i = tabs.getSelectedIndex();
    int next = (i + 1) % tabs.getTabCount();
    tabs.setSelectedIndex(next);
  }

  private void prevTab() {
    int i = tabs.getSelectedIndex();
    int prev = (i - 1 + tabs.getTabCount()) % tabs.getTabCount();
    tabs.setSelectedIndex(prev);
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

  void removeCaptureHotkey() {
    HotkeyManager.getInstance().removeHotkey("Capture");
  }

  void installCaptureHotkey() {
    HotkeyManager.getInstance().addHotkey("Capture", new HotkeyListener() {
      @Override
      public void hotkeyPressed(HotkeyEvent e) {
        if (!isRunningScript()) {
          onQuickCapture();
        }
      }
    });
  }

  void onQuickCapture() {
    onQuickCapture(null);
  }

  void onQuickCapture(String arg) {
    if (_inited) {
      log(3, "QuickCapture");
      _btnCapture.capture(0);
    }
  }

  void removeStopHotkey() {
    HotkeyManager.getInstance().removeHotkey("Abort");
  }

  void installStopHotkey() {
    HotkeyManager.getInstance().addHotkey("Abort", new HotkeyListener() {
      @Override
      public void hotkeyPressed(HotkeyEvent e) {
        onStopRunning();
      }
    });
  }

  void onStopRunning() {
    boolean shouldCleanUp = false;
    if (_btnRun != null && _btnRun.isRunning()) {
      shouldCleanUp = _btnRun.stopRunScript();
    }
    if (_btnRunViz != null && _btnRunViz.isRunning()) {
      shouldCleanUp = _btnRunViz.stopRunScript();
    }
    if (shouldCleanUp) {
      log(3, "AbortKey was pressed");
      //RunTime.cleanUp();
      //setVisible(true);
      showAgain();
    } else {
      log(3, "AbortKey was pressed, but nothing to stop here ;-)");
    }
  }

  private void initHotkeys() {
    installCaptureHotkey();
    installStopHotkey();
  }
  //</editor-fold>
}
