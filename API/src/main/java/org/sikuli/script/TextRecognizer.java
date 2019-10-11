/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder.Finder2;
import org.sikuli.script.support.RunTime;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TextRecognizer {

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
    COUNT // 13
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
  
  private static int lvl = 3;

  private static TextRecognizer textRecognizer = null;
  public static String versionTess4J = "4.4.1";
  public static String versionTesseract = "4.1.0";

  private TextRecognizer() {
    Finder.Finder2.init();
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
  public float optimumDPI = 192;

  private float factor() {
    return optimumDPI / actualDPI;
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
        String libName = RunTime.get().runningMac ? "libtesseract.dylib" : "libtesseract.so";
        Debug.error("TextRecognizer: start: Tesseract library not found (%s)", libName);
        String helpURL = "https://github.com/RaiMan/SikuliX1/wiki/macOS-Linux:-Support-libraries-for-Tess4J-Tesseract-4-OCR";
        if (RunTime.isIDE()) {
          Debug.error("Save your work, correct the problem and restart the IDE!");
          try {
            Desktop.getDesktop().browse(new URI(helpURL));
          } catch (IOException ex) {
          } catch (URISyntaxException ex) {
          }
        }
        Debug.error("see: " + helpURL);
      }
    }
    if (null == textRecognizer) {
      //RunTime.get().terminate(999, "TextRecognizer could not be initialized");
      throw new SikuliXception(String.format("fatal: " + "TextRecognizer could not be initialized"));
    }
    textRecognizer.setLanguage(textRecognizer.language);
    return textRecognizer;
  }

  public static boolean extractTessdata() {
    File fTessDataPath;
    File fTessConfNodict;
    File fTessEngTData;
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
      textRecognizer.dataPath = fTessDataPath.getAbsolutePath();
      textRecognizer.hasOsdTrData = new File(textRecognizer.dataPath, "osd.traineddata").exists();
      return true;
    }
    return false;
  }

  public Tesseract1 getAPI() {
    return tess;
  }

  private int oem = -1;
  private int psm = -1;
  private boolean dataPathProvided = false;
  private String dataPath = null;
  private String language = Settings.OcrLanguage;

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
    if (isValid()) {
      if (psm == PageSegMode.OSD_ONLY.ordinal() || psm == PageSegMode.AUTO_OSD.ordinal()
              || psm == PageSegMode.SPARSE_TEXT_OSD.ordinal()) {
        if(!hasOsdTrData) {
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

  public TextRecognizer setVariable(String key, String value) {
    if (isValid()) {
      tess.setTessVariable(key, value);
    }
    return this;
  }

  public TextRecognizer setConfigs(String... configs) {
    if (isValid()) {
      tess.setConfigs(Arrays.asList(configs));
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
        return tess.doOCR(optimize(bimg));
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: Tess4J: doOCR: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "";
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
    Mat img = Finder2.makeMat(bimg);

    Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);

    // sharpen original image to primarily get rid of sub pixel rendering artifacts
    img = unsharpMask(img, 3);

    // Resize to optimumDPI
    actualDPI = Toolkit.getDefaultToolkit().getScreenResolution();
    float rFactor = factor();

    if (rFactor > 1) {
      int newW = (int) (rFactor * bimg.getWidth());
      int newH = (int) (rFactor * bimg.getHeight());
      Imgproc.resize(img, img, new Size(newW, newH), 0, 0, Imgproc.INTER_CUBIC);
    }

    // sharpen the enlarged image again
    img = unsharpMask(img, 5);

    // invert in case of mainly dark background
    if (Core.mean(img).val[0] < 127) {
      Core.bitwise_not(img, img);
    }

    // configure tesseract to handle the resized image correctly
    setVariable("user_defined_dpi", "" + optimumDPI);

    return Finder2.getBufferedImage(img);

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

  public static void reset() {
    textRecognizer = null;
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
}
