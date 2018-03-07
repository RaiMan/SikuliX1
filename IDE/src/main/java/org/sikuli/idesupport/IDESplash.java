/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import java.awt.Color;
import static java.awt.Component.CENTER_ALIGNMENT;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.sikuli.script.RunTime;

public class IDESplash extends JFrame {
  RunTime runTime = null;
  JLabel action;
  JLabel step;

  public IDESplash(RunTime rt) {
    init(rt);
  }

  void init(RunTime rt) {
    runTime = rt;
    setResizable(false);
    setUndecorated(true);
    Container pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(Color.lightGray, 5));
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(new JLabel(" "));
    JLabel title = new JLabel("SikuliX-IDE is starting");
    title.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(title);
    pane.add(new JLabel(" "));
    action = new JLabel(" ");
    action.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(action);
    pane.add(new JLabel(" "));
    step = new JLabel("... starting");
    step.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(step);
    pane.add(new JLabel(" "));
    JLabel version = new JLabel(String.format("%s-%s", rt.getVersionShort(), rt.sxBuildStamp));
    version.setAlignmentX(CENTER_ALIGNMENT);
    pane.add(version);
    pane.add(new JLabel(" "));
    pack();
    setSize(200, 155);
    setLocationRelativeTo(null);
    setAlwaysOnTop(true);
    setVisible(true);
  }

  public void showAction(String actionText) {
    action.setText(actionText);
    repaint();
  }

  public void showStep(String stepTitle) {
    step.setText(stepTitle);
    repaint();
  }
}
