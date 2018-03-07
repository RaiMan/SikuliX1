/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.nimbus;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.basic.BasicTaskPaneUI;

/**
 * Nimbus implementation of the <code>JXTaskPane</code> UI. <br>
 *
 * @author <a href="mailto:Radu.Dumitrescu@pss.ro">Radu Dumitrescu</a>
 */
public class NimbusTaskPaneUI extends BasicTaskPaneUI {

  public static ComponentUI createUI(JComponent c) {
    return new NimbusTaskPaneUI();
  }

  @Override
protected Border createPaneBorder() {
    return new NimbusPaneBorder();
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
      g.fillRect(0, getRoundHeight(), c.getWidth(), c.getHeight() -
getRoundHeight());
    }
    paint(g, c);
  }

        /**
         * The border of the task pane group paints the "text", the "icon", the
         * "expanded" status and the "special" type.
         *
         */
        class NimbusPaneBorder extends PaneBorder {

                @Override
                protected void paintTitleBackground(JXTaskPane group, Graphics g) {

                        Paint oldPaint = ((Graphics2D) g).getPaint();

                        roundHeight = 7;

                        if (group.isSpecial()) {
                                g.setColor(specialTitleBackground);

                                g.fillRoundRect(0, 0, group.getWidth(), getRoundHeight() * 2,
                                                getRoundHeight(), getRoundHeight());
                                g.fillRect(0, getRoundHeight(), group.getWidth(),
                                                getTitleHeight(group) - getRoundHeight());

                        } else {
                                Color[] colors = { titleBackgroundGradientStart,
                                                titleBackgroundGradientEnd };

                                float[] fractions = { 0.0f, 1.0f };

                                LinearGradientPaint gradient = new LinearGradientPaint(group
                                                .getWidth() / 2, 0.0f, group.getWidth() / 2,
                                                getTitleHeight(group), fractions, colors);

                                ((Graphics2D) g).setPaint(gradient);

                                ((Graphics2D) g).setRenderingHint(
                                                RenderingHints.KEY_COLOR_RENDERING,
                                                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                                ((Graphics2D) g).setRenderingHint(
                                                RenderingHints.KEY_INTERPOLATION,
                                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
                                                RenderingHints.VALUE_RENDER_QUALITY);
                                ((Graphics2D) g).setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                g.fillRoundRect(0, 0, group.getWidth(),
                                                getTitleHeight(group) / 2, getRoundHeight(),
                                                getRoundHeight());

                                g.fillRect(0, getRoundHeight(), group.getWidth(),
                                                getTitleHeight(group) - getRoundHeight());

                        }

                        // draw the border around the title area
                        g.setColor(borderColor);

                        g.drawRoundRect(0, 0, group.getWidth() - 1, getTitleHeight(group)
                                        + getRoundHeight(), getRoundHeight(), getRoundHeight());
                        g.drawLine(0, getTitleHeight(group) - 1, group.getWidth(),
                                        getTitleHeight(group) - 1);

                        ((Graphics2D) g).setPaint(oldPaint);
                }

                @Override
                protected void paintExpandedControls(JXTaskPane group, Graphics g,
                                int x, int y, int width, int height) {
                        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g.setColor(getPaintColor(group));
                        paintChevronControls(group, g, x, y, width, height);

                        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                @Override
                protected void paintTitle(JXTaskPane group, Graphics g,
                                Color textColor, int x, int y, int width, int height) {
                        configureLabel(group);
                        // Nimbus has some issues with ColorUIResource
                        label.setForeground(new Color(textColor.getRGB()));
                        if (group.getFont() != null
                                        && !(group.getFont() instanceof FontUIResource)) {
                                label.setFont(group.getFont());
                        }
                        g.translate(x, y);
                        label.setBounds(0, 0, width, height);
                        label.paint(g);
                        g.translate(-x, -y);
                }

                @Override
                protected boolean isMouseOverBorder() {
                        return true;
                }
        }

}
