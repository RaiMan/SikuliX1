/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.multislider;

import java.util.List;

/**
 *
 * @author joshy
 */
public interface MultiThumbModel<E> extends Iterable<Thumb<E>> {

    public float getMinimumValue();
    public void setMinimumValue(float minimumValue);
    public float getMaximumValue();
    public void setMaximumValue(float maximumValue);

    public int addThumb(float value, E obj);
    public void insertThumb(float value, E obj, int index);
    public void removeThumb(int index);
    public int getThumbCount();
    public Thumb<E> getThumbAt(int index);
    public int getThumbIndex(Thumb<E> thumb);
    public List<Thumb<E>> getSortedThumbs();
    public void thumbPositionChanged(Thumb<E> thumb);
    public void thumbValueChanged(Thumb<E> thumb);

    public void addThumbDataListener(ThumbDataListener listener);
    public void removeThumbDataListener(ThumbDataListener listener);
}
