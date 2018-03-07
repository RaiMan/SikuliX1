/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.auth;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This is a singleton that marks the set of permissions for a given logged in user.
 * It is one of the optional results of a successful login operation.
 * The purpose of this class is to provide a central location and client side bridge
 * to the server side permissions and user roles (see J2EE role based authorization).
 * This class is used by gui widgets and actions to determine visibility and enabled
 * status and thus a UI can adapt itself to users with a lower set of privileges.
 *
 * This class is not meant as a secure barrier! It is only a thin layer to supplant the
 * server side permissions. This class can be compromized by the user and thus its purpose
 * is only to help UI flow and navigation and not to prevent attack against a client side
 * UI. A server implementation must ALWAYS recheck permissions sent by the client regardless
 * of the client.
 *
 * @author Shai Almog
 */
public class UserPermissions {
    private static final UserPermissions INSTANCE = new UserPermissions();
    private PropertyChangeSupport propertyChange = new PropertyChangeSupport(this);
    private String[] roles;

    /** Creates a new instance of UserPermissions */
    private UserPermissions() {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChange.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChange.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChange.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChange.removePropertyChangeListener(name, listener);
    }

    /**
     * Returns the singleton instance of this class. A singleton is used to simplify access for
     * the permissions from every point in the application.
     */
    public static UserPermissions getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the roles of the currently logged in user
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * Returns true if the user is in the given role (case sensitive).
     */
    public boolean isUserInRole(String role) {
        if(roles != null) {
            for(int iter = 0 ; iter < roles.length ; iter++) {
                if(roles[iter].equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the user is in one of the given roles (case sensitive).
     */
    public boolean isUserInARole(String[] roles) {
        for(int iter = 0 ; iter < roles.length ; iter++) {
            if(isUserInRole(roles[iter])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the user is in all of the given roles (case sensitive).
     */
    public boolean isUserInRoles(String[] roles) {
        for(int iter = 0 ; iter < roles.length ; iter++) {
            if(!isUserInRole(roles[iter])) {
                return false;
            }
        }
        return true;
    }

    void setRoles(String[] roles) {
        String[] oldValue = this.roles;
        this.roles = roles;
        propertyChange.firePropertyChange("roles", oldValue, roles);
    }
}
