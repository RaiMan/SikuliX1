/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package jxgrabkey;

/**
 * This Exception is thrown when another application already registered a hotkey
 * which JXGrabKey tried to register for itself.
 * X11 hotkeys can only be registered by one application at a time.
 * Because JXGrabKey registers the same hotkey with different combinations of
 * offending masks like scrolllock, numlock and capslock,
 * any of those registrations can be the cause of the conflict.
 * It is best to unregister the hotkey after receiving this exception.
 * Otherwise the hotkey may not work at all, or may not work with all mask combinations.
 *
 * @author subes
 */
public class HotkeyConflictException extends Exception {

    public HotkeyConflictException() {
        super();
    }

    public HotkeyConflictException(String message) {
        super(message);
    }

    public HotkeyConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public HotkeyConflictException(Throwable cause) {
        super(cause);
    }
}
