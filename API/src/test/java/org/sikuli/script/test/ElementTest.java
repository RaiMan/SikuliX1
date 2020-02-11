/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.test;

import org.junit.Test;
import org.sikuli.script.Image;
import org.sikuli.script.Location;
import org.sikuli.script.Region;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ElementTest {
  @Test
  public void testImageRegion() {
    Region region = new Region(100, 200, 300, 400);
    Image image = new Image(region);
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }

  @Test
  public void testImageLocation() {
    Location location = new Location(100, 200);
    Image image = new Image(location);
    assertFalse("Valid???: " + image.toString(), image.isValid());
  }

  @Test
  public void testImageScreenImage() {
    Region region = new Region(100, 200, 300, 400);
    Image image = region.getImage();
    assertTrue("NotValid: " + image.toString(), image.isValid());
  }
}
