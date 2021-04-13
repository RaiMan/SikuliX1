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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SXDialog extends JFrame {

  public enum POSITION {TOP, TOPLEFT, TOPRIGHT, CENTER}

  enum STATE {ON, OFF}

  JFrame frame;
  Container pane;

  private SXDialog() {
    frame = this;
    setResizable(false);
    setUndecorated(true);
    pane = getContentPane();
    pane.setLayout(null);
    setMargin(10);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        Commons.debug("global key listener fired - closing window without saving state");
        close();
      }
    });
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
    Class clazz = SXDialog.class;
    if (!res.startsWith("/")) {
      if (res.startsWith("ide")) {
        try {
          clazz = Class.forName("org.sikuli.ide.SikulixIDE");
        } catch (ClassNotFoundException e) {
        }
      }
      res = "/Settings/" + res;
    }
    textToItems(Commons.copyResourceToString(res, clazz));
    packLines(pane, dialogLines);
    if (pos.equals(POSITION.TOP)) {
      where.x -= finalSize.width / 2;
    } else if (pos.equals(POSITION.CENTER)) {
      where.x -= finalSize.width / 2;
      where.y -= finalSize.height / 2;
    }
    popup(where);
  }

  //region 04 global handler
  String globalPrefix = "";

  public enum KEYS {ESC, ENTER, SPACE, ANY}

  static Map<KEYS, Integer> keyMap = new HashMap<>();

  static {
    keyMap.put(KEYS.ESC, KeyEvent.VK_ESCAPE);
    keyMap.put(KEYS.ENTER, KeyEvent.VK_ENTER);
    keyMap.put(KEYS.SPACE, KeyEvent.VK_SPACE);
    keyMap.put(KEYS.ANY, -1);
  }

  boolean checkKey(KeyEvent e, KEYS key) {
    final int actualKey = e.getKeyCode();
    if (key.equals(KEYS.ANY)) {
      return true;
    }
    return actualKey == keyMap.get(key);
  }

  class ItemAction implements Runnable {
    String actionName = "standard_click_action";
    Object options;
    BasicItem item;

    public ItemAction(BasicItem item, Object... options) {
      this.item = item;
      if (options.length > 0) {
        if (options[0] instanceof String) {
          actionName = (String) options[0];
        }
        if (options.length > 1) {
          this.options = options[1];
        }
      }
    }

    @Override
    public void run() {
    }
  }

  ItemAction standardItemAction(BasicItem item) {
    return new ItemAction(item) {
      @Override
      public void run() {
        String title = item.getTitle();
        if (item instanceof ButtonItem) {
          switch (title) {
            case "OK":
              closeOk();
              return;
            case "APPLY":
              apply();
              return;
            case "CANCEL":
              closeCancel();
              return;
          }
        }
        Commons.debug("Button: %s: running action: %s", title, actionName);
      }
    };
  }

  ButtonItem OK_BUTTON = null;
  ButtonItem APPLY_BUTTON = null;
  ButtonItem CANCEL_BUTTON = null;


  void globalButtons(ButtonItem button) {
    switch (button.getTitle()) {
      case "OK":
        OK_BUTTON = button;
        return;
      case "APPLY":
        APPLY_BUTTON = button;
        return;
      case "CANCEL":
        CANCEL_BUTTON = button;
        return;
    }


  }
  //endregion

  //region 05 global features
  public Color SXRED = new Color(0x9D, 0x42, 0x30, 208);
  public Color SXLBLBUTTON = new Color(241, 230, 206);
  public Color SXLBLSELECTED = new Color(167, 192, 220);
  public Color BACKGROUNDCOLOR = Color.WHITE;

  public String fontName = Font.SANS_SERIF;

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
    if (APPLY_BUTTON != null) {
      APPLY_BUTTON.comp().requestFocusInWindow();
    }
  }

  void close() {
    setVisible(false);
  }

  void closeCancel() {
    Commons.debug("ESC or Button CANCEL: closing dialog without saving anything");
    close();
  }

  void closeOk() {
    apply();
    Commons.debug("ENTER or Button OK: closing dialog");
    close();
  }

  void apply() {
    Commons.debug("SPACE or Button APPLY: saving changed state");
  }
  //endregion

  //region 30 text to items
  enum TEXT {TEXT, HTML, TEXT_IN_LINE}

  String lineTypeStd = "#";
  String lineTypeLeft = "+";
  String lineTypeRight = "-";

  void textToItems(String text) {
    String[] lines = text.split("\n");
    boolean first = true;
    for (String line : lines) {
      String lineType = lineTypeStd;
      line = line.strip();
      if (line.isEmpty()) {
        continue;
      }
      String[] options = line.split(";");
      if (first && line.startsWith("#global")) {
        if (options.length > 1) {
          Commons.debug("--- %s", line);
          for (String option : options) {
            if (option.startsWith(lineTypeStd)) {
              continue;
            }
            applyOption(option);
          }
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
            append(new LineItem().stroke(number), lineType);
            continue;
          }
        }
        append(new LineItem(), lineType);
        continue;
      }
      TEXT isText = null;
      lineType = line.substring(0, 1);
      if ("#+-".contains(lineType)) {
        BasicItem item = null;
        int start = 2;
        String feature = options[0].strip().toLowerCase().substring(1);
        String title = "";
        if (options.length > 1) {
          title = options[1].strip();
        }
        if (feature.startsWith("link")) {
          String[] parts = title.split("\\|");
          String url = parts[0].strip();
          String urlText = url;
          if (parts.length > 1) {
            urlText = parts[0].strip();
            url = parts[1].strip();
          }
          item = new LinkItem(urlText, url);
        } else if (feature.startsWith("image")) {
          item = new ImageItem(this.getClass().getResource(title));
        } else if (feature.startsWith("close")) {
          item = new TextItem(title);
          item.setActive();
        } else if (feature.startsWith("action")) {
          String action = options.length > 2 ? options[2] : "";
          item = new ActionItem(title, action);
        } else if (feature.startsWith("option")) {
          String action = options.length > 2 ? options[2] : "";
          STATE state = options.length > 3 ? (options[2].toLowerCase().contains("on") ? STATE.ON : STATE.OFF) : STATE.OFF;
          item = new OptionItem(title, action, state);
        } else if (feature.startsWith("buttons")) {
          if (title.isEmpty()) {
            new ButtonItems("CANCEL | APPLY | OK", options, lineType);
          } else {
            new ButtonItems(title, options, lineType);
          }
          continue;
        } else if (feature.startsWith("button")) {
          item = new ButtonItem(title);
        } else if (feature.startsWith("html")) {
          isText = TEXT.HTML;
        } else if (feature.isEmpty()) {
          isText = TEXT.TEXT_IN_LINE;
        } else if (feature.startsWith("prefix")) {
          globalPrefix = title;
          continue;
        } else {
          Commons.error("SXDialog: unknown feature %s", feature);
          item = new TextItem("? " + title + " ?");
          append(item, lineTypeStd);
          continue;
        }
        if (isText == null) {
          applyOptions(item, options, start);
          append(item, lineType);
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
        } else if (isText.equals(TEXT.TEXT_IN_LINE)) {
          item = new TextItem(options[1]);
          start = 2;
        } else {
          lineType = lineTypeStd;
          item = new TextItem(options[0]);
        }
        applyOptions(item, options, start);
        append(item, lineType);
      } else {
        lineType = lineTypeStd;
        append(new TextItem(line), lineType);
      }
    }
    appendLine(lineTypeStd);
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
  List<BasicItem[]> dialogLines = new ArrayList<>();
  List<BasicItem> dialogLineLeft = new ArrayList<>();
  List<BasicItem> dialogLineRight = new ArrayList<>();

  void append(BasicItem item, String lineType) {
    if (item != null) {
      if (item.getPadding().top() == 0) {
        item.padT(spaceBefore);
      }
      if (lineType.equals(lineTypeStd)) {
        appendLine(lineType);
        dialogLines.add(new BasicItem[]{item});
      } else if (lineType.equals(lineTypeLeft)) {
        appendLine(lineType);
        dialogLineLeft.add(item);
      } else {
        appendLine(lineType);
        item.align(ALIGN.RIGHT);
        dialogLineRight.add(item);
      }
    }
  }

  void appendLine(String lineType) {
    if (!lineType.equals(lineTypeLeft) && dialogLineLeft.size() > 0) {
      dialogLines.add(dialogLineLeft.toArray(new BasicItem[0]));
      dialogLineLeft.clear();
    }
    int size = dialogLineRight.size();
    if (!lineType.equals(lineTypeRight) && size > 0) {
      dialogLines.add(dialogLineRight.toArray(new BasicItem[0]));
      dialogLineRight.clear();
    }
  }

  public static BasicItem[] reverseArray(BasicItem[] anArray) {
    for (int i = 0; i < anArray.length / 2; i++) {
      BasicItem temp = anArray[i];
      anArray[i] = anArray[anArray.length - i - 1];
      anArray[anArray.length - i - 1] = temp;
    }
    return anArray;
  }

  void append(BasicItem[] items, String lineType) {
    for (BasicItem item : items) {
      if (item.getPadding().top() == 0)
        item.padT(spaceBefore);
      if (item.getPadding().left() == 0)
        item.padL(spaceBefore);
    }
    appendLine(lineType);
    dialogLines.add(items);
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
      int nextPosX;
      bounds = item.getBounds();
      bounds.y = nextPosY;
      if (!first) {
        bounds.y += item.getPadding().top;
      }
      nextPosY = bounds.y + bounds.height;
      bounds.x += margin.left;
      maxW = Math.max(bounds.x + bounds.width + margin.right, maxW);
      nextPosX = bounds.x + bounds.width;
      item.setBounds(bounds);
      if (items.length > 1) {
        for (int n = 1; n < items.length; n++) {
          item = items[n];
          item.create();
          bounds = item.getBounds();
          bounds.y = currentPosY;
          if (!first) {
            bounds.y += item.getPadding().top;
          }
          nextPosY = Math.max(nextPosY, bounds.y + bounds.height);
          bounds.x = nextPosX + item.getPadding().left;
          nextPosX = bounds.x + bounds.width;
          maxW = Math.max(maxW, nextPosX + margin.right);
          item.setBounds(bounds);
        }
      }
      first = false;
    }
    Dimension paneSize = new Dimension(maxW, nextPosY + margin.bottom);
    int availableW = paneSize.width - margin.left() - margin.right();
    for (BasicItem[] items : this.dialogLines) {
      int length = items.length;
      if (items[0].isCenter() && length > 1) {
        bounds = items[length - 1].getBounds();
      }
      if (!items[0].isLeft()) {
        int nextPosX = paneSize.width - margin.right;
        first = true;
        for (int n = items.length - 1; n > -1; n--) {
          BasicItem item = items[n];
          bounds = item.getBounds();
          bounds.x = nextPosX - bounds.width;
          if (!first) {
            bounds.x -= item.getPadding().right;
          } else {
            first = false;
          }
          item.setBounds(bounds);
          nextPosX = bounds.x;
        }
      }
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
        }
        bounds.x += off;
        if (item.isValid()) {
          final JComponent comp = item.finalComp();
          comp.setBounds(bounds);
          pane.add(comp);
          item.addListeners();
        }
      } else {
        for (BasicItem item : items) {
          pane.add(item.finalComp());
          item.addListeners();
        }
      }
    }
    finalSize = paneSize;
  }
  //endregion

  //region 50 BasicItem
  abstract class BasicItem {

    BasicItem() {
    }

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

    ItemAction itemAction = null;

    void itemAction(ItemAction action) {
      itemAction = action;
    }

    void addListeners() {
      if (active()) {
        if (comp instanceof JLabel) {
          if (this instanceof ActionItem || this instanceof LinkItem) {
            comp.setOpaque(true);
            comp.setBackground(BACKGROUNDCOLOR);
          } else if (this instanceof TextItem) {
            comp.setOpaque(true);
            setBackground(SXLBLBUTTON);
            comp.setBackground(getBackground());
          }
        }
        comp.addMouseListener(new ItemMouseAdapter(this));
        comp.addKeyListener(new ItemKeyAdapter(this));
      }
    }

    class ItemMouseAdapter extends MouseAdapter {
      BasicItem item;

      ItemMouseAdapter(BasicItem item) {
        this.item = item;
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        item.mouseClick(e);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        item.mouseEnter(e);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        item.mouseExit(e);
      }
    }

    void mouseClick(MouseEvent e) {
      if (itemAction != null) {
        itemAction.run();
      }
    }

    void mouseEnter(MouseEvent e) {
      comp().setBackground(getSelected());
    }

    void mouseExit(MouseEvent e) {
      comp().setBackground(getBackground());
    }

    class ItemKeyAdapter extends KeyAdapter {
      BasicItem item;

      ItemKeyAdapter(BasicItem item) {
        this.item = item;
      }

      @Override
      public void keyReleased(KeyEvent e) {
        keyRelease(e);
      }
    }

    void keyRelease(KeyEvent e) {
      if (checkKey(e, KEYS.ESC)) {
        closeCancel();
      } else if (checkKey(e, KEYS.ENTER)) {
        closeOk();
      } else if (checkKey(e, KEYS.SPACE)) {
        apply();
      }
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

    Color selected = SXLBLSELECTED;

    public Color getSelected() {
      return selected;
    }

    void setSelected(Color color) {
      selected = color;
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

    LinkItem(String text, String link) {
      aText = text;
      if (!text.equals(link)) {
        aLink = link.strip();
      }
      underline();
      setActive();
    }

    void mouseClick(MouseEvent e) {
      close();
      Commons.browse(aLink);
    }
  }

  //endregion

  //region 522 ActionItem
  class ActionItem extends TextItem {
    String aAction = "";
    String command = null;
    String what = "";

    ActionItem() {
    }

    ActionItem(String text, String action) {
      aText = text.strip();
      aAction = action.strip();
      setActive();
      bold();
      setBackground(BACKGROUNDCOLOR);
      getAction();
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

    @Override
    void mouseClick(MouseEvent e) {
      if (command != null) {
        if (command.equals("show")) {
          new SXDialog(what);
        }
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
    }

    void create() {
      super.create();
      setStartState((JLabel) comp());
    }

    @Override
    void mouseClick(MouseEvent e) {
      if (state.equals(STATE.ON)) {
        ((JLabel) comp()).setText("( ) " + title);
        state = STATE.OFF;
      } else {
        ((JLabel) comp()).setText("(X) " + title);
        state = STATE.ON;
      }
    }
  }
  //endregion

  //region 524 ButtonItem
  class ButtonItem extends BasicItem {
    ButtonItem(String text) {
      this(text, null);
    }

    ButtonItem(String text, ItemAction itemAction) {
      title = text;
      this.itemAction(itemAction);
      setActive();
      bold();
    }

    void create() {
      if (itemAction == null) {
        itemAction = standardItemAction(this);
      }
      JButton button = new JButton(getTitle());
      button.setForeground(SXRED);
      setSize(button.getPreferredSize());
      comp(button); //Button
    }

    void mouseEnter(MouseEvent e) {
      JButton button = (JButton) comp();
      if (frame.isActive()) {
        button.setForeground(Color.WHITE);
        button.setSelected(true);
      }
    }

    void mouseExit(MouseEvent e) {
      JButton button = (JButton) comp();
      button.setForeground(SXRED);
      button.setSelected(false);
    }
  }

  class ButtonItems {
    ButtonItems(String text, String[] options, String lineType) {
      String[] parts = null;
      parts = text.split("\\|");
      ButtonItem[] buttonItems = new ButtonItem[parts.length];
      int ix = 0;
      for (String part : parts) {
        String buttonTyp = part.strip();
        ButtonItem buttonItem = new ButtonItem(buttonTyp);
        if (lineType.equals(lineTypeRight)) {
          buttonItem.align(ALIGN.RIGHT);
        }
        globalButtons(buttonItem);
        buttonItems[ix] = buttonItem;
        ix++;
      }
      append(buttonItems, lineType);
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

    void mouseEnter(MouseEvent e) {
      comp().setBorder(BorderFactory.createLineBorder(SXRED, 3));
    }

    void mouseExit(MouseEvent e) {
      comp().setBorder(null);
    }

  }
  //endregion

  //region 531 ImageLink
  class ImageLink extends ImageItem {
    String aLink = "https://sikulix.github.io";

    ImageLink(URL url) {
      super(url);
      setActive();
    }

    ImageLink(URL url, String link) {
      this(url);
      aLink = link;
    }

    @Override
    void mouseClick(MouseEvent e) {
      close();
      Commons.browse(aLink);
    }
  }
  //endregion
}
