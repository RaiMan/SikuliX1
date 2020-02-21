/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.Core;
import org.sikuli.script.support.IScreen;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * The region on the screen or rectangle in the image,
 * where the given image or text was found.
 * <p>-</p>
 * <p>Is itself a {@link Region} and holds:</p>
 * <ul>
 * <li>the match score (0 ... 1.0) {@link #score()}</li>
 * <li>the click target {@link #getTarget()} (e.g. from {@link Pattern})</li>
 * <li>a ref to the image used for search {@link #getImage()} or {@link #getImageFilename()}</li>
 * <li>the found text {@link #getText()} in case of text find ops</li>
 * </ul>
 */
public class Match extends Region implements Iterator<Match>, Comparable<Match> {

  //<editor-fold desc="00 instance">
  /**
   * creates a Match on primary screen as (0, 0, 1, 1)
   */
  public Match() {
    this(null);
  }

  /**
   * create a copy of another Match or create new Match with element's dimension<br>
   * to e.g. set another TargetOffset for same match
   *
   * @param element other Match (copied) or element (dimension only)
   */
  public Match(Element element) {
    if (SX.isNull(element)) {
      init(0, 0, 1, 1, Screen.getPrimaryScreen());
    } else if (element instanceof Match) {
      Match match = (Match) element;
      init(match.x, match.y, match.w, match.h, match.getScreen());
      copy(match);
    } else {
      init(element.x, element.y, element.w, element.h, element.getScreen());
    }
  }

  /**
   * create a Match from a region with given SimScore
   *
   * @param reg Region
   * @param sc  SimScore
   */
  public Match(Region reg, double sc) {
    init(reg.x, reg.y, reg.w, reg.h, reg.getScreen());
    simScore = sc;
  }

  public Match(Region region, IScreen parent) {
    init(region.x, region.y, region.w, region.h, parent);
  }

  protected Match(Rectangle rect, double confidence, String text, Region base) {
    init(rect.x, rect.y, rect.width, rect.height, base == null ? null : base.getScreen());
    simScore = confidence / 100;
    ocrText = text;
  }

  protected Match(Rectangle rect, double confidence, String text) {
    x = rect.x;
    y = rect.y;
    w = rect.width;
    h = rect.height;
    simScore = confidence / 100;
    ocrText = text;
    onScreen(false);
  }

  public Match(int _x, int _y, int _w, int _h, double score, IScreen _parent) {
    init(_x, _y, _w, _h, _parent);
    simScore = score;
  }


//TODO  protected Match(FindResult f, IScreen _parent) {
//    init(f.getX(), f.getY(), f.getW(), f.getH(), _parent);
//    simScore = f.getScore();
//  }

  private void init(int X, int Y, int W, int H, IScreen parent) {
    x = X;
    y = Y;
    w = W;
    h = H;
    if (parent != null) {
      setScreen(parent);
    }
  }

  public static Match create(Match match, IScreen screen) {
    Match newMatch = new Match(match, screen);
    newMatch.copy(match);
    return newMatch;
  }

  private void copy(Match m) {
    simScore = m.simScore;
    ocrText = m.ocrText;
    image = m.image;
    target = null;
    if (m.target != null) {
      target = new Location(m.target);
    }
    lastFindTime = m.lastFindTime;
    lastSearchTime = m.lastSearchTime;
    setScreen(m.getScreen());
  }

  @Override
  public int compareTo(Match m) {
    if (simScore != m.simScore) {
      return simScore < m.simScore ? -1 : 1;
    }
    if (x != m.x) {
      return x - m.x;
    }
    if (y != m.y) {
      return y - m.y;
    }
    if (w != m.w) {
      return w - m.w;
    }
    if (h != m.h) {
      return h - m.h;
    }
    if (equals(m)) {
      return 0;
    }
    return -1;
  }

  @Override
  public boolean equals(Object oThat) {
    if (this == oThat) {
      return true;
    }
    if (!(oThat instanceof Match)) {
      return false;
    }
    Match that = (Match) oThat;
    return x == that.x && y == that.y && w == that.w && h == that.h
            && Math.abs(simScore - that.simScore) < 1e-5 && getTarget().equals(that.getTarget());
  }

  public String toStringLong() {
    String text = super.toString().replace("R[", "M[");
    if (!isOnScreen()) {

    }
    String starget;
    Location c = getCenter();
    if (target != null && !c.equals(target)) {
      starget = String.format("T:%d,%d", target.x, target.y);
    } else {
      starget = String.format("C:%d,%d", c.x, c.y);
    }
    String findTimes = String.format("[%d msec]", lastFindTime);
    return String.format("%s S:%.2f %s %s", text, simScore, starget, findTimes);
  }

  @Override
  public String toString() {
    String message = "M[%d,%d %dx%d";
    String onScreen = " ";
    if (isOnScreen()) {
      onScreen = String.format(" On(%s) ", getScreen().getID());
    }
    return String.format(message + onScreen + "S(%.2f) (%d, %d)]", x, y, w, h,
            ((float) Math.round(score() * 10000))/100, lastFindTime, lastSearchTime);
  }
  //</editor-fold>

  //<editor-fold desc="01 similarity">
  /**
   * the match score
   *
   * @return a decimal value between 0 (no match) and 1 (exact match)
   */
  public double getScore() {
    return simScore;
  }

  /**
   * the match score
   *
   * @return a decimal value between 0 (no match) and 1 (exact match)
   */
  public double score() {
    return simScore;
  }

  public void score(double simScore) {
    this.simScore = simScore;
  }

  private double simScore = 0;
  //</editor-fold>

  //<editor-fold desc="02 offset">
  private Location target = null;

  /**
   * {@inheritDoc}
   *
   * @return the point defined by target offset (if set) or the center
   */
  @Override
  public Location getTarget() {
    if (target != null) {
      return target;
    }
    return getCenter();
  }

  /**
   * like {@link Pattern#targetOffset(org.sikuli.script.Location) Pattern.targetOffset}
   * sets the click target by offset relative to the center
   *
   * @param offset as a Location
   */
  public Match setTargetOffset(Location offset) {
    target = new Location(getCenter());
    target.translate(offset.x, offset.y);
    return this;
  }

  /**
   * like {@link Pattern#targetOffset(int, int) Pattern.targetOffset}
   * sets the click target relative to the center
   *
   * @param x x offset
   * @param y y offset
   */
  public void setTargetOffset(int x, int y) {
    setTargetOffset(new Location(x, y));
  }

  /**
   * convenience - same as {@link Pattern#getTargetOffset()}
   *
   * @return the relative offset to the center
   */
  public Location getTargetOffset() {
    return new Location(getCenter().getOffset(getTarget()));
  }

  /**
   * INTERNAL USE
   *
   * @param tx x
   * @param ty y
   */
  public Match setTarget(int tx, int ty) {
    target = new Location(tx, ty);
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="03 image">
  private Image image = null;

  /**
   * set the image after finding with success
   *
   * @param img Image
   */
  protected Match setImage(Image img) {
    image = img;
    return this;
  }

  /**
   * get the image used for searching
   *
   * @return image or null
   */
  public Image getTargetImage() {
    return image;
  }

  /**
   * get the filename of the image used for searching
   *
   * @return filename
   */
  public String getImageFilename() {
    return image.getFilename();
  }
  //</editor-fold>

  //<editor-fold desc="04 text">
  private String ocrText = "";

  /**
   * @return the text stored by findWord, findLine, ...
   */
  public String getText() {
    return ocrText;
  }

  /**
   * internally used to set the text found by findWord, findLine, ...
   *
   * @param text
   */
  protected Match setText(String text) {
    ocrText = text;
    return this;
  }
  //</editor-fold>

  //<editor-fold desc="05 index">
  public int getIndex() {
    return index;
  }

  public Match setIndex(int index) {
    this.index = index;
    return this;
  }

  private int index = -1;
  //</editor-fold>

  //<editor-fold desc="09 timing">
  private long lastSearchTime = -1;
  private long lastFindTime = -1;

  /**
   * INTERNAL USE
   * set the elapsed times from search
   *
   * @param ftime time
   * @param stime time
   */
  public void setTimes(long ftime, long stime) {
    lastFindTime = ftime;
    lastSearchTime = stime;
  }

  /**
   * @return this Match's actual waiting time from last successful find
   */
  public long getTime() {
    return lastFindTime;
  }
  //</editor-fold>

  //<editor-fold desc="10 Iterator Match">
  private Core.MinMaxLocResult result = null;

  public Match(Point point, double score, Core.MinMaxLocResult minMax) {
    this();
    this.x = point.x;
    this.y = point.y;
    this.simScore =score;
    this.result = minMax;
  }

  public static Match createFromResult(Image image, Match matchResult, long findTime, long searchTime) {
    Match match = null;
    if (matchResult != null) {
      match = new Match();
      match.setX(image.x + matchResult.x);
      match.setY(image.y + matchResult.y);
      match.setW(image.w);
      match.setH(image.h);
      match.score(matchResult.score());
      match.offset(image.offset());
      match.setImage(image);
      match.onScreen(image.isOnScreen());
      match.lastFindTime = findTime;
      match.lastSearchTime = searchTime;
    }
    return match;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public Match next() {
    return null;
  }

  @Override
  public void remove() {

  }

  @Override
  public void forEachRemaining(Consumer<? super Match> action) {

  }
  //</editor-fold>
}
