/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.windows.WindowsClassicLookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;
import org.jdesktop.swingx.util.OS;

/**
 * Addon for <code>JXTaskPane</code>.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Karl Schaefer
 */
public class TaskPaneAddon extends AbstractComponentAddon {

  public TaskPaneAddon() {
    super("JXTaskPane");
  }

  @Override
  protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    Font taskPaneFont = UIManagerExt.getSafeFont("Label.font", new Font(
                "Dialog", Font.PLAIN, 12));
    taskPaneFont = taskPaneFont.deriveFont(Font.BOLD);

    Color menuBackground = new ColorUIResource(SystemColor.menu);

    defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicTaskPaneUI");
    defaults.add("TaskPane.font", new FontUIResource(taskPaneFont));
    defaults.add("TaskPane.background", UIManagerExt.getSafeColor("List.background",
              new ColorUIResource(Color.decode("#005C5C"))));
    defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(menuBackground.darker()));
    defaults.add("TaskPane.titleBackgroundGradientStart", menuBackground);
    defaults.add("TaskPane.titleBackgroundGradientEnd", menuBackground);
    defaults.add("TaskPane.titleForeground", new ColorUIResource(SystemColor.menuText));
    defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(SystemColor.menuText.brighter()));
    defaults.add("TaskPane.animate", Boolean.TRUE);
    defaults.add("TaskPane.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
            "ENTER", "toggleCollapsed",
            "SPACE", "toggleCollapsed"}));
  }

  @Override
  protected void addLinuxDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    addMetalDefaults(addon, defaults);
  }

  @Override
  protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addMetalDefaults(addon, defaults);

    if (MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme) {
        defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.misc.GlossyTaskPaneUI");
    } else {
        defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.metal.MetalTaskPaneUI");
    }

    //TODO use safe methods
    defaults.add("TaskPane.foreground", UIManager.getColor("activeCaptionText"));
    defaults.add("TaskPane.background", MetalLookAndFeel.getControl());
    defaults.add("TaskPane.specialTitleBackground", MetalLookAndFeel.getPrimaryControl());
    defaults.add("TaskPane.titleBackgroundGradientStart", MetalLookAndFeel.getPrimaryControl());
    defaults.add("TaskPane.titleBackgroundGradientEnd", MetalLookAndFeel.getPrimaryControlHighlight());
    defaults.add("TaskPane.titleForeground", MetalLookAndFeel.getControlTextColor());
    defaults.add("TaskPane.specialTitleForeground", MetalLookAndFeel.getControlTextColor());
    defaults.add("TaskPane.borderColor", MetalLookAndFeel.getPrimaryControl());
    defaults.add("TaskPane.titleOver", new ColorUIResource(MetalLookAndFeel.getControl().darker()));
    defaults.add("TaskPane.specialTitleOver", MetalLookAndFeel.getPrimaryControlHighlight());
  }

  @Override
  protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addWindowsDefaults(addon, defaults);

    if (addon instanceof WindowsLookAndFeelAddons) {
      defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.windows.WindowsTaskPaneUI");

      String xpStyle = OS.getWindowsVisualStyle();
      if (WindowsLookAndFeelAddons.HOMESTEAD_VISUAL_STYLE
        .equalsIgnoreCase(xpStyle)) {
        defaults.add("TaskPane.foreground", new ColorUIResource(86, 102, 45));
        defaults.add("TaskPane.background", new ColorUIResource(246, 246, 236));
        defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(224, 231, 184));
        defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(Color.WHITE));
        defaults.add("TaskPane.titleBackgroundGradientEnd", new ColorUIResource(224, 231, 184));
        defaults.add("TaskPane.titleForeground", new ColorUIResource(86, 102, 45));
        defaults.add("TaskPane.titleOver", new ColorUIResource(114, 146, 29));
        defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(86, 102, 45));
        defaults.add("TaskPane.specialTitleOver", new ColorUIResource(114, 146, 29));
        defaults.add("TaskPane.borderColor", new ColorUIResource(Color.WHITE));
      } else if (WindowsLookAndFeelAddons.SILVER_VISUAL_STYLE
        .equalsIgnoreCase(xpStyle)) {
        defaults.add("TaskPane.foreground", new ColorUIResource(Color.BLACK));
        defaults.add("TaskPane.background", new ColorUIResource(240, 241, 245));
        defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(222, 222, 222));
        defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(Color.WHITE));
        defaults.add("TaskPane.titleBackgroundGradientEnd", new ColorUIResource(214, 215, 224));
        defaults.add("TaskPane.titleForeground", new ColorUIResource(Color.BLACK));
        defaults.add("TaskPane.titleOver", new ColorUIResource(126, 124, 124));
        defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(Color.BLACK));
        defaults.add("TaskPane.specialTitleOver", new ColorUIResource(126, 124, 124));
        defaults.add("TaskPane.borderColor", new ColorUIResource(Color.WHITE));
      } else if (OS.isWindowsVista()) {
          //do not need to use safe method since the properties can never return null
         final Toolkit toolkit = Toolkit.getDefaultToolkit();

         defaults.add("TaskPane.foreground", new ColorUIResource(Color.WHITE));
         defaults.add("TaskPane.background",
                 new ColorUIResource((Color)toolkit.getDesktopProperty("win.3d.backgroundColor")));
         defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(33, 89, 201));
         defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(Color.WHITE));
         defaults.add("TaskPane.titleBackgroundGradientEnd",
                 new ColorUIResource((Color)toolkit.getDesktopProperty("win.frame.inactiveCaptionColor")));
         defaults.add("TaskPane.titleForeground",
                 new ColorUIResource((Color)toolkit.getDesktopProperty("win.frame.inactiveCaptionTextColor")));
         defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(Color.WHITE));
         defaults.add("TaskPane.borderColor", new ColorUIResource(Color.WHITE));
       } else {
         defaults.add("TaskPane.foreground", new ColorUIResource(Color.WHITE));
         defaults.add("TaskPane.background", new ColorUIResource(214, 223, 247));
         defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(33, 89, 201));
         defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(Color.WHITE));
         defaults.add("TaskPane.titleBackgroundGradientEnd", new ColorUIResource(199, 212, 247));
         defaults.add("TaskPane.titleForeground", new ColorUIResource(33, 89, 201));
         defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(Color.WHITE));
         defaults.add("TaskPane.borderColor", new ColorUIResource(Color.WHITE));
       }
    }

    if (addon instanceof WindowsClassicLookAndFeelAddons) {
      defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.windows.WindowsClassicTaskPaneUI");
      defaults.add("TaskPane.foreground", new ColorUIResource(Color.BLACK));
      defaults.add("TaskPane.background", new ColorUIResource(Color.WHITE));
      defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(10, 36, 106));
      defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(212, 208, 200));
      defaults.add("TaskPane.titleBackgroundGradientEnd", new ColorUIResource(212, 208, 200));
      defaults.add("TaskPane.titleForeground", new ColorUIResource(Color.BLACK));
      defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(Color.WHITE));
      defaults.add("TaskPane.borderColor", new ColorUIResource(212, 208, 200));
    }
  }

  @Override
  protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
    super.addMacDefaults(addon, defaults);

    defaults.add(JXTaskPane.uiClassID, "org.jdesktop.swingx.plaf.misc.GlossyTaskPaneUI");
    defaults.add("TaskPane.background", new ColorUIResource(245, 245, 245));
    defaults.add("TaskPane.titleForeground", new ColorUIResource(Color.BLACK));
    defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(188,188,188));
    defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(Color.BLACK));
    defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(250,250,250));
    defaults.add("TaskPane.titleBackgroundGradientEnd", new ColorUIResource(188,188,188));
    defaults.add("TaskPane.borderColor", new ColorUIResource(97, 97, 97));
    defaults.add("TaskPane.titleOver", new ColorUIResource(125, 125, 97));
    defaults.add("TaskPane.specialTitleOver", new ColorUIResource(125, 125, 97));
  }

    @Override
    protected void addNimbusDefaults(LookAndFeelAddons addon,
            DefaultsList defaults) {
        super.addNimbusDefaults(addon, defaults);

        defaults.add(JXTaskPane.uiClassID,
                "org.jdesktop.swingx.plaf.nimbus.NimbusTaskPaneUI");
        // dynamically changing the LaF to Nimbus does not refresh correctly the
        // control colors if they are not hard-coded due to Nimbus DerivedColors
        // lazy initialization
        // defaults.add("TaskPane.foreground", new
        // ColorUIResource(UIManager.getColor("activeCaption")));
        defaults.add("TaskPane.foreground", new ColorUIResource(186, 190, 198));
        // defaults.add("TaskPane.background", new
        // ColorUIResource(UIManager.getColor("control")));
        defaults.add("TaskPane.background", new ColorUIResource(214, 217, 223));
        // defaults.add("TaskPane.specialTitleBackground", new
        // ColorUIResource(UIManager.getColor("nimbusBlueGrey")));
        defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(
                169, 176, 190));
        // defaults.add("TaskPane.titleBackgroundGradientStart", new
        // ColorUIResource(UIManager.getColor("background")));
        defaults.add("TaskPane.titleBackgroundGradientStart",
                new ColorUIResource(214, 217, 223));
        // defaults.add("TaskPane.titleBackgroundGradientEnd", new
        // ColorUIResource(UIManager.getColor("controlLHighlight")));
        defaults.add("TaskPane.titleBackgroundGradientEnd",
                new ColorUIResource(247, 248, 250));
        defaults.add("TaskPane.titleForeground", new ColorUIResource(
                Color.BLACK));
        defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(
                Color.BLACK));
        // defaults.add("TaskPane.borderColor", new
        // ColorUIResource(UIManager.getColor("nimbusBorder")));
        defaults
                .add("TaskPane.borderColor", new ColorUIResource(146, 151, 161));
        // defaults.add("TaskPane.titleOver", new
        // ColorUIResource(UIManager.getColor("nimbusSelection")));
        defaults.add("TaskPane.titleOver", new ColorUIResource(57, 105, 138));
        // defaults.add("TaskPane.specialTitleOver", new
        // ColorUIResource(UIManager.getColor("nimbusSelection")));
        defaults.add("TaskPane.specialTitleOver", new ColorUIResource(57, 105,
                138));

    }

}
