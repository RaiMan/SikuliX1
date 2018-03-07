/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.beans.JavaBean;

/**
 * <p>A simple horizontal separator that contains a title.<br/>
 *
 * <p>JXTitledSeparator allows you to specify the title via the {@link #setTitle} method.
 * The title alignment may be specified by using the {@link #setHorizontalAlignment}
 * method, and accepts all the same arguments as the {@link javax.swing.JLabel#setHorizontalAlignment}
 * method.</p>
 *
 * <p>In addition, you may specify an Icon to use with this separator. The icon
 * will appear "leading" the title (on the left in left-to-right languages,
 * on the right in right-to-left languages). To change the position of the
 * title with respect to the icon, call {@link #setHorizontalTextPosition}.</p>
 *
 * <p>The default font and color of the title comes from the <code>LookAndFeel</code>, mimicking
 * the font and color of the {@link javax.swing.border.TitledBorder}</p>
 *
 * <p>Here are a few example code snippets:
 * <pre><code>
 *  //create a plain separator
 *  JXTitledSeparator sep = new JXTitledSeparator();
 *  sep.setTitle("Customer Info");
 *
 *  //create a separator with an icon
 *  sep = new JXTitledSeparator();
 *  sep.setTitle("Customer Info");
 *  sep.setIcon(new ImageIcon("myimage.png"));
 *
 *  //create a separator with an icon to the right of the title,
 *  //center justified
 *  sep = new JXTitledSeparator();
 *  sep.setTitle("Customer Info");
 *  sep.setIcon(new ImageIcon("myimage.png"));
 *  sep.setHorizontalAlignment(SwingConstants.CENTER);
 *  sep.setHorizontalTextPosition(SwingConstants.TRAILING);
 * </code></pre>
 *
 * @status REVIEWED
 * @author rbair
 */
@JavaBean
public class JXTitledSeparator extends JXPanel {
    /**
     * Implementation detail: the label used to display the title
     */
    private JLabel label;
    /**
     * Implementation detail: a separator to use on the left of the
     * title if alignment is centered or right justified
     */
    private JSeparator leftSeparator;
    /**
     * Implementation detail: a separator to use on the right of the
     * title if alignment is centered or left justified
     */
    private JSeparator rightSeparator;

    /**
     * Creates a new instance of <code>JXTitledSeparator</code>. The default title is simply
     * an empty string. Default justification is <code>LEADING</code>, and the default
     * horizontal text position is <code>TRAILING</code> (title follows icon)
     */
    public JXTitledSeparator() {
        this("Untitled");
    }

    /**
     * Creates a new instance of <code>JXTitledSeparator</code> with the specified
     * title. Default horizontal alignment is <code>LEADING</code>, and the default
     * horizontal text position is <code>TRAILING</code> (title follows icon)
     */
    public JXTitledSeparator(String title) {
        this(title, SwingConstants.LEADING, null);
    }

    /**
     * Creates a new instance of <code>JXTitledSeparator</code> with the specified
     * title and horizontal alignment. The default
     * horizontal text position is <code>TRAILING</code> (title follows icon)
     */
    public JXTitledSeparator(String title, int horizontalAlignment) {
        this(title, horizontalAlignment, null);
    }

    /**
     * Creates a new instance of <code>JXTitledSeparator</code> with the specified
     * title, icon, and horizontal alignment. The default
     * horizontal text position is <code>TRAILING</code> (title follows icon)
     */
    public JXTitledSeparator(String title, int horizontalAlignment, Icon icon) {
        setLayout(new GridBagLayout());

        label = new JLabel(title) {
            @Override
            public void updateUI(){
              super.updateUI();
              updateTitle();
            }
        };
        label.setIcon(icon);
        label.setHorizontalAlignment(horizontalAlignment);
        leftSeparator = new JSeparator();
        rightSeparator = new JSeparator();

        layoutSeparator();

        updateTitle();
        setOpaque(false);
    }

    /**
     * Implementation detail. Handles updates of title color and font on LAF change. For more
     * details see swingx#451.
     */
    //TODO remove this method in favor of UI delegate -- kgs
    protected void updateTitle()
    {
      if (label == null) return;

      Color c = label.getForeground();
      if (c == null || c instanceof ColorUIResource)
        setForeground(UIManager.getColor("TitledBorder.titleColor"));

      Font f = label.getFont();
      if (f == null || f instanceof FontUIResource)
        setFont(UIManager.getFont("TitledBorder.font"));
    }

    /**
     * Implementation detail. lays out this component, showing/hiding components
     * as necessary. Actually changes the containment (removes and adds components).
     * <code>JXTitledSeparator</code> is treated as a single component rather than
     * a container.
     */
    private void layoutSeparator() {
        removeAll();

        //SwingX #304 fix alignment issues
        //this is really a hacky fix, but a fix nonetheless
        //we need a better layout approach for this class
        int alignment = getHorizontalAlignment();

        if (!getComponentOrientation().isLeftToRight()) {
            switch (alignment) {
            case SwingConstants.LEFT:
                alignment = SwingConstants.RIGHT;
                break;
            case SwingConstants.RIGHT:
                alignment = SwingConstants.LEFT;
                break;
            case SwingConstants.EAST:
                alignment = SwingConstants.WEST;
                break;
            case SwingConstants.WEST:
                alignment = SwingConstants.EAST;
                break;
            default:
                break;
            }
        }

        switch (alignment) {
            case SwingConstants.LEFT:
            case SwingConstants.LEADING:
            case SwingConstants.WEST:
                add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                add(Box.createHorizontalStrut(3), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                add(rightSeparator, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
                break;
            case SwingConstants.RIGHT:
            case SwingConstants.TRAILING:
            case SwingConstants.EAST:
                add(rightSeparator, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
                add(Box.createHorizontalStrut(3), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                add(label, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                break;
            case SwingConstants.CENTER:
            default:
                add(leftSeparator, new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
                add(Box.createHorizontalStrut(3), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                add(label, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                add(Box.createHorizontalStrut(3), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
                add(rightSeparator, new GridBagConstraints(4, 0, 1, 1, 0.5, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
        }
    }

    /**
     * Sets the title for the separator. This may be simple html, or plain
     * text.
     *
     * @param title the new title. Any string input is acceptable
     */
    public void setTitle(String title) {
        String old = getTitle();
        label.setText(title);
        firePropertyChange("title", old, getTitle());
    }

    /**
     * Gets the title.
     *
     * @return the title being used for this <code>JXTitledSeparator</code>.
     *         This will be the raw title text, and so may include html tags etc
     *         if they were so specified in #setTitle.
     */
    public String getTitle() {
        return label.getText();
    }

    /**
     * <p>Sets the alignment of the title along the X axis. If leading, then
     * the title will lead the separator (in left-to-right languages,
     * the title will be to the left and the separator to the right). If centered,
     * then a separator will be to the left, followed by the title (centered),
     * followed by a separator to the right. Trailing will have the title
     * on the right with a separator to its left, in left-to-right languages.</p>
     *
     * <p>LEFT and RIGHT always position the text left or right of the separator,
     * respectively, regardless of the language orientation.</p>
     *
     * @param alignment  One of the following constants
     *           defined in <code>SwingConstants</code>:
     *           <code>LEFT</code>,
     *           <code>CENTER</code>,
     *           <code>RIGHT</code>,
     *           <code>LEADING</code> (the default) or
     *           <code>TRAILING</code>.
     *
     * @throws IllegalArgumentException if the alignment does not match one of
     *         the accepted inputs.
     * @see SwingConstants
     * @see #getHorizontalAlignment
     */
    public void setHorizontalAlignment(int alignment) {
        int old = getHorizontalAlignment();
        label.setHorizontalAlignment(alignment);
        if (old != getHorizontalAlignment()) {
            layoutSeparator();
        }
        firePropertyChange("horizontalAlignment", old, getHorizontalAlignment());
    }

    /**
     * Returns the alignment of the title contents along the X axis.
     *
     * @return   The value of the horizontalAlignment property, one of the
     *           following constants defined in <code>SwingConstants</code>:
     *           <code>LEFT</code>,
     *           <code>CENTER</code>,
     *           <code>RIGHT</code>,
     *           <code>LEADING</code> or
     *           <code>TRAILING</code>.
     *
     * @see #setHorizontalAlignment
     * @see SwingConstants
     */
    public int getHorizontalAlignment() {
        return label.getHorizontalAlignment();
    }

    /**
     * Sets the horizontal position of the title's text,
     * relative to the icon.
     *
     * @param position  One of the following constants
     *           defined in <code>SwingConstants</code>:
     *           <code>LEFT</code>,
     *           <code>CENTER</code>,
     *           <code>RIGHT</code>,
     *           <code>LEADING</code>, or
     *           <code>TRAILING</code> (the default).
     * @throws IllegalArgumentException if the position does not match one of
     *         the accepted inputs.
     */
    public void setHorizontalTextPosition(int position) {
        int old = getHorizontalTextPosition();
        label.setHorizontalTextPosition(position);
        firePropertyChange("horizontalTextPosition", old, getHorizontalTextPosition());
    }

    /**
     * Returns the horizontal position of the title's text,
     * relative to the icon.
     *
     * @return   One of the following constants
     *           defined in <code>SwingConstants</code>:
     *           <code>LEFT</code>,
     *           <code>CENTER</code>,
     *           <code>RIGHT</code>,
     *           <code>LEADING</code> or
     *           <code>TRAILING</code>.
     *
     * @see SwingConstants
     */
    public int getHorizontalTextPosition() {
        return label.getHorizontalTextPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentOrientation getComponentOrientation() {
        return label.getComponentOrientation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        ComponentOrientation old = label.getComponentOrientation();
        label.setComponentOrientation(o);
        firePropertyChange("componentOrientation", old, label.getComponentOrientation());
    }

    /**
     * Defines the icon this component will display.  If
     * the value of icon is null, nothing is displayed.
     * <p>
     * The default value of this property is null.
     *
     * @see #setHorizontalTextPosition
     * @see #getIcon
     */
    public void setIcon(Icon icon) {
        Icon old = getIcon();
        label.setIcon(icon);
        firePropertyChange("icon", old, getIcon());
    }

    /**
     * Returns the graphic image (glyph, icon) that the
     * <code>JXTitledSeparator</code> displays.
     *
     * @return an Icon
     * @see #setIcon
     */
    public Icon getIcon() {
        return label.getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setForeground(Color foreground) {
        if (label != null) {
            label.setForeground(foreground);
        }
        super.setForeground(foreground);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFont(Font font) {
        if (label != null) {
            label.setFont(font);
        }
        super.setFont(font);
    }
}
