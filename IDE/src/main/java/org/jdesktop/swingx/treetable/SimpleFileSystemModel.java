/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.treetable;

import java.io.File;
import java.util.Date;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

/**
 * A tree table model to simulate a file system.
 * <p>
 * This tree table model implementation does not extends
 * {@code AbstractTreeTableModel}. The file system metaphor demonstrates that
 * it is often easier to directly implement tree structures directly instead of
 * using intermediaries, such as {@code TreeNode}.
 * <p>
 * It would be possible to create this same class by extending
 * {@code AbstractTreeTableModel}, however the number of methods that you would
 * need to override almost precludes that means of implementation.
 * <p>
 * A "full" version of this model might allow editing of file names, the
 * deletion of files, and the movement of files. This simple implementation does
 * not intend to tackle such problems, but this implementation may be extended
 * to handle such details.
 *
 * @author Ramesh Gupta
 * @author Karl Schaefer
 */
public class SimpleFileSystemModel implements TreeTableModel {
    protected EventListenerList listenerList;

    // the returned file length for directories
    private static final Long ZERO = Long.valueOf(0);

    private File root;

    /**
     * Creates a file system model, using the root directory as the model root.
     */
    public SimpleFileSystemModel() {
        this(new File(File.separator));
    }

    /**
     * Creates a file system model, using the specified {@code root} as the
     * model root.
     */
    public SimpleFileSystemModel(File root) {
        this.root = root;
        this.listenerList = new EventListenerList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getChild(Object parent, int index) {
        if (parent instanceof File) {
            File parentFile = (File) parent;
            File[] files = parentFile.listFiles();

            if (files != null) {
                return files[index];
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof File) {
            String[] children = ((File) parent).list();

            if (children != null) {
                return children.length;
            }
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int column) {
        switch(column) {
        case 0:
            return String.class;
        case 1:
            return Long.class;
        case 2:
            return Boolean.class;
        case 3:
            return Date.class;
        default:
            return Object.class;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Name";
        case 1:
            return "Size";
        case 2:
            return "Directory";
        case 3:
            return "Modification Date";
        default:
            return "Column " + column;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof File) {
            File file = (File) node;
            switch (column) {
            case 0:
                return file.getName();
            case 1:
                return file.isFile() ? file.length() : ZERO;
            case 2:
                return file.isDirectory();
            case 3:
                return new Date(file.lastModified());
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHierarchicalColumn() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, Object node, int column) {
        //does nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof File && child instanceof File) {
            File parentFile = (File) parent;
            File[] files = parentFile.listFiles();

            for (int i = 0, len = files.length; i < len; i++) {
                if (files[i].equals(child)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getRoot() {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaf(Object node) {
        if (node instanceof File) {
            //do not use isFile(); some system files return false
            return ((File) node).list() == null;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        //does nothing
    }

    /**
     * Gets a an array of all the listeners attached to this model.
     *
     * @return an array of listeners; this array is guaranteed to be
     * non-{@code null}
     */
    public TreeModelListener[] getTreeModelListeners() {
        return listenerList.getListeners(TreeModelListener.class);
    }
}
