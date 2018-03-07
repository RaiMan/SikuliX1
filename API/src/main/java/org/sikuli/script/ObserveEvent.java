/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * provides information about the observed event being in the {@link ObserverCallBack}
 */
public class ObserveEvent {

  public enum Type {
    APPEAR, VANISH, CHANGE, GENERIC, FINDFAILED, MISSING
  }

  /**
   * the event's type as ObserveEvent.APPEAR, .VANISH, .CHANGE, ...
   */
  private Type type;

  private Region region = null;
  private Object pattern = null;
  private Match match = null;
  private Image image = null;
  private FindFailedResponse response = FindFailed.defaultFindFailedResponse;
  private int index = -1;
  private List<Match> changes = null;
  private long time;
  private String name;
  private Object[] vals = new Object[] {null, null, null};

  protected ObserveEvent() {
  }

  /**
   * INTERNAL USE ONLY: creates an observed event
   */
  protected ObserveEvent(String name, Type type, Object v1, Object v2, Object v3, long now) {
    init(name, type, v1, v2, v3, now);
  }

	private void init(String name, Type type, Object v1, Object v2, Object v3, long now) {
    this.name = name;
    this.type = type;
    if (now > 0) {
      time = now;
    } else {
      time = new Date().getTime();
    }
    if (Type.GENERIC.equals(type)) {
      setVals(v1, v2, v3);
    } else if (Type.FINDFAILED.equals(type) || Type.MISSING.equals(type)) {
      setRegion(v3);
      setImage(v2);
      setPattern(v1);
    } else {
      setRegion(v3);
      setMatch(v2);
      setPattern(v1);
    }
	}

  /**
   * get the observe event type
   * @return a string containing either APPEAR, VANISH, CHANGE or GENERIC
   */
  public String getType() {
    return type.toString();
  }

  /**
   * check the observe event type
   * @return true if it is APPEAR, false otherwise
   */
  public boolean isAppear() {
    return Type.APPEAR.equals(type);
  }

 /**
   * check the observe event type
   * @return true if it is VANISH, false otherwise
   */
   public boolean isVanish() {
    return Type.VANISH.equals(type);
  }

 /**
   * check the observe event type
   * @return true if it is CHANGE, false otherwise
   */
   public boolean isChange() {
    return Type.CHANGE.equals(type);
  }

 /**
   * check the observe event type
   * @return true if it is GENERIC, false otherwise
   */
   public boolean isGeneric() {
    return Type.GENERIC.equals(type);
  }

 /**
   * check the observe event type
   * @return true if it is FINDFAILED, false otherwise
   */
   public boolean isFindFailed() {
    return Type.FINDFAILED.equals(type);
  }

 /**
   * check the observe event type
   * @return true if it is MISSING, false otherwise
   */
   public boolean isMissing() {
    return Type.MISSING.equals(type);
  }

  /**
   * for type GENERIC: 3 values can be stored in the event
   * (the value's type is known by creator and user of getVals as some private protocol)
   * @param v1
   * @param v2
   * @param v3
   */
  protected void setVals(Object v1, Object v2, Object v3) {
    vals[0] = v1;
    vals[1] = v2;
    vals[2] = v3;
  }

  /**
   * for type GENERIC: (the value's type is known by creator and user of getVals as some private protocol)
   * @return an array with the 3 stored values (might be null)
   */
  public Object[] getVals() {
    return vals;
  }

  /**
   *
   * @return the observer name of this event
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return this event's observer's region
   */
  public Region getRegion() {
    return region;
  }

  protected void setRegion(Object r) {
    if (r instanceof Region) {
      region = (Region) r;
    }
  }

  /**
   *
   * @return the observed match (APEAR, VANISH)
   */
  public Match getMatch() {
    return match;
  }

  protected void setMatch(Object m) {
    if (null != m && m instanceof Match) {
      match = new Match((Match) m);
    }
  }

  protected void setIndex(int index) {
    this.index = index;
  }

  /**
   *
   * @return a list of observed changes as matches (CHANGE)
   */
  public List<Match> getChanges() {
    return changes;
  }

  protected void setChanges(List<Match> c) {
    if (c != null) {
      changes = new ArrayList<Match>();
      changes.addAll(c);
    }
  }

  /**
   *
   * @return the used pattern for this event's observing
   */
  public Pattern getPattern() {
    return (Pattern) pattern;
  }

  public void setPattern(Object p) {
    if (null != p) {
      if (p.getClass().isInstance("")) {
        pattern = new Pattern((String) p);
      } else if (p instanceof Pattern) {
        pattern = new Pattern((Pattern) p);
      } else if (p instanceof Image) {
        pattern = new Pattern((Image) p);
      }
    }
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Object img) {
    image = (Image) img;
  }

  public void setResponse(FindFailedResponse resp) {
    response = resp;
  }

  public FindFailedResponse getResponse() {
    return response;
  }

  public long getTime() {
    return time;
  }

  /**
   * tell the observer to repeat this event's observe action immediately
   * after returning from this handler (APPEAR, VANISH)
   */
  public void repeat() {
    repeat(0);
  }

  /**
   * tell the observer to repeat this event's observe action after given time in secs
   * after returning from this handler (APPEAR, VANISH)
   * @param secs seconds
   */
  public void repeat(long secs) {
    region.getObserver().repeat(name, secs);
  }

  /**
   * @return the number how often this event has already been triggered until now
   */
  public int getCount() {
    return region.getObserver().getCount(name);
  }

  /**
   * stops the observer
   */
  public void stopObserver() {
    region.stopObserver();
  }

  /**
   * stops the observer and prints the given text
   * @param text text
   */
  public void stopObserver(String text) {
    region.stopObserver(text);
  }

  @Override
  public String toString() {
    if (type == Type.CHANGE) {
      return String.format("Event(%s) %s on: %s with: %d count: %d",
            type, name, region, index, getCount());
    } else {
      return String.format("Event(%s) %s on: %s with: %s\nmatch: %s count: %d",
            type, name, region, pattern, match, getCount());
    }
  }
}
