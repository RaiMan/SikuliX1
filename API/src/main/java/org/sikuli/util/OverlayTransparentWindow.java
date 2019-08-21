/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import org.sikuli.basics.Debug;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * INTERNAL USE
 * implements a transparent screen overlay for various purposes
 */
public class OverlayTransparentWindow extends JFrame implements EventSubject {

  private JPanel _panel = null;
  private Color _col = null;
  private OverlayTransparentWindow _win = null;
  private Graphics2D _currG2D = null;
  private EventObserver _obs;

  public OverlayTransparentWindow() {
    init(null, null);
  }

  public OverlayTransparentWindow(Color col, EventObserver o) {
    init(col, o);
  }

  private void init(Color col, EventObserver o) {
    setUndecorated(true);
    setAlwaysOnTop(true);
    if (col != null) {
      _obs = o;
      _win = this;
      try {
        setBackground(col);
      } catch (Exception e) {
        Debug.error("OverlayTransparentWindow.setBackground: did not work");
      }
      _panel = new javax.swing.JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
          if (g instanceof Graphics2D) {
              _currG2D = (Graphics2D) g;
            if (_obs != null) {
              _obs.update(_win);
            }
          } else {
            super.paintComponent(g);
          }
        }
      };
      _panel.setLayout(null);
      add(_panel);
    }
  }

  @Override
  public void setOpacity(float alpha) {
    try {
      Class<?> c = Class.forName("javax.swing.JFrame");
      Method m = c.getMethod("setOpacity", float.class);
      m.invoke(this, alpha);
    } catch (Exception e) {
      Debug.error("OverlayTransparentWindow.setOpacity: did not work");
    }
  }

  public JPanel getJPanel() {
    return _panel;
  }

  public Graphics2D getJPanelGraphics() {
    return _currG2D;
  }

  @Override
  public void addObserver(EventObserver o) {
    _obs = o;
  }

  @Override
  public void notifyObserver() {
    _obs.update(this);
  }

  public void close() {
    setVisible(false);
    dispose();
  }
}
