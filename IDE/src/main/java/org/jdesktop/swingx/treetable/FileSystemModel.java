/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.treetable;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

/**
 * A tree table model to simulate a file system.
 * <p>
 * This tree table model implementation extends {@code AbstractTreeTableModel}.
 * The file system metaphor demonstrates that it is often easier to directly
 * implement tree structures directly instead of using intermediaries, such as
 * {@code TreeTableNode}.
 * <p>
 * A comparison of this class with {@code SimpleFileSystemModel}, shows that
 * extending {@code AbstractTreeTableModel} is often easier than creating a model
 * from scratch.
 * <p>
 * A "full" version of this model might allow editing of file names, the
 * deletion of files, and the movement of files. This simple implementation does
 * not intend to tackle such problems, but this implementation may be extended
 * to handle such details.
 *
 * @author Ramesh Gupta
 * @author Karl Schaefer
 */
public class FileSystemModel extends AbstractTreeTableModel {
    // The the returned file length for directories.
    private static final Long DIRECTORY = 0L;

    /**
     * Creates a file system model using the root directory as the model root.
     */
    public FileSystemModel() {
        this(new File(File.separator));
    }

    /**
     * Creates a file system model using the specified {@code root}.
     *
     * @param root
     *            the root for this model; this may be different than the root
     *            directory for a file system.
     */
    public FileSystemModel(File root) {
        super(root);
    }

    private boolean isValidFileNode(Object file) {
        boolean result = false;

        if (file instanceof File) {
            File f = (File) file;

            while (!result && f != null) {
                result = f.equals(root);

                f = f.getParentFile();
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getChild(Object parent, int index) {
        if (!isValidFileNode(parent)) {
            throw new IllegalArgumentException("parent is not a file governed by this model");
        }

        File parentFile = (File) parent;
        String[] children = parentFile.list();

        if (children != null) {
            return new File(parentFile, children[index]);
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
        switch (column) {
        case 0:
            return String.class;
        case 1:
            return Long.class;
        case 2:
            return Boolean.class;
        case 3:
            return Date.class;
        default:
            return super.getColumnClass(column);
        }
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

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
            return super.getColumnName(column);
        }
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof File) {
            File file = (File) node;
            switch (column) {
            case 0:
                return file.getName();
            case 1:
                return isLeaf(node) ? file.length() : DIRECTORY;
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
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof File && child instanceof File) {
            File parentFile = (File) parent;
            File[] files = parentFile.listFiles();

            Arrays.sort(files);

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
        return (File) root;
    }

    /**
     * Sets the root for this tree table model. This method will notify
     * listeners that a change has taken place.
     *
     * @param root
     *            the new root node to set
     */
    public void setRoot(File root) {
        this.root = root;

        modelSupport.fireNewRoot();
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
}
