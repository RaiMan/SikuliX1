/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.opencv.gpu;

// C++: class TargetArchs
public class TargetArchs {

    protected final long nativeObj;
    protected TargetArchs(long addr) { nativeObj = addr; }

    //
    // C++: static bool TargetArchs::builtWith(int feature_set)
    //

    public static boolean builtWith(int feature_set)
    {
        boolean retVal = builtWith_0(feature_set);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::has(int major, int minor)
    //

    public static boolean has(int major, int minor)
    {
        boolean retVal = has_0(major, minor);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::hasBin(int major, int minor)
    //

    public static boolean hasBin(int major, int minor)
    {
        boolean retVal = hasBin_0(major, minor);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::hasEqualOrGreater(int major, int minor)
    //

    public static boolean hasEqualOrGreater(int major, int minor)
    {
        boolean retVal = hasEqualOrGreater_0(major, minor);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::hasEqualOrGreaterBin(int major, int minor)
    //

    public static boolean hasEqualOrGreaterBin(int major, int minor)
    {
        boolean retVal = hasEqualOrGreaterBin_0(major, minor);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::hasEqualOrGreaterPtx(int major, int minor)
    //

    public static boolean hasEqualOrGreaterPtx(int major, int minor)
    {
        boolean retVal = hasEqualOrGreaterPtx_0(major, minor);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::hasEqualOrLessPtx(int major, int minor)
    //

    public static boolean hasEqualOrLessPtx(int major, int minor)
    {
        boolean retVal = hasEqualOrLessPtx_0(major, minor);
        return retVal;
    }

    //
    // C++: static bool TargetArchs::hasPtx(int major, int minor)
    //

    public static boolean hasPtx(int major, int minor)
    {
        boolean retVal = hasPtx_0(major, minor);
        return retVal;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++: static bool TargetArchs::builtWith(int feature_set)
    private static native boolean builtWith_0(int feature_set);

    // C++: static bool TargetArchs::has(int major, int minor)
    private static native boolean has_0(int major, int minor);

    // C++: static bool TargetArchs::hasBin(int major, int minor)
    private static native boolean hasBin_0(int major, int minor);

    // C++: static bool TargetArchs::hasEqualOrGreater(int major, int minor)
    private static native boolean hasEqualOrGreater_0(int major, int minor);

    // C++: static bool TargetArchs::hasEqualOrGreaterBin(int major, int minor)
    private static native boolean hasEqualOrGreaterBin_0(int major, int minor);

    // C++: static bool TargetArchs::hasEqualOrGreaterPtx(int major, int minor)
    private static native boolean hasEqualOrGreaterPtx_0(int major, int minor);

    // C++: static bool TargetArchs::hasEqualOrLessPtx(int major, int minor)
    private static native boolean hasEqualOrLessPtx_0(int major, int minor);

    // C++: static bool TargetArchs::hasPtx(int major, int minor)
    private static native boolean hasPtx_0(int major, int minor);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
