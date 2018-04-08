/*
 * Copyright 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
#ifndef _SIKULU_DEBUG_H
#define _SIKULI_DEBUG_H

#include <streambuf>
#include <ostream>

namespace sikuli{
   enum DebugCategories {
      OCR, FINDER
   };
   void setDebug(DebugCategories cat, int level);

   extern int OCR_DEBUG_LEVEL;
   extern int FINDER_DEBUG_LEVEL;

   template <class cT, class traits = std::char_traits<cT> >
     class basic_nullbuf: public std::basic_streambuf<cT, traits> {
        typename traits::int_type overflow(typename traits::int_type c)
        {
           return traits::not_eof(c); // indicate success
        }
     };

   template <class cT, class traits = std::char_traits<cT> >
     class basic_onullstream: public std::basic_ostream<cT, traits> {
     public:
        basic_onullstream():
           std::basic_ios<cT, traits>(&m_sbuf),
           std::basic_ostream<cT, traits>(&m_sbuf)
        {
           this->init(&m_sbuf);
        }

     private:
        basic_nullbuf<cT, traits> m_sbuf;
     };

   typedef basic_onullstream<char> onullstream;
   typedef basic_onullstream<wchar_t> wonullstream;

   std::ostream& dout(const char* name="");
   std::ostream& dhead(const char* name);

}

#endif
