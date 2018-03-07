/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.JXTipOfTheDay.ShowOnStartupChoice;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.plaf.basic.BasicTipOfTheDayUI;

/**
 * Windows implementation of the TipOfTheDayUI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class WindowsTipOfTheDayUI extends BasicTipOfTheDayUI {

  public static ComponentUI createUI(JComponent c) {
    return new WindowsTipOfTheDayUI((JXTipOfTheDay)c);
  }

  public WindowsTipOfTheDayUI(JXTipOfTheDay tipPane) {
    super(tipPane);
  }

  @Override
  public JDialog createDialog(Component parentComponent,
    final ShowOnStartupChoice choice) {
    return createDialog(parentComponent, choice, false);
  }

  @Override
protected void installComponents() {
    tipPane.setLayout(new BorderLayout());

    // tip icon
    JLabel tipIcon = new JLabel();
    tipIcon.setPreferredSize(new Dimension(60, 100));
    tipIcon.setIcon(UIManager.getIcon("TipOfTheDay.icon"));
    tipIcon.setHorizontalAlignment(JLabel.CENTER);
    tipIcon.setVerticalAlignment(JLabel.TOP);
    tipIcon.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
    tipPane.add("West", tipIcon);

    // tip area
    JPanel rightPane = new JPanel(new BorderLayout());
    JLabel didYouKnow = new JLabel(UIManagerExt
      .getString("TipOfTheDay.didYouKnowText", tipPane.getLocale()));
    didYouKnow.setPreferredSize(new Dimension(50, 32));
    didYouKnow.setOpaque(true);
    didYouKnow.setBackground(UIManager.getColor("TextArea.background"));
    didYouKnow.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0,
      0, 2, 0, tipPane.getBackground()), BorderFactory.createEmptyBorder(4, 4,
      4, 4)));
    didYouKnow.setFont(tipPane.getFont().deriveFont(Font.BOLD, 15));
    rightPane.add("North", didYouKnow);

    tipArea = new JPanel(new BorderLayout());
    tipArea.setOpaque(true);
    tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tipArea.setBackground(UIManager.getColor("TextArea.background"));
    rightPane.add("Center", tipArea);

    tipPane.add("Center", rightPane);
  }

  public static class TipAreaBorder implements Border {
    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(2, 2, 2, 2);
    }
    @Override
    public boolean isBorderOpaque() {
      return false;
    }
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
      int height) {
      g.setColor(UIManager.getColor("TipOfTheDay.background"));
      g.drawLine(x, y, x + width - 1, y);
      g.drawLine(x, y, x, y + height - 1);

      g.setColor(Color.black);
      g.drawLine(x + 1, y + 1, x + width - 3, y + 1);
      g.drawLine(x + 1, y + 1, x + 1, y + height - 3);

      g.setColor(Color.white);
      g.drawLine(x, y + height - 1, x + width, y + height - 1);
      g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
    }
  }

}
