/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.image.*;
import java.util.Date;

public class LoadingSpinner {
   protected GifDecoder _gif;
   protected long _start_t = 0;
   protected int _curFrame_i = 0;
   protected BufferedImage _curFrame = null;

   public LoadingSpinner(){
      _gif = new GifDecoder();
      _gif.read(getClass().getResourceAsStream("/icons/loading.gif"));
      _curFrame = _gif.getFrame(0);
   }

   public BufferedImage getFrame(){
      int delay = _gif.getDelay(_curFrame_i);
      long now = (new Date()).getTime();
      if(now - _start_t >= delay){
         _start_t = now;
         _curFrame_i = (_curFrame_i+1) % _gif.getFrameCount();
         _curFrame = _gif.getFrame(_curFrame_i);
      }
      return _curFrame;
   }
}
