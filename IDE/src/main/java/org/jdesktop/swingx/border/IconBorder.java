/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.border;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.icon.EmptyIcon;

/**
 * {@code IconBorder} creates a border that places an {@code Icon} in the border
 * on the horizontal axis. The border does not add any additional insets other
 * than the inset required to produce the space for the icon. If additional
 * insets are required, users should create a
 * {@link javax.swing.border.CompoundBorder compund border}.
 * <p>
 * This border is useful when attempting to add {@code Icon}s to pre-existing
 * components without requiring specialty painting.
 *
 * @author Amy Fowler
 * @author Karl Schaefer
 *
 * @version 1.1
 */
@JavaBean
public class IconBorder implements Border, Serializable {

    /**
     * An empty icon.
     */
    public static final Icon EMPTY_ICON = new EmptyIcon();
    private int padding;
    private Icon icon;
    private int iconPosition;
    private Rectangle iconBounds = new Rectangle();

    /**
     * Creates an {@code IconBorder} with an empty icon in a trailing position
     * with a padding of 4.
     *
     * @see #EMPTY_ICON
     */
    public IconBorder() {
        this(null);
    }

    /**
     * Creates an {@code IconBorder} with the specified icon in a trailing
     * position with a padding of 4.
     *
     * @param validIcon
     *            the icon to set. This may be {@code null} to represent an
     *            empty icon.
     * @see #EMPTY_ICON
     */
    public IconBorder(Icon validIcon) {
        this(validIcon, SwingConstants.TRAILING);
    }

    /**
     * Creates an {@code IconBorder} with the specified constraints and a
     * padding of 4.
     *
     * @param validIcon
     *            the icon to set. This may be {@code null} to represent an
     *            empty icon.
     * @param iconPosition
     *            the position to place the icon relative to the component
     *            contents. This must be one of the following
     *            {@code SwingConstants}:
     *            <ul>
     *            <li>{@code LEADING}</li>
     *            <li>{@code TRAILING}</li>
     *            <li>{@code EAST}</li>
     *            <li>{@code WEST}</li>
     *            </ul>
     * @throws IllegalArgumentException
     *             if {@code iconPosition} is not a valid position.
     * @see #EMPTY_ICON
     */
    public IconBorder(Icon validIcon, int iconPosition) {
        this(validIcon, iconPosition, 4);
    }

    /**
     * Creates an {@code IconBorder} with the specified constraints. If
     * {@code validIcon} is {@code null}, {@code EMPTY_ICON} is used instead.
     * If {@code padding} is negative, then the border does not use padding.
     *
     * @param validIcon
     *            the icon to set. This may be {@code null} to represent an
     *            empty icon.
     * @param iconPosition
     *            the position to place the icon relative to the component
     *            contents. This must be one of the following
     *            {@code SwingConstants}:
     *            <ul>
     *            <li>{@code LEADING}</li>
     *            <li>{@code TRAILING}</li>
     *            <li>{@code EAST}</li>
     *            <li>{@code WEST}</li>
     *            </ul>
     * @param padding
     *            the padding to surround the icon with. All non-positive values
     *            set the padding to 0.
     * @throws IllegalArgumentException
     *             if {@code iconPosition} is not a valid position.
     * @see #EMPTY_ICON
     */
    public IconBorder(Icon validIcon, int iconPosition, int padding) {
        setIcon(validIcon);
        setPadding(padding);
        setIconPosition(iconPosition);
    }

    private boolean isValidPosition(int position) {
        boolean result = false;

        switch (position) {
        case SwingConstants.LEADING:
        case SwingConstants.TRAILING:
        case SwingConstants.EAST:
        case SwingConstants.WEST:
            result = true;
            break;
        default:
            result = false;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Insets getBorderInsets(Component c) {
        int horizontalInset = icon.getIconWidth() + (2 * padding);
        int iconPosition = bidiDecodeLeadingTrailing(c.getComponentOrientation(), this.iconPosition);
        if (iconPosition == SwingConstants.EAST) {
            return new Insets(0, 0, 0, horizontalInset);
        }
        return new Insets(0, horizontalInset, 0, 0);
    }

    /**
     * Sets the icon for this border.
     *
     * @param validIcon
     *            the icon to set.  This may be {@code null} to represent an
     *            empty icon.
     * @see #EMPTY_ICON
     */
    public void setIcon(Icon validIcon) {
        this.icon = validIcon == null ? EMPTY_ICON : validIcon;
    }

    /**
     * This border is not opaque.
     *
     * @return always returns {@code false}
     */
    public boolean isBorderOpaque() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
        int height) {
        int iconPosition = bidiDecodeLeadingTrailing(c.getComponentOrientation(), this.iconPosition);
        if (iconPosition == SwingConstants.NORTH_EAST) {
            iconBounds.y = y + padding;
            iconBounds.x = x + width - padding - icon.getIconWidth();
        } else if (iconPosition == SwingConstants.EAST) {    // EAST
            iconBounds.y = y
                + ((height - icon.getIconHeight()) / 2);
            iconBounds.x = x + width - padding - icon.getIconWidth();
        } else if (iconPosition == SwingConstants.WEST) {
            iconBounds.y = y
                + ((height - icon.getIconHeight()) / 2);
            iconBounds.x = x + padding;
        }
        iconBounds.width = icon.getIconWidth();
        iconBounds.height = icon.getIconHeight();
        icon.paintIcon(c, g, iconBounds.x, iconBounds.y);
    }

    /**
     * Returns EAST or WEST depending on the ComponentOrientation and
     * the given postion LEADING/TRAILING this method has no effect for other
     * position values
     */
    private int bidiDecodeLeadingTrailing(ComponentOrientation c, int position) {
        if(position == SwingConstants.TRAILING) {
            if(!c.isLeftToRight()) {
                return SwingConstants.WEST;
            }
            return SwingConstants.EAST;
        }
        if(position == SwingConstants.LEADING) {
            if(c.isLeftToRight()) {
                return SwingConstants.WEST;
            }
            return SwingConstants.EAST;
        }
        return position;
    }

    /**
     * Gets the padding surrounding the icon.
     *
     * @return the padding for the icon. This value is guaranteed to be
     *         nonnegative.
     */
    public int getPadding() {
        return padding;
    }

    /**
     * Sets the padding around the icon.
     *
     * @param padding
     *            the padding to set. If {@code padding < 0}, then
     *            {@code padding} will be set to {@code 0}.
     */
    public void setPadding(int padding) {
        this.padding = padding < 0 ? 0 : padding;
    }

    /**
     * Returns the position to place the icon (relative to the component contents).
     *
     * @return one of the following {@code SwingConstants}:
     *        <ul>
     *          <li>{@code LEADING}</li>
     *          <li>{@code TRAILING}</li>
     *          <li>{@code EAST}</li>
     *          <li>{@code WEST}</li>
     *        </ul>
     */
    public int getIconPosition() {
        return iconPosition;
    }

    /**
     * Sets the position to place the icon (relative to the component contents).
     *
     * @param iconPosition must be one of the following {@code SwingConstants}:
     *        <ul>
     *          <li>{@code LEADING}</li>
     *          <li>{@code TRAILING}</li>
     *          <li>{@code EAST}</li>
     *          <li>{@code WEST}</li>
     *        </ul>
     * @throws IllegalArgumentException
     *             if {@code iconPosition} is not a valid position.
     */
    public void setIconPosition(int iconPosition) {
        if (!isValidPosition(iconPosition)) {
            throw new IllegalArgumentException("Invalid icon position");
        }
        this.iconPosition = iconPosition;
    }

}
