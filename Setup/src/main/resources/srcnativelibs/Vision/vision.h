/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 *  modified RaiMan 2013
 */

#ifndef _VISION_H_
#define _VISION_H_

#include "find-result.h"
#include "opencv.hpp"
#include "tessocr.h"
//#include "cvgui.h" not used

using namespace cv;

namespace sikuli {

enum TARGET_TYPE{
   TARGET_TYPE_MAT,
   TARGET_TYPE_IMAGE,
   TARGET_TYPE_TEXT,
   TARGET_TYPE_BUTTON
};

class FindInput{

public:

   FindInput();
   FindInput(Mat source, Mat target);
   FindInput(Mat source, int target_type, const char* target);

   FindInput(const char* source_filename, int target_type, const char* target);

   FindInput(Mat source, int target_type);
   FindInput(const char* source_filename, int target_type);

   // copy everything in 'other' except for the source image
   FindInput(Mat source, const FindInput other);

   void setSource(const char* source_filename);
   void setTarget(int target_type, const char* target_string);

   void setSource(Mat source);
   void setTarget(Mat target);

   Mat getSourceMat();
   Mat getTargetMat();

   void setFindAll(bool all);
   bool isFindingAll();

   void setLimit(int limit);
   int getLimit();

   void setSimilarity(double similarity);
   double getSimilarity();

   int getTargetType();

   std::string getTargetText();

private:

   void init();

   Mat source;
   Mat target;
   std::string target_text;

   int limit;
   double similarity;
   int target_type;

   int ordering;
   int position;

   bool bFindingAll;
};

class Vision{
   static std::map<std::string, float> _params;
   static std::map<std::string, std::string> _sparams;
	 static void initParameters();
	 static void initSParameters();

public:

   static std::vector<FindResult> find(FindInput q);

   static std::vector<FindResult> findChanges(FindInput q);

   static double compare(cv::Mat m1, cv::Mat m2);

   static void initOCR(const char* ocrDataPath);

   // not used currently
   static string query(const char* index_filename, cv::Mat image);

   static OCRText recognize_as_ocrtext(cv::Mat image);

   // not used currently
   static std::vector<FindResult> findBlobs(const cv::Mat& image, bool textOnly=false);
   // not used currently
   static std::vector<FindResult> findTextBlobs(const cv::Mat& image);

   static std::string recognize(cv::Mat image);
   static std::string recognizeWord(cv::Mat image);

   //helper functions
   static cv::Mat createMat(int _rows, int _cols, unsigned char* _data);

   static void setParameter(std::string param, float val);
   static float getParameter(std::string param);
   static void setSParameter(std::string param, std::string val);
   static std::string getSParameter(std::string param);

private:

};

}

#endif // _VISION_H_
