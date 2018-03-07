/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.auth;

/**
 *
 * @author rbair
 */
public abstract class LoginAdapter implements LoginListener {
    /**
     * @inheritDoc
     */
    public void loginSucceeded(LoginEvent source) {}

    /**
     * @inheritDoc
     */
    public void loginStarted(LoginEvent source) {}

    /**
     * @inheritDoc
     */
    public void loginFailed(LoginEvent source) {}

    /**
     * @inheritDoc
     */
    public void loginCanceled(LoginEvent source) {}
}
