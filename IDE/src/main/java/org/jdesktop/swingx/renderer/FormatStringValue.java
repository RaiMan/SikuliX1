/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import java.text.Format;

/**
 * Base type for <code>Format</code>-backed <code>StringValue</code>. Has
 * static defaults for Date and Number which use the locale-dependent default
 * <code>Format</code>s as returned from xxFormat.getInstance().
 * <p>
 *
 * This class is intended to ease the handling of formatted cell content.
 * F.i. to show a list of <code>Date</code>s in the default
 * <code>Locale</code>'s FULL version and right align the text:
 *
 * <pre><code>
 *    StringValue stringValue = new FormatStringValue(
 *        DateFormat.getInstance(DateFormat.FULL));
 *    list.setCellRenderer(
 *        new DefaultListRenderer(stringValue, JLabel.RIGHT);
 * </code></pre>
 *
 *
 * PENDING: need to update on Locale change? How to detect? When?
 *
 * @author Jeanette Winzenburg
 */
public class FormatStringValue implements StringValue {

    /** the format used in creating the String representation. */
    protected Format format;

    /**
     * Instantiates a formatted converter with null format.
     *
     */
    public FormatStringValue() {
        this(null);
    }

    /**
     * Instantiates a formatted converter with the given Format.
     *
     * @param format the format to use in creating the String representation.
     */
    public FormatStringValue(Format format) {
       this.format = format;
    }

    /**
     *
     * @return the format used in creating the String representation.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(Object value) {
        if (value == null) return "";
        if (format != null) {
            try {
                return format.format(value);
            } catch (IllegalArgumentException e) {
                // didn't work, nothing we can do
            }
        }
        return value.toString();
    }

}
