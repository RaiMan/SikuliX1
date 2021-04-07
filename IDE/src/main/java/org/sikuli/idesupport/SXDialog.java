/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SXDialog extends JFrame {

  static SXDialog singleton = null;
  SXDialog dialog = null;
  Container pane;

  public SXDialog() {
    destroy();
    dialog = this;
    globalInit();
    keyListener();
  }

  //region 04 global handler
  private void keyListener() {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        singleton.setVisible(false);
      }
    });
  }
  //endregion

  //region 05 global features
  public Color SXRED = new Color(0x9D, 0x42, 0x30, 208);
  public Color SXLBLBUTTON = new Color(241, 230, 206);
  public Color SXLBLSELECTED = new Color(167, 192, 220);

  public boolean asSingleton() {
    if (singleton == null) {
      singleton = this;
      return true;
    }
    return false;
  }

  void globalInit() {
    setResizable(false);
    setUndecorated(true);
    pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(SXRED, 3));
    pane.setLayout(null);
    setMargin(10);
  }

  Dimension finalSize = null;

  private int maxW = 800;
  private int maxH = 800;

  void setDialogSize(int w, int h) {
    maxW = w;
    maxH = h;
  }

  Dimension getDialogSize() {
    return new Dimension(maxW, maxH);
  }

  Padding margin = null;

  void setMargin(int val) {
    margin = new Padding(val);
  }

  int spaceBefore = 20;

  void setSpaceBefore(int val) {
    spaceBefore = val;
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

  enum ALIGN {LEFT, CENTER, RIGHT}

  ALIGN stdAlign = ALIGN.LEFT;

  void setAlign(ALIGN type) {
    stdAlign = type;
  }

  class ClickAction implements Runnable {
    @Override
    public void run() {
      close();
    }
  }
  //endregion

  //region 10 show/hide/destroy
  void popup() {
    popup(null);
  }

  void popup(Point where) {
    setSize(finalSize);
    if (where != null) {
      setLocation(where);
    }
    setAlwaysOnTop(true);
    setVisible(true);
  }
  public static void destroy() {
    if (singleton != null) {
      singleton.dispose();
      singleton = null;
    }
  }

  void close() {
    dialog.setVisible(false);
  }
  //endregion

  //region 20 top-down line items
  List<BasicItem> lines = new ArrayList<>();

  void appendY(BasicItem item) {
    if (item.getPadding().top() == 0)
      item.padT(spaceBefore);
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
  //endregion

  //region 50 BasicItem
  abstract class BasicItem {

    //region Component
    private Component comp = null;

    void comp(Component comp) {
      this.comp = comp;
    }

    Component comp() {
      return comp;
    }

    Component create() {
      return null;
    }

    Component make(int w) {
      return null;
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
      if (comp == null && len == 0) {
        return false;
      }
      return alignment.equals(ALIGN.CENTER) || stdAlign.equals(ALIGN.CENTER);
    }

    public boolean isLeft() {
      return alignment.equals(ALIGN.LEFT);
    }

    public boolean isRight() {
      return alignment.equals(ALIGN.RIGHT);
    }
    //endregion

    //region Location size
    BasicItem resize(int width) {
      return this;
    }

    int len = 0;

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
    //endregion

    //region Listener
    private boolean active = false;

    BasicItem setActive() {
      active = true;
      return this;
    }

    void addListeners(Component comp) {
      if (active) {
        mouseListener(comp);
        if (comp instanceof JLabel) {
          ((JLabel) comp).setOpaque(true);
          comp.setBackground(SXLBLBUTTON);
        }
      }
    }

    void mouseListener(Component comp) {
      comp.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          clicked();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          comp.setBackground(SXLBLSELECTED);
        }

        @Override
        public void mouseExited(MouseEvent e) {
          comp.setBackground(SXLBLBUTTON);
        }
      });
    }

    ClickAction clickAction = new ClickAction();

    void clicked() {
      clickAction.run();
    }

    BasicItem setClickAction(ClickAction action) {
      clickAction = action;
      return this;
    }
    //endregion

    //region Decoration
    boolean underline = false;

    BasicItem underline() {
      underline = true;
      return this;
    }
    //endregion

  }
  //endregion

  //region 51 LineItem
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
  //endregion

  //region 52 TextItem
  private int stdFontSize = 20;

  void setFontSize(int val) {
    stdFontSize = val;
  }

  class TextItem extends BasicItem {

    String aText = "";

    TextItem() {
    }

    TextItem(String aText) {
      this.aText = aText;
    }

    TextItem fontSize(int size) {
      fontSize = size;
      return this;
    }

    int fontSize = stdFontSize;

    TextItem bold() {
      fontBold = Font.BOLD;
      return this;
    }
    int fontBold = 0;

    JLabel create() {
      JLabel lblText = new JLabel(aText);
      Font titleFont = new Font(Font.MONOSPACED, fontBold, fontSize);
      Rectangle2D textLen = lblText.getFontMetrics(titleFont).getStringBounds(aText, getGraphics());
      if (textLen.getWidth() > maxW) {
        fontSize = (int) (fontSize * maxW / textLen.getWidth());
        titleFont = new Font(Font.MONOSPACED, fontBold, fontSize);
        textLen = lblText.getFontMetrics(titleFont).getStringBounds(aText, getGraphics());
      }
      if (underline) {
        lblText = new UnderlinedLabel(aText);
      }
      lblText.setFont(titleFont);
      lblText.setBounds(new Rectangle(0, 0, (int) textLen.getWidth(), (int) textLen.getHeight()));
      addListeners(lblText);
      return lblText;
    }

    class UnderlinedLabel extends JLabel {
      public UnderlinedLabel(String text) {
        super(text);
      }

      public void paint(Graphics g) {
        Rectangle r;
        super.paint(g);
        r = g.getClipBounds();
        int height = r.height - getFontMetrics(getFont()).getDescent() + 3;
        int width = getFontMetrics(getFont()).stringWidth(getText());
        g.drawLine(0, height, width, height);
      }
    }
  }
  //endregion

  //region 521 Linkitem
  class LinkItem extends TextItem {
    String aLink = "https://sikulix.github.io";

    ClickAction clickAction = new ClickAction() {
      @Override
      public void run() {
        close();
        Commons.browse(aLink);
      }
    };

    LinkItem(String text, String link) {
      aText = text;
      aLink = link;
      underline();
      setActive();
      super.clickAction = clickAction;
    }
  }
  //endregion

  //region 53 ImageItem
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
  //endregion
}
