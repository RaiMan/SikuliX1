/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.decorator;

import java.awt.Rectangle;

import javax.swing.JComponent;

import org.jdesktop.swingx.renderer.StringValues;

/**
 * Abstract base class for all component data adapter classes. A
 * <code>ComponentAdapter</code> allows the decoration collaborators like f.i.
 * {@link Highlighter} to interact with a {@link #target} component through a
 * common API. <p>
 *
 * It has two aspects:
 * <ul>
 * <li> interact with the view state for the "current" cell. The row/column
 * fields and the parameterless methods service this aspect. The coordinates are
 * in view coordinate system.
 * <li> interact with the data of the component. The methods for this are those
 * taking row/column indices as parameters. The coordinates are in model
 * coordinate system.
 * </ul>
 *
 * Typically, application code is interested in the first aspect. An example is
 * highlighting the background of a row in a JXTable based on the value of a
 * cell in a specific column. The solution is to implement a custom
 * HighlightPredicate which decides if a given cell should be highlighted and
 * configure a ColorHighlighter with the predicate and an appropriate background
 * color.
 *
 * <pre><code>
 * HighlightPredicate feverWarning = new HighlightPredicate() {
 *     int temperatureColumn = 10;
 *
 *     public boolean isHighlighted(Component component, ComponentAdapter adapter) {
 *         return hasFever(adapter.getValue(temperatureColumn));
 *     }
 *
 *     private boolean hasFever(Object value) {
 *         if (!value instanceof Number)
 *             return false;
 *         return ((Number) value).intValue() &gt; 37;
 *     }
 * };
 *
 * Highlighter hl = new ColorHighlighter(feverWarning, Color.RED, null);
 * </code></pre>
 *
 * The adapter is responsible for mapping column and row coordinates.
 *
 * All input column indices are in model coordinates with exactly two
 * exceptions:
 * <ul>
 * <li> {@link #column} in column view coordinates
 * <li> the mapping method {@link #convertColumnIndexToModel(int)} in view coordinates
 * </ul>
 *
 * All input row indices are in model coordinates with exactly four exceptions:
 * <ul>
 * <li> {@link #row} in row view coordinates
 * <li> the mapping method {@link #convertRowIndexToModel(int)} in view coordinates
 * <li> the getter for the filtered value {@link #getFilteredValueAt(int, int)}
 * takes the row in view coordinates.
  * <li> the getter for the filtered string representation {@link #getFilteredStringAt(int, int)}
 * takes the row in view coordinates.
* </ul>
 *
 *
 * PENDING JW: anything to gain by generics here?<p>
 * PENDING JW: formally document that row/column coordinates must be valid in all methods taking
 *  model coordinates, that is 0<= row < getRowCount().
 *
 * @author Ramesh Gupta
 * @author Karl Schaefer
 * @author Jeanette Winzenburg
 *
 * @see org.jdesktop.swingx.decorator.HighlightPredicate
 * @see org.jdesktop.swingx.decorator.Highlighter
 */
public abstract class ComponentAdapter {
    public static final Object DEFAULT_COLUMN_IDENTIFIER = "Column0";
    /** current row in view coordinates. */
    public int row = 0;
    /** current column in view coordinates. */
    public int column = 0;
    protected final JComponent    target;

    /**
     * Constructs a ComponentAdapter, setting the specified component as the
     * target component.
     *
     * @param component target component for this adapter
     */
    public ComponentAdapter(JComponent component) {
        target = component;
    }

    /**
     * Returns the component which is this adapter's target.
     *
     * @return the component which is this adapter's target.
     */
    public JComponent getComponent() {
        return target;
    }

//---------------------------- accessing the target's model: column meta data

    /**
     * Returns the column's display name (= headerValue) of the column
     * at columnIndex in model coordinates.
     *
     * Used f.i. in SearchPanel to fill the field with the
     * column name.<p>
     *
     * Note: it's up to the implementation to decide for which
     * columns it returns a name - most will do so for the
     * subset with isTestable = true.
     *
     * This implementation delegates to getColumnIdentifierAt and returns it's
     * toString or null.
     *
     * @param columnIndex in model coordinates
     * @return column name or null if not found
     */
    public String getColumnName(int columnIndex) {
        Object identifier = getColumnIdentifierAt(columnIndex);
        return identifier != null ? identifier.toString() : null;
    }

    /**
     * Returns logical identifier of the column at
     * columnIndex in model coordinates.
     *
     * Note: it's up to the implementation to decide for which
     * columns it returns an identifier - most will do so for the
     * subset with isTestable = true.<p>
     *
     * This implementation returns DEFAULT_COLUMN_IDENTIFIER.
     *
     * PENDING JW: This method replaces the old getColumnIdentifier(int)
     * which returned a String which is overly restrictive.
     * The only way to gently replace this method was
     * to add this with a different name - which makes this name suboptimal.
     * Probably should rename again once the old has died out ;-)
     *
     * @param columnIndex in model coordinates, must be valid.
     * @return the identifier of the column at columnIndex or null if it has none.
     * @throws ArrayIndexOutOfBoundsException if columnIndex < 0 or columnIndex >= getColumnCount().
     *
     *
     * @see #getColumnIndex(Object)
     */
    public Object getColumnIdentifierAt(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= getColumnCount())) {
            throw new ArrayIndexOutOfBoundsException("invalid column index: " + columnIndex);
        }
        return DEFAULT_COLUMN_IDENTIFIER;
    }

    /**
     * Returns the column index in model coordinates for the logical identifier.
     * <p>
     *
     * This implementation returns 0 if the identifier is the same as the one
     * known identifier returned from getColumnIdentifierAt(0), or -1 otherwise.
     * So subclasses with one column and a customizable identifier need not
     * override. Subclasses which support multiple columns must override this as
     * well to keep the contract as in (assuming that the lookup succeeded):
     *
     * <pre><code>
     *  Object id = getColumnIdentifierAt(index);
     *  assertEquals(index, getColumnIndex(index);
     *  // and the reverse
     *  int column = getColumnIndex(identifier);
     *  assertEquals(identifier, getColumnIdentifierAt(column));
     * </code></pre>
     *
     *
     * @param identifier the column's identifier, must not be null
     * @return the index of the column identified by identifier in model
     *         coordinates or -1 if no column with the given identifier is
     *         found.
     * @throws NullPointerException if identifier is null.
     * @see #getColumnIdentifierAt(int)
     */
    public int getColumnIndex(Object identifier) {
        if (identifier.equals(getColumnIdentifierAt(0))) {
            return 0;
        }
        return -1;
    }

    /**
     * Returns true if the column should be included in testing.<p>
     *
     * Here: returns true if visible (that is modelToView gives a valid
     * view column coordinate).
     *
     * @param column the column index in model coordinates
     * @return true if the column should be included in testing
     */
    public boolean isTestable(int column) {
        return convertColumnIndexToView(column) >= 0;
    }

    /**
     * Returns the common class of all data column identified by the given
     * column index in model coordinates.<p>
     *
     * This implementation returns <code>Object.class</code>. Subclasses should
     * implement as appropriate.
     *
     * @return the common class of all data given column in model coordinates.
     *
     * @see #getColumnClass()
     */
    public Class<?> getColumnClass(int column) {
        return Object.class;
    }

    /**
     * Returns the common class of all data in the current column.<p>
     *
     * This implementation delegates to getColumnClass(int) with the current
     * column converted to model coordinates.
     *
     * @return the common class of all data in the current column.
     * @see #getColumnClass(int)
     */
    public Class<?> getColumnClass() {
        return getColumnClass(convertColumnIndexToModel(column));
    }


//---------------------------- accessing the target's model: meta data
    /**
     * Returns the number of columns in the target's data model.
     *
     * @return the number of columns in the target's data model.
     */
    public int getColumnCount() {
        return 1;    // default for combo-boxes, lists, and trees
    }

    /**
     * Returns the number of rows in the target's data model.
     *
     * @return the number of rows in the target's data model.
     */
    public int getRowCount() {
        return 0;
    }

//---------------------------- accessing the target's model: data
    /**
     * Returns the value of the target component's cell identified by the
     * specified row and column in model coordinates.
     *
     * @param row in model coordinates
     * @param column in model coordinates
     * @return the value of the target component's cell identified by the
     *          specified row and column
     */
    public abstract Object getValueAt(int row, int column);

    /**
     * Determines whether this cell is editable.
     *
     * @param row the row to query in model coordinates
     * @param column the column to query in model coordinates
     * @return <code>true</code> if the cell is editable, <code>false</code>
     *         otherwise
     */
    public abstract boolean isCellEditable(int row, int column);

    /**
     * Returns the String representation of the value of the cell identified by this adapter. That is,
     * for the at position (adapter.row, adapter.column) in view coordinates.<p>
     *
     * NOTE: this implementation assumes that view coordinates == model
     * coordinates, that is simply calls getValueAt(this.row, this.column). It is
     * up to subclasses to override appropriately is they support model/view
     * coordinate transformation. <p>
     *
     * This implementation messages the StringValue.TO_STRING with the getValue,
     * subclasses should re-implement and use the API appropriate for the target component type.
     *
     * @return the String representation of value of the cell identified by this adapter
     * @see #getValueAt(int, int)
     * @see #getFilteredValueAt(int, int)
     * @see #getValue(int)
     */
    public String getString() {
        return getString(convertColumnIndexToModel(column));
    }

    /**
     * Returns the String representation of the value of the cell identified by the current
     * adapter row and the given column index in model coordinates.<p>
     *
     * @param modelColumnIndex the column index in model coordinates
     * @return the String representation of the value of the cell identified by this adapter
     *
     * @see #getFilteredStringAt(int, int)
     * @see #getString()
     */
    public String getString(int modelColumnIndex) {
        return getFilteredStringAt(row, modelColumnIndex);
    }

    /**
     * Returns the String representation of the filtered value of the cell identified by the row
     * in view coordinate and the column in model coordinates.<p>
     *
     * Note: the asymetry of the coordinates is intentional - clients like
     * Highlighters are interested in view values but might need to access
     * non-visible columns for testing. While it is possible to access
     * row coordinates different from the current (that is this.row) it is not
     * safe to do so for row > this.row because the adapter doesn't allow to
     * query the count of visible rows.<p>
     *
     * This implementation messages the StringValue.TO_STRING with the filteredValue,
     * subclasses should re-implement and use the API appropriate for the target component type.<p>
     *
     * PENDING JW: what about null cell values? StringValue has a contract to return a
     * empty string then, would that be okay here as well?
     *
     * @param row the row of the cell in view coordinates
     * @param column the column of the cell in model coordinates.
     * @return the String representation of the filtered value of the cell identified by the row
     * in view coordinate and the column in model coordinates
     */
    public String getFilteredStringAt(int row, int column) {
        return getStringAt(convertRowIndexToModel(row), column);
    }

    /**
     * Returns the String representation of the value of the cell identified by the row
     * specified row and column in model coordinates.<p>
     *
     * This implementation messages the StringValue.TO_STRING with the valueAt,
     * subclasses should re-implement and use the api appropriate for the target component type.<p>
     *
     * @param row in model coordinates
     * @param column in model coordinates
     * @return the value of the target component's cell identified by the
     *          specified row and column
     */
    public String getStringAt(int row, int column) {
        return StringValues.TO_STRING.getString(getValueAt(row, column));
    }

    /**
     * Returns the value of the cell identified by this adapter. That is,
     * for the at position (adapter.row, adapter.column) in view coordinates.<p>
     *
     * NOTE: this implementation assumes that view coordinates == model
     * coordinates, that is simply calls getValueAt(this.row, this.column). It is
     * up to subclasses to override appropriately is they support model/view
     * coordinate transformation.
     *
     * @return the value of the cell identified by this adapter
     * @see #getValueAt(int, int)
     * @see #getFilteredValueAt(int, int)
     * @see #getValue(int)
     */
    public Object getValue() {
        return getValue(convertColumnIndexToModel(column));
    }

    /**
     * Returns the value of the cell identified by the current
     * adapter row and the given column index in model coordinates.<p>
     *
     * @param modelColumnIndex the column index in model coordinates
     * @return the value of the cell identified by this adapter
     * @see #getValueAt(int, int)
     * @see #getFilteredValueAt(int, int)
     * @see #getValue(int)
     */
    public Object getValue(int modelColumnIndex) {
        return getFilteredValueAt(row, modelColumnIndex);
    }

    /**
     * Returns the filtered value of the cell identified by the row
     * in view coordinate and the column in model coordinates.
     *
     * Note: the asymmetry of the coordinates is intentional - clients like
     * Highlighters are interested in view values but might need to access
     * non-visible columns for testing. While it is possible to access
     * row coordinates different from the current (that is this.row) it is not
     * safe to do so for row > this.row because the adapter doesn't allow to
     * query the count of visible rows.
     *
     * @param row the row of the cell in view coordinates
     * @param column the column of the cell in model coordinates.
     * @return the filtered value of the cell identified by the row
     * in view coordinate and the column in model coordinates
     */
    public Object getFilteredValueAt(int row, int column) {
        return getValueAt(convertRowIndexToModel(row), column);
    }

    //----------------------- accessing the target's view state

    /**
     * Returns the bounds of the cell identified by this adapter.<p>
     *
     * @return the bounds of the cell identified by this adapter
     */
    public Rectangle getCellBounds() {
        return target.getBounds();
    }

    /**
     * Returns true if the cell identified by this adapter currently has focus.
     * Otherwise, it returns false.
     *
     * @return true if the cell identified by this adapter currently has focus;
     *  Otherwise, return false
     */
    public abstract boolean hasFocus();

    /**
     * Returns true if the cell identified by this adapter is currently selected.
     * Otherwise, it returns false.
     *
     * @return true if the cell identified by this adapter is currently selected;
     *  Otherwise, return false
     */
    public abstract boolean isSelected();

    /**
     * Returns {@code true} if the cell identified by this adapter is editable,
     * {@code false} otherwise.
     *
     * @return {@code true} if the cell is editable, {@code false} otherwise
     */
    public abstract boolean isEditable();

    /**
     * Returns true if the cell identified by this adapter is currently expanded.
     * Otherwise, it returns false. For components that do not support
     * hierarchical data, this method always returns true because the cells in
     * such components can never be collapsed.
     *
     * @return true if the cell identified by this adapter is currently expanded;
     *     Otherwise, return false
     */
    public boolean isExpanded() {
        return true; // sensible default for JList and JTable
    }

    /**
     * Returns true if the cell identified by this adapter is a leaf node.
     * Otherwise, it returns false. For components that do not support
     * hierarchical data, this method always returns true because the cells in
     * such components can never have children.
     *
     * @return true if the cell identified by this adapter is a leaf node;
     *     Otherwise, return false
     */
    public boolean isLeaf() {
        return true; // sensible default for JList and JTable
    }

    /**
     * Returns true if the cell identified by this adapter displays the hierarchical node.
     * Otherwise, it returns false. For components that do not support
     * hierarchical data, this method always returns false because the cells in
     * such components can never have children.
     *
     * @return true if the cell identified by this adapter displays the hierarchical node;
     *  Otherwise, return false
     */
    public boolean isHierarchical() {
        return false; // sensible default for JList and JTable
    }

    /**
     * Returns the depth of this row in the hierarchy where the root is 0. For
     * components that do not contain hierarchical data, this method returns 1.
     *
     * @return the depth for this adapter
     */
    public int getDepth() {
        return 1; // sensible default for JList and JTable
    }

//-------------------- cell coordinate transformations

    /**
     * For target components that support multiple columns in their model,
     * along with column reordering in the view, this method transforms the
     * specified columnIndex from model coordinates to view coordinates. For all
     * other types of target components, this method returns the columnIndex
     * unchanged.
     *
     * @param columnModelIndex index of a column in model coordinates
     * @return index of the specified column in view coordinates
     */
    public int convertColumnIndexToView(int columnModelIndex) {
        return columnModelIndex; // sensible default for JList and JTree
    }

    /**
     * For target components that support multiple columns in their model, along
     * with column reordering in the view, this method transforms the specified
     * columnIndex from view coordinates to model coordinates. For all other
     * types of target components, this method returns the columnIndex
     * unchanged.
     *
     * @param columnViewIndex index of a column in view coordinates
     * @return index of the specified column in model coordinates
     */
    public int convertColumnIndexToModel(int columnViewIndex) {
        return columnViewIndex; // sensible default for JList and JTree
    }

    /**
     * Converts a row index in model coordinates to an index in view coordinates.
     *
     * @param rowModelIndex index of a row in model coordinates
     * @return index of the specified row in view coordinates
     */
    public int convertRowIndexToView(int rowModelIndex) {
        return rowModelIndex; // sensible default for JTree
    }

    /**
     * Converts a row index in view coordinates to an index in model coordinates.
     *
     * @param rowViewIndex index of a row in view coordinates
     * @return index of the specified row in model coordinates
     */
    public int convertRowIndexToModel(int rowViewIndex) {
        return rowViewIndex; // sensible default for JTree
    }
}
