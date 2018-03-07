/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.table;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.action.ActionContainerFactory;
import org.jdesktop.swingx.plaf.ColumnControlButtonAddon;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.table.ColumnControlPopup.ActionGrouper;
import org.jdesktop.swingx.table.ColumnControlPopup.ActionGroupable;

/**
 * A component to allow interactive customization of <code>JXTable</code>'s
 * columns.
 * It's main purpose is to allow toggling of table columns' visibility.
 * Additionally, arbitrary configuration actions can be exposed.
 * <p>
 *
 * This component is installed in the <code>JXTable</code>'s
 * trailing corner, if enabled:
 *
 * <pre><code>
 * table.setColumnControlVisible(true);
 * </code></pre>
 *
 * From the perspective of a <code>JXTable</code>, the component's behaviour is
 * opaque. Typically, the button's action is to popup a component for user
 * interaction. <p>
 *
 * This class is responsible for handling/providing/updating the lists of
 * actions and to keep each Action's state in synch with Table-/Column state.
 * The visible behaviour of the popup is delegated to a
 * <code>ColumnControlPopup</code>. <p>
 *
 * Default support for adding table (configuration or other) <code>Action</code>s is
 * informal, driven by convention:
 * <ul>
 * <li> the JXTable's actionMap is scanned for candidate actions, the default marker
 *   is a key of type String which starts with {@link ColumnControlButton.COLUMN_CONTROL_MARKER}
 * <li> the actions are sorted by that key and then handed over to the ColumnControlPopup
 *   for binding and addition of appropriate menu items
 * <li> the addition as such is control by additionalActionsVisible property, its
 *   default value is true
 * </ul>
 *
 *
 *
 * @see TableColumnExt
 * @see TableColumnModelExt
 * @see JXTable#setColumnControl
 *
 */
public class ColumnControlButton extends JButton {

    // JW: really want to extend? for builders?
    /** Marker to auto-recognize actions which should be added to the popup. */
    public static final String COLUMN_CONTROL_MARKER = "column.";

    /** the key for looking up the control's icon in the UIManager. Typically, it's LAF dependent. */
    public static final String COLUMN_CONTROL_BUTTON_ICON_KEY = "ColumnControlButton.actionIcon";

    /** the key for looking up the control's margin in the UIManager. Typically, it's LAF dependent. */
    public static final String COLUMN_CONTROL_BUTTON_MARGIN_KEY = "ColumnControlButton.margin";

    static {
        LookAndFeelAddons.contribute(new ColumnControlButtonAddon());
    }

    /** exposed for testing. */
    protected ColumnControlPopup popup;
    // TODO: the table reference is a potential leak?
    /** The table which is controlled by this. */
    private JXTable table;
    /** Listener for table property changes. */
    private PropertyChangeListener tablePropertyChangeListener;
    /** Listener for table's columnModel. */
    TableColumnModelListener columnModelListener;
    /** the list of actions for column menuitems.*/
    private List<ColumnVisibilityAction> columnVisibilityActions;

    private boolean additionalActionsVisible;

    /**
     * Creates a column control button for the table. Uses the default
     * icon as provided by the addon.
     *
     * @param table  the <code>JXTable</code> controlled by this component
     */
    public ColumnControlButton(JXTable table) {
        this(table, null);
    }

    /**
     * Creates a column control button for the table. The button
     * uses the given icon and has no text.
     * @param table  the <code>JXTable</code> controlled by this component
     * @param icon the <code>Icon</code> to show
     */
    public ColumnControlButton(JXTable table, Icon icon) {
        super();
        init();
        // JW: icon LF dependent?
        setAction(createControlAction(icon));
        updateActionUI();
        updateButtonUI();
        installTable(table);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // JW: icon may be LF dependent
        updateActionUI();
        updateButtonUI();
        getColumnControlPopup().updateUI();
    }

    /**
     * Updates this button's properties provided by the LAF.
     * Here: overwrites the action's small_icon with the icon from the ui if the current
     *   icon is null or a UIResource.
     */
    protected void updateButtonUI() {
        if ((getMargin() == null) || (getMargin() instanceof UIResource)) {
            Insets insets = UIManager.getInsets(COLUMN_CONTROL_BUTTON_MARGIN_KEY);
            setMargin(insets);
        }
    }

    /**
     * Updates the action properties provided by the LAF.
     * Here: overwrites the action's small_icon with the icon from the ui if the current
     *   icon is null or a UIResource.
     */
    protected void updateActionUI() {
        if (getAction() == null) return;
        Icon icon = (Icon) getAction().getValue(Action.SMALL_ICON);
        if ((icon == null) || (icon instanceof UIResource)) {
            icon = UIManager.getIcon(COLUMN_CONTROL_BUTTON_ICON_KEY);
            getAction().putValue(Action.SMALL_ICON, icon);
        }
    }

    /**
     * Toggles the popup component's visibility. This method is
     * called by this control's default action. <p>
     *
     * Here: delegates to getControlPopup().
     */
    public void togglePopup() {
        getColumnControlPopup().toggleVisibility(this);
    }

    /**
     * Returns the actionsVisible property which controls whether or not
     * additional table Actions should be included into the popup.
     *
     * @return a boolean indicating whether or not additional table Actions
     *    are visible
     */
    public boolean getAdditionalActionsVisible() {
        return additionalActionsVisible;
    }

    /**
     * Sets the additonalActionsVisible property. It controls whether or
     * not additional table actions should be included into the popup. <p>
     *
     * The default value is <code>true</code>.
     *
     * @param additionalActionsVisible the additionalActionsVisible to set
     */
    public void setAdditionalActionsVisible(boolean additionalActionsVisible) {
        if (additionalActionsVisible == getAdditionalActionsVisible()) return;
        boolean old = getAdditionalActionsVisible();
        this.additionalActionsVisible = additionalActionsVisible;
        populatePopup();
        firePropertyChange("additionalActionsVisible", old, getAdditionalActionsVisible());
    }

    /**
     * Sets the grouper to use for grouping the additional actions. Maybe null to
     * have no additional grouping. Has no effect
     * if the ColumnControlPopup doesn't implement Groupable. The default
     * ColumnControlPopup supports Groupable, but is instantiated without a Grouper.
     *
     * @param grouper
     */
    public void setActionGrouper(ActionGrouper grouper) {
        if (!(getColumnControlPopup() instanceof ActionGroupable)) return;
        ((ActionGroupable) getColumnControlPopup()).setActionGrouper(grouper);
        populatePopup();
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        getColumnControlPopup().applyComponentOrientation(o);
    }

//-------------------------- Action in synch with column properties
    /**
     * A specialized <code>Action</code> which takes care of keeping in synch with
     * TableColumn state.
     *
     * NOTE: client must call releaseColumn if this action is no longer needed!
     *
     */
    public class ColumnVisibilityAction extends AbstractActionExt {

        private TableColumn column;

        private PropertyChangeListener columnListener;

        /** flag to distinguish selection changes triggered by
         *  column's property change from those triggered by
         *  user interaction. Hack around #212-swingx.
         */
        private boolean fromColumn;

        /**
         * Creates a action synched to the table column.
         *
         * @param column the <code>TableColumn</code> to keep synched to.
         */
        public ColumnVisibilityAction(TableColumn column) {
            super((String) null);
            setStateAction();
            installColumn(column);
        }

        /**
         * Releases all references to the synched <code>TableColumn</code>.
         * Client code must call this method if the
         * action is no longer needed. After calling this action must not be
         * used any longer.
         */
        public void releaseColumn() {
            column.removePropertyChangeListener(columnListener);
            column = null;
        }

        /**
         * Returns true if the action is enabled. Returns
         * true only if the action is enabled and the table
         * column can be controlled.
         *
         * @return true if the action is enabled, false otherwise
         * @see #canControlColumn()
         */
        @Override
        public boolean isEnabled() {
            return super.isEnabled() && canControlColumn();
        }

        /**
         * Returns flag to indicate if column's visibility can
         * be controlled. Minimal requirement is that column is of type
         * <code>TableColumnExt</code>.
         *
         * @return boolean to indicate if columns's visibility can be controlled.
         */
        protected boolean canControlColumn() {
            // JW: should have direction? control is from action to column, the
            // other way round should be guaranteed always
            return (column instanceof TableColumnExt);
        }

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (canControlColumn()) {
                if ((e.getStateChange() == ItemEvent.DESELECTED)
                        //JW: guarding against 1 leads to #212-swingx: setting
                        // column visibility programatically fails if
                        // the current column is the second last visible
                        // guarding against 0 leads to hiding all columns
                        // by deselecting the menu item.
                        && (table.getColumnCount() <= 1)
                        // JW Fixed #212: basically implemented Rob's idea to distinguish
                        // event sources instead of unconditionally reselect
                        // not entirely sure if the state transitions are completely
                        // defined but all related tests are passing now.
                        && !fromColumn) {
                    reselect();
                } else {
                    setSelected(e.getStateChange() == ItemEvent.SELECTED);
                }
            }
        }

        @Override
        public synchronized void setSelected(boolean newValue) {
            super.setSelected(newValue);
            if (canControlColumn()) {
                if (!fromColumn)
                ((TableColumnExt) column).setVisible(newValue);
            }
        }

        /**
         * Does nothing. Synch from action state to TableColumn state
         * is done in itemStateChanged.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

        }

        /**
         * Synchs selected property to visible. This
         * is called on change of tablecolumn's <code>visible</code> property.
         *
         * @param visible column visible state to synch to.
         */
        private void updateFromColumnVisible(boolean visible) {
//            /*boolean*/ visible = true;
//            if (canControlColumn()) {
//                visible = ((TableColumnExt) column).isVisible();
//            }
            fromColumn = true;
            setSelected(visible);
            fromColumn = false;
        }

        protected void updateFromColumnHideable(boolean hideable) {
            setEnabled(hideable);
        }

        /**
         * Synchs name property to value. This is called on change of
         * tableColumn's <code>headerValue</code> property.
         *
         * @param value
         */
        private void updateFromColumnHeader(Object value) {
            setName(String.valueOf(value));
        }

        /**
         * Enforces selected to <code>true</code>. Called if user interaction
         * tried to de-select the last single visible column.
         *
         */
        private void reselect() {
            firePropertyChange("selected", null, Boolean.TRUE);
        }

        // -------------- init
        private void installColumn(TableColumn column) {
            this.column = column;
            column.addPropertyChangeListener(getColumnListener());
            updateFromColumnHeader(column.getHeaderValue());
            // #429-swing: actionCommand must be string
            if (column.getIdentifier() != null) {
                setActionCommand(column.getIdentifier().toString());
            }
            boolean visible = (column instanceof TableColumnExt) ?
                    ((TableColumnExt) column).isVisible() : true;
            updateFromColumnVisible(visible);
        }

        /**
         * Returns the listener to column's property changes. The listener
         * is created lazily if necessary.
         *
         * @return the <code>PropertyChangeListener</code> listening to
         *   <code>TableColumn</code>'s property changes, guaranteed to be
         *   not <code>null</code>.
         */
        protected PropertyChangeListener getColumnListener() {
            if (columnListener == null) {
                columnListener = createPropertyChangeListener();
            }
            return columnListener;
        }

        /**
         * Creates and returns the listener to column's property changes.
         * Subclasses are free to roll their own.
         * <p>
         * Implementation note: this listener reacts to column's
         * <code>visible</code> and <code>headerValue</code> properties and
         * calls the respective <code>updateFromXX</code> methodes.
         *
         * @return the <code>PropertyChangeListener</code> to use with the
         *         column
         */
        protected PropertyChangeListener createPropertyChangeListener() {
            return new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("visible".equals(evt.getPropertyName())) {
                        updateFromColumnVisible((Boolean) evt.getNewValue());
                    } else if ("headerValue".equals(evt.getPropertyName())) {
                        updateFromColumnHeader(evt.getNewValue());
                    } else if ("hideable".equals(evt.getPropertyName())) {
                        updateFromColumnHideable((Boolean) evt.getNewValue());
                    }
                }
            };
        }
    }

    // ---------------------- the popup

    /**
     * A default implementation of ColumnControlPopup.
     * It uses a JPopupMenu with
     * MenuItems corresponding to the Actions as
     * provided by the ColumnControlButton.
     *
     *
     */
    public class DefaultColumnControlPopup implements ColumnControlPopup, ActionGroupable {
        private JPopupMenu popupMenu;
        private ActionGrouper grouper;

        public DefaultColumnControlPopup() {
            this(null);
        }

        //------------------ public methods to control visibility status

        public DefaultColumnControlPopup(ActionGrouper grouper) {
            this.grouper = grouper;
        }

        /**
         * @inheritDoc
         *
         */
        @Override
        public void updateUI() {
            SwingUtilities.updateComponentTreeUI(getPopupMenu());
        }

        /**
         * @inheritDoc
         *
         */
        @Override
        public void toggleVisibility(JComponent owner) {
            JPopupMenu popupMenu = getPopupMenu();
            if (popupMenu.isVisible()) {
                popupMenu.setVisible(false);
            } else if (popupMenu.getComponentCount() > 0) {
                Dimension buttonSize = owner.getSize();
                int xPos = owner.getComponentOrientation().isLeftToRight() ? buttonSize.width
                        - popupMenu.getPreferredSize().width
                        : 0;
                popupMenu.show(owner,
                        // JW: trying to allow popup without CCB showing
                        // weird behaviour
//                        owner.isShowing()? owner : null,
                        xPos, buttonSize.height);
            }

        }

        /**
         * @inheritDoc
         *
         */
        @Override
        public void applyComponentOrientation(ComponentOrientation o) {
            getPopupMenu().applyComponentOrientation(o);

        }

        //-------------------- public methods to manipulate popup contents.

        /**
         * @inheritDoc
         *
         */
        @Override
        public void removeAll() {
            getPopupMenu().removeAll();
        }

        /**
         * @inheritDoc
         *
         */
        @Override
        public void addVisibilityActionItems(
                List<? extends AbstractActionExt> actions) {
            addItems(new ArrayList<Action>(actions));

        }

        /**
         * @inheritDoc
         *
         */
        @Override
        public void addAdditionalActionItems(List<? extends Action> actions) {
            if (actions.size() == 0)
                return;
            // JW: this is a reference to the enclosing class
            // prevents to make this implementation static
            // Hmmm...any way around?
            if (canControl()) {
                addSeparator();
            }

            if (getGrouper() == null) {
                addItems(actions);
                return;
            }
            List<? extends List<? extends Action>> groups = grouper.group(actions);
            for (List<? extends Action> group : groups) {
                addItems(group);
                if (group != groups.get(groups.size()- 1))
                    addSeparator();

            }

        }

        //--------------------------- internal helpers to manipulate popups content

        /**
         * Here: creates and adds a menuItem to the popup for every
         * Action in the list. Does nothing if
         * if the list is empty.
         *
         * PRE: actions != null.
         *
         * @param actions a list containing the actions to add to the popup.
         *        Must not be null.
         *
         */
        protected void addItems(List<? extends Action> actions) {
            ActionContainerFactory factory = new ActionContainerFactory(null);
            for (Action action : actions) {
                addItem(factory.createMenuItem(action));
            }
        }

        /**
         * adds a separator to the popup.
         *
         */
        protected void addSeparator() {
            getPopupMenu().addSeparator();
        }

        /**
         *
         * @param item the menuItem to add to the popup.
         */
        protected void addItem(JMenuItem item) {
            getPopupMenu().add(item);
        }

        /**
         *
         * @return the popup to add menuitems. Guaranteed to be != null.
         */
        protected JPopupMenu getPopupMenu() {
            if (popupMenu == null) {
                popupMenu = new JPopupMenu();
            }
            return popupMenu;
        }

        // --------------- implement Groupable

        @Override
        public void setActionGrouper(ActionGrouper grouper) {
            this.grouper = grouper;
        }

        protected ActionGrouper getGrouper() {
            return grouper;
        }

    }
    /**
     * Returns to popup component for user interaction. Lazily
     * creates the component if necessary.
     *
     * @return the ColumnControlPopup for showing the items, guaranteed
     *   to be not <code>null</code>.
     * @see #createColumnControlPopup()
     */
    protected ColumnControlPopup getColumnControlPopup() {
        if (popup == null) {
            popup = createColumnControlPopup();
        }
        return popup;
    }

    /**
     * Factory method to return a <code>ColumnControlPopup</code>.
     * Subclasses can override to hook custom implementations.
     *
     * @return the <code>ColumnControlPopup</code> used.
     */
    protected ColumnControlPopup createColumnControlPopup() {
        return new DefaultColumnControlPopup();
    }

//-------------------------- updates from table propertyChangelistnere

    /**
     * Adjusts internal state after table's column model property has changed.
     * Handles cleanup of listeners to the old/new columnModel (Note, that
     * it listens to the column model only if it can control column visibility).
     * Updates content of popup.
     *
     * @param oldModel the old <code>TableColumnModel</code> we had been listening to.
     */
    protected void updateFromColumnModelChange(TableColumnModel oldModel) {
        if (oldModel != null) {
            oldModel.removeColumnModelListener(columnModelListener);
        }
        populatePopup();
        if (canControl()) {
            table.getColumnModel().addColumnModelListener(getColumnModelListener());
        }
    }

    /**
     * Synchs this button's enabled with table's enabled.
     *
     */
    protected void updateFromTableEnabledChanged() {
        getAction().setEnabled(table.isEnabled());

    }
    /**
     * Method to check if we can control column visibility POST: if true we can
     * be sure to have an extended TableColumnModel
     *
     * @return boolean to indicate if controlling the visibility state is
     *   possible.
     */
    protected boolean canControl() {
        return table.getColumnModel() instanceof TableColumnModelExt;
    }

//  ------------------------ updating the popup
    /**
     * Populates the popup from scratch.
     *
     * If applicable, creates and adds column visibility actions. Always adds
     * additional actions.
     */
    protected void populatePopup() {
        clearAll();
        if (canControl()) {
            createVisibilityActions();
            addVisibilityActionItems();
        }
        addAdditionalActionItems();
    }

    /**
     *
     * removes all components from the popup, making sure to release all
     * columnVisibility actions.
     *
     */
    protected void clearAll() {
        clearColumnVisibilityActions();
        getColumnControlPopup().removeAll();
    }

    /**
     * Releases actions and clears list of actions.
     *
     */
    protected void clearColumnVisibilityActions() {
        if (columnVisibilityActions == null)
            return;
        for (ColumnVisibilityAction action : columnVisibilityActions) {
            action.releaseColumn();
        }
        columnVisibilityActions.clear();
    }

    /**
     * Adds visibility actions into the popup view.
     *
     * Here: delegates the list of actions to the DefaultColumnControlPopup.
     * <p>
     * PRE: columnVisibilityActions populated before calling this.
     *
     */
    protected void addVisibilityActionItems() {
        getColumnControlPopup().addVisibilityActionItems(
                Collections.unmodifiableList(getColumnVisibilityActions()));
    }

    /**
     * Adds additional actions to the popup, if additionalActionsVisible is true,
     * does nothing otherwise.<p>
     *
     * Here: delegates the list of actions as returned by #getAdditionalActions()
     *   to the DefaultColumnControlPopup.
     * Does nothing if #getColumnActions() is empty.
     *
     */
    protected void addAdditionalActionItems() {
        if (!getAdditionalActionsVisible()) return;
        getColumnControlPopup().addAdditionalActionItems(
                Collections.unmodifiableList(getAdditionalActions()));
    }

    /**
     * Creates and adds a ColumnVisiblityAction for every column that should be
     * togglable via the column control. <p>
     *
     * Here: all table columns contained in the <code>TableColumnModel</code> -
     * visible and invisible columns - to <code>createColumnVisibilityAction</code> and
     * adds all not <code>null</code> return values.
     *
     * <p>
     * PRE: canControl()
     *
     * @see #createColumnVisibilityAction
     */
    protected void createVisibilityActions() {
        List<TableColumn> columns = table.getColumns(true);
        for (TableColumn column : columns) {
            ColumnVisibilityAction action = createColumnVisibilityAction(column);
            if (action != null) {
                getColumnVisibilityActions().add(action);
            }
        }

    }

    /**
     * Creates and returns a <code>ColumnVisibilityAction</code> for the given
     * <code>TableColumn</code>. The return value might be null, f.i. if the
     * column should not be allowed to be toggled.
     *
     * @param column the <code>TableColumn</code> to use for the action
     * @return a ColumnVisibilityAction to use for the given column,
     *    may be <code>null</code>.
     */
    protected ColumnVisibilityAction createColumnVisibilityAction(TableColumn column) {
        return new ColumnVisibilityAction(column);
    }

    /**
     * Lazyly creates and returns the List of visibility actions.
     *
     * @return the list of visibility actions, guaranteed to be != null.
     */
    protected List<ColumnVisibilityAction> getColumnVisibilityActions() {
        if (columnVisibilityActions == null) {
            columnVisibilityActions = new ArrayList<ColumnVisibilityAction>();
        }
        return columnVisibilityActions;
    }

    /**
     * creates and returns a list of additional Actions to add to the popup.
     * Here: the actions are looked up in the table's actionMap according
     * to the keys as returned from #getColumnControlActionKeys();
     *
     * @return a list containing all additional actions to include into the popup.
     */
    protected List<Action> getAdditionalActions() {
        List<?> actionKeys = getColumnControlActionKeys();
        List<Action> actions = new ArrayList<Action>();
        for (Object key : actionKeys) {
          actions.add(table.getActionMap().get(key));
        }
        return actions;
    }

    /**
     * Looks up and returns action keys to access actions in the
     * table's actionMap which should be included into the popup.
     *
     * Here: all keys with isColumnControlActionKey(key). The list
     * is sorted by those keys.
     *
     * @return the action keys of table's actionMap entries whose
     *   action should be included into the popup.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List getColumnControlActionKeys() {
        Object[] allKeys = table.getActionMap().allKeys();
        List columnKeys = new ArrayList();
        for (int i = 0; i < allKeys.length; i++) {
            if (isColumnControlActionKey(allKeys[i])) {
                columnKeys.add(allKeys[i]);
            }
        }
        // JW: this will blow for non-String keys!
        // so this method is less decoupled from the
        // decision method isControl than expected.
        Collections.sort(columnKeys);
        return columnKeys;
    }

    /**
     * Here: true if a String key starts with #COLUMN_CONTROL_MARKER.
     *
     * @param actionKey a key in the table's actionMap.
     * @return a boolean to indicate whether the given actionKey maps to
     *    an action which should be included into the popup.
     *
     */
    protected boolean isColumnControlActionKey(Object actionKey) {
        return (actionKey instanceof String) &&
            ((String) actionKey).startsWith(COLUMN_CONTROL_MARKER);
    }

    //--------------------------- init

    private void installTable(JXTable table) {
        this.table = table;
        table.addPropertyChangeListener(getTablePropertyChangeListener());
        updateFromColumnModelChange(null);
        updateFromTableEnabledChanged();
    }

    /**
     * Initialize the column control button's gui
     */
    private void init() {
        setFocusPainted(false);
        setFocusable(false);
        // this is a trick to get hold of the client prop which
        // prevents closing of the popup
        JComboBox box = new JComboBox();
        Object preventHide = box.getClientProperty("doNotCancelPopup");
        putClientProperty("doNotCancelPopup", preventHide);
        additionalActionsVisible = true;
    }

    /**
     * Creates and returns the default action for this button.
     * @param icon
     *
     * @param icon the Icon to use in the action.
     * @return the default action.
     */
    private Action createControlAction(Icon icon) {

        Action control = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                togglePopup();
            }

        };
        control.putValue(Action.SMALL_ICON, icon);
        return control;
    }

    // -------------------------------- listeners

    /**
     * Returns the listener to table's property changes. The listener is
     * lazily created if necessary.
     * @return the <code>PropertyChangeListener</code> for use with the
     *  table, guaranteed to be not <code>null</code>.
     */
    protected PropertyChangeListener getTablePropertyChangeListener() {
        if (tablePropertyChangeListener == null) {
            tablePropertyChangeListener = createTablePropertyChangeListener();
        }
        return tablePropertyChangeListener;
    }

    /**
     * Creates the listener to table's property changes. Subclasses are free
     * to roll their own. <p>
     * Implementation note: this listener reacts to table's <code>enabled</code> and
     * <code>columnModel</code> properties and calls the respective
     * <code>updateFromXX</code> methodes.
     *
     * @return the <code>PropertyChangeListener</code> for use with the table.
     */
    protected PropertyChangeListener createTablePropertyChangeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("columnModel".equals(evt.getPropertyName())) {
                    updateFromColumnModelChange((TableColumnModel) evt
                            .getOldValue());
                } else if ("enabled".equals(evt.getPropertyName())) {
                    updateFromTableEnabledChanged();
                }
            }
        };
    }

    /**
     * Returns the listener to table's column model. The listener is
     * lazily created if necessary.
     * @return the <code>TableColumnModelListener</code> for use with the
     *  table's column model, guaranteed to be not <code>null</code>.
     */
    protected TableColumnModelListener getColumnModelListener() {
        if (columnModelListener == null) {
            columnModelListener = createColumnModelListener();
        }
        return columnModelListener;
    }

    /**
     * Creates the listener to columnModel. Subclasses are free to roll their
     * own.
     * <p>
     * Implementation note: this listener reacts to "real" columnRemoved/-Added by
     * populating the popups content from scratch.
     *
     * @return the <code>TableColumnModelListener</code> for use with the
     *         table's columnModel.
     */
    protected TableColumnModelListener createColumnModelListener() {
        return new TableColumnModelListener() {
            /** Tells listeners that a column was added to the model. */
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                // quickfix for #192
                if (!isVisibilityChange(e, true)) {
                    populatePopup();
                }
            }

            /** Tells listeners that a column was removed from the model. */
            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                if (!isVisibilityChange(e, false)) {
                    populatePopup();
                }
            }

            /**
             * Check if the add/remove event is triggered by a move to/from the
             * invisible columns.
             *
             * PRE: the event must be received in columnAdded/Removed.
             *
             * @param e the received event
             * @param added if true the event is assumed to be received via
             *        columnAdded, otherwise via columnRemoved.
             * @return boolean indicating whether the removed/added is a side-effect
             *    of hiding/showing the column.
             */
            private boolean isVisibilityChange(TableColumnModelEvent e,
                    boolean added) {
                // can't tell
                if (!(e.getSource() instanceof DefaultTableColumnModelExt))
                    return false;
                DefaultTableColumnModelExt model = (DefaultTableColumnModelExt) e
                        .getSource();
                if (added) {
                    return model.isAddedFromInvisibleEvent(e.getToIndex());
                } else {
                    return model.isRemovedToInvisibleEvent(e.getFromIndex());
                }
            }

            /** Tells listeners that a column was repositioned. */
            @Override
            public void columnMoved(TableColumnModelEvent e) {
            }

            /** Tells listeners that a column was moved due to a margin change. */
            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            /**
             * Tells listeners that the selection model of the TableColumnModel
             * changed.
             */
            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        };
    }

} // end class ColumnControlButton
