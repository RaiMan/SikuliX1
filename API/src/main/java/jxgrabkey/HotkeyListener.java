/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package jxgrabkey;

/**
 * This listener is used to handle hotkey events from externally.
 *
 * @author subes
 */
public interface HotkeyListener {

    /**
     * This method receives the hotkey events which are received by the main listen loop.
     *
     * @param id
     */
    void onHotkey(int id);
}
