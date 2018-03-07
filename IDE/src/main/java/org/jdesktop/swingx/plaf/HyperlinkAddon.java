/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import javax.swing.plaf.ColorUIResource;

import org.jdesktop.swingx.JXHyperlink;

/**
 * Addon for <code>JXHyperlink</code>.<br>
 */
public class HyperlinkAddon extends AbstractComponentAddon {
    public HyperlinkAddon() {
        super("JXHyperlink");
    }

    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);

        defaults.add(JXHyperlink.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI");
        //using CSS pseudo classes for Color types
        defaults.add("Hyperlink.linkColor", new ColorUIResource(0, 0x33, 0xFF));
        defaults.add("Hyperlink.visitedColor", new ColorUIResource(0x99, 0, 0x99));
        defaults.add("Hyperlink.hoverColor", new ColorUIResource(0xFF, 0x33, 0));
        defaults.add("Hyperlink.activeColor", new ColorUIResource(0xFF, 0x33, 0));
    }
}
