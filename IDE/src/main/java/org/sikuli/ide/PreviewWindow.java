/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.ide;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.sikuli.script.Screen;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.devices.Devices;
import org.sikuli.script.support.devices.ScreenDevice;
import org.sikuli.util.EventSubject;
import org.sikuli.util.OverlayTransparentWindow;

import javax.swing.*;

/**
 *
 * @author rhocke
 */
public class PreviewWindow extends OverlayTransparentWindow implements MouseListener, KeyListener {
//public class PreviewWindow extends JFrame implements MouseListener, KeyListener {

  EditorPatternButton callerEPB = null;
  static Color baseColor = new Color(255, 0, 0, 25);

  public PreviewWindow(Object caller) {
    super(baseColor, null);
    if (caller instanceof EditorPatternButton) {
      callerEPB = (EditorPatternButton) caller;
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
