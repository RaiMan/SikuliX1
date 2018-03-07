/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.error;

import java.util.logging.Level;

/**
 * <p>Extends {@link java.util.logging.Level} adding the <code>FATAL</code> error level.
 * Fatal errors are those unrecoverable errors that must result in the termination
 * of the application.</p>
 *
 * @status REVIEWED
 * @author rbair
 */
public class ErrorLevel extends Level {
    /**
     * FATAL is a message level indicating a catastrophic failure that should
     * result in the immediate termination of the application.
     * <p>
     * In general FATAL messages should describe events that are
     * of considerable critical and which will prevent
     * program execution.   They should be reasonably intelligible
     * to end users and to system administrators.
     * This level is initialized to <CODE>1100</CODE>.
     */
    public static final ErrorLevel FATAL = new ErrorLevel("FATAL", 1100);

    /** Creates a new instance of ErrorLevel */
    protected ErrorLevel(String name, int value) {
        super(name, value);
    }
}
