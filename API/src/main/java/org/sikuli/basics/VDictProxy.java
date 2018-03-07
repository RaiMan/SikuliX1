/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.RunTime;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VDictProxy<T> {
   private long _instance;
   private Map<Integer, T> _i2obj = new HashMap<Integer, T>();

   static {
         RunTime.loadLibrary("VDictProxy");
   }

   public VDictProxy(){
      _instance = getInstance();
   }

   private native long getInstance();

   private String getAbsolutePath(String filename) throws FileNotFoundException{
      if(new File(filename).exists())
         return filename;
      filename = Settings.BundlePath + File.separator + filename;
      if(new File(filename).exists())
         return filename;
      throw new FileNotFoundException("No such file: " + filename);
   }

   // insert an (key,value) entry using an image key
   public void insert(String imagekey_filename, T value) throws FileNotFoundException {
      imagekey_filename = getAbsolutePath(imagekey_filename);
      int hash = value.hashCode();
      while(true){
         if( hash != -1 && !_i2obj.containsKey(hash) ){
            _i2obj.put(hash, value);
            break;
         }
         else{
            hash += (int)(Math.random()*100);
         }
      }
      _insert(_instance, imagekey_filename, hash);
   }

   public native void _insert(long instance, String imagekey_filename, int value);

   // lookup the entry using an image key (exact match)
   public T lookup(String imagekey_filename) throws FileNotFoundException{
      imagekey_filename = getAbsolutePath(imagekey_filename);
      int hash = _lookup(_instance, imagekey_filename);
      if(hash==-1) return null;
      return _i2obj.get(hash);
   }

   private native int _lookup(long instance, String imagekey_filename);

   // lookup the first entry with a similar image key
   public T lookup_similar(String imagekey_filename, double similarity_threshold) throws FileNotFoundException{
      imagekey_filename = getAbsolutePath(imagekey_filename);
      int hash = _lookup_similar(_instance, imagekey_filename, similarity_threshold);
      if(hash==-1) return null;
      return _i2obj.get(hash);
   }

   private native int _lookup_similar(long instance, String imagekey_filename, double similarity_threshold);

   // lookup at most n entries with keys similar to the given image (n = 0 : all)
   public List<T> lookup_similar_n(String imagekey_filename, double similarity_threshold, int n) throws FileNotFoundException{
      imagekey_filename = getAbsolutePath(imagekey_filename);
      int h[] = _lookup_similar_n(_instance, imagekey_filename, similarity_threshold, n);
      List<T> ret = new Vector<T>(h.length);
      for(int i=0;i<h.length;i++){
         if(h[i] == -1)
            ret.add(i, null);
         else
            ret.add(i, _i2obj.get(h[i]));
      }
      return ret;
   }

   private native int[] _lookup_similar_n(long instance, String imagekey_filename, double similarity_threshold, int n);

   // erase the entry associated with the image
   public void erase(String imagekey_filename) throws FileNotFoundException{
      imagekey_filename = getAbsolutePath(imagekey_filename);
      int h = _lookup(_instance, imagekey_filename);
      if(h!=-1)   _i2obj.remove(h);
      _erase(_instance, imagekey_filename);
   }

   private native void _erase(long _instance, String imagekey_filename);

   public int size(){   return _size(_instance); }
   private native int _size(long instance);  // return the number of image keys stored

   public boolean empty(){ return _empty(_instance);  }
   private native boolean _empty(long instance); // test whether it is empty
}
