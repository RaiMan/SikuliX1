/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.windows;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.basic.BasicTaskPaneUI;

/**
 * Windows implementation of the TaskPaneUI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class WindowsTaskPaneUI extends BasicTaskPaneUI {

  public static ComponentUI createUI(JComponent c) {
    return new WindowsTaskPaneUI();
  }

  @Override
  protected Border createPaneBorder() {
    return new XPPaneBorder();
  }

  /**
   * Overriden to paint the background of the component but keeping the rounded
   * corners.
   */
  @Override
  public void update(Graphics g, JComponent c) {
    if (c.isOpaque()) {
      g.setColor(c.getParent().getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
      g.setColor(c.getBackground());
      g.fillRect(0, getRoundHeight(), c.getWidth(), c.getHeight() - getRoundHeight());
    }
    paint(g, c);
  }

  /**
   * The border of the taskpane group paints the "text", the "icon", the
   * "expanded" status and the "special" type.
   *
   */
  class XPPaneBorder extends PaneBorder {

    @Override
    protected void paintTitleBackground(JXTaskPane group, Graphics g) {
      if (group.isSpecial()) {
        g.setColor(specialTitleBackground);
        g.fillRoundRect(
          0,
          0,
          group.getWidth(),
          getRoundHeight() * 2,
          getRoundHeight(),
          getRoundHeight());
        g.fillRect(
          0,
          getRoundHeight(),
          group.getWidth(),
          getTitleHeight(group) - getRoundHeight());
      } else {
        Paint oldPaint = ((Graphics2D)g).getPaint();
        GradientPaint gradient = new GradientPaint(
          0f,
          group.getWidth() / 2,
          group.getComponentOrientation().isLeftToRight()?
            titleBackgroundGradientStart
            :titleBackgroundGradientEnd,
          group.getWidth(),
          getTitleHeight(group),
          group.getComponentOrientation().isLeftToRight()?
            titleBackgroundGradientEnd
            :titleBackgroundGradientStart);

        ((Graphics2D)g).setRenderingHint(
          RenderingHints.KEY_COLOR_RENDERING,
          RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        ((Graphics2D)g).setRenderingHint(
          RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D)g).setRenderingHint(
          RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D)g).setPaint(gradient);
        g.fillRoundRect(
          0,
          0,
          group.getWidth(),
          getRoundHeight() * 2,
          getRoundHeight(),
          getRoundHeight());
        g.fillRect(
          0,
          getRoundHeight(),
          group.getWidth(),
          getTitleHeight(group) - getRoundHeight());
        ((Graphics2D)g).setPaint(oldPaint);
      }
    }

    @Override
    protected void paintExpandedControls(JXTaskPane group, Graphics g, int x,
      int y, int width, int height) {
      ((Graphics2D)g).setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

      paintOvalAroundControls(group, g, x, y, width, height);
      g.setColor(getPaintColor(group));
      paintChevronControls(group, g, x, y, width, height);

      ((Graphics2D)g).setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    @Override
    protected boolean isMouseOverBorder() {
      return true;
    }

  }

}
