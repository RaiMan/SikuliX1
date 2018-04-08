%module VisionProxy
%{
#include "vision.h"
#include "sikuli-debug.h"
#include <iostream>
#include "opencv.hpp"
//#include "cvgui.h" not used
#include "tessocr.h"
%}

%include "std_vector.i"
%include "std_string.i"
%include "typemaps.i"
%include "various.i"

%template(FindResults) std::vector<FindResult>;

%template(OCRChars) std::vector<OCRChar>;
%template(OCRWords) std::vector<OCRWord>;
%template(OCRLines) std::vector<OCRLine>;
%template(OCRParagraphs) std::vector<OCRParagraph>;

%typemap(jni) unsigned char*        "jbyteArray"
%typemap(jtype) unsigned char*      "byte[]"
%typemap(jstype) unsigned char*     "byte[]"

// Map input argument: java byte[] -> C++ unsigned char *
%typemap(in) unsigned char* {
   long len = JCALL1(GetArrayLength, jenv, $input);
   $1 = (unsigned char *)malloc(len + 1);
   if ($1 == 0) {
      std::cerr << "out of memory\n";
      return 0;
   }
   JCALL4(GetByteArrayRegion, jenv, $input, 0, len, (jbyte *)$1);
}

%typemap(freearg) unsigned char* %{
   free($1);
%}

// change Java wrapper output mapping for unsigned char*
%typemap(javaout) unsigned char* {
    return $jnicall;
 }

%typemap(javain) unsigned char* "$javainput"


struct FindResult {
   int x, y;
   int w, h;
   double score;
   FindResult(){
      x=0;y=0;w=0;h=0;score=-1;text = "";
   }
   FindResult(int _x, int _y, int _w, int _h, double _score){
      x = _x; y = _y;
      w = _w; h = _h;
      score = _score;
      text = "";
   }

   std::string text;
};

class OCRRect {

public:

   OCRRect();
   OCRRect(int x_, int y_, int width_, int height_);

   int x;
   int y;
   int height;
   int width;

};

class OCRChar : public OCRRect{

public:

   OCRChar(const std::string& ch_, int x_, int y_, int width_, int height_)
   : ch(ch_), OCRRect(x_,y_,width_,height_){};

   std::string ch;
};

class OCRWord : public OCRRect {

public:
   float score;
   std::string getString();

   std::vector<OCRChar> getChars();
};

class OCRLine : public OCRRect{
public:

   std::string getString();
   std::vector<OCRWord> getWords();

};

class OCRParagraph : public OCRRect{
public:

   std::vector<OCRLine> getLines();

};

class OCRText : public OCRRect{

public:

   std::string getString();

   std::vector<OCRWord> getWords();
   std::vector<OCRParagraph> getParagraphs();

};

/* not used
//class Blob : public cv::Rect{
class Blob {

public:

   Blob(){};
   Blob(const cv::Rect& rect);

   bool isContainedBy(Blob& b);

   double area;
   int mb;
   int mg;
   int mr;
   int score;
};
*/

%include "enumtypeunsafe.swg"
%javaconst(1);
enum TARGET_TYPE{
   MAT,
   IMAGE,
   TEXT,
   BUTTON
};

namespace sikuli {

   class FindInput{

   public:

      FindInput();
      FindInput(cv::Mat source, cv::Mat target);
      FindInput(cv::Mat source, int target_type, const char* target);

      FindInput(const char* source_filename, int target_type, const char* target);

      FindInput(cv::Mat source, int target_type);
      FindInput(const char* source_filename, int target_type);

      // copy everything in 'other' except for the source image
      FindInput(cv::Mat source, const FindInput other);

      void setSource(const char* source_filename);
      void setTarget(int target_type, const char* target_string);

      void setSource(cv::Mat source);
      void setTarget(cv::Mat target);

      cv::Mat getSourceMat();
      cv::Mat getTargetMat();

      void setFindAll(bool all);
      bool isFindingAll();

      void setLimit(int limit);
      int getLimit();

      void setSimilarity(double similarity);
      double getSimilarity();

      int getTargetType();

      std::string getTargetText();
   };

   class Vision{
   public:

      static std::vector<FindResult> find(FindInput q);
      static std::vector<FindResult> findChanges(FindInput q);

      // not used currently
      //static std::string query(const char* index_filename, cv::Mat image);

      static double compare(cv::Mat m1, cv::Mat m2);

      static void initOCR(const char* ocrDataPath);

      static OCRText recognize_as_ocrtext(cv::Mat image);

      // not used currently
      //static std::vector<FindResult> findBlobs(const cv::Mat& image, bool textOnly=false);
      //static std::vector<FindResult> findTextBlobs(const cv::Mat& image);

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


   enum DebugCategories {
      OCR, FINDER
   };
   void setDebug(DebugCategories cat, int level);



}

class OCR {
public:
   static void setParameter(std::string param, std::string value);
};


namespace cv{
   class Mat {
     int _w, _h;
     unsigned char* _data;

   public:
     //Mat(int _rows, int _cols, int _type, unsigned char* _data);
   };

}


