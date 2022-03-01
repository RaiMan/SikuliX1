/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;
import java.text.MessageFormat;
import java.util.*;

public class SikuliIDEI18N {
   static ResourceBundle i18nRB = null;
   static ResourceBundle i18nRB_en = null;
   static Locale curLocale = null;

   static {
      Locale locale_en = new Locale("en","US");
      i18nRB_en = ResourceBundle.getBundle("i18n/IDE",locale_en);
      curLocale = Commons.getLocale();
      if(!setLocale(curLocale)){
         curLocale = locale_en;
         Commons.setLocale(curLocale);
      }
   }

   public static boolean setLocale(Locale locale){
      try{
         i18nRB = ResourceBundle.getBundle("i18n/IDE",locale);
      }
      catch(MissingResourceException e){
         Debug.error("SikuliIDEI18N: no locale for " + locale);
         return false;
      }
      return true;
   }

   public static Locale getLocale() {
      return curLocale;
//     String ret = curLocale.toString();
//     if (i18nRB == null) ret += " (using en_US)";
//     return ret;
   }

   public static String _I(String key, Object... args){
      String ret;
      if(i18nRB==null)
         ret = i18nRB_en.getString(key);
      else{
         try {
            ret = i18nRB.getString(key);
         } catch (MissingResourceException e) {
            ret = i18nRB_en.getString(key);
         }
      }
      if(args.length>0){
         MessageFormat formatter = new MessageFormat("");
         formatter.setLocale(curLocale);
         formatter.applyPattern(ret);
         ret = formatter.format(args);
      }
      return ret;
   }

}
