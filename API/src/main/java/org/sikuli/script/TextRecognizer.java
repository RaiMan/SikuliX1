/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXOpenCV;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Intended to be used only internally - still public for being backward compatible
 *<p></p>
 * <b>New projects should use class OCR</b>
 *<p></p>
 * Implementation of the Tess4J/Tesseract API
 *
 */
public class TextRecognizer {

  private TextRecognizer() {
  }

  private static int lvl = 3;

  private static final String versionTess4J = "4.4.1";
  private static final String versionTesseract = "4.1.0";

  private OCR.Options options;

  //<editor-fold desc="00 instance, reset">
  /**
   * New TextRecognizer instance using the global options.
   * @return instance
   * @deprecated no longer needed at all
   */
  @Deprecated
  public static TextRecognizer start() {
    return TextRecognizer.get(OCR.globalOptions());
  }

  /**
   * INTERNAL
   * @param options an Options set
   * @return a new TextRecognizer instance
   */
  protected static TextRecognizer get(OCR.Options options) {
    RunTime.loadLibrary(RunTime.libOpenCV);

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
      String helpURL;
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
      throw new SikuliXception(String.format("OCR: start: Tesseract library problems: %s", e.getMessage()));
    }
  }

  /**
   * @deprecated use OCR.reset() instead
   * @see OCR#reset()
   */
  @Deprecated
  public static void reset() {
    OCR.globalOptions().reset();
  }

  /**
   * @deprecated use OCR.status() instead
   * @see OCR#status()
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
   * @deprecated Use options().oem()
   * @see OCR.Options#oem(OCR.OEM)
   */
  @Deprecated
  public TextRecognizer setOEM(OCR.OEM oem) {
    return setOEM(oem.ordinal());
  }

  /**
   * @param oem
   * @return instance
   * @deprecated use OCR.globalOptions().oem()
   * @see OCR.Options#oem(int)
   */
  @Deprecated
  public TextRecognizer setOEM(int oem) {
    options.oem(oem);
    return this;
  }


  /**
   * @param psm
   * @return instance
   * @deprecated use OCR.globalOptions().psm()
   * @see OCR.Options#psm(OCR.PSM)
   */
  @Deprecated
  public TextRecognizer setPSM(OCR.PSM psm) {
    return setPSM(psm.ordinal());
  }

  /**
   * @param psm
   * @return instance
   * @deprecated use OCR.globalOptions().psm()
   * @see OCR.Options#psm(int)
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
   * @deprecated use OCR.globalOptions().datapath()
   * @see OCR.Options#dataPath()
   */
  @Deprecated
  public TextRecognizer setDataPath(String dataPath) {
    options.dataPath(dataPath);
    return this;
  }

  /**
   * @param language
   * @return instance
   * @deprecated use OCR.globalOptions().language()
   * @see OCR.Options#language(String)
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
   * @deprecated use OCR.globalOptions().variable(String key, String value)
   * @see OCR.Options#variable(String, String)
   */
  @Deprecated
  public TextRecognizer setVariable(String key, String value) {
    options.variable(key, value);
    return this;
  }

  /**
   * @param configs
   * @return instance
   * @deprecated Use OCR.globalOptions.configs(String... configs)
   * @see OCR.Options#configs(String...)
   */
  @Deprecated
  public TextRecognizer setConfigs(String... configs) {
    setConfigs(Arrays.asList(configs));
    return this;
  }

  /**
   * @param configs
   * @return
   * @deprecated Use options.configs
   * @see OCR.Options#configs(List)
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
   * @deprecated use OCR.globalOptions().fontSize(int size)
   * @see OCR.Options#fontSize(int)
   */
  @Deprecated
  public TextRecognizer setFontSize(int size) {
    options.fontSize(size);
    return this;
  }

  /**
   * @param height of an uppercase X in px
   * @deprecated use OCR.globalOptions().textHeight(int height)
   * @see OCR.Options#textHeight(float)
   */
  @Deprecated
  public TextRecognizer setTextHeight(int height) {
    options.textHeight(height);
    return this;
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
      File fTessDataPath = new File(RunTime.get().fSikulixAppFolder, "SikulixTesseract/tessdata");
      boolean shouldExport = RunTime.get().shouldExport();
      boolean fExists = fTessDataPath.exists();
      if (!fExists || shouldExport) {
        if (0 == RunTime.get().extractResourcesToFolder("/tessdataSX", fTessDataPath, null).size()) {
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
    try {
      String text = "";
      if (from instanceof Mat) {
        Mat img = ((Mat) from).clone();
        if (!img.empty()) {
          img = SXOpenCV.optimize(img, options.factor(), options.resizeInterpolation());
          byte[] bytes = new byte[img.width() * img.height()];
          int n = img.get(0, 0, bytes);
          text = getTesseractAPI().doOCR(img.width(), img.height(), ByteBuffer.wrap(bytes), null, 8);
        } else {
          return "";
        }
      } else {
        BufferedImage bimg = SXOpenCV.optimize(Element.getBufferedImage(from), options.factor(), options.resizeInterpolation());
        text = getTesseractAPI().doOCR(bimg);
      }
      return text.trim().replace("\n\n", "\n");
    } catch (TesseractException e) {
      Debug.error("OCR: read: Tess4J: doOCR: %s", e.getMessage());
      return "";
    }
  }

  protected <SFIRBS> List<Match> readTextItems(SFIRBS from, int level) {
    List<Match> lines = new ArrayList<>();
    BufferedImage bimg = Element.getBufferedImage(from);
    BufferedImage bimgResized = SXOpenCV.optimize(bimg, options.factor(), options.resizeInterpolation());
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
   * @deprecated use OCR.readText() instead
   * @see OCR#readText(Object)
   */
  @Deprecated
  public String doOCR(ScreenImage simg) {
    return OCR.readText(simg);
  }

  /**
   * @param bimg
   * @return the text read
   * @deprecated use OCR.readText() instead
   * @see OCR#readText(Object)
   */
  @Deprecated
  public String doOCR(BufferedImage bimg) {
    return OCR.readText(bimg);
  }

  /**
   * @param simg
   * @return text
   * @deprecated use OCR.readText() instead
   * @see OCR#readText(Object)
   */
  @Deprecated
  public String recognize(ScreenImage simg) {
    BufferedImage bimg = simg.getBufferedImage();
    return OCR.readText(bimg);
  }

  /**
   * @param bimg
   * @return text
   * @deprecated use OCR.readText() instead
   * @see OCR#readText(Object)
   */
  @Deprecated
  public String recognize(BufferedImage bimg) {
    return OCR.readText(bimg);
  }
  //</editor-fold>

}
