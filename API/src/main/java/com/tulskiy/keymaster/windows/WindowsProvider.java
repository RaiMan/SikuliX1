/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package com.tulskiy.keymaster.windows;

import com.sun.jna.platform.win32.WinUser.MSG;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.MediaKey;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static com.tulskiy.keymaster.windows.User32.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Author: Denis Tulskiy
 * Date: 6/12/11
 */
public class WindowsProvider extends Provider {
//    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsProvider.class);
    private static volatile int idSeq = 0;

    private boolean listen;
    private Boolean reset = false;
    private final Object lock = new Object();
    private Thread thread;

    private Map<Integer, HotKey> hotKeys = new HashMap<Integer, HotKey>();
    private Queue<HotKey> registerQueue = new LinkedList<HotKey>();

    public void init() {
        MSG msg = new MSG();
        
        Runnable runnable = new Runnable() {
            public void run() {
                //LOGGER.info("Starting Windows global hotkey provider");                
                listen = true;
                while (listen) {
                    while (PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                        if (msg.message == WM_HOTKEY) {
                            int id = msg.wParam.intValue();
                            HotKey hotKey = hotKeys.get(id);

                            if (hotKey != null) {
                                fireEvent(hotKey);
                            }
                        }
                    }

                    synchronized (lock) {
                        if (reset) {
                            //LOGGER.info("Reset hotkeys");
                            for (Integer id : hotKeys.keySet()) {
                                UnregisterHotKey(null, id);
                            }

                            hotKeys.clear();
                            reset = false;
                            lock.notify();
                        }

                        while (!registerQueue.isEmpty()) {
                            register(registerQueue.poll());
                        }
                        try {
                            lock.wait(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //LOGGER.info("Exit listening thread");
            }
        };

        thread = new Thread(runnable);
        thread.start();
    }

    private void register(HotKey hotKey) {
        int id = idSeq++;
        int code = KeyMap.getCode(hotKey);
        if (RegisterHotKey(null, id, KeyMap.getModifiers(hotKey.keyStroke), code)) {
            //LOGGER.info("Registering hotkey: " + hotKey);
            hotKeys.put(id, hotKey);
        } else {
            //LOGGER.warn("Could not register hotkey: " + hotKey);
        }
    }

    public void register(KeyStroke keyCode, HotKeyListener listener) {
        synchronized (lock) {
            registerQueue.add(new HotKey(keyCode, listener));
        }
    }

    public void register(MediaKey mediaKey, HotKeyListener listener) {
        synchronized (lock) {
            registerQueue.add(new HotKey(mediaKey, listener));
        }
    }

    public void reset() {
        synchronized (lock) {
            reset = true;
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        listen = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }
}
