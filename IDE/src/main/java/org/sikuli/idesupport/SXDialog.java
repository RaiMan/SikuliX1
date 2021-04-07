/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

//import org.sikuli.ide.SikulixIDE;

import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SXDialog extends JFrame {

  static SXDialog instance = null;
  Container pane;

  public SXDialog() {
    destroy();
    globalInit();
    keyListener();
  }

  public void asSingleton() {
    instance = this;
  }

  public static void destroy() {
    if (instance != null) {
      instance.dispose();
      instance = null;
    }
  }

  private void keyListener() {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        instance.setVisible(false);
        keyHandler();
      }
    });
  }

  void keyHandler() {
  }

  Padding margin = null;

  void setMargin(int val) {
    margin = new Padding(val);
  }

  Dimension finalSize = null;

  Dimension getDialogSize() {
    return finalSize;
  }

  public Color SXRED = new Color(0x9D, 0x42, 0x30, 208);

  void globalInit() {
    setResizable(false);
    setUndecorated(true);
    pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(SXRED, 3));
    pane.setLayout(null);
    setMargin(10);
  }

  List<BasicItem> lines = new ArrayList<>();

  void appendY(BasicItem item) {
    lines.add(item);
  }

  void packLines(Container pane, List<BasicItem> items) {
    int nextPos = margin.top;
    int maxW = 0;
    Rectangle bounds;
    for (BasicItem item : items) {
      Component comp = item.create();
      if ( comp != null) {
        bounds = comp.getBounds();
        bounds.y = nextPos + item.getPadding().top;
        bounds.x += margin.left;
        comp.setBounds(bounds);
        item.comp(comp);
        nextPos = bounds.y + bounds.height;
      } else {
        item.setPos(margin.left, nextPos);
        bounds = item.getBounds();
        int height = item.getHeight();
        nextPos += height;
      }
      maxW = Math.max(bounds.x + bounds.width + margin.right, maxW);
    }
    Dimension paneSize = new Dimension(maxW, nextPos + margin.bottom);
    int availableW = paneSize.width - margin.left() - margin.right();
    for (BasicItem item : lines) {
      Component comp = item.comp();
      bounds = item.getBounds();
      int off = 0;
      if (comp != null) {
        bounds = comp.getBounds();
      }
      if (item.isCenter()) {
        off = (availableW - bounds.width) / 2;
      } else if (item.isRight()) {
        off = availableW - bounds.width;
      }
      bounds.x += off;
      if (comp != null) {
        comp.setBounds(bounds);
        pane.add(comp);
      } else {
        item.setBounds(bounds);
        Component vComp = item.make(availableW);
        pane.add(vComp);
      }
    }
    finalSize = paneSize;
  }

  void popup() {
    popup(null);
  }

  void popup(Point where) {
    pack();
    setSize(finalSize);
    if (where != null) {
      setLocation(where);
    }
    setAlwaysOnTop(true);
    setVisible(true);
  }

  class LineItem extends BasicItem {

    LineItem() {}

    LineItem(int len) {
      this.len = len;
    }

    LineItem(int len, int stroke) {
      this.len = len;
      this.stroke = stroke;
    }

    LineItem(int len, Color color) {
      this.len = len;
      this.color = color;
    }

    LineItem(int len, int stroke, Color color) {
      this.len = len;
      this.stroke = stroke;
    }

    LineItem setStroke(int stroke) {
      this.stroke = stroke;
      return this;
    }

    LineItem setColor(Color color) {
      this.color = color;
      return this;
    }

    int len = 0;
    int stroke = 5;
    Color color = SXRED;

    class Line extends JComponent {
      public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(stroke));
        g2.setColor(color);
        g2.draw(new Line2D.Float(0, 0, len, 0));
      }
    }

    int getHeight() {
      return stroke + getPadding().top();
    }

    Rectangle getBounds() {
      return new Rectangle(posX, posY, len, stroke);
    }

    Line make(int w) {
      if (len == 0) {
        len = w;
        bounds.width = w;
      }
      bounds.height = getHeight();
      Line line = new Line();
      line.setBounds(bounds);
      return line;
    }
  }

  class ImageItem extends BasicItem {
    BufferedImage img = null;

    ImageItem(URL url) {
      try {
        img = ImageIO.read(url);
      } catch (IOException e) {
        Commons.error("ImageItem: %s", url);
      }
    }

    public ImageItem resize(int width) {
      return resize(width, 0);
    }

    public ImageItem resize(int width, int height) {
      if (img == null || (width < 1 && height < 1)) {
        return this;
      }
      if (width > 0) {
        return resize((double) width / img.getWidth());
      } else if (height > 0) {
        return resize((double) height / img.getHeight());
      }
      return this;
    }

    public ImageItem resize(double factor) {
      if (img == null || !(factor > 0)) {
        return this;
      }
      RunTime.loadOpenCV();
      img = Commons.resize(img, (float) factor);
      return this;
    }

    JLabel create() {
      JLabel lblimg = new JLabel();
      if (img != null) {
        lblimg.setIcon(new ImageIcon(img));
        int wimg = img.getWidth();
        int himg = img.getHeight();
        lblimg.setBounds(0, 0, wimg, himg);
      }
      return lblimg;
    }
  }

  //region TextItem
  private int stdFontSize = 20;

  void setFontSize(int val) {
    stdFontSize = val;
  }

  int lineSpace = -1;

  void setLineSpace(int val) {
    lineSpace = val;
  }

  class TextItem extends BasicItem {

    String aText = "";

    TextItem(String aText) {
      this.aText = aText;
      if (lineSpace > -1) {
        padT(lineSpace);
      }
    }

    TextItem fontSize(int size) {
      fontSize = size;
      return this;
    }

    int fontSize = stdFontSize;

    JLabel create() {
      JLabel lblText = new JLabel(aText);
      Font titleFont = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
      Rectangle2D textLen = lblText.getFontMetrics(titleFont).getStringBounds(aText, getGraphics());
      if (textLen.getWidth() > maxW) {
        fontSize = (int) (fontSize * maxW / textLen.getWidth());
        titleFont = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
        textLen = lblText.getFontMetrics(titleFont).getStringBounds(aText, getGraphics());
      }
      lblText.setFont(titleFont);
      lblText.setBounds(new Rectangle(0, 0, (int) textLen.getWidth(), (int) textLen.getHeight()));
      return lblText;
    }
  }
  //endregion

  private int maxW = 800;
  private int maxH = 800;

  void setDialogSize(int w, int h) {
    maxW = w;
    maxH = h;
  }

  abstract class BasicItem {

    //region Component
    private Component comp = null;

    void comp(Component comp) {
      this.comp = comp;
    }

    Component comp() {
      return comp;
    }
    //endregion

    //region Padding
    private Padding padding = new Padding(0);

    Padding getPadding() {
      return padding;
    }

    BasicItem padT(int val) {
      padding.top(val);
      return this;
    }
    //endregion

    //region Alignment
    ALIGN alignment = ALIGN.LEFT;

    public BasicItem align(ALIGN type) {
      alignment = type;
      return this;
    }

    public boolean isCenter() {
      return alignment.equals(ALIGN.CENTER) || stdAlign.equals(ALIGN.CENTER);
    }

    public boolean isLeft() {
      return alignment.equals(ALIGN.LEFT);
    }

    public boolean isRight() {
      return alignment.equals(ALIGN.RIGHT);
    }
    //endregion

    Component create() {
      return null;
    }

    Component make(int w) {
      return null;
    }

    BasicItem resize(int width) {
      return this;
    }

    int getHeight() {
      return 0;
    }

    int posX = 0;
    int posY = 0;

    void setPos(int posX, int posY) {
      this.posX = posX;
      this.posY = posY + getPadding().top;
    }

    Rectangle bounds = null;

    void setBounds(Rectangle bounds) {
      this.bounds = bounds;
    }

    Rectangle getBounds() {
      return new Rectangle();
    }
  }

  enum ALIGN {LEFT, CENTER, RIGHT}

  ALIGN stdAlign = ALIGN.LEFT;

  void setAlign(ALIGN type) {
    stdAlign = type;
  }

  class Padding {

    private int left = 0;
    private int top = 0;
    private int bottom = 0;
    private int right = 0;

    Padding(int all) {
      top(all);
      bottom(all);
      left(all);
      right(all);
    }

    Padding(int top, int bottom, int left, int right) {
      top(top);
      bottom(bottom);
      left(left);
      right(right);
    }

    public void left(int left) {
      this.left = left;
    }

    public void top(int top) {
      this.top = top;
    }

    public void bottom(int bottom) {
      this.bottom = bottom;
    }

    public void right(int right) {
      this.right = right;
    }

    public int left() {
      return left;
    }

    public int top() {
      return top;
    }

    public int bottom() {
      return bottom;
    }

    public int right() {
      return right;
    }
  }
}
