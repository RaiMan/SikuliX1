/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.sort;

import javax.swing.ListModel;

/**
 * A SortController to use with JXList.
 *
 * @author Jeanette Winzenburg
 */
public class ListSortController<M extends ListModel> extends DefaultSortController<M> {

    /** underlying model */
    private M listModel;
    /**
     * @param model
     */
    public ListSortController(M model) {
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
        listModel = model;
        if (model != null)
            cachedModelRowCount = model.getSize();
        setModelWrapper(new ListRowSorterModelWrapper());
    }

    /**
     * Implementation of DefaultRowSorter.ModelWrapper that delegates to a
     * TableModel.
     */
    private class ListRowSorterModelWrapper extends ModelWrapper<M,Integer> {
        @Override
        public M getModel() {
            return listModel;
        }

        @Override
        public int getColumnCount() {
            return (listModel == null) ? 0 : 1;
        }

        @Override
        public int getRowCount() {
            return (listModel == null) ? 0 : listModel.getSize();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return listModel.getElementAt(row);
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
