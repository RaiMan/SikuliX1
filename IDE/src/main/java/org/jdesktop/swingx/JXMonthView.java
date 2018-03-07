/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.calendar.CalendarUtils;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jdesktop.swingx.calendar.DaySelectionModel;
import org.jdesktop.swingx.calendar.DateSelectionModel.SelectionMode;
import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;
import org.jdesktop.swingx.event.EventListenerMap;
import org.jdesktop.swingx.event.DateSelectionEvent.EventType;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.MonthViewAddon;
import org.jdesktop.swingx.plaf.MonthViewUI;
import org.jdesktop.swingx.util.Contract;

/**
 * Component that displays a month calendar which can be used to select a day
 * or range of days.  By default the <code>JXMonthView</code> will display a
 * single calendar using the current month and year, using
 * <code>Calendar.SUNDAY</code> as the first day of the week.
 * <p>
 * The <code>JXMonthView</code> can be configured to display more than one
 * calendar at a time by calling
 * <code>setPreferredCalCols</code>/<code>setPreferredCalRows</code>.  These
 * methods will set the preferred number of calendars to use in each
 * column/row.  As these values change, the <code>Dimension</code> returned
 * from <code>getMinimumSize</code> and <code>getPreferredSize</code> will
 * be updated.  The following example shows how to create a 2x2 view which is
 * contained within a <code>JFrame</code>:
 * <pre>
 *     JXMonthView monthView = new JXMonthView();
 *     monthView.setPreferredCols(2);
 *     monthView.setPreferredRows(2);
 *
 *     JFrame frame = new JFrame();
 *     frame.getContentPane().add(monthView);
 *     frame.pack();
 *     frame.setVisible(true);
 * </pre>
 * <p>
 * <code>JXMonthView</code> can be further configured to allow any day of the
 * week to be considered the first day of the week.  Character
 * representation of those days may also be set by providing an array of
 * strings.
 * <pre>
 *    monthView.setFirstDayOfWeek(Calendar.MONDAY);
 *    monthView.setDaysOfTheWeek(
 *            new String[]{"S", "M", "T", "W", "Th", "F", "S"});
 * </pre>
 * <p>
 * This component supports flagging days.  These flagged days are displayed
 * in a bold font.  This can be used to inform the user of such things as
 * scheduled appointment.
 * <pre><code>
 *    // Create some dates that we want to flag as being important.
 *    Calendar cal1 = Calendar.getInstance();
 *    cal1.set(2004, 1, 1);
 *    Calendar cal2 = Calendar.getInstance();
 *    cal2.set(2004, 1, 5);
 *
 *    monthView.setFlaggedDates(cal1.getTime(), cal2.getTime(), new Date());
 * </code></pre>
 * Applications may have the need to allow users to select different ranges of
 * dates.  There are three modes of selection that are supported, single, single interval
 * and multiple interval selection.  Once a selection is made a DateSelectionEvent is
 * fired to inform listeners of the change.
 * <pre>
 *    // Change the selection mode to select full weeks.
 *    monthView.setSelectionMode(SelectionMode.SINGLE_INTERVAL_SELECTION);
 *
 *    // Register a date selection listener to get notified about
 *    // any changes in the date selection model.
 *    monthView.getSelectionModel().addDateSelectionListener(new DateSelectionListener {
 *        public void valueChanged(DateSelectionEvent e) {
 *            log.info(e.getSelection());
 *        }
 *    });
 * </pre>
 *
 * NOTE (for users of earlier versions): as of version 1.19 control about selection
 * dates is moved completely into the model. The default model used is of type
 * DaySelectionModel, which handles dates in the same way the JXMonthView did earlier
 * (that is, normalize all to the start of the day, which means zeroing all time
 * fields).<p>
 *
 * @author Joshua Outwater
 * @author Jeanette Winzenburg
 * @version  $Revision: 4147 $
 */
@JavaBean
public class JXMonthView extends JComponent {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(JXMonthView.class
            .getName());
    /*
     * moved from package calendar to swingx at version 1.51
     */

    /** action command used for commit actionEvent. */
    public static final String COMMIT_KEY = "monthViewCommit";
    /** action command used for cancel actionEvent. */
    public static final String CANCEL_KEY = "monthViewCancel";

    public static final String BOX_PADDING_X = "boxPaddingX";
    public static final String BOX_PADDING_Y = "boxPaddingY";
    public static final String DAYS_OF_THE_WEEK = "daysOfTheWeek";
    public static final String SELECTION_MODEL = "selectionModel";
    public static final String TRAVERSABLE = "traversable";
    public static final String FLAGGED_DATES = "flaggedDates";

    static {
        LookAndFeelAddons.contribute(new MonthViewAddon());
    }

     /**
     * UI Class ID
     */
    public static final String uiClassID = "MonthViewUI";

    public static final int DAYS_IN_WEEK = 7;
    public static final int MONTHS_IN_YEAR = 12;

    /**
     * Keeps track of the first date we are displaying.  We use this as a
     * restore point for the calendar. This is normalized to the start of the
     * first day of the month given in setFirstDisplayedDate.
     */
    private Date firstDisplayedDay;
    /**
     * the calendar to base all selections, flagging upon.
     * NOTE: the time of this calendar is undefined - before using, internal
     * code must explicitly set it.
     * PENDING JW: as of version 1.26 all calendar/properties are controlled by the model.
     * We keep a clone of the model's calendar here for notification reasons:
     * model fires DateSelectionEvent of type CALENDAR_CHANGED which neiter carry the
     * oldvalue nor the property name needed to map into propertyChange notification.
     */
    private Calendar cal;
    /** calendar to store the real input of firstDisplayedDate. */
    private Calendar anchor;
    /**
     * Start of the day which contains System.millis() in the current calendar.
     * Kept in synch via a timer started in addNotify.
     */
    private Date today;
    /**
     * The timer used to keep today in synch with system time.
     */
    private Timer todayTimer;
    // PENDING JW: why kept apart from cal? Why writable? - shouldn't the calendar have complete
    // control?
    private int firstDayOfWeek;
    //-------------- selection/flagging
    /**
     * The DateSelectionModel driving this component. This model's calendar
     * is the reference for all dates.
     */
    private DateSelectionModel model;
    /**
     * Listener registered with the current model to keep Calendar dependent
     * state synched.
     */
    private DateSelectionListener modelListener;
    /**
     * The manager of the flagged dates. Note
     * that the type of this is an implementation detail.
     */
    private DaySelectionModel flaggedDates;
    /**
     * Storage of actionListeners registered with the monthView.
     */
    private EventListenerMap listenerMap;

    private boolean traversable;
    private boolean leadingDays;
    private boolean trailingDays;
    private boolean showWeekNumber;
    private boolean componentInputMapEnabled;

    //-------------------
    // PENDING JW: ??
    @SuppressWarnings({"FieldCanBeLocal"})
    protected Date modifiedStartDate;
    @SuppressWarnings({"FieldCanBeLocal"})
    protected Date modifiedEndDate;

    //------------- visuals

    /**
     * Localizable day column headers. Default typically installed by the uidelegate.
     */
    private String[] _daysOfTheWeek;

    protected Insets _monthStringInsets = new Insets(0, 0, 0, 0);
    private int boxPaddingX;
    private int boxPaddingY;
    private int minCalCols = 1;
    private int minCalRows = 1;
    private Color todayBackgroundColor;
    private Color monthStringBackground;
    private Color monthStringForeground;
    private Color daysOfTheWeekForeground;
    private Color selectedBackground;
    private Hashtable<Integer, Color> dayToColorTable = new Hashtable<Integer, Color>();
    private Color flaggedDayForeground;

    private Color selectedForeground;
    private boolean zoomable;

    /**
     * Create a new instance of the <code>JXMonthView</code> class using the
     * default Locale and the current system time as the first date to
     * display.
     */
    public JXMonthView() {
        this(null, null, null);
    }

    /**
     * Create a new instance of the <code>JXMonthView</code> class using the
     * default Locale and the current system time as the first date to
     * display.
     *
     * @param locale desired locale, if null the system default locale is used
     */
    public JXMonthView(final Locale locale) {
        this(null, null, locale);
    }

    /**
     * Create a new instance of the <code>JXMonthView</code> class using the
     * default Locale and the given time as the first date to
     * display.
     *
     * @param firstDisplayedDay a day of the first month to display; if null, the current
     *   System time is used.
     */
    public JXMonthView(Date firstDisplayedDay) {
        this(firstDisplayedDay, null, null);
    }

    /**
     * Create a new instance of the <code>JXMonthView</code> class using the
     * default Locale, the given time as the first date to
     * display and the given selection model.
     *
     * @param firstDisplayedDay a day of the first month to display; if null, the current
     *   System time is used.
     * @param model the selection model to use, if null a <code>DefaultSelectionModel</code> is
     *   created.
     */
    public JXMonthView(Date firstDisplayedDay, final DateSelectionModel model) {
        this(firstDisplayedDay, model, null);
    }

    /**
     * Create a new instance of the <code>JXMonthView</code> class using the
     * given Locale, the given time as the first date to
     * display and the given selection model.
     *
     * @param firstDisplayedDay a day of the first month to display; if null, the current
     *   System time is used.
     * @param model the selection model to use, if null a <code>DefaultSelectionModel</code> is
     *   created.
     * @param locale desired locale, if null the system default locale is used
     */
    public JXMonthView(Date firstDisplayedDay, final DateSelectionModel model, final Locale locale) {
        super();
        listenerMap = new EventListenerMap();

        initModel(model, locale);
        superSetLocale(locale);
        setFirstDisplayedDay(firstDisplayedDay != null ? firstDisplayedDay : getCurrentDate());
        // Keep track of today
        updateTodayFromCurrentTime();

        // install the controller
        updateUI();

        setFocusable(true);
        todayBackgroundColor = getForeground();

    }

//------------------ Calendar related properties

    /**
     * Sets locale and resets text and format used to display months and days.
     * Also resets firstDayOfWeek. <p>
     *
     * PENDING JW: the following warning should be obsolete (installCalendar
     * should take care) - check if it really is!
     *
     * <p>
     * <b>Warning:</b> Since this resets any string labels that are cached in UI
     * (month and day names) and firstDayofWeek, use <code>setDaysOfTheWeek</code> and/or
     * setFirstDayOfWeek after (re)setting locale.
     * </p>
     *
     * @param   locale new Locale to be used for formatting
     * @see     #setDaysOfTheWeek(String[])
     * @see     #setFirstDayOfWeek(int)
     */
    @Override
    public void setLocale(Locale locale) {
        model.setLocale(locale);
    }

    /**
     *
     * @param locale
     */
    private void superSetLocale(Locale locale) {
        // PENDING JW: formally, a null value is allowed and must be passed on to super
        // I suspect this is not done here to keep the logic out off the constructor?
        //
        if (locale != null) {
            super.setLocale(locale);
            repaint();
       }
    }

    /**
     * Returns a clone of the internal calendar, with it's time set to firstDisplayedDate.
     *
     * PENDING: firstDisplayed useful as reference time? It's timezone dependent anyway.
     * Think: could return with monthView's today instead?
     *
     * @return a clone of internal calendar, configured to the current firstDisplayedDate
     * @throws IllegalStateException if called before instantitation is completed
     */
    public Calendar getCalendar() {
        // JW: this is to guard against a regression of not-fully understood
        // problems in constructor (UI used to call back into this before we were ready)
        if (cal == null) throw
            new IllegalStateException("must not be called before instantiation is complete");
        Calendar calendar = (Calendar) cal.clone();
        calendar.setTime(firstDisplayedDay);
        return calendar;
    }

    /**
     * Gets the time zone.
     *
     * @return The <code>TimeZone</code> used by the <code>JXMonthView</code>.
     */
    public TimeZone getTimeZone() {
        // PENDING JW: looks fishy (left-over?) .. why not ask the model?
        return cal.getTimeZone();
    }

    /**
     * Sets the time zone with the given time zone value.
     *
     * This is a bound property.
     *
     * @param tz The <code>TimeZone</code>.
     */
    public void setTimeZone(TimeZone tz) {
        model.setTimeZone(tz);
    }

    /**
     * Gets what the first day of the week is; e.g.,
     * <code>Calendar.SUNDAY</code> in the U.S., <code>Calendar.MONDAY</code>
     * in France.
     *
     * @return int The first day of the week.
     */
    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Sets what the first day of the week is; e.g.,
     * <code>Calendar.SUNDAY</code> in US, <code>Calendar.MONDAY</code>
     * in France.
     *
     * @param firstDayOfWeek The first day of the week.
     * @see java.util.Calendar
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        getSelectionModel().setFirstDayOfWeek(firstDayOfWeek);
    }


//---------------------- synch to model's calendar

    /**
     * Initializes selection model related internals. If the Locale is
     * null, it falls back to JComponent.defaultLocale. If the model
     * is null it creates a default model with the locale.
     *
     * PENDING JW: leave default locale fallback to model?
     *
     * @param model the DateSelectionModel which should drive the monthView.
     *    If null, a default model is created and initialized with the given locale.
     * @param locale the Locale to use with the selectionModel. If null,
     *   JComponent.getDefaultLocale is used.
     */
    private void initModel(DateSelectionModel model, Locale locale) {
        if (locale == null) {
            locale = JComponent.getDefaultLocale();
        }
        if (model == null) {
            model = new DaySelectionModel(locale);
        }
        this.model = model;
        // PENDING JW: do better to synchronize Calendar related
        // properties of flaggedDates to those of the selection model.
        // plus: should use the same normalization?
        this.flaggedDates = new DaySelectionModel(locale);
        flaggedDates.setSelectionMode(SelectionMode.MULTIPLE_INTERVAL_SELECTION);

        installCalendar();
        model.addDateSelectionListener(getDateSelectionListener());
    }

    /**
     * Lazily creates and returns the DateSelectionListener which listens
     * for model's calendar properties.
     *
     * @return a DateSelectionListener for model's CALENDAR_CHANGED notification.
     */
    private DateSelectionListener getDateSelectionListener() {
        if (modelListener == null) {
            modelListener = new DateSelectionListener() {

                @Override
                public void valueChanged(DateSelectionEvent ev) {
                    if (EventType.CALENDAR_CHANGED.equals(ev.getEventType())) {
                        updateCalendar();
                    }

                }

            };
        }
        return modelListener;
    }

    /**
     * Installs the internal calendars from the selection model.<p>
     *
     * PENDING JW: in fixing #11433, added update of firstDisplayedDay and
     * today here - check if correct place to do so.
     *
     */
    private void installCalendar() {
        cal = model.getCalendar();
        firstDayOfWeek = cal.getFirstDayOfWeek();
        Date anchorDate = getAnchorDate();
        anchor = (Calendar) cal.clone();
        if (anchorDate != null) {
            setFirstDisplayedDay(anchorDate);
        }
        updateTodayFromCurrentTime();
    }

    /**
     * Returns the anchor date. Currently, this is the "uncleaned" input date
     * of setFirstDisplayedDate. This is a quick hack for Issue #618-swingx, to
     * have some invariant for testing. Do not use in client code, may change
     * without notice!
     *
     * @return the "uncleaned" first display date or null if the firstDisplayedDay
     *   is not yet set.
     */
    protected Date getAnchorDate() {
        return anchor != null ? anchor.getTime() : null;
    }

    /**
     * Callback from selection model calendar changes.
     */
    private void updateCalendar() {
       if (!getLocale().equals(model.getLocale())) {
           installCalendar();
           superSetLocale(model.getLocale());
       } else {
           if (!model.getTimeZone().equals(getTimeZone())) {
               updateTimeZone();
           }
           if (cal.getMinimalDaysInFirstWeek() != model.getMinimalDaysInFirstWeek()) {
               updateMinimalDaysOfFirstWeek();
           }
           if (cal.getFirstDayOfWeek() != model.getFirstDayOfWeek()) {
              updateFirstDayOfWeek();
           }
       }
    }

    /**
     * Callback from changing timezone in model.
     */
    private void updateTimeZone() {
        TimeZone old = getTimeZone();
        TimeZone tz = model.getTimeZone();
        cal.setTimeZone(tz);
        anchor.setTimeZone(tz);
        setFirstDisplayedDay(anchor.getTime());
        updateTodayFromCurrentTime();
        updateDatesAfterTimeZoneChange(old);
        firePropertyChange("timeZone", old, getTimeZone());
    }

    /**
     * All dates are "cleaned" relative to the timezone they had been set.
     * After changing the timezone, they need to be updated to the new.
     *
     * Here: clear everything.
     *
     * @param oldTimeZone the timezone before the change
     */
    protected void updateDatesAfterTimeZoneChange(TimeZone oldTimeZone) {
        SortedSet<Date> flagged = getFlaggedDates();
        flaggedDates.setTimeZone(getTimeZone());
        firePropertyChange("flaggedDates", flagged, getFlaggedDates());
     }

    /**
     * Call back from listening to model firstDayOfWeek change.
     */
    private void updateFirstDayOfWeek() {
        int oldFirstDayOfWeek = this.firstDayOfWeek;

        firstDayOfWeek = getSelectionModel().getFirstDayOfWeek();
        cal.setFirstDayOfWeek(firstDayOfWeek);
        anchor.setFirstDayOfWeek(firstDayOfWeek);
        firePropertyChange("firstDayOfWeek", oldFirstDayOfWeek, firstDayOfWeek);
    }

    /**
     * Call back from listening to model minimalDaysOfFirstWeek change.
     * <p>
     * NOTE: this is not a property as we have no public api to change
     * it on JXMonthView.
     */
    private void updateMinimalDaysOfFirstWeek() {
        cal.setMinimalDaysInFirstWeek(model.getMinimalDaysInFirstWeek());
        anchor.setMinimalDaysInFirstWeek(model.getMinimalDaysInFirstWeek());
    }


//-------------------- scrolling
    /**
     * Returns the last date able to be displayed.  For example, if the last
     * visible month was April the time returned would be April 30, 23:59:59.
     *
     * @return long The last displayed date.
     */
    public Date getLastDisplayedDay() {
        return getUI().getLastDisplayedDay();
    }

    /**
     * Returns the first displayed date.
     *
     * @return long The first displayed date.
     */
    public Date getFirstDisplayedDay() {
        return firstDisplayedDay;
    }

    /**
     * Set the first displayed date.  We only use the month and year of
     * this date.  The <code>Calendar.DAY_OF_MONTH</code> field is reset to
     * 1 and all other fields, with exception of the year and month,
     * are reset to 0.
     *
     * @param date The first displayed date.
     */
    public void setFirstDisplayedDay(Date date) {
        anchor.setTime(date);
        Date oldDate = getFirstDisplayedDay();

        cal.setTime(anchor.getTime());
        CalendarUtils.startOfMonth(cal);
        firstDisplayedDay = cal.getTime();
        firePropertyChange("firstDisplayedDay", oldDate, getFirstDisplayedDay() );
    }

    /**
     * Moves the <code>date</code> into the visible region of the calendar. If
     * the date is greater than the last visible date it will become the last
     * visible date. While if it is less than the first visible date it will
     * become the first visible date. <p>
     *
     * NOTE: this is the recommended method to scroll to a particular date, the
     * functionally equivalent method taking a long as parameter will most
     * probably be deprecated.
     *
     * @param date Date to make visible, must not be null.
     * @see #ensureDateVisible(Date)
     */
    public void ensureDateVisible(Date date) {
        if (date.before(firstDisplayedDay)) {
            setFirstDisplayedDay(date);
        } else {
            Date lastDisplayedDate = getLastDisplayedDay();
            if (date.after(lastDisplayedDate)) {
                // extract to CalendarUtils!
                cal.setTime(date);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);

                cal.setTime(lastDisplayedDate);
                int lastMonth = cal.get(Calendar.MONTH);
                int lastYear = cal.get(Calendar.YEAR);

                int diffMonths = month - lastMonth
                        + ((year - lastYear) * MONTHS_IN_YEAR);

                cal.setTime(firstDisplayedDay);
                cal.add(Calendar.MONTH, diffMonths);
                setFirstDisplayedDay(cal.getTime());
            }
        }
    }

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
    public Date getDayAtLocation(int x, int y) {
        return getUI().getDayAtLocation(x, y);
    }

//------------------ today

    /**
     * Returns the current Date (whateverthatmeans). Internally always invoked when
     * the current default is needed. Introduced mainly for testing, don't override!
     *
     * This implementation returns a Date instantiated with <code>System.currentTimeInMillis</code>.
     *
     * @return the date deemed as current.
     */
    Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * Sets today from the current system time.
     *
     * temporary widened access for testing.
     */
    protected void updateTodayFromCurrentTime() {
        setToday(getCurrentDate());
    }

    /**
     * Increments today. This is used by the timer.
     *
     * PENDING: is it safe? doesn't check if we are really tomorrow?
     * temporary widened access for testing.
     */
    protected void incrementToday() {
        cal.setTime(getToday());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        setToday(cal.getTime());
    }

    /**
     * Sets the date which represents today. Internally
     * modified to the start of the day which contains the
     * given date in this monthView's calendar coordinates.
     *
     * temporary widened access for testing.
     *
     * @param date the date which should be used as today.
     */
    protected void setToday(Date date) {
        Date oldToday = getToday();
        // PENDING JW: do we really want the start of today?
        this.today = startOfDay(date);
        firePropertyChange("today", oldToday, getToday());
    }

    /**
     * Returns the start of today in this monthviews calendar coordinates.
     *
     * @return the start of today as Date.
     */
    public Date getToday() {
        // null only happens in the very first time ...
        return today != null ? (Date) today.clone() : null;
    }


//----   internal date manipulation ("cleanup" == start of day in monthView's calendar)

    /**
     * Returns the start of the day as Date.
     *
     * @param date the Date.
     * @return start of the given day as Date, relative to this
     *    monthView's calendar.
     *
     */
    private Date startOfDay(Date date) {
        return CalendarUtils.startOfDay(cal, date);
    }

//------------------- ui delegate
    /**
     * @inheritDoc
     */
    public MonthViewUI getUI() {
        return (MonthViewUI)ui;
    }

    /**
     * Sets the L&F object that renders this component.
     *
     * @param ui UI to use for this {@code JXMonthView}
     */
    public void setUI(MonthViewUI ui) {
        super.setUI(ui);
    }

    /**
     * Resets the UI property with the value from the current look and feel.
     *
     * @see UIManager#getUI(JComponent)
     */
    @Override
    public void updateUI() {
        setUI((MonthViewUI)LookAndFeelAddons.getUI(this, MonthViewUI.class));
        invalidate();
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

//---------------- DateSelectionModel

    /**
     * Returns the date selection model which drives this
     * JXMonthView.
     *
     * @return the date selection model
     */
    public DateSelectionModel getSelectionModel() {
        return model;
    }

    /**
     * Sets the date selection model to drive this monthView.
     *
     * @param model the selection model to use, must not be null.
     * @throws NullPointerException if model is null
     */
    public void setSelectionModel(DateSelectionModel model) {
        Contract.asNotNull(model, "date selection model must not be null");
        DateSelectionModel oldModel = getSelectionModel();
        model.removeDateSelectionListener(getDateSelectionListener());
        this.model = model;
        installCalendar();
        if (!model.getLocale().equals(getLocale())) {
            super.setLocale(model.getLocale());
        }
        model.addDateSelectionListener(getDateSelectionListener());
        firePropertyChange(SELECTION_MODEL, oldModel, getSelectionModel());
    }

//-------------------- delegates to model

    /**
     * Clear any selection from the selection model
     */
    public void clearSelection() {
        getSelectionModel().clearSelection();
    }

    /**
     * Return true if the selection is empty, false otherwise
     *
     * @return true if the selection is empty, false otherwise
     */
    public boolean isSelectionEmpty() {
        return getSelectionModel().isSelectionEmpty();
    }

    /**
     * Get the current selection
     *
     * @return sorted set of selected dates
     */
   public SortedSet<Date> getSelection() {
        return getSelectionModel().getSelection();
    }

    /**
     * Adds the selection interval to the selection model.
     *
     * @param startDate Start of date range to add to the selection
     * @param endDate End of date range to add to the selection
     */
    public void addSelectionInterval(Date startDate, Date endDate) {
            getSelectionModel().addSelectionInterval(startDate, endDate);
    }

    /**
     * Sets the selection interval to the selection model.
     *
     * @param startDate Start of date range to set the selection to
     * @param endDate End of date range to set the selection to
     */
    public void setSelectionInterval(final Date startDate, final Date endDate) {
            getSelectionModel().setSelectionInterval(startDate, endDate);
    }

    /**
     * Removes the selection interval from the selection model.
     *
     * @param startDate Start of the date range to remove from the selection
     * @param endDate End of the date range to remove from the selection
     */
    public void removeSelectionInterval(final Date startDate, final Date endDate) {
        getSelectionModel().removeSelectionInterval(startDate, endDate);
    }

    /**
     * Returns the current selection mode for this JXMonthView.
     *
     * @return int Selection mode.
     */
    public SelectionMode getSelectionMode() {
        return getSelectionModel().getSelectionMode();
    }

    /**
     * Set the selection mode for this JXMonthView.

     * @param selectionMode The selection mode to use for this {@code JXMonthView}
     */
    public void setSelectionMode(final SelectionMode selectionMode) {
        getSelectionModel().setSelectionMode(selectionMode);
    }

    /**
     * Returns the earliest selected date.
     *
     *
     * @return the first Date in the selection or null if empty.
     */
    public Date getFirstSelectionDate() {
        return getSelectionModel().getFirstSelectionDate();
     }

    /**
     * Returns the earliest selected date.
     *
     * @return the first Date in the selection or null if empty.
     */
    public Date getLastSelectionDate() {
        return getSelectionModel().getLastSelectionDate();
     }

    /**
     * Returns the earliest selected date.
     *
     * PENDING JW: keep this? it was introduced before the first/last
     *   in model. When delegating everything, we duplicate here.
     *
     * @return the first Date in the selection or null if empty.
     */
    public Date getSelectionDate() {
        return getFirstSelectionDate();
    }

    /**
     * Sets the model's selection to the given date or clears the selection if
     * null.
     *
     * @param newDate the selection date to set
     */
    public void setSelectionDate(Date newDate) {
        if (newDate == null) {
            clearSelection();
        } else {
            setSelectionInterval(newDate, newDate);
        }
    }

    /**
     * Returns true if the specified date falls within the _startSelectedDate
     * and _endSelectedDate range.
     *
     * @param date The date to check
     * @return true if the date is selected, false otherwise
     */
    public boolean isSelected(Date date) {
        return getSelectionModel().isSelected(date);
    }

    /**
     * Set the lower bound date that is allowed to be selected. <p>
     *
     *
     * @param lowerBound the lower bound, null means none.
     */
    public void setLowerBound(Date lowerBound) {
        getSelectionModel().setLowerBound(lowerBound);
    }

    /**
     * Set the upper bound date that is allowed to be selected. <p>
     *
     * @param upperBound the upper bound, null means none.
     */
    public void setUpperBound(Date upperBound) {
        getSelectionModel().setUpperBound(upperBound);
    }

    /**
     * Return the lower bound date that is allowed to be selected for this
     * model.
     *
     * @return lower bound date or null if not set
     */
    public Date getLowerBound() {
        return getSelectionModel().getLowerBound();
    }

    /**
     * Return the upper bound date that is allowed to be selected for this
     * model.
     *
     * @return upper bound date or null if not set
     */
    public Date getUpperBound() {
        return getSelectionModel().getUpperBound();
    }

    /**
     * Identifies whether or not the date passed is an unselectable date.
     * <p>
     *
     * @param date date which to test for unselectable status
     * @return true if the date is unselectable, false otherwise
     */
    public boolean isUnselectableDate(Date date) {
        return getSelectionModel().isUnselectableDate(date);
    }

    /**
     * Sets the dates that should be unselectable. This will replace the model's
     * current set of unselectable dates. The implication is that calling with
     * zero dates will remove all unselectable dates.
     * <p>
     *
     * NOTE: neither the given array nor any of its elements must be null.
     *
     * @param unselectableDates zero or more not-null dates that should be
     *        unselectable.
     * @throws NullPointerException if either the array or any of the elements
     *         are null
     */
    public void setUnselectableDates(Date... unselectableDates) {
        Contract.asNotNull(unselectableDates,
                "unselectable dates must not be null");
        SortedSet<Date> unselectableSet = new TreeSet<Date>();
        for (Date unselectableDate : unselectableDates) {
            unselectableSet.add(unselectableDate);
        }
        getSelectionModel().setUnselectableDates(unselectableSet);
        // PENDING JW: check that ui does the repaint!
        repaint();
    }

    // --------------------- flagged dates
    /**
     * Identifies whether or not the date passed is a flagged date.
     *
     * @param date date which to test for flagged status
     * @return true if the date is flagged, false otherwise
     */
    public boolean isFlaggedDate(Date date) {
        if (date == null)
            return false;
        return flaggedDates.isSelected(date);
    }

    /**
     * Replace all flags with the given dates.<p>
     *
     * NOTE: neither the given array nor any of its elements should be null.
     * Currently, a null array will be tolerated to ease migration. A null
     * has the same effect as clearFlaggedDates.
     *
     *
     * @param flagged the dates to be flagged
     */
    public void setFlaggedDates(Date... flagged) {
//        Contract.asNotNull(flagged, "must not be null");
        SortedSet<Date> oldFlagged = getFlaggedDates();
        flaggedDates.clearSelection();
        if (flagged != null) {
            for (Date date : flagged) {
                flaggedDates.addSelectionInterval(date, date);
            }
        }
        firePropertyChange("flaggedDates", oldFlagged, getFlaggedDates());
   }
    /**
     * Adds the dates to the flags.
     *
     * NOTE: neither the given array nor any of its elements should be null.
     * Currently, a null array will be tolerated to ease migration. A null
     * does nothing.
     *
     * @param flagged the dates to be flagged
     */
    public void addFlaggedDates(Date... flagged) {
//        Contract.asNotNull(flagged, "must not be null");
        SortedSet<Date> oldFlagged = flaggedDates.getSelection();
        if (flagged != null) {
            for (Date date : flagged) {
                flaggedDates.addSelectionInterval(date, date);
            }
        }
        firePropertyChange("flaggedDates", oldFlagged, flaggedDates.getSelection());
    }

    /**
     * Unflags the given dates.
     *
     * NOTE: neither the given array nor any of its elements should be null.
     * Currently, a null array will be tolerated to ease migration.
     *
     * @param flagged the dates to be unflagged
     */
    public void removeFlaggedDates(Date... flagged) {
//        Contract.asNotNull(flagged, "must not be null");
        SortedSet<Date> oldFlagged = flaggedDates.getSelection();
        if (flagged != null) {
            for (Date date : flagged) {
                flaggedDates.removeSelectionInterval(date, date);
            }
        }
        firePropertyChange("flaggedDates", oldFlagged, flaggedDates.getSelection());
    }
    /**
     * Clears all flagged dates.
     *
     */
    public void clearFlaggedDates() {
        SortedSet<Date> oldFlagged = flaggedDates.getSelection();
        flaggedDates.clearSelection();
        firePropertyChange("flaggedDates", oldFlagged, flaggedDates.getSelection());
    }

    /**
     * Returns a sorted set of flagged Dates. The returned set is guaranteed to
     * be not null, but may be empty.
     *
     * @return a sorted set of flagged dates.
     */
    public SortedSet<Date> getFlaggedDates() {
        return flaggedDates.getSelection();
    }

    /**
     * Returns a boolean indicating if this monthView has flagged dates.
     *
     * @return a boolean indicating if this monthView has flagged dates.
     */
    public boolean hasFlaggedDates() {
        return !flaggedDates.isSelectionEmpty();
    }

//------------------- visual properties
    /**
     * Sets a boolean property indicating whether or not to show leading dates
     * for a months displayed by this component.<p>
     *
     * The default value is false.
     *
     * @param value true if leading dates should be displayed, false otherwise.
     */
    public void setShowingLeadingDays(boolean value) {
        boolean old = isShowingLeadingDays();
        leadingDays = value;
        firePropertyChange("showingLeadingDays", old, isShowingLeadingDays());
    }

    /**
     * Returns a boolean indicating whether or not we're showing leading dates.
     *
     * @return true if leading dates are shown, false otherwise.
     */
    public boolean isShowingLeadingDays() {
        return leadingDays;
    }

    /**
     * Sets a boolean property indicating whether or not to show
     * trailing dates for the months displayed by this component.<p>
     *
     * The default value is false.
     *
     * @param value true if trailing dates should be displayed, false otherwise.
     */
    public void setShowingTrailingDays(boolean value) {
        boolean old = isShowingTrailingDays();
        trailingDays = value;
        firePropertyChange("showingTrailingDays", old, isShowingTrailingDays());
    }

    /**
     * Returns a boolean indicating whether or not we're showing trailing dates.
     *
     * @return true if trailing dates are shown, false otherwise.
     */
    public boolean isShowingTrailingDays() {
        return trailingDays;
    }

    /**
     * Returns whether or not the month view supports traversing months.
     * If zoomable is enabled, traversable is enabled as well. Otherwise
     * returns the traversable property as set by client code.
     *
     * @return <code>true</code> if month traversing is enabled.
     * @see #setZoomable(boolean)
     */
    public boolean isTraversable() {
        if (isZoomable()) return true;
        return traversable;
    }

    /**
     * Set whether or not the month view will display buttons to allow the user
     * to traverse to previous or next months. <p>
     *
     * The default value is false. <p>
     *
     * PENDING JW: fire the "real" property or the compound with zoomable?
     *
     * @param traversable set to true to enable month traversing, false
     *        otherwise.
     * @see #isTraversable()
     * @see #setZoomable(boolean)
     */
    public void setTraversable(boolean traversable) {
        boolean old = isTraversable();
        this.traversable = traversable;
        firePropertyChange(TRAVERSABLE, old, isTraversable());
    }

    /**
     * Returns true if zoomable (through date ranges).
     *
     * @return true if zoomable is enabled.
     * @see #setZoomable(boolean)
     */
    public boolean isZoomable() {
        return zoomable;
    }

    /**
     * Sets the zoomable property. If true, the calendar's date range can
     * be zoomed. This state implies that the calendar is traversable and
     * showing exactly one calendar box, effectively ignoring the properties.<p>
     *
     * <b>Note</b>: The actual zoomable behaviour is not yet implemented.
     *
     * @param zoomable a boolean indicating whether or not zooming date
     *    ranges is enabled.
     *
     * @see #setTraversable(boolean)
     */
    public void setZoomable(boolean zoomable) {
        boolean old = isZoomable();
        this.zoomable = zoomable;
        firePropertyChange("zoomable", old, isZoomable());
    }

    /**
     * Returns whether or not this <code>JXMonthView</code> should display
     * week number.
     *
     * @return <code>true</code> if week numbers should be displayed
     */
    public boolean isShowingWeekNumber() {
        return showWeekNumber;
    }

    /**
     * Set whether or not this <code>JXMonthView</code> will display week
     * numbers or not.
     *
     * @param showWeekNumber true if week numbers should be displayed,
     *        false otherwise
     */
    public void setShowingWeekNumber(boolean showWeekNumber) {
        boolean old = isShowingWeekNumber();
        this.showWeekNumber = showWeekNumber;
        firePropertyChange("showingWeekNumber", old, isShowingWeekNumber());
    }

    /**
     * Sets the String representation for each day of the week as used
     * in the header of the day's grid. For
     * this method the first days of the week days[0] is assumed to be
     * <code>Calendar.SUNDAY</code>. If null, the representation provided
     * by the MonthViewUI is used.
     *
     * The default value is the representation as
     * returned from the MonthViewUI.
     *
     * @param days Array of characters that represents each day
     * @throws IllegalArgumentException if not null and <code>days.length</code> !=
     *         DAYS_IN_WEEK
     */
    public void setDaysOfTheWeek(String[] days) {
        if ((days != null) && (days.length != DAYS_IN_WEEK)) {
            throw new IllegalArgumentException(
                    "Array of days is not of length " + DAYS_IN_WEEK
                            + " as expected.");
        }

        String[] oldValue = getDaysOfTheWeek();
        _daysOfTheWeek = days;
        firePropertyChange(DAYS_OF_THE_WEEK, oldValue, days);
    }

    /**
     * Returns the String representation for each day of the
     * week.
     *
     * @return String representation for the days of the week, guaranteed to
     *   never be null.
     *
     * @see #setDaysOfTheWeek(String[])
     * @see MonthViewUI
     */
    public String[] getDaysOfTheWeek() {
        if (_daysOfTheWeek != null) {
            String[] days = new String[DAYS_IN_WEEK];
            System.arraycopy(_daysOfTheWeek, 0, days, 0, DAYS_IN_WEEK);
            return days;
        }
        return getUI().getDaysOfTheWeek();
    }

    /**
     *
     * @param dayOfWeek
     * @return String representation of day of week.
     */
    public String getDayOfTheWeek(int dayOfWeek) {
        return getDaysOfTheWeek()[dayOfWeek - 1];
    }

    /**
     * Returns the padding used between days in the calendar.
     *
     * @return Padding used between days in the calendar
     */
    public int getBoxPaddingX() {
        return boxPaddingX;
    }

    /**
     * Sets the number of pixels used to pad the left and right side of a day.
     * The padding is applied to both sides of the days.  Therefore, if you
     * used the padding value of 3, the number of pixels between any two days
     * would be 6.
     *
     * @param boxPaddingX Number of pixels applied to both sides of a day
     */
    public void setBoxPaddingX(int boxPaddingX) {
        int oldBoxPadding = getBoxPaddingX();
        this.boxPaddingX = boxPaddingX;
        firePropertyChange(BOX_PADDING_X, oldBoxPadding, getBoxPaddingX());
    }

    /**
     * Returns the padding used above and below days in the calendar.
     *
     * @return Padding used between dats in the calendar
     */
    public int getBoxPaddingY() {
        return boxPaddingY;
    }

    /**
     * Sets the number of pixels used to pad the top and bottom of a day.
     * The padding is applied to both the top and bottom of a day.  Therefore,
     * if you used the padding value of 3, the number of pixels between any
     * two days would be 6.
     *
     * @param boxPaddingY Number of pixels applied to top and bottom of a day
     */
    public void setBoxPaddingY(int boxPaddingY) {
        int oldBoxPadding = getBoxPaddingY();
        this.boxPaddingY = boxPaddingY;
        firePropertyChange(BOX_PADDING_Y, oldBoxPadding, getBoxPaddingY());
    }

    /**
     * Returns the selected background color.
     *
     * @return the selected background color.
     */
    public Color getSelectionBackground() {
        return selectedBackground;
    }

    /**
     * Sets the selected background color to <code>c</code>.  The default color
     * is installed by the ui.
     *
     * @param c Selected background.
     */
    public void setSelectionBackground(Color c) {
        Color old = getSelectionBackground();
        selectedBackground = c;
        firePropertyChange("selectionBackground", old, getSelectionBackground());
    }

    /**
     * Returns the selected foreground color.
     *
     * @return the selected foreground color.
     */
    public Color getSelectionForeground() {
        return selectedForeground;
    }

    /**
     * Sets the selected foreground color to <code>c</code>.  The default color
     * is installed by the ui.
     *
     * @param c Selected foreground.
     */
    public void setSelectionForeground(Color c) {
        Color old = getSelectionForeground();
        selectedForeground = c;
        firePropertyChange("selectionForeground", old, getSelectionForeground());
    }

    /**
     * Returns the color used when painting the today background.
     *
     * @return Color Color
     */
    public Color getTodayBackground() {
        return todayBackgroundColor;
    }

    /**
     * Sets the color used to draw the bounding box around today.  The default
     * is the background of the <code>JXMonthView</code> component.
     *
     * @param c color to set
     */
    public void setTodayBackground(Color c) {
        Color oldValue = getTodayBackground();
        todayBackgroundColor = c;
        firePropertyChange("todayBackground", oldValue, getTodayBackground());
        // PENDING JW: remove repaint, ui must take care of it
        repaint();
    }

    /**
     * Returns the color used to paint the month string background.
     *
     * @return Color Color.
     */
    public Color getMonthStringBackground() {
        return monthStringBackground;
    }

    /**
     * Sets the color used to draw the background of the month string.  The
     * default is <code>138, 173, 209 (Blue-ish)</code>.
     *
     * @param c color to set
     */
    public void setMonthStringBackground(Color c) {
        Color old = getMonthStringBackground();
        monthStringBackground = c;
        firePropertyChange("monthStringBackground", old, getMonthStringBackground());
        // PENDING JW: remove repaint, ui must take care of it
        repaint();
    }

    /**
     * Returns the color used to paint the month string foreground.
     *
     * @return Color Color.
     */
    public Color getMonthStringForeground() {
        return monthStringForeground;
    }

    /**
     * Sets the color used to draw the foreground of the month string.  The
     * default is <code>Color.WHITE</code>.
     *
     * @param c color to set
     */
    public void setMonthStringForeground(Color c) {
        Color old = getMonthStringForeground();
        monthStringForeground = c;
        firePropertyChange("monthStringForeground", old, getMonthStringForeground());
        // PENDING JW: remove repaint, ui must take care of it
        repaint();
    }

    /**
     * Sets the color used to draw the foreground of each day of the week. These
     * are the titles
     *
     * @param c color to set
     */
    public void setDaysOfTheWeekForeground(Color c) {
        Color old = getDaysOfTheWeekForeground();
        daysOfTheWeekForeground = c;
        firePropertyChange("daysOfTheWeekForeground", old, getDaysOfTheWeekForeground());
    }

    /**
     * @return Color Color
     */
    public Color getDaysOfTheWeekForeground() {
        return daysOfTheWeekForeground;
    }

    /**
     * Set the color to be used for painting the specified day of the week.
     * Acceptable values are Calendar.SUNDAY - Calendar.SATURDAY. <p>
     *
     * PENDING JW: this is not a property - should it be and
     * fire a change notification? If so, how?
     *
     *
     * @param dayOfWeek constant value defining the day of the week.
     * @param c         The color to be used for painting the numeric day of the week.
     */
    public void setDayForeground(int dayOfWeek, Color c) {
        if ((dayOfWeek < Calendar.SUNDAY) || (dayOfWeek > Calendar.SATURDAY)) {
            throw new IllegalArgumentException("dayOfWeek must be in [Calendar.SUNDAY ... " +
            		"Calendar.SATURDAY] but was " + dayOfWeek);
        }
        dayToColorTable.put(dayOfWeek, c);
        repaint();
    }

    /**
     * Return the color that should be used for painting the numerical day of the week.
     *
     * @param dayOfWeek The day of week to get the color for.
     * @return The color to be used for painting the numeric day of the week.
     *         If this was no color has yet been defined the component foreground color
     *         will be returned.
     */
    public Color getDayForeground(int dayOfWeek) {
        Color c;
        c = dayToColorTable.get(dayOfWeek);
        if (c == null) {
            c = getForeground();
        }
        return c;
    }

    /**
     * Return the color that should be used for painting the numerical day of the week.
     *
     * @param dayOfWeek The day of week to get the color for.
     * @return The color to be used for painting the numeric day of the week or null
     *         If no color has yet been defined.
     */
    public Color getPerDayOfWeekForeground(int dayOfWeek) {
        return dayToColorTable.get(dayOfWeek);
    }
    /**
     * Set the color to be used for painting the foreground of a flagged day.
     *
     * @param c The color to be used for painting.
     */
    public void setFlaggedDayForeground(Color c) {
        Color old = getFlaggedDayForeground();
        flaggedDayForeground = c;
        firePropertyChange("flaggedDayForeground", old, getFlaggedDayForeground());
    }

    /**
     * Return the color that should be used for painting the foreground of the flagged day.
     *
     * @return The color to be used for painting
     */
    public Color getFlaggedDayForeground() {
        return flaggedDayForeground;
    }

    /**
     * Returns a copy of the insets used to paint the month string background.
     *
     * @return Insets Month string insets.
     */
    public Insets getMonthStringInsets() {
        return (Insets) _monthStringInsets.clone();
    }

    /**
     * Insets used to modify the width/height when painting the background
     * of the month string area.
     *
     * @param insets Insets
     */
    public void setMonthStringInsets(Insets insets) {
        Insets old = getMonthStringInsets();
        if (insets == null) {
            _monthStringInsets.top = 0;
            _monthStringInsets.left = 0;
            _monthStringInsets.bottom = 0;
            _monthStringInsets.right = 0;
        } else {
            _monthStringInsets.top = insets.top;
            _monthStringInsets.left = insets.left;
            _monthStringInsets.bottom = insets.bottom;
            _monthStringInsets.right = insets.right;
        }
        firePropertyChange("monthStringInsets", old, getMonthStringInsets());
        // PENDING JW: remove repaint, ui must take care of it
        repaint();
    }

    /**
     * Returns the preferred number of columns to paint calendars in.
     * <p>
     * @return int preferred number of columns of calendars.
     *
     * @see #setPreferredColumnCount(int)
     */
    public int getPreferredColumnCount() {
        return minCalCols;
    }

    /**
     * Sets the preferred number of columns of calendars. Does nothing if cols
     * <= 0. The default value is 1.
     * <p>
     * @param cols The number of columns of calendars.
     *
     * @see #getPreferredColumnCount()
     */
    public void setPreferredColumnCount(int cols) {
        if (cols <= 0) {
            return;
        }
        int old = getPreferredColumnCount();
        minCalCols = cols;
        firePropertyChange("preferredColumnCount", old, getPreferredColumnCount());
        // PENDING JW: remove revalidate/repaint, ui must take care of it
        revalidate();
        repaint();
    }

    /**
     * Returns the preferred number of rows to paint calendars in.
     * <p>
     * @return int Rows of calendars.
     *
     * @see #setPreferredRowCount(int)
     */
    public int getPreferredRowCount() {
        return minCalRows;
    }

    /**
     * Sets the preferred number of rows to paint calendars.Does nothing if rows
     * <= 0. The default value is 1.
     * <p>
     *
     * @param rows The number of rows of calendars.
     *
     * @see #getPreferredRowCount()
     */
    public void setPreferredRowCount(int rows) {
        if (rows <= 0) {
            return;
        }
        int old = getPreferredRowCount();
        minCalRows = rows;
        firePropertyChange("preferredRowCount", old, getPreferredRowCount());
        // PENDING JW: remove revalidate/repaint, ui must take care of it
        revalidate();
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotify() {
        if (todayTimer != null) {
            todayTimer.stop();
        }
        super.removeNotify();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNotify() {
        super.addNotify();
        // partial fix for #1125: today updated in addNotify
        // partial, because still not in synch if not shown
        updateTodayFromCurrentTime();
        // Setup timer to update the value of today.
        int secondsTillTomorrow = 86400;

        if (todayTimer == null) {
            todayTimer = new Timer(secondsTillTomorrow * 1000,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            incrementToday();
                        }
                    });
        }

        // Modify the initial delay by the current time.
//        cal.setTimeInMillis(System.currentTimeMillis());
        cal.setTime(getCurrentDate());
        secondsTillTomorrow = secondsTillTomorrow -
                (cal.get(Calendar.HOUR_OF_DAY) * 3600) -
                (cal.get(Calendar.MINUTE) * 60) -
                cal.get(Calendar.SECOND);
        todayTimer.setInitialDelay(secondsTillTomorrow * 1000);
        todayTimer.start();
    }

//-------------------- action and listener

    /**
     * Commits the current selection. <p>
     *
     * Resets the model's adjusting property to false
     * and fires an ActionEvent
     * with the COMMIT_KEY action command.
     *
     *
     * @see #cancelSelection()
     * @see org.jdesktop.swingx.calendar.DateSelectionModel#setAdjusting(boolean)
     */
    public void commitSelection() {
        getSelectionModel().setAdjusting(false);
        fireActionPerformed(COMMIT_KEY);
    }

    /**
     * Cancels the selection. <p>
     *
     * Resets the model's adjusting property to
     * false and fires an ActionEvent with the CANCEL_KEY action command.
     *
     * @see #commitSelection
     * @see org.jdesktop.swingx.calendar.DateSelectionModel#setAdjusting(boolean)
     */
    public void cancelSelection() {
        getSelectionModel().setAdjusting(false);
        fireActionPerformed(CANCEL_KEY);
    }

    /**
     * Sets the component input map enablement property.<p>
     *
     * If enabled, the keybinding for WHEN_IN_FOCUSED_WINDOW are
     * installed, otherwise not. Changing this property will
     * install/clear the corresponding key bindings. Typically, clients
     * which want to use the monthview in a popup, should enable these.<p>
     *
     * The default value is false.
     *
     * @param enabled boolean to indicate whether the component
     *   input map should be enabled.
     * @see #isComponentInputMapEnabled()
     */
    public void setComponentInputMapEnabled(boolean enabled) {
        boolean old = isComponentInputMapEnabled();
        this.componentInputMapEnabled = enabled;
        firePropertyChange("componentInputMapEnabled", old, isComponentInputMapEnabled());
    }

    /**
     * Returns the componentInputMapEnabled property.
     *
     * @return a boolean indicating whether the component input map is
     *   enabled.
     * @see #setComponentInputMapEnabled(boolean)
     *
     */
    public boolean isComponentInputMapEnabled() {
        return componentInputMapEnabled;
    }

    /**
     * Adds an ActionListener.
     * <p/>
     * The ActionListener will receive an ActionEvent with its actionCommand
     * set to COMMIT_KEY or CANCEL_KEY after the selection has been committed
     * or canceled, respectively.
     * <p>
     *
     * Note that actionEvents are typically fired after a dedicated user gesture
     * to end an ongoing selectin (like ENTER, ESCAPE) or after explicit programmatic
     * commits/cancels. It is usually not fired after each change to the selection state.
     * Client code which wants to be notified about all selection changes should
     * register a DateSelectionListener to the DateSelectionModel.
     *
     * @param l The ActionListener that is to be notified
     *
     * @see #commitSelection()
     * @see #cancelSelection()
     * @see #getSelectionModel()
     */
    public void addActionListener(ActionListener l) {
        listenerMap.add(ActionListener.class, l);
    }

    /**
     * Removes an ActionListener.
     *
     * @param l The ActionListener to remove.
     */
    public void removeActionListener(ActionListener l) {
        listenerMap.remove(ActionListener.class, l);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        java.util.List<T> listeners = listenerMap.getListeners(listenerType);
        T[] result;
        if (!listeners.isEmpty()) {
            //noinspection unchecked
            result = (T[]) java.lang.reflect.Array.newInstance(listenerType, listeners.size());
            result = listeners.toArray(result);
        } else {
            result = super.getListeners(listenerType);
        }
        return result;
    }

    /**
     * Creates and fires an ActionEvent with the given action
     * command to all listeners.
     *
     * @param actionCommand the command for the created.
     */
    protected void fireActionPerformed(String actionCommand) {
        ActionListener[] listeners = getListeners(ActionListener.class);
        ActionEvent e = null;

        for (ActionListener listener : listeners) {
            if (e == null) {
                e = new ActionEvent(JXMonthView.this,
                        ActionEvent.ACTION_PERFORMED,
                        actionCommand);
            }
            listener.actionPerformed(e);
        }
    }

//--- deprecated code - NOTE: these methods will be removed soon!

    /**
     * @deprecated pre-0.9.5 - this is kept as a reminder only, <b>don't
     *             use</b>! we can make this private or comment it out after
     *             next version
     */
     @Deprecated
    protected void cleanupWeekSelectionDates(Date startDate, Date endDate) {
        int count = 1;
        cal.setTime(startDate);
        while (cal.getTimeInMillis() < endDate.getTime()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            count++;
        }

        if (count > JXMonthView.DAYS_IN_WEEK) {
            // Move the start date to the first day of the week.
            cal.setTime(startDate);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int firstDayOfWeek = getFirstDayOfWeek();
            int daysFromStart = dayOfWeek - firstDayOfWeek;
            if (daysFromStart < 0) {
                daysFromStart += JXMonthView.DAYS_IN_WEEK;
            }
            cal.add(Calendar.DAY_OF_MONTH, -daysFromStart);

            modifiedStartDate = cal.getTime();

            // Move the end date to the last day of the week.
            cal.setTime(endDate);
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int lastDayOfWeek = firstDayOfWeek - 1;
            if (lastDayOfWeek == 0) {
                lastDayOfWeek = Calendar.SATURDAY;
            }
            int daysTillEnd = lastDayOfWeek - dayOfWeek;
            if (daysTillEnd < 0) {
                daysTillEnd += JXMonthView.DAYS_IN_WEEK;
            }
            cal.add(Calendar.DAY_OF_MONTH, daysTillEnd);
            modifiedEndDate = cal.getTime();
        }
    }





}
