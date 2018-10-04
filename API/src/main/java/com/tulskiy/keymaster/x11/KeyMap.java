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

package com.tulskiy.keymaster.x11;

import com.sun.jna.Pointer;
import com.tulskiy.keymaster.common.MediaKey;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.HashMap;

import static com.tulskiy.keymaster.x11.KeySymDef.*;
import static com.tulskiy.keymaster.x11.X11.*;
import static java.awt.event.KeyEvent.*;

/**
 * Author: Denis Tulskiy
 * Date: 6/13/11
 */
public class KeyMap {
    private static final HashMap<Integer, Integer> common = new HashMap<Integer, Integer>() {{
        put(VK_ESCAPE, XK_Escape);
        put(VK_BACK_SPACE, XK_BackSpace);
        put(VK_TAB, XK_Tab);
        put(VK_ENTER, XK_Return);
        put(VK_PAUSE, XK_Pause);
        put(VK_SCROLL_LOCK, XK_Scroll_Lock);
        put(VK_DELETE, XK_Delete);
        put(VK_CLEAR, XK_Clear);
        put(VK_HOME, XK_Home);
        put(VK_LEFT, XK_Left);
        put(VK_UP, XK_Up);
        put(VK_RIGHT, XK_Right);
        put(VK_DOWN, XK_Down);
        put(VK_PAGE_UP, XK_Page_Up);
        put(VK_PAGE_DOWN, XK_Page_Down);
        put(VK_END, XK_End);
        put(VK_BEGIN, XK_Begin);
        put(VK_KP_DOWN, XK_KP_Down);
        put(VK_KP_UP, XK_KP_Up);
        put(VK_KP_LEFT, XK_KP_Left);
        put(VK_KP_RIGHT, XK_KP_Right);
        put(VK_SPACE, XK_space);
        put(VK_EXCLAMATION_MARK, XK_exclam);
        put(VK_QUOTEDBL, XK_quotedbl);
        put(VK_NUMBER_SIGN, XK_numbersign);
        put(VK_DOLLAR, XK_dollar);
        put(VK_AMPERSAND, XK_ampersand);
        put(VK_LEFT_PARENTHESIS, XK_parenleft);
        put(VK_RIGHT_PARENTHESIS, XK_parenright);
        put(VK_ASTERISK, XK_asterisk);
        put(VK_PLUS, XK_plus);
        put(VK_COMMA, XK_comma);
        put(VK_MINUS, XK_minus);
        put(VK_PERIOD, XK_period);
        put(VK_SLASH, XK_slash);
        put(VK_COLON, XK_colon);
        put(VK_SEMICOLON, XK_semicolon);
        put(VK_LESS, XK_less);
        put(VK_EQUALS, XK_equal);
        put(VK_GREATER, XK_greater);
        put(VK_AT, XK_at);
        put(VK_BRACELEFT, XK_braceleft);
        put(VK_BRACERIGHT, XK_braceright);
        put(VK_BACK_SLASH, XK_backslash);
        put(VK_CIRCUMFLEX, XK_asciicircum);
        put(VK_UNDERSCORE, XK_underscore);
        put(VK_BACK_QUOTE, XK_grave);
        put(VK_DIVIDE, XK_KP_Divide);
        put(VK_MULTIPLY, XK_KP_Multiply);
        put(VK_INSERT, XK_Insert);
        put(VK_ADD, XK_KP_Add);
        put(VK_SUBTRACT, XK_KP_Subtract);
        put(VK_BEGIN, XK_KP_Begin);
        put(VK_PRINTSCREEN, XK_Print);
        put(XF86XK_AudioNext, XF86XK_AudioNext);
        put(XF86XK_AudioPlay, XF86XK_AudioPlay);
        put(XF86XK_AudioPrev, XF86XK_AudioPrev);
        put(XF86XK_AudioStop, XF86XK_AudioStop);
    }};

    public static byte getCode(KeyStroke keyStroke, Pointer display) {
        int code = keyStroke.getKeyCode();

        int ret = -1;
        if ((code >= VK_0 && code <= VK_9) || (code >= VK_A && code <= VK_Z)) {
            ret = code;
        } else if (code >= VK_F1 && code <= VK_F12) {
            ret = code - (VK_F1 - XK_F1);
        } else if (code >= VK_NUMPAD0 && code <= VK_NUMPAD9) {
            ret = code - (VK_NUMPAD0 - XK_KP_0);
        } else {
            Integer i = common.get(code);
            if (i != null) {
                ret = i;
            }
        }

        if (ret != -1) {
            return Lib.XKeysymToKeycode(display, ret);
        } else {
            return 0;
        }
    }

    public static byte getMediaCode(MediaKey mediaKey, Pointer display) {
        int code = 0;
        switch (mediaKey) {
            case MEDIA_NEXT_TRACK:
                code = XF86XK_AudioNext;
                break;
            case MEDIA_PLAY_PAUSE:
                code = XF86XK_AudioPlay;
                break;
            case MEDIA_PREV_TRACK:
                code = XF86XK_AudioPrev;
                break;
            case MEDIA_STOP:
                code = XF86XK_AudioStop;
                break;
        }
        return Lib.XKeysymToKeycode(display, code);
    }

    public static int getModifiers(KeyStroke keyCode) {
        int modifiers = 0;
        if ((keyCode.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
            modifiers |= ShiftMask;
        }
        if ((keyCode.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
            modifiers |= ControlMask;
        }
        if ((keyCode.getModifiers() & InputEvent.META_DOWN_MASK) != 0) {
            modifiers |= Mod4Mask;
        }
        if ((keyCode.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
            modifiers |= Mod1Mask;
        }
        return modifiers;
    }
}
