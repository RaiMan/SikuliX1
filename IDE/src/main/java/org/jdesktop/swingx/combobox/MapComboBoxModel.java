/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.combobox;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@code ComboBoxModel} for {@code Map}s. The model will always present a {@code Map}
 * consistently, once it is instantiated. However, unless the {@code Map} is ordered, as a
 * {@code java.util.TreeMap} is, the model is not guaranteed to present the maps in a consistent
 * order between instantiations.
 *
 * @author jm158417
 * @author Karl George Schaefer
 *
 * @param <K>
 *                the type of keys maintained by the map backing this model
 * @param <V>
 *                the type of mapped values
 */
public class MapComboBoxModel<K, V> extends ListComboBoxModel<K> {

    /**
     * The map backing this model.
     */
    protected Map<K, V> map_data;

    /**
     * Creates an empty model.
     */
    public MapComboBoxModel() {
        this(new LinkedHashMap<K, V>());
    }

    /**
     * Creates a model backed by the specified map.
     *
     * @param map
     *                the map backing this model
     */
    public MapComboBoxModel(Map<K, V> map) {
        super(buildIndex(map));

        this.map_data = map;
    }

    /**
     * Builds an index for this model. This method ensures that the map is always presented in a
     * consistent order.
     * <p>
     * This method is called by the constructor and the {@code List} is passed to {@code super}.
     *
     * @param <E>
     *                the type of keys for the map
     * @param map
     *                the map to build an index for
     * @return a list containing the map keys
     */
    private static <E> List<E> buildIndex(Map<E, ?> map) {
        return new ArrayList<E>(map.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return map_data.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        //kgs - this code might not be performant with large maps
        if(evt.getActionCommand().equals(UPDATE)) {
            //add new keys
            Set<K> keys = map_data.keySet();
            keys.removeAll(data);
            data.addAll(keys);

            //remove dead keys
            List<K> copy = new ArrayList<K>(data);
            keys = map_data.keySet();
            copy.removeAll(keys);
            data.removeAll(copy);

            fireContentsChanged(this, 0, getSize() - 1);
        }
    }

    /**
     * Selects an item from the model and returns that map value.
     *
     * @param selectedItem
     *                the item to select
     * @return the value for the selected item
     */
    public V getValue(Object selectedItem) {
        return map_data.get(selectedItem);
    }

    /**
     * Selects an item from the model and returns that map value.
     *
     * @param selectedItem
     *                selects the item at the specified index in this model
     * @return the value for the item at the selected index
     */
    public V getValue(int selectedItem) {
        return getValue(getElementAt(selectedItem));
    }
}
