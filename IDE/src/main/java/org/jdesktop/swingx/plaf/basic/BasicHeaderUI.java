/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXHeader.IconPosition;
import org.jdesktop.swingx.JXLabel.MultiLineSupport;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.plaf.HeaderUI;
import org.jdesktop.swingx.plaf.PainterUIResource;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 * Base implementation of <code>Header</code> UI. <p>
 *
 * PENDING JW: This implementation is unusual in that it does not keep a reference
 * to the component it controls. Typically, such is only the case if the ui is
 * shared between instances. Historical? A consequence is that the un/install methods
 * need to carry the header as parameter. Which looks funny when at the same time
 * the children of the header are instance fields in this. Should think about cleanup:
 * either get rid off the instance fields here, or reference the header and remove
 * the param (would break subclasses).<p>
 *
 * PENDING JW: keys for uidefaults are inconsistent - most have prefix "JXHeader." while
 * defaultIcon has prefix "Header." <p>
 *
 * @author rbair
 * @author rah003
 * @author Jeanette Winzenburg
 */
public class BasicHeaderUI extends HeaderUI {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BasicHeaderUI.class
            .getName());
    // Implementation detail. Neeeded to expose getMultiLineSupport() method to allow restoring view
    // lost after LAF switch
    protected class DescriptionPane extends JXLabel {
            @Override
            public void paint(Graphics g) {
                // switch off jxlabel default antialiasing
                // JW: that cost me dearly to track down - it's the default foreground painter
                // which is an AbstractPainter which has _global_ antialiased on by default
                // and here the _text_ antialiased is turned off
                // changed JXLabel default foregroundPainter to have antialiasing false by
                // default, so remove interference here
                // part of fix for #920 - the other part is in JXLabel, fix 1164
//                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                super.paint(g);
            }

            @Override
            public MultiLineSupport getMultiLineSupport() {
                return super.getMultiLineSupport();
            }
    }

    protected JLabel titleLabel;
    protected DescriptionPane descriptionPane;
    protected JLabel imagePanel;
    private PropertyChangeListener propListener;
    private HierarchyBoundsListener boundsListener;
    private Color gradientLightColor;
    private Color gradientDarkColor;

    /** Creates a new instance of BasicHeaderUI */
    public BasicHeaderUI() {
    }

    /**
     * Returns an instance of the UI delegate for the specified component.
     * Each subclass must provide its own static <code>createUI</code>
     * method that returns an instance of that UI delegate subclass.
     * If the UI delegate subclass is stateless, it may return an instance
     * that is shared by multiple components.  If the UI delegate is
     * stateful, then it should return a new instance per component.
     * The default implementation of this method throws an error, as it
     * should never be invoked.
     */
    public static ComponentUI createUI(JComponent c) {
        return new BasicHeaderUI();
    }

    /**
     * Configures the specified component appropriate for the look and feel.
     * This method is invoked when the <code>ComponentUI</code> instance is being installed
     * as the UI delegate on the specified component.  This method should
     * completely configure the component for the look and feel,
     * including the following:
     * <ol>
     * <li>Install any default property values for color, fonts, borders,
     *     icons, opacity, etc. on the component.  Whenever possible,
     *     property values initialized by the client program should <i>not</i>
     *     be overridden.
     * <li>Install a <code>LayoutManager</code> on the component if necessary.
     * <li>Create/add any required sub-components to the component.
     * <li>Create/install event listeners on the component.
     * <li>Create/install a <code>PropertyChangeListener</code> on the component in order
     *     to detect and respond to component property changes appropriately.
     * <li>Install keyboard UI (mnemonics, traversal, etc.) on the component.
     * <li>Initialize any appropriate instance data.
     * </ol>
     * @param c the component where this UI delegate is being installed
     *
     * @see #uninstallUI
     * @see javax.swing.JComponent#setUI
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        assert c instanceof JXHeader;
        JXHeader header = (JXHeader)c;

        installDefaults(header);
        installComponents(header);
        installListeners(header);
    }

    /**
     * Reverses configuration which was done on the specified component during
     * <code>installUI</code>.  This method is invoked when this
     * <code>UIComponent</code> instance is being removed as the UI delegate
     * for the specified component.  This method should undo the
     * configuration performed in <code>installUI</code>, being careful to
     * leave the <code>JComponent</code> instance in a clean state (no
     * extraneous listeners, look-and-feel-specific property objects, etc.).
     * This should include the following:
     * <ol>
     * <li>Remove any UI-set borders from the component.
     * <li>Remove any UI-set layout managers on the component.
     * <li>Remove any UI-added sub-components from the component.
     * <li>Remove any UI-added event/property listeners from the component.
     * <li>Remove any UI-installed keyboard UI from the component.
     * <li>Nullify any allocated instance data objects to allow for GC.
     * </ol>
     * @param c the component from which this UI delegate is being removed;
     *          this argument is often ignored,
     *          but might be used if the UI object is stateless
     *          and shared by multiple components
     *
     * @see #installUI
     * @see javax.swing.JComponent#updateUI
     */
    @Override
    public void uninstallUI(JComponent c) {
        assert c instanceof JXHeader;
        JXHeader header = (JXHeader)c;

        uninstallListeners(header);
        uninstallComponents(header);
        uninstallDefaults(header);
    }

    /**
     * Installs default header properties.
     * <p>
     *
     * NOTE: this method is called before the children are created, so must not
     * try to access any of those!.
     *
     * @param header the header to install.
     */
    protected void installDefaults(JXHeader header) {
        gradientLightColor = UIManagerExt.getColor("JXHeader.startBackground");
        if (gradientLightColor == null) {
            // fallback to white
            gradientLightColor = Color.WHITE;
        }
        gradientDarkColor = UIManagerExt.getColor("JXHeader.background");
        // for backwards compatibility (mostly for substance and synthetica,
        // I suspect) I'll fall back on the "control" color if
        // JXHeader.background
        // isn't specified.
        if (gradientDarkColor == null) {
            gradientDarkColor = UIManagerExt.getColor("control");
        }

        if (isUIInstallable(header.getBackgroundPainter())) {
            header.setBackgroundPainter(createBackgroundPainter());
        }

        // title properties
        if (isUIInstallable(header.getTitleFont())) {
            Font titleFont = UIManager.getFont("JXHeader.titleFont");
            // fallback to label font
            header.setTitleFont(titleFont != null ? titleFont : UIManager
                    .getFont("Label.font"));
        }
        if (isUIInstallable(header.getTitleForeground())) {
            Color titleForeground = UIManagerExt
                    .getColor("JXHeader.titleForeground");
            // fallback to label foreground
            header.setTitleForeground(titleForeground != null ? titleForeground
                    : UIManagerExt.getColor("Label.foreground"));
        }

        // description properties
        if (isUIInstallable(header.getDescriptionFont())) {
            Font descFont = UIManager.getFont("JXHeader.descriptionFont");
            // fallback to label font
            header.setDescriptionFont(descFont != null ? descFont : UIManager
                    .getFont("Label.font"));
        }
        if (isUIInstallable(header.getDescriptionForeground())) {
            Color descForeground = UIManagerExt
                    .getColor("JXHeader.descriptionForeground");
            // fallback to label foreground
            header.setDescriptionForeground(descForeground != null ? descForeground
                    : UIManagerExt.getColor("Label.foreground"));
        }

        // icon label properties
        if (isUIInstallable(header.getIcon())) {
            header.setIcon(UIManager.getIcon("Header.defaultIcon"));
        }
    }

    /**
     * Uninstalls the given header's default properties. This implementation
     * does nothing.
     *
     * @param h the header to ininstall the properties from.
     */
    protected void uninstallDefaults(JXHeader h) {
    }

    /**
     * Creates, configures, adds contained components.
     * PRE: header's default properties must be set before calling this.
     *
     * @param header the header to install the components into.
     */
    protected void installComponents(JXHeader header) {
        titleLabel = new JLabel();
        descriptionPane = new DescriptionPane();
        imagePanel = new JLabel();
        installComponentDefaults(header);
        header.setLayout(new GridBagLayout());
        resetLayout(header);
    }

    /**
     * Unconfigures, removes and nulls contained components.
     *
     * @param header the header to install the components into.
     */
    protected void uninstallComponents(JXHeader header) {
        uninstallComponentDefaults(header);
        header.remove(titleLabel);
        header.remove(descriptionPane);
        header.remove(imagePanel);
        titleLabel = null;
        descriptionPane = null;
        imagePanel = null;
    }

    /**
     * Configures the component default properties from the given header.
     *
     * @param header the header to install the components into.
     */
    protected void installComponentDefaults(JXHeader header) {
        // JW: force a not UIResource for properties which have ui default values
        // like color, font, ??
        titleLabel.setFont(getAsNotUIResource(header.getTitleFont()));
        titleLabel.setForeground(getAsNotUIResource(header.getTitleForeground()));
        titleLabel.setText(header.getTitle());
        descriptionPane.setFont(getAsNotUIResource(header.getDescriptionFont()));
        descriptionPane.setForeground(getAsNotUIResource(header.getDescriptionForeground()));
        descriptionPane.setOpaque(false);
        descriptionPane.setText(header.getDescription());
        descriptionPane.setLineWrap(true);

        imagePanel.setIcon(header.getIcon());

    }

    /**
     * Returns a Font based on the param which is not of type UIResource.
     *
     * @param font the base font
     * @return a font not of type UIResource, may be null.
     */
    private Font getAsNotUIResource(Font font) {
        if (!(font instanceof UIResource)) return font;
        // PENDING JW: correct way to create another font instance?
       return font.deriveFont(font.getAttributes());
    }

    /**
     * Returns a Color based on the param which is not of type UIResource.
     *
     * @param color the base color
     * @return a color not of type UIResource, may be null.
     */
    private Color getAsNotUIResource(Color color) {
        if (!(color instanceof UIResource)) return color;
        // PENDING JW: correct way to create another color instance?
        float[] rgb = color.getRGBComponents(null);
        return new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
    }

    /**
     * Checks and returns whether the given property should be replaced
     * by the UI's default value.<p>
     *
     * PENDING JW: move as utility method ... where?
     *
     * @param property the property to check.
     * @return true if the given property should be replaced by the UI#s
     *   default value, false otherwise.
     */
    private boolean isUIInstallable(Object property) {
       return (property == null) || (property instanceof UIResource);
    }

    /**
     * Uninstalls component defaults. This implementation does nothing.
     *
     * @param header the header to uninstall from.
     */
    protected void uninstallComponentDefaults(JXHeader header) {
    }

    protected void installListeners(final JXHeader header) {
        propListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                onPropertyChange(header, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        };
        boundsListener = new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                if (header == e.getComponent()) {
                    View v = (View) descriptionPane.getClientProperty(BasicHTML.propertyKey);
                    // view might get lost on LAF change ...
                    if (v == null) {
                        descriptionPane.putClientProperty(BasicHTML.propertyKey,
                                MultiLineSupport.createView(descriptionPane));
                        v = (View) descriptionPane.getClientProperty(BasicHTML.propertyKey);
                    }
                    if (v != null) {
                        Container tla = header.getTopLevelAncestor();
                        if (tla == null) {
                            tla = header.getParent();
                            while (tla.getParent() != null) {
                                tla = tla.getParent();
                            }
                        }
                        int h = Math.max(descriptionPane.getHeight(), tla.getHeight());
                        int w = Math.min(tla.getWidth(), header.getParent().getWidth());
                        // 35 = description pane insets, TODO: obtain dynamically
                        w -= 35 + header.getInsets().left + header.getInsets().right + descriptionPane.getInsets().left + descriptionPane.getInsets().right + imagePanel.getInsets().left + imagePanel.getInsets().right + imagePanel.getWidth() + descriptionPane.getBounds().x;
                        v.setSize(w, h);
                        descriptionPane.setSize(w, (int) Math.ceil(v.getPreferredSpan(View.Y_AXIS)));
                    }
                }
            }};
        header.addPropertyChangeListener(propListener);
        header.addHierarchyBoundsListener(boundsListener);
    }

    protected void uninstallListeners(JXHeader h) {
        h.removePropertyChangeListener(propListener);
        h.removeHierarchyBoundsListener(boundsListener);
    }

    protected void onPropertyChange(JXHeader h, String propertyName, Object oldValue, final Object newValue) {
        if ("title".equals(propertyName)) {
            titleLabel.setText(h.getTitle());
        } else if ("description".equals(propertyName)) {
            descriptionPane.setText(h.getDescription());
        } else if ("icon".equals(propertyName)) {
            imagePanel.setIcon(h.getIcon());
        } else if ("enabled".equals(propertyName)) {
            boolean enabled = h.isEnabled();
            titleLabel.setEnabled(enabled);
            descriptionPane.setEnabled(enabled);
            imagePanel.setEnabled(enabled);
        } else if ("titleFont".equals(propertyName)) {
            titleLabel.setFont((Font)newValue);
        } else if ("descriptionFont".equals(propertyName)) {
            descriptionPane.setFont((Font)newValue);
        } else if ("titleForeground".equals(propertyName)) {
            titleLabel.setForeground((Color)newValue);
        } else if ("descriptionForeground".equals(propertyName)) {
            descriptionPane.setForeground((Color)newValue);
        } else if ("iconPosition".equals(propertyName)) {
            resetLayout(h);
        }
    }

    private void resetLayout(JXHeader h) {
        h.remove(titleLabel);
        h.remove(descriptionPane);
        h.remove(imagePanel);
        if (h.getIconPosition() == null || h.getIconPosition() == IconPosition.RIGHT) {
            h.add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 0, 11), 0, 0));
            h.add(descriptionPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 24, 12, 11), 0, 0));
            h.add(imagePanel, new GridBagConstraints(1, 0, 1, 2, 0.0, 1.0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, new Insets(12, 0, 11, 11), 0, 0));
        } else {
            h.add(titleLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 0, 11), 0, 0));
            h.add(descriptionPane, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 24, 12, 11), 0, 0));
            h.add(imagePanel, new GridBagConstraints(0, 0, 1, 2, 0.0, 1.0, GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE, new Insets(12, 11, 0, 11), 0, 0));
        }
    }


    protected Painter createBackgroundPainter() {
        MattePainter p = new MattePainter(new GradientPaint(0, 0, gradientLightColor, 1, 0, gradientDarkColor));
        p.setPaintStretched(true);
        return new PainterUIResource(p);
    }

}
