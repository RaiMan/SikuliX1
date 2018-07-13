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

  public boolean isValid() {
    return valid;
  }

  public String read(Image image) {
    if (valid) {
      try {
        return tess.doOCR(image.get());
      } catch (TesseractException e) {
        Debug.error("TextRecognizer: read: %s", e.getMessage());
      }
    } else {
      Debug.error("TextRecognizer: read: not valid");
    }
    return "did not work";
  }
  private static TextRecognizer _instance = null;
  private static boolean initSuccess = false;
	private static int lvl = 3;

  static {
    //TODO RunTime.loadLibrary("VisionProxy");
  }

  private TextRecognizer() {
    init();
  }

  private void init() {
    tess = new Tesseract1();
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
      //TODO VisionNative.initOCR(FileManager.slashify(Settings.OcrDataPath, true));
      //Debug.log(lvl, "TextRecognizer: init OK: using as data folder:\n%s", Settings.OcrDataPath);
      Debug.error("TextRecognizer: currently not useable");
      Settings.OcrTextRead = false;
      Settings.OcrTextSearch = false;
      initSuccess = false;
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

	public static void reset() {
		_instance = null;
		//TODO VisionNative.setSParameter("OCRLang", Settings.OcrLanguage);
	}

  public static void reset(String language) {
    _instance = null;
    //TODO VisionNative.setSParameter("OCRLang", language);
  }

  public enum ListTextMode {
    WORD, LINE, PARAGRAPH
  };

  public List<Match> listText(ScreenImage simg, Region parent) {
    return listText(simg, parent, ListTextMode.WORD);
  }

  //TODO: listText() support LINE and PARAGRAPH
  // listText only supports WORD mode now.
  public List<Match> listText(ScreenImage simg, Region parent, ListTextMode mode) {
    List<Match> ret = new LinkedList<Match>();
//    MatNative mat = Image.convertBufferedImageToMat(simg.getImage());
//    OCRWords words = VisionNative.recognize_as_ocrtext(mat).getWords();
//    for (int i = 0; i < words.size(); i++) {
//      OCRWord w = words.get(i);
//      Match m = new Match(parent.x + w.getX(), parent.y + w.getY(), w.getWidth(), w.getHeight(),
//              w.getScore(), parent.getScreen(), w.getString());
//      ret.add(m);
//    }
    return ret;
  }

  public String recognize(ScreenImage simg) {
    BufferedImage img = simg.getImage();
    return recognize(img);
  }

  public String recognize(BufferedImage img) {
    if (initSuccess) {
      //MatNative mat = Image.convertBufferedImageToMat(img);
      //TODO return VisionNative.recognize(mat).trim();
    }
    return "";
  }

  public String recognizeWord(ScreenImage simg) {
    BufferedImage img = simg.getImage();
    return recognizeWord(img);
  }

  public String recognizeWord(BufferedImage img) {
    if (initSuccess) {
      //MatNative mat = Image.convertBufferedImageToMat(img);
      //TODO return VisionNative.recognizeWord(mat).trim();
    }
    return "";
  }
}
