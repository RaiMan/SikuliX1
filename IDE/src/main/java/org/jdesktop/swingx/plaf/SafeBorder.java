/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * Created on 04.11.2010
 *
 */
package org.jdesktop.swingx.plaf;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Component.BaselineResizeBehavior;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

/**
 * Wrapper around a delegate with the same behaviour as the delegate except that
 * it catches null insets (hack around Issue 1297-swingx which is core bug
 * 6739738)
 */
public class SafeBorder extends AbstractBorder implements UIResource {

    private AbstractBorder delegate;

    public SafeBorder(AbstractBorder delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBaseline(Component c, int width, int height) {
        return delegate.getBaseline(c, width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior(Component c) {
        return delegate.getBaselineResizeBehavior(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets result = delegate.getBorderInsets(c, safeInsets(insets));
        return safeInsets(result);
    }

    /**
     * @param insets
     *            the insets to query
     * @return the insets supplied or an empty insets if the value is {@code null}
     */
    private Insets safeInsets(Insets insets) {
        return insets != null ? insets : new Insets(0, 0, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insets getBorderInsets(Component c) {
        Insets result = delegate.getBorderInsets(c);
        return safeInsets(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle getInteriorRectangle(Component c, int x, int y, int width,
            int height) {
        return delegate.getInteriorRectangle(c, x, y, width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBorderOpaque() {
        return delegate.isBorderOpaque();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
            int height) {
        delegate.paintBorder(c, g, x, y, width, height);
    }

}
