/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import com.sun.jna.Platform;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.LoadLibs;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder.Finder2;
import org.sikuli.script.support.RunTime;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

public class TextRecognizer {

  private static int lvl = 3;

  private static TextRecognizer textRecognizer = null;
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
  public float optimumDPI = 300;
  private float factor() {
    System.out.println(actualDPI);
    return optimumDPI / actualDPI;
  }

  private Tesseract1 tess = null;
  public static TextRecognizer start() {
    if (textRecognizer == null) {
      textRecognizer = new TextRecognizer();
      try {
        textRecognizer.tess = new Tesseract1();
        boolean tessdataOK = extractTessdata();
        if (tessdataOK) {
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
    boolean shouldExtract = false;
    if (textRecognizer.dataPath != null) {
      fTessdataPath = new File(textRecognizer.dataPath, "tessdata");
    } else {
      fTessdataPath = new File(RunTime.get().fSikulixAppPath, "SikulixTesseract/tessdata");
      if (fTessdataPath.exists()) {
        if (!new File(fTessdataPath, "pdf.ttf").exists()) {
          shouldExtract = true;
          FileManager.deleteFileOrFolder(fTessdataPath);
        }
      } else {
        shouldExtract = true;
      }
    }
    if (shouldExtract) {
      long tessdataStart = new Date().getTime();
      List<String> files = RunTime.get().extractResourcesToFolder("/tessdata", fTessdataPath, null);
      Debug.log("takes %d", new Date().getTime() - tessdataStart);
      if (files.size() == 0) {
        Debug.error("TextRecognizer: start: export tessdata not possible");
      }
    }
    if (fTessdataPath.exists()) {
      textRecognizer.dataPath = fTessdataPath.getAbsolutePath();
      return true;
    }
    return false;
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
          Debug.error("TextRecognizer: setDataPath: not valid - no %s.traineddata (%s)",language, newDataPath);
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
        setVariable("user_defined_dpi", "" + optimumDPI);
                                       
        return tess.doOCR(optimize(bimg));
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: Tess4J: doOCR: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "";
  }
  
  private BufferedImage blur(BufferedImage bimg) {
    Mat mat = Finder2.makeMat(bimg);
    Imgproc.GaussianBlur(mat, mat, new Size(3,3), 0);
    return Finder2.getBufferedImage(mat);
  }
  
  private BufferedImage normalizeSubPixels(BufferedImage bimg) {
    int width = bimg.getWidth();
    int height = bimg.getHeight();
    
    BufferedImage normalized = new BufferedImage(width * 3, height * 3, BufferedImage.TYPE_BYTE_GRAY);
        
    for (int y = 0; y < height; y++) {
      for(int x = 0; x < width; x++) {
        Color rgb = new Color(bimg.getRGB(x, y));
        int red = rgb.getRed();                
        int green = rgb.getGreen();
        int blue = rgb.getBlue();
        
        int redSubPixel = new Color(red,red,red).getRGB();
        int greenSubPixel = new Color(green,green,green).getRGB();
        int blueSubPixel = new Color(blue,blue,blue).getRGB();
                       
        for(int yi = 0; yi < 3; yi++) {
          normalized.setRGB(x * 3, y * 3 + yi, redSubPixel);
          normalized.setRGB(x * 3 + 1, y * 3 + yi, greenSubPixel); 
          normalized.setRGB(x * 3 + 2, y * 3 + yi, blueSubPixel);
        }
      }
    } 
    
    return normalized;
  }   

  public BufferedImage optimize(BufferedImage bimg) {
    bimg = normalizeSubPixels(bimg);    
    
    actualDPI = Toolkit.getDefaultToolkit().getScreenResolution();
    float rFactor = factor() / 3; // normalizeSubPixels already scales by a factor of 3
        
    if (rFactor > 1) {
      int newW = (int) (rFactor * bimg.getWidth());
      int newH = (int) (rFactor * bimg.getHeight());
      bimg = ImageHelper.getScaledInstance(bimg, newW, newH);
      
      BufferedImage target = new BufferedImage(newW, newH, bimg.getType());
      Graphics2D g2 = target.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.drawImage(bimg, 0, 0, newW, newH, null);
      g2.dispose();
      bimg = target;
      
    }
//    
//      bimg = blur(bimg);
    
    try {
      ImageIO.write(bimg, "PNG", new File("/tmp/normalized.png"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return bimg;
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
