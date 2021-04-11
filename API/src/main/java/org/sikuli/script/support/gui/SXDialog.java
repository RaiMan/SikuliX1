/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.gui;

import org.sikuli.script.support.Commons;
import org.sikuli.script.support.devices.ScreenDevice;

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
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SXDialog extends JFrame {

  public enum POSITION {TOP, TOPLEFT, TOPRIGHT, CENTER}

  enum STATE {ON, OFF}

  Container pane;

  private SXDialog() {
    globalInit();
    keyListenerClose();
  }

  public SXDialog(String res) {
    this(res, ScreenDevice.primary().getCenter(), POSITION.CENTER);
  }

  public SXDialog(String res, Point where) {
    this(res, where, POSITION.TOPLEFT);
  }

  public SXDialog(String res, Point where, POSITION pos) {
    this();
    if (!res.contains(".") && !res.endsWith(".txt")) {
      res += ".txt";
    }
    if (!res.startsWith("/")) {
      res = "/Settings/" + res;
    }
    textToItems(Commons.copyResourceToString(res, SXDialog.class));
    packLines(pane, lines);
    if (pos.equals(POSITION.TOP)) {
      where.x -= finalSize.width / 2;
    } else if (pos.equals(POSITION.CENTER)) {
      where.x -= finalSize.width / 2;
      where.y -= finalSize.height / 2;
    }
    popup(where);
  }

  //region 04 global handler
  public enum KEYS {ESC, ANY}

  void keyListenerClose(KEYS key) {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (key.equals(KEYS.ANY) || checkKey(e, key)) {
          setVisible(false);
        }
      }
    });
  }

  void keyListenerClose() {
    keyListenerClose(KEYS.ESC);
  }

  boolean checkKey(KeyEvent e, KEYS key) {
    final int actualKey = e.getKeyCode();
    if (key.equals(KEYS.ESC)) {
      return actualKey == (KeyEvent.VK_ESCAPE);
    }
    return false;
  }

  void addListeners(BasicItem item) {
    if (item.active()) {
      item.mouseListener(item);
      if (item.comp() instanceof JLabel) {
        if (item instanceof ActionItem || item instanceof LinkItem) {
          ((JLabel) item.comp).setOpaque(true);
          item.comp.setBackground(BACKGROUNDCOLOR);
        } else if (item instanceof TextItem) {
          ((JLabel) item.comp).setOpaque(true);
          item.setBackground(SXLBLBUTTON);
          item.comp.setBackground(item.getBackground());
        }
      }
    }
  }
  //endregion

  //region 05 global features
  public Color SXRED = new Color(0x9D, 0x42, 0x30, 208);
  public Color SXLBLBUTTON = new Color(241, 230, 206);
  public Color SXLBLSELECTED = new Color(167, 192, 220);
  public Color BACKGROUNDCOLOR = Color.WHITE;

  public String fontName = Font.SANS_SERIF;

  void globalInit() {
    setResizable(false);
    setUndecorated(true);
    pane = getContentPane();
    pane.setLayout(null);
    setMargin(10);
  }

  Color borderColor = SXRED;
  int border = 3;

  void setBorder(int dim) {
    border = dim;
  }

  void setBorder(Color color) {
    borderColor = color;
  }

  void setBorder(int dim, Color color) {
    border = dim;
    borderColor = color;
  }

  private void setBorderColor(String parm) {
    final Field[] fields = Color.class.getDeclaredFields();
    for (Field field : fields) {
      if (parm.equalsIgnoreCase(field.getName())) {
        try {
          setBorder((Color) field.get(null));
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
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
    pane.setBackground(BACKGROUNDCOLOR);
    ((JComponent) pane).setBorder(BorderFactory.createLineBorder(borderColor, border));
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

  //region 30 text to items
  enum TEXT {TEXT, HTML}

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
      if (line.startsWith("---")) {
        if (line.length() > 3) {
          final Integer number = getNumber(line.substring(3).strip());
          if (number != null) {
            append(new LineItem().stroke(number));
            continue;
          }
        }
        append(new LineItem());
        continue;
      }
      TEXT isText = null;
      if (line.startsWith("#")) {
        BasicItem item = null;
        int start = 2;
        String feature = options[0].strip().toLowerCase();
        String title = "#default";
        if (options.length > 1) {
          title = options[1].strip();
        }
        if (feature.startsWith("#link")) {
          String[] parts = title.split("\\|");
          String url = parts[0].strip();
          String urlText = url;
          if (parts.length > 1) {
            urlText = parts[0].strip();
            url = parts[1].strip();
          }
          item = new LinkItem(urlText, url);
        } else if (feature.startsWith("#image")) {
          item = new ImageItem(this.getClass().getResource(title));
        } else if (feature.startsWith("#close")) {
          item = new TextItem(title);
          item.setActive();
        } else if (feature.startsWith("#action")) {
          String action = options.length > 2 ? options[2] : "";
          item = new ActionItem(title, action);
        } else if (feature.startsWith("#option")) {
          String action = options.length > 2 ? options[2] : "";
          STATE state = options.length > 3 ? (options[2].toLowerCase().contains("on") ? STATE.ON : STATE.OFF) : STATE.OFF;
          item = new OptionItem(title, action, state);
        } else if (feature.startsWith("#buttons")) {
          new ButtonItems(title, options);
          continue;
        } else if (feature.startsWith("#html")) {
          isText = TEXT.HTML;
        } else {
          Commons.error("SXDialog: unknown feature %s", feature);
          item = new TextItem("? " + title + " ?");
          append(item);
          continue;
        }
        if (isText == null) {
          applyOptions(item, options, start);
          append(item);
          continue;
        }
      }
      isText = isText == null ? TEXT.TEXT : isText;
      if (options.length > 1) {
        TextItem item;
        int start = 1;
        if (isText.equals(TEXT.HTML)) {
          item = new HtmlItem(options[1]);
          start = 2;
        } else {
          item = new TextItem(options[0]);
        }
        applyOptions(item, options, start);
        append(item);
      } else {
        append(new TextItem(line));
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
    } else if (feature.contains("font")) {
      setFontSize(Integer.parseInt(parms[1]));
    } else if (feature.contains("before")) {
      setSpaceBefore(Integer.parseInt(parms[1]));
    } else if (feature.contains("border") && parms.length > 1) {
      for (int n = 1; n < parms.length; n++) {
        String parm = parms[n];
        Integer num = getNumber(parm);
        if (num == null) {
          setBorderColor(parm);
        } else {
          setBorder(num);
        }
      }
    }
  }

  Integer getNumber(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  void applyOptions(BasicItem item, String[] options, int start) {
    for (int n = start; n < options.length; n++) {
      String option = options[n].strip();
      String[] parms = option.split(" ");
      String feature = parms[0].toLowerCase();
      if (feature.contains("resize") && parms.length > 1) {
        item.resize(Integer.parseInt(parms[1]));
      } else if (feature.startsWith("f")) {
        item.fontSize(Integer.parseInt(parms[1]));
      } else if (feature.contains("top")) {
        item.padT(Integer.parseInt(parms[1]));
      } else if (feature.contains("bold")) {
        item.bold();
      } else if (feature.contains("center")) {
        item.align(ALIGN.CENTER);
      }
    }
  }
  //endregion

  //region 20 top-down line items
  List<BasicItem[]> lines = new ArrayList<>();

  void append(BasicItem item) {
    if (item != null) {
      if (item.getPadding().top() == 0)
        item.padT(spaceBefore);
      lines.add(new BasicItem[]{item});
    }
  }

  void append(BasicItem[] items) {
    for (BasicItem item : items) {
      if (item.getPadding().top() == 0)
        item.padT(spaceBefore);
      if (item.getPadding().left() == 0)
        item.padL(spaceBefore);
    }
    lines.add(items);
  }

  void packLines(Container pane, List<BasicItem[]> lines) {
    int nextPosY = margin.top;
    int currentPosY;
    int maxW = 0;
    Rectangle bounds;
    boolean first = true;
    for (BasicItem[] items : lines) {
      BasicItem item = items[0];
      item.create();
      currentPosY = nextPosY;
      bounds = item.getBounds();
      if (item.isValid()) {
        bounds.y = nextPosY;
        if (!first) {
          bounds.y += item.getPadding().top;
        }
        bounds.x += margin.left;
        item.setBounds(bounds);
        nextPosY = bounds.y + bounds.height;
      } else {
        Commons.error("SXDialog: packLines: comp == null: %s", item);
      }
      int nextPosX = bounds.x + bounds.width;
      maxW = Math.max(bounds.x + bounds.width + margin.right, maxW);
      if (items.length > 1) {
        for (int n = 1; n < items.length; n++) {
          item = items[n];
          item.create();
          if (item.isValid()) {
            bounds = item.getBounds();
            bounds.y = currentPosY;
            if (!first) {
              bounds.y += item.getPadding().top;
            }
            bounds.x = nextPosX + item.getPadding().left;
            nextPosY = Math.max(nextPosY, bounds.y + bounds.height);
            nextPosX = bounds.x + bounds.width;
            item.setBounds(bounds);
          }
          maxW = Math.max(maxW, nextPosX + margin.right);
        }
      }
      first = false;
    }
    Dimension paneSize = new Dimension(maxW, nextPosY + margin.bottom);
    int availableW = paneSize.width - margin.left() - margin.right();
    for (BasicItem[] items : this.lines) {
      if (items.length == 1) {
        BasicItem item = items[0];
        int off = 0;
        bounds = item.getBounds();
        if (bounds.width == 0) {
          item.fill(availableW);
          bounds.setSize(item.getSize());
        }
        if (item.isCenter()) {
          off = (availableW - bounds.width) / 2;
        } else if (item.isRight()) {
          off = availableW - bounds.width;
        }
        bounds.x += off;
        if (item.isValid()) {
          final JComponent comp = item.finalComp();
          comp.setBounds(bounds);
          pane.add(comp);
          addListeners(item);
        }
      } else {
        for (BasicItem item : items) {
          pane.add(item.finalComp());
          addListeners(item);
        }
      }
    }
    finalSize = paneSize;
  }
  //endregion

  //region 50 BasicItem
  abstract class BasicItem {

    //region Component
    String title = this.getClass().getSimpleName();

    String getTitle() {
      return title;
    }

    boolean isValid() {
      return comp != null;
    }

    private JComponent comp = null;

    JComponent comp(JComponent comp) {
      this.comp = comp;
      return comp;
    }

    JComponent comp() {
      return comp;
    }

    void create() {
    }

    void fill(int w) {
    }

    JComponent finalComp() {
      comp.setBounds(getBounds());
      return comp;
    }

    JComponent panel = null;
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

    BasicItem padL(int val) {
      padding.left(val);
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
      if (comp == null) {
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

    int X = 0;
    int Y = 0;

    void setPos(Point where) {
      this.X = where.x;
      this.Y = where.y;
    }

    int width = 0;
    int height = 0;

    int getH() {
      return height;
    }

    void setH(int val) {
      height = val;
    }

    int getW() {
      return width;
    }

    void setW(int val) {
      width = val;
    }

    Dimension getSize() {
      return new Dimension(getW(), getH());
    }

    void setSize(Dimension size) {
      width = size.width;
      height = size.height;
    }

    void setBounds(Rectangle bounds) {
      setPos(bounds.getLocation());
      setSize(bounds.getSize());
    }

    Rectangle getBounds() {
      return new Rectangle(X, Y, width, height);
    }
    //endregion

    //region Listener
    private boolean active = false;

    boolean active() {
      return active;
    }

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
          } else if (item instanceof ButtonItem) {
            JButton button = (JButton) item.comp();
            button.setForeground(Color.WHITE);
            button.setSelected(true);
          } else {
            item.comp().setBackground(SXLBLSELECTED);
          }
        }

        @Override
        public void mouseExited(MouseEvent e) {
          if (item instanceof ImageItem) {
            ((JLabel) item.comp()).setBorder(null);
          } else if (item instanceof ButtonItem) {
            JButton button = (JButton) item.comp();
            button.setForeground(SXRED);
            button.setSelected(false);
          } else {
            item.comp().setBackground(item.getBackground());
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
    Color background = BACKGROUNDCOLOR;

    public Color getBackground() {
      return background;
    }

    void setBackground(Color color) {
      background = color;
    }

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
      setH(stroke);
    }

    LineItem(int len) {
      this();
      setW(len);
    }

    LineItem stroke(int stroke) {
      this.stroke = stroke;
      setH(stroke);
      return this;
    }

    LineItem color(Color color) {
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
        g2.draw(new Line2D.Float(0, 0, getW(), 0));
      }
    }

    void fill(int len) {
      setW(len);
      create();
    }

    void create() {
      Line line = new Line();
      line.setPreferredSize(getSize());
      comp(line); //Line
    }
  }
  //endregion

  //region 52 TextItem
  private int stdFontSize = 14;

  void setFontSize(int val) {
    stdFontSize = val;
  }

  class TextItem extends BasicItem {

    String aText = "";

    String getTitle() {
      return aText;
    }

    TextItem() {
    }

    TextItem(String aText) {
      this.aText = aText;
    }

    void create() {
      JLabel lblText = new JLabel(aText);
      Font font = new Font(fontName, fontBold, fontSize);
      Rectangle2D textLen = lblText.getFontMetrics(font).getStringBounds(aText, getGraphics());
      if (textLen.getWidth() > maxW) {
        fontSize = (int) (fontSize * maxW / textLen.getWidth());
        font = new Font(fontName, fontBold, fontSize);
        textLen = lblText.getFontMetrics(font).getStringBounds(aText, getGraphics());
      }
      if (underline) {
        lblText = new UnderlinedLabel(aText, font);
      }
      lblText.setFont(font);
      Rectangle r = new Rectangle(0, 0, (int) textLen.getWidth(), (int) textLen.getHeight());
      lblText.setBounds(r);
      setSize(r.getSize());
      comp(lblText);
    }

    class UnderlinedLabel extends JLabel {
      public UnderlinedLabel(String text, Font textFont) {
        super(text);
        setFont(textFont);
      }

      public void paint(Graphics g) {
        Rectangle r;
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(1));
        r = g2d.getClipBounds();
        int height = r.height - getFontMetrics(getFont()).getDescent() + 3;
        int width = getFontMetrics(getFont()).stringWidth(getText());
        g2d.drawLine(0, height, width, height);
      }
    }
  }

  class HtmlItem extends TextItem {

    HtmlItem(String text) {
      super(text);
    }

    void create() {
      super.create();
      JLabel lblHtml = (JLabel) comp();
      Dimension size = getSize();
      aText = "<html>" + aText.replace("|", "<br>");
      lblHtml.setText(aText);
      setSize(lblHtml.getPreferredSize());
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
      if (!text.equals(link)) {
        aLink = link.strip();
      }
      underline();
      setActive();
      super.clickAction = clickAction;
    }
  }
  //endregion

  //region 522 ActionItem
  class ActionItem extends TextItem {
    String aAction = "";
    String command = null;
    String what = "";

    ClickAction clickAction = new ClickAction() {
      @Override
      public void run() {
        if (command != null) {
          if (command.equals("show")) {
            new SXDialog(what);
          }
        }
      }
    };

    ActionItem() {
    }

    ActionItem(String text, String action) {
      aText = text.strip();
      aAction = action.strip();
      setActive();
      bold();
      setBackground(BACKGROUNDCOLOR);
      getAction();
      setClickAction(clickAction);
    }

    void getAction() {
      if (aAction.strip().isEmpty()) {
        return;
      }
      final String[] parts = aAction.split(" ");
      command = parts[0].strip();
      if (command.startsWith("show")) {
        if (parts.length > 1) {
          what = parts[1].strip();
        } else {
          Commons.error("ActionItem: show: no dialog");
        }
      } else {
        Commons.error("ActionItem: not implemented: %s", aAction);
      }
    }
  }
  //endregion

  //region 523 OptionItem
  class OptionItem extends ActionItem {
    String aOption = "";
    STATE state;
    STATE startState = STATE.OFF;
    String title;

    void setStartState(JLabel lbl) {
      if (startState.equals(STATE.OFF)) {
        lbl.setText("( ) " + title);
        state = STATE.OFF;
      } else {
        lbl.setText("(X) " + title);
        state = STATE.ON;
      }
    }

    ClickAction clickAction = new ClickAction() {
      @Override
      public void run() {
        if (state.equals(STATE.ON)) {
          ((JLabel) comp()).setText("( ) " + title);
          state = STATE.OFF;
        } else {
          ((JLabel) comp()).setText("(X) " + title);
          state = STATE.ON;
        }
      }
    };

    OptionItem(String text, String option, STATE state) {
      this.state = state;
      title = text;
      aText = "(X) " + text;
      if (state.equals(STATE.ON)) {
        startState = STATE.ON;
      }
      aOption = option;
      setActive();
      bold();
      setBackground(BACKGROUNDCOLOR);
      setClickAction(clickAction);
    }

    void create() {
      super.create();
      setStartState((JLabel) comp());
    }
  }
  //endregion

  //region 524 ButtonItem
  class ButtonItems {
    ButtonItems(String text, String[] options) {
      String[] parts = null;
      if (text.startsWith("#default")) {
        parts = new String[]{"CANCEL", "APPLY", "OK"};
      } else {
        parts = text.split("\\|");
      }
      ButtonItem[] buttonItems = new ButtonItem[parts.length];
      int ix = 0;
      for (String part : parts) {
        part = part.strip();
        buttonItems[ix] = new ButtonItem(part, "action" + part);
        ix++;
      }
      append(buttonItems);
    }
  }

  class ButtonItem extends ActionItem {
    String aAction;

    String getTitle() {
      return "Button: " + aAction;
    }

    ClickAction clickAction = new ClickAction() {
      @Override
      public void run() {
        close();
      }
    };

    ButtonItem(String text, String action) {
      aText = text;
      aAction = action;
      setActive();
      bold();
      setClickAction(clickAction);
    }

    void create() {
      JButton button = new JButton(aText);
      button.setForeground(SXRED);
      setSize(button.getPreferredSize());
      comp(button); //Button
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

    void create() {
      JLabel lblimg = new JLabel();
      if (img != null) {
        lblimg.setIcon(new ImageIcon(img));
        setSize(new Dimension(img.getWidth(), img.getHeight()));
      }
      comp(lblimg); //Image
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
