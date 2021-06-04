/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.basics.Settings;
import org.sikuli.support.ide.ExtensionManager;
import org.sikuli.support.ide.IIDESupport;
import org.sikuli.support.ide.IIndentationLogic;
import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.ScreenImage;
import org.sikuli.support.runner.IRunner;
import org.sikuli.support.runner.JythonRunner;
import org.sikuli.support.runner.PythonRunner;
import org.sikuli.support.runner.TextRunner;
import org.sikuli.support.Commons;
import org.sikuli.support.recorder.generators.ICodeGenerator;
import org.sikuli.support.recorder.generators.JythonCodeGenerator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  long editorPaneID = 0; //TODO needed?

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

  boolean init() { //TODO needed?
    makeReady();
    return true;
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

  void updateDocumentListeners(String source) { //TODO when is it needed?
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
  public IRunner getRunner() {
    return editorPaneRunner;
  }

  IRunner editorPaneRunner = null;

  File editorPaneFileToRun = null;

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
    File screenshotDir = context.getScreenshotFolder();
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
      File f = context.getFolder();
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

  public String parseLineText(String line) {
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

    private Map<String, String> copiedImgs = new HashMap<>();

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
        String transferString = null;
        String msg = "";
        try {
          transferString = (String) t.getTransferData(htmlFlavor);
        } catch (UnsupportedFlavorException e) {
          msg = e.getMessage();
        } catch (IOException e) {
          msg = e.getMessage();
        }
        if (transferString == null) {
          log("ERROR: MyTransferHandler: importData: getTransferData: %s", msg);
        }
        EditorPane targetPane = (EditorPane) comp;
        for (Map.Entry<String, String> entry : _copiedImgs.entrySet()) {
          String imgName = entry.getKey();
          String imgPath = entry.getValue();
          File destFile = Commons.smartCopy(new File(imgPath), context.getImageFolder());
          if (destFile != null) {
            String newName = destFile.getName();
            if (!newName.equals(imgName)) {
              String ptnImgName = "\"" + imgName + "\"";
              newName = "\"" + newName + "\"";
              transferString = transferString.replaceAll(ptnImgName, newName);
              log("MyTransferHandler: importData: image renamed: %s to %s", ptnImgName, newName);
            }
          }
        }
        targetPane.insertString(transferString);
        return true;
      }
      return false;
    }
  }

  public File copyFileToBundle(File file) {
    if (file.exists()) {
      File newFile = Commons.smartCopy(file, context.getImageFolder());
      return newFile;
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
    SikulixIDE.doHide();
    new Thread(new Runnable() {
      @Override
      public void run() {
        SikulixIDE ide = SikulixIDE.get();

        try {
          ide.clearMessageArea();
          ide.resetErrorMark();

          if (null != context.getSupport()) {
            context.getRunner().runLines(context.getSupport().normalizePartialScript(lines), null);
          } else {
            context.getRunner().runLines(lines, null);
          }
        } finally {
          SikulixIDE.showAgain();
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
      ButtonCapture btnCapture = null; //TODO new ButtonCapture(this, line);
      insertComponent(btnCapture);
      doc.insertString(pos + 2, ")", null);
    }

  }
  //</editor-fold>
}
