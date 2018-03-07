/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.compare;

import java.util.Comparator;

import org.sikuli.script.Location;
import org.sikuli.script.Region;

/**
 * Compares the Regions by distance to a point.
 */
public class DistanceComparator implements Comparator<Region> {

    /** X value for comparison */
    private double x;
    /** Y value for comparison */
    private double y;

    /**
     * Constructor for class DistanceComparator.
     * @param x X-Position for comparison
     * @param y Y-Position for comparison
     */
    public DistanceComparator(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor for class DistanceComparator.
     * @param location Location for comparison
     */
    public DistanceComparator(Location location) {
        this.x = location.getX();
        this.y = location.getY();
    }

    /**
     * Constructor for class DistanceComparator.
     * @param region Region for comparison
     */
    public DistanceComparator(Region region) {
        this.x = region.getCenter().getX();
        this.y = region.getCenter().getY();
    }

    /**
     * Compares the distance of two {@link Region} objects.
     * @param region1 The first {@link Region} object
     * @param region2 The second {@link Region} object
     * @return
     * <ul>
     * <li>-1 if the distance to region2 is smaller than to region1</li>
     * <li>0 if the distances are equal</li>
     * <li>1 if the distance to region1 is smaller than to region2</li>
     * </ul>
     */
    @Override
    public int compare(Region region1, Region region2) {
        if (region1 == region2) {
            return 0;
        }

        double distance1 = Math.sqrt(Math.pow(y - region1.getY(),2) + Math.pow(x - region1.getX(),2));
        double distance2 = Math.sqrt(Math.pow(y - region2.getY(),2) + Math.pow(x - region2.getX(),2));

        if (distance1 == distance2) {
            return 0;
        }

        return distance1  < distance2 ? -1 : 1;
    }

}
