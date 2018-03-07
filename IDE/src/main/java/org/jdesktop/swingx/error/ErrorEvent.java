/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.error;

import java.util.EventObject;

/**
 * Defines an event which encapsulates an error which occurred in a JX Swing component
 * which supports ErrorListeners.
 *
 * @author Joshua Marinacci joshua.marinacci@sun.com
 * @see ErrorListener
 * @see ErrorSupport
 */
public class ErrorEvent extends EventObject {
    private Throwable throwable;

    /**
     * Creates a new instance of <CODE>ErrorEvent</CODE>
     * @param throwable The Error or Exception which occurred.
     * @param source The object which threw the Error or Exception
     */
    public ErrorEvent(Throwable throwable, Object source) {
        super(source);
        this.throwable = throwable;
    }

    /**
     * Gets the Error or Exception which occurred.
     * @return The Error or Exception which occurred.
     */
    public Throwable getThrowable() {
        return throwable;
    }

}
