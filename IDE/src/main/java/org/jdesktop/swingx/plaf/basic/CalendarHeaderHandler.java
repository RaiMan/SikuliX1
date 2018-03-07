/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.action.AbstractActionExt;

/**
 * Provides and wires a component appropriate as a calendar navigation header.
 * The design idea is to support a pluggable header for a zoomable (PENDING JW:
 * naming!) JXMonthView. Then custom implementations can be tailored to exactly
 * fit their needs.
 * <p>
 *
 * To install a custom implementation, register the class name of the custom
 * header handler with the key <code>CalendarHeaderHandler.uiControllerID</code>
 * , example:
 *
 * <pre>
 * <code>
 *  UIManager.put(CalendarHeaderHandler.uiControllerID, &quot;com.foo.bar.MagicHeaderHandler&quot;)
 * </code>
 * </pre>
 *
 * Basic navigation action should (will) be defined by the ui delegate itself (PENDING
 * JW: still incomplete in BasicMonthViewUI). This handler can modify/enhance
 * them as appropriate for its context.
 * <p>
 *
 * PENDING JW: those icons ... who's responsible? Shouldn't we use any of the
 * default arrows as defined in the laf anyway (are there any?)
 * <p>
 *
 * <b>Note</b>: this is work-in-progress, be prepared to change if subclassing
 * for custom requirements!
 *
 * @author Jeanette Winzenburg
 */
public abstract class CalendarHeaderHandler {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CalendarHeaderHandler.class.getName());

    public static final String uiControllerID = "CalendarHeaderHandler";

    protected JXMonthView monthView;

    private JComponent calendarHeader;

    protected Icon monthDownImage;

    protected Icon monthUpImage;

    private PropertyChangeListener monthViewPropertyChangeListener;

    /**
     * Installs this handler to the given month view.
     *
     * @param monthView the target month view to install to.
     */
    public void install(JXMonthView monthView) {
        this.monthView = monthView;
        // PENDING JW: remove here if rendererHandler takes over control
        // completely
        // as is, some properties are duplicated
        monthDownImage = UIManager.getIcon("JXMonthView.monthDownFileName");
        monthUpImage = UIManager.getIcon("JXMonthView.monthUpFileName");
        installNavigationActions();
        installListeners();
        componentOrientationChanged();
        monthStringBackgroundChanged();
        fontChanged();
    }

    /**
     * Uninstalls this handler from the given target month view.
     *
     * @param monthView the target month view to install from.
     */
    public void uninstall(JXMonthView monthView) {
        this.monthView.remove(getHeaderComponent());
        uninstallListeners();
        this.monthView = null;
    }

    /**
     * Returns a component to be used as header in a zoomable month view,
     * guaranteed to be not null.
     *
     * @return a component to be used as header in a zoomable JXMonthView
     */
    public JComponent getHeaderComponent() {
        if (calendarHeader == null) {
            calendarHeader = createCalendarHeader();
        }
        return calendarHeader;
    }

    /**
     * Creates and registered listeners on the monthView as appropriate. This
     * implementation registers a PropertyChangeListener which synchronizes
     * internal state on changes of componentOrientation, font and
     * monthStringBackground.
     */
    protected void installListeners() {
        monthView
                .addPropertyChangeListener(getMonthViewPropertyChangeListener());
    }

    /**
     * Unregisters listeners which had been installed to the monthView.
     */
    protected void uninstallListeners() {
        monthView.removePropertyChangeListener(monthViewPropertyChangeListener);
    }

    /**
     * Returns the propertyChangelistener for the monthView. Lazily created.
     *
     * @return the propertyChangeListener for the monthView.
     */
    private PropertyChangeListener getMonthViewPropertyChangeListener() {
        if (monthViewPropertyChangeListener == null) {
            monthViewPropertyChangeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("componentOrientation".equals(evt.getPropertyName())) {
                        componentOrientationChanged();
                    } else if ("font".equals(evt.getPropertyName())) {
                        fontChanged();
                    } else if ("monthStringBackground".equals(evt
                            .getPropertyName())) {
                        monthStringBackgroundChanged();
                    }

                }
            };
        }
        return monthViewPropertyChangeListener;
    }

    /**
     * Synchronizes internal state which depends on the month view's
     * monthStringBackground.
     */
    protected void monthStringBackgroundChanged() {
        getHeaderComponent().setBackground(
                getAsNotUIResource(monthView.getMonthStringBackground()));

    }

    /**
     * Synchronizes internal state which depends on the month view's font.
     */
    protected void fontChanged() {
        getHeaderComponent().setFont(getAsNotUIResource(createDerivedFont()));
        monthView.revalidate();
    }

    /**
     * Synchronizes internal state which depends on the month view's
     * componentOrientation.
     *
     * This implementation updates the month navigation icons and the header
     * component's orientation.
     */
    protected void componentOrientationChanged() {
        getHeaderComponent().applyComponentOrientation(
                monthView.getComponentOrientation());
        if (monthView.getComponentOrientation().isLeftToRight()) {
            updateMonthNavigationIcons(monthDownImage, monthUpImage);
        } else {
            updateMonthNavigationIcons(monthUpImage, monthDownImage);
        }
    }

    /**
     * @param previous the icon to use in the previousMonth action
     * @param next the icon to use on the nextMonth action
     */
    private void updateMonthNavigationIcons(Icon previous, Icon next) {
        updateActionIcon("previousMonth", previous);
        updateActionIcon("nextMonth", next);
    }

    /**
     * @param previousKey
     * @param previous
     */
    private void updateActionIcon(String previousKey, Icon previous) {
        Action action = monthView.getActionMap().get(previousKey);
        if (action != null) {
            action.putValue(Action.SMALL_ICON, previous);
        }
    }

    /**
     * Creates and returns the component used as header in a zoomable monthView.
     *
     * @return the component used as header in a zoomable monthView, guaranteed
     *         to be not null.
     */
    protected abstract JComponent createCalendarHeader();

    /**
     * Installs and configures navigational actions.
     * <p>
     *
     * This implementation creates and installs wrappers around the
     * scrollToPrevious/-NextMonth actions installed by the ui and configures
     * them with the appropriate next/previous icons.
     */
    protected void installNavigationActions() {
        installWrapper("scrollToPreviousMonth", "previousMonth", monthView
                .getComponentOrientation().isLeftToRight() ? monthDownImage
                : monthUpImage);
        installWrapper("scrollToNextMonth", "nextMonth", monthView
                .getComponentOrientation().isLeftToRight() ? monthUpImage
                : monthDownImage);
    }

    /**
     * Creates an life action wrapper around the action registered with
     * actionKey, sets its SMALL_ICON property to the given icon and installs
     * itself with the newActionKey.
     *
     * @param actionKey the key of the action to wrap around
     * @param newActionKey the key of the wrapper action
     * @param icon the icon to use in the wrapper action
     */
    private void installWrapper(final String actionKey, String newActionKey,
            Icon icon) {
        AbstractActionExt wrapper = new AbstractActionExt(null, icon) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Action action = monthView.getActionMap().get(actionKey);
                if (action != null) {
                    action.actionPerformed(e);
                }
            }

        };
        monthView.getActionMap().put(newActionKey, wrapper);
    }

    /**
     * Returns a Font based on the param which is not of type UIResource.
     *
     * @param font the base font
     * @return a font not of type UIResource, may be null.
     */
    private Font getAsNotUIResource(Font font) {
        if (!(font instanceof UIResource))
            return font;
        // PENDING JW: correct way to create another font instance?
        return font.deriveFont(font.getAttributes());
    }

    /**
     * Returns a Color based on the param which is not of type UIResource.
     *
     * @param color the base color
     * @return a color not of type UIResource, may be null.
     */
    private Color getAsNotUIResource(Color color) {
        if (!(color instanceof UIResource))
            return color;
        // PENDING JW: correct way to create another color instance?
        float[] rgb = color.getRGBComponents(null);
        return new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
    }

    /**
     * Create a derived font used to when painting various pieces of the month
     * view component. This method will be called whenever the font on the
     * component is set so a new derived font can be created.
     */
    protected Font createDerivedFont() {
        return monthView.getFont().deriveFont(Font.BOLD);
    }

}
