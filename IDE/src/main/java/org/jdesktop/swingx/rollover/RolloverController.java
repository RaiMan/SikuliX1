/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.rollover;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.plaf.UIAction;

/**
 * Controller for "live" behaviour of XXRenderers.
 *
 * Once installed on a component, it updates renderer's rollover
 * state based on the component's rollover properties. Rollover
 * client properties are Points with cell coordinates
 * in the view coordinate
 * system as appropriate for the concrete component
 * (Point.x == column, Point.y == row).
 *
 * Repaints effected component regions. Updates
 * link cursor. Installs a click-action bound to space-released in the target's
 * actionMap/inputMap.
 *
 *
 * @author Jeanette Winzenburg, Berlin
 */
public abstract class RolloverController<T extends JComponent> implements
        PropertyChangeListener {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(RolloverController.class
            .getName());
    /**
     * the key of the rollover click action which is installed in the
     * component's actionMap.
     */
    public static final String EXECUTE_BUTTON_ACTIONCOMMAND = "executeButtonAction";

    protected T component;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // JW: should not happen ... being paranoid.
        if ((component == null) || (component != evt.getSource()))
            return;
        if (RolloverProducer.ROLLOVER_KEY.equals(evt.getPropertyName())) {
            rollover((Point) evt.getOldValue(), (Point) evt.getNewValue());
        } else if (RolloverProducer.CLICKED_KEY.equals(evt.getPropertyName())) {
            click((Point) evt.getNewValue());
        }
    }

    /**
     * Install this as controller for the given component.
     *
     * @param table the component which has renderers to control.
     */
    public void install(T table) {
        release();
        this.component = table;
        table.addPropertyChangeListener(RolloverProducer.CLICKED_KEY, this);
        table.addPropertyChangeListener(RolloverProducer.ROLLOVER_KEY, this);
        registerExecuteButtonAction();
    }

    /**
     * Uninstall this as controller from the component, if any.
     *
     */
    public void release() {
        if (component == null)
            return;
        component.removePropertyChangeListener(RolloverProducer.CLICKED_KEY, this);
        component.removePropertyChangeListener(RolloverProducer.ROLLOVER_KEY, this);
        unregisterExecuteButtonAction();
        component = null;
    }

    /**
     * called on change of client property Rollover_Key.
     *
     * @param oldLocation the old value of the rollover location.
     * @param newLocation the new value of the rollover location.
     */
    protected abstract void rollover(Point oldLocation, Point newLocation);

    /**
     * called on change of client property Clicked_key.
     * @param location the new value of the clicked location.
     */
    protected void click(Point location) {
        if (!isClickable(location))
            return;
        RolloverRenderer rollover = getRolloverRenderer(location, true);
        if (rollover != null) {
            rollover.doClick();
            component.repaint();
        }
    }

    /**
     * Returns the rolloverRenderer at the given location. <p>
     *
     * The result
     * may be null if there is none or if rollover is not enabled.
     *
     * If the prepare flag is true, the renderer will be prepared
     * with value and state as appropriate for the given location.
     *
     * Note: PRE - the location must be valid in cell coordinate space.
     *
     * @param location a valid location in cell coordinates, p.x == column, p.y == row.
     * @param prepare
     * @return <code>RolloverRenderer</code> at the given location
     */
    protected abstract RolloverRenderer getRolloverRenderer(Point location,
            boolean prepare);

    /**
     * Returns a boolean indicating whether or not the cell at the given
     * location is clickable. <p>
     *
     * This implementation returns true if the target is enabled and the
     * cell has a rollover renderer.
     *
     * @param location in cell coordinates, p.x == column, p.y == row.
     * @return true if the cell at the given location is clickable
     *
     * @see #hasRollover(Point)
     */
    protected boolean isClickable(Point location) {
        return component.isEnabled() && hasRollover(location);
    }

    /**
     * Returns a boolean indicating whether the or not the cell at the
     * given has a rollover renderer. Always returns false if the location
     * is not valid.
     *
     * @param location in cell coordinates, p.x == column, p.y == row.
     * @return true if the location is valid and has rollover effects, false
     *   otherwise.
     *
     */
    protected boolean hasRollover(Point location) {
        if (location == null || location.x < 0 || location.y < 0)
            return false;
        return getRolloverRenderer(location, false) != null;
    }

    /**
     * The coordinates of the focused cell in view coordinates.
     *
     * This method is called if the click action is invoked by a keyStroke.
     * The returned cell coordinates should be related to
     * what is typically interpreted as "focused" in the context of the
     * component.
     *
     * p.x == focused column, p.y == focused row.
     * A null return value or any coordinate value of < 0
     * is interpreted as "outside".
     *
     * @return the location of the focused cell.
     */
    protected abstract Point getFocusedCell();

    /**
     * uninstalls and deregisters the click action from the component's
     * actionMap/inputMap.
     *
     */
    protected void unregisterExecuteButtonAction() {
        component.getActionMap().put(EXECUTE_BUTTON_ACTIONCOMMAND, null);
        KeyStroke space = KeyStroke.getKeyStroke("released SPACE");
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                space, null);
    }

    /**
     * installs and registers the click action in the component's
     * actionMap/inputMap.
     *
     */
    protected void registerExecuteButtonAction() {
        component.getActionMap().put(EXECUTE_BUTTON_ACTIONCOMMAND,
                createExecuteButtonAction());
        KeyStroke space = KeyStroke.getKeyStroke("released SPACE");
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                space, EXECUTE_BUTTON_ACTIONCOMMAND);

    }

    /**
     * creates and returns the click action to install in the
     * component's actionMap.
     *
     */
    protected Action createExecuteButtonAction() {
        return new UIAction(null) {
            @Override
            public void actionPerformed(ActionEvent e) {
                click(getFocusedCell());
            }

            @Override
            public boolean isEnabled(Object sender) {
                if (component == null || !component.isEnabled() || !component.hasFocus())
                    return false;
                return isClickable(getFocusedCell());
            }
        };
    }

}
