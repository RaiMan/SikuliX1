/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic.core;

import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.dnd.DragSource;
import javax.swing.*;

import org.jdesktop.swingx.SwingXUtilities;
//import sun.awt.dnd.SunDragSourceContextPeer;
//import sun.awt.AppContext;

/**
 * Drag gesture recognition support for classes that have a
 * <code>TransferHandler</code>. The gesture for a drag in this class is a mouse
 * press followed by movement by <code>DragSource.getDragThreshold()</code>
 * pixels. An instance of this class is maintained per AppContext, and the
 * public static methods call into the appropriate instance. <p>
 *
 * This is a c&p of core (package private) needed for BasicXListUI. It differs from
 * core in that references to sun packages have been replaced.
 * <ul>
 * <li> a static method of SunDragSourceContextPeer has been copied into SwingXUtilities
 *    and is used here
 * <li> the shared instance of this class is maintained in the UIManager instead of
 *   per appContext.
 * </ul>
 *
 * @author Shannon Hickey
 * @version 1.2 11/17/05
 */
public class DragRecognitionSupport {
    private int motionThreshold;
    private MouseEvent dndArmedEvent;
    private JComponent component;

    /**
     * This interface allows us to pass in a handler to mouseDragged,
     * so that we can be notified immediately before a drag begins.
     */
    public static interface BeforeDrag {
        public void dragStarting(MouseEvent me);
    }

    /**
     * Returns the DragRecognitionSupport for the caller's AppContext.
     */
    private static DragRecognitionSupport getDragRecognitionSupport() {
//        DragRecognitionSupport support =
//            (DragRecognitionSupport)AppContext.getAppContext().
//                get(DragRecognitionSupport.class);
//
//        if (support == null) {
//            support = new DragRecognitionSupport();
//            AppContext.getAppContext().put(DragRecognitionSupport.class, support);
//        }

        DragRecognitionSupport support = (DragRecognitionSupport)
            UIManager.get("sharedInstance.dragRecognitionSupport");
        if (support == null) {
            support = new DragRecognitionSupport();
            UIManager.put("sharedInstance.dragRecognitionSupport", support);
        }
        return support;
    }

    /**
     * Returns whether or not the event is potentially part of a drag sequence.
     */
    public static boolean mousePressed(MouseEvent me) {
        return ((DragRecognitionSupport)getDragRecognitionSupport()).
            mousePressedImpl(me);
    }

    /**
     * If a dnd recognition has been going on, return the MouseEvent
     * that started the recognition. Otherwise, return null.
     */
    public static MouseEvent mouseReleased(MouseEvent me) {
        return ((DragRecognitionSupport)getDragRecognitionSupport()).
            mouseReleasedImpl(me);
    }

    /**
     * Returns whether or not a drag gesture recognition is ongoing.
     */
    public static boolean mouseDragged(MouseEvent me, BeforeDrag bd) {
        return ((DragRecognitionSupport)getDragRecognitionSupport()).
            mouseDraggedImpl(me, bd);
    }

    private void clearState() {
        dndArmedEvent = null;
        component = null;
    }

    private int mapDragOperationFromModifiers(MouseEvent me,
                                              TransferHandler th) {

        if (th == null || !SwingUtilities.isLeftMouseButton(me)) {
            return TransferHandler.NONE;
        }
        // PENDING JW: c'p from SunDragSourceContextPeer
        return SwingXUtilities.
            convertModifiersToDropAction(me.getModifiersEx(),
                                         th.getSourceActions(component));
    }

    /**
     * Returns whether or not the event is potentially part of a drag sequence.
     */
    private boolean mousePressedImpl(MouseEvent me) {
        component = (JComponent)me.getSource();

        if (mapDragOperationFromModifiers(me, component.getTransferHandler())
                != TransferHandler.NONE) {

            motionThreshold = DragSource.getDragThreshold();
            dndArmedEvent = me;
            return true;
        }

        clearState();
        return false;
    }

    /**
     * If a dnd recognition has been going on, return the MouseEvent
     * that started the recognition. Otherwise, return null.
     */
    private MouseEvent mouseReleasedImpl(MouseEvent me) {
        /* no recognition has been going on */
        if (dndArmedEvent == null) {
            return null;
        }

        MouseEvent retEvent = null;

        if (me.getSource() == component) {
            retEvent = dndArmedEvent;
        } // else component has changed unexpectedly, so return null

        clearState();
        return retEvent;
    }

    /**
     * Returns whether or not a drag gesture recognition is ongoing.
     */
    private boolean mouseDraggedImpl(MouseEvent me, BeforeDrag bd) {
        /* no recognition is in progress */
        if (dndArmedEvent == null) {
            return false;
        }

        /* component has changed unexpectedly, so bail */
        if (me.getSource() != component) {
            clearState();
            return false;
        }

        int dx = Math.abs(me.getX() - dndArmedEvent.getX());
        int dy = Math.abs(me.getY() - dndArmedEvent.getY());
        if ((dx > motionThreshold) || (dy > motionThreshold)) {
            TransferHandler th = component.getTransferHandler();
            int action = mapDragOperationFromModifiers(me, th);
            if (action != TransferHandler.NONE) {
                /* notify the BeforeDrag instance */
                if (bd != null) {
                    bd.dragStarting(dndArmedEvent);
                }
                th.exportAsDrag(component, dndArmedEvent, action);
                clearState();
            }
        }

        return true;
    }
}
