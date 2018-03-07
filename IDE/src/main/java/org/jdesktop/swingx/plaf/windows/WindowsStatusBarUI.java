/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.windows;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

/**
 * @author rbair
 */
public class WindowsStatusBarUI extends BasicStatusBarUI {
    private static final Logger log = Logger.getLogger(WindowsStatusBarUI.class.getName());
    private BufferedImage leftImage;
    private BufferedImage middleImage;
    private BufferedImage rightImage;

    /** Creates a new instance of WindowsStatusBarUI */
    public WindowsStatusBarUI() {
        //SwingX #827: must create these here or size is incorrect
        //TODO need to determine a better way to handle these images
        try {
            leftImage = ImageIO.read(WindowsStatusBarUI.class.getResource(UIManagerExt.getString("StatusBar.leftImage")));
            middleImage = ImageIO.read(WindowsStatusBarUI.class.getResource(UIManagerExt.getString("StatusBar.middleImage")));
            rightImage = ImageIO.read(WindowsStatusBarUI.class.getResource(UIManagerExt.getString("StatusBar.rightImage")));
        } catch (Exception e) {
            // log the message in case of init failure
            log.log(Level.FINE, e.getLocalizedMessage(), e);
        }
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
        return new WindowsStatusBarUI();
    }

    @Override protected void paintBackground(Graphics2D g, JXStatusBar statusBar) {
        if (leftImage == null || middleImage == null || rightImage == null) {
            log.severe("Failed to initialize necessary assets. Set logging to FINE to see more details.");
            return;
        }
        //if bidi, reverse the image painting order
        //TODO need to handle vertical stretching better
        g.drawImage(leftImage, 0, 0, leftImage.getWidth(), statusBar.getHeight(), null);

        if (statusBar.isResizeHandleEnabled()) {
            g.drawImage(middleImage, leftImage.getWidth(), 0, statusBar.getWidth() - leftImage.getWidth() - rightImage.getWidth(), statusBar.getHeight(), null);
            g.drawImage(rightImage, statusBar.getWidth() - rightImage.getWidth(), 0, rightImage.getWidth(), statusBar.getHeight(), null);
        } else {
            g.drawImage(middleImage, leftImage.getWidth(), 0, statusBar.getWidth() - leftImage.getWidth(), statusBar.getHeight(), null);
        }
    }

    @Override protected Insets getSeparatorInsets(Insets insets) {
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }
        insets.top = 1;
        insets.left = 4;
        insets.bottom = 0;
        insets.right = 4;
        return insets;
    }
}
