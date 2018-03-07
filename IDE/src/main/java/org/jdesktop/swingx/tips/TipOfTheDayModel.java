/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.tips;

import org.jdesktop.swingx.JXTipOfTheDay;

/**
 * A model for {@link org.jdesktop.swingx.JXTipOfTheDay}.<br>
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public interface TipOfTheDayModel {

  /**
   * @return the number of tips in this model
   */
  int getTipCount();

  /**
   * @param index
   * @return the tip at <code>index</code>
   * @throws IndexOutOfBoundsException
   *           if the index is out of range (index &lt; 0 || index &gt;=
   *           getTipCount()).
   */
  Tip getTipAt(int index);

  /**
   * A tip.<br>
   */
  interface Tip {

    /**
     * @return very short (optional) text describing the tip
     */
    String getTipName();

    /**
     * The tip object to show. See {@link JXTipOfTheDay} for supported object
     * types.
     *
     * @return the tip to display
     */
    Object getTip();
  }

}
