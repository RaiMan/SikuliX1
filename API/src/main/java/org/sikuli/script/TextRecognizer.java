/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class TextRecognizer {

  static RunTime runTime = RunTime.get();
  private static int lvl = 3;

  private static TextRecognizer textRecognizer = null;
  private TextRecognizer() { }

  private static boolean valid = false;
  public boolean isValid() {
    return valid;
  }

  private Tesseract1 tess = null;
  public static TextRecognizer start() {
    if (textRecognizer == null) {
      textRecognizer = new TextRecognizer();
      textRecognizer.tess = new Tesseract1();
      File fTessdataPath = null;
      valid = false;
      if (textRecognizer.dataPath != null) {
        fTessdataPath = new File(FileManager.slashify(textRecognizer.dataPath, false), "tessdata");
        valid = fTessdataPath.exists();
      }
      if (!valid) {
        fTessdataPath = new File(runTime.fSikulixAppPath, "SikulixTesseract/tessdata");
        if (!(valid = fTessdataPath.exists())) {
          if (!(valid = (null != runTime.extractTessData(fTessdataPath)))) {
            Debug.error("TextRecognizer: start: export tessdata not possible");
          }
        }
        if (valid) {
          textRecognizer.dataPath = fTessdataPath.getAbsolutePath();
        }
      }
      if (!new File(fTessdataPath, "eng.traineddata").exists()) {
        Debug.error("TextRecognizer: start: no eng.traineddata");
        valid = false;
      }
      if (!valid) {
        Debug.error("TextRecognizer not working: tessdata stuff not available at:\n%s", fTessdataPath);
      } else {
        Debug.log(lvl, "TextRecognizer: init OK: using as data folder:\n%s", fTessdataPath);
        textRecognizer.tess.setDatapath(textRecognizer.dataPath);
      }
    }
    return textRecognizer;
  }

  public Tesseract1 getAPI() {
    return tess;
  }

  private int oem = -1;
  private int psm = -1;
  private String dataPath = Settings.OcrDataPath;
  private String language = Settings.OcrLanguage;

  /**
   * OCR Engine modes:
   *   0    Original Tesseract only.
   *   1    Cube only.
   *   2    Tesseract + cube.
   *   3    Default, based on what is available.
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

  /**
   * Page segmentation modes:
   *   0    Orientation and script detection (OSD) only.
   *   1    Automatic page segmentation with OSD.
   *   2    Automatic page segmentation, but no OSD, or OCR.
   *   3    Fully automatic page segmentation, but no OSD. (Default)
   *   4    Assume a single column of text of variable sizes.
   *   5    Assume a single uniform block of vertically aligned text.
   *   6    Assume a single uniform block of text.
   *   7    Treat the image as a single text line.
   *   8    Treat the image as a single word.
   *   9    Treat the image as a single word in a circle.
   *  10    Treat the image as a single character.
   *  11    Sparse text. Find as much text as possible in no particular order.
   *  12    Sparse text with OSD.
   *  13    Raw line. Treat the image as a single text line, bypassing hacks that are Tesseract-specific.
   * @param psm
   * @return the textRecognizer instance
   */
  public TextRecognizer setPSM(int psm) {
    if (psm < 0 || psm > 13) {
      Debug.error("Tesseract: psm invalid (%d) - using default (3)", psm);
      psm = 3;
    }
    if (isValid()) {
      this.psm = psm;
      tess.setOcrEngineMode(this.oem);
    }
    return this;
  }

  public TextRecognizer setDataPath(String dataPath) {
    if (isValid()) {
      if (new File(dataPath).exists()) {
        if (new File(dataPath, "eng.traineddata").exists()) {
          this.dataPath = dataPath;
          tess.setDatapath(this.dataPath);
        } else {
          Debug.error("TextRecognizer: setDataPath: not valid - no eng.traineddata (%s)",dataPath);
        }
      }
    }
    return this;
  }

  public TextRecognizer setLanguage(String language) {
    if (isValid()) {
      if (new File(dataPath, language + ".traineddata").exists()) {
        this.language = language;
        tess.setLanguage(this.language);
      } else {
        Debug.error("TextRecognizer: setLanguage: no %s.traineddata - still using %s", language, this.language);
      }
    }
    return this;
  }

  public TextRecognizer setVariable(String key, String value) {
    if (isValid()) {
      tess.setTessVariable(key, value);
    }
    return this;
  }

  public TextRecognizer setConfigs(List<String> configs) {
    if (isValid()) {
      tess.setConfigs(configs);
    }
    return this;
  }

  public static String doOCR(ScreenImage simg) {
    return doOCR(simg.getImage());
  }

  public static String doOCR(BufferedImage bimg) {
    TextRecognizer tr = start();
    String text = "";
    if (tr.isValid()) {
      text = tr.read(bimg);
    }
    return text;
  }

  private String read(BufferedImage bimg) {
    if (isValid()) {
      try {
        return tess.doOCR(resize(bimg));
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: Tess4J: doOCR: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "";
  }

  public BufferedImage resize(BufferedImage bimg) {
    int actualDPI = 72;
    float optimumDPI = 300;
    float factor = optimumDPI / actualDPI;
    BufferedImage resizedBimg = bimg;
    if (factor > 1) {
      int newW = (int) (factor * bimg.getWidth());
      int newH = (int) (factor * bimg.getHeight());
      resizedBimg = ImageHelper.getScaledInstance(bimg, newW, newH);
    }
    return  resizedBimg;
  }

  /**
   * use start() instead
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
   * no longer needed - use start() and setXXX()
   */
  @Deprecated
  public static void reset() {
		textRecognizer = null;
		start();
	}

  /**
   * no longer needed - use start() and setXXX()
   */
  @Deprecated
  public static void reset(String language) {
    textRecognizer = null;
    start().setLanguage(language);
  }

  /**
   * deprecated use doOCR() instead
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
   * @param bimg
   * @return text
   */
  @Deprecated
  public String recognize(BufferedImage bimg) {
    return read(bimg);
  }
}
