/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder.Finder2;
import org.sikuli.script.support.RunTime;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TextRecognizer {

  //<editor-fold desc="00 instance, start, stop, reset ">
  private static int lvl = 3;

  private static TextRecognizer textRecognizer = null;
  public static String versionTess4J = "4.4.1";
  public static String versionTesseract = "4.1.0";

  private static final int TESSERACT_USER_DEFINED_DPI = 300;

  private TextRecognizer() {
    Finder.Finder2.init();
  }

  public void status() {
    Debug.logp("Textrecognizer current settings" +
        "\ndata = %s" +
        "\nlanguage(%s) oem(%d) psm(%d) height(%d)",
        dataPath, language, oem, psm, uppercaseXHeight);
  }

  public boolean isValid() {
    if (tess == null) {
      return false;
    }
    return true;
  }

  private Tesseract1 tess = null;

  public static TextRecognizer start() {
    if (textRecognizer == null) {
      textRecognizer = new TextRecognizer();
      Debug.log(lvl, "TextRecognizer: start: Tess4J %s using Tesseract %s", versionTess4J, versionTesseract);
      try {
        textRecognizer.tess = new Tesseract1();
        boolean tessdataOK = extractTessdata();
        if (tessdataOK) {
          Debug.log(lvl, "TextRecognizer: start: data folder: %s", textRecognizer.dataPath);
          textRecognizer.tess.setDatapath(textRecognizer.dataPath);
          if (!new File(textRecognizer.dataPath, textRecognizer.language + ".traineddata").exists()) {
            textRecognizer = null;
            Debug.error("TextRecognizer: start: no %s.traineddata - provide another language", textRecognizer.language);
          } else {
            Debug.log(lvl, "TextRecognizer: start: language: %s", textRecognizer.language);
          }
        } else {
          textRecognizer = null;
          if (textRecognizer.dataPathProvided) {
            Debug.error("TextRecognizer: start: provided tessdata folder not found: %s", Settings.OcrDataPath);
          } else {
            Debug.error("TextRecognizer: start: no valid tesseract data folder");
          }
        }
      } catch (Exception e) {
        textRecognizer = null;
        Debug.error("TextRecognizer: start: %s", e.getMessage());
      } catch (UnsatisfiedLinkError e) {
        textRecognizer = null;
        String helpURL;
        Debug.error("TextRecognizer: start: Tesseract library problems: %s", e.getMessage());
        if (RunTime.get().runningWindows) {
          helpURL = "https://github.com/RaiMan/SikuliX1/wiki/Windows:-Problems-with-libraries-OpenCV-or-Tesseract";
        } else {
          helpURL = "https://github.com/RaiMan/SikuliX1/wiki/macOS-Linux:-Support-libraries-for-Tess4J-Tesseract-4-OCR";
        }
        Debug.error("see: " + helpURL);
        if (RunTime.isIDE()) {
          Debug.error("Save your work, correct the problem and restart the IDE!");
          try {
            Desktop.getDesktop().browse(new URI(helpURL));
          } catch (IOException ex) {
          } catch (URISyntaxException ex) {
          }
        }
      }
      if (null != textRecognizer) {
        textRecognizer.setLanguage(textRecognizer.language);
        textRecognizer.setOEM(OcrEngineMode.DEFAULT);
        textRecognizer.setPSM(PageSegMode.AUTO);

        // Set user_defined_dpi to something other than 70 to avoid
        // getting an error message on STDERR about guessing the resolution.
        // Interestingly, setting this to whatever value between
        // 70 and 2400 seems to have no impact on accuracy.
        // Not with LSTM and not with the legacy model either.
        // TODO Investigate this further
        textRecognizer.setVariable("user_defined_dpi", Integer.toString(TESSERACT_USER_DEFINED_DPI));

        textRecognizer.shouldRestart = false;
      }
    }
    if (null == textRecognizer) {
      //RunTime.get().terminate(999, "TextRecognizer could not be initialized");
      throw new SikuliXception(String.format("fatal: " + "TextRecognizer could not be started"));
    }
    return textRecognizer;
  }

  public static boolean extractTessdata() {
    File fTessDataPath;
    boolean shouldExtract = false;
    fTessDataPath = new File(RunTime.get().fSikulixAppFolder, "SikulixTesseract/tessdata");
    //export latest tessdata to the standard SikuliX tessdata folder in any case
    if (fTessDataPath.exists()) {
      if (RunTime.get().shouldExport()) {
        shouldExtract = true;
        FileManager.deleteFileOrFolder(fTessDataPath);
      }
    } else {
      shouldExtract = true;
    }
    if (shouldExtract) {
      long tessdataStart = new Date().getTime();
      List<String> files = RunTime.get().extractResourcesToFolder("/tessdataSX", fTessDataPath, null);
      Debug.log("TextRecognizer: start: extracting tessdata took %d msec", new Date().getTime() - tessdataStart);
      if (files.size() == 0) {
        Debug.error("TextRecognizer: start: export tessdata not possible");
      }
    }
    // if set, try with provided tessdata folder
    if (Settings.OcrDataPath != null) {
      fTessDataPath = new File(Settings.OcrDataPath, "tessdata");
      textRecognizer.dataPathProvided = true;
    }
    if (fTessDataPath.exists()) {
      textRecognizer.startDataPath = fTessDataPath.getAbsolutePath();
      textRecognizer.dataPath = textRecognizer.startDataPath;
      textRecognizer.hasOsdTrData = new File(textRecognizer.dataPath, "osd.traineddata").exists();
      return true;
    }
    return false;
  }

  public Tesseract1 getAPI() {
    return tess;
  }

  public static void reset() {
    if (null != textRecognizer) {
      if (textRecognizer.shouldRestart) {
        stop();
        start();
      } else {
        textRecognizer.resetUppercaseXHeight();
        textRecognizer.setOEM(OcrEngineMode.DEFAULT);
        textRecognizer.setPSM(PageSegMode.AUTO);
        textRecognizer.resetDataPath();
        textRecognizer.resetLanguage();
      }
    }
  }

  public static void stop() {
    textRecognizer = null;
  }
  //</editor-fold>

  //<editor-fold desc="02 PSM and OEM">
  private int oem = -1;
  private int psm = -1;

  /**
   * OCR Engine modes:
   * 0    Original Tesseract only. TESSERACT_ONLY
   * 1    Cube/LSTM only. LSTM_ONLY
   * 2    Tesseract + Cube/LSTM. TESSERACT_LSTM_COMBINED
   * 3    Default, based on what is available. DEFAULT
   */
  public enum OcrEngineMode {
    TESSERACT_ONLY, // 0
    LSTM_ONLY, // 1
    TESSERACT_LSTM_COMBINED, // 2
    DEFAULT // 3
  }

  public TextRecognizer setOEM(OcrEngineMode oem) {
    return setOEM(oem.ordinal());
  }

  /**
   * OCR Engine modes:
   * 0    Original Tesseract only.
   * 1    Cube only.
   * 2    Tesseract + cube.
   * 3    Default, based on what is available.
   *
   * @param oem
   * @return
   */
  public TextRecognizer setOEM(int oem) {
    if (oem < 0 || oem > 3) {
      Debug.error("Tesseract: oem invalid (%d) - using default (3)", oem);
      oem = OcrEngineMode.DEFAULT.ordinal();
    }
    if (isValid()) {
      this.oem = oem;
      tess.setOcrEngineMode(this.oem);
    }
    return this;
  }

  private boolean hasOsdTrData = false;

  /**
   * Page segmentation modes:
   * 0    OSD_ONLY Orientation and script detection (OSD) only.
   * 1    AUTO_OSD Automatic page segmentation with OSD.
   * 2    AUTO_ONLY Automatic page segmentation, but no OSD, or OCR.
   * 3    AUTO Fully automatic page segmentation, but no OSD. (Default)
   * 4    SINGLE_COLUMN Assume a single column of text of variable sizes.
   * 5    SINGLE_BLOCK_VERT_TEXT Assume a single uniform block of vertically aligned text.
   * 6    SINGLE_BLOCK Assume a single uniform block of text.
   * 7    SINGLE_LINE Treat the image as a single text line.
   * 8    SINGLE_WORD Treat the image as a single word.
   * 9    CIRCLE_WORD Treat the image as a single word in a circle.
   * 10    SINGLE_CHAR Treat the image as a single character.
   * 11    SPARSE_TEXT Sparse text. Find as much text as possible in no particular order.
   * 12    SPARSE_TEXT_OSD Sparse text with OSD.
   * 13    RAW_LINE Raw line. Treat the image as a single text line, bypassing hacks that are Tesseract-specific.
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

  public TextRecognizer setPSM(PageSegMode psm) {
    return setPSM(psm.ordinal());
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
   *
   * @param psm
   * @return the textRecognizer instance
   */
  public TextRecognizer setPSM(int psm) {
    if (psm < 0 || psm > 13) {
      Debug.error("Tesseract: psm invalid (%d) - using default (3)", psm);
      psm = 3;
    }
    if (isValid()) {
      if (psm == PageSegMode.OSD_ONLY.ordinal() || psm == PageSegMode.AUTO_OSD.ordinal()
          || psm == PageSegMode.SPARSE_TEXT_OSD.ordinal()) {
        if (!hasOsdTrData) {
          String msg = String.format("TextRecognizer: setPSM(%d): needs OSD, " +
              "but no osd.traineddata found in tessdata folder", psm);
          //RunTime.get().terminate(999, msg);
          throw new SikuliXception(String.format("fatal: " + msg));
        }
      }
      this.psm = psm;
      tess.setPageSegMode(this.psm);
    }
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="03 set datapath, language, variable, configs">
  private boolean dataPathProvided = false;
  private String startDataPath = null;
  private String dataPath = null;
  private String startLanguage = Settings.OcrLanguage;
  private String language = startLanguage;

  public TextRecognizer setDataPath(String newDataPath) {
    if (isValid()) {
      if (new File(newDataPath).exists()) {
        if (new File(newDataPath, language + ".traineddata").exists()) {
          dataPath = newDataPath;
          tess.setDatapath(dataPath);
        } else {
          String msg = String.format("TextRecognizer: setDataPath: not valid " +
              "- no %s.traineddata (%s)", language, newDataPath);
          //RunTime.get().terminate(999, msg);
          throw new SikuliXception(String.format("fatal: " + msg));
        }
      }
    }
    return this;
  }

  private void resetDataPath() {
    dataPath = startDataPath;
    tess.setDatapath(dataPath);
  }

  public TextRecognizer setLanguage(String language) {
    if (isValid()) {
      if (new File(dataPath, language + ".traineddata").exists()) {
        this.language = language;
        tess.setLanguage(this.language);
      } else {
        String msg = String.format("TextRecognizer: setLanguage: no %s.traineddata in %s", language, this.dataPath);
        //RunTime.get().terminate(999, msg);
        throw new SikuliXception(String.format("fatal: " + msg));
      }
    }
    return this;
  }

  private void resetLanguage() {
    language = startLanguage;
    tess.setLanguage(language);
  }

  private boolean shouldRestart = false;

  public TextRecognizer setVariable(String key, String value) {
    if (isValid()) {
      shouldRestart = true;
      tess.setTessVariable(key, value);
    }
    return this;
  }

  public TextRecognizer setConfigs(String... configs) {
    setConfigs(Arrays.asList(configs));
    return this;
  }

  public TextRecognizer setConfigs(List<String> configs) {
    if (isValid()) {
      shouldRestart = true;
      tess.setConfigs(configs);
    }
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="10 image optimization">
  /**
   * @deprecated use setExpectedFontSize(int size) or setExpectedXHeight(int height) instead
   */
  public Float optimumDPI = null;

  /**
   * Hint for the OCR Engine about the expected font size in pt
   *
   * @param size expected font size in pt (must be > 7)
   */
  public void setFontSize(int size) {
    if (size > 7) {
      Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
      try {
        Font font = new Font(g.getFont().getFontName(), 0, size);
        FontMetrics fm = g.getFontMetrics(font);
        uppercaseXHeight = (int) fm.getLineMetrics("X", g).getHeight();
      } finally {
        g.dispose();
      }
    }
  }

  /**
   * Hint for the OCR Engine about the expected height of an uppercase X in px
   *
   * @param height of an uppercase X in px (must be greater than 7)
   */
  public void setUppercaseXHeight(int height) {
    if (height > 7) {
      uppercaseXHeight = height;
    } else {
      uppercaseXHeight = getDefaultUppercaseXHeight();
    }
  }

  public void resetUppercaseXHeight() {
    uppercaseXHeight = getDefaultUppercaseXHeight();
  }

  public int getUppercaseXHeight() {
    return uppercaseXHeight;
  }

  private int getDefaultUppercaseXHeight() {
    Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
    try {
      Font font = g.getFont();
      FontMetrics fm = g.getFontMetrics(font);
      return (int) fm.getLineMetrics("X", g).getHeight();
    } finally {
      g.dispose();
    }
  }

  private int uppercaseXHeight = getDefaultUppercaseXHeight();

  private static final int OPTIMAL_X_HEIGHT = 30;

  public Image.Interpolation resizeInterpolation = Image.Interpolation.LINEAR;

  private float factor() {
    // LEGACY: Calculate the resize factor based on the optimal and
    // calculated DPI value if optimumDPI has been set manually
    if (optimumDPI != null) {
      return optimumDPI / Toolkit.getDefaultToolkit().getScreenResolution();
    }
    return OPTIMAL_X_HEIGHT / uppercaseXHeight;
  }

  public static double getCurrentResize() {
    TextRecognizer tr = start();
    if (tr.isValid()) {
      return tr.factor();
    }
    return 1;
  }

  /*
   * sharpens the image using an unsharp mask
   */
  private Mat unsharpMask(Mat img, double sigma) {
    Mat blurred = new Mat();
    Imgproc.GaussianBlur(img, blurred, new Size(), sigma, sigma);
    Core.addWeighted(img, 1.5, blurred, -0.5, 0, img);
    return img;
  }

  public BufferedImage optimize(BufferedImage bimg) {
    Mat mimg = Finder2.makeMat(bimg);

    Imgproc.cvtColor(mimg, mimg, Imgproc.COLOR_BGR2GRAY);

    // sharpen original image to primarily get rid of sub pixel rendering artifacts
    mimg = unsharpMask(mimg, 3);

    float rFactor = factor();

    if (rFactor > 0 && rFactor != 1) {
      Image.resize(mimg, rFactor, resizeInterpolation);
    }

    // sharpen the enlarged image again
    mimg = unsharpMask(mimg, 5);

    // invert in case of mainly dark background
    if (Core.mean(mimg).val[0] < 127) {
      Core.bitwise_not(mimg, mimg);
    }

    return Finder2.getBufferedImage(mimg);
  }
  //</editor-fold>

  //<editor-fold desc="20 OCR from BufferedImage">
  public static String doOCR(ScreenImage simg) {
    return doOCR(simg.getImage());
  }

  public static String doOCR(BufferedImage bimg) {
    String text = "";
    TextRecognizer tr = start();
    if (tr.isValid()) {
      text = tr.read(bimg);
    }
    return text;
  }

  private String read(BufferedImage bimg) {
    if (isValid()) {
      try {
        return tess.doOCR(optimize(bimg));
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: Tess4J: doOCR: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "";
  }

  public static int PAGE_ITERATOR_LEVEL_LINE = 2;
  public static int PAGE_ITERATOR_LEVEL_WORD = 3;

  protected static List<Match> readLines(BufferedImage bimg) {
    return readTextItems(bimg, PAGE_ITERATOR_LEVEL_LINE, null);
  }

  public static List<Match> readWords(BufferedImage bimg) {
    return readTextItems(bimg, PAGE_ITERATOR_LEVEL_WORD, null);
  }

  protected static List<Match> readLines(BufferedImage bimg, Region base) {
    return readTextItems(bimg, PAGE_ITERATOR_LEVEL_LINE, base);
  }

  public static List<Match> readWords(BufferedImage bimg, Region base) {
    return readTextItems(bimg, PAGE_ITERATOR_LEVEL_WORD, base);
  }

  private static List<Match> readTextItems(BufferedImage bimg, int level, Region base) {
    List<Match> lines = new ArrayList<>();
    TextRecognizer tr = start();
    if (tr.isValid()) {
      BufferedImage bimgResized = tr.optimize(bimg);
      List<Word> textItems = tr.getAPI().getWords(bimgResized, level);
      double wFactor = (double) bimg.getWidth() / bimgResized.getWidth();
      double hFactor = (double) bimg.getHeight() / bimgResized.getHeight();
      int offX = 0;
      int offY = 0;
      if (null != base) {
        offX = base.x;
        offY = base.y;
      }
      for (Word textItem : textItems) {
        Rectangle boundingBox = textItem.getBoundingBox();
        Rectangle realBox = new Rectangle(
                offX + (int) (boundingBox.x * wFactor) - 1,
                offY + (int) (boundingBox.y * hFactor) - 1,
                1 + (int) (boundingBox.width * wFactor) + 2,
                1 + (int) (boundingBox.height * hFactor) + 2);
        if (null == base) {
          lines.add(new Match(realBox, textItem.getConfidence(), textItem.getText()));
        } else {
          lines.add(new Match(realBox, textItem.getConfidence(), textItem.getText(), base));
        }
      }
    }
    return lines;
  }
  //</editor-fold>

  //<editor-fold desc="30 helper">
  private Region relocate(Rectangle rect, Region base, double factor) {
    Region reg = new Region(); //rescale(rect);
    reg.x = base.x + (int) (rect.getX() / factor);
    reg.y = base.y + (int) (rect.getY() / factor);
    reg.w = (int) (1 + rect.getWidth() / factor);
    reg.h = (int) (1 + rect.getHeight() / factor);
    reg.setScreen(base.getScreen().getID());
    return reg;
  }

  public Rectangle relocateAsRectangle(Rectangle rect, Region base) {
    Region reg = relocate(rect, base, factor());
    return new Rectangle(reg.x, reg.y, reg.w, reg.h);
  }
  //</editor-fold>

  //<editor-fold desc="99 deprecated or not-used">

  /**
   * use start() instead
   *
   * @return
   */
  @Deprecated
  public static TextRecognizer getInstance() {
    TextRecognizer tr = TextRecognizer.start();
    if (!tr.isValid()) {
      return null;
    }
    return tr;
  }

  /**
   * deprecated use doOCR() instead
   *
   * @param simg
   * @return text
   */
  @Deprecated
  public String recognize(ScreenImage simg) {
    BufferedImage bimg = simg.getImage();
    return read(bimg);
  }

  /**
   * deprecated use doOCR() instead
   *
   * @param bimg
   * @return text
   */
  @Deprecated
  public String recognize(BufferedImage bimg) {
    return read(bimg);
  }
  //</editor-fold>
}
