/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

#ifndef _MYOCR_H_
#define _MYOCR_H_

#include <vector>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <stdlib.h>

#include "opencv.hpp"
#include "find-result.h"

#ifdef WIN32
   #include "baseapi.h"
#else
   #include "tesseract/baseapi.h"
#endif

using namespace std;

class OCRRect {

public:

   OCRRect();

   OCRRect(int x_, int y_, int width_, int height_);

   int x;
   int y;
   int height;
   int width;

protected:

   void addOCRRect(const OCRRect& r);

};

class OCRChar : public OCRRect{

public:

   OCRChar() : ch(0), OCRRect(0,0,0,0){};
   OCRChar(const string& ch_, int x_, int y_, int width_, int height_)
   : ch(ch_), OCRRect(x_,y_,width_,height_){};

   string ch;
};

class OCRWord : public OCRRect {

public:
   float score;
   void add(const OCRChar& ocr_char);

   string str();

    void clear();

    bool empty() { return ocr_chars_.empty();};

   bool isValidWord();

   string getString();

   vector<OCRChar> getChars();

private:
   vector<OCRChar> ocr_chars_;
};

class OCRLine : public OCRRect{
public:

   void addWord(OCRWord& word);

   string getString();
   vector<OCRWord> getWords();

private:

   vector<OCRWord> ocr_words_;
};

class OCRParagraph : public OCRRect{
public:

   void addLine(OCRLine& line);
   vector<OCRLine> getLines();

private:

   vector<OCRLine> ocr_lines_;

};

class OCRText : public OCRRect{

public:
   void addParagraph(OCRParagraph& ocr_paragraph);

   typedef vector<OCRWord>::iterator iterator;

   void save(const char* filename);
   void save_with_location(const char* filename);

   vector<string> getLineStrings();
   vector<string> getWordStrings();

   string getString();

   vector<OCRWord> getWords();
   vector<OCRParagraph> getParagraphs();

private:

   vector<OCRParagraph> ocr_paragraphs_;

};

class OCR {
private:
   static std::string _datapath, _lang;

public:
   static void setParameter(std::string param, std::string value);

   static tesseract::TessBaseAPI _tessAPI;
   static char* getBoxText(const unsigned char* imagedata, int width, int height, int bpp);
   static char* getText(const unsigned char* imagedata, int width, int height, int bpp);
   static vector<OCRWord> recognize_to_words(const unsigned char* imagedata,
                                             int width, int height, int bpp);

   static vector<OCRChar> recognize(const unsigned char* imagedata,
                                    int width, int height, int bpp);

   static OCRText recognize(cv::Mat mat);
   static string recognize_as_string(const cv::Mat& mat);

   static vector<FindResult> find_word(const cv::Mat& mat, string word, bool is_find_one = true);

   static vector<FindResult> find_phrase(const cv::Mat& mat, vector<string> words, bool is_find_one = true);

   static OCRText recognize_screenshot(const char* screenshot_filename);

   static void init();
   static void init(const char* datapath);

   static int findEditDistance(const char *s1, const char *s2,
                                 int max_distance=100);

private:

   static bool isInitialized;

};

#endif // _MYOCR_H_
