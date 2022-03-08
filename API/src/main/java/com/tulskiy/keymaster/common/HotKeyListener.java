/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package com.tulskiy.keymaster.common;

import java.util.EventListener;

/**
 * Author: Denis Tulskiy
 * Date: 6/21/11
 */
public interface HotKeyListener extends EventListener {
    public void onHotKey(HotKey hotKey);
}
