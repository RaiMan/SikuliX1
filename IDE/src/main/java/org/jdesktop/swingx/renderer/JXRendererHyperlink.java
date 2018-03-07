/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.painter.Painter;

/**
 * A <code>JXHyperlink</code> optimized for usage in renderers and
 * with a minimal background painter support. <p>
 *
 * <i>Note</i>: the painter support will be switched to painter_work as
 * soon it enters main.
 *
 * @author Jeanette Winzenburg
 */
public class JXRendererHyperlink extends JXHyperlink implements PainterAware {
    protected Painter painter;

    /**
     * {@inheritDoc}
     */
    public void setPainter(Painter painter) {
        Painter old = getPainter();
        this.painter = painter;
        if (painter != null) {
            // ui maps to !opaque
            // Note: this is incomplete - need to keep track of the
            // "real" contentfilled property
            setContentAreaFilled(false);
        }
        firePropertyChange("painter", old, getPainter());
    }

    /**
     * {@inheritDoc}
     */
    public Painter getPainter() {
        return painter;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (painter != null) {
            // we have a custom (background) painter
            // try to inject if possible
            // there's no guarantee - some LFs have their own background
            // handling  elsewhere
            paintComponentWithPainter((Graphics2D) g);
        } else {
            // no painter - delegate to super
            super.paintComponent(g);
        }
    }

    /**
     *
     * Hack around AbstractPainter.paint bug which disposes the Graphics.
     * So here we give it a scratch to paint on. <p>
     * TODO - remove again, the issue is fixed?
     *
     * @param g the graphics to paint on
     */
    private void paintPainter(Graphics g) {
        // fail fast: we assume that g must not be null
        // which throws an NPE here instead deeper down the bowels
        // this differs from corresponding core implementation!
        Graphics2D scratch = (Graphics2D) g.create();
        try {
            painter.paint(scratch, this, getWidth(), getHeight());
        }
        finally {
            scratch.dispose();
        }
    }

    /**
     * PRE: painter != null
     * @param g
     */
    protected void paintComponentWithPainter(Graphics2D g) {
        // 1. be sure to fill the background
        // 2. paint the painter
        // by-pass ui.update and hook into ui.paint directly
        if (ui != null) {
            // fail fast: we assume that g must not be null
            // which throws an NPE here instead deeper down the bowels
            // this differs from corresponding core implementation!
            Graphics scratchGraphics = g.create();
            try {
                scratchGraphics.setColor(getBackground());
                scratchGraphics.fillRect(0, 0, getWidth(), getHeight());
                paintPainter(g);
                ui.paint(scratchGraphics, this);
            } finally {
                scratchGraphics.dispose();
            }
        }

    }

    @Override
    public void updateUI() {
        super.updateUI();
        setBorderPainted(true);
        setOpaque(true);
    }
    /**
     * {@inheritDoc} <p>
     *
     * Overridden to not automatically de/register itself from/to the ToolTipManager.
     * As rendering component it is not considered to be active in any way, so the
     * manager must not listen.
     */
    @Override
    public void setToolTipText(String text) {
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void invalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if ("text".equals(propertyName)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }

}
