/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.plaf.ButtonUI;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.hyperlink.HyperlinkAction;
import org.jdesktop.swingx.plaf.HyperlinkAddon;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

/**
 * A hyperlink component that derives from JButton to provide compatibility
 * mostly for binding actions enabled/disabled behavior accessibility i18n etc...
 * <p>
 *
 * This button has visual state related to a notion of "clicked":
 * foreground color is unclickedColor or clickedColor depending on
 * its boolean bound property clicked being false or true, respectively.
 * If the hyperlink has an action, it guarantees to synchronize its
 * "clicked" state to an action value with key LinkAction.VISITED_KEY.
 * Synchronization happens on setAction() and on propertyChange notification
 * from the action. JXHyperlink accepts any type of action -
 * {@link AbstractHyperlinkAction} is a convenience implementation to
 * simplify clicked control.
 * <p>
 *
 * <pre> <code>
 *      LinkAction linkAction = new LinkAction("http://swinglabs.org") {
 *            public void actionPerformed(ActionEvent e) {
 *                doSomething(getTarget());
 *                setVisited(true);
 *            }
 *      };
 *      JXHyperlink hyperlink = new JXHyperlink(linkAction);
 * <code> </pre>
 *
 * The hyperlink can be configured to always update its clicked
 * property after firing the actionPerformed:
 *
 * <pre> <code>
 *      JXHyperlink hyperlink = new JXHyperlink(action);
 *      hyperlink.setOverrulesActionOnClick(true);
 * <code> </pre>
 *
 * By default, this property is false. The hyperlink will
 * auto-click only if it has no action. Developers can change the
 * behaviour by overriding {@link JXHyperlink#isAutoSetClicked()};
 *
 * @author Richard Bair
 * @author Shai Almog
 * @author Jeanette Winzenburg
 */
@JavaBean
public class JXHyperlink extends JButton {

    /**
     * @see #getUIClassID
     * @see #readObject
     */
    public static final String uiClassID = "HyperlinkUI";

    // ensure at least the default ui is registered
    static {
      LookAndFeelAddons.contribute(new HyperlinkAddon());
    }

    private boolean hasBeenVisited = false;

    /**
     * Color for the hyper link if it has not yet been clicked. This color can
     * be set both in code, and through the UIManager with the property
     * "JXHyperlink.unclickedColor".
     */
    private Color unclickedColor;

    /**
     * Color for the hyper link if it has already been clicked. This color can
     * be set both in code, and through the UIManager with the property
     * "JXHyperlink.clickedColor".
     */
    private Color clickedColor;

    private boolean overrulesActionOnClick;

    /**
     * Creates a new instance of JXHyperlink with default parameters
     */
    public JXHyperlink() {
        this(null);
    }

    /**
     * Creates a new instance of JHyperLink and configures it from provided Action.
     *
     * @param action Action whose parameters will be borrowed to configure newly
     *        created JXHyperLink
     */
    public JXHyperlink(Action action) {
        super();
        setAction(action);
        init();
    }

    /**
     * Convenience method to create and install a HyperlinkAction for the given URI.
     *
     * @param uri
     *            to uri to create a HyperlinkAction for, maybe null.
     * @throws HeadlessException
     *             if {@link GraphicsEnvironment#isHeadless()} returns {@code true}
     * @throws UnsupportedOperationException
     *             if the current platform doesn't support Desktop
     *
     * @see HyperlinkAction#createHyperlinkAction(URI)
     */
    public void setURI(URI uri) {
        setAction(HyperlinkAction.createHyperlinkAction(uri));
    }

    /**
     * Returns the foreground color for unvisited links.
     *
     * @return Color for the hyper link if it has not yet been clicked.
     */
    public Color getUnclickedColor() {
        return unclickedColor;
    }

    /**
     * Sets the color for the previously visited link. This value will override the one
     * set by the "JXHyperlink.clickedColor" UIManager property and defaults.
     *
     * @param color Color for the hyper link if it has already been clicked.
     */
    public void setClickedColor(Color color) {
        Color old = getClickedColor();
        clickedColor = color;
        if (isClicked()) {
            setForeground(getClickedColor());
        }
        firePropertyChange("clickedColor", old, getClickedColor());
    }

    /**
     * Returns the foreground color for visited links.
     *
     * @return Color for the hyper link if it has already been clicked.
     */
    public Color getClickedColor() {
        return clickedColor;
    }

    /**
     * Sets the color for the previously not visited link. This value will override the one
     * set by the "JXHyperlink.unclickedColor" UIManager property and defaults.
     *
     * @param color Color for the hyper link if it has not yet been clicked.
     */
    public void setUnclickedColor(Color color) {
        Color old = getUnclickedColor();
        unclickedColor = color;
        if (!isClicked()) {
            setForeground(getUnclickedColor());
        }
        firePropertyChange("unclickedColor", old, getUnclickedColor());
    }

    /**
     * Sets the clicked property and updates visual state depending on clicked.
     * This implementation updated the foreground color.
     * <p>
     *
     * NOTE: as with all button's visual properties, this will not update the
     * backing action's "visited" state.
     *
     * @param clicked flag to indicate if the button should be regarded as
     *        having been clicked or not.
     * @see #isClicked()
     */
    public void setClicked(boolean clicked) {
        boolean old = isClicked();
        hasBeenVisited = clicked;
        setForeground(isClicked() ? getClickedColor() : getUnclickedColor());
        firePropertyChange("clicked", old, isClicked());
    }

    /**
     * Returns a boolean indicating if this link has already been visited.
     *
     * @return <code>true</code> if hyper link has already been clicked.
     * @see #setClicked(boolean)
     */
    public boolean isClicked() {
        return hasBeenVisited;
    }

    /**
     * Sets the overrulesActionOnClick property. It controls whether this
     * button should overrule the Action's visited property on actionPerformed. <p>
     *
     * The default value is <code>false</code>.
     *
     * @param overrule if true, fireActionPerformed will set clicked to true
     *   independent of action.
     *
     * @see #getOverrulesActionOnClick()
     * @see #setClicked(boolean)
     */
    public void setOverrulesActionOnClick(boolean overrule) {
        boolean old = getOverrulesActionOnClick();
        this.overrulesActionOnClick = overrule;
        firePropertyChange("overrulesActionOnClick", old, getOverrulesActionOnClick());
    }

    /**
     * Returns a boolean indicating whether the clicked property should be set
     * always on clicked.
     *
     * @return overrulesActionOnClick false if his button clicked property
     *         respects the Action's visited property. True if the clicked
     *         should be updated on every actionPerformed.
     *
     * @see #setOverrulesActionOnClick(boolean)
     * @see #setClicked(boolean)
     */
    public boolean getOverrulesActionOnClick() {
        return overrulesActionOnClick;
    }

    /**
     * {@inheritDoc} <p>
     * Overridden to respect the overrulesActionOnClick property.
     */
    @Override
    protected void fireActionPerformed(ActionEvent event) {
        super.fireActionPerformed(event);
        if (isAutoSetClicked()) {
            setClicked(true);
        }
    }

    /**
     * Returns a boolean indicating whether the clicked property should be set
     * after firing action events.
     * Here: true if no action or overrulesAction property is true.
     * @return true if fireActionEvent should force a clicked, false if not.
     */
    protected boolean isAutoSetClicked() {
        return getAction() == null || getOverrulesActionOnClick();
    }

    /**
     * Creates and returns a listener that will watch the changes of the
     * provided <code>Action</code> and will update JXHyperlink's properties
     * accordingly.
     */
    @Override
    protected PropertyChangeListener createActionPropertyChangeListener(
            final Action a) {
        final PropertyChangeListener superListener = super
                .createActionPropertyChangeListener(a);
        // JW: need to do something better - only weak refs allowed!
        // no way to hook into super
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (AbstractHyperlinkAction.VISITED_KEY.equals(evt.getPropertyName())) {
                    configureClickedPropertyFromAction(a);
                } else {
                    superListener.propertyChange(evt);
                }

            }

        };
    }

    /**
     * Read all the essential properties from the provided <code>Action</code>
     * and apply it to the <code>JXHyperlink</code>
     */
    @Override
    protected void configurePropertiesFromAction(Action a) {
        super.configurePropertiesFromAction(a);
        configureClickedPropertyFromAction(a);
    }

    private void configureClickedPropertyFromAction(Action a) {
        boolean clicked = false;
        if (a != null) {
            clicked = Boolean.TRUE.equals(a.getValue(AbstractHyperlinkAction.VISITED_KEY));

        }
        setClicked(clicked);
    }

    private void init() {
        setForeground(isClicked() ? getClickedColor() : getUnclickedColor());
    }

    /**
     * Returns a string that specifies the name of the L&F class
     * that renders this component.
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
      setUI((ButtonUI)LookAndFeelAddons.getUI(this, ButtonUI.class));
    }
}
