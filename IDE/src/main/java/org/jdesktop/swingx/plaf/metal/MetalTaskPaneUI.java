/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.metal;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.basic.BasicTaskPaneUI;

/**
 * Metal implementation of the <code>JXTaskPane</code> UI. <br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class MetalTaskPaneUI extends BasicTaskPaneUI {

  public static ComponentUI createUI(JComponent c) {
    return new MetalTaskPaneUI();
  }

  @Override
  protected void installDefaults() {
    super.installDefaults();

    LookAndFeel.installProperty(group, "opaque", false);
  }

  @Override
  protected Border createPaneBorder() {
    return new MetalPaneBorder();
  }

  /**
   * The border of the task pane group paints the "text", the "icon",
   * the "expanded" status and the "special" type.
   *
   */
  class MetalPaneBorder extends PaneBorder {

    @Override
    protected void paintExpandedControls(JXTaskPane group, Graphics g, int x,
      int y, int width, int height) {
      ((Graphics2D)g).setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(getPaintColor(group));
      paintRectAroundControls(group, g, x, y, width, height, g.getColor(), g
        .getColor());
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
