/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import org.sikuli.script.Region;

public class SxCallout extends Visual {

  static final int TRIANGLE_SIZE = 20;
  static int defMaxWidth = 400;
  static int defFontSize = 14;
  static String defFont = "Verdana";
  HTMLTextPane textArea;
  RoundedBox rbox;
  Triangle triangle;
  int dx = 0;
  int dy = 0;

  public SxCallout(String text) {
    super();
    init(text);
  }

  void init(String text) {
    layout = Layout.RIGHT;
    maxWidth = defMaxWidth;
    fontName = defFont;
    fontSize = defFontSize;
    this.text = text;
    textArea = new HTMLTextPane(this);
    rbox = new RoundedBox(textArea.getBounds());
    add(textArea);
    add(rbox);
    triangle = new Triangle();
    add(triangle);
    targetRegion = null;
    setColors(null, Color.YELLOW, Color.YELLOW, null, null);
    makeComponent();
  }

  @Override
  public void updateComponent() {
    textArea.setText(text);
    textArea.setLocation(PADDING_X, PADDING_Y);
    Rectangle rect = textArea.getBounds();
    rect.grow(PADDING_X, PADDING_Y);
    rbox.setBounds(rect);
    makeComponent();
    triangle.setForeground(colorBack);
    rbox.setForeground(colorBack);
    if (targetRegion != null) {
      super.setLocationRelativeToRegion(targetRegion, layout);
    }
  }

  @Override
  public Visual setLocationRelativeToRegion(Region region, Layout side) {
    if (side != layout) {
      layout = side;
      updateComponent();
    }
    targetRegion = region;
    return super.setLocationRelativeToRegion(targetRegion, side);
  }

  private void makeComponent() {
    if (layout == Layout.TOP) {
      triangle.rotate(0);
      dx = 0; dy = 0;
      triangle.setLocationRelativeToComponent(rbox, Layout.BOTTOM);
    } else if (layout == Layout.BOTTOM) {
      dx = 0; dy = TRIANGLE_SIZE;
      triangle.rotate(Math.PI);
      triangle.setLocationRelativeToComponent(rbox, Layout.TOP);
    } else if (layout == Layout.LEFT) {
      dx = 0; dy = 0;
      triangle.rotate(-Math.PI / 2);
      triangle.setLocationRelativeToComponent(rbox, Layout.RIGHT);
    } else if (layout == Layout.RIGHT) {
      dx = TRIANGLE_SIZE; dy = 0;
      triangle.rotate(Math.PI / 2);
      triangle.setLocationRelativeToComponent(rbox, Layout.LEFT);
    }
    Rectangle bounds = rbox.getBounds();
    bounds.add(triangle.getBounds());
    setActualBounds(bounds);
  }

  @Override
  public void paintComponent(Graphics g) {
    g.translate(dx, dy);
    super.paintComponent(g);
  }

  class Triangle extends Visual {

    GeneralPath gp;

    public Triangle() {
      super();
      init();
    }

    private void init() {
      gp = new GeneralPath();
      gp.moveTo(TRIANGLE_SIZE * 0.45, 0);
      gp.lineTo(TRIANGLE_SIZE * 0.5, TRIANGLE_SIZE);
      gp.lineTo(TRIANGLE_SIZE * 0.85, 0);
      gp.closePath();
      setActualSize(new Dimension(TRIANGLE_SIZE, TRIANGLE_SIZE));
    }

    public void rotate(double radius) {
      init();
      AffineTransform rat = new AffineTransform();
      rat.rotate(radius, TRIANGLE_SIZE / 2, TRIANGLE_SIZE / 2);
      gp.transform(rat);
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.fill(gp);
    }
  }

  class RoundedBox extends Visual {

    public RoundedBox(Rectangle rect) {
      super();
      setActualBounds(rect);
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
      g2d.fill(roundedRectangle);
    }
  }
}
