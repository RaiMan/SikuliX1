/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.table;

import java.math.BigDecimal;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.text.NumberFormatter;

/**
 * Experiment to work around Issue #1183-swingx: NumberEditorExt throws exception
 * on getCellValue. Remaining issue: no visual error feedback if the expected
 * number type exceeds its range.
 *
 * @author Jeanette Winzenburg
 *
 * @deprecated (pre-1.6.2) moved to text package
 */
@Deprecated
class StrictNumberFormatter extends NumberFormatter {

    private BigDecimal maxAsBig;
    private BigDecimal minAsBig;

    /**
     * @param format
     */
    public StrictNumberFormatter(NumberFormat format) {
        super(format);
    }

    /**
     * {@inheritDoc} <p>
     *
     * Overridden to automatically set the minimum/maximum to the boundaries of
     * the Number type if it corresponds to a raw type, or null if not.
     */
    @Override
    public void setValueClass(Class<?> valueClass) {
        super.setValueClass(valueClass);
        updateMinMax();
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private void updateMinMax() {
        Comparable min = null;
        Comparable max = null;
        if (getValueClass() == Integer.class) {
            max = Integer.MAX_VALUE;
            min = Integer.MIN_VALUE;
        } else if (getValueClass() == Long.class) {
            max = Long.MAX_VALUE;
            min = Long.MIN_VALUE;
        } else if (getValueClass() == Short.class) {
            max = Short.MAX_VALUE;
            min = Short.MIN_VALUE;
        } else if (getValueClass() == Byte.class) {
            max = Byte.MAX_VALUE;
            min = Byte.MIN_VALUE;
        } else if (getValueClass() == Float.class) {
            max = Float.MAX_VALUE;
            min = Float.MIN_VALUE;
        } else if (getValueClass() == Double.class) {
            // don*t understand what's happening here, naive compare with bigDecimal
            // fails - so don't do anything for now
            // JW: revisit!
        }
        setMaximum(max);
        setMinimum(min);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMaximum(Comparable max) {
        super.setMaximum(max);
        this.maxAsBig = max != null ? new BigDecimal(max.toString()) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMinimum(Comparable minimum) {
        super.setMinimum(minimum);
        this.minAsBig = minimum != null ? new BigDecimal(minimum.toString()) : null;
    }

    /**
     * Returns the <code>Object</code> representation of the
     * <code>String</code> <code>text</code>, may be null.
     *
     * @param text <code>String</code> to convert
     * @return <code>Object</code> representation of text
     * @throws ParseException if there is an error in the conversion
     */
    @Override
    public Object stringToValue(String text) throws ParseException {
        Object value = getParsedValue(text, getFormat());
        try {
            if (!isValueInRange(value, true)) {
                throw new ParseException("Value not within min/max range", 0);
            }
        } catch (ClassCastException cce) {
            throw new ParseException("Class cast exception comparing values: "
                                     + cce, 0);
        }
        return convertValueToValueClass(value, getValueClass());
    }

    /**
     * Converts the passed in value to the passed in class. This only
     * works if <code>valueClass</code> is one of <code>Integer</code>,
     * <code>Long</code>, <code>Float</code>, <code>Double</code>,
     * <code>Byte</code> or <code>Short</code> and <code>value</code>
     * is an instanceof <code>Number</code>.
     */
    private Object convertValueToValueClass(Object value, Class<?> valueClass) {
        if (valueClass != null && (value instanceof Number)) {
            if (valueClass == Integer.class) {
                return new Integer(((Number)value).intValue());
            }
            else if (valueClass == Long.class) {
                return new Long(((Number)value).longValue());
            }
            else if (valueClass == Float.class) {
                return new Float(((Number)value).floatValue());
            }
            else if (valueClass == Double.class) {
                return new Double(((Number)value).doubleValue());
            }
            else if (valueClass == Byte.class) {
                return new Byte(((Number)value).byteValue());
            }
            else if (valueClass == Short.class) {
                return new Short(((Number)value).shortValue());
            }
        }
        return value;
    }

    /**
     * Invokes <code>parseObject</code> on <code>f</code>, returning
     * its value.
     */
    private Object getParsedValue(String text, Format f) throws ParseException {
        if (f == null) {
            return text;
        }
        return f.parseObject(text);
    }

    /**
     * Returns true if <code>value</code> is between the min/max.
     *
     * @param wantsCCE If false, and a ClassCastException is thrown in
     *                 comparing the values, the exception is consumed and
     *                 false is returned.
     */
    private boolean isValueInRange(Object orgValue, boolean wantsCCE) {
        if (orgValue == null) return true;
        if ((getMinimum() == null) && getMaximum() == null) return true;

        BigDecimal value = new BigDecimal(orgValue.toString());
        Comparable<BigDecimal> min = getMinimumAsBig();

        try {
            if (min != null && min.compareTo(value) > 0) {
                return false;
            }
        } catch (ClassCastException cce) {
            if (wantsCCE) {
                throw cce;
            }
            return false;
        }

        Comparable<BigDecimal> max = getMaximumAsBig();
        try {
            if (max != null && max.compareTo(value) < 0) {
                return false;
            }
        } catch (ClassCastException cce) {
            if (wantsCCE) {
                throw cce;
            }
            return false;
        }
        return true;
    }

    private Comparable<BigDecimal> getMinimumAsBig() {
        return minAsBig;
    }

    private Comparable<BigDecimal> getMaximumAsBig() {
        return maxAsBig;
    }

}
