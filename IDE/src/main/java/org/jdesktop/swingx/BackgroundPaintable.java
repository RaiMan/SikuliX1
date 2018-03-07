/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import org.jdesktop.swingx.painter.Painter;

/**
 * An interface to define the common methods that are required for defining a background painter.
 *
 * @author kschaefer
 */
@SuppressWarnings("rawtypes")
interface BackgroundPaintable {
    /**
     * Returns the current background painter.
     *
     * @return the current painter
     * @see #setBackgroundPainter(Painter)
     * @see #isPaintBorderInsets()
     */
    Painter getBackgroundPainter();

    /**
     * Sets the new background painter.
     *
     * @param painter the new background painter; may be {@code null}
     */
    void setBackgroundPainter(Painter painter);

    /**
     * Determines whether this component paints its background paint underneath the border.
     *
     * @return {@code true} to paint under the border; {@code false} otherwise
     */
    boolean isPaintBorderInsets();

    /**
     *
     * @param paintBorderInsets
     */
    void setPaintBorderInsets(boolean paintBorderInsets);
}
