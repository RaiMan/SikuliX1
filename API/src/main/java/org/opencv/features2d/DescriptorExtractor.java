/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.utils.Converters;

// C++: class javaDescriptorExtractor
/**
 * <p>Abstract base class for computing descriptors for image keypoints.</p>
 *
 * <p>class CV_EXPORTS DescriptorExtractor <code></p>
 *
 * <p>// C++ code:</p>
 *
 *
 * <p>public:</p>
 *
 * <p>virtual ~DescriptorExtractor();</p>
 *
 * <p>void compute(const Mat& image, vector<KeyPoint>& keypoints,</p>
 *
 * <p>Mat& descriptors) const;</p>
 *
 * <p>void compute(const vector<Mat>& images, vector<vector<KeyPoint> >& keypoints,</p>
 *
 * <p>vector<Mat>& descriptors) const;</p>
 *
 * <p>virtual void read(const FileNode&);</p>
 *
 * <p>virtual void write(FileStorage&) const;</p>
 *
 * <p>virtual int descriptorSize() const = 0;</p>
 *
 * <p>virtual int descriptorType() const = 0;</p>
 *
 * <p>static Ptr<DescriptorExtractor> create(const string& descriptorExtractorType);</p>
 *
 * <p>protected:...</p>
 *
 * <p>};</p>
 *
 * <p>In this interface, a keypoint descriptor can be represented as a </code></p>
 *
 * <p>dense, fixed-dimension vector of a basic type. Most descriptors follow this
 * pattern as it simplifies computing distances between descriptors. Therefore,
 * a collection of descriptors is represented as "Mat", where each row is a
 * keypoint descriptor.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_descriptor_extractors.html#descriptorextractor">org.opencv.features2d.DescriptorExtractor : public Algorithm</a>
 */
public class DescriptorExtractor {

    protected final long nativeObj;
    protected DescriptorExtractor(long addr) { nativeObj = addr; }

    private static final int
            OPPONENTEXTRACTOR = 1000;

    public static final int
            SIFT = 1,
            SURF = 2,
            ORB = 3,
            BRIEF = 4,
            BRISK = 5,
            FREAK = 6,
            OPPONENT_SIFT = OPPONENTEXTRACTOR + SIFT,
            OPPONENT_SURF = OPPONENTEXTRACTOR + SURF,
            OPPONENT_ORB = OPPONENTEXTRACTOR + ORB,
            OPPONENT_BRIEF = OPPONENTEXTRACTOR + BRIEF,
            OPPONENT_BRISK = OPPONENTEXTRACTOR + BRISK,
            OPPONENT_FREAK = OPPONENTEXTRACTOR + FREAK;

    //
    // C++:  void javaDescriptorExtractor::compute(Mat image, vector_KeyPoint& keypoints, Mat descriptors)
    //

/**
 * <p>Computes the descriptors for a set of keypoints detected in an image (first
 * variant) or image set (second variant).</p>
 *
 * @param image Image.
 * @param keypoints Input collection of keypoints. Keypoints for which a
 * descriptor cannot be computed are removed. Sometimes new keypoints can be
 * added, for example: <code>SIFT</code> duplicates keypoint with several
 * dominant orientations (for each orientation).
 * @param descriptors Computed descriptors. In the second variant of the method
 * <code>descriptors[i]</code> are descriptors computed for a <code>keypoints[i]".
 * Row </code>j<code> is the </code>keypoints<code> (or </code>keypoints[i]<code>)
 * is the descriptor for keypoint </code>j"-th keypoint.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_descriptor_extractors.html#descriptorextractor-compute">org.opencv.features2d.DescriptorExtractor.compute</a>
 */
    public  void compute(Mat image, MatOfKeyPoint keypoints, Mat descriptors)
    {
        Mat keypoints_mat = keypoints;
        compute_0(nativeObj, image.nativeObj, keypoints_mat.nativeObj, descriptors.nativeObj);

        return;
    }

    //
    // C++:  void javaDescriptorExtractor::compute(vector_Mat images, vector_vector_KeyPoint& keypoints, vector_Mat& descriptors)
    //

/**
 * <p>Computes the descriptors for a set of keypoints detected in an image (first
 * variant) or image set (second variant).</p>
 *
 * @param images Image set.
 * @param keypoints Input collection of keypoints. Keypoints for which a
 * descriptor cannot be computed are removed. Sometimes new keypoints can be
 * added, for example: <code>SIFT</code> duplicates keypoint with several
 * dominant orientations (for each orientation).
 * @param descriptors Computed descriptors. In the second variant of the method
 * <code>descriptors[i]</code> are descriptors computed for a <code>keypoints[i]".
 * Row </code>j<code> is the </code>keypoints<code> (or </code>keypoints[i]<code>)
 * is the descriptor for keypoint </code>j"-th keypoint.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_descriptor_extractors.html#descriptorextractor-compute">org.opencv.features2d.DescriptorExtractor.compute</a>
 */
    public  void compute(List<Mat> images, List<MatOfKeyPoint> keypoints, List<Mat> descriptors)
    {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        List<Mat> keypoints_tmplm = new ArrayList<Mat>((keypoints != null) ? keypoints.size() : 0);
        Mat keypoints_mat = Converters.vector_vector_KeyPoint_to_Mat(keypoints, keypoints_tmplm);
        Mat descriptors_mat = new Mat();
        compute_1(nativeObj, images_mat.nativeObj, keypoints_mat.nativeObj, descriptors_mat.nativeObj);
        Converters.Mat_to_vector_vector_KeyPoint(keypoints_mat, keypoints);
        Converters.Mat_to_vector_Mat(descriptors_mat, descriptors);
        return;
    }

    //
    // C++: static javaDescriptorExtractor* javaDescriptorExtractor::create(int extractorType)
    //

/**
 * <p>Creates a descriptor extractor by name.</p>
 *
 * <p>The current implementation supports the following types of a descriptor
 * extractor:</p>
 * <ul>
 *   <li> <code>"SIFT"</code> -- "SIFT"
 *   <li> <code>"SURF"</code> -- "SURF"
 *   <li> <code>"BRIEF"</code> -- "BriefDescriptorExtractor"
 *   <li> <code>"BRISK"</code> -- "BRISK"
 *   <li> <code>"ORB"</code> -- "ORB"
 *   <li> <code>"FREAK"</code> -- "FREAK"
 * </ul>
 *
 * <p>A combined format is also supported: descriptor extractor adapter name
 * (<code>"Opponent"</code> -- "OpponentColorDescriptorExtractor") + descriptor
 * extractor name (see above), for example: <code>"OpponentSIFT"</code>.</p>
 *
 * @param extractorType a extractorType
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_descriptor_extractors.html#descriptorextractor-create">org.opencv.features2d.DescriptorExtractor.create</a>
 */
    public static DescriptorExtractor create(int extractorType)
    {

        DescriptorExtractor retVal = new DescriptorExtractor(create_0(extractorType));

        return retVal;
    }

    //
    // C++:  int javaDescriptorExtractor::descriptorSize()
    //

    public  int descriptorSize()
    {

        int retVal = descriptorSize_0(nativeObj);

        return retVal;
    }

    //
    // C++:  int javaDescriptorExtractor::descriptorType()
    //

    public  int descriptorType()
    {

        int retVal = descriptorType_0(nativeObj);

        return retVal;
    }

    //
    // C++:  bool javaDescriptorExtractor::empty()
    //

    public  boolean empty()
    {

        boolean retVal = empty_0(nativeObj);

        return retVal;
    }

    //
    // C++:  void javaDescriptorExtractor::read(string fileName)
    //

    public  void read(String fileName)
    {

        read_0(nativeObj, fileName);

        return;
    }

    //
    // C++:  void javaDescriptorExtractor::write(string fileName)
    //

    public  void write(String fileName)
    {

        write_0(nativeObj, fileName);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++:  void javaDescriptorExtractor::compute(Mat image, vector_KeyPoint& keypoints, Mat descriptors)
    private static native void compute_0(long nativeObj, long image_nativeObj, long keypoints_mat_nativeObj, long descriptors_nativeObj);

    // C++:  void javaDescriptorExtractor::compute(vector_Mat images, vector_vector_KeyPoint& keypoints, vector_Mat& descriptors)
    private static native void compute_1(long nativeObj, long images_mat_nativeObj, long keypoints_mat_nativeObj, long descriptors_mat_nativeObj);

    // C++: static javaDescriptorExtractor* javaDescriptorExtractor::create(int extractorType)
    private static native long create_0(int extractorType);

    // C++:  int javaDescriptorExtractor::descriptorSize()
    private static native int descriptorSize_0(long nativeObj);

    // C++:  int javaDescriptorExtractor::descriptorType()
    private static native int descriptorType_0(long nativeObj);

    // C++:  bool javaDescriptorExtractor::empty()
    private static native boolean empty_0(long nativeObj);

    // C++:  void javaDescriptorExtractor::read(string fileName)
    private static native void read_0(long nativeObj, String fileName);

    // C++:  void javaDescriptorExtractor::write(string fileName)
    private static native void write_0(long nativeObj, String fileName);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
