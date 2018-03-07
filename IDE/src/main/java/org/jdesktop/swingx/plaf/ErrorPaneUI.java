/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.plaf.PanelUI;

/**
 * The ComponentUI for a JXErrorPane.
 * <p>
 *
 * @author rbair
 */
public abstract class ErrorPaneUI extends PanelUI {
    /**
     * Creates new ErrorPane wrapped in the frame window centered at provided owner component.
     * @param owner component to center created error frame at.
     * @return New ErrorPane instance wrapped in JFrame.
     */
    public abstract JFrame getErrorFrame(Component owner);

    /**
     * Creates new ErrorPane wrapped in the dialog window centered at provided owner component.
     * @param owner component to center created error dialog at.
     * @return New ErrorPane instance wrapped in JDialog.
     */
    public abstract JDialog getErrorDialog(Component owner);

    /**
     * Creates new ErrorPane wrapped in the internal frame window centered at provided owner component.
     * @param owner component to center created error frame at.
     * @return New ErrorPane instance wrapped in JInternalFrame.
     */
    public abstract JInternalFrame getErrorInternalFrame(Component owner);
    /**
     * Calculates default prefered size for JXErrorPane on given platform/LAF.
     * @return Preferred size.
     */
    public abstract Dimension calculatePreferredSize();
}
