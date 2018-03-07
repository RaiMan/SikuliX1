/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.auth;

/**
 *  PasswordStore specifies a mechanism to store passwords used to authenticate
 *  using the <strong>LoginService</strong>. The actual mechanism used
 *  to store the passwords is left up to the implementation.
 *
 * @author Bino George
 * @author Jonathan Giles
 */
public abstract class PasswordStore {
    /**
     *  Saves a password for future use.
     *
     *  @param username username used to authenticate.
     *  @param server server used for authentication
     *  @param password password to save. Password can't be null. Use empty array for empty password.
     */
    public abstract boolean set(String username, String server, char[] password);

    /**
     * Fetches the password for a given server and username.
     *  @param username username
     *  @param server server
     *  @return <code>null</code> if not found, a character array representing the password
     *  otherwise. Returned array can be empty if the password is empty.
     */
    public abstract char[] get(String username, String server);

    /**
     * This should attempt to remove the given username from the password store, as well as any associated password.
     * @param username The username to remove
     */
    public abstract void removeUserPassword(String username);
}
