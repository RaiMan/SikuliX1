/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.*;
import org.sikuli.support.FileManager;
import org.sikuli.support.ide.*;
import org.sikuli.support.ide.syntaxhighlight.ResolutionException;
import org.sikuli.support.ide.syntaxhighlight.grammar.Lexer;
import org.sikuli.support.ide.syntaxhighlight.grammar.Token;
import org.sikuli.support.ide.syntaxhighlight.grammar.TokenType;
import org.sikuli.script.Image;
import org.sikuli.script.Sikulix;
import org.sikuli.script.*;
import org.sikuli.support.runner.IRunner;
import org.sikuli.support.ide.Runner;
import org.sikuli.support.runner.InvalidRunner;
import org.sikuli.support.runner.JythonRunner;
import org.sikuli.support.runner.TextRunner;
import org.sikuli.support.Commons;
import org.sikuli.support.devices.IScreen;
import org.sikuli.support.recorder.Recorder;
import org.sikuli.support.RunTime;
import org.sikuli.support.devices.ScreenDevice;
import org.sikuli.support.recorder.generators.ICodeGenerator;
import org.sikuli.support.gui.SXDialog;
import org.sikuli.support.recorder.actions.IRecordedAction;
import org.sikuli.util.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

  private static void todo(String message, Object... args) {
    Debug.logx(-1, me + "TODO: " + message, args);
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

  public static void  doHide() {
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
  private static AtomicBoolean ideIsReady = new AtomicBoolean(false);

  protected static void start() {

    ideWindowRect = getWindowRect();

    IDEDesktopSupport.init();
    IDESupport.initIDESupport();
    if (!Commons.hasOption(CommandArgsEnum.CONSOLE)) {
      get().messages = new EditorConsolePane();
    }
    IDESupport.initRunners();
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
    if (messages != null) {
      initMessageArea();
    }
    Debug.log("IDE: creating combined work window");
    JPanel codePane = new JPanel(new BorderLayout(10, 10));
    codePane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    codePane.add(tabs, BorderLayout.CENTER);

    Debug.log("IDE: Putting all together");
    JPanel editPane = new JPanel(new BorderLayout(0, 0));
    mainPane = null;
    if (messageArea != null) {
      if (prefs.getPrefMoreMessage() == PreferencesUser.VERTICAL) {
        mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codePane, messageArea);
      } else {
        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codePane, messageArea);
      }
      mainPane.setResizeWeight(0.6);
      mainPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
      editPane.add(mainPane, BorderLayout.CENTER);
    } else {
      editPane.add(codePane, BorderLayout.CENTER);
    }

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
    if (messages != null) {
      messages.initRedirect();
    }
    Debug.log("IDE: Putting all together - Restore last Session");
    restoreSession();

    initShortcutKeys();
    ideIsReady.set(true);
    Commons.startLog(3, "IDE ready: on Java %d (%4.1f sec)", Commons.getJavaVersion(), Commons.getSinceStart());
  }

  public static void showAfterStart() {
    while (!ideIsReady.get()) {
      RunTime.pause(100);
    }
    org.sikuli.ide.Sikulix.stopSplash();
    ideWindow.setVisible(true);
    if (sikulixIDE.mainPane != null) {
      sikulixIDE.mainPane.setDividerLocation(0.6); //TODO saved value
    }
    sikulixIDE.getActiveContext().focus();
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
    PaneContext context = contexts.get(pos);
    showContext(context);
    return context;
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
    showContext(context);
  }

  private void showContext(PaneContext context) {
    setIDETitle(context.getFile().getAbsolutePath());
    ImagePath.setBundleFolder(context.getFolder());

    getStatusbar().setType(context.getType());

    chkShowThumbs.setState(getActiveContext().getShowThumbs());

    final EditorPane editorPane = context.getPane();
    int dot = editorPane.getCaret().getDot();
    editorPane.setCaretPosition(dot);
    updateUndoRedoStates();

    if (context.isText()) {
      collapseMessageArea();
    } else {
      uncollapseMessageArea();
    }
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
    if (!context.isValid()) {
      log("PaneContext: open not posssible: %s", file);
      return;
    }
    context.pos = contexts.size();
    context.create();
    context.doShowThumbs();
    context.notDirty();
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

    IRunner runner = null;
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

    public IRunner getRunner() {
      return runner;
    }

    private void setRunner(IRunner _runner) {
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
        final IRunner _runner = Runner.getRunner(file.getAbsolutePath());
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

    public String getFileName() {
      return name + "." + ext;
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

    public File getScreenshotFile(String name) {
      if (!FilenameUtils.getExtension(name).equals("png")) {
        name += ".png";
      }
      File shot = new File(name);
      if (!shot.isAbsolute()) {
        shot = new File(getScreenshotFolder(), name);
      }
      if (shot.exists()) {
        return shot;
      }
      return null;
    }

    public File getScreenshotFile(File shot) {
      if (shot.exists()) {
        return shot;
      }
      return null;
    }

    public boolean getShowThumbs() {
      return showThumbs;
    }

    public void setShowThumbs(boolean state) {
      showThumbs = state;
    }

    int getLineStart(int lineNumber) {
      try {
        return pane.getLineStartOffset(lineNumber);
      } catch (BadLocationException e) {
        return -1;
      }
    }

    private void create() {
      int lastPos = -1;
      if (contexts.size() > 0) {
        lastPos = getActiveContext().pos;
      }
      showThumbs = !PreferencesUser.get().getPrefMorePlainText();
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
      return doClose(false);
    }

    public boolean closeSilent() {
      return doClose(true);
    }

    boolean doClose(boolean discard) {
      boolean shouldSave = true;
      if (isDirty() || (isTemp() && !discard)) {
        if (isTemp()) {
          String msg = String.format("%s: content not yet saved!", getFileName());
          final int answer = SXDialog.askForDecision(sikulixIDE, "Closing Tab", msg,
                  "Discard", "Save");
          if (answer == SXDialog.DECISION_CANCEL) {
            return false;
          }
          if (answer == SXDialog.DECISION_ACCEPT) {
            File fileSaved = selectFileForSave(this);
            if (fileSaved != null) {
              setFile(fileSaved);
            } else {
              return false;
            }
          } else {
            shouldSave = false;
          }
        }
        if (shouldSave && !discard) {
          cleanBundle(); //TODO
          save();
        }
        notDirty();
      }
      int closedPos = pos;
      tabs.remove(pos);
      contexts.remove(pos);
      pos = -1;
      contextsClosed.add(0, this);
      if (ideIsQuitting) {
        resetPos();
        if (contexts.size() > 0) {
          setActiveContext(contexts.size() - 1);
        }
      } else {
        if (resetPos() == 0) {
          createEmptyScriptContext();
        } else {
          setActiveContext(Math.min(closedPos, contexts.size() - 1));
        }
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

    private void cleanBundle() { //TODO consolidate with FileManager and Settings
      log("cleanBundle: %s", getName());
      String[] text = pane.getText().split("\n");
      List<Map<String, Object>> images = collectImages(text);
      Set<String> foundImages = new HashSet<>();
      for (Map<String, Object> image : images) {
        File imgFile = (File) image.get(IButton.FILE);
        String name = imgFile.getName();
        foundImages.add(name);
      }
      deleteNotUsedImages(getImageFolder(), foundImages);
      deleteNotUsedScreenshots(getScreenshotFolder(), foundImages);
      log("cleanBundle finished: %s", getName());
    }

    public void deleteNotUsedImages(File scriptFolder, Set<String> usedImages) {
      if (!scriptFolder.isDirectory()) {
        return;
      }
      File[] files = scriptFolder.listFiles((dir, name) -> {
        if ((name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
          if (!name.startsWith("_")) {
            return true;
          }
        }
        return false;
      });
      if (files == null || files.length == 0) {
        return;
      }
      for (File image : files) {
        if (!usedImages.contains(image.getName())) {
          image.delete(); //TODO make a backup??
        }
      }
    }

    public void deleteNotUsedScreenshots(File screenshotsDir, Set<String> usedImages) {
      if (screenshotsDir.exists()) {
        File[] files = screenshotsDir.listFiles();
        if (files == null || files.length == 0) {
          return;
        }
        for (File screenshot : files) {
          if (!usedImages.contains(screenshot.getName())) {
            screenshot.delete();
          }
        }
      }
    }

    public boolean isDirty() {
      return dirty;
    }

    public void setDirty() {
      if (!dirty) {
        dirty = true;
        setFileTabTitleDirty(pos, dirty);
      }
    }

    public void notDirty() {
      if (dirty) {
        dirty = false;
        setFileTabTitleDirty(pos, dirty);
      }
    }

    void setFileTabTitleDirty(int pos, boolean isDirty) {
      String title = tabs.getTitleAt(pos);
      if (!isDirty && title.startsWith("*")) {
        title = title.substring(1);
        tabs.setTitleAt(pos, title);
      } else if (isDirty && !title.startsWith("*")) {
        title = "*" + title;
        tabs.setTitleAt(pos, title);
      }
    }

    public boolean saveAs() {
      boolean shouldSave = true;
      if (!isTemp() && isDirty()) {
        todo("PaneContext: saveAs: ask: discard or save changes"); //TODO
        String msg = String.format("%s: save changes?", file.getName());
        final int answer = SXDialog.askForDecision(sikulixIDE, "Saving Tab", msg,
                "Do not save", "Save");
        if (answer == SXDialog.DECISION_CANCEL) {
          return false;
        }
        if (answer == SXDialog.DECISION_IGNORE) {
          shouldSave = false;
          notDirty();
        }
      }
      if (shouldSave) {
        if (!save()) {
          return false;
        }
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
          log("PaneContext: alreadyopen: %s", file);
          String msg = String.format("%s: is currently open!", file.getName());
          final int answer = SXDialog.askForDecision(sikulixIDE, "Saving Tab", msg,
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

    void reparse() {
      pane.saveCaretPosition();
      if (getShowThumbs()) {
        doShowThumbs();
      } else {
        resetContentToText();
      }
      pane.restoreCaretPosition();
    }

    private void doShowThumbs() {
      if (getShowThumbs()) {
        String[] text = pane.getText().split("\n");
        List<Map<String, Object>> images = collectImages(text);
        List<Map<String, Object>> patterns = patternMatcher(images, text);
        if (images.size() > 0 || patterns.size() > 0) {
          for (Map<String, Object> item : images) {
            final File imgFile = (File) item.get(IButton.FILE);
            //TODO make it optional? _image as thumb
            if (imgFile.getName().startsWith("_")) {
              continue;
            }
            final EditorImageButton button = new EditorImageButton(item);
            int itemStart = getLineStart((Integer) item.get(IButton.LINE)) + (Integer) item.get(IButton.LOFF);
            int itemEnd = itemStart + ((String) item.get(IButton.TEXT)).length();
            pane.select(itemStart, itemEnd);
            pane.insertComponent(button);
          }
          pane.setCaretPosition(0);
        }
        log("ImageButtons: images(%d) patterns(%d)", images.size(), patterns.size());
      }
    }

    private List<Map<String, Object>> collectImages(String[] text) {
      List<Map<String, Object>> images = new ArrayList<>();
      if (text.length > 0) {
        images = imageMatcher(images, text, Pattern.compile(".*?(\".*?\")"));
        images = imageMatcher(images, text, Pattern.compile(".*?('.*?')"));
      }
      return images;
    }

    private List<Map<String, Object>> patternMatcher(List<Map<String, Object>> images, String[] text) {
      List<Map<String, Object>> patterns = new ArrayList<>();
      for (Map<String, Object> match : images) {
        //TODO patternMatcher
      }
      return patterns;
    }

    private List<Map<String, Object>> imageMatcher(List<Map<String, Object>> images, String[] text, Pattern pat) {
      int lnNbr = 0;
      for (String line : text) {
        line = line.strip();
        Matcher matcher = pat.matcher(line);
        if (line.contains("\"\"\"") || line.contains("'''")) {
          continue;
        }
        while (matcher.find()) {
          String match = matcher.group(1);
          if (match != null) {
            int start = matcher.start(1);
            String imgName = match.substring(1, match.length() - 1);
            final File imgFile = imageExists(imgName);
            if (imgFile != null) {
              Map<String, Object> options = new HashMap<>();
              options.put(IButton.TEXT, match);
              options.put(IButton.LINE, lnNbr);
              options.put(IButton.LOFF, start);
              options.put(IButton.FILE, imgFile);
              images.add(options);
            }
          }
        }
        lnNbr++;
      }
      return images;
    }

    private File imageExists(String imgName) {
      String orgName = imgName;
      imgName = FilenameUtils.normalizeNoEndSeparator(imgName, true);
      imgName = imgName.replaceAll("//", "/");
      String ext;
      try {
        ext = FilenameUtils.getExtension(imgName);
      } catch (Exception e) {
        return null;
      }
      File folder = getImageFolder();
      if (ext.isEmpty()) {
        ext = "png";
        imgName += ".png";
      }
      if ("png;jpg;jpeg;".contains(ext + ";")) {
        File imgFile = new File(imgName);
        if (!imgFile.isAbsolute()) {
          imgFile = new File(folder, imgName);
        }
        if (imgFile.exists()) {
          log("%s (%s)", orgName, imgFile);
          return imgFile;
        }
      }
      return null;
    }

    public void insertImageButton(File imgFile) {
      final EditorImageButton button = new EditorImageButton(imgFile);
      pane.insertComponent(button);
    }

    public boolean resetContentToText() {
      InputStreamReader isr;
      try {
        isr = new InputStreamReader(new ByteArrayInputStream(
                pane.getText().getBytes(Charset.forName("utf-8"))), Charset.forName("utf-8"));
        pane.read(new BufferedReader(isr), null);
      } catch (Exception ex) {
        error("readContent: from String (%s)", ex.getMessage());
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

  private void convertSrcToHtml(String bundle) {
//    IScriptRunner runner = ScriptingSupport.getRunner(null, "jython");
//    if (runner != null) {
//      runner.doSomethingSpecial("convertSrcToHtml", new String[]{bundle});
//    }
  }

  public void exportAsZip() {
    PaneContext context = getActiveContext();
    SikulixFileChooser chooser = new SikulixFileChooser(SikulixIDE.get());
    File file = chooser.export();
    if (file == null) {
      return;
    }
    if (!context.save()) {
      return;
    }
    String zipPath = file.getAbsolutePath();
    if (context.isBundle()) {
      if (!file.getAbsolutePath().endsWith(".skl")) {
        zipPath += ".skl";
      }
    } else {
      if (!file.getAbsolutePath().endsWith(".zip")) {
        zipPath += ".zip";
      }
    }
    File zipFile = new File(zipPath);
    if (zipFile.exists()) {
      int answer = JOptionPane.showConfirmDialog(
              null, SikuliIDEI18N._I("msgFileExists", zipFile),
              SikuliIDEI18N._I("dlgFileExists"), JOptionPane.YES_NO_OPTION);
      if (answer != JOptionPane.YES_OPTION) {
        return;
      }
      FileManager.deleteFileOrFolder(zipFile);
    }
    try {
      zipDir(context.getFolder(), zipFile, context.getFileName());
      log("Exported packed SikuliX Script to: %s", zipFile);
    } catch (Exception ex) {
      log("ERROR: Export did not work: %s", zipFile); //TODO
    }
  }

  private static void zipDir(File zipDir, File zipFile, String fScript) throws IOException {
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new FileOutputStream(zipFile));
      String[] dirList = zipDir.list();
      byte[] readBuffer = new byte[1024];
      int bytesIn;
      ZipEntry anEntry;
      String ending = fScript.substring(fScript.length() - 3);
      String sName = zipFile.getName();
      sName = sName.substring(0, sName.length() - 4) + ending;
      for (int i = 0; i < dirList.length; i++) {
        File f = new File(zipDir, dirList[i]);
        if (f.isFile()) {
          if (fScript.equals(f.getName())) {
            anEntry = new ZipEntry(sName);
          } else {
            anEntry = new ZipEntry(f.getName());
          }
          FileInputStream fis = new FileInputStream(f);
          zos.putNextEntry(anEntry);
          while ((bytesIn = fis.read(readBuffer)) != -1) {
            zos.write(readBuffer, 0, bytesIn);
          }
          fis.close();
        }
      }
    } catch (Exception ex) {
      String msg = "";
      msg = ex.getMessage() + "";
    } finally {
      zos.close();
    }
  }

  Map<String, List<Integer>> parseforImages() {
    File imageFolder = getActiveContext().getImageFolder();
    trace("parseforImages: in %s", imageFolder);
    String scriptText = getActiveContext().getPane().getText();
    Lexer lexer = getLexer();
    Map<String, List<Integer>> images = new HashMap<>();
    lineNumber = 0;
    parseforImagesWalk(imageFolder, lexer, scriptText, 0, images);
    trace("parseforImages finished");
    return images;
  }

  private void parseforImagesWalk(File imageFolder, Lexer lexer,
                                  String text, int pos, Map<String, List<Integer>> images) {
    trace("parseforImagesWalk");
    Iterable<Token> tokens = lexer.getTokens(text);
    boolean inString = false;
    String current;
    String innerText;
    String[] possibleImage = new String[]{""};
    String[] stringType = new String[]{""};
    for (Token t : tokens) {
      current = t.getValue();
      if (current.endsWith("\n")) {
        if (inString) {
          SX.popError(
                  String.format("Orphan string delimiter (\" or ')\n" +
                          "in line %d\n" +
                          "No images will be deleted!\n" +
                          "Correct the problem before next save!", lineNumber),
                  "Delete images on save");
          error("DeleteImagesOnSave: No images deleted, caused by orphan string delimiter (\" or ') in line %d", lineNumber);
          images.clear();
          images.put(uncompleteStringError, null);
          break;
        }
        lineNumber++;
      }
      if (t.getType() == TokenType.Comment) {
        trace("parseforImagesWalk::Comment");
        innerText = t.getValue().substring(1);
        parseforImagesWalk(imageFolder, lexer, innerText, t.getPos() + 1, images);
        continue;
      }
      if (t.getType() == TokenType.String_Doc) {
        trace("parseforImagesWalk::String_Doc");
        innerText = t.getValue().substring(3, t.getValue().length() - 3);
        parseforImagesWalk(imageFolder, lexer, innerText, t.getPos() + 3, images);
        continue;
      }
      if (!inString) {
        inString = parseforImagesGetName(current, inString, possibleImage, stringType);
        continue;
      }
      if (!parseforImagesGetName(current, inString, possibleImage, stringType)) {
        inString = false;
        parseforImagesCollect(imageFolder, possibleImage[0], pos + t.getPos(), images);
        continue;
      }
    }
  }

  private boolean parseforImagesGetName(String current, boolean inString,
                                        String[] possibleImage, String[] stringType) {
    trace("parseforImagesGetName (inString: %s) %s", inString, current);
    if (!inString) {
      if (!current.isEmpty() && (current.contains("\"") || current.contains("'"))) {
        possibleImage[0] = "";
        stringType[0] = current.substring(current.length() - 1, current.length());
        return true;
      }
    }
    if (!current.isEmpty() && "'\"".contains(current) && stringType[0].equals(current)) {
      return false;
    }
    if (inString) {
      possibleImage[0] += current;
    }
    return inString;
  }

  private void parseforImagesCollect(File imageFolder, String img, int pos,
                                     Map<String, List<Integer>> images) {
    trace("parseforImagesCollect");
    if (img.endsWith(".png") || img.endsWith(".jpg") || img.endsWith(".jpeg")) {
      if (img.contains(File.separator)) {
        if (!img.contains(imageFolder.getPath())) {
          return;
        }
        img = new File(img).getName();
      }
      if (images.containsKey(img)) {
        images.get(img).add(pos);
      } else {
        List<Integer> poss = new ArrayList<>();
        poss.add(pos);
        images.put(img, poss);
      }
    }
  }

  public void reparseOnRenameImage(String oldName, String newName, boolean fileOverWritten) {
    if (fileOverWritten) {
      Image.unCache(newName);
    }
    Map<String, List<Integer>> images = parseforImages();
    oldName = new File(oldName).getName();
    List<Integer> poss = images.get(oldName);
    if (images.containsKey(oldName) && poss.size() > 0) {
      Collections.sort(poss, new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
          if (o1 > o2) return -1;
          return 1;
        }
      });
      reparseRenameImages(poss, oldName, new File(newName).getName());
    }
    //TODO doReparse();
  }

  private boolean reparseRenameImages(List<Integer> poss, String oldName, String newName) {
    StringBuilder text = new StringBuilder(getActiveContext().getPane().getText());
    int lenOld = oldName.length();
    for (int pos : poss) {
      text.replace(pos - lenOld, pos, newName);
    }
    getActiveContext().getPane().setText(text.toString());
    return true;
  }

  private Lexer getLexer() {
//TODO this only works for cleanbundle to find the image strings
    String scriptType = "python";
    if (null != lexers.get(scriptType)) {
      return lexers.get(scriptType);
    }
    try {
      Lexer lexer = Lexer.getByName(scriptType);
      lexers.put(scriptType, lexer);
      return lexer;
    } catch (ResolutionException ex) {
      return null;
    }
  }

  private static final Map<String, Lexer> lexers = new HashMap<>();

  int lineNumber = 0;
  public String uncompleteStringError = "uncomplete_string_error";

  public String getImageNameFromLine() {
    String line = getLineTextAtCaret().strip();
    Pattern aName = Pattern.compile("^([A-Za-z0-9_]+).*?=");
    Matcher matcher = aName.matcher(line);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  String getLineTextAtCaret() {
    return getActiveContext().getPane().getLineTextAtCaret();
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
      }
      if (context.isTemp()) {
        continue;
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
        for (String filename : filenames) {
          if (filename.isEmpty()) {
            continue;
          }
          filesToLoad.add(new File(filename));
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
    if (filesToLoad.size() > 0) {
      for (File file : filesToLoad) {
        createFileContext(file);
      }
      getContextAt(0).closeSilent();
      tempIndex = 1;
    }
    return filesToLoad;
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

    public void doExport(ActionEvent ae) {
      exportAsZip();
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
      getActiveContext().setShowThumbs(showThumbsState);
      getActiveContext().reparse();
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
    JButton btnSubregion = new ButtonSubregion();
    JButton btnLocation = new ButtonLocation();
    JButton btnOffset = new ButtonOffset();
//TODO ButtonShow/ButtonShowIn
    JButton btnShow = new ButtonShow();
    JButton btnShowIn = new ButtonShowIn();

    btnCapture = new ButtonCapture();
    toolbar.add(btnCapture);
    toolbar.add(btnInsertImage);
    toolbar.add(btnSubregion);
    toolbar.add(btnLocation);
    toolbar.add(btnOffset);
    toolbar.add(btnShow);
    toolbar.add(btnShowIn);
    toolbar.add(Box.createHorizontalGlue());
    btnRun = new ButtonRun();
    toolbar.add(btnRun);
    btnRunSlow = new ButtonRunViz();
    toolbar.add(btnRunSlow);
    toolbar.add(Box.createHorizontalGlue());

    toolbar.add(Box.createRigidArea(new Dimension(7, 0)));
    toolbar.setFloatable(false);

    btnRecord = new ButtonRecord();
    toolbar.add(btnRecord);

//    JComponent jcSearchField = createSearchField();
//    toolbar.add(jcSearchField);

    return toolbar;
  }

  class ButtonInsertImage extends ButtonOnToolbar {

    ButtonInsertImage() {
      super();
      buttonText = SikulixIDE._I("btnInsertImageLabel");
      buttonHint = SikulixIDE._I("btnInsertImageHint");
      iconFile = "/icons/insert-image-icon.png";
      init();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
      final String name = sikulixIDE.getImageNameFromLine();
      final PaneContext context = getActiveContext();
      EditorPane codePane = context.getPane();
      File file = new SikulixFileChooser(ideWindow).loadImage();
      if (file == null) {
        return;
      }
      File imgFile = codePane.copyFileToBundle(file); //TODO context
      if (!name.isEmpty()) {
        final File newFile = new File(imgFile.getParentFile(), name + "." + FilenameUtils.getExtension(imgFile.getName()));
        if (!newFile.exists()) {
          if (imgFile.renameTo(newFile)) {
            imgFile = newFile;
          }
        } else {
          final String msg = String.format("%s already exists - stored as %s", newFile.getName(), imgFile.getName());
          Sikulix.popError(msg, "IDE: Insert Image");
        }
      }
      if (context.getShowThumbs()) {
        codePane.insertComponent(new EditorImageButton(imgFile));
      } else {
        codePane.insertString("\"" + imgFile.getName() + "\"");
      }
    }
  }

  class ButtonSubregion extends ButtonOnToolbar implements EventObserver {

    String promptText;
    Point start = new Point(0, 0);
    Point end = new Point(0, 0);

    ButtonSubregion() {
      super();
      buttonText = "Region"; // SikuliIDE._I("btnRegionLabel");
      buttonHint = SikulixIDE._I("btnRegionHint");
      iconFile = "/icons/region-icon.png";
      promptText = SikulixIDE._I("msgCapturePrompt");
      init();
    }

    @Override
    public void runAction(ActionEvent ae) {
      if (shouldRun()) {
        OverlayCapturePrompt.capturePrompt(this, promptText);
      }
    }

    @Override
    public void update(EventSubject es) {
      OverlayCapturePrompt ocp = (OverlayCapturePrompt) es;
      Rectangle selectedRectangle = ocp.getSelectionRectangle();
      start = ocp.getStart();
      end = ocp.getEnd();
      ocp.close();
      ScreenDevice.closeCapturePrompts();
      captureComplete(selectedRectangle);
      SikulixIDE.showAgain();
    }

    void captureComplete(Rectangle selectedRectangle) {
      int x, y, w, h;
      EditorPane codePane = getCurrentCodePane();
      if (selectedRectangle != null) {
        Rectangle roi = selectedRectangle;
        x = (int) roi.getX();
        y = (int) roi.getY();
        w = (int) roi.getWidth();
        h = (int) roi.getHeight();
        if (codePane.context.getShowThumbs()) {
          if (prefs.getPrefMoreImageThumbs()) { //TODO
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
      buttonText = "Location";
      buttonHint = "Location as center of selection";
      iconFile = "/icons/region-icon.png";
      promptText = "Select a Location";
      init();
    }

    @Override
    public void captureComplete(Rectangle selectedRectangle) {
      int x, y, w, h;
      if (selectedRectangle != null) {
        Rectangle roi = selectedRectangle;
        x = (int) (roi.getX() + roi.getWidth() / 2);
        y = (int) (roi.getY() + roi.getHeight() / 2);
        getCurrentCodePane().insertString(String.format("Location(%d, %d)", x, y));
      }
    }
  }

  class ButtonOffset extends ButtonSubregion {

    ButtonOffset() {
      super();
      buttonText = "Offset";
      buttonHint = "Offset as width/height of selection";
      iconFile = "/icons/region-icon.png";
      promptText = "Select an Offset";
      init();
    }

    @Override
    public void captureComplete(Rectangle selectedRectangle) {
      int x, y, ox, oy;
      if (selectedRectangle != null) {
        Rectangle roi = selectedRectangle;
        ox = (int) roi.getWidth();
        oy = (int) roi.getHeight();
        Location start = new Location(super.start);
        Location end = new Location(super.end);
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

  class ButtonShow extends ButtonOnToolbar {

    ButtonShow() {
      super();
      buttonText = "Show";
      buttonHint = "Find and highlight the image in current line";
      iconFile = "/icons/region-icon.png";
      init();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      EditorPane codePane = getActiveContext().getPane();
      String line = codePane.getLineTextAtCaret();
      final String item = codePane.parseLineText(line);
      if (!item.isEmpty()) {
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
            log("ButtonShow:\n%s", eval); //TODO eval ButtonShow
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
      buttonHint = "Like Show, but in selected region";
      iconFile = "/icons/region-icon.png";
      init();
    }

    public boolean shouldRun() {
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

    public void captureComplete(Rectangle selectedRectangle) {
      if (selectedRectangle != null) {
        Region reg = new Region(selectedRectangle);
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
        Sikulix.popup("ButtonShowIn: Nothing to show!" +
                "\nThe line with the cursor should contain:" +
                "\n- an image file name or" +
                "\n- a Pattern with an image file name");
      }
    }
  }

  class ButtonRecord extends ButtonOnToolbar {

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
        PaneContext context = getActiveContext();
        EditorPane pane = context.getPane();
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
                context.reparse();
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
  class ButtonRun extends ButtonOnToolbar {

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

      SikulixIDE.getStatusbar().resetMessage();
      SikulixIDE.doHide();

      final PaneContext context = getActiveContext();
      EditorPane editorPane = context.getPane();
      if (editorPane.getDocument().getLength() == 0) {
        log("Run script not possible: Script is empty");
        return;
      }
      context.save();

      new Thread(new Runnable() {
        @Override
        public void run() {
          if (System.out.checkError()) {
            boolean shouldContinue = Sikulix.popAsk("System.out is broken (console output)!"
                    + "\nYou will not see any messages anymore!"
                    + "\nSave your work and restart the IDE!"
                    + "\nYou may ignore this on your own risk!" +
                    "\nYes: continue  ---  No: back to IDE", "Fatal Error");
            if (!shouldContinue) {
              log("Run script aborted: System.out is broken (console output)");
              SikulixIDE.showAgain();
              return;
            }
            log("Run script continued, though System.out is broken (console output)");
          }

          RunTime.pause(0.1f);
          clearMessageArea();
          resetErrorMark();
          doBeforeRun();

          IRunner.Options runOptions = new IRunner.Options();
          runOptions.setRunningInIDE();

          int exitValue = -1;
          try {
            IRunner runner = context.getRunner();
            //TODO make reloadImported specific for each editor tab
            if (runner.getType().equals(JythonRunner.TYPE)) {
              JythonSupport.get().reloadImported();
            }
            exitValue = runner.runScript(context.getFile().getAbsolutePath(), Commons.getUserArgs(), runOptions);
          } catch (Exception e) {
            log("Run Script: internal error:");
            e.printStackTrace();
          } finally {
            Runner.setLastScriptRunReturnCode(exitValue);
          }

          log("************** after RunScript");
          addErrorMark(runOptions.getErrorLine());
          if (Image.getIDEshouldReload()) {
            int line = context.getPane().getLineNumberAtCaret(context.getPane().getCaretPosition());
            context.reparse();
            context.getPane().jumpTo(line);
          }

          RunTime.cleanUpAfterScript();
          SikulixIDE.showAgain();
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
  private JTabbedPane messageArea = null;
  private EditorConsolePane messages = null;

  private boolean SHOULD_WRAP_LINE = false;

  private void initMessageArea() {
    messages.init(SHOULD_WRAP_LINE);
    messageArea = new JTabbedPane();
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

  void clearMessageArea() {
    if (messages == null) {
      return;
    }
    messages.clear();
  }

  void collapseMessageArea() {
    if (messages == null) {
      return;
    }
    if (messageAreaCollapsed) {
      return;
    }
    toggleCollapsed();
  }

  void uncollapseMessageArea() {
    if (messages == null) {
      return;
    }
    if (messageAreaCollapsed) {
      toggleCollapsed();
    }
  }

  private void toggleCollapsed() {
    if (messages == null) {
      return;
    }
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

  public EditorConsolePane getConsole() {
    return messages;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="25 Init ShortCuts HotKeys">
  void removeCaptureHotkey() {
    HotkeyManager.getInstance().removeHotkey("Capture");
  }

  void installCaptureHotkey() {
    HotkeyManager.getInstance().addHotkey("Capture", new HotkeyListener() {
      @Override
      public void hotkeyPressed(HotkeyEvent e) {
        if (sikulixIDE.isVisible()) {
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
