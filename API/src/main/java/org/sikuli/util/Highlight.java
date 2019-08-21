package org.sikuli.util;

import org.sikuli.basics.Debug;
import org.sikuli.script.Location;
import org.sikuli.script.Region;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class Highlight extends JFrame {

  static boolean hasTL = false;
  static boolean hasPPTL = false;
  static boolean hasPPTP = false;
  static double gdW = -1;
  static double gdH = -1;

  static Highlight activeHighlight = null;

  static {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    gdW = gd.getDefaultConfiguration().getBounds().getWidth();
    gdH = gd.getDefaultConfiguration().getBounds().getHeight();
    hasTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
    hasPPTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    hasPPTP = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
  }

  static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  int side = 70;
  float halfSide = side * 0.5f;
  float halfSidex = halfSide;
  float halfSidey = halfSide;
  int locx = -1;
  int locy = -1;
  int sidex = -1;
  int sidey = -1;
  int frameX = -1;
  int frameY = -1;

  public Highlight(Location loc) {
    this();
    locx = loc.x;
    locy = loc.y;
    JPanel panel = initAsCross();
    setContentPane(panel);
  }

  public Highlight(Region reg) {
    this();
    locx = reg.x;
    locy = reg.y;
    sidex = reg.h;
    sidey = reg.w;
    JPanel panel = initAsFrame();
    setContentPane(panel);
  }

  private Highlight() {
    if (!hasPPTL) {
      Debug.error("Highlight: is not supported in your environment:\n%s");
      Debug.error("Highlight: TansparencySupport: TL: %s PPTL: %s PPTP: %s", Highlight.hasTL, Highlight.hasPPTL, Highlight.hasPPTP);
    }
    setUndecorated(true);
    setBackground(new Color(0, 0, 0, 0));
    setAlwaysOnTop(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    activeHighlight = this;
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

  private JPanel initAsFrame() {
    int lineWidth = 3;
    frameX = locx - lineWidth;
    frameY = locy - lineWidth;
    int frameW = sidey + 2 * lineWidth;
    int frameH = sidex + 2 * lineWidth;
    setSize(frameW, frameH);
    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
          Point2D.Float center = new Point2D.Float(frameW / 2.0f, frameH / 2.0f);
          Color redFull = new Color(255, 0, 0, 30);
          Color redTrans = new Color(255, 0, 0, 0);
          float radius = Math.max(center.x, center.y);
          float[] dist = {0.0f, 1.0f};
          Color[] colorsRed = {redTrans, redFull};
          Graphics2D g2d = (Graphics2D) g;
          g2d.setPaint(new RadialGradientPaint(center, radius, dist, colorsRed));
          g2d.fillRect(0, 0, frameW, frameH);
        }
      }
    };
    panel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
    return panel;
  }

  public void doShow(double secs) {
    if (locx < 0 || locy < 0) {
      frameX = (int) (gdW / 2);
      frameY = (int) (gdH / 2);
    }
    setLocation(frameX, frameY);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        setVisible(false);
        dispose();
      }
    });
      SwingUtilities.invokeLater(() -> setVisible(true));
    try {
      Thread.sleep((int) (secs * 1000));
    } catch (InterruptedException ex) {
    }
    Highlight.close();
  }

  public static void close() {
    if (null != activeHighlight) {
      activeHighlight.setVisible(false);
      activeHighlight.dispose();
      activeHighlight = null;
    }
  }
}
