/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.sikuli.basics.Debug;

public class EditorLineNumberView extends JComponent implements MouseListener {

  private static ImageIcon ERROR_ICON = SikulixIDE.getIconResource("/icons/error_icon.gif");
  // This is for the border to the right of the line numbers.
  // There's probably a UIDefaults value that could be used for this.
  private static final Color BORDER_COLOR = new Color(155, 155, 155);
  private static Color FG_COLOR = Color.GRAY;
  private static Color BG_COLOR = new Color(241, 241, 241);
  private static Color selBG_COLOR = new Color(220, 220, 220);
  private static final int WIDTH_TEMPLATE = 999;
  private static final int MARGIN = 5;
  private FontMetrics viewFontMetrics;
  private int maxNumberWidth;
  private int componentWidth;
  private int textTopInset;
  private int textFontAscent;
  private int textFontHeight;

  public EditorPane getEditorPane() {
    return editorPane;
  }

  private EditorPane editorPane;
  private SizeSequence sizes;
  private int startLine = 0;
  private boolean structureChanged = true;
  private Set<Integer> errLines = new HashSet<>();
  private int line;
  private SikuliIDEPopUpMenu popMenuLineNumber = null;
  private boolean wasPopup = false;

  public EditorLineNumberView(JTextComponent editorPane) {
    /**
     * Construct a LineNumberView and attach it to the given editorPane component. The LineNumberView will
     * listen for certain kinds of events from the editorPane component and update itself accordingly.
     */
    if (editorPane == null) {
      throw new IllegalArgumentException("Text component required! Cannot be null!");
    }
    this.editorPane = (EditorPane) editorPane;
    updateCachedMetrics();

    UpdateHandler handler = new UpdateHandler();
    editorPane.getDocument().addDocumentListener(handler);
    editorPane.addPropertyChangeListener(handler);
    editorPane.addComponentListener(handler);

    setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
    setForeground(FG_COLOR);
    setBackground(BG_COLOR);
    init();
  }

  private void init() {
    addMouseListener(this);
    setToolTipText("RightClick for options - left to jump to - double to select");
    popMenuLineNumber = new SikuliIDEPopUpMenu("POP_LINE", this);
    if (!popMenuLineNumber.isValidMenu()) {
      popMenuLineNumber = null;
    }
    popMenuLineNumber = null; //TODO popMenu
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(componentWidth, editorPane.getHeight());
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    updateCachedMetrics();
  }

  private void updateCachedMetrics() {
    // Cache some values that are used a lot in painting or size calculations.
    Font textFont = editorPane.getFont();
    FontMetrics fm = getFontMetrics(textFont);
    textFontHeight = fm.getHeight();
    textFontAscent = fm.getAscent();
    textTopInset = editorPane.getInsets().top;

    Font viewFont = getFont();
    boolean changed = false;
    if (viewFont == null) {
      viewFont = UIManager.getFont("Label.font");
      viewFont = viewFont.deriveFont(Font.PLAIN);
      changed = true;
    }
    if (viewFont.getSize() > textFont.getSize()) {
      viewFont = viewFont.deriveFont(textFont.getSize2D());
      changed = true;
    }

    viewFontMetrics = getFontMetrics(viewFont);
    maxNumberWidth = viewFontMetrics.stringWidth(String.valueOf(WIDTH_TEMPLATE));
    componentWidth = 2 * MARGIN + maxNumberWidth;

    if (changed) {
      super.setFont(viewFont);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    updateSizes();
    Rectangle clip = g.getClipBounds();
    g.setColor(getBackground());
    g.fillRect(clip.x, clip.y, clip.width, clip.height);
    if (sizes == null) {
      return;
    }
    // draw line numbers
    g.setColor(getForeground());
    int base = clip.y - textTopInset;
    int first = sizes.getIndex(base);
    int last = sizes.getIndex(base + clip.height);
    String lnum;
    lnum = "";
    for (int i = first; i < last; i++) {
      lnum = String.valueOf(i + 1);
      int x = MARGIN + maxNumberWidth - viewFontMetrics.stringWidth(lnum);
      int y = (sizes.getPosition(i) + sizes.getPosition(i + 1)) / 2 + textFontAscent / 2 + textTopInset / 2;
      if (errLines.contains(i + 1)) {
        final int h = 12;
        g.drawImage(ERROR_ICON.getImage(), 0, y - h + 1, h, h, null);
        g.setColor(Color.RED);
      } else {
        g.setColor(getForeground());
      }
      g.drawString(lnum, x, y);
    }
  }

  private void updateSizes() {
    // Update the line heights as needed.
    if (startLine < 0) {
      return;
    }
    if (structureChanged) {
      int count = getAdjustedLineCount();
      sizes = new SizeSequence(count);
      for (int i = 0; i < count; i++) {
        sizes.setSize(i, getLineHeight(i));
      }
      structureChanged = false;
    } else {
      if (sizes != null) {
        sizes.setSize(startLine, getLineHeight(startLine));
      }
    }
    startLine = -1;
  }

  private int getAdjustedLineCount() {
    // There is an implicit break being modeled at the end of the
    // document to deal with boundary conditions at the end.  This
    // is not desired in the line count, so we detect it and remove
    // its effect if throwing off the count.
    Element root = editorPane.getDocument().getDefaultRootElement();
    int n = root.getElementCount();
    Element lastLine = root.getElement(n - 1);
    if ((lastLine.getEndOffset() - lastLine.getStartOffset()) >= 1) {
      return n;
    }
    return n - 1;
  }

  private int getLineHeight(int index) {
    // Get the height of a line from the JTextComponent.
    Element e;
    int lastPos = sizes.getPosition(index) + textTopInset;
    Element l = editorPane.getDocument().getDefaultRootElement().getElement(index);
    Rectangle r = null;
    Rectangle r1;
    int h = textFontHeight;
    int max_h = 0;
    try {
      if (l.getElementCount() < 2) {
        r = editorPane.modelToView(l.getEndOffset() - 1);
      } else {
        for (int i = 0; i < l.getElementCount(); i++) {
          e = l.getElement(i);
          if ("component".equals(e.getName())) {
            r1 = editorPane.modelToView(e.getStartOffset());
            if (max_h < r1.height) {
              max_h = r1.height;
              r = r1;
            }
          }
        }
      }
      if (r == null) {
        r = editorPane.modelToView(l.getEndOffset() - 1);
      }
      h = (r.y - lastPos) + r.height;
    } catch (Exception ex) {
    }
    return h;
  }

  public void addErrorMark(int line) {
    errLines.add(line);
  }

  public void resetErrorMark() {
    errLines.clear();
  }

  //<editor-fold defaultstate="collapsed" desc="mouse actions">
  @Override
  public void mouseEntered(MouseEvent me) {
    setBackground(selBG_COLOR);
  }

  @Override
  public void mouseExited(MouseEvent me) {
    setBackground(BG_COLOR);
  }

  @Override
  public void mouseClicked(MouseEvent me) {
    if (wasPopup) {
      wasPopup = false;
      currentStart = currentEnd = -1;
    }
    setCurrentLine(sizes.getIndex(me.getY()) + 1);
    editorPane.jumpTo(getCurrentLine());
    if (me.getClickCount() == 2) {
      Element lineAtCaret = editorPane.getLineAtCaret(-1);
      currentStart = lineAtCaret.getStartOffset();
      currentEnd = lineAtCaret.getEndOffset();
      editorPane.select(currentStart, currentEnd);
      return;
    }
    if (shouldPopup) {
      shouldPopup = false;
      checkPopup(me);
      if (currentStart > -1) {
        editorPane.select(currentStart, currentEnd);
        currentStart = currentEnd = -1;
      }
    }
  }

  private boolean shouldPopup = false;

  private void checkPopup(MouseEvent me) {
    if (popMenuLineNumber != null) {
      wasPopup = true;
      popMenuLineNumber.show(this, (int) getSize().getWidth() - 3, me.getY() + 8);
    }
  }

  public int getCurrentLine() {
    return currentLine;
  }

  public void setCurrentLine(int currentLine) {
    this.currentLine = currentLine;
  }

  private int currentLine = -1;
  private int currentStart = -1;
  private int currentEnd = -1;

  @Override
  public void mousePressed(MouseEvent me) {
    int start = editorPane.getSelectionStart();
    int end = editorPane.getSelectionEnd();
    if (start != end) {
      currentStart = start;
      currentEnd = end;
    }
    if (me.isPopupTrigger()) {
      shouldPopup = true;
    }
  }

  @Override
  public void mouseReleased(MouseEvent me) {
    if (me.isPopupTrigger()) {
      shouldPopup = true;
    }
  }

  //</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="UpdateHandler">
  private void viewChanged(int startLine, boolean structureChanged) {
    // Schedule a repaint because one or more line heights may have changed.
    // triggered by UpdateHandler
    this.startLine = startLine;
    this.structureChanged |= structureChanged;
    revalidate();
    repaint();
  }

  class UpdateHandler extends ComponentAdapter implements PropertyChangeListener, DocumentListener {

    @Override
    public void componentResized(ComponentEvent evt) {
      // all lines invalidated
      viewChanged(0, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      // a doc prop changed - invalidate all lines
      Object oldValue = evt.getOldValue();
      Object newValue = evt.getNewValue();
      String propertyName = evt.getPropertyName();
      if ("document".equals(propertyName)) {
        if (oldValue != null && oldValue instanceof Document) {
          ((Document) oldValue).removeDocumentListener(this);
        }
        if (newValue != null && newValue instanceof Document) {
          ((Document) newValue).addDocumentListener(this);
        }
      }
      updateCachedMetrics();
      viewChanged(0, true);
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
      update(evt, "insert"); // Text was inserted into the document.
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
      update(evt, "remove"); //Text was removed from the document.
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
      //      update(evt); //done by insert / remove already
    }

    private void update(DocumentEvent evt, String msg) {
      // invalidate one or all lines
      Element map = editorPane.getDocument().getDefaultRootElement();
      int line = map.getElementIndex(evt.getOffset());
      DocumentEvent.ElementChange ec = evt.getChange(map);
      Debug.log(6, "LineNumbers: " + msg + " update - struct changed: " + (ec != null));
      viewChanged(line, ec != null);
    }
  }
  //</editor-fold>
}
