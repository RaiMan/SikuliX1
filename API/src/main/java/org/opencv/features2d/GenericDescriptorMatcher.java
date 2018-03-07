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
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.utils.Converters;

// C++: class javaGenericDescriptorMatcher
/**
 * <p>Abstract interface for extracting and matching a keypoint descriptor. There
 * are also "DescriptorExtractor" and "DescriptorMatcher" for these purposes but
 * their interfaces are intended for descriptors represented as vectors in a
 * multidimensional space. <code>GenericDescriptorMatcher</code> is a more
 * generic interface for descriptors. <code>DescriptorMatcher</code> and
 * <code>GenericDescriptorMatcher</code> have two groups of match methods: for
 * matching keypoints of an image with another image or with an image set.</p>
 *
 * <p>class GenericDescriptorMatcher <code></p>
 *
 * <p>// C++ code:</p>
 *
 *
 * <p>public:</p>
 *
 * <p>GenericDescriptorMatcher();</p>
 *
 * <p>virtual ~GenericDescriptorMatcher();</p>
 *
 * <p>virtual void add(const vector<Mat>& images,</p>
 *
 * <p>vector<vector<KeyPoint> >& keypoints);</p>
 *
 * <p>const vector<Mat>& getTrainImages() const;</p>
 *
 * <p>const vector<vector<KeyPoint> >& getTrainKeypoints() const;</p>
 *
 * <p>virtual void clear();</p>
 *
 * <p>virtual void train() = 0;</p>
 *
 * <p>virtual bool isMaskSupported() = 0;</p>
 *
 * <p>void classify(const Mat& queryImage,</p>
 *
 * <p>vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>const Mat& trainImage,</p>
 *
 * <p>vector<KeyPoint>& trainKeypoints) const;</p>
 *
 * <p>void classify(const Mat& queryImage,</p>
 *
 * <p>vector<KeyPoint>& queryKeypoints);</p>
 *
 * <p>/ *</p>
 * <ul>
 *   <li> Group of methods to match keypoints from an image pair.
 *   <li> /
 * </ul>
 *
 * <p>void match(const Mat& queryImage, vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>const Mat& trainImage, vector<KeyPoint>& trainKeypoints,</p>
 *
 * <p>vector<DMatch>& matches, const Mat& mask=Mat()) const;</p>
 *
 * <p>void knnMatch(const Mat& queryImage, vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>const Mat& trainImage, vector<KeyPoint>& trainKeypoints,</p>
 *
 * <p>vector<vector<DMatch> >& matches, int k,</p>
 *
 * <p>const Mat& mask=Mat(), bool compactResult=false) const;</p>
 *
 * <p>void radiusMatch(const Mat& queryImage, vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>const Mat& trainImage, vector<KeyPoint>& trainKeypoints,</p>
 *
 * <p>vector<vector<DMatch> >& matches, float maxDistance,</p>
 *
 * <p>const Mat& mask=Mat(), bool compactResult=false) const;</p>
 *
 * <p>/ *</p>
 * <ul>
 *   <li> Group of methods to match keypoints from one image to an image set.
 *   <li> /
 * </ul>
 *
 * <p>void match(const Mat& queryImage, vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>vector<DMatch>& matches, const vector<Mat>& masks=vector<Mat>());</p>
 *
 * <p>void knnMatch(const Mat& queryImage, vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>vector<vector<DMatch> >& matches, int k,</p>
 *
 * <p>const vector<Mat>& masks=vector<Mat>(), bool compactResult=false);</p>
 *
 * <p>void radiusMatch(const Mat& queryImage, vector<KeyPoint>& queryKeypoints,</p>
 *
 * <p>vector<vector<DMatch> >& matches, float maxDistance,</p>
 *
 * <p>const vector<Mat>& masks=vector<Mat>(), bool compactResult=false);</p>
 *
 * <p>virtual void read(const FileNode&);</p>
 *
 * <p>virtual void write(FileStorage&) const;</p>
 *
 * <p>virtual Ptr<GenericDescriptorMatcher> clone(bool emptyTrainData=false) const
 * = 0;</p>
 *
 * <p>protected:...</p>
 *
 * <p>};</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher">org.opencv.features2d.GenericDescriptorMatcher</a>
 */
public class GenericDescriptorMatcher {

    protected final long nativeObj;
    protected GenericDescriptorMatcher(long addr) { nativeObj = addr; }

    public static final int
            ONEWAY = 1,
            FERN = 2;

    //
    // C++:  void javaGenericDescriptorMatcher::add(vector_Mat images, vector_vector_KeyPoint keypoints)
    //

/**
 * <p>Adds images and their keypoints to the training collection stored in the
 * class instance.</p>
 *
 * @param images Image collection.
 * @param keypoints Point collection. It is assumed that <code>keypoints[i]</code>
 * are keypoints detected in the image <code>images[i]</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-add">org.opencv.features2d.GenericDescriptorMatcher.add</a>
 */
    public  void add(List<Mat> images, List<MatOfKeyPoint> keypoints)
    {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        List<Mat> keypoints_tmplm = new ArrayList<Mat>((keypoints != null) ? keypoints.size() : 0);
        Mat keypoints_mat = Converters.vector_vector_KeyPoint_to_Mat(keypoints, keypoints_tmplm);
        add_0(nativeObj, images_mat.nativeObj, keypoints_mat.nativeObj);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::classify(Mat queryImage, vector_KeyPoint& queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints)
    //

/**
 * <p>Classifies keypoints from a query set.</p>
 *
 * <p>The method classifies each keypoint from a query set. The first variant of
 * the method takes a train image and its keypoints as an input argument. The
 * second variant uses the internally stored training collection that can be
 * built using the <code>GenericDescriptorMatcher.add</code> method.</p>
 *
 * <p>The methods do the following:</p>
 * <ul>
 *   <li> Call the <code>GenericDescriptorMatcher.match</code> method to find
 * correspondence between the query set and the training set.
 *   <li> Set the <code>class_id</code> field of each keypoint from the query
 * set to <code>class_id</code> of the corresponding keypoint from the training
 * set.
 * </ul>
 *
 * @param queryImage Query image.
 * @param queryKeypoints Keypoints from a query image.
 * @param trainImage Train image.
 * @param trainKeypoints Keypoints from a train image.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-classify">org.opencv.features2d.GenericDescriptorMatcher.classify</a>
 */
    public  void classify(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        classify_0(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::classify(Mat queryImage, vector_KeyPoint& queryKeypoints)
    //

/**
 * <p>Classifies keypoints from a query set.</p>
 *
 * <p>The method classifies each keypoint from a query set. The first variant of
 * the method takes a train image and its keypoints as an input argument. The
 * second variant uses the internally stored training collection that can be
 * built using the <code>GenericDescriptorMatcher.add</code> method.</p>
 *
 * <p>The methods do the following:</p>
 * <ul>
 *   <li> Call the <code>GenericDescriptorMatcher.match</code> method to find
 * correspondence between the query set and the training set.
 *   <li> Set the <code>class_id</code> field of each keypoint from the query
 * set to <code>class_id</code> of the corresponding keypoint from the training
 * set.
 * </ul>
 *
 * @param queryImage Query image.
 * @param queryKeypoints Keypoints from a query image.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-classify">org.opencv.features2d.GenericDescriptorMatcher.classify</a>
 */
    public  void classify(Mat queryImage, MatOfKeyPoint queryKeypoints)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        classify_1(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::clear()
    //

/**
 * <p>Clears a train collection (images and keypoints).</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-clear">org.opencv.features2d.GenericDescriptorMatcher.clear</a>
 */
    public  void clear()
    {

        clear_0(nativeObj);

        return;
    }

    //
    // C++:  javaGenericDescriptorMatcher* javaGenericDescriptorMatcher::jclone(bool emptyTrainData = false)
    //

    public  GenericDescriptorMatcher clone(boolean emptyTrainData)
    {

        GenericDescriptorMatcher retVal = new GenericDescriptorMatcher(clone_0(nativeObj, emptyTrainData));

        return retVal;
    }

    public  GenericDescriptorMatcher clone()
    {

        GenericDescriptorMatcher retVal = new GenericDescriptorMatcher(clone_1(nativeObj));

        return retVal;
    }

    //
    // C++: static javaGenericDescriptorMatcher* javaGenericDescriptorMatcher::create(int matcherType)
    //

    public static GenericDescriptorMatcher create(int matcherType)
    {

        GenericDescriptorMatcher retVal = new GenericDescriptorMatcher(create_0(matcherType));

        return retVal;
    }

    //
    // C++:  bool javaGenericDescriptorMatcher::empty()
    //

    public  boolean empty()
    {

        boolean retVal = empty_0(nativeObj);

        return retVal;
    }

    //
    // C++:  vector_Mat javaGenericDescriptorMatcher::getTrainImages()
    //

/**
 * <p>Returns a train image collection.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-gettrainimages">org.opencv.features2d.GenericDescriptorMatcher.getTrainImages</a>
 */
    public  List<Mat> getTrainImages()
    {
        List<Mat> retVal = new ArrayList<Mat>();
        Mat retValMat = new Mat(getTrainImages_0(nativeObj));
        Converters.Mat_to_vector_Mat(retValMat, retVal);
        return retVal;
    }

    //
    // C++:  vector_vector_KeyPoint javaGenericDescriptorMatcher::getTrainKeypoints()
    //

/**
 * <p>Returns a train keypoints collection.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-gettrainkeypoints">org.opencv.features2d.GenericDescriptorMatcher.getTrainKeypoints</a>
 */
    public  List<MatOfKeyPoint> getTrainKeypoints()
    {
        List<MatOfKeyPoint> retVal = new ArrayList<MatOfKeyPoint>();
        Mat retValMat = new Mat(getTrainKeypoints_0(nativeObj));
        Converters.Mat_to_vector_vector_KeyPoint(retValMat, retVal);
        return retVal;
    }

    //
    // C++:  bool javaGenericDescriptorMatcher::isMaskSupported()
    //

/**
 * <p>Returns <code>true</code> if a generic descriptor matcher supports masking
 * permissible matches.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-ismasksupported">org.opencv.features2d.GenericDescriptorMatcher.isMaskSupported</a>
 */
    public  boolean isMaskSupported()
    {

        boolean retVal = isMaskSupported_0(nativeObj);

        return retVal;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::knnMatch(Mat queryImage, vector_KeyPoint queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints, vector_vector_DMatch& matches, int k, Mat mask = Mat(), bool compactResult = false)
    //

/**
 * <p>Finds the <code>k</code> best matches for each query keypoint.</p>
 *
 * <p>The methods are extended variants of <code>GenericDescriptorMatch.match</code>.
 * The parameters are similar, and the semantics is similar to <code>DescriptorMatcher.knnMatch</code>.
 * But this class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param trainImage a trainImage
 * @param trainKeypoints a trainKeypoints
 * @param matches a matches
 * @param k a k
 * @param mask a mask
 * @param compactResult a compactResult
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-knnmatch">org.opencv.features2d.GenericDescriptorMatcher.knnMatch</a>
 */
    public  void knnMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints, List<MatOfDMatch> matches, int k, Mat mask, boolean compactResult)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        Mat matches_mat = new Mat();
        knnMatch_0(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj, matches_mat.nativeObj, k, mask.nativeObj, compactResult);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

/**
 * <p>Finds the <code>k</code> best matches for each query keypoint.</p>
 *
 * <p>The methods are extended variants of <code>GenericDescriptorMatch.match</code>.
 * The parameters are similar, and the semantics is similar to <code>DescriptorMatcher.knnMatch</code>.
 * But this class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param trainImage a trainImage
 * @param trainKeypoints a trainKeypoints
 * @param matches a matches
 * @param k a k
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-knnmatch">org.opencv.features2d.GenericDescriptorMatcher.knnMatch</a>
 */
    public  void knnMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints, List<MatOfDMatch> matches, int k)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        Mat matches_mat = new Mat();
        knnMatch_1(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj, matches_mat.nativeObj, k);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::knnMatch(Mat queryImage, vector_KeyPoint queryKeypoints, vector_vector_DMatch& matches, int k, vector_Mat masks = vector<Mat>(), bool compactResult = false)
    //

/**
 * <p>Finds the <code>k</code> best matches for each query keypoint.</p>
 *
 * <p>The methods are extended variants of <code>GenericDescriptorMatch.match</code>.
 * The parameters are similar, and the semantics is similar to <code>DescriptorMatcher.knnMatch</code>.
 * But this class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param matches a matches
 * @param k a k
 * @param masks a masks
 * @param compactResult a compactResult
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-knnmatch">org.opencv.features2d.GenericDescriptorMatcher.knnMatch</a>
 */
    public  void knnMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, List<MatOfDMatch> matches, int k, List<Mat> masks, boolean compactResult)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat matches_mat = new Mat();
        Mat masks_mat = Converters.vector_Mat_to_Mat(masks);
        knnMatch_2(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, matches_mat.nativeObj, k, masks_mat.nativeObj, compactResult);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

/**
 * <p>Finds the <code>k</code> best matches for each query keypoint.</p>
 *
 * <p>The methods are extended variants of <code>GenericDescriptorMatch.match</code>.
 * The parameters are similar, and the semantics is similar to <code>DescriptorMatcher.knnMatch</code>.
 * But this class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param matches a matches
 * @param k a k
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-knnmatch">org.opencv.features2d.GenericDescriptorMatcher.knnMatch</a>
 */
    public  void knnMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, List<MatOfDMatch> matches, int k)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat matches_mat = new Mat();
        knnMatch_3(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, matches_mat.nativeObj, k);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::match(Mat queryImage, vector_KeyPoint queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints, vector_DMatch& matches, Mat mask = Mat())
    //

/**
 * <p>Finds the best match in the training set for each keypoint from the query
 * set.</p>
 *
 * <p>The methods find the best match for each query keypoint. In the first variant
 * of the method, a train image and its keypoints are the input arguments. In
 * the second variant, query keypoints are matched to the internally stored
 * training collection that can be built using the <code>GenericDescriptorMatcher.add</code>
 * method. Optional mask (or masks) can be passed to specify which query and
 * training descriptors can be matched. Namely, <code>queryKeypoints[i]</code>
 * can be matched with <code>trainKeypoints[j]</code> only if <code>mask.at<uchar>(i,j)</code>
 * is non-zero.</p>
 *
 * @param queryImage Query image.
 * @param queryKeypoints Keypoints detected in <code>queryImage</code>.
 * @param trainImage Train image. It is not added to a train image collection
 * stored in the class object.
 * @param trainKeypoints Keypoints detected in <code>trainImage</code>. They are
 * not added to a train points collection stored in the class object.
 * @param matches Matches. If a query descriptor (keypoint) is masked out in
 * <code>mask</code>, match is added for this descriptor. So, <code>matches</code>
 * size may be smaller than the query keypoints count.
 * @param mask Mask specifying permissible matches between an input query and
 * train keypoints.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-match">org.opencv.features2d.GenericDescriptorMatcher.match</a>
 */
    public  void match(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints, MatOfDMatch matches, Mat mask)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        Mat matches_mat = matches;
        match_0(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj, matches_mat.nativeObj, mask.nativeObj);

        return;
    }

/**
 * <p>Finds the best match in the training set for each keypoint from the query
 * set.</p>
 *
 * <p>The methods find the best match for each query keypoint. In the first variant
 * of the method, a train image and its keypoints are the input arguments. In
 * the second variant, query keypoints are matched to the internally stored
 * training collection that can be built using the <code>GenericDescriptorMatcher.add</code>
 * method. Optional mask (or masks) can be passed to specify which query and
 * training descriptors can be matched. Namely, <code>queryKeypoints[i]</code>
 * can be matched with <code>trainKeypoints[j]</code> only if <code>mask.at<uchar>(i,j)</code>
 * is non-zero.</p>
 *
 * @param queryImage Query image.
 * @param queryKeypoints Keypoints detected in <code>queryImage</code>.
 * @param trainImage Train image. It is not added to a train image collection
 * stored in the class object.
 * @param trainKeypoints Keypoints detected in <code>trainImage</code>. They are
 * not added to a train points collection stored in the class object.
 * @param matches Matches. If a query descriptor (keypoint) is masked out in
 * <code>mask</code>, match is added for this descriptor. So, <code>matches</code>
 * size may be smaller than the query keypoints count.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-match">org.opencv.features2d.GenericDescriptorMatcher.match</a>
 */
    public  void match(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints, MatOfDMatch matches)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        Mat matches_mat = matches;
        match_1(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj, matches_mat.nativeObj);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::match(Mat queryImage, vector_KeyPoint queryKeypoints, vector_DMatch& matches, vector_Mat masks = vector<Mat>())
    //

/**
 * <p>Finds the best match in the training set for each keypoint from the query
 * set.</p>
 *
 * <p>The methods find the best match for each query keypoint. In the first variant
 * of the method, a train image and its keypoints are the input arguments. In
 * the second variant, query keypoints are matched to the internally stored
 * training collection that can be built using the <code>GenericDescriptorMatcher.add</code>
 * method. Optional mask (or masks) can be passed to specify which query and
 * training descriptors can be matched. Namely, <code>queryKeypoints[i]</code>
 * can be matched with <code>trainKeypoints[j]</code> only if <code>mask.at<uchar>(i,j)</code>
 * is non-zero.</p>
 *
 * @param queryImage Query image.
 * @param queryKeypoints Keypoints detected in <code>queryImage</code>.
 * @param matches Matches. If a query descriptor (keypoint) is masked out in
 * <code>mask</code>, match is added for this descriptor. So, <code>matches</code>
 * size may be smaller than the query keypoints count.
 * @param masks Set of masks. Each <code>masks[i]</code> specifies permissible
 * matches between input query keypoints and stored train keypoints from the
 * i-th image.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-match">org.opencv.features2d.GenericDescriptorMatcher.match</a>
 */
    public  void match(Mat queryImage, MatOfKeyPoint queryKeypoints, MatOfDMatch matches, List<Mat> masks)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat matches_mat = matches;
        Mat masks_mat = Converters.vector_Mat_to_Mat(masks);
        match_2(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, matches_mat.nativeObj, masks_mat.nativeObj);

        return;
    }

/**
 * <p>Finds the best match in the training set for each keypoint from the query
 * set.</p>
 *
 * <p>The methods find the best match for each query keypoint. In the first variant
 * of the method, a train image and its keypoints are the input arguments. In
 * the second variant, query keypoints are matched to the internally stored
 * training collection that can be built using the <code>GenericDescriptorMatcher.add</code>
 * method. Optional mask (or masks) can be passed to specify which query and
 * training descriptors can be matched. Namely, <code>queryKeypoints[i]</code>
 * can be matched with <code>trainKeypoints[j]</code> only if <code>mask.at<uchar>(i,j)</code>
 * is non-zero.</p>
 *
 * @param queryImage Query image.
 * @param queryKeypoints Keypoints detected in <code>queryImage</code>.
 * @param matches Matches. If a query descriptor (keypoint) is masked out in
 * <code>mask</code>, match is added for this descriptor. So, <code>matches</code>
 * size may be smaller than the query keypoints count.
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-match">org.opencv.features2d.GenericDescriptorMatcher.match</a>
 */
    public  void match(Mat queryImage, MatOfKeyPoint queryKeypoints, MatOfDMatch matches)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat matches_mat = matches;
        match_3(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, matches_mat.nativeObj);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::radiusMatch(Mat queryImage, vector_KeyPoint queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints, vector_vector_DMatch& matches, float maxDistance, Mat mask = Mat(), bool compactResult = false)
    //

/**
 * <p>For each query keypoint, finds the training keypoints not farther than the
 * specified distance.</p>
 *
 * <p>The methods are similar to <code>DescriptorMatcher.radius</code>. But this
 * class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param trainImage a trainImage
 * @param trainKeypoints a trainKeypoints
 * @param matches a matches
 * @param maxDistance a maxDistance
 * @param mask a mask
 * @param compactResult a compactResult
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-radiusmatch">org.opencv.features2d.GenericDescriptorMatcher.radiusMatch</a>
 */
    public  void radiusMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints, List<MatOfDMatch> matches, float maxDistance, Mat mask, boolean compactResult)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        Mat matches_mat = new Mat();
        radiusMatch_0(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj, matches_mat.nativeObj, maxDistance, mask.nativeObj, compactResult);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

/**
 * <p>For each query keypoint, finds the training keypoints not farther than the
 * specified distance.</p>
 *
 * <p>The methods are similar to <code>DescriptorMatcher.radius</code>. But this
 * class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param trainImage a trainImage
 * @param trainKeypoints a trainKeypoints
 * @param matches a matches
 * @param maxDistance a maxDistance
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-radiusmatch">org.opencv.features2d.GenericDescriptorMatcher.radiusMatch</a>
 */
    public  void radiusMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, Mat trainImage, MatOfKeyPoint trainKeypoints, List<MatOfDMatch> matches, float maxDistance)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat trainKeypoints_mat = trainKeypoints;
        Mat matches_mat = new Mat();
        radiusMatch_1(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, trainImage.nativeObj, trainKeypoints_mat.nativeObj, matches_mat.nativeObj, maxDistance);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::radiusMatch(Mat queryImage, vector_KeyPoint queryKeypoints, vector_vector_DMatch& matches, float maxDistance, vector_Mat masks = vector<Mat>(), bool compactResult = false)
    //

/**
 * <p>For each query keypoint, finds the training keypoints not farther than the
 * specified distance.</p>
 *
 * <p>The methods are similar to <code>DescriptorMatcher.radius</code>. But this
 * class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param matches a matches
 * @param maxDistance a maxDistance
 * @param masks a masks
 * @param compactResult a compactResult
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-radiusmatch">org.opencv.features2d.GenericDescriptorMatcher.radiusMatch</a>
 */
    public  void radiusMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, List<MatOfDMatch> matches, float maxDistance, List<Mat> masks, boolean compactResult)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat matches_mat = new Mat();
        Mat masks_mat = Converters.vector_Mat_to_Mat(masks);
        radiusMatch_2(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, matches_mat.nativeObj, maxDistance, masks_mat.nativeObj, compactResult);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

/**
 * <p>For each query keypoint, finds the training keypoints not farther than the
 * specified distance.</p>
 *
 * <p>The methods are similar to <code>DescriptorMatcher.radius</code>. But this
 * class does not require explicitly computed keypoint descriptors.</p>
 *
 * @param queryImage a queryImage
 * @param queryKeypoints a queryKeypoints
 * @param matches a matches
 * @param maxDistance a maxDistance
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-radiusmatch">org.opencv.features2d.GenericDescriptorMatcher.radiusMatch</a>
 */
    public  void radiusMatch(Mat queryImage, MatOfKeyPoint queryKeypoints, List<MatOfDMatch> matches, float maxDistance)
    {
        Mat queryKeypoints_mat = queryKeypoints;
        Mat matches_mat = new Mat();
        radiusMatch_3(nativeObj, queryImage.nativeObj, queryKeypoints_mat.nativeObj, matches_mat.nativeObj, maxDistance);
        Converters.Mat_to_vector_vector_DMatch(matches_mat, matches);
        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::read(string fileName)
    //

/**
 * <p>Reads a matcher object from a file node.</p>
 *
 * @param fileName a fileName
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-read">org.opencv.features2d.GenericDescriptorMatcher.read</a>
 */
    public  void read(String fileName)
    {

        read_0(nativeObj, fileName);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::train()
    //

/**
 * <p>Trains descriptor matcher</p>
 *
 * <p>Prepares descriptor matcher, for example, creates a tree-based structure, to
 * extract descriptors or to optimize descriptors matching.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-train">org.opencv.features2d.GenericDescriptorMatcher.train</a>
 */
    public  void train()
    {

        train_0(nativeObj);

        return;
    }

    //
    // C++:  void javaGenericDescriptorMatcher::write(string fileName)
    //

/**
 * <p>Writes a match object to a file storage.</p>
 *
 * @param fileName a fileName
 *
 * @see <a href="http://docs.opencv.org/modules/features2d/doc/common_interfaces_of_generic_descriptor_matchers.html#genericdescriptormatcher-write">org.opencv.features2d.GenericDescriptorMatcher.write</a>
 */
    public  void write(String fileName)
    {

        write_0(nativeObj, fileName);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++:  void javaGenericDescriptorMatcher::add(vector_Mat images, vector_vector_KeyPoint keypoints)
    private static native void add_0(long nativeObj, long images_mat_nativeObj, long keypoints_mat_nativeObj);

    // C++:  void javaGenericDescriptorMatcher::classify(Mat queryImage, vector_KeyPoint& queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints)
    private static native void classify_0(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj);

    // C++:  void javaGenericDescriptorMatcher::classify(Mat queryImage, vector_KeyPoint& queryKeypoints)
    private static native void classify_1(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj);

    // C++:  void javaGenericDescriptorMatcher::clear()
    private static native void clear_0(long nativeObj);

    // C++:  javaGenericDescriptorMatcher* javaGenericDescriptorMatcher::jclone(bool emptyTrainData = false)
    private static native long clone_0(long nativeObj, boolean emptyTrainData);
    private static native long clone_1(long nativeObj);

    // C++: static javaGenericDescriptorMatcher* javaGenericDescriptorMatcher::create(int matcherType)
    private static native long create_0(int matcherType);

    // C++:  bool javaGenericDescriptorMatcher::empty()
    private static native boolean empty_0(long nativeObj);

    // C++:  vector_Mat javaGenericDescriptorMatcher::getTrainImages()
    private static native long getTrainImages_0(long nativeObj);

    // C++:  vector_vector_KeyPoint javaGenericDescriptorMatcher::getTrainKeypoints()
    private static native long getTrainKeypoints_0(long nativeObj);

    // C++:  bool javaGenericDescriptorMatcher::isMaskSupported()
    private static native boolean isMaskSupported_0(long nativeObj);

    // C++:  void javaGenericDescriptorMatcher::knnMatch(Mat queryImage, vector_KeyPoint queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints, vector_vector_DMatch& matches, int k, Mat mask = Mat(), bool compactResult = false)
    private static native void knnMatch_0(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj, long matches_mat_nativeObj, int k, long mask_nativeObj, boolean compactResult);
    private static native void knnMatch_1(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj, long matches_mat_nativeObj, int k);

    // C++:  void javaGenericDescriptorMatcher::knnMatch(Mat queryImage, vector_KeyPoint queryKeypoints, vector_vector_DMatch& matches, int k, vector_Mat masks = vector<Mat>(), bool compactResult = false)
    private static native void knnMatch_2(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long matches_mat_nativeObj, int k, long masks_mat_nativeObj, boolean compactResult);
    private static native void knnMatch_3(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long matches_mat_nativeObj, int k);

    // C++:  void javaGenericDescriptorMatcher::match(Mat queryImage, vector_KeyPoint queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints, vector_DMatch& matches, Mat mask = Mat())
    private static native void match_0(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj, long matches_mat_nativeObj, long mask_nativeObj);
    private static native void match_1(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj, long matches_mat_nativeObj);

    // C++:  void javaGenericDescriptorMatcher::match(Mat queryImage, vector_KeyPoint queryKeypoints, vector_DMatch& matches, vector_Mat masks = vector<Mat>())
    private static native void match_2(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long matches_mat_nativeObj, long masks_mat_nativeObj);
    private static native void match_3(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long matches_mat_nativeObj);

    // C++:  void javaGenericDescriptorMatcher::radiusMatch(Mat queryImage, vector_KeyPoint queryKeypoints, Mat trainImage, vector_KeyPoint trainKeypoints, vector_vector_DMatch& matches, float maxDistance, Mat mask = Mat(), bool compactResult = false)
    private static native void radiusMatch_0(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj, long matches_mat_nativeObj, float maxDistance, long mask_nativeObj, boolean compactResult);
    private static native void radiusMatch_1(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long trainImage_nativeObj, long trainKeypoints_mat_nativeObj, long matches_mat_nativeObj, float maxDistance);

    // C++:  void javaGenericDescriptorMatcher::radiusMatch(Mat queryImage, vector_KeyPoint queryKeypoints, vector_vector_DMatch& matches, float maxDistance, vector_Mat masks = vector<Mat>(), bool compactResult = false)
    private static native void radiusMatch_2(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long matches_mat_nativeObj, float maxDistance, long masks_mat_nativeObj, boolean compactResult);
    private static native void radiusMatch_3(long nativeObj, long queryImage_nativeObj, long queryKeypoints_mat_nativeObj, long matches_mat_nativeObj, float maxDistance);

    // C++:  void javaGenericDescriptorMatcher::read(string fileName)
    private static native void read_0(long nativeObj, String fileName);

    // C++:  void javaGenericDescriptorMatcher::train()
    private static native void train_0(long nativeObj);

    // C++:  void javaGenericDescriptorMatcher::write(string fileName)
    private static native void write_0(long nativeObj, String fileName);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
