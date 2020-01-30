/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import javax.swing.*;

import com.explodingpixels.macwidgets.plaf.EmphasizedLabelUI;
import org.sikuli.script.support.RunTime;

import java.util.Date;

class SikuliIDEStatusBar extends JPanel {

  private JLabel _lblMsg;
  private JLabel _lblCaretPos;
  private String currentContentType = "???";
  private int currentRow;
  private int currentCol;
  private long starting;

  public SikuliIDEStatusBar() {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(10, 20));

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setOpaque(false);
    _lblMsg = new JLabel();
    _lblMsg.setPreferredSize(new Dimension(500, 20));
    _lblMsg.setUI(new EmphasizedLabelUI());
    _lblMsg.setFont(new Font("Monaco", Font.TRUETYPE_FONT, 11));
    _lblCaretPos = new JLabel();
    _lblCaretPos.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
    _lblCaretPos.setUI(new EmphasizedLabelUI());
    _lblCaretPos.setFont(UIManager.getFont("Button.font").deriveFont(11.0f));
    setCaretPosition(1, 1);
    resetMessage();
    add(_lblMsg, BorderLayout.WEST);
    add(_lblCaretPos, BorderLayout.LINE_END);
//    add(rightPanel, BorderLayout.EAST);
  }

  public void setType(String contentType) {
    if (contentType == null) {
      return;
    }
    currentContentType = contentType.replaceFirst(".*?\\/", "");
    if (currentContentType.equals("null")) {
      currentContentType = "text";
    }
    setCaretPosition(-1, 0);
  }

  public void setCaretPosition(int row, int col) {
    if (row > -1) {
      currentRow = row;
      currentCol = col;
    }
    _lblCaretPos.setText(String.format("(%s) | R: %d | C: %d", currentContentType, currentRow, currentCol));
    if (starting > 0 && new Date().getTime() - starting > 3000) {
      resetMessage();
    }
  }

  public void setMessage(String text) {
    _lblMsg.setText("   " + text);
    repaint();
    starting = new Date().getTime();
  }

  public void resetMessage() {
    String buildNumber = SikulixIDE.runTime.SXBuildNumber;
    String message = SikulixIDE.runTime.SXVersionIDE;
    if (!buildNumber.isEmpty()) {
      message += String.format(" build#: %s (%s)", buildNumber, SikulixIDE.runTime.SXBuild);
    }
    if (RunTime.isSandbox()) {
      message += " (Sandbox)";
    }
    if (RunTime.isDevelop()) {
      message += " (dev)";
    }
    setMessage(message);
    starting = 0;
  }
//  @Override
//  protected void paintComponent(Graphics g) {
//    super.paintComponent(g);
//    int y = 0;
//    g.setColor(new Color(156, 154, 140));
//    g.drawLine(0, y, getWidth(), y);
//    y++;
//    g.setColor(new Color(196, 194, 183));
//    g.drawLine(0, y, getWidth(), y);
//  }
}
