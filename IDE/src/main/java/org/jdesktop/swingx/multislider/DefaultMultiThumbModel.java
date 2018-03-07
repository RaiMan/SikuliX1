/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.multislider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author joshy
 */
public class DefaultMultiThumbModel<E> extends AbstractMultiThumbModel<E> {

    protected List<Thumb<E>>thumbs = new ArrayList<Thumb<E>>();

    /** Creates a new instance of DefaultMultiThumbModel */
    public DefaultMultiThumbModel() {
        setMinimumValue(0.0f);
        setMaximumValue(1.0f);
    }
    // returns the index of the newly added thumb
    @Override
    public int addThumb(float value, E obj) {
        Thumb<E> thumb = new Thumb<E>(this);
        thumb.setPosition(value);
        thumb.setObject(obj);
        thumbs.add(thumb);
        int n = thumbs.size();
        ThumbDataEvent evt = new ThumbDataEvent(this,-1,thumbs.size()-1,thumb);
        for(ThumbDataListener tdl : thumbDataListeners) {
            tdl.thumbAdded(evt);
        }
        return n-1;
    }

    @Override
    public void insertThumb(float value, E obj, int index) {
        Thumb<E> thumb = new Thumb<E>(this);
        thumb.setPosition(value);
        thumb.setObject(obj);
        thumbs.add(index,thumb);
        ThumbDataEvent evt = new ThumbDataEvent(this,-1,index,thumb);
        for(ThumbDataListener tdl : thumbDataListeners) {
            tdl.thumbAdded(evt);
        }
    }

    @Override
    public void removeThumb(int index) {
        Thumb<E> thumb = thumbs.remove(index);
        ThumbDataEvent evt = new ThumbDataEvent(this,-1,index,thumb);
        for(ThumbDataListener tdl : thumbDataListeners) {
            tdl.thumbRemoved(evt);
        }
    }

    @Override
    public int getThumbCount() {
        return thumbs.size();
    }

    @Override
    public Thumb<E> getThumbAt(int index) {
        return thumbs.get(index);
    }

    @Override
    public List<Thumb<E>> getSortedThumbs() {
        List<Thumb<E>> list = new ArrayList<Thumb<E>>();
        list.addAll(thumbs);
        Collections.sort(list, new Comparator<Thumb<E>>() {
            @Override
            public int compare(Thumb<E> o1, Thumb<E> o2) {
                float f1 = o1.getPosition();
                float f2 = o2.getPosition();
                if(f1<f2) {
                    return -1;
                }
                if(f1>f2) {
                    return 1;
                }
                return 0;
            }
        });
        return list;
    }

    @Override
    public Iterator<Thumb<E>> iterator() {
        return thumbs.iterator();
    }

    @Override
    public int getThumbIndex(Thumb<E> thumb) {
        return thumbs.indexOf(thumb);
    }
}
