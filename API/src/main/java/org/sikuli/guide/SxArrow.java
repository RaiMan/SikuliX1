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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Array;

import org.sikuli.basics.Debug;

public class SxArrow extends Visual implements ComponentListener {

  public static final int STRAIGHT = 0;
  public static final int ELBOW_X = 1;
  public static final int ELBOW_Y = 2;
  int style;
  private Point source = null;
  private Point destination = null;
  private Visual from = null;
  private Visual to = null;
  boolean hasComponents = false;

  public SxArrow(Point from, Point to) {
    super();
    this.source = from;
    this.destination = to;
    init();
  }

  public SxArrow(Visual from, Visual to) {
    super();
    hasComponents = true;
    this.from = from;
    this.to = to;
    from.addComponentListener(this);
    to.addComponentListener(this);
    init();
  }

  private void init() {
    colorFront = Color.RED;
    style = STRAIGHT;
    updateComponent();
  }

  @Override
  public void updateComponent() {
    setForeground(colorFront);
    Rectangle dirtyBounds = getBounds();
    if (from != null && to != null) {
      source = from.getCenter();
      destination = to.getCenter();
    }
    Rectangle r = new Rectangle(getSource());
    r.add(getDestination());
    r.grow(10, 10);
    setActualBounds(r);
    dirtyBounds.add(getBounds());
    if (getTopLevelAncestor() != null) {
      getTopLevelAncestor().repaint(dirtyBounds.x, dirtyBounds.y, dirtyBounds.width, dirtyBounds.height);
    }
    if (hasComponents) {
      updateVisibility();
    }
  }

  public void setStyle(int style) {
    this.style = style;
  }

  private void drawPolylineArrow(Graphics g, int[] xPoints, int[] yPoints,
          int headLength, int headwidth) {
    double theta1;
    Object tempX1 = ((Array.get(xPoints, ((xPoints.length) - 2))));
    Object tempX2 = ((Array.get(xPoints, ((xPoints.length) - 1))));
    Integer fooX1 = (Integer) tempX1;
    int x1 = fooX1.intValue();
    Integer fooX2 = (Integer) tempX2;
    int x2 = fooX2.intValue();
    Object tempY1 = ((Array.get(yPoints, ((yPoints.length) - 2))));
    Object tempY2 = ((Array.get(yPoints, ((yPoints.length) - 1))));
    Integer fooY1 = (Integer) tempY1;
    int y1 = fooY1.intValue();
    Integer fooY2 = (Integer) tempY2;
    int y2 = fooY2.intValue();
    int deltaX = (x2 - x1);
    int deltaY = (y2 - y1);
    double theta = Math.atan((double) (deltaY) / (double) (deltaX));
    if (deltaX < 0.0) {
      theta1 = theta + Math.PI; //If theta is negative make it positive
    } else {
      theta1 = theta; //else leave it alone
    }
    int lengthdeltaX = -(int) (Math.cos(theta1) * headLength);
    int lengthdeltaY = -(int) (Math.sin(theta1) * headLength);
    int widthdeltaX = (int) (Math.sin(theta1) * headwidth);
    int widthdeltaY = (int) (Math.cos(theta1) * headwidth);
    g.drawPolyline(xPoints, yPoints, xPoints.length);
    g.drawLine(x2, y2, x2 + lengthdeltaX + widthdeltaX, y2 + lengthdeltaY - widthdeltaY);
    g.drawLine(x2, y2, x2 + lengthdeltaX - widthdeltaX, y2 + lengthdeltaY + widthdeltaY);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    Rectangle r = getActualBounds();
    Stroke pen = new BasicStroke(3.0F);
    g2d.setStroke(pen);
    g2d.translate(-r.x, -r.y);
    if (style == STRAIGHT) {
      drawPolylineArrow(g, new int[]{getSource().x, getDestination().x}, new int[]{getSource().y, getDestination().y}, 6, 6);
    } else if (style == ELBOW_X) {
      Point m = new Point(getDestination().x, getSource().y);
      g2d.drawLine(getSource().x, getSource().y, m.x, m.y);
      drawPolylineArrow(g, new int[]{m.x, getDestination().x}, new int[]{m.y, getDestination().y}, 6, 6);
    } else if (style == ELBOW_Y) {
      Point m = new Point(getSource().x, getDestination().y);
      g2d.drawLine(getSource().x, getSource().y, m.x, m.y);
      drawPolylineArrow(g, new int[]{m.x, getDestination().x}, new int[]{m.y, getDestination().y}, 6, 6);
    }
  }

  public void setDestination(Point destination) {
    this.destination = destination;
    updateComponent();
  }

  public Point getDestination() {
    return destination;
  }

  public void setSource(Point source) {
    this.source = source;
    updateComponent();
  }

  public Point getSource() {
    return source;
  }

  void updateVisibility() {
    setVisible(from.isVisible() && to.isVisible());
  }

  @Override
  public void componentHidden(ComponentEvent arg0) {
    updateVisibility();
  }

  @Override
  public void componentMoved(ComponentEvent arg0) {
    updateComponent();
  }

  @Override
  public void componentResized(ComponentEvent arg0) {
  }

  @Override
  public void componentShown(ComponentEvent arg0) {
    updateComponent();
    updateVisibility();
  }
}
