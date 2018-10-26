/*
 * Copyright (c) 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.keymaster.windows;

import com.sun.jna.*;
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

    @SuppressWarnings({"UnusedDeclaration"})
    public static class MSG extends Structure {
        public Pointer hWnd;
        public int message;
        public Parameter wParam;
        public Parameter lParam;
        public int time;
        public int x;
        public int y;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("hWnd", "message", "wParam", "lParam", "time", "x", "y");
        }
    }

    public static class Parameter extends IntegerType {
        @SuppressWarnings("UnusedDeclaration")
        public Parameter() {
            this(0);
        }

        public Parameter(long value) {
            super(Pointer.SIZE, value);
        }
    }
}
