/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.opencv.core;

// C++: class Mat
/**
 * <p>OpenCV C++ n-dimensional dense array class</p>
 *
 * <p>class CV_EXPORTS Mat <code></p>
 *
 * <p>// C++ code:</p>
 *
 *
 * <p>public:</p>
 *
 * <p>//... a lot of methods......</p>
 *
 * <p>/ *! includes several bit-fields:</p>
 *
 * <p>- the magic signature</p>
 *
 * <p>- continuity flag</p>
 *
 * <p>- depth</p>
 *
 * <p>- number of channels</p>
 * <ul>
 *   <li> /
 * </ul>
 *
 * <p>int flags;</p>
 *
 * <p>//! the array dimensionality, >= 2</p>
 *
 * <p>int dims;</p>
 *
 * <p>//! the number of rows and columns or (-1, -1) when the array has more than 2
 * dimensions</p>
 *
 * <p>int rows, cols;</p>
 *
 * <p>//! pointer to the data</p>
 *
 * <p>uchar* data;</p>
 *
 * <p>//! pointer to the reference counter;</p>
 *
 * <p>// when array points to user-allocated data, the pointer is NULL</p>
 *
 * <p>int* refcount;</p>
 *
 * <p>// other members...</p>
 *
 * <p>};</p>
 *
 * <p>The class <code>Mat</code> represents an n-dimensional dense numerical
 * single-channel or multi-channel array. It can be used to store real or
 * complex-valued vectors and matrices, grayscale or color images, voxel
 * volumes, vector fields, point clouds, tensors, histograms (though, very
 * high-dimensional histograms may be better stored in a <code>SparseMat</code>).
 * The data layout of the array </code></p>
 *
 * <p><em>M</em> is defined by the array <code>M.step[]</code>, so that the address
 * of element <em>(i_0,...,i_(M.dims-1))</em>, where <em>0 <= i_k&ltM.size[k]</em>,
 * is computed as:</p>
 *
 * <p><em>addr(M_(i_0,...,i_(M.dims-1))) = M.data + M.step[0]*i_0 + M.step[1]*i_1
 * +... + M.step[M.dims-1]*i_(M.dims-1)</em></p>
 *
 * <p>In case of a 2-dimensional array, the above formula is reduced to:</p>
 *
 * <p><em>addr(M_(i,j)) = M.data + M.step[0]*i + M.step[1]*j</em></p>
 *
 * <p>Note that <code>M.step[i] >= M.step[i+1]</code> (in fact, <code>M.step[i] >=
 * M.step[i+1]*M.size[i+1]</code>). This means that 2-dimensional matrices are
 * stored row-by-row, 3-dimensional matrices are stored plane-by-plane, and so
 * on. <code>M.step[M.dims-1]</code> is minimal and always equal to the element
 * size <code>M.elemSize()</code>.</p>
 *
 * <p>So, the data layout in <code>Mat</code> is fully compatible with
 * <code>CvMat</code>, <code>IplImage</code>, and <code>CvMatND</code> types
 * from OpenCV 1.x. It is also compatible with the majority of dense array types
 * from the standard toolkits and SDKs, such as Numpy (ndarray), Win32
 * (independent device bitmaps), and others, that is, with any array that uses
 * *steps* (or *strides*) to compute the position of a pixel. Due to this
 * compatibility, it is possible to make a <code>Mat</code> header for
 * user-allocated data and process it in-place using OpenCV functions.</p>
 *
 * <p>There are many different ways to create a <code>Mat</code> object. The most
 * popular options are listed below:</p>
 * <ul>
 *   <li> Use the <code>create(nrows, ncols, type)</code> method or the similar
 * <code>Mat(nrows, ncols, type[, fillValue])</code> constructor. A new array of
 * the specified size and type is allocated. <code>type</code> has the same
 * meaning as in the <code>cvCreateMat</code> method.
 * </ul>
 * <p>For example, <code>CV_8UC1</code> means a 8-bit single-channel array,
 * <code>CV_32FC2</code> means a 2-channel (complex) floating-point array, and
 * so on.</p>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// make a 7x7 complex matrix filled with 1+3j.</p>
 *
 * <p>Mat M(7,7,CV_32FC2,Scalar(1,3));</p>
 *
 * <p>// and now turn M to a 100x60 15-channel 8-bit matrix.</p>
 *
 * <p>// The old content will be deallocated</p>
 *
 * <p>M.create(100,60,CV_8UC(15));</p>
 *
 * <p></code></p>
 *
 * <p>As noted in the introduction to this chapter, <code>create()</code> allocates
 * only a new array when the shape or type of the current array are different
 * from the specified ones.</p>
 * <ul>
 *   <li> Create a multi-dimensional array:
 * </ul>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// create a 100x100x100 8-bit array</p>
 *
 * <p>int sz[] = {100, 100, 100};</p>
 *
 * <p>Mat bigCube(3, sz, CV_8U, Scalar.all(0));</p>
 *
 * <p></code></p>
 *
 * <p>It passes the number of dimensions =1 to the <code>Mat</code> constructor but
 * the created array will be 2-dimensional with the number of columns set to 1.
 * So, <code>Mat.dims</code> is always >= 2 (can also be 0 when the array is
 * empty).</p>
 * <ul>
 *   <li> Use a copy constructor or assignment operator where there can be an
 * array or expression on the right side (see below). As noted in the
 * introduction, the array assignment is an O(1) operation because it only
 * copies the header and increases the reference counter. The <code>Mat.clone()</code>
 * method can be used to get a full (deep) copy of the array when you need it.
 *   <li> Construct a header for a part of another array. It can be a single
 * row, single column, several rows, several columns, rectangular region in the
 * array (called a *minor* in algebra) or a diagonal. Such operations are also
 * O(1) because the new header references the same data. You can actually modify
 * a part of the array using this feature, for example:
 * </ul>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// add the 5-th row, multiplied by 3 to the 3rd row</p>
 *
 * <p>M.row(3) = M.row(3) + M.row(5)*3;</p>
 *
 * <p>// now copy the 7-th column to the 1-st column</p>
 *
 * <p>// M.col(1) = M.col(7); // this will not work</p>
 *
 * <p>Mat M1 = M.col(1);</p>
 *
 * <p>M.col(7).copyTo(M1);</p>
 *
 * <p>// create a new 320x240 image</p>
 *
 * <p>Mat img(Size(320,240),CV_8UC3);</p>
 *
 * <p>// select a ROI</p>
 *
 * <p>Mat roi(img, Rect(10,10,100,100));</p>
 *
 * <p>// fill the ROI with (0,255,0) (which is green in RGB space);</p>
 *
 * <p>// the original 320x240 image will be modified</p>
 *
 * <p>roi = Scalar(0,255,0);</p>
 *
 * <p></code></p>
 *
 * <p>Due to the additional <code>datastart</code> and <code>dataend</code>
 * members, it is possible to compute a relative sub-array position in the main
 * *container* array using <code>locateROI()</code>:</p>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A = Mat.eye(10, 10, CV_32S);</p>
 *
 * <p>// extracts A columns, 1 (inclusive) to 3 (exclusive).</p>
 *
 * <p>Mat B = A(Range.all(), Range(1, 3));</p>
 *
 * <p>// extracts B rows, 5 (inclusive) to 9 (exclusive).</p>
 *
 * <p>// that is, C ~ A(Range(5, 9), Range(1, 3))</p>
 *
 * <p>Mat C = B(Range(5, 9), Range.all());</p>
 *
 * <p>Size size; Point ofs;</p>
 *
 * <p>C.locateROI(size, ofs);</p>
 *
 * <p>// size will be (width=10,height=10) and the ofs will be (x=1, y=5)</p>
 *
 * <p></code></p>
 *
 * <p>As in case of whole matrices, if you need a deep copy, use the
 * <code>clone()</code> method of the extracted sub-matrices.</p>
 * <ul>
 *   <li> Make a header for user-allocated data. It can be useful to do the
 * following:
 *   <li> Process "foreign" data using OpenCV (for example, when you implement a
 * DirectShow* filter or a processing module for <code>gstreamer</code>, and so
 * on). For example:
 * </ul>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>void process_video_frame(const unsigned char* pixels,</p>
 *
 * <p>int width, int height, int step)</p>
 *
 *
 * <p>Mat img(height, width, CV_8UC3, pixels, step);</p>
 *
 * <p>GaussianBlur(img, img, Size(7,7), 1.5, 1.5);</p>
 *
 *
 * <p></code></p>
 * <ul>
 *   <li> Quickly initialize small matrices and/or get a super-fast element
 * access.
 * </ul>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>double m[3][3] = {{a, b, c}, {d, e, f}, {g, h, i}};</p>
 *
 * <p>Mat M = Mat(3, 3, CV_64F, m).inv();</p>
 *
 * <p></code></p>
 *
 * <p>Partial yet very common cases of this *user-allocated data* case are
 * conversions from <code>CvMat</code> and <code>IplImage</code> to
 * <code>Mat</code>. For this purpose, there are special constructors taking
 * pointers to <code>CvMat</code> or <code>IplImage</code> and the optional flag
 * indicating whether to copy the data or not.</p>
 *
 * <p>Backward conversion from <code>Mat</code> to <code>CvMat</code> or
 * <code>IplImage</code> is provided via cast operators <code>Mat.operator
 * CvMat() const</code> and <code>Mat.operator IplImage()</code>. The operators
 * do NOT copy the data.</p>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>IplImage* img = cvLoadImage("greatwave.jpg", 1);</p>
 *
 * <p>Mat mtx(img); // convert IplImage* -> Mat</p>
 *
 * <p>CvMat oldmat = mtx; // convert Mat -> CvMat</p>
 *
 * <p>CV_Assert(oldmat.cols == img->width && oldmat.rows == img->height &&</p>
 *
 * <p>oldmat.data.ptr == (uchar*)img->imageData && oldmat.step == img->widthStep);</p>
 *
 * <p></code></p>
 * <ul>
 *   <li> Use MATLAB-style array initializers, <code>zeros(), ones(),
 * eye()</code>, for example:
 * </ul>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// create a double-precision identity martix and add it to M.</p>
 *
 * <p>M += Mat.eye(M.rows, M.cols, CV_64F);</p>
 *
 * <p></code></p>
 * <ul>
 *   <li> Use a comma-separated initializer:
 * </ul>
 *
 * <p><code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// create a 3x3 double-precision identity matrix</p>
 *
 * <p>Mat M = (Mat_<double>(3,3) << 1, 0, 0, 0, 1, 0, 0, 0, 1);</p>
 *
 * <p></code></p>
 *
 * <p>With this approach, you first call a constructor of the "Mat_" class with the
 * proper parameters, and then you just put <code><<</code> operator followed by
 * comma-separated values that can be constants, variables, expressions, and so
 * on. Also, note the extra parentheses required to avoid compilation errors.</p>
 *
 * <p>Once the array is created, it is automatically managed via a
 * reference-counting mechanism. If the array header is built on top of
 * user-allocated data, you should handle the data by yourself.
 * The array data is deallocated when no one points to it. If you want to
 * release the data pointed by a array header before the array destructor is
 * called, use <code>Mat.release()</code>.</p>
 *
 * <p>The next important thing to learn about the array class is element access.
 * This manual already described how to compute an address of each array
 * element. Normally, you are not required to use the formula directly in the
 * code. If you know the array element type (which can be retrieved using the
 * method <code>Mat.type()</code>), you can access the element<em>M_(ij)</em>
 * of a 2-dimensional array as: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>M.at<double>(i,j) += 1.f;</p>
 *
 * <p>assuming that M is a double-precision floating-point array. There are several
 * variants of the method <code>at</code> for a different number of dimensions.
 * </code></p>
 *
 * <p>If you need to process a whole row of a 2D array, the most efficient way is
 * to get the pointer to the row first, and then just use the plain C operator
 * <code>[]</code> : <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// compute sum of positive matrix elements</p>
 *
 * <p>// (assuming that M isa double-precision matrix)</p>
 *
 * <p>double sum=0;</p>
 *
 * <p>for(int i = 0; i < M.rows; i++)</p>
 *
 *
 * <p>const double* Mi = M.ptr<double>(i);</p>
 *
 * <p>for(int j = 0; j < M.cols; j++)</p>
 *
 * <p>sum += std.max(Mi[j], 0.);</p>
 *
 *
 * <p>Some operations, like the one above, do not actually depend on the array
 * shape. They just process elements of an array one by one (or elements from
 * multiple arrays that have the same coordinates, for example, array addition).
 * Such operations are called *element-wise*. It makes sense to check whether
 * all the input/output arrays are continuous, namely, have no gaps at the end
 * of each row. If yes, process them as a long single row:</p>
 *
 * <p>// compute the sum of positive matrix elements, optimized variant</p>
 *
 * <p>double sum=0;</p>
 *
 * <p>int cols = M.cols, rows = M.rows;</p>
 *
 * <p>if(M.isContinuous())</p>
 *
 *
 * <p>cols *= rows;</p>
 *
 * <p>rows = 1;</p>
 *
 *
 * <p>for(int i = 0; i < rows; i++)</p>
 *
 *
 * <p>const double* Mi = M.ptr<double>(i);</p>
 *
 * <p>for(int j = 0; j < cols; j++)</p>
 *
 * <p>sum += std.max(Mi[j], 0.);</p>
 *
 *
 * <p>In case of the continuous matrix, the outer loop body is executed just once.
 * So, the overhead is smaller, which is especially noticeable in case of small
 * matrices.
 * </code></p>
 *
 * <p>Finally, there are STL-style iterators that are smart enough to skip gaps
 * between successive rows: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// compute sum of positive matrix elements, iterator-based variant</p>
 *
 * <p>double sum=0;</p>
 *
 * <p>MatConstIterator_<double> it = M.begin<double>(), it_end = M.end<double>();</p>
 *
 * <p>for(; it != it_end; ++it)</p>
 *
 * <p>sum += std.max(*it, 0.);</p>
 *
 * <p>The matrix iterators are random-access iterators, so they can be passed to
 * any STL algorithm, including <code>std.sort()</code>.
 * </code></p>
 *
 * <p>Note:</p>
 * <ul>
 *   <li> An example demonstrating the serial out capabilities of cv.Mat can be
 * found at opencv_source_code/samples/cpp/cout_mat.cpp
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat">org.opencv.core.Mat</a>
 */
public class Mat {

    public final long nativeObj;

    public Mat(long addr)
    {
        if (addr == 0)
            throw new java.lang.UnsupportedOperationException("Native object address is NULL");
        nativeObj = addr;
    }

    //
    // C++: Mat::Mat()
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat()
    {

        nativeObj = n_Mat();

        return;
    }

    //
    // C++: Mat::Mat(int rows, int cols, int type)
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param rows Number of rows in a 2D array.
 * @param cols Number of columns in a 2D array.
 * @param type Array type. Use <code>CV_8UC1,..., CV_64FC4</code> to create 1-4
 * channel matrices, or <code>CV_8UC(n),..., CV_64FC(n)</code> to create
 * multi-channel (up to <code>CV_CN_MAX</code> channels) matrices.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(int rows, int cols, int type)
    {

        nativeObj = n_Mat(rows, cols, type);

        return;
    }

    //
    // C++: Mat::Mat(Size size, int type)
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param size 2D array size: <code>Size(cols, rows)</code>. In the
 * <code>Size()</code> constructor, the number of rows and the number of columns
 * go in the reverse order.
 * @param type Array type. Use <code>CV_8UC1,..., CV_64FC4</code> to create 1-4
 * channel matrices, or <code>CV_8UC(n),..., CV_64FC(n)</code> to create
 * multi-channel (up to <code>CV_CN_MAX</code> channels) matrices.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(Size size, int type)
    {

        nativeObj = n_Mat(size.width, size.height, type);

        return;
    }

    //
    // C++: Mat::Mat(int rows, int cols, int type, Scalar s)
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param rows Number of rows in a 2D array.
 * @param cols Number of columns in a 2D array.
 * @param type Array type. Use <code>CV_8UC1,..., CV_64FC4</code> to create 1-4
 * channel matrices, or <code>CV_8UC(n),..., CV_64FC(n)</code> to create
 * multi-channel (up to <code>CV_CN_MAX</code> channels) matrices.
 * @param s An optional value to initialize each matrix element with. To set all
 * the matrix elements to the particular value after the construction, use the
 * assignment operator <code>Mat.operator=(const Scalar& value)</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(int rows, int cols, int type, Scalar s)
    {

        nativeObj = n_Mat(rows, cols, type, s.val[0], s.val[1], s.val[2], s.val[3]);

        return;
    }

    //
    // C++: Mat::Mat(Size size, int type, Scalar s)
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param size 2D array size: <code>Size(cols, rows)</code>. In the
 * <code>Size()</code> constructor, the number of rows and the number of columns
 * go in the reverse order.
 * @param type Array type. Use <code>CV_8UC1,..., CV_64FC4</code> to create 1-4
 * channel matrices, or <code>CV_8UC(n),..., CV_64FC(n)</code> to create
 * multi-channel (up to <code>CV_CN_MAX</code> channels) matrices.
 * @param s An optional value to initialize each matrix element with. To set all
 * the matrix elements to the particular value after the construction, use the
 * assignment operator <code>Mat.operator=(const Scalar& value)</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(Size size, int type, Scalar s)
    {

        nativeObj = n_Mat(size.width, size.height, type, s.val[0], s.val[1], s.val[2], s.val[3]);

        return;
    }

    //
    // C++: Mat::Mat(Mat m, Range rowRange, Range colRange = Range::all())
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param m Array that (as a whole or partly) is assigned to the constructed
 * matrix. No data is copied by these constructors. Instead, the header pointing
 * to <code>m</code> data or its sub-array is constructed and associated with
 * it. The reference counter, if any, is incremented. So, when you modify the
 * matrix formed using such a constructor, you also modify the corresponding
 * elements of <code>m</code>. If you want to have an independent copy of the
 * sub-array, use <code>Mat.clone()</code>.
 * @param rowRange Range of the <code>m</code> rows to take. As usual, the range
 * start is inclusive and the range end is exclusive. Use <code>Range.all()</code>
 * to take all the rows.
 * @param colRange Range of the <code>m</code> columns to take. Use
 * <code>Range.all()</code> to take all the columns.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(Mat m, Range rowRange, Range colRange)
    {

        nativeObj = n_Mat(m.nativeObj, rowRange.start, rowRange.end, colRange.start, colRange.end);

        return;
    }

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param m Array that (as a whole or partly) is assigned to the constructed
 * matrix. No data is copied by these constructors. Instead, the header pointing
 * to <code>m</code> data or its sub-array is constructed and associated with
 * it. The reference counter, if any, is incremented. So, when you modify the
 * matrix formed using such a constructor, you also modify the corresponding
 * elements of <code>m</code>. If you want to have an independent copy of the
 * sub-array, use <code>Mat.clone()</code>.
 * @param rowRange Range of the <code>m</code> rows to take. As usual, the range
 * start is inclusive and the range end is exclusive. Use <code>Range.all()</code>
 * to take all the rows.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(Mat m, Range rowRange)
    {

        nativeObj = n_Mat(m.nativeObj, rowRange.start, rowRange.end);

        return;
    }

    //
    // C++: Mat::Mat(Mat m, Rect roi)
    //

/**
 * <p>Various Mat constructors</p>
 *
 * <p>These are various constructors that form a matrix. As noted in the
 * "AutomaticAllocation", often the default constructor is enough, and the
 * proper matrix will be allocated by an OpenCV function. The constructed matrix
 * can further be assigned to another matrix or matrix expression or can be
 * allocated with "Mat.create". In the former case, the old content is
 * de-referenced.</p>
 *
 * @param m Array that (as a whole or partly) is assigned to the constructed
 * matrix. No data is copied by these constructors. Instead, the header pointing
 * to <code>m</code> data or its sub-array is constructed and associated with
 * it. The reference counter, if any, is incremented. So, when you modify the
 * matrix formed using such a constructor, you also modify the corresponding
 * elements of <code>m</code>. If you want to have an independent copy of the
 * sub-array, use <code>Mat.clone()</code>.
 * @param roi Region of interest.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
 */
    public Mat(Mat m, Rect roi)
    {

        nativeObj = n_Mat(m.nativeObj, roi.y, roi.y + roi.height, roi.x, roi.x + roi.width);

        return;
    }

    //
    // C++: Mat Mat::adjustROI(int dtop, int dbottom, int dleft, int dright)
    //

/**
 * <p>Adjusts a submatrix size and position within the parent matrix.</p>
 *
 * <p>The method is complimentary to"Mat.locateROI". The typical use of these
 * functions is to determine the submatrix position within the parent matrix and
 * then shift the position somehow. Typically, it can be required for filtering
 * operations when pixels outside of the ROI should be taken into account. When
 * all the method parameters are positive, the ROI needs to grow in all
 * directions by the specified amount, for example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>A.adjustROI(2, 2, 2, 2);</p>
 *
 * <p>In this example, the matrix size is increased by 4 elements in each
 * direction. The matrix is shifted by 2 elements to the left and 2 elements up,
 * which brings in all the necessary pixels for the filtering with the 5x5
 * kernel.
 * </code></p>
 *
 * <p><code>adjustROI</code> forces the adjusted ROI to be inside of the parent
 * matrix that is boundaries of the adjusted ROI are constrained by boundaries
 * of the parent matrix. For example, if the submatrix <code>A</code> is located
 * in the first row of a parent matrix and you called <code>A.adjustROI(2, 2, 2,
 * 2)</code> then <code>A</code> will not be increased in the upward direction.</p>
 *
 * <p>The function is used internally by the OpenCV filtering functions, like
 * "filter2D", morphological operations, and so on.</p>
 *
 * @param dtop Shift of the top submatrix boundary upwards.
 * @param dbottom Shift of the bottom submatrix boundary downwards.
 * @param dleft Shift of the left submatrix boundary to the left.
 * @param dright Shift of the right submatrix boundary to the right.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-adjustroi">org.opencv.core.Mat.adjustROI</a>
 * @see org.opencv.imgproc.Imgproc#copyMakeBorder
 */
    public Mat adjustROI(int dtop, int dbottom, int dleft, int dright)
    {

        Mat retVal = new Mat(n_adjustROI(nativeObj, dtop, dbottom, dleft, dright));

        return retVal;
    }

    //
    // C++: void Mat::assignTo(Mat m, int type = -1)
    //

/**
 * <p>Provides a functional form of <code>convertTo</code>.</p>
 *
 * <p>This is an internally used method called by the "MatrixExpressions" engine.</p>
 *
 * @param m Destination array.
 * @param type Desired destination array depth (or -1 if it should be the same
 * as the source type).
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-assignto">org.opencv.core.Mat.assignTo</a>
 */
    public void assignTo(Mat m, int type)
    {

        n_assignTo(nativeObj, m.nativeObj, type);

        return;
    }

/**
 * <p>Provides a functional form of <code>convertTo</code>.</p>
 *
 * <p>This is an internally used method called by the "MatrixExpressions" engine.</p>
 *
 * @param m Destination array.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-assignto">org.opencv.core.Mat.assignTo</a>
 */
    public void assignTo(Mat m)
    {

        n_assignTo(nativeObj, m.nativeObj);

        return;
    }

    //
    // C++: int Mat::channels()
    //

/**
 * <p>Returns the number of matrix channels.</p>
 *
 * <p>The method returns the number of matrix channels.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-channels">org.opencv.core.Mat.channels</a>
 */
    public int channels()
    {

        int retVal = n_channels(nativeObj);

        return retVal;
    }

    //
    // C++: int Mat::checkVector(int elemChannels, int depth = -1, bool
    // requireContinuous = true)
    //

    public int checkVector(int elemChannels, int depth, boolean requireContinuous)
    {

        int retVal = n_checkVector(nativeObj, elemChannels, depth, requireContinuous);

        return retVal;
    }

    public int checkVector(int elemChannels, int depth)
    {

        int retVal = n_checkVector(nativeObj, elemChannels, depth);

        return retVal;
    }

    public int checkVector(int elemChannels)
    {

        int retVal = n_checkVector(nativeObj, elemChannels);

        return retVal;
    }

    //
    // C++: Mat Mat::clone()
    //

/**
 * <p>Creates a full copy of the array and the underlying data.</p>
 *
 * <p>The method creates a full copy of the array. The original <code>step[]</code>
 * is not taken into account. So, the array copy is a continuous array occupying
 * <code>total()*elemSize()</code> bytes.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-clone">org.opencv.core.Mat.clone</a>
 */
    public Mat clone()
    {

        Mat retVal = new Mat(n_clone(nativeObj));

        return retVal;
    }

    //
    // C++: Mat Mat::col(int x)
    //

/**
 * <p>Creates a matrix header for the specified matrix column.</p>
 *
 * <p>The method makes a new header for the specified matrix column and returns it.
 * This is an O(1) operation, regardless of the matrix size. The underlying data
 * of the new matrix is shared with the original matrix. See also the "Mat.row"
 * description.</p>
 *
 * @param x A 0-based column index.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-col">org.opencv.core.Mat.col</a>
 */
    public Mat col(int x)
    {

        Mat retVal = new Mat(n_col(nativeObj, x));

        return retVal;
    }

    //
    // C++: Mat Mat::colRange(int startcol, int endcol)
    //

/**
 * <p>Creates a matrix header for the specified column span.</p>
 *
 * <p>The method makes a new header for the specified column span of the matrix.
 * Similarly to "Mat.row" and "Mat.col", this is an O(1) operation.</p>
 *
 * @param startcol An inclusive 0-based start index of the column span.
 * @param endcol An exclusive 0-based ending index of the column span.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-colrange">org.opencv.core.Mat.colRange</a>
 */
    public Mat colRange(int startcol, int endcol)
    {

        Mat retVal = new Mat(n_colRange(nativeObj, startcol, endcol));

        return retVal;
    }

    //
    // C++: Mat Mat::colRange(Range r)
    //

/**
 * <p>Creates a matrix header for the specified column span.</p>
 *
 * <p>The method makes a new header for the specified column span of the matrix.
 * Similarly to "Mat.row" and "Mat.col", this is an O(1) operation.</p>
 *
 * @param r "Range" structure containing both the start and the end indices.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-colrange">org.opencv.core.Mat.colRange</a>
 */
    public Mat colRange(Range r)
    {

        Mat retVal = new Mat(n_colRange(nativeObj, r.start, r.end));

        return retVal;
    }

    //
    // C++: int Mat::dims()
    //

    public int dims()
    {

        int retVal = n_dims(nativeObj);

        return retVal;
    }

    //
    // C++: int Mat::cols()
    //

    public int cols()
    {

        int retVal = n_cols(nativeObj);

        return retVal;
    }

    //
    // C++: void Mat::convertTo(Mat& m, int rtype, double alpha = 1, double beta
    // = 0)
    //

/**
 * <p>Converts an array to another data type with optional scaling.</p>
 *
 * <p>The method converts source pixel values to the target data type.
 * <code>saturate_cast<></code> is applied at the end to avoid possible
 * overflows:</p>
 *
 * <p><em>m(x,y) = saturate _ cast&ltrType&gt(alpha(*this)(x,y) + beta)</em></p>
 *
 * @param m output matrix; if it does not have a proper size or type before the
 * operation, it is reallocated.
 * @param rtype desired output matrix type or, rather, the depth since the
 * number of channels are the same as the input has; if <code>rtype</code> is
 * negative, the output matrix will have the same type as the input.
 * @param alpha optional scale factor.
 * @param beta optional delta added to the scaled values.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-convertto">org.opencv.core.Mat.convertTo</a>
 */
    public void convertTo(Mat m, int rtype, double alpha, double beta)
    {

        n_convertTo(nativeObj, m.nativeObj, rtype, alpha, beta);

        return;
    }

/**
 * <p>Converts an array to another data type with optional scaling.</p>
 *
 * <p>The method converts source pixel values to the target data type.
 * <code>saturate_cast<></code> is applied at the end to avoid possible
 * overflows:</p>
 *
 * <p><em>m(x,y) = saturate _ cast&ltrType&gt(alpha(*this)(x,y) + beta)</em></p>
 *
 * @param m output matrix; if it does not have a proper size or type before the
 * operation, it is reallocated.
 * @param rtype desired output matrix type or, rather, the depth since the
 * number of channels are the same as the input has; if <code>rtype</code> is
 * negative, the output matrix will have the same type as the input.
 * @param alpha optional scale factor.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-convertto">org.opencv.core.Mat.convertTo</a>
 */
    public void convertTo(Mat m, int rtype, double alpha)
    {

        n_convertTo(nativeObj, m.nativeObj, rtype, alpha);

        return;
    }

/**
 * <p>Converts an array to another data type with optional scaling.</p>
 *
 * <p>The method converts source pixel values to the target data type.
 * <code>saturate_cast<></code> is applied at the end to avoid possible
 * overflows:</p>
 *
 * <p><em>m(x,y) = saturate _ cast&ltrType&gt(alpha(*this)(x,y) + beta)</em></p>
 *
 * @param m output matrix; if it does not have a proper size or type before the
 * operation, it is reallocated.
 * @param rtype desired output matrix type or, rather, the depth since the
 * number of channels are the same as the input has; if <code>rtype</code> is
 * negative, the output matrix will have the same type as the input.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-convertto">org.opencv.core.Mat.convertTo</a>
 */
    public void convertTo(Mat m, int rtype)
    {

        n_convertTo(nativeObj, m.nativeObj, rtype);

        return;
    }

    //
    // C++: void Mat::copyTo(Mat& m)
    //

/**
 * <p>Copies the matrix to another one.</p>
 *
 * <p>The method copies the matrix data to another matrix. Before copying the data,
 * the method invokes <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>m.create(this->size(), this->type);</p>
 *
 * <p>so that the destination matrix is reallocated if needed. While
 * <code>m.copyTo(m);</code> works flawlessly, the function does not handle the
 * case of a partial overlap between the source and the destination matrices.
 * </code></p>
 *
 * <p>When the operation mask is specified, and the <code>Mat.create</code> call
 * shown above reallocated the matrix, the newly allocated matrix is initialized
 * with all zeros before copying the data.</p>
 *
 * @param m Destination matrix. If it does not have a proper size or type before
 * the operation, it is reallocated.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-copyto">org.opencv.core.Mat.copyTo</a>
 */
    public void copyTo(Mat m)
    {

        n_copyTo(nativeObj, m.nativeObj);

        return;
    }

    //
    // C++: void Mat::copyTo(Mat& m, Mat mask)
    //

/**
 * <p>Copies the matrix to another one.</p>
 *
 * <p>The method copies the matrix data to another matrix. Before copying the data,
 * the method invokes <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>m.create(this->size(), this->type);</p>
 *
 * <p>so that the destination matrix is reallocated if needed. While
 * <code>m.copyTo(m);</code> works flawlessly, the function does not handle the
 * case of a partial overlap between the source and the destination matrices.
 * </code></p>
 *
 * <p>When the operation mask is specified, and the <code>Mat.create</code> call
 * shown above reallocated the matrix, the newly allocated matrix is initialized
 * with all zeros before copying the data.</p>
 *
 * @param m Destination matrix. If it does not have a proper size or type before
 * the operation, it is reallocated.
 * @param mask Operation mask. Its non-zero elements indicate which matrix
 * elements need to be copied.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-copyto">org.opencv.core.Mat.copyTo</a>
 */
    public void copyTo(Mat m, Mat mask)
    {

        n_copyTo(nativeObj, m.nativeObj, mask.nativeObj);

        return;
    }

    //
    // C++: void Mat::create(int rows, int cols, int type)
    //

/**
 * <p>Allocates new array data if needed.</p>
 *
 * <p>This is one of the key <code>Mat</code> methods. Most new-style OpenCV
 * functions and methods that produce arrays call this method for each output
 * array. The method uses the following algorithm:</p>
 * <ul>
 *   <li> If the current array shape and the type match the new ones, return
 * immediately. Otherwise, de-reference the previous data by calling
 * "Mat.release".
 *   <li> Initialize the new header.
 *   <li> Allocate the new data of <code>total()*elemSize()</code> bytes.
 *   <li> Allocate the new, associated with the data, reference counter and set
 * it to 1.
 * </ul>
 * <p>Such a scheme makes the memory management robust and efficient at the same
 * time and helps avoid extra typing for you. This means that usually there is
 * no need to explicitly allocate output arrays. That is, instead of writing:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat color;...</p>
 *
 * <p>Mat gray(color.rows, color.cols, color.depth());</p>
 *
 * <p>cvtColor(color, gray, CV_BGR2GRAY);</p>
 *
 * <p>you can simply write:</p>
 *
 * <p>Mat color;...</p>
 *
 * <p>Mat gray;</p>
 *
 * <p>cvtColor(color, gray, CV_BGR2GRAY);</p>
 *
 * <p>because <code>cvtColor</code>, as well as the most of OpenCV functions, calls
 * <code>Mat.create()</code> for the output array internally.
 * </code></p>
 *
 * @param rows New number of rows.
 * @param cols New number of columns.
 * @param type New matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-create">org.opencv.core.Mat.create</a>
 */
    public void create(int rows, int cols, int type)
    {

        n_create(nativeObj, rows, cols, type);

        return;
    }

    //
    // C++: void Mat::create(Size size, int type)
    //

/**
 * <p>Allocates new array data if needed.</p>
 *
 * <p>This is one of the key <code>Mat</code> methods. Most new-style OpenCV
 * functions and methods that produce arrays call this method for each output
 * array. The method uses the following algorithm:</p>
 * <ul>
 *   <li> If the current array shape and the type match the new ones, return
 * immediately. Otherwise, de-reference the previous data by calling
 * "Mat.release".
 *   <li> Initialize the new header.
 *   <li> Allocate the new data of <code>total()*elemSize()</code> bytes.
 *   <li> Allocate the new, associated with the data, reference counter and set
 * it to 1.
 * </ul>
 * <p>Such a scheme makes the memory management robust and efficient at the same
 * time and helps avoid extra typing for you. This means that usually there is
 * no need to explicitly allocate output arrays. That is, instead of writing:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat color;...</p>
 *
 * <p>Mat gray(color.rows, color.cols, color.depth());</p>
 *
 * <p>cvtColor(color, gray, CV_BGR2GRAY);</p>
 *
 * <p>you can simply write:</p>
 *
 * <p>Mat color;...</p>
 *
 * <p>Mat gray;</p>
 *
 * <p>cvtColor(color, gray, CV_BGR2GRAY);</p>
 *
 * <p>because <code>cvtColor</code>, as well as the most of OpenCV functions, calls
 * <code>Mat.create()</code> for the output array internally.
 * </code></p>
 *
 * @param size Alternative new matrix size specification: <code>Size(cols,
 * rows)</code>
 * @param type New matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-create">org.opencv.core.Mat.create</a>
 */
    public void create(Size size, int type)
    {

        n_create(nativeObj, size.width, size.height, type);

        return;
    }

    //
    // C++: Mat Mat::cross(Mat m)
    //

/**
 * <p>Computes a cross-product of two 3-element vectors.</p>
 *
 * <p>The method computes a cross-product of two 3-element vectors. The vectors
 * must be 3-element floating-point vectors of the same shape and size. The
 * result is another 3-element vector of the same shape and type as operands.</p>
 *
 * @param m Another cross-product operand.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-cross">org.opencv.core.Mat.cross</a>
 */
    public Mat cross(Mat m)
    {

        Mat retVal = new Mat(n_cross(nativeObj, m.nativeObj));

        return retVal;
    }

    //
    // C++: long Mat::dataAddr()
    //

    public long dataAddr()
    {

        long retVal = n_dataAddr(nativeObj);

        return retVal;
    }

    //
    // C++: int Mat::depth()
    //

/**
 * <p>Returns the depth of a matrix element.</p>
 *
 * <p>The method returns the identifier of the matrix element depth (the type of
 * each individual channel). For example, for a 16-bit signed element array, the
 * method returns <code>CV_16S</code>. A complete list of matrix types contains
 * the following values:</p>
 * <ul>
 *   <li> <code>CV_8U</code> - 8-bit unsigned integers (<code>0..255</code>)
 *   <li> <code>CV_8S</code> - 8-bit signed integers (<code>-128..127</code>)
 *   <li> <code>CV_16U</code> - 16-bit unsigned integers (<code>0..65535</code>)
 *   <li> <code>CV_16S</code> - 16-bit signed integers (<code>-32768..32767</code>)
 *   <li> <code>CV_32S</code> - 32-bit signed integers (<code>-2147483648..2147483647</code>)
 *   <li> <code>CV_32F</code> - 32-bit floating-point numbers (<code>-FLT_MAX..FLT_MAX,
 * INF, NAN</code>)
 *   <li> <code>CV_64F</code> - 64-bit floating-point numbers (<code>-DBL_MAX..DBL_MAX,
 * INF, NAN</code>)
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-depth">org.opencv.core.Mat.depth</a>
 */
    public int depth()
    {

        int retVal = n_depth(nativeObj);

        return retVal;
    }

    //
    // C++: Mat Mat::diag(int d = 0)
    //

/**
 * <p>Extracts a diagonal from a matrix, or creates a diagonal matrix.</p>
 *
 * <p>The method makes a new header for the specified matrix diagonal. The new
 * matrix is represented as a single-column matrix. Similarly to "Mat.row" and
 * "Mat.col", this is an O(1) operation.</p>
 *
 * @param d Single-column matrix that forms a diagonal matrix or index of the
 * diagonal, with the following values:
 * <ul>
 *   <li> d=0 is the main diagonal.
 *   <li> d>0 is a diagonal from the lower half. For example, <code>d=1</code>
 * means the diagonal is set immediately below the main one.
 *   <li> d<0 is a diagonal from the upper half. For example, <code>d=1</code>
 * means the diagonal is set immediately above the main one.
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-diag">org.opencv.core.Mat.diag</a>
 */
    public Mat diag(int d)
    {

        Mat retVal = new Mat(n_diag(nativeObj, d));

        return retVal;
    }

/**
 * <p>Extracts a diagonal from a matrix, or creates a diagonal matrix.</p>
 *
 * <p>The method makes a new header for the specified matrix diagonal. The new
 * matrix is represented as a single-column matrix. Similarly to "Mat.row" and
 * "Mat.col", this is an O(1) operation.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-diag">org.opencv.core.Mat.diag</a>
 */
    public Mat diag()
    {

        Mat retVal = new Mat(n_diag(nativeObj, 0));

        return retVal;
    }

    //
    // C++: static Mat Mat::diag(Mat d)
    //

/**
 * <p>Extracts a diagonal from a matrix, or creates a diagonal matrix.</p>
 *
 * <p>The method makes a new header for the specified matrix diagonal. The new
 * matrix is represented as a single-column matrix. Similarly to "Mat.row" and
 * "Mat.col", this is an O(1) operation.</p>
 *
 * @param d Single-column matrix that forms a diagonal matrix or index of the
 * diagonal, with the following values:
 * <ul>
 *   <li> d=0 is the main diagonal.
 *   <li> d>0 is a diagonal from the lower half. For example, <code>d=1</code>
 * means the diagonal is set immediately below the main one.
 *   <li> d<0 is a diagonal from the upper half. For example, <code>d=1</code>
 * means the diagonal is set immediately above the main one.
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-diag">org.opencv.core.Mat.diag</a>
 */
    public static Mat diag(Mat d)
    {

        Mat retVal = new Mat(n_diag(d.nativeObj));

        return retVal;
    }

    //
    // C++: double Mat::dot(Mat m)
    //

/**
 * <p>Computes a dot-product of two vectors.</p>
 *
 * <p>The method computes a dot-product of two matrices. If the matrices are not
 * single-column or single-row vectors, the top-to-bottom left-to-right scan
 * ordering is used to treat them as 1D vectors. The vectors must have the same
 * size and type. If the matrices have more than one channel, the dot products
 * from all the channels are summed together.</p>
 *
 * @param m another dot-product operand.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-dot">org.opencv.core.Mat.dot</a>
 */
    public double dot(Mat m)
    {

        double retVal = n_dot(nativeObj, m.nativeObj);

        return retVal;
    }

    //
    // C++: size_t Mat::elemSize()
    //

/**
 * <p>Returns the matrix element size in bytes.</p>
 *
 * <p>The method returns the matrix element size in bytes. For example, if the
 * matrix type is <code>CV_16SC3</code>, the method returns <code>3*sizeof(short)</code>
 * or 6.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-elemsize">org.opencv.core.Mat.elemSize</a>
 */
    public long elemSize()
    {

        long retVal = n_elemSize(nativeObj);

        return retVal;
    }

    //
    // C++: size_t Mat::elemSize1()
    //

/**
 * <p>Returns the size of each matrix element channel in bytes.</p>
 *
 * <p>The method returns the matrix element channel size in bytes, that is, it
 * ignores the number of channels. For example, if the matrix type is
 * <code>CV_16SC3</code>, the method returns <code>sizeof(short)</code> or 2.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-elemsize1">org.opencv.core.Mat.elemSize1</a>
 */
    public long elemSize1()
    {

        long retVal = n_elemSize1(nativeObj);

        return retVal;
    }

    //
    // C++: bool Mat::empty()
    //

/**
 * <p>Returns <code>true</code> if the array has no elements.</p>
 *
 * <p>The method returns <code>true</code> if <code>Mat.total()</code> is 0 or if
 * <code>Mat.data</code> is NULL. Because of <code>pop_back()</code> and
 * <code>resize()</code> methods <code>M.total() == 0</code> does not imply that
 * <code>M.data == NULL</code>.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-empty">org.opencv.core.Mat.empty</a>
 */
    public boolean empty()
    {

        boolean retVal = n_empty(nativeObj);

        return retVal;
    }

    //
    // C++: static Mat Mat::eye(int rows, int cols, int type)
    //

/**
 * <p>Returns an identity matrix of the specified size and type.</p>
 *
 * <p>The method returns a Matlab-style identity matrix initializer, similarly to
 * "Mat.zeros". Similarly to"Mat.ones", you can use a scale operation to
 * create a scaled identity matrix efficiently: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// make a 4x4 diagonal matrix with 0.1's on the diagonal.</p>
 *
 * <p>Mat A = Mat.eye(4, 4, CV_32F)*0.1;</p>
 *
 * @param rows Number of rows.
 * @param cols Number of columns.
 * @param type Created matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-eye">org.opencv.core.Mat.eye</a>
 */
    public static Mat eye(int rows, int cols, int type)
    {

        Mat retVal = new Mat(n_eye(rows, cols, type));

        return retVal;
    }

    //
    // C++: static Mat Mat::eye(Size size, int type)
    //

/**
 * <p>Returns an identity matrix of the specified size and type.</p>
 *
 * <p>The method returns a Matlab-style identity matrix initializer, similarly to
 * "Mat.zeros". Similarly to"Mat.ones", you can use a scale operation to
 * create a scaled identity matrix efficiently: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// make a 4x4 diagonal matrix with 0.1's on the diagonal.</p>
 *
 * <p>Mat A = Mat.eye(4, 4, CV_32F)*0.1;</p>
 *
 * @param size Alternative matrix size specification as <code>Size(cols,
 * rows)</code>.
 * @param type Created matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-eye">org.opencv.core.Mat.eye</a>
 */
    public static Mat eye(Size size, int type)
    {

        Mat retVal = new Mat(n_eye(size.width, size.height, type));

        return retVal;
    }

    //
    // C++: Mat Mat::inv(int method = DECOMP_LU)
    //

/**
 * <p>Inverses a matrix.</p>
 *
 * <p>The method performs a matrix inversion by means of matrix expressions. This
 * means that a temporary matrix inversion object is returned by the method and
 * can be used further as a part of more complex matrix expressions or can be
 * assigned to a matrix.</p>
 *
 * @param method Matrix inversion method. Possible values are the following:
 * <ul>
 *   <li> DECOMP_LU is the LU decomposition. The matrix must be non-singular.
 *   <li> DECOMP_CHOLESKY is the Cholesky <em>LL^T</em> decomposition for
 * symmetrical positively defined matrices only. This type is about twice faster
 * than LU on big matrices.
 *   <li> DECOMP_SVD is the SVD decomposition. If the matrix is singular or even
 * non-square, the pseudo inversion is computed.
 * </ul>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-inv">org.opencv.core.Mat.inv</a>
 */
    public Mat inv(int method)
    {

        Mat retVal = new Mat(n_inv(nativeObj, method));

        return retVal;
    }

/**
 * <p>Inverses a matrix.</p>
 *
 * <p>The method performs a matrix inversion by means of matrix expressions. This
 * means that a temporary matrix inversion object is returned by the method and
 * can be used further as a part of more complex matrix expressions or can be
 * assigned to a matrix.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-inv">org.opencv.core.Mat.inv</a>
 */
    public Mat inv()
    {

        Mat retVal = new Mat(n_inv(nativeObj));

        return retVal;
    }

    //
    // C++: bool Mat::isContinuous()
    //

/**
 * <p>Reports whether the matrix is continuous or not.</p>
 *
 * <p>The method returns <code>true</code> if the matrix elements are stored
 * continuously without gaps at the end of each row. Otherwise, it returns
 * <code>false</code>. Obviously, <code>1x1</code> or <code>1xN</code> matrices
 * are always continuous. Matrices created with "Mat.create" are always
 * continuous. But if you extract a part of the matrix using "Mat.col",
 * "Mat.diag", and so on, or constructed a matrix header for externally
 * allocated data, such matrices may no longer have this property.
 * The continuity flag is stored as a bit in the <code>Mat.flags</code> field
 * and is computed automatically when you construct a matrix header. Thus, the
 * continuity check is a very fast operation, though theoretically it could be
 * done as follows: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>// alternative implementation of Mat.isContinuous()</p>
 *
 * <p>bool myCheckMatContinuity(const Mat& m)</p>
 *
 *
 * <p>//return (m.flags & Mat.CONTINUOUS_FLAG) != 0;</p>
 *
 * <p>return m.rows == 1 || m.step == m.cols*m.elemSize();</p>
 *
 *
 * <p>The method is used in quite a few of OpenCV functions. The point is that
 * element-wise operations (such as arithmetic and logical operations, math
 * functions, alpha blending, color space transformations, and others) do not
 * depend on the image geometry. Thus, if all the input and output arrays are
 * continuous, the functions can process them as very long single-row vectors.
 * The example below illustrates how an alpha-blending function can be
 * implemented.</p>
 *
 * <p>template<typename T></p>
 *
 * <p>void alphaBlendRGBA(const Mat& src1, const Mat& src2, Mat& dst)</p>
 *
 *
 * <p>const float alpha_scale = (float)std.numeric_limits<T>.max(),</p>
 *
 * <p>inv_scale = 1.f/alpha_scale;</p>
 *
 * <p>CV_Assert(src1.type() == src2.type() &&</p>
 *
 * <p>src1.type() == CV_MAKETYPE(DataType<T>.depth, 4) &&</p>
 *
 * <p>src1.size() == src2.size());</p>
 *
 * <p>Size size = src1.size();</p>
 *
 * <p>dst.create(size, src1.type());</p>
 *
 * <p>// here is the idiom: check the arrays for continuity and,</p>
 *
 * <p>// if this is the case,</p>
 *
 * <p>// treat the arrays as 1D vectors</p>
 *
 * <p>if(src1.isContinuous() && src2.isContinuous() && dst.isContinuous())</p>
 *
 *
 * <p>size.width *= size.height;</p>
 *
 * <p>size.height = 1;</p>
 *
 *
 * <p>size.width *= 4;</p>
 *
 * <p>for(int i = 0; i < size.height; i++)</p>
 *
 *
 * <p>// when the arrays are continuous,</p>
 *
 * <p>// the outer loop is executed only once</p>
 *
 * <p>const T* ptr1 = src1.ptr<T>(i);</p>
 *
 * <p>const T* ptr2 = src2.ptr<T>(i);</p>
 *
 * <p>T* dptr = dst.ptr<T>(i);</p>
 *
 * <p>for(int j = 0; j < size.width; j += 4)</p>
 *
 *
 * <p>float alpha = ptr1[j+3]*inv_scale, beta = ptr2[j+3]*inv_scale;</p>
 *
 * <p>dptr[j] = saturate_cast<T>(ptr1[j]*alpha + ptr2[j]*beta);</p>
 *
 * <p>dptr[j+1] = saturate_cast<T>(ptr1[j+1]*alpha + ptr2[j+1]*beta);</p>
 *
 * <p>dptr[j+2] = saturate_cast<T>(ptr1[j+2]*alpha + ptr2[j+2]*beta);</p>
 *
 * <p>dptr[j+3] = saturate_cast<T>((1 - (1-alpha)*(1-beta))*alpha_scale);</p>
 *
 *
 *
 *
 * <p>This approach, while being very simple, can boost the performance of a simple
 * element-operation by 10-20 percents, especially if the image is rather small
 * and the operation is quite simple.
 * </code></p>
 *
 * <p>Another OpenCV idiom in this function, a call of "Mat.create" for the
 * destination array, that allocates the destination array unless it already has
 * the proper size and type. And while the newly allocated arrays are always
 * continuous, you still need to check the destination array because
 * "Mat.create" does not always allocate a new matrix.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-iscontinuous">org.opencv.core.Mat.isContinuous</a>
 */
    public boolean isContinuous()
    {

        boolean retVal = n_isContinuous(nativeObj);

        return retVal;
    }

    //
    // C++: bool Mat::isSubmatrix()
    //

    public boolean isSubmatrix()
    {

        boolean retVal = n_isSubmatrix(nativeObj);

        return retVal;
    }

    //
    // C++: void Mat::locateROI(Size wholeSize, Point ofs)
    //

/**
 * <p>Locates the matrix header within a parent matrix.</p>
 *
 * <p>After you extracted a submatrix from a matrix using "Mat.row", "Mat.col",
 * "Mat.rowRange", "Mat.colRange", and others, the resultant submatrix points
 * just to the part of the original big matrix. However, each submatrix contains
 * information (represented by <code>datastart</code> and <code>dataend</code>
 * fields) that helps reconstruct the original matrix size and the position of
 * the extracted submatrix within the original matrix. The method
 * <code>locateROI</code> does exactly that.</p>
 *
 * @param wholeSize Output parameter that contains the size of the whole matrix
 * containing <code>*this</code> as a part.
 * @param ofs Output parameter that contains an offset of <code>*this</code>
 * inside the whole matrix.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-locateroi">org.opencv.core.Mat.locateROI</a>
 */
    public void locateROI(Size wholeSize, Point ofs)
    {
        double[] wholeSize_out = new double[2];
        double[] ofs_out = new double[2];
        locateROI_0(nativeObj, wholeSize_out, ofs_out);
        if(wholeSize!=null){ wholeSize.width = wholeSize_out[0]; wholeSize.height = wholeSize_out[1]; }
        if(ofs!=null){ ofs.x = ofs_out[0]; ofs.y = ofs_out[1]; }
        return;
    }

    //
    // C++: Mat Mat::mul(Mat m, double scale = 1)
    //

/**
 * <p>Performs an element-wise multiplication or division of the two matrices.</p>
 *
 * <p>The method returns a temporary object encoding per-element array
 * multiplication, with optional scale. Note that this is not a matrix
 * multiplication that corresponds to a simpler "*" operator.
 * Example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat C = A.mul(5/B); // equivalent to divide(A, B, C, 5)</p>
 *
 * @param m Another array of the same type and the same size as
 * <code>*this</code>, or a matrix expression.
 * @param scale Optional scale factor.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mul">org.opencv.core.Mat.mul</a>
 */
    public Mat mul(Mat m, double scale)
    {

        Mat retVal = new Mat(n_mul(nativeObj, m.nativeObj, scale));

        return retVal;
    }

/**
 * <p>Performs an element-wise multiplication or division of the two matrices.</p>
 *
 * <p>The method returns a temporary object encoding per-element array
 * multiplication, with optional scale. Note that this is not a matrix
 * multiplication that corresponds to a simpler "*" operator.
 * Example: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat C = A.mul(5/B); // equivalent to divide(A, B, C, 5)</p>
 *
 * @param m Another array of the same type and the same size as
 * <code>*this</code>, or a matrix expression.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-mul">org.opencv.core.Mat.mul</a>
 */
    public Mat mul(Mat m)
    {

        Mat retVal = new Mat(n_mul(nativeObj, m.nativeObj));

        return retVal;
    }

    //
    // C++: static Mat Mat::ones(int rows, int cols, int type)
    //

/**
 * <p>Returns an array of all 1's of the specified size and type.</p>
 *
 * <p>The method returns a Matlab-style 1's array initializer, similarly
 * to"Mat.zeros". Note that using this method you can initialize an array with
 * an arbitrary value, using the following Matlab idiom: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A = Mat.ones(100, 100, CV_8U)*3; // make 100x100 matrix filled with 3.</p>
 *
 * <p>The above operation does not form a 100x100 matrix of 1's and then multiply
 * it by 3. Instead, it just remembers the scale factor (3 in this case) and use
 * it when actually invoking the matrix initializer.
 * </code></p>
 *
 * @param rows Number of rows.
 * @param cols Number of columns.
 * @param type Created matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-ones">org.opencv.core.Mat.ones</a>
 */
    public static Mat ones(int rows, int cols, int type)
    {

        Mat retVal = new Mat(n_ones(rows, cols, type));

        return retVal;
    }

    //
    // C++: static Mat Mat::ones(Size size, int type)
    //

/**
 * <p>Returns an array of all 1's of the specified size and type.</p>
 *
 * <p>The method returns a Matlab-style 1's array initializer, similarly
 * to"Mat.zeros". Note that using this method you can initialize an array with
 * an arbitrary value, using the following Matlab idiom: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A = Mat.ones(100, 100, CV_8U)*3; // make 100x100 matrix filled with 3.</p>
 *
 * <p>The above operation does not form a 100x100 matrix of 1's and then multiply
 * it by 3. Instead, it just remembers the scale factor (3 in this case) and use
 * it when actually invoking the matrix initializer.
 * </code></p>
 *
 * @param size Alternative to the matrix size specification <code>Size(cols,
 * rows)</code>.
 * @param type Created matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-ones">org.opencv.core.Mat.ones</a>
 */
    public static Mat ones(Size size, int type)
    {

        Mat retVal = new Mat(n_ones(size.width, size.height, type));

        return retVal;
    }

    //
    // C++: void Mat::push_back(Mat m)
    //

/**
 * <p>Adds elements to the bottom of the matrix.</p>
 *
 * <p>The methods add one or more elements to the bottom of the matrix. They
 * emulate the corresponding method of the STL vector class. When
 * <code>elem</code> is <code>Mat</code>, its type and the number of columns
 * must be the same as in the container matrix.</p>
 *
 * @param m Added line(s).
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-push-back">org.opencv.core.Mat.push_back</a>
 */
    public void push_back(Mat m)
    {

        n_push_back(nativeObj, m.nativeObj);

        return;
    }

    //
    // C++: void Mat::release()
    //

/**
 * <p>Decrements the reference counter and deallocates the matrix if needed.</p>
 *
 * <p>The method decrements the reference counter associated with the matrix data.
 * When the reference counter reaches 0, the matrix data is deallocated and the
 * data and the reference counter pointers are set to NULL's. If the matrix
 * header points to an external data set (see "Mat.Mat"), the reference counter
 * is NULL, and the method has no effect in this case.</p>
 *
 * <p>This method can be called manually to force the matrix data deallocation. But
 * since this method is automatically called in the destructor, or by any other
 * method that changes the data pointer, it is usually not needed. The reference
 * counter decrement and check for 0 is an atomic operation on the platforms
 * that support it. Thus, it is safe to operate on the same matrices
 * asynchronously in different threads.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-release">org.opencv.core.Mat.release</a>
 */
    public void release()
    {

        n_release(nativeObj);

        return;
    }

    //
    // C++: Mat Mat::reshape(int cn, int rows = 0)
    //

/**
 * <p>Changes the shape and/or the number of channels of a 2D matrix without
 * copying the data.</p>
 *
 * <p>The method makes a new matrix header for <code>*this</code> elements. The new
 * matrix may have a different size and/or different number of channels. Any
 * combination is possible if:</p>
 * <ul>
 *   <li> No extra elements are included into the new matrix and no elements are
 * excluded. Consequently, the product <code>rows*cols*channels()</code> must
 * stay the same after the transformation.
 *   <li> No data is copied. That is, this is an O(1) operation. Consequently,
 * if you change the number of rows, or the operation changes the indices of
 * elements row in some other way, the matrix must be continuous. See
 * "Mat.isContinuous".
 * </ul>
 * <p>For example, if there is a set of 3D points stored as an STL vector, and you
 * want to represent the points as a <code>3xN</code> matrix, do the following:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>std.vector<Point3f> vec;...</p>
 *
 * <p>Mat pointMat = Mat(vec). // convert vector to Mat, O(1) operation</p>
 *
 * <p>reshape(1). // make Nx3 1-channel matrix out of Nx1 3-channel.</p>
 *
 * <p>// Also, an O(1) operation</p>
 *
 * <p>t(); // finally, transpose the Nx3 matrix.</p>
 *
 * <p>// This involves copying all the elements</p>
 *
 * @param cn New number of channels. If the parameter is 0, the number of
 * channels remains the same.
 * @param rows New number of rows. If the parameter is 0, the number of rows
 * remains the same.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-reshape">org.opencv.core.Mat.reshape</a>
 */
    public Mat reshape(int cn, int rows)
    {

        Mat retVal = new Mat(n_reshape(nativeObj, cn, rows));

        return retVal;
    }

/**
 * <p>Changes the shape and/or the number of channels of a 2D matrix without
 * copying the data.</p>
 *
 * <p>The method makes a new matrix header for <code>*this</code> elements. The new
 * matrix may have a different size and/or different number of channels. Any
 * combination is possible if:</p>
 * <ul>
 *   <li> No extra elements are included into the new matrix and no elements are
 * excluded. Consequently, the product <code>rows*cols*channels()</code> must
 * stay the same after the transformation.
 *   <li> No data is copied. That is, this is an O(1) operation. Consequently,
 * if you change the number of rows, or the operation changes the indices of
 * elements row in some other way, the matrix must be continuous. See
 * "Mat.isContinuous".
 * </ul>
 * <p>For example, if there is a set of 3D points stored as an STL vector, and you
 * want to represent the points as a <code>3xN</code> matrix, do the following:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>std.vector<Point3f> vec;...</p>
 *
 * <p>Mat pointMat = Mat(vec). // convert vector to Mat, O(1) operation</p>
 *
 * <p>reshape(1). // make Nx3 1-channel matrix out of Nx1 3-channel.</p>
 *
 * <p>// Also, an O(1) operation</p>
 *
 * <p>t(); // finally, transpose the Nx3 matrix.</p>
 *
 * <p>// This involves copying all the elements</p>
 *
 * @param cn New number of channels. If the parameter is 0, the number of
 * channels remains the same.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-reshape">org.opencv.core.Mat.reshape</a>
 */
    public Mat reshape(int cn)
    {

        Mat retVal = new Mat(n_reshape(nativeObj, cn));

        return retVal;
    }

    //
    // C++: Mat Mat::row(int y)
    //

/**
 * <p>Creates a matrix header for the specified matrix row.</p>
 *
 * <p>The method makes a new header for the specified matrix row and returns it.
 * This is an O(1) operation, regardless of the matrix size. The underlying data
 * of the new matrix is shared with the original matrix. Here is the example of
 * one of the classical basic matrix processing operations, <code>axpy</code>,
 * used by LU and many other algorithms: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>inline void matrix_axpy(Mat& A, int i, int j, double alpha)</p>
 *
 *
 * <p>A.row(i) += A.row(j)*alpha;</p>
 *
 *
 * <p>Note: </code></p>
 *
 * <p>In the current implementation, the following code does not work as expected:
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A;...</p>
 *
 * <p>A.row(i) = A.row(j); // will not work</p>
 *
 * <p>This happens because <code>A.row(i)</code> forms a temporary header that is
 * further assigned to another header. Remember that each of these operations is
 * O(1), that is, no data is copied. Thus, the above assignment is not true if
 * you may have expected the j-th row to be copied to the i-th row. To achieve
 * that, you should either turn this simple assignment into an expression or use
 * the "Mat.copyTo" method:</p>
 *
 * <p>Mat A;...</p>
 *
 * <p>// works, but looks a bit obscure.</p>
 *
 * <p>A.row(i) = A.row(j) + 0;</p>
 *
 * <p>// this is a bit longer, but the recommended method.</p>
 *
 * <p>A.row(j).copyTo(A.row(i));</p>
 *
 * @param y A 0-based row index.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-row">org.opencv.core.Mat.row</a>
 */
    public Mat row(int y)
    {

        Mat retVal = new Mat(n_row(nativeObj, y));

        return retVal;
    }

    //
    // C++: Mat Mat::rowRange(int startrow, int endrow)
    //

/**
 * <p>Creates a matrix header for the specified row span.</p>
 *
 * <p>The method makes a new header for the specified row span of the matrix.
 * Similarly to "Mat.row" and "Mat.col", this is an O(1) operation.</p>
 *
 * @param startrow An inclusive 0-based start index of the row span.
 * @param endrow An exclusive 0-based ending index of the row span.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-rowrange">org.opencv.core.Mat.rowRange</a>
 */
    public Mat rowRange(int startrow, int endrow)
    {

        Mat retVal = new Mat(n_rowRange(nativeObj, startrow, endrow));

        return retVal;
    }

    //
    // C++: Mat Mat::rowRange(Range r)
    //

/**
 * <p>Creates a matrix header for the specified row span.</p>
 *
 * <p>The method makes a new header for the specified row span of the matrix.
 * Similarly to "Mat.row" and "Mat.col", this is an O(1) operation.</p>
 *
 * @param r "Range" structure containing both the start and the end indices.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-rowrange">org.opencv.core.Mat.rowRange</a>
 */
    public Mat rowRange(Range r)
    {

        Mat retVal = new Mat(n_rowRange(nativeObj, r.start, r.end));

        return retVal;
    }

    //
    // C++: int Mat::rows()
    //

    public int rows()
    {

        int retVal = n_rows(nativeObj);

        return retVal;
    }

    //
    // C++: Mat Mat::operator =(Scalar s)
    //

    public Mat setTo(Scalar s)
    {

        Mat retVal = new Mat(n_setTo(nativeObj, s.val[0], s.val[1], s.val[2], s.val[3]));

        return retVal;
    }

    //
    // C++: Mat Mat::setTo(Scalar value, Mat mask = Mat())
    //

/**
 * <p>Sets all or some of the array elements to the specified value.</p>
 *
 * @param value Assigned scalar converted to the actual array type.
 * @param mask Operation mask of the same size as <code>*this</code>. This is an
 * advanced variant of the <code>Mat.operator=(const Scalar& s)</code>
 * operator.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-setto">org.opencv.core.Mat.setTo</a>
 */
    public Mat setTo(Scalar value, Mat mask)
    {

        Mat retVal = new Mat(n_setTo(nativeObj, value.val[0], value.val[1], value.val[2], value.val[3], mask.nativeObj));

        return retVal;
    }

    //
    // C++: Mat Mat::setTo(Mat value, Mat mask = Mat())
    //

/**
 * <p>Sets all or some of the array elements to the specified value.</p>
 *
 * @param value Assigned scalar converted to the actual array type.
 * @param mask Operation mask of the same size as <code>*this</code>. This is an
 * advanced variant of the <code>Mat.operator=(const Scalar& s)</code>
 * operator.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-setto">org.opencv.core.Mat.setTo</a>
 */
    public Mat setTo(Mat value, Mat mask)
    {

        Mat retVal = new Mat(n_setTo(nativeObj, value.nativeObj, mask.nativeObj));

        return retVal;
    }

/**
 * <p>Sets all or some of the array elements to the specified value.</p>
 *
 * @param value Assigned scalar converted to the actual array type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-setto">org.opencv.core.Mat.setTo</a>
 */
    public Mat setTo(Mat value)
    {

        Mat retVal = new Mat(n_setTo(nativeObj, value.nativeObj));

        return retVal;
    }

    //
    // C++: Size Mat::size()
    //

/**
 * <p>Returns a matrix size.</p>
 *
 * <p>The method returns a matrix size: <code>Size(cols, rows)</code>. When the
 * matrix is more than 2-dimensional, the returned size is (-1, -1).</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-size">org.opencv.core.Mat.size</a>
 */
    public Size size()
    {

        Size retVal = new Size(n_size(nativeObj));

        return retVal;
    }

    //
    // C++: size_t Mat::step1(int i = 0)
    //

/**
 * <p>Returns a normalized step.</p>
 *
 * <p>The method returns a matrix step divided by "Mat.elemSize1()". It can be
 * useful to quickly access an arbitrary matrix element.</p>
 *
 * @param i a i
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-step1">org.opencv.core.Mat.step1</a>
 */
    public long step1(int i)
    {

        long retVal = n_step1(nativeObj, i);

        return retVal;
    }

/**
 * <p>Returns a normalized step.</p>
 *
 * <p>The method returns a matrix step divided by "Mat.elemSize1()". It can be
 * useful to quickly access an arbitrary matrix element.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-step1">org.opencv.core.Mat.step1</a>
 */
    public long step1()
    {

        long retVal = n_step1(nativeObj);

        return retVal;
    }

    //
    // C++: Mat Mat::operator()(int rowStart, int rowEnd, int colStart, int
    // colEnd)
    //

/**
 * <p>Extracts a rectangular submatrix.</p>
 *
 * <p>The operators make a new header for the specified sub-array of
 * <code>*this</code>. They are the most generalized forms of "Mat.row",
 * "Mat.col", "Mat.rowRange", and "Mat.colRange". For example,
 * <code>A(Range(0, 10), Range.all())</code> is equivalent to <code>A.rowRange(0,
 * 10)</code>. Similarly to all of the above, the operators are O(1) operations,
 * that is, no matrix data is copied.</p>
 *
 * @param rowStart a rowStart
 * @param rowEnd a rowEnd
 * @param colStart a colStart
 * @param colEnd a colEnd
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-operator">org.opencv.core.Mat.operator()</a>
 */
    public Mat submat(int rowStart, int rowEnd, int colStart, int colEnd)
    {

        Mat retVal = new Mat(n_submat_rr(nativeObj, rowStart, rowEnd, colStart, colEnd));

        return retVal;
    }

    //
    // C++: Mat Mat::operator()(Range rowRange, Range colRange)
    //

/**
 * <p>Extracts a rectangular submatrix.</p>
 *
 * <p>The operators make a new header for the specified sub-array of
 * <code>*this</code>. They are the most generalized forms of "Mat.row",
 * "Mat.col", "Mat.rowRange", and "Mat.colRange". For example,
 * <code>A(Range(0, 10), Range.all())</code> is equivalent to <code>A.rowRange(0,
 * 10)</code>. Similarly to all of the above, the operators are O(1) operations,
 * that is, no matrix data is copied.</p>
 *
 * @param rowRange Start and end row of the extracted submatrix. The upper
 * boundary is not included. To select all the rows, use <code>Range.all()</code>.
 * @param colRange Start and end column of the extracted submatrix. The upper
 * boundary is not included. To select all the columns, use <code>Range.all()</code>.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-operator">org.opencv.core.Mat.operator()</a>
 */
    public Mat submat(Range rowRange, Range colRange)
    {

        Mat retVal = new Mat(n_submat_rr(nativeObj, rowRange.start, rowRange.end, colRange.start, colRange.end));

        return retVal;
    }

    //
    // C++: Mat Mat::operator()(Rect roi)
    //

/**
 * <p>Extracts a rectangular submatrix.</p>
 *
 * <p>The operators make a new header for the specified sub-array of
 * <code>*this</code>. They are the most generalized forms of "Mat.row",
 * "Mat.col", "Mat.rowRange", and "Mat.colRange". For example,
 * <code>A(Range(0, 10), Range.all())</code> is equivalent to <code>A.rowRange(0,
 * 10)</code>. Similarly to all of the above, the operators are O(1) operations,
 * that is, no matrix data is copied.</p>
 *
 * @param roi Extracted submatrix specified as a rectangle.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-operator">org.opencv.core.Mat.operator()</a>
 */
    public Mat submat(Rect roi)
    {

        Mat retVal = new Mat(n_submat(nativeObj, roi.x, roi.y, roi.width, roi.height));

        return retVal;
    }

    //
    // C++: Mat Mat::t()
    //

/**
 * <p>Transposes a matrix.</p>
 *
 * <p>The method performs matrix transposition by means of matrix expressions. It
 * does not perform the actual transposition but returns a temporary matrix
 * transposition object that can be further used as a part of more complex
 * matrix expressions or can be assigned to a matrix: <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A1 = A + Mat.eye(A.size(), A.type)*lambda;</p>
 *
 * <p>Mat C = A1.t()*A1; // compute (A + lambda*I)^t * (A + lamda*I)</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-t">org.opencv.core.Mat.t</a>
 */
    public Mat t()
    {

        Mat retVal = new Mat(n_t(nativeObj));

        return retVal;
    }

    //
    // C++: size_t Mat::total()
    //

/**
 * <p>Returns the total number of array elements.</p>
 *
 * <p>The method returns the number of array elements (a number of pixels if the
 * array represents an image).</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-total">org.opencv.core.Mat.total</a>
 */
    public long total()
    {

        long retVal = n_total(nativeObj);

        return retVal;
    }

    //
    // C++: int Mat::type()
    //

/**
 * <p>Returns the type of a matrix element.</p>
 *
 * <p>The method returns a matrix element type. This is an identifier compatible
 * with the <code>CvMat</code> type system, like <code>CV_16SC3</code> or 16-bit
 * signed 3-channel array, and so on.</p>
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-type">org.opencv.core.Mat.type</a>
 */
    public int type()
    {

        int retVal = n_type(nativeObj);

        return retVal;
    }

    //
    // C++: static Mat Mat::zeros(int rows, int cols, int type)
    //

/**
 * <p>Returns a zero array of the specified size and type.</p>
 *
 * <p>The method returns a Matlab-style zero array initializer. It can be used to
 * quickly form a constant array as a function parameter, part of a matrix
 * expression, or as a matrix initializer.
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A;</p>
 *
 * <p>A = Mat.zeros(3, 3, CV_32F);</p>
 *
 * <p>In the example above, a new matrix is allocated only if <code>A</code> is not
 * a 3x3 floating-point matrix. Otherwise, the existing matrix <code>A</code> is
 * filled with zeros.
 * </code></p>
 *
 * @param rows Number of rows.
 * @param cols Number of columns.
 * @param type Created matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-zeros">org.opencv.core.Mat.zeros</a>
 */
    public static Mat zeros(int rows, int cols, int type)
    {

        Mat retVal = new Mat(n_zeros(rows, cols, type));

        return retVal;
    }

    //
    // C++: static Mat Mat::zeros(Size size, int type)
    //

/**
 * <p>Returns a zero array of the specified size and type.</p>
 *
 * <p>The method returns a Matlab-style zero array initializer. It can be used to
 * quickly form a constant array as a function parameter, part of a matrix
 * expression, or as a matrix initializer.
 * <code></p>
 *
 * <p>// C++ code:</p>
 *
 * <p>Mat A;</p>
 *
 * <p>A = Mat.zeros(3, 3, CV_32F);</p>
 *
 * <p>In the example above, a new matrix is allocated only if <code>A</code> is not
 * a 3x3 floating-point matrix. Otherwise, the existing matrix <code>A</code> is
 * filled with zeros.
 * </code></p>
 *
 * @param size Alternative to the matrix size specification <code>Size(cols,
 * rows)</code>.
 * @param type Created matrix type.
 *
 * @see <a href="http://docs.opencv.org/modules/core/doc/basic_structures.html#mat-zeros">org.opencv.core.Mat.zeros</a>
 */
    public static Mat zeros(Size size, int type)
    {

        Mat retVal = new Mat(n_zeros(size.width, size.height, type));

        return retVal;
    }

    @Override
    protected void finalize() throws Throwable {
        n_delete(nativeObj);
        super.finalize();
    }

    @Override
    public String toString() {
        return "Mat [ " +
                rows() + "*" + cols() + "*" + CvType.typeToString(type()) +
                ", isCont=" + isContinuous() + ", isSubmat=" + isSubmatrix() +
                ", nativeObj=0x" + Long.toHexString(nativeObj) +
                ", dataAddr=0x" + Long.toHexString(dataAddr()) +
                " ]";
    }

    public String dump() {
        return nDump(nativeObj);
    }

    public int put(int row, int col, double... data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        return nPutD(nativeObj, row, col, data.length, data);
    }

    public int put(int row, int col, float[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32F) {
            return nPutF(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int put(int row, int col, int[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32S) {
            return nPutI(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int put(int row, int col, short[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_16U || CvType.depth(t) == CvType.CV_16S) {
            return nPutS(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int put(int row, int col, byte[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nPutB(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, byte[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_8U || CvType.depth(t) == CvType.CV_8S) {
            return nGetB(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, short[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_16U || CvType.depth(t) == CvType.CV_16S) {
            return nGetS(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, int[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32S) {
            return nGetI(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, float[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_32F) {
            return nGetF(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, double[] data) {
        int t = type();
        if (data == null || data.length % CvType.channels(t) != 0)
            throw new java.lang.UnsupportedOperationException(
                    "Provided data element number (" +
                            (data == null ? 0 : data.length) +
                            ") should be multiple of the Mat channels count (" +
                            CvType.channels(t) + ")");
        if (CvType.depth(t) == CvType.CV_64F) {
            return nGetD(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public double[] get(int row, int col) {
        return nGet(nativeObj, row, col);
    }

    public int height() {
        return rows();
    }

    public int width() {
        return cols();
    }

    public long getNativeObjAddr() {
        return nativeObj;
    }

    // C++: Mat::Mat()
    private static native long n_Mat();

    // C++: Mat::Mat(int rows, int cols, int type)
    private static native long n_Mat(int rows, int cols, int type);

    // C++: Mat::Mat(Size size, int type)
    private static native long n_Mat(double size_width, double size_height, int type);

    // C++: Mat::Mat(int rows, int cols, int type, Scalar s)
    private static native long n_Mat(int rows, int cols, int type, double s_val0, double s_val1, double s_val2, double s_val3);

    // C++: Mat::Mat(Size size, int type, Scalar s)
    private static native long n_Mat(double size_width, double size_height, int type, double s_val0, double s_val1, double s_val2, double s_val3);

    // C++: Mat::Mat(Mat m, Range rowRange, Range colRange = Range::all())
    private static native long n_Mat(long m_nativeObj, int rowRange_start, int rowRange_end, int colRange_start, int colRange_end);

    private static native long n_Mat(long m_nativeObj, int rowRange_start, int rowRange_end);

    // C++: Mat Mat::adjustROI(int dtop, int dbottom, int dleft, int dright)
    private static native long n_adjustROI(long nativeObj, int dtop, int dbottom, int dleft, int dright);

    // C++: void Mat::assignTo(Mat m, int type = -1)
    private static native void n_assignTo(long nativeObj, long m_nativeObj, int type);

    private static native void n_assignTo(long nativeObj, long m_nativeObj);

    // C++: int Mat::channels()
    private static native int n_channels(long nativeObj);

    // C++: int Mat::checkVector(int elemChannels, int depth = -1, bool
    // requireContinuous = true)
    private static native int n_checkVector(long nativeObj, int elemChannels, int depth, boolean requireContinuous);

    private static native int n_checkVector(long nativeObj, int elemChannels, int depth);

    private static native int n_checkVector(long nativeObj, int elemChannels);

    // C++: Mat Mat::clone()
    private static native long n_clone(long nativeObj);

    // C++: Mat Mat::col(int x)
    private static native long n_col(long nativeObj, int x);

    // C++: Mat Mat::colRange(int startcol, int endcol)
    private static native long n_colRange(long nativeObj, int startcol, int endcol);

    // C++: int Mat::dims()
    private static native int n_dims(long nativeObj);

    // C++: int Mat::cols()
    private static native int n_cols(long nativeObj);

    // C++: void Mat::convertTo(Mat& m, int rtype, double alpha = 1, double beta
    // = 0)
    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype, double alpha, double beta);

    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype, double alpha);

    private static native void n_convertTo(long nativeObj, long m_nativeObj, int rtype);

    // C++: void Mat::copyTo(Mat& m)
    private static native void n_copyTo(long nativeObj, long m_nativeObj);

    // C++: void Mat::copyTo(Mat& m, Mat mask)
    private static native void n_copyTo(long nativeObj, long m_nativeObj, long mask_nativeObj);

    // C++: void Mat::create(int rows, int cols, int type)
    private static native void n_create(long nativeObj, int rows, int cols, int type);

    // C++: void Mat::create(Size size, int type)
    private static native void n_create(long nativeObj, double size_width, double size_height, int type);

    // C++: Mat Mat::cross(Mat m)
    private static native long n_cross(long nativeObj, long m_nativeObj);

    // C++: long Mat::dataAddr()
    private static native long n_dataAddr(long nativeObj);

    // C++: int Mat::depth()
    private static native int n_depth(long nativeObj);

    // C++: Mat Mat::diag(int d = 0)
    private static native long n_diag(long nativeObj, int d);

    // C++: static Mat Mat::diag(Mat d)
    private static native long n_diag(long d_nativeObj);

    // C++: double Mat::dot(Mat m)
    private static native double n_dot(long nativeObj, long m_nativeObj);

    // C++: size_t Mat::elemSize()
    private static native long n_elemSize(long nativeObj);

    // C++: size_t Mat::elemSize1()
    private static native long n_elemSize1(long nativeObj);

    // C++: bool Mat::empty()
    private static native boolean n_empty(long nativeObj);

    // C++: static Mat Mat::eye(int rows, int cols, int type)
    private static native long n_eye(int rows, int cols, int type);

    // C++: static Mat Mat::eye(Size size, int type)
    private static native long n_eye(double size_width, double size_height, int type);

    // C++: Mat Mat::inv(int method = DECOMP_LU)
    private static native long n_inv(long nativeObj, int method);

    private static native long n_inv(long nativeObj);

    // C++: bool Mat::isContinuous()
    private static native boolean n_isContinuous(long nativeObj);

    // C++: bool Mat::isSubmatrix()
    private static native boolean n_isSubmatrix(long nativeObj);

    // C++: void Mat::locateROI(Size wholeSize, Point ofs)
    private static native void locateROI_0(long nativeObj, double[] wholeSize_out, double[] ofs_out);

    // C++: Mat Mat::mul(Mat m, double scale = 1)
    private static native long n_mul(long nativeObj, long m_nativeObj, double scale);

    private static native long n_mul(long nativeObj, long m_nativeObj);

    // C++: static Mat Mat::ones(int rows, int cols, int type)
    private static native long n_ones(int rows, int cols, int type);

    // C++: static Mat Mat::ones(Size size, int type)
    private static native long n_ones(double size_width, double size_height, int type);

    // C++: void Mat::push_back(Mat m)
    private static native void n_push_back(long nativeObj, long m_nativeObj);

    // C++: void Mat::release()
    private static native void n_release(long nativeObj);

    // C++: Mat Mat::reshape(int cn, int rows = 0)
    private static native long n_reshape(long nativeObj, int cn, int rows);

    private static native long n_reshape(long nativeObj, int cn);

    // C++: Mat Mat::row(int y)
    private static native long n_row(long nativeObj, int y);

    // C++: Mat Mat::rowRange(int startrow, int endrow)
    private static native long n_rowRange(long nativeObj, int startrow, int endrow);

    // C++: int Mat::rows()
    private static native int n_rows(long nativeObj);

    // C++: Mat Mat::operator =(Scalar s)
    private static native long n_setTo(long nativeObj, double s_val0, double s_val1, double s_val2, double s_val3);

    // C++: Mat Mat::setTo(Scalar value, Mat mask = Mat())
    private static native long n_setTo(long nativeObj, double s_val0, double s_val1, double s_val2, double s_val3, long mask_nativeObj);

    // C++: Mat Mat::setTo(Mat value, Mat mask = Mat())
    private static native long n_setTo(long nativeObj, long value_nativeObj, long mask_nativeObj);

    private static native long n_setTo(long nativeObj, long value_nativeObj);

    // C++: Size Mat::size()
    private static native double[] n_size(long nativeObj);

    // C++: size_t Mat::step1(int i = 0)
    private static native long n_step1(long nativeObj, int i);

    private static native long n_step1(long nativeObj);

    // C++: Mat Mat::operator()(Range rowRange, Range colRange)
    private static native long n_submat_rr(long nativeObj, int rowRange_start, int rowRange_end, int colRange_start, int colRange_end);

    // C++: Mat Mat::operator()(Rect roi)
    private static native long n_submat(long nativeObj, int roi_x, int roi_y, int roi_width, int roi_height);

    // C++: Mat Mat::t()
    private static native long n_t(long nativeObj);

    // C++: size_t Mat::total()
    private static native long n_total(long nativeObj);

    // C++: int Mat::type()
    private static native int n_type(long nativeObj);

    // C++: static Mat Mat::zeros(int rows, int cols, int type)
    private static native long n_zeros(int rows, int cols, int type);

    // C++: static Mat Mat::zeros(Size size, int type)
    private static native long n_zeros(double size_width, double size_height, int type);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

    private static native int nPutD(long self, int row, int col, int count, double[] data);

    private static native int nPutF(long self, int row, int col, int count, float[] data);

    private static native int nPutI(long self, int row, int col, int count, int[] data);

    private static native int nPutS(long self, int row, int col, int count, short[] data);

    private static native int nPutB(long self, int row, int col, int count, byte[] data);

    private static native int nGetB(long self, int row, int col, int count, byte[] vals);

    private static native int nGetS(long self, int row, int col, int count, short[] vals);

    private static native int nGetI(long self, int row, int col, int count, int[] vals);

    private static native int nGetF(long self, int row, int col, int count, float[] vals);

    private static native int nGetD(long self, int row, int col, int count, double[] vals);

    private static native double[] nGet(long self, int row, int col);

    private static native String nDump(long self);
}
