/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.tree;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.util.Contract;

/**
 * Support for change notification, usable by {@code TreeModel}s.
 *
 * The changed/inserted/removed is expressed in terms of a {@code TreePath},
 * it's up to the client model to build it as appropriate.
 *
 * This is inspired by {@code AbstractTreeModel} from Christian Kaufhold,
 * www.chka.de.
 *
 * TODO - implement and test precondition failure of added/removed notification
 *
 * @author JW
 */
public final class TreeModelSupport {
    protected EventListenerList listeners;

    private TreeModel treeModel;

    /**
     * Creates the support class for the given {@code TreeModel}.
     *
     * @param model the model to support
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public TreeModelSupport(TreeModel model) {
        if (model == null)
            throw new NullPointerException("model must not be null");
        listeners = new EventListenerList();
        this.treeModel = model;
    }

//---------------------- structural changes on subtree

    /**
     * Notifies registered TreeModelListeners that the tree's root has
     * been replaced. Can cope with a null root.
     */
    public void fireNewRoot() {

        Object root = treeModel.getRoot();

        /*
         * Undocumented. I think it is the only reasonable/possible solution to
         * use use null as path if there is no root. TreeModels without root
         * aren't important anyway, since JTree doesn't support them (yet).
         */
        TreePath path = (root != null) ? new TreePath(root) : null;
        fireTreeStructureChanged(path);
    }

    /**
     * Call when a node has changed its leaf state.<p>
     *
     * PENDING: rename? Do we need it?
     * @param path the path to the node with changed leaf state.
     */
    public void firePathLeafStateChanged(TreePath path) {
        fireTreeStructureChanged(path);
    }

    /**
     * Notifies registered TreeModelListeners that the structure
     * below the node identified by the given path has been
     * completely changed.
     * <p>
     * NOTE: the subtree path maybe null if the root is null.
     * If not null, it must contain at least one element (the root).
     *
     * @param subTreePath the path to the root of the subtree
     *    whose structure was changed.
     * @throws NullPointerException if the path is not null but empty
     *   or contains null elements.
     */
    public void fireTreeStructureChanged(TreePath subTreePath) {
        if (subTreePath != null) {
            Contract.asNotNull(subTreePath.getPath(),
                    "path must not contain null elements");
        }
        Object[] pairs = listeners.getListenerList();

        TreeModelEvent e = null;

        for (int i = pairs.length - 2; i >= 0; i -= 2) {
            if (pairs[i] == TreeModelListener.class) {
                if (e == null)
                    e = createStructureChangedEvent(subTreePath);

                ((TreeModelListener) pairs[i + 1]).treeStructureChanged(e);
            }
        }
    }

//----------------------- node modifications, no mutations

    /**
     * Notifies registered TreeModelListeners that the
     * the node identified by the given path has been modified.
     *
     * @param path the path to the node that has been modified,
     *   must not be null and must not contain null path elements.
     *
     */
    public void firePathChanged(TreePath path) {
        Object node = path.getLastPathComponent();
        TreePath parentPath = path.getParentPath();

        if (parentPath == null)
            fireChildrenChanged(path, null, null);
        else {
            Object parent = parentPath.getLastPathComponent();

            fireChildChanged(parentPath, treeModel
                    .getIndexOfChild(parent, node), node);
        }
    }

    /**
     * Notifies registered TreeModelListeners that the given child of
     * the node identified by the given parent path has been modified.
     * The parent path must not be null, nor empty nor contain null
     * elements.
     *
     * @param parentPath the path to the parent of the modified children.
     * @param index the position of the child
     * @param child child node that has been modified, must not be null
     */
    public void fireChildChanged(TreePath parentPath, int index, Object child) {
        fireChildrenChanged(parentPath, new int[] { index },
                new Object[] { child });
    }

    /**
     * Notifies registered TreeModelListeners that the given children of
     * the node identified by the given parent path have been modified.
     * The parent path must not be null, nor empty nor contain null
     * elements. Note that the index array must contain the position of the
     * corresponding child in the the children array. The indices must be in
     * ascending order. <p>
     *
     * The exception to these rules is if the root itself has been
     * modified (which has no parent by definition). In this case
     * the path must be the path to the root and both indices and children
     * arrays must be null.
     *
     * @param parentPath the path to the parent of the modified children.
     * @param indices the positions of the modified children
     * @param children the modified children
     */
    public void fireChildrenChanged(TreePath parentPath, int[] indices,
            Object[] children) {
        Contract.asNotNull(parentPath.getPath(),
                "path must not be null and must not contain null elements");
        Object[] pairs = listeners.getListenerList();

        TreeModelEvent e = null;

        for (int i = pairs.length - 2; i >= 0; i -= 2) {
            if (pairs[i] == TreeModelListener.class) {
                if (e == null)
                    e = createTreeModelEvent(parentPath, indices, children);

                ((TreeModelListener) pairs[i + 1]).treeNodesChanged(e);
            }
        }
    }

//------------------------ mutations (insert/remove nodes)

    /**
     * Notifies registered TreeModelListeners that the child has been added to
     * the the node identified by the given parent path at the given position.
     * The parent path must not be null, nor empty nor contain null elements.
     *
     * @param parentPath the path to the parent of added child.
     * @param index the position of the added children
     * @param child the added child
     */
    public void fireChildAdded(TreePath parentPath, int index, Object child) {
        fireChildrenAdded(parentPath, new int[] { index },
                new Object[] { child });
    }

    /**
     * Notifies registered TreeModelListeners that the child has been removed
     * from the node identified by the given parent path from the given position.
     * The parent path must not be null, nor empty nor contain null elements.
     *
     * @param parentPath the path to the parent of removed child.
     * @param index the position of the removed children before the removal
     * @param child the removed child
     */
    public void fireChildRemoved(TreePath parentPath, int index, Object child) {
        fireChildrenRemoved(parentPath, new int[] { index },
                new Object[] { child });
    }

    /**
     * Notifies registered TreeModelListeners that the given children have been
     * added to the the node identified by the given parent path at the given
     * locations. The parent path and the child array must not be null, nor
     * empty nor contain null elements. Note that the index array must contain
     * the position of the corresponding child in the the children array. The
     * indices must be in ascending order.
     * <p>
     *
     * @param parentPath the path to the parent of the added children.
     * @param indices the positions of the added children.
     * @param children the added children.
     */
    public void fireChildrenAdded(TreePath parentPath, int[] indices,
            Object[] children) {
        Object[] pairs = listeners.getListenerList();

        TreeModelEvent e = null;

        for (int i = pairs.length - 2; i >= 0; i -= 2) {
            if (pairs[i] == TreeModelListener.class) {
                if (e == null)
                    e = createTreeModelEvent(parentPath, indices, children);

                ((TreeModelListener) pairs[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Notifies registered TreeModelListeners that the given children have been
     * removed to the the node identified by the given parent path from the
     * given locations. The parent path and the child array must not be null,
     * nor empty nor contain null elements. Note that the index array must
     * contain the position of the corresponding child in the the children
     * array. The indices must be in ascending order.
     * <p>
     *
     * @param parentPath the path to the parent of the removed children.
     * @param indices the positions of the removed children before the removal
     * @param children the removed children
     */
    public void fireChildrenRemoved(TreePath parentPath, int[] indices,
            Object[] children) {
        Object[] pairs = listeners.getListenerList();

        TreeModelEvent e = null;

        for (int i = pairs.length - 2; i >= 0; i -= 2) {
            if (pairs[i] == TreeModelListener.class) {
                if (e == null)
                    e = createTreeModelEvent(parentPath, indices, children);
                ((TreeModelListener) pairs[i + 1]).treeNodesRemoved(e);
            }
        }
    }

//------------------- factory methods of TreeModelEvents

    /**
     * Creates and returns a TreeModelEvent for structureChanged
     * event notification. The given path may be null to indicate
     * setting a null root. In all other cases, the first path element
     * must contain the root and the last path element the rootNode of the
     * structural change. Specifically, a TreePath with a single element
     * (which is the root) denotes a structural change of the complete tree.
     *
     * @param parentPath the path to the root of the changed structure,
     *   may be null to indicate setting a null root.
     * @return a TreeModelEvent for structureChanged notification.
     *
     * @see javax.swing.event.TreeModelEvent
     * @see javax.swing.event.TreeModelListener
     */
    private TreeModelEvent createStructureChangedEvent(TreePath parentPath) {
        return createTreeModelEvent(parentPath, null, null);
    }

    /**
     * Creates and returns a TreeModelEvent for changed/inserted/removed
     * event notification.
     *
     * @param parentPath path to parent of modified node
     * @param indices the indices of the modified children (before the change)
     * @param children the array of modified children
     * @return a TreeModelEvent for changed/inserted/removed notification
     *
     * @see javax.swing.event.TreeModelEvent
     * @see javax.swing.event.TreeModelListener
     */
    private TreeModelEvent createTreeModelEvent(TreePath parentPath,
            int[] indices, Object[] children) {
        return new TreeModelEvent(treeModel, parentPath, indices, children);
    }

//------------------------ handling listeners

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(TreeModelListener.class, l);
    }

    public TreeModelListener[] getTreeModelListeners() {
        return listeners.getListeners(TreeModelListener.class);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(TreeModelListener.class, l);
    }
}
