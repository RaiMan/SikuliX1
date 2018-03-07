/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.auth;
import java.util.EventObject;

/**
 * This is an event object that is passed to login listener methods
 *
 * @author Shai Almog
 */
public class LoginEvent extends EventObject {
    private Throwable cause;

    public LoginEvent(Object source) {
        this(source, null);
    }

    /** Creates a new instance of LoginEvent */
    public LoginEvent(Object source, Throwable cause) {
        super(source);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}
