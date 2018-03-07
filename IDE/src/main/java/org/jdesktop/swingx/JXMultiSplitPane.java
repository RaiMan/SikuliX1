/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * <p>
 * All properties in this class are bound: when a properties value
 * is changed, all PropertyChangeListeners are fired.
 *
 * @author Hans Muller
 * @author Luan O'Carroll
 */
@JavaBean
public class JXMultiSplitPane extends JPanel implements BackgroundPaintable {
    private AccessibleContext accessibleContext = null;
    private boolean continuousLayout = true;
    private DividerPainter dividerPainter = new DefaultDividerPainter();
    private Painter backgroundPainter;
    private boolean paintBorderInsets;

    /**
     * Creates a MultiSplitPane with it's LayoutManager set to
     * to an empty MultiSplitLayout.
     */
    public JXMultiSplitPane() {
        this(new MultiSplitLayout());
    }

    /**
     * Creates a MultiSplitPane.
     * @param layout the new split pane's layout
     */
    public JXMultiSplitPane( MultiSplitLayout layout ) {
        super(layout);
        InputHandler inputHandler = new InputHandler();
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addKeyListener(inputHandler);
        setFocusable(true);
    }

    /**
     * A convenience method that returns the layout manager cast
     * to MutliSplitLayout.
     *
     * @return this MultiSplitPane's layout manager
     * @see java.awt.Container#getLayout
     * @see #setModel
     */
    public final MultiSplitLayout getMultiSplitLayout() {
        return (MultiSplitLayout)getLayout();
    }

    /**
     * A convenience method that sets the MultiSplitLayout model.
     * Equivalent to <code>getMultiSplitLayout.setModel(model)</code>
     *
     * @param model the root of the MultiSplitLayout model
     * @see #getMultiSplitLayout
     * @see MultiSplitLayout#setModel
     */
    public final void setModel(Node model) {
        getMultiSplitLayout().setModel(model);
    }

    /**
     * A convenience method that sets the MultiSplitLayout dividerSize
     * property. Equivalent to
     * <code>getMultiSplitLayout().setDividerSize(newDividerSize)</code>.
     *
     * @param dividerSize the value of the dividerSize property
     * @see #getMultiSplitLayout
     * @see MultiSplitLayout#setDividerSize
     */
    public final void setDividerSize(int dividerSize) {
        getMultiSplitLayout().setDividerSize(dividerSize);
    }

    /**
     * A convenience method that returns the MultiSplitLayout dividerSize
     * property. Equivalent to
     * <code>getMultiSplitLayout().getDividerSize()</code>.
     *
     * @see #getMultiSplitLayout
     * @see MultiSplitLayout#getDividerSize
     */
    public final int getDividerSize() {
        return getMultiSplitLayout().getDividerSize();
    }

    /**
     * Sets the value of the <code>continuousLayout</code> property.
     * If true, then the layout is revalidated continuously while
     * a divider is being moved.  The default value of this property
     * is true.
     *
     * @param continuousLayout value of the continuousLayout property
     * @see #isContinuousLayout
     */
    public void setContinuousLayout(boolean continuousLayout) {
        boolean oldContinuousLayout = isContinuousLayout();
        this.continuousLayout = continuousLayout;
        firePropertyChange("continuousLayout", oldContinuousLayout, isContinuousLayout());
    }

    /**
     * Returns true if dragging a divider only updates
     * the layout when the drag gesture ends (typically, when the
     * mouse button is released).
     *
     * @return the value of the <code>continuousLayout</code> property
     * @see #setContinuousLayout
     */
    public boolean isContinuousLayout() {
        return continuousLayout;
    }

    /**
     * Returns the Divider that's currently being moved, typically
     * because the user is dragging it, or null.
     *
     * @return the Divider that's being moved or null.
     */
    public Divider activeDivider() {
        return dragDivider;
    }

    /**
     * Draws a single Divider.  Typically used to specialize the
     * way the active Divider is painted.
     *
     * @see #getDividerPainter
     * @see #setDividerPainter
     */
    public static abstract class DividerPainter extends AbstractPainter<Divider> {
    }

    private class DefaultDividerPainter extends DividerPainter implements Serializable {
        @Override
        protected void doPaint(Graphics2D g, Divider divider, int width, int height) {
            if ((divider == activeDivider()) && !isContinuousLayout()) {
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
            }
        }
    }

    /**
     * The DividerPainter that's used to paint Dividers on this MultiSplitPane.
     * This property may be null.
     *
     * @return the value of the dividerPainter Property
     * @see #setDividerPainter
     */
    public DividerPainter getDividerPainter() {
    return dividerPainter;
    }

    /**
     * Sets the DividerPainter that's used to paint Dividers on this
     * MultiSplitPane.  The default DividerPainter only draws
     * the activeDivider (if there is one) and then, only if
     * continuousLayout is false.  The value of this property is
     * used by the paintChildren method: Dividers are painted after
     * the MultiSplitPane's children have been rendered so that
     * the activeDivider can appear "on top of" the children.
     *
     * @param dividerPainter the value of the dividerPainter property, can be null
     * @see #paintChildren
     * @see #activeDivider
     */
    public void setDividerPainter(DividerPainter dividerPainter) {
        DividerPainter old = getDividerPainter();
        this.dividerPainter = dividerPainter;
        firePropertyChange("dividerPainter", old, getDividerPainter());
    }

    /**
     * Calls the UI delegate's paint method, if the UI delegate
     * is non-<code>null</code>.  We pass the delegate a copy of the
     * <code>Graphics</code> object to protect the rest of the
     * paint code from irrevocable changes
     * (for example, <code>Graphics.translate</code>).
     * <p>
     * If you override this in a subclass you should not make permanent
     * changes to the passed in <code>Graphics</code>. For example, you
     * should not alter the clip <code>Rectangle</code> or modify the
     * transform. If you need to do these operations you may find it
     * easier to create a new <code>Graphics</code> from the passed in
     * <code>Graphics</code> and manipulate it. Further, if you do not
     * invoker super's implementation you must honor the opaque property,
     * that is
     * if this component is opaque, you must completely fill in the background
     * in a non-opaque color. If you do not honor the opaque property you
     * will likely see visual artifacts.
     * <p>
     * The passed in <code>Graphics</code> object might
     * have a transform other than the identify transform
     * installed on it.  In this case, you might get
     * unexpected results if you cumulatively apply
     * another transform.
     *
     * @param g the <code>Graphics</code> object to protect
     * @see #paint(Graphics)
     * @see javax.swing.plaf.ComponentUI
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        if (backgroundPainter == null) {
            super.paintComponent(g);
        } else {
            if (isOpaque()) {
                super.paintComponent(g);
            }

            Graphics2D g2 = (Graphics2D) g.create();

            try {
                SwingXUtilities.paintBackground(this, g2);
            } finally {
                g2.dispose();
            }

            getUI().paint(g, this);
        }
    }

    /**
     * Specifies a Painter to use to paint the background of this JXPanel.
     * If <code>p</code> is not null, then setOpaque(false) will be called
     * as a side effect. A component should not be opaque if painters are
     * being used, because Painters may paint transparent pixels or not
     * paint certain pixels, such as around the border insets.
     */
    @Override
    public void setBackgroundPainter(Painter p)
    {
        Painter old = getBackgroundPainter();
        this.backgroundPainter = p;

        if (p != null) {
            setOpaque(false);
        }

        firePropertyChange("backgroundPainter", old, getBackgroundPainter());
        repaint();
    }

    @Override
    public Painter getBackgroundPainter() {
        return backgroundPainter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPaintBorderInsets() {
        return paintBorderInsets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPaintBorderInsets(boolean paintBorderInsets) {
        boolean oldValue = isPaintBorderInsets();
        this.paintBorderInsets = paintBorderInsets;
        firePropertyChange("paintBorderInsets", oldValue, isPaintBorderInsets());
    }

    /**
     * Uses the DividerPainter (if any) to paint each Divider that
     * overlaps the clip Rectangle.  This is done after the call to
     * <code>super.paintChildren()</code> so that Dividers can be
     * rendered "on top of" the children.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected void paintChildren(Graphics g) {
      super.paintChildren(g);
      DividerPainter dp = getDividerPainter();
      Rectangle clipR = g.getClipBounds();
      if ((dp != null) && (clipR != null)) {
        MultiSplitLayout msl = getMultiSplitLayout();
        if ( msl.hasModel()) {
          for(Divider divider : msl.dividersThatOverlap(clipR)) {
            Rectangle bounds = divider.getBounds();
            Graphics cg = g.create( bounds.x, bounds.y, bounds.width, bounds.height );
            try {
              dp.paint((Graphics2D)cg, divider, bounds.width, bounds.height );
            } finally {
              cg.dispose();
            }
          }
        }
      }
    }

    private boolean dragUnderway = false;
    private MultiSplitLayout.Divider dragDivider = null;
    private Rectangle initialDividerBounds = null;
    private boolean oldFloatingDividers = true;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int dragMin = -1;
    private int dragMax = -1;

    private void startDrag(int mx, int my) {
        requestFocusInWindow();
        MultiSplitLayout msl = getMultiSplitLayout();
        MultiSplitLayout.Divider divider = msl.dividerAt(mx, my);
        if (divider != null) {
            MultiSplitLayout.Node prevNode = divider.previousSibling();
            MultiSplitLayout.Node nextNode = divider.nextSibling();
            if ((prevNode == null) || (nextNode == null)) {
            dragUnderway = false;
            }
            else {
            initialDividerBounds = divider.getBounds();
            dragOffsetX = mx - initialDividerBounds.x;
            dragOffsetY = my - initialDividerBounds.y;
            dragDivider  = divider;

            Rectangle prevNodeBounds = prevNode.getBounds();
            Rectangle nextNodeBounds = nextNode.getBounds();
            if (dragDivider.isVertical()) {
                dragMin = prevNodeBounds.x;
                dragMax = nextNodeBounds.x + nextNodeBounds.width;
                dragMax -= dragDivider.getBounds().width;
                if ( msl.getLayoutMode() == MultiSplitLayout.USER_MIN_SIZE_LAYOUT )
                  dragMax -= msl.getUserMinSize();
            }
            else {
                dragMin = prevNodeBounds.y;
                dragMax = nextNodeBounds.y + nextNodeBounds.height;
                dragMax -= dragDivider.getBounds().height;
                if ( msl.getLayoutMode() == MultiSplitLayout.USER_MIN_SIZE_LAYOUT )
                  dragMax -= msl.getUserMinSize();
            }

            if ( msl.getLayoutMode() == MultiSplitLayout.USER_MIN_SIZE_LAYOUT ) {
              dragMin = dragMin + msl.getUserMinSize();
            }
            else {
              if (dragDivider.isVertical()) {
                dragMin = Math.max( dragMin, dragMin + getMinNodeSize(msl,prevNode).width );
                dragMax = Math.min( dragMax, dragMax - getMinNodeSize(msl,nextNode).width );

                Dimension maxDim = getMaxNodeSize(msl,prevNode);
                if ( maxDim != null )
                  dragMax = Math.min( dragMax, prevNodeBounds.x + maxDim.width );
              }
              else {
                dragMin = Math.max( dragMin, dragMin + getMinNodeSize(msl,prevNode).height );
                dragMax = Math.min( dragMax, dragMax - getMinNodeSize(msl,nextNode).height );

                Dimension maxDim  = getMaxNodeSize(msl,prevNode);
                if ( maxDim != null )
                  dragMax = Math.min( dragMax, prevNodeBounds.y + maxDim.height );
              }
            }

            oldFloatingDividers = getMultiSplitLayout().getFloatingDividers();
            getMultiSplitLayout().setFloatingDividers(false);
            dragUnderway = true;
            }
        }
        else {
            dragUnderway = false;
        }
    }

    /**
     * Set the maximum node size. This method can be overridden to limit the
     * size of a node during a drag operation on a divider. When implementing
     * this method in a subclass the node instance should be checked, for
     * example:
     * <code>
     * class MyMultiSplitPane extends JXMultiSplitPane
     * {
     *   protected Dimension getMaxNodeSize( MultiSplitLayout msl, Node n )
     *   {
     *     if (( n instanceof Leaf ) && ((Leaf)n).getName().equals( "top" ))
     *       return msl.maximumNodeSize( n );
     *     return null;
     *   }
     * }
     * </code>
     * @param msl the MultiSplitLayout used by this pane
     * @param n the node being resized
     * @return the maximum size or null (by default) to ignore the maximum size.
     */
    protected Dimension getMaxNodeSize( MultiSplitLayout msl, Node n ) {
      return null;
    }

    /**
     * Set the minimum node size. This method can be overridden to limit the
     * size of a node during a drag operation on a divider.
     * @param msl the MultiSplitLayout used by this pane
     * @param n the node being resized
     * @return the maximum size or null (by default) to ignore the maximum size.
     */
    protected Dimension getMinNodeSize( MultiSplitLayout msl, Node n ) {
      return msl.minimumNodeSize(n);
    }

    private void repaintDragLimits() {
        Rectangle damageR = dragDivider.getBounds();
        if (dragDivider.isVertical()) {
            damageR.x = dragMin;
            damageR.width = dragMax - dragMin;
        }
        else {
            damageR.y = dragMin;
            damageR.height = dragMax - dragMin;
        }
        repaint(damageR);
    }

    private void updateDrag(int mx, int my) {
        if (!dragUnderway) {
            return;
        }
        Rectangle oldBounds = dragDivider.getBounds();
        Rectangle bounds = new Rectangle(oldBounds);
        if (dragDivider.isVertical()) {
            bounds.x = mx - dragOffsetX;
            bounds.x = Math.max(bounds.x, dragMin );
            bounds.x = Math.min(bounds.x, dragMax);
        }
        else {
            bounds.y = my - dragOffsetY;
            bounds.y = Math.max(bounds.y, dragMin );
            bounds.y = Math.min(bounds.y, dragMax);
        }
        dragDivider.setBounds(bounds);
        if (isContinuousLayout()) {
            revalidate();
            repaintDragLimits();
        }
        else {
            repaint(oldBounds.union(bounds));
        }
    }

    private void clearDragState() {
        dragDivider = null;
        initialDividerBounds = null;
        oldFloatingDividers = true;
        dragOffsetX = dragOffsetY = 0;
        dragMin = dragMax = -1;
        dragUnderway = false;
    }

    private void finishDrag(int x, int y) {
        if (dragUnderway) {
            clearDragState();
            if (!isContinuousLayout()) {
            revalidate();
            repaint();
            }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void cancelDrag() {
        if (dragUnderway) {
            dragDivider.setBounds(initialDividerBounds);
            getMultiSplitLayout().setFloatingDividers(oldFloatingDividers);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
            revalidate();
            clearDragState();
        }
    }

    private void updateCursor(int x, int y, boolean show) {
        if (dragUnderway) {
            return;
        }
        int cursorID = Cursor.DEFAULT_CURSOR;
        if (show) {
            MultiSplitLayout.Divider divider = getMultiSplitLayout().dividerAt(x, y);
            if (divider != null) {
            cursorID  = (divider.isVertical()) ?
                Cursor.E_RESIZE_CURSOR :
                Cursor.N_RESIZE_CURSOR;
            }
        }
        setCursor(Cursor.getPredefinedCursor(cursorID));
    }

    private class InputHandler extends MouseInputAdapter implements KeyListener {

        @Override
        public void mouseEntered(MouseEvent e) {
            updateCursor(e.getX(), e.getY(), true);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateCursor(e.getX(), e.getY(), true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateCursor(e.getX(), e.getY(), false);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            startDrag(e.getX(), e.getY());
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            finishDrag(e.getX(), e.getY());
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            updateDrag(e.getX(), e.getY());
        }
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancelDrag();
            }
        }
        @Override
        public void keyReleased(KeyEvent e) { }

        @Override
        public void keyTyped(KeyEvent e) { }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if( accessibleContext == null ) {
            accessibleContext = new AccessibleMultiSplitPane();
        }
        return accessibleContext;
    }

    protected class AccessibleMultiSplitPane extends AccessibleJPanel {
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SPLIT_PANE;
        }
    }
}
