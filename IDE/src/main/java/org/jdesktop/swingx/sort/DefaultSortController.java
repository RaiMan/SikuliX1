/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultRowSorter;
import javax.swing.SortOrder;

import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.util.Contract;

/**
 * A default SortController implementation used as parent class for concrete
 * SortControllers in SwingX.<p>
 *
 * Additionally, this implementation contains a fix for core
 * <a href=http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6894632>Issue 6894632</a>.
 * It guarantees to only touch the underlying model during sort/filter and during
 * processing the notification methods. This implies that the conversion and size query
 * methods are valid at all times outside the internal updates, including the critical
 * period (in core with undefined behaviour) after the underlying model has changed and
 * before this sorter has been notified.
 *
 * @author Jeanette Winzenburg
 */
public abstract class DefaultSortController<M> extends DefaultRowSorter<M, Integer> implements
        SortController<M> {

    /**
     * Comparator that uses compareTo on the contents.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Comparator COMPARABLE_COMPARATOR =
            new ComparableComparator();

    private final static SortOrder[] DEFAULT_CYCLE = new SortOrder[] {SortOrder.ASCENDING, SortOrder.DESCENDING};

    private List<SortOrder> sortCycle;

    private boolean sortable;

    private StringValueProvider stringValueProvider;

    protected int cachedModelRowCount;

    public DefaultSortController() {
        super();
        setSortable(true);
        setSortOrderCycle(DEFAULT_CYCLE);
        setSortsOnUpdates(true);
    }
    /**
     * {@inheritDoc} <p>
     *
     */
    @Override
    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    /**
     * {@inheritDoc} <p>
     *
     */
    @Override
    public boolean isSortable() {
        return sortable;
    }

    /**
     * {@inheritDoc} <p>
     *
     */
    @Override
    public void setSortable(int column, boolean sortable) {
        super.setSortable(column, sortable);
    }

    /**
     * {@inheritDoc} <p>
     *
     */
    @Override
    public boolean isSortable(int column) {
        if (!isSortable()) return false;
        return super.isSortable(column);
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Overridden - that is completely new implementation - to get first/next SortOrder
     * from sort order cycle. Does nothing if the cycle is empty.
     */
    @Override
    public void toggleSortOrder(int column) {
        checkColumn(column);
        if (!isSortable(column))
            return;
        SortOrder firstInCycle = getFirstInCycle();
        // nothing to toggle through
        if (firstInCycle == null)
            return;
        List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
        SortKey sortKey = SortUtils.getFirstSortKeyForColumn(keys, column);
        if (keys.indexOf(sortKey) == 0)  {
            //  primary key: in this case we'll use next sortorder in cylce
            keys.set(0, new SortKey(column, getNextInCycle(sortKey.getSortOrder())));
        } else {
            // all others: make primary with first sortOrder in cycle
            keys.remove(sortKey);
            keys.add(0, new SortKey(column, getFirstInCycle()));
        }
        if (keys.size() > getMaxSortKeys()) {
            keys = keys.subList(0, getMaxSortKeys());
        }
        setSortKeys(keys);
    }

    /**
     * Returns the next SortOrder relative to the current, or null
     * if the sort order cycle is empty.
     *
     * @param current the current SortOrder
     * @return the next SortOrder to use, may be null if the cycle is empty.
     */
    private SortOrder getNextInCycle(SortOrder current) {
        int pos = sortCycle.indexOf(current);
        if (pos < 0) {
            // not in cycle ... what to do?
            return getFirstInCycle();
        }
        pos++;
        if (pos >= sortCycle.size()) {
            pos = 0;
        }
        return sortCycle.get(pos);
    }

    /**
     * Returns the first SortOrder in the sort order cycle, or null if empty.
     *
     * @return the first SortOrder in the sort order cycle or null if empty.
     */
    private SortOrder getFirstInCycle() {
        return sortCycle.size() > 0 ? sortCycle.get(0) : null;
    }

    private void checkColumn(int column) {
        if (column < 0 || column >= getModelWrapper().getColumnCount()) {
            throw new IndexOutOfBoundsException(
                    "column beyond range of TableModel");
        }
    }

    /**
     * {@inheritDoc} <p>
     *
     * PENDING JW: toggle has two effects: makes the column the primary sort column,
     * and cycle through. So here we something similar. Should we?
     *
     */
    @Override
    public void setSortOrder(int column, SortOrder sortOrder) {
        if (!isSortable(column)) return;
        SortKey replace = new SortKey(column, sortOrder);
        List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
        SortUtils.removeFirstSortKeyForColumn(keys, column);
        keys.add(0, replace);
        // PENDING max sort keys, respect here?
        setSortKeys(keys);
    }

    /**
     * {@inheritDoc} <p>
     *
     */
    @Override
    public SortOrder getSortOrder(int column) {
        SortKey key = SortUtils.getFirstSortKeyForColumn(getSortKeys(), column);
        return key != null ? key.getSortOrder() : SortOrder.UNSORTED;
    }

    /**
     * {@inheritDoc} <p>
     *
     */
    @Override
    public void resetSortOrders() {
        if (!isSortable()) return;
        List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
        for (int i = keys.size() -1; i >= 0; i--) {
            SortKey sortKey = keys.get(i);
            if (isSortable(sortKey.getColumn())) {
                keys.remove(sortKey);
            }

        }
        setSortKeys(keys);

    }

    /**
     * {@inheritDoc} <p>
     */
    @Override
    public SortOrder[] getSortOrderCycle() {
        return sortCycle.toArray(new SortOrder[0]);
    }

    /**
     * {@inheritDoc} <p>
     */
    @Override
    public void setSortOrderCycle(SortOrder... cycle) {
        Contract.asNotNull(cycle, "Elements of SortOrderCycle must not be null");
        // JW: not safe enough?
        sortCycle = Arrays.asList(cycle);
    }

    /**
     * Sets the registry of string values. If null, the default provider is used.
     *
     * @param registry the registry to get StringValues for conversion.
     */
    @Override
    public void setStringValueProvider(StringValueProvider registry) {
        this.stringValueProvider = registry;
//        updateStringConverter();
    }

    /**
     * Returns the registry of string values.
     *
     * @return the registry of string converters, guaranteed to never be null.
     */
    @Override
    public StringValueProvider getStringValueProvider() {
        if (stringValueProvider == null) {
            stringValueProvider = DEFAULT_PROVIDER;
        }
        return stringValueProvider;
    }

    /**
     * Returns the default cycle.
     *
     * @return default sort order cycle.
     */
    public static SortOrder[] getDefaultSortOrderCycle() {
        return Arrays.copyOf(DEFAULT_CYCLE, DEFAULT_CYCLE.length);
    }

    private static final StringValueProvider DEFAULT_PROVIDER = new StringValueProvider() {

        @Override
        public StringValue getStringValue(int row, int column) {
            return StringValues.TO_STRING;
        }

    };

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static class ComparableComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            return ((Comparable)o1).compareTo(o2);
        }
    }

//-------------------------- replacing super for more consistent conversion/rowCount behaviour

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to use check against <code>getViewRowCount</code> for validity.
     *
     * @see #getViewRowCount()
     */
    @Override
    public int convertRowIndexToModel(int viewIndex) {
        if ((viewIndex < 0) || viewIndex >= getViewRowCount())
            throw new IndexOutOfBoundsException("valid viewIndex: 0 <= index < "
                    + getViewRowCount()
                    + " but was: " + viewIndex);
        try {
             return super.convertRowIndexToModel(viewIndex);
        } catch (Exception e) {
            // this will happen only if unsorted/-filtered and super
            // incorrectly access the model while it had been changed
            // under its feet
        }
        return viewIndex;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to use check against <code>getModelRowCount</code> for validity.
     *
     * @see #getModelRowCount()
     */
    @Override
    public int convertRowIndexToView(int modelIndex) {
        if ((modelIndex < 0) || modelIndex >= getModelRowCount())
            throw new IndexOutOfBoundsException("valid modelIndex: 0 <= index < "
                    + getModelRowCount()
                    + " but was: " + modelIndex);
        try {
            return super.convertRowIndexToView(modelIndex);
        } catch (Exception e) {
            // this will happen only if unsorted/-filtered and super
            // incorrectly access the model while it had been changed
            // under its feet
        }
        return modelIndex;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return the model row count which corresponds to the currently
     * mapped model instead of accessing the model directly (as super does).
     * This may differ from the "real" current model row count if the model has changed
     * but this sorter not yet notified.
     *
     */
    @Override
    public int getModelRowCount() {
        return cachedModelRowCount;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return the model row count if no filters installed, otherwise
     * return super.
     *
     * @see #getModelRowCount()
     *
     */
    @Override
    public int getViewRowCount() {
        if (hasRowFilter())
            return super.getViewRowCount();
        return getModelRowCount();
    }

    /**
     * @return
     */
    private boolean hasRowFilter() {
        return getRowFilter() != null;
    }

//------------------ overridden notification methods: cache model row count
    @Override
    public void allRowsChanged() {
        cachedModelRowCount = getModelWrapper().getRowCount();
        super.allRowsChanged();
    }
    @Override
    public void modelStructureChanged() {
        super.modelStructureChanged();
        cachedModelRowCount = getModelWrapper().getRowCount();
    }
    @Override
    public void rowsDeleted(int firstRow, int endRow) {
        cachedModelRowCount = getModelWrapper().getRowCount();
        super.rowsDeleted(firstRow, endRow);
    }
    @Override
    public void rowsInserted(int firstRow, int endRow) {
        cachedModelRowCount = getModelWrapper().getRowCount();
        super.rowsInserted(firstRow, endRow);
    }

}
