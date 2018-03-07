/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

/**
 * TODO add type doc
 *
 * @author Jeanette Winzenburg
 */
public class TableAddon extends AbstractComponentAddon {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TableAddon.class
            .getName());

    /**
     * @param name
     */
    public TableAddon() {
        super("JXTable");
    }


    @Override
    protected void addNimbusDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        super.addNimbusDefaults(addon, defaults);
        // JW: Hacking around core issue #6882917
        // which is the underlying reason for issue #1180-swingx
        // (SwingX vs Nimbus table striping)
        if (Boolean.TRUE.equals(UIManager.get("Nimbus.keepAlternateRowColor"))) return;
        Object value = UIManager.getLookAndFeelDefaults().remove("Table.alternateRowColor");
        if (value instanceof Color) {
            defaults.add("UIColorHighlighter.stripingBackground", value, false);
        }
    }


    /**
     * @inherited <p>
     *
     * PENDING JW: move to addLinuxDefaults after testing
     */
    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);
        if (isGTK()) {
            replaceListTableBorders(addon, defaults);
        }
    }

    private void replaceListTableBorders(LookAndFeelAddons addon,
            DefaultsList defaults) {
        replaceBorder(defaults, "Table.", "focusCellHighlightBorder");
        replaceBorder(defaults, "Table.", "focusSelectedCellHighlightBorder");
        replaceBorder(defaults, "Table.", "noFocusBorder");
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
