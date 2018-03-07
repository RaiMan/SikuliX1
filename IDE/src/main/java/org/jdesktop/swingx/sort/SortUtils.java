/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * Created on 15.03.2006
 *
 */
package org.jdesktop.swingx.sort;

import java.util.List;

import javax.swing.RowSorter;
import javax.swing.SortOrder;

/**
 * Collection of convenience methods.
 */
public class SortUtils {

//---------------------- static utility methods

    /**
     * Returns the first SortKey in the list which is sorted.
     * If none is sorted, null is returned.
     *
     * @param keys a list of SortKeys to search
     * @return the first SortKey which is sorted or null, if no
     *   is found.
     */
    public static RowSorter.SortKey getFirstSortingKey(List<? extends RowSorter.SortKey> keys) {
        for (RowSorter.SortKey key : keys) {
            if (isSorted(key.getSortOrder())) {
                return key;
            }
        }
        return null;
    }

    /**
     * Returns the first SortKey in the list for the given column,
     * or null if the column has no SortKey.
     *
     * @param keys a list of SortKeys to search
     * @param modelColumn the column index in model coordinates
     * @return the first SortKey for the given column or null if none is
     *   found.
     */
    public static RowSorter.SortKey getFirstSortKeyForColumn(List<? extends RowSorter.SortKey> keys, int modelColumn) {
        for (RowSorter.SortKey key : keys) {
            if (key.getColumn() == modelColumn) {
                return key;
            }
        }
        return null;
    }

    /**
     * Removes and returns the first SortKey in the list for the given column,
     * or null if the column has no SortKey.
     *
     * @param keys a list of SortKeys to search
     * @param modelColumn the column index in model coordinates
     * @return the first SortKey for the given column or null if none is
     *   found.
     */
    public static RowSorter.SortKey removeFirstSortKeyForColumn(List<? extends RowSorter.SortKey> keys, int modelColumn) {
        for (RowSorter.SortKey key : keys) {
            if (key.getColumn() == modelColumn) {
                keys.remove(key);
                return key;
            }
        }
        return null;
    }
    public static boolean isSorted(SortOrder sortOrder) {
        return sortOrder != null && (SortOrder.UNSORTED != sortOrder);
    }

    /**
     * Convenience to check for ascending sort order.
     * PENDING: is this helpful at all?
     *
     * @return true if ascendingly sorted, false for unsorted/descending.
     */
    public static boolean isAscending(SortOrder sortOrder) {
        return sortOrder == SortOrder.ASCENDING;
    }

    public static boolean isSorted(SortOrder sortOrder, boolean ascending) {
        return isSorted(sortOrder) && (ascending == isAscending(sortOrder));
    }

    private SortUtils() {};
}
