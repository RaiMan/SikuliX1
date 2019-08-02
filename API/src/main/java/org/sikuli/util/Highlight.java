package org.sikuli.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class Highlight extends JFrame {

  static boolean hasTL = false;
  static boolean hasPPTL = false;
  static boolean hasPPTP = false;

  static {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    hasTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
    hasPPTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    hasPPTP = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
  }

  public Highlight() {
    setUndecorated(true);
    setBackground(new Color(0, 0, 0, 0));
    int side = 70;
    setSize(side, side);
    setLocation(500, 500);
    setAlwaysOnTop(true);
    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
          int side = getWidth();
          float halfSide = side * 0.5f;
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
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        setVisible(false);
        dispose();
      }
    });
  }

  public static void main(String[] args) {
    //p("TL: %s PPTL: %s PPTP: %s", Highlight.hasTL, Highlight.hasPPTL, Highlight.hasPPTP);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Highlight highlight = new Highlight();
        highlight.setVisible(true);
      }
    });
  }
}
