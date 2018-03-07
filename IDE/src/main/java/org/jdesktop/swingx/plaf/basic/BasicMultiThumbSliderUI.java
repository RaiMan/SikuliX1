/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.ThumbRenderer;
import org.jdesktop.swingx.multislider.TrackRenderer;
import org.jdesktop.swingx.plaf.MultiThumbSliderUI;

/**
 *
 * @author Joshua Marinacci
 */
public class BasicMultiThumbSliderUI extends MultiThumbSliderUI {

    protected JXMultiThumbSlider<?> slider;

    public static ComponentUI createUI(JComponent c) {
        return new BasicMultiThumbSliderUI();
    }

    @Override
    public void installUI(JComponent c) {
        slider = (JXMultiThumbSlider<?>)c;
        slider.setThumbRenderer(new BasicThumbRenderer());
        slider.setTrackRenderer(new BasicTrackRenderer());
    }
    @Override
    public void uninstallUI(JComponent c) {
        slider = null;
    }

    private class BasicThumbRenderer extends JComponent implements ThumbRenderer {
        public BasicThumbRenderer() {
            setPreferredSize(new Dimension(14,14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.green);
            Polygon poly = new Polygon();
            JComponent thumb = this;
            poly.addPoint(thumb.getWidth()/2,0);
            poly.addPoint(0,thumb.getHeight()/2);
            poly.addPoint(thumb.getWidth()/2,thumb.getHeight());
            poly.addPoint(thumb.getWidth(),thumb.getHeight()/2);
            g.fillPolygon(poly);
        }

        @Override
        public JComponent getThumbRendererComponent(JXMultiThumbSlider slider, int index, boolean selected) {
            return this;
        }
    }

    private class BasicTrackRenderer extends JComponent implements TrackRenderer {
        private JXMultiThumbSlider<?> slider;
        @Override
        public void paintComponent(Graphics g) {
            g.setColor(slider.getBackground());
            g.fillRect(0, 0, slider.getWidth(), slider.getHeight());
            g.setColor(Color.black);
            g.drawLine(0,slider.getHeight()/2,slider.getWidth(),slider.getHeight()/2);
            g.drawLine(0,slider.getHeight()/2+1,slider.getWidth(),slider.getHeight()/2+1);
        }

        @Override
        public JComponent getRendererComponent(JXMultiThumbSlider slider) {
            this.slider = slider;
            return this;
        }
    }
}
