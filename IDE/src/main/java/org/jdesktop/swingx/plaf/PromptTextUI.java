/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.Component.BaselineResizeBehavior;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Method;

import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.EditorKit;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;

/**
 * <p>
 * Abstract {@link TextUI} class that delegates most work to another
 * {@link TextUI} and additionally renders a prompt text as specified in the
 * {@link JTextComponent}s client properties by {@link PromptSupport}.
 * <p>
 * Subclasses of this class must provide a prompt component used for rendering
 * the prompt text.
 * </p>
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 */
public abstract class PromptTextUI extends TextUI {
    protected class PainterHighlighter implements Highlighter {
        private final Painter painter;

        private JTextComponent c;

        public PainterHighlighter(Painter painter) {
            this.painter = painter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object addHighlight(int p0, int p1, HighlightPainter p)
                throws BadLocationException {
            return new Object();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void changeHighlight(Object tag, int p0, int p1)
                throws BadLocationException {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void deinstall(JTextComponent c) {
            c = null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Highlight[] getHighlights() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void install(JTextComponent c) {
            this.c = c;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            try {
                painter.paint(g2d, c, c.getWidth(), c.getHeight());
            } finally {
                g2d.dispose();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeAllHighlights() {
            // TODO Auto-generated method stub

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeHighlight(Object tag) {
            // TODO Auto-generated method stub

        }
    }

    static final FocusHandler focusHandler = new FocusHandler();

    /**
     * Delegate the hard work to this object.
     */
    protected final TextUI delegate;

    /**
     * This component ist painted when rendering the prompt text.
     */
    protected JTextComponent promptComponent;

    /**
     * Creates a new {@link PromptTextUI} which delegates most work to another
     * {@link TextUI}.
     *
     * @param delegate
     */
    public PromptTextUI(TextUI delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a component which should be used to render the prompt text.
     *
     * @return
     */
    protected abstract JTextComponent createPromptComponent();

    /**
     * Calls TextUI#installUI(JComponent) on the delegate and installs a focus
     * listener on <code>c</code> which repaints the component when it gains or
     * loses the focus.
     */
    @Override
    public void installUI(JComponent c) {
        delegate.installUI(c);

        JTextComponent txt = (JTextComponent) c;

        // repaint to correctly highlight text if FocusBehavior is
        // HIGHLIGHT_LABEL in Metal and Windows LnF
        txt.addFocusListener(focusHandler);
    }

    /**
     * Delegates, then uninstalls the focus listener.
     */
    @Override
    public void uninstallUI(JComponent c) {
        delegate.uninstallUI(c);
        c.removeFocusListener(focusHandler);
        promptComponent = null;
    }

    /**
     * Creates a label component, if none has already been created. Sets the
     * prompt components properties to reflect the given {@link JTextComponent}s
     * properties and returns it.
     *
     * @param txt
     * @return the adjusted prompt component
     */
    public JTextComponent getPromptComponent(JTextComponent txt) {
        if (promptComponent == null) {
            promptComponent = createPromptComponent();
        }
        if (txt.isFocusOwner()
                && PromptSupport.getFocusBehavior(txt) == FocusBehavior.HIDE_PROMPT) {
            promptComponent.setText(null);
        } else {
            promptComponent.setText(PromptSupport.getPrompt(txt));
        }

        promptComponent.getHighlighter().removeAllHighlights();
        if (txt.isFocusOwner()
                && PromptSupport.getFocusBehavior(txt) == FocusBehavior.HIGHLIGHT_PROMPT) {
            promptComponent.setForeground(txt.getSelectedTextColor());
            try {
                promptComponent.getHighlighter().addHighlight(0,
                        promptComponent.getText().length(),
                        new DefaultHighlightPainter(txt.getSelectionColor()));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            promptComponent.setForeground(PromptSupport.getForeground(txt));
        }

        if (PromptSupport.getFontStyle(txt) == null) {
            promptComponent.setFont(txt.getFont());
        } else {
            promptComponent.setFont(txt.getFont().deriveFont(
                    PromptSupport.getFontStyle(txt)));
        }

        promptComponent.setBackground(PromptSupport.getBackground(txt));
        promptComponent.setHighlighter(new PainterHighlighter(PromptSupport
                .getBackgroundPainter(txt)));
        promptComponent.setEnabled(txt.isEnabled());
        promptComponent.setOpaque(txt.isOpaque());
        promptComponent.setBounds(txt.getBounds());
        Border b = txt.getBorder();

        if (b == null) {
            promptComponent.setBorder(txt.getBorder());
        } else {
            Insets insets = b.getBorderInsets(txt);
            promptComponent.setBorder(
                    createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
        }

        promptComponent.setSelectedTextColor(txt.getSelectedTextColor());
        promptComponent.setSelectionColor(txt.getSelectionColor());
        promptComponent.setEditable(txt.isEditable());
        promptComponent.setMargin(txt.getMargin());

        return promptComponent;
    }

    /**
     * When {@link #shouldPaintPrompt(JTextComponent)} returns true, the prompt
     * component is retrieved by calling
     * {@link #getPromptComponent(JTextComponent)} and it's preferred size is
     * returned. Otherwise super{@link #getPreferredSize(JComponent)} is called.
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        JTextComponent txt = (JTextComponent) c;
        if (shouldPaintPrompt(txt)) {
            return getPromptComponent(txt).getPreferredSize();
        }
        return delegate.getPreferredSize(c);
    }

    /**
     * Delegates painting when {@link #shouldPaintPrompt(JTextComponent)}
     * returns false. Otherwise the prompt component is retrieved by calling
     * {@link #getPromptComponent(JTextComponent)} and painted. Then the caret
     * of the given text component is painted.
     */
    @Override
    public void paint(Graphics g, final JComponent c) {
        JTextComponent txt = (JTextComponent) c;

        if (shouldPaintPrompt(txt)) {
            paintPromptComponent(g, txt);
        } else {
            delegate.paint(g, c);
        }
    }

    protected void paintPromptComponent(Graphics g, JTextComponent txt) {
        JTextComponent lbl = getPromptComponent(txt);
        SwingUtilities.paintComponent(g, lbl, txt, 0, 0, txt.getWidth(), txt.getHeight());

        if (txt.getCaret() != null) {
            txt.getCaret().paint(g);
        }
    }

    /**
     * Returns if the prompt or the text field should be painted, depending on
     * the state of <code>txt</code>.
     *
     * @param txt
     * @return true when <code>txt</code> contains not text, otherwise false
     */
    public boolean shouldPaintPrompt(JTextComponent txt) {
        return txt.getText() == null || txt.getText().length() == 0;
    }

    /**
     * Calls super.{@link #update(Graphics, JComponent)}, which in turn calls
     * the paint method of this object.
     */
    @Override
    public void update(Graphics g, JComponent c) {
        if (shouldPaintPrompt((JTextComponent) c)) {
            super.update(g, c);
        } else {
            delegate.update(g, c);
        }
    }

    /**
     * Delegate when {@link #shouldPaintPrompt(JTextComponent)} returns false.
     * Otherwise get the prompt component's UI and delegate to it. This ensures
     * that the {@link Caret} is painted on the correct position (this is
     * important when the text is centered, so that the caret will not be
     * painted inside the label text)
     */
    @Override
    public Rectangle modelToView(JTextComponent t, int pos, Bias bias)
            throws BadLocationException {
        if (shouldPaintPrompt(t)) {
            return getPromptComponent(t).getUI().modelToView(t, pos, bias);
        } else {
            return delegate.modelToView(t, pos, bias);
        }
    }

    /**
     * Calls {@link #modelToView(JTextComponent, int, Bias)} with
     * {@link Bias#Forward}.
     */
    @Override
    public Rectangle modelToView(JTextComponent t, int pos)
            throws BadLocationException {
        return modelToView(t, pos, Position.Bias.Forward);
    }

    // ********************* Delegate methods *************************///
    // ****************************************************************///

    @Override
    public boolean contains(JComponent c, int x, int y) {
        return delegate.contains(c, x, y);
    }

    @Override
    public void damageRange(JTextComponent t, int p0, int p1, Bias firstBias,
            Bias secondBias) {
        delegate.damageRange(t, p0, p1, firstBias, secondBias);
    }

    @Override
    public void damageRange(JTextComponent t, int p0, int p1) {
        delegate.damageRange(t, p0, p1);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public Accessible getAccessibleChild(JComponent c, int i) {
        return delegate.getAccessibleChild(c, i);
    }

    @Override
    public int getAccessibleChildrenCount(JComponent c) {
        return delegate.getAccessibleChildrenCount(c);
    }

    @Override
    public EditorKit getEditorKit(JTextComponent t) {
        return delegate.getEditorKit(t);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return delegate.getMaximumSize(c);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return delegate.getMinimumSize(c);
    }

    @Override
    public int getNextVisualPositionFrom(JTextComponent t, int pos, Bias b,
            int direction, Bias[] biasRet) throws BadLocationException {
        return delegate
                .getNextVisualPositionFrom(t, pos, b, direction, biasRet);
    }

    @Override
    public View getRootView(JTextComponent t) {
        return delegate.getRootView(t);
    }

    @Override
    public String getToolTipText(JTextComponent t, Point pt) {
        return delegate.getToolTipText(t, pt);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getClass().getName(), delegate
                .toString());
    }

    @Override
    public int viewToModel(JTextComponent t, Point pt, Bias[] biasReturn) {
        return delegate.viewToModel(t, pt, biasReturn);
    }

    @Override
    public int viewToModel(JTextComponent t, Point pt) {
        return delegate.viewToModel(t, pt);
    }

    /**
     * Tries to call {@link ComponentUI#getBaseline(int, int)} on the delegate
     * via Reflection. Workaround to maintain compatibility with Java 5. Ideally
     * we should also override {@link #getBaselineResizeBehavior(JComponent)},
     * but that's impossible since the {@link BaselineResizeBehavior} class,
     * which does not exist in Java 5, is involved.
     *
     * @return the baseline, or -2 if <code>getBaseline</code> could not be
     *         invoked on the delegate.
     */
    @Override
    public int getBaseline(JComponent c, int width, int height) {
        try {
            Method m = delegate.getClass().getMethod("getBaseline",
                    JComponent.class, int.class, int.class);
            Object o = m.invoke(delegate, new Object[] { c, width, height });
            return (Integer) o;
        } catch (Exception ex) {
            // ignore
            return -2;
        }
    }

    /**
     * Repaint the {@link TextComponent} when it loses or gains the focus.
     */
    private static final class FocusHandler extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            e.getComponent().repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            e.getComponent().repaint();
        }
    }
}
