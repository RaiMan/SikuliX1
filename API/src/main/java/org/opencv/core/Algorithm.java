/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.core;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.opencv.utils.Converters;

// C++: class Algorithm
/**
 * <p>class CV_EXPORTS_W Algorithm <code></p>
 *
 * <p>// C++ code:</p>
 *
 *
 * <p>public:</p>
 *
 * <p>Algorithm();</p>
 *
 * <p>virtual ~Algorithm();</p>
 *
 * <p>string name() const;</p>
 *
 * <p>template<typename _Tp> typename ParamType<_Tp>.member_type get(const string&
 * name) const;</p>
 *
 * <p>template<typename _Tp> typename ParamType<_Tp>.member_type get(const char*
 * name) const;</p>
 *
 * <p>CV_WRAP int getInt(const string& name) const;</p>
 *
 * <p>CV_WRAP double getDouble(const string& name) const;</p>
 *
 * <p>CV_WRAP bool getBool(const string& name) const;</p>
 *
 * <p>CV_WRAP string getString(const string& name) const;</p>
 *
 * <p>CV_WRAP Mat getMat(const string& name) const;</p>
 *
 * <p>CV_WRAP vector<Mat> getMatVector(const string& name) const;</p>
 *
 * <p>CV_WRAP Ptr<Algorithm> getAlgorithm(const string& name) const;</p>
 *
 * <p>void set(const string& name, int value);</p>
 *
 * <p>void set(const string& name, double value);</p>
 *
 * <p>void set(const string& name, bool value);</p>
 *
 * <p>void set(const string& name, const string& value);</p>
 *
 * <p>void set(const string& name, const Mat& value);</p>
 *
 * <p>void set(const string& name, const vector<Mat>& value);</p>
 *
 * <p>void set(const string& name, const Ptr<Algorithm>& value);</p>
 *
 * <p>template<typename _Tp> void set(const string& name, const Ptr<_Tp>& value);</p>
 *
 * <p>CV_WRAP void setInt(const string& name, int value);</p>
 *
 * <p>CV_WRAP void setDouble(const string& name, double value);</p>
 *
 * <p>CV_WRAP void setBool(const string& name, bool value);</p>
 *
 * <p>CV_WRAP void setString(const string& name, const string& value);</p>
 *
 * <p>CV_WRAP void setMat(const string& name, const Mat& value);</p>
 *
 * <p>CV_WRAP void setMatVector(const string& name, const vector<Mat>& value);</p>
 *
 * <p>CV_WRAP void setAlgorithm(const string& name, const Ptr<Algorithm>& value);</p>
 *
 * <p>template<typename _Tp> void setAlgorithm(const string& name, const Ptr<_Tp>&
 * value);</p>
 *
 * <p>void set(const char* name, int value);</p>
 *
 * <p>void set(const char* name, double value);</p>
 *
 * <p>void set(const char* name, bool value);</p>
 *
 * <p>void set(const char* name, const string& value);</p>
 *
 * <p>void set(const char* name, const Mat& value);</p>
 *
 * <p>void set(const char* name, const vector<Mat>& value);</p>
 *
 * <p>void set(const char* name, const Ptr<Algorithm>& value);</p>
 *
 * <p>template<typename _Tp> void set(const char* name, const Ptr<_Tp>& value);</p>
 *
 * <p>void setInt(const char* name, int value);</p>
 *
 * <p>void setDouble(const char* name, double value);</p>
 *
 * <p>void setBool(const char* name, bool value);</p>
 *
 * <p>void setString(const char* name, const string& value);</p>
 *
 * <p>void setMat(const char* name, const Mat& value);</p>
 *
 * <p>void setMatVector(const char* name, const vector<Mat>& value);</p>
 *
 * <p>void setAlgorithm(const char* name, const Ptr<Algorithm>& value);</p>
 *
 * <p>template<typename _Tp> void setAlgorithm(const char* name, const Ptr<_Tp>&
 * value);</p>
 *
 * <p>CV_WRAP string paramHelp(const string& name) const;</p>
 *
 * <p>int paramType(const char* name) const;</p>
 *
 * <p>CV_WRAP int paramType(const string& name) const;</p>
 *
 * <p>CV_WRAP void getParams(CV_OUT vector<string>& names) const;</p>
 *
 * <p>virtual void write(FileStorage& fs) const;</p>
 *
 * <p>virtual void read(const FileNode& fn);</p>
 *
 * <p>typedef Algorithm* (*Constructor)(void);</p>
 *
 * <p>typedef int (Algorithm.*Getter)() const;</p>
 *
 * <p>typedef void (Algorithm.*Setter)(int);</p>
 *
 * <p>CV_WRAP static void getList(CV_OUT vector<string>& algorithms);</p>
 *
 * <p>CV_WRAP static Ptr<Algorithm> _create(const string& name);</p>
 *
 * <p>template<typename _Tp> static Ptr<_Tp> create(const string& name);</p>
 *
 * <p>virtual AlgorithmInfo* info() const / * TODO: make it = 0;* / { return 0; }</p>
 *
 * <p>};</p>
 *
 * <p>This is a base class for all more or less complex algorithms in OpenCV,
 * especially for classes of algorithms, for which there can be multiple
 * implementations. The examples are stereo correspondence (for which there are
 * algorithms like block matching, semi-global block matching, graph-cut etc.),
 * background subtraction (which can be done using mixture-of-gaussians models,
 * codebook-based algorithm etc.), optical flow (block matching, Lucas-Kanade,
 * Horn-Schunck etc.).
 * </code></p>
 *
 * <p>The class provides the following features for all derived classes:</p>
 * <ul>
 *   <li> so called "virtual constructor". That is, each Algorithm derivative is
 * registered at program start and you can get the list of registered algorithms
 * and create instance of a particular algorithm by its name (see
 * <code>Algorithm.create</code>). If you plan to add your own algorithms, it
 * is good practice to add a unique prefix to your algorithms to distinguish
 * them from other algorithms.
 *   <li> setting/retrieving algorithm parameters by name. If you used video
 * capturing functionality from OpenCV highgui module, you are probably familar
 * with <code>cvSetCaptureProperty()</code>, <code>cvGetCaptureProperty()</code>,
 * <code>VideoCapture.set()</code> and <code>VideoCapture.get()</code>.
 * <code>Algorithm</code> provides similar method where instead of integer id's
 * you specify the parameter names as text strings. See <code>Algorithm.set</code>
 * and <code>Algorithm.get</code> for details.
 *   <li> reading and writing parameters from/to XML or YAML files. Every
 * Algorithm derivative can store all its parameters and then read them back.
 * There is no need to re-implement it each time.
 * </ul>
 * <p>Here is example of SIFT use in your application via Algorithm interface:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>#include "opencv2/opencv.hpp"</p>
 *
 * <p>#include "opencv2/nonfree/nonfree.hpp"...</p>
 *
 * <p>initModule_nonfree(); // to load SURF/SIFT etc.</p>
 *
 * <p>Ptr<Feature2D> sift = Algorithm.create<Feature2D>("Feature2D.SIFT");</p>
 *
 * <p>FileStorage fs("sift_params.xml", FileStorage.READ);</p>
 *
 * <p>if(fs.isOpened()) // if we have file with parameters, read them</p>
 *
 *
 * <p>sift->read(fs["sift_params"]);</p>
 *
 * <p>fs.release();</p>
 *
 *
 * <p>else // else modify the parameters and store them; user can later edit the
 * file to use different parameters</p>
 *
 *
 * <p>sift->set("contrastThreshold", 0.01f); // lower the contrast threshold,
 * compared to the default value</p>
 *
 *
 * <p>WriteStructContext ws(fs, "sift_params", CV_NODE_MAP);</p>
 *
 * <p>sift->write(fs);</p>
 *
 *
 *
 * <p>Mat image = imread("myimage.png", 0), descriptors;</p>
 *
 * <p>vector<KeyPoint> keypoints;</p>
 *
 * <p>(*sift)(image, noArray(), keypoints, descriptors);</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#algorithm">org.opencv.core.Algorithm</a>
 */
public class Algorithm {

    protected final long nativeObj;
    protected Algorithm(long addr) { nativeObj = addr; }

    //
    // C++: static Ptr_Algorithm Algorithm::_create(string name)
    //

    // Return type 'Ptr_Algorithm' is not supported, skipping the function

    //
    // C++:  Ptr_Algorithm Algorithm::getAlgorithm(string name)
    //

    // Return type 'Ptr_Algorithm' is not supported, skipping the function

    //
    // C++:  bool Algorithm::getBool(string name)
    //

    public  boolean getBool(String name)
    {

        boolean retVal = getBool_0(nativeObj, name);

        return retVal;
    }

    //
    // C++:  double Algorithm::getDouble(string name)
    //

    public  double getDouble(String name)
    {

        double retVal = getDouble_0(nativeObj, name);

        return retVal;
    }

    //
    // C++:  int Algorithm::getInt(string name)
    //

    public  int getInt(String name)
    {

        int retVal = getInt_0(nativeObj, name);

        return retVal;
    }

    //
    // C++: static void Algorithm::getList(vector_string& algorithms)
    //

    // Unknown type 'vector_string' (O), skipping the function

    //
    // C++:  Mat Algorithm::getMat(string name)
    //

    public  Mat getMat(String name)
    {

        Mat retVal = new Mat(getMat_0(nativeObj, name));

        return retVal;
    }

    //
    // C++:  vector_Mat Algorithm::getMatVector(string name)
    //

    public  List<Mat> getMatVector(String name)
    {
        List<Mat> retVal = new ArrayList<Mat>();
        Mat retValMat = new Mat(getMatVector_0(nativeObj, name));
        Converters.Mat_to_vector_Mat(retValMat, retVal);
        return retVal;
    }

    //
    // C++:  void Algorithm::getParams(vector_string& names)
    //

    // Unknown type 'vector_string' (O), skipping the function

    //
    // C++:  string Algorithm::getString(string name)
    //

    public  String getString(String name)
    {

        String retVal = getString_0(nativeObj, name);

        return retVal;
    }

    //
    // C++:  string Algorithm::paramHelp(string name)
    //

    public  String paramHelp(String name)
    {

        String retVal = paramHelp_0(nativeObj, name);

        return retVal;
    }

    //
    // C++:  int Algorithm::paramType(string name)
    //

    public  int paramType(String name)
    {

        int retVal = paramType_0(nativeObj, name);

        return retVal;
    }

    //
    // C++:  void Algorithm::setAlgorithm(string name, Ptr_Algorithm value)
    //

    // Unknown type 'Ptr_Algorithm' (I), skipping the function

    //
    // C++:  void Algorithm::setBool(string name, bool value)
    //

    public  void setBool(String name, boolean value)
    {

        setBool_0(nativeObj, name, value);

        return;
    }

    //
    // C++:  void Algorithm::setDouble(string name, double value)
    //

    public  void setDouble(String name, double value)
    {

        setDouble_0(nativeObj, name, value);

        return;
    }

    //
    // C++:  void Algorithm::setInt(string name, int value)
    //

    public  void setInt(String name, int value)
    {

        setInt_0(nativeObj, name, value);

        return;
    }

    //
    // C++:  void Algorithm::setMat(string name, Mat value)
    //

    public  void setMat(String name, Mat value)
    {

        setMat_0(nativeObj, name, value.nativeObj);

        return;
    }

    //
    // C++:  void Algorithm::setMatVector(string name, vector_Mat value)
    //

    public  void setMatVector(String name, List<Mat> value)
    {
        Mat value_mat = Converters.vector_Mat_to_Mat(value);
        setMatVector_0(nativeObj, name, value_mat.nativeObj);

        return;
    }

    //
    // C++:  void Algorithm::setString(string name, string value)
    //

    public  void setString(String name, String value)
    {

        setString_0(nativeObj, name, value);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++:  bool Algorithm::getBool(string name)
    private static native boolean getBool_0(long nativeObj, String name);

    // C++:  double Algorithm::getDouble(string name)
    private static native double getDouble_0(long nativeObj, String name);

    // C++:  int Algorithm::getInt(string name)
    private static native int getInt_0(long nativeObj, String name);

    // C++:  Mat Algorithm::getMat(string name)
    private static native long getMat_0(long nativeObj, String name);

    // C++:  vector_Mat Algorithm::getMatVector(string name)
    private static native long getMatVector_0(long nativeObj, String name);

    // C++:  string Algorithm::getString(string name)
    private static native String getString_0(long nativeObj, String name);

    // C++:  string Algorithm::paramHelp(string name)
    private static native String paramHelp_0(long nativeObj, String name);

    // C++:  int Algorithm::paramType(string name)
    private static native int paramType_0(long nativeObj, String name);

    // C++:  void Algorithm::setBool(string name, bool value)
    private static native void setBool_0(long nativeObj, String name, boolean value);

    // C++:  void Algorithm::setDouble(string name, double value)
    private static native void setDouble_0(long nativeObj, String name, double value);

    // C++:  void Algorithm::setInt(string name, int value)
    private static native void setInt_0(long nativeObj, String name, int value);

    // C++:  void Algorithm::setMat(string name, Mat value)
    private static native void setMat_0(long nativeObj, String name, long value_nativeObj);

    // C++:  void Algorithm::setMatVector(string name, vector_Mat value)
    private static native void setMatVector_0(long nativeObj, String name, long value_mat_nativeObj);

    // C++:  void Algorithm::setString(string name, string value)
    private static native void setString_0(long nativeObj, String name, String value);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
