/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.FormatStringValue;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;

/**
 * The RenderingHandler responsible for text rendering. It provides
 * and configures a rendering component for the given cell of
 * a JXMonthView. <p>
 *
 * Note: exposing the createXXStringValue methods is an emergency workaround for
 * Issue #1062-swingx (core doesn't use arabic digits where appropriate) to allow
 * subclasses to do better than core. So beware of future changes!
 *
 */
class BasicCalendarRenderingHandler implements CalendarRenderingHandler {
    /** The CellContext for content and default visual config. */
    private CalendarCellContext cellContext;
    /** The providers to use per DayState. */
    private Map<CalendarState, ComponentProvider<?>> providers;
    //-------- Highlight properties
    /** The Painter used for highlighting unselectable dates. */
    private TextCrossingPainter<?> textCross;
    /** The foreground color for unselectable date highlight. */
    private Color unselectableDayForeground;

    /**
     * Instantiates a RenderingHandler and installs default state.
     */
    public BasicCalendarRenderingHandler() {
        install();
    }

    private void install() {
        unselectableDayForeground = UIManagerExt.getColor("JXMonthView.unselectableDayForeground");
        textCross = new TextCrossingPainter<JLabel>();
        cellContext = new CalendarCellContext();
        installProviders();
    }

    /**
     * Creates and stores ComponentProviders for all DayStates.
     */
    private void installProviders() {
        providers = new HashMap<CalendarState, ComponentProvider<?>>();

        StringValue sv = createDayStringValue(null);
        ComponentProvider<?> provider = new LabelProvider(sv, JLabel.RIGHT);
        providers.put(CalendarState.IN_MONTH, provider);
        providers.put(CalendarState.TODAY, provider);
        providers.put(CalendarState.TRAILING, provider);
        providers.put(CalendarState.LEADING, provider);

        StringValue wsv = createWeekOfYearStringValue(null);
        ComponentProvider<?> weekOfYearProvider = new LabelProvider(wsv,
                JLabel.RIGHT);
        providers.put(CalendarState.WEEK_OF_YEAR, weekOfYearProvider);

        ComponentProvider<?> dayOfWeekProvider = new LabelProvider(JLabel.CENTER) {

            @Override
            protected String getValueAsString(CellContext context) {
                Object value = context.getValue();
                // PENDING JW: this is breaking provider's contract in its
                // role as StringValue! Don't in the general case.
                if (value instanceof Calendar) {
                    int day = ((Calendar) value).get(Calendar.DAY_OF_WEEK);
                    return ((JXMonthView) context.getComponent()).getDayOfTheWeek(day);
                }
                return super.getValueAsString(context);
            }

        };
        providers.put(CalendarState.DAY_OF_WEEK, dayOfWeekProvider);

        StringValue tsv = createMonthHeaderStringValue(null);
        ComponentProvider<?> titleProvider = new LabelProvider(tsv,
                JLabel.CENTER);
        providers.put(CalendarState.TITLE, titleProvider);
    }

    /**
     * Creates and returns a StringValue used for rendering the title of a month box.
     * The input they are assumed to handle is a Calendar configured to a day of
     * the month to render.
     *
     * @param locale the Locale to use, might be null to indicate usage of the default
     *   Locale
     * @return a StringValue appropriate for rendering month title.
     */
    protected StringValue createMonthHeaderStringValue(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String[] monthNames = DateFormatSymbols.getInstance(locale).getMonths();
        StringValue tsv = new StringValue() {

            @Override
            public String getString(Object value) {
                if (value instanceof Calendar) {
                    String month = monthNames[((Calendar) value)
                            .get(Calendar.MONTH)];
                    return month + " "
                            + ((Calendar) value).get(Calendar.YEAR);
                }
                return StringValues.TO_STRING.getString(value);
            }

        };
        return tsv;
    }

    /**
     * Creates and returns a StringValue used for rendering the week of year.
     * The input they are assumed to handle is a Calendar configured to a day of
     * the week to render.
     *
     * @param locale the Locale to use, might be null to indicate usage of the default
     *   Locale
     * @return a StringValue appropriate for rendering week of year.
     */
    protected StringValue createWeekOfYearStringValue(Locale locale) {
        StringValue wsv = new StringValue() {

            @Override
            public String getString(Object value) {
                if (value instanceof Calendar) {
                    value = ((Calendar) value).get(Calendar.WEEK_OF_YEAR);
                }
                return StringValues.TO_STRING.getString(value);
            }

        };
        return wsv;
    }

    /**
     * Creates and returns a StringValue used for rendering days in a month.
     * The input they are assumed to handle is a Calendar configured to the day.
     *
     * @param locale the Locale to use, might be null to indicate usage of the default
     *   Locale
     * @return a StringValue appropriate for rendering days in a month
     */
    protected StringValue createDayStringValue(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        FormatStringValue sv = new FormatStringValue(new SimpleDateFormat("d", locale)) {

            @Override
            public String getString(Object value) {
                if (value instanceof Calendar) {
                    ((DateFormat) getFormat()).setTimeZone(((Calendar) value).getTimeZone());
                    value = ((Calendar) value).getTime();
                }
                return super.getString(value);
            }

        };
        return sv;
    }

    /**
     * Updates internal state to the given Locale.
     *
     * @param locale the new Locale.
     */
    @Override
    public void setLocale(Locale locale) {
        StringValue dayValue = createDayStringValue(locale);
        providers.get(CalendarState.IN_MONTH).setStringValue(dayValue);
        providers.get(CalendarState.TODAY).setStringValue(dayValue);
        providers.get(CalendarState.TRAILING).setStringValue(dayValue);
        providers.get(CalendarState.LEADING).setStringValue(dayValue);

        providers.get(CalendarState.WEEK_OF_YEAR).setStringValue(createWeekOfYearStringValue(locale));
        providers.get(CalendarState.TITLE).setStringValue(createMonthHeaderStringValue(locale));
    }

    /**
     * Configures and returns a component for rendering of the given monthView cell.
     *
     * @param monthView the JXMonthView to render onto
     * @param calendar the cell value
     * @param dayState the DayState of the cell
     * @return a component configured for rendering the given cell
     */
    @Override
    public JComponent prepareRenderingComponent(JXMonthView monthView, Calendar calendar, CalendarState dayState) {
        cellContext.installContext(monthView, calendar,
                isSelected(monthView, calendar, dayState),
                isFocused(monthView, calendar, dayState),
                dayState);
        JComponent comp = providers.get(dayState).getRendererComponent(cellContext);
        return highlight(comp, monthView, calendar, dayState);
    }

    /**
     *
     * NOTE: it's the responsibility of the CalendarCellContext to detangle
     * all "default" (that is: which could be queried from the comp and/or UIManager)
     * foreground/background colors based on the given state! Moved out off here.
     * <p>
     * PENDING JW: replace hard-coded logic by giving over to highlighters.
     *
     * @param monthView the JXMonthView to render onto
     * @param calendar the cell value
     * @param dayState the DayState of the cell
     * @param dayState
     */
    private JComponent highlight(JComponent comp, JXMonthView monthView,
            Calendar calendar, CalendarState dayState) {
        CalendarAdapter adapter = getCalendarAdapter(monthView, calendar, dayState);
        return (JComponent) getHighlighter().highlight(comp, adapter);
    }

    /**
     * @return
     */
    private Highlighter getHighlighter() {
        if (highlighter == null) {
            highlighter = new CompoundHighlighter();
            installHighlighters();
        }
        return highlighter;
    }

    /**
     *
     */
    private void installHighlighters() {
        HighlightPredicate boldPredicate = new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component renderer,
                    ComponentAdapter adapter) {
                if (!(adapter instanceof CalendarAdapter))
                    return false;
                CalendarAdapter ca = (CalendarAdapter) adapter;
                return CalendarState.DAY_OF_WEEK == ca.getCalendarState() ||
                    CalendarState.TITLE == ca.getCalendarState();
            }

        };
        Highlighter font = new AbstractHighlighter(boldPredicate) {

            @Override
            protected Component doHighlight(Component component,
                    ComponentAdapter adapter) {
                component.setFont(getDerivedFont(component.getFont()));
                return component;
            }

        };
        highlighter.addHighlighter(font);

        HighlightPredicate unselectable = new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component renderer,
                    ComponentAdapter adapter) {
                if (!(adapter instanceof CalendarAdapter))
                    return false;
                return ((CalendarAdapter) adapter).isUnselectable();
            }

        };
        textCross.setForeground(unselectableDayForeground);
        Highlighter painterHL = new PainterHighlighter(unselectable, textCross);
        highlighter.addHighlighter(painterHL);

    }

    /**
     * @param monthView
     * @param calendar
     * @param dayState
     * @return
     */
    private CalendarAdapter getCalendarAdapter(JXMonthView monthView,
            Calendar calendar, CalendarState dayState) {
        if (calendarAdapter == null) {
            calendarAdapter = new CalendarAdapter(monthView);
        }
        return calendarAdapter.install(calendar, dayState);
    }

    private CalendarAdapter calendarAdapter;
    private CompoundHighlighter highlighter;

    /**
     * @param font
     * @return
     */
    private Font getDerivedFont(Font font) {
        return font.deriveFont(Font.BOLD);
    }

    /**
     * @param monthView
     * @param calendar
     * @param dayState
     * @return
     */
    private boolean isFocused(JXMonthView monthView, Calendar calendar,
            CalendarState dayState) {
        return false;
    }

    /**
     * @param monthView the JXMonthView to render onto
     * @param calendar the cell value
     * @param dayState the DayState of the cell
     * @return
     */
    private boolean isSelected(JXMonthView monthView, Calendar calendar,
            CalendarState dayState) {
        if (!isSelectable(dayState)) return false;
        return monthView.isSelected(calendar.getTime());
    }

    /**
     * @param dayState
     * @return
     */
    private boolean isSelectable(CalendarState dayState) {
        return (CalendarState.IN_MONTH == dayState) || (CalendarState.TODAY == dayState);
    }

}
