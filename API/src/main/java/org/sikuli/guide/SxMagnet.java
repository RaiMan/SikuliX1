/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.sikuli.guide.SxAnchor.AnchorListener;
import org.sikuli.guide.Visual.Layout;
import org.sikuli.basics.Debug;
import org.sikuli.script.Env;
import org.sikuli.script.Location;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

public class SxMagnet
        implements Transition, GlobalMouseMotionListener {

  Guide guide;
  GlobalMouseMotionTracker mouseTracker;
  private SxClickable lastClickedClickable;

  public SxMagnet(Guide guide) {
    this.guide = guide;

    mouseTracker = GlobalMouseMotionTracker.getInstance();
    mouseTracker.addListener(this);

    // TOOD: fix this hack
    // use this trick to engage clickablewindow
    guide.addComponent(new SxClickable(), 0);

  }
  ArrayList<SxAnchor> targets = new ArrayList<SxAnchor>();
  ArrayList<Link> links = new ArrayList<Link>();
//   void flyTarget(SxAnchor a){
//
//      Location mouseLocation = Env.getMouseLocation();
//
//      try {
//         Pattern pattern = a.getPattern();
//         SxImage img = new SxImage(pattern.getBImage());
//         img.setActualLocation(a.getActualLocation());
//
//         Dimension currentSize = a.getActualSize();
//         Dimension targetSize = new Dimension(currentSize);
//         targetSize.width *= 1.5;
//         targetSize.height *= 1.5;
//
//         img.addResizeAnimation(currentSize, targetSize);
//
//
//         Point currentLocation = new Point(a.getActualLocation());
//         currentLocation.x += img.getActualWidth();
//         currentLocation.y += img.getActualHeight();
//
//         int dx = mouseLocation.x - currentLocation.x;
//         int dy = mouseLocation.y - currentLocation.y;
//
//         int radius = 50;
//         double distance = mouseLocation.distance(currentLocation);
//         double m = radius / distance;
//
//         Point targetLocation = new Point();
//         targetLocation.x = (int) (mouseLocation.x - dx*m) - img.getActualWidth()/2;
//         targetLocation.y = (int) (mouseLocation.y - dy*m) - img.getActualHeight()/2;
//
//
//         Rectangle desiredSpot = new Rectangle(targetLocation, targetSize);
//         desiredSpot = getFreeSpot(desiredSpot);
//         targetLocation = desiredSpot.getLocation();
//
//
//         img.addMoveAnimation(currentLocation,  targetLocation);
//         guide.addToFront(img);
//         img.startAnimation();
//
//
//         Region r = new Region(mouseLocation.x-radius,mouseLocation.y-radius,radius*2,radius*2);
//         SxCircle c = new SxCircle(r);
//         guide.addComponent(c,1);
//
//         guide.repaint();
//
//
//      } catch (IOException e) {
//         e.printStackTrace();
//      }

  class Link {
    SxImage image;
    SxAnchor anchor;
  }

  void attractTarget(SxAnchor a, Point targetLocation) {

    try {
      Pattern pattern = a.getPattern();
      SxImage img = new SxImage(pattern.getBImage());

      SxClickable clickable = new SxClickable();
      clickable.setLocationRelativeToComponent(img, Layout.OVER);
      guide.addToFront(clickable);

      clickable.clickPoint = a.getCenter();

      Link link = new Link();
      link.image = img;
      link.anchor = a;
      links.add(link);

      img.setShadowDefault();
      img.setActualLocation(a.getActualLocation());

      Dimension currentSize = a.getActualSize();
      Dimension targetSize = new Dimension(currentSize);
      targetSize.width *= 1.5;
      targetSize.height *= 1.5;

      img.addResizeAnimation(currentSize, targetSize);

      Point currentLocation = new Point(a.getActualLocation());

      targetLocation.x -= targetSize.width / 2;
      targetLocation.y -= targetSize.height / 2;

      img.addMoveAnimation(currentLocation, targetLocation);
      guide.addToFront(img);
      img.startAnimation();

      guide.repaint();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  SxCircle selection;

  public void allTargetAnchored() {

    double theta = 0;
    double dtheta = 2.0f * Math.PI / (double) targets.size();

    Location mouseLocation = Env.getMouseLocation();
    int x = mouseLocation.x;
    int y = mouseLocation.y;
    int radius = 50;

    Region r = new Region(mouseLocation.x - radius, mouseLocation.y - radius, radius * 2, radius * 2);
    SxCircle c = new SxCircle(r);
    guide.addToFront(c);

    selection = new SxCircle();
    guide.addToFront(selection);

    // sort targets along x-axis
    Collections.sort(targets, new Comparator<SxAnchor>() {
      @Override
      public int compare(SxAnchor a, SxAnchor b) {
        return b.getX() - a.getX();
      }
    });

    for (SxAnchor target : targets) {

      int px = (int) (x + radius * Math.cos(theta));
      int py = (int) (y + radius * Math.sin(theta));
      theta += dtheta;

      attractTarget(target, new Point(px, py));
    }

  }
  int anchoredCount = 0;

  public void addTarget(final Pattern pattern) {

    final SxAnchor a = new SxAnchor(pattern);
    guide.addToFront(a);

    targets.add(a);

    SxFlag f = new SxFlag("Flag");
    f.setLocationRelativeToComponent(a, Layout.LEFT);
    guide.addToFront(f);

    a.addListener(new AnchorListener() {
      @Override
      public void anchored() {
        Debug.info("[Magnet] pattern anchored");

        anchoredCount += 1;

        if (anchoredCount == targets.size()) {
          allTargetAnchored();
        }
      }

      @Override
      public void found(SxAnchor source) {
        // TODO Auto-generated method stub
      }
    });

  }
//   ArrayList<Rectangle> occupiedSpots = new ArrayList<Rectangle>();
//   Rectangle getFreeSpot(Rectangle desired){
//
//      Rectangle freeSpot = new Rectangle(desired);
//
//      for (Rectangle occupiedSpot : occupiedSpots){
//
//         if (freeSpot.intersects(occupiedSpot)){
//            freeSpot.x = occupiedSpot.x + occupiedSpot.width + 10;
//         }
//
//      }
//
//      occupiedSpots.add(freeSpot);
//
//      return freeSpot;
//   }
  TransitionListener token;

  @Override
  public String waitForTransition(TransitionListener token) {
    this.token = token;
    mouseTracker.start();
    return "Next";
  }

  @Override
  public void globalMouseMoved(int x, int y) {
//      Debug.log("[SxMagnet] moved to " + x + "," + y);

    Point p = new Point(x, y);
    for (Link link : links) {

      if (link.image.getActualBounds().contains(p)) {
        //Debug.info("[SxMagnet] mouseover on a target");

        if (selection != null) {
          selection.setMargin(5, 5, 5, 5);
          selection.setLocationRelativeToComponent(link.anchor, Layout.OVER);
          guide.repaint();
        }
      }
    }

  }

  @Override
  public void globalMouseIdled(int x, int y) {
  }
}
