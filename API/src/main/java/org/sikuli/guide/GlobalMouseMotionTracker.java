/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import org.sikuli.script.Env;
import org.sikuli.script.Location;

public class GlobalMouseMotionTracker implements ActionListener {

   final static int IDLE_COUNT_THRESHOLD = 200;

   // this keeps track of how many times the cursor stays stationary
   int idle_count;

   Location lastLocation = null;

   static GlobalMouseMotionTracker _instance = null;
   static public GlobalMouseMotionTracker getInstance(){
      if (_instance == null){
         _instance = new GlobalMouseMotionTracker();
      }
      return _instance;
   }

   ArrayList<GlobalMouseMotionListener> listeners
   = new ArrayList<GlobalMouseMotionListener>();

   public void addListener(GlobalMouseMotionListener listener){
      listeners.add(listener);
   }

   Timer timer;
   private GlobalMouseMotionTracker(){
      timer = new Timer(10, this);
   }

   public void start(){
      timer.start();
      //Debug.info("[GlobalMouseMotionTracker] started");
   }

   public void stop(){
      timer.stop();
      //Debug.info("[GlobalMouseMotionTracker] stopped");
   }

   @Override
   public void actionPerformed(ActionEvent arg) {

      Location newLocation = Env.getMouseLocation();
      //Debug.info("Mouse loction: " + newLocation);

      if (lastLocation != null){

         if (lastLocation.x != newLocation.x ||
               lastLocation.y != newLocation.y){

            for (GlobalMouseMotionListener listener : listeners){
               listener.globalMouseMoved(newLocation.x,newLocation.y);
            }

            idle_count = 0;


         }else{
            idle_count++;
         }

         //Debug.info("idle: "  + idle_count);
         if (idle_count > IDLE_COUNT_THRESHOLD){
            for (GlobalMouseMotionListener listener : listeners){
               listener.globalMouseIdled(newLocation.x,newLocation.y);
            }
            idle_count = 0;
         }

      }

      lastLocation = newLocation;
   }
}
