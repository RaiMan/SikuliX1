/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

/**
 * An interface to describe an object that is capable of painting with an alpha value.
 *
 * @author kschaefer
 */
interface AlphaPaintable {
    /**
     * Get the current alpha value.
     *
     * @return the alpha translucency level for this component. This will be a value between 0 and
     *         1, inclusive.
     */
    float getAlpha();

    /**
     * Set the alpha transparency level for this component. This automatically causes a repaint of
     * the component.
     *
     * @param alpha
     *            must be a value between 0 and 1 inclusive
     * @throws IllegalArgumentException
     *             if the value is invalid
     */
    void setAlpha(float alpha);

    /**
     * Returns the state of the panel with respect to inheriting alpha values.
     *
     * @return {@code true} if this panel inherits alpha values; {@code false}
     *         otherwise
     * @see #setInheritAlpha(boolean)
     */
    boolean isInheritAlpha();

    /**
     * Determines if the effective alpha of this component should include the
     * alpha of ancestors.
     *
     * @param inheritAlpha
     *            {@code true} to include ancestral alpha data; {@code false}
     *            otherwise
     * @see #isInheritAlpha()
     * @see #getEffectiveAlpha()
     */
    void setInheritAlpha(boolean inheritAlpha);

    /**
     * Unlike other properties, alpha can be set on a component, or on one of
     * its parents. If the alpha of a parent component is .4, and the alpha on
     * this component is .5, effectively the alpha for this component is .4
     * because the lowest alpha in the hierarchy &quot;wins.&quot;
     *
     * @return the lowest alpha value in the hierarchy
     */
    float getEffectiveAlpha();
}
