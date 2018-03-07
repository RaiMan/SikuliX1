/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.treetable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreePath;

/**
 * {@code DefaultTreeTableModel} is a concrete implementation of
 * {@code AbstractTreeTableModel} and is provided purely as a convenience for
 * use with {@code TreeTableNode}s. Applications that use {@code JXTreeTable}
 * without {@code TreeTableNode}s are expected to provide their own
 * implementation of a {@code TreeTableModel}.
 * <p>
 * The {@code DefaultTreeTableModel} is designed to be used with
 * {@code TreeTableNode}s. Specifically, users should extend
 * {@code AbstractMutableTreeTableNode} to provide custom implementations for
 * data display.
 * <p>
 * Users who do not provide a list of column identifiers must provide a root
 * that contains at least one column. Without specified identifiers the model
 * will attempt to calculate the columns required for display by querying the
 * root node. Normally, the root node can be little more than a shell (in
 * displays that hide it), but without identifiers, the model relies on the root
 * node metadata for display.
 *
 * @author Ramesh Gupta
 * @author Karl Schaefer
 */
public class DefaultTreeTableModel extends AbstractTreeTableModel {
    /** The <code>List</code> of column identifiers. */
    protected List<?> columnIdentifiers;

    private boolean useAutoCalculatedIdentifiers;

    /**
     * Creates a new {@code DefaultTreeTableModel} with a {@code null} root.
     */
    public DefaultTreeTableModel() {
        this(null);
    }

    /**
     * Creates a new {@code DefaultTreeTableModel} with the specified
     * {@code root}.
     *
     * @param root
     *            the root node of the tree
     */
    public DefaultTreeTableModel(TreeTableNode root) {
        this(root, null);
    }

    /**
     * Creates a new {@code DefaultTreeTableModel} with the specified {@code
     * root} and column names.
     *
     * @param root
     *            the root node of the tree
     * @param columnNames
     *            the names of the columns used by this model
     * @see #setColumnIdentifiers(List)
     */
    public DefaultTreeTableModel(TreeTableNode root, List<?> columnNames) {
        super(root);

        setColumnIdentifiers(columnNames);
    }

    private boolean isValidTreeTableNode(Object node) {
         boolean result = false;

        if (node instanceof TreeTableNode) {
            TreeTableNode ttn = (TreeTableNode) node;

            while (!result && ttn != null) {
                result = ttn == root;

                ttn = ttn.getParent();
            }
        }

        return result;
    }

    /**
     * Replaces the column identifiers in the model. If the number of
     * <code>newIdentifier</code>s is greater than the current number of
     * columns, new columns are added to the end of each row in the model. If
     * the number of <code>newIdentifier</code>s is less than the current
     * number of columns, all the extra columns at the end of a row are
     * discarded.
     * <p>
     *
     * @param columnIdentifiers
     *            vector of column identifiers. If <code>null</code>, set the
     *            model to zero columns
     */
    // from DefaultTableModel
    public void setColumnIdentifiers(List<?> columnIdentifiers) {
        useAutoCalculatedIdentifiers = columnIdentifiers == null;

        this.columnIdentifiers = useAutoCalculatedIdentifiers
                ? getAutoCalculatedIdentifiers(getRoot())
                : columnIdentifiers;

        modelSupport.fireNewRoot();
    }

    private static List<String> getAutoCalculatedIdentifiers(
            TreeTableNode exemplar) {
        List<String> autoCalculatedIndentifiers = new ArrayList<String>();

        if (exemplar != null) {
            for (int i = 0, len = exemplar.getColumnCount(); i < len; i++) {
                // forces getColumnName to use super.getColumnName
                autoCalculatedIndentifiers.add(null);
            }
        }

        return autoCalculatedIndentifiers;
    }

    /**
     * Returns the root of the tree. Returns {@code null} only if the tree has
     * no nodes.
     *
     * @return the root of the tree
     *
     * @throws ClassCastException
     *             if {@code root} is not a {@code TreeTableNode}. Even though
     *             subclasses have direct access to {@code root}, they should
     *             avoid accessing it directly.
     * @see AbstractTreeTableModel#root
     * @see #setRoot(TreeTableNode)
     */
    @Override
    public TreeTableNode getRoot() {
        return (TreeTableNode) root;
    }

    /**
     * Gets the value for the {@code node} at {@code column}.
     *
     * @impl delegates to {@code TreeTableNode.getValueAt(int)}
     * @param node
     *            the node whose value is to be queried
     * @param column
     *            the column whose value is to be queried
     * @return the value Object at the specified cell
     * @throws IllegalArgumentException
     *             if {@code node} is not an instance of {@code TreeTableNode}
     *             or is not managed by this model, or {@code column} is not a
     *             valid column index
     */
    @Override
    public Object getValueAt(Object node, int column) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException(
                    "node must be a valid node managed by this model");
        }

        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be a valid index");
        }

        TreeTableNode ttn = (TreeTableNode) node;

        if (column >= ttn.getColumnCount()) {
            return null;
        }

        return ttn.getValueAt(column);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, Object node, int column) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException(
                    "node must be a valid node managed by this model");
        }

        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be a valid index");
        }

        TreeTableNode ttn = (TreeTableNode) node;

        if (column < ttn.getColumnCount()) {
            ttn.setValueAt(value, column);

            modelSupport.firePathChanged(new TreePath(getPathToRoot(ttn)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return columnIdentifiers.size();
    }

    /**
     * {@inheritDoc}
     */
    // Can we make getColumnClass final and avoid the complex DTM copy? -- kgs
    @Override
    public String getColumnName(int column) {
        // Copied from DefaultTableModel.
        Object id = null;

        // This test is to cover the case when
        // getColumnCount has been subclassed by mistake ...
        if (column < columnIdentifiers.size() && (column >= 0)) {
            id = columnIdentifiers.get(column);
        }

        return (id == null) ? super.getColumnName(column) : id.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChild(Object parent, int index) {
        if (!isValidTreeTableNode(parent)) {
            throw new IllegalArgumentException(
                    "parent must be a TreeTableNode managed by this model");
        }

        return ((TreeTableNode) parent).getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount(Object parent) {
        if (!isValidTreeTableNode(parent)) {
            throw new IllegalArgumentException(
                    "parent must be a TreeTableNode managed by this model");
        }

        return ((TreeTableNode) parent).getChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (!isValidTreeTableNode(parent) || !isValidTreeTableNode(child)) {
            return -1;
        }

        return ((TreeTableNode) parent).getIndex((TreeTableNode) child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(Object node, int column) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException(
                    "node must be a valid node managed by this model");
        }

        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be a valid index");
        }

        TreeTableNode ttn = (TreeTableNode) node;

        if (column >= ttn.getColumnCount()) {
            return false;
        }

        return ttn.isEditable(column);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaf(Object node) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException(
                    "node must be a TreeTableNode managed by this model");
        }

        return ((TreeTableNode) node).isLeaf();
    }

    /**
     * Gets the path from the root to the specified node.
     *
     * @param aNode
     *            the node to query
     * @return an array of {@code TreeTableNode}s, where
     *         {@code arr[0].equals(getRoot())} and
     *         {@code arr[arr.length - 1].equals(aNode)}, or an empty array if
     *         the node is not found.
     * @throws NullPointerException
     *             if {@code aNode} is {@code null}
     */
    public TreeTableNode[] getPathToRoot(TreeTableNode aNode) {
        List<TreeTableNode> path = new ArrayList<TreeTableNode>();
        TreeTableNode node = aNode;

        while (node != root) {
            path.add(0, node);

            node = node.getParent();
        }

        if (node == root) {
            path.add(0, node);
        }

        return path.toArray(new TreeTableNode[0]);
    }

    /**
     * Sets the root for this table model. If no column identifiers have been
     * specified, this will rebuild the identifier list, using {@code root} as
     * an examplar of the table.
     *
     * @param root
     *            the node to set as root
     */
    public void setRoot(TreeTableNode root) {
        this.root = root;

        if (useAutoCalculatedIdentifiers) {
            // rebuild the list
            //this already fires an event don't duplicate
            setColumnIdentifiers(null);
        } else {
            modelSupport.fireNewRoot();
        }
    }

    /**
     * Invoked this to insert newChild at location index in parents children.
     * This will then message nodesWereInserted to create the appropriate event.
     * This is the preferred way to add children as it will create the
     * appropriate event.
     */
    public void insertNodeInto(MutableTreeTableNode newChild,
            MutableTreeTableNode parent, int index) {
        parent.insert(newChild, index);

        modelSupport.fireChildAdded(new TreePath(getPathToRoot(parent)), index,
                newChild);
    }

    /**
     * Message this to remove node from its parent. This will message
     * nodesWereRemoved to create the appropriate event. This is the preferred
     * way to remove a node as it handles the event creation for you.
     */
    public void removeNodeFromParent(MutableTreeTableNode node) {
        MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();

        if (parent == null) {
            throw new IllegalArgumentException("node does not have a parent.");
        }

        int index = parent.getIndex(node);
        node.removeFromParent();

        modelSupport.fireChildRemoved(new TreePath(getPathToRoot(parent)),
                index, node);
    }

    /**
     * Called when value for the item identified by path has been changed. If
     * newValue signifies a truly new value the model should post a {@code
     * treeNodesChanged} event.
     * <p>
     * This changes the object backing the {@code TreeTableNode} described by
     * the path. This change does not alter a nodes children in any way. If you
     * need to change structure of the node, use one of the provided mutator
     * methods.
     *
     * @param path
     *            path to the node that has changed
     * @param newValue
     *            the new value
     * @throws NullPointerException
     *             if {@code path} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code path} is not a path managed by this model
     * @throws ClassCastException
     *             if {@code path.getLastPathComponent()} is not a {@code
     *             TreeTableNode}
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (path.getPathComponent(0) != root) {
            throw new IllegalArgumentException("invalid path");
        }

        TreeTableNode node = (TreeTableNode) path.getLastPathComponent();
        node.setUserObject(newValue);

        modelSupport.firePathChanged(path);
    }

    /**
     * Sets the user object for a node. Client code must use this method, so
     * that the model can notify listeners that a change has occurred.
     * <p>
     * This method is a convenient cover for
     * {@link #valueForPathChanged(TreePath, Object)}.
     *
     * @param node
     *            the node to modify
     * @param userObject
     *            the new user object to set
     * @throws NullPointerException
     *             if {@code node} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code node} is not a node managed by this model
     */
    public void setUserObject(TreeTableNode node, Object userObject) {
        valueForPathChanged(new TreePath(getPathToRoot(node)), userObject);
    }
}
