/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.*;
import org.sikuli.idesupport.ExtensionManager;
import org.sikuli.idesupport.IDEDesktopSupport;
import org.sikuli.idesupport.IDESupport;
import org.sikuli.idesupport.IIDESupport;
import org.sikuli.script.Image;
import org.sikuli.script.Sikulix;
import org.sikuli.script.*;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runnerSupport.JythonSupport;
import org.sikuli.script.runnerSupport.Runner;
import org.sikuli.script.runners.InvalidRunner;
import org.sikuli.script.runners.JythonRunner;
import org.sikuli.script.runners.TextRunner;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.IScreen;
import org.sikuli.script.support.Recorder;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.generators.ICodeGenerator;
import org.sikuli.script.support.gui.SXDialog;
import org.sikuli.script.support.gui.SikuliIDEI18N;
import org.sikuli.script.support.recorder.actions.IRecordedAction;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;
import org.sikuli.util.OverlayCapturePrompt;
import org.sikuli.util.SikulixFileChooser;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class SikulixIDE extends JFrame {

  static final String me = "IDE: ";

  private static void log(String message, Object... args) {
    Debug.logx(3, me + message, args);
  }

  private static void trace(String message, Object... args) {
    Debug.logx(4, me + message, args);
  }

  private static void error(String message, Object... args) {
    Debug.logx(-1, me + message, args);
  }

  private static void fatal(String message, Object... args) {
    Debug.logx(-1, me + "FATAL: " + message, args);
  }

  //<editor-fold desc="00 IDE instance">
  static final SikulixIDE sikulixIDE = new SikulixIDE();

  static PreferencesUser prefs;

  private SikulixIDE() {
    prefs = PreferencesUser.get();
    if (prefs.getUserType() < 0) {
      prefs.setIdeSession("");
      prefs.setDefaults();
    }
  }

  public static SikulixIDE get() {
    if (sikulixIDE == null) {
      throw new SikuliXception("SikulixIDE:get(): instance should not be null");
    }
    return sikulixIDE;
  }

  static Rectangle ideWindowRect = null;

  public static Rectangle getWindowRect() {
    Dimension windowSize = prefs.getIdeSize();
    Point windowLocation = prefs.getIdeLocation();
    if (windowSize.width < 700) {
      windowSize.width = 800;
    }
    if (windowSize.height < 500) {
      windowSize.height = 600;
    }
    return new Rectangle(windowLocation, windowSize);
  }

  public static Point getWindowCenter() {
    return new Point((int) getWindowRect().getCenterX(), (int) getWindowRect().getCenterY());
  }

  public static Point getWindowTop() {
    Rectangle rect = getWindowRect();
    int x = rect.x + rect.width / 2;
    int y = rect.y + 30;
    return new Point(x, y);
  }

  static JFrame ideWindow = null;

  public static void setWindow() {
    if (ideWindow == null) {
      ideWindow = sikulixIDE;
    }
  }

  public void setIDETitle(String title) {
    ideWindow.setTitle(title);
  }

  public static void doShow() {
    showAgain();
  }

  public static boolean notHidden() {
    return ideWindow.isVisible();
  }

  public static void doHide() {
    doHide(0.5f);
  }

  public static void doHide(float waitTime) {
    ideWindow.setVisible(false);
    RunTime.pause(waitTime);
  }

  static void showAgain() {
    ideWindow.setVisible(true);
    sikulixIDE.getActiveContext().focus();
  }

  //TODO showAfterStart to be revised
  public static void showAfterStart() {
    org.sikuli.ide.Sikulix.stopSplash();
    ideWindow.setVisible(true);
    sikulixIDE.mainPane.setDividerLocation(0.6); //TODO saved value
    sikulixIDE.getActiveContext().focus();
  }

  static String _I(String key, Object... args) {
    try {
      return SikuliIDEI18N._I(key, args);
    } catch (Exception e) {
      log("[I18N] " + key);
      return key;
    }
  }

  static ImageIcon getIconResource(String name) {
    URL url = SikulixIDE.class.getResource(name);
    if (url == null) {
      Debug.error("IDE: Could not load \"" + name + "\" icon");
      return null;
    }
    return new ImageIcon(url);
  }
  //</editor-fold>

  //<editor-fold desc="01 startup / quit">
  protected static void start() {

    ideWindowRect = getWindowRect();

    IDEDesktopSupport.init();
    Commons.startLog(1, "IDEDesktopSupport ready (%4.1f)", Commons.getSinceStart());

    IDESupport.init();
    Commons.startLog(1, "IDESupport ready --- GUI start (%4.1f)", Commons.getSinceStart());

    sikulixIDE.startGUI();
  }

  boolean ideIsQuitting = false;

  private boolean closeIDE() {
    ideIsQuitting = true;
    if (!doBeforeQuit()) {
      return false;
    }
    ideIsQuitting = false;
    return true;
  }

  private boolean doBeforeQuit() {
    if (checkDirtyPanes()) {
      int answer = askForSaveAll("Quit");
      if (answer == SXDialog.DECISION_CANCEL) {
        return false;
      }
      if (answer == SXDialog.DECISION_ACCEPT) {
        return saveSession(DO_SAVE_ALL);
      }
      log("Quit: without saving anything");
    }
    return saveSession(DO_SAVE_NOTHING);
  }

  int DO_SAVE_ALL = 1;
  int DO_SAVE_NOTHING = -1;

  private int askForSaveAll(String typ) {
    String message = "Some scripts are not saved yet!";
    String title = SikuliIDEI18N._I("dlgAskCloseTab");
    String ignore = typ + " immediately";
    String accept = "Save all and " + typ;
    return SXDialog.askForDecision(sikulixIDE, title, message, ignore, accept);
  }

  public boolean terminate() {
    log("Quit requested");
    if (closeIDE()) {
      RunTime.terminate(0, "");
    }
    log("Quit: cancelled or did not work");
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="02 init IDE">
  private void startGUI() {
    setWindow();

    installCaptureHotkey();
    installStopHotkey();

    ideWindow.setSize(ideWindowRect.getSize());
    ideWindow.setLocation(ideWindowRect.getLocation());

    Debug.log("IDE: Adding components to window");
    initMenuBars(ideWindow);
    final Container ideContainer = ideWindow.getContentPane();
    ideContainer.setLayout(new BorderLayout());
    Debug.log("IDE: creating tabbed editor");
    initTabs();
    Debug.log("IDE: creating message area");
    initMessageArea();
    Debug.log("IDE: creating combined work window");
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

    Debug.log("IDE: Putting all together");
    JPanel editPane = new JPanel(new BorderLayout(0, 0));

    editPane.add(mainPane, BorderLayout.CENTER);
    ideContainer.add(editPane, BorderLayout.CENTER);
    Debug.log("IDE: Putting all together - after main pane");

    JToolBar tb = initToolbar();
    ideContainer.add(tb, BorderLayout.NORTH);
    Debug.log("IDE: Putting all together - after toolbar");

    _status = new SikuliIDEStatusBar();
    ideContainer.add(_status, BorderLayout.SOUTH);

    Debug.log("IDE: Putting all together - before layout");
    ideContainer.doLayout();

    Debug.log("IDE: Putting all together - after layout");
    ideWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    ideWindow.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        terminate();
      }
    });

    ideWindow.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        PreferencesUser.get().setIdeSize(ideWindow.getSize());
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        PreferencesUser.get().setIdeLocation(ideWindow.getLocation());
      }
    });
    ToolTipManager.sharedInstance().setDismissDelay(30000);

    createEmptyScriptContext();
    Debug.log("IDE: Putting all together - Restore last Session");
    //restoreSession();

    initShortcutKeys();
    Commons.startLog(3, "IDE ready: on Java %d (%4.1f sec)", Commons.getJavaVersion(), Commons.getSinceStart());
  }

  //TODO initShortcutKey
  private void initShortcutKeys() {
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      private boolean isKeyNextTab(java.awt.event.KeyEvent ke) {
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_TAB
            && ke.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
          return true;
        }
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_CLOSE_BRACKET
            && ke.getModifiersEx() == (InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) {
          return true;
        }
        return false;
      }

      private boolean isKeyPrevTab(java.awt.event.KeyEvent ke) {
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_TAB
            && ke.getModifiersEx() == (InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) {
          return true;
        }
        if (ke.getKeyCode() == java.awt.event.KeyEvent.VK_OPEN_BRACKET
            && ke.getModifiersEx() == (InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) {
          return true;
        }
        return false;
      }

      public void eventDispatched(AWTEvent e) {
        java.awt.event.KeyEvent ke = (java.awt.event.KeyEvent) e;
        if (ke.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
          if (isKeyNextTab(ke)) {
            int i = tabs.getSelectedIndex();
            int next = (i + 1) % tabs.getTabCount();
            tabs.setSelectedIndex(next);
          } else if (isKeyPrevTab(ke)) {
            int i = tabs.getSelectedIndex();
            int prev = (i - 1 + tabs.getTabCount()) % tabs.getTabCount();
            tabs.setSelectedIndex(prev);
          }
        }
      }
    }, AWTEvent.KEY_EVENT_MASK);
  }

  static SikuliIDEStatusBar getStatusbar() {
    return sikulixIDE._status;
  }

  private SikuliIDEStatusBar _status;

  private JSplitPane mainPane;

  private void initTabs() {
    tabs = new CloseableTabbedPane();
    tabs.setUI(new AquaCloseableTabbedPaneUI());
    tabs.addCloseableTabbedPaneListener(tabIndexToClose -> {
      getContextAt(tabIndexToClose).close();
      return false;
    });
    tabs.addChangeListener(e -> {
      JTabbedPane tab = (JTabbedPane) e.getSource();
      int ix = tab.getSelectedIndex();
      switchContext(ix);
    });
  }

  CloseableTabbedPane getTabs() {
    return tabs;
  }

  private CloseableTabbedPane tabs;
  //</editor-fold>

  //<editor-fold desc="03 PaneContext">
  List<PaneContext> contexts = new ArrayList<>();
  List<PaneContext> contextsClosed = new ArrayList<>();
  String tempName = "sxtemp";
  int tempIndex = 1;
  PaneContext lastContext = null;

  PaneContext getActiveContext() {
    final int ix = tabs.getSelectedIndex();
    if (ix < 0) {
      fatal("PaneContext: no context available"); //TODO possible?
    }
    return contexts.get(ix);
  }

  PaneContext setActiveContext(int pos) {
    tabs.setSelectedIndex(pos);
    return contexts.get(pos);
  }

  PaneContext getContextAt(int ix) {
    return contexts.get(ix);
  }

  void switchContext(int ix) {
    if (ix < 0) {
      return;
    }
    if (ix >= contexts.size()) {
      RunTime.terminate(999, "IDE: switchPane: invalid tab index: %d (valid: 0 .. %d)",
          ix, contexts.size() - 1);
    }
    PaneContext context = contexts.get(ix);
    PaneContext previous = lastContext;
    lastContext = context;

    if (null != previous && previous.isDirty()) {
      previous.save();
    }
    setIDETitle(context.getFile().getAbsolutePath());
    ImagePath.setBundleFolder(context.getFolder());
    if (context.isText()) {
      collapseMessageArea();
    } else {
      uncollapseMessageArea();
    }
    getStatusbar().setType(context.getType());
    chkShowThumbs.setState(getActiveContext().getShowThumbs());

    final EditorPane editorPane = context.getPane();
    int dot = editorPane.getCaret().getDot();
    editorPane.setCaretPosition(dot);
    updateUndoRedoStates();
  }

  void createEmptyScriptContext() {
    final PaneContext context = new PaneContext();
    context.setRunner(IDESupport.getDefaultRunner());
    context.setFile();
    context.create();
  }

  void createEmptyTextContext() {
    final PaneContext context = new PaneContext();
    context.setRunner(Runner.getRunner(TextRunner.class));
    context.setFile();
    context.create();
  }

  void createFileContext(File file) {
    final int pos = alreadyOpen(file);
    if (pos >= 0) {
      setActiveContext(pos);
      log("PaneContext: alreadyopen: %s", file);
      return;
    }
    final PaneContext context = new PaneContext();
    context.setFile(file);
    context.setRunner();
    if (context.isValid()) {
      context.pos = contexts.size();
      context.create();
    } else {
      log("PaneContext: open not posssible: %s", file);
    }
  }

  int alreadyOpen(File file) {
    for (PaneContext context : contexts) {
      File folderOrFile = context.file;
      if (context.isBundle(file)) {
        folderOrFile = context.folder;
      }
      if (file.equals(folderOrFile)) {
        return context.pos;
      }
    }
    return -1;
  }

  public File selectFileToOpen() {
    File fileSelected = new SikulixFileChooser(sikulixIDE).open();
    if (fileSelected == null) {
      return null;
    }
    return fileSelected;
  }

  public File selectFileForSave(PaneContext context) {
    File fileSelected = new SikulixFileChooser(sikulixIDE).saveAs(
        context.getExt(), context.isBundle() || context.isTemp());
    if (fileSelected == null) {
      return null;
    }
    return fileSelected;
  }

  boolean checkDirtyPanes() {
    for (PaneContext context : contexts) {
      if (context.isDirty() || (context.isTemp() && context.hasContent())) {
        return true;
      }
    }
    return false;
  }

  List<File> collectPaneFiles() {
    List<File> files = new ArrayList<>();
    for (PaneContext context : contexts) {
      if (context.isTemp()) {
        log("TODO: collectPaneFiles: save temp pane");
        context.notDirty();
      }
      if (context.isDirty()) {
        log("TODO: collectPaneFiles: save dirty pane");
      }
      files.add(context.getFile());
    }
    return files;
  }

  class PaneContext {
    File folder;
    File imageFolder;
    File file;
    String name;
    String ext;

    IScriptRunner runner = null;
    IIDESupport support;
    String type;

    EditorPane pane;
    boolean showThumbs; //TODO
    int pos = 0;

    boolean dirty = false;
    boolean temp = false;

    private PaneContext() {
    }

    public void focus() {
      if (isText()) {
        collapseMessageArea();
      }
      pane.requestFocusInWindow();
    }

    public EditorPane getPane() {
      return pane;
    }

    public boolean isValid() {
      return file.exists() && runner != null;
    }

    public boolean isText() {
      return type.equals(TextRunner.TYPE);
    }

    public boolean isTemp() {
      return temp;
    }

    public boolean hasContent() {
      return !pane.getText().isEmpty();
    }

    public IScriptRunner getRunner() {
      return runner;
    }

    private void setRunner(IScriptRunner _runner) {
      runner = _runner;
      type = runner.getType();
      ext = runner.getDefaultExtension();
      support = IDESupport.get(type);
    }

    private void setRunner() {
      if (ext.isEmpty()) {
        runner = IDESupport.getDefaultRunner();
        ext = runner.getDefaultExtension();
        file = new File(file.getAbsolutePath() + "." + ext);
      } else {
        final IScriptRunner _runner = Runner.getRunner(file.getAbsolutePath());
        if (!(_runner instanceof InvalidRunner)) {
          runner = _runner;
        } else {
          runner = new TextRunner();
        }
      }
      type = runner.getType();
      support = IDESupport.get(type);
    }

    public IIDESupport getSupport() {
      return support;
    }

    public String getType() {
      return type;
    }

    public File getFolder() {
      return folder;
    }

    public File getFile() {
      return file;
    }

    private void setFile() {
      name = tempName + tempIndex++;
      folder = new File(Commons.getIDETemp(), name);
      folder.mkdirs();
      file = new File(folder, name + "." + ext);
      if (file.exists()) {
        file.delete();
      }
      try {
        file.createNewFile();
      } catch (IOException e) {
        fatal("PaneContext: setFile: create not possible: %s", file); //TODO
      }
      imageFolder = folder;
      temp = true;
      log("PaneContext: setFile: %s", file);
    }

    private boolean setFile(File _file) {
      if (_file == null) {
        return false;
      }
      File _folder;
      String path = _file.getAbsolutePath();
      String _ext = FilenameUtils.getExtension(path);
      String _name = FilenameUtils.getBaseName(path);
      if (_file.exists()) {
        if (_file.isDirectory()) {
          _folder = _file;
          _file = Runner.getScriptFile(_folder);
          if (_file != null) {
            _ext = FilenameUtils.getExtension(_file.getPath());
          } else {
            _ext = IDESupport.getDefaultRunner().getDefaultExtension();
            try {
              _file.createNewFile();
            } catch (IOException e) {
              fatal("PaneContext: setFile: create not possible: %s", file); //TODO
              _file = null;
            }
          }
        } else {
          _folder = _file.getParentFile();
        }
      } else {
        if (_ext.isEmpty() || _ext.equals("sikuli")) {
          _file.mkdirs();
          _folder = _file;
          _file = new File(_folder, _name);
          _ext = "";
        } else {
          _folder = _file.getParentFile();
          _folder.mkdirs();
        }
        try {
          _file.createNewFile();
        } catch (IOException e) {
          fatal("PaneContext: setFile: create not possible: %s", file); //TODO
          _file = null;
        }
      }
      if (_file == null) {
        return false;
      }
      file = _file;
      ext = _ext;
      name = _name;
      folder = _folder;
      imageFolder = folder;
      log("PaneContext: setFile: %s", file);
      return true;
    }

    public String getExt() {
      return ext;
    }

    public boolean isBundle() {
      return folder.getAbsolutePath().endsWith(".sikuli");
    }

    public boolean isBundle(File file) {
      return file.getAbsolutePath().endsWith(".sikuli") || FilenameUtils.getExtension(file.getName()).isEmpty();
    }

    public File getImageFolder() {
      return imageFolder;
    }

    public void setImageFolder(File folder) {
      if (folder == null) {
        fatal("PaneContext: setImageFolder: is null (ignored)"); //TODO
        return;
      }
      if (!folder.exists()) {
        folder.mkdirs();
      }
      if (folder.exists()) {
        imageFolder = folder;
        ImagePath.setBundleFolder(imageFolder);
      } else {
        fatal("PaneContext: setImageFolder: create not possible: %s", folder); //TODO
      }
    }

    public File getScreenshotFolder() {
      return new File(imageFolder, ImagePath.SCREENSHOT_DIRECTORY);
    }

    public boolean getShowThumbs() {
      return showThumbs;
    }

    public void setShowThumbs(boolean state) {
      showThumbs = state;
    }

    private void create() {
      int lastPos = -1;
      if (contexts.size() > 0) {
        lastPos = getActiveContext().pos;
      }
      showThumbs = !PreferencesUser.get().getPrefMorePlainText(); //TODO
      pane = new EditorPane(this);
      contexts.add(pos, this);
      tabs.addNewTab(name, pane.getScrollPane(), pos);
      tabs.setSelectedIndex(pos);
      pane.makeReady();
      if (load()) {
        pane.requestFocus();
      } else {
        if (lastPos >= 0) {
          tabs.remove(pos);
          tabs.setSelectedIndex(lastPos);
          contexts.remove(pos);
        } else {
          tabs.remove(0);
          fatal("PaneContext: create: start tab failed"); //TODO possible?
        }
      }
      resetPos();
    }

    public boolean close() {
      boolean success = true;
      if (isDirty()) {
        if (isTemp()) {
          final int answer = SXDialog.askForDecision(sikulixIDE, "Closing Tab", "Tab content not yet saved!",
              "Discard", "Save");
          if (answer == SXDialog.DECISION_CANCEL) {
            return false;
          }
          File fileSaved = null;
          if (answer == SXDialog.DECISION_ACCEPT) {
            fileSaved = selectFileForSave(this);
          }
          if (fileSaved != null) {
            setFile(fileSaved);
          } else {
            success = false;
          }
        }
        if (success) {
          save();
          notDirty();
        }
      }
      tabs.remove(pos);
      contexts.remove(pos);
      pos = -1;
      contextsClosed.add(0, this);
      if (resetPos() == 0 && !ideIsQuitting) {
        createEmptyScriptContext();
      }
      return true;
    }

    private int resetPos() {
      int n = 0;
      for (PaneContext context : contexts) {
        context.pos = n++;
      }
      return n;
    }

    public boolean isDirty() {
      return dirty;
    }

    public void setDirty() {
      if (!dirty) {
        dirty = true;
        SikulixIDE.get().setCurrentFileTabTitleDirty(dirty);
      }
    }

    public void notDirty() {
      if (dirty) {
        dirty = false;
        SikulixIDE.get().setCurrentFileTabTitleDirty(dirty);
      }
    }

    public boolean saveAs() {//TODO
      if (!save()) {
        return false;
      }
      boolean success = true;
      File file;
      while (true) {
        file = selectFileForSave(this);
        if (file == null) {
          return false;
        }
        final int pos = alreadyOpen(file);
        if (pos >= 0) {
          setActiveContext(pos);
          log("PaneContext: alreadyopen: %s", file); //TODO saveAs
          final int answer = SXDialog.askForDecision(sikulixIDE, "Saving Tab", "Tab is currently open!",
              "Overwrite", "Try again");
          if (answer == SXDialog.DECISION_CANCEL) {
            return false;
          }
          if (answer == SXDialog.DECISION_ACCEPT) {
            continue;
          }
          contexts.get(pos).close();
          setActiveContext(this.pos);
        }
        break;
      }
      final PaneContext newContext = new PaneContext();
      if (newContext.setFile(file)) {
        newContext.file.delete();
        try {
          copyContent(this, newContext, isBundle(file));
        } catch (IOException e) {
          success = false;
        }
      }
      if (success) {
        newContext.setRunner();
        newContext.pos = pos;
        newContext.pane = pane;
        newContext.showThumbs = showThumbs;
        contexts.set(pos, newContext);
        contextsClosed.add(0, this);
        tabs.setTitleAt(pos, newContext.name);
        sikulixIDE.setIDETitle(newContext.file.getAbsolutePath());
      }
      return success;
    }

    private void copyContent(PaneContext currentContext, PaneContext newContext, boolean asBundle) throws
        IOException {
      if (asBundle) {
        FileUtils.copyDirectory(currentContext.folder, newContext.folder);
        final String oldName = currentContext.file.getName();
        final String newName = FilenameUtils.getBaseName(newContext.file.getName());
        final String ext = "." + FilenameUtils.getExtension(oldName);
        new File(newContext.folder, oldName).renameTo(new File(newContext.folder, newName + ext));
      } else {
        FileUtils.copyFile(currentContext.file, newContext.file);
      }
    }

    public boolean save() {
      String msg = "";
      boolean success = true;
      try {
        pane.write(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8")));
        notDirty();
      } catch (IOException e) {
        msg = String.format(" did not work: %s", e.getMessage());
        success = false;
      }
      log("PaneContext: save: %s%s", name, msg);
      return success;
    }

    public boolean load() {
      return load(file);
    }

    public boolean load(File file) {
      InputStreamReader isr;
      try {
        isr = new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8"));
        pane.loadContent(isr);
      } catch (Exception ex) {
        log("PaneContext: loadFile: %s ERROR(%s)", file, ex.getMessage());
        return false;
      }
      return true;
    }

    public boolean load(String content) {
      InputStreamReader isr;
      try {
        isr = new InputStreamReader(new ByteArrayInputStream(content.getBytes(Charset.forName("utf-8"))),
            Charset.forName("utf-8"));
        pane.loadContent(isr);
      } catch (Exception ex) {
        log("PaneContext: loadString: ERROR(%s)", ex.getMessage());
        return false;
      }
      return true;
    }
  }

  EditorPane getCurrentCodePane() {
    return getActiveContext().getPane();
  }

  EditorPane getPaneAtIndex(int index) {
    return getContextAt(index).getPane();
  }

  void zzzzzzz() {
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="04 save / restore session">
  private boolean saveSession(int saveAction) {
    StringBuilder sbuf = new StringBuilder();
    for (PaneContext context : contexts.toArray(new PaneContext[]{new PaneContext()})) {
      if (saveAction == DO_SAVE_ALL) {
        if (!context.close()) {
          return false;
        }
      } else {
        if (context.isTemp()) {
          continue;
        }
      }
      if (sbuf.length() > 0) {
        sbuf.append(";");
      }
      sbuf.append(context.getFile());
    }
    PreferencesUser.get().setIdeSession(sbuf.toString());
    return true;
  }

  static final String[] loadScripts = new String[0];

  private List<File> restoreSession() {
    String session_str = prefs.getIdeSession();
    List<File> filesToLoad = new ArrayList<>();
    if (IDEDesktopSupport.filesToOpen != null && IDEDesktopSupport.filesToOpen.size() > 0) {
      for (File f : IDEDesktopSupport.filesToOpen) {
        filesToLoad.add(f);
      }
    }
    if (session_str != null && !session_str.isEmpty()) {
      String[] filenames = session_str.split(";");
      if (filenames.length > 0) {
        log("Restore scripts from last session");
        for (String filename : filenames) {
          if (filename.isEmpty()) {
            continue;
          }
          File fileToLoad = new File(filename);
          File fileToLoadClean = new File(filename.replace("###isText", ""));
          String shortName = fileToLoad.getName();
          if (fileToLoadClean.exists() && !filesToLoad.contains(fileToLoad)) {
            if (shortName.endsWith(".py")) {
              log("Restore Python script: %s", fileToLoad.getName());
            } else if (shortName.endsWith("###isText")) {
              log("Restore Text file: %s", fileToLoad.getName());
            } else {
              log("Restore Sikuli script: %s", fileToLoad);
            }
            filesToLoad.add(fileToLoad);
          }
        }
      }
    }
    //TODO implement load scripts (preload)
    if (loadScripts.length > 0) {
      log("Preload given scripts");
      for (String loadScript : loadScripts) {
        if (loadScript.isEmpty()) {
          continue;
        }
        File f = new File(loadScript);
        if (f.exists() && !filesToLoad.contains(f)) {
          if (f.getName().endsWith(".py")) {
            Debug.info("Python script: %s", f.getName());
          } else {
            log("Sikuli script: %s", f);
          }
        }
      }
    }
    return filesToLoad;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="06 tabs handling">
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
  //</editor-fold>

  //<editor-fold desc="07 menu helpers">
  public void showPreferencesWindow() {
    PreferencesWin pwin = new PreferencesWin();
    pwin.setAlwaysOnTop(true);
    pwin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    pwin.setVisible(true);
  }

  void openSpecial() {
    log("Open Special requested");
//    Map<String, String> specialFiles = new Hashtable<>();
//    specialFiles.put("1 SikuliX Global Options", Commons.getOptions().getOptionsFile());
//    File extensionsFile = ExtensionManager.getExtensionsFile();
//    specialFiles.put("2 SikuliX Extensions Options", extensionsFile.getAbsolutePath());
//    File sitesTxt = ExtensionManager.getSitesTxt();
//    specialFiles.put("3 SikuliX Additional Sites", sitesTxt.getAbsolutePath());
//    String[] defaults = new String[specialFiles.size()];
//    defaults[0] = Options.getOptionsFileDefault();
//    defaults[1] = ExtensionManager.getExtensionsFileDefault();
//    defaults[2] = ExtensionManager.getSitesTxtDefault();
//    String msg = "";
//    int num = 1;
//    String[] files = new String[specialFiles.size()];
//    for (String specialFile : specialFiles.keySet()) {
//      files[num - 1] = specialFiles.get(specialFile).trim();
//      msg += specialFile + "\n";
//      num++;
//    }
//    msg += "\n" + "Enter a number to select a file";
//    String answer = SX.input(msg, "Edit a special SikuliX file", false, 10);
//    if (null != answer && !answer.isEmpty()) {
//      try {
//        num = Integer.parseInt(answer.substring(0, 1));
//        if (num > 0 && num <= specialFiles.size()) {
//          String file = files[num - 1];
//          if (!new File(file).exists()) {
//            FileManager.writeStringToFile(defaults[num - 1], file);
//          }
//          log( "Open Special: should load: %s", file);
//          newTabWithContent(file);
//        }
//      } catch (NumberFormatException e) {
//      }
//    }
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
      log("Problem when initializing menues\nError: %s", e.getMessage());
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
        log("Can't find menu action: " + cnfe);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (actMethod != null) {
        try {
          log("MenuAction." + action);
          Object[] params = new Object[1];
          params[0] = e;
          actMethod.invoke(this, params);
        } catch (Exception ex) {
          log("Problem when trying to invoke menu action %s\nError: %s",
              action, ex.getMessage());
        }
      }
    }
  }

  private static JMenu recentMenu = null;
  private static Map<String, String> recentProjects = new HashMap<>();
  private static java.util.List<String> recentProjectsMenu = new ArrayList<>();
  private static int recentMax = 10;
  private static int recentMaxMax = recentMax + 10;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="11 Init FileMenu">
  JMenu getFileMenu() {
    return _fileMenu;
  }

  //<editor-fold desc="menu">
  private void initFileMenu() throws NoSuchMethodException {
    _fileMenu = new JMenu(_I("menuFile"));
    JMenuItem jmi;
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    _fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);

    if (IDEDesktopSupport.showAbout) {
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

    jmi = _fileMenu.add(createMenuItem(_I("menuFileSave"),
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, scMask),
        new FileAction(FileAction.SAVE)));
    jmi.setName("SAVE");

    jmi = _fileMenu.add(createMenuItem(_I("menuFileSaveAs"),
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
            InputEvent.SHIFT_DOWN_MASK | scMask),
        new FileAction(FileAction.SAVE_AS)));
    jmi.setName("SAVE_AS");

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

    _fileMenu.add(createMenuItem("Export as runnable jar",
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J,
            InputEvent.SHIFT_DOWN_MASK | scMask),
        new FileAction(FileAction.ASRUNJAR)));

    jmi = _fileMenu.add(createMenuItem(_I("menuFileCloseTab"),
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, scMask),
        new FileAction(FileAction.CLOSE_TAB)));
    jmi.setName("CLOSE_TAB");

    if (IDEDesktopSupport.showPrefs) {
      _fileMenu.addSeparator();
      _fileMenu.add(createMenuItem(_I("menuFilePreferences"),
          KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, scMask),
          new FileAction(FileAction.PREFERENCES)));
    }

    _fileMenu.addSeparator();
    _fileMenu.add(createMenuItem("Open Special Files",
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK | scMask),
        new FileAction(FileAction.OPEN_SPECIAL)));

//TODO restart IDE
/*
    _fileMenu.add(createMenuItem("Restart IDE",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
                    InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
            new FileAction(FileAction.RESTART)));
*/
    if (IDEDesktopSupport.showQuit) {
      _fileMenu.addSeparator();
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
    static final String OPEN = "doOpen";
    static final String RECENT = "doRecent";
    static final String SAVE = "doSave";
    static final String SAVE_AS = "doSaveAs";
    static final String SAVE_ALL = "doSaveAll";
    static final String EXPORT = "doExport";
    static final String ASJAR = "doAsJar";
    static final String ASRUNJAR = "doAsRunJar";
    static final String CLOSE_TAB = "doCloseTab";
    static final String PREFERENCES = "doPreferences";
    static final String QUIT = "doQuit";
    static final String ENTRY = "doRecent";
    static final String OPEN_SPECIAL = "doOpenSpecial";

    FileAction(String item) throws NoSuchMethodException {
      super(item);
    }

    FileAction(int tabIndex) {
      super();
      targetTab = tabIndex;
    }

    private int targetTab = -1;

    public void doNew(ActionEvent ae) {
      createEmptyScriptContext();
    }

    public void doOpen(ActionEvent ae) {
      final File file = selectFileToOpen();
      createFileContext(file);
    }

    public void doRecent(ActionEvent ae) { //TODO
      log(ae.getActionCommand());
    }

    class OpenRecent extends MenuAction {

      OpenRecent() {
        super();
      }

      void openRecent(ActionEvent ae) {
        log(ae.getActionCommand());
      }
    }

    public void doSave(ActionEvent ae) {
      getActiveContext().save();
    }

    public void doSaveAs(ActionEvent ae) {
      getActiveContext().saveAs();
    }

    public void doSaveAll(ActionEvent ae) {
      log("doSaveAll requested");
      if (!checkDirtyPanes()) {
        return;
      }
      saveSession(DO_SAVE_ALL);
    }

    public void doExport(ActionEvent ae) {
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log("doExport requested: %s", orgName);
      String fname = null;
      try {
        fname = codePane.exportAsZip();
      } catch (Exception ex) {
        log("Problem when trying to save %s\nError: %s",
            fname, ex.getMessage());
      }
    }

    public void doAsJar(ActionEvent ae) {
      EditorPane codePane = getCurrentCodePane();
      String orgName = codePane.getCurrentShortFilename();
      log("doAsJar requested: %s", orgName);
      if (codePane.isDirty()) {
        Sikulix.popError("Please save script before!", "Export as jar");
      } else {
        File fScript = codePane.saveAndGetCurrentFile();
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
      log("doAsRunJar requested: %s", orgName);
      if (codePane.isDirty()) {
        Sikulix.popError("Please save script before!", "Export as runnable jar");
      } else {
        File fScript = codePane.saveAndGetCurrentFile();
        List<String> options = new ArrayList<>();
        options.add(fScript.getParentFile().getAbsolutePath());
        String fpJar = FileManager.makeScriptjar(options);
        if (null != fpJar) {
          Sikulix.popup(fpJar, "Export as runnable jar ...");
        } else {
          Sikulix.popError("did not work for: " + orgName, "Export as runnable jar");
        }
      }
    }

    public void doCloseTab(ActionEvent ae) {
      getActiveContext().close();
    }

    public void doAbout(ActionEvent ae) {
      new SXDialog("sxideabout", SikulixIDE.getWindowTop(), SXDialog.POSITION.TOP).run();
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

    public void doQuit(ActionEvent ae) {
      log("Quit requested");
      terminate();
      log("Quit: cancelled or did not work");
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="12 Init EditMenu">
  private String findText = "";

  private void initEditMenu() throws NoSuchMethodException {
    int scMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

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
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, scMask | InputEvent.SHIFT_DOWN_MASK),
        new EditAction(EditAction.COPY)));
    _editMenu.add(createMenuItem(_I("menuEditCut"),
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, scMask),
        new EditAction(EditAction.CUT)));
    _editMenu.add(createMenuItem("Cut line",
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, scMask | InputEvent.SHIFT_DOWN_MASK),
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
      log("find \"" + str + "\" at " + begin + ", found: " + pos);
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
//      Debug.log( "search failed: " + failed);
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
      final PaneContext activePane = getActiveContext();
      EditorPane pane = activePane.getPane();
      if (pane != null) {
        final UndoManager manager = pane.getUndoRedo().getUndoManager();
        if (manager.canUndo()) {
          setEnabled(true);
        }
      } else {
        setEnabled(false);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final PaneContext activePane = getActiveContext();
      EditorPane pane = activePane.getPane();
      if (pane != null) {
        try {
          pane.getUndoRedo().getUndoManager().undo();
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
      final PaneContext activePane = getActiveContext();
      EditorPane pane = activePane.getPane();
      if (pane != null) {
        if (pane.getUndoRedo().getUndoManager().canRedo()) {
          setEnabled(true);
        }
      } else {
        setEnabled(false);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final PaneContext activePane = getActiveContext();
      EditorPane pane = activePane.getPane();
      if (pane != null) {
        try {
          pane.getUndoRedo().getUndoManager().redo();
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
        KeyStroke.getKeyStroke(KeyEvent.VK_R,
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
      btnRun.runCurrentScript();
    }

    public void runShowActions(ActionEvent ae) {
      btnRunSlow.runCurrentScript();
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
      if (Commons.runningMac()) {
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
      getCurrentCodePane().context.setShowThumbs(showThumbsState);
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

    _toolMenu.add(createMenuItem("Pack Jar with Jython",
        null,
        new ToolAction(ToolAction.JARWITHJYTHON)));

    _toolMenu.add(createMenuItem(_I("menuToolAndroid"),
        null,
        new ToolAction(ToolAction.ANDROID)));
  }

  class ToolAction extends MenuAction {

    static final String EXTENSIONS = "extensions";
    static final String JARWITHJYTHON = "jarWithJython";
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

    public void jarWithJython(ActionEvent ae) {
      if (SX.popAsk("*** You should know what you are doing! ***\n\n" +
          "This may take a while. Wait for success popup!" +
          "\nClick Yes to start.", "Creating jar file")) {
        (new Thread() {
          @Override
          public void run() {
            makeJarWithJython();
          }
        }).start();
      }
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

  private void makeJarWithJython() {
    String ideJarName = getRunningJar(SikulixIDE.class);
    if (ideJarName.isEmpty()) {
      log("makeJarWithJython: JAR containing IDE not available");
      return;
    }
    if (ideJarName.endsWith("/classes/")) {
      String version = Commons.getSXVersionShort();
      String name = "sikulixide-" + version + "-complete.jar";
      ideJarName = new File(new File(ideJarName).getParentFile(), name).getAbsolutePath();
    }
    String jythonJarName = "";
    try {
      jythonJarName = getRunningJar(Class.forName("org.python.util.jython"));
    } catch (ClassNotFoundException e) {
      log("makeJarWithJython: Jar containing Jython not available");
      return;
    }
    String targetJar = new File(Commons.getAppDataStore(), "sikulixjython.jar").getAbsolutePath();
    String[] jars = new String[]{ideJarName, jythonJarName};
    SikulixIDE.getStatusbar().setMessage(String.format("Creating SikuliX with Jython: %s", targetJar));
    if (FileManager.buildJar(targetJar, jars, null, null, null)) {
      String msg = String.format("Created SikuliX with Jython: %s", targetJar);
      log(msg);
      SX.popup(msg.replace(": ", "\n"));
    } else {
      String msg = String.format("Create SikuliX with Jython not possible: %s", targetJar);
      log(msg);
      SX.popError(msg.replace(": ", "\n"));
    }
    SikulixIDE.getStatusbar().resetMessage();
  }

  private String getRunningJar(Class clazz) {
    String jarName = "";
    CodeSource codeSrc = clazz.getProtectionDomain().getCodeSource();
    if (codeSrc != null && codeSrc.getLocation() != null) {
      try {
        jarName = codeSrc.getLocation().getPath();
        jarName = URLDecoder.decode(jarName, "utf8");
      } catch (UnsupportedEncodingException e) {
        log("URLDecoder: not possible: " + jarName);
        jarName = "";
      }
    }
    return jarName;
  }

  private void androidSupport() {
//    final ADBScreen aScr = new ADBScreen();
//    String title = "Android Support - !!EXPERIMENTAL!!";
//    if (aScr.isValid()) {
//      String warn = "Device found: " + aScr.getDeviceDescription() + "\n\n" +
//          "click Check: a short test is run with the device\n" +
//          "click Default...: set device as default screen for capture\n" +
//          "click Cancel: capture is reset to local screen\n" +
//          "\nBE PREPARED: Feature is experimental - no guarantee ;-)";
//      String[] options = new String[3];
//      options[WARNING_DO_NOTHING] = "Check";
//      options[WARNING_ACCEPTED] = "Default Android";
//      options[WARNING_CANCEL] = "Cancel";
//      int ret = JOptionPane.showOptionDialog(this, warn, title, 0, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
//      if (ret == WARNING_CANCEL || ret == JOptionPane.CLOSED_OPTION) {
//        defaultScreen = null;
//        return;
//      }
//      if (ret == WARNING_DO_NOTHING) {
//        SikulixIDE.hideIDE();
//        Thread test = new Thread() {
//          @Override
//          public void run() {
//            androidSupportTest(aScr);
//          }
//        };
//        test.start();
//      } else if (ret == WARNING_ACCEPTED) {
//        defaultScreen = aScr;
//        return;
//      }
//    } else if (!ADBClient.isAdbAvailable) {
//      Sikulix.popError("Package adb seems not to be available.\nIt must be installed for Android support.", title);
//    } else {
//      Sikulix.popError("No android device attached", title);
//    }
  }

  private void androidSupportTest(IScreen aScr) {
//    ADBTest.ideTest(aScr);
//    ADBScreen.stop();
    SikulixIDE.doShow();
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

//    _helpMenu.addSeparator();
//    _helpMenu.add(createMenuItem("SikuliX1 Downloads",
//        null, new HelpAction(HelpAction.OPEN_DOWNLOADS)));
//    _helpMenu.add(createMenuItem(_I("menuHelpCheckUpdate"),
//        null, new HelpAction(HelpAction.CHECK_UPDATE)));
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
      String actualBuild = Commons.getSxBuildStamp();
      try {
        long lb = Long.parseLong(latestBuild);
        long ab = Long.parseLong(actualBuild);
        if (lb > ab) {
          newBuildAvailable = true;
          newBuildStamp = latestBuildFull;
        }
        log("latest build: %s this build: %s (newer: %s)", latestBuild, actualBuild, newBuildAvailable);
      } catch (NumberFormatException e) {
        log("check for new build: stamps not readable");
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
      String si = Commons.getSystemInfo();
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
            Commons.getSXVersionIDE(), msgType);
      }
    }

    boolean checkUpdate(boolean isAutoCheck) {
      String ver = "";
      String details;
      AutoUpdater au = new AutoUpdater();
      PreferencesUser pref = PreferencesUser.get();
      log("being asked to check update");
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
  private ButtonCapture btnCapture;
  private ButtonRun btnRun;
  private ButtonRunViz btnRunSlow;
  private ButtonRecord btnRecord;

  private JToolBar initToolbar() {
    JToolBar toolbar = new JToolBar();
    JButton btnInsertImage = new ButtonInsertImage();
    JButton btnSubregion = new ButtonSubregion().init();
    JButton btnLocation = new ButtonLocation().init();
    JButton btnOffset = new ButtonOffset().init();
//TODO ButtonShow/ButtonShowIn
/*
    JButton btnShow = new ButtonShow().init();
    JButton btnShowIn = new ButtonShowIn().init();
*/
    btnCapture = new ButtonCapture();
    toolbar.add(btnCapture);
    toolbar.add(btnInsertImage);
    toolbar.add(btnSubregion);
    toolbar.add(btnLocation);
    toolbar.add(btnOffset);
/*
    toolbar.add(btnShow);
    toolbar.add(btnShowIn);
*/
    toolbar.add(Box.createHorizontalGlue());
    btnRun = new ButtonRun();
    toolbar.add(btnRun);
    btnRunSlow = new ButtonRunViz();
    toolbar.add(btnRunSlow);
    toolbar.add(Box.createHorizontalGlue());

//    JComponent jcSearchField = createSearchField();
//    toolbar.add(jcSearchField);

    toolbar.add(Box.createRigidArea(new Dimension(7, 0)));
    toolbar.setFloatable(false);
    //toolbar.setMargin(new Insets(0, 0, 0, 5));

    btnRecord = new ButtonRecord();
    toolbar.add(btnRecord);

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
      File file = new SikulixFileChooser(ideWindow).loadImage();
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
        ideWindow.setVisible(false);
        RunTime.pause(0.5f);
        Screen.doPrompt(promptText, this);
      } else {
        nothingTodo();
      }
    }

    void nothingTodo() {
    }

    boolean shouldRun() {
      log("TRACE: ButtonSubRegion triggered");
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
        ideWindow.setVisible(false);
        if (codePane.context.getShowThumbs()) {
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
      final String item = codePane.parseLineText(line);
      if (!item.isEmpty()) {
//TODO ButtonShow action performed
        SikulixIDE.doHide();
        new Thread(new Runnable() {
          @Override
          public void run() {
            String eval = "";
            eval = item.replaceAll("\"", "\\\"");
            if (item.startsWith("Region")) {
              if (item.contains(".asOffset()")) {
                eval = item.replace(".asOffset()", "");
              }
              eval = "Region.create" + eval.substring(6) + ".highlight(2);";
            } else if (item.startsWith("Location")) {
              eval = "new " + item + ".grow(10).highlight(2);";
            } else if (item.startsWith("Pattern")) {
              eval = "m = Screen.all().exists(new " + item + ", 0);";
              eval += "if (m != null) m.highlight(2);";
            } else if (item.startsWith("\"")) {
              eval = "m = Screen.all().exists(" + item + ", 0); ";
              eval += "if (m != null) m.highlight(2);";
            }
            log("ButtonShow:\n%s", eval);
            SikulixIDE.doShow();
          }
        }).start();
        return;
      }
      Sikulix.popup("ButtonShow: Nothing to show!" +
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
      log("TRACE: ButtonShowIn triggered");
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
      Sikulix.popup("ButtonShowIn: Nothing to show!" +
          "\nThe line with the cursor should contain:" +
          "\n- an image file name or" +
          "\n- a Pattern with an image file name");
    }

    @Override
    public void captureComplete(ScreenImage simg) {
      if (simg != null) {
        Region reg = new Region(simg.getROI());
        String itemReg = String.format("new Region(%d, %d, %d, %d)", reg.x, reg.y, reg.w, reg.h);
        item = item.replace("#region#", itemReg);
        final String evalText = item;
        new Thread(new Runnable() {
          @Override
          public void run() {
            log("ButtonShowIn:\n%s", evalText);
            //TODO ButtonShowIn perform show
          }
        }).start();
        RunTime.pause(2.0f);
      } else {
        SikulixIDE.showAgain();
        nothingTodo();
      }
    }
  }

  class ButtonRecord extends ButtonOnToolbar implements ActionListener {

    private Recorder recorder = new Recorder();

    ButtonRecord() {
      super();

      URL imageURL = SikulixIDE.class.getResource("/icons/record.png");
      setIcon(new ImageIcon(imageURL));
      initTooltip();
      addActionListener(this);
      setText(_I("btnRecordLabel"));
      // setMaximumSize(new Dimension(45,45));
    }

    private void initTooltip() {
      PreferencesUser pref = PreferencesUser.get();
      String strHotkey = Key.convertKeyToText(pref.getStopHotkey(), pref.getStopHotkeyModifiers());
      String stopHint = _I("btnRecordStopHint", strHotkey);
      setToolTipText(_I("btnRecord", stopHint));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      ideWindow.setVisible(false);
      recorder.start();
    }

    public void stopRecord() {
      SikulixIDE.showAgain();

      if (isRunning()) {
        EditorPane pane = getCurrentCodePane();
        ICodeGenerator generator = pane.getCodeGenerator();

        ProgressMonitor progress = new ProgressMonitor(pane, _I("processingWorkflow"), "", 0, 0);
        progress.setMillisToDecideToPopup(0);
        progress.setMillisToPopup(0);

        new Thread(() -> {
          try {
            List<IRecordedAction> actions = recorder.stop(progress);

            if (!actions.isEmpty()) {
              List<String> actionStrings = actions.stream().map((a) -> a.generate(generator)).collect(Collectors.toList());

              EventQueue.invokeLater(() -> {
                pane.insertString("\n" + String.join("\n", actionStrings) + "\n");
                pane.doReparse();
              });
            }
          } finally {
            progress.close();
          }
        }).start();
      }
    }

    public boolean isRunning() {
      return recorder.isRunning();
    }
  }
//</editor-fold>

  //<editor-fold desc="21 Init Run Buttons">
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
      runCurrentScript();
    }

    void runCurrentScript() {
      log("************** before RunScript"); //TODO
      //doBeforeQuitOrRun();

      EditorPane editorPane = getCurrentCodePane();
      if (editorPane.getDocument().getLength() == 0) {
        log("Run script not possible: Script is empty");
        return;
      }
      File scriptFile = editorPane.getCurrentFile();
      if (editorPane.isDirty()) {
        if (editorPane.isTemp()) {
          scriptFile = editorPane.getCurrentFile();
        } else {
          scriptFile = FileManager.createTempFile(editorPane.getRunner().getDefaultExtension());
        }
        if (scriptFile != null) {
          try {
            editorPane.write(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scriptFile), "UTF8")));
          } catch (Exception ex) {
            scriptFile = null;
          }
        }
        if (scriptFile == null) {
          log("Run Script: not yet saved: temp file for running not available");
          return;
        }
      }
      final File scriptFileToRun = scriptFile;

      new Thread(new Runnable() {
        @Override
        public void run() {
          synchronized (ideIsRunningScript) {
            if (isRunningScript()) {
              log("Run Script: not possible: already running another script");
              return;
            }
            if (System.out.checkError()) {
              boolean shouldContinue = Sikulix.popAsk("System.out is broken (console output)!"
                  + "\nYou will not see any messages anymore!"
                  + "\nSave your work and restart the IDE!"
                  + "\nYou may ignore this on your own risk!" +
                  "\nYes: continue  ---  No: back to IDE", "Fatal Error");
              if (!shouldContinue) {
                log("Run script aborted: System.out is broken (console output)");
                return;
              }
              log("Run script continued, though System.out is broken (console output)");
            }
            sikulixIDE.setIsRunningScript(true);
          }

          SikulixIDE.getStatusbar().resetMessage();
          SikulixIDE.doHide();
          RunTime.pause(0.1f);
          messages.clear();
          resetErrorMark();
          doBeforeRun();

          IScriptRunner.Options runOptions = new IScriptRunner.Options();
          runOptions.setRunningInIDE();

          int exitValue = -1;
          try {
            IScriptRunner runner = editorPane.editorPaneRunner;
            setCurrentRunner(runner);
            setCurrentScript(scriptFileToRun);
            //TODO make reloadImported specific for each editor tab
            if (runner.getType().equals(JythonRunner.TYPE)) {
              JythonSupport.get().reloadImported();
            }
            exitValue = runner.runScript(scriptFileToRun.getAbsolutePath(), Commons.getUserArgs(), runOptions);
          } catch (Exception e) {
            log("Run Script: internal error:");
            e.printStackTrace();
          } finally {
            setCurrentRunner(null);
            setCurrentScript(null);
            Runner.setLastScriptRunReturnCode(exitValue);
          }

          log("************** after RunScript");
          addErrorMark(runOptions.getErrorLine());
          if (Image.getIDEshouldReload()) {
            EditorPane pane = getCurrentCodePane();
            int line = pane.getLineNumberAtCaret(pane.getCaretPosition());
            getCurrentCodePane().doReparse();
            getCurrentCodePane().jumpTo(line);
          }

          RunTime.cleanUpAfterScript();
          SikulixIDE.showAgain();

          synchronized (ideIsRunningScript) {
            setIsRunningScript(false);
          }

        }
      }).start();
    }

    void doBeforeRun() {
      Settings.ActionLogs = prefs.getPrefMoreLogActions();
      Settings.DebugLogs = prefs.getPrefMoreLogDebug();
      Settings.InfoLogs = prefs.getPrefMoreLogInfo();
      Settings.Highlight = prefs.getPrefMoreHighlight();
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
    protected void doBeforeRun() {
      super.doBeforeRun();
      Settings.setShowActions(true);
    }

  }

  synchronized boolean isRunningScript() {
    return ideIsRunningScript;
  }

  synchronized void setIsRunningScript(boolean state) {
    ideIsRunningScript = state;
  }

  public Boolean ideIsRunningScript = false;

  public IScriptRunner getCurrentRunner() {
    return currentRunner;
  }

  public void setCurrentRunner(IScriptRunner currentRunner) {
    this.currentRunner = currentRunner;
  }

  private IScriptRunner currentRunner = null;

  public File getCurrentScript() {
    return currentScript;
  }

  public void setCurrentScript(File currentScript) {
    this.currentScript = currentScript;
  }

  private File currentScript = null;

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
  //</editor-fold>

  //<editor-fold desc="30 MsgArea">
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
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="25 Init ShortCuts HotKeys">
  void removeCaptureHotkey() {
    HotkeyManager.getInstance().removeHotkey("Capture");
  }

  void installCaptureHotkey() {
    HotkeyManager.getInstance().addHotkey("Capture", new HotkeyListener() {
      @Override
      public void hotkeyPressed(HotkeyEvent e) {
        if (!isRunningScript()) {
          btnCapture.capture(0);
        }
      }
    });
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
    log("AbortKey was pressed: aborting all running scripts");
    Runner.abortAll();
    EventQueue.invokeLater(() -> {
      btnRecord.stopRecord();
    });
  }
  //</editor-fold>
}
