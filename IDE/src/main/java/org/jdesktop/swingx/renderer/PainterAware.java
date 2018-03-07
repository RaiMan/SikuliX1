/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import org.jdesktop.swingx.painter.Painter;

/**
 * Temporary hook to allow painters in rendering. <p>
 *
 * NOTE: this will be removed as soon as the painter_work enters
 * main.
 *
 * @author Jeanette Winzenburg
 */
public interface PainterAware {
    void setPainter(Painter<?> painter);
    Painter<?> getPainter();
}
