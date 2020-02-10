/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.Test;
import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.sikuli.script.Pattern;

public class ElementTest {

  @Test
  public void getImage() {
    Pattern pattern = new Pattern();
    pattern.toString();
    Image image = pattern.getImage();
    Image image1 = image.getImage();
    Debug.logp("");
  }
}