/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.plaf.LoginPaneUI;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.util.GraphicsUtilities;
/**
 * Base implementation of the <code>JXLoginPane</code> UI.
 *
 * @author rbair
 */
public class BasicLoginPaneUI extends LoginPaneUI {
    private class LocaleHandler implements PropertyChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object src = evt.getSource();

            if (src instanceof JComponent) {
                ((JComponent) src).updateUI();
            }
        }
    }

    private JXLoginPane dlg;

    /** Creates a new instance of BasicLoginDialogUI */
    public BasicLoginPaneUI(JXLoginPane dlg) {
        this.dlg = dlg;
//        dlg.addPropertyChangeListener("locale", new LocaleHandler());
    }

  public static ComponentUI createUI(JComponent c) {
    return new BasicLoginPaneUI((JXLoginPane)c);
  }

    @Override
    public void installUI(JComponent c) {
        installDefaults();
    }

    protected void installDefaults() {
        String s = dlg.getBannerText();
        if (s == null || s.equals("")) {
            dlg.setBannerText(UIManagerExt.getString("JXLoginPane.bannerString", dlg.getLocale()));
        }

        s = dlg.getErrorMessage();
        if (s == null || s.equals("")) {
            dlg.setErrorMessage(UIManagerExt.getString("JXLoginPane.errorMessage", dlg.getLocale()));
        }
    }

    /**
     * Creates default 400x60 banner for the login panel.
     * @see org.jdesktop.swingx.plaf.LoginPaneUI#getBanner()
     */
    @Override
    public Image getBanner() {
        int w = 400;
        int h = 60;
        float loginStringX = w * .05f;
        float loginStringY = h * .75f;

        BufferedImage img = GraphicsUtilities.createCompatibleImage(w, h);
        Graphics2D g2 = img.createGraphics();
        try {
            Font font = UIManager.getFont("JXLoginPane.bannerFont");
            g2.setFont(font);
            Graphics2D originalGraphics = g2;

            try {
                if (!dlg.getComponentOrientation().isLeftToRight()) {
                    originalGraphics = (Graphics2D) g2.create();
                    g2.scale(-1, 1);
                    g2.translate(-w, 0);
                    loginStringX = w
                            - (((float) font.getStringBounds(
                                    dlg.getBannerText(),
                                    originalGraphics.getFontRenderContext())
                                    .getWidth()) + w * .05f);
                }

                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                // draw a big square
                g2.setColor(UIManager
                        .getColor("JXLoginPane.bannerDarkBackground"));
                g2.fillRect(0, 0, w, h);

                // create the curve shape
                GeneralPath curveShape = new GeneralPath(
                        GeneralPath.WIND_NON_ZERO);
                curveShape.moveTo(0, h * .6f);
                curveShape.curveTo(w * .167f, h * 1.2f, w * .667f, h * -.5f, w,
                        h * .75f);
                curveShape.lineTo(w, h);
                curveShape.lineTo(0, h);
                curveShape.lineTo(0, h * .8f);
                curveShape.closePath();

                // draw into the buffer a gradient (bottom to top), and the text
                // "Login"
                GradientPaint gp = new GradientPaint(0, h, UIManager
                        .getColor("JXLoginPane.bannerDarkBackground"), 0, 0,
                        UIManager.getColor("JXLoginPane.bannerLightBackground"));
                g2.setPaint(gp);
                g2.fill(curveShape);

                originalGraphics.setColor(UIManager
                        .getColor("JXLoginPane.bannerForeground"));
                originalGraphics.drawString(dlg.getBannerText(), loginStringX,
                        loginStringY);
            } finally {
                originalGraphics.dispose();
            }
        } finally {
            g2.dispose();
        }
        return img;
    }
}
