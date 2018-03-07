/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.color;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.ThumbRenderer;
import org.jdesktop.swingx.util.PaintUtils;

public class GradientThumbRenderer extends JComponent implements ThumbRenderer {
    private Image thumb_black;
    private Image thumb_gray;

    public GradientThumbRenderer() {
        super();

        try {
            thumb_black = ImageIO.read(GradientThumbRenderer.class.getResourceAsStream("/icons/thumb_black.png"));
            thumb_gray = ImageIO.read(GradientThumbRenderer.class.getResourceAsStream("/icons/thumb_gray.png"));
        } catch (Exception ex)        {
//            ex.printStackTrace();
        }
    }

    private boolean selected;
    @Override
    protected void paintComponent(Graphics g) {
        JComponent thumb = this;
        int w = thumb.getWidth();
        g.setColor(getForeground());
        g.fillRect(0, 0, w - 1, w - 1);
        if (selected) {
            g.drawImage(thumb_black, 0, 0, null);
        } else {
            g.drawImage(thumb_gray, 0, 0, null);
        }
    }

    public JComponent getThumbRendererComponent(JXMultiThumbSlider slider, int index, boolean selected) {
        Color c = (Color)slider.getModel().getThumbAt(index).getObject();
        c = PaintUtils.removeAlpha(c);
        this.setForeground(c);
        this.selected = selected;
        return this;
    }
}
