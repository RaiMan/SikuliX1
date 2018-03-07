/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.auth;

import java.util.EventListener;

/**
 * <b>LoginListener</b> provides a listener for the actual login
 * process.
 *
 * @author Bino George
 * @author Shai Almog
 */
public interface LoginListener extends EventListener {

    /**
     *  Called by the <strong>JXLoginPane</strong> in the event of a login failure
     *
     * @param source panel that fired the event
     */
    public void loginFailed(LoginEvent source);
    /**
     *  Called by the <strong>JXLoginPane</strong> when the Authentication
     *  operation is started.
     * @param source panel that fired the event
     */
    public void loginStarted(LoginEvent source);
    /**
     *  Called by the <strong>JXLoginPane</strong> in the event of a login
     *  cancellation by the user.
     *
     * @param source panel that fired the event
     */
    public void loginCanceled(LoginEvent source);
    /**
     *  Called by the <strong>JXLoginPane</strong> in the event of a
     *  successful login.
     *
     * @param source panel that fired the event
     */
    public void loginSucceeded(LoginEvent source);
}
