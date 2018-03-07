/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgproc;

import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.core.MatOfFloat6;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.utils.Converters;

// C++: class Subdiv2D
public class Subdiv2D {

    protected final long nativeObj;
    protected Subdiv2D(long addr) { nativeObj = addr; }

    public static final int
            PTLOC_ERROR = -2,
            PTLOC_OUTSIDE_RECT = -1,
            PTLOC_INSIDE = 0,
            PTLOC_VERTEX = 1,
            PTLOC_ON_EDGE = 2,
            NEXT_AROUND_ORG = 0x00,
            NEXT_AROUND_DST = 0x22,
            PREV_AROUND_ORG = 0x11,
            PREV_AROUND_DST = 0x33,
            NEXT_AROUND_LEFT = 0x13,
            NEXT_AROUND_RIGHT = 0x31,
            PREV_AROUND_LEFT = 0x20,
            PREV_AROUND_RIGHT = 0x02;

    //
    // C++:   Subdiv2D::Subdiv2D()
    //

    public   Subdiv2D()
    {

        nativeObj = Subdiv2D_0();

        return;
    }

    //
    // C++:   Subdiv2D::Subdiv2D(Rect rect)
    //

    public   Subdiv2D(Rect rect)
    {

        nativeObj = Subdiv2D_1(rect.x, rect.y, rect.width, rect.height);

        return;
    }

    //
    // C++:  int Subdiv2D::edgeDst(int edge, Point2f* dstpt = 0)
    //

    public  int edgeDst(int edge, Point dstpt)
    {
        double[] dstpt_out = new double[2];
        int retVal = edgeDst_0(nativeObj, edge, dstpt_out);
        if(dstpt!=null){ dstpt.x = dstpt_out[0]; dstpt.y = dstpt_out[1]; }
        return retVal;
    }

    public  int edgeDst(int edge)
    {

        int retVal = edgeDst_1(nativeObj, edge);

        return retVal;
    }

    //
    // C++:  int Subdiv2D::edgeOrg(int edge, Point2f* orgpt = 0)
    //

    public  int edgeOrg(int edge, Point orgpt)
    {
        double[] orgpt_out = new double[2];
        int retVal = edgeOrg_0(nativeObj, edge, orgpt_out);
        if(orgpt!=null){ orgpt.x = orgpt_out[0]; orgpt.y = orgpt_out[1]; }
        return retVal;
    }

    public  int edgeOrg(int edge)
    {

        int retVal = edgeOrg_1(nativeObj, edge);

        return retVal;
    }

    //
    // C++:  int Subdiv2D::findNearest(Point2f pt, Point2f* nearestPt = 0)
    //

    public  int findNearest(Point pt, Point nearestPt)
    {
        double[] nearestPt_out = new double[2];
        int retVal = findNearest_0(nativeObj, pt.x, pt.y, nearestPt_out);
        if(nearestPt!=null){ nearestPt.x = nearestPt_out[0]; nearestPt.y = nearestPt_out[1]; }
        return retVal;
    }

    public  int findNearest(Point pt)
    {

        int retVal = findNearest_1(nativeObj, pt.x, pt.y);

        return retVal;
    }

    //
    // C++:  int Subdiv2D::getEdge(int edge, int nextEdgeType)
    //

    public  int getEdge(int edge, int nextEdgeType)
    {

        int retVal = getEdge_0(nativeObj, edge, nextEdgeType);

        return retVal;
    }

    //
    // C++:  void Subdiv2D::getEdgeList(vector_Vec4f& edgeList)
    //

    public  void getEdgeList(MatOfFloat4 edgeList)
    {
        Mat edgeList_mat = edgeList;
        getEdgeList_0(nativeObj, edgeList_mat.nativeObj);

        return;
    }

    //
    // C++:  void Subdiv2D::getTriangleList(vector_Vec6f& triangleList)
    //

    public  void getTriangleList(MatOfFloat6 triangleList)
    {
        Mat triangleList_mat = triangleList;
        getTriangleList_0(nativeObj, triangleList_mat.nativeObj);

        return;
    }

    //
    // C++:  Point2f Subdiv2D::getVertex(int vertex, int* firstEdge = 0)
    //

    public  Point getVertex(int vertex, int[] firstEdge)
    {
        double[] firstEdge_out = new double[1];
        Point retVal = new Point(getVertex_0(nativeObj, vertex, firstEdge_out));
        if(firstEdge!=null) firstEdge[0] = (int)firstEdge_out[0];
        return retVal;
    }

    public  Point getVertex(int vertex)
    {

        Point retVal = new Point(getVertex_1(nativeObj, vertex));

        return retVal;
    }

    //
    // C++:  void Subdiv2D::getVoronoiFacetList(vector_int idx, vector_vector_Point2f& facetList, vector_Point2f& facetCenters)
    //

    public  void getVoronoiFacetList(MatOfInt idx, List<MatOfPoint2f> facetList, MatOfPoint2f facetCenters)
    {
        Mat idx_mat = idx;
        Mat facetList_mat = new Mat();
        Mat facetCenters_mat = facetCenters;
        getVoronoiFacetList_0(nativeObj, idx_mat.nativeObj, facetList_mat.nativeObj, facetCenters_mat.nativeObj);
        Converters.Mat_to_vector_vector_Point2f(facetList_mat, facetList);
        return;
    }

    //
    // C++:  void Subdiv2D::initDelaunay(Rect rect)
    //

    public  void initDelaunay(Rect rect)
    {

        initDelaunay_0(nativeObj, rect.x, rect.y, rect.width, rect.height);

        return;
    }

    //
    // C++:  int Subdiv2D::insert(Point2f pt)
    //

    public  int insert(Point pt)
    {

        int retVal = insert_0(nativeObj, pt.x, pt.y);

        return retVal;
    }

    //
    // C++:  void Subdiv2D::insert(vector_Point2f ptvec)
    //

    public  void insert(MatOfPoint2f ptvec)
    {
        Mat ptvec_mat = ptvec;
        insert_1(nativeObj, ptvec_mat.nativeObj);

        return;
    }

    //
    // C++:  int Subdiv2D::locate(Point2f pt, int& edge, int& vertex)
    //

    public  int locate(Point pt, int[] edge, int[] vertex)
    {
        double[] edge_out = new double[1];
        double[] vertex_out = new double[1];
        int retVal = locate_0(nativeObj, pt.x, pt.y, edge_out, vertex_out);
        if(edge!=null) edge[0] = (int)edge_out[0];
        if(vertex!=null) vertex[0] = (int)vertex_out[0];
        return retVal;
    }

    //
    // C++:  int Subdiv2D::nextEdge(int edge)
    //

    public  int nextEdge(int edge)
    {

        int retVal = nextEdge_0(nativeObj, edge);

        return retVal;
    }

    //
    // C++:  int Subdiv2D::rotateEdge(int edge, int rotate)
    //

    public  int rotateEdge(int edge, int rotate)
    {

        int retVal = rotateEdge_0(nativeObj, edge, rotate);

        return retVal;
    }

    //
    // C++:  int Subdiv2D::symEdge(int edge)
    //

    public  int symEdge(int edge)
    {

        int retVal = symEdge_0(nativeObj, edge);

        return retVal;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }


    // C++:   Subdiv2D::Subdiv2D()
    private static native long Subdiv2D_0();

    // C++:   Subdiv2D::Subdiv2D(Rect rect)
    private static native long Subdiv2D_1(int rect_x, int rect_y, int rect_width, int rect_height);

    // C++:  int Subdiv2D::edgeDst(int edge, Point2f* dstpt = 0)
    private static native int edgeDst_0(long nativeObj, int edge, double[] dstpt_out);
    private static native int edgeDst_1(long nativeObj, int edge);

    // C++:  int Subdiv2D::edgeOrg(int edge, Point2f* orgpt = 0)
    private static native int edgeOrg_0(long nativeObj, int edge, double[] orgpt_out);
    private static native int edgeOrg_1(long nativeObj, int edge);

    // C++:  int Subdiv2D::findNearest(Point2f pt, Point2f* nearestPt = 0)
    private static native int findNearest_0(long nativeObj, double pt_x, double pt_y, double[] nearestPt_out);
    private static native int findNearest_1(long nativeObj, double pt_x, double pt_y);

    // C++:  int Subdiv2D::getEdge(int edge, int nextEdgeType)
    private static native int getEdge_0(long nativeObj, int edge, int nextEdgeType);

    // C++:  void Subdiv2D::getEdgeList(vector_Vec4f& edgeList)
    private static native void getEdgeList_0(long nativeObj, long edgeList_mat_nativeObj);

    // C++:  void Subdiv2D::getTriangleList(vector_Vec6f& triangleList)
    private static native void getTriangleList_0(long nativeObj, long triangleList_mat_nativeObj);

    // C++:  Point2f Subdiv2D::getVertex(int vertex, int* firstEdge = 0)
    private static native double[] getVertex_0(long nativeObj, int vertex, double[] firstEdge_out);
    private static native double[] getVertex_1(long nativeObj, int vertex);

    // C++:  void Subdiv2D::getVoronoiFacetList(vector_int idx, vector_vector_Point2f& facetList, vector_Point2f& facetCenters)
    private static native void getVoronoiFacetList_0(long nativeObj, long idx_mat_nativeObj, long facetList_mat_nativeObj, long facetCenters_mat_nativeObj);

    // C++:  void Subdiv2D::initDelaunay(Rect rect)
    private static native void initDelaunay_0(long nativeObj, int rect_x, int rect_y, int rect_width, int rect_height);

    // C++:  int Subdiv2D::insert(Point2f pt)
    private static native int insert_0(long nativeObj, double pt_x, double pt_y);

    // C++:  void Subdiv2D::insert(vector_Point2f ptvec)
    private static native void insert_1(long nativeObj, long ptvec_mat_nativeObj);

    // C++:  int Subdiv2D::locate(Point2f pt, int& edge, int& vertex)
    private static native int locate_0(long nativeObj, double pt_x, double pt_y, double[] edge_out, double[] vertex_out);

    // C++:  int Subdiv2D::nextEdge(int edge)
    private static native int nextEdge_0(long nativeObj, int edge);

    // C++:  int Subdiv2D::rotateEdge(int edge, int rotate)
    private static native int rotateEdge_0(long nativeObj, int edge, int rotate);

    // C++:  int Subdiv2D::symEdge(int edge)
    private static native int symEdge_0(long nativeObj, int edge);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
