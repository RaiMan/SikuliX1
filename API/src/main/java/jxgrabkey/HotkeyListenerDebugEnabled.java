/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package jxgrabkey;

/**
 * This listener handles debug messages aswell as hotkey events.
 * It can be used for custom logging needs.
 *
 * @author subes
 */
public interface HotkeyListenerDebugEnabled extends HotkeyListener {

    /**
     * This method is used to handle debug messages from JXGrabKey.
     * You need to enable debug to receive those.
     *
     * @param debugMessage
     */
    void debugCallback(String debugMessage);
}
