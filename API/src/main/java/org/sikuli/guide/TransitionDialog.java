/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.sikuli.util.OverlayTransparentWindow;

public class TransitionDialog extends OverlayTransparentWindow implements Transition{

   // the location user moved to by dragging
   static Point userPreferredLocation = null;

   Box buttons;
   JLabel titleBar;
   Button defaultButton;
   TextPane textPane;

   String command = null;
   boolean isLocationSet = false;

   protected void dismiss(){
      setVisible(false);
      dispose();

      synchronized(this){
         this.notify();
      }
   }

   public void setTitle(String title){
      titleBar.setText(title);
      titleBar.setVisible(true);
   }

   public String getActionCommand(){
      return command;
   }

   public TransitionDialog(){
      init("");
   }

   public TransitionDialog(String text){
      init(text);
   }

   public void setLocationToUserPreferredLocation(){
      if (userPreferredLocation != null)
         setLocation(userPreferredLocation);
      else
         setLocationRelativeTo(null);
   }

   public void setTimeout(int timeout){
      Timer timer = new Timer(timeout, new ActionListener(){

         @Override
         public void actionPerformed(ActionEvent arg0) {
            command = null;
            dismiss();
         }

      });
      timer.setRepeats(false);
      timer.start();
   }

   void init(String text){

      setBackground(Color.yellow);
      setForeground(Color.black);

      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      add(content);

      textPane = new TextPane();
      textPane.setText(text);
      textPane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

      Color darkyellow = new Color(238,185,57);

      titleBar = new JLabel();
      titleBar.setFont(new Font("sansserif", Font.BOLD, 14));
      titleBar.setBackground(darkyellow);
      titleBar.setOpaque(true);
      titleBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 5));
      titleBar.setSize(titleBar.getPreferredSize());
      titleBar.setVisible(false);

      buttons = new Box(BoxLayout.X_AXIS);
      defaultButton = new Button("Close");
      buttons.add(defaultButton);
      buttons.setBorder(BorderFactory.createEmptyBorder(15,5,5,5));

      content.add(titleBar);
      content.add(textPane);
      content.add(buttons);

      // this allows the title bar to take the whole width of the dialog box
      titleBar.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
      buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
      textPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));

      // these allow all the parts to left aligned
      titleBar.setAlignmentX(Component.LEFT_ALIGNMENT);
      textPane.setAlignmentX(Component.LEFT_ALIGNMENT);
      buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

      // these are meant to prevent the message box from stealing
      // focus when it's clicked, but they don't seem to work
//      setFocusableWindowState(false);
//      setFocusable(false);

      // this allows the window to be dragged to another location on the screen
      ComponentMover cm = new ComponentMover();
      cm.registerComponent(this);

      pack();
   }

   public String waitForTransition(TransitionListener token){

      // must be set visible
      setVisible(true);

      // before being brought to the front
      toFront();
      setAlwaysOnTop(true);

      Point startLocation = getLocation();
      //Debug.info("init location:" + getLocation());

      // pack needs to be called after the component is set visible
      pack();

      // these do not seem necessary any more
      // force the dialog to paint right away before animation starts
      //repaint();

      synchronized(this){
         try {
            this.wait();
         } catch (InterruptedException e) {
         }
      }

      Point endLocation = getLocation();
      //Debug.info("end location:" + getLocation());

      // if the location has changed
      if (endLocation.x != startLocation.x ||
         endLocation.y != startLocation.y){
         //Debug.info("user has moved the position of this dialog box");

         // it must be the result of user's moving the dialog box
         // to a presumably preferred location
         userPreferredLocation = endLocation;
      }

      setVisible(false);
      return command;
   }

   public void addButton(String name){
      if (defaultButton != null){
         //Debug.info("removing" + defaultButton);

         buttons.remove(defaultButton);
         buttons.removeAll();
         defaultButton = null;
      }
      buttons.add(new Button(name));
   }

   public void setText(String text) {
      textPane.setText(text);
      pack();
   }

   class Button extends JButton implements ActionListener{

      public Button(String text){
         super(text);
         Font f = new Font("sansserif", Font.BOLD, 12);
         setFont(f);
         setFocusable(false);
         setActionCommand(text);
         addActionListener(this);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         command = e.getActionCommand();
         dismiss();
      }

   }

   class TextPane extends HTMLTextPane{

      public TextPane(){
         super();
         setFont(new Font("sansserif", Font.PLAIN, 14));
         setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
      }
   }

}
