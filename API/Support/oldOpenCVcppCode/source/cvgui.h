/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
#ifndef _CVGUI_H_
#define _CVGUI_H_

#include "opencv.hpp"
#include <iostream>
#include "tessocr.h"

using namespace cv;

//TODO: Visual Logging ??
#ifdef ENABLE_VISUAL_LOG
   const bool enable_visual_log = true;
#else
   const bool enable_visual_log = false;
#endif

class VisualLogger{

private:

   static int image_i;
   static int step_i;
   static char* prefix;

public:

   static void newImage(){
      if (!enable_visual_log)
         return;

      image_i++;
      step_i = 0;
   }

   static void next(){
      if (!enable_visual_log)
         return;

      image_i++;
      step_i = 0;
   }

   static void setEnabled(bool enabled_){
      //enabled = enabled_;
   }

   static void log(const char* name, const Mat& image){
      //TODO: std::cout << "vlog " << enable_visual_log << " " << name << std::endl;
      if (!enable_visual_log)
         return;

      char buf[200];

      if (prefix){

         sprintf(buf, "%s-%02d-%s.vlog.png", prefix, step_i, name);

      }else{
         sprintf(buf, "%03d-%02d-%s.vlog.png",image_i,step_i,name);
      }

      imwrite(buf, image);

      step_i++;
   }
};

#define VLOG(x,y) VisualLogger::log(x,y)
#define VNEW()    VisualLogger::newImage()

class Blob : public Rect{

public:

   Blob(){};

   Blob(const Rect& rect){
      x = rect.x;
      y = rect.y;
      height = rect.height;
      width = rect.width;
      area = 0;
   }

   bool isContainedBy(Blob& b){
      return x >= b.x && y >= b.y && (x+width) <= (b.x+b.width) && (y+height) <= (b.y+b.height);
   }

   double area;
   int mb;
   int mg;
   int mr;
   int score;
};

class LineBlob : public Blob{

public:

   LineBlob() {};

   void add(Blob& blob);
   void merge(LineBlob& blob);

   void calculateBoundingRectangle();
   vector<Blob> blobs;

protected:

   void updateBoundingRect(Blob& blob);

};

class ParagraphBlob : public LineBlob {

public:
   void add(LineBlob& lineblob);

   vector<LineBlob>::const_iterator begin() const { return lineblobs.begin();};
   vector<LineBlob>::const_iterator end() const { return lineblobs.end();};

   vector<LineBlob> lineblobs;
};

class Color{
public:
   static Scalar RED;
   static Scalar WHITE;

   static Scalar RANDOM();

};

class Painter {

public:

   static void drawRect(Mat& image, OCRRect r, Scalar color);
   static void drawRect(Mat& image, Rect r, Scalar color);
   static void drawRects(Mat& image, vector<Rect>& rects, Scalar color);
   static void drawRects(Mat& image, vector<Rect>& rects);

   static void drawBlobs(Mat& image, vector<Blob>& blobs);
   static void drawBlobs(Mat& image, vector<Blob>& blobs, Scalar color);

   static void drawBlobsRandomShading(Mat& image, vector<Blob>& blobs);

   static void drawLineBlobs(Mat& image, vector<LineBlob>& lineblobs, Scalar color);
   static void drawParagraphBlobs(Mat& image, vector<ParagraphBlob> blobs, Scalar color);

   static void drawOCRWord(Mat& image, OCRWord ocrword);
   static void drawOCRLine(Mat& image, OCRLine ocrline);
   static void drawOCRParagraph(Mat& image, OCRParagraph ocrparagraph);
   static void drawOCRText(Mat& image, OCRText ocrtext);

};

class Util{

public:

   static void growRect(Rect& rect, int xd, int yd, Rect bounds);
   static void growRect(Rect& rect, int xd, int yd, cv::Mat image);
   static void rgb2grayC3(const Mat& input, Mat& output);

};

class cvgui {

public:

   static void segmentScreenshot(const Mat& screen, vector<Blob>& text_blobs, vector<Blob>& image_blobs);
   static void getLineBlobsAsIndividualWords(const Mat& screen, vector<LineBlob>& lineblobs);
   static void getParagraphBlobs(const Mat& screen, vector<ParagraphBlob>& parablobs);

   static void findBoxes(const Mat& screen, vector<Blob>& output_blobs);
   static Mat findPokerBoxes(const Mat& screen, vector<Blob>& output_blobs);

   static Mat findBoxesByVoting(const Mat& screen, int box_width, int box_height, vector<Blob>& output_blobs);

private:

   static void computeUnitBlobs(const Mat& input, Mat& output);

   static Mat removeGrayBackground(const Mat& input);
   static Mat obtainGrayBackgroundMask(const Mat& input);

   static void findLongLines(const Mat& binary, Mat& dest, int min_length = 100, int extension = 0);
   static void findLongLines_Horizontal(const Mat& binary, Mat& dest, int min_length = 100, int extension = 0);
   static void findLongLines_Vertical(const Mat& binary, Mat& dest, int min_length = 100, int extension = 0);

   static void filterLineSegments(const Mat& src, Mat& dest, int min_length, int max_length);

   static void extractRects(const Mat& src, vector<Rect>& rects);
   static void extractBlobs(const Mat& src, vector<Blob>& blobs);


   static void extractSmallRects(const Mat& src, vector<Rect>& rects);

   static bool hasMoreThanNUniqueColors(const Mat& src, int n);
   static bool areHorizontallyAligned(const vector<Rect>& rects);

   // voting
   static void voteCenter_Horizontal(const Mat& binary, Mat& dest, int min_length, int tolerance, int distance);
   static void voteCenter_Vertical(const Mat& binary, Mat& dest, int min_length, int tolerance, int distance);


   // linking
   static void linkLineBlobsIntoPagagraphBlobs(vector<LineBlob>& blobs, vector<ParagraphBlob>& parablobs);
   static void mergeLineBlobs(vector<LineBlob>& blobs, vector<LineBlob>& merged_blobs);
   static void linkBlobsIntoLineBlobs(vector<Blob>& blobs, vector<LineBlob>& lines, int max_spacing = 8);

   static void run_ocr_on_lineblobs(vector<LineBlob>& ocr_input_lineblobs,
                                    Mat& input_image,
                                    vector<OCRLine>& ocrlines);

   static void calculateColor(vector<Blob>& blobs,
                               const Mat& color_image,
                               const Mat& foreground_mask);

};

#endif // _CVGUI_H_
