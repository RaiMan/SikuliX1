/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.util;

import org.sikuli.script.Screen;
import org.sikuli.script.support.devices.ScreenDevice;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rhocke
 */
public class PreviewWindow extends OverlayTransparentWindow implements MouseListener, KeyListener {
//public class PreviewWindow extends JFrame implements MouseListener, KeyListener {

  Object caller = null;
  Map<String, Object> options = new HashMap<>();
  static Color baseColor = new Color(255, 0, 0, 25);

  public PreviewWindow(Map<String, Object> options) {
    super(baseColor, null);
    this.options = options;
    caller = options.get("caller");
    if (caller != null) {
      addObserver((EventObserver) caller);
    }
    addMouseListener(this);
    addKeyListener(this);
    Screen scr = ScreenDevice.getPrimaryScreen();
    setSize(new Dimension(scr.w - getLocation().x, scr.h - getLocation().y));
    setVisible(true);
    requestFocus();
  }

  public void close() {
    setVisible(false);
    dispose();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

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

  @Override
  public void keyTyped(KeyEvent e) {
    char keyChar = e.getKeyChar();
    close();
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
}
