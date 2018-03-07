/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.android.ADBScreen;
import org.sikuli.basics.PreferencesUser;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.*;
import org.sikuli.script.Sikulix;
import org.sikuli.util.OverlayCapturePrompt;
import org.sikuli.basics.Settings;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;

import static org.sikuli.script.Sikulix.popup;

class ButtonCapture extends ButtonOnToolbar implements ActionListener, Cloneable, EventObserver {

  private static final String me = "ButtonCapture: ";
  protected Element _line;
  protected EditorPane _codePane;
  private boolean captureCancelled = false;
  private EditorPatternLabel _lbl = null;
  private String givenName = "";

  public static boolean debugTrace = true;

  public ButtonCapture() {
    super();
    URL imageURL = SikuliIDE.class.getResource("/icons/camera-icon.png");
    setIcon(new ImageIcon(imageURL));
    PreferencesUser pref = PreferencesUser.getInstance();
    String strHotkey = Key.convertKeyToText(
            pref.getCaptureHotkey(), pref.getCaptureHotkeyModifiers());
    setToolTipText(SikuliIDE._I("btnCaptureHint", strHotkey));
    setText(SikuliIDE._I("btnCaptureLabel"));
    //setBorderPainted(false);
    //setMaximumSize(new Dimension(26,26));
    addActionListener(this);
    _line = null;
  }

  public ButtonCapture(EditorPane codePane, Element elmLine) {
    this();
    _line = elmLine;
    _codePane = codePane;
    setUI(UIManager.getUI(this));
    setBorderPainted(true);
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    setText(null);
    URL imageURL = SikuliIDE.class.getResource("/icons/capture-small.png");
    setIcon(new ImageIcon(imageURL));
  }

  public ButtonCapture(EditorPatternLabel lbl) {
    // for internal use with the image label __CLICK-TO-CAPTURE__
    super();
    _line = null;
    _codePane = null;
    _lbl = lbl;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debug.log(3, "ButtonCapture: capture!");
    captureWithAutoDelay();
  }

  public void captureWithAutoDelay() {
    PreferencesUser pref = PreferencesUser.getInstance();
    int delay = (int) (pref.getCaptureDelay() * 1000.0) + 1;
    capture(delay);
  }

  IScreen defaultScreen = null;
  ScreenImage sImgNonLocal = null;

  public void capture(final int delay) {
    String line = "";
    SikuliIDE ide = SikuliIDE.getInstance();
    ide.setVisible(false);
    EditorPane codePane = ide.getCurrentCodePane();
    line = codePane.getLineTextAtCaret();
    givenName = codePane.parseLineText("#" + line.trim());
    Debug.log(3, "ButtonCapture: doPrompt for %s", givenName);
    RunTime.pause(((float) delay)/1000);
    defaultScreen = SikuliIDE.getDefaultScreen();
    if (defaultScreen == null) {
      Screen.doPrompt("Select an image", this);
    } else {
      Sikulix.popup("Android capture");
      Thread adbCapture = new Thread() {
        @Override
        public void run() {
          ADBScreen aScr = (ADBScreen) defaultScreen;
          aScr.wakeUp(2);
          sImgNonLocal = aScr.userCapture("");
          ButtonCapture.this.update((EventSubject) null);
        }
      };
      adbCapture.start();
    }
  }

  @Override
  public void update(EventSubject es) {
    Debug.log(3, "ButtonCapture: update");
    ScreenImage simg = null;
    OverlayCapturePrompt ocp = null;
    if (null == es) {
      simg = sImgNonLocal;
    } else {
      ocp = (OverlayCapturePrompt) es;
      simg = ocp.getSelection();
      Screen.closePrompt();
    }
    String filename = null;
    String fullpath = null;
    EditorPane pane = SikuliIDE.getInstance().getCurrentCodePane();
    boolean saveOverwrite = Settings.OverwriteImages;
    if (simg != null) {
      if (!givenName.isEmpty()) {
        filename = givenName + ".png";
        Settings.OverwriteImages = true;
      } else {
        int naming = PreferencesUser.getInstance().getAutoNamingMethod();
        if (naming == PreferencesUser.AUTO_NAMING_TIMESTAMP) {
          filename = Settings.getTimestamp();
        } else if (naming == PreferencesUser.AUTO_NAMING_OCR) {
          filename = PatternPaneNaming.getFilenameFromImage(simg.getImage());
          if (filename == null || filename.length() == 0) {
            filename = Settings.getTimestamp();
          }
        } else {
          String nameOCR = "";
          try {
            nameOCR = PatternPaneNaming.getFilenameFromImage(simg.getImage());
          } catch (Exception e) {
          }
          filename = getFilenameFromUser(nameOCR);
        }
      }

      if (filename != null) {
        fullpath = FileManager.saveImage(simg.getImage(), filename, pane.getSrcBundle());
        if (fullpath != null) {
          fullpath = FileManager.slashify(fullpath, false);
        }
      }
    }
    Settings.OverwriteImages = saveOverwrite;
    captureCompleted(fullpath);
    if (ocp != null) {
      Screen.resetPrompt(ocp);
    }
    SikuliIDE.showAgain();
  }

  public void captureCompleted(String imgFullPath) {
    Element src = getSrcElement();
    if (imgFullPath != null) {
      Debug.log(3, "captureCompleted: " + imgFullPath);
      if (src == null) {
        if (_codePane == null) {
          if (_lbl == null) {
            insertAtCursor(SikuliIDE.getInstance().getCurrentCodePane(), imgFullPath);
          } else {
            _lbl.setFile(imgFullPath);
          }
        } else {
          insertAtCursor(_codePane, imgFullPath);
        }
      } else {
        replaceButton(src, imgFullPath);
      }
    } else {
      Debug.log(3, "ButtonCapture: Capture cancelled");
      if (src != null) {
        captureCancelled = true;
        replaceButton(src, "");
        captureCancelled = false;
      }
    }
  }

  //<editor-fold defaultstate="collapsed" desc="RaiMan not used">
	/*public boolean hasNext() {
   * return false;
   * }*/
  /*public CaptureButton getNextDiffButton() {
   * return null;
   * }*/
  /*public void setParentPane(SikuliPane parent) {
   * _codePane = parent;
   * }*/
  /*public void setDiffMode(boolean flag) {
   * }*/
  /*public void setSrcElement(Element elmLine) {
   * _line = elmLine;
   * }*/
  //</editor-fold>

  private String getFilenameFromUser(String hint) {
    return (String) JOptionPane.showInputDialog(
            _codePane,
            SikuliIDEI18N._I("msgEnterScreenshotFilename"),
            SikuliIDEI18N._I("dlgEnterScreenshotFilename"),
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            hint);
  }

  private Element getSrcElement() {
    return _line;
  }

  private boolean replaceButton(Element src, String imgFullPath) {
    if (captureCancelled) {
      if (_codePane.showThumbs && PreferencesUser.getInstance().getPrefMoreImageThumbs()
              || !_codePane.showThumbs) {
        return true;
      }
    }
    int start = src.getStartOffset();
    int end = src.getEndOffset();
    int old_sel_start = _codePane.getSelectionStart(),
            old_sel_end = _codePane.getSelectionEnd();
    try {
      StyledDocument doc = (StyledDocument) src.getDocument();
      String text = doc.getText(start, end - start);
      Debug.log(3, text);
      for (int i = start; i < end; i++) {
        Element elm = doc.getCharacterElement(i);
        if (elm.getName().equals(StyleConstants.ComponentElementName)) {
          AttributeSet attr = elm.getAttributes();
          Component com = StyleConstants.getComponent(attr);
          boolean isButton = com instanceof ButtonCapture;
          boolean isLabel = com instanceof EditorPatternLabel;
          if (isButton || isLabel && ((EditorPatternLabel) com).isCaptureButton()) {
            Debug.log(5, "button is at " + i);
            int oldCaretPos = _codePane.getCaretPosition();
            _codePane.select(i, i + 1);
            if (!_codePane.showThumbs) {
              _codePane.insertString((new EditorPatternLabel(_codePane, imgFullPath, true)).toString());
            } else {
              if (PreferencesUser.getInstance().getPrefMoreImageThumbs()) {
                com = new EditorPatternButton(_codePane, imgFullPath);
              } else {
                if (captureCancelled) {
                  com = new EditorPatternLabel(_codePane, "");
                } else {
                  com = new EditorPatternLabel(_codePane, imgFullPath, true);
                }
              }
              _codePane.insertComponent(com);
            }
            _codePane.setCaretPosition(oldCaretPos);
            break;
          }
        }
      }
    } catch (BadLocationException ble) {
      Debug.error(me + "Problem inserting Button!\n%s", ble.getMessage());
    }
    _codePane.select(old_sel_start, old_sel_end);
    _codePane.requestFocus();
    return true;
  }

  protected void insertAtCursor(EditorPane pane, String imgFilename) {
    String img = "\"" + (new File(imgFilename)).getName() + "\"";
    if (!pane.showThumbs) {
      pane.insertString(img);
    } else {
      if (PreferencesUser.getInstance().getPrefMoreImageThumbs()) {
        EditorPatternButton comp = EditorPatternButton.createFromFilename(pane, imgFilename, null);
        if (comp != null) {
          pane.insertComponent(comp);
        }
      } else {
        pane.insertComponent(new EditorPatternLabel(pane, imgFilename, true));
      }
    }
//TODO set Caret
    pane.requestFocus();
  }

  @Override
  public String toString() {
    return "\"__CLICK-TO-CAPTURE__\"";
  }
}
