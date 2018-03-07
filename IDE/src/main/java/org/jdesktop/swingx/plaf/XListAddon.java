/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXList;

/**
 * Addon for <code>JXList</code>.
 * <p>
 *
 * Install a custom ui to support sorting/filtering in JXList.
 */
public class XListAddon extends AbstractComponentAddon {

    public XListAddon() {
        super("JXList");
    }

    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        defaults.add(JXList.uiClassID,
                "org.jdesktop.swingx.plaf.basic.core.BasicXListUI");
        if (isGTK()) {
            replaceListTableBorders(addon, defaults);
        }
    }

    @Override
    protected void addNimbusDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        defaults.add(JXList.uiClassID,
                "org.jdesktop.swingx.plaf.synth.SynthXListUI");
    }

    private void replaceListTableBorders(LookAndFeelAddons addon,
            DefaultsList defaults) {
        replaceBorder(defaults, "List.", "focusCellHighlightBorder");
        replaceBorder(defaults, "List.", "focusSelectedCellHighlightBorder");
        replaceBorder(defaults, "List.", "noFocusBorder");
    }


    /**
     * @param defaults
     * @param componentPrefix
     * @param borderKey
     */
    private void replaceBorder(DefaultsList defaults, String componentPrefix,
            String borderKey) {
        String key = componentPrefix + borderKey;
        Border border = UIManager.getBorder(componentPrefix + borderKey);
        if (border instanceof AbstractBorder && border instanceof UIResource
                && border.getClass().getName().contains("ListTable")) {
            border = new SafeBorder((AbstractBorder) border);
            // PENDING JW: this is fishy ... adding to lookAndFeelDefaults is taken
            UIManager.getLookAndFeelDefaults().put(key, border);
            // adding to defaults is not
//            defaults.add(key, border);

        }
    }


    /**
     *
     * @return true if the LF is GTK.
     */
    private boolean isGTK() {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }

}
