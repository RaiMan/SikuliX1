/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import org.sikuli.natives.FindInput;
import org.sikuli.natives.FindResult;
import org.sikuli.natives.FindResults;
import org.sikuli.natives.Mat;
import org.sikuli.natives.Vision;

/**
 * INTERNAL USE implements the observe action for a region and calls the ObserverCallBacks
 */
public class Observer {

  private static String me = "Observer: ";
  private static int lvl = 3;

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  protected enum State {

    FIRST, UNKNOWN, MISSING, REPEAT, HAPPENED, INACTIVE
  }
  private Region observedRegion = null;
  private Mat lastImgMat = null;
  private org.opencv.core.Mat lastImageMat = null;
  private Map<String, State> eventStates = null;
  private Map<String, Long> eventRepeatWaitTimes = null;
  private Map<String, Match> eventMatches = null;
  private Map<String, Object> eventNames = null;
  private Map<String, ObserveEvent.Type> eventTypes = null;
  private Map<String, Object> eventCallBacks = null;
  private Map<String, Integer> eventCounts = null;
  private int minChanges = 0;
  private int numChangeCallBacks = 0;
  private int numChangeObservers = 0;
  private static boolean shouldStopOnFirstEvent = false;

  private Observer() {
  }

  protected Observer(Region region) {
    observedRegion = region;
    eventStates = Collections.synchronizedMap(new HashMap<String, State>());
    eventRepeatWaitTimes = Collections.synchronizedMap(new HashMap<String, Long>());
    eventCounts = Collections.synchronizedMap(new HashMap<String, Integer>());
    eventMatches = Collections.synchronizedMap(new HashMap<String, Match>());
    eventNames = Collections.synchronizedMap(new HashMap<String, Object>());
    eventTypes = Collections.synchronizedMap(new HashMap<String, ObserveEvent.Type>());
    eventCallBacks = Collections.synchronizedMap(new HashMap<String, Object>());
  }

  protected void initialize() {
    log(3, "resetting observe states for " + observedRegion.toStringShort());
    synchronized (eventNames) {
      for (String name : eventNames.keySet()) {
        eventStates.put(name, State.FIRST);
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

  protected void setStopOnFirstEvent() {
    shouldStopOnFirstEvent = true;
  }

  protected String[] getNames() {
    return eventNames.keySet().toArray(new String[0]);
  }

  protected void setActive(String name, boolean state) {
    if (eventNames.containsKey(me)) {
      if (state) {
        eventStates.put(name, State.FIRST);
      } else {
        eventStates.put(name, State.INACTIVE);
      }
    }
  }

  protected int getCount(String name) {
    return eventCounts.get(name);
  }

  private <PSC> float getSimiliarity(PSC ptn) {
    float similarity = -1f;
    if (ptn instanceof Pattern) {
      similarity = ((Pattern) ptn).getSimilar();
    }
    if (similarity < 0) {
      similarity = (float) Settings.MinSimilarity;
    }
    return similarity;
  }

  protected <PSC> void addObserver(PSC ptn, ObserverCallBack ob, String name, ObserveEvent.Type type) {
    eventCallBacks.put(name, ob);
    eventStates.put(name, State.FIRST);
    eventNames.put(name, ptn);
    eventTypes.put(name, type);
    if (type == ObserveEvent.Type.CHANGE) {
      minChanges = getMinChanges();
      numChangeObservers++;
      if (eventCallBacks.get(name) != null) {
        numChangeCallBacks++;
      }
    }
  }

  protected void removeObserver(String name) {
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

  protected boolean hasObservers() {
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
      Object ptn = eventNames.get(name);
      Image img = Image.getImageFromTarget(ptn);
      if (img == null || !img.isUseable()) {
        Debug.error("EventMgr: checkPatterns: Image not valid", ptn);
        eventStates.put(name, State.MISSING);
        continue;
      }
      Match match = null;
      boolean hasMatch = false;
      long lastSearchTime;
      long now = 0;
      if (!Settings.UseImageFinder && Settings.CheckLastSeen && null != img.getLastSeen()) {
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
          if (Settings.UseImageFinder) {
            finder = new ImageFinder(observedRegion);
            ((ImageFinder) finder).setIsMultiFinder();
          } else {
            finder = new Finder(simg, observedRegion);
          }
        }
        lastSearchTime = (new Date()).getTime();
        now = (new Date()).getTime();
        finder.find(img);
        if (finder.hasNext()) {
          match = finder.next();
          match.setTimes(0, now - lastSearchTime);
          if (match.getScore() >= getSimiliarity(ptn)) {
            hasMatch = true;
            img.setLastSeen(match.getRect(), match.getScore());
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

  private boolean patternsToCheck () {
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

  protected void repeat(String name, long secs) {
    eventStates.put(name, State.REPEAT);
    if (secs <= 0) {
      secs = (long) observedRegion.getRepeatWaitTime();
    }
    eventRepeatWaitTimes.put(name, (new Date()).getTime() + 1000 * secs);
    log(lvl, "repeat (%s): %s after %d seconds", eventTypes.get(name), name, secs);
  }

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

  private boolean checkChanges(ScreenImage img) {
    if (numChangeObservers == 0) {
      return false;
    }
    boolean leftToDo = false;
    if (lastImgMat == null) {
      if (Settings.UseImageFinder) {
        lastImageMat = new org.opencv.core.Mat();
      } else {
        lastImgMat = Image.convertBufferedImageToMat(img.getImage());
      }
      return true;
    }
    if (Settings.UseImageFinder && lastImageMat.empty()) {
      lastImageMat = Image.createMat(img.getImage());
      return true;
    }
    for (String name : eventNames.keySet()) {
      if (eventTypes.get(name) != ObserveEvent.Type.CHANGE) {
        continue;
      }
      if (eventStates.get(name) == State.REPEAT) {
        if ((new Date()).getTime() < eventRepeatWaitTimes.get(name)) {
          continue;
        }
      }
      leftToDo = true;
    }
    if (leftToDo) {
      leftToDo = false;
      log(lvl + 1, "update: checking changes");
      if (Settings.UseImageFinder) {
        ImageFinder f = new ImageFinder(lastImageMat);
        f.setMinChanges(minChanges);
        org.opencv.core.Mat current = Image.createMat(img.getImage());
        if (f.hasChanges(current)) {
          //TODO implement ChangeObserver: processing changes
          log(lvl, "TODO: processing changes");
        }
        lastImageMat = current;
      } else {
        FindInput fin = new FindInput();
        fin.setSource(lastImgMat);
        Mat target = Image.convertBufferedImageToMat(img.getImage());
        fin.setTarget(target);
        fin.setSimilarity(minChanges);
        FindResults results = Vision.findChanges(fin);
        if (results.size() > 0) {
          callChangeObserver(results);
          if (shouldStopOnFirstEvent) {
            observedRegion.stopObserver();
          }
        } else {
          leftToDo = true;
        }
        lastImgMat = target;
      }
    }
    return leftToDo |= numChangeCallBacks > 0;
  }

  private void callChangeObserver(FindResults results) {
    int n;
    log(lvl, "changes: %d in: %s", results.size(), observedRegion);
    for (String name : eventNames.keySet()) {
      if (eventTypes.get(name) != ObserveEvent.Type.CHANGE) {
        continue;
      }
      n = (Integer) eventNames.get(name);
      List<Match> changes = new ArrayList<Match>();
      for (int i = 0; i < results.size(); i++) {
        FindResult r = results.get(i);
        if (r.getW() * r.getH() >= n) {
          changes.add(observedRegion.toGlobalCoord(new Match(r, observedRegion.getScreen())));
        }
      }
      if (changes.size() > 0) {
        long now = (new Date()).getTime();
        eventCounts.put(name, eventCounts.get(name) + 1);
        ObserveEvent observeEvent = new ObserveEvent(name, ObserveEvent.Type.CHANGE, null, null, observedRegion, now);
        observeEvent.setChanges(changes);
        observeEvent.setIndex(n);
        Observing.addEvent(observeEvent);
        Object callBack = eventCallBacks.get(name);
        if (callBack != null) {
          log(lvl, "running call back");
          ((ObserverCallBack) callBack).changed(observeEvent);
        }
      }
    }
  }

  protected boolean update(ScreenImage simg) {
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
