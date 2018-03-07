/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf;

import java.beans.PropertyVetoException;
import java.util.Date;

import javax.swing.plaf.ComponentUI;

/**
 * The ComponentUI for a JXDatePicker.
 * <p>
 *
 * Responsible for keeping the date property of all participants synchronized at
 * all "stable" points in their life-cycle. That is the following invariant is
 * guaranteed:
 *
 * <pre><code>
 * Date selected = datePicker.getMonthView().getSelectedDate();
 * assertEquals(selected, datePicker.getDate());
 * assertEquals(selected, datePicker.getEditor().getValue());
 * </code></pre>
 *
 * @author Joshua Outwater
 * @author Jeanette Winzenburg
 */
public abstract class DatePickerUI extends ComponentUI {
    /**
     * Get the baseline for the specified component, or a value less
     * than 0 if the baseline can not be determined.  The baseline is measured
     * from the top of the component.
     *
     * @param width  Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    public int getBaseline(int width, int height) {
        return -1;
    }

    /**
     * Checks the given date for validity for selection. If valid,
     * returns the date as appropriate in the picker's context, otherwise
     * throws a propertyVetoException. Note that the returned date might
     * be different from the input date, f.i. the time fields might be
     * cleared. The input date is guaranteed to be unchanged.
     *
     * @param date date to check
     * @return the date as allowed in the context of the picker.
     *
     * @throws PropertyVetoException if the given date is not valid for
     *    selection
     */
    public abstract Date getSelectableDate(Date date) throws PropertyVetoException;

}
