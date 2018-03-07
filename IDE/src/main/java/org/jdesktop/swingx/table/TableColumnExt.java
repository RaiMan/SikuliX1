/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.table;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.renderer.AbstractRenderer;

/**
 * <code>TableColumn</code> extension for enhanced view column configuration.
 * The general drift is to strengthen the TableColumn abstraction as <b>the</b>
 * place to configure and dynamically update view column properties, covering a
 * broad range of customization requirements. Using collaborators are expected
 * to listen to property changes and update themselves accordingly.
 * <p>
 *
 * A functionality enhancement is the notion of column visibility:
 * <code>TableColumnModelExt</code> manages sets of visible/hidden
 * <code>TableColumnExt</code>s controlled by the columns'
 * <code>visible</code> property. Typically, users can toggle column
 * visibility at runtime, f.i. through a dedicated control in the upper trailing
 * corner of a <code>JScrollPane</code>.
 * <p>
 *
 * A prominent group of properties allows fine-grained, per-column control of
 * corresponding Table/-Header features.
 *
 * <ul>
 * <li><b>Sorting</b>: <code>sortable</code> controls whether this column
 * should be sortable by user's sort gestures; <code>Comparator</code> can
 * hold a column specific type.
 *
 * <li><b>Editing</b>: <code>editable</code> controls whether cells of this
 * column should be accessible to in-table editing.
 *
 * <li><b>Tooltip</b>: <code>toolTipText</code> holds the column tooltip
 * which is shown when hovering over the column's header.
 *
 * <li><b>Highlighter</b>: <code>highlighters</code> holds the column
 * highlighters; these are applied to the renderer after the table highlighters.
 * Any modification of the list of contained <code>Highlighter</code>s
 * (setting them, adding one or removing one) will result in a
 * {@code PropertyChangeEvent} being fired for "highlighters". State changes on
 * contained <code>Highlighter</code>s will result in a PropertyChangeEvent
 * for "highlighterStateChanged".
 * </ul>
 *
 *
 * Analogous to <code>JComponent</code>, this class supports per-instance
 * "client" properties. They are meant as a small-scale extension mechanism.
 * They are similar to regular bean properties in that registered
 * <code>PropertyChangeListener</code>s are notified about changes. TODO:
 * example?
 * <p>
 *
 * A <code>TableColumnExt</code> implements UIDependent, that is it takes over
 * responsibility to update LAF dependent properties of contained elements when
 * messaged with updateUI. This implementation updates its <code>Highlighter</code>s,
 * Cell-/HeaderRenderer and CellEditor. <p>
 *
 * TODO: explain prototype (sizing, collaborator-used-by ColumnFactory (?))
 * <p>
 *
 * @author Ramesh Gupta
 * @author Amy Fowler
 * @author Jeanette Winzenburg
 * @author Karl Schaefer
 *
 * @see TableColumnModelExt
 * @see ColumnFactory
 * @see org.jdesktop.swingx.plaf.UIDependent
 * @see javax.swing.JComponent#putClientProperty
 */
public class TableColumnExt extends TableColumn implements UIDependent {

    /** visible property. Initialized to <code>true</code>.*/
    protected boolean visible = true;
    /** hideable property. Initialized to <code>true</code>.*/
    protected boolean hideable = true;

    /** prototype property. */
    protected Object prototypeValue;

    /** per-column comparator  */
    protected Comparator<?> comparator;
    /** per-column sortable property. Initialized to <code>true</code>. */
    protected boolean sortable = true;
    /** per-column editable property. Initialized to <code>true</code>.*/
    protected boolean editable = true;
    /** per-column tool tip text. */
    private String toolTipText;

    /** storage for client properties. */
    protected Hashtable<Object, Object> clientProperties;

    /**
     * The compound highlighter for the column.
     */
    protected CompoundHighlighter compoundHighlighter;

    private ChangeListener highlighterChangeListener;

    private boolean ignoreHighlighterStateChange;

    /**
     * Creates new table view column with a model index = 0.
     */
    public TableColumnExt() {
        this(0);
    }

    /**
     * Creates new table view column with the specified model index.
     * @param modelIndex index of table model column to which this view column
     *        is bound.
     */
    public TableColumnExt(int modelIndex) {
        this(modelIndex, 75);    // default width taken from javax.swing.table.TableColumn
    }

    /**
     * Creates new table view column with the specified model index and column width.
     * @param modelIndex index of table model column to which this view column
     *        is bound.
     * @param width pixel width of view column
     */
    public TableColumnExt(int modelIndex, int width) {
        this(modelIndex, width, null, null);
    }

    /**
     * Creates new table view column with the specified model index, column
     * width, cell renderer and cell editor.
     * @param modelIndex index of table model column to which this view column
     *        is bound.
     * @param width pixel width of view column
     * @param cellRenderer the cell renderer which will render all cells in this
     *        view column
     * @param cellEditor the cell editor which will edit cells in this view column
     */
    public TableColumnExt(int modelIndex, int width,
                          TableCellRenderer cellRenderer, TableCellEditor cellEditor) {
        super(modelIndex, width, cellRenderer, cellEditor);
    }

    /**
     * Instantiates a new table view column with all properties copied from the
     * given original.
     *
     * @param columnExt the column to copy properties from
     * @see #copyFrom(TableColumnExt)
     */
    public TableColumnExt(TableColumnExt columnExt) {
        this(columnExt.getModelIndex(), columnExt.getWidth(), columnExt
                .getCellRenderer(), columnExt.getCellEditor());
        copyFrom(columnExt);
    }

    /**
     * Sets the <code>Highlighter</code>s to the table, replacing any old settings.
     * None of the given Highlighters must be null.<p>
     *
     * This is a bound property. <p>
     *
     * Note: as of version #1.257 the null constraint is enforced strictly. To remove
     * all highlighters use this method without param.
     *
     * @param highlighters zero or more not null highlighters to use for renderer decoration.
     * @throws NullPointerException if array is null or array contains null values.
     *
     * @see #getHighlighters()
     * @see #addHighlighter(Highlighter)
     * @see #removeHighlighter(Highlighter)
     *
     */
    public void setHighlighters(Highlighter... highlighters) {
        ignoreHighlighterStateChange = true;
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().setHighlighters(highlighters);
        firePropertyChange("highlighters", old, getHighlighters());
        ignoreHighlighterStateChange = false;
    }

    /**
     * Returns the <code>Highlighter</code>s used by this table.
     * Maybe empty, but guarantees to be never null.
     *
     * @return the Highlighters used by this table, guaranteed to never null.
     * @see #setHighlighters(Highlighter[])
     */
    public Highlighter[] getHighlighters() {
        return getCompoundHighlighter().getHighlighters();
    }
    /**
     * Appends a <code>Highlighter</code> to the end of the list of used
     * <code>Highlighter</code>s. The argument must not be null.
     * <p>
     *
     * @param highlighter the <code>Highlighter</code> to add, must not be null.
     * @throws NullPointerException if <code>Highlighter</code> is null.
     *
     * @see #removeHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter[])
     */
    public void addHighlighter(Highlighter highlighter) {
        ignoreHighlighterStateChange = true;
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().addHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
        ignoreHighlighterStateChange = false;
    }

    /**
     * Removes the given Highlighter. <p>
     *
     * Does nothing if the Highlighter is not contained.
     *
     * @param highlighter the Highlighter to remove.
     * @see #addHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter...)
     */
    public void removeHighlighter(Highlighter highlighter) {
        ignoreHighlighterStateChange = true;
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().removeHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
        ignoreHighlighterStateChange = false;
    }

    /**
     * Returns the CompoundHighlighter assigned to the table, null if none.
     * PENDING: open up for subclasses again?.
     *
     * @return the CompoundHighlighter assigned to the table.
     */
    protected CompoundHighlighter getCompoundHighlighter() {
        if (compoundHighlighter == null) {
            compoundHighlighter = new CompoundHighlighter();
            compoundHighlighter.addChangeListener(getHighlighterChangeListener());
        }
        return compoundHighlighter;
    }

    /**
     * Returns the <code>ChangeListener</code> to use with highlighters. Lazily
     * creates the listener.
     *
     * @return the ChangeListener for observing changes of highlighters,
     *   guaranteed to be <code>not-null</code>
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
     * Here: repaints the table on receiving a stateChanged.
     *
     * @return the ChangeListener defining the reaction to changes of
     *         highlighters.
     */
    protected ChangeListener createHighlighterChangeListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (ignoreHighlighterStateChange) return;
                firePropertyChange("highlighterStateChanged", false, true);
            }
        };
    }

    /**
     * Returns true if the user <i>can</i> resize the TableColumn's width,
     * false otherwise. This is a usability override: it takes into account
     * the case where it's principally <i>allowed</i> to resize the column
     * but not possible because the column has fixed size.
     *
     * @return a boolean indicating whether the user can resize this column.
     */
    @Override
    public boolean getResizable() {
        // TODO JW: resizable is a bound property, so to be strict
        // we'll need to override setMin/MaxWidth to fire resizable
        // property change.
        return super.getResizable() && (getMinWidth() < getMaxWidth());
    }

    /**
     * Sets the editable property. This property allows to mark all cells in a
     * column as read-only, independent of the per-cell editability as returned
     * by the <code>TableModel.isCellEditable</code>. If the cell is
     * read-only in the model layer, this property will have no effect.
     *
     * @param editable boolean indicating whether or not the user may edit cell
     *        values in this view column
     * @see #isEditable
     * @see org.jdesktop.swingx.JXTable#isCellEditable(int, int)
     * @see javax.swing.table.TableModel#isCellEditable
     */
    public void setEditable(boolean editable) {
        boolean oldEditable = this.editable;
        this.editable = editable;
        firePropertyChange("editable",
                           Boolean.valueOf(oldEditable),
                           Boolean.valueOf(editable));
    }

    /**
     * Returns the per-column editable property.
     * The default is <code>true</code>.
     *
     * @return boolean indicating whether or not the user may edit cell
     *        values in this view column
     * @see #setEditable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the prototypeValue property.  The value should be of a type
     * which corresponds to the column's class as defined by the table model.
     * If non-null, the JXTable instance will use this property to calculate
     * and set the initial preferredWidth of the column.  Note that this
     * initial preferredWidth will be overridden if the user resizes columns
     * directly.
     *
     * @param value Object containing the value of the prototype to be used
     *         to calculate the initial preferred width of the column
     * @see #getPrototypeValue
     * @see org.jdesktop.swingx.JXTable#getPreferredScrollableViewportSize
     */
    public void setPrototypeValue(Object value) {
        Object oldPrototypeValue = this.prototypeValue;
        this.prototypeValue = value;
        firePropertyChange("prototypeValue",
                           oldPrototypeValue,
                           value);

    }

    /**
     * Returns the prototypeValue property.
     * The default is <code>null</code>.
     *
     * @return Object containing the value of the prototype to be used
     *         to calculate the initial preferred width of the column
     * @see #setPrototypeValue
     */
    public Object getPrototypeValue() {
        return prototypeValue;
    }

    /**
     * Sets the comparator to use for this column.
     * <code>JXTable</code> sorting api respects this property by passing it on
     * to the <code>SortController</code>.
     *
     * @param comparator a custom comparator to use in interactive
     *    sorting.
     * @see #getComparator
     * @see org.jdesktop.swingx.sort.SortController
     * @see org.jdesktop.swingx.decorator.SortKey
     */
    public void setComparator(Comparator<?> comparator) {
        Comparator<?> old = getComparator();
        this.comparator = comparator;
        firePropertyChange("comparator", old, getComparator());
    }

    /**
     * Returns the comparator to use for the column.
     * The default is <code>null</code>.
     *
     * @return <code>Comparator</code> to use for this column
     * @see #setComparator
     */
    public Comparator<?> getComparator() {
        return comparator;
    }

    /**
     * Sets the sortable property. <code>JXTable</code> sorting api respects this
     * property by disabling interactive sorting on this column if false.
     *
     * @param sortable boolean indicating whether or not this column can
     *        be sorted in the table
     * @see #isSortable
     */
    public void setSortable(boolean sortable) {
        boolean old = isSortable();
        this.sortable = sortable;
        firePropertyChange("sortable", old, isSortable());
    }

    /**
     * Returns the sortable property.
     * The default value is <code>true</code>.
     *
     * @return boolean indicating whether this view column is sortable
     * @see #setSortable
     */
    public boolean isSortable() {
        return sortable;
    }

    /**
     * Registers the text to display in the column's tool tip.
     * Typically, this is used by <code>JXTableHeader</code> to
     * display when the mouse cursor lingers over the column's
     * header cell.
     *
     * @param toolTipText text to show.
     * @see #setToolTipText(String)
     */
    public void setToolTipText(String toolTipText) {
        String old = getToolTipText();
        this.toolTipText = toolTipText;
        firePropertyChange("toolTipText", old, getToolTipText());
    }

    /**
     * Returns the text of to display in the column's tool tip.
     * The default is <code>null</code>.
     *
     * @return the text of the column ToolTip.
     * @see #setToolTipText(String)
     */
    public String getToolTipText() {
        return toolTipText;
    }

    /**
     * Sets the title of this view column.  This is a convenience
     * wrapper for <code>setHeaderValue</code>.
     * @param title String containing the title of this view column
     */
    public void setTitle(String title) {
        setHeaderValue(title);                // simple wrapper
    }

    /**
     * Convenience method which returns the headerValue property after
     * converting it to a string.
     * @return String containing the title of this view column or null if
     *   no headerValue is set.
     */
    public String getTitle() {
        Object header = getHeaderValue();
        return header != null ? header.toString() : null; // simple wrapper
    }

    /**
     * Sets the visible property.  This property controls whether or not
     * this view column is currently visible in the table.
     *
     * @param visible boolean indicating whether or not this view column is
     *        visible in the table
     * @see #setVisible
     */
    public void setVisible(boolean visible) {
        boolean oldVisible = isVisible();
        this.visible = visible;
        firePropertyChange("visible", oldVisible, isVisible());
    }

    /**
     * Returns a boolean indicating whether or not this column is visible.
     * The bare property value is constrained by this column's hideable setting,
     * that is a not hideable column is always visible, irrespective of the
     * property setting.
     * <p>
     * The default is <code>true</code>.
     *
     * @return boolean indicating whether or not this view column is
     *        visible in the table
     * @see #setVisible
     */
    public boolean isVisible() {
        if (!isHideable()) return true;
        return visible;
    }

    /**
     * Sets the hideable property. This property controls whether the column can
     * be hidden. This is a bound property. If the column's visibilty is affected,
     * listeners are notified about that change as well..
     * <p>
     *
     * The default value is true.
     *
     * @param hideable
     */
    public void setHideable(boolean hideable) {
        boolean old = isHideable();
        boolean oldVisible = isVisible();
        this.hideable = hideable;
        firePropertyChange("visible", oldVisible, isVisible());
        firePropertyChange("hideable", old, isHideable());
    }

    /**
     * Returns the hideable property.
     *
     * @return the hideable property.
     *
     * @see #setHideable(boolean)
     */
    public boolean isHideable() {
        return hideable;
    }

    /**
     * Sets the client property "key" to <code>value</code>.
     * If <code>value</code> is <code>null</code> this method will remove the property.
     * Changes to
     * client properties are reported with <code>PropertyChange</code> events.
     * The name of the property (for the sake of PropertyChange events) is
     * <code>key.toString()</code>.
     * <p>
     * The <code>get/putClientProperty</code> methods provide access to a
     * per-instance hashtable, which is intended for small scale extensions of
     * TableColumn.
     * <p>
     *
     * @param key Object which is used as key to retrieve value
     * @param value Object containing value of client property
     * @throws IllegalArgumentException if key is <code>null</code>
     * @see #getClientProperty
     * @see javax.swing.JComponent#putClientProperty
     */
    public void putClientProperty(Object key, Object value) {
        if (key == null)
            throw new IllegalArgumentException("null key");

        if ((value == null) && (getClientProperty(key) == null)) {
            return;
        }

        Object old = getClientProperty(key);
        if (value == null) {
            getClientProperties().remove(key);
        }
        else {
            getClientProperties().put(key, value);
        }

        firePropertyChange(key.toString(), old, value);
        /* Make all fireXXX methods in TableColumn protected instead of private */
    }

    /**
     * Returns the value of the property with the specified key. Only properties
     * added with <code>putClientProperty</code> will return a non-<code>null</code>
     * value.
     *
     * @param key Object which is used as key to retrieve value
     * @return Object containing value of client property or <code>null</code>
     *
     * @see #putClientProperty
     */
    public Object getClientProperty(Object key) {
        return ((key == null) || (clientProperties == null)) ?
                null : clientProperties.get(key);
    }

    private Hashtable<Object, Object> getClientProperties() {
        if (clientProperties == null) {
            clientProperties = new Hashtable<Object, Object>();
        }
        return clientProperties;
    }

     /**
      * Copies properties from original. Handles all properties except
      * modelIndex, width, cellRenderer, cellEditor. Called from copy
      * constructor.
      *
      * @param original the tableColumn to copy from
      *
      * @see #TableColumnExt(TableColumnExt)
      */
     protected void copyFrom(TableColumnExt original) {
             setEditable(original.isEditable());
             setHeaderValue(original.getHeaderValue());    // no need to copy setTitle();
             setToolTipText(original.getToolTipText());
             setIdentifier(original.getIdentifier());
             setMaxWidth(original.getMaxWidth());
             setMinWidth(original.getMinWidth());
             setPreferredWidth(original.getPreferredWidth());
             setPrototypeValue(original.getPrototypeValue());
             // JW: isResizable is overridden to return a calculated property!
             setResizable(original.isResizable);
             setVisible(original.isVisible());
             setSortable(original.isSortable());
             setComparator(original.getComparator());
             copyClientPropertiesFrom(original);

             if (original.compoundHighlighter != null) {
                 setHighlighters(original.getHighlighters());
             }

     }

     /**
      * Copies all clientProperties of this <code>TableColumnExt</code>
      * to the target column.
      *
      * @param original the target column.
      */
     protected void copyClientPropertiesFrom(TableColumnExt original) {
        if (original.clientProperties == null) return;
        for(Object key: original.clientProperties.keySet()) {
            putClientProperty(key, original.getClientProperty(key));
        }
    }

    /**
     * Notifies registered <code>PropertyChangeListener</code>s
     * about property changes. This method must be invoked internally
     * whe any of the enhanced properties changed.
     * <p>
     * Implementation note: needed to replicate super
     * functionality because super's field <code>propertyChangeSupport</code>
     * and method <code>fireXX</code> are both private.
     *
     * @param propertyName  name of changed property
     * @param oldValue old value of changed property
     * @param newValue new value of changed property
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if ((oldValue != null && !oldValue.equals(newValue)) ||
              oldValue == null && newValue != null) {
             PropertyChangeListener pcl[] = getPropertyChangeListeners();
             if (pcl != null && pcl.length != 0) {
                 PropertyChangeEvent pce = new PropertyChangeEvent(this,
                     propertyName,
                     oldValue, newValue);

                 for (int i = 0; i < pcl.length; i++) {
                     pcl[i].propertyChange(pce);
                 }
             }
         }
     }

//---------------- implement UIDependent

    /**
     * Update ui of owned ui-dependent parts. This implementation
     * updates the contained highlighters.
     *
     */
    @Override
    public void updateUI() {
        updateHighlighterUI();
        updateRendererUI(getCellRenderer());
        updateRendererUI(getHeaderRenderer());
        updateEditorUI(getCellEditor());
    }

    /**
     * @param editor
     *
     */
    private void updateEditorUI(TableCellEditor editor) {
        if (editor == null) return;
        // internal knowledge of core table - already updated
        if ((editor instanceof JComponent)
                || (editor instanceof DefaultCellEditor))
            return;
        try {
            Component comp = editor
                    .getTableCellEditorComponent(null, null, false, -1, -1);
            if (comp != null) {
                SwingUtilities.updateComponentTreeUI(comp);
            }
        } catch (Exception e) {
            // can't do anything - renderer can't cope with off-range cells
        }
    }

    /**
     * @param tableCellRenderer
     *
     */
    private void updateRendererUI(TableCellRenderer renderer) {
        if (renderer == null) return;
        // internal knowledge of core table - already updated
        if (renderer instanceof JComponent) {
            return;
        }
        Component comp = null;
        if (renderer instanceof AbstractRenderer) {
            comp = ((AbstractRenderer) renderer).getComponentProvider().getRendererComponent(null);
        } else {
            try {
                comp = renderer
                .getTableCellRendererComponent(null, null, false, false,
                        -1, -1);

            } catch (Exception e) {
                // can't do anything - renderer can't cope with off-range cells
            }
        }
        if (comp != null) {
            SwingUtilities.updateComponentTreeUI(comp);
        }
    }

    /**
     *
     */
    private void updateHighlighterUI() {
        if (compoundHighlighter == null) return;
        compoundHighlighter.updateUI();
    }
}
