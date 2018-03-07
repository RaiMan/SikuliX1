/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.io.Serializable;

/**
 * A simple abstract class to handle common layout implementations. Package-private as we do NOT
 * want to export this as part of the public API.
 *
 * @author kschaefer
 */
abstract class AbstractLayoutManager implements LayoutManager, Serializable {
    private static final long serialVersionUID = 1446292747820044161L;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing.
     */
    @Override
    public void addLayoutComponent(String name, Component comp) {
        //does nothing
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing.
     */
    @Override
    public void removeLayoutComponent(Component comp) {
        // does nothing
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation defers to {@link #preferredLayoutSize(Container)}.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }
}
