/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.sort;

import java.text.Collator;
import java.util.Comparator;

import javax.swing.table.TableModel;

/**
 * A SortController to use for a JXTable.<p>
 *
 * @author Jeanette Winzenburg
 */
public class TableSortController<M extends TableModel> extends DefaultSortController<M>  {
    /**
     * Underlying model.
     */
    private M tableModel;

    public TableSortController() {
        this(null);
    }

    /**
     * @param model
     */
    public TableSortController(M model) {
        super();
        setModel(model);
    }

    /**
     * Sets the <code>TableModel</code> to use as the underlying model
     * for this <code>TableRowSorter</code>.  A value of <code>null</code>
     * can be used to set an empty model.
     *
     * @param model the underlying model to use, or <code>null</code>
     */
    public void setModel(M model) {
        tableModel = model;
        if (model != null)
            cachedModelRowCount = model.getRowCount();
        setModelWrapper(new TableRowSorterModelWrapper());
    }

    /**
     * Returns the <code>Comparator</code> for the specified
     * column.  If a <code>Comparator</code> has not been specified using
     * the <code>setComparator</code> method a <code>Comparator</code>
     * will be returned based on the column class
     * (<code>TableModel.getColumnClass</code>) of the specified column.
     * If the column class is <code>String</code>,
     * <code>Collator.getInstance</code> is returned.  If the
     * column class implements <code>Comparable</code> a private
     * <code>Comparator</code> is returned that invokes the
     * <code>compareTo</code> method.  Otherwise
     * <code>Collator.getInstance</code> is returned.<p>
     *
     * PENDING JW: think about implications to string value lookup!
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public Comparator<?> getComparator(int column) {
        Comparator<?> comparator = super.getComparator(column);
        if (comparator != null) {
            return comparator;
        }
        Class<?> columnClass = getModel().getColumnClass(column);
        if (columnClass == String.class) {
            return Collator.getInstance();
        }
        if (Comparable.class.isAssignableFrom(columnClass)) {
            return COMPARABLE_COMPARATOR;
        }
        return Collator.getInstance();
    }

    /**
     * {@inheritDoc}<p>
     * Note: must implement same logic as the overridden comparator
     * lookup, otherwise will throw ClassCastException because
     * here the comparator is never null. <p>
     *
     * PENDING JW: think about implications to string value lookup!
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    protected boolean useToString(int column) {
        Comparator<?> comparator = super.getComparator(column);
        if (comparator != null) {
            return false;
        }
        Class<?> columnClass = getModel().getColumnClass(column);
        if (columnClass == String.class) {
            return false;
        }
        if (Comparable.class.isAssignableFrom(columnClass)) {
            return false;
        }
        return true;
    }

    /**
     * Implementation of DefaultRowSorter.ModelWrapper that delegates to a
     * TableModel.
     */
    private class TableRowSorterModelWrapper extends ModelWrapper<M,Integer> {
        @Override
        public M getModel() {
            return tableModel;
        }

        @Override
        public int getColumnCount() {
            return (tableModel == null) ? 0 : tableModel.getColumnCount();
        }

        @Override
        public int getRowCount() {
            return (tableModel == null) ? 0 : tableModel.getRowCount();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return tableModel.getValueAt(row, column);
        }

        @Override
        public String getStringValueAt(int row, int column) {
            return getStringValueProvider().getStringValue(row, column)
                .getString(getValueAt(row, column));
        }

        @Override
        public Integer getIdentifier(int index) {
            return index;
        }
    }

}
