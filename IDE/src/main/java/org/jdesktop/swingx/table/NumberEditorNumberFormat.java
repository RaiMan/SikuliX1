/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.table;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * A specialised Format for the NumberEditor that returns a null for empty
 * strings.
 *
 * @author Noel Grandin
 *
 * @deprecated (pre-1.6.2) replaced by NumberEditorExt, no longer used internally
 */
@Deprecated
class NumberEditorNumberFormat extends Format {
    private final NumberFormat childFormat;

    public NumberEditorNumberFormat(NumberFormat childFormat) {
        if (childFormat == null) {
            childFormat = NumberFormat.getInstance();
        }
        this.childFormat = childFormat;
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj == null)
            return new AttributedString("").getIterator();
        return childFormat.formatToCharacterIterator(obj);
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos) {
        if (obj == null)
            return new StringBuffer("");
        return childFormat.format(obj, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        if (source == null) {
            pos.setIndex(1); // otherwise Format thinks parse failed
            return null;
        }
        if (source.trim().equals("")) {
            pos.setIndex(1); // otherwise Format thinks parse failed
            return null;
        }
        Object val = childFormat.parseObject(source, pos);
        /*
         * The default behaviour of Format objects is to keep parsing as long as
         * they encounter valid data. By for table editing we don't want
         * trailing bad data to be considered a "valid value". So set the index
         * to 0 so that the parse(Object) method knows that we had an error.
         */
        if (pos.getIndex() != source.length()) {
            pos.setErrorIndex(pos.getIndex());
            pos.setIndex(0);
        }
        return val;
    }
}
