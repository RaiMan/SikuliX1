package org.sikuli.script;

import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Static helper class for OCR.
 */

public class OCR {

  /**
   * OCR Engine modes:
   * 0    Tesseract Legacy only.
   * 1    LSTM only.
   * 2    LSTM + Legacy.
   * 3    Default, based on what is available.
   */
  public enum OcrEngineMode {
    TESSERACT_ONLY, // 0
    LSTM_ONLY, // 1
    TESSERACT_LSTM_COMBINED, // 2
    DEFAULT // 3
  }

  /**
   * Page segmentation modes:
   * 0    Orientation and script detection (OSD) only.
   * 1    Automatic page segmentation with OSD.
   * 2    Automatic page segmentation, but no OSD, or OCR.
   * 3    Fully automatic page segmentation, but no OSD. (Default)
   * 4    Assume a single column of text of variable sizes.
   * 5    Assume a single uniform block of vertically aligned text.
   * 6    Assume a single uniform block of text.
   * 7    Treat the image as a single text line.
   * 8    Treat the image as a single word.
   * 9    Treat the image as a single word in a circle.
   * 10    Treat the image as a single character.
   * 11    Sparse text. Find as much text as possible in no particular order.
   * 12    Sparse text with OSD.
   * 13    Raw line. Treat the image as a single text line, bypassing hacks that are Tesseract-specific.
   */
  public enum PageSegMode {
    OSD_ONLY, // 0
    AUTO_OSD, // 1
    AUTO_ONLY, // 2
    AUTO, // 3
    SINGLE_COLUMN, // 4
    SINGLE_BLOCK_VERT_TEXT, // 5
    SINGLE_BLOCK, // 6
    SINGLE_LINE, // 7
    SINGLE_WORD, // 8
    CIRCLE_WORD, // 9
    SINGLE_CHAR, // 10
    SPARSE_TEXT, // 11
    SPARSE_TEXT_OSD, // 12
    RAW_LINE // 13
  }

  private static Options globalOptions = Options.options();

  public static Options globalOptions() {
    return globalOptions;
  }

  public static class Options implements Cloneable {

    //<editor-fold desc="02 init, reset">
    public String toString() {
      String msg = String.format(
          "OCR.Options:" +
              "\ndata = %s" +
              "\nlanguage(%s) oem(%d) psm(%d) height(%.1f) factor(%.2f) dpi(%d/%d) %s",
          dataPath(), language(), oem(), psm(),
          textHeight(), factor(),
          Toolkit.getDefaultToolkit().getScreenResolution(), userDPI(), resizeInterpolation());
      if (hasVariablesOrConfigs()) {
        msg += "\n" + logVariablesConfigs();
      }
      return msg;
    }

    public Options() {
      reset();
    }

    public static Options options() {
      return new Options();
    }

    @Override
    public Options clone() {
      Options options = new Options();
      options.oem = oem;
      options.psm = psm;
      options.language = language;
      options.dataPath = dataPath;
      options.textHeight = textHeight;
      options.resizeInterpolation = resizeInterpolation;
      options.variables = new LinkedHashMap<>(variables);
      options.configs = new LinkedHashSet<>(configs);
      options.bestDPI = bestDPI;
      options.userDPI = userDPI;
      return options;
    }

    public Options reset() {
      oem = OcrEngineMode.DEFAULT.ordinal();
      psm = PageSegMode.AUTO.ordinal();
      language = Settings.OcrLanguage;
      dataPath = null;
      textHeight = getDefaultTextHeight();
      resizeInterpolation = Image.Interpolation.LINEAR;
      variables.clear();
      configs.clear();
      bestDPI = null;
      userDPI(TESSERACT_USER_DEFINED_DPI);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="10 oem">
    private int oem;

    public int oem() {
      return oem;
    }

    public Options oem(int oem) {
      if (oem < 0 || oem > 3) {
        throw new IllegalArgumentException(String.format("OCR: Invalid OEM %s (0 .. 3)", oem));
      }
      this.oem = oem;
      return this;
    }

    public Options oem(OcrEngineMode oem) {
      oem(oem.ordinal());
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="11 psm">
    private int psm;

    public int psm() {
      return psm;
    }

    public Options psm(int psm) {
      if (psm < 0 || psm > 13) {
        throw new IllegalArgumentException(String.format("OCR: Invalid PSM %s (0 .. 12)", psm));
      }

      if (psm == OCR.PageSegMode.OSD_ONLY.ordinal() || psm == OCR.PageSegMode.AUTO_OSD.ordinal()
          || psm == OCR.PageSegMode.SPARSE_TEXT_OSD.ordinal()) {
        if (!new File(dataPath(), "osd.traineddata").exists()) {
          throw new IllegalArgumentException(String.format("OCR: setPSM(%d): needs OSD, " +
              "but no osd.traineddata found in tessdata folder", psm));
        }
      }

      this.psm = psm;
      return this;
    }

    public Options psm(PageSegMode psm) {
      psm(psm.ordinal());
      return this;
    }

    /**
     * Sets this Options PSM to -1
     *
     * This causes Tess4J not to set the PSM at all.
     *
     * @return
     */
    public Options resetPSM() {
      psm = -1;
      return this;
    }

    /**
     * Configure Options in order to recognize a single line.
     *
     * @return this Options
     */
    public Options asLine() {
      return psm(PageSegMode.SINGLE_LINE);
    }

    /**
     * Configure Options in order to recognize a single word.
     *
     * @return this Options
     */
    public Options asWord() {
      return psm(PageSegMode.SINGLE_WORD);
    }

    /**
     * Configure Options in order to recognize a single character.
     *
     * @return this Options
     */
    public Options asChar() {
      return psm(PageSegMode.SINGLE_CHAR);
    }
    //</editor-fold>

    //<editor-fold desc="12 language">
    private String language;

    public String language() {
      return language;
    }

    public Options language(String language) {
      if (!new File(dataPath(), language + ".traineddata").exists()) {
        throw new SikuliXception(String.format("OCR: language: no %s.traineddata in %s", language, dataPath()));
      }
      this.language = language;
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="13 datapath">
    protected static String defaultDataPath = null;
    private String dataPath;

    public String dataPath() {
      if (dataPath == null) {
        return defaultDataPath;
      }
      return dataPath;
    }

    public Options dataPath(String dataPath) {
      if(!"tessdata".equals(new File(dataPath).getName())) {
        dataPath = new File(dataPath, "tessdata").getAbsolutePath();
      }
      this.dataPath = dataPath;
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="14 optimization">
    public Options smallFont() {
      textHeight(10);
      return this;
    }

    private static float getDefaultTextHeight() {
      Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
      try {
        Font font = g.getFont();
        FontMetrics fm = g.getFontMetrics(font);
        return fm.getLineMetrics("X", g).getHeight();
      } finally {
        g.dispose();
      }
    }

    private float textHeight;

    private static final int OPTIMAL_X_HEIGHT = 30;

    public float textHeight() {
      return textHeight;
    }

    public Options textHeight(float height) {
      textHeight = height;
      return this;
    }

    public Options setFontSize(int size) {
      Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
      try {
        Font font = new Font(g.getFont().getFontName(), 0, size);
        FontMetrics fm = g.getFontMetrics(font);
        textHeight(fm.getLineMetrics("X", g).getHeight());
        return this;
      } finally {
        g.dispose();
      }
    }

    private Image.Interpolation resizeInterpolation;

    public Image.Interpolation resizeInterpolation() {
      return resizeInterpolation;
    }

    public Options resizeInterpolation(Image.Interpolation method) {
      resizeInterpolation = method;
      return this;
    }

    private Float bestDPI = null;

    public Options bestDPI(int dpi) {
      bestDPI = (float) dpi;
      return this;
    }

    private static final int TESSERACT_USER_DEFINED_DPI = 300;
    private int userDPI;

    private int userDPI() {
      return userDPI;
    }

    public Options userDPI(int dpi) {
      if (dpi == 0) {
        dpi = Toolkit.getDefaultToolkit().getScreenResolution();
      }
      if (dpi < 70 || dpi > 2400) {
        throw new IllegalArgumentException(String.format("OCR: Invalid user DPI: %s (must be 70 .. 2400)", dpi));
      }
      userDPI = dpi;
      variable("user_defined_dpi", Integer.toString(userDPI()));
      return this;
    }

    public float factor() {
      // LEGACY: Calculate the resize factor based on the optimal and
      // calculated DPI value if optimumDPI has been set manually
      if (bestDPI != null) {
        return bestDPI / Toolkit.getDefaultToolkit().getScreenResolution();
      }
      return OPTIMAL_X_HEIGHT / textHeight;
    }
    //</editor-fold>

    //<editor-fold desc="15 variables">
    private Map<String, String> variables = new LinkedHashMap<>();

    public Map<String, String> variables() {
      return variables;
    }

    public Options variable(String key, String value) {
      variables.put(key, value);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="16 configs">
    private Set<String> configs = new LinkedHashSet<>();

    public List<String> configs() {
      return new ArrayList<>(configs);
    }

    public Options configs(String... configs) {
      configs(Arrays.asList(configs));
      return this;
    }

    public Options configs(List<String> configs) {
      this.configs = new LinkedHashSet<>(configs);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="20 helpers">
    protected boolean hasVariablesOrConfigs() {
      return !configs.isEmpty() || !variables.isEmpty();
    }

    public String logVariablesConfigs() {
      String logConfigs = "";
      if (!logConfigs.isEmpty()) {
        logConfigs = "configs: " + logConfigs;
      }
      String logVariables = "";
      for (String key : variables.keySet()) {
        if (!logVariables.isEmpty()) {
          logVariables += ",";
        }
        logVariables += key + ":" + variables.get(key);
      }
      if (!logVariables.isEmpty()) {
        logVariables = "variables: " + logVariables;
      }
      return (logConfigs + logVariables).trim();
    }
    //</editor-fold>
  }
  
  /**
   * Creates a new TextRecognizer instance using the global options.
   */
  public static TextRecognizer start() {
    return start(globalOptions());
  }
  
  /**
   * Creates a new TextRecognizer instance.
   * 
   * @param options
   */
  public static TextRecognizer start(Options options) {
    return TextRecognizer.start(options);
  }  

  /**
   * Reads text from the given source.
   *
   * Uses the global options.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @return text
   */
  public static <SFIRBS> String readText(Object from) {
    return readText(from, globalOptions());
  }

  /**
   * Reads text from the given source.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @param options options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readText(SFIRBS from, Options options) {
    TextRecognizer tr = TextRecognizer.start(options);
    return tr.readText(from);
  }

  /**
   * Reads text from the given source assuming the source
   * contains a single line of text.
   *
   * Uses the global options.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @return text
   */
  public static <SFIRBS> String readLine(SFIRBS from) {
    return readLine(from, globalOptions());
  }

  /**
   * Reads text from the given source assuming the source
   * contains a single line of text.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @param options options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readLine(SFIRBS from, Options options) {
    return readText(from, options.clone().asLine());
  }

  /**
   * Reads text from the given source assuming the source
   * contains a single word.
   *
   * Uses the global options.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @return text
   */
  public static <SFIRBS> String readWord(SFIRBS from) {
    return readWord(from, globalOptions());
  }

  /**
   * Reads text from the given source assuming the source
   * contains a single word.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @param options options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readWord(SFIRBS from, Options options) {
    return readText(from, options.clone().asWord());
  }

  /**
   * Reads text from the given source assuming the source
   * contains a single character.
   *
   * Uses the global options.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @return text
   */
  public static <SFIRBS> String readChar(SFIRBS from) {
    return readChar(from, globalOptions());
  }

  /**
   * Reads text from the given source assuming the source
   * contains a single character.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @param options options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readChar(SFIRBS from, Options options) {
    return readText(from, options.clone().asChar());
  }

  /**
   * Read text and return a list of lines.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @return lines
   */
  public static <SFIRBS> List<Match> readLines(SFIRBS from) {
    return readLines(from, globalOptions());
  }

  /**
   * Read text and return a list of lines.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @param options options for the used TextRecognizer
   * @return lines
   */
  public static <SFIRBS> List<Match> readLines(SFIRBS from, Options options) {
    TextRecognizer tr = TextRecognizer.start(options);
    return tr.readLines(from);
  }

  /**
   *  Read text and return a list of words.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @return words
   */
  public static <SFIRBS> List<Match> readWords(SFIRBS from) {
    return readWords(from, OCR.globalOptions());
  }

  /**
   * Read text and return a list of words.
   *
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from source to read text from
   * @param options options for the used TextRecognizer
   * @return words
   */
  public static <SFIRBS> List<Match> readWords(SFIRBS from, Options options) {
    TextRecognizer tr = TextRecognizer.start(options);
    return tr.readWords(from);
  }
}
