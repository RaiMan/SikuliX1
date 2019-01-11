/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import com.sun.jna.Platform;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.LoadLibs;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class TextRecognizer {

  private static int lvl = 3;

  private static TextRecognizer textRecognizer = null;
  private TextRecognizer() {
    Finder2.init();
  }

  public boolean isValid() {
    if (tess == null) {
      return false;
    }
    return true;
  }

  public int getActualDPI() {
    return actualDPI;
  }

  private int actualDPI = 72;
  private float optimumDPI = 300;
  private float factor() {
    return optimumDPI / actualDPI;
  }

  private Tesseract1 tess = null;
  public static TextRecognizer start() {
    if (textRecognizer == null) {
      textRecognizer = new TextRecognizer();
      if (RunTime.get().runningWindows && RunTime.get().runningAs.equals(RunTime.RunType.OTHER)) {
        File fLibs = RunTime.get().fLibsFolder;
        String pLibs = fLibs.getAbsolutePath();
        System.setProperty("jna.library.path", pLibs);
        String libFolder = "/" + Platform.RESOURCE_PREFIX + "/";
        Class tessClass = net.sourceforge.tess4j.Tesseract.class;
        String tessLib = LoadLibs.LIB_NAME;
        String nTessLib = tessLib + ".dll";
        String pTessLib = libFolder + nTessLib;
        File fTessLib = new File(fLibs, nTessLib);
        if (!fTessLib.exists()) {
          try (FileOutputStream outFile = new FileOutputStream(fTessLib);
               InputStream inpTessLib = tessClass.getResourceAsStream(pTessLib)) {
            RunTime.copy(inpTessLib, outFile);
          } catch (IOException ex) {
            Debug.error("TextRecognizer: export native lib: %s (%s)", pTessLib, ex.getMessage());
            return null;
          }
        }
        Class leptClass = net.sourceforge.lept4j.Box.class;
        String leptLib = net.sourceforge.lept4j.util.LoadLibs.LIB_NAME;
        String nLeptLib = leptLib + ".dll";
        String pLeptLib = libFolder + nLeptLib;
        File fLeptLib = new File(fLibs, nLeptLib);
        if (!fLeptLib.exists()) {
          try (FileOutputStream outFile = new FileOutputStream(fLeptLib);
               InputStream inpLeptLib = leptClass.getResourceAsStream(pLeptLib)) {
            RunTime.copy(inpLeptLib, outFile);
          } catch (IOException ex) {
            Debug.error("TextRecognizer: export native lib: %s (%s)", pLeptLib, ex.getMessage());
            return null;
          }
        }
      } else {
        System.setProperty("jna.library.path", RunTime.get().fLibsFolder.getAbsolutePath());
      }
      try {
        textRecognizer.tess = new Tesseract1();
        if (extractTessdata()) {
          Debug.log(lvl, "TextRecognizer: start: data folder: %s", textRecognizer.dataPath);
          textRecognizer.tess.setDatapath(textRecognizer.dataPath);
          if (!new File(textRecognizer.dataPath, textRecognizer.language + ".traineddata").exists()) {
            Debug.error("TextRecognizer: start: no %s.traineddata - provide another language", textRecognizer.language);
          } else {
            Debug.log(lvl, "TextRecognizer: start: language: %s", textRecognizer.language);
          }
        } else {
          textRecognizer = null;
          Debug.error("TextRecognizer: start: no valid tesseract data folder");
        }
      } catch (Exception e) {
        textRecognizer = null;
        Debug.error("TextRecognizer: start: %s", e.getMessage());
      }
    }
    if (null == textRecognizer) {
      RunTime.get().terminate(999, "fatal: TextRecognizer could not be initialized");
    }
    textRecognizer.setLanguage(textRecognizer.language);
    return textRecognizer;
  }

  public static boolean extractTessdata() {
    File fTessdataPath;
    if (dataPath != null) {
      fTessdataPath = new File(FileManager.slashify(dataPath, false), "tessdata");
    } else {
      fTessdataPath = new File(RunTime.get().fSikulixAppPath, "SikulixTesseract/tessdata");
      if (!fTessdataPath.exists()) {
        if (null == RunTime.get().extractTessData(fTessdataPath)) {
          Debug.error("TextRecognizer: start: export tessdata not possible");
        }
      }
    }
    if (fTessdataPath.exists()) {
      dataPath = fTessdataPath.getAbsolutePath();
      return true;
    }
    return false;
  }

  public Tesseract1 getAPI() {
    return tess;
  }

  private int oem = -1;
  private int psm = -1;
  private static String dataPath = Settings.OcrDataPath;
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
      tess.setPageSegMode(this.psm);
    }
    return this;
  }

  public TextRecognizer setDataPath(String dataPath) {
    if (isValid()) {
      if (new File(dataPath).exists()) {
        if (new File(dataPath, language + ".traineddata").exists()) {
          this.dataPath = dataPath;
          tess.setDatapath(this.dataPath);
        } else {
          Debug.error("TextRecognizer: setDataPath: not valid - no %s.traineddata (%s)",language, dataPath);
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
    actualDPI = Toolkit.getDefaultToolkit().getScreenResolution();
    BufferedImage resizedBimg = bimg;
    float rFactor = factor();
    if (rFactor > 1) {
      int newW = (int) (rFactor * bimg.getWidth());
      int newH = (int) (rFactor * bimg.getHeight());
      resizedBimg = ImageHelper.getScaledInstance(bimg, newW, newH);
    }
    return  resizedBimg;
  }

  public Region rescale(Rectangle rect) {
    Region reg = new Region();
    reg.x = (int) (rect.getX() / factor());
    reg.y = (int) (rect.getY() / factor());
    reg.w = (int) (rect.getWidth() / factor()) + 2;
    reg.h = (int) (rect.getHeight() / factor()) + 2;
    return reg;
  }

  public Region relocate(Rectangle rect, Region base) {
    Region reg = rescale(rect);
    reg.x += base.x;
    reg.y += base.y;
    reg.setScreen(base.getScreen().getID());
    return reg;
  }

  public Rectangle relocateAsRectangle(Rectangle rect, Region base) {
    Region reg = relocate(rect, base);
    return new Rectangle(reg.x, reg.y, reg.w, reg.h);
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
