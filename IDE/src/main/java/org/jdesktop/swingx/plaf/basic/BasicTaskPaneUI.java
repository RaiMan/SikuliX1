/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import static org.jdesktop.swingx.SwingXUtilities.isUIInstallable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.plaf.TaskPaneUI;

/**
 * Base implementation of the <code>JXTaskPane</code> UI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class BasicTaskPaneUI extends TaskPaneUI {

    private static FocusListener focusListener = new RepaintOnFocus();

    public static ComponentUI createUI(JComponent c) {
        return new BasicTaskPaneUI();
    }

    protected int titleHeight = 25;
    protected int roundHeight = 5;

    protected JXTaskPane group;

    protected boolean mouseOver;
    protected MouseInputListener mouseListener;

    protected PropertyChangeListener propertyListener;

    /**
     * {@inheritDoc}
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        group = (JXTaskPane) c;

        installDefaults();
        installListeners();
        installKeyboardActions();
    }

    /**
     * Installs default properties. Following properties are installed:
     * <ul>
     * <li>TaskPane.background</li>
     * <li>TaskPane.foreground</li>
     * <li>TaskPane.font</li>
     * <li>TaskPane.borderColor</li>
     * <li>TaskPane.titleForeground</li>
     * <li>TaskPane.titleBackgroundGradientStart</li>
     * <li>TaskPane.titleBackgroundGradientEnd</li>
     * <li>TaskPane.titleOver</li>
     * <li>TaskPane.specialTitleOver</li>
     * <li>TaskPane.specialTitleForeground</li>
     * <li>TaskPane.specialTitleBackground</li>
     * </ul>
     */
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(group, "TaskPane.background",
                "TaskPane.foreground", "TaskPane.font");
        LookAndFeel.installProperty(group, "opaque", false);

        if (isUIInstallable(group.getBorder())) {
            group.setBorder(createPaneBorder());
        }

        if (group.getContentPane() instanceof JComponent) {
            JComponent content = (JComponent) group.getContentPane();

            LookAndFeel.installColorsAndFont(content,
                    "TaskPane.background", "TaskPane.foreground", "TaskPane.font");

            if (isUIInstallable(content.getBorder())) {
                content.setBorder(createContentPaneBorder());
            }
        }
    }

    /**
     * Installs listeners for UI delegate.
     */
    protected void installListeners() {
        mouseListener = createMouseInputListener();
        group.addMouseMotionListener(mouseListener);
        group.addMouseListener(mouseListener);

        group.addFocusListener(focusListener);
        propertyListener = createPropertyListener();
        group.addPropertyChangeListener(propertyListener);
    }

    /**
     * Installs keyboard actions to allow task pane to react on hot keys.
     */
    protected void installKeyboardActions() {
        InputMap inputMap = (InputMap) UIManager.get("TaskPane.focusInputMap");
        if (inputMap != null) {
            SwingUtilities.replaceUIInputMap(group, JComponent.WHEN_FOCUSED,
                    inputMap);
        }

        ActionMap map = getActionMap();
        if (map != null) {
            SwingUtilities.replaceUIActionMap(group, map);
        }
    }

    ActionMap getActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put("toggleCollapsed", new ToggleCollapsedAction());
        return map;
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallListeners();
        super.uninstallUI(c);
    }

    /**
     * Uninstalls previously installed listeners to free component for garbage collection.
     */
    protected void uninstallListeners() {
        group.removeMouseListener(mouseListener);
        group.removeMouseMotionListener(mouseListener);
        group.removeFocusListener(focusListener);
        group.removePropertyChangeListener(propertyListener);
    }

    /**
     * Creates new toggle listener.
     * @return MouseInputListener reacting on toggle events of task pane.
     */
    protected MouseInputListener createMouseInputListener() {
        return new ToggleListener();
    }

    /**
     * Creates property change listener for task pane.
     * @return Property change listener reacting on changes to the task pane.
     */
    protected PropertyChangeListener createPropertyListener() {
        return new ChangeListener();
    }

    /**
     * Evaluates whenever given mouse even have occurred within borders of task pane.
     * @param event Evaluated event.
     * @return True if event occurred within task pane area, false otherwise.
     */
    protected boolean isInBorder(MouseEvent event) {
        return event.getY() < getTitleHeight(event.getComponent());
    }

        /**
         * Gets current title height. Default value is 25 if not specified otherwise. Method checks
         * provided component for user set font (!instanceof FontUIResource), if font is set, height
         * will be calculated from font metrics instead of using internal preset height.
         * @return Current title height.
         */
        protected int getTitleHeight(Component c) {
            if (c instanceof JXTaskPane) {
                JXTaskPane taskPane = (JXTaskPane) c;
                Font font = taskPane.getFont();
                int height = titleHeight;

                if (font != null && !(font instanceof FontUIResource)) {
                    height = Math.max(height, taskPane.getFontMetrics(font).getHeight());
                }

                Icon icon = taskPane.getIcon();

                if (icon != null) {
                    height = Math.max(height, icon.getIconHeight() + 4);
                }

                return height;
            }

            return titleHeight;
        }

    /**
     * Creates new border for task pane.
     * @return Fresh border on every call.
     */
    protected Border createPaneBorder() {
        return new PaneBorder();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Component component = group.getComponent(0);
        if (!(component instanceof JXCollapsiblePane)) {
            // something wrong in this JXTaskPane
            return super.getPreferredSize(c);
        }

        JXCollapsiblePane collapsible = (JXCollapsiblePane) component;
        Dimension dim = collapsible.getPreferredSize();

        Border groupBorder = group.getBorder();
        if (groupBorder instanceof PaneBorder) {
            ((PaneBorder) groupBorder).label.setDisplayedMnemonic(group
                    .getMnemonic());
            Dimension border = ((PaneBorder) groupBorder)
                    .getPreferredSize(group);
            dim.width = Math.max(dim.width, border.width);
            dim.height += border.height;
        } else {
            dim.height += getTitleHeight(c);
        }

        return dim;
    }

    /**
     * Creates content pane border.
     * @return Fresh content pane border initialized with current value of TaskPane.borderColor
     * on every call.
     */
    protected Border createContentPaneBorder() {
        Color borderColor = UIManager.getColor("TaskPane.borderColor");
        return new CompoundBorder(new ContentPaneBorder(borderColor),
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    @Override
    public Component createAction(Action action) {
        JXHyperlink link = new JXHyperlink(action) {
            @Override
            public void updateUI() {
                super.updateUI();
                // ensure the ui of this link is correctly update on l&f changes
                configure(this);
            }
        };
        configure(link);
        return link;
    }

    /**
     * Configures internally used hyperlink on new action creation and on every call to
     * <code>updateUI()</code>.
     * @param link Configured hyperlink.
     */
    protected void configure(JXHyperlink link) {
        link.setOpaque(false);
        link.setBorderPainted(false);
        link.setFocusPainted(true);
        link.setForeground(UIManager.getColor("TaskPane.titleForeground"));
    }

    /**
     * Ensures expanded group is visible. Issues delayed request for scrolling to visible.
     */
    protected void ensureVisible() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                group.scrollRectToVisible(new Rectangle(group.getWidth(), group
                        .getHeight()));
            }
        });
    }

    /**
     * Focus listener responsible for repainting of the taskpane on focus change.
     */
    static class RepaintOnFocus implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            e.getComponent().repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            e.getComponent().repaint();
        }
    }

    /**
     * Change listener responsible for change handling.
     */
    class ChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // if group is expanded but not animated
            // or if animated has reached expanded state
            // scroll to visible if scrollOnExpand is enabled
            if (("collapsed".equals(evt.getPropertyName())
                    && Boolean.TRUE.equals(evt.getNewValue()) && !group
                    .isAnimated())) {
                if (group.isScrollOnExpand()) {
                    ensureVisible();
                }
            } else if (JXTaskPane.ICON_CHANGED_KEY
                    .equals(evt.getPropertyName())
                    || JXTaskPane.TITLE_CHANGED_KEY.equals(evt
                            .getPropertyName())
                    || JXTaskPane.SPECIAL_CHANGED_KEY.equals(evt
                            .getPropertyName())) {
                // icon, title, special must lead to a repaint()
                group.repaint();
            } else if ("mnemonic".equals(evt.getPropertyName())) {
                SwingXUtilities.updateMnemonicBinding(group, "toggleCollapsed");

                Border b = group.getBorder();

                if (b instanceof PaneBorder) {
                    int key = (Integer) evt.getNewValue();
                    ((PaneBorder) b).label.setDisplayedMnemonic(key);
                }
            }
        }
    }

    /**
     * Mouse listener responsible for handling of toggle events.
     */
    class ToggleListener extends MouseInputAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (isInBorder(e)) {
                e.getComponent().setCursor(
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                mouseOver = false;
                                group.repaint(0, 0, group.getWidth(), getTitleHeight(group));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            e.getComponent().setCursor(null);
            mouseOver = false;
                        group.repaint(0, 0, group.getWidth(), getTitleHeight(group));
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (isInBorder(e)) {
                e.getComponent().setCursor(
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                mouseOver = true;
            } else {
                e.getComponent().setCursor(null);
                mouseOver = false;
            }

                        group.repaint(0, 0, group.getWidth(), getTitleHeight(group));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && isInBorder(e)) {
                group.setCollapsed(!group.isCollapsed());
            }
        }
    }

    /**
     * Toggle expanded action.
     */
    class ToggleCollapsedAction extends AbstractAction {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 5676859881615358815L;

        public ToggleCollapsedAction() {
            super("toggleCollapsed");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            group.setCollapsed(!group.isCollapsed());
        }

        @Override
        public boolean isEnabled() {
            return group.isVisible();
        }
    }

    /**
     * Toggle icon.
     */
    protected static class ChevronIcon implements Icon {
        boolean up = true;

        public ChevronIcon(boolean up) {
            this.up = up;
        }

        @Override
        public int getIconHeight() {
            return 3;
        }

        @Override
        public int getIconWidth() {
            return 6;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (up) {
                g.drawLine(x + 3, y, x, y + 3);
                g.drawLine(x + 3, y, x + 6, y + 3);
            } else {
                g.drawLine(x, y, x + 3, y + 3);
                g.drawLine(x + 3, y + 3, x + 6, y);
            }
        }
    }

    /**
     * The border around the content pane
     */
    protected static class ContentPaneBorder implements Border, UIResource {
        Color color;

        public ContentPaneBorder(Color color) {
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 1, 1, 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {
            g.setColor(color);
            g.drawLine(x, y, x, y + height - 1);
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
            g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
        }
    }

    /**
     * The border of the taskpane group paints the "text", the "icon", the
     * "expanded" status and the "special" type.
     *
     */
    protected class PaneBorder implements Border, UIResource {

        protected Color borderColor;
        protected Color titleForeground;
        protected Color specialTitleBackground;
        protected Color specialTitleForeground;
        protected Color titleBackgroundGradientStart;
        protected Color titleBackgroundGradientEnd;

        protected Color titleOver;
        protected Color specialTitleOver;

        protected JLabel label;

        /**
         * Creates new instance of individual pane border.
         */
        public PaneBorder() {
            borderColor = UIManager.getColor("TaskPane.borderColor");

            titleForeground = UIManager.getColor("TaskPane.titleForeground");

            specialTitleBackground = UIManager
                    .getColor("TaskPane.specialTitleBackground");
            specialTitleForeground = UIManager
                    .getColor("TaskPane.specialTitleForeground");

            titleBackgroundGradientStart = UIManager
                    .getColor("TaskPane.titleBackgroundGradientStart");
            titleBackgroundGradientEnd = UIManager
                    .getColor("TaskPane.titleBackgroundGradientEnd");

            titleOver = UIManager.getColor("TaskPane.titleOver");
            if (titleOver == null) {
                titleOver = specialTitleBackground.brighter();
            }
            specialTitleOver = UIManager.getColor("TaskPane.specialTitleOver");
            if (specialTitleOver == null) {
                specialTitleOver = specialTitleBackground.brighter();
            }

            label = new JLabel();
            label.setOpaque(false);
            label.setIconTextGap(8);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(getTitleHeight(c), 0, 0, 0);
        }

        /**
         * Overwritten to always return <code>true</code> to speed up
         * painting. Don't use transparent borders unless providing UI delegate
         * that provides proper return value when calling this method.
         *
         * @see javax.swing.border.Border#isBorderOpaque()
         */
        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        /**
         * Calculates the preferred border size, its size so all its content
         * fits.
         *
         * @param group
         *            Selected group.
         */
        public Dimension getPreferredSize(JXTaskPane group) {
            // calculate the title width so it is fully visible
            // it starts with the title width
            configureLabel(group);
            Dimension dim = label.getPreferredSize();
            // add the title left offset
            dim.width += 3;
            // add the controls width
            dim.width += getTitleHeight(group);
            // and some space between label and controls
            dim.width += 3;

            dim.height = getTitleHeight(group);
            return dim;
        }

        /**
         * Paints background of the title. This may differ based on properties
         * of the group.
         *
         * @param group
         *            Selected group.
         * @param g
         *            Target graphics.
         */
        protected void paintTitleBackground(JXTaskPane group, Graphics g) {
            if (group.isSpecial()) {
                g.setColor(specialTitleBackground);
            } else {
                g.setColor(titleBackgroundGradientStart);
            }
            g.fillRect(0, 0, group.getWidth(), getTitleHeight(group) - 1);
        }

        /**
         * Paints current group title.
         *
         * @param group
         *            Selected group.
         * @param g
         *            Target graphics.
         * @param textColor
         *            Title color.
         * @param x
         *            X coordinate of the top left corner.
         * @param y
         *            Y coordinate of the top left corner.
         * @param width
         *            Width of the box.
         * @param height
         *            Height of the box.
         */
        protected void paintTitle(JXTaskPane group, Graphics g,
                Color textColor, int x, int y, int width, int height) {
            configureLabel(group);
            label.setForeground(textColor);
            if (group.getFont() != null && ! (group.getFont() instanceof FontUIResource)) {
                label.setFont(group.getFont());
            }
            g.translate(x, y);
            label.setBounds(0, 0, width, height);
            label.paint(g);
            g.translate(-x, -y);
        }

        /**
         * Configures label for the group using its title, font, icon and
         * orientation.
         *
         * @param group
         *            Selected group.
         */
        protected void configureLabel(JXTaskPane group) {
            label.applyComponentOrientation(group.getComponentOrientation());
            label.setFont(group.getFont());
            label.setText(group.getTitle());
            label.setIcon(group.getIcon() == null ? new EmptyIcon() : group
                    .getIcon());
        }

        /**
         * Paints expanded controls. Default implementation does nothing.
         *
         * @param group
         *            Expanded group.
         * @param g
         *            Target graphics.
         * @param x
         *            X coordinate of the top left corner.
         * @param y
         *            Y coordinate of the top left corner.
         * @param width
         *            Width of the box.
         * @param height
         *            Height of the box.
         */
        protected void paintExpandedControls(JXTaskPane group, Graphics g,
                int x, int y, int width, int height) {
        }

        /**
         * Gets current paint color.
         *
         * @param group
         *            Selected group.
         * @return Color to be used for painting provided group.
         */
        protected Color getPaintColor(JXTaskPane group) {
            Color paintColor;
            if (isMouseOverBorder()) {
                if (mouseOver) {
                    if (group.isSpecial()) {
                        paintColor = specialTitleOver;
                    } else {
                        paintColor = titleOver;
                    }
                } else {
                    if (group.isSpecial()) {
                        paintColor = specialTitleForeground;
                    } else {
                        paintColor = group.getForeground() == null || group.getForeground() instanceof ColorUIResource ? titleForeground : group.getForeground();
                    }
                }
            } else {
                if (group.isSpecial()) {
                    paintColor = specialTitleForeground;
                } else {
                    paintColor = group.getForeground() == null || group.getForeground() instanceof ColorUIResource ? titleForeground : group.getForeground();
                }
            }
            return paintColor;
        }

        /*
         * @see javax.swing.border.Border#paintBorder(java.awt.Component,
         *      java.awt.Graphics, int, int, int, int)
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {

            JXTaskPane group = (JXTaskPane) c;

            // calculate position of title and toggle controls
            int controlWidth = getTitleHeight(group) - 2 * getRoundHeight();
            int controlX = group.getWidth() - getTitleHeight(group);
            int controlY = getRoundHeight() - 1;
            int titleX = 3;
            int titleY = 0;
            int titleWidth = group.getWidth() - getTitleHeight(group) - 3;
            int titleHeight = getTitleHeight(group);

            if (!group.getComponentOrientation().isLeftToRight()) {
                controlX = group.getWidth() - controlX - controlWidth;
                titleX = group.getWidth() - titleX - titleWidth;
            }

            // paint the title background
            paintTitleBackground(group, g);

            // paint the the toggles
            paintExpandedControls(group, g, controlX, controlY, controlWidth,
                    controlWidth);

            // paint the title text and icon
            Color paintColor = getPaintColor(group);

            // focus painted same color as text
            if (group.hasFocus()) {
                paintFocus(g, paintColor, 3, 3, width - 6, getTitleHeight(group) - 6);
            }

            paintTitle(group, g, paintColor, titleX, titleY, titleWidth,
                    titleHeight);
        }

        /**
         * Paints oval 'border' area around the control itself.
         *
         * @param group
         *            Expanded group.
         * @param g
         *            Target graphics.
         * @param x
         *            X coordinate of the top left corner.
         * @param y
         *            Y coordinate of the top left corner.
         * @param width
         *            Width of the box.
         * @param height
         *            Height of the box.
         */
        protected void paintRectAroundControls(JXTaskPane group, Graphics g,
                int x, int y, int width, int height, Color highColor,
                Color lowColor) {
            if (mouseOver) {
                int x2 = x + width;
                int y2 = y + height;
                g.setColor(highColor);
                g.drawLine(x, y, x2, y);
                g.drawLine(x, y, x, y2);
                g.setColor(lowColor);
                g.drawLine(x2, y, x2, y2);
                g.drawLine(x, y2, x2, y2);
            }
        }

        /**
         * Paints oval 'border' area around the control itself.
         *
         * @param group
         *            Expanded group.
         * @param g
         *            Target graphics.
         * @param x
         *            X coordinate of the top left corner.
         * @param y
         *            Y coordinate of the top left corner.
         * @param width
         *            Width of the box.
         * @param height
         *            Height of the box.
         */
        protected void paintOvalAroundControls(JXTaskPane group, Graphics g,
                int x, int y, int width, int height) {
            if (group.isSpecial()) {
                g.setColor(specialTitleBackground.brighter());
                g.drawOval(x, y, width, height);
            } else {
                g.setColor(titleBackgroundGradientStart);
                g.fillOval(x, y, width, height);

                g.setColor(titleBackgroundGradientEnd.darker());
                g.drawOval(x, y, width, width);
            }
        }

        /**
         * Paints controls for the group.
         *
         * @param group
         *            Expanded group.
         * @param g
         *            Target graphics.
         * @param x
         *            X coordinate of the top left corner.
         * @param y
         *            Y coordinate of the top left corner.
         * @param width
         *            Width of the box.
         * @param height
         *            Height of the box.
         */
        protected void paintChevronControls(JXTaskPane group, Graphics g,
                int x, int y, int width, int height) {
            ChevronIcon chevron;
            if (group.isCollapsed()) {
                chevron = new ChevronIcon(false);
            } else {
                chevron = new ChevronIcon(true);
            }
            int chevronX = x + width / 2 - chevron.getIconWidth() / 2;
            int chevronY = y + (height / 2 - chevron.getIconHeight());
            chevron.paintIcon(group, g, chevronX, chevronY);
            chevron.paintIcon(group, g, chevronX, chevronY
                    + chevron.getIconHeight() + 1);
        }

        /**
         * Paints focused group.
         *
         * @param g
         *            Target graphics.
         * @param paintColor
         *            Focused group color.
         * @param x
         *            X coordinate of the top left corner.
         * @param y
         *            Y coordinate of the top left corner.
         * @param width
         *            Width of the box.
         * @param height
         *            Height of the box.
         */
        protected void paintFocus(Graphics g, Color paintColor, int x, int y,
                int width, int height) {
            g.setColor(paintColor);
            BasicGraphicsUtils.drawDashedRect(g, x, y, width, height);
        }

        /**
         * Default implementation returns false.
         *
         * @return true if this border wants to display things differently when
         *         the mouse is over it
         */
        protected boolean isMouseOverBorder() {
            return false;
        }
    }

    /**
     * Gets size of arc used to round corners.
     *
     * @return size of arc used to round corners of the panel.
     */
    protected int getRoundHeight() {
        return roundHeight;
    }

}
