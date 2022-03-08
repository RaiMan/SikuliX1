/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package com.tulskiy.keymaster.common;

import com.sun.jna.Platform;
import com.tulskiy.keymaster.osx.CarbonProvider;
import com.tulskiy.keymaster.windows.WindowsProvider;
import com.tulskiy.keymaster.x11.X11Provider;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Main interface to global hotkey providers
 * <br>
 * Author: Denis Tulskiy
 * Date: 6/12/11
 */
public abstract class Provider {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Provider.class);

    static {
        System.setProperty("jna.nosys", "true");
    }

    private boolean useSwingEventQueue;

    /**
     * Get global hotkey provider for current platform
     *
     * @param useSwingEventQueue whether the provider should be using Swing Event queue or a regular thread
     * @return new instance of Provider, or null if platform is not supported
     * @see X11Provider
     * @see WindowsProvider
     * @see CarbonProvider
     */
    public static com.tulskiy.keymaster.common.Provider getCurrentProvider(boolean useSwingEventQueue) {
        com.tulskiy.keymaster.common.Provider provider;
        if (Platform.isX11()) {
            provider = new X11Provider();
        } else if (Platform.isWindows()) {
            provider = new WindowsProvider();
        } else if (Platform.isMac()) {
            provider = new CarbonProvider();
        } else {
            //LOGGER.warn("No suitable provider for " + System.getProperty("os.name"));
            return null;
        }
        provider.setUseSwingEventQueue(useSwingEventQueue);
        provider.init();
        return provider;

    }

    private ExecutorService eventQueue;


    /**
     * Initialize provider. Starts main thread that will listen to hotkey events
     */
    protected abstract void init();

    /**
     * Stop the provider. Stops main thread and frees any resources.
     * <br>
     * all hotkeys should be reset before calling this method
     *
     * @see com.tulskiy.keymaster.common.Provider#reset()
     */
    public void stop() {
        if (eventQueue != null)
            eventQueue.shutdown();
    }

    /**
     * Reset all hotkey listeners
     */
    public abstract void reset();

    /**
     * Register a global hotkey. Only keyCode and modifiers fields are respected
     *
     * @param keyCode  KeyStroke to register
     * @param listener listener to be notified of hotkey events
     * @see KeyStroke
     */
    public abstract void register(KeyStroke keyCode, HotKeyListener listener);

    /**
     * Register a media hotkey. Currently supported media keys are:
     * <br>
     * <ul>
     * <li>Play/Pause</li>
     * <li>Stop</li>
     * <li>Next track</li>
     * <li>Previous Track</li>
     * </ul>
     *
     * @param mediaKey media key to register
     * @param listener listener to be notified of hotkey events
     * @see MediaKey
     */
    public abstract void register(MediaKey mediaKey, HotKeyListener listener);

    /**
     * Helper method fro providers to fire hotkey event in a separate thread
     *
     * @param hotKey hotkey to fire
     */
    protected void fireEvent(HotKey hotKey) {
        HotKeyEvent event = new HotKeyEvent(hotKey);
        if (useSwingEventQueue) {
            SwingUtilities.invokeLater(event);
        } else {
            if (eventQueue == null) {
                eventQueue = Executors.newSingleThreadExecutor();
            }
            eventQueue.execute(event);
        }
    }

    public void setUseSwingEventQueue(boolean useSwingEventQueue) {
        this.useSwingEventQueue = useSwingEventQueue;
    }

    private class HotKeyEvent implements Runnable {
        private HotKey hotKey;

        private HotKeyEvent(HotKey hotKey) {
            this.hotKey = hotKey;
        }

        public void run() {
            hotKey.listener.onHotKey(hotKey);
        }
    }

}
