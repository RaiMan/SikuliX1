/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.icon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import org.jdesktop.swingx.painter.Painter;

public class PainterIcon implements Icon {
    Dimension size;
    private Painter painter;
    public PainterIcon(Dimension size) {
        this.size = size;
    }

    @Override
    public int getIconHeight() {
        return size.height;
    }

    @Override
    public int getIconWidth() {
        return size.width;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (getPainter() != null && g instanceof Graphics2D) {
            g = g.create();

            try {
                g.translate(x, y);
                getPainter().paint((Graphics2D) g, c, size.width, size.height);
                g.translate(-x, -y);
            } finally {
                g.dispose();
            }
        }
    }

    public Painter getPainter() {
        return painter;
    }

    public void setPainter(Painter painter) {
        this.painter = painter;
    }
}
