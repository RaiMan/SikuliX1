/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.plaf.PanelUI;

import org.jdesktop.swingx.JXTipOfTheDay;

/**
 * Pluggable UI for <code>JXTipOfTheDay</code>.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public abstract class TipOfTheDayUI extends PanelUI {

  /**
   * Creates a new JDialog to display a JXTipOfTheDay panel. If
   * <code>choice</code> is not null then the window will offer a way for the
   * end-user to not show the tip of the day dialog.
   *
   * @param parentComponent
   * @param choice
   * @return a new JDialog to display a JXTipOfTheDay panel
   */
  public abstract JDialog createDialog(Component parentComponent,
    JXTipOfTheDay.ShowOnStartupChoice choice);

}
