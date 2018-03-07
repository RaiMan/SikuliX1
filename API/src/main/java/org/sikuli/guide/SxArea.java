/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

public class SxArea extends Visual
implements ComponentListener{

   ArrayList<Region> regions = new ArrayList<Region>();

   ArrayList<Visual> landmarks = new ArrayList<Visual>();

   public SxArea(){
      super();
      // default to transparent so it can be faded in when it becomes visible later
      setOpacity(0);
   }

   public static final int BOUNDING = 0;
   public static final int INTERSECTION = 1;

   int relationship = BOUNDING;
   public void setRelationship(int relationship){
      this.relationship = relationship;
   }

   int mode = 0;
   public static int VERTICAL = 1;
   public static int HORIZONTAL = 2;
   public void setMode(int mode){
      this.mode = mode;
   }

   // update the bounds to the union of all the rectangles
   void updateBounds(){

      Rectangle rect = null;
      Screen s = new Screen();

      for (Visual comp : landmarks){

         if (rect == null){
            rect = new Rectangle(comp.getBounds());
            continue;
         }else {

            if (relationship == BOUNDING){
               rect.add(comp.getBounds());
            }else if (relationship == INTERSECTION){
               rect = rect.intersection(comp.getBounds());
            }

         }
      }

      if (rect.width<0 || rect.height<=0){
         setVisible(false);
      }else{
         setVisible(true);

//         for (Visual sklComp : getFollowers())
         // hack to get the locations of the followers to update

         if (mode == 0){
            setActualLocation(rect.x,rect.y);
            setActualSize(rect.getSize());
         } else if (mode == VERTICAL){
            setActualLocation(rect.x,0);
            setActualSize(rect.width, s.h);
         } else if (mode == HORIZONTAL){
            setActualLocation(0, rect.y);
            setActualSize(s.w, rect.height);
         }
      }

      updateVisibility();
   }

   public void addLandmark(Visual comp){
      landmarks.add(comp);
      updateBounds();
      comp.addComponentListener(this);
   }

   public void addRegion(Region region){

      if (regions.isEmpty()){

         setActualBounds(region.getRect());

      }else{

         Rectangle bounds = getBounds();
         bounds.add(region.getRect());
         setActualBounds(bounds);

      }

      regions.add(region);
   }

   public void paintComponent(Graphics g){
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D)g;

      if (false){
      Rectangle r = getActualBounds();
      g2d.setColor(Color.black);
      g2d.drawRect(0,0,r.width-1,r.height-1);
      //g2d.setColor(Color.white);
      g2d.setColor(Color.green);
      g2d.setStroke(new BasicStroke(3f));
      g2d.drawRect(1,1,r.width-3,r.height-3);
      }
   }

   private void updateVisibility(){
      boolean allHidden = true;
      for (Visual landmark : landmarks){
         allHidden = allHidden && !landmark.isVisible();
      }

      if (allHidden){
         //Debug.info("SxArea is hidden");
      }
      setVisible(!allHidden);

      // if area is visible, do fadein
      if (isVisible()){
         addFadeinAnimation();
         startAnimation();
      }
   }

   @Override
   public void componentHidden(ComponentEvent e) {
      updateVisibility();
   }

   @Override
   public void componentMoved(ComponentEvent e) {
      Rectangle r = getBounds();
      updateBounds();
      r.add(getBounds());
      if (getTopLevelAncestor() != null)
         getTopLevelAncestor().repaint(r.x,r.y,r.width,r.height);
   }

   @Override
   public void componentResized(ComponentEvent e) {
   }

   @Override
   public void componentShown(ComponentEvent e) {
      setVisible(true);
   }

}
