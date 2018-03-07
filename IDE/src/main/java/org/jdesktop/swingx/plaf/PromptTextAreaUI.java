/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import javax.swing.JTextArea;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;

/**
 * {@link PromptTextUI} implementation for rendering prompts on
 * {@link JTextArea}s and uses a {@link JTextArea} as a prompt component.
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 */
public class PromptTextAreaUI extends PromptTextUI {
    /**
     * Shared prompt renderer.
     */
    private final static JTextArea txt = new JTextArea();

    /**
     * Creates a new {@link PromptTextAreaUI}.
     *
     * @param delegate
     */
    public PromptTextAreaUI(TextUI delegate) {
        super(delegate);
    }

    /**
     * Overrides {@link #getPromptComponent(JTextComponent)} to additionally
     * update {@link JTextArea} specific properties.
     */
    @Override
    public JTextComponent getPromptComponent(JTextComponent txt) {
        JTextArea lbl = (JTextArea) super.getPromptComponent(txt);
        JTextArea txtArea = (JTextArea) txt;

        lbl.setColumns(txtArea.getColumns());
        lbl.setRows(txtArea.getRows());

        return lbl;
    }

    /**
     * Returns a shared {@link JTextArea}.
     */
    @Override
    protected JTextComponent createPromptComponent() {
        txt.updateUI();
        return txt;
    }
}
