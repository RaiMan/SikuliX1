/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import org.sikuli.basics.Debug;
import org.sikuli.support.devices.ScreenDevice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.awt.image.RescaleOp;

/**
 * INTERNAL USE implements the screen overlay used with the capture feature
 */
public class OverlayCapturePrompt extends JFrame implements EventSubject {

  final static float MIN_DARKER_FACTOR = 0.6f;
  final static long MSG_DISPLAY_TIME = 2000;
  final static long WIN_FADE_IN_TIME = 200;
  static final Font fontMsg = new Font("Arial", Font.PLAIN, 60);
  static final Color selFrameColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
  static final Color selCrossColor = new Color(1.0f, 0.0f, 0.0f, 0.6f);
  static final Color screenFrameColor = new Color(1.0f, 0.0f, 0.0f, 0.6f);
  private Rectangle screenFrame = null;
  static final BasicStroke strokeScreenFrame = new BasicStroke(5);
  static final BasicStroke _StrokeCross = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{2f}, 0);
  static final BasicStroke bs = new BasicStroke(1);

  private BufferedImage scr_img_original = null;

  public BufferedImage getOriginal() {
    return scr_img_original;
  }

  private BufferedImage scr_img = null;
  private BufferedImage scr_img_darker = null;
  private BufferedImage bi = null;
  private float darker_factor;

  private boolean canceled = false;

  public boolean isCanceled() {
    return canceled;
  }

  private boolean hasFinished = false;

  private boolean dragging = false;
  private boolean hasStarted = false;
  private boolean mouseMoves = false;

  private int scr_img_type = BufferedImage.TYPE_INT_RGB;
  private double scr_img_scale = 1;
  private Rectangle scr_img_rect = null;

  private boolean isLocalScreen = true;

  private ScreenDevice screen;

  private Rectangle rectSelected = new Rectangle();
  private int startX, startY, endX, endY;
  private int endMinX, endMaxX, endMinY, endMaxY;

  private EventObserver captureObserver;
  private String message = "";

  public static boolean capturePrompt(EventObserver observer, String message) {
    if (ScreenDevice.capturePromptActive()) {
      return false;
    }
    ScreenDevice[] screenDevices = ScreenDevice.get();
    for (ScreenDevice screen : screenDevices) {
      final OverlayCapturePrompt prompt = new OverlayCapturePrompt(screen, observer, message);
      screen.setPrompt(prompt);
      prompt.setPromptImage();
    }
    for (ScreenDevice screen : screenDevices) {
      screen.showPrompt();
    }
    return true;
  }

  private OverlayCapturePrompt(ScreenDevice screen, EventObserver observer, String message) {
    this.screen = screen;
    this.message = message;
    captureObserver = observer;
    init();
  }

  private void init() {
    setUndecorated(true);
    setAlwaysOnTop(true);

    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        mouseDown(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        mouseUp(e);
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        mouseMove(e);
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        mouseDrag(e);
      }
    });

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          hasFinished = canceled = true;
          Debug.log(3, "CapturePrompt: aborted using key ESC");
          setVisible(false);
          notifyObserver();
        }
      }
    });
  }

  private void mouseDown(MouseEvent event) {
    if (scr_img == null) {
      return;
    }
    if (event.getButton() != java.awt.event.MouseEvent.BUTTON1) {
      return;
    }
    hasStarted = true;
    endX = startX = event.getX();
    endY = startY = event.getY();
    if (isLocalScreen) {
      Debug.log(3, "CapturePrompt: started at (%d,%d) on S(%d)",
          screen.x() + startX, screen.y() + startY, screen.getScreenId());
      endMinX = 0;
      endMaxX = screen.width() - 1;
      endMinY = 0;
      endMaxY = screen.height() - 1;
    }
    message = null;
    repaint();
  }

  private void mouseUp(MouseEvent event) {
    if (scr_img == null) {
      return;
    }
    if (event.getButton() != java.awt.event.MouseEvent.BUTTON1) {
      canceled = true;
      Debug.log(3, "CapturePrompt: aborted: not using left mouse button");
    } else {
      if (isLocalScreen) {
        Debug.log(3, "CapturePrompt: finished at (%d,%d) on S(%d)",
            screen.x() + endX, screen.y() + endY, screen.getScreenId());
      }
    }
    hasFinished = true;
    setVisible(false);
    notifyObserver();
  }

  private void mouseMove(MouseEvent event) {
    if (message == null) {
      return;
    }
    if (!mouseMoves) {
      mouseMoves = true;
      return;
    }
    message = null;
    repaint();
  }

  private void mouseDrag(MouseEvent event) {
    if (!hasStarted || scr_img == null) {
      return;
    }
    if (!dragging) {
      ScreenDevice.closeOtherCapturePrompts(screen);
      dragging = true;
    }
    endX = event.getX();
    endY = event.getY();
    repaint();
  }

  @Override
  public void addObserver(EventObserver obs) {
    Debug.log(3, "TRACE: OverlayCapturePrompt: addObserver: %s", obs != null);
    captureObserver = obs;
  }

  @Override
  public void notifyObserver() {
    Debug.log(3, "TRACE: OverlayCapturePrompt: notifyObserver: %s", captureObserver != null);
    if (null != captureObserver) {
      captureObserver.update(this);
    }
  }

  public int getScrID() {
    return screen.getScreenId();
  }

  public void prompt(String msg, int delayMS) {
    try {
      Thread.sleep(delayMS);
    } catch (InterruptedException ie) {
    }
    prompt(msg);
  }

  public void prompt(int delayMS) {
    prompt(null, delayMS);
  }

  public void prompt() {
    prompt(message);
  }

  void setPromptImage() {
    scr_img_original = screen.capture();
    if (Debug.getDebugLevel() > 2) {
      //TODO scr_img_original.getFile(Commons.getAppDataStore().getAbsolutePath(), "lastScreenShot");
    }
    scr_img = scr_img_original;
    scr_img_type = scr_img.getType();
    scr_img_rect = screen.asRectangle();
    darker_factor = 0.6f;
    RescaleOp op = new RescaleOp(darker_factor, 0, null);
    scr_img_darker = op.filter(scr_img, null);
  }

  public void prompt(String msg) {
//TODO nonLocal device
/*
      promptMsg = null;
      if (scr_img_rect.height > Screen.getPrimaryScreen().getBounds().getHeight()) {
        scr_img_scale = Screen.getPrimaryScreen().getBounds().getHeight() / scr_img_rect.height;
      }
      if (scr_img_rect.width > Screen.getPrimaryScreen().getBounds().getWidth()) {
        scr_img_scale = Math.min(Screen.getPrimaryScreen().getBounds().getWidth() / scr_img_rect.width, scr_img_scale);
      }
      if (1 != scr_img_scale) {
        scr_img_rect.width = (int) (scr_img_rect.width * scr_img_scale);
        scr_img_rect.height = (int) (scr_img_rect.height * scr_img_scale);
        Image tmp = scr_img.getScaledInstance(scr_img_rect.width, scr_img_rect.height, Image.SCALE_SMOOTH);
        scr_img = new BufferedImage(scr_img_rect.width, scr_img_rect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scr_img.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        scr_img_darker = scr_img;
      }
*/
    this.setBounds(screen.asRectangle());
    this.setVisible(true);
  }

  public void close() {
    Debug.log(4, "CapturePrompt.close: S(%d) freeing resources", screen.getScreenId());
    setVisible(false);
    dispose();
    scr_img = null;
    scr_img_darker = null;
    bi = null;
  }

  public boolean isComplete() {
    return hasFinished;
  }

  public Point getStart() {
    return new Point(startX, startY);
  }

  public Point getEnd() {
    return new Point(endX, endY);
  }

  public BufferedImage getSelectionImage() {
    if (canceled) {
      return null;
    }
    BufferedImage cropImg = cropSelection();
    if (cropImg == null) {
      return null;
    }
    rectSelected.x += screen.x();
    rectSelected.y += screen.y();
    return cropImg;
  }

  public Rectangle getSelectionRectangle() {
    return rectSelected;
  }

  private BufferedImage cropSelection() {
    int w = rectSelected.width, h = rectSelected.height;
    if (w <= 0 || h <= 0) {
      return null;
    }
    int x = rectSelected.x;
    int y = rectSelected.y;
    if (!isLocalScreen && scr_img_scale != 1) {
      x = (int) (x / scr_img_scale);
      y = (int) (y / scr_img_scale);
      w = (int) (w / scr_img_scale);
      h = (int) (h / scr_img_scale);
    }
    BufferedImage crop = new BufferedImage(w, h, scr_img_type);
    Graphics2D crop_g2d = crop.createGraphics();
    try {
      crop_g2d.drawImage(getOriginal().getSubimage(x, y, w, h), null, 0, 0);
    } catch (RasterFormatException e) {
      Debug.error("OverlayCapturePrompt: cropSelection: RasterFormatException", e.getMessage());
    }
    crop_g2d.dispose();
    return crop;
  }

  void drawMessage(Graphics2D g2d) {
    if (message == null) {
      return;
    }
    g2d.setFont(fontMsg);
    g2d.setColor(new Color(1f, 1f, 1f, 1));
    int sw = g2d.getFontMetrics().stringWidth(message);
    int sh = g2d.getFontMetrics().getMaxAscent();
    Rectangle ubound = screen.asRectangle();
    for (ScreenDevice screen : ScreenDevice.get()) {
      if (screen.hasPrompt()) {
        Rectangle bound = screen.asRectangle();
        int cx = bound.x + (bound.width - sw) / 2 - ubound.x;
        int cy = bound.y + (bound.height - sh) / 2 - ubound.y;
        g2d.drawString(message, cx, cy);
      }
    }
  }

  private void drawSelection(Graphics2D g2d) {
    if (startX != endX || startY != endY) {
      if (endX < endMinX) {
        endX = endMinX;
      } else if (endX > endMaxX) {
        endX = endMaxX;
      }
      if (endY < endMinY) {
        endY = endMinY;
      } else if (endY > endMaxY) {
        endY = endMaxY;
      }
      rectSelected.x = Math.min(startX, endX);
      rectSelected.y = Math.min(startY, endY);
      int xEnd = Math.max(startX, endX);
      int yEnd = Math.max(startY, endY);

      rectSelected.width = (xEnd - rectSelected.x) + 1;
      rectSelected.height = (yEnd - rectSelected.y) + 1;
      if (rectSelected.width > 0 && rectSelected.height > 0) {
        g2d.drawImage(scr_img.getSubimage(rectSelected.x, rectSelected.y, rectSelected.width, rectSelected.height),
            null, rectSelected.x, rectSelected.y);
      }

      g2d.setColor(selFrameColor);
      g2d.setStroke(bs);
      g2d.draw(rectSelected);

      int cx = (rectSelected.x + xEnd) / 2;
      int cy = (rectSelected.y + yEnd) / 2;
      g2d.setColor(selCrossColor);
      g2d.setStroke(_StrokeCross);
      g2d.drawLine(cx, rectSelected.y, cx, yEnd);
      g2d.drawLine(rectSelected.x, cy, xEnd, cy);

      if (isLocalScreen && ScreenDevice.numDevices() > 1) {
        drawScreenFrame(g2d);
      }
    }
  }

  private void drawScreenFrame(Graphics2D g2d) {
    g2d.setColor(screenFrameColor);
    g2d.setStroke(strokeScreenFrame);
    if (screenFrame == null) {
      screenFrame = new Rectangle(0, 0, screen.width(), screen.height());
      int sw = (int) (strokeScreenFrame.getLineWidth() / 2);
      screenFrame.x += sw;
      screenFrame.y += sw;
      screenFrame.width -= sw * 2;
      screenFrame.height -= sw * 2;
    }
    g2d.draw(screenFrame);
  }

  @Override
  public void paint(Graphics g) {
    if (scr_img != null) {
      Graphics2D g2dWin = (Graphics2D) g;
      if (bi == null) {
        bi = new BufferedImage(screen.width(), screen.height(), scr_img_type);
      }
      Graphics2D bfG2 = bi.createGraphics();
      bfG2.drawImage(scr_img_darker, 0, 0, this);
      drawMessage(bfG2);
      drawSelection(bfG2);
      g2dWin.drawImage(bi, 0, 0, this);
      setVisible(true);
    } else {
      setVisible(false);
    }
  }
}
