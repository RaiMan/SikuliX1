/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.event;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intended to be a replacement for {@link javax.swing.event.EventListenerList}.
 *
 * @author Joshua Outwater
 * @author Karl Schaefer
 * @see javax.swing.event.EventListenerList
 */
public class EventListenerMap {
    private final Map<Class<? extends EventListener>, List<? extends EventListener>> listenerList =
            new HashMap<Class<? extends EventListener>, List<? extends EventListener>>();

    /**
     * Returns a list containing all of the listeners managed by this {@code EventListenerMap}.
     *
     * @return all managed listeners
     */
    public List<EventListener> getListeners() {
        List<EventListener> listeners = new ArrayList<EventListener>();

        for (List<? extends EventListener> list : listenerList.values()) {
            listeners.addAll(list);
        }

        return listeners;
    }

    /**
     * Return a list of all the listeners of the given type.
     *
     * @return all of the listeners of the specified type.
     */
    @SuppressWarnings("unchecked")
    public <T extends EventListener> List<T> getListeners(Class<T> clazz) {
        List<T> list = (List<T>) listenerList.get(clazz);
        if (list == null) {
            list = new ArrayList<T>();
        }
        return list;
    }

    /**
     * Returns the total number of listeners of the supplied type
     * for this listener list.
     */
    public int getListenerCount() {
        int count = 0;

        for (List<? extends EventListener> list : listenerList.values()) {
            count += list.size();
        }

        return count;
    }

    /**
     * Returns the total number of listeners for this listener type.
     */
    @SuppressWarnings("unchecked")
    public <T extends EventListener> int getListenerCount(Class<T> clazz) {
        List<T> list = (List<T>) listenerList.get(clazz);
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    /**
     * Adds the listener as a listener of the specified type.
     *
     * @param <T>
     *            the type of the listener to be added
     * @param clazz
     *            the class type to add
     * @param l
     *            the listener to be added
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends EventListener> void add(Class<T> clazz, T listener) {
        if (listener == null) {
            return;
        }

        List<T> list = (List<T>) listenerList.get(clazz);
        if (list == null) {
            list = new ArrayList<T>();
            listenerList.put(clazz, list);
        }
        list.add(listener);
    }

    /**
     * Removes the listener as a listener of the specified type.
     *
     * @param <T>
     *            the type of the listener to remove
     * @param clazz
     *            the class type to remove
     * @param l
     *            the listener to remove
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends EventListener> void remove(Class<T> clazz, T listener) {
        if (listener == null) {
            return;
        }

        List<T> list = (List<T>) listenerList.get(clazz);
        if (list != null) {
            list.remove(listener);
        }
    }
}
