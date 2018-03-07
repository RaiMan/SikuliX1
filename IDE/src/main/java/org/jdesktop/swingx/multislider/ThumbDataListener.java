/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.multislider;

/**
 *
 * @author jm158417
 */
public interface ThumbDataListener {
    public void valueChanged(ThumbDataEvent e);
    public void positionChanged(ThumbDataEvent e);
    public void thumbAdded(ThumbDataEvent e);
    public void thumbRemoved(ThumbDataEvent e);
}
