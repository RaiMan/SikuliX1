/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public abstract class Animator implements ActionListener {
   Timer timer;
   Visual comp;
   int duration = 1000;
   int cycle = 50;
   boolean played = false;

   public Animator(Visual comp){
      this.comp = comp;
      timer = new Timer(cycle,this);
   }

   public void start(){
      played = true;
      timer.start();
   }

   public void stop(){
         timer.stop();
   }

   public boolean isRunning(){
      return timer.isRunning();
   }

   public boolean isPlayed(){
      return played;
   }
}

//<editor-fold defaultstate="collapsed" desc="class CircleAnimatoOld">
class CircleAnimatoOld extends Animator{
  int repeatCount;
  int count;

  LinearInterpolation funcr;

  Point origin;
  int radius;

  public CircleAnimatoOld(Visual comp, int radius){
    super(comp);

    repeatCount = duration / cycle;
    count = 0;

    funcr = new LinearInterpolation(0,(float) (2*Math.PI),repeatCount);

    origin = comp.getLocation();
    this.radius = radius;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    float r = funcr.getValue(count);

    int x = (int) (origin.x + (int) radius * Math.sin(r));
    int y=  (int) (origin.y + (int) radius * Math.cos(r));

    Point p = new Point(x,y);

    Rectangle r1 = comp.getBounds();

    comp.setLocation(p);

    // r1 stores the union of the bounds before/after the animated step
    Rectangle r2 = comp.getBounds();
    r1.add(r2);

    comp.getParent().getParent().repaint(r1.x,r1.y,r1.width,r1.height);
    //repaint(r1);
    //      comp.getParent().invalidate();
    //      comp.repaint();

    if (count == repeatCount)
      count = 0;
    else
      count++;

  }
}
//</editor-fold>

class MoveAnimator extends  Animator{
   int repeatCount;
   int count;
   QuarticEase tfuncx;
   QuarticEase tfuncy;
   Point src,dest;

   public MoveAnimator(Visual comp, Point src, Point dest){
      super(comp);
      repeatCount = duration / cycle;
      count = 0;
      tfuncx = new QuarticEase(src.x,dest.x,repeatCount);
      tfuncy = new QuarticEase(src.y,dest.y,repeatCount);
      this.dest = dest;
      this.src = src;
   }

   @Override
   public void actionPerformed(ActionEvent e){
      if ( count <= repeatCount){

         int x = (int) tfuncx.getValue(count);
         int y = (int) tfuncy.getValue(count);

         count++;


         Rectangle r1 = comp.getBounds();

         comp.setActualLocation(x,y);

         Rectangle r2 = comp.getBounds();
         r1.add(r2);

          Container c = comp.getParent();

          if (c != null){
             c.getParent().getParent().repaint(r1.x,r1.y,r1.width,r1.height);
          }

      }else{
         timer.stop();

      }
   }

   @Override
   public void start(){
      comp.setActualLocation(src.x,src.y);
      super.start();
   }

   @Override
   public void stop(){
      //comp.setLocation(dest.x,dest.y);
      super.stop();
   }
}
