/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

/**
 *
 * @author rbair
 */
public class WindowsClassicStatusBarUI extends BasicStatusBarUI {
    /** Creates a new instance of BasicStatusBarUI */
    public WindowsClassicStatusBarUI() {
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
        return new WindowsClassicStatusBarUI();
    }

    @Override protected void paintBackground(Graphics2D g, JXStatusBar bar) {
        g.setColor(bar.getBackground());
        g.fillRect(0, 0, bar.getWidth(), bar.getHeight());

        //paint an inset border around each component. This suggests that
        //there is an extra border around the status bar...!
        Border b = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                Color.WHITE, bar.getBackground(), bar.getBackground(), Color.GRAY);
        Insets insets = new Insets(0, 0, 0, 0);
        for (Component c : bar.getComponents()) {
            getSeparatorInsets(insets);
            int x = c.getX() - insets.right;
            int y = c.getY() - 2;
            int w = c.getWidth() + insets.left + insets.right;
            int h = c.getHeight() + 4;
            b.paintBorder(c, g, x, y, w, h);
        }
    }

    @Override protected void paintSeparator(Graphics2D g, JXStatusBar bar, int x, int y, int w, int h) {
        //paint nothing, since paintBackground handles this
    }

    @Override protected int getSeparatorWidth() {
        return 11;
    }

    @Override protected BorderUIResource createBorder() {
        return new BorderUIResource(BorderFactory.createEmptyBorder(4, 5, 3, 22));
    }
}
