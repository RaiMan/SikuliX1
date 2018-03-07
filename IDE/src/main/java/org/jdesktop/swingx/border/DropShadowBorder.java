/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.jdesktop.swingx.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.border.Border;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.util.GraphicsUtilities;

/**
 * Implements a DropShadow for components. In general, the DropShadowBorder will
 * work with any rectangular components that do not have a default border
 * installed as part of the look and feel, or otherwise. For example,
 * DropShadowBorder works wonderfully with JPanel, but horribly with JComboBox.
 * <p>
 * Note: {@code DropShadowBorder} should usually be added to non-opaque
 * components, otherwise the background is likely to bleed through.</p>
 * <p>Note: Since generating drop shadows is relatively expensive operation,
 * {@code DropShadowBorder} keeps internal static cache that allows sharing
 * same border for multiple re-rendering and between different instances of the
 * class. Since this cache is shared at class level and never reset, it might
 * bleed your app memory in case you tend to create many different borders
 * rapidly.</p>
 * @author rbair
 */
@JavaBean
public class DropShadowBorder implements Border, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 715287754750604058L;

    private static enum Position {TOP, TOP_LEFT, LEFT, BOTTOM_LEFT,
                    BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT}

    private static final Map<Double,Map<Position,BufferedImage>> CACHE
            = new HashMap<Double,Map<Position,BufferedImage>>();

    private Color shadowColor;
    private int shadowSize;
    private float shadowOpacity;
    private int cornerSize;
    private boolean showTopShadow;
    private boolean showLeftShadow;
    private boolean showBottomShadow;
    private boolean showRightShadow;

    public DropShadowBorder() {
        this(Color.BLACK, 5);
    }

    public DropShadowBorder(Color shadowColor, int shadowSize) {
        this(shadowColor, shadowSize, .5f, 12, false, false, true, true);
    }

    public DropShadowBorder(boolean showLeftShadow) {
        this(Color.BLACK, 5, .5f, 12, false, showLeftShadow, true, true);
    }

    public DropShadowBorder(Color shadowColor, int shadowSize,
            float shadowOpacity, int cornerSize, boolean showTopShadow,
            boolean showLeftShadow, boolean showBottomShadow, boolean showRightShadow) {
        this.shadowColor = shadowColor;
        this.shadowSize = shadowSize;
        this.shadowOpacity = shadowOpacity;
        this.cornerSize = cornerSize;
        this.showTopShadow = showTopShadow;
        this.showLeftShadow = showLeftShadow;
        this.showBottomShadow = showBottomShadow;
        this.showRightShadow = showRightShadow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
        /*
         * 1) Get images for this border
         * 2) Paint the images for each side of the border that should be painted
         */
        Map<Position,BufferedImage> images = getImages((Graphics2D)graphics);

        Graphics2D g2 = (Graphics2D)graphics.create();

        try {
            //The location and size of the shadows depends on which shadows are being
            //drawn. For instance, if the left & bottom shadows are being drawn, then
            //the left shadow extends all the way down to the corner, a corner is drawn,
            //and then the bottom shadow begins at the corner. If, however, only the
            //bottom shadow is drawn, then the bottom-left corner is drawn to the
            //right of the corner, and the bottom shadow is somewhat shorter than before.

            int shadowOffset = 2; //the distance between the shadow and the edge

            Point topLeftShadowPoint = null;
            if (showLeftShadow || showTopShadow) {
                topLeftShadowPoint = new Point();
                if (showLeftShadow && !showTopShadow) {
                    topLeftShadowPoint.setLocation(x, y + shadowOffset);
                } else if (showLeftShadow && showTopShadow) {
                    topLeftShadowPoint.setLocation(x, y);
                } else if (!showLeftShadow && showTopShadow) {
                    topLeftShadowPoint.setLocation(x + shadowSize, y);
                }
            }

            Point bottomLeftShadowPoint = null;
            if (showLeftShadow || showBottomShadow) {
                bottomLeftShadowPoint = new Point();
                if (showLeftShadow && !showBottomShadow) {
                    bottomLeftShadowPoint.setLocation(x, y + height - shadowSize - shadowSize);
                } else if (showLeftShadow && showBottomShadow) {
                    bottomLeftShadowPoint.setLocation(x, y + height - shadowSize);
                } else if (!showLeftShadow && showBottomShadow) {
                    bottomLeftShadowPoint.setLocation(x + shadowSize, y + height - shadowSize);
                }
            }

            Point bottomRightShadowPoint = null;
            if (showRightShadow || showBottomShadow) {
                bottomRightShadowPoint = new Point();
                if (showRightShadow && !showBottomShadow) {
                    bottomRightShadowPoint.setLocation(x + width - shadowSize, y + height - shadowSize - shadowSize);
                } else if (showRightShadow && showBottomShadow) {
                    bottomRightShadowPoint.setLocation(x + width - shadowSize, y + height - shadowSize);
                } else if (!showRightShadow && showBottomShadow) {
                    bottomRightShadowPoint.setLocation(x + width - shadowSize - shadowSize, y + height - shadowSize);
                }
            }

            Point topRightShadowPoint = null;
            if (showRightShadow || showTopShadow) {
                topRightShadowPoint = new Point();
                if (showRightShadow && !showTopShadow) {
                    topRightShadowPoint.setLocation(x + width - shadowSize, y + shadowOffset);
                } else if (showRightShadow && showTopShadow) {
                    topRightShadowPoint.setLocation(x + width - shadowSize, y);
                } else if (!showRightShadow && showTopShadow) {
                    topRightShadowPoint.setLocation(x + width - shadowSize - shadowSize, y);
                }
            }

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_SPEED);

            if (showLeftShadow) {
                Rectangle leftShadowRect =
                    new Rectangle(x,
                                  topLeftShadowPoint.y + shadowSize,
                                  shadowSize,
                                  bottomLeftShadowPoint.y - topLeftShadowPoint.y - shadowSize);
                g2.drawImage(images.get(Position.LEFT),
                             leftShadowRect.x, leftShadowRect.y,
                             leftShadowRect.width, leftShadowRect.height, null);
            }

            if (showBottomShadow) {
                Rectangle bottomShadowRect =
                    new Rectangle(bottomLeftShadowPoint.x + shadowSize,
                                  y + height - shadowSize,
                                  bottomRightShadowPoint.x - bottomLeftShadowPoint.x - shadowSize,
                                  shadowSize);
                g2.drawImage(images.get(Position.BOTTOM),
                             bottomShadowRect.x, bottomShadowRect.y,
                             bottomShadowRect.width, bottomShadowRect.height, null);
            }

            if (showRightShadow) {
                Rectangle rightShadowRect =
                    new Rectangle(x + width - shadowSize,
                                  topRightShadowPoint.y + shadowSize,
                                  shadowSize,
                                  bottomRightShadowPoint.y - topRightShadowPoint.y - shadowSize);
                g2.drawImage(images.get(Position.RIGHT),
                             rightShadowRect.x, rightShadowRect.y,
                             rightShadowRect.width, rightShadowRect.height, null);
            }

            if (showTopShadow) {
                Rectangle topShadowRect =
                    new Rectangle(topLeftShadowPoint.x + shadowSize,
                                  y,
                                  topRightShadowPoint.x - topLeftShadowPoint.x - shadowSize,
                                  shadowSize);
                g2.drawImage(images.get(Position.TOP),
                             topShadowRect.x, topShadowRect.y,
                             topShadowRect.width, topShadowRect.height, null);
            }

            if (showLeftShadow || showTopShadow) {
                g2.drawImage(images.get(Position.TOP_LEFT),
                             topLeftShadowPoint.x, topLeftShadowPoint.y, null);
            }
            if (showLeftShadow || showBottomShadow) {
                g2.drawImage(images.get(Position.BOTTOM_LEFT),
                             bottomLeftShadowPoint.x, bottomLeftShadowPoint.y, null);
            }
            if (showRightShadow || showBottomShadow) {
                g2.drawImage(images.get(Position.BOTTOM_RIGHT),
                             bottomRightShadowPoint.x, bottomRightShadowPoint.y, null);
            }
            if (showRightShadow || showTopShadow) {
                g2.drawImage(images.get(Position.TOP_RIGHT),
                             topRightShadowPoint.x, topRightShadowPoint.y, null);
            }
        } finally {
            g2.dispose();
        }
    }

    private Map<Position,BufferedImage> getImages(Graphics2D g2) {
        //first, check to see if an image for this size has already been rendered
        //if so, use the cache. Else, draw and save
        Map<Position,BufferedImage> images = CACHE.get(shadowSize + (shadowColor.hashCode() * .3) + (shadowOpacity * .12));//TODO do a real hash
        if (images == null) {
            images = new HashMap<Position,BufferedImage>();

            /*
             * To draw a drop shadow, I have to:
             *  1) Create a rounded rectangle
             *  2) Create a BufferedImage to draw the rounded rect in
             *  3) Translate the graphics for the image, so that the rectangle
             *     is centered in the drawn space. The border around the rectangle
             *     needs to be shadowWidth wide, so that there is space for the
             *     shadow to be drawn.
             *  4) Draw the rounded rect as shadowColor, with an opacity of shadowOpacity
             *  5) Create the BLUR_KERNEL
             *  6) Blur the image
             *  7) copy off the corners, sides, etc into images to be used for
             *     drawing the Border
             */
            int rectWidth = cornerSize + 1;
            RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, rectWidth, rectWidth, cornerSize, cornerSize);
            int imageWidth = rectWidth + shadowSize * 2;
            BufferedImage image = GraphicsUtilities.createCompatibleTranslucentImage(imageWidth, imageWidth);
            Graphics2D buffer = (Graphics2D)image.getGraphics();

            try {
                buffer.setPaint(new Color(shadowColor.getRed(), shadowColor.getGreen(),
                        shadowColor.getBlue(), (int)(shadowOpacity * 255)));
//                buffer.setColor(new Color(0.0f, 0.0f, 0.0f, shadowOpacity));
                buffer.translate(shadowSize, shadowSize);
                buffer.fill(rect);
            } finally {
                buffer.dispose();
            }

            float blurry = 1.0f / (shadowSize * shadowSize);
            float[] blurKernel = new float[shadowSize * shadowSize];
            for (int i=0; i<blurKernel.length; i++) {
                blurKernel[i] = blurry;
            }
            ConvolveOp blur = new ConvolveOp(new Kernel(shadowSize, shadowSize, blurKernel));
            BufferedImage targetImage = GraphicsUtilities.createCompatibleTranslucentImage(imageWidth, imageWidth);
            ((Graphics2D)targetImage.getGraphics()).drawImage(image, blur, -(shadowSize/2), -(shadowSize/2));

            int x = 1;
            int y = 1;
            int w = shadowSize;
            int h = shadowSize;
            images.put(Position.TOP_LEFT, getSubImage(targetImage, x, y, w, h));
            x = 1;
            y = h;
            w = shadowSize;
            h = 1;
            images.put(Position.LEFT, getSubImage(targetImage, x, y, w, h));
            x = 1;
            y = rectWidth;
            w = shadowSize;
            h = shadowSize;
            images.put(Position.BOTTOM_LEFT, getSubImage(targetImage, x, y, w, h));
            x = cornerSize + 1;
            y = rectWidth;
            w = 1;
            h = shadowSize;
            images.put(Position.BOTTOM, getSubImage(targetImage, x, y, w, h));
            x = rectWidth;
            y = x;
            w = shadowSize;
            h = shadowSize;
            images.put(Position.BOTTOM_RIGHT, getSubImage(targetImage, x, y, w, h));
            x = rectWidth;
            y = cornerSize + 1;
            w = shadowSize;
            h = 1;
            images.put(Position.RIGHT, getSubImage(targetImage, x, y, w, h));
            x = rectWidth;
            y = 1;
            w = shadowSize;
            h = shadowSize;
            images.put(Position.TOP_RIGHT, getSubImage(targetImage, x, y, w, h));
            x = shadowSize;
            y = 1;
            w = 1;
            h = shadowSize;
            images.put(Position.TOP, getSubImage(targetImage, x, y, w, h));

            image.flush();
            CACHE.put(shadowSize + (shadowColor.hashCode() * .3) + (shadowOpacity * .12), images); //TODO do a real hash
        }
        return images;
    }

    /**
     * Returns a new BufferedImage that represents a subregion of the given
     * BufferedImage.  (Note that this method does not use
     * BufferedImage.getSubimage(), which will defeat image acceleration
     * strategies on later JDKs.)
     */
    private BufferedImage getSubImage(BufferedImage img,
                                      int x, int y, int w, int h) {
        BufferedImage ret = GraphicsUtilities.createCompatibleTranslucentImage(w, h);
        Graphics2D g2 = ret.createGraphics();

        try {
            g2.drawImage(img,
                         0, 0, w, h,
                         x, y, x+w, y+h,
                         null);
        } finally {
            g2.dispose();
        }

        return ret;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Insets getBorderInsets(Component c) {
        int top = showTopShadow ? shadowSize : 0;
        int left = showLeftShadow ? shadowSize : 0;
        int bottom = showBottomShadow ? shadowSize : 0;
        int right = showRightShadow ? shadowSize : 0;
        return new Insets(top, left, bottom, right);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    public boolean isShowTopShadow() {
        return showTopShadow;
    }

    public boolean isShowLeftShadow() {
        return showLeftShadow;
    }

    public boolean isShowRightShadow() {
        return showRightShadow;
    }

    public boolean isShowBottomShadow() {
        return showBottomShadow;
    }

    public int getShadowSize() {
        return shadowSize;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public float getShadowOpacity() {
        return shadowOpacity;
    }

    public int getCornerSize() {
        return cornerSize;
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setShadowSize(int shadowSize) {
        this.shadowSize = shadowSize;
    }

    public void setShadowOpacity(float shadowOpacity) {
        this.shadowOpacity = shadowOpacity;
    }

    public void setCornerSize(int cornerSize) {
        this.cornerSize = cornerSize;
    }

    public void setShowTopShadow(boolean showTopShadow) {
        this.showTopShadow = showTopShadow;
    }

    public void setShowLeftShadow(boolean showLeftShadow) {
        this.showLeftShadow = showLeftShadow;
    }

    public void setShowBottomShadow(boolean showBottomShadow) {
        this.showBottomShadow = showBottomShadow;
    }

    public void setShowRightShadow(boolean showRightShadow) {
        this.showRightShadow = showRightShadow;
    }
}
