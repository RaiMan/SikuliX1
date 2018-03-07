/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.treetable;

/**
 * A default implementation of an {@code AbstractMutableTreeTableNode} that
 * returns {@code getUserObject().toString()} for all value queries. This
 * implementation is designed mainly for testing. It is NOT recommended to use
 * this implementation. Any user that needs to create {@code TreeTableNode}s
 * should consider directly extending {@code AbstractMutableTreeTableNode} or
 * directly implementing the interface.
 *
 * @author Karl Schaefer
 */
public class DefaultMutableTreeTableNode extends AbstractMutableTreeTableNode {

    /**
     *
     */
    public DefaultMutableTreeTableNode() {
        super();
    }

    /**
     * @param userObject
     */
    public DefaultMutableTreeTableNode(Object userObject) {
        super(userObject);
    }

    /**
     * @param userObject
     * @param allowsChildren
     */
    public DefaultMutableTreeTableNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int column) {
        return getUserObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditable(int column) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object aValue, int column) {
        setUserObject(aValue);
    }
}
