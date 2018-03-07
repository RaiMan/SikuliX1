/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.plaf.BusyLabelUI;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 * Base implementation of the <code>JXBusyLabel</code> UI.
 *
 * @author rah003
 */
public class BasicBusyLabelUI extends BasicLabelUI implements BusyLabelUI {

    /** Creates a new instance of BasicBusyLabelUI */
    public BasicBusyLabelUI(JXBusyLabel lbl) {
    }

  public static ComponentUI createUI(JComponent c) {
    return new BasicBusyLabelUI((JXBusyLabel)c);
  }

    @Override
    public BusyPainter getBusyPainter(final Dimension dim) {
        BusyPainter p = new BusyPainter() {
            @Override
            protected void init(Shape point, Shape trajectory, Color b, Color h) {
                super.init(dim == null ? UIManagerExt.getShape("JXBusyLabel.pointShape") : getScaledDefaultPoint(dim.height),
                        dim == null ? UIManagerExt.getShape("JXBusyLabel.trajectoryShape") : getScaledDefaultTrajectory(dim.height),
                        UIManagerExt.getSafeColor("JXBusyLabel.baseColor", Color.LIGHT_GRAY),
                        UIManagerExt.getSafeColor("JXBusyLabel.highlightColor", Color.BLACK));
            }
        };
        return p;
    }

    @Override
    public int getDelay() {
        return UIManager.getInt("JXBusyLabel.delay");
    }

}
