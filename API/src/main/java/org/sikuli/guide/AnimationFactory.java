/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class AnimationFactory {

  static NewAnimator createResizeAnimation(Visual sklComponent, Dimension currentSize, Dimension targetSize) {
    return new ResizeAnimator(sklComponent, currentSize, targetSize);
  }

  static NewAnimator createCenteredResizeToAnimation(Visual sklComponent, Dimension targetSize) {
    return new CenteredResizeToAnimator(sklComponent, targetSize);
  }

  static NewAnimator createCenteredMoveAnimation(Visual sklComponent, Point source, Point destination) {
    NewMoveAnimator anim = new NewMoveAnimator(sklComponent, source, destination);
    anim.centered = true;
    return anim;
  }

  static NewAnimator createMoveAnimation(Visual sklComponent, Point source, Point destination) {
    return new NewMoveAnimator(sklComponent, source, destination);
  }

  static public NewAnimator createCircleAnimation(
          Visual sklComponent, Point origin, float radius) {
    return new CircleAnimator(sklComponent, origin, radius);
  }

  static public NewAnimator createOpacityAnimation(
          Visual sklComponent, float sourceOpacity, float targetOpacity) {
    return new OpacityAnimator(sklComponent, sourceOpacity, targetOpacity);
  }
}

interface AnimationListener {

  void animationCompleted();
}

class LinearStepper {

  float beginVal;
  float endVal;
  int step;
  int steps;

  public LinearStepper(float beginVal, float endVal, int steps) {
    this.step = 0;
    this.steps = steps;
    this.beginVal = beginVal;
    this.endVal = endVal;
  }

  public float next() {
    float ret = beginVal + step * (endVal - beginVal) / steps;
    step += 1;
    return ret;
  }

  public boolean hasNext() {
    return step <= steps;
  }
}

class NewAnimator implements ActionListener {

  Timer timer;
  boolean looping = false;
  boolean animationRunning = false;
  Visual sklComponent;

  NewAnimator(Visual sklComponent) {
    this.sklComponent = sklComponent;
  }

  protected void init() {
  }

  public void start() {
    init();
    timer = new Timer(25, this);
    timer.start();
    animationRunning = true;
  }

  protected boolean isRunning() {
    return animationRunning;
  }

  public void setLooping(boolean looping) {
    this.looping = looping;
  }
  AnimationListener listener;

  public void setListener(AnimationListener listener) {
    this.listener = listener;
  }

  protected void animate() {
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (isRunning()) {

      Rectangle r = sklComponent.getBounds();

      //setActualLocation((int) x, (int) y);

      animate();

      r.add(sklComponent.getBounds());

      if (sklComponent.getTopLevelAncestor() != null) {
        sklComponent.getTopLevelAncestor().repaint(r.x, r.y, r.width, r.height);
      }

    } else {
      timer.stop();
      if (looping) {
        start();
      } else {
        animationRunning = false;
        if (listener != null) {
          listener.animationCompleted();
        }
      }
    }
  }
}

class NewMoveAnimator extends NewAnimator {

  LinearStepper xStepper;
  LinearStepper yStepper;
  Point source;
  Point destination;

  NewMoveAnimator(Visual sklComponent, Point source, Point destination) {
    super(sklComponent);
    this.source = source;
    this.destination = destination;
  }
  boolean centered = false;

  @Override
  protected void init() {
    xStepper = new LinearStepper(source.x, destination.x, 10);
    yStepper = new LinearStepper(source.y, destination.y, 10);
  }

  @Override
  protected boolean isRunning() {
    return xStepper.hasNext();
  }

  @Override
  protected void animate() {
    float x = xStepper.next();
    float y = yStepper.next();

    if (centered) {
      x -= sklComponent.getActualWidth() / 2;
      y -= sklComponent.getActualHeight() / 2;
    }
    sklComponent.setActualLocation((int) x, (int) y);
  }
}

class CircleAnimator extends NewAnimator {

  LinearStepper radiusStepper;
  Point origin;
  float radius;

  CircleAnimator(Visual sklComponent, Point origin, float radius) {
    super(sklComponent);
    this.radius = radius;
    this.origin = origin;
    setLooping(true);
  }

  @Override
  protected void init() {
    radiusStepper = new LinearStepper(0, (float) (2 * Math.PI), 20);
  }

  @Override
  protected boolean isRunning() {
    return radiusStepper.hasNext();
  }

  @Override
  protected void animate() {
    float theta = radiusStepper.next();

    int x = (int) (origin.x + (int) radius * Math.sin(theta));
    int y = (int) (origin.y + (int) radius * Math.cos(theta));

    sklComponent.setActualLocation((int) x, (int) y);
  }
}

class ResizeAnimator extends NewAnimator {

  LinearStepper widthStepper;
  LinearStepper heightStepper;
  Dimension currentSize;
  Dimension targetSize;

  ResizeAnimator(Visual sklComponent, Dimension currentSize, Dimension targetSize) {
    super(sklComponent);
    this.currentSize = currentSize;
    this.targetSize = targetSize;
  }

  @Override
  protected void init() {
    widthStepper = new LinearStepper(currentSize.width, targetSize.width, 10);
    heightStepper = new LinearStepper(currentSize.height, targetSize.height, 10);
  }

  @Override
  protected boolean isRunning() {
    return widthStepper.hasNext();
  }

  @Override
  protected void animate() {
    float width = widthStepper.next();
    float height = heightStepper.next();
    sklComponent.setActualSize(new Dimension((int) width, (int) height));
  }
}

class CenteredResizeToAnimator extends NewAnimator {

  LinearStepper widthStepper;
  LinearStepper heightStepper;
  Dimension currentSize;
  Dimension targetSize;
  Point centerLocation;

  CenteredResizeToAnimator(Visual sklComponent, Dimension targetSize) {
    super(sklComponent);
    this.targetSize = targetSize;
  }

  @Override
  protected void init() {
    centerLocation = sklComponent.getCenter();
    widthStepper = new LinearStepper(sklComponent.getActualWidth(), targetSize.width, 10);
    heightStepper = new LinearStepper(sklComponent.getActualHeight(), targetSize.height, 10);
  }

  @Override
  protected boolean isRunning() {
    return widthStepper.hasNext();
  }

  @Override
  protected void animate() {
    float width = widthStepper.next();
    float height = heightStepper.next();

    Point newLocation = new Point(centerLocation);
    newLocation.x -= width / 2;
    newLocation.y -= height / 2;
    sklComponent.setActualSize(new Dimension((int) width, (int) height));
    sklComponent.setActualLocation(newLocation);
  }
}

class OpacityAnimator extends NewAnimator {

  LinearStepper stepper;
  float sourceOpacity;
  float targetOpacity;

  OpacityAnimator(Visual sklComponent, float sourceOpacity, float targetOpacity) {
    super(sklComponent);
    this.sourceOpacity = sourceOpacity;
    this.targetOpacity = targetOpacity;
  }

  @Override
  protected void init() {
    stepper = new LinearStepper(sourceOpacity, targetOpacity, 10);
  }

  @Override
  protected boolean isRunning() {
    return stepper.hasNext();
  }

  @Override
  public void animate() {
    float f = stepper.next();
    sklComponent.setOpacity(f);
  }
}

class PopupAnimator implements ActionListener {

  LinearStepper shadowSizeStepper;
  LinearStepper offsetStepper;
  LinearStepper scaleStepper;
  LinearStepper widthStepper;
  LinearStepper heightStepper;
  Timer timer;
  Point centerLocation;

  PopupAnimator() {
    //         shadowSizeStepper = new LinearStepper(5,13,10);
    //         offsetStepper = new LinearStepper(0,10,5);
    //         //scaleStepper = new LinearStepper(1f,1.2f,);
    //         widthStepper = new LinearStepper(getActualWidth(),1.0f*getActualWidth()*1.1f,10);
    //         heightStepper = new LinearStepper(getActualHeight(),1.0f*getActualHeight()*1.1f,10);
    //
    //         centerLocation = new Point(getActualLocation());
    //         centerLocation.x = centerLocation.x + getActualWidth()/2;
    //         centerLocation.y = centerLocation.y + getActualHeight()/2;
  }

  public void start() {
    //         Timer timer = new Timer(25, this);
    //         timer.start();
    //         animationRunning = true;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //         if (shadowSizeStepper.hasNext()){
    //
    //            float shadowSize = shadowSizeStepper.next();
    //            float offset = offsetStepper.next();
    //            float width = widthStepper.next();
    //            float height = heightStepper.next();
    //
    //            Rectangle r = getBounds();
    //
    //            setActualLocation((int)(centerLocation.x - width/2), (int)(centerLocation.y - height/2));
    //            setActualSize((int)width, (int)height);
    //
    //            setShadow((int)shadowSize,(int) 2);
    //            //Point p = getActualLocation();
    //            //p.x -= 1;
    //            //p.y -= 1;
    //            //setActualLocation(p);
    //            r.add(getBounds());
    //
    //            getParent().getParent().repaint();//r.x,r.y,r.width,r.height);
    //         }else{
    //            ((Timer)e.getSource()).stop();
    //            animationRunning = false;
    //            animationCompleted();
    //         }
  }
}
