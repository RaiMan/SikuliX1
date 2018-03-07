/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.border.IconBorder;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.renderer.CellContext;

/**
 * MonthView specific CellContext. This is internally used by BasisMonthViewUI rendering.
 *
 * @author Jeanette Winzenburg
 */
class CalendarCellContext extends CellContext {

    /**
     * The padding for month traversal icons.
     * PENDING JW: decouple rendering and hit-detection. As is, these are
     * hard-coded "magic numbers" which must be the same in both
     * the ui-delegate (which does the hit-detection) and here (which
     * returns the default title border)
     *
     * Added as preliminary fix for #1028-swingx: title border incorrect if box-padding 0
     */
    private int arrowPaddingX = 3;
    private int arrowPaddingY = 3;

    private CalendarState dayState;

    public void installContext(JXMonthView component, Calendar value,
            boolean selected, boolean focused, CalendarState dayState) {
        this.component = component;
        this.dayState = dayState;
        installState(value, -1, -1, selected, focused, true, true);
    }

    @Override
    public JXMonthView getComponent() {
        return (JXMonthView) super.getComponent();
    }

    public CalendarState getCalendarState() {
        return dayState;
    }

    public Calendar getCalendar() {
        return (getValue() instanceof Calendar) ? (Calendar) getValue() : null;
    }

    @Override
    protected Color getForeground() {
        if (CalendarState.LEADING == dayState) {
            return getUIColor("leadingDayForeground");
        }
        if (CalendarState.TRAILING == dayState) {
            return getUIColor("trailingDayForeground");
        }
        if ((CalendarState.TITLE == dayState) && (getComponent() != null)) {
            return getComponent().getMonthStringForeground();
        }
        if (CalendarState.WEEK_OF_YEAR == dayState) {
            Color weekOfTheYearForeground = getUIColor("weekOfTheYearForeground");
            if (weekOfTheYearForeground != null) {
                return weekOfTheYearForeground;
            }
        }
        if (CalendarState.DAY_OF_WEEK == dayState) {
            Color daysOfTheWeekForeground = getComponent() != null
                ? getComponent().getDaysOfTheWeekForeground() : null;
            if (daysOfTheWeekForeground != null) {
                return daysOfTheWeekForeground;
            }
        }

        Color flaggedOrPerDayForeground = getFlaggedOrPerDayForeground();
        return flaggedOrPerDayForeground != null ? flaggedOrPerDayForeground : super.getForeground();
    }

    /**
     * @param key
     * @return
     */
    private Color getUIColor(String key) {
        return UIManagerExt.getColor(getUIPrefix() + key);
    }

    /**
     * Returns the special color used for flagged days or per weekday or null if none is
     * set, the component or the calendar are null.
     *
     * @return the special foreground color for flagged days or per dayOfWeek.
     */
    protected Color getFlaggedOrPerDayForeground() {

        if (getComponent() != null && (getCalendar() != null)) {
            if (getComponent().isFlaggedDate(getCalendar().getTime())) {
                return getComponent().getFlaggedDayForeground();
            } else {
                Color perDay = getComponent().getPerDayOfWeekForeground(getCalendar().get(Calendar.DAY_OF_WEEK));
                if (perDay != null) {
                    return perDay;
                }

            }
        }
        return null;
    }

    @Override
    protected Color getBackground() {
        if ((CalendarState.TITLE == dayState) && (getComponent() != null)) {
            return getComponent().getMonthStringBackground();
        }
        return super.getBackground();
    }

    @Override
    protected Color getSelectionBackground() {
        if (CalendarState.LEADING == dayState || CalendarState.TRAILING == dayState) return getBackground();
        return getComponent() != null ? getComponent().getSelectionBackground() : null;
    }

    @Override
    protected Color getSelectionForeground() {
        if (CalendarState.LEADING == dayState || CalendarState.TRAILING == dayState) return getForeground();
        Color flaggedOrPerDayForeground = getFlaggedOrPerDayForeground();
        if (flaggedOrPerDayForeground != null) {
            return flaggedOrPerDayForeground;
        }
        return getComponent() != null ? getComponent().getSelectionForeground() : null;
    }


    @Override
    protected Border getBorder() {
        if (getComponent() == null) {
            return super.getBorder();
        }
        if (CalendarState.TITLE == dayState) {
            return getTitleBorder();
        }
        if (isToday()) {
            int x = getComponent().getBoxPaddingX();
            int y = getComponent().getBoxPaddingY();
           Border todayBorder = BorderFactory.createLineBorder(getComponent().getTodayBackground());
           Border empty = BorderFactory.createEmptyBorder(y - 1, x - 1, y - 1, x -1);
           return BorderFactory.createCompoundBorder(todayBorder, empty);
        }
        return BorderFactory.createEmptyBorder(getComponent().getBoxPaddingY(), getComponent().getBoxPaddingX(), getComponent().getBoxPaddingY(), getComponent().getBoxPaddingX());
    }

    /**
     * @return
     */
    private Border getTitleBorder() {
        if (getComponent().isTraversable()) {
            Icon downIcon = UIManager.getIcon("JXMonthView.monthDownFileName");
            Icon upIcon = UIManager.getIcon("JXMonthView.monthUpFileName");

            // fix for #1028-swingx: title border whacky for boxpadding 0
            // in fact there had been a deeper issue - without using the arrowPadding here
            // the hit-detection of the buttons is slightly off target
            IconBorder up = new IconBorder(upIcon, SwingConstants.EAST, arrowPaddingX);
            IconBorder down = new IconBorder(downIcon, SwingConstants.WEST, arrowPaddingX);
            Border compound = BorderFactory.createCompoundBorder(up, down);
            Border empty = BorderFactory.createEmptyBorder(2* arrowPaddingY, 0, 2*arrowPaddingY, 0);
            return BorderFactory.createCompoundBorder(compound, empty);
        }

        return BorderFactory.createEmptyBorder(getComponent().getBoxPaddingY(), getComponent().getBoxPaddingX(), getComponent().getBoxPaddingY(), getComponent().getBoxPaddingX());
    }

    /**
     * @return
     */
    protected boolean isToday() {
        return CalendarState.TODAY == dayState;
    }

    @Override
    protected String getUIPrefix() {
        return "JXMonthView.";
    }

}
