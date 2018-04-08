/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

#include "pyramid-template-matcher.h"
#include "vision.h"

#ifdef DEBUG
#define dout std::cerr
#else
#define dout if(0) std::cerr
#endif

#define MIN_PIXELS_TO_USE_GPU 90000
#define WORTH_GPU(mat) ( ((mat).rows * (mat).cols) > MIN_PIXELS_TO_USE_GPU)

void PyramidTemplateMatcher::init() {
   _use_gpu = false;
   _hasMatchedResult = false;
}

PyramidTemplateMatcher::PyramidTemplateMatcher(const MatchingData& data_, int levels, float _factor)
: factor(_factor),  lowerPyramid(NULL)
{
   data = data_;

   if (data.getSource().rows < data.getTarget().rows || data.getSource().cols < data.getTarget().cols)
      return;

   init();
#ifdef ENABLE_GPU
   if(sikuli::Vision::getParameter("GPU") && WORTH_GPU(data.getSource())){
      if(gpu::getCudaEnabledDeviceCount()>0)
         _use_gpu = true;
      //cout << data.getSource().rows << "x" << data.getSource().cols << endl;
   }
#endif
   if (levels > 0)
      lowerPyramid = createSmallMatcher(levels-1);
}


PyramidTemplateMatcher::~PyramidTemplateMatcher(){
   if (lowerPyramid != NULL)
      delete lowerPyramid;
};

PyramidTemplateMatcher* PyramidTemplateMatcher::createSmallMatcher(int level){
      return new PyramidTemplateMatcher(data.createSmallData(factor), level, factor);
}

double PyramidTemplateMatcher::findBest(const MatchingData& data, Rect* roi, Mat& out_result, Point& out_location){
      double out_score;
      Mat source;
      if(roi != NULL)
         source = data.getSource()(*roi);
      else
         source = data.getSource();
      const Mat& target = data.getTarget();

#ifdef ENABLE_GPU
      if(_use_gpu){
         gpu::GpuMat gSource, gTarget;
         gSource.upload(source);
         gTarget.upload(target);
         gpu::matchTemplate(gSource,gTarget,gResult,CV_TM_CCOEFF_NORMED);
         gpu::minMaxLoc(gResult, NULL, &out_score, NULL, &out_location);
         return out_score;
      }
#endif

      if(data.isSameColor()){ // pure color target
         source = data.getOrigSource();
         if(roi != NULL)
            source = source(*roi);
         if(data.isBlack()){ // black target
            Mat inv_source, inv_target;
            bitwise_not(source, inv_source);
            bitwise_not(data.getOrigTarget(), inv_target);
            matchTemplate(inv_source, inv_target, out_result, CV_TM_SQDIFF_NORMED);
         }
         else{
            matchTemplate(source, data.getOrigTarget(), out_result, CV_TM_SQDIFF_NORMED);
         }
         result = Mat::ones(out_result.size(), CV_32F) - result;
      }
      else{
         matchTemplate(source, target, out_result, CV_TM_CCOEFF_NORMED);
      }
      minMaxLoc(result, NULL, &out_score, NULL, &out_location);
      return out_score;
}

void PyramidTemplateMatcher::eraseResult(int x, int y, int xmargin, int ymargin){
   int x0 = max(x-xmargin,0);
   int y0 = max(y-ymargin,0);
#ifdef ENABLE_GPU
   int rows = _use_gpu? gResult.rows : result.rows;
   int cols = _use_gpu? gResult.cols : result.cols;
#else
   int rows = result.rows;
   int cols = result.cols;
#endif
   int x1 = min(x+xmargin,cols);  // no need to blank right and bottom
   int y1 = min(y+ymargin,rows);

#ifdef ENABLE_GPU
   if(_use_gpu)
      gResult(Range(y0, y1), Range(x0, x1)) = 0.f;
   else
#endif
   {
      result(Range(y0, y1), Range(x0, x1)) = 0.f;
   }
}

FindResult PyramidTemplateMatcher::next(){
   if (data.isSourceSmallerThanTarget()){
      return FindResult(0,0,0,0,-1);
   }
   if (lowerPyramid != NULL)
      return nextFromLowerPyramid();

   double detectionScore;
   Point detectionLoc;
   if(!_hasMatchedResult){
      detectionScore = findBest(data, NULL, result, detectionLoc);
      _hasMatchedResult = true;
   }
   else{
#ifdef ENABLE_GPU
      if(_use_gpu)
         gpu::minMaxLoc(gResult, NULL, &detectionScore, NULL, &detectionLoc);
      else
#endif
      {
         minMaxLoc(result, NULL, &detectionScore, NULL, &detectionLoc);
      }

   }

   const Mat& target = data.getTarget();

   int xmargin = target.cols/3;
   int ymargin = target.rows/3;
   eraseResult(detectionLoc.x, detectionLoc.y, xmargin, ymargin);

   return FindResult(detectionLoc.x,detectionLoc.y,target.cols,target.rows,detectionScore);
}

FindResult PyramidTemplateMatcher::nextFromLowerPyramid(){
   FindResult match = lowerPyramid->next();

   int x = match.x*factor;
   int y = match.y*factor;

   // compute the parameter to define the neighborhood rectangle
   int x0 = max(x-(int)factor,0);
   int y0 = max(y-(int)factor,0);
   int x1 = min(x+data.target.cols+(int)factor,data.source.cols);
   int y1 = min(y+data.target.rows+(int)factor,data.source.rows);
   Rect roi(x0,y0,x1-x0,y1-y0);

   Point detectionLoc;
   double detectionScore = findBest(data, &roi, result, detectionLoc);

   detectionLoc.x += roi.x;
   detectionLoc.y += roi.y;

   return FindResult(detectionLoc.x,detectionLoc.y,data.target.cols,data.target.rows,detectionScore);
}
