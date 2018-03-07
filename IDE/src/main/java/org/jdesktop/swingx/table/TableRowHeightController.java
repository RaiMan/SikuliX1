/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.table;

import static org.jdesktop.swingx.table.TableUtilities.isDataChanged;
import static org.jdesktop.swingx.table.TableUtilities.isInsert;
import static org.jdesktop.swingx.table.TableUtilities.isStructureChanged;
import static org.jdesktop.swingx.table.TableUtilities.isUpdate;
import static org.jdesktop.swingx.table.TableUtilities.setPreferredRowHeight;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A controller to adjust JTable rowHeight based on sizing requirements of its renderers.
 *
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowHeightController {

    private JTable table;
    private TableModelListener tableModelListener;
    private PropertyChangeListener tablePropertyListener;

    /**
     * Instantiates an unbound TableRowHeightController.
     */
    public TableRowHeightController() {
        this(null);
    }

    /**
     * Instantiates a TableRowHeightController and installs itself to the given table.
     * The row heights of all visible rows are automatically adjusted on model changes.
     *
     * @param table the table to control.
     */
    public TableRowHeightController(JTable table) {
        install(table);
    }

    /**
     * Installs this controller on the given table. Releases control from previously
     * installed table, if any.
     * @param table the table to install upon.
     */
    public void install(JTable table) {
        release();
        if (table != null) {
            this.table = table;
            installListeners();
            updatePreferredRowHeights();
        }
    }

    /**
     * Release this controller from its table. Does nothing if no table installed.
     *
     */
    public void release() {
        if (table == null)
            return;
        uninstallListeners();
        table = null;
    }

    /**
     * Sets the row heights of the rows in the range of first- to lastRow, inclusive.
     * The coordinates are model indices.
     *
     * @param firstRow the first row in model coordinates
     * @param lastRow the last row in model coordinates
     */
    protected void updatePreferredRowHeights(int firstRow, int lastRow) {
        for (int row = firstRow; row <= lastRow; row++) {
            int viewRow = table.convertRowIndexToView(row);
            if (viewRow >= 0) {
//                int oldHeight = table.getRowHeight(viewRow);
//                LOG.info("in viewRow/old/new: " + viewRow + " / " + oldHeight + " / " + table.getRowHeight(viewRow));
                setPreferredRowHeight(table, viewRow);
            }
        }
    }

    /**
     * Sets the row heights of all rows.
     */
    protected void updatePreferredRowHeights() {
        if (table.getRowCount() == 0) return;
        updatePreferredRowHeights(0, table.getModel().getRowCount() - 1);
    }

    /**
     * @param oldValue
     */
    protected void updateModel(TableModel oldValue) {
        if (oldValue != null) {
            oldValue.removeTableModelListener(getTableModelListener());
        }
        table.getModel().addTableModelListener(getTableModelListener());
        updatePreferredRowHeights();
    }

    /**
     * @return
     */
    protected PropertyChangeListener createTablePropertyListener() {
        PropertyChangeListener l = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                invokedPropertyChanged(evt);
            }

            /**
             * @param evt
             */
            private void invokedPropertyChanged(final PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if ("model".equals(evt.getPropertyName())) {
                            updateModel((TableModel) evt.getOldValue());
                        }

                    }
                });
            }
        };
        return l;
    }

    protected TableModelListener createTableModelListener() {
        TableModelListener l = new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        invokedTableChanged(e);
                    }
                });
            }

            private void invokedTableChanged(TableModelEvent e) {
                if (isStructureChanged(e) || isDataChanged(e)) {
                    updatePreferredRowHeights();
                } else  if (isUpdate(e) || isInsert(e)) {
                    updatePreferredRowHeights(e.getFirstRow(), e.getLastRow());
                }
                // do nothing on delete
            }
        };
        return l;
    }
    /**
     *
     */
    private void uninstallListeners() {
        table.removePropertyChangeListener(getPropertyChangeListener());
        table.getModel().removeTableModelListener(getTableModelListener());
        // whatever else turns out to be needed
    }

    private void installListeners() {
        table.addPropertyChangeListener(getPropertyChangeListener());
        table.getModel().addTableModelListener(getTableModelListener());
        // whatever else turns out to be needed
    }

    /**
     * @return
     */
    protected TableModelListener getTableModelListener() {
        if (tableModelListener == null) {
            tableModelListener = createTableModelListener();
        }
        return tableModelListener;
    }

    /**
     * @return
     */
    protected PropertyChangeListener getPropertyChangeListener() {
        if (tablePropertyListener == null) {
            tablePropertyListener = createTablePropertyListener();
        }
        return tablePropertyListener;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
        .getLogger(TableRowHeightController.class.getName());
}
