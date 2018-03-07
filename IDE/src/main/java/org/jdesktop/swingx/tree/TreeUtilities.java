/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Contains convenience classes/methods for handling hierarchical Swing structures.
 *
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeUtilities {

    /**
     * An enumeration that is always empty.
     */
    public static final Enumeration EMPTY_ENUMERATION
        = new Enumeration() {
            @Override
            public boolean hasMoreElements() { return false; }
            @Override
            public Object nextElement() {
                throw new NoSuchElementException("No more elements");
            }
    };

    /**
     * Implementation of a preorder traversal of a TreeModel.
     */
    public static class PreorderModelEnumeration implements Enumeration {
        protected Deque<Enumeration> stack;
        protected TreeModel model;
        // the last component is the current subtree to travers
        private TreePath path;

        /**
         * Instantiates a preorder traversal starting from the root of the
         * TreeModel.
         *
         * @param model the TreeModel to travers.
         */
        public PreorderModelEnumeration(TreeModel model) {
            this(model, model.getRoot());
        }

        /**
         * Instantiates a preorder traversal of the TreeModel which
         * starts at the given node. It iterates over all nodes of the
         * subtree, only.
         *
         * @param model the TreeModel to travers.
         * @param node the node to start
         */
        public PreorderModelEnumeration(TreeModel model, Object node) {
            this.model = model;
            stack = new ArrayDeque<Enumeration>();
            pushNodeAsEnumeration(node);
        }

        /**
         * Instantiates a preorder traversal of the TreeModel which starts at the
         * last path component of the given TreePath. It iterates over all nodes
         * of the subtree and all of its siblings, with the same end as a traversal
         * starting at the model's roolt would have.
         *
         * @param model the TreeModel to travers.
         * @param path the TreePath to start from
         */
        public PreorderModelEnumeration(TreeModel model, TreePath path) {
            this(model, path.getLastPathComponent());
            this.path = path;
        }

        @Override
        public boolean hasMoreElements() {
            return (!stack.isEmpty() && stack.peek().hasMoreElements());
        }

        @Override
        public Object nextElement() {
            Enumeration enumer = stack.peek();
            Object  node = enumer.nextElement();
            Enumeration children = children(model, node);

            if (!enumer.hasMoreElements()) {
                stack.pop();
            }
            if (children.hasMoreElements()) {
                stack.push(children);
            }
            if (!hasMoreElements()) {
                // check if there are more subtrees to travers
                // and update internal state accordingly
                updateSubtree();
            }
            return node;
        }

        /**
         *
         */
        private void updateSubtree() {
            if (path == null) return;
            TreePath parentPath = path.getParentPath();
            if (parentPath == null) {
                // root
                path = null;
                return;
            }
            Object parent = parentPath.getLastPathComponent();
            Object currentNode = path.getLastPathComponent();
            int currentIndex = model.getIndexOfChild(parent, currentNode);
            if (currentIndex +1 < model.getChildCount(parent)) {
                // use sibling
                Object child = model.getChild(parent, currentIndex + 1);
                path = parentPath.pathByAddingChild(child);
                pushNodeAsEnumeration(child);
            } else {
                path = parentPath;
                // up one level
                updateSubtree();
            }
        }

        private void pushNodeAsEnumeration(Object node) {
            // single element enum
            Vector v = new Vector(1);
            v.add(node);
            stack.push(v.elements()); //children(model));
        }

    }  // End of class PreorderEnumeration

    /**
     * Implementation of a breadthFirst traversal of a subtree in a TreeModel.
     */
    public static class BreadthFirstModelEnumeration implements Enumeration {
        protected Queue<Enumeration> queue;
        private TreeModel model;

        public BreadthFirstModelEnumeration(TreeModel model) {
            this(model, model.getRoot());
        }

        public BreadthFirstModelEnumeration(TreeModel model, Object node) {
            this.model = model;
            // Vector is just used for getting an Enumeration easily
            Vector v = new Vector(1);
            v.addElement(node);
            queue = new ArrayDeque<Enumeration>();
            queue.offer(v.elements());
        }

        @Override
        public boolean hasMoreElements() {
            return !queue.isEmpty() &&
                    queue.peek().hasMoreElements();
        }

        @Override
        public Object nextElement() {
            // look at head
            Enumeration enumer = queue.peek();
            Object node = enumer.nextElement();
            Enumeration children = children(model, node);

            if (!enumer.hasMoreElements()) {
                // remove head
                queue.poll();
            }
            if (children.hasMoreElements()) {
                // add at tail
                queue.offer(children);
            }
            return node;
        }

    }  // End of class BreadthFirstEnumeration

    /**
     * Implementation of a postorder traversal of a subtree in a TreeModel.
     */
    public static class PostorderModelEnumeration implements Enumeration {
        protected TreeModel model;
        protected Object root;
        protected Enumeration children;
        protected Enumeration subtree;

        public PostorderModelEnumeration(TreeModel model) {
            this(model, model.getRoot());
        }

        public PostorderModelEnumeration(TreeModel model, Object node) {
            this.model = model;
            root = node;
            children = children(model, root);
            subtree = EMPTY_ENUMERATION;
        }

        @Override
        public boolean hasMoreElements() {
            return root != null;
        }

        @Override
        public Object nextElement() {
            Object retval;

            if (subtree.hasMoreElements()) {
                retval = subtree.nextElement();
            } else if (children.hasMoreElements()) {
                subtree = new PostorderModelEnumeration(model,
                        children.nextElement());
                retval = subtree.nextElement();
            } else {
                retval = root;
                root = null;
            }

            return retval;
        }

    }  // End of class PostorderEnumeration

    /**
     * Implementation of a preorder traversal of a subtree with nodes of type TreeNode.
     */
    public static class PreorderNodeEnumeration<M extends TreeNode> implements Enumeration<M> {
        protected Deque<Enumeration<M>> stack;

        public PreorderNodeEnumeration(M rootNode) {
            // Vector is just used for getting an Enumeration easily
            Vector<M> v = new Vector<M>(1);
            v.addElement(rootNode);
            stack = new ArrayDeque<Enumeration<M>>();
            stack.push(v.elements());
        }

        @Override
        public boolean hasMoreElements() {
            return (!stack.isEmpty() &&
                    stack.peek().hasMoreElements());
        }

        @Override
        public M nextElement() {
            Enumeration<M> enumer = stack.peek();
            M node = enumer.nextElement();
            Enumeration<M> children = getChildren(node);

            if (!enumer.hasMoreElements()) {
                stack.pop();
            }
            if (children.hasMoreElements()) {
                stack.push(children);
            }
            return node;
        }

        protected Enumeration<M> getChildren(M node) {
            Enumeration<M> children = (Enumeration<M>) node.children();
            return children;
        }

    }  // End of class PreorderEnumeration

    /**
     * Implementation of a postorder traversal of a subtree with nodes of type TreeNode.
     */
    public static class PostorderNodeEnumeration<M extends TreeNode> implements Enumeration<M> {
        protected M root;
        protected Enumeration<M> children;
        protected Enumeration<M> subtree;

        public PostorderNodeEnumeration(M rootNode) {
            super();
            root = rootNode;
            children = getChildren(rootNode);
            subtree = EMPTY_ENUMERATION;
        }
        @Override
        public boolean hasMoreElements() {
            return root != null;
        }

        @Override
        public M nextElement() {
            M retval;

            if (subtree.hasMoreElements()) {
                retval = subtree.nextElement();
            } else if (children.hasMoreElements()) {
                subtree = createSubTree(children.nextElement());
                retval = subtree.nextElement();
            } else {
                retval = root;
                root = null;
            }

            return retval;
        }

        /**
         * Creates and returns a PostorderEnumeration on the given node.
         *
         * @param node the node to create the PostorderEnumeration for
         * @return the PostorderEnumeration on the given node
         */
        protected PostorderNodeEnumeration<M> createSubTree(M node) {
            return new PostorderNodeEnumeration<M>(node);
        }

        /**
         * Returns an enumeration on the children of the root node.
         * @param node
         * @return
         */
        protected Enumeration<M> getChildren(M node) {
            return (Enumeration<M>) node.children();
        }

    }  // End of class PostorderEnumeration

    /**
     * Implementation of a breadthFirst traversal of a subtree with nodes of type TreeNode.
     */
    public static class BreadthFirstNodeEnumeration<M extends TreeNode> implements Enumeration<M> {
        protected Queue<Enumeration<M>> queue;

        public BreadthFirstNodeEnumeration(M rootNode) {
            // Vector is just used for getting an Enumeration easily
            Vector<M> v = new Vector<M>(1);
            v.addElement(rootNode);
            queue = new ArrayDeque<Enumeration<M>>();
            queue.offer(v.elements());
        }

        @Override
        public boolean hasMoreElements() {
            return !queue.isEmpty() &&
                    queue.peek().hasMoreElements();
        }

        @Override
        public M nextElement() {
            // look at head
            Enumeration<M> enumer = queue.peek();
            M node = enumer.nextElement();
            Enumeration<M> children = getChildren(node);

            if (!enumer.hasMoreElements()) {
                // remove head
                queue.poll();
            }
            if (children.hasMoreElements()) {
                // add at tail
                queue.offer(children);
            }
            return node;
        }

        protected Enumeration<M> getChildren(M node) {
            Enumeration<M> children = (Enumeration<M>) node.children();
            return children;
        }

    }  // End of class BreadthFirstEnumeration

    /**
     * Creates and returns an Enumeration across the direct children of the
     * rootNode in the given TreeModel.
     *
     * @param model the TreeModel which contains parent, must not be null
     * @return an Enumeration across the direct children of the model's root, the enumeration
     *    is empty if the root is null or contains no children
     */
    public static Enumeration children(TreeModel model) {
        return children(model, model.getRoot());
    }

    /**
     * Creates and returns an Enumeration across the direct children of parentNode
     * in the given TreeModel.
     *
     * @param model the TreeModel which contains parent, must not be null
     * @param parent the parent of the enumerated children
     * @return an Enumeration across the direct children of parent, the enumeration
     *    is empty if the parent is null or contains no children
     */
    public static Enumeration children(final TreeModel model, final Object parent) {
        if (parent == null || model.isLeaf(parent)) {
            return EMPTY_ENUMERATION;
        }
        Enumeration<?> e = new Enumeration() {

            int currentIndex = 0;
            @Override
            public boolean hasMoreElements() {
                return model.getChildCount(parent) > currentIndex;
            }

            @Override
            public Object nextElement() {
                return model.getChild(parent, currentIndex++);
            }

        };
        return e;
    }

    private TreeUtilities() {}

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TreeUtilities.class
            .getName());
}
