/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import java.awt.Dimension;

import org.jdesktop.swingx.painter.BusyPainter;

/**
 *
 * @author rah003
 */
public interface BusyLabelUI {
    /**
     * @return The BusyPainter for the JXBusyLabel. If
     * this method returns null, then no progress indication will be shown by busy label.
     */
    public BusyPainter getBusyPainter(Dimension dim);

    /**
     * Delay between moving from one point to another. The exact timing will be close to the selected value but is not guaranteed to be precise (subject to the timing precision of underlaying jvm).
     * @return Delay in ms.
     */
    public int getDelay();
}
