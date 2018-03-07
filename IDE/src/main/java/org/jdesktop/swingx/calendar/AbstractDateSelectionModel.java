/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.calendar;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;
import org.jdesktop.swingx.event.EventListenerMap;
import org.jdesktop.swingx.event.DateSelectionEvent.EventType;

/**
 * Abstract base implementation of DateSelectionModel. Implements
 * notification, Calendar related properties and lower/upper bounds.
 *
 * @author Jeanette Winzenburg
 */
public abstract class AbstractDateSelectionModel implements DateSelectionModel {
    public static final SortedSet<Date> EMPTY_DATES = Collections.unmodifiableSortedSet(new TreeSet<Date>());

    protected EventListenerMap listenerMap;
    protected boolean adjusting;
    protected Calendar calendar;
    protected Date upperBound;
    protected Date lowerBound;

    /**
     * the locale used by the calendar. <p>
     * NOTE: need to keep separately as a Calendar has no getter.
     */
    protected Locale locale;

    /**
     * Instantiates a DateSelectionModel with default locale.
     */
    public AbstractDateSelectionModel() {
        this(null);
    }

    /**
     * Instantiates a DateSelectionModel with the given locale. If the locale is
     * null, the Locale's default is used.
     *
     * PENDING JW: fall back to JComponent.getDefaultLocale instead? We use this
     *   with components anyway?
     *
     * @param locale the Locale to use with this model, defaults to Locale.default()
     *    if null.
     */
    public AbstractDateSelectionModel(Locale locale) {
        this.listenerMap = new EventListenerMap();
        setLocale(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Calendar getCalendar() {
        return (Calendar) calendar.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFirstDayOfWeek() {
        return calendar.getFirstDayOfWeek();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstDayOfWeek(final int firstDayOfWeek) {
        if (firstDayOfWeek == getFirstDayOfWeek()) return;
        calendar.setFirstDayOfWeek(firstDayOfWeek);
        fireValueChanged(EventType.CALENDAR_CHANGED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinimalDaysInFirstWeek() {
        return calendar.getMinimalDaysInFirstWeek();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMinimalDaysInFirstWeek(int minimalDays) {
        if (minimalDays == getMinimalDaysInFirstWeek()) return;
        calendar.setMinimalDaysInFirstWeek(minimalDays);
        fireValueChanged(EventType.CALENDAR_CHANGED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone getTimeZone() {
        return calendar.getTimeZone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeZone(TimeZone timeZone) {
        if (getTimeZone().equals(timeZone)) return;
        TimeZone oldTimeZone = getTimeZone();
        calendar.setTimeZone(timeZone);
        adjustDatesToTimeZone(oldTimeZone);
        fireValueChanged(EventType.CALENDAR_CHANGED);
    }

    /**
     * Adjusts all stored dates to a new time zone.
     * This method is called after the change had been made. <p>
     *
     * This implementation resets all dates to null, clears everything.
     * Subclasses may override to really map to the new time zone.
     *
     * @param oldTimeZone the old time zone
     *
     */
    protected void adjustDatesToTimeZone(TimeZone oldTimeZone) {
        clearSelection();
        setLowerBound(null);
        setUpperBound(null);
        setUnselectableDates(EMPTY_DATES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        if (locale.equals(getLocale())) return;
        this.locale = locale;
        if (calendar != null) {
            calendar = Calendar.getInstance(calendar.getTimeZone(), locale);
        } else {
            calendar = Calendar.getInstance(locale);
        }
        fireValueChanged(EventType.CALENDAR_CHANGED);
    }

//------------------- utility methods

    /**
     * Returns the start of the day of the given date in this model's calendar.
     * NOTE: the calendar is changed by this operation.
     *
     * @param date the Date to get the start for.
     * @return the Date representing the start of the day of the input date.
     */
    protected Date startOfDay(Date date) {
        return CalendarUtils.startOfDay(calendar, date);
    }

    /**
     * Returns the end of the day of the given date in this model's calendar.
     * NOTE: the calendar is changed by this operation.
     *
     * @param date the Date to get the start for.
     * @return the Date representing the end of the day of the input date.
     */
    protected Date endOfDay(Date date) {
        return CalendarUtils.endOfDay(calendar, date);
    }

    /**
     * Returns a boolean indicating whether the given dates are on the same day in
     * the coordinates of the model's calendar.
     *
     * @param selected one of the dates to check, must not be null.
     * @param compare the other of the dates to check, must not be null.
     * @return true if both dates represent the same day in this model's calendar.
     */
    protected boolean isSameDay(Date selected, Date compare) {
        return startOfDay(selected).equals(startOfDay(compare));
    }

//------------------- bounds

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getUpperBound() {
        return upperBound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUpperBound(Date upperBound) {
        if (upperBound != null) {
            upperBound = getNormalizedDate(upperBound);
        }
        if (CalendarUtils.areEqual(upperBound, getUpperBound()))
            return;
        this.upperBound = upperBound;
        if (this.upperBound != null && !isSelectionEmpty()) {
            long justAboveUpperBoundMs = this.upperBound.getTime() + 1;
            removeSelectionInterval(new Date(justAboveUpperBoundMs),
                    getLastSelectionDate());
        }
        fireValueChanged(EventType.UPPER_BOUND_CHANGED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLowerBound() {
        return lowerBound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLowerBound(Date lowerBound) {
        if (lowerBound != null) {
            lowerBound = getNormalizedDate(lowerBound);
        }
        if (CalendarUtils.areEqual(lowerBound, getLowerBound()))
            return;
        this.lowerBound = lowerBound;
        if (this.lowerBound != null && !isSelectionEmpty()) {
            // Remove anything below the lower bound
            long justBelowLowerBoundMs = this.lowerBound.getTime() - 1;
            removeSelectionInterval(getFirstSelectionDate(), new Date(
                    justBelowLowerBoundMs));
        }
        fireValueChanged(EventType.LOWER_BOUND_CHANGED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAdjusting() {
        return adjusting;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdjusting(boolean adjusting) {
        if (adjusting == isAdjusting()) return;
        this.adjusting = adjusting;
       fireValueChanged(adjusting ? EventType.ADJUSTING_STARTED : EventType.ADJUSTING_STOPPED);

    }

//----------------- notification
    /**
     * {@inheritDoc}
     */
    @Override
    public void addDateSelectionListener(DateSelectionListener l) {
        listenerMap.add(DateSelectionListener.class, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDateSelectionListener(DateSelectionListener l) {
        listenerMap.remove(DateSelectionListener.class, l);
    }

    public List<DateSelectionListener> getDateSelectionListeners() {
        return listenerMap.getListeners(DateSelectionListener.class);
    }

    protected void fireValueChanged(DateSelectionEvent.EventType eventType) {
        List<DateSelectionListener> listeners = getDateSelectionListeners();
        DateSelectionEvent e = null;

        for (DateSelectionListener listener : listeners) {
            if (e == null) {
                e = new DateSelectionEvent(this, eventType, isAdjusting());
            }
            listener.valueChanged(e);
        }
    }


}
