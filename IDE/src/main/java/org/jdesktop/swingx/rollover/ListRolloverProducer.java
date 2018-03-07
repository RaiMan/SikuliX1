/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JList;

/**
 * List-specific implementation of RolloverProducer.
 *
 * @author Jeanette Winzenburg
 */
public class ListRolloverProducer extends RolloverProducer {

    @Override
    protected void updateRolloverPoint(JComponent component, Point mousePoint) {
        JList list = (JList) component;
        int row = list.locationToIndex(mousePoint);
        if (row >= 0) {
            Rectangle cellBounds = list.getCellBounds(row, row);
            if (!cellBounds.contains(mousePoint)) {
                row = -1;
            }
        }
        int col = row < 0 ? -1 : 0;
        rollover.x = col;
        rollover.y = row;
    }

}
