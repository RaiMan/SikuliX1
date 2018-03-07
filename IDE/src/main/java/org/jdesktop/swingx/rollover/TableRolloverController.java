/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * listens to rollover properties. Repaints effected component regions. Updates
 * link cursor.
 *
 * @author Jeanette Winzenburg
 */
public class TableRolloverController<T extends JTable> extends
        RolloverController<T> {

    private Cursor oldCursor;

    // --------------------------- JTable rollover

    @Override
    protected void rollover(Point oldLocation, Point newLocation) {
        // check which rows are effected and need repaint
        boolean paintOldRow = hasRow(oldLocation);
        boolean paintNewRow = hasRow(newLocation);
        if (paintOldRow && paintNewRow) {
            if (oldLocation.y == newLocation.y) {
                // row unchanged, no need for repaint
                paintOldRow = false;
                paintNewRow = false;
            }
        }
        // check which columns are effected and need repaint
        boolean paintOldColumn = hasColumn(oldLocation);
        boolean paintNewColumn = hasColumn(newLocation);
        if (paintOldColumn && paintNewColumn) {
            if (oldLocation.x == newLocation.x) {
                // column unchanged, no need for repaint
                paintOldColumn = false;
                paintNewColumn = false;
            }
        }

        List<Rectangle> rectangles = getPaintRectangles(null, oldLocation, paintOldRow, paintOldColumn);
        rectangles = getPaintRectangles(rectangles, newLocation, paintNewRow, paintNewColumn);
        if (rectangles != null) {
            for (Rectangle rectangle : rectangles) {
                component.repaint(rectangle);
            }
        }
        setRolloverCursor(newLocation);
    }

    /**
     * @param rectangles List of rectangles to paint, maybe null
     * @param cellLocation the location of the cell, guaranteed to be not null
     * @param paintRow boolean indicating whether the row should be painted
     * @param paintColumn boolean indicating whether the column should be painted
     * @return list of rectangles to paint, maybe null
     */
    private List<Rectangle> getPaintRectangles(List<Rectangle> rectangles, Point cellLocation,
            boolean paintRow, boolean paintColumn) {
        if (!paintRow && !paintColumn) return rectangles;
        if (rectangles == null) {
            rectangles = new ArrayList<Rectangle>();
        }
        Rectangle r = component.getCellRect(cellLocation.y, cellLocation.x,
                false);
        if (paintRow) {
            rectangles.add(new Rectangle(0, r.y, component.getWidth(),
                    r.height));
        }
        if (paintColumn) {
            rectangles.add(new Rectangle(r.x, 0, r.width, component
                    .getHeight()));
        }
        return rectangles;
    }

    /**
     * @param cellLocation the cell location to check, may be null
     * @return a boolean indicating whether the given cellLocation has a column
     *    to paint
     */
    private boolean hasColumn(Point cellLocation) {
        return cellLocation != null && cellLocation.x >= 0;
    }

    /**
     * @param cellLocation the cell location to check, may be null
     * @return a boolean indicating whether the given cellLocation has a row
     *    to paint
     */
    private boolean hasRow(Point cellLocation) {
        return cellLocation != null && cellLocation.y >= 0;
    }

    /**
     * overridden to return false if cell editable.
     */
    @Override
    protected boolean isClickable(Point location) {
        return super.isClickable(location)
                && !component.isCellEditable(location.y, location.x);
    }

    @Override
    protected RolloverRenderer getRolloverRenderer(Point location,
            boolean prepare) {
        TableCellRenderer renderer = component.getCellRenderer(location.y,
                location.x);
        RolloverRenderer rollover = renderer instanceof RolloverRenderer ? (RolloverRenderer) renderer
                : null;
        if ((rollover != null) && !rollover.isEnabled()) {
            rollover = null;
        }
        if ((rollover != null) && prepare) {
            component.prepareRenderer(renderer, location.y, location.x);
        }
        return rollover;
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
    protected Point getFocusedCell() {
        int leadRow = component.getSelectionModel().getLeadSelectionIndex();
        int leadColumn = component.getColumnModel().getSelectionModel()
                .getLeadSelectionIndex();
        return new Point(leadColumn, leadRow);
    }

}
