/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JTable;

/**
 * Table-specific implementation of RolloverProducer.
 *
 * @author Jeanette Winzenburg
 */
public class TableRolloverProducer extends RolloverProducer {

    @Override
    protected void updateRolloverPoint(JComponent component, Point mousePoint) {
        JTable table = (JTable) component;
        int col = table.columnAtPoint(mousePoint);
        int row = table.rowAtPoint(mousePoint);
        if ((col < 0) || (row < 0)) {
            row = -1;
            col = -1;
        }
        rollover.x = col;
        rollover.y = row;
    }

}
