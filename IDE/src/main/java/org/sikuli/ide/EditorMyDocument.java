/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import org.sikuli.basics.Debug;

class EditorMyDocument extends DefaultStyledDocument {
   @Override
      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
         str = str.replaceAll("\t", "  ");
         super.insertString(offs, str, a);
         Debug.log(5, "insertString: " + str);
      }
}
