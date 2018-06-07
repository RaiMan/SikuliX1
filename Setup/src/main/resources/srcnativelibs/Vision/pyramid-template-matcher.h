/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
#ifndef _PYRAMID_TEMPLATE_MATCHER_
#define _PYRAMID_TEMPLATE_MATCHER_

#include <stdio.h>
#include <iostream>

#include "opencv.hpp"
#include "find-result.h"
#include "vision.h"
#ifdef ENABLE_GPU
#include <opencv2/gpu/gpu.hpp>
#endif

// select how images are downsampled
#define USE_RESIZE 1
#define USE_PYRDOWN 0

using namespace cv;
using namespace std;

struct MatchingData {
   Mat source, target;
   Mat source_gray, target_gray;
   Scalar mean, stddev;
   bool use_gray;

   inline MatchingData(){
   }

   inline MatchingData(const Mat& source_, const Mat& target_) : source(source_), target(target_){
      use_gray = false;
      meanStdDev( target, mean, stddev );
   }

   inline MatchingData createSmallData(float factor){
      Mat new_source, new_target;
      resize(source, target, new_source, new_target, factor);
      MatchingData newData(new_source, new_target);
      if(use_gray)
         newData.useGray(true);
      return newData;
   }

   inline void resize(const Mat& source, const Mat& target, Mat& out_source, Mat& out_target, float factor){
#if USE_PYRDOWN
      // Faster
      pyrDown(source, out_source);
      pyrDown(target, out_target);
#endif
#if USE_RESIZE
      cv::resize(source, out_source, Size(source.cols/factor, source.rows/factor),INTER_NEAREST);
      cv::resize(target, out_target, Size(target.cols/factor, target.rows/factor),INTER_NEAREST);
#endif
   }

   inline bool isSameColor() const{
      return stddev[0]+stddev[1]+stddev[2]+stddev[3] <= 1e-5;
   }

   inline bool isBlack() const{
      return (mean[0]+mean[1]+mean[2]+mean[3] <= 1e-5) && isSameColor();
   }

   inline bool isSourceSmallerThanTarget() const{
      return source.rows < target.rows || source.cols < target.cols;
   }

   inline const Mat& getOrigSource() const{
      return source;
   }

   inline const Mat& getOrigTarget() const{
      return target;
   }

   inline const Mat& getSource() const{
      return use_gray? source_gray : source;
   }

   inline const Mat& getTarget() const{
      return use_gray? target_gray : target;
   }

   inline bool useGray() const{
      return use_gray;
   }

   inline bool useGray(bool flag){
      use_gray = flag;
      if(use_gray){
         cvtColor(source, source_gray, CV_RGB2GRAY);
         cvtColor(target, target_gray, CV_RGB2GRAY);
      }
      return flag;
   }

   inline void setSourceROI(const Rect& roi){
      if(use_gray)
         source_gray.adjustROI(roi.y, roi.y+roi.height, roi.x, roi.x+roi.width);
      else
         source.adjustROI(roi.y, roi.y+roi.height, roi.x, roi.x+roi.width);
   }
};

class PyramidTemplateMatcher{
private:
   bool _use_gpu;
   void init();
public:

   PyramidTemplateMatcher(){
      init();
   }
   PyramidTemplateMatcher(const MatchingData& data, int levels, float factor);
   ~PyramidTemplateMatcher();

   virtual FindResult next();

protected:

   PyramidTemplateMatcher* createSmallMatcher(int level);
   double findBest(const MatchingData& data, Rect* roi, Mat& out_result, Point& out_location);
   void eraseResult(int x, int y, int xmargin, int ymargin);
   FindResult nextFromLowerPyramid();

   MatchingData data;
   float factor;
   bool _hasMatchedResult;
   double _detectedScore;
   Point  _detectedLoc;

   PyramidTemplateMatcher* lowerPyramid;

   Mat result;

#ifdef ENABLE_GPU
   gpu::GpuMat gResult;
#endif
};

#endif
