/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import java.awt.*;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.text.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import org.sikuli.script.Runner;

public class EditorViewFactory implements ViewFactory {

	private String sikuliContentType;

  @Override
  public View create(Element elem) {
    String kind = elem.getName();
    Debug.log(6, "ViewCreate: " + kind);
    if (kind != null) {
      if (kind.equals(AbstractDocument.ContentElementName)) {
        return new SyntaxHighlightLabelView(elem, sikuliContentType);
      } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
        return new LineBoxView(elem, View.X_AXIS);
      } else if (kind.equals(AbstractDocument.SectionElementName)) {
        return new SectionBoxView(elem, View.Y_AXIS);
      } else if (kind.equals(StyleConstants.ComponentElementName)) {
        return new ButtonView(elem);
      } else if (kind.equals(StyleConstants.IconElementName)) {
        return new IconView(elem);
      }
    }
    // default to text display
    return new LabelView(elem);
  }

	public void setContentType(String ct) {
		sikuliContentType = ct;
	}
}

//<editor-fold defaultstate="collapsed" desc="Section">
class SectionBoxView extends BoxView {

  public SectionBoxView(Element elem, int axis) {
    super(elem, axis);
  }

  @Override
  protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    super.layoutMajorAxis(targetSpan, axis, offsets, spans);
    int count = getViewCount();
    if (count == 0) {
      return;
    }
    int offset = 0;
    offsets[0] = 0;
    spans[0] = (int) getView(0).getMinimumSpan(View.Y_AXIS);
    for (int i = 1; i < count; i++) {
      View view = getView(i);
      spans[i] = (int) view.getMinimumSpan(View.Y_AXIS);
      offset += spans[i - 1];
      offsets[i] = offset;
    }
    return;
  }

  @Override
  protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    super.layoutMinorAxis(targetSpan, axis, offsets, spans);
    int count = getViewCount();
    for (int i = 0; i < count; i++) {
      offsets[i] = 0;
    }
 }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Line">
class LineBoxView extends BoxView {

  public LineBoxView(Element elem, int axis) {
    super(elem, axis);
  }

  // for Utilities.getRowStart
  @Override
  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    Rectangle r = a.getBounds();
    View v = getViewAtPosition(pos, r);
    if ((v != null) && (!v.getElement().isLeaf())) {
      // Don't adjust the height if the view represents a branch.
      return super.modelToView(pos, a, b);
    }
    int height = r.height;
    int y = r.y;
    Shape loc = super.modelToView(pos, a, b);
    r = loc.getBounds();
    r.height = height;
    r.y = y;
    return r;
   }

  @Override
  protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    super.layoutMinorAxis(targetSpan, axis, offsets, spans);
    int maxH = 0;
    for (int i = 0; i < spans.length; i++) {
      if (spans[i] > maxH) {
        maxH = spans[i];
      }
    }
    for (int i = 0; i < offsets.length; i++) {
      offsets[i] = (maxH - spans[i]) / 2;
    }
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Highlight">
class SyntaxHighlightLabelView extends LabelView {

  static FontMetrics _fMetrics = null;
  static String tabStr = nSpaces(PreferencesUser.getInstance().getTabWidth());

  private static Map<Pattern, Color> patternColors;
  private static Map<Pattern, Color> patternColorsPython;
  private static Map<Pattern, Color> patternColorsRuby;
  private static Map<Pattern, Color> patternColorsSikuli;
  private static Font fontParenthesis;

  //<editor-fold defaultstate="collapsed" desc="keyword lists">
  private static String[] keywordsPython = {
    "and", "del", "for", "is", "raise",
    "assert", "elif", "from", "lambda", "return",
    "break", "else", "global", "not", "try",
    "class", "except", "if", "or", "while",
    "continue", "exec", "import", "pass", "yield",
    "def", "finally", "in", "print", "with", "self"
  };
  private static String[] keywordsRuby = {
    "return", "break", "else", "class", "if", "nil", "begin", "end", "rescue",
    "next", "def", "puts", "java_import", "eval", "then", "alias", "and",
		"case", "defined?", "do", "elsif", "ensure", "false", "for", "in", "module",
		"not", "or", "redo", "retry", "self", "super", "true", "undef", "unless",
		"until", "when", "while", "yield", "BEGIN", "END", "__ENCODING__", "__END__",
		"__FILE__", "__LINE__", "require"
  };
  private static String[] keywordsSikuliClass = {
    "Screen", "Region", "Location", "Match", "Pattern",
    "Env", "Key", "Button", "Finder",
    "App", "KeyModifier", "Mouse", "Keys", "Image", "ImagePath", "ImageGroup",
		"ImageFind", "ImageFinder", "Settings",
  };
  private static String[] keywordsSikuli = {
    "find", "wait", "findAll", "findText", "findAllText",
    "waitVanish", "exists", "text",
    "click", "doubleClick", "rightClick", "hover", "wheel", "delayClick",
    "type", "paste", "write", "delayType",
    "dragDrop", "drag", "dropAt",
    "mouseMove", "mouseDown", "mouseUp",
    "keyDown", "keyUp",
    "onAppear", "onVanish", "onChange",
    "observe", "observeInBackground", "stopObserver", "isObserving",
    "popup", "input", "sleep", "run", "runScript",
    "switchApp", "openApp", "closeApp", "use", "useRemote", "ucode", "load",
    "capture", "selectRegion",
    "getOS", "getMouseLocation", "exit",
    //Region
    "right", "rightAt", "left", "leftAt", "above", "aboveAt", "below", "belowAt",
    "nearby", "inside", "grow", "union", "intersection",
    "getScreen", "getCenter", "setCenter", "setSize", "setLocation",
    "setX", "setY", "setW", "setH", "setRect", "setROI",
    "getX", "getY", "getW", "getH", "getRect", "getROI",
    "highlight", "add", "getLastScreenImageFile",
    "getNumberScreens", "getBounds",
    "contains", "containsMouse", "atMouse",
    "getTopLeft", "setTopLeft", "getTopRight", "setTopRight",
    "getBottomLeft", "setBottomLeft", "getBottomRight", "setBottomRight",
    "get", "setRows", "getRows", "setCols", "setCols", "getRowH", "getColW",
    "setRaster", "getRow", "getCol", "getCell",
    "getImage",
    //Event
    "repeat",
    //Pattern
    "similar", "targetOffset", "getLastMatch", "getLastMatches",
    "getTargetOffset", "getFilename",
    //global
    "setAutoWaitTimeout", "setBundlePath", "setShowActions", "setThrowException",
    "getAutoWaitTimeout", "getBundlePath", "getShowActions", "getThrowException",
    "highlightOff",
    "setFindFailedResponse", "getFindFailedResponse",
    "setWaitScanRate", "getWaitScanRate",
    "setObserveScanRate", "getObserveScanRate",
    "setWaitForVanish", "getWaitForVanish",
    "showScreens", "resetScreens", "showMonitors", "resetMonitors",
    "hasNext", "next", "destroy", "exact", "offset",
    "getOSVersion", "getScore", "getTarget",
    "getClipboard",
    "addImagePath", "removeImagePath", "getImagePath", "addImportPath", "resetImagePath",
    "getParentPath", "makePath",
    //App class
    "open", "close", "focus", "window", "focusedWindow",};
  private static String[] constantsSikuli = {
    "FOREVER",
    "KEY_SHIFT", "KEY_CTRL", "KEY_META", "KEY_ALT", "KEY_CMD", "KEY_WIN",
    "ENTER", "BACKSPACE", "TAB", "ESC", "UP", "RIGHT", "DOWN", "LEFT",
    "PAGE_UP", "PAGE_DOWN", "DELETE", "END", "HOME", "INSERT", "F1",
    "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
    "F13", "F14", "F15", "SHIFT", "CTRL", "ALT", "META", "CMD", "WIN",
    "SCREEN", "MIDDLE",
    "WHEEL_UP", "WHEEL_DOWN",
    "PRINTSCREEN", "SCROLL_LOCK", "PAUSE", "CAPS_LOCK", "NUM0",
    "NUM1", "NUM2", "NUM3", "NUM4", "NUM5", "NUM6", "NUM7", "NUM8", "NUM9",
    "SEPARATOR", "NUM_LOCK", "ADD", "MINUS", "MULTIPLY", "DIVIDE"
  };

	private String sikuliContentType;
  //</editor-fold>

  static {
    PreferencesUser.getInstance().addPreferenceChangeListener(
            new PreferenceChangeListener() {
              @Override
              public void preferenceChange(PreferenceChangeEvent event) {
                //TODO: need to reposition images
                if (event.getKey().equals("TAB_WIDTH")) {
                  tabStr = nSpaces(Integer.parseInt(event.getNewValue()));
                }
              }
            });
    fontParenthesis = new Font("Osaka-Mono", Font.PLAIN, 30);

    // NOTE: the order is important!
		patternColors = new HashMap<Pattern, Color>();
    patternColorsPython = new HashMap<Pattern, Color>();
    patternColorsRuby = new HashMap<Pattern, Color>();
    patternColorsSikuli = new HashMap<Pattern, Color>();
    patternColors.put(Pattern.compile("(#:.*$)"), new Color(220, 220, 220));
    patternColors.put(Pattern.compile("(#.*$)"), new Color(138, 140, 193));
    patternColors.put(Pattern.compile("(\"[^\"]*\"?)"), new Color(128, 0, 0));
    patternColors.put(Pattern.compile("(\'[^\']*\'?)"), new Color(128, 0, 0));
    patternColors.put(Pattern.compile("\\b([0-9]+)\\b"), new Color(128, 64, 0));
		patternColorsPython.putAll(patternColors);
		patternColorsRuby.putAll(patternColors);
    for (int i = 0; i < keywordsPython.length; i++) { patternColorsPython.put(Pattern.compile(
              "\\b(" + keywordsPython[i] + ")\\b"), Color.blue);
    }
    for (int i = 0; i < keywordsRuby.length; i++) { patternColorsRuby.put(Pattern.compile(
              "\\b(" + keywordsRuby[i] + ")\\b"), Color.blue);
    }
    for (int i = 0; i < keywordsSikuli.length; i++) { patternColorsSikuli.put(Pattern.compile(
              "\\b(" + keywordsSikuli[i] + ")\\b"), new Color(63, 127, 127));
    }
    for (int i = 0; i < keywordsSikuliClass.length; i++) { patternColorsSikuli.put(Pattern.compile(
              "\\b(" + keywordsSikuliClass[i] + ")\\b"), new Color(215, 41, 56));
    }
    for (int i = 0; i < constantsSikuli.length; i++) { patternColorsSikuli.put(Pattern.compile(
              "\\b(" + constantsSikuli[i] + ")\\b"), new Color(128, 64, 0));
    }
		patternColorsPython.putAll(patternColorsSikuli);
		patternColorsRuby.putAll(patternColorsSikuli);
		patternColorsSikuli = null;
  }

	public SyntaxHighlightLabelView(Element elm, String contentType) {
		super(elm);
		sikuliContentType = contentType;
		if (Runner.CPYTHON.equals(sikuliContentType)) {
			patternColors = patternColorsPython;
		} else if (Runner.CRUBY.equals(sikuliContentType)) {
			patternColors = patternColorsRuby;
		}
	}

	private static String nSpaces(int n) {
    char[] s = new char[n];
    Arrays.fill(s, ' ');
    return new String(s);
  }

  //<editor-fold defaultstate="collapsed" desc="length of line in view">
  @Override
  public float getMinimumSpan(int axis) {
    float f = super.getMinimumSpan(axis);
    if (axis == View.X_AXIS) {
      f = tabbedWidth();
    }
    return f;
  }

  @Override
  public float getMaximumSpan(int axis) {
    float f = super.getMaximumSpan(axis);
    if (axis == View.X_AXIS) {
      f = tabbedWidth();
    }
    return f;
  }

  @Override
  public float getPreferredSpan(int axis) {
    float f = super.getPreferredSpan(axis);
    if (axis == View.X_AXIS) {
      f = tabbedWidth();
    }
    return f;
  }

  private float tabbedWidth() {
    String str = getText(getStartOffset(), getEndOffset()).toString();
    int tab = countTab(str);
    if (Settings.isMac()) {
      return stringWidth(str) + getRealTabWidth() * tab;
    } else {
      return stringWidth(str) + getTabWidth() * tab;
    }
  }

  private int countTab(String str) {
    int pos = -1;
    int count = 0;
    while ((pos = str.indexOf('\t', pos + 1)) != -1) {
      count++;
    }
    return count;
  }

  private int stringWidth(String str) {
    if (_fMetrics == null) {
      _fMetrics = getGraphics().getFontMetrics();
    }
    return _fMetrics.stringWidth(str);
  }

  private float getRealTabWidth() {
    final int tabCharWidth;
    if (Settings.isMac()) {
      tabCharWidth = stringWidth("\t");
    } else {
      tabCharWidth = stringWidth(" ");
    }
    return getTabWidth() - tabCharWidth /* + 1f */; //still buggy
  }

  private int getTabWidth() {
    return stringWidth(tabStr);
  }
  //</editor-fold>

  @Override
  public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
    bias[0] = Position.Bias.Forward;

    Debug.log(9, "viewToModel: " + fx + " " + fy);
    String str = getText(getStartOffset(), getEndOffset()).toString();

    int left = getStartOffset(), right = getEndOffset();
    int pos = 0;
    while (left < right) {
      Debug.log(9, "viewToModel: " + left + " " + right + " " + pos);
      pos = (left + right) / 2;
      try {
        Shape s = modelToView(pos, a, bias[0]);
        float sx = s.getBounds().x;
        if (sx > fx) {
          right = pos;
        } else if (sx < fx) {
          left = pos + 1;
        } else {
          break;
        }
      } catch (BadLocationException ble) {
        break;
      }
    }
    pos = left - 1 >= getStartOffset() ? left - 1 : getStartOffset();
    try {
      Debug.log(9, "viewToModel: try " + pos);
      Shape s1 = modelToView(pos, a, bias[0]);
      Shape s2 = modelToView(pos + 1, a, bias[0]);
      if (Math.abs(s1.getBounds().x - fx) <= Math.abs(s2.getBounds().x - fx)) {
        return pos;
      } else {
        return pos + 1;
      }
    } catch (BadLocationException ble) {
    }
    return pos;
  }

  @Override
  public Shape modelToView(int pos, Shape a, Position.Bias b)
          throws BadLocationException {

    int start = getStartOffset(), end = getEndOffset();
    Debug.log(9, "[modelToView] start: " + start
            + " end: " + end + " pos:" + pos);
    String strHead = getText(start, pos).toString();
    String strTail = getText(pos, end).toString();
    Debug.log(9, "[modelToView] [" + strHead + "]-pos-[" + strTail + "]");
    int tabHead = countTab(strHead), tabTail = countTab(strTail);
    Debug.log(9, "[modelToView] " + tabHead + " " + tabTail);
    Shape s = super.modelToView(pos, a, b);
    Rectangle ret = s.getBounds();
    Debug.log(9, "[modelToView] super.bounds: " + ret);
    if (pos != end) {
      ret.x += tabHead * getRealTabWidth();
    }
    //ret.width += tabTail*tabWidth;
    Debug.log(9, "[modelToView] new bounds: " + ret);
    return ret;
  }

  //<editor-fold defaultstate="collapsed" desc="overwritten paint">
  @Override
  public void paint(Graphics g, Shape shape) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    //super.paint(g, shape); // for drawing selection

    String text = getText(getStartOffset(), getEndOffset()).toString();
    //System.out.println("draw " + text);

    SortedMap<Integer, Integer> posMap = new TreeMap<Integer, Integer>();
    SortedMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();
    buildColorMaps(text, posMap, colorMap);

    if (_fMetrics == null) {
      _fMetrics = g2d.getFontMetrics();
    }
    Rectangle alloc = (shape instanceof Rectangle)
            ? (Rectangle) shape : shape.getBounds();

    int sx = alloc.x;
    int sy = alloc.y + alloc.height - _fMetrics.getDescent();
    int i = 0;

    for (Map.Entry<Integer, Integer> entry : posMap.entrySet()) {
      int start = entry.getKey();
      int end = entry.getValue();

      if (i <= start) {
        g2d.setColor(Color.black);
        String str = text.substring(i, start);
        sx = drawString(g2d, str, sx, sy);
      } else {
        break;
      }

      g2d.setColor(colorMap.get(start));
      i = end;
      String str = text.substring(start, i);
      /*
       * if( str.equals("(") || str.equals(")") )
       * sx = drawParenthesis(g2d, str, sx, sy);
       * else
       */
      sx = drawString(g2d, str, sx, sy);
    }

    // Paint possible remaining text black
    if (i < text.length()) {
      g2d.setColor(Color.black);
      String str = text.substring(i, text.length());
      drawString(g2d, str, sx, sy);
    }

  }

  int drawString(Graphics2D g2d, String str, int x, int y) {
    if (str.length() == 0) {
      return x;
    }
    int tabPos = str.indexOf('\t');
    if (tabPos != -1) {
      x = drawString(g2d, str.substring(0, tabPos), x, y);
      x = drawTab(g2d, x, y);
      x = drawString(g2d, str.substring(tabPos + 1), x, y);
    } else {
      g2d.drawString(str, x, y);
      x += stringWidth(str);
    }
    return x;
  }

  int drawTab(Graphics2D g2d, int x, int y) {
    return drawString(g2d, tabStr, x, y);
  }

  int drawParenthesis(Graphics2D g2d, String str, int x, int y) {
    Font origFont = g2d.getFont();
    g2d.setFont(fontParenthesis);
    g2d.drawString(str, x, y);
    x += g2d.getFontMetrics().stringWidth(str);
    g2d.setFont(origFont);
    return x;
  }

  void buildColorMaps(String text, Map<Integer, Integer> posMap,
          Map<Integer, Color> colorMap) {

    // Match all regexes on this snippet, store positions
    for (Map.Entry<Pattern, Color> entry : patternColors.entrySet()) {

      Matcher matcher = entry.getKey().matcher(text);

      while (matcher.find()) {
        posMap.put(matcher.start(1), matcher.end());
        colorMap.put(matcher.start(1), entry.getValue());
      }
    }
  }

  //</editor-fold>

}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Button">
class ButtonView extends ComponentView {

  public ButtonView(Element elem) {
    super(elem);
    Debug.log(6,"ViewCreate: Button");
  }

  @Override
  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    return super.modelToView(pos, a, b);
  }

  @Override
  public void paint(Graphics g, Shape shape) {
    JComponent comp = (JComponent) getComponent();
    Rectangle alloc = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
  }
}
//</editor-fold>
