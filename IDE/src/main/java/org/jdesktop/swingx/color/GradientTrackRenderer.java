/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.color;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.TrackRenderer;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * <p><b>Dependency</b>: Because this class relies on LinearGradientPaint and
 * RadialGradientPaint, it requires the optional MultipleGradientPaint.jar</p>
 */
public class GradientTrackRenderer extends JComponent implements TrackRenderer {
    private Paint checker_paint;

    public GradientTrackRenderer() {
        checker_paint = PaintUtils.getCheckerPaint();
    }

    private JXMultiThumbSlider slider;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintComponent(g);
    }

    @Override
    protected void paintComponent(Graphics gfx) {
        Graphics2D g = (Graphics2D)gfx;

        // get the list of colors
        List<Thumb<Color>> stops = slider.getModel().getSortedThumbs();
        int len = stops.size();

        // set up the data for the gradient
        float[] fractions = new float[len];
        Color[] colors = new Color[len];
        int i = 0;
        for(Thumb<Color> thumb : stops) {
            colors[i] = (Color)thumb.getObject();
            fractions[i] = thumb.getPosition();
            i++;
        }

        // calculate the track area
        int thumb_width = 12;
        int track_width = slider.getWidth() - thumb_width;
        g.translate(thumb_width / 2, 12);
        Rectangle2D rect = new Rectangle(0, 0, track_width, 20);

        // fill in the checker
        g.setPaint(checker_paint);
        g.fill(rect);

        // fill in the gradient
        Point2D start = new Point2D.Float(0,0);
        Point2D end = new Point2D.Float(track_width,0);
        MultipleGradientPaint paint = new LinearGradientPaint(
                (float)start.getX(),
                (float)start.getY(),
                (float)end.getX(),
                (float)end.getY(),
                fractions,colors);
        g.setPaint(paint);
        g.fill(rect);

        // draw a border
        g.setColor(Color.black);
        g.draw(rect);
        g.translate(-thumb_width / 2, -12);
    }

    public JComponent getRendererComponent(JXMultiThumbSlider slider) {
        this.slider = slider;
        return this;
    }
}
