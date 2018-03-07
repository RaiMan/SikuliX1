/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.error;

import java.util.EventListener;

/**
 * ErrorListener defines the interface for an object which listens to errors generated
 * by a JX Swing component. ErrorEvents are only generated for internal un-recoverable errors
 * that cannot be thrown. An example would be an internal Action implementation that cannot
 * throw an Exception directly because the ActionListener interface forbids it. Exceptions
 * which can be throw directly (say from the constructor of the JX component) should not use
 * the ErrorListener mechanism.
 *
 * @see ErrorEvent
 * @see ErrorSupport
 * @author Joshua Marinacci joshua.marinacci@sun.com
 */
public interface ErrorListener extends EventListener {

    /**
     * Tells listeners that an error has occured within the watched component.
     * @param event
     */
    public void errorOccured(ErrorEvent event);
}
