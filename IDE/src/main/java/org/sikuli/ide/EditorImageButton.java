/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.support.ide.IButton;
import org.sikuli.script.Location;
import org.sikuli.script.Pattern;
import org.sikuli.support.Commons;
import org.sikuli.support.RunTime;
import org.sikuli.support.gui.SXDialog;
import org.sikuli.support.gui.SXDialogPaneImage;
import org.sikuli.support.gui.SXDialogPaneImageMenu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EditorImageButton extends JButton implements ActionListener, Serializable, MouseListener {

  Map<String, Object> options;

  public Map<String, Object> getOptions() {
    return options;
  }

  public String getFilename() {
    return ((File) options.get(IButton.FILE)).getAbsolutePath();
  }

  int MAXHEIGHT = 20;

  BufferedImage thumbnail;

  public BufferedImage getThumbnail() {
    return thumbnail;
  }

  public EditorImageButton() {
  }

  public EditorImageButton(Map<String, Object> options) {
    this.options = options;
    thumbnail = createThumbnailImage((File) this.options.get(IButton.FILE), MAXHEIGHT);

    init();
  }

  public EditorImageButton(File imgFile) {
    thumbnail = createThumbnailImage(imgFile, MAXHEIGHT);
    options = new HashMap<>();
    options.put(IButton.FILE, imgFile);
    options.put(IButton.TEXT, "\"" + info() + "\"");

    init();
  }

  public EditorImageButton(Pattern pattern) {
    thumbnail = createThumbnailImage(pattern, MAXHEIGHT);
    options = new HashMap<>();
    options.put(IButton.FILE, pattern.getImage().file());
    options.put(IButton.TEXT, "\"" + info() + "\"");
    options.put(IButton.PATT, pattern);

    init();
  }

  private void init() {
    setIcon(new ImageIcon(thumbnail));
    setButtonText();

    setMargin(new Insets(0, 0, 0, 0));
    setBorderPainted(true);
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    addActionListener(this);
    addMouseListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final EditorImageButton source = (EditorImageButton) e.getSource();
    handlePopup(null);
  }

  private boolean closeIfVisible(SXDialog popup) {
    if (popup != null && popup.isVisible()) {
      popup.closeCancel();
      return true;
    }
    return false;
  }

  SXDialogPaneImageMenu popmenu = null;

  private void handlePopup(MouseEvent me) {
    if (closeIfVisible(popmenu)) {
      return;
    }
    closeIfVisible(popwin);
    if (me == null) {
      handlePreview();
    } else {
      Point where = getLocationOnScreen();
      where.y += MAXHEIGHT + 10;

      popmenu = new SXDialogPaneImageMenu(where,
          new String[]{"image"}, options.get(IButton.FILE), this);
      popmenu.run();
    }
  }

  private SXDialogPaneImage popwin = null;

  private void handlePreview() {
    Point where = getLocationOnScreen();
    popwin = new SXDialogPaneImage(where, new String[]{"image"}, options.get(IButton.FILE), this);
    popwin.run();
  }

  @Override
  public Point getLocationOnScreen() {
    return super.getLocationOnScreen();
  }

  @Override
  public void mouseEntered(MouseEvent me) {
  }

  @Override
  public void mouseExited(MouseEvent me) {
  }

  @Override
  public void mousePressed(MouseEvent me) {
    if (me.isPopupTrigger()) {
      handlePopup(me);
    }
  }

  @Override
  public void mouseReleased(MouseEvent me) {
    if (me.isPopupTrigger()) {
      handlePopup(me);
    }
  }

  @Override
  public void mouseClicked(MouseEvent me) {
  }

  private BufferedImage createThumbnailImage(Pattern pattern, int maxHeight) {
    //TODO Pattern thumbnail
    return createThumbnailImage(pattern.getImage().file(), maxHeight);
  }

  private BufferedImage createThumbnailImage(File imgFile, int maxHeight) {
    BufferedImage img = null;
    try {
      img = ImageIO.read(imgFile);
    } catch (IOException e) {
    }
    if (img == null) {
      try {
        img = ImageIO.read(SikulixIDE.class.getResource("/icons/sxcapture.png"));
      } catch (Exception e) {
        RunTime.terminate(999, "EditorImageButton: createThumbnailImage: possible? %s", e.getMessage());
      }
    }
    int w = img.getWidth();
    int h = img.getHeight();
    if (maxHeight == 0 || maxHeight >= h) {
      return img;
    }
    float _scale = (float) maxHeight / h;
    w *= _scale;
    h *= _scale;
    h = (int) h;
    BufferedImage thumb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = thumb.createGraphics();
    g2d.drawImage(img, 0, 0, w, h, null);
    g2d.dispose();
    return thumb;
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(new Color(0, 128, 128, 128));
    g2d.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 5, 5);
  }

  @Override
  public String toString() {
    return (String) options.get(IButton.TEXT);
  }

  public String info() {
    final String name = FilenameUtils.getBaseName(((File) options.get(IButton.FILE)).getAbsolutePath());
    return String.format("%s", name);
  }

  void setButtonText() {
    setToolTipText(info());
  }

  public static void renameImage(String name, Map<String, Object> options) {
    Commons.error("N/A: EditorImageButton::renameImage (%s -> %s)", options.get("image"), name);
    // rename image file
    // replace image name usage in script
    // thumbnails off/on
  }

  //imgBtn.setImage(filename);
  public void setImage(String fname) {

  }

  //imgBtn.setParameters(
  //						_screenshot.isExact(), _screenshot.getSimilarity(),
  //						_screenshot.getNumMatches()));
  public boolean setParameters(boolean exact, double sim, int numM) {
    return true;
  }

  //imgBtn.setTargetOffset(_tarOffsetPane.getTargetOffset()))
  public boolean setTargetOffset(Location offset) {
    return true;
  }

  //imgBtn.getWindow()
  public PatternWindow getWindow() {
    return null;
  }

  //imgBtn.resetParameters()
  public void resetParameters() {

  }
}
