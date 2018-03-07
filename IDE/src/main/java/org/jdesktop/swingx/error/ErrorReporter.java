/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.error;

/**
 * <p>ErrorReporter is used by {@link org.jdesktop.swingx.JXErrorPane} to
 * implement a pluggable error reporting API. For example, a
 * <code>JXErrorPane</code> may use an <code>EmailErrorReporter</code>, or a
 * {@code LogErrorReporter}, or perhaps even an
 * <code>RSSErrorReporter</code>.</p>
 *
 * @status REVIEWED
 * @author Alexander Zuev
 * @author rbair
 */
public interface ErrorReporter {
    /**
     * <p>Reports an error based on the given {@link ErrorInfo}. This
     * method may be a long running method, and so should not block the EDT in
     * any way. If an error occurs while reporting the error, it <strong>must not</strong>
     * throw an exception from this method. If an error dialog causes another error,
     * it should be silently swallowed. If proper heuristics can be used, an attempt
     * can be made some time later to re-report failed error reports, but such attempts
     * should be transparent to the user.</p>
     *
     * @param info encapsulates all information to report using this facility. Must not be null.
     * @exception thrown if the info param is null
     */
    public void reportError(ErrorInfo info) throws NullPointerException;
}
