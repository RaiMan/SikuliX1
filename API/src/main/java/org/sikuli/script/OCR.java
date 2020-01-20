package org.sikuli.script;

import net.sourceforge.tess4j.ITesseract;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class OCR extends TextRecognizer {

  protected OCR(Options options) {
    super(options);
  }

  /**
   * OCR Engine modes:
   * 0    Original Tesseract only.
   * 1    Cube only.
   * 2    Tesseract + cube.
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

  public static Options options() {
    return Options.options();
  }

  private static Options globalOptions = Options.options();

  public static Options globalOptions() {
    return globalOptions;
  }

  public static class Options {

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

    private Options() {
      reset();
    }

    public static Options options() {
      return new Options();
    }
    //</editor-fold>

    public Options reset() {
      oem = OcrEngineMode.DEFAULT.ordinal();
      psm = PageSegMode.AUTO.ordinal();
      language = startLanguage;
      dataPath = defaultDataPath;
      textHeight = getDefaultTextHeight();
      resizeInterpolation = Image.Interpolation.LINEAR;
      variables.clear();
      configs.clear();
      bestDPI = null;
      userDPI(TESSERACT_USER_DEFINED_DPI);
      return this;
    }

    public void initTesseract(ITesseract tesseract) {
      tesseract.setOcrEngineMode(oem);
      tesseract.setPageSegMode(psm);
      tesseract.setLanguage(language);
      tesseract.setDatapath(dataPath);
      for (Map.Entry<String, String> entry : variables.entrySet()) {
        tesseract.setTessVariable(entry.getKey(), entry.getValue());
      }
      if (!configs.isEmpty()) {
        tesseract.setConfigs(new ArrayList<>(configs));
      }
    }
    //</editor-fold>

    //<editor-fold desc="10 oem">
    private int oem;

    public int oem() {
      return oem;
    }

    public Options oem(int oem) {
      if (oem < 0 || oem > 3) {
        throw new IllegalArgumentException(String.format("Invaid OEM %s", oem));
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
        throw new IllegalArgumentException(String.format("Tesseract: psm invalid (%d) - using default (3)", psm));
      }
      if (psm == OCR.PageSegMode.OSD_ONLY.ordinal() || psm == OCR.PageSegMode.AUTO_OSD.ordinal()
          || psm == OCR.PageSegMode.SPARSE_TEXT_OSD.ordinal()) {
        if (!new File(dataPath(), "osd.traineddata").exists()) {
          throw new IllegalArgumentException( String.format("OCR: setPSM(%d): needs OSD, " +
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

    public Options resetPSM() {
      psm = -1;
      return this;
    }

    public Options asLine() {
      psm(PageSegMode.SINGLE_LINE);
      return this;
    }

    public Options asWord() {
      psm(PageSegMode.SINGLE_WORD);
      return this;
    }

    public Options asChar() {
      psm(PageSegMode.SINGLE_CHAR);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="12 language">
    private String startLanguage = Settings.OcrLanguage;
    private String language;

    public String language() {
      return language;
    }

    public Options language(String language) {
      if (new File(dataPath(), language + ".traineddata").exists()) {
        this.language = language;
      } else {
        throw new SikuliXception(String.format("OCR: setLanguage: no %s.traineddata in %s", language, dataPath()));
      }
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
      if(!checkDataPath(dataPath)) {
        throw new IllegalArgumentException(String.format("OCR: datapath: no %s.traineddata - provide another language", language()));
      }

      this.dataPath = dataPath;
      return this;
    }


    private boolean checkDataPath(String dataPath) {
      if (new File(dataPath, language() + ".traineddata").exists()) {
        return true;
      }
      return false;
    }
    //</editor-fold>

    //<editor-fold desc="14 optimization">
    public Options smallFont() {
      textHeight(11);
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
      if(dpi < 70 || dpi > 2400) {
        throw new IllegalArgumentException("user DPI must be between 70 and 2400");
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
    private Map<String, String> variables = new HashMap<>();

    public Options variable(String key, String value) {
      variables.put(key, value);
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="16 configs">
    private Set<String> configs = new HashSet<>();

    public Options configs(String... configs) {
      configs(Arrays.asList(configs));
      return this;
    }

    public Options configs(List<String> configs) {
      this.configs = new HashSet<>(configs);
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
      for (String key: variables.keySet()) {
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
}
