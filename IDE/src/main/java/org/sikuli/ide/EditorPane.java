/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.PreferencesUser;
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
import org.sikuli.script.*;
import org.sikuli.script.runnerSupport.IScriptRunner;
import org.sikuli.script.runnerSupport.IScriptRunner.EffectiveRunner;
import org.sikuli.script.runnerSupport.Runner;
import org.sikuli.script.runners.JythonRunner;
import org.sikuli.script.runners.PythonRunner;
import org.sikuli.script.runners.TextRunner;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.generators.ICodeGenerator;
import org.sikuli.script.support.generators.JythonCodeGenerator;
import org.sikuli.script.support.gui.SikuliIDEI18N;
import org.sikuli.util.SikulixFileChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.Element;
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
  static final String me = "EditorPane: ";

  private static void log(String message, Object... args) {
    Debug.logx(3, me + message, args);
  }

  private static void trace(String message, Object... args) {
    Debug.logx(4, me + message, args);
  }

  private static void error(String message, Object... args) {
    Debug.logx(-1, me + message, args);
  }

  //for debugging watches
  EditorPane editorPane;
  SikulixIDE.PaneContext context;

  EditorPane() {
    addMouseListener(new MouseInputAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          new Thread(() -> handlePopup()).start();
        }
        super.mouseClicked(e);
      }
    });
    scrollPane = new JScrollPane(this);
    scrollPane.setRowHeaderView(new EditorLineNumberView(this));
    editorPaneID = new Date().getTime();
    editorPane = this;
    setTransferHandler(new MyTransferHandler());
    lineHighlighter = new EditorCurrentLineHighlighter(this);
    addCaretListener(lineHighlighter);
    initKeyMap();
    log("created %d", editorPaneID);
  }

  EditorPane(SikulixIDE.PaneContext context) {
    this();
    this.context = context;
  }

  public void makeReady() {
    editorPaneRunner = context.getRunner();
    paneType = context.getType();
    indentationLogic = null;
    IIDESupport paneSupport = context.getSupport();
    if (null != paneSupport) {
      try {
        indentationLogic = paneSupport.getIndentationLogic();
        indentationLogic.setTabWidth(PreferencesUser.get().getTabWidth());
      } catch (Exception ex) {
      }
      codeGenerator = paneSupport.getCodeGenerator();
    } else {
      // Take Jython generator if no IDESupport is available
      // TODO Needs better implementation
      codeGenerator = new JythonCodeGenerator();
    }

    if (paneType != null) {
      SikuliEditorKit editorKit = new SikuliEditorKit();
      setEditorKit(editorKit);
      setContentType(paneType);

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

    popMenuImage = new SikuliIDEPopUpMenu("POP_IMAGE", this);
    if (!popMenuImage.isValidMenu()) {
      popMenuImage = null;
    }
    popMenuImage = null; //TODO poMenu

    popMenuCompletion = new SikuliIDEPopUpMenu("POP_COMPLETION", this);
    if (!popMenuCompletion.isValidMenu()) {
      popMenuCompletion = null;
    }
    popMenuCompletion = null; //TODO popMenu

    setFont(new Font(PreferencesUser.get().getFontName(), Font.PLAIN, PreferencesUser.get().getFontSize()));
    setMargin(new Insets(3, 3, 3, 3));
    setBackground(Color.WHITE);
    if (!Settings.isMac()) {
      setSelectionColor(new Color(170, 200, 255));
    }

    SikulixIDE.getStatusbar().setType(paneType);
    log("InitTab: (%s)", paneType);
  }

  public void loadContent(InputStreamReader isr) throws IOException {
    read(new BufferedReader(isr), null);
    getDocument().addDocumentListener(new DirtyHandler());
    getDocument().addUndoableEditListener(getUndoRedo());
  }

  EditorPaneUndoRedo getUndoRedo() {
    return undoRedo;
  }

  private EditorPaneUndoRedo undoRedo = new EditorPaneUndoRedo();

  JScrollPane getScrollPane() {
    return scrollPane;
  }

  JScrollPane scrollPane;

  private EditorCurrentLineHighlighter lineHighlighter = null;

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
    trace("text popup");
  }

  void updateDocumentListeners(String source) {
    trace("updateDocumentListeners from: %s", source);
//    getDocument().addDocumentListener(getDirtyHandler());
//    getDocument().addUndoableEditListener(getUndoRedo());
//    SikulixIDE.getStatusbar().setType(paneType);
  }

  IIndentationLogic getIndentationLogic() {
    return indentationLogic;
  }

  private IIndentationLogic indentationLogic = null;

  ICodeGenerator getCodeGenerator() {
    return codeGenerator;
  }

  private ICodeGenerator codeGenerator = null;

  private void initKeyMap() {
    InputMap map = this.getInputMap();
    int shift = InputEvent.SHIFT_DOWN_MASK;
    int ctrl = InputEvent.CTRL_DOWN_MASK;
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, shift), SikuliEditorKit.deIndentAction);
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ctrl), SikuliEditorKit.completionAction);
  }
  //</editor-fold>

  //<editor-fold desc="10 load content">
  boolean init() { //TODO
    makeReady();
    return true;
  }

//TODO  private int alreadyOpen(String fileSelected, int currentTab) {

//    CloseableTabbedPane tabs = getTabs();
//    int nTab = tabs.getTabCount();
//    if (nTab > 0) {
//      File possibleBundle = new File(fileSelected);
//      String possibleBundlePath = null;
//      if (EditorPane.isInBundle(possibleBundle)) {
//        possibleBundlePath = FilenameUtils.removeExtension(possibleBundle.getParent());
//      } else if (possibleBundle.isDirectory()) {
//        possibleBundlePath = FilenameUtils.removeExtension(possibleBundle.getPath());
//      }
//      for (int iTab = 0; iTab < nTab; iTab++) {
//        if (currentTab > -1 && iTab == currentTab) {
//          continue;
//        }
//        EditorPane checkedPane = getPaneAtIndex(iTab);
//        String paneFile = checkedPane.editorPaneFileSelected;
//        if (null == paneFile) continue;
//        if (new File(paneFile).equals(new File(fileSelected))) {
//          tabs.setAlreadyOpen(iTab);
//          return iTab;
//        }
//        if (possibleBundlePath != null && (checkedPane.isBundle() || checkedPane.isInBundle())) {
//          String paneBundle = FilenameUtils.removeExtension(checkedPane.editorPaneFolder.getPath());
//          if (possibleBundlePath.equals(paneBundle)) {
//            tabs.setAlreadyOpen(iTab);
//            return iTab;
//          }
//        }
//      }
//    }
//    return -1;
//  }

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

  private boolean readContent(String script) {
    InputStreamReader isr;
    try {
      isr = new InputStreamReader(new ByteArrayInputStream(script.getBytes(Charset.forName("utf-8"))), Charset.forName("utf-8"));
      read(new BufferedReader(isr), null);
    } catch (Exception ex) {
      error("readContent: from String (%s)", ex.getMessage());
      return false;
    }
    return true;
  }

  public boolean isText() {
    return paneType == TextRunner.TYPE;
  }

  public boolean isPython() {
    return paneType == JythonRunner.TYPE || paneType == PythonRunner.TYPE;
  }
  //</editor-fold>

  //<editor-fold desc="15 content file">
  public File getScreenshotImageFile(String imageName) {
    if (!imageName.endsWith(".png")) {
      imageName += ".png";
    }
    File screenshotDir = new File(getImagePath(), ImagePath.SCREENSHOT_DIRECTORY);
    return new File(screenshotDir, imageName);
  }

  public boolean hasScreenshotImage(String imageName) {
    return getScreenshotImageFile(imageName).exists();
  }

  public ScreenImage getScreenshotImage(String imageName) {
    File screenshotFile = getScreenshotImageFile(imageName);
    if (screenshotFile.exists()) {
      return new ScreenImage(screenshotFile);
    }
    return null;
  }

  public void setTemp(boolean temp) {
    editorPaneIsTemp = temp;
  }

  public boolean isTemp() {
    return editorPaneIsTemp;
  }

  private boolean editorPaneIsTemp = false;

  boolean isInBundle() {
    return isInBundle(editorPaneFileToRun);
  }

  static boolean isInBundle(File file) {
    String possibleBundleName = FilenameUtils.removeExtension(file.getName()).toLowerCase();
    String folderOrBundleName = FilenameUtils.removeExtension(file.getParentFile().getName()).toLowerCase();
    return possibleBundleName.equals(folderOrBundleName);
  }

  static boolean isPossibleBundle(String fileName) {
    if (FilenameUtils.getExtension(fileName).isEmpty() ||
            FilenameUtils.getExtension(fileName).equals("sikuli")) {
      return true;
    }
    return false;
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
    setFiles(editorPaneFile, null);
  }

  public void setFiles(File paneFile, String paneFileSelected) {
    if (paneFile == null) {
      return;
    }
    editorPaneFileSelected = paneFileSelected;
    editorPaneFile = paneFile;
    editorPaneFolder = paneFile.getParentFile();
    setImageFolder(editorPaneFolder);
    if (null != paneFileSelected) {
      trace("setFiles: for: %s", paneFileSelected);
    } else {
      if (!isTemp()) {
        setIsFile();
        editorPaneFileSelected = paneFile.getAbsolutePath();
        editorPaneFileToRun = paneFile;
        trace("setFiles: for: %s", paneFile);
      }
    }
  }

  private void changeFiles() {
    String extension = editorPaneRunner.getDefaultExtension();
    setFiles(changeExtension(editorPaneFileToRun, extension));
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
    return paneType;
  }

  void setType(String editorPaneType) {
    this.paneType = editorPaneType;
  }

  private String paneType;

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

  public String getCurrentScriptname() {
    return FilenameUtils.getBaseName(getCurrentShortFilename());
  }

  public File saveAndGetCurrentFile() {
    if (hasEditingFile() && isDirty()) {
      //TODO saveAsSelect();
    }
    return editorPaneFile;
  }

  public File getCurrentFile() {
    return editorPaneFile;
  }
  //</editor-fold>

  //<editor-fold desc="16 image path">
  public File getImageFolder() {
    return context.getImageFolder();
  }

  public void setImageFolder(File imageFolder) {
    context.setImageFolder(imageFolder);
  }

  public String getImagePath() {
    return context.getImageFolder().getAbsolutePath();
  }

  public String getScreenshotFolder() {
    return context.getScreenshotFolder().getAbsolutePath();
  }

  public void setBundleFolder() {
    ImagePath.setBundleFolder(getImageFolder());
  }

  public String getSrcBundle() {
    return context.getFolder().getAbsolutePath();
  }

  public String getBundlePath() {
    return context.getImageFolder().getAbsolutePath();
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
    trace("jumpTo pos: " + lineNo + "," + column);
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
    trace("jumpTo line: " + lineNo);
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
        error("insertString: Problem while trying to insert\n%s", e.getMessage());
      }
    }
    int pos = getCaretPosition();
    insertString(pos, str);
    int new_pos = getCaretPosition();
    int end = parseRange(pos, new_pos);
    setCaretPosition(end);
  }

  public String getRegionString(int x, int y, int w, int h) {
    return String.format("Region(%d,%d,%d,%d)", x, y, w, h);
  }

  public void insertRegionString(int x, int y, int w, int h) {
    insertString(getRegionString(x, y, w, h));
  }

  public String getPatternString(org.sikuli.script.Image img, double sim, Location off, float rFactor, String mask) {
    return codeGenerator.pattern(org.sikuli.script.Pattern.make(img, sim, off, rFactor, mask));
  }

  private void insertString(int pos, String str) {
    Document doc = getDocument();
    try {
      doc.insertString(pos, str, null);
    } catch (Exception e) {
      error("insertString: Problem while trying to insert at pos\n%s", e.getMessage());
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
      error("appendString: Problem while trying to append\n%s", e.getMessage());
    }
  }

  //</editor-fold>

  public void doParse() {
    Document doc = getDocument();
    Element root = doc.getDefaultRootElement();
    parse(root);
  }

  public void doReparse() {
    saveCaretPosition();
    readContent(getText());
    updateDocumentListeners("reparse");
    doParse();
    restoreCaretPosition();
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

  private void parse(Element node) {
    if (!context.getShowThumbs()) {
      // do not show any thumbnails
      return;
    }
    int count = node.getElementCount();
    for (int i = 0; i < count; i++) {
      Element elm = node.getElement(i);
      if (elm.isLeaf()) {
        parseRange(elm.getStartOffset(), elm.getEndOffset());
      } else {
        parse(elm);
      }
    }
  }

  private int parseRange(int start, int end) {
    if (!context.getShowThumbs()) {
      // do not show any thumbnails
      return end;
    }
    try {
      end = parseLine(start, end, patCaptureBtn);
      end = parseLine(start, end, patPatternStr);
      end = parseLine(start, end, patRegionStr);
      end = parseLine(start, end, patPngStr);
    } catch (BadLocationException e) {
      error("parseRange: Problem while trying to parse line\n%s", e.getMessage());
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

  //<editor-fold defaultstate="collapsed" desc="19 replace text patterns with image buttons">
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
    File imageFolder = context.getImageFolder();
    trace("parseforImages: in %s", imageFolder);
    String scriptText = getText();
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

  private void parseforImagesCollect(File imageFolder, String img, int pos, Map<String, List<Integer>> images) {
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
  String uncompleteStringError = "uncomplete_string_error";

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
    return context.isDirty();
  }

  public void setDirty(boolean flag) {
    if (flag) {
      context.setDirty();
    } else {
      context.notDirty();
    }
  }

  private DirtyHandler getDirtyHandler() {
    if (dirtyHandler == null) {
      dirtyHandler = new DirtyHandler();
    }
    return dirtyHandler;
  }

  private DirtyHandler dirtyHandler;

  private class DirtyHandler implements DocumentListener {

    @Override
    public void changedUpdate(DocumentEvent ev) {
      trace("change update");
      //setDirty(true);
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
      trace("insert update");
      setDirty(true);
    }

    @Override
    public void removeUpdate(DocumentEvent ev) {
      trace("remove update");
      setDirty(true);
    }
  }
  //</editor-fold>

  //<editor-fold desc="22 save, close">
  private boolean writeSriptFile() {
    trace("writeSrcFile: " + editorPaneFile);
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
          error("Problem while trying to create HTML: %s", ex.getMessage());
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
        if (!paneType.equals(JythonRunner.TYPE)) {
          trace("delete-not-used-images for %s using Python string syntax", paneType);
        }
        try {
          cleanBundle();
        } catch (Exception ex) {
          error("Problem while trying to clean bundle (not used images): %s", ex.getMessage());
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
        trace("Exported packed SikuliX Script to:\n%s", zipPath);
      } catch (Exception ex) {
        error("Exporting packed SikuliX Script did not work:\n%s", zipPath);
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
    trace("cleanBundle");
    Set<String> foundImages = parseforImages().keySet();
    if (foundImages.contains(uncompleteStringError)) {
      error("cleanBundle aborted (%s)", uncompleteStringError);
    } else {
      FileManager.deleteNotUsedImages(getBundlePath(), foundImages);
      trace("cleanBundle finished");
    }
    FileManager.deleteNotUsedScreenshots(getBundlePath(), foundImages);
    trace("cleanBundle finished: %s", getCurrentScriptname());
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
          error("MyTransferHandler: exportDone: Problem while trying to remove text\n%s", e.getMessage());
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
        error("MyTransferHandler: createTransferable: Problem creating text to copy\n%s", e.getMessage());
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
          error("MyTransferHandler: importData: Problem pasting text\n%s", e.getMessage());
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
        error("copyFileToBundle: Problem while trying to save %s\n%s",
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
   * trace("caret: "  + pos);
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
    trace("search: %s from %d forward: %s", str, pos, forward);
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
      //error("search: did not work:\n" + e.getStackTrace());
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
    if (lines.startsWith(" ") || lines.startsWith("\t ")) {
    }
    SikulixIDE.doHide();
    new Thread(new Runnable() {
      @Override
      public void run() {
        SikulixIDE ide = SikulixIDE.get();

        try {
          ide.setCurrentRunner(editorPane.editorPaneRunner);
          ide.setCurrentScript(editorPane.getCurrentFile());
          ide.setIsRunningScript(true);
          ide.clearMessageArea();
          ide.resetErrorMark();

          if (null != context.getSupport()) {
            editorPane.editorPaneRunner.runLines(context.getSupport().normalizePartialScript(lines), null);
          } else {
            editorPane.editorPaneRunner.runLines(lines, null);
          }
        } finally {
          SikulixIDE.showAgain();
          ide.setCurrentRunner(null);
          ide.setCurrentScript(null);
          ide.setIsRunningScript(false);
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
    trace("[" + strLine + "]");
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
