package org.sikuli.util;

import org.sikuli.script.Location;
import org.sikuli.script.Region;
import org.sikuli.script.support.IScreen;
import org.sikuli.script.support.RunTime;

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
  int locx = -1;
  int locy = -1;
  int sidex = -1;
  int sidey = -1;

  public Highlight(Location loc) {
    this();
    locx = loc.x;
    locy = loc.y;
    initAsCross();
  }

  public Highlight(Region reg) {
    this();
    locx = reg.x;
    locy = reg.y;
    sidex = reg.h;
    sidey = reg.w;
    initAsFrame();
  }

  private Highlight() {
    setUndecorated(true);
    setBackground(new Color(0, 0, 0, 0));
    setAlwaysOnTop(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        setVisible(false);
        dispose();
      }
    });
    activeHighlight = this;
  }

  private void initAsCross() {
    setSize(side, side);
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
    setContentPane(panel);
  }

  private void initAsFrame() {

  }

  public void doShow(double secs) {
    if (locx < 0 || locy < 0) {
      locx = (int) (gdW/2);
      locy = (int) (gdH/2);
    }
    setLocation(locx - (int) halfSide, locy - (int) halfSide);
    showAndWait(secs);
  }

  private void showAndWait(double secs) {
    setVisible(true);
    RunTime.pause((float) secs);
    setVisible(false);
    dispose();
    activeHighlight = null;
  }

  public static void close() {
    if (null != activeHighlight) {
      activeHighlight.setVisible(false);
      activeHighlight.dispose();
      activeHighlight = null;
    }
  }

  public static void main(String[] args) {
    //p("TL: %s PPTL: %s PPTP: %s", Highlight.hasTL, Highlight.hasPPTL, Highlight.hasPPTP);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Highlight highlight = new Highlight(new Location(gdW/2, gdH/2));
        highlight.doShow(2);
      }
    });
  }
}
