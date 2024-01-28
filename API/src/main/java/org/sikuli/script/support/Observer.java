/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.support;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.util.*;

/**
 * INTERNAL USE implements the observe action for a region and calls the ObserverCallBacks
 */
public class Observer {

  private static String me = "Observer: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  public enum State {

    FIRST, UNKNOWN, MISSING, REPEAT, HAPPENED, INACTIVE
  }

  private Region observedRegion = null;
  private Map<String, State> eventStates = Collections.synchronizedMap(new HashMap<String, State>());
  private Map<String, Long> eventRepeatWaitTimes = Collections.synchronizedMap(new HashMap<String, Long>());
  private Map<String, Match> eventMatches = Collections.synchronizedMap(new HashMap<String, Match>());
  private Map<String, Object> eventNames = Collections.synchronizedMap(new HashMap<String, Object>());
  private Map<String, ObserveEvent.Type> eventTypes = Collections.synchronizedMap(new HashMap<String, ObserveEvent.Type>());
  private Map<String, Object> eventCallBacks = Collections.synchronizedMap(new HashMap<String, Object>());
  private Map<String, Integer> eventCounts = Collections.synchronizedMap(new HashMap<String, Integer>());
  private static boolean shouldStopOnFirstEvent = false;

  private Observer() {
  }

  public Observer(Region region) {
    observedRegion = region;
  }

  public void initialize() {
    log(3, "resetting observe states for " + observedRegion.toStringShort());
    synchronized (eventNames) {
      for (String name : eventNames.keySet()) {
        if (eventStates.get(name) != State.INACTIVE) {
          eventStates.put(name, State.FIRST);
        }
        eventCounts.put(name, 0);
        eventMatches.put(name, null);
      }
    }
    shouldStopOnFirstEvent = false;
    if (Observing.getStopOnFirstEvent()) {
      log(lvl, "requested to stop on first event");
      shouldStopOnFirstEvent = true;
    }
  }

  public void setStopOnFirstEvent() {
    shouldStopOnFirstEvent = true;
  }

  public String[] getNames() {
    return eventNames.keySet().toArray(new String[0]);
  }

  public void setActive(String name, boolean state) {
    if (eventNames.containsKey(name)) {
      if (state) {
        eventStates.put(name, State.FIRST);
      } else {
        eventStates.put(name, State.INACTIVE);
      }
    }
  }

  public int getCount(String name) {
    Integer count = eventCounts.get(name);
    return count == null ? 1 : count;
  }

  private <PSC> double getSimiliarity(PSC ptn) {
    double similarity = -1;
    if (ptn instanceof Pattern) {
      similarity = ((Pattern) ptn).getSimilar();
    }
    if (similarity < 0) {
      similarity = Settings.MinSimilarity;
    }
    return similarity;
  }

  private int minChanges = 0;
  private int numChangeCallBacks = 0;
  private String changeEventName = null;

  //<editor-fold desc="obsolete">
  private int numChangeObservers = 0;
  private int getMinChanges() {
    int min = Integer.MAX_VALUE;
    int n;
    for (String name : eventNames.keySet()) {
      if (eventTypes.get(name) != ObserveEvent.Type.CHANGE) continue;
      n = (Integer) eventNames.get(name);
      if (n < min) {
        min = n;
      }
    }
    return min;
  }
  //</editor-fold>

  public <PSC> String addObserver(PSC ptn, ObserverCallBack ob, String name, ObserveEvent.Type type) {
    if (type == ObserveEvent.Type.CHANGE) {
      if (changeEventName == null) {
        changeEventName = name;
      } else {
        name = changeEventName;
      }
      minChanges = (int) ptn;
    }
    eventCallBacks.put(name, ob);
    eventStates.put(name, State.FIRST);
    eventNames.put(name, ptn);
    eventTypes.put(name, type);
    return name;
  }

  public void removeObserver(String name) {
    Observing.remove(name);
    if (eventTypes.get(name) == ObserveEvent.Type.CHANGE) {
      if (eventCallBacks.get(name) != null) {
        numChangeCallBacks--;
      }
      numChangeObservers--;
    }
    eventNames.remove(name);
    eventCallBacks.remove(name);
    eventStates.remove(name);
    eventTypes.remove(name);
    eventCounts.remove(name);
    eventMatches.remove(name);
    eventRepeatWaitTimes.remove(name);
  }

  public boolean hasObservers() {
    return eventNames.size() > 0;
  }

  private void callEventObserver(String name, Match match, long time) {
    Object ptn = eventNames.get(name);
    ObserveEvent.Type obsType = eventTypes.get(name);
    log(lvl, "%s: %s with: %s at: %s", obsType, name, ptn, match);
    ObserveEvent observeEvent = new ObserveEvent(name, obsType, ptn, match, observedRegion, time);
    Object callBack = eventCallBacks.get(name);
    Observing.addEvent(observeEvent);
    if (callBack != null && callBack instanceof ObserverCallBack) {
      log(lvl, "running call back: %s", obsType);
      if (obsType == ObserveEvent.Type.APPEAR) {
        ((ObserverCallBack) callBack).appeared(observeEvent);
      } else if (obsType == ObserveEvent.Type.VANISH) {
        ((ObserverCallBack) callBack).vanished(observeEvent);
      } else if (obsType == ObserveEvent.Type.CHANGE) {
        ((ObserverCallBack) callBack).changed(observeEvent);
      } else if (obsType == ObserveEvent.Type.GENERIC) {
        ((ObserverCallBack) callBack).happened(observeEvent);
      }
    }
  }

  private boolean checkPatterns(ScreenImage simg) {
    log(lvl + 1, "update: checking patterns");
    if (!observedRegion.isObserving()) {
      return false;
    }
    Finder finder = null;
    for (String name : eventStates.keySet()) {
      if (!patternsToCheck()) {
        continue;
      }
      if (eventStates.get(name) == State.REPEAT) {
        if ((new Date()).getTime() < eventRepeatWaitTimes.get(name)) {
          continue;
        } else {
          eventStates.put(name, State.UNKNOWN);
        }
      }
      if (eventStates.get(name) == State.INACTIVE || eventStates.get(name) == State.MISSING) {
        continue;
      }
      Object ptn = eventNames.get(name);
      Image img = Element.getImageFromTarget(ptn);
      if (img == null || !img.isUseable()) {
        Debug.error("EventMgr: checkPatterns: Image not valid", ptn);
        eventStates.put(name, State.MISSING);
        continue;
      }
      Match match = null;
      boolean hasMatch = false;
      long lastSearchTime;
      long now = 0;
//      if (!Settings.UseImageFinder && Settings.CheckLastSeen && null != img.getLastSeen()) {
      if (Settings.CheckLastSeen && null != img.getLastSeen()) {
        Region r = Region.create(img.getLastSeen());
        if (observedRegion.contains(r)) {
          lastSearchTime = (new Date()).getTime();
          Finder f = new Finder(new Screen().capture(r), r);
          f.find(new Pattern(img).similar(Settings.CheckLastSeenSimilar));
          if (f.hasNext()) {
            log(lvl + 1, "checkLastSeen: still there");
            match = new Match(new Region(img.getLastSeen()), img.getLastSeenScore());
            match.setTimes(0, (new Date()).getTime() - lastSearchTime);
            hasMatch = true;
          } else {
            log(lvl + 1, "checkLastSeen: not there");
          }
        }
      }
      if (match == null) {
        if (finder == null) {
//          if (Settings.UseImageFinder) {
//            finder = new ImageFinder(observedRegion);
//            ((ImageFinder) finder).setIsMultiFinder();
//          } else {
//            finder = new Finder(simg, observedRegion);
//          }
          finder = new Finder(simg, observedRegion);
        }
        lastSearchTime = (new Date()).getTime();
        now = (new Date()).getTime();
        finder.find(img);
        if (finder.hasNext()) {
          match = finder.next();
          match.setTimes(0, now - lastSearchTime);
          if (match.getScore() >= getSimiliarity(ptn)) {
            hasMatch = true;
            img.setLastSeen(match); // checkPatterns
          }
        }
      }
      if (hasMatch) {
        eventMatches.put(name, match);
        log(lvl + 1, "(%s): %s match: %s in:%s", eventTypes.get(name), ptn.toString(),
                match.toStringShort(), observedRegion.toStringShort());
      } else if (eventStates.get(ptn) == State.FIRST) {
        log(lvl + 1, "(%s): %s match: %s in:%s", eventTypes.get(name), ptn.toString(),
                match.toStringShort(), observedRegion.toStringShort());
        eventStates.put(name, State.UNKNOWN);
      }
      if (eventStates.get(name) != State.HAPPENED) {
        if (hasMatch && eventTypes.get(name) == ObserveEvent.Type.VANISH) {
          eventMatches.put(name, match);
        }
        if ((hasMatch && eventTypes.get(name) == ObserveEvent.Type.APPEAR)
                || (!hasMatch && eventTypes.get(name) == ObserveEvent.Type.VANISH)) {
          eventStates.put(name, State.HAPPENED);
          eventCounts.put(name, eventCounts.get(name) + 1);
          callEventObserver(name, eventMatches.get(name), now);
          if (shouldStopOnFirstEvent) {
            observedRegion.stopObserver();
          }
        }
      }
      if (!observedRegion.isObserving()) {
        return false;
      }
    }
    return patternsToCheck();
  }

  private boolean patternsToCheck() {
    for (String name : eventNames.keySet()) {
      if (eventTypes.get(name) == ObserveEvent.Type.CHANGE) {
        continue;
      }
      State s = eventStates.get(name);
      if (s == State.FIRST || s == State.UNKNOWN || s == State.REPEAT) {
        return true;
      }
    }
    return false;
  }

  public void repeat(String name, long secs) {
    eventStates.put(name, State.REPEAT);
    if (secs <= 0) {
      secs = (long) observedRegion.getRepeatWaitTime();
    }
    eventRepeatWaitTimes.put(name, (new Date()).getTime() + 1000 * secs);
    log(lvl, "repeat (%s): %s after %d seconds", eventTypes.get(name), name, secs);
  }

  private ScreenImage lastImage = null;

  private boolean checkChanges(ScreenImage img) {
    if (changeEventName == null) {
      return false;
    }
    //boolean leftToDo = false;
    if (lastImage == null) {
      lastImage = img;
      return true;
    }
//    for (String name : eventNames.keySet()) {
//      if (eventTypes.get(name) != ObserveEvent.Type.CHANGE) {
//        continue;
//      }
//      if (eventStates.get(name) == State.REPEAT) {
//        if ((new Date()).getTime() < eventRepeatWaitTimes.get(name)) {
//          continue;
//        }
//      }
//      leftToDo = true;
//    }
    if (eventStates.get(changeEventName) == State.REPEAT) {
      if ((new Date()).getTime() < eventRepeatWaitTimes.get(changeEventName)) {
        return true;
      }
    }
    log(lvl + 1, "update: checking changes");
    Finder finder = new Finder(lastImage.getImage()); // screenImage
    List<Region> result = finder.findChanges(img);
    if (result.size() > 0) {
      callChangeObserver(result);
      if (shouldStopOnFirstEvent) {
        observedRegion.stopObserver();
      }
    }
    lastImage = img;
    return true;
  }

  private void callChangeObserver(List<Region> results) {
    log(lvl, "changes: %d in: %s", results.size(), observedRegion);
    int offX = observedRegion.x;
    int offY = observedRegion.y;
    for (String name : eventNames.keySet()) {
      if (eventTypes.get(name) != ObserveEvent.Type.CHANGE) {
        continue;
      }
      int minChangedPixels = (Integer) eventNames.get(name);
      List<Match> changes = new ArrayList<Match>();
      for (Region rect : results) {
        if (rect.getW() * rect.getH() >= minChangedPixels) {
          rect.x += offX;
          rect.y += offY;
          changes.add(new Match(rect, 1));
        }
      }
      if (changes.size() > 0) {
        long now = (new Date()).getTime();
        eventCounts.put(name, eventCounts.get(name) + 1);
        ObserveEvent observeEvent = new ObserveEvent(name, ObserveEvent.Type.CHANGE, null, null, observedRegion, now);
        observeEvent.setChanges(changes);
        observeEvent.setIndex(minChangedPixels);
        Observing.addEvent(observeEvent);
        Object callBack = eventCallBacks.get(name);
        if (callBack != null) {
          log(lvl, "running call back");
          ((ObserverCallBack) callBack).changed(observeEvent);
        }
      }
    }
  }

  public boolean update(ScreenImage simg) {
    boolean fromPatterns = checkPatterns(simg);
    log(lvl, "update result: Patterns: %s", fromPatterns);
    if (!observedRegion.isObserving()) {
      return false;
    }
    boolean fromChanges = checkChanges(simg);
    log(lvl, "update result: Changes: %s", fromChanges);
    if (!observedRegion.isObserving()) {
      return false;
    }
    return false || fromPatterns || fromChanges;
  }
}
