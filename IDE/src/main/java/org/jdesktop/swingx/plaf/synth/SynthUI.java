/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.synth;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.synth.SynthContext;

/**
 * Replacement of sun.swing.plaf.SynthUI.<p>
 *
 * Note: this is a temporary emergency measure to make SwingX web-deployable. It is
 * used internally only. Expect problems in future, as custom styles might not be
 * found: SynthStyleFactory checks against type of sun SynthUI.
 *
 * @author Jeanette Winzenburg
 */
public interface SynthUI {

    public SynthContext getContext(JComponent arg0);

    public void paintBorder(SynthContext context, Graphics g, int x,
            int y, int w, int h);

}
