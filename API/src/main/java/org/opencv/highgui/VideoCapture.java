/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.highgui;

import java.lang.String;
import org.opencv.core.Mat;

// C++: class VideoCapture
/**
 * <p>Class for video capturing from video files or cameras.
 * The class provides C++ API for capturing video from cameras or for reading
 * video files. Here is how the class can be used:</p>
 *
 * <p>#include "opencv2/opencv.hpp" <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>using namespace cv;</p>
 *
 * <p>int main(int, char)</p>
 *
 *
 * <p>VideoCapture cap(0); // open the default camera</p>
 *
 * <p>if(!cap.isOpened()) // check if we succeeded</p>
 *
 * <p>return -1;</p>
 *
 * <p>Mat edges;</p>
 *
 * <p>namedWindow("edges",1);</p>
 *
 * <p>for(;;)</p>
 *
 *
 * <p>Mat frame;</p>
 *
 * <p>cap >> frame; // get a new frame from camera</p>
 *
 * <p>cvtColor(frame, edges, CV_BGR2GRAY);</p>
 *
 * <p>GaussianBlur(edges, edges, Size(7,7), 1.5, 1.5);</p>
 *
 * <p>Canny(edges, edges, 0, 30, 3);</p>
 *
 * <p>imshow("edges", edges);</p>
 *
 * <p>if(waitKey(30) >= 0) break;</p>
 *
 *
 * <p>// the camera will be deinitialized automatically in VideoCapture destructor</p>
 *
 * <p>return 0;</p>
 *
 *
 * <p>Note: In C API the black-box structure <code>CvCapture</code> is used instead
 * of <code>VideoCapture</code>.
 * </code></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> A basic sample on using the VideoCapture interface can be found at
 * opencv_source_code/samples/cpp/starter_video.cpp
 *   <li> Another basic video processing sample can be found at
 * opencv_source_code/samples/cpp/video_dmtx.cpp
 *   <li> (Python) A basic sample on using the VideoCapture interface can be
 * found at opencv_source_code/samples/python2/video.py
 *   <li> (Python) Another basic video processing sample can be found at
 * opencv_source_code/samples/python2/video_dmtx.py
 *   <li> (Python) A multi threaded video processing sample can be found at
 * opencv_source_code/samples/python2/video_threaded.py
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture">org.opencv.highgui.VideoCapture</a>
 */
public class VideoCapture {

    protected final long nativeObj;
    protected VideoCapture(long addr) { nativeObj = addr; }

    //
    // C++:   VideoCapture::VideoCapture()
    //

/**
 * <p>VideoCapture constructors.</p>
 *
 * <p>Note: In C API, when you finished working with video, release
 * <code>CvCapture</code> structure with <code>cvReleaseCapture()</code>, or use
 * <code>Ptr<CvCapture></code> that calls <code>cvReleaseCapture()</code>
 * automatically in the destructor.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-videocapture">org.opencv.highgui.VideoCapture.VideoCapture</a>
 */
    public   VideoCapture()
    {

        nativeObj = VideoCapture_0();

        return;
    }

    //
    // C++:   VideoCapture::VideoCapture(string filename)
    //

/**
 * <p>VideoCapture constructors.</p>
 *
 * <p>Note: In C API, when you finished working with video, release
 * <code>CvCapture</code> structure with <code>cvReleaseCapture()</code>, or use
 * <code>Ptr<CvCapture></code> that calls <code>cvReleaseCapture()</code>
 * automatically in the destructor.</p>
 *
 * @param filename name of the opened video file (eg. video.avi) or image
 * sequence (eg. img_%02d.jpg, which will read samples like img_00.jpg,
 * img_01.jpg, img_02.jpg,...)
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-videocapture">org.opencv.highgui.VideoCapture.VideoCapture</a>
 */
    public   VideoCapture(String filename)
    {

        nativeObj = VideoCapture_1(filename);

        return;
    }

    //
    // C++:   VideoCapture::VideoCapture(int device)
    //

/**
 * <p>VideoCapture constructors.</p>
 *
 * <p>Note: In C API, when you finished working with video, release
 * <code>CvCapture</code> structure with <code>cvReleaseCapture()</code>, or use
 * <code>Ptr<CvCapture></code> that calls <code>cvReleaseCapture()</code>
 * automatically in the destructor.</p>
 *
 * @param device id of the opened video capturing device (i.e. a camera index).
 * If there is a single camera connected, just pass 0.
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-videocapture">org.opencv.highgui.VideoCapture.VideoCapture</a>
 */
    public   VideoCapture(int device)
    {

        nativeObj = VideoCapture_2(device);

        return;
    }

    //
    // C++:  double VideoCapture::get(int propId)
    //

/**
 * <p>Returns the specified <code>VideoCapture</code> property</p>
 *
 * <p>Note: When querying a property that is not supported by the backend used by
 * the <code>VideoCapture</code> class, value 0 is returned.</p>
 *
 * @param propId Property identifier. It can be one of the following:
 * <ul>
 *   <li> CV_CAP_PROP_POS_MSEC Current position of the video file in
 * milliseconds or video capture timestamp.
 *   <li> CV_CAP_PROP_POS_FRAMES 0-based index of the frame to be
 * decoded/captured next.
 *   <li> CV_CAP_PROP_POS_AVI_RATIO Relative position of the video file: 0 -
 * start of the film, 1 - end of the film.
 *   <li> CV_CAP_PROP_FRAME_WIDTH Width of the frames in the video stream.
 *   <li> CV_CAP_PROP_FRAME_HEIGHT Height of the frames in the video stream.
 *   <li> CV_CAP_PROP_FPS Frame rate.
 *   <li> CV_CAP_PROP_FOURCC 4-character code of codec.
 *   <li> CV_CAP_PROP_FRAME_COUNT Number of frames in the video file.
 *   <li> CV_CAP_PROP_FORMAT Format of the Mat objects returned by
 * <code>retrieve()</code>.
 *   <li> CV_CAP_PROP_MODE Backend-specific value indicating the current capture
 * mode.
 *   <li> CV_CAP_PROP_BRIGHTNESS Brightness of the image (only for cameras).
 *   <li> CV_CAP_PROP_CONTRAST Contrast of the image (only for cameras).
 *   <li> CV_CAP_PROP_SATURATION Saturation of the image (only for cameras).
 *   <li> CV_CAP_PROP_HUE Hue of the image (only for cameras).
 *   <li> CV_CAP_PROP_GAIN Gain of the image (only for cameras).
 *   <li> CV_CAP_PROP_EXPOSURE Exposure (only for cameras).
 *   <li> CV_CAP_PROP_CONVERT_RGB Boolean flags indicating whether images should
 * be converted to RGB.
 *   <li> CV_CAP_PROP_WHITE_BALANCE Currently not supported
 *   <li> CV_CAP_PROP_RECTIFICATION Rectification flag for stereo cameras (note:
 * only supported by DC1394 v 2.x backend currently)
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-get">org.opencv.highgui.VideoCapture.get</a>
 */
    public  double get(int propId)
    {

        double retVal = get_0(nativeObj, propId);

        return retVal;
    }

    //
    // C++:  bool VideoCapture::grab()
    //

/**
 * <p>Grabs the next frame from video file or capturing device.</p>
 *
 * <p>The methods/functions grab the next frame from video file or camera and
 * return true (non-zero) in the case of success.</p>
 *
 * <p>The primary use of the function is in multi-camera environments, especially
 * when the cameras do not have hardware synchronization. That is, you call
 * <code>VideoCapture.grab()</code> for each camera and after that call the
 * slower method <code>VideoCapture.retrieve()</code> to decode and get frame
 * from each camera. This way the overhead on demosaicing or motion jpeg
 * decompression etc. is eliminated and the retrieved frames from different
 * cameras will be closer in time.</p>
 *
 * <p>Also, when a connected camera is multi-head (for example, a stereo camera or
 * a Kinect device), the correct way of retrieving data from it is to call
 * "VideoCapture.grab" first and then call "VideoCapture.retrieve" one or more
 * times with different values of the <code>channel</code> parameter. See
 * https://github.com/Itseez/opencv/tree/master/samples/cpp/openni_capture.cpp</p>
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-grab">org.opencv.highgui.VideoCapture.grab</a>
 */
    public  boolean grab()
    {

        boolean retVal = grab_0(nativeObj);

        return retVal;
    }

    //
    // C++:  bool VideoCapture::isOpened()
    //

/**
 * <p>Returns true if video capturing has been initialized already.</p>
 *
 * <p>If the previous call to <code>VideoCapture</code> constructor or
 * <code>VideoCapture.open</code> succeeded, the method returns true.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-isopened">org.opencv.highgui.VideoCapture.isOpened</a>
 */
    public  boolean isOpened()
    {

        boolean retVal = isOpened_0(nativeObj);

        return retVal;
    }

    //
    // C++:  bool VideoCapture::open(string filename)
    //

/**
 * <p>Open video file or a capturing device for video capturing</p>
 *
 * <p>The methods first call "VideoCapture.release" to close the already opened
 * file or camera.</p>
 *
 * @param filename name of the opened video file (eg. video.avi) or image
 * sequence (eg. img_%02d.jpg, which will read samples like img_00.jpg,
 * img_01.jpg, img_02.jpg,...)
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-open">org.opencv.highgui.VideoCapture.open</a>
 */
    public  boolean open(String filename)
    {

        boolean retVal = open_0(nativeObj, filename);

        return retVal;
    }

    //
    // C++:  bool VideoCapture::open(int device)
    //

/**
 * <p>Open video file or a capturing device for video capturing</p>
 *
 * <p>The methods first call "VideoCapture.release" to close the already opened
 * file or camera.</p>
 *
 * @param device id of the opened video capturing device (i.e. a camera index).
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-open">org.opencv.highgui.VideoCapture.open</a>
 */
    public  boolean open(int device)
    {

        boolean retVal = open_1(nativeObj, device);

        return retVal;
    }

    //
    // C++:  bool VideoCapture::read(Mat& image)
    //

/**
 * <p>Grabs, decodes and returns the next video frame.</p>
 *
 * <p>The methods/functions combine "VideoCapture.grab" and "VideoCapture.retrieve"
 * in one call. This is the most convenient method for reading video files or
 * capturing data from decode and return the just grabbed frame. If no frames
 * has been grabbed (camera has been disconnected, or there are no more frames
 * in video file), the methods return false and the functions return NULL
 * pointer.</p>
 *
 * <p>Note: OpenCV 1.x functions <code>cvRetrieveFrame</code> and <code>cv.RetrieveFrame</code>
 * return image stored inside the video capturing structure. It is not allowed
 * to modify or release the image! You can copy the frame using "cvCloneImage"
 * and then do whatever you want with the copy.</p>
 *
 * @param image a image
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-read">org.opencv.highgui.VideoCapture.read</a>
 */
    public  boolean read(Mat image)
    {

        boolean retVal = read_0(nativeObj, image.nativeObj);

        return retVal;
    }

    //
    // C++:  void VideoCapture::release()
    //

/**
 * <p>Closes video file or capturing device.</p>
 *
 * <p>The methods are automatically called by subsequent "VideoCapture.open" and
 * by <code>VideoCapture</code> destructor.</p>
 *
 * <p>The C function also deallocates memory and clears <code>*capture</code>
 * pointer.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-release">org.opencv.highgui.VideoCapture.release</a>
 */
    public  void release()
    {

        release_0(nativeObj);

        return;
    }

    //
    // C++:  bool VideoCapture::retrieve(Mat& image, int channel = 0)
    //

/**
 * <p>Decodes and returns the grabbed video frame.</p>
 *
 * <p>The methods/functions decode and return the just grabbed frame. If no frames
 * has been grabbed (camera has been disconnected, or there are no more frames
 * in video file), the methods return false and the functions return NULL
 * pointer.</p>
 *
 * <p>Note: OpenCV 1.x functions <code>cvRetrieveFrame</code> and <code>cv.RetrieveFrame</code>
 * return image stored inside the video capturing structure. It is not allowed
 * to modify or release the image! You can copy the frame using "cvCloneImage"
 * and then do whatever you want with the copy.</p>
 *
 * @param image a image
 * @param channel a channel
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-retrieve">org.opencv.highgui.VideoCapture.retrieve</a>
 */
    public  boolean retrieve(Mat image, int channel)
    {

        boolean retVal = retrieve_0(nativeObj, image.nativeObj, channel);

        return retVal;
    }

/**
 * <p>Decodes and returns the grabbed video frame.</p>
 *
 * <p>The methods/functions decode and return the just grabbed frame. If no frames
 * has been grabbed (camera has been disconnected, or there are no more frames
 * in video file), the methods return false and the functions return NULL
 * pointer.</p>
 *
 * <p>Note: OpenCV 1.x functions <code>cvRetrieveFrame</code> and <code>cv.RetrieveFrame</code>
 * return image stored inside the video capturing structure. It is not allowed
 * to modify or release the image! You can copy the frame using "cvCloneImage"
 * and then do whatever you want with the copy.</p>
 *
 * @param image a image
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-retrieve">org.opencv.highgui.VideoCapture.retrieve</a>
 */
    public  boolean retrieve(Mat image)
    {

        boolean retVal = retrieve_1(nativeObj, image.nativeObj);

        return retVal;
    }

    //
    // C++:  bool VideoCapture::set(int propId, double value)
    //

/**
 * <p>Sets a property in the <code>VideoCapture</code>.</p>
 *
 * @param propId Property identifier. It can be one of the following:
 * <ul>
 *   <li> CV_CAP_PROP_POS_MSEC Current position of the video file in
 * milliseconds.
 *   <li> CV_CAP_PROP_POS_FRAMES 0-based index of the frame to be
 * decoded/captured next.
 *   <li> CV_CAP_PROP_POS_AVI_RATIO Relative position of the video file: 0 -
 * start of the film, 1 - end of the film.
 *   <li> CV_CAP_PROP_FRAME_WIDTH Width of the frames in the video stream.
 *   <li> CV_CAP_PROP_FRAME_HEIGHT Height of the frames in the video stream.
 *   <li> CV_CAP_PROP_FPS Frame rate.
 *   <li> CV_CAP_PROP_FOURCC 4-character code of codec.
 *   <li> CV_CAP_PROP_FRAME_COUNT Number of frames in the video file.
 *   <li> CV_CAP_PROP_FORMAT Format of the Mat objects returned by
 * <code>retrieve()</code>.
 *   <li> CV_CAP_PROP_MODE Backend-specific value indicating the current capture
 * mode.
 *   <li> CV_CAP_PROP_BRIGHTNESS Brightness of the image (only for cameras).
 *   <li> CV_CAP_PROP_CONTRAST Contrast of the image (only for cameras).
 *   <li> CV_CAP_PROP_SATURATION Saturation of the image (only for cameras).
 *   <li> CV_CAP_PROP_HUE Hue of the image (only for cameras).
 *   <li> CV_CAP_PROP_GAIN Gain of the image (only for cameras).
 *   <li> CV_CAP_PROP_EXPOSURE Exposure (only for cameras).
 *   <li> CV_CAP_PROP_CONVERT_RGB Boolean flags indicating whether images should
 * be converted to RGB.
 *   <li> CV_CAP_PROP_WHITE_BALANCE Currently unsupported
 *   <li> CV_CAP_PROP_RECTIFICATION Rectification flag for stereo cameras (note:
 * only supported by DC1394 v 2.x backend currently)
 * </ul>
 * @param value Value of the property.
 *
 * @see <a href="http://docs.opencv.org/modules/highgui/doc/reading_and_writing_images_and_video.html#videocapture-set">org.opencv.highgui.VideoCapture.set</a>
 */
    public  boolean set(int propId, double value)
    {

        boolean retVal = set_0(nativeObj, propId, value);

        return retVal;
    }

    public java.util.List<org.opencv.core.Size> getSupportedPreviewSizes()
    {
        String[] sizes_str = getSupportedPreviewSizes_0(nativeObj).split(",");
        java.util.List<org.opencv.core.Size> sizes = new java.util.ArrayList<org.opencv.core.Size>(sizes_str.length);

        for (String str : sizes_str) {
            String[] wh = str.split("x");
            sizes.add(new org.opencv.core.Size(Double.parseDouble(wh[0]), Double.parseDouble(wh[1])));
        }

        return sizes;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++:   VideoCapture::VideoCapture()
    private static native long VideoCapture_0();

    // C++:   VideoCapture::VideoCapture(string filename)
    private static native long VideoCapture_1(String filename);

    // C++:   VideoCapture::VideoCapture(int device)
    private static native long VideoCapture_2(int device);

    // C++:  double VideoCapture::get(int propId)
    private static native double get_0(long nativeObj, int propId);

    // C++:  bool VideoCapture::grab()
    private static native boolean grab_0(long nativeObj);

    // C++:  bool VideoCapture::isOpened()
    private static native boolean isOpened_0(long nativeObj);

    // C++:  bool VideoCapture::open(string filename)
    private static native boolean open_0(long nativeObj, String filename);

    // C++:  bool VideoCapture::open(int device)
    private static native boolean open_1(long nativeObj, int device);

    // C++:  bool VideoCapture::read(Mat& image)
    private static native boolean read_0(long nativeObj, long image_nativeObj);

    // C++:  void VideoCapture::release()
    private static native void release_0(long nativeObj);

    // C++:  bool VideoCapture::retrieve(Mat& image, int channel = 0)
    private static native boolean retrieve_0(long nativeObj, long image_nativeObj, int channel);
    private static native boolean retrieve_1(long nativeObj, long image_nativeObj);

    // C++:  bool VideoCapture::set(int propId, double value)
    private static native boolean set_0(long nativeObj, int propId, double value);

    private static native String getSupportedPreviewSizes_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
