/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.util.Separator;

/**
 * Organizes components in a vertical layout.
 *
 * @author fred
 * @author Karl Schaefer
 */
@JavaBean
public class VerticalLayout extends AbstractLayoutManager {
    private static final long serialVersionUID = 5342270033773736441L;

    private int gap;

    /**
     * Creates a layout without a gap between components.
     */
    public VerticalLayout() {
        this(0);
    }

    /**
     * Creates a layout with the specified gap between components.
     *
     * @param gap
     *            the gap between components
     */
    //TODO should we allow negative gaps?
    public VerticalLayout(int gap) {
        this.gap = gap;
    }

    /**
     * The current gap to place between components.
     *
     * @return the current gap
     */
    public int getGap() {
        return gap;
    }

    /**
     * The new gap to place between components.
     *
     * @param gap
     *            the new gap
     */
    //TODO should we allow negative gaps?
    public void setGap(int gap) {
        this.gap = gap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension pref = new Dimension(0, 0);
        Separator<Integer> sep = new Separator<Integer>(0, gap);

        for (int i = 0, c = parent.getComponentCount(); i < c; i++) {
            Component m = parent.getComponent(i);

            if (m.isVisible()) {
                Dimension componentPreferredSize = parent.getComponent(i).getPreferredSize();
                pref.height += componentPreferredSize.height + sep.get();
                pref.width = Math.max(pref.width, componentPreferredSize.width);
            }
        }

        Insets insets = parent.getInsets();
        pref.width += insets.left + insets.right;
        pref.height += insets.top + insets.bottom;

        return pref;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        Dimension size = parent.getSize();
        int width = size.width - insets.left - insets.right;
        int height = insets.top;

        for (int i = 0, c = parent.getComponentCount(); i < c; i++) {
            Component m = parent.getComponent(i);
            if (m.isVisible()) {
                m.setBounds(insets.left, height, width, m.getPreferredSize().height);
                height += m.getSize().height + gap;
            }
        }
    }
}
