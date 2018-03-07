/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.Timer;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.util.WindowUtils;

/**
 * <p>
 * {@code JXFrame} is an enhanced {@link JFrame}. While {@code JXFrame} can
 * replace any {@code JFrame}, it has features that make it particularly useful
 * as the "main" frame for an application.
 * </p>
 * <h3>Additional Features</h3>
 * <p>
 * Root pane: {@code JXFrame} uses {@link JXRootPane} as its default root pane.
 * The frame provide several convenience methods to provide easy access to the
 * additional features.
 * </p>
 * <p>
 * Idle: {@code JXFrame} offers an idle timer. Registering a
 * {@link java.beans.PropertyChangeListener} for "idle" will notify when the
 * user has not interacted with the JVM. A primary use for this type of
 * functionality is to secure the application, blocking access and requiring the
 * user to login again.
 * </p>
 * <p>
 * Wait (busy) glass pane: The {@code JXFrame} can be configured with an
 * alternate glass pane. Typically, this glass pane is used to notify the user
 * that the application is busy, but the glass pane could be for any purpose.
 * This secondary glass pane can be quickly enabled or disabled by
 * {@linkplain #setWaitPaneVisible(boolean) setting the wait pane visible}.
 * </p>
 *
 * @author unascribed from JDNC
 */
@JavaBean
@SuppressWarnings({ "nls", "serial" })
public class JXFrame extends JFrame {
    /**
     * An enumeration of {@link JXFrame} starting locations.
     *
     * @author unascribed from JDNC
     */
    public enum StartPosition {CenterInScreen, CenterInParent, Manual}

    private Component waitPane = null;
    private Component glassPane = null;
    private boolean waitPaneVisible = false;
    private Cursor realCursor = null;
    private boolean waitCursorVisible = false;
    private boolean waiting = false;
    private StartPosition startPosition;
    private boolean hasBeenVisible = false; //startPosition is only used the first time the window is shown
    private AWTEventListener keyEventListener; //for listening to KeyPreview events
    private boolean keyPreview = false;
    private AWTEventListener idleListener; //for listening to events. If no events happen for a specific amount of time, mark as idle
    private Timer idleTimer;
    private long idleThreshold = 0;
    private boolean idle;

    /**
     * Creates a {@code JXFrame} with no title and standard closing behavior.
     */
    public JXFrame() {
        this(null, false);
    }

    /**
     * Creates a {@code JXFrame} with the specified title and default closing
     * behavior.
     *
     * @param title
     *            the frame title
     */
    public JXFrame(String title) {
        this(title, false);
    }

    /**
     * Creates a <code>JXFrame</code> in the specified
     * <code>GraphicsConfiguration</code> of
     * a screen device, a blank title and default closing behaviour.
     * <p>
     *
     * @param gc the <code>GraphicsConfiguration</code> that is used
     *          to construct the new <code>Frame</code>;
     *          if <code>gc</code> is <code>null</code>, the system
     *          default <code>GraphicsConfiguration</code> is assumed
     * @exception IllegalArgumentException if <code>gc</code> is not from
     *          a screen device.  This exception is always thrown when
     *      GraphicsEnvironment.isHeadless() returns true.
     */
    public JXFrame(GraphicsConfiguration gc) {
        this(null, gc, false);
    }

    /**
     * Creates a <code>JXFrame</code> with the specified title, the
     * specified <code>GraphicsConfiguration</code> of a screen device and
     * default closing behaviour.
     * <p>
     *
     * @param title the title to be displayed in the
     *          frame's border. A <code>null</code> value is treated as
     *          an empty string, "".
     * @param gc the <code>GraphicsConfiguration</code> that is used
     *          to construct the new <code>JFrame</code> with;
     *          if <code>gc</code> is <code>null</code>, the system
     *          default <code>GraphicsConfiguration</code> is assumed
     * @exception IllegalArgumentException if <code>gc</code> is not from
     *          a screen device.  This exception is always thrown when
     *      GraphicsEnvironment.isHeadless() returns true.
     */
    public JXFrame(String title, GraphicsConfiguration gc) {
        this(title, gc, false);
     }

    /**
     * Creates a {@code JXFrame} with the specified title and closing behavior.
     *
     * @param title
     *            the frame title
     * @param exitOnClose
     *            {@code true} to override the default ({@link JFrame}) closing
     *            behavior and use {@link JFrame#EXIT_ON_CLOSE EXIT_ON_CLOSE}
     *            instead; {@code false} to use the default behavior
     */
    public JXFrame(String title, boolean exitOnClose) {
        this(title, null, exitOnClose);
    }

    /**
     * Creates a {@code JXFrame} with the specified title, GraphicsConfiguration
     * and closing behavior.
     *
     * @param title the frame title
     * @param gc the <code>GraphicsConfiguration</code> of the target screen
     *        device. If <code>gc</code> is <code>null</code>, the system
     *        default <code>GraphicsConfiguration</code> is assumed.
     * @param exitOnClose {@code true} to override the default ({@link JFrame})
     *        closing behavior and use {@link JFrame#EXIT_ON_CLOSE
     *        EXIT_ON_CLOSE} instead; {@code false} to use the default behavior
     * @exception IllegalArgumentException if <code>gc</code> is not from a
     *            screen device.
     *
     */
   public JXFrame(String title, GraphicsConfiguration gc, boolean exitOnClose) {
        super(title, gc);
        if (exitOnClose) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        //create the event handler for key preview functionality
        keyEventListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent aWTEvent) {
                if (aWTEvent instanceof KeyEvent) {
                    KeyEvent evt = (KeyEvent)aWTEvent;
                    for (KeyListener kl : getKeyListeners()) {
                        int id = aWTEvent.getID();
                        switch (id) {
                            case KeyEvent.KEY_PRESSED:
                                kl.keyPressed(evt);
                                break;
                            case KeyEvent.KEY_RELEASED:
                                kl.keyReleased(evt);
                                break;
                            case KeyEvent.KEY_TYPED:
                                kl.keyTyped(evt);
                                break;
                            default:
                                System.err.println("Unhandled Key ID: " + id);
                        }
                    }
                }
            }
        };

        idleTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setIdle(true);
            }
        });

        //create the event handler for key preview functionality
        idleListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent aWTEvent) {
                //reset the timer
                idleTimer.stop();
                //if the user is idle, then change to not idle
                if (isIdle()) {
                    setIdle(false);
                }
                //start the timer
                idleTimer.restart();
            }
        };
    }

    /**
     * Sets the cancel button property on the underlying {@code JXRootPane}.
     *
     * @param button
     *            the {@code JButton} which is to be the cancel button
     * @see #getCancelButton()
     * @see JXRootPane#setCancelButton(JButton)
     */
    public void setCancelButton(JButton button) {
        getRootPaneExt().setCancelButton(button);
    }

    /**
     * Returns the value of the cancel button property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JButton} which is the cancel button
     * @see #setCancelButton(JButton)
     * @see JXRootPane#getCancelButton()
     */
    public JButton getCancelButton() {
        return getRootPaneExt().getCancelButton();
    }

    /**
     * Sets the default button property on the underlying {@code JRootPane}.
     *
     * @param button
     *            the {@code JButton} which is to be the default button
     * @see #getDefaultButton()
     * @see JXRootPane#setDefaultButton(JButton)
     */
    public void setDefaultButton(JButton button) {
        JButton old = getDefaultButton();
        getRootPane().setDefaultButton(button);
        firePropertyChange("defaultButton", old, getDefaultButton());
    }

    /**
     * Returns the value of the default button property from the underlying
     * {@code JRootPane}.
     *
     * @return the {@code JButton} which is the default button
     * @see #setDefaultButton(JButton)
     * @see JXRootPane#getDefaultButton()
     */
    public JButton getDefaultButton() {
        return getRootPane().getDefaultButton();
    }

    /**
     * If enabled the {@code KeyListener}s will receive a preview of the {@code
     * KeyEvent} prior to normal viewing.
     *
     * @param flag {@code true} to enable previewing; {@code false} otherwise
     * @see #getKeyPreview()
     * @see #addKeyListener(KeyListener)
     */
    public void setKeyPreview(boolean flag) {
        Toolkit.getDefaultToolkit().removeAWTEventListener(keyEventListener);
        if (flag) {
            Toolkit.getDefaultToolkit().addAWTEventListener(keyEventListener, AWTEvent.KEY_EVENT_MASK);
        }
        boolean old = keyPreview;
        keyPreview = flag;
        firePropertyChange("keyPreview", old, keyPreview);
    }

    /**
     * Returns the value for the key preview.
     *
     * @return if {@code true} previewing is enabled; otherwise it is not
     * @see #setKeyPreview(boolean)
     */
    public final boolean getKeyPreview() {
        return keyPreview;
    }

    /**
     * Sets the start position for this frame. Setting this value only has an
     * effect is the frame has never been displayed.
     *
     * @param position
     *            the position to display the frame at
     * @see #getStartPosition()
     * @see #setVisible(boolean)
     */
    public void setStartPosition(StartPosition position) {
        StartPosition old = getStartPosition();
        this.startPosition = position;
        firePropertyChange("startPosition", old, getStartPosition());
    }

    /**
     * Returns the start position for this frame.
     *
     * @return the start position of the frame
     * @see #setStartPosition(StartPosition)
     */
    public StartPosition getStartPosition() {
        return startPosition == null ? StartPosition.Manual : startPosition;
    }

    /**
     * Switches the display cursor to or from the wait cursor.
     *
     * @param flag
     *            {@code true} to enable the wait cursor; {@code false} to
     *            enable the previous cursor
     * @see #isWaitCursorVisible()
     * @see Cursor#WAIT_CURSOR
     */
    public void setWaitCursorVisible(boolean flag) {
        boolean old = isWaitCursorVisible();
        if (flag != old) {
            waitCursorVisible = flag;
            if (isWaitCursorVisible()) {
                realCursor = getCursor();
                super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            } else {
                super.setCursor(realCursor);
            }
            firePropertyChange("waitCursorVisible", old, isWaitCursorVisible());
        }
    }

    /**
     * Returns the state of the wait cursor visibility.
     *
     * @return {@code true} if the current cursor is the wait cursor; {@code
     *         false} otherwise
     */
    public boolean isWaitCursorVisible() {
        return waitCursorVisible;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCursor(Cursor c) {
        if (!isWaitCursorVisible()) {
            super.setCursor(c);
        } else {
            this.realCursor = c;
        }
    }

    /**
     * Sets the component to use as a wait glass pane. This component is not
     * part of the display hierarchy unless {@code isWaitPaneVisible() == true}.
     *
     * @param c
     *            the wait glass pane for this frame
     * @see #getWaitPane()
     * @see #setWaitPaneVisible(boolean)
     */
    public void setWaitPane(Component c) {
        Component old = getWaitPane();
        this.waitPane = c;
        firePropertyChange("waitPane", old, getWaitPane());
    }

    /**
     * Returns the current wait pane for this frame. This component may or may
     * not be part of the display hierarchy.
     *
     * @return the current wait pane
     * @see #setWaitPane(Component)
     */
    public Component getWaitPane() {
        return waitPane;
    }

    /**
     * Enabled or disabled the display of the normal or wait glass pane. If
     * {@code true} the wait pane is be displayed. Altering this property alters
     * the display hierarchy.
     *
     * @param flag
     *            {@code true} to display the wait glass pane; {@code false} to
     *            display the normal glass pane
     * @see #isWaitPaneVisible()
     * @see #setWaitPane(Component)
     */
    public void setWaitPaneVisible(boolean flag) {
        boolean old = isWaitPaneVisible();
        if (flag != old) {
            this.waitPaneVisible = flag;
            Component wp = getWaitPane();
            if (isWaitPaneVisible()) {
                glassPane = getRootPane().getGlassPane();
                if (wp != null) {
                    getRootPane().setGlassPane(wp);
                    wp.setVisible(true);
                }
            } else {
                if (wp != null) {
                    wp.setVisible(false);
                }
                getRootPane().setGlassPane(glassPane);
            }
            firePropertyChange("waitPaneVisible", old, isWaitPaneVisible());
        }
    }

    /**
     * Returns the current visibility of the wait glass pane.
     *
     * @return {@code true} if the wait glass pane is visible; {@code false}
     *         otherwise
     */
    public boolean isWaitPaneVisible() {
        return waitPaneVisible;
    }

    /**
     * Sets the frame into a wait state or restores the frame from a wait state.
     *
     * @param waiting
     *            {@code true} to place the frame in a wait state; {@code false}
     *            otherwise
     * @see #isWaiting()
     * @see #setWaitCursorVisible(boolean)
     * @see #setWaitPaneVisible(boolean)
     */
    public void setWaiting(boolean waiting) {
        boolean old = isWaiting();
        this.waiting = waiting;
        firePropertyChange("waiting", old, isWaiting());
        setWaitPaneVisible(waiting);
        setWaitCursorVisible(waiting);
    }

    /**
     * Determines if the frame is in a wait state or not.
     *
     * @return {@code true} if the frame is in the wait state; {@code false}
     *         otherwise
     * @see #setWaiting(boolean)
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        if (!hasBeenVisible && visible) {
            //move to the proper start position
            StartPosition pos = getStartPosition();
            switch (pos) {
                case CenterInParent:
                    setLocationRelativeTo(getParent());
                    break;
                case CenterInScreen:
                    setLocation(WindowUtils.getPointForCentering(this));
                    break;
                case Manual:
                default:
                    //nothing to do!
            }
        }
        super.setVisible(visible);
    }

    public boolean isIdle() {
        return idle;
    }

    /**
     * Sets the frame into an idle state or restores the frame from an idle state.
     *
     * @param idle
     *            {@code true} to place the frame in an idle state; {@code false}
     *            otherwise
     * @see #isIdle()
     * @see #setIdleThreshold(long)
     */
    public void setIdle(boolean idle) {
        boolean old = isIdle();
        this.idle = idle;
        firePropertyChange("idle", old, isIdle());
    }

    /**
     * Sets a threshold for user interaction before automatically placing the
     * frame in an idle state.
     *
     * @param threshold
     *            the time (in milliseconds) to elapse before setting the frame
     *            idle
     * @see #getIdleThreshold()
     * @see #setIdle(boolean)
     */
    public void setIdleThreshold(long threshold) {
        long old = getIdleThreshold();
        this.idleThreshold = threshold;
        firePropertyChange("idleThreshold", old, getIdleThreshold());

        threshold = getIdleThreshold(); // in case the getIdleThreshold method has been overridden

        Toolkit.getDefaultToolkit().removeAWTEventListener(idleListener);
        if (threshold > 0) {
            Toolkit.getDefaultToolkit().addAWTEventListener(idleListener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }
        idleTimer.stop();
        idleTimer.setInitialDelay((int)threshold);
        idleTimer.restart();
    }

    /**
     * Returns the amount of time that must elapse before the frame
     * automatically enters an idle state.
     *
     * @return the time in milliseconds
     */
    public long getIdleThreshold() {
        return idleThreshold;
    }

    /**
     * Sets the status bar property on the underlying {@code JXRootPane}.
     *
     * @param statusBar
     *            the {@code JXStatusBar} which is to be the status bar
     * @see #getStatusBar()
     * @see JXRootPane#setStatusBar(JXStatusBar)
     */
    public void setStatusBar(JXStatusBar statusBar) {
        getRootPaneExt().setStatusBar(statusBar);
    }

    /**
     * Returns the value of the status bar property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JXStatusBar} which is the current status bar
     * @see #setStatusBar(JXStatusBar)
     * @see JXRootPane#getStatusBar()
     */
    public JXStatusBar getStatusBar() {
        return getRootPaneExt().getStatusBar();
    }

    /**
     * Sets the tool bar property on the underlying {@code JXRootPane}.
     *
     * @param toolBar
     *            the {@code JToolBar} which is to be the tool bar
     * @see #getToolBar()
     * @see JXRootPane#setToolBar(JToolBar)
     */
    public void setToolBar(JToolBar toolBar) {
        getRootPaneExt().setToolBar(toolBar);
    }

    /**
     * Returns the value of the tool bar property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JToolBar} which is the current tool bar
     * @see #setToolBar(JToolBar)
     * @see JXRootPane#getToolBar()
     */
    public JToolBar getToolBar() {
        return getRootPaneExt().getToolBar();
    }

    //---------------------------------------------------- Root Pane Methods
    /**
     * Overridden to create a JXRootPane.
     */
    @Override
    protected JRootPane createRootPane() {
        return new JXRootPane();
    }

    /**
     * Overridden to make this public.
     */
    @Override
    public void setRootPane(JRootPane root) {
        super.setRootPane(root);
    }

    /**
     * Return the extended root pane. If this frame doesn't contain
     * an extended root pane the root pane should be accessed with
     * getRootPane().
     *
     * @return the extended root pane or null.
     */
    public JXRootPane getRootPaneExt() {
        if (rootPane instanceof JXRootPane) {
            return (JXRootPane)rootPane;
        }
        return null;
    }
}
