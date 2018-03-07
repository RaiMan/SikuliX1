/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

public class SxAnchor extends Visual {
  Region region;
  ArrayList<AnchorListener> listeners = new ArrayList<AnchorListener>();
  private boolean animateAnchoring = false;
  Pattern pattern = null;
  Tracker tracker = null;

  public SxAnchor() {
    super();
    setForeground(Color.black);
  }

  public SxAnchor(Pattern pattern) {
    super();
    this.pattern = pattern;
    setTracker(pattern);
  }

  public SxAnchor(Region region) {
    super();
    if (region != null) {
      this.region = region;
      setActualBounds(region.getRect());
    }
    setForeground(Color.black);
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setAnimateAnchoring(boolean animateAnchoring) {
    this.animateAnchoring = animateAnchoring;
  }

  public boolean isAnimateAnchoring() {
    return animateAnchoring;
  }

  public interface AnchorListener {
    public void anchored();
    public void found(SxAnchor source);

//   public class AnchorAdapter implements AnchorListener {
//      public void anchored(){};
//      public void found(){};
//   }
   }

  public void addListener(AnchorListener listener) {
    listeners.add(listener);
  }

  public void found(Rectangle bounds) {
    for (AnchorListener listener : listeners) {
      listener.found(this);
    }
    if (isAnimateAnchoring()) {
      Point center = new Point();
      center.x = (int) (bounds.x + bounds.width / 2);
      center.y = (int) (bounds.y + bounds.height / 2);
      moveTo(center, new AnimationListener() {
        public void animationCompleted() {
          anchored();
        }
      });
    } else {
      setActualLocation(bounds.x, bounds.y);
      anchored();
    }
  }

  public void anchored() {
    for (AnchorListener listener : listeners) {
      listener.anchored();
    }
    // this implements the default behavior for fadein entrance when
    // the anchor pattern is found and anchored for the first time
    addFadeinAnimation();
    startAnimation();
  }

  public void setTracker(Pattern pattern) {
    setOpacity(0f);
    tracker = new Tracker(pattern);
    BufferedImage img;
    try {
      img = pattern.getBImage();
      setActualSize(img.getWidth(), img.getHeight());
      tracker.setAnchor(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void startTracking() {
    if (tracker != null) {
      //Debug.info("[SxAnchor] start tracking");
      tracker.start();
    }
  }

  public void stopTracking() {
    if (tracker != null) {
      tracker.stopTracking();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    //<editor-fold defaultstate="collapsed" desc="TODO not used currently">
    /*   if (editable) {
     * if (true) {
     * Rectangle r = getActualBounds();
     * g2d.setColor(getForeground());
     * g2d.drawRect(0, 0, r.width - 1, r.height - 1);
     * g2d.setColor(Color.white);
     * g2d.drawRect(1, 1, r.width - 3, r.height - 3);
     * g2d.setColor(getForeground());
     * g2d.drawRect(2, 2, r.width - 5, r.height - 5);
     * g2d.setColor(Color.white);
     * g2d.drawRect(3, 3, r.width - 7, r.height - 7);
     * } else {
     * Rectangle r = getActualBounds();
     * g2d.setColor(Color.red);
     * g2d.setStroke(new BasicStroke(3f));
     * g2d.drawRect(1, 1, r.width - 3, r.height - 3);
     * }
     * }*/
    //</editor-fold>
  }
}
