/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.support.Commons;
import org.sikuli.support.RunTime;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Intended to be used only internally - still public for being backward compatible
 * <p></p>
 * <b>New projects should use class OCR</b>
 * <p></p>
 * Implementation of the Tess4J/Tesseract API
 */
public class TextRecognizer {

  private TextRecognizer() {
  }

  private static boolean isValid = false;

  private static int lvl = 3;

  private static final String versionTess4J = "4.5.4";
  private static final String versionTesseract = "4.1.x";

  private OCR.Options options;

  //<editor-fold desc="00 instance, reset">

  /**
   * New TextRecognizer instance using the global options.
   *
   * @return instance
   * @deprecated no longer needed at all
   */
  @Deprecated
  public static TextRecognizer start() {
    return TextRecognizer.get(OCR.globalOptions());
  }

  /**
   * INTERNAL
   *
   * @param options an Options set
   * @return a new TextRecognizer instance
   */
  protected static TextRecognizer get(OCR.Options options) {
    if (!isValid) {
      //TODO Tess4J: macOS: tesseract library load problem
      if (Commons.runningMac()) {
        String libPath = "/usr/local/lib";
        File libTess = new File(libPath, "libtesseract.dylib");
        if (libTess.exists()) {
          Commons.jnaPathAdd(libPath);
        } else {
          throw new SikuliXception(String.format("OCR: validate: libtesseract.dylib not in /usr/local/lib"));
        }
      }
      Commons.loadOpenCV();
      isValid = true;
    }

    initDefaultDataPath();

    Debug.log(lvl, "OCR: start: Tess4J %s using Tesseract %s", versionTess4J, versionTesseract);
    if (options == null) {
      options = OCR.globalOptions();
    }
    options.validate();

    TextRecognizer textRecognizer = new TextRecognizer();
    textRecognizer.options = options;

    return textRecognizer;
  }

  private ITesseract getTesseractAPI() {
    try {
      ITesseract tesseract = new Tesseract1();
      tesseract.setOcrEngineMode(options.oem());
      tesseract.setPageSegMode(options.psm());
      tesseract.setLanguage(options.language());
      tesseract.setDatapath(options.dataPath());
      for (Map.Entry<String, String> entry : options.variables().entrySet()) {
        tesseract.setTessVariable(entry.getKey(), entry.getValue());
      }
      if (!options.configs().isEmpty()) {
        tesseract.setConfigs(new ArrayList<>(options.configs()));
      }
      return tesseract;
    } catch (UnsatisfiedLinkError e) {
      //TODO open website on Error
/*
      String helpURL;
      if (Commons.runningWindows()) {
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
*/
      throw new SikuliXception(String.format("OCR: start: Tesseract library problems: %s", e.getMessage()));
    }
  }

  /**
   * @see OCR#reset()
   * @deprecated use OCR.reset() instead
   */
  @Deprecated
  public static void reset() {
    OCR.globalOptions().reset();
  }

  /**
   * @see OCR#status()
   * @deprecated use OCR.status() instead
   */
  @Deprecated
  public static void status() {
    Debug.logp("Global settings " + OCR.globalOptions().toString());
  }
  //</editor-fold>

  //<editor-fold desc="02 set OEM, PSM">

  /**
   * @param oem
   * @return instance
   * @see OCR.Options#oem(OCR.OEM)
   * @deprecated Use options().oem()
   */
  @Deprecated
  public TextRecognizer setOEM(OCR.OEM oem) {
    return setOEM(oem.ordinal());
  }

  /**
   * @param oem
   * @return instance
   * @see OCR.Options#oem(int)
   * @deprecated use OCR.globalOptions().oem()
   */
  @Deprecated
  public TextRecognizer setOEM(int oem) {
    options.oem(oem);
    return this;
  }


  /**
   * @param psm
   * @return instance
   * @see OCR.Options#psm(OCR.PSM)
   * @deprecated use OCR.globalOptions().psm()
   */
  @Deprecated
  public TextRecognizer setPSM(OCR.PSM psm) {
    return setPSM(psm.ordinal());
  }

  /**
   * @param psm
   * @return instance
   * @see OCR.Options#psm(int)
   * @deprecated use OCR.globalOptions().psm()
   */
  @Deprecated
  public TextRecognizer setPSM(int psm) {
    options.psm(psm);
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="03 set datapath, language, variable, configs">

  /**
   * @param dataPath
   * @return instance
   * @see OCR.Options#dataPath()
   * @deprecated use OCR.globalOptions().datapath()
   */
  @Deprecated
  public TextRecognizer setDataPath(String dataPath) {
    options.dataPath(dataPath);
    return this;
  }

  /**
   * @param language
   * @return instance
   * @see OCR.Options#language(String)
   * @deprecated use OCR.globalOptions().language()
   */
  @Deprecated
  public TextRecognizer setLanguage(String language) {
    options.language(language);
    return this;
  }

  /**
   * @param key
   * @param value
   * @return instance
   * @see OCR.Options#variable(String, String)
   * @deprecated use OCR.globalOptions().variable(String key, String value)
   */
  @Deprecated
  public TextRecognizer setVariable(String key, String value) {
    options.variable(key, value);
    return this;
  }

  /**
   * @param configs
   * @return instance
   * @see OCR.Options#configs(String...)
   * @deprecated Use OCR.globalOptions.configs(String... configs)
   */
  @Deprecated
  public TextRecognizer setConfigs(String... configs) {
    setConfigs(Arrays.asList(configs));
    return this;
  }

  /**
   * @param configs
   * @return
   * @see OCR.Options#configs(List)
   * @deprecated Use options.configs
   */
  @Deprecated
  public TextRecognizer setConfigs(List<String> configs) {
    options.configs(configs);
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="10 image optimization">

  /**
   * @param size expected font size in pt
   * @see OCR.Options#fontSize(int)
   * @deprecated use OCR.globalOptions().fontSize(int size)
   */
  @Deprecated
  public TextRecognizer setFontSize(int size) {
    options.fontSize(size);
    return this;
  }

  /**
   * @param height of an uppercase X in px
   * @see OCR.Options#textHeight(float)
   * @deprecated use OCR.globalOptions().textHeight(int height)
   */
  @Deprecated
  public TextRecognizer setTextHeight(int height) {
    options.textHeight(height);
    return this;
  }

  private BufferedImage optimize(BufferedImage bimg) {
    Mat mimg = Commons.makeMat(bimg);

    Imgproc.cvtColor(mimg, mimg, Imgproc.COLOR_BGR2GRAY);

    // sharpen original image to primarily get rid of sub pixel rendering artifacts
    mimg = unsharpMask(mimg, 3);

    float rFactor = options.factor();

    if (rFactor > 0 && rFactor != 1) {
      Commons.resize(mimg, rFactor, options.resizeInterpolation());
    }

    // sharpen the enlarged image again
    mimg = unsharpMask(mimg, 5);

    // invert if font color is said to be light
    if (options.isLightFont()) {
      Core.bitwise_not(mimg, mimg);
    }
    //TODO does it really make sense? invert in case of mainly dark background
//    else if (Core.mean(mimg).val[0] < 127) {
//      Core.bitwise_not(mimg, mimg);
//    }

    BufferedImage optImg = Commons.getBufferedImage(mimg);
    return optImg;
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

  //<editor-fold desc="20 text, lines, words - internal use">
  protected <SFIRBS> String readText(SFIRBS from) {
    return doRead(from);
  }

  protected <SFIRBS> List<Match> readLines(SFIRBS from) {
    BufferedImage bimg = Element.getBufferedImage(from);
    return readTextItems(bimg, OCR.PAGE_ITERATOR_LEVEL_LINE);
  }

  protected <SFIRBS> List<Match> readWords(SFIRBS from) {
    BufferedImage bimg = Element.getBufferedImage(from);
    return readTextItems(bimg, OCR.PAGE_ITERATOR_LEVEL_WORD);
  }
  //</editor-fold>

  //<editor-fold desc="30 helper">
  private static void initDefaultDataPath() {
    if (OCR.Options.defaultDataPath == null) {
      // export SikuliX eng.traineddata, if libs are exported as well
      File fTessDataPath = new File(Commons.getAppDataPath(), "SikulixTesseract/tessdata");
      boolean shouldExport = Commons.shouldExport();
      boolean fExists = fTessDataPath.exists();
      if (!fExists || shouldExport) {
        if (0 == RunTime.extractResourcesToFolder("/tessdataSX", fTessDataPath, null).size()) {
          throw new SikuliXception(String.format("OCR: start: export tessdata did not work: %s", fTessDataPath));
        }
      }
      // if set, try with provided tessdata parent folder
      String defaultDataPath;
      if (Settings.OcrDataPath != null) {
        defaultDataPath = new File(Settings.OcrDataPath, "tessdata").getAbsolutePath();
      } else {
        defaultDataPath = fTessDataPath.getAbsolutePath();
      }
      OCR.Options.defaultDataPath = defaultDataPath;
    }
  }

  protected <SFIRBS> String doRead(SFIRBS from) {
    String text = "";
    BufferedImage bimg = Element.getBufferedImage(from);
    try {
      text = getTesseractAPI().doOCR(optimize(bimg)).trim().replace("\n\n", "\n");
    } catch (TesseractException e) {
      Debug.error("OCR: read: Tess4J: doOCR: %s", e.getMessage());
      return "";
    }
    return text;
  }

  protected <SFIRBS> List<Match> readTextItems(SFIRBS from, int level) {
    List<Match> lines = new ArrayList<>();
    BufferedImage bimg = Element.getBufferedImage(from);
    BufferedImage bimgResized = optimize(bimg);
    List<Word> textItems = getTesseractAPI().getWords(bimgResized, level);
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
    return lines;
  }
  //</editor-fold>

  //<editor-fold desc="99 obsolete">

  /**
   * @return the current screen resolution in dots per inch
   * @deprecated Will be removed in future versions<br>
   * use Toolkit.getDefaultToolkit().getScreenResolution()
   */
  @Deprecated
  public int getActualDPI() {
    return Toolkit.getDefaultToolkit().getScreenResolution();
  }

  /**
   * @param simg
   * @return the text read
   * @see OCR#readText(Object)
   * @deprecated use OCR.readText() instead
   */
  @Deprecated
  public String doOCR(ScreenImage simg) {
    return OCR.readText(simg);
  }

  /**
   * @param bimg
   * @return the text read
   * @see OCR#readText(Object)
   * @deprecated use OCR.readText() instead
   */
  @Deprecated
  public String doOCR(BufferedImage bimg) {
    return OCR.readText(bimg);
  }

  /**
   * @param simg
   * @return text
   * @see OCR#readText(Object)
   * @deprecated use OCR.readText() instead
   */
  @Deprecated
  public String recognize(ScreenImage simg) {
    BufferedImage bimg = simg.getImage();
    return OCR.readText(bimg);
  }

  /**
   * @param bimg
   * @return text
   * @see OCR#readText(Object)
   * @deprecated use OCR.readText() instead
   */
  @Deprecated
  public String recognize(BufferedImage bimg) {
    return OCR.readText(bimg);
  }
  //</editor-fold>

}
