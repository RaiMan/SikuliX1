/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.auth;

import org.jdesktop.beans.AbstractBean;

/**
 * <b>UsernameStore</b> is a class that implements persistence of usernames
 *
 * @author Bino George
 * @author rbair
 */
public abstract class UserNameStore extends AbstractBean {
    /**
     * Gets the current list of users.
     */
    public abstract String[] getUserNames();
    /**
     */
    public abstract void setUserNames(String[] names);
    /**
     * lifecycle method for loading names from persistent storage
     */
    public abstract void loadUserNames();
    /**
     * lifecycle method for saving name to persistent storage
     */
    public abstract void saveUserNames();
    /**
     */
    public abstract boolean containsUserName(String name);

    /**
     * Add a username to the store.
     * @param userName
     */
    public abstract void addUserName(String userName);

    /**
     * Removes a username from the list.
     * @param userName
     */
    public abstract void removeUserName(String userName);
}
