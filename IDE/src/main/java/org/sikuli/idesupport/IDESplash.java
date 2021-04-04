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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class IDESplash extends JFrame {

  IDESplash instance = null;

  public IDESplash(Object[] ideWindow) {
    instance = this;
    initForIDE(ideWindow);
  }

  void initForIDE(Object[] ideWindow) {
    final Dimension size = (Dimension) ideWindow[0];
    setResizable(false);
    setUndecorated(true);
    Container pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(new Color(0x9D, 0x42, 0x30, 208), 3));
    pane.setLayout(null); //new BorderLayout(10,10));
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        instance.setVisible(false);
        SikulixIDE.get().setVisible(true);
      }
    });
    JLabel bar = new JLabel(" "); bar.setFont(new Font(Font.MONOSPACED, Font.BOLD, 72));

    BufferedImage logo = null;
    JLabel lblLogo = new JLabel();
    int nextPos = 0;
    int pad = 20;
    try {
      logo = ImageIO.read(SikulixIDE.class.getResource("/icons/sikulix-red-x.png"));
      lblLogo.setIcon(new ImageIcon(logo));
      int wLogo = logo.getWidth();
      int hLogo = logo.getHeight();
      int posX = (size.width - wLogo) / 2;
      int posY = (size.height - hLogo) / 2;
      lblLogo.setBounds(posX, -posY + pad, size.width, size.height);
      nextPos = pad + hLogo;
    } catch (IOException e) {
    }

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
      textLen = metrics.getStringBounds(titleText, getGraphics());
    }
    title.setFont(titleFont);
    int posX = (size.width - (int) textLen.getWidth()) / 2;
    title.setBounds(posX, nextPos + pad * 2, (int) textLen.getWidth(), (int) textLen.getHeight());

    pane.add(lblLogo);
    pane.add(title);

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
