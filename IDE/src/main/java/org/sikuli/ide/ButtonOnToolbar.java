/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import com.explodingpixels.macwidgets.plaf.UnifiedToolbarButtonUI;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class ButtonOnToolbar extends JButton {

   public ButtonOnToolbar(){
      setBorderPainted(false);
      putClientProperty("JButton.buttonType", "textured");
      //setIconTextGap(8);
      /*
      setVerticalTextPosition(SwingConstants.BOTTOM);
      setHorizontalTextPosition(SwingConstants.CENTER);
      Font f = new Font(null, Font.PLAIN, 10);
      setFont(f);
      */
      setUI(new UnifiedToolbarButtonUI());
      //setMaximumSize(new Dimension(32,32));
      setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
   }
}
