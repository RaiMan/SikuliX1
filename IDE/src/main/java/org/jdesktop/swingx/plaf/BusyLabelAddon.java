/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.plaf.ColorUIResource;

import org.jdesktop.swingx.JXBusyLabel;

/**
 * Addon for <code>JXBusyLabel</code>.<br>
 *
 * @author rah003
 */
public class BusyLabelAddon extends AbstractComponentAddon {

  public BusyLabelAddon() {
    super("JXBusyLabel");
  }

  @Override
  protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    defaults.add(JXBusyLabel.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicBusyLabelUI");
    defaults.add("JXBusyLabel.delay", 100);
    defaults.add("JXBusyLabel.baseColor", new ColorUIResource(Color.LIGHT_GRAY));
    defaults.add("JXBusyLabel.highlightColor", new ColorUIResource(UIManagerExt.getSafeColor("Label.foreground", Color.BLACK)));
    float barLength = 8;
    float barWidth = 4;
    float height = 26;
    defaults.add("JXBusyLabel.pointShape", new ShapeUIResource(
            new RoundRectangle2D.Float(0, 0, barLength, barWidth,
                barWidth, barWidth)));
    defaults.add("JXBusyLabel.trajectoryShape", new ShapeUIResource(
            new Ellipse2D.Float(barLength / 2, barLength / 2, height
                    - barLength, height - barLength)));

  }
}
