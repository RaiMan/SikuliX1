/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import org.sikuli.script.support.IScreen;

import java.awt.*;

/**
 * The region on the screen or rectangle in the image,
 * where the given image or text was found.
 * <p>-</p>
 * <p>Is itself a {@link Region} and holds:</p>
 * <ul>
 * <li>the match score (0 ... 1.0) {@link #getScore()}</li>
 * <li>the click target {@link #getTarget()} (e.g. from {@link Pattern})</li>
 * <li>a ref to the image used for search {@link #getImage()} or {@link #getImageFilename()}</li>
 * <li>the found text {@link #getText()} in case of text find ops</li>
 * </ul>
 */
public class Match extends Region implements Comparable<Match> {

  private double simScore = 0;
  private Location target = null;
  private Image image = null;
  private String ocrText = "";
  private long searchTime = -1;
  private long findTime = -1;
  private int index = -1;
  private boolean onScreen = true;
  private Image where = null;

  public Match() {

  }

  public void setOnScreen(boolean state) {
    onScreen = state;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setWhere(Image img) {
    where = img;
  }

  public Image getWhere() {
    return where;
  }

  /**
   * INTERNAL USE
   * set the elapsed times from search
   *
   * @param ftime time
   * @param stime time
   */
  public void setTimes(long ftime, long stime) {
    findTime = ftime;
    searchTime = stime;
  }

  /**
   * @return msecs search duration including waiting for target
   */
  public long getFindTime() {
    return findTime;
  }

  /**
   * @return msecs search duration excluding waiting for target
   */
  public long getSearchTime() {
    return searchTime;
  }


  /**
   * create a copy of Match object<br>
   * to e.g. set another TargetOffset for same match
   *
   * @param m other Match
   */
  public Match(Match m) {
    if (SX.isNull(m)) {
      init(0, 0, 1, 1, Screen.getPrimaryScreen());
    }
    init(m.x, m.y, m.w, m.h, m.getScreen());
    copy(m);
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
    onScreen = false;
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
    findTime = m.findTime;
    searchTime = m.searchTime;
    where = m.where;
  }

  /**
   * the match score
   *
   * @return a decimal value between 0 (no match) and 1 (exact match)
   */
  public double getScore() {
    return simScore;
  }

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
  public void setTargetOffset(Location offset) {
    target = new Location(getCenter());
    target.translate(offset.x, offset.y);
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
   * set the image after finding with success
   *
   * @param img Image
   */
  protected void setImage(Image img) {
    image = img;
  }

  /**
   * get the image used for searching
   *
   * @return image or null
   */
  public Image getImage() {
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
  protected void setText(String text) {
    ocrText = text;
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

  @Override
  public String toString() {
    String text = toStringShort();
    String starget;
    Location c = getCenter();
    if (target != null && !c.equals(target)) {
      starget = String.format("T(%d,%d)", target.x, target.y);
    } else {
      starget = String.format("C(%d,%d)", c.x, c.y);
    }
    String findTimes = String.format("[%d/%d msec]", findTime, searchTime);
    return String.format("%s %s %s", text, starget, findTimes);
  }

  @Override
  public String toStringShort() { //TODO Match::where == null
    return String.format("M[%d,%d %dx%d]IN(%s) %%%.2f", x, y, w, h,
        (getScreen() == null ? (getWhere() == null ? "?" : getWhere().getName()) : getScreen().getID()), simScore * 100);
  }


  /**
   * INTERNAL USE
   *
   * @param tx x
   * @param ty y
   */
  public void setTarget(int tx, int ty) {
    target = new Location(tx, ty);
  }
}
