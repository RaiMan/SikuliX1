package org.sikuli.script.support;

import java.awt.*;

public class Highlight {

  boolean hasTL = false;
  boolean hasPPTL = false;
  boolean hasPPTP = false;

  public Highlight() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    hasTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
    hasPPTL = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    hasPPTP = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
  }

  static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  public static void main(String[] args) {
    Highlight highlight = new Highlight();
    p("TL: %s PPTL: %s PPTP: %s", highlight.hasTL, highlight.hasPPTL, highlight.hasPPTP);
  }
}
