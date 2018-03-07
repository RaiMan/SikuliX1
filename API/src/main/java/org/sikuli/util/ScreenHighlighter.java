/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import org.sikuli.basics.Animator;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.IScreen;
import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

/**
 * INTERNAL USE produces and manages the red framed rectangles from Region.highlight()
 */
public class ScreenHighlighter extends OverlayTransparentWindow implements MouseListener {

  static Color _transparentColor = new Color(0F, 0F, 0F, 0.2F);
  Color _targetColor = Color.RED;
  final static int TARGET_SIZE = 50;
  final static int DRAGGING_TIME = 200;
  static int MARGIN = 20;
  static Set<ScreenHighlighter> _opened = new HashSet<ScreenHighlighter>();
  IScreen _scr;
  BufferedImage _screen = null;
  BufferedImage _darker_screen = null;
  BufferedImage bi = null;
  int srcx, srcy, destx, desty;
  Location _lastTarget;
  boolean _borderOnly = false;
  Animator _anim;
  BasicStroke _StrokeCross = new BasicStroke(1);
  BasicStroke _StrokeCircle = new BasicStroke(2);
  BasicStroke _StrokeBorder = new BasicStroke(3);
  Animator _aniX, _aniY;
  boolean noWaitAfter = false;

  boolean _native_transparent = false;
  boolean _double_buffered = false;
  boolean _isTransparentSupported = false;

  public ScreenHighlighter(IScreen scr, String color) {
    _scr = scr;
    init();
    setVisible(false);
    setAlwaysOnTop(true);

    if (color != null) {
      // a frame color is specified
      // if not decodable, then predefined Color.RED is used
      if (color.startsWith("#")) {
        if (color.length() > 7) {
          // might be the version #nnnnnnnnn
          if (color.length() == 10) {
            int cR = 255, cG = 0, cB = 0;
            try {
              cR = Integer.decode(color.substring(1, 4));
              cG = Integer.decode(color.substring(4, 7));
              cB = Integer.decode(color.substring(7, 10));
            } catch (NumberFormatException ex) {
            }
            try {
              _targetColor = new Color(cR, cG, cB);
            } catch (IllegalArgumentException ex) {
            }
          }
        } else {
          // supposing it is a hex value
          try {
            _targetColor = new Color(Integer.decode(color));
          } catch (NumberFormatException nex) {
          }
        }
      } else {
        // supposing color contains one of the defined color names
        if (!color.endsWith("Gray") || "Gray".equals(color)) {
          // the name might be given with any mix of lower/upper-case
          // only lightGray, LIGHT_GRAY, darkGray and DARK_GRAY must be given exactly
          color = color.toUpperCase();
        }
        try {
          Field field = Class.forName("java.awt.Color").getField(color);
          _targetColor = (Color) field.get(null);
        } catch (Exception e) {
        }
      }
    }
  }

  private void init() {
    _opened.add(this);
    if (Settings.isLinux()) {
      _double_buffered = true;
    } else if (Settings.isMac()) {
      _native_transparent = true;
    }
    GraphicsDevice screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    _isTransparentSupported = screenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)
            && screenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)
            && screenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
//		((JPanel) getContentPane()).setDoubleBuffered(_double_buffered);
    addMouseListener(this);
  }

  public void setWaitAfter(boolean state) {
    noWaitAfter = state;
  }

  @Override
  public void close() {
    setVisible(false);
    _opened.remove(this);
    clean();
    if (!noWaitAfter) {
      try {
        Thread.sleep((int) (Settings.WaitAfterHighlight > 0.3f ? Settings.WaitAfterHighlight * 1000 - 300 : 300));
      } catch (InterruptedException e) {
      }
    }
  }

  private void closeAfter(float secs) {
    try {
      Thread.sleep((int) secs * 1000 - 300);
    } catch (InterruptedException e) {
    }
    close();
  }

  public static void closeAll() {
    if (_opened.size() > 0) {
      Debug.log(3, "ScreenHighlighter: Removing all highlights");
      for (ScreenHighlighter s : _opened) {
        if (s.isVisible()) {
          s.setVisible(false);
          s.clean();
        }
      }
      _opened.clear();
    }
  }

  private void clean() {
    dispose();
    _screen = null;
    _darker_screen = null;
    bi = null;
  }

  //<editor-fold defaultstate="collapsed" desc="mouse events not implemented">
  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
  //</editor-fold>

  @Override
  public void mouseClicked(MouseEvent e) {
    setVisible(false);
  }

  public void highlight(Region r_) {
    //change due to oracle blog: https://blogs.oracle.com/thejavatutorials/entry/translucent_and_shaped_windows_in
    if (!_isTransparentSupported) {
      Debug.error("highlight transparent is not support on " + System.getProperty("os.name")+ "!");
      //use at least an not transparent color
      _transparentColor = Color.pink;
    }
    _borderOnly = true;
    Region r;
    r = r_.grow(3);
    if (!_native_transparent) {
      captureScreen(r.x, r.y, r.w, r.h);
    }
    setLocation(r.x, r.y);
    setSize(r.w, r.h);
    this.setBackground(_transparentColor);
    setVisible(true);
    requestFocus();
  }

  protected boolean isWindowTranslucencySupported() {
    if(Settings.isLinux()){
      GraphicsDevice screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      return screenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)
              && screenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)
              &&  screenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
    }
    return true;
  }

  public void highlight(Region r_, float secs) {
    highlight(r_);
    closeAfter(secs);
  }

  public void showTarget(Location loc, float secs) {
    final int w = TARGET_SIZE, h = TARGET_SIZE;
    int x = loc.x - w / 2, y = loc.y - w / 2;
    _lastTarget = loc;
    Debug.log(2, "showTarget " + x + " " + y + " " + w + " " + h);
    showWindow(x, y, w, h, secs);
  }

  private void captureScreen(int x, int y, int w, int h) {
    ScreenImage img = ((Screen)_scr).captureforHighlight(x, y, w, h);
    _screen = img.getImage();
    float scaleFactor = .6f;
    RescaleOp op = new RescaleOp(scaleFactor, 0, null);
    _darker_screen = op.filter(_screen, null);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d;
    if (_native_transparent || _screen != null) {
      if (_double_buffered) {
        if (bi == null || bi.getWidth(this) != getWidth()
                || bi.getHeight(this) != getHeight()) {
          bi = new BufferedImage(
                  getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D bfG2 = bi.createGraphics();
        g2d = bfG2;
      } else {
        g2d = (Graphics2D) g;
      }
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
      g2d.fillRect(0, 0, getWidth(), getHeight());
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      if (_borderOnly) {
        if (!_native_transparent) {
          g2d.drawImage(_screen, 0, 0, this);
        }
        drawBorder(g2d);
      } else {
        if (!_native_transparent) {
          g2d.drawImage(_screen, 0, 0, this);
        }
        drawTarget(g2d);
      }
      if (_double_buffered) {
        ((Graphics2D) g).drawImage(bi, 0, 0, this);
      }
      if (!isVisible()) {
        setVisible(true);
      }
    } else {
      if (isVisible()) {
        setVisible(false);
      }
    }
  }

  private void drawBorder(Graphics2D g2d) {
    g2d.setColor(_targetColor);
    g2d.setStroke(_StrokeBorder);
    int w = (int) _StrokeBorder.getLineWidth();
    g2d.drawRect(w / 2, w / 2, getWidth() - w, getHeight() - w);
  }

  private void drawTarget(Graphics2D g2d) {
    int r = TARGET_SIZE / 2;
    g2d.setColor(Color.black);
    g2d.setStroke(_StrokeCross);
    g2d.drawLine(0, r, r * 2, r);
    g2d.drawLine(r, 0, r, r * 2);

    g2d.setColor(_targetColor);
    g2d.setStroke(_StrokeCircle);
    drawCircle(r, r, r - 4, g2d);
    drawCircle(r, r, r - 10, g2d);
  }

  private void drawCircle(int x, int y, int radius, Graphics g) {
    g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
  }

  private void showWindow(int x, int y, int w, int h, float secs) {
    if (!_native_transparent) {
      captureScreen(x, y, w, h);
    }
    setLocation(x, y);
    setSize(w, h);
    this.setBackground(_targetColor);
    this.repaint();
    setVisible(true);
    requestFocus();
    closeAfter(secs);
  }
}
