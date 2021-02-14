/*
 * Copyright (c) 2010-2021, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * INTERNAL: An abstract super-class for {@link Region} and {@link Image}.
 * <br>
 * <p>BE AWARE: This class cannot be used as such (cannot be instantiated)
 * <br>... instead use the classes Region or Image as needed</p>
 * NOTES:
 * <br>- the classname might change in the future without notice
 * <br>- the intention is, to have only one implementation for features, that are the same for Region and Image
 * <br>- the implementation here is ongoing beginning with version 2.0.2 and hence not complete yet
 * <br>- you might get <b>not-implemented exceptions</b> until complete
 */
public abstract class Element {

  protected static final int logLevel = 3;

  protected static void log(int level, String message, Object... args) {
    if (!Debug.is(level)) {
        return;
    }
    String className = Thread.currentThread().getStackTrace()[2].getClassName();
    String caller = className.substring(className.lastIndexOf(".") +1);
    Debug.logx(level, caller + ": " + message, args);
  }

  protected Element returnThis() {
    return this;
  }

  //<editor-fold desc="01 Fields x, y, w, h">
  /**
   * @return x of top left corner
   */
  public int getX() {
    return x;
  }

  /**
   * X-coordinate of the Region (ignored for Image)
   */
  public int x = 0;

  /**
   * @return y of top left corner
   */
  public int getY() {
    return y;
  }

  /**
   * Y-coordinate of the Region (ignored for Image)
   */
  public int y = 0;

  /**
   * @return width of region/image
   */
  public int getW() {
    return w;
  }

  /**
   * Width of the Region/Image
   */
  public int w = 0;

  /**
   * @return height of region/image
   */
  public int getH() {
    return h;
  }

  /**
   * Height of the Region/Image
   */
  public int h = 0;

  protected boolean isEmpty() {
    return w <= 1 && h <= 1;
  }

  /**
   * INTERNAL: to identify a Region or Image
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * INTERNAL: to identify a Region or Image
   * @param name to be used
   */
  public void setName(String name) {
    this.name = name;
  }

  private String name = "";
  //</editor-fold>

  //<editor-fold desc="08 imageMissingHandler">
  protected void setImageMissingHandler(Object handler) {
    imageMissingHandler = FindFailed.setHandler(handler, ObserveEvent.Type.MISSING);
  }

  protected Object imageMissingHandler = FindFailed.getImageMissingHandler();
  //</editor-fold>

  //<editor-fold desc="10 find image">
  /**
   * finds the given Pattern, String or Image in the region and returns the best match.
   *
   * @param <PSI>  Pattern, String or Image
   * @param target what (PSI) to find in this Region
   * @return If found, the element. null otherwise
   * @throws FindFailed if the Find operation failed
   */
  public <PSI> Match find(PSI target) throws FindFailed {
    //TODO implement find image
    throw new SikuliXception(String.format("Pixels: find: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="12 OCR - read text, line, word, char">
  /**
   * tries to read the text in this region/image<br>
   * might contain misread characters, NL characters and
   * other stuff, when interpreting contained grafics as text<br>
   * Best results: one or more lines of text with no contained grafics
   *
   * @return the text read (utf8 encoded)
   */
  public String text() {
    return OCR.readText(this);
  }

  /**
   * get text from this region/image
   * supposing it is one line of text
   *
   * @return the text or empty string
   */
  public String textLine() {
    return OCR.readLine(this);
  }

  /**
   * get text from this region/image
   * supposing it is one word
   *
   * @return the text or empty string
   */
  public String textWord() {
    return OCR.readWord(this);
  }

  /**
   * get text from this region/image
   * supposing it is one character
   *
   * @return the text or empty string
   */
  public String textChar() {
    return OCR.readChar(this);
  }

  /**
   * find text lines in this region/image
   *
   * @return list of strings each representing one line of text
   */
  public List<String> textLines() {
    List<String> lines = new ArrayList<>();
    List<Match> matches = findLines();
    for (Match match : matches) {
      lines.add(match.getText());
    }
    return lines;
  }

  /**
   * find the words as text in this region/image (top left to bottom right)<br>
   * a word is a sequence of detected utf8-characters surrounded by significant background space
   * might contain characters misinterpreted from contained grafics
   *
   * @return list of strings each representing one word
   */
  public List<String> textWords() {
    List<String> words = new ArrayList<>();
    List<Match> matches = findWords();
    for (Match match : matches) {
      words.add(match.getText());
    }
    return words;
  }

  /**
   * Find all lines as text (top left to bottom right) in this {@link Region} or {@link Image}
   *
   * @return a list of text {@link Match}es or empty list if not found
   */
  public List<Match> findLines() {
    return relocate(OCR.readLines(this));
  }

  /**
   * Find all words as text (top left to bottom right)
   *
   * @return a list of text matches
   */
  public List<Match> findWords() {
    return relocate(OCR.readWords(this));
  }
  //</editor-fold>

  //<editor-fold desc="15 find text (word, line)">
  /**
   * Find the first word as text (top left to bottom right) containing the given text
   *
   * @param word to be searched
   * @return a text match or null if not found
   */
  public Match findWord(String word) {
    Match match = null;
    if (!word.isEmpty()) {
      Object result = doFindText(word, levelWord, false);
      if (result != null) {
        match = relocate( (Match) result);
      }
    }
    return match;
  }

  /**
   * Find all words as text (top left to bottom right) containing the given text
   *
   * @param word to be searched
   * @return a list of text matches
   */
  public List<Match> findWords(String word) {
    Finder finder = ((Finder) doFindText(word, levelWord, true));
    if (null != finder) {
      return finder.getListFor(this);
    }
    return new ArrayList<>();
  }

  /**
   * Find the first line as text (top left to bottom right) containing the given text
   *
   * @param text the line should contain
   * @return a text match or null if not found
   */
  public Match findLine(String text) {
    Match match = null;
    if (!text.isEmpty()) {
      Object result = doFindText(text, levelLine, false);
      if (result != null) {
        match = relocate((Match) result);
      }
    }
    return match;
  }

  /**
   * Find all lines as text (top left to bottom right) containing the given text
   *
   * @param text the lines should contain
   * @return a list of text matches or empty list if not found
   */
  public List<Match> findLines(String text) {
    Finder finder = (Finder) doFindText(text, levelLine, true);
    if (null != finder) {
      return finder.getListFor(this);
    }
    return new ArrayList<>();
  }

  private int levelWord = 3;
  private int levelLine = 2;

  private Object doFindText(String text, int level, boolean multi) {
    Object returnValue = null;
    Finder finder = new Finder(this);
    lastSearchTime = (new Date()).getTime();
    if (level == levelWord) {
      if (multi) {
        if (finder.findWords(text)) {
          returnValue = finder;
        }
      } else {
        if (finder.findWord(text)) {
          returnValue = finder.next();
        }
      }
    } else if (level == levelLine) {
      if (multi) {
        if (finder.findLines(text)) {
          returnValue = finder;
        }
      } else {
        if (finder.findLine(text)) {
          returnValue = finder.next();
        }
      }
    }
    return returnValue;
  }
  //</editor-fold>

  //<editor-fold desc="17 find text like find image">
  public Match findText(String text) throws FindFailed {
    //TODO implement findText
    throw new SikuliXception(String.format("Pixels: findText: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }

  public Match findT(String text) throws FindFailed {
    return findText(text);
  }

  public Match existsText(String text) {
    //TODO existsText: try: findText:true catch: false
    throw new SikuliXception(String.format("Pixels: existsText: not implemented for", this.getClass().getCanonicalName()));
    //return match;
  }

  public Match existsT(String text) {
    return existsText(text);
  }

  public boolean hasText(String text) {
    return null != existsText(text);
  }

  public boolean hasT(String text) {
    return hasText(text);
  }

  public List<Match> findAllText(String text) {
    List<Match> matches = new ArrayList<>();
    throw new SikuliXception(String.format("Pixels: findAllText: not implemented for", this.getClass().getCanonicalName()));
    //return matches;
  }

  public List<Match> findAllT(String text) {
    return findAllText(text);
  }
  //</editor-fold>

  //<editor-fold desc="20 helper">
  /**
   * INTERNAL: get Image from target
   * @param <PSI>   Pattern, Filename, Image, ScreenImage
   * @param target what(PSI) to search
   * @return Image object
   */
  public static <PSI> Image getImageFromTarget(PSI target) {
    if (target instanceof Pattern) {
      return ((Pattern) target).getImage();
    } else if (target instanceof String) {
      Image img = Image.create((String) target);
      return img;
    } else if (target instanceof Image) {
      return (Image) target;
    } else if (target instanceof ScreenImage) {
      return new Image(((ScreenImage) target).getImage());
    } else {
      throw new IllegalArgumentException(String.format("SikuliX: find, wait, exists: invalid parameter: %s", target));
    }
  }

  protected static <SFIRBS> BufferedImage getBufferedImage(SFIRBS whatEver) {
    if (whatEver instanceof String) {
      return Image.create((String) whatEver).get();
    } else if (whatEver instanceof File) {
      return Image.create((File) whatEver).get();
    } else if (whatEver instanceof Match) {
      Region theRegion = new Region((Match) whatEver);
      return theRegion.getImage().get();
    } else if (whatEver instanceof Region) {
      return ((Region) whatEver).getImage().get();
    } else if (whatEver instanceof Image) {
      return ((Image) whatEver).get();
    } else if (whatEver instanceof ScreenImage) {
      return ((ScreenImage) whatEver).getImage();
    } else if (whatEver instanceof BufferedImage) {
      return (BufferedImage) whatEver;
    }
    throw new IllegalArgumentException(String.format("Illegal OCR source: %s", whatEver != null ? whatEver.getClass() : "null"));
  }

  protected Image getImage() {
    throw new SikuliXception(String.format("Pixels: getImage: not implemented for", this.getClass().getCanonicalName()));
  }

  protected List<Match> relocate(List<Match> matches) {
    return matches;
  }

  protected Match relocate(Match match) {
    return match;
  }
  //</editor-fold>

  //<editor-fold desc="25 lastMatch">
  /**
   * The last found {@link Match} in the Region
   */
  protected Match lastMatch = null;

  /**
   * The last found {@link Match}es in the Region
   */
  protected Iterator<Match> lastMatches = null;
  protected long lastSearchTime = -1;
  protected long lastFindTime = -1;
  protected long lastSearchTimeRepeat = -1;

  /**
   * a find operation saves its match on success in this region/image.
   * <br>... unchanged if not successful
   *
   * @return the Match object from last successful find
   */
  public Match getLastMatch() {
    return lastMatch;
  }

  // ************************************************

  /**
   * a searchAll operation saves its matches on success in this region/image
   * <br>... unchanged if not successful
   *
   * @return a Match-Iterator of matches from last successful searchAll
   */
  public Iterator<Match> getLastMatches() {
    return lastMatches;
  }
  //</editor-fold>

  //<editor-fold desc="99 obsolete">
  /**
   * @return a list of matches
   * @deprecated use findLines() instead
   * @see #findLines()
   */
  public List<Match> collectLines() {
    return findLines();
  }

  /**
   * @return a list of lines as strings
   * @deprecated use textLines() instead
   * @see #textLines()
   */
  @Deprecated
  public List<String> collectLinesText() {
    return textLines();
  }

  /**
   * @return a list of matches
   * @deprecated use findWords() instead
   * @see #findWords()
   */
  @Deprecated
  public List<Match> collectWords() {
    return findWords();
  }

  /**
   * @return a list of words sa strings
   * @deprecated use textWords() instead
   * @see #textWords()
   */
  @Deprecated
  public List<String> collectWordsText() {
    return textWords();
  }
  //</editor-fold>


}
