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

package com.tulskiy.keymaster.common;

import javax.swing.*;

/**
 * Internal representation of a hotkey. Either keyStroke or mediaKey should be set.
 * <br>
 * Author: Denis Tulskiy
 * Date: 6/20/11
 */
public class HotKey {
    public KeyStroke keyStroke;
    public MediaKey mediaKey;
    public HotKeyListener listener;

    public HotKey(KeyStroke keyStroke, HotKeyListener listener) {
        this.keyStroke = keyStroke;
        this.listener = listener;
    }

    public HotKey(MediaKey mediaKey, HotKeyListener listener) {
        this.mediaKey = mediaKey;
        this.listener = listener;
    }

    public boolean isMedia() {
        return mediaKey != null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("HotKey");
        if (keyStroke != null)
            sb.append("{").append(keyStroke.toString().replaceAll("pressed ", ""));
        if (mediaKey != null)
            sb.append("{").append(mediaKey);
        sb.append('}');
        return sb.toString();
    }
}
