/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.guide;

import org.sikuli.script.Region;
import org.sikuli.util.OverlayTransparentWindow;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;
import org.sikuli.basics.Settings;
import org.sikuli.natives.SysUtil;

public class SxBeam extends OverlayTransparentWindow
        implements Transition, GlobalMouseMotionListener, EventObserver {

  Guide guide;

  public SxBeam(Guide guide, Region target) {
    super(new Color(1f, 0f, 0f, 0.7f), null);
    super.addObserver(this);
    this.guide = guide;
    this.target = target;

    /*
     setBackground(null);
     // when opaque is set to false, the content seems to get cleared properly
     // this is tested on both Windows and Mac
     SysUtil.getOSUtil().setWindowOpaque(this, false);
     setOpacity(0.7f);
     */
  }
  public Point current = null;
  public Point to = null;
  Region target;

  @Override
  public void update(EventSubject es) {
    Graphics2D g = ((OverlayTransparentWindow) es).getJPanelGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    drawRayPolygon(g, current, target.getRect());
  }
  /*
   public void paint(Graphics g){
   Graphics2D g2d = (Graphics2D)g;
   super.paint(g);

   g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   drawRayPolygon(g, current, target.getRect());
   }
   */

  public void drawRayPolygon(Graphics g, Point p, Rectangle rect) {
    if (p == null || rect == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) g;

    Rectangle r = rect;
    Ellipse2D.Double ellipse =
            new Ellipse2D.Double(r.x, r.y, r.width - 1, r.height - 1);

    g2d.setColor(Color.red);
    g2d.fill(ellipse);
    //g2d.drawRect(rect.x,rect.y,rect.width,rect.height);

    // compute tangent points
    g2d.translate(rect.x + rect.width / 2, rect.y + rect.height / 2);

    float a0 = r.width / 2;
    float b0 = r.height / 2;
    float a = a0 * a0;
    float b = b0 * b0;

    float m = p.x - rect.x - rect.width / 2;
    float n = p.y - rect.y - rect.height / 2;
    float t1 = (1f + (a * n * n) / (b * m * m));
    float t2 = -2f * a * n / (m * m);
    float t3 = (b * a) / (m * m) - b;

    float s = (float) Math.sqrt(t2 * t2 - 4 * t1 * t3);
    float y1 = (-t2 + s) / (2 * t1);
    float y2 = (-t2 - s) / (2 * t1);

    float x1 = a / m - y1 * (a * n) / (b * m);
    float x2 = a / m - y2 * (a * n) / (b * m);

//      g2d.drawLine((int)m,(int)n,(int)x1,(int)y1);
//      g2d.drawLine((int)m,(int)n,(int)x2,(int)y2);
//
//
//      g2d.setColor(Color.black);
//      g2d.drawLine(0,0,(int)a0,(int)b0);
//
//
//
    GeneralPath flagShape = new GeneralPath();
    flagShape.moveTo(m, n);
    flagShape.lineTo(x1, y1);
    flagShape.lineTo(x2, y2);
    flagShape.closePath();

    g2d.fill(flagShape);

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));

    //Rectangle smaller = new Rectangle(rect);
    ellipse =
            new Ellipse2D.Double(-r.width / 2 + 3, -r.height / 2 + 3, r.width - 6, r.height - 6);
    g2d.fill(ellipse);
  }

  public void drawRayPolygon1(Graphics g, Point p, Rectangle rect) {
    if (p == null || rect == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) g;

    Rectangle r = rect;
    // corners of the target rectangle
    int cxs[] = {r.x, r.x, r.x + r.width, r.x + r.width};
    int cys[] = {r.y, r.y + r.height, r.y + r.height, r.height};

    ArrayList<Point> corners = new ArrayList<Point>();
    corners.add(new Point(r.x, r.y));
    corners.add(new Point(r.x + r.width, r.y + r.height));
    corners.add(new Point(r.x + r.width, r.y));
    corners.add(new Point(r.x, r.y + r.height));

    Collections.sort(corners, new Comparator() {
      @Override
      public int compare(Object arg0, Object arg1) {
        return (int) (current.distance((Point) arg0) - current.distance((Point) arg1));
      }
    });

    int[] xs;
    int[] ys;

    int d = 5;
    if (p.x > rect.getMinX() - 5 && p.x < rect.getMaxX() + 5
            || p.y > rect.getMinY() - 5 && p.y < rect.getMaxY() + 5) {

      xs = new int[3];
      ys = new int[3];

      xs[0] = (int) p.x;
      xs[1] = (int) corners.get(0).x;
      xs[2] = (int) corners.get(1).x;

      ys[0] = (int) p.y;
      ys[1] = (int) corners.get(0).y;
      ys[2] = (int) corners.get(1).y;

    } else {

      xs = new int[4];
      ys = new int[4];

      xs[0] = (int) p.x;
      xs[1] = (int) corners.get(2).x;
      xs[2] = (int) corners.get(0).x;
      xs[3] = (int) corners.get(1).x;

      ys[0] = (int) p.y;
      ys[1] = (int) corners.get(2).y;
      ys[2] = (int) corners.get(0).y;
      ys[3] = (int) corners.get(1).y;
    }

    Polygon shape = new Polygon(xs, ys, xs.length);

    Stroke pen = new BasicStroke(3.0F);
    g2d.setStroke(pen);
    g2d.setColor(Color.black);
    //g2d.drawPolygon(pointing_triangle);
    //g2d.drawRect(x,y,w,h);

    g2d.setColor(Color.red);
    g2d.fillPolygon(shape);
    g2d.drawRect(rect.x, rect.y, rect.width, rect.height);


  }

  @Override
  public void toFront() {
    if ( Settings.isMac() || Settings.isWindows() ) {
      // this call is necessary to allow clicks to go through the window (ignoreMouse == true)
      if (Settings.JavaVersion < 7) {
          SysUtil.getOSUtil().bringWindowToFront(this, true);
      } else {
      }
    }
    super.toFront();
  }

  GlobalMouseMotionTracker mouseTracker;
  TransitionListener listener;

  @Override
  public String waitForTransition(TransitionListener listener) {
    this.listener = listener;

    mouseTracker = GlobalMouseMotionTracker.getInstance();
    mouseTracker.addListener(this);
    mouseTracker.start();

    setBounds(guide.getRegion().getRect());
    setVisible(true);
    toFront();

//      repaint();

//      Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
//      Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
//      Cursor currentCursor = null;
//
//      boolean running = true;
//      while (running){
//
//         Rectangle target_rect = target.getRect();
//        Location m = Region.atMouse();
//
//         current = new Point(m.x,m.y);
//
//         Cursor cursor = null;
//         if (target_rect.contains(current)){
//            running = false;
//            cursor = handCursor;
//
//            setVisible(false);
//            dispose();
////            synchronized(guide){
////               guide.notify();
////               dispose();
////               return "Next";
////            }
//            token.transitionOccurred(this);
//            return "Next";
//
//         }else{
//            cursor = defaultCursor;
//            repaint();
//         }
//
//
//         if (cursor != currentCursor){
//            setCursor(cursor);
//            currentCursor = cursor;
//         }
//
//      }
    return "Next";
  }

  @Override
  public void globalMouseIdled(int x, int y) {
  }

  @Override
  public void globalMouseMoved(int x, int y) {

    current = new Point(x, y);
    repaint();

    if (target.getRect().contains(current)) {
      setVisible(false);
      dispose();
      listener.transitionOccurred(this);

    }
  }
}
