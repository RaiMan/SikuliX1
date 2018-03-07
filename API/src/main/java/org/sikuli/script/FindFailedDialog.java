/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * INTERNAL USE
 */
class FindFailedDialog extends JDialog implements ActionListener {

  JButton retryButton;
  JButton skipButton;
  JButton abortButton;
  FindFailedResponse _response;
  boolean isCapture = false;

  public FindFailedDialog(org.sikuli.script.Image target) {
    init(target, false);
  }

  public FindFailedDialog(org.sikuli.script.Image target, boolean isCapture) {
    init(target, isCapture);
  }

  private void init(org.sikuli.script.Image target, boolean isCapture) {
    this.isCapture = isCapture;
    setModal(true);
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    Component targetComp = createTargetComponent(target);
    panel.add(targetComp, BorderLayout.NORTH);
    JPanel buttons = new JPanel();
    String textRetry = "Retry";
    if (isCapture) {
      textRetry = "Capture";
    }
    String textSkip = "Capture/Skip";
    if (isCapture) {
      textSkip = "Skip";
    }
    retryButton = new JButton(textRetry);
    retryButton.addActionListener(this);
    skipButton = new JButton(textSkip);
    skipButton.addActionListener(this);
    abortButton = new JButton("Abort");
    abortButton.addActionListener(this);
    buttons.add(retryButton);
    buttons.add(skipButton);
    buttons.add(abortButton);
    panel.add(buttons, BorderLayout.SOUTH);
    add(panel);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        _response = FindFailedResponse.ABORT;
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (retryButton == e.getSource()) {
      _response = FindFailedResponse.RETRY;
    } else if (abortButton == e.getSource()) {
      _response = FindFailedResponse.ABORT;
    } else if (skipButton == e.getSource()) {
      _response = FindFailedResponse.SKIP;
    }
    dispose();
  }

  public FindFailedResponse getResponse() {
    return _response;
  }

  <PatternString> Component createTargetComponent(org.sikuli.script.Image img) {
    JLabel cause = null;
    JPanel dialog = new JPanel();
    dialog.setLayout(new BorderLayout());
    if (img.isValid()) {
      if (!img.isText()) {
        Image bimage = img.get(false);
        if (bimage != null) {
          String rescale = "";
          JLabel iconLabel = new JLabel();
          int w = bimage.getWidth(this);
          int h = bimage.getHeight(this);
          if (w > 500) {
            w = 500;
            h = -h;
            rescale = " (rescaled 500x...)";
          }
          if (h > 300) {
            h = 300;
            w = -w;
            rescale = " (rescaled ...x300)";
          }
          if (h < 0 && w < 0) {
            w = 500;
            h = 300;
            rescale = " (rescaled 500x300)";
          }
          bimage = bimage.getScaledInstance(w, h, Image.SCALE_DEFAULT);
          iconLabel.setIcon(new ImageIcon(bimage));
          cause = new JLabel("Cannot find " + img.getName() + rescale);
          dialog.add(iconLabel, BorderLayout.PAGE_END);
        }
      } else {
        cause = new JLabel("Sikuli cannot find text:" + img.getName());
      }
    }
    if (isCapture) {
      cause = new JLabel("Request to capture: " + img.getName());
    }
    dialog.add(cause, BorderLayout.PAGE_START);
    return dialog;
  }

  @Override
  public void setVisible(boolean flag) {
    if (flag) {
//TODO Can not be called in the constructor (as JFRrame?)
// Doing so somehow made it impossible to keep
// the dialog always on top.
      pack();
      setAlwaysOnTop(true);
      setResizable(false);
      setLocationRelativeTo(this);
      requestFocus();
    }
    super.setVisible(flag);
  }
}
