/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.RunTime;
import java.util.*;
import jxgrabkey.HotkeyConflictException;
import jxgrabkey.JXGrabKey;
import org.sikuli.basics.Debug;
import org.sikuli.script.RunTime;

public class LinuxHotkeyManager extends HotkeyManager {
   static{
         RunTime.loadLibrary("JXGrabKey");
   }

   class HotkeyData {
      int key, modifiers;
      HotkeyListener listener;

      public HotkeyData(int key_, int mod_, HotkeyListener l_){
         key = key_;
         modifiers = mod_;
         listener = l_;
      }
   };

   class MyHotkeyHandler implements jxgrabkey.HotkeyListener{
      public void onHotkey(int id){
         Debug.log(4, "Hotkey pressed");
         HotkeyData data = _idCallbackMap.get(id);
         HotkeyEvent e = new HotkeyEvent(data.key, data.modifiers);
         data.listener.invokeHotkeyPressed(e);
      }
   };

   private Map<Integer, HotkeyData> _idCallbackMap = new HashMap<Integer,HotkeyData >();
   private int _gHotkeyId = 1;

   public boolean _addHotkey(int keyCode, int modifiers, HotkeyListener listener){
      JXGrabKey grabKey = JXGrabKey.getInstance();

      if(_gHotkeyId == 1){
         grabKey.addHotkeyListener(new MyHotkeyHandler());
      }

      _removeHotkey(keyCode, modifiers);
      int id = _gHotkeyId++;
      HotkeyData data = new HotkeyData(keyCode, modifiers, listener);
      _idCallbackMap.put(id, data);

      try{
         //JXGrabKey.setDebugOutput(true);
         grabKey.registerAwtHotkey(id, modifiers, keyCode);
      }catch(HotkeyConflictException e){
         Debug.error("Hot key conflicts");
         return false;
      }
      return true;
   }

   public boolean _removeHotkey(int keyCode, int modifiers){
      for( Map.Entry<Integer, HotkeyData> entry : _idCallbackMap.entrySet() ){
         HotkeyData data = entry.getValue();
         if(data.key == keyCode && data.modifiers == modifiers){
            JXGrabKey grabKey = JXGrabKey.getInstance();
            int id = entry.getKey();
            grabKey.unregisterHotKey(id);
            _idCallbackMap.remove(id);
            return true;
         }
      }
      return false;
   }

   public void cleanUp(){
      JXGrabKey grabKey = JXGrabKey.getInstance();
      for( Map.Entry<Integer, HotkeyData> entry : _idCallbackMap.entrySet() ){
         int id = entry.getKey();
         grabKey.unregisterHotKey(id);
      }
      _gHotkeyId = 1;
      _idCallbackMap.clear();
      grabKey.getInstance().cleanUp();
   }

}
