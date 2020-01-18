package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import org.sikuli.basics.Settings;

import java.awt.*;
import java.awt.image.BufferedImage;

public class OCR extends TextRecognizer {

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

  public static void withSmallFont() {
    TextRecognizer tr = TextRecognizer.start();
    tr.setTextHeight(11);
  }

  public static Options options() {
    return Options.options();
  }

  public static class Options {

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

    private Options() {}

    public static Options options() {
      return new Options();
    }

    public Options init() {
      optionsToTesseract();
      return this;
    }

    public static Options reset(Options currentOptions) {
      Options options = new Options(currentOptions.tesseract);
      options.startDataPath(currentOptions.startDataPath);
      options.optionsToTesseract();
      return options;
    }

    private void optionsToTesseract() {
      if (tesseract == null) {
        return;
      }
      tesseract.setOcrEngineMode(o_oem);
      tesseract.setPageSegMode(o_psm);
      tesseract.setLanguage(o_language);
      tesseract.setDatapath(o_dataPath);
    }

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
      startDataPath = dataPath;
      dataPath(dataPath);
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

    public float factor() {
      // LEGACY: Calculate the resize factor based on the optimal and
      // calculated DPI value if optimumDPI has been set manually
      if (bestDPI != null) {
        return bestDPI / Toolkit.getDefaultToolkit().getScreenResolution();
      }
      return OPTIMAL_X_HEIGHT / uppercaseXHeight;
    }
  }
}
