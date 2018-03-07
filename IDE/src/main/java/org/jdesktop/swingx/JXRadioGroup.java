/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.EventListenerList;

import org.jdesktop.beans.JavaBean;

/**
 * <p>
 * {@code JXRadioGroup} is a group of radio buttons that functions as a unit. It
 * is similar in concept to a {@link JComboBox} in functionality, but can offer
 * a better presentation for a small number of choices. {@code JXRadioGroup}
 * should be used in preference to {@code JComboBox} when the number of choices
 * is small (less than six) or the choices are verbose.
 * </p>
 * <p>
 * Notes:
 * <ol>
 * <li>Enabling and disabling the JXRadioGroup will enable/disable all of the
 * child buttons inside the JXRadioGroup.</li>
 * <li>
 * If the generic type parameter of JXRadioGroup is a subclass of
 * {@link AbstractButton}, then the buttons will be added "as is" to the
 * container. If the generic type is anything else, buttons will be created as
 * {@link JRadioButton} objects, and the button text will be set by calling
 * toString() on the value object.</li>
 * <li>
 * Alternatively, if you want to configure the buttons individually, construct
 * the JXRadioGroup normally, and then call {@link #getChildButton(int)} or
 * {@link #getChildButton(Object)} and configure the buttons.</li>
 * </ol>
 * </p>
 * <p>
 * TODO back with a model (possibly reuse of extend {@link ComboBoxModel}
 * </p>
 *
 * @author Amy Fowler
 * @author Noel Grandin
 * @version 1.0
 */
@JavaBean
public class JXRadioGroup<T> extends JPanel {

    private static final long serialVersionUID = 3257285842266567986L;

    private ButtonGroup buttonGroup;

    private final List<T> values = new ArrayList<T>();

    private ActionSelectionListener actionHandler;

    /**
     * Create a default JXRadioGroup with a default layout axis of {@link BoxLayout#X_AXIS}.
     */
    public JXRadioGroup() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        buttonGroup = new ButtonGroup();
    }

    /**
     * Create a default JXRadioGroup with a default layout axis of {@link BoxLayout#X_AXIS}.
     *
     * @param radioValues the list of values used to create the group.
     */
    public JXRadioGroup(T[] radioValues) {
        this();
        for (int i = 0; i < radioValues.length; i++) {
            add(radioValues[i]);
        }
    }

    /**
     * Convenience factory method.
     * Reduces code clutter when dealing with generics.
     *
     * @param radioValues the list of values used to create the group.
     */
    public static <T> JXRadioGroup<T> create(T[] radioValues)
    {
        return new JXRadioGroup<T>(radioValues);
    }

    /**
     * Set the layout axis of the radio group.
     *
     * @param axis values from {@link BoxLayout}.
     */
    public void setLayoutAxis(int axis)
    {
        setLayout(new BoxLayout(this, axis));
    }

    /**
     * Sets the values backing this group. This replaces the current set of
     * values with the new set.
     *
     * @param radioValues
     *            the new backing values for this group
     */
    public void setValues(T[] radioValues) {
        clearAll();
        for (int i = 0; i < radioValues.length; i++) {
            add(radioValues[i]);
        }
    }

    private void clearAll() {
        values.clear();
        buttonGroup = new ButtonGroup();
        // remove all the child components
        removeAll();
    }

    /**
     * You can use this method to manually add your own AbstractButton objects, provided you declared
     * the class as <code>JXRadioGroup&lt;JRadioButton&gt;</code>.
     */
    public void add(T radioValue) {
        if (values.contains(radioValue))
        {
            throw new IllegalArgumentException("cannot add the same value twice " + radioValue);
        }
        if (radioValue instanceof AbstractButton) {
            values.add(radioValue);
            addButton((AbstractButton) radioValue);
        } else {
            values.add(radioValue);
            // Note: the "quote + object" trick here allows null values
            addButton(new JRadioButton(""+radioValue));
        }
    }

    private void addButton(AbstractButton button) {
        buttonGroup.add(button);
        super.add(button);
        if (actionHandler == null) {
            actionHandler = new ActionSelectionListener();
        }
        button.addActionListener(actionHandler);
        button.addItemListener(actionHandler);
    }

    private class ActionSelectionListener implements ActionListener, ItemListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            fireActionEvent(e);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireActionEvent(null);
        }
    }

    /**
     * Gets the currently selected button.
     *
     * @return the currently selected button
     * @see #getSelectedValue()
     */
    public AbstractButton getSelectedButton() {
        final ButtonModel selectedModel = buttonGroup.getSelection();
        final AbstractButton children[] = getButtonComponents();
        for (int i = 0; i < children.length; i++) {
            AbstractButton button = children[i];
            if (button.getModel() == selectedModel) {
                return button;
            }
        }
        return null;
    }

    private AbstractButton[] getButtonComponents() {
        final Component[] children = getComponents();
        final List<AbstractButton> buttons = new ArrayList<AbstractButton>();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof AbstractButton) {
                buttons.add((AbstractButton) children[i]);
            }
        }
        return buttons.toArray(new AbstractButton[buttons.size()]);
    }

    private int getSelectedIndex() {
        final ButtonModel selectedModel = buttonGroup.getSelection();
        final Component children[] = getButtonComponents();
        for (int i = 0; i < children.length; i++) {
            AbstractButton button = (AbstractButton) children[i];
            if (button.getModel() == selectedModel) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The currently selected value.
     *
     * @return the current value
     */
    public T getSelectedValue() {
        final int index = getSelectedIndex();
        return (index < 0 || index >= values.size()) ? null : values.get(index);
    }

    /**
     * Selects the supplied value.
     *
     * @param value
     *            the value to select
     */
    public void setSelectedValue(T value) {
        final int index = values.indexOf(value);
        AbstractButton button = getButtonComponents()[index];
        button.setSelected(true);
    }

    /**
     * Retrieve the child button by index.
     */
    public AbstractButton getChildButton(int index) {
        return getButtonComponents()[index];
    }

    /**
     * Retrieve the child button that represents this value.
     */
    public AbstractButton getChildButton(T value) {
        final int index = values.indexOf(value);
        return getButtonComponents()[index];
    }

    /**
     * Get the number of child buttons.
     */
    public int getChildButtonCount() {
        return getButtonComponents().length;
    }

    /**
     * Adds an <code>ActionListener</code>.
     * <p>
     * The <code>ActionListener</code> will receive an <code>ActionEvent</code>
     * when a selection has been made.
     *
     * @param l  the <code>ActionListener</code> that is to be notified
     * @see #setSelectedValue(Object)
     */
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an <code>ActionListener</code>.
     *
     * @param l
     *            the <code>ActionListener</code> to remove
     */
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    /**
     * Returns an array of all the <code>ActionListener</code>s added
     * to this JRadioGroup with addActionListener().
     *
     * @return all of the <code>ActionListener</code>s added or an empty
     *         array if no listeners have been added
     */
    public ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     *
     * @param e
     *            the event to pass to the listeners
     * @see EventListenerList
     */
    protected void fireActionEvent(ActionEvent e) {
        for (ActionListener l : getActionListeners()) {
            l.actionPerformed(e);
        }
    }

    /**
     * Enable/disable all of the child buttons
     *
     * @see JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Enumeration<AbstractButton> en = buttonGroup.getElements(); en.hasMoreElements();) {
            final AbstractButton button = en.nextElement();
            /* We don't want to enable a button where the action does not
             * permit it. */
            if (enabled && button.getAction() != null
                    && !button.getAction().isEnabled()) {
                // do nothing
            } else {
                button.setEnabled(enabled);
            }
        }
    }

}
