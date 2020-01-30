/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;

/**
 * Inspired from  http://zetcode.com/tutorials/javaswingtutorial/resizablecomponent
 * 
 * Implements a rectangle that can be resized using drag & drop.
 *
 * @author mbalmer
 */

public class Resizable extends JComponent {

    public static class Border implements javax.swing.border.Border {
      private int dist = 8;
      private Color color = Color.BLACK;
      private Stroke stroke = new BasicStroke(1);

      int locations[] = {
              SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.WEST,
              SwingConstants.EAST, SwingConstants.NORTH_WEST,
              SwingConstants.NORTH_EAST, SwingConstants.SOUTH_WEST,
              SwingConstants.SOUTH_EAST
      };

      int cursors[] = {
              Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR,
              Cursor.E_RESIZE_CURSOR, Cursor.NW_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
              Cursor.SW_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR
      };

      public Border(int dist) {
          this.dist = dist;
      }

      @Override
      public Insets getBorderInsets(Component component) {
          return new Insets(dist, dist, dist, dist);
      }

      @Override
      public boolean isBorderOpaque() {
          return false;
      }

      public void setColor(Color color) {
        this.color = color;
      }

      public void setStroke(Stroke stroke) {
        this.stroke = stroke;
      }

      @Override
      public void paintBorder(Component component, Graphics g, int x, int y,
                              int w, int h) {
          Graphics2D g2d = (Graphics2D) g;

          g2d.setColor(color);
          Stroke savedStroke = g2d.getStroke();
          g2d.setStroke(stroke);
          g2d.drawRect(x + dist / 2, y + dist / 2, w - dist, h - dist);
          g2d.setStroke(savedStroke);

          for (int i = 0; i < locations.length; i++) {

              Rectangle rect = getRectangle(x, y, w, h, locations[i]);

              g2d.setColor(Color.WHITE);
              g2d.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
              g2d.setColor(Color.BLACK);
              g2d.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
          }
      }

      private Rectangle getRectangle(int x, int y, int w, int h, int location) {

          switch (location) {

              case SwingConstants.NORTH:
                  return new Rectangle(x + w / 2 - dist / 2, y, dist, dist);

              case SwingConstants.SOUTH:
                  return new Rectangle(x + w / 2 - dist / 2, y + h - dist, dist, dist);

              case SwingConstants.WEST:
                  return new Rectangle(x, y + h / 2 - dist / 2, dist, dist);

              case SwingConstants.EAST:
                  return new Rectangle(x + w - dist, y + h / 2 - dist / 2, dist, dist);

              case SwingConstants.NORTH_WEST:
                  return new Rectangle(x, y, dist, dist);

              case SwingConstants.NORTH_EAST:
                  return new Rectangle(x + w - dist, y, dist, dist);

              case SwingConstants.SOUTH_WEST:
                  return new Rectangle(x, y + h - dist, dist, dist);

              case SwingConstants.SOUTH_EAST:
                  return new Rectangle(x + w - dist, y + h - dist, dist, dist);
          }
          return null;
      }

      public int getCursor(MouseEvent me) {

          Component c = me.getComponent();
          int w = c.getWidth();
          int h = c.getHeight();

          for (int i = 0; i < locations.length; i++) {

              Rectangle rect = getRectangle(0, 0, w, h, locations[i]);

              if (rect.contains(me.getPoint())) {
                  return cursors[i];
              }
          }

          return Cursor.MOVE_CURSOR;
      }
    }

    int MIN_WIDTH = 20;
    int MIN_HEIGHT = 20;

    private Rectangle maxBounds = new Rectangle(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public Resizable() {
        this(new Border(8));
    }

    public Resizable(Border border) {
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        setBorder(border);
    }

    private void resize() {

        if (getParent() != null) {
            getParent().revalidate();
        }
    }
    
    /**
     * Set the maximum extent the box can be resized to
     * 
     * @param maxBounds
     */
    public void setMaxBounds(Rectangle maxBounds){
      this.maxBounds = maxBounds;
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
      Rectangle b = new Rectangle(x,y,w,h).intersection(maxBounds);
      super.setBounds(b.x, b.y, b.width, b.height);
    }

    MouseInputListener mouseListener = new MouseInputAdapter() {

        @Override
        public void mouseMoved(MouseEvent me) {
            Border resizableBorder = (Border) getBorder();
            setCursor(Cursor.getPredefinedCursor(resizableBorder.getCursor(me)));
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            setCursor(Cursor.getDefaultCursor());
        }

        private int cursor;
        private Point startPos = null;
        private boolean moved = false;

        @Override
        public void mousePressed(MouseEvent me) {
            Border resizableBorder = (Border) getBorder();
            cursor = resizableBorder.getCursor(me);
            startPos = me.getPoint();
            moved = false;
        }

        @Override
        public void mouseDragged(MouseEvent me) {

            if (startPos != null) {
                moved = true;
                int x = getX();
                int y = getY();
                int w = getWidth();
                int h = getHeight();

                int dx = me.getX() - startPos.x;
                int dy = me.getY() - startPos.y;

                switch (cursor) {

                    case Cursor.N_RESIZE_CURSOR:

                        if (!(h - dy < MIN_HEIGHT)) {
                            setBounds(x, y + dy, w, h - dy);
                            resize();
                        }
                        break;

                    case Cursor.S_RESIZE_CURSOR:

                        if (!(h + dy < MIN_HEIGHT)) {
                            setBounds(x, y, w, h + dy);
                            startPos = me.getPoint();
                            resize();
                        }
                        break;

                    case Cursor.W_RESIZE_CURSOR:

                        if (!(w - dx < MIN_WIDTH)) {
                            setBounds(x + dx, y, w - dx, h);
                            resize();
                        }
                        break;

                    case Cursor.E_RESIZE_CURSOR:

                        if (!(w + dx < MIN_WIDTH)) {
                            setBounds(x, y, w + dx, h);
                            startPos = me.getPoint();
                            resize();
                        }
                        break;

                    case Cursor.NW_RESIZE_CURSOR:
                        if (!(w - dx < MIN_WIDTH) && !(h - dy < MIN_HEIGHT)) {
                            setBounds(x + dx, y + dy, w - dx, h - dy);
                            resize();
                        }
                        break;

                    case Cursor.NE_RESIZE_CURSOR:

                        if (!(w + dx < MIN_WIDTH) && !(h - dy < MIN_HEIGHT)) {
                            setBounds(x, y + dy, w + dx, h - dy);
                            startPos = new Point(me.getX(), startPos.y);
                            resize();
                        }
                        break;

                    case Cursor.SW_RESIZE_CURSOR:

                        if (!(w - dx < MIN_WIDTH) && !(h + dy < MIN_HEIGHT)) {
                            setBounds(x + dx, y, w - dx, h + dy);
                            startPos = new Point(startPos.x, me.getY());
                            resize();
                        }
                        break;

                    case Cursor.SE_RESIZE_CURSOR:

                        if (!(w + dx < MIN_WIDTH) && !(h + dy < MIN_HEIGHT)) {
                            setBounds(x, y, w + dx, h + dy);
                            startPos = me.getPoint();
                            resize();
                        }
                        break;

                    case Cursor.MOVE_CURSOR:

                        Rectangle bounds = getBounds();
                        bounds.translate(dx, dy);

                        int minX = maxBounds.x;
                        int maxX = maxBounds.x + maxBounds.width - bounds.width;

                        if (bounds.x < minX) {
                          bounds.x = minX;
                        } else if (bounds.x > maxX) {
                          bounds.x = maxX;
                        }

                        int minY = maxBounds.y;
                        int maxY = maxBounds.y + maxBounds.height - bounds.height;

                        if (bounds.y < minY) {
                          bounds.y = minY;
                        } else if (bounds.y > maxY) {
                          bounds.y = maxY;
                        }

                        setBounds(bounds);
                        resize();
                }

                setCursor(Cursor.getPredefinedCursor(cursor));
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
          Rectangle bounds = Resizable.this.getBounds();
          if(moved) {
            getParent().dispatchEvent(new MouseEvent(Resizable.this, MouseEvent.MOUSE_RELEASED, me.getWhen(), me.getModifiers(), bounds.x + me.getX(), bounds.y + me.getY(), me.getClickCount(), false));
          } else {
            getParent().dispatchEvent(new MouseEvent(Resizable.this, MouseEvent.MOUSE_PRESSED, me.getWhen(), me.getModifiers(), bounds.x + me.getX(), bounds.y + me.getY(), me.getClickCount(), false));
          }
          moved = false;
          startPos = null;
        }
    };
}
