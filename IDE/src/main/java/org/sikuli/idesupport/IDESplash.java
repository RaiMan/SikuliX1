/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.Image;
import org.sikuli.script.support.Commons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class IDESplash extends JFrame {

  public IDESplash(Object[] ideWindow) {
    initForIDE(ideWindow);
  }

  void initForIDE(Object[] ideWindow) {
    final Dimension size = (Dimension) ideWindow[0];
    setResizable(false);
    setUndecorated(true);
    Container pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(new Color(0x9D, 0x42, 0x30, 208), 3));
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    JLabel bar = new JLabel(" "); bar.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
    //pane.add(bar);
    BufferedImage logo; //= Toolkit.getDefaultToolkit().createImage(SikulixIDE.class.getResource("/icons/sikulix-red-x.png"));
    try {
      logo = ImageIO.read(SikulixIDE.class.getResource("/icons/sikulix-red-x.png"));
      JLabel lblLogo = new JLabel();
      lblLogo.setIcon(new ImageIcon(logo));
      pane.add(lblLogo);
    } catch (IOException e) {
    }
    pane.add(bar);
    final String titleText = String.format("---  SikuliX-IDE  ---  %s  ---  starting on Java %s  ---",
        Commons.getSXVersion(), Commons.getJavaVersion());
    JLabel title = new JLabel(titleText);
    int fontsize = 20;
    Font titleFont = new Font(Font.MONOSPACED, Font.BOLD, fontsize);
    FontMetrics metrics = title.getFontMetrics(titleFont);
    Rectangle2D textLen = metrics.getStringBounds(titleText, getGraphics());
    if (textLen.getWidth() > size.width) {
      fontsize = (int) (fontsize * size.width / textLen.getWidth());
      titleFont = new Font(Font.MONOSPACED, Font.BOLD, fontsize);
    }
    title.setFont(titleFont);
    title.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(title);
    pane.add(new JLabel(" "));
    pane.add(new JLabel(" "));
    pack();
    setSize(size);
    setLocation((Point) ideWindow[1]);
    setAlwaysOnTop(true);
    setVisible(true);
  }

  public void showAction(String actionText) {
//    action.setText(actionText);
//    repaint();
  }

  public void showStep(String stepTitle) {
//    step.setText(stepTitle);
//    repaint();
  }
}
