/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;

import javax.swing.JComponent;

/**
 * Mouse/Motion/Listener which maps mouse coordinates to client coordinates
 * and stores these as client properties in the target JComponent. The exact
 * mapping process is left to subclasses. Typically, they will map to "cell"
 * coordinates. <p>
 *
 * Note: this class assumes that the target component is of type JComponent.<p>
 * Note: this implementation is stateful, it can't be shared across different
 * instances of a target component.<p>
 *
 *
 * @author Jeanette Winzenburg
 */
public abstract class RolloverProducer implements MouseListener, MouseMotionListener,
   ComponentListener {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(RolloverProducer.class
            .getName());

    /**
     * Key for client property mapped from mouse-triggered action.
     * Note that the actual mouse-event which results in setting the property
     * depends on the implementation of the concrete RolloverProducer.
     */
    public static final String CLICKED_KEY = "swingx.clicked";

    /** Key for client property mapped from rollover events */
    public static final String ROLLOVER_KEY = "swingx.rollover";

    //        public static final String PRESSED_KEY = "swingx.pressed";

    private boolean isDragging;

    /**
     * Installs all listeners, as required.
     *
     * @param component target to install required listeners on, must
     *   not be null.
     */
    public void install(JComponent component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addComponentListener(this);
    }

    /**
     * Removes all listeners.
     *
     * @param component target component to uninstall required listeners from,
     *   must not be null
     */
    public void release(JComponent component) {
        component.removeMouseListener(this);
        component.removeMouseMotionListener(this);
        component.removeComponentListener(this);
    }

    //----------------- mouseListener

    /**
     * Implemented to map to Rollover properties as needed. This implemenation calls
     * updateRollover with both ROLLOVER_KEY and CLICKED_KEY properties.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        Point oldCell = new Point(rollover);
        // JW: fix for #456-swingx - rollover not updated after end of dragging
        updateRollover(e, ROLLOVER_KEY, false);
        // Fix Issue 1387-swingx - no click on release-after-drag
        if (isClick(e, oldCell, isDragging)) {
            updateRollover(e, CLICKED_KEY, true);
        }
        isDragging = false;
    }

    /**
     * Returns a boolean indicating whether or not the given mouse event should
     * be interpreted as a click. This method is called from mouseReleased
     * after the cell coordiates were updated. While the ID of mouse event
     * is not formally enforced, it is assumed to be a MOUSE_RELEASED. Calling
     * for other types might or might not work as expected. <p>
     *
     * This implementation returns true if the current rollover point is the same
     * cell as the given oldRollover, that is ending a drag inside the same cell
     * triggers the action while ending a drag somewhere does not. <p>
     *
     * PENDING JW: open to more complex logic in case it clashes with existing code,
     * see Issue #1387.
     *
     * @param e the mouseEvent which triggered calling this, assumed to be
     *    a mouseReleased, must not be null
     * @param oldRollover the cell before the mouseEvent was mapped, must not be null
     * @param wasDragging true if the release happened
     * @return a boolean indicating whether or not the given mouseEvent should
     *   be interpreted as a click.
     */
    protected boolean isClick(MouseEvent e, Point oldRollover, boolean wasDragging) {
        return oldRollover.equals(rollover);
    }

    /**
     * Implemented to map to client property rollover and fire only if client
     * coordinate changed.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
//        LOG.info("" + e);
        isDragging = false;
        updateRollover(e, ROLLOVER_KEY, false);
    }

    /**
     * Implemented to remove client properties rollover and clicked. if the
     * source is a JComponent. Does nothing otherwise.
     */
    @Override
    public void mouseExited(MouseEvent e) {
        isDragging = false;
//        screenLocation = null;
//        LOG.info("" + e);
//        if (((JComponent) e.getComponent()).getMousePosition(true) != null)  {
//            updateRollover(e, ROLLOVER_KEY, false);
//        } else {
//        }
        ((JComponent) e.getSource()).putClientProperty(ROLLOVER_KEY, null);
        ((JComponent) e.getSource()).putClientProperty(CLICKED_KEY, null);

        }

    /**
     * Implemented to do nothing.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Implemented to do nothing.
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    // ---------------- MouseMotionListener
    /**
     * Implemented to set a dragging flag to true.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        isDragging = true;
    }

    /**
     * Implemented to map to client property rollover and fire only if client
     * coordinate changed.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        updateRollover(e, ROLLOVER_KEY, false);
    }

    //---------------- ComponentListener

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        updateRollover(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        updateRollover(e);
    }

    /**
     * @param e
     */
    private void updateRollover(ComponentEvent e) {
        Point componentLocation = e.getComponent().getMousePosition();
        if (componentLocation == null) {
            componentLocation = new Point(-1, -1);
        }
//        LOG.info("" + componentLocation + " / " + e);
        updateRolloverPoint((JComponent) e.getComponent(), componentLocation);
        updateClientProperty((JComponent) e.getComponent(), ROLLOVER_KEY, true);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    //---------------- mapping methods

    /**
     * Controls the mapping of the given mouse event to a client property. This
     * implementation first calls updateRolloverPoint to convert the mouse coordinates.
     * Then calls updateClientProperty to actually set the client property in the
     *
     * @param e the MouseEvent to map to client coordinates
     * @param property the client property to map to
     * @param fireAlways a flag indicating whether a client event should be fired if unchanged.
     *
     * @see #updateRolloverPoint(JComponent, Point)
     * @see #updateClientProperty(JComponent, String, boolean)
     */
    protected void updateRollover(MouseEvent e, String property,
            boolean fireAlways) {
        updateRolloverPoint((JComponent) e.getComponent(), e.getPoint());
        updateClientProperty((JComponent) e.getComponent(), property, fireAlways);
    }

    /** Current mouse location in client coordinates. */
    protected Point rollover = new Point(-1, -1);

    /**
     * Sets the given client property to the value of current mouse location in
     * client coordinates. If fireAlways, the property is force to fire a change.
     *
     * @param component the target component
     * @param property the client property to set
     * @param fireAlways a flag indicating whether a client property
     *  should be forced to fire an event.
     */
    protected void updateClientProperty(JComponent component, String property,
            boolean fireAlways) {
        if (fireAlways) {
            // fix Issue #864-swingx: force propertyChangeEvent
            component.putClientProperty(property, null);
            component.putClientProperty(property, new Point(rollover));
        } else {
            Point p = (Point) component.getClientProperty(property);
            if (p == null || (rollover.x != p.x) || (rollover.y != p.y)) {
                component.putClientProperty(property, new Point(rollover));
            }
        }
    }

    /**
     * Subclasses must implement to map the given mouse coordinates into
     * appropriate client coordinates. The result must be stored in the
     * rollover field.
     *
     * @param component the target component which received a mouse event
     * @param mousePoint the mouse position of the event, coordinates are
     *    component pixels
     */
    protected abstract void updateRolloverPoint(JComponent component, Point mousePoint);

}
