/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.jdesktop.swingx.JXLoginPane;

/**
 *
 * @author rbair
 */
public class LoginPaneAddon extends AbstractComponentAddon {

    /** Creates a new instance of LoginPaneAddon */
    public LoginPaneAddon() {
        super("JXLoginPane");
    }

  @Override
  protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addBasicDefaults(addon, defaults);
    Color errorBG = new Color(255, 215, 215);

    defaults.add(JXLoginPane.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicLoginPaneUI");
    defaults.add("JXLoginPane.errorIcon",
            LookAndFeel.makeIcon(LoginPaneAddon.class, "basic/resources/error16.png"));
    defaults.add("JXLoginPane.bannerFont", new FontUIResource("Arial Bold", Font.PLAIN, 36));
    //#911 Not every LAF has Label.font defined ...
    Font labelFont = UIManager.getFont("Label.font");
    Font boldLabel = labelFont != null ? labelFont.deriveFont(Font.BOLD) : new Font("SansSerif", Font.BOLD, 12);
    defaults.add("JXLoginPane.pleaseWaitFont",
            new FontUIResource(boldLabel));
    defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
    defaults.add("JXLoginPane.bannerDarkBackground", new ColorUIResource(Color.GRAY));
    defaults.add("JXLoginPane.bannerLightBackground", new ColorUIResource(Color.LIGHT_GRAY));
    defaults.add("JXLoginPane.errorBackground", new ColorUIResource(errorBG));
    defaults.add("JXLoginPane.errorBorder",
            new BorderUIResource(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 36, 0, 11),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.GRAY.darker()),
                            BorderFactory.createMatteBorder(5, 7, 5, 5, errorBG)))));

    UIManagerExt.addResourceBundle(
        "org.jdesktop.swingx.plaf.basic.resources.LoginPane");
  }

  @Override
  protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addMetalDefaults(addon, defaults);

    if (isPlastic()) {
      defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
      defaults.add("JXLoginPane.bannerDarkBackground", new ColorUIResource(Color.GRAY));
      defaults.add("JXLoginPane.bannerLightBackground", new ColorUIResource(Color.LIGHT_GRAY));
    } else {
        defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
        defaults.add("JXLoginPane.bannerDarkBackground",
                MetalLookAndFeel.getCurrentTheme().getPrimaryControlDarkShadow());
        defaults.add("JXLoginPane.bannerLightBackground",
                MetalLookAndFeel.getCurrentTheme().getPrimaryControl());
    }
  }

  @Override
  protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addWindowsDefaults(addon, defaults);

    defaults.add("JXLoginPane.bannerForeground", new ColorUIResource(Color.WHITE));
    defaults.add("JXLoginPane.bannerDarkBackground", new ColorUIResource(49, 121, 242));
    defaults.add("JXLoginPane.bannerLightBackground", new ColorUIResource(198, 211, 247));
  }
}
