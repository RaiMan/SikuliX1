/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdesktop.swingx.event.EventListenerMap;
import org.jdesktop.swingx.event.DateSelectionEvent.EventType;
import org.jdesktop.swingx.util.Contract;

/**
 *
 * DaySelectionModel is a (temporary?) implementation of DateSelectionModel
 * which normalizes all dates to the start of the day, that is zeroes all
 * time fields. Responsibility extracted from JXMonthView (which must
 * respect rules of model instead of trying to be clever itself).
 *
 * @author Joshua Outwater
 */
public class DaySelectionModel extends AbstractDateSelectionModel {
    private SelectionMode selectionMode;
    private SortedSet<Date> selectedDates;
    private SortedSet<Date> unselectableDates;

    /**
     *
     */
    public DaySelectionModel() {
        this(null);
    }

    /**
     *
     */
    public DaySelectionModel(Locale locale) {
        super(locale);
        this.listenerMap = new EventListenerMap();
        this.selectionMode = SelectionMode.SINGLE_SELECTION;
        this.selectedDates = new TreeSet<Date>();
        this.unselectableDates = new TreeSet<Date>();

    }
    /**
     *
     */
    @Override
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    /**
     *
     */
    @Override
    public void setSelectionMode(final SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        clearSelection();
    }

    //---------------------- selection ops
    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelectionInterval(Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return;
        }
        startDate = startOfDay(startDate);
        endDate = startOfDay(endDate);
        boolean added = false;
        switch (selectionMode) {
            case SINGLE_SELECTION:
                if (isSelected(startDate)) return;
                clearSelectionImpl();
                added = addSelectionImpl(startDate, startDate);
                break;
            case SINGLE_INTERVAL_SELECTION:
                if (isIntervalSelected(startDate, endDate)) return;
                clearSelectionImpl();
                added = addSelectionImpl(startDate, endDate);
                break;
            case MULTIPLE_INTERVAL_SELECTION:
                if (isIntervalSelected(startDate, endDate)) return;
                added = addSelectionImpl(startDate, endDate);
                break;
            default:
                break;
        }
        if (added) {
            fireValueChanged(EventType.DATES_ADDED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectionInterval(Date startDate, Date endDate) {
        startDate = startOfDay(startDate);
        endDate = startOfDay(endDate);
        if (SelectionMode.SINGLE_SELECTION.equals(selectionMode)) {
           if (isSelected(startDate)) return;
           endDate = startDate;
        } else {
            if (isIntervalSelected(startDate, endDate)) return;
        }
        clearSelectionImpl();
        if (addSelectionImpl(startDate, endDate)) {
            fireValueChanged(EventType.DATES_SET);
        }
    }

    /**
     * Checks and returns if the single date interval bounded by startDate and endDate
     * is selected. This is useful only for SingleInterval mode.
     *
     * @param startDate the start of the interval
     * @param endDate the end of the interval, must be >= startDate
     * @return true the interval is selected, false otherwise.
     */
    private boolean isIntervalSelected(Date startDate, Date endDate) {
        if (isSelectionEmpty()) return false;
        startDate = startOfDay(startDate);
        endDate = startOfDay(endDate);
        return selectedDates.first().equals(startDate)
           && selectedDates.last().equals(endDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelectionInterval(Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            return;
        }

        startDate = startOfDay(startDate);
        endDate = startOfDay(endDate);
        long startDateMs = startDate.getTime();
        long endDateMs = endDate.getTime();
        ArrayList<Date> datesToRemove = new ArrayList<Date>();
        for (Date selectedDate : selectedDates) {
            long selectedDateMs = selectedDate.getTime();
            if (selectedDateMs >= startDateMs && selectedDateMs <= endDateMs) {
                datesToRemove.add(selectedDate);
            }
        }

        if (!datesToRemove.isEmpty()) {
            selectedDates.removeAll(datesToRemove);
            fireValueChanged(EventType.DATES_REMOVED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearSelection() {
        if (isSelectionEmpty()) return;
        clearSelectionImpl();
        fireValueChanged(EventType.SELECTION_CLEARED);
    }

    private void clearSelectionImpl() {
        selectedDates.clear();
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
     * {@inheritDoc}
     */
    @Override
    public boolean isSelected(Date date) {
        // JW: don't need Contract ... startOfDay will throw NPE if null
        return selectedDates.contains(startOfDay(date));
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
        this.unselectableDates.clear();
        for (Date date : unselectables) {
            unselectableDates.add(startOfDay(date));
        }
        for (Date unselectableDate : this.unselectableDates) {
            removeSelectionInterval(unselectableDate, unselectableDate);
        }
        fireValueChanged(EventType.UNSELECTED_DATES_CHANGED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnselectableDate(Date date) {
        date = startOfDay(date);
        return upperBound != null && upperBound.getTime() < date.getTime() ||
                lowerBound != null && lowerBound.getTime() > date.getTime() ||
                unselectableDates != null && unselectableDates.contains(date);
    }

    private boolean addSelectionImpl(final Date startDate, final Date endDate) {
        boolean hasAdded = false;
        calendar.setTime(startDate);
        Date date = calendar.getTime();
        while (date.before(endDate) || date.equals(endDate)) {
            if (!isUnselectableDate(date)) {
                hasAdded = true;
                selectedDates.add(date);
            }
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
        }
        return hasAdded;
    }

    /**
     * {@inheritDoc}
     */

    /**
     * {@inheritDoc} <p>
     *
     * Implemented to return the start of the day which contains the date.
     */
    @Override
    public Date getNormalizedDate(Date date) {
        Contract.asNotNull(date, "date must not be null");
        return startOfDay(date);
    }

}
