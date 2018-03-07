/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

public class SxText extends Visual {

  JLabel label;

  public SxText(String text) {
    super();
    init(text);
  }

  private void init(String text) {
    this.text = text;
    label = new JLabel();
    add(label);
    fontSize = 12;
    label.setFont(new Font("SansSerif", Font.PLAIN, 36));
  }

  @Override
  public void updateComponent() {
    String htmltxt = "<html><div style='" + getStyleString() + "'>"
            + text + "</div></html>";
    label.setText(htmltxt);
    Dimension size = label.getPreferredSize();
    if (size.width > maxWidth) {
      // hack to limit the width of the text to width
      htmltxt = "<html><div style='width:" + maxWidth + ";" + getStyleString() + "'>"
                + text + "</div></html>";
      label.setText(htmltxt);
      size = label.getPreferredSize();
    }
    label.setSize(size);
    setActualSize(size);
  }

  @Override
  public void paintComponent(Graphics g) {
    Dimension originalSize = label.getPreferredSize();
    Dimension actualSize = getActualSize();
    float scalex = 1f * actualSize.width / originalSize.width;
    float scaley = 1f * actualSize.height / originalSize.height;
    ((Graphics2D) g).scale(scalex, scaley);
    super.paintComponent(g);
  }

  //<editor-fold defaultstate="collapsed" desc="TODO make text editable??">
  /*  TextPropertyEditor ed = null;
   *
   * public void setEditable(boolean editable) {
   * if (editable) {
   * } else {
   * }
   * }*/  //</editor-fold>
}
