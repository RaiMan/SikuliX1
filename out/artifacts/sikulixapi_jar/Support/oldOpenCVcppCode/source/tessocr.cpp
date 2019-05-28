/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <stdlib.h>
#include <locale.h>
#include "tessocr.h"
#include "sikuli-debug.h"

using namespace std;
using namespace sikuli;
using namespace tesseract;

TessBaseAPI OCR::_tessAPI;

#define COMPUTE_IMAGE_XDIM(xsize,bpp) ((bpp)>8 ? ((xsize)*(bpp)+7)/8 :((xsize)+8/(bpp)-1)/(8/(bpp)))
char* OCR::getBoxText(const unsigned char* imagedata,
                         int width, int height, int bpp){

   int bytes_per_pixel = bpp / 8;
   int bytes_per_line = COMPUTE_IMAGE_XDIM(width,bpp);
   _tessAPI.SetImage(imagedata, width, height, bytes_per_pixel, bytes_per_line);
   _tessAPI.Recognize(0);
   char *boxtext = _tessAPI.GetBoxText(0);

   /*
   char* text = TessBaseAPI::TesseractRectBoxes(imagedata,
                                                bytes_per_pixel,
                                                bytes_per_line, 0, 0,
                                                width,
                                                height,
                                                height);
                                                */
   return boxtext;
}

char* OCR::getText(const unsigned char* imagedata,
                         int width, int height, int bpp){

   int bytes_per_pixel = bpp / 8;
   int bytes_per_line = COMPUTE_IMAGE_XDIM(width,bpp);
   _tessAPI.SetImage(imagedata, width, height, bytes_per_pixel, bytes_per_line);
   _tessAPI.Recognize(0);
   char *text = _tessAPI.GetUTF8Text();
   return text;
}

OCRRect::OCRRect(int x_, int y_, int width_, int height_)
: x(x_), y(y_), width(width_), height(height_){};

OCRRect::OCRRect(){
   x = -1;
   y = -1;
   width = -1;
   height = -1;
}

void
OCRRect::addOCRRect(const OCRRect& rect){
   if (width < 0 && height < 0){
      x = rect.x;
      y = rect.y;
      height = rect.height;
      width = rect.width;
   }else{
      int left = x < rect.x ? x : rect.x;
      int top = y < rect.y ? y : rect.y;
      int lhs = x + width;
      int rhs = rect.x + rect.width;
      int right = lhs > rhs ? lhs : rhs;
      lhs = y + height;
      rhs = rect.y + rect.height;
      int bottom = lhs > rhs ? lhs : rhs;
      x = left; y = top; width = right - left; height = bottom - top;
   }
}

void
OCRWord::add(const OCRChar& ocr_char){
   addOCRRect(ocr_char);
   ocr_chars_.push_back(ocr_char);
}

string
OCRWord::str(){
   string ret = "";
   for (vector<OCRChar>::iterator it = ocr_chars_.begin(); it != ocr_chars_.end(); ++it){
      ret = ret + it->ch;
   }
   return ret;
}

vector<OCRChar>
OCRWord::getChars(){
   return ocr_chars_;
}

string
OCRWord::getString(){
   return str();
}

void
OCRWord::clear() {
   width = -1; height = -1;
   ocr_chars_.clear();
};

bool
OCRWord::isValidWord(){
   return OCR::_tessAPI.IsValidWord(str().c_str());
}

void
OCRLine::addWord(OCRWord& ocr_word){
   addOCRRect(ocr_word);
   ocr_words_.push_back(ocr_word);
}

vector<OCRWord>
OCRLine::getWords(){
   return ocr_words_;
}

string
OCRLine::getString(){
   if (ocr_words_.empty())
      return string("");

   string ret;
   ret = ocr_words_.front().getString();
   for (vector<OCRWord>::iterator it = ocr_words_.begin()+1;
        it != ocr_words_.end(); ++it){
      OCRWord& word = *it;
      ret = ret + " " + word.getString();
   }
   return ret;
}

void
OCRParagraph::addLine(OCRLine& ocr_line){
   addOCRRect(ocr_line);
   ocr_lines_.push_back(ocr_line);
}

vector<OCRLine>
OCRParagraph::getLines(){
   return ocr_lines_;
}

//void
//OCRText::add(OCRWord& ocr_word){
//   ocr_words_.push_back(ocr_word);
//}
//
//void
//OCRText::addLine(OCRLine& ocr_line){
//   ocr_lines_.push_back(ocr_line);
//}

void
OCRText::save(const char* filename){
// TODO: reimplement

//
//   ofstream of(filename);
//
//   for (iterator it = begin();
//        it != end(); ++it){
//
//      of << it->str() << " ";
//   }
//
//   of.close();
}

void
OCRText::save_with_location(const char* filename){

   vector<OCRWord> words = getWords();

   ofstream of(filename);

   for (vector<OCRWord>::iterator it = words.begin();
        it != words.end(); ++it){

      OCRWord& w = *it;

      of << w.x << " " << w.y << " " << w.width << " " << w.height << " ";
      of << w.getString() << " ";
      of << endl;
   }

   of.close();
}

void
OCRText::addParagraph(OCRParagraph& ocr_paragraph){
   addOCRRect(ocr_paragraph);
   ocr_paragraphs_.push_back(ocr_paragraph);
}

vector<string>
OCRText::getLineStrings(){
   vector<string> line_strings;

   for (vector<OCRParagraph>::iterator it = ocr_paragraphs_.begin();
        it != ocr_paragraphs_.end(); ++it){

      OCRParagraph& para = *it;

      for (vector<OCRLine>::iterator it1 = para.getLines().begin();
           it1 != para.getLines().end(); ++it1){

         OCRLine& line = *it1;

         string line_string = line.getString();

         line_strings.push_back(line_string);

      }
   }

   return line_strings;
}

vector<OCRWord>
OCRText::getWords(){
   vector<OCRWord> ret_words;

   for (vector<OCRParagraph>::iterator it = ocr_paragraphs_.begin();
        it != ocr_paragraphs_.end(); ++it){

      vector<OCRLine> lines = it->getLines();

      for (vector<OCRLine>::iterator it1 = lines.begin();
           it1 != lines.end(); ++it1){

         vector<OCRWord> words = it1->getWords();

         for (vector<OCRWord>::iterator it2 = words.begin();
              it2 != words.end(); ++it2){

            OCRWord word = *it2;
            ret_words.push_back(word);
         }
      }
   }

   return ret_words;
}

vector<OCRParagraph>
OCRText::getParagraphs(){
   return ocr_paragraphs_;
}

vector<string>
OCRText::getWordStrings(){
   vector<string> word_strings;

   for (vector<OCRParagraph>::iterator it = ocr_paragraphs_.begin();
        it != ocr_paragraphs_.end(); ++it){

      vector<OCRLine> lines = it->getLines();

      for (vector<OCRLine>::iterator it1 = lines.begin();
           it1 != lines.end(); ++it1){

         vector<OCRWord> words = it1->getWords();

         for (vector<OCRWord>::iterator it2 = words.begin();
              it2 != words.end(); ++it2){

            OCRWord& word = *it2;
            word_strings.push_back(word.getString());
         }

         // add new line
         word_strings.push_back("\n");
      }
   }

   return word_strings;
}

string
OCRText::getString(){
   vector<string> word_strings;
   word_strings = getWordStrings();

   if (word_strings.empty())
      return "";

   string ret = word_strings.front();

   for (vector<string>::iterator it = word_strings.begin() + 1;
        it != word_strings.end(); ++it){

      ret = ret + *it + " ";
   }

   return ret;
}

char
encode(char ch){
   char code;
   if (ch >= '0' && ch <= '9')
      code = ch - '0' + 2;
   else if (ch >= 'a' && ch <= 'z')
      code = ch - 'a' + 12;
   else if (ch >= 'A' && ch <= 'Z')
      code = ch - 'A' + 12;
   else
      code = 0;
   return code;
}

// produce a new image 200% the size of the given image
unsigned char* x2(const unsigned char* imagedata,
                  int width, int height, int bpp){

   int bytes_per_pixel = bpp / 8;

   unsigned char* newimage = new unsigned char[width*height*4];

   const unsigned char* p = imagedata;
   unsigned char* q = newimage;

   for (int k=0;k<height;++k){

      const unsigned char* p1 = p;

      for (int i=0;i<2;++i){
         for (int j=0;j<width;++j){
            *q = *p1;
            q++;
            *q = *p1;
            q++;p1++;
         }
      }

      p += width * bytes_per_pixel;
   }

   return newimage;

}

bool OCR::isInitialized = false;
string OCR::_datapath = "tessdata";
string OCR::_lang = "eng";

void OCR::setParameter(std::string param, std::string value){
   bool reinit = false;
   if(param == "datapath"){
      _datapath = value;
      reinit = true;
   }
   else if(param == "lang"){
      _lang = value;
      reinit = true;
   }
   else
      _tessAPI.SetVariable(param.c_str(), value.c_str());
   if(reinit){
      isInitialized = false;
      _tessAPI.End();
   }

}

void
OCR::init(){
   init(_datapath.c_str());
}

void
OCR::init(const char* datapath){
   if (isInitialized)
      return;

	 setlocale (LC_NUMERIC, "C");
   _datapath = datapath;

#ifdef WIN32
   string env_datapath = string("TESSDATA_PREFIX=") + string(datapath);
   putenv(const_cast<char*>(env_datapath.c_str()));
#else
   //putenv on Mac breaks the "open" command somehow.
   //we have to use setenv instead.
   setenv("TESSDATA_PREFIX", datapath, 1);
#endif
   int ret = _tessAPI.Init(datapath, _lang.c_str());
	 //TODO
	 //int ret = _tessAPI.Init(datapath, _lang.c_str(), OEM_TESSERACT_ONLY);
	 //   _tessAPI.SetAccuracyVSpeed(AVS_MOST_ACCURATE); // FIXME: doesn't work?
   isInitialized = true;
}





#include "cvgui.h"
using namespace cv;

#define MAXLEN 80

static int findMin(int d1, int d2, int d3) {
   /*
    * return min of d1, d2 and d3.
    */
   if(d1 < d2 && d1 < d3)
      return d1;
   else if(d1 < d3)
      return d2;
   else if(d2 < d3)
      return d2;
   else
      return d3;
}

static int
findEditDistanceLessThanK(const char *s1, const char *s2,
                                    int k){
   /*
    * returns edit distance between s1 and s2.
    */
   int d1, d2, d3;

   if(*s1 == 0)
      return strlen(s2);
   if(*s2 == 0)
      return strlen(s1);
   if (k == 0)
      return 0;
   if(*s1 == *s2)
      d1 = findEditDistanceLessThanK(s1+1, s2+1, k);
   else
      d1 = 1 + findEditDistanceLessThanK(s1+1, s2+1, k-1);    // update.
   d2 = 1+findEditDistanceLessThanK(s1, s2+1, k-1);                   // insert.
   d3 = 1+findEditDistanceLessThanK(s1+1, s2, k-1);                   // delete.

   return findMin(d1, d2, d3);
}

static int findEditDistance(const char *s1, const char *s2) {
   /*
    * returns edit distance between s1 and s2.
    */
   int d1, d2, d3;

   if(*s1 == 0)
      return strlen(s2);
   if(*s2 == 0)
      return strlen(s1);
   if(*s1 == *s2)
      d1 = findEditDistance(s1+1, s2+1);
   else
      d1 = 1 + findEditDistance(s1+1, s2+1);    // update.
   d2 = 1+findEditDistance(s1, s2+1);                   // insert.
   d3 = 1+findEditDistance(s1+1, s2);                   // delete.

   return findMin(d1, d2, d3);
}

void sharpen(Mat& img){
   Mat blur;
   GaussianBlur(img, blur, cv::Size(0, 0), 5);
   addWeighted(img, 2.5, blur, -1.5, 0, img);
}

float preprocess_for_ocr(const Mat& in_img, Mat& out_img){
   const float MIN_HEIGHT = 30;
   float scale = 1.f;
   if (in_img.rows < MIN_HEIGHT){
      scale = MIN_HEIGHT / float(in_img.rows);
      resize(in_img, out_img, Size(in_img.cols*scale,in_img.rows*scale));
			//TODO
			//resize(in_img, out_img, Size(in_img.cols*scale,in_img.rows*scale), 0, 0, INTER_CUBIC);
			//copyMakeBorder (in_img, out_img, 0, (scale-1)*in_img.rows, 0, (scale-1)*in_img.cols, BORDER_REPLICATE);
   }else {
      out_img = in_img;
   }
   sharpen(out_img);
   //imshow("ocrImage", out_img);
   return scale;
}

string OCR::recognize_as_string(const Mat& blobImage){
   Mat gray, ocrImage;  // the image passed to tesseract
   OCR::init();
   cvtColor(blobImage, gray, CV_RGB2GRAY);
   preprocess_for_ocr(gray, ocrImage);

   //imshow("ocr", ocrImage); waitKey();
   char* text = getText((unsigned char*)ocrImage.data,
                              ocrImage.cols,
                              ocrImage.rows,
                              8);
   if(text){
      string ret = string(text);
      delete [] text;
      return ret;
   }
   return "";
}

vector<OCRWord> getWordsFromImage(const Mat& screen, const Blob& blob){

   Mat blobImage(screen,blob);

   Mat ocrImage;  // the image passed to tesseract
   float scale = preprocess_for_ocr(blobImage, ocrImage);

   vector<OCRWord> ocr_words;
   ocr_words = OCR::recognize_to_words((unsigned char*)ocrImage.data,
                              ocrImage.cols,
                              ocrImage.rows,
                              8);

   for (vector<OCRWord>::iterator iter = ocr_words.begin();
        iter != ocr_words.end(); iter++){
      OCRWord& word = *iter;
      if(scale>1.f){
         // scale back the coordinates in the OCR result
         word.x = word.x/scale;
         word.y = word.y/scale;
         word.width = word.width/scale;
         word.height = word.height/scale;
      }

      word.x += blob.x;
      word.y += blob.y;
   }

   return ocr_words;
}


vector<OCRChar> run_ocr(const Mat& screen, const Blob& blob){

   Mat blobImage(screen,blob);

   Mat ocrImage;  // the image passed to tesseract
   float scale = preprocess_for_ocr(blobImage, ocrImage);

   vector<OCRChar> ocr_chars;
   ocr_chars = OCR::recognize((unsigned char*)ocrImage.data,
                              ocrImage.cols,
                              ocrImage.rows,
                              8);

   for (vector<OCRChar>::iterator iter = ocr_chars.begin();
        iter != ocr_chars.end(); iter++){
      OCRChar& ocrchar = *iter;
      if(scale>1.f){
         // scale back the coordinates in the OCR result
         ocrchar.x = ocrchar.x/scale;
         ocrchar.y = ocrchar.y/scale;
         ocrchar.width = ocrchar.width/scale;
         ocrchar.height = ocrchar.height/scale;
      }

      ocrchar.x += blob.x;
      ocrchar.y += blob.y;

   }

   return ocr_chars;
}

void
find_phrase_helper(const Mat& screen_gray, vector<string> words, vector<LineBlob> lineblobs,
                   LineBlob resultblob, vector<FindResult>& results, bool is_find_one = true){

   string word = words[0];

   vector<string> rest;
   for (vector<string>::iterator it2 = words.begin()+1;
        it2 != words.end(); ++ it2)
      rest.push_back(*it2);

   dhead("find_phrase") << "<" << word << ">" << endl;

   vector<LineBlob> lineblobs_thisround = lineblobs;
   for (int r = 0; r < 3; ++r){

      for (int tolerance = 0; tolerance < 3; ++tolerance){

         vector<LineBlob> lineblobs_nextround;

         for (vector<LineBlob>::iterator it = lineblobs_thisround.begin();
              it != lineblobs_thisround.end(); ++it){

            LineBlob lineblob = *it;

            if (abs((int)lineblob.blobs.size() - (int)word.size()) > tolerance){
               lineblobs_nextround.push_back(lineblob);
               continue;
            }

            dhead("find_phrase") << lineblob.x << "," << lineblob.y << "," << lineblob.width << "," << lineblob.height << endl;

            vector<OCRChar> ocr_chars = run_ocr(screen_gray, lineblob);
            dhead("find_phrase") << word << "<->";

            string ocrword = "";
            for (vector<OCRChar>::iterator iter = ocr_chars.begin();
                 iter != ocr_chars.end(); iter++){

               OCRChar& ocrchar = *iter;
               dout("find_phrase") << ocrchar.ch;

               ocrword = ocrword + ocrchar.ch;
            }

            if (ocr_chars.size() < 1){
               dout("find_phrase") << endl;
               continue;
            }

            int d = findEditDistanceLessThanK(word.c_str(), ocrword.c_str(),3);

            dout("find_phrase") << '[' << d << ']';

            if (d > 2){
               dout("find_phrase") << endl;
               lineblobs_nextround.push_back(lineblob);
               continue;
            }

            if (rest.empty()){
               dout("find_phrase") << " ... match!" << endl;

               //Blob b = resultblob;
               //dout("find_phrase") << b.x << "," << b.y << endl;
               //b = lineblob;
               //dout("find_phrase") << b.x << "," << b.y << endl;

               resultblob.merge(lineblob);

               FindResult result(resultblob.x,resultblob.y,
                                 resultblob.width,resultblob.height, 1.0);

               results.push_back(result);
               return;

            }
            else
               dout("find_phrase") << endl;




            vector<LineBlob> nextblobs;
            for (vector<LineBlob>::iterator it2 = lineblobs.begin();
                 it2 != lineblobs.end(); ++it2){

               LineBlob& b1 = lineblob;
               LineBlob& b2 = *it2;

               bool similar_baseline = abs((b1.y + b1.height) - (b2.y + b2.height)) < 5;
               bool close_right = (b2.x > b1.x) && (b2.x - (b1.x+b1.width)) < 20;
               bool close_below = (b2.y > b1.y) && (b2.y - b1.y) < 20;

               if (close_right && similar_baseline)
                  nextblobs.push_back(b2);

            }


            if (!rest.empty() && !nextblobs.empty()){

               LineBlob next_resultblob = resultblob;
               next_resultblob.merge(lineblob);

               find_phrase_helper(screen_gray, rest, nextblobs, next_resultblob, results, is_find_one);
            }


            dout("find_phrase") << endl;

            // check if we have already found one match
            if (is_find_one && results.size() >= 1)
               // if so, we return the reuslts right away
               return;

         }

         lineblobs_thisround = lineblobs_nextround;

      }


   }
}

int
OCR::findEditDistance(const char *s1, const char *s2,
                      int k){

   return findEditDistanceLessThanK(s1,s2,k);
}

vector<FindResult>
OCR::find_phrase(const Mat& screen, vector<string> words, bool is_find_one){

   vector<LineBlob> lineblobs;
   cvgui::getLineBlobsAsIndividualWords(screen, lineblobs);

   Mat screen_gray;
   cvtColor(screen,screen_gray,CV_RGB2GRAY);

   vector<FindResult> results;

   LineBlob empty;
   find_phrase_helper(screen_gray, words, lineblobs, empty, results, is_find_one);

   return results;
}

vector<FindResult>
OCR::find_word(const Mat& screenshot, string word, bool is_find_one){

   vector<string> words;
   words.push_back(word);

   return find_phrase(screenshot, words, is_find_one);
}

OCRText
OCR::recognize_screenshot(const char* screenshot_filename){

   Mat screenshot = imread(screenshot_filename, 1);
   return recognize(screenshot);
}


OCRLine
linkOCRCharsToOCRLine(const vector<OCRChar>& ocrchars){

   OCRLine ocrline;
   OCRWord ocrword;

   int previous_spacing = 1000;
   int next_spacing = 1000;
   for (vector<OCRChar>::const_iterator it = ocrchars.begin();
        it != ocrchars.end(); it++){

      const OCRChar& ocrchar = *it;

      int current_spacing = 0;
      if (it > ocrchars.begin()){
         const OCRChar& previous_ocrchar = *(it-1);

         current_spacing = ocrchar.x - (previous_ocrchar.x + previous_ocrchar.width);
         //cout << '[' << ocrchar.height << ':' << spacing << ']';
         //cout << '[' << spacing << ']';
      }

      if (it < ocrchars.end() - 1){
         const OCRChar& next_ocrchar = *(it+1);
         next_spacing = next_ocrchar.x - (ocrchar.x + ocrchar.width);

//         if (current_spacing > next_spacing + 1){// || spacing >= 2){
//            ocrline.addWord(ocrword);
//            ocrword.clear();
//            //cout << ' ';
//         }

      }

      if (current_spacing > previous_spacing + 2 ||
          current_spacing > next_spacing + 2){
         ocrline.addWord(ocrword);
         ocrword.clear();
         //cout << ' ';
      }

      previous_spacing = current_spacing;

      ocrword.add(ocrchar);
      //cout << ocrchar.ch;
   }

   if (!ocrword.empty())
      ocrline.addWord(ocrword);

   return ocrline;
}

OCRLine
recognize_line(const cv::Mat& screen_gray, const LineBlob& lineblob){

   Blob b(lineblob);

   vector<OCRWord> words = getWordsFromImage(screen_gray, lineblob);
   OCRLine line;
   for(vector<OCRWord>::iterator it = words.begin(); it != words.end(); ++it)
      line.addWord(*it);
   return line;
}

/*
OCRLine
recognize_line(const cv::Mat& screen_gray, const LineBlob& lineblob){

   Blob b(lineblob);
   //Util::growRect(b, 2, 2, screen_gray);

   vector<OCRChar> ocrchars = run_ocr(screen_gray, b);
   OCRLine ocrline = linkOCRCharsToOCRLine(ocrchars);
   return ocrline;
}
*/

OCRParagraph
recognize_paragraph(const cv::Mat& screen_gray, const ParagraphBlob& parablob){

   OCRParagraph ocrparagraph;

   for (vector<LineBlob>::const_iterator it = parablob.begin();
        it != parablob.end(); ++it){

      const LineBlob& lineblob = *it;
      OCRLine ocrline = recognize_line(screen_gray, lineblob);

      if (!ocrline.getWords().empty())
         ocrparagraph.addLine(ocrline);
   }

   return ocrparagraph;
}

OCRText
OCR::recognize(cv::Mat screen){

   OCRText ocrtext;

   vector<ParagraphBlob> parablobs;
   cvgui::getParagraphBlobs(screen, parablobs);

   Mat screen_gray;
   if(screen.channels()>1)
      cvtColor(screen,screen_gray,CV_RGB2GRAY);
   else
      screen_gray = screen;

   for (vector<ParagraphBlob>::iterator it = parablobs.begin();
        it != parablobs.end(); ++it){

      ParagraphBlob& parablob = *it;

      OCRParagraph ocrpara;
      ocrpara = recognize_paragraph(screen_gray, parablob);
      ocrtext.addParagraph(ocrpara);

   }
   //TODO: VISUAL LOGGING
   //Mat dark = screen * 0.2;
   //Painter::drawOCRText(dark, ocrtext);
   //VLOG("OCR-result", dark);

   return ocrtext;
}


vector<OCRChar>
OCR::recognize(const unsigned char* imagedata,
               int width, int height, int bpp){

   OCR::init();

   vector<OCRChar> ret;

   char* boxtext = getBoxText(imagedata,width,height,bpp);

   //Result ocr_result;

   if (boxtext){

      stringstream str(boxtext);
      string ch;
      int x0,y0,x1,y1, page;
      while (str >> ch >> x0 >> y0 >> x1 >> y1 >> page){
         //cout << ch << " " << x0 << " " << y0 << " " << x1 << " " << y1 << endl;

         //convert back to the screen coordinate (0,0) - (left,top)
         int h = y1 - y0;
         int w = x1 - x0;
         OCRChar ocr_char(ch,x0,height-y1,w,h);

         ret.push_back(ocr_char);
      };

      delete [] boxtext;
   }


   return ret;
}


vector<OCRWord>
OCR::recognize_to_words(const unsigned char* imagedata,
                        int width, int height, int bpp){

   OCR::init();

   vector<OCRWord> ret;
   vector<OCRChar> chars = OCR::recognize(imagedata, width, height, bpp);
   char *text = _tessAPI.GetUTF8Text();
   //cout << "chars: " << chars.size() << endl;
   //cout << "UTF8Text: [" << text << "]\n";
   int *scores = _tessAPI.AllWordConfidences();
   char *p_ch = text;
   OCRWord word;

   for(vector<OCRChar>::iterator it = chars.begin(); it != chars.end(); ){
      int len = it->ch.length();
      if(*p_ch != ' ' && *p_ch != '\n'){
         word.add(*it);
         ++it;
      }
      else{
         if(!word.empty()){
            //cout << "add " << word.str() << endl;
            ret.push_back(word);
            word.clear();
         }
      }
      p_ch += len;
   }
   if(!word.empty())
      ret.push_back(word);
   int i;
   for(i=0;i<ret.size() && scores[i] >= 0;i++){
      ret[i].score = scores[i]/100.f;
      //cout << ret[i].str() << " " << ret[i].score << endl;
   }
   while(scores[i]>=0) i++;
   if(ret.size() != i){
   //   cerr << "WARNING: num of words not consistent!: "
   //        << "#WORDS: " << ret.size() <<  " " << i << endl;
   }
   return ret;
}
