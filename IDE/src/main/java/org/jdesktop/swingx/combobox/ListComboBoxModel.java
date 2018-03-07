/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.combobox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * A {@code ComboBoxModel} for {@code List}s.
 *
 * @param <E> the type of elements maintained by the list backing this model
 *
 * @author jm158417
 * @author Karl George Schaefer
 */
public class ListComboBoxModel<E> extends AbstractListModel implements ComboBoxModel, ActionListener {
    /**
     * A key used to notify the model that the backing {@code List} has changed.
     */
    public static final String UPDATE = "update";

    /**
     * A reference to the list backing this model.
     * <p>
     * This model does <b>not</b> make a copy of the list, so any changes in
     * the list without synchronizing the model may have drastic effects.
     */
    protected final List<E> data;

    /**
     * The currently selected item.
     */
    protected E selected;

    /**
     * Creates a {@code ListComboBoxModel} backed by the supplied {@code list}.
     *
     * @param list
     *                the list backing this model
     * @throws NullPointerException
     *                 if {@code list} is {@code null}
     */
    public ListComboBoxModel(List<E> list) {
        this.data = list;

        if(list.size() > 0) {
            selected = list.get(0);
        }
    }

    /**
     * Set the selected item. The implementation of this method should notify
     * all registered {@code ListDataListener}s that the contents have changed.
     *
     * @param item
     *                the list object to select or {@code null} to clear the
     *                selection
     * @throws ClassCastException
     *                 if {@code item} is not of type {@code E}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object item) {
        if ((selected != null && !selected.equals(item))
                || selected == null && item != null) {
            selected = (E) item;
            fireContentsChanged(this, -1, -1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getSelectedItem() {
        return this.selected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getElementAt(int index) {
        return data.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return data.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getActionCommand().equals(UPDATE)) {
            this.fireContentsChanged(this, 0, getSize() - 1);
        }
    }
}
