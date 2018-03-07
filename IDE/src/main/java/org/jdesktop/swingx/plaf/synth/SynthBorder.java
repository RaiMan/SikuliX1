/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.synth;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;

/**
 * SynthBorder is a border that delegates to a Painter. The Insets
 * are determined at construction time.<p>
 *
 * Copied from core
 *
 * @version 1.15, 11/30/06
 * @author Scott Violet
 */
class SynthBorder extends AbstractBorder implements UIResource {
    private SynthUI ui;
    private Insets insets;

    SynthBorder(SynthUI ui, Insets insets) {
        this.ui = ui;
        this.insets = insets;
    }

    SynthBorder(SynthUI ui) {
        this(ui, null);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {
        JComponent jc = (JComponent)c;
        SynthContext context = ui.getContext(jc);
        SynthStyle style = context.getStyle();
        if (style == null) {
            assert false: "SynthBorder is being used outside after the UI " +
                          "has been uninstalled";
            return;
        }
        ui.paintBorder(context, g, x, y, width, height);
    }

    /**
     * This default implementation returns a new <code>Insets</code>
     * instance where the <code>top</code>, <code>left</code>,
     * <code>bottom</code>, and
     * <code>right</code> fields are set to <code>0</code>.
     * @param c the component for which this border insets value applies
     * @return the new <code>Insets</code> object initialized to 0
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, null);
    }

    /**
     * Reinitializes the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     * @return the <code>insets</code> object
     */
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        if (this.insets != null) {
            if (insets == null) {
                insets = new Insets(this.insets.top, this.insets.left,
                                  this.insets.bottom, this.insets.right);
            }
            else {
                insets.top    = this.insets.top;
                insets.bottom = this.insets.bottom;
                insets.left   = this.insets.left;
                insets.right  = this.insets.right;
            }
        }
        else if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }
        else {
            insets.top = insets.bottom = insets.left = insets.right = 0;
        }
        return insets;
    }

    /**
     * This default implementation returns false.
     * @return false
     */
    @Override
    public boolean isBorderOpaque() {
        return false;
    }

}
