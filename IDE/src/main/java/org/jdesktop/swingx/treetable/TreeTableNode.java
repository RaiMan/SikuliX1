/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.treetable;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

/**
 * Defines the requirements for an object that can be used as a tree node in a
 * {@code JXTreeTable}.
 *
 * @author Karl Schaefer
 */
public interface TreeTableNode extends TreeNode {
    /**
     * Returns an enumeration this node's children.
     *
     * @return an enumeration of {@code TreeTableNode}s
     */
    @Override
    Enumeration<? extends TreeTableNode> children();

    /**
     * Gets the value for this node that corresponds to a particular tabular
     * column.
     *
     * @param column
     *            the column to query
     * @return the value for the queried column
     * @throws IndexOutOfBoundsException
     *             if {@code column} is not a valid column index
     */
    Object getValueAt(int column);

    /**
     * Overridden to specify the return type. Returns the child {@code TreeNode}
     * at index {@code childIndex}. Models that utilize this node should verify
     * the column count before querying this node, since nodes may return
     * differing sizes even for the same model.
     *
     * @param childIndex
     *            the index of the child
     * @return the {@code TreeTableNode} corresponding to the specified index
     */
    @Override
    TreeTableNode getChildAt(int childIndex);

    /**
     * Returns the number of columns supported by this {@code TreeTableNode}.
     *
     * @return the number of columns this node supports
     */
    int getColumnCount();

    /**
     * Overridden to specify the return type. Returns the parent
     * {@code TreeTableNode} of the receiver.
     *
     * @return the parent {@code TreeTableNode} or {@code null} if this node has
     *         no parent (such nodes are usually root nodes).
     */
    @Override
    TreeTableNode getParent();

    /**
     * Determines whether the specified column is editable.
     *
     * @param column
     *            the column to query
     * @return {@code true} if the column is editable, {@code false} otherwise
     */
    boolean isEditable(int column);

    /**
     * Sets the value for the given {@code column}.
     *
     * @param aValue
     *            the value to set
     * @param column
     *            the column to set the value on
     */
    void setValueAt(Object aValue, int column);

    /**
     * Returns this node's user object.
     *
     * @return the Object stored at this node by the user
     */
    Object getUserObject();

    /**
     * Sets the user object stored in this node.
     *
     * @param userObject
     *                the object to store
     */
    void setUserObject(Object userObject);
}
