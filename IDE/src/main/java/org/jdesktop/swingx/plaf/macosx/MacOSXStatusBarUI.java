/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.macosx;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

/**
 *
 * @author rbair
 */
public class MacOSXStatusBarUI extends BasicStatusBarUI {
    /**
     * Returns an instance of the UI delegate for the specified component.
     * Each subclass must provide its own static <code>createUI</code>
     * method that returns an instance of that UI delegate subclass.
     * If the UI delegate subclass is stateless, it may return an instance
     * that is shared by multiple components.  If the UI delegate is
     * stateful, then it should return a new instance per component.
     * The default implementation of this method throws an error, as it
     * should never be invoked.
     */
    public static ComponentUI createUI(JComponent c) {
        return new MacOSXStatusBarUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void installDefaults(JXStatusBar sb) {
        super.installDefaults(sb);

        LookAndFeel.installProperty(statusBar, "opaque", Boolean.FALSE);
    }
}
