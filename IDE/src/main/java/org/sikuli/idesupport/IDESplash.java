/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IDESplash extends JFrame {

  static IDESplash instance = null;

  public IDESplash(Object[] ideWindow) {
    destroy();
    instance = this;
    keyListener();
    initForIDE(ideWindow);
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
        SikulixIDE.get().setVisible(true);
      }
    });
  }

  private Dimension size = new Dimension(500, 400);

  final String titleText = String.format("---  SikuliX-IDE  ---  %s  ---  starting on Java %s  ---",
          Commons.getSXVersion(), Commons.getJavaVersion());

  Padding margin = new Padding(10);

  void initForIDE(Object[] ideWindow) {
    size = (Dimension) ideWindow[0];
    setResizable(false);
    setUndecorated(true);
    Container pane = getContentPane();
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(new Color(0x9D, 0x42, 0x30, 208), 3));
    pane.setLayout(null);

    URL image = SikulixIDE.class.getResource("/icons/sikulix-red-x.png");
    appendY(new ImageItem(image).align(ALIGN.CENTER));
    appendY(new TextItem(titleText).padT(50));
    appendY(new TextItem(titleText).fontSize(16).padT(100).align(ALIGN.CENTER));
    appendY(new ImageItem(image).align(ALIGN.CENTER).padT(50).resize(200));

    Dimension finalSize = packLines(pane, lines);

    pack();
    setSize(finalSize);
    setLocation((Point) ideWindow[1]);

    setAlwaysOnTop(true);
    setVisible(true);
  }

  List<BasicItem> lines = new ArrayList<>();

  private void appendY(BasicItem item) {
    lines.add(item);
  }

  Dimension packLines(Container pane, List<BasicItem> items) {
    int nextPos = margin.top;
    int maxW = 0;
    for (BasicItem item : items) {
      Component comp = item.create();
      Rectangle bounds = comp.getBounds();
      bounds.y = nextPos + item.getPadding().top;
      bounds.x += margin.left;
      comp.setBounds(bounds);
      item.comp(comp);
      maxW = Math.max(bounds.x + bounds.width + margin.right, maxW);
      nextPos = bounds.y + bounds.height;
    }
    Dimension paneSize = new Dimension(maxW, nextPos + margin.bottom);
    for (BasicItem item : lines) {
      Component comp = item.comp();
      if (item.isCenter()) {
        Rectangle bounds = comp.getBounds();
        int off = (paneSize.width - margin.left() - margin.right() - bounds.width) / 2;
        bounds.x += off;
        comp.setBounds(bounds);
      } else if (item.isRight()) {
        Rectangle bounds = comp.getBounds();
        int off = paneSize.width - margin.left() - margin.right() - bounds.width;
        bounds.x += off;
        comp.setBounds(bounds);
      }
      pane.add(comp);
    }
    return paneSize;
  }

  private class ImageItem extends BasicItem {
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

  private int stdFontSize = 20;
  private int maxWidth = 800;

  private class TextItem extends BasicItem {

    String aText = "";

    TextItem(String aText) {
      this.aText = aText;
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
      if (textLen.getWidth() > maxWidth) {
        fontSize = (int) (fontSize * maxWidth / textLen.getWidth());
        titleFont = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
        textLen = lblText.getFontMetrics(titleFont).getStringBounds(aText, getGraphics());
      }
      lblText.setFont(titleFont);
      lblText.setBounds(new Rectangle(0, 0, (int) textLen.getWidth(), (int) textLen.getHeight()));
      return lblText;
    }
  }

  private abstract class BasicItem {

    private Component comp = null;

    void comp(Component comp) {
      this.comp = comp;
    }

    Component comp() {
      return comp;
    }

    private Padding padding = new Padding(0);

    Padding getPadding() {
      return padding;
    }

    BasicItem padT(int val) {
      padding.top(val);
      return this;
    }

    ALIGN alignment = ALIGN.LEFT;

    public BasicItem align(ALIGN type) {
      alignment = type;
      return this;
    }

    public boolean isCenter() {
      return alignment.equals(ALIGN.CENTER);
    }

    public boolean isLeft() {
      return alignment.equals(ALIGN.LEFT);
    }

    public boolean isRight() {
      return alignment.equals(ALIGN.RIGHT);
    }

    abstract Component create();

    BasicItem resize(int width) {
      return this;
    }
  }

  enum ALIGN {LEFT, CENTER, RIGHT}

  private class Padding {

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
