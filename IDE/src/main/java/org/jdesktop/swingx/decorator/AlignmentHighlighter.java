/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A Highlighter which sets the horizontal alignment.
 *
 * @author Jeanette Winzenburg (slight cleanup)
 * @author original contributed by swingx member martinm1000
 */
public class AlignmentHighlighter extends AbstractHighlighter {
    private static final int defaultAlignment = SwingConstants.LEADING;
    private int alignment;

    /**
     * Instantiates a AlignmentHighlighter with default alignment LEADING. The Highlighter is
     * applied always.
     */
    public AlignmentHighlighter() {
        this(defaultAlignment);
    }

    /**
     * Instantiates a AlignmentHighlighter with the specified alignment. The Highlighter is
     * applied always.
     *
     * @param alignment the horizontal alignment to use.
     * @throws IllegalArgumentException if not one of the constants allowed as horizontal alignment,
     * that is one of LEADING, LEFT, CENTER, RIGHT, TRAILING
     */
    public AlignmentHighlighter(int alignment) {
        this(null, alignment);
    }

    /**
     * Instantiates a FontHighlighter with the given HighlightPredicate and default
     * horizontal alignement.
     *
     * @param predicate the HighlightPredicate to use, may be null to default to ALWAYS.
     */
    public AlignmentHighlighter(HighlightPredicate predicate) {
        this(predicate, defaultAlignment);
    }

    /**
     * Instantiates a FontHighlighter with the given HighlightPredicate and null Font.
     *
     * @param predicate the HighlightPredicate to use, may be null to default to ALWAYS.
     * @param alignment the horizontal alignment to use.
     * @throws IllegalArgumentException if not one of the constants allowed as horizontal alignment,
     * that is one of LEADING, LEFT, CENTER, RIGHT, TRAILING
     */
    public AlignmentHighlighter(HighlightPredicate predicate, int alignment) {
        super(predicate);
        this.alignment = checkHorizontalAlignment(alignment);
    }

    /**
     * Returns the alignment which is applied.
     * @return the alignment
     */
    public int getHorizontalAlignment() {
        return alignment;
    }

    /**
     * Sets the horizontal alignment to apply.
     *
     * @param alignment the horizontal alignment to set
     * @throws IllegalArgumentException if not one of the constants allowed as horizontal alignment,
     * that is one of LEADING, LEFT, CENTER, RIGHT, TRAILING
     */
    public void setHorizontalAlignment(int alignment) {
        if (getHorizontalAlignment() == alignment) return;
        this.alignment = checkHorizontalAlignment(alignment);
        fireStateChanged();
    }

    /**
     * Checks if the horizontal alignment is valid.
     *
     * @param alignment the horizontal alignment to check
     * @throws IllegalArgumentException if not one of the constants allowed as horizontal alignment,
     * that is one of LEADING, LEFT, CENTER, RIGHT, TRAILING
     */
    private int checkHorizontalAlignment(int alignment) {
        if ((alignment == SwingConstants.LEFT) ||
                (alignment == SwingConstants.CENTER) ||
                (alignment == SwingConstants.RIGHT) ||
                (alignment == SwingConstants.LEADING) ||
                (alignment == SwingConstants.TRAILING)) {
                return alignment;
            }
            else {
                throw new IllegalArgumentException("invalid horizontal alignment, expected one of "
                      + SwingConstants.LEFT + " / " + SwingConstants.CENTER +
                      " / " + SwingConstants.RIGHT + " / " + SwingConstants.LEADING +
                      " / " + SwingConstants.TRAILING + " but was: " + alignment);
            }
    }

    /**
     * {@inheritDoc} <p>
     *
     * Implemented to set the horizontal alignement of the rendering component.
     */
    @Override
    protected Component doHighlight(Component renderer, ComponentAdapter adapter) {
        if (renderer instanceof JLabel) {
            ((JLabel) renderer).setHorizontalAlignment(getHorizontalAlignment());
        } else if (renderer instanceof AbstractButton ) {
            ((AbstractButton) renderer).setHorizontalAlignment(getHorizontalAlignment());
        } else {
            ((JTextField) renderer).setHorizontalAlignment(getHorizontalAlignment());
        }
        return renderer;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Implemented to return true for components of type JLabel, AbstractButton or JTextField,
     * false otherwise.
     */
    @Override
    protected boolean canHighlight(Component component, ComponentAdapter adapter) {
        return (component instanceof JLabel)
            || (component instanceof AbstractButton)
            || (component instanceof JTextField)
            ;
    }
}
