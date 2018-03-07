/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.opencv.features2d;

//C++: class DMatch

/**
 * Structure for matching: query descriptor index, train descriptor index, train
 * image index and distance between descriptors.
 */
public class DMatch {

    /**
     * Query descriptor index.
     */
    public int queryIdx;
    /**
     * Train descriptor index.
     */
    public int trainIdx;
    /**
     * Train image index.
     */
    public int imgIdx;

    public float distance;

    public DMatch() {
        this(-1, -1, Float.MAX_VALUE);
    }

    public DMatch(int _queryIdx, int _trainIdx, float _distance) {
        queryIdx = _queryIdx;
        trainIdx = _trainIdx;
        imgIdx = -1;
        distance = _distance;
    }

    public DMatch(int _queryIdx, int _trainIdx, int _imgIdx, float _distance) {
        queryIdx = _queryIdx;
        trainIdx = _trainIdx;
        imgIdx = _imgIdx;
        distance = _distance;
    }

    /**
     * Less is better.
     */
    public boolean lessThan(DMatch it) {
        return distance < it.distance;
    }

    @Override
    public String toString() {
        return "DMatch [queryIdx=" + queryIdx + ", trainIdx=" + trainIdx
                + ", imgIdx=" + imgIdx + ", distance=" + distance + "]";
    }

}
