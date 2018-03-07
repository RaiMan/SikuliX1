/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXStatusBar.Constraint;
import org.jdesktop.swingx.plaf.StatusBarUI;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 *
 * @author rbair
 * @author Karl Schaefer
 */
public class BasicStatusBarUI extends StatusBarUI {
    private class Handler implements MouseListener, MouseMotionListener, PropertyChangeListener {
        private Window window = SwingUtilities.getWindowAncestor(statusBar);
        private int handleBoundary = getHandleBoundary();
        private boolean validPress = false;
        private Point startingPoint;

        private int getHandleBoundary() {
            Border border = statusBar.getBorder();

            if (border == null || !statusBar.isResizeHandleEnabled()) {
                return 0;
            }

            if (statusBar.getComponentOrientation().isLeftToRight()) {
                return border.getBorderInsets(statusBar).right;
            } else {
                return border.getBorderInsets(statusBar).left;
            }
        }

        private boolean isHandleAreaPoint(Point point) {
            if (window == null || window.isMaximumSizeSet()) {
                return false;
            }

            if (statusBar.getComponentOrientation().isLeftToRight()) {
                return point.x >= statusBar.getWidth() - handleBoundary;
            } else {
                return point.x <= handleBoundary;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            //does nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            if (isHandleAreaPoint(e.getPoint())) {
                if (statusBar.getComponentOrientation().isLeftToRight()) {
                    window.setCursor(Cursor.getPredefinedCursor(
                            Cursor.SE_RESIZE_CURSOR));
                } else {
                    window.setCursor(Cursor.getPredefinedCursor(
                            Cursor.SW_RESIZE_CURSOR));
                }
            } else {
                window.setCursor(null);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseExited(MouseEvent e) {
            if (!validPress) {
                window.setCursor(null);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e) {
            validPress = SwingUtilities.isLeftMouseButton(e) && isHandleAreaPoint(e.getPoint());
            startingPoint = e.getPoint();
            SwingUtilities.convertPointToScreen(startingPoint, statusBar);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            validPress = !SwingUtilities.isLeftMouseButton(e);
            window.validate();
            window.setCursor(null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (validPress) {
                Rectangle wb = window.getBounds();
                Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, statusBar);

                wb.height += (p.y - startingPoint.y);
                if (statusBar.getComponentOrientation().isLeftToRight()) {
                    wb.width += (p.x - startingPoint.x);
                } else {
                    wb.x += (p.x - startingPoint.x);
                    wb.width += (startingPoint.x - p.x);
                }

                window.setBounds(wb);
                window.validate();
                startingPoint = p;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            if (isHandleAreaPoint(e.getPoint())) {
                if (statusBar.getComponentOrientation().isLeftToRight()) {
                    window.setCursor(Cursor.getPredefinedCursor(
                            Cursor.SE_RESIZE_CURSOR));
                } else {
                    window.setCursor(Cursor.getPredefinedCursor(
                            Cursor.SW_RESIZE_CURSOR));
                }
            } else {
                window.setCursor(null);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("ancestor".equals(evt.getPropertyName())) {
                window = SwingUtilities.getWindowAncestor(statusBar);

                boolean useResizeHandle = statusBar.getParent() != null
                        && statusBar.getRootPane() != null
                        && (statusBar.getParent() == statusBar.getRootPane()
                        || statusBar.getParent() == statusBar.getRootPane().getContentPane());
                statusBar.setResizeHandleEnabled(useResizeHandle);
            } else if ("border".equals(evt.getPropertyName())) {
                handleBoundary = getHandleBoundary();
            } else if ("componentOrientation".equals(evt.getPropertyName())) {
                handleBoundary = getHandleBoundary();
            } else if ("resizeHandleEnabled".equals(evt.getPropertyName())) {
                //TODO disable handle display
                handleBoundary = getHandleBoundary();
            }
        }
    }

    public static final String AUTO_ADD_SEPARATOR = new StringBuffer("auto-add-separator").toString();
    /**
     * Used to help reduce the amount of trash being generated
     */
    private static Insets TEMP_INSETS;
    /**
     * The one and only JXStatusBar for this UI delegate
     */
    protected JXStatusBar statusBar;

    protected MouseListener mouseListener;

    protected MouseMotionListener mouseMotionListener;

    protected PropertyChangeListener propertyChangeListener;

    private Handler handler;

    /** Creates a new instance of BasicStatusBarUI */
    public BasicStatusBarUI() {
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
        return new BasicStatusBarUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installUI(JComponent c) {
        assert c instanceof JXStatusBar;
        statusBar = (JXStatusBar)c;

        installDefaults(statusBar);
        installListeners(statusBar);

        // only set the layout manager if the layout manager of the component is
        // null or a UIResource. Do not replace custom layout managers.
        LayoutManager m = statusBar.getLayout();
        if (m == null || m instanceof UIResource) {
            statusBar.setLayout(createLayout());
        }
    }

    protected void installDefaults(JXStatusBar sb) {
        //only set the border if it is an instanceof UIResource
        //In other words, only replace the border if it has not been
        //set by the developer. UIResource is the flag we use to indicate whether
        //the value was set by the UIDelegate, or by the developer.
        Border b = statusBar.getBorder();
        if (b == null || b instanceof UIResource) {
            statusBar.setBorder(createBorder());
        }

        LookAndFeel.installProperty(sb, "opaque", Boolean.TRUE);
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }

        return handler;
    }

    /**
     * Creates a {@code MouseListener} which will be added to the
     * status bar. If this method returns null then it will not
     * be added to the status bar.
     * <p>
     * Subclasses may override this method to return instances of their own
     * MouseEvent handlers.
     *
     * @return an instance of a {@code MouseListener} or null
     */
    protected MouseListener createMouseListener() {
        return getHandler();
    }

    /**
     * Creates a {@code MouseMotionListener} which will be added to the
     * status bar. If this method returns null then it will not
     * be added to the status bar.
     * <p>
     * Subclasses may override this method to return instances of their own
     * MouseEvent handlers.
     *
     * @return an instance of a {@code MouseMotionListener} or null
     */
    protected MouseMotionListener createMouseMotionListener() {
        return getHandler();
    }

    /**
     * Creates a {@code PropertyChangeListener} which will be added to the
     * status bar. If this method returns null then it will not
     * be added to the status bar.
     * <p>
     * Subclasses may override this method to return instances of their own
     * PropertyChangeEvent handlers.
     *
     * @return an instance of a {@code PropertyChangeListener} or null
     */
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    /**
     * Create and install the listeners for the status bar.
     * This method is called when the UI is installed.
     */
    protected void installListeners(JXStatusBar sb) {
        if ((mouseListener = createMouseListener()) != null) {
            statusBar.addMouseListener(mouseListener);
        }

        if ((mouseMotionListener = createMouseMotionListener()) != null) {
            statusBar.addMouseMotionListener(mouseMotionListener);
        }

        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            statusBar.addPropertyChangeListener(propertyChangeListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent c) {
        assert c instanceof JXStatusBar;

        uninstallDefaults(statusBar);
        uninstallListeners(statusBar);

        if (statusBar.getLayout() instanceof UIResource) {
            statusBar.setLayout(null);
        }
    }

    protected void uninstallDefaults(JXStatusBar sb) {
        if (sb.getBorder() instanceof UIResource) {
            sb.setBorder(null);
        }
    }

    /**
     * Remove the installed listeners from the status bar.
     * The number and types of listeners removed in this method should be
     * the same that were added in <code>installListeners</code>
     */
    protected void uninstallListeners(JXStatusBar sb) {
        if (mouseListener != null) {
            statusBar.removeMouseListener(mouseListener);
        }

        if (mouseMotionListener != null) {
            statusBar.removeMouseMotionListener(mouseMotionListener);
        }

        if (propertyChangeListener != null) {
            statusBar.removePropertyChangeListener(propertyChangeListener);
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        //paint the background if opaque
        if (statusBar.isOpaque()) {
            Graphics2D g2 = (Graphics2D)g;
            paintBackground(g2, statusBar);
        }

        if (includeSeparators()) {
            //now paint the separators
            TEMP_INSETS = getSeparatorInsets(TEMP_INSETS);
            for (int i=0; i<statusBar.getComponentCount()-1; i++) {
                Component comp = statusBar.getComponent(i);
                int x = comp.getX() + comp.getWidth() + TEMP_INSETS.left;
                int y = TEMP_INSETS.top;
                int w = getSeparatorWidth() - TEMP_INSETS.left - TEMP_INSETS.right;
                int h = c.getHeight() - TEMP_INSETS.top - TEMP_INSETS.bottom;

                paintSeparator((Graphics2D)g, statusBar, x, y, w, h);
            }
        }
    }

    //----------------------------------------------------- Extension Points
    protected void paintBackground(Graphics2D g, JXStatusBar bar) {
        if (bar.isOpaque()) {
            g.setColor(bar.getBackground());
            g.fillRect(0, 0, bar.getWidth(), bar.getHeight());
        }
    }

    protected void paintSeparator(Graphics2D g, JXStatusBar bar, int x, int y, int w, int h) {
        Color fg = UIManagerExt.getSafeColor("Separator.foreground", Color.BLACK);
        Color bg = UIManagerExt.getSafeColor("Separator.background", Color.WHITE);

        x += w / 2;
        g.setColor(fg);
        g.drawLine(x, y, x, h);

        g.setColor(bg);
        g.drawLine(x+1, y, x+1, h);
    }

    protected Insets getSeparatorInsets(Insets insets) {
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }

        insets.top = 4;
        insets.left = 4;
        insets.bottom = 2;
        insets.right = 4;

        return insets;
    }

    protected int getSeparatorWidth() {
        return 10;
    }

    protected boolean includeSeparators() {
        Boolean b = (Boolean)statusBar.getClientProperty(AUTO_ADD_SEPARATOR);
        return b == null || b;
    }

    protected BorderUIResource createBorder() {
        return new BorderUIResource(BorderFactory.createEmptyBorder(4, 5, 4, 22));
    }

    protected LayoutManager createLayout() {
        //This is in the UI delegate because the layout
        //manager takes into account spacing for the separators between components
        return new LayoutManager2() {
            private Map<Component,Constraint> constraints = new HashMap<Component,Constraint>();

            @Override
            public void addLayoutComponent(String name, Component comp) {addLayoutComponent(comp, null);}
            @Override
            public void removeLayoutComponent(Component comp) {constraints.remove(comp);}
            @Override
            public Dimension minimumLayoutSize(Container parent) {return preferredLayoutSize(parent);}
            @Override
            public Dimension maximumLayoutSize(Container target) {return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);}
            @Override
            public float getLayoutAlignmentX(Container target) {return .5f;}
            @Override
            public float getLayoutAlignmentY(Container target) {return .5f;}
            @Override
            public void invalidateLayout(Container target) {}

            @Override
            public void addLayoutComponent(Component comp, Object constraint) {
                //we accept an Insets, a ResizeBehavior, or a Constraint.
                if (constraint instanceof Insets) {
                    constraint = new Constraint((Insets)constraint);
                } else if (constraint instanceof Constraint.ResizeBehavior) {
                    constraint = new Constraint((Constraint.ResizeBehavior)constraint);
                }

                constraints.put(comp, (Constraint)constraint);
            }

            @Override
            public Dimension preferredLayoutSize(Container parent) {
                Dimension prefSize = new Dimension();
                int count = 0;
                for (Component comp : constraints.keySet()) {
                    Constraint c = constraints.get(comp);
                    Dimension d = comp.getPreferredSize();
                    int prefWidth = 0;
                    if (c != null) {
                        Insets i = c.getInsets();
                        d.width += i.left + i.right;
                        d.height += i.top + i.bottom;
                        prefWidth = c.getFixedWidth();
                    }
                    prefSize.height = Math.max(prefSize.height, d.height);
                    prefSize.width += Math.max(d.width, prefWidth);

                    //If this is not the last component, add extra space between each
                    //component (for the separator).
                    count++;
                    if (includeSeparators() && constraints.size() < count) {
                        prefSize.width += getSeparatorWidth();
                    }
                }

                Insets insets = parent.getInsets();
                prefSize.height += insets.top + insets.bottom;
                prefSize.width += insets.left + insets.right;
                return prefSize;
            }

            @Override
            public void layoutContainer(Container parent) {
                /*
                 * Layout algorithm:
                 *      If the parent width is less than the sum of the preferred
                 *      widths of the components (including separators), where
                 *      preferred width means either the component preferred width +
                 *      constraint insets, or fixed width + constraint insets, then
                 *      simply layout the container from left to right and let the
                 *      right hand components flow off the parent.
                 *
                 *      Otherwise, lay out each component according to its preferred
                 *      width except for components with a FILL constraint. For these,
                 *      resize them evenly for each FILL constraint.
                 */

                //the insets of the parent component.
                Insets parentInsets = parent.getInsets();
                //the available width for putting components.
                int availableWidth = parent.getWidth() - parentInsets.left - parentInsets.right;
                if (includeSeparators()) {
                    //remove from availableWidth the amount of space the separators will take
                    availableWidth -= (parent.getComponentCount() - 1) * getSeparatorWidth();
                }

                //the preferred widths of all of the components -- where preferred
                //width mean the preferred width after calculating fixed widths and
                //constraint insets
                int[] preferredWidths = new int[parent.getComponentCount()];
                int sumPreferredWidths = 0;
                for (int i=0; i<preferredWidths.length; i++) {
                    preferredWidths[i] = getPreferredWidth(parent.getComponent(i));
                    sumPreferredWidths += preferredWidths[i];
                }

                //if the availableWidth is greater than the sum of preferred
                //sizes, then adjust the preferred width of each component that
                //has a FILL constraint, to evenly use up the extra space.
                if (availableWidth > sumPreferredWidths) {
                    //the number of components with a fill constraint
                    int numFilledComponents = 0;
                    for (Component comp : parent.getComponents()) {
                        Constraint c = constraints.get(comp);
                        if (c != null && c.getResizeBehavior() == Constraint.ResizeBehavior.FILL) {
                            numFilledComponents++;
                        }
                    }

                    if (numFilledComponents > 0) {
                        //calculate the share of free space each FILL component will take
                        availableWidth -= sumPreferredWidths;
                        double weight = 1.0 / (double)numFilledComponents;
                        int share = (int)(availableWidth * weight);
                        int remaining = numFilledComponents;
                        for (int i=0; i<parent.getComponentCount(); i++) {
                            Component comp = parent.getComponent(i);
                            Constraint c = constraints.get(comp);
                            if (c != null && c.getResizeBehavior() == Constraint.ResizeBehavior.FILL) {
                                if (remaining > 1) {
                                    preferredWidths[i] += share;
                                    availableWidth -= share;
                                } else {
                                    preferredWidths[i] += availableWidth;
                                }
                                remaining--;
                            }
                        }
                    }
                }

                //now lay out the components
                int nextX = parentInsets.left;
                int height = parent.getHeight() - parentInsets.top - parentInsets.bottom;
                for (int i=0; i<parent.getComponentCount(); i++) {
                    Component comp = parent.getComponent(i);
                    Constraint c = constraints.get(comp);
                    Insets insets = c == null ? new Insets(0,0,0,0) : c.getInsets();
                    int width = preferredWidths[i] - (insets.left + insets.right);
                    int x = nextX + insets.left;
                    int y = parentInsets.top + insets.top;
                    comp.setSize(width, height);
                    comp.setLocation(x, y);
                    nextX = x + width + insets.right;
                    //If this is not the last component, add extra space
                    //for the separator
                    if (includeSeparators() && i < parent.getComponentCount() - 1) {
                        nextX += getSeparatorWidth();
                    }
                }
            }

            /**
             * @return the "preferred" width, where that means either
             *         comp.getPreferredSize().width + constraintInsets, or
             *         constraint.fixedWidth + constraintInsets.
             */
            private int getPreferredWidth(Component comp) {
                Constraint c = constraints.get(comp);
                if (c == null) {
                    return comp.getPreferredSize().width;
                } else {
                    Insets insets = c.getInsets();
                    assert insets != null;
                    if (c.getFixedWidth() <= 0) {
                        return comp.getPreferredSize().width + insets.left + insets.right;
                    } else {
                        return c.getFixedWidth() + insets.left + insets.right;
                    }
                }
            }

        };
    }
}
