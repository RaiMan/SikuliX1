/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.plaf.basic.BasicOptionPaneUI;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 * First cut for enhanced Dialog. The idea is to have a pluggable content
 * from which the dialog auto-configures all its "dialogueness".
 *
 * <ul>
 * <li> accepts a content and configures itself from content's properties -
 *  replaces the execute action from the appropriate action in content's action map (if any)
 *  and set's its title from the content's name.
 * <li> registers stand-in actions for close/execute with the dialog's RootPane
 * <li> registers keyStrokes for esc/enter to trigger the close/execute actions
 * <li> takes care of building the button panel using the close/execute actions.
 * </ul>
 *
 * <ul>
 * <li>TODO: add link to forum discussion, wiki summary?
 * <li>PENDING: add support for vetoing the close.
 * <li>PENDING: add complete set of constructors
 * <li>PENDING: add windowListener to delegate to close action
 * </ul>
 *
 * @author Jeanette Winzenburg
 * @author Karl Schaefer
 */
@JavaBean
public class JXDialog extends JDialog {

    static {
        // Hack to enforce loading of SwingX framework ResourceBundle
        LookAndFeelAddons.getAddon();
    }

    public static final String EXECUTE_ACTION_COMMAND = "execute";
    public static final String CLOSE_ACTION_COMMAND = "close";
    public static final String UIPREFIX = "XDialog.";

    protected JComponent content;

    /**
     * Creates a non-modal dialog with the given component as
     * content and without specified owner.  A shared, hidden frame will be
     * set as the owner of the dialog.
     * <p>
     * @param content the component to show and to auto-configure from.
     */
    public JXDialog(JComponent content) {
        super();
        setContent(content);
    }

    /**
     * Creates a non-modal dialog with the given component as content and the
     * specified <code>Frame</code> as owner.
     * <p>
     * @param frame the owner
     * @param content the component to show and to auto-configure from.
     */
    public JXDialog(Frame frame, JComponent content) {
        super(frame);
        setContent(content);
    }

    /**
     * Creates a non-modal dialog with the given component as content and the
     * specified <code>Dialog</code> as owner.
     * <p>
     * @param dialog the owner
     * @param content the component to show and to auto-configure from.
     */
    public JXDialog(Dialog dialog, JComponent content) {
        super(dialog);
        setContent(content);
    }

    /**
     * Creates a non-modal dialog with the given component as content and the
     * specified <code>Window</code> as owner.
     * <p>
     * @param window the owner
     * @param content the component to show and to auto-configure from.
     */
    public JXDialog(Window window, JComponent content) {
        super(window);
        setContentPane(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JXRootPane createRootPane() {
        return new JXRootPane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JXRootPane getRootPane() {
        return (JXRootPane) super.getRootPane();
    }

    /**
     * Sets the status bar property on the underlying {@code JXRootPane}.
     *
     * @param statusBar
     *            the {@code JXStatusBar} which is to be the status bar
     * @see #getStatusBar()
     * @see JXRootPane#setStatusBar(JXStatusBar)
     */
    public void setStatusBar(JXStatusBar statusBar) {
        getRootPane().setStatusBar(statusBar);
    }

    /**
     * Returns the value of the status bar property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JXStatusBar} which is the current status bar
     * @see #setStatusBar(JXStatusBar)
     * @see JXRootPane#getStatusBar()
     */
    public JXStatusBar getStatusBar() {
        return getRootPane().getStatusBar();
    }

    /**
     * Sets the tool bar property on the underlying {@code JXRootPane}.
     *
     * @param toolBar
     *            the {@code JToolBar} which is to be the tool bar
     * @see #getToolBar()
     * @see JXRootPane#setToolBar(JToolBar)
     */
    public void setToolBar(JToolBar toolBar) {
        getRootPane().setToolBar(toolBar);
    }

    /**
     * Returns the value of the tool bar property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JToolBar} which is the current tool bar
     * @see #setToolBar(JToolBar)
     * @see JXRootPane#getToolBar()
     */
    public JToolBar getToolBar() {
        return getRootPane().getToolBar();
    }

    /**
     * PENDING: widen access - this could be public to make the content really
     * pluggable?
     *
     * @param content
     */
    private void setContent(JComponent content) {
        if (this.content != null) {
            throw new IllegalStateException("content must not be set more than once");
        }
        initActions();
        Action contentCloseAction = content.getActionMap().get(CLOSE_ACTION_COMMAND);
        if (contentCloseAction != null) {
            putAction(CLOSE_ACTION_COMMAND, contentCloseAction);
        }
        Action contentExecuteAction = content.getActionMap().get(EXECUTE_ACTION_COMMAND);
        if (contentExecuteAction != null) {
            putAction(EXECUTE_ACTION_COMMAND, contentExecuteAction);
        }
        this.content = content;
        build();
        setTitleFromContent();
    }

    /**
     * Infers and sets this dialog's title from the the content.
     * Does nothing if content is null.
     *
     * Here: uses the content's name as title.
     */
    protected void setTitleFromContent() {
        if (content == null) return;
        setTitle(content.getName());
    }

    /**
     * pre: content != null.
     *
     */
    private void build() {
        JComponent contentBox = new Box(BoxLayout.PAGE_AXIS);
        contentBox.add(content);
        JComponent buttonPanel = createButtonPanel();
        contentBox.add(buttonPanel);
        contentBox.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
//        content.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

//        fieldPanel.setAlignmentX();
//      buttonPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        add(contentBox);

    }

    /**
     * {@inheritDoc}
     *
     * Overridden to check if content is available. <p>
     * PENDING: doesn't make sense - the content is immutable and guaranteed
     * to be not null.
     */
    @Override
    public void setVisible(boolean visible) {
        if (content == null) throw
            new IllegalStateException("content must be built before showing the dialog");
        super.setVisible(visible);
    }

//------------------------ dynamic locale support

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set the content's Locale and then updated
     * this dialog's internal state. <p>
     *
     *
     */
    @Override
    public void setLocale(Locale l) {
        /*
         * NOTE: this is called from super's constructor as one of the
         * first methods (prior to setting the rootPane!). So back out
         *
         */
        if (content != null) {
            content.setLocale(l);
            updateLocaleState(l);
        }
        super.setLocale(l);
    }

    /**
     * Updates this dialog's locale-dependent state.
     *
     * Here: updates title and actions.
     * <p>
     *
     *
     * @see #setLocale(Locale)
     */
    protected void updateLocaleState(Locale locale) {
        setTitleFromContent();
        for (Object key : getRootPane().getActionMap().allKeys()) {
            if (key instanceof String) {
                Action contentAction = content.getActionMap().get(key);
                Action rootPaneAction = getAction(key);
                if ((!rootPaneAction.equals(contentAction))) {
                    String keyString = getUIString((String) key, locale);
                    if (!key.equals(keyString)) {
                        rootPaneAction.putValue(Action.NAME, keyString);
                    }
                }
            }
        }
    }

    /**
     * The callback method executed when closing the dialog. <p>
     * Here: calls dispose.
     *
     */
    public void doClose() {
        dispose();
    }

    private void initActions() {
        Action defaultAction = createCloseAction();
        putAction(CLOSE_ACTION_COMMAND, defaultAction);
        putAction(EXECUTE_ACTION_COMMAND, defaultAction);
    }

    private Action createCloseAction() {
        String actionName = getUIString(CLOSE_ACTION_COMMAND);
        BoundAction action = new BoundAction(actionName,
                CLOSE_ACTION_COMMAND);
        action.registerCallback(this, "doClose");
        return action;
    }

    /**
     * create the dialog button controls.
     *
     *
     * @return panel containing button controls
     */
    protected JComponent createButtonPanel() {
        // PENDING: this is a hack until we have a dedicated ButtonPanel!
        JPanel panel = new JPanel(new BasicOptionPaneUI.ButtonAreaLayout(true, 6))
        {
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };

        panel.setBorder(BorderFactory.createEmptyBorder(9, 0, 0, 0));
        Action executeAction = getAction(EXECUTE_ACTION_COMMAND);
        Action closeAction = getAction(CLOSE_ACTION_COMMAND);

        JButton defaultButton = new JButton(executeAction);
        panel.add(defaultButton);
        getRootPane().setDefaultButton(defaultButton);

        if (executeAction != closeAction) {
            JButton b = new JButton(closeAction);
            panel.add(b);
            getRootPane().setCancelButton(b);
        }

        return panel;
    }

    /**
     * convenience wrapper to access rootPane's actionMap.
     * @param key
     * @param action
     */
    private void putAction(Object key, Action action) {
        getRootPane().getActionMap().put(key, action);
    }

    /**
     * convenience wrapper to access rootPane's actionMap.
     *
     * @param key
     * @return root pane's <code>ActionMap</code>
     */
    private Action getAction(Object key) {
        return getRootPane().getActionMap().get(key);
    }

    /**
     * Returns a potentially localized value from the UIManager. The given key
     * is prefixed by this component|s <code>UIPREFIX</code> before doing the
     * lookup. The lookup respects this table's current <code>locale</code>
     * property. Returns the key, if no value is found.
     *
     * @param key the bare key to look up in the UIManager.
     * @return the value mapped to UIPREFIX + key or key if no value is found.
     */
    protected String getUIString(String key) {
        return getUIString(key, getLocale());
    }

    /**
     * Returns a potentially localized value from the UIManager for the
     * given locale. The given key
     * is prefixed by this component's <code>UIPREFIX</code> before doing the
     * lookup. Returns the key, if no value is found.
     *
     * @param key the bare key to look up in the UIManager.
     * @param locale the locale use for lookup
     * @return the value mapped to UIPREFIX + key in the given locale,
     *    or key if no value is found.
     */
    protected String getUIString(String key, Locale locale) {
        String text = UIManagerExt.getString(UIPREFIX + key, locale);
        return text != null ? text : key;
    }
}
