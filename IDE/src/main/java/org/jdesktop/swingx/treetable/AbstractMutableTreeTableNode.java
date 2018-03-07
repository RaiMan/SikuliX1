/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.treetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

/**
 * {@code AbstractMutableTreeTableNode} provides an implementation of most of
 * the {@code MutableTreeTableNode} features.
 *
 * @author Karl Schaefer
 */
public abstract class AbstractMutableTreeTableNode implements
        MutableTreeTableNode {
    /** this node's parent, or null if this node has no parent */
    protected MutableTreeTableNode parent;

    /**
     * List of children, if this node has no children the list will be empty.
     * This list will never be null.
     */
    protected final List<MutableTreeTableNode> children;

    /** optional user object */
    protected transient Object userObject;

    protected boolean allowsChildren;

    public AbstractMutableTreeTableNode() {
        this(null);
    }

    public AbstractMutableTreeTableNode(Object userObject) {
        this(userObject, true);
    }

    public AbstractMutableTreeTableNode(Object userObject,
            boolean allowsChildren) {
        this.userObject = userObject;
        this.allowsChildren = allowsChildren;
        children = createChildrenList();
    }

    /**
     * Creates the list used to manage the children of this node.
     * <p>
     * This method is called by the constructor.
     *
     * @return a list; this list is guaranteed to be non-{@code null}
     */
    protected List<MutableTreeTableNode> createChildrenList() {
        return new ArrayList<MutableTreeTableNode>();
    }

    public void add(MutableTreeTableNode child) {
        insert(child, getChildCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(MutableTreeTableNode child, int index) {
        if (!allowsChildren) {
            throw new IllegalStateException("this node cannot accept children");
        }

        if (children.contains(child)) {
            children.remove(child);
            index--;
        }

        children.add(index, child);

        if (child.getParent() != this) {
            child.setParent(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(int index) {
        children.remove(index).setParent(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(MutableTreeTableNode node) {
        children.remove(node);
        node.setParent(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromParent() {
        parent.remove(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(MutableTreeTableNode newParent) {
        if (newParent == null || newParent.getAllowsChildren()) {
            if (parent != null && parent.getIndex(this) != -1) {
                parent.remove(this);
            }
        } else {
            throw new IllegalArgumentException(
                    "newParent does not allow children");
        }

        parent = newParent;

        if (parent != null && parent.getIndex(this) == -1) {
            parent.insert(this, parent.getChildCount());
        }
    }

    /**
     * Returns this node's user object.
     *
     * @return the Object stored at this node by the user
     * @see #setUserObject
     * @see #toString
     */
    @Override
    public Object getUserObject() {
        return userObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserObject(Object object) {
        userObject = object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeTableNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeTableNode getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<? extends MutableTreeTableNode> children() {
        return Collections.enumeration(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowsChildren() {
        return allowsChildren;
    }

    /**
     * Determines whether or not this node is allowed to have children. If
     * {@code allowsChildren} is {@code false}, all of this node's children are
     * removed.
     * <p>
     * Note: By default, a node allows children.
     *
     * @param allowsChildren
     *            {@code true} if this node is allowed to have children
     */
    public void setAllowsChildren(boolean allowsChildren) {
        this.allowsChildren = allowsChildren;

        if (!this.allowsChildren) {
            children.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount() {
        return children.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    /**
     * Determines whether the specified column is editable.
     *
     * @param column
     *            the column to query
     * @return always returns {@code false}
     */
    @Override
    public boolean isEditable(int column) {
        return false;
    }

    /**
     * Sets the value for the given {@code column}.
     *
     * @impl does nothing. It is provided for convenience.
     * @param aValue
     *            the value to set
     * @param column
     *            the column to set the value on
     */
    @Override
    public void setValueAt(Object aValue, int column) {
        // does nothing
    }

    /**
     * Returns the result of sending <code>toString()</code> to this node's
     * user object, or null if this node has no user object.
     *
     * @see #getUserObject
     */
    @Override
    public String toString() {
        if (userObject == null) {
            return "";
        } else {
            return userObject.toString();
        }
    }
}
