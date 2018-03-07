/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.sikuli.script.Region;
import org.sikuli.script.Image;

public class SxImage extends Visual {

  private BufferedImage image = null;
  float scale;
  int w, h;

  public SxImage(String filename) {
    super();
    init(Image.create(filename).get());
  }

  public SxImage(BufferedImage image) {
    super();
    init(image);
  }

  private void init(BufferedImage image) {
    this.image = image;
    setScale(1.0f);
  }

  @Override
  public void updateComponent() {
    setActualBounds(getTarget().getRect());
  }

  @Override
  public Visual setScale(float scale) {
    this.scale = scale;
    if (scale == 0) {
      int x = (int) (getTarget().getCenter().x - image.getWidth()/2);
      int y = (int) (getTarget().getCenter().y - image.getHeight()/2);
      setTarget(Region.create(x, y, image.getWidth(), image.getHeight()));
    } else {
      w = (int) (scale * image.getWidth());
      h = (int) (scale * image.getHeight());
    }
    return this;
  }

  @Override
  public void paintComponent(Graphics g) {
    if (image == null) {
      return;
    }
    Graphics2D g2d = (Graphics2D) g;
    int aw = w > getActualWidth() ? getActualWidth() : w;
    int ah = h> getActualHeight() ? getActualHeight() : h;
    int ay = (int) ((getActualHeight() - ah)/2);
    g2d.drawImage(image, 0, ay, aw, ah, null);
    g2d.drawRect(0, 0, getActualWidth() - 1, getActualHeight() - 1);
  }

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public BufferedImage getImage() {
    return image;
  }
}
