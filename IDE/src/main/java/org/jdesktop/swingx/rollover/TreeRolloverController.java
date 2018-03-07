/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
     * listens to rollover properties.
     * Repaints effected component regions.
     * Updates link cursor.
     *
     * @author Jeanette Winzenburg
     */
    public class TreeRolloverController<T extends JTree>  extends RolloverController<T> {

        private Cursor oldCursor;

//    -------------------------------------JTree rollover

        @Override
        protected void rollover(Point oldLocation, Point newLocation) {
            // JW: conditional repaint not working?
//            component.repaint();
            if (oldLocation != null) {
                Rectangle r = component.getRowBounds(oldLocation.y);
                if (r != null) {
                    r.x = 0;
                    r.width = component.getWidth();
                    component.repaint(r);
                }
            }
            if (newLocation != null) {
                Rectangle r = component.getRowBounds(newLocation.y);
                if (r != null) {
                    r.x = 0;
                    r.width = component.getWidth();
                    component.repaint(r);
                }
            }
            setRolloverCursor(newLocation);
        }

        private void setRolloverCursor(Point location) {
            if (hasRollover(location)) {
                if (oldCursor == null) {
                    oldCursor = component.getCursor();
                    component.setCursor(Cursor
                            .getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            } else {
                if (oldCursor != null) {
                    component.setCursor(oldCursor);
                    oldCursor = null;
                }
            }

        }

        @Override
        protected RolloverRenderer getRolloverRenderer(Point location, boolean prepare) {
            TreeCellRenderer renderer = component.getCellRenderer();
            RolloverRenderer rollover = renderer instanceof RolloverRenderer
                ? (RolloverRenderer) renderer : null;
            if ((rollover != null) && !rollover.isEnabled()) {
                rollover = null;
            }
            if ((rollover != null) && prepare) {
                TreePath path = component.getPathForRow(location.y);
                Object element = path != null ? path.getLastPathComponent() : null;
                renderer.getTreeCellRendererComponent(component, element, false,
                        false, false,
                        location.y, false);
            }
            return rollover;
        }

        @Override
        protected Point getFocusedCell() {
            // TODO Auto-generated method stub
            return null;
        }

    }
