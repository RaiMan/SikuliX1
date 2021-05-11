/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.basics.PreferencesUser;
import org.sikuli.idesupport.IButton;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class EditorImageButton extends JButton implements ActionListener, Serializable, MouseListener {

  Map<String, Object> options;

  int MAXHEIGHT = 30;
  BufferedImage thumbnail;

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
    Debug.log(3, "ImageButton: action performed");
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

  //<editor-fold defaultstate="collapsed" desc="paint button">
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(new Color(0, 128, 128, 128));
    g2d.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 5, 5);
  }
  //</editor-fold>

  @Override
  public String toString() {
    return (String) options.get(IButton.TEXT);
  }

  public String info() {
    final String name = FilenameUtils.getBaseName(((File) options.get(IButton.FILE)).getAbsolutePath());
    return String.format("%s", name);
  }

  private void setButtonText() {
      setToolTipText(info());
  }

  //<editor-fold defaultstate="collapsed" desc="mouse events not used">
  @Override
  public void mouseClicked(MouseEvent me) {
    final boolean popupTrigger = me.isPopupTrigger();
    Debug.log(3, "ImageButton: mouse click: popup(%s)", popupTrigger);
  }

  @Override
  public void mousePressed(MouseEvent me) {
    final boolean popupTrigger = me.isPopupTrigger();
    Debug.log(3, "ImageButton: mouse press: popup(%s)", popupTrigger);
  }

  @Override
  public void mouseReleased(MouseEvent me) {
    final boolean popupTrigger = me.isPopupTrigger();
    Debug.log(3, "ImageButton: mouse release: popup(%s)", popupTrigger);
  }
  //</editor-fold>
}
