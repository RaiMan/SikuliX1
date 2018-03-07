/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/**
 *
 */
package org.sikuli.guide;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ShadowRenderer {

   Visual source;
   public ShadowRenderer(Visual source, int shadowSize){
      this.source = source;
      sourceActualSize = source.getActualSize();
      this.shadowSize = shadowSize;
   }

   float shadowOpacity = 0.8f;
   int shadowSize = 10;
   Color shadowColor = Color.black;
   BufferedImage createShadowMask(BufferedImage image){
      BufferedImage mask = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

      Graphics2D g2d = mask.createGraphics();
      g2d.drawImage(image, 0, 0, null);
      // Ar = As*Ad - Cr = Cs*Ad -> extract 'Ad'
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, shadowOpacity));
      g2d.setColor(shadowColor);
      g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
      g2d.dispose();
      return mask;
   }

   ConvolveOp getBlurOp(int size) {
      float[] data = new float[size * size];
      float value = 1 / (float) (size * size);
      for (int i = 0; i < data.length; i++) {
         data[i] = value;
      }
      return new ConvolveOp(new Kernel(size, size, data));
   }

   BufferedImage shadowImage = null;
   Dimension sourceActualSize = null;
   public BufferedImage createShadowImage(){

      BufferedImage image = new BufferedImage(source.getActualWidth() + shadowSize * 2,
            source.getActualHeight() + shadowSize * 2, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = image.createGraphics();
      g2.translate(shadowSize,shadowSize);
      source.paintPlain(g2);

      shadowImage = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
      getBlurOp(shadowSize).filter(createShadowMask(image), shadowImage);
      g2.dispose();

      //Debug.info("[Shadow] shadowImage: " + shadowImage);

      return shadowImage;
   }

   public void paintComponent(Graphics g){
      Graphics2D g2d = (Graphics2D)g;

      // create shadow image if the size of the source component has changed since last rendering attempt
      if (shadowImage == null || source.getActualHeight() != sourceActualSize.height ||
            source.getActualWidth() != sourceActualSize.width){
         createShadowImage();
         sourceActualSize = source.getActualSize();
      }
      //Debug.info("[Shadow] painting shadow" + shadowImage);
      g2d.drawImage(shadowImage, 0, 0, null, null);
   }
}
