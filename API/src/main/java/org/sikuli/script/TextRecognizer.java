/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.awt.image.BufferedImage;

import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.sikuli.basics.Settings;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.Debug;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class TextRecognizer {

  static RunTime runTime = RunTime.get();

  private Tesseract1 tess = null;
  private boolean valid = false;

  private static TextRecognizer _instance = null;
  private static boolean initSuccess = false;
  private static int lvl = 3;

  private TextRecognizer() {
    init();
  }

  private void init() {
    File fTessdataPath = null;
    initSuccess = false;
    if (Settings.OcrDataPath != null) {
      fTessdataPath = new File(FileManager.slashify(Settings.OcrDataPath, false), "tessdata");
      initSuccess = fTessdataPath.exists();
    }
    if(!initSuccess) {
      fTessdataPath = new File(runTime.fSikulixAppPath, "SikulixTesseract/tessdata");
      if (!(initSuccess = fTessdataPath.exists())) {
        if (!(initSuccess = (null != runTime.extractTessData(fTessdataPath)))) {
          Debug.error("TextRecognizer: init: export tessdata not possible - run setup with option 3");
        }
      }
		}
		if (!new File(fTessdataPath, "eng.traineddata").exists()) {
			initSuccess = false;
		}
    if (!initSuccess) {
      Debug.error("TextRecognizer not working: tessdata stuff not available at:\n%s", fTessdataPath);
      Settings.OcrTextRead = false;
      Settings.OcrTextSearch = false;
    } else {
      Settings.OcrDataPath = fTessdataPath.getParent();
      Debug.log(lvl, "TextRecognizer: init OK: using as data folder:\n%s", Settings.OcrDataPath);
      tess = new Tesseract1();
      tess.setDatapath(Settings.OcrDataPath);
    }
    valid = initSuccess;
  }

  public static TextRecognizer getInstance() {
    if (_instance == null) {
      _instance = new TextRecognizer();
    }
    if (!initSuccess) {
      return null;
    }
    return _instance;
  }

  public boolean isValid() {
    return valid;
  }

  public static void reset() {
		_instance = null;
		//TODO VisionNative.setSParameter("OCRLang", Settings.OcrLanguage);
	}

  public static void reset(String language) {
    _instance = null;
    //TODO VisionNative.setSParameter("OCRLang", language);
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
        return tess.doOCR(Image.resize(bimg, 2));
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: Tess4J: doOCR: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "";
  }
}
