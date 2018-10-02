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

package com.tulskiy.keymaster.osx;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

import static com.tulskiy.keymaster.osx.Carbon.*;
import static java.awt.event.KeyEvent.*;

/**
 * Author: Denis Tulskiy
 * Date: 6/17/11
 */
public class KeyMap {
    private static Map<Integer, Integer> codes = new HashMap<Integer, Integer>() {{
        put(VK_BACK_QUOTE, 50);
        put(VK_1, 18);
        put(VK_2, 19);
        put(VK_3, 20);
        put(VK_4, 21);
        put(VK_5, 23);
        put(VK_6, 22);
        put(VK_7, 26);
        put(VK_8, 28);
        put(VK_9, 25);
        put(VK_0, 29);
        put(VK_MINUS, 27);
        put(VK_UNDERSCORE, 27);
        put(VK_PLUS, 24);
        put(VK_EQUALS, 24);
        put(VK_BACK_SPACE, 51);
        put(VK_TAB, 48);
        put(VK_Q, 12);
        put(VK_W, 13);
        put(VK_E, 14);
        put(VK_R, 15);
        put(VK_T, 17);
        put(VK_Y, 16);
        put(VK_U, 32);
        put(VK_I, 34);
        put(VK_O, 31);
        put(VK_P, 35);
        put(VK_BRACELEFT, 33);
        put(VK_BRACERIGHT, 30);
        put(VK_BACK_SLASH, 42);
        put(VK_A, 0);
        put(VK_S, 1);
        put(VK_D, 2);
        put(VK_F, 3);
        put(VK_G, 5);
        put(VK_H, 4);
        put(VK_J, 38);
        put(VK_K, 40);
        put(VK_L, 37);
        put(VK_COLON, 41);
        put(VK_SEMICOLON, 41);
        put(VK_QUOTE, 39);
        put(VK_ENTER, 36);
        put(VK_Z, 6);
        put(VK_X, 7);
        put(VK_C, 8);
        put(VK_V, 9);
        put(VK_B, 11);
        put(VK_N, 45);
        put(VK_M, 46);
        put(VK_COMMA, 43);
        put(VK_PERIOD, 47);
        put(VK_SLASH, 44);
        put(VK_SPACE, 49);
        put(VK_F1, 122);
        put(VK_F2, 120);
        put(VK_F3, 99);
        put(VK_F4, 118);
        put(VK_F5, 96);
        put(VK_F6, 97);
        put(VK_F7, 98);
        put(VK_F8, 100);
        put(VK_F9, 101);
        put(VK_F10, 109);
        put(VK_F11, 103);
        put(VK_F12, 111);
        put(VK_ESCAPE, 53);
        put(VK_INSERT, 114);
        put(VK_HELP, 114);
        put(VK_HOME, 115);
        put(VK_PAGE_UP, 116);
        put(VK_DELETE, 117);
        put(VK_END, 119);
        put(VK_PAGE_DOWN, 121);
        put(VK_UP, 126);
        put(VK_DOWN, 125);
        put(VK_LEFT, 123);
        put(VK_RIGHT, 124);
        put(VK_EQUALS, 81);
        put(VK_DIVIDE, 75);
        put(VK_MULTIPLY, 67);
        put(VK_SUBTRACT, 78);
        put(VK_ADD, 69);
        put(VK_SUBTRACT, 78);
        put(VK_NUMPAD0, 82);
        put(VK_NUMPAD1, 83);
        put(VK_NUMPAD2, 84);
        put(VK_NUMPAD3, 85);
        put(VK_NUMPAD4, 86);
        put(VK_NUMPAD5, 87);
        put(VK_NUMPAD6, 88);
        put(VK_NUMPAD7, 89);
        put(VK_NUMPAD8, 91);
        put(VK_NUMPAD9, 92);
        put(VK_DECIMAL, 65);
    }};

    public static int getKeyCode(KeyStroke keyStroke) {
        Integer ret = codes.get(keyStroke.getKeyCode());
        if (ret != null)
            return ret;
        else
            return -1;
    }

    public static int getModifier(KeyStroke keyStroke) {
        int modifiers = 0;
        if ((keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
            modifiers |= shiftKey;
        }
        if ((keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
            modifiers |= controlKey;
        }
        if ((keyStroke.getModifiers() & InputEvent.META_DOWN_MASK) != 0) {
            modifiers |= cmdKey;
        }
        if ((keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
            modifiers |= optionKey;
        }
        return modifiers;
    }
}
