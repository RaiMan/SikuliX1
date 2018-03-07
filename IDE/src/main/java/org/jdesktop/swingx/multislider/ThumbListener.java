/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.multislider;

import java.awt.event.MouseEvent;

public interface ThumbListener {
    public void thumbMoved(int thumb, float pos);
    public void thumbSelected(int thumb);
    public void mousePressed(MouseEvent evt);
}
