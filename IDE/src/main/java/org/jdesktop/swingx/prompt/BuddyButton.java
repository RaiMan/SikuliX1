/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.prompt;

import java.awt.Cursor;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

import org.jdesktop.swingx.plaf.SearchFieldUI;

/**
 * Non focusable, no border, no margin and insets button with no content area
 * filled.
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 */
public class BuddyButton extends JButton {
    public BuddyButton() {
        this(null);
    }

    public BuddyButton(String text) {
        super(text);
        setFocusable(false);
        setMargin(SearchFieldUI.NO_INSETS);

        // Windows UI will add 1 pixel for width and height, if this is true
        setFocusPainted(false);

        setBorderPainted(false);
        setContentAreaFilled(false);
        setIconTextGap(0);

        setBorder(null);

        setOpaque(false);

        setCursor(Cursor.getDefaultCursor());
    }

    // Windows UI overrides Insets.
    // Who knows what other UIs are doing...
    @Override
    public Insets getInsets() {
        return SearchFieldUI.NO_INSETS;
    }

    @Override
    public Insets getInsets(Insets insets) {
        return getInsets();
    }

    @Override
    public Insets getMargin() {
        return getInsets();
    }

    @Override
    public void setBorder(Border border) {
        // Don't let Motif overwrite my Border
        super.setBorder(BorderFactory.createEmptyBorder());
    }
}
