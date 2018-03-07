/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;

import javax.swing.JList;

/**
 * List specific <code>CellContext</code>.
 */
public class ListCellContext extends CellContext {

    /**
     * Sets state of the cell's context. Note that the component might be null
     * to indicate a cell without a concrete context. All accessors must cope
     * with.
     *
     * @param component the component the cell resides on, might be null
     * @param value the content value of the cell
     * @param row the cell's row index in view coordinates
     * @param column the cell's column index in view coordinates
     * @param selected the cell's selected state
     * @param focused the cell's focused state
     * @param expanded the cell's expanded state
     * @param leaf the cell's leaf state
     */
    public void installContext(JList component, Object value, int row, int column,
            boolean selected, boolean focused, boolean expanded, boolean leaf) {
        this.component = component;
        installState(value, row, column, selected, focused, expanded, leaf);
        this.dropOn = checkDropOnState();
    }

    /**
     *
     */
    private boolean checkDropOnState() {
        if ((getComponent() == null)) {
            return false;
        }
        JList.DropLocation dropLocation = getComponent().getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == row) {
            return true;
        }
        return false;
    }

    @Override
    public JList getComponent() {
        return (JList) super.getComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Color getSelectionBackground() {
        Color selection = null;
        if (isDropOn()) {
            selection = getDropCellBackground();
            if (selection != null) return selection;
        }
        return getComponent() != null ? getComponent().getSelectionBackground() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Color getSelectionForeground() {
        Color selection = null;
        if (isDropOn()) {
            selection = getDropCellForeground();
            if (selection != null) return selection;
        }
        return getComponent() != null ? getComponent().getSelectionForeground() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUIPrefix() {
        return "List.";
    }


}
