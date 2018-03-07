/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.plaf.HeaderAddon;
import org.jdesktop.swingx.plaf.HeaderUI;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

/**
 * <p><code>JXHeader is a simple component consisting of a title, a description,
 * and an icon. An example of such a component can be seen on
 * <a href="http://jext.free.fr/header.png">Romain Guys ProgX website</a></p>
 *
 * <p><code>JXHeader</code> is a simple component that is also sufficiently
 * configurable to be usable. The description area
 * accepts HTML conforming to version 3.2 of the HTML standard. The icon, title,
 * and description are all configurable. <code>JXHeader</code> itself extends
 * {@link JXPanel}, providing translucency and painting delegates.</p>
 *
 * <p>If I were to reconstruct the ui shown in the above screenshot, I might
 * do so like this:<br/>
 * <pre><code>
 *      JXHeader header = new JXHeader();
 *      header.setTitle("Timing Framework Spline Editor");
 *      header.setDescription("Drag control points in the display to change the " +
 *          "shape of the spline\n" +
 *          "Click the Copy Code button to generate the corresponding Java code.");
 *      Icon icon = new ImageIcon(getClass().getResource("tools.png"));
 *      header.setIcon(icon);
 * </code></pre></p>
 *
 * Note: The HTML support doesn't exist yet. The UI delegate needs to discover whether
 * the text supplied is HTML or not, and change the content type of the editor pane
 * being used. The problem is that if "text/html" is always used, the font is wrong.
 * This same situation will be found in other parts of the code (JXErrorPane, for instance),
 * so this needs to be dealt with.
 *
 * <h2>Defaults</h2>
 * <p>BasicHeaderUI uses the following UI defaults:
 *  <ul>
 *      <li><b>Header.defaultIcon:</b> The default icon to use when creating a new JXHeader.</li>
 *  </ul>
 * </p>
 *
 * @status REVIEWED
 * @author rbair
 * @author rah003
 */
@JavaBean
public class JXHeader extends JXPanel {
    /**
     * SerialVersionUID.
     */
    private static final long serialVersionUID = 3593838231433068954L;

    /**
     * JXHeader pluggable UI key <i>HeaderUI</i>
     */
    public final static String uiClassID = "HeaderUI";

    // ensure at least the default ui is registered
    static {
        LookAndFeelAddons.contribute(new HeaderAddon());
    }

    /**
     * Specifies desired location of the icon relative to the title/description text.
     */
    public static enum IconPosition {
        /**
         * Positions icon left from the text.
         */
        LEFT,
        /**
         * Positions icon right from the text.
         */
        RIGHT
    }
    private String title;
    private String description;
    private Icon icon;
    private Font titleFont;
    private Font descriptionFont;
    private Color titleForeground;
    private Color descriptionForeground;
    private IconPosition iconPosition = IconPosition.RIGHT;

    /** Creates a new instance of JXHeader */
    public JXHeader() {
    }

    /**
     * Creates a new instance of JXHeader. PropertyChangeEvents are fired
     * when the title and description properties are set.
     *
     * @param title specifies the title property for this JXHeader
     * @param description specifies the description property for this JXHeader
     */
    public JXHeader(String title, String description) {
        this(title, description, null);
    }

    /** Creates a new instance of JXHeader. PropertyChangeEvents are fired
     * when the title and description properties are set.
     *
     * @param title specifies the title property for this JXHeader
     * @param description specifies the description property for this JXHeader
     * @param icon specifies the icon property for this JXHeader
     */
    public JXHeader(String title, String description, Icon icon) {
        setTitle(title);
        setDescription(description);
        setIcon(icon);
    }

    //------------------------------------------------------------- UI Logic

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderUI getUI() {
        return (HeaderUI)super.getUI();
    }

    /**
     * Sets the look and feel (L&F) object that renders this component.
     *
     * @param ui the HeaderUI L&F object
     * @see javax.swing.UIDefaults#getUI
     */
    public void setUI(HeaderUI ui) {
        super.setUI(ui);
    }

    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @return the string {@link #uiClassID}
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
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
        setUI((HeaderUI) LookAndFeelAddons
                .getUI(this, HeaderUI.class));
    }

    /**
     * Sets the title to use. This may be either plain text, or a simplified
     * version of HTML, as JLabel would use.
     *
     * @param title the Title. May be null.
     */
    public void setTitle(String title) {
        String old = getTitle();
        this.title = title;
        firePropertyChange("title", old, getTitle());
    }

    /**
     * Gets the title. This may use HTML, such as
     * that supported by JLabel (version 3.2 of the HTML spec).
     * @return the title. May be null.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the description for this header. This may use HTML, such as
     * that supported by JLabel (version 3.2 of the HTML spec).
     *
     * @param description the description. May be null, may be HTML or plain text.
     */
    public void setDescription(String description) {
        String old = getDescription();
        this.description = description;
        firePropertyChange("description", old, getDescription());
    }

    /**
     * Gets the description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the icon to use for the header. It is generally recommended that this
     * be an image 64x64 pixels in size, and that the icon have no gaps at the top.
     *
     * @param icon may be null
     */
    public void setIcon(Icon icon) {
        Icon old = getIcon();
        this.icon = icon;
        firePropertyChange("icon", old, getIcon());
    }

    /**
     * Gets the icon.
     *
     * @return the Icon being used. May be null.
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets new font for both, title and description line of the header.
     * @see javax.swing.JComponent#setFont(java.awt.Font)
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        setTitleFont(font);
        setDescriptionFont(font);
    }

    /**
     * Sets new font for title.
     * @param font New title font.
     */
    public void setTitleFont(Font font) {
        Font old = getTitleFont();
        this.titleFont = font;
        firePropertyChange("titleFont", old, getTitleFont());
    }

    /**
     * Gets title font.
     *
     * @return the Font being used. May be null.
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * Sets font for the description line of header.
     * @param font New description font.
     */
    public void setDescriptionFont(Font font) {
        Font old = getDescriptionFont();
        this.descriptionFont = font;
        firePropertyChange("descriptionFont", old, getDescriptionFont());
    }

    /**
     * Gets description font.
     *
     * @return the Font being used. May be null.
     */
    public Font getDescriptionFont() {
        return descriptionFont;
    }

    /**
     * Gets current title foreground color.
     * @return the Color used to paint title. May be null.
     */
    public Color getTitleForeground() {
        return titleForeground;
    }

    /**
     * Sets title foreground color.
     * @param titleForeground the Color to be used to paint title.
     */
    public void setTitleForeground(Color titleForeground) {
        Color old = getTitleForeground();
        this.titleForeground = titleForeground;
        firePropertyChange("titleForeground", old, getTitleForeground());
    }

    /**
     * Gets current description foreground color.
     * @return the Color used to paint description. May be null.
     */
    public Color getDescriptionForeground() {
        return descriptionForeground;
    }

    /**
     * Sets description foreground color.
     * @param descriptionForeground the Color to be used to paint description.
     */
    public void setDescriptionForeground(Color descriptionForeground) {
        Color old = getDescriptionForeground();
        this.descriptionForeground = descriptionForeground;
        firePropertyChange("descriptionForeground", old, getDescriptionForeground());
    }

    /**
     * Gets current icon position. Default is RIGHT.
     * @return Current Icon position.
     */
    public IconPosition getIconPosition() {
        return iconPosition;
    }

    /**
     * Sets new Icon position. Position is relative to the text. Default value is RIGHT.
     * @see #getIconPosition()
     * @param iconPosition new desired icon position
     */
    public void setIconPosition(IconPosition iconPosition) {
        IconPosition old = getIconPosition();
        this.iconPosition = iconPosition;
        firePropertyChange("iconPosition", old, getIconPosition());
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension s = super.getPreferredSize();
        // TODO: hack for JXLabel issue ... see JXHeaderVisualCheck.interactiveCustomProperties();
        s.width += 5;
        return s;
    }
}
