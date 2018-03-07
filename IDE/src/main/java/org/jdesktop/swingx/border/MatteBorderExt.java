/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.border.MatteBorder;

import org.jdesktop.beans.JavaBean;

/**
 * Matte border that allows specialized icons for corners and sides.
 *
 * @author Ramesh Gupta
 */
@JavaBean
public class MatteBorderExt extends MatteBorder {
    protected Icon[] tileIcons = null;
    private Icon defaultIcon = null;

    /**
     * Draws a matte border using specialized icons for corners and sides. If
         * tileIcons is null, or if the length of tileIcons array is less than 2, this
         * defaults to the {@link javax.swing.border.MatteBorder superclass} behavior.
         * Otherwise, tileIcons must specify icons in clockwise order, starting with
         * the top-left icon at index zero, culminating with the left icon at index 7.
     * If the length of the tileIcons array is greater than 1, but less than 8,
     * then tileIcons[0] is used to paint the corners, and tileIcons[1] is used
         * to paint the sides, with icons rotated as necessary. Other icons, if any,
     * are ignored.
     *
     * @param top top inset
     * @param left left inset
     * @param bottom bottom inset
     * @param right right inset
     * @param tileIcons array of icons starting with top-left in index 0,
     * continuing clockwise through the rest of the indices
     */
    public MatteBorderExt(int top, int left, int bottom, int right,
                          Icon[] tileIcons) {
        super(top, left, bottom, right,
              (tileIcons == null) || (tileIcons.length == 0) ? null :
              tileIcons[0]);
        this.tileIcons = tileIcons;
    }

    /**
     * @see MatteBorder#MatteBorder(int, int, int, int, java.awt.Color)
     */
    public MatteBorderExt(int top, int left, int bottom, int right,
                          Color matteColor) {
        super(top, left, bottom, right, matteColor);
    }

    /**
     * @see MatteBorder#MatteBorder(java.awt.Insets, java.awt.Color)
     */
    public MatteBorderExt(Insets borderInsets, Color matteColor) {
        super(borderInsets, matteColor);
    }

    /**
     * @see MatteBorder#MatteBorder(int, int, int, int, javax.swing.Icon)
     */
    public MatteBorderExt(int top, int left, int bottom, int right,
                          Icon tileIcon) {
        super(top, left, bottom, right, tileIcon);
    }

    /**
     * @see MatteBorder#MatteBorder(java.awt.Insets, javax.swing.Icon)
     */
    public MatteBorderExt(Insets borderInsets, Icon tileIcon) {
        super(borderInsets, tileIcon);
    }

    /**
     * @see MatteBorder#MatteBorder(javax.swing.Icon)
     */
    public MatteBorderExt(Icon tileIcon) {
        super(tileIcon);
    }

    /**
     * Returns the icons used by this border
     *
     * @return the icons used by this border
     */
    public Icon[] getTileIcons() {
        return tileIcons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {
        if ( (tileIcons == null) || (tileIcons.length < 2)) {
            super.paintBorder(c, g, x, y, width, height);
            return;
        }

        Insets insets = getBorderInsets(c);
        int clipWidth, clipHeight;

        clipWidth = Math.min(width, insets.left); // clip to component width or insets
        clipHeight = Math.min(height, insets.top); // clip to component height or insets

        if ( (clipWidth <= 0) || (clipHeight <= 0)) {
            return; // nothing to paint
        }

        // We now know that we have at least two icons!

        Color oldColor = g.getColor(); // restore before exiting
        g.translate(x, y);    // restore before exiting

        for (int i = 0; i < tileIcons.length; i++) {
            // Make sure we have an icon to paint with
            if (tileIcons[i] == null) {
                tileIcons[i] = getDefaultIcon();
            }
        }

        paintTopLeft(c, g, 0, 0, insets.left, insets.top);
        paintTop(c, g, insets.left, 0, width - insets.left - insets.right, insets.top);
        paintTopRight(c, g, width - insets.right, 0, insets.right, insets.top);
        paintRight(c, g, width - insets.right, insets.top, insets.right, height - insets.top - insets.bottom);
        paintBottomRight(c, g, width - insets.right, height - insets.bottom, insets.right, insets.bottom);
        paintBottom(c, g, insets.left, height - insets.bottom, width - insets.left - insets.right, insets.bottom);
        paintBottomLeft(c, g, 0, height - insets.bottom, insets.left, insets.bottom);
        paintLeft(c, g, 0, insets.top, insets.left, height - insets.top - insets.bottom);

        g.translate( -x, -y); // restore
        g.setColor(oldColor); // restore

    }

    protected void paint(Icon icon, Component c, Graphics g, int x, int y,
                             int width, int height) {
        Graphics cg = g.create();

        try {
            cg.setClip(x, y, width, height);
            int tileW = icon.getIconWidth();
            int tileH = icon.getIconHeight();
            int xpos, ypos, startx, starty;
            for (ypos = 0; height - ypos > 0; ypos += tileH) {
                for (xpos = 0; width - xpos > 0; xpos += tileW) {
                    icon.paintIcon(c, cg, x + xpos, y + ypos);
                }
            }
        } finally {
            cg.dispose();
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintTopLeft(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics cg = g.create();

        try {
            cg.setClip(x, y, width, height);
            tileIcons[0].paintIcon(c, cg, x, y);
        } finally {
            cg.dispose();
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintTop(Component c, Graphics g, int x, int y, int width, int height) {
        paint(tileIcons[1], c, g, x, y, width, height);
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintTopRight(Component c, Graphics g, int x, int y, int width, int height) {
        if (tileIcons.length == 8) {
            paint(tileIcons[2], c, g, x, y, width, height);
        }
        else {
            Icon icon = tileIcons[0];
            /** @todo Rotate -90 and paint icon */
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintRight(Component c, Graphics g, int x, int y, int width, int height) {
        if (tileIcons.length == 8) {
            paint(tileIcons[3], c, g, x, y, width, height);
        }
        else {
            Icon icon = tileIcons[1];
            /** @todo Rotate -90 and paint icon */
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintBottomRight(Component c, Graphics g, int x, int y, int width, int height) {
        if (tileIcons.length == 8) {
            paint(tileIcons[4], c, g, x, y, width, height);
        }
        else {
            Icon icon = tileIcons[0];
            /** @todo Rotate -180 and paint icon */
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintBottom(Component c, Graphics g, int x, int y, int width, int height) {
        if (tileIcons.length == 8) {
            paint(tileIcons[5], c, g, x, y, width, height);
        }
        else {
            Icon icon = tileIcons[1];
            /** @todo Rotate -180 and paint icon */
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintBottomLeft(Component c, Graphics g, int x, int y, int width, int height) {
        if (tileIcons.length == 8) {
            paint(tileIcons[6], c, g, x, y, width, height);
        }
        else {
            Icon icon = tileIcons[0];
            /** @todo Rotate -270 and paint icon */
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected void paintLeft(Component c, Graphics g, int x, int y, int width, int height) {
        if (tileIcons.length == 8) {
            paint(tileIcons[7], c, g, x, y, width, height);
        }
        else {
            Icon icon = tileIcons[1];
            /** @todo Rotate -270 and paint icon */
        }
    }

    /**
     * Only called by paintBorder()
     */
    protected Icon getDefaultIcon() {
        if (defaultIcon == null) {
            defaultIcon = new Icon() {
                private int width = 3;
                private int height = 3;

                public int getIconWidth() {
                    return width;
                }

                public int getIconHeight() {
                    return height;
                }

                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(c.getBackground().darker().darker());
                    //g.translate(x, y);
                    g.fillRect(x, y, width, height);
                }
            };
        }
        return defaultIcon;
    }
}
