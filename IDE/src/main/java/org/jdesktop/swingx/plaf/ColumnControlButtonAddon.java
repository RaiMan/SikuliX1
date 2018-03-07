/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import javax.swing.plaf.InsetsUIResource;

import org.jdesktop.swingx.icon.ColumnControlIcon;

/**
 * Addon to load LF specific properties for the ColumnControlButton.
 *
 * @author Jeanette Winzenburg
 */
public class ColumnControlButtonAddon extends AbstractComponentAddon {

    /**
     * Instantiates the addon for ColumnControlButton.
     */
    public ColumnControlButtonAddon() {
        super("ColumnControlButton");
    }

    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);
        defaults.add("ColumnControlButton.actionIcon", new ColumnControlIcon());
        defaults.add("ColumnControlButton.margin", new InsetsUIResource(1, 2, 2, 1));
    }

}
