/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.Location;
import org.sikuli.script.Region;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Highlight extends JFrame {

  static boolean hasTL = false;
  static boolean hasPPTL = false;
  static boolean hasPPTP = false;
  static double gdW = -1;
  static double gdH = -1;

  static List<Highlight> highlights = new ArrayList<>();

  static {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    gdW = gd.getDefaultConfiguration().getBounds().getWidth();
    gdH = gd.getDefaultConfiguration().getBounds().getHeight();
    hasTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
    hasPPTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    hasPPTP = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
  }

  int side = 70;
  float halfSide = side * 0.5f;
  int locx = -1;
  int locy = -1;
  int sidex = -1;
  int sidey = -1;
  int frameX = -1;
  int frameY = -1;
  Region region = null;

  public boolean isShowable() {
    return showable;
  }

  boolean showable = false;

  private Highlight() {
    if (!hasPPTL) {
      Debug.error("Highlight: is not supported in your environment");
      Debug.error("Highlight: TansparencySupport: TL: %s PPTL: %s PPTP: %s", Highlight.hasTL, Highlight.hasPPTL, Highlight.hasPPTP);
    } else {
      showable = true;
      setUndecorated(true);
      setBackground(new Color(0, 0, 0, 0));
      setAlwaysOnTop(true);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setType(JFrame.Type.UTILITY);
      setFocusableWindowState(false);
      setAutoRequestFocus(false);
    }
  }

  public Highlight(Location loc) {
    this();
    if (isShowable()) {
      locx = loc.x;
      locy = loc.y;
      JPanel panel = initAsCross();
      setContentPane(panel);
    }
  }

  public Highlight(Region reg) {
    this(reg, null);
  }

  public Highlight(Region reg, String color) {
    this();
    if (isShowable()) {
      givenColor = evalColor(color);
      region = reg;
      locx = reg.x;
      locy = reg.y;
      sidex = reg.h;
      sidey = reg.w;
      JPanel panel = initAsFrame();
      if (panel == null) {
        getRootPane().setBorder(BorderFactory.createLineBorder(givenColor, 3));
      } else {
        setContentPane(panel);
      }
    }
  }

  private JPanel initAsCross() {
    setSize(side, side);
    frameX = locx - (int) halfSide;
    frameY = locy - (int) halfSide;
    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
          int side = getWidth();
          Point2D.Float center = new Point2D.Float(halfSide, halfSide);
          Color redFull = new Color(255, 0, 0, 150);
          Color redTrans = new Color(255, 0, 0, 0);
          float radius = halfSide;
          float[] dist = {0.0f, 1.0f};
          Color[] colorsRed = {redFull, redTrans};
          Color[] colorsBlack = {Color.BLACK, Color.WHITE};
          Graphics2D g2d = (Graphics2D) g;
          g2d.setPaint(new RadialGradientPaint(center, radius, dist, colorsRed));
          g2d.fillRect(0, 0, side, side);
          g2d.setPaint(new RadialGradientPaint(center, radius, dist, colorsBlack));
          g2d.fillRect((int) halfSide - 1, 0, 2, side);
          g2d.fillRect(0, (int) halfSide - 1, side, 2);
        }
      }
    };
    return panel;
  }

  private int defaultOpaque = 30;
  private Color defaultColor = new Color(255, 0, 0, defaultOpaque);
  private Color givenColor = null;

  private Color getColor() {
    if (null == givenColor) {
      return defaultColor;
    }
    return new Color(givenColor.getRed(), givenColor.getGreen(), givenColor.getBlue(), defaultOpaque);
  }

  private Color evalColor(String color) {
    Color targetColor = Color.RED;
    if (color == null || color.isEmpty()) {
      color = Settings.DefaultHighlightColor;
    }
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
            targetColor = new Color(cR, cG, cB);
          } catch (IllegalArgumentException ex) {
          }
        }
      } else {
        // supposing it is a hex value
        try {
          targetColor = new Color(Integer.decode(color));
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
        targetColor = (Color) field.get(null);
      } catch (Exception e) {
      }
    }
    return targetColor;
  }

  private JPanel initAsFrame() {
    int lineWidth = 3;
    frameX = locx - lineWidth;
    frameY = locy - lineWidth;
    int frameW = sidey + 2 * lineWidth;
    int frameH = sidex + 2 * lineWidth;
    setSize(frameW, frameH);
    if (Settings.HighlightTransparent) {
      return null;
    }
    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
          Point2D.Float center = new Point2D.Float(frameW / 2.0f, frameH / 2.0f);
          Color colorFull = getColor();
          Color colorTrans = new Color(colorFull.getRed(), colorFull.getGreen(), colorFull.getBlue(), 0);
          float radius = Math.max(center.x, center.y);
          float[] dist = {0.0f, 1.0f};
          Color[] colors = {colorTrans, colorFull};
          Graphics2D g2d = (Graphics2D) g;
          g2d.setPaint(new RadialGradientPaint(center, radius, dist, colors));
          g2d.fillRect(0, 0, frameW, frameH);
        }
      }
    };
    panel.setBorder(BorderFactory.createLineBorder(givenColor, 3));
    return panel;
  }

  public static Highlight fakeHighlight() {
    //TODO fakeHighlightOn
    return null;
  }

  public Highlight doShow() {
    return doShow(0);
  }

  public Highlight doShow(double secs) {
    if (isShowable()) {
      setLocation(frameX, frameY);
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          setVisible(false);
          dispose();
        }
      });
      synchronized (Highlight.class) {
        if (!highlights.contains(this)) {
          highlights.add(this);
        }
      }
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          setVisible(true);
        }
      });
      if (secs > 0) {
        try {
          Thread.sleep((int) (1000 * (secs + Settings.WaitAfterHighlight)));
        } catch (InterruptedException ex) {
        }
        close();
      }
    }
    return this;
  }

  public void unShow() {
    if (isShowable()) {
      setVisible(false);
    }
  }

  public void close() {
    if (isShowable()) {
      setVisible(false);
    }
    dispose();
    showable = false;
    if (null != region) {
      region.internalUseOnlyHighlightReset();
    }
    if (inCloseAll) {
      return;
    }
    synchronized (Highlight.class) {
      highlights.remove(this);
    }
  }

  private static boolean inCloseAll = false;

  public static void closeAll() {
    synchronized (Highlight.class) {
      if (highlights.size() > 0) {
        inCloseAll = true;
        for (Highlight highlight : highlights) {
          highlight.close();
        }
        highlights.clear();
        inCloseAll = false;
      }
    }
  }
}
