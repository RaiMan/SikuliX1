/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.table;

import java.awt.Component;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

/**
 * Creates and configures <code>TableColumnExt</code>s.
 * <p>
 * TODO JW: explain types of configuration - initial from tableModel, initial
 * from table context, user triggered at runtime.
 * <p>
 *
 * <code>JXTable</code> delegates all <code>TableColumn</code> creation and
 * configuration to a <code>ColumnFactory</code>. Enhanced column
 * configuration should be implemented in a custom factory subclass. The example
 * beautifies the column titles to always start with a capital letter:
 *
 * <pre>
 * <code>
 *    MyColumnFactory extends ColumnFactory {
 *        //@Override
 *        public void configureTableColumn(TableModel model,
 *            TableColumnExt columnExt) {
 *            super.configureTableColumn(model, columnExt);
 *            String title = columnExt.getTitle();
 *            title = title.substring(0,1).toUpperCase() + title.substring(1).toLowerCase();
 *            columnExt.setTitle(title);
 *        }
 *    };
 * </code>
 * </pre>
 *
 * By default a single instance is shared across all tables of an application.
 * This instance can be replaced by a custom implementation, preferably "early"
 * in the application's lifetime.
 *
 * <pre><code>
 * ColumnFactory.setInstance(new MyColumnFactory());
 * </code></pre>
 *
 * Alternatively, any instance of <code>JXTable</code> can be configured
 * individually with its own <code>ColumnFactory</code>.
 *
 * <pre>
 *  <code>
 * JXTable table = new JXTable();
 * table.setColumnFactory(new MyColumnFactory());
 * table.setModel(myTableModel);
 * </code>
 *  </pre>
 *
 * <p>
 *
 * @see org.jdesktop.swingx.JXTable#setColumnFactory(ColumnFactory)
 *
 * @author Jeanette Winzenburg
 * @author M.Hillary (the pack code)
 */
public class ColumnFactory {

    /** the shared instance. */
    private static ColumnFactory columnFactory;
    /** the default margin to use in pack. */
    private int packMargin = 4;

    /**
     * Returns the shared default factory.
     *
     * @return the shared instance of <code>ColumnFactory</code>
     * @see #setInstance(ColumnFactory)
     */
    public static synchronized ColumnFactory getInstance() {
        if (columnFactory == null) {
            columnFactory = new ColumnFactory();
        }
        return columnFactory;
    }

    /**
     * Sets the shared default factory. The shared instance is used
     * by <code>JXTable</code> if none has been set individually.
     *
     * @param factory the default column factory.
     * @see #getInstance()
     * @see org.jdesktop.swingx.JXTable#getColumnFactory()
     */
    public static synchronized void  setInstance(ColumnFactory factory) {
        columnFactory = factory;
    }

    /**
     * Creates and configures a TableColumnExt. <code>JXTable</code> calls
     * this method for each column in the <code>TableModel</code>.
     *
     * @param model the TableModel to read configuration properties from
     * @param modelIndex column index in model coordinates
     * @return a TableColumnExt to use for the modelIndex
     * @throws NPE if model == null
     * @throws IllegalStateException if the modelIndex is invalid
     *   (in coordinate space of the tablemodel)
     *
     * @see #createTableColumn(int)
     * @see #configureTableColumn(TableModel, TableColumnExt)
     * @see org.jdesktop.swingx.JXTable#createDefaultColumnsFromModel()
     */
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int modelIndex) {
        TableColumnExt column = createTableColumn(modelIndex);
        if (column != null) {
            configureTableColumn(model, column);
        }
        return column;
    }

    /**
     * Creates a table column with modelIndex.
     * <p>
     * The factory's column creation is passed through this method, so
     * subclasses can override to return custom column types.
     *
     * @param modelIndex column index in model coordinates
     * @return a TableColumnExt with <code>modelIndex</code>
     *
     * @see #createAndConfigureTableColumn(TableModel, int)
     *
     */
    public TableColumnExt createTableColumn(int modelIndex) {
        return new TableColumnExt(modelIndex);
    }

    /**
     * Configure column properties from TableModel. This implementation
     * sets the column's <code>headerValue</code> property from the
     * model's <code>columnName</code>.
     * <p>
     *
     * The factory's initial column configuration is passed through this method, so
     * subclasses can override to customize.
     * <p>
     *
     * @param model the TableModel to read configuration properties from
     * @param columnExt the TableColumnExt to configure.
     * @throws NullPointerException if model or column == null
     * @throws IllegalStateException if column does not have valid modelIndex
     *   (in coordinate space of the tablemodel)
     *
     * @see #createAndConfigureTableColumn(TableModel, int)
     */
    public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
        if ((columnExt.getModelIndex() < 0)
                || (columnExt.getModelIndex() >= model.getColumnCount()))
            throw new IllegalStateException("column must have valid modelIndex");
        columnExt.setHeaderValue(model.getColumnName(columnExt.getModelIndex()));
    }

    /**
     * Configures column initial widths properties from <code>JXTable</code>.
     * This implementation sets the column's
     * <code>preferredWidth</code> with the strategy:
     * <ol> if the column has a prototype, measure the rendering
     *    component with the prototype as value and use that as
     *    pref width
     * <ol> if the column has no prototype, use the standard magic
     *   pref width (= 75)
     * <ol> try to measure the column's header and use it's preferred
     *   width if it exceeds the former.
     * </ol>
     *
     * TODO JW - rename method to better convey what's happening, maybe
     * initializeColumnWidths like the old method in JXTable. <p>
     *
     * TODO JW - how to handle default settings which are different from
     *   standard 75?
     *
     * @param table the context the column will live in.
     * @param columnExt the Tablecolumn to configure.
     *
     * @see org.jdesktop.swingx.JXTable#getPreferredScrollableViewportSize()
     */
    public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
        /*
         * PENDING JW: really only called once in a table's lifetime?
         * unfortunately: yes - should be called always after structureChanged.
         *
         */
        // magic value: default in TableColumn
        int prefWidth = 75 - table.getColumnMargin();
        int prototypeWidth = calcPrototypeWidth(table, columnExt);
        if (prototypeWidth > 0) {
            prefWidth = prototypeWidth;
        }
        int headerWidth = calcHeaderWidth(table, columnExt);
        prefWidth = Math.max(prefWidth, headerWidth);
        prefWidth += table.getColumnModel().getColumnMargin();
        columnExt.setPreferredWidth(prefWidth);
    }

    /**
     * Calculates and returns the preferred scrollable viewport
     * width of the given table. Subclasses are free to override
     * and implement a custom strategy.<p>
     *
     * This implementation sums the pref widths of the first
     * visibleColumnCount contained visible tableColumns. If
     * the table contains less columns, the standard preferred
     * width per column is added to the result.
     *
     * @param table the table containing the columns
     */
    public int getPreferredScrollableViewportWidth(JXTable table) {
        int w = 0;
        int count;
        if (table.getVisibleColumnCount() < 0) {
            count = table.getColumnCount();
        } else {
            count = Math.min(table.getColumnCount(), table.getVisibleColumnCount());
        }
        for (int i = 0; i < count; i++) {
            // sum up column's pref size, until maximal the
            // visibleColumnCount
            w += table.getColumn(i).getPreferredWidth();
        }
        if (count < table.getVisibleColumnCount()) {
            w += (table.getVisibleColumnCount() - count) * 75;
        }
        return w;

    }
    /**
     * Measures and returns the preferred width of the header. Returns -1 if not
     * applicable.
     *
     * @param table the component the renderer lives in
     * @param columnExt the TableColumn to configure
     * @return the preferred width of the header or -1 if none.
     */
    protected int calcHeaderWidth(JXTable table, TableColumnExt columnExt) {
        int prototypeWidth = -1;
        // now calculate how much room the column header wants
        TableCellRenderer renderer = getHeaderRenderer(table, columnExt);
        if (renderer != null) {
            Component comp = renderer.getTableCellRendererComponent(table,
                    columnExt.getHeaderValue(), false, false, -1, -1);

            prototypeWidth = comp.getPreferredSize().width;
        }
        return prototypeWidth;
    }

    /**
     * Measures and returns the preferred width of the rendering component
     * configured with the prototype value, if any. Returns -1 if not
     * applicable.
     *
     * @param table the component the renderer lives in
     * @param columnExt the TableColumn to configure
     * @return the preferred width of the prototype or -1 if none.
     */
    protected int calcPrototypeWidth(JXTable table, TableColumnExt columnExt) {
        int prototypeWidth = -1;
        Object prototypeValue = columnExt.getPrototypeValue();
        if (prototypeValue != null) {
            // calculate how much room the prototypeValue requires
            TableCellRenderer cellRenderer = getCellRenderer(table, columnExt);
            Component comp = cellRenderer.getTableCellRendererComponent(table,
                    prototypeValue, false, false, 0, -1);
            prototypeWidth = comp.getPreferredSize().width;
        }
        return prototypeWidth;
    }

    /**
     * Returns the cell renderer to use for measuring. Delegates to
     * JXTable for visible columns, duplicates table logic for hidden
     * columns. <p>
     *
     * @param table the table which provides the renderer
     * @param columnExt the TableColumn to configure
     *
     * @return returns a cell renderer for measuring.
     */
    protected TableCellRenderer getCellRenderer(JXTable table, TableColumnExt columnExt) {
        int viewIndex = table.convertColumnIndexToView(columnExt
                .getModelIndex());
        if (viewIndex >= 0) {
            // JW: ok to not guard against rowCount < 0?
            // technically, the index should be a valid coordinate
            return table.getCellRenderer(0, viewIndex);
        }
        // hidden column - need api on JXTable to access renderer for hidden?
        // here we duplicate JXTable api ... maybe by-passing the strategy
        // implemented there
        TableCellRenderer renderer = columnExt.getCellRenderer();
        if (renderer == null) {
            renderer = table.getDefaultRenderer(table.getModel().getColumnClass(columnExt.getModelIndex()));
        }
        return renderer;
    }

    /**
     * Looks up and returns the renderer used for the column's header.<p>
     *
     * @param table the table which contains the column
     * @param columnExt the column to lookup the header renderer for
     * @return the renderer for the columns header, may be null.
     */
    protected TableCellRenderer getHeaderRenderer(JXTable table, TableColumnExt columnExt) {
        TableCellRenderer renderer = columnExt.getHeaderRenderer();
        if (renderer == null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                renderer = header.getDefaultRenderer();
            }
        }
        // JW: default to something if null?
        // if so, could be table's default object/string header?
        return renderer;
    }

    /**
     * Configures the column's <code>preferredWidth</code> to fit the content.
     * It respects the table context, a margin to add and a maximum width. This
     * is typically called in response to a user gesture to adjust the column's
     * width to the "widest" cell content of a column.
     * <p>
     *
     * This implementation loops through all rows of the given column and
     * measures the renderers pref width (it's a potential performance sink).
     * Subclasses can override to implement a different strategy.
     * <p>
     *
     * Note: though 2 * margin is added as spacing, this does <b>not</b> imply
     * a left/right symmetry - it's up to the table to place the renderer and/or
     * the renderer/highlighter to configure a border.<p>
     *
     * PENDING: support pack for hidden column?
     *      This implementation can't handle it! For now, added doc and
     *      fail-fast.
     *
     * @param table the context the column will live in.
     * @param columnExt the column to configure.
     * @param margin the extra spacing to add twice, if -1 uses this factories
     *        default
     * @param max an upper limit to preferredWidth, -1 is interpreted as no
     *        limit
     * @throws IllegalStateException if column is not visible
     *
     * @see #setDefaultPackMargin(int)
     * @see org.jdesktop.swingx.JXTable#packTable(int)
     * @see org.jdesktop.swingx.JXTable#packColumn(int, int)
     *
     */
    public void packColumn(JXTable table, TableColumnExt columnExt, int margin,
            int max) {
        if (!columnExt.isVisible())
            throw new IllegalStateException("column must be visible to pack");

        int column = table.convertColumnIndexToView(columnExt.getModelIndex());
        int width = 0;
        TableCellRenderer headerRenderer = getHeaderRenderer(table, columnExt);
        if (headerRenderer != null) {
            Component comp = headerRenderer.getTableCellRendererComponent(table,
                    columnExt.getHeaderValue(), false, false, 0, column);
            width = comp.getPreferredSize().width;
        }
        // PENDING JW: slightly inconsistent - the getCellRenderer here
        // returns a (guessed) renderer for invisible columns which must not
        // be used in the loop. For now that's okay, as we back out early anyway
        TableCellRenderer renderer = getCellRenderer(table, columnExt);
        for (int r = 0; r < getRowCount(table); r++) {
            // JW: fix for #1215-swing as suggested by the reporter adrienclerc
            Component comp = table.prepareRenderer(renderer, r, column);
//            Component comp = renderer.getTableCellRendererComponent(table, table
//                    .getValueAt(r, column), false, false, r, column);
            width = Math.max(width, comp.getPreferredSize().width);
        }
        if (margin < 0) {
            margin = getDefaultPackMargin();
        }
        width += 2 * margin;

        /* Check if the width exceeds the max */
        if (max != -1 && width > max)
            width = max;

        columnExt.setPreferredWidth(width);

    }

    /**
     * Returns the number of table view rows accessible during row-related
     * config. All row-related access is bounded by the value returned from this
     * method.
     *
     * Here: delegates to table.getRowCount().
     * <p>
     *
     * Subclasses can override to reduce the number (for performance) or support
     * restrictions due to lazy loading, f.i. Implementors must guarantee that
     * view row access with <code>0 <= row < getRowCount(JXTable)</code>
     * succeeds.
     *
     * @param table the table to access
     * @return valid rowCount
     */
    protected int getRowCount(JXTable table) {
        return table.getRowCount();
    }

// ------------------------ default state

    /**
     * Returns the default pack margin.
     *
     * @return the default pack margin to use in packColumn.
     *
     * @see #setDefaultPackMargin(int)
     */
    public int getDefaultPackMargin() {
        return packMargin;
    }

    /**
     * Sets the default pack margin. <p>
     *
     * Note: this is <b>not</b> really a margin in the sense of symmetrically
     * adding white space to the left/right of a cell's content. It's simply an
     * amount of space which is added twice to the measured widths in packColumn.
     *
     * @param margin the default marging to use in packColumn.
     *
     * @see #getDefaultPackMargin()
     * @see #packColumn(JXTable, TableColumnExt, int, int)
     */
    public void setDefaultPackMargin(int margin) {
        this.packMargin = margin;
    }

}
