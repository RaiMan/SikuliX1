/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.util;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.awt.image.RescaleOp;
import javax.swing.JFrame;
import org.sikuli.basics.Debug;
import org.sikuli.script.IScreen;
import org.sikuli.script.Location;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

/**
 * INTERNAL USE implements the screen overlay used with the capture feature
 */
public class OverlayCapturePrompt extends JFrame  implements EventSubject {

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
  private EventObserver captureObserver = null;
  private IScreen scrOCP;
  private BufferedImage scr_img = null;
  private BufferedImage scr_img_darker = null;
  private BufferedImage bi = null;
  private float darker_factor;
  private Rectangle rectSelection;
  private int srcScreenId = -1;
  private Location srcScreenLocation = null;
  private Location destScreenLocation = null;
  private int srcx, srcy, destx, desty;
  private boolean canceled = false;
  private String promptMsg = "";
  private boolean dragging = false;
  private boolean hasFinished = false;
  private boolean hasStarted = false;
  private boolean mouseMoves = false;
  private int scr_img_type = BufferedImage.TYPE_INT_RGB;
  private double scr_img_scale = 1;
  private Rectangle scr_img_rect = null;
  private ScreenImage scr_img_original = null;

  private boolean isLocalScreen = true;

//  private JPanel _panel = null;
//  private Graphics2D _currG2D = null;

  public OverlayCapturePrompt(IScreen scr) {
//    super();
    Debug.log(3, "TRACE: OverlayCapturePrompt: init: S(%d)", scr.getID());
    scrOCP = scr;
    canceled = false;

    setUndecorated(true);
    setAlwaysOnTop(true);

    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    rectSelection = new Rectangle();

    if (scr.isOtherScreen()) {
      isLocalScreen = false;
    }

//    _panel = new javax.swing.JPanel() {
//      @Override
//      protected void paintComponent(Graphics g) {
//        if (g instanceof Graphics2D) {
//          Graphics2D g2d = (Graphics2D) g;
//          _currG2D = g2d;
//        } else {
//          super.paintComponent(g);
//        }
//      }
//    };
//    _panel.setLayout(null);
//    add(_panel);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(java.awt.event.MouseEvent e) {
        if (scr_img == null) {
          return;
        }
        if (e.getButton() != java.awt.event.MouseEvent.BUTTON1) {
          return;
        }
        hasStarted = true;
        destx = srcx = e.getX();
        desty = srcy = e.getY();
        if (isLocalScreen) {
          srcScreenId = scrOCP.getIdFromPoint(srcx, srcy);
          srcScreenLocation = new Location(srcx + scrOCP.getX(), srcy + scrOCP.getY());
          Debug.log(3, "CapturePrompt: started at (%d,%d) as %s on %d", srcx, srcy,
                  srcScreenLocation.toStringShort(), srcScreenId);
        }
        promptMsg = null;
        repaint();
      }

      @Override
      public void mouseReleased(java.awt.event.MouseEvent e) {
        if (scr_img == null) {
          return;
        }
        if (e.getButton() != java.awt.event.MouseEvent.BUTTON1) {
          canceled = true;
          Debug.log(3, "CapturePrompt: aborted: not using left mouse button");
        } else {
          if (isLocalScreen) {
            destScreenLocation = new Location(destx + scrOCP.getX(), desty + scrOCP.getY());
            Debug.log(3, "CapturePrompt: finished at (%d,%d) as %s on %d", destx, desty,
              destScreenLocation.toStringShort(), srcScreenId);
          }
        }
        hasFinished = true;
        setVisible(false);
        notifyObserver();
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(java.awt.event.MouseEvent e) {
        if (promptMsg == null) {
          return;
        }
        if (!mouseMoves) {
          mouseMoves = true;
          return;
        }
        promptMsg = null;
        repaint();
      }

      @Override
      public void mouseDragged(java.awt.event.MouseEvent e) {
        if (!hasStarted || scr_img == null) {
          return;
        }
        if (!dragging) {
          if (promptMsg != null) {
            Screen.closePrompt((Screen) scrOCP);
          }
          dragging = true;
        }
        destx = e.getX();
        desty = e.getY();
        repaint();
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

  public int getScrID() {
    return srcScreenId;
  }

  public void close() {
    Debug.log(4, "CapturePrompt.close: S(%d) freeing resources", scrOCP.getID());
    setVisible(false);
    dispose();
    scr_img = null;
    scr_img_darker = null;
    bi = null;
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
    prompt(null);
  }

  public void prompt(String msg) {
    scr_img_original = scrOCP.capture();
    scr_img = scr_img_original.getImage();
    scr_img_darker = scr_img;
    scr_img_type = scr_img.getType();
    scr_img_rect = new Rectangle(scrOCP.getBounds());
    promptMsg = msg;
    if (isLocalScreen) {
      darker_factor = 0.6f;
      RescaleOp op = new RescaleOp(darker_factor, 0, null);
      scr_img_darker = op.filter(scr_img, null);
    } else {
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
    }
    this.setBounds(scr_img_rect);
    this.setVisible(true);
  }

  public boolean isComplete() {
    return hasFinished;
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

  public ScreenImage getSelection() {
    if (canceled) {
      return null;
    }
    BufferedImage cropImg = cropSelection();
    if (cropImg == null) {
      return null;
    }
    rectSelection.x += scrOCP.getX();
    rectSelection.y += scrOCP.getY();
    ScreenImage ret = new ScreenImage(rectSelection, cropImg);
    return ret;
  }

  private BufferedImage cropSelection() {
    int w = rectSelection.width, h = rectSelection.height;
    if (w <= 0 || h <= 0) {
      return null;
    }
    int x = rectSelection.x;
    int y = rectSelection.y;
    if (!isLocalScreen && scr_img_scale != 1) {
      x = (int) (x / scr_img_scale);
      y = (int) (y / scr_img_scale);
      w = (int) (w / scr_img_scale);
      h = (int) (h / scr_img_scale);

    }
    BufferedImage crop = new BufferedImage(w, h, scr_img_type);
    Graphics2D crop_g2d = crop.createGraphics();
    try {
      crop_g2d.drawImage(scr_img_original.getImage().getSubimage(x, y, w, h), null, 0, 0);
    } catch (RasterFormatException e) {
      Debug.error("OverlayCapturePrompt: cropSelection: RasterFormatException", e.getMessage());
    }
    crop_g2d.dispose();
    return crop;
  }

  void drawMessage(Graphics2D g2d) {
    if (promptMsg == null) {
      return;
    }
    g2d.setFont(fontMsg);
    g2d.setColor(new Color(1f, 1f, 1f, 1));
    int sw = g2d.getFontMetrics().stringWidth(promptMsg);
    int sh = g2d.getFontMetrics().getMaxAscent();
    Rectangle ubound = scrOCP.getBounds();
    for (int i = 0; i < Screen.getNumberScreens(); i++) {
      if (!Screen.getScreen(i).hasPrompt()) {
        continue;
      }
      Rectangle bound = Screen.getBounds(i);
      int cx = bound.x + (bound.width - sw) / 2 - ubound.x;
      int cy = bound.y + (bound.height - sh) / 2 - ubound.y;
      g2d.drawString(promptMsg, cx, cy);
    }
  }

  private void drawSelection(Graphics2D g2d) {
    if (srcx != destx || srcy != desty) {
      int x1 = (srcx < destx) ? srcx : destx;
      int y1 = (srcy < desty) ? srcy : desty;
      int x2 = (srcx > destx) ? srcx : destx;
      int y2 = (srcy > desty) ? srcy : desty;

      rectSelection.x = x1;
      rectSelection.y = y1;
      rectSelection.width = (x2 - x1) + 1;
      rectSelection.height = (y2 - y1) + 1;
      if (rectSelection.width > 0 && rectSelection.height > 0) {
        g2d.drawImage(scr_img.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1),
                null, x1, y1);
      }

      g2d.setColor(selFrameColor);
      g2d.setStroke(bs);
      g2d.draw(rectSelection);
      int cx = (x1 + x2) / 2;
      int cy = (y1 + y2) / 2;
      g2d.setColor(selCrossColor);
      g2d.setStroke(_StrokeCross);
      g2d.drawLine(cx, y1, cx, y2);
      g2d.drawLine(x1, cy, x2, cy);

      if (isLocalScreen && Screen.getNumberScreens() > 1) {
        drawScreenFrame(g2d, srcScreenId);
      }
    }
  }

  private void drawScreenFrame(Graphics2D g2d, int scrId) {
    if (!isLocalScreen) {
      return;
    }
    g2d.setColor(screenFrameColor);
    g2d.setStroke(strokeScreenFrame);
    if (screenFrame == null) {
      screenFrame = Screen.getBounds(scrId);
      Rectangle ubound = scrOCP.getBounds();
      screenFrame.x -= ubound.x;
      screenFrame.y -= ubound.y;
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
        bi = new BufferedImage(scr_img_rect.width, scr_img_rect.height, scr_img_type);
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
