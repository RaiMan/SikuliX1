/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.metal;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

/**
 *
 * @author rbair
 */
public class MetalStatusBarUI extends BasicStatusBarUI {

    /** Creates a new instance of BasicStatusBarUI */
    public MetalStatusBarUI() {
    }

    /**
     * Returns an instance of the UI delegate for the specified component.
     * Each subclass must provide its own static <code>createUI</code>
     * method that returns an instance of that UI delegate subclass.
     * If the UI delegate subclass is stateless, it may return an instance
     * that is shared by multiple components.  If the UI delegate is
     * stateful, then it should return a new instance per component.
     * The default implementation of this method throws an error, as it
     * should never be invoked.
     */
    public static ComponentUI createUI(JComponent c) {
        return new MetalStatusBarUI();
    }

    @Override
    protected void paintBackground(Graphics2D g, JXStatusBar bar) {
        int w = bar.getWidth(); //TODO deal with insets
        int h = bar.getHeight(); //TODO deal with insets

        //This list is comprised of floats and Colors, which together
        //constitute the gradient.
        List<?> gradient = (List<?>) UIManager.get("MenuBar.gradient");

        if (gradient != null && w > 0 && 0 < h) {
            float ratio1 = ((Number)gradient.get(0)).floatValue();
            float ratio2 = ((Number)gradient.get(1)).floatValue();
            Color c1 = (Color)gradient.get(2);
            Color c2 = (Color)gradient.get(3);
            Color c3 = (Color)gradient.get(4);
            int mid = (int)(ratio1 * h);
            int mid2 = (int)(ratio2 * h);
            if (mid > 0) {
                g.setPaint(new GradientPaint((float)0, (float)0, c1, (float)0,
                                       (float)mid, c2));
                g.fillRect(0, 0, w, mid);
            }
            if (mid2 > 0) {
                g.setColor(c2);
                g.fillRect(0, mid, w, mid2);
            }
            if (mid > 0) {
                g.setPaint(new GradientPaint((float)0, (float)mid + mid2, c2,
                                       (float)0, (float)mid * 2 + mid2, c1));
                g.fillRect(0, mid + mid2, w, mid);
            }
            if (h - mid * 2 - mid2 > 0) {
                g.setPaint(new GradientPaint((float)0, (float)mid * 2 + mid2, c1,
                                       (float)0, (float)h, c3));
                g.fillRect(0, mid * 2 + mid2, w, h - mid * 2 - mid2);
            }
        }
    }
}
