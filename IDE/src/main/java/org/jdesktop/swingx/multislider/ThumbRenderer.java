/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.multislider;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiThumbSlider;

public interface ThumbRenderer {
    public JComponent getThumbRendererComponent(JXMultiThumbSlider slider, int index, boolean selected);
}
