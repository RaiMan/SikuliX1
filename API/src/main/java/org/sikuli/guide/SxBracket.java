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
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.JLabel;

import org.sikuli.script.Region;

public class SxBracket extends Visual{
   public final static int DIRECTION_EAST = 1;
   public final static int DIRECTION_WEST = 2;
   public final static int DIRECTION_SOUTH = 3;
   public final static int DIRECTION_NORTH = 4;
   int direction;

   public int PADDING_X = 2;
   public int PADDING_Y = 2;
   public int SHADOW_SIZE = 2;

   int thickness = 10;
   int margin = 5;

   JLabel label;
   int length;

   boolean entrance = false;

   public SxBracket(){
      super();
      init();
   }

   private void init(){
      this.length = 30;
      colorFront = Color.RED;
      stroke = 3;
   }

   @Override
   public void updateComponent() {
      setLocationRelativeToRegion(getTarget(), layout);
   }

   @Override
   public Visual setLocationRelativeToRegion(Region region, Layout side) {
      if (side == Layout.TOP){
         setActualSize(region.w,thickness);
         setDirection(DIRECTION_SOUTH);
      } else if (side == Layout.BOTTOM){
         setActualSize(region.w,thickness);
         setDirection(DIRECTION_NORTH);
      } else if (side == Layout.LEFT){
         setActualSize(thickness,region.h);
         setDirection(DIRECTION_EAST);
      } else if (side == Layout.RIGHT){
         setActualSize(thickness,region.h);
         setDirection(DIRECTION_WEST);
      }

      if (side == Layout.LEFT || side == Layout.RIGHT){
         length = region.h;
      }else{
         length = region.w;
      }
      return super.setLocationRelativeToRegion(region,side);
   }

   @Override
   public void startAnimation(){
      // TODO: move this somewhere else
      // this should only be called first time animation
      // is started
      // Why called here? Because ...
      // we want the location to be decided
         if (direction == DIRECTION_EAST){
            setEntranceAnimation(createSlidingAnimator(-20,0));
         } else if (direction == DIRECTION_WEST){
            setEntranceAnimation(createSlidingAnimator(20,0));
         } else if (direction == DIRECTION_SOUTH){
            setEntranceAnimation(createSlidingAnimator(0,-20));
         } else if (direction == DIRECTION_NORTH){
            setEntranceAnimation(createSlidingAnimator(0,20));
         }
      super.startAnimation();
   }

   public void setDirection(int direction){
      this.direction = direction;
   }

   @Override
   public void paintComponent(Graphics g){
      Graphics2D g2d = (Graphics2D) g;
      Stroke pen = new BasicStroke(stroke);
      g2d.setStroke(pen);
      g2d.setColor(colorFront);
      GeneralPath polyline = new GeneralPath();
      polyline.moveTo(0,0);
      polyline.lineTo(5,5);
      polyline.lineTo(5,length/2-6);
      polyline.lineTo(8,length/2);
      polyline.lineTo(5,length/2+6);
      polyline.lineTo(5,length-5);
      polyline.lineTo(0,length);
      AffineTransform rat = new AffineTransform();
      if (direction == DIRECTION_EAST){
         rat.translate(thickness,length);
         rat.rotate(Math.PI);
      } else if (direction == DIRECTION_SOUTH){
         rat.translate(0,thickness);
         rat.rotate(-Math.PI/2);
      } else if (direction == DIRECTION_NORTH){
         rat.translate(length,0);
         rat.rotate(Math.PI/2);
      }
      g2d.transform(rat);
      g2d.draw(polyline);
      super.paintComponent(g);
   }
}
