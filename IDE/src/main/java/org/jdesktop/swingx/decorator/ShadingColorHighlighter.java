/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Color;
import java.awt.Component;

/**
 * Experimental replacement of HierarchicalColumnHighligher.
 * Darkens the component's background.
 *
 * @author Jeanette Winzenburg
 */
public class ShadingColorHighlighter extends ColorHighlighter {

    /**
     * Instantiates a Highlighter with null colors using the default
     * HighlightPredicate.
     *
     */
    public ShadingColorHighlighter() {
        this(null);
    }

    /**
     * Instantiates a Highlighter with null colors using the specified
     * HighlightPredicate.
     *
     * @param predicate the HighlightPredicate to use.
     */
    public ShadingColorHighlighter(HighlightPredicate predicate) {
        super(predicate, null, null);
    }

    /**
     * Applies a suitable background for the renderer component within the
     * specified adapter. <p>
     *
     * This implementation applies its a darkened background to an unselected
     * adapter. Does nothing for selected cells.
     *
     * @param renderer the cell renderer component that is to be decorated
     * @param adapter the ComponentAdapter for this decorate operation
     */
    @Override
    protected void applyBackground(Component renderer, ComponentAdapter adapter) {
        if (adapter.isSelected())
            return;
        // PENDING JW: really? That would be applying a absolute color, instead
        // of shading whatever the renderer has.
        Color background = getBackground();
        if (background == null) {
            background = renderer.getBackground();
        }
        // Change to the following
//        Color background = renderer.getBackground();
        if (background != null) {
            renderer.setBackground(computeBackgroundSeed(background));
        }
    }

    protected Color computeBackgroundSeed(Color seed) {
        return new Color(Math.max((int) (seed.getRed() * 0.95), 0), Math.max(
                (int) (seed.getGreen() * 0.95), 0), Math.max((int) (seed
                .getBlue() * 0.95), 0));
    }

}
