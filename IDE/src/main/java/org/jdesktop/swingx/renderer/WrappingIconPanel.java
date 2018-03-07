/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.Painter;

/**
 * Compound component for usage in tree renderer. <p>
 *
 * Supports setting an icon for the node and a delegate component
 * which is used to show the text/content of the node. The delegate
 * component can be shared across renderers. <p>
 *
 * This implements the PainterAware by delegating to the delegate component if that
 * is of type PainterAware. Does nothing if not.
 */
public class WrappingIconPanel extends JXPanel implements PainterAware, IconAware {
    protected JComponent delegate;
    JLabel iconLabel;
    String labelPosition = BorderLayout.CENTER; //2;
    int iconLabelGap;
    private Border ltorBorder;
    private Border rtolBorder;
    private boolean dropHackEnabled;
    private boolean extendsComponentOpacity;

    /**
     * Instantiates and configures a WrappingIconPanel with the dropHack
     * enabled.
     *
     */
    public WrappingIconPanel() {
        this(true);
    }
    /**
     * Instantiates and configures a WrappingIconPanel with the dropHack
     * property set as indicated by the boolean.
     *
     * @param dropHackEnabled a boolean indicating whether the drop hack should
     *        be enabled.
     *
     * @see #isVisible()
     */
    public WrappingIconPanel(boolean dropHackEnabled) {
        setOpaque(false);
        iconLabel = new JRendererLabel();
        iconLabelGap = iconLabel.getIconTextGap();
        iconLabel.setOpaque(false);
        updateIconBorder();
        setBorder(null);
        setLayout(new BorderLayout());
        add(iconLabel, BorderLayout.LINE_START);
        setDropHackEnabled(dropHackEnabled);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to update the icon position.
     */
    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        super.setComponentOrientation(o);
        updateIconBorder();
    }

    /**
     * Updates the icon position according to ComponentOrientation.
     */
    private void updateIconBorder() {
        if (ltorBorder == null) {
            ltorBorder = BorderFactory.createEmptyBorder(0, 0, 0, iconLabelGap);
            rtolBorder = BorderFactory.createEmptyBorder(0, iconLabelGap, 0, 0);
        }
        if (getComponentOrientation().isLeftToRight()) {
            iconLabel.setBorder(ltorBorder);
        } else {
            iconLabel.setBorder(rtolBorder);
        }
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon to use.
     */
    @Override
    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
        iconLabel.setText(null);
        validate();
    }

    /**
     * Returns the icon used in this panel, may be null.
     *
     * @return the icon used in this panel, may be null.
     */
    @Override
    public Icon getIcon() {
        return iconLabel.getIcon();
    }

    /**
     * Sets the delegate component.
     *
     * @param comp the component to add as delegate.
     */
    public void setComponent(JComponent comp) {
        JComponent old = getComponent();
        if (delegate != null) {
            remove(delegate);
        }
        delegate = comp;
        if (extendsComponentOpacity) {
            iconLabel.setOpaque(comp.isOpaque());
        } else {
            iconLabel.setOpaque(false);
        }
        add(delegate, labelPosition);
        validate();
        firePropertyChange("component", old, getComponent());
    }

    /**
     * Returns the delegate component.
     *
     * @return the delegate component.
     */
    public JComponent getComponent() {
        return delegate;
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set the background of the delegate and icon label as well.
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (iconLabel != null) {
            iconLabel.setBackground(bg);
        }
        if (delegate != null) {
            delegate.setBackground(bg);
        }
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set the foreground of the delegate and icon label as well.
     */
    @Override
    public void setForeground(Color bg) {
        super.setForeground(bg);
        if (iconLabel != null) {
            iconLabel.setForeground(bg);
        }
        if (delegate != null) {
            delegate.setForeground(bg);
        }
    }




    /**
     * {@inheritDoc} <p>
     *
     * Overridden to set the Font of the delegate as well.
     */
    @Override
    public void setFont(Font font) {
        if (delegate != null) {
            delegate.setFont(font);
        }
        super.setFont(font);
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Overridden to hack around #766-swingx: cursor flickering in DnD when
     * dragging over tree column. This is a core bug (#6700748) related to
     * painting the rendering component on a CellRendererPane. A trick around is
     * to let this return false.
     * <p>
     *
     * Some LayoutManagers don't layout an invisible component, so need to make
     * the hack-enabled configurable. This implementation will return false
     * if isDropHackEnabled, super.isVisible otherwise.
     */
    @Override
    public boolean isVisible() {
        return dropHackEnabled ? false : super.isVisible();
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Returns the delegate's Painter if it is of type PainterAware or null
     * otherwise.
     *
     * @return the delegate's Painter or null.
     */
    @Override
    public Painter<?> getPainter() {
        if (delegate instanceof PainterAware) {
            return ((PainterAware) delegate).getPainter();
        }
        return null;
    }

    /**
     * Sets the delegate's Painter if it is of type PainterAware. Does nothing otherwise.
     *
     * @param painter the Painter to apply to the delegate.
     */
    @Override
    public void setPainter(Painter<?> painter) {
        if (delegate instanceof PainterAware) {
            ((PainterAware) delegate).setPainter(painter);
        }

    }

    /**
     *
     * Returns the bounds of the delegate component or null if the delegate is null.
     *
     * PENDING JW: where do we use it? Maybe it was for testing only?
     *
     * @return the bounds of the delegate, or null if the delegate is null.
     */
    public Rectangle getDelegateBounds() {
        if (delegate == null) return null;
        return delegate.getBounds();
    }

    /**
     * Sets the dropHackEnabled property. <p>
     *
     * The default value is true.
     *
     * @param dropHackEnabled
     *
     * @see #isVisible()
     */
    public void setDropHackEnabled(boolean dropHackEnabled) {
        this.dropHackEnabled = dropHackEnabled;
    }

    /**
     * Sets a boolean indicating whether or not the main component's opacity
     * should be applied to the Icon region.<p>
     *
     * The default value is false. This covers the main use case in a JTree.
     *
     * @param extendsComponentOpacity
     */
    public void setExtendsComponentOpacity(boolean extendsComponentOpacity) {
        this.extendsComponentOpacity = extendsComponentOpacity;

    }
    /**
     * @return the extendsComponentOpacity
     */
    public boolean getExtendsComponentOpacity() {
        return extendsComponentOpacity;
    }
}
