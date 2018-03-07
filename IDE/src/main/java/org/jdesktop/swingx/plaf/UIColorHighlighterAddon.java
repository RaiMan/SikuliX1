/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;
import org.jdesktop.swingx.util.OS;

/**
 * Loads LF specific background striping colors.
 *
 * The colors are based on the LF selection colors for certain
 * LFs and themes, for unknown LFs/themes a generic grey is used.
 *
 *
 * @author Jeanette Winzenburg
 * @author Karl Schaefer
 */
public class UIColorHighlighterAddon extends AbstractComponentAddon {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(UIColorHighlighterAddon.class.getName());

    public UIColorHighlighterAddon() {
        super("UIColorHighlighter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);

        defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(229, 229, 229));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMacDefaults(addon, defaults);

        defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(237, 243, 254));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMetalDefaults(addon, defaults);

        if (MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme) {
            defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(230, 238, 246));
        } else {
            defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(235, 235, 255));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addWindowsDefaults(addon, defaults);

        if (OS.isUsingWindowsVisualStyles()) {
            String xpStyle = OS.getWindowsVisualStyle();

            if (WindowsLookAndFeelAddons.HOMESTEAD_VISUAL_STYLE
                    .equalsIgnoreCase(xpStyle)) {
                defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(228, 231, 219));
            } else if (WindowsLookAndFeelAddons.SILVER_VISUAL_STYLE
                    .equalsIgnoreCase(xpStyle)) {
                defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(235, 235, 236));
            } else {
                // default blue
                defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(224, 233, 246));
            }

        } else {
            defaults.add("UIColorHighlighter.stripingBackground", new ColorUIResource(218, 222, 233));
        }
    }

    @Override
    protected void addNimbusDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        super.addNimbusDefaults(addon, defaults);
        // JW: Hacking around core issue #6882917
        // which is the underlying reason for issue #1180-swingx
        // (SwingX vs Nimbus table striping)
        if (Boolean.TRUE.equals(UIManager.get("Nimbus.keepAlternateRowColor"))) return;
        // PENDING JW: not entirely sure if it is safe to really grab the color here
        // the Nimbus (Derived)Color is not yet fully installed at this moment
        // so without a table to instantiate may be rgb = 0,0,0
        Object value = UIManager.getLookAndFeelDefaults().remove("Table.alternateRowColor");
        if (value instanceof Color) {
            defaults.add("UIColorHighlighter.stripingBackground", value, false);
        }
    }

}
