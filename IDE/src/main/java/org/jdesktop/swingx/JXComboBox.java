/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.ComboPopup;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.JRendererPanel;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.jdesktop.swingx.sort.StringValueRegistry;
import org.jdesktop.swingx.util.Contract;

/**
 * An enhanced {@code JComboBox} that provides the following additional functionality:
 * <p>
 * Auto-starts edits correctly for AutoCompletion when inside a {@code JTable}. A normal {@code
 * JComboBox} fails to recognize the first key stroke when it has been
 * {@link org.jdesktop.swingx.autocomplete.AutoCompleteDecorator#decorate(JComboBox) decorated}.
 * <p>
 * Adds highlighting support.
 *
 * @author Karl Schaefer
 * @author Jeanette Winzenburg
 */
@SuppressWarnings({"nls", "serial"})
public class JXComboBox extends JComboBox {
    /**
     * A decorator for the original ListCellRenderer. Needed to hook highlighters
     * after messaging the delegate.<p>
     */
    public class DelegatingRenderer implements ListCellRenderer, RolloverRenderer, UIDependent {
        /** the delegate. */
        private ListCellRenderer delegateRenderer;
        private JRendererPanel wrapper;

        /**
         * Instantiates a DelegatingRenderer with combo box's default renderer as delegate.
         */
        public DelegatingRenderer() {
            this(null);
        }

        /**
         * Instantiates a DelegatingRenderer with the given delegate. If the
         * delegate is {@code null}, the default is created via the combo box's factory method.
         *
         * @param delegate the delegate to use, if {@code null} the combo box's default is
         *   created and used.
         */
        public DelegatingRenderer(ListCellRenderer delegate) {
            wrapper = new JRendererPanel(new BorderLayout());
            setDelegateRenderer(delegate);
        }

        /**
         * Sets the delegate. If the delegate is {@code null}, the default is created via the combo
         * box's factory method.
         *
         * @param delegate
         *            the delegate to use, if null the list's default is created and used.
         */
        public void setDelegateRenderer(ListCellRenderer delegate) {
            if (delegate == null) {
                delegate = createDefaultCellRenderer();
            }
            delegateRenderer = delegate;
        }

        /**
         * Returns the delegate.
         *
         * @return the delegate renderer used by this renderer, guaranteed to
         *   not-null.
         */
        public ListCellRenderer getDelegateRenderer() {
            return delegateRenderer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void updateUI() {
             wrapper.updateUI();

             if (delegateRenderer instanceof UIDependent) {
                 ((UIDependent) delegateRenderer).updateUI();
             } else if (delegateRenderer instanceof Component) {
                 SwingUtilities.updateComponentTreeUI((Component) delegateRenderer);
             } else if (delegateRenderer != null) {
                 try {
                     Component comp = delegateRenderer.getListCellRendererComponent(
                             getPopupListFor(JXComboBox.this), null, -1, false, false);
                     SwingUtilities.updateComponentTreeUI(comp);
                 } catch (Exception e) {
                     // nothing to do - renderer barked on off-range row
                 }
             }
         }

         // --------- implement ListCellRenderer
        /**
         * {@inheritDoc} <p>
         *
         * Overridden to apply the highlighters, if any, after calling the delegate.
         * The decorators are not applied if the row is invalid.
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Component comp = null;

            if (index == -1) {
                comp = delegateRenderer.getListCellRendererComponent(list, value,
                        getSelectedIndex(), isSelected, cellHasFocus);

                if (isUseHighlightersForCurrentValue() && compoundHighlighter != null && getSelectedIndex() != -1) {
                    comp = compoundHighlighter.highlight(comp, getComponentAdapter(getSelectedIndex()));

                    // this is done to "trick" BasicComboBoxUI.paintCurrentValue which resets all of
                    // the painted information after asking the list to render the value. the panel
                    // wrappers receives all of the post-rendering configuration, which is dutifully
                    // ignored by the real rendering component
                    wrapper.add(comp);
                    comp = wrapper;
                }
            } else {
                comp = delegateRenderer.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);

                if ((compoundHighlighter != null) && (index >= 0) && (index < getItemCount())) {
                    comp = compoundHighlighter.highlight(comp, getComponentAdapter(index));
                }
            }

            return comp;
        }

        // implement RolloverRenderer

        /**
         * {@inheritDoc}
         *
         */
        @Override
        public boolean isEnabled() {
            return (delegateRenderer instanceof RolloverRenderer) &&
               ((RolloverRenderer) delegateRenderer).isEnabled();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doClick() {
            if (isEnabled()) {
                ((RolloverRenderer) delegateRenderer).doClick();
            }
        }
    }

    @SuppressWarnings("hiding")
    protected static class ComboBoxAdapter extends ComponentAdapter {
        private final JXComboBox comboBox;

        /**
         * Constructs a <code>ListAdapter</code> for the specified target
         * JXList.
         *
         * @param component  the target list.
         */
        public ComboBoxAdapter(JXComboBox component) {
            super(component);
            comboBox = component;
        }

        /**
         * Typesafe accessor for the target component.
         *
         * @return the target component as a {@link org.jdesktop.swingx.JXComboBox}
         */
        public JXComboBox getComboBox() {
            return comboBox;
        }

        /**
         * A safe way to access the combo box's popup visibility.
         *
         * @return {@code true} if the popup is visible; {@code false} otherwise
         */
        protected boolean isPopupVisible() {
            if (comboBox.updatingUI) {
                return false;
            }

            return comboBox.isPopupVisible();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasFocus() {
            if (isPopupVisible()) {
                JList list = getPopupListFor(comboBox);

                return list != null && list.isFocusOwner() && (row == list.getLeadSelectionIndex());
            }

            return comboBox.isFocusOwner();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRowCount() {
            return comboBox.getModel().getSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(int row, int column) {
            return comboBox.getModel().getElementAt(row);
        }

        /**
         * {@inheritDoc}
         * This is implemented to query the table's StringValueRegistry for an appropriate
         * StringValue and use that for getting the string representation.
         */
        @Override
        public String getStringAt(int row, int column) {
            StringValue sv = comboBox.getStringValueRegistry().getStringValue(row, column);

            return sv.getString(getValueAt(row, column));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Rectangle getCellBounds() {
            JList list = getPopupListFor(comboBox);

            if (list == null) {
                assert false;
                return new Rectangle(comboBox.getSize());
            }

            return list.getCellBounds(row, row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return row == -1 && comboBox.isEditable();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEditable() {
            return isCellEditable(row, column);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {
            if (isPopupVisible()) {
                JList list = getPopupListFor(comboBox);

                return list != null && row == list.getLeadSelectionIndex();
            }

            return comboBox.isFocusOwner();
        }
    }

    class StringValueKeySelectionManager implements KeySelectionManager, Serializable, UIDependent {
        private long timeFactor;
        private long lastTime = 0L;
        private String prefix = "";
        private String typedString = "";

        public StringValueKeySelectionManager() {
            updateUI();
        }

        @Override
        public int selectionForKey(char aKey, ComboBoxModel aModel) {
            if (lastTime == 0L) {
                prefix = "";
                typedString = "";
            }

            int startIndex = getSelectedIndex();

            if (EventQueue.getMostRecentEventTime() - lastTime < timeFactor) {
                typedString += aKey;
                if ((prefix.length() == 1) && (aKey == prefix.charAt(0))) {
                    // Subsequent same key presses move the keyboard focus to the next
                    // object that starts with the same letter.
                    startIndex++;
                } else {
                    prefix = typedString;
                }
            } else {
                startIndex++;
                typedString = "" + aKey;
                prefix = typedString;
            }

            lastTime = EventQueue.getMostRecentEventTime();

            if (startIndex < 0 || startIndex >= aModel.getSize()) {
                startIndex = 0;
            }

            for (int i = startIndex, c = aModel.getSize(); i < c; i++) {
                String v = getStringAt(i).toLowerCase();

                if (v.length() > 0 && v.charAt(0) == aKey) {
                    return i;
                }
            }

            for (int i = startIndex, c = aModel.getSize(); i < c; i++) {
                String v = getStringAt(i).toLowerCase();

                if (v.length() > 0 && v.charAt(0) == aKey) {
                    return i;
                }
            }

            for (int i = 0; i < startIndex; i++) {
                String v = getStringAt(i).toLowerCase();

                if (v.length() > 0 && v.charAt(0) == aKey) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public void updateUI() {
            Long l = (Long) UIManager.get("ComboBox.timeFactor");
            timeFactor = l == null ? 1000L : l.longValue();
        }
    }

    private ComboBoxAdapter dataAdapter;

    private DelegatingRenderer delegatingRenderer;

    private StringValueRegistry stringValueRegistry;

    private boolean useHighlightersForCurrentValue = true;

    private CompoundHighlighter compoundHighlighter;

    private ChangeListener highlighterChangeListener;

    private List<KeyEvent> pendingEvents;

    private boolean isDispatching;

    private boolean updatingUI;

    /**
     * Creates a <code>JXComboBox</code> with a default data model. The default data model is an
     * empty list of objects. Use <code>addItem</code> to add items. By default the first item in
     * the data model becomes selected.
     *
     * @see DefaultComboBoxModel
     */
    public JXComboBox() {
        super();
        init();
    }

    /**
     * Creates a <code>JXComboBox</code> that takes its items from an existing
     * <code>ComboBoxModel</code>. Since the <code>ComboBoxModel</code> is provided, a combo box
     * created using this constructor does not create a default combo box model and may impact how
     * the insert, remove and add methods behave.
     *
     * @param model
     *            the <code>ComboBoxModel</code> that provides the displayed list of items
     * @see DefaultComboBoxModel
     */
    public JXComboBox(ComboBoxModel model) {
        super(model);
        init();
    }

    /**
     * Creates a <code>JXComboBox</code> that contains the elements in the specified array. By
     * default the first item in the array (and therefore the data model) becomes selected.
     *
     * @param items
     *            an array of objects to insert into the combo box
     * @see DefaultComboBoxModel
     */
    public JXComboBox(Object[] items) {
        super(items);
        init();
    }

    /**
     * Creates a <code>JXComboBox</code> that contains the elements in the specified Vector. By
     * default the first item in the vector (and therefore the data model) becomes selected.
     *
     * @param items
     *            an array of vectors to insert into the combo box
     * @see DefaultComboBoxModel
     */
    public JXComboBox(Vector<?> items) {
        super(items);
        init();
    }

    private void init() {
        pendingEvents = new ArrayList<KeyEvent>();

        if (keySelectionManager == null || keySelectionManager instanceof UIResource) {
            setKeySelectionManager(createDefaultKeySelectionManager());
        }
    }

    protected static JList getPopupListFor(JComboBox comboBox) {
        int count = comboBox.getUI().getAccessibleChildrenCount(comboBox);

        for (int i = 0; i < count; i++) {
            Accessible a = comboBox.getUI().getAccessibleChild(comboBox, i);

            if (a instanceof ComboPopup) {
                return ((ComboPopup) a).getList();
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses the {@code StringValue} representation of the elements to determine
     * the selected item.
     */
    @Override
    protected KeySelectionManager createDefaultKeySelectionManager() {
        return new StringValueKeySelectionManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, final KeyEvent e, int condition,
            boolean pressed) {
        boolean retValue = super.processKeyBinding(ks, e, condition, pressed);

        if (!retValue && editor != null) {
            if (isStartingCellEdit(e)) {
                pendingEvents.add(e);
            } else if (pendingEvents.size() == 2) {
                pendingEvents.add(e);
                isDispatching = true;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (KeyEvent event : pendingEvents) {
                                editor.getEditorComponent().dispatchEvent(event);
                            }

                            pendingEvents.clear();
                        } finally {
                            isDispatching = false;
                        }
                    }
                });
            }
        }
        return retValue;
    }

    private boolean isStartingCellEdit(KeyEvent e) {
        if (isDispatching) {
            return false;
        }

        JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, this);
        boolean isOwned = table != null
                && !Boolean.FALSE.equals(table.getClientProperty("JTable.autoStartsEdit"));

        return isOwned && e.getComponent() == table;
    }

    /**
     * @return the unconfigured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter() {
        if (dataAdapter == null) {
            dataAdapter = new ComboBoxAdapter(this);
        }
        return dataAdapter;
    }

    /**
     * Convenience to access a configured ComponentAdapter.
     * Note: the column index of the configured adapter is always 0.
     *
     * @param index the row index in view coordinates, must be valid.
     * @return the configured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter(int index) {
        ComponentAdapter adapter = getComponentAdapter();
        adapter.column = 0;
        adapter.row = index;
        return adapter;
    }

    /**
     * Returns the StringValueRegistry which defines the string representation for
     * each cells. This is strictly for internal use by the table, which has the
     * responsibility to keep in synch with registered renderers.<p>
     *
     * Currently exposed for testing reasons, client code is recommended to not use nor override.
     *
     * @return the current string value registry
     */
    protected StringValueRegistry getStringValueRegistry() {
        if (stringValueRegistry == null) {
            stringValueRegistry = createDefaultStringValueRegistry();
        }
        return stringValueRegistry;
    }

    /**
     * Creates and returns the default registry for StringValues.<p>
     *
     * @return the default registry for StringValues.
     */
    protected StringValueRegistry createDefaultStringValueRegistry() {
        return new StringValueRegistry();
    }

    /**
     * Returns the string representation of the cell value at the given position.
     *
     * @param row the row index of the cell in view coordinates
     * @return the string representation of the cell value as it will appear in the
     *   table.
     */
    public String getStringAt(int row) {
        // changed implementation to use StringValueRegistry
        StringValue stringValue = getStringValueRegistry().getStringValue(row, 0);

        return stringValue.getString(getItemAt(row));
    }

    private DelegatingRenderer getDelegatingRenderer() {
        if (delegatingRenderer == null) {
            // only called once... to get hold of the default?
            delegatingRenderer = new DelegatingRenderer();
        }
        return delegatingRenderer;
    }

    /**
     * Creates and returns the default cell renderer to use. Subclasses
     * may override to use a different type. Here: returns a <code>DefaultListRenderer</code>.
     *
     * @return the default cell renderer to use with this list.
     */
    protected ListCellRenderer createDefaultCellRenderer() {
        return new DefaultListRenderer();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to return the delegating renderer which is wrapped around the
     * original to support highlighting. The returned renderer is of type
     * DelegatingRenderer and guaranteed to not-null<p>
     *
     * @see #setRenderer(ListCellRenderer)
     * @see DelegatingRenderer
     */
    @Override
    public ListCellRenderer getRenderer() {
        // PENDING JW: something wrong here - why exactly can't we return super?
        // not even if we force the initial setting in init?
//        return super.getCellRenderer();
        return getDelegatingRenderer();
    }

    /**
     * Returns the renderer installed by client code or the default if none has
     * been set.
     *
     * @return the wrapped renderer.
     * @see #setRenderer(ListCellRenderer)
     */
    public ListCellRenderer getWrappedRenderer() {
        return getDelegatingRenderer().getDelegateRenderer();
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to wrap the given renderer in a DelegatingRenderer to support
     * highlighting. <p>
     *
     * Note: the wrapping implies that the renderer returned from the getCellRenderer
     * is <b>not</b> the renderer as given here, but the wrapper. To access the original,
     * use <code>getWrappedCellRenderer</code>.
     *
     * @see #getWrappedRenderer()
     * @see #getRenderer()
     */
    @Override
    public void setRenderer(ListCellRenderer renderer) {
        // PENDING: do something against recursive setting
        // == multiple delegation...
        ListCellRenderer oldValue = super.getRenderer();
        getDelegatingRenderer().setDelegateRenderer(renderer);
        getStringValueRegistry().setStringValue(
                renderer instanceof StringValue ? (StringValue) renderer : null, 0);
        super.setRenderer(delegatingRenderer);

        if (oldValue == delegatingRenderer) {
            firePropertyChange("renderer", null, delegatingRenderer);
        }
    }

    /**
     * PENDING JW to KS: review method naming - doesn't sound like valid English to me (no
     * native speaker of course :-). Options are to
     * change the property name to usingHighlightersForCurrentValue (as we did in JXMonthView
     * after some debate) or stick to getXX. Thinking about it: maybe then the property should be
     * usesHighlightersXX, that is third person singular instead of imperative,
     * like in tracksVerticalViewport of JTable?
     *
     * @return {@code true} if the combo box decorates the current value with highlighters; {@code false} otherwise
     */
    public boolean isUseHighlightersForCurrentValue() {
        return useHighlightersForCurrentValue;
    }

    public void setUseHighlightersForCurrentValue(boolean useHighlightersForCurrentValue) {
        boolean oldValue = isUseHighlightersForCurrentValue();
        this.useHighlightersForCurrentValue = useHighlightersForCurrentValue;
        repaint();
        firePropertyChange("useHighlightersForCurrentValue", oldValue,
                isUseHighlightersForCurrentValue());
    }

    /**
     * Returns the CompoundHighlighter assigned to the table, null if none. PENDING: open up for
     * subclasses again?.
     *
     * @return the CompoundHighlighter assigned to the table.
     * @see #setCompoundHighlighter(CompoundHighlighter)
     */
    private CompoundHighlighter getCompoundHighlighter() {
        return compoundHighlighter;
    }

    /**
     * Assigns a CompoundHighlighter to the table, maybe null to remove all Highlighters.
     * <p>
     *
     * The default value is <code>null</code>.
     * <p>
     *
     * PENDING: open up for subclasses again?.
     *
     * @param pipeline
     *            the CompoundHighlighter to use for renderer decoration.
     * @see #getCompoundHighlighter()
     * @see #addHighlighter(Highlighter)
     * @see #removeHighlighter(Highlighter)
     *
     */
    private void setCompoundHighlighter(CompoundHighlighter pipeline) {
        CompoundHighlighter old = getCompoundHighlighter();
        if (old != null) {
            old.removeChangeListener(getHighlighterChangeListener());
        }
        compoundHighlighter = pipeline;
        if (compoundHighlighter != null) {
            compoundHighlighter.addChangeListener(getHighlighterChangeListener());
        }
        // PENDING: wrong event - the property is either "compoundHighlighter"
        // or "highlighters" with the old/new array as value
        firePropertyChange("highlighters", old, getCompoundHighlighter());
    }

    /**
     * Sets the <code>Highlighter</code>s to the column, replacing any old settings. None of the
     * given Highlighters must be null.
     * <p>
     *
     * @param highlighters
     *            zero or more not null highlighters to use for renderer decoration.
     *
     * @see #getHighlighters()
     * @see #addHighlighter(Highlighter)
     * @see #removeHighlighter(Highlighter)
     *
     */
    public void setHighlighters(Highlighter... highlighters) {
        Contract.asNotNull(highlighters, "highlighters cannot be null or contain null");

        CompoundHighlighter pipeline = null;
        if (highlighters.length > 0) {
            pipeline = new CompoundHighlighter(highlighters);
        }

        setCompoundHighlighter(pipeline);
    }

    /**
     * Returns the <code>Highlighter</code>s used by this column. Maybe empty, but guarantees to be
     * never null.
     *
     * @return the Highlighters used by this column, guaranteed to never null.
     * @see #setHighlighters(Highlighter[])
     */
    public Highlighter[] getHighlighters() {
        return getCompoundHighlighter() != null ? getCompoundHighlighter().getHighlighters()
                : CompoundHighlighter.EMPTY_HIGHLIGHTERS;
    }

    /**
     * Adds a Highlighter. Appends to the end of the list of used Highlighters.
     * <p>
     *
     * @param highlighter
     *            the <code>Highlighter</code> to add.
     * @throws NullPointerException
     *             if <code>Highlighter</code> is null.
     *
     * @see #removeHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter[])
     */
    public void addHighlighter(Highlighter highlighter) {
        CompoundHighlighter pipeline = getCompoundHighlighter();
        if (pipeline == null) {
            setCompoundHighlighter(new CompoundHighlighter(highlighter));
        } else {
            pipeline.addHighlighter(highlighter);
        }
    }

    /**
     * Removes the given Highlighter.
     * <p>
     *
     * Does nothing if the Highlighter is not contained.
     *
     * @param highlighter
     *            the Highlighter to remove.
     * @see #addHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter...)
     */
    public void removeHighlighter(Highlighter highlighter) {
        if ((getCompoundHighlighter() == null)) {
            return;
        }
        getCompoundHighlighter().removeHighlighter(highlighter);
    }

    /**
     * Returns the <code>ChangeListener</code> to use with highlighters. Lazily creates the
     * listener.
     *
     * @return the ChangeListener for observing changes of highlighters, guaranteed to be
     *         <code>not-null</code>
     */
    protected ChangeListener getHighlighterChangeListener() {
        if (highlighterChangeListener == null) {
            highlighterChangeListener = createHighlighterChangeListener();
        }

        return highlighterChangeListener;
    }

    /**
     * Creates and returns the ChangeListener observing Highlighters.
     * <p>
     * A property change event is create for a state change.
     *
     * @return the ChangeListener defining the reaction to changes of highlighters.
     */
    protected ChangeListener createHighlighterChangeListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // need to fire change so JXComboBox can update
                firePropertyChange("highlighters", null, getHighlighters());
                repaint();
            }
        };
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to update renderer and highlighters.
     */
    @Override
    public void updateUI() {
        updatingUI = true;

        try {
            super.updateUI();

            if (keySelectionManager instanceof UIDependent) {
                ((UIDependent) keySelectionManager).updateUI();
            }

            ListCellRenderer renderer = getRenderer();

            if (renderer instanceof UIDependent) {
                ((UIDependent) renderer).updateUI();
            } else if (renderer instanceof Component) {
                SwingUtilities.updateComponentTreeUI((Component) renderer);
            }

            if (compoundHighlighter != null) {
                compoundHighlighter.updateUI();
            }
        } finally {
            updatingUI = false;
        }
    }
}
