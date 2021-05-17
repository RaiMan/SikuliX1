/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import com.explodingpixels.macwidgets.plaf.UnifiedToolbarButtonUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class ButtonOnToolbar extends JButton implements ActionListener {

   String buttonText;
   String buttonHint;
   String iconFile;

   public ButtonOnToolbar(){
      setBorderPainted(false);
      putClientProperty("JButton.buttonType", "textured");
      setUI(new UnifiedToolbarButtonUI());
      setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
   }

   void init() {
      URL imageURL = SikulixIDE.class.getResource(iconFile);
      setIcon(new ImageIcon(imageURL));
      setText(buttonText);
      setToolTipText(buttonHint);
      addActionListener(this);
   }

   boolean shouldRun() {
      return true;
   }

   void runAction(ActionEvent e) {
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      runAction(e);
   }
}
