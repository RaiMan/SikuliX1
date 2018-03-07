/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.basic;

import static java.awt.event.KeyEvent.VK_CAPS_LOCK;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import org.jdesktop.beans.AbstractBean;

/**
 * A class for determining the state of the {@link java.awt.event.KeyEvent.VK_CAPS_LOCK CAPS LOCK
 * key}. It also supports notification when the locking state changes.
 * <p>
 * Although it is possible to use {@link Toolkit#getLockingKeyState(int)} to determine the current
 * state of the CAPS LOCK key, that method is not guaranteed to work on all platforms. This class
 * attempts to handle those shortfalls and provide an easy mechanism for listening to state changes.
 *
 * <pre>
 * CapsLockSupport cls = CapsLockSupport.getInstance();
 * // for get the current state of the caps lock key
 * boolean currentState = cls.isCapsLockEnabled();
 * // for listening to changes in the caps lock state
 * cls.addPropertyChangeListener(&quot;capsLockEnabled&quot;, myListener);
 * </pre>
 *
 * There is one special case to be aware of. If {@code CapsLockSupport} is not able to determine the
 * state of the CAPS LOCK key, then {@link #isInitialized()} will return {@code false} until it is
 * able to introspect a {@link KeyEvent} and determine the current locking state. If
 * {@code CapsLockSupport} must use delayed initialization, it will fire a property change to notify
 * listeners that it is now in an accurate state.
 *
 * @author kschaefer
 */
public final class CapsLockSupport extends AbstractBean implements KeyEventDispatcher {
    private boolean useToolkit;
    private boolean capsLockeEnabled;
    private boolean updateViaKeyEvent;

    private static class SingletonHolder {
        private static final CapsLockSupport INSTANCE = new CapsLockSupport();

        static {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(INSTANCE);
        }
    }

    private CapsLockSupport() {
        try {
            capsLockeEnabled = Toolkit.getDefaultToolkit().getLockingKeyState(VK_CAPS_LOCK);
            useToolkit = true;
            updateViaKeyEvent = false;
        } catch (UnsupportedOperationException e) {
            capsLockeEnabled = false;
            useToolkit = false;
            updateViaKeyEvent = true;
        }
    }

    /**
     * Gets the only instance of {@code CapsLockSupport}.
     *
     * @return the {@code CapsLockSupport} instance
     */
    public static CapsLockSupport getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Determines if {@code CapsLockSupport} is accurately synchronized with the state of the CAPS
     * LOCK key. When not initialized, {@link #isCapsLockEnabled()} will always return {@code false}
     * . {@code CapsLockSupport} will fail to initialize only if
     * {@code Toolkit#getLockingKeyState(int)} throws an exception; in that case, it will initialize
     * as soon as it receives a valid key event (that can be used to determine the current locking
     * state).
     *
     * @return {@code true} if {@code CapsLockSupport} accurately knows the state of the CAPS LOCK
     *         key
     */
    public boolean isInitialized() {
        return useToolkit || (useToolkit ^ updateViaKeyEvent);
    }

    /**
     * Determines the current state of the {@link java.awt.event.KeyEvent.VK_CAPS_LOCK CAPS LOCK key}.
     *
     * @return {@code true} if CAPS LOCK is enabled; {@code false} otherwise
     */
    public boolean isCapsLockEnabled() {
        if (useToolkit) {
            try {
                return Toolkit.getDefaultToolkit().getLockingKeyState(VK_CAPS_LOCK);
            } catch (UnsupportedOperationException shouldNeverHappen) {
                return capsLockeEnabled;
            }
        }

        return capsLockeEnabled;
    }

    void setCapsLockEnabled(boolean capsLockEnabled) {
        boolean oldValue = this.capsLockeEnabled;
        this.capsLockeEnabled = capsLockEnabled;
        firePropertyChange("capsLockEnabled", oldValue, this.capsLockeEnabled); //$NON-NLS-1$
    }

    // updateViaKeyEvent is use to find the initial state of the CAPS LOCK key when the Toolkit does
    // not support it
    /**
     * This is an implementation detail and should not be considered public.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            int keyCode = e.getKeyCode();

            if (keyCode == VK_CAPS_LOCK) {
                if (!updateViaKeyEvent) {
                    if (useToolkit) {
                        try {
                            setCapsLockEnabled(Toolkit.getDefaultToolkit().getLockingKeyState(VK_CAPS_LOCK));
                        } catch (UnsupportedOperationException shouldNeverHappen) {
                            setCapsLockEnabled(!capsLockeEnabled);
                        }
                    } else {
                        setCapsLockEnabled(!capsLockeEnabled);
                    }
                }
            } else if (updateViaKeyEvent && Character.isLetter(keyCode)) {
                if (keyCode == e.getKeyChar()) {
                    capsLockeEnabled = !e.isShiftDown();
                } else {
                    capsLockeEnabled = e.isShiftDown();
                }

                updateViaKeyEvent = false;
                firePropertyChange("initialized", false, true); //$NON-NLS-1$
            }
        }

        return false;
    }
}
