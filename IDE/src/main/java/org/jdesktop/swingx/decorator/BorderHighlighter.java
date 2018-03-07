/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * A Highlighter that applies a border the the renderer component.
 *
 * The resulting border can be configured to
 * - ignore the component's border, set this highlighter's border
 * - compound of this highlighter's border and component border, with
 *   this highlighter's border either inner or outer.
 *
 * The default setting is compound outer.
 *
 */
public class BorderHighlighter extends AbstractHighlighter {

    private Border paddingBorder;
    private boolean inner;
    private boolean compound;

    /**
     *
     * Instantiates a BorderHighlighter with no padding. The
     * Highlighter is applied unconditionally.
     *
     */
    public BorderHighlighter() {
        this((HighlightPredicate) null, null);
    }

    /**
     *
     * Instantiates a BorderHighlighter with no padding, using the
     * given predicate.
     *
     * @param predicate the HighlightPredicate to use
     *
     */
    public BorderHighlighter(HighlightPredicate predicate) {
        this(predicate, null);
    }

    /**
     *
     * Instantiates a BorderHighlighter with the given padding. The
     * Highlighter is applied unconditionally.
     *
     * @param paddingBorder the border to apply as visual decoration.
     *
     */
    public BorderHighlighter(Border paddingBorder) {
        this(null, paddingBorder);
    }

    /**
     *
     * Instantiates a BorderHighlighter with the given padding,
     * HighlightPredicate and default compound property.
     * If the predicate is null, the highlighter
     * will be applied unconditionally.
     *
     * @param predicate the HighlightPredicate to use
     * @param paddingBorder the border to apply as visual decoration.
     *
     */
    public BorderHighlighter(HighlightPredicate predicate, Border paddingBorder) {
        this(predicate, paddingBorder, true);
    }

    /**
     *
     * Instantiates a BorderHighlighter with the given padding,
     * HighlightPredicate, compound property and default inner property.
     * If the predicate is null, the highlighter
     * will be applied unconditionally.
     *
     * @param predicate the HighlightPredicate to use
     * @param paddingBorder the border to apply as visual decoration.
     * @param compound the compound property.
     *
     */
    public BorderHighlighter(HighlightPredicate predicate,
            Border paddingBorder, boolean compound) {
        this(predicate, paddingBorder, compound, false);
    }

    /**
     *
     * Instantiates a BorderHighlighter with the given padding,
     * HighlightPredicate and compound property. If the predicate is null, the highlighter
     * will be applied unconditionally.
     *
     * @param predicate the HighlightPredicate to use
     * @param paddingBorder the border to apply as visual decoration.
     * @param compound the compound property
     * @param inner the inner property
     */
    public BorderHighlighter(HighlightPredicate predicate,
            Border paddingBorder, boolean compound, boolean inner) {
        super(predicate);
        this.paddingBorder = paddingBorder;
        this.compound = compound;
        this.inner = inner;
    }




    /**
     * {@inheritDoc}
     */
    @Override
    protected Component doHighlight(Component renderer, ComponentAdapter adapter) {
          ((JComponent) renderer).setBorder(compoundBorder(
                    ((JComponent) renderer).getBorder()));
         return renderer;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to prevent highlighting if there's no padding available or
     * the renderer is not of type JComponent.
     *
     * @param component the cell renderer component that is to be decorated
     * @param adapter the ComponentAdapter for this decorate operation
     * @return true padding is available and the
     *   renderer is a JComponent. False otherwise.
     */
    @Override
    protected boolean canHighlight(Component component, ComponentAdapter adapter) {
        return (getBorder() !=  null) && (component instanceof JComponent);
    }

    /**
     * Sets the compound property. If true, the highlight border will be compounded
     * with the renderer's border, if any. Otherwise, the highlight border will
     * replace the renderer's border.<p>
     *
     * The default value is true;
     *
     * @param compound a boolean indicating whether the highlight border should be
     *  compounded with the component's border.
     */
    public void setCompound(boolean compound) {
        if (isCompound() == compound) return;
        this.compound = compound;
        fireStateChanged();
    }

    /**
     *
     * @return the compound property.
     * @see #setCompound(boolean)
     */
    public boolean isCompound() {
        return compound;
    }

    /**
     * Sets the inner property. If true/false and compounded is enabled
     * the highlight border will be the inner/outer border of the compound.
     *
     * The default value is false;
     *
     * @param inner a boolean indicating whether the highlight border should be
     *  compounded as inner or outer border.
     *
     *  @see #isInner()
     */
    public void setInner(boolean inner) {
        if (isInner() == inner) return;
        this.inner = inner;
        fireStateChanged();
    }

    /**
     * Returns the inner property.
     *
     * @return the inner property.
     * @see #setInner(boolean)
     */
    public boolean isInner() {
        return inner;
    }

    /**
     * Sets the Border used for highlighting. <p>
     *
     * The default value is null.
     *
     * @param padding the Border to use
     */
    public void setBorder(Border padding) {
        if (areEqual(padding, getBorder())) return;
        this.paddingBorder = padding;
        fireStateChanged();
    }

    /**
     * Returns the border used for highlighing.<p>
     *
     * @return the border used to highlight.
     */
    public Border getBorder() {
        return paddingBorder;
    }

    /**
     * PRE: paddingBorder != null.
     * @param border
     * @return
     */
    private Border compoundBorder(Border border) {
        if (compound) {
            if (border != null) {
                if (inner) {
                    return BorderFactory.createCompoundBorder(border,
                            paddingBorder);
                }
                return BorderFactory.createCompoundBorder(paddingBorder,
                        border);
            }
        }
        return paddingBorder;
    }

}
