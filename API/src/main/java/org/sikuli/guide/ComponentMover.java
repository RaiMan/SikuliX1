/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ComponentMover extends MouseAdapter{

   // private boolean moveWindow;


   private Class destinationClass;

   private Component destinationComponent;

   private Component destination;

   private Component source;


   private boolean changeCursor = true;


   private Point pressed;

   private Point location;


   private Cursor originalCursor;

   private boolean autoscrolls;


   private Insets dragInsets = new Insets(0, 0, 0, 0);

   private Dimension snapSize = new Dimension(1, 1);


   /**

    *  Constructor for moving individual components. The components must be

    *  regisetered using the registerComponent() method.

    */

   public ComponentMover()

   {

   }


   /**

    *  Constructor to specify a Class of Component that will be moved when

    *  drag events are generated on a registered child component. The events

    *  will be passed to the first ancestor of this specified class.

    *

    *  @param destinationClass  the Class of the ancestor component

    *  @param components         the Components to be registered for forwarding

    *                           drag events to the ancestor Component.

    */

   public ComponentMover(Class destinationClass, Component... components)

   {

      this.destinationClass = destinationClass;

      registerComponent( components );

   }


   /**

    *  Constructor to specify a parent component that will be moved when drag

    *  events are generated on a registered child component.

    *

    *  @param destinationComponent  the component drage events should be forwareded to

    *  @param components    the Components to be registered for forwarding drag

    *                       events to the parent component to be moved

    */

   public ComponentMover(Component destinationComponent, Component... components)

   {

      this.destinationComponent = destinationComponent;

      registerComponent( components );

   }


   /**

    *  Get the change cursor property

    *

    *  @return  the change cursor property

    */

   public boolean isChangeCursor()

   {

      return changeCursor;

   }


   /**

    *  Set the change cursor property

    *

    *  @param  changeCursor when true the cursor will be changed to the

    *                       Cursor.MOVE_CURSOR while the mouse is pressed

    */

   public void setChangeCursor(boolean changeCursor)

   {

      this.changeCursor = changeCursor;

   }


   /**

    *  Get the drag insets

    *

    *  @return  the drag insets

    */

   public Insets getDragInsets()

   {

      return dragInsets;

   }


   /**

    *  Set the drag insets. The insets specify an area where mouseDragged

    *  events should be ignored and therefore the component will not be moved.

    *  This will prevent these events from being confused with a

    *  MouseMotionListener that supports component resizing.

    *

    *  @param  dragInsets

    */

   public void setDragInsets(Insets dragInsets)

   {

      this.dragInsets = dragInsets;

   }


   /**

    *  Remove listeners from the specified component

    *

    *  @param components  components the listeners are removed from

    */

   public void deregisterComponent(Component... components)

   {

      for (Component component : components)

         component.removeMouseListener( this );

   }


   /**

    *  Add the required listeners to the specified component

    *

    *  @param components  components the listeners are added to

    */

   public void registerComponent(Component... components)

   {

      for (Component component : components)

         component.addMouseListener( this );

   }


   /**

    * Get the snap size

    *

    *  @return the snap size

    */

   public Dimension getSnapSize()

   {

      return snapSize;

   }


   /**

    *  Set the snap size. Forces the component to be snapped to

    *  the closest grid position. Snapping will occur when the mouse is

    *  dragged half way.

    */

   public void setSnapSize(Dimension snapSize)

   {

      this.snapSize = snapSize;

   }


   /**

    *  Setup the variables used to control the moving of the component:

    *

    *  source - the source component of the mouse event

    *  destination - the component that will ultimately be moved

    *  pressed - the Point where the mouse was pressed in the destination

    *      component coordinates.

    */

   @Override

   public void mousePressed(MouseEvent e)

   {

      source = e.getComponent();

      int width  = source.getSize().width  - dragInsets.left - dragInsets.right;

      int height = source.getSize().height - dragInsets.top - dragInsets.bottom;

      Rectangle r = new Rectangle(dragInsets.left, dragInsets.top, width, height);


      if (r.contains(e.getPoint()))

         setupForDragging(e);

   }


   private void setupForDragging(MouseEvent e)

   {

      source.addMouseMotionListener( this );


      //  Determine the component that will ultimately be moved


      if (destinationComponent != null)

      {

         destination = destinationComponent;

      }

      else if (destinationClass == null)

      {

         destination = source;

      }

      else  //  forward events to destination component

      {

         destination = SwingUtilities.getAncestorOfClass(destinationClass, source);

      }


      pressed = e.getLocationOnScreen();

      if (destination instanceof Visual){
         location = ((Visual) destination).getActualLocation();
      }else{
         location = destination.getLocation();
      }


      if (changeCursor)

      {

         originalCursor = source.getCursor();

         source.setCursor( Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );

      }


      //  Making sure autoscrolls is false will allow for smoother dragging of

      //  individual components


      if (destination instanceof JComponent)

      {

         JComponent jc = (JComponent)destination;

         autoscrolls = jc.getAutoscrolls();

         jc.setAutoscrolls( false );

      }

   }


   /**

    *  Move the component to its new location. The dragged Point must be in

    *  the destination coordinates.

    */

   @Override

   public void mouseDragged(MouseEvent e)

   {

      Point dragged = e.getLocationOnScreen();

      int dragX = getDragDistance(dragged.x, pressed.x, snapSize.width);

      int dragY = getDragDistance(dragged.y, pressed.y, snapSize.height);

      if (destination instanceof Visual)
         ((Visual) destination).setActualLocation(location.x + dragX, location.y + dragY);
      else
         destination.setLocation(location.x + dragX, location.y + dragY);

   }


   /*

    *  Determine how far the mouse has moved from where dragging started

    *  (Assume drag direction is down and right for positive drag distance)

    */

   private int getDragDistance(int larger, int smaller, int snapSize)

   {

      int halfway = snapSize / 2;

      int drag = larger - smaller;

      drag += (drag < 0) ? -halfway : halfway;

      drag = (drag / snapSize) * snapSize;


      return drag;

   }


   /**

    *  Restore the original state of the Component

    */

   @Override

   public void mouseReleased(MouseEvent e)

   {

      source.removeMouseMotionListener( this );


      if (changeCursor)

         source.setCursor( originalCursor );


      if (destination instanceof JComponent)

      {

         ((JComponent)destination).setAutoscrolls( autoscrolls );

      }

   }

}
