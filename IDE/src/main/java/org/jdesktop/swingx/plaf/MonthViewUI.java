/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.util.Date;

import javax.swing.plaf.ComponentUI;

public abstract class MonthViewUI extends ComponentUI {

    /**
     * Returns an array of String to use as names for the days of the week.
     *
     * @return array of names for the days of the week.
     */
    public abstract String[] getDaysOfTheWeek();

    /**
     * Returns the Date at the given location. May be null if the
     * coordinates don't map to a day in the month which contains the
     * coordinates. Specifically: hitting leading/trailing dates returns null.
     *
     * Mapping pixel to calendar day.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the day at the given location or null if the location
     *   doesn't map to a day in the month which contains the coordinates.
     */
    public abstract Date getDayAtLocation(int x, int y);




    /**
     * Returns the last possible date that can be displayed.
     * This is implemented by the UI since it is in control of layout
     * and may possibly yeild different results based on implementation. <p>
     *
     * It's up to the UI to keep this property, based on internal state and
     * the firstDisplayed as controlled by the JXMonthView.
     *
     * @return Date The date.
     */
    public abstract Date getLastDisplayedDay();


}
