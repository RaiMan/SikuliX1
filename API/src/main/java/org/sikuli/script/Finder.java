/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.util.ScreenHighlighter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Finder implements Iterator<Match> {

  public final static int TARGET_TYPE_TEXT = 3;

  private Region _region = null;
  private Pattern _pattern = null;
  private Image _image = null;
  private FindInput2 _findInput = new FindInput2();
  private FindResult2 _results = null;
  private Region where = null;

  public void setFindAll() {
    isFindAll = true;
  }

  private boolean isFindAll = false;

  private int currentMatchIndex;
  private boolean repeating = false;
  private boolean valid = true;
  private boolean screenFinder = true;

  private static String me = "Finder: ";
  private static int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
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

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  protected Finder() {}

  public Finder(FindInput2 findInput) {
    _findInput = findInput;
  }

  public Finder(Region reg) {
    where = reg;
  }

  public Region getRegion() {
    return where;
  }

  /**
   * Finder constructor (finding within an image).
   * <br>internally used with a screen snapshot
   *
   * @param imageFilename a string (name, path, url)
   */
  public Finder(String imageFilename) {
    this(imageFilename, null);
  }

  /**
   * Finder constructor (finding within an image within the given region).
   * <br>internally used with a screen snapshot
   *
   * @param imageFilename a string (name, path, url)
   * @param region search Region within image - topleft = (0,0)
   */
  public Finder(String imageFilename, Region region) {
    Image img = Image.create(imageFilename);
    if (img.isValid()) {
      _findInput.setSource(Finder2.makeMat(img.get()));
      _region = region;
      screenFinder = false;
    } else {
      log(-1, "imagefile not found:\n%s", imageFilename);
      valid = false;
    }
  }

  /**
   * Constructor for special use from a BufferedImage
   *
   * @param bimg BufferedImage
   */
  public Finder(BufferedImage bimg) {
    _findInput.setSource(Finder2.makeMat(bimg));
  }

  /**
   * Finder constructor for special use from a ScreenImage
   *
   * @param simg ScreenImage
   */
  public Finder(ScreenImage simg) {
    initScreenFinder(simg, null);
  }

  /**
   * Finder constructor for special use from a region on a ScreenImage
   *
   * @param simg ScreenImage
   * @param region the cropping region
   */
  public Finder(ScreenImage simg, Region region) {
    initScreenFinder(simg, region);
  }

  /**
   * Finder constructor for special use from an Image
   *
   * @param img Image
   */
  public Finder(Image img) {
    log(lvl, "Image: %s", img);
    _findInput.setSource(Finder2.makeMat(img.get()));
  }

  private void initScreenFinder(ScreenImage simg, Region region) {
    _findInput = new FindInput2();
    setScreenImage(simg);
    _region = region;
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
    BufferedImage newBimg = img.get();
    float factor = oneTimeResize;
    if (factor == 0 && Settings.AlwaysResize > 0 && Settings.AlwaysResize != 1) {
      factor = Settings.AlwaysResize;
    }
    if (factor > 0 && factor != 1) {
      Debug.log(3, "Finder::possibleImageResizeOrCallback: resize");
      newBimg = Image.resize(newBimg, factor);
    } else if (Settings.ImageCallback != null) {
      Debug.log(3, "Finder::possibleImageResizeOrCallback: callback");
      newBimg = Settings.ImageCallback.callback(img);
    }
    Mat mat = Finder2.makeMat(newBimg, false);
    if (mat.empty()) {
      RunTime.get().terminate(-1, "%s: conversion error --- makes no sense to continue", img);
    }
    return mat;
  }

  /**
   * do a find op with the given pattern in the Finder's image
   * (hasNext() and next() will reveal possible match results)
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
    if (Do.SX.isNull(changedImage)) {
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

  /**
   * do a text find with the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
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
    if (isFindAll) {
      _findInput.setFindAll();
    }
    _results = Finder2.find(_findInput);
    currentMatchIndex = 0;
    return text;
  }

  private int levelWord = 3;
  private int levelLine = 2;

  public boolean findWord(String text) {
    _findInput.setTextLevel(levelWord);
    findText(text);
    return hasNext();
  }

  public boolean findWords(String text) {
    _findInput.setTextLevel(levelWord);
    _findInput.setFindAll();
    findText(text);
    return hasNext();
  }

  public boolean findWords() {
    _findInput.setTextLevel(levelWord);
    _findInput.setFindAll();
    findText("");
    return hasNext();
  }

  public boolean findLine(String text) {
    _findInput.setTextLevel(levelLine);
    findText(text);
    return hasNext();
  }

  public boolean findLines(String text) {
    _findInput.setTextLevel(levelLine);
    _findInput.setFindAll();
    findText(text);
    return hasNext();
  }

  public boolean findLines() {
    _findInput.setTextLevel(levelLine);
    _findInput.setFindAll();
    findText("");
    return hasNext();
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="findAll">
  /**
   * do a findAll op with the given image or the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
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
      setFindAll();
      return findText(imageOrText);
    }
    if (img.isValid()) {
      return findAll(img);
    }
    return null;
  }

  /**
   * do a find op with the given pattern in the Finder's image
   * (hasNext() and next() will reveal possible match results)
   * @param aPtn Pattern
   * @return null. if find setup not possible
   */
  public String findAll(Pattern aPtn)  {
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
   * @param img Image
   * @return null. if find setup not possible
   */
  public String findAll(Image img)  {
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

  /**
   * do a findAll op with the given text in the Finder's image
   * (hasNext() and next() will reveal possible match results)
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

  /**
   *
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
   *
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
  public void remove(){
    destroy();
  }

  public void destroy() {
    _findInput = null;
    _results = null;
    _pattern = null;
  }
//</editor-fold>

//</editor-fold>

}
