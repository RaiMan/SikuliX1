/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.opencv.core;

/**
 * <p>class CV_EXPORTS TermCriteria <code></p>
 *
 * <p>// C++ code:</p>
 *
 *
 * <p>public:</p>
 *
 * <p>enum</p>
 *
 *
 * <p>COUNT=1, //!< the maximum number of iterations or elements to compute</p>
 *
 * <p>MAX_ITER=COUNT, //!< ditto</p>
 *
 * <p>EPS=2 //!< the desired accuracy or change in parameters at which the
 * iterative algorithm stops</p>
 *
 * <p>};</p>
 *
 * <p>//! default constructor</p>
 *
 * <p>TermCriteria();</p>
 *
 * <p>//! full constructor</p>
 *
 * <p>TermCriteria(int type, int maxCount, double epsilon);</p>
 *
 * <p>//! conversion from CvTermCriteria</p>
 *
 * <p>TermCriteria(const CvTermCriteria& criteria);</p>
 *
 * <p>//! conversion to CvTermCriteria</p>
 *
 * <p>operator CvTermCriteria() const;</p>
 *
 * <p>int type; //!< the type of termination criteria: COUNT, EPS or COUNT + EPS</p>
 *
 * <p>int maxCount; // the maximum number of iterations/elements</p>
 *
 * <p>double epsilon; // the desired accuracy</p>
 *
 * <p>};</p>
 *
 * <p>The class defining termination criteria for iterative algorithms. You can
 * initialize it by default constructor and then override any parameters, or the
 * structure may be fully initialized using the advanced variant of the
 * constructor.
 * </code></p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#termcriteria">org.opencv.core.TermCriteria</a>
 */
public class TermCriteria {

    /**
     * The maximum number of iterations or elements to compute
     */
    public static final int COUNT = 1;
    /**
     * The maximum number of iterations or elements to compute
     */
    public static final int MAX_ITER = COUNT;
    /**
     * The desired accuracy threshold or change in parameters at which the iterative algorithm is terminated.
     */
    public static final int EPS = 2;

    public int type;
    public int maxCount;
    public double epsilon;

    /**
     * Termination criteria for iterative algorithms.
     *
     * @param type
     *            the type of termination criteria: COUNT, EPS or COUNT + EPS.
     * @param maxCount
     *            the maximum number of iterations/elements.
     * @param epsilon
     *            the desired accuracy.
     */
    public TermCriteria(int type, int maxCount, double epsilon) {
        this.type = type;
        this.maxCount = maxCount;
        this.epsilon = epsilon;
    }

    /**
     * Termination criteria for iterative algorithms.
     */
    public TermCriteria() {
        this(0, 0, 0.0);
    }

    public TermCriteria(double[] vals) {
        set(vals);
    }

    public void set(double[] vals) {
        if (vals != null) {
            type = vals.length > 0 ? (int) vals[0] : 0;
            maxCount = vals.length > 1 ? (int) vals[1] : 0;
            epsilon = vals.length > 2 ? (double) vals[2] : 0;
        } else {
            type = 0;
            maxCount = 0;
            epsilon = 0;
        }
    }

    public TermCriteria clone() {
        return new TermCriteria(type, maxCount, epsilon);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(type);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxCount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(epsilon);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TermCriteria)) return false;
        TermCriteria it = (TermCriteria) obj;
        return type == it.type && maxCount == it.maxCount && epsilon == it.epsilon;
    }

    @Override
    public String toString() {
        if (this == null) return "null";
        return "{ type: " + type + ", maxCount: " + maxCount + ", epsilon: " + epsilon + "}";
    }
}
