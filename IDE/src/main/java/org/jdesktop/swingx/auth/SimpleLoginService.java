/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.auth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of LoginService that simply matches
 * the username/password against a list of known users and their passwords.
 * This is useful for demos or prototypes where a proper login server is not available.
 *
 * <em>This Implementation is NOT secure. DO NOT USE this in a real application</em>
 * To make this implementation more secure, the passwords should be passed in and
 * stored as the result of a one way hash algorithm. That way an attacker cannot
 * simply read the password in memory to crack into the system.
 *
 * @author rbair
 */
public final class SimpleLoginService extends LoginService {
    private Map<String,char[]> passwordMap;

    /**
     * Creates a new SimpleLoginService based on the given password map.
     */
    public SimpleLoginService(Map<String,char[]> passwordMap) {
        if (passwordMap == null) {
            passwordMap = new HashMap<String,char[]>();
        }
        this.passwordMap = passwordMap;
    }

    /**
     * Attempts to authenticate the given username and password against the password map
     */
    @Override
    public boolean authenticate(String name, char[] password, String server) throws Exception {
        char[] p = passwordMap.get(name);
        return Arrays.equals(password, p);
    }
}
