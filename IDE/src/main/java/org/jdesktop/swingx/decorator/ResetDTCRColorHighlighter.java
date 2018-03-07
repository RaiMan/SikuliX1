/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This is a hack around DefaultTableCellRenderer color "memory",
 * see Issue #258-swingx.<p>
 *
 * The issue is that the default has internal color management
 * which is different from other types of renderers. The
 * consequence of the internal color handling is that there's
 * a color memory which must be reset somehow. The "old" hack around
 * reset the xxColors of all types of renderers to the adapter's
 * target XXColors, introducing #178-swingx (Highlighgters must not
 * change any colors except those for which their color properties are
 * explicitly set).<p>
 *
 * This hack limits the interference to renderers of type
 * DefaultTableCellRenderer, applying a hacking highlighter which
 *  resets the renderers XXColors to a previously "memorized"
 *  color. Note that setting the color to null didn't have the desired
 *  effect.<p>
 *
 * PENDING: extend ColorHighlighter
 */

public class ResetDTCRColorHighlighter extends ColorHighlighter {

    public ResetDTCRColorHighlighter() {
        super(null, null);
    }

    /**
     * applies the memory hack for renderers of type DefaultTableCellRenderer,
     * does nothing for other types.
     * @param renderer the component to highlight
     * @param adapter the renderee's component state.
     */
    @Override
    public Component highlight(Component renderer, ComponentAdapter adapter) {
        //JW
        // table renderers have different state memory as list/tree renderers
        // without the null they don't unstamp!
        // but... null has adversory effect on JXList f.i. - selection
        // color is changed. This is related to #178-swingx:
        // highlighter background computation is weird.
        //
       if (renderer instanceof DefaultTableCellRenderer) {
            return super.highlight(renderer, adapter);
        }
        return renderer;
    }

    @Override
    protected void applyBackground(Component renderer, ComponentAdapter adapter) {
        if (!adapter.isSelected()) {
            Object colorMemory = ((JComponent) renderer).getClientProperty("rendererColorMemory.background");
            if (colorMemory instanceof ColorMemory) {
                renderer.setBackground(((ColorMemory) colorMemory).color);
            } else {
                ((JComponent) renderer).putClientProperty("rendererColorMemory.background", new ColorMemory(renderer.getBackground()));
            }
        }
    }

    @Override
    protected void applyForeground(Component renderer, ComponentAdapter adapter) {
        if (!adapter.isSelected()) {
            Object colorMemory = ((JComponent) renderer).getClientProperty("rendererColorMemory.foreground");
            if (colorMemory instanceof ColorMemory) {
                renderer.setForeground(((ColorMemory) colorMemory).color);
            } else {
                ((JComponent) renderer).putClientProperty("rendererColorMemory.foreground", new ColorMemory(renderer.getForeground()));
            }
        }
    }

    private static class ColorMemory {
        public ColorMemory(Color color) {
            this.color = color;
        }

        Color color;
    }


}
