/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package com.tulskiy.keymaster.windows;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.win32.W32APIOptions;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: 6/15/11
 */
public class User32 {
    static {
        Native.register(NativeLibrary.getInstance("user32", W32APIOptions.DEFAULT_OPTIONS));
    }

    public static final int MOD_ALT = 0x0001;
    public static final int MOD_CONTROL = 0x0002;
    public static final int MOD_NOREPEAT = 0x4000;
    public static final int MOD_SHIFT = 0x0004;
    public static final int MOD_WIN = 0x0008;
    public static final int WM_HOTKEY = 0x0312;
    public static final int VK_MEDIA_NEXT_TRACK = 0xB0;
    public static final int VK_MEDIA_PREV_TRACK = 0xB1;
    public static final int VK_MEDIA_STOP = 0xB2;
    public static final int VK_MEDIA_PLAY_PAUSE = 0xB3;
    public static final int PM_REMOVE = 0x0001;

    public static native boolean RegisterHotKey(Pointer hWnd, int id, int fsModifiers, int vk);

    public static native boolean UnregisterHotKey(Pointer hWnd, int id);

    public static native boolean PeekMessage(MSG lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg);

}
