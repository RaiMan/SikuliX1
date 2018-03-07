/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.error;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * ErrorSupport provides support for managing error listeners.
 * @author Joshua Marinacci joshua.marinacci@sun.com
 * @see ErrorListener
 * @see ErrorEvent
 */
public class ErrorSupport {
    private List<ErrorListener> listeners;
    private Object source;

    /**
     * Creates a new instance of <CODE>ErrorSupport</CODE>
     * @param source The object which will fire the <CODE>ErrorEvent</CODE>s
     */
    public ErrorSupport(Object source) {
        this.source = source;
        listeners = new ArrayList<ErrorListener>();
    }

    /**
     * Add an ErrorListener
     * @param listener the listener to add
     */
    public void addErrorListener(ErrorListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove an error listener
     * @param listener the listener to remove
     */
    public void removeErrorListener(ErrorListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns an array of all the listeners which were added to the
     * <CODE>ErrorSupport</CODE> object with <CODE>addErrorListener()</CODE>.
     * @return all of the <CODE>ErrorListener</CODE>s added or an empty array if no listeners have been
     * added.
     */
    public ErrorListener[] getErrorListeners() {
        return listeners.toArray(null);
    }

    /**
     * Report that an error has occurred
     * @param throwable The <CODE>{@link Error}</CODE> or <CODE>{@link Exception}</CODE> which occured.
     */
    public void fireErrorEvent(final Throwable throwable) {
        final ErrorEvent evt = new ErrorEvent(throwable, source);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for(ErrorListener el : listeners) {
                    el.errorOccured(evt);
                }
            }
        });
    }

}
