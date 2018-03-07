/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.calendar.CalendarUtils;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jdesktop.swingx.calendar.DateSelectionModel.SelectionMode;
import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;
import org.jdesktop.swingx.plaf.MonthViewUI;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 * Base implementation of the <code>JXMonthView</code> UI.<p>
 *
 * <b>Note</b>: The api changed considerably between releases 0.9.4 and 0.9.5.
 * <p>
 *
 * The general drift of the change was to delegate all text rendering to a dedicated
 * rendering controller (currently named RenderingHandler), similar to
 * the collection view rendering. The UI itself keeps layout and positioning of
 * the rendering components. Plus updating on property changes received from the
 * monthView. <p>
 *
 *
 * <p>
 * Painting: coordinate systems.
 *
 * <ul>
 * <li> Screen coordinates of months/days, accessible via the getXXBounds() methods. These
 * coordinates are absolute in the system of the monthView.
 * <li> The grid of visible months with logical row/column coordinates. The logical
 * coordinates are adjusted to ComponentOrientation.
 * <li> The grid of days in a month with logical row/column coordinates. The logical
 * coordinates are adjusted to ComponentOrientation. The columns
 * are the (optional) week header and the days of the week. The rows are the day header
 * and the weeks in a month. The day header shows  the localized names of the days and
 * has the row coordinate DAY_HEADER_ROW. It is shown always.
 * The row header shows the week number in the year and has the column coordinate WEEK_HEADER_COLUMN. It
 * is shown only if the showingWeekNumber property is true.
 * </ul>
 *
 * On the road to "zoomable" date range views (Vista-style).<p>
 *
 * Added support (doesn't do anything yet, zoom-logic must yet be defined)
 * by way of an active calendar header which is added to the monthView if zoomable.
 * It is disabled by default. In this mode, the view is always
 * traversable and shows exactly one calendar. It is orthogonal to the classic
 * mode, that is client code should not be effected in any way as long as the mode
 * is not explicitly enabled. <p>
 *
 * NOTE to LAF implementors: the active calendar header is very, very, very raw and
 * sure to change without much notice. Better not yet to support it right now.
 *
 * @author dmouse
 * @author rbair
 * @author rah003
 * @author Jeanette Winzenburg
 */
public class BasicMonthViewUI extends MonthViewUI {
    @SuppressWarnings("all")
    private static final Logger LOG = Logger.getLogger(BasicMonthViewUI.class
            .getName());

    private static final int CALENDAR_SPACING = 10;

    /** Return value used to identify when the month down button is pressed. */
    public static final int MONTH_DOWN = 1;
    /** Return value used to identify when the month up button is pressed. */
    public static final int MONTH_UP = 2;

    // constants for day columns
    protected static final int WEEK_HEADER_COLUMN = 0;
    protected static final int DAYS_IN_WEEK = 7;
    protected static final int FIRST_DAY_COLUMN = WEEK_HEADER_COLUMN + 1;
    protected static final int LAST_DAY_COLUMN = FIRST_DAY_COLUMN + DAYS_IN_WEEK -1;

    // constants for day rows (aka: weeks)
    protected static final int DAY_HEADER_ROW = 0;
    protected static final int WEEKS_IN_MONTH = 6;
    protected static final int FIRST_WEEK_ROW = DAY_HEADER_ROW + 1;
    protected static final int LAST_WEEK_ROW = FIRST_WEEK_ROW + WEEKS_IN_MONTH - 1;

    /** the component we are installed for. */
    protected JXMonthView monthView;
    // listeners
    private PropertyChangeListener propertyChangeListener;
    private MouseListener mouseListener;
    private MouseMotionListener mouseMotionListener;
    private Handler handler;

    // fields related to visible date range
    /** end of day of the last visible month. */
    private Date lastDisplayedDate;
    /**

    //---------- fields related to selection/navigation

    /** flag indicating keyboard navigation. */
    private boolean usingKeyboard = false;
    /** For interval selections we need to record the date we pivot around. */
    private Date pivotDate = null;
    /**
     * Date span used by the keyboard actions to track the original selection.
     */
    private SortedSet<Date> originalDateSpan;

    //------------------ visuals

    protected boolean isLeftToRight;
    protected Icon monthUpImage;
    protected Icon monthDownImage;

    /**
     * The padding for month traversal icons.
     * PENDING JW: decouple rendering and hit-detection.
     */
    private int arrowPaddingX = 3;
    private int arrowPaddingY = 3;

    /** height of month header including the monthView's box padding. */
    private int fullMonthBoxHeight;
    /**
     * width of a "day" box including the monthView's box padding
     * this is the same for days-of-the-week, weeks-of-the-year and days
     */
    private int fullBoxWidth;
    /**
     * height of a "day" box including the monthView's box padding
     * this is the same for days-of-the-week, weeks-of-the-year and days
     */
    private int fullBoxHeight;
    /** the width of a single month display. */
    private int calendarWidth;
    /** the height of a single month display. */
    private int calendarHeight;
    /** the height of a single month grid cell, including padding. */
    private int fullCalendarHeight;
    /** the width of a single month grid cell, including padding. */
    private int fullCalendarWidth;
    /** The number of calendars displayed vertically. */
    private int calendarRowCount = 1;
    /** The number of calendars displayed horizontally. */
    private int calendarColumnCount = 1;

    /**
     * The bounding box of the grid of visible months.
     */
    protected Rectangle calendarGrid = new Rectangle();

    /**
     * The Strings used for the day headers. This is the fall-back for
     * the monthView if no custom strings are set.
     * PENDING JW: delegate to RenderingHandler?
     */
    private String[] daysOfTheWeek;

    /**
     * Provider of configured components for text rendering.
     */
    private CalendarRenderingHandler renderingHandler;
    /**
     * The CellRendererPane for stamping rendering comps.
     */
    private CellRendererPane rendererPane;

    /**
     * The CalendarHeaderHandler which provides the header component if zoomable.
     */
    private CalendarHeaderHandler calendarHeaderHandler;

    @SuppressWarnings({"UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        return new BasicMonthViewUI();
    }

    /**
     * Installs the component as appropriate for the current lf.
     *
     * PENDING JW: clarify sequence of installXX methods.
     */
    @Override
    public void installUI(JComponent c) {
        monthView = (JXMonthView)c;
        monthView.setLayout(createLayoutManager());

        // PENDING JW: move to installDefaults or installComponents?
        installRenderingHandler();

        installDefaults();
        installDelegate();
        installKeyboardActions();
        installComponents();
        updateLocale(false);
        updateZoomable();
        installListeners();
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallRenderingHandler();
        uninstallListeners();
        uninstallKeyboardActions();
        uninstallDefaults();
        uninstallComponents();
        monthView.setLayout(null);
        monthView = null;
    }

    /**
     * Creates and installs the calendar header handler.
     */
    protected void installComponents() {
        setCalendarHeaderHandler(createCalendarHeaderHandler());
        getCalendarHeaderHandler().install(monthView);
    }

    /**
     * Uninstalls the calendar header handler.
     */
    protected void uninstallComponents() {
        getCalendarHeaderHandler().uninstall(monthView);
        setCalendarHeaderHandler(null);
    }

    /**
     * Installs default values. <p>
     *
     * This is refactored to only install default properties on the monthView.
     * Extracted install of this delegate's properties into installDelegate.
     *
     */
    protected void installDefaults() {
        LookAndFeel.installProperty(monthView, "opaque", Boolean.TRUE);

       // @KEEP JW: do not use the core install methods (might have classloader probs)
        // instead access all properties via the UIManagerExt ..
        //        BasicLookAndFeel.installColorsAndFont(monthView,
//                "JXMonthView.background", "JXMonthView.foreground", "JXMonthView.font");

        if (SwingXUtilities.isUIInstallable(monthView.getBackground())) {
            monthView.setBackground(UIManagerExt.getColor("JXMonthView.background"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getForeground())) {
            monthView.setForeground(UIManagerExt.getColor("JXMonthView.foreground"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getFont())) {
            // PENDING JW: missing in managerExt? Or not applicable anyway?
            monthView.setFont(UIManager.getFont("JXMonthView.font"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getMonthStringBackground())) {
            monthView.setMonthStringBackground(UIManagerExt.getColor("JXMonthView.monthStringBackground"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getMonthStringForeground())) {
            monthView.setMonthStringForeground(UIManagerExt.getColor("JXMonthView.monthStringForeground"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getDaysOfTheWeekForeground())) {
            monthView.setDaysOfTheWeekForeground(UIManagerExt.getColor("JXMonthView.daysOfTheWeekForeground"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getSelectionBackground())) {
            monthView.setSelectionBackground(UIManagerExt.getColor("JXMonthView.selectedBackground"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getSelectionForeground())) {
            monthView.setSelectionForeground(UIManagerExt.getColor("JXMonthView.selectedForeground"));
        }
        if (SwingXUtilities.isUIInstallable(monthView.getFlaggedDayForeground())) {
            monthView.setFlaggedDayForeground(UIManagerExt.getColor("JXMonthView.flaggedDayForeground"));
        }

        monthView.setBoxPaddingX(UIManagerExt.getInt("JXMonthView.boxPaddingX"));
        monthView.setBoxPaddingY(UIManagerExt.getInt("JXMonthView.boxPaddingY"));
    }

    /**
     * Installs this ui delegate's properties.
     */
    protected void installDelegate() {
        isLeftToRight = monthView.getComponentOrientation().isLeftToRight();
        // PENDING JW: remove here if rendererHandler takes over control completely
        // as is, some properties are duplicated
        monthDownImage = UIManager.getIcon("JXMonthView.monthDownFileName");
        monthUpImage = UIManager.getIcon("JXMonthView.monthUpFileName");
        // install date related state
        setFirstDisplayedDay(monthView.getFirstDisplayedDay());
    }

    protected void uninstallDefaults() {}

    protected void installKeyboardActions() {
        // Setup the keyboard handler.
        // JW: changed (0.9.6) to when-ancestor just to be on the safe side
        // if the title contain active comps
        installKeyBindings(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // JW: removed the automatic keybindings in WHEN_IN_FOCUSED
        // which caused #555-swingx (binding active if not focused)
        ActionMap actionMap = monthView.getActionMap();
        KeyboardAction acceptAction = new KeyboardAction(KeyboardAction.ACCEPT_SELECTION);
        actionMap.put("acceptSelection", acceptAction);
        KeyboardAction cancelAction = new KeyboardAction(KeyboardAction.CANCEL_SELECTION);
        actionMap.put("cancelSelection", cancelAction);

        actionMap.put("selectPreviousDay", new KeyboardAction(KeyboardAction.SELECT_PREVIOUS_DAY));
        actionMap.put("selectNextDay", new KeyboardAction(KeyboardAction.SELECT_NEXT_DAY));
        actionMap.put("selectDayInPreviousWeek", new KeyboardAction(KeyboardAction.SELECT_DAY_PREVIOUS_WEEK));
        actionMap.put("selectDayInNextWeek", new KeyboardAction(KeyboardAction.SELECT_DAY_NEXT_WEEK));

        actionMap.put("adjustSelectionPreviousDay", new KeyboardAction(KeyboardAction.ADJUST_SELECTION_PREVIOUS_DAY));
        actionMap.put("adjustSelectionNextDay", new KeyboardAction(KeyboardAction.ADJUST_SELECTION_NEXT_DAY));
        actionMap.put("adjustSelectionPreviousWeek", new KeyboardAction(KeyboardAction.ADJUST_SELECTION_PREVIOUS_WEEK));
        actionMap.put("adjustSelectionNextWeek", new KeyboardAction(KeyboardAction.ADJUST_SELECTION_NEXT_WEEK));

        actionMap.put(JXMonthView.COMMIT_KEY, acceptAction);
        actionMap.put(JXMonthView.CANCEL_KEY, cancelAction);

        // PENDING JW: complete (year-, decade-, ?? ) and consolidate with KeyboardAction
        // additional navigation actions
        AbstractActionExt prev = new AbstractActionExt() {

            @Override
            public void actionPerformed(ActionEvent e) {
                previousMonth();
            }

        };
        monthView.getActionMap().put("scrollToPreviousMonth", prev);
        AbstractActionExt next = new AbstractActionExt() {

            @Override
            public void actionPerformed(ActionEvent e) {
                nextMonth();
            }

        };
        monthView.getActionMap().put("scrollToNextMonth", next);

    }

    /**
     * @param inputMap
     */
    private void installKeyBindings(int type) {
        InputMap inputMap = monthView.getInputMap(type);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "acceptSelection");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancelSelection");

        // @KEEP quickly check #606-swingx: keybindings not working in internalframe
        // eaten somewhere
//        inputMap.put(KeyStroke.getKeyStroke("F1"), "selectPreviousDay");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "selectPreviousDay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "selectNextDay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "selectDayInPreviousWeek");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "selectDayInNextWeek");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK, false), "adjustSelectionPreviousDay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK, false), "adjustSelectionNextDay");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK, false), "adjustSelectionPreviousWeek");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK, false), "adjustSelectionNextWeek");
    }

    /**
     * @param inputMap
     */
    private void uninstallKeyBindings(int type) {
        InputMap inputMap = monthView.getInputMap(type);
        inputMap.clear();
    }

    protected void uninstallKeyboardActions() {}

    protected void installListeners() {
        propertyChangeListener = createPropertyChangeListener();
        mouseListener = createMouseListener();
        mouseMotionListener = createMouseMotionListener();

        monthView.addPropertyChangeListener(propertyChangeListener);
        monthView.addMouseListener(mouseListener);
        monthView.addMouseMotionListener(mouseMotionListener);

        monthView.getSelectionModel().addDateSelectionListener(getHandler());
    }

    protected void uninstallListeners() {
        monthView.getSelectionModel().removeDateSelectionListener(getHandler());
        monthView.removeMouseMotionListener(mouseMotionListener);
        monthView.removeMouseListener(mouseListener);
        monthView.removePropertyChangeListener(propertyChangeListener);

        mouseMotionListener = null;
        mouseListener = null;
        propertyChangeListener = null;
    }

    /**
     * Creates and installs the renderingHandler and infrastructure to use it.
     */
    protected void installRenderingHandler() {
        setRenderingHandler(createRenderingHandler());
        if (getRenderingHandler() != null) {
            rendererPane = new CellRendererPane();
            monthView.add(rendererPane);
        }
    }

    /**
     * Uninstalls the renderingHandler and infrastructure that used it.
     */
    protected void uninstallRenderingHandler() {
        if (getRenderingHandler() == null) return;
        monthView.remove(rendererPane);
        rendererPane = null;
        setRenderingHandler(null);
    }

    /**
     * Returns the <code>CalendarRenderingHandler</code> to use. Subclasses may override to
     * plug-in custom implementations. <p>
     *
     * This implementation returns an instance of RenderingHandler.
     *
     * @return the endering handler to use for painting, must not be null
     */
    protected CalendarRenderingHandler createRenderingHandler() {
        return new RenderingHandler();
    }

    /**
     * @param renderingHandler the renderingHandler to set
     */
    protected void setRenderingHandler(CalendarRenderingHandler renderingHandler) {
        this.renderingHandler = renderingHandler;
    }

    /**
     * @return the renderingHandler
     */
    protected CalendarRenderingHandler getRenderingHandler() {
        return renderingHandler;
    }

    /**
     *
     * Empty subclass for backward compatibility. The original implementation was
     * extracted as standalone class and renamed to BasicCalendarRenderingHandler. <p>
     *
     * This will be available for extension by LAF providers until all collaborators
     * in the new rendering pipeline are ready for public exposure.
     */
    protected static class RenderingHandler extends BasicCalendarRenderingHandler {

    }
    /**
     * Binds/clears the keystrokes in the component input map,
     * based on the monthView's componentInputMap enabled property.
     *
     * @see org.jdesktop.swingx.JXMonthView#isComponentInputMapEnabled()
     */
    protected void updateComponentInputMap() {
        if (monthView.isComponentInputMapEnabled()) {
            installKeyBindings(JComponent.WHEN_IN_FOCUSED_WINDOW);
        } else {
            uninstallKeyBindings(JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }


    /**
     * Updates internal state according to monthView's locale. Revalidates the
     * monthView if the boolean parameter is true.
     *
     * @param revalidate a boolean indicating whether the monthView should be
     * revalidated after the change.
     */
    protected void updateLocale(boolean revalidate) {
        Locale locale = monthView.getLocale();
        if (getRenderingHandler() != null) {
            getRenderingHandler().setLocale(locale);
        }

        // fixed JW: respect property in UIManager if available
        // PENDING JW: what to do if weekdays had been set
        // with JXMonthView method? how to detect?
        daysOfTheWeek = (String[]) UIManager.get("JXMonthView.daysOfTheWeek");

        if (daysOfTheWeek == null) {
            daysOfTheWeek = new String[7];
            String[] dateFormatSymbols = DateFormatSymbols.getInstance(locale)
                    .getShortWeekdays();
            daysOfTheWeek = new String[JXMonthView.DAYS_IN_WEEK];
            for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                daysOfTheWeek[i - 1] = dateFormatSymbols[i];
            }
        }
        if (revalidate) {
            monthView.invalidate();
            monthView.validate();
        }
    }

   @Override
   public String[] getDaysOfTheWeek() {
       String[] days = new String[daysOfTheWeek.length];
       System.arraycopy(daysOfTheWeek, 0, days, 0, days.length);
       return days;
   }

//---------------------- listener creation
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    protected LayoutManager createLayoutManager() {
        return getHandler();
    }

    protected MouseListener createMouseListener() {
        return getHandler();
    }

    protected MouseMotionListener createMouseMotionListener() {
        return getHandler();
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }

        return handler;
    }

    public boolean isUsingKeyboard() {
        return usingKeyboard;
    }

    public void setUsingKeyboard(boolean val) {
        usingKeyboard = val;
    }


    // ----------------------- mapping day coordinates

    /**
     * Returns the bounds of the day in the grid of days which contains the
     * given location. The bounds are in monthView screen coordinate system.
     * <p>
     *
     * Note: this is a pure geometric mapping. The returned rectangle need not
     * necessarily map to a date in the month which contains the location, it
     * can represent a week-number/column header or a leading/trailing date.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the bounds of the day which contains the location, or null if
     *         outside
     */
    protected Rectangle getDayBoundsAtLocation(int x, int y) {
        Rectangle monthDetails = getMonthDetailsBoundsAtLocation(x, y);
        if ((monthDetails == null) || (!monthDetails.contains(x, y)))
            return null;
        // calculate row/column in absolute grid coordinates
        int row = (y - monthDetails.y) / fullBoxHeight;
        int column = (x - monthDetails.x) / fullBoxWidth;
        return new Rectangle(monthDetails.x + column * fullBoxWidth, monthDetails.y
                + row * fullBoxHeight, fullBoxWidth, fullBoxHeight);
    }

    /**
     * Returns the bounds of the day box at logical coordinates in the given month.
     * The row's range is from DAY_HEADER_ROW to LAST_WEEK_ROW. Column's range is from
     * WEEK_HEADER_COLUMN to LAST_DAY_COLUMN.
     *
     * @param month the month containing the day box
     * @param row the logical row (== week) coordinate in the day grid
     * @param column the logical column (== day) coordinate in the day grid
     * @return the bounds of the daybox or null if not showing
     * @throws IllegalArgumentException if row or column are out off range.
     *
     * @see #getDayGridPositionAtLocation(int, int)
     */
    protected Rectangle getDayBoundsInMonth(Date month, int row, final int column) {
        checkValidRow(row, column);
        if ((WEEK_HEADER_COLUMN == column) && !monthView.isShowingWeekNumber()) return null;
        Rectangle monthBounds = getMonthBounds(month);
        if (monthBounds == null) return null;
        // dayOfWeek header is shown always
        monthBounds.y += getMonthHeaderHeight() + (row - DAY_HEADER_ROW) * fullBoxHeight;
        // PENDING JW: still looks fishy ...
        int absoluteColumn = column - FIRST_DAY_COLUMN;
        if (monthView.isShowingWeekNumber()) {
            absoluteColumn++;
        }
        if (isLeftToRight) {
           monthBounds.x += absoluteColumn * fullBoxWidth;
        } else {
            int leading = monthBounds.x + monthBounds.width - fullBoxWidth;
            monthBounds.x = leading - absoluteColumn * fullBoxWidth;
        }
        monthBounds.width = fullBoxWidth;
        monthBounds.height = fullBoxHeight;
        return monthBounds;
    }

    /**
     * Returns the logical coordinates of the day which contains the given
     * location. The p.x of the returned value represents the week header or the
     * day of week, ranging from WEEK_HEADER_COLUMN to LAST_DAY_COLUMN. The
     * p.y represents the day header or week of the month, ranging from DAY_HEADER_ROW
     * to LAST_WEEK_ROW. The transformation takes care of
     * ComponentOrientation.
     * <p>
     *
     * Note: The returned grid position need not
     * necessarily map to a date in the month which contains the location, it
     * can represent a week-number/column header or a leading/trailing date.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the logical coordinates of the day in the grid of days in a month
     *         or null if outside.
     *
     * @see #getDayBoundsInMonth(Date, int, int)
     */
    protected Point getDayGridPositionAtLocation(int x, int y) {
        Rectangle monthDetailsBounds = getMonthDetailsBoundsAtLocation(x, y);
        if ((monthDetailsBounds == null) ||(!monthDetailsBounds.contains(x, y))) return null;
        int calendarRow = (y - monthDetailsBounds.y) / fullBoxHeight + DAY_HEADER_ROW;
        int absoluteColumn = (x - monthDetailsBounds.x) / fullBoxWidth;
        int calendarColumn = absoluteColumn + FIRST_DAY_COLUMN;
        if (!isLeftToRight) {
            int leading = monthDetailsBounds.x + monthDetailsBounds.width;
            calendarColumn = (leading - x) / fullBoxWidth + FIRST_DAY_COLUMN;
        }
        if (monthView.isShowingWeekNumber()) {
            calendarColumn -= 1;
        }
        return new Point(calendarColumn, calendarRow);
    }

    /**
     * Returns the Date defined by the logical
     * grid coordinates relative to the given month. May be null if the
     * logical coordinates represent a header in the day grid or is outside of the
     * given month.
     *
     * Mapping logical day grid coordinates to Date.<p>
     *
     * PENDING JW: relax the startOfMonth pre? Why did I require it?
     *
     * @param month a calendar representing the first day of the month, must not
     *   be null.
     * @param row the logical row index in the day grid of the month
     * @param column the logical column index in the day grid of the month
     * @return the day at the logical grid coordinates in the given month or null
     *    if the coordinates are day/week header or leading/trailing dates
     * @throws IllegalStateException if the month is not the start of the month.
     *
     * @see #getDayGridPosition(Date)
     */
    protected Date getDayInMonth(Date month, int row, int column) {
        if ((row == DAY_HEADER_ROW) || (column == WEEK_HEADER_COLUMN)) return null;
        Calendar calendar = getCalendar(month);
        int monthField = calendar.get(Calendar.MONTH);
        if (!CalendarUtils.isStartOfMonth(calendar))
            throw new IllegalStateException("calendar must be start of month but was: " + month.getTime());
        CalendarUtils.startOfWeek(calendar);
        // PENDING JW: correctly mapped now?
        calendar.add(Calendar.DAY_OF_MONTH,
                (row - FIRST_WEEK_ROW) * DAYS_IN_WEEK + (column - FIRST_DAY_COLUMN));
        if (calendar.get(Calendar.MONTH) == monthField) {
            return calendar.getTime();
        }
        return null;

    }

    /**
     * Returns the given date's position in the grid of the month it is contained in.
     *
     * @param date the Date to get the logical position for, must not be null.
     * @return the logical coordinates of the day in the grid of days in a
     *   month or null if the Date is not visible.
     *
     *  @see #getDayInMonth(Date, int, int)
     */
    protected Point getDayGridPosition(Date date) {
        if (!isVisible(date)) return null;
        Calendar calendar = getCalendar(date);
        Date startOfDay = CalendarUtils.startOfDay(calendar, date);
        // there must be a less ugly way?
        // columns
        CalendarUtils.startOfWeek(calendar);
        int column = FIRST_DAY_COLUMN;
        while (calendar.getTime().before(startOfDay)) {
            column++;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Date startOfWeek = CalendarUtils.startOfWeek(calendar, date);
        calendar.setTime(date);
        CalendarUtils.startOfMonth(calendar);
        int row = FIRST_WEEK_ROW;
        while (calendar.getTime().before(startOfWeek)) {
            row++;
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return new Point(column, row);
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
     *
     * @see #getDayBounds(Date)
     */
    @Override
    public Date getDayAtLocation(int x, int y) {
        Point dayInGrid = getDayGridPositionAtLocation(x, y);
        if ((dayInGrid == null)
                || (dayInGrid.x == WEEK_HEADER_COLUMN) || (dayInGrid.y == DAY_HEADER_ROW)) return null;
        Date month = getMonthAtLocation(x, y);
        return getDayInMonth(month, dayInGrid.y, dayInGrid.x);
    }

    /**
     * Returns the bounds of the given day.
     * The bounds are in monthView coordinate system.<p>
     *
     * PENDING JW: this most probably should be public as it is the logical
     * reverse of getDayAtLocation <p>
     *
     * @param date the Date to return the bounds for. Must not be null.
     * @return the bounds of the given date or null if not visible.
     *
     * @see #getDayAtLocation(int, int)
     */
    protected Rectangle getDayBounds(Date date) {
        if (!isVisible(date)) return null;
        Point position = getDayGridPosition(date);
        Rectangle monthBounds = getMonthBounds(date);
        monthBounds.y += getMonthHeaderHeight() + (position.y - DAY_HEADER_ROW) * fullBoxHeight;
        if (monthView.isShowingWeekNumber()) {
            position.x++;
        }
        position.x -= FIRST_DAY_COLUMN;
        if (isLeftToRight) {
           monthBounds.x += position.x * fullBoxWidth;
        } else {
            int start = monthBounds.x + monthBounds.width - fullBoxWidth;
            monthBounds.x = start - position.x * fullBoxWidth;
        }
        monthBounds.width = fullBoxWidth;
        monthBounds.height = fullBoxHeight;
        return monthBounds;
    }

    /**
     * @param row
     */
    private void checkValidRow(int row, int column) {
        if ((column < WEEK_HEADER_COLUMN) || (column > LAST_DAY_COLUMN))
            throw new IllegalArgumentException("illegal column in day grid " + column);
        if ((row < DAY_HEADER_ROW) || (row > LAST_WEEK_ROW))
            throw new IllegalArgumentException("illegal row in day grid" + row);
    }

    /**
     * Returns a boolean indicating if the given Date is visible. Trailing/leading
     * dates of the last/first displayed month are considered to be invisible.
     *
     * @param date the Date to check for visibility. Must not be null.
     * @return true if the date is visible, false otherwise.
     */
    private boolean isVisible(Date date) {
        if (getFirstDisplayedDay().after(date) || getLastDisplayedDay().before(date)) return false;
        return true;
    }

    // ------------------- mapping month parts

    /**
     * Mapping pixel to bounds.<p>
     *
     * PENDING JW: define the "action grid". Currently this replaces the old
     * version to remove all internal usage of deprecated methods.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the bounds of the active header area in containing the location
     *   or null if outside.
     */
    protected int getTraversableGridPositionAtLocation(int x, int y) {
        Rectangle headerBounds = getMonthHeaderBoundsAtLocation(x, y);
        if (headerBounds == null) return -1;
        if (y < headerBounds.y + arrowPaddingY) return -1;
        if (y > headerBounds.y + headerBounds.height - arrowPaddingY) return -1;
        headerBounds.setBounds(headerBounds.x + arrowPaddingX, y,
                headerBounds.width - 2 * arrowPaddingX, headerBounds.height);
        if (!headerBounds.contains(x, y)) return -1;
        Rectangle hitArea = new Rectangle(headerBounds.x, headerBounds.y, monthUpImage.getIconWidth(), monthUpImage.getIconHeight());
        if (hitArea.contains(x, y)) {
            return isLeftToRight ? MONTH_DOWN : MONTH_UP;
        }
        hitArea.translate(headerBounds.width - monthUpImage.getIconWidth(), 0);
        if (hitArea.contains(x, y)) {
            return isLeftToRight ? MONTH_UP : MONTH_DOWN;
        }
        return -1;
    }

    /**
     * Returns the bounds of the month header which contains the
     * given location. The bounds are in monthView coordinate system.
     *
     * <p>
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the bounds of the month which contains the location,
     *   or null if outside
     */
    protected Rectangle getMonthHeaderBoundsAtLocation(int x, int y) {
        Rectangle header = getMonthBoundsAtLocation(x, y);
        if (header == null) return null;
        header.height = getMonthHeaderHeight();
        return header;
    }

    /**
     * Returns the bounds of the month details which contains the
     * given location. The bounds are in monthView coordinate system.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the bounds of the details grid in the month at
     *   location or null if outside.
     */
    protected Rectangle getMonthDetailsBoundsAtLocation(int x, int y) {
        Rectangle month = getMonthBoundsAtLocation(x, y);
        if (month == null) return null;
        int startOfDaysY = month.y + getMonthHeaderHeight();
        if (y < startOfDaysY) return null;
        month.y = startOfDaysY;
        month.height = month.height - getMonthHeaderHeight();
        return month;
    }

    // ---------------------- mapping month coordinates

    /**
      * Returns the bounds of the month which contains the
     * given location. The bounds are in monthView coordinate system.
     *
     * <p>
     *
     * Mapping pixel to bounds.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the bounds of the month which contains the location,
     *   or null if outside
     */
    protected Rectangle getMonthBoundsAtLocation(int x, int y) {
        if (!calendarGrid.contains(x, y)) return null;
        int calendarRow = (y - calendarGrid.y) / fullCalendarHeight;
        int calendarColumn = (x - calendarGrid.x) / fullCalendarWidth;
        return new Rectangle(
                calendarGrid.x + calendarColumn * fullCalendarWidth,
                calendarGrid.y + calendarRow * fullCalendarHeight,
                calendarWidth, calendarHeight);
    }

    /**
     *
     * Returns the logical coordinates of the month which contains
     * the given location. The p.x of the returned value represents the column, the
     * p.y represents the row the month is shown in. The transformation takes
     * care of ComponentOrientation. <p>
     *
     * Mapping pixel to logical grid coordinates.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the logical coordinates of the month in the grid of month shown by
     *   this monthView or null if outside.
     */
    protected Point getMonthGridPositionAtLocation(int x, int y) {
        if (!calendarGrid.contains(x, y)) return null;
        int calendarRow = (y - calendarGrid.y) / fullCalendarHeight;
        int calendarColumn = (x - calendarGrid.x) / fullCalendarWidth;
        if (!isLeftToRight) {
            int start = calendarGrid.x + calendarGrid.width;
            calendarColumn = (start - x) / fullCalendarWidth;

        }
        return new Point(calendarColumn, calendarRow);
    }

    /**
     * Returns the Date representing the start of the month which
     * contains the given location.<p>
     *
     * Mapping pixel to calendar day.
     *
     * @param x the x position of the location in pixel
     * @param y the y position of the location in pixel
     * @return the start of the month which contains the given location or
     *    null if the location is outside the grid of months.
     */
    protected Date getMonthAtLocation(int x, int y) {
        Point month = getMonthGridPositionAtLocation(x, y);
        if (month ==  null) return null;
        return getMonth(month.y, month.x);
    }

    /**
     * Returns the Date representing the start of the month at the given
     * logical position in the grid of months. <p>
     *
     * Mapping logical grid coordinates to Calendar.
     *
     * @param row the rowIndex in the grid of months.
     * @param column the columnIndex in the grid months.
     * @return a Date representing the start of the month at the given
     *   logical coordinates.
     *
     * @see #getMonthGridPosition(Date)
     */
    protected Date getMonth(int row, int column) {
        Calendar calendar = getCalendar();
        calendar.add(Calendar.MONTH,
                row * calendarColumnCount + column);
        return calendar.getTime();

    }

    /**
     * Returns the logical grid position of the month containing the given date.
     * The Point's x value is the column in the grid of months, the y value
     * is the row in the grid of months.
     *
     * Mapping Date to logical grid position, this is the reverse of getMonth(int, int).
     *
     * @param date the Date to return the bounds for. Must not be null.
     * @return the postion of the month that contains the given date or null if not visible.
     *
     * @see #getMonth(int, int)
     * @see #getMonthBounds(int, int)
     */
    protected Point getMonthGridPosition(Date date) {
        if (!isVisible(date)) return null;
        // start of grid
        Calendar calendar = getCalendar();
        int firstMonth = calendar.get(Calendar.MONTH);
        int firstYear = calendar.get(Calendar.YEAR);

        //
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        int diffMonths = month - firstMonth
            + ((year - firstYear) * JXMonthView.MONTHS_IN_YEAR);

        int row = diffMonths / calendarColumnCount;
        int column = diffMonths % calendarColumnCount;

        return new Point(column, row);
    }

    /**
     * Returns the bounds of the month at the given logical coordinates
     * in the grid of visible months.<p>
     *
     * Mapping logical grip position to pixel.
     *
     * @param row the rowIndex in the grid of months.
     * @param column the columnIndex in the grid months.
     * @return the bounds of the month at the given logical logical position.
     *
     * @see #getMonthGridPositionAtLocation(int, int)
     * @see #getMonthBoundsAtLocation(int, int)
     */
    protected Rectangle getMonthBounds(int row, int column) {
        int startY = calendarGrid.y + row * fullCalendarHeight;
        int startX = calendarGrid.x + column * fullCalendarWidth;
        if (!isLeftToRight) {
            startX = calendarGrid.x + (calendarColumnCount - 1 - column) * fullCalendarWidth;
        }
        return new Rectangle(startX, startY, calendarWidth, calendarHeight);
    }

    /**
     * Returns the bounds of the month containing the given date.
     * The bounds are in monthView coordinate system.<p>
     *
     * Mapping Date to pixel.
     *
     * @param date the Date to return the bounds for. Must not be null.
     * @return the bounds of the month that contains the given date or null if not visible.
     *
     * @see #getMonthAtLocation(int, int)
     */
    protected Rectangle getMonthBounds(Date date) {
        Point position = getMonthGridPosition(date);
        return position != null ? getMonthBounds(position.y, position.x) : null;
    }

    /**
     * Returns the bounds of the month containing the given date.
     * The bounds are in monthView coordinate system.<p>
     *
     * Mapping Date to pixel.
     *
     * @param date the Date to return the bounds for. Must not be null.
     * @return the bounds of the month that contains the given date or null if not visible.
     *
     * @see #getMonthAtLocation(int, int)
     */
    protected Rectangle getMonthHeaderBounds(Date date, boolean includeInsets) {
        Point position = getMonthGridPosition(date);
        if (position == null) return null;
        Rectangle bounds = getMonthBounds(position.y, position.x);
        bounds.height = getMonthHeaderHeight();
        if (!includeInsets) {

        }
        return bounds;
    }

    //---------------- accessors for sizes

    /**
     * Returns the size of a month.
     * @return the size of a month.
     */
    protected Dimension getMonthSize() {
        return new Dimension(calendarWidth, calendarHeight);
    }

    /**
     * Returns the size of a day including the padding.
     * @return the size of a month.
     */
    protected Dimension getDaySize() {
        return new Dimension(fullBoxWidth, fullBoxHeight);
    }
    /**
     * Returns the height of the month header.
     *
     * @return the height of the month header.
     */
    protected int getMonthHeaderHeight() {
        return fullMonthBoxHeight;
    }


    //-------------------  layout

    /**
     * Called from layout: calculates properties
     * of grid of months.
     */
    private void calculateMonthGridLayoutProperties() {
        calculateMonthGridRowColumnCount();
        calculateMonthGridBounds();
    }

    /**
     * Calculates the bounds of the grid of months.
     *
     * CalendarRow/ColumnCount and calendarWidth/Height must be
     * initialized before calling this.
     */
    private void calculateMonthGridBounds() {
        calendarGrid.setBounds(calculateCalendarGridX(),
                calculateCalendarGridY(),
                calculateCalendarGridWidth(),
                calculateCalendarGridHeight());
    }

    private int calculateCalendarGridY() {
        return (monthView.getHeight() - calculateCalendarGridHeight()) / 2;
    }

    private int calculateCalendarGridX() {
        return (monthView.getWidth() - calculateCalendarGridWidth()) / 2;
    }

    private int calculateCalendarGridHeight() {
        return ((calendarHeight * calendarRowCount) +
                (CALENDAR_SPACING * (calendarRowCount - 1 )));
    }

    private int calculateCalendarGridWidth() {
        return ((calendarWidth * calendarColumnCount) +
                (CALENDAR_SPACING * (calendarColumnCount - 1)));
    }

    /**
     * Calculates and updates the numCalCols/numCalRows that determine the
     * number of calendars that can be displayed. Updates the last displayed
     * date if appropriate.
     *
     */
    private void calculateMonthGridRowColumnCount() {
        int oldNumCalCols = calendarColumnCount;
        int oldNumCalRows = calendarRowCount;

        calendarRowCount = 1;
        calendarColumnCount = 1;
        if (!isZoomable()) {
            // Determine how many columns of calendars we want to paint.
            int addColumns = (monthView.getWidth() - calendarWidth)
                    / (calendarWidth + CALENDAR_SPACING);
            // happens if used as renderer in a tree.. don't know yet why
            if (addColumns > 0) {
                calendarColumnCount += addColumns;
            }

            // Determine how many rows of calendars we want to paint.
            int addRows = (monthView.getHeight() - calendarHeight)
                    / (calendarHeight + CALENDAR_SPACING);
            if (addRows > 0) {
                calendarRowCount += addRows;
            }
        }
        if (oldNumCalCols != calendarColumnCount
                || oldNumCalRows != calendarRowCount) {
            updateLastDisplayedDay(getFirstDisplayedDay());
        }
    }

    /**
     * @return true if the month view can be zoomed, false otherwise
     */
    protected boolean isZoomable() {
        return monthView.isZoomable();
    }




//-------------------- painting

    /**
     * Overridden to extract the background painting for ease-of-use of
     * subclasses.
     */
    @Override
    public void update(Graphics g, JComponent c) {
        paintBackground(g);
        paint(g, c);
    }

    /**
     * Paints the background of the component. This implementation fill the
     * monthView's area with its background color if opaque, does nothing
     * if not opaque. Subclasses can override but must comply to opaqueness
     * contract.
     *
     * @param g the Graphics to fill.
     *
     */
    protected void paintBackground(Graphics g) {
        if (monthView.isOpaque()) {
            g.setColor(monthView.getBackground());
            g.fillRect(0, 0, monthView.getWidth(), monthView.getHeight());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        Rectangle clip = g.getClipBounds();
        // Get a calender set to the first displayed date
        Calendar cal = getCalendar();
        // loop through grid of months
        for (int row = 0; row < calendarRowCount; row++) {
            for (int column = 0; column < calendarColumnCount; column++) {
                // get the bounds of the current month.
                Rectangle bounds = getMonthBounds(row, column);
                // Check if this row falls in the clip region.
                if (bounds.intersects(clip)) {
                    paintMonth(g, cal);
                }
                cal.add(Calendar.MONTH, 1);
            }
        }

    }

    /**
     * Paints the month represented by the given Calendar.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the first day of the month to
     *        paint, must not be null
     */
    protected void paintMonth(Graphics g, Calendar month) {
        paintMonthHeader(g, month);
        paintDayHeader(g, month);
        paintWeekHeader(g, month);
        paintDays(g, month);
    }

    /**
     * Paints the header of a month.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the first day of the month to
     *        paint, must not be null
     */
    protected void paintMonthHeader(Graphics g, Calendar month) {
        Rectangle page = getMonthHeaderBounds(month.getTime(), false);
        paintDayOfMonth(g, page, month, CalendarState.TITLE);
    }

    /**
     * Paints the day column header.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the first day of the month to
     *        paint, must not be null
     */
    protected void paintDayHeader(Graphics g, Calendar month) {
        paintDaysOfWeekSeparator(g, month);
        Calendar cal = (Calendar) month.clone();
        CalendarUtils.startOfWeek(cal);
        for (int i = FIRST_DAY_COLUMN; i <= LAST_DAY_COLUMN; i++) {
            Rectangle dayBox = getDayBoundsInMonth(month.getTime(), DAY_HEADER_ROW, i);
            paintDayOfMonth(g, dayBox, cal, CalendarState.DAY_OF_WEEK);
            cal.add(Calendar.DATE, 1);
        }
    }

    /**
     * Paints the day column header.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the first day of the month to
     *        paint, must not be null
     */
    protected void paintWeekHeader(Graphics g, Calendar month) {
        if (!monthView.isShowingWeekNumber())
            return;
        paintWeekOfYearSeparator(g, month);

        int weeks = getWeeks(month);
        // the calendar passed to the renderers
        Calendar weekCalendar = (Calendar) month.clone();
        // we loop by logical row (== week in month) coordinates
        for (int week = FIRST_WEEK_ROW; week < FIRST_WEEK_ROW + weeks; week++) {
            // get the day bounds based on logical row/column coordinates
            Rectangle dayBox = getDayBoundsInMonth(month.getTime(), week, WEEK_HEADER_COLUMN);
            // NOTE: this can be set to any day in the week to render the weeknumber of
            // categorized by CalendarState
            paintDayOfMonth(g, dayBox, weekCalendar, CalendarState.WEEK_OF_YEAR);
            weekCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
    }

    /**
     * Paints the days of the given month.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the first day of the month to
     *        paint, must not be null
     */
    protected void paintDays(Graphics g, Calendar month) {
        Calendar clonedCal = (Calendar) month.clone();
        CalendarUtils.startOfMonth(clonedCal);
        Date startOfMonth = clonedCal.getTime();
        CalendarUtils.endOfMonth(clonedCal);
        Date endOfMonth = clonedCal.getTime();
        // reset the clone
        clonedCal.setTime(month.getTime());
        // adjust to start of week
        clonedCal.setTime(month.getTime());
        CalendarUtils.startOfWeek(clonedCal);
        for (int week = FIRST_WEEK_ROW; week <= LAST_WEEK_ROW; week++) {
            for (int day = FIRST_DAY_COLUMN; day <= LAST_DAY_COLUMN; day++) {
                CalendarState state = null;
                if (clonedCal.getTime().before(startOfMonth)) {
                    if (monthView.isShowingLeadingDays()) {
                        state = CalendarState.LEADING;
                    }
                } else if (clonedCal.getTime().after(endOfMonth)) {
                    if (monthView.isShowingTrailingDays()) {
                        state = CalendarState.TRAILING;
                    }

                } else {
                    state = isToday(clonedCal.getTime()) ? CalendarState.TODAY : CalendarState.IN_MONTH;
                }
                if (state != null) {
                    Rectangle bounds = getDayBoundsInMonth(startOfMonth, week, day);
                    paintDayOfMonth(g, bounds, clonedCal, state);
                }
                clonedCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    /**
     * Paints a day which is of the current month with the given state.<p>
     *
     * PENDING JW: mis-nomer - this is in fact called for rendering any day-related
     * state (including weekOfYear, dayOfWeek headers) and for rendering
     * the month header as well, that is from everywhere.
     *  Rename to paintSomethingGeneral. Think about impact for subclasses
     *  (what do they really need? feedback please!)
     *
     * @param g the graphics to paint into.
     * @param bounds the rectangle to paint the day into
     * @param calendar the calendar representing the day to paint
     * @param state the calendar state
     */
    protected void paintDayOfMonth(Graphics g, Rectangle bounds, Calendar calendar, CalendarState state) {
        JComponent comp = getRenderingHandler().prepareRenderingComponent(monthView, calendar,
                state);
        rendererPane.paintComponent(g, comp, monthView, bounds.x, bounds.y,
                bounds.width, bounds.height, true);
    }

    /**
     * Paints the separator between row header (weeks of year) and days.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the first day of the month to
     *        paint, must not be null
     */
    protected void paintWeekOfYearSeparator(Graphics g, Calendar month) {
        Rectangle r = getSeparatorBounds(month, FIRST_WEEK_ROW, WEEK_HEADER_COLUMN);
        if (r == null) return;
        g.setColor(monthView.getForeground());
        g.drawLine(r.x, r.y, r.x, r.y + r.height);
    }

    /**
     * Paints the separator between column header (days of week) and days.
     *
     * Note: the given calendar must not be changed.
     * @param g the graphics to paint into
     * @param month the calendar specifying the the first day of the month to
     *        paint, must not be null
     */
    protected void paintDaysOfWeekSeparator(Graphics g, Calendar month) {
        Rectangle r = getSeparatorBounds(month, DAY_HEADER_ROW, FIRST_DAY_COLUMN);
        if (r == null) return;
        g.setColor(monthView.getForeground());
        g.drawLine(r.x, r.y, r.x + r.width, r.y);
    }

    /**
     * @param month
     * @param row
     * @param column
     * @return
     */
    private Rectangle getSeparatorBounds(Calendar month, int row, int column) {
        Rectangle separator = getDayBoundsInMonth(month.getTime(), row, column);
        if (separator == null) return null;
        if (column == WEEK_HEADER_COLUMN) {
            separator.height *= WEEKS_IN_MONTH;
            if (isLeftToRight) {
                separator.x += separator.width - 1;
            }
            separator.width = 1;
        } else if (row == DAY_HEADER_ROW) {
            int oldWidth = separator.width;
            separator.width *= DAYS_IN_WEEK;
            if (!isLeftToRight) {
                separator.x -= separator.width - oldWidth;
            }
            separator.y += separator.height - 1;
            separator.height = 1;
        }
        return separator;
    }

    /**
     * Returns the number of weeks to paint in the current month, as represented
     * by the given calendar. The calendar is expected to be set to the first
     * of the month.
     *
     * Note: the given calendar must not be changed.
     *
     * @param month the calendar specifying the the first day of the month to
     *        paint, must not be null
     * @return the number of weeks of this month.
     */
    protected int getWeeks(Calendar month) {
        Calendar cloned = (Calendar) month.clone();
        // the calendar is set to the first of month, get date for last
        CalendarUtils.endOfMonth(cloned);
        // marker for end
        Date last = cloned.getTime();
        // start again
        cloned.setTime(month.getTime());
        CalendarUtils.startOfWeek(cloned);
        int weeks = 0;
        while (last.after(cloned.getTime())) {
            weeks++;
            cloned.add(Calendar.WEEK_OF_MONTH, 1);
        }
        return weeks;
    }


    private void traverseMonth(int arrowType) {
        if (arrowType == MONTH_DOWN) {
            previousMonth();
        } else if (arrowType == MONTH_UP) {
            nextMonth();
        }
    }

    private void nextMonth() {
        Date upperBound = monthView.getUpperBound();
        if (upperBound == null
                || upperBound.after(getLastDisplayedDay()) ){
            Calendar cal = getCalendar();
            cal.add(Calendar.MONTH, 1);
            monthView.setFirstDisplayedDay(cal.getTime());
        }
    }

    private void previousMonth() {
        Date lowerBound = monthView.getLowerBound();
        if (lowerBound == null
                || lowerBound.before(getFirstDisplayedDay())){
            Calendar cal = getCalendar();
            cal.add(Calendar.MONTH, -1);
            monthView.setFirstDisplayedDay(cal.getTime());
        }
    }

//--------------------------- displayed dates, calendar

    /**
     * Returns the monthViews calendar configured to the firstDisplayedDate.
     *
     * NOTE: it's safe to change the calendar state without resetting because
     * it's JXMonthView's responsibility to protect itself.
     *
     * @return the monthView's calendar, configured with the firstDisplayedDate.
     */
    protected Calendar getCalendar() {
        return getCalendar(getFirstDisplayedDay());
    }

    /**
     * Returns the monthViews calendar configured to the given time.
     *
     * NOTE: it's safe to change the calendar state without resetting because
     * it's JXMonthView's responsibility to protect itself.
     *
     * @param date the date to configure the calendar with
     * @return the monthView's calendar, configured with the given date.
     */
    protected Calendar getCalendar(Date date) {
        Calendar calendar = monthView.getCalendar();
        calendar.setTime(date);
        return calendar;
    }


    /**
     * Updates the lastDisplayedDate property based on the given first and
     * visible # of months.
     *
     * @param first the date of the first visible day.
     */
    private void updateLastDisplayedDay(Date first) {
        Calendar cal = getCalendar(first);
        cal.add(Calendar.MONTH, ((calendarColumnCount * calendarRowCount) - 1));
        CalendarUtils.endOfMonth(cal);
        lastDisplayedDate = cal.getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLastDisplayedDay() {
        return lastDisplayedDate;
    }

    /*-------------- refactored: encapsulate aliased fields
     */

    /**
     * Updates internal state that depends on the MonthView's firstDisplayedDay
     * property. <p>
     *
     * Here: updates lastDisplayedDay.
     * <p>
     *
     *
     * @param firstDisplayedDay the firstDisplayedDate to set
     */
    protected void setFirstDisplayedDay(Date firstDisplayedDay) {
        updateLastDisplayedDay(firstDisplayedDay);
    }

    /**
     * Returns the first displayed day. Convenience delegate to
     *
     * @return the firstDisplayed
     */
    protected Date getFirstDisplayedDay() {
        return monthView.getFirstDisplayedDay();
    }

    /**
     * @return the firstDisplayedMonth
     */
    protected int getFirstDisplayedMonth() {
        return getCalendar().get(Calendar.MONTH);
    }

    /**
     * @return the firstDisplayedYear
     */
    protected int getFirstDisplayedYear() {
        return getCalendar().get(Calendar.YEAR);
    }

    /**
     * @return the selection
     */
    protected SortedSet<Date> getSelection() {
        return monthView.getSelection();
    }

    /**
     * @return the start of today.
     */
    protected Date getToday() {
        return monthView.getToday();
    }

    /**
     * Returns true if the date passed in is the same as today.
     *
     * PENDING JW: really want the exact test?
     *
     * @param date long representing the date you want to compare to today.
     * @return true if the date passed is the same as today.
     */
    protected boolean isToday(Date date) {
        return date.equals(getToday());
    }

//-----------------------end encapsulation

//------------------ Handler implementation
//
    /**
     * temporary: removed SelectionMode.NO_SELECTION, replaced
     * all access by this method to enable easy re-adding, if we want it.
     * If not - remove.
     */
    private boolean canSelectByMode() {
        return true;
    }

    private class Handler implements
        MouseListener, MouseMotionListener, LayoutManager,
            PropertyChangeListener, DateSelectionListener {
        private boolean armed;
        private Date startDate;
        private Date endDate;

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            // If we were using the keyboard we aren't anymore.
            setUsingKeyboard(false);

            if (!monthView.isEnabled()) {
                return;
            }

            if (!monthView.hasFocus() && monthView.isFocusable()) {
                monthView.requestFocusInWindow();
            }

            // Check if one of the month traverse buttons was pushed.
            if (monthView.isTraversable()) {
                int arrowType = getTraversableGridPositionAtLocation(e.getX(), e.getY());
                if (arrowType != -1) {
                    traverseMonth(arrowType);
                    return;
                }
            }

            if (!canSelectByMode()) {
                return;
            }

            Date cal = getDayAtLocation(e.getX(), e.getY());
            if (cal == null) {
                return;
            }

            // Update the selected dates.
            startDate = cal;
            endDate = cal;

            if (monthView.getSelectionMode() == SelectionMode.SINGLE_INTERVAL_SELECTION ||
//                    selectionMode == SelectionMode.WEEK_INTERVAL_SELECTION ||
                    monthView.getSelectionMode() == SelectionMode.MULTIPLE_INTERVAL_SELECTION) {
                pivotDate = startDate;
            }

            monthView.getSelectionModel().setAdjusting(true);

            if (monthView.getSelectionMode() == SelectionMode.MULTIPLE_INTERVAL_SELECTION && e.isControlDown()) {
                monthView.addSelectionInterval(startDate, endDate);
            } else {
                monthView.setSelectionInterval(startDate, endDate);
            }

            // Arm so we fire action performed on mouse release.
            armed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // If we were using the keyboard we aren't anymore.
            setUsingKeyboard(false);

            if (!monthView.isEnabled()) {
                return;
            }

            if (!monthView.hasFocus() && monthView.isFocusable()) {
                monthView.requestFocusInWindow();
            }

            if (armed) {
                monthView.commitSelection();
            }
            armed = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseDragged(MouseEvent e) {
            // If we were using the keyboard we aren't anymore.
            setUsingKeyboard(false);
            if (!monthView.isEnabled() || !canSelectByMode()) {
                return;
            }
            Date cal = getDayAtLocation(e.getX(), e.getY());
            if (cal == null) {
                return;
            }

            Date selected = cal;
            Date oldStart = startDate;
            Date oldEnd = endDate;

            if (monthView.getSelectionMode() == SelectionMode.SINGLE_SELECTION) {
                if (selected.equals(oldStart)) {
                    return;
                }
                startDate = selected;
                endDate = selected;
            } else  if (pivotDate != null){
                if (selected.before(pivotDate)) {
                    startDate = selected;
                    endDate = pivotDate;
                } else if (selected.after(pivotDate)) {
                    startDate = pivotDate;
                    endDate = selected;
                }
            } else { // pivotDate had not yet been initialiased
                // might happen on first click into leading/trailing dates
                // JW: fix of #996-swingx: NPE when dragging
                startDate = selected;
                endDate = selected;
                pivotDate = selected;
            }

            if (startDate.equals(oldStart) && endDate.equals(oldEnd)) {
                return;
            }

            if (monthView.getSelectionMode() == SelectionMode.MULTIPLE_INTERVAL_SELECTION && e.isControlDown()) {
                monthView.addSelectionInterval(startDate, endDate);
            } else {
                monthView.setSelectionInterval(startDate, endDate);
            }

            // Set trigger.
            armed = true;
        }

        @Override
        public void mouseMoved(MouseEvent e) {}

//------------------------ layout

        private Dimension preferredSize = new Dimension();

        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            layoutContainer(parent);
            return new Dimension(preferredSize);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {

            int maxMonthWidth = 0;
            int maxMonthHeight = 0;
            Calendar calendar = getCalendar();
            for (int i = calendar.getMinimum(Calendar.MONTH); i <= calendar.getMaximum(Calendar.MONTH); i++) {
                calendar.set(Calendar.MONTH, i);
                CalendarUtils.startOfMonth(calendar);
                JComponent comp = getRenderingHandler().prepareRenderingComponent(monthView, calendar, CalendarState.TITLE);
                Dimension pref = comp.getPreferredSize();
                maxMonthWidth = Math.max(maxMonthWidth, pref.width);
                maxMonthHeight = Math.max(maxMonthHeight, pref.height);
            }

            int maxBoxWidth = 0;
            int maxBoxHeight = 0;
            calendar = getCalendar();
            CalendarUtils.startOfWeek(calendar);
            for (int i = 0; i < JXMonthView.DAYS_IN_WEEK; i++) {
                JComponent comp = getRenderingHandler().prepareRenderingComponent(monthView, calendar, CalendarState.DAY_OF_WEEK);
                Dimension pref = comp.getPreferredSize();
                maxBoxWidth = Math.max(maxBoxWidth, pref.width);
                maxBoxHeight = Math.max(maxBoxHeight, pref.height);
                calendar.add(Calendar.DATE, 1);
            }

            calendar = getCalendar();
            for (int i = 0; i < calendar.getMaximum(Calendar.DAY_OF_MONTH); i++) {
                JComponent comp = getRenderingHandler().prepareRenderingComponent(monthView, calendar, CalendarState.IN_MONTH);
                Dimension pref = comp.getPreferredSize();
                maxBoxWidth = Math.max(maxBoxWidth, pref.width);
                maxBoxHeight = Math.max(maxBoxHeight, pref.height);
                calendar.add(Calendar.DATE, 1);
            }

            int dayColumns = JXMonthView.DAYS_IN_WEEK;
            if (monthView.isShowingWeekNumber()) {
                dayColumns++;
            }

            if (maxMonthWidth > maxBoxWidth * dayColumns) {
                //  monthHeader pref > sum of box widths
                // handle here: increase day box width accordingly
                double diff = maxMonthWidth - (maxBoxWidth * dayColumns);
                maxBoxWidth += Math.ceil(diff/(double) dayColumns);

            }

            fullBoxWidth = maxBoxWidth;
            fullBoxHeight = maxBoxHeight;
            // PENDING JW: huuh? what we doing here?
            int boxHeight = maxBoxHeight - 2 * monthView.getBoxPaddingY();
            fullMonthBoxHeight = Math.max(boxHeight, maxMonthHeight) ;

            // Keep track of calendar width and height for use later.
            calendarWidth = fullBoxWidth * JXMonthView.DAYS_IN_WEEK;
            if (monthView.isShowingWeekNumber()) {
                calendarWidth += fullBoxWidth;
            }
            fullCalendarWidth = calendarWidth + CALENDAR_SPACING;

            calendarHeight = (fullBoxHeight * 7) + fullMonthBoxHeight;
            fullCalendarHeight = calendarHeight + CALENDAR_SPACING;
            // Calculate minimum width/height for the component.
            int prefRows = getPreferredRows();
            preferredSize.height = (calendarHeight * prefRows) +
                    (CALENDAR_SPACING * (prefRows - 1));

            int prefCols = getPreferredColumns();
            preferredSize.width = (calendarWidth * prefCols) +
                    (CALENDAR_SPACING * (prefCols - 1));

            // Add insets to the dimensions.
            Insets insets = monthView.getInsets();
            preferredSize.width += insets.left + insets.right;
            preferredSize.height += insets.top + insets.bottom;

            calculateMonthGridLayoutProperties();

            if (isZoomable()) {
                getCalendarHeaderHandler().getHeaderComponent().setBounds(getMonthHeaderBounds(monthView.getFirstDisplayedDay(), false));
            }
        }

        /**
         * @return
         */
        private int getPreferredColumns() {
            return isZoomable() ? 1 : monthView.getPreferredColumnCount();
        }

        /**
         * @return
         */
        private int getPreferredRows() {
            return isZoomable() ? 1 : monthView.getPreferredRowCount();
        }


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();

            if ("componentOrientation".equals(property)) {
                isLeftToRight = monthView.getComponentOrientation().isLeftToRight();
                monthView.revalidate();
                monthView.repaint();
            } else if (JXMonthView.SELECTION_MODEL.equals(property)) {
                DateSelectionModel selectionModel = (DateSelectionModel) evt.getOldValue();
                selectionModel.removeDateSelectionListener(getHandler());
                selectionModel = (DateSelectionModel) evt.getNewValue();
                selectionModel.addDateSelectionListener(getHandler());
            } else if ("firstDisplayedDay".equals(property)) {
                setFirstDisplayedDay(((Date) evt.getNewValue()));
                monthView.repaint();
            } else if (JXMonthView.BOX_PADDING_X.equals(property)
                    || JXMonthView.BOX_PADDING_Y.equals(property)
                    || JXMonthView.TRAVERSABLE.equals(property)
                    || JXMonthView.DAYS_OF_THE_WEEK.equals(property)
                    || "border".equals(property)
                    || "showingWeekNumber".equals(property)
                    || "traversable".equals(property)

                    ) {
                monthView.revalidate();
                monthView.repaint();
            } else if ("zoomable".equals(property)) {
                updateZoomable();
//            } else if ("font".equals(property)) {
//                calendarHeaderHandler.getHeaderComponent().setFont(getAsNotUIResource(createDerivedFont()));
//                monthView.revalidate();
            } else if ("componentInputMapEnabled".equals(property)) {
                updateComponentInputMap();
            } else if ("locale".equals(property)) { // "locale" is bound property
                updateLocale(true);
            } else {
                monthView.repaint();
//                LOG.info("got propertyChange:" + property);
            }
        }

        @Override
        public void valueChanged(DateSelectionEvent ev) {
            monthView.repaint();
        }

    }

    /**
     * Class that supports keyboard traversal of the JXMonthView component.
     */
    private class KeyboardAction extends AbstractAction {
        public static final int ACCEPT_SELECTION = 0;
        public static final int CANCEL_SELECTION = 1;
        public static final int SELECT_PREVIOUS_DAY = 2;
        public static final int SELECT_NEXT_DAY = 3;
        public static final int SELECT_DAY_PREVIOUS_WEEK = 4;
        public static final int SELECT_DAY_NEXT_WEEK = 5;
        public static final int ADJUST_SELECTION_PREVIOUS_DAY = 6;
        public static final int ADJUST_SELECTION_NEXT_DAY = 7;
        public static final int ADJUST_SELECTION_PREVIOUS_WEEK = 8;
        public static final int ADJUST_SELECTION_NEXT_WEEK = 9;

        private int action;

        public KeyboardAction(int action) {
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            if (!canSelectByMode())
                return;
            if (!isUsingKeyboard()) {
                originalDateSpan = getSelection();
            }
            // JW: removed the isUsingKeyboard from the condition
            // need to fire always.
            if (action >= ACCEPT_SELECTION && action <= CANCEL_SELECTION) {
                // refactor the logic ...
                if (action == CANCEL_SELECTION) {
                    // Restore the original selection.
                    if ((originalDateSpan != null)
                            && !originalDateSpan.isEmpty()) {
                        monthView.setSelectionInterval(
                                originalDateSpan.first(), originalDateSpan
                                        .last());
                    } else {
                        monthView.clearSelection();
                    }
                    monthView.cancelSelection();
                } else {
                    // Accept the keyboard selection.
                    monthView.commitSelection();
                }
                setUsingKeyboard(false);
            } else if (action >= SELECT_PREVIOUS_DAY
                    && action <= SELECT_DAY_NEXT_WEEK) {
                setUsingKeyboard(true);
                monthView.getSelectionModel().setAdjusting(true);
                pivotDate = null;
                traverse(action);
            } else if (isIntervalMode()
                    && action >= ADJUST_SELECTION_PREVIOUS_DAY
                    && action <= ADJUST_SELECTION_NEXT_WEEK) {
                setUsingKeyboard(true);
                monthView.getSelectionModel().setAdjusting(true);
                addToSelection(action);
            }
        }

        /**
         * @return
         */
        private boolean isIntervalMode() {
            return !(monthView.getSelectionMode() == SelectionMode.SINGLE_SELECTION);
        }

        private void traverse(int action) {
            Date oldStart = monthView.isSelectionEmpty() ?
                    monthView.getToday() : monthView.getFirstSelectionDate();
            Calendar cal = getCalendar(oldStart);
            switch (action) {
                case SELECT_PREVIOUS_DAY:
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case SELECT_NEXT_DAY:
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case SELECT_DAY_PREVIOUS_WEEK:
                    cal.add(Calendar.DAY_OF_MONTH, -JXMonthView.DAYS_IN_WEEK);
                    break;
                case SELECT_DAY_NEXT_WEEK:
                    cal.add(Calendar.DAY_OF_MONTH, JXMonthView.DAYS_IN_WEEK);
                    break;
            }

            Date newStartDate = cal.getTime();
            if (!newStartDate.equals(oldStart)) {
                monthView.setSelectionInterval(newStartDate, newStartDate);
                monthView.ensureDateVisible(newStartDate);
            }
        }

        /**
         * If we are in a mode that allows for range selection this method
         * will extend the currently selected range.
         *
         * NOTE: This may not be the expected behavior for the keyboard controls
         * and we ay need to update this code to act in a way that people expect.
         *
         * @param action action for adjusting selection
         */
        private void addToSelection(int action) {
            Date newStartDate;
            Date newEndDate;
            Date selectionStart;
            Date selectionEnd;
            if (!monthView.isSelectionEmpty()) {
                newStartDate = selectionStart = monthView.getFirstSelectionDate();
                newEndDate = selectionEnd = monthView.getLastSelectionDate();
            } else {
                newStartDate = selectionStart = monthView.getToday();
                newEndDate = selectionEnd = newStartDate;
            }

            if (pivotDate == null) {
                pivotDate = newStartDate;
            }

            // want a copy to play with - each branch sets and reads the time
            // actually don't care about the pre-set time.
            Calendar cal = getCalendar();
            boolean isStartMoved;
            switch (action) {
            case ADJUST_SELECTION_PREVIOUS_DAY:
                if (newEndDate.after(pivotDate)) {
                    newEndDate = previousDay(cal, newEndDate);
                    isStartMoved = false;
                } else {
                    newStartDate = previousDay(cal, newStartDate);
                    newEndDate = pivotDate;
                    isStartMoved = true;
                }
                break;
            case ADJUST_SELECTION_NEXT_DAY:
                if (newStartDate.before(pivotDate)) {
                    newStartDate = nextDay(cal, newStartDate);
                    isStartMoved = true;
                } else {
                    newEndDate = nextDay(cal, newEndDate);
                    isStartMoved = false;
                    newStartDate = pivotDate;
                }
                break;
            case ADJUST_SELECTION_PREVIOUS_WEEK:
                if (newEndDate.after(pivotDate)) {
                    Date newTime = previousWeek(cal, newEndDate);
                    if (newTime.after(pivotDate)) {
                        newEndDate = newTime;
                        isStartMoved = false;
                    } else {
                        newStartDate = newTime;
                        newEndDate = pivotDate;
                        isStartMoved = true;
                    }
                } else {
                    newStartDate = previousWeek(cal, newStartDate);
                    isStartMoved = true;
                }
                break;
            case ADJUST_SELECTION_NEXT_WEEK:
                if (newStartDate.before(pivotDate)) {
                    Date newTime = nextWeek(cal, newStartDate);
                    if (newTime.before(pivotDate)) {
                        newStartDate = newTime;
                        isStartMoved = true;
                    } else {
                        newStartDate = pivotDate;
                        newEndDate = newTime;
                        isStartMoved = false;
                    }
                } else {
                    newEndDate = nextWeek(cal, newEndDate);
                    isStartMoved = false;
                }
                break;
            default : throw new IllegalArgumentException("invalid adjustment action: " + action);
            }

            if (!newStartDate.equals(selectionStart) || !newEndDate.equals(selectionEnd)) {
                monthView.setSelectionInterval(newStartDate, newEndDate);
                monthView.ensureDateVisible(isStartMoved ? newStartDate  : newEndDate);
            }

        }

        /**
         * @param cal
         * @param date
         * @return
         */
        private Date nextWeek(Calendar cal, Date date) {
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, JXMonthView.DAYS_IN_WEEK);
            return cal.getTime();
        }

        /**
         * @param cal
         * @param date
         * @return
         */
        private Date previousWeek(Calendar cal, Date date) {
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, -JXMonthView.DAYS_IN_WEEK);
            return cal.getTime();
        }

        /**
         * @param cal
         * @param date
         * @return
         */
        private Date nextDay(Calendar cal, Date date) {
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }

        /**
         * @param cal
         * @param date
         * @return
         */
        private Date previousDay(Calendar cal, Date date) {
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return cal.getTime();
        }

    }

//--------------------- zoomable

    /**
     * Updates state after the monthView's zoomable property has been changed.
     * This implementation adds/removes the header component if zoomable is true/false
     * respectively.
     */
    protected void updateZoomable() {
        if (monthView.isZoomable()) {
            monthView.add(getCalendarHeaderHandler().getHeaderComponent());
        } else {
            monthView.remove(getCalendarHeaderHandler().getHeaderComponent());
        }
        monthView.revalidate();
        monthView.repaint();
    }

    /**
     * Creates and returns a calendar header handler which provides and configures
     * a component for use in a zoomable monthView. Subclasses may override to return
     * a custom handler.<p>
     *
     * This implementation first queries the UIManager for class to use and returns
     * that if available, returns a BasicCalendarHeaderHandler if not.
     *
     * @return a calendar header handler providing a component for use in zoomable
     *   monthView.
     *
     * @see #getHeaderFromUIManager()
     * @see CalendarHeaderHandler
     * @see BasicCalendarHeaderHandler
     */
    protected CalendarHeaderHandler createCalendarHeaderHandler() {
        CalendarHeaderHandler handler = getHeaderFromUIManager();
        return handler != null ? handler : new BasicCalendarHeaderHandler();
    }

    /**
     * Returns a CalendarHeaderHandler looked up in the UIManager. This implementation
     * looks for a String registered with a key of CalendarHeaderHandler.uiControllerID. If
     * found it assumes that the value is the class name of the handler and tries
     * to instantiate the handler.
     *
     * @return a CalendarHeaderHandler from the UIManager or null if none
     *   available or instantiation failed.
     */
    protected CalendarHeaderHandler getHeaderFromUIManager() {
        Object handlerClass = UIManager.get(CalendarHeaderHandler.uiControllerID);
        if (handlerClass instanceof String) {
            return instantiateClass((String) handlerClass);
        }
        return null;
    }

    /**
     * @param handlerClassName
     * @return
     */
    private CalendarHeaderHandler instantiateClass(String handlerClassName) {
        Class<?> handler = null;
        try {
            handler = Class.forName(handlerClassName);
            return instantiateClass(handler);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         return null;
    }

    /**
     * @param handlerClass
     * @return
     */
    private CalendarHeaderHandler instantiateClass(Class<?> handlerClass) {
        Constructor<?> constructor = null;
        try {
            constructor = handlerClass.getConstructor();
        } catch (SecurityException e) {
            LOG.finer("cant instantiate CalendarHeaderHandler (security) " + handlerClass);
        } catch (NoSuchMethodException e) {
            LOG.finer("cant instantiate CalendarHeaderHandler (missing parameterless constructo?)" + handlerClass);
        }
        if (constructor != null) {
            try {
                return (CalendarHeaderHandler) constructor.newInstance();
            } catch (IllegalArgumentException e) {
                LOG.finer("cant instantiate CalendarHeaderHandler (missing parameterless constructo?)" + handlerClass);
            } catch (InstantiationException e) {
                LOG.finer("cant instantiate CalendarHeaderHandler (not instantiable) " + handlerClass);
            } catch (IllegalAccessException e) {
                LOG.finer("cant instantiate CalendarHeaderHandler (constructor not public) " + handlerClass);
            } catch (InvocationTargetException e) {
                LOG.finer("cant instantiate CalendarHeaderHandler (Invocation target)" + handlerClass);
            }
        }
        return null;
    }

    /**
     * @param calendarHeaderHandler the calendarHeaderHandler to set
     */
    protected void setCalendarHeaderHandler(CalendarHeaderHandler calendarHeaderHandler) {
        this.calendarHeaderHandler = calendarHeaderHandler;
    }

    /**
     * @return the calendarHeaderHandler
     */
    protected CalendarHeaderHandler getCalendarHeaderHandler() {
        return calendarHeaderHandler;
    }

}
