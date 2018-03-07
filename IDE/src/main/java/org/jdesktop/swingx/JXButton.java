/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.graphics.FilterComposite;
import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.PainterPaint;
import org.jdesktop.swingx.util.GraphicsUtilities;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * <p>A {@link org.jdesktop.swingx.painter.Painter} enabled subclass of {@link javax.swing.JButton}.
 * This class supports setting the foreground and background painters of the button separately.</p>
 *
 * <p>For example, if you wanted to blur <em>just the text</em> on the button, and let everything else be
 * handled by the UI delegate for your look and feel, then you could:
 * <pre><code>
 *  JXButton b = new JXButton("Execute");
 *  AbstractPainter fgPainter = (AbstractPainter)b.getForegroundPainter();
 *  StackBlurFilter filter = new StackBlurFilter();
 *  fgPainter.setFilters(filter);
 * </code></pre>
 *
 * <p>If <em>either</em> the foreground painter or the background painter is set,
 * then super.paintComponent() is not called. By setting both the foreground and background
 * painters to null, you get <em>exactly</em> the same painting behavior as JButton.</p>
 *
 * @author rbair
 * @author rah003
 * @author Jan Stola
 * @author Karl George Schaefer
 */
@JavaBean
@SuppressWarnings({ "nls", "serial" })
public class JXButton extends JButton implements BackgroundPaintable {
    private class BackgroundButton extends JButton {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDefaultButton() {
            return JXButton.this.isDefaultButton();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getDisabledIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getDisabledSelectedIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getDisplayedMnemonicIndex() {
            return -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getHorizontalAlignment() {
            return JXButton.this.getHorizontalAlignment();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getHorizontalTextPosition() {
            return JXButton.this.getHorizontalTextPosition();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getIconTextGap() {
            return JXButton.this.getIconTextGap();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Insets getMargin() {
            return JXButton.this.getMargin();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMnemonic() {
            return -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ButtonModel getModel() {
            return JXButton.this.getModel();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getPressedIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getRolloverIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getRolloverSelectedIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getSelectedIcon() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText() {
            return "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getVerticalAlignment() {
            return JXButton.this.getVerticalAlignment();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getVerticalTextPosition() {
            return JXButton.this.getVerticalTextPosition();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isBorderPainted() {
            return JXButton.this.isBorderPainted();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isContentAreaFilled() {
            return JXButton.this.isContentAreaFilled();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFocusPainted() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isRolloverEnabled() {
            return JXButton.this.isRolloverEnabled();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {
            return JXButton.this.isSelected();
        }

    }

    private class ForegroundButton extends JButton {
        @Override
        public Font getFont() {
            return JXButton.this.getFont();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Color getForeground() {
            if (fgPainter == null) {
                return JXButton.this.getForeground();
            }

            return PaintUtils.setAlpha(JXButton.this.getForeground(), 0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDefaultButton() {
            return JXButton.this.isDefaultButton();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getDisabledIcon() {
            return JXButton.this.getDisabledIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getDisabledSelectedIcon() {
            return JXButton.this.getDisabledSelectedIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getDisplayedMnemonicIndex() {
            return JXButton.this.getDisplayedMnemonicIndex();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getHorizontalAlignment() {
            return JXButton.this.getHorizontalAlignment();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getHorizontalTextPosition() {
            return JXButton.this.getHorizontalTextPosition();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return JXButton.this.getIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getIconTextGap() {
            return JXButton.this.getIconTextGap();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Insets getMargin() {
            return JXButton.this.getMargin();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMnemonic() {
            return JXButton.this.getMnemonic();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ButtonModel getModel() {
            return JXButton.this.getModel();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getPressedIcon() {
            return JXButton.this.getPressedIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getRolloverIcon() {
            return JXButton.this.getRolloverIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getRolloverSelectedIcon() {
            return JXButton.this.getRolloverSelectedIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getSelectedIcon() {
            return JXButton.this.getSelectedIcon();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText() {
            return JXButton.this.getText();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getVerticalAlignment() {
            return JXButton.this.getVerticalAlignment();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getVerticalTextPosition() {
            return JXButton.this.getVerticalTextPosition();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isBorderPainted() {
            return JXButton.this.isBorderPainted();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isContentAreaFilled() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasFocus() {
            return JXButton.this.hasFocus();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFocusPainted() {
            return JXButton.this.isFocusPainted();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isRolloverEnabled() {
            return JXButton.this.isRolloverEnabled();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {
            return JXButton.this.isSelected();
        }
    }

    private ForegroundButton fgStamp;
    @SuppressWarnings("rawtypes")
    private Painter fgPainter;
    @SuppressWarnings("rawtypes")
    private PainterPaint fgPaint;
    private BackgroundButton bgStamp;
    @SuppressWarnings("rawtypes")
    private Painter bgPainter;

    private boolean paintBorderInsets = true;

    private Rectangle viewRect = new Rectangle();
    private Rectangle textRect = new Rectangle();
    private Rectangle iconRect = new Rectangle();

    /**
     * Creates a button with no set text or icon.
     */
    public JXButton() {
        init();
    }

    /**
     * Creates a button with text.
     *
     * @param text
     *            the text of the button
     */
    public JXButton(String text) {
        super(text);
        init();
    }

    /**
     * Creates a button where properties are taken from the {@code Action} supplied.
     *
     * @param a
     *            the {@code Action} used to specify the new button
     */
    public JXButton(Action a) {
        super(a);
        init();
    }

    /**
     * Creates a button with an icon.
     *
     * @param icon
     *            the Icon image to display on the button
     */
    public JXButton(Icon icon) {
        super(icon);
        init();
    }

    /**
     * Creates a button with initial text and an icon.
     *
     * @param text
     *            the text of the button
     * @param icon
     *            the Icon image to display on the button
     */
    public JXButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    private void init() {
        fgStamp = new ForegroundButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Painter getBackgroundPainter() {
        return bgPainter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void setBackgroundPainter(Painter p) {
        Painter old = getBackgroundPainter();
        this.bgPainter = p;
        firePropertyChange("backgroundPainter", old, getBackgroundPainter());
        repaint();
    }

    /**
     * @return the foreground painter for this button
     */
    @SuppressWarnings("rawtypes")
    public Painter getForegroundPainter() {
        return fgPainter;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setForegroundPainter(Painter p) {
        Painter old = getForegroundPainter();
        this.fgPainter = p;

        if (fgPainter == null) {
            fgPaint = null;
        } else {
            fgPaint = new PainterPaint(fgPainter, this);

            if (bgStamp == null) {
                bgStamp = new BackgroundButton();
            }
        }

        firePropertyChange("foregroundPainter", old, getForegroundPainter());
        repaint();
    }

    /**
     * Returns true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is
     * true by default. This property affects the width, height,
     * and initial transform passed to the background painter.
     */
    @Override
    public boolean isPaintBorderInsets() {
        return paintBorderInsets;
    }

    /**
     * Sets the paintBorderInsets property.
     * Set to true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is true by default.
     * This property affects the width, height,
     * and initial transform passed to the background painter.
     *
     * This is a bound property.
     */
    @Override
    public void setPaintBorderInsets(boolean paintBorderInsets) {
        boolean old = this.isPaintBorderInsets();
        this.paintBorderInsets = paintBorderInsets;
        firePropertyChange("paintBorderInsets", old, isPaintBorderInsets());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        if (getComponentCount() == 1 && getComponent(0) instanceof CellRendererPane) {
            return BasicGraphicsUtils.getPreferredButtonSize(fgStamp, getIconTextGap());
        }

        return super.getPreferredSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (fgPainter == null && bgPainter == null) {
            super.paintComponent(g);
        } else {
            if (fgPainter == null) {
                Graphics2D g2d = (Graphics2D) g.create();

                try{
                    paintWithoutForegroundPainter(g2d);
                } finally {
                    g2d.dispose();
                }
            } else if (fgPainter instanceof AbstractPainter && ((AbstractPainter<?>) fgPainter).getFilters().length > 0) {
                paintWithForegroundPainterWithFilters(g);
            } else {
                Graphics2D g2d = (Graphics2D) g.create();

                try {
                    paintWithForegroundPainterWithoutFilters(g2d);
                } finally {
                    g2d.dispose();
                }
            }
        }
    }

    private void paintWithoutForegroundPainter(Graphics2D g2d) {
        if (bgPainter == null) {
            SwingUtilities.paintComponent(g2d, bgStamp, this, 0, 0, getWidth(), getHeight());
        } else {
            SwingXUtilities.paintBackground(this, g2d);
        }

        SwingUtilities.paintComponent(g2d, fgStamp, this, 0, 0, getWidth(), getHeight());
    }

    private void paintWithForegroundPainterWithoutFilters(Graphics2D g2d) {
        paintWithoutForegroundPainter(g2d);

        if (getText() != null && !getText().isEmpty()) {
            Insets i = getInsets();
            viewRect.x = i.left;
            viewRect.y = i.top;
            viewRect.width = getWidth() - (i.right + viewRect.x);
            viewRect.height = getHeight() - (i.bottom + viewRect.y);

            textRect.x = textRect.y = textRect.width = textRect.height = 0;
            iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

            // layout the text and icon
            String text = SwingUtilities.layoutCompoundLabel(
                this, g2d.getFontMetrics(), getText(), getIcon(),
                getVerticalAlignment(), getHorizontalAlignment(),
                getVerticalTextPosition(), getHorizontalTextPosition(),
                viewRect, iconRect, textRect,
                getText() == null ? 0 : getIconTextGap());

            if (!isPaintBorderInsets()) {
                g2d.translate(i.left, i.top);
            }

            g2d.setPaint(fgPaint);
            BasicGraphicsUtils.drawStringUnderlineCharAt(g2d, text, getDisplayedMnemonicIndex(),
                    textRect.x, textRect.y + g2d.getFontMetrics().getAscent());
        }
    }

    private void paintWithForegroundPainterWithFilters(Graphics g) {
        BufferedImage im = GraphicsUtilities.createCompatibleTranslucentImage(getWidth(), getHeight());
        Graphics2D g2d = im.createGraphics();

        try {
            Graphics gfx = getComponentGraphics(g2d);
            assert gfx == g2d;

            paintWithForegroundPainterWithoutFilters(g2d);
        } finally {
            g2d.dispose();
        }

        Graphics2D filtered = (Graphics2D) g.create();

        try {
            for (BufferedImageOp filter : ((AbstractPainter<?>) fgPainter).getFilters()) {
                filtered.setComposite(new FilterComposite(filtered.getComposite(), filter));
            }

            filtered.drawImage(im, 0, 0, this);
        } finally {
            filtered.dispose();
        }
    }

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
        super.updateUI();

        if (bgStamp != null) {
            bgStamp.updateUI();
        }

        if (fgStamp != null) {
            fgStamp.updateUI();
        }
    }
}
