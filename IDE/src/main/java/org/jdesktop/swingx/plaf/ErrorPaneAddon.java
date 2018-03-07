/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import org.jdesktop.swingx.JXErrorPane;

/**
 *
 * @author rbair
 */
public class ErrorPaneAddon extends AbstractComponentAddon {

    /** Creates a new instance of ErrorPaneAddon */
    public ErrorPaneAddon() {
        super("JXErrorPane");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);

        defaults.add(JXErrorPane.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicErrorPaneUI");

        UIManagerExt.addResourceBundle(
            "org.jdesktop.swingx.plaf.basic.resources.ErrorPane");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMacDefaults(addon, defaults);

        defaults.add(JXErrorPane.uiClassID, "org.jdesktop.swingx.plaf.macosx.MacOSXErrorPaneUI");

        UIManagerExt.addResourceBundle(
            "org.jdesktop.swingx.plaf.macosx.resources.ErrorPane");
    }
}
