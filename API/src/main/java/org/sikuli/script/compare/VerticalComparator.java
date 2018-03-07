/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.compare;

import java.util.Comparator;

import org.sikuli.script.Region;

/**
 * Compares the Regions by y-position.
 */
public class VerticalComparator implements Comparator<Region> {

    /**
     * Compares the Y-Position of two {@link Region} objects.
     * @param region1 The first {@link Region} object
     * @param region2 The second {@link Region} object
     * @return
     * <ul>
     * <li>-1 if the y-position of region1 is smaller</li>
     * <li>0 if the y-positions are equal</li>
     * <li>1 if the y-position of region2 is smaller</li>
     * </ul>
     */
    @Override
    public int compare(Region region1, Region region2) {
        if (region1 == region2) {
            return 0;
        }

        if (region1.getY() == region2.getY()) {
            return 0;
        }

        return region1.getY() < region2.getY() ? -1 : 1;
    }

}
