/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.plaf.TitledPanelUI;

/**
 * All TitledPanels contain a title section and a content section. The default
 * implementation for the title section relies on a Gradient background. All
 * title sections can have components embedded to the &quot;left&quot; or
 * &quot;right&quot; of the Title.
 *
 * @author Richard Bair
 * @author Jeanette Winzenburg
 * @author rah003
 *
 */
public class BasicTitledPanelUI extends TitledPanelUI {
    private static final Logger LOG = Logger.getLogger(BasicTitledPanelUI.class.getName());

    /**
     * JLabel used for the title in the Title section of the JTitledPanel.
     */
    protected JLabel caption;
    /**
     * The Title section panel.
     */
    protected JXPanel topPanel;
    /**
     * Listens to changes in the title of the JXTitledPanel component
     */
    protected PropertyChangeListener titleChangeListener;

    protected JComponent left;
    protected JComponent right;

    /** Creates a new instance of BasicTitledPanelUI */
    public BasicTitledPanelUI() {
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
        return new BasicTitledPanelUI();
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
        assert c instanceof JXTitledPanel;
        JXTitledPanel titledPanel = (JXTitledPanel)c;
        installDefaults(titledPanel);

        caption = createAndConfigureCaption(titledPanel);
        topPanel = createAndConfigureTopPanel(titledPanel);

        installComponents(titledPanel);
        installListeners(titledPanel);
    }

    protected void installDefaults(JXTitledPanel titledPanel) {
        installProperty(titledPanel, "titlePainter", UIManager.get("JXTitledPanel.titlePainter"));
        installProperty(titledPanel, "titleForeground", UIManager.getColor("JXTitledPanel.titleForeground"));
        installProperty(titledPanel, "titleFont", UIManager.getFont("JXTitledPanel.titleFont"));
        LookAndFeel.installProperty(titledPanel, "opaque", false);
    }

    protected void uninstallDefaults(JXTitledPanel titledPanel) {
    }

    protected void installComponents(JXTitledPanel titledPanel) {
        topPanel.add(caption, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, getCaptionInsets(), 0, 0));
        if (titledPanel.getClientProperty(JXTitledPanel.RIGHT_DECORATION) instanceof JComponent) {
            setRightDecoration((JComponent) titledPanel.getClientProperty(JXTitledPanel.RIGHT_DECORATION));
        }
        if (titledPanel.getClientProperty(JXTitledPanel.LEFT_DECORATION) instanceof JComponent) {
            setLeftDecoration((JComponent) titledPanel.getClientProperty(JXTitledPanel.LEFT_DECORATION));
        }
        // swingx#500
        if (!(titledPanel.getLayout() instanceof BorderLayout)){
            titledPanel.setLayout(new BorderLayout());
        }
        titledPanel.add(topPanel, BorderLayout.NORTH);
        // fix #1063-swingx: must respect custom border
        if (SwingXUtilities.isUIInstallable(titledPanel.getBorder())) {
            // use uiresource border
            // old was: BorderFactory.createRaisedBevelBorder());
            titledPanel.setBorder(BorderUIResource.getRaisedBevelBorderUIResource());
        }
    }

    protected void uninstallComponents(JXTitledPanel titledPanel) {
        titledPanel.remove(topPanel);
    }

    protected Insets getCaptionInsets() {
      return UIManager.getInsets("JXTitledPanel.captionInsets");
    }

    protected JXPanel createAndConfigureTopPanel(JXTitledPanel titledPanel) {
        JXPanel topPanel = new JXPanel();
        topPanel.setBackgroundPainter(titledPanel.getTitlePainter());
        topPanel.setBorder(BorderFactory.createEmptyBorder());
        topPanel.setLayout(new GridBagLayout());
        topPanel.setOpaque(false);
        return topPanel;
    }

    protected JLabel createAndConfigureCaption(final JXTitledPanel titledPanel) {
        JLabel caption = new JLabel(titledPanel.getTitle()){
            //#501
            @Override
            public void updateUI(){
              super.updateUI();
              setForeground(titledPanel.getTitleForeground());
              setFont(titledPanel.getTitleFont());
            }
          };
        caption.setFont(titledPanel.getTitleFont());
        caption.setForeground(titledPanel.getTitleForeground());
        return caption;
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
        assert c instanceof JXTitledPanel;
        JXTitledPanel titledPanel = (JXTitledPanel) c;
        uninstallListeners(titledPanel);
        // JW: this is needed to make the gradient paint work correctly...
        // LF changes will remove the left/right components...
        topPanel.removeAll();
        titledPanel.remove(topPanel);
        titledPanel.putClientProperty(JXTitledPanel.LEFT_DECORATION, left);
        titledPanel.putClientProperty(JXTitledPanel.RIGHT_DECORATION, right);
        caption =  null;
        topPanel = null;
        titledPanel = null;
        left = null;
        right = null;
    }

    protected void installListeners(final JXTitledPanel titledPanel) {
        titleChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("title")) {
                    caption.setText((String)evt.getNewValue());
                } else if (evt.getPropertyName().equals("titleForeground")) {
                    caption.setForeground((Color)evt.getNewValue());
                } else if (evt.getPropertyName().equals("titleFont")) {
                    caption.setFont((Font)evt.getNewValue());
                } else if ("titlePainter".equals(evt.getPropertyName())) {
                    topPanel.setBackgroundPainter(titledPanel.getTitlePainter());
                    topPanel.repaint();
                }
            }
        };
        titledPanel.addPropertyChangeListener(titleChangeListener);
    }

    protected void uninstallListeners(JXTitledPanel titledPanel) {
        titledPanel.removePropertyChangeListener(titleChangeListener);
    }

    protected void installProperty(JComponent c, String propName, Object value) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(c.getClass());
            for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                if (pd.getName().equals(propName)) {
                    Method m = pd.getReadMethod();
                    Object oldVal = m.invoke(c);
                    if (oldVal == null || oldVal instanceof UIResource) {
                        m = pd.getWriteMethod();
                        m.invoke(c, value);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to install property " + propName, e);
        }
    }

    /**
     * Paints the specified component appropriate for the look and feel.
     * This method is invoked from the <code>ComponentUI.update</code> method when
     * the specified component is being painted.  Subclasses should override
     * this method and use the specified <code>Graphics</code> object to
     * render the content of the component.<p>
     *
     * PENDING JW: we don't need this, do we - remove!
     *
     * @param g the <code>Graphics</code> context in which to paint
     * @param c the component being painted;
     *          this argument is often ignored,
     *          but might be used if the UI object is stateless
     *          and shared by multiple components
     *
     * @see #update
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
    }

    /**
     * Adds the given JComponent as a decoration on the right of the title
     * @param decoration
     */
    @Override
    public void setRightDecoration(JComponent decoration) {
        if (right != null) topPanel.remove(right);
        right = decoration;
        if (right != null) {
            topPanel.add(decoration, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, UIManager.getInsets("JXTitledPanel.rightDecorationInsets"), 0, 0));

        }
    }

    @Override
    public JComponent getRightDecoration() {
        return right;
    }

    /**
     * Adds the given JComponent as a decoration on the left of the title
     * @param decoration
     */
    @Override
    public void setLeftDecoration(JComponent decoration) {
        if (left != null) topPanel.remove(left);
        left = decoration;
        if (left != null) {
            topPanel.add(left, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, UIManager.getInsets("JXTitledPanel.leftDecorationInsets"), 0, 0));
        }
    }

    @Override
    public JComponent getLeftDecoration() {
        return left;
    }

    /**
     * @return the Container acting as the title bar for this component
     */
    @Override
    public Container getTitleBar() {
        return topPanel;
    }
}
