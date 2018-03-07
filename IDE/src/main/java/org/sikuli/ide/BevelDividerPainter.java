/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import javax.swing.*;
import org.jdesktop.swingx.JXMultiSplitPane.DividerPainter;
import org.jdesktop.swingx.MultiSplitLayout.Divider;

class BevelDividerPainter extends DividerPainter {
   private JComponent owner;

   public BevelDividerPainter( JComponent c )
   {
      owner = c;
   }

	@Override
   public void doPaint(Graphics2D g, Divider divider, int width, int height)
   {
      Color c = owner.getBackground();
      g.setColor( c );
      g.fillRect(0, 0, width, height);

      int size = 1;
      if ( divider.isVertical()) {
         size = Math.max( size, ( width / 5 ) -1 );
         g.setColor( c.brighter());
         g.fillRect( 1, 0, size, height);
         g.setColor( c.darker());
         g.fillRect( width-size-1, 0, size, height);

         g.setColor( c.darker().darker());
         g.drawLine( 0, 0, 0, height);
         g.drawLine( width-1, 0, width-1, height);
      }
      else {
         size = Math.max( size, height / 5 );
         g.setColor( c.brighter());
         g.fillRect( 0, 1, width, size );
         g.setColor( c.darker());
         g.fillRect( 0, height-size-1, width, size );

         g.setColor( c.darker().darker());
         g.drawLine( 0, 0, width, 0 );
         g.drawLine( 0, height-1, width, height-1);
      }
   }
}
