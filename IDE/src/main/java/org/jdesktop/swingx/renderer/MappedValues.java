/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import javax.swing.Icon;
import javax.swing.plaf.UIResource;

/**
 * A collection of common {@code MappedValue} implementations.
 *
 * @author kschaefer
 */
public final class MappedValues {
    /**
     * A {@code MappedValue} that returns either a {@code String} or {@code Icon}, but not both.
     */
    @SuppressWarnings("serial")
    public static final MappedValue STRING_OR_ICON_ONLY = new MappedValue(new StringValue() {
        @Override
        public String getString(Object value) {
            if (value instanceof Icon) {
                return StringValues.EMPTY.getString(value);
            }

            return StringValues.TO_STRING.getString(value);
        }
    }, IconValues.ICON);

    /**
     * MappedValue wrapper of type UIResource to tag LAF installed converters.
     *
     * @author (Jeanette Winzenburg, Berlin
     */
    public static class MappedValueUIResource extends MappedValue implements UIResource {

        public MappedValueUIResource(MappedValue delegate) {
            this(delegate, delegate, delegate);
        }

        /**
         * @param stringDelegate
         * @param iconDelegate
         * @param booleanDelegate
         */
        public MappedValueUIResource(StringValue stringDelegate,
                IconValue iconDelegate, BooleanValue booleanDelegate) {
            super(stringDelegate, iconDelegate, booleanDelegate);
        }

        /**
         * @param stringDelegate
         * @param iconDelegate
         */
        public MappedValueUIResource(StringValue stringDelegate,
                IconValue iconDelegate) {
            super(stringDelegate, iconDelegate);
        }

    }

    private MappedValues() {
        //prevent instantiation
    }
}
