package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class OCR extends TextRecognizer {

  public static Tesseract1 newTesseract() {
    return new Tesseract1();
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

  public static class Options {

    //<editor-fold desc="02 init, reset">
    private Options() {}

    public static Options options() {
      return new Options();
    }

    public Options init() {

      // Set user_defined_dpi to something other than 70 to avoid
      // getting an error message on STDERR about guessing the resolution.
      // Interestingly, setting this to whatever value between
      // 70 and 2400 seems to have no impact on accuracy.
      // Not with LSTM and not with the legacy model either.
      // TODO Investigate this further
      globalVariable("user_defined_dpi", Integer.toString(userDPI()));

      optionsToTesseract();
      return this;
    }

    public static Options reset(Options currentOptions) {
      Options options;
      if (currentOptions.hasVariablesOrConfigs()) {
        options = new Options();
        options.tesseract(OCR.newTesseract());
        options.globalVariables(currentOptions);
      } else {
        options = new Options(currentOptions.tesseract);
      }
      options.startDataPath(currentOptions.startDataPath);
      options.optionsToTesseract();
      return options;
    }

    protected Options updateFrom(Options currentOpts) {
      tesseract(currentOpts.tesseract());
      if (dataPath() == null) {
        startDataPath(currentOpts.dataPath());
      }
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="05 tesseract">
    private Tesseract1 tesseract = null;

    public Tesseract1 tesseract() {
      return tesseract;
    }

    public Options tesseract(Tesseract1 tesseract) {
      this.tesseract = tesseract;
      return this;
    }

    public Options(Tesseract1 tesseract) {
      this.tesseract = tesseract;
    }

    protected void optionsToTesseract() {
      if (tesseract == null) {
        return;
      }
      tesseract.setOcrEngineMode(o_oem);
      tesseract.setPageSegMode(o_psm);
      tesseract.setLanguage(o_language);
      tesseract.setDatapath(o_dataPath);
      for (String key : savedGlobalVariables.keySet()) {
        tesseract.setTessVariable(key, savedGlobalVariables.get(key));
      }
      if (savedConfigs.size() > 0) {
        tesseract.setConfigs(savedConfigs);
      }
      for (String key : savedVariables.keySet()) {
        tesseract.setTessVariable(key, savedVariables.get(key));
      }
    }
    //</editor-fold>

    //<editor-fold desc="10 oem">
    private int o_oem = OcrEngineMode.DEFAULT.ordinal();
    
    public int oem() {
      return o_oem;
    }

    public Options oem(int oem) {
      o_oem = oem;
      if (null!= tesseract) {
        tesseract.setOcrEngineMode(o_oem);
      }
      return this;
    }

    public Options oem(OcrEngineMode oem) {
      oem(oem.ordinal());
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="11 psm">
    private int o_psm = PageSegMode.AUTO.ordinal();
 
    public int psm() {
      return o_psm;
    }

    public Options psm(int psm) {
      o_psm = psm;
      if (null!= tesseract) {
        tesseract.setPageSegMode(o_psm);
      }
      return this;
    }

    public Options psm(PageSegMode psm) {
      psm(psm.ordinal());
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
    private String o_language = startLanguage;

    public String language() {
      return o_language;
    }

    public Options language(String language) {
      o_language = language;
      if (null!= tesseract) {
        tesseract.setLanguage(o_language);
      }
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="13 datapath">
    private String startDataPath = null;
    private String o_dataPath = null;

    public String dataPath() {
      return o_dataPath;
    }

    public Options dataPath(String dataPath) {
      o_dataPath = dataPath;
      if (null!= tesseract) {
        tesseract.setDatapath(o_dataPath);
      }
      return this;
    }

    public Options startDataPath(String dataPath) {
      if (checkDataPath(dataPath)) {
        startDataPath = dataPath;
        o_dataPath = dataPath;
      }
      return this;
    }

    private boolean checkDataPath(String dataPath) {
      if (new File(dataPath, language() + ".traineddata").exists()) {
        return true;
      }
      Debug.error("OCR: datapath: no %s.traineddata - provide another language", language());
      return false;
    }
    //</editor-fold>

    //<editor-fold desc="14 optimization">
    public Options smallFont() {
      textHeight(11);
      return this;
    }

    private static float getDefaultHeight() {
      Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
      try {
        Font font = g.getFont();
        FontMetrics fm = g.getFontMetrics(font);
        return fm.getLineMetrics("X", g).getHeight();
      } finally {
        g.dispose();
      }
    }

    private float uppercaseXHeight = getDefaultHeight();

    private static final int OPTIMAL_X_HEIGHT = 30;

    public float textHeight() {
      return uppercaseXHeight;
    }

    public Options textHeight(float height) {
      uppercaseXHeight = height;
      return this;
    }

    private Image.Interpolation resizeInterpolation = Image.Interpolation.LINEAR;

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
    private int userDPI = TESSERACT_USER_DEFINED_DPI;

    private int userDPI() {
      return userDPI;
    }

    public Options userDPI(int dpi) {
      if (dpi < 70) dpi = Toolkit.getDefaultToolkit().getScreenResolution();
          userDPI = dpi;
      return this;
    }

    public float factor() {
      // LEGACY: Calculate the resize factor based on the optimal and
      // calculated DPI value if optimumDPI has been set manually
      if (bestDPI != null) {
        return bestDPI / Toolkit.getDefaultToolkit().getScreenResolution();
      }
      return OPTIMAL_X_HEIGHT / uppercaseXHeight;
    }
    //</editor-fold>

    //<editor-fold desc="15 variables">
    private Map<String, String> savedVariables = new HashMap<>();
    private Map<String, String> savedGlobalVariables = new HashMap<>();

    public Options variable(String key, String value) {
      savedVariables.put(key, value);
      if (null != tesseract) {
        tesseract.setTessVariable(key, value);
      }
      return this;
    }

    protected Options globalVariable(String key, String value) {
      savedGlobalVariables.put(key, value);
      if (null != tesseract) {
        tesseract.setTessVariable(key, value);
      }
      return this;
    }

    private void globalVariables(Options currentOptions) {
      for (String key : currentOptions.savedGlobalVariables.keySet()) {
        globalVariable(key, currentOptions.savedGlobalVariables.get(key));
      }
    }
    //</editor-fold>

    //<editor-fold desc="16 configs">
    private List<String> savedConfigs = new ArrayList<>();

    public Options configs(String... configs) {
      configs(Arrays.asList(configs));
      return this;
    }

    public Options configs(List<String> configs) {
      savedConfigs = new ArrayList<>();
      for (String config : configs) {
        if (!savedConfigs.contains(config)) {
          savedConfigs.add(config);
        }
      }
      if (null != tesseract) {
        tesseract.setConfigs(configs);
      }
      return this;
    }
    //</editor-fold>

    //<editor-fold desc="20 helpers">
    protected boolean hasVariablesOrConfigs() {
      return savedConfigs.size() > 0 || savedVariables.size() > 0 || savedGlobalVariables.size() > 0;
    }

    public String logVariablesConfigs() {
      String logConfigs = "";
      for (String config: savedConfigs) {
        if (!logConfigs.isEmpty()) {
          logConfigs += ",";
        }
        logConfigs += config;
      }
      if (!logConfigs.isEmpty()) {
        logConfigs = "configs: " + logConfigs;
      }
      String logVariables = "";
      for (String key: savedVariables.keySet()) {
        if (!logVariables.isEmpty()) {
          logVariables += ",";
        }
        logVariables += key + ":" + savedVariables.get(key);
      }
      if (!logVariables.isEmpty()) {
        logVariables = "variables: " + logVariables;
      }
      String logGlobalVariables = "";
      for (String key: savedGlobalVariables.keySet()) {
        if (!logGlobalVariables.isEmpty()) {
          logGlobalVariables += ",";
        }
        logGlobalVariables += key + ":" + savedGlobalVariables.get(key);
      }
      if (!logGlobalVariables.isEmpty()) {
        logGlobalVariables = "global variables: " + logGlobalVariables;
      }
      if (!logConfigs.isEmpty()) {
        logConfigs += "\n";
      }
      if (!logGlobalVariables.isEmpty()) {
        logGlobalVariables += "\n";
      }
      return (logConfigs + logGlobalVariables + logVariables).trim();
    }
    //</editor-fold>
  }
}
