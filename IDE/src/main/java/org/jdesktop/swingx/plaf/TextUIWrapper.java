/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.prompt.BuddySupport;

/**
 * TODO:
 *
 * @author Peter Weishapl <petw@gmx.net>
 *
 * @param <UI>
 */
public abstract class TextUIWrapper<UI extends TextUI> {
    private static final DefaultWrapper defaultWrapper = new DefaultWrapper();

    public static final TextUIWrapper<? extends PromptTextUI> getDefaultWrapper() {
        return defaultWrapper;
    }

    private Class<UI> wrapperClass;

    protected TextUIWrapper(Class<UI> wrapperClass) {
        this.wrapperClass = wrapperClass;
    }

    /**
     * <p>
     * Wraps and replaces the current UI of the given <code>textComponent</code>, by calling
     * {@link #wrapUI(JTextComponent)} if necessary.
     * </p>
     *
     * @param textComponent
     * @param stayOnUIChange
     *            if <code>true</code>, a {@link PropertyChangeListener} is registered, which
     *            listens for UI changes and wraps any new UI object.
     */
    public final void install(final JTextComponent textComponent, boolean stayOnUIChange) {
        replaceUIIfNeeded(textComponent);
        if (stayOnUIChange) {
            uiChangeHandler.install(textComponent);
        }
    }

    /**
     * Wraps and replaces the text components current UI by calling {@link #wrapUI(TextUI)}, if the
     * text components current UI is not an instance of the given wrapper class.
     *
     * @param textComponent
     * @return <code>true</code> if the UI has been replaced
     */
    protected boolean replaceUIIfNeeded(JTextComponent textComponent) {
        if (wrapperClass.isAssignableFrom(textComponent.getUI().getClass())) {
            return false;
        }

        textComponent.setUI(wrapUI(textComponent));

        return true;
    }

    /**
     * Override to return the appropriate UI wrapper object for the given {@link TextUI}.
     *
     * @param textUI
     * @return the wrapping UI
     */
    public abstract UI wrapUI(JTextComponent textComponent);

    /**
     * Returns the wrapper class.
     *
     * @return the wrapper class
     */
    public Class<UI> getWrapperClass() {
        return wrapperClass;
    }

    /**
     * <p>
     * Removes the {@link PropertyChangeListener}, which listens for "UI" property changes (if
     * installed) and then calls {@link JComponent#updateUI()} on the <code>textComponent</code> to
     * set the UI object provided by the current {@link UIDefaults}.
     * </p>
     *
     * @param textComponent
     */
    public final void uninstall(final JTextComponent textComponent) {
        uiChangeHandler.uninstall(textComponent);
        textComponent.updateUI();
    }

    private final TextUIChangeHandler uiChangeHandler = new TextUIChangeHandler();

    private final class TextUIChangeHandler extends AbstractUIChangeHandler {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JTextComponent txt = (JTextComponent) evt.getSource();

            replaceUIIfNeeded(txt);
        }
    }

    public static final class DefaultWrapper extends TextUIWrapper<PromptTextUI> {
        private DefaultWrapper() {
            super(PromptTextUI.class);
        }

        /**
         * <p>
         * Creates a new {@link PromptTextUI}, which wraps the given <code>textComponent</code>s UI.
         * </p>
         * <p>
         * If the UI is already of type {@link PromptTextUI}, it will be returned. If
         * <code>textComponent</code> is of type {@link JXSearchField} a new {@link SearchFieldUI}
         * object will be returned. If <code>textComponent</code> is of type {@link JTextField} or
         * {@link JTextArea} a {@link BuddyTextFieldUI} or {@link PromptTextAreaUI} will be
         * returned, respectively. If the UI is of any other type, a
         * {@link IllegalArgumentException} will be thrown.
         * </p>
         *
         * @param textComponent
         *            wrap this components UI
         * @return a {@link PromptTextUI} which wraps the <code>textComponent</code>s UI.
         */
        @Override
        public PromptTextUI wrapUI(JTextComponent textComponent) {
            TextUI textUI = textComponent.getUI();

            if (textUI instanceof PromptTextUI) {
                return (PromptTextUI) textUI;
            } else if (textComponent instanceof JXSearchField) {
                return new SearchFieldUI(textUI);
            } else if (textComponent instanceof JTextField) {
                return new BuddyTextFieldUI(textUI);
            } else if (textComponent instanceof JTextArea) {
                return new PromptTextAreaUI(textUI);
            }
            throw new IllegalArgumentException("ui implementation not supported: "
                    + textUI.getClass());
        }

        /**
         * Every time the UI needs to be replaced we also need to make sure, that all buddy
         * components are also in the component hierarchy. (That's because {@link BasicTextUI}
         * removes all our buddies upon UI changes).
         */
        @Override
        protected boolean replaceUIIfNeeded(JTextComponent textComponent) {
            boolean replaced = super.replaceUIIfNeeded(textComponent);

            if (replaced && textComponent instanceof JTextField) {
                BuddySupport.ensureBuddiesAreInComponentHierarchy((JTextField) textComponent);
            }
            return replaced;
        }
    }
}
