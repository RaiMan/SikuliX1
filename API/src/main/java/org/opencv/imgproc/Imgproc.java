/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgproc;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.utils.Converters;

public class Imgproc {

    private static final int
            IPL_BORDER_CONSTANT = 0,
            IPL_BORDER_REPLICATE = 1,
            IPL_BORDER_REFLECT = 2,
            IPL_BORDER_WRAP = 3,
            IPL_BORDER_REFLECT_101 = 4,
            IPL_BORDER_TRANSPARENT = 5,
            CV_INTER_NN = 0,
            CV_INTER_LINEAR = 1,
            CV_INTER_CUBIC = 2,
            CV_INTER_AREA = 3,
            CV_INTER_LANCZOS4 = 4,
            CV_MOP_ERODE = 0,
            CV_MOP_DILATE = 1,
            CV_MOP_OPEN = 2,
            CV_MOP_CLOSE = 3,
            CV_MOP_GRADIENT = 4,
            CV_MOP_TOPHAT = 5,
            CV_MOP_BLACKHAT = 6,
            CV_RETR_EXTERNAL = 0,
            CV_RETR_LIST = 1,
            CV_RETR_CCOMP = 2,
            CV_RETR_TREE = 3,
            CV_RETR_FLOODFILL = 4,
            CV_CHAIN_APPROX_NONE = 1,
            CV_CHAIN_APPROX_SIMPLE = 2,
            CV_CHAIN_APPROX_TC89_L1 = 3,
            CV_CHAIN_APPROX_TC89_KCOS = 4,
            CV_THRESH_BINARY = 0,
            CV_THRESH_BINARY_INV = 1,
            CV_THRESH_TRUNC = 2,
            CV_THRESH_TOZERO = 3,
            CV_THRESH_TOZERO_INV = 4,
            CV_THRESH_MASK = 7,
            CV_THRESH_OTSU = 8;

    public static final int
            CV_BLUR_NO_SCALE = 0,
            CV_BLUR = 1,
            CV_GAUSSIAN = 2,
            CV_MEDIAN = 3,
            CV_BILATERAL = 4,
            CV_GAUSSIAN_5x5 = 7,
            CV_SCHARR = -1,
            CV_MAX_SOBEL_KSIZE = 7,
            CV_RGBA2mRGBA = 125,
            CV_mRGBA2RGBA = 126,
            CV_WARP_FILL_OUTLIERS = 8,
            CV_WARP_INVERSE_MAP = 16,
            CV_SHAPE_RECT = 0,
            CV_SHAPE_CROSS = 1,
            CV_SHAPE_ELLIPSE = 2,
            CV_SHAPE_CUSTOM = 100,
            CV_CHAIN_CODE = 0,
            CV_LINK_RUNS = 5,
            CV_POLY_APPROX_DP = 0,
            CV_CONTOURS_MATCH_I1 = 1,
            CV_CONTOURS_MATCH_I2 = 2,
            CV_CONTOURS_MATCH_I3 = 3,
            CV_CLOCKWISE = 1,
            CV_COUNTER_CLOCKWISE = 2,
            CV_COMP_CORREL = 0,
            CV_COMP_CHISQR = 1,
            CV_COMP_INTERSECT = 2,
            CV_COMP_BHATTACHARYYA = 3,
            CV_COMP_HELLINGER = CV_COMP_BHATTACHARYYA,
            CV_DIST_MASK_3 = 3,
            CV_DIST_MASK_5 = 5,
            CV_DIST_MASK_PRECISE = 0,
            CV_DIST_LABEL_CCOMP = 0,
            CV_DIST_LABEL_PIXEL = 1,
            CV_DIST_USER = -1,
            CV_DIST_L1 = 1,
            CV_DIST_L2 = 2,
            CV_DIST_C = 3,
            CV_DIST_L12 = 4,
            CV_DIST_FAIR = 5,
            CV_DIST_WELSCH = 6,
            CV_DIST_HUBER = 7,
            CV_CANNY_L2_GRADIENT = (1 << 31),
            CV_HOUGH_STANDARD = 0,
            CV_HOUGH_PROBABILISTIC = 1,
            CV_HOUGH_MULTI_SCALE = 2,
            CV_HOUGH_GRADIENT = 3,
            BORDER_REPLICATE = IPL_BORDER_REPLICATE,
            BORDER_CONSTANT = IPL_BORDER_CONSTANT,
            BORDER_REFLECT = IPL_BORDER_REFLECT,
            BORDER_WRAP = IPL_BORDER_WRAP,
            BORDER_REFLECT_101 = IPL_BORDER_REFLECT_101,
            BORDER_REFLECT101 = BORDER_REFLECT_101,
            BORDER_TRANSPARENT = IPL_BORDER_TRANSPARENT,
            BORDER_DEFAULT = BORDER_REFLECT_101,
            BORDER_ISOLATED = 16,
            KERNEL_GENERAL = 0,
            KERNEL_SYMMETRICAL = 1,
            KERNEL_ASYMMETRICAL = 2,
            KERNEL_SMOOTH = 4,
            KERNEL_INTEGER = 8,
            MORPH_ERODE = CV_MOP_ERODE,
            MORPH_DILATE = CV_MOP_DILATE,
            MORPH_OPEN = CV_MOP_OPEN,
            MORPH_CLOSE = CV_MOP_CLOSE,
            MORPH_GRADIENT = CV_MOP_GRADIENT,
            MORPH_TOPHAT = CV_MOP_TOPHAT,
            MORPH_BLACKHAT = CV_MOP_BLACKHAT,
            MORPH_RECT = 0,
            MORPH_CROSS = 1,
            MORPH_ELLIPSE = 2,
            GHT_POSITION = 0,
            GHT_SCALE = 1,
            GHT_ROTATION = 2,
            INTER_NEAREST = CV_INTER_NN,
            INTER_LINEAR = CV_INTER_LINEAR,
            INTER_CUBIC = CV_INTER_CUBIC,
            INTER_AREA = CV_INTER_AREA,
            INTER_LANCZOS4 = CV_INTER_LANCZOS4,
            INTER_MAX = 7,
            WARP_INVERSE_MAP = CV_WARP_INVERSE_MAP,
            INTER_BITS = 5,
            INTER_BITS2 = INTER_BITS*2,
            INTER_TAB_SIZE = (1<<INTER_BITS),
            INTER_TAB_SIZE2 = INTER_TAB_SIZE*INTER_TAB_SIZE,
            THRESH_BINARY = CV_THRESH_BINARY,
            THRESH_BINARY_INV = CV_THRESH_BINARY_INV,
            THRESH_TRUNC = CV_THRESH_TRUNC,
            THRESH_TOZERO = CV_THRESH_TOZERO,
            THRESH_TOZERO_INV = CV_THRESH_TOZERO_INV,
            THRESH_MASK = CV_THRESH_MASK,
            THRESH_OTSU = CV_THRESH_OTSU,
            ADAPTIVE_THRESH_MEAN_C = 0,
            ADAPTIVE_THRESH_GAUSSIAN_C = 1,
            PROJ_SPHERICAL_ORTHO = 0,
            PROJ_SPHERICAL_EQRECT = 1,
            GC_BGD = 0,
            GC_FGD = 1,
            GC_PR_BGD = 2,
            GC_PR_FGD = 3,
            GC_INIT_WITH_RECT = 0,
            GC_INIT_WITH_MASK = 1,
            GC_EVAL = 2,
            DIST_LABEL_CCOMP = 0,
            DIST_LABEL_PIXEL = 1,
            FLOODFILL_FIXED_RANGE = 1 << 16,
            FLOODFILL_MASK_ONLY = 1 << 17,
            COLOR_BGR2BGRA = 0,
            COLOR_RGB2RGBA = COLOR_BGR2BGRA,
            COLOR_BGRA2BGR = 1,
            COLOR_RGBA2RGB = COLOR_BGRA2BGR,
            COLOR_BGR2RGBA = 2,
            COLOR_RGB2BGRA = COLOR_BGR2RGBA,
            COLOR_RGBA2BGR = 3,
            COLOR_BGRA2RGB = COLOR_RGBA2BGR,
            COLOR_BGR2RGB = 4,
            COLOR_RGB2BGR = COLOR_BGR2RGB,
            COLOR_BGRA2RGBA = 5,
            COLOR_RGBA2BGRA = COLOR_BGRA2RGBA,
            COLOR_BGR2GRAY = 6,
            COLOR_RGB2GRAY = 7,
            COLOR_GRAY2BGR = 8,
            COLOR_GRAY2RGB = COLOR_GRAY2BGR,
            COLOR_GRAY2BGRA = 9,
            COLOR_GRAY2RGBA = COLOR_GRAY2BGRA,
            COLOR_BGRA2GRAY = 10,
            COLOR_RGBA2GRAY = 11,
            COLOR_BGR2BGR565 = 12,
            COLOR_RGB2BGR565 = 13,
            COLOR_BGR5652BGR = 14,
            COLOR_BGR5652RGB = 15,
            COLOR_BGRA2BGR565 = 16,
            COLOR_RGBA2BGR565 = 17,
            COLOR_BGR5652BGRA = 18,
            COLOR_BGR5652RGBA = 19,
            COLOR_GRAY2BGR565 = 20,
            COLOR_BGR5652GRAY = 21,
            COLOR_BGR2BGR555 = 22,
            COLOR_RGB2BGR555 = 23,
            COLOR_BGR5552BGR = 24,
            COLOR_BGR5552RGB = 25,
            COLOR_BGRA2BGR555 = 26,
            COLOR_RGBA2BGR555 = 27,
            COLOR_BGR5552BGRA = 28,
            COLOR_BGR5552RGBA = 29,
            COLOR_GRAY2BGR555 = 30,
            COLOR_BGR5552GRAY = 31,
            COLOR_BGR2XYZ = 32,
            COLOR_RGB2XYZ = 33,
            COLOR_XYZ2BGR = 34,
            COLOR_XYZ2RGB = 35,
            COLOR_BGR2YCrCb = 36,
            COLOR_RGB2YCrCb = 37,
            COLOR_YCrCb2BGR = 38,
            COLOR_YCrCb2RGB = 39,
            COLOR_BGR2HSV = 40,
            COLOR_RGB2HSV = 41,
            COLOR_BGR2Lab = 44,
            COLOR_RGB2Lab = 45,
            COLOR_BayerBG2BGR = 46,
            COLOR_BayerGB2BGR = 47,
            COLOR_BayerRG2BGR = 48,
            COLOR_BayerGR2BGR = 49,
            COLOR_BayerBG2RGB = COLOR_BayerRG2BGR,
            COLOR_BayerGB2RGB = COLOR_BayerGR2BGR,
            COLOR_BayerRG2RGB = COLOR_BayerBG2BGR,
            COLOR_BayerGR2RGB = COLOR_BayerGB2BGR,
            COLOR_BGR2Luv = 50,
            COLOR_RGB2Luv = 51,
            COLOR_BGR2HLS = 52,
            COLOR_RGB2HLS = 53,
            COLOR_HSV2BGR = 54,
            COLOR_HSV2RGB = 55,
            COLOR_Lab2BGR = 56,
            COLOR_Lab2RGB = 57,
            COLOR_Luv2BGR = 58,
            COLOR_Luv2RGB = 59,
            COLOR_HLS2BGR = 60,
            COLOR_HLS2RGB = 61,
            COLOR_BayerBG2BGR_VNG = 62,
            COLOR_BayerGB2BGR_VNG = 63,
            COLOR_BayerRG2BGR_VNG = 64,
            COLOR_BayerGR2BGR_VNG = 65,
            COLOR_BayerBG2RGB_VNG = COLOR_BayerRG2BGR_VNG,
            COLOR_BayerGB2RGB_VNG = COLOR_BayerGR2BGR_VNG,
            COLOR_BayerRG2RGB_VNG = COLOR_BayerBG2BGR_VNG,
            COLOR_BayerGR2RGB_VNG = COLOR_BayerGB2BGR_VNG,
            COLOR_BGR2HSV_FULL = 66,
            COLOR_RGB2HSV_FULL = 67,
            COLOR_BGR2HLS_FULL = 68,
            COLOR_RGB2HLS_FULL = 69,
            COLOR_HSV2BGR_FULL = 70,
            COLOR_HSV2RGB_FULL = 71,
            COLOR_HLS2BGR_FULL = 72,
            COLOR_HLS2RGB_FULL = 73,
            COLOR_LBGR2Lab = 74,
            COLOR_LRGB2Lab = 75,
            COLOR_LBGR2Luv = 76,
            COLOR_LRGB2Luv = 77,
            COLOR_Lab2LBGR = 78,
            COLOR_Lab2LRGB = 79,
            COLOR_Luv2LBGR = 80,
            COLOR_Luv2LRGB = 81,
            COLOR_BGR2YUV = 82,
            COLOR_RGB2YUV = 83,
            COLOR_YUV2BGR = 84,
            COLOR_YUV2RGB = 85,
            COLOR_BayerBG2GRAY = 86,
            COLOR_BayerGB2GRAY = 87,
            COLOR_BayerRG2GRAY = 88,
            COLOR_BayerGR2GRAY = 89,
            COLOR_YUV2RGB_NV12 = 90,
            COLOR_YUV2BGR_NV12 = 91,
            COLOR_YUV2RGB_NV21 = 92,
            COLOR_YUV2BGR_NV21 = 93,
            COLOR_YUV420sp2RGB = COLOR_YUV2RGB_NV21,
            COLOR_YUV420sp2BGR = COLOR_YUV2BGR_NV21,
            COLOR_YUV2RGBA_NV12 = 94,
            COLOR_YUV2BGRA_NV12 = 95,
            COLOR_YUV2RGBA_NV21 = 96,
            COLOR_YUV2BGRA_NV21 = 97,
            COLOR_YUV420sp2RGBA = COLOR_YUV2RGBA_NV21,
            COLOR_YUV420sp2BGRA = COLOR_YUV2BGRA_NV21,
            COLOR_YUV2RGB_YV12 = 98,
            COLOR_YUV2BGR_YV12 = 99,
            COLOR_YUV2RGB_IYUV = 100,
            COLOR_YUV2BGR_IYUV = 101,
            COLOR_YUV2RGB_I420 = COLOR_YUV2RGB_IYUV,
            COLOR_YUV2BGR_I420 = COLOR_YUV2BGR_IYUV,
            COLOR_YUV420p2RGB = COLOR_YUV2RGB_YV12,
            COLOR_YUV420p2BGR = COLOR_YUV2BGR_YV12,
            COLOR_YUV2RGBA_YV12 = 102,
            COLOR_YUV2BGRA_YV12 = 103,
            COLOR_YUV2RGBA_IYUV = 104,
            COLOR_YUV2BGRA_IYUV = 105,
            COLOR_YUV2RGBA_I420 = COLOR_YUV2RGBA_IYUV,
            COLOR_YUV2BGRA_I420 = COLOR_YUV2BGRA_IYUV,
            COLOR_YUV420p2RGBA = COLOR_YUV2RGBA_YV12,
            COLOR_YUV420p2BGRA = COLOR_YUV2BGRA_YV12,
            COLOR_YUV2GRAY_420 = 106,
            COLOR_YUV2GRAY_NV21 = COLOR_YUV2GRAY_420,
            COLOR_YUV2GRAY_NV12 = COLOR_YUV2GRAY_420,
            COLOR_YUV2GRAY_YV12 = COLOR_YUV2GRAY_420,
            COLOR_YUV2GRAY_IYUV = COLOR_YUV2GRAY_420,
            COLOR_YUV2GRAY_I420 = COLOR_YUV2GRAY_420,
            COLOR_YUV420sp2GRAY = COLOR_YUV2GRAY_420,
            COLOR_YUV420p2GRAY = COLOR_YUV2GRAY_420,
            COLOR_YUV2RGB_UYVY = 107,
            COLOR_YUV2BGR_UYVY = 108,
            COLOR_YUV2RGB_Y422 = COLOR_YUV2RGB_UYVY,
            COLOR_YUV2BGR_Y422 = COLOR_YUV2BGR_UYVY,
            COLOR_YUV2RGB_UYNV = COLOR_YUV2RGB_UYVY,
            COLOR_YUV2BGR_UYNV = COLOR_YUV2BGR_UYVY,
            COLOR_YUV2RGBA_UYVY = 111,
            COLOR_YUV2BGRA_UYVY = 112,
            COLOR_YUV2RGBA_Y422 = COLOR_YUV2RGBA_UYVY,
            COLOR_YUV2BGRA_Y422 = COLOR_YUV2BGRA_UYVY,
            COLOR_YUV2RGBA_UYNV = COLOR_YUV2RGBA_UYVY,
            COLOR_YUV2BGRA_UYNV = COLOR_YUV2BGRA_UYVY,
            COLOR_YUV2RGB_YUY2 = 115,
            COLOR_YUV2BGR_YUY2 = 116,
            COLOR_YUV2RGB_YVYU = 117,
            COLOR_YUV2BGR_YVYU = 118,
            COLOR_YUV2RGB_YUYV = COLOR_YUV2RGB_YUY2,
            COLOR_YUV2BGR_YUYV = COLOR_YUV2BGR_YUY2,
            COLOR_YUV2RGB_YUNV = COLOR_YUV2RGB_YUY2,
            COLOR_YUV2BGR_YUNV = COLOR_YUV2BGR_YUY2,
            COLOR_YUV2RGBA_YUY2 = 119,
            COLOR_YUV2BGRA_YUY2 = 120,
            COLOR_YUV2RGBA_YVYU = 121,
            COLOR_YUV2BGRA_YVYU = 122,
            COLOR_YUV2RGBA_YUYV = COLOR_YUV2RGBA_YUY2,
            COLOR_YUV2BGRA_YUYV = COLOR_YUV2BGRA_YUY2,
            COLOR_YUV2RGBA_YUNV = COLOR_YUV2RGBA_YUY2,
            COLOR_YUV2BGRA_YUNV = COLOR_YUV2BGRA_YUY2,
            COLOR_YUV2GRAY_UYVY = 123,
            COLOR_YUV2GRAY_YUY2 = 124,
            COLOR_YUV2GRAY_Y422 = COLOR_YUV2GRAY_UYVY,
            COLOR_YUV2GRAY_UYNV = COLOR_YUV2GRAY_UYVY,
            COLOR_YUV2GRAY_YVYU = COLOR_YUV2GRAY_YUY2,
            COLOR_YUV2GRAY_YUYV = COLOR_YUV2GRAY_YUY2,
            COLOR_YUV2GRAY_YUNV = COLOR_YUV2GRAY_YUY2,
            COLOR_RGBA2mRGBA = 125,
            COLOR_mRGBA2RGBA = 126,
            COLOR_RGB2YUV_I420 = 127,
            COLOR_BGR2YUV_I420 = 128,
            COLOR_RGB2YUV_IYUV = COLOR_RGB2YUV_I420,
            COLOR_BGR2YUV_IYUV = COLOR_BGR2YUV_I420,
            COLOR_RGBA2YUV_I420 = 129,
            COLOR_BGRA2YUV_I420 = 130,
            COLOR_RGBA2YUV_IYUV = COLOR_RGBA2YUV_I420,
            COLOR_BGRA2YUV_IYUV = COLOR_BGRA2YUV_I420,
            COLOR_RGB2YUV_YV12 = 131,
            COLOR_BGR2YUV_YV12 = 132,
            COLOR_RGBA2YUV_YV12 = 133,
            COLOR_BGRA2YUV_YV12 = 134,
            COLOR_COLORCVT_MAX = 135,
            TM_SQDIFF = 0,
            TM_SQDIFF_NORMED = 1,
            TM_CCORR = 2,
            TM_CCORR_NORMED = 3,
            TM_CCOEFF = 4,
            TM_CCOEFF_NORMED = 5,
            RETR_EXTERNAL = CV_RETR_EXTERNAL,
            RETR_LIST = CV_RETR_LIST,
            RETR_CCOMP = CV_RETR_CCOMP,
            RETR_TREE = CV_RETR_TREE,
            RETR_FLOODFILL = CV_RETR_FLOODFILL,
            CHAIN_APPROX_NONE = CV_CHAIN_APPROX_NONE,
            CHAIN_APPROX_SIMPLE = CV_CHAIN_APPROX_SIMPLE,
            CHAIN_APPROX_TC89_L1 = CV_CHAIN_APPROX_TC89_L1,
            CHAIN_APPROX_TC89_KCOS = CV_CHAIN_APPROX_TC89_KCOS;

    //
    // C++:  void Canny(Mat image, Mat& edges, double threshold1, double threshold2, int apertureSize = 3, bool L2gradient = false)
    //

/**
 * <p>Finds edges in an image using the [Canny86] algorithm.</p>
 *
 * <p>The function finds edges in the input image <code>image</code> and marks them
 * in the output map <code>edges</code> using the Canny algorithm. The smallest
 * value between <code>threshold1</code> and <code>threshold2</code> is used for
 * edge linking. The largest value is used to find initial segments of strong
 * edges. See http://en.wikipedia.org/wiki/Canny_edge_detector</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example on using the canny edge detector can be found at
 * opencv_source_code/samples/cpp/edge.cpp
 *   <li> (Python) An example on using the canny edge detector can be found at
 * opencv_source_code/samples/python/edge.py
 * </ul>
 *
 * @param image single-channel 8-bit input image.
 * @param edges output edge map; it has the same size and type as
 * <code>image</code>.
 * @param threshold1 first threshold for the hysteresis procedure.
 * @param threshold2 second threshold for the hysteresis procedure.
 * @param apertureSize aperture size for the "Sobel" operator.
 * @param L2gradient a flag, indicating whether a more accurate <em>L_2</em>
 * norm <em>=sqrt((dI/dx)^2 + (dI/dy)^2)</em> should be used to calculate the
 * image gradient magnitude (<code>L2gradient=true</code>), or whether the
 * default <em>L_1</em> norm <em>=|dI/dx|+|dI/dy|</em> is enough
 * (<code>L2gradient=false</code>).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#canny">org.opencv.imgproc.Imgproc.Canny</a>
 */
    public static void Canny(Mat image, Mat edges, double threshold1, double threshold2, int apertureSize, boolean L2gradient)
    {

        Canny_0(image.nativeObj, edges.nativeObj, threshold1, threshold2, apertureSize, L2gradient);

        return;
    }

/**
 * <p>Finds edges in an image using the [Canny86] algorithm.</p>
 *
 * <p>The function finds edges in the input image <code>image</code> and marks them
 * in the output map <code>edges</code> using the Canny algorithm. The smallest
 * value between <code>threshold1</code> and <code>threshold2</code> is used for
 * edge linking. The largest value is used to find initial segments of strong
 * edges. See http://en.wikipedia.org/wiki/Canny_edge_detector</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example on using the canny edge detector can be found at
 * opencv_source_code/samples/cpp/edge.cpp
 *   <li> (Python) An example on using the canny edge detector can be found at
 * opencv_source_code/samples/python/edge.py
 * </ul>
 *
 * @param image single-channel 8-bit input image.
 * @param edges output edge map; it has the same size and type as
 * <code>image</code>.
 * @param threshold1 first threshold for the hysteresis procedure.
 * @param threshold2 second threshold for the hysteresis procedure.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#canny">org.opencv.imgproc.Imgproc.Canny</a>
 */
    public static void Canny(Mat image, Mat edges, double threshold1, double threshold2)
    {

        Canny_1(image.nativeObj, edges.nativeObj, threshold1, threshold2);

        return;
    }

    //
    // C++:  void GaussianBlur(Mat src, Mat& dst, Size ksize, double sigmaX, double sigmaY = 0, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Blurs an image using a Gaussian filter.</p>
 *
 * <p>The function convolves the source image with the specified Gaussian kernel.
 * In-place filtering is supported.</p>
 *
 * @param src input image; the image can have any number of channels, which are
 * processed independently, but the depth should be <code>CV_8U</code>,
 * <code>CV_16U</code>, <code>CV_16S</code>, <code>CV_32F</code> or
 * <code>CV_64F</code>.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ksize Gaussian kernel size. <code>ksize.width</code> and
 * <code>ksize.height</code> can differ but they both must be positive and odd.
 * Or, they can be zero's and then they are computed from <code>sigma*</code>.
 * @param sigmaX Gaussian kernel standard deviation in X direction.
 * @param sigmaY Gaussian kernel standard deviation in Y direction; if
 * <code>sigmaY</code> is zero, it is set to be equal to <code>sigmaX</code>, if
 * both sigmas are zeros, they are computed from <code>ksize.width</code> and
 * <code>ksize.height</code>, respectively (see "getGaussianKernel" for
 * details); to fully control the result regardless of possible future
 * modifications of all this semantics, it is recommended to specify all of
 * <code>ksize</code>, <code>sigmaX</code>, and <code>sigmaY</code>.
 * @param borderType pixel extrapolation method (see "borderInterpolate" for
 * details).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#gaussianblur">org.opencv.imgproc.Imgproc.GaussianBlur</a>
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#medianBlur
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#blur
 * @see org.opencv.imgproc.Imgproc#filter2D
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 */
    public static void GaussianBlur(Mat src, Mat dst, Size ksize, double sigmaX, double sigmaY, int borderType)
    {

        GaussianBlur_0(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX, sigmaY, borderType);

        return;
    }

/**
 * <p>Blurs an image using a Gaussian filter.</p>
 *
 * <p>The function convolves the source image with the specified Gaussian kernel.
 * In-place filtering is supported.</p>
 *
 * @param src input image; the image can have any number of channels, which are
 * processed independently, but the depth should be <code>CV_8U</code>,
 * <code>CV_16U</code>, <code>CV_16S</code>, <code>CV_32F</code> or
 * <code>CV_64F</code>.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ksize Gaussian kernel size. <code>ksize.width</code> and
 * <code>ksize.height</code> can differ but they both must be positive and odd.
 * Or, they can be zero's and then they are computed from <code>sigma*</code>.
 * @param sigmaX Gaussian kernel standard deviation in X direction.
 * @param sigmaY Gaussian kernel standard deviation in Y direction; if
 * <code>sigmaY</code> is zero, it is set to be equal to <code>sigmaX</code>, if
 * both sigmas are zeros, they are computed from <code>ksize.width</code> and
 * <code>ksize.height</code>, respectively (see "getGaussianKernel" for
 * details); to fully control the result regardless of possible future
 * modifications of all this semantics, it is recommended to specify all of
 * <code>ksize</code>, <code>sigmaX</code>, and <code>sigmaY</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#gaussianblur">org.opencv.imgproc.Imgproc.GaussianBlur</a>
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#medianBlur
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#blur
 * @see org.opencv.imgproc.Imgproc#filter2D
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 */
    public static void GaussianBlur(Mat src, Mat dst, Size ksize, double sigmaX, double sigmaY)
    {

        GaussianBlur_1(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX, sigmaY);

        return;
    }

/**
 * <p>Blurs an image using a Gaussian filter.</p>
 *
 * <p>The function convolves the source image with the specified Gaussian kernel.
 * In-place filtering is supported.</p>
 *
 * @param src input image; the image can have any number of channels, which are
 * processed independently, but the depth should be <code>CV_8U</code>,
 * <code>CV_16U</code>, <code>CV_16S</code>, <code>CV_32F</code> or
 * <code>CV_64F</code>.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ksize Gaussian kernel size. <code>ksize.width</code> and
 * <code>ksize.height</code> can differ but they both must be positive and odd.
 * Or, they can be zero's and then they are computed from <code>sigma*</code>.
 * @param sigmaX Gaussian kernel standard deviation in X direction.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#gaussianblur">org.opencv.imgproc.Imgproc.GaussianBlur</a>
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#medianBlur
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#blur
 * @see org.opencv.imgproc.Imgproc#filter2D
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 */
    public static void GaussianBlur(Mat src, Mat dst, Size ksize, double sigmaX)
    {

        GaussianBlur_2(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX);

        return;
    }

    //
    // C++:  void HoughCircles(Mat image, Mat& circles, int method, double dp, double minDist, double param1 = 100, double param2 = 100, int minRadius = 0, int maxRadius = 0)
    //

/**
 * <p>Finds circles in a grayscale image using the Hough transform.</p>
 *
 * <p>The function finds circles in a grayscale image using a modification of the
 * Hough transform.
 * Example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include <cv.h></p>
 *
 * <p>#include <highgui.h></p>
 *
 * <p>#include <math.h></p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat img, gray;</p>
 *
 * <p>if(argc != 2 && !(img=imread(argv[1], 1)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>cvtColor(img, gray, CV_BGR2GRAY);</p>
 *
 * <p>// smooth it, otherwise a lot of false circles may be detected</p>
 *
 * <p>GaussianBlur(gray, gray, Size(9, 9), 2, 2);</p>
 *
 * <p>vector<Vec3f> circles;</p>
 *
 * <p>HoughCircles(gray, circles, CV_HOUGH_GRADIENT,</p>
 *
 * <p>2, gray->rows/4, 200, 100);</p>
 *
 * <p>for(size_t i = 0; i < circles.size(); i++)</p>
 *
 *
 * <p>Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));</p>
 *
 * <p>int radius = cvRound(circles[i][2]);</p>
 *
 * <p>// draw the circle center</p>
 *
 * <p>circle(img, center, 3, Scalar(0,255,0), -1, 8, 0);</p>
 *
 * <p>// draw the circle outline</p>
 *
 * <p>circle(img, center, radius, Scalar(0,0,255), 3, 8, 0);</p>
 *
 *
 * <p>namedWindow("circles", 1);</p>
 *
 * <p>imshow("circles", img);</p>
 *
 * <p>return 0;</p>
 *
 *
 * <p>Note: Usually the function detects the centers of circles well. However, it
 * may fail to find correct radii. You can assist to the function by specifying
 * the radius range (<code>minRadius</code> and <code>maxRadius</code>) if you
 * know it. Or, you may ignore the returned radius, use only the center, and
 * find the correct radius using an additional procedure.
 * </code></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Hough circle detector can be found at
 * opencv_source_code/samples/cpp/houghcircles.cpp
 * </ul>
 *
 * @param image 8-bit, single-channel, grayscale input image.
 * @param circles Output vector of found circles. Each vector is encoded as a
 * 3-element floating-point vector <em>(x, y, radius)</em>.
 * @param method Detection method to use. Currently, the only implemented method
 * is <code>CV_HOUGH_GRADIENT</code>, which is basically *21HT*, described in
 * [Yuen90].
 * @param dp Inverse ratio of the accumulator resolution to the image
 * resolution. For example, if <code>dp=1</code>, the accumulator has the same
 * resolution as the input image. If <code>dp=2</code>, the accumulator has half
 * as big width and height.
 * @param minDist Minimum distance between the centers of the detected circles.
 * If the parameter is too small, multiple neighbor circles may be falsely
 * detected in addition to a true one. If it is too large, some circles may be
 * missed.
 * @param param1 First method-specific parameter. In case of <code>CV_HOUGH_GRADIENT</code>,
 * it is the higher threshold of the two passed to the "Canny" edge detector
 * (the lower one is twice smaller).
 * @param param2 Second method-specific parameter. In case of <code>CV_HOUGH_GRADIENT</code>,
 * it is the accumulator threshold for the circle centers at the detection
 * stage. The smaller it is, the more false circles may be detected. Circles,
 * corresponding to the larger accumulator values, will be returned first.
 * @param minRadius Minimum circle radius.
 * @param maxRadius Maximum circle radius.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghcircles">org.opencv.imgproc.Imgproc.HoughCircles</a>
 * @see org.opencv.imgproc.Imgproc#minEnclosingCircle
 * @see org.opencv.imgproc.Imgproc#fitEllipse
 */
    public static void HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius)
    {

        HoughCircles_0(image.nativeObj, circles.nativeObj, method, dp, minDist, param1, param2, minRadius, maxRadius);

        return;
    }

/**
 * <p>Finds circles in a grayscale image using the Hough transform.</p>
 *
 * <p>The function finds circles in a grayscale image using a modification of the
 * Hough transform.
 * Example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include <cv.h></p>
 *
 * <p>#include <highgui.h></p>
 *
 * <p>#include <math.h></p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat img, gray;</p>
 *
 * <p>if(argc != 2 && !(img=imread(argv[1], 1)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>cvtColor(img, gray, CV_BGR2GRAY);</p>
 *
 * <p>// smooth it, otherwise a lot of false circles may be detected</p>
 *
 * <p>GaussianBlur(gray, gray, Size(9, 9), 2, 2);</p>
 *
 * <p>vector<Vec3f> circles;</p>
 *
 * <p>HoughCircles(gray, circles, CV_HOUGH_GRADIENT,</p>
 *
 * <p>2, gray->rows/4, 200, 100);</p>
 *
 * <p>for(size_t i = 0; i < circles.size(); i++)</p>
 *
 *
 * <p>Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));</p>
 *
 * <p>int radius = cvRound(circles[i][2]);</p>
 *
 * <p>// draw the circle center</p>
 *
 * <p>circle(img, center, 3, Scalar(0,255,0), -1, 8, 0);</p>
 *
 * <p>// draw the circle outline</p>
 *
 * <p>circle(img, center, radius, Scalar(0,0,255), 3, 8, 0);</p>
 *
 *
 * <p>namedWindow("circles", 1);</p>
 *
 * <p>imshow("circles", img);</p>
 *
 * <p>return 0;</p>
 *
 *
 * <p>Note: Usually the function detects the centers of circles well. However, it
 * may fail to find correct radii. You can assist to the function by specifying
 * the radius range (<code>minRadius</code> and <code>maxRadius</code>) if you
 * know it. Or, you may ignore the returned radius, use only the center, and
 * find the correct radius using an additional procedure.
 * </code></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Hough circle detector can be found at
 * opencv_source_code/samples/cpp/houghcircles.cpp
 * </ul>
 *
 * @param image 8-bit, single-channel, grayscale input image.
 * @param circles Output vector of found circles. Each vector is encoded as a
 * 3-element floating-point vector <em>(x, y, radius)</em>.
 * @param method Detection method to use. Currently, the only implemented method
 * is <code>CV_HOUGH_GRADIENT</code>, which is basically *21HT*, described in
 * [Yuen90].
 * @param dp Inverse ratio of the accumulator resolution to the image
 * resolution. For example, if <code>dp=1</code>, the accumulator has the same
 * resolution as the input image. If <code>dp=2</code>, the accumulator has half
 * as big width and height.
 * @param minDist Minimum distance between the centers of the detected circles.
 * If the parameter is too small, multiple neighbor circles may be falsely
 * detected in addition to a true one. If it is too large, some circles may be
 * missed.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghcircles">org.opencv.imgproc.Imgproc.HoughCircles</a>
 * @see org.opencv.imgproc.Imgproc#minEnclosingCircle
 * @see org.opencv.imgproc.Imgproc#fitEllipse
 */
    public static void HoughCircles(Mat image, Mat circles, int method, double dp, double minDist)
    {

        HoughCircles_1(image.nativeObj, circles.nativeObj, method, dp, minDist);

        return;
    }

    //
    // C++:  void HoughLines(Mat image, Mat& lines, double rho, double theta, int threshold, double srn = 0, double stn = 0)
    //

/**
 * <p>Finds lines in a binary image using the standard Hough transform.</p>
 *
 * <p>The function implements the standard or standard multi-scale Hough transform
 * algorithm for line detection. See http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm
 * for a good explanation of Hough transform.
 * See also the example in "HoughLinesP" description.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Hough line detector can be found at
 * opencv_source_code/samples/cpp/houghlines.cpp
 * </ul>
 *
 * @param image 8-bit, single-channel binary source image. The image may be
 * modified by the function.
 * @param lines Output vector of lines. Each line is represented by a
 * two-element vector <em>(rho, theta)</em>. <em>rho</em> is the distance from
 * the coordinate origin <em>(0,0)</em> (top-left corner of the image).
 * <em>theta</em> is the line rotation angle in radians (<em>0 ~ vertical line,
 * pi/2 ~ horizontal line</em>).
 * @param rho Distance resolution of the accumulator in pixels.
 * @param theta Angle resolution of the accumulator in radians.
 * @param threshold Accumulator threshold parameter. Only those lines are
 * returned that get enough votes (<em>&gtthreshold</em>).
 * @param srn For the multi-scale Hough transform, it is a divisor for the
 * distance resolution <code>rho</code>. The coarse accumulator distance
 * resolution is <code>rho</code> and the accurate accumulator resolution is
 * <code>rho/srn</code>. If both <code>srn=0</code> and <code>stn=0</code>, the
 * classical Hough transform is used. Otherwise, both these parameters should be
 * positive.
 * @param stn For the multi-scale Hough transform, it is a divisor for the
 * distance resolution <code>theta</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghlines">org.opencv.imgproc.Imgproc.HoughLines</a>
 */
    public static void HoughLines(Mat image, Mat lines, double rho, double theta, int threshold, double srn, double stn)
    {

        HoughLines_0(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn, stn);

        return;
    }

/**
 * <p>Finds lines in a binary image using the standard Hough transform.</p>
 *
 * <p>The function implements the standard or standard multi-scale Hough transform
 * algorithm for line detection. See http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm
 * for a good explanation of Hough transform.
 * See also the example in "HoughLinesP" description.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Hough line detector can be found at
 * opencv_source_code/samples/cpp/houghlines.cpp
 * </ul>
 *
 * @param image 8-bit, single-channel binary source image. The image may be
 * modified by the function.
 * @param lines Output vector of lines. Each line is represented by a
 * two-element vector <em>(rho, theta)</em>. <em>rho</em> is the distance from
 * the coordinate origin <em>(0,0)</em> (top-left corner of the image).
 * <em>theta</em> is the line rotation angle in radians (<em>0 ~ vertical line,
 * pi/2 ~ horizontal line</em>).
 * @param rho Distance resolution of the accumulator in pixels.
 * @param theta Angle resolution of the accumulator in radians.
 * @param threshold Accumulator threshold parameter. Only those lines are
 * returned that get enough votes (<em>&gtthreshold</em>).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghlines">org.opencv.imgproc.Imgproc.HoughLines</a>
 */
    public static void HoughLines(Mat image, Mat lines, double rho, double theta, int threshold)
    {

        HoughLines_1(image.nativeObj, lines.nativeObj, rho, theta, threshold);

        return;
    }

    //
    // C++:  void HoughLinesP(Mat image, Mat& lines, double rho, double theta, int threshold, double minLineLength = 0, double maxLineGap = 0)
    //

/**
 * <p>Finds line segments in a binary image using the probabilistic Hough
 * transform.</p>
 *
 * <p>The function implements the probabilistic Hough transform algorithm for line
 * detection, described in[Matas00]. See the line detection example below:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>/ * This is a standalone program. Pass an image name as the first parameter</p>
 *
 * <p>of the program. Switch between standard and probabilistic Hough transform</p>
 *
 * <p>by changing "#if 1" to "#if 0" and back * /</p>
 *
 * <p>#include <cv.h></p>
 *
 * <p>#include <highgui.h></p>
 *
 * <p>#include <math.h></p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src, dst, color_dst;</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 0)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>Canny(src, dst, 50, 200, 3);</p>
 *
 * <p>cvtColor(dst, color_dst, CV_GRAY2BGR);</p>
 *
 * <p>#if 0</p>
 *
 * <p>vector<Vec2f> lines;</p>
 *
 * <p>HoughLines(dst, lines, 1, CV_PI/180, 100);</p>
 *
 * <p>for(size_t i = 0; i < lines.size(); i++)</p>
 *
 *
 * <p>float rho = lines[i][0];</p>
 *
 * <p>float theta = lines[i][1];</p>
 *
 * <p>double a = cos(theta), b = sin(theta);</p>
 *
 * <p>double x0 = a*rho, y0 = b*rho;</p>
 *
 * <p>Point pt1(cvRound(x0 + 1000*(-b)),</p>
 *
 * <p>cvRound(y0 + 1000*(a)));</p>
 *
 * <p>Point pt2(cvRound(x0 - 1000*(-b)),</p>
 *
 * <p>cvRound(y0 - 1000*(a)));</p>
 *
 * <p>line(color_dst, pt1, pt2, Scalar(0,0,255), 3, 8);</p>
 *
 *
 * <p>#else</p>
 *
 * <p>vector<Vec4i> lines;</p>
 *
 * <p>HoughLinesP(dst, lines, 1, CV_PI/180, 80, 30, 10);</p>
 *
 * <p>for(size_t i = 0; i < lines.size(); i++)</p>
 *
 *
 * <p>line(color_dst, Point(lines[i][0], lines[i][1]),</p>
 *
 * <p>Point(lines[i][2], lines[i][3]), Scalar(0,0,255), 3, 8);</p>
 *
 *
 * <p>#endif</p>
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>namedWindow("Detected Lines", 1);</p>
 *
 * <p>imshow("Detected Lines", color_dst);</p>
 *
 * <p>waitKey(0);</p>
 *
 * <p>return 0;</p>
 *
 *
 * <p>This is a sample picture the function parameters have been tuned for: </code></p>
 *
 * <p>And this is the output of the above program in case of the probabilistic
 * Hough transform:</p>
 *
 * @param image 8-bit, single-channel binary source image. The image may be
 * modified by the function.
 * @param lines Output vector of lines. Each line is represented by a 4-element
 * vector <em>(x_1, y_1, x_2, y_2)</em>, where <em>(x_1,y_1)</em> and <em>(x_2,
 * y_2)</em> are the ending points of each detected line segment.
 * @param rho Distance resolution of the accumulator in pixels.
 * @param theta Angle resolution of the accumulator in radians.
 * @param threshold Accumulator threshold parameter. Only those lines are
 * returned that get enough votes (<em>&gtthreshold</em>).
 * @param minLineLength Minimum line length. Line segments shorter than that are
 * rejected.
 * @param maxLineGap Maximum allowed gap between points on the same line to link
 * them.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghlinesp">org.opencv.imgproc.Imgproc.HoughLinesP</a>
 */
    public static void HoughLinesP(Mat image, Mat lines, double rho, double theta, int threshold, double minLineLength, double maxLineGap)
    {

        HoughLinesP_0(image.nativeObj, lines.nativeObj, rho, theta, threshold, minLineLength, maxLineGap);

        return;
    }

/**
 * <p>Finds line segments in a binary image using the probabilistic Hough
 * transform.</p>
 *
 * <p>The function implements the probabilistic Hough transform algorithm for line
 * detection, described in[Matas00]. See the line detection example below:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>/ * This is a standalone program. Pass an image name as the first parameter</p>
 *
 * <p>of the program. Switch between standard and probabilistic Hough transform</p>
 *
 * <p>by changing "#if 1" to "#if 0" and back * /</p>
 *
 * <p>#include <cv.h></p>
 *
 * <p>#include <highgui.h></p>
 *
 * <p>#include <math.h></p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src, dst, color_dst;</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 0)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>Canny(src, dst, 50, 200, 3);</p>
 *
 * <p>cvtColor(dst, color_dst, CV_GRAY2BGR);</p>
 *
 * <p>#if 0</p>
 *
 * <p>vector<Vec2f> lines;</p>
 *
 * <p>HoughLines(dst, lines, 1, CV_PI/180, 100);</p>
 *
 * <p>for(size_t i = 0; i < lines.size(); i++)</p>
 *
 *
 * <p>float rho = lines[i][0];</p>
 *
 * <p>float theta = lines[i][1];</p>
 *
 * <p>double a = cos(theta), b = sin(theta);</p>
 *
 * <p>double x0 = a*rho, y0 = b*rho;</p>
 *
 * <p>Point pt1(cvRound(x0 + 1000*(-b)),</p>
 *
 * <p>cvRound(y0 + 1000*(a)));</p>
 *
 * <p>Point pt2(cvRound(x0 - 1000*(-b)),</p>
 *
 * <p>cvRound(y0 - 1000*(a)));</p>
 *
 * <p>line(color_dst, pt1, pt2, Scalar(0,0,255), 3, 8);</p>
 *
 *
 * <p>#else</p>
 *
 * <p>vector<Vec4i> lines;</p>
 *
 * <p>HoughLinesP(dst, lines, 1, CV_PI/180, 80, 30, 10);</p>
 *
 * <p>for(size_t i = 0; i < lines.size(); i++)</p>
 *
 *
 * <p>line(color_dst, Point(lines[i][0], lines[i][1]),</p>
 *
 * <p>Point(lines[i][2], lines[i][3]), Scalar(0,0,255), 3, 8);</p>
 *
 *
 * <p>#endif</p>
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>namedWindow("Detected Lines", 1);</p>
 *
 * <p>imshow("Detected Lines", color_dst);</p>
 *
 * <p>waitKey(0);</p>
 *
 * <p>return 0;</p>
 *
 *
 * <p>This is a sample picture the function parameters have been tuned for: </code></p>
 *
 * <p>And this is the output of the above program in case of the probabilistic
 * Hough transform:</p>
 *
 * @param image 8-bit, single-channel binary source image. The image may be
 * modified by the function.
 * @param lines Output vector of lines. Each line is represented by a 4-element
 * vector <em>(x_1, y_1, x_2, y_2)</em>, where <em>(x_1,y_1)</em> and <em>(x_2,
 * y_2)</em> are the ending points of each detected line segment.
 * @param rho Distance resolution of the accumulator in pixels.
 * @param theta Angle resolution of the accumulator in radians.
 * @param threshold Accumulator threshold parameter. Only those lines are
 * returned that get enough votes (<em>&gtthreshold</em>).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#houghlinesp">org.opencv.imgproc.Imgproc.HoughLinesP</a>
 */
    public static void HoughLinesP(Mat image, Mat lines, double rho, double theta, int threshold)
    {

        HoughLinesP_1(image.nativeObj, lines.nativeObj, rho, theta, threshold);

        return;
    }

    //
    // C++:  void HuMoments(Moments m, Mat& hu)
    //

/**
 * <p>Calculates seven Hu invariants.</p>
 *
 * <p>The function calculates seven Hu invariants (introduced in [Hu62]; see also
 * http://en.wikipedia.org/wiki/Image_moment) defined as:</p>
 *
 * <p><em>hu[0]= eta _20+ eta _02
 * hu[1]=(eta _20- eta _02)^2+4 eta _11^2
 * hu[2]=(eta _30-3 eta _12)^2+ (3 eta _21- eta _03)^2
 * hu[3]=(eta _30+ eta _12)^2+ (eta _21+ eta _03)^2
 * hu[4]=(eta _30-3 eta _12)(eta _30+ eta _12)[(eta _30+ eta _12)^2-3(eta _21+
 * eta _03)^2]+(3 eta _21- eta _03)(eta _21+ eta _03)[3(eta _30+ eta _12)^2-(eta
 * _21+ eta _03)^2]
 * hu[5]=(eta _20- eta _02)[(eta _30+ eta _12)^2- (eta _21+ eta _03)^2]+4 eta
 * _11(eta _30+ eta _12)(eta _21+ eta _03)
 * hu[6]=(3 eta _21- eta _03)(eta _21+ eta _03)[3(eta _30+ eta _12)^2-(eta _21+
 * eta _03)^2]-(eta _30-3 eta _12)(eta _21+ eta _03)[3(eta _30+ eta _12)^2-(eta
 * _21+ eta _03)^2]
 * </em></p>
 *
 * <p>where <em>eta_(ji)</em> stands for <em>Moments.nu_(ji)</em>.</p>
 *
 * <p>These values are proved to be invariants to the image scale, rotation, and
 * reflection except the seventh one, whose sign is changed by reflection. This
 * invariance is proved with the assumption of infinite image resolution. In
 * case of raster images, the computed Hu invariants for the original and
 * transformed images are a bit different.</p>
 *
 * @param m a m
 * @param hu Output Hu invariants.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#humoments">org.opencv.imgproc.Imgproc.HuMoments</a>
 * @see org.opencv.imgproc.Imgproc#matchShapes
 */
    public static void HuMoments(Moments m, Mat hu)
    {

        HuMoments_0(m.nativeObj, hu.nativeObj);

        return;
    }

    //
    // C++:  void Laplacian(Mat src, Mat& dst, int ddepth, int ksize = 1, double scale = 1, double delta = 0, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Calculates the Laplacian of an image.</p>
 *
 * <p>The function calculates the Laplacian of the source image by adding up the
 * second x and y derivatives calculated using the Sobel operator:</p>
 *
 * <p><em>dst = Delta src = (d^2 src)/(dx^2) + (d^2 src)/(dy^2)</em></p>
 *
 * <p>This is done when <code>ksize > 1</code>. When <code>ksize == 1</code>, the
 * Laplacian is computed by filtering the image with the following <em>3 x
 * 3</em> aperture:</p>
 *
 * <p><em>vecthreethree 0101(-4)1010</em></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Laplace transformation for edge detection can be
 * found at opencv_source_code/samples/cpp/laplace.cpp
 * </ul>
 *
 * @param src Source image.
 * @param dst Destination image of the same size and the same number of channels
 * as <code>src</code>.
 * @param ddepth Desired depth of the destination image.
 * @param ksize Aperture size used to compute the second-derivative filters. See
 * "getDerivKernels" for details. The size must be positive and odd.
 * @param scale Optional scale factor for the computed Laplacian values. By
 * default, no scaling is applied. See "getDerivKernels" for details.
 * @param delta Optional delta value that is added to the results prior to
 * storing them in <code>dst</code>.
 * @param borderType Pixel extrapolation method. See "borderInterpolate" for
 * details.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#laplacian">org.opencv.imgproc.Imgproc.Laplacian</a>
 * @see org.opencv.imgproc.Imgproc#Scharr
 * @see org.opencv.imgproc.Imgproc#Sobel
 */
    public static void Laplacian(Mat src, Mat dst, int ddepth, int ksize, double scale, double delta, int borderType)
    {

        Laplacian_0(src.nativeObj, dst.nativeObj, ddepth, ksize, scale, delta, borderType);

        return;
    }

/**
 * <p>Calculates the Laplacian of an image.</p>
 *
 * <p>The function calculates the Laplacian of the source image by adding up the
 * second x and y derivatives calculated using the Sobel operator:</p>
 *
 * <p><em>dst = Delta src = (d^2 src)/(dx^2) + (d^2 src)/(dy^2)</em></p>
 *
 * <p>This is done when <code>ksize > 1</code>. When <code>ksize == 1</code>, the
 * Laplacian is computed by filtering the image with the following <em>3 x
 * 3</em> aperture:</p>
 *
 * <p><em>vecthreethree 0101(-4)1010</em></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Laplace transformation for edge detection can be
 * found at opencv_source_code/samples/cpp/laplace.cpp
 * </ul>
 *
 * @param src Source image.
 * @param dst Destination image of the same size and the same number of channels
 * as <code>src</code>.
 * @param ddepth Desired depth of the destination image.
 * @param ksize Aperture size used to compute the second-derivative filters. See
 * "getDerivKernels" for details. The size must be positive and odd.
 * @param scale Optional scale factor for the computed Laplacian values. By
 * default, no scaling is applied. See "getDerivKernels" for details.
 * @param delta Optional delta value that is added to the results prior to
 * storing them in <code>dst</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#laplacian">org.opencv.imgproc.Imgproc.Laplacian</a>
 * @see org.opencv.imgproc.Imgproc#Scharr
 * @see org.opencv.imgproc.Imgproc#Sobel
 */
    public static void Laplacian(Mat src, Mat dst, int ddepth, int ksize, double scale, double delta)
    {

        Laplacian_1(src.nativeObj, dst.nativeObj, ddepth, ksize, scale, delta);

        return;
    }

/**
 * <p>Calculates the Laplacian of an image.</p>
 *
 * <p>The function calculates the Laplacian of the source image by adding up the
 * second x and y derivatives calculated using the Sobel operator:</p>
 *
 * <p><em>dst = Delta src = (d^2 src)/(dx^2) + (d^2 src)/(dy^2)</em></p>
 *
 * <p>This is done when <code>ksize > 1</code>. When <code>ksize == 1</code>, the
 * Laplacian is computed by filtering the image with the following <em>3 x
 * 3</em> aperture:</p>
 *
 * <p><em>vecthreethree 0101(-4)1010</em></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the Laplace transformation for edge detection can be
 * found at opencv_source_code/samples/cpp/laplace.cpp
 * </ul>
 *
 * @param src Source image.
 * @param dst Destination image of the same size and the same number of channels
 * as <code>src</code>.
 * @param ddepth Desired depth of the destination image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#laplacian">org.opencv.imgproc.Imgproc.Laplacian</a>
 * @see org.opencv.imgproc.Imgproc#Scharr
 * @see org.opencv.imgproc.Imgproc#Sobel
 */
    public static void Laplacian(Mat src, Mat dst, int ddepth)
    {

        Laplacian_2(src.nativeObj, dst.nativeObj, ddepth);

        return;
    }

    //
    // C++:  double PSNR(Mat src1, Mat src2)
    //

    public static double PSNR(Mat src1, Mat src2)
    {

        double retVal = PSNR_0(src1.nativeObj, src2.nativeObj);

        return retVal;
    }

    //
    // C++:  void Scharr(Mat src, Mat& dst, int ddepth, int dx, int dy, double scale = 1, double delta = 0, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Calculates the first x- or y- image derivative using Scharr operator.</p>
 *
 * <p>The function computes the first x- or y- spatial image derivative using the
 * Scharr operator. The call</p>
 *
 * <p><em>Scharr(src, dst, ddepth, dx, dy, scale, delta, borderType)</em></p>
 *
 * <p>is equivalent to</p>
 *
 * <p><em>Sobel(src, dst, ddepth, dx, dy, CV_SCHARR, scale, delta,
 * borderType).</em></p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth output image depth (see "Sobel" for the list of supported
 * combination of <code>src.depth()</code> and <code>ddepth</code>).
 * @param dx order of the derivative x.
 * @param dy order of the derivative y.
 * @param scale optional scale factor for the computed derivative values; by
 * default, no scaling is applied (see "getDerivKernels" for details).
 * @param delta optional delta value that is added to the results prior to
 * storing them in <code>dst</code>.
 * @param borderType pixel extrapolation method (see "borderInterpolate" for
 * details).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#scharr">org.opencv.imgproc.Imgproc.Scharr</a>
 * @see org.opencv.core.Core#cartToPolar
 */
    public static void Scharr(Mat src, Mat dst, int ddepth, int dx, int dy, double scale, double delta, int borderType)
    {

        Scharr_0(src.nativeObj, dst.nativeObj, ddepth, dx, dy, scale, delta, borderType);

        return;
    }

/**
 * <p>Calculates the first x- or y- image derivative using Scharr operator.</p>
 *
 * <p>The function computes the first x- or y- spatial image derivative using the
 * Scharr operator. The call</p>
 *
 * <p><em>Scharr(src, dst, ddepth, dx, dy, scale, delta, borderType)</em></p>
 *
 * <p>is equivalent to</p>
 *
 * <p><em>Sobel(src, dst, ddepth, dx, dy, CV_SCHARR, scale, delta,
 * borderType).</em></p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth output image depth (see "Sobel" for the list of supported
 * combination of <code>src.depth()</code> and <code>ddepth</code>).
 * @param dx order of the derivative x.
 * @param dy order of the derivative y.
 * @param scale optional scale factor for the computed derivative values; by
 * default, no scaling is applied (see "getDerivKernels" for details).
 * @param delta optional delta value that is added to the results prior to
 * storing them in <code>dst</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#scharr">org.opencv.imgproc.Imgproc.Scharr</a>
 * @see org.opencv.core.Core#cartToPolar
 */
    public static void Scharr(Mat src, Mat dst, int ddepth, int dx, int dy, double scale, double delta)
    {

        Scharr_1(src.nativeObj, dst.nativeObj, ddepth, dx, dy, scale, delta);

        return;
    }

/**
 * <p>Calculates the first x- or y- image derivative using Scharr operator.</p>
 *
 * <p>The function computes the first x- or y- spatial image derivative using the
 * Scharr operator. The call</p>
 *
 * <p><em>Scharr(src, dst, ddepth, dx, dy, scale, delta, borderType)</em></p>
 *
 * <p>is equivalent to</p>
 *
 * <p><em>Sobel(src, dst, ddepth, dx, dy, CV_SCHARR, scale, delta,
 * borderType).</em></p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth output image depth (see "Sobel" for the list of supported
 * combination of <code>src.depth()</code> and <code>ddepth</code>).
 * @param dx order of the derivative x.
 * @param dy order of the derivative y.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#scharr">org.opencv.imgproc.Imgproc.Scharr</a>
 * @see org.opencv.core.Core#cartToPolar
 */
    public static void Scharr(Mat src, Mat dst, int ddepth, int dx, int dy)
    {

        Scharr_2(src.nativeObj, dst.nativeObj, ddepth, dx, dy);

        return;
    }

    //
    // C++:  void Sobel(Mat src, Mat& dst, int ddepth, int dx, int dy, int ksize = 3, double scale = 1, double delta = 0, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Calculates the first, second, third, or mixed image derivatives using an
 * extended Sobel operator.</p>
 *
 * <p>In all cases except one, the <em>ksize x&ltBR&gtksize</em> separable kernel
 * is used to calculate the derivative. When <em>ksize = 1</em>, the <em>3 x
 * 1</em> or <em>1 x 3</em> kernel is used (that is, no Gaussian smoothing is
 * done). <code>ksize = 1</code> can only be used for the first or the second x-
 * or y- derivatives.</p>
 *
 * <p>There is also the special value <code>ksize = CV_SCHARR</code> (-1) that
 * corresponds to the <em>3x3</em> Scharr filter that may give more accurate
 * results than the <em>3x3</em> Sobel. The Scharr aperture is</p>
 *
 * <p><em>
 * |-3 0 3|
 * |-10 0 10|
 * |-3 0 3|
 * </em></p>
 *
 * <p>for the x-derivative, or transposed for the y-derivative.</p>
 *
 * <p>The function calculates an image derivative by convolving the image with the
 * appropriate kernel:</p>
 *
 * <p><em>dst = (d^(xorder+yorder) src)/(dx^(xorder) dy^(yorder))</em></p>
 *
 * <p>The Sobel operators combine Gaussian smoothing and differentiation, so the
 * result is more or less resistant to the noise. Most often, the function is
 * called with (<code>xorder</code> = 1, <code>yorder</code> = 0,
 * <code>ksize</code> = 3) or (<code>xorder</code> = 0, <code>yorder</code> = 1,
 * <code>ksize</code> = 3) to calculate the first x- or y- image derivative. The
 * first case corresponds to a kernel of:</p>
 *
 * <p><em>
 * |-1 0 1|
 * |-2 0 2|
 * |-1 0 1|
 * </em></p>
 *
 * <p>The second case corresponds to a kernel of:</p>
 *
 * <p><em>
 * |-1 -2 -1|
 * |0 0 0|
 * |1 2 1|
 * </em></p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth output image depth; the following combinations of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the destination image will have the same depth
 * as the source; in the case of 8-bit input images it will result in truncated
 * derivatives.</p>
 * @param dx a dx
 * @param dy a dy
 * @param ksize size of the extended Sobel kernel; it must be 1, 3, 5, or 7.
 * @param scale optional scale factor for the computed derivative values; by
 * default, no scaling is applied (see "getDerivKernels" for details).
 * @param delta optional delta value that is added to the results prior to
 * storing them in <code>dst</code>.
 * @param borderType pixel extrapolation method (see "borderInterpolate" for
 * details).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#sobel">org.opencv.imgproc.Imgproc.Sobel</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.core.Core#cartToPolar
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#Laplacian
 * @see org.opencv.imgproc.Imgproc#Scharr
 * @see org.opencv.imgproc.Imgproc#filter2D
 */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize, double scale, double delta, int borderType)
    {

        Sobel_0(src.nativeObj, dst.nativeObj, ddepth, dx, dy, ksize, scale, delta, borderType);

        return;
    }

/**
 * <p>Calculates the first, second, third, or mixed image derivatives using an
 * extended Sobel operator.</p>
 *
 * <p>In all cases except one, the <em>ksize x&ltBR&gtksize</em> separable kernel
 * is used to calculate the derivative. When <em>ksize = 1</em>, the <em>3 x
 * 1</em> or <em>1 x 3</em> kernel is used (that is, no Gaussian smoothing is
 * done). <code>ksize = 1</code> can only be used for the first or the second x-
 * or y- derivatives.</p>
 *
 * <p>There is also the special value <code>ksize = CV_SCHARR</code> (-1) that
 * corresponds to the <em>3x3</em> Scharr filter that may give more accurate
 * results than the <em>3x3</em> Sobel. The Scharr aperture is</p>
 *
 * <p><em>
 * |-3 0 3|
 * |-10 0 10|
 * |-3 0 3|
 * </em></p>
 *
 * <p>for the x-derivative, or transposed for the y-derivative.</p>
 *
 * <p>The function calculates an image derivative by convolving the image with the
 * appropriate kernel:</p>
 *
 * <p><em>dst = (d^(xorder+yorder) src)/(dx^(xorder) dy^(yorder))</em></p>
 *
 * <p>The Sobel operators combine Gaussian smoothing and differentiation, so the
 * result is more or less resistant to the noise. Most often, the function is
 * called with (<code>xorder</code> = 1, <code>yorder</code> = 0,
 * <code>ksize</code> = 3) or (<code>xorder</code> = 0, <code>yorder</code> = 1,
 * <code>ksize</code> = 3) to calculate the first x- or y- image derivative. The
 * first case corresponds to a kernel of:</p>
 *
 * <p><em>
 * |-1 0 1|
 * |-2 0 2|
 * |-1 0 1|
 * </em></p>
 *
 * <p>The second case corresponds to a kernel of:</p>
 *
 * <p><em>
 * |-1 -2 -1|
 * |0 0 0|
 * |1 2 1|
 * </em></p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth output image depth; the following combinations of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the destination image will have the same depth
 * as the source; in the case of 8-bit input images it will result in truncated
 * derivatives.</p>
 * @param dx a dx
 * @param dy a dy
 * @param ksize size of the extended Sobel kernel; it must be 1, 3, 5, or 7.
 * @param scale optional scale factor for the computed derivative values; by
 * default, no scaling is applied (see "getDerivKernels" for details).
 * @param delta optional delta value that is added to the results prior to
 * storing them in <code>dst</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#sobel">org.opencv.imgproc.Imgproc.Sobel</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.core.Core#cartToPolar
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#Laplacian
 * @see org.opencv.imgproc.Imgproc#Scharr
 * @see org.opencv.imgproc.Imgproc#filter2D
 */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize, double scale, double delta)
    {

        Sobel_1(src.nativeObj, dst.nativeObj, ddepth, dx, dy, ksize, scale, delta);

        return;
    }

/**
 * <p>Calculates the first, second, third, or mixed image derivatives using an
 * extended Sobel operator.</p>
 *
 * <p>In all cases except one, the <em>ksize x&ltBR&gtksize</em> separable kernel
 * is used to calculate the derivative. When <em>ksize = 1</em>, the <em>3 x
 * 1</em> or <em>1 x 3</em> kernel is used (that is, no Gaussian smoothing is
 * done). <code>ksize = 1</code> can only be used for the first or the second x-
 * or y- derivatives.</p>
 *
 * <p>There is also the special value <code>ksize = CV_SCHARR</code> (-1) that
 * corresponds to the <em>3x3</em> Scharr filter that may give more accurate
 * results than the <em>3x3</em> Sobel. The Scharr aperture is</p>
 *
 * <p><em>
 * |-3 0 3|
 * |-10 0 10|
 * |-3 0 3|
 * </em></p>
 *
 * <p>for the x-derivative, or transposed for the y-derivative.</p>
 *
 * <p>The function calculates an image derivative by convolving the image with the
 * appropriate kernel:</p>
 *
 * <p><em>dst = (d^(xorder+yorder) src)/(dx^(xorder) dy^(yorder))</em></p>
 *
 * <p>The Sobel operators combine Gaussian smoothing and differentiation, so the
 * result is more or less resistant to the noise. Most often, the function is
 * called with (<code>xorder</code> = 1, <code>yorder</code> = 0,
 * <code>ksize</code> = 3) or (<code>xorder</code> = 0, <code>yorder</code> = 1,
 * <code>ksize</code> = 3) to calculate the first x- or y- image derivative. The
 * first case corresponds to a kernel of:</p>
 *
 * <p><em>
 * |-1 0 1|
 * |-2 0 2|
 * |-1 0 1|
 * </em></p>
 *
 * <p>The second case corresponds to a kernel of:</p>
 *
 * <p><em>
 * |-1 -2 -1|
 * |0 0 0|
 * |1 2 1|
 * </em></p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth output image depth; the following combinations of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the destination image will have the same depth
 * as the source; in the case of 8-bit input images it will result in truncated
 * derivatives.</p>
 * @param dx a dx
 * @param dy a dy
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#sobel">org.opencv.imgproc.Imgproc.Sobel</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.core.Core#cartToPolar
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#Laplacian
 * @see org.opencv.imgproc.Imgproc#Scharr
 * @see org.opencv.imgproc.Imgproc#filter2D
 */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy)
    {

        Sobel_2(src.nativeObj, dst.nativeObj, ddepth, dx, dy);

        return;
    }

    //
    // C++:  void accumulate(Mat src, Mat& dst, Mat mask = Mat())
    //

/**
 * <p>Adds an image to the accumulator.</p>
 *
 * <p>The function adds <code>src</code> or some of its elements to
 * <code>dst</code> :</p>
 *
 * <p><em>dst(x,y) <- dst(x,y) + src(x,y) if mask(x,y) != 0</em></p>
 *
 * <p>The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * <p>The functions <code>accumulate*</code> can be used, for example, to collect
 * statistics of a scene background viewed by a still camera and for the further
 * foreground-background segmentation.</p>
 *
 * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point.
 * @param dst Accumulator image with the same number of channels as input image,
 * 32-bit or 64-bit floating-point.
 * @param mask Optional operation mask.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulate">org.opencv.imgproc.Imgproc.accumulate</a>
 * @see org.opencv.imgproc.Imgproc#accumulateWeighted
 * @see org.opencv.imgproc.Imgproc#accumulateProduct
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulate(Mat src, Mat dst, Mat mask)
    {

        accumulate_0(src.nativeObj, dst.nativeObj, mask.nativeObj);

        return;
    }

/**
 * <p>Adds an image to the accumulator.</p>
 *
 * <p>The function adds <code>src</code> or some of its elements to
 * <code>dst</code> :</p>
 *
 * <p><em>dst(x,y) <- dst(x,y) + src(x,y) if mask(x,y) != 0</em></p>
 *
 * <p>The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * <p>The functions <code>accumulate*</code> can be used, for example, to collect
 * statistics of a scene background viewed by a still camera and for the further
 * foreground-background segmentation.</p>
 *
 * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point.
 * @param dst Accumulator image with the same number of channels as input image,
 * 32-bit or 64-bit floating-point.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulate">org.opencv.imgproc.Imgproc.accumulate</a>
 * @see org.opencv.imgproc.Imgproc#accumulateWeighted
 * @see org.opencv.imgproc.Imgproc#accumulateProduct
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulate(Mat src, Mat dst)
    {

        accumulate_1(src.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void accumulateProduct(Mat src1, Mat src2, Mat& dst, Mat mask = Mat())
    //

/**
 * <p>Adds the per-element product of two input images to the accumulator.</p>
 *
 * <p>The function adds the product of two images or their selected regions to the
 * accumulator <code>dst</code> :</p>
 *
 * <p><em>dst(x,y) <- dst(x,y) + src1(x,y) * src2(x,y) if mask(x,y) != 0</em></p>
 *
 * <p>The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * @param src1 First input image, 1- or 3-channel, 8-bit or 32-bit floating
 * point.
 * @param src2 Second input image of the same type and the same size as
 * <code>src1</code>.
 * @param dst Accumulator with the same number of channels as input images,
 * 32-bit or 64-bit floating-point.
 * @param mask Optional operation mask.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulateproduct">org.opencv.imgproc.Imgproc.accumulateProduct</a>
 * @see org.opencv.imgproc.Imgproc#accumulate
 * @see org.opencv.imgproc.Imgproc#accumulateWeighted
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulateProduct(Mat src1, Mat src2, Mat dst, Mat mask)
    {

        accumulateProduct_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);

        return;
    }

/**
 * <p>Adds the per-element product of two input images to the accumulator.</p>
 *
 * <p>The function adds the product of two images or their selected regions to the
 * accumulator <code>dst</code> :</p>
 *
 * <p><em>dst(x,y) <- dst(x,y) + src1(x,y) * src2(x,y) if mask(x,y) != 0</em></p>
 *
 * <p>The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * @param src1 First input image, 1- or 3-channel, 8-bit or 32-bit floating
 * point.
 * @param src2 Second input image of the same type and the same size as
 * <code>src1</code>.
 * @param dst Accumulator with the same number of channels as input images,
 * 32-bit or 64-bit floating-point.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulateproduct">org.opencv.imgproc.Imgproc.accumulateProduct</a>
 * @see org.opencv.imgproc.Imgproc#accumulate
 * @see org.opencv.imgproc.Imgproc#accumulateWeighted
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulateProduct(Mat src1, Mat src2, Mat dst)
    {

        accumulateProduct_1(src1.nativeObj, src2.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void accumulateSquare(Mat src, Mat& dst, Mat mask = Mat())
    //

/**
 * <p>Adds the square of a source image to the accumulator.</p>
 *
 * <p>The function adds the input image <code>src</code> or its selected region,
 * raised to a power of 2, to the accumulator <code>dst</code> :</p>
 *
 * <p><em>dst(x,y) <- dst(x,y) + src(x,y)^2 if mask(x,y) != 0</em></p>
 *
 * <p>The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point.
 * @param dst Accumulator image with the same number of channels as input image,
 * 32-bit or 64-bit floating-point.
 * @param mask Optional operation mask.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulatesquare">org.opencv.imgproc.Imgproc.accumulateSquare</a>
 * @see org.opencv.imgproc.Imgproc#accumulateWeighted
 * @see org.opencv.imgproc.Imgproc#accumulateProduct
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulateSquare(Mat src, Mat dst, Mat mask)
    {

        accumulateSquare_0(src.nativeObj, dst.nativeObj, mask.nativeObj);

        return;
    }

/**
 * <p>Adds the square of a source image to the accumulator.</p>
 *
 * <p>The function adds the input image <code>src</code> or its selected region,
 * raised to a power of 2, to the accumulator <code>dst</code> :</p>
 *
 * <p><em>dst(x,y) <- dst(x,y) + src(x,y)^2 if mask(x,y) != 0</em></p>
 *
 * <p>The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point.
 * @param dst Accumulator image with the same number of channels as input image,
 * 32-bit or 64-bit floating-point.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulatesquare">org.opencv.imgproc.Imgproc.accumulateSquare</a>
 * @see org.opencv.imgproc.Imgproc#accumulateWeighted
 * @see org.opencv.imgproc.Imgproc#accumulateProduct
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulateSquare(Mat src, Mat dst)
    {

        accumulateSquare_1(src.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void accumulateWeighted(Mat src, Mat& dst, double alpha, Mat mask = Mat())
    //

/**
 * <p>Updates a running average.</p>
 *
 * <p>The function calculates the weighted sum of the input image <code>src</code>
 * and the accumulator <code>dst</code> so that <code>dst</code> becomes a
 * running average of a frame sequence:</p>
 *
 * <p><em>dst(x,y) <- (1- alpha) * dst(x,y) + alpha * src(x,y) if mask(x,y) !=
 * 0</em></p>
 *
 * <p>That is, <code>alpha</code> regulates the update speed (how fast the
 * accumulator "forgets" about earlier images).
 * The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point.
 * @param dst Accumulator image with the same number of channels as input image,
 * 32-bit or 64-bit floating-point.
 * @param alpha Weight of the input image.
 * @param mask Optional operation mask.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulateweighted">org.opencv.imgproc.Imgproc.accumulateWeighted</a>
 * @see org.opencv.imgproc.Imgproc#accumulate
 * @see org.opencv.imgproc.Imgproc#accumulateProduct
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulateWeighted(Mat src, Mat dst, double alpha, Mat mask)
    {

        accumulateWeighted_0(src.nativeObj, dst.nativeObj, alpha, mask.nativeObj);

        return;
    }

/**
 * <p>Updates a running average.</p>
 *
 * <p>The function calculates the weighted sum of the input image <code>src</code>
 * and the accumulator <code>dst</code> so that <code>dst</code> becomes a
 * running average of a frame sequence:</p>
 *
 * <p><em>dst(x,y) <- (1- alpha) * dst(x,y) + alpha * src(x,y) if mask(x,y) !=
 * 0</em></p>
 *
 * <p>That is, <code>alpha</code> regulates the update speed (how fast the
 * accumulator "forgets" about earlier images).
 * The function supports multi-channel images. Each channel is processed
 * independently.</p>
 *
 * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point.
 * @param dst Accumulator image with the same number of channels as input image,
 * 32-bit or 64-bit floating-point.
 * @param alpha Weight of the input image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#accumulateweighted">org.opencv.imgproc.Imgproc.accumulateWeighted</a>
 * @see org.opencv.imgproc.Imgproc#accumulate
 * @see org.opencv.imgproc.Imgproc#accumulateProduct
 * @see org.opencv.imgproc.Imgproc#accumulateSquare
 */
    public static void accumulateWeighted(Mat src, Mat dst, double alpha)
    {

        accumulateWeighted_1(src.nativeObj, dst.nativeObj, alpha);

        return;
    }

    //
    // C++:  void adaptiveBilateralFilter(Mat src, Mat& dst, Size ksize, double sigmaSpace, double maxSigmaColor = 20.0, Point anchor = Point(-1, -1), int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Applies the adaptive bilateral filter to an image.</p>
 *
 * <p>A main part of our strategy will be to load each raw pixel once, and reuse it
 * to calculate all pixels in the output (filtered) image that need this pixel
 * value. The math of the filter is that of the usual bilateral filter, except
 * that the sigma color is calculated in the neighborhood, and clamped by the
 * optional input value.</p>
 *
 * @param src The source image
 * @param dst The destination image; will have the same size and the same type
 * as src
 * @param ksize The kernel size. This is the neighborhood where the local
 * variance will be calculated, and where pixels will contribute (in a weighted
 * manner).
 * @param sigmaSpace Filter sigma in the coordinate space. Larger value of the
 * parameter means that farther pixels will influence each other (as long as
 * their colors are close enough; see sigmaColor). Then d>0, it specifies the
 * neighborhood size regardless of sigmaSpace, otherwise d is proportional to
 * sigmaSpace.
 * @param maxSigmaColor Maximum allowed sigma color (will clamp the value
 * calculated in the ksize neighborhood. Larger value of the parameter means
 * that more dissimilar pixels will influence each other (as long as their
 * colors are close enough; see sigmaColor). Then d>0, it specifies the
 * neighborhood size regardless of sigmaSpace, otherwise d is proportional to
 * sigmaSpace.
 * @param anchor a anchor
 * @param borderType Pixel extrapolation method.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#adaptivebilateralfilter">org.opencv.imgproc.Imgproc.adaptiveBilateralFilter</a>
 */
    public static void adaptiveBilateralFilter(Mat src, Mat dst, Size ksize, double sigmaSpace, double maxSigmaColor, Point anchor, int borderType)
    {

        adaptiveBilateralFilter_0(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaSpace, maxSigmaColor, anchor.x, anchor.y, borderType);

        return;
    }

/**
 * <p>Applies the adaptive bilateral filter to an image.</p>
 *
 * <p>A main part of our strategy will be to load each raw pixel once, and reuse it
 * to calculate all pixels in the output (filtered) image that need this pixel
 * value. The math of the filter is that of the usual bilateral filter, except
 * that the sigma color is calculated in the neighborhood, and clamped by the
 * optional input value.</p>
 *
 * @param src The source image
 * @param dst The destination image; will have the same size and the same type
 * as src
 * @param ksize The kernel size. This is the neighborhood where the local
 * variance will be calculated, and where pixels will contribute (in a weighted
 * manner).
 * @param sigmaSpace Filter sigma in the coordinate space. Larger value of the
 * parameter means that farther pixels will influence each other (as long as
 * their colors are close enough; see sigmaColor). Then d>0, it specifies the
 * neighborhood size regardless of sigmaSpace, otherwise d is proportional to
 * sigmaSpace.
 * @param maxSigmaColor Maximum allowed sigma color (will clamp the value
 * calculated in the ksize neighborhood. Larger value of the parameter means
 * that more dissimilar pixels will influence each other (as long as their
 * colors are close enough; see sigmaColor). Then d>0, it specifies the
 * neighborhood size regardless of sigmaSpace, otherwise d is proportional to
 * sigmaSpace.
 * @param anchor a anchor
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#adaptivebilateralfilter">org.opencv.imgproc.Imgproc.adaptiveBilateralFilter</a>
 */
    public static void adaptiveBilateralFilter(Mat src, Mat dst, Size ksize, double sigmaSpace, double maxSigmaColor, Point anchor)
    {

        adaptiveBilateralFilter_1(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaSpace, maxSigmaColor, anchor.x, anchor.y);

        return;
    }

/**
 * <p>Applies the adaptive bilateral filter to an image.</p>
 *
 * <p>A main part of our strategy will be to load each raw pixel once, and reuse it
 * to calculate all pixels in the output (filtered) image that need this pixel
 * value. The math of the filter is that of the usual bilateral filter, except
 * that the sigma color is calculated in the neighborhood, and clamped by the
 * optional input value.</p>
 *
 * @param src The source image
 * @param dst The destination image; will have the same size and the same type
 * as src
 * @param ksize The kernel size. This is the neighborhood where the local
 * variance will be calculated, and where pixels will contribute (in a weighted
 * manner).
 * @param sigmaSpace Filter sigma in the coordinate space. Larger value of the
 * parameter means that farther pixels will influence each other (as long as
 * their colors are close enough; see sigmaColor). Then d>0, it specifies the
 * neighborhood size regardless of sigmaSpace, otherwise d is proportional to
 * sigmaSpace.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#adaptivebilateralfilter">org.opencv.imgproc.Imgproc.adaptiveBilateralFilter</a>
 */
    public static void adaptiveBilateralFilter(Mat src, Mat dst, Size ksize, double sigmaSpace)
    {

        adaptiveBilateralFilter_2(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaSpace);

        return;
    }

    //
    // C++:  void adaptiveThreshold(Mat src, Mat& dst, double maxValue, int adaptiveMethod, int thresholdType, int blockSize, double C)
    //

/**
 * <p>Applies an adaptive threshold to an array.</p>
 *
 * <p>The function transforms a grayscale image to a binary image according to the
 * formulae:</p>
 * <ul>
 *   <li> THRESH_BINARY
 * </ul>
 *
 * <p><em>dst(x,y) = maxValue if src(x,y) &gt T(x,y); 0 otherwise</em></p>
 *
 * <ul>
 *   <li> THRESH_BINARY_INV
 * </ul>
 *
 * <p><em>dst(x,y) = 0 if src(x,y) &gt T(x,y); maxValue otherwise</em></p>
 *
 * <p>where <em>T(x,y)</em> is a threshold calculated individually for each pixel.</p>
 * <ul>
 *   <li> For the method <code>ADAPTIVE_THRESH_MEAN_C</code>, the threshold
 * value <em>T(x,y)</em> is a mean of the <em>blockSize x blockSize</em>
 * neighborhood of <em>(x, y)</em> minus <code>C</code>.
 *   <li> For the method <code>ADAPTIVE_THRESH_GAUSSIAN_C</code>, the threshold
 * value <em>T(x, y)</em> is a weighted sum (cross-correlation with a Gaussian
 * window) of the <em>blockSize x blockSize</em> neighborhood of <em>(x, y)</em>
 * minus <code>C</code>. The default sigma (standard deviation) is used for the
 * specified <code>blockSize</code>. See "getGaussianKernel".
 * </ul>
 *
 * <p>The function can process the image in-place.</p>
 *
 * @param src Source 8-bit single-channel image.
 * @param dst Destination image of the same size and the same type as
 * <code>src</code>.
 * @param maxValue Non-zero value assigned to the pixels for which the condition
 * is satisfied. See the details below.
 * @param adaptiveMethod Adaptive thresholding algorithm to use,
 * <code>ADAPTIVE_THRESH_MEAN_C</code> or <code>ADAPTIVE_THRESH_GAUSSIAN_C</code>.
 * See the details below.
 * @param thresholdType Thresholding type that must be either <code>THRESH_BINARY</code>
 * or <code>THRESH_BINARY_INV</code>.
 * @param blockSize Size of a pixel neighborhood that is used to calculate a
 * threshold value for the pixel: 3, 5, 7, and so on.
 * @param C Constant subtracted from the mean or weighted mean (see the details
 * below). Normally, it is positive but may be zero or negative as well.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#adaptivethreshold">org.opencv.imgproc.Imgproc.adaptiveThreshold</a>
 * @see org.opencv.imgproc.Imgproc#threshold
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#blur
 */
    public static void adaptiveThreshold(Mat src, Mat dst, double maxValue, int adaptiveMethod, int thresholdType, int blockSize, double C)
    {

        adaptiveThreshold_0(src.nativeObj, dst.nativeObj, maxValue, adaptiveMethod, thresholdType, blockSize, C);

        return;
    }

    //
    // C++:  void approxPolyDP(vector_Point2f curve, vector_Point2f& approxCurve, double epsilon, bool closed)
    //

/**
 * <p>Approximates a polygonal curve(s) with the specified precision.</p>
 *
 * <p>The functions <code>approxPolyDP</code> approximate a curve or a polygon with
 * another curve/polygon with less vertices so that the distance between them is
 * less or equal to the specified precision. It uses the Douglas-Peucker
 * algorithm http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm</p>
 *
 * <p>See https://github.com/Itseez/opencv/tree/master/samples/cpp/contours2.cpp
 * for the function usage model.</p>
 *
 * @param curve Input vector of a 2D point stored in:
 * <ul>
 *   <li> <code>std.vector</code> or <code>Mat</code> (C++ interface)
 *   <li> <code>Nx2</code> numpy array (Python interface)
 *   <li> <code>CvSeq</code> or <code> </code>CvMat" (C interface)
 * </ul>
 * @param approxCurve Result of the approximation. The type should match the
 * type of the input curve. In case of C interface the approximated curve is
 * stored in the memory storage and pointer to it is returned.
 * @param epsilon Parameter specifying the approximation accuracy. This is the
 * maximum distance between the original curve and its approximation.
 * @param closed If true, the approximated curve is closed (its first and last
 * vertices are connected). Otherwise, it is not closed.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#approxpolydp">org.opencv.imgproc.Imgproc.approxPolyDP</a>
 */
    public static void approxPolyDP(MatOfPoint2f curve, MatOfPoint2f approxCurve, double epsilon, boolean closed)
    {
        Mat curve_mat = curve;
        Mat approxCurve_mat = approxCurve;
        approxPolyDP_0(curve_mat.nativeObj, approxCurve_mat.nativeObj, epsilon, closed);

        return;
    }

    //
    // C++:  double arcLength(vector_Point2f curve, bool closed)
    //

/**
 * <p>Calculates a contour perimeter or a curve length.</p>
 *
 * <p>The function computes a curve length or a closed contour perimeter.</p>
 *
 * @param curve Input vector of 2D points, stored in <code>std.vector</code> or
 * <code>Mat</code>.
 * @param closed Flag indicating whether the curve is closed or not.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#arclength">org.opencv.imgproc.Imgproc.arcLength</a>
 */
    public static double arcLength(MatOfPoint2f curve, boolean closed)
    {
        Mat curve_mat = curve;
        double retVal = arcLength_0(curve_mat.nativeObj, closed);

        return retVal;
    }

    //
    // C++:  void bilateralFilter(Mat src, Mat& dst, int d, double sigmaColor, double sigmaSpace, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Applies the bilateral filter to an image.</p>
 *
 * <p>The function applies bilateral filtering to the input image, as described in
 * http://www.dai.ed.ac.uk/CVonline/LOCAL_COPIES/MANDUCHI1/Bilateral_Filtering.html
 * <code>bilateralFilter</code> can reduce unwanted noise very well while
 * keeping edges fairly sharp. However, it is very slow compared to most
 * filters.</p>
 * <ul>
 *   <li>Sigma values*: For simplicity, you can set the 2 sigma values to be the
 * same. If they are small (< 10), the filter will not have much effect, whereas
 * if they are large (> 150), they will have a very strong effect, making the
 * image look "cartoonish".
 *   <li>Filter size*: Large filters (d > 5) are very slow, so it is recommended
 * to use d=5 for real-time applications, and perhaps d=9 for offline
 * applications that need heavy noise filtering.
 * </ul>
 *
 * <p>This filter does not work inplace.</p>
 *
 * @param src Source 8-bit or floating-point, 1-channel or 3-channel image.
 * @param dst Destination image of the same size and type as <code>src</code>.
 * @param d Diameter of each pixel neighborhood that is used during filtering.
 * If it is non-positive, it is computed from <code>sigmaSpace</code>.
 * @param sigmaColor Filter sigma in the color space. A larger value of the
 * parameter means that farther colors within the pixel neighborhood (see
 * <code>sigmaSpace</code>) will be mixed together, resulting in larger areas of
 * semi-equal color.
 * @param sigmaSpace Filter sigma in the coordinate space. A larger value of the
 * parameter means that farther pixels will influence each other as long as
 * their colors are close enough (see <code>sigmaColor</code>). When
 * <code>d>0</code>, it specifies the neighborhood size regardless of
 * <code>sigmaSpace</code>. Otherwise, <code>d</code> is proportional to
 * <code>sigmaSpace</code>.
 * @param borderType a borderType
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#bilateralfilter">org.opencv.imgproc.Imgproc.bilateralFilter</a>
 */
    public static void bilateralFilter(Mat src, Mat dst, int d, double sigmaColor, double sigmaSpace, int borderType)
    {

        bilateralFilter_0(src.nativeObj, dst.nativeObj, d, sigmaColor, sigmaSpace, borderType);

        return;
    }

/**
 * <p>Applies the bilateral filter to an image.</p>
 *
 * <p>The function applies bilateral filtering to the input image, as described in
 * http://www.dai.ed.ac.uk/CVonline/LOCAL_COPIES/MANDUCHI1/Bilateral_Filtering.html
 * <code>bilateralFilter</code> can reduce unwanted noise very well while
 * keeping edges fairly sharp. However, it is very slow compared to most
 * filters.</p>
 * <ul>
 *   <li>Sigma values*: For simplicity, you can set the 2 sigma values to be the
 * same. If they are small (< 10), the filter will not have much effect, whereas
 * if they are large (> 150), they will have a very strong effect, making the
 * image look "cartoonish".
 *   <li>Filter size*: Large filters (d > 5) are very slow, so it is recommended
 * to use d=5 for real-time applications, and perhaps d=9 for offline
 * applications that need heavy noise filtering.
 * </ul>
 *
 * <p>This filter does not work inplace.</p>
 *
 * @param src Source 8-bit or floating-point, 1-channel or 3-channel image.
 * @param dst Destination image of the same size and type as <code>src</code>.
 * @param d Diameter of each pixel neighborhood that is used during filtering.
 * If it is non-positive, it is computed from <code>sigmaSpace</code>.
 * @param sigmaColor Filter sigma in the color space. A larger value of the
 * parameter means that farther colors within the pixel neighborhood (see
 * <code>sigmaSpace</code>) will be mixed together, resulting in larger areas of
 * semi-equal color.
 * @param sigmaSpace Filter sigma in the coordinate space. A larger value of the
 * parameter means that farther pixels will influence each other as long as
 * their colors are close enough (see <code>sigmaColor</code>). When
 * <code>d>0</code>, it specifies the neighborhood size regardless of
 * <code>sigmaSpace</code>. Otherwise, <code>d</code> is proportional to
 * <code>sigmaSpace</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#bilateralfilter">org.opencv.imgproc.Imgproc.bilateralFilter</a>
 */
    public static void bilateralFilter(Mat src, Mat dst, int d, double sigmaColor, double sigmaSpace)
    {

        bilateralFilter_1(src.nativeObj, dst.nativeObj, d, sigmaColor, sigmaSpace);

        return;
    }

    //
    // C++:  void blur(Mat src, Mat& dst, Size ksize, Point anchor = Point(-1,-1), int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Blurs an image using the normalized box filter.</p>
 *
 * <p>The function smoothes an image using the kernel:</p>
 *
 * <p><em>K = 1/(ksize.width*ksize.height) 1 1 1 *s 1 1
 * 1 1 1 *s 1 1..................
 * 1 1 1 *s 1 1
 * </em></p>
 *
 * <p>The call <code>blur(src, dst, ksize, anchor, borderType)</code> is equivalent
 * to <code>boxFilter(src, dst, src.type(), anchor, true, borderType)</code>.</p>
 *
 * @param src input image; it can have any number of channels, which are
 * processed independently, but the depth should be <code>CV_8U</code>,
 * <code>CV_16U</code>, <code>CV_16S</code>, <code>CV_32F</code> or
 * <code>CV_64F</code>.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ksize blurring kernel size.
 * @param anchor anchor point; default value <code>Point(-1,-1)</code> means
 * that the anchor is at the kernel center.
 * @param borderType border mode used to extrapolate pixels outside of the
 * image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#blur">org.opencv.imgproc.Imgproc.blur</a>
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#medianBlur
 */
    public static void blur(Mat src, Mat dst, Size ksize, Point anchor, int borderType)
    {

        blur_0(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, anchor.x, anchor.y, borderType);

        return;
    }

/**
 * <p>Blurs an image using the normalized box filter.</p>
 *
 * <p>The function smoothes an image using the kernel:</p>
 *
 * <p><em>K = 1/(ksize.width*ksize.height) 1 1 1 *s 1 1
 * 1 1 1 *s 1 1..................
 * 1 1 1 *s 1 1
 * </em></p>
 *
 * <p>The call <code>blur(src, dst, ksize, anchor, borderType)</code> is equivalent
 * to <code>boxFilter(src, dst, src.type(), anchor, true, borderType)</code>.</p>
 *
 * @param src input image; it can have any number of channels, which are
 * processed independently, but the depth should be <code>CV_8U</code>,
 * <code>CV_16U</code>, <code>CV_16S</code>, <code>CV_32F</code> or
 * <code>CV_64F</code>.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ksize blurring kernel size.
 * @param anchor anchor point; default value <code>Point(-1,-1)</code> means
 * that the anchor is at the kernel center.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#blur">org.opencv.imgproc.Imgproc.blur</a>
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#medianBlur
 */
    public static void blur(Mat src, Mat dst, Size ksize, Point anchor)
    {

        blur_1(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, anchor.x, anchor.y);

        return;
    }

/**
 * <p>Blurs an image using the normalized box filter.</p>
 *
 * <p>The function smoothes an image using the kernel:</p>
 *
 * <p><em>K = 1/(ksize.width*ksize.height) 1 1 1 *s 1 1
 * 1 1 1 *s 1 1..................
 * 1 1 1 *s 1 1
 * </em></p>
 *
 * <p>The call <code>blur(src, dst, ksize, anchor, borderType)</code> is equivalent
 * to <code>boxFilter(src, dst, src.type(), anchor, true, borderType)</code>.</p>
 *
 * @param src input image; it can have any number of channels, which are
 * processed independently, but the depth should be <code>CV_8U</code>,
 * <code>CV_16U</code>, <code>CV_16S</code>, <code>CV_32F</code> or
 * <code>CV_64F</code>.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ksize blurring kernel size.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#blur">org.opencv.imgproc.Imgproc.blur</a>
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#medianBlur
 */
    public static void blur(Mat src, Mat dst, Size ksize)
    {

        blur_2(src.nativeObj, dst.nativeObj, ksize.width, ksize.height);

        return;
    }

    //
    // C++:  int borderInterpolate(int p, int len, int borderType)
    //

/**
 * <p>Computes the source location of an extrapolated pixel.</p>
 *
 * <p>The function computes and returns the coordinate of a donor pixel
 * corresponding to the specified extrapolated pixel when using the specified
 * extrapolation border mode. For example, if you use <code>BORDER_WRAP</code>
 * mode in the horizontal direction, <code>BORDER_REFLECT_101</code> in the
 * vertical direction and want to compute value of the "virtual" pixel
 * <code>Point(-5, 100)</code> in a floating-point image <code>img</code>, it
 * looks like: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>float val = img.at<float>(borderInterpolate(100, img.rows, BORDER_REFLECT_101),</p>
 *
 * <p>borderInterpolate(-5, img.cols, BORDER_WRAP));</p>
 *
 * <p>Normally, the function is not called directly. It is used inside </code></p>
 *
 * <p>"FilterEngine" and "copyMakeBorder" to compute tables for quick
 * extrapolation.</p>
 *
 * @param p 0-based coordinate of the extrapolated pixel along one of the axes,
 * likely <0 or >= <code>len</code>.
 * @param len Length of the array along the corresponding axis.
 * @param borderType Border type, one of the <code>BORDER_*</code>, except for
 * <code>BORDER_TRANSPARENT</code> and <code>BORDER_ISOLATED</code>. When
 * <code>borderType==BORDER_CONSTANT</code>, the function always returns -1,
 * regardless of <code>p</code> and <code>len</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#borderinterpolate">org.opencv.imgproc.Imgproc.borderInterpolate</a>
 * @see org.opencv.imgproc.Imgproc#copyMakeBorder
 */
    public static int borderInterpolate(int p, int len, int borderType)
    {

        int retVal = borderInterpolate_0(p, len, borderType);

        return retVal;
    }

    //
    // C++:  Rect boundingRect(vector_Point points)
    //

/**
 * <p>Calculates the up-right bounding rectangle of a point set.</p>
 *
 * <p>The function calculates and returns the minimal up-right bounding rectangle
 * for the specified point set.</p>
 *
 * @param points Input 2D point set, stored in <code>std.vector</code> or
 * <code>Mat</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#boundingrect">org.opencv.imgproc.Imgproc.boundingRect</a>
 */
    public static Rect boundingRect(MatOfPoint points)
    {
        Mat points_mat = points;
        Rect retVal = new Rect(boundingRect_0(points_mat.nativeObj));

        return retVal;
    }

    //
    // C++:  void boxFilter(Mat src, Mat& dst, int ddepth, Size ksize, Point anchor = Point(-1,-1), bool normalize = true, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Blurs an image using the box filter.</p>
 *
 * <p>The function smoothes an image using the kernel:</p>
 *
 * <p><em>K = alpha 1 1 1 *s 1 1
 * 1 1 1 *s 1 1..................
 * 1 1 1 *s 1 1 </em></p>
 *
 * <p>where</p>
 *
 * <p><em>alpha = 1/(ksize.width*ksize.height) when normalize=true; 1
 * otherwise</em></p>
 *
 * <p>Unnormalized box filter is useful for computing various integral
 * characteristics over each pixel neighborhood, such as covariance matrices of
 * image derivatives (used in dense optical flow algorithms, and so on). If you
 * need to compute pixel sums over variable-size windows, use "integral".</p>
 *
 * @param src input image.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ddepth the output image depth (-1 to use <code>src.depth()</code>).
 * @param ksize blurring kernel size.
 * @param anchor anchor point; default value <code>Point(-1,-1)</code> means
 * that the anchor is at the kernel center.
 * @param normalize flag, specifying whether the kernel is normalized by its
 * area or not.
 * @param borderType border mode used to extrapolate pixels outside of the
 * image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#boxfilter">org.opencv.imgproc.Imgproc.boxFilter</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#medianBlur
 * @see org.opencv.imgproc.Imgproc#integral
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#blur
 */
    public static void boxFilter(Mat src, Mat dst, int ddepth, Size ksize, Point anchor, boolean normalize, int borderType)
    {

        boxFilter_0(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height, anchor.x, anchor.y, normalize, borderType);

        return;
    }

/**
 * <p>Blurs an image using the box filter.</p>
 *
 * <p>The function smoothes an image using the kernel:</p>
 *
 * <p><em>K = alpha 1 1 1 *s 1 1
 * 1 1 1 *s 1 1..................
 * 1 1 1 *s 1 1 </em></p>
 *
 * <p>where</p>
 *
 * <p><em>alpha = 1/(ksize.width*ksize.height) when normalize=true; 1
 * otherwise</em></p>
 *
 * <p>Unnormalized box filter is useful for computing various integral
 * characteristics over each pixel neighborhood, such as covariance matrices of
 * image derivatives (used in dense optical flow algorithms, and so on). If you
 * need to compute pixel sums over variable-size windows, use "integral".</p>
 *
 * @param src input image.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ddepth the output image depth (-1 to use <code>src.depth()</code>).
 * @param ksize blurring kernel size.
 * @param anchor anchor point; default value <code>Point(-1,-1)</code> means
 * that the anchor is at the kernel center.
 * @param normalize flag, specifying whether the kernel is normalized by its
 * area or not.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#boxfilter">org.opencv.imgproc.Imgproc.boxFilter</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#medianBlur
 * @see org.opencv.imgproc.Imgproc#integral
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#blur
 */
    public static void boxFilter(Mat src, Mat dst, int ddepth, Size ksize, Point anchor, boolean normalize)
    {

        boxFilter_1(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height, anchor.x, anchor.y, normalize);

        return;
    }

/**
 * <p>Blurs an image using the box filter.</p>
 *
 * <p>The function smoothes an image using the kernel:</p>
 *
 * <p><em>K = alpha 1 1 1 *s 1 1
 * 1 1 1 *s 1 1..................
 * 1 1 1 *s 1 1 </em></p>
 *
 * <p>where</p>
 *
 * <p><em>alpha = 1/(ksize.width*ksize.height) when normalize=true; 1
 * otherwise</em></p>
 *
 * <p>Unnormalized box filter is useful for computing various integral
 * characteristics over each pixel neighborhood, such as covariance matrices of
 * image derivatives (used in dense optical flow algorithms, and so on). If you
 * need to compute pixel sums over variable-size windows, use "integral".</p>
 *
 * @param src input image.
 * @param dst output image of the same size and type as <code>src</code>.
 * @param ddepth the output image depth (-1 to use <code>src.depth()</code>).
 * @param ksize blurring kernel size.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#boxfilter">org.opencv.imgproc.Imgproc.boxFilter</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#medianBlur
 * @see org.opencv.imgproc.Imgproc#integral
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#blur
 */
    public static void boxFilter(Mat src, Mat dst, int ddepth, Size ksize)
    {

        boxFilter_2(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height);

        return;
    }

    //
    // C++:  void calcBackProject(vector_Mat images, vector_int channels, Mat hist, Mat& dst, vector_float ranges, double scale)
    //

/**
 * <p>Calculates the back projection of a histogram.</p>
 *
 * <p>The functions <code>calcBackProject</code> calculate the back project of the
 * histogram. That is, similarly to <code>calcHist</code>, at each location
 * <code>(x, y)</code> the function collects the values from the selected
 * channels in the input images and finds the corresponding histogram bin. But
 * instead of incrementing it, the function reads the bin value, scales it by
 * <code>scale</code>, and stores in <code>backProject(x,y)</code>. In terms of
 * statistics, the function computes probability of each element value in
 * respect with the empirical probability distribution represented by the
 * histogram. See how, for example, you can find and track a bright-colored
 * object in a scene:</p>
 * <ul>
 *   <li> Before tracking, show the object to the camera so that it covers
 * almost the whole frame. Calculate a hue histogram. The histogram may have
 * strong maximums, corresponding to the dominant colors in the object.
 *   <li> When tracking, calculate a back projection of a hue plane of each
 * input video frame using that pre-computed histogram. Threshold the back
 * projection to suppress weak colors. It may also make sense to suppress pixels
 * with non-sufficient color saturation and too dark or too bright pixels.
 *   <li> Find connected components in the resulting picture and choose, for
 * example, the largest component.
 * </ul>
 *
 * <p>This is an approximate algorithm of the "CamShift" color object tracker.</p>
 *
 * @param images Source arrays. They all should have the same depth,
 * <code>CV_8U</code> or <code>CV_32F</code>, and the same size. Each of them
 * can have an arbitrary number of channels.
 * @param channels The list of channels used to compute the back projection. The
 * number of channels must match the histogram dimensionality. The first array
 * channels are numerated from 0 to <code>images[0].channels()-1</code>, the
 * second array channels are counted from <code>images[0].channels()</code> to
 * <code>images[0].channels() + images[1].channels()-1</code>, and so on.
 * @param hist Input histogram that can be dense or sparse.
 * @param dst a dst
 * @param ranges Array of arrays of the histogram bin boundaries in each
 * dimension. See "calcHist".
 * @param scale Optional scale factor for the output back projection.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/histograms.html#calcbackproject">org.opencv.imgproc.Imgproc.calcBackProject</a>
 * @see org.opencv.imgproc.Imgproc#calcHist
 */
    public static void calcBackProject(List<Mat> images, MatOfInt channels, Mat hist, Mat dst, MatOfFloat ranges, double scale)
    {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        Mat channels_mat = channels;
        Mat ranges_mat = ranges;
        calcBackProject_0(images_mat.nativeObj, channels_mat.nativeObj, hist.nativeObj, dst.nativeObj, ranges_mat.nativeObj, scale);

        return;
    }

    //
    // C++:  void calcHist(vector_Mat images, vector_int channels, Mat mask, Mat& hist, vector_int histSize, vector_float ranges, bool accumulate = false)
    //

/**
 * <p>Calculates a histogram of a set of arrays.</p>
 *
 * <p>The functions <code>calcHist</code> calculate the histogram of one or more
 * arrays. The elements of a tuple used to increment a histogram bin are taken
 * from the correspondinginput arrays at the same location. The sample below
 * shows how to compute a 2D Hue-Saturation histogram for a color image.
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include <cv.h></p>
 *
 * <p>#include <highgui.h></p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src, hsv;</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 1)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>cvtColor(src, hsv, CV_BGR2HSV);</p>
 *
 * <p>// Quantize the hue to 30 levels</p>
 *
 * <p>// and the saturation to 32 levels</p>
 *
 * <p>int hbins = 30, sbins = 32;</p>
 *
 * <p>int histSize[] = {hbins, sbins};</p>
 *
 * <p>// hue varies from 0 to 179, see cvtColor</p>
 *
 * <p>float hranges[] = { 0, 180 };</p>
 *
 * <p>// saturation varies from 0 (black-gray-white) to</p>
 *
 * <p>// 255 (pure spectrum color)</p>
 *
 * <p>float sranges[] = { 0, 256 };</p>
 *
 * <p>const float* ranges[] = { hranges, sranges };</p>
 *
 * <p>MatND hist;</p>
 *
 * <p>// we compute the histogram from the 0-th and 1-st channels</p>
 *
 * <p>int channels[] = {0, 1};</p>
 *
 * <p>calcHist(&hsv, 1, channels, Mat(), // do not use mask</p>
 *
 * <p>hist, 2, histSize, ranges,</p>
 *
 * <p>true, // the histogram is uniform</p>
 *
 * <p>false);</p>
 *
 * <p>double maxVal=0;</p>
 *
 * <p>minMaxLoc(hist, 0, &maxVal, 0, 0);</p>
 *
 * <p>int scale = 10;</p>
 *
 * <p>Mat histImg = Mat.zeros(sbins*scale, hbins*10, CV_8UC3);</p>
 *
 * <p>for(int h = 0; h < hbins; h++)</p>
 *
 * <p>for(int s = 0; s < sbins; s++)</p>
 *
 *
 * <p>float binVal = hist.at<float>(h, s);</p>
 *
 * <p>int intensity = cvRound(binVal*255/maxVal);</p>
 *
 * <p>rectangle(histImg, Point(h*scale, s*scale),</p>
 *
 * <p>Point((h+1)*scale - 1, (s+1)*scale - 1),</p>
 *
 * <p>Scalar.all(intensity),</p>
 *
 * <p>CV_FILLED);</p>
 *
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>namedWindow("H-S Histogram", 1);</p>
 *
 * <p>imshow("H-S Histogram", histImg);</p>
 *
 * <p>waitKey();</p>
 *
 *
 * <p>Note: </code></p>
 * <ul>
 *   <li> An example for creating histograms of an image can be found at
 * opencv_source_code/samples/cpp/demhist.cpp
 *   <li> (Python) An example for creating color histograms can be found at
 * opencv_source/samples/python2/color_histogram.py
 *   <li> (Python) An example illustrating RGB and grayscale histogram plotting
 * can be found at opencv_source/samples/python2/hist.py
 * </ul>
 *
 * @param images Source arrays. They all should have the same depth,
 * <code>CV_8U</code> or <code>CV_32F</code>, and the same size. Each of them
 * can have an arbitrary number of channels.
 * @param channels List of the <code>dims</code> channels used to compute the
 * histogram. The first array channels are numerated from 0 to <code>images[0].channels()-1</code>,
 * the second array channels are counted from <code>images[0].channels()</code>
 * to <code>images[0].channels() + images[1].channels()-1</code>, and so on.
 * @param mask Optional mask. If the matrix is not empty, it must be an 8-bit
 * array of the same size as <code>images[i]</code>. The non-zero mask elements
 * mark the array elements counted in the histogram.
 * @param hist Output histogram, which is a dense or sparse <code>dims</code>
 * -dimensional array.
 * @param histSize Array of histogram sizes in each dimension.
 * @param ranges Array of the <code>dims</code> arrays of the histogram bin
 * boundaries in each dimension. When the histogram is uniform (<code>uniform</code>
 * =true), then for each dimension <code>i</code> it is enough to specify the
 * lower (inclusive) boundary <em>L_0</em> of the 0-th histogram bin and the
 * upper (exclusive) boundary <em>U_(histSize[i]-1)</em> for the last histogram
 * bin <code>histSize[i]-1</code>. That is, in case of a uniform histogram each
 * of <code>ranges[i]</code> is an array of 2 elements. When the histogram is
 * not uniform (<code>uniform=false</code>), then each of <code>ranges[i]</code>
 * contains <code>histSize[i]+1</code> elements: <em>L_0, U_0=L_1, U_1=L_2,...,
 * U_(histSize[i]-2)=L_(histSize[i]-1), U_(histSize[i]-1)</em>. The array
 * elements, that are not between <em>L_0</em> and <em>U_(histSize[i]-1)</em>,
 * are not counted in the histogram.
 * @param accumulate Accumulation flag. If it is set, the histogram is not
 * cleared in the beginning when it is allocated. This feature enables you to
 * compute a single histogram from several sets of arrays, or to update the
 * histogram in time.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/histograms.html#calchist">org.opencv.imgproc.Imgproc.calcHist</a>
 */
    public static void calcHist(List<Mat> images, MatOfInt channels, Mat mask, Mat hist, MatOfInt histSize, MatOfFloat ranges, boolean accumulate)
    {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        Mat channels_mat = channels;
        Mat histSize_mat = histSize;
        Mat ranges_mat = ranges;
        calcHist_0(images_mat.nativeObj, channels_mat.nativeObj, mask.nativeObj, hist.nativeObj, histSize_mat.nativeObj, ranges_mat.nativeObj, accumulate);

        return;
    }

/**
 * <p>Calculates a histogram of a set of arrays.</p>
 *
 * <p>The functions <code>calcHist</code> calculate the histogram of one or more
 * arrays. The elements of a tuple used to increment a histogram bin are taken
 * from the correspondinginput arrays at the same location. The sample below
 * shows how to compute a 2D Hue-Saturation histogram for a color image.
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include <cv.h></p>
 *
 * <p>#include <highgui.h></p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src, hsv;</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 1)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>cvtColor(src, hsv, CV_BGR2HSV);</p>
 *
 * <p>// Quantize the hue to 30 levels</p>
 *
 * <p>// and the saturation to 32 levels</p>
 *
 * <p>int hbins = 30, sbins = 32;</p>
 *
 * <p>int histSize[] = {hbins, sbins};</p>
 *
 * <p>// hue varies from 0 to 179, see cvtColor</p>
 *
 * <p>float hranges[] = { 0, 180 };</p>
 *
 * <p>// saturation varies from 0 (black-gray-white) to</p>
 *
 * <p>// 255 (pure spectrum color)</p>
 *
 * <p>float sranges[] = { 0, 256 };</p>
 *
 * <p>const float* ranges[] = { hranges, sranges };</p>
 *
 * <p>MatND hist;</p>
 *
 * <p>// we compute the histogram from the 0-th and 1-st channels</p>
 *
 * <p>int channels[] = {0, 1};</p>
 *
 * <p>calcHist(&hsv, 1, channels, Mat(), // do not use mask</p>
 *
 * <p>hist, 2, histSize, ranges,</p>
 *
 * <p>true, // the histogram is uniform</p>
 *
 * <p>false);</p>
 *
 * <p>double maxVal=0;</p>
 *
 * <p>minMaxLoc(hist, 0, &maxVal, 0, 0);</p>
 *
 * <p>int scale = 10;</p>
 *
 * <p>Mat histImg = Mat.zeros(sbins*scale, hbins*10, CV_8UC3);</p>
 *
 * <p>for(int h = 0; h < hbins; h++)</p>
 *
 * <p>for(int s = 0; s < sbins; s++)</p>
 *
 *
 * <p>float binVal = hist.at<float>(h, s);</p>
 *
 * <p>int intensity = cvRound(binVal*255/maxVal);</p>
 *
 * <p>rectangle(histImg, Point(h*scale, s*scale),</p>
 *
 * <p>Point((h+1)*scale - 1, (s+1)*scale - 1),</p>
 *
 * <p>Scalar.all(intensity),</p>
 *
 * <p>CV_FILLED);</p>
 *
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>namedWindow("H-S Histogram", 1);</p>
 *
 * <p>imshow("H-S Histogram", histImg);</p>
 *
 * <p>waitKey();</p>
 *
 *
 * <p>Note: </code></p>
 * <ul>
 *   <li> An example for creating histograms of an image can be found at
 * opencv_source_code/samples/cpp/demhist.cpp
 *   <li> (Python) An example for creating color histograms can be found at
 * opencv_source/samples/python2/color_histogram.py
 *   <li> (Python) An example illustrating RGB and grayscale histogram plotting
 * can be found at opencv_source/samples/python2/hist.py
 * </ul>
 *
 * @param images Source arrays. They all should have the same depth,
 * <code>CV_8U</code> or <code>CV_32F</code>, and the same size. Each of them
 * can have an arbitrary number of channels.
 * @param channels List of the <code>dims</code> channels used to compute the
 * histogram. The first array channels are numerated from 0 to <code>images[0].channels()-1</code>,
 * the second array channels are counted from <code>images[0].channels()</code>
 * to <code>images[0].channels() + images[1].channels()-1</code>, and so on.
 * @param mask Optional mask. If the matrix is not empty, it must be an 8-bit
 * array of the same size as <code>images[i]</code>. The non-zero mask elements
 * mark the array elements counted in the histogram.
 * @param hist Output histogram, which is a dense or sparse <code>dims</code>
 * -dimensional array.
 * @param histSize Array of histogram sizes in each dimension.
 * @param ranges Array of the <code>dims</code> arrays of the histogram bin
 * boundaries in each dimension. When the histogram is uniform (<code>uniform</code>
 * =true), then for each dimension <code>i</code> it is enough to specify the
 * lower (inclusive) boundary <em>L_0</em> of the 0-th histogram bin and the
 * upper (exclusive) boundary <em>U_(histSize[i]-1)</em> for the last histogram
 * bin <code>histSize[i]-1</code>. That is, in case of a uniform histogram each
 * of <code>ranges[i]</code> is an array of 2 elements. When the histogram is
 * not uniform (<code>uniform=false</code>), then each of <code>ranges[i]</code>
 * contains <code>histSize[i]+1</code> elements: <em>L_0, U_0=L_1, U_1=L_2,...,
 * U_(histSize[i]-2)=L_(histSize[i]-1), U_(histSize[i]-1)</em>. The array
 * elements, that are not between <em>L_0</em> and <em>U_(histSize[i]-1)</em>,
 * are not counted in the histogram.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/histograms.html#calchist">org.opencv.imgproc.Imgproc.calcHist</a>
 */
    public static void calcHist(List<Mat> images, MatOfInt channels, Mat mask, Mat hist, MatOfInt histSize, MatOfFloat ranges)
    {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        Mat channels_mat = channels;
        Mat histSize_mat = histSize;
        Mat ranges_mat = ranges;
        calcHist_1(images_mat.nativeObj, channels_mat.nativeObj, mask.nativeObj, hist.nativeObj, histSize_mat.nativeObj, ranges_mat.nativeObj);

        return;
    }

    //
    // C++:  double compareHist(Mat H1, Mat H2, int method)
    //

/**
 * <p>Compares two histograms.</p>
 *
 * <p>The functions <code>compareHist</code> compare two dense or two sparse
 * histograms using the specified method:</p>
 * <ul>
 *   <li> Correlation (<code>method=CV_COMP_CORREL</code>)
 * </ul>
 *
 * <p><em>d(H_1,H_2) = (sum_I(H_1(I) - H_1")(H_2(I) - H_2"))/(sqrt(sum_I(H_1(I) -
 * H_1")^2 sum_I(H_2(I) - H_2")^2))</em></p>
 *
 * <p>where</p>
 *
 * <p><em>H_k" = 1/(N) sum _J H_k(J)</em></p>
 *
 * <p>and <em>N</em> is a total number of histogram bins.</p>
 * <ul>
 *   <li> Chi-Square (<code>method=CV_COMP_CHISQR</code>)
 * </ul>
 *
 * <p><em>d(H_1,H_2) = sum _I((H_1(I)-H_2(I))^2)/(H_1(I))</em></p>
 *
 * <ul>
 *   <li> Intersection (<code>method=CV_COMP_INTERSECT</code>)
 * </ul>
 *
 * <p><em>d(H_1,H_2) = sum _I min(H_1(I), H_2(I))</em></p>
 *
 * <ul>
 *   <li> Bhattacharyya distance (<code>method=CV_COMP_BHATTACHARYYA</code> or
 * <code>method=CV_COMP_HELLINGER</code>). In fact, OpenCV computes Hellinger
 * distance, which is related to Bhattacharyya coefficient.
 * </ul>
 *
 * <p><em>d(H_1,H_2) = sqrt(1 - frac(1)(sqrt(H_1" H_2" N^2)) sum_I sqrt(H_1(I) *
 * H_2(I)))</em></p>
 *
 * <p>The function returns <em>d(H_1, H_2)</em>.</p>
 *
 * <p>While the function works well with 1-, 2-, 3-dimensional dense histograms, it
 * may not be suitable for high-dimensional sparse histograms. In such
 * histograms, because of aliasing and sampling problems, the coordinates of
 * non-zero histogram bins can slightly shift. To compare such histograms or
 * more general sparse configurations of weighted points, consider using the
 * "EMD" function.</p>
 *
 * @param H1 First compared histogram.
 * @param H2 Second compared histogram of the same size as <code>H1</code>.
 * @param method Comparison method that could be one of the following:
 * <ul>
 *   <li> CV_COMP_CORREL Correlation
 *   <li> CV_COMP_CHISQR Chi-Square
 *   <li> CV_COMP_INTERSECT Intersection
 *   <li> CV_COMP_BHATTACHARYYA Bhattacharyya distance
 *   <li> CV_COMP_HELLINGER Synonym for <code>CV_COMP_BHATTACHARYYA</code>
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/histograms.html#comparehist">org.opencv.imgproc.Imgproc.compareHist</a>
 */
    public static double compareHist(Mat H1, Mat H2, int method)
    {

        double retVal = compareHist_0(H1.nativeObj, H2.nativeObj, method);

        return retVal;
    }

    //
    // C++:  double contourArea(Mat contour, bool oriented = false)
    //

/**
 * <p>Calculates a contour area.</p>
 *
 * <p>The function computes a contour area. Similarly to "moments", the area is
 * computed using the Green formula. Thus, the returned area and the number of
 * non-zero pixels, if you draw the contour using "drawContours" or "fillPoly",
 * can be different.
 * Also, the function will most certainly give a wrong results for contours with
 * self-intersections.
 * Example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>vector<Point> contour;</p>
 *
 * <p>contour.push_back(Point2f(0, 0));</p>
 *
 * <p>contour.push_back(Point2f(10, 0));</p>
 *
 * <p>contour.push_back(Point2f(10, 10));</p>
 *
 * <p>contour.push_back(Point2f(5, 4));</p>
 *
 * <p>double area0 = contourArea(contour);</p>
 *
 * <p>vector<Point> approx;</p>
 *
 * <p>approxPolyDP(contour, approx, 5, true);</p>
 *
 * <p>double area1 = contourArea(approx);</p>
 *
 * <p>cout << "area0 =" << area0 << endl <<</p>
 *
 * <p>"area1 =" << area1 << endl <<</p>
 *
 * <p>"approx poly vertices" << approx.size() << endl;</p>
 *
 * @param contour Input vector of 2D points (contour vertices), stored in
 * <code>std.vector</code> or <code>Mat</code>.
 * @param oriented Oriented area flag. If it is true, the function returns a
 * signed area value, depending on the contour orientation (clockwise or
 * counter-clockwise). Using this feature you can determine orientation of a
 * contour by taking the sign of an area. By default, the parameter is
 * <code>false</code>, which means that the absolute value is returned.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#contourarea">org.opencv.imgproc.Imgproc.contourArea</a>
 */
    public static double contourArea(Mat contour, boolean oriented)
    {

        double retVal = contourArea_0(contour.nativeObj, oriented);

        return retVal;
    }

/**
 * <p>Calculates a contour area.</p>
 *
 * <p>The function computes a contour area. Similarly to "moments", the area is
 * computed using the Green formula. Thus, the returned area and the number of
 * non-zero pixels, if you draw the contour using "drawContours" or "fillPoly",
 * can be different.
 * Also, the function will most certainly give a wrong results for contours with
 * self-intersections.
 * Example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>vector<Point> contour;</p>
 *
 * <p>contour.push_back(Point2f(0, 0));</p>
 *
 * <p>contour.push_back(Point2f(10, 0));</p>
 *
 * <p>contour.push_back(Point2f(10, 10));</p>
 *
 * <p>contour.push_back(Point2f(5, 4));</p>
 *
 * <p>double area0 = contourArea(contour);</p>
 *
 * <p>vector<Point> approx;</p>
 *
 * <p>approxPolyDP(contour, approx, 5, true);</p>
 *
 * <p>double area1 = contourArea(approx);</p>
 *
 * <p>cout << "area0 =" << area0 << endl <<</p>
 *
 * <p>"area1 =" << area1 << endl <<</p>
 *
 * <p>"approx poly vertices" << approx.size() << endl;</p>
 *
 * @param contour Input vector of 2D points (contour vertices), stored in
 * <code>std.vector</code> or <code>Mat</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#contourarea">org.opencv.imgproc.Imgproc.contourArea</a>
 */
    public static double contourArea(Mat contour)
    {

        double retVal = contourArea_1(contour.nativeObj);

        return retVal;
    }

    //
    // C++:  void convertMaps(Mat map1, Mat map2, Mat& dstmap1, Mat& dstmap2, int dstmap1type, bool nninterpolation = false)
    //

/**
 * <p>Converts image transformation maps from one representation to another.</p>
 *
 * <p>The function converts a pair of maps for "remap" from one representation to
 * another. The following options (<code>(map1.type(), map2.type())</code>
 * <em>-></em> <code>(dstmap1.type(), dstmap2.type())</code>) are supported:</p>
 * <ul>
 *   <li> <em>(CV_32FC1, CV_32FC1) -> (CV_16SC2, CV_16UC1)</em>. This is the
 * most frequently used conversion operation, in which the original
 * floating-point maps (see "remap") are converted to a more compact and much
 * faster fixed-point representation. The first output array contains the
 * rounded coordinates and the second array (created only when <code>nninterpolation=false</code>)
 * contains indices in the interpolation tables.
 *   <li> <em>(CV_32FC2) -> (CV_16SC2, CV_16UC1)</em>. The same as above but the
 * original maps are stored in one 2-channel matrix.
 *   <li> Reverse conversion. Obviously, the reconstructed floating-point maps
 * will not be exactly the same as the originals.
 * </ul>
 *
 * @param map1 The first input map of type <code>CV_16SC2</code>,
 * <code>CV_32FC1</code>, or <code>CV_32FC2</code>.
 * @param map2 The second input map of type <code>CV_16UC1</code>,
 * <code>CV_32FC1</code>, or none (empty matrix), respectively.
 * @param dstmap1 The first output map that has the type <code>dstmap1type</code>
 * and the same size as <code>src</code>.
 * @param dstmap2 The second output map.
 * @param dstmap1type Type of the first output map that should be
 * <code>CV_16SC2</code>, <code>CV_32FC1</code>, or <code>CV_32FC2</code>.
 * @param nninterpolation Flag indicating whether the fixed-point maps are used
 * for the nearest-neighbor or for a more complex interpolation.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#convertmaps">org.opencv.imgproc.Imgproc.convertMaps</a>
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#initUndistortRectifyMap
 * @see org.opencv.imgproc.Imgproc#undistort
 */
    public static void convertMaps(Mat map1, Mat map2, Mat dstmap1, Mat dstmap2, int dstmap1type, boolean nninterpolation)
    {

        convertMaps_0(map1.nativeObj, map2.nativeObj, dstmap1.nativeObj, dstmap2.nativeObj, dstmap1type, nninterpolation);

        return;
    }

/**
 * <p>Converts image transformation maps from one representation to another.</p>
 *
 * <p>The function converts a pair of maps for "remap" from one representation to
 * another. The following options (<code>(map1.type(), map2.type())</code>
 * <em>-></em> <code>(dstmap1.type(), dstmap2.type())</code>) are supported:</p>
 * <ul>
 *   <li> <em>(CV_32FC1, CV_32FC1) -> (CV_16SC2, CV_16UC1)</em>. This is the
 * most frequently used conversion operation, in which the original
 * floating-point maps (see "remap") are converted to a more compact and much
 * faster fixed-point representation. The first output array contains the
 * rounded coordinates and the second array (created only when <code>nninterpolation=false</code>)
 * contains indices in the interpolation tables.
 *   <li> <em>(CV_32FC2) -> (CV_16SC2, CV_16UC1)</em>. The same as above but the
 * original maps are stored in one 2-channel matrix.
 *   <li> Reverse conversion. Obviously, the reconstructed floating-point maps
 * will not be exactly the same as the originals.
 * </ul>
 *
 * @param map1 The first input map of type <code>CV_16SC2</code>,
 * <code>CV_32FC1</code>, or <code>CV_32FC2</code>.
 * @param map2 The second input map of type <code>CV_16UC1</code>,
 * <code>CV_32FC1</code>, or none (empty matrix), respectively.
 * @param dstmap1 The first output map that has the type <code>dstmap1type</code>
 * and the same size as <code>src</code>.
 * @param dstmap2 The second output map.
 * @param dstmap1type Type of the first output map that should be
 * <code>CV_16SC2</code>, <code>CV_32FC1</code>, or <code>CV_32FC2</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#convertmaps">org.opencv.imgproc.Imgproc.convertMaps</a>
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#initUndistortRectifyMap
 * @see org.opencv.imgproc.Imgproc#undistort
 */
    public static void convertMaps(Mat map1, Mat map2, Mat dstmap1, Mat dstmap2, int dstmap1type)
    {

        convertMaps_1(map1.nativeObj, map2.nativeObj, dstmap1.nativeObj, dstmap2.nativeObj, dstmap1type);

        return;
    }

    //
    // C++:  void convexHull(vector_Point points, vector_int& hull, bool clockwise = false,  _hidden_  returnPoints = true)
    //

/**
 * <p>Finds the convex hull of a point set.</p>
 *
 * <p>The functions find the convex hull of a 2D point set using the Sklansky's
 * algorithm [Sklansky82] that has *O(N logN)* complexity in the current
 * implementation. See the OpenCV sample <code>convexhull.cpp</code> that
 * demonstrates the usage of different function variants.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the convexHull functionality can be found at
 * opencv_source_code/samples/cpp/convexhull.cpp
 * </ul>
 *
 * @param points Input 2D point set, stored in <code>std.vector</code> or
 * <code>Mat</code>.
 * @param hull Output convex hull. It is either an integer vector of indices or
 * vector of points. In the first case, the <code>hull</code> elements are
 * 0-based indices of the convex hull points in the original array (since the
 * set of convex hull points is a subset of the original point set). In the
 * second case, <code>hull</code> elements are the convex hull points
 * themselves.
 * @param clockwise Orientation flag. If it is true, the output convex hull is
 * oriented clockwise. Otherwise, it is oriented counter-clockwise. The assumed
 * coordinate system has its X axis pointing to the right, and its Y axis
 * pointing upwards.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#convexhull">org.opencv.imgproc.Imgproc.convexHull</a>
 */
    public static void convexHull(MatOfPoint points, MatOfInt hull, boolean clockwise)
    {
        Mat points_mat = points;
        Mat hull_mat = hull;
        convexHull_0(points_mat.nativeObj, hull_mat.nativeObj, clockwise);

        return;
    }

/**
 * <p>Finds the convex hull of a point set.</p>
 *
 * <p>The functions find the convex hull of a 2D point set using the Sklansky's
 * algorithm [Sklansky82] that has *O(N logN)* complexity in the current
 * implementation. See the OpenCV sample <code>convexhull.cpp</code> that
 * demonstrates the usage of different function variants.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the convexHull functionality can be found at
 * opencv_source_code/samples/cpp/convexhull.cpp
 * </ul>
 *
 * @param points Input 2D point set, stored in <code>std.vector</code> or
 * <code>Mat</code>.
 * @param hull Output convex hull. It is either an integer vector of indices or
 * vector of points. In the first case, the <code>hull</code> elements are
 * 0-based indices of the convex hull points in the original array (since the
 * set of convex hull points is a subset of the original point set). In the
 * second case, <code>hull</code> elements are the convex hull points
 * themselves.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#convexhull">org.opencv.imgproc.Imgproc.convexHull</a>
 */
    public static void convexHull(MatOfPoint points, MatOfInt hull)
    {
        Mat points_mat = points;
        Mat hull_mat = hull;
        convexHull_1(points_mat.nativeObj, hull_mat.nativeObj);

        return;
    }

    //
    // C++:  void convexityDefects(vector_Point contour, vector_int convexhull, vector_Vec4i& convexityDefects)
    //

/**
 * <p>Finds the convexity defects of a contour.</p>
 *
 * <p>The function finds all convexity defects of the input contour and returns a
 * sequence of the <code>CvConvexityDefect</code> structures, where
 * <code>CvConvexityDetect</code> is defined as: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>struct CvConvexityDefect</p>
 *
 *
 * <p>CvPoint* start; // point of the contour where the defect begins</p>
 *
 * <p>CvPoint* end; // point of the contour where the defect ends</p>
 *
 * <p>CvPoint* depth_point; // the farthest from the convex hull point within the
 * defect</p>
 *
 * <p>float depth; // distance between the farthest point and the convex hull</p>
 *
 * <p>};</p>
 *
 * <p>The figure below displays convexity defects of a hand contour: </code></p>
 *
 * @param contour Input contour.
 * @param convexhull Convex hull obtained using "convexHull" that should contain
 * indices of the contour points that make the hull.
 * @param convexityDefects The output vector of convexity defects. In C++ and
 * the new Python/Java interface each convexity defect is represented as
 * 4-element integer vector (a.k.a. <code>cv.Vec4i</code>): <code>(start_index,
 * end_index, farthest_pt_index, fixpt_depth)</code>, where indices are 0-based
 * indices in the original contour of the convexity defect beginning, end and
 * the farthest point, and <code>fixpt_depth</code> is fixed-point approximation
 * (with 8 fractional bits) of the distance between the farthest contour point
 * and the hull. That is, to get the floating-point value of the depth will be
 * <code>fixpt_depth/256.0</code>. In C interface convexity defect is
 * represented by <code>CvConvexityDefect</code> structure - see below.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#convexitydefects">org.opencv.imgproc.Imgproc.convexityDefects</a>
 */
    public static void convexityDefects(MatOfPoint contour, MatOfInt convexhull, MatOfInt4 convexityDefects)
    {
        Mat contour_mat = contour;
        Mat convexhull_mat = convexhull;
        Mat convexityDefects_mat = convexityDefects;
        convexityDefects_0(contour_mat.nativeObj, convexhull_mat.nativeObj, convexityDefects_mat.nativeObj);

        return;
    }

    //
    // C++:  void copyMakeBorder(Mat src, Mat& dst, int top, int bottom, int left, int right, int borderType, Scalar value = Scalar())
    //

/**
 * <p>Forms a border around an image.</p>
 *
 * <p>The function copies the source image into the middle of the destination
 * image. The areas to the left, to the right, above and below the copied source
 * image will be filled with extrapolated pixels. This is not what
 * "FilterEngine" or filtering functions based on it do (they extrapolate pixels
 * on-fly), but what other more complex functions, including your own, may do to
 * simplify image boundary handling.
 * The function supports the mode when <code>src</code> is already in the middle
 * of <code>dst</code>. In this case, the function does not copy
 * <code>src</code> itself but simply constructs the border, for example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// let border be the same in all directions</p>
 *
 * <p>int border=2;</p>
 *
 * <p>// constructs a larger image to fit both the image and the border</p>
 *
 * <p>Mat gray_buf(rgb.rows + border*2, rgb.cols + border*2, rgb.depth());</p>
 *
 * <p>// select the middle part of it w/o copying data</p>
 *
 * <p>Mat gray(gray_canvas, Rect(border, border, rgb.cols, rgb.rows));</p>
 *
 * <p>// convert image from RGB to grayscale</p>
 *
 * <p>cvtColor(rgb, gray, CV_RGB2GRAY);</p>
 *
 * <p>// form a border in-place</p>
 *
 * <p>copyMakeBorder(gray, gray_buf, border, border,</p>
 *
 * <p>border, border, BORDER_REPLICATE);</p>
 *
 * <p>// now do some custom filtering......</p>
 *
 * <p>Note: </code></p>
 *
 * <p>When the source image is a part (ROI) of a bigger image, the function will
 * try to use the pixels outside of the ROI to form a border. To disable this
 * feature and always do extrapolation, as if <code>src</code> was not a ROI,
 * use <code>borderType | BORDER_ISOLATED</code>.</p>
 *
 * @param src Source image.
 * @param dst Destination image of the same type as <code>src</code> and the
 * size <code>Size(src.cols+left+right, src.rows+top+bottom)</code>.
 * @param top a top
 * @param bottom a bottom
 * @param left a left
 * @param right Parameter specifying how many pixels in each direction from the
 * source image rectangle to extrapolate. For example, <code>top=1, bottom=1,
 * left=1, right=1</code> mean that 1 pixel-wide border needs to be built.
 * @param borderType Border type. See "borderInterpolate" for details.
 * @param value Border value if <code>borderType==BORDER_CONSTANT</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#copymakeborder">org.opencv.imgproc.Imgproc.copyMakeBorder</a>
 * @see org.opencv.imgproc.Imgproc#borderInterpolate
 */
    public static void copyMakeBorder(Mat src, Mat dst, int top, int bottom, int left, int right, int borderType, Scalar value)
    {

        copyMakeBorder_0(src.nativeObj, dst.nativeObj, top, bottom, left, right, borderType, value.val[0], value.val[1], value.val[2], value.val[3]);

        return;
    }

/**
 * <p>Forms a border around an image.</p>
 *
 * <p>The function copies the source image into the middle of the destination
 * image. The areas to the left, to the right, above and below the copied source
 * image will be filled with extrapolated pixels. This is not what
 * "FilterEngine" or filtering functions based on it do (they extrapolate pixels
 * on-fly), but what other more complex functions, including your own, may do to
 * simplify image boundary handling.
 * The function supports the mode when <code>src</code> is already in the middle
 * of <code>dst</code>. In this case, the function does not copy
 * <code>src</code> itself but simply constructs the border, for example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// let border be the same in all directions</p>
 *
 * <p>int border=2;</p>
 *
 * <p>// constructs a larger image to fit both the image and the border</p>
 *
 * <p>Mat gray_buf(rgb.rows + border*2, rgb.cols + border*2, rgb.depth());</p>
 *
 * <p>// select the middle part of it w/o copying data</p>
 *
 * <p>Mat gray(gray_canvas, Rect(border, border, rgb.cols, rgb.rows));</p>
 *
 * <p>// convert image from RGB to grayscale</p>
 *
 * <p>cvtColor(rgb, gray, CV_RGB2GRAY);</p>
 *
 * <p>// form a border in-place</p>
 *
 * <p>copyMakeBorder(gray, gray_buf, border, border,</p>
 *
 * <p>border, border, BORDER_REPLICATE);</p>
 *
 * <p>// now do some custom filtering......</p>
 *
 * <p>Note: </code></p>
 *
 * <p>When the source image is a part (ROI) of a bigger image, the function will
 * try to use the pixels outside of the ROI to form a border. To disable this
 * feature and always do extrapolation, as if <code>src</code> was not a ROI,
 * use <code>borderType | BORDER_ISOLATED</code>.</p>
 *
 * @param src Source image.
 * @param dst Destination image of the same type as <code>src</code> and the
 * size <code>Size(src.cols+left+right, src.rows+top+bottom)</code>.
 * @param top a top
 * @param bottom a bottom
 * @param left a left
 * @param right Parameter specifying how many pixels in each direction from the
 * source image rectangle to extrapolate. For example, <code>top=1, bottom=1,
 * left=1, right=1</code> mean that 1 pixel-wide border needs to be built.
 * @param borderType Border type. See "borderInterpolate" for details.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#copymakeborder">org.opencv.imgproc.Imgproc.copyMakeBorder</a>
 * @see org.opencv.imgproc.Imgproc#borderInterpolate
 */
    public static void copyMakeBorder(Mat src, Mat dst, int top, int bottom, int left, int right, int borderType)
    {

        copyMakeBorder_1(src.nativeObj, dst.nativeObj, top, bottom, left, right, borderType);

        return;
    }

    //
    // C++:  void cornerEigenValsAndVecs(Mat src, Mat& dst, int blockSize, int ksize, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Calculates eigenvalues and eigenvectors of image blocks for corner detection.</p>
 *
 * <p>For every pixel <em>p</em>, the function <code>cornerEigenValsAndVecs</code>
 * considers a <code>blockSize</code> <em>x</em> <code>blockSize</code>
 * neighborhood <em>S(p)</em>. It calculates the covariation matrix of
 * derivatives over the neighborhood as:</p>
 *
 * <p><em>M = sum(by: S(p))(dI/dx)^2 sum(by: S(p))(dI/dx dI/dy)^2
 * sum(by: S(p))(dI/dx dI/dy)^2 sum(by: S(p))(dI/dy)^2 </em></p>
 *
 * <p>where the derivatives are computed using the "Sobel" operator.</p>
 *
 * <p>After that, it finds eigenvectors and eigenvalues of <em>M</em> and stores
 * them in the destination image as <em>(lambda_1, lambda_2, x_1, y_1, x_2,
 * y_2)</em> where</p>
 * <ul>
 *   <li> <em>lambda_1, lambda_2</em> are the non-sorted eigenvalues of
 * <em>M</em>
 *   <li> <em>x_1, y_1</em> are the eigenvectors corresponding to
 * <em>lambda_1</em>
 *   <li> <em>x_2, y_2</em> are the eigenvectors corresponding to
 * <em>lambda_2</em>
 * </ul>
 *
 * <p>The output of the function can be used for robust edge or corner detection.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> (Python) An example on how to use eigenvectors and eigenvalues to
 * estimate image texture flow direction can be found at opencv_source_code/samples/python2/texture_flow.py
 * </ul>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the results. It has the same size as
 * <code>src</code> and the type <code>CV_32FC(6)</code>.
 * @param blockSize Neighborhood size (see details below).
 * @param ksize Aperture parameter for the "Sobel" operator.
 * @param borderType Pixel extrapolation method. See "borderInterpolate".
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornereigenvalsandvecs">org.opencv.imgproc.Imgproc.cornerEigenValsAndVecs</a>
 * @see org.opencv.imgproc.Imgproc#cornerHarris
 * @see org.opencv.imgproc.Imgproc#cornerMinEigenVal
 * @see org.opencv.imgproc.Imgproc#preCornerDetect
 */
    public static void cornerEigenValsAndVecs(Mat src, Mat dst, int blockSize, int ksize, int borderType)
    {

        cornerEigenValsAndVecs_0(src.nativeObj, dst.nativeObj, blockSize, ksize, borderType);

        return;
    }

/**
 * <p>Calculates eigenvalues and eigenvectors of image blocks for corner detection.</p>
 *
 * <p>For every pixel <em>p</em>, the function <code>cornerEigenValsAndVecs</code>
 * considers a <code>blockSize</code> <em>x</em> <code>blockSize</code>
 * neighborhood <em>S(p)</em>. It calculates the covariation matrix of
 * derivatives over the neighborhood as:</p>
 *
 * <p><em>M = sum(by: S(p))(dI/dx)^2 sum(by: S(p))(dI/dx dI/dy)^2
 * sum(by: S(p))(dI/dx dI/dy)^2 sum(by: S(p))(dI/dy)^2 </em></p>
 *
 * <p>where the derivatives are computed using the "Sobel" operator.</p>
 *
 * <p>After that, it finds eigenvectors and eigenvalues of <em>M</em> and stores
 * them in the destination image as <em>(lambda_1, lambda_2, x_1, y_1, x_2,
 * y_2)</em> where</p>
 * <ul>
 *   <li> <em>lambda_1, lambda_2</em> are the non-sorted eigenvalues of
 * <em>M</em>
 *   <li> <em>x_1, y_1</em> are the eigenvectors corresponding to
 * <em>lambda_1</em>
 *   <li> <em>x_2, y_2</em> are the eigenvectors corresponding to
 * <em>lambda_2</em>
 * </ul>
 *
 * <p>The output of the function can be used for robust edge or corner detection.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> (Python) An example on how to use eigenvectors and eigenvalues to
 * estimate image texture flow direction can be found at opencv_source_code/samples/python2/texture_flow.py
 * </ul>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the results. It has the same size as
 * <code>src</code> and the type <code>CV_32FC(6)</code>.
 * @param blockSize Neighborhood size (see details below).
 * @param ksize Aperture parameter for the "Sobel" operator.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornereigenvalsandvecs">org.opencv.imgproc.Imgproc.cornerEigenValsAndVecs</a>
 * @see org.opencv.imgproc.Imgproc#cornerHarris
 * @see org.opencv.imgproc.Imgproc#cornerMinEigenVal
 * @see org.opencv.imgproc.Imgproc#preCornerDetect
 */
    public static void cornerEigenValsAndVecs(Mat src, Mat dst, int blockSize, int ksize)
    {

        cornerEigenValsAndVecs_1(src.nativeObj, dst.nativeObj, blockSize, ksize);

        return;
    }

    //
    // C++:  void cornerHarris(Mat src, Mat& dst, int blockSize, int ksize, double k, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Harris edge detector.</p>
 *
 * <p>The function runs the Harris edge detector on the image. Similarly to
 * "cornerMinEigenVal" and "cornerEigenValsAndVecs", for each pixel <em>(x,
 * y)</em> it calculates a <em>2x2</em> gradient covariance matrix
 * <em>M^((x,y))</em> over a <em>blockSize x blockSize</em> neighborhood. Then,
 * it computes the following characteristic:</p>
 *
 * <p><em>dst(x,y) = det M^((x,y)) - k * (tr M^((x,y)))^2</em></p>
 *
 * <p>Corners in the image can be found as the local maxima of this response map.</p>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the Harris detector responses. It has the type
 * <code>CV_32FC1</code> and the same size as <code>src</code>.
 * @param blockSize Neighborhood size (see the details on "cornerEigenValsAndVecs").
 * @param ksize Aperture parameter for the "Sobel" operator.
 * @param k Harris detector free parameter. See the formula below.
 * @param borderType Pixel extrapolation method. See "borderInterpolate".
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornerharris">org.opencv.imgproc.Imgproc.cornerHarris</a>
 */
    public static void cornerHarris(Mat src, Mat dst, int blockSize, int ksize, double k, int borderType)
    {

        cornerHarris_0(src.nativeObj, dst.nativeObj, blockSize, ksize, k, borderType);

        return;
    }

/**
 * <p>Harris edge detector.</p>
 *
 * <p>The function runs the Harris edge detector on the image. Similarly to
 * "cornerMinEigenVal" and "cornerEigenValsAndVecs", for each pixel <em>(x,
 * y)</em> it calculates a <em>2x2</em> gradient covariance matrix
 * <em>M^((x,y))</em> over a <em>blockSize x blockSize</em> neighborhood. Then,
 * it computes the following characteristic:</p>
 *
 * <p><em>dst(x,y) = det M^((x,y)) - k * (tr M^((x,y)))^2</em></p>
 *
 * <p>Corners in the image can be found as the local maxima of this response map.</p>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the Harris detector responses. It has the type
 * <code>CV_32FC1</code> and the same size as <code>src</code>.
 * @param blockSize Neighborhood size (see the details on "cornerEigenValsAndVecs").
 * @param ksize Aperture parameter for the "Sobel" operator.
 * @param k Harris detector free parameter. See the formula below.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornerharris">org.opencv.imgproc.Imgproc.cornerHarris</a>
 */
    public static void cornerHarris(Mat src, Mat dst, int blockSize, int ksize, double k)
    {

        cornerHarris_1(src.nativeObj, dst.nativeObj, blockSize, ksize, k);

        return;
    }

    //
    // C++:  void cornerMinEigenVal(Mat src, Mat& dst, int blockSize, int ksize = 3, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Calculates the minimal eigenvalue of gradient matrices for corner detection.</p>
 *
 * <p>The function is similar to "cornerEigenValsAndVecs" but it calculates and
 * stores only the minimal eigenvalue of the covariance matrix of derivatives,
 * that is, <em>min(lambda_1, lambda_2)</em> in terms of the formulae in the
 * "cornerEigenValsAndVecs" description.</p>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the minimal eigenvalues. It has the type
 * <code>CV_32FC1</code> and the same size as <code>src</code>.
 * @param blockSize Neighborhood size (see the details on "cornerEigenValsAndVecs").
 * @param ksize Aperture parameter for the "Sobel" operator.
 * @param borderType Pixel extrapolation method. See "borderInterpolate".
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornermineigenval">org.opencv.imgproc.Imgproc.cornerMinEigenVal</a>
 */
    public static void cornerMinEigenVal(Mat src, Mat dst, int blockSize, int ksize, int borderType)
    {

        cornerMinEigenVal_0(src.nativeObj, dst.nativeObj, blockSize, ksize, borderType);

        return;
    }

/**
 * <p>Calculates the minimal eigenvalue of gradient matrices for corner detection.</p>
 *
 * <p>The function is similar to "cornerEigenValsAndVecs" but it calculates and
 * stores only the minimal eigenvalue of the covariance matrix of derivatives,
 * that is, <em>min(lambda_1, lambda_2)</em> in terms of the formulae in the
 * "cornerEigenValsAndVecs" description.</p>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the minimal eigenvalues. It has the type
 * <code>CV_32FC1</code> and the same size as <code>src</code>.
 * @param blockSize Neighborhood size (see the details on "cornerEigenValsAndVecs").
 * @param ksize Aperture parameter for the "Sobel" operator.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornermineigenval">org.opencv.imgproc.Imgproc.cornerMinEigenVal</a>
 */
    public static void cornerMinEigenVal(Mat src, Mat dst, int blockSize, int ksize)
    {

        cornerMinEigenVal_1(src.nativeObj, dst.nativeObj, blockSize, ksize);

        return;
    }

/**
 * <p>Calculates the minimal eigenvalue of gradient matrices for corner detection.</p>
 *
 * <p>The function is similar to "cornerEigenValsAndVecs" but it calculates and
 * stores only the minimal eigenvalue of the covariance matrix of derivatives,
 * that is, <em>min(lambda_1, lambda_2)</em> in terms of the formulae in the
 * "cornerEigenValsAndVecs" description.</p>
 *
 * @param src Input single-channel 8-bit or floating-point image.
 * @param dst Image to store the minimal eigenvalues. It has the type
 * <code>CV_32FC1</code> and the same size as <code>src</code>.
 * @param blockSize Neighborhood size (see the details on "cornerEigenValsAndVecs").
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornermineigenval">org.opencv.imgproc.Imgproc.cornerMinEigenVal</a>
 */
    public static void cornerMinEigenVal(Mat src, Mat dst, int blockSize)
    {

        cornerMinEigenVal_2(src.nativeObj, dst.nativeObj, blockSize);

        return;
    }

    //
    // C++:  void cornerSubPix(Mat image, vector_Point2f& corners, Size winSize, Size zeroZone, TermCriteria criteria)
    //

/**
 * <p>Refines the corner locations.</p>
 *
 * <p>The function iterates to find the sub-pixel accurate location of corners or
 * radial saddle points, as shown on the figure below.</p>
 *
 * <p>Sub-pixel accurate corner locator is based on the observation that every
 * vector from the center <em>q</em> to a point <em>p</em> located within a
 * neighborhood of <em>q</em> is orthogonal to the image gradient at <em>p</em>
 * subject to image and measurement noise. Consider the expression:</p>
 *
 * <p><em>epsilon _i = (DI_(p_i))^T * (q - p_i)</em></p>
 *
 * <p>where <em>(DI_(p_i))</em> is an image gradient at one of the points
 * <em>p_i</em> in a neighborhood of <em>q</em>. The value of <em>q</em> is to
 * be found so that <em>epsilon_i</em> is minimized. A system of equations may
 * be set up with <em>epsilon_i</em> set to zero:</p>
 *
 * <p><em>sum _i(DI_(p_i) * (DI_(p_i))^T) - sum _i(DI_(p_i) * (DI_(p_i))^T *
 * p_i)</em></p>
 *
 * <p>where the gradients are summed within a neighborhood ("search window") of
 * <em>q</em>. Calling the first gradient term <em>G</em> and the second
 * gradient term <em>b</em> gives:</p>
 *
 * <p><em>q = G^(-1) * b</em></p>
 *
 * <p>The algorithm sets the center of the neighborhood window at this new center
 * <em>q</em> and then iterates until the center stays within a set threshold.</p>
 *
 * @param image Input image.
 * @param corners Initial coordinates of the input corners and refined
 * coordinates provided for output.
 * @param winSize Half of the side length of the search window. For example, if
 * <code>winSize=Size(5,5)</code>, then a <em>5*2+1 x 5*2+1 = 11 x 11</em>
 * search window is used.
 * @param zeroZone Half of the size of the dead region in the middle of the
 * search zone over which the summation in the formula below is not done. It is
 * used sometimes to avoid possible singularities of the autocorrelation matrix.
 * The value of (-1,-1) indicates that there is no such a size.
 * @param criteria Criteria for termination of the iterative process of corner
 * refinement. That is, the process of corner position refinement stops either
 * after <code>criteria.maxCount</code> iterations or when the corner position
 * moves by less than <code>criteria.epsilon</code> on some iteration.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#cornersubpix">org.opencv.imgproc.Imgproc.cornerSubPix</a>
 */
    public static void cornerSubPix(Mat image, MatOfPoint2f corners, Size winSize, Size zeroZone, TermCriteria criteria)
    {
        Mat corners_mat = corners;
        cornerSubPix_0(image.nativeObj, corners_mat.nativeObj, winSize.width, winSize.height, zeroZone.width, zeroZone.height, criteria.type, criteria.maxCount, criteria.epsilon);

        return;
    }

    //
    // C++:  Ptr_CLAHE createCLAHE(double clipLimit = 40.0, Size tileGridSize = Size(8, 8))
    //

    // Return type 'Ptr_CLAHE' is not supported, skipping the function

    //
    // C++:  void createHanningWindow(Mat& dst, Size winSize, int type)
    //

/**
 * <p>This function computes a Hanning window coefficients in two dimensions. See
 * http://en.wikipedia.org/wiki/Hann_function and http://en.wikipedia.org/wiki/Window_function
 * for more information.</p>
 *
 * <p>An example is shown below: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// create hanning window of size 100x100 and type CV_32F</p>
 *
 * <p>Mat hann;</p>
 *
 * <p>createHanningWindow(hann, Size(100, 100), CV_32F);</p>
 *
 * @param dst Destination array to place Hann coefficients in
 * @param winSize The window size specifications
 * @param type Created array type
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#createhanningwindow">org.opencv.imgproc.Imgproc.createHanningWindow</a>
 * @see org.opencv.imgproc.Imgproc#phaseCorrelate
 */
    public static void createHanningWindow(Mat dst, Size winSize, int type)
    {

        createHanningWindow_0(dst.nativeObj, winSize.width, winSize.height, type);

        return;
    }

    //
    // C++:  void cvtColor(Mat src, Mat& dst, int code, int dstCn = 0)
    //

/**
 * <p>Converts an image from one color space to another.</p>
 *
 * <p>The function converts an input image from one color space to another. In case
 * of a transformation to-from RGB color space, the order of the channels should
 * be specified explicitly (RGB or BGR).
 * Note that the default color format in OpenCV is often referred to as RGB but
 * it is actually BGR (the bytes are reversed). So the first byte in a standard
 * (24-bit) color image will be an 8-bit Blue component, the second byte will be
 * Green, and the third byte will be Red. The fourth, fifth, and sixth bytes
 * would then be the second pixel (Blue, then Green, then Red), and so on.</p>
 *
 * <p>The conventional ranges for R, G, and B channel values are:</p>
 * <ul>
 *   <li> 0 to 255 for <code>CV_8U</code> images
 *   <li> 0 to 65535 for <code>CV_16U</code> images
 *   <li> 0 to 1 for <code>CV_32F</code> images
 * </ul>
 *
 * <p>In case of linear transformations, the range does not matter.
 * But in case of a non-linear transformation, an input RGB image should be
 * normalized to the proper value range to get the correct results, for example,
 * for RGB<em>-></em> L*u*v* transformation. For example, if you have a 32-bit
 * floating-point image directly converted from an 8-bit image without any
 * scaling, then it will have the 0..255 value range instead of 0..1 assumed by
 * the function. So, before calling <code>cvtColor</code>, you need first to
 * scale the image down: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>img *= 1./255;</p>
 *
 * <p>cvtColor(img, img, CV_BGR2Luv);</p>
 *
 * <p>If you use <code>cvtColor</code> with 8-bit images, the conversion will have
 * some information lost. For many applications, this will not be noticeable but
 * it is recommended to use 32-bit images in applications that need the full
 * range of colors or that convert an image before an operation and then convert
 * back.
 * </code></p>
 *
 * <p>The function can do the following transformations:</p>
 * <ul>
 *   <li> RGB <em><-></em> GRAY (<code>CV_BGR2GRAY, CV_RGB2GRAY, CV_GRAY2BGR,
 * CV_GRAY2RGB</code>) Transformations within RGB space like adding/removing the
 * alpha channel, reversing the channel order, conversion to/from 16-bit RGB
 * color (R5:G6:B5 or R5:G5:B5), as well as conversion to/from grayscale using:
 * </ul>
 *
 * <p><em>RGB[A] to Gray: Y <- 0.299 * R + 0.587 * G + 0.114 * B</em></p>
 *
 * <p>and</p>
 *
 * <p><em>Gray to RGB[A]: R <- Y, G <- Y, B <- Y, A <- 0</em></p>
 *
 * <p>The conversion from a RGB image to gray is done with:</p>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>cvtColor(src, bwsrc, CV_RGB2GRAY);</p>
 *
 * <p></code></p>
 *
 * <p>More advanced channel reordering can also be done with "mixChannels".</p>
 * <ul>
 *   <li> RGB <em><-></em> CIE XYZ.Rec 709 with D65 white point
 * (<code>CV_BGR2XYZ, CV_RGB2XYZ, CV_XYZ2BGR, CV_XYZ2RGB</code>):
 * </ul>
 *
 * <p><em>X
 * Z ltBR gt <- 0.412453 0.357580 0.180423
 * 0.212671 0.715160 0.072169
 * 0.019334 0.119193 0.950227 ltBR gt * R
 * B ltBR gt</em></p>
 *
 *
 *
 * <p><em>R
 * B ltBR gt <- 3.240479 -1.53715 -0.498535
 * -0.969256 1.875991 0.041556
 * 0.055648 -0.204043 1.057311 ltBR gt * X
 * Z ltBR gt</em></p>
 *
 * <p><em>X</em>, <em>Y</em> and <em>Z</em> cover the whole value range (in case of
 * floating-point images, <em>Z</em> may exceed 1).</p>
 * <ul>
 *   <li> RGB <em><-></em> YCrCb JPEG (or YCC) (<code>CV_BGR2YCrCb,
 * CV_RGB2YCrCb, CV_YCrCb2BGR, CV_YCrCb2RGB</code>)
 * </ul>
 *
 * <p><em>Y <- 0.299 * R + 0.587 * G + 0.114 * B</em></p>
 *
 *
 *
 * <p><em>Cr <- (R-Y) * 0.713 + delta</em></p>
 *
 *
 *
 * <p><em>Cb <- (B-Y) * 0.564 + delta</em></p>
 *
 *
 *
 * <p><em>R <- Y + 1.403 * (Cr - delta)</em></p>
 *
 *
 *
 * <p><em>G <- Y - 0.714 * (Cr - delta) - 0.344 * (Cb - delta)</em></p>
 *
 *
 *
 * <p><em>B <- Y + 1.773 * (Cb - delta)</em></p>
 *
 * <p>where</p>
 *
 * <p><em>delta = <= ft (128 for 8-bit images
 * 32768 for 16-bit images
 * 0.5 for floating-point images right.</em></p>
 *
 * <p>Y, Cr, and Cb cover the whole value range.</p>
 * <ul>
 *   <li> RGB <em><-></em> HSV (<code>CV_BGR2HSV, CV_RGB2HSV, CV_HSV2BGR,
 * CV_HSV2RGB</code>) In case of 8-bit and 16-bit images, R, G, and B are
 * converted to the floating-point format and scaled to fit the 0 to 1 range.
 * </ul>
 *
 * <p><em>V <- max(R,G,B)</em></p>
 *
 *
 *
 * <p><em>S <- (V-min(R,G,B))/(V) if V != 0; 0 otherwise</em></p>
 *
 *
 *
 * <p><em>H <- (60(G - B))/((V-min(R,G,B))) if V=R; (120+60(B - R))/((V-min(R,G,B)))
 * if V=G; (240+60(R - G))/((V-min(R,G,B))) if V=B</em></p>
 *
 * <p>If <em>H&lt0</em> then <em>H <- H+360</em>. On output <em>0 <= V <= 1</em>,
 * <em>0 <= S <= 1</em>, <em>0 <= H <= 360</em>.</p>
 *
 * <p>The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>V <- 255 V, S <- 255 S, H <- H/2(to fit to 0 to 255)</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 * </ul>
 *
 * <p><em>V &lt- 65535 V, S &lt- 65535 S, H &lt- H</em></p>
 *
 * <ul>
 *   <li> 32-bit images H, S, and V are left as is
 *   <li> RGB <em><-></em> HLS (<code>CV_BGR2HLS, CV_RGB2HLS, CV_HLS2BGR,
 * CV_HLS2RGB</code>).
 * </ul>
 * <p>In case of 8-bit and 16-bit images, R, G, and B are converted to the
 * floating-point format and scaled to fit the 0 to 1 range.</p>
 *
 * <p><em>V_(max) <- (max)(R,G,B)</em></p>
 *
 *
 *
 * <p><em>V_(min) <- (min)(R,G,B)</em></p>
 *
 *
 *
 * <p><em>L <- (V_(max) + V_(min))/2</em></p>
 *
 *
 *
 * <p><em>S <- fork ((V_(max) - V_(min))/(V_(max) + V_(min)))(if L &lt
 * 0.5)&ltBR&gt((V_(max) - V_(min))/(2 - (V_(max) + V_(min))))(if L >= 0.5)</em></p>
 *
 *
 *
 * <p><em>H <- forkthree ((60(G - B))/(S))(if V_(max)=R)&ltBR&gt((120+60(B -
 * R))/(S))(if V_(max)=G)&ltBR&gt((240+60(R - G))/(S))(if V_(max)=B)</em></p>
 *
 * <p>If <em>H&lt0</em> then <em>H <- H+360</em>. On output <em>0 <= L <= 1</em>,
 * <em>0 <= S <= 1</em>, <em>0 <= H <= 360</em>.</p>
 *
 * <p>The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>V <- 255 * V, S <- 255 * S, H <- H/2(to fit to 0 to 255)</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 * </ul>
 *
 * <p><em>V &lt- 65535 * V, S &lt- 65535 * S, H &lt- H</em></p>
 *
 * <ul>
 *   <li> 32-bit images H, S, V are left as is
 *   <li> RGB <em><-></em> CIE L*a*b* (<code>CV_BGR2Lab, CV_RGB2Lab, CV_Lab2BGR,
 * CV_Lab2RGB</code>).
 * </ul>
 * <p>In case of 8-bit and 16-bit images, R, G, and B are converted to the
 * floating-point format and scaled to fit the 0 to 1 range.</p>
 *
 * <p><em>[X Y Z] <-
 * |0.412453 0.357580 0.180423|
 * |0.212671 0.715160 0.072169|
 * |0.019334 0.119193 0.950227|</p>
 * <ul>
 *   <li> [R G B]</em>
 *
 *
 * </ul>
 *
 * <p><em>X <- X/X_n, where X_n = 0.950456</em></p>
 *
 *
 *
 * <p><em>Z <- Z/Z_n, where Z_n = 1.088754</em></p>
 *
 *
 *
 * <p><em>L <- 116*Y^(1/3)-16 for Y&gt0.008856; 903.3*Y for Y <= 0.008856</em></p>
 *
 *
 *
 * <p><em>a <- 500(f(X)-f(Y)) + delta</em></p>
 *
 *
 *
 * <p><em>b <- 200(f(Y)-f(Z)) + delta</em></p>
 *
 * <p>where</p>
 *
 * <p><em>f(t)= t^(1/3) for t&gt0.008856; 7.787 t+16/116 for t <= 0.008856</em></p>
 *
 * <p>and</p>
 *
 * <p><em>delta = 128 for 8-bit images; 0 for floating-point images</em></p>
 *
 * <p>This outputs <em>0 <= L <= 100</em>, <em>-127 <= a <= 127</em>, <em>-127 <= b
 * <= 127</em>. The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>L <- L*255/100, a <- a + 128, b <- b + 128</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 *   <li> 32-bit images L, a, and b are left as is
 *   <li> RGB <em><-></em> CIE L*u*v* (<code>CV_BGR2Luv, CV_RGB2Luv, CV_Luv2BGR,
 * CV_Luv2RGB</code>).
 * </ul>
 * <p>In case of 8-bit and 16-bit images, R, G, and B are converted to the
 * floating-point format and scaled to fit 0 to 1 range.</p>
 *
 * <p><em>[X Y Z] <-
 * |0.412453 0.357580 0.180423|
 * |0.212671 0.715160 0.072169|
 * |0.019334 0.119193 0.950227|</p>
 * <ul>
 *   <li> [R G B]</em>
 *
 *
 * </ul>
 *
 * <p><em>L <- 116 Y^(1/3) for Y&gt0.008856; 903.3 Y for Y <= 0.008856</em></p>
 *
 *
 *
 * <p><em>u' <- 4*X/(X + 15*Y + 3 Z)</em></p>
 *
 *
 *
 * <p><em>v' <- 9*Y/(X + 15*Y + 3 Z)</em></p>
 *
 *
 *
 * <p><em>u <- 13*L*(u' - u_n) where u_n=0.19793943</em></p>
 *
 *
 *
 * <p><em>v <- 13*L*(v' - v_n) where v_n=0.46831096</em></p>
 *
 * <p>This outputs <em>0 <= L <= 100</em>, <em>-134 <= u <= 220</em>, <em>-140 <= v
 * <= 122</em>.</p>
 *
 * <p>The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>L <- 255/100 L, u <- 255/354(u + 134), v <- 255/256(v + 140)</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 *   <li> 32-bit images L, u, and v are left as is
 * </ul>
 *
 * <p>The above formulae for converting RGB to/from various color spaces have been
 * taken from multiple sources on the web, primarily from the Charles Poynton
 * site http://www.poynton.com/ColorFAQ.html</p>
 * <ul>
 *   <li> Bayer <em>-></em> RGB (<code>CV_BayerBG2BGR, CV_BayerGB2BGR,
 * CV_BayerRG2BGR, CV_BayerGR2BGR, CV_BayerBG2RGB, CV_BayerGB2RGB,
 * CV_BayerRG2RGB, CV_BayerGR2RGB</code>). The Bayer pattern is widely used in
 * CCD and CMOS cameras. It enables you to get color pictures from a single
 * plane where R,G, and B pixels (sensors of a particular component) are
 * interleaved as follows: The output RGB components of a pixel are interpolated
 * from 1, 2, or <code>
 * </ul>
 *
 * <p>// C++ code:</p>
 *
 * <p>4 neighbors of the pixel having the same color. There are several</p>
 *
 * <p>modifications of the above pattern that can be achieved by shifting</p>
 *
 * <p>the pattern one pixel left and/or one pixel up. The two letters</p>
 *
 * <p><em>C_1</em> and</p>
 *
 * <p><em>C_2</em> in the conversion constants <code>CV_Bayer</code> <em>C_1
 * C_2</em> <code>2BGR</code> and <code>CV_Bayer</code> <em>C_1 C_2</em>
 * <code>2RGB</code> indicate the particular pattern</p>
 *
 * <p>type. These are components from the second row, second and third</p>
 *
 * <p>columns, respectively. For example, the above pattern has a very</p>
 *
 * <p>popular "BG" type.</p>
 *
 * @param src input image: 8-bit unsigned, 16-bit unsigned (<code>CV_16UC...</code>),
 * or single-precision floating-point.
 * @param dst output image of the same size and depth as <code>src</code>.
 * @param code color space conversion code (see the description below).
 * @param dstCn number of channels in the destination image; if the parameter is
 * 0, the number of the channels is derived automatically from <code>src</code>
 * and <code>code</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#cvtcolor">org.opencv.imgproc.Imgproc.cvtColor</a>
 */
    public static void cvtColor(Mat src, Mat dst, int code, int dstCn)
    {

        cvtColor_0(src.nativeObj, dst.nativeObj, code, dstCn);

        return;
    }

/**
 * <p>Converts an image from one color space to another.</p>
 *
 * <p>The function converts an input image from one color space to another. In case
 * of a transformation to-from RGB color space, the order of the channels should
 * be specified explicitly (RGB or BGR).
 * Note that the default color format in OpenCV is often referred to as RGB but
 * it is actually BGR (the bytes are reversed). So the first byte in a standard
 * (24-bit) color image will be an 8-bit Blue component, the second byte will be
 * Green, and the third byte will be Red. The fourth, fifth, and sixth bytes
 * would then be the second pixel (Blue, then Green, then Red), and so on.</p>
 *
 * <p>The conventional ranges for R, G, and B channel values are:</p>
 * <ul>
 *   <li> 0 to 255 for <code>CV_8U</code> images
 *   <li> 0 to 65535 for <code>CV_16U</code> images
 *   <li> 0 to 1 for <code>CV_32F</code> images
 * </ul>
 *
 * <p>In case of linear transformations, the range does not matter.
 * But in case of a non-linear transformation, an input RGB image should be
 * normalized to the proper value range to get the correct results, for example,
 * for RGB<em>-></em> L*u*v* transformation. For example, if you have a 32-bit
 * floating-point image directly converted from an 8-bit image without any
 * scaling, then it will have the 0..255 value range instead of 0..1 assumed by
 * the function. So, before calling <code>cvtColor</code>, you need first to
 * scale the image down: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>img *= 1./255;</p>
 *
 * <p>cvtColor(img, img, CV_BGR2Luv);</p>
 *
 * <p>If you use <code>cvtColor</code> with 8-bit images, the conversion will have
 * some information lost. For many applications, this will not be noticeable but
 * it is recommended to use 32-bit images in applications that need the full
 * range of colors or that convert an image before an operation and then convert
 * back.
 * </code></p>
 *
 * <p>The function can do the following transformations:</p>
 * <ul>
 *   <li> RGB <em><-></em> GRAY (<code>CV_BGR2GRAY, CV_RGB2GRAY, CV_GRAY2BGR,
 * CV_GRAY2RGB</code>) Transformations within RGB space like adding/removing the
 * alpha channel, reversing the channel order, conversion to/from 16-bit RGB
 * color (R5:G6:B5 or R5:G5:B5), as well as conversion to/from grayscale using:
 * </ul>
 *
 * <p><em>RGB[A] to Gray: Y <- 0.299 * R + 0.587 * G + 0.114 * B</em></p>
 *
 * <p>and</p>
 *
 * <p><em>Gray to RGB[A]: R <- Y, G <- Y, B <- Y, A <- 0</em></p>
 *
 * <p>The conversion from a RGB image to gray is done with:</p>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>cvtColor(src, bwsrc, CV_RGB2GRAY);</p>
 *
 * <p></code></p>
 *
 * <p>More advanced channel reordering can also be done with "mixChannels".</p>
 * <ul>
 *   <li> RGB <em><-></em> CIE XYZ.Rec 709 with D65 white point
 * (<code>CV_BGR2XYZ, CV_RGB2XYZ, CV_XYZ2BGR, CV_XYZ2RGB</code>):
 * </ul>
 *
 * <p><em>X
 * Z ltBR gt <- 0.412453 0.357580 0.180423
 * 0.212671 0.715160 0.072169
 * 0.019334 0.119193 0.950227 ltBR gt * R
 * B ltBR gt</em></p>
 *
 *
 *
 * <p><em>R
 * B ltBR gt <- 3.240479 -1.53715 -0.498535
 * -0.969256 1.875991 0.041556
 * 0.055648 -0.204043 1.057311 ltBR gt * X
 * Z ltBR gt</em></p>
 *
 * <p><em>X</em>, <em>Y</em> and <em>Z</em> cover the whole value range (in case of
 * floating-point images, <em>Z</em> may exceed 1).</p>
 * <ul>
 *   <li> RGB <em><-></em> YCrCb JPEG (or YCC) (<code>CV_BGR2YCrCb,
 * CV_RGB2YCrCb, CV_YCrCb2BGR, CV_YCrCb2RGB</code>)
 * </ul>
 *
 * <p><em>Y <- 0.299 * R + 0.587 * G + 0.114 * B</em></p>
 *
 *
 *
 * <p><em>Cr <- (R-Y) * 0.713 + delta</em></p>
 *
 *
 *
 * <p><em>Cb <- (B-Y) * 0.564 + delta</em></p>
 *
 *
 *
 * <p><em>R <- Y + 1.403 * (Cr - delta)</em></p>
 *
 *
 *
 * <p><em>G <- Y - 0.714 * (Cr - delta) - 0.344 * (Cb - delta)</em></p>
 *
 *
 *
 * <p><em>B <- Y + 1.773 * (Cb - delta)</em></p>
 *
 * <p>where</p>
 *
 * <p><em>delta = <= ft (128 for 8-bit images
 * 32768 for 16-bit images
 * 0.5 for floating-point images right.</em></p>
 *
 * <p>Y, Cr, and Cb cover the whole value range.</p>
 * <ul>
 *   <li> RGB <em><-></em> HSV (<code>CV_BGR2HSV, CV_RGB2HSV, CV_HSV2BGR,
 * CV_HSV2RGB</code>) In case of 8-bit and 16-bit images, R, G, and B are
 * converted to the floating-point format and scaled to fit the 0 to 1 range.
 * </ul>
 *
 * <p><em>V <- max(R,G,B)</em></p>
 *
 *
 *
 * <p><em>S <- (V-min(R,G,B))/(V) if V != 0; 0 otherwise</em></p>
 *
 *
 *
 * <p><em>H <- (60(G - B))/((V-min(R,G,B))) if V=R; (120+60(B - R))/((V-min(R,G,B)))
 * if V=G; (240+60(R - G))/((V-min(R,G,B))) if V=B</em></p>
 *
 * <p>If <em>H&lt0</em> then <em>H <- H+360</em>. On output <em>0 <= V <= 1</em>,
 * <em>0 <= S <= 1</em>, <em>0 <= H <= 360</em>.</p>
 *
 * <p>The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>V <- 255 V, S <- 255 S, H <- H/2(to fit to 0 to 255)</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 * </ul>
 *
 * <p><em>V &lt- 65535 V, S &lt- 65535 S, H &lt- H</em></p>
 *
 * <ul>
 *   <li> 32-bit images H, S, and V are left as is
 *   <li> RGB <em><-></em> HLS (<code>CV_BGR2HLS, CV_RGB2HLS, CV_HLS2BGR,
 * CV_HLS2RGB</code>).
 * </ul>
 * <p>In case of 8-bit and 16-bit images, R, G, and B are converted to the
 * floating-point format and scaled to fit the 0 to 1 range.</p>
 *
 * <p><em>V_(max) <- (max)(R,G,B)</em></p>
 *
 *
 *
 * <p><em>V_(min) <- (min)(R,G,B)</em></p>
 *
 *
 *
 * <p><em>L <- (V_(max) + V_(min))/2</em></p>
 *
 *
 *
 * <p><em>S <- fork ((V_(max) - V_(min))/(V_(max) + V_(min)))(if L &lt
 * 0.5)&ltBR&gt((V_(max) - V_(min))/(2 - (V_(max) + V_(min))))(if L >= 0.5)</em></p>
 *
 *
 *
 * <p><em>H <- forkthree ((60(G - B))/(S))(if V_(max)=R)&ltBR&gt((120+60(B -
 * R))/(S))(if V_(max)=G)&ltBR&gt((240+60(R - G))/(S))(if V_(max)=B)</em></p>
 *
 * <p>If <em>H&lt0</em> then <em>H <- H+360</em>. On output <em>0 <= L <= 1</em>,
 * <em>0 <= S <= 1</em>, <em>0 <= H <= 360</em>.</p>
 *
 * <p>The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>V <- 255 * V, S <- 255 * S, H <- H/2(to fit to 0 to 255)</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 * </ul>
 *
 * <p><em>V &lt- 65535 * V, S &lt- 65535 * S, H &lt- H</em></p>
 *
 * <ul>
 *   <li> 32-bit images H, S, V are left as is
 *   <li> RGB <em><-></em> CIE L*a*b* (<code>CV_BGR2Lab, CV_RGB2Lab, CV_Lab2BGR,
 * CV_Lab2RGB</code>).
 * </ul>
 * <p>In case of 8-bit and 16-bit images, R, G, and B are converted to the
 * floating-point format and scaled to fit the 0 to 1 range.</p>
 *
 * <p><em>[X Y Z] <-
 * |0.412453 0.357580 0.180423|
 * |0.212671 0.715160 0.072169|
 * |0.019334 0.119193 0.950227|</p>
 * <ul>
 *   <li> [R G B]</em>
 *
 *
 * </ul>
 *
 * <p><em>X <- X/X_n, where X_n = 0.950456</em></p>
 *
 *
 *
 * <p><em>Z <- Z/Z_n, where Z_n = 1.088754</em></p>
 *
 *
 *
 * <p><em>L <- 116*Y^(1/3)-16 for Y&gt0.008856; 903.3*Y for Y <= 0.008856</em></p>
 *
 *
 *
 * <p><em>a <- 500(f(X)-f(Y)) + delta</em></p>
 *
 *
 *
 * <p><em>b <- 200(f(Y)-f(Z)) + delta</em></p>
 *
 * <p>where</p>
 *
 * <p><em>f(t)= t^(1/3) for t&gt0.008856; 7.787 t+16/116 for t <= 0.008856</em></p>
 *
 * <p>and</p>
 *
 * <p><em>delta = 128 for 8-bit images; 0 for floating-point images</em></p>
 *
 * <p>This outputs <em>0 <= L <= 100</em>, <em>-127 <= a <= 127</em>, <em>-127 <= b
 * <= 127</em>. The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>L <- L*255/100, a <- a + 128, b <- b + 128</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 *   <li> 32-bit images L, a, and b are left as is
 *   <li> RGB <em><-></em> CIE L*u*v* (<code>CV_BGR2Luv, CV_RGB2Luv, CV_Luv2BGR,
 * CV_Luv2RGB</code>).
 * </ul>
 * <p>In case of 8-bit and 16-bit images, R, G, and B are converted to the
 * floating-point format and scaled to fit 0 to 1 range.</p>
 *
 * <p><em>[X Y Z] <-
 * |0.412453 0.357580 0.180423|
 * |0.212671 0.715160 0.072169|
 * |0.019334 0.119193 0.950227|</p>
 * <ul>
 *   <li> [R G B]</em>
 *
 *
 * </ul>
 *
 * <p><em>L <- 116 Y^(1/3) for Y&gt0.008856; 903.3 Y for Y <= 0.008856</em></p>
 *
 *
 *
 * <p><em>u' <- 4*X/(X + 15*Y + 3 Z)</em></p>
 *
 *
 *
 * <p><em>v' <- 9*Y/(X + 15*Y + 3 Z)</em></p>
 *
 *
 *
 * <p><em>u <- 13*L*(u' - u_n) where u_n=0.19793943</em></p>
 *
 *
 *
 * <p><em>v <- 13*L*(v' - v_n) where v_n=0.46831096</em></p>
 *
 * <p>This outputs <em>0 <= L <= 100</em>, <em>-134 <= u <= 220</em>, <em>-140 <= v
 * <= 122</em>.</p>
 *
 * <p>The values are then converted to the destination data type:</p>
 * <ul>
 *   <li> 8-bit images
 * </ul>
 *
 * <p><em>L <- 255/100 L, u <- 255/354(u + 134), v <- 255/256(v + 140)</em></p>
 *
 * <ul>
 *   <li> 16-bit images (currently not supported)
 *   <li> 32-bit images L, u, and v are left as is
 * </ul>
 *
 * <p>The above formulae for converting RGB to/from various color spaces have been
 * taken from multiple sources on the web, primarily from the Charles Poynton
 * site http://www.poynton.com/ColorFAQ.html</p>
 * <ul>
 *   <li> Bayer <em>-></em> RGB (<code>CV_BayerBG2BGR, CV_BayerGB2BGR,
 * CV_BayerRG2BGR, CV_BayerGR2BGR, CV_BayerBG2RGB, CV_BayerGB2RGB,
 * CV_BayerRG2RGB, CV_BayerGR2RGB</code>). The Bayer pattern is widely used in
 * CCD and CMOS cameras. It enables you to get color pictures from a single
 * plane where R,G, and B pixels (sensors of a particular component) are
 * interleaved as follows: The output RGB components of a pixel are interpolated
 * from 1, 2, or <code>
 * </ul>
 *
 * <p>// C++ code:</p>
 *
 * <p>4 neighbors of the pixel having the same color. There are several</p>
 *
 * <p>modifications of the above pattern that can be achieved by shifting</p>
 *
 * <p>the pattern one pixel left and/or one pixel up. The two letters</p>
 *
 * <p><em>C_1</em> and</p>
 *
 * <p><em>C_2</em> in the conversion constants <code>CV_Bayer</code> <em>C_1
 * C_2</em> <code>2BGR</code> and <code>CV_Bayer</code> <em>C_1 C_2</em>
 * <code>2RGB</code> indicate the particular pattern</p>
 *
 * <p>type. These are components from the second row, second and third</p>
 *
 * <p>columns, respectively. For example, the above pattern has a very</p>
 *
 * <p>popular "BG" type.</p>
 *
 * @param src input image: 8-bit unsigned, 16-bit unsigned (<code>CV_16UC...</code>),
 * or single-precision floating-point.
 * @param dst output image of the same size and depth as <code>src</code>.
 * @param code color space conversion code (see the description below).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#cvtcolor">org.opencv.imgproc.Imgproc.cvtColor</a>
 */
    public static void cvtColor(Mat src, Mat dst, int code)
    {

        cvtColor_1(src.nativeObj, dst.nativeObj, code);

        return;
    }

    //
    // C++:  void dilate(Mat src, Mat& dst, Mat kernel, Point anchor = Point(-1,-1), int iterations = 1, int borderType = BORDER_CONSTANT, Scalar borderValue = morphologyDefaultBorderValue())
    //

/**
 * <p>Dilates an image by using a specific structuring element.</p>
 *
 * <p>The function dilates the source image using the specified structuring element
 * that determines the shape of a pixel neighborhood over which the maximum is
 * taken:</p>
 *
 * <p><em>dst(x,y) = max _((x',y'): element(x',y') != 0) src(x+x',y+y')</em></p>
 *
 * <p>The function supports the in-place mode. Dilation can be applied several
 * (<code>iterations</code>) times. In case of multi-channel images, each
 * channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphological dilate operation can be found at
 * opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src input image; the number of channels can be arbitrary, but the
 * depth should be one of <code>CV_8U</code>, <code>CV_16U</code>,
 * <code>CV_16S</code>, <code>CV_32F" or </code>CV_64F".
 * @param dst output image of the same size and type as <code>src</code>.
 * @param kernel a kernel
 * @param anchor position of the anchor within the element; default value
 * <code>(-1, -1)</code> means that the anchor is at the element center.
 * @param iterations number of times dilation is applied.
 * @param borderType pixel extrapolation method (see "borderInterpolate" for
 * details).
 * @param borderValue border value in case of a constant border (see
 * "createMorphologyFilter" for details).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#dilate">org.opencv.imgproc.Imgproc.dilate</a>
 * @see org.opencv.imgproc.Imgproc#erode
 * @see org.opencv.imgproc.Imgproc#morphologyEx
 */
    public static void dilate(Mat src, Mat dst, Mat kernel, Point anchor, int iterations, int borderType, Scalar borderValue)
    {

        dilate_0(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations, borderType, borderValue.val[0], borderValue.val[1], borderValue.val[2], borderValue.val[3]);

        return;
    }

/**
 * <p>Dilates an image by using a specific structuring element.</p>
 *
 * <p>The function dilates the source image using the specified structuring element
 * that determines the shape of a pixel neighborhood over which the maximum is
 * taken:</p>
 *
 * <p><em>dst(x,y) = max _((x',y'): element(x',y') != 0) src(x+x',y+y')</em></p>
 *
 * <p>The function supports the in-place mode. Dilation can be applied several
 * (<code>iterations</code>) times. In case of multi-channel images, each
 * channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphological dilate operation can be found at
 * opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src input image; the number of channels can be arbitrary, but the
 * depth should be one of <code>CV_8U</code>, <code>CV_16U</code>,
 * <code>CV_16S</code>, <code>CV_32F" or </code>CV_64F".
 * @param dst output image of the same size and type as <code>src</code>.
 * @param kernel a kernel
 * @param anchor position of the anchor within the element; default value
 * <code>(-1, -1)</code> means that the anchor is at the element center.
 * @param iterations number of times dilation is applied.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#dilate">org.opencv.imgproc.Imgproc.dilate</a>
 * @see org.opencv.imgproc.Imgproc#erode
 * @see org.opencv.imgproc.Imgproc#morphologyEx
 */
    public static void dilate(Mat src, Mat dst, Mat kernel, Point anchor, int iterations)
    {

        dilate_1(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations);

        return;
    }

/**
 * <p>Dilates an image by using a specific structuring element.</p>
 *
 * <p>The function dilates the source image using the specified structuring element
 * that determines the shape of a pixel neighborhood over which the maximum is
 * taken:</p>
 *
 * <p><em>dst(x,y) = max _((x',y'): element(x',y') != 0) src(x+x',y+y')</em></p>
 *
 * <p>The function supports the in-place mode. Dilation can be applied several
 * (<code>iterations</code>) times. In case of multi-channel images, each
 * channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphological dilate operation can be found at
 * opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src input image; the number of channels can be arbitrary, but the
 * depth should be one of <code>CV_8U</code>, <code>CV_16U</code>,
 * <code>CV_16S</code>, <code>CV_32F" or </code>CV_64F".
 * @param dst output image of the same size and type as <code>src</code>.
 * @param kernel a kernel
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#dilate">org.opencv.imgproc.Imgproc.dilate</a>
 * @see org.opencv.imgproc.Imgproc#erode
 * @see org.opencv.imgproc.Imgproc#morphologyEx
 */
    public static void dilate(Mat src, Mat dst, Mat kernel)
    {

        dilate_2(src.nativeObj, dst.nativeObj, kernel.nativeObj);

        return;
    }

    //
    // C++:  void distanceTransform(Mat src, Mat& dst, int distanceType, int maskSize)
    //

/**
 * <p>Calculates the distance to the closest zero pixel for each pixel of the
 * source image.</p>
 *
 * <p>The functions <code>distanceTransform</code> calculate the approximate or
 * precise distance from every binary image pixel to the nearest zero pixel.
 * For zero image pixels, the distance will obviously be zero.</p>
 *
 * <p>When <code>maskSize == CV_DIST_MASK_PRECISE</code> and <code>distanceType ==
 * CV_DIST_L2</code>, the function runs the algorithm described in
 * [Felzenszwalb04]. This algorithm is parallelized with the TBB library.</p>
 *
 * <p>In other cases, the algorithm [Borgefors86] is used. This means that for a
 * pixel the function finds the shortest path to the nearest zero pixel
 * consisting of basic shifts: horizontal, vertical, diagonal, or knight's move
 * (the latest is available for a <em>5x 5</em> mask). The overall distance is
 * calculated as a sum of these basic distances. Since the distance function
 * should be symmetric, all of the horizontal and vertical shifts must have the
 * same cost (denoted as <code>a</code>), all the diagonal shifts must have the
 * same cost (denoted as <code>b</code>), and all knight's moves must have the
 * same cost (denoted as <code>c</code>). For the <code>CV_DIST_C</code> and
 * <code>CV_DIST_L1</code> types, the distance is calculated precisely, whereas
 * for <code>CV_DIST_L2</code> (Euclidean distance) the distance can be
 * calculated only with a relative error (a <em>5x 5</em> mask gives more
 * accurate results). For <code>a</code>,<code>b</code>, and <code>c</code>,
 * OpenCV uses the values suggested in the original paper:</p>
 *
 * <p>============== =================== ======================
 * <code>CV_DIST_C</code> <em>(3x 3)</em> a = 1, b = 1 \
 * ============== =================== ======================
 * <code>CV_DIST_L1</code> <em>(3x 3)</em> a = 1, b = 2 \
 * <code>CV_DIST_L2</code> <em>(3x 3)</em> a=0.955, b=1.3693 \
 * <code>CV_DIST_L2</code> <em>(5x 5)</em> a=1, b=1.4, c=2.1969 \
 * ============== =================== ======================</p>
 *
 * <p>Typically, for a fast, coarse distance estimation <code>CV_DIST_L2</code>, a
 * <em>3x 3</em> mask is used. For a more accurate distance estimation
 * <code>CV_DIST_L2</code>, a <em>5x 5</em> mask or the precise algorithm is
 * used.
 * Note that both the precise and the approximate algorithms are linear on the
 * number of pixels.</p>
 *
 * <p>The second variant of the function does not only compute the minimum distance
 * for each pixel <em>(x, y)</em> but also identifies the nearest connected
 * component consisting of zero pixels (<code>labelType==DIST_LABEL_CCOMP</code>)
 * or the nearest zero pixel (<code>labelType==DIST_LABEL_PIXEL</code>). Index
 * of the component/pixel is stored in <em>labels(x, y)</em>.
 * When <code>labelType==DIST_LABEL_CCOMP</code>, the function automatically
 * finds connected components of zero pixels in the input image and marks them
 * with distinct labels. When <code>labelType==DIST_LABEL_CCOMP</code>, the
 * function scans through the input image and marks all the zero pixels with
 * distinct labels.</p>
 *
 * <p>In this mode, the complexity is still linear.
 * That is, the function provides a very fast way to compute the Voronoi diagram
 * for a binary image.
 * Currently, the second variant can use only the approximate distance transform
 * algorithm, i.e. <code>maskSize=CV_DIST_MASK_PRECISE</code> is not supported
 * yet.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example on using the distance transform can be found at
 * opencv_source_code/samples/cpp/distrans.cpp
 *   <li> (Python) An example on using the distance transform can be found at
 * opencv_source/samples/python2/distrans.py
 * </ul>
 *
 * @param src 8-bit, single-channel (binary) source image.
 * @param dst Output image with calculated distances. It is a 32-bit
 * floating-point, single-channel image of the same size as <code>src</code>.
 * @param distanceType Type of distance. It can be <code>CV_DIST_L1,
 * CV_DIST_L2</code>, or <code>CV_DIST_C</code>.
 * @param maskSize Size of the distance transform mask. It can be 3, 5, or
 * <code>CV_DIST_MASK_PRECISE</code> (the latter option is only supported by the
 * first function). In case of the <code>CV_DIST_L1</code> or <code>CV_DIST_C</code>
 * distance type, the parameter is forced to 3 because a <em>3x 3</em> mask
 * gives the same result as <em>5x 5</em> or any larger aperture.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#distancetransform">org.opencv.imgproc.Imgproc.distanceTransform</a>
 */
    public static void distanceTransform(Mat src, Mat dst, int distanceType, int maskSize)
    {

        distanceTransform_0(src.nativeObj, dst.nativeObj, distanceType, maskSize);

        return;
    }

    //
    // C++:  void distanceTransform(Mat src, Mat& dst, Mat& labels, int distanceType, int maskSize, int labelType = DIST_LABEL_CCOMP)
    //

/**
 * <p>Calculates the distance to the closest zero pixel for each pixel of the
 * source image.</p>
 *
 * <p>The functions <code>distanceTransform</code> calculate the approximate or
 * precise distance from every binary image pixel to the nearest zero pixel.
 * For zero image pixels, the distance will obviously be zero.</p>
 *
 * <p>When <code>maskSize == CV_DIST_MASK_PRECISE</code> and <code>distanceType ==
 * CV_DIST_L2</code>, the function runs the algorithm described in
 * [Felzenszwalb04]. This algorithm is parallelized with the TBB library.</p>
 *
 * <p>In other cases, the algorithm [Borgefors86] is used. This means that for a
 * pixel the function finds the shortest path to the nearest zero pixel
 * consisting of basic shifts: horizontal, vertical, diagonal, or knight's move
 * (the latest is available for a <em>5x 5</em> mask). The overall distance is
 * calculated as a sum of these basic distances. Since the distance function
 * should be symmetric, all of the horizontal and vertical shifts must have the
 * same cost (denoted as <code>a</code>), all the diagonal shifts must have the
 * same cost (denoted as <code>b</code>), and all knight's moves must have the
 * same cost (denoted as <code>c</code>). For the <code>CV_DIST_C</code> and
 * <code>CV_DIST_L1</code> types, the distance is calculated precisely, whereas
 * for <code>CV_DIST_L2</code> (Euclidean distance) the distance can be
 * calculated only with a relative error (a <em>5x 5</em> mask gives more
 * accurate results). For <code>a</code>,<code>b</code>, and <code>c</code>,
 * OpenCV uses the values suggested in the original paper:</p>
 *
 * <p>============== =================== ======================
 * <code>CV_DIST_C</code> <em>(3x 3)</em> a = 1, b = 1 \
 * ============== =================== ======================
 * <code>CV_DIST_L1</code> <em>(3x 3)</em> a = 1, b = 2 \
 * <code>CV_DIST_L2</code> <em>(3x 3)</em> a=0.955, b=1.3693 \
 * <code>CV_DIST_L2</code> <em>(5x 5)</em> a=1, b=1.4, c=2.1969 \
 * ============== =================== ======================</p>
 *
 * <p>Typically, for a fast, coarse distance estimation <code>CV_DIST_L2</code>, a
 * <em>3x 3</em> mask is used. For a more accurate distance estimation
 * <code>CV_DIST_L2</code>, a <em>5x 5</em> mask or the precise algorithm is
 * used.
 * Note that both the precise and the approximate algorithms are linear on the
 * number of pixels.</p>
 *
 * <p>The second variant of the function does not only compute the minimum distance
 * for each pixel <em>(x, y)</em> but also identifies the nearest connected
 * component consisting of zero pixels (<code>labelType==DIST_LABEL_CCOMP</code>)
 * or the nearest zero pixel (<code>labelType==DIST_LABEL_PIXEL</code>). Index
 * of the component/pixel is stored in <em>labels(x, y)</em>.
 * When <code>labelType==DIST_LABEL_CCOMP</code>, the function automatically
 * finds connected components of zero pixels in the input image and marks them
 * with distinct labels. When <code>labelType==DIST_LABEL_CCOMP</code>, the
 * function scans through the input image and marks all the zero pixels with
 * distinct labels.</p>
 *
 * <p>In this mode, the complexity is still linear.
 * That is, the function provides a very fast way to compute the Voronoi diagram
 * for a binary image.
 * Currently, the second variant can use only the approximate distance transform
 * algorithm, i.e. <code>maskSize=CV_DIST_MASK_PRECISE</code> is not supported
 * yet.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example on using the distance transform can be found at
 * opencv_source_code/samples/cpp/distrans.cpp
 *   <li> (Python) An example on using the distance transform can be found at
 * opencv_source/samples/python2/distrans.py
 * </ul>
 *
 * @param src 8-bit, single-channel (binary) source image.
 * @param dst Output image with calculated distances. It is a 32-bit
 * floating-point, single-channel image of the same size as <code>src</code>.
 * @param labels Optional output 2D array of labels (the discrete Voronoi
 * diagram). It has the type <code>CV_32SC1</code> and the same size as
 * <code>src</code>. See the details below.
 * @param distanceType Type of distance. It can be <code>CV_DIST_L1,
 * CV_DIST_L2</code>, or <code>CV_DIST_C</code>.
 * @param maskSize Size of the distance transform mask. It can be 3, 5, or
 * <code>CV_DIST_MASK_PRECISE</code> (the latter option is only supported by the
 * first function). In case of the <code>CV_DIST_L1</code> or <code>CV_DIST_C</code>
 * distance type, the parameter is forced to 3 because a <em>3x 3</em> mask
 * gives the same result as <em>5x 5</em> or any larger aperture.
 * @param labelType Type of the label array to build. If <code>labelType==DIST_LABEL_CCOMP</code>
 * then each connected component of zeros in <code>src</code> (as well as all
 * the non-zero pixels closest to the connected component) will be assigned the
 * same label. If <code>labelType==DIST_LABEL_PIXEL</code> then each zero pixel
 * (and all the non-zero pixels closest to it) gets its own label.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#distancetransform">org.opencv.imgproc.Imgproc.distanceTransform</a>
 */
    public static void distanceTransformWithLabels(Mat src, Mat dst, Mat labels, int distanceType, int maskSize, int labelType)
    {

        distanceTransformWithLabels_0(src.nativeObj, dst.nativeObj, labels.nativeObj, distanceType, maskSize, labelType);

        return;
    }

/**
 * <p>Calculates the distance to the closest zero pixel for each pixel of the
 * source image.</p>
 *
 * <p>The functions <code>distanceTransform</code> calculate the approximate or
 * precise distance from every binary image pixel to the nearest zero pixel.
 * For zero image pixels, the distance will obviously be zero.</p>
 *
 * <p>When <code>maskSize == CV_DIST_MASK_PRECISE</code> and <code>distanceType ==
 * CV_DIST_L2</code>, the function runs the algorithm described in
 * [Felzenszwalb04]. This algorithm is parallelized with the TBB library.</p>
 *
 * <p>In other cases, the algorithm [Borgefors86] is used. This means that for a
 * pixel the function finds the shortest path to the nearest zero pixel
 * consisting of basic shifts: horizontal, vertical, diagonal, or knight's move
 * (the latest is available for a <em>5x 5</em> mask). The overall distance is
 * calculated as a sum of these basic distances. Since the distance function
 * should be symmetric, all of the horizontal and vertical shifts must have the
 * same cost (denoted as <code>a</code>), all the diagonal shifts must have the
 * same cost (denoted as <code>b</code>), and all knight's moves must have the
 * same cost (denoted as <code>c</code>). For the <code>CV_DIST_C</code> and
 * <code>CV_DIST_L1</code> types, the distance is calculated precisely, whereas
 * for <code>CV_DIST_L2</code> (Euclidean distance) the distance can be
 * calculated only with a relative error (a <em>5x 5</em> mask gives more
 * accurate results). For <code>a</code>,<code>b</code>, and <code>c</code>,
 * OpenCV uses the values suggested in the original paper:</p>
 *
 * <p>============== =================== ======================
 * <code>CV_DIST_C</code> <em>(3x 3)</em> a = 1, b = 1 \
 * ============== =================== ======================
 * <code>CV_DIST_L1</code> <em>(3x 3)</em> a = 1, b = 2 \
 * <code>CV_DIST_L2</code> <em>(3x 3)</em> a=0.955, b=1.3693 \
 * <code>CV_DIST_L2</code> <em>(5x 5)</em> a=1, b=1.4, c=2.1969 \
 * ============== =================== ======================</p>
 *
 * <p>Typically, for a fast, coarse distance estimation <code>CV_DIST_L2</code>, a
 * <em>3x 3</em> mask is used. For a more accurate distance estimation
 * <code>CV_DIST_L2</code>, a <em>5x 5</em> mask or the precise algorithm is
 * used.
 * Note that both the precise and the approximate algorithms are linear on the
 * number of pixels.</p>
 *
 * <p>The second variant of the function does not only compute the minimum distance
 * for each pixel <em>(x, y)</em> but also identifies the nearest connected
 * component consisting of zero pixels (<code>labelType==DIST_LABEL_CCOMP</code>)
 * or the nearest zero pixel (<code>labelType==DIST_LABEL_PIXEL</code>). Index
 * of the component/pixel is stored in <em>labels(x, y)</em>.
 * When <code>labelType==DIST_LABEL_CCOMP</code>, the function automatically
 * finds connected components of zero pixels in the input image and marks them
 * with distinct labels. When <code>labelType==DIST_LABEL_CCOMP</code>, the
 * function scans through the input image and marks all the zero pixels with
 * distinct labels.</p>
 *
 * <p>In this mode, the complexity is still linear.
 * That is, the function provides a very fast way to compute the Voronoi diagram
 * for a binary image.
 * Currently, the second variant can use only the approximate distance transform
 * algorithm, i.e. <code>maskSize=CV_DIST_MASK_PRECISE</code> is not supported
 * yet.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example on using the distance transform can be found at
 * opencv_source_code/samples/cpp/distrans.cpp
 *   <li> (Python) An example on using the distance transform can be found at
 * opencv_source/samples/python2/distrans.py
 * </ul>
 *
 * @param src 8-bit, single-channel (binary) source image.
 * @param dst Output image with calculated distances. It is a 32-bit
 * floating-point, single-channel image of the same size as <code>src</code>.
 * @param labels Optional output 2D array of labels (the discrete Voronoi
 * diagram). It has the type <code>CV_32SC1</code> and the same size as
 * <code>src</code>. See the details below.
 * @param distanceType Type of distance. It can be <code>CV_DIST_L1,
 * CV_DIST_L2</code>, or <code>CV_DIST_C</code>.
 * @param maskSize Size of the distance transform mask. It can be 3, 5, or
 * <code>CV_DIST_MASK_PRECISE</code> (the latter option is only supported by the
 * first function). In case of the <code>CV_DIST_L1</code> or <code>CV_DIST_C</code>
 * distance type, the parameter is forced to 3 because a <em>3x 3</em> mask
 * gives the same result as <em>5x 5</em> or any larger aperture.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#distancetransform">org.opencv.imgproc.Imgproc.distanceTransform</a>
 */
    public static void distanceTransformWithLabels(Mat src, Mat dst, Mat labels, int distanceType, int maskSize)
    {

        distanceTransformWithLabels_1(src.nativeObj, dst.nativeObj, labels.nativeObj, distanceType, maskSize);

        return;
    }

    //
    // C++:  void drawContours(Mat& image, vector_vector_Point contours, int contourIdx, Scalar color, int thickness = 1, int lineType = 8, Mat hierarchy = Mat(), int maxLevel = INT_MAX, Point offset = Point())
    //

/**
 * <p>Draws contours outlines or filled contours.</p>
 *
 * <p>The function draws contour outlines in the image if <em>thickness >= 0</em>
 * or fills the area bounded by the contours if<em>thickness&lt0</em>. The
 * example below shows how to retrieve connected components from the binary
 * image and label them: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include "cv.h"</p>
 *
 * <p>#include "highgui.h"</p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src;</p>
 *
 * <p>// the first command-line parameter must be a filename of the binary</p>
 *
 * <p>// (black-n-white) image</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 0)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>Mat dst = Mat.zeros(src.rows, src.cols, CV_8UC3);</p>
 *
 * <p>src = src > 1;</p>
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>vector<vector<Point> > contours;</p>
 *
 * <p>vector<Vec4i> hierarchy;</p>
 *
 * <p>findContours(src, contours, hierarchy,</p>
 *
 * <p>CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);</p>
 *
 * <p>// iterate through all the top-level contours,</p>
 *
 * <p>// draw each connected component with its own random color</p>
 *
 * <p>int idx = 0;</p>
 *
 * <p>for(; idx >= 0; idx = hierarchy[idx][0])</p>
 *
 *
 * <p>Scalar color(rand()&255, rand()&255, rand()&255);</p>
 *
 * <p>drawContours(dst, contours, idx, color, CV_FILLED, 8, hierarchy);</p>
 *
 *
 * <p>namedWindow("Components", 1);</p>
 *
 * <p>imshow("Components", dst);</p>
 *
 * <p>waitKey(0);</p>
 *
 *
 * <p>Note: </code></p>
 * <ul>
 *   <li> An example using the drawContour functionality can be found at
 * opencv_source_code/samples/cpp/contours2.cpp
 *   <li> An example using drawContours to clean up a background segmentation
 * result at opencv_source_code/samples/cpp/segment_objects.cpp
 *   <li> (Python) An example using the drawContour functionality can be found
 * at opencv_source/samples/python2/contours.py
 * </ul>
 *
 * @param image Destination image.
 * @param contours All the input contours. Each contour is stored as a point
 * vector.
 * @param contourIdx Parameter indicating a contour to draw. If it is negative,
 * all the contours are drawn.
 * @param color Color of the contours.
 * @param thickness Thickness of lines the contours are drawn with. If it is
 * negative (for example, <code>thickness=CV_FILLED</code>), the contour
 * interiors are drawn.
 * @param lineType Line connectivity. See "line" for details.
 * @param hierarchy Optional information about hierarchy. It is only needed if
 * you want to draw only some of the contours (see <code>maxLevel</code>).
 * @param maxLevel Maximal level for drawn contours. If it is 0, only the
 * specified contour is drawn. If it is 1, the function draws the contour(s) and
 * all the nested contours. If it is 2, the function draws the contours, all the
 * nested contours, all the nested-to-nested contours, and so on. This parameter
 * is only taken into account when there is <code>hierarchy</code> available.
 * @param offset Optional contour shift parameter. Shift all the drawn contours
 * by the specified <em>offset=(dx,dy)</em>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#drawcontours">org.opencv.imgproc.Imgproc.drawContours</a>
 */
    public static void drawContours(Mat image, List<MatOfPoint> contours, int contourIdx, Scalar color, int thickness, int lineType, Mat hierarchy, int maxLevel, Point offset)
    {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_0(image.nativeObj, contours_mat.nativeObj, contourIdx, color.val[0], color.val[1], color.val[2], color.val[3], thickness, lineType, hierarchy.nativeObj, maxLevel, offset.x, offset.y);

        return;
    }

/**
 * <p>Draws contours outlines or filled contours.</p>
 *
 * <p>The function draws contour outlines in the image if <em>thickness >= 0</em>
 * or fills the area bounded by the contours if<em>thickness&lt0</em>. The
 * example below shows how to retrieve connected components from the binary
 * image and label them: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include "cv.h"</p>
 *
 * <p>#include "highgui.h"</p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src;</p>
 *
 * <p>// the first command-line parameter must be a filename of the binary</p>
 *
 * <p>// (black-n-white) image</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 0)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>Mat dst = Mat.zeros(src.rows, src.cols, CV_8UC3);</p>
 *
 * <p>src = src > 1;</p>
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>vector<vector<Point> > contours;</p>
 *
 * <p>vector<Vec4i> hierarchy;</p>
 *
 * <p>findContours(src, contours, hierarchy,</p>
 *
 * <p>CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);</p>
 *
 * <p>// iterate through all the top-level contours,</p>
 *
 * <p>// draw each connected component with its own random color</p>
 *
 * <p>int idx = 0;</p>
 *
 * <p>for(; idx >= 0; idx = hierarchy[idx][0])</p>
 *
 *
 * <p>Scalar color(rand()&255, rand()&255, rand()&255);</p>
 *
 * <p>drawContours(dst, contours, idx, color, CV_FILLED, 8, hierarchy);</p>
 *
 *
 * <p>namedWindow("Components", 1);</p>
 *
 * <p>imshow("Components", dst);</p>
 *
 * <p>waitKey(0);</p>
 *
 *
 * <p>Note: </code></p>
 * <ul>
 *   <li> An example using the drawContour functionality can be found at
 * opencv_source_code/samples/cpp/contours2.cpp
 *   <li> An example using drawContours to clean up a background segmentation
 * result at opencv_source_code/samples/cpp/segment_objects.cpp
 *   <li> (Python) An example using the drawContour functionality can be found
 * at opencv_source/samples/python2/contours.py
 * </ul>
 *
 * @param image Destination image.
 * @param contours All the input contours. Each contour is stored as a point
 * vector.
 * @param contourIdx Parameter indicating a contour to draw. If it is negative,
 * all the contours are drawn.
 * @param color Color of the contours.
 * @param thickness Thickness of lines the contours are drawn with. If it is
 * negative (for example, <code>thickness=CV_FILLED</code>), the contour
 * interiors are drawn.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#drawcontours">org.opencv.imgproc.Imgproc.drawContours</a>
 */
    public static void drawContours(Mat image, List<MatOfPoint> contours, int contourIdx, Scalar color, int thickness)
    {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_1(image.nativeObj, contours_mat.nativeObj, contourIdx, color.val[0], color.val[1], color.val[2], color.val[3], thickness);

        return;
    }

/**
 * <p>Draws contours outlines or filled contours.</p>
 *
 * <p>The function draws contour outlines in the image if <em>thickness >= 0</em>
 * or fills the area bounded by the contours if<em>thickness&lt0</em>. The
 * example below shows how to retrieve connected components from the binary
 * image and label them: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include "cv.h"</p>
 *
 * <p>#include "highgui.h"</p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int argc, char argv)</p>
 *
 *
 * <p>Mat src;</p>
 *
 * <p>// the first command-line parameter must be a filename of the binary</p>
 *
 * <p>// (black-n-white) image</p>
 *
 * <p>if(argc != 2 || !(src=imread(argv[1], 0)).data)</p>
 *
 * <p>return -1;</p>
 *
 * <p>Mat dst = Mat.zeros(src.rows, src.cols, CV_8UC3);</p>
 *
 * <p>src = src > 1;</p>
 *
 * <p>namedWindow("Source", 1);</p>
 *
 * <p>imshow("Source", src);</p>
 *
 * <p>vector<vector<Point> > contours;</p>
 *
 * <p>vector<Vec4i> hierarchy;</p>
 *
 * <p>findContours(src, contours, hierarchy,</p>
 *
 * <p>CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);</p>
 *
 * <p>// iterate through all the top-level contours,</p>
 *
 * <p>// draw each connected component with its own random color</p>
 *
 * <p>int idx = 0;</p>
 *
 * <p>for(; idx >= 0; idx = hierarchy[idx][0])</p>
 *
 *
 * <p>Scalar color(rand()&255, rand()&255, rand()&255);</p>
 *
 * <p>drawContours(dst, contours, idx, color, CV_FILLED, 8, hierarchy);</p>
 *
 *
 * <p>namedWindow("Components", 1);</p>
 *
 * <p>imshow("Components", dst);</p>
 *
 * <p>waitKey(0);</p>
 *
 *
 * <p>Note: </code></p>
 * <ul>
 *   <li> An example using the drawContour functionality can be found at
 * opencv_source_code/samples/cpp/contours2.cpp
 *   <li> An example using drawContours to clean up a background segmentation
 * result at opencv_source_code/samples/cpp/segment_objects.cpp
 *   <li> (Python) An example using the drawContour functionality can be found
 * at opencv_source/samples/python2/contours.py
 * </ul>
 *
 * @param image Destination image.
 * @param contours All the input contours. Each contour is stored as a point
 * vector.
 * @param contourIdx Parameter indicating a contour to draw. If it is negative,
 * all the contours are drawn.
 * @param color Color of the contours.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#drawcontours">org.opencv.imgproc.Imgproc.drawContours</a>
 */
    public static void drawContours(Mat image, List<MatOfPoint> contours, int contourIdx, Scalar color)
    {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_2(image.nativeObj, contours_mat.nativeObj, contourIdx, color.val[0], color.val[1], color.val[2], color.val[3]);

        return;
    }

    //
    // C++:  void equalizeHist(Mat src, Mat& dst)
    //

/**
 * <p>Equalizes the histogram of a grayscale image.</p>
 *
 * <p>The function equalizes the histogram of the input image using the following
 * algorithm:</p>
 * <ul>
 *   <li> Calculate the histogram <em>H</em> for <code>src</code>.
 *   <li> Normalize the histogram so that the sum of histogram bins is 255.
 *   <li> Compute the integral of the histogram:
 * </ul>
 *
 * <p><em>H'_i = sum(by: 0 <= j &lt i) H(j)</em></p>
 *
 * <ul>
 *   <li>
 * </ul>
 * <p>Transform the image using <em>H'</em> as a look-up table: <em>dst(x,y) =
 * H'(src(x,y))</em></p>
 *
 * <p>The algorithm normalizes the brightness and increases the contrast of the
 * image.</p>
 *
 * @param src Source 8-bit single channel image.
 * @param dst Destination image of the same size and type as <code>src</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/histograms.html#equalizehist">org.opencv.imgproc.Imgproc.equalizeHist</a>
 */
    public static void equalizeHist(Mat src, Mat dst)
    {

        equalizeHist_0(src.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void erode(Mat src, Mat& dst, Mat kernel, Point anchor = Point(-1,-1), int iterations = 1, int borderType = BORDER_CONSTANT, Scalar borderValue = morphologyDefaultBorderValue())
    //

/**
 * <p>Erodes an image by using a specific structuring element.</p>
 *
 * <p>The function erodes the source image using the specified structuring element
 * that determines the shape of a pixel neighborhood over which the minimum is
 * taken:</p>
 *
 * <p><em>dst(x,y) = min _((x',y'): element(x',y') != 0) src(x+x',y+y')</em></p>
 *
 * <p>The function supports the in-place mode. Erosion can be applied several
 * (<code>iterations</code>) times. In case of multi-channel images, each
 * channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphological erode operation can be found at
 * opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src input image; the number of channels can be arbitrary, but the
 * depth should be one of <code>CV_8U</code>, <code>CV_16U</code>,
 * <code>CV_16S</code>, <code>CV_32F" or </code>CV_64F".
 * @param dst output image of the same size and type as <code>src</code>.
 * @param kernel a kernel
 * @param anchor position of the anchor within the element; default value
 * <code>(-1, -1)</code> means that the anchor is at the element center.
 * @param iterations number of times erosion is applied.
 * @param borderType pixel extrapolation method (see "borderInterpolate" for
 * details).
 * @param borderValue border value in case of a constant border (see
 * "createMorphologyFilter" for details).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#erode">org.opencv.imgproc.Imgproc.erode</a>
 * @see org.opencv.imgproc.Imgproc#morphologyEx
 * @see org.opencv.imgproc.Imgproc#dilate
 */
    public static void erode(Mat src, Mat dst, Mat kernel, Point anchor, int iterations, int borderType, Scalar borderValue)
    {

        erode_0(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations, borderType, borderValue.val[0], borderValue.val[1], borderValue.val[2], borderValue.val[3]);

        return;
    }

/**
 * <p>Erodes an image by using a specific structuring element.</p>
 *
 * <p>The function erodes the source image using the specified structuring element
 * that determines the shape of a pixel neighborhood over which the minimum is
 * taken:</p>
 *
 * <p><em>dst(x,y) = min _((x',y'): element(x',y') != 0) src(x+x',y+y')</em></p>
 *
 * <p>The function supports the in-place mode. Erosion can be applied several
 * (<code>iterations</code>) times. In case of multi-channel images, each
 * channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphological erode operation can be found at
 * opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src input image; the number of channels can be arbitrary, but the
 * depth should be one of <code>CV_8U</code>, <code>CV_16U</code>,
 * <code>CV_16S</code>, <code>CV_32F" or </code>CV_64F".
 * @param dst output image of the same size and type as <code>src</code>.
 * @param kernel a kernel
 * @param anchor position of the anchor within the element; default value
 * <code>(-1, -1)</code> means that the anchor is at the element center.
 * @param iterations number of times erosion is applied.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#erode">org.opencv.imgproc.Imgproc.erode</a>
 * @see org.opencv.imgproc.Imgproc#morphologyEx
 * @see org.opencv.imgproc.Imgproc#dilate
 */
    public static void erode(Mat src, Mat dst, Mat kernel, Point anchor, int iterations)
    {

        erode_1(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations);

        return;
    }

/**
 * <p>Erodes an image by using a specific structuring element.</p>
 *
 * <p>The function erodes the source image using the specified structuring element
 * that determines the shape of a pixel neighborhood over which the minimum is
 * taken:</p>
 *
 * <p><em>dst(x,y) = min _((x',y'): element(x',y') != 0) src(x+x',y+y')</em></p>
 *
 * <p>The function supports the in-place mode. Erosion can be applied several
 * (<code>iterations</code>) times. In case of multi-channel images, each
 * channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphological erode operation can be found at
 * opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src input image; the number of channels can be arbitrary, but the
 * depth should be one of <code>CV_8U</code>, <code>CV_16U</code>,
 * <code>CV_16S</code>, <code>CV_32F" or </code>CV_64F".
 * @param dst output image of the same size and type as <code>src</code>.
 * @param kernel a kernel
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#erode">org.opencv.imgproc.Imgproc.erode</a>
 * @see org.opencv.imgproc.Imgproc#morphologyEx
 * @see org.opencv.imgproc.Imgproc#dilate
 */
    public static void erode(Mat src, Mat dst, Mat kernel)
    {

        erode_2(src.nativeObj, dst.nativeObj, kernel.nativeObj);

        return;
    }

    //
    // C++:  void filter2D(Mat src, Mat& dst, int ddepth, Mat kernel, Point anchor = Point(-1,-1), double delta = 0, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Convolves an image with the kernel.</p>
 *
 * <p>The function applies an arbitrary linear filter to an image. In-place
 * operation is supported. When the aperture is partially outside the image, the
 * function interpolates outlier pixel values according to the specified border
 * mode.</p>
 *
 * <p>The function does actually compute correlation, not the convolution:</p>
 *
 * <p><em>dst(x,y) = sum(by: 0 <= x' &lt kernel.cols, 0 <= y' &lt kernel.rows)
 * kernel(x',y')* src(x+x'- anchor.x,y+y'- anchor.y)</em></p>
 *
 * <p>That is, the kernel is not mirrored around the anchor point. If you need a
 * real convolution, flip the kernel using "flip" and set the new anchor to
 * <code>(kernel.cols - anchor.x - 1, kernel.rows - anchor.y - 1)</code>.</p>
 *
 * <p>The function uses the DFT-based algorithm in case of sufficiently large
 * kernels (~<code>11 x 11</code> or larger) and the direct algorithm (that uses
 * the engine retrieved by "createLinearFilter") for small kernels.</p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth desired depth of the destination image; if it is negative, it
 * will be the same as <code>src.depth()</code>; the following combinations of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the output image will have the same depth as the
 * source.</p>
 * @param kernel convolution kernel (or rather a correlation kernel), a
 * single-channel floating point matrix; if you want to apply different kernels
 * to different channels, split the image into separate color planes using
 * "split" and process them individually.
 * @param anchor anchor of the kernel that indicates the relative position of a
 * filtered point within the kernel; the anchor should lie within the kernel;
 * default value (-1,-1) means that the anchor is at the kernel center.
 * @param delta optional value added to the filtered pixels before storing them
 * in <code>dst</code>.
 * @param borderType pixel extrapolation method (see "borderInterpolate" for
 * details).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#filter2d">org.opencv.imgproc.Imgproc.filter2D</a>
 * @see org.opencv.imgproc.Imgproc#matchTemplate
 * @see org.opencv.core.Core#dft
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel, Point anchor, double delta, int borderType)
    {

        filter2D_0(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj, anchor.x, anchor.y, delta, borderType);

        return;
    }

/**
 * <p>Convolves an image with the kernel.</p>
 *
 * <p>The function applies an arbitrary linear filter to an image. In-place
 * operation is supported. When the aperture is partially outside the image, the
 * function interpolates outlier pixel values according to the specified border
 * mode.</p>
 *
 * <p>The function does actually compute correlation, not the convolution:</p>
 *
 * <p><em>dst(x,y) = sum(by: 0 <= x' &lt kernel.cols, 0 <= y' &lt kernel.rows)
 * kernel(x',y')* src(x+x'- anchor.x,y+y'- anchor.y)</em></p>
 *
 * <p>That is, the kernel is not mirrored around the anchor point. If you need a
 * real convolution, flip the kernel using "flip" and set the new anchor to
 * <code>(kernel.cols - anchor.x - 1, kernel.rows - anchor.y - 1)</code>.</p>
 *
 * <p>The function uses the DFT-based algorithm in case of sufficiently large
 * kernels (~<code>11 x 11</code> or larger) and the direct algorithm (that uses
 * the engine retrieved by "createLinearFilter") for small kernels.</p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth desired depth of the destination image; if it is negative, it
 * will be the same as <code>src.depth()</code>; the following combinations of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the output image will have the same depth as the
 * source.</p>
 * @param kernel convolution kernel (or rather a correlation kernel), a
 * single-channel floating point matrix; if you want to apply different kernels
 * to different channels, split the image into separate color planes using
 * "split" and process them individually.
 * @param anchor anchor of the kernel that indicates the relative position of a
 * filtered point within the kernel; the anchor should lie within the kernel;
 * default value (-1,-1) means that the anchor is at the kernel center.
 * @param delta optional value added to the filtered pixels before storing them
 * in <code>dst</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#filter2d">org.opencv.imgproc.Imgproc.filter2D</a>
 * @see org.opencv.imgproc.Imgproc#matchTemplate
 * @see org.opencv.core.Core#dft
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel, Point anchor, double delta)
    {

        filter2D_1(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj, anchor.x, anchor.y, delta);

        return;
    }

/**
 * <p>Convolves an image with the kernel.</p>
 *
 * <p>The function applies an arbitrary linear filter to an image. In-place
 * operation is supported. When the aperture is partially outside the image, the
 * function interpolates outlier pixel values according to the specified border
 * mode.</p>
 *
 * <p>The function does actually compute correlation, not the convolution:</p>
 *
 * <p><em>dst(x,y) = sum(by: 0 <= x' &lt kernel.cols, 0 <= y' &lt kernel.rows)
 * kernel(x',y')* src(x+x'- anchor.x,y+y'- anchor.y)</em></p>
 *
 * <p>That is, the kernel is not mirrored around the anchor point. If you need a
 * real convolution, flip the kernel using "flip" and set the new anchor to
 * <code>(kernel.cols - anchor.x - 1, kernel.rows - anchor.y - 1)</code>.</p>
 *
 * <p>The function uses the DFT-based algorithm in case of sufficiently large
 * kernels (~<code>11 x 11</code> or larger) and the direct algorithm (that uses
 * the engine retrieved by "createLinearFilter") for small kernels.</p>
 *
 * @param src input image.
 * @param dst output image of the same size and the same number of channels as
 * <code>src</code>.
 * @param ddepth desired depth of the destination image; if it is negative, it
 * will be the same as <code>src.depth()</code>; the following combinations of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the output image will have the same depth as the
 * source.</p>
 * @param kernel convolution kernel (or rather a correlation kernel), a
 * single-channel floating point matrix; if you want to apply different kernels
 * to different channels, split the image into separate color planes using
 * "split" and process them individually.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#filter2d">org.opencv.imgproc.Imgproc.filter2D</a>
 * @see org.opencv.imgproc.Imgproc#matchTemplate
 * @see org.opencv.core.Core#dft
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel)
    {

        filter2D_2(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj);

        return;
    }

    //
    // C++:  void findContours(Mat& image, vector_vector_Point& contours, Mat& hierarchy, int mode, int method, Point offset = Point())
    //

/**
 * <p>Finds contours in a binary image.</p>
 *
 * <p>The function retrieves contours from the binary image using the algorithm
 * [Suzuki85]. The contours are a useful tool for shape analysis and object
 * detection and recognition. See <code>squares.c</code> in the OpenCV sample
 * directory.</p>
 *
 * <p>Note: Source <code>image</code> is modified by this function. Also, the
 * function does not take into account 1-pixel border of the image (it's filled
 * with 0's and used for neighbor analysis in the algorithm), therefore the
 * contours touching the image border will be clipped.</p>
 *
 * <p>Note: If you use the new Python interface then the <code>CV_</code> prefix
 * has to be omitted in contour retrieval mode and contour approximation method
 * parameters (for example, use <code>cv2.RETR_LIST</code> and <code>cv2.CHAIN_APPROX_NONE</code>
 * parameters). If you use the old Python interface then these parameters have
 * the <code>CV_</code> prefix (for example, use <code>cv.CV_RETR_LIST</code>
 * and <code>cv.CV_CHAIN_APPROX_NONE</code>).</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the findContour functionality can be found at
 * opencv_source_code/samples/cpp/contours2.cpp
 *   <li> An example using findContours to clean up a background segmentation
 * result at opencv_source_code/samples/cpp/segment_objects.cpp
 *   <li> (Python) An example using the findContour functionality can be found
 * at opencv_source/samples/python2/contours.py
 *   <li> (Python) An example of detecting squares in an image can be found at
 * opencv_source/samples/python2/squares.py
 * </ul>
 *
 * @param image Source, an 8-bit single-channel image. Non-zero pixels are
 * treated as 1's. Zero pixels remain 0's, so the image is treated as
 * <code>binary</code>. You can use "compare", "inRange", "threshold",
 * "adaptiveThreshold", "Canny", and others to create a binary image out of a
 * grayscale or color one. The function modifies the <code>image</code> while
 * extracting the contours.
 * @param contours Detected contours. Each contour is stored as a vector of
 * points.
 * @param hierarchy Optional output vector, containing information about the
 * image topology. It has as many elements as the number of contours. For each
 * i-th contour <code>contours[i]</code>, the elements <code>hierarchy[i][0]</code>,
 * <code>hiearchy[i][1]</code>, <code>hiearchy[i][2]</code>, and
 * <code>hiearchy[i][3]</code> are set to 0-based indices in <code>contours</code>
 * of the next and previous contours at the same hierarchical level, the first
 * child contour and the parent contour, respectively. If for the contour
 * <code>i</code> there are no next, previous, parent, or nested contours, the
 * corresponding elements of <code>hierarchy[i]</code> will be negative.
 * @param mode Contour retrieval mode (if you use Python see also a note below).
 * <ul>
 *   <li> CV_RETR_EXTERNAL retrieves only the extreme outer contours. It sets
 * <code>hierarchy[i][2]=hierarchy[i][3]=-1</code> for all the contours.
 *   <li> CV_RETR_LIST retrieves all of the contours without establishing any
 * hierarchical relationships.
 *   <li> CV_RETR_CCOMP retrieves all of the contours and organizes them into a
 * two-level hierarchy. At the top level, there are external boundaries of the
 * components. At the second level, there are boundaries of the holes. If there
 * is another contour inside a hole of a connected component, it is still put at
 * the top level.
 *   <li> CV_RETR_TREE retrieves all of the contours and reconstructs a full
 * hierarchy of nested contours. This full hierarchy is built and shown in the
 * OpenCV <code>contours.c</code> demo.
 * </ul>
 * @param method Contour approximation method (if you use Python see also a note
 * below).
 * <ul>
 *   <li> CV_CHAIN_APPROX_NONE stores absolutely all the contour points. That
 * is, any 2 subsequent points <code>(x1,y1)</code> and <code>(x2,y2)</code> of
 * the contour will be either horizontal, vertical or diagonal neighbors, that
 * is, <code>max(abs(x1-x2),abs(y2-y1))==1</code>.
 *   <li> CV_CHAIN_APPROX_SIMPLE compresses horizontal, vertical, and diagonal
 * segments and leaves only their end points. For example, an up-right
 * rectangular contour is encoded with 4 points.
 *   <li> CV_CHAIN_APPROX_TC89_L1,CV_CHAIN_APPROX_TC89_KCOS applies one of the
 * flavors of the Teh-Chin chain approximation algorithm. See [TehChin89] for
 * details.
 * </ul>
 * @param offset Optional offset by which every contour point is shifted. This
 * is useful if the contours are extracted from the image ROI and then they
 * should be analyzed in the whole image context.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#findcontours">org.opencv.imgproc.Imgproc.findContours</a>
 */
    public static void findContours(Mat image, List<MatOfPoint> contours, Mat hierarchy, int mode, int method, Point offset)
    {
        Mat contours_mat = new Mat();
        findContours_0(image.nativeObj, contours_mat.nativeObj, hierarchy.nativeObj, mode, method, offset.x, offset.y);
        Converters.Mat_to_vector_vector_Point(contours_mat, contours);
        return;
    }

/**
 * <p>Finds contours in a binary image.</p>
 *
 * <p>The function retrieves contours from the binary image using the algorithm
 * [Suzuki85]. The contours are a useful tool for shape analysis and object
 * detection and recognition. See <code>squares.c</code> in the OpenCV sample
 * directory.</p>
 *
 * <p>Note: Source <code>image</code> is modified by this function. Also, the
 * function does not take into account 1-pixel border of the image (it's filled
 * with 0's and used for neighbor analysis in the algorithm), therefore the
 * contours touching the image border will be clipped.</p>
 *
 * <p>Note: If you use the new Python interface then the <code>CV_</code> prefix
 * has to be omitted in contour retrieval mode and contour approximation method
 * parameters (for example, use <code>cv2.RETR_LIST</code> and <code>cv2.CHAIN_APPROX_NONE</code>
 * parameters). If you use the old Python interface then these parameters have
 * the <code>CV_</code> prefix (for example, use <code>cv.CV_RETR_LIST</code>
 * and <code>cv.CV_CHAIN_APPROX_NONE</code>).</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the findContour functionality can be found at
 * opencv_source_code/samples/cpp/contours2.cpp
 *   <li> An example using findContours to clean up a background segmentation
 * result at opencv_source_code/samples/cpp/segment_objects.cpp
 *   <li> (Python) An example using the findContour functionality can be found
 * at opencv_source/samples/python2/contours.py
 *   <li> (Python) An example of detecting squares in an image can be found at
 * opencv_source/samples/python2/squares.py
 * </ul>
 *
 * @param image Source, an 8-bit single-channel image. Non-zero pixels are
 * treated as 1's. Zero pixels remain 0's, so the image is treated as
 * <code>binary</code>. You can use "compare", "inRange", "threshold",
 * "adaptiveThreshold", "Canny", and others to create a binary image out of a
 * grayscale or color one. The function modifies the <code>image</code> while
 * extracting the contours.
 * @param contours Detected contours. Each contour is stored as a vector of
 * points.
 * @param hierarchy Optional output vector, containing information about the
 * image topology. It has as many elements as the number of contours. For each
 * i-th contour <code>contours[i]</code>, the elements <code>hierarchy[i][0]</code>,
 * <code>hiearchy[i][1]</code>, <code>hiearchy[i][2]</code>, and
 * <code>hiearchy[i][3]</code> are set to 0-based indices in <code>contours</code>
 * of the next and previous contours at the same hierarchical level, the first
 * child contour and the parent contour, respectively. If for the contour
 * <code>i</code> there are no next, previous, parent, or nested contours, the
 * corresponding elements of <code>hierarchy[i]</code> will be negative.
 * @param mode Contour retrieval mode (if you use Python see also a note below).
 * <ul>
 *   <li> CV_RETR_EXTERNAL retrieves only the extreme outer contours. It sets
 * <code>hierarchy[i][2]=hierarchy[i][3]=-1</code> for all the contours.
 *   <li> CV_RETR_LIST retrieves all of the contours without establishing any
 * hierarchical relationships.
 *   <li> CV_RETR_CCOMP retrieves all of the contours and organizes them into a
 * two-level hierarchy. At the top level, there are external boundaries of the
 * components. At the second level, there are boundaries of the holes. If there
 * is another contour inside a hole of a connected component, it is still put at
 * the top level.
 *   <li> CV_RETR_TREE retrieves all of the contours and reconstructs a full
 * hierarchy of nested contours. This full hierarchy is built and shown in the
 * OpenCV <code>contours.c</code> demo.
 * </ul>
 * @param method Contour approximation method (if you use Python see also a note
 * below).
 * <ul>
 *   <li> CV_CHAIN_APPROX_NONE stores absolutely all the contour points. That
 * is, any 2 subsequent points <code>(x1,y1)</code> and <code>(x2,y2)</code> of
 * the contour will be either horizontal, vertical or diagonal neighbors, that
 * is, <code>max(abs(x1-x2),abs(y2-y1))==1</code>.
 *   <li> CV_CHAIN_APPROX_SIMPLE compresses horizontal, vertical, and diagonal
 * segments and leaves only their end points. For example, an up-right
 * rectangular contour is encoded with 4 points.
 *   <li> CV_CHAIN_APPROX_TC89_L1,CV_CHAIN_APPROX_TC89_KCOS applies one of the
 * flavors of the Teh-Chin chain approximation algorithm. See [TehChin89] for
 * details.
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#findcontours">org.opencv.imgproc.Imgproc.findContours</a>
 */
    public static void findContours(Mat image, List<MatOfPoint> contours, Mat hierarchy, int mode, int method)
    {
        Mat contours_mat = new Mat();
        findContours_1(image.nativeObj, contours_mat.nativeObj, hierarchy.nativeObj, mode, method);
        Converters.Mat_to_vector_vector_Point(contours_mat, contours);
        return;
    }

    //
    // C++:  RotatedRect fitEllipse(vector_Point2f points)
    //

/**
 * <p>Fits an ellipse around a set of 2D points.</p>
 *
 * <p>The function calculates the ellipse that fits (in a least-squares sense) a
 * set of 2D points best of all. It returns the rotated rectangle in which the
 * ellipse is inscribed. The algorithm [Fitzgibbon95] is used.
 * Developer should keep in mind that it is possible that the returned
 * ellipse/rotatedRect data contains negative indices, due to the data points
 * being close to the border of the containing Mat element.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the fitEllipse technique can be found at
 * opencv_source_code/samples/cpp/fitellipse.cpp
 * </ul>
 *
 * @param points Input 2D point set, stored in:
 * <ul>
 *   <li> <code>std.vector<></code> or <code>Mat</code> (C++ interface)
 *   <li> <code>CvSeq*</code> or <code>CvMat*</code> (C interface)
 *   <li> Nx2 numpy array (Python interface)
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#fitellipse">org.opencv.imgproc.Imgproc.fitEllipse</a>
 */
    public static RotatedRect fitEllipse(MatOfPoint2f points)
    {
        Mat points_mat = points;
        RotatedRect retVal = new RotatedRect(fitEllipse_0(points_mat.nativeObj));

        return retVal;
    }

    //
    // C++:  void fitLine(Mat points, Mat& line, int distType, double param, double reps, double aeps)
    //

/**
 * <p>Fits a line to a 2D or 3D point set.</p>
 *
 * <p>The function <code>fitLine</code> fits a line to a 2D or 3D point set by
 * minimizing <em>sum_i rho(r_i)</em> where <em>r_i</em> is a distance between
 * the <em>i^(th)</em> point, the line and <em>rho(r)</em> is a distance
 * function, one of the following:</p>
 * <ul>
 *   <li> distType=CV_DIST_L2
 * </ul>
 *
 * <p><em>rho(r) = r^2/2(the simplest and the fastest least-squares method)</em></p>
 *
 * <ul>
 *   <li> distType=CV_DIST_L1
 * </ul>
 *
 * <p><em>rho(r) = r</em></p>
 *
 * <ul>
 *   <li> distType=CV_DIST_L12
 * </ul>
 *
 * <p><em>rho(r) = 2 * (sqrt(1 + frac(r^2)2) - 1)</em></p>
 *
 * <ul>
 *   <li> distType=CV_DIST_FAIR
 * </ul>
 *
 * <p><em>rho(r) = C^2 * ((r)/(C) - log((1 + (r)/(C)))) where C=1.3998</em></p>
 *
 * <ul>
 *   <li> distType=CV_DIST_WELSCH
 * </ul>
 *
 * <p><em>rho(r) = (C^2)/2 * (1 - exp((-((r)/(C))^2))) where C=2.9846</em></p>
 *
 * <ul>
 *   <li> distType=CV_DIST_HUBER
 * </ul>
 *
 * <p><em>rho(r) = r^2/2 if r &lt C; C * (r-C/2) otherwise where C=1.345</em></p>
 *
 * <p>The algorithm is based on the M-estimator (http://en.wikipedia.org/wiki/M-estimator)
 * technique that iteratively fits the line using the weighted least-squares
 * algorithm. After each iteration the weights <em>w_i</em> are adjusted to be
 * inversely proportional to <em>rho(r_i)</em>... Sample code:</p>
 * <ul>
 *   <li> (Python) An example of robust line fitting can be found at
 * opencv_source_code/samples/python2/fitline.py
 * </ul>
 *
 * @param points Input vector of 2D or 3D points, stored in <code>std.vector<></code>
 * or <code>Mat</code>.
 * @param line Output line parameters. In case of 2D fitting, it should be a
 * vector of 4 elements (like <code>Vec4f</code>) - <code>(vx, vy, x0,
 * y0)</code>, where <code>(vx, vy)</code> is a normalized vector collinear to
 * the line and <code>(x0, y0)</code> is a point on the line. In case of 3D
 * fitting, it should be a vector of 6 elements (like <code>Vec6f</code>) -
 * <code>(vx, vy, vz, x0, y0, z0)</code>, where <code>(vx, vy, vz)</code> is a
 * normalized vector collinear to the line and <code>(x0, y0, z0)</code> is a
 * point on the line.
 * @param distType Distance used by the M-estimator (see the discussion below).
 * @param param Numerical parameter (<code>C</code>) for some types of
 * distances. If it is 0, an optimal value is chosen.
 * @param reps Sufficient accuracy for the radius (distance between the
 * coordinate origin and the line).
 * @param aeps Sufficient accuracy for the angle. 0.01 would be a good default
 * value for <code>reps</code> and <code>aeps</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#fitline">org.opencv.imgproc.Imgproc.fitLine</a>
 */
    public static void fitLine(Mat points, Mat line, int distType, double param, double reps, double aeps)
    {

        fitLine_0(points.nativeObj, line.nativeObj, distType, param, reps, aeps);

        return;
    }

    //
    // C++:  int floodFill(Mat& image, Mat& mask, Point seedPoint, Scalar newVal, Rect* rect = 0, Scalar loDiff = Scalar(), Scalar upDiff = Scalar(), int flags = 4)
    //

/**
 * <p>Fills a connected component with the given color.</p>
 *
 * <p>The functions <code>floodFill</code> fill a connected component starting from
 * the seed point with the specified color. The connectivity is determined by
 * the color/brightness closeness of the neighbor pixels. The pixel at
 * <em>(x,y)</em> is considered to belong to the repainted domain if:</p>
 * <ul>
 *   <li> <em>src(x',y')- loDiff <= src(x,y) <= src(x',y')+ upDiff</em>
 * </ul>
 *
 * <p>in case of a grayscale image and floating range</p>
 * <ul>
 *   <li> <em>src(seedPoint.x, seedPoint.y)- loDiff <= src(x,y) <=
 * src(seedPoint.x, seedPoint.y)+ upDiff</em>
 * </ul>
 *
 * <p>in case of a grayscale image and fixed range</p>
 * <ul>
 *   <li> <em>src(x',y')_r- loDiff _r <= src(x,y)_r <= src(x',y')_r+ upDiff
 * _r,</em>
 *
 *
 * </ul>
 *
 * <p><em>src(x',y')_g- loDiff _g <= src(x,y)_g <= src(x',y')_g+ upDiff _g</em></p>
 *
 * <p>and</p>
 *
 * <p><em>src(x',y')_b- loDiff _b <= src(x,y)_b <= src(x',y')_b+ upDiff _b</em></p>
 *
 * <p>in case of a color image and floating range</p>
 * <ul>
 *   <li> <em>src(seedPoint.x, seedPoint.y)_r- loDiff _r <= src(x,y)_r <=
 * src(seedPoint.x, seedPoint.y)_r+ upDiff _r,</em>
 *
 *
 * </ul>
 *
 * <p><em>src(seedPoint.x, seedPoint.y)_g- loDiff _g <= src(x,y)_g <=
 * src(seedPoint.x, seedPoint.y)_g+ upDiff _g</em></p>
 *
 * <p>and</p>
 *
 * <p><em>src(seedPoint.x, seedPoint.y)_b- loDiff _b <= src(x,y)_b <=
 * src(seedPoint.x, seedPoint.y)_b+ upDiff _b</em></p>
 *
 * <p>in case of a color image and fixed range</p>
 *
 * <p>where <em>src(x',y')</em> is the value of one of pixel neighbors that is
 * already known to belong to the component. That is, to be added to the
 * connected component, a color/brightness of the pixel should be close enough
 * to:</p>
 * <ul>
 *   <li> Color/brightness of one of its neighbors that already belong to the
 * connected component in case of a floating range.
 *   <li> Color/brightness of the seed point in case of a fixed range.
 * </ul>
 *
 * <p>Use these functions to either mark a connected component with the specified
 * color in-place, or build a mask and then extract the contour, or copy the
 * region to another image, and so on.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the FloodFill technique can be found at
 * opencv_source_code/samples/cpp/ffilldemo.cpp
 *   <li> (Python) An example using the FloodFill technique can be found at
 * opencv_source_code/samples/python2/floodfill.cpp
 * </ul>
 *
 * @param image Input/output 1- or 3-channel, 8-bit, or floating-point image. It
 * is modified by the function unless the <code>FLOODFILL_MASK_ONLY</code> flag
 * is set in the second variant of the function. See the details below.
 * @param mask (For the second function only) Operation mask that should be a
 * single-channel 8-bit image, 2 pixels wider and 2 pixels taller. The function
 * uses and updates the mask, so you take responsibility of initializing the
 * <code>mask</code> content. Flood-filling cannot go across non-zero pixels in
 * the mask. For example, an edge detector output can be used as a mask to stop
 * filling at edges. It is possible to use the same mask in multiple calls to
 * the function to make sure the filled area does not overlap.
 *
 * <p>Note: Since the mask is larger than the filled image, a pixel <em>(x, y)</em>
 * in <code>image</code> corresponds to the pixel <em>(x+1, y+1)</em> in the
 * <code>mask</code>.</p>
 * @param seedPoint Starting point.
 * @param newVal New value of the repainted domain pixels.
 * @param rect Optional output parameter set by the function to the minimum
 * bounding rectangle of the repainted domain.
 * @param loDiff Maximal lower brightness/color difference between the currently
 * observed pixel and one of its neighbors belonging to the component, or a seed
 * pixel being added to the component.
 * @param upDiff Maximal upper brightness/color difference between the currently
 * observed pixel and one of its neighbors belonging to the component, or a seed
 * pixel being added to the component.
 * @param flags Operation flags. Lower bits contain a connectivity value, 4
 * (default) or 8, used within the function. Connectivity determines which
 * neighbors of a pixel are considered. Upper bits can be 0 or a combination of
 * the following flags:
 * <ul>
 *   <li> FLOODFILL_FIXED_RANGE If set, the difference between the current pixel
 * and seed pixel is considered. Otherwise, the difference between neighbor
 * pixels is considered (that is, the range is floating).
 *   <li> FLOODFILL_MASK_ONLY If set, the function does not change the image
 * (<code>newVal</code> is ignored), but fills the mask. The flag can be used
 * for the second variant only.
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#floodfill">org.opencv.imgproc.Imgproc.floodFill</a>
 * @see org.opencv.imgproc.Imgproc#findContours
 */
    public static int floodFill(Mat image, Mat mask, Point seedPoint, Scalar newVal, Rect rect, Scalar loDiff, Scalar upDiff, int flags)
    {
        double[] rect_out = new double[4];
        int retVal = floodFill_0(image.nativeObj, mask.nativeObj, seedPoint.x, seedPoint.y, newVal.val[0], newVal.val[1], newVal.val[2], newVal.val[3], rect_out, loDiff.val[0], loDiff.val[1], loDiff.val[2], loDiff.val[3], upDiff.val[0], upDiff.val[1], upDiff.val[2], upDiff.val[3], flags);
        if(rect!=null){ rect.x = (int)rect_out[0]; rect.y = (int)rect_out[1]; rect.width = (int)rect_out[2]; rect.height = (int)rect_out[3]; }
        return retVal;
    }

/**
 * <p>Fills a connected component with the given color.</p>
 *
 * <p>The functions <code>floodFill</code> fill a connected component starting from
 * the seed point with the specified color. The connectivity is determined by
 * the color/brightness closeness of the neighbor pixels. The pixel at
 * <em>(x,y)</em> is considered to belong to the repainted domain if:</p>
 * <ul>
 *   <li> <em>src(x',y')- loDiff <= src(x,y) <= src(x',y')+ upDiff</em>
 * </ul>
 *
 * <p>in case of a grayscale image and floating range</p>
 * <ul>
 *   <li> <em>src(seedPoint.x, seedPoint.y)- loDiff <= src(x,y) <=
 * src(seedPoint.x, seedPoint.y)+ upDiff</em>
 * </ul>
 *
 * <p>in case of a grayscale image and fixed range</p>
 * <ul>
 *   <li> <em>src(x',y')_r- loDiff _r <= src(x,y)_r <= src(x',y')_r+ upDiff
 * _r,</em>
 *
 *
 * </ul>
 *
 * <p><em>src(x',y')_g- loDiff _g <= src(x,y)_g <= src(x',y')_g+ upDiff _g</em></p>
 *
 * <p>and</p>
 *
 * <p><em>src(x',y')_b- loDiff _b <= src(x,y)_b <= src(x',y')_b+ upDiff _b</em></p>
 *
 * <p>in case of a color image and floating range</p>
 * <ul>
 *   <li> <em>src(seedPoint.x, seedPoint.y)_r- loDiff _r <= src(x,y)_r <=
 * src(seedPoint.x, seedPoint.y)_r+ upDiff _r,</em>
 *
 *
 * </ul>
 *
 * <p><em>src(seedPoint.x, seedPoint.y)_g- loDiff _g <= src(x,y)_g <=
 * src(seedPoint.x, seedPoint.y)_g+ upDiff _g</em></p>
 *
 * <p>and</p>
 *
 * <p><em>src(seedPoint.x, seedPoint.y)_b- loDiff _b <= src(x,y)_b <=
 * src(seedPoint.x, seedPoint.y)_b+ upDiff _b</em></p>
 *
 * <p>in case of a color image and fixed range</p>
 *
 * <p>where <em>src(x',y')</em> is the value of one of pixel neighbors that is
 * already known to belong to the component. That is, to be added to the
 * connected component, a color/brightness of the pixel should be close enough
 * to:</p>
 * <ul>
 *   <li> Color/brightness of one of its neighbors that already belong to the
 * connected component in case of a floating range.
 *   <li> Color/brightness of the seed point in case of a fixed range.
 * </ul>
 *
 * <p>Use these functions to either mark a connected component with the specified
 * color in-place, or build a mask and then extract the contour, or copy the
 * region to another image, and so on.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the FloodFill technique can be found at
 * opencv_source_code/samples/cpp/ffilldemo.cpp
 *   <li> (Python) An example using the FloodFill technique can be found at
 * opencv_source_code/samples/python2/floodfill.cpp
 * </ul>
 *
 * @param image Input/output 1- or 3-channel, 8-bit, or floating-point image. It
 * is modified by the function unless the <code>FLOODFILL_MASK_ONLY</code> flag
 * is set in the second variant of the function. See the details below.
 * @param mask (For the second function only) Operation mask that should be a
 * single-channel 8-bit image, 2 pixels wider and 2 pixels taller. The function
 * uses and updates the mask, so you take responsibility of initializing the
 * <code>mask</code> content. Flood-filling cannot go across non-zero pixels in
 * the mask. For example, an edge detector output can be used as a mask to stop
 * filling at edges. It is possible to use the same mask in multiple calls to
 * the function to make sure the filled area does not overlap.
 *
 * <p>Note: Since the mask is larger than the filled image, a pixel <em>(x, y)</em>
 * in <code>image</code> corresponds to the pixel <em>(x+1, y+1)</em> in the
 * <code>mask</code>.</p>
 * @param seedPoint Starting point.
 * @param newVal New value of the repainted domain pixels.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#floodfill">org.opencv.imgproc.Imgproc.floodFill</a>
 * @see org.opencv.imgproc.Imgproc#findContours
 */
    public static int floodFill(Mat image, Mat mask, Point seedPoint, Scalar newVal)
    {

        int retVal = floodFill_1(image.nativeObj, mask.nativeObj, seedPoint.x, seedPoint.y, newVal.val[0], newVal.val[1], newVal.val[2], newVal.val[3]);

        return retVal;
    }

    //
    // C++:  Mat getAffineTransform(vector_Point2f src, vector_Point2f dst)
    //

/**
 * <p>Calculates an affine transform from three pairs of the corresponding points.</p>
 *
 * <p>The function calculates the <em>2 x 3</em> matrix of an affine transform so
 * that:</p>
 *
 * <p><em>x'_i
 * y'_i = map_matrix * x_i
 * y_i
 * 1 </em></p>
 *
 * <p>where</p>
 *
 * <p><em>dst(i)=(x'_i,y'_i),&ltBR&gtsrc(i)=(x_i, y_i),&ltBR&gti=0,1,2</em></p>
 *
 * @param src Coordinates of triangle vertices in the source image.
 * @param dst Coordinates of the corresponding triangle vertices in the
 * destination image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getaffinetransform">org.opencv.imgproc.Imgproc.getAffineTransform</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.core.Core#transform
 */
    public static Mat getAffineTransform(MatOfPoint2f src, MatOfPoint2f dst)
    {
        Mat src_mat = src;
        Mat dst_mat = dst;
        Mat retVal = new Mat(getAffineTransform_0(src_mat.nativeObj, dst_mat.nativeObj));

        return retVal;
    }

    //
    // C++:  Mat getDefaultNewCameraMatrix(Mat cameraMatrix, Size imgsize = Size(), bool centerPrincipalPoint = false)
    //

/**
 * <p>Returns the default new camera matrix.</p>
 *
 * <p>The function returns the camera matrix that is either an exact copy of the
 * input <code>cameraMatrix</code> (when <code>centerPrinicipalPoint=false</code>),
 * or the modified one (when <code>centerPrincipalPoint=true</code>).</p>
 *
 * <p>In the latter case, the new camera matrix will be:</p>
 *
 * <p><em>f_x 0(imgSize.width -1)*0.5
 * 0 f_y(imgSize.height -1)*0.5
 * 0 0 1,</em></p>
 *
 * <p>where <em>f_x</em> and <em>f_y</em> are <em>(0,0)</em> and <em>(1,1)</em>
 * elements of <code>cameraMatrix</code>, respectively.</p>
 *
 * <p>By default, the undistortion functions in OpenCV (see "initUndistortRectifyMap",
 * "undistort") do not move the principal point. However, when you work with
 * stereo, it is important to move the principal points in both views to the
 * same y-coordinate (which is required by most of stereo correspondence
 * algorithms), and may be to the same x-coordinate too. So, you can form the
 * new camera matrix for each view where the principal points are located at the
 * center.</p>
 *
 * @param cameraMatrix Input camera matrix.
 * @param imgsize Camera view image size in pixels.
 * @param centerPrincipalPoint Location of the principal point in the new camera
 * matrix. The parameter indicates whether this location should be at the image
 * center or not.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getdefaultnewcameramatrix">org.opencv.imgproc.Imgproc.getDefaultNewCameraMatrix</a>
 */
    public static Mat getDefaultNewCameraMatrix(Mat cameraMatrix, Size imgsize, boolean centerPrincipalPoint)
    {

        Mat retVal = new Mat(getDefaultNewCameraMatrix_0(cameraMatrix.nativeObj, imgsize.width, imgsize.height, centerPrincipalPoint));

        return retVal;
    }

/**
 * <p>Returns the default new camera matrix.</p>
 *
 * <p>The function returns the camera matrix that is either an exact copy of the
 * input <code>cameraMatrix</code> (when <code>centerPrinicipalPoint=false</code>),
 * or the modified one (when <code>centerPrincipalPoint=true</code>).</p>
 *
 * <p>In the latter case, the new camera matrix will be:</p>
 *
 * <p><em>f_x 0(imgSize.width -1)*0.5
 * 0 f_y(imgSize.height -1)*0.5
 * 0 0 1,</em></p>
 *
 * <p>where <em>f_x</em> and <em>f_y</em> are <em>(0,0)</em> and <em>(1,1)</em>
 * elements of <code>cameraMatrix</code>, respectively.</p>
 *
 * <p>By default, the undistortion functions in OpenCV (see "initUndistortRectifyMap",
 * "undistort") do not move the principal point. However, when you work with
 * stereo, it is important to move the principal points in both views to the
 * same y-coordinate (which is required by most of stereo correspondence
 * algorithms), and may be to the same x-coordinate too. So, you can form the
 * new camera matrix for each view where the principal points are located at the
 * center.</p>
 *
 * @param cameraMatrix Input camera matrix.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getdefaultnewcameramatrix">org.opencv.imgproc.Imgproc.getDefaultNewCameraMatrix</a>
 */
    public static Mat getDefaultNewCameraMatrix(Mat cameraMatrix)
    {

        Mat retVal = new Mat(getDefaultNewCameraMatrix_1(cameraMatrix.nativeObj));

        return retVal;
    }

    //
    // C++:  void getDerivKernels(Mat& kx, Mat& ky, int dx, int dy, int ksize, bool normalize = false, int ktype = CV_32F)
    //

/**
 * <p>Returns filter coefficients for computing spatial image derivatives.</p>
 *
 * <p>The function computes and returns the filter coefficients for spatial image
 * derivatives. When <code>ksize=CV_SCHARR</code>, the Scharr <em>3 x 3</em>
 * kernels are generated (see "Scharr"). Otherwise, Sobel kernels are generated
 * (see "Sobel"). The filters are normally passed to "sepFilter2D" or to
 * "createSeparableLinearFilter".</p>
 *
 * @param kx Output matrix of row filter coefficients. It has the type
 * <code>ktype</code>.
 * @param ky Output matrix of column filter coefficients. It has the type
 * <code>ktype</code>.
 * @param dx Derivative order in respect of x.
 * @param dy Derivative order in respect of y.
 * @param ksize Aperture size. It can be <code>CV_SCHARR</code>, 1, 3, 5, or 7.
 * @param normalize Flag indicating whether to normalize (scale down) the filter
 * coefficients or not. Theoretically, the coefficients should have the
 * denominator <em>=2^(ksize*2-dx-dy-2)</em>. If you are going to filter
 * floating-point images, you are likely to use the normalized kernels. But if
 * you compute derivatives of an 8-bit image, store the results in a 16-bit
 * image, and wish to preserve all the fractional bits, you may want to set
 * <code>normalize=false</code>.
 * @param ktype Type of filter coefficients. It can be <code>CV_32f</code> or
 * <code>CV_64F</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#getderivkernels">org.opencv.imgproc.Imgproc.getDerivKernels</a>
 */
    public static void getDerivKernels(Mat kx, Mat ky, int dx, int dy, int ksize, boolean normalize, int ktype)
    {

        getDerivKernels_0(kx.nativeObj, ky.nativeObj, dx, dy, ksize, normalize, ktype);

        return;
    }

/**
 * <p>Returns filter coefficients for computing spatial image derivatives.</p>
 *
 * <p>The function computes and returns the filter coefficients for spatial image
 * derivatives. When <code>ksize=CV_SCHARR</code>, the Scharr <em>3 x 3</em>
 * kernels are generated (see "Scharr"). Otherwise, Sobel kernels are generated
 * (see "Sobel"). The filters are normally passed to "sepFilter2D" or to
 * "createSeparableLinearFilter".</p>
 *
 * @param kx Output matrix of row filter coefficients. It has the type
 * <code>ktype</code>.
 * @param ky Output matrix of column filter coefficients. It has the type
 * <code>ktype</code>.
 * @param dx Derivative order in respect of x.
 * @param dy Derivative order in respect of y.
 * @param ksize Aperture size. It can be <code>CV_SCHARR</code>, 1, 3, 5, or 7.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#getderivkernels">org.opencv.imgproc.Imgproc.getDerivKernels</a>
 */
    public static void getDerivKernels(Mat kx, Mat ky, int dx, int dy, int ksize)
    {

        getDerivKernels_1(kx.nativeObj, ky.nativeObj, dx, dy, ksize);

        return;
    }

    //
    // C++:  Mat getGaborKernel(Size ksize, double sigma, double theta, double lambd, double gamma, double psi = CV_PI*0.5, int ktype = CV_64F)
    //

    public static Mat getGaborKernel(Size ksize, double sigma, double theta, double lambd, double gamma, double psi, int ktype)
    {

        Mat retVal = new Mat(getGaborKernel_0(ksize.width, ksize.height, sigma, theta, lambd, gamma, psi, ktype));

        return retVal;
    }

    public static Mat getGaborKernel(Size ksize, double sigma, double theta, double lambd, double gamma)
    {

        Mat retVal = new Mat(getGaborKernel_1(ksize.width, ksize.height, sigma, theta, lambd, gamma));

        return retVal;
    }

    //
    // C++:  Mat getGaussianKernel(int ksize, double sigma, int ktype = CV_64F)
    //

/**
 * <p>Returns Gaussian filter coefficients.</p>
 *
 * <p>The function computes and returns the <em>ksize x 1</em> matrix of Gaussian
 * filter coefficients:</p>
 *
 * <p><em>G_i= alpha *e^(-(i-(ksize -1)/2)^2/(2* sigma)^2),</em></p>
 *
 * <p>where <em>i=0..ksize-1</em> and <em>alpha</em> is the scale factor chosen so
 * that <em>sum_i G_i=1</em>.</p>
 *
 * <p>Two of such generated kernels can be passed to "sepFilter2D" or to
 * "createSeparableLinearFilter". Those functions automatically recognize
 * smoothing kernels (a symmetrical kernel with sum of weights equal to 1) and
 * handle them accordingly. You may also use the higher-level "GaussianBlur".</p>
 *
 * @param ksize Aperture size. It should be odd (<em>ksize mod 2 = 1</em>) and
 * positive.
 * @param sigma Gaussian standard deviation. If it is non-positive, it is
 * computed from <code>ksize</code> as <code>sigma = 0.3*((ksize-1)*0.5 - 1) +
 * 0.8</code>.
 * @param ktype Type of filter coefficients. It can be <code>CV_32f</code> or
 * <code>CV_64F</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#getgaussiankernel">org.opencv.imgproc.Imgproc.getGaussianKernel</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#getStructuringElement
 * @see org.opencv.imgproc.Imgproc#getDerivKernels
 */
    public static Mat getGaussianKernel(int ksize, double sigma, int ktype)
    {

        Mat retVal = new Mat(getGaussianKernel_0(ksize, sigma, ktype));

        return retVal;
    }

/**
 * <p>Returns Gaussian filter coefficients.</p>
 *
 * <p>The function computes and returns the <em>ksize x 1</em> matrix of Gaussian
 * filter coefficients:</p>
 *
 * <p><em>G_i= alpha *e^(-(i-(ksize -1)/2)^2/(2* sigma)^2),</em></p>
 *
 * <p>where <em>i=0..ksize-1</em> and <em>alpha</em> is the scale factor chosen so
 * that <em>sum_i G_i=1</em>.</p>
 *
 * <p>Two of such generated kernels can be passed to "sepFilter2D" or to
 * "createSeparableLinearFilter". Those functions automatically recognize
 * smoothing kernels (a symmetrical kernel with sum of weights equal to 1) and
 * handle them accordingly. You may also use the higher-level "GaussianBlur".</p>
 *
 * @param ksize Aperture size. It should be odd (<em>ksize mod 2 = 1</em>) and
 * positive.
 * @param sigma Gaussian standard deviation. If it is non-positive, it is
 * computed from <code>ksize</code> as <code>sigma = 0.3*((ksize-1)*0.5 - 1) +
 * 0.8</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#getgaussiankernel">org.opencv.imgproc.Imgproc.getGaussianKernel</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#sepFilter2D
 * @see org.opencv.imgproc.Imgproc#getStructuringElement
 * @see org.opencv.imgproc.Imgproc#getDerivKernels
 */
    public static Mat getGaussianKernel(int ksize, double sigma)
    {

        Mat retVal = new Mat(getGaussianKernel_1(ksize, sigma));

        return retVal;
    }

    //
    // C++:  Mat getPerspectiveTransform(Mat src, Mat dst)
    //

/**
 * <p>Calculates a perspective transform from four pairs of the corresponding
 * points.</p>
 *
 * <p>The function calculates the <em>3 x 3</em> matrix of a perspective transform
 * so that:</p>
 *
 * <p><em>t_i x'_i
 * t_i y'_i
 * t_i = map_matrix * x_i
 * y_i
 * 1 </em></p>
 *
 * <p>where</p>
 *
 * <p><em>dst(i)=(x'_i,y'_i),&ltBR&gtsrc(i)=(x_i, y_i),&ltBR&gti=0,1,2,3</em></p>
 *
 * @param src Coordinates of quadrangle vertices in the source image.
 * @param dst Coordinates of the corresponding quadrangle vertices in the
 * destination image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getperspectivetransform">org.opencv.imgproc.Imgproc.getPerspectiveTransform</a>
 * @see org.opencv.core.Core#perspectiveTransform
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 */
    public static Mat getPerspectiveTransform(Mat src, Mat dst)
    {

        Mat retVal = new Mat(getPerspectiveTransform_0(src.nativeObj, dst.nativeObj));

        return retVal;
    }

    //
    // C++:  void getRectSubPix(Mat image, Size patchSize, Point2f center, Mat& patch, int patchType = -1)
    //

/**
 * <p>Retrieves a pixel rectangle from an image with sub-pixel accuracy.</p>
 *
 * <p>The function <code>getRectSubPix</code> extracts pixels from <code>src</code></p>
 *
 * <p><em>dst(x, y) = src(x + center.x - (dst.cols -1)*0.5, y + center.y -
 * (dst.rows -1)*0.5)</em></p>
 *
 * <p>where the values of the pixels at non-integer coordinates are retrieved using
 * bilinear interpolation. Every channel of multi-channel images is processed
 * independently. While the center of the rectangle must be inside the image,
 * parts of the rectangle may be outside. In this case, the replication border
 * mode (see "borderInterpolate") is used to extrapolate the pixel values
 * outside of the image.</p>
 *
 * @param image a image
 * @param patchSize Size of the extracted patch.
 * @param center Floating point coordinates of the center of the extracted
 * rectangle within the source image. The center must be inside the image.
 * @param patch a patch
 * @param patchType Depth of the extracted pixels. By default, they have the
 * same depth as <code>src</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getrectsubpix">org.opencv.imgproc.Imgproc.getRectSubPix</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 */
    public static void getRectSubPix(Mat image, Size patchSize, Point center, Mat patch, int patchType)
    {

        getRectSubPix_0(image.nativeObj, patchSize.width, patchSize.height, center.x, center.y, patch.nativeObj, patchType);

        return;
    }

/**
 * <p>Retrieves a pixel rectangle from an image with sub-pixel accuracy.</p>
 *
 * <p>The function <code>getRectSubPix</code> extracts pixels from <code>src</code></p>
 *
 * <p><em>dst(x, y) = src(x + center.x - (dst.cols -1)*0.5, y + center.y -
 * (dst.rows -1)*0.5)</em></p>
 *
 * <p>where the values of the pixels at non-integer coordinates are retrieved using
 * bilinear interpolation. Every channel of multi-channel images is processed
 * independently. While the center of the rectangle must be inside the image,
 * parts of the rectangle may be outside. In this case, the replication border
 * mode (see "borderInterpolate") is used to extrapolate the pixel values
 * outside of the image.</p>
 *
 * @param image a image
 * @param patchSize Size of the extracted patch.
 * @param center Floating point coordinates of the center of the extracted
 * rectangle within the source image. The center must be inside the image.
 * @param patch a patch
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getrectsubpix">org.opencv.imgproc.Imgproc.getRectSubPix</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 */
    public static void getRectSubPix(Mat image, Size patchSize, Point center, Mat patch)
    {

        getRectSubPix_1(image.nativeObj, patchSize.width, patchSize.height, center.x, center.y, patch.nativeObj);

        return;
    }

    //
    // C++:  Mat getRotationMatrix2D(Point2f center, double angle, double scale)
    //

/**
 * <p>Calculates an affine matrix of 2D rotation.</p>
 *
 * <p>The function calculates the following matrix:</p>
 *
 * <p><em>alpha beta(1- alpha) * center.x - beta * center.y
 * - beta alpha beta * center.x + (1- alpha) * center.y </em></p>
 *
 * <p>where</p>
 *
 * <p><em>alpha = scale * cos angle,
 * beta = scale * sin angle </em></p>
 *
 * <p>The transformation maps the rotation center to itself. If this is not the
 * target, adjust the shift.</p>
 *
 * @param center Center of the rotation in the source image.
 * @param angle Rotation angle in degrees. Positive values mean
 * counter-clockwise rotation (the coordinate origin is assumed to be the
 * top-left corner).
 * @param scale Isotropic scale factor.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#getrotationmatrix2d">org.opencv.imgproc.Imgproc.getRotationMatrix2D</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#getAffineTransform
 * @see org.opencv.core.Core#transform
 */
    public static Mat getRotationMatrix2D(Point center, double angle, double scale)
    {

        Mat retVal = new Mat(getRotationMatrix2D_0(center.x, center.y, angle, scale));

        return retVal;
    }

    //
    // C++:  Mat getStructuringElement(int shape, Size ksize, Point anchor = Point(-1,-1))
    //

/**
 * <p>Returns a structuring element of the specified size and shape for
 * morphological operations.</p>
 *
 * <p>The function constructs and returns the structuring element that can be
 * further passed to "createMorphologyFilter", "erode", "dilate" or
 * "morphologyEx". But you can also construct an arbitrary binary mask yourself
 * and use it as the structuring element.</p>
 *
 * <p>Note: When using OpenCV 1.x C API, the created structuring element
 * <code>IplConvKernel* element</code> must be released in the end using
 * <code>cvReleaseStructuringElement(&element)</code>.</p>
 *
 * @param shape Element shape that could be one of the following:
 * <ul>
 *   <li> MORPH_RECT - a rectangular structuring element:
 * </ul>
 *
 * <p><em>E_(ij)=1</em></p>
 *
 * <ul>
 *   <li> MORPH_ELLIPSE - an elliptic structuring element, that is, a filled
 * ellipse inscribed into the rectangle <code>Rect(0, 0, esize.width,
 * 0.esize.height)</code>
 *   <li> MORPH_CROSS - a cross-shaped structuring element:
 * </ul>
 *
 * <p><em>E_(ij) = 1 if i=anchor.y or j=anchor.x; 0 otherwise</em></p>
 *
 * <ul>
 *   <li> CV_SHAPE_CUSTOM - custom structuring element (OpenCV 1.x API)
 * </ul>
 * @param ksize Size of the structuring element.
 * @param anchor Anchor position within the element. The default value <em>(-1,
 * -1)</em> means that the anchor is at the center. Note that only the shape of
 * a cross-shaped element depends on the anchor position. In other cases the
 * anchor just regulates how much the result of the morphological operation is
 * shifted.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#getstructuringelement">org.opencv.imgproc.Imgproc.getStructuringElement</a>
 */
    public static Mat getStructuringElement(int shape, Size ksize, Point anchor)
    {

        Mat retVal = new Mat(getStructuringElement_0(shape, ksize.width, ksize.height, anchor.x, anchor.y));

        return retVal;
    }

/**
 * <p>Returns a structuring element of the specified size and shape for
 * morphological operations.</p>
 *
 * <p>The function constructs and returns the structuring element that can be
 * further passed to "createMorphologyFilter", "erode", "dilate" or
 * "morphologyEx". But you can also construct an arbitrary binary mask yourself
 * and use it as the structuring element.</p>
 *
 * <p>Note: When using OpenCV 1.x C API, the created structuring element
 * <code>IplConvKernel* element</code> must be released in the end using
 * <code>cvReleaseStructuringElement(&element)</code>.</p>
 *
 * @param shape Element shape that could be one of the following:
 * <ul>
 *   <li> MORPH_RECT - a rectangular structuring element:
 * </ul>
 *
 * <p><em>E_(ij)=1</em></p>
 *
 * <ul>
 *   <li> MORPH_ELLIPSE - an elliptic structuring element, that is, a filled
 * ellipse inscribed into the rectangle <code>Rect(0, 0, esize.width,
 * 0.esize.height)</code>
 *   <li> MORPH_CROSS - a cross-shaped structuring element:
 * </ul>
 *
 * <p><em>E_(ij) = 1 if i=anchor.y or j=anchor.x; 0 otherwise</em></p>
 *
 * <ul>
 *   <li> CV_SHAPE_CUSTOM - custom structuring element (OpenCV 1.x API)
 * </ul>
 * @param ksize Size of the structuring element.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#getstructuringelement">org.opencv.imgproc.Imgproc.getStructuringElement</a>
 */
    public static Mat getStructuringElement(int shape, Size ksize)
    {

        Mat retVal = new Mat(getStructuringElement_1(shape, ksize.width, ksize.height));

        return retVal;
    }

    //
    // C++:  void goodFeaturesToTrack(Mat image, vector_Point& corners, int maxCorners, double qualityLevel, double minDistance, Mat mask = Mat(), int blockSize = 3, bool useHarrisDetector = false, double k = 0.04)
    //

/**
 * <p>Determines strong corners on an image.</p>
 *
 * <p>The function finds the most prominent corners in the image or in the
 * specified image region, as described in [Shi94]:</p>
 * <ul>
 *   <li> Function calculates the corner quality measure at every source image
 * pixel using the "cornerMinEigenVal" or "cornerHarris".
 *   <li> Function performs a non-maximum suppression (the local maximums in *3
 * x 3* neighborhood are retained).
 *   <li> The corners with the minimal eigenvalue less than <em>qualityLevel *
 * max_(x,y) qualityMeasureMap(x,y)</em> are rejected.
 *   <li> The remaining corners are sorted by the quality measure in the
 * descending order.
 *   <li> Function throws away each corner for which there is a stronger corner
 * at a distance less than <code>maxDistance</code>.
 * </ul>
 *
 * <p>The function can be used to initialize a point-based tracker of an object.</p>
 *
 * <p>Note: If the function is called with different values <code>A</code> and
 * <code>B</code> of the parameter <code>qualityLevel</code>, and <code>A</code>
 * > {B}, the vector of returned corners with <code>qualityLevel=A</code> will
 * be the prefix of the output vector with <code>qualityLevel=B</code>.</p>
 *
 * @param image Input 8-bit or floating-point 32-bit, single-channel image.
 * @param corners Output vector of detected corners.
 * @param maxCorners Maximum number of corners to return. If there are more
 * corners than are found, the strongest of them is returned.
 * @param qualityLevel Parameter characterizing the minimal accepted quality of
 * image corners. The parameter value is multiplied by the best corner quality
 * measure, which is the minimal eigenvalue (see "cornerMinEigenVal") or the
 * Harris function response (see "cornerHarris"). The corners with the quality
 * measure less than the product are rejected. For example, if the best corner
 * has the quality measure = 1500, and the <code>qualityLevel=0.01</code>, then
 * all the corners with the quality measure less than 15 are rejected.
 * @param minDistance Minimum possible Euclidean distance between the returned
 * corners.
 * @param mask Optional region of interest. If the image is not empty (it needs
 * to have the type <code>CV_8UC1</code> and the same size as <code>image</code>),
 * it specifies the region in which the corners are detected.
 * @param blockSize Size of an average block for computing a derivative
 * covariation matrix over each pixel neighborhood. See "cornerEigenValsAndVecs".
 * @param useHarrisDetector Parameter indicating whether to use a Harris
 * detector (see "cornerHarris") or "cornerMinEigenVal".
 * @param k Free parameter of the Harris detector.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#goodfeaturestotrack">org.opencv.imgproc.Imgproc.goodFeaturesToTrack</a>
 * @see org.opencv.imgproc.Imgproc#cornerHarris
 * @see org.opencv.imgproc.Imgproc#cornerMinEigenVal
 */
    public static void goodFeaturesToTrack(Mat image, MatOfPoint corners, int maxCorners, double qualityLevel, double minDistance, Mat mask, int blockSize, boolean useHarrisDetector, double k)
    {
        Mat corners_mat = corners;
        goodFeaturesToTrack_0(image.nativeObj, corners_mat.nativeObj, maxCorners, qualityLevel, minDistance, mask.nativeObj, blockSize, useHarrisDetector, k);

        return;
    }

/**
 * <p>Determines strong corners on an image.</p>
 *
 * <p>The function finds the most prominent corners in the image or in the
 * specified image region, as described in [Shi94]:</p>
 * <ul>
 *   <li> Function calculates the corner quality measure at every source image
 * pixel using the "cornerMinEigenVal" or "cornerHarris".
 *   <li> Function performs a non-maximum suppression (the local maximums in *3
 * x 3* neighborhood are retained).
 *   <li> The corners with the minimal eigenvalue less than <em>qualityLevel *
 * max_(x,y) qualityMeasureMap(x,y)</em> are rejected.
 *   <li> The remaining corners are sorted by the quality measure in the
 * descending order.
 *   <li> Function throws away each corner for which there is a stronger corner
 * at a distance less than <code>maxDistance</code>.
 * </ul>
 *
 * <p>The function can be used to initialize a point-based tracker of an object.</p>
 *
 * <p>Note: If the function is called with different values <code>A</code> and
 * <code>B</code> of the parameter <code>qualityLevel</code>, and <code>A</code>
 * > {B}, the vector of returned corners with <code>qualityLevel=A</code> will
 * be the prefix of the output vector with <code>qualityLevel=B</code>.</p>
 *
 * @param image Input 8-bit or floating-point 32-bit, single-channel image.
 * @param corners Output vector of detected corners.
 * @param maxCorners Maximum number of corners to return. If there are more
 * corners than are found, the strongest of them is returned.
 * @param qualityLevel Parameter characterizing the minimal accepted quality of
 * image corners. The parameter value is multiplied by the best corner quality
 * measure, which is the minimal eigenvalue (see "cornerMinEigenVal") or the
 * Harris function response (see "cornerHarris"). The corners with the quality
 * measure less than the product are rejected. For example, if the best corner
 * has the quality measure = 1500, and the <code>qualityLevel=0.01</code>, then
 * all the corners with the quality measure less than 15 are rejected.
 * @param minDistance Minimum possible Euclidean distance between the returned
 * corners.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#goodfeaturestotrack">org.opencv.imgproc.Imgproc.goodFeaturesToTrack</a>
 * @see org.opencv.imgproc.Imgproc#cornerHarris
 * @see org.opencv.imgproc.Imgproc#cornerMinEigenVal
 */
    public static void goodFeaturesToTrack(Mat image, MatOfPoint corners, int maxCorners, double qualityLevel, double minDistance)
    {
        Mat corners_mat = corners;
        goodFeaturesToTrack_1(image.nativeObj, corners_mat.nativeObj, maxCorners, qualityLevel, minDistance);

        return;
    }

    //
    // C++:  void grabCut(Mat img, Mat& mask, Rect rect, Mat& bgdModel, Mat& fgdModel, int iterCount, int mode = GC_EVAL)
    //

/**
 * <p>Runs the GrabCut algorithm.</p>
 *
 * <p>The function implements the GrabCut image segmentation algorithm
 * (http://en.wikipedia.org/wiki/GrabCut).
 * See the sample <code>grabcut.cpp</code> to learn how to use the function.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the GrabCut algorithm can be found at
 * opencv_source_code/samples/cpp/grabcut.cpp
 *   <li> (Python) An example using the GrabCut algorithm can be found at
 * opencv_source_code/samples/python2/grabcut.py
 * </ul>
 *
 * @param img Input 8-bit 3-channel image.
 * @param mask Input/output 8-bit single-channel mask. The mask is initialized
 * by the function when <code>mode</code> is set to <code>GC_INIT_WITH_RECT</code>.
 * Its elements may have one of following values:
 * <ul>
 *   <li> GC_BGD defines an obvious background pixels.
 *   <li> GC_FGD defines an obvious foreground (object) pixel.
 *   <li> GC_PR_BGD defines a possible background pixel.
 *   <li> GC_PR_FGD defines a possible foreground pixel.
 * </ul>
 * @param rect ROI containing a segmented object. The pixels outside of the ROI
 * are marked as "obvious background". The parameter is only used when
 * <code>mode==GC_INIT_WITH_RECT</code>.
 * @param bgdModel Temporary array for the background model. Do not modify it
 * while you are processing the same image.
 * @param fgdModel Temporary arrays for the foreground model. Do not modify it
 * while you are processing the same image.
 * @param iterCount Number of iterations the algorithm should make before
 * returning the result. Note that the result can be refined with further calls
 * with <code>mode==GC_INIT_WITH_MASK</code> or <code>mode==GC_EVAL</code>.
 * @param mode Operation mode that could be one of the following:
 * <ul>
 *   <li> GC_INIT_WITH_RECT The function initializes the state and the mask
 * using the provided rectangle. After that it runs <code>iterCount</code>
 * iterations of the algorithm.
 *   <li> GC_INIT_WITH_MASK The function initializes the state using the
 * provided mask. Note that <code>GC_INIT_WITH_RECT</code> and <code>GC_INIT_WITH_MASK</code>
 * can be combined. Then, all the pixels outside of the ROI are automatically
 * initialized with <code>GC_BGD</code>.
 *   <li> GC_EVAL The value means that the algorithm should just resume.
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#grabcut">org.opencv.imgproc.Imgproc.grabCut</a>
 */
    public static void grabCut(Mat img, Mat mask, Rect rect, Mat bgdModel, Mat fgdModel, int iterCount, int mode)
    {

        grabCut_0(img.nativeObj, mask.nativeObj, rect.x, rect.y, rect.width, rect.height, bgdModel.nativeObj, fgdModel.nativeObj, iterCount, mode);

        return;
    }

/**
 * <p>Runs the GrabCut algorithm.</p>
 *
 * <p>The function implements the GrabCut image segmentation algorithm
 * (http://en.wikipedia.org/wiki/GrabCut).
 * See the sample <code>grabcut.cpp</code> to learn how to use the function.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the GrabCut algorithm can be found at
 * opencv_source_code/samples/cpp/grabcut.cpp
 *   <li> (Python) An example using the GrabCut algorithm can be found at
 * opencv_source_code/samples/python2/grabcut.py
 * </ul>
 *
 * @param img Input 8-bit 3-channel image.
 * @param mask Input/output 8-bit single-channel mask. The mask is initialized
 * by the function when <code>mode</code> is set to <code>GC_INIT_WITH_RECT</code>.
 * Its elements may have one of following values:
 * <ul>
 *   <li> GC_BGD defines an obvious background pixels.
 *   <li> GC_FGD defines an obvious foreground (object) pixel.
 *   <li> GC_PR_BGD defines a possible background pixel.
 *   <li> GC_PR_FGD defines a possible foreground pixel.
 * </ul>
 * @param rect ROI containing a segmented object. The pixels outside of the ROI
 * are marked as "obvious background". The parameter is only used when
 * <code>mode==GC_INIT_WITH_RECT</code>.
 * @param bgdModel Temporary array for the background model. Do not modify it
 * while you are processing the same image.
 * @param fgdModel Temporary arrays for the foreground model. Do not modify it
 * while you are processing the same image.
 * @param iterCount Number of iterations the algorithm should make before
 * returning the result. Note that the result can be refined with further calls
 * with <code>mode==GC_INIT_WITH_MASK</code> or <code>mode==GC_EVAL</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#grabcut">org.opencv.imgproc.Imgproc.grabCut</a>
 */
    public static void grabCut(Mat img, Mat mask, Rect rect, Mat bgdModel, Mat fgdModel, int iterCount)
    {

        grabCut_1(img.nativeObj, mask.nativeObj, rect.x, rect.y, rect.width, rect.height, bgdModel.nativeObj, fgdModel.nativeObj, iterCount);

        return;
    }

    //
    // C++:  void initUndistortRectifyMap(Mat cameraMatrix, Mat distCoeffs, Mat R, Mat newCameraMatrix, Size size, int m1type, Mat& map1, Mat& map2)
    //

/**
 * <p>Computes the undistortion and rectification transformation map.</p>
 *
 * <p>The function computes the joint undistortion and rectification transformation
 * and represents the result in the form of maps for "remap". The undistorted
 * image looks like original, as if it is captured with a camera using the
 * camera matrix <code>=newCameraMatrix</code> and zero distortion. In case of a
 * monocular camera, <code>newCameraMatrix</code> is usually equal to
 * <code>cameraMatrix</code>, or it can be computed by "getOptimalNewCameraMatrix"
 * for a better control over scaling. In case of a stereo camera,
 * <code>newCameraMatrix</code> is normally set to <code>P1</code> or
 * <code>P2</code> computed by "stereoRectify".</p>
 *
 * <p>Also, this new camera is oriented differently in the coordinate space,
 * according to <code>R</code>. That, for example, helps to align two heads of a
 * stereo camera so that the epipolar lines on both images become horizontal and
 * have the same y- coordinate (in case of a horizontally aligned stereo
 * camera).</p>
 *
 * <p>The function actually builds the maps for the inverse mapping algorithm that
 * is used by "remap". That is, for each pixel <em>(u, v)</em> in the
 * destination (corrected and rectified) image, the function computes the
 * corresponding coordinates in the source image (that is, in the original image
 * from camera). The following process is applied:</p>
 *
 * <p><em>x <- (u - (c')_x)/(f')_x
 * y <- (v - (c')_y)/(f')_y
 * ([X Y W]) ^T <- R^(-1)*[x y 1]^T
 * x' <- X/W
 * y' <- Y/W
 * x" <- x' (1 + k_1 r^2 + k_2 r^4 + k_3 r^6) + 2p_1 x' y' + p_2(r^2 + 2 x'^2)
 * y" <- y' (1 + k_1 r^2 + k_2 r^4 + k_3 r^6) + p_1(r^2 + 2 y'^2) + 2 p_2 x' y'
 * map_x(u,v) <- x" f_x + c_x
 * map_y(u,v) <- y" f_y + c_y </em></p>
 *
 * <p>where <em>(k_1, k_2, p_1, p_2[, k_3])</em> are the distortion coefficients.</p>
 *
 * <p>In case of a stereo camera, this function is called twice: once for each
 * camera head, after "stereoRectify", which in its turn is called after
 * "stereoCalibrate". But if the stereo camera was not calibrated, it is still
 * possible to compute the rectification transformations directly from the
 * fundamental matrix using "stereoRectifyUncalibrated". For each camera, the
 * function computes homography <code>H</code> as the rectification
 * transformation in a pixel domain, not a rotation matrix <code>R</code> in 3D
 * space. <code>R</code> can be computed from <code>H</code> as</p>
 *
 * <p><em>R = cameraMatrix ^(-1) * H * cameraMatrix</em></p>
 *
 * <p>where <code>cameraMatrix</code> can be chosen arbitrarily.</p>
 *
 * @param cameraMatrix Input camera matrix <em>A=
 * <p>|f_x 0 c_x|
 * |0 f_y c_y|
 * |0 0 1|
 * </em>.</p>
 * @param distCoeffs Input vector of distortion coefficients <em>(k_1, k_2, p_1,
 * p_2[, k_3[, k_4, k_5, k_6]])</em> of 4, 5, or 8 elements. If the vector is
 * NULL/empty, the zero distortion coefficients are assumed.
 * @param R Optional rectification transformation in the object space (3x3
 * matrix). <code>R1</code> or <code>R2</code>, computed by "stereoRectify" can
 * be passed here. If the matrix is empty, the identity transformation is
 * assumed. In <code>cvInitUndistortMap</code> R assumed to be an identity
 * matrix.
 * @param newCameraMatrix New camera matrix <em>A'=
 * <p>|f_x' 0 c_x'|
 * |0 f_y' c_y'|
 * |0 0 1|
 * </em>.</p>
 * @param size Undistorted image size.
 * @param m1type Type of the first output map that can be <code>CV_32FC1</code>
 * or <code>CV_16SC2</code>. See "convertMaps" for details.
 * @param map1 The first output map.
 * @param map2 The second output map.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#initundistortrectifymap">org.opencv.imgproc.Imgproc.initUndistortRectifyMap</a>
 */
    public static void initUndistortRectifyMap(Mat cameraMatrix, Mat distCoeffs, Mat R, Mat newCameraMatrix, Size size, int m1type, Mat map1, Mat map2)
    {

        initUndistortRectifyMap_0(cameraMatrix.nativeObj, distCoeffs.nativeObj, R.nativeObj, newCameraMatrix.nativeObj, size.width, size.height, m1type, map1.nativeObj, map2.nativeObj);

        return;
    }

    //
    // C++:  float initWideAngleProjMap(Mat cameraMatrix, Mat distCoeffs, Size imageSize, int destImageWidth, int m1type, Mat& map1, Mat& map2, int projType = PROJ_SPHERICAL_EQRECT, double alpha = 0)
    //

    public static float initWideAngleProjMap(Mat cameraMatrix, Mat distCoeffs, Size imageSize, int destImageWidth, int m1type, Mat map1, Mat map2, int projType, double alpha)
    {

        float retVal = initWideAngleProjMap_0(cameraMatrix.nativeObj, distCoeffs.nativeObj, imageSize.width, imageSize.height, destImageWidth, m1type, map1.nativeObj, map2.nativeObj, projType, alpha);

        return retVal;
    }

    public static float initWideAngleProjMap(Mat cameraMatrix, Mat distCoeffs, Size imageSize, int destImageWidth, int m1type, Mat map1, Mat map2)
    {

        float retVal = initWideAngleProjMap_1(cameraMatrix.nativeObj, distCoeffs.nativeObj, imageSize.width, imageSize.height, destImageWidth, m1type, map1.nativeObj, map2.nativeObj);

        return retVal;
    }

    //
    // C++:  void integral(Mat src, Mat& sum, int sdepth = -1)
    //

/**
 * <p>Calculates the integral of an image.</p>
 *
 * <p>The functions calculate one or more integral images for the source image as
 * follows:</p>
 *
 * <p><em>sum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)</em></p>
 *
 *
 *
 * <p><em>sqsum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)^2</em></p>
 *
 *
 *
 * <p><em>tilted(X,Y) = sum(by: y&ltY,abs(x-X+1) <= Y-y-1) image(x,y)</em></p>
 *
 * <p>Using these integral images, you can calculate sa um, mean, and standard
 * deviation over a specific up-right or rotated rectangular region of the image
 * in a constant time, for example:</p>
 *
 * <p><em>sum(by: x_1 <= x &lt x_2, y_1 <= y &lt y_2) image(x,y) = sum(x_2,y_2)-
 * sum(x_1,y_2)- sum(x_2,y_1)+ sum(x_1,y_1)</em></p>
 *
 * <p>It makes possible to do a fast blurring or fast block correlation with a
 * variable window size, for example. In case of multi-channel images, sums for
 * each channel are accumulated independently.</p>
 *
 * <p>As a practical example, the next figure shows the calculation of the integral
 * of a straight rectangle <code>Rect(3,3,3,2)</code> and of a tilted rectangle
 * <code>Rect(5,1,2,3)</code>. The selected pixels in the original
 * <code>image</code> are shown, as well as the relative pixels in the integral
 * images <code>sum</code> and <code>tilted</code>.</p>
 *
 * @param src a src
 * @param sum integral image as <em>(W+1)x(H+1)</em>, 32-bit integer or
 * floating-point (32f or 64f).
 * @param sdepth desired depth of the integral and the tilted integral images,
 * <code>CV_32S</code>, <code>CV_32F</code>, or <code>CV_64F</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#integral">org.opencv.imgproc.Imgproc.integral</a>
 */
    public static void integral(Mat src, Mat sum, int sdepth)
    {

        integral_0(src.nativeObj, sum.nativeObj, sdepth);

        return;
    }

/**
 * <p>Calculates the integral of an image.</p>
 *
 * <p>The functions calculate one or more integral images for the source image as
 * follows:</p>
 *
 * <p><em>sum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)</em></p>
 *
 *
 *
 * <p><em>sqsum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)^2</em></p>
 *
 *
 *
 * <p><em>tilted(X,Y) = sum(by: y&ltY,abs(x-X+1) <= Y-y-1) image(x,y)</em></p>
 *
 * <p>Using these integral images, you can calculate sa um, mean, and standard
 * deviation over a specific up-right or rotated rectangular region of the image
 * in a constant time, for example:</p>
 *
 * <p><em>sum(by: x_1 <= x &lt x_2, y_1 <= y &lt y_2) image(x,y) = sum(x_2,y_2)-
 * sum(x_1,y_2)- sum(x_2,y_1)+ sum(x_1,y_1)</em></p>
 *
 * <p>It makes possible to do a fast blurring or fast block correlation with a
 * variable window size, for example. In case of multi-channel images, sums for
 * each channel are accumulated independently.</p>
 *
 * <p>As a practical example, the next figure shows the calculation of the integral
 * of a straight rectangle <code>Rect(3,3,3,2)</code> and of a tilted rectangle
 * <code>Rect(5,1,2,3)</code>. The selected pixels in the original
 * <code>image</code> are shown, as well as the relative pixels in the integral
 * images <code>sum</code> and <code>tilted</code>.</p>
 *
 * @param src a src
 * @param sum integral image as <em>(W+1)x(H+1)</em>, 32-bit integer or
 * floating-point (32f or 64f).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#integral">org.opencv.imgproc.Imgproc.integral</a>
 */
    public static void integral(Mat src, Mat sum)
    {

        integral_1(src.nativeObj, sum.nativeObj);

        return;
    }

    //
    // C++:  void integral(Mat src, Mat& sum, Mat& sqsum, int sdepth = -1)
    //

/**
 * <p>Calculates the integral of an image.</p>
 *
 * <p>The functions calculate one or more integral images for the source image as
 * follows:</p>
 *
 * <p><em>sum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)</em></p>
 *
 *
 *
 * <p><em>sqsum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)^2</em></p>
 *
 *
 *
 * <p><em>tilted(X,Y) = sum(by: y&ltY,abs(x-X+1) <= Y-y-1) image(x,y)</em></p>
 *
 * <p>Using these integral images, you can calculate sa um, mean, and standard
 * deviation over a specific up-right or rotated rectangular region of the image
 * in a constant time, for example:</p>
 *
 * <p><em>sum(by: x_1 <= x &lt x_2, y_1 <= y &lt y_2) image(x,y) = sum(x_2,y_2)-
 * sum(x_1,y_2)- sum(x_2,y_1)+ sum(x_1,y_1)</em></p>
 *
 * <p>It makes possible to do a fast blurring or fast block correlation with a
 * variable window size, for example. In case of multi-channel images, sums for
 * each channel are accumulated independently.</p>
 *
 * <p>As a practical example, the next figure shows the calculation of the integral
 * of a straight rectangle <code>Rect(3,3,3,2)</code> and of a tilted rectangle
 * <code>Rect(5,1,2,3)</code>. The selected pixels in the original
 * <code>image</code> are shown, as well as the relative pixels in the integral
 * images <code>sum</code> and <code>tilted</code>.</p>
 *
 * @param src a src
 * @param sum integral image as <em>(W+1)x(H+1)</em>, 32-bit integer or
 * floating-point (32f or 64f).
 * @param sqsum integral image for squared pixel values; it is <em>(W+1)x(H+1)</em>,
 * double-precision floating-point (64f) array.
 * @param sdepth desired depth of the integral and the tilted integral images,
 * <code>CV_32S</code>, <code>CV_32F</code>, or <code>CV_64F</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#integral">org.opencv.imgproc.Imgproc.integral</a>
 */
    public static void integral2(Mat src, Mat sum, Mat sqsum, int sdepth)
    {

        integral2_0(src.nativeObj, sum.nativeObj, sqsum.nativeObj, sdepth);

        return;
    }

/**
 * <p>Calculates the integral of an image.</p>
 *
 * <p>The functions calculate one or more integral images for the source image as
 * follows:</p>
 *
 * <p><em>sum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)</em></p>
 *
 *
 *
 * <p><em>sqsum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)^2</em></p>
 *
 *
 *
 * <p><em>tilted(X,Y) = sum(by: y&ltY,abs(x-X+1) <= Y-y-1) image(x,y)</em></p>
 *
 * <p>Using these integral images, you can calculate sa um, mean, and standard
 * deviation over a specific up-right or rotated rectangular region of the image
 * in a constant time, for example:</p>
 *
 * <p><em>sum(by: x_1 <= x &lt x_2, y_1 <= y &lt y_2) image(x,y) = sum(x_2,y_2)-
 * sum(x_1,y_2)- sum(x_2,y_1)+ sum(x_1,y_1)</em></p>
 *
 * <p>It makes possible to do a fast blurring or fast block correlation with a
 * variable window size, for example. In case of multi-channel images, sums for
 * each channel are accumulated independently.</p>
 *
 * <p>As a practical example, the next figure shows the calculation of the integral
 * of a straight rectangle <code>Rect(3,3,3,2)</code> and of a tilted rectangle
 * <code>Rect(5,1,2,3)</code>. The selected pixels in the original
 * <code>image</code> are shown, as well as the relative pixels in the integral
 * images <code>sum</code> and <code>tilted</code>.</p>
 *
 * @param src a src
 * @param sum integral image as <em>(W+1)x(H+1)</em>, 32-bit integer or
 * floating-point (32f or 64f).
 * @param sqsum integral image for squared pixel values; it is <em>(W+1)x(H+1)</em>,
 * double-precision floating-point (64f) array.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#integral">org.opencv.imgproc.Imgproc.integral</a>
 */
    public static void integral2(Mat src, Mat sum, Mat sqsum)
    {

        integral2_1(src.nativeObj, sum.nativeObj, sqsum.nativeObj);

        return;
    }

    //
    // C++:  void integral(Mat src, Mat& sum, Mat& sqsum, Mat& tilted, int sdepth = -1)
    //

/**
 * <p>Calculates the integral of an image.</p>
 *
 * <p>The functions calculate one or more integral images for the source image as
 * follows:</p>
 *
 * <p><em>sum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)</em></p>
 *
 *
 *
 * <p><em>sqsum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)^2</em></p>
 *
 *
 *
 * <p><em>tilted(X,Y) = sum(by: y&ltY,abs(x-X+1) <= Y-y-1) image(x,y)</em></p>
 *
 * <p>Using these integral images, you can calculate sa um, mean, and standard
 * deviation over a specific up-right or rotated rectangular region of the image
 * in a constant time, for example:</p>
 *
 * <p><em>sum(by: x_1 <= x &lt x_2, y_1 <= y &lt y_2) image(x,y) = sum(x_2,y_2)-
 * sum(x_1,y_2)- sum(x_2,y_1)+ sum(x_1,y_1)</em></p>
 *
 * <p>It makes possible to do a fast blurring or fast block correlation with a
 * variable window size, for example. In case of multi-channel images, sums for
 * each channel are accumulated independently.</p>
 *
 * <p>As a practical example, the next figure shows the calculation of the integral
 * of a straight rectangle <code>Rect(3,3,3,2)</code> and of a tilted rectangle
 * <code>Rect(5,1,2,3)</code>. The selected pixels in the original
 * <code>image</code> are shown, as well as the relative pixels in the integral
 * images <code>sum</code> and <code>tilted</code>.</p>
 *
 * @param src a src
 * @param sum integral image as <em>(W+1)x(H+1)</em>, 32-bit integer or
 * floating-point (32f or 64f).
 * @param sqsum integral image for squared pixel values; it is <em>(W+1)x(H+1)</em>,
 * double-precision floating-point (64f) array.
 * @param tilted integral for the image rotated by 45 degrees; it is
 * <em>(W+1)x(H+1)</em> array with the same data type as <code>sum</code>.
 * @param sdepth desired depth of the integral and the tilted integral images,
 * <code>CV_32S</code>, <code>CV_32F</code>, or <code>CV_64F</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#integral">org.opencv.imgproc.Imgproc.integral</a>
 */
    public static void integral3(Mat src, Mat sum, Mat sqsum, Mat tilted, int sdepth)
    {

        integral3_0(src.nativeObj, sum.nativeObj, sqsum.nativeObj, tilted.nativeObj, sdepth);

        return;
    }

/**
 * <p>Calculates the integral of an image.</p>
 *
 * <p>The functions calculate one or more integral images for the source image as
 * follows:</p>
 *
 * <p><em>sum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)</em></p>
 *
 *
 *
 * <p><em>sqsum(X,Y) = sum(by: x&ltX,y&ltY) image(x,y)^2</em></p>
 *
 *
 *
 * <p><em>tilted(X,Y) = sum(by: y&ltY,abs(x-X+1) <= Y-y-1) image(x,y)</em></p>
 *
 * <p>Using these integral images, you can calculate sa um, mean, and standard
 * deviation over a specific up-right or rotated rectangular region of the image
 * in a constant time, for example:</p>
 *
 * <p><em>sum(by: x_1 <= x &lt x_2, y_1 <= y &lt y_2) image(x,y) = sum(x_2,y_2)-
 * sum(x_1,y_2)- sum(x_2,y_1)+ sum(x_1,y_1)</em></p>
 *
 * <p>It makes possible to do a fast blurring or fast block correlation with a
 * variable window size, for example. In case of multi-channel images, sums for
 * each channel are accumulated independently.</p>
 *
 * <p>As a practical example, the next figure shows the calculation of the integral
 * of a straight rectangle <code>Rect(3,3,3,2)</code> and of a tilted rectangle
 * <code>Rect(5,1,2,3)</code>. The selected pixels in the original
 * <code>image</code> are shown, as well as the relative pixels in the integral
 * images <code>sum</code> and <code>tilted</code>.</p>
 *
 * @param src a src
 * @param sum integral image as <em>(W+1)x(H+1)</em>, 32-bit integer or
 * floating-point (32f or 64f).
 * @param sqsum integral image for squared pixel values; it is <em>(W+1)x(H+1)</em>,
 * double-precision floating-point (64f) array.
 * @param tilted integral for the image rotated by 45 degrees; it is
 * <em>(W+1)x(H+1)</em> array with the same data type as <code>sum</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#integral">org.opencv.imgproc.Imgproc.integral</a>
 */
    public static void integral3(Mat src, Mat sum, Mat sqsum, Mat tilted)
    {

        integral3_1(src.nativeObj, sum.nativeObj, sqsum.nativeObj, tilted.nativeObj);

        return;
    }

    //
    // C++:  float intersectConvexConvex(Mat _p1, Mat _p2, Mat& _p12, bool handleNested = true)
    //

    public static float intersectConvexConvex(Mat _p1, Mat _p2, Mat _p12, boolean handleNested)
    {

        float retVal = intersectConvexConvex_0(_p1.nativeObj, _p2.nativeObj, _p12.nativeObj, handleNested);

        return retVal;
    }

    public static float intersectConvexConvex(Mat _p1, Mat _p2, Mat _p12)
    {

        float retVal = intersectConvexConvex_1(_p1.nativeObj, _p2.nativeObj, _p12.nativeObj);

        return retVal;
    }

    //
    // C++:  void invertAffineTransform(Mat M, Mat& iM)
    //

/**
 * <p>Inverts an affine transformation.</p>
 *
 * <p>The function computes an inverse affine transformation represented by <em>2 x
 * 3</em> matrix <code>M</code> :</p>
 *
 * <p><em>a_11 a_12 b_1
 * a_21 a_22 b_2 </em></p>
 *
 * <p>The result is also a <em>2 x 3</em> matrix of the same type as
 * <code>M</code>.</p>
 *
 * @param M Original affine transformation.
 * @param iM Output reverse affine transformation.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#invertaffinetransform">org.opencv.imgproc.Imgproc.invertAffineTransform</a>
 */
    public static void invertAffineTransform(Mat M, Mat iM)
    {

        invertAffineTransform_0(M.nativeObj, iM.nativeObj);

        return;
    }

    //
    // C++:  bool isContourConvex(vector_Point contour)
    //

/**
 * <p>Tests a contour convexity.</p>
 *
 * <p>The function tests whether the input contour is convex or not. The contour
 * must be simple, that is, without self-intersections. Otherwise, the function
 * output is undefined.</p>
 *
 * @param contour Input vector of 2D points, stored in:
 * <ul>
 *   <li> <code>std.vector<></code> or <code>Mat</code> (C++ interface)
 *   <li> <code>CvSeq*</code> or <code>CvMat*</code> (C interface)
 *   <li> Nx2 numpy array (Python interface)
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#iscontourconvex">org.opencv.imgproc.Imgproc.isContourConvex</a>
 */
    public static boolean isContourConvex(MatOfPoint contour)
    {
        Mat contour_mat = contour;
        boolean retVal = isContourConvex_0(contour_mat.nativeObj);

        return retVal;
    }

    //
    // C++:  double matchShapes(Mat contour1, Mat contour2, int method, double parameter)
    //

/**
 * <p>Compares two shapes.</p>
 *
 * <p>The function compares two shapes. All three implemented methods use the Hu
 * invariants (see "HuMoments") as follows (<em>A</em> denotes <code>object1</code>,<em>B</em>
 * denotes <code>object2</code>):</p>
 * <ul>
 *   <li> method=CV_CONTOURS_MATCH_I1
 * </ul>
 *
 * <p><em>I_1(A,B) = sum(by: i=1...7) <= ft|1/(m^A_i) - 1/(m^B_i) right|</em></p>
 *
 * <ul>
 *   <li> method=CV_CONTOURS_MATCH_I2
 * </ul>
 *
 * <p><em>I_2(A,B) = sum(by: i=1...7) <= ft|m^A_i - m^B_i right|</em></p>
 *
 * <ul>
 *   <li> method=CV_CONTOURS_MATCH_I3
 * </ul>
 *
 * <p><em>I_3(A,B) = max _(i=1...7)(<= ft| m^A_i - m^B_i right|)/(<= ft| m^A_i
 * right|)</em></p>
 *
 * <p>where</p>
 *
 * <p><em>m^A_i = sign(h^A_i) * log(h^A_i)
 * m^B_i = sign(h^B_i) * log(h^B_i) </em></p>
 *
 * <p>and <em>h^A_i, h^B_i</em> are the Hu moments of <em>A</em> and <em>B</em>,
 * respectively.</p>
 *
 * @param contour1 a contour1
 * @param contour2 a contour2
 * @param method Comparison method: <code>CV_CONTOURS_MATCH_I1</code>,
 * <code>CV_CONTOURS_MATCH_I2</code> \
 * <p>or <code>CV_CONTOURS_MATCH_I3</code> (see the details below).</p>
 * @param parameter Method-specific parameter (not supported now).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#matchshapes">org.opencv.imgproc.Imgproc.matchShapes</a>
 */
    public static double matchShapes(Mat contour1, Mat contour2, int method, double parameter)
    {

        double retVal = matchShapes_0(contour1.nativeObj, contour2.nativeObj, method, parameter);

        return retVal;
    }

    //
    // C++:  void matchTemplate(Mat image, Mat templ, Mat& result, int method)
    //

/**
 * <p>Compares a template against overlapped image regions.</p>
 *
 * <p>The function slides through <code>image</code>, compares the overlapped
 * patches of size <em>w x h</em> against <code>templ</code> using the specified
 * method and stores the comparison results in <code>result</code>. Here are the
 * formulae for the available comparison methods (<em>I</em> denotes
 * <code>image</code>, <em>T</em> <code>template</code>, <em>R</em>
 * <code>result</code>). The summation is done over template and/or the image
 * patch: <em>x' = 0...w-1, y' = 0...h-1</em></p>
 * <ul>
 *   <li> method=CV_TM_SQDIFF
 * </ul>
 *
 * <p><em>R(x,y)= sum(by: x',y')(T(x',y')-I(x+x',y+y'))^2</em></p>
 *
 * <ul>
 *   <li> method=CV_TM_SQDIFF_NORMED
 * </ul>
 *
 * <p><em>R(x,y)= (sum_(x',y')(T(x',y')-I(x+x',y+y'))^2)/(sqrt(sum_(x',y')T(x',y')^2
 * * sum_(x',y') I(x+x',y+y')^2))</em></p>
 *
 * <ul>
 *   <li> method=CV_TM_CCORR
 * </ul>
 *
 * <p><em>R(x,y)= sum(by: x',y')(T(x',y') * I(x+x',y+y'))</em></p>
 *
 * <ul>
 *   <li> method=CV_TM_CCORR_NORMED
 * </ul>
 *
 * <p><em>R(x,y)= (sum_(x',y')(T(x',y') * I(x+x',y+y')))/(sqrt(sum_(x',y')T(x',y')^2
 * * sum_(x',y') I(x+x',y+y')^2))</em></p>
 *
 * <ul>
 *   <li> method=CV_TM_CCOEFF
 * </ul>
 *
 * <p><em>R(x,y)= sum(by: x',y')(T'(x',y') * I'(x+x',y+y'))</em></p>
 *
 * <p>where</p>
 *
 * <p><em>T'(x',y')=T(x',y') - 1/(w * h) * sum(by: x'',y'') T(x'',y'')
 * I'(x+x',y+y')=I(x+x',y+y') - 1/(w * h) * sum(by: x'',y'') I(x+x'',y+y'')
 * </em></p>
 *
 * <ul>
 *   <li> method=CV_TM_CCOEFF_NORMED
 * </ul>
 *
 * <p><em>R(x,y)= (sum_(x',y')(T'(x',y') * I'(x+x',y+y')))/(sqrt(sum_(x',y')T'(x',y')^2
 * * sum_(x',y') I'(x+x',y+y')^2))</em></p>
 *
 * <p>After the function finishes the comparison, the best matches can be found as
 * global minimums (when <code>CV_TM_SQDIFF</code> was used) or maximums (when
 * <code>CV_TM_CCORR</code> or <code>CV_TM_CCOEFF</code> was used) using the
 * "minMaxLoc" function. In case of a color image, template summation in the
 * numerator and each sum in the denominator is done over all of the channels
 * and separate mean values are used for each channel. That is, the function can
 * take a color template and a color image. The result will still be a
 * single-channel image, which is easier to analyze.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> (Python) An example on how to match mouse selected regions in an image
 * can be found at opencv_source_code/samples/python2/mouse_and_match.py
 * </ul>
 *
 * @param image Image where the search is running. It must be 8-bit or 32-bit
 * floating-point.
 * @param templ Searched template. It must be not greater than the source image
 * and have the same data type.
 * @param result Map of comparison results. It must be single-channel 32-bit
 * floating-point. If <code>image</code> is <em>W x H</em> and <code>templ</code>
 * is <em>w x h</em>, then <code>result</code> is <em>(W-w+1) x(H-h+1)</em>.
 * @param method Parameter specifying the comparison method (see below).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/object_detection.html#matchtemplate">org.opencv.imgproc.Imgproc.matchTemplate</a>
 */
    public static void matchTemplate(Mat image, Mat templ, Mat result, int method)
    {

        matchTemplate_0(image.nativeObj, templ.nativeObj, result.nativeObj, method);

        return;
    }

    //
    // C++:  void medianBlur(Mat src, Mat& dst, int ksize)
    //

/**
 * <p>Blurs an image using the median filter.</p>
 *
 * <p>The function smoothes an image using the median filter with the <em>ksize x
 * ksize</em> aperture. Each channel of a multi-channel image is processed
 * independently. In-place operation is supported.</p>
 *
 * @param src input 1-, 3-, or 4-channel image; when <code>ksize</code> is 3 or
 * 5, the image depth should be <code>CV_8U</code>, <code>CV_16U</code>, or
 * <code>CV_32F</code>, for larger aperture sizes, it can only be
 * <code>CV_8U</code>.
 * @param dst destination array of the same size and type as <code>src</code>.
 * @param ksize aperture linear size; it must be odd and greater than 1, for
 * example: 3, 5, 7...
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#medianblur">org.opencv.imgproc.Imgproc.medianBlur</a>
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#bilateralFilter
 * @see org.opencv.imgproc.Imgproc#blur
 */
    public static void medianBlur(Mat src, Mat dst, int ksize)
    {

        medianBlur_0(src.nativeObj, dst.nativeObj, ksize);

        return;
    }

    //
    // C++:  RotatedRect minAreaRect(vector_Point2f points)
    //

/**
 * <p>Finds a rotated rectangle of the minimum area enclosing the input 2D point
 * set.</p>
 *
 * <p>The function calculates and returns the minimum-area bounding rectangle
 * (possibly rotated) for a specified point set. See the OpenCV sample
 * <code>minarea.cpp</code>.
 * Developer should keep in mind that the returned rotatedRect can contain
 * negative indices when data is close the the containing Mat element boundary.</p>
 *
 * @param points Input vector of 2D points, stored in:
 * <ul>
 *   <li> <code>std.vector<></code> or <code>Mat</code> (C++ interface)
 *   <li> <code>CvSeq*</code> or <code>CvMat*</code> (C interface)
 *   <li> Nx2 numpy array (Python interface)
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#minarearect">org.opencv.imgproc.Imgproc.minAreaRect</a>
 */
    public static RotatedRect minAreaRect(MatOfPoint2f points)
    {
        Mat points_mat = points;
        RotatedRect retVal = new RotatedRect(minAreaRect_0(points_mat.nativeObj));

        return retVal;
    }

    //
    // C++:  void minEnclosingCircle(vector_Point2f points, Point2f& center, float& radius)
    //

/**
 * <p>Finds a circle of the minimum area enclosing a 2D point set.</p>
 *
 * <p>The function finds the minimal enclosing circle of a 2D point set using an
 * iterative algorithm. See the OpenCV sample <code>minarea.cpp</code>.</p>
 *
 * @param points Input vector of 2D points, stored in:
 * <ul>
 *   <li> <code>std.vector<></code> or <code>Mat</code> (C++ interface)
 *   <li> <code>CvSeq*</code> or <code>CvMat*</code> (C interface)
 *   <li> Nx2 numpy array (Python interface)
 * </ul>
 * @param center Output center of the circle.
 * @param radius Output radius of the circle.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#minenclosingcircle">org.opencv.imgproc.Imgproc.minEnclosingCircle</a>
 */
    public static void minEnclosingCircle(MatOfPoint2f points, Point center, float[] radius)
    {
        Mat points_mat = points;
        double[] center_out = new double[2];
        double[] radius_out = new double[1];
        minEnclosingCircle_0(points_mat.nativeObj, center_out, radius_out);
        if(center!=null){ center.x = center_out[0]; center.y = center_out[1]; }
        if(radius!=null) radius[0] = (float)radius_out[0];
        return;
    }

    //
    // C++:  Moments moments(Mat array, bool binaryImage = false)
    //

/**
 * <p>Calculates all of the moments up to the third order of a polygon or
 * rasterized shape.</p>
 *
 * <p>The function computes moments, up to the 3rd order, of a vector shape or a
 * rasterized shape. The results are returned in the structure <code>Moments</code>
 * defined as: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>class Moments</p>
 *
 *
 * <p>public:</p>
 *
 * <p>Moments();</p>
 *
 * <p>Moments(double m00, double m10, double m01, double m20, double m11,</p>
 *
 * <p>double m02, double m30, double m21, double m12, double m03);</p>
 *
 * <p>Moments(const CvMoments& moments);</p>
 *
 * <p>operator CvMoments() const;</p>
 *
 * <p>// spatial moments</p>
 *
 * <p>double m00, m10, m01, m20, m11, m02, m30, m21, m12, m03;</p>
 *
 * <p>// central moments</p>
 *
 * <p>double mu20, mu11, mu02, mu30, mu21, mu12, mu03;</p>
 *
 * <p>// central normalized moments</p>
 *
 * <p>double nu20, nu11, nu02, nu30, nu21, nu12, nu03;</p>
 *
 *
 * <p>In case of a raster image, the spatial moments <em>Moments.m_(ji)</em> are
 * computed as: </code></p>
 *
 * <p><em>m _(ji)= sum(by: x,y)(array(x,y) * x^j * y^i)</em></p>
 *
 * <p>The central moments <em>Moments.mu_(ji)</em> are computed as:</p>
 *
 * <p><em>mu _(ji)= sum(by: x,y)(array(x,y) * (x - x")^j * (y - y")^i)</em></p>
 *
 * <p>where <em>(x", y")</em> is the mass center:</p>
 *
 * <p><em>x" = (m_10)/(m_(00)), y" = (m_01)/(m_(00))</em></p>
 *
 * <p>The normalized central moments <em>Moments.nu_(ij)</em> are computed as:</p>
 *
 * <p><em>nu _(ji)= (mu_(ji))/(m_(00)^((i+j)/2+1)).</em></p>
 *
 * <p>Note:</p>
 *
 * <p><em>mu_00=m_00</em>, <em>nu_00=1</em> <em>nu_10=mu_10=mu_01=mu_10=0</em>,
 * hence the values are not stored.</p>
 *
 * <p>The moments of a contour are defined in the same way but computed using the
 * Green's formula (see http://en.wikipedia.org/wiki/Green_theorem). So, due to
 * a limited raster resolution, the moments computed for a contour are slightly
 * different from the moments computed for the same rasterized contour.</p>
 *
 * <p>Note:</p>
 *
 * <p>Since the contour moments are computed using Green formula, you may get
 * seemingly odd results for contours with self-intersections, e.g. a zero area
 * (<code>m00</code>) for butterfly-shaped contours.</p>
 *
 * @param array Raster image (single-channel, 8-bit or floating-point 2D array)
 * or an array (<em>1 x N</em> or <em>N x 1</em>) of 2D points (<code>Point</code>
 * or <code>Point2f</code>).
 * @param binaryImage If it is true, all non-zero image pixels are treated as
 * 1's. The parameter is used for images only.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#moments">org.opencv.imgproc.Imgproc.moments</a>
 * @see org.opencv.imgproc.Imgproc#contourArea
 * @see org.opencv.imgproc.Imgproc#arcLength
 */
    public static Moments moments(Mat array, boolean binaryImage)
    {

        Moments retVal = new Moments(moments_0(array.nativeObj, binaryImage));

        return retVal;
    }

/**
 * <p>Calculates all of the moments up to the third order of a polygon or
 * rasterized shape.</p>
 *
 * <p>The function computes moments, up to the 3rd order, of a vector shape or a
 * rasterized shape. The results are returned in the structure <code>Moments</code>
 * defined as: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>class Moments</p>
 *
 *
 * <p>public:</p>
 *
 * <p>Moments();</p>
 *
 * <p>Moments(double m00, double m10, double m01, double m20, double m11,</p>
 *
 * <p>double m02, double m30, double m21, double m12, double m03);</p>
 *
 * <p>Moments(const CvMoments& moments);</p>
 *
 * <p>operator CvMoments() const;</p>
 *
 * <p>// spatial moments</p>
 *
 * <p>double m00, m10, m01, m20, m11, m02, m30, m21, m12, m03;</p>
 *
 * <p>// central moments</p>
 *
 * <p>double mu20, mu11, mu02, mu30, mu21, mu12, mu03;</p>
 *
 * <p>// central normalized moments</p>
 *
 * <p>double nu20, nu11, nu02, nu30, nu21, nu12, nu03;</p>
 *
 *
 * <p>In case of a raster image, the spatial moments <em>Moments.m_(ji)</em> are
 * computed as: </code></p>
 *
 * <p><em>m _(ji)= sum(by: x,y)(array(x,y) * x^j * y^i)</em></p>
 *
 * <p>The central moments <em>Moments.mu_(ji)</em> are computed as:</p>
 *
 * <p><em>mu _(ji)= sum(by: x,y)(array(x,y) * (x - x")^j * (y - y")^i)</em></p>
 *
 * <p>where <em>(x", y")</em> is the mass center:</p>
 *
 * <p><em>x" = (m_10)/(m_(00)), y" = (m_01)/(m_(00))</em></p>
 *
 * <p>The normalized central moments <em>Moments.nu_(ij)</em> are computed as:</p>
 *
 * <p><em>nu _(ji)= (mu_(ji))/(m_(00)^((i+j)/2+1)).</em></p>
 *
 * <p>Note:</p>
 *
 * <p><em>mu_00=m_00</em>, <em>nu_00=1</em> <em>nu_10=mu_10=mu_01=mu_10=0</em>,
 * hence the values are not stored.</p>
 *
 * <p>The moments of a contour are defined in the same way but computed using the
 * Green's formula (see http://en.wikipedia.org/wiki/Green_theorem). So, due to
 * a limited raster resolution, the moments computed for a contour are slightly
 * different from the moments computed for the same rasterized contour.</p>
 *
 * <p>Note:</p>
 *
 * <p>Since the contour moments are computed using Green formula, you may get
 * seemingly odd results for contours with self-intersections, e.g. a zero area
 * (<code>m00</code>) for butterfly-shaped contours.</p>
 *
 * @param array Raster image (single-channel, 8-bit or floating-point 2D array)
 * or an array (<em>1 x N</em> or <em>N x 1</em>) of 2D points (<code>Point</code>
 * or <code>Point2f</code>).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#moments">org.opencv.imgproc.Imgproc.moments</a>
 * @see org.opencv.imgproc.Imgproc#contourArea
 * @see org.opencv.imgproc.Imgproc#arcLength
 */
    public static Moments moments(Mat array)
    {

        Moments retVal = new Moments(moments_1(array.nativeObj));

        return retVal;
    }

    //
    // C++:  void morphologyEx(Mat src, Mat& dst, int op, Mat kernel, Point anchor = Point(-1,-1), int iterations = 1, int borderType = BORDER_CONSTANT, Scalar borderValue = morphologyDefaultBorderValue())
    //

/**
 * <p>Performs advanced morphological transformations.</p>
 *
 * <p>The function can perform advanced morphological transformations using an
 * erosion and dilation as basic operations.</p>
 *
 * <p>Opening operation:</p>
 *
 * <p><em>dst = open(src, element)= dilate(erode(src, element))</em></p>
 *
 * <p>Closing operation:</p>
 *
 * <p><em>dst = close(src, element)= erode(dilate(src, element))</em></p>
 *
 * <p>Morphological gradient:</p>
 *
 * <p><em>dst = morph_grad(src, element)= dilate(src, element)- erode(src,
 * element)</em></p>
 *
 * <p>"Top hat":</p>
 *
 * <p><em>dst = tophat(src, element)= src - open(src, element)</em></p>
 *
 * <p>"Black hat":</p>
 *
 * <p><em>dst = blackhat(src, element)= close(src, element)- src</em></p>
 *
 * <p>Any of the operations can be done in-place. In case of multi-channel images,
 * each channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphologyEx function for the morphological
 * opening and closing operations can be found at opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src Source image. The number of channels can be arbitrary. The depth
 * should be one of <code>CV_8U</code>, <code>CV_16U</code>, <code>CV_16S</code>,
 * <code>CV_32F" or </code>CV_64F".
 * @param dst Destination image of the same size and type as <code>src</code>.
 * @param op Type of a morphological operation that can be one of the following:
 * <ul>
 *   <li> MORPH_OPEN - an opening operation
 *   <li> MORPH_CLOSE - a closing operation
 *   <li> MORPH_GRADIENT - a morphological gradient
 *   <li> MORPH_TOPHAT - "top hat"
 *   <li> MORPH_BLACKHAT - "black hat"
 * </ul>
 * @param kernel a kernel
 * @param anchor a anchor
 * @param iterations Number of times erosion and dilation are applied.
 * @param borderType Pixel extrapolation method. See "borderInterpolate" for
 * details.
 * @param borderValue Border value in case of a constant border. The default
 * value has a special meaning. See "createMorphologyFilter" for details.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#morphologyex">org.opencv.imgproc.Imgproc.morphologyEx</a>
 * @see org.opencv.imgproc.Imgproc#erode
 * @see org.opencv.imgproc.Imgproc#dilate
 */
    public static void morphologyEx(Mat src, Mat dst, int op, Mat kernel, Point anchor, int iterations, int borderType, Scalar borderValue)
    {

        morphologyEx_0(src.nativeObj, dst.nativeObj, op, kernel.nativeObj, anchor.x, anchor.y, iterations, borderType, borderValue.val[0], borderValue.val[1], borderValue.val[2], borderValue.val[3]);

        return;
    }

/**
 * <p>Performs advanced morphological transformations.</p>
 *
 * <p>The function can perform advanced morphological transformations using an
 * erosion and dilation as basic operations.</p>
 *
 * <p>Opening operation:</p>
 *
 * <p><em>dst = open(src, element)= dilate(erode(src, element))</em></p>
 *
 * <p>Closing operation:</p>
 *
 * <p><em>dst = close(src, element)= erode(dilate(src, element))</em></p>
 *
 * <p>Morphological gradient:</p>
 *
 * <p><em>dst = morph_grad(src, element)= dilate(src, element)- erode(src,
 * element)</em></p>
 *
 * <p>"Top hat":</p>
 *
 * <p><em>dst = tophat(src, element)= src - open(src, element)</em></p>
 *
 * <p>"Black hat":</p>
 *
 * <p><em>dst = blackhat(src, element)= close(src, element)- src</em></p>
 *
 * <p>Any of the operations can be done in-place. In case of multi-channel images,
 * each channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphologyEx function for the morphological
 * opening and closing operations can be found at opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src Source image. The number of channels can be arbitrary. The depth
 * should be one of <code>CV_8U</code>, <code>CV_16U</code>, <code>CV_16S</code>,
 * <code>CV_32F" or </code>CV_64F".
 * @param dst Destination image of the same size and type as <code>src</code>.
 * @param op Type of a morphological operation that can be one of the following:
 * <ul>
 *   <li> MORPH_OPEN - an opening operation
 *   <li> MORPH_CLOSE - a closing operation
 *   <li> MORPH_GRADIENT - a morphological gradient
 *   <li> MORPH_TOPHAT - "top hat"
 *   <li> MORPH_BLACKHAT - "black hat"
 * </ul>
 * @param kernel a kernel
 * @param anchor a anchor
 * @param iterations Number of times erosion and dilation are applied.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#morphologyex">org.opencv.imgproc.Imgproc.morphologyEx</a>
 * @see org.opencv.imgproc.Imgproc#erode
 * @see org.opencv.imgproc.Imgproc#dilate
 */
    public static void morphologyEx(Mat src, Mat dst, int op, Mat kernel, Point anchor, int iterations)
    {

        morphologyEx_1(src.nativeObj, dst.nativeObj, op, kernel.nativeObj, anchor.x, anchor.y, iterations);

        return;
    }

/**
 * <p>Performs advanced morphological transformations.</p>
 *
 * <p>The function can perform advanced morphological transformations using an
 * erosion and dilation as basic operations.</p>
 *
 * <p>Opening operation:</p>
 *
 * <p><em>dst = open(src, element)= dilate(erode(src, element))</em></p>
 *
 * <p>Closing operation:</p>
 *
 * <p><em>dst = close(src, element)= erode(dilate(src, element))</em></p>
 *
 * <p>Morphological gradient:</p>
 *
 * <p><em>dst = morph_grad(src, element)= dilate(src, element)- erode(src,
 * element)</em></p>
 *
 * <p>"Top hat":</p>
 *
 * <p><em>dst = tophat(src, element)= src - open(src, element)</em></p>
 *
 * <p>"Black hat":</p>
 *
 * <p><em>dst = blackhat(src, element)= close(src, element)- src</em></p>
 *
 * <p>Any of the operations can be done in-place. In case of multi-channel images,
 * each channel is processed independently.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the morphologyEx function for the morphological
 * opening and closing operations can be found at opencv_source_code/samples/cpp/morphology2.cpp
 * </ul>
 *
 * @param src Source image. The number of channels can be arbitrary. The depth
 * should be one of <code>CV_8U</code>, <code>CV_16U</code>, <code>CV_16S</code>,
 * <code>CV_32F" or </code>CV_64F".
 * @param dst Destination image of the same size and type as <code>src</code>.
 * @param op Type of a morphological operation that can be one of the following:
 * <ul>
 *   <li> MORPH_OPEN - an opening operation
 *   <li> MORPH_CLOSE - a closing operation
 *   <li> MORPH_GRADIENT - a morphological gradient
 *   <li> MORPH_TOPHAT - "top hat"
 *   <li> MORPH_BLACKHAT - "black hat"
 * </ul>
 * @param kernel a kernel
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#morphologyex">org.opencv.imgproc.Imgproc.morphologyEx</a>
 * @see org.opencv.imgproc.Imgproc#erode
 * @see org.opencv.imgproc.Imgproc#dilate
 */
    public static void morphologyEx(Mat src, Mat dst, int op, Mat kernel)
    {

        morphologyEx_2(src.nativeObj, dst.nativeObj, op, kernel.nativeObj);

        return;
    }

    //
    // C++:  Point2d phaseCorrelate(Mat src1, Mat src2, Mat window = Mat())
    //

/**
 * <p>The function is used to detect translational shifts that occur between two
 * images. The operation takes advantage of the Fourier shift theorem for
 * detecting the translational shift in the frequency domain. It can be used for
 * fast image registration as well as motion estimation. For more information
 * please see http://en.wikipedia.org/wiki/Phase_correlation.</p>
 *
 * <p>Calculates the cross-power spectrum of two supplied source arrays. The arrays
 * are padded if needed with "getOptimalDFTSize".</p>
 *
 * <p>Return value: detected phase shift (sub-pixel) between the two arrays.</p>
 *
 * <p>The function performs the following equations</p>
 * <ul>
 *   <li> First it applies a Hanning window (see http://en.wikipedia.org/wiki/Hann_function)
 * to each image to remove possible edge effects. This window is cached until
 * the array size changes to speed up processing time.
 *   <li> Next it computes the forward DFTs of each source array:
 * </ul>
 *
 * <p><em>mathbf(G)_a = mathcal(F)(src_1), mathbf(G)_b = mathcal(F)(src_2)</em></p>
 *
 * <p>where <em>mathcal(F)</em> is the forward DFT.</p>
 * <ul>
 *   <li> It then computes the cross-power spectrum of each frequency domain
 * array:
 * </ul>
 *
 * <p><em>R = (mathbf(G)_a mathbf(G)_b^*)/(|mathbf(G)_a mathbf(G)_b^*|)</em></p>
 *
 * <ul>
 *   <li> Next the cross-correlation is converted back into the time domain via
 * the inverse DFT:
 * </ul>
 *
 * <p><em>r = mathcal(F)^(-1)(R)</em></p>
 *
 * <ul>
 *   <li> Finally, it computes the peak location and computes a 5x5 weighted
 * centroid around the peak to achieve sub-pixel accuracy.
 * </ul>
 *
 * <p><em>(Delta x, Delta y) = weightedCentroid (arg max_((x, y))(r))</em></p>
 *
 * <ul>
 *   <li> If non-zero, the response parameter is computed as the sum of the
 * elements of r within the 5x5 centroid around the peak location. It is
 * normalized to a maximum of 1 (meaning there is a single peak) and will be
 * smaller when there are multiple peaks.
 * </ul>
 *
 * @param src1 Source floating point array (CV_32FC1 or CV_64FC1)
 * @param src2 Source floating point array (CV_32FC1 or CV_64FC1)
 * @param window Floating point array with windowing coefficients to reduce edge
 * effects (optional).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#phasecorrelate">org.opencv.imgproc.Imgproc.phaseCorrelate</a>
 * @see org.opencv.imgproc.Imgproc#createHanningWindow
 * @see org.opencv.core.Core#dft
 * @see org.opencv.core.Core#mulSpectrums
 * @see org.opencv.core.Core#getOptimalDFTSize
 * @see org.opencv.core.Core#idft
 */
    public static Point phaseCorrelate(Mat src1, Mat src2, Mat window)
    {

        Point retVal = new Point(phaseCorrelate_0(src1.nativeObj, src2.nativeObj, window.nativeObj));

        return retVal;
    }

/**
 * <p>The function is used to detect translational shifts that occur between two
 * images. The operation takes advantage of the Fourier shift theorem for
 * detecting the translational shift in the frequency domain. It can be used for
 * fast image registration as well as motion estimation. For more information
 * please see http://en.wikipedia.org/wiki/Phase_correlation.</p>
 *
 * <p>Calculates the cross-power spectrum of two supplied source arrays. The arrays
 * are padded if needed with "getOptimalDFTSize".</p>
 *
 * <p>Return value: detected phase shift (sub-pixel) between the two arrays.</p>
 *
 * <p>The function performs the following equations</p>
 * <ul>
 *   <li> First it applies a Hanning window (see http://en.wikipedia.org/wiki/Hann_function)
 * to each image to remove possible edge effects. This window is cached until
 * the array size changes to speed up processing time.
 *   <li> Next it computes the forward DFTs of each source array:
 * </ul>
 *
 * <p><em>mathbf(G)_a = mathcal(F)(src_1), mathbf(G)_b = mathcal(F)(src_2)</em></p>
 *
 * <p>where <em>mathcal(F)</em> is the forward DFT.</p>
 * <ul>
 *   <li> It then computes the cross-power spectrum of each frequency domain
 * array:
 * </ul>
 *
 * <p><em>R = (mathbf(G)_a mathbf(G)_b^*)/(|mathbf(G)_a mathbf(G)_b^*|)</em></p>
 *
 * <ul>
 *   <li> Next the cross-correlation is converted back into the time domain via
 * the inverse DFT:
 * </ul>
 *
 * <p><em>r = mathcal(F)^(-1)(R)</em></p>
 *
 * <ul>
 *   <li> Finally, it computes the peak location and computes a 5x5 weighted
 * centroid around the peak to achieve sub-pixel accuracy.
 * </ul>
 *
 * <p><em>(Delta x, Delta y) = weightedCentroid (arg max_((x, y))(r))</em></p>
 *
 * <ul>
 *   <li> If non-zero, the response parameter is computed as the sum of the
 * elements of r within the 5x5 centroid around the peak location. It is
 * normalized to a maximum of 1 (meaning there is a single peak) and will be
 * smaller when there are multiple peaks.
 * </ul>
 *
 * @param src1 Source floating point array (CV_32FC1 or CV_64FC1)
 * @param src2 Source floating point array (CV_32FC1 or CV_64FC1)
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/motion_analysis_and_object_tracking.html#phasecorrelate">org.opencv.imgproc.Imgproc.phaseCorrelate</a>
 * @see org.opencv.imgproc.Imgproc#createHanningWindow
 * @see org.opencv.core.Core#dft
 * @see org.opencv.core.Core#mulSpectrums
 * @see org.opencv.core.Core#getOptimalDFTSize
 * @see org.opencv.core.Core#idft
 */
    public static Point phaseCorrelate(Mat src1, Mat src2)
    {

        Point retVal = new Point(phaseCorrelate_1(src1.nativeObj, src2.nativeObj));

        return retVal;
    }

    //
    // C++:  Point2d phaseCorrelateRes(Mat src1, Mat src2, Mat window, double* response = 0)
    //

    public static Point phaseCorrelateRes(Mat src1, Mat src2, Mat window, double[] response)
    {
        double[] response_out = new double[1];
        Point retVal = new Point(phaseCorrelateRes_0(src1.nativeObj, src2.nativeObj, window.nativeObj, response_out));
        if(response!=null) response[0] = (double)response_out[0];
        return retVal;
    }

    public static Point phaseCorrelateRes(Mat src1, Mat src2, Mat window)
    {

        Point retVal = new Point(phaseCorrelateRes_1(src1.nativeObj, src2.nativeObj, window.nativeObj));

        return retVal;
    }

    //
    // C++:  double pointPolygonTest(vector_Point2f contour, Point2f pt, bool measureDist)
    //

/**
 * <p>Performs a point-in-contour test.</p>
 *
 * <p>The function determines whether the point is inside a contour, outside, or
 * lies on an edge (or coincides with a vertex). It returns positive (inside),
 * negative (outside), or zero (on an edge) value, correspondingly. When
 * <code>measureDist=false</code>, the return value is +1, -1, and 0,
 * respectively. Otherwise, the return value is a signed distance between the
 * point and the nearest contour edge.</p>
 *
 * <p>See below a sample output of the function where each image pixel is tested
 * against the contour.</p>
 *
 * @param contour Input contour.
 * @param pt Point tested against the contour.
 * @param measureDist If true, the function estimates the signed distance from
 * the point to the nearest contour edge. Otherwise, the function only checks if
 * the point is inside a contour or not.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html#pointpolygontest">org.opencv.imgproc.Imgproc.pointPolygonTest</a>
 */
    public static double pointPolygonTest(MatOfPoint2f contour, Point pt, boolean measureDist)
    {
        Mat contour_mat = contour;
        double retVal = pointPolygonTest_0(contour_mat.nativeObj, pt.x, pt.y, measureDist);

        return retVal;
    }

    //
    // C++:  void preCornerDetect(Mat src, Mat& dst, int ksize, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Calculates a feature map for corner detection.</p>
 *
 * <p>The function calculates the complex spatial derivative-based function of the
 * source image</p>
 *
 * <p><em>dst = (D_x src)^2 * D_(yy) src + (D_y src)^2 * D_(xx) src - 2 D_x src *
 * D_y src * D_(xy) src</em></p>
 *
 * <p>where <em>D_x</em>,<em>D_y</em> are the first image derivatives,
 * <em>D_(xx)</em>,<em>D_(yy)</em> are the second image derivatives, and
 * <em>D_(xy)</em> is the mixed derivative.
 * The corners can be found as local maximums of the functions, as shown below:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat corners, dilated_corners;</p>
 *
 * <p>preCornerDetect(image, corners, 3);</p>
 *
 * <p>// dilation with 3x3 rectangular structuring element</p>
 *
 * <p>dilate(corners, dilated_corners, Mat(), 1);</p>
 *
 * <p>Mat corner_mask = corners == dilated_corners;</p>
 *
 * <p></code></p>
 *
 * @param src Source single-channel 8-bit of floating-point image.
 * @param dst Output image that has the type <code>CV_32F</code> and the same
 * size as <code>src</code>.
 * @param ksize Aperture size of the "Sobel".
 * @param borderType Pixel extrapolation method. See "borderInterpolate".
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#precornerdetect">org.opencv.imgproc.Imgproc.preCornerDetect</a>
 */
    public static void preCornerDetect(Mat src, Mat dst, int ksize, int borderType)
    {

        preCornerDetect_0(src.nativeObj, dst.nativeObj, ksize, borderType);

        return;
    }

/**
 * <p>Calculates a feature map for corner detection.</p>
 *
 * <p>The function calculates the complex spatial derivative-based function of the
 * source image</p>
 *
 * <p><em>dst = (D_x src)^2 * D_(yy) src + (D_y src)^2 * D_(xx) src - 2 D_x src *
 * D_y src * D_(xy) src</em></p>
 *
 * <p>where <em>D_x</em>,<em>D_y</em> are the first image derivatives,
 * <em>D_(xx)</em>,<em>D_(yy)</em> are the second image derivatives, and
 * <em>D_(xy)</em> is the mixed derivative.
 * The corners can be found as local maximums of the functions, as shown below:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat corners, dilated_corners;</p>
 *
 * <p>preCornerDetect(image, corners, 3);</p>
 *
 * <p>// dilation with 3x3 rectangular structuring element</p>
 *
 * <p>dilate(corners, dilated_corners, Mat(), 1);</p>
 *
 * <p>Mat corner_mask = corners == dilated_corners;</p>
 *
 * <p></code></p>
 *
 * @param src Source single-channel 8-bit of floating-point image.
 * @param dst Output image that has the type <code>CV_32F</code> and the same
 * size as <code>src</code>.
 * @param ksize Aperture size of the "Sobel".
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/feature_detection.html#precornerdetect">org.opencv.imgproc.Imgproc.preCornerDetect</a>
 */
    public static void preCornerDetect(Mat src, Mat dst, int ksize)
    {

        preCornerDetect_1(src.nativeObj, dst.nativeObj, ksize);

        return;
    }

    //
    // C++:  void pyrDown(Mat src, Mat& dst, Size dstsize = Size(), int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Blurs an image and downsamples it.</p>
 *
 * <p>The function performs the downsampling step of the Gaussian pyramid
 * construction. First, it convolves the source image with the kernel:</p>
 *
 * <p><em>1/256 1 4 6 4 1
 * 4 16 24 16 4
 * 6 24 36 24 6
 * 4 16 24 16 4
 * 1 4 6 4 1 </em></p>
 *
 * <p>Then, it downsamples the image by rejecting even rows and columns.</p>
 *
 * @param src input image.
 * @param dst output image; it has the specified size and the same type as
 * <code>src</code>.
 * @param dstsize size of the output image; by default, it is computed as
 * <code>Size((src.cols+1)/2, (src.rows+1)/2)</code>, but in any case, the
 * following conditions should be satisfied:
 *
 * <p><em> ltBR gt| dstsize.width *2-src.cols| <= 2
 * |dstsize.height *2-src.rows| <= 2 </em></p>
 * @param borderType a borderType
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrdown">org.opencv.imgproc.Imgproc.pyrDown</a>
 */
    public static void pyrDown(Mat src, Mat dst, Size dstsize, int borderType)
    {

        pyrDown_0(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height, borderType);

        return;
    }

/**
 * <p>Blurs an image and downsamples it.</p>
 *
 * <p>The function performs the downsampling step of the Gaussian pyramid
 * construction. First, it convolves the source image with the kernel:</p>
 *
 * <p><em>1/256 1 4 6 4 1
 * 4 16 24 16 4
 * 6 24 36 24 6
 * 4 16 24 16 4
 * 1 4 6 4 1 </em></p>
 *
 * <p>Then, it downsamples the image by rejecting even rows and columns.</p>
 *
 * @param src input image.
 * @param dst output image; it has the specified size and the same type as
 * <code>src</code>.
 * @param dstsize size of the output image; by default, it is computed as
 * <code>Size((src.cols+1)/2, (src.rows+1)/2)</code>, but in any case, the
 * following conditions should be satisfied:
 *
 * <p><em> ltBR gt| dstsize.width *2-src.cols| <= 2
 * |dstsize.height *2-src.rows| <= 2 </em></p>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrdown">org.opencv.imgproc.Imgproc.pyrDown</a>
 */
    public static void pyrDown(Mat src, Mat dst, Size dstsize)
    {

        pyrDown_1(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height);

        return;
    }

/**
 * <p>Blurs an image and downsamples it.</p>
 *
 * <p>The function performs the downsampling step of the Gaussian pyramid
 * construction. First, it convolves the source image with the kernel:</p>
 *
 * <p><em>1/256 1 4 6 4 1
 * 4 16 24 16 4
 * 6 24 36 24 6
 * 4 16 24 16 4
 * 1 4 6 4 1 </em></p>
 *
 * <p>Then, it downsamples the image by rejecting even rows and columns.</p>
 *
 * @param src input image.
 * @param dst output image; it has the specified size and the same type as
 * <code>src</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrdown">org.opencv.imgproc.Imgproc.pyrDown</a>
 */
    public static void pyrDown(Mat src, Mat dst)
    {

        pyrDown_2(src.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void pyrMeanShiftFiltering(Mat src, Mat& dst, double sp, double sr, int maxLevel = 1, TermCriteria termcrit = TermCriteria( TermCriteria::MAX_ITER+TermCriteria::EPS,5,1))
    //

/**
 * <p>Performs initial step of meanshift segmentation of an image.</p>
 *
 * <p>The function implements the filtering stage of meanshift segmentation, that
 * is, the output of the function is the filtered "posterized" image with color
 * gradients and fine-grain texture flattened. At every pixel <code>(X,Y)</code>
 * of the input image (or down-sized input image, see below) the function
 * executes meanshift iterations, that is, the pixel <code>(X,Y)</code>
 * neighborhood in the joint space-color hyperspace is considered:</p>
 *
 * <p><em>(x,y): X- sp <= x <= X+ sp, Y- sp <= y <= Y+ sp, ||(R,G,B)-(r,g,b)|| <=
 * sr</em></p>
 *
 * <p>where <code>(R,G,B)</code> and <code>(r,g,b)</code> are the vectors of color
 * components at <code>(X,Y)</code> and <code>(x,y)</code>, respectively
 * (though, the algorithm does not depend on the color space used, so any
 * 3-component color space can be used instead). Over the neighborhood the
 * average spatial value <code>(X',Y')</code> and average color vector
 * <code>(R',G',B')</code> are found and they act as the neighborhood center on
 * the next iteration:</p>
 *
 * <p><em>(X,Y)~(X',Y'), (R,G,B)~(R',G',B').</em></p>
 *
 * <p>After the iterations over, the color components of the initial pixel (that
 * is, the pixel from where the iterations started) are set to the final value
 * (average color at the last iteration):</p>
 *
 * <p><em>I(X,Y) &lt- (R*,G*,B*)</em></p>
 *
 * <p>When <code>maxLevel > 0</code>, the gaussian pyramid of <code>maxLevel+1</code>
 * levels is built, and the above procedure is run on the smallest layer first.
 * After that, the results are propagated to the larger layer and the iterations
 * are run again only on those pixels where the layer colors differ by more than
 * <code>sr</code> from the lower-resolution layer of the pyramid. That makes
 * boundaries of color regions sharper. Note that the results will be actually
 * different from the ones obtained by running the meanshift procedure on the
 * whole original image (i.e. when <code>maxLevel==0</code>).</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using mean-shift image segmentation can be found at
 * opencv_source_code/samples/cpp/meanshift_segmentation.cpp
 * </ul>
 *
 * @param src The source 8-bit, 3-channel image.
 * @param dst The destination image of the same format and the same size as the
 * source.
 * @param sp The spatial window radius.
 * @param sr The color window radius.
 * @param maxLevel Maximum level of the pyramid for the segmentation.
 * @param termcrit Termination criteria: when to stop meanshift iterations.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrmeanshiftfiltering">org.opencv.imgproc.Imgproc.pyrMeanShiftFiltering</a>
 */
    public static void pyrMeanShiftFiltering(Mat src, Mat dst, double sp, double sr, int maxLevel, TermCriteria termcrit)
    {

        pyrMeanShiftFiltering_0(src.nativeObj, dst.nativeObj, sp, sr, maxLevel, termcrit.type, termcrit.maxCount, termcrit.epsilon);

        return;
    }

/**
 * <p>Performs initial step of meanshift segmentation of an image.</p>
 *
 * <p>The function implements the filtering stage of meanshift segmentation, that
 * is, the output of the function is the filtered "posterized" image with color
 * gradients and fine-grain texture flattened. At every pixel <code>(X,Y)</code>
 * of the input image (or down-sized input image, see below) the function
 * executes meanshift iterations, that is, the pixel <code>(X,Y)</code>
 * neighborhood in the joint space-color hyperspace is considered:</p>
 *
 * <p><em>(x,y): X- sp <= x <= X+ sp, Y- sp <= y <= Y+ sp, ||(R,G,B)-(r,g,b)|| <=
 * sr</em></p>
 *
 * <p>where <code>(R,G,B)</code> and <code>(r,g,b)</code> are the vectors of color
 * components at <code>(X,Y)</code> and <code>(x,y)</code>, respectively
 * (though, the algorithm does not depend on the color space used, so any
 * 3-component color space can be used instead). Over the neighborhood the
 * average spatial value <code>(X',Y')</code> and average color vector
 * <code>(R',G',B')</code> are found and they act as the neighborhood center on
 * the next iteration:</p>
 *
 * <p><em>(X,Y)~(X',Y'), (R,G,B)~(R',G',B').</em></p>
 *
 * <p>After the iterations over, the color components of the initial pixel (that
 * is, the pixel from where the iterations started) are set to the final value
 * (average color at the last iteration):</p>
 *
 * <p><em>I(X,Y) &lt- (R*,G*,B*)</em></p>
 *
 * <p>When <code>maxLevel > 0</code>, the gaussian pyramid of <code>maxLevel+1</code>
 * levels is built, and the above procedure is run on the smallest layer first.
 * After that, the results are propagated to the larger layer and the iterations
 * are run again only on those pixels where the layer colors differ by more than
 * <code>sr</code> from the lower-resolution layer of the pyramid. That makes
 * boundaries of color regions sharper. Note that the results will be actually
 * different from the ones obtained by running the meanshift procedure on the
 * whole original image (i.e. when <code>maxLevel==0</code>).</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using mean-shift image segmentation can be found at
 * opencv_source_code/samples/cpp/meanshift_segmentation.cpp
 * </ul>
 *
 * @param src The source 8-bit, 3-channel image.
 * @param dst The destination image of the same format and the same size as the
 * source.
 * @param sp The spatial window radius.
 * @param sr The color window radius.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrmeanshiftfiltering">org.opencv.imgproc.Imgproc.pyrMeanShiftFiltering</a>
 */
    public static void pyrMeanShiftFiltering(Mat src, Mat dst, double sp, double sr)
    {

        pyrMeanShiftFiltering_1(src.nativeObj, dst.nativeObj, sp, sr);

        return;
    }

    //
    // C++:  void pyrUp(Mat src, Mat& dst, Size dstsize = Size(), int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Upsamples an image and then blurs it.</p>
 *
 * <p>The function performs the upsampling step of the Gaussian pyramid
 * construction, though it can actually be used to construct the Laplacian
 * pyramid. First, it upsamples the source image by injecting even zero rows and
 * columns and then convolves the result with the same kernel as in "pyrDown"
 * multiplied by 4.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> (Python) An example of Laplacian Pyramid construction and merging can
 * be found at opencv_source_code/samples/python2/lappyr.py
 * </ul>
 *
 * @param src input image.
 * @param dst output image. It has the specified size and the same type as
 * <code>src</code>.
 * @param dstsize size of the output image; by default, it is computed as
 * <code>Size(src.cols*2, (src.rows*2)</code>, but in any case, the following
 * conditions should be satisfied:
 *
 * <p><em> ltBR gt| dstsize.width -src.cols*2| <= (dstsize.width mod 2)
 * |dstsize.height -src.rows*2| <= (dstsize.height mod 2) </em></p>
 * @param borderType a borderType
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrup">org.opencv.imgproc.Imgproc.pyrUp</a>
 */
    public static void pyrUp(Mat src, Mat dst, Size dstsize, int borderType)
    {

        pyrUp_0(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height, borderType);

        return;
    }

/**
 * <p>Upsamples an image and then blurs it.</p>
 *
 * <p>The function performs the upsampling step of the Gaussian pyramid
 * construction, though it can actually be used to construct the Laplacian
 * pyramid. First, it upsamples the source image by injecting even zero rows and
 * columns and then convolves the result with the same kernel as in "pyrDown"
 * multiplied by 4.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> (Python) An example of Laplacian Pyramid construction and merging can
 * be found at opencv_source_code/samples/python2/lappyr.py
 * </ul>
 *
 * @param src input image.
 * @param dst output image. It has the specified size and the same type as
 * <code>src</code>.
 * @param dstsize size of the output image; by default, it is computed as
 * <code>Size(src.cols*2, (src.rows*2)</code>, but in any case, the following
 * conditions should be satisfied:
 *
 * <p><em> ltBR gt| dstsize.width -src.cols*2| <= (dstsize.width mod 2)
 * |dstsize.height -src.rows*2| <= (dstsize.height mod 2) </em></p>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrup">org.opencv.imgproc.Imgproc.pyrUp</a>
 */
    public static void pyrUp(Mat src, Mat dst, Size dstsize)
    {

        pyrUp_1(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height);

        return;
    }

/**
 * <p>Upsamples an image and then blurs it.</p>
 *
 * <p>The function performs the upsampling step of the Gaussian pyramid
 * construction, though it can actually be used to construct the Laplacian
 * pyramid. First, it upsamples the source image by injecting even zero rows and
 * columns and then convolves the result with the same kernel as in "pyrDown"
 * multiplied by 4.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> (Python) An example of Laplacian Pyramid construction and merging can
 * be found at opencv_source_code/samples/python2/lappyr.py
 * </ul>
 *
 * @param src input image.
 * @param dst output image. It has the specified size and the same type as
 * <code>src</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#pyrup">org.opencv.imgproc.Imgproc.pyrUp</a>
 */
    public static void pyrUp(Mat src, Mat dst)
    {

        pyrUp_2(src.nativeObj, dst.nativeObj);

        return;
    }

    //
    // C++:  void remap(Mat src, Mat& dst, Mat map1, Mat map2, int interpolation, int borderMode = BORDER_CONSTANT, Scalar borderValue = Scalar())
    //

/**
 * <p>Applies a generic geometrical transformation to an image.</p>
 *
 * <p>The function <code>remap</code> transforms the source image using the
 * specified map:</p>
 *
 * <p><em>dst(x,y) = src(map_x(x,y),map_y(x,y))</em></p>
 *
 * <p>where values of pixels with non-integer coordinates are computed using one of
 * available interpolation methods.
 * <em>map_x</em> and <em>map_y</em> can be encoded as separate floating-point
 * maps in <em>map_1</em> and <em>map_2</em> respectively, or interleaved
 * floating-point maps of <em>(x,y)</em> in <em>map_1</em>, or fixed-point maps
 * created by using "convertMaps". The reason you might want to convert from
 * floating to fixed-point representations of a map is that they can yield much
 * faster (~2x) remapping operations. In the converted case, <em>map_1</em>
 * contains pairs <code>(cvFloor(x), cvFloor(y))</code> and <em>map_2</em>
 * contains indices in a table of interpolation coefficients.</p>
 *
 * <p>This function cannot operate in-place.</p>
 *
 * @param src Source image.
 * @param dst Destination image. It has the same size as <code>map1</code> and
 * the same type as <code>src</code>.
 * @param map1 The first map of either <code>(x,y)</code> points or just
 * <code>x</code> values having the type <code>CV_16SC2</code>,
 * <code>CV_32FC1</code>, or <code>CV_32FC2</code>. See "convertMaps" for
 * details on converting a floating point representation to fixed-point for
 * speed.
 * @param map2 The second map of <code>y</code> values having the type
 * <code>CV_16UC1</code>, <code>CV_32FC1</code>, or none (empty map if
 * <code>map1</code> is <code>(x,y)</code> points), respectively.
 * @param interpolation Interpolation method (see "resize"). The method
 * <code>INTER_AREA</code> is not supported by this function.
 * @param borderMode Pixel extrapolation method (see "borderInterpolate"). When
 * <code>borderMode=BORDER_TRANSPARENT</code>, it means that the pixels in the
 * destination image that corresponds to the "outliers" in the source image are
 * not modified by the function.
 * @param borderValue Value used in case of a constant border. By default, it is
 * 0.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#remap">org.opencv.imgproc.Imgproc.remap</a>
 */
    public static void remap(Mat src, Mat dst, Mat map1, Mat map2, int interpolation, int borderMode, Scalar borderValue)
    {

        remap_0(src.nativeObj, dst.nativeObj, map1.nativeObj, map2.nativeObj, interpolation, borderMode, borderValue.val[0], borderValue.val[1], borderValue.val[2], borderValue.val[3]);

        return;
    }

/**
 * <p>Applies a generic geometrical transformation to an image.</p>
 *
 * <p>The function <code>remap</code> transforms the source image using the
 * specified map:</p>
 *
 * <p><em>dst(x,y) = src(map_x(x,y),map_y(x,y))</em></p>
 *
 * <p>where values of pixels with non-integer coordinates are computed using one of
 * available interpolation methods.
 * <em>map_x</em> and <em>map_y</em> can be encoded as separate floating-point
 * maps in <em>map_1</em> and <em>map_2</em> respectively, or interleaved
 * floating-point maps of <em>(x,y)</em> in <em>map_1</em>, or fixed-point maps
 * created by using "convertMaps". The reason you might want to convert from
 * floating to fixed-point representations of a map is that they can yield much
 * faster (~2x) remapping operations. In the converted case, <em>map_1</em>
 * contains pairs <code>(cvFloor(x), cvFloor(y))</code> and <em>map_2</em>
 * contains indices in a table of interpolation coefficients.</p>
 *
 * <p>This function cannot operate in-place.</p>
 *
 * @param src Source image.
 * @param dst Destination image. It has the same size as <code>map1</code> and
 * the same type as <code>src</code>.
 * @param map1 The first map of either <code>(x,y)</code> points or just
 * <code>x</code> values having the type <code>CV_16SC2</code>,
 * <code>CV_32FC1</code>, or <code>CV_32FC2</code>. See "convertMaps" for
 * details on converting a floating point representation to fixed-point for
 * speed.
 * @param map2 The second map of <code>y</code> values having the type
 * <code>CV_16UC1</code>, <code>CV_32FC1</code>, or none (empty map if
 * <code>map1</code> is <code>(x,y)</code> points), respectively.
 * @param interpolation Interpolation method (see "resize"). The method
 * <code>INTER_AREA</code> is not supported by this function.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#remap">org.opencv.imgproc.Imgproc.remap</a>
 */
    public static void remap(Mat src, Mat dst, Mat map1, Mat map2, int interpolation)
    {

        remap_1(src.nativeObj, dst.nativeObj, map1.nativeObj, map2.nativeObj, interpolation);

        return;
    }

    //
    // C++:  void resize(Mat src, Mat& dst, Size dsize, double fx = 0, double fy = 0, int interpolation = INTER_LINEAR)
    //

/**
 * <p>Resizes an image.</p>
 *
 * <p>The function <code>resize</code> resizes the image <code>src</code> down to
 * or up to the specified size.Note that the initial <code>dst</code> type or
 * size are not taken into account. Instead, the size and type are derived from
 * the <code>src</code>,<code>dsize</code>,<code>fx</code>, and <code>fy</code>.
 * If you want to resize <code>src</code> so that it fits the pre-created
 * <code>dst</code>, you may call the function as follows: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// explicitly specify dsize=dst.size(); fx and fy will be computed from that.</p>
 *
 * <p>resize(src, dst, dst.size(), 0, 0, interpolation);</p>
 *
 * <p>If you want to decimate the image by factor of 2 in each direction, you can
 * call the function this way:</p>
 *
 * <p>// specify fx and fy and let the function compute the destination image size.</p>
 *
 * <p>resize(src, dst, Size(), 0.5, 0.5, interpolation);</p>
 *
 * <p>To shrink an image, it will generally look best with CV_INTER_AREA
 * interpolation, whereas to enlarge an image, it will generally look best with
 * CV_INTER_CUBIC (slow) or CV_INTER_LINEAR (faster but still looks OK).
 * </code></p>
 *
 * @param src input image.
 * @param dst output image; it has the size <code>dsize</code> (when it is
 * non-zero) or the size computed from <code>src.size()</code>, <code>fx</code>,
 * and <code>fy</code>; the type of <code>dst</code> is the same as of
 * <code>src</code>.
 * @param dsize output image size; if it equals zero, it is computed as:
 *
 * <p><em>dsize = Size(round(fx*src.cols), round(fy*src.rows))</em></p>
 *
 * <p>Either <code>dsize</code> or both <code>fx</code> and <code>fy</code> must be
 * non-zero.</p>
 * @param fx scale factor along the horizontal axis; when it equals 0, it is
 * computed as
 *
 * <p><em>(double)dsize.width/src.cols</em></p>
 * @param fy scale factor along the vertical axis; when it equals 0, it is
 * computed as
 *
 * <p><em>(double)dsize.height/src.rows</em></p>
 * @param interpolation interpolation method:
 * <ul>
 *   <li> INTER_NEAREST - a nearest-neighbor interpolation
 *   <li> INTER_LINEAR - a bilinear interpolation (used by default)
 *   <li> INTER_AREA - resampling using pixel area relation. It may be a
 * preferred method for image decimation, as it gives moire'-free results. But
 * when the image is zoomed, it is similar to the <code>INTER_NEAREST</code>
 * method.
 *   <li> INTER_CUBIC - a bicubic interpolation over 4x4 pixel neighborhood
 *   <li> INTER_LANCZOS4 - a Lanczos interpolation over 8x8 pixel neighborhood
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#resize">org.opencv.imgproc.Imgproc.resize</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 */
    public static void resize(Mat src, Mat dst, Size dsize, double fx, double fy, int interpolation)
    {

        resize_0(src.nativeObj, dst.nativeObj, dsize.width, dsize.height, fx, fy, interpolation);

        return;
    }

/**
 * <p>Resizes an image.</p>
 *
 * <p>The function <code>resize</code> resizes the image <code>src</code> down to
 * or up to the specified size.Note that the initial <code>dst</code> type or
 * size are not taken into account. Instead, the size and type are derived from
 * the <code>src</code>,<code>dsize</code>,<code>fx</code>, and <code>fy</code>.
 * If you want to resize <code>src</code> so that it fits the pre-created
 * <code>dst</code>, you may call the function as follows: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// explicitly specify dsize=dst.size(); fx and fy will be computed from that.</p>
 *
 * <p>resize(src, dst, dst.size(), 0, 0, interpolation);</p>
 *
 * <p>If you want to decimate the image by factor of 2 in each direction, you can
 * call the function this way:</p>
 *
 * <p>// specify fx and fy and let the function compute the destination image size.</p>
 *
 * <p>resize(src, dst, Size(), 0.5, 0.5, interpolation);</p>
 *
 * <p>To shrink an image, it will generally look best with CV_INTER_AREA
 * interpolation, whereas to enlarge an image, it will generally look best with
 * CV_INTER_CUBIC (slow) or CV_INTER_LINEAR (faster but still looks OK).
 * </code></p>
 *
 * @param src input image.
 * @param dst output image; it has the size <code>dsize</code> (when it is
 * non-zero) or the size computed from <code>src.size()</code>, <code>fx</code>,
 * and <code>fy</code>; the type of <code>dst</code> is the same as of
 * <code>src</code>.
 * @param dsize output image size; if it equals zero, it is computed as:
 *
 * <p><em>dsize = Size(round(fx*src.cols), round(fy*src.rows))</em></p>
 *
 * <p>Either <code>dsize</code> or both <code>fx</code> and <code>fy</code> must be
 * non-zero.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#resize">org.opencv.imgproc.Imgproc.resize</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 */
    public static void resize(Mat src, Mat dst, Size dsize)
    {

        resize_1(src.nativeObj, dst.nativeObj, dsize.width, dsize.height);

        return;
    }

    //
    // C++:  void sepFilter2D(Mat src, Mat& dst, int ddepth, Mat kernelX, Mat kernelY, Point anchor = Point(-1,-1), double delta = 0, int borderType = BORDER_DEFAULT)
    //

/**
 * <p>Applies a separable linear filter to an image.</p>
 *
 * <p>The function applies a separable linear filter to the image. That is, first,
 * every row of <code>src</code> is filtered with the 1D kernel
 * <code>kernelX</code>. Then, every column of the result is filtered with the
 * 1D kernel <code>kernelY</code>. The final result shifted by <code>delta</code>
 * is stored in <code>dst</code>.</p>
 *
 * @param src Source image.
 * @param dst Destination image of the same size and the same number of channels
 * as <code>src</code>.
 * @param ddepth Destination image depth. The following combination of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the destination image will have the same depth
 * as the source.</p>
 * @param kernelX Coefficients for filtering each row.
 * @param kernelY Coefficients for filtering each column.
 * @param anchor Anchor position within the kernel. The default value
 * <em>(-1,-1)</em> means that the anchor is at the kernel center.
 * @param delta Value added to the filtered results before storing them.
 * @param borderType Pixel extrapolation method. See "borderInterpolate" for
 * details.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#sepfilter2d">org.opencv.imgproc.Imgproc.sepFilter2D</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#Sobel
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#blur
 * @see org.opencv.imgproc.Imgproc#filter2D
 */
    public static void sepFilter2D(Mat src, Mat dst, int ddepth, Mat kernelX, Mat kernelY, Point anchor, double delta, int borderType)
    {

        sepFilter2D_0(src.nativeObj, dst.nativeObj, ddepth, kernelX.nativeObj, kernelY.nativeObj, anchor.x, anchor.y, delta, borderType);

        return;
    }

/**
 * <p>Applies a separable linear filter to an image.</p>
 *
 * <p>The function applies a separable linear filter to the image. That is, first,
 * every row of <code>src</code> is filtered with the 1D kernel
 * <code>kernelX</code>. Then, every column of the result is filtered with the
 * 1D kernel <code>kernelY</code>. The final result shifted by <code>delta</code>
 * is stored in <code>dst</code>.</p>
 *
 * @param src Source image.
 * @param dst Destination image of the same size and the same number of channels
 * as <code>src</code>.
 * @param ddepth Destination image depth. The following combination of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the destination image will have the same depth
 * as the source.</p>
 * @param kernelX Coefficients for filtering each row.
 * @param kernelY Coefficients for filtering each column.
 * @param anchor Anchor position within the kernel. The default value
 * <em>(-1,-1)</em> means that the anchor is at the kernel center.
 * @param delta Value added to the filtered results before storing them.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#sepfilter2d">org.opencv.imgproc.Imgproc.sepFilter2D</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#Sobel
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#blur
 * @see org.opencv.imgproc.Imgproc#filter2D
 */
    public static void sepFilter2D(Mat src, Mat dst, int ddepth, Mat kernelX, Mat kernelY, Point anchor, double delta)
    {

        sepFilter2D_1(src.nativeObj, dst.nativeObj, ddepth, kernelX.nativeObj, kernelY.nativeObj, anchor.x, anchor.y, delta);

        return;
    }

/**
 * <p>Applies a separable linear filter to an image.</p>
 *
 * <p>The function applies a separable linear filter to the image. That is, first,
 * every row of <code>src</code> is filtered with the 1D kernel
 * <code>kernelX</code>. Then, every column of the result is filtered with the
 * 1D kernel <code>kernelY</code>. The final result shifted by <code>delta</code>
 * is stored in <code>dst</code>.</p>
 *
 * @param src Source image.
 * @param dst Destination image of the same size and the same number of channels
 * as <code>src</code>.
 * @param ddepth Destination image depth. The following combination of
 * <code>src.depth()</code> and <code>ddepth</code> are supported:
 * <ul>
 *   <li> <code>src.depth()</code> = <code>CV_8U</code>, <code>ddepth</code> =
 * -1/<code>CV_16S</code>/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_16U</code>/<code>CV_16S</code>,
 * <code>ddepth</code> = -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_32F</code>, <code>ddepth</code> =
 * -1/<code>CV_32F</code>/<code>CV_64F</code>
 *   <li> <code>src.depth()</code> = <code>CV_64F</code>, <code>ddepth</code> =
 * -1/<code>CV_64F</code>
 * </ul>
 *
 * <p>when <code>ddepth=-1</code>, the destination image will have the same depth
 * as the source.</p>
 * @param kernelX Coefficients for filtering each row.
 * @param kernelY Coefficients for filtering each column.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html#sepfilter2d">org.opencv.imgproc.Imgproc.sepFilter2D</a>
 * @see org.opencv.imgproc.Imgproc#GaussianBlur
 * @see org.opencv.imgproc.Imgproc#Sobel
 * @see org.opencv.imgproc.Imgproc#boxFilter
 * @see org.opencv.imgproc.Imgproc#blur
 * @see org.opencv.imgproc.Imgproc#filter2D
 */
    public static void sepFilter2D(Mat src, Mat dst, int ddepth, Mat kernelX, Mat kernelY)
    {

        sepFilter2D_2(src.nativeObj, dst.nativeObj, ddepth, kernelX.nativeObj, kernelY.nativeObj);

        return;
    }

    //
    // C++:  double threshold(Mat src, Mat& dst, double thresh, double maxval, int type)
    //

/**
 * <p>Applies a fixed-level threshold to each array element.</p>
 *
 * <p>The function applies fixed-level thresholding to a single-channel array. The
 * function is typically used to get a bi-level (binary) image out of a
 * grayscale image ("compare" could be also used for this purpose) or for
 * removing a noise, that is, filtering out pixels with too small or too large
 * values. There are several types of thresholding supported by the function.
 * They are determined by <code>type</code> :</p>
 * <ul>
 *   <li> THRESH_BINARY
 * </ul>
 *
 * <p><em>dst(x,y) = maxval if src(x,y) &gt thresh; 0 otherwise</em></p>
 *
 * <ul>
 *   <li> THRESH_BINARY_INV
 * </ul>
 *
 * <p><em>dst(x,y) = 0 if src(x,y) &gt thresh; maxval otherwise</em></p>
 *
 * <ul>
 *   <li> THRESH_TRUNC
 * </ul>
 *
 * <p><em>dst(x,y) = threshold if src(x,y) &gt thresh; src(x,y) otherwise</em></p>
 *
 * <ul>
 *   <li> THRESH_TOZERO
 * </ul>
 *
 * <p><em>dst(x,y) = src(x,y) if src(x,y) &gt thresh; 0 otherwise</em></p>
 *
 * <ul>
 *   <li> THRESH_TOZERO_INV
 * </ul>
 *
 * <p><em>dst(x,y) = 0 if src(x,y) &gt thresh; src(x,y) otherwise</em></p>
 *
 * <p>Also, the special value <code>THRESH_OTSU</code> may be combined with one of
 * the above values. In this case, the function determines the optimal threshold
 * value using the Otsu's algorithm and uses it instead of the specified
 * <code>thresh</code>.
 * The function returns the computed threshold value.
 * Currently, the Otsu's method is implemented only for 8-bit images.</p>
 *
 * @param src input array (single-channel, 8-bit or 32-bit floating point).
 * @param dst output array of the same size and type as <code>src</code>.
 * @param thresh threshold value.
 * @param maxval maximum value to use with the <code>THRESH_BINARY</code> and
 * <code>THRESH_BINARY_INV</code> thresholding types.
 * @param type thresholding type (see the details below).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#threshold">org.opencv.imgproc.Imgproc.threshold</a>
 * @see org.opencv.imgproc.Imgproc#findContours
 * @see org.opencv.core.Core#max
 * @see org.opencv.imgproc.Imgproc#adaptiveThreshold
 * @see org.opencv.core.Core#compare
 * @see org.opencv.core.Core#min
 */
    public static double threshold(Mat src, Mat dst, double thresh, double maxval, int type)
    {

        double retVal = threshold_0(src.nativeObj, dst.nativeObj, thresh, maxval, type);

        return retVal;
    }

    //
    // C++:  void undistort(Mat src, Mat& dst, Mat cameraMatrix, Mat distCoeffs, Mat newCameraMatrix = Mat())
    //

/**
 * <p>Transforms an image to compensate for lens distortion.</p>
 *
 * <p>The function transforms an image to compensate radial and tangential lens
 * distortion.</p>
 *
 * <p>The function is simply a combination of "initUndistortRectifyMap" (with unity
 * <code>R</code>) and "remap" (with bilinear interpolation). See the former
 * function for details of the transformation being performed.</p>
 *
 * <p>Those pixels in the destination image, for which there is no correspondent
 * pixels in the source image, are filled with zeros (black color).</p>
 *
 * <p>A particular subset of the source image that will be visible in the corrected
 * image can be regulated by <code>newCameraMatrix</code>. You can use
 * "getOptimalNewCameraMatrix" to compute the appropriate <code>newCameraMatrix</code>
 * depending on your requirements.</p>
 *
 * <p>The camera matrix and the distortion parameters can be determined using
 * "calibrateCamera". If the resolution of images is different from the
 * resolution used at the calibration stage, <em>f_x, f_y, c_x</em> and
 * <em>c_y</em> need to be scaled accordingly, while the distortion coefficients
 * remain the same.</p>
 *
 * @param src Input (distorted) image.
 * @param dst Output (corrected) image that has the same size and type as
 * <code>src</code>.
 * @param cameraMatrix Input camera matrix <em>A =
 * <p>|f_x 0 c_x|
 * |0 f_y c_y|
 * |0 0 1|
 * </em>.</p>
 * @param distCoeffs Input vector of distortion coefficients <em>(k_1, k_2, p_1,
 * p_2[, k_3[, k_4, k_5, k_6]])</em> of 4, 5, or 8 elements. If the vector is
 * NULL/empty, the zero distortion coefficients are assumed.
 * @param newCameraMatrix Camera matrix of the distorted image. By default, it
 * is the same as <code>cameraMatrix</code> but you may additionally scale and
 * shift the result by using a different matrix.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#undistort">org.opencv.imgproc.Imgproc.undistort</a>
 */
    public static void undistort(Mat src, Mat dst, Mat cameraMatrix, Mat distCoeffs, Mat newCameraMatrix)
    {

        undistort_0(src.nativeObj, dst.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj, newCameraMatrix.nativeObj);

        return;
    }

/**
 * <p>Transforms an image to compensate for lens distortion.</p>
 *
 * <p>The function transforms an image to compensate radial and tangential lens
 * distortion.</p>
 *
 * <p>The function is simply a combination of "initUndistortRectifyMap" (with unity
 * <code>R</code>) and "remap" (with bilinear interpolation). See the former
 * function for details of the transformation being performed.</p>
 *
 * <p>Those pixels in the destination image, for which there is no correspondent
 * pixels in the source image, are filled with zeros (black color).</p>
 *
 * <p>A particular subset of the source image that will be visible in the corrected
 * image can be regulated by <code>newCameraMatrix</code>. You can use
 * "getOptimalNewCameraMatrix" to compute the appropriate <code>newCameraMatrix</code>
 * depending on your requirements.</p>
 *
 * <p>The camera matrix and the distortion parameters can be determined using
 * "calibrateCamera". If the resolution of images is different from the
 * resolution used at the calibration stage, <em>f_x, f_y, c_x</em> and
 * <em>c_y</em> need to be scaled accordingly, while the distortion coefficients
 * remain the same.</p>
 *
 * @param src Input (distorted) image.
 * @param dst Output (corrected) image that has the same size and type as
 * <code>src</code>.
 * @param cameraMatrix Input camera matrix <em>A =
 * <p>|f_x 0 c_x|
 * |0 f_y c_y|
 * |0 0 1|
 * </em>.</p>
 * @param distCoeffs Input vector of distortion coefficients <em>(k_1, k_2, p_1,
 * p_2[, k_3[, k_4, k_5, k_6]])</em> of 4, 5, or 8 elements. If the vector is
 * NULL/empty, the zero distortion coefficients are assumed.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#undistort">org.opencv.imgproc.Imgproc.undistort</a>
 */
    public static void undistort(Mat src, Mat dst, Mat cameraMatrix, Mat distCoeffs)
    {

        undistort_1(src.nativeObj, dst.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj);

        return;
    }

    //
    // C++:  void undistortPoints(vector_Point2f src, vector_Point2f& dst, Mat cameraMatrix, Mat distCoeffs, Mat R = Mat(), Mat P = Mat())
    //

/**
 * <p>Computes the ideal point coordinates from the observed point coordinates.</p>
 *
 * <p>The function is similar to "undistort" and "initUndistortRectifyMap" but it
 * operates on a sparse set of points instead of a raster image. Also the
 * function performs a reverse transformation to"projectPoints". In case of a 3D
 * object, it does not reconstruct its 3D coordinates, but for a planar object,
 * it does, up to a translation vector, if the proper <code>R</code> is
 * specified.
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// (u,v) is the input point, (u', v') is the output point</p>
 *
 * <p>// camera_matrix=[fx 0 cx; 0 fy cy; 0 0 1]</p>
 *
 * <p>// P=[fx' 0 cx' tx; 0 fy' cy' ty; 0 0 1 tz]</p>
 *
 * <p>x" = (u - cx)/fx</p>
 *
 * <p>y" = (v - cy)/fy</p>
 *
 * <p>(x',y') = undistort(x",y",dist_coeffs)</p>
 *
 * <p>[X,Y,W]T = R*[x' y' 1]T</p>
 *
 * <p>x = X/W, y = Y/W</p>
 *
 * <p>// only performed if P=[fx' 0 cx' [tx]; 0 fy' cy' [ty]; 0 0 1 [tz]] is
 * specified</p>
 *
 * <p>u' = x*fx' + cx'</p>
 *
 * <p>v' = y*fy' + cy',</p>
 *
 * <p>where <code>undistort()</code> is an approximate iterative algorithm that
 * estimates the normalized original point coordinates out of the normalized
 * distorted point coordinates ("normalized" means that the coordinates do not
 * depend on the camera matrix).
 * </code></p>
 *
 * <p>The function can be used for both a stereo camera head or a monocular camera
 * (when R is empty).</p>
 *
 * @param src Observed point coordinates, 1xN or Nx1 2-channel (CV_32FC2 or
 * CV_64FC2).
 * @param dst Output ideal point coordinates after undistortion and reverse
 * perspective transformation. If matrix <code>P</code> is identity or omitted,
 * <code>dst</code> will contain normalized point coordinates.
 * @param cameraMatrix Camera matrix <em>
 * <p>|f_x 0 c_x|
 * |0 f_y c_y|
 * |0 0 1|
 * </em>.</p>
 * @param distCoeffs Input vector of distortion coefficients <em>(k_1, k_2, p_1,
 * p_2[, k_3[, k_4, k_5, k_6]])</em> of 4, 5, or 8 elements. If the vector is
 * NULL/empty, the zero distortion coefficients are assumed.
 * @param R Rectification transformation in the object space (3x3 matrix).
 * <code>R1</code> or <code>R2</code> computed by "stereoRectify" can be passed
 * here. If the matrix is empty, the identity transformation is used.
 * @param P New camera matrix (3x3) or new projection matrix (3x4).
 * <code>P1</code> or <code>P2</code> computed by "stereoRectify" can be passed
 * here. If the matrix is empty, the identity new camera matrix is used.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#undistortpoints">org.opencv.imgproc.Imgproc.undistortPoints</a>
 */
    public static void undistortPoints(MatOfPoint2f src, MatOfPoint2f dst, Mat cameraMatrix, Mat distCoeffs, Mat R, Mat P)
    {
        Mat src_mat = src;
        Mat dst_mat = dst;
        undistortPoints_0(src_mat.nativeObj, dst_mat.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj, R.nativeObj, P.nativeObj);

        return;
    }

/**
 * <p>Computes the ideal point coordinates from the observed point coordinates.</p>
 *
 * <p>The function is similar to "undistort" and "initUndistortRectifyMap" but it
 * operates on a sparse set of points instead of a raster image. Also the
 * function performs a reverse transformation to"projectPoints". In case of a 3D
 * object, it does not reconstruct its 3D coordinates, but for a planar object,
 * it does, up to a translation vector, if the proper <code>R</code> is
 * specified.
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// (u,v) is the input point, (u', v') is the output point</p>
 *
 * <p>// camera_matrix=[fx 0 cx; 0 fy cy; 0 0 1]</p>
 *
 * <p>// P=[fx' 0 cx' tx; 0 fy' cy' ty; 0 0 1 tz]</p>
 *
 * <p>x" = (u - cx)/fx</p>
 *
 * <p>y" = (v - cy)/fy</p>
 *
 * <p>(x',y') = undistort(x",y",dist_coeffs)</p>
 *
 * <p>[X,Y,W]T = R*[x' y' 1]T</p>
 *
 * <p>x = X/W, y = Y/W</p>
 *
 * <p>// only performed if P=[fx' 0 cx' [tx]; 0 fy' cy' [ty]; 0 0 1 [tz]] is
 * specified</p>
 *
 * <p>u' = x*fx' + cx'</p>
 *
 * <p>v' = y*fy' + cy',</p>
 *
 * <p>where <code>undistort()</code> is an approximate iterative algorithm that
 * estimates the normalized original point coordinates out of the normalized
 * distorted point coordinates ("normalized" means that the coordinates do not
 * depend on the camera matrix).
 * </code></p>
 *
 * <p>The function can be used for both a stereo camera head or a monocular camera
 * (when R is empty).</p>
 *
 * @param src Observed point coordinates, 1xN or Nx1 2-channel (CV_32FC2 or
 * CV_64FC2).
 * @param dst Output ideal point coordinates after undistortion and reverse
 * perspective transformation. If matrix <code>P</code> is identity or omitted,
 * <code>dst</code> will contain normalized point coordinates.
 * @param cameraMatrix Camera matrix <em>
 * <p>|f_x 0 c_x|
 * |0 f_y c_y|
 * |0 0 1|
 * </em>.</p>
 * @param distCoeffs Input vector of distortion coefficients <em>(k_1, k_2, p_1,
 * p_2[, k_3[, k_4, k_5, k_6]])</em> of 4, 5, or 8 elements. If the vector is
 * NULL/empty, the zero distortion coefficients are assumed.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#undistortpoints">org.opencv.imgproc.Imgproc.undistortPoints</a>
 */
    public static void undistortPoints(MatOfPoint2f src, MatOfPoint2f dst, Mat cameraMatrix, Mat distCoeffs)
    {
        Mat src_mat = src;
        Mat dst_mat = dst;
        undistortPoints_1(src_mat.nativeObj, dst_mat.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj);

        return;
    }

    //
    // C++:  void warpAffine(Mat src, Mat& dst, Mat M, Size dsize, int flags = INTER_LINEAR, int borderMode = BORDER_CONSTANT, Scalar borderValue = Scalar())
    //

/**
 * <p>Applies an affine transformation to an image.</p>
 *
 * <p>The function <code>warpAffine</code> transforms the source image using the
 * specified matrix:</p>
 *
 * <p><em>dst(x,y) = src(M _11 x + M _12 y + M _13, M _21 x + M _22 y + M _23)</em></p>
 *
 * <p>when the flag <code>WARP_INVERSE_MAP</code> is set. Otherwise, the
 * transformation is first inverted with "invertAffineTransform" and then put in
 * the formula above instead of <code>M</code>.
 * The function cannot operate in-place.</p>
 *
 * <p>Note: <code>cvGetQuadrangleSubPix</code> is similar to <code>cvWarpAffine</code>,
 * but the outliers are extrapolated using replication border mode.</p>
 *
 * @param src input image.
 * @param dst output image that has the size <code>dsize</code> and the same
 * type as <code>src</code>.
 * @param M <em>2x 3</em> transformation matrix.
 * @param dsize size of the output image.
 * @param flags combination of interpolation methods (see "resize") and the
 * optional flag <code>WARP_INVERSE_MAP</code> that means that <code>M</code> is
 * the inverse transformation (<em>dst->src</em>).
 * @param borderMode pixel extrapolation method (see "borderInterpolate"); when
 * <code>borderMode=BORDER_TRANSPARENT</code>, it means that the pixels in the
 * destination image corresponding to the "outliers" in the source image are not
 * modified by the function.
 * @param borderValue value used in case of a constant border; by default, it is
 * 0.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#warpaffine">org.opencv.imgproc.Imgproc.warpAffine</a>
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 * @see org.opencv.imgproc.Imgproc#getRectSubPix
 * @see org.opencv.imgproc.Imgproc#resize
 * @see org.opencv.core.Core#transform
 */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize, int flags, int borderMode, Scalar borderValue)
    {

        warpAffine_0(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags, borderMode, borderValue.val[0], borderValue.val[1], borderValue.val[2], borderValue.val[3]);

        return;
    }

/**
 * <p>Applies an affine transformation to an image.</p>
 *
 * <p>The function <code>warpAffine</code> transforms the source image using the
 * specified matrix:</p>
 *
 * <p><em>dst(x,y) = src(M _11 x + M _12 y + M _13, M _21 x + M _22 y + M _23)</em></p>
 *
 * <p>when the flag <code>WARP_INVERSE_MAP</code> is set. Otherwise, the
 * transformation is first inverted with "invertAffineTransform" and then put in
 * the formula above instead of <code>M</code>.
 * The function cannot operate in-place.</p>
 *
 * <p>Note: <code>cvGetQuadrangleSubPix</code> is similar to <code>cvWarpAffine</code>,
 * but the outliers are extrapolated using replication border mode.</p>
 *
 * @param src input image.
 * @param dst output image that has the size <code>dsize</code> and the same
 * type as <code>src</code>.
 * @param M <em>2x 3</em> transformation matrix.
 * @param dsize size of the output image.
 * @param flags combination of interpolation methods (see "resize") and the
 * optional flag <code>WARP_INVERSE_MAP</code> that means that <code>M</code> is
 * the inverse transformation (<em>dst->src</em>).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#warpaffine">org.opencv.imgproc.Imgproc.warpAffine</a>
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 * @see org.opencv.imgproc.Imgproc#getRectSubPix
 * @see org.opencv.imgproc.Imgproc#resize
 * @see org.opencv.core.Core#transform
 */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize, int flags)
    {

        warpAffine_1(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags);

        return;
    }

/**
 * <p>Applies an affine transformation to an image.</p>
 *
 * <p>The function <code>warpAffine</code> transforms the source image using the
 * specified matrix:</p>
 *
 * <p><em>dst(x,y) = src(M _11 x + M _12 y + M _13, M _21 x + M _22 y + M _23)</em></p>
 *
 * <p>when the flag <code>WARP_INVERSE_MAP</code> is set. Otherwise, the
 * transformation is first inverted with "invertAffineTransform" and then put in
 * the formula above instead of <code>M</code>.
 * The function cannot operate in-place.</p>
 *
 * <p>Note: <code>cvGetQuadrangleSubPix</code> is similar to <code>cvWarpAffine</code>,
 * but the outliers are extrapolated using replication border mode.</p>
 *
 * @param src input image.
 * @param dst output image that has the size <code>dsize</code> and the same
 * type as <code>src</code>.
 * @param M <em>2x 3</em> transformation matrix.
 * @param dsize size of the output image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#warpaffine">org.opencv.imgproc.Imgproc.warpAffine</a>
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.imgproc.Imgproc#warpPerspective
 * @see org.opencv.imgproc.Imgproc#getRectSubPix
 * @see org.opencv.imgproc.Imgproc#resize
 * @see org.opencv.core.Core#transform
 */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize)
    {

        warpAffine_2(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height);

        return;
    }

    //
    // C++:  void warpPerspective(Mat src, Mat& dst, Mat M, Size dsize, int flags = INTER_LINEAR, int borderMode = BORDER_CONSTANT, Scalar borderValue = Scalar())
    //

/**
 * <p>Applies a perspective transformation to an image.</p>
 *
 * <p>The function <code>warpPerspective</code> transforms the source image using
 * the specified matrix:</p>
 *
 * <p><em>dst(x,y) = src((M_11 x + M_12 y + M_13)/(M_(31) x + M_32 y +
 * M_33),&ltBR&gt(M_21 x + M_22 y + M_23)/(M_(31) x + M_32 y + M_33))</em></p>
 *
 * <p>when the flag <code>WARP_INVERSE_MAP</code> is set. Otherwise, the
 * transformation is first inverted with "invert" and then put in the formula
 * above instead of <code>M</code>.
 * The function cannot operate in-place.</p>
 *
 * @param src input image.
 * @param dst output image that has the size <code>dsize</code> and the same
 * type as <code>src</code>.
 * @param M <em>3x 3</em> transformation matrix.
 * @param dsize size of the output image.
 * @param flags combination of interpolation methods (<code>INTER_LINEAR</code>
 * or <code>INTER_NEAREST</code>) and the optional flag <code>WARP_INVERSE_MAP</code>,
 * that sets <code>M</code> as the inverse transformation (<em>dst->src</em>).
 * @param borderMode pixel extrapolation method (<code>BORDER_CONSTANT</code> or
 * <code>BORDER_REPLICATE</code>).
 * @param borderValue value used in case of a constant border; by default, it
 * equals 0.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#warpperspective">org.opencv.imgproc.Imgproc.warpPerspective</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.core.Core#perspectiveTransform
 * @see org.opencv.imgproc.Imgproc#getRectSubPix
 * @see org.opencv.imgproc.Imgproc#resize
 */
    public static void warpPerspective(Mat src, Mat dst, Mat M, Size dsize, int flags, int borderMode, Scalar borderValue)
    {

        warpPerspective_0(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags, borderMode, borderValue.val[0], borderValue.val[1], borderValue.val[2], borderValue.val[3]);

        return;
    }

/**
 * <p>Applies a perspective transformation to an image.</p>
 *
 * <p>The function <code>warpPerspective</code> transforms the source image using
 * the specified matrix:</p>
 *
 * <p><em>dst(x,y) = src((M_11 x + M_12 y + M_13)/(M_(31) x + M_32 y +
 * M_33),&ltBR&gt(M_21 x + M_22 y + M_23)/(M_(31) x + M_32 y + M_33))</em></p>
 *
 * <p>when the flag <code>WARP_INVERSE_MAP</code> is set. Otherwise, the
 * transformation is first inverted with "invert" and then put in the formula
 * above instead of <code>M</code>.
 * The function cannot operate in-place.</p>
 *
 * @param src input image.
 * @param dst output image that has the size <code>dsize</code> and the same
 * type as <code>src</code>.
 * @param M <em>3x 3</em> transformation matrix.
 * @param dsize size of the output image.
 * @param flags combination of interpolation methods (<code>INTER_LINEAR</code>
 * or <code>INTER_NEAREST</code>) and the optional flag <code>WARP_INVERSE_MAP</code>,
 * that sets <code>M</code> as the inverse transformation (<em>dst->src</em>).
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#warpperspective">org.opencv.imgproc.Imgproc.warpPerspective</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.core.Core#perspectiveTransform
 * @see org.opencv.imgproc.Imgproc#getRectSubPix
 * @see org.opencv.imgproc.Imgproc#resize
 */
    public static void warpPerspective(Mat src, Mat dst, Mat M, Size dsize, int flags)
    {

        warpPerspective_1(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags);

        return;
    }

/**
 * <p>Applies a perspective transformation to an image.</p>
 *
 * <p>The function <code>warpPerspective</code> transforms the source image using
 * the specified matrix:</p>
 *
 * <p><em>dst(x,y) = src((M_11 x + M_12 y + M_13)/(M_(31) x + M_32 y +
 * M_33),&ltBR&gt(M_21 x + M_22 y + M_23)/(M_(31) x + M_32 y + M_33))</em></p>
 *
 * <p>when the flag <code>WARP_INVERSE_MAP</code> is set. Otherwise, the
 * transformation is first inverted with "invert" and then put in the formula
 * above instead of <code>M</code>.
 * The function cannot operate in-place.</p>
 *
 * @param src input image.
 * @param dst output image that has the size <code>dsize</code> and the same
 * type as <code>src</code>.
 * @param M <em>3x 3</em> transformation matrix.
 * @param dsize size of the output image.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/geometric_transformations.html#warpperspective">org.opencv.imgproc.Imgproc.warpPerspective</a>
 * @see org.opencv.imgproc.Imgproc#warpAffine
 * @see org.opencv.imgproc.Imgproc#remap
 * @see org.opencv.core.Core#perspectiveTransform
 * @see org.opencv.imgproc.Imgproc#getRectSubPix
 * @see org.opencv.imgproc.Imgproc#resize
 */
    public static void warpPerspective(Mat src, Mat dst, Mat M, Size dsize)
    {

        warpPerspective_2(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height);

        return;
    }

    //
    // C++:  void watershed(Mat image, Mat& markers)
    //

/**
 * <p>Performs a marker-based image segmentation using the watershed algorithm.</p>
 *
 * <p>The function implements one of the variants of watershed, non-parametric
 * marker-based segmentation algorithm, described in [Meyer92].</p>
 *
 * <p>Before passing the image to the function, you have to roughly outline the
 * desired regions in the image <code>markers</code> with positive
 * (<code>>0</code>) indices. So, every region is represented as one or more
 * connected components with the pixel values 1, 2, 3, and so on. Such markers
 * can be retrieved from a binary mask using "findContours" and "drawContours"
 * (see the <code>watershed.cpp</code> demo). The markers are "seeds" of the
 * future image regions. All the other pixels in <code>markers</code>, whose
 * relation to the outlined regions is not known and should be defined by the
 * algorithm, should be set to 0's. In the function output, each pixel in
 * markers is set to a value of the "seed" components or to -1 at boundaries
 * between the regions.</p>
 *
 * <p>Visual demonstration and usage example of the function can be found in the
 * OpenCV samples directory (see the <code>watershed.cpp</code> demo).</p>
 *
 * <p>Note: Any two neighbor connected components are not necessarily separated by
 * a watershed boundary (-1's pixels); for example, they can touch each other in
 * the initial marker image passed to the function.</p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example using the watershed algorithm can be found at
 * opencv_source_code/samples/cpp/watershed.cpp
 *   <li> (Python) An example using the watershed algorithm can be found at
 * opencv_source_code/samples/python2/watershed.py
 * </ul>
 *
 * @param image Input 8-bit 3-channel image.
 * @param markers Input/output 32-bit single-channel image (map) of markers. It
 * should have the same size as <code>image</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/imgproc/doc/miscellaneous_transformations.html#watershed">org.opencv.imgproc.Imgproc.watershed</a>
 * @see org.opencv.imgproc.Imgproc#findContours
 */
    public static void watershed(Mat image, Mat markers)
    {

        watershed_0(image.nativeObj, markers.nativeObj);

        return;
    }




    // C++:  void Canny(Mat image, Mat& edges, double threshold1, double threshold2, int apertureSize = 3, bool L2gradient = false)
    private static native void Canny_0(long image_nativeObj, long edges_nativeObj, double threshold1, double threshold2, int apertureSize, boolean L2gradient);
    private static native void Canny_1(long image_nativeObj, long edges_nativeObj, double threshold1, double threshold2);

    // C++:  void GaussianBlur(Mat src, Mat& dst, Size ksize, double sigmaX, double sigmaY = 0, int borderType = BORDER_DEFAULT)
    private static native void GaussianBlur_0(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double sigmaX, double sigmaY, int borderType);
    private static native void GaussianBlur_1(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double sigmaX, double sigmaY);
    private static native void GaussianBlur_2(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double sigmaX);

    // C++:  void HoughCircles(Mat image, Mat& circles, int method, double dp, double minDist, double param1 = 100, double param2 = 100, int minRadius = 0, int maxRadius = 0)
    private static native void HoughCircles_0(long image_nativeObj, long circles_nativeObj, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius);
    private static native void HoughCircles_1(long image_nativeObj, long circles_nativeObj, int method, double dp, double minDist);

    // C++:  void HoughLines(Mat image, Mat& lines, double rho, double theta, int threshold, double srn = 0, double stn = 0)
    private static native void HoughLines_0(long image_nativeObj, long lines_nativeObj, double rho, double theta, int threshold, double srn, double stn);
    private static native void HoughLines_1(long image_nativeObj, long lines_nativeObj, double rho, double theta, int threshold);

    // C++:  void HoughLinesP(Mat image, Mat& lines, double rho, double theta, int threshold, double minLineLength = 0, double maxLineGap = 0)
    private static native void HoughLinesP_0(long image_nativeObj, long lines_nativeObj, double rho, double theta, int threshold, double minLineLength, double maxLineGap);
    private static native void HoughLinesP_1(long image_nativeObj, long lines_nativeObj, double rho, double theta, int threshold);

    // C++:  void HuMoments(Moments m, Mat& hu)
    private static native void HuMoments_0(long m_nativeObj, long hu_nativeObj);

    // C++:  void Laplacian(Mat src, Mat& dst, int ddepth, int ksize = 1, double scale = 1, double delta = 0, int borderType = BORDER_DEFAULT)
    private static native void Laplacian_0(long src_nativeObj, long dst_nativeObj, int ddepth, int ksize, double scale, double delta, int borderType);
    private static native void Laplacian_1(long src_nativeObj, long dst_nativeObj, int ddepth, int ksize, double scale, double delta);
    private static native void Laplacian_2(long src_nativeObj, long dst_nativeObj, int ddepth);

    // C++:  double PSNR(Mat src1, Mat src2)
    private static native double PSNR_0(long src1_nativeObj, long src2_nativeObj);

    // C++:  void Scharr(Mat src, Mat& dst, int ddepth, int dx, int dy, double scale = 1, double delta = 0, int borderType = BORDER_DEFAULT)
    private static native void Scharr_0(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy, double scale, double delta, int borderType);
    private static native void Scharr_1(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy, double scale, double delta);
    private static native void Scharr_2(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy);

    // C++:  void Sobel(Mat src, Mat& dst, int ddepth, int dx, int dy, int ksize = 3, double scale = 1, double delta = 0, int borderType = BORDER_DEFAULT)
    private static native void Sobel_0(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy, int ksize, double scale, double delta, int borderType);
    private static native void Sobel_1(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy, int ksize, double scale, double delta);
    private static native void Sobel_2(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy);

    // C++:  void accumulate(Mat src, Mat& dst, Mat mask = Mat())
    private static native void accumulate_0(long src_nativeObj, long dst_nativeObj, long mask_nativeObj);
    private static native void accumulate_1(long src_nativeObj, long dst_nativeObj);

    // C++:  void accumulateProduct(Mat src1, Mat src2, Mat& dst, Mat mask = Mat())
    private static native void accumulateProduct_0(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj, long mask_nativeObj);
    private static native void accumulateProduct_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    // C++:  void accumulateSquare(Mat src, Mat& dst, Mat mask = Mat())
    private static native void accumulateSquare_0(long src_nativeObj, long dst_nativeObj, long mask_nativeObj);
    private static native void accumulateSquare_1(long src_nativeObj, long dst_nativeObj);

    // C++:  void accumulateWeighted(Mat src, Mat& dst, double alpha, Mat mask = Mat())
    private static native void accumulateWeighted_0(long src_nativeObj, long dst_nativeObj, double alpha, long mask_nativeObj);
    private static native void accumulateWeighted_1(long src_nativeObj, long dst_nativeObj, double alpha);

    // C++:  void adaptiveBilateralFilter(Mat src, Mat& dst, Size ksize, double sigmaSpace, double maxSigmaColor = 20.0, Point anchor = Point(-1, -1), int borderType = BORDER_DEFAULT)
    private static native void adaptiveBilateralFilter_0(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double sigmaSpace, double maxSigmaColor, double anchor_x, double anchor_y, int borderType);
    private static native void adaptiveBilateralFilter_1(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double sigmaSpace, double maxSigmaColor, double anchor_x, double anchor_y);
    private static native void adaptiveBilateralFilter_2(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double sigmaSpace);

    // C++:  void adaptiveThreshold(Mat src, Mat& dst, double maxValue, int adaptiveMethod, int thresholdType, int blockSize, double C)
    private static native void adaptiveThreshold_0(long src_nativeObj, long dst_nativeObj, double maxValue, int adaptiveMethod, int thresholdType, int blockSize, double C);

    // C++:  void approxPolyDP(vector_Point2f curve, vector_Point2f& approxCurve, double epsilon, bool closed)
    private static native void approxPolyDP_0(long curve_mat_nativeObj, long approxCurve_mat_nativeObj, double epsilon, boolean closed);

    // C++:  double arcLength(vector_Point2f curve, bool closed)
    private static native double arcLength_0(long curve_mat_nativeObj, boolean closed);

    // C++:  void bilateralFilter(Mat src, Mat& dst, int d, double sigmaColor, double sigmaSpace, int borderType = BORDER_DEFAULT)
    private static native void bilateralFilter_0(long src_nativeObj, long dst_nativeObj, int d, double sigmaColor, double sigmaSpace, int borderType);
    private static native void bilateralFilter_1(long src_nativeObj, long dst_nativeObj, int d, double sigmaColor, double sigmaSpace);

    // C++:  void blur(Mat src, Mat& dst, Size ksize, Point anchor = Point(-1,-1), int borderType = BORDER_DEFAULT)
    private static native void blur_0(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double anchor_x, double anchor_y, int borderType);
    private static native void blur_1(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height, double anchor_x, double anchor_y);
    private static native void blur_2(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height);

    // C++:  int borderInterpolate(int p, int len, int borderType)
    private static native int borderInterpolate_0(int p, int len, int borderType);

    // C++:  Rect boundingRect(vector_Point points)
    private static native double[] boundingRect_0(long points_mat_nativeObj);

    // C++:  void boxFilter(Mat src, Mat& dst, int ddepth, Size ksize, Point anchor = Point(-1,-1), bool normalize = true, int borderType = BORDER_DEFAULT)
    private static native void boxFilter_0(long src_nativeObj, long dst_nativeObj, int ddepth, double ksize_width, double ksize_height, double anchor_x, double anchor_y, boolean normalize, int borderType);
    private static native void boxFilter_1(long src_nativeObj, long dst_nativeObj, int ddepth, double ksize_width, double ksize_height, double anchor_x, double anchor_y, boolean normalize);
    private static native void boxFilter_2(long src_nativeObj, long dst_nativeObj, int ddepth, double ksize_width, double ksize_height);

    // C++:  void calcBackProject(vector_Mat images, vector_int channels, Mat hist, Mat& dst, vector_float ranges, double scale)
    private static native void calcBackProject_0(long images_mat_nativeObj, long channels_mat_nativeObj, long hist_nativeObj, long dst_nativeObj, long ranges_mat_nativeObj, double scale);

    // C++:  void calcHist(vector_Mat images, vector_int channels, Mat mask, Mat& hist, vector_int histSize, vector_float ranges, bool accumulate = false)
    private static native void calcHist_0(long images_mat_nativeObj, long channels_mat_nativeObj, long mask_nativeObj, long hist_nativeObj, long histSize_mat_nativeObj, long ranges_mat_nativeObj, boolean accumulate);
    private static native void calcHist_1(long images_mat_nativeObj, long channels_mat_nativeObj, long mask_nativeObj, long hist_nativeObj, long histSize_mat_nativeObj, long ranges_mat_nativeObj);

    // C++:  double compareHist(Mat H1, Mat H2, int method)
    private static native double compareHist_0(long H1_nativeObj, long H2_nativeObj, int method);

    // C++:  double contourArea(Mat contour, bool oriented = false)
    private static native double contourArea_0(long contour_nativeObj, boolean oriented);
    private static native double contourArea_1(long contour_nativeObj);

    // C++:  void convertMaps(Mat map1, Mat map2, Mat& dstmap1, Mat& dstmap2, int dstmap1type, bool nninterpolation = false)
    private static native void convertMaps_0(long map1_nativeObj, long map2_nativeObj, long dstmap1_nativeObj, long dstmap2_nativeObj, int dstmap1type, boolean nninterpolation);
    private static native void convertMaps_1(long map1_nativeObj, long map2_nativeObj, long dstmap1_nativeObj, long dstmap2_nativeObj, int dstmap1type);

    // C++:  void convexHull(vector_Point points, vector_int& hull, bool clockwise = false,  _hidden_  returnPoints = true)
    private static native void convexHull_0(long points_mat_nativeObj, long hull_mat_nativeObj, boolean clockwise);
    private static native void convexHull_1(long points_mat_nativeObj, long hull_mat_nativeObj);

    // C++:  void convexityDefects(vector_Point contour, vector_int convexhull, vector_Vec4i& convexityDefects)
    private static native void convexityDefects_0(long contour_mat_nativeObj, long convexhull_mat_nativeObj, long convexityDefects_mat_nativeObj);

    // C++:  void copyMakeBorder(Mat src, Mat& dst, int top, int bottom, int left, int right, int borderType, Scalar value = Scalar())
    private static native void copyMakeBorder_0(long src_nativeObj, long dst_nativeObj, int top, int bottom, int left, int right, int borderType, double value_val0, double value_val1, double value_val2, double value_val3);
    private static native void copyMakeBorder_1(long src_nativeObj, long dst_nativeObj, int top, int bottom, int left, int right, int borderType);

    // C++:  void cornerEigenValsAndVecs(Mat src, Mat& dst, int blockSize, int ksize, int borderType = BORDER_DEFAULT)
    private static native void cornerEigenValsAndVecs_0(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize, int borderType);
    private static native void cornerEigenValsAndVecs_1(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize);

    // C++:  void cornerHarris(Mat src, Mat& dst, int blockSize, int ksize, double k, int borderType = BORDER_DEFAULT)
    private static native void cornerHarris_0(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize, double k, int borderType);
    private static native void cornerHarris_1(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize, double k);

    // C++:  void cornerMinEigenVal(Mat src, Mat& dst, int blockSize, int ksize = 3, int borderType = BORDER_DEFAULT)
    private static native void cornerMinEigenVal_0(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize, int borderType);
    private static native void cornerMinEigenVal_1(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize);
    private static native void cornerMinEigenVal_2(long src_nativeObj, long dst_nativeObj, int blockSize);

    // C++:  void cornerSubPix(Mat image, vector_Point2f& corners, Size winSize, Size zeroZone, TermCriteria criteria)
    private static native void cornerSubPix_0(long image_nativeObj, long corners_mat_nativeObj, double winSize_width, double winSize_height, double zeroZone_width, double zeroZone_height, int criteria_type, int criteria_maxCount, double criteria_epsilon);

    // C++:  void createHanningWindow(Mat& dst, Size winSize, int type)
    private static native void createHanningWindow_0(long dst_nativeObj, double winSize_width, double winSize_height, int type);

    // C++:  void cvtColor(Mat src, Mat& dst, int code, int dstCn = 0)
    private static native void cvtColor_0(long src_nativeObj, long dst_nativeObj, int code, int dstCn);
    private static native void cvtColor_1(long src_nativeObj, long dst_nativeObj, int code);

    // C++:  void dilate(Mat src, Mat& dst, Mat kernel, Point anchor = Point(-1,-1), int iterations = 1, int borderType = BORDER_CONSTANT, Scalar borderValue = morphologyDefaultBorderValue())
    private static native void dilate_0(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj, double anchor_x, double anchor_y, int iterations, int borderType, double borderValue_val0, double borderValue_val1, double borderValue_val2, double borderValue_val3);
    private static native void dilate_1(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj, double anchor_x, double anchor_y, int iterations);
    private static native void dilate_2(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj);

    // C++:  void distanceTransform(Mat src, Mat& dst, int distanceType, int maskSize)
    private static native void distanceTransform_0(long src_nativeObj, long dst_nativeObj, int distanceType, int maskSize);

    // C++:  void distanceTransform(Mat src, Mat& dst, Mat& labels, int distanceType, int maskSize, int labelType = DIST_LABEL_CCOMP)
    private static native void distanceTransformWithLabels_0(long src_nativeObj, long dst_nativeObj, long labels_nativeObj, int distanceType, int maskSize, int labelType);
    private static native void distanceTransformWithLabels_1(long src_nativeObj, long dst_nativeObj, long labels_nativeObj, int distanceType, int maskSize);

    // C++:  void drawContours(Mat& image, vector_vector_Point contours, int contourIdx, Scalar color, int thickness = 1, int lineType = 8, Mat hierarchy = Mat(), int maxLevel = INT_MAX, Point offset = Point())
    private static native void drawContours_0(long image_nativeObj, long contours_mat_nativeObj, int contourIdx, double color_val0, double color_val1, double color_val2, double color_val3, int thickness, int lineType, long hierarchy_nativeObj, int maxLevel, double offset_x, double offset_y);
    private static native void drawContours_1(long image_nativeObj, long contours_mat_nativeObj, int contourIdx, double color_val0, double color_val1, double color_val2, double color_val3, int thickness);
    private static native void drawContours_2(long image_nativeObj, long contours_mat_nativeObj, int contourIdx, double color_val0, double color_val1, double color_val2, double color_val3);

    // C++:  void equalizeHist(Mat src, Mat& dst)
    private static native void equalizeHist_0(long src_nativeObj, long dst_nativeObj);

    // C++:  void erode(Mat src, Mat& dst, Mat kernel, Point anchor = Point(-1,-1), int iterations = 1, int borderType = BORDER_CONSTANT, Scalar borderValue = morphologyDefaultBorderValue())
    private static native void erode_0(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj, double anchor_x, double anchor_y, int iterations, int borderType, double borderValue_val0, double borderValue_val1, double borderValue_val2, double borderValue_val3);
    private static native void erode_1(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj, double anchor_x, double anchor_y, int iterations);
    private static native void erode_2(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj);

    // C++:  void filter2D(Mat src, Mat& dst, int ddepth, Mat kernel, Point anchor = Point(-1,-1), double delta = 0, int borderType = BORDER_DEFAULT)
    private static native void filter2D_0(long src_nativeObj, long dst_nativeObj, int ddepth, long kernel_nativeObj, double anchor_x, double anchor_y, double delta, int borderType);
    private static native void filter2D_1(long src_nativeObj, long dst_nativeObj, int ddepth, long kernel_nativeObj, double anchor_x, double anchor_y, double delta);
    private static native void filter2D_2(long src_nativeObj, long dst_nativeObj, int ddepth, long kernel_nativeObj);

    // C++:  void findContours(Mat& image, vector_vector_Point& contours, Mat& hierarchy, int mode, int method, Point offset = Point())
    private static native void findContours_0(long image_nativeObj, long contours_mat_nativeObj, long hierarchy_nativeObj, int mode, int method, double offset_x, double offset_y);
    private static native void findContours_1(long image_nativeObj, long contours_mat_nativeObj, long hierarchy_nativeObj, int mode, int method);

    // C++:  RotatedRect fitEllipse(vector_Point2f points)
    private static native double[] fitEllipse_0(long points_mat_nativeObj);

    // C++:  void fitLine(Mat points, Mat& line, int distType, double param, double reps, double aeps)
    private static native void fitLine_0(long points_nativeObj, long line_nativeObj, int distType, double param, double reps, double aeps);

    // C++:  int floodFill(Mat& image, Mat& mask, Point seedPoint, Scalar newVal, Rect* rect = 0, Scalar loDiff = Scalar(), Scalar upDiff = Scalar(), int flags = 4)
    private static native int floodFill_0(long image_nativeObj, long mask_nativeObj, double seedPoint_x, double seedPoint_y, double newVal_val0, double newVal_val1, double newVal_val2, double newVal_val3, double[] rect_out, double loDiff_val0, double loDiff_val1, double loDiff_val2, double loDiff_val3, double upDiff_val0, double upDiff_val1, double upDiff_val2, double upDiff_val3, int flags);
    private static native int floodFill_1(long image_nativeObj, long mask_nativeObj, double seedPoint_x, double seedPoint_y, double newVal_val0, double newVal_val1, double newVal_val2, double newVal_val3);

    // C++:  Mat getAffineTransform(vector_Point2f src, vector_Point2f dst)
    private static native long getAffineTransform_0(long src_mat_nativeObj, long dst_mat_nativeObj);

    // C++:  Mat getDefaultNewCameraMatrix(Mat cameraMatrix, Size imgsize = Size(), bool centerPrincipalPoint = false)
    private static native long getDefaultNewCameraMatrix_0(long cameraMatrix_nativeObj, double imgsize_width, double imgsize_height, boolean centerPrincipalPoint);
    private static native long getDefaultNewCameraMatrix_1(long cameraMatrix_nativeObj);

    // C++:  void getDerivKernels(Mat& kx, Mat& ky, int dx, int dy, int ksize, bool normalize = false, int ktype = CV_32F)
    private static native void getDerivKernels_0(long kx_nativeObj, long ky_nativeObj, int dx, int dy, int ksize, boolean normalize, int ktype);
    private static native void getDerivKernels_1(long kx_nativeObj, long ky_nativeObj, int dx, int dy, int ksize);

    // C++:  Mat getGaborKernel(Size ksize, double sigma, double theta, double lambd, double gamma, double psi = CV_PI*0.5, int ktype = CV_64F)
    private static native long getGaborKernel_0(double ksize_width, double ksize_height, double sigma, double theta, double lambd, double gamma, double psi, int ktype);
    private static native long getGaborKernel_1(double ksize_width, double ksize_height, double sigma, double theta, double lambd, double gamma);

    // C++:  Mat getGaussianKernel(int ksize, double sigma, int ktype = CV_64F)
    private static native long getGaussianKernel_0(int ksize, double sigma, int ktype);
    private static native long getGaussianKernel_1(int ksize, double sigma);

    // C++:  Mat getPerspectiveTransform(Mat src, Mat dst)
    private static native long getPerspectiveTransform_0(long src_nativeObj, long dst_nativeObj);

    // C++:  void getRectSubPix(Mat image, Size patchSize, Point2f center, Mat& patch, int patchType = -1)
    private static native void getRectSubPix_0(long image_nativeObj, double patchSize_width, double patchSize_height, double center_x, double center_y, long patch_nativeObj, int patchType);
    private static native void getRectSubPix_1(long image_nativeObj, double patchSize_width, double patchSize_height, double center_x, double center_y, long patch_nativeObj);

    // C++:  Mat getRotationMatrix2D(Point2f center, double angle, double scale)
    private static native long getRotationMatrix2D_0(double center_x, double center_y, double angle, double scale);

    // C++:  Mat getStructuringElement(int shape, Size ksize, Point anchor = Point(-1,-1))
    private static native long getStructuringElement_0(int shape, double ksize_width, double ksize_height, double anchor_x, double anchor_y);
    private static native long getStructuringElement_1(int shape, double ksize_width, double ksize_height);

    // C++:  void goodFeaturesToTrack(Mat image, vector_Point& corners, int maxCorners, double qualityLevel, double minDistance, Mat mask = Mat(), int blockSize = 3, bool useHarrisDetector = false, double k = 0.04)
    private static native void goodFeaturesToTrack_0(long image_nativeObj, long corners_mat_nativeObj, int maxCorners, double qualityLevel, double minDistance, long mask_nativeObj, int blockSize, boolean useHarrisDetector, double k);
    private static native void goodFeaturesToTrack_1(long image_nativeObj, long corners_mat_nativeObj, int maxCorners, double qualityLevel, double minDistance);

    // C++:  void grabCut(Mat img, Mat& mask, Rect rect, Mat& bgdModel, Mat& fgdModel, int iterCount, int mode = GC_EVAL)
    private static native void grabCut_0(long img_nativeObj, long mask_nativeObj, int rect_x, int rect_y, int rect_width, int rect_height, long bgdModel_nativeObj, long fgdModel_nativeObj, int iterCount, int mode);
    private static native void grabCut_1(long img_nativeObj, long mask_nativeObj, int rect_x, int rect_y, int rect_width, int rect_height, long bgdModel_nativeObj, long fgdModel_nativeObj, int iterCount);

    // C++:  void initUndistortRectifyMap(Mat cameraMatrix, Mat distCoeffs, Mat R, Mat newCameraMatrix, Size size, int m1type, Mat& map1, Mat& map2)
    private static native void initUndistortRectifyMap_0(long cameraMatrix_nativeObj, long distCoeffs_nativeObj, long R_nativeObj, long newCameraMatrix_nativeObj, double size_width, double size_height, int m1type, long map1_nativeObj, long map2_nativeObj);

    // C++:  float initWideAngleProjMap(Mat cameraMatrix, Mat distCoeffs, Size imageSize, int destImageWidth, int m1type, Mat& map1, Mat& map2, int projType = PROJ_SPHERICAL_EQRECT, double alpha = 0)
    private static native float initWideAngleProjMap_0(long cameraMatrix_nativeObj, long distCoeffs_nativeObj, double imageSize_width, double imageSize_height, int destImageWidth, int m1type, long map1_nativeObj, long map2_nativeObj, int projType, double alpha);
    private static native float initWideAngleProjMap_1(long cameraMatrix_nativeObj, long distCoeffs_nativeObj, double imageSize_width, double imageSize_height, int destImageWidth, int m1type, long map1_nativeObj, long map2_nativeObj);

    // C++:  void integral(Mat src, Mat& sum, int sdepth = -1)
    private static native void integral_0(long src_nativeObj, long sum_nativeObj, int sdepth);
    private static native void integral_1(long src_nativeObj, long sum_nativeObj);

    // C++:  void integral(Mat src, Mat& sum, Mat& sqsum, int sdepth = -1)
    private static native void integral2_0(long src_nativeObj, long sum_nativeObj, long sqsum_nativeObj, int sdepth);
    private static native void integral2_1(long src_nativeObj, long sum_nativeObj, long sqsum_nativeObj);

    // C++:  void integral(Mat src, Mat& sum, Mat& sqsum, Mat& tilted, int sdepth = -1)
    private static native void integral3_0(long src_nativeObj, long sum_nativeObj, long sqsum_nativeObj, long tilted_nativeObj, int sdepth);
    private static native void integral3_1(long src_nativeObj, long sum_nativeObj, long sqsum_nativeObj, long tilted_nativeObj);

    // C++:  float intersectConvexConvex(Mat _p1, Mat _p2, Mat& _p12, bool handleNested = true)
    private static native float intersectConvexConvex_0(long _p1_nativeObj, long _p2_nativeObj, long _p12_nativeObj, boolean handleNested);
    private static native float intersectConvexConvex_1(long _p1_nativeObj, long _p2_nativeObj, long _p12_nativeObj);

    // C++:  void invertAffineTransform(Mat M, Mat& iM)
    private static native void invertAffineTransform_0(long M_nativeObj, long iM_nativeObj);

    // C++:  bool isContourConvex(vector_Point contour)
    private static native boolean isContourConvex_0(long contour_mat_nativeObj);

    // C++:  double matchShapes(Mat contour1, Mat contour2, int method, double parameter)
    private static native double matchShapes_0(long contour1_nativeObj, long contour2_nativeObj, int method, double parameter);

    // C++:  void matchTemplate(Mat image, Mat templ, Mat& result, int method)
    private static native void matchTemplate_0(long image_nativeObj, long templ_nativeObj, long result_nativeObj, int method);

    // C++:  void medianBlur(Mat src, Mat& dst, int ksize)
    private static native void medianBlur_0(long src_nativeObj, long dst_nativeObj, int ksize);

    // C++:  RotatedRect minAreaRect(vector_Point2f points)
    private static native double[] minAreaRect_0(long points_mat_nativeObj);

    // C++:  void minEnclosingCircle(vector_Point2f points, Point2f& center, float& radius)
    private static native void minEnclosingCircle_0(long points_mat_nativeObj, double[] center_out, double[] radius_out);

    // C++:  Moments moments(Mat array, bool binaryImage = false)
    private static native long moments_0(long array_nativeObj, boolean binaryImage);
    private static native long moments_1(long array_nativeObj);

    // C++:  void morphologyEx(Mat src, Mat& dst, int op, Mat kernel, Point anchor = Point(-1,-1), int iterations = 1, int borderType = BORDER_CONSTANT, Scalar borderValue = morphologyDefaultBorderValue())
    private static native void morphologyEx_0(long src_nativeObj, long dst_nativeObj, int op, long kernel_nativeObj, double anchor_x, double anchor_y, int iterations, int borderType, double borderValue_val0, double borderValue_val1, double borderValue_val2, double borderValue_val3);
    private static native void morphologyEx_1(long src_nativeObj, long dst_nativeObj, int op, long kernel_nativeObj, double anchor_x, double anchor_y, int iterations);
    private static native void morphologyEx_2(long src_nativeObj, long dst_nativeObj, int op, long kernel_nativeObj);

    // C++:  Point2d phaseCorrelate(Mat src1, Mat src2, Mat window = Mat())
    private static native double[] phaseCorrelate_0(long src1_nativeObj, long src2_nativeObj, long window_nativeObj);
    private static native double[] phaseCorrelate_1(long src1_nativeObj, long src2_nativeObj);

    // C++:  Point2d phaseCorrelateRes(Mat src1, Mat src2, Mat window, double* response = 0)
    private static native double[] phaseCorrelateRes_0(long src1_nativeObj, long src2_nativeObj, long window_nativeObj, double[] response_out);
    private static native double[] phaseCorrelateRes_1(long src1_nativeObj, long src2_nativeObj, long window_nativeObj);

    // C++:  double pointPolygonTest(vector_Point2f contour, Point2f pt, bool measureDist)
    private static native double pointPolygonTest_0(long contour_mat_nativeObj, double pt_x, double pt_y, boolean measureDist);

    // C++:  void preCornerDetect(Mat src, Mat& dst, int ksize, int borderType = BORDER_DEFAULT)
    private static native void preCornerDetect_0(long src_nativeObj, long dst_nativeObj, int ksize, int borderType);
    private static native void preCornerDetect_1(long src_nativeObj, long dst_nativeObj, int ksize);

    // C++:  void pyrDown(Mat src, Mat& dst, Size dstsize = Size(), int borderType = BORDER_DEFAULT)
    private static native void pyrDown_0(long src_nativeObj, long dst_nativeObj, double dstsize_width, double dstsize_height, int borderType);
    private static native void pyrDown_1(long src_nativeObj, long dst_nativeObj, double dstsize_width, double dstsize_height);
    private static native void pyrDown_2(long src_nativeObj, long dst_nativeObj);

    // C++:  void pyrMeanShiftFiltering(Mat src, Mat& dst, double sp, double sr, int maxLevel = 1, TermCriteria termcrit = TermCriteria( TermCriteria::MAX_ITER+TermCriteria::EPS,5,1))
    private static native void pyrMeanShiftFiltering_0(long src_nativeObj, long dst_nativeObj, double sp, double sr, int maxLevel, int termcrit_type, int termcrit_maxCount, double termcrit_epsilon);
    private static native void pyrMeanShiftFiltering_1(long src_nativeObj, long dst_nativeObj, double sp, double sr);

    // C++:  void pyrUp(Mat src, Mat& dst, Size dstsize = Size(), int borderType = BORDER_DEFAULT)
    private static native void pyrUp_0(long src_nativeObj, long dst_nativeObj, double dstsize_width, double dstsize_height, int borderType);
    private static native void pyrUp_1(long src_nativeObj, long dst_nativeObj, double dstsize_width, double dstsize_height);
    private static native void pyrUp_2(long src_nativeObj, long dst_nativeObj);

    // C++:  void remap(Mat src, Mat& dst, Mat map1, Mat map2, int interpolation, int borderMode = BORDER_CONSTANT, Scalar borderValue = Scalar())
    private static native void remap_0(long src_nativeObj, long dst_nativeObj, long map1_nativeObj, long map2_nativeObj, int interpolation, int borderMode, double borderValue_val0, double borderValue_val1, double borderValue_val2, double borderValue_val3);
    private static native void remap_1(long src_nativeObj, long dst_nativeObj, long map1_nativeObj, long map2_nativeObj, int interpolation);

    // C++:  void resize(Mat src, Mat& dst, Size dsize, double fx = 0, double fy = 0, int interpolation = INTER_LINEAR)
    private static native void resize_0(long src_nativeObj, long dst_nativeObj, double dsize_width, double dsize_height, double fx, double fy, int interpolation);
    private static native void resize_1(long src_nativeObj, long dst_nativeObj, double dsize_width, double dsize_height);

    // C++:  void sepFilter2D(Mat src, Mat& dst, int ddepth, Mat kernelX, Mat kernelY, Point anchor = Point(-1,-1), double delta = 0, int borderType = BORDER_DEFAULT)
    private static native void sepFilter2D_0(long src_nativeObj, long dst_nativeObj, int ddepth, long kernelX_nativeObj, long kernelY_nativeObj, double anchor_x, double anchor_y, double delta, int borderType);
    private static native void sepFilter2D_1(long src_nativeObj, long dst_nativeObj, int ddepth, long kernelX_nativeObj, long kernelY_nativeObj, double anchor_x, double anchor_y, double delta);
    private static native void sepFilter2D_2(long src_nativeObj, long dst_nativeObj, int ddepth, long kernelX_nativeObj, long kernelY_nativeObj);

    // C++:  double threshold(Mat src, Mat& dst, double thresh, double maxval, int type)
    private static native double threshold_0(long src_nativeObj, long dst_nativeObj, double thresh, double maxval, int type);

    // C++:  void undistort(Mat src, Mat& dst, Mat cameraMatrix, Mat distCoeffs, Mat newCameraMatrix = Mat())
    private static native void undistort_0(long src_nativeObj, long dst_nativeObj, long cameraMatrix_nativeObj, long distCoeffs_nativeObj, long newCameraMatrix_nativeObj);
    private static native void undistort_1(long src_nativeObj, long dst_nativeObj, long cameraMatrix_nativeObj, long distCoeffs_nativeObj);

    // C++:  void undistortPoints(vector_Point2f src, vector_Point2f& dst, Mat cameraMatrix, Mat distCoeffs, Mat R = Mat(), Mat P = Mat())
    private static native void undistortPoints_0(long src_mat_nativeObj, long dst_mat_nativeObj, long cameraMatrix_nativeObj, long distCoeffs_nativeObj, long R_nativeObj, long P_nativeObj);
    private static native void undistortPoints_1(long src_mat_nativeObj, long dst_mat_nativeObj, long cameraMatrix_nativeObj, long distCoeffs_nativeObj);

    // C++:  void warpAffine(Mat src, Mat& dst, Mat M, Size dsize, int flags = INTER_LINEAR, int borderMode = BORDER_CONSTANT, Scalar borderValue = Scalar())
    private static native void warpAffine_0(long src_nativeObj, long dst_nativeObj, long M_nativeObj, double dsize_width, double dsize_height, int flags, int borderMode, double borderValue_val0, double borderValue_val1, double borderValue_val2, double borderValue_val3);
    private static native void warpAffine_1(long src_nativeObj, long dst_nativeObj, long M_nativeObj, double dsize_width, double dsize_height, int flags);
    private static native void warpAffine_2(long src_nativeObj, long dst_nativeObj, long M_nativeObj, double dsize_width, double dsize_height);

    // C++:  void warpPerspective(Mat src, Mat& dst, Mat M, Size dsize, int flags = INTER_LINEAR, int borderMode = BORDER_CONSTANT, Scalar borderValue = Scalar())
    private static native void warpPerspective_0(long src_nativeObj, long dst_nativeObj, long M_nativeObj, double dsize_width, double dsize_height, int flags, int borderMode, double borderValue_val0, double borderValue_val1, double borderValue_val2, double borderValue_val3);
    private static native void warpPerspective_1(long src_nativeObj, long dst_nativeObj, long M_nativeObj, double dsize_width, double dsize_height, int flags);
    private static native void warpPerspective_2(long src_nativeObj, long dst_nativeObj, long M_nativeObj, double dsize_width, double dsize_height);

    // C++:  void watershed(Mat image, Mat& markers)
    private static native void watershed_0(long image_nativeObj, long markers_nativeObj);

}
