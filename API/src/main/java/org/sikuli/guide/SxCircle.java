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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import org.sikuli.basics.Debug;

import org.sikuli.script.Region;

public class SxCircle extends Visual {

  public SxCircle(Region region) {
    super();
    init(region);
  }

  public SxCircle(Visual comp) {
    super();
    init(comp.getRegion());
  }

  public SxCircle() {
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
    setActualBounds(getTarget().getRect());
    setForeground(colorFront);
    super.setLocationRelative(Layout.OVER);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    Stroke pen = new BasicStroke((float) stroke);
    g2d.setStroke(pen);
    Rectangle r = new Rectangle(getActualBounds());
    r.grow(-(stroke-1), -(stroke-1));
    g2d.translate(stroke-1, stroke-1);
    Ellipse2D.Double ellipse = new Ellipse2D.Double(0, 0, r.width - 1, r.height - 1);
    g2d.draw(ellipse);
  }
}
