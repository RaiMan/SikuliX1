/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.calendar;

import java.util.Calendar;
import java.util.Date;

/**
 * Calendar manipulation.
 *
 * PENDING: replace by something tested - as is c&p'ed dateUtils
 * to work on a calendar instead of using long
 *
 * @author Jeanette Winzenburg
 */
public class CalendarUtils {

    // Constants used internally; unit is milliseconds
    @SuppressWarnings("unused")
    public static final int ONE_MINUTE = 60*1000;
    @SuppressWarnings("unused")
    public static final int ONE_HOUR   = 60*ONE_MINUTE;
    @SuppressWarnings("unused")
    public static final int THREE_HOURS = 3 * ONE_HOUR;
    @SuppressWarnings("unused")
    public static final int ONE_DAY    = 24*ONE_HOUR;

    public static final int DECADE = 5467;
    public static final int YEAR_IN_DECADE = DECADE + 1;

    /**
     * Increments the calendar field of the given calendar by amount.
     *
     * @param calendar
     * @param field the field to increment, allowed are all fields known to
     *   Calendar plus DECADE.
     * @param amount
     *
     * @throws IllegalArgumentException
     */
    public static void add(Calendar calendar, int field, int amount) {
        if (isNativeField(field)) {
            calendar.add(field, amount);
        } else {
            switch (field) {
            case DECADE:
                calendar.add(Calendar.YEAR, amount * 10);
                break;
            default:
                throw new IllegalArgumentException("unsupported field: " + field);
            }

        }
    }

    /**
     * Gets the calendar field of the given calendar by amount.
     *
     * @param calendar
     * @param field the field to get, allowed are all fields known to
     *   Calendar plus DECADE.
     *
     * @throws IllegalArgumentException
     */
    public static int get(Calendar calendar, int field) {
        if (isNativeField(field)) {
          return calendar.get(field);
        }
        switch (field) {
        case DECADE:
            return decade(calendar.get(Calendar.YEAR));
        case YEAR_IN_DECADE:
            return calendar.get(Calendar.YEAR) % 10;
        default:
            throw new IllegalArgumentException("unsupported field: " + field);
        }
    }

    /**
     * Sets the calendar field of the given calendar by amount. <p>
     *
     * NOTE: the custom field implementations are very naive (JSR-310 will do better)
     * - for decade: value must be positive, value must be a multiple of 10 and is interpreted as the
     *    first-year-of-the-decade
     * - for year-in-decade:  value is added/substracted to/from the start-of-decade of the
     *   date of the given calendar
     *
     * @param calendar
     * @param field the field to increment, allowed are all fields known to
     *   Calendar plus DECADE.
     * @param value the decade to set, must be a
     *
     * @throws IllegalArgumentException if the field is unsupported or the value is
     *    not dividable by 10 or negative.
     */
    public static void set(Calendar calendar, int field, int value) {
        if (isNativeField(field)) {
            calendar.set(field, value);
        } else {
            switch (field) {
            case DECADE:
                if(value <= 0 ) {
                    throw new IllegalArgumentException("value must be a positive but was: " + value);
                }
                if (value % 10 != 0) {
                    throw new IllegalArgumentException("value must be a multiple of 10 but was: " + value);
                }
                int yearInDecade = get(calendar, YEAR_IN_DECADE);
                calendar.set(Calendar.YEAR, value + yearInDecade);
                break;
            case YEAR_IN_DECADE:
                int decade = get(calendar, DECADE);
                calendar.set(Calendar.YEAR, value + decade);
                break;
            default:
                throw new IllegalArgumentException("unsupported field: " + field);
            }

        }
    }

    /**
     * @param calendarField
     * @return
     */
    private static boolean isNativeField(int calendarField) {
        return calendarField < DECADE;
    }

    /**
     * Adjusts the Calendar to the end of the day of the last day in DST in the
     * current year or unchanged if  not using DST. Returns the calendar's date or null, if not
     * using DST.<p>
     *
     *
     * @param calendar the calendar to adjust
     * @return the end of day of the last day in DST, or null if not using DST.
     */
    public static Date getEndOfDST(Calendar calendar) {
        if (!calendar.getTimeZone().useDaylightTime()) return null;
        long old = calendar.getTimeInMillis();
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        endOfMonth(calendar);
        startOfDay(calendar);
        for (int i = 0; i < 366; i++) {
           calendar.add(Calendar.DATE, -1);
           if (calendar.getTimeZone().inDaylightTime(calendar.getTime())) {
               endOfDay(calendar);
               return calendar.getTime();
           }
        }
        calendar.setTimeInMillis(old);
        return null;
    }

    /**
     * Adjusts the Calendar to the end of the day of the first day in DST in the
     * current year or unchanged if  not using DST. Returns the calendar's date or null, if not
     * using DST.<p>
     *
     * Note: the start of the day of the first day in DST is ill-defined!
     *
     * @param calendar the calendar to adjust
     * @return the start of day of the first day in DST, or null if not using DST.
     */
    public static Date getStartOfDST(Calendar calendar) {
        if (!calendar.getTimeZone().useDaylightTime()) return null;
        long old = calendar.getTimeInMillis();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        startOfMonth(calendar);
        endOfDay(calendar);
        for (int i = 0; i < 366; i++) {
           calendar.add(Calendar.DATE, 1);
           if (calendar.getTimeZone().inDaylightTime(calendar.getTime())) {
               endOfDay(calendar);
               return calendar.getTime();
           }
        }
        calendar.setTimeInMillis(old);
        return null;
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * start of a day (in the calendar's time zone). The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the start of the day,
     *   false otherwise.
     */
    public static boolean isStartOfDay(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, -1);
        return temp.get(Calendar.DATE) != calendar.get(Calendar.DATE);
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * end of a day (in the calendar's time zone). The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the end of the day,
     *   false otherwise.
     */
    public static boolean isEndOfDay(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, 1);
        return temp.get(Calendar.DATE) != calendar.get(Calendar.DATE);
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * start of a month (in the calendar's time zone). Returns true, if the time is
     * the start of the first day of the month, false otherwise. The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the start of the first day of the month,
     *   false otherwise.
     */
    public static boolean isStartOfMonth(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, -1);
        return temp.get(Calendar.MONTH) != calendar.get(Calendar.MONTH);
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * end of a month (in the calendar's time zone). Returns true, if the time is
     * the end of the last day of the month, false otherwise. The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the end of the last day of the month,
     *   false otherwise.
     */
    public static boolean isEndOfMonth(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, 1);
        return temp.get(Calendar.MONTH) != calendar.get(Calendar.MONTH);
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * start of a month (in the calendar's time zone). Returns true, if the time is
     * the start of the first day of the month, false otherwise. The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the start of the first day of the month,
     *   false otherwise.
     */
    public static boolean isStartOfWeek(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, -1);
        return temp.get(Calendar.WEEK_OF_YEAR) != calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * end of a week (in the calendar's time zone). Returns true, if the time is
     * the end of the last day of the week, false otherwise. The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the end of the last day of the week,
     *   false otherwise.
     */
    public static boolean isEndOfWeek(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, 1);
        return temp.get(Calendar.WEEK_OF_YEAR) != calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Adjusts the calendar to the start of the current week.
     * That is, first day of the week with all time fields cleared.
     * @param calendar the calendar to adjust.
     * @return the Date the calendar is set to
     */
    public static void startOfWeek(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        startOfDay(calendar);
    }

    /**
     * Adjusts the calendar to the end of the current week.
     * That is, last day of the week with all time fields at max.
     * @param calendar the calendar to adjust.
     */
    public static void endOfWeek(Calendar calendar) {
        startOfWeek(calendar);
        calendar.add(Calendar.DATE, 7);
        calendar.add(Calendar.MILLISECOND, -1);
    }

    /**
     * Adjusts the calendar to the end of the current week.
     * That is, last day of the week with all time fields at max.
     * The Date of the adjusted Calendar is
     * returned.
     *
     * @param calendar calendar to adjust.
     * @param date the Date to use.
     * @return the end of the week of the given date
     */
    public static Date endOfWeek(Calendar calendar, Date date) {
        calendar.setTime(date);
        endOfWeek(calendar);
        return calendar.getTime();
    }

    /**
     * Adjusts the calendar to the start of the current week.
     * That is, last day of the week with all time fields at max.
     * The Date of the adjusted Calendar is
     * returned.
     *
     * @param calendar calendar to adjust.
     * @param date the Date to use.
     * @return the start of the week of the given date
     */
    public static Date startOfWeek(Calendar calendar, Date date) {
        calendar.setTime(date);
        startOfWeek(calendar);
        return calendar.getTime();
    }

    /**
     * Adjusts the given Calendar to the start of the decade.
     *
     * @param calendar the calendar to adjust.
     */
    public static void startOfDecade(Calendar calendar) {
        calendar.set(Calendar.YEAR, decade(calendar.get(Calendar.YEAR)) );
        startOfYear(calendar);
    }

    /**
     * @param year
     * @return
     */
    private static int decade(int year) {
        return (year / 10) * 10;
    }

    /**
     * Adjusts the given Calendar to the start of the decade as defined by
     * the given date. Returns the calendar's Date.
     *
     * @param calendar calendar to adjust.
     * @param date the Date to use.
     * @return the start of the decade of the given date
     */
    public static Date startOfDecade(Calendar calendar, Date date) {
        calendar.setTime(date);
        startOfDecade(calendar);
        return calendar.getTime();
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * start of a decade (in the calendar's time zone). Returns true, if the time is
     * the start of the first day of the decade, false otherwise. The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the start of the first day of the month,
     *   false otherwise.
     */
    public static boolean isStartOfDecade(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, -1);
        return decade(temp.get(Calendar.YEAR)) != decade(calendar.get(Calendar.YEAR));
    }

    /**
     * Adjusts the given Calendar to the start of the year.
     *
     * @param calendar the calendar to adjust.
     */
    public static void startOfYear(Calendar calendar) {
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        startOfMonth(calendar);
    }

    /**
     * Adjusts the given Calendar to the start of the year as defined by
     * the given date. Returns the calendar's Date.
     *
     * @param calendar calendar to adjust.
     * @param date the Date to use.
     * @return the start of the year of the given date
     */
    public static Date startOfYear(Calendar calendar, Date date) {
        calendar.setTime(date);
        startOfYear(calendar);
        return calendar.getTime();
    }

    /**
     * Returns a boolean indicating if the given calendar represents the
     * start of a year (in the calendar's time zone). Returns true, if the time is
     * the start of the first day of the year, false otherwise. The calendar is unchanged.
     *
     * @param calendar the calendar to check.
     *
     * @return true if the calendar's time is the start of the first day of the month,
     *   false otherwise.
     */
    public static boolean isStartOfYear(Calendar calendar) {
        Calendar temp = (Calendar) calendar.clone();
        temp.add(Calendar.MILLISECOND, -1);
        return temp.get(Calendar.YEAR) != calendar.get(Calendar.YEAR);
    }

    /**
     * Adjusts the calendar to the start of the current month.
     * That is, first day of the month with all time fields cleared.
     *
     * @param calendar calendar to adjust.
     */
    public static void startOfMonth(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startOfDay(calendar);
    }

    /**
     * Adjusts the calendar to the end of the current month.
     * That is the last day of the month with all time-fields
     * at max.
     *
     * @param calendar calendar to adjust.
     */
    public static void endOfMonth(Calendar calendar) {
        // start of next month
        calendar.add(Calendar.MONTH, 1);
        startOfMonth(calendar);
        // one millisecond back
        calendar.add(Calendar.MILLISECOND, -1);
    }

    /**
     * Adjust the given calendar to the first millisecond of the given date.
     * that is all time fields cleared. The Date of the adjusted Calendar is
     * returned.
     *
     * @param calendar calendar to adjust.
     * @param date the Date to use.
     * @return the start of the day of the given date
     */
    public static Date startOfDay(Calendar calendar, Date date) {
        calendar.setTime(date);
        startOfDay(calendar);
        return calendar.getTime();
    }

    /**
     * Adjust the given calendar to the last millisecond of the given date.
     * that is all time fields cleared. The Date of the adjusted Calendar is
     * returned.
     *
     * @param calendar calendar to adjust.
     * @param date the Date to use.
     * @return the end of the day of the given date
     */
    public static Date endOfDay(Calendar calendar, Date date) {
        calendar.setTime(date);
        endOfDay(calendar);
        return calendar.getTime();
    }

    /**
     * Adjust the given calendar to the first millisecond of the current day.
     * that is all time fields cleared.
     *
     * @param calendar calendar to adjust.
     */
    public static void startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.getTimeInMillis();
    }

    /**
     * Adjust the given calendar to the last millisecond of the specified date.
     *
     * @param calendar calendar to adjust.
     */
    public static void endOfDay(Calendar calendar) {
        calendar.add(Calendar.DATE, 1);
        startOfDay(calendar);
        calendar.add(Calendar.MILLISECOND, -1);
    }

    /**
     * Adjusts the given calendar to the start of the period as indicated by the
     * given field. This delegates to startOfDay, -Week, -Month, -Year as appropriate.
     *
     * @param calendar
     * @param field the period to adjust, allowed are Calendar.DAY_OF_MONTH, -.MONTH,
     * -.WEEK and YEAR and CalendarUtils.DECADE.
     */
    public static void startOf(Calendar calendar, int field) {
        switch (field) {
        case Calendar.DAY_OF_MONTH:
            startOfDay(calendar);
            break;
        case Calendar.MONTH:
            startOfMonth(calendar);
            break;
        case Calendar.WEEK_OF_YEAR:
            startOfWeek(calendar);
            break;
        case Calendar.YEAR:
            startOfYear(calendar);
            break;
        case DECADE:
            startOfDecade(calendar);
            break;
        default:
            throw new IllegalArgumentException("unsupported field: " + field);

        }
    }

    /**
     * Returns a boolean indicating if the calendar is set to the start of a
     * period  as defined by the
     * given field. This delegates to startOfDay, -Week, -Month, -Year as appropriate.
     * The calendar is unchanged.
     *
     * @param calendar
     * @param field the period to adjust, allowed are Calendar.DAY_OF_MONTH, -.MONTH,
     * -.WEEK and YEAR and CalendarUtils.DECADE.
     * @throws IllegalArgumentException if the field is not supported.
     */
    public static boolean isStartOf(Calendar calendar, int field) {
        switch (field) {
            case Calendar.DAY_OF_MONTH:
                return isStartOfDay(calendar);
            case Calendar.MONTH:
                return isStartOfMonth(calendar);
            case Calendar.WEEK_OF_YEAR:
                return isStartOfWeek(calendar);
            case Calendar.YEAR:
                return isStartOfYear(calendar);
            case DECADE:
                return isStartOfDecade(calendar);
            default:
                throw new IllegalArgumentException("unsupported field: " + field);
            }
    }

    /**
     * Checks the given dates for being equal.
     *
     * @param current one of the dates to compare
     * @param date the otherr of the dates to compare
     * @return true if the two given dates both are null or both are not null and equal,
     *  false otherwise.
     */
    public static boolean areEqual(Date current, Date date) {
        if ((date == null) && (current == null)) {
            return true;
        }
        if (date != null) {
           return date.equals(current);
        }
        return false;
    }

    /**
     * Returns a boolean indicating whether the given Date is the same day as
     * the day in the calendar. Calendar and date are unchanged by the check.
     *
     * @param today the Calendar representing a date, must not be null.
     * @param now the date to compare to, must not be null
     * @return true if the calendar and date represent the same day in the
     *   given calendar.
     */
    public static boolean isSameDay(Calendar today, Date now) {
        Calendar temp = (Calendar) today.clone();
        startOfDay(temp);
        Date start = temp.getTime();
        temp.setTime(now);
        startOfDay(temp);
        return start.equals(temp.getTime());
    }

    /**
     * Returns a boolean indicating whether the given Date is in the same period as
     * the Date in the calendar, as defined by the calendar field.
     * Calendar and date are unchanged by the check.
     *
     * @param today the Calendar representing a date, must not be null.
     * @param now the date to compare to, must not be null
     * @return true if the calendar and date represent the same day in the
     *   given calendar.
     */
    public static boolean isSame(Calendar today, Date now, int field) {
        Calendar temp = (Calendar) today.clone();
        startOf(temp, field);
        Date start = temp.getTime();
        temp.setTime(now);
        startOf(temp, field);
        return start.equals(temp.getTime());
    }

    /**
     * Returns a boolean to indicate whether the given calendar is flushed. <p>
     *
     * The only way to guarantee a flushed state is to let client code call
     * getTime or getTimeInMillis. See
     *
     * <a href=http://forums.java.net/jive/thread.jspa?threadID=74472&tstart=0>Despairing
     * in Calendar</a>
     * <p>
     * Note: this if for testing only and not entirely safe!
     *
     * @param calendar
     * @return
     */
    public static boolean isFlushed(Calendar calendar) {
        return !calendar.toString().contains("time=?");
    }
}
