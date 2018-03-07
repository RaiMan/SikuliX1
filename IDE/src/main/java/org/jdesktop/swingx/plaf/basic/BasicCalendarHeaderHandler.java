/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;

/**
 * Custom implementation of a CalendarHeaderHandler in preparation of a vista-style
 * calendar. Does nothing yet.
 *
 * @author Jeanette Winzenburg
 */
public class BasicCalendarHeaderHandler extends CalendarHeaderHandler {

    @Override
    public void install(JXMonthView monthView) {
        super.install(monthView);
        getHeaderComponent().setActions(monthView.getActionMap().get("previousMonth"),
                monthView.getActionMap().get("nextMonth"),
                monthView.getActionMap().get("zoomOut"));

    }


    @Override
    protected void installNavigationActions() {
        // TODO Auto-generated method stub
        super.installNavigationActions();
        ZoomOutAction zoomOutAction = new ZoomOutAction();
        zoomOutAction.setTarget(monthView);
        monthView.getActionMap().put("zoomOut", zoomOutAction);
    }


    @Override
    public void uninstall(JXMonthView monthView) {
        getHeaderComponent().setActions(null, null, null);
        super.uninstall(monthView);
    }

    @Override
    public BasicCalendarHeader getHeaderComponent() {
        // TODO Auto-generated method stub
        return (BasicCalendarHeader) super.getHeaderComponent();
    }

    @Override
    protected BasicCalendarHeader createCalendarHeader() {
        return new BasicCalendarHeader();
    }

    /**
     * Quick fix for Issue #1046-swingx: header text not updated if zoomable.
     *
     */
    protected static class ZoomOutAction extends AbstractHyperlinkAction<JXMonthView> {

        private PropertyChangeListener linkListener;
        // Formatters/state used by Providers.
        /** Localized month strings used in title. */
        private String[] monthNames;
        private StringValue tsv ;

        public ZoomOutAction() {
            super();
            tsv = new StringValue() {

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
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub

        }

        /**
         * installs a propertyChangeListener on the target and
         * updates the visual properties from the target.
         */
        @Override
        protected void installTarget() {
            if (getTarget() != null) {
                getTarget().addPropertyChangeListener(getTargetListener());
            }
            updateLocale();
            updateFromTarget();
        }

        /**
         *
         */
        private void updateLocale() {
            Locale current = getTarget() != null ? getTarget().getLocale() : Locale.getDefault();
            monthNames = DateFormatSymbols.getInstance(current).getMonths();
        }

        /**
         * removes the propertyChangeListener. <p>
         *
         * Implementation NOTE: this does not clean-up internal state! There is
         * no need to because updateFromTarget handles both null and not-null
         * targets. Hmm...
         *
         */
        @Override
        protected void uninstallTarget() {
            if (getTarget() == null) return;
            getTarget().removePropertyChangeListener(getTargetListener());
        }

        protected void updateFromTarget() {
            // this happens on construction with null target
            if (tsv == null) return;
            Calendar calendar = getTarget() != null ? getTarget().getCalendar() : null;
            setName(tsv.getString(calendar));
        }

        private PropertyChangeListener getTargetListener() {
            if (linkListener == null) {
             linkListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("firstDisplayedDay".equals(evt.getPropertyName())) {
                        updateFromTarget();
                    } else if ("locale".equals(evt.getPropertyName())) {
                        updateLocale();
                        updateFromTarget();
                    }
                }

            };
            }
            return linkListener;
        }

    }

    /**
     * Active header for a JXMonthView in zoomable mode.<p>
     *
     *  PENDING JW: very much work-in-progress.
     */
    static class BasicCalendarHeader extends JXPanel {

        protected AbstractButton prevButton;
        protected AbstractButton nextButton;
        protected JXHyperlink zoomOutLink;

        public BasicCalendarHeader() {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            prevButton = createNavigationButton();
            nextButton = createNavigationButton();
            zoomOutLink = createZoomLink();
            add(prevButton);
            add(Box.createHorizontalGlue());
            add(zoomOutLink);
            add(Box.createHorizontalGlue());
            add(nextButton);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        }

        /**
         * Sets the actions for backward, forward and zoom out navigation.
         *
         * @param prev
         * @param next
         * @param zoomOut
         */
        public void setActions(Action prev, Action next, Action zoomOut) {
            prevButton.setAction(prev);
            nextButton.setAction(next);
            zoomOutLink.setAction(zoomOut);
        }

        /**
         * {@inheritDoc} <p>
         *
         * Overridden to set the font of the zoom hyperlink.
         */
        @Override
        public void setFont(Font font) {
            super.setFont(font);
            if (zoomOutLink != null)
                zoomOutLink.setFont(font);
        }

        private JXHyperlink createZoomLink() {
            JXHyperlink zoomOutLink = new JXHyperlink();
            Color textColor = new Color(16, 66, 104);
            zoomOutLink.setUnclickedColor(textColor);
            zoomOutLink.setClickedColor(textColor);
            zoomOutLink.setFocusable(false);
            return zoomOutLink;
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
