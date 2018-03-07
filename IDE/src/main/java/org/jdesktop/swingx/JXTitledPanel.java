/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.TitledPanelAddon;
import org.jdesktop.swingx.plaf.TitledPanelUI;

/**
 * A special type of Panel that has a Title section and a Content section.<br>
 * The following properties can be set with the UIManager to change the look
 * and feel of the JXTitledPanel:
 * <ul>
 * <li>JXTitledPanel.titleForeground</li>
 * <li>JXTitledPanel.titleBackground</li>
 * <li>JXTitledPanel.titleFont</li>
 * <li>JXTitledPanel.titlePainter</li>
 * <li>JXTitledPanel.captionInsets</li>
 * <li>JXTitledPanel.rightDecorationInsets</li>
 * <li>JXTitledPanel.leftDecorationInsets</li>
 * </ul>
 *
 * @author Richard Bair
 * @author Nicola Ken Barozzi
 * @author Jeanette Winzenburg
 */
@JavaBean
public class JXTitledPanel extends JXPanel {

    /**
     * @see #getUIClassID // *
     * @see #readObject
     */
    static public final String uiClassID = "TitledPanelUI";

    public static final String LEFT_DECORATION = "JXTitledPanel.leftDecoration";

    public static final String RIGHT_DECORATION = "JXTitledPanel.rightDecoration";

    /**
     * Initialization that would ideally be moved into various look and feel
     * classes.
     */
    static {
        LookAndFeelAddons.contribute(new TitledPanelAddon());
    }

    /**
     * The text to use for the title
     */
    private String title;

    /**
     * The Font to use for the Title
     */
    private Font titleFont;

    /**
     * The foreground color to use for the Title (particularly for the text)
     */
    private Color titleForeground;

    /**
     * The ContentPanel. Whatever this container is will be displayed in the
     * Content section
     */
    private Container contentPanel;

    /**
     * The Painter to use for painting the title section of the JXTitledPanel
     */
    private Painter titlePainter;

    /**
     * Create a new JTitledPanel with an empty string for the title.
     */
    public JXTitledPanel() {
        this(null);
    }

    /**
     * Create a new JTitledPanel with the given title as the title for the
     * panel.
     *
     * @param title
     */
    public JXTitledPanel(String title) {
        this(title, createDefaultContainer());
    }

    /**
     * Create a new JTitledPanel with the given String as the title, and the
     * given Container as the content panel.
     *
     * @param title
     * @param content
     */
    public JXTitledPanel(String title, Container content) {
        setTitle(title);
        setContentContainer(content);
    }

    /**
     * Returns the look and feel (L&F) object that renders this component.
     *
     * @return the TitledPanelUI object that renders this component
     */
    @Override
    public TitledPanelUI getUI() {
        return (TitledPanelUI) ui;
    }

    /**
     * Sets the look and feel (L&F) object that renders this component.
     *
     * @param ui
     *            the TitledPanelUI L&F object
     * @see javax.swing.UIDefaults#getUI
     * @beaninfo bound: true
     *          hidden: true attribute: visualUpdate true
     *     description: The UI object that implements the Component's LookAndFeel.
     */
    public void setUI(TitledPanelUI ui) {
        super.setUI(ui);
    }

    /**
     * Returns a string that specifies the name of the L&F class that renders
     * this component.
     *
     * @return "TitledPanelUI"
     * @see JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     * @beaninfo expert: true
     *      description: A string that specifies the name of the L&F class.
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void updateUI() {
        setUI((TitledPanelUI) LookAndFeelAddons
                .getUI(this, TitledPanelUI.class));
    }

    /**
     * Gets the title for this titled panel.
     *
     * @return the currently displayed title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title for this title panel.
     *
     * @param title
     *            the title to display
     */
    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = (title == null ? "" : title);
        // JW: fix swingx #9 - missing/incorrect notification
        // let standard notification handle
        // NOTE - "getting" the new property in the fire method is
        // intentional: there's no way of missing any transformations
        // on the parameter to set (like above: setting a
        // value depending on whether the input is null).
        firePropertyChange("title", oldTitle, getTitle());
    }

    public Container getContentContainer() {
        if (contentPanel == null) {
            contentPanel = new JXPanel();
            ((JXPanel) contentPanel).setBorder(BorderFactory
                    .createEmptyBorder());
            this.add(contentPanel, BorderLayout.CENTER);
        }
        return contentPanel;
    }

    public void setContentContainer(Container contentPanel) {
        if (this.contentPanel != null) {
            remove(this.contentPanel);
        }
        add(contentPanel, BorderLayout.CENTER);
        this.contentPanel = contentPanel;
    }

    /**
     * Adds the given JComponent as a decoration on the right of the title
     *
     * @param decoration
     */
    public void setRightDecoration(JComponent decoration) {
        JComponent old = getRightDecoration();
        getUI().setRightDecoration(decoration);
        firePropertyChange("rightDecoration", old, getRightDecoration());
    }

    public JComponent getRightDecoration() {
        return getUI().getRightDecoration();
    }

    /**
     * Adds the given JComponent as a decoration on the left of the title
     *
     * @param decoration
     */
    public void setLeftDecoration(JComponent decoration) {
        JComponent old = getLeftDecoration();
        getUI().setLeftDecoration(decoration);
        firePropertyChange("leftDecoration", old, getLeftDecoration());
    }

    public JComponent getLeftDecoration() {
        return getUI().getLeftDecoration();
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font titleFont) {
        Font old = getTitleFont();
        this.titleFont = titleFont;
        firePropertyChange("titleFont", old, getTitleFont());
    }

    /**
     * Set the Painter to use for painting the title section of the JXTitledPanel.
     * This value may be null, which will cause the current look and feel to paint
     * an appropriate look
     *
     * @param p The Painter to use. May be null
     */
    public void setTitlePainter(Painter p) {
        Painter old = getTitlePainter();
        this.titlePainter = p;
        firePropertyChange("titlePainter", old, getTitlePainter());
    }

    /**
     * @return the Painter to use for painting the background of the title section
     */
    public Painter getTitlePainter() {
        return titlePainter;
    }

    public Color getTitleForeground() {
        return titleForeground;
    }

    public void setTitleForeground(Color titleForeground) {
        Color old = getTitleForeground();
        this.titleForeground = titleForeground;
        firePropertyChange("titleForeground", old, getTitleForeground());
    }

    private static Container createDefaultContainer() {
        //TODO: All this default container creation stuff should be in the UI
        //delegate. Not enough time at the moment for me to do this right.
        JXPanel p = new JXPanel();
        return p;
    }

}
