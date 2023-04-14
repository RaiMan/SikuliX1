/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.runners.ProcessRunner;
import org.sikuli.script.support.Commons;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

  protected static void setValid(boolean state) {
    isValid = state;
  }

  private static final int lvl = 3;

  private static final String versionTess4J = "5.7.0";
  private static String versionTesseract = "???";
  private static String tesseractStamp = "";

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
        if (Commons.runningMacM1()) {
          libPath = "/opt/homebrew/lib";
        }
        File libTess = new File(libPath, "libtesseract.dylib");
        if (libTess.exists()) {
          Commons.jnaPathAdd(libPath);
        } else {
          throw new SikuliXception(String.format("OCR: validate: libtesseract.dylib not in %s", libPath));
        }
      }
      String libVersion = LoadLibs.LIB_NAME.replace("libtesseract", "");
      versionTesseract = String.format("%s.%s.%s", libVersion.substring(0, 1), libVersion.substring(1, 2), libVersion.substring(2));
      if (!Commons.runningWindows()) {
        String tesseract = ProcessRunner.run("tesseract", "--version");
        String runningTesseract = "-None-";
        boolean success = false;
        if (tesseract.startsWith("0\n")) {
          String[] split = tesseract.split("\n");
          if (split[1].startsWith("tesseract")) {
            runningTesseract = split[1].replace("tesseract ", "");
            if (runningTesseract.equals(versionTesseract)) {
              success = true;
            } else {
              String[] versionExpected = versionTesseract.split("\\.");
              String[] versionFound = runningTesseract.split("\\.");
              if (versionFound[0].equals(versionExpected[0])) {
                int subE = Integer.parseInt(versionExpected[1]);
                int subF = Integer.parseInt(versionFound[1]);
                if (subF >= subE) {
                  Debug.log(3, "OCR: Tesseract version found: %s expected: %s --- should work",
                      runningTesseract, versionTesseract);
                }
                versionTesseract = runningTesseract;
                success = true;
              }
            }
          }
        }
        if (!success) {
          Commons.terminate(999, "OCR/TextRecognizer: Tesseract version found: %s expected: %s",
              runningTesseract, versionTesseract);
        }
      }
      Debug.log(lvl, "OCR: Tess4J %s --- Tesseract %s", versionTess4J, versionTesseract);
      tesseractStamp = String.format("tes4j-%s_tesseract-%s", versionTess4J, versionTesseract);
      Commons.loadOpenCV();

      //initDefaultDataPath();
      File sxTessdata = new File(Commons.getAppDataPath(), "SikulixTesseract/tessdata");
      if (OCR.Options.defaultDataPath == null) {
        if (Settings.OcrDataPath != null) {
          File defaultDataFolder = new File(Settings.OcrDataPath, "tessdata");
          if (!defaultDataFolder.exists()) {
            defaultDataFolder = defaultDataFolder.getParentFile();
          }
          OCR.Options.defaultDataPath = defaultDataFolder.getAbsolutePath();
        } else {
          OCR.Options.defaultDataPath = sxTessdata.getAbsolutePath();
        }
        String resFolder = "/tessdata";
        File targetFolder = new File(OCR.globalOptions().dataPath());
        if (targetFolder.equals(sxTessdata)) {
          try {
            if ((!tesseractStamp.isEmpty() && !new File(targetFolder, tesseractStamp).exists()) ||
                !new File(targetFolder, "eng.traineddata").exists()) {
              List<String> contentList = Commons.getFileList(resFolder, OCR.classTesseract);
              for (String res : contentList) {
                String targetName = res.substring(resFolder.length() + 1);
                if (targetName.startsWith("osd.") || targetName.startsWith("pdf.")) {
                  continue;
                }
                Commons.copyResourceToFile(res, OCR.classTesseract, new File(targetFolder, targetName));
              }
              if (contentList.size() > 0 && !new File(targetFolder, tesseractStamp).exists()) {
                FileUtils.touch(new File(targetFolder, tesseractStamp));
              }
            }
          } catch (IOException e) {
            Commons.terminate(999, "OCR/TextRecognizer: tessdata export not possible: %s", targetFolder);
          }
        }
      }

      if (options == null) {
        options = OCR.globalOptions();
      }
      options.validate();

      isValid = true;
    }

    TextRecognizer textRecognizer = new TextRecognizer();
    textRecognizer.options = options;

    return textRecognizer;
  }

  private ITesseract getTesseractAPI() {
    try {
      ITesseract tesseract = new Tesseract1();
      tesseract.setOcrEngineMode(options.oem());
      tesseract.setPageSegMode(options.psm());
      String language = options.language();
      tesseract.setLanguage(language);
      String dataPath = options.dataPath();
      tesseract.setDatapath(dataPath);
      options.validate();
      for (Map.Entry<String, String> entry : options.variables().entrySet()) {
        tesseract.setVariable(entry.getKey(), entry.getValue());
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
    Debug.print("Global settings " + OCR.globalOptions().toString());
  }
  //</editor-fold>

  //<editor-fold desc="02 set OEM, PSM">

  /**
   * @param oem OCR Engine mode
   * @return instance
   * @see OCR.Options#oem(OCR.OEM)
   * @deprecated Use options().oem()
   */
  @Deprecated
  public TextRecognizer setOEM(OCR.OEM oem) {
    return setOEM(oem.ordinal());
  }

  /**
   * @param oem OCR Engine mode
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
   * @param psm Page segmentation mode
   * @return instance
   * @see OCR.Options#psm(OCR.PSM)
   * @deprecated use OCR.globalOptions().psm()
   */
  @Deprecated
  public TextRecognizer setPSM(OCR.PSM psm) {
    return setPSM(psm.ordinal());
  }

  /**
   * @param psm Page segmentation mode
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
   * @param dataPath tessdata path
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
   * @param language tessdata language
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
   * @param key   variable key
   * @param value variable value
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
   * @param configs tessdata configs
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
   * @param configs tessdata configs
   * @return TextRecognizer instance
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
    Mat mimg = Image.makeMat(bimg);

    Imgproc.cvtColor(mimg, mimg, Imgproc.COLOR_BGR2GRAY);

    // sharpen original image to primarily get rid of sub pixel rendering artifacts
    unsharpMask(mimg, 3);

    float rFactor = options.factor();

    if (rFactor > 0 && rFactor != 1) {
      Image.resize(mimg, rFactor, options.resizeInterpolation());
      // sharpen the enlarged image again
      unsharpMask(mimg, 5);
    }

    // invert if font color is said to be light
    if (options.isLightFont()) {
      Core.bitwise_not(mimg, mimg);
    }
    //TODO does it really make sense? invert in case of mainly dark background
//    else if (Core.mean(mimg).val[0] < 127) {
//      Core.bitwise_not(mimg, mimg);
//    }

    return Image.getBufferedImage(mimg);
  }

  /*
   * sharpens the image using an unsharp mask
   */
  private void unsharpMask(Mat img, double sigma) {
    Mat blurred = new Mat();
    Imgproc.GaussianBlur(img, blurred, new Size(), sigma, sigma);
    Core.addWeighted(img, 1.5, blurred, -0.5, 0, img);
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
  protected <SFIRBS> String doRead(SFIRBS from) {
    BufferedImage bimg = Element.getBufferedImage(from);
    if (bimg == null) {
      Debug.error("OCR: read: %s (no image)", from);
      return "";
    }
    String text;
    try {
      ITesseract tesseractAPI = getTesseractAPI();
      text = tesseractAPI.doOCR(optimize(bimg));
      text = text.trim().replace("\n\n", "\n");
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
   * @param simg ScreenImage
   * @return the text read
   * @see OCR#readText(Object)
   * @deprecated use OCR.readText() instead
   */
  @Deprecated
  public String doOCR(ScreenImage simg) {
    return OCR.readText(simg);
  }

  /**
   * @param bimg BufferedImage
   * @return the text read
   * @see OCR#readText(Object)
   * @deprecated use OCR.readText() instead
   */
  @Deprecated
  public String doOCR(BufferedImage bimg) {
    return OCR.readText(bimg);
  }

  /**
   * @param simg ScreenImage
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
   * @param bimg BufferedImage
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
