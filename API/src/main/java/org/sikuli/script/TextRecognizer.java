/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
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

  private static int lvl = 3;

  public static String versionTess4J = "4.4.1";
  public static String versionTesseract = "4.1.0";

  //<editor-fold desc="00 start, stop, reset">
  public TextRecognizer() {
    RunTime.loadLibrary(RunTime.libOpenCV);
  }

  private static TextRecognizer textRecognizer = null;
  private Tesseract1 tess = null;

  public boolean isValid() {
    if (tess == null) {
      return false;
    }
    return true;
  }

  public static void status() {
    if (textRecognizer != null) {
      TextRecognizer tr = textRecognizer;
      Debug.logp("OCR: current settings" +
                      "\ndata = %s" +
                      "\nlanguage(%s) oem(%d) psm(%d) height(%.1f) factor(%.2f) dpi(%d) %s",
              tr.dataPath, tr.language, tr.oem, tr.psm, tr.uppercaseXHeight, tr.factor(),
              Toolkit.getDefaultToolkit().getScreenResolution(), tr.resizeInterpolation);
    } else {
      Debug.logp("OCR: not running");
    }
  }

  private static final int TESSERACT_USER_DEFINED_DPI = 300;

  public static TextRecognizer start() {
    if (textRecognizer == null) {
      textRecognizer = new TextRecognizer();
      Debug.log(lvl, "OCR: start: Tess4J %s using Tesseract %s", versionTess4J, versionTesseract);
      try {
        textRecognizer.tess = new Tesseract1();
        boolean tessdataOK = extractTessdata();
        if (tessdataOK) {
          Debug.log(lvl, "OCR: start: data folder: %s", textRecognizer.dataPath);
          textRecognizer.tess.setDatapath(textRecognizer.dataPath);
          if (!new File(textRecognizer.dataPath, textRecognizer.language + ".traineddata").exists()) {
            textRecognizer = null;
            Debug.error("OCR: start: no %s.traineddata - provide another language", textRecognizer.language);
          } else {
            Debug.log(lvl, "OCR: start: language: %s", textRecognizer.language);
          }
        } else {
          textRecognizer = null;
          if (textRecognizer.dataPathProvided) {
            Debug.error("OCR: start: provided tessdata folder not found: %s", Settings.OcrDataPath);
          } else {
            Debug.error("OCR: start: no valid tesseract data folder");
          }
        }
      } catch (Exception e) {
        textRecognizer = null;
        Debug.error("OCR: start: %s", e.getMessage());
      } catch (UnsatisfiedLinkError e) {
        textRecognizer = null;
        String helpURL;
        Debug.error("OCR: start: Tesseract library problems: %s", e.getMessage());
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
      throw new SikuliXception(String.format("fatal: " + "TextRecognizer could not be initialized"));
    }
    return textRecognizer;
  }

  public static boolean extractTessdata() {
    File fTessDataPath;
    boolean shouldExtract = false;
    fTessDataPath = new File(RunTime.get().fSikulixAppPath, "SikulixTesseract/tessdata");
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
      Debug.log("OCR: start: extracting tessdata took %d msec", new Date().getTime() - tessdataStart);
      if (files.size() == 0) {
        Debug.error("OCR: start: export tessdata not possible");
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

  public static void stop() {
    textRecognizer = null;
  }

  public static TextRecognizer reset() {
    if (null != textRecognizer) {
      if (textRecognizer.shouldRestart) {
        stop();
        start();
      } else {
        textRecognizer.resetHeight();
        textRecognizer.setOEM(OcrEngineMode.DEFAULT);
        textRecognizer.setPSM(PageSegMode.AUTO);
        textRecognizer.resetDataPath();
        textRecognizer.resetLanguage();
      }
    } else {
      start();
    }
    return textRecognizer;
  }

  private int savedPSM = -1;

  private void resetSavedPSM() {
    if (savedPSM > -1) {
      setPSM(savedPSM);
      savedPSM = -1;
    }
  }

  private float savedXHeight = -1;

  private void resetSavedXHeight() {
    if (savedXHeight > -1) {
      uppercaseXHeight = savedXHeight;
      savedXHeight = -1;
    }
  }

  private void savePSM() {
    savedPSM = psm;
  }

  private void saveHeight() {
    savedXHeight = uppercaseXHeight;
  }

  public static TextRecognizer asLine() {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      tr.setPSM(PageSegMode.SINGLE_LINE);
    }
    return tr;
  }

  public static TextRecognizer asWord() {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      tr.setPSM(PageSegMode.SINGLE_WORD);
    }
    return tr;
  }

  public static TextRecognizer asChar() {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      tr.setPSM(PageSegMode.SINGLE_CHAR);
    }
    return tr;
  }
  //</editor-fold>

  //<editor-fold desc="02 set OEM, PSM">

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

  private int oem = -1;
  private int psm = -1;

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
      oem = 3;
    }
    if (isValid()) {
      this.oem = oem;
      tess.setOcrEngineMode(this.oem);
    }
    return this;
  }

  private boolean hasOsdTrData = false;

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
//    if (psm == 3) {
//      resetPSM();
//      return this;
//    }
    if (isValid()) {
      if (psm == PageSegMode.OSD_ONLY.ordinal() || psm == PageSegMode.AUTO_OSD.ordinal()
              || psm == PageSegMode.SPARSE_TEXT_OSD.ordinal()) {
        if (!hasOsdTrData) {
          String msg = String.format("OCR: setPSM(%d): needs OSD, " +
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

  public TextRecognizer resetPSM() {
    this.psm = -1;
    if (isValid()) {
      tess.setPageSegMode(-1);
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
          String msg = String.format("OCR: setDataPath: not valid " +
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
        String msg = String.format("OCR: setLanguage: no %s.traineddata in %s", language, this.dataPath);
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
    if (isValid()) {
      setConfigs(Arrays.asList(configs));
    }
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
  private Float optimumDPI = null;

  /**
   * Hint for the OCR Engine about the expected font size in pt
   *
   * @param size expected font size in pt
   */
  protected static void setFontSize(int size) {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
      try {
        Font font = new Font(g.getFont().getFontName(), 0, size);
        FontMetrics fm = g.getFontMetrics(font);
        tr.uppercaseXHeight = fm.getLineMetrics("X", g).getHeight();
      } finally {
        g.dispose();
      }
    }
  }

  /**
   * Hint for the OCR Engine about the expected height of an uppercase X in px
   *
   * @param height of an uppercase X in px
   */
  protected static void setHeight(int height) {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      tr.uppercaseXHeight = height;
    }
  }

  protected static void resetHeight() {
    TextRecognizer tr = TextRecognizer.start();
    if (tr.isValid()) {
      tr.uppercaseXHeight = tr.getDefaultHeight();
    }
  }

  private float getDefaultHeight() {
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

  private Image.Interpolation resizeInterpolation = Image.Interpolation.LINEAR;

  private float factor() {
    // LEGACY: Calculate the resize factor based on the optimal and
    // calculated DPI value if optimumDPI has been set manually
    if (optimumDPI != null) {
      return optimumDPI / getActualDPI();
    }
    return OPTIMAL_X_HEIGHT / uppercaseXHeight;
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

  /*
   * sharpens the image using an unsharp mask
   */
  private Mat unsharpMask(Mat img, double sigma) {
    Mat blurred = new Mat();
    Imgproc.GaussianBlur(img, blurred, new Size(), sigma, sigma);
    Core.addWeighted(img, 1.5, blurred, -0.5, 0, img);
    return img;
  }
  //</editor-fold>

  //<editor-fold desc="20 OCR from Regions or images">
  public static final int PAGE_ITERATOR_LEVEL_WORD = 3;
  public static final int PAGE_ITERATOR_LEVEL_LINE = 2;

  public static String readText(Object... args) {
    TextRecognizer tr = TextRecognizer.start();
    String text = "";
    if (tr.isValid()) {
      tr.saveHeight();
      text = readXXXrun(tr, args);
      tr.resetSavedXHeight();
    }
    return text;
  }

  public static String readLine(Object... args) {
    TextRecognizer tr = TextRecognizer.start();
    String text = "";
    if (tr.isValid()) {
      tr.savePSM();
      tr.saveHeight();
      tr.setPSM(PageSegMode.SINGLE_LINE);
      text = readXXXrun(tr, args);
      tr.resetSavedPSM();
      tr.resetSavedXHeight();
    }
    return text;
  }

  public static String readWord(Object... args) {
    TextRecognizer tr = TextRecognizer.start();
    String text = "";
    if (tr.isValid()) {
      tr.savePSM();
      tr.saveHeight();
      tr.setPSM(PageSegMode.SINGLE_WORD);
      text = readXXXrun(tr, args);
      tr.resetSavedPSM();
      tr.resetSavedXHeight();
    }
    return text;
  }

  public static String readChar(Object... args) {
    TextRecognizer tr = TextRecognizer.start();
    String text = "";
    if (tr.isValid()) {
      tr.savePSM();
      tr.saveHeight();
      tr.setPSM(PageSegMode.SINGLE_CHAR);
      text = readXXXrun(tr, args);
      tr.resetSavedPSM();
      tr.resetSavedXHeight();
    }
    return text;
  }

//  private String read(BufferedImage bimg) {
//    if (isValid()) {
//      try {
//        return tess.doOCR(optimize(bimg)).trim().replace("\n\n", "\n");
//      } catch (TesseractException e) {
//        Debug.error("OCR: read: Tess4J: doOCR: %s", e.getMessage());
//      }
//    } else {
//      Debug.error("OCR: read: not valid");
//    }
//    return "";
//  }

  private static String readXXXrun(TextRecognizer tr, Object... args) {
    String text = "";
    BufferedImage bimg = readXXXevalParameters(tr, args);
    if (bimg != null) {
//      if (tr.psm == PageSegMode.AUTO.ordinal()) {
        text = "";
        for (Match line : readLines(bimg)) {
          if (!text.isEmpty()) text += "\n";
          text += line.getText();
        }
//      } else {
//        text = tr.read(bimg);
//      }
    }
    if (Debug.getDebugLevel() > 2) {
      TextRecognizer.status();
    }
    return text;
  }

  private static BufferedImage readXXXevalParameters(TextRecognizer tr, Object... args) {
    BufferedImage bimg = null;
    for (Object arg : args) {
      if (arg instanceof Number) {
        tr.setHeight((Integer) arg);
      } else {
        bimg = getBufferedImage(arg);
      }
    }
    return bimg;
  }

  private static BufferedImage getBufferedImage(Object whatEver) {
    if (whatEver instanceof String) {
      return Image.create((String) whatEver).get();
    } else if (whatEver instanceof File) {
      return Image.create((File) whatEver).get();
    } else if (whatEver instanceof Region) {
      Region reg = (Region) whatEver;
      return reg.getScreen().capture(reg).getImage();
    } else if (whatEver instanceof Image) {
      return ((Image) whatEver).get();
    } else if (whatEver instanceof ScreenImage) {
      return ((ScreenImage) whatEver).getImage();
    } else if (whatEver instanceof BufferedImage) {
      return (BufferedImage) whatEver;
    }
    return null;
  }

  /**
   * @param simg
   * @return the text read
   * @deprecated use readText() instead
   */
  @Deprecated
  public static String doOCR(ScreenImage simg) {
    return doOCR(simg.getImage());
  }

  /**
   * @param bimg
   * @return the text read
   * @deprecated use readText() instead
   */
  @Deprecated
  public static String doOCR(BufferedImage bimg) {
    String text = "";
    TextRecognizer tr = start();
    if (tr.isValid()) {
      text = tr.readText(bimg);
    }
    return text;
  }

  public static List<Match> readStuff(int level, Object... args) {
    return readTextItems(readXXXevalParameters(null, args), level);
  }

  public static List<Match> readLines(BufferedImage bimg) {
    return readTextItems(bimg, PAGE_ITERATOR_LEVEL_LINE);
  }

  public static List<Match> readWords(BufferedImage bimg) {
    return readTextItems(bimg, PAGE_ITERATOR_LEVEL_WORD);
  }

  private static List<Match> readTextItems(BufferedImage bimg, int level) {
    List<Match> lines = new ArrayList<>();
    TextRecognizer tr = start();
    if (tr.isValid()) {
      BufferedImage bimgResized = tr.optimize(bimg);
      List<Word> textItems = tr.getAPI().getWords(bimgResized, level);
      double wFactor = (double) bimg.getWidth() / bimgResized.getWidth();
      double hFactor = (double) bimg.getHeight() / bimgResized.getHeight();
      for (Word textItem : textItems) {
        Rectangle boundingBox = textItem.getBoundingBox();
        Rectangle realBox = new Rectangle(
                (int) (boundingBox.x * wFactor) - 1,
                (int) (boundingBox.y * hFactor) - 1,
                1 + (int) (boundingBox.width * wFactor) + 2,
                1 + (int) (boundingBox.height * hFactor) + 2);
        lines.add(new Match(realBox, textItem.getConfidence(), textItem.getText().trim()));
      }
    }
    return lines;
  }
  //</editor-fold>

  //<editor-fold desc="30 helper">

  /**
   * @return the current screen resolution in dots per inch
   * @deprecated Will be removed in future versions<br>
   * use Toolkit.getDefaultToolkit().getScreenResolution()
   */
  public int getActualDPI() {
    return Toolkit.getDefaultToolkit().getScreenResolution();
  }
  //</editor-fold>

  //<editor-fold desc="99 obsolete">

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
   * deprecated use readText() instead
   *
   * @param simg
   * @return text
   */
  @Deprecated
  public String recognize(ScreenImage simg) {
    BufferedImage bimg = simg.getImage();
    return readText(bimg);
  }

  /**
   * deprecated use readText() instead
   *
   * @param bimg
   * @return text
   */
  @Deprecated
  public String recognize(BufferedImage bimg) {
    return readText(bimg);
  }
  //</editor-fold>

}
