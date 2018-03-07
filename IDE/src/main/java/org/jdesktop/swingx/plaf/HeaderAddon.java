/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.Font;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXHeader;

/**
 * Addon for <code>JXHeader</code>.<br>
 *
 */
public class HeaderAddon extends AbstractComponentAddon {

    public HeaderAddon() {
        super("JXHeader");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);

        defaults.add(JXHeader.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicHeaderUI");
        //TODO image is missing
        defaults.add("JXHeader.defaultIcon",
                LookAndFeel.makeIcon(HeaderAddon.class, "basic/resources/header-default.png"));
        //TODO use safe methods
        defaults.add("JXHeader.titleFont", new FontUIResource(UIManager.getFont("Label.font").deriveFont(Font.BOLD)));
        defaults.add("JXHeader.titleForeground", UIManager.getColor("Label.foreground"));
        defaults.add("JXHeader.descriptionFont", UIManager.getFont("Label.font"));
        defaults.add("JXHeader.descriptionForeground", UIManager.getColor("Label.foreground"));
        defaults.add("JXHeader.background",
                UIManagerExt.getSafeColor("control", new ColorUIResource(Color.decode("#C0C0C0"))));
        defaults.add("JXHeader.startBackground", new ColorUIResource(Color.WHITE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMacDefaults(addon, defaults);

        defaults.add("JXHeader.background", new ColorUIResource(new Color(218, 218, 218)));
        defaults.add("JXHeader.startBackground", new ColorUIResource(new Color(235, 235, 235)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addNimbusDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addNimbusDefaults(addon, defaults);

        defaults.add("JXHeader.background", new ColorUIResource(new Color(214, 217, 223, 255)));
    }
}
