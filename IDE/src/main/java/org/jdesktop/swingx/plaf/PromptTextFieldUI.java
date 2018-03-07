/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.search.NativeSearchFieldSupport;
import org.jdesktop.swingx.util.OS;

/**
 * {@link PromptTextUI} implementation for rendering prompts on
 * {@link JTextField}s and uses a {@link JTextField} as a prompt component.
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 */
public class PromptTextFieldUI extends PromptTextUI {
    /**
     * Creates a new {@link PromptTextFieldUI}.
     *
     * @param delegate
     */
    public PromptTextFieldUI(TextUI delegate) {
        super(delegate);
    }

    /**
     * Overrides {@link #getPromptComponent(JTextComponent)} to additionally
     * update {@link JTextField} specific properties.
     */
    @Override
    public JTextComponent getPromptComponent(JTextComponent txt) {
        LabelField lbl = (LabelField) super.getPromptComponent(txt);
        JTextField txtField = (JTextField) txt;

        lbl.setHorizontalAlignment(txtField.getHorizontalAlignment());
        lbl.setColumns(txtField.getColumns());

        // Make search field in Leopard paint focused border.
        lbl.hasFocus = txtField.hasFocus()
                && NativeSearchFieldSupport.isNativeSearchField(txtField);

        // leopard client properties. see
        // http://developer.apple.com/technotes/tn2007/tn2196.html#JTEXTFIELD_VARIANT
        NativeSearchFieldSupport.setSearchField(lbl, NativeSearchFieldSupport
                .isSearchField(txtField));
        NativeSearchFieldSupport.setFindPopupMenu(lbl, NativeSearchFieldSupport
                .getFindPopupMenu(txtField));

        // here we need to copy the border again for Mac OS X, because the above
        // calls may have replaced it.
        Border b = txt.getBorder();

        if (b == null) {
            lbl.setBorder(txt.getBorder());
        } else {
            Insets insets = b.getBorderInsets(txt);
            lbl.setBorder(
                    createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
        }
        //		lbl.setBorder(txtField.getBorder());

        // buddy support: not needed, because BuddyLayoutAndBorder queries
        // original text field
        // BuddySupport.setOuterMargin(lbl,
        // BuddySupport.getOuterMargin(txtField));
        // BuddySupport.setLeft(lbl, BuddySupport.getLeft(txtField));
        // BuddySupport.setRight(lbl, BuddySupport.getRight(txtField));

        return lbl;
    }

    /**
     * Returns a shared {@link JTextField}.
     */
    @Override
    protected JTextComponent createPromptComponent() {
        return new LabelField();
    }

    private static final class LabelField extends JTextField {
        boolean hasFocus;

        @Override
        public boolean hasFocus() {
            return hasFocus;
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to not automatically de/register itself from/to the ToolTipManager.
         * As rendering component it is not considered to be active in any way, so the
         * manager must not listen.
         */
        @Override
        public void setToolTipText(String text) {
            putClientProperty(TOOL_TIP_TEXT_KEY, text);
        }

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         *
         * @since 1.5
         */
        @Override
        public void invalidate() {}

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         */
        @Override
        public void validate() {}

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         */
        @Override
        public void revalidate() {}

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         */
        @Override
        public void repaint(long tm, int x, int y, int width, int height) {}

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         */
        @Override
        public void repaint(Rectangle r) { }

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         *
         * @since 1.5
         */
        @Override
        public void repaint() {
        }

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         */
        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        	if (OS.isMacOSX()) {
        		super.firePropertyChange(propertyName, oldValue, newValue);
        	} else {
        		// Strings get interned...
        		if ("document".equals(propertyName)) {
        			super.firePropertyChange(propertyName, oldValue, newValue);
        		}
        	}
        }

        /**
         * Overridden for performance reasons.
         * See the <a href="#override">Implementation Note</a>
         * for more information.
         */
        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        	if (OS.isMacOSX()) {
        		super.firePropertyChange(propertyName, oldValue, newValue);
        	}
        }
    }
}
