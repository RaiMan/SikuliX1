/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.renderer;

import javax.swing.Icon;

/**
 * Compound implementation of XXValue. Currently, XX stands for String,
 * Icon, Boolean. <p>
 *
 * Quick hack around #590-swingx: LabelProvider should respect StringValue
 * when formatting (instead of going clever with icons).
 *
 * Note: this will change!
 *
 * @see CheckBoxProvider
 */
public class MappedValue implements StringValue, IconValue, BooleanValue {

    private StringValue stringDelegate;
    private IconValue iconDelegate;
    private BooleanValue booleanDelegate;

    public MappedValue(StringValue stringDelegate, IconValue iconDelegate) {
        this(stringDelegate, iconDelegate, null);
    }

    public MappedValue(StringValue stringDelegate, IconValue iconDelegate,
            BooleanValue booleanDelegate) {
        this.stringDelegate = stringDelegate;
        this.iconDelegate = iconDelegate;
        this.booleanDelegate = booleanDelegate;
    }

    /**
     * {@inheritDoc}<p>
     *
     * This implementation delegates to the contained StringValue if available or
     * returns an empty String, if not.
     *
     */
    @Override
    public String getString(Object value) {
        if (stringDelegate != null) {
            return stringDelegate.getString(value);
        }
        return "";
    }

    /**
     * {@inheritDoc}<p>
     *
     * This implementation delegates to the contained IconValue if available or
     * returns null, if not.
     *
     */
    @Override
    public Icon getIcon(Object value) {
        if (iconDelegate != null) {
            return iconDelegate.getIcon(value);
        }
        return null;
    }

    /**
     * {@inheritDoc}<p>
     *
     * This implementation delegates to the contained BooleanValue if available or
     * returns false, if not.
     *
     */
    @Override
    public boolean getBoolean(Object value) {
        if (booleanDelegate != null) {
            return booleanDelegate.getBoolean(value);
        }
        return false;
    }

}
