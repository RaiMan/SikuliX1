/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.calendar;

import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdesktop.swingx.event.DateSelectionEvent.EventType;
import org.jdesktop.swingx.util.Contract;

/**
 * DateSelectionModel which allows a single selection only. <p>
 *
 * Temporary quick & dirty class to explore requirements as needed by
 * a DatePicker. Need to define the states more exactly. Currently
 *
 * <li> takes all Dates as-are (that is the normalized is the same as the given):
 * selected, unselectable, lower/upper bounds
 * <li> interprets any Date between the start/end of day of the selected as selected
 * <li> interprets any Date between the start/end of an unselectable date as unselectable
 * <li> interprets the lower/upper bounds as being the start/end of the given
 * dates, that is any Date after the start of day of the lower and before the end of
 * day of the upper is selectable.
 *
 *
 * @author Jeanette Winzenburg
 */
public class SingleDaySelectionModel extends AbstractDateSelectionModel {

    private SortedSet<Date> selectedDates;
    private SortedSet<Date> unselectableDates;

    /**
     * Instantiates a SingleDaySelectionModel with default locale.
     */
    public SingleDaySelectionModel() {
        this(null);
    }

    /**
     * Instantiates a SingleSelectionModel with the given locale. If the locale is
     * null, the Locale's default is used.
     *
     * PENDING JW: fall back to JComponent.getDefaultLocale instead? We use this
     *   with components anyway?
     *
     * @param locale the Locale to use with this model, defaults to Locale.default()
     *    if null.
     */
    public SingleDaySelectionModel(Locale locale) {
        super(locale);
        this.selectedDates = new TreeSet<Date>();
        this.unselectableDates = new TreeSet<Date>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE_SELECTION;
    }

    /**
     * {@inheritDoc}<p>
     *
     * Implemented to do nothing.
     *
     */
    @Override
    public void setSelectionMode(final SelectionMode selectionMode) {
    }

    //---------------------- selection ops
    /**
     * {@inheritDoc} <p>
     *
     * Implemented to call setSelectionInterval with startDate for both
     * parameters.
     */
    @Override
    public void addSelectionInterval(Date startDate, Date endDate) {
        setSelection(startDate);
    }

    /**
     * {@inheritDoc}<p>
     *
     * PENDING JW: define what happens if we have a selection but the interval
     * isn't selectable.
     */
    @Override
    public void setSelectionInterval(Date startDate, Date endDate) {
        setSelection(startDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelectionInterval(Date startDate, Date endDate) {
        Contract.asNotNull(startDate, "date must not be null");
        if (isSelectionEmpty()) return;
        if (isSelectionInInterval(startDate, endDate)) {
            selectedDates.clear();
            fireValueChanged(EventType.DATES_REMOVED);
        }
    }

    /**
     * Checks and returns whether the selected date is contained in the interval
     * given by startDate/endDate. The selection must not be empty when
     * calling this method. <p>
     *
     * This implementation interprets the interval between the start of the day
     * of startDay to the end of the day of endDate.
     *
     * @param startDate the start of the interval, must not be null
     * @param endDate  the end of the interval, must not be null
     * @return true if the selected date is contained in the interval
     */
    protected boolean isSelectionInInterval(Date startDate, Date endDate) {
        if (selectedDates.first().before(startOfDay(startDate))
                || (selectedDates.first().after(endOfDay(endDate)))) return false;
        return true;
    }

    /**
     * Selects the given date if it is selectable and not yet selected.
     * Does nothing otherwise.
     * If this operation changes the current selection, it will fire a
     * DateSelectionEvent of type DATES_SET.
     *
     * @param date the Date to select, must not be null.
     */
    protected void setSelection(Date date) {
        Contract.asNotNull(date, "date must not be null");
        if (isSelectedStrict(date)) return;
        if (isSelectable(date)) {
            selectedDates.clear();
            // PENDING JW: use normalized
            selectedDates.add(date);
            fireValueChanged(EventType.DATES_SET);
        }
    }

    /**
     * Checks and returns whether the given date is contained in the selection.
     * This differs from isSelected in that it tests for the exact (normalized)
     * Date instead of for the same day.
     *
     * @param date the Date to test.
     * @return true if the given date is contained in the selection,
     *    false otherwise
     *
     */
    private boolean isSelectedStrict(Date date) {
        if (!isSelectionEmpty()) {
            // PENDING JW: use normalized
            return selectedDates.first().equals(date);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getFirstSelectionDate() {
        return isSelectionEmpty() ? null : selectedDates.first();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLastSelectionDate() {
        return isSelectionEmpty() ? null : selectedDates.last();
    }

    /**
     * Returns a boolean indicating whether the given date is selectable.
     *
     * @param date the date to check for selectable, must not be null.
     * @return true if the given date is selectable, false if not.
     */
    public  boolean isSelectable(Date date) {
        if (outOfBounds(date)) return false;
        return !inUnselectables(date);
    }

    /**
     * @param date
     * @return
     */
    private boolean inUnselectables(Date date) {
        for (Date unselectable : unselectableDates) {
            if (isSameDay(unselectable, date)) return true;
        }
        return false;
    }

    /**
     * Returns a boolean indication whether the given date is below
     * the lower or above the upper bound.
     *
     * @param date
     * @return
     */
    private boolean outOfBounds(Date date) {
        if (belowLowerBound(date)) return true;
        if (aboveUpperBound(date)) return true;
        return false;
    }

    /**
     * @param date
     * @return
     */
    private boolean aboveUpperBound(Date date) {
        if (upperBound != null) {
            return endOfDay(upperBound).before(date);
        }
        return false;
    }

    /**
     * @param date
     * @return
     */
    private boolean belowLowerBound(Date date) {
        if (lowerBound != null) {
           return startOfDay(lowerBound).after(date);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearSelection() {
        if (isSelectionEmpty()) return;
        selectedDates.clear();
        fireValueChanged(EventType.SELECTION_CLEARED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getSelection() {
        return new TreeSet<Date>(selectedDates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelected(Date date) {
        Contract.asNotNull(date, "date must not be null");
        if (isSelectionEmpty()) return false;
        return isSameDay(selectedDates.first(), date);
    }


    /**
     * {@inheritDoc}<p>
     *
     * Implemented to return the date itself.
     */
    @Override
    public Date getNormalizedDate(Date date) {
        return new Date(date.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectionEmpty() {
        return selectedDates.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getUnselectableDates() {
        return new TreeSet<Date>(unselectableDates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnselectableDates(SortedSet<Date> unselectables) {
        Contract.asNotNull(unselectables, "unselectable dates must not be null");
        this.unselectableDates.clear();
        for (Date unselectableDate : unselectables) {
            removeSelectionInterval(unselectableDate, unselectableDate);
            unselectableDates.add(unselectableDate);
        }
        fireValueChanged(EventType.UNSELECTED_DATES_CHANGED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnselectableDate(Date date) {
        return !isSelectable(date);
    }





}
