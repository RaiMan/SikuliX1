/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package jxgrabkey;

import java.awt.event.KeyEvent;

/**
 * This class holds definitions for X11 keysyms. It can also convert AWT keys into X11 keysyms.
 *
 * These definitions are taken from the escher project (http://escher.sourceforge.net/).
 * They converted them from the original X11 definitions.
 */
public final class X11KeysymDefinitions {

    //Latin1 ******************************************************************
    public static final int SPACE = 0x020;
    public static final int EXCLAM = 0x021;
    public static final int QUOTE_DBL = 0x022;
    public static final int NUMBER_SIGN = 0x023;
    public static final int DOLLAR = 0x024;
    public static final int PERCENT = 0x025;
    public static final int AMPERSAND = 0x026;
    public static final int APOSTROPHE = 0x027;
    public static final int QUOTE_RIGHT = 0x027; /* deprecated */
    public static final int PAREN_LEFT = 0x028;
    public static final int PAREN_RIGHT = 0x029;
    public static final int ASTERISK = 0x02a;
    public static final int PLUS = 0x02b;
    public static final int COMMA = 0x02c;
    public static final int MINUS = 0x02d;
    public static final int PERIOD = 0x02e;
    public static final int SLASH = 0x02f;
    public static final int NUM_0 = 0x030;
    public static final int NUM_1 = 0x031;
    public static final int NUM_2 = 0x032;
    public static final int NUM_3 = 0x033;
    public static final int NUM_4 = 0x034;
    public static final int NUM_5 = 0x035;
    public static final int NUM_6 = 0x036;
    public static final int NUM_7 = 0x037;
    public static final int NUM_8 = 0x038;
    public static final int NUM_9 = 0x039;
    public static final int COLON = 0x03a;
    public static final int SEMICOLON = 0x03b;
    public static final int LESS = 0x03c;
    public static final int EQUAL = 0x03d;
    public static final int GREATER = 0x03e;
    public static final int QUESTION = 0x03f;
    public static final int AT = 0x040;
    public static final int A = 0x041;
    public static final int B = 0x042;
    public static final int C = 0x043;
    public static final int D = 0x044;
    public static final int E = 0x045;
    public static final int F = 0x046;
    public static final int G = 0x047;
    public static final int H = 0x048;
    public static final int I = 0x049;
    public static final int J = 0x04a;
    public static final int K = 0x04b;
    public static final int L = 0x04c;
    public static final int M = 0x04d;
    public static final int N = 0x04e;
    public static final int O = 0x04f;
    public static final int P = 0x050;
    public static final int Q = 0x051;
    public static final int R = 0x052;
    public static final int S = 0x053;
    public static final int T = 0x054;
    public static final int U = 0x055;
    public static final int V = 0x056;
    public static final int W = 0x057;
    public static final int X = 0x058;
    public static final int Y = 0x059;
    public static final int Z = 0x05a;
    public static final int BRACKET_LEFT = 0x05b;
    public static final int BACKSLASH = 0x05c;
    public static final int BRACKET_RIGHT = 0x05d;
    public static final int ASCII_CIRCUM = 0x05e;
    public static final int UNDERSCORE = 0x05f;
    public static final int GRAVE = 0x060;
    public static final int QUOTE_LEFT = 0x060; /* deprecated */

    public static final int A_SMALL = 0x061;
    public static final int B_SMALL = 0x062;
    public static final int C_SMALL = 0x063;
    public static final int D_SMALL = 0x064;
    public static final int E_SMALL = 0x065;
    public static final int F_SMALL = 0x066;
    public static final int G_SMALL = 0x067;
    public static final int H_SMALL = 0x068;
    public static final int I_SMALL = 0x069;
    public static final int J_SMALL = 0x06a;
    public static final int K_SMALL = 0x06b;
    public static final int L_SMALL = 0x06c;
    public static final int M_SMALL = 0x06d;
    public static final int N_SMALL = 0x06e;
    public static final int O_SMALL = 0x06f;
    public static final int P_SMALL = 0x070;
    public static final int Q_SMALL = 0x071;
    public static final int R_SMALL = 0x072;
    public static final int S_SMALL = 0x073;
    public static final int T_SMALL = 0x074;
    public static final int U_SMALL = 0x075;
    public static final int V_SMALL = 0x076;
    public static final int W_SMALL = 0x077;
    public static final int X_SMALL = 0x078;
    public static final int Y_SMALL = 0x079;
    public static final int Z_SMALL = 0x07a;

    public static final int BRACE_LEFT = 0x07b;
    public static final int BAR = 0x07c;
    public static final int BRACE_RIGHT = 0x07d;
    public static final int ASCII_TILDE = 0x07e;

    public static final int NO_BREAKSPACE = 0x0a0;;
    public static final int EXCLAM_DOWN = 0x0a1;
    public static final int CENT = 0x0a2;
    public static final int STERLING = 0x0a3;
    public static final int CURRENCY = 0x0a4;
    public static final int YEN = 0x0a5;
    public static final int BROKEN_BAR = 0x0a6;
    public static final int SECTION = 0x0a7;
    public static final int DIAERESIS = 0x0a8;
    public static final int COPYRIGHT = 0x0a9;
    public static final int ORDFEMININE = 0x0aa;
    public static final int GUILLEMOT_LEFT = 0x0ab; /* left angle quotation mark */
    public static final int NOT_SIGN = 0x0ac;
    public static final int HYPHEN = 0x0ad;
    public static final int REGISTERED = 0x0ae;
    public static final int MACRON = 0x0af;
    public static final int DEGREE = 0x0b0;
    public static final int PLUS_MINUS = 0x0b1;
    public static final int TWO_SUPERIOR = 0x0b2;
    public static final int THREE_SUPERIOR = 0x0b3;
    public static final int ACUTE = 0x0b4;
    public static final int MU = 0x0b5;
    public static final int PARAGRAPH = 0x0b6;
    public static final int PERIOD_CENTERED = 0x0b7;
    public static final int CEDILLA = 0x0b8;
    public static final int ONE_SUPERIOR = 0x0b9;
    public static final int MASCULINE = 0x0ba;
    public static final int GUILLEMOT_RIGHT = 0x0bb; /* right angle quotation mark */
    public static final int ONE_QUARTER = 0x0bc;
    public static final int ONE_HALF = 0x0bd;
    public static final int THREE_QUARTERS = 0x0be;
    public static final int QUESTION_DOWN = 0x0bf;
    public static final int A_GRAVE = 0x0c0;
    public static final int A_ACUTE = 0x0c1;
    public static final int A_CIRCUMFLEX = 0x0c2;
    public static final int A_TILDE = 0x0c3;
    public static final int A_DIAERESIS = 0x0c4;
    public static final int A_RING = 0x0c5;
    public static final int AE = 0x0c6;
    public static final int C_CEDILLA = 0x0c7;
    public static final int E_GRAVE = 0x0c8;
    public static final int E_ACUTE = 0x0c9;
    public static final int E_CIRCUMFLEX = 0x0ca;
    public static final int E_DIAERESIS = 0x0cb;
    public static final int I_GRAVE = 0x0cc;
    public static final int I_ACUTE = 0x0cd;
    public static final int I_CIRCUMFLEX = 0x0ce;
    public static final int I_DIAERESIS = 0x0cf;
    public static final int E_TH = 0x0d0;
    public static final int N_TILDE = 0x0d1;
    public static final int O_GRAVE = 0x0d2;
    public static final int O_ACUTE = 0x0d3;
    public static final int O_CIRCUMFLEX = 0x0d4;
    public static final int O_TILDE = 0x0d5;
    public static final int O_DIAERESIS = 0x0d6;
    public static final int MULTIPLY = 0x0d7;
    public static final int O_OBLIQUE = 0x0d8;
    public static final int O_SLASH = 0x0d9;
    public static final int U_ACUTE = 0x0da;;
    public static final int U_CIRCUMFLEX = 0x0db;
    public static final int U_DIAERESIS = 0x0dc;
    public static final int Y_ACUTE = 0x0dd;
    public static final int T_HORN = 0x0de;
    public static final int S_SHARP = 0x0df;

    public static final int A_GRAVE_SMALL = 0x0e0;
    public static final int A_ACUTE_SMALL = 0x0e1;
    public static final int A_CIRCUMFLEX_SMALL = 0x0e2;
    public static final int A_TILDE_SMALL = 0x0e3;
    public static final int A_DIAERESIS_SMALL = 0x0e4;
    public static final int A_RING_SMALL = 0x0e5;
    public static final int AE_SMALL = 0x0e6;
    public static final int C_CEDILLA_SMALL = 0x0e7;
    public static final int E_GRAVE_SMALL = 0x0e8;
    public static final int E_ACUTE_SMALL = 0x0e9;
    public static final int E_CIRCUMFLEX_SMALL = 0x0ea;
    public static final int E_DIAERESIS_SMALL = 0x0eb;
    public static final int I_GRAVE_SMALL = 0x0ec;
    public static final int I_ACUTE_SMALL = 0x0ed;
    public static final int I_CIRCUMFLEX_SMALL = 0x0ee;
    public static final int I_DIAERESIS_SMALL = 0x0ef;
    public static final int E_TH_SMALL = 0x0f0;
    public static final int N_TILDE_SMALL = 0x0f1;
    public static final int O_GRAVE_SMALL = 0x0f2;
    public static final int O_ACUTE_SMALL = 0x0f3;
    public static final int O_CIRCUMFLEX_SMALL = 0x0f4;
    public static final int O_TILDE_SMALL = 0x0f5;
    public static final int O_DIAERESIS_SMALL = 0x0f6;
    public static final int DIVISION_SMALL = 0x0f7;
    public static final int O_SLASH_SMALL = 0x0f8;
    public static final int O_OBLIQUE_SMALL = 0x0f9;
    public static final int UA_CUTE_SMALL = 0x0fa;;
    public static final int U_CIRCUMFLEX_SMALL = 0x0fb;
    public static final int U_DIAERESIS_SMALL = 0x0fc;
    public static final int YA_CUTE_SMALL = 0x0fd;
    public static final int T_HORN_SMALL = 0x0fe;
    public static final int Y_DIAERESIS_SMALL = 0x0ff;

    //Misc ********************************************************************
    public static final int VOID_SYMBOL = 0xffffff;

    /* TTY Functions, cleverly chosen to map to ascii, for convenience of
    * programming, but could have been arbitrary (at the cost of lookup
    * tables in client code).
    */

    public static final int BACKSPACE = 0xff08; /* back space, back char */
    public static final int TAB = 0xff09;
    public static final int LINEFEED = 0xff0a; /* linefeed, LF */
    public static final int CLEAR = 0xff0b;
    public static final int RETURN = 0xff0d; /* return, enter */
    public static final int PAUSE = 0xff13; /* pause, hold */
    public static final int SCROLL_LOCK = 0xff14;
    public static final int SYS_REQ = 0xff15;
    public static final int ESCAPE = 0xff1b;
    public static final int DELETE = 0xffff; /* delete, rubout */

    /* International & multi-key character composition. */

    public static final int MULTI_KEY = 0xff20; /* multi-key character compose */
    public static final int CODEINPUT = 0xff37;
    public static final int SINGLE_CANDIDATE = 0xff3c;
    public static final int MULTIPLE_CANDIDATE = 0xff3d;
    public static final int PREVIOUS_CANDIDATE = 0xff3e;


    /* Japanese keyboard support. 0xff31 thru 0xff3f are under XK_KOREAN. */

    public static final int KANJI = 0xff21; /* kanji, kanji convert */
    public static final int MUHENKAN = 0xff22; /* cancel conversion */
    public static final int HENKAN_MODE = 0xff23; /* start/stop conversion */
    public static final int HENKAN = 0xff23; /* alias for henkan_mode */
    public static final int ROMAJI = 0xff24; /* to romaji */
    public static final int HIRAGANA = 0xff25; /* to hiragana */
    public static final int KATAKANA = 0xff26; /* to katakana */
    public static final int HIRAGANA_KATAKANA = 0xff27; /* hiragana/katakana toggle */
    public static final int ZENKAKU = 0xff28; /* to zenkaku */
    public static final int HANKAKU = 0xff29; /* to hankaku */
    public static final int ZENKAKU_HANKAKU = 0xff2a; /* zenkaku/hankaku toggle */
    public static final int TOUROKU = 0xff2b; /* add to dictionary */
    public static final int MASSYO = 0xff2c; /* delete from dictionary */
    public static final int KANA_LOCK = 0xff2d; /* kana lock */
    public static final int KANA_SHIFT = 0xff2e; /* kana shift */
    public static final int EISU_SHIFT = 0xff2f; /* alphanumeric shift */
    public static final int EISU_TOGGLE = 0xff30; /* alphanumeric toggle */
    public static final int KANJI_BANGOU = 0xff37; /* codeinput */
    public static final int ZEN_KOHO = 0xff3d; /* multiple/all candidate(s) */
    public static final int MAE_KOHO = 0xff3e; /* previous candidate */

    /** Cursor control & motion. */

    public static final int HOME = 0xff50;
    public static final int LEFT = 0xff51; /* move left, left arrow */
    public static final int UP = 0xff52; /* move up, up arrow */
    public static final int RIGHT = 0xff53; /* move right, right arrow */
    public static final int DOWN = 0xff54; /* move down, down arrow */
    public static final int PRIOR = 0xff55; /* prior, previous */
    public static final int PAGE_UP = 0xff55;
    public static final int NEXT = 0xff56; /* next */
    public static final int PAGE_DOWN = 0xff56;
    public static final int END = 0xff57; /* eol */
    public static final int BEGIN = 0xff58; /* bol */

    /* Misc Functions. */

    public static final int SELECT = 0xff60; /* select, mark */
    public static final int PRINT = 0xff61;
    public static final int EXECUTE = 0xff62; /* execute, run, do */
    public static final int INSERT = 0xff63; /* insert, insert here */
    public static final int UNDO = 0xff65; /* undo, oops */
    public static final int REDO = 0xff66; /* redo, again */
    public static final int MENU = 0xff67;
    public static final int FIND = 0xff68; /* find, search */
    public static final int CANCEL = 0xff69; /* cancel, stop, abort, exit */
    public static final int HELP = 0xff6a; /* help */
    public static final int BREAK = 0xff6b;
    public static final int MODE_SWITCH = 0xff7e; /* character set switch */
    public static final int SCRIPT_SWITCH = 0xff7e; /* alias for mode_switch */
    public static final int NUM_LOCK = 0xff7f;

    /* Keypad Functions, keypad numbers cleverly chosen to map to ascii. */

    public static final int KP_SPACE = 0xff80; /* space */
    public static final int KP_TAB = 0xff89;
    public static final int KP_ENTER = 0xff8d; /* enter */
    public static final int KP_F1 = 0xff91; /* pf1, kp_a, ... */
    public static final int KP_F2 = 0xff92;
    public static final int KP_F3 = 0xff93;
    public static final int KP_F4 = 0xff94;
    public static final int KP_HOME = 0xff95;
    public static final int KP_LEFT = 0xff96;
    public static final int KP_UP = 0xff97;
    public static final int KP_RIGHT = 0xff98;
    public static final int KP_DOWN = 0xff99;
    public static final int KP_PRIOR = 0xff9a;
    public static final int KP_PAGE_UP = 0xff9a;
    public static final int KP_NEXT = 0xff9b;
    public static final int KP_PAGE_DOWN = 0xff9b;
    public static final int KP_END = 0xff9c;
    public static final int KP_BEGIN = 0xff9d;
    public static final int KP_INSERT = 0xff9e;
    public static final int KP_DELETE = 0xff9f;
    public static final int KP_EQUAL = 0xffbd; /* equals */
    public static final int KP_MULTIPLY = 0xffaa;
    public static final int KP_ADD = 0xffab;
    public static final int KP_SEPARATOR = 0xffac; /* separator, often comma */
    public static final int KP_SUBTRACT = 0xffad;
    public static final int KP_DECIMAL = 0xffae;
    public static final int KP_DIVIDE = 0xffaf;

    public static final int KP_0 = 0xffb0;;
    public static final int KP_1 = 0xffb1;
    public static final int KP_2 = 0xffb2;
    public static final int KP_3 = 0xffb3;
    public static final int KP_4 = 0xffb4;
    public static final int KP_5 = 0xffb5;
    public static final int KP_6 = 0xffb6;
    public static final int KP_7 = 0xffb7;
    public static final int KP_8 = 0xffb8;
    public static final int KP_9 = 0xffb9;


    /* Auxilliary Functions; note the duplicate definitions for left and
    * right function keys; Sun keyboards and a few other manufactures have
    * such function key groups on the left and/or right sides of the
    * keyboard. We've not found a keyboard with more than 35 function keys
    * total.
    */

    public static final int F1 = 0xffbe;
    public static final int F2 = 0xffbf;
    public static final int F3 = 0xffc0;
    public static final int F4 = 0xffc1;
    public static final int F5 = 0xffc2;
    public static final int F6 = 0xffc3;
    public static final int F7 = 0xffc4;
    public static final int F8 = 0xffc5;
    public static final int F9 = 0xffc6;
    public static final int F10 = 0xffc7;
    public static final int F11 = 0xffc8;
    public static final int L1 = 0xffc8;
    public static final int F12 = 0xffc9;
    public static final int L2 = 0xffc9;
    public static final int F13 = 0xffca;
    public static final int L3 = 0xffca;
    public static final int F14 = 0xffcb;
    public static final int L4 = 0xffcb;
    public static final int F15 = 0xffcc;
    public static final int L5 = 0xffcc;
    public static final int F16 = 0xffcd;
    public static final int L6 = 0xffcd;
    public static final int F17 = 0xffce;
    public static final int L7 = 0xffce;
    public static final int F18 = 0xffcf;
    public static final int L8 = 0xffcf;
    public static final int F19 = 0xffd0;
    public static final int L9 = 0xffd0;
    public static final int F20 = 0xffd1;
    public static final int L10 = 0xffd1;
    public static final int F21 = 0xffd2;
    public static final int R1 = 0xffd2;
    public static final int F22 = 0xffd3;
    public static final int R2 = 0xffd3;
    public static final int F23 = 0xffd4;
    public static final int R3 = 0xffd4;
    public static final int F24 = 0xffd5;
    public static final int R4 = 0xffd5;
    public static final int F25 = 0xffd6;
    public static final int R5 = 0xffd6;
    public static final int F26 = 0xffd7;
    public static final int R6 = 0xffd7;
    public static final int F27 = 0xffd8;
    public static final int R7 = 0xffd8;
    public static final int F28 = 0xffd9;
    public static final int R8 = 0xffd9;
    public static final int F29 = 0xffda;
    public static final int R9 = 0xffda;
    public static final int F30 = 0xffdb;
    public static final int R10 = 0xffdb;
    public static final int F31 = 0xffdc;
    public static final int R11 = 0xffdc;
    public static final int F32 = 0xffdd;
    public static final int R12 = 0xffdd;
    public static final int F33 = 0xffde;
    public static final int R13 = 0xffde;
    public static final int F34 = 0xffdf;
    public static final int R14 = 0xffdf;
    public static final int F35 = 0xffe0;
    public static final int R15 = 0xffe0;

    /* Modifiers. */

    public static final int SHIFT_L = 0xffe1; /* left shift */
    public static final int SHIFT_R = 0xffe2; /* right shift */
    public static final int CONTROL_L = 0xffe3; /* left control */
    public static final int CONTROL_R = 0xffe4; /* right control */
    public static final int CAPS_LOCK = 0xffe5; /* caps lock */
    public static final int SHIFT_LOCK = 0xffe6; /* shift lock */

    public static final int META_L = 0xffe7; /* left meta */
    public static final int META_R = 0xffe8; /* right meta */
    public static final int ALT_L = 0xffe9; /* left alt */
    public static final int ALT_R = 0xffea; /* right alt */
    public static final int SUPER_L = 0xffeb; /* left super */
    public static final int SUPER_R = 0xffec; /* right super */
    public static final int HYPER_L = 0xffed; /* left hyper */
    public static final int HYPER_R = 0xffee; /* right hyper */

    //XKB *********************************************************************
    public static final int LOCK = 0xfe01;
    public static final int LEVEL2_LATCH = 0xfe02;
    public static final int LEVEL3_SHIFT = 0xfe03;
    public static final int LEVEL3_LATCH = 0xfe04;
    public static final int LEVEL3_LOCK = 0xfe05;
    public static final int GROUP_SHIFT = 0xff7e; /* alias for mode_switch */
    public static final int GROUP_LATCH = 0xfe06;
    public static final int GROUP_LOCK = 0xfe07;
    public static final int NEXT_GROUP = 0xfe08;
    public static final int NEXT_GROUP_LOCK = 0xfe09;
    public static final int PREV_GROUP = 0xfe0a;
    public static final int PREV_GROUP_LOCK = 0xfe0b;
    public static final int FIRST_GROUP = 0xfe0c;
    public static final int FIRST_GROUP_LOCK = 0xfe0d;
    public static final int LAST_GROUP = 0xfe0e;
    public static final int LAST_GROUP_LOCK = 0xfe0f;

    public static final int LEFT_TAB = 0xfe20;;
    public static final int MOVE_LINE_UP = 0xfe21;
    public static final int MOVE_LINE_DOWN = 0xfe22;
    public static final int PARTIAL_LINE_UP = 0xfe23;
    public static final int PARTIAL_LINE_DOWN = 0xfe24;
    public static final int PARTIAL_SPACE_LEFT = 0xfe25;
    public static final int PARTIAL_SPACE_RIGHT = 0xfe26;
    public static final int SET_MARGIN_LEFT = 0xfe27;
    public static final int SET_MARGIN_RIGHT = 0xfe28;
    public static final int RELEASE_MARGIN_LEFT = 0xfe29;
    public static final int RELEASE_MARGIN_RIGHT = 0xfe2a;
    public static final int RELEASE_BOTH_MARGINS = 0xfe2b;
    public static final int FAST_CURSOR_LEFT = 0xfe2c;
    public static final int FAST_CURSOR_RIGHT = 0xfe2d;
    public static final int FAST_CURSOR_UP = 0xfe2e;
    public static final int FAST_CURSOR_DOWN = 0xfe2f;
    public static final int CONTINUOUS_UNDERLINE = 0xfe30;
    public static final int DISCONTINUOUS_UNDERLINE = 0xfe31;
    public static final int EMPHASIZE = 0xfe32;
    public static final int CENTER_OBJECT = 0xfe33;
    public static final int ENTER = 0xfe34;

    public static final int DEAD_GRAVE = 0xfe50;;
    public static final int DEAD_ACUTE = 0xfe51;
    public static final int DEAD_CIRCUMFLEX = 0xfe52;
    public static final int DEAD_TILDE = 0xfe53;
    public static final int DEAD_MACRON = 0xfe54;
    public static final int DEAD_BREVE = 0xfe55;
    public static final int DEAD_ABOVEDOT = 0xfe56;
    public static final int DEAD_DIAERESIS = 0xfe57;
    public static final int DEAD_ABOVERING = 0xfe58;
    public static final int DEAD_DOUBLEACUTE = 0xfe59;
    public static final int DEAD_CARON = 0xfe5a;
    public static final int DEAD_CEDILLA = 0xfe5b;
    public static final int DEAD_OGONEK = 0xfe5c;
    public static final int DEAD_IOTA = 0xfe5d;
    public static final int DEAD_VOICED_SOUND = 0xfe5e;
    public static final int DEAD_SEMIVOICED_SOUND = 0xfe5f;
    public static final int DEAD_BELOWDOT = 0xfe60;
    public static final int DEAD_HOOK = 0xfe61;
    public static final int DEAD_HORN = 0xfe62;

    public static final int FIRST_VIRTUAL_SCREEN = 0xfed0;;
    public static final int PREV_VIRTUAL_SCREEN = 0xfed1;
    public static final int NEXT_VIRTUAL_SCREEN = 0xfed2;
    public static final int LAST_VIRTUAL_SCREEN = 0xfed4;
    public static final int TERMINATE_SERVER = 0xfed5;

    public static final int ACCESS_X_ENABLE = 0xfe70;;
    public static final int ACCESS_X_FEEDBACK_ENABLE = 0xfe71;
    public static final int REPEAT_KEYS_ENABLE = 0xfe72;
    public static final int SLOW_KEYS_ENABLE = 0xfe73;
    public static final int BOUNCE_KEYS_ENABLE = 0xfe74;
    public static final int STICKY_KEYS_ENABLE = 0xfe75;
    public static final int MOUSE_KEYS_ENABLE = 0xfe76;
    public static final int MOUSE_KEYS_ACCEL_ENABLE = 0xfe77;
    public static final int OVERLAY1_ENABLE = 0xfe78;
    public static final int OVERLAY2_ENABLE = 0xfe79;
    public static final int AUDIBLE_BELL_ENABLE = 0xfe7a;

    public static final int POINTER_LEFT = 0xfee0;;
    public static final int POINTER_RIGHT = 0xfee1;
    public static final int POINTER_UP = 0xfee2;
    public static final int POINTER_DOWN = 0xfee3;
    public static final int POINTER_UP_LEFT = 0xfee4;
    public static final int POINTER_UP_RIGHT = 0xfee5;
    public static final int POINTER_DOWN_LEFT = 0xfee6;
    public static final int POINTER_DOWN_RIGHT = 0xfee7;
    public static final int POINTER_BUTTON_DFLT = 0xfee8;
    public static final int POINTER_BUTTON1 = 0xfee9;
    public static final int POINTER_BUTTON2 = 0xfeea;
    public static final int POINTER_BUTTON3 = 0xfeeb;
    public static final int POINTER_BUTTON4 = 0xfeec;
    public static final int POINTER_BUTTON5 = 0xfeed;
    public static final int POINTER_DBL_CLICK_DFLT = 0xfeee;
    public static final int POINTER_DBL_CLICK1 = 0xfeef;
    public static final int POINTER_DBL_CLICK2 = 0xfef0;
    public static final int POINTER_DBL_CLICK3 = 0xfef1;
    public static final int POINTER_DBL_CLICK4 = 0xfef2;
    public static final int POINTER_DBL_CLICK5 = 0xfef3;
    public static final int POINTER_DRAG_DFLT = 0xfef4;
    public static final int POINTER_DRAG1 = 0xfef5;
    public static final int POINTER_DRAG2 = 0xfef6;
    public static final int POINTER_DRAG3 = 0xfef7;
    public static final int POINTER_DRAG4 = 0xfef8;
    public static final int POINTER_DRAG5 = 0xfefd;

    public static final int POINTER_ENABLE_KEYS = 0xfef9;;
    public static final int POINTER_ACCELERATE = 0xfefa;
    public static final int POINTER_DFLT_BTN_NEXT = 0xfefb;
    public static final int POINTER_DFLT_BTN_PREV = 0xfefc;

    //XFree86 *****************************************************************
    /* ModeLock. This one is old, and not really used any more since XKB
    * offers this functionality.
    */

    public static final int MODE_LOCK = 0x1008ff01; /* mode switch lock */

    /* "Internet" keyboards. */

    public static final int STANDBY = 0x1008ff10;
    public static final int AUDIO_LOWER_VOLUME = 0x1008ff11;
    public static final int AUDIO_MUTE = 0x1008ff12;
    public static final int AUDIO_RAISE_VOLUME = 0x1008ff13;
    public static final int AUDIO_PLAY = 0x1008ff14;
    public static final int AUDIO_STOP = 0x1008ff15;
    public static final int AUDIO_PREV = 0x1008ff16;
    public static final int AUDIO_NEXT = 0x1008ff17;
    public static final int HOME_PAGE = 0x1008ff18;
    public static final int MAIL = 0x1008ff19;
    public static final int START = 0x1008ff1a;
    public static final int SEARCH = 0x1008ff1b;
    public static final int AUDIO_RECORD = 0x1008ff1c;

    /* PDA's (e.g. Palm, PocketPC or elsewhere). */

    public static final int CALCULATOR = 0x1008ff1d;
    public static final int MEMO = 0x1008ff1e;
    public static final int TO_DO_LIST = 0x1008ff1f;
    public static final int CALENDAR = 0x1008ff20;
    public static final int POWER_DOWN = 0x1008ff21;
    public static final int CONTRASTADJUST = 0x1008ff22;
    public static final int ROCKER_UP = 0x1008ff23;
    public static final int ROCKER_DOWN = 0x1008ff24;
    public static final int ROCKER_ENTER = 0x1008ff25;
    public static final int BACK = 0x1008ff26;
    public static final int FORWARD = 0x1008ff27;
    public static final int STOP = 0x1008ff28;
    public static final int REFRESH = 0x1008ff29;
    public static final int POWER_OFF = 0x1008ff1a;
    public static final int WAKE_UP = 0x1008ff1b;

    /* Note, 0x1008ff02 - 0x1008ff0f are free and should be used for misc new
    * keysyms that don't fit into any of the groups below.
    */

    /* Misc. */

    public static final int FAVORITES = 0x1008ff30;
    public static final int AUDIO_PAUSE = 0x1008ff31;
    public static final int AUDIO_MEDIA = 0x1008ff32;
    public static final int MY_COMPUTER = 0x1008ff33;
    public static final int VENDOR_HOME = 0x1008ff34;
    public static final int LIGHT_BULB = 0x1008ff35;
    public static final int SHOP = 0x1008ff36;

    //Currency ****************************************************************
    public static final int CURR_ECU = 0x20a0;
    public static final int CURR_COLON = 0x20a1;
    public static final int CURR_CRUZEIRO = 0x20a2;
    public static final int CURR_FFRANC = 0x20a3;
    public static final int CURR_LIRA = 0x20a4;
    public static final int CURR_MILL = 0x20a5;
    public static final int CURR_NAIRA = 0x20a6;
    public static final int CURR_PESETA = 0x20a7;
    public static final int CURR_RUPEE = 0x20a8;
    public static final int CURR_WON = 0x20a9;
    public static final int CURR_NEW_SHEQEL = 0x20aa;
    public static final int CURR_DONG = 0x20ab;
    public static final int CURR_EURO = 0x20ac;

    private X11KeysymDefinitions(){}

    /**
     * Converts an AWT key into a X11 keysym.
     *
     * @param awtKey
     * @return
     */
    public static int awtKeyToX11Keysym(int awtKey){
        switch(awtKey){
            case KeyEvent.VK_ENTER:
                return RETURN;
            case KeyEvent.VK_BACK_SPACE:
                return BACKSPACE;
            case KeyEvent.VK_TAB:
                return TAB;
            case KeyEvent.VK_CANCEL:
                return CANCEL;
            case KeyEvent.VK_CLEAR:
                return CLEAR;
            case KeyEvent.VK_SHIFT:
                return SHIFT_L;
            case KeyEvent.VK_CONTROL:
                return CONTROL_L;
            case KeyEvent.VK_ALT:
                return ALT_L;
            case KeyEvent.VK_PAUSE:
                return PAUSE;
            case KeyEvent.VK_CAPS_LOCK:
                return CAPS_LOCK;
            case KeyEvent.VK_ESCAPE:
                return ESCAPE;
            case KeyEvent.VK_SPACE:
                return SPACE;
            case KeyEvent.VK_PAGE_UP:
                return PAGE_UP;
            case KeyEvent.VK_PAGE_DOWN:
                return PAGE_DOWN;
            case KeyEvent.VK_END:
                return END;
            case KeyEvent.VK_HOME:
                return HOME;
            case KeyEvent.VK_LEFT:
                return LEFT;
            case KeyEvent.VK_UP:
                return UP;
            case KeyEvent.VK_RIGHT:
                return RIGHT;
            case KeyEvent.VK_DOWN:
                return DOWN;
            case KeyEvent.VK_COMMA:
                return COMMA;
            case KeyEvent.VK_MINUS:
                return MINUS;
            case KeyEvent.VK_PERIOD:
                return PERIOD;
            case KeyEvent.VK_SLASH:
                return SLASH;
            case KeyEvent.VK_0:
                return NUM_0;
            case KeyEvent.VK_1:
                return NUM_1;
            case KeyEvent.VK_2:
                return NUM_2;
            case KeyEvent.VK_3:
                return NUM_3;
            case KeyEvent.VK_4:
                return NUM_4;
            case KeyEvent.VK_5:
                return NUM_5;
            case KeyEvent.VK_6:
                return NUM_6;
            case KeyEvent.VK_7:
                return NUM_7;
            case KeyEvent.VK_8:
                return NUM_8;
            case KeyEvent.VK_9:
                return NUM_9;
            case KeyEvent.VK_SEMICOLON:
                return SEMICOLON;
            case KeyEvent.VK_EQUALS:
                return EQUAL;
            case KeyEvent.VK_A:
                return A;
            case KeyEvent.VK_B:
                return B;
            case KeyEvent.VK_C:
                return C;
            case KeyEvent.VK_D:
                return D;
            case KeyEvent.VK_E:
                return E;
            case KeyEvent.VK_F:
                return F;
            case KeyEvent.VK_G:
                return G;
            case KeyEvent.VK_H:
                return H;
            case KeyEvent.VK_I:
                return I;
            case KeyEvent.VK_J:
                return J;
            case KeyEvent.VK_K:
                return K;
            case KeyEvent.VK_L:
                return L;
            case KeyEvent.VK_M:
                return M;
            case KeyEvent.VK_N:
                return N;
            case KeyEvent.VK_O:
                return O;
            case KeyEvent.VK_P:
                return P;
            case KeyEvent.VK_Q:
                return Q;
            case KeyEvent.VK_R:
                return R;
            case KeyEvent.VK_S:
                return S;
            case KeyEvent.VK_T:
                return T;
            case KeyEvent.VK_U:
                return U;
            case KeyEvent.VK_V:
                return V;
            case KeyEvent.VK_W:
                return W;
            case KeyEvent.VK_X:
                return X;
            case KeyEvent.VK_Y:
                return Y;
            case KeyEvent.VK_Z:
                return Z;
            case KeyEvent.VK_OPEN_BRACKET:
                return BRACKET_LEFT;
            case KeyEvent.VK_BACK_SLASH:
                return BACKSLASH;
            case KeyEvent.VK_CLOSE_BRACKET:
                return BRACKET_LEFT;
            case KeyEvent.VK_NUMPAD0:
                return KP_0;
            case KeyEvent.VK_NUMPAD1:
                return KP_1;
            case KeyEvent.VK_NUMPAD2:
                return KP_2;
            case KeyEvent.VK_NUMPAD3:
                return KP_3;
            case KeyEvent.VK_NUMPAD4:
                return KP_4;
            case KeyEvent.VK_NUMPAD5:
                return KP_5;
            case KeyEvent.VK_NUMPAD6:
                return KP_6;
            case KeyEvent.VK_NUMPAD7:
                return KP_7;
            case KeyEvent.VK_NUMPAD8:
                return KP_8;
            case KeyEvent.VK_NUMPAD9:
                return KP_9;
            case KeyEvent.VK_MULTIPLY:
                return KP_MULTIPLY;
            case KeyEvent.VK_ADD:
                return KP_ADD;
            case KeyEvent.VK_SEPARATER:
                return KP_SEPARATOR;
            case KeyEvent.VK_SUBTRACT:
                return KP_SUBTRACT;
            case KeyEvent.VK_DECIMAL:
                return KP_DECIMAL;
            case KeyEvent.VK_DIVIDE:
                return KP_DIVIDE;
            case KeyEvent.VK_DELETE:
                return DELETE;
            case KeyEvent.VK_NUM_LOCK:
                return NUM_LOCK;
            case KeyEvent.VK_SCROLL_LOCK:
                return SCROLL_LOCK;
            case KeyEvent.VK_F1:
                return F1;
            case KeyEvent.VK_F2:
                return F2;
            case KeyEvent.VK_F3:
                return F3;
            case KeyEvent.VK_F4:
                return F4;
            case KeyEvent.VK_F5:
                return F5;
            case KeyEvent.VK_F6:
                return F6;
            case KeyEvent.VK_F7:
                return F7;
            case KeyEvent.VK_F8:
                return F8;
            case KeyEvent.VK_F9:
                return F9;
            case KeyEvent.VK_F10:
                return F10;
            case KeyEvent.VK_F11:
                return F11;
            case KeyEvent.VK_F12:
                return F12;
            case KeyEvent.VK_F13:
                return F13;
            case KeyEvent.VK_F14:
                return F14;
            case KeyEvent.VK_F15:
                return F15;
            case KeyEvent.VK_F16:
                return F16;
            case KeyEvent.VK_F17:
                return F17;
            case KeyEvent.VK_F18:
                return F18;
            case KeyEvent.VK_F19:
                return F19;
            case KeyEvent.VK_F20:
                return F20;
            case KeyEvent.VK_F21:
                return F21;
            case KeyEvent.VK_F22:
                return F22;
            case KeyEvent.VK_F23:
                return F23;
            case KeyEvent.VK_F24:
                return F24;
            case KeyEvent.VK_PRINTSCREEN:
                return PRINT;
            case KeyEvent.VK_INSERT:
                return INSERT;
            case KeyEvent.VK_HELP:
                return HELP;
            case KeyEvent.VK_META:
                return META_L;
            case KeyEvent.VK_BACK_QUOTE:
                return QUOTE_LEFT;
            case KeyEvent.VK_QUOTE:
                return QUOTE_RIGHT;
            case KeyEvent.VK_KP_UP:
                return KP_UP;
            case KeyEvent.VK_KP_DOWN:
                return KP_DOWN;
            case KeyEvent.VK_KP_LEFT:
                return KP_LEFT;
            case KeyEvent.VK_KP_RIGHT:
                return KP_RIGHT;
            case KeyEvent.VK_DEAD_GRAVE:
                return DEAD_GRAVE;
            case KeyEvent.VK_DEAD_ACUTE:
                return DEAD_ACUTE;
            case KeyEvent.VK_DEAD_CIRCUMFLEX:
                return DEAD_CIRCUMFLEX;
            case KeyEvent.VK_DEAD_TILDE:
                return DEAD_TILDE;
            case KeyEvent.VK_DEAD_MACRON:
                return DEAD_MACRON;
            case KeyEvent.VK_DEAD_BREVE:
                return DEAD_BREVE;
            case KeyEvent.VK_DEAD_ABOVEDOT:
                return DEAD_ABOVEDOT;
            case KeyEvent.VK_DEAD_DIAERESIS:
                return DEAD_DIAERESIS;
            case KeyEvent.VK_DEAD_ABOVERING:
                return DEAD_ABOVERING;
            case KeyEvent.VK_DEAD_DOUBLEACUTE:
                return DEAD_DOUBLEACUTE;
            case KeyEvent.VK_DEAD_CARON:
                return DEAD_CARON;
            case KeyEvent.VK_DEAD_CEDILLA:
                return DEAD_CEDILLA;
            case KeyEvent.VK_DEAD_OGONEK:
                return DEAD_OGONEK;
            case KeyEvent.VK_DEAD_IOTA:
                return DEAD_IOTA;
            case KeyEvent.VK_DEAD_VOICED_SOUND:
                return DEAD_VOICED_SOUND;
            case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
                return DEAD_SEMIVOICED_SOUND;
            case KeyEvent.VK_AMPERSAND:
                return AMPERSAND;
            case KeyEvent.VK_ASTERISK:
                return ASTERISK;
            case KeyEvent.VK_QUOTEDBL:
                return QUOTE_DBL;
            case KeyEvent.VK_LESS:
                return LESS;
            case KeyEvent.VK_GREATER:
                return GREATER;
            case KeyEvent.VK_BRACELEFT:
                return BRACE_LEFT;
            case KeyEvent.VK_BRACERIGHT:
                return BRACE_RIGHT;
            case KeyEvent.VK_AT:
                return AT;
            case KeyEvent.VK_COLON:
                return COLON;
            case KeyEvent.VK_CIRCUMFLEX:
                return ASCII_CIRCUM;
            case KeyEvent.VK_DOLLAR:
                return DOLLAR;
            case KeyEvent.VK_EURO_SIGN:
                return CURR_EURO;
            case KeyEvent.VK_EXCLAMATION_MARK:
                return EXCLAM;
            case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                return EXCLAM_DOWN;
            case KeyEvent.VK_LEFT_PARENTHESIS:
                return PAREN_LEFT;
            case KeyEvent.VK_NUMBER_SIGN:
                return NUMBER_SIGN;
            case KeyEvent.VK_PLUS:
                return PLUS;
            case KeyEvent.VK_RIGHT_PARENTHESIS:
                return PAREN_RIGHT;
            case KeyEvent.VK_UNDERSCORE:
                return UNDERSCORE;
            case KeyEvent.VK_WINDOWS:
                return SUPER_L;
            case KeyEvent.VK_CONTEXT_MENU:
                return MENU;
            case KeyEvent.VK_FINAL:
                return 0; //????
            case KeyEvent.VK_CONVERT:
                return 0; //????
            case KeyEvent.VK_NONCONVERT:
                return 0; //????
            case KeyEvent.VK_ACCEPT:
                return 0; //????
            case KeyEvent.VK_MODECHANGE:
                return MODE_SWITCH;
            case KeyEvent.VK_KANA:
                return KANA_SHIFT;
            case KeyEvent.VK_KANJI:
                return KANJI;
            case KeyEvent.VK_ALPHANUMERIC:
                return EISU_SHIFT;
            case KeyEvent.VK_KATAKANA:
                return KATAKANA;
            case KeyEvent.VK_HIRAGANA:
                return HIRAGANA;
            case KeyEvent.VK_FULL_WIDTH:
                return 0; //????
            case KeyEvent.VK_HALF_WIDTH:
                return 0; //????
            case KeyEvent.VK_ROMAN_CHARACTERS:
                return 0; //????
            case KeyEvent.VK_ALL_CANDIDATES:
                return MULTIPLE_CANDIDATE;
            case KeyEvent.VK_PREVIOUS_CANDIDATE:
                return PREVIOUS_CANDIDATE;
            case KeyEvent.VK_CODE_INPUT:
                return CODEINPUT;
            case KeyEvent.VK_JAPANESE_KATAKANA:
                return KATAKANA;
            case KeyEvent.VK_JAPANESE_HIRAGANA:
                return HIRAGANA;
            case KeyEvent.VK_JAPANESE_ROMAN:
                return 0; //????
            case KeyEvent.VK_KANA_LOCK:
                return KANA_LOCK;
            case KeyEvent.VK_INPUT_METHOD_ON_OFF:
                return 0; //????
            case KeyEvent.VK_CUT:
                return 0; //????
            case KeyEvent.VK_COPY:
                return 0; //????
            case KeyEvent.VK_PASTE:
                return 0; //????
            case KeyEvent.VK_UNDO:
                return UNDO;
            case KeyEvent.VK_AGAIN:
                return REDO;
            case KeyEvent.VK_FIND:
                return FIND;
            case KeyEvent.VK_PROPS:
                return 0; //????
            case KeyEvent.VK_STOP:
                return CANCEL;
            case KeyEvent.VK_COMPOSE:
                return MULTI_KEY;
            case KeyEvent.VK_ALT_GRAPH:
                return 0; //????
            case KeyEvent.VK_BEGIN:
                return BEGIN;
            case KeyEvent.VK_UNDEFINED:
                return 0;
            default:
                return 0;
        }
    }
}
