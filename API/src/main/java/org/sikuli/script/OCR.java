/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import net.sourceforge.tess4j.ITesseract;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Static helper class for OCR via Tess4J/Tesseract.
 * <p>
 * The methods in this class are not threadsafe.
 * @see <a href="https://sikulix-2014.readthedocs.io/en/latest/textandocr.html">SikuliX docs: Text and OCR</a>
 */
public class OCR {

  public static final Class classTesseract = ITesseract.class;
  public static final String classTesseractName = classTesseract.getName();

  //<editor-fold desc="02 housekeeping">

  /**
   * OCR Engine modes.
   * <pre>
   * 0  TESSERACT_ONLY  Tesseract Legacy only.
   * 1  LSTM_ONLY       LSTM only.
   * 2  TESSERACT_LSTM_COMBINED  LSTM + Legacy.
   * 3  DEFAULT         Default, based on what is available. (DEFAULT)
   * </pre>
   */
  public enum OEM {
    TESSERACT_ONLY, // 0
    LSTM_ONLY, // 1
    TESSERACT_LSTM_COMBINED, // 2
    DEFAULT // 3
  }

  /**
   * Page segmentation modes.
   * <pre>
   * 0  OSD_ONLY   Orientation and script detection (OSD) only.
   * 1  AUTO_OSD   Automatic page segmentation with OSD.
   * 2  AUTO_ONLY  Automatic page segmentation, but no OSD, or OCR.
   * 3  AUTO       Fully automatic page segmentation, but no OSD. (Default)
   * 4  SINGLE_COLUMN  Assume a single column of text of variable sizes.
   * 5  SINGLE_BLOCK_VERT_TEXT  Assume a single uniform block of vertically aligned text.
   * 6  SINGLE_COLUMN  Assume a single uniform block of text.
   * 7  SINGLE_LINE    Treat the image as a single text line.
   * 8  SINGLE_WORD    Treat the image as a single word.
   * 9  CIRCLE_WORD    Treat the image as a single word in a circle.
   * 10  SINGLE_CHAR   Treat the image as a single character.
   * 11  SPARSE_TEXT      Sparse text. Find as much text as possible in no particular order.
   * 12  SPARSE_TEXT_OSD  Sparse text with OSD.
   * 13  RAW_LINE         Raw line. Treat the image as a single text line, bypassing hacks that are Tesseract-specific.
   * </pre>
   */
  public enum PSM {
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

  /**
   * INTERNAL: Tesseract option.
   */
  protected static final int PAGE_ITERATOR_LEVEL_WORD = 3;
  /**
   * INTERNAL: Tesseract option.
   */
  protected static final int PAGE_ITERATOR_LEVEL_LINE = 2;
  //</editor-fold>

  //<editor-fold desc="05 options">
  private static Options options = new Options();

  /**
   * access/get the current global Options (Singleton).
   *
   * @return the global Options
   */
  public static Options globalOptions() {
    if (null != Settings.OcrLanguage && !Settings.OcrLanguage.equals("eng")) {
      options.language(Settings.OcrLanguage);
    }
    return options;
  }

  /**
   * A container for the options relevant for using {@link OCR} on
   * {@link Region}s or {@link Image}s.
   * <p>Use OCR.{@link #Options()} to get a new option set</p>
   * <p>use OCR.{@link #globalOptions()} to access the global options</p>
   * <p>
   * In case you have to consult the Tesseract docs
   * @see <a href="https://github.com/tesseract-ocr/tesseract/wiki/Documentation">Tesseract docs</a>
   */
  public static class Options implements Cloneable {

    //<editor-fold desc="02 init, reset">

    /**
     * create a new Options set from the initial defaults settings.
     * <p>
     * about the default settings see {@link #reset()}
     */
    public Options() {
      reset();
    }

    /**
     * makes a copy of this Options
     * @return new Options as copy
     */
    @Override
    public Options clone() {
      Options options = new Options();
      options.oem = oem;
      options.psm = psm;
      options.language = language;
      options.dataPath = dataPath;
      options.isLightFont = isLightFont;
      options.textHeight = textHeight;
      options.resizeInterpolation = resizeInterpolation;
      options.variables = new LinkedHashMap<>(variables);
      options.configs = new LinkedHashSet<>(configs);
      options.bestDPI = bestDPI;
      options.userDPI = userDPI;
      return options;
    }

    /**
     * resets this Options set to the initial defaults.
     * <pre>
     * oem = OcrEngineMode.DEFAULT.ordinal();
     * psm = PageSegMode.AUTO.ordinal();
     * language = Settings.OcrLanguage;
     * dataPath = null; //(see comment)
     * textHeight = getDefaultTextHeight();
     * variables.clear();
     * configs.clear();
     * </pre>
     * comment on <b>dataPath==null</b>: dataPath will be evaluated at the next use of an OCR feature
     * to the SikuliX default or Settings.OcrDataPath (if set)
     *
     * @return this
     */
    public Options reset() {
      oem = OEM.DEFAULT.ordinal();
      psm = PSM.AUTO.ordinal();
      language = "eng";
      dataPath = null;
      isLightFont = false;
      textHeight = getDefaultTextHeight();
      resizeInterpolation = Image.Interpolation.LINEAR;
      variables.clear();
      configs.clear();
      bestDPI = null;
      userDPI(TESSERACT_USER_DEFINED_DPI);
      return this;
    }

    /**
     * Current state of this Options as some formatted lines of text.
     * <pre>
     * OCR.Options:
     * data = ...some-path.../tessdata
     * language(eng) oem(3) psm(3) height(15,1) factor(1,99) dpi(96)
     * configs: conf1, conf2, ...
     * variables: key:value, ...
     * </pre>
     * @return a text string as before
     */
    public String toString() {
      String light = isLightFont() ? "light" : "";
      String msg = String.format(
              "OCR.Options:" +
                      "\ndata = %s" +
                      "\nlanguage(%s) oem(%d) psm(%d) height(%.1f) factor(%.2f) dpi(%d) %s",
              dataPath(), language(), oem(), psm(),
              textHeight(), factor(),
              Toolkit.getDefaultToolkit().getScreenResolution(), light);
      if (hasVariablesOrConfigs()) {
        msg += "\n" + logVariablesConfigs();
      }
      return msg;
    }

    /**
     * INTERNAL: validates this Options before OCR usage.
     */
    protected void validate() {
      String languageFile = language() + ".traineddata";
      String dataPath = dataPath();
      if (!new File(dataPath, languageFile).exists()) {
        throw new SikuliXception(String.format("OCR: no %s.traineddata in %s",
            language(), dataPath));
      }
    }
    //</editor-fold>

    //<editor-fold desc="10 oem">
    private int oem;

    /**
     * get this OEM.
     *
     * @return oem as int
     * @see OEM
     */
    public int oem() {
      return oem;
    }

    /**
     * set this OEM.
     *
     * @param oem as int
     * @return this Options
     * @see OEM
     */
    public Options oem(int oem) {
      if (oem < 0 || oem > 3) {
        throw new IllegalArgumentException(String.format("OCR: Invalid OEM %s (0 .. 3)", oem));
      }
      this.oem = oem;
      return this;
    }

    /**
     * set this OEM.
     *
     * @param oem as enum constant
     * @return this Options
     * @see OEM
     */
    public Options oem(OEM oem) {
      oem(oem.ordinal());
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="11 psm">
    private int psm;

    /**
     * get this PSM.
     *
     * @return psm as int
     * @see PSM
     */
    public int psm() {
      return psm;
    }

    /**
     * set this PSM.
     *
     * @param psm as int
     * @return this Options
     * @see PSM
     */
    public Options psm(int psm) {
      if (psm < 0 || psm > 13) {
        throw new IllegalArgumentException(String.format("OCR: Invalid PSM %s (0 .. 12)", psm));
      }

      if (psm == PSM.OSD_ONLY.ordinal() || psm == PSM.AUTO_OSD.ordinal()
              || psm == PSM.SPARSE_TEXT_OSD.ordinal()) {
        if (!new File(dataPath(), "osd.traineddata").exists()) {
          throw new IllegalArgumentException(String.format("OCR: setPSM(%d): needs OSD, " +
                  "but no osd.traineddata found in tessdata folder", psm));
        }
      }

      this.psm = psm;
      return this;
    }

    /**
     * set this PSM.
     *
     * @param psm as enum constant
     * @return this Options
     * @see PSM
     */
    public Options psm(PSM psm) {
      psm(psm.ordinal());
      return this;
    }

    /**
     * Sets this PSM to -1.
     * <p>
     * This causes Tess4J not to set the PSM at all.
     * <br>Only use it, if you know what you are doing.
     *
     * @return this Options
     */
    public Options resetPSM() {
      psm = -1;
      return this;
    }

    /**
     * Configure Options to recognize a single line.
     *
     * @return this Options
     */
    public Options asLine() {
      return psm(PSM.SINGLE_LINE);
    }

    /**
     * Configure Options to recognize a single word.
     *
     * @return this Options
     */
    public Options asWord() {
      return psm(PSM.SINGLE_WORD);
    }

    /**
     * Configure Options to recognize a single character.
     *
     * @return this Options
     */
    public Options asChar() {
      return psm(PSM.SINGLE_CHAR);
    }
    //</editor-fold>

    //<editor-fold desc="12 language">
    private String language;

    /**
     * get the cutrrent language
     * @return the language short string
     * @see #language(String)
     */
    public String language() {
      return language;
    }

    /**
     * Set the language short string.
     * <p>(must not be null or empty,
     * see {@link Settings#OcrLanguage} for a useable fallback)</p>
     * <p>According to the Tesseract rules this is a 3-lowercase-letters string
     * like eng, deu, fra, rus, ....</p>
     * <p>For special cases it might be something like xxx_yyy (chi_sim)
     * or even xxx_yyyy (deu_frak) or even xxx_yyy_zzzz (chi_tra_vert), but always all lowercase.</p>
     * <p>Take care that you have the corresponding ....traineddata file in the datapath/tessdata folder
     * latest at time of OCR feature usage</p>
     * @see <a href="https://github.com/tesseract-ocr/tessdata">Tesseract language files</a>
     * @param language the language string
     * @return this Options
     */
    public Options language(String language) {
      if (language == null || language.isEmpty()) {
        throw new IllegalArgumentException(String.format("OCR: Invalid language %s", language));
      }
      //TODO check language string (RegEx?)
      this.language = language;
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="13 datapath">
    protected static String defaultDataPath = null;
    private String dataPath;

    /**
     * get the current datapath in this Options.
     * <p>might be null, if no OCR feature was used until now</p>
     * <p>if null, it will be evaluated at time of OCR feature usage to the default
     * SikuliX path or to Settings.OcrDataPath (if set)</p>
     * @return the current Tesseract datapath in this Options
     */
    public String dataPath() {
      if (dataPath == null) {
        return defaultDataPath;
      }
      return dataPath;
    }

    /**
     * Set folder for Tesseract to find language and configs files.
     * <p>in the tessdata subfolder (the path spec might be given without the trailing /tessdata)</p>
     * <p><b>TAKE CARE,</b> that all is in place at time of OCR feature usage</p>
     * <p><b>if null,</b> it will be evaluated at time of OCR feature usage to the default
     * SikuliX path or to Settings.OcrDataPath (if set)</p>
     * @see #language(String)
     * @param dataPath the absolute filename string
     * @return this Options
     */
    public Options dataPath(String dataPath) {
      if (dataPath != null) {
        if (!"tessdata".equals(new File(dataPath).getName())) {
          dataPath = new File(dataPath, "tessdata").getAbsolutePath();
        }
      }
      this.dataPath = dataPath;
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="14 optimization">
    private boolean isLightFont = false;

    public Options lightFont() {
      isLightFont = true;
      return this;
    }

    public boolean isLightFont() {
      return isLightFont;
    }

    /**
     * Convenience: Configure the Option's optimization.
     * <p>
     * Might give better results in cases with small
     * fonts with a pixel height lt 12 (font sizes lt 10)
     * @return this Options
     */
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

    /**
     * current base for image optimization before OCR.
     * @return value
     * @see #textHeight(float)
     */
    public float textHeight() {
      return textHeight;
    }

    /**
     * Configure image optimization.
     * <p>
     * should be the (in case average) height in pixels of an uppercase X in the image's text
     * <p><b>NOTE:</b> should only be tried in cases, where the defaults do not lead to acceptable results</p>
     * @param height a number of pixels
     * @return this Options
     */
    public Options textHeight(float height) {
      textHeight = height;
      return this;
    }

    /**
     * Configure the image optimization.
     * <p>
     * should be the (in case average) fontsize as base for internally calculating the {@link #textHeight()}
     * <p><b>NOTE:</b> should only be tried in cases, where the defaults do not lead to acceptable results</p>
     * @param size of a font
     * @return this Options
     */
    public Options fontSize(int size) {
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

    protected Image.Interpolation resizeInterpolation() {
      return resizeInterpolation;
    }

    /**
     * INTERNAL (under investigation).
     * <p>should not be used - not supported
     * @param method {@link Image.Interpolation}
     * @return this Options
     */
    public Options resizeInterpolation(Image.Interpolation method) {
      resizeInterpolation = method;
      return this;
    }

    private Float bestDPI = null;

    protected float bestDPI() {
      return bestDPI;
    }

    /**
     * INTERNAL (under investigation).
     * <p>should not be used - not supported
     * @param dpi the dpi value
     * @return this Options
     */
    public Options bestDPI(int dpi) {
      bestDPI = (float) dpi;
      return this;
    }

    private static final int TESSERACT_USER_DEFINED_DPI = 300;
    private int userDPI;

    /**
     * INTERNAL (under investigation).
     * <p>should not be used - not supported
     * @param dpi 70 .. 2400
     * @return this Options
     */
    //TODO why is this needed? Tess4J/Tesseract produce a warning is not set or not 70 .. 2400
    public Options userDPI(int dpi) {
      if (dpi == 0) {
        dpi = Toolkit.getDefaultToolkit().getScreenResolution();
      }
      if (dpi < 70 || dpi > 2400) {
        throw new IllegalArgumentException(String.format("OCR: Invalid user DPI: %s (must be 70 .. 2400)", dpi));
      }
      userDPI = dpi;
      variable("user_defined_dpi", Integer.toString(dpi));
      return this;
    }

    protected float factor() {
      // LEGACY: Calculate the resize factor based on the optimal and
      // calculated DPI value if bestDPI has been set manually
      if (bestDPI != null) {
        return bestDPI / Toolkit.getDefaultToolkit().getScreenResolution();
      }
      return OPTIMAL_X_HEIGHT / textHeight;
    }
    //</editor-fold>

    //<editor-fold desc="15 variables">
    private Map<String, String> variables = new LinkedHashMap<>();

    /**
     * @return the currently stored variables
     * @see #variable(String, String)
     */
    public Map<String, String> variables() {
      return variables;
    }

    /**
     * set a variable for Tesseract.
     * <p>
     * you should know, what you are doing - consult the Tesseract docs
     * </p>
     * @param key the key
     * @param value the value
     * @return this Options
     * @see <a href="https://github.com/tesseract-ocr/tesseract/wiki/Documentation">Tesseract docs</a>
     */
    public Options variable(String key, String value) {
      variables.put(key, value);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="16 configs">
    private Set<String> configs = new LinkedHashSet<>();

    /**
     * get current configs
     * @return currently stored names of configs files
     * @see #configs(String...)
     */
    public List<String> configs() {
      return new ArrayList<>(configs);
    }

    /**
     * set one ore more configs file names.
     * <p>you should know, what you are doing - consult the Tesseract docs
     * @param configs one or more configs filenames
     * @return this Options
     * @see <a href="https://github.com/tesseract-ocr/tesseract/wiki/Documentation">Tesseract docs</a>
     */
    public Options configs(String... configs) {
      configs(Arrays.asList(configs));
      return this;
    }

    /**
     * set a list of configs file names.
     * <p>you should know, what you are doing - consult the Tesseract docs
     * @param configs a list of configs filenames
     * @return this Options
     * @see <a href="https://github.com/tesseract-ocr/tesseract/wiki/Documentation">Tesseract docs</a>
     */
    public Options configs(List<String> configs) {
      this.configs = new LinkedHashSet<>(configs);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="20 helpers">
    private boolean hasVariablesOrConfigs() {
      return !configs.isEmpty() || !variables.isEmpty();
    }

    private String logVariablesConfigs() {
      String logConfigs = "";
      for (String config : configs) {
        if (!logConfigs.isEmpty()) {
          logConfigs += ", ";
        }
        logConfigs += config;
      }
      if (!logConfigs.isEmpty()) {
        logConfigs = "configs: " + logConfigs;
      }
      String logVariables = "";
      for (String key : variables.keySet()) {
        if (!logVariables.isEmpty()) {
          logVariables += ", ";
        }
        logVariables += key + ":" + variables.get(key);
      }
      if (!logVariables.isEmpty()) {
        logVariables = "variables: " + logVariables;
      }
      if (!logConfigs.isEmpty()) {
        logVariables = "\n" + logVariables;
      }
      return (logConfigs + logVariables).trim();
    }
    //</editor-fold>
  }
  //</editor-fold>

  //<editor-fold desc="10 global">
  /**
   * Resets the global options to the initial defaults.
   * @see OCR.Options#reset()
   * @return the global Options
   */
  public static Options reset() {
    return globalOptions().reset();
  }

  public static void restart() {
    Options.defaultDataPath = null;
    TextRecognizer.setValid(false);
  }

  /**
   * prints out the current global options.
   */
  public static void status() {
    Debug.print("Global settings " + globalOptions().toString());
  }
  //</editor-fold>

  //<editor-fold desc="20 text">
  /**
   * Reads text from the given source.
   * <p>Uses the global options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @return text
   */
  public static <SFIRBS> String readText(SFIRBS from) {
    return readText(from, globalOptions());
  }

  /**
   * Reads text from the given source.
   * <p>Uses the given options
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @param options  Options to be used
   * @return text
   */
  public static <SFIRBS> String readText(SFIRBS from, Options options) {
    if (options.psm() == PSM.AUTO.ordinal()) {
      options.psm(PSM.SINGLE_BLOCK);
    }
    return TextRecognizer.get(options).readText(from);
  }
  //</editor-fold>

  /**
   * chapter info
   */
  //<editor-fold desc="21 line">

  /**
   * Reads text from the given source (line).
   * <p>assuming the source contains a single line of text.
   * <p>Uses the global options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @return text
   */
  public static <SFIRBS> String readLine(SFIRBS from) {
    return readLine(from, globalOptions());
  }

  /**
   * Reads text from the given source (line).
   * <p>assuming the source contains a single line of text.
   * <p>Uses the given options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @param options  options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readLine(SFIRBS from, Options options) {
    return readText(from, options.clone().asLine());
  }

  /**
   * Reads text from the given source as lines.
   * <p>Uses the global options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @return lines as a list of matches
   */
  public static <SFIRBS> List<Match> readLines(SFIRBS from) {
    return readLines(from, globalOptions());
  }

  /**
   * Reads text from the given source as lines.
   * <p>Uses the given options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @param options  options for the used TextRecognizer
   * @return lines
   */
  public static <SFIRBS> List<Match> readLines(SFIRBS from, Options options) {
    return TextRecognizer.get(options).readLines(from);
  }
  //</editor-fold>

  //<editor-fold desc="22 word">

  /**
   * Reads text from the given source (word).
   * <p>assuming the source contains a single word of text.
   * <p>Uses the global options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @return text
   */
  public static <SFIRBS> String readWord(SFIRBS from) {
    return readWord(from, globalOptions());
  }

  /**
   * Reads text from the given source (word).
   * <p>assuming the source contains a single word of text.
   * <p>Uses the given options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @param options  options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readWord(SFIRBS from, Options options) {
    return readText(from, options.clone().asWord());
  }

  /**
   * Reads text from the given source as words.
   * <p>Uses the global options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @return words as alist of matches
   */
  public static <SFIRBS> List<Match> readWords(SFIRBS from) {
    return readWords(from, OCR.globalOptions());
  }

  /**
   * Reads text from the given source as words.
   * <p>Uses the given options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @param options  options for the used TextRecognizer
   * @return words as a list of matches
   */
  public static <SFIRBS> List<Match> readWords(SFIRBS from, Options options) {
    return TextRecognizer.get(options).readWords(from);
  }
  //</editor-fold>

  //<editor-fold desc="23 char">

  /**
   * Reads text from the given source (character).
   * <p>assuming the source contains a single character.
   * <p>Uses the global options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @return text
   */
  public static <SFIRBS> String readChar(SFIRBS from) {
    return readChar(from, globalOptions());
  }

  /**
   * Reads text from the given source (character).
   * <p>assuming the source contains a single character.
   * <p>Uses the given options.
   * @param <SFIRBS> File name, File, Image, Region, BufferdImage or ScreenImage
   * @param from     source to read text from
   * @param options  options for the used TextRecognizer
   * @return text
   */
  public static <SFIRBS> String readChar(SFIRBS from, Options options) {
    return readText(from, options.clone().asChar());
  }
  //</editor-fold>
}
