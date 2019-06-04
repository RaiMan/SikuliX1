/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
#include "sikuli-debug.h"

int sikuli::OCR_DEBUG_LEVEL = 0;
int sikuli::FINDER_DEBUG_LEVEL = 0;
sikuli::onullstream null_out;

void sikuli::setDebug(DebugCategories cat, int level){
   using namespace sikuli;
   switch(cat){
      case OCR:
         OCR_DEBUG_LEVEL = level;
         return;
      case FINDER:
         FINDER_DEBUG_LEVEL = level;
         return;
   }
}

std::ostream& sikuli::dout(const char* name){
#ifdef SHOW_DEBUG_MESSAGE
   return cerr << "[" << name << "] ";
#else
   return null_out;
#endif
}

std::ostream& sikuli::dhead(const char* name){
#ifdef SHOW_DEBUG_MESSAGE
   return cerr << "[" << name << "]\n----------------------------\n";
#else
   return null_out;
#endif
}
