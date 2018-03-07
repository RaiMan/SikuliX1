/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.plaf.basic.BasicDatePickerUI;
import org.jdesktop.swingx.util.OS;

/**
 * @author Joshua Outwater
 */
public class DatePickerAddon extends AbstractComponentAddon {
    public DatePickerAddon() {
        super("JXDatePicker");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);

        defaults.add(JXDatePicker.uiClassID, BasicDatePickerUI.class.getName());
        defaults.add("JXDatePicker.border",
                new BorderUIResource(BorderFactory.createCompoundBorder(
                        LineBorder.createGrayLineBorder(),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3))));

        UIManagerExt.addResourceBundle(
                "org.jdesktop.swingx.plaf.basic.resources.DatePicker");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addWindowsDefaults(addon, defaults);
        if (OS.isWindowsXP() && OS.isUsingWindowsVisualStyles()) {
            defaults.add("JXDatePicker.arrowIcon",
                    LookAndFeel.makeIcon(DatePickerAddon.class, "windows/resources/combo-xp.png"));
        } else {
            defaults.add("JXDatePicker.arrowIcon",
                    LookAndFeel.makeIcon(DatePickerAddon.class, "windows/resources/combo-w2k.png"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addLinuxDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addLinuxDefaults(addon, defaults);

        defaults.add("JXDatePicker.arrowIcon",
                LookAndFeel.makeIcon(DatePickerAddon.class, "linux/resources/combo-gtk.png"));

        if (isGTK()) {
            // Issue #667-swingx: ugly border in GTK
            // remove the border which was installed in addBasicDefaults
           defaults.add("JXDatePicker.border", null);
        }
    }

    /**
     *
     * @return true if the LF is GTK.
     */
    private boolean isGTK() {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addMacDefaults(addon, defaults);

        defaults.add("JXDatePicker.arrowIcon",
                LookAndFeel.makeIcon(DatePickerAddon.class, "macosx/resources/combo-osx.png"));

        defaults.add("JXDatePicker.border", "none");

    }

    /** {@inheritDoc} */
    @Override
    protected void addNimbusDefaults (LookAndFeelAddons addon,
        DefaultsList defaults) {
        super.addNimbusDefaults (addon, defaults);

        // Issue #913-swingx: ugly in Nimbus
        // TODO: don't use an image here, Nimbus uses Painters for everything
        // => e.g. reuse the
//        com.sun.java.swing.plaf.nimbus.ComboBoxComboBoxArrowButtonPainter
        // (at the moment the OS-X icon looks most similar, it's much better
        //  than no icon...)
        defaults.add ("JXDatePicker.arrowIcon",
            LookAndFeel.makeIcon (DatePickerAddon.class,
            "macosx/resources/combo-osx.png"));

        // Issue #913-swingx: ugly in Nimbus
        // remove the border which was installed in addBasicDefaults
        // this is done by Nimbus
        defaults.add ("JXDatePicker.border", null);
    }

}
