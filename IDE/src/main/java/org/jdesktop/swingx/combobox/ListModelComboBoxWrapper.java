/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.combobox;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

public class ListModelComboBoxWrapper extends AbstractListModel implements ComboBoxModel {
    private ListModel delegate;

    private Object selectedItem;

    public ListModelComboBoxWrapper(ListModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public Object getElementAt(int index) {
        return delegate.getElementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        super.addListDataListener(l);
        delegate.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        delegate.removeListDataListener(l);
        super.removeListDataListener(l);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if ((selectedItem != null && !selectedItem.equals(anItem))
                || selectedItem == null && anItem != null) {
            selectedItem = anItem;

            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }
}
