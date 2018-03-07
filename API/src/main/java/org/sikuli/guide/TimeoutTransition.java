/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;


public class TimeoutTransition implements Transition, ActionListener {

   Timer timer;
   TransitionListener listener;
   public TimeoutTransition(int timeout){
       timer = new Timer(timeout,this);
   }

   public String waitForTransition(final TransitionListener listener){
      this.listener = listener;
      timer.start();
      return "Next";
   }

   @Override
   public void actionPerformed(ActionEvent arg0) {
      listener.transitionOccurred(this);
   }

}
