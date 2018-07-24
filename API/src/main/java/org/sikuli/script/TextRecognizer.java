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

  private Tesseract1 tess = null;

  private int oem = -1;
  private int psm = -1;
  private String dataPath = Settings.OcrDataPath;
  private String language = Settings.OcrLanguage;

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
      this.dataPath = dataPath;
      tess.setDatapath(this.dataPath);
    }
    return this;
  }

  public TextRecognizer setLanguage(String language) {
    if (isValid()) {
      this.language = language;
      tess.setLanguage(this.language);
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

  private static TextRecognizer textRecognizer = null;
  private TextRecognizer() { }

  private static boolean valid = false;
  public boolean isValid() {
    return valid;
  }

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

  public static String doOCR(BufferedImage bimg) {
    TextRecognizer tr = start();
    String text = "";
    if (tr.isValid()) {
      text = tr.read(bimg);
    }
    return text;
  }

  public String recognize(ScreenImage simg) {
    BufferedImage bimg = simg.getImage();
    return read(bimg);
  }

  public String recognize(BufferedImage bimg) {
    return read(bimg);
  }

  public String read(Image image) {
    return read(image.get());
  }

  public String read(BufferedImage bimg) {
    if (isValid()) {
      try {
        int actualDPI = 72;
        float optimumDPI = 300;
        float factor = optimumDPI / actualDPI;
        BufferedImage resizedBimg = bimg;
        if (factor > 1) {
          int newW = (int) (factor * bimg.getWidth());
          int newH = (int) (factor * bimg.getHeight());
          resizedBimg = ImageHelper.getScaledInstance(bimg, newW, newH);
        }
        return tess.doOCR(resizedBimg);
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: Tess4J: doOCR: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "";
  }
}
