/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import org.sikuli.basics.Debug;

import org.sikuli.script.Region;

public class SxRectangle extends Visual {

  public SxRectangle(Region region) {
    super();
    init(region);
  }

  public SxRectangle(Visual comp) {
    super();
    init(comp.getRegion());
  }

  public SxRectangle() {
    super();
    init(null);
  }

  private void init(Region region) {
    if (region != null) {
      targetRegion = region;
    } else {
      Debug.log(2, "SikuliGuideRectangle: targetRegion is given as null");
      targetRegion = Region.create(0, 0, 2*stroke, 2*stroke);
    }
    setColor(Color.RED);
  }

  @Override
  public void updateComponent() {
    setActualBounds(targetRegion.getRect());
    setForeground(colorFront);
    super.setLocationRelative(Layout.OVER);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    Stroke pen = new BasicStroke(stroke);
    g2d.setStroke(pen);
    g2d.drawRect(0, 0, getActualWidth() - 1, getActualHeight() - 1);
  }
}
