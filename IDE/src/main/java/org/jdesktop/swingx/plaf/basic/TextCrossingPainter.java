/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.painter.AbstractPainter;

/**
 * Painter used to cross-out unselectable dates.
 *
 * PENDING JW: subclass (or maybe even use?) one of the painter subclasses.
 *
 * @author Jeanette Winzenburg
 */
class TextCrossingPainter<T extends JComponent> extends AbstractPainter<T> {
    Rectangle paintIconR = new Rectangle();
    Rectangle paintViewR = new Rectangle();
    Rectangle paintTextR = new Rectangle();
    Insets insetss = new Insets(0, 0, 0, 0);
    Color crossColor;
    /**
     * {@inheritDoc} <p>
     *
     *  Paints a diagonal cross over the text if the comp is of type JLabel,
     *  does nothing otherwise.
     */
    @Override
    protected void doPaint(Graphics2D g, JComponent comp, int width,
            int height) {
        if (!(comp instanceof JLabel)) return;
        JLabel label = (JLabel) comp;
        Insets insets = label.getInsets(insetss);
        paintViewR.x = insets.left;
        paintViewR.y = insets.top;
        paintViewR.width = width - (insets.left + insets.right);
        paintViewR.height = height - (insets.top + insets.bottom);
        paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
        paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
        SwingUtilities.layoutCompoundLabel(label,
                label.getFontMetrics(label.getFont()), label.getText(), null,
                label.getVerticalAlignment(), label.getHorizontalAlignment(),
                label.getVerticalTextPosition(), label.getHorizontalTextPosition(),
                paintViewR, paintIconR, paintTextR, label.getIconTextGap());
        doPaint(g, paintTextR);
    }

    private void doPaint(Graphics2D g, Rectangle r) {
        Color old = g.getColor();
        g.setColor(getForeground());
        g.drawLine(r.x, r.y, r.x + r.width, r.y + r.height);
        g.drawLine(r.x + 1, r.y, r.x + r.width + 1, r.y + r.height);
        g.drawLine(r.x + r.width, r.y, r.x, r.y + r.height);
        g.drawLine(r.x + r.width - 1, r.y, r.x - 1, r.y + r.height);
        g.setColor(old);

    }

    /**
     *
     * @param crossColor the color to paint the cross with
     */
    public void setForeground(Color crossColor) {
        Color old = getForeground();
        this.crossColor = crossColor;
        firePropertyChange("foreground", old, getForeground());
    }

    /**
     * Returns the color to use for painting the cross.
     *
     * @return the color used for painting.
     */
    public Color getForeground() {
        return crossColor;
    }
}
