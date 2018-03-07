/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * <p>An implementation of {@link RepaintManager} which adds support for transparency
 * in {@link JXPanel}s. <code>JXPanel</code> (which supports translucency) will
 * replace the current RepaintManager with an instance of RepaintManagerX
 * <em>unless</em> the current RepaintManager is tagged by the {@link TranslucentRepaintManager}
 * annotation.</p>
 *
 * @author zixle
 * @author rbair
 * @author Karl Schaefer
 */
@TranslucentRepaintManager
public class RepaintManagerX extends ForwardingRepaintManager {
    /**
     * Creates a new manager that forwards all calls to the delegate.
     *
     * @param delegate
     *            the manager backing this {@code RepaintManagerX}
     * @throws NullPointerException
     *             if {@code delegate} is {@code null}
     */
    public RepaintManagerX(RepaintManager delegate) {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        AlphaPaintable alphaPaintable = (AlphaPaintable) SwingUtilities.getAncestorOfClass(AlphaPaintable.class, c);

        if (alphaPaintable != null && alphaPaintable.getAlpha() < 1f) {
            Point p = SwingUtilities.convertPoint(c, x, y, (JComponent) alphaPaintable);
            addDirtyRegion((JComponent) alphaPaintable, p.x, p.y, w, h);
        } else {
            super.addDirtyRegion(c, x, y, w, h);
        }
    }
}
