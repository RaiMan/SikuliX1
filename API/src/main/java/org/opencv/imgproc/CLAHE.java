/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgproc;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Size;

// C++: class CLAHE
public class CLAHE extends Algorithm {

    protected CLAHE(long addr) { super(addr); }

    //
    // C++:  void CLAHE::apply(Mat src, Mat& dst)
    //

    public  void apply(Mat src, Mat dst)
    {

        apply_0(nativeObj, src.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void CLAHE::setClipLimit(double clipLimit)
    //

    public  void setClipLimit(double clipLimit)
    {

        setClipLimit_0(nativeObj, clipLimit);

        return;
    }

    //
    // C++:  void CLAHE::setTilesGridSize(Size tileGridSize)
    //

    public  void setTilesGridSize(Size tileGridSize)
    {

        setTilesGridSize_0(nativeObj, tileGridSize.width, tileGridSize.height);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++:  void CLAHE::apply(Mat src, Mat& dst)
    private static native void apply_0(long nativeObj, long src_nativeObj, long dst_nativeObj);

    // C++:  void CLAHE::setClipLimit(double clipLimit)
    private static native void setClipLimit_0(long nativeObj, double clipLimit);

    // C++:  void CLAHE::setTilesGridSize(Size tileGridSize)
    private static native void setTilesGridSize_0(long nativeObj, double tileGridSize_width, double tileGridSize_height);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
