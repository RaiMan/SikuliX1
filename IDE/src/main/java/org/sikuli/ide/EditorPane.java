/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.idesupport.IIndentationLogic;
import org.sikuli.script.Location;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Runner;
import org.sikuli.script.Sikulix;
import org.sikuli.scriptrunner.IScriptRunner;
import org.sikuli.scriptrunner.ScriptingSupport;
import org.sikuli.syntaxhighlight.ResolutionException;
import org.sikuli.syntaxhighlight.grammar.Lexer;
import org.sikuli.syntaxhighlight.grammar.Token;
import org.sikuli.syntaxhighlight.grammar.TokenType;
import org.sikuli.util.SikulixFileChooser;

public class EditorPane extends JTextPane implements KeyListener, CaretListener {

  private static final String me = "EditorPane: ";
  private static final int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static TransferHandler transferHandler = null;
  private static final Map<String, Lexer> lexers = new HashMap<String, Lexer>();
  private PreferencesUser pref;

  private File scriptSource = null;
  private File scriptParent = null;
  private File scriptImages = null;
  private String scriptType = null;
  private boolean scriptIsTemp = false;

  private boolean scriptIsDirty = false;
  private DirtyHandler dirtyHandler;

  private File _editingFile;
  //	private String scriptType = null;
  private String _srcBundlePath = null;
  private boolean _srcBundleTemp = false;
  private String sikuliContentType;

  private EditorCurrentLineHighlighter _highlighter = null;
  private EditorUndoManager _undo = null;
  // TODO: move to SikuliDocument ????
  private IIndentationLogic _indentationLogic = null;

  private boolean hasErrorHighlight = false;

  public boolean showThumbs;
  static Pattern patPngStr = Pattern.compile("(\"[^\"]+?\\.(?i)(png|jpg|jpeg)\")");
  static Pattern patCaptureBtn = Pattern.compile("(\"__CLICK-TO-CAPTURE__\")");
  static Pattern patPatternStr = Pattern.compile(
          "\\b(Pattern\\s*\\(\".*?\"\\)(\\.\\w+\\([^)]*\\))+)");
  static Pattern patRegionStr = Pattern.compile(
          "\\b(Region\\s*\\((-?[\\d\\s],?)+\\))");
  static Pattern patLocationStr = Pattern.compile(
          "\\b(Location\\s*\\((-?[\\d\\s],?)+\\))");

  //TODO what is it for???
  private int _caret_last_x = -1;
  private boolean _can_update_caret_last_x = true;
  private SikuliIDEPopUpMenu popMenuImage;
  private SikuliEditorKit editorKit;
  private EditorViewFactory editorViewFactory;
  private SikuliIDE sikuliIDE = null;
  private int caretPosition = -1;

  //<editor-fold defaultstate="collapsed" desc="Initialization">
  public EditorPane(SikuliIDE ide) {
    pref = PreferencesUser.getInstance();
    showThumbs = !pref.getPrefMorePlainText();
    sikuliIDE = ide;
    log(lvl, "EditorPane: creating new pane (constructor)");
  }

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

  public void initBeforeLoad(String scriptType) {
    initBeforeLoad(scriptType, false);
  }

  public void reInit(String scriptType) {
    initBeforeLoad(scriptType, true);
  }

  private void initBeforeLoad(String scriptType, boolean reInit) {
    String scrType = null;
    boolean paneIsEmpty = false;

    log(lvl, "initBeforeLoad: %s", scriptType);
    if (scriptType == null) {
      scriptType = Runner.EDEFAULT;
      paneIsEmpty = true;
    }

    if (Runner.EPYTHON.equals(scriptType)) {
      scrType = Runner.CPYTHON;
      _indentationLogic = SikuliIDE.getIDESupport(scriptType).getIndentationLogic();
      _indentationLogic.setTabWidth(pref.getTabWidth());
    } else if (Runner.ERUBY.equals(scriptType)) {
      scrType = Runner.CRUBY;
      _indentationLogic = null;
    }

//TODO should know, that scripttype not changed here to avoid unnecessary new setups
    if (scrType != null) {
      sikuliContentType = scrType;
      editorKit = new SikuliEditorKit();
      editorViewFactory = (EditorViewFactory) editorKit.getViewFactory();
      setEditorKit(editorKit);
      setContentType(scrType);

      if (_indentationLogic != null) {
        pref.addPreferenceChangeListener(new PreferenceChangeListener() {
          @Override
          public void preferenceChange(PreferenceChangeEvent event) {
            if (event.getKey().equals("TAB_WIDTH")) {
              _indentationLogic.setTabWidth(Integer.parseInt(event.getNewValue()));
            }
          }
        });
      }
    }

    if (transferHandler == null) {
      transferHandler = new MyTransferHandler();
    }
    setTransferHandler(transferHandler);

    if (_highlighter == null) {
      _highlighter = new EditorCurrentLineHighlighter(this);
      addCaretListener(_highlighter);
      initKeyMap();
      addKeyListener(this);
      addCaretListener(this);
    }

    setFont(new Font(pref.getFontName(), Font.PLAIN, pref.getFontSize()));
    setMargin(new Insets(3, 3, 3, 3));
    setBackground(Color.WHITE);
    if (!Settings.isMac()) {
      setSelectionColor(new Color(170, 200, 255));
    }

    updateDocumentListeners("initBeforeLoad");

    popMenuImage = new SikuliIDEPopUpMenu("POP_IMAGE", this);
    if (!popMenuImage.isValidMenu()) {
      popMenuImage = null;
    }

    if (paneIsEmpty || reInit) {
//			this.setText(String.format(Settings.TypeCommentDefault, getSikuliContentType()));
      this.setText("");
    }
    SikuliIDE.getStatusbar().setCurrentContentType(getSikuliContentType());
    log(lvl, "InitTab: (%s)", getSikuliContentType());
    if (!ScriptingSupport.hasTypeRunner(getSikuliContentType())) {
      Sikulix.popup("No installed runner supports (" + getSikuliContentType() + ")\n"
              + "Trying to run the script will crash IDE!", "... serious problem detected!");
    }
  }

  public String getSikuliContentType() {
    return sikuliContentType;
  }

  public SikuliIDEPopUpMenu getPopMenuImage() {
    return popMenuImage;
  }

  private void updateDocumentListeners(String source) {
    log(lvl, "updateDocumentListeners from: %s", source);
    if (dirtyHandler == null) {
      dirtyHandler = new DirtyHandler();
    }
    getDocument().addDocumentListener(dirtyHandler);
    getDocument().addUndoableEditListener(getUndoManager());
  }

  public EditorUndoManager getUndoManager() {
    if (_undo == null) {
      _undo = new EditorUndoManager();
    }
    return _undo;
  }

  public IIndentationLogic getIndentationLogic() {
    return _indentationLogic;
  }

  private void initKeyMap() {
    InputMap map = this.getInputMap();
    int shift = InputEvent.SHIFT_MASK;
    int ctrl = InputEvent.CTRL_MASK;
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, shift), SikuliEditorKit.deIndentAction);
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ctrl), SikuliEditorKit.deIndentAction);
  }

  @Override
  public void keyPressed(java.awt.event.KeyEvent ke) {
  }

  @Override
  public void keyReleased(java.awt.event.KeyEvent ke) {
  }

  @Override
  public void keyTyped(java.awt.event.KeyEvent ke) {
    //TODO implement code completion * checkCompletion(ke);
  }
  //</editor-fold>

  public String loadFile(boolean accessingAsFile) throws IOException {
    File file = new SikulixFileChooser(sikuliIDE, accessingAsFile).load();
    if (file == null) {
      return null;
    }
    String fname = FileManager.slashify(file.getAbsolutePath(), false);
    int i = sikuliIDE.isAlreadyOpen(fname);
    if (i > -1) {
      log(lvl, "loadFile: Already open in IDE: " + fname);
      return null;
    }
    loadFile(fname);
    if (_editingFile == null) {
      return null;
    }
    return _editingFile.getParent();
  }

  public void loadFile(String filename) {
    log(lvl, "loadfile: %s", filename);
    filename = FileManager.slashify(filename, false);
    File script = new File(filename);
    _editingFile = Runner.getScriptFile(script);
    if (_editingFile != null) {
      setSrcBundle(FileManager.slashify(_editingFile.getParent(), true));
      scriptType = _editingFile.getAbsolutePath().substring(_editingFile.getAbsolutePath().lastIndexOf(".") + 1);
      initBeforeLoad(scriptType);
      if (!readScript(_editingFile)) {
        _editingFile = null;
      }
      updateDocumentListeners("loadFile");
      setDirty(false);
    }
    if (_editingFile == null) {
      _srcBundlePath = null;
    } else {
      _srcBundleTemp = false;
    }
  }

  private boolean readScript(Object script) {
    InputStreamReader isr;
    try {
      if (script instanceof String) {
        isr = new InputStreamReader(
                new ByteArrayInputStream(((String) script).getBytes(Charset.forName("utf-8"))),
                Charset.forName("utf-8"));
      } else if (script instanceof File) {
        isr = new InputStreamReader(
                new FileInputStream((File) script),
                Charset.forName("utf-8"));
      } else {
        log(-1, "readScript: not supported %s as %s", script, script.getClass());
        return false;
      }
      this.read(new BufferedReader(isr), null);
    } catch (Exception ex) {
      log(-1, "read returned %s", ex.getMessage());
      return false;
    }
    return true;
  }

  @Override
  public void read(Reader in, Object desc) throws IOException {
    super.read(in, desc);
    Document doc = getDocument();
    Element root = doc.getDefaultRootElement();
    parse(root);
    restoreCaretPosition();
  }

  public boolean hasEditingFile() {
    return _editingFile != null;
  }

  public String saveFile() throws IOException {
    if (_editingFile == null) {
      return saveAsFile(Settings.isMac());
    } else {
      writeSrcFile();
      return getCurrentShortFilename();
    }
  }

  public String saveAsFile(boolean accessingAsFile) throws IOException {
    File file = new SikulixFileChooser(sikuliIDE, accessingAsFile).save();
    if (file == null) {
      return null;
    }
    String bundlePath = FileManager.slashify(file.getAbsolutePath(), false);
    if (!file.getAbsolutePath().endsWith(".sikuli")) {
      bundlePath += ".sikuli";
    }
    if (FileManager.exists(bundlePath)) {
      int res = JOptionPane.showConfirmDialog(
              null, SikuliIDEI18N._I("msgFileExists", bundlePath),
              SikuliIDEI18N._I("dlgFileExists"), JOptionPane.YES_NO_OPTION);
      if (res != JOptionPane.YES_OPTION) {
        return null;
      }
      FileManager.deleteFileOrFolder(bundlePath);
    }
    FileManager.mkdir(bundlePath);
    try {
      saveAsBundle(bundlePath, (sikuliIDE.getCurrentFileTabTitle()));
      if (Settings.isMac()) {
        if (!Settings.handlesMacBundles) {
          makeBundle(bundlePath, accessingAsFile);
        }
      }
    } catch (IOException iOException) {
    }
    return getCurrentShortFilename();
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

  private void saveAsBundle(String bundlePath, String current) throws IOException {
//TODO allow other file types
    log(lvl, "saveAsBundle: " + getSrcBundle());
    bundlePath = FileManager.slashify(bundlePath, true);
    if (_srcBundlePath != null) {
      if (!ScriptingSupport.transferScript(_srcBundlePath, bundlePath)) {
        log(-1, "saveAsBundle: did not work - ");
      }
    }
    ImagePath.remove(_srcBundlePath);
    if (_srcBundleTemp) {
      FileManager.deleteTempDir(_srcBundlePath);
      _srcBundleTemp = false;
    }
    setSrcBundle(bundlePath);
    _editingFile = createSourceFile(bundlePath, "." + Runner.typeEndings.get(sikuliContentType));
    writeSrcFile();
    reparse();
  }

  private File createSourceFile(String bundlePath, String ext) {
    if (ext != null) {
      String name = new File(bundlePath).getName();
      name = name.substring(0, name.lastIndexOf("."));
      return new File(bundlePath, name + ext);
    } else {
      return new File(bundlePath);
    }
  }

  private void writeSrcFile() throws IOException {
    log(lvl, "writeSrcFile: " + _editingFile.getName());
    this.write(new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(_editingFile.getAbsolutePath()), "UTF8")));
    if (PreferencesUser.getInstance().getAtSaveMakeHTML()) {
      convertSrcToHtml(getSrcBundle());
    } else {
      String snameDir = new File(_editingFile.getAbsolutePath()).getParentFile().getName();
      String sname = snameDir.replace(".sikuli", "") + ".html";
      (new File(snameDir, sname)).delete();
    }
    if (PreferencesUser.getInstance().getAtSaveCleanBundle()) {
      if (!sikuliContentType.equals(Runner.CPYTHON)) {
        log(lvl, "delete-not-used-images for %s using Python string syntax", sikuliContentType);
      }
      cleanBundle();
    }
    setDirty(false);
  }

  private void cleanBundle() {
    log(3, "cleanBundle");
    Set<String> foundImages = parseforImages().keySet();
    if (foundImages.contains("uncomplete_comment_error")) {
      log(-1, "cleanBundle aborted (uncomplete_comment_error)");
    } else {
      FileManager.deleteNotUsedImages(getBundlePath(), foundImages);
      log(3, "cleanBundle finished");
    }
  }

  private Map<String, List<Integer>> parseforImages() {
    String pbundle = FileManager.slashify(getSrcBundle(), false);
    log(3, "parseforImages: in \n%s", pbundle);
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

  public String exportAsZip() {
    File file = new SikulixFileChooser(sikuliIDE).export();
    if (file == null) {
      return null;
    }
    String zipPath = file.getAbsolutePath();
    if (!file.getAbsolutePath().endsWith(".skl")) {
      zipPath += ".skl";
    }
    if (new File(zipPath).exists()) {
      if (!Sikulix.popAsk(String.format("Overwrite existing file?\n%s", zipPath),
              "Exporting packed SikuliX Script")) {
        return null;
      }
    }
    String pSource = _editingFile.getParent();
    try {
      writeSrcFile();
      zipDir(pSource, zipPath, _editingFile.getName());
      log(lvl, "Exported packed SikuliX Script to:\n%s", zipPath);
    } catch (Exception ex) {
      log(-1, "Exporting packed SikuliX Script did not work:\n%s", zipPath);
      return null;
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

  public boolean close() throws IOException {
    log(lvl, "Tab close clicked");
    if (isDirty()) {
      Object[] options = {SikuliIDEI18N._I("yes"), SikuliIDEI18N._I("no"), SikuliIDEI18N._I("cancel")};
      int ans = JOptionPane.showOptionDialog(this,
              SikuliIDEI18N._I("msgAskSaveChanges", getCurrentShortFilename()),
              SikuliIDEI18N._I("dlgAskCloseTab"),
              JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.WARNING_MESSAGE,
              null,
              options, options[0]);
      if (ans == JOptionPane.CANCEL_OPTION
              || ans == JOptionPane.CLOSED_OPTION) {
        return false;
      } else if (ans == JOptionPane.YES_OPTION) {
        if (saveFile() == null) {
          return false;
        }
      } else {
//				sikuliIDE.getTabPane().resetLastClosed();
      }
      setDirty(false);
    }
    if (_srcBundlePath != null) {
      ImagePath.remove(_srcBundlePath);
      if (_srcBundleTemp) {
        FileManager.deleteTempDir(_srcBundlePath);
      }
    }
    return true;
  }

  private void setSrcBundle(String newBundlePath) {
    try {
      newBundlePath = new File(newBundlePath).getCanonicalPath();
    } catch (Exception ex) {
      return;
    }
    _srcBundlePath = newBundlePath;
    ImagePath.setBundlePath(_srcBundlePath);
  }

  public String getSrcBundle() {
    if (_srcBundlePath == null) {
      File tmp = FileManager.createTempDir();
      setSrcBundle(FileManager.slashify(tmp.getAbsolutePath(), true));
      _srcBundleTemp = true;
    }
    return _srcBundlePath;
  }

  // used at ButtonRun.run
  public String getBundlePath() {
    return _srcBundlePath;
  }

  public boolean isSourceBundleTemp() {
    return _srcBundleTemp;
  }

  public String getCurrentSrcDir() {
    if (_srcBundlePath != null) {
      if (_editingFile == null || _srcBundleTemp) {
        return FileManager.normalize(_srcBundlePath);
      } else {
        return _editingFile.getParent();
      }
    } else {
      return null;
    }
  }

  public String getCurrentShortFilename() {
    if (_srcBundlePath != null) {
      File f = new File(_srcBundlePath);
      return f.getName();
    }
    return "Untitled";
  }

  public File getCurrentFile() {
    return getCurrentFile(true);
  }

  public File getCurrentFile(boolean shouldSave) {
    if (shouldSave && _editingFile == null && isDirty()) {
      try {
        saveAsFile(Settings.isMac());
      } catch (IOException e) {
        log(-1, "getCurrentFile: Problem while trying to save %s\n%s",
                _editingFile.getAbsolutePath(), e.getMessage());
      }
    }
    return _editingFile;
  }

  public String getCurrentFilename() {
    if (_editingFile == null) {
      return null;
    }
    return _editingFile.getAbsolutePath();
  }

  //TODO convertSrcToHtml has to be completely revised
  private void convertSrcToHtml(String bundle) {
    IScriptRunner runner = ScriptingSupport.getRunner(null, "jython");
    if (runner != null) {
      runner.doSomethingSpecial("convertSrcToHtml", new String[]{bundle});
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

  public Image getImageInBundle(String filename) {
    return Image.createThumbNail(filename);
  }

  //<editor-fold defaultstate="collapsed" desc="Dirty handling">
  public boolean isDirty() {
    return scriptIsDirty;
  }

  public void setDirty(boolean flag) {
    if (scriptIsDirty == flag) {
      return;
    }
    scriptIsDirty = flag;
    sikuliIDE.setCurrentFileTabTitleDirty(scriptIsDirty);
  }

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
  //<editor-fold defaultstate="collapsed" desc="Caret handling">
//TODO not used
  @Override
  public void caretUpdate(CaretEvent evt) {
    /* seems not to be used
     * if (_can_update_caret_last_x) {
		 * _caret_last_x = -1;
		 * } else {
		 * _can_update_caret_last_x = true;
		 * }
		 */
  }

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

  //<editor-fold defaultstate="collapsed" desc="TODO only used for UnitTest">
  public void jumpTo(String funcName) throws BadLocationException {
    log(lvl + 1, "jumpTo function: " + funcName);
    Element root = getDocument().getDefaultRootElement();
    int pos = getFunctionStartOffset(funcName, root);
    if (pos >= 0) {
      setCaretPosition(pos);
    } else {
      throw new BadLocationException("Can't find function " + funcName, -1);
    }
  }

  private int getFunctionStartOffset(String func, Element node) throws BadLocationException {
    Document doc = getDocument();
    int count = node.getElementCount();
    Pattern patDef = Pattern.compile("def\\s+" + func + "\\s*\\(");
    for (int i = 0; i < count; i++) {
      Element elm = node.getElement(i);
      if (elm.isLeaf()) {
        int start = elm.getStartOffset(), end = elm.getEndOffset();
        String line = doc.getText(start, end - start);
        Matcher matcher = patDef.matcher(line);
        if (matcher.find()) {
          return start;
        }
      } else {
        int p = getFunctionStartOffset(func, elm);
        if (p >= 0) {
          return p;
        }
      }
    }
    return -1;
  }
  //</editor-fold>
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="replace text patterns with image buttons">
  public boolean reparse(String oldName, String newName, boolean fileOverWritten) {
    boolean success;
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
    return reparse();
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

  public boolean reparse() {
    String paneContent = this.getText();
    if (paneContent.length() < 7) {
      return true;
    }
    if (readScript(paneContent)) {
      updateDocumentListeners("reparse");
      return true;
    }
    return false;
  }

  //TODO not clear what this is for LOL (SikuliIDEPopUpMenu.doSetType)
  public boolean reparseCheckContent() {
    if (this.getText().contains(ScriptingSupport.TypeCommentToken)) {
      return true;
    }
    return false;
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
        int start = elm.getStartOffset(), end = elm.getEndOffset();
        parseRange(start, end);
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
      if (m.find()) {
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

  private boolean replaceWithImage(int startOff, int endOff, Pattern ptn)
          throws BadLocationException {
    Document doc = getDocument();
    String imgStr = doc.getText(startOff, endOff - startOff);
    JComponent comp = null;

    if (ptn == patPatternStr || ptn == patPngStr) {
      if (pref.getPrefMoreImageThumbs()) {
        comp = EditorPatternButton.createFromString(this, imgStr, null);
      } else {
        comp = EditorPatternLabel.labelFromString(this, imgStr);
      }
    } else if (ptn == patRegionStr) {
      if (pref.getPrefMoreImageThumbs()) {
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

  public String getPatternString(String ifn, float sim, Location off, Image img) {
//TODO ifn really needed??
    if (ifn == null) {
      return "\"" + EditorPatternLabel.CAPTURE + "\"";
    }
    String imgName = new File(ifn).getName();
    if (img != null) {
      imgName = img.getName();
    }
    String pat = "Pattern(\"" + imgName + "\")";
    String ret = "";
    if (sim > 0) {
      if (sim >= 0.99F) {
        ret += ".exact()";
      } else if (sim != 0.7F) {
        ret += String.format(Locale.ENGLISH, ".similar(%.2f)", sim);
      }
    }
    if (off != null && (off.x != 0 || off.y != 0)) {
      ret += ".targetOffset(" + off.x + "," + off.y + ")";
    }
    if (!ret.equals("")) {
      ret = pat + ret;
    } else {
      ret = "\"" + imgName + "\"";
    }
    return ret;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="content insert append">
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

  //<editor-fold defaultstate="collapsed" desc="feature search">
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
    Debug.log(lvl, "search: %s from %d forward: %s", str, pos, forward);
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
      log(-1, "search: did not work:\n" + e.getStackTrace());
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

  //<editor-fold defaultstate="collapsed" desc="Transfer code incl. images between code panes">
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
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="currently not used">
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
