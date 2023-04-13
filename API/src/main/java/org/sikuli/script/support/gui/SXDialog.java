/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.support.gui;

import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
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
        if (checkKey(e, KEYS.ESC)) {
          Commons.trace("global key listener fired - closing window without saving state");
          close();
        }
      }
    });
  }

  public SXDialog(String res) {
    this(res, ScreenDevice.getPrimary().getCenter(), POSITION.CENTER);
  }

  public SXDialog(String res, POSITION pos) {
    this(res, new Point(ScreenDevice.getPrimary().x(), ScreenDevice.getPrimary().y()), pos);
  }

  public SXDialog(String res, Point where) {
    this(res, where, POSITION.TOPLEFT);
  }

  public SXDialog(String res, Point where, POSITION pos) {
    this();

    Class clazz = SXDialog.class;
    String text = "";
    if (!res.startsWith("/") && !res.startsWith("#")) {
      if (!res.contains(".") && !res.endsWith(".txt")) {
        res += ".txt";
      }
      if (res.startsWith("sx")) {
        if (res.startsWith("sxide")) {
          try {
            clazz = Class.forName("org.sikuli.ide.SikulixIDE");
          } catch (ClassNotFoundException e) {
          }
        }
        res = "/Settings/" + res;
      }
    } else if (res.startsWith("#")) {
      text = res;
      clazz = null;
    } else {
      Commons.trace("not implemented: res = %s", res);
    }
    if (clazz != null) {
      text = Commons.copyResourceToString(res, clazz);
    }
    if (text != null && !text.isEmpty()) {
      valid = true;
      this.pos = pos;
      this.where = where;
      textToItems(text);
      packBoxes(pane, dialogLines);
    }
  }

  public static void onScreen(SXDialog dialog) {
    onScreen(dialog,0,0);
  }

  public static void onScreen(SXDialog dialog, long when) {
    onScreen(dialog, when,0);
  }

  public static void onScreen(SXDialog dialog, long when, long time) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (when > 0) {
          Commons.pause(when);
        }
        dialog.setAlwaysOnTop(true);
        dialog.run();
        if (time > 0) {
          Commons.pause(time);
          dialog.dispose();
        }
      }
    }).start();
    Commons.pause(1);
  }

  private boolean valid = false;

  boolean isOK() {
    return valid;
  }

  private Point where;
  private POSITION pos;

  public void run() {
    if (isOK()) {
      if (pos.equals(POSITION.TOP)) {
        where.x -= finalSize.width / 2;
      } else if (pos.equals(POSITION.CENTER)) {
        where.x -= finalSize.width / 2;
        where.y -= finalSize.height / 2;
      }
      popup(where);
    }
  }

  //region 03 global constants
  JFrame frame;
  Container pane;

  public enum POSITION {TOP, TOPLEFT, TOPRIGHT, CENTER}

  enum STATE {ON, OFF}

  enum FEATURE {ERROR, CLOSE, LINK, IMAGE, ACTION, OPTION, BUTTON, HTML, PREFIX, TEXT, BOX, BOXH, BOXV, ROW, COL}

  final static String FEATURE_PLAIN = "#plain";
  final static String FEATURE_TEXT = "*text";
  final static String FEATURE_BOXEND = "##boxend##";

  static String FEATURES = "";
  static int FEATURE_MAXLEN = 0;
  static int FEATURE_MINLEN = Integer.MAX_VALUE;

  static {
    for (FEATURE feat : FEATURE.values()) {
      FEATURE_MAXLEN = Math.max(FEATURE_MAXLEN, feat.toString().length());
      FEATURE_MINLEN = Math.min(FEATURE_MINLEN, feat.toString().length());
      FEATURES += (feat + ";").toLowerCase();
    }
  }

  static String getFeature(String line) {
    String feature;
    int posItemSep = line.indexOf(itemSep);
    if (posItemSep >= FEATURE_MINLEN) {
      feature = line.substring(0, posItemSep);
      String lineType = feature.substring(0, 1);
      if (lineTypes.contains(lineType)) {
        feature = feature.substring(1);
      } else {
        lineType = lineTypeSame;
      }
      if (feature.length() <= FEATURE_MAXLEN) {
        feature = feature.toLowerCase();
        if (!FEATURES.contains(feature)) {
          Commons.trace("possible invalid feature - taken as text: %s", line);
          return FEATURE_PLAIN;
        }
      } else {
        return FEATURE_PLAIN;
      }
      return lineType + feature;
    }
    return FEATURE_PLAIN;
  }

  static boolean isFeat(String option, FEATURE token) {
    option = option.toUpperCase();
    String tok = token.toString();
    return option.equals(tok);
  }

  enum TEXT {TEXT, HTML}

  enum OPT {SIZE, MARGIN, PADDING, CENTER, FONT, BORDER}

  enum ITEMTYPE {SINGLE, LEFT, RIGHT, CENTER, OUT, BOTTOM, SAME}
  //endregion

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

  ButtonItem OK_BUTTON = null;
  ButtonItem APPLY_BUTTON = null;
  ButtonItem CANCEL_BUTTON = null;

  ItemAction standardItemAction(BasicItem item) {
    return new ItemAction(item) {
      @Override
      public void run() {
        String title = item.title();
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
        Commons.trace("Button: %s: running action: %s", title, actionName);
      }
    };
  }

  void globalButtons(ButtonItem button) {
    switch (button.title()) {
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
    if (dim > -1) {
      border = dim;
    }
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

  public Dimension getFinalSize() {
    return finalSize;
  }

  private int maxW = (int) (1024 * 0.8);
  private int maxH = maxW / 4 * 3;

  void setDialogSize(int w, int h) {
    if (maxW > -1) {
      maxW = w;
    }
    if (maxH > -1) {
      maxH = h;
    }
  }

  Dimension getDialogSize() {
    return new Dimension(maxW, maxH);
  }

  Rectangle margin = new Rectangle(0, 0, 0, 0);

  void setMargin(int val) {
    margin = new Rectangle(val, val, val, val);
  }

  int spaceAfter = 10;

  void setSpaceAfter(int val) {
    if (val > -1) {
      spaceAfter = val;
    }
  }

  enum ALIGN {NOTSET, LEFT, CENTER, RIGHT, TOP, BOTTOM}

  ALIGN stdAlign = ALIGN.NOTSET;

  void setAlign(ALIGN type) {
    stdAlign = type;
  }
  //endregion

  //region 14 show/hide/destroy
  void popup() {
    popup(null);
  }

  void popup(Point where) {
    if (finalSize.width > 0) {
      pane.setBackground(BACKGROUNDCOLOR);
      ((JComponent) pane).setBorder(BorderFactory.createLineBorder(borderColor, border));
      setSize(finalSize);
      if (where != null) {
        setLocation(where);
      }
      setAlwaysOnTop(true);
      setVisible(true);
      running = true;
      if (APPLY_BUTTON != null) {
        APPLY_BUTTON.comp().requestFocusInWindow();
      }
    } else {
      Commons.trace("pane empty");
    }
  }

  private boolean running = false;

  public boolean isRunning() {
    return running;
  }

  void close() {
    setVisible(false);
    running = false;
  }

  void closeCancel() {
    Commons.trace("ESC or Button CANCEL: closing dialog without saving anything");
    close();
  }

  void closeOk() {
    apply();
    Commons.trace("ENTER or Button OK: closing dialog");
    close();
  }

  void apply() {
    Commons.trace("SPACE or Button APPLY: saving changed state");
  }
  //endregion

  //region 10 text to items
  static String lineTypeSingle = "#";
  static String lineTypeLeft = "-";
  static String lineTypeCenter = "|";
  static String lineTypeRight = "+";
  static String lineTypeSame = "*";
  static String lineTypes = "*#+|-./";
  static String lineTypeNext = "/";
  static String lineTypeLine = "---";
  static String lineTypeRect = "###";
  static String lineTypeBreakLine = "===";
  static String itemSep = ";";

  boolean isLineItem(String line) {
    return line.startsWith(lineTypeLine);
  }

  boolean isRectItem(String line) {
    return line.startsWith(lineTypeRect);
  }

  boolean isBreakLine(String line) {
    return line.startsWith(lineTypeBreakLine);
  }

  boolean notLineItem(String line) {
    return !isLineItem(line) && !isRectItem(line) && !isBreakLine(line);
  }

  List<String> textLines = new ArrayList<>();

  ITEMTYPE lastItemType = ITEMTYPE.LEFT;
  BasicItem lastItem = null;

  String evalBoxes(String textline) {
    String checkStart = (textline + " ").substring(0, 2);
    String checkEnd = (textline).substring(textline.length() - 1);
    boolean startsWith = false;
    boolean endsWith = false;
    if (checkStart.contains("[")) {
      startsWith = true;
    }
    if (checkEnd.contains("]")) {
      endsWith = true;
    }
    if (!startsWith && !endsWith) {
      return textline;
    }
    String line = " " + textline + " ";
    String[] parts = line.split("\\[");
    int lenp = parts.length;
    if (lenp < 2 && !endsWith) {
      return textline;
    }
    if (startsWith) {
      String post;
      String col;
      String pres = "-|+";
      String pre = parts[0].strip();
      for (int n = 1; n < lenp; n++) {
        if (!pre.isEmpty()) {
          pre = pre.substring(0, 1);
          if (!pres.contains(pre)) {
            pre = " ";
          }
        }
        col = "";
        String contentLine = "";
        post = parts[n].strip();
        if (post.length() > 0 && post.substring(0, 1).equalsIgnoreCase("v")) {
          col = "v";
          contentLine = (post + " ").substring(1).strip();
        } else {
          contentLine = post;
        }
        String boxLine = pre + "box" + col + ";";
        textLines.add(boxLine);
        if (!contentLine.isEmpty()) {
          pre = evalBoxesEnd(contentLine);
        }
      }
    } else {
      evalBoxesEnd(textline);
    }
    return "";
  }

  String evalBoxesEnd(String line) {
    String pre = "";
    String[] partsEnd = (line + " ").split("\\]");
    if (partsEnd.length > 0) {
      for (int ne = 0; ne < partsEnd.length - 1; ne++) {
        textLines.add(partsEnd[ne]);
        textLines.add("#");
      }
      if (!partsEnd[partsEnd.length - 1].strip().isEmpty()) {
        pre = partsEnd[partsEnd.length - 1].strip();
      }
    }
    return pre;
  }

  void textToItems(String text) {

    lastItemType = ITEMTYPE.LEFT;

    boolean lineContinue = false;
    String finalTextLine = "";


    for (String textLine : text.split("\n")) {
      textLine = textLine.strip();
      if (textLine.isEmpty() || textLine.startsWith("//")) {
        continue;
      }
      if (!lineContinue) {
        finalTextLine = textLine;
      } else {
        finalTextLine += textLine;
      }
      if (finalTextLine.endsWith(lineTypeNext)) {
        lineContinue = true;
        finalTextLine = textLine.substring(0, textLine.length() - 1).strip();
        continue;
      }
      finalTextLine = evalBoxes(finalTextLine);
      if (finalTextLine.equals("##")) {
        break;
      }
      if (!finalTextLine.isEmpty()) {
        textLines.add(finalTextLine);
      }
    }

    if (textLines.get(0).startsWith("#global")) {
      getGlobals(textLines.get(0));
      textLines.remove(0);
    }

    for (String line : textLines) {
      if (line.isEmpty()) {
        continue;
      }
      String orgLine = line;
      TEXT isText = null;
      BasicItem item = null;
      String[] options = null;
      String feature = "";
      String lineType = "-";

      //TODO Commons.trace(line);
      if (text.contains("{")) {
        line = replaceVariables(line);
      }

      if (notLineItem(line)) {
        feature = getFeature(line);
        if (isFeat(feature, FEATURE.ERROR)) {
          continue;
        }
        if (feature.equals(FEATURE_PLAIN)) {
          feature = FEATURE_TEXT;
          String first = line.substring(0, 1);
          if (lineTypes.contains(first)) {
            if (line.length() == 1) {
              line = " ";
            } else {
              line = line.substring(1);
              feature = first + feature.substring(1);
            }
          }
          line = feature + ";" + line;
        }

        lineType = feature.substring(0, 1);
        feature = feature.substring(1);
      }

      String title = "";
      if (lineTypes.contains(lineType)) {

        if (!notLineItem(line)) {
          Integer number = null;
          if (line.length() > 3) {
            number = getNumber(line.substring(3).strip());
          }
          if (isLineItem(line)) { // FEAT.LINE
            if (number != null) {
              item = new LineItem().stroke(number);
            } else {
              item = new LineItem();
            }
          } else if (isRectItem(line)) {
            if (number != null) {
              item = new RectItem().stroke(number);
            } else {
              item = new RectItem();
            }
          }
        } else {
          options = line.split(itemSep);

          if (options.length > 1) {
            title = options[1].strip();
          }
          String[] itemOptions = new String[0];
          if (options.length > 2) {
            itemOptions = new String[options.length - 2];
            for (int n = 2; n < options.length; n++) {
              itemOptions[n - 2] = options[n];
            }
          }

          if (isFeat(feature, FEATURE.BOX) || isFeat(feature, FEATURE.BOXV) ||
              isFeat(feature, FEATURE.BOXH) || isFeat(feature, FEATURE.ROW) ||
              isFeat(feature, FEATURE.COL)) {
            item = new BoxItem(feature, title);

          } else if (isFeat(feature, FEATURE.LINK)) {
            String[] parts = title.split("\\|");
            String url = parts[0].strip();
            String urlText = url;
            if (parts.length > 1) {
              urlText = parts[0].strip();
              url = parts[1].strip();
            }
            item = new LinkItem(urlText, url);

          } else if (isFeat(feature, FEATURE.IMAGE) && !title.isEmpty()) {
            URL url = null;
            if (title.startsWith("file:")) {
              url = Commons.makeURL(title.substring(5));
            } else {
              url = this.getClass().getResource(title);
            }
            item = new ImageItem(url);

          } else if (isFeat(feature, FEATURE.CLOSE)) {
            if (title.isEmpty()) {
              title = "CANCEL: ESC or click";
            }
            item = new TextItem(title);
            item.setActive();

          } else if (isFeat(feature, FEATURE.ACTION)) {
            if (itemOptions.length > 0) {
              item = new ActionItem(title, itemOptions);
            }

          } else if (isFeat(feature, FEATURE.OPTION)) {
            if (itemOptions.length > 0) {
              item = new OptionItem(title, itemOptions);
            }

          } else if (isFeat(feature, FEATURE.BUTTON)) {
            if (title.isEmpty()) {
              append(new BoxItem(FEATURE.ROW.name(), "stdbuttons"), lineType);
              append(new ButtonItem("CANCEL"));
              append(new ButtonItem("APPLY"));
              append(new ButtonItem("OK"));
              append(new TextItem(FEATURE_BOXEND));
              continue;
            }
            item = new ButtonItem(title, itemOptions);

          } else if (isFeat(feature, FEATURE.HTML)) {
            isText = TEXT.HTML;
          } else if (isFeat(feature, FEATURE.TEXT)) {
            isText = TEXT.TEXT;

          } else if (isFeat(feature, FEATURE.PREFIX)) {
            globalPrefix = title;
            continue;
          }
        }
        if (isText != null) {
          if (isText.equals(TEXT.HTML)) {
            item = new HtmlItem(options[1]);
          } else if (isText.equals(TEXT.TEXT)) {
            if (orgLine.equals("#")) {
              item = new TextItem(FEATURE_BOXEND);
            } else {
              item = new TextItem(options[1]);
            }
          }
        }

        if (options != null && options.length > 2) {
          applyOptions(item, options);
        }

        append(item, lineType);
      }
    }
  }

  void append(BasicItem item) {
    append(item, "*");
  }

  void append(BasicItem item, String lineType) {
    if (item != null) {
      ITEMTYPE itemType = ITEMTYPE.SAME;
      if (lineType.equals(lineTypeLeft)) {
        itemType = ITEMTYPE.LEFT;
      } else if (lineType.equals(lineTypeRight)) {
        itemType = ITEMTYPE.RIGHT;
      } else if (lineType.equals(lineTypeCenter)) {
        itemType = ITEMTYPE.CENTER;
      } else if (lineType.equals(lineTypeSingle)) {
        if (item.isBox()) {
          itemType = ITEMTYPE.OUT;
        } else {
          itemType = ITEMTYPE.LEFT;
        }
      }
      if (itemType.equals(ITEMTYPE.SAME)) {
        if (lastItem == null || lastItem.isBoxEnd() || lastItem.isBoxBreak()) {
          itemType = ITEMTYPE.LEFT;
        } else {
          itemType = lastItemType;
        }
      }
      lastItemType = itemType;
      item.itemType(itemType);
      dialogLines.add(item);
    }
  }

  void getGlobals(String line) {
    String[] options = line.split(itemSep);
    if (!options[0].equals("#globals") || options.length == 1) {
      Commons.trace("#globals; empty or invalid: %s", line);
      return;
    }
    for (int n = 1; n < options.length; n++) {
      String[] parms = options[n].strip().split(" ");
      String option = parms[0].toLowerCase();
      int parm1 = getNumber(parms, 1);
      int parm2 = getNumber(parms, 2);
      if (isOption(option, OPT.SIZE)) {
        setDialogSize(parm1, parm2);
      } else if (isOption(option, OPT.MARGIN)) {
        setMargin(parm1);
      } else if (isOption(option, OPT.PADDING)) {
        setSpaceAfter(parm1);
      } else if (isOption(option, OPT.CENTER)) {
        setAlign(ALIGN.CENTER);
      } else if (isOption(option, OPT.FONT)) {
        setFontSize(parm1);
      } else if (isOption(option, OPT.BORDER)) {
        for (int pn = 1; pn < parms.length; pn++) {
          String parm = parms[pn];
          Integer num = getNumber(parm);
          if (num == null) {
            setBorderColor(parm);
          } else {
            setBorder(num);
          }
        }
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

  boolean isOption(String option, OPT token) {
    return option.substring(0, 1).equals(token.toString().substring(0, 1).toLowerCase());
  }

  Integer getNumber(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  Integer getNumber(String[] parms, int ix) {
    if (ix < parms.length) {
      Integer num = getNumber(parms[ix]);
      if (num != null) {
        return num;
      }
    }
    return -1;
  }

  void applyOptions(BasicItem item, String[] options) {
    if (item != null && options.length > 2) {
      for (int n = 2; n < options.length; n++) {
        String[] parms = options[n].strip().split(" ");
        String option = parms[0].strip().toLowerCase();
        int parm1 = getNumber(parms, 1);
        int parm2 = getNumber(parms, 2);
        if (option.startsWith("r") && parms.length > 1) {
          item.resize(parm1, parm2);
        } else if (option.startsWith("f")) {
          item.fontSize(parm1);
        } else if (option.startsWith("b")) {
          item.bold();
        } else if (option.startsWith("c")) {
          item.align(ALIGN.CENTER);
        }
      }
    }
  }
  //endregion

  //region 12 top-down pack items
  List<BasicItem> dialogLines = new ArrayList<>();

  BoxItem paneBox;
  BoxItem currentBox;
  List<BoxItem> boxStack = new ArrayList<>();
  BasicItem item;

  String cb() {
    return currentBox.toStringAll();
  }

  String pb() {
    return paneBox.toStringAll();
  }

  String it() {
    return item.toString();
  }

  void packBoxes(Container pane, List<BasicItem> allBoxesAndItems) {

    for (BasicItem itm : allBoxesAndItems) {
      Commons.trace("%s", itm);
    }

    paneBox = new BoxItem("boxv", "sxpanebox");
    paneBox.asPane();
    currentBox = paneBox;

    for (BasicItem itm : allBoxesAndItems) {
      item = itm;
      if (itm.isBoxEnd()) {
        if (boxStack.size() > 0) {
          currentBox.adjust();
          currentBox = boxStack.remove(0);
          currentBox.adjustSize();
        }
        continue;
      }

      if (itm instanceof RectItem) {
        itm.create(currentBox);
      } else {
        itm.create();
      }

      if (itm.isOut()) {
        currentBox.adjustSize();
      }
      currentBox.add(itm);
      if (itm.isBox()) {
        boxStack.add(0, currentBox);
        currentBox = (BoxItem) itm;
        continue;
      }
    }

    paneBox.adjust();
    paneBox.adjustSize();
    //finalSize = paneBox.makeReady();
    finalSize = paneBox.dim();
    if (margin.x > 0 || margin.y > 0) {
      for (BasicItem itm : allBoxesAndItems) {
        itm.addMargin(margin.getLocation());
      }
    }
    Commons.trace("%s", paneBox.toStringAll());
    finalSize.width += margin.x + margin.width;
    finalSize.height += margin.y + margin.height;

    for (BasicItem item : allBoxesAndItems) {
      if (item.hasComp()) {
        pane.add(item.finalComp());
        item.addListeners();
      }
    }
    pane.setSize(finalSize);
  }
  //endregion

  //region 50 BasicItem
  abstract class BasicItem {

    BasicItem() {
      alignment = stdAlign;
    }

    public String toString() {
      String clazz = this.getClass().getSimpleName();
      String title = title();
      title = title.length() > 20 ? title.substring(0, 20) + "..." : title;
      String str3 = itemType().toString().substring(0, 1);
      if (this instanceof BoxItem) {
        str3 += ((BoxItem) this).col ? "V" : "";
      }
      return String.format("%s[\"%s\" %s [%d,%d %dx%d]]", clazz, title, str3,
          x, y, width, height);
    }

    ITEMTYPE itemType = ITEMTYPE.LEFT;

    public ITEMTYPE itemType() {
      return itemType;
    }

    public void itemType(ITEMTYPE itemtype) {
      this.itemType = itemtype;
    }

    public boolean isBox() {
      return this instanceof BoxItem;
    }

    public boolean isBoxEnd() {
      return title.equals(FEATURE_BOXEND);
    }

    public boolean isBoxBreak() { //TODO
      return false;
      //return title.equals(FEATURE_BOXBREAK);
    }

    public boolean hasComp() {
      return !isBox() && !isBoxEnd() && !isBoxBreak();
    }

    boolean isLine() {
      return this instanceof LineItem;
    }

    boolean isRect() {
      return this instanceof RectItem;
    }

    boolean inPane() {
      return ((BoxItem) parent).isPane();
    }

    public void adjust(boolean col, int boxW, int boxH) {
      int off = 0;
      if (isLine()) {
        fill(col, boxW, boxH);
        return;
      }
      if (isRect()) {
        fill(boxW, boxH);
        return;
      }
      if (col) {
        if (alignCenter()) {
          off = (boxW - width) / 2;
        } else if (alignRightBottom()) {
          off = boxW - width;
        }
        x += off;
      } else {
        if (alignCenter()) {
          off = (boxH - height) / 2;
        } else if (alignRightBottom()) {
          off = boxH - height;
        }
        y += off;
      }
    }

    public BasicItem parent() {
      return parent;
    }

    public BasicItem parent(BasicItem parent) {
      this.parent = parent;
      return this;
    }

    BasicItem parent = null;

    //region Component
    String title = this.getClass().getSimpleName();

    String title() {
      return title;
    }

    void title(String text) {
      title = text;
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

    BasicItem create() {
      return this;
    }

    BasicItem create(BoxItem box) {
      return this;
    }

    void fill(boolean direction, int w, int h) {
    }

    void fill(int w, int h) {
    }

    JComponent finalComp() {
      comp.setBounds(rect());
      return comp;
    }

    JComponent panel = null;
    //endregion

    //region Alignment
    ALIGN alignment;

    public BasicItem align(ALIGN type) {
      alignment = type;
      return this;
    }

    public boolean alignCenter() {
      if (comp == null) {
        return false;
      }
      if (itemType.equals(ITEMTYPE.CENTER)) {
        return true;
      }
      if (alignment.equals(ALIGN.NOTSET)) {
        alignment = stdAlign;
      }
      return alignment.equals(ALIGN.CENTER);
    }

    public boolean alignRightBottom() {
      if (comp == null) {
        return false;
      }
      if (itemType.equals(ITEMTYPE.RIGHT) || itemType.equals(ITEMTYPE.BOTTOM)) {
        return true;
      }
      if (alignment.equals(ALIGN.NOTSET)) {
        alignment = stdAlign;
      }
      return alignment.equals(ALIGN.RIGHT) || alignment.equals(ALIGN.BOTTOM);
    }

    boolean isOut() { //TODO
      return false;
      //return this instanceof BoxItem && itemType.equals(ITEMTYPE.OUT);
    }
    //endregion

    //region Location Size
    BasicItem resize(int width) {
      return this;
    }

    BasicItem resize(int width, int height) {
      if (height < 0) {
        resize(width);
      }
      return this;
    }

    int x = 0;
    int y = 0;

    void pos(int x, int y) {
      this.x = x;
      this.y = y;
    }

    void pos(Point where) {
      x = where.x;
      y = where.y;
    }

    Point pos() {
      return new Point(x, y);
    }

    void addMargin(Point margin) {
      x += margin.x;
      y += margin.y;
    }

    int width = 0;
    int height = 0;

    void setH(int val) {
      height = val;
    }

    void setW(int val) {
      width = val;
    }

    Dimension dim() {
      return new Dimension(width, height);
    }

    void dim(int width, int height) {
      this.width = width;
      this.height = height;
    }

    void dim(Dimension size) {
      width = size.width;
      height = size.height;
    }

    void rect(Rectangle bounds) {
      pos(bounds.getLocation());
      dim(bounds.getSize());
    }

    void rect(Point where, Dimension dim) {
      pos(where);
      dim(dim);
    }

    Rectangle rect() {
      return new Rectangle(x, y, width, height);
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
      } else {
        closeCancel();
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
      if (size > -1) {
        fontSize = size;
      }
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

  //region 501 BoxItem
  class BoxItem extends BasicItem {

    RectItem rect = null;

    //region 10 adjust
    void add(BasicItem item) {
      item.parent(this);
      if (!item.isRect()) {
        evalItem(item);
        items.add(item);
      } else {
        rect = (RectItem) item;
      }
    }

    void evalItem(BasicItem item) {
      if (items.size() == 0) {
        item.pos(pos());
        dim(item.dim());
        return;
      }
      int x, y;
      final Dimension newDim = new Dimension();
      final Dimension bDim = dim();
      final Dimension iDim = item.dim();
      if (col) {
        x = pos().x;
        y = pos().y + dim().height + spaceAfter;
        newDim.width = Math.max(bDim.width, iDim.width);
        newDim.height = bDim.height + iDim.height + spaceAfter;
      } else {
        x = pos().x + dim().width + spaceAfter;
        y = pos().y;
        newDim.width = bDim.width + iDim.width + spaceAfter;
        newDim.height = Math.max(bDim.height, iDim.height);
      }
      dim(newDim);
      if (item.isLine()) {
        item.setW(dim().width);
      }
      item.pos(x, y);
    }

    void adjust() {
      if (items.size() == 0) {
        return;
      }
      if (rect != null) {
        items.add(rect);
      }
      for (BasicItem item : items) {
        if (!item.isBoxEnd() && !item.isBoxBreak()) {
          item.adjust(col, dim().width, dim().height);
        }
      }
    }

    void adjustSize() {
      if (items.size() == 0) {
        return;
      }
      int boxW = 0, boxH = 0;
      for (BasicItem item : items) {
        if (!item.isBoxEnd() && !item.isBoxBreak()) {
          int outX = item.pos().x;
          int outY = item.pos().y;
          if (col) {
            if (item.isOut()) {
              boxW = Math.max(boxW, outX + item.dim().width);
              boxH = Math.max(boxH, item.dim().height + spaceAfter);
            } else {
              boxW = Math.max(boxW, item.dim().width);
              boxH += item.dim().height + spaceAfter;
            }
          } else {
            if (item.isOut()) {
              boxW = Math.max(boxW, item.dim().width);
              boxH = Math.max(boxH, outY + item.dim().height + spaceAfter);
            } else {
              boxW += item.dim().width + spaceAfter;
              boxH = Math.max(boxH, item.dim().height);
            }
          }
        }
      }
      if (col) {
        dim(boxW, boxH - spaceAfter);
      } else {
        dim(boxW - spaceAfter, boxH);
      }
    }
    //endregion

    //region 20 other
    public String toStringAll() {
      String before = "\n" + super.toString();
      before += col ? " COL" : " ROW";
      before += " (" + items.size() + ")";
      String prefix = isPane() ? "" : " - ";
      String out = "\n";
      if (items.size() > 0) {
        for (BasicItem item : items) {
          out += prefix + (item.isBox() ? ((BoxItem) item).toStringAll() : item) + "\n";
        }
      }
      if (out.isEmpty()) {
        out = " *** empty ***";
      }
      out = before + out;
      return out;
    }

    BoxItem(String type, String title) {
      this.title = title;
      if (type.equals("boxv")) {
        col = true;
      }
    }

    boolean col = false;

    boolean pane = false;

    boolean isPane() {
      return pane;
    }

    void asPane() {
      pane = true;
    }

    List<BasicItem> items = new ArrayList<>();

    BasicItem get(int ix) {
      if (ix >= 0) {
        int range = items.size();
        if (ix < range) {
          return items.get(ix);
        }
      }
      return new NullItem();
    }
    //endregion
  }

  //endregion

  //region 502 NullItem
  class NullItem extends BasicItem {

  }
  //endregion

  //region 51 LineItem
  class LineItem extends BasicItem {

    LineItem() {
      title("");
    }

    LineItem stroke(int stroke) {
      this.stroke = stroke;
      return this;
    }

    LineItem color(Color color) {
      this.color = color;
      return this;
    }

    int stdStroke = 1;
    int stroke = 0;
    Color color = SXRED;

    class Line extends JComponent {

      boolean hori;

      Line(boolean col) {
        hori = col;
      }

      public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(stroke));
        g2.setColor(color);
        if (hori) {
          g2.draw(new Line2D.Float(0, 0, width, 0));
        } else
          g2.draw(new Line2D.Float(0, 0, 0, height));
      }
    }

    void fill(boolean horizontal, int lenW, int lenH) {
      if (horizontal) {
        width = lenW;
        height = stroke;
      } else {
        width = stroke;
        height = lenH;
      }
      Line line = new Line(horizontal);
      line.setPreferredSize(dim());
      comp(line); //Line
    }

    BasicItem create() {
      if (stroke == 0) {
        stroke = stdStroke;
      }
      height = stroke;
      width = stroke;
      return this;
    }
  }
  //endregion

  //region 511 Rectangle
  class RectItem extends BasicItem {

    RectItem() {
      title("");
    }

    RectItem stroke(int stroke) {
      this.stroke = stroke;
      return this;
    }

    RectItem color(Color color) {
      this.color = color;
      return this;
    }

    int stdStroke = 1;
    int stroke = 0;
    Color color = SXRED;

    class Rect extends JComponent {

      Rectangle rect;

      Rect(Rectangle rect) {
        this.rect = rect;
      }

      public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(stroke));
        g2.setColor(color);
        g2.draw(new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height));
      }
    }

    void fill(int w, int h) {
      if (box != null) {
        pos(box.pos());
        dim(box.dim());
        Rect rect = new Rect(new Rectangle(box.rect()));
        rect.setPreferredSize(box.dim());
        comp(rect); //Rect
      }
    }

    BoxItem box = null;

    BasicItem create(BoxItem box) {
      if (stroke == 0) {
        stroke = stdStroke;
      }
      this.box = box;
      return this;
    }
  }
  //endregion

  //region 52 TextItem
  private int stdFontSize = 14;

  void setFontSize(int val) {
    if (val > -1) {
      stdFontSize = val;
    }
  }

  class TextItem extends BasicItem {

    TextItem() {
    }

    TextItem(String aText) {
      title(aText);
    }

    BasicItem create() {
      JLabel lblText = new JLabel(title());
      Font font = new Font(fontName, fontBold, fontSize);
      Rectangle2D textLen = lblText.getFontMetrics(font).getStringBounds(title(), getGraphics());
      if (textLen.getWidth() > maxW) {
        fontSize = (int) (fontSize * maxW / textLen.getWidth());
        font = new Font(fontName, fontBold, fontSize);
        textLen = lblText.getFontMetrics(font).getStringBounds(title(), getGraphics());
      }
      if (underline) {
        lblText = new UnderlinedLabel(title(), font);
      }
      lblText.setFont(font);
      Rectangle r = new Rectangle(0, 0, (int) textLen.getWidth(), (int) textLen.getHeight());
      lblText.setBounds(r);
      dim(r.getSize());
      comp(lblText);
      return this;
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

    BasicItem create() {
      super.create();
      JLabel lblHtml = (JLabel) comp();
      Dimension size = dim();
      title("<html>" + title().replace("|", "<br>"));
      lblHtml.setText(title());
      dim(lblHtml.getPreferredSize());
      return this;
    }
  }
//endregion

  //region 521 Linkitem
  class LinkItem extends TextItem {
    String aLink = "https://sikulix.github.io";

    LinkItem(String text, String link) {
      title(text);
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

    ActionItem(String text, String[] options) {
      title(text.strip());
      aAction = options[0].strip();
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
          Debug.error("ActionItem: show: no dialog");
        }
      } else {
        Debug.error("ActionItem: not implemented: %s", aAction);
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

    OptionItem(String text, String[] options) {
      //TODO OptionItem
//      String action = options.length > 2 ? options[2] : "";
//      STATE state = options.length > 3 ? (options[2].toLowerCase().contains("on") ? STATE.ON : STATE.OFF) : STATE.OFF;
//      this.state = state;
//      aOption = option;
      title = text;
      title("(X) " + text);
      if (state.equals(STATE.ON)) {
        startState = STATE.ON;
      }
      setActive();
      bold();
      setBackground(BACKGROUNDCOLOR);
    }

    BasicItem create() {
      super.create();
      setStartState((JLabel) comp());
      return this;
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
      this(text, new String[0]);
    }

    ButtonItem(String text, String[] options) {
      title = text;
      if (options.length > 0)
        this.itemAction(new ItemAction(this, options[0].strip()));
      setActive();
      bold();
    }

    BasicItem create() {
      if (itemAction == null) {
        itemAction = standardItemAction(this);
      }
      JButton button = new JButton(title());
      button.setForeground(SXRED);
      dim(button.getPreferredSize());
      comp(button); //Button
      return this;
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

  BasicItem[] buttonItems(String text, String[] options) {
    String[] parts = null;
    parts = text.split("\\|");
    ButtonItem[] buttonItems = new ButtonItem[parts.length];
    int ix = 0;
    for (String part : parts) {
      String buttonTyp = part.strip();
      ButtonItem buttonItem = new ButtonItem(buttonTyp);
      globalButtons(buttonItem);
      buttonItems[ix] = buttonItem;
      ix++;
    }
    return buttonItems;
  }
//endregion

  //region 53 ImageItem
  class ImageItem extends BasicItem {
    BufferedImage img = null;

    ImageItem() {
      Debug.error("ImageItem: no image given");
    }

    ImageItem(URL url) {
      if (url != null) {
        try {
          img = ImageIO.read(url);
        } catch (IOException e) {
        }
      }
      if (img == null) {
        Debug.error("ImageItem: %s", url);
      }
    }

    //TODO Image resize
    public ImageItem resize(int width) {
      double factor = width / (double) img.getWidth();
      int height = (int) (img.getHeight() * factor);
      img = resizeImage(img, width, height);
      return this;
    }

    public ImageItem resize(int width, int height) {
      if (img == null || (width < 1 && height < 1)) {
        return this;
      }
      if (height < 0) {
        return resize(width);
      }
      if (width > 0 && height > 0) {
        img = resizeImage(img, width, height);
        return this;
      } else if (width > 0) {
        img = resizeImage(img, width, img.getHeight());
        return this;
      } else if (height > 0) {
        img = resizeImage(img, img.getWidth(), height);
        return this;
      }
      return this;
    }

    public ImageItem resize(double factor) {
      if (img == null || !(factor > 0)) {
        return this;
      }
      img = resizeImage(img, (int) (img.getWidth() * factor), (int) (img.getWidth() * factor));
      return this;
    }

    private BufferedImage resizeImage(BufferedImage img, int width, int height) {
      return Image.resizeImage(img, width, height);
    }

    BasicItem create() {
      JLabel lblimg = new JLabel();
      if (img != null) {
        lblimg.setIcon(new ImageIcon(img));
        dim(new Dimension(img.getWidth(), img.getHeight()));
      }
      comp(lblimg); //Image
      return this;
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
