/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.plaf.basic.BasicTipOfTheDayUI;
import org.jdesktop.swingx.plaf.windows.WindowsTipOfTheDayUI;

/**
 * Addon for <code>JXTipOfTheDay</code>.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class TipOfTheDayAddon extends AbstractComponentAddon {

  public TipOfTheDayAddon() {
    super("JXTipOfTheDay");
  }

  @Override
  protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
      super.addBasicDefaults(addon, defaults);

      Font font = UIManagerExt.getSafeFont("Label.font", new Font("Dialog", Font.PLAIN, 12));
      font = font.deriveFont(Font.BOLD, 13f);

      defaults.add(JXTipOfTheDay.uiClassID, BasicTipOfTheDayUI.class.getName());
      defaults.add("TipOfTheDay.font", UIManagerExt.getSafeFont("TextPane.font",
                new FontUIResource("Serif", Font.PLAIN, 12)));
      defaults.add("TipOfTheDay.tipFont", new FontUIResource(font));
      defaults.add("TipOfTheDay.background", new ColorUIResource(Color.WHITE));
      defaults.add("TipOfTheDay.icon",
              LookAndFeel.makeIcon(BasicTipOfTheDayUI.class, "resources/TipOfTheDay24.gif"));
      defaults.add("TipOfTheDay.border", new BorderUIResource(
              BorderFactory.createLineBorder(new Color(117, 117, 117))));

    UIManagerExt.addResourceBundle(
            "org.jdesktop.swingx.plaf.basic.resources.TipOfTheDay");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addWindowsDefaults(addon, defaults);

    Font font = UIManagerExt.getSafeFont("Label.font",
            new Font("Dialog", Font.PLAIN, 12));
    font = font.deriveFont(13f);

    defaults.add(JXTipOfTheDay.uiClassID, WindowsTipOfTheDayUI.class.getName());
    defaults.add("TipOfTheDay.background", new ColorUIResource(Color.GRAY));
    defaults.add("TipOfTheDay.font", new FontUIResource(font));
    defaults.add("TipOfTheDay.icon",
            LookAndFeel.makeIcon(WindowsTipOfTheDayUI.class, "resources/tipoftheday.png"));
    defaults.add("TipOfTheDay.border" ,new BorderUIResource(new WindowsTipOfTheDayUI.TipAreaBorder()));

    UIManagerExt.addResourceBundle(
        "org.jdesktop.swingx.plaf.windows.resources.TipOfTheDay");
  }

}
