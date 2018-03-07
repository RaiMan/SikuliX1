/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.Dimension;
import javax.swing.JLabel;

import javax.swing.JTextPane;

class HTMLTextPane extends JTextPane {

  int maximum_width;
  String text;
  public Dimension preferredDimension;
  Visual comp = null;
  String htmltxt;

  public HTMLTextPane(Visual comp) {
    this.comp = comp;
    maximum_width = comp.maxWidth - 10;
    init();
  }

  public HTMLTextPane() {
    maximum_width = 400;
    init();
  }

  private void init() {
    setContentType("text/html");
  }

  @Override
  public void setText(String text) {
    this.text = text;
    if (comp != null) {
      maximum_width = comp.maxWidth - 2 * comp.PADDING_X;
      htmltxt = "<html><div style='" + comp.getStyleString() + "'>"
              + text + "</div></html>";
    } else {
      htmltxt = "<html><font size=5>"+text+"</font></html>";
    }
    super.setText(htmltxt);
    JTextPane tp = new JTextPane();
    tp.setText(htmltxt);
    if (getPreferredSize().getWidth() > maximum_width) {
      // hack to limit the width of the text to width
      if (comp != null) {
        htmltxt = "<html><div style='width:" + maximum_width + ";" + comp.getStyleString() + "'>"
                  + text + "</div></html>";
      } else {
        htmltxt = "<html><div width='"+maximum_width+"'><font size=5>"+text+"</font></div></html>";
      }
      super.setText(htmltxt);
    }
    setSize(getPreferredSize());
  }

  @Override
  public String getText() {
    return this.text;
  }

}
