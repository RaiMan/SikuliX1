/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuli.idesupport;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 *
 * @author rhocke
 */
public interface IIndentationLogic {

	public void setTabWidth(int tabwidth);

	public int checkDedent(String leadingWhitespace, int line);

	public void checkIndent(String leadingWhitespace, int line);

	public boolean shouldAddColon();

	public void setLastLineEndsWithColon();

	public int shouldChangeLastLineIndentation();

	public int shouldChangeNextLineIndentation();

	public void reset();

	public void addText(String text);

  public String getLeadingWhitespace(String text) ;

  public String getLeadingWhitespace(StyledDocument doc, int head, int len) throws BadLocationException;

  public int atEndOfLine(StyledDocument doc, int cpos, int start, String s, int sLen);

}
