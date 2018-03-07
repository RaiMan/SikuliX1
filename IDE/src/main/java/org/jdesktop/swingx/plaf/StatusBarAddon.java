/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;
import org.jdesktop.swingx.util.OS;

/**
 * Addon for <code>JXStatusBar</code>.<br>
 *
 */
public class StatusBarAddon extends AbstractComponentAddon {

    public StatusBarAddon() {
        super("JXStatusBar");
    }

    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);

        defaults.add(JXStatusBar.uiClassID,
                "org.jdesktop.swingx.plaf.basic.BasicStatusBarUI");
    }

    @Override
    protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMacDefaults(addon, defaults);

        defaults.add(JXStatusBar.uiClassID,
                "org.jdesktop.swingx.plaf.macosx.MacOSXStatusBarUI");
    }

    @Override
    protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMetalDefaults(addon, defaults);

        defaults.add(JXStatusBar.uiClassID,
                "org.jdesktop.swingx.plaf.metal.MetalStatusBarUI");
    }

    @Override
    protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addWindowsDefaults(addon, defaults);
        if (OS.isUsingWindowsVisualStyles()) {
            defaults.add(JXStatusBar.uiClassID,
                    "org.jdesktop.swingx.plaf.windows.WindowsStatusBarUI");

            String xpStyle = OS.getWindowsVisualStyle();

            if (WindowsLookAndFeelAddons.SILVER_VISUAL_STYLE.equalsIgnoreCase(xpStyle)
                    || WindowsLookAndFeelAddons.VISTA_VISUAL_STYLE.equalsIgnoreCase(xpStyle)) {
                defaults.add("StatusBar.leftImage", "resources/silver-statusbar-left.png");
                defaults.add("StatusBar.middleImage", "resources/silver-statusbar-middle.png");
                defaults.add("StatusBar.rightImage", "resources/silver-statusbar-right.png");
            } else {
                defaults.add("StatusBar.leftImage", "resources/statusbar-left.png");
                defaults.add("StatusBar.middleImage", "resources/statusbar-middle.png");
                defaults.add("StatusBar.rightImage", "resources/statusbar-right.png");
            }
        } else {
            defaults.add(JXStatusBar.uiClassID,
                    "org.jdesktop.swingx.plaf.windows.WindowsClassicStatusBarUI");
        }
    }
}
