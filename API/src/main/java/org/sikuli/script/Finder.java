/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.IScreen;
import org.sikuli.script.support.RunTime;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.regex.Matcher;

public class Finder implements Iterator<Match> {

  private Region _region = null;
  private Pattern _pattern = null;
  private Image _image = null;
  private FindInput2 _findInput = new FindInput2();
  private FindResult2 _results = null;
  private Region where = null;

  private int currentMatchIndex;
  private boolean repeating = false;
  private boolean valid = true;
  private boolean screenFinder = true;

  private static String me = "Finder: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  protected Finder() {
    resetFindChanges();
  }

  /**
   * Create a Finder for the given element
   * @param inWhat in what element (RIBS) to search
   * @param <RIBS> Region, Image, BufferedImage, ScreenImage or image filename
   */
  public <RIBS> Finder(RIBS inWhat) {
    if (inWhat instanceof Region) {
      where = (Region) inWhat;
    } else if (inWhat instanceof Image) {
      _findInput.setSource(Finder2.makeMat(((Image) inWhat).get()));
    } else if (inWhat instanceof String) {
      _findInput.setSource(Finder2.makeMat(Image.create((String) inWhat).get()));
    } else if (inWhat instanceof BufferedImage) {
      _findInput.setSource(Finder2.makeMat(((BufferedImage) inWhat)));
    } else if (inWhat instanceof ScreenImage) {
      initScreenFinder(((ScreenImage) inWhat), null);
    } else {
      throw new IllegalArgumentException(String.format("Finder: not possible with: %s", inWhat));
    }
    resetFindChanges();
  }

  /**
   * Finder for a Region on a ScreenImage
   *
   * @param simg   ScreenImage
   * @param region the cropping region
   */
  public Finder(ScreenImage simg, Region region) {
    initScreenFinder(simg, region);
  }

  private void initScreenFinder(ScreenImage simg, Region region) {
    setScreenImage(simg);
    _region = region;
    resetFindChanges();
   }

  protected void setScreenImage(ScreenImage simg) {
    _findInput.setSource(Finder2.makeMat(simg.getImage()));
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="internal repeating">

  /**
   * internal use: to be able to reuse the same Finder
   */
  protected void setRepeating() {
    repeating = true;
  }

  /**
   * internal use: repeat with same Finder
   */
  protected void findRepeat() {
    _results = Finder2.find(_findInput);
    currentMatchIndex = 0;
  }

  /**
   * internal use: repeat with same Finder
   */
  protected void findAllRepeat() {
    Debug timing = Debug.startTimer("Finder.findAll");
    _results = Finder2.find(_findInput);
    currentMatchIndex = 0;
    timing.end();
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="find">

  /**
   * do a find op with the given image or the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param imageOrText image file name or text
   * @return null. if find setup not possible
   */
  public String find(String imageOrText) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    Image img = Image.create(imageOrText);
    if (img.isText()) {
      return findText(imageOrText);
    }
    if (img.isValid()) {
      return find(img);
    }
    return null;
  }

  private Mat possibleImageResizeOrCallback(Image img) {
    return possibleImageResizeOrCallback(img, 0);
  }

  private Mat possibleImageResizeOrCallback(Image img, float oneTimeResize) {
    float factor = oneTimeResize;
    if (factor == 0 && Settings.AlwaysResize > 0 && Settings.AlwaysResize != 1) {
      factor = Settings.AlwaysResize;
    }
    Mat mat = Finder2.makeMat(img.get(), false);
    if (factor > 0 && factor != 1) {
      Debug.log(3, "Finder::possibleImageResizeOrCallback: resize");
      if (!mat.empty()) {
        Image.resize(mat, factor);
      }
    } else if (Settings.ImageCallback != null) {
      Debug.log(3, "Finder::possibleImageResizeOrCallback: callback");
      BufferedImage newBimg = Settings.ImageCallback.callback(img);
      mat = Finder2.makeMat(newBimg, false);
    }
    if (mat.empty()) {
      log(-1, "%s: conversion error --- find will fail", img);
    }
    return mat;
  }

  /**
   * do a find op with the given pattern in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param aPtn Pattern
   * @return null. if find setup not possible
   */
  public String find(Pattern aPtn) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    if (aPtn.isValid()) {
      _pattern = aPtn;
      if (_pattern.hasMask()) {
        _findInput.setMask(_pattern.getMask());
      }
      _image = aPtn.getImage();
      _findInput.setTarget(possibleImageResizeOrCallback(_image, aPtn.getResize()));
      _findInput.setSimilarity(aPtn.getSimilar());
      _findInput.setIsPattern();
      _results = Finder2.find(_findInput);
      currentMatchIndex = 0;
      return aPtn.getFilename();
    } else {
      return null;
    }
  }

  /**
   * do a find op with the given image in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param img Image
   * @return null. if find setup not possible
   */
  public String find(Image img) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    if (img.isValid()) {
      _image = img;
      _findInput.setTarget(possibleImageResizeOrCallback(img));
      _findInput.setSimilarity(Settings.MinSimilarity);
      _results = Finder2.find(_findInput);
      currentMatchIndex = 0;
      return img.getFilename();
    } else if (img.isUseable()) {
      return find(new Pattern(img));
    } else {
      return null;
    }
  }

  /**
   * do a find op with the given image in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param img BufferedImage
   * @return null. if find setup not possible
   */
  public String find(BufferedImage img) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    return find(new Image(img));
  }

  public List<Region> findChanges(Object changedImage) {
    if (SX.isNull(changedImage)) {
      return null;
    }
    if (changedImage instanceof String) {
      Image img = Image.create((String) changedImage);
      _findInput.setTarget(possibleImageResizeOrCallback(img));
    } else if (changedImage instanceof ScreenImage) {
      Image img = new Image((ScreenImage) changedImage);
      _findInput.setTarget(possibleImageResizeOrCallback(img));
    }
    return Finder2.findChanges(_findInput);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="findAll">

  /**
   * do a findAll op with the given image or the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param imageOrText iamge file name or text
   * @return null. if find setup not possible
   */
  public String findAll(String imageOrText) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    Image img = Image.create(imageOrText);
    _image = img;
    if (img.isText()) {
      return findAllText(imageOrText);
    }
    if (img.isValid()) {
      return findAll(img);
    }
    return null;
  }

  /**
   * do a find op with the given pattern in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param aPtn Pattern
   * @return null. if find setup not possible
   */
  public String findAll(Pattern aPtn) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    if (aPtn.isValid()) {
      _pattern = aPtn;
      _image = aPtn.getImage();
      _findInput.setTarget(possibleImageResizeOrCallback(_image, aPtn.getResize()));
      _findInput.setSimilarity(aPtn.getSimilar());
      _findInput.setIsPattern();
      _findInput.setFindAll();
      if (_pattern.hasMask()) {
        _findInput.setMask(_pattern.getMask());
      }
      Debug timing = Debug.startTimer("Finder.findAll");
      _results = Finder2.find(_findInput);
      currentMatchIndex = 0;
      timing.end();
      return aPtn.getFilename();
    } else {
      return null;
    }
  }

  /**
   * do a findAll op with the given image in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param img Image
   * @return null. if find setup not possible
   */
  public String findAll(Image img) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    if (img.isValid()) {
      _image = img;
      _findInput.setTarget(possibleImageResizeOrCallback(img));
      _findInput.setSimilarity(Settings.MinSimilarity);
      _findInput.setFindAll();
      Debug timing = Debug.startTimer("Finder.findAll");
      _results = Finder2.find(_findInput);
      currentMatchIndex = 0;
      timing.end();
      return img.getFilename();
    } else {
      return null;
    }
  }
  //</editor-fold>

  //<editor-fold desc="findText">

  /**
   * do a text find with the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param text text
   * @return null. if find setup not possible
   */
  public String findText(String text) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    _findInput.setTargetText(text);
    _findInput.setWhere(where);
    _results = Finder2.find(_findInput);
    currentMatchIndex = 0;
    return text;
  }

  public boolean findWord(String text) {
    _findInput.setTextLevel(OCR.PAGE_ITERATOR_LEVEL_WORD);
    findText(text);
    return hasNext();
  }

  public boolean findWords(String text) {
    _findInput.setTextLevel(OCR.PAGE_ITERATOR_LEVEL_WORD);
    _findInput.setFindAll();
    findText(text);
    return hasNext();
  }

  public boolean findWords() {
    _findInput.setTextLevel(OCR.PAGE_ITERATOR_LEVEL_WORD);
    _findInput.setFindAll();
    findText("");
    return hasNext();
  }

  public boolean findLine(String text) {
    _findInput.setTextLevel(OCR.PAGE_ITERATOR_LEVEL_LINE);
    findText(text);
    return hasNext();
  }

  public boolean findLines(String text) {
    _findInput.setTextLevel(OCR.PAGE_ITERATOR_LEVEL_LINE);
    _findInput.setFindAll();
    findText(text);
    return hasNext();
  }

  public boolean findLines() {
    _findInput.setTextLevel(OCR.PAGE_ITERATOR_LEVEL_LINE);
    _findInput.setFindAll();
    findText("");
    return hasNext();
  }

  /**
   * do a findAll op with the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   *
   * @param text text
   * @return null. if find setup not possible
   */
  public String findAllText(String text) {
    if (!valid) {
      log(-1, "not valid");
      return null;
    }
    _findInput.setFindAll();
    return findText(text);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Iterator">
  public List<Match> getList() {
    List<Match> matches = new ArrayList<>();
    while (hasNext()) {
      matches.add(next());
    }
    return matches;
  }

  public <RI> List<Match> getListFor(RI what) {
    List<Match> matches = new ArrayList<>();
    if (what instanceof Element)
    while (hasNext()) {
      matches.add(((Element) what).relocate(next()));
    }
    return matches;
  }

  /**
   * @return true if Finder has a next match, false otherwise
   */
  @Override
  public boolean hasNext() {
    if (_results != null && _results.hasNext()) {
      return true;
    }
    return false;
  }

  /**
   * @return the next match or null
   */
  @Override
  public Match next() {
    Match match = null;
    if (hasNext()) {
      match = _results.next();
      if (!_findInput.isText() && _region != null) {
        match.x += _region.x;
        match.y += _region.y;
      }
      IScreen parentScreen = null;
      if (screenFinder && _region != null) {
        parentScreen = _region.getScreen();
        match = Match.create(match, parentScreen);
      }
      if (_pattern != null) {
        Location offset = _pattern.getTargetOffset();
        match.setTargetOffset(offset);
      }
      match.setOnScreen(screenFinder);
      match.setImage(_image);
    }
    return match;
  }

  /*
    public Match next() {
      Match match = null;
      if (hasNext()) {
        FindResult fr = _results.get(_cur_result_i++);
        IScreen parentScreen = null;
        if (screenFinder && _region != null) {
          parentScreen = _region.getScreen();
        }
        match = new Match(fr, parentScreen);
        match.setOnScreen(screenFinder);
        fr.delete();
        if (_region != null) {
          match = _region.toGlobalCoord(match);
        }
        if (_pattern != null) {
          Location offset = _pattern.getTargetOffset();
          match.setTargetOffset(offset);
        }
        match.setImage(_image);
      }
      return match;
    }
  */

  @Override
  public void remove() {
    destroy();
  }

  public void destroy() {
    _findInput = null;
    _results = null;
    _pattern = null;
  }
//</editor-fold>

  static final int PIXEL_DIFF_THRESHOLD_DEFAULT = 3;
  static final int IMAGE_DIFF_THRESHOLD_DEFAULT = 5;
  static int PIXEL_DIFF_THRESHOLD = PIXEL_DIFF_THRESHOLD_DEFAULT;
  static int IMAGE_DIFF_THRESHOLD = IMAGE_DIFF_THRESHOLD_DEFAULT;

  public void resetFindChanges() {
    PIXEL_DIFF_THRESHOLD = PIXEL_DIFF_THRESHOLD_DEFAULT;
    IMAGE_DIFF_THRESHOLD = IMAGE_DIFF_THRESHOLD_DEFAULT;
  }

  public void setFindChangesPixelDiff(int value) {
    PIXEL_DIFF_THRESHOLD = value;
  }

  public void setFindChangesImageDiff(int value) {
    IMAGE_DIFF_THRESHOLD = value;
  }

  protected static class Finder2 {

    static {
      RunTime.loadLibrary(RunTime.libOpenCV);
    }

    protected static void init() {
    }

    //<editor-fold desc="housekeeping">
    static class Log {
      private static String prefix = "UnKnown";

      public Log(String prefix) {
        this.prefix = prefix + ": ";
      }

      public static void error(String msg, Object... args) {
        Debug.error(prefix + msg, args);
      }

      public static void trace(String msg, Object... args) {
        Debug.log(3, prefix + msg, args);
      }
    }

    private static Log log = new Log("Finder2");

    private Mat mBase = getNewMat();
    private Mat mResult = getNewMat();

    private enum FindType {
      ONE, ALL
    }

    private Finder2() {
    }

    public boolean isValid() {
      return !mBase.empty();
    }
    //</editor-fold>

    private FindInput2 fInput = null;

    protected static FindResult2 find(FindInput2 findInput) {
      findInput.setAttributes();
      Finder2 finder2 = new Finder2();
      finder2.fInput = findInput;
      FindResult2 results = finder2.doFind();
      return results;
    }

    private final float resizeMinFactor = 1.5f;
    private final float[] resizeLevels = new float[]{1f, 0.4f};
    private boolean isCheckLastSeen = false;
    private static final double downSimDiff = 0.15;

    private boolean isWord() {
      return fInput.getTextLevel() == OCR.PAGE_ITERATOR_LEVEL_WORD;
    }

    private boolean isLine() {
      return fInput.getTextLevel() == OCR.PAGE_ITERATOR_LEVEL_LINE;
    }

    private boolean isTextMatching(String base, String probe, java.util.regex.Pattern pattern) {
      if (SX.isNull(pattern)) {
        return base.equals(probe);
      }
      Matcher matcher = pattern.matcher(base.trim());
      return matcher.find();
    }

    private boolean isTextContained(String base, String probe, java.util.regex.Pattern pattern) {
      if (SX.isNull(pattern)) {
        return base.contains(probe);
      }
      Matcher matcher = pattern.matcher(base);
      return matcher.find();
    }

    private FindResult2 doFind() {
      if (!fInput.isValid()) {
        return null;
      }
      if (fInput.isText()) {
        return doFindText();
      } else {
        return doFindImage();
      }
    }

    private FindResult2 doFindImage() {
      FindResult2 findResult = null;
      FindInput2 findInput = fInput;
      log.trace("doFindImage: start %s", findInput);
      mBase = findInput.getBase();
      boolean success = false;
      long begin_lap = 0;
      long begin_find = new Date().getTime();
      Core.MinMaxLocResult mMinMax = null;

      double rfactor = 0;
      boolean downSizeFound = false;
      double downSizeScore = -1;
      double downSizeWantedScore = 0;
      Mat findWhere = getNewMat();
      Mat findWhat = getNewMat();

      boolean trueOrFalse = findInput.shouldSearchDownsized(resizeMinFactor);
      //TODO search downsized?
      trueOrFalse = false;
      if (trueOrFalse) {
        // ************************************************* search in downsized
        begin_lap = new Date().getTime();
        double imgFactor = findInput.getResizeFactor();
        Size sizeBase, sizePattern;
        mResult = null;
        for (double factor : resizeLevels) {
          rfactor = factor * imgFactor;
          sizeBase = new Size(this.mBase.cols() / rfactor, this.mBase.rows() / rfactor);
          sizePattern = new Size(findInput.getTarget().cols() / rfactor, findInput.getTarget().rows() / rfactor);
          Imgproc.resize(this.mBase, findWhere, sizeBase, 0, 0, Imgproc.INTER_AREA);
          Imgproc.resize(findInput.getTarget(), findWhat, sizePattern, 0, 0, Imgproc.INTER_AREA);
          mResult = doFindMatch(findWhat, findWhere, findInput);
          mMinMax = Core.minMaxLoc(mResult);
          downSizeWantedScore = ((int) ((findInput.getScore() - downSimDiff) * 100)) / 100.0;
          downSizeScore = mMinMax.maxVal;
          if (downSizeScore > downSizeWantedScore) {
            downSizeFound = true;
            break;
          }
        }
        log.trace("downSizeFound: %s", downSizeFound);
        log.trace("doFindImage: down: %%%.2f %d msec", 100 * mMinMax.maxVal, new Date().getTime() - begin_lap);
      }
      findWhere = this.mBase;
      trueOrFalse = !findInput.isFindAll() && downSizeFound;
      if (trueOrFalse) {
        // ************************************* check after downsized success
        if (findWhere.size().equals(findInput.getTarget().size())) {
          // trust downsized mResult, if images have same size
          return new FindResult2(mResult, findInput);
        } else {
          int maxLocX = (int) (mMinMax.maxLoc.x * rfactor);
          int maxLocY = (int) (mMinMax.maxLoc.y * rfactor);
          begin_lap = new Date().getTime();
          int margin = ((int) findInput.getResizeFactor()) + 1;
          Rectangle rSub = new Rectangle(Math.max(0, maxLocX - margin), Math.max(0, maxLocY - margin),
              Math.min(findInput.getTarget().width() + 2 * margin, findWhere.width()),
              Math.min(findInput.getTarget().height() + 2 * margin, findWhere.height()));
          Rectangle rWhere = new Rectangle(0, 0, findWhere.cols(), findWhere.rows());
          Rectangle rSubNew = rWhere.intersection(rSub);
          Rect rectSub = new Rect(rSubNew.x, rSubNew.y, rSubNew.width, rSubNew.height);
          mResult = doFindMatch(findInput.getTarget(), findWhere.submat(rectSub), findInput);
          mMinMax = Core.minMaxLoc(mResult);
          double maxVal = mMinMax.maxVal;
          double wantedScore = findInput.getScore();
          if (maxVal > wantedScore) {
            findResult = new FindResult2(mResult, findInput, new int[]{rectSub.x, rectSub.y});
          }
          if (SX.isNotNull(findResult)) {
            log.trace("doFindImage after down: %%%.2f(?%%%.2f) %d msec",
                maxVal * 100, wantedScore * 100, new Date().getTime() - begin_lap);
          }
        }
      }
      // ************************************** search in original
      if (downSizeScore < 0) {
        begin_lap = new Date().getTime();
        mResult = doFindMatch(findInput.getTarget(), findWhere, findInput);
        mMinMax = Core.minMaxLoc(mResult);
        if (!isCheckLastSeen) {
          log.trace("doFindImage: in original: %%%.4f (?%.0f) %d msec %s",
              mMinMax.maxVal * 100, findInput.getScore() * 100, new Date().getTime() - begin_lap,
              findInput.hasMask() ? " **withMask" : "");
        }
        if (mMinMax.maxVal > findInput.getScore()) {
          findResult = new FindResult2(mResult, findInput);
        }
      }
      log.trace("doFindImage: end %d msec", new Date().getTime() - begin_find);
      return findResult;
    }

    private Mat doFindMatch(Mat what, Mat where, FindInput2 findInput) {
      Mat mResult = getNewMat();
      if (what.empty()) {
        log.error("doFindMatch: image conversion to cvMat did not work");
      } else {
        Mat mWhere = where;
        if (findInput.isGray()) {
          Imgproc.cvtColor(where, mWhere, Imgproc.COLOR_BGR2GRAY);
        }
        if (!findInput.isPlainColor()) {
          if (findInput.hasMask()) {
            Mat mask = findInput.getMask();
            Imgproc.matchTemplate(mWhere, what, mResult, Imgproc.TM_CCORR_NORMED, mask);
          } else {
            Imgproc.matchTemplate(mWhere, what, mResult, Imgproc.TM_CCOEFF_NORMED);
          }
        } else {
          Mat wherePlain = mWhere;
          Mat whatPlain = what;
          if (findInput.isBlack()) {
            Core.bitwise_not(mWhere, wherePlain);
            Core.bitwise_not(what, whatPlain);
          }
          if (findInput.hasMask()) {
            Imgproc.matchTemplate(wherePlain, what, mResult, Imgproc.TM_SQDIFF_NORMED, findInput.getMask());
          } else {
            Imgproc.matchTemplate(wherePlain, whatPlain, mResult, Imgproc.TM_SQDIFF_NORMED);
          }
          Core.subtract(Mat.ones(mResult.size(), CvType.CV_32F), mResult, mResult);
        }
      }
      return mResult;
    }

    private FindResult2 doFindText() {
      FindResult2 findResult = null;
      Region where = fInput.getWhere();
      String text = fInput.getTargetText();
      boolean globalSearch = false;
      boolean singleWord = true;
      List<Match> wordsFound;
      String[] textSplit = new String[0];
      java.util.regex.Pattern pattern = null;

      int textLevel = fInput.getTextLevel();
      long timer = new Date().getTime();
      BufferedImage bimg = fInput.getImage();
      if (fInput.isTextRegEx()) {
        pattern = fInput.getRegEx();
      } else {
        text = text.trim();
      }
      if (textLevel == OCR.PAGE_ITERATOR_LEVEL_LINE) {
        wordsFound = OCR.readLines(bimg);
      } else if (textLevel == OCR.PAGE_ITERATOR_LEVEL_WORD) {
        wordsFound = OCR.readWords(bimg);
      } else {
        globalSearch = true;
        textSplit = text.split("\\s");
        if (textSplit.length > 1) {
          singleWord = false;
          if (textSplit.length == 3 && textSplit[1].contains("+")) {
            pattern = java.util.regex.Pattern.compile(textSplit[0] + ".*?" + textSplit[2]);
          }
        }
        wordsFound = OCR.readLines(bimg);
      }
      timer = new Date().getTime() - timer;
      List<Match> wordsMatch = new ArrayList<>();
      if (!text.isEmpty()) {
        for (Match match : wordsFound) {
          if (isWord()) {
            if (!isTextMatching(match.getText(), text, pattern)) {
              continue;
            }
          } else if (isLine()) {
            if (!isTextContained(match.getText(), text, pattern)) {
              continue;
            }
          } else if (globalSearch) {
            if (!isTextContained(match.getText().toLowerCase(), text.toLowerCase(), pattern)) {
              continue;
            }
          } else {
            continue;
          }
          Rectangle wordOrLine = match.getRect();
          List<Match> wordsInLine;
          if (globalSearch) {
            BufferedImage bLine = Image.createSubimage(bimg, wordOrLine);
            wordsInLine = OCR.readWords(bLine);
            if (singleWord) {
              for (Match wordInLine : wordsInLine) {
                if (!isTextContained(wordInLine.getText().toLowerCase(), text.toLowerCase(), null)) {
                  continue;
                }
                Rectangle rword = new Rectangle(wordInLine.getRect());
                rword.x += wordOrLine.x;
                rword.y += wordOrLine.y;
                wordsMatch.add(new Match(rword, wordInLine.getScore(), wordInLine.getText(), where));
              }
            } else {
              int startText = -1;
              int endText = -1;
              int ix = 0;
              String firstWord = textSplit[0].toLowerCase();
              String lastWord = textSplit[textSplit.length - 1].toLowerCase();
              for (Match wordInLine : wordsInLine) {
                if (startText < 0) {
                  if (isTextContained(wordInLine.getText().toLowerCase(), firstWord, null)) {
                    startText = ix;
                  }
                } else if (endText < 0) {
                  if (isTextContained(wordInLine.getText().toLowerCase(), lastWord, null)) {
                    endText = ix;
                  }
                } else {
                  break;
                }
                ix++;
              }
              if (startText > -1 && endText > -1) {
                Rectangle rword = (new Rectangle(wordsInLine.get(startText).getRect())).
                    union(new Rectangle(wordsInLine.get(endText).getRect()));
                rword.x += wordOrLine.x;
                rword.y += wordOrLine.y;
                double score = (wordsInLine.get(startText).getScore() + wordsInLine.get(startText).getScore()) / 2;
                String foundText = wordsInLine.get(startText).getText() + " ... " + wordsInLine.get(endText);
                wordsMatch.add(new Match(rword, score, foundText, where));
              }
            }
          } else {
            wordsMatch.add(new Match(match.getRect(), match.getScore(), match.getText(), where));
          }
        }
        if (wordsMatch.size() > 0) {
          log.trace("doFindText: %s found: %d times (%d msec) ", text, wordsMatch.size(), timer);
          findResult = new FindResult2(wordsMatch, fInput);
        } else {
          log.trace("doFindText: %s (%d msec): not found", text, timer);
        }
      } else {
        if (isWord()) {
          log.trace("doFindText: listWords: %d words (%d msec) ", wordsFound.size(), timer);
        } else {
          log.trace("doFindText: listLines: %d lines (%d msec) ", wordsFound.size(), timer);
        }
        for (Match match : wordsFound) {
          Rectangle wordOrLine = match.getRect();
          wordsMatch.add(new Match(match.getRect(), match.getScore(), match.getText(), where));
        }
        findResult = new FindResult2(wordsMatch, fInput);
      }
      return findResult;
    }
    //</editor-fold>

    //<editor-fold desc="detect changes">
    private static int toGray = Imgproc.COLOR_BGR2GRAY;
    private static int toColor = Imgproc.COLOR_GRAY2BGR;
    private static int gray = CvType.CV_8UC1;
    private static int colored = CvType.CV_8UC3;
    private static int transparent = CvType.CV_8UC4;

    private static boolean isGray(Mat mat) {
      return mat.type() == gray;
    }

    private static boolean isColored(Mat mat) {
      return mat.type() == colored || mat.type() == transparent;
    }

    public static List<Region> findChanges(FindInput2 findInput) {
      findInput.setAttributes();
      Mat previousGray = getNewMat();
      Mat nextGray = getNewMat();
      Mat mDiffAbs = getNewMat();
      Mat mDiffTresh = getNewMat();

      Imgproc.cvtColor(findInput.getBase(), previousGray, toGray);
      Imgproc.cvtColor(findInput.getTarget(), nextGray, toGray);
      Core.absdiff(previousGray, nextGray, mDiffAbs);
      Imgproc.threshold(mDiffAbs, mDiffTresh, PIXEL_DIFF_THRESHOLD, 0.0, Imgproc.THRESH_TOZERO);

      List<Region> rectangles = new ArrayList<>();
      if (Core.countNonZero(mDiffTresh) > IMAGE_DIFF_THRESHOLD) {
        Imgproc.threshold(mDiffAbs, mDiffAbs, PIXEL_DIFF_THRESHOLD, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(mDiffAbs, mDiffAbs, getNewMat());
        Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mDiffAbs, mDiffAbs, Imgproc.MORPH_CLOSE, se);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mHierarchy = getNewMat();
        Imgproc.findContours(mDiffAbs, contours, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        rectangles = contoursToRectangle(contours);

        //Core.subtract(mDiffAbs, mDiffAbs, mChanges);
        //Imgproc.drawContours(mChanges, contours, -1, new Scalar(255));
        //logShow(mDiffAbs);
      }
      return rectangles;
    }

    public static List<Region> contoursToRectangle(List<MatOfPoint> contours) {
      List<Region> rects = new ArrayList<>();
      for (MatOfPoint contour : contours) {
        //log.trace("*** new contour");
        int x1 = 99999;
        int y1 = 99999;
        int x2 = 0;
        int y2 = 0;
        List<org.opencv.core.Point> points = contour.toList();
        for (Point point : points) {
          int x = (int) point.x;
          int y = (int) point.y;
          //log.trace("x: %d y: %d", x, y);
          if (x < x1) x1 = x;
          if (x > x2) x2 = x;
          if (y < y1) y1 = y;
          if (y > y2) y2 = y;
        }
        Region rect = new Region(x1, y1, x2 - x1, y2 - y1);
        rects.add(rect);
      }
      return rects;
    }
    //</editor-fold>

    //<editor-fold desc="OpenCV Mat">
    public static boolean isOpaque(BufferedImage bImg) {
      if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
        List<Mat> mats = getMatList(bImg);
        Mat transMat = mats.get(0);
        int allPixel = (int) transMat.size().area();
        int nonZeroPixel = Core.countNonZero(transMat);
        if (nonZeroPixel != allPixel) {
          return false;
        }
      }
      return true;
    }

    private static List<Mat> getMatList(BufferedImage bImg) {
      byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
      Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
      aMat.put(0, 0, data);
      List<Mat> mats = new ArrayList<Mat>();
      Core.split(aMat, mats);
      return mats;
    }

    public static Mat makeMat(BufferedImage bImg) {
      return makeMat(bImg, true);
    }

    public static Mat makeMat(BufferedImage bImg, boolean asBGR) {
      if (bImg.getType() == BufferedImage.TYPE_INT_RGB) {
        log.trace("makeMat: INT_RGB (%dx%d)", bImg.getWidth(), bImg.getHeight());
        int[] data = ((DataBufferInt) bImg.getRaster().getDataBuffer()).getData();
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);
        Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC4);
        aMat.put(0, 0, byteBuffer.array());
        Mat oMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
        Mat oMatA = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
        List<Mat> mixIn = new ArrayList<Mat>(Arrays.asList(new Mat[]{aMat}));
        List<Mat> mixOut = new ArrayList<Mat>(Arrays.asList(new Mat[]{oMatA, oMatBGR}));
        //A 0 - R 1 - G 2 - B 3 -> A 0 - B 1 - G 2 - R 3
        Core.mixChannels(mixIn, mixOut, new MatOfInt(0, 0, 1, 3, 2, 2, 3, 1));
        return oMatBGR;
      } else if (bImg.getType() == BufferedImage.TYPE_3BYTE_BGR) {
        log.trace("makeMat: 3BYTE_BGR (%dx%d)", bImg.getWidth(), bImg.getHeight());
        byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
        Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
        aMatBGR.put(0, 0, data);
        return aMatBGR;
      } else if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED
          || bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
        String bImgType = "BYTE_BINARY";
        if (bImg.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
          bImgType = "BYTE_INDEXED";
        }
        log.trace("makeMat: %s (%dx%d)", bImgType, bImg.getWidth(), bImg.getHeight());
        BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics graphics = bimg3b.getGraphics();
        graphics.drawImage(bImg, 0, 0, null);
        byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
        Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
        aMatBGR.put(0, 0, data);
        return aMatBGR;
      } else if (bImg.getType() == BufferedImage.TYPE_4BYTE_ABGR
              || bImg.getType() == BufferedImage.TYPE_CUSTOM) {
        log.trace("makeMat: TYPE_4BYTE_ABGR (%dx%d)", bImg.getWidth(), bImg.getHeight());
        List<Mat> mats = getMatList(bImg);
        Size size = mats.get(0).size();
        if (asBGR) {
          Mat mBGR = getNewMat(size, 3, -1);
          mats.remove(0);
          Core.merge(mats, mBGR);
          return mBGR;
        } else {
          Mat mBGRA = getNewMat(size, 4, -1);
          mats.add(mats.remove(0));
          Core.merge(mats, mBGRA);
          return mBGRA;
        }
      } else if (bImg.getType() == BufferedImage.TYPE_BYTE_GRAY) {
        log.trace("makeMat: BYTE_GRAY (%dx%d)", bImg.getWidth(), bImg.getHeight());
        byte[] data = ((DataBufferByte) bImg.getRaster().getDataBuffer()).getData();
        Mat aMat = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC1);
        aMat.put(0, 0, data);
        return aMat;
      } else if (bImg.getType() == BufferedImage.TYPE_BYTE_BINARY) {
        log.trace("makeMat: BYTE_BINARY (%dx%d)", bImg.getWidth(), bImg.getHeight());
        BufferedImage bimg3b = new BufferedImage(bImg.getWidth(), bImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics graphics = bimg3b.getGraphics();
        graphics.drawImage(bImg, 0, 0, null);
        byte[] data = ((DataBufferByte) bimg3b.getRaster().getDataBuffer()).getData();
        Mat aMatBGR = new Mat(bImg.getHeight(), bImg.getWidth(), CvType.CV_8UC3);
        aMatBGR.put(0, 0, data);
        return aMatBGR;
      } else {
        log.error("makeMat: BufferedImage: type not supported: %d --- please report this problem", bImg.getType());
      }
      return getNewMat();
    }

    public static Mat makeMat() {
      return getNewMat();
    }

    public static Mat makeMat(Size size, int type, int fill) {
      return getNewMat(size, type, fill);
    }

    protected static Mat getNewMat() {
      return new Mat();
    }

    protected static Mat getNewMat(Size size, int type, int fill) {
      switch (type) {
        case 1:
          type = CvType.CV_8UC1;
          break;
        case 3:
          type = CvType.CV_8UC3;
          break;
        case 4:
          type = CvType.CV_8UC4;
          break;
        default:
          type = -1;
      }
      if (type < 0) {
        return new Mat();
      }
      Mat result;
      if (fill < 0) {
        result = new Mat(size, type);
      } else {
        result = new Mat(size, type, new Scalar(fill));
      }
      return result;
    }

    protected static Mat matMulti(Mat mat, int channels) {
      if (mat.type() != CvType.CV_8UC1 || mat.channels() == channels) {
        return mat;
      }
      List<Mat> listMat = new ArrayList<>();
      for (int n = 0; n < channels; n++) {
        listMat.add(mat);
      }
      Mat mResult = getNewMat();
      Core.merge(listMat, mResult);
      return mResult;
    }

    protected static List<Mat> extractMask(Mat target, boolean onlyChannel4) {
      List<Mat> extracted = new ArrayList<>();
      Mat mask = new Mat();
      Mat targetBGR = new Mat();
      int nChannels = target.channels();
      if (nChannels == 4) {
        List<Mat> mats = new ArrayList<Mat>();
        Core.split(target, mats);
        mask = mats.remove(3);
        Core.merge(mats, targetBGR);
        int allPixel = (int) mask.size().area();
        int nonZeroPixel = Core.countNonZero(mask);
        if (nonZeroPixel != allPixel) {
          Mat maskMask = new Mat();
          Imgproc.threshold(mask, maskMask, 0.0, 1.0, Imgproc.THRESH_BINARY);
          mask = Finder2.matMulti(maskMask, 3);
        } else {
          mask = new Mat();
        }
      } else {
        targetBGR = target;
      }
      if (!onlyChannel4 && (mask.empty() || nChannels == 3)) {
        Mat mGray = new Mat();
        Imgproc.cvtColor(targetBGR, mGray, toGray);
        int allPixel = (int) mGray.size().area();
        int nonZeroPixel = Core.countNonZero(mGray);
        if (nonZeroPixel != allPixel) {
          Mat maskMask = new Mat();
          Imgproc.threshold(mGray, maskMask, 0.0, 1.0, Imgproc.THRESH_BINARY);
          mask = Finder2.matMulti(maskMask, 3);
        }
      }
      extracted.add(targetBGR);
      extracted.add(mask);
      return extracted;
    }

    protected final static String PNG = "png";
    protected final static String dotPNG = "." + PNG;

    public static BufferedImage getBufferedImage(Mat mat) {
      return getBufferedImage(mat, dotPNG);
    }

    public static BufferedImage getBufferedImage(Mat mat, String type) {
      BufferedImage bImg = null;
      MatOfByte bytemat = new MatOfByte();
      if (SX.isNull(mat)) {
        mat = getNewMat();
      }
      Imgcodecs.imencode(type, mat, bytemat);
      byte[] bytes = bytemat.toArray();
      InputStream in = new ByteArrayInputStream(bytes);
      try {
        bImg = ImageIO.read(in);
      } catch (IOException ex) {
        log.error("getBufferedImage: %s error(%s)", mat, ex.getMessage());
      }
      return bImg;
    }
    //</editor-fold>
  }

  private static class FindInput2 {

    static {
      Finder2.init();
    }

    private Mat mask = new Mat();

    protected boolean hasMask() {
      return !mask.empty();
    }

    protected Mat getMask() {
      return mask;
    }

    protected void setMask(Mat mask) {
      this.mask = mask;
    }

    private Mat targetBGR = new Mat();

    public void setWhere(Region where) {
      this.where = where;
    }

    public Region getWhere() {
      return where;
    }

    private Region where = null;

    public BufferedImage getImage() {
      if (where != null) {
        return where.getScreen().capture(where).getImage();
      } else if (source != null) {
        return Finder.Finder2.getBufferedImage(source);
      } else if (image != null) {
        return image.get();
      }
      return null;
    }

    private Image image = null;

    private double similarity = 0.7;

    private boolean findAll = false;

    private Mat target = null;
    private String targetText = "";
    private boolean targetTypeText = false;

    public void setTarget(Mat target) {
      this.target = target;
    }

    public Mat getTarget() {
      if (targetBGR.empty()) {
        return target;
      }
      return targetBGR;
    }

    public void setTargetText(String text) {
      targetText = text;
      targetTypeText = true;
    }

    public String getTargetText() {
      return targetText;
    }

    public boolean isValid() {
      if (SX.isNull(source) && SX.isNull(where)) {
        return false;
      }
      if (SX.isNotNull(target) && !targetTypeText) {
        if (target.empty()) {
          throw new SikuliXception("Finder::isValid: image to search is empty");
        }
        if (source.width() < target.width() || source.height() < target.height()) {
          throw new SikuliXception(
              String.format("image to search (%d, %d) is larger than image to search in (%d, %d)",
                  target.width(), target.height(), source.width(), source.height()));
        }
        return true;
      }
      if (targetTypeText && !targetText.isEmpty()) {
        return true;
      }
      return false;
    }

    public int getTextLevel() {
      return textLevel;
    }

    public void setTextLevel(int textLevel) {
      this.textLevel = textLevel;
    }

    private int textLevel = -1;

    public boolean isText() {
      return targetTypeText;
    }

    private boolean textRegex = false;

    public void textAsRegEx() {
      textRegex = true;
    }

    public boolean isTextRegEx() {
      return textRegex;
    }

    public java.util.regex.Pattern getRegEx() {
      return java.util.regex.Pattern.compile(targetText);
    }

    public boolean isFindAll() {
      return findAll;
    }

    private Mat source = null;

    public void setSource(Mat source) {
      this.source = source;
    }

    public Mat getBase() {
      return source;
    }

    boolean isPattern = false;

    public void setIsPattern() {
      isPattern = true;
    }

    public boolean isPattern() {
      return isPattern;
    }

    public void setPattern(boolean pattern) {
      isPattern = pattern;
    }

    public void setSimilarity(double similarity) {
      this.similarity = similarity;
    }

    public boolean isExact() {
      return similarity >= 0.99;
    }

    public boolean shouldSearchDownsized(float resizeMinFactor) {
      return !hasMask() && !isExact() && !isFindAll() && getResizeFactor() > resizeMinFactor;
    }

    private double scoreMaxDiff = 0.05;

    protected double getScoreMaxDiff() {
      return scoreMaxDiff;
    }

    protected double getScore() {
      return similarity;
    }

    public void setFindAll() {
      findAll = true;
    }

    protected boolean plainColor = false;
    protected boolean blackColor = false;
    protected boolean whiteColor = false;
    protected boolean grayColor = false;

    public boolean isPlainColor() {
      return isValid() && plainColor;
    }

    public boolean isBlack() {
      return isValid() && blackColor;
    }

    public boolean isGray() {
      return isValid() && grayColor;
    }

    public boolean isWhite() {
      return isValid() && blackColor;
    }

    public double getResizeFactor() {
      return isValid() ? resizeFactor : 1;
    }

    protected double resizeFactor;

    private final int resizeMinDownSample = 12;
    private int[] meanColor = null;
    private double minThreshhold = 1.0E-5;

    public Color getMeanColor() {
      return new Color(meanColor[2], meanColor[1], meanColor[0]);
    }

    public boolean isMeanColorEqual(Color otherMeanColor) {
      Color col = getMeanColor();
      int r = (col.getRed() - otherMeanColor.getRed()) * (col.getRed() - otherMeanColor.getRed());
      int g = (col.getGreen() - otherMeanColor.getGreen()) * (col.getGreen() - otherMeanColor.getGreen());
      int b = (col.getBlue() - otherMeanColor.getBlue()) * (col.getBlue() - otherMeanColor.getBlue());
      return Math.sqrt(r + g + b) < minThreshhold;
    }

    double targetStdDev = -1;
    double targetMean = -1;

    public void setAttributes() {
      if (targetTypeText) {
        return;
      }
      List<Mat> mats = Finder2.extractMask(target, true);
      targetBGR = mats.get(0);
      if (mask.empty()) {
        mask = mats.get(1);
      }

      if (targetBGR.channels() == 1) {
        grayColor = true;
      }
      plainColor = false;
      blackColor = false;
      resizeFactor = Math.min(((double) targetBGR.width()) / resizeMinDownSample,
          ((double) targetBGR.height()) / resizeMinDownSample);
      resizeFactor = Math.max(1.0, resizeFactor);
      MatOfDouble pMean = new MatOfDouble();
      MatOfDouble pStdDev = new MatOfDouble();

      if (mask.empty()) {
        Core.meanStdDev(targetBGR, pMean, pStdDev);
      } else {
        List<Mat> maskMats = new ArrayList<>();
        Core.split(mask, maskMats);
        Core.meanStdDev(targetBGR, pMean, pStdDev, maskMats.get(0));
      }
      double sum = 0.0;
      double[] arr = pStdDev.toArray();
      for (int i = 0; i < arr.length; i++) {
        sum += arr[i];
      }
      targetStdDev = sum;
      if (sum < minThreshhold) {
        plainColor = true;
      }
      sum = 0.0;
      arr = pMean.toArray();
      meanColor = new int[arr.length];
      for (int i = 0; i < arr.length; i++) {
        meanColor[i] = (int) arr[i];
        sum += arr[i];
      }
      targetMean = sum;
      if (sum < minThreshhold && plainColor) {
        blackColor = true;
      }
      if (meanColor.length > 1) {
        whiteColor = isMeanColorEqual(Color.WHITE);
      }
    }

    public String toString() {
      return String.format("(stdDev: %.4f mean: %4f)", targetStdDev, targetMean);
    }
  }

  private static class FindResult2 implements Iterator<Match> {

    private FindInput2 findInput = null;
    private int offX = 0;
    private int offY = 0;
    private Mat result = null;
    private List<Match> matches = new ArrayList<>();

    private FindResult2() {
    }

    public FindResult2(List<Match> matches, FindInput2 findInput) {
      this.matches = matches;
      this.findInput = findInput;
    }

    public FindResult2(Mat result, FindInput2 findInput) {
      this.result = result;
      this.findInput = findInput;
    }

    public FindResult2(Mat result, FindInput2 target, int[] off) {
      this(result, target);
      offX = off[0];
      offY = off[1];
    }

    private Core.MinMaxLocResult resultMinMax = null;

    private double currentScore = -1;
    double targetScore = -1;
    double lastScore = -1;
    double scoreMeanDiff = -1;
    double scoreMaxDiff = 0.005;
    int matchCount = 0;

    private int currentX = -1;
    private int currentY = -1;
    private int baseW = -1;
    private int baseH = -1;
    private int targetW = -1;
    private int targetH = -1;
    private int marginX = -1;
    private int marginY = -1;

    public boolean hasNext() {
      if (findInput.isText()) {
        if (matches.size() > 0) {
          return true;
        }
        return false;
      }
      resultMinMax = Core.minMaxLoc(result);
      currentScore = resultMinMax.maxVal;
      currentX = (int) resultMinMax.maxLoc.x;
      currentY = (int) resultMinMax.maxLoc.y;
      if (lastScore < 0) {
        lastScore = currentScore;
        targetScore = findInput.getScore();
        baseW = result.width();
        baseH = result.height();
        targetW = findInput.getTarget().width();
        targetH = findInput.getTarget().height();
        marginX = (int) (targetW * 0.8);
        marginY = (int) (targetH * 0.8);
        matchCount = 0;
      }
      boolean isMatch = false;
      if (currentScore > targetScore) {
        if (matchCount == 0) {
          isMatch = true;
        } else if (matchCount == 1) {
          scoreMeanDiff = lastScore - currentScore;
          isMatch = true;
        } else {
          double scoreDiff = lastScore - currentScore;
          if (findInput.isPattern || scoreDiff <= (scoreMeanDiff + 0.01)) { // 0.005
            scoreMeanDiff = ((scoreMeanDiff * matchCount) + scoreDiff) / (matchCount + 1);
            isMatch = true;
          }
        }
        if (!isMatch) {
          Debug.log(3, "findAll: (%d) stop: %.4f (%.4f) %s", matchCount, currentScore, scoreMeanDiff, findInput);
        }
      }
      return isMatch;
    }

    public Match next() {
      Match match = null;
      if (hasNext()) {
        if (findInput.isText()) {
          return matches.remove(0);
        } else {
          match = new Match(currentX + offX, currentY + offY, targetW, targetH, currentScore, null);
          matchCount++;
          lastScore = currentScore;
          //int margin = getPurgeMargin();
          Range rangeX = new Range(Math.max(currentX - marginX, 0), Math.min(currentX + marginX, result.width()));
          Range rangeY = new Range(Math.max(currentY - marginY, 0), Math.min(currentY + marginY, result.height()));
          result.colRange(rangeX).rowRange(rangeY).setTo(new Scalar(0f));
        }
      }
      return match;
    }

    private int getPurgeMargin() {
      if (currentScore < 0.95) {
        return 4;
      } else if (currentScore < 0.85) {
        return 8;
      } else if (currentScore < 0.71) {
        return 16;
      }
      return 2;
    }

    double bestScore = 0;
    double meanScore = 0;
    double stdDevScore = 0;

    public List<Match> getMatches() {
      if (hasNext()) {
        List<Match> matches = new ArrayList<Match>();
        List<Double> scores = new ArrayList<>();
        while (true) {
          Match match = next();
          if (SX.isNull(match)) {
            break;
          }
          meanScore = (meanScore * matches.size() + match.getScore()) / (matches.size() + 1);
          bestScore = Math.max(bestScore, match.getScore());
          matches.add(match);
          scores.add(match.getScore());
        }
        stdDevScore = calcStdDev(scores, meanScore);
        return matches;
      }
      return null;
    }

    public double[] getScores() {
      return new double[]{bestScore, meanScore, stdDevScore};
    }

    private double calcStdDev(List<Double> doubles, double mean) {
      double stdDev = 0;
      for (double doubleVal : doubles) {
        stdDev += (doubleVal - mean) * (doubleVal - mean);
      }
      return Math.sqrt(stdDev / doubles.size());
    }

    @Override
    public void remove() {
    }
  }
}
