/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * Created on 23.04.2009
 *
 */
package org.jdesktop.swingx.event;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import org.jdesktop.beans.AbstractBean;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.util.Contract;

/**
 * An convenience class which maps focusEvents received
 * from a container hierarchy to a bound read-only property. Registered
 * PropertyChangeListeners are notified if the focus is transfered into/out of
 * the hierarchy of a given root.
 * <p>
 *
 * F.i, client code which wants to get notified if focus enters/exits the hierarchy below
 * panel would install the compound focus listener like:
 *
 * <pre>
 * <code>
 *         // add some components inside
 *         panel.add(new JTextField(&quot;something to .... focus&quot;));
 *         panel.add(new JXDatePicker(new Date()));
 *         JComboBox combo = new JComboBox(new Object[] {&quot;dooooooooo&quot;, 1, 2, 3, 4 });
 *         combo.setEditable(true);
 *         panel.add(new JButton(&quot;something else to ... focus&quot;));
 *         panel.add(combo);
 *         panel.setBorder(new TitledBorder(&quot;has focus dispatcher&quot;));
 *         // register the compound dispatcher
 *         CompoundFocusListener report = new CompoundFocusListener(panel);
 *         PropertyChangeListener l = new PropertyChangeListener() {
 *
 *             public void propertyChange(PropertyChangeEvent evt) {
 *                 // do something useful here
 *
 *             }};
 *         report.addPropertyChangeListener(l);
 *
 * </code>
 * </pre>
 *
 * PENDING JW: change of current instance of KeyboardFocusManager?
 *
 */
public class CompoundFocusListener extends AbstractBean {

    /** the root of the component hierarchy.
     * PENDING JW: weak reference and auto-release listener?
     */
    private JComponent root;
    /** PropertyChangeListener registered with the current keyboardFocusManager. */
    private PropertyChangeListener managerListener;
    private boolean focused;

    /**
     * Instantiates a CompoundFocusListener on the component hierarchy below the given
     * component.
     *
     * @param root the root of a component hierarchy
     * @throws NullPointerException if the root is null
     */
    public CompoundFocusListener(JComponent root) {
        this.root = Contract.asNotNull(root, "root must not be null");
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        addManagerListener(manager);
        permanentFocusOwnerChanged(manager.getPermanentFocusOwner());
    }

    /**
     * Return true if the root or any of its descendants is focused. This is a
     * read-only bound property, that is property change event is fired if focus
     * is transfered into/out of root's hierarchy.
     *
     * @return a boolean indicating whether or not any component in the
     *         container hierarchy below root is permanent focus owner.
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Releases all listeners and internal references.<p>
     *
     * <b>Note</b>: this instance must not be used after calling this method.
     *
     */
    public void release() {
        removeManagerListener(KeyboardFocusManager.getCurrentKeyboardFocusManager());
        removeAllListeners();
        this.root = null;
    }

    /**
     * Removes all property change listeners which are registered with this instance.
     */
    private void removeAllListeners() {
        for (PropertyChangeListener l : getPropertyChangeListeners()) {
            removePropertyChangeListener(l);
        }
    }

    /**
     * Updates focused property depending on whether or not the given component
     * is below the root's hierarchy. <p>
     *
     * Note: Does nothing if the component is null. This might not be entirely correct,
     * but property change events from the focus manager come in pairs, with only
     * one of the new/old value not-null.
     *
     * @param focusOwner the component with is the current focusOwner.
     */
    protected void permanentFocusOwnerChanged(Component focusOwner) {
        if (focusOwner == null) return;
        setFocused(SwingXUtilities.isDescendingFrom(focusOwner, root));
    }

    private void setFocused(boolean focused) {
        boolean old = isFocused();
        this.focused = focused;
        firePropertyChange("focused", old, isFocused());
    }

    /**
     * Adds all listeners to the given KeyboardFocusManager. <p>
     *
     * @param manager the KeyboardFocusManager to add internal listeners to.
     * @see #removeManagerListener(KeyboardFocusManager)
     */
    private void addManagerListener(KeyboardFocusManager manager) {
        manager.addPropertyChangeListener("permanentFocusOwner", getManagerListener());
    }

    /**
     * Removes all listeners this instance has installed from the given KeyboardFocusManager.<p>
     *
     * @param manager the KeyboardFocusManager to remove internal listeners from.
     * @see #addManagerListener(KeyboardFocusManager)
     */
    private void removeManagerListener(KeyboardFocusManager manager) {
        manager.removePropertyChangeListener("permanentFocusOwner", getManagerListener());
    }

    /**
     * Lazily creates and returns a property change listener to be registered on the
     * KeyboardFocusManager.
     *
     * @return a property change listener to be registered on the KeyboardFocusManager.
     */
    private PropertyChangeListener getManagerListener() {
        if (managerListener == null) {
            managerListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("permanentFocusOwner".equals(evt.getPropertyName())) {
                        permanentFocusOwnerChanged((Component) evt.getNewValue());
                    }

                }};
        }
        return managerListener;
    }

}
