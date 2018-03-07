/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.util.Contract;

/**
 * A collection of common {@code StringValue} implementations.
 *
 * @author Karl George Schaefer
 * @author Jeanette Winzenburg
 */
public final class StringValues {
    /**
     * A {@code StringValue} that always presents an empty string.
     */
    @SuppressWarnings("serial")
    public final static StringValue EMPTY = new StringValue() {
        @Override
        public String getString(Object value) {
            return "";
        }
    };

    /**
     * A {@code StringValue} that presents a {@link Object#toString() toString}
     * value for the given object. If the value passed is {@code null}, this has
     * the same effect as {@link StringValues#EMPTY}.
     */
    @SuppressWarnings("serial")
    public final static StringValue TO_STRING = new StringValue() {
        @Override
        public String getString(Object value) {
            return (value != null) ? value.toString() : StringValues.EMPTY.getString(value);
        }
    };

    /**
     * A {@code StringValue} that presents the current L&F display name for a
     * given file. If the value passed to {@code FILE_NAME} is not a
     * {@link File}, this has the same effect as {@link StringValues#TO_STRING}.
     */
    @SuppressWarnings("serial")
    public static final StringValue FILE_NAME = new StringValue() {
        @Override
        public String getString(Object value) {
            if (value instanceof File) {
                FileSystemView fsv = FileSystemView.getFileSystemView();

                return fsv.getSystemDisplayName((File) value);
            }

            return StringValues.TO_STRING.getString(value);
        }
    };

    /**
     * A {@code StringValue} that presents the current L&F type name for a
     * given file. If the value passed to {@code FILE_TYPE} is not a
     * {@link File}, this has the same effect as {@link StringValues#TO_STRING}.
     */
    @SuppressWarnings("serial")
    public static final StringValue FILE_TYPE = new StringValue() {
        @Override
        public String getString(Object value) {
            if (value instanceof File) {
                FileSystemView fsv = FileSystemView.getFileSystemView();

                return fsv.getSystemTypeDescription((File) value);
            }

            return StringValues.TO_STRING.getString(value);
        }
    };

    /** keep track of default locale. */
    private static Locale defaultLocale;

    /**
     * Returns a boolean to indicate if the default Locale has changed.
     * Updates internal state to keep track of the default Locale.
     *
     * @return true if the default Locale has changed.
     */
    private static boolean localeChanged() {
        boolean changed = !Locale.getDefault().equals(defaultLocale);
        if (changed) {
            defaultLocale = Locale.getDefault();
        }
        return changed;
    }

    /**
     * Default converter for <code>Date</code> types. Uses the default format
     * as returned from <code>DateFormat</code>.
     */
    @SuppressWarnings("serial")
    public final static FormatStringValue DATE_TO_STRING = new FormatStringValue() {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(Object value) {
            if (format == null || localeChanged()) {
                format = DateFormat.getDateInstance();
            }
            return super.getString(value);
        }

    };

    /**
     * Default converter for <code>Number</code> types. Uses the default format
     * as returned from <code>NumberFormat</code>.
     */
    @SuppressWarnings("serial")
    public final static FormatStringValue NUMBER_TO_STRING = new FormatStringValue() {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getString(Object value) {
            if (format == null || localeChanged()) {
                format = NumberFormat.getNumberInstance();
            }
            return super.getString(value);
        }

    };


    public static final StringValue TO_STRING_UI = new StringValueUIResource(StringValues.TO_STRING);
    public static final StringValue EMPTY_UI = new StringValueUIResource(StringValues.EMPTY);

    /**
     * StringValue wrapper of type UIResource to tag LAF installed converters.
     *
     * @author Jeanette Winzenburg, Berlin
     */
    public static class StringValueUIResource implements StringValue, UIResource {

        private StringValue delegate;

        public StringValueUIResource(StringValue toString) {
            Contract.asNotNull(toString, "delegate StringValue must not be null");
            this.delegate = toString;
        }

        @Override
        public String getString(Object value) {
            return delegate.getString(value);
        }

    }

    private StringValues() {
        // does nothing
    }
}
