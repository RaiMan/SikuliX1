/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

public class HotkeyEvent {
   public int keyCode;
   public int modifiers;

   public HotkeyEvent(int code_, int mod_){
      init(code_, mod_);
   }

   void init(int code_, int mod_){
      keyCode = code_;
      modifiers = mod_;
   }
}
