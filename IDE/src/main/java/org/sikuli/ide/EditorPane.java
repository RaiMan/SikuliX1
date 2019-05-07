/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.Settings;
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
import org.sikuli.script.Sikulix;
import org.sikuli.script.runners.JythonRunner;
import org.sikuli.script.runners.PythonRunner;
import org.sikuli.script.runners.TextRunner;
import org.sikuli.script.support.ExtensionManager;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.Runner;
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

  private void handlePopup() {
    log(3, "text popup");
  }

  private void updateDocumentListeners(String source) {
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
  void init(String scriptType) {
    if (null == scriptType) {
      runner = RunTime.getDefaultRunner();
    } else {
      runner = Runner.getRunner(scriptType);
    }
    setText("");
    initForScriptType();
  }

  public String selectFile(boolean accessingAsFile) {
    File file = new SikulixFileChooser(SikulixIDE.get(), accessingAsFile).load();
    if (file == null) {
      return null;
    }
    String fileSelected = FileManager.slashify(file.getAbsolutePath(), false);
    if (fileSelected.endsWith("###isText")) {
      fileSelected = fileSelected.replace("###isText", "");
      isText = true;
    }
    //int i = SikulixIDE.get().isAlreadyOpen(fname);
    if (alreadyOpen(fileSelected)) {
      log(lvl, "loadFile: Already open in IDE: " + fileSelected);
      return null;
    }
    loadFile(fileSelected);
    if (editorPaneFile == null) {
      return null;
    }
    return fileSelected;
  }

  private boolean alreadyOpen(String fileSelected) {
    CloseableTabbedPane tabs = getTabs();
    int nTab = tabs.getTabCount();
    if (nTab > 0) {
      for (int iTab = 0; iTab < nTab; iTab++) {
        String paneFile = getPaneAtIndex(iTab).editorPaneFileSelected;
        if (paneFile != null && new File(paneFile).equals(new File(fileSelected))) {
          tabs.setAlreadyOpen(iTab);
          return true;
        }
      }
    }
    return false;
  }

  EditorPane getPaneAtIndex(int index) {
    return (EditorPane) ((JScrollPane) getTabs().getComponentAt(index)).getViewport().getView();
  }

  private CloseableTabbedPane getTabs() {
    return SikulixIDE.get().getTabs();
  }

  public void loadFile(String filename) {
    log(lvl, "loadfile: %s", filename);
    File fileLoaded = null;
    filename = FileManager.slashify(filename, false);
    if (filename.endsWith("###isText")) {
      filename = filename.replace("###isText", "");
      isText = true;
    }
    File fileToLoad = new File(filename);
    if (filename.endsWith(".py")) {
      fileLoaded = fileToLoad;
      isPython = true;
    } else if (isText) {
      fileLoaded = fileToLoad;
    } else {
      fileLoaded = Runner.getScriptFile(fileToLoad);
    }
    if (fileLoaded != null) {
      if (isText) {
        scriptType = "txt";
        setSrcBundle(FileManager.slashify(fileLoaded.getParent(), true));
      } else {
        setSrcBundle(FileManager.slashify(fileLoaded.getParent(), true));
        scriptType = fileLoaded.getAbsolutePath().substring(fileLoaded.getAbsolutePath().lastIndexOf(".") + 1);
      }
      runner = Runner.getRunner(scriptType);
      initForScriptType();
      if (!readContent(fileLoaded)) {
        fileLoaded = null;
      } else {
        updateDocumentListeners("loadFile");
        checkSource();
        doParse();
        restoreCaretPosition();
        setDirty(false);
      }
    }
    setFile(fileLoaded, fileToLoad.getAbsolutePath());
  }

  private void initForScriptType() {
    String scrType = null;

    log(lvl, "doInit: %s", runner);
    // initialize runner to speed up first script run
    (new Thread() {
      @Override
      public void run() {
        runner.init(null);
      }
    }).start();

    scrType = runner.getType();
    if (!scrType.equals(editorPaneType)) {
      editorPaneType = scrType;
      indentationLogic = null;

      if (JythonRunner.TYPE.equals(editorPaneType) || PythonRunner.TYPE.equals(editorPaneType)) {
        IIDESupport ideSupport = SikulixIDE.getIDESupport(editorPaneType);
        indentationLogic = ideSupport.getIndentationLogic();
        indentationLogic.setTabWidth(PreferencesUser.get().getTabWidth());
      } else if (TextRunner.TYPE.equals(editorPaneType)) {
        isText = true;
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

      if (!isText) {
        popMenuImage = new SikuliIDEPopUpMenu("POP_IMAGE", this);
        if (!popMenuImage.isValidMenu()) {
          popMenuImage = null;
        }

        popMenuCompletion = new SikuliIDEPopUpMenu("POP_COMPLETION", this);
        if (!popMenuCompletion.isValidMenu()) {
          popMenuCompletion = null;
        }
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

  private IScriptRunner runner = null;
  private String scriptType = null;
  public boolean isPython = false;
  public boolean isText = false;
  //</editor-fold>

  //<editor-fold desc="11 check content">
  public void checkSource() {
    String scriptText = getText();
    if (!isBundle()) {
      if (isPython) {
        editorPaneType = JythonRunner.TYPE;
        if (ExtensionManager.hasPython()) {
          String intro = scriptText.substring(0, Math.min(20, scriptText.length())).trim().toUpperCase();
          if (intro.contains(ExtensionManager.shebangPython)) {
            editorPaneType = PythonRunner.TYPE;
          }
        }
      }
    }
    SikulixIDE.getStatusbar().setType(getType());
    Matcher matcher = patSetBundlePath.matcher(scriptText);
    if (matcher.find()) {
      String path = matcher.group(1);
      String msg = String.format("found in script: setBundlePath: %s", path);
      if (setImagePath(path)) {
        Debug.log(3, msg);
      } else {
        Debug.error(msg);
      }
    } else {
      setImagePath();
    }
  }

  static Pattern patSetBundlePath = Pattern.compile("setBundlePath.*?\\(.*?\"(.*?)\".*?\\)");
  //</editor-fold>

  //<editor-fold desc="15 content file">
  public void setTemp(boolean temp) {
    notYetSaved = temp;
  }

  public boolean isTemp() {
    return notYetSaved;
  }

  private boolean notYetSaved = false;

  boolean isBundle() {
    if (editorPaneIsBundle) {
      return true;
    }
    if (isPython || isText) {
      setIsNotBundle();
    } else {
      setIsBundle();
    }
    return editorPaneIsBundle;
  }

  public void setIsBundle() {
    editorPaneIsBundle = true;
  }

  public void setIsNotBundle() {
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

  public void setTempFile() {
    String editorPaneType = getType();
    String extension = Runner.getExtension(editorPaneType);
    File tempPath = new File(RunTime.get().fpBaseTempPath, "temp" + editorPaneID);
    File tempFile = FileManager.createTempFile(extension, tempPath.getAbsolutePath());
    setFile(tempFile);
    updateDocumentListeners("empty tab");
  }

  public void setFile(File editorPaneFile) {
    setFile(editorPaneFile, null);
  }

  public void setFile(File editorPaneFile, String editorPaneFileSelected) {
    if (editorPaneFile == null) {
      return;
    }
    this.editorPaneFile = editorPaneFile;
    editorPaneFolder = editorPaneFile.getParentFile();
    setImageFolder(editorPaneFolder);
    if (null != editorPaneFileSelected) {
      this.editorPaneFileSelected = editorPaneFileSelected;
    }
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

  public boolean hasEditingFile() {
    return editorPaneFile != null;
  }

  public void showType() {
    if (isPython) {
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
    if (isText) {
      return editorPaneFile.getName();
    }
    if (getSrcBundle() != null) {
      if (isPython) {
        return editorPaneFile.getName();
      } else {
        File f = new File(getSrcBundle());
        return f.getName();
      }
    }
    return "Untitled";
  }

  public File getCurrentFile() {
    return getCurrentFile(true);
  }

  public File getCurrentFile(boolean shouldSave) {
    if (shouldSave && hasEditingFile() && isDirty()) {
      saveAsSelect(Settings.isMac());
    }
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

  public void setImageFolder() {
    if (null != editorPaneImageFolder) {
      ImagePath.setBundlePath(editorPaneImageFolder.getAbsolutePath());
    }
  }

  public void setImageFolder(File imageFolder) {
    editorPaneImageFolder = imageFolder;
    ImagePath.setBundlePath(editorPaneImageFolder.getAbsolutePath());
  }

  File editorPaneImageFolder = null;

  public boolean setImagePath() {
    setImageFolder();
    return true;
  }

  public boolean setImagePath(String newBundlePath) {
    try {
      if (!new File(newBundlePath).isAbsolute()) {
        newBundlePath = new File(editorPaneFolder, newBundlePath).getCanonicalPath();
      }
      newBundlePath = FileManager.normalizeAbsolute(newBundlePath, false);
    } catch (Exception ex) {
      return false;
    }
    setImageFolder(new File(newBundlePath));
    return true;
  }

  private boolean setSrcBundle(String newBundlePath) {
    try {
      newBundlePath = FileManager.normalizeAbsolute(newBundlePath, false);
    } catch (Exception ex) {
      return false;
    }
    setImageFolder(new File(newBundlePath));
    return true;
  }

  public String getSrcBundle() {
    return editorPaneFolder.getAbsolutePath();
  }

  public String getBundlePath() {
    return editorPaneImageFolder.getAbsolutePath();
  }

  public boolean isSourceBundleTemp() {
    return notYetSaved;
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
      setCaretPosition(getDocument().getLength() - 1);
    }
    caretPosition = -1;
  }

  private int caretPosition = -1;

  public int getLineNumberAtCaret(int caretPosition) {
    Element root = getDocument().getDefaultRootElement();
    return root.getElementIndex(caretPosition) + 1;
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
    Element elem = getLineAtCaret(-1);
    Document doc = elem.getDocument();
    Element subElem;
    String text;
    String line = "";
    int start = elem.getStartOffset();
    int end = elem.getEndOffset();
    for (int i = 0; i < elem.getElementCount(); i++) {
      text = "";
      subElem = elem.getElement(i);
      start = subElem.getStartOffset();
      end = subElem.getEndOffset();
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
      Image.unCacheBundledImage(newName);
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

  public String getPatternString(String ifn, float sim, Location off, Image img, float resizeFactor, String mask) {
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
      if (sim >= 0.99F) {
        patternString += ".exact()";
      } else if (sim != 0.7F) {
        patternString += String.format(Locale.ENGLISH, ".similar(%.2f)", sim);
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
    parseforImagesWalk(pbundle, lexer, scriptText, 0, images, 0);
    log(3, "parseforImages finished");
    return images;
  }

  //TODO " and ' in comments - line numbers not reported correctly in case
  private void parseforImagesWalk(String pbundle, Lexer lexer,
                                  String text, int pos, Map<String, List<Integer>> images, Integer line) {
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
        line++;
        if (inString) {
          boolean answer = Sikulix.popAsk(String.format("Possible incomplete string in line %d\n" +
              "\"%s\"\n" +
              "Yes: No images will be deleted!\n" +
              "No: Ignore and continue", line, text), "Delete images on save");
          if (answer) {
            log(-1, "DeleteImagesOnSave: possible incomplete string in line %d", line);
            images.clear();
            images.put("uncomplete_comment_error", null);
          }
          break;
        }
      }
      if (t.getType() == TokenType.Comment) {
        //log(3, "parseforImagesWalk::Comment");
        innerText = t.getValue().substring(1);
        parseforImagesWalk(pbundle, lexer, innerText, t.getPos() + 1, images, line);
        continue;
      }
      if (t.getType() == TokenType.String_Doc) {
        //log(3, "parseforImagesWalk::String_Doc");
        innerText = t.getValue().substring(3, t.getValue().length() - 3);
        parseforImagesWalk(pbundle, lexer, innerText, t.getPos() + 3, images, line);
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
      return saveAsSelect(Settings.isMac());
    } else {
      if (writeSriptFile()) {
        return editorPaneFile.getAbsolutePath();
      }
      return null;
    }
  }

  public String saveAsSelect(boolean accessingAsFile) {
    SikulixFileChooser fileChooser = new SikulixFileChooser(SikulixIDE.get(), accessingAsFile);
    if (notYetSaved) {
      fileChooser.setUntitled();
      if (isText) {
        fileChooser.setText();
      }
    } else if (isPython) {
      fileChooser.setPython();
    } else if (isText) {
      fileChooser.setText();
    }
    File file = fileChooser.save();
    if (file == null) {
      return null;
    }
    String filename = file.getAbsolutePath();
    if (filename.endsWith(".py")) {
      isPython = true;
    }
    if (isText) {
      filename = filename.replace("###isText", "");
      if (!filename.endsWith(".txt")) {
        filename += ".txt";
      }
    }
    if (isBundle()) {
      if (!filename.endsWith(".sikuli")) {
        filename += ".sikuli";
      }
    }
    if (FileManager.exists(filename)) {
      int res = JOptionPane.showConfirmDialog(
          null, SikuliIDEI18N._I("msgFileExists", filename),
          SikuliIDEI18N._I("dlgFileExists"), JOptionPane.YES_NO_OPTION);
      if (res != JOptionPane.YES_OPTION) {
        return null;
      }
      FileManager.deleteFileOrFolder(filename);
    }
    File savedFile = null;
    if (isBundle()) {
      FileManager.mkdir(filename);
      savedFile = saveAsBundle(filename);
      if (Settings.isMac()) {
        if (!Settings.handlesMacBundles) {
          makeBundle(filename, accessingAsFile);
        }
      }
    } else {
      savedFile = saveAsFile(filename);
    }
    return savedFile.getAbsolutePath();
  }

  private void makeBundle(String path, boolean asFile) {
    String isBundle = asFile ? "B" : "b";
    String result = Sikulix.run(new String[]{"#SetFile", "-a", isBundle, path});
    if (!result.isEmpty()) {
      log(-1, "makeBundle: return: " + result);
    }
    if (asFile) {
      if (!FileManager.writeStringToFile("/Applications/SikuliX-IDE.app",
          (new File(path, ".LSOverride")).getAbsolutePath())) {
        log(-1, "makeBundle: not possible: .LSOverride");
      }
    } else {
      new File(path, ".LSOverride").delete();
    }
  }

  private File saveAsBundle(String targetFolder) {
    String extension = Runner.getExtension(editorPaneType);
    if (extension != null) {
      String sourceFolder = editorPaneFolder.getAbsolutePath();
      log(lvl, "saveAsBundle: " + sourceFolder);
      targetFolder = FileManager.slashify(targetFolder, true);
      if (!IDESupport.transferScript(sourceFolder, targetFolder, getRunner())) {
        log(-1, "saveAsBundle: did not work");
      }
      ImagePath.remove(getImagePath());
      if (notYetSaved) {
        FileManager.deleteTempDir(sourceFolder);
        notYetSaved = false;
      }
      String name = new File(targetFolder).getName();
      name = name.substring(0, name.lastIndexOf("."));
      File scriptFile = new File(targetFolder, name + "." + extension);
      setFile(scriptFile, targetFolder);
      if (writeSriptFile()) {
        checkSource();
        doReparse();
        return editorPaneFolder;
      }
    }
    return null;
  }

  private File saveAsFile(String filename) {
    log(lvl, "saveAsFile: " + filename);
    setFile(new File(filename));
    if (writeSriptFile()) {
      checkSource();
      doReparse();
      return editorPaneFolder;
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
    if (isPython) {
      chooser.setPython();
    } else if (isText) {
      chooser.setText();
    }
    File file = chooser.export();
    if (file == null) {
      return null;
    }
    String zipPath = file.getAbsolutePath();
    if (isPython || isText) {
      if (!file.getAbsolutePath().endsWith(".zip")) {
        zipPath += ".zip";
      }
    } else {
      if (!file.getAbsolutePath().endsWith(".skl")) {
        zipPath += ".skl";
      }
    }
    if (new File(zipPath).exists()) {
      if (!Sikulix.popAsk(String.format("Overwrite existing file?\n%s", zipPath),
          "Exporting packed SikuliX Script")) {
        return null;
      }
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
    if (foundImages.contains("uncomplete_comment_error")) {
      log(-1, "cleanBundle aborted (uncomplete_comment_error)");
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
    Map<String, String> _copiedImgs = new HashMap<String, String>();

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

    @Override
    protected Transferable createTransferable(JComponent c) {
      JTextPane aTextPane = (JTextPane) c;

      SikuliEditorKit kit = ((SikuliEditorKit) aTextPane.getEditorKit());
      Document doc = aTextPane.getDocument();
      int sel_start = aTextPane.getSelectionStart();
      int sel_end = aTextPane.getSelectionEnd();

      StringWriter writer = new StringWriter();
      try {
        _copiedImgs.clear();
        kit.write(writer, doc, sel_start, sel_end - sel_start, _copiedImgs);
        return new StringSelection(writer.toString());
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
              Debug.info("MyTransferHandler: importData:" + ptnImgName + " exists. Rename it to " + newName);
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
   * Debug.log("caret: "  + pos);
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
    Debug.log(4, "search: %s from %d forward: %s", str, pos, forward);
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
      runLines(getSelectedText());
    }
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
    if (lines.startsWith(" ") || lines.startsWith("\t ")) {
    }
    SikulixIDE.get().setVisible(false);
    new Thread(new Runnable() {
      @Override
      public void run() {
        getRunner().runLines(lines, null);
        SikulixIDE.showAgain();
      }
    }).start();
  }

  public IScriptRunner getRunner() {
    IScriptRunner runner = Runner.getRunner(getType());
    return runner;
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
    Debug.log(9, "[" + strLine + "]");
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
