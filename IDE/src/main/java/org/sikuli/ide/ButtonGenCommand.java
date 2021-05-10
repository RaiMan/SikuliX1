/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.net.URL;

import org.sikuli.basics.Debug;

public class ButtonGenCommand extends JButton implements ActionListener,
        MouseListener {

  String _cmd;
  String[] _params;
  String _desc;
  EditorPane pane;
  PreferencesUser pref;

  final static String DefaultStyle = "color:black;font-family:monospace;font-size:10px; font-weight:normal",
          HoverStyle = "color:#3333ff;font-family:monospace;font-size:10px; font-weight:bold;",
          PressedStyle = "color:#3333ff;font-family:monospace;font-size:10px; font-weight:bold;text-decoration:underline;";

  public ButtonGenCommand(String cmd, String desc, String... params) {
    super(getRichRepresentation(DefaultStyle, cmd, desc, params, false));
    _cmd = cmd;
    _params = params;
    _desc = desc;
    setToolTipText(getRichRepresentation(DefaultStyle, cmd, desc, params, true));
    setHorizontalAlignment(SwingConstants.LEFT);
    addActionListener(this);
    addMouseListener(this);
    setBorderPainted(false);
    setBorder(BorderFactory.createEmptyBorder(1, 2, 2, 1));
    setContentAreaFilled(false);
  }

  static String getParamHTML(String p, boolean first, boolean showOptParam) {
    URL imgPattern = SikulixIDE.class.getResource("/icons/capture-small.png");
    String item = "";
    if (!first) {
      item += ", ";
    }
    if (p.equals("PATTERN")) {
      item += "<img src=\"" + imgPattern + "\">";
    } else {
      if (p.startsWith("[") && p.endsWith("]")) {
        if (showOptParam) {
          item += "<i>" + p + "</i>";
        }
      } else {
        if (p.startsWith("_")) {
          item += "<u>" + p.charAt(1) + "</u>" + p.substring(2);
        } else {
          item += p;
        }
      }
    }
    return !item.equals(", ") ? item : "";
  }

  static String getRichRepresentation(String style, String cmd, String desc, String[] params, boolean showOptParam) {
    StringBuilder ret = new StringBuilder("<html><table><tr><td valign=\"middle\">"
        + "<span style=\"" + style + "\">" + cmd + "(");
    int count = 0;
    for (String p : params) {
      String item = getParamHTML(p, count == 0, showOptParam);
      if (!item.equals("")) {
        ret.append("<td valign=\"middle\" style=\"").append(style).append("\">").append(item);
      }
      count++;
    }
    ret.append("<td>)</table>");
    if (showOptParam) {
      ret.append("<p> ").append(desc);
    }
    return ret.toString();
  }

  static String getTextRepresentation(String cmd, String[] params) {
    StringBuilder ret = new StringBuilder("" + cmd + "(");
    int count = 0;
    for (String p : params) {
      ret.append(p);
      if (++count < params.length) {
        ret.append(", ");
      }
    }
    ret.append(")");
    return ret.toString();
  }

  @Override
  public String toString() {
    return getTextRepresentation(_cmd, _params);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    SikulixIDE ide = SikulixIDE.get();
    pane = ide.getCurrentCodePane();
    pref = PreferencesUser.get();
    SwingUtilities.invokeLater(this::insertCommand);
  }

  public void insertCommand() {
    pref = PreferencesUser.get();
    int endPos = -1, endPosLen = 0;
    boolean first = true;
    ButtonCapture btnCapture;
    Element line;
    pane.insertString(_cmd + "(");
    for (String p : _params) {
      if (p.equals("PATTERN")) {
        line = pane.getLineAtCaret(-1);
        if (!first) {
          pane.insertString(", ");
        } else {
          first = false;
        }
        if (pref.getAutoCaptureForCmdButtons()) {
          btnCapture = null; //TODO new ButtonCapture(pane, line);
          pane.insertComponent(btnCapture);
          btnCapture.captureWithAutoDelay();
        } else {
          if (pane.context.getShowThumbs() && pref.getPrefMoreImageThumbs()) {
            //TODO pane.insertComponent(new ButtonCapture(pane, line));
          } else {
            pane.insertComponent(new EditorPatternLabel(pane, ""));
          }
        }
        continue;
      }
      if (!p.startsWith("[")) {
        if (!first) {
          pane.insertString(", ");
        }
        if (p.startsWith("_")) {
          endPos = pane.getCaretPosition();
          p = p.substring(1);
        }
        endPosLen = p.length();
        pane.insertString(p);
        first = false;
      }
    }
    pane.insertString(")");
    (new SikuliEditorKit.InsertBreakAction()).insertBreak(pane);
    if (endPos >= 0) {
      pane.requestFocus();
      pane.setCaretPosition(endPos);
      pane.setSelectionStart(endPos);
      pane.setSelectionEnd(endPos + endPosLen);
      Debug.log(5, "sel: " + pane.getSelectedText());
    }
    pane.requestFocus();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    setText(getRichRepresentation(HoverStyle, _cmd, _desc, _params, false));
  }

  @Override
  public void mouseExited(MouseEvent e) {
    setText(getRichRepresentation(DefaultStyle, _cmd, _desc, _params, false));
  }

  @Override
  public void mousePressed(MouseEvent e) {
    setText(getRichRepresentation(PressedStyle, _cmd, _desc, _params, false));
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    setText(getRichRepresentation(HoverStyle, _cmd, _desc, _params, false));
  }
}
