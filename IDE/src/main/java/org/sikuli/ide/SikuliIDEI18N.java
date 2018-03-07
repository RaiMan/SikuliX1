/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import java.text.MessageFormat;
import java.util.*;
import org.sikuli.basics.Debug;

public class SikuliIDEI18N {
   static ResourceBundle i18nRB = null;
   static ResourceBundle i18nRB_en = null;
   static Locale curLocale = null;

   static {
      Locale locale_en = new Locale("en","US");
      i18nRB_en = ResourceBundle.getBundle("i18n/IDE",locale_en);
      Locale locale = PreferencesUser.getInstance().getLocale();
      curLocale = locale;
      if(!setLocale(locale)){
         locale = locale_en;
         PreferencesUser.getInstance().setLocale(locale);
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

   public static String getLocaleShow() {
     String ret = curLocale.toString();
     if (i18nRB == null) ret += " (using en_US)";
     return ret;
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
