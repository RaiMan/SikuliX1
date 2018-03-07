/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.AbstractSpinnerModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.UIManager;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JSpinner.NumberEditor;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.renderer.FormatStringValue;

/**
 * Custom CalendarHeaderHandler which supports year-wise navigation.
 * <p>
 *
 * The custom component used as header component of this implementation contains
 * month-navigation buttons, a label with localized month text and a spinner for
 * .. well ... spinning the years. There is minimal configuration control via
 * the UIManager:
 *
 * <ul>
 * <li>control the position of the nextMonth button: the default is at the
 * trailing edge of the header. Option is to insert it directly after the month
 * text, to enable set a Boolean.TRUE as value for key
 * <code>ARROWS_SURROUNDS_MONTH</code>.
 * <li>control the focusability of the spinner's text field: the default is
 * false. To enable set a Boolean.TRUE as value for key
 * <code>FOCUSABLE_SPINNER_TEXT</code>.
 * </ul>
 *
 * <b>Note</b>: this header is <b>not</b> used by default. To make it the
 * per-application default register it with the UIManager, like
 *
 * <pre><code>
 * UIManager.put(CalendarHeaderHandler.uiControllerID,
 *      "org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler");
 * </code>
 * </pre>
 *
 * PENDING JW: implement and bind actions for keyboard navigation. These are
 * potentially different from navigation by mouse: need to move the selection
 * along with the scrolling?
 *
 */
public class SpinningCalendarHeaderHandler extends CalendarHeaderHandler {

    /**
     * Key for use in UIManager to control the position of the nextMonth arrow.
     */
    public static final String ARROWS_SURROUND_MONTH = "SpinningCalendarHeader.arrowsSurroundMonth";

    /**
     * Key for use in UIManager to control the focusable property of the year
     * spinner.
     */
    public static final String FOCUSABLE_SPINNER_TEXT = "SpinningCalendarHeader.focusableSpinnerText";

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SpinningCalendarHeaderHandler.class.getName());

    /** the spinner model for year-wise navigation. */
    private SpinnerModel yearSpinnerModel;

    /** listener for property changes of the JXMonthView. */
    private PropertyChangeListener monthPropertyListener;

    /** converter for month text. */
    private FormatStringValue monthStringValue;

    // ----------------- public/protected overrides to manage custom
    // creation/config

    /**
     * {@inheritDoc}
     * <p>
     *
     * Overridden to configure header specifics component after calling super.
     */
    @Override
    public void install(JXMonthView monthView) {
        super.install(monthView);
        getHeaderComponent().setActions(
                monthView.getActionMap().get("previousMonth"),
                monthView.getActionMap().get("nextMonth"),
                getYearSpinnerModel());
        componentOrientationChanged();
        monthStringBackgroundChanged();
        fontChanged();
        localeChanged();
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Overridden to cleanup the specifics before calling super.
     */
    @Override
    public void uninstall(JXMonthView monthView) {
        getHeaderComponent().setActions(null, null, null);
        getHeaderComponent().setMonthText("");
        super.uninstall(monthView);
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Convenience override to the type created.
     */
    @Override
    public SpinningCalendarHeader getHeaderComponent() {
        return (SpinningCalendarHeader) super.getHeaderComponent();
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Implemented to create and configure the custom header component.
     */
    @Override
    protected SpinningCalendarHeader createCalendarHeader() {
        SpinningCalendarHeader header = new SpinningCalendarHeader();
        if (Boolean.TRUE.equals(UIManager.getBoolean(FOCUSABLE_SPINNER_TEXT))) {
            header.setSpinnerFocusable(true);
        }
        if (Boolean.TRUE.equals(UIManager.getBoolean(ARROWS_SURROUND_MONTH))) {
            header.setArrowsSurroundMonth(true);
        }
        return header;
    }

    /**
     * {@inheritDoc}
     * <p>
     */
    @Override
    protected void installListeners() {
        super.installListeners();
        monthView.addPropertyChangeListener(getPropertyChangeListener());
    }

    /**
     * {@inheritDoc}
     * <p>
     */
    @Override
    protected void uninstallListeners() {
        monthView.removePropertyChangeListener(getPropertyChangeListener());
        super.uninstallListeners();
    }

    // ---------------- listening/update triggered by changes of the JXMonthView

    /**
     * Updates the formatter of the month text to the JXMonthView's Locale.
     */
    protected void updateFormatters() {
        SimpleDateFormat monthNameFormat = (SimpleDateFormat) DateFormat
                .getDateInstance(DateFormat.SHORT, monthView.getLocale());
        monthNameFormat.applyPattern("MMMM");
        monthStringValue = new FormatStringValue(monthNameFormat);
    }

    /**
     * Updates internal state to monthView's firstDisplayedDay.
     */
    protected void firstDisplayedDayChanged() {
        ((YearSpinnerModel) getYearSpinnerModel()).fireStateChanged();
        getHeaderComponent().setMonthText(
                monthStringValue.getString(monthView.getFirstDisplayedDay()));
    }

    /**
     * Updates internal state to monthView's locale.
     */
    protected void localeChanged() {
        updateFormatters();
        firstDisplayedDayChanged();
    }

    /**
     * Returns the property change listener for use on the monthView. This is
     * lazyly created if not yet done. This implementation listens to changes of
     * firstDisplayedDay and locale property and updates internal state
     * accordingly.
     *
     * @return the property change listener for the monthView, never null.
     */
    private PropertyChangeListener getPropertyChangeListener() {
        if (monthPropertyListener == null) {
            monthPropertyListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("firstDisplayedDay".equals(evt.getPropertyName())) {
                        firstDisplayedDayChanged();
                    } else if ("locale".equals(evt.getPropertyName())) {
                        localeChanged();
                    }

                }

            };
        }
        return monthPropertyListener;
    }

    // ---------------------- methods to back to Spinner model

    /**
     * Returns the current year of the monthView. Callback for spinner model.
     *
     * return the current year of the monthView.
     */
    private int getYear() {
        Calendar cal = monthView.getCalendar();
        return cal.get(Calendar.YEAR);
    }

    /**
     * Returns the previous year of the monthView. Callback for spinner model.
     * <p>
     *
     * PENDING JW: check against lower bound.
     *
     * return the previous year of the monthView.
     */
    private int getPreviousYear() {
        Calendar cal = monthView.getCalendar();
        cal.add(Calendar.YEAR, -1);
        return cal.get(Calendar.YEAR);
    }

    /**
     * Returns the next year of the monthView. Callback for spinner model.
     * <p>
     *
     * PENDING JW: check against upper bound.
     *
     * return the next year of the monthView.
     */
    private int getNextYear() {
        Calendar cal = monthView.getCalendar();
        cal.add(Calendar.YEAR, 1);
        return cal.get(Calendar.YEAR);
    }

    /**
     * Sets the current year of the monthView to the given value. Callback for
     * spinner model.
     *
     * @param value the new value of the year.
     * @return a boolean indicating if a change actually happened.
     */
    private boolean setYear(Object value) {
        int year = ((Integer) value).intValue();
        Calendar cal = monthView.getCalendar();
        if (cal.get(Calendar.YEAR) == year)
            return false;
        cal.set(Calendar.YEAR, year);
        monthView.setFirstDisplayedDay(cal.getTime());
        return true;
    }

    /**
     * Thin-layer implementation of a SpinnerModel which is actually backed by
     * this controller.
     */
    private class YearSpinnerModel extends AbstractSpinnerModel {

        @Override
        public Object getNextValue() {
            return getNextYear();
        }

        @Override
        public Object getPreviousValue() {
            return getPreviousYear();
        }

        @Override
        public Object getValue() {
            return getYear();
        }

        @Override
        public void setValue(Object value) {
            if (setYear(value)) {
                fireStateChanged();
            }
        }

        @Override
        public void fireStateChanged() {
            super.fireStateChanged();
        }

    }

    private SpinnerModel getYearSpinnerModel() {
        if (yearSpinnerModel == null) {
            yearSpinnerModel = new YearSpinnerModel();
        }
        return yearSpinnerModel;
    }

    /**
     * The custom header component controlled and configured by this handler.
     *
     */
    protected static class SpinningCalendarHeader extends JXPanel {
        private AbstractButton prevButton;

        private AbstractButton nextButton;

        private JLabel monthText;

        private JSpinner yearSpinner;

        private boolean surroundMonth;

        public SpinningCalendarHeader() {
            initComponents();
        }

        /**
         * Installs the actions and models to be used by this component.
         *
         * @param prev the action to use for the previous button
         * @param next the action to use for the next button
         * @param model the spinner model to use for the spinner.
         */
        public void setActions(Action prev, Action next, SpinnerModel model) {
            prevButton.setAction(prev);
            nextButton.setAction(next);
            uninstallZoomAction();
            installZoomAction(model);
        }

        /**
         * Sets the focusable property of the spinner's editor's text field.
         *
         * The default value is false.
         *
         * @param focusable the focusable property of the spinner's editor.
         */
        public void setSpinnerFocusable(boolean focusable) {
            ((DefaultEditor) yearSpinner.getEditor()).getTextField()
                    .setFocusable(focusable);
        }

        /**
         * The default value is false.
         *
         * @param surroundMonth
         */
        public void setArrowsSurroundMonth(boolean surroundMonth) {
            if (this.surroundMonth == surroundMonth)
                return;
            this.surroundMonth = surroundMonth;
            removeAll();
            addComponents();
        }

        /**
         * Sets the text to use for the month label.
         *
         * @param text the text to use for the month label.
         */
        public void setMonthText(String text) {
            monthText.setText(text);
        }

        /**
         * {@inheritDoc}
         * <p>
         *
         * Overridden to set the font of its child components.
         */
        @Override
        public void setFont(Font font) {
            super.setFont(font);
            if (monthText != null) {
                monthText.setFont(font);
                yearSpinner.setFont(font);
                yearSpinner.getEditor().setFont(font);
                ((DefaultEditor) yearSpinner.getEditor()).getTextField()
                        .setFont(font);
            }
        }

        /**
         * {@inheritDoc}
         * <p>
         *
         * Overridden to set the background of its child compenents.
         */
        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            for (int i = 0; i < getComponentCount(); i++) {
                getComponent(i).setBackground(bg);
            }
            if (yearSpinner != null) {
                yearSpinner.setBackground(bg);
                yearSpinner.getEditor().setBackground(bg);
                ((DefaultEditor) yearSpinner.getEditor()).getTextField()
                        .setBackground(bg);
            }
        }

        private void installZoomAction(SpinnerModel model) {
            if (model == null)
                return;
            yearSpinner.setModel(model);
        }

        private void uninstallZoomAction() {
        }

        private void initComponents() {
            createComponents();
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            addComponents();
        }

        /**
         *
         */
        private void addComponents() {
            if (surroundMonth) {
                add(prevButton);
                add(monthText);
                add(nextButton);
                add(Box.createHorizontalStrut(5));
                add(yearSpinner);
            } else {
                add(prevButton);
                add(Box.createHorizontalGlue());
                add(monthText);
                add(Box.createHorizontalStrut(5));
                add(yearSpinner);
                add(Box.createHorizontalGlue());
                add(nextButton);
            }
        }

        /**
         *
         */
        private void createComponents() {
            prevButton = createNavigationButton();
            nextButton = createNavigationButton();
            monthText = createMonthText();
            yearSpinner = createSpinner();
        }

        private JLabel createMonthText() {
            JLabel comp = new JLabel() {

                @Override
                public Dimension getMaximumSize() {
                    Dimension dim = super.getMaximumSize();
                    dim.width = Integer.MAX_VALUE;
                    dim.height = Integer.MAX_VALUE;
                    return dim;
                }

            };
            comp.setHorizontalAlignment(JLabel.CENTER);
            return comp;
        }

        /**
         * Creates and returns the JSpinner used for year navigation.
         *
         * @return
         */
        private JSpinner createSpinner() {
            JSpinner spinner = new JSpinner();
            spinner.setFocusable(false);
            spinner.setBorder(BorderFactory.createEmptyBorder());
            NumberEditor editor = new NumberEditor(spinner);
            editor.getFormat().setGroupingUsed(false);
            editor.getTextField().setFocusable(false);
            spinner.setEditor(editor);
            return spinner;
        }

        private AbstractButton createNavigationButton() {
            JXHyperlink b = new JXHyperlink();
            b.setContentAreaFilled(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setRolloverEnabled(true);
            b.setFocusable(false);
            return b;
        }

    }

}
