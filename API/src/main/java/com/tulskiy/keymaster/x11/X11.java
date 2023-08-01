/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package com.tulskiy.keymaster.x11;

import com.sun.jna.*;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: 7/14/11
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface X11 extends Library {
    public static com.tulskiy.keymaster.x11.X11 Lib = (com.tulskiy.keymaster.x11.X11) Native.load("X11", com.tulskiy.keymaster.x11.X11.class);

    public static final int GrabModeAsync = 1;
    public static final int KeyPress = 2;
    public static final int KeyRelease = 3;

    public static final int ShiftMask = (1);
    public static final int LockMask = (1 << 1);
    public static final int ControlMask = (1 << 2);
    public static final int Mod1Mask = (1 << 3);
    public static final int Mod2Mask = (1 << 4);
    public static final int Mod3Mask = (1 << 5);
    public static final int Mod4Mask = (1 << 6);
    public static final int Mod5Mask = (1 << 7);

    public Pointer XOpenDisplay(String name);

    public NativeLong XDefaultRootWindow(Pointer display);

    public byte XKeysymToKeycode(Pointer display, long keysym);

    public int XGrabKey(Pointer display, int code, int modifiers, NativeLong root, int ownerEvents, int pointerMode, int keyBoardMode);

    public int XUngrabKey(Pointer display, int code, int modifiers, NativeLong root);

    public int XNextEvent(Pointer display, XEvent event);

    public int XPending(Pointer display);

    public int XCloseDisplay(Pointer display);

    public int XkbSetDetectableAutoRepeat(Pointer display, boolean detectable, Pointer supported_rtrn);

    public XErrorHandler XSetErrorHandler(XErrorHandler errorHandler);

    public int XGetErrorText(Pointer display, int code, byte[] buffer, int len);

    public interface XErrorHandler extends Callback {
        public int apply(Pointer display, XErrorEvent errorEvent);
    }

    public static class XEvent extends Union {
        public int type;
        public XKeyEvent xkey;
        public NativeLong[] pad = new NativeLong[24];
    }

    public static class XKeyEvent extends Structure {
        public int type;            // of event
        public NativeLong serial;   // # of last request processed by server
        public int send_event;      // true if this came from a SendEvent request
        public Pointer display;     // public Display the event was read from
        public NativeLong window;         // "event" window it is reported relative to
        public NativeLong root;           // root window that the event occurred on
        public NativeLong subwindow;      // child window
        public NativeLong time;     // milliseconds
        public int x, y;            // pointer x, y coordinates in event window
        public int x_root, y_root;  // coordinates relative to root
        public int state;           // key or button mask
        public int keycode;         // detail
        public int same_screen;     // same screen flag

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("type", "serial", "send_event", "display", "window", "root", "subwindow", "time",
                    "x", "y", "x_root", "y_root", "state", "keycode", "same_screen");
        }
    }

    public static class XErrorEvent extends Structure {
        public int type;
        public Pointer display;     // Display the event was read from
        public NativeLong resourceid;     // resource id
        public NativeLong serial;   // serial number of failed request
        public byte error_code;     // error code of failed request
        public byte request_code;   // Major op-code of failed request
        public byte minor_code;     // Minor op-code of failed request

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("type", "display", "serial", "error_code", "request_code", "minor_code", "resourceid");
        }
    }
}
