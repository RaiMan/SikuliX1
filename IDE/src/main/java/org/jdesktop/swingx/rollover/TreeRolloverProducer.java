/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTree;

/**
 * Tree-specific implementation of RolloverProducer.
 * <p>
 * This implementation assumes a "hit" for rollover if the mouse is anywhere in
 * the total width of the tree. Additionally, a pressed to the right (but
 * outside of the label bounds) is re-dispatched as a pressed just inside the
 * label bounds. This is a first go for #166-swingx.
 * <p>
 *
 * PENDING JW: bidi-compliance of pressed?
 *
 * @author Jeanette Winzenburg
 */
public class TreeRolloverProducer extends RolloverProducer {

    @Override
    public void mousePressed(MouseEvent e) {
        JTree tree = (JTree) e.getComponent();
        Point mousePoint = e.getPoint();
        int labelRow = tree.getRowForLocation(mousePoint.x, mousePoint.y);
        // default selection
        if (labelRow >= 0)
            return;
        int row = tree.getClosestRowForLocation(mousePoint.x, mousePoint.y);
        Rectangle bounds = tree.getRowBounds(row);
        if (bounds == null) {
            row = -1;
        } else {
            if ((bounds.y + bounds.height < mousePoint.y)
                    || bounds.x > mousePoint.x) {
                row = -1;
            }
        }
        // no hit
        if (row < 0)
            return;
        tree.dispatchEvent(new MouseEvent(tree, e.getID(), e.getWhen(), e
                .getModifiers(), bounds.x + bounds.width - 2, mousePoint.y, e
                .getClickCount(), e.isPopupTrigger(), e.getButton()));
    }

    @Override
    protected void updateRolloverPoint(JComponent component, Point mousePoint) {
        JTree tree = (JTree) component;
        int row = tree.getClosestRowForLocation(mousePoint.x, mousePoint.y);
        Rectangle bounds = tree.getRowBounds(row);
        if (bounds == null) {
            row = -1;
        } else {
            if ((bounds.y + bounds.height < mousePoint.y)
                    || bounds.x > mousePoint.x) {
                row = -1;
            }
        }
        int col = row < 0 ? -1 : 0;
        rollover.x = col;
        rollover.y = row;
    }

}
