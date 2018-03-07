/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

/**
 * Interface to mark renderers as "live". <p>
 *
 * PENDING: probably need methods to enabled/click taking a similar
 *   set of parameters as getXXComponent because the actual
 *   outcome might depend on the given value. If so, we'll need
 *   to extend the XXRenderer interfaces.
 *
 * @author Jeanette Winzenburg
 */
public interface RolloverRenderer {
    /**
     *
     * @return true if rollover effects are on and clickable.
     */
    boolean isEnabled();

    /**
     * Same as AbstractButton.doClick(). It's up to client
     * code to prepare the renderer's component before calling
     * this method.
     *
     */
    void doClick();
}
