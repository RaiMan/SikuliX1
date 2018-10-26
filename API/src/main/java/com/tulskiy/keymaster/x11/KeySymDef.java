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

/**
 * Author: Denis Tulskiy
 * Date: 6/13/11
 */
public class KeySymDef {
    public static final int XK_F1 = 0xffbe;
    public static final int XK_F2 = 0xffbf;
    public static final int XK_F3 = 0xffc0;
    public static final int XK_F4 = 0xffc1;
    public static final int XK_F5 = 0xffc2;
    public static final int XK_F6 = 0xffc3;
    public static final int XK_F7 = 0xffc4;
    public static final int XK_F8 = 0xffc5;
    public static final int XK_F9 = 0xffc6;
    public static final int XK_F10 = 0xffc7;
    public static final int XK_F11 = 0xffc8;
    public static final int XK_F12 = 0xffc9;

    public static final int XK_BackSpace = 0xff08;  /* Back space, back char */
    public static final int XK_Tab = 0xff09;
    public static final int XK_Linefeed = 0xff0a;  /* Linefeed, LF */
    public static final int XK_Clear = 0xff0b;
    public static final int XK_Return = 0xff0d;  /* Return, enter */
    public static final int XK_Pause = 0xff13;  /* Pause, hold */
    public static final int XK_Scroll_Lock = 0xff14;
    public static final int XK_Sys_Req = 0xff15;
    public static final int XK_Escape = 0xff1b;
    public static final int XK_Delete = 0xffff;  /* Delete, rubout */

    /* Keypad functions, keypad numbers cleverly chosen to map to ASCII */

    public static final int XK_KP_Space = 0xff80;  /* Space */
    public static final int XK_KP_Tab = 0xff89;
    public static final int XK_KP_Enter = 0xff8d;  /* Enter */
    public static final int XK_KP_F1 = 0xff91;  /* PF1, KP_A, ... */
    public static final int XK_KP_F2 = 0xff92;
    public static final int XK_KP_F3 = 0xff93;
    public static final int XK_KP_F4 = 0xff94;
    public static final int XK_KP_Home = 0xff95;
    public static final int XK_KP_Left = 0xff96;
    public static final int XK_KP_Up = 0xff97;
    public static final int XK_KP_Right = 0xff98;
    public static final int XK_KP_Down = 0xff99;
    public static final int XK_KP_Prior = 0xff9a;
    public static final int XK_KP_Page_Up = 0xff9a;
    public static final int XK_KP_Next = 0xff9b;
    public static final int XK_KP_Page_Down = 0xff9b;
    public static final int XK_KP_End = 0xff9c;
    public static final int XK_KP_Begin = 0xff9d;
    public static final int XK_KP_Insert = 0xff9e;
    public static final int XK_KP_Delete = 0xff9f;
    public static final int XK_KP_Equal = 0xffbd;  /* Equals */
    public static final int XK_KP_Multiply = 0xffaa;
    public static final int XK_KP_Add = 0xffab;
    public static final int XK_KP_Separator = 0xffac;  /* Separator, often comma */
    public static final int XK_KP_Subtract = 0xffad;
    public static final int XK_KP_Decimal = 0xffae;
    public static final int XK_KP_Divide = 0xffaf;

    public static final int XK_KP_0 = 0xffb0;
    public static final int XK_KP_1 = 0xffb1;
    public static final int XK_KP_2 = 0xffb2;
    public static final int XK_KP_3 = 0xffb3;
    public static final int XK_KP_4 = 0xffb4;
    public static final int XK_KP_5 = 0xffb5;
    public static final int XK_KP_6 = 0xffb6;
    public static final int XK_KP_7 = 0xffb7;
    public static final int XK_KP_8 = 0xffb8;
    public static final int XK_KP_9 = 0xffb9;

    /* Cursor control & motion */

    public static final int XK_Home = 0xff50;
    public static final int XK_Left = 0xff51;  /* Move left, left arrow */
    public static final int XK_Up = 0xff52;  /* Move up, up arrow */
    public static final int XK_Right = 0xff53;  /* Move right, right arrow */
    public static final int XK_Down = 0xff54;  /* Move down, down arrow */
    public static final int XK_Prior = 0xff55;  /* Prior, previous */
    public static final int XK_Page_Up = 0xff55;
    public static final int XK_Next = 0xff56;  /* Next */
    public static final int XK_Page_Down = 0xff56;
    public static final int XK_End = 0xff57;  /* EOL */
    public static final int XK_Begin = 0xff58;  /* BOL */

    public static final int XK_space = 0x0020;  /* U+0020 SPACE */
    public static final int XK_exclam = 0x0021;  /* U+0021 EXCLAMATION MARK */
    public static final int XK_quotedbl = 0x0022;  /* U+0022 QUOTATION MARK */
    public static final int XK_numbersign = 0x0023;  /* U+0023 NUMBER SIGN */
    public static final int XK_dollar = 0x0024;  /* U+0024 DOLLAR SIGN */
    public static final int XK_percent = 0x0025;  /* U+0025 PERCENT SIGN */
    public static final int XK_ampersand = 0x0026;  /* U+0026 AMPERSAND */
    public static final int XK_apostrophe = 0x0027;  /* U+0027 APOSTROPHE */
    public static final int XK_quoteright = 0x0027;  /* deprecated */
    public static final int XK_parenleft = 0x0028;  /* U+0028 LEFT PARENTHESIS */
    public static final int XK_parenright = 0x0029;  /* U+0029 RIGHT PARENTHESIS */
    public static final int XK_asterisk = 0x002a;  /* U+002A ASTERISK */
    public static final int XK_plus = 0x002b;  /* U+002B PLUS SIGN */
    public static final int XK_comma = 0x002c;  /* U+002C COMMA */
    public static final int XK_minus = 0x002d;  /* U+002D HYPHEN-MINUS */
    public static final int XK_period = 0x002e;  /* U+002E FULL STOP */
    public static final int XK_slash = 0x002f;  /* U+002F SOLIDUS */

    public static final int XK_colon = 0x003a;  /* U+003A COLON */
    public static final int XK_semicolon = 0x003b;  /* U+003B SEMICOLON */
    public static final int XK_less = 0x003c;  /* U+003C LESS-THAN SIGN */
    public static final int XK_equal = 0x003d;  /* U+003D EQUALS SIGN */
    public static final int XK_greater = 0x003e;  /* U+003E GREATER-THAN SIGN */
    public static final int XK_question = 0x003f;  /* U+003F QUESTION MARK */
    public static final int XK_at = 0x0040;  /* U+0040 COMMERCIAL AT */

    public static final int XK_bracketleft = 0x005b;  /* U+005B LEFT SQUARE BRACKET */
    public static final int XK_backslash = 0x005c;  /* U+005C REVERSE SOLIDUS */
    public static final int XK_bracketright = 0x005d;  /* U+005D RIGHT SQUARE BRACKET */
    public static final int XK_asciicircum = 0x005e;  /* U+005E CIRCUMFLEX ACCENT */
    public static final int XK_underscore = 0x005f;  /* U+005F LOW LINE */
    public static final int XK_grave = 0x0060;  /* U+0060 GRAVE ACCENT */

    public static final int XK_braceleft = 0x007b;  /* U+007B LEFT CURLY BRACKET */
    public static final int XK_bar = 0x007c;  /* U+007C VERTICAL LINE */
    public static final int XK_braceright = 0x007d;  /* U+007D RIGHT CURLY BRACKET */
    public static final int XK_asciitilde = 0x007e;  /* U+007E TILDE */

    public static final int XK_Print = 0xff61;  /* Insert, insert here */
    public static final int XK_Insert = 0xff63;  /* Insert, insert here */

    public static final int XF86XK_AudioPlay = 0x1008FF14;  /* Start playing of audio >   */
    public static final int XF86XK_AudioStop = 0x1008FF15;  /* Stop playing audio         */
    public static final int XF86XK_AudioPrev = 0x1008FF16;  /* Previous track             */
    public static final int XF86XK_AudioNext = 0x1008FF17;  /* Next track                 */
}
