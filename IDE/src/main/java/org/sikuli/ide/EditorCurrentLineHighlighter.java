/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import org.sikuli.basics.Debug;

public class EditorCurrentLineHighlighter implements CaretListener {

  private static final String me = "EditorCurrentLineHighlighter: ";
  static final Color DEFAULT_COLOR = new Color(230, 230, 210);
  static final Color ERROR_COLOR = new Color(255, 105, 105);
  private Highlighter.HighlightPainter painter;
  private Object highlight = null;

  public EditorCurrentLineHighlighter(JTextPane textPane) {
    this(textPane, null);
  }

  public EditorCurrentLineHighlighter(JTextPane textPane, Color highlightColor) {
    Color c = highlightColor != null ? highlightColor : DEFAULT_COLOR;
    MyHighlighter h = new MyHighlighter();
    textPane.setHighlighter(h);
    painter = new DefaultHighlighter.DefaultHighlightPainter(c);
  }

  @Override
  public void caretUpdate(CaretEvent evt) {
    JTextComponent comp = (JTextComponent) evt.getSource();
    if (comp != null) {
      if (comp.getSelectionStart() != comp.getSelectionEnd()) {
        // cancel line highlighting if selection exists
        removeLineHighlight(comp);
        comp.repaint();
        return;
      }
      int pos = comp.getCaretPosition();
      Element elem = Utilities.getParagraphElement(comp, pos);
      int start = elem.getStartOffset();
      int end = elem.getEndOffset();
      Document doc = comp.getDocument();
      Element root = doc.getDefaultRootElement();
      int line = root.getElementIndex(pos);
      Debug.log(5, "LineHighlight: Caret at " + pos + " line " + line + " for " + start + "-" + end);
      if (SikuliIDE.getStatusbar() != null) {
        SikuliIDE.getStatusbar().setCaretPosition(line + 1, pos - start + 1);
      }
      removeLineHighlight(comp);
      try {
        highlight = comp.getHighlighter().addHighlight(start, end, painter);
        comp.repaint();
      } catch (BadLocationException ex) {
        Debug.error(me + "Problem while highlighting line %d\n%s", pos, ex.getMessage());
      }
    }
  }

  private void removeLineHighlight(JTextComponent comp) {
    if (highlight != null) {
      comp.getHighlighter().removeHighlight(highlight);
      highlight = null;
    }
  }
}

//<editor-fold defaultstate="collapsed" desc="class MyHighlighter extends DefaultHighlighter">
class MyHighlighter extends DefaultHighlighter {

  private JTextComponent component;
  private Rectangle a = null;

  @Override
  public final void install(final JTextComponent c) {
    super.install(c);
    this.component = c;
  }

  @Override
  public final void deinstall(final JTextComponent c) {
    super.deinstall(c);
    this.component = null;
  }

  @Override
  public final void paint(final Graphics g) {
    final Highlighter.Highlight[] highlights = getHighlights();
    final int len = highlights.length;
    if (len == 0) {
      return;
    }
    if (highlights[0].getClass().getName().indexOf("LayeredHighlightInfo") > -1) {
      Debug.log(6, "LineHighlight: painting enter for " + len);
      a = this.component.getBounds();
      final Insets insets = this.component.getInsets();
      a.x = insets.left;
      Debug.log(6, "LineHighlight: painting 0");
      highlights[0].getPainter().paint(g,
                highlights[0].getStartOffset(), highlights[0].getEndOffset(), a, this.component);
    }
    for (int i = 1; i < len; i++) {
      if (highlights[i].getClass().getName().indexOf("LayeredHighlightInfo") > -1) {
        Debug.log(6, "LineHighlight: painting " + i);
        highlights[i].getPainter().paint(g,
                highlights[i].getStartOffset(), highlights[i].getEndOffset(), a, this.component);
      }
    }
  }
}
//</editor-fold>
