/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import org.sikuli.script.Region;

public class SxButton extends SxClickable {

  Font f = new Font("sansserif", Font.BOLD, 18);
  JLabel label = null;

  public SxButton(String name) {
    super(new Region(0, 0, 0, 0));
    init(name);
  }

  private void init(String name) {
    PADDING_X = PADDING_Y = 10;
    fontSize = 18;
    setName(name);
    setColors(null, null, null, null, Color.WHITE);
    mouseOverColor = new Color(0.3f, 0.3f, 0.3f);
    layout = Layout.BOTTOM;
  }

  @Override
  public void setName(String name) {
    if (label == null) {
      super.setName(name);
      this.label = new JLabel(name);
      add(label);
    }
    label.setFont(new Font("sansserif", Font.BOLD, fontSize));
    label.setForeground(colorText);
    Dimension s = label.getPreferredSize();
    label.setLocation((int) (PADDING_X/2), (int) (PADDING_Y/2));
    label.setSize(s);
    s.height += PADDING_Y;
    s.width += PADDING_X;
    setActualSize(s);
  }

  @Override
  public void updateComponent() {
    setName(name);
    setLocationRelative(layout);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    Color cb = null;
    if (isMouseOver()) {
      cb = mouseOverColor;
    } else {
      cb = colorFront;
    }
    g2d.setColor(cb);
    RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getActualWidth() - 1, getActualHeight() - 1, PADDING_X, PADDING_Y);
    g2d.fill(roundedRectangle);
    g2d.setColor(cb);
    g2d.draw(roundedRectangle);
    roundedRectangle = new RoundRectangle2D.Float(1, 1, getActualWidth() - 3, getActualHeight() - 3, PADDING_X, PADDING_Y);
    g2d.setColor(colorFrame);
    g2d.draw(roundedRectangle);
    label.paintComponents(g);
  }
}
