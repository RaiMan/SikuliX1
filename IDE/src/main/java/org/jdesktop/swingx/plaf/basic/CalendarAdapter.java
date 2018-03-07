/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.util.Calendar;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.decorator.ComponentAdapter;

/**
 * ComponentAdapter for a JXMonthView (experimental for internal use of BasicMonthViewUI).<p>
 *
 * For now, this effectively disables all notion of row/column coordinates. It's focused
 * on an externally provided date (as Calendar) and CalendarState. Yeah, I know, that's
 * tweaking too much but then, I want to use highlighters which need an adapter...
 *
 *
 * @author Jeanette Winzenburg
 */
class CalendarAdapter extends ComponentAdapter {

    Calendar calendar;
    CalendarState dayState;

    /**
     * @param component
     */
    public CalendarAdapter(JXMonthView component) {
        super(component);
    }

    /**
     * @param calendar2
     * @param dayState2
     * @return
     */
    public CalendarAdapter install(Calendar calendar, CalendarState dayState) {
        this.calendar = calendar;
        this.dayState = dayState;
        return this;
    }

    @Override
    public JXMonthView getComponent() {
        return (JXMonthView) super.getComponent();
    }

    public CalendarState getCalendarState() {
        return dayState;
    }

    public boolean isFlagged() {
        if (getComponent() == null || calendar == null) {
            return false;
        }
        return getComponent().isFlaggedDate(calendar.getTime());
    }

    public boolean isUnselectable() {
        if (getComponent() == null || calendar == null || !isSelectable()) {
            return false;
        }
        return getComponent().isUnselectableDate(calendar.getTime());
    }

    /**
     * @param dayState
     * @return
     */
    private boolean isSelectable() {
        return (CalendarState.IN_MONTH == getCalendarState()) || (CalendarState.TODAY == getCalendarState());
    }

    @Override
    public boolean isSelected() {
        if (getComponent() == null || calendar == null) {
            return false;
        }
        return getComponent().isSelected(calendar.getTime());
    }

    @Override
    public Object getFilteredValueAt(int row, int column) {
        return getValueAt(row, column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        return calendar;
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
