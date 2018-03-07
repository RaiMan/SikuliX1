/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.multislider;

import java.util.EventObject;

/**
 *
 * @author jm158417
 */
public class ThumbDataEvent extends EventObject {
    private int type, index;
    private Thumb<?> thumb;

    /** Creates a new instance of ThumbDataEvent */
    public ThumbDataEvent(Object source, int type, int index, Thumb<?> thumb) {
        super(source);
        this.type = type;
        this.thumb = thumb;
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public Thumb<?> getThumb() {
        return thumb;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " : " + type + " " + index + " " + thumb;
    }
}
