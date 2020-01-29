/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import javax.swing.*;
import java.awt.Color;
import java.awt.Container;

public class IDESplash extends JFrame {
  JLabel action;
  JLabel step;

  public IDESplash(String version, String jversion) {
    init(version, jversion);
  }

  void init(String version, String jversion) {
    setResizable(false);
    setUndecorated(true);
    Container pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(Color.lightGray, 5));
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(new JLabel(" "));
    pane.add(new JLabel(" "));
    JLabel title = new JLabel(String .format("SikuliX-IDE %s is starting on Java %s", version, jversion));
    title.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(title);
    pane.add(new JLabel(" "));
    pane.add(new JLabel(" "));
    pack();
    setSize(500, 100);
    setLocationRelativeTo(null);
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
