/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

/**
 *
 * @author rbair
 */
public abstract class TitledPanelUI extends PanelUI {
    /**
     * Adds the given JComponent as a decoration on the right of the title
     * @param decoration
     */
    public abstract void setRightDecoration(JComponent decoration);
    public abstract JComponent getRightDecoration();

    /**
     * Adds the given JComponent as a decoration on the left of the title
     * @param decoration
     */
    public abstract void setLeftDecoration(JComponent decoration);
    public abstract JComponent getLeftDecoration();
    /**
     * @return the Container acting as the title bar for this component
     */
    public abstract Container getTitleBar();
}
