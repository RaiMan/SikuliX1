/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.JLabel;

import org.sikuli.script.Region;

public class SxFlag extends Visual{

   // which direction this element is pointing
   public final static int DIRECTION_EAST = 1;
   public final static int DIRECTION_WEST = 2;
   public final static int DIRECTION_SOUTH = 3;
   public final static int DIRECTION_NORTH = 4;

   JLabel label;
   Rectangle textBox;
   Rectangle triangle;
   FontMetrics fm;
   String defFont = "sansserif";
   Font font;
   int defFontSize = 16;
   int direction;
   Dimension canonicalSize;
   GeneralPath flagShape;

   public SxFlag(String text){
      super();
      init(text);
   }

   private void init(String text){
      this.text = text;
      setForeground(colorText);
      setBackground(colorFront);
      textBox = new Rectangle();
      triangle = new Rectangle();
      font = new Font(defFont, Font.BOLD, defFontSize);
   }

   @Override
   public void updateComponent() {
      fm = getFontMetrics(font);
      textBox.setSize(fm.stringWidth(text),fm.getHeight());
      textBox.grow(PADDING_X, PADDING_Y);
      setLocationRelativeToRegion(getTarget(), layout);
   }

   @Override
   public Visual setText(String text){
      this.text = text;
      updateComponent();
      return this;
   }

   @Override
   public Visual setFont(String fontName, int fsize) {
     font = new Font(fontName, Font.BOLD, fsize>0 ? fsize : fontSize);
     updateComponent();
     return this;
   }

   @Override
   public Visual setLocationRelativeToRegion(Region region, Layout side) {
      if (side == Layout.TOP){
         setDirection(DIRECTION_SOUTH);
      } else if (side == Layout.BOTTOM){
         setDirection(DIRECTION_NORTH);
      } else if (side == Layout.LEFT){
         setDirection(DIRECTION_EAST);
      } else if (side == Layout.RIGHT){
         setDirection(DIRECTION_WEST);
      }
      return super.setLocationRelativeToRegion(region,side);
   }

   public void setDirection(int direction){
      this.direction = direction;
      if (direction == DIRECTION_EAST || direction == DIRECTION_WEST){
         triangle.setSize(10,textBox.height);
         canonicalSize = new Dimension(textBox.width + triangle.width, textBox.height);
      }else{
         triangle.setSize(20, 10);
         setActualSize(textBox.width, textBox.height + triangle.height);
         canonicalSize = new Dimension(textBox.width,  textBox.height + triangle.height);
      }
      setActualSize(canonicalSize);
      if (direction == DIRECTION_EAST){
         textBox.setLocation(0, 0);
      } else if (direction == DIRECTION_WEST){
         textBox.setLocation(triangle.width, 0);
      } else if (direction == DIRECTION_SOUTH){
         textBox.setLocation(0, 0);
      } else if (direction == DIRECTION_NORTH){
         textBox.setLocation(0, triangle.height);
      }
      flagShape = new GeneralPath();
      if (direction == DIRECTION_WEST || direction == DIRECTION_EAST) {
         flagShape.moveTo(0,0);
         flagShape.lineTo(textBox.width,0);
         flagShape.lineTo(textBox.width+triangle.width, textBox.height/2);
         flagShape.lineTo(textBox.width, textBox.height);
         flagShape.lineTo(0,textBox.height);
         flagShape.closePath();
      }else{
         flagShape.moveTo(0,0);
         flagShape.lineTo(textBox.width,0);
         flagShape.lineTo(textBox.width, textBox.height);
         flagShape.lineTo(textBox.width/2+8, textBox.height);
         flagShape.lineTo(textBox.width/2,textBox.height+triangle.height);
         flagShape.lineTo(textBox.width/2-8, textBox.height);
         flagShape.lineTo(0,textBox.height);
         flagShape.closePath();
      }
      if (direction == DIRECTION_WEST){
         AffineTransform rat = new AffineTransform();
         rat.setToTranslation(textBox.width + triangle.width, textBox.height);
         rat.rotate(Math.PI);
         flagShape.transform(rat);
      }else if (direction == DIRECTION_NORTH){
         AffineTransform rat = new AffineTransform();
         rat.setToTranslation(textBox.width, textBox.height+triangle.height);
         rat.rotate(Math.PI);
         flagShape.transform(rat);
      }
   }

   @Override
   public void paintComponent(Graphics g){
      Dimension d = new Dimension(textBox.width + triangle.width, textBox.height);
      Dimension originalSize = canonicalSize;
      Dimension actualSize = getActualSize();
      float scalex = 1f * actualSize.width / originalSize.width;
      float scaley = 1f * actualSize.height / originalSize.height;
      ((Graphics2D) g).scale(scalex, scaley);
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setFont(font);
      g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON));
      g2d.setColor(colorFront);
      g2d.fill(flagShape);
      // draw outline
      Stroke pen = new BasicStroke(1.0F);
      g2d.setStroke(pen);
      g2d.setColor(colorFrame);
      g2d.draw(flagShape);
      g2d.setColor(colorText);
      g2d.drawString(text, textBox.x + PADDING_X,
            textBox.y +  textBox.height - fm.getDescent() - PADDING_Y);
   }
}
