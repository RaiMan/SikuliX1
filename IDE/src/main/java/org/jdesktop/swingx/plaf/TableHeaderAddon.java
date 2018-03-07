/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.metal.MetalBorders;

/**
 * Addon for JXTableHeader.
 *
 * Implemented to hack around core issue ??: Metal header renderer appears squeezed.
 *
 * @author Jeanette Winzenburg
 */
public class TableHeaderAddon extends AbstractComponentAddon {

    /**
     * @param name
     */
    public TableHeaderAddon() {
        super("JXTableHeader");
    }

    @Override
    protected void addMetalDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        super.addMetalDefaults(addon, defaults);
        String key = "TableHeader.cellBorder";
        Border border = UIManager.getBorder(key);
        if (border instanceof MetalBorders.TableHeaderBorder) {
            border = new BorderUIResource.CompoundBorderUIResource(border,
                    BorderFactory.createEmptyBorder());
            // PENDING JW: this is fishy ... adding to lookAndFeelDefaults is taken
            UIManager.getLookAndFeelDefaults().put(key, border);
            // adding to defaults is not
//            defaults.add(key, border);
        }

    }


}
