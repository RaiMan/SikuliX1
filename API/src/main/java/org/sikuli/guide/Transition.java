/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

public interface Transition {

   public interface TransitionListener {
      void transitionOccurred(Object source);
   }

   String waitForTransition(TransitionListener token);

}
