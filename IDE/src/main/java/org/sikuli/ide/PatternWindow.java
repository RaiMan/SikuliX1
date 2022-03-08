/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.sikuli.script.Location;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.support.ScreenUnion;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.devices.ScreenDevice;

public class PatternWindow extends JFrame {

  private static final String me = "PatternWindow: ";
  private EditorPatternButton imageButton;
  private PatternPaneNaming paneNaming;
  private PatternPaneScreenshot paneScreenshot;
  private PatternPaneTargetOffset paneTargetImage;
  private JTabbedPane tabPane;
  private JLabel[] msgApplied;
  private int tabSequence = 0;
  private static final int tabMax = 3;
  private ScreenImage screenImage;
  private boolean dirty;
  private EditorPane currentPane;
  boolean isFileOverwritten = false;
  String fileRenameOld;
  String fileRenameNew;
  Dimension screenSize;
  Point screenTopLeft;

  static String _I(String key, Object... args) {
    return SikuliIDEI18N._I(key, args);
  }

  public PatternWindow(EditorPatternButton imgBtn, boolean exact, double similarity, Location offset) {
    init(imgBtn, exact, similarity, offset);
  }

  private void init(EditorPatternButton imgBtn, boolean exact, double similarity, Location offset) {
    setTitle(_I("winPatternSettings"));
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    imageButton = imgBtn;

    takeScreenshot();
    Container c = getContentPane();
    c.setLayout(new BorderLayout());
		Rectangle screenRect = ScreenDevice.get(ScreenDevice.whichMonitor(SikulixIDE.getWindowRect())).asRectangle();
		int margin = 50;
    screenSize = screenRect.getSize();
    screenTopLeft = screenRect.getLocation();
    screenSize.width = (int) ((screenSize.width - 2 * margin) * 0.95);
    screenSize.height = (int) ((screenSize.height - 2 * margin) * 0.95);

    // optimize window size to aspect ratio of screenshot
    double ratio = (double) screenImage.w / screenImage.h;
    screenSize.width = Math.min((int) ((screenSize.height - PatternPaneScreenshot.BOTTOM_MARGIN) * ratio), screenSize.width);

    // center window
    int offsetX = (screenRect.width - screenSize.width) / 2;
    int offsetY = (screenRect.height - screenSize.height) / 2;
    screenTopLeft.translate(offsetX, offsetY);
    setPreferredSize(screenSize);

    tabPane = new JTabbedPane();
    msgApplied = new JLabel[tabMax];

    tabSequence = 0;
    JLabel aMsg = msgApplied[tabSequence] = new JLabel();
    setMessageApplied(tabSequence, false);
    paneNaming = new PatternPaneNaming(imageButton, aMsg);
    tabPane.addTab(_I("tabNaming"), paneNaming);

    tabSequence++;
    msgApplied[tabSequence] = new JLabel();
    setMessageApplied(tabSequence, false);
    tabPane.addTab(_I("tabMatchingPreview"), createPreviewPanel());

    tabSequence++;
    msgApplied[tabSequence] = new JLabel();
    setMessageApplied(tabSequence, false);
    tabPane.addTab(_I("tabTargetOffset"), createTargetPanel());
    paneTargetImage.setTarget(offset.x, offset.y);

    c.add(tabPane, BorderLayout.CENTER);
    c.add(createButtons(), BorderLayout.SOUTH);
    c.doLayout();
    pack();
    try {
      paneScreenshot.createMatches(imageButton.getFilename(), exact, similarity);
    } catch (Exception e) {
      Debug.error(me + "Problem while setting up pattern pane\n%s", e.getMessage());
    }
    setDirty(false);
    currentPane = SikulixIDE.get().getCurrentCodePane();
    setLocation(screenTopLeft);
    setVisible(true);
  }

  void takeScreenshot() {
    SikulixIDE.doHide();
    try {
      Thread.sleep(500);
    } catch (Exception e) {
    }
    screenImage = (new ScreenUnion()).getScreen().capture();
    SikulixIDE.showAgain();
  }

  private JPanel createPreviewPanel() {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    paneScreenshot = new PatternPaneScreenshot(screenImage, screenSize, msgApplied[tabSequence]);
    createMarginBox(p, paneScreenshot);
    p.add(Box.createVerticalStrut(5));
    p.add(paneScreenshot.createControls());
//		p.add(Box.createVerticalStrut(5));
//		p.add(msgApplied[tabSequence]);
    p.doLayout();
    return p;
  }

  private JPanel createTargetPanel() {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    paneTargetImage = new PatternPaneTargetOffset(
        screenImage, imageButton.getFilename(), imageButton.getTargetOffset(), screenSize, msgApplied[tabSequence]);
    createMarginBox(p, paneTargetImage);
    p.add(Box.createVerticalStrut(5));
    p.add(paneTargetImage.createControls());
    p.doLayout();
    return p;
  }

  private JComponent createButtons() {
    JPanel pane = new JPanel(new GridBagLayout());
    JButton btnOK = new JButton(_I("ok"));
    btnOK.addActionListener(new ActionOK(this));
    JButton btnApply = new JButton(_I("apply"));
    btnApply.addActionListener(new ActionApply(this));
    final JButton btnCancel = new JButton(_I("cancel"));
    btnCancel.addActionListener(new ActionCancel(this));
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 3;
    c.gridx = 0;
    c.insets = new Insets(5, 0, 10, 0);
    c.anchor = GridBagConstraints.LAST_LINE_END;
    pane.add(btnOK, c);
    c.gridx = 1;
    pane.add(btnApply, c);
    c.gridx = 2;
    pane.add(btnCancel, c);
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        btnCancel.doClick();
      }
    });
    KeyStroke escapeKeyStroke =
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
    this.getRootPane().
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
        put(escapeKeyStroke, "ESCAPE");
    this.getRootPane().getActionMap().put("ESCAPE",
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            btnCancel.doClick();
          }
        });
    return pane;
  }

  private void createMarginBox(Container c, Component comp) {
    c.add(Box.createVerticalStrut(10));
    Box lrMargins = Box.createHorizontalBox();
    lrMargins.add(Box.createHorizontalStrut(10));
    lrMargins.add(comp);
    lrMargins.add(Box.createHorizontalStrut(10));
    c.add(lrMargins);
    c.add(Box.createVerticalStrut(10));
  }

  public void setMessageApplied(int i, boolean flag) {
    if (flag) {
      for (JLabel m : msgApplied) {
        m.setText("     (changed)");
      }
    } else {
      msgApplied[i].setText("     (          )");
    }
  }

  public void close() {
    screenImage = null;
    imageButton.resetWindow();
  }

  public JTabbedPane getTabbedPane() {
    return tabPane;
  }

  public void setTargetOffset(Location offset) {
    if (offset != null) {
      paneTargetImage.setTarget(offset.x, offset.y);
    }
  }

//  private void actionPerformedUpdates(Window _parent) {
//    boolean tempDirty = isDirty();
//    if (paneNaming.isDirty()) {
//      String filename = paneNaming.getAbsolutePath();
//      if (filename.contains("%")) {
//        Debug.error("%s\n%% in filename replaced with _", filename);
//        filename = filename.replace("%", "_");
//      }
//      String oldFilename = _imgBtn.getFilename();
//      if (FileManager.exists(filename)) {
//        String name = FileManager.getName(filename);
//        int ret = JOptionPane.showConfirmDialog(
//            _parent,
//            SikuliIDEI18N._I("msgFileExists", name),
//            SikuliIDEI18N._I("dlgFileExists"),
//            JOptionPane.WARNING_MESSAGE,
//            JOptionPane.YES_NO_OPTION);
//        if (ret != JOptionPane.YES_OPTION) {
//          return;
//        }
//        if (isFileOverwritten) {
//          if (!revertImageRename()) {
//            return;
//          }
//        }
//        isFileOverwritten = true;
//      }
//      try {
//        FileManager.xcopy(oldFilename, filename);
//        _imgBtn.setFilename(filename);
//        fileRenameOld = oldFilename;
//        fileRenameNew = filename;
//      } catch (IOException ioe) {
//        Debug.error("renaming failed: old: %s \nnew: %s\n%s",
//            oldFilename, filename, ioe.getMessage());
//        isFileOverwritten = false;
//        return;
//      }
//      paneNaming.updateFilename();
//      addDirty(true);
//    }
//    addDirty(_imgBtn.setParameters(
//        _screenshot.isExact(), (float) _screenshot.getSimilarity(),
//        _screenshot.getNumMatches()));
//    addDirty(_imgBtn.setTargetOffset(_tarOffsetPane.getTargetOffset()));
//    if (isDirty() || tempDirty) {
//      Debug.log(3, "Preview: update: " + _imgBtn.toString());
//      int i = _imgBtn.getWindow().getTabbedPane().getSelectedIndex();
//      _imgBtn.getWindow().setMessageApplied(i, true);
//      _imgBtn.repaint();
//    }
//  }

  private void actionPerformedUpdates(Window _parent) {
    boolean tempDirty = isDirty();
    if (paneNaming.isDirty()) {
      String filename = paneNaming.getAbsolutePath();
      if (filename.contains("%")) {
        Debug.error("%s\n%% in filename replaced with _", filename);
        filename = filename.replace("%", "_");
      }
      String oldFilename = imageButton.getFilename();
      if (new File(filename).exists()) {
        String name = new File(filename).getName();
        int ret = JOptionPane.showConfirmDialog(
            _parent,
            SikuliIDEI18N._I("msgFileExists", name),
            SikuliIDEI18N._I("dlgFileExists"),
            JOptionPane.WARNING_MESSAGE,
            JOptionPane.YES_NO_OPTION);
        if (ret != JOptionPane.YES_OPTION) {
          return;
        }
        if (isFileOverwritten) {
          if (!revertImageRename()) {
            return;
          }
        }
        isFileOverwritten = true;
      }
      try {
        FileManager.xcopy(oldFilename, filename);
        imageButton.setImage(filename);
        fileRenameOld = oldFilename;
        fileRenameNew = filename;
      } catch (IOException ioe) {
        Debug.error("renaming failed: old: %s \nnew: %s\n%s",
            oldFilename, filename, ioe.getMessage());
        isFileOverwritten = false;
        return;
      }
      //paneNaming.updateFilename();
      addDirty(true);
    }

    Rectangle changedBounds = paneTargetImage.getChangedBounds();
    if (changedBounds != null) {

      File file = new File(paneNaming.getAbsolutePath());

      BufferedImage changedImg = screenImage.getImage().getSubimage(changedBounds.x, changedBounds.y, changedBounds.width, changedBounds.height);

      try {
        ImageIO.write(changedImg, "png", file);
      } catch (IOException e) {
        Debug.error("PatternWindow: Error while saving resized pattern image: %s", e.getMessage());
      }

      //TODO imgBtn.reloadImage();
      paneScreenshot.reloadImage();
      //paneNaming.reloadImage();
      currentPane.repaint();
    }

    boolean isExact = paneScreenshot.isExact();
    double similarity = paneScreenshot.getSimilarity();
    addDirty(imageButton.setParameters(isExact, similarity));
    addDirty(imageButton.setTargetOffset(paneTargetImage.getTargetOffset()));

    if (isDirty() || tempDirty) {
      Debug.log(3, "Preview: update: " + imageButton.toString());
      int i = imageButton.getWindow().tabPane.getSelectedIndex();
      imageButton.getWindow().setMessageApplied(i, true);
      imageButton.repaint();
    }
  }

  private boolean revertImageRename() {
    try {
      FileManager.xcopy(fileRenameNew, fileRenameOld);
      imageButton.setFilename(fileRenameOld);
    } catch (IOException ioe) {
      Debug.error("revert renaming failed: new: %s \nold: %s\n%s",
          fileRenameNew, fileRenameOld, ioe.getMessage());
      return false;
    }
    return true;
  }

  class ActionOK implements ActionListener {

    private Window _parent;

    public ActionOK(JFrame parent) {
      _parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      actionPerformedUpdates(_parent);
      if (fileRenameOld != null) {
        currentPane.reparseOnRenameImage(fileRenameOld, fileRenameNew, isFileOverwritten);
      }
      imageButton.getWindow().close();
      _parent.dispose();
      currentPane.setDirty(setDirty(false));
    }
  }

  class ActionApply implements ActionListener {

    private Window _parent;

    public ActionApply(Window parent) {
      _parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      actionPerformedUpdates(_parent);
      imageButton.getWindow().getTabbedPane().getSelectedComponent().transferFocus();
    }
  }

  class ActionCancel implements ActionListener {

    private Window _parent;

    public ActionCancel(Window parent) {
      _parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (isDirty()) {
        imageButton.resetParameters();
        if (isFileOverwritten) {
          revertImageRename();
        }
      }
      imageButton.getWindow().close();
      _parent.dispose();
    }
  }

  protected boolean isDirty() {
    return dirty;
  }

  private boolean setDirty(boolean flag) {
    boolean xDirty = dirty;
    dirty = flag;
    return xDirty;
  }

  protected void addDirty(boolean flag) {
    dirty |= flag;
  }
}
