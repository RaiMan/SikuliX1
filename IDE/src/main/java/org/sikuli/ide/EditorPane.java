/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.PreferencesUser;
import org.sikuli.basics.Settings;
import org.sikuli.idesupport.ExtensionManager;
import org.sikuli.idesupport.IDESupport;
import org.sikuli.idesupport.IIDESupport;
import org.sikuli.idesupport.IIndentationLogic;
import org.sikuli.idesupport.syntaxhighlight.ResolutionException;
import org.sikuli.idesupport.syntaxhighlight.grammar.Lexer;
import org.sikuli.idesupport.syntaxhighlight.grammar.Token;
import org.sikuli.idesupport.syntaxhighlight.grammar.TokenType;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Location;
import org.sikuli.script.SX;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runnerSupport.IScriptRunner.EffectiveRunner;
import org.sikuli.script.runnerSupport.Runner;
import org.sikuli.script.runners.JythonRunner;
import org.sikuli.script.runners.PythonRunner;
import org.sikuli.script.runners.TextRunner;
import org.sikuli.script.support.Commons;
import org.sikuli.util.SikulixFileChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EditorPane extends JTextPane {

  //<editor-fold defaultstate="collapsed" desc="02 Initialization">
  private static final String me = "EditorPane: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  //for debugging watches
  EditorPane editorPane = null;

  EditorPane() {
    showThumbs = !PreferencesUser.get().getPrefMorePlainText();
    addMouseListener(new MouseInputAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              handlePopup();
            }
          }).start();
        }
        super.mouseClicked(e);
      }
    });
    scrollPane = new JScrollPane(this);
    editorPaneID = new Date().getTime();
    editorPane = this;
    log(lvl, "created %d", editorPaneID);
  }

  JScrollPane getScrollPane() {
    return scrollPane;
  }

  JScrollPane scrollPane = null;

  private SikuliEditorKit editorKit;
  private EditorCurrentLineHighlighter lineHighlighter = null;
  private TransferHandler transferHandler = null;

  SikuliIDEPopUpMenu getPopMenuImage() {
    return popMenuImage;
  }

  private SikuliIDEPopUpMenu popMenuImage;

  SikuliIDEPopUpMenu getPopMenuCompletion() {
    return popMenuCompletion;
  }

  private SikuliIDEPopUpMenu popMenuCompletion;

  //TODO right mouse click in tab text
  private void handlePopup() {
    log(3, "text popup");
  }

  void updateDocumentListeners(String source) {
    log(lvl + 1, "updateDocumentListeners from: %s", source);
    getDocument().addDocumentListener(getDirtyHandler());
    getDocument().addUndoableEditListener(getUndoRedo(this));
    SikulixIDE.getStatusbar().setType(editorPaneType);
  }

  EditorPaneUndoRedo getUndoRedo(EditorPane pane) {
    if (undoRedo == null) {
      undoRedo = new EditorPaneUndoRedo(pane);
    }
    return undoRedo;
  }

  private EditorPaneUndoRedo undoRedo = null;

  IIndentationLogic getIndentationLogic() {
    return indentationLogic;
  }

  private IIndentationLogic indentationLogic = null;

  private void initKeyMap() {
    InputMap map = this.getInputMap();
    int shift = InputEvent.SHIFT_DOWN_MASK;
    int ctrl = InputEvent.CTRL_DOWN_MASK;
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, shift), SikuliEditorKit.deIndentAction);
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ctrl), SikuliEditorKit.completionAction);
  }
  //</editor-fold>

  //<editor-fold desc="10 load content">
  boolean confirmDialog(String message, String title) {
    int ret = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
    boolean returnValue = true;
    if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.NO_OPTION) {
      returnValue = false;
    }
    return returnValue;
  }

  void init(String scriptType) {
    init(scriptType, "");
  }

  boolean init(String scriptType, String tabContent) {
//TODO setType for non-empty tab
/*
    Boolean shouldOverwrite = null;
    boolean useAlreadyOpen = false;
    boolean shouldChangeType = false;
    String dialogTitle = "Changing Tab Type";
    String msgSuffix = "\nNot yet handled -> NOOP";
    if (!tabContent.trim().isEmpty()) {
      //TODO Changing Tab Type for non-empty tab
      if (isDirty()) {
        String message = String.format("Save changes before?");
        if (confirmDialog(message + msgSuffix, dialogTitle)) {
          return false;
        }
        return false;
      }
      IScriptRunner newRunner = Runner.getRunner(scriptType);
      File newFile = changeExtension(editorPaneFileToRun, newRunner.getDefaultExtension());
      if (isBundle()) {
        if (isDirty()) {
          String message = String.format("Is bundle (.sikuli or bundle folder)");
          if (confirmDialog(message + msgSuffix, dialogTitle)) {
            return false;
          }
          return false;
        }
      }
      if (alreadyOpen(newFile.getPath(), -1)) {
        String message = String.format("Overwrite already open?");
        if (confirmDialog(message + msgSuffix, dialogTitle)) {
          return false;
        }
        useAlreadyOpen = true;
        return false;
      }
      if (newFile.exists()) {
        String message = String.format("overwrite existing file?\n%s" +
                "\n\nYes: overwrite - No: select new file", newFile);
        if (confirmDialog(message + msgSuffix, dialogTitle)) {
          shouldOverwrite = Boolean.TRUE;
          return false;
        }
      }
      shouldChangeType = true;
    }
*/
    if (null == scriptType) {
      editorPaneRunner = IDESupport.getDefaultRunner();
    } else {
      editorPaneRunner = Runner.getRunner(scriptType);
    }
    initForScriptType();
//TODO setType for non-empty tab
/*
    if (!tabContent.trim().isEmpty()) {
      setText(tabContent);
      changeFiles();
      if (null != shouldOverwrite) {
        if (shouldOverwrite) {
          saveAsFile(editorPaneFileToRun.getPath());
        } else {
          saveAsSelect();
        }
      }
      SikulixIDE.get().setCurrentFileTabTitle(editorPaneFileToRun.getPath());
      setDirty(shouldOverwrite == null);
    } else {
      setDirty(false);
    }
*/
    setDirty(false);
    return true;
  }

  public File selectFile() {
    File fileSelected = new SikulixFileChooser(SikulixIDE.get()).open();
    if (fileSelected == null) {
      return null;
    }
    int isOpen = alreadyOpen(fileSelected.getPath(), -1);
    if (isOpen != -1) {
      String toOpen = fileSelected.getName();
      String alreadyOpen = new File(getPaneAtIndex(isOpen).editorPaneFileSelected).getName();
      if (toOpen.equals(alreadyOpen)) {
        log(-1, "%s already open", toOpen);
      } else {
        log(-1, "%s already open as: %s", toOpen, alreadyOpen);
      }
      return null;
    }
    loadFile(fileSelected);
    if (editorPaneFile == null) {
      return null;
    }
    return fileSelected;
  }

  private int alreadyOpen(String pathSelected, int currentTab) {
    CloseableTabbedPane tabs = getTabs();
    int nTab = tabs.getTabCount();
    if (nTab > 0) {
      File fileSelected = new File(pathSelected);
      String possibleBundlePath = null;
      if (fileSelected.isDirectory()) {
        possibleBundlePath = FilenameUtils.removeExtension(fileSelected.getPath());
      } else if (isInBundle(fileSelected)) {
        possibleBundlePath = FilenameUtils.removeExtension(fileSelected.getParent());
      }
      for (int iTab = 0; iTab < nTab; iTab++) {
        if (currentTab > -1 && iTab == currentTab) {
          continue;
        }
        EditorPane checkedPane = getPaneAtIndex(iTab);
        String paneFile = checkedPane.editorPaneFileSelected;
        if (null == paneFile) continue;
        if (new File(paneFile).equals(fileSelected)) {
          tabs.setAlreadyOpen(iTab);
          return iTab;
        }
        if (possibleBundlePath != null && (checkedPane.isBundle() || checkedPane.isInBundle())) {
          String paneBundle = FilenameUtils.removeExtension(checkedPane.editorPaneFolder.getPath());
          if (possibleBundlePath.equals(paneBundle)) {
            tabs.setAlreadyOpen(iTab);
            return iTab;
          }
        }
      }
    }
    return -1;
  }

  EditorPane getPaneAtIndex(int index) {
    return (EditorPane) ((JScrollPane) getTabs().getComponentAt(index)).getViewport().getView();
  }

  private CloseableTabbedPane getTabs() {
    return SikulixIDE.get().getTabs();
  }

  public IScriptRunner getRunner() {
    return editorPaneRunner;
  }

  IScriptRunner editorPaneRunner = null;

  File editorPaneFileToRun = null;

  private boolean evalRunnerAndFile(File file) {
    IScriptRunner runner = Runner.getRunner(file.getAbsolutePath());
    EffectiveRunner runnerAndFile = runner.getEffectiveRunner(file.getAbsolutePath());
    if (runnerAndFile.getRunner() != null) {
      String script = runnerAndFile.getScript();
      if (null == script) {
        if (!file.isFile()) {
          return false;
        } else {
          script = file.getAbsolutePath();
        }
      }

      if (script.endsWith(".class")) {
        return false;
      }

      editorPaneFileToRun = new File(script);
      editorPaneRunner = runnerAndFile.getRunner();
      editorPaneIsBundle = runnerAndFile.isBundle();
      setTemp(runnerAndFile.isTempBundle());
      return true;
    }
    return false;
  }

  public void loadFile(File file) {
    log(lvl, "loadfile: %s", file);
    if (!evalRunnerAndFile(file)) {
      return;
    }
    initForScriptType();
    if (readContent(editorPaneFileToRun)) {
      setFiles(editorPaneFileToRun, file.getAbsolutePath()); // loadfile
      updateDocumentListeners("loadFile");
      if (!isBundle()) {
        checkSource();
      }
      String text = getText();
      if (text.isEmpty()) {
        appendString("\n");
      } else {
        String lastChar = text.substring(text.length() - 1);
        if (!lastChar.equals("\n")) {
          appendString("\n");
        }
        doParse();
      }
      setCaretPosition(0);
      setDirty(false);
      editorPaneRunner.adjustImportPath(getFiles(), null);
    }
  }

  private void initForScriptType() {
    // initialize runner to speed up first script run
//    (new Thread() {
//      @Override
//      public void run() {
//        editorPaneRunner.init(null);
//        SikulixIDE.isRunnerReady(true);
//      }
//    }).start();

    editorPaneType = editorPaneRunner.getType();
    indentationLogic = null;
    setEditorPaneIDESupport(editorPaneType);
    if (null != editorPaneIDESupport) {
      try {
        indentationLogic = editorPaneIDESupport.getIndentationLogic();
        indentationLogic.setTabWidth(PreferencesUser.get().getTabWidth());
      } catch (Exception ex) {
      }
    }

    if (editorPaneType != null) {
      editorKit = new SikuliEditorKit();
      setEditorKit(editorKit);
      setContentType(editorPaneType);

      if (indentationLogic != null) {
        PreferencesUser.get().addPreferenceChangeListener(new PreferenceChangeListener() {
          @Override
          public void preferenceChange(PreferenceChangeEvent event) {
            if (event.getKey().equals("TAB_WIDTH")) {
              indentationLogic.setTabWidth(Integer.parseInt(event.getNewValue()));
            }
          }
        });
      }
    }

    if (transferHandler == null) {
      transferHandler = new MyTransferHandler();
    }
    setTransferHandler(transferHandler);

    if (lineHighlighter == null) {
      lineHighlighter = new EditorCurrentLineHighlighter(this);
      addCaretListener(lineHighlighter);
      initKeyMap();
      //addKeyListener(this);
      //addCaretListener(this);
    }

    popMenuImage = new SikuliIDEPopUpMenu("POP_IMAGE", this);
    if (!popMenuImage.isValidMenu()) {
      popMenuImage = null;
    }

    popMenuCompletion = new SikuliIDEPopUpMenu("POP_COMPLETION", this);
    if (!popMenuCompletion.isValidMenu()) {
      popMenuCompletion = null;
    }

    setFont(new Font(PreferencesUser.get().getFontName(), Font.PLAIN, PreferencesUser.get().getFontSize()));
    setMargin(new Insets(3, 3, 3, 3));
    setBackground(Color.WHITE);
    if (!Settings.isMac()) {
      setSelectionColor(new Color(170, 200, 255));
    }

//      updateDocumentListeners("initBeforeLoad");

    SikulixIDE.getStatusbar().setType(editorPaneType);
    log(lvl, "InitTab: (%s)", editorPaneType);
  }

  private boolean readContent(File scriptFile) {
    InputStreamReader isr;
    try {
      isr = new InputStreamReader(new FileInputStream(scriptFile), Charset.forName("utf-8"));
      read(new BufferedReader(isr), null);
    } catch (Exception ex) {
      log(-1, "readContent: %s (%s)", scriptFile, ex.getMessage());
      return false;
    }
    return true;
  }

  private boolean readContent(String script) {
    InputStreamReader isr;
    try {
      isr = new InputStreamReader(new ByteArrayInputStream(script.getBytes(Charset.forName("utf-8"))), Charset.forName("utf-8"));
      read(new BufferedReader(isr), null);
    } catch (Exception ex) {
      log(-1, "readContent: from String (%s)", ex.getMessage());
      return false;
    }
    return true;
  }

  public boolean isText() {
    return editorPaneType == TextRunner.TYPE;
  }

  public boolean isPython() {
    return editorPaneType == JythonRunner.TYPE || editorPaneType == PythonRunner.TYPE;
  }
  //</editor-fold>

  //<editor-fold desc="11 check content">
  //TODO checkSource::setBundlePath for non-bundle-tabs
  public void checkSource() {
    if (isBundle()) {
      return;
    }
    log(3, "checkSource: started (%s)", editorPaneFile);
    String scriptText = getText();
    if (editorPaneType == JythonRunner.TYPE) {
      if (ExtensionManager.hasPython()) {
        String intro = scriptText.substring(0, Math.min(20, scriptText.length())).trim().toUpperCase();
        if (intro.contains(ExtensionManager.shebangPython)) {
          editorPaneType = PythonRunner.TYPE;
        }
      }
    }
    SikulixIDE.getStatusbar().setType(getType());
    Matcher matcher = Pattern.compile("setBundlePath.*?\\(.*?\"(.*?)\".*?\\)").matcher(scriptText);
    if (matcher.find()) {
      String line = getTextLineAt(matcher.start());
      if (lineIsComment(line)) {
        return;
      }
      String path = matcher.group(1);
      log(3, "checkSource: found setBundlePath: %s", path);
      File newBundleFolder = new File(path.replace("\\\\", "\\"));
      if (Commons.runningWindows() && (newBundleFolder.getPath().startsWith("\\") || newBundleFolder.getPath().startsWith("/"))) {
        try {
          newBundleFolder = new File(new File("\\").getCanonicalPath(), newBundleFolder.getPath().substring(1));
        } catch (IOException e) {
          return;
        }
      }
      try {
        if (newBundleFolder.isAbsolute()) {
          newBundleFolder = newBundleFolder.getCanonicalFile();
        } else {
          newBundleFolder = new File(editorPaneFolder, newBundleFolder.getPath()).getCanonicalFile();
        }
      } catch (Exception ex) {
        return;
      }
      setImageFolder(newBundleFolder);
    }
  }

  private boolean lineIsComment(String line) {
    //TODO eval other comment types
    if (line.startsWith("#")) return true;
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="15 content file">
  public void setTemp(boolean temp) {
    editorPaneIsTemp = temp;
  }

  public boolean isTemp() {
    return editorPaneIsTemp;
  }

  private boolean editorPaneIsTemp = false;

  public boolean isEmpty() {
    return getText().isEmpty();
  }

  boolean isInBundle() {
    return isInBundle(editorPaneFileToRun);
  }

  static boolean isInBundle(File file) {
    String possibleBundleName = FilenameUtils.removeExtension(file.getName()).toLowerCase();
    String folderOrBundleName = FilenameUtils.removeExtension(file.getParentFile().getName()).toLowerCase();
    return possibleBundleName.equals(folderOrBundleName);
  }

  boolean isBundle() {
    return editorPaneIsBundle;
  }

  void setIsBundle() {
    editorPaneIsBundle = true;
  }

  void setIsFile() {
    editorPaneIsBundle = false;
  }

  private boolean editorPaneIsBundle = false;

  public String getFilePath() {
    if (!hasEditingFile()) {
      return null;
    }
    return editorPaneFile.getAbsolutePath();
  }

  public String getFolderPath() {
    if (!hasEditingFile()) {
      return null;
    }
    return editorPaneFile.getParent();
  }

  public String getSourceReference() {
    if (!hasEditingFile()) {
      return null;
    }
    if (isBundle()) {
      return getFolderPath();
    } else {
      return getFilePath();
    }
  }

  public void setFiles(File editorPaneFile) {
    setFiles(editorPaneFile, null); // setFiles(file)
  }

  public void setFiles(File paneFile, String paneFileSelected) {
    if (paneFile == null) {
      return;
    }
    editorPaneFileSelected = paneFileSelected;
    editorPaneFile = paneFile;
    editorPaneFolder = editorPaneFile.getParentFile();
    setImageFolder(editorPaneFolder);
    if (null != paneFileSelected) {
      log(3, "setFiles: for: %s", paneFileSelected);
    } else {
      if (!isTemp()) {
        setIsFile();
        editorPaneFileSelected = editorPaneFile.getAbsolutePath();
        editorPaneFileToRun = editorPaneFile;
        log(3, "setFiles: for: %s", editorPaneFile);
      }
    }
  }

  private void changeFiles() {
    String extension = editorPaneRunner.getDefaultExtension();
    setFiles(changeExtension(editorPaneFileToRun, extension)); //changeFiles (not used)
  }

  public Map<String, String> getFiles() {
    Map<String, String> files = new HashMap<>();
    files.put("file", editorPaneFile.getAbsolutePath());
    files.put("folder", editorPaneFolder.getAbsolutePath());
    files.put("images", editorPaneImageFolder.getAbsolutePath());
    files.put("isBundle", String.valueOf(isBundle()));
    return files;
  }

  private File changeExtension(File file, String extension) {
    String filePath = FilenameUtils.removeExtension(file.getPath()) + "." + extension;
    return new File(filePath);
  }

  public long getID() {
    return editorPaneID;
  }

  long editorPaneID = 0;

  File editorPaneFile = null;
  File editorPaneFolder = null;

  String editorPaneFileSelected = null;

  String getType() {
    return editorPaneType;
  }

  void setType(String editorPaneType) {
    this.editorPaneType = editorPaneType;
  }

  private String editorPaneType;

  public boolean hasIDESupport() {
    return null != editorPaneIDESupport;
  }

  public IIDESupport getEditorPaneIDESupport() {
    return editorPaneIDESupport;
  }

  public void setEditorPaneIDESupport(String type) {
    editorPaneIDESupport = IDESupport.ideSupporter.get(type);
  }

  private IIDESupport editorPaneIDESupport = null;

  public boolean hasEditingFile() {
    return editorPaneFile != null;
  }

  public void showType() {
    if (isPython()) {
      if (ExtensionManager.hasPython() && ExtensionManager.hasShebang(ExtensionManager.shebangPython, getText())) {
        setType(PythonRunner.TYPE);
        SikulixIDE.getStatusbar().setType(PythonRunner.TYPE);
      } else {
        setType(JythonRunner.TYPE);
        SikulixIDE.getStatusbar().setType(JythonRunner.TYPE);
      }
    }
  }

  public String getCurrentShortFilename() {
    if (isBundle()) {
      File f = new File(getSrcBundle());
      return f.getName();
    }
    if (isTemp()) {
      return "Untitled";
    }
    return editorPaneFile.getName();
  }

  public File saveAndGetCurrentFile() {
    if (hasEditingFile() && isDirty()) {
      saveAsSelect();
    }
    return editorPaneFile;
  }

  public File getCurrentFile() {
    return editorPaneFile;
  }
  //</editor-fold>

  //<editor-fold desc="16 image path">
  public File getImageFolder() {
    return editorPaneImageFolder;
  }

  public String getImagePath() {
    return editorPaneImageFolder.getAbsolutePath();
  }

  public void setBundleFolder() {
    ImagePath.setBundleFolder(editorPaneImageFolder);
  }

  public void setImageFolder(File imageFolder) {
    if (imageFolder != null && imageFolder.exists()) {
      editorPaneImageFolder = imageFolder;
      ImagePath.setBundleFolder(editorPaneImageFolder);
    } else {
      log(-1, "setImageFolder: null or not exists: %s", imageFolder);
    }
  }

  File editorPaneImageFolder = null;

  public String getSrcBundle() {
    return editorPaneFolder.getAbsolutePath();
  }

  public String getBundlePath() {
    return editorPaneImageFolder.getAbsolutePath();
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="17 caret handling">
  public void saveCaretPosition() {
    caretPosition = getCaretPosition();
  }

  public void restoreCaretPosition() {
    if (caretPosition < 0) {
      return;
    }
    if (caretPosition < getDocument().getLength()) {
      setCaretPosition(caretPosition);
    } else {
      setCaretPosition(Math.max(0, getDocument().getLength() - 1));
    }
    caretPosition = -1;
  }

  private int caretPosition = -1;

  public int getLineNumberAtCaret(int caretPosition) {
    Element root = getDocument().getDefaultRootElement();
    return root.getElementIndex(caretPosition) + 1;
  }

  private String getTextLineAt(int start) {
    Element root = getDocument().getDefaultRootElement();
    return getLineTextFrom(root.getElement(root.getElementIndex(start)));
  }

  public Element getLineAtCaret(int caretPosition) {
    Element root = getDocument().getDefaultRootElement();
    Element result;
    if (caretPosition == -1) {
      result = root.getElement(root.getElementIndex(getCaretPosition()));
    } else {
      result = root.getElement(root.getElementIndex(root.getElementIndex(caretPosition)));
    }
    return result;
  }

  public String getLineTextAtCaret() {
    return getLineTextFrom(getLineAtCaret(-1));
  }

  private String getLineTextFrom(Element elem) {
    Document doc = elem.getDocument();
    Element subElem;
    String text;
    String line = "";
    for (int i = 0; i < elem.getElementCount(); i++) {
      text = "";
      subElem = elem.getElement(i);
      int start = subElem.getStartOffset();
      int end = subElem.getEndOffset();
      if (subElem.getName().contains("component")) {
        text = StyleConstants.getComponent(subElem.getAttributes()).toString();
      } else {
        try {
          text = doc.getText(start, end - start);
        } catch (Exception ex) {
        }
      }
      line += text;
    }
    return line.trim();
  }

  public Element getLineAtPoint(MouseEvent me) {
    Point p = me.getLocationOnScreen();
    Point pp = getLocationOnScreen();
    p.translate(-pp.x, -pp.y);
    int pos = viewToModel(p);
    Element root = getDocument().getDefaultRootElement();
    int e = root.getElementIndex(pos);
    if (e == -1) {
      return null;
    }
    return root.getElement(e);
  }

  public boolean jumpTo(int lineNo, int column) {
    log(lvl + 1, "jumpTo pos: " + lineNo + "," + column);
    try {
      int off = getLineStartOffset(lineNo - 1) + column - 1;
      int lineCount = getDocument().getDefaultRootElement().getElementCount();
      if (lineNo < lineCount) {
        int nextLine = getLineStartOffset(lineNo);
        if (off >= nextLine) {
          off = nextLine - 1;
        }
      }
      if (off >= 0) {
        setCaretPosition(off);
      }
    } catch (BadLocationException ex) {
      jumpTo(lineNo);
      return false;
    }
    return true;
  }

  public boolean jumpTo(int lineNo) {
    log(lvl + 1, "jumpTo line: " + lineNo);
    try {
      setCaretPosition(getLineStartOffset(lineNo - 1));
    } catch (BadLocationException ex) {
      return false;
    }
    return true;
  }

  public int getLineStartOffset(int line) throws BadLocationException {
    // line starting from 0
    Element map = getDocument().getDefaultRootElement();
    if (line < 0) {
      throw new BadLocationException("Negative line", -1);
    } else if (line >= map.getElementCount()) {
      throw new BadLocationException("No such line", getDocument().getLength() + 1);
    } else {
      Element lineElem = map.getElement(line);
      return lineElem.getStartOffset();
    }
  }

  public String getLine(int lineno) {
    String line = "";
    Element map = getDocument().getDefaultRootElement();
    Element lineElem = map.getElement(lineno - 1);
    int start = lineElem.getStartOffset();
    int end = lineElem.getEndOffset();
    try {
      line = getDocument().getText(start, end - start);
    } catch (BadLocationException e) {
    }
    return line;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="18 content insert append">
  public void insertString(String str) {
    int sel_start = getSelectionStart();
    int sel_end = getSelectionEnd();
    if (sel_end != sel_start) {
      try {
        getDocument().remove(sel_start, sel_end - sel_start);
      } catch (BadLocationException e) {
        log(-1, "insertString: Problem while trying to insert\n%s", e.getMessage());
      }
    }
    int pos = getCaretPosition();
    insertString(pos, str);
    int new_pos = getCaretPosition();
    int end = parseRange(pos, new_pos);
    setCaretPosition(end);
  }

  private void insertString(int pos, String str) {
    Document doc = getDocument();
    try {
      doc.insertString(pos, str, null);
    } catch (Exception e) {
      log(-1, "insertString: Problem while trying to insert at pos\n%s", e.getMessage());
    }
  }

  //TODO not used
  public void appendString(String str) {
    Document doc = getDocument();
    try {
      int start = doc.getLength();
      doc.insertString(doc.getLength(), str, null);
      int end = doc.getLength();
      //end = parseLine(start, end, patHistoryBtnStr);
    } catch (Exception e) {
      log(-1, "appendString: Problem while trying to append\n%s", e.getMessage());
    }
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="19 replace text patterns with image buttons">
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
    doReparse();
  }

  private boolean reparseRenameImages(List<Integer> poss, String oldName, String newName) {
    StringBuilder text = new StringBuilder(getText());
    int lenOld = oldName.length();
    for (int pos : poss) {
      text.replace(pos - lenOld, pos, newName);
    }
    setText(text.toString());
    return true;
  }

  public void doReparse() {
    saveCaretPosition();
    readContent(getText());
    updateDocumentListeners("reparse");
    doParse();
    restoreCaretPosition();
  }

  public void doParse() {
    Document doc = getDocument();
    Element root = doc.getDefaultRootElement();
    parse(root);
  }

  private void parse(Element node) {
    if (!showThumbs) {
      // do not show any thumbnails
      return;
    }
    int count = node.getElementCount();
    for (int i = 0; i < count; i++) {
      Element elm = node.getElement(i);
      log(lvl + 1, elm.toString());
      if (elm.isLeaf()) {
        parseRange(elm.getStartOffset(), elm.getEndOffset());
      } else {
        parse(elm);
      }
    }
  }

  public String parseLineText(String line) {
    if (line.startsWith("#")) {
      Pattern aName = Pattern.compile("^#[A-Za-z0-9_]+ =$");
      Matcher mN = aName.matcher(line);
      if (mN.find()) {
        return line.substring(1).split(" ")[0];
      }
      return "";
    }
    Matcher mR = patRegionStr.matcher(line);
    String asOffset = ".asOffset()";
    if (mR.find()) {
      if (line.length() >= mR.end() + asOffset.length()) {
        if (line.substring(mR.end()).contains(asOffset)) {
          return line.substring(mR.start(), mR.end()) + asOffset;
        }
      }
      return line.substring(mR.start(), mR.end());
    }
    Matcher mL = patLocationStr.matcher(line);
    if (mL.find()) {
      return line.substring(mL.start(), mL.end());
    }
    Matcher mP = patPatternStr.matcher(line);
    if (mP.find()) {
      return line.substring(mP.start(), mP.end());
    }
    Matcher mI = patPngStr.matcher(line);
    if (mI.find()) {
      return line.substring(mI.start(), mI.end());
    }
    return "";
  }

  private int parseRange(int start, int end) {
    if (!showThumbs) {
      // do not show any thumbnails
      return end;
    }
    try {
      end = parseLine(start, end, patCaptureBtn);
      end = parseLine(start, end, patPatternStr);
      end = parseLine(start, end, patRegionStr);
      end = parseLine(start, end, patPngStr);
    } catch (BadLocationException e) {
      log(-1, "parseRange: Problem while trying to parse line\n%s", e.getMessage());
    }
    return end;
  }

  private int parseLine(int startOff, int endOff, Pattern ptn) throws BadLocationException {
    if (endOff <= startOff) {
      return endOff;
    }
    Document doc = getDocument();
    while (true) {
      String line = doc.getText(startOff, endOff - startOff);
      Matcher m = ptn.matcher(line);
      //System.out.println("["+line+"]");
      boolean ptnFound = m.find();
      if (ptnFound) {
        int len = m.end() - m.start();
        boolean replaced = replaceWithImage(startOff + m.start(), startOff + m.end(), ptn);
        if (replaced) {
          startOff += m.start() + 1;
          endOff -= len - 1;
        } else {
          startOff += m.end() + 1;
        }
      } else {
        break;
      }
    }
    return endOff;
  }

  private boolean replaceWithImage(int startOff, int endOff, Pattern ptn) throws BadLocationException {
    Document doc = getDocument();
    String imgStr = doc.getText(startOff, endOff - startOff);
    JComponent comp = null;

    if (ptn == patPatternStr || ptn == patPngStr) {
      if (PreferencesUser.get().getPrefMoreImageThumbs()) {
        comp = EditorPatternButton.createFromString(this, imgStr, null);
      } else {
        comp = EditorPatternLabel.labelFromString(this, imgStr);
      }
    } else if (ptn == patRegionStr) {
      if (PreferencesUser.get().getPrefMoreImageThumbs()) {
        comp = EditorRegionButton.createFromString(this, imgStr);
      } else {
        comp = EditorRegionLabel.labelFromString(this, imgStr);
      }
    } else if (ptn == patCaptureBtn) {
      comp = EditorPatternLabel.labelFromString(this, "");
    }
    if (comp != null) {
      this.select(startOff, endOff);
      this.insertComponent(comp);
      return true;
    }
    return false;
  }

  public String getRegionString(int x, int y, int w, int h) {
    return String.format("Region(%d,%d,%d,%d)", x, y, w, h);
  }

  public String getPatternString(String ifn, double sim, Location off, Image img, float resizeFactor, String mask) {
//TODO ifn really needed??
    if (ifn == null) {
      return "\"" + EditorPatternLabel.CAPTURE + "\"";
    }
    String imgName = new File(ifn).getName();
    if (img != null) {
      imgName = new File(img.getName()).getName();
    }
    String pat = "Pattern(\"" + imgName + "\")";
    String patternString = "";
    if (resizeFactor > 0 && resizeFactor != 1) {
      patternString += String.format(".resize(%.2f)", resizeFactor).replace(",", ".");
    }
    if (sim > 0) {
      if (sim >= 0.99) {
        patternString += ".exact()";
      } else if (!EditorPatternButton.isDefaultSimilarity(sim)) {
        patternString += String.format(Locale.ENGLISH, ".similar(.%d)", (int) (Math.round(sim * 100)));
      }
    }
    if (off != null && (off.x != 0 || off.y != 0)) {
      patternString += ".targetOffset(" + off.x + "," + off.y + ")";
    }
    if (null != mask && !mask.isEmpty()) {
      patternString += "." + mask + ")";
    }
    if (!patternString.equals("")) {
      patternString = pat + patternString;
    } else {
      patternString = "\"" + imgName + "\"";
    }
    return patternString;
  }

  private Map<String, List<Integer>> parseforImages() {
    String pbundle = FileManager.slashify(editorPaneImageFolder.getAbsolutePath(), false);
    log(3, "parseforImages: in %s", pbundle);
    String scriptText = getText();
    Lexer lexer = getLexer();
    Map<String, List<Integer>> images = new HashMap<String, List<Integer>>();
    lineNumber = 0;
    parseforImagesWalk(pbundle, lexer, scriptText, 0, images);
    log(3, "parseforImages finished");
    return images;
  }

  int lineNumber = 0;
  String uncompleteStringError = "uncomplete_string_error";

  private void parseforImagesWalk(String pbundle, Lexer lexer,
                                  String text, int pos, Map<String, List<Integer>> images) {
    //log(3, "parseforImagesWalk");
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
          log(-1, "DeleteImagesOnSave: No images deleted, caused by orphan string delimiter (\" or ') in line %d", lineNumber);
          images.clear();
          images.put(uncompleteStringError, null);
          break;
        }
        lineNumber++;
      }
      if (t.getType() == TokenType.Comment) {
        //log(3, "parseforImagesWalk::Comment");
        innerText = t.getValue().substring(1);
        parseforImagesWalk(pbundle, lexer, innerText, t.getPos() + 1, images);
        continue;
      }
      if (t.getType() == TokenType.String_Doc) {
        //log(3, "parseforImagesWalk::String_Doc");
        innerText = t.getValue().substring(3, t.getValue().length() - 3);
        parseforImagesWalk(pbundle, lexer, innerText, t.getPos() + 3, images);
        continue;
      }
      if (!inString) {
        inString = parseforImagesGetName(current, inString, possibleImage, stringType);
        continue;
      }
      if (!parseforImagesGetName(current, inString, possibleImage, stringType)) {
        inString = false;
        parseforImagesCollect(pbundle, possibleImage[0], pos + t.getPos(), images);
        continue;
      }
    }
  }

  private boolean parseforImagesGetName(String current, boolean inString,
                                        String[] possibleImage, String[] stringType) {
    //log(3, "parseforImagesGetName (inString: %s) %s", inString, current);
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

  private void parseforImagesCollect(String pbundle, String img, int pos, Map<String, List<Integer>> images) {
    String fimg;
    //log(3, "parseforImagesCollect");
    if (img.endsWith(".png") || img.endsWith(".jpg") || img.endsWith(".jpeg")) {
      fimg = FileManager.slashify(img, false);
      if (fimg.contains("/")) {
        if (!fimg.contains(pbundle)) {
          return;
        }
        img = new File(fimg).getName();
      }
      if (images.containsKey(img)) {
        images.get(img).add(pos);
      } else {
        List<Integer> poss = new ArrayList<Integer>();
        poss.add(pos);
        images.put(img, poss);
      }
    }
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

  private static final Map<String, Lexer> lexers = new HashMap<String, Lexer>();

  public boolean showThumbs;
  static Pattern patPngStr = Pattern.compile("(\"[^\"]+?\\.(?i)(png|jpg|jpeg)\")");
  static Pattern patCaptureBtn = Pattern.compile("(\"__CLICK-TO-CAPTURE__\")");
  static Pattern patPatternStr = Pattern.compile(
      "\\b(Pattern\\s*\\(\".*?\"\\)(\\.\\w+\\([^)]*\\))+)");
  static Pattern patRegionStr = Pattern.compile(
      "\\b(Region\\s*\\((-?[\\d\\s],?)+\\))");
  static Pattern patLocationStr = Pattern.compile(
      "\\b(Location\\s*\\((-?[\\d\\s],?)+\\))");
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="20 dirty handling">
  public boolean isDirty() {
    return scriptIsDirty;
  }

  public void setDirty(boolean flag) {
    if (scriptIsDirty == flag) {
      return;
    }
    scriptIsDirty = flag;
    SikulixIDE.get().setCurrentFileTabTitleDirty(scriptIsDirty);
  }

  private DirtyHandler getDirtyHandler() {
    if (dirtyHandler == null) {
      dirtyHandler = new DirtyHandler();
    }
    return dirtyHandler;
  }

  private boolean scriptIsDirty = false;
  private DirtyHandler dirtyHandler;

  private class DirtyHandler implements DocumentListener {

    @Override
    public void changedUpdate(DocumentEvent ev) {
      log(lvl + 1, "change update");
      //setDirty(true);
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
      log(lvl + 1, "insert update");
      setDirty(true);
    }

    @Override
    public void removeUpdate(DocumentEvent ev) {
      log(lvl + 1, "remove update");
      setDirty(true);
    }
  }
  //</editor-fold>

  //<editor-fold desc="22 save, close">
  public String saveTabContent() {
    if (editorPaneFile == null || isTemp()) {
      return saveAsSelect();
    } else {
      if (writeSriptFile()) {
        return editorPaneFile.getAbsolutePath();
      }
      return null;
    }
  }

  public String saveAsSelect() {
    SikulixFileChooser fileChooser = new SikulixFileChooser(SikulixIDE.get());
    File file = fileChooser.saveAs(getRunner().getDefaultExtension(), isBundle());
    if (file == null) {
      return null;
    }
    int currentTab = getTabs().getSelectedIndex();
    int tabAlreadyOpen = alreadyOpen(file.getAbsolutePath(), currentTab);
    if (-1 != tabAlreadyOpen) {
      SX.popError(String.format("Target is open in IDE\n%s\n" +
              "Close tab (%d) before doing saveAs or use other filename", file.getAbsolutePath(), tabAlreadyOpen + 1),
              "SaveAs: file is opened");
      return null;
    }
    if (file.exists()) {
      int answer = JOptionPane.showConfirmDialog(
          null, SikuliIDEI18N._I("msgFileExists", file),
          SikuliIDEI18N._I("dlgFileExists"), JOptionPane.YES_NO_OPTION);
      if (answer != JOptionPane.YES_OPTION) {
        return null;
      }
      FileManager.deleteFileOrFolder(file);
    }
    File savedFile;
    if (FilenameUtils.getExtension(file.getName()).isEmpty() ||
        FilenameUtils.getExtension(file.getName()).equals("sikuli")) {
      file.mkdir();
      savedFile = saveAsBundle(file.getAbsolutePath());
    } else {
      savedFile = saveAsFile(file.getAbsolutePath());
    }
    return savedFile.getAbsolutePath();
  }

  private File saveAsBundle(String targetFolder) {
    String sourceFolder = editorPaneFolder.getAbsolutePath();
    targetFolder = new File(targetFolder).getAbsolutePath();
    log(lvl, "saveAsBundle: to: %s", targetFolder);
    log(lvl, "saveAsBundle: from: %s", sourceFolder);
    if (isBundle()) {
      if (!IDESupport.transferScript(sourceFolder, targetFolder, getRunner())) {
        log(-1, "saveAsBundle: did not work");
        return null;
      }
    }
    ImagePath.remove(getImagePath());
    if (isTemp()) {
      FileManager.deleteTempDir(sourceFolder);
      setTemp(false);
    }
    String scriptName = FilenameUtils.getBaseName(targetFolder) + "." + getRunner().getDefaultExtension();
    File scriptFile = new File(targetFolder, scriptName);
    setIsBundle();
    setFiles(scriptFile, targetFolder); // saveAsBundle
    if (writeSriptFile()) {
      return editorPaneFolder;
    }
    return null;
  }

  private File saveAsFile(String filename) {
    log(lvl, "saveAsFile: " + filename);
    if (isTemp()) {
      FileManager.deleteTempDir(editorPaneFolder.getAbsolutePath());
      setTemp(false);
    }
    setFiles(new File(filename)); // saveAsFile
    if (writeSriptFile()) {
      return editorPaneFile;
    }
    return null;
  }

  private boolean writeSriptFile() {
    log(lvl, "writeSrcFile: " + editorPaneFile);
    try {
      this.write(new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(editorPaneFile.getAbsolutePath()),
          "UTF8")));
    } catch (IOException e) {
      return false;
    }
    if (isBundle()) {
      boolean shouldDeleteHTML = true;
      if (PreferencesUser.get().getAtSaveMakeHTML()) {
        try {
          convertSrcToHtml(getSrcBundle());
          shouldDeleteHTML = false;
        } catch (Exception ex) {
          log(-1, "Problem while trying to create HTML: %s", ex.getMessage());
        }
      }
      if (shouldDeleteHTML) {
        String snameDir = editorPaneFile.getParent();
        if (snameDir.endsWith(".sikuli")) {
          String sname = snameDir.replace(".sikuli", "") + ".html";
          (new File(snameDir, sname)).delete();
        }
      }
      if (PreferencesUser.get().getAtSaveCleanBundle()) {
        if (!editorPaneType.equals(JythonRunner.TYPE)) {
          log(lvl, "delete-not-used-images for %s using Python string syntax", editorPaneType);
        }
        try {
          cleanBundle();
        } catch (Exception ex) {
          log(-1, "Problem while trying to clean bundle (not used images): %s", ex.getMessage());
        }
      }
    }
    setDirty(false);
    return true;
  }

  public String exportAsZip() {
    SikulixFileChooser chooser = new SikulixFileChooser(SikulixIDE.get());
    File file = chooser.export();
    if (file == null) {
      return null;
    }
    String zipPath = file.getAbsolutePath();
    if (isBundle()) {
      if (!file.getAbsolutePath().endsWith(".skl")) {
        zipPath += ".skl";
      }
    } else {
      if (!file.getAbsolutePath().endsWith(".zip")) {
        zipPath += ".zip";
      }
    }
    if (new File(zipPath).exists()) {
      int answer = JOptionPane.showConfirmDialog(
          null, SikuliIDEI18N._I("msgFileExists", zipPath),
          SikuliIDEI18N._I("dlgFileExists"), JOptionPane.YES_NO_OPTION);
      if (answer != JOptionPane.YES_OPTION) {
        return null;
      }
      FileManager.deleteFileOrFolder(zipPath);
    }
    String pSource = editorPaneFile.getParent();
    if (writeSriptFile()) {
      try {
        zipDir(pSource, zipPath, editorPaneFile.getName());
        log(lvl, "Exported packed SikuliX Script to:\n%s", zipPath);
      } catch (Exception ex) {
        log(-1, "Exporting packed SikuliX Script did not work:\n%s", zipPath);
        return null;
      }
    }
    return zipPath;
  }

  private static void zipDir(String dir, String zipPath, String fScript) throws IOException {
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new FileOutputStream(zipPath));
      File zipDir = new File(dir);
      String[] dirList = zipDir.list();
      byte[] readBuffer = new byte[1024];
      int bytesIn;
      ZipEntry anEntry = null;
      String ending = fScript.substring(fScript.length() - 3);
      String sName = new File(zipPath).getName();
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

  private void convertSrcToHtml(String bundle) {
//    IScriptRunner runner = ScriptingSupport.getRunner(null, "jython");
//    if (runner != null) {
//      runner.doSomethingSpecial("convertSrcToHtml", new String[]{bundle});
//    }
  }

  private void cleanBundle() {
    log(3, "cleanBundle");
    Set<String> foundImages = parseforImages().keySet();
    if (foundImages.contains(uncompleteStringError)) {
      log(-1, "cleanBundle aborted (%s)", uncompleteStringError);
    } else {
      FileManager.deleteNotUsedImages(getBundlePath(), foundImages);
      log(lvl, "cleanBundle finished");
    }
  }

  public boolean close() throws IOException {
    if (!isTemp()) {
      log(lvl, "Tab close: %s", getCurrentShortFilename());
    }
    if (isDirty()) {
      if (isTemp()) {
        log(lvl, "Tab close: temp-%s", getCurrentShortFilename());
      }
      Object[] options = {SikuliIDEI18N._I("yes"), SikuliIDEI18N._I("no"), SikuliIDEI18N._I("cancel")};
      int ans = JOptionPane.showOptionDialog(this,
          SikuliIDEI18N._I("msgAskSaveChanges", getCurrentShortFilename()),
          SikuliIDEI18N._I("dlgAskCloseTab"),
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE,
          null,
          options, options[0]);
      if (ans == JOptionPane.CANCEL_OPTION || ans == JOptionPane.CLOSED_OPTION) {
        return false;
      } else if (ans == JOptionPane.YES_OPTION) {
        String fileSaved = saveTabContent();
        if (fileSaved == null) {
          return false;
        }
        if (getImageFolder() != null) {
          ImagePath.remove(getImagePath());
        }
        SikulixIDE.get().setCurrentFileTabTitle(fileSaved);
      } else {
        setDirty(false);
      }
    }
    return true;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="25 Transfer code incl. images between code panes">
  private class MyTransferHandler extends TransferHandler {

    private static final String me = "EditorPaneTransferHandler: ";

    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
      super.exportToClipboard(comp, clip, action);
    }

    @Override
    protected void exportDone(JComponent source,
                              Transferable data,
                              int action) {
      if (action == TransferHandler.MOVE) {
        JTextPane aTextPane = (JTextPane) source;
        int sel_start = aTextPane.getSelectionStart();
        int sel_end = aTextPane.getSelectionEnd();
        Document doc = aTextPane.getDocument();
        try {
          doc.remove(sel_start, sel_end - sel_start);
        } catch (BadLocationException e) {
          log(-1, "MyTransferHandler: exportDone: Problem while trying to remove text\n%s", e.getMessage());
        }
      }
    }

    @Override
    public int getSourceActions(JComponent c) {
      return COPY_OR_MOVE;
    }

    private Map<String, String> getCopiedImgs() {
      return copiedImgs;
    }

    private Map<String, String> copiedImgs = new HashMap<String, String>();

    @Override
    protected Transferable createTransferable(JComponent c) {
      Map<String, String> _copiedImgs = getCopiedImgs();
      JTextPane aTextPane = (JTextPane) c;

      SikuliEditorKit kit = ((SikuliEditorKit) aTextPane.getEditorKit());
      Document doc = aTextPane.getDocument();
      int sel_start = aTextPane.getSelectionStart();
      int sel_end = aTextPane.getSelectionEnd();

      StringWriter writer = new StringWriter();
      try {
        _copiedImgs.clear();
        kit.write(writer, doc, sel_start, sel_end - sel_start, _copiedImgs);
        StringSelection copiedString = new StringSelection(writer.toString());
        return copiedString;
      } catch (Exception e) {
        log(-1, "MyTransferHandler: createTransferable: Problem creating text to copy\n%s", e.getMessage());
      }
      return null;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      for (int i = 0; i < transferFlavors.length; i++) {
        //System.out.println(transferFlavors[i]);
        if (transferFlavors[i].equals(DataFlavor.stringFlavor)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
      Map<String, String> _copiedImgs = getCopiedImgs();
      DataFlavor htmlFlavor = DataFlavor.stringFlavor;
      if (canImport(comp, t.getTransferDataFlavors())) {
        try {
          String transferString = (String) t.getTransferData(htmlFlavor);
          EditorPane targetTextPane = (EditorPane) comp;
          for (Map.Entry<String, String> entry : _copiedImgs.entrySet()) {
            String imgName = entry.getKey();
            String imgPath = entry.getValue();
            File destFile = targetTextPane.copyFileToBundle(imgPath);
            String newName = destFile.getName();
            if (!newName.equals(imgName)) {
              String ptnImgName = "\"" + imgName + "\"";
              newName = "\"" + newName + "\"";
              transferString = transferString.replaceAll(ptnImgName, newName);
              Debug.info("MyTransferHandler: importData:" + ptnImgName + " exists. Renamed to " + newName);
            }
          }
          targetTextPane.insertString(transferString);
        } catch (Exception e) {
          log(-1, "MyTransferHandler: importData: Problem pasting text\n%s", e.getMessage());
        }
        return true;
      }
      return false;
    }
  }

  public File copyFileToBundle(String filename) {
    File f = new File(filename);
    String bundlePath = getSrcBundle();
    if (f.exists()) {
      try {
        File newFile = FileManager.smartCopy(filename, bundlePath);
        return newFile;
      } catch (IOException e) {
        log(-1, "copyFileToBundle: Problem while trying to save %s\n%s",
            filename, e.getMessage());
        return f;
      }
    }
    return null;
  }

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="27 feature search">
  /*
   * public int search(Pattern pattern){
   * return search(pattern, true);
   * }
   *
   * public int search(Pattern pattern, boolean forward){
   * if(!pattern.equals(_lastSearchPattern)){
   * _lastSearchPattern = pattern;
   * Document doc = getDocument();
   * int pos = getCaretPosition();
   * log("caret: "  + pos);
   * try{
   * String body = doc.getText(pos, doc.getLength()-pos);
   * _lastSearchMatcher = pattern.matcher(body);
   * }
   * catch(BadLocationException e){
   * e.printStackTrace();
   * }
   * }
   * return continueSearch(forward);
   * }
   */

  /*
   * public int search(String str){
   * return search(str, true);
   * }
   */
  public int search(String str, int pos, boolean forward) {
    boolean isCaseSensitive = true;
    String toSearch = str;
    if (str.startsWith("!")) {
      str = str.substring(1).toUpperCase();
      isCaseSensitive = false;
    }
    int ret = -1;
    Document doc = getDocument();
    log(4, "search: %s from %d forward: %s", str, pos, forward);
    try {
      String body;
      int begin;
      if (forward) {
        int len = doc.getLength() - pos;
        body = doc.getText(pos, len > 0 ? len : 0);
        begin = pos;
      } else {
        body = doc.getText(0, pos);
        begin = 0;
      }
      if (!isCaseSensitive) {
        body = body.toUpperCase();
      }
      Pattern pattern = Pattern.compile(Pattern.quote(str));
      Matcher matcher = pattern.matcher(body);
      ret = continueSearch(matcher, begin, forward);
      if (ret < 0) {
        if (forward && pos != 0) {
          return search(toSearch, 0, forward);
        }
        if (!forward && pos != doc.getLength()) {
          return search(toSearch, doc.getLength(), forward);
        }
      }
    } catch (BadLocationException e) {
      //log(-1, "search: did not work:\n" + e.getStackTrace());
      ret = -1;
    }
    return ret;
  }

  protected int continueSearch(Matcher matcher, int pos, boolean forward) {
    boolean hasNext = false;
    int start = 0, end = 0;
    if (!forward) {
      while (matcher.find()) {
        hasNext = true;
        start = matcher.start();
        end = matcher.end();
      }
    } else {
      hasNext = matcher.find();
      if (!hasNext) {
        return -1;
      }
      start = matcher.start();
      end = matcher.end();
    }
    if (hasNext) {
      Document doc = getDocument();
      getCaret().setDot(pos + end);
      getCaret().moveDot(pos + start);
      getCaret().setSelectionVisible(true);
      return pos + start;
    }
    return -1;
  }
  //</editor-fold>

  //<editor-fold desc="30 run lines">
  public void runSelection() {
    int start = getSelectionStart();
    int end = getSelectionEnd();
    if (start == end) {
      runLines(getLineTextAtCaret().trim());
    } else {
       runLines(getLinesFromSelection(start, end));
    }
  }

  private String getLinesFromSelection(int start, int end) {
    String lines = "";
    String[] scriptLines = getText().split("\n");
    int startLine = getLineNumberAtCaret(start) - 1;
    int endLine = Math.min(getLineNumberAtCaret(end), scriptLines.length);
    for (int i = startLine; i < endLine; i++) {
      lines += scriptLines[i] + "\n";
    }
    return lines;
  }

  public String getLines(int current, Boolean selection) {
    String lines = "";
    String[] scriptLines = getText().split("\n");
    if (selection == null) {
      lines = scriptLines[current - 1].trim();
    } else if (selection) {
      for (int i = current - 1; i < scriptLines.length; i++) {
        lines += scriptLines[i] + "\n";
      }
    } else {
      for (int i = 0; i < current; i++) {
        lines += scriptLines[i] + "\n";
      }
    }
    return lines;
  }

  public void runLines(String lines) {
    SikulixIDE.doHide();
    new Thread(new Runnable() {
      @Override
      public void run() {
        SikulixIDE ide = SikulixIDE.get();

        try {
          ide.setCurrentRunner(editorPane.editorPaneRunner);
          ide.setCurrentScript(editorPane.getCurrentFile());
          //TODO ? ide.setIsRunningScript(true);
          ide.clearMessageArea();
          ide.resetErrorMark();

          if (hasIDESupport()) {
            editorPane.editorPaneRunner.runLines(getEditorPaneIDESupport().normalizePartialScript(lines), null);
          } else {
            editorPane.editorPaneRunner.runLines(lines, null);
          }
        } finally {
          SikulixIDE.showAgain();
          ide.setCurrentRunner(null);
          ide.setCurrentScript(null);
          //TODO ? ide.setIsRunningScript(false);
        }
      }
    }).start();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="90 not used">
  private String _tabString = "   ";

  private void setTabSize(int charactersPerTab) {
    FontMetrics fm = this.getFontMetrics(this.getFont());
    int charWidth = fm.charWidth('w');
    int tabWidth = charWidth * charactersPerTab;

    TabStop[] tabs = new TabStop[10];

    for (int j = 0; j < tabs.length; j++) {
      int tab = j + 1;
      tabs[j] = new TabStop(tab * tabWidth);
    }

    TabSet tabSet = new TabSet(tabs);
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    StyleConstants.setFontSize(attributes, 18);
    StyleConstants.setFontFamily(attributes, "Osaka-Mono");
    StyleConstants.setTabSet(attributes, tabSet);
    int length = getDocument().getLength();
    getStyledDocument().setParagraphAttributes(0, length, attributes, true);
  }

  private void setTabs(int spaceForTab) {
    String t = "";
    for (int i = 0; i < spaceForTab; i++) {
      t += " ";
    }
    _tabString = t;
  }

  private void expandTab() throws BadLocationException {
    int pos = getCaretPosition();
    Document doc = getDocument();
    doc.remove(pos - 1, 1);
    doc.insertString(pos - 1, _tabString, null);
  }

  private Class _historyBtnClass;

  private void setHistoryCaptureButton(ButtonCapture btn) {
    _historyBtnClass = btn.getClass();
  }

  private void indent(int startLine, int endLine, int level) {
    Document doc = getDocument();
    String strIndent = "";
    if (level > 0) {
      for (int i = 0; i < level; i++) {
        strIndent += "  ";
      }
    } else {
      Debug.error("negative indentation not supported yet!!");
    }
    for (int i = startLine; i < endLine; i++) {
      try {
        int off = getLineStartOffset(i);
        if (level > 0) {
          doc.insertString(off, strIndent, null);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void checkCompletion(java.awt.event.KeyEvent ke) throws BadLocationException {
    Document doc = getDocument();
    Element root = doc.getDefaultRootElement();
    int pos = getCaretPosition();
    int lineIdx = root.getElementIndex(pos);
    Element line = root.getElement(lineIdx);
    int start = line.getStartOffset(), len = line.getEndOffset() - start;
    String strLine = doc.getText(start, len - 1);
    log(9, "[" + strLine + "]");
    if (strLine.endsWith("find") && ke.getKeyChar() == '(') {
      ke.consume();
      doc.insertString(pos, "(", null);
      ButtonCapture btnCapture = new ButtonCapture(this, line);
      insertComponent(btnCapture);
      doc.insertString(pos + 2, ")", null);
    }

  }
  //</editor-fold>
}
