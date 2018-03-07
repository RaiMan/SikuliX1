/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.event;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

/**
 * Helper to listen to TreeExpansion events and notify with a remapped source.
 *
 * @author Jeanette Winzenburg
 */
public class TreeExpansionBroadcaster implements TreeExpansionListener {

    private Object source;

    private EventListenerList listeners;

    public TreeExpansionBroadcaster(Object source) {
        this.source = source;
    }

    public void addTreeExpansionListener(TreeExpansionListener l) {
        getEventListenerList().add(TreeExpansionListener.class, l);
    }

    public void removeTreeExpansionListener(TreeExpansionListener l) {
        if (!hasListeners())
            return;
        listeners.remove(TreeExpansionListener.class, l);
    }

    /**
     * @return
     */
    private EventListenerList getEventListenerList() {
        if (listeners == null) {
            listeners = new EventListenerList();
        }
        return listeners;
    }

    // -------------------- TreeExpansionListener
    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        if (!hasListeners())
            return;
        fireTreeExpanded(retarget(event));
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        if (!hasListeners())
            return;
        fireTreeCollapsed(retarget(event));
    }

    /**
     * @param event
     */
    private void fireTreeExpanded(TreeExpansionEvent event) {
        TreeExpansionListener[] ls = listeners
                .getListeners(TreeExpansionListener.class);
        for (int i = ls.length - 1; i >= 0; i--) {
            ls[i].treeExpanded(event);
        }
    }

    /**
     * @param event
     */
    private void fireTreeCollapsed(TreeExpansionEvent event) {
        TreeExpansionListener[] ls = listeners
                .getListeners(TreeExpansionListener.class);
        for (int i = ls.length - 1; i >= 0; i--) {
            ls[i].treeCollapsed(event);
        }
    }

    /**
     * @param event
     * @return
     */
    private TreeExpansionEvent retarget(TreeExpansionEvent event) {
        return new TreeExpansionEvent(source, event.getPath());
    }

    /**
     * @return
     */
    private boolean hasListeners() {
        return listeners != null && listeners.getListenerCount() > 0;
    }

}
