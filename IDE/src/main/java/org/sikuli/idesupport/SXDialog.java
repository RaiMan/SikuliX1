/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.idesupport;

//import org.sikuli.ide.SikulixIDE;

import org.sikuli.ide.SikulixIDE;
import org.sikuli.script.support.Commons;

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

  enum POSITION {CENTERED, TOPLEFT}

  Container pane;

  public SXDialog() {
    globalInit();
    keyListener();
  }

  public SXDialog(String res, Point where) {
    this(res, where, POSITION.TOPLEFT);
  }

  public SXDialog(String res, Point where, POSITION pos) {
    this();
    textToItems(Commons.copyResourceToString(res, SXDialog.class));
    packLines(pane, lines);
    if (pos.equals(POSITION.CENTERED)) {
      where.x -= finalSize.width / 2;
    }
    popup(where);
  }

  //region 04 global handler
  void keyListener() {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        setVisible(false);
      }
    });
  }

  void addListeners(BasicItem item) {
    if (item.active) {
      item.mouseListener(item);
      if (item.comp() instanceof JLabel) {
        if (item instanceof TextItem) {
          ((JLabel) item.comp).setOpaque(true);
          item.comp.setBackground(SXLBLBUTTON);
        }
      }
    }
  }
  //endregion

  //region 05 global features
  public Color SXRED = new Color(0x9D, 0x42, 0x30, 208);
  public Color SXLBLBUTTON = new Color(241, 230, 206);
  public Color SXLBLSELECTED = new Color(167, 192, 220);

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

  static Point getIDEWindowTop() {
    return SikulixIDE.getWindowTop();
  }
  //endregion

  //region 10 show/hide/destroy
  void popup() {
    popup(null);
  }

  void popup(Point where) {
    pane.setBackground(Color.WHITE);
    setSize(finalSize);
    if (where != null) {
      setLocation(where);
    }
    setAlwaysOnTop(true);
    setVisible(true);
  }

  void close() {
    setVisible(false);
  }
  //endregion

  //region 20 top-down line items
  void textToItems(String text) {
    String[] lines = text.split("\n");
    boolean first = true;
    for (String line : lines) {
      line = line.strip();
      if (line.isEmpty()) {
        continue;
      }
      String[] options = line.split(";");
      if (first && line.startsWith("#global")) {
        if (options.length > 1) {
          Commons.debug("--- options");
          for (String option : options) {
            if (option.startsWith("#")) {
              continue;
            }
            applyOption(option);
          }
          Commons.debug("---");
        }
        first = false;
        continue;
      } else {
        first = false;
      }
      Commons.debug(line);
      if (text.contains("{")) {
        line = replaceVariables(line);
        options = line.split(";");
      }
      if (line.equals("---")) {
        appendY(new LineItem());
        continue;
      }
      if (line.startsWith("#")) {
        BasicItem item = null;
        int start = 2;
        if (line.startsWith("#link")) {
          String[] parts = options[1].split("\\|");
          String url = parts[0].strip();
          String urlText = url;
          if (parts.length > 1) {
            urlText = parts[0].strip();
            url = parts[1].strip();
          }
          item = new LinkItem(urlText, url);
        } else if (line.startsWith("#image")) {
          item = new ImageItem(this.getClass().getResource(options[1].strip()));
        } else if (line.startsWith("#close")) {
          item = new TextItem(options[1]);
          item.setActive();
        } else {
          Commons.error("SXDialog: unknown feature %s", line);
          continue;
        }
        applyOptions(item, options, start);
        appendY(item);
        continue;
      }
      if (options.length > 1) {
        TextItem item = new TextItem(options[0]);
        applyOptions(item, options, 1);
        appendY(item);
      } else {
        appendY(new TextItem(line));
      }
    }
  }

  String replaceVariables(String text) {
    while (text.contains("{")) {
      int start = text.indexOf("{");
      int end = text.indexOf("}");
      if (start > -1 && end > start) {
        String before = text.substring(0, start);
        String after = text.substring(end + 1);
        int len = end - start;
        String var = text.substring(start + 1, start + len);
        text = before + getVariable(var) + after;
      }
    }
    return text;
  }

  String getVariable(String var) {
    if (var.equals("sxversion")) {
      return Commons.getSXVersion();
    }
    if (var.equals("javaversion")) {
      return "" + Commons.getJavaVersion();
    }
    return "";
  }

  void applyOption(String option) {
    option = option.strip();
    String[] parms = option.split(" ");
    String feature = parms[0].toLowerCase();
    if (feature.contains("size") && parms.length > 2) {
      setDialogSize(Integer.parseInt(parms[1]), Integer.parseInt(parms[2]));
    } else if (feature.contains("margin") && parms.length > 1) {
      setMargin(Integer.parseInt(parms[1]));
    } else if (feature.contains("center")) {
      setAlign(ALIGN.CENTER);
    } else if (feature.contains("fontsize")) {
      setFontSize(Integer.parseInt(parms[1]));
    } else if (feature.contains("before")) {
      setSpaceBefore(Integer.parseInt(parms[1]));
    }
  }

  void applyOptions(BasicItem item, String[] options, int start) {
    for (int n = start; n < options.length; n++) {
      String option = options[n].strip();
      String[] parms = option.split(" ");
      String feature = parms[0].toLowerCase();
      if (feature.contains("resize") && parms.length > 1) {
        item.resize(Integer.parseInt(parms[1]));
      } else if (feature.contains("fontsize")) {
        item.fontSize(Integer.parseInt(parms[1]));
      } else if (feature.contains("top")) {
        item.padT(Integer.parseInt(parms[1]));
      } else if (feature.contains("bold")) {
        item.bold();
      }
    }
  }

  List<BasicItem> lines = new ArrayList<>();

  void appendY(BasicItem item) {
    if (item != null) {
      if (item.getPadding().top() == 0)
        item.padT(spaceBefore);
      lines.add(item);
    }
  }

  void packLines(Container pane, List<BasicItem> items) {
    int nextPos = margin.top;
    int maxW = 0;
    Rectangle bounds;
    for (BasicItem item : items) {
      Component comp = item.create();
      if (comp != null) {
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
      addListeners(item);
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

    void mouseListener(BasicItem item) {
      item.comp().addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          clicked();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          if (item instanceof ImageItem) {
            ((JLabel) item.comp()).setBorder(BorderFactory.createLineBorder(SXRED, 3));
          } else {
            item.comp().setBackground(SXLBLSELECTED);
          }
        }

        @Override
        public void mouseExited(MouseEvent e) {
          if (item instanceof ImageItem) {
            ((JLabel) item.comp()).setBorder(null);
          } else {
            item.comp().setBackground(SXLBLBUTTON);
          }
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

    BasicItem fontSize(int size) {
      fontSize = size;
      return this;
    }

    int fontSize = stdFontSize;

    BasicItem bold() {
      fontBold = Font.BOLD;
      return this;
    }

    int fontBold = 0;
    //endregion

  }
  //endregion

  //region 51 LineItem
  class LineItem extends BasicItem {

    LineItem() {
    }

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

    ImageItem() {
      Commons.error("ImageItem: no image given");
    }

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
      //TODO resize BufferedImage
      Commons.loadOpenCV();
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

  //region 531 ImageLink
  class ImageLink extends ImageItem {
    String aLink = "https://sikulix.github.io";

    ClickAction clickAction = new ClickAction() {
      @Override
      public void run() {
        close();
        Commons.browse(aLink);
      }
    };

    ImageLink(URL url) {
      super(url);
      setActive();
      super.clickAction = clickAction;
    }

    ImageLink(URL url, String link) {
      this(url);
      aLink = link;
    }
  }
  //endregion
}
