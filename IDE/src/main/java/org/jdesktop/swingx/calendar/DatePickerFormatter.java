/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.JFormattedTextField;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.util.Contract;

/**
 * Default formatter for the JXDatePicker component.
 * It can handle a variety of date formats.
 *
 * @author Joshua Outwater
 */
public class DatePickerFormatter extends
        JFormattedTextField.AbstractFormatter {

    private static final Logger LOG = Logger
            .getLogger(DatePickerFormatter.class.getName());
    private DateFormat _formats[] = null;

    /**
     * Instantiates a formatter with the localized format patterns defined
     * in the swingx.properties.
     *
     * These formats are localizable and fields may be re-arranged, such as
     * swapping the month and day fields.  The keys for localizing these fields
     * are:
     * <ul>
     * <li>JXDatePicker.longFormat
     * <li>JXDatePicker.mediumFormat
     * <li>JXDatePicker.shortFormat
     * </ul>
     *
     */
    public DatePickerFormatter() {
        this(null, null);
    }

    /**
     * Instantiates a formatter with the given date formats. If the
     * array is null, default formats are created from the localized
     * patterns in swingx.properties. If empty?
     *
     * @param formats the array of formats to use. May be null to
     *   use defaults or empty to do nothing (?), but must not contain
     *   null formats.
     */
    public DatePickerFormatter(DateFormat formats[]) {
        this(formats, null);
    }

    /**
     * Instantiates a formatter with default date formats in the
     * given locale. The default formats are created from the localized
     * patterns in swingx.properties.
     *
     * @param locale the Locale the use for the default formats.
     */
    public DatePickerFormatter(Locale locale) {
        this(null, locale);
    }

    /**
     * Instantiates a formatter with the given formats and locale.
     *
     * PENDING JW: makes no sense as a public constructor because the locale is ignored
     * if the formats are null. So has same public behaviour as the constructor with
     * formats only ...
     *
     * @param formats
     * @param locale
     */
    public DatePickerFormatter(DateFormat formats[], Locale locale) {
//        super();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        if (formats == null) {
            formats = createDefaultFormats(locale);
        }
        Contract.asNotNull(formats, "The array of DateFormats must not contain null formats");
        _formats = formats;
    }

    /**
     * Returns an array of the formats used by this formatter.
     *
     * @return the formats used by this formatter, guaranteed to be
     *   not null.
     */
    public DateFormat[] getFormats() {
        DateFormat[] results = new DateFormat[_formats.length];
        System.arraycopy(_formats, 0, results, 0, results.length);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object stringToValue(String text) throws ParseException {
        Object result = null;
        ParseException pex = null;

        if (text == null || text.trim().length() == 0) {
            return null;
        }

        // If the current formatter did not work loop through the other
        // formatters and see if any of them can parse the string passed
        // in.
        for (DateFormat _format : _formats) {
            try {
                result = (_format).parse(text);
                pex = null;
                break;
            } catch (ParseException ex) {
                pex = ex;
            }
        }

        if (pex != null) {
            throw pex;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String valueToString(Object value) throws ParseException {
         if ((value != null) && (_formats.length > 0)){
            return _formats[0].format(value);
        }
        return null;
    }

    /**
     * Creates and returns the localized default formats. First tries to
     * add formats created using the patterns stored in the UIManager. If
     * there are no patterns, use the DateFormat's instance with style
     * DateFormat.SHORT.
     *
     * @return the localized default formats.
     */
    protected DateFormat[] createDefaultFormats(Locale locale) {
        List<DateFormat> f = new ArrayList<DateFormat>();
        addFormat(f, "JXDatePicker.longFormat", locale);
        addFormat(f, "JXDatePicker.mediumFormat", locale);
        addFormat(f, "JXDatePicker.shortFormat", locale);
        if (f.size() == 0) {
           addSystemDefaultFormat(f, locale);
        }
        return f.toArray(new DateFormat[f.size()]);
    }

    /**
     * Adds the system's default DateFormat. This implementation adds a
     * dateInstance of style DateFormat.SHORT.
     *
     * @param f the List of formats to add to
     * @param locale the Locale to use for the formatter.
     */
    private void addSystemDefaultFormat(List<DateFormat> f, Locale locale) {
        f.add(DateFormat.getDateInstance(DateFormat.SHORT, locale));
    }

    /**
     * Creates and adds a DateFormat to the given list. Looks up
     * a format pattern registered in the UIManager for the given
     * key and tries to create a SimpleDateFormat. Does nothing
     * if there is no format pattern registered or the pattern is
     * invalid.
     *
     * @param f the list of formats
     * @param key the key for getting the pattern from the UI
     */
    private void addFormat(List<DateFormat> f, String key, Locale locale) {
        String pattern = UIManagerExt.getString(key, locale);
        if (pattern == null) return;
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
            f.add(format);
        } catch (RuntimeException e) {
            // format string  not available or invalid
            LOG.finer("creating date format failed for key/pattern: " + key + "/" + pattern);
        }
    }

    /**
     *
     * Same as DatePickerFormatter, but tagged as UIResource.
     *
     * @author Jeanette Winzenburg
     */
    public static class DatePickerFormatterUIResource extends DatePickerFormatter
        implements UIResource {

        /**
         * @param locale
         */
        public DatePickerFormatterUIResource(Locale locale) {
            super(locale);
        }

        /**
         *
         */
        public DatePickerFormatterUIResource() {
            this(null);
        }

        public DatePickerFormatterUIResource(DateFormat[] formats, Locale locale) {
            super(formats, locale);
        }
    }
}
