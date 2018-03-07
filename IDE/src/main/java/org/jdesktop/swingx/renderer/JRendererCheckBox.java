/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.UIManager;

import org.jdesktop.swingx.painter.Painter;

/**
 * A <code>JCheckBox</code> optimized for usage in renderers and
 * with a minimal background painter support. <p>
 *
 * <b>Note</b>: As of revision #4223, there's a complete overhaul (aka: changed the tricksery) to
 * fix Issue swingx-1513 (allow client code to set renderer transparent) while keeping
 * fix Issue swingx-897 (striping/background lost when painter installed)
 * <p>
 *
 * <b>Note</b>: The change of logic _did_ introduce a regression (swingx-1546)
 * which was fixed by forcing the box's  opacity to true (for regression release
 * 1.6.5-1). Further improvements (like f.i. the option to delegate to the ui's
 * update - to allow LAF installed painters - instead of paint) are deferred
 * to a later normal release, more discussions needed.
 * <p>
 *
 * @author Jeanette Winzenburg
 *
 * @see #paintComponent(Graphics)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JRendererCheckBox extends JCheckBox implements PainterAware {
    /** the swingx painter */
    protected Painter painter;
    /** a flag to prevent ui painting from filling the background. */
    private boolean fakeTransparency;

    /**
     * Instantiates a JRendererCheckBox with opacity true.
     */
    public JRendererCheckBox() {
        super();
        // fix # 1546-swingx: striping lost in synth-based lafs
        // forcing opaque to enable painting the background
        setOpaque(true);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Painter getPainter() {
        return painter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPainter(Painter painter) {
        Painter old = getPainter();
        this.painter = painter;
        firePropertyChange("painter", old, getPainter());
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return false if painting flag is true.<p>
     *
     */
    @Override
    public boolean isOpaque() {
        if (fakeTransparency) {
            return false;
        }
        return super.isOpaque();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return false if painting flag is true.<p>
     *
     */
     @Override
    public boolean isContentAreaFilled() {
        if (fakeTransparency) {
            return false;
        }
        return super.isContentAreaFilled();
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
     * Overridden to snatch painting from super if a painter installed or Nimbus
     * detected.<p>
     *
     * The overall logic currently (since 1.6.5) is to simply call super without SwingX
     * painter. Otherwise, that is with SwingX painter:
     * <ol>
     * <li> if opaque
     * <ol>
     * <li> set a flag which fakes transparency, that is both
     *      <code>contentAreaFilled</code> and
     *      <code>opaque</code> return false
     * <li> fill background with the component's background color
     * <li> apply swingx painter
     * <li> hook into <code>ui.paint(...)</code>
     * <li> reset the flag
     * </ol>
     * <li> else
     * <ol> apply swingx painter
     * <ol> call super
     * <li>
     * <ol>
     * </ol>
     *
     * Note that Nimbus is special cased (mainly due to its bug of
     * even row striping instead of odd)
     * and handled as if a SwingX painter were set.
     *
     */
    @Override
    protected void paintComponent(Graphics g) {
        // JW: hack around for #1178-swingx (core issue)
        // grab painting if Nimbus detected
        if ((painter != null) || isNimbus()) {
            // we have a custom (background) painter
            // try to inject if possible
            // there's no guarantee - some LFs have their own background
            // handling  elsewhere
            if (isOpaque()) {
                // replace the paintComponent completely
                fakeTransparency = true;
                paintComponentWithPainter((Graphics2D) g);
                fakeTransparency = false;
            } else {
                // transparent apply the background painter before calling super
                paintPainter(g);
                super.paintComponent(g);
            }
        } else {
            // nothing to worry about - delegate to super
            super.paintComponent(g);
        }
    }

    /**
     * Hack around Nimbus not respecting background colors if UIResource.
     * So by-pass ...
     *
     * @return
     */
    private boolean isNimbus() {
        return UIManager.getLookAndFeel().getName().contains("Nimbus");
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
        if (painter == null) return;
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
     *
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
//                super.paintComponent(g);
            } finally {
                scratchGraphics.dispose();
            }
        }

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
