/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;
import java.awt.Image;

import javax.swing.plaf.PanelUI;

/**
 *
 * @author rbair
 */
public abstract class LoginPaneUI extends PanelUI {
    /**
     * @return The Image to use as the banner for the JXLoginPane. If
     * this method returns null, then no banner will be shown.
     */
    public abstract Image getBanner();
}
