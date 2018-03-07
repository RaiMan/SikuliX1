/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Encapsulates a snapshop of cell content and default display context
 * for usage by a <code>ComponentProvider</code>.
 * <p>
 *
 * One part is the super-set of properties that's traditionally passed into the
 * core renderers' (Table-, List-, Tree-) getXXCellRendererComponent. Raw
 * properties which define the context are
 *
 * <ul>
 * <li> selected
 * <li> focused
 * <li> expanded
 * <li> leaf
 * </ul>
 *
 * Similarl to a ComponentAdapter, the properties are a super-set of those for
 * a concrete component type. It's up to sub-classes (once the generics will be removed, until
 * then the DefaultXXRenderers - PENDING JW: undecided - even after the generics removal, the
 * param list in the subclasses are the same) fill any reasonable
 * defaults for those not applicable to the specific component context.
 *
 * With those raw properties given, a CellContext looks up and returns dependent visual
 * properties as appropriate for the concrete component. Typically, they are taken
 * from the component if supported, or requested from the UIManager.
 * Dependent properties are
 *
 * <ul>
 * <li> foreground and background color
 * <li> border
 * <li> icon (relevant for trees only)
 * <li> editable
 * </ul>
 *
 * For a backdoor, the cell location (in horizontal and vertical view coordinates)
 * and the originating component is accessible as well. Note that they are not necessarily
 * valid for the "life" component. It's not recommended to actually use them. If needed,
 * that's probably a sign the api is lacking :-)
 * <p>
 *
 *
 * <ul>
 *
 * <li>PENDING: still incomplete? how about Font?
 * <li>PENDING: protected methods? Probably need to open up - derived
 * properties should be accessible in client code.
 * </ul>
 *
 * @author Jeanette Winzenburg
 */
public class CellContext implements Serializable {

    /** the default border for unfocused cells. */
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    /** ?? the default border for unfocused cells. ?? */
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1,
            1);

    /**
     * Returns the shared border for unfocused cells.
     * <p>
     * PENDING: ?? copied from default renderers - why is it done like this?
     *
     * @return the border for unfocused cells.
     */
    private static Border getNoFocusBorder() {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        } else {
            return noFocusBorder;
        }
    }

    /** PENDING JW: maybe make this a WeakReference? Would be a more robust fix for Issue #1040-swingx. */
    protected transient JComponent component;

    /** PENDING JW: maybe make this a WeakReference? Would be a more robust fix for Issue #1040-swingx. */
    protected transient Object value;

    protected transient int row;

    protected transient int column;

    protected transient boolean selected;

    protected transient boolean focused;

    protected transient boolean expanded;

    protected transient boolean leaf;

    protected transient boolean dropOn;

    // --------------------------- install context

    /**
     * Sets the state of the cell's context. Convenience method for subclasses.
     *
     * @param value the content value of the cell
     * @param row the cell's row index in view coordinates
     * @param column the cell's column index in view coordinates
     * @param selected the cell's selected state
     * @param focused the cell's focused state
     * @param expanded the cell's expanded state
     * @param leaf the cell's leaf state
     */
    protected void installState(Object value, int row, int column,
            boolean selected, boolean focused, boolean expanded, boolean leaf) {
        this.value = value;
        this.row = row;
        this.column = column;
        this.selected = selected;
        this.focused = focused;
        this.expanded = expanded;
        this.leaf = leaf;
    }

    /**
     * Replaces the value of this cell context with the given parameter and returns
     * the replaced value.
     *
     * @param value the new value of the cell context
     * @return the replaced value of the cell context
     */
    public Object replaceValue(Object value) {
        Object old = getValue();
        this.value = value;
        return old;
    }

    // -------------------- accessors of installed state

    /**
     * Returns the component the cell resides on, may be null. Subclasses are
     * expected to override and return the component type they are handling.
     *
     * @return the component the cell resides on, may be null.
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * Returns the value of the cell as set in the install.
     *
     * @return the content value of the cell.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the cell's row index in view coordinates as set in the install.
     *
     * @return the cell's row index.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the cell's column index in view coordinates as set in the
     * install.
     *
     * @return the cell's column index.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the selected state as set in the install.
     *
     * @return the cell's selected state.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns the focused state as set in the install.
     *
     * @return the cell's focused state.
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Returns the expanded state as set in the install.
     *
     * @return the cell's expanded state.
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Returns the leaf state as set in the install.
     *
     * @return the cell's leaf state.
     */
    public boolean isLeaf() {
        return leaf;
    }

    // -------------------- accessors for derived state
    /**
     * Returns the cell's editability. Subclasses should override to return a
     * reasonable cell-related state.
     * <p>
     *
     * Here: false.
     *
     * @return the cell's editable property.
     */
    public boolean isEditable() {
        return false;
    }

    /**
     * Returns the icon. Subclasses should override to return a reasonable
     * cell-related state.
     * <p>
     *
     * Here: <code>null</code>.
     *
     * @return the cell's icon.
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Returns a boolean indicating if the cell is a drop location with any of the dropOn
     * modes. It's up to subclasses to implement.
     * <p>
     *
     * Here: false.
     *
     * @return true if the current cell is a drop location with any of the dropOn modes,
     *    false otherwise
     */
    protected boolean isDropOn() {
        return dropOn;
    }

    /**
     * Returns the foreground color of the renderered component or null if the
     * component is null
     * <p>
     *
     * PENDING: fallback to UI properties if comp == null?
     *
     * @return the foreground color of the rendered component.
     */
    protected Color getForeground() {
        if (isDropOn()) {
            return getSelectionForeground();
        }
        return getComponent() != null ? getComponent().getForeground() : null;
    }

    /**
     * Returns the background color of the renderered component or null if the
     * component is null
     * <p>
     *
     * PENDING: fallback to UI properties if comp == null?
     *
     * @return the background color of the rendered component.
     */
    protected Color getBackground() {
        if (isDropOn()) {
            return getSelectionBackground();
        }
        return getComponent() != null ? getComponent().getBackground() : null;
    }

    /**
     * Returns the default selection background color of the renderered
     * component. Typically, the color is LF specific. It's up to subclasses to
     * look it up. Here: returns null.
     * <p>
     *
     * PENDING: return UI properties here?
     *
     * @return the selection background color of the rendered component.
     */
    protected Color getSelectionBackground() {
        return null;
    }

    /**
     * Returns the default selection foreground color of the renderered
     * component. Typically, the color is LF specific. It's up to subclasses to
     * look it up. Here: returns null.
     * <p>
     *
     * PENDING: return UI properties here?
     *
     * @return the selection foreground color of the rendered component.
     */
    protected Color getSelectionForeground() {
        return null;
    }

    /**
     * Returns the default focus border of the renderered component. Typically,
     * the border is LF specific.
     *
     * @return the focus border of the rendered component.
     */
    protected Border getFocusBorder() {
        Border border = null;
        if (isSelected()) {
            border = UIManager
                    .getBorder(getUIKey("focusSelectedCellHighlightBorder"));
        }
        if (border == null) {
            border = UIManager.getBorder(getUIKey("focusCellHighlightBorder"));
        }
        return border;
    }

    /**
     * Returns the default border of the renderered component depending on cell
     * state. Typically, the border is LF specific.
     * <p>
     *
     * Here: returns the focus border if the cell is focused, the context
     * defined no focus border otherwise.
     *
     * @return the default border of the rendered component.
     */
    protected Border getBorder() {
        if (isFocused()) {
            return getFocusBorder();
        }
        Border border = UIManager.getBorder(getUIKey("cellNoFocusBorder"));
        return border != null ? border : getNoFocusBorder();
    }

    /**
     * Returns the default focused foreground color of the renderered component.
     * Typically, the color is LF specific.
     *
     * @return the focused foreground color of the rendered component.
     */
    protected Color getFocusForeground() {
        return UIManager.getColor(getUIKey("focusCellForeground"));
    }

    /**
     * Returns the default focused background color of the renderered component.
     * Typically, the color is LF specific.
     *
     * @return the focused background color of the rendered component.
     */
    protected Color getFocusBackground() {
        return UIManager.getColor(getUIKey("focusCellBackground"));
    }

    protected Color getDropCellForeground() {
        return UIManager.getColor(getUIKey("dropCellForeground"));
    }

    protected Color getDropCellBackground() {
        return UIManager.getColor(getUIKey("dropCellBackground"));
    }
    // ----------------------- convenience

    /**
     * Convenience method to build a component type specific lookup key for the
     * UIManager.
     *
     * @param key the general part of the key
     * @return a composed key build of a component type prefix and the input.
     */
    protected String getUIKey(String key) {
        return getUIPrefix() + key;
    }

    /**
     * Returns the component type specific prefix of keys for lookup in the
     * UIManager. Subclasses must override, here: returns the empty String.
     *
     * @return the component type specific prefix.
     */
    protected String getUIPrefix() {
        return "";
    }

    /**
     * Returns the Font of the target component or null if no component installed.
     * @return
     */
    protected Font getFont() {
        return getComponent() != null ? getComponent().getFont() : null;
    }

    public String getCellRendererName() {
        return getUIPrefix() + "cellRenderer";
    }

}
