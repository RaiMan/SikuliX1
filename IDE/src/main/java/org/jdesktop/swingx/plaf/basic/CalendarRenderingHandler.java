/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXMonthView;

/**
 * The RenderingHandler responsible for text rendering. It provides
 * and configures a rendering component for the given cell of
 * a JXMonthView. <p>
 *
 * @author Jeanette Winzenburg
 */
public interface CalendarRenderingHandler {

    /**
     * Configures and returns a component for rendering of the given monthView cell.
     *
     * @param monthView the JXMonthView to render onto
     * @param calendar the cell value
     * @param state the DayState of the cell
     * @return a component configured for rendering the given cell
     */
    public JComponent prepareRenderingComponent(JXMonthView monthView,
            Calendar calendar, CalendarState state);

    /**
     * Updates internal state to the given Locale.
     *
     * PENDING JW: ideally, the handler should be stateless and this method
     * removed. Currently needed because there is no way to get the Locale
     * from a Calendar.
     *
     * @param locale the new Locale.
     */
    public void setLocale(Locale locale);

}
