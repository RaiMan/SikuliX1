/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.SXOpenCV;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.regex.Matcher;

public class Finder implements Matches {

  //TODO needed? currentMatchIndex
  private int currentMatchIndex;

  private boolean screenFinder = true;

  private static String me = "Finder: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  private Region _region = null;
  private Pattern _pattern = null;
  private Image _image = null;
  private FindInput2 _findInput = new FindInput2();
  private FindResult2 _results = null;
  private Region where = null;

  protected Finder() {
    resetFindChanges();
  }

  /**
   * Create a Finder for the given element
   *
   * @param inWhat  in what element (RIBS) to search
   * @param <RIBSM> Region, Image, BufferedImage, ScreenImage, image filename, cvMat
   */
  public <RIBSM> Finder(RIBSM inWhat) {
    if (inWhat instanceof Region) {
      where = (Region) inWhat;
    } else if (inWhat instanceof Image) {
      _findInput.setSource(SXOpenCV.makeMat(((Image) inWhat).getBufferedImage()));
    } else if (inWhat instanceof String) {
      _findInput.setSource(SXOpenCV.makeMat(new Image(inWhat).getBufferedImage()));
    } else if (inWhat instanceof BufferedImage) {
      _findInput.setSource(SXOpenCV.makeMat(((BufferedImage) inWhat)));
    } else if (inWhat instanceof ScreenImage) {
      initScreenFinder(((ScreenImage) inWhat), null);
    } else if (inWhat instanceof Mat) {
      _findInput.setSource((Mat) inWhat);
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
    _findInput.setSource(SXOpenCV.makeMat(simg.getBufferedImage()));
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="internal repeating">
  //TODO needed? repeating
  private boolean repeating = false;

  protected void setRepeating() {
    repeating = true;
  }

  /**
   * internal use: repeat with same Finder
   */
  protected void findRepeat() {
    _results = Finder2.find(_findInput);
    //currentMatchIndex = 0;
  }

  /**
   * internal use: repeat with same Finder
   */
  protected void findAllRepeat() {
    Debug timing = Debug.startTimer("Finder.findAll");
    _results = Finder2.find(_findInput);
    //currentMatchIndex = 0;
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
    double factor = oneTimeResize;
    if (factor == 0 && Settings.AlwaysResize > 0 && Settings.AlwaysResize != 1) {
      factor = Settings.AlwaysResize;
    }
    Mat mat = SXOpenCV.makeMat(img.getBufferedImage(), false);
    if (factor > 0 && factor != 1) {
      Debug.log(3, "Finder::possibleImageResizeOrCallback: resize");
      if (!mat.empty()) {
        SXOpenCV.resize(mat, factor);
      }
    } else if (Settings.ImageCallback != null) {
      Debug.log(3, "Finder::possibleImageResizeOrCallback: callback");
      BufferedImage newBimg = Settings.ImageCallback.callback(img);
      mat = SXOpenCV.makeMat(newBimg, false);
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
    if (aPtn.isValid()) {
      _pattern = aPtn;
      if (_pattern.hasMask()) {
        _findInput.setMask(_pattern.getMask().getContent());
      }
      _image = aPtn.getImage();
      _findInput.setTarget(possibleImageResizeOrCallback(_image, aPtn.getResize()));
      _findInput.setSimilarity(aPtn.getSimilar());
      _findInput.setIsPattern();
      _results = Finder2.find(_findInput);
      //currentMatchIndex = 0;
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
    if (img.isValid()) {
      _image = img;
      _findInput.setTarget(possibleImageResizeOrCallback(img));
      _findInput.setSimilarity(Settings.MinSimilarity);
      _results = Finder2.find(_findInput);
      //currentMatchIndex = 0;
      return img.getFilename();
    } else if (img.isValid()) {
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
    if (aPtn.isValid()) {
      _pattern = aPtn;
      _image = aPtn.getImage();
      _findInput.setTarget(possibleImageResizeOrCallback(_image, aPtn.getResize()));
      _findInput.setSimilarity(aPtn.getSimilar());
      _findInput.setIsPattern();
      _findInput.setFindAll();
      if (_pattern.hasMask()) {
        _findInput.setMask(_pattern.getMask().getContent());
      }
      Debug timing = Debug.startTimer("Finder.findAll");
      _results = Finder2.find(_findInput);
      //currentMatchIndex = 0;
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
    if (img.isValid()) {
      _image = img;
      _findInput.setTarget(possibleImageResizeOrCallback(img));
      _findInput.setSimilarity(Settings.MinSimilarity);
      _findInput.setFindAll();
      Debug timing = Debug.startTimer("Finder.findAll");
      _results = Finder2.find(_findInput);
      //currentMatchIndex = 0;
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
    _findInput.setTargetText(text);
    _findInput.setWhere(where);
    _results = Finder2.find(_findInput);
    //currentMatchIndex = 0;
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
    _findInput.setFindAll();
    return findText(text);
  }

  private static String markRegex = Key.ESC;

  public static String asRegEx(String text) {
    return markRegex + text;
  }

  public static boolean isRegEx(String text) {
    return text.startsWith(markRegex);
  }

  public static java.util.regex.Pattern getRegEx(String text) {
    return java.util.regex.Pattern.compile(text.substring(1));
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
      if (screenFinder && _region != null) {
        match.setScreen(_region.getScreen());
      }
      if (_pattern != null) {
        match.setTargetOffset(_pattern.getTargetOffset());
      }
      match.onScreen(screenFinder);
      match.setImage(_image);
    }
    return match;
  }


  @Override
  public void remove() {
    destroy();
  }

  public void destroy() {
    _findInput = null;
    _results = null;
    _pattern = null;
  }

  @Override
  public Match asMatch() {
    return null;
  }

  @Override
  public List<Match> asList() {
    return null;
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

    private Mat mBase = SXOpenCV.newMat();
    private Mat mResult = SXOpenCV.newMat();

    private enum FindType {
      ONE, ALL
    }

    private Finder2() {
    }

    public boolean isValid() {
      return !mBase.empty();
    }
    //</editor-fold>

    //<editor-fold desc="find internal">
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
      Mat findWhere = SXOpenCV.newMat();
      Mat findWhat = SXOpenCV.newMat();

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
      Mat mResult = SXOpenCV.newMat();
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
                wordsMatch.add(new Match(rword, wordInLine.score(), wordInLine.getText(), where));
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
                double score = (wordsInLine.get(startText).score() + wordsInLine.get(startText).score()) / 2;
                String foundText = wordsInLine.get(startText).getText() + " ... " + wordsInLine.get(endText);
                wordsMatch.add(new Match(rword, score, foundText, where));
              }
            }
          } else {
            wordsMatch.add(new Match(match.getRect(), match.score(), match.getText(), where));
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
          wordsMatch.add(new Match(match.getRect(), match.score(), match.getText(), where));
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
      Mat previousGray = SXOpenCV.newMat();
      Mat nextGray = SXOpenCV.newMat();
      Mat mDiffAbs = SXOpenCV.newMat();
      Mat mDiffThresh = SXOpenCV.newMat();
      Imgproc.cvtColor(findInput.getBase(), previousGray, toGray);
      Imgproc.cvtColor(findInput.getTarget(), nextGray, toGray);
      Core.absdiff(previousGray, nextGray, mDiffAbs);
      Imgproc.threshold(mDiffAbs, mDiffThresh, PIXEL_DIFF_THRESHOLD, 0.0, Imgproc.THRESH_TOZERO);

      List<Region> rectangles = new ArrayList<>();
      if (Core.countNonZero(mDiffThresh) > IMAGE_DIFF_THRESHOLD) {
        Imgproc.threshold(mDiffAbs, mDiffAbs, PIXEL_DIFF_THRESHOLD, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(mDiffAbs, mDiffAbs, SXOpenCV.newMat());
        Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mDiffAbs, mDiffAbs, Imgproc.MORPH_CLOSE, se);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mHierarchy = SXOpenCV.newMat();
        Imgproc.findContours(mDiffAbs, contours, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : contours) {
          int x1 = 99999;
          int y1 = 99999;
          int x2 = 0;
          int y2 = 0;
          List<org.opencv.core.Point> points = contour.toList();
          for (Point point : points) {
            int x = (int) point.x;
            int y = (int) point.y;
            if (x < x1) x1 = x;
            if (x > x2) x2 = x;
            if (y < y1) y1 = y;
            if (y > y2) y2 = y;
          }
          Region rect = new Region(x1, y1, x2 - x1, y2 - y1);
          rectangles.add(rect);
        }
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
  }

  private static class FindInput2 {

    static {
      Finder2.init();
    }

    private Mat mask = SXOpenCV.newMat();

    protected boolean hasMask() {
      return !mask.empty();
    }

    protected Mat getMask() {
      return mask;
    }

    protected void setMask(Mat mask) {
      this.mask = mask;
    }

    private Mat targetBGR = SXOpenCV.newMat();

    public void setWhere(Region where) {
      this.where = where;
    }

    public Region getWhere() {
      return where;
    }

    private Region where = null;

    public BufferedImage getImage() {
      if (where != null) {
        return where.getScreen().capture(where).getBufferedImage();
      } else if (source != null) {
        return Element.getBufferedImage(source);
      } else if (image != null) {
        return image.getBufferedImage();
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

    protected double getScore() {
      return similarity;
    }

    public void setFindAll() {
      findAll = true;
    }

    private double getResizeFactor() {
      return isValid() ? resizeFactor : 1;
    }

    private double resizeFactor;

    private final int resizeMinDownSample = 12;

    double targetStdDev = -1;
    double targetMean = -1;

    private int[] meanColor = null;
    private double minThreshhold = 1.0E-5;

    private boolean plainColor = false;
    private boolean blackColor = false;
    private boolean whiteColor = false;
    private boolean grayColor = false;
    private boolean isPlainColor() {
      return isValid() && plainColor;
    }
    private boolean isBlack() {
      return isValid() && blackColor;
    }
    private boolean isGray() {
      return isValid() && grayColor;
    }
    private boolean isWhite() {
      return isValid() && whiteColor;
    }

    private void setAttributes() {
      if (targetTypeText) {
        return;
      }
      List<Mat> mats = SXOpenCV.extractMask(target, true);
      targetBGR = mats.get(0);
      if (mask.empty()) {
        mask = mats.get(1);
      }

      //TODO plaincolor/black with masking
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
      Mat check = SXOpenCV.newMat();

      if (mask.empty()) {
        check = targetBGR;
      } else {
        Core.multiply(targetBGR, mask, check);
      }
      Core.meanStdDev(check, pMean, pStdDev);
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

    private Color getMeanColor() {
      return new Color(meanColor[2], meanColor[1], meanColor[0]);
    }

    private boolean isMeanColorEqual(Color otherMeanColor) {
      Color col = getMeanColor();
      int r = (col.getRed() - otherMeanColor.getRed()) * (col.getRed() - otherMeanColor.getRed());
      int g = (col.getGreen() - otherMeanColor.getGreen()) * (col.getGreen() - otherMeanColor.getGreen());
      int b = (col.getBlue() - otherMeanColor.getBlue()) * (col.getBlue() - otherMeanColor.getBlue());
      return Math.sqrt(r + g + b) < minThreshhold;
    }

    public String dump() {
      Object base = null;
      if (where != null) {
        base = where;
      } else if (source != null) {
        base = source;
      } else if (image != null) {
        base = image;
      }
      return String.format("where: %s", base);
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
          meanScore = (meanScore * matches.size() + match.score()) / (matches.size() + 1);
          bestScore = Math.max(bestScore, match.score());
          matches.add(match);
          scores.add(match.score());
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
