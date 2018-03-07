/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import org.sikuli.util.OverlayCapturePrompt;
import org.sikuli.script.IScreen;
import org.sikuli.script.Region;
import org.sikuli.script.ScreenImage;
import org.sikuli.basics.Debug;
import org.sikuli.script.Screen;
import org.sikuli.util.EventObserver;
import org.sikuli.util.EventSubject;

class EditorRegionButton extends JButton implements ActionListener, EventObserver {

  private static final String me = "EditorRegionButton: ";
  EditorPane _pane;
  int _x, _y, _w, _h;

  public EditorRegionButton(EditorPane pane, int x, int y, int w, int h) {
    _pane = pane;
    _x = x;
    _y = y;
    _w = w;
    _h = h;
    setIcon(new ImageIcon(getRegionImage(x, y, w, h)));
    setBorderPainted(true);
    setToolTipText(this.toString());
    addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    SikuliIDE ide = SikuliIDE.getInstance();
    ide.setVisible(false);
    Screen.doPrompt(SikuliIDE._I("msgCapturePrompt"), this);
  }

  @Override
  public void update(EventSubject es) {
    OverlayCapturePrompt ocp = (OverlayCapturePrompt) es;
    ScreenImage simg = ocp.getSelection();
    Screen.closePrompt();
    if (simg != null) {
      try {
        Thread.sleep(300);
      } catch (InterruptedException ie) {
      }
      Rectangle roi = simg.getROI();
      _x = (int) roi.getX();
      _y = (int) roi.getY();
      _w = (int) roi.getWidth();
      _h = (int) roi.getHeight();
      BufferedImage img = getRegionImage(_x, _y, _w, _h);
      setIcon(new ImageIcon(img));
      setToolTipText(this.toString());
    }
    Screen.resetPrompt(ocp);
    SikuliIDE.showAgain();
  }

  private BufferedImage getRegionImage(int x, int y, int w, int h) {
    Region region = Region.create(x, y, w, h);
    IScreen _screen = region.getScreen();
    ScreenImage simg = _screen.capture();
    int scr_w = simg.w, scr_h = simg.h;
    int max_h = 80; // FIXME: put max_h in UserPreferences
    float scale = (float) max_h / scr_h;
    scr_w *= scale;
    scr_h *= scale;
    BufferedImage screen = new BufferedImage(scr_w, scr_h, BufferedImage.TYPE_INT_RGB);
    Graphics2D screen_g2d = screen.createGraphics();
    try {
      screen_g2d.drawImage(simg.getImage(), 0, 0, scr_w, scr_h, null);
      int sx = (int) ((x - simg.x) * scale), sy = (int) ((y - simg.y) * scale),
              sw = (int) (w * scale), sh = (int) (h * scale);
      screen_g2d.setColor(new Color(255, 0, 0, 150));
      screen_g2d.fillRect(sx, sy, sw, sh);
    } catch (RasterFormatException e) {
      Debug.error(me + "getRegionImage: Problem making image\n%s", e.getMessage());
    }
    screen_g2d.dispose();
    return screen;
  }

  public static EditorRegionButton createFromString(EditorPane parentPane, String str) {
    String[] tokens = str.split("[(),]");
    try {
      int x = Integer.valueOf(tokens[1].trim()), y = Integer.valueOf(tokens[2].trim()),
              w = Integer.valueOf(tokens[3].trim()), h = Integer.valueOf(tokens[4].trim());
      return new EditorRegionButton(parentPane, x, y, w, h);
    } catch (Exception e) {
      Debug.error(me + "createFromString: Problem parsing region expression\n%s", e.getMessage());
    }
    return null;
  }

  @Override
  public String toString() {
    return String.format("Region(%d,%d,%d,%d)", _x, _y, _w, _h);
  }

}
