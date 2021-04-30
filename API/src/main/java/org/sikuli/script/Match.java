/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.sikuli.script.support.FindAttributes;
import org.sikuli.script.support.IScreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
public class Match extends Region implements Matches, Comparable<Match> {

  //<editor-fold desc="00 instance">

  /**
   * creates a Match on primary screen as (0, 0, 1, 1)
   */
  public Match() {
    init(0, 0, 1, 1, Screen.getPrimaryScreen());
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

  public Match(Rectangle rect) {
    init(rect.x, rect.y, rect.width, rect.height, null);
  }

  public Match(int x, int y, int w, int h) {
    init(x, y, w, h, null);
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

  public Region getRegion() {
    return new Region(this);
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
  public String toStringShort() {
    return doToString(true);
  }

  @Override
  public String toString() {
    return doToString(false);
  }

  public String doToString(boolean asShort) {
    String message = "M[%d,%d %dx%d";
    String onScreen = " ";
    if (isOnScreen()) {
      onScreen = String.format(" On(%s) ", getScreen().getID());
    }
    String time = "";
    if (!asShort) {
      if (lastFindTime > 0 && times.length == 4) {
        time = String.format(" (%d, %d, %d, %d)", lastFindTime, lastSearchTime, times[2], times[3]);
      }
    }
    return String.format(message + onScreen + "S(%.2f)%s]", x, y, w, h,
        ((float) Math.round(score() * 10000)) / 100, time);
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
  private long[] times = new long[0];

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

  //<editor-fold desc="20 Iterator basics">
  private Mat result = null;

  double bestScore = 0;
  double meanScore = 0;
  double stdDevScore = 0;

  private double calcStdDev(List<Double> doubles, double mean) {
    double stdDev = 0;
    for (double doubleVal : doubles) {
      stdDev += (doubleVal - mean) * (doubleVal - mean);
    }
    return Math.sqrt(stdDev / doubles.size());
  }

  public Match(Point point, double score, Mat result) {
    this();
    this.x = point.x;
    this.y = point.y;
    this.simScore = score;
    this.result = result;
  }

  public static Match createFromResult(Element where, FindAttributes findAttributes, Match matchResult, long[] times) {
    Match match = null;
    if (matchResult != null) {
      match = new Match();
      match.setX(where.x + matchResult.x);
      match.setY(where.y + matchResult.y);
      match.setW(findAttributes.target().w);
      match.setH(findAttributes.target().h);
      match.score(matchResult.score());
      match.setTargetOffset(findAttributes.target().offset());
      match.setImage(findAttributes.originalTarget());
      match.onScreen(where.isOnScreen());
      match.lastFindTime = times[0];
      match.lastSearchTime = times[1];
      match.times = times;
      match.result = matchResult.result;
    }
    return match;
  }

  @Override
  public void remove() {

  }

  @Override
  public void forEachRemaining(Consumer<? super Match> action) {

  }
  //</editor-fold>

  //<editor-fold desc="21 Iterator iterate">
  private Core.MinMaxLocResult resultMinMax = null;

  private double currentScore = -1;
  double targetScore = -1;
  double lastScore = -1;
  double scoreMeanDiff = -1;
  int matchCount = 0;

  private int currentX = -1;
  private int currentY = -1;
  private int targetW = -1;
  private int targetH = -1;
  private int marginX = -1;
  private int marginY = -1;

  @Override
  public boolean hasNext() {
    resultMinMax = Core.minMaxLoc(result);
    currentScore = resultMinMax.maxVal;
    currentX = (int) resultMinMax.maxLoc.x;
    currentY = (int) resultMinMax.maxLoc.y;
    if (lastScore < 0) {
      lastScore = currentScore;
      targetScore = image.similarity();
      targetW = image.w;
      targetH = image.h;
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
        if (scoreDiff <= (scoreMeanDiff + 0.01)) { // 0.005
          scoreMeanDiff = ((scoreMeanDiff * matchCount) + scoreDiff) / (matchCount + 1);
          isMatch = true;
        }
      }
    }
    return isMatch;
  }

  @Override
  public Match next() {
    Match match = null;
    if (hasNext()) {
      match = new Match(currentX, currentY, targetW, targetH, currentScore, null);
      matchCount++;
      lastScore = currentScore;
      //int margin = getPurgeMargin();
      Range rangeX = new Range(Math.max(currentX - marginX, 0), Math.min(currentX + marginX, result.width()));
      Range rangeY = new Range(Math.max(currentY - marginY, 0), Math.min(currentY + marginY, result.height()));
      result.colRange(rangeX).rowRange(rangeY).setTo(new Scalar(0f));
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
  //</editor-fold>

  //<editor-fold desc="025 as List / Match">
  @Override
  public List<Match> asList() {
    if (hasNext()) {
      java.util.List<Match> matches = new ArrayList<Match>();
      java.util.List<Double> scores = new ArrayList<>();
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

  @Override
  public Match asMatch() {
    return this;
  }
  //</editor-fold>
}
